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

package sun.security.x509;

import java.lang.reflect.*;
import java.io.IOException;
import java.io.StringReader;
import java.security.PrivilegedExceptionAction;
import java.security.AccessController;
import java.security.Principal;
import java.util.*;

import sun.security.util.*;
import sun.security.pkcs.PKCS9Attribute;
import javax.security.auth.x500.X500Principal;

/**
 * RDNs are a set of {attribute = value} assertions.  Some of those
 * attributes are "distinguished" (unique w/in context).  Order is
 * never relevant.
 *
 * Some X.500 names include only a single distinguished attribute
 * per RDN.  This style is currently common.
 *
 * Note that DER-encoded RDNs sort AVAs by assertion OID ... so that
 * when we parse this data we don't have to worry about canonicalizing
 * it, but we'll need to sort them when we expose the RDN class more.
 * <p>
 * The ASN.1 for RDNs is:
 * <pre>
 * RelativeDistinguishedName ::=
 *   SET OF AttributeTypeAndValue
 *
 * AttributeTypeAndValue ::= SEQUENCE {
 *   type     AttributeType,
 *   value    AttributeValue }
 *
 * AttributeType ::= OBJECT IDENTIFIER
 *
 * AttributeValue ::= ANY DEFINED BY AttributeType
 * </pre>
 *
 * Note that instances of this class are immutable.
 *
 * @version 1.4, 03/12/05
 */
public class RDN {

    // currently not private, accessed directly from X500Name
    final AVA[] assertion;
    
    // cached immutable List of the AVAs
    private volatile List avaList;
    
    // cache canonical String form
    private volatile String canonicalString;

    /**
     * Constructs an RDN from its printable representation. 
     *
     * An RDN may consist of one or multiple Attribute Value Assertions (AVAs),
     * using '+' as a separator.
     * If the '+' should be considered part of an AVA value, it must be
     * preceded by '\'.
     *
     * @param name String form of RDN
     * @throws IOException on parsing error
     */
    public RDN(String name) throws IOException {
        int quoteCount = 0;
        int searchOffset = 0;
	int avaOffset = 0;
	Vector avaVec = new Vector(3);
	int nextPlus = name.indexOf('+');
	while (nextPlus >= 0) {
	    quoteCount += X500Name.countQuotes(name, searchOffset, nextPlus);
            /*
             * We have encountered an AVA delimiter (plus sign).
             * If the plus sign in the RDN under consideration is
             * preceded by a backslash (escape), or by a double quote, it
             * is part of the AVA. Otherwise, it is used as a separator, to 
	     * delimit the AVA under consideration from any subsequent AVAs.
             */
	    if (nextPlus > 0 && name.charAt(nextPlus - 1) != '\\'
	        && quoteCount != 1) {
                /*
                 * Plus sign is a separator
                 */
                String avaString = name.substring(avaOffset, nextPlus);
	        if (avaString.length() == 0) {
		    throw new IOException("empty AVA in RDN \"" + name + "\"");
		}

                // Parse AVA, and store it in vector
                AVA ava = new AVA(new StringReader(avaString));
                avaVec.addElement(ava);

                // Increase the offset
                avaOffset = nextPlus + 1;

                // Set quote counter back to zero
                quoteCount = 0;
	    }
            searchOffset = nextPlus + 1;
	    nextPlus = name.indexOf('+', searchOffset);
        }

        // parse last or only AVA
        String avaString = name.substring(avaOffset);
        if (avaString.length() == 0) {
	    throw new IOException("empty AVA in RDN \"" + name + "\"");
        }
        AVA ava = new AVA(new StringReader(avaString));
        avaVec.addElement(ava);

        assertion = (AVA[]) avaVec.toArray(new AVA[avaVec.size()]);
    }

