/*
 * @(#)debugDispatch.h	1.18 06/10/25
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

#ifndef JDWP_DEBUGDISPATCH_H
#define JDWP_DEBUGDISPATCH_H

/*
 * Type of all command handler functions. First argument is the 
 * input stream. Second argument is the output sent back to the 
 * originator, but only if JNI_TRUE is returned. If JNI_FALSE
 * is returned, no reply is made.
 */
struct PacketInputStream;
struct PacketOutputStream;

typedef jboolean (*CommandHandler)(struct PacketInputStream *, 
                                  struct PacketOutputStream *);
void debugDispatch_initialize(void);
void debugDispatch_reset(void);
CommandHandler debugDispatch_getHandler(int cmdSet, int cmd) ;

#endif
