/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 
package org.jnode.driver.block;

import java.io.IOException;

import org.jnode.partitions.PartitionTableEntry;

/**
 * Alignment support class implementing the FSBlockDeviceAPI.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class FSBlockAlignmentSupport
	extends BlockAlignmentSupport
	implements FSBlockDeviceAPI {

	private final FSBlockDeviceAPI parentApi;

	/**
	 * @param parentApi
	 * @param alignment
	 */
	public FSBlockAlignmentSupport(FSBlockDeviceAPI parentApi, int alignment) {
		super(parentApi, alignment);
		this.parentApi = parentApi;
	}

	/**
	 * @see org.jnode.driver.block.FSBlockDeviceAPI#getPartitionTableEntry()
	 * @return The partition table entry
	 */
	public PartitionTableEntry getPartitionTableEntry() {
		return parentApi.getPartitionTableEntry();
	}

	/**
	 * @see org.jnode.driver.block.FSBlockDeviceAPI#getSectorSize()
	 * @return int
	 */
	public int getSectorSize() throws IOException {
		return parentApi.getSectorSize();
	}
}
