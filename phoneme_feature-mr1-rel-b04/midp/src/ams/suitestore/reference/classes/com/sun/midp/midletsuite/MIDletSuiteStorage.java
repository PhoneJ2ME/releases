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

package com.sun.midp.midletsuite;

import java.util.Vector;

import java.io.IOException;

import com.sun.midp.security.SecurityToken;
import com.sun.midp.security.Permissions;

import com.sun.midp.midlet.MIDletSuite;
import com.sun.midp.midlet.MIDletStateHandler;

import com.sun.midp.content.CHManager;

import com.sun.midp.util.Properties;

import com.sun.midp.configurator.Constants;

/**
 * This class manages the persistent data for MIDlet suites.
 * <P>
 * Each installed package is uniquely identified by a unique ID.
 * Only suites installed or updated using this API appear
 * in the list of known suites.
 */
public class MIDletSuiteStorage {

    /** This class has a different security domain than the MIDlet suite. */
    private static SecurityToken classSecurityToken;

    /** This is the master storage object to synchronize all accesses */
    private static MIDletSuiteStorage masterStorage;

    /**
     * Initializes the security token for this class, so it can
     * perform actions that a normal MIDlet Suite cannot.
     *
     * @param token security token for this class.
     */
    public static void initSecurityToken(SecurityToken token) {
        if (classSecurityToken != null) {
            return;
        }

        classSecurityToken = token;
        MIDletSuiteImpl.initSecurityToken(classSecurityToken);
    }

    /**
     * Returns a reference to the singleton MIDlet suite storage object.
     *
     * @return the storage reference
     *
     * @exception SecurityException if the caller does not have permission
     *   to install software
     */
    public static MIDletSuiteStorage getMIDletSuiteStorage()
            throws SecurityException {
        MIDletSuite midletSuite =
            MIDletStateHandler.getMidletStateHandler().getMIDletSuite();

        if (midletSuite == null) {
            throw new
                IllegalStateException("This method can't be called before " +
                                      "a suite is started.");
        }

        midletSuite.checkIfPermissionAllowed(Permissions.AMS);

        return getMasterStorage();
    }

    /**
     * Returns a reference to the singleton MIDlet suite storage object.
     *
     * @param securityToken security token of the calling class
     *
     * @return the storage reference
     *
     * @exception SecurityException if the caller does not have permission
     *   to manage midlets
     */
    public static MIDletSuiteStorage getMIDletSuiteStorage(
           SecurityToken securityToken) throws SecurityException {
        securityToken.checkIfPermissionAllowed(Permissions.AMS);

        return getMasterStorage();
    }


    /**
     * Returns a reference to the singleton storage object.
     *
     * @return the storage reference
     */
    private static MIDletSuiteStorage getMasterStorage() {
        if (masterStorage == null) {
            masterStorage = new MIDletSuiteStorage();
        }

        return masterStorage;
    }

    /**
     * Private constructor to prevent outside instantiation.
     */
    private MIDletSuiteStorage() {
    }
    
    /**
     * Gets the MIDlet Suite from storage, and selects one midlet to be run.
     *
     * @param id the unique ID of the suite given
     *        by the installer when it was downloaded
     * @param update true is this MIDletSuite need to be updated
     *
     * @exception MIDletSuiteLockedException is thrown, if the MIDletSuite is
     * locked; MIDletSuiteCorruptedException is thrown if the MIDletSuite is 
     * corrupted
     *
     * @return MIDlet Suite reference
     */
    public synchronized MIDletSuiteImpl getMIDletSuite(String id,
            boolean update)
            throws MIDletSuiteLockedException, MIDletSuiteCorruptedException {
        if (!suiteExists(id)) {
            return null;
        }

        MIDletSuiteImpl.lockMIDletSuite(id, update);

        /*
         * save on startup time, get the properties at first getProperty call
         * and fill permissions on getPermission
         */
        return new MIDletSuiteImpl(id);
    }

    public synchronized String[] getMidletSuiteClassPath(String id) {
        String jarFile = getMidletSuiteJarPath(id);

        if (Constants.MONET_ENABLED && !id.equals("internal")) {
            String bunFile = getMidletSuiteAppImagePath(id);
            return new String[]{bunFile, jarFile};
        }        
        return new String[]{jarFile};
    }

    public synchronized native String getMidletSuiteAppImagePath(String id);

    /**
     * Get the class path for a suite.
     *
     * @param id unique ID of the suite
     *
     * @return class path or null if the suite does not exist
     */
    public synchronized native String getMidletSuiteJarPath(String id);

