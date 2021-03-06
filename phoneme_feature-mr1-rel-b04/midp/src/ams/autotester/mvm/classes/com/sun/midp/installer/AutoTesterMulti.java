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

package com.sun.midp.installer;

import java.io.*;

import java.util.*;

import com.sun.cldc.isolate.*;

import com.sun.midp.main.AmsUtil;

import javax.microedition.io.*;

import javax.microedition.lcdui.*;

import javax.microedition.midlet.*;

import javax.microedition.rms.*;

import com.sun.midp.io.j2me.storage.*;

import com.sun.midp.i18n.Resource;
import com.sun.midp.i18n.ResourceConstants;

import com.sun.midp.midlet.*;

import com.sun.midp.midletsuite.*;

import com.sun.midp.security.*;

import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;


/**
 * Fetches a list of URLs pointing to test suites to install, then,
 * for each specified test suite in parallel, installs/updates the suite,
 * runs the first MIDlet in the suite in a loop specified number of iterations
 * or until the new version of the suite is not found, then removes the suite.
 * <p>
 * The MIDlet uses these application properties as arguments: </p>
 * <ol>
 *   <li>arg-0: URL for html page with links to test suites. This html page
 *    looks like this:
 *      <a href="http://localhost/suite1.jad">Suite1</a>
 *      <a href="http://localhost/suite2.jad">Suite2</a>
 *   <li>arg-1: Used to override the default domain used when installing
 *    an unsigned suite. The default is maximum to allow the runtime API tests
 *    be performed automatically without tester interaction.
 *    <li>arg-2: Integer number, specifying how many iterations to run
 *    the suite. If argument is not given or less then zero, then suite
 *    will be run until the new version of the suite is not found.
 * </ol>
 * <p>
 * If arg-0 is not given then a form will be used to query the tester for
 * the arguments.</p>
 */
