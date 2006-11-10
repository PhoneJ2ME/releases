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
 
package java.security;

import java.util.Vector;
import sun.security.util.Debug;
import sun.security.util.SecurityConstants;

/** 
 * An AccessControlContext is used to make system resource access decisions
 * based on the context it encapsulates.
 * 
 * <p>More specifically, it encapsulates a context and
 * has a single method, <code>checkPermission</code>,
 * that is equivalent to the <code>checkPermission</code> method
 * in the AccessController class, with one difference: The AccessControlContext
 * <code>checkPermission</code> method makes access decisions based on the 
 * context it encapsulates,
 * rather than that of the current execution thread.
 * 
 * <p>Thus, the purpose of AccessControlContext is for those situations where
 * a security check that should be made within a given context
 * actually needs to be done from within a
 * <i>different</i> context (for example, from within a worker thread).
 * 
 * <p> An AccessControlContext is created by calling the 
 * <code>AccessController.getContext</code> method. 
 * The <code>getContext</code> method takes a "snapshot"
 * of the current calling context, and places
 * it in an AccessControlContext object, which it returns. A sample call is
 * the following:
 * 
 * <pre>
 * 
 *   AccessControlContext acc = AccessController.getContext()
 * 
 * </pre>
 * 
 * <p>
 * Code within a different context can subsequently call the
 * <code>checkPermission</code> method on the
 * previously-saved AccessControlContext object. A sample call is the
 * following:
 * 
 * <pre>
 * 
 *   acc.checkPermission(permission)
 * 
 * </pre> 
 * 
 * @see AccessController
 *
 * @author Roland Schemers
 */

public final class AccessControlContext {

    private ProtectionDomain context[];
    private boolean isPrivileged;
    private AccessControlContext privilegedContext;
    private DomainCombiner combiner = null;

    private static boolean debugInit = false;
    private static Debug debug = null;

    static Debug getDebug()
    {
	if (debugInit)
	    return debug;
	else {
	    if (Policy.isSet()) {
		debug = Debug.getInstance("access");
		debugInit = true;
	    }
	    return debug;
	}
    }

    /**
     * Create an AccessControlContext with the given set of ProtectionDomains.
     * Context must not be null. Duplicate domains will be removed from the
     * context.
     *
     * @param context the ProtectionDomains associated with this context.
     */

    public AccessControlContext(ProtectionDomain context[])
    {
	if (context.length == 0) {
	    this.context = null;
	} else if (context.length == 1) {
	    this.context = (ProtectionDomain[])context.clone();
	} else {
	    Vector v = new Vector(context.length);
	    for (int i =0; i< context.length; i++) {
		if ((context[i] != null) &&  (!v.contains(context[i])))
		    v.addElement(context[i]);
	    }
	    this.context = new ProtectionDomain[v.size()];
	    v.copyInto(this.context);
	}
    }

    /**
     * Create a new <code>AccessControlContext</code> with the given
     * <code>AccessControlContext</code> and <code>DomainCombiner</code>.
     * This constructor associates the provided
     * <code>DomainCombiner</code> with the provided
     * <code>AccessControlContext</code>.
     *
     * <p>
     *
     * @param acc the <code>AccessControlContext</code> associated
     *		with the provided <code>DomainCombiner</code>. <p>
     *
     * @param combiner the <code>DomainCombiner</code> to be associated
     *		with the provided <code>AccessControlContext</code>.
     *
     * @exception NullPointerException if the provided
     *		<code>context</code> is <code>null</code>. <p>
     *
     * @exception SecurityException if the caller does not have permission
     *		to invoke this constructor.
     */
    public AccessControlContext(AccessControlContext acc,
				DomainCombiner combiner) {

	SecurityManager sm = System.getSecurityManager();
	if (sm != null) {
	    sm.checkPermission(SecurityConstants.CREATE_ACC_PERMISSION);
	}

	if (acc == null ){
	    throw new NullPointerException
		("null AccessControlContext was provided");
	}

	this.context = acc.context;

	// we do not need to run the combine method on the
	// provided ACC.  it was already "combined" when the
	// context was originally retrieved.
	//
	// at this point in time, we simply throw away the old
	// combiner and use the newly provided one.
	this.combiner = combiner;
    }

    private AccessControlContext(ProtectionDomain context[], 
				DomainCombiner combiner) {
	this.context = (ProtectionDomain[])context.clone();
	this.combiner = combiner;
    }

    /**
     * package private constructor for AccessController
     */