    /**
     * Gets the unique identifier of MIDlet suite.
     *
     * @param vendor name of the vendor that created the application, as
     *          given in a JAD file
     * @param name name of the suite, as given in a JAD file
     *
     * @return the platform-specific storage name of the application
     *         given by vendorName and appName or null if the suite does
     *         not exist
     */
    public native static String getSuiteID(String vendor, String name);


    // -------------- Installer related functionality ---------------

    /**
     * Get the installation information of a suite.
     *
     * @param midletSuite Suite object
     *
     * @return installation information
     *
     * @exception IOException if an the information cannot be read
     */
    InstallInfo getInstallInfo(MIDletSuiteImpl midletSuite)
            throws IOException {
        return midletSuite.getInstallInfo();
    }

    /**
     * Tells if a suite exists.
     *
     * @param id ID of a suite
     *
     * @return true if a suite of the given storage name
     *          already exists on the system
     *
     * @exception MIDletSuiteCorruptedException is thrown if the 
     * MIDletSuite is corrupted
     */
    public native boolean suiteExists(String id) 
        throws MIDletSuiteCorruptedException;

    /**
     * Returns a unique identifier of MIDlet suite.
     * Constructed from the combination
     * of the values of the <code>MIDlet-Name</code> and
     * <code>MIDlet-Vendor</code> attributes.
     *
     * @param vendor name of the vendor that created the application, as
     *          given in a JAD file
     * @param name name of the suite, as given in a JAD file
     *
     * @return the platform-specific storage name of the application
     *          given by vendorName and appName
     */
    public native String createSuiteID(String vendor, String name);

    /**
     * Stores or updates a suite.
     *
     * @param id unique ID of the suite
     * @param jadUrl where the JAD came from, can be null
     * @param jadProps properties the JAD
     * @param jarUrl where the JAR came from
     * @param jarFilename the downloaded JAR
     * @param manifestProps properties of the manifest
     * @param authPath if signed, the authorization path starting with the
     *        most trusted authority
     * @param domain security domain of the suite
     * @param trusted true if suite is trusted
     * @param permissions permissions for the suite
     * @param pushInterruptSetting push interrupt setting for the suite
     * @param pushOptions user options for push interrupts
     * @param verifyHash hash value of the suite with preverified classes
     *
     * @exception IOException is thrown, if an I/O error occurs during
     * storing the suite
     * @exception MIDletSuiteLockedException is thrown, if the MIDletSuite is
     * locked
     *
     */
    public synchronized void storeSuite(String id, String jadUrl,
            Properties jadProps, String jarUrl, String jarFilename,
            Properties manifestProps, String[] authPath, String domain,
            boolean trusted, byte[] permissions, byte pushInterruptSetting,
            int pushOptions, byte[] verifyHash)
            throws IOException, MIDletSuiteLockedException {
        /*
         * Convert the property args to String arrays to save
         * creating the native KNI code to access the object.
         */

        String[] jadProperties = getPropertiesStrings(jadProps);
        String[] jarProperties = getPropertiesStrings(manifestProps);

        nativeStoreSuite(id, jadUrl, jadProperties, jarUrl, jarFilename,
            jarProperties, authPath, domain, trusted, permissions,
            pushInterruptSetting, pushOptions, verifyHash);
    }

    /**
     * Disables a suite given its suite ID.
     * <p>
     * The method does not stop the suite if is in use. However any future
     * attepts to run a MIDlet from this suite while disabled should fail.
     *
     * @param id suite ID for the installed package
     *
     * @exception IllegalArgumentException if the suite cannot be found
     * @exception MIDletSuiteLockedException is thrown, if the MIDletSuite is
     * locked for updating
     */
    public native void disable(String id) throws MIDletSuiteLockedException;

    /**
     * Enables a suite given its suite ID.
     * <p>
     * The method does update an suites that are currently loaded for
     * settings or of application management purposes.
     *
     * @param id suite ID for the installed package
     *
     * @exception IllegalArgumentException if the suite cannot be found
     * @exception MIDletSuiteLockedException is thrown, if the MIDletSuite is
     * locked for updating
     */
    public native void enable(String id) throws MIDletSuiteLockedException;

    /**
     * Removes a software package given its suite ID.
     * The content handler manager is called to remove any registrations,
     * if any.
     * <p>
     * If the component is in use it must continue to be available
     * to the other components that are using it.  The resources it
     * consumes must not be released until it is not in use.
     *
     * @param id suite ID for the installed package
     *
     * @exception IllegalArgumentException if the suite cannot be found
     * @exception MIDletSuiteLockedException is thrown, if the MIDletSuite is
     * locked
     */
    public void remove(String id) throws MIDletSuiteLockedException {
        remove0(id);

        /*
         * If no exception occurs,
         * remove the content handler registrations, if any.
         */
        CHManager.getManager(classSecurityToken).uninstall(id);
    }

