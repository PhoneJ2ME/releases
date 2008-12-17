/*
 * @(#)NativeSeedGenerator.java	1.6 06/10/10
 *
 * Copyright  1990-2008 Sun Microsystems, Inc. All Rights Reserved.
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

package sun.security.provider;

import java.io.IOException;

/**
 * Seed generator for Windows making use of MS CryptoAPI using native code.
 *
 * @version 1.6, 10/10/06
 */
class NativeSeedGenerator extends SeedGenerator {

    /**
     * Create a new CryptoAPI seed generator instances.
     *
     * @exception IOException if CryptoAPI seeds are not available
     * on this platform.
     */
    NativeSeedGenerator() throws IOException {
	super();
	// try generating two random bytes to see if CAPI is available
        if (!nativeGenerateSeed(new byte[2])) {
	    throw new IOException("Required native CryptoAPI features not "
				  + " available on this machine");
	}
    }

    /**
     * Native method to do the actual work.
     */
    private static native boolean nativeGenerateSeed(byte[] result);

    void getSeedBytes(byte[] result) {
	// fill array as a side effect
	if (nativeGenerateSeed(result) == false) {
	    // should never happen if constructor check succeeds
	    throw new InternalError
			    ("Unexpected CryptoAPI failure generating seed");
	}
    }

    byte getSeedByte() {
	byte[] b = new byte[1];
	getSeedBytes(b);
	return b[0];
    }
}
