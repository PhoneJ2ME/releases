/*
 * @(#)SecureRandom.java	1.43 06/10/10
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
 *
 */
 
package java.security;

import java.util.Enumeration;

/**
 * <p>This class provides a cryptographically strong pseudo-random number
 * generator (PRNG). A cryptographically strong pseudo-random number
 * minimally complies with the statistical random number generator tests
 * specified in <a href="http://csrc.nist.gov/cryptval/140-2.htm"><i>FIPS 140-2, Security Requirements for Cryptographic Modules</i></a>, section 4.9.1.
 * Additionally, SecureRandom must produce non-deterministic 
 * output and therefore it is required that the seed material be unpredictable
 * and that output of SecureRandom be cryptographically strong sequences as
 * described in <a href="http://www.ietf.org/rfc/rfc1750.txt"><i>RFC 1750: Randomness Recommendations for Security</i></a>.
 * 
 * <p>Like other algorithm-based classes in Java Security, SecureRandom 
 * provides implementation-independent algorithms, whereby a caller 
 * (application code) requests a particular PRNG algorithm
 * and is handed back a SecureRandom object for that algorithm. It is
 * also possible, if desired, to request a particular algorithm from a
 * particular provider. See the <code>getInstance</code> methods.
 *
 * <p>Thus, there are two ways to request a SecureRandom object: by
 * specifying either just an algorithm name, or both an algorithm name
 * and a package provider.
 *
 * <ul>
 *
 * <li>If just an algorithm name is specified, as in:
 * <pre>
 *      SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
 * </pre>
 * the system will determine if there is an implementation of the algorithm
 * requested available in the environment, and if there is more than one, if
 * there is a preferred one.<p>
 * 
 * <li>If both an algorithm name and a package provider are specified, as in:
 * <pre>
 *      SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
 * </pre>
 * the system will determine if there is an implementation of the
 * algorithm in the package requested, and throw an exception if there
 * is not.
 *
 * </ul>
 *
 * <p>The SecureRandom implementation attempts to completely
 * randomize the internal state of the generator itself unless
 * the caller follows the call to a <code>getInstance</code> method
 * with a call to the <code>setSeed</code> method:
 * <pre>
 *      SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
 *      random.setSeed(seed);
 * </pre>
 *
 * <p>After the caller obtains the SecureRandom object from the
 * <code>getInstance</code> call, it can call <code>nextBytes</code>
 * to generate random bytes:
 * <pre>
 *      byte bytes[] = new byte[20];
 *      random.nextBytes(bytes);
 * </pre>
 *
 * <p>The caller may also invoke the <code>generateSeed</code> method
 * to generate a given number of seed bytes (to seed other random number
 * generators, for example):
 * <pre>
 *      byte seed[] = random.generateSeed(20);
 * </pre>
 *
 * @see java.security.SecureRandomSpi
 * @see java.util.Random
 * 
 * @version 1.36, 02/02/00
 * @author Benjamin Renaud
 * @author Josh Bloch 
 */

public class SecureRandom extends java.util.Random {

    /**
     * The provider.
     *
     * @serial
     * @since 1.2
     */
    private Provider provider = null;
 
    /**
     * The provider implementation.
     *
     * @serial
     * @since 1.2
     */
    private SecureRandomSpi secureRandomSpi = null;

    // Seed Generator
    private static SecureRandom seedGenerator = null;

    /**
     * <p>By using this constructor, the caller obtains a SecureRandom object
     * containing the implementation from the highest-priority installed
     * provider that has a SecureRandom implementation.
     * 
     * <p>Note that this instance of SecureRandom has not been seeded.
     * A call to the <code>setSeed</code> method will seed the SecureRandom
     * object.  If a call is not made to <code>setSeed</code>, the first call
     * to the <code>nextBytes</code> method will force the SecureRandom object
     * to seed itself.
     *
     * <p>This constructor is provided for backwards compatibility. 
     * The caller is encouraged to use one of the alternative
     * <code>getInstance</code> methods to obtain a SecureRandom object.
     */
    public SecureRandom() {
	/*
	 * This call to our superclass constructor will result in a call
	 * to our own <code>setSeed</code> method, which will return
	 * immediately when it is passed zero.
	 */
	super(0);
	String prng = getPrngAlgorithm();
	if (prng == null) {
	    // bummer, get the SUN implementation
	    this.secureRandomSpi = new sun.security.provider.SecureRandom();
	    this.provider = new sun.security.provider.Sun();
	} else {
	    try {
		SecureRandom random = SecureRandom.getInstance(prng);
		this.secureRandomSpi = random.getSecureRandomSpi();
		this.provider = random.getProvider();
	    } catch (NoSuchAlgorithmException nsae) {
		// never happens, because we made sure the algorithm exists
	    }
	}
    }

