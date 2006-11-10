/*
 * @(#)TestSlotFactory.java	1.5 05/10/07 @(#)
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

package com.sun.cardreader;

import com.sun.midp.i3test.TestCase;
import java.io.IOException;

/**
 * This test case tests basic SlotFactory functionality, primarily configuration
 * properties.
 */
public class TestSlotFactory extends TestCase {
    public static final int SLOT_COUNT = 1;

    /**
     * Test card devices configuration reading and device creation.
     */
    private void testInit() throws java.io.IOException, CardDeviceException {
        boolean stub_flag = false;
        
        try {
            SlotFactory.init();
        }
        catch (CardDeviceException e) {
            if (e.getMessage().equals("stub")) {
                stub_flag = true;
            } else {
                throw e;
            }
        }

        if (!stub_flag) {
            int deviceCount = SlotFactory.getCardDeviceCount();
            assertTrue(deviceCount == 1);
    
            int slotCount = SlotFactory.getCardSlotCount();
            assertTrue(slotCount == 1);
        } else {
            assertTrue(true);
        }
    }

    /**
     * Test card slot creation.
     */
    private void testSlotCreation() throws IOException, CardDeviceException {
        /* Placeholder */
        assertTrue(true);
    }
     
    /**
     * Run tests.
     */
    public void runTests() {
        try {
            declare("testInit");
            testInit();

            declare("testSlotCreation");
            testSlotCreation();
        }
        catch (Throwable t) {
            fail("" + t);
        }
    }
}
