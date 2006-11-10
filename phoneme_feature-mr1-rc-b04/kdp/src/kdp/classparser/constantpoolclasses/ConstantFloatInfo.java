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
package kdp.classparser.constantpoolclasses;

import java.io.*;

/**
 * Encapsulates a CONSTANT_Float in a Java class file
 * constant pool. 
 *
 *
 * Revision History
 *   $Log: ConstantFloatInfo.java,v $
 *   Revision 1.1.1.1  2000/07/07 13:34:24  jrv
 *   Initial import of kdp code
 *
 *   Revision 1.1.1.1  2000/05/31 19:14:48  ritsun
 *   Initial import of kvmdt to CVS
 *
 *   Revision 1.1  2000/04/25 00:34:06  ritsun
 *   Initial revision
 *
 */
public class ConstantFloatInfo extends ConstantPoolInfo
{
    /** the bytes that make up this field */
    private int            bytes;

    /**
     * Constructor.  Creates the constant float info object.
     *
     * @param             iStream        input stream to read from
     *
     * @exception         IOException    just pass IOExceptions up.
     */
    public ConstantFloatInfo (DataInputStream iStream) throws IOException
    {
        tag = ConstantPoolInfo.CONSTANT_Float;
        bytes = iStream.readInt ();
    }

    /**
     * Returns this ConstantFloatInfo as a string for displaying.
     * Converted to a float as specified in the JVM Specification
     *
     * @return            String         the float as a string.
     */
    public String toString ()
    {
        if (bytes == 0x7f800000) {
            return ("CONSTANT_Float=\t" + "Positive Infinity");
        }

        if (bytes == 0xff800000) {
            return ("CONSTANT_Float=\t" + "Negative Infinity");
        }

        if (((bytes >= 0x7f800001) && (bytes <= 0x7fffffff)) ||
            ((bytes >= 0xff800001) && (bytes <= 0xffffffff))) {
            return ("CONSTANT_Float=\t" + "NaN");
        }
        
        int s = ((bytes >> 31) == 0) ? 1 : -1;
        int e = ((bytes >> 23) & 0xff);
        int m = (e == 0) ? (bytes & 0x7fffff) << 1 :
            (bytes & 0x7fffff) | 0x800000;
        
        float    value = s * m * (2^(e - 150));

        return ("CONSTANT_Float=\t" + value);
    }
}
