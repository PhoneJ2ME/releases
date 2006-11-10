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

// import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.VolatileImage;

/**
 * The <code>GraphicsConfiguration</code> class describes the
 * characteristics of a graphics destination such as a printer or monitor.
 * There can be many <code>GraphicsConfiguration</code> objects associated
 * with a single graphics device, representing different drawing modes or
 * capabilities.  The corresponding native structure will vary from platform
 * to platform.  For example, on X11 windowing systems,
 * each visual is a different <code>GraphicsConfiguration</code>.  
 * On win32, <code>GraphicsConfiguration</code>s represent PixelFormats 
 * available in the current resolution and color depth. 
 * <p>
 * In a virtual device multi-screen environment in which the desktop
 * area could span multiple physical screen devices, the bounds of the 
 * <code>GraphicsConfiguration</code> objects are relative to the
 * virtual coordinate system.  When setting the location of a 
 * component, use {@link #getBounds() getBounds} to get the bounds of 
 * the desired <code>GraphicsConfiguration</code> and offset the location
 * with the coordinates of the <code>GraphicsConfiguration</code>,
 * as the following code sample illustrates:
 * <pre>
 *      Frame f = new Frame(GraphicsConfiguration gc);
 *      Rectangle bounds = gc.getBounds();
 *      f.setLocation(10 + bounds.x, 10 + bounds.y);
 * </pre>
 * To determine if your environment is a virtual device
 * environment, call <code>getBounds</code> on all of the 
 * <code>GraphicsConfiguration</code> objects in your system.  If 
 * any of the origins of the returned bounds are not (0,&nbsp;0),
 * your environment is a virtual device environment.
 * <p>
 * You can also use <code>getBounds</code> to determine the bounds
 * of the virtual device.  Call <code>getBounds</code> on all
 * of the <code>GraphicsConfiguration</code> objects in your
 * system.  Then, calculate the union of all of the bounds returned
 * from the calls to <code>getBounds</code>.  The union is the
 * bounds of the virtual device.  The following code sample
 * calculates the bounds of the virtual device.
 * <pre>
 *	Rectangle virtualBounds = new Rectangle();
 *      GraphicsEnvironment ge = GraphicsEnvironment.
 *		getLocalGraphicsEnvironment();
 *	GraphicsDevice[] gs =
 *		ge.getScreenDevices();
 *	for (int j = 0; j < gs.length; j++) { 
 *	   GraphicsDevice gd = gs[j];
 *	   GraphicsConfiguration[] gc =
 *	      gd.getConfigurations();
 *	   for (int i=0; i < gc.length; i++) {
 *	      virtualBounds =
 *		virtualBounds.union(gc[i].getBounds());
 *	   }
 *      }
 * </pre>                   
 * @see Window
 * @see Frame
 * @see GraphicsEnvironment
 * @see GraphicsDevice
 */

/*
 * TODO:  What to do about capabilities?
 * The
 * capabilities of the device can be determined by enumerating the possible
 * capabilities and checking if the GraphicsConfiguration
 * implements the interface for that capability.
 *
 * @version 1.28, 02/09/01
 */


public abstract class GraphicsConfiguration {
    private static ImageCapabilities defaultImageCaps;

    /**
     * This is an abstract class that cannot be instantiated directly.
     * Instances must be obtained from a suitable factory or query method.
     *
     * @see GraphicsDevice#getConfigurations
     * @see GraphicsDevice#getDefaultConfiguration
     * @see GraphicsDevice#getBestConfiguration
     * @see Graphics2D#getDeviceConfiguration
     */
    protected GraphicsConfiguration() {}

    /**
     * Returns the {@link GraphicsDevice} associated with this
     * <code>GraphicsConfiguration</code>.
     * @return a <code>GraphicsDevice</code> object that is 
     * associated with this <code>GraphicsConfiguration</code>.
     */
    public abstract GraphicsDevice getDevice();

    /**
     * Returns a {@link BufferedImage} with a data layout and color model
     * compatible with this <code>GraphicsConfiguration</code>.  This
     * method has nothing to do with memory-mapping
     * a device.  The returned <code>BufferedImage</code> has
     * a layout and color model that is closest to this native device
     * configuration and can therefore be optimally blitted to this
     * device.
     * @param width the width of the returned <code>BufferedImage</code>
     * @param height the height of the returned <code>BufferedImage</code>
     * @return a <code>BufferedImage</code> whose data layout and color
     * model is compatible with this <code>GraphicsConfiguration</code>.
     */
    public abstract BufferedImage createCompatibleImage(int width, int height);

    /**
     * Returns a <code>BufferedImage</code> that supports the specified
     * transparency and has a data layout and color model
     * compatible with this <code>GraphicsConfiguration</code>.  This
     * method has nothing to do with memory-mapping
     * a device. The returned <code>BufferedImage</code> has a layout and
     * color model that can be optimally blitted to a device
     * with this <code>GraphicsConfiguration</code>.
     * @param width the width of the returned <code>BufferedImage</code>
     * @param height the height of the returned <code>BufferedImage</code>
     * @param transparency the specified transparency mode
     * @return a <code>BufferedImage</code> whose data layout and color  
     * model is compatible with this <code>GraphicsConfiguration</code>
     * and also supports the specified transparency.
     * @see Transparency#OPAQUE
     * @see Transparency#BITMASK
     * @see Transparency#TRANSLUCENT
     */
    
