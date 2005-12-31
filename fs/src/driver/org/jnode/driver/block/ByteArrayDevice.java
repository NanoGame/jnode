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
 
package org.jnode.driver.block;

import java.nio.ByteBuffer;

import org.jnode.driver.Device;
import org.jnode.driver.Driver;
import org.jnode.driver.InvalidDriverException;

/**
 * @author epr
 */
public class ByteArrayDevice extends Device implements BlockDeviceAPI {

	private byte[] array;

	public ByteArrayDevice(byte[] array) {
		super(null, "byte-array" + System.currentTimeMillis());
		this.array = array;
		registerAPI(BlockDeviceAPI.class, this);
	}

	/**
	 * @see org.jnode.driver.block.BlockDeviceAPI#getLength()
	 * @return The length
	 */
	public long getLength() {
		return array.length;
	}

	/**
	 * @param devOffset
	 * @param dest
	 * @param destOffset
	 * @param length
	 * @see org.jnode.driver.block.BlockDeviceAPI#read(long, byte[], int, int)
	 */
	public void read(long devOffset, ByteBuffer dest) {
		//System.arraycopy(array, (int)devOffset, dest, destOffset, length);
        dest.put(this.array, (int)devOffset, dest.remaining());        
	}

	/**
	 * @param devOffset
	 * @param src
	 * @param srcOffset
	 * @param length
	 * @see org.jnode.driver.block.BlockDeviceAPI#write(long, byte[], int, int)
	 */
	public void write(long devOffset, ByteBuffer src) {
		//System.arraycopy(src, srcOffset, array, (int)devOffset, length);
        src.get(this.array, (int)devOffset, src.remaining());        
	}

	/**
	 * @see org.jnode.driver.block.BlockDeviceAPI#flush()
	 */
	public void flush() {
		/* Nothing to do here */
	}

	/**
	 * @param driver
	 * @see org.jnode.driver.Device#setDriver(org.jnode.driver.Driver)
	 * @throws InvalidDriverException
	 */
	public void setDriver(Driver driver) throws InvalidDriverException {
		throw new InvalidDriverException("No driver allowed here.");
	}
}
