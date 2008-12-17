/*
 * @(#)outStream.h	1.19 06/10/25
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

#ifndef JDWP_OUTSTREAM_H
#define JDWP_OUTSTREAM_H

#include "typedefs.h"

#include "transport.h"
#include "FrameID.h"

struct bag;

#define INITIAL_SEGMENT_SIZE   300
#define MAX_SEGMENT_SIZE     10000

typedef struct PacketData {
    int length;
    jbyte *data;
    struct PacketData *next;
} PacketData;

typedef struct PacketOutputStream {
    jbyte *current;
    jint left;
    struct PacketData *segment;
    struct PacketData firstSegment;
    jvmtiError error;
    jboolean sent;
    jdwpPacket packet;
    jbyte initialSegment[INITIAL_SEGMENT_SIZE];
    struct bag *ids;
} PacketOutputStream;

void outStream_initCommand(PacketOutputStream *stream, jint id, 
                           jbyte flags, jbyte commandSet, jbyte command);
void outStream_initReply(PacketOutputStream *stream, jint id);

jint outStream_id(PacketOutputStream *stream);
jbyte outStream_command(PacketOutputStream *stream);

jdwpError outStream_writeBoolean(PacketOutputStream *stream, jboolean val);
jdwpError outStream_writeByte(PacketOutputStream *stream, jbyte val);
jdwpError outStream_writeChar(PacketOutputStream *stream, jchar val);
jdwpError outStream_writeShort(PacketOutputStream *stream, jshort val);
jdwpError outStream_writeInt(PacketOutputStream *stream, jint val);
jdwpError outStream_writeLong(PacketOutputStream *stream, jlong val);
jdwpError outStream_writeFloat(PacketOutputStream *stream, jfloat val);
jdwpError outStream_writeDouble(PacketOutputStream *stream, jdouble val);
jdwpError outStream_writeObjectRef(JNIEnv *env, PacketOutputStream *stream, jobject val);
jdwpError outStream_writeObjectTag(JNIEnv *env, PacketOutputStream *stream, jobject val);
jdwpError outStream_writeFrameID(PacketOutputStream *stream, FrameID val);
jdwpError outStream_writeMethodID(PacketOutputStream *stream, jmethodID val);
jdwpError outStream_writeFieldID(PacketOutputStream *stream, jfieldID val);
jdwpError outStream_writeLocation(PacketOutputStream *stream, jlocation val);
jdwpError outStream_writeByteArray(PacketOutputStream*stream, jint length, jbyte *bytes);
jdwpError outStream_writeString(PacketOutputStream *stream, char *string);
jdwpError outStream_writeValue(JNIEnv *env, struct PacketOutputStream *out, 
                          jbyte typeKey, jvalue value);
jdwpError outStream_skipBytes(PacketOutputStream *stream, jint count);

jdwpError outStream_error(PacketOutputStream *stream);
void outStream_setError(PacketOutputStream *stream, jdwpError error);

void outStream_sendReply(PacketOutputStream *stream);
void outStream_sendCommand(PacketOutputStream *stream);

void outStream_destroy(PacketOutputStream *stream);

#endif
