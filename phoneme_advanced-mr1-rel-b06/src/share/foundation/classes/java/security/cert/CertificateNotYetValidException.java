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

package java.security.cert;

/**
 * Certificate is not yet valid exception. This is thrown whenever
 * the current <code>Date</code> or the specified <code>Date</code>
 * is before the <code>notBefore</code> date/time in the Certificate
 * validity period.
 *
 * @author Hemma Prafullchandra
 * 1.6
 */
public class CertificateNotYetValidException extends CertificateException {

    /**
     * Constructs a CertificateNotYetValidException with no detail message. A
     * detail message is a String that describes this particular
     * exception.
     */
    public CertificateNotYetValidException() {
        super();
    }

    /**
     * Constructs a CertificateNotYetValidException with the specified detail
     * message. A detail message is a String that describes this
     * particular exception.
     *   
     * @param message the detail message.
     */
    public CertificateNotYetValidException(String message) {
        super(message);
    }
}
