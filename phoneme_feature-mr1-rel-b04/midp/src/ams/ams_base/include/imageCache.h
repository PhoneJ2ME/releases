/*
 *
 * Copyright  1990-2006 Sun Microsystems, Inc. All Rights Reserved.
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

#ifndef _IMAGECACHE_H_
#define _IMAGECACHE_H_

/**
 * @file
 * @ingroup ams_base
 * API for native image cache. Declares methods for creating cache
 * and dealing with cached images.
 */

#include <midpString.h>

#ifdef __cplusplus
extern "C" {
#endif

/**
 * Loads a native image from cache, if present. 
 *
 * @param suiteId   Suite id
 * @param resName   Name of the image resource
 * @param bufPtr    Pointer to buffer pointer. Caller will need
 *                  to free this on return.
 *
 * @return handle to a jar or NULL if an error with pError containing a status
 */
int loadImageFromCache(const pcsl_string * suiteId, const pcsl_string * resName,
                       unsigned char **bufPtr);

/**
 * Create cache of images in the jar file associated with the suiteId
 *
 * @param suiteId   Suite id
 */
void createImageCache(const pcsl_string * suiteId);

#ifdef __cplusplus
}
#endif

#endif /* _IMAGECACHE_H_ */
