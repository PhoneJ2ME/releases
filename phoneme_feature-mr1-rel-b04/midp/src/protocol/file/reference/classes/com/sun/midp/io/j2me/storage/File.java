/*
 *
 * Copyright  1990-2006 Sun Microsystems, Inc. All Rights Reserved.
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
 */

package com.sun.midp.io.j2me.storage;

import java.io.IOException;

import java.util.Vector;

import com.sun.midp.security.SecurityToken;
import com.sun.midp.security.Permissions;

import com.sun.midp.midlet.Scheduler;
import com.sun.midp.midlet.MIDletSuite;

import com.sun.midp.io.Util;

/**
 * Provide the methods to manage files in a device's persistant storage.
 */
public class File {
    /** Table to speed up the unicodeToAsciiFilename conversion method. */
    private static final char NUMS[] = {
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    /** Caches the storage root to save repeated native method calls. */
    private static String storageRoot = null;

    /** Caches the configuration root to save repeated native method calls. */
    private static String configRoot = null;

    /**
     * Returns the root to build storage filenames including an needed
     * file separators, abstracting difference of the file systems
     * of development and device platforms. Note the root is never null.
     *
     * @return root of any filename for accessing device persistant
     *         storage. 
     */
    public static String getStorageRoot() {
        if (storageRoot == null) {
            storageRoot = initStorageRoot();
        } 

        return storageRoot;
    }

    /**
     * Returns the root to build configuration filenames including an needed
     * file separators, abstracting difference of the file systems
     * of development and device platforms. Note the root is never null.
     *
     * @return root of any configuration filename for accessing device
     *     persistant storage. 
     */
    public static String getConfigRoot() {
        if (configRoot == null) {
            configRoot = initConfigRoot();
        } 

        return configRoot;
    }

    /**
     * Convert a file name into a form that can be safely stored on
     * an ANSI-compatible file system. All characters that are not
     * [A-Za-z0-9] are converted into %uuuu, where uuuu is the hex
     * representation of the character's unicode value. Note even
     * though "_" is allowed it is converted because we use it for
     * for internal purposes. Potential file separators are converted
     * so the native layer does not have deal with sub-directory hierarchies.
     *
     * @param str a string that may contain any character
     * @return an equivalent string that contains only the "safe" characters.
     */
    public static String unicodeToAsciiFilename(String str) {
        StringBuffer sbuf = new StringBuffer();

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if ((c >= 'a' && c <= 'z') ||
                (c >= '0' && c <= '9')) {
                sbuf.append(c);
            } else if (c >= 'A' && c <= 'Z') {
		sbuf.append('#');
		sbuf.append(c);
	    } else {
                int v = (int)(c & 0xffff);
                sbuf.append('%');
                sbuf.append(NUMS[(v & 0xf000) >> 12]);
                sbuf.append(NUMS[(v & 0x0f00) >>  8]);
                sbuf.append(NUMS[(v & 0x00f0) >>  4]);
                sbuf.append(NUMS[(v & 0x000f) >>  0]);
            }
        }

        return sbuf.toString();
    }

    /**
     * Perform the reverse conversion of unicodeToAscii().
     *
     * @param str a string previously returned by escape()
     * @return the original string before the conversion by escape().
     */
    public static String asciiFilenameToUnicode(String str) {
        StringBuffer sbuf = new StringBuffer();

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '%') {
                int v = 0;

                v <<= 4; v += hexValue(str.charAt(i+1));
                v <<= 4; v += hexValue(str.charAt(i+2));
                v <<= 4; v += hexValue(str.charAt(i+3));
                v <<= 4; v += hexValue(str.charAt(i+4));

                i += 4;
                sbuf.append((char)(v & 0x0000ffff));
            } else if (c == '#') {
		// drop c
	    } else {
                sbuf.append(c);
            }
        }

        return sbuf.toString();
    }

    /**
     * A utility method that convert a hex character 0-9a-f to the
     * numerical value represented by this hex char.
     *
     * @param c the character to convert
     * @return the number represented by the character. E.g., '0' represents
     * the number 0x0, 'a' represents the number 0x0a, etc.
     */
    private static int hexValue(char c) {
        if (c >= '0' && c <= '9') {
            return ((int)c) - '0';
        } else {
            return ((int)c) - 'a' + 10;
        }
    }

    /**
     * Constructs a file object.
     */
    public File() {
        MIDletSuite midletSuite = Scheduler.getScheduler().getMIDletSuite();

        // if a MIDlet suite is not scheduled, assume the JAM is calling.
        if (midletSuite != null) {
            midletSuite.checkIfPermissionAllowed(Permissions.AMS);
        }
    }

    /**
     * Constructs a file object.
     *
     * @param callerSecurityToken security token of the caller
     */
    public File(SecurityToken callerSecurityToken) {
        callerSecurityToken.checkIfPermissionAllowed(Permissions.AMS);
    }

    /**
     * Replaces the current name of storage, <code>oldName</code>
     * with <code>newName</code>.
     *
     * @param oldName original name of storage file
     * @param newName new name for storage file
     * @exception IOException if an error occurs during rename
     */
    public synchronized void rename(String oldName, String newName)
            throws IOException {
        renameStorage(oldName, newName);
    }

    /**
     * Returns <code>true</code> if storage file <code>name</code>
     * exists.
     *
     * @param name name of storage file
     *
     * @return <code>true</code> if the named storage file exists
     */
    public synchronized boolean exists(String name) {
        return storageExists(name);
    }

    /**
     * Remove a file from storage if it exists.
     *
     * @param name name of the file to delete
     * @exception IOException if an error occurs during delete
     */
    public synchronized void delete(String name)
            throws IOException {
        deleteStorage(name);
    }

    /**
     * Retrieves the approximate space available to grow or
     * create new storage files.
     *
     * @return approximate number of free bytes in storage
     */
    public int getBytesAvailableForFiles() {
        return availableStorage();
    }

    /**
     * Initializes storage root for this file instance.
     *
     * @return path of the storage root
     */
    private static native String initStorageRoot();

    /**
     * Initializes the configuration root for this file instance.
     *
     * @return path of the configuration root
     */
    private static native String initConfigRoot();

    /**
     * Renames storage file.
     *
     * @param oldName old name of storage file
     * @param newName new name for storage file
     */
    private static native void renameStorage(String oldName,
                                             String newName)
        throws IOException;
    
    /**
     * Determines if a storage file matching filename exists.
     *
     * @param filename storage file to match
     *
     * @return <code>true</code> if storage indicated by 
     *         <code>szFilename</code> exists
     */
    private static native boolean storageExists(String filename);

    /**
     * Removes a file from storage.
     *
     * @param filename storage file to delete
     *
     * @exception IOException if an error occurs during deletion
     */
    private static native void deleteStorage(String filename)
        throws IOException;

    /**
     * Gets the approximate number of free storage bytes remaining.
     *
     * @return free storage space remaining, in bytes
     */
    private static native int availableStorage();
}
