/*
 * @(#)SoftReference.java	1.34 06/10/10
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

package java.lang.ref;


/**
 * Soft reference objects, which are cleared at the discretion of the garbage
 * collector in response to memory demand.  Soft references are most often used
 * to implement memory-sensitive caches.
 *
 * <p> Suppose that the garbage collector determines at a certain point in time
 * that an object is <a href="package-summary.html#reachability">softly
 * reachable</a>.  At that time it may choose to clear atomically all soft
 * references to that object and all soft references to any other
 * softly-reachable objects from which that object is reachable through a chain
 * of strong references.  At the same time or at some later time it will
 * enqueue those newly-cleared soft references that are registered with
 * reference queues.
 *
 * <p> All soft references to softly-reachable objects are guaranteed to have
 * been cleared before the virtual machine throws an
 * <code>OutOfMemoryError</code>.  Otherwise no constraints are placed upon the
 * time at which a soft reference will be cleared or the order in which a set
 * of such references to different objects will be cleared.  Virtual machine
 * implementations are, however, encouraged to bias against clearing
 * recently-created or recently-used soft references.
 *
 * <p> Direct instances of this class may be used to implement simple caches;
 * this class or derived subclasses may also be used in larger data structures
 * to implement more sophisticated caches.  As long as the referent of a soft
 * reference is strongly reachable, that is, is actually in use, the soft
 * reference will not be cleared.  Thus a sophisticated cache can, for example,
 * prevent its most recently used entries from being discarded by keeping
 * strong referents to those entries, leaving the remaining entries to be
 * discarded at the discretion of the garbage collector.
 *
 * @version  1.27, 02/02/00
 * @author   Mark Reinhold
 * @since    1.2
 */

public class SoftReference extends Reference {

    /* Timestamp clock, updated by the garbage collector
     */
    static private long clock;

    /* Timestamp updated by each invocation of the get method.  The VM may use
     * this field when selecting soft references to be cleared, but it is not
     * required to do so.
     */
    private long timestamp;

    /**
     * Creates a new soft reference that refers to the given object.  The new
     * reference is not registered with any queue.
     *
     * @param referent object the new soft reference will refer to
     */
    public SoftReference(Object referent) {
	super(referent);
	this.timestamp = clock;
    }

    /**
     * Creates a new soft reference that refers to the given object and is
     * registered with the given queue.
     *
     * @param referent object the new soft reference will refer to
     * @param q queue the soft reference is registered with
     * @throws  NullPointerException  If the <code>queue</code> argument
     *                                is <code>null</code>
     *
     */
    public SoftReference(Object referent, ReferenceQueue q) {
	super(referent, q);
	this.timestamp = clock;
    }

    /**
     * Returns this reference object's referent.  If this reference object has
     * been cleared, either by the program or by the garbage collector, then
     * this method returns <code>null</code>.
     *
     * @return   The object to which this reference refers, or
     *           <code>null</code> if this reference object has been cleared
     */
    public Object get() {
	Object o = super.get();
	if (o != null) this.timestamp = clock;
	return o;
    }

}
