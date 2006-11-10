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

public class IDArrayRWLongTest extends Thread {

   private long [] privateArray;
   public static Object lock;

   public IDArrayRWLongTest(int num) {
      privateArray = new long[num];
   }

   private native void nSetArray(long [] dstArray, long [] srcArray, int arrayLength);

   public void setArray(long [] longArray) {
	this.nSetArray(this.privateArray, longArray, getArrayLength());
   }

   public long [] getArray() {
      int arrLength = getArrayLength();
      long [] longArray = new long[arrLength];

      for(int i=0; i<arrLength; i++)
         longArray[i] = privateArray[i];

       return longArray;
   }

   public int getArrayLength() {
	return this.privateArray.length;
   }

   public void run() {
      int arrayLength;
      boolean pass=true;
      Random rd = new Random();

      arrayLength = getArrayLength();

      long [] inArray = new long[arrayLength];
      for(int i=0; i<arrayLength; i++)
         inArray[i] = rd.nextLong();

      setArray(inArray);
      long [] outArray = getArray();

      for(int i=0; i<arrayLength; i++) {
         if(outArray[i] != inArray[i]) {
            pass = false;       
            break;
         }
      }

      System.out.println();

      if(pass)
         System.out.println("PASS: IDArrayRWLongTest, Data written and read were same");
      else
         System.out.println("FAIL: IDArrayRWLongTest, Data written and read were not same");
   }
   
   public static void main(String args[]) {
      int numGcThreads = 10;
      Random rd = new Random();

      IDArrayRWLongTest test = new IDArrayRWLongTest(rd.nextInt(100));

      GcThread.sleepCount = numGcThreads;
      GcThread [] gc = new GcThread[numGcThreads];
      for(int i=0; i<gc.length; i++) {
         gc[i] = new GcThread();
         gc[i].start();
      }

      test.start();

      try {
         test.join();
      } catch (Exception e) {}

      for(int i=0; i<gc.length; i++) {
         gc[i].interrupt();
      }

      try {
         for(int i=0; i<gc.length; i++) {
            gc[i].join();
         }
      } catch (Exception e) {}
   }

}
