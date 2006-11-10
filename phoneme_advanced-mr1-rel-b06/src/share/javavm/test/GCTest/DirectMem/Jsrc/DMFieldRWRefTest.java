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

public class DMFieldRWRefTest extends Thread {

   private String publicString;
   private String protectedString;
   private String privateString;
   public static Object lock;

   public DMFieldRWRefTest() {
      publicString = null;
      protectedString = null;
      privateString = null;
   }

   public native void nSetStrings(String str1, String str2, String str3);

   public native String [] nGetStrings();

   public String [] getStrings() {
      String [] strArray = {publicString, protectedString, privateString};
      return strArray;
   }

   public void run () {
      boolean pass=true;
      Random rd = new Random();
      String [] str = {String.valueOf(rd.nextInt()), String.valueOf(rd.nextInt()),String.valueOf(rd.nextInt())};

      System.out.println();

      nSetStrings(str[0], str[1], str[2]);

      System.out.println();

      String [] strArray1 = getStrings();
      String [] strArray2 = nGetStrings();

      if(strArray1.length == strArray2.length) {
         for(int i=0; i<strArray1.length; i++) {
            if(strArray1[i].compareTo(strArray2[i]) != 0) {
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
         System.out.println("PASS: DMFieldRWRefTest, Data written and read were same");
      else
         System.out.println("FAIL: DMFieldRWRefTest, Data written and read were not same");

   }

   public static void main(String args[]) {
      Object lck = new Object();

      DMFieldRWRefTest.lock = lck;
      GcThread.lock = lck;

      GcThread gc = new GcThread();
      DMFieldRWRefTest test = new DMFieldRWRefTest();

      gc.setPriority(test.getPriority() + 1);
      gc.start();
      test.start();

      try {
         test.join();
      } catch (Exception e) {}

      gc.interrupt();
   }
}
