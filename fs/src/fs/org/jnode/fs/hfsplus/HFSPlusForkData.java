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
 
package org.jnode.fs.hfsplus;

import org.jnode.fs.hfsplus.extent.ExtentDescriptor;
import org.jnode.util.BigEndian;

public class HFSPlusForkData {
    public static final int FORK_DATA_LENGTH = 80;
    private static final int EXTENT_OFFSET = 16;

    private byte[] data;

    /**
     * 
     * @param src
     * @param offset
     */
    public HFSPlusForkData(final byte[] src, final int offset) {
        data = new byte[FORK_DATA_LENGTH];
        System.arraycopy(src, offset, data, 0, FORK_DATA_LENGTH);
    }

    /**
     * 
     * Create a new empty fork data object.
     * 
     * @param totalSize
     * @param clumpSize
     * @param totalBock
     */
    public HFSPlusForkData() {
        data = new byte[FORK_DATA_LENGTH];
    }

    public final long getTotalSize() {
        return BigEndian.getInt64(data, 0);
    }

    public final void setTotalSize(long totalSize) {
        BigEndian.setInt64(data, 0, totalSize);
    }

    public final int getClumpSize() {
        return BigEndian.getInt32(data, 8);
    }

    public final void setClumpSize(int clumpSize) {
        BigEndian.setInt32(data, 8, clumpSize);
    }

    public final int getTotalBlocks() {
        return BigEndian.getInt32(data, 12);
    }

    public final void setTotalBlocks(int totalBlock) {
        BigEndian.setInt32(data, 12, totalBlock);
    }

    public final ExtentDescriptor[] getExtents() {
        ExtentDescriptor[] list = new ExtentDescriptor[8];
        for (int i = 0; i < 8; i++) {
            list[i] = new ExtentDescriptor(data, EXTENT_OFFSET + (i * ExtentDescriptor.EXTENT_DESCRIPTOR_LENGTH));
        }
        return list;
    }

    public final void setExtentDescriptor(int position, ExtentDescriptor desc) {
        int offset = EXTENT_OFFSET + (position * ExtentDescriptor.EXTENT_DESCRIPTOR_LENGTH);
        System.arraycopy(desc.getBytes(), 0, data, offset, ExtentDescriptor.EXTENT_DESCRIPTOR_LENGTH);
    }

    public byte[] getBytes() {
        return data;
    }

    public final String toString() {
        StringBuffer s = new StringBuffer();
        s.append("Total size : ").append(getTotalSize()).append("\n");
        s.append("Clump size : ").append(getClumpSize()).append("\n");
        s.append("Total Blocks : ").append(getTotalBlocks()).append("\n");
        ExtentDescriptor[] list = getExtents();
        for (int i = 0; i < list.length; i++) {
            s.append("Extent[" + i + "]: " + list[i].toString());
        }
        return s.toString();
    }
}
