/*
 * @(#)eventHelper.h	1.24 06/10/25
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

#ifndef JDWP_EVENTHELPER_H
#define JDWP_EVENTHELPER_H

#include "bag.h"
#include "invoker.h"

void eventHelper_initialize(jbyte sessionID);
void eventHelper_shutdown(void);
void eventHelper_reset(jbyte sessionID);
struct bag *eventHelper_createEventBag(void);

void eventHelper_recordEvent(EventInfo *evinfo, jint id, 
                             jbyte suspendPolicy, struct bag *eventBag);
void eventHelper_recordClassUnload(jint id, char *signature, struct bag *eventBag);
void eventHelper_recordFrameEvent(jint id, jbyte suspendPolicy, EventIndex ei,
                                  jthread thread, jclass clazz, 
                                  jmethodID method, jlocation location,
                                  int needReturnValue,
                                  jvalue returnValue,
                                  struct bag *eventBag);

jbyte eventHelper_reportEvents(jbyte sessionID, struct bag *eventBag);
void eventHelper_reportInvokeDone(jbyte sessionID, jthread thread);
void eventHelper_reportVMInit(JNIEnv *env, jbyte sessionID, jthread thread, jbyte suspendPolicy);
void eventHelper_suspendThread(jbyte sessionID, jthread thread);

void eventHelper_holdEvents(void);
void eventHelper_releaseEvents(void);

void eventHelper_lock(void);
void eventHelper_unlock(void);

/*
 * Private interface for coordinating between eventHelper.c: commandLoop()
 * and ThreadReferenceImpl.c: resume() and VirtualMachineImpl.c: resume().
 */
void unblockCommandLoop(void);

#endif

