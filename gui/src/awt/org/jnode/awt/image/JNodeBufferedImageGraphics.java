/*
 * $Id$
 */
package org.jnode.awt.image;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;

import org.apache.log4j.Logger;
import org.jnode.awt.util.AbstractSurfaceGraphics;
import org.jnode.awt.util.BitmapGraphics;
import org.jnode.driver.video.Surface;
import org.jnode.driver.video.util.AbstractSurface;

/**
 * @author epr
 */
public class JNodeBufferedImageGraphics extends AbstractSurfaceGraphics {

	private final BufferedImage image;

	/**
	 * @param src
	 */
	public JNodeBufferedImageGraphics(JNodeBufferedImageGraphics src) {
		super(src);
		this.image = src.image;
	}

	/**
	 * @param image
	 */
	public JNodeBufferedImageGraphics(BufferedImage image) {
		super(new BufferedImageSurface(image), image.getWidth(), image.getHeight());
		this.image = image;
	}

	/**
	 * @see java.awt.Graphics#create()
	 * @return The graphics
	 */
	public Graphics create() {
		return new JNodeBufferedImageGraphics(this);
	}

	/**
	 * @see java.awt.Graphics2D#getDeviceConfiguration()
	 * @return The configuration
	 */
	public GraphicsConfiguration getDeviceConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

	private static class BufferedImageSurface extends AbstractSurface {

		/** My logger */
		private final Logger log = Logger.getLogger(getClass());
		private final BufferedImage sImage;
		private final BitmapGraphics bitmapGraphics;
		private final ColorModel model;

		public BufferedImageSurface(BufferedImage image) {
			super(image.getWidth(), image.getHeight());
			this.model = image.getColorModel();
			this.sImage = image;
			final Raster raster = image.getRaster();
			final DataBuffer dataBuffer = raster.getDataBuffer();
			final SampleModel sampleModel = raster.getSampleModel();
			if (sampleModel instanceof SinglePixelPackedSampleModel) {
				final SinglePixelPackedSampleModel sppSM = (SinglePixelPackedSampleModel) sampleModel;
				final int dataType = dataBuffer.getDataType();
				final int dataTypeSize = DataBuffer.getDataTypeSize(dataType);
				log.debug("dataTypeSize=" + dataTypeSize + ", dataType=" + dataType);
				this.bitmapGraphics = BitmapGraphics.createInstance(dataBuffer, width, height, sppSM.getScanlineStride() * dataTypeSize / 8);
			} else {
				this.bitmapGraphics = null;
			}
		}

		/**
		 * @param color
		 * @return The color value
		 */
		protected int convertColor(Color color) {
			// TODO Make dependent on BufferedImage type
			return color.getRGB();
		}

		/**
		 * Draw a pixel
		 * @param x
		 * @param y
		 * @param color
		 * @param mode
		 */
		protected void drawPixel(int x, int y, int color, int mode) {
			if (bitmapGraphics != null) {
				bitmapGraphics.drawPixels(x, y, 1, color, mode);
			} else {
				if (mode == Surface.PAINT_MODE) {
					sImage.setRGB(x, y, color);
				} else {
					sImage.setRGB(x, y, sImage.getRGB(x, y) ^ color);
				}
			}
		}

		/**
		 * Draw an image to this surface
		 * @param src
		 * @param srcX
		 * @param srcY
		 * @param x The upper left x coordinate 
		 * @param y The upper left y coordinate
		 * @param w
		 * @param h
		 * @param bgColor
		 */
		public void drawCompatibleRaster(Raster src, int srcX, int srcY, int x, int y, int w, int h, Color bgColor) {
			if (bitmapGraphics != null) {
				if (bgColor != null) {
					bitmapGraphics.drawImage(src, srcX, srcY, x, y, w, h, convertColor(bgColor));
				} else {
					bitmapGraphics.drawImage(src, srcX, srcY, x, y, w, h);
				}
			} else {
				final Object pData;
				final WritableRaster r = sImage.getRaster();
				pData = src.getDataElements(srcX, srcY, w, h, null);
				r.setDataElements(x, y, w, h, pData);
			}
		}

		/**
		 * Draw a line
		 * @param x1
		 * @param y1
		 * @param x2
		 * @param y2
		 * @param color
		 * @param mode
		 */
		protected void drawLine(int x1, int y1, int x2, int y2, int color, int mode) {
			if ((y1 == y2) && (bitmapGraphics != null)) {
				bitmapGraphics.drawLine(Math.min(x1, x2), y1, Math.abs(x2 - x1) + 1, color, mode);
			} else {
				super.drawLine(x1, y1, x2, y2, color, mode);
			}
		}

		/**
		 * @see org.jnode.driver.video.Surface#getColorModel()
		 * @return the color model
		 */
		public ColorModel getColorModel() {
			return model;
		}
	}
}
