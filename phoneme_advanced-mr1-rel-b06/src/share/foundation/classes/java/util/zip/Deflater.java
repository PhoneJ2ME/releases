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
 * This class provides support for general purpose compression using the
 * popular ZLIB compression library. The ZLIB compression library was
 * initially developed as part of the PNG graphics standard and is not
 * protected by patents. It is fully described in the specifications at 
 * the <a href="package-summary.html#package_description">java.util.zip
 * package description</a>.
 *
 * <p>The following code fragment demonstrates a trivial compression
 * and decompression of a string using <tt>Deflater</tt> and
 * <tt>Inflater</tt>.
 *
 * <blockquote><pre>
 * // Encode a String into bytes
 * String inputString = "blahblahblah\u20AC\u20AC";
 * byte[] input = inputString.getBytes("UTF-8");
 *
 * // Compress the bytes
 * byte[] output = new byte[100];
 * Deflater compresser = new Deflater();
 * compresser.setInput(input);
 * compresser.finish();
 * int compressedDataLength = compresser.deflate(output);
 *
 * // Decompress the bytes
 * Inflater decompresser = new Inflater();
 * decompresser.setInput(output, 0, compressedDataLength);
 * byte[] result = new byte[100];
 * int resultLength = decompresser.inflate(result);
 * decompresser.end();
 *
 * // Decode the bytes into a String
 * String outputString = new String(result, 0, resultLength, "UTF-8");
 * </pre></blockquote>
 * 
 * @see		Inflater
 * @version 	1.35, 05/03/00
 * @author 	David Connelly
 */
public
class Deflater {
    private long strm;
    private byte[] buf = new byte[0];
    private int off, len;
    private int level, strategy;
    private boolean setParams;
    private boolean finish, finished;

    /**
     * Compression method for the deflate algorithm (the only one currently
     * supported).
     */
    public static final int DEFLATED = 8;

    /**
     * Compression level for no compression.
     */
    public static final int NO_COMPRESSION = 0;

    /**
     * Compression level for fastest compression.
     */
    public static final int BEST_SPEED = 1;

    /**
     * Compression level for best compression.
     */
    public static final int BEST_COMPRESSION = 9;

    /**
     * Default compression level.
     */
    public static final int DEFAULT_COMPRESSION = -1;

    /**
     * Compression strategy best used for data consisting mostly of small
     * values with a somewhat random distribution. Forces more Huffman coding
     * and less string matching.
     */
    public static final int FILTERED = 1;

    /**
     * Compression strategy for Huffman coding only.
     */
    public static final int HUFFMAN_ONLY = 2;

    /**
     * Default compression strategy.
     */
    public static final int DEFAULT_STRATEGY = 0;

    /*
     * Loads the ZLIB library.
     */
    static {
	java.security.AccessController.doPrivileged(
		  new sun.security.action.LoadLibraryAction("zip"));
	initIDs();
    }

    /**
     * Creates a new compressor using the specified compression level.
     * If 'nowrap' is true then the ZLIB header and checksum fields will
     * not be used in order to support the compression format used in
     * both GZIP and PKZIP.
     * @param level the compression level (0-9)
     * @param nowrap if true then use GZIP compatible compression
     */
    public Deflater(int level, boolean nowrap) {
	this.level = level;
	this.strategy = DEFAULT_STRATEGY;
	strm = init(level, DEFAULT_STRATEGY, nowrap);
    }

    /** 
     * Creates a new compressor using the specified compression level.
     * Compressed data will be generated in ZLIB format.
     * @param level the compression level (0-9)
     */
    public Deflater(int level) {
	this(level, false);
    }

    /**
     * Creates a new compressor with the default compression level.
     * Compressed data will be generated in ZLIB format.
     */
    public Deflater() {
	this(DEFAULT_COMPRESSION, false);
    }

    /**
     * Sets input data for compression. This should be called whenever
     * needsInput() returns true indicating that more input data is required.
     * @param b the input data bytes
     * @param off the start offset of the data
     * @param len the length of the data
     * @see Deflater#needsInput
     */
    public synchronized void setInput(byte[] b, int off, int len) {
	if (b== null) {
	    throw new NullPointerException();
	}
        if (off < 0 || len < 0 || off > b.length - len) {
	    throw new ArrayIndexOutOfBoundsException();
	}
	this.buf = b;
	this.off = off;
	this.len = len;
    }

    /**
     * Sets input data for compression. This should be called whenever
     * needsInput() returns true indicating that more input data is required.
     * @param b the input data bytes
     * @see Deflater#needsInput
     */
    public void setInput(byte[] b) {
	setInput(b, 0, b.length);
    }

    /**
     * Sets preset dictionary for compression. A preset dictionary is used
     * when the history buffer can be predetermined. When the data is later
     * uncompressed with Inflater.inflate(), Inflater.getAdler() can be called
     * in order to get the Adler-32 value of the dictionary required for
     * decompression.
     * @param b the dictionary data bytes
     * @param off the start offset of the data
     * @param len the length of the data
     * @see Inflater#inflate
     * @see Inflater#getAdler
     */
    public synchronized void setDictionary(byte[] b, int off, int len) {
	if (strm == 0 || b == null) {
	    throw new NullPointerException();
	}
        if (off < 0 || len < 0 || off > b.length - len) {
	    throw new ArrayIndexOutOfBoundsException();
	}
	setDictionary(strm, b, off, len);
    }

