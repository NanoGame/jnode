/*
 * $Id$
 */
package org.jnode.net.ipv4.layer;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jnode.driver.Device;
import org.jnode.driver.net.NetDeviceAPI;
import org.jnode.driver.net.NetworkException;
import org.jnode.net.InvalidLayerException;
import org.jnode.net.LayerAlreadyRegisteredException;
import org.jnode.net.NetworkLayer;
import org.jnode.net.NoSuchProtocolException;
import org.jnode.net.ProtocolAddress;
import org.jnode.net.SocketBuffer;
import org.jnode.net.TransportLayer;
import org.jnode.net.ethernet.EthernetConstants;
import org.jnode.net.ipv4.IPv4Address;
import org.jnode.net.ipv4.IPv4Constants;
import org.jnode.net.ipv4.IPv4FragmentList;
import org.jnode.net.ipv4.IPv4Header;
import org.jnode.net.ipv4.IPv4Protocol;
import org.jnode.net.ipv4.IPv4ProtocolAddressInfo;
import org.jnode.net.ipv4.IPv4RoutingTable;
import org.jnode.net.ipv4.IPv4Service;
import org.jnode.net.ipv4.icmp.ICMPProtocol;
import org.jnode.net.ipv4.raw.RAWProtocol;
import org.jnode.net.ipv4.tcp.TCPProtocol;
import org.jnode.net.ipv4.udp.UDPProtocol;
import org.jnode.net.ipv4.util.ResolverImpl;
import org.jnode.util.NumberUtils;
import org.jnode.util.Statistics;

/**
 * @author epr
 */
