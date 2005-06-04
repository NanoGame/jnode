/*
 * $Id$
 */
package org.jnode.awt;

import java.awt.EventQueue;
import java.awt.VMEventQueue;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class JNodeEventQueue extends EventQueue {

    private boolean shutdown = false;
    
    /**
     * Shut me down.
     */
    final void shutdown() {
        this.shutdown = true;
        VMEventQueue.shutdown(this);
    }
    
    /**
     * Is this queue still live (is has not been shutdown).
     */
    final boolean isLive() {
        return !shutdown;
    }    
}
