/*
 * @(#)threadControl.h	1.40 06/10/25
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

#ifndef JDWP_THREADCONTROL_H
#define JDWP_THREADCONTROL_H

#include "stepControl.h"
#include "invoker.h"
#include "bag.h"

void threadControl_initialize(void);
void threadControl_reset(void);
void threadControl_detachInvokes(void);

void threadControl_onHook(void);
void threadControl_onConnect(void);
void threadControl_onDisconnect(void);
void threadControl_joinAllDebugThreads(void);

jvmtiError threadControl_popFrames(jthread thread, FrameNumber fnum);

struct bag *threadControl_onEventHandlerEntry(jbyte sessionID, 
                  EventIndex ei, jthread thread, jobject currentException);
void threadControl_onEventHandlerExit(EventIndex ei, jthread thread, struct bag *);


jvmtiError threadControl_suspendThread(jthread thread, jboolean deferred);
jvmtiError threadControl_resumeThread(jthread thread, jboolean do_unblock);
jvmtiError threadControl_suspendCount(jthread thread, jint *count);

jvmtiError threadControl_suspendAll(void);
jvmtiError threadControl_resumeAll(void);

StepRequest *threadControl_getStepRequest(jthread);
InvokeRequest *threadControl_getInvokeRequest(jthread);

jboolean threadControl_isDebugThread(jthread thread);
jvmtiError threadControl_addDebugThread(jthread thread);

jvmtiError threadControl_applicationThreadStatus(jthread thread, jdwpThreadStatus *pstatus, jint *suspendStatus);
jvmtiError threadControl_interrupt(jthread thread);
jvmtiError threadControl_stop(jthread thread, jobject throwable);

jvmtiError threadControl_setEventMode(jvmtiEventMode mode, EventIndex ei, jthread thread);
jvmtiEventMode threadControl_getInstructionStepMode(jthread thread);

jthread threadControl_currentThread(void);
void threadControl_setPendingInterrupt(jthread thread);
void threadControl_clearCLEInfo(JNIEnv *env, jthread thread);
jboolean threadControl_cmpCLEInfo(JNIEnv *env, jthread thread, jclass clazz,
                                  jmethodID method, jlocation location);
void threadControl_saveCLEInfo(JNIEnv *env, jthread thread, EventIndex ei,
                               jclass clazz, jmethodID method,
                               jlocation location);
jlong threadControl_getFrameGeneration(jthread thread);

#endif

