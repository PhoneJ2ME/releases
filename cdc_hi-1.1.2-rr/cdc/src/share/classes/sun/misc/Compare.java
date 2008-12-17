/*
 * @(#)Compare.java	1.15 06/10/10
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

/**
 * Compare: an interface to enable users to define the result of
 * 	    a comparison of two objects.
 *
 * @version 1.9, 02/02/00
 * @author Sunita Mani
 */

package sun.misc;

public interface Compare {

    /**
     * doCompare
     *
     * @param  obj1 first object to compare.
     * @param  obj2 second object to compare.
     * @return -1 if obj1 < obj2, 0 if obj1 == obj2, 1 if obj1 > obj2.
     */
    public int doCompare(Object obj1, Object obj2);

}
