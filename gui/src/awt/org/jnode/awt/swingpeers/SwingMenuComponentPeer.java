/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2006 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.awt.swingpeers;

import java.awt.peer.MenuComponentPeer;
import java.awt.Font;

import javax.swing.JComponent;

import org.jnode.awt.JNodeGenericPeer;

/**
 * @author Levente S\u00e1ntha
 */
abstract class SwingMenuComponentPeer<awtT extends Object, peerT extends JComponent>
        extends JNodeGenericPeer<SwingToolkit, awtT> implements MenuComponentPeer {
    
    protected final peerT jComponent;

    public SwingMenuComponentPeer(SwingToolkit toolkit, awtT component,
            peerT peer) {
        super(toolkit, component);
        this.jComponent = peer;
    }

    public void setFont(Font font) {
        jComponent.setFont(font);
    }
}
