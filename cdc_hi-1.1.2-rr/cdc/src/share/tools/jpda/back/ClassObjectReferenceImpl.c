/*
 * @(#)ClassObjectReferenceImpl.c	1.12 06/10/25
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
#include "ClassObjectReferenceImpl.h"
#include "inStream.h"
#include "outStream.h"

static jboolean 
reflectedType(PacketInputStream *in, PacketOutputStream *out)
{
    jbyte tag;
    jobject object;
    JNIEnv *env;

    env = getEnv();
    
    object = inStream_readObjectRef(env, in);
    if (inStream_error(in)) {
        return JNI_TRUE;
    }

    /*
     * In our implementation, the reference type id is the same as the
     * class object id, so we bounce it right back.
     *
     */
    
    tag = referenceTypeTag(object);
    (void)outStream_writeByte(out, tag);
    (void)outStream_writeObjectRef(env, out, object);
    
    return JNI_TRUE;
}

void *ClassObjectReference_Cmds[] = { (void *)1
    ,(void *)reflectedType
};

