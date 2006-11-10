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

package com.sun.midp.appmanager;

import java.util.*;

import java.io.*;

import javax.microedition.io.*;

import javax.microedition.midlet.*;

import javax.microedition.lcdui.*;

import javax.microedition.rms.*;

import com.sun.midp.i18n.*;

import com.sun.midp.midlet.*;

import com.sun.midp.installer.*;

import com.sun.midp.midletsuite.*;

import com.sun.midp.security.*;

import com.sun.midp.log.*;

import com.sun.midp.main.*;

import com.sun.midp.io.j2me.storage.*;

import com.sun.midp.configurator.Constants;

/**
 * This is an implementation of the ApplicationManager interface
 * for the MVM mode of VM capable of running with 
 * more than 1 midlet concurrently.
 * 
 * Application manager controls midlet life cycle:
 *    - installs, updates and removes midlets/midlet suites
 *    - launches, moves to foreground and terminates midlets
 *    - displays info about a midlet/midlet suite
 *    - shuts down the AMS system
 */
public class MVMManager extends MIDlet 
    implements MIDletProxyListListener, ApplicationManager {

    /** Constant for the discovery application class name. */
    private static final String DISCOVERY_APP =
        "com.sun.midp.installer.DiscoveryApp";
    /** Constant for the graphical installer class name. */
    private static final String INSTALLER =
        "com.sun.midp.installer.GraphicalInstaller";
    /** Constant for the CA manager class name. */
    private static final String CA_MANAGER =
        "com.sun.midp.appmanager.CaManager";

    /** True until constructed for the first time. */
    private static boolean first = true;

    /** MIDlet Suite storage object. */
    private MIDletSuiteStorage midletSuiteStorage;

    /** Screen that displays all installed midlets and installer */
    private AppManagerUI appManagerUI;

    /** MIDlet proxy list reference. */
    private MIDletProxyList midletProxyList;

    /** UI to display error alerts. */
    private DisplayError displayError;


    /**
     * Create and initialize a new MVMManager MIDlet.
     */
    public MVMManager() {
        MIDletProxy thisMidlet;

        midletSuiteStorage = MIDletSuiteStorage.getMIDletSuiteStorage();

        /*
         * Listen to the MIDlet proxy list.
         * this allows us to notify the Application Selector 
         * of any changes whenever switch back to the AMS.
         */
        midletProxyList = MIDletProxyList.getMIDletProxyList();
        midletProxyList.addListener(this);

        // The proxy for this MIDlet may not have been create yet.
        for (; ; ) {
            thisMidlet = midletProxyList.findMIDletProxy("internal",
                         this.getClass().getName());
        
            if (thisMidlet != null) {
                break;
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException ie) {
                // ignore
            }
        }

        midletProxyList.setDisplayController(
            new MVMDisplayController(midletProxyList, thisMidlet));

        IndicatorManager.init(midletProxyList);

        GraphicalInstaller.initSettings();

	first = (getAppProperty("logo-displayed") == null);

        Display display = Display.getDisplay(this);
	displayError = new DisplayError(display);

	// AppManagerUI will be set to be current at the end of its constructor
        appManagerUI = new AppManagerUI(this, display, displayError, first);

        if (first) {
            first = false;
        }
    }

    // =================================================================
    // ---------- Operations that can be performed on Midlets ----------

    /**
     * Start app; there is nothing that needs to be done at start up.
     */
    public void startApp() {
    }

    /**
     * Pause; there are no resources that need to be released.
     */
    public void pauseApp() {
    }

    /**
     * Destroy midlet. Cleans up the resources used.
     *
     * @param unconditional is ignored; this object always
     * destroys itself when requested.
     */
    public void destroyApp(boolean unconditional) {
        /*
         * Save user settings such as currently selected MIDlet
         * This may not be needed since we are always running
         */
        GraphicalInstaller.saveSettings(null, "");

        // Ending the MIDlet ends all others.
        midletProxyList.shutdown();
    }

    // ==============================================================
    // ------ Implementation of the MIDletProxyListListener interface 

    /**
     * Called when a MIDlet is added to the list.
     *
     * @param midlet The proxy of the MIDlet being added
     */
    public void midletAdded(MIDletProxy midlet) {
        appManagerUI.notifyMidletStarted(midlet);
    }

    /**
     * Called when the state of a MIDlet in the list is updated.
     *
     * @param midlet The proxy of the MIDlet that was updated
     * @param fieldId code for which field of the proxy was updated
     */
    public void midletUpdated(MIDletProxy midlet, int fieldId) {
        appManagerUI.notifyMidletStateChanged(midlet);
    }

    /**
     * Called when a MIDlet is removed from the list.
     *
     * @param midlet The proxy of the removed MIDlet
     */
    public void midletRemoved(MIDletProxy midlet) {
        appManagerUI.notifyMidletExited(midlet);
    }

    /**
     * Called when error occurred while starting a MIDlet object.
     *
     * @param externalAppId ID assigned by the external application manager
     * @param suiteId Suite ID of the MIDlet
     * @param className Class name of the MIDlet
     * @param error start error code
     */
    public void midletStartError(int externalAppId, String suiteId,
				 String className, int error) {
        appManagerUI.notifyMidletStartError(suiteId, className, error);
    }

    // ==============================================================
    // ------ Implementation of the MIDletProxyListListener interface 

    /** Discover and install a suite. */
    public void installSuite() {
        try {
            MIDletSuiteLoader.execute("internal", DISCOVERY_APP,
                Resource.getString(ResourceConstants.INSTALL_APPLICATION));
        } catch (Exception ex) {
            displayError.showErrorAlert(Resource.getString(
                                  ResourceConstants.INSTALL_APPLICATION),
                              ex, null, null);
        }
    }

    /** Launch the CA manager. */
    public void launchCaManager() {
        try {
            MIDletSuiteLoader.execute("internal", CA_MANAGER,
                Resource.getString(ResourceConstants.CA_MANAGER_APP));
        } catch (Exception ex) {
            displayError.showErrorAlert(Resource.getString(
                ResourceConstants.CA_MANAGER_APP), ex, null, null);
        }
    }

    /**
     * Launches a suite.
     *
     * @param suiteInfo information for suite to launch
     * @param midletToRun class name of the MIDlet to launch
     */
    public void launchSuite(MIDletSuiteInfo suiteInfo, String midletToRun) {

        if (Constants.MEASURE_STARTUP) {
            System.err.println("Application Startup Time: Begin at "
                                +System.currentTimeMillis());
        }

        try {
            // Create an instance of the MIDlet class
            // All other initialization happens in MIDlet constructor
            MIDletSuiteLoader.execute(suiteInfo.id, midletToRun, null);
        } catch (Exception ex) {
            displayError.showErrorAlert(suiteInfo.displayName, ex, null, null);
        }
    }

    /**
     * Update a suite.
     *
     * @param suiteInfo information for suite to update
     */
    public void updateSuite(MIDletSuiteInfo suiteInfo) {
        /*
         * Setting arg 0 to "U" signals that arg 1 is a suite ID for updating.
         */
        try {
            MIDletSuiteLoader.executeWithArgs("internal", INSTALLER,
                                              suiteInfo.displayName, 
                                              "U", suiteInfo.id, null); 
        } catch (Exception ex) {
            displayError.showErrorAlert(suiteInfo.displayName, ex, null, null);
        }
    }

    /**
     * Shut down the system
     */
    public void shutDown() {
        midletProxyList.shutdown();
    }
   
    /**
     * Bring the midlet with the passed in midlet suite info to the 
     * foreground.
     * 
     * @param suiteInfo information for the midlet to be put to foreground
     */
    public void moveToForeground(MIDletSuiteInfo suiteInfo) {
        try {
            if (suiteInfo != null) {
                midletProxyList.setForegroundMIDlet(suiteInfo.proxy);
            }
            
        } catch (Exception ex) {
            displayError.showErrorAlert(suiteInfo.displayName, ex, null, null);
        }
    }


    /**
     * Exit the midlet with the passed in midlet suite info.
     * 
     * @param suiteInfo information for the midlet to be terminated
     */
    public void exitMidlet(MIDletSuiteInfo suiteInfo) {
        try {
            if (suiteInfo != null) {
                suiteInfo.proxy.destroyMidlet();
            }
            
        } catch (Exception ex) {
            displayError.showErrorAlert(suiteInfo.displayName, ex, null, null);
        }
    }

}