    /*
     public abstract BufferedImage createCompatibleImage(int width, int height,
     int transparency);
     */

    /**
     * Returns a {@link VolatileImage} with a data layout and color model
     * compatible with this <code>GraphicsConfiguration</code>.  
     * The returned <code>VolatileImage</code> 
     * may have data that is stored optimally for the underlying graphics 
     * device and may therefore benefit from platform-specific rendering 
     * acceleration.
     * @param width the width of the returned <code>VolatileImage</code>
     * @param height the height of the returned <code>VolatileImage</code>
     * @return a <code>VolatileImage</code> whose data layout and color
     * model is compatible with this <code>GraphicsConfiguration</code>.
     * @see Component#createVolatileImage(int, int)
     */
    public abstract VolatileImage createCompatibleVolatileImage(int width, 
                                                                int height);

    /** 
     * Returns a {@link VolatileImage} with a data layout and color model 
     * compatible with this <code>GraphicsConfiguration</code>, using 
     * the specified image capabilities. 
     * The returned <code>VolatileImage</code> has 
     * a layout and color model that is closest to this native device 
     * configuration and can therefore be optimally blitted to this 
     * device. 
     * @return a <code>VolatileImage</code> whose data layout and color 
     * model is compatible with this <code>GraphicsConfiguration</code>. 
     * @param width the width of the returned <code>VolatileImage</code> 
     * @param height the height of the returned <code>VolatileImage</code> 
     * @param caps the image capabilities 
     * @exception AWTException if the supplied image capabilities could not 
     * be met by this graphics configuration 
     * @since 1.4 
     */
    public VolatileImage createCompatibleVolatileImage(int width, int height,
        ImageCapabilities caps) throws AWTException {
        // TODO : check caps
        return createCompatibleVolatileImage(width, height);
    }

    public ImageCapabilities getImageCapabilities() {
        if (defaultImageCaps == null) {
            defaultImageCaps = new ImageCapabilities(false);
        }
        return defaultImageCaps;
    }

    /**
     * Returns the {@link ColorModel} associated with this 
     * <code>GraphicsConfiguration</code>.
     * @return a <code>ColorModel</code> object that is associated with
     * this <code>GraphicsConfiguration</code>.
     */
    public abstract ColorModel getColorModel();

    /**
     * Returns the <code>ColorModel</code> associated with this
     * <code>GraphicsConfiguration</code> that supports the specified
     * transparency.
     * @param transparency the specified transparency mode
     * @return a <code>ColorModel</code> object that is associated with
     * this <code>GraphicsConfiguration</code> and supports the 
     * specified transparency.
     */
    //    public abstract ColorModel getColorModel(int transparency);

    /**
     * Returns the default {@link AffineTransform} for this 
     * <code>GraphicsConfiguration</code>. This
     * <code>AffineTransform</code> is typically the Identity transform
     * for most normal screens.  The default <code>AffineTransform</code>
     * maps coordinates onto the device such that 72 user space
     * coordinate units measure approximately 1 inch in device
     * space.  The normalizing transform can be used to make
     * this mapping more exact.  Coordinates in the coordinate space
     * defined by the default <code>AffineTransform</code> for screen and
     * printer devices have the origin in the upper left-hand corner of
     * the target region of the device, with X coordinates
     * increasing to the right and Y coordinates increasing downwards.
     * For image buffers not associated with a device, such as those not
     * created by <code>createCompatibleImage</code>,
     * this <code>AffineTransform</code> is the Identity transform.
     * @return the default <code>AffineTransform</code> for this
     * <code>GraphicsConfiguration</code>.
     */
    //    public abstract AffineTransform getDefaultTransform();

    /**
     *
     * Returns a <code>AffineTransform</code> that can be concatenated
     * with the default <code>AffineTransform</code>
     * of a <code>GraphicsConfiguration</code> so that 72 units in user
     * space equals 1 inch in device space.  
     * <p>
     * For a particular {@link Graphics2D}, g, one
     * can reset the transformation to create
     * such a mapping by using the following pseudocode:
     * <pre>
     *      GraphicsConfiguration gc = g.getGraphicsConfiguration();
     *
     *      g.setTransform(gc.getDefaultTransform());
     *      g.transform(gc.getNormalizingTransform());
     * </pre>
     * Note that sometimes this <code>AffineTransform</code> is identity,
     * such as for printers or metafile output, and that this 
     * <code>AffineTransform</code> is only as accurate as the information
     * supplied by the underlying system.  For image buffers not
     * associated with a device, such as those not created by
     * <code>createCompatibleImage</code>, this
     * <code>AffineTransform</code> is the Identity transform
     * since there is no valid distance measurement.
     * @return an <code>AffineTransform</code> to concatenate to the
     * default <code>AffineTransform</code> so that 72 units in user
     * space is mapped to 1 inch in device space.
     */
    //    public abstract AffineTransform getNormalizingTransform();

    /**
     * Returns the bounds of the <code>GraphicsConfiguration</code>
     * in the device coordinates. In a multi-screen environment
     * with a virtual device, the bounds can have negative X
     * or Y origins.
     * @return the bounds of the area covered by this
     * <code>GraphicsConfiguration</code>.
     * @since 1.3
     */
    public abstract Rectangle getBounds();
}
