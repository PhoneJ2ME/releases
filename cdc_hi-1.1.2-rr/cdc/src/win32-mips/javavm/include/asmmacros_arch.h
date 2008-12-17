/*
 * @(#)asmmacros_arch.h	1.7 06/10/10
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

#ifndef _INCLUDED_ASMMACROS_ARCH_H
#define _INCLUDED_ASMMACROS_ARCH_H

#ifndef _ASM
#define _ASM 
#endif

#include <kxmips.h>

#define jp t9
#define fp s8
#define NESTED(name_, size_, reg_)	NESTED_ENTRY(name_, size_, reg_)
#define ENTRY(x)	LEAF_ENTRY(x)
#define SET_SIZE(x)	.end	x;
#define EXPORT(x)	.globl x
#define LA(r,sym)	la	r, sym

#endif /* _INCLUDED_ASMMACROS_ARCH_H */
