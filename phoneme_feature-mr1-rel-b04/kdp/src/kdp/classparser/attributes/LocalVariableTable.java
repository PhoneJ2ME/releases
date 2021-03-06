/*
 * Copyright  1990-2006 Sun Microsystems, Inc. All Rights Reserved.
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
 */
/**
 * Represents an individual item in the LocalVariableTable of a
 * Java class file.
 *
 *
 * Revision History
 *   $Log: LocalVariableTable.java,v $
 *   Revision 1.1.1.1  2000/07/07 13:34:24  jrv
 *   Initial import of kdp code
 *
 *   Revision 1.1.1.1  2000/05/31 19:14:48  ritsun
 *   Initial import of kvmdt to CVS
 *
 *   Revision 1.1  2000/04/25 00:30:39  ritsun
 *   Initial revision
 *
 */
package kdp.classparser.attributes;

import kdp.classparser.attributes.*;

public class LocalVariableTable
{
    /** index into code array that begins the range where
        a local variable has a value */
    public int        startPC;
    /** index into code array, startPC + length specifies
        the position where the local variable ceases to
        have a value */
    public int        length;
    /** index into constant pool table containing the name
        of the local variable as a simple name */
    public int        nameIndex;
    /** index into constant pool table containing the
        encoded data type of the local variable */
    public int        descriptorIndex;
    /** local variable must be at index in the local 
        variable array of the current frame */
    public int        index;
}
