/*
 * @(#)defs_arch.h	1.7 06/10/10
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

#ifndef _SYMBIAN_SARM_DEFS_ARCH_H
#define _SYMBIAN_SARM_DEFS_ARCH_H

/*
 * CVMatomicCompareAndSwap() is not supported.
 * CVMatomicSwap() is supported.
 */
#undef  CVM_ADV_ATOMIC_CMPANDSWAP
#define CVM_ADV_ATOMIC_SWAP

/* CVMdynlinkSym() does not need to prepend an underscore. */
#undef CVM_DYNLINKSYM_PREPEND_UNDERSCORE

/* We define CVM_ADV_SPINLOCK to indicate that we'll be providing
   platform specific implementations of:
       void CVMspinlockYield();
       void CVMvolatileStore(CVMAddr new_value, volatile CVMAddr *addr);
*/
/*#define CVM_ADV_SPINLOCK */

#endif /* _SYMBIAN_SARM_DEFS_ARCH_H */