    /**
     * <p>By using this constructor, the caller obtains a SecureRandom object
     * containing the implementation from the highest-priority installed
     * provider that has a SecureRandom implementation. This constructor 
     * uses a user-provided seed in
     * preference to the self-seeding algorithm referred to in the empty
     * constructor description. It may be preferable to the empty constructor
     * if the caller has access to high-quality random bytes from some physical
     * device (for example, a radiation detector or a noisy diode).
     * 
     * <p>This constructor is provided for backwards compatibility. 
     * The caller is encouraged to use one of the alternative
     * <code>getInstance</code> methods to obtain a SecureRandom object, and
     * then to call the <code>setSeed</code> method to seed it.
     * 
     * @param seed the seed.
     */
    public SecureRandom(byte seed[]) {
	super(0);
	String prng = getPrngAlgorithm();
	if (prng == null) {
	    // bummer, get the SUN implementation
	    this.secureRandomSpi = new sun.security.provider.SecureRandom();
	    this.provider = new sun.security.provider.Sun();
	    this.secureRandomSpi.engineSetSeed(seed);
	} else {
	    try {
		SecureRandom random = getInstance(prng);
		this.secureRandomSpi = random.getSecureRandomSpi();
		this.provider = random.getProvider();
		this.secureRandomSpi.engineSetSeed(seed);
	    } catch (NoSuchAlgorithmException nsae) {
		// never happens, because we made sure the algorithm exists
	    }
	}
    }

    /**
     * Creates a SecureRandom object.
     *
     * @param secureRandomSpi the SecureRandom implementation.
     * @param provider the provider.
     */
    protected SecureRandom(SecureRandomSpi secureRandomSpi,
			   Provider provider) {
	super(0);
	this.secureRandomSpi = secureRandomSpi;
	this.provider = provider;
    }

    /**
     * Generates a SecureRandom object that implements the specified
     * Pseudo Random Number Generator (PRNG) algorithm. If the default
     * provider package provides an implementation of the requested PRNG,
     * an instance of SecureRandom containing that implementation is returned.
     * If the PRNG is not available in the default 
     * package, other packages are searched.
     *
     * <p>Note that the returned instance of SecureRandom has not been seeded.
     * A call to the <code>setSeed</code> method will seed the SecureRandom
     * object.  If a call is not made to <code>setSeed</code>, the first call
     * to the <code>nextBytes</code> method will force the SecureRandom object
     * to seed itself.
     *
     * @param algorithm the name of the PRNG algorithm.
     * See Appendix A in the <a href=
     * "../../../guide/security/CryptoSpec.html#AppA">
     * Java Cryptography Architecture API Specification &amp; Reference </a> 
     * for information about standard PRNG algorithm names.
     *
     * @return the new SecureRandom object.
     *
     * @exception NoSuchAlgorithmException if the PRNG algorithm is
     * not available in the caller's environment.
     *
     * @since 1.2
     */
    public static SecureRandom getInstance(String algorithm)
	throws NoSuchAlgorithmException {
	    try {
		Object[] objs = Security.getImpl(algorithm,
						 "SecureRandom",
						 (String)null);
		return new SecureRandom((SecureRandomSpi)objs[0],
					(Provider)objs[1]);
	    } catch (NoSuchProviderException e) {
		throw new NoSuchAlgorithmException(algorithm + " not found");
	    }
    }

