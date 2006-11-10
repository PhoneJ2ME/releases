/*
 * Copyright 1990-2006 Sun Microsystems, Inc. All Rights Reserved. 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER 
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 only,
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * version 2 for more details (a copy is included at /legal/license.txt).
 * 
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 or visit www.sun.com if you need additional information or have
 * any questions.
 */

package java.awt;


import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.util.NoSuchElementException;

import java.awt.image.ImageProducer;
import java.awt.image.ImageObserver;
import java.awt.image.ImageConsumer;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.RasterFormatException;
import java.awt.image.BufferedImage;
import java.awt.image.ReplicateScaleFilter;
import java.awt.image.ImageFilter;
import java.awt.image.FilteredImageSource;

/**
	Qt image implementation.
 @version 1.50 05/28/02 */


class QtImage extends java.awt.Image implements ImageConsumer, sun.awt.image.BufferedImagePeer {

	static final int REDRAW_COUNT = 20;

	/** Qt PSD for this image */

	int psd;

	QtGraphicsConfiguration gc;

	int width, height;

	int status;
	boolean empty;

	private Hashtable properties;
	private Vector observers = new Vector();


	/** The producer actually used and returned by getProducer. This may be a scaled
		image producer if the image was prepared with a specified width and height
		or it may be the original image producer if -1 was used for the width and height
	 to prepareImage. */

	ImageProducer producer;
	boolean started;

	private int scanlineCount;

	static native void pDrawImage(int psd, int psd_image, int x, int y, Color bg);

	static private native void initIDs();

	static {

		Toolkit.getDefaultToolkit();

		initIDs();
	}

	/** Creates an offscreen image of the specified width and height. This constructor exists
	 for the QtOffscreenImage class which is a sub class and should not be called directly. */

	QtImage(int width, int height, QtGraphicsConfiguration gc) {

		this.width = width;
		this.height = height;
		status = ImageObserver.ALLBITS | ImageObserver.WIDTH | ImageObserver.HEIGHT;

		if (width > 0 && height > 0) {
			this.gc = gc;
			psd = gc.createCompatibleImagePSD(width, height);
		} else psd = -1;
	}

        /** Creates an image from the supplied image producer. */
                                                                                 
        QtImage(ImageProducer imageProd) {
                this(imageProd, false);
        }
                                                                                 
	/* 5084018.
           Take the parameter isEmpty, which indicates whether we've been
           passed an obvioulsy bad image (like a non-existing file name).
           If this flag is set, at prepareImage() where the actual image
           loading starts, we take a shortcut and pretend that the image
           loaded and errored immediately instead of actually starting 
           ImageProducer's image production.

           Even if the image production starts with a bad image, it will 
           error properly as the decoder won't be good.  But we want 
           this shortcut for the performance reason, and also because
           there can be a race condition in shutdown and production 
           if the app keeps on loading and flushing the errornous image 
           over and over again with pbp's simplified image pipeline model.  
        **/ 
        QtImage(ImageProducer imageProd, boolean isEmpty) {
                                                                                 
                width = -1;
                height = -1;
                producer = imageProd;
                started = false;
                                                                                 
                gc = (QtGraphicsConfiguration)GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
                psd = -1;
                                                                                 
                empty = isEmpty;
        }

	/** Creates a new QtImage from an existing one.
	 This constructor only exists for QtSubImage class and should not be used for any other purpose. */

	QtImage(QtImage image) {
		gc = image.gc;
		psd = gc.pClonePSD(image.psd);
		status = ImageObserver.ALLBITS | ImageObserver.WIDTH | ImageObserver.HEIGHT;
		width = image.width;
		height = image.height;
	}

	protected void finalize() throws Throwable {
		dispose();
		super.finalize();
	}

	protected void dispose() {
		if(psd>0) {
		    psd = gc.pDisposePSD(psd);
		}
	}

	public Graphics getGraphics() {
		throw new UnsupportedOperationException("Graphics can only be created for images created with Component.createImage(width, height)");
	}


	public int getWidth() {
		return width;
	}

