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

package sun.net.www.protocol.http;

import java.net.URL;

/**
 * An interface for all objects that implement HTTP authentication.
 * See the HTTP spec for details on how this works in general.
 * A single class or object can implement an arbitrary number of
 * authentication schemes.  
 *
 * @author David Brown
 *
 * @deprecated -- use java.net.Authenticator instead
 * @see java.net.Authenticator
 */
//
// NOTE:  Unless compatibility with sun.* API's from 1.2 to 2.0 is
// a goal, there's no reason to carry this forward into JDK 2.0.

public interface HttpAuthenticator {
    

    /**
     * Indicate whether the specified authentication scheme is
     * supported.  In accordance with HTTP specifications, the
     * scheme name should be checked in a case-insensitive fashion.
     */

    boolean schemeSupported (String scheme);

    /**
     * Returns the String that should be included in the HTTP
     * <B>Authorization</B> field.  Return null if no info was
     * supplied or could be found.
     * <P>
     * Example:
     * --> GET http://www.authorization-required.com/ HTTP/1.0
     * <-- HTTP/1.0 403 Unauthorized
     * <-- WWW-Authenticate: Basic realm="WallyWorld"
     * call schemeSupported("Basic"); (return true)
     * call authString(u, "Basic", "WallyWorld", null);
     *   return "QWadhgWERghghWERfdfQ=="
     * --> GET http://www.authorization-required.com/ HTTP/1.0
     * --> Authorization: Basic QWadhgWERghghWERfdfQ==
     * <-- HTTP/1.0 200 OK
     * <B> YAY!!!</B>
     */

    public String authString (URL u, String scheme, String realm);

}




