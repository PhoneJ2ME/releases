/*
 * @(#)net_util_md.c	1.7 06/10/10
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

#include <errno.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>

#include "jni_util.h"
#include "jvm.h"
#include "net_util.h"

void 
NET_ThrowCurrent(JNIEnv *env, char *msg) {
    NET_ThrowNew(env, errno, msg);
}

void
NET_ThrowNew(JNIEnv *env, int errorNumber, char *msg) {
    char fullMsg[512];
    if (!msg) {
	msg = "no further information";
    }
    switch(errorNumber) {
    case EBADF: 
	jio_snprintf(fullMsg, sizeof(fullMsg), "socket closed: %s", msg);
	JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException", fullMsg);
	break;
    case EINTR:
	JNU_ThrowByName(env, JNU_JAVAIOPKG "InterruptedIOException", msg);
	break;
    default:
	jio_snprintf(fullMsg, sizeof(fullMsg), "%s: %s", 
		     msg, strerror(errno));
	JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException", fullMsg);
	break;
    }
}


jfieldID
NET_GetFileDescriptorID(JNIEnv *env)
{
    jclass cls = (*env)->FindClass(env, "java/io/FileDescriptor");
    return (*env)->GetFieldID(env, cls, "fd", "I");
}