public class IPv4NetworkLayer implements NetworkLayer, IPv4Constants,
        IPv4Service {

    /** My logger */
    private final Logger log = Logger.getLogger(getClass());

    private final HashMap protocols = new HashMap();

    /** List of in-complete fragments */
    private final HashMap fragments = new HashMap();

    /** System time of last call to removeDeadFragments */
    private long lastFragmentCleanup = 0;

    /** My statistics */
    private final IPv4Statistics stat = new IPv4Statistics();

    /** The routing table */
    private final IPv4RoutingTable rt = new IPv4RoutingTable();

    /** The sender */
    private final IPv4Sender sender;

    /**
     * Initialize a new instance
     *  
     */
    public IPv4NetworkLayer() throws NetworkException {
        sender = new IPv4Sender(this);
        registerProtocol(new ICMPProtocol(this));
        registerProtocol(new TCPProtocol(this));
        registerProtocol(new UDPProtocol(this));
        registerProtocol(new RAWProtocol(this));
    }

    /**
     * Gets the name of this type
     */
    public String getName() {
        return "ipv4";
    }

    /**
     * Gets the protocol ID this packettype handles
     */
    public int getProtocolID() {
        return EthernetConstants.ETH_P_IP;
    }

    /**
     * Can this packet type process packets received from the given device?
     */
    public boolean isAllowedForDevice(Device dev) {
        // For all devices
        return true;
    }

    /**
     * Process a packet that has been received and matches getType()
     * 
     * @param skbuf
     * @param deviceAPI
     * @throws SocketException
     */
    public void receive(SocketBuffer skbuf, NetDeviceAPI deviceAPI)
            throws SocketException {

        // Update statistics
        stat.ipackets.inc();

        // Get IP header
        final IPv4Header hdr = new IPv4Header(skbuf);
        if (!hdr.isChecksumOk()) {
            stat.badsum.inc();
            //log.debug("Ignored IP packet with invalid checksum");
            return;
        }
        // Set the header object in the buffer-field
        skbuf.setNetworkLayerHeader(hdr);
        // Remove header from skbuf-data
        skbuf.pull(hdr.getLength());
        // Trim the end of the message, to we have a valid length
        skbuf.trim(hdr.getDataLength());

        // Now test if the size of the buffer equals the datalength in the
        // header, if now ignore the packet
        if (skbuf.getSize() < hdr.getDataLength()) {
            stat.badlen.inc();
            //log.debug("Ignored IP packet, mismatch between datalength and
            // buffersize");
            return;
        }

        // Get my IP address
        final IPv4ProtocolAddressInfo myAddrInfo = (IPv4ProtocolAddressInfo) deviceAPI
                .getProtocolAddressInfo(getProtocolID());
        if (myAddrInfo == null) {
            stat.nodevaddr.inc();
        }

        // Should I process this packet, or is it for somebody else?
        final IPv4Address dstAddr = hdr.getDestination();
        final boolean shouldProcess;
        if (dstAddr.isBroadcast()) {
            shouldProcess = true;
        } else {
            if (myAddrInfo != null) {
                shouldProcess = myAddrInfo.contains(dstAddr);
            } else {
                // I don't have an IP address yet, if the linklayer says
                // it is for me, we'll process it, otherwise we'll drop it.
                shouldProcess = !skbuf.getLinkLayerHeader()
                        .getDestinationAddress().isBroadcast();
            }
        }
        if (!shouldProcess) { 
        //log.debug("IPPacket not for me, ignoring (dst=" + dstAddr + ")");
        return; }

        // Is it a fragment?
        if (hdr.isFragment()) {
            // Yes it is a fragment
            stat.fragments.inc();
            deliverFragment(hdr, skbuf);
        } else {
            // It is a complete packet, find the protocol handler
            // and let it do the rest
            deliver(hdr, skbuf);
        }

        // Do a cleanup of the fragmentlist from time to time
        final long now = System.currentTimeMillis();
        if ((now - lastFragmentCleanup) >= (IP_FRAGTIMEOUT * 2)) {
            removeDeadFragments();
        }
    }

    /**
     * Gets the routing table
     */
    public IPv4RoutingTable getRoutingTable() {
        return rt;
    }

    /**
     * Deliver a packet to the corresponding protocol
     * 
     * @param hdr
     * @param skbuf
     */
    private void deliver(IPv4Header hdr, SocketBuffer skbuf)
            throws SocketException {
        final IPv4Protocol protocol;
        try {
            protocol = getProtocol(hdr.getProtocol());
            protocol.receive(skbuf);
        } catch (NoSuchProtocolException ex) {
            log.debug("Found unknown IP src=" + hdr.getSource() + ", dst="
                    + hdr.getDestination() + ", prot=0x"
                    + NumberUtils.hex(hdr.getProtocol(), 2));
        }
    }

    /**
     * Process the delivery of a fragment
     * 
     * @param hdr
     * @param skbuf
     * @throws NetworkException
     */
    private void deliverFragment(IPv4Header hdr, SocketBuffer skbuf)
            throws SocketException {
        final Object key = hdr.getFragmentListKey();
        final IPv4FragmentList flist = (IPv4FragmentList) fragments.get(key);
        if (flist == null) {
            // This is a fragment for a new list
            //log.debug("Created fragmentlist " + key);
            fragments.put(key, new IPv4FragmentList(skbuf));
        } else {
            if (flist.isAlive()) {
                flist.add(skbuf);
                if (flist.isComplete()) {
                    // The fragmentlist is now complete, deliver it
                    final SocketBuffer pbuf = flist.getPacket();
                    final IPv4Header phdr = (IPv4Header) pbuf
                            .getNetworkLayerHeader();
                    //log.debug("Fragmentlist is complete, delivering");
                    stat.reassembled.inc();
                    deliver(phdr, pbuf);
                } else {
                    // Fragmentlist is not yet complete
                    //log.debug("Added fragment(" + hdr.getFragmentOffset() +
                    // ") to list " + key);
                }
            } else {
                // Timeout of fragmentlist, destroy it
                fragments.remove(key);
            }
        }
    }

    /**
     * Remove all dead fragments from the fragment list
     */
    private final void removeDeadFragments() {
        final Vector deadFragmentKeys = new Vector();
        // First collect all dead fragment keys
        // Do not remove the directly, since that will create an error
        // in the iterator.
        for (Iterator i = fragments.values().iterator(); i.hasNext();) {
            final IPv4FragmentList f = (IPv4FragmentList) i.next();
            if (!f.isAlive()) {
                deadFragmentKeys.add(f.getKey());
            }
        }
        if (!deadFragmentKeys.isEmpty()) {
            // Now remove all dead fragments
            for (Iterator i = deadFragmentKeys.iterator(); i.hasNext();) {
                fragments.remove(i.next());
            }
            // We're done
            log.debug("Removed " + deadFragmentKeys.size() + " dead fragments");
        }
        // Update our last invocation timestamp
        lastFragmentCleanup = System.currentTimeMillis();
    }

    /**
     * Gets the protocol for a given ID
     * 
     * @param protocolID
     * @throws NoSuchProtocolException
     *             No protocol with the given ID was found.
     */
    public IPv4Protocol getProtocol(int protocolID)
            throws NoSuchProtocolException {
        final IPv4Protocol protocol;
        protocol = (IPv4Protocol) protocols.get(new Integer(protocolID));
        if (protocol == null) { throw new NoSuchProtocolException("with ID "
                + protocolID); }
        return protocol;
    }

    /**
     * Register a protocol
     * 
     * @param protocol
     */
    protected void registerProtocol(IPv4Protocol protocol) {
        protocols.put(new Integer(protocol.getProtocolID()), protocol);
    }

    /**
     * Unregister a protocol
     * 
     * @param protocol
     */
    protected void unregisterProtocol(IPv4Protocol protocol) {
        protocols.remove(new Integer(protocol.getProtocolID()));
    }

    /**
     * Register a transportlayer as possible destination of packets received by
     * this networklayer
     * 
     * @param layer
     */
    public void registerTransportLayer(TransportLayer layer)
            throws LayerAlreadyRegisteredException, InvalidLayerException {
        if (layer instanceof IPv4Protocol) {
            registerProtocol((IPv4Protocol) layer);
        } else {
            throw new InvalidLayerException("No IPv4Protocol");
        }
    }

    /**
     * Unregister a transportlayer
     * 
     * @param layer
     */
    public void unregisterTransportLayer(TransportLayer layer) {
        if (layer instanceof IPv4Protocol) {
            unregisterProtocol((IPv4Protocol) layer);
        }
    }

    /**
     * Gets all registered transport-layers
     */
    public Collection getTransportLayers() {
        final ArrayList result = new ArrayList(protocols.values());
        return result;
    }

    /**
     * Gets a registered transportlayer by its protocol ID.
     * 
     * @param protocolID
     */
    public TransportLayer getTransportLayer(int protocolID)
            throws NoSuchProtocolException {
        return getProtocol(protocolID);
    }

    /**
     * @see org.jnode.net.NetworkLayer#getStatistics()
     */
    public Statistics getStatistics() {
        return stat;
    }

    /**
     * @see org.jnode.net.ipv4.IPv4Service#transmit(org.jnode.net.ipv4.IPv4Header,
     *      org.jnode.net.SocketBuffer)
     */
    public void transmit(IPv4Header hdr, SocketBuffer skbuf)
            throws SocketException {
        sender.transmit(hdr, skbuf);
    }

    /**
     * Gets the protocol addresses for a given name, or null if not found.
     * 
     * @param hostname
     * @return
     */
    public ProtocolAddress[] getHostByName(String hostname) {
        try {
            return ResolverImpl.getInstance().getByName(hostname);
        } catch (UnknownHostException ex) {
            return null;
        }
    }
}
