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

package sun.awt.image;

import java.awt.image.ImageConsumer;

class ImageConsumerQueue {
    ImageConsumerQueue next;
    ImageConsumer consumer;
    boolean interested;
    Object securityContext;
    boolean secure;
    static ImageConsumerQueue removeConsumer(ImageConsumerQueue cqbase,
        ImageConsumer ic,
        boolean stillinterested) {
        ImageConsumerQueue cqprev = null;
        for (ImageConsumerQueue cq = cqbase; cq != null; cq = cq.next) {
            if (cq.consumer == ic) {
                if (cqprev == null) {
                    cqbase = cq.next;
                } else {
                    cqprev.next = cq.next;
                }
                cq.interested = stillinterested;
                break;
            }
            cqprev = cq;
        }
        return cqbase;
    }

    static boolean isConsumer(ImageConsumerQueue cqbase, ImageConsumer ic) {
        for (ImageConsumerQueue cq = cqbase; cq != null; cq = cq.next) {
            if (cq.consumer == ic) {
                return true;
            }
        }
        return false;
    }

    ImageConsumerQueue(InputStreamImageSource src, ImageConsumer ic) {
        consumer = ic;
        interested = true;
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            securityContext = security.getSecurityContext();
        } else {
            securityContext = null;
        }
    }

    public String toString() {
        return ("[" + consumer +
                ", " + (interested ? "" : "not ") + "interested" +
                (securityContext != null ? ", " + securityContext : "") +
                "]");
    }
}
