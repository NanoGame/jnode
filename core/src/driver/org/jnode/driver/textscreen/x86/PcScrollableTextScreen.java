/*
 * $Id$
 */
package org.jnode.driver.textscreen.x86;

import org.jnode.driver.textscreen.ScrollableTextScreen;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
class PcScrollableTextScreen extends PcBufferTextScreen implements
        ScrollableTextScreen {

    /** Offset of top visible row */
    private int ofsY;
    
    /** Height of the parent screen */
    private final int parentHeight;
    
    /** Maximum row that has valid data */
    private int maxValidY;

    /**
     * @param width
     * @param height
     */
    public PcScrollableTextScreen(int width, int height, AbstractPcTextScreen parent) {
        super(width, height, parent);
        this.parentHeight = parent.getHeight();
    }

    /**
     * @see org.jnode.driver.textscreen.ScrollableTextScreen#ensureVisible(int)
     */
    public void ensureVisible(int row) {
        if (row < ofsY) {
            ofsY = row;
        } else if (row >= ofsY + parentHeight) {
            ofsY = (row - parentHeight) + 1;
        }
    }

    /**
     * @see org.jnode.driver.textscreen.ScrollableTextScreen#scrollDown(int)
     */
    public void scrollDown(int rows) {
        if (rows < 0) {
            throw new IllegalArgumentException("rows < 0");
        }
        final int height = Math.min(maxValidY + 1, getHeight());
        if (ofsY + parentHeight < height) {
            ofsY = ofsY + Math.min(rows, height - (ofsY + parentHeight));
        }
    }

    /**
     * @see org.jnode.driver.textscreen.ScrollableTextScreen#scrollUp(int)
     */
    public void scrollUp(int rows) {
        if (rows < 0) {
            throw new IllegalArgumentException("rows < 0");
        }
        if (ofsY > 0) {
            ofsY = ofsY - Math.min(ofsY, rows);
        }
    }

    /**
     * Return the offset in the buffer of the first visible row.
     * 
     * @return
     */
    protected int getTopOffset() {
        return ofsY * getWidth();
    }  
    
    /**
     * @see org.jnode.driver.textscreen.TextScreen#set(int, char, int, int)
     */
    public void set(int offset, char ch, int count, int color) {
        maxValidY = Math.max(maxValidY, offset / getWidth());
        super.set(offset, ch, count, color);
    }
    /**
     * @see org.jnode.driver.textscreen.TextScreen#set(int, char[], int, int, int)
     */
    public void set(int offset, char[] ch, int chOfs, int length, int color) {
        maxValidY = Math.max(maxValidY, (offset + length-1) / getWidth());
        super.set(offset, ch, chOfs, length, color);
    }
    /**
     * @see org.jnode.driver.textscreen.TextScreen#set(int, char[], int, int, int[], int)
     */
    public void set(int offset, char[] ch, int chOfs, int length, int[] colors,
            int colorsOfs) {
        maxValidY = Math.max(maxValidY, (offset + length-1) / getWidth());
        super.set(offset, ch, chOfs, length, colors, colorsOfs);
    }
}