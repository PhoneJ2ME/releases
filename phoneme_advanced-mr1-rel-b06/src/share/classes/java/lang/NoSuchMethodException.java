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

package java.lang;

/**
 * Thrown when a particular method cannot be found.
 *
 * @author     unascribed
 * @version    1.10, 02/02/00
 * @since      JDK1.0
 */
public
class NoSuchMethodException extends Exception {
    /**
     * Constructs a <code>NoSuchMethodException</code> without a detail message.
     */
    public NoSuchMethodException() {
	super();
    }

    /**
     * Constructs a <code>NoSuchMethodException</code> with a detail message. 
     *
     * @param      s   the detail message.
     */
    public NoSuchMethodException(String s) {
	super(s);
    }
}
