/*
 * @(#)JarFile.java	1.46 06/10/11
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

package java.util.jar;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.security.cert.Certificate;
import sun.security.util.ManifestEntryVerifier;

/**
 * The <code>JarFile</code> class is used to read the contents of a JAR file
 * from any file that can be opened with <code>java.io.RandomAccessFile</code>.
 * It extends the class <code>java.util.zip.ZipFile</code> with support
 * for reading an optional <code>Manifest</code> entry. The
 * <code>Manifest</code> can be used to specify meta-information about the
 * JAR file and its entries.
 *
 * @author  David Connelly
 * @version 1.38, 02/02/00
 * @see	    Manifest
 * @see     java.util.zip.ZipFile
 * @see     java.util.jar.JarEntry
 * @since   1.2
 */
public
class JarFile extends ZipFile {
    private Manifest man;
    private JarEntry manEntry;
    private boolean manLoaded;
    private JarVerifier jv;
    private boolean jvInitialized;
    private boolean verify;

    /**
     * The JAR manifest file name.
     */
    public static final String MANIFEST_NAME = "META-INF/MANIFEST.MF";

    /**
     * Creates a new <code>JarFile</code> to read from the specified
     * file <code>name</code>. The <code>JarFile</code> will be verified if
     * it is signed.
     * @param name the name of the JAR file to be opened for reading
     * @exception IOException if an I/O error has occurred
     * @exception SecurityException if access to the file is denied
     *            by the SecurityManager
     */
    public JarFile(String name) throws IOException {
	this(new File(name), true, ZipFile.OPEN_READ);
    }

    /**
     * Creates a new <code>JarFile</code> to read from the specified
     * file <code>name</code>.
     * @param name the name of the JAR file to be opened for reading
     * @param verify whether or not to verify the JarFile if
     * it is signed.
     * @exception IOException if an I/O error has occurred
     * @exception SecurityException if access to the file is denied
     *            by the SecurityManager 
     */
    public JarFile(String name, boolean verify) throws IOException {
        this(new File(name), verify, ZipFile.OPEN_READ);
    }

    /**
     * Creates a new <code>JarFile</code> to read from the specified
     * <code>File</code> object. The <code>JarFile</code> will be verified if
     * it is signed.
     * @param file the JAR file to be opened for reading
     * @exception IOException if an I/O error has occurred
     * @exception SecurityException if access to the file is denied
     *            by the SecurityManager
     */
    public JarFile(File file) throws IOException {
	this(file, true, ZipFile.OPEN_READ);
    }


    /**
     * Creates a new <code>JarFile</code> to read from the specified
     * <code>File</code> object.
     * @param file the JAR file to be opened for reading
     * @param verify whether or not to verify the JarFile if
     * it is signed.
     * @exception IOException if an I/O error has occurred
     * @exception SecurityException if access to the file is denied
     *            by the SecurityManager.
     */
    public JarFile(File file, boolean verify) throws IOException {
	this(file, verify, ZipFile.OPEN_READ);
    }


    /**
     * Creates a new <code>JarFile</code> to read from the specified
     * <code>File</code> object in the specified mode.  The mode argument
     * must be either <tt>OPEN_READ</tt> or <tt>OPEN_READ | OPEN_DELETE</tt>.
     *
     * @param file the JAR file to be opened for reading
     * @param verify whether or not to verify the JarFile if
     * it is signed.
     * @param mode the mode in which the file is to be opened
     * @exception IOException if an I/O error has occurred
     * @exception IllegalArgumentException
     *            If the <tt>mode</tt> argument is invalid
     * @exception SecurityException if access to the file is denied
     *            by the SecurityManager
     */
    public JarFile(File file, boolean verify, int mode) throws IOException {
	super(file, mode);
	this.verify = verify;
    }

    /**
     * Returns the JAR file manifest, or <code>null</code> if none.
     *
     * @return the JAR file manifest, or <code>null</code> if none
     */
    public Manifest getManifest() throws IOException {
	if (!manLoaded) {
	    // First look up manifest entry using standard name
	    manEntry = getJarEntry(MANIFEST_NAME);
	    if (manEntry == null) {
		// If not found, then iterate through all the "META-INF/"
		// entries to find a match.
		String[] names = getMetaInfEntryNames();
		if (names != null) {
		    for (int i = 0; i < names.length; i++) {
			if (MANIFEST_NAME.equals(names[i].toUpperCase())) {
			    manEntry = getJarEntry(names[i]);
			    break;
			}
		    }
		}
	    }
	    // If found then load the manifest
	    if (manEntry != null) {
		if (verify) {
		    byte[] b = getBytes(manEntry);
		    man = new Manifest(new ByteArrayInputStream(b));
		    jv = new JarVerifier(man, b);
		} else {
		    man = new Manifest(super.getInputStream(manEntry));
		}
	    }
	    manLoaded = true;
	}
	return man;
    }

