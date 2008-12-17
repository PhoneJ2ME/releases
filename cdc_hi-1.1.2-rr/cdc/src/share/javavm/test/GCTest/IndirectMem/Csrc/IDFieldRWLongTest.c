/*
 * @(#)IDFieldRWLongTest.c	1.8 06/10/10
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

#include "javavm/export/jvm.h"
#include "javavm/export/jni.h"
#include "native/common/jni_util.h"
#include "javavm/include/interpreter.h"
#include "javavm/include/gc_common.h"
#include "javavm/include/porting/threads.h"
#include "javavm/include/porting/time.h"
#include "javavm/include/porting/int.h"
#include "javavm/include/porting/doubleword.h"
#include "javavm/include/clib.h"
#include "javavm/include/indirectmem.h"
#include "javavm/include/directmem.h"
#include "javavm/include/globalroots.h"
#include "javavm/include/localroots.h"
#include "javavm/include/preloader.h"
#include "javavm/include/common_exceptions.h"
#include "generated/offsets/java_lang_Integer.h"
#include "generated/offsets/java_lang_Long.h"
#include "generated/offsets/java_lang_Class.h"
#include "generated/offsets/java_lang_Thread.h"
#include "generated/offsets/java_lang_Throwable.h"
#include "generated/jni/java_lang_reflect_Modifier.h"
#include <stdio.h>
#include "IDFieldRWLongTest.h"

JNIEXPORT jlongArray JNICALL 
Java_IDFieldRWLongTest_nGetValues (JNIEnv *env, jobject obj) 
{
   jint i, j;
   jint numFields = 0;
   jlong var;
   jclass clazz;
   jlongArray longArray;
   CVMFieldBlock* fb;
   CVMClassBlock *cb;
   CVMExecEnv *ee;

   ee = CVMjniEnv2ExecEnv(env);

   clazz = (*env)->GetObjectClass(env, obj);

   cb = CVMgcSafeClassRef2ClassBlock(ee, clazz);

   for(i=0; i< CVMcbFieldCount(cb); i++) {
      fb = CVMcbFieldSlot(cb, i);
      if(!CVMfbIs(fb, STATIC))
         ++numFields;
   }

   longArray = (*env)->NewLongArray(env, numFields);

   CVMD_gcUnsafeExec( ee, {
      CVMD_gcSafeExec(ee, {
         jint objOffset;

         for(i=0, j=0; i< CVMcbFieldCount(cb); i++) {
            fb = CVMcbFieldSlot(cb, i);

            if(!CVMfbIs(fb, STATIC)) {
               objOffset = CVMfbOffset(fb);
               CVMID_fieldReadLong(ee, (CVMObjectICell*)obj, objOffset, var);
               CVMID_arrayWriteLong(ee, (CVMArrayOfLongICell*) longArray, i, var);
               ++j;
            }
         }
	
      });
   });

   return longArray;
}

JNIEXPORT void JNICALL
Java_IDFieldRWLongTest_nSetValues(JNIEnv *env, jobject obj, 
                       jlong v1, jlong v2, jlong v3)
{
   jint i, numFields = 0;
   jclass clazz;
   CVMFieldBlock* fb;
   CVMClassBlock *cb;
   CVMExecEnv *ee;

   ee = CVMjniEnv2ExecEnv(env);

   clazz = (*env)->GetObjectClass(env, obj);

   cb = CVMgcSafeClassRef2ClassBlock(ee, clazz);

   CVMD_gcUnsafeExec( ee, {
      int objOffset[CVMcbFieldCount(cb)];

      for(i=0; i< CVMcbFieldCount(cb); i++) {
           fb = CVMcbFieldSlot(cb, i);
           if(!CVMfbIs(fb, STATIC))
              objOffset[numFields++] = CVMfbOffset(fb);
      }

      CVMD_gcSafeExec(ee, {
         CVMID_fieldWriteLong(ee, (CVMObjectICell*) obj, objOffset[0], v1);
         CVMID_fieldWriteLong(ee, (CVMObjectICell*) obj, objOffset[1], v2);
         CVMID_fieldWriteLong(ee, (CVMObjectICell*) obj, objOffset[2], v3);

      });
   } );
}
