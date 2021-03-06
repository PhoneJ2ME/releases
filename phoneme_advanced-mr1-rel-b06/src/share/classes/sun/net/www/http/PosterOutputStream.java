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

package sun.net.www.http;

import java.io.*;
import java.net.*;

/**
 * Instances of this class are returned to applications for the purpose of
 * sending user data for a HTTP POST or PUT request. This class is used
 * when the content-length will be specified in the header of the request.
 * The semantics of ByteArrayOutputStream are extended so that 
 * when close() is called, it is no longer possible to write 
 * additional data to the stream. From this point the content length of 
 * the request is fixed and cannot change.
 *
 * @version 1.1, 07/26/01
 * @author Michael McMahon
 */

public class PosterOutputStream extends ByteArrayOutputStream {

    private boolean closed;

    /**
     * Creates a new output stream for POST user data
     */
    public PosterOutputStream () {
	super (256);
    }

    /**
     * Writes the specified byte to this output stream.
     *
     * @param   b   the byte to be written.
     */
    public synchronized void write(int b) {
	if (closed) {
	    return;
	}
	super.write (b);
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array
     * starting at offset <code>off</code> to this output stream.
     *
     * @param   b     the data.
     * @param   off   the start offset in the data.
     * @param   len   the number of bytes to write.
     */
    public synchronized void write(byte b[], int off, int len) {
	if (closed) {
	    return;
	}
	super.write (b, off, len);
    }

    /**
     * Resets the <code>count</code> field of this output
     * stream to zero, so that all currently accumulated output in the
     * ouput stream is discarded. The output stream can be used again,
     * reusing the already allocated buffer space. If the output stream
     * has been closed, then this method has no effect.
     *
     * @see     java.io.ByteArrayInputStream#count
     */
    public synchronized void reset() {
	if (closed) {
	    return;
	}
	super.reset ();
    }

    /**
     * After close() has been called, it is no longer possible to write
     * to this stream. Further calls to write will have no effect.
     */
    public synchronized void close() throws IOException {
	closed = true;
	super.close ();
    }
}