    AccessControlContext(ProtectionDomain context[], boolean isPrivileged,
				AccessControlContext privilegedContext)
    {
	this.context = context;
	this.isPrivileged = isPrivileged;
	this.privilegedContext = privilegedContext;
    }

    private AccessControlContext(ProtectionDomain context[],
				 boolean isPrivileged)
    {
	this(context, isPrivileged, null);
    }

    /**
     * Returns true if this context is privileged.
     */
    boolean isPrivileged() 
    {
	return isPrivileged;

    }

    /**
     * Get the <code>DomainCombiner</code> associated with this
     * <code>AccessControlContext</code>.
     *
     * <p>
     *
     * @return the <code>DomainCombiner</code> associated with this
     *		<code>AccessControlContext</code>, or <code>null</code>
     *		if there is none.
     *
     * @exception SecurityException if the caller does not have permission
     *		to get the <code>DomainCombiner</code> associated with this
     *		<code>AccessControlContext</code>.
     */
    public DomainCombiner getDomainCombiner() {

	SecurityManager sm = System.getSecurityManager();
	if (sm != null) {
	    sm.checkPermission(SecurityConstants.GET_COMBINER_PERMISSION);
	}
	return combiner;
    }

    /** 
     * Determines whether the access request indicated by the
     * specified permission should be allowed or denied, based on
     * the security policy currently in effect, and the context in
     * this object.
     * <p>
     * This method quietly returns if the access request
     * is permitted, or throws a suitable AccessControlException otherwise. 
     *
     * @param perm the requested permission.
     * 
     * @exception AccessControlException if the specified permission
     * is not permitted, based on the current security policy and the
     * context encapsulated by this object.
     * @exception NullPointerException if the permission to check for is null.
     */
    public void checkPermission(Permission perm)
	throws AccessControlException 
    {
	if (perm == null) {
	    throw new NullPointerException("permission can't be null");
	}
	if (getDebug() != null) {
	    if (Debug.isOn("stack"))
			Thread.currentThread().dumpStack();
	    if (Debug.isOn("domain")) {
		if (context == null) {
			debug.println("domain (context is null)");
		} else {
		    for (int i=0; i< context.length; i++) {
			debug.println("domain "+i+" "+context[i]);
		    }
		}
	    }
	}

	/*
	 * iterate through the ProtectionDomains in the context.
	 * Stop at the first one that doesn't allow the
	 * requested permission (throwing an exception).
	 *
	 */

	/* if ctxt is null, all we had on the stack were system domains,
	   or the first domain was a Privileged system domain. This
	   is to make the common case for system code very fast */

	if (context == null)
	    return;

	for (int i=0; i< context.length; i++) {
	    if (context[i] != null &&  !context[i].implies(perm)) {
		if (debug != null) {
		    debug.println("access denied "+perm);
		    if (Debug.isOn("failure")) {
			Thread.currentThread().dumpStack();
			final ProtectionDomain pd = context[i];
			final Debug db = debug;
			AccessController.doPrivileged (new PrivilegedAction() {
			    public Object run() {
				db.println("domain that failed "+pd);
				return null;
			    }
			});
		    }
		}
		throw new AccessControlException("access denied "+perm, perm);
	    }
	}

	// allow if all of them allowed access
	if (debug != null)
	    debug.println("access allowed "+perm);

	return;	
    }

