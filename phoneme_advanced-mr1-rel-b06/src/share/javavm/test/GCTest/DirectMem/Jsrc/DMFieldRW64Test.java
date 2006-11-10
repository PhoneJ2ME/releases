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

import java.util.Random;

public class DMFieldRW64Test extends Thread {

   public long publicVar;
   private long privateVar;
   protected long protectedVar;
   public static Object lock;

   public DMFieldRW64Test() {
      publicVar = 0;
      privateVar = 0;
      protectedVar = 0;
   }

   public native void nSetValues(long v1, long v2, long v3);

   public native long [] nGetValues();

   public long [] getValues() {
      long [] longArray = {publicVar, privateVar, protectedVar};
      return longArray;
   }

   public void run() {
      boolean pass=true;
      Random rd = new Random();
      long [] var = {rd.nextLong(), rd.nextLong(), rd.nextLong()};

      System.out.println();

      nSetValues(var[0], var[1], var[2]);

      System.out.println();

      long [] longArray1 = getValues();
      long [] longArray2 = nGetValues();

      if(longArray1.length == longArray2.length) {
         for(int i=0; i<longArray1.length; i++) {
            if(longArray1[i] != longArray2[i]) {
               pass = false;	
               break;
            }
         }
      }
      else {
         pass = false;
      }

      System.out.println();

      if(pass)
         System.out.println("PASS: DMFieldRW64Test, Data written and read were same");
      else
         System.out.println("FAIL: DMFieldRW64Test, Data written and read were not same");

   }

   public static void main(String args[]) {
      Object lck = new Object();

      DMFieldRW64Test.lock = lck;
      GcThread.lock = lck;

      GcThread gc = new GcThread();
      DMFieldRW64Test test = new DMFieldRW64Test();

      gc.setPriority(test.getPriority() + 1);
      gc.start();
      test.start();

      try {
         test.join();
      } catch (Exception e) {}

      gc.interrupt();
   }

}
