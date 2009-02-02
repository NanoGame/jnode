/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.apps.jpartition.commands;

import org.jnode.apps.jpartition.commands.framework.CommandException;
import org.jnode.driver.bus.ide.IDEDevice;
import org.jnode.fs.FileSystem;
import org.jnode.fs.Formatter;

public class FormatPartitionCommand extends BasePartitionCommand {
    private final Formatter<? extends FileSystem<?>> formatter;

    public FormatPartitionCommand(IDEDevice device, int partitionNumber,
            Formatter<? extends FileSystem<?>> formatter) {
        super("format partition", device, partitionNumber);
        this.formatter = formatter;
    }

    @Override
    protected final void doExecute() throws CommandException {
        // TODO Auto-generated method stub
    }

    @Override
    public String toString() {
        return "format partition " + partitionNumber + " on device " + device.getId() + " with " +
                formatter.getFileSystemType().getName();
    }
}