    /**
     * Take the stack-based context (this) and combine it with the
     * privileged or inherited context, if need be.
     */
    AccessControlContext optimize()
    {
	// the assigned (privileged or inherited) context
	AccessControlContext acc;
	if (isPrivileged) {
	    acc = privilegedContext;
	} else {
	    acc = AccessController.getInheritedAccessControlContext();
	}

	// this.context could be null if only system code is on the stack;
	// in that case, ignore the stack context
	boolean skipStack = (context == null);

	// acc.context could be null if only system code was involved;
	// in that case, ignore the assigned context
	boolean skipAssigned = (acc == null || acc.context == null);

	// optimization: if neither have contexts; return acc if possible
	// rather than this, because acc might have a combiner
	if (skipAssigned && skipStack) {
	    return (acc != null) ? acc : this;
	}

	if (acc != null && acc.combiner != null) {
	    // let the assigned acc's combiner do its thing
	    return goCombiner(context, acc);
	}

	// optimization: if there is no stack context; there is no reason
	// to compress the assigned context, it already is compressed
	if (skipStack) {
	    return acc;
	}

	int slen = context.length;

	// optimization: if there is no assigned context and the stack length
	// is less then or equal to two; there is no reason to compress the
	// stack context, it already is
	if (skipAssigned && slen <= 2) {
	    return this;
	}

	// optimization: if there is a single stack domain and that domain
	// is already in the assigned context; no need to combine
	if ((slen == 1) && (context[0] == acc.context[0])) {
	    return acc;
	}

	int n = (skipAssigned) ? 0 : acc.context.length;

	// now we combine both of them, and create a new context
	ProtectionDomain pd[] = new ProtectionDomain[slen + n];

	// first copy in the assigned context domains, no need to compress
	if (!skipAssigned) {
	    System.arraycopy(acc.context, 0, pd, 0, n);
	}

	// now add the stack context domains, discarding nulls and duplicates
    outer:
	for (int i = 0; i < context.length; i++) {
	    ProtectionDomain sd = context[i];
	    if (sd != null) {
		for (int j = 0; j < n; j++) {
		    if (sd == pd[j]) {
			continue outer;
		    }
		}
		pd[n++] = sd;
	    }
	}

	// if length isn't equal, we need to shorten the array
	if (n != pd.length) {
	    // optimization: if we didn't really combine anything
	    if (!skipAssigned && n == acc.context.length) {
		return acc;
	    } else if (skipAssigned && n == slen) {
		return this;
	    }
	    ProtectionDomain tmp[] = new ProtectionDomain[n];
	    System.arraycopy(pd, 0, tmp, 0, n);
	    pd = tmp;
	}

	return new AccessControlContext(pd, false);
    }

    private AccessControlContext goCombiner(ProtectionDomain[] current,
					AccessControlContext assigned) {

	// the assigned ACC's combiner is not null --
	// let the combiner do its thing

	// We could add optimizations to 'current' here ...

	if (getDebug() != null) {
	    debug.println("AccessControlContext invoking the Combiner");
	}

	ProtectionDomain[] combinedPds = assigned.combiner.combine
		(current == null ?
			null :
			(ProtectionDomain[])current.clone(),
		assigned.context == null ?
			null :
			(ProtectionDomain[])assigned.context.clone());

	// return the new ACC
	return new AccessControlContext(combinedPds, assigned.combiner);
    }

    /**
     * Checks two AccessControlContext objects for equality. 
     * Checks that <i>obj</i> is
     * an AccessControlContext and has the same set of ProtectionDomains
     * as this context.
     * <P>
     * @param obj the object we are testing for equality with this object.
     * @return true if <i>obj</i> is an AccessControlContext, and has the 
     * same set of ProtectionDomains as this context, false otherwise.
     */
    public boolean equals(Object obj) {
	if (obj == this)
	    return true;

	if (! (obj instanceof AccessControlContext))
	    return false;

	AccessControlContext that = (AccessControlContext) obj;


	if (context == null) {
	    return (that.context == null);
	}

	if (that.context == null)
	    return false;

	if (!(this.containsAllPDs(that) && that.containsAllPDs(this)))
	    return false;

	if (this.combiner == null)
	    return (that.combiner == null);

	if (that.combiner == null)
	    return false;

	if (!this.combiner.equals(that.combiner))
	    return false;

	return true;

    }

    private boolean containsAllPDs(AccessControlContext that) {
	boolean match = false;
	//
	// ProtectionDomains within an ACC currently cannot be null
	// and this is enforced by the contructor and the various
	// optimize methods. However, historically this logic made attempts
	// to support the notion of a null PD and therefore this logic continues
	// to support that notion.
	for (int i = 0; i < context.length; i++) {
	    match = false;
	    if (context[i] == null) {
		for (int j = 0; (j < that.context.length) && !match; j++) {
		    match = (that.context[j] == null);
		}
	    } else {
		for (int j = 0; (j < that.context.length) && !match; j++) {
		    if (that.context[j] != null) {
			match =
			    ((context[i].getClass()==that.context[j].getClass()) &&
			     (context[i].equals(that.context[j])));
		    }
		}
	    }
	    if (!match) return false;
	}
	return match;
    }
    /**
     * Returns the hash code value for this context. The hash code
     * is computed by exclusive or-ing the hash code of all the protection
     * domains in the context together.
     * 
     * @return a hash code value for this context.
     */

    public int hashCode() {
	int hashCode = 0;

	if (context == null)
	    return hashCode;

	for (int i =0; i < context.length; i++) {
	    if (context[i] != null)
		hashCode ^= context[i].hashCode();
	}
	return hashCode;
    }
}
