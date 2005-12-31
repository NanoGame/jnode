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
 
package org.jnode.java.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;


/**
 * A FileHandle represents a single, opened file for a single principal.
 * @author epr
 */
public interface VMFileHandle {

	/**
	 * Gets the length (in bytes) of this file
	 * @return long
	 */
	public long getLength();
	
	/**
	 * Sets the length of this file.
	 * @param length
	 * @throws IOException
	 */
	public void setLength(long length)
	throws IOException;

	/**
	 * Gets the current position in the file
	 * @return long
	 */
	public long getPosition();
	
	/**
	 * Sets the position in the file.
	 * @param position
	 * @throws IOException
	 */
	public void setPosition(long position)
	throws IOException;

	/**
	 * Read <code>len</code> bytes from the given position.
	 * The read data is read fom this file starting at offset <code>fileOffset</code>
	 * and stored in <code>dest</code> starting at offset <code>ofs</code>.
	 * @param dest
	 * @param off
	 * @param len
     * @return
	 * @throws IOException
	 */	
//	public int read(byte[] dest, int off, int len)
//	throws IOException;
      public int read(ByteBuffer dest)
      throws IOException;
    
	
	/**
	 * Write <code>len</code> bytes to the given position. 
	 * The data is read from <code>src</code> starting at offset
	 * <code>ofs</code> and written to this file starting at offset <code>fileOffset</code>.
	 * @param src
	 * @param off
	 * @param len
	 * @throws IOException
	 */	
	//public void write(byte[] src, int off, int len)
    public void write(ByteBuffer src)
	throws IOException;

	/**
	 * Close this file.
	 */
	public void close()
	throws IOException;
	
	/**
	 * Has this handle been closed?
	 */
	public boolean isClosed();

    public int available();

    public void unlock(long pos, long len);

    public int read() throws IOException;

    public void write(int b) throws IOException;

    public boolean lock();

    public MappedByteBuffer mapImpl(char mode, long position, int size);
}