    private native String[] getMetaInfEntryNames();

    /**
     * Returns the <code>JarEntry</code> for the given entry name or
     * <code>null</code> if not found.
     *
     * @param name the JAR file entry name
     * @return the <code>JarEntry</code> for the given entry name or
     *         <code>null</code> if not found.
     * @see java.util.jar.JarEntry
     */
    public JarEntry getJarEntry(String name) {
	return (JarEntry)getEntry(name);
    }

    /**
     * Returns the <code>ZipEntry</code> for the given entry name or
     * <code>null</code> if not found.
     *
     * @param name the JAR file entry name
     * @return the <code>ZipEntry</code> for the given entry name or
     *         <code>null</code> if not found
     * @see java.util.zip.ZipEntry
     */
    public ZipEntry getEntry(String name) {
	ZipEntry ze = super.getEntry(name);
	if (ze != null) {
	    return new JarFileEntry(ze);
	}
	return null;
    }

    /**
     * Returns an enumeration of the ZIP file entries.
     */
    public Enumeration entries() {
	final Enumeration enum_ = super.entries();
	return new Enumeration() {
	    public boolean hasMoreElements() {
		return enum_.hasMoreElements();
	    }
	    public Object nextElement() {
		ZipEntry ze = (ZipEntry)enum_.nextElement();
		return new JarFileEntry(ze);
	    }
	};
    }

    private class JarFileEntry extends JarEntry {
	JarFileEntry(ZipEntry ze) {
	    super(ze);
	}
	public Attributes getAttributes() throws IOException {
	    Manifest man = JarFile.this.getManifest();
	    if (man != null) {
		return man.getAttributes(getName());
	    } else {
		return null;
	    }
	}
	public java.security.cert.Certificate[] getCertificates() {
	    if (certs == null && jv != null) {
		Certificate[] cs = jv.getCerts(getName());
		if (cs != null) {
		    certs = (Certificate[])cs.clone();
		}
	    }
	    return certs;
	}
    }
	    
    /*
     * Initializes the verifier object by reading all the manifest
     * entries and passing them to the verifier.
     */
    private void initializeVerifier() {
	ManifestEntryVerifier mev = null;

	// Verify "META-INF/" entries...
	try {
	    String[] names = getMetaInfEntryNames();
	    if (names != null) {
		for (int i = 0; i < names.length; i++) {
		    JarEntry e = getJarEntry(names[i]);
		    if (!e.isDirectory()) {
			if (mev == null) {
			    mev = new ManifestEntryVerifier(man);
			}
			byte[] b = getBytes(e);
			if (b != null && b.length > 0) {
			    jv.beginEntry(e, mev);
			    jv.update(b.length, b, 0, b.length, mev);
			    jv.update(-1, null, 0, 0, mev);
			}
		    }
		}
	    }
	} catch (IOException ex) {
	    // if we had an error parsing any blocks, just
	    // treat the jar file as being unsigned
	    jv = null;
	}

	// if after initializing the verifier we have nothing
	// signed, we null it out.

	if (jv != null) {

	    jv.doneWithMeta();
	    if (JarVerifier.debug != null) {
		JarVerifier.debug.println("done with meta!"); 
	    }

	    if (jv.nothingToVerify()) {
		if (JarVerifier.debug != null) {
		    JarVerifier.debug.println("nothing to verify!");
		}
		jv = null;
	    }
	}
    }

    /*
     * Reads all the bytes for a given entry. Used to process the
     * the META-INF files.
     */
    private byte[] getBytes(ZipEntry ze) throws IOException {
	byte[] b = new byte[(int)ze.getSize()];
	DataInputStream is = new DataInputStream(super.getInputStream(ze));
	is.readFully(b, 0, b.length);
	is.close();
	return b;
    }

    /**
     * Returns an input stream for reading the contents of the specified
     * ZIP file entry.
     * @param ze the zip file entry
     * @return an input stream for reading the contents of the specified
     *         ZIP file entry
     * @exception ZipException if a ZIP format error has occurred
     * @exception IOException if an I/O error has occurred
     * @exception SecurityException if any of the JarFile entries are incorrectly signed.
     */
    public synchronized InputStream getInputStream(ZipEntry ze) 
	throws IOException 
    {
	if (!manLoaded) {
	    getManifest();
	}
	if (jv == null) {
	    return super.getInputStream(ze);
	}
	if (!jvInitialized) {
	    initializeVerifier();
	    jvInitialized = true;
	    // could be set to null after a call to
	    // initializeVerifier if we have nothing to
	    // verify
	    if (jv == null)
		return super.getInputStream(ze);
	}

	// wrap a verifier stream around the real stream
	return new JarVerifier.VerifierStream(man, (JarEntry)ze,
					      super.getInputStream(ze), jv);
    }
}
