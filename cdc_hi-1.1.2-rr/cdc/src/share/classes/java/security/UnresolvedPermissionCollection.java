/*
 * @(#)UnresolvedPermissionCollection.java	1.17 06/10/10
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

import java.util.*;
import java.io.ObjectStreamField;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

/**
 * A UnresolvedPermissionCollection stores a collection
 * of UnresolvedPermission permissions.
 *
 * @see java.security.Permission
 * @see java.security.Permissions
 * @see java.security.UnresolvedPermission
 *
 * @version 1.9 00/02/02
 *
 * @author Roland Schemers
 *
 * @serial include
 */

final class UnresolvedPermissionCollection
extends PermissionCollection
implements java.io.Serializable
{
    /**
     * Key is permission type, value is a list of the UnresolvedPermissions
     * of the same type.
     * Not serialized; see serialization section at end of class.
     */
    private transient Map perms;

    /**
     * Create an empty UnresolvedPermissionCollection object.
     *
     */
    public UnresolvedPermissionCollection() {
	perms = new HashMap(11);
    }

    /**
     * Adds a permission to this UnresolvedPermissionCollection. 
     * The key for the hash is the unresolved permission's type (class) name.
     *
     * @param permission the Permission object to add.
     */

    public void add(Permission permission)
    {
	if (! (permission instanceof UnresolvedPermission))
	    throw new IllegalArgumentException("invalid permission: "+
					       permission);
	UnresolvedPermission up = (UnresolvedPermission) permission;

        // No need to synchronize because all adds are done sequentially
	// (by policy provider) before any getUnresolvedPermissions() calls.
	// If policy provider is multithreaded, it should lock the
	// collection to allow concurrent adds

	List v = (List) perms.get(up.getName());
	if (v == null) {
	    v = new ArrayList();
	    perms.put(up.getName(), v);
	}
	v.add(up);
    }

    /**
     * get any unresolved permissions of the same type as p,
     * and return the List containing them.
     */
    // No need to synchronize; list is only gotten for reading
    List getUnresolvedPermissions(Permission p) {
	return (List) perms.get(p.getClass().getName());
    }

    /**
     * always returns false for unresolved permissions
     *
     */
    public boolean implies(Permission permission)
    {
	return false;
    }

    /**
     * Returns an enumeration of all the UnresolvedPermission lists in the
     * container.
     *
     * @return an enumeration of all the UnresolvedPermission objects.
     */

    // No need to synchronize; lists won't change after initial adds
    // A performance improvement might be to construct the global 
    // list lazily as enumeration proceeds.
    
    public Enumeration elements() {
	List results = new ArrayList(); // where results are stored

	// Get iterator of Map values (which are lists of permissions)
	for (Iterator iter = perms.values().iterator(); iter.hasNext();) {
	    results.addAll((List) iter.next());
	}

	return Collections.enumeration(results);
    }

    private static final long serialVersionUID = -7176153071733132400L;

    // Need to maintain serialization interoperability with earlier releases,
    // which had the serializable field:
    // private Hashtable permissions; // keyed on type

    /**
     * @serialField permissions java.util.Hashtable
     *     A table of the UnresolvedPermissions keyed on type, value is Vector
     *     of permissions
     */
    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("permissions", Hashtable.class),
    };

    /**
     * @serialData Default field.
     */
    /*
     * Writes the contents of the perms field out as a Hashtable 
     * in which the values are Vectors for
     * serialization compatibility with earlier releases.
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
	// Don't call out.defaultWriteObject()

	// Copy perms into a Hashtable
	Hashtable permissions = new Hashtable(perms.size()*2);

	// Convert each entry (List) into a Vector
	Set set = perms.entrySet();
	for (Iterator iter = set.iterator(); iter.hasNext(); ) {
	    Map.Entry e = (Map.Entry)iter.next();

	    // Convert list into Vector
	    List list = (List) e.getValue();
	    Vector vec = new Vector(list.size());
	    vec.addAll(list);

	    // Add to Hashtable being serialized
	    permissions.put(e.getKey(), vec);
	}

	// Write out serializable fields
        ObjectOutputStream.PutField pfields = out.putFields();
        pfields.put("permissions", permissions);
        out.writeFields();
    }

    /*
     * Reads in a Hashtable in which the values are Vectors of
     * UnresolvedPermissions and saves them in the perms field. 
     */
    private void readObject(ObjectInputStream in) throws IOException, 
    ClassNotFoundException {
	// Don't call defaultReadObject()

	// Read in serialized fields
	ObjectInputStream.GetField gfields = in.readFields();

	// Get permissions
	Hashtable permissions = (Hashtable)gfields.get("permissions", null);
	perms = new HashMap(permissions.size()*2);

	// Convert each entry (Vector) into a List
	Set set = permissions.entrySet();
	for (Iterator iter = set.iterator(); iter.hasNext(); ) {
	    Map.Entry e = (Map.Entry)iter.next();

	    // Convert Vector into ArrayList
	    Vector vec = (Vector) e.getValue();
	    List list = new ArrayList(vec.size());
	    list.addAll(vec);

	    // Add to Hashtable being serialized
	    perms.put(e.getKey(), list);
	}
    }
}