    /**
     * Generates a SecureRandom object for the specified PRNG
     * algorithm, as supplied from the specified provider, if such a
     * PRNG implementation is available from the provider.
     *
     * <p>Note that the returned instance of SecureRandom has not been seeded.
     * A call to the <code>setSeed</code> method will seed the SecureRandom
     * object.  If a call is not made to <code>setSeed</code>, the first call
     * to the <code>nextBytes</code> method will force the SecureRandom object
     * to seed itself.
     *
     * @param algorithm the name of the PRNG algorithm.
     * See Appendix A in the <a href=
     * "../../../guide/security/CryptoSpec.html#AppA">
     * Java Cryptography Architecture API Specification &amp; Reference </a> 
     * for information about standard PRNG algorithm names.
     *
     * @param provider the name of the provider.
     *
     * @return the new SecureRandom object.
     *
     * @exception NoSuchAlgorithmException if the requested PRNG
     * implementation is not available from the provider.
     *
     * @exception NoSuchProviderException if the provider has not been
     * configured.
     *
     * @exception IllegalArgumentException if the provider name is null
     * or empty.
     *
     * @see Provider
     *
     * @since 1.2
     */
    public static SecureRandom getInstance(String algorithm, String provider)
	throws NoSuchAlgorithmException, NoSuchProviderException
    {
	if (provider == null || provider.length() == 0)
	    throw new IllegalArgumentException("missing provider");
	Object[] objs = Security.getImpl(algorithm, "SecureRandom", provider);
	return new SecureRandom((SecureRandomSpi)objs[0], (Provider)objs[1]);
    }

    /**
     * Generates a SecureRandom object for the specified PRNG
     * algorithm, as supplied from the specified provider, if such a
     * PRNG implementation is available from the provider.
     * Note: the <code>provider</code> doesn't have to be registered. 
     *
     * <p>Note that the returned instance of SecureRandom has not been seeded.
     * A call to the <code>setSeed</code> method will seed the SecureRandom
     * object.  If a call is not made to <code>setSeed</code>, the first call
     * to the <code>nextBytes</code> method will force the SecureRandom object
     * to seed itself.
     *
     * @param algorithm the name of the PRNG algorithm.
     * See Appendix A in the <a href=
     * "../../../guide/security/CryptoSpec.html#AppA">
     * Java Cryptography Architecture API Specification &amp; Reference </a> 
     * for information about standard PRNG algorithm names.
     *
     * @param provider the provider.
     *
     * @return the new SecureRandom object.
     *
     * @exception NoSuchAlgorithmException if the requested PRNG
     * implementation is not available from the provider.
     *
     * @exception IllegalArgumentException if the <code>provider</code> is
     * null.
     *
     * @see Provider
     *
     * @since 1.4
     */
    public static SecureRandom getInstance(String algorithm,
					   Provider provider)
	throws NoSuchAlgorithmException
    {
	if (provider == null)
	    throw new IllegalArgumentException("missing provider");
	Object[] objs = Security.getImpl(algorithm, "SecureRandom", provider);
	return new SecureRandom((SecureRandomSpi)objs[0], (Provider)objs[1]);
    }
 
    /**
     * Returns the SecureRandomSpi of this SecureRandom object.
     */
    SecureRandomSpi getSecureRandomSpi() {
	return secureRandomSpi;
    }

    /**
     * Returns the provider of this SecureRandom object.
     *
     * @return the provider of this SecureRandom object.
     */
    public final Provider getProvider() {
	return provider;
    }

    /**
     * Reseeds this random object. The given seed supplements, rather than
     * replaces, the existing seed. Thus, repeated calls are guaranteed
     * never to reduce randomness.
     *
     * @param seed the seed.
     *
     * @see #getSeed
     */
    synchronized public void setSeed(byte[] seed) {
	secureRandomSpi.engineSetSeed(seed);
    }

    /**
     * Reseeds this random object, using the eight bytes contained 
     * in the given <code>long seed</code>. The given seed supplements, 
     * rather than replaces, the existing seed. Thus, repeated calls 
     * are guaranteed never to reduce randomness. 
     * 
     * <p>This method is defined for compatibility with 
     * <code>java.util.Random</code>.
     *
     * @param seed the seed.
     *
     * @see #getSeed
     */
    public void setSeed(long seed) {
	/* 
	 * Ignore call from super constructor (as well as any other calls
	 * unfortunate enough to be passing 0).  It's critical that we
	 * ignore call from superclass constructor, as digest has not
	 * yet been initialized at that point.
	 */
	if (seed != 0)
	    secureRandomSpi.engineSetSeed(longToByteArray(seed));
    }

