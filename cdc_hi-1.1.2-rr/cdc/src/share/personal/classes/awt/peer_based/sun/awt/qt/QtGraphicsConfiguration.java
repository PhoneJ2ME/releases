/*
 * @(#)QtGraphicsConfiguration.java	1.8 06/10/10
 *
 * Copyright  1990-2008 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation. 
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included at /legal/license.txt). 
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA 
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, CA 95054 or visit www.sun.com if you need additional
 * information or have any questions. 
 *
 */

package sun.awt.qt;


import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.Component;
import java.awt.Canvas;
import java.awt.image.VolatileImage;


class QtGraphicsConfiguration extends GraphicsConfiguration {
    QtGraphicsConfiguration (QtGraphicsDevice device) {
        this.device = device;
    }
	
    public GraphicsDevice getDevice() {
        return device;
    }
	
    public BufferedImage createCompatibleImage(int width, int height) {
        return QtToolkit.createBufferedImage(new QtImage (IMAGE_COMPONENT, width, height));
    }
	
    /**
     * Returns a VolatileImage
     * compatible with this graphics configuration.
     * The returned <code>VolatileImage</code> has
     * a layout and color model that is closest to this native device
     * and may have data that is stored on
     * the device (i.e., in a pixmap) and can therefore be rendered to and
     * blitted from using platform-specific acceleration.
     */
    public VolatileImage createCompatibleVolatileImage(int width, int height) {
        System.out.println("createCompatibleVolatileImage: width: " + width +
                           " height: " + height);
        return new 
            QtVolatileImage(width, height, this);
    }
    public Rectangle getBounds() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		
        return new Rectangle (0, 0, screenSize.width, screenSize.height);
    }
	
    public ColorModel getColorModel() {
        return Toolkit.getDefaultToolkit().getColorModel();
    }
	
    private QtGraphicsDevice device;
	
    private static final Component IMAGE_COMPONENT = new Canvas();
}
