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

package java.util.zip;

/**
 * A class that can be used to compute the CRC-32 of a data stream.
 *
 * @see		Checksum
 * @version 	1.25, 05/03/00
 * @author 	David Connelly
 */
public
class CRC32 implements Checksum {
    private int crc;

    /*
     * Loads the ZLIB library.
     */
    private static native void init();
    static {
	java.security.AccessController.doPrivileged(
		  new sun.security.action.LoadLibraryAction("zip"));
	/* Work-around for Symbian tool bug.  No longer needed. */
	init();
    }

    /**
     * Creates a new CRC32 object.
     */
    public CRC32() {
    }
   

    /**
     * Updates CRC-32 with specified byte.
     */
    public void update(int b) {
	crc = update(crc, b);
    }

    /**
     * Updates CRC-32 with specified array of bytes.
     */
    public void update(byte[] b, int off, int len) {
	if (b == null) {
	    throw new NullPointerException();
	}
        if (off < 0 || len < 0 || off > b.length - len) {
	    throw new ArrayIndexOutOfBoundsException();
	}
	crc = updateBytes(crc, b, off, len);
    }

    /**
     * Updates checksum with specified array of bytes.
     *
     * @param b the array of bytes to update the checksum with
     */
    public void update(byte[] b) {
	crc = updateBytes(crc, b, 0, b.length);
    }

    /**
     * Resets CRC-32 to initial value.
     */
    public void reset() {
	crc = 0;
    }

    /**
     * Returns CRC-32 value.
     */
    public long getValue() {
	return (long)crc & 0xffffffffL;
    }

    private native static int update(int crc, int b);
    private native static int updateBytes(int crc, byte[] b, int off, int len);
}