    /*
     * Constructs an RDN from its printable representation. 
     *
     * An RDN may consist of one or multiple Attribute Value Assertions (AVAs),
     * using '+' as a separator.
     * If the '+' should be considered part of an AVA value, it must be
     * preceded by '\'.
     *
     * @param name String form of RDN
     * @throws IOException on parsing error
     */
    RDN(String name, String format) throws IOException {
        if (format.equalsIgnoreCase("RFC2253") == false) {
	    throw new IOException("Unsupported format " + format);
	}
        int searchOffset = 0;
	int avaOffset = 0;
	Vector avaVec = new Vector(3);
	int nextPlus = name.indexOf('+');
	while (nextPlus >= 0) {
            /*
             * We have encountered an AVA delimiter (plus sign).
             * If the plus sign in the RDN under consideration is
             * preceded by a backslash (escape), or by a double quote, it
             * is part of the AVA. Otherwise, it is used as a separator, to 
	     * delimit the AVA under consideration from any subsequent AVAs.
             */
	    if (nextPlus > 0 && name.charAt(nextPlus - 1) != '\\' ) {
                /*
                 * Plus sign is a separator
                 */
                String avaString = name.substring(avaOffset, nextPlus);
	        if (avaString.length() == 0) {
		    throw new IOException("empty AVA in RDN \"" + name + "\"");
		}

                // Parse AVA, and store it in vector
                AVA ava = new AVA(new StringReader(avaString), AVA.RFC2253);
                avaVec.addElement(ava);

                // Increase the offset
                avaOffset = nextPlus + 1;
	    }
            searchOffset = nextPlus + 1;
	    nextPlus = name.indexOf('+', searchOffset);
        }

        // parse last or only AVA
        String avaString = name.substring(avaOffset);
        if (avaString.length() == 0) {
	    throw new IOException("empty AVA in RDN \"" + name + "\"");
        }
        AVA ava = new AVA(new StringReader(avaString), AVA.RFC2253);
        avaVec.addElement(ava);

        assertion = (AVA[]) avaVec.toArray(new AVA[avaVec.size()]);
    }

    /*
     * Constructs an RDN from an ASN.1 encoded value.  The encoding
     * of the name in the stream uses DER (a BER/1 subset).
     *
     * @param value a DER-encoded value holding an RDN.
     * @throws IOException on parsing error.
     */
    RDN(DerValue rdn) throws IOException {
	if (rdn.tag != DerValue.tag_Set) {
	    throw new IOException("X500 RDN");
	}
	DerInputStream dis = new DerInputStream(rdn.toByteArray());
	DerValue[] avaset = dis.getSet(5);

	assertion = new AVA[avaset.length];
	for (int i = 0; i < avaset.length; i++) {
	    assertion[i] = new AVA(avaset[i]);
	}
    }

    /*
     * Creates an empty RDN with slots for specified
     * number of AVAs.
     *
     * @param i number of AVAs to be in RDN
     */
    RDN(int i) { assertion = new AVA[i]; }
    
    public RDN(AVA ava) {
	if (ava == null) {
	    throw new NullPointerException();
	}
	assertion = new AVA[] { ava };
    }
    
    public RDN(AVA[] avas) {
	assertion = (AVA[])avas.clone();
	for (int i = 0; i < assertion.length; i++) {
	    if (assertion[i] == null) {
		throw new NullPointerException();
	    }
	}
    }
    
    /**
     * Return an immutable List of the AVAs in this RDN.
     */
    public List avas() {
	List list = avaList;
	if (list == null) {
	    list = Collections.unmodifiableList(Arrays.asList(assertion));
	    avaList = list;
	}
	return list;
    }
    
    /**
     * Return the number of AVAs in this RDN.
     */
    public int size() {
	return assertion.length;
    }

