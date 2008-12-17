/*
 * @(#)ThreadGroupReferenceImpl.c	1.22 06/10/25
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
 */

#include "util.h"
#include "ThreadGroupReferenceImpl.h"
#include "inStream.h"
#include "outStream.h"

static jboolean 
name(PacketInputStream *in, PacketOutputStream *out) 
{
    JNIEnv *env;
    jthreadGroup group;
    
    env = getEnv();
    
    group = inStream_readThreadGroupRef(env, in);
    if (inStream_error(in)) {
        return JNI_TRUE;
    }

    WITH_LOCAL_REFS(env, 5) {

        jvmtiThreadGroupInfo info;

        (void)memset(&info, 0, sizeof(info));
        threadGroupInfo(group, &info);
        (void)outStream_writeString(out, info.name);
        if ( info.name != NULL )
            jvmtiDeallocate(info.name);
    
    } END_WITH_LOCAL_REFS(env);
    
    return JNI_TRUE;
}

static jboolean 
parent(PacketInputStream *in, PacketOutputStream *out) 
{
    JNIEnv *env;
    jthreadGroup group;
    
    env = getEnv();
    
    group = inStream_readThreadGroupRef(env, in);
    if (inStream_error(in)) {
        return JNI_TRUE;
    }

    WITH_LOCAL_REFS(env, 5) {

        jvmtiThreadGroupInfo info;
        
        (void)memset(&info, 0, sizeof(info));
        threadGroupInfo(group, &info);
        (void)outStream_writeObjectRef(env, out, info.parent);
        if ( info.name != NULL )
            jvmtiDeallocate(info.name);

    } END_WITH_LOCAL_REFS(env);
    
    return JNI_TRUE;
}

static jboolean 
children(PacketInputStream *in, PacketOutputStream *out) 
{
     JNIEnv *env;
     jthreadGroup group;
     
     env = getEnv();
    
     group = inStream_readThreadGroupRef(env, in);
     if (inStream_error(in)) {
         return JNI_TRUE;
     }
 
     WITH_LOCAL_REFS(env, 5) {
     
         jvmtiError error;
         jint threadCount;
         jint groupCount;
         jthread *theThreads;
         jthread *theGroups;
         
         error = JVMTI_FUNC_PTR(gdata->jvmti,GetThreadGroupChildren)(gdata->jvmti, group,
                                              &threadCount,&theThreads,
                                              &groupCount, &theGroups);
         if (error != JVMTI_ERROR_NONE) {
             outStream_setError(out, map2jdwpError(error));
         } else {

             int i;
             
             /* Squish out all of the debugger-spawned threads */
             threadCount = filterDebugThreads(theThreads, threadCount);
          
             (void)outStream_writeInt(out, threadCount);
             for (i = 0; i < threadCount; i++) {
               outStream_writeObjectRef(env, out, theThreads[i]);
             }
             (void)outStream_writeInt(out, groupCount);
             for (i = 0; i < groupCount; i++) {
               outStream_writeObjectRef(env, out, theGroups[i]);
             }

             jvmtiDeallocate(theGroups);
             jvmtiDeallocate(theThreads);
         }

     } END_WITH_LOCAL_REFS(env);

     return JNI_TRUE;
}

void *ThreadGroupReference_Cmds[] = { (void *)3,
                                      (void *)name,
                                      (void *)parent,
                                      (void *)children };

