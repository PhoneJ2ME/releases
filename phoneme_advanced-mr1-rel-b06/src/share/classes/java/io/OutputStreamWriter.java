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

import sun.io.CharToByteConverter;
import sun.io.ConversionBufferFullException;


/**
 * An OutputStreamWriter is a bridge from character streams to byte streams:
 * Characters written to it are translated into bytes according to a specified
 * <a href="../lang/package-summary.html#charenc">character encoding</a>.  The
 * encoding that it uses may be specified by name, or the platform's default
 * encoding may be accepted.
 *
 * <p> Each invocation of a write() method causes the encoding converter to be
 * invoked on the given character(s).  The resulting bytes are accumulated in a
 * buffer before being written to the underlying output stream.  The size of
 * this buffer may be specified, but by default it is large enough for most
 * purposes.  Note that the characters passed to the write() methods are not
 * buffered.
 *
 * <p> For top efficiency, consider wrapping an OutputStreamWriter within a
 * BufferedWriter so as to avoid frequent converter invocations.  For example:
 *
 * <pre>
 * Writer out
 *   = new BufferedWriter(new OutputStreamWriter(System.out));
 * </pre>
 *
 * <p> A <i>surrogate pair</i> is a character represented by a sequence of two
 * <tt>char</tt> values: A <i>high</i> surrogate in the range '&#92;uD800' to
 * '&#92;uDBFF' followed by a <i>low</i> surrogate in the range '&#92;uDC00' to
 * '&#92;uDFFF'.  If the character represented by a surrogate pair cannot be
 * encoded by a given encoding then a encoding-dependent <i>substitution
 * sequence</i> is written to the output stream.
 *
 * <p> A <i>malformed surrogate element</i> is a high surrogate that is not
 * followed by a low surrogate or a low surrogate that is not preceeded by a
 * high surrogate.  It is illegal to attempt to write a character stream
 * containing malformed surrogate elements.  The behavior of an instance of
 * this class when a malformed surrogate element is written is not specified.
 *
 * @see BufferedWriter
 * @see OutputStream
 * @see <a href="../lang/package-summary.html#charenc">Character encodings</a>
 *
 * @version 	1.28, 02/02/00
 * @author	Mark Reinhold
 * @since	JDK1.1
 */

public class OutputStreamWriter extends Writer {

    private CharToByteConverter ctb;
    private OutputStream out;

    private static final int defaultByteBufferSize = 8192;
    /* bb is a temporary output buffer into which bytes are written. */
    private byte bb[];
    /* nextByte is where the next byte will be written into bb */
    private int nextByte = 0;
    /* nBytes is the buffer size = defaultByteBufferSize in this class */
    private int nBytes = 0;

    /**
     * Create an OutputStreamWriter that uses the named character encoding.
     *
     * @param  out  An OutputStream
     * @param  enc  The name of a supported
     *              <a href="../lang/package-summary.html#charenc">character
     *              encoding</a>
     *
     * @exception  UnsupportedEncodingException
     *             If the named encoding is not supported
     */
    public OutputStreamWriter(OutputStream out, String enc)
	throws UnsupportedEncodingException
    {
	this(out, CharToByteConverter.getConverter(enc));
    }

    /**
     * Create an OutputStreamWriter that uses the default character encoding.
     *
     * @param  out  An OutputStream
     */
    public OutputStreamWriter(OutputStream out) {
	this(out, CharToByteConverter.getDefault());
    }

    /**
     * Create an OutputStreamWriter that uses the specified character-to-byte
     * converter.  The converter is assumed to have been reset.
     *
     * @param  out  An OutputStream
     * @param  ctb  A CharToByteConverter
     */
    private OutputStreamWriter(OutputStream out, CharToByteConverter ctb) {
	super(out);
	if (out == null) 
	    throw new NullPointerException("out is null");
	this.out = out;
	this.ctb = ctb;
	bb = new byte[defaultByteBufferSize];
	nBytes = defaultByteBufferSize;
    }

