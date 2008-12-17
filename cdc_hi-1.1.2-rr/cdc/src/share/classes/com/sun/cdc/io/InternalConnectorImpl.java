/*
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
package com.sun.cdc.io;

import java.io.*;
import javax.microedition.io.*;
import sun.security.action.GetPropertyAction;

public class InternalConnectorImpl implements InternalConnector {
    protected ClassLoader protocolClassLoader;
    protected String classRoot;

    /*
     * The default for class root is <code>com.sun.cdc.io</code> and can be
     * replaced by setting the system property
     * <code>javax.microedition.io.Connector.protocolpath</code>.
     *
     * @return class root
     */
    protected String getClassRoot() {
        if (classRoot != null) {
            return classRoot;
        }
        
        String profileTemp = null;
        
        try {
            /*
             * Check to see if there is a property override for the dynamic
             * building of class root.
             */
            classRoot =(String)java.security.AccessController.doPrivileged( 
                new GetPropertyAction(
                    "javax.microedition.io.Connector.protocolpath"));
        } catch (Throwable t) {
            // do nothing
        }
        
        if (classRoot == null) {
            classRoot = "com.sun.cdc.io";
        }
        
        return classRoot;
    }
    
    protected ClassLoader getProtocolClassLoader() {
        if (protocolClassLoader != null) {
            return protocolClassLoader;
        }
        
        protocolClassLoader = this.getClass().getClassLoader();
        return protocolClassLoader;
    }

    /*
     * Create and open a Connection.
     * <p>
     * There are 2 ways to find a connection implementation.
     * 1. Get the class name from a system property generated in the form of:
     * <pre>
     *    j2me.{connection protocol}.protocol
     * </pre>
     * 2. Use the class root (see getClassRoot) and the connection
     * protocol to dynamically construct a class name in the the form of:
     * <pre>
     *   {class root}.j2me.{connection protocol}.Protocol
     * </pre>
     * The connection protocol is parsed from the <code>name</code> parameter
     * which takes the form of:
     * <pre>
     *   {connection protocol}:{protocol specific part}
     * </pre>
     * In order to avoid problems with illegal
     * class file names, all the '-' characters in the connection protocol
     * are automatically converted into '_' characters.
     * <p>
     * Additionally the protocol specific part is parsed from the name
     * parameter and passed to the connection's factory method.
     *
     * @param name             The URL for the connection
     * @param mode             The access mode
     * @param timeouts         A flag to indicate that the caller
     *                         wants timeout exceptions
     *
     * @return                 A new Connection object
     *
     * @exception IllegalArgumentException If a parameter is invalid.
     * @exception ConnectionNotFoundException If the target of the
     *   name cannot be found, or if the requested protocol type
     *   is not supported.
     * @exception IOException If some other kind of I/O error occurs.
     * @exception IllegalArgumentException If a parameter is invalid.
     */
    public Connection open(String name, int mode, boolean timeouts)
            throws IOException {
        /* Test for null argument */
        if (name == null) {
            throw new IllegalArgumentException("Null URL");
        }
        
        /* Look for : as in "http:", "file:", or whatever */
        int colon = name.indexOf(':');

        /* Test for null argument */
        if (colon < 1) {
            throw new IllegalArgumentException("no ':' in URL");
        }

        try {
            String protocol;

            /* Strip off the protocol name */
            protocol = name.substring(0, colon);

            /* Strip off the rest of the string */
            name = name.substring(colon + 1);

            /*
             * Convert all the '-' characters in the protocol
             * name to '_' characters (dashes are not allowed
             * in class names).  This operation creates garbage
             * only if the protocol name actually contains dashes
             */
            protocol = protocol.replace('-', '_');
            
            /*
             * Use the platform and protocol names to look up
             * a class to implement the connection
             */
            String className = getClassRoot() + "." + "j2me"+ "." + protocol +
               ".Protocol";

            Class clazz = Class.forName(className, true,
                                        getProtocolClassLoader());
            
            /* Construct a new instance of the protocol */
            ConnectionBaseInterface uc =
                (ConnectionBaseInterface)clazz.newInstance();
            
            /* Open the connection, and return it */
            return uc.openPrim(name, mode, timeouts);
        } catch (InstantiationException x) {
            throw new IOException(x.toString());
        } catch (IllegalAccessException x) {
            throw new IOException(x.toString());
        } catch (ClassCastException x) {
            throw new IOException(x.toString());
        } catch (ClassNotFoundException x) {
            throw new ConnectionNotFoundException(
                "The requested protocol does not exist " + name);
        }
    }
}

    
   
      
