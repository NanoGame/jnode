/*
 * $Id$
 */
package org.jnode.plugin.model;

import java.util.Iterator;
import java.util.Vector;

import nanoxml.XMLElement;

import org.jnode.plugin.Extension;
import org.jnode.plugin.ExtensionPoint;
import org.jnode.plugin.ExtensionPointListener;
import org.jnode.plugin.PluginException;

/**
 * @author epr
 */
public class ExtensionPointModel extends PluginModelObject implements
        ExtensionPoint {

    private final String id;

    private final String uniqueId;

    private final String name;

    private Vector listeners;

    private Extension[] extensionArray;

    private transient Vector extensionsCache;

    public ExtensionPointModel(PluginDescriptorModel plugin, XMLElement e)
            throws PluginException {
        super(plugin);
        id = getAttribute(e, "id", true);
        name = getAttribute(e, "name", true);
        uniqueId = plugin.getId() + "." + id;
        if (id.indexOf('.') >= 0) { throw new PluginException(
                "id cannot contain a '.'"); }
    }

    /**
     * Resolve all references to (elements of) other plugin descriptors
     * 
     * @throws PluginException
     */
    protected void resolve() throws PluginException {
        // Do nothing
    }

    /**
     * Returns the simple identifier of this extension point. This identifier is
     * a non-empty string containing no period characters ('.') and is
     * guaranteed to be unique within the defining plug-in.
     */
    public String getSimpleIdentifier() {
        return id;
    }

    /**
     * Returns the unique identifier of this extension point. This identifier is
     * unique within the plug-in registry, and is composed of the identifier of
     * the plug-in that declared this extension point and this extension point's
     * simple identifier.
     */
    public String getUniqueIdentifier() {
        return uniqueId;
    }

    /**
     * Gets the human readable name of this extensionpoint
     */
    public String getName() {
        return name;
    }

    /**
     * Gets all extensions configured to this extensionpoint.
     */
    public Extension[] getExtensions() {
        if (extensionArray == null) {
            final Vector cache = getExtensionsCache();
            extensionArray = (Extension[]) cache.toArray(new Extension[ cache
                    .size()]);
        }
        return extensionArray;
    }

    /**
     * Add a listener
     * 
     * @param listener
     */
    public void addListener(ExtensionPointListener listener) {
        if (listeners == null) {
            listeners = new Vector();
        }
        listeners.add(listener);
    }

    /**
     * Remove a listener
     * 
     * @param listener
     */
    public void removeListener(ExtensionPointListener listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    /**
     * Add an extension to this point.
     * 
     * @param extension
     */
    protected void add(Extension extension) {
        final Vector extensions = getExtensionsCache();
        if (!extensions.contains(extension)) {
            extensions.add(extension);
            // Re-create the array
            extensionArray = null;
            fireExtensionAdded(extension);
        }
    }

    /**
     * Add an extension to this point.
     * 
     * @param extension
     */
    protected void remove(Extension extension) {
        final Vector extensions = getExtensionsCache();
        extensions.remove(extension);
        extensionArray = null;
        fireExtensionRemoved(extension);
    }

    /**
     * Fire and extensionAdded event to all listeners
     * 
     * @param extension
     */
    protected void fireExtensionAdded(Extension extension) {
        if (listeners != null) {
            for (Iterator i = listeners.iterator(); i.hasNext();) {
                final ExtensionPointListener l = (ExtensionPointListener) i
                        .next();
                l.extensionAdded(this, extension);
            }
        }
    }

    /**
     * Fire and extensionRemoved event to all listeners
     * 
     * @param extension
     */
    protected void fireExtensionRemoved(Extension extension) {
        if (listeners != null) {
            for (Iterator i = listeners.iterator(); i.hasNext();) {
                final ExtensionPointListener l = (ExtensionPointListener) i
                        .next();
                l.extensionRemoved(this, extension);
            }
        }
    }

    /**
     * Gets the extension cache. This will re-create the cache from the
     * extensionArray if needed.
     */
    private Vector getExtensionsCache() {
        if (extensionsCache == null) {
            extensionsCache = new Vector();
            if (extensionArray != null) {
                for (int i = 0; i < extensionArray.length; i++) {
                    extensionsCache.add(extensionArray[ i]);
                }
            }
        }
        return extensionsCache;
    }

    /**
     * @see org.jnode.vm.VmSystemObject#verifyBeforeEmit()
     */
    public void verifyBeforeEmit() {
        super.verifyBeforeEmit();
        //System.out.println("Cache->Array " + extensionsCache);
        if (extensionsCache != null) {
            extensionArray = (Extension[]) extensionsCache
                    .toArray(new Extension[ extensionsCache.size()]);
        }
    }
}