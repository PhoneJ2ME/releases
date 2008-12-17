/*
 * @(#)UnresolvedPermission.java	1.12 06/10/10
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

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.lang.reflect.*;
import java.security.cert.*;

/**
 * The UnresolvedPermission class is used to hold Permissions that
 * were "unresolved" when the Policy was initialized. 
 * An unresolved permission is one whose actual Permission class
 * does not yet exist at the time the Policy is initialized (see below).
 * 
 * <p>The policy for a Java runtime (specifying 
 * which permissions are available for code from various principals)
 * is represented by a Policy object.
 * Whenever a Policy is initialized or refreshed, Permission objects of
 * appropriate classes are created for all permissions
 * allowed by the Policy. 
 * 
 * <p>Many permission class types 
 * referenced by the policy configuration are ones that exist
 * locally (i.e., ones that can be found on CLASSPATH).
 * Objects for such permissions can be instantiated during
 * Policy initialization. For example, it is always possible
 * to instantiate a java.io.FilePermission, since the
 * FilePermission class is found on the CLASSPATH.
 * 
 * <p>Other permission classes may not yet exist during Policy
 * initialization. For example, a referenced permission class may
 * be in a JAR file that will later be loaded.
 * For each such class, an UnresolvedPermission is instantiated.
 * Thus, an UnresolvedPermission is essentially a "placeholder"
 * containing information about the permission.
 * 
 * <p>Later, when code calls AccessController.checkPermission 
 * on a permission of a type that was previously unresolved,
 * but whose class has since been loaded, previously-unresolved
 * permissions of that type are "resolved". That is,
 * for each such UnresolvedPermission, a new object of
 * the appropriate class type is instantiated, based on the
 * information in the UnresolvedPermission.
 *
 * <p> To instantiate the new class, UnresolvedPermission assumes
 * the class provides a zero, one, and/or two-argument constructor.
 * The zero-argument constructor would be used to instantiate
 * a permission without a name and without actions.
 * A one-arg constructor is assumed to take a <code>String</code>
 * name as input, and a two-arg constructor is assumed to take a
 * <code>String</code> name and <code>String</code> actions
 * as input.  UnresolvedPermission may invoke a
 * constructor with a <code>null</code> name and/or actions.
 * If an appropriate permission constructor is not available,
 * the UnresolvedPermission is ignored and the relevant permission
 * will not be granted to executing code.
 *
 * <p> The newly created permission object replaces the
 * UnresolvedPermission, which is removed.
 *
 * @see java.security.Permission
 * @see java.security.Permissions
 * @see java.security.PermissionCollection
 * @see java.security.Policy
 *
 * @version 1.24 03/01/23
 *
 * @author Roland Schemers
 */