	public int getWidth(ImageObserver observer) {
		if (width == -1) {
			addObserver(observer);                       
                        startProduction();
                }

		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getHeight(ImageObserver observer) {
		if (height == -1) {
			addObserver(observer);                   
                        startProduction();
                }

		return height;
	}

	public Object getProperty(String name) {
                return getProperty(name, null);
	}

	public Object getProperty(String name, ImageObserver observer) {
               /* 467980
                * getProperty checks if he properties hashtable exists.
                * If so, the prop is assigned the value from properties
                * that relates to the name specified. If no match is found,
                * then the UndefinedProperty is returned.
                *
                * addObserver is called if prop is null and the observer is null.
                *
              */             
               
               Object prop = null;
                           
               if (properties != null) {
                   prop = properties.get(name);
                   if (prop == null) {
                       prop = UndefinedProperty;
                   }
               }                
               
               if ((prop == null) && (observer != null)) {
                   addObserver(observer);
               }                       
                                     
               return prop;    
	}

	public String[] getPropertyNames() {
		if(properties==null) return null;

		Object[] names = properties.keySet().toArray();
		String[] newNames = new String[names.length];

		System.arraycopy(names, 0, newNames, 0, newNames.length);

		return newNames;
	}



	int getStatus(ImageObserver observer) {	
		
		if (observer != null) {
			 if(observer.imageUpdate(this, status, 0, 0, width, height))
				  addObserver(observer);
		}
		return status;
	}

	public BufferedImage getSubimage(int x, int y, int w, int h) {

		if(x<0) throw new RasterFormatException("x is outside image");
		if(y<0) throw new RasterFormatException("y is outside image");
		if(x+w>width) throw new RasterFormatException("(x + width) is outside image");
		if(y+h>height) throw new RasterFormatException("(y + height) is outside image");
		
		QtSubimage qs = new QtSubimage(this, x, y, w, h);

		return gc.createBufferedImageObject(qs, qs.psd);

	}

	synchronized boolean prepareImage(int width, int height, ImageObserver observer) {
		
		if (width == 0 || height == 0) {
			if ((observer!=null) && observer.imageUpdate(this, ImageObserver.ALLBITS, 0, 0, 0, 0))
				addObserver(observer);
			return true;
		}

	        /* 5084018.  We know the image is bad - error immediately */
		if (empty) {
			started=true;
			status |= ImageObserver.ERROR;
		}

		if (hasError()) {
			if ((observer != null) &&
				   observer.imageUpdate(this, ImageObserver.ERROR|ImageObserver.ABORT, -1, -1, -1, -1))
                               	addObserver(observer);
			return false;
		}

		if(started){

			if ((observer!=null) && observer.imageUpdate(this, status, 0, 0, width, height))
				addObserver(observer);

			return ((status & ImageObserver.ALLBITS) != 0);
		}
		else {
			addObserver(observer);
			startProduction();
		}

		// Some producers deliver image data synchronously
	        return ((status & ImageObserver.ALLBITS) != 0);

	}

		synchronized void addObserver(ImageObserver observer) {

			if(isComplete()){
				if(observer != null){
					observer.imageUpdate(this, status, 0, 0, width, height);
				}
				return;
			}

			if (observer != null && !isObserver(observer)) {
					 observers.addElement(observer);
				}
		}


		private boolean isObserver(ImageObserver observer) {
			return (observer != null && observers.contains(observer));
		}

		private synchronized void removeObserver(ImageObserver observer) {
			if (observer != null) {
				observers.removeElement(observer);
			}
		}

		private synchronized void notifyObservers(final Image img,
                                                          final int info,
                                                          final int x,
                                                          final int y,
                                                          final int w,
                                                          final int h) 
                {                    
                    Enumeration enum = observers.elements();
                    Vector uninterested = null;
                    while (enum.hasMoreElements()) {
                        ImageObserver observer;
                        try {
                            observer = (ImageObserver) enum.nextElement();
                        } catch (NoSuchElementException e) {
                            break;
                        }
                        
                        if (!observer.imageUpdate(img, info, x, y, w, h)) {
                            if (uninterested == null) {
                                uninterested = new Vector();
                            }
                            uninterested.addElement(observer);
                        }
                    }
                    if (uninterested != null) {
                        enum = uninterested.elements();
                        while (enum.hasMoreElements()) {
                            ImageObserver observer = (ImageObserver) enum.nextElement();
                            removeObserver(observer);
                        }
                    }		
		}

	synchronized void startProduction() {

		if (producer != null && !started) {

			if(!producer.isConsumer(this)){
				producer.addConsumer(this);
			}
			started = true;
			producer.startProduction(this);
		}
	}

	public synchronized void flush() {

            dispose();
		 
            started = true;

// Begin of: PBP/PP [6262553]
// Take security fixes from J2SE SunToolkit.java into PBP/PP's SunToolkit.java.
// Image cache is now maintained at the SunToolkit.java via a sun.misc.SoftCache.

//            QtToolkit.clearCache(this);

// End of: PBP/PP [6262553]

            width = -1;
            height = -1;
			
	    if(!empty) {
		status = 0;
		started = false;
 	    }
        
	    if(producer != null) {
		producer.removeConsumer(this);
            
		if(producer instanceof sun.awt.image.InputStreamImageSource){
			((sun.awt.image.InputStreamImageSource)producer).flush();
		}
	    }     
        }

	public ImageProducer getSource() {
		return producer;
	}

	void drawImage(int psd, int x, int y, Color bg) {
		pDrawImage(psd, this.psd, x, y, bg);
	}

	private static native void pDrawImageScaled(int psd, int dx1, int dy1, int dx2, int dy2, int imagePSD, int sx1, int sy1, int sx2, int sy2, Color bg);

	void drawImage(int psd, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bg)
	{
		pDrawImageScaled(psd, dx1, dy1, dx2, dy2, this.psd, sx1, sy1, sx2, sy2, bg);
	}

		boolean isComplete(){
			return ((status & (ImageObserver.ALLBITS | ImageObserver.ERROR | ImageObserver.ABORT)) != 0);
		}

		boolean hasError() {
			return ((status & ImageObserver.ERROR)!=0);
		}

	/*****  --- Consumer Stuff --- *****/
	private native static void imageCompleteLoadBuffer(int imagePSD, boolean dispose);

	public void imageComplete(int stat) {

		switch (stat) {
		case STATICIMAGEDONE:
			status = ImageObserver.ALLBITS;
			break;

		case SINGLEFRAMEDONE:
			status = ImageObserver.FRAMEBITS;
			break;

		case IMAGEERROR:
			status = ImageObserver.ERROR;
			break;

		case IMAGEABORTED:
			status = ImageObserver.ABORT;
			break;
		}

		if (status != 0) {
			imageCompleteLoadBuffer(psd, (status==ImageObserver.ALLBITS));
			notifyObservers(this, status, 0, 0, width, height);
		}
		
		if (isComplete())
			producer.removeConsumer(this);
	}

	public void setColorModel(ColorModel cm) {}

	public synchronized void setDimensions(int width, int height) {

		if ((width>0) && (height>0)) {
			this.width = width;
			this.height = height;

			dispose();

			psd = gc.createCompatibleImagePSD(width, height);

			status = ImageObserver.WIDTH | ImageObserver.HEIGHT;

			notifyObservers(this, status, 0, 0, width, height);
		  }
	}

	public void setProperties(Hashtable props) {

		properties = props;
	}

	public void setHints(int hints) {/*
	 System.out.println("ImageHints:");

	 if((hints&RANDOMPIXELORDER) != 0)
	 System.out.println("Hints: random order");

	 if((hints&TOPDOWNLEFTRIGHT) != 0)
	 System.out.println("Hints: top down");

	 if((hints&COMPLETESCANLINES) != 0)
	 System.out.println("Hints: complete scan lines");

	 if((hints&SINGLEPASS) != 0)
	 System.out.println("Hints: single pass");

	 if((hints&SINGLEFRAME) != 0)
	 System.out.println("Hints: single frame");
	 */}

	/** Unaccelerated native function for setting pixels in the image from any kind of ColorModel. */

	private static native void pSetColorModelBytePixels(int psd, int x, int y, int w, int h, ColorModel cm, byte[] pixels, int offset, int scansize);

	/** Unaccelerated native function for setting pixels in the image from any kind of ColorModel. */

	private static native void pSetColorModelIntPixels(int psd, int x, int y, int w, int h, ColorModel cm, int[] pixels, int offset, int scansize);

	/** Accelerated native function for setting pixels in the image when the ColorModel is an IndexColorModel. */

	private static native void pSetIndexColorModelBytePixels(int psd, int x, int y, int w, int h, ColorModel cm, byte[] pixels, int offset, int scansize);

	/** Accelerated native function for setting pixels in the image when the ColorModel is an IndexColorModel. */

	private static native void pSetIndexColorModelIntPixels(int psd, int x, int y, int w, int h, ColorModel cm, int[] pixels, int offset, int scansize);

	/** Accelerated native function for setting pixels in the image when the ColorModel is a DirectColorModel. */

	private static native void pSetDirectColorModelPixels(int psd, int x, int y, int w, int h, ColorModel cm, int[] pixels, int offset, int scansize, int totalMask);

	/** Gets the ARGB color value at the supplied location. */

	private static native int pGetRGB(int psd, int x, int y);
	
	/* Gets an area of ARGB values and stores them in the array. */
	
	private static native void pGetRGBArray(int psd, int x, int y, int w, int h, int[] pixels, int off, int scansize);
	
	/* Sets the pixel at the supplied location to an ARGB value. */
	
	private static native void pSetRGB(int psd, int x, int y, int rgb);

	private static native void pSetRGBArray(int psd, int startX, int startY, int w, int h, int[] rgbArray, int offset, int scansize);
	
	
	public void setPixels(int x, int y, int w, int h, ColorModel cm, byte[] pixels, int off, int scansize) {
		
		if(psd<0) return;


		/* 6199102 if ( pixels.length < h * scansize + off) */
		if ( pixels.length < (h-1) * scansize + off + w)
			throw new IllegalArgumentException("The pixel array is not big enough");

			// Use accelerated set pixels routine if possible


			 if (cm instanceof IndexColorModel)
				pSetIndexColorModelBytePixels(psd, x, y, w, h, cm, pixels, off, scansize);

			  else pSetColorModelBytePixels(psd, x, y, w, h, cm, pixels, off, scansize);
		
			scanlineCount++;

                status = ImageObserver.SOMEBITS;
                
		if (scanlineCount % REDRAW_COUNT == 0) {
                    notifyObservers(this, ImageObserver.SOMEBITS, x, y, w, h);
		}


   //             System.out.println("SetPixelsByte " + new Rectangle(x, y, w, h));

	}

	public void setPixels(int x, int y, int w, int h, ColorModel cm, int[] pixels, int off, int scansize) {

		if(psd<0) return;
		
		/* 6199102 if ( pixels.length < h * scansize + off) */
		if ( pixels.length < (h-1) * scansize + off + w)
			throw new IllegalArgumentException("The pixel array is not big enough");

			// Use accelerated set pixels routine if possible
			if (cm instanceof DirectColorModel) {
				DirectColorModel dcm = (DirectColorModel)cm;
				int totalMask = dcm.getAlphaMask() | dcm.getRedMask() | dcm.getGreenMask() | dcm.getBlueMask();
				pSetDirectColorModelPixels(psd, x, y, w, h, cm, pixels, off, scansize, totalMask);			
			}
			else if (cm instanceof IndexColorModel)
				pSetIndexColorModelIntPixels(psd, x, y, w, h, cm, pixels, off, scansize);

			 else pSetColorModelIntPixels(psd, x, y, w, h, cm, pixels, off, scansize);


		
		scanlineCount++;

		status = ImageObserver.SOMEBITS;

		if (scanlineCount % REDRAW_COUNT == 0) {
                    notifyObservers(this, ImageObserver.SOMEBITS, x, y, w, h);
		}

     //           System.out.println("SetPixelsInt " + new Rectangle(x, y, w, h));
	}

	public int getType() {
		return gc.getCompatibleImageType();
	}


	public ColorModel getColorModel() {
		return gc.getColorModel();
	}


		public void setRGB(int x, int y, int rgb) {
		if(x<0||y<0||x>=width||y>=height)
			throw new java.lang.ArrayIndexOutOfBoundsException(x+y*width);

			pSetRGB(psd, x, y, rgb);
		};

	public void setRGB(int startX, int startY, int w, int h, int[] rgbArray, int offset, int scansize) {
		
		if((startX<0)||(startY<0)||(startX>=width)||(startY>=height))
			return;
		
		if(w>scansize) return; // Overrun!!			
		
	    pSetRGBArray(psd, startX, startY, w, h, rgbArray, offset, scansize);
	}
	
	public int getRGB(int x, int y) {

		if(x<0||y<0||x>=width||y>=height)
			throw new java.lang.ArrayIndexOutOfBoundsException(x+y*width);

		// pGetPixel returns us an RGB color.
	    int r = pGetRGB(psd, x, y);
		return r;
	}

	public int[] getRGB(int startX, int startY, int w, int h, int[] rgbArray, int offset, int scansize) {

		int yoff = offset;
		int off;

			  if((startX<0)||(startY<0)||((startX+w)>width)||((startY+h)>height))
				   throw new java.lang.ArrayIndexOutOfBoundsException(startX+startY*width);

			  // pGetPixelArray returns a RGB pixel

			  if (rgbArray == null)
					rgbArray = new int[offset + h * scansize];
		
			else	if (rgbArray.length < offset + h * scansize)
				throw new IllegalArgumentException("rgbArray is not large enough to store all the values");

		    pGetRGBArray(psd, startX, startY, w, h, rgbArray, offset, scansize);
		return rgbArray;
	}

	public String toString() {
		return "[psd=" + psd + ",width=" + width + ",height=" + height + ",status=" + status + "]";
	}
}