    /**
     * Returns the canonical name of the character encoding being used by this
     * stream.  
     * 
     * <p> If this instance was created with the {@link 
     * #OutputStreamWriter(OutputStream, String)} constructor then the returned
     * encoding name, being canonical, may differ from the encoding name passed
     * to the constructor. This method may return <code>null</code> if the stream
     * has been closed. </p>
     *
     * <p> NOTE : In J2ME CDC, there is no concept of historical name, so only
     * canonical name of character encoding is returned.  For a list of
     * acceptable canonical names of the character encoding see:
     * <a href="http://java.sun.com/j2se/1.4.2/docs/guide/intl/encoding.doc.html">http://java.sun.com/j2se/1.4.2/docs/guide/intl/encoding.doc.html</a>
     *
     * @return a String representing the encoding name, or possibly
     *         <code>null</code> if the stream has been closed
     *
     * @see <a href="../lang/package-summary.html#charenc">Character
     *      encodings</a>
     */
    public String getEncoding() {
	synchronized (lock) {
	    if (ctb != null)
		return ctb.getCharacterEncoding();
	    else
		return null;
	}
    }

    /** Check to make sure that the stream has not been closed */
    private void ensureOpen() throws IOException {
	if (out == null)
	    throw new IOException("Stream closed");
    }

    /**
     * Write a single character.
     *
     * @exception  IOException  If an I/O error occurs
     */
    public void write(int c) throws IOException {
	char cbuf[] = new char[1];
	cbuf[0] = (char) c;
	write(cbuf, 0, 1);
    }

    /**
     * Write a portion of an array of characters.
     *
     * @param  cbuf  Buffer of characters
     * @param  off   Offset from which to start writing characters
     * @param  len   Number of characters to write
     *
     * @exception  IOException  If an I/O error occurs
     */
    public void write(char cbuf[], int off, int len) throws IOException {
	synchronized (lock) {
	    ensureOpen();
            if ((off < 0) || (off > cbuf.length) || (len < 0) ||
                ((off + len) > cbuf.length) || ((off + len) < 0)) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0) {
                return;
            }
	    int ci = off, end = off + len;
	    boolean bufferFlushed = false; 
	    while (ci < end) {
		boolean bufferFull = false;
		try {
		    nextByte += ctb.convertAny(cbuf, ci, end,
					    bb, nextByte, nBytes);
		    ci = end;
		}
		catch (ConversionBufferFullException x) {
		    int nci = ctb.nextCharIndex();
		    if ((nci == ci) && bufferFlushed) {
			/* If the buffer has been flushed and it 
			   still does not hold even one character */
			throw new 
			    CharConversionException("Output buffer too small");
		    }
		    ci = nci;
		    bufferFull = true;
		    nextByte = ctb.nextByteIndex();
		} 
		if ((nextByte >= nBytes) || bufferFull) {
		    out.write(bb, 0, nextByte);
		    nextByte = 0;
		    bufferFlushed = true;
		}
	    }
	}
    }

    /**
     * Write a portion of a string.
     *
     * @param  str  A String
     * @param  off  Offset from which to start writing characters
     * @param  len  Number of characters to write
     *
     * @exception  IOException  If an I/O error occurs
     */
    public void write(String str, int off, int len) throws IOException {
	/* Check the len before creating a char buffer */
	if (len < 0)
	    throw new IndexOutOfBoundsException();

	char cbuf[] = new char[len];
	str.getChars(off, off + len, cbuf, 0);
	write(cbuf, 0, len);
    }

    /**
     * Flush the output buffer to the underlying byte stream, without flushing
     * the byte stream itself.  This method is non-private only so that it may
     * be invoked by PrintStream.
     */
    void flushBuffer() throws IOException {
	synchronized (lock) {
	    ensureOpen();

	    for (;;) {
		try {
		    nextByte += ctb.flushAny(bb, nextByte, nBytes);
		}
		catch (ConversionBufferFullException x) {
		    nextByte = ctb.nextByteIndex();
		}
		if (nextByte == 0)
		    break;
		if (nextByte > 0) {
		    out.write(bb, 0, nextByte);
		    nextByte = 0;
		}
	    }
	}
    }

    /**
     * Flush the stream.
     *
     * @exception  IOException  If an I/O error occurs
     */
    public void flush() throws IOException {
	synchronized (lock) {
	    flushBuffer();
	    out.flush();
	}
    }

    /**
     * Close the stream.
     *
     * @exception  IOException  If an I/O error occurs
     */
    public void close() throws IOException {
	synchronized (lock) {
	    if (out == null)
		return;
	    flush();
	    out.close();
	    out = null;
	    bb = null;
	    ctb = null;
	}
    }

}