public final class UnresolvedPermission extends Permission 
implements java.io.Serializable
{
    /**
     * The class name of the Permission class that will be
     * created when this unresolved permission is resolved.
     *
     * @serial
     */
    private String type;

    /**
     * The permission name.
     *
     * @serial
     */
    private String name;

    /**
     * The actions of the permission.
     *
     * @serial
     */
    private String actions;

    private transient java.security.cert.Certificate certs[];

    /**
     * Creates a new UnresolvedPermission containing the permission
     * information needed later to actually create a Permission of the
     * specified class, when the permission is resolved.
     * 
     * @param type the class name of the Permission class that will be
     * created when this unresolved permission is resolved.
     * @param name the name of the permission.
     * @param actions the actions of the permission.
     * @param certs the certificates the permission's class was signed with.
     * This is a list of certificate chains, where each chain is composed of a
     * signer certificate and optionally its supporting certificate chain.
     * Each chain is ordered bottom-to-top (i.e., with the signer certificate
     * first and the (root) certificate authority last).
     */
    public UnresolvedPermission(String type,
				String name,
				String actions,
				java.security.cert.Certificate certs[])
    {
	super(type);

	if (type == null) 
		throw new NullPointerException("type can't be null");

	this.type = type;
	this.name = name;
	this.actions = actions;

	// J2ME CDC reduction: security implementation removed
    }


    private static final Class[] PARAMS = { String.class, String.class};

    /**
     * try and resolve this permission using the class loader of the permission
     * that was passed in.
     */
    Permission resolve(Permission p, java.security.cert.Certificate certs[]) {
	if (this.certs != null) {
	    // if p wasn't signed, we don't have a match
	    if (certs == null) {
		return null;
	    }

	    // all certs in this.certs must be present in certs
	    boolean match;
	    for (int i = 0; i < this.certs.length; i++) {
		match = false;
		for (int j = 0; j < certs.length; j++) {
		    if (this.certs[i].equals(certs[j])) {
			match = true;
			break;
		    }
		}
		if (!match) return null;
	    }
	}
	try {
	    Class pc = p.getClass();
	    Constructor c = pc.getConstructor(PARAMS);
	    return (Permission) c.newInstance(new Object[] { name, actions });
	} catch (Exception e) {
	    return null;
	}
    }

    /**
     * This method always returns false for unresolved permissions.
     * That is, an UnresolvedPermission is never considered to
     * imply another permission.
     *
     * @param p the permission to check against.
     * 
     * @return false.
     */
    public boolean implies(Permission p) {
	return false;
    }

    /**
     * Checks two UnresolvedPermission objects for equality. 
     * Checks that <i>obj</i> is an UnresolvedPermission, and has 
     * the same type (class) name, permission name, actions, and
     * certificates as this object.
     * 
     * @param obj the object we are testing for equality with this object.
     * 
     * @return true if obj is an UnresolvedPermission, and has the same 
     * type (class) name, permission name, actions, and
     * certificates as this object.
     */
    public boolean equals(Object obj) {
	if (obj == this)
	    return true;

	if (! (obj instanceof UnresolvedPermission))
	    return false;
	UnresolvedPermission that = (UnresolvedPermission) obj;

	if (!(this.type.equals(that.type) &&
	    this.name.equals(that.name) &&
	    this.actions.equals(that.actions)))
	    return false;

	if ((this.certs != null) && (this.certs.length != that.certs.length))
	    return false;
	    
	int i,j;
	boolean match;

	if (this.certs != null) {
	    for (i = 0; i < this.certs.length; i++) {
		match = false;
		for (j = 0; j < that.certs.length; j++) {
		    if (this.certs[i].equals(that.certs[j])) {
			match = true;
			break;
		    }
		}
		if (!match) return false;
	    }

	    for (i = 0; i < that.certs.length; i++) {
		match = false;
		for (j = 0; j < this.certs.length; j++) {
		    if (that.certs[i].equals(this.certs[j])) {
			match = true;
			break;
		    }
		}
		if (!match) return false;
	    }
	}
	return true;
    }

    /**
     * Returns the hash code value for this object.
     *
     * @return a hash code value for this object.
     */

    public int hashCode() {
	int hash = type.hashCode();
	if (name != null)
	    hash ^= name.hashCode();
	if (actions != null)
	    hash ^= actions.hashCode();
	return hash;
    }

    /**
     * Returns the canonical string representation of the actions,
     * which currently is the empty string "", since there are no actions for 
     * an UnresolvedPermission. That is, the actions for the
     * permission that will be created when this UnresolvedPermission
     * is resolved may be non-null, but an UnresolvedPermission
     * itself is never considered to have any actions.
     *
     * @return the empty string "".
     */
    public String getActions()
    {
	return "";
    }

    /**
     * Returns a string describing this UnresolvedPermission.  The convention 
     * is to specify the class name, the permission name, and the actions, in
     * the following format: '(unresolved "ClassName" "name" "actions")'.
     * 
     * @return information about this UnresolvedPermission.
     */
    public String toString() {
	return "(unresolved " + type + " " + name + " " + actions + ")";
    }

    /**
     * Returns a new PermissionCollection object for storing 
     * UnresolvedPermission  objects.
     * <p>
     * @return a new PermissionCollection object suitable for 
     * storing UnresolvedPermissions.
     */

    public PermissionCollection newPermissionCollection() {
	return new UnresolvedPermissionCollection();
    }

    /**
     * Writes this object out to a stream (i.e., serializes it).
     *
     * @serialData An initial <code>String</code> denoting the
     * <code>type</code> is followed by a <code>String</code> denoting the
     * <code>name</code> is followed by a <code>String</code> denoting the
     * <code>actions</code> is followed by an <code>int</code> indicating the
     * number of certificates to follow 
     * (a value of "zero" denotes that there are no certificates associated
     * with this object).
     * Each certificate is written out starting with a <code>String</code>
     * denoting the certificate type, followed by an
     * <code>int</code> specifying the length of the certificate encoding,
     * followed by the certificate encoding itself which is written out as an
     * array of bytes.
     */
    private synchronized void writeObject(java.io.ObjectOutputStream oos)
        throws IOException
    {
	oos.defaultWriteObject();

	if (certs==null || certs.length==0) {
	    oos.writeInt(0);
	} else {
	    // write out the total number of certs
	    oos.writeInt(certs.length);
	    // write out each cert, including its type
	    for (int i=0; i < certs.length; i++) {
		java.security.cert.Certificate cert = certs[i];
		try {
		    oos.writeUTF(cert.getType());
		    byte[] encoded = cert.getEncoded();
		    oos.writeInt(encoded.length);
		    oos.write(encoded);
		} catch (CertificateEncodingException cee) {
		    throw new IOException(cee.getMessage());
		}
	    }
	}
    }

    /**
     * Restores this object from a stream (i.e., deserializes it).
     * In the case of an implementation which does not provide
     * CertificateFactory support, a ClassNotFoundException will 
     * be thrown.
     */
    private synchronized void readObject(java.io.ObjectInputStream ois)
	throws IOException, ClassNotFoundException
    {
	Object cf = null;		// No certificates in J2ME CDC
	Hashtable cfs=null;

	ois.defaultReadObject();

	if (type == null) 
		throw new NullPointerException("type can't be null");

	// process any new-style certs in the stream (if present)
	int size = ois.readInt();
	if (size > 0) {
	    // we know of 3 different cert types: X.509, PGP, SDSI, which
	    // could all be present in the stream at the same time
	    cfs = new Hashtable(3);
	    this.certs = new java.security.cert.Certificate[size];
	}

	for (int i=0; i<size; i++) {
	    // read the certificate type, and instantiate a certificate
	    // factory of that type (reuse existing factory if possible)
	    String certType = ois.readUTF();
	    if (cfs.containsKey(certType)) {
		// reuse certificate factory
		cf = null;
	    } else {
		// NOTE: Removed certificates from J2ME CDC
                // Throw a ClassNotFoundException to indicate that
                // we have no CertificateFactory class. See bug
                // 4916953, originally filed against CodeSource.
                throw new ClassNotFoundException
                    ( "Certificate factory for " + certType + " not found" );
	    }
	    // parse the certificate
	    byte[] encoded=null;
	    try {
		encoded = new byte[ois.readInt()];
	    } catch (OutOfMemoryError oome) {
		throw new IOException("Certificate too big");
	    }
	    ois.readFully(encoded);
	    ByteArrayInputStream bais = new ByteArrayInputStream(encoded);
	    // NOTE: Removed certificates from J2ME CDC

	    bais.close();
	}
    }
}
