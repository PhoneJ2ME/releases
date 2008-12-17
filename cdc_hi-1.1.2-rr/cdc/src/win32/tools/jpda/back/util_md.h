/*
 * @(#)util_md.h	1.11 06/10/26
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
#include <stdlib.h>      /* for _MAx_PATH */

typedef unsigned __int64 UNSIGNED_JLONG;
typedef unsigned long UNSIGNED_JINT;

#define	MAXPATHLEN _MAX_PATH

/* Needed on Windows because names seem to be hidden in stdio.h. */

#define snprintf        _snprintf
#define vsnprintf       _vsnprintf

#define JDWP_ONLOAD_SYMBOLS   {"_JDWP_OnLoad@16", "JDWP_OnLoad"}

#ifdef WINCE
#define _IOLBF 1
#define BUFSIZ 1
#define getenv(x) NULL
#define perror(x) ((void)0)
#endif
