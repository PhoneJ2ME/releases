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
package javax.microedition.media.protocol;

import java.io.IOException;
import javax.microedition.media.Controllable;

/**
 * This class is defined by the JSR-135 specification
 * <em>Mobile Media API,
 * Version 1.2.</em>
 */
// JAVADOC COMMENT ELIDED

public interface SourceStream extends Controllable {

    // JAVADOC COMMENT ELIDED
    int NOT_SEEKABLE = 0;

    // JAVADOC COMMENT ELIDED
    int SEEKABLE_TO_START = 1;

    // JAVADOC COMMENT ELIDED
    int RANDOM_ACCESSIBLE = 2;
    
    // JAVADOC COMMENT ELIDED
    ContentDescriptor getContentDescriptor();


    // JAVADOC COMMENT ELIDED
    long getContentLength();
    

    // JAVADOC COMMENT ELIDED
    int read(byte[] b, int off, int len)
	throws IOException;


    // JAVADOC COMMENT ELIDED
    int getTransferSize();


    // JAVADOC COMMENT ELIDED
    long seek(long where) throws IOException;


    // JAVADOC COMMENT ELIDED
    long tell();

   
    // JAVADOC COMMENT ELIDED
    int getSeekType();
}
