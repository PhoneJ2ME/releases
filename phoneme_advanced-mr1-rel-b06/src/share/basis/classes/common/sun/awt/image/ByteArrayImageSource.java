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

package sun.awt.image;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.BufferedInputStream;

public class ByteArrayImageSource extends InputStreamImageSource {
    byte[] imagedata;
    int imageoffset;
    int imagelength;
    public ByteArrayImageSource(byte[] data) {
        this(data, 0, data.length);
    }

    public ByteArrayImageSource(byte[] data, int offset, int length) {
        imagedata = data;
        imageoffset = offset;
        imagelength = length;
    }

    final boolean checkSecurity(Object context, boolean quiet) {
        // No need to check security.  Applets and downloaded code can
        // only make byte array image once they already have a handle
        // on the image data anyway...
        return true;
    }

    protected ImageDecoder getDecoder() {
        InputStream is =
            new BufferedInputStream(new ByteArrayInputStream(imagedata,
                    imageoffset,
                    imagelength));
        return getDecoder(is);
    }
}
