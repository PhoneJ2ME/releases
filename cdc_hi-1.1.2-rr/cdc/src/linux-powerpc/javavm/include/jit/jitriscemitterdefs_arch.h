/*
 * @(#)jitriscemitterdefs_arch.h	1.6 06/10/10
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

#ifndef _INCLUDED_JITRISCEMITTERDEFS_ARCH_H
#define _INCLUDED_JITRISCEMITTERDEFS_ARCH_H

/**************************************************************
 * CPU C Call convention abstraction - The following are prototypes of calling
 * convention support functions required by the RISC emitter porting layer.
 **************************************************************/

/* The linux powerpc platform has a custom calling convention: */
#define CVMCPU_HAVE_PLATFORM_SPECIFIC_C_CALL_CONVENTION

typedef struct CVMCPUCallContext CVMCPUCallContext;
struct CVMCPUCallContext
{
    int currentIntRegIndex;
#ifdef CVM_HAVE_HARDWARE_FLOATING_POINT
    int currentFloatRegIndex;
#endif
};

#endif /* _INCLUDED_JITRISCEMITTERDEFS_ARCH_H */
