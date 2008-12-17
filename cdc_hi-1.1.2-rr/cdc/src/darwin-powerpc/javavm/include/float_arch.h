/*
 * @(#)float_arch.h	1.9 06/10/10
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

#ifndef _DARWIN_FLOAT_ARCH_H
#define _DARWIN_FLOAT_ARCH_H

#undef JAVA_COMPLIANT_f2i
#undef JAVA_COMPLIANT_f2l

#define NAN_CHECK_f2i
#define NAN_CHECK_f2l
#define BOUNDS_CHECK_f2l

/* Use software floating point */
#undef USE_NATIVE_FREM
#define USE_ANSI_FMOD
#undef USE_NATIVE_FCOMPARE
#define USE_ANSI_FCOMPARE

#define setFPMode()	{}

#endif /* _DARWIN_FLOAT_ARCH_H */
