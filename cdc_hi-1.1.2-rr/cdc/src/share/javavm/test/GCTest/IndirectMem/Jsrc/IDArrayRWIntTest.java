/*
 * @(#)IDArrayRWIntTest.java	1.6 06/10/10
 *
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

import java.util.Random;

public class IDArrayRWIntTest extends Thread {

   private int [] privateArray;

   public IDArrayRWIntTest(int num) {
      privateArray = new int[num];
   }

   private native void nSetArray(int [] dstArray, int [] srcArray, int arrayLength);

   public void setArray(int [] intArray) {
	this.nSetArray(this.privateArray, intArray, getArrayLength());
   }

   public int [] getArray() {
      int arrLength = getArrayLength();
      int [] intArray = new int[arrLength];

      for(int i=0; i<arrLength; i++)
         intArray[i] = privateArray[i];

       return intArray;
   }

   public int getArrayLength() {
	return this.privateArray.length;
   }

   public void run() {
      int arrayLength;
      boolean pass=true;
      Random rd = new Random();

      arrayLength = getArrayLength();

      int [] inArray = new int[arrayLength];
      for(int i=0; i<arrayLength; i++)
         inArray[i] = rd.nextInt();

      setArray(inArray);
      int [] outArray = getArray();

      for(int i=0; i<arrayLength; i++) {
         if(outArray[i] != inArray[i]) {
            pass = false;       
            break;
         }
      }

      System.out.println();

      if(pass)
         System.out.println("PASS: IDArrayRWIntTest, Data written and read were same");
      else
         System.out.println("FAIL: IDArrayRWIntTest, Data written and read were not same");
   }
   
   public static void main(String args[]) {
      int numGcThreads = 10;
      Random rd = new Random();

      IDArrayRWIntTest test = new IDArrayRWIntTest(rd.nextInt(100));

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
