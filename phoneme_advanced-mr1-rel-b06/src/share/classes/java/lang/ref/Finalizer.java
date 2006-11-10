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

package java.lang.ref;

import java.security.PrivilegedAction;
import java.security.AccessController;
import sun.misc.ThreadRegistry;
import sun.misc.CVM;


final class Finalizer extends FinalReference { /* Package-private; must be in
						  same package as the Reference
						  class */

    /* A native method that invokes an arbitrary object's finalize method is
       required since the finalize method is protected
     */
    static native void invokeFinalizeMethod(Object o) throws Throwable;

    static private ReferenceQueue queue = new ReferenceQueue();
    static private Finalizer unfinalized = null;
    static private Object lock = new Object();

    private Finalizer
        next = null,
	prev = null;

    private boolean hasBeenFinalized() {
	return (next == this);
    }

    private void add() {
	synchronized (lock) {
	    if (unfinalized != null) {
		this.next = unfinalized;
		unfinalized.prev = this;
	    }
	    unfinalized = this;
	}
    }

    private void remove() {
	synchronized (lock) {
	    if (unfinalized == this) {
		if (this.next != null) {
		    unfinalized = this.next;
		} else {
		    unfinalized = this.prev;
		}
	    }
	    if (this.next != null) {
		this.next.prev = this.prev;
	    }
	    if (this.prev != null) {
		this.prev.next = this.next;
	    }
	    this.next = this;	/* Indicates that this has been finalized */
	    this.prev = this;
	}
    }

    private Finalizer(Object finalizee) {
	super(finalizee, queue);
	add();
    }

    /* Invoked by VM */
    static void register(Object finalizee) {
	new Finalizer(finalizee);
    }

    /* This function is called with remote exception disabled */
    private void runFinalizer() {
	synchronized (this) {
	    if (hasBeenFinalized()) {
		CVM.enableRemoteExceptions();	// lvm
		return;
	    }
	    remove();
	}
	try {
	    Object finalizee = this.get();
	    if (finalizee != null) {
		invokeFinalizeMethod(finalizee);
		/* Clear stack slot containing this variable, to decrease
		   the chances of false retention with a conservative GC */
		finalizee = null;
	    } else {
		CVM.enableRemoteExceptions();	// lvm
	    }
	} catch (ThreadDeath td) {
	    throw td;
	} catch (Throwable x) { }
	super.clear();
    }

    /* Create a privileged secondary finalizer thread in the system thread
       group for the given Runnable, and wait for it to complete.

       This method is used by both runFinalization and runFinalizersOnExit.
       The former method invokes all pending finalizers, while the latter
       invokes all uninvoked finalizers if on-exit finalization has been
       enabled.

       These two methods could have been implemented by offloading their work
       to the regular finalizer thread and waiting for that thread to finish.
       The advantage of creating a fresh thread, however, is that it insulates
       invokers of these methods from a stalled or deadlocked finalizer thread.
     */
    private static void forkSecondaryFinalizer(final Runnable proc) {
	PrivilegedAction pa = new PrivilegedAction() {
	    public Object run() {
		ThreadGroup tg = Thread.currentThread().getThreadGroup();
		for (ThreadGroup tgn = tg;
		     tgn != null;
		     tg = tgn, tgn = tg.getParent());
		Thread sft = new Thread(tg, proc, "Secondary finalizer");
		sft.start();
		try {
		    sft.join();
		} catch (InterruptedException x) {
		    /* Ignore */
		}
		return null;
	    }};
	AccessController.doPrivileged(pa);
    }

    /* Called by Runtime.runFinalization() */
    static void runFinalization() {
	forkSecondaryFinalizer(new Runnable() {
	    public void run() {
		for (;;) {
		    Finalizer f = (Finalizer)queue.poll();
		    if (f == null) break;
		    CVM.disableRemoteExceptions();	// lvm
		    f.runFinalizer();
		}
	    }
	});
    }

    /* Invoked by java.lang.Shutdown */
    static void runAllFinalizers() {
	forkSecondaryFinalizer(new Runnable() {
	    public void run() {
		for (;;) {
		    Finalizer f;
		    CVM.disableRemoteExceptions();	// lvm
		    synchronized (lock) {
			f = unfinalized;
			if (f == null) break;
			unfinalized = f.next;
		    }
		    f.runFinalizer();
		}}});
    }

    // %begin lvm
    /* Invoked by sun.misc.LogicalVMImpl when we shutdown a Logical VM.
     * Run all the finalizers of system class without forking a new
     * finalizer thread since new thread creation is disenabled during 
     * LVM termination. We know the thread that executes this code, and
     * have full control over it. */
    static void runAllFinalizersOfSystemClass() {
	for (;;) {
	    Finalizer f;
	    CVM.disableRemoteExceptions();	// lvm
	    synchronized (lock) {
		f = unfinalized;
		if (f == null) break;
		unfinalized = f.next;
	    }
	    Object finalizee = f.get();
	    if (finalizee.getClass().getClassLoader() == null) {
		f.runFinalizer();
	    } else {
		CVM.enableRemoteExceptions();	// lvm
	    }
	}
    }
    // %end lvm

    private static class FinalizerThread extends Thread {
	FinalizerThread(ThreadGroup g) {
	    super(g, "Finalizer");
	}
	public void run() {
	    while (!ThreadRegistry.exitRequested()) {
		try {
		    CVM.disableRemoteExceptions();	// lvm
		    Finalizer f = (Finalizer)queue.remove();
		    f.runFinalizer();
		} catch (InterruptedException x) {
		    continue;
		}
	    }
	}
    }

    //
    // Package private, so that the finalizer thread can be re-startable.
    //
    static void startFinalizerThread() {
        ThreadGroup tg = Thread.currentThread().getThreadGroup();
        for (ThreadGroup tgn = tg;
             tgn != null;
             tg = tgn, tgn = tg.getParent());
	Thread finalizer = new FinalizerThread(tg);
	finalizer.setPriority(Thread.MAX_PRIORITY - 2);
	finalizer.setDaemon(true);
	finalizer.start();
    }

    static {
	startFinalizerThread();
    }

}