    /**
     * Sets preset dictionary for compression. A preset dictionary is used
     * when the history buffer can be predetermined. When the data is later
     * uncompressed with Inflater.inflate(), Inflater.getAdler() can be called
     * in order to get the Adler-32 value of the dictionary required for
     * decompression.
     * @param b the dictionary data bytes
     * @see Inflater#inflate
     * @see Inflater#getAdler
     */
    public void setDictionary(byte[] b) {
	setDictionary(b, 0, b.length);
    }

    /**
     * Sets the compression strategy to the specified value.
     * @param strategy the new compression strategy
     * @exception IllegalArgumentException if the compression strategy is
     *				           invalid
     */
    public synchronized void setStrategy(int strategy) {
	switch (strategy) {
	  case DEFAULT_STRATEGY:
	  case FILTERED:
	  case HUFFMAN_ONLY:
	    break;
	  default:
	    throw new IllegalArgumentException();
	}
	if (this.strategy != strategy) {
	    this.strategy = strategy;
	    setParams = true;
	}
    }

    /**
     * Sets the current compression level to the specified value.
     * @param level the new compression level (0-9)
     * @exception IllegalArgumentException if the compression level is invalid
     */
    public synchronized void setLevel(int level) {
	if ((level < 0 || level > 9) && level != DEFAULT_COMPRESSION) {
	    throw new IllegalArgumentException("invalid compression level");
	}
	if (this.level != level) {
	    this.level = level;
	    setParams = true;
	}
    }

    /**
     * Returns true if the input data buffer is empty and setInput()
     * should be called in order to provide more input.
     * @return true if the input data buffer is empty and setInput()
     * should be called in order to provide more input
     */
    public boolean needsInput() {
	return len <= 0;
    }

    /**
     * When called, indicates that compression should end with the current
     * contents of the input buffer.
     */
    public synchronized void finish() {
	finish = true;
    }

    /**
     * Returns true if the end of the compressed data output stream has
     * been reached.
     * @return true if the end of the compressed data output stream has
     * been reached
     */
    public synchronized boolean finished() {
	return finished;
    }

    /**
     * Fills specified buffer with compressed data. Returns actual number
     * of bytes of compressed data. A return value of 0 indicates that
     * needsInput() should be called in order to determine if more input
     * data is required.
     * @param b the buffer for the compressed data
     * @param off the start offset of the data
     * @param len the maximum number of bytes of compressed data
     * @return the actual number of bytes of compressed data
     */
    public synchronized int deflate(byte[] b, int off, int len) {
	if (b == null) {
	    throw new NullPointerException();
	}
        if (off < 0 || len < 0 || off > b.length - len) {
	    throw new ArrayIndexOutOfBoundsException();
	}
	return deflateBytes(b, off, len);
    }

    /**
     * Fills specified buffer with compressed data. Returns actual number
     * of bytes of compressed data. A return value of 0 indicates that
     * needsInput() should be called in order to determine if more input
     * data is required.
     * @param b the buffer for the compressed data
     * @return the actual number of bytes of compressed data
     */
    public int deflate(byte[] b) {
	return deflate(b, 0, b.length);
    }

    /**
     * Returns the ADLER-32 value of the uncompressed data.
     * @return the ADLER-32 value of the uncompressed data
     */
    public synchronized int getAdler() {
	if (strm == 0) {
	    throw new NullPointerException();
	}
	return getAdler(strm);
    }

    /**
     * Returns the total number of bytes input so far.
     * @return the total number of bytes input so far
     */
    public synchronized int getTotalIn() {
	if (strm == 0) {
	    throw new NullPointerException();
	}
	return getTotalIn(strm);
    }

    /**
     * Returns the total number of bytes output so far.
     * @return the total number of bytes output so far
     */
    public synchronized int getTotalOut() {
	if (strm == 0) {
	    throw new NullPointerException();
	}
	return getTotalOut(strm);
    }

    /**
     * Resets deflater so that a new set of input data can be processed.
     * Keeps current compression level and strategy settings.
     */
    public synchronized void reset() {
	if (strm == 0) {
	    throw new NullPointerException();
	}
	reset(strm);
	finish = false;
	finished = false;
	off = len = 0;
    }

    /**
     * Closes the compressor and discards any unprocessed input.
     * This method should be called when the compressor is no longer
     * being used, but will also be called automatically by the
     * finalize() method. Once this method is called, the behavior
     * of the Deflater object is undefined.
     */
    public synchronized void end() {
	if (strm != 0) {
	    end(strm);
	    strm = 0;
	}
    }

    /**
     * Closes the compressor when garbage is collected.
     */
    protected void finalize() {
	end();
    }

    private static native void initIDs();
    private native static long init(int level, int strategy, boolean nowrap);
    private native static void setDictionary(long strm, byte[] b, int off,
					     int len);
    private native int deflateBytes(byte[] b, int off, int len);
    private native static int getAdler(long strm);
    private native static int getTotalIn(long strm);
    private native static int getTotalOut(long strm);
    private native static void reset(long strm);
    private native static void end(long strm);
}
