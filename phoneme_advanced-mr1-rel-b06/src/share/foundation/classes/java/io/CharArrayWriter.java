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

package java.io;

/**
 * This class implements a character buffer that can be used as an Writer.
 * The buffer automatically grows when data is written to the stream.  The data
 * can be retrieved using toCharArray() and toString().
 *
 * @author	Herb Jellinek
 * @version 	1.15, 02/02/00
 * @since       JDK1.1
 */
public
class CharArrayWriter extends Writer {
    /**
     * The buffer where data is stored.
     */
    protected char buf[];

    /**
     * The number of chars in the buffer.
     */
    protected int count;

    /**
     * Creates a new CharArrayWriter.
     */
    public CharArrayWriter() {
	this(32);
    }

    /**
     * Creates a new CharArrayWriter with the specified initial size.
     *
     * @param initialSize  an int specifying the initial buffer size.
     * @exception IllegalArgumentException if initialSize is negative
     */
    public CharArrayWriter(int initialSize) {
        if (initialSize < 0) {
            throw new IllegalArgumentException("Negative initial size: "
					       + initialSize);
        }
	buf = new char[initialSize];
    }

    /**
     * Writes a character to the buffer.
     */
    public void write(int c) {
	synchronized (lock) {
	    int newcount = count + 1;
	    if (newcount > buf.length) {
		char newbuf[] = new char[Math.max(buf.length << 1, newcount)];
		System.arraycopy(buf, 0, newbuf, 0, count);
		buf = newbuf;
	    }
	    buf[count] = (char)c;
	    count = newcount;
	}
    }

    /**
     * Writes characters to the buffer.
     * @param c	the data to be written
     * @param off	the start offset in the data
     * @param len	the number of chars that are written
     */
    public void write(char c[], int off, int len) {
	if ((off < 0) || (off > c.length) || (len < 0) ||
            ((off + len) > c.length) || ((off + len) < 0)) {
	    throw new IndexOutOfBoundsException();
	} else if (len == 0) {
	    return;
	}
	synchronized (lock) {
	    int newcount = count + len;
	    if (newcount > buf.length) {
		char newbuf[] = new char[Math.max(buf.length << 1, newcount)];
		System.arraycopy(buf, 0, newbuf, 0, count);
		buf = newbuf;
	    }
	    System.arraycopy(c, off, buf, count, len);
	    count = newcount;
	}
    }

    /**
     * Write a portion of a string to the buffer.
     * @param  str  String to be written from
     * @param  off  Offset from which to start reading characters
     * @param  len  Number of characters to be written
     */
    public void write(String str, int off, int len) {
	synchronized (lock) {
	    int newcount = count + len;
	    if (newcount > buf.length) {
		char newbuf[] = new char[Math.max(buf.length << 1, newcount)];
		System.arraycopy(buf, 0, newbuf, 0, count);
		buf = newbuf;
	    }
	    str.getChars(off, off + len, buf, count);
	    count = newcount;
	}
    }

    /**
     * Writes the contents of the buffer to another character stream.
     *
     * @param out	the output stream to write to
     * @throws IOException If an I/O error occurs.
     */
    public void writeTo(Writer out) throws IOException {
	synchronized (lock) {
	    out.write(buf, 0, count);
	}
    }

    /**
     * Resets the buffer so that you can use it again without
     * throwing away the already allocated buffer.
     */
    public void reset() {
	count = 0;
    }

    /**
     * Returns a copy of the input data.
     *
     * @return an array of chars copied from the input data.
     */
    public char toCharArray()[] {
	synchronized (lock) {
	    char newbuf[] = new char[count];
	    System.arraycopy(buf, 0, newbuf, 0, count);
	    return newbuf;
	}
    }

    /**
     * Returns the current size of the buffer.
     *
     * @return an int representing the current size of the buffer.
     */
    public int size() {
	return count;
    }

    /**
     * Converts input data to a string.
     * @return the string.
     */
    public String toString() {
	synchronized (lock) {
	    return new String(buf, 0, count);
	}
    }

    /**
     * Flush the stream.
     */
    public void flush() { }

    /**
     * Close the stream.  This method does not release the buffer, since its
     * contents might still be required.
     */
    public void close() { }

}
