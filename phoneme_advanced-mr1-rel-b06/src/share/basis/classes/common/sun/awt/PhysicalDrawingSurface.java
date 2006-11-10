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

package sun.awt;

/**
 * The PhysicalDrawingSurface interface provides a common interface
 * which can be implemented by all of the objects which provide
 * implementation-specific information about the physical surface
 * upon which a DrawingSurface resides.  There are no common methods
 * for all of these objects to implement, thus this interface is
 * empty.  This interface merely exists to provide a common root type
 * for all of the possible objects that can be returned from the
 * DrawingSurfaceInfo.getSurface() method under different implementations.
 * The caller must be able to recognize the specific type of the
 * PhysicalDrawingSurface object in order to render directly to
 * that surface.  Many of the physical handles will be usable only
 * by native code which accesses platform-specific drawing interfaces.
 *
 * @version 	1.6, 08/19/02
 * @author 	Jim Graham
 */
public interface PhysicalDrawingSurface {}
