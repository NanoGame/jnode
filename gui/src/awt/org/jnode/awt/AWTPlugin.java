/*
 * $Id$
 */
package org.jnode.awt;

import gnu.java.security.actions.SetPropertyAction;

import java.awt.GraphicsEnvironment;
import java.awt.image.VMImageUtils;
import java.security.AccessController;

import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class AWTPlugin extends Plugin {

    //private static final String TOOLKIT = "org.jnode.awt.peer.JNodeToolkit";
    private static final String TOOLKIT = "org.jnode.awt.swingpeers.SwingToolkit";
    
	/**
	 * @param descriptor
	 */
	public AWTPlugin(PluginDescriptor descriptor) {
		super(descriptor);
	}

	/**
	 * Start this plugin
	 * @throws PluginException
	 */
	protected void startPlugin() throws PluginException {
	    AccessController.doPrivileged(new SetPropertyAction("awt.toolkit", TOOLKIT));
		VMImageUtils.setAPI(new VMImageAPIImpl(), this);
		GraphicsEnvironment.setLocalGraphicsEnvironment(new JNodeGraphicsEnvironment());
	}

	/**
	 * Stop this plugin
	 * @throws PluginException
	 */
	protected void stopPlugin() throws PluginException {
		// GraphicsEnvironment.setLocalGraphicsEnvironment(null);
		VMImageUtils.resetAPI(this);
	}
}
