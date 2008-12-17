/*
 * @(#)commonRef.h	1.17 06/10/25
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

#ifndef JDWP_COMMONREF_H
#define JDWP_COMMONREF_H

void commonRef_initialize(void);            
void commonRef_reset(JNIEnv *env);            

jlong commonRef_refToID(JNIEnv *env, jobject ref);
jobject commonRef_idToRef(JNIEnv *env, jlong id);
void commonRef_idToRef_delete(JNIEnv *env, jobject ref);
jvmtiError commonRef_pin(jlong id);
jvmtiError commonRef_unpin(jlong id);
void commonRef_releaseMultiple(JNIEnv *env, jlong id, jint refCount);
void commonRef_release(JNIEnv *env, jlong id);
void commonRef_compact(void);

void commonRef_lock(void);
void commonRef_unlock(void);

#endif

