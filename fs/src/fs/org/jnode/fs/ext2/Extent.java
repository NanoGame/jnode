package org.jnode.fs.ext2;

/**
 * An ext4 extent object.
 *
 * @author Luke Quinane
 */
public class Extent {
    /**
     * The length of an extent.
     */
    public static final int EXTENT_LENGTH = 12;

    /**
     * The data for the extent.
     */
    private byte[] data;

    /**
     * Create an extent object.
     *
     * @param data the data for the extent.
     */
    public Extent(byte[] data) {
        this.data = new byte[EXTENT_LENGTH];
        System.arraycopy(data, 0, this.data, 0, EXTENT_LENGTH);
    }

    public long getBlockIndex() {
        return Ext2Utils.get32(data, 0);
    }

    public int getBlockCount() {
        return Ext2Utils.get16(data, 4);
    }

    public long getStartLow() {
        return Ext2Utils.get32(data, 8);
    }

    public int getStartHigh() {
        return Ext2Utils.get16(data, 6);
    }
}
