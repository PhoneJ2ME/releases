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

package sun.awt.image;

/**
 * This interface allows the ImageFetcher class to drive the production
 * of image data in an ImageProducer class by calling the doFetch()
 * method from one of a pool of threads which are created to facilitate
 * asynchronous delivery of image data outside of the standard system
 * threads which manage the applications User Interface.
 *
 * @see ImageFetcher
 * @see ImageProducer
 *
 * @version 	1.7 08/19/02
 * @author 	Jim Graham
 */
public interface ImageFetchable {
    /**
     * This method is called by one of the ImageFetcher threads to start
     * the flow of information from the ImageProducer to the ImageConsumer.
     * @see ImageFetcher
     * @see ImageProducer
     */
    public void doFetch();
}
