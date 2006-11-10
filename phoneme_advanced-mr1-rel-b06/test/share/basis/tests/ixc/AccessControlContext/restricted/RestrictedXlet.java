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

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

import javax.microedition.xlet.Xlet;
import javax.microedition.xlet.XletContext;
import javax.microedition.xlet.ixc.IxcRegistry;


public class RestrictedXlet implements Xlet {
    public void initXlet(XletContext restrictedXletContext) {
        System.out.println("RestrictedXlet restrictedXletcontext = " + restrictedXletContext);

        // Get the unrestricted registry.  This Xlet was instantiated by the
        // Unrestricted Xlet.  The Unrestricted Xlet pass in an XletContext which
        // was created with a restrictive class loader.
        IxcRegistry restrictedRegistry = IxcRegistry.getRegistry(restrictedXletContext);

        // Bind into restricted IxcRegistry
        try {
            restrictedRegistry.bind("/restricted/Registry", restrictedRegistry);
        } catch(Exception e) {
            System.out.println("RestrictedXlet.initXlet Exception thrown binding into restrictedRegistry.");
            e.printStackTrace();
        }
    }

    public void destroyXlet(boolean unconditional) {
    }

    public void pauseXlet() {
    }

    public void startXlet() {
    }
}