    /**
     * Stores or updates a suite.
     *
     * @param id unique ID of the suite
     * @param jadUrl where the JAD came from, can be null
     * @param jadProps properties the JAD as an array of strings in
     *        key/value pair order, can be null if jadUrl is null
     * @param jarUrl where the JAR came from
     * @param jarFilename the downloaded JAR
     * @param manifestProps properties of the manifest as an array of strings
     *        in key/value pair order
     * @param authPath if signed, the authorization path starting with the
     *        most trusted authority
     * @param domain security domain of the suite
     * @param trusted true if suite is trusted
     * @param permissions permissions for the suite
     * @param pushInterruptSetting push interrupt setting for the suite
     * @param pushOptions user options for push interrupts
     * @param verifyHash hash value of the suite with preverified classes
     *
     * @exception IOException is thrown, if an I/O error occurs during
     * storing the suite
     * @exception MIDletSuiteLockedException is thrown, if the MIDletSuite is
     * locked
     */
    private native void nativeStoreSuite(String id, String jadUrl,
        String[] jadProps, String jarUrl, String jarFilename,
        String[] manifestProps, String[] authPath, String domain,
        boolean trusted, byte[] permissions, byte pushInterruptSetting,
        int pushOptions, byte[] verifyHash) throws IOException,
        MIDletSuiteLockedException;

    /**
     * Native remove of a software package given its suite ID.
     * <p>
     * If the component is in use it must continue to be available
     * to the other components that are using it.  The resources it
     * consumes must not be released until it is not in use.
     *
     * @param id suite ID for the installed package
     *
     * @exception IllegalArgumentException if the suite cannot be found
     * @exception MIDletSuiteLockedException is thrown, if the MIDletSuite is
     * locked
     */
    private native void remove0(String id) throws MIDletSuiteLockedException;

    /**
     * Fill plain array with properties key/value String pairs.
     * It's needed to simplify properties using in a native code.
     *
     * @param props properties to get Strings from
     *
     * @return array of Strings filled with property key/value pairs
     */
    static String[] getPropertiesStrings(Properties props) {
        if (props != null) {
            int size = props.size();
            String[] res = new String[size * 2];
            for (int i = 0, j = 0; i < size; i++) {
                res[j++] = props.getKeyAt(i);
                res[j++] = props.getValueAt(i);
            }
            return res;
        } else return null;
    }

    // ------------ Graphical App Manager ------------------


    /**
     * Saves any of the settings (security or others) that the user may have
     * changed.
     *
     * @param id ID of the suite
     * @param pushInterruptSetting push interrupt setting
     * @param pushOptions push options
     * @param permissions security permissions for the suite
     *
     * @exception IOException if an error happens while writing
     */
    public void saveSuiteSettings(String id,
            byte pushInterruptSetting, int pushOptions, byte[] permissions)
		throws IOException {
        SuiteSettings settings = new SuiteSettings(id);
        settings.setPushInterruptSetting(pushInterruptSetting);
        settings.setPushOptions(pushOptions);
        settings.setPermissions(permissions);
        settings.save();
    }

    /**
     * Gets the amount of storage on the device that this suite is using.
     * This includes the JAD, JAR, management data, and RMS.
     *
     * @param id ID of a MIDlet suite
     *
     * @return number of bytes of storage the suite is using
     */
    public native int getStorageUsed(String id);

    /**
     * List all installed software packages by storage name.
     *
     * @return an array of Strings of the storage names for the
     *     installed packages
     * @exception SecurityException if the caller does not have permission
     *     to see what software is installed
     */
    public synchronized String[] getListOfSuites() {
        String[] array = new String[getNumberOfSuites()];

        if (array.length > 0) {
            getSuiteList(array);
        }

        return array;
    }

    /**
     * Get the number of installed of MIDlet suites.
     *
     * @return the number of installed suites
     */
    private native int getNumberOfSuites();

    /**
     * Retrieves the list of MIDlet suites and store them into a Vector
     * object. Each element in the Vector is the storage name
     * of an installed application.
     *
     * @param suites an empty array of suite IDs to fill, call
     *     getNumberOfSuites to know how big to make the array
     */
    private native void getSuiteList(String[] suites);
}