public class AutoTesterMulti extends AutoTesterBase
        implements AutoTesterInterface {
    /**
     * Info about suites to install
     */
    Vector installList = new Vector();

    /**
     * Create and initialize a new auto tester MIDlet.
     */
    public AutoTesterMulti() {
        super();

        if (url != null) {
            startBackgroundTester();
        } else if (restoreSession()) {
            // continuation of a previous session
            startBackgroundTester();
        } else {
            /**
             * No URL has been provided, ask the user.
             *
             * commandAction will subsequently call startBackgroundTester.
             */
            getUrl();
        }
    }

    /** Run the installer. */
    public void run() {
        installAndPerformTests(midletSuiteStorage, installer, url);
    }

    /**
     * Restore the data from the last session, since this version of the
     * autotester does not have sessions it just returns false.
     *
     * @return true if there was data saved from the last session
     */
    public boolean restoreSession() {
        return false;
    }

    /**
     * Get list of test suites, for each suite start a thread
     * that performs testing, wait until all threads have finished.
     *
     * @param inp_storage MIDletSuiteStorage object
     * @param inp_installer Installer object
     * @param inp_url URL for the html page with links to suites
     */
    public void installAndPerformTests(
        MIDletSuiteStorage inp_storage,
        Installer inp_installer, String inp_url) {

        fetchInstallList(url);
        int totalSuites = installList.size();
        if (totalSuites > 0) {
            Thread[] threads = new Thread[totalSuites];

            // create threads
            for (int i = 0; i < totalSuites; i++) {
                SuiteDownloadInfo suite =
                    (SuiteDownloadInfo)installList.elementAt(i);
                threads[i] = new Thread(new AutoTesterRunner(
                    suite.url, inp_storage, inp_installer));
            }

            // start threads
            for (int i = 0; i < totalSuites; i++) {
                threads[i].start();
            }

            // wait for threads to finish
            for (int i = 0; i < totalSuites; i++) {
                try {
                    threads[i].join();
                } catch (Exception ex) {
                    // just ignore
                }
            }

            notifyDestroyed();
            return;
        }
    }


    /**
     * Go to given URL, fetch and parse html page with links
     * to tests suites. If there was error while fetching
     * or parsing, display an alert.
     *
     * @param url URL for html page with links to suites
     */
    private void fetchInstallList(String url) {
        StreamConnection conn = null;
        InputStreamReader in = null;
        String errorMessage;

        try {
            conn = (StreamConnection)Connector.open(url, Connector.READ);
            in = new InputStreamReader(conn.openInputStream());
            try {
                installList = SuiteDownloadInfo.getDownloadInfoFromPage(in);
                if (installList.size() > 0) {
                    return;
                }
                errorMessage = Resource.getString(
                        ResourceConstants.AMS_DISC_APP_CHECK_URL_MSG);
            } catch (IllegalArgumentException ex) {
                errorMessage = Resource.getString(
                        ResourceConstants.AMS_DISC_APP_URL_FORMAT_MSG);
            } catch (Exception ex) {
                errorMessage = ex.getMessage();
            }
        } catch (Exception ex) {
            errorMessage = Resource.getString(
                    ResourceConstants.AMS_DISC_APP_CONN_FAILED_MSG);
        } finally {
            try {
                conn.close();
                in.close();
            } catch (Exception e) {
                if (Logging.REPORT_LEVEL <= Logging.WARNING) {
                    Logging.report(Logging.WARNING, LogChannels.LC_AMS,
                            "close threw an Exception");
                }
            }
        }

        Alert a = new Alert(Resource.getString(ResourceConstants.ERROR),
                errorMessage, null, AlertType.ERROR);
        a.setTimeout(Alert.FOREVER);
        display.setCurrent(a);
    }


    /**
     * An runnable class that runs specified test suite in a loop.
     */
    private class AutoTesterRunner implements Runnable {
        /**
         * Number of retries to fetch a suite from given URL before exiting
         */
        private final static int MAX_RETRIES = 20;

        /**
         * Time to wait before trying to fetch a suite again
         */
        private final static int RETRY_WAIT_TIME = 30000;

        /**
         * URL for the test suite
         */
        private String url;

        /**
         * MIDletSuiteStorage object
         */
        MIDletSuiteStorage storage;

        /**
         * Installer object
         */
        Installer installer;


        /**
         * Constructor.
         *
         * @param theUrl URL for the test suite
         * @param theStorage MIDletSuiteStorage object to use
         * @param theInstaller Installer object to use
         */
        private AutoTesterRunner(String theUrl,
                MIDletSuiteStorage theStorage, Installer theInstaller) {
            url = theUrl;
            storage = theStorage;
            installer = theInstaller;
        }

        /**
         * In a loop, install/update the suite and run it until
         * new version of the suite is not found.
         */
        public void run() {
            // when installing suite for the first time,
            // do not retry because URL may be wrong and
            // we want immediately tell user about it.
            boolean retry = false;
            boolean hasSuite = true;

            String suiteID = null;
            String lastSuiteID = null;

            MIDletInfo midletInfo;
            Isolate testIsolate;

            for (; loopCount != 0 && hasSuite; ) {
                suiteID = installSuite(retry);
                if (suiteID != null) {
                    midletInfo = getFirstMIDletOfSuite(suiteID, storage);
                    testIsolate = AmsUtil.startMidletInNewIsolate(suiteID,
                            midletInfo.classname, midletInfo.name, null,
                            null, null);

                    testIsolate.waitForExit();

                    if (loopCount > 0) {
                        loopCount -= 1;
                    }

                    lastSuiteID = suiteID;

                    // suite has been found, so URL isn't wrong.
                    // next time if there is no suite, do retries.
                    retry = true;
                } else {
                    hasSuite = false;
                }
            }

            if (midletSuiteStorage != null && lastSuiteID != null) {
                try {
                    midletSuiteStorage.remove(lastSuiteID);
                } catch (Throwable ex) {
                    // ignore
                }
            }
        }

        /**
         * Install the suite.
         *
         * @param retry if true, do a number of retries to
         * install a suite in case it hasn't been found.
         *
         * @return suiteID if the suite has been installed
         */
        private String installSuite(boolean retry) {
            int maxRetries = retry? MAX_RETRIES : 1;
            int retryCount = 0;
            String suiteID = null;

            for (; retryCount < maxRetries; ++retryCount) {
                try {
                    synchronized (installer) {
                        // force an overwrite and remove the RMS data
                        suiteID = installer.installJad(url, true, true,
                                installListener);
                    }
                    return suiteID;

                } catch (Throwable t) {
                    // if retrying, just ignore exception and wait,
                    // otherwise display error message to user.
                    if (retry) {
                        try {
                            Thread.sleep(RETRY_WAIT_TIME);
                        } catch (Exception ex) {
                        }
                    } else {
                        handleInstallerException(suiteID, t);
                    }
                }
            }

            return null;
        }
    }
}

