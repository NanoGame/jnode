/*
 * $Id$
 */
package org.jnode.driver.net.ethernet.spi;

import org.jnode.driver.net.NetworkException;
import org.jnode.driver.net.spi.AbstractNetDriver;
import org.jnode.net.HardwareAddress;
import org.jnode.net.SocketBuffer;
import org.jnode.net.ethernet.EthernetAddress;
import org.jnode.net.ethernet.EthernetConstants;
import org.jnode.net.ethernet.EthernetHeader;
import org.jnode.net.ethernet.EthernetUtils;

/**
 * @author epr
 */
public abstract class AbstractEthernetDriver extends AbstractNetDriver implements EthernetConstants {

	/**
	 * Gets the maximum transfer unit, the number of bytes this device can
	 * transmit at a time.
	 */
	public int getMTU() {
		return ETH_DATA_LEN;
	}

	/**
	 * @see org.jnode.driver.net.spi.AbstractNetDriver#getDevicePrefix()
	 */
	protected final String getDevicePrefix() {
		return ETH_DEVICE_PREFIX;
	}

	/**
	 * @see org.jnode.driver.net.spi.AbstractNetDriver#onReceive(org.jnode.net.SocketBuffer)
	 */
	public void onReceive(SocketBuffer skbuf) throws NetworkException {
		// Extract ethernet header
		final EthernetHeader hdr = new EthernetHeader(skbuf); 
		skbuf.setLinkLayerHeader(hdr);
		skbuf.setProtocolID(EthernetUtils.getProtocol(hdr));
		skbuf.pull(hdr.getLength());
		// Send to PM
		super.onReceive(skbuf);
	}
	
	/**
	 * @see org.jnode.driver.net.spi.AbstractNetDriver#doTransmit(org.jnode.net.SocketBuffer, org.jnode.net.HardwareAddress)
	 */
	protected final void doTransmit(SocketBuffer skbuf, HardwareAddress destination)
	throws NetworkException {
		skbuf.insert(ETH_HLEN);
		if (destination != null) {
			destination.writeTo(skbuf, 0);
		} else {
			EthernetAddress.BROADCAST.writeTo(skbuf, 0);
		}
		getAddress().writeTo(skbuf, 6);
		skbuf.set16(12, skbuf.getProtocolID());
		doTransmitEthernet(skbuf);
	}

	/**
	 * @see org.jnode.driver.net.spi.AbstractNetDriver#doTransmit(org.jnode.net.SocketBuffer, org.jnode.net.HardwareAddress)
	 */
	protected abstract void doTransmitEthernet(SocketBuffer skbuf)
	throws NetworkException;
}
