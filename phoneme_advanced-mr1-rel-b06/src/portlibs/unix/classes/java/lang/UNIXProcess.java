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

package java.lang;

import java.io.*; 

/* java.lang.Process subclass in the UNIX environment.
 * 
 * @author Mario Wolczko and Ross Knippel.
 */

class UNIXProcess extends Process {
    private FileDescriptor stdin_fd;
    private FileDescriptor stdout_fd;
    private FileDescriptor stderr_fd;
    private int pid;
    private int exitcode;
    private boolean hasExited;

    private OutputStream stdin_stream;
    private BufferedInputStream stdout_stream;
    private DeferredCloseInputStream stdout_inner_stream;
    private DeferredCloseInputStream stderr_stream;

    /* this is for the reaping thread */
    private native int waitForProcessExit(int pid);

    private UNIXProcess() {}

    private native int forkAndExec(String cmd[], String env[], String path,
				   FileDescriptor stdin_fd,
				   FileDescriptor stdout_fd,
				   FileDescriptor stderr_fd)
	throws java.io.IOException;
  
    UNIXProcess(String cmdarray[], String env[]) throws java.io.IOException {
	this(cmdarray, env, null);
    }

    UNIXProcess(String cmdarray[], String env[], String path)
    throws java.io.IOException {

	stdin_fd = new FileDescriptor();
	stdout_fd = new FileDescriptor();
	stderr_fd = new FileDescriptor();
	
	pid = forkAndExec(cmdarray, env, path, stdin_fd, stdout_fd, stderr_fd);

	java.security.AccessController.doPrivileged(
				    new java.security.PrivilegedAction() {
	    public Object run() {
	        stdin_stream
		    = new BufferedOutputStream(new FileOutputStream(stdin_fd));
		stdout_inner_stream = new DeferredCloseInputStream(stdout_fd);
	        stdout_stream = new BufferedInputStream(stdout_inner_stream);
	        stderr_stream = new DeferredCloseInputStream(stderr_fd);
		return null;
	    }
	});

	/*
	 * For each subprocess forked a corresponding reaper thread
	 * is started.  That thread is the only thread which waits
	 * for the subprocess to terminate and it doesn't hold any
	 * locks while doing so.  This design allows waitFor() and 
	 * exitStatus() to be safely executed in parallel (and they
	 * need no native code).
	 */
	 
	java.security.AccessController.doPrivileged(
			    new java.security.PrivilegedAction() {
	    public Object run() {
		Thread t = new Thread("process reaper") {
		    public void run() {
			int res = waitForProcessExit(pid);
			synchronized (UNIXProcess.this) {
			    hasExited = true;
			    exitcode = res;
			    UNIXProcess.this.notifyAll();
			}
		    }
		};
		t.setDaemon(true);
		t.start();
		return null;
	    }
	});
    }

    public OutputStream getOutputStream() {
	return stdin_stream;
    }

    public InputStream getInputStream() {
	return stdout_stream;
    }

    public InputStream getErrorStream() {
	return stderr_stream;
    }

    public synchronized int waitFor() throws InterruptedException {
        while (!hasExited) {
	    wait();
	}
	return exitcode;
    }

    public synchronized int exitValue() { 
	if (!hasExited) {
	    throw new IllegalThreadStateException("process hasn't exited");
	}
	return exitcode;
    }

    private static native void destroyProcess(int pid);

    public synchronized void destroy() {
	destroyProcess(pid);
	try {
            stdin_stream.close();
	    stdout_inner_stream.closeDeferred(stdout_stream);
	    stderr_stream.closeDeferred(stderr_stream);
        } catch (IOException e) {
            // ignore
        }
    }

    // A FileInputStream that supports the deferment of the actual close
    // operation until the last pending I/O operation on the stream has
    // finished.  This is required on Solaris because we must close the stdin
    // and stdout streams in the destroy method in order to reclaim the
    // underlying file descriptors.  Doing so, however, causes any thread
    // currently blocked in a read on one of those streams to receive an
    // IOException("Bad file number"), which is incompatible with historical
    // behavior.  By deferring the close we allow any pending reads to see -1
    // (EOF) as they did before.
    //
    private static class DeferredCloseInputStream
	extends FileInputStream
    {

	private DeferredCloseInputStream(FileDescriptor fd) {
	    super(fd);
	}

	private Object lock = new Object();	// For the following fields
	private boolean closePending = false;
	private int useCount = 0;
	private InputStream streamToClose;

	private void raise() {
	    synchronized (lock) {
		useCount++;
	    }
	}

	private void lower() throws IOException {
	    synchronized (lock) {
		useCount--;
		if (useCount == 0 && closePending) {
		    streamToClose.close();
		}
	    }
	}

	// stc is the actual stream to be closed; it might be this object, or
	// it might be an upstream object for which this object is downstream.
	//
	private void closeDeferred(InputStream stc) throws IOException {
	    synchronized (lock) {
		if (useCount == 0) {
		    stc.close();
		} else {
		    closePending = true;
		    streamToClose = stc;
		}
	    }
	}

	public void close() throws IOException {
	    synchronized (lock) {
		useCount = 0;
		closePending = false;
	    }
	    super.close();
	}

	public int read() throws IOException {
	    raise();
	    try {
		return super.read();
	    } finally {
		lower();
	    }
	}

	public int read(byte[] b) throws IOException {
	    raise();
	    try {
		return super.read(b);
	    } finally {
		lower();
	    }
	}

	public int read(byte[] b, int off, int len) throws IOException {
	    raise();
	    try {
		return super.read(b, off, len);
	    } finally {
		lower();
	    }
	}

	public long skip(long n) throws IOException {
	    raise();
	    try {
		return super.skip(n);
	    } finally {
		lower();
	    }
	}

	public int available() throws IOException {
	    raise();
	    try {
		return super.available();
	    } finally {
		lower();
	    }
	}

    }

}