    /**
     * Generates a user-specified number of random bytes.  This method is
     * used as the basis of all random entities returned by this class
     * (except seed bytes).
     * 
     * @param bytes the array to be filled in with random bytes.
     */

    synchronized public void nextBytes(byte[] bytes) {
	secureRandomSpi.engineNextBytes(bytes);
    }

    /**
     * Generates an integer containing the user-specified number of
     * pseudo-random bits (right justified, with leading zeros).  This
     * method overrides a <code>java.util.Random</code> method, and serves
     * to provide a source of random bits to all of the methods inherited
     * from that class (for example, <code>nextInt</code>,
     * <code>nextLong</code>, and <code>nextFloat</code>).
     *
     * @param numBits number of pseudo-random bits to be generated, where
     * 0 <= <code>numBits</code> <= 32.
     *
     * @return an <code>int</code> containing the user-specified number
     * of pseudo-random bits (right justified, with leading zeros).
     */
    final protected int next(int numBits) {
	int numBytes = (numBits+7)/8;
	byte b[] = new byte[numBytes];
	int next = 0;
 
	nextBytes(b);
	for (int i = 0; i < numBytes; i++)
	    next = (next << 8) + (b[i] & 0xFF);
 
	return next >>> (numBytes*8 - numBits);
    }

    /**
     * Returns the given number of seed bytes, computed using the seed
     * generation algorithm that this class uses to seed itself.  This
     * call may be used to seed other random number generators.
     *
     * <p>This method is only included for backwards compatibility. 
     * The caller is encouraged to use one of the alternative
     * <code>getInstance</code> methods to obtain a SecureRandom object, and
     * then call the <code>generateSeed</code> method to obtain seed bytes
     * from that object.
     *
     * @param numBytes the number of seed bytes to generate.
     * 
     * @return the seed bytes.
     *
     * @see #setSeed
     */
    public static byte[] getSeed(int numBytes) {
	if (seedGenerator == null)
	    seedGenerator = new SecureRandom();
	return seedGenerator.generateSeed(numBytes);
    }

    /**
     * Returns the given number of seed bytes, computed using the seed
     * generation algorithm that this class uses to seed itself.  This
     * call may be used to seed other random number generators.
     *
     * @param numBytes the number of seed bytes to generate.
     * 
     * @return the seed bytes.
     */
    public byte[] generateSeed(int numBytes) {
	return secureRandomSpi.engineGenerateSeed(numBytes);
    }

    /**
     * Helper function to convert a long into a byte array (least significant
     * byte first).
     */
    private static byte[] longToByteArray(long l) {
	byte[] retVal = new byte[8];

	for (int i = 0; i < 8; i++) {
	    retVal[i] = (byte) l;
	    l >>= 8;
	}

	return retVal;
    }

    /**
     * Gets a default PRNG algorithm by looking through all registered
     * providers. Returns the first PRNG algorithm of the first provider that
     * has registered a SecureRandom implementation, or null if none of the
     * registered providers supplies a SecureRandom implementation.
     */
    private static String getPrngAlgorithm() {
	Provider[] provs = Security.getProviders();
	for (int i = 0; i < provs.length; i++) {
	    // search the provider's properties list for a property name
	    // that starts with "SecureRandom."
	    for (Enumeration e = provs[i].propertyNames();
		 e.hasMoreElements();) {
		String propName = (String)e.nextElement();
		// if property name is followed by an atttribute, skip it
		if ((propName.startsWith("SecureRandom.")) &&
		    (propName.indexOf(' ') < 0)) {
		    int index = propName.indexOf(".", 0);
                    return propName.substring(index+1);
                }
	    }
	}
	return null;
    }

    // Declare serialVersionUID to be compatible with JDK1.1
    static final long serialVersionUID = 4940670005562187L;

    // Retain unused values serialized from JDK1.1
    /**
     * @serial
     */
    private byte[] state;
    /**
     * @serial
     */
    private MessageDigest digest = null;
    /**
     * @serial
     *
     * We know that the MessageDigest class does not implement
     * java.io.Serializable.  However, since this field is no longer
     * used, it will always be NULL and won't affect the serialization
     * of the SecureRandom class itself.
     */
    private byte[] randomBytes;
    /**
     * @serial
     */
    private int randomBytesUsed;
    /**
     * @serial
     */
    private long counter;
}
