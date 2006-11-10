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

import java.security.PublicKey;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.InvalidKeyException;
import java.security.SignatureException;

/* J2ME CDC subsetted class */

/**
 * <p>Abstract class for managing a variety of identity certificates.
 * An identity certificate is a binding of a principal to a public key which
 * is vouched for by another principal.  (A principal represents
 * an entity such as an individual user, a group, or a corporation.)
 *<p>
 * This class is an abstraction for certificates that have different
 * formats but important common uses.  For example, different types of
 * certificates, such as X.509 and PGP, share general certificate
 * functionality (like encoding and verifying) and
 * some types of information (like a public key).
 * <p>
 * X.509, PGP, and SDSI certificates can all be implemented by
 * subclassing the Certificate class, even though they contain different
 * sets of information, and they store and retrieve the information in
 * different ways.
 *
 * @see X509Certificate
 *
 * @author Hemma Prafullchandra
 * @version 1.18 00/02/02
 */

public abstract class Certificate implements java.io.Serializable {

    // the certificate type
    private String type;

    /**
     * Creates a certificate of the specified type.
     *
     * @param type the standard name of the certificate type.
     * See Appendix A in the <a href=
     * "../../../../guide/security/CryptoSpec.html#AppA">
     * Java Cryptography Architecture API Specification &amp; Reference </a>
     * for information about standard certificate types.
     */
    protected Certificate(String type) {
	this.type = type;
    }

    /**
     * Returns the type of this certificate.
     *
     * @return the type of this certificate.
     */
    public final String getType() {
	return this.type;
    }

    /**
     * Compares this certificate for equality with the specified
     * object. If the <code>other</code> object is an
     * <code>instanceof</code> <code>Certificate</code>, then
     * its encoded form is retrieved and compared with the
     * encoded form of this certificate.
     *
     * @param other the object to test for equality with this certificate.
     * @return true iff the encoded forms of the two certificates
     * match, false otherwise.
     */
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof Certificate))
            return false;
        try {
            byte[] thisCert = this.getEncoded();
            byte[] otherCert = ((Certificate)other).getEncoded();

            if (thisCert.length != otherCert.length)
                return false;
            for (int i = 0; i < thisCert.length; i++)
                 if (thisCert[i] != otherCert[i])
                     return false;
            return true;
        } catch (CertificateException e) {
	    return false;
        }
    }

    /**
     * Returns a hashcode value for this certificate from its
     * encoded form.
     *
     * @return the hashcode value.
     */
    public int hashCode() {
        int     retval = 0;
        try {
            byte[] certData = this.getEncoded();
            for (int i = 1; i < certData.length; i++) {
                 retval += certData[i] * i;
            }
            return(retval);
        } catch (CertificateException e) {
            return(retval);
        }
    }

    /**
     * Returns the encoded form of this certificate. It is
     * assumed that each certificate type would have only a single
     * form of encoding; for example, X.509 certificates would
     * be encoded as ASN.1 DER.
     *
     * @return the encoded form of this certificate
     *
     * @exception CertificateEncodingException if an encoding error occurs.
     */
    public abstract byte[] getEncoded()
        throws CertificateEncodingException;

    /**
     * Verifies that this certificate was signed using the
     * private key that corresponds to the specified public key.
     *
     * @param key the PublicKey used to carry out the verification.
     *
     * @exception NoSuchAlgorithmException on unsupported signature
     * algorithms.
     * @exception InvalidKeyException on incorrect key.
     * @exception NoSuchProviderException if there's no default provider.
     * @exception SignatureException on signature errors.
     * @exception CertificateException on encoding errors.
     */
    public abstract void verify(PublicKey key)
        throws CertificateException, NoSuchAlgorithmException,
        InvalidKeyException, NoSuchProviderException,
        SignatureException;

    /**

     * Verifies that this certificate was signed using the
     * private key that corresponds to the specified public key.
     * This method uses the signature verification engine
     * supplied by the specified provider.
     *
     * @param key the PublicKey used to carry out the verification.
     * @param sigProvider the name of the signature provider.
     *
     * @exception NoSuchAlgorithmException on unsupported signature
     * algorithms.
     * @exception InvalidKeyException on incorrect key.
     * @exception NoSuchProviderException on incorrect provider.
     * @exception SignatureException on signature errors.
     * @exception CertificateException on encoding errors.
     */
    public abstract void verify(PublicKey key, String sigProvider)
        throws CertificateException, NoSuchAlgorithmException,
        InvalidKeyException, NoSuchProviderException,
        SignatureException;

    /**
     * Returns a string representation of this certificate.
     *
     * @return a string representation of this certificate.
     */
    public abstract String toString();

    /**
     * Gets the public key from this certificate.
     *
     * @return the public key.
     */
    public abstract PublicKey getPublicKey();

    /**
     * Alternate Certificate class for serialization.
     */
    protected static class CertificateRep implements java.io.Serializable {
	private String type;
	private byte[] data;

	/**
	 * Construct the alternate Certificate class with the Certificate
	 * type and Certificate encoding bytes.
	 *
	 * <p>
	 *
	 * @param type the standard name of the Certificate type. <p>
	 *
	 * @param data the Certificate data.
	 */
	protected CertificateRep(String type, byte[] data) {
	    this.type = type;
	    this.data = data;
	}

	/**
	 * Resolve the Certificate Object.
 	 *
 	 * <p>In the case of an implementation which does not provide
	 * CertificateFactory support, <code>null</code> will be
	 * returned.
 	 *
	 * @return the resolved Certificate Object
	 *
	 * @throws java.io.ObjectStreamException if the Certificate 
	 *	could not be resolved
	 */
	protected Object readResolve() throws java.io.ObjectStreamException {
	    // NOTE: Removed certificates from J2ME CDC
	    return null;
	}
    }

    /**
     * Replace the Certificate to be serialized.
     *
     * @return the alternate Certificate object to be serialized
     *
     * @throws java.io.ObjectStreamException if a new object representing 
     * this Certificate could not be created
     */
    protected Object writeReplace() throws java.io.ObjectStreamException {
	try {
	    return new CertificateRep(type, getEncoded());
	} catch (CertificateException e) {
	    throw new java.io.NotSerializableException
				("java.security.cert.Certificate: " +
				type +
				": " +
				e.getMessage());
	}
    }
}