    public boolean equals(Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj instanceof RDN == false) {
	    return false;
	}
	RDN other = (RDN)obj;
	if (this.assertion.length != other.assertion.length) {
	    return false;
	}
	String thisCanon = this.toRFC2253String(true);
	String otherCanon = other.toRFC2253String(true);
	return thisCanon.equals(otherCanon);
    }
    
    /*
     * Calculates a hash code value for the object.  Objects
     * which are equal will also have the same hashcode.
     *
     * @returns int hashCode value
     */
    public int hashCode() {
	return toRFC2253String(true).hashCode();
    }

    /*
     * return specified attribute value from RDN
     *
     * @params oid ObjectIdentifier of attribute to be found
     * @returns DerValue of attribute value; null if attribute does not exist
     */
    DerValue findAttribute(ObjectIdentifier oid) {
	for (int i = 0; i < assertion.length; i++) {
	    if (assertion[i].oid.equals(oid)) {
		return assertion[i].value;
	    }
	}
	return null;
    }

    /*
     * Encode the RDN in DER-encoded form.
     *
     * @param out DerOutputStream to which RDN is to be written
     * @throws IOException on error
     */
    void encode(DerOutputStream out) throws IOException {
	out.putOrderedSetOf(DerValue.tag_Set, assertion);
    }

    /*
     * Returns a printable form of this RDN, using RFC 1779 style catenation
     * of attribute/value assertions, and emitting attribute type keywords
     * from RFC 1779, 2253, and 2459.
     */
    public String toString() {
	if (assertion.length == 1) {
	    return assertion[0].toString();
	}
	
	StringBuffer sb = new StringBuffer();
	for (int i = 0; i < assertion.length; i++) {
	    if (i != 0) {
		sb.append(" + ");
	    }
	    sb.append(assertion[i].toString());
	}
	return sb.toString();
    }

    /*
     * Returns a printable form of this RDN using the algorithm defined in
     * RFC 1779. Only RFC 1779 attribute type keywords are emitted.
     */
    public String toRFC1779String() {
	if (assertion.length == 1) {
	    return assertion[0].toRFC1779String();
	}

	StringBuffer sb = new StringBuffer();
	for (int i = 0; i < assertion.length; i++) {
	    if (i != 0) {
		sb.append(" + ");
	    }
	    sb.append(assertion[i].toRFC1779String());
	}
	return sb.toString();
    }

    /*
     * Returns a printable form of this RDN using the algorithm defined in
     * RFC 2253. Only RFC 2253 attribute type keywords are emitted.
     */
    public String toRFC2253String() {
	return toRFC2253StringInternal(false);
    }

    /*
     * Returns a printable form of this RDN using the algorithm defined in
     * RFC 2253. Only RFC 2253 attribute type keywords are emitted.
     * If canonical is true, then additional canonicalizations
     * documented in X500Principal.getName are performed.
     */
    public String toRFC2253String(boolean canonical) {
	if (canonical == false) {
	    return toRFC2253StringInternal(false);
	}
	String c = canonicalString;
	if (c == null) {
	    c = toRFC2253StringInternal(true);
	    canonicalString = c;
	}
	return c;
    }
    
    private String toRFC2253StringInternal(boolean canonical) {
	/*
	 * Section 2.2: When converting from an ASN.1 RelativeDistinguishedName 
	 * to a string, the output consists of the string encodings of each 
	 * AttributeTypeAndValue (according to 2.3), in any order.
	 *
   	 * Where there is a multi-valued RDN, the outputs from adjoining
   	 * AttributeTypeAndValues are separated by a plus ('+' ASCII 43)
   	 * character.
	 */
	 
	// normally, an RDN only contains one AVA
	if (assertion.length == 1) {
	    return canonical ? assertion[0].toRFC2253CanonicalString() : 
	    		       assertion[0].toRFC2253String();
	}

	StringBuffer relname = new StringBuffer();
	if (!canonical) {
	    for (int i = 0; i < assertion.length; i++) {
		if (i > 0) {
		    relname.append('+');
		}
		relname.append(assertion[i].toRFC2253String());
	    }
	} else {
	    // order the string type AVA's alphabetically,
	    // followed by the oid type AVA's numerically
	    List avaList = new ArrayList(assertion.length);
	    for (int i = 0; i < assertion.length; i++) {
		avaList.add(assertion[i]);
	    }
	    java.util.Collections.sort(avaList, AVAComparator.getInstance());

	    for (int i = 0; i < avaList.size(); i++) {
		if (i > 0) {
		    relname.append('+');
		}
		relname.append
		    (((AVA)avaList.get(i)).toRFC2253CanonicalString());
	    }
	}
	return new String(relname);
    }

}

class AVAComparator implements Comparator {
    
    private static final Comparator INSTANCE = new AVAComparator();
    
    private AVAComparator() {
	// empty
    }
    
    static Comparator getInstance() {
	return INSTANCE;
    }
    
    /**
     * AVA's containing a standard keyword are ordered alphabetically,
     * followed by AVA's containing an OID keyword, ordered numerically
     */
    public int compare(Object o1, Object o2) {
	AVA a1 = (AVA)o1;
	AVA a2 = (AVA)o2;
	
	boolean a1Has2253 = a1.hasRFC2253Keyword();
	boolean a2Has2253 = a2.hasRFC2253Keyword();

	if (a1Has2253 == a2Has2253) {
	    return a1.toRFC2253CanonicalString().compareTo
			(a2.toRFC2253CanonicalString());
	} else {
	    if (a1Has2253) {
		return -1;
	    } else {
		return 1;
	    }
	}
    }

}
