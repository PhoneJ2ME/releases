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
#include <stdlib.h>

#include "transport.h"
#include "debugDispatch.h"
#include "util.h"
#include "JDWP.h"
#include "VirtualMachineImpl.h"
#include "ReferenceTypeImpl.h"
#include "ClassTypeImpl.h"
#include "ArrayTypeImpl.h"
#include "FieldImpl.h"
#include "MethodImpl.h"
#include "ObjectReferenceImpl.h"
#include "StringReferenceImpl.h"
#include "ThreadReferenceImpl.h"
#include "ThreadGroupReferenceImpl.h"
#include "ClassLoaderReferenceImpl.h"
#include "ClassObjectReferenceImpl.h"
#include "ArrayReferenceImpl.h"
#include "EventRequestImpl.h"
#include "StackFrameImpl.h"

static void **l1Array;

void 
debugDispatch_initialize()
{
    /*
     * Create the level-one (CommandSet) dispatch table.
     * Zero the table so that unknown CommandSets do not
     * cause random errors.
     */
    l1Array = jdwpClearedAlloc((JDWP_HIGHEST_COMMAND_SET+1) * sizeof(void *));

    if (l1Array == NULL) {
        ALLOC_ERROR_EXIT();
    }

    /*
     * Create the level-two (Command) dispatch tables to the
     * corresponding slots in the CommandSet dispatch table..
     */
    l1Array[JDWP_COMMAND_SET(VirtualMachine)] = (void *)VirtualMachine_Cmds;
    l1Array[JDWP_COMMAND_SET(ReferenceType)] = (void *)ReferenceType_Cmds;
    l1Array[JDWP_COMMAND_SET(ClassType)] = (void *)ClassType_Cmds;
    l1Array[JDWP_COMMAND_SET(ArrayType)] = (void *)ArrayType_Cmds;

    l1Array[JDWP_COMMAND_SET(Field)] = (void *)Field_Cmds;
    l1Array[JDWP_COMMAND_SET(Method)] = (void *)Method_Cmds;
    l1Array[JDWP_COMMAND_SET(ObjectReference)] = (void *)ObjectReference_Cmds;
    l1Array[JDWP_COMMAND_SET(StringReference)] = (void *)StringReference_Cmds;
    l1Array[JDWP_COMMAND_SET(ThreadReference)] = (void *)ThreadReference_Cmds;
    l1Array[JDWP_COMMAND_SET(ThreadGroupReference)] = (void *)ThreadGroupReference_Cmds;
    l1Array[JDWP_COMMAND_SET(ClassLoaderReference)] = (void *)ClassLoaderReference_Cmds;
    l1Array[JDWP_COMMAND_SET(ArrayReference)] = (void *)ArrayReference_Cmds;
    l1Array[JDWP_COMMAND_SET(EventRequest)] = (void *)EventRequest_Cmds;
    l1Array[JDWP_COMMAND_SET(StackFrame)] = (void *)StackFrame_Cmds;
    l1Array[JDWP_COMMAND_SET(ClassObjectReference)] = (void *)ClassObjectReference_Cmds;
}

void
debugDispatch_reset()
{
}

CommandHandler 
debugDispatch_getHandler(int cmdSet, int cmd) 
{
    void **l2Array;

    if (cmdSet > JDWP_HIGHEST_COMMAND_SET) {
        return NULL;
    }

    l2Array = (void **)l1Array[cmdSet];

    /*
     * If there is no such CommandSet or the Command
     * is greater than the nummber of commands (the first
     * element) in the CommandSet, indicate this is invalid.
     */
    if (l2Array == NULL || cmd > (int)l2Array[0]) {
        return NULL;
    }

    return (CommandHandler)l2Array[cmd];
}
