/*
 * @(#)npt.c	1.5 06/10/26
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


#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#include "jni.h"

#include "npt.h"

#include "utf.h"

static int
version_check(char *version)
{
    if ( version==NULL || strcmp(version, NPT_VERSION)!=0 ) {
        return 1;
    }
    return 0;
}

JNIEXPORT void JNICALL 
nptInitialize(NptEnv **pnpt, char *nptVersion, char *options)
{
    NptEnv *npt;

    (*pnpt) = NULL;

    if ( version_check(nptVersion) ) {
	NPT_ERROR("NPT version doesn't match");
	return;
    }

    npt = (NptEnv*)calloc(sizeof(NptEnv), 1);
    if ( npt == NULL ) {
	NPT_ERROR("Cannot allocate calloc space for NptEnv*");
	return;
    }

    if ( options != NULL ) {
	npt->options = strdup(options);
    }
    npt->utfInitialize 		= &utfInitialize;
    npt->utfTerminate 		= &utfTerminate;
    npt->utf8ToPlatform 	= &utf8ToPlatform;
    npt->utf8FromPlatform 	= &utf8FromPlatform;
    npt->utf8ToUtf16 		= &utf8ToUtf16;
    npt->utf16ToUtf8m 		= &utf16ToUtf8m;
    npt->utf16ToUtf8s 		= &utf16ToUtf8s;
    npt->utf8sToUtf8mLength 	= &utf8sToUtf8mLength;
    npt->utf8sToUtf8m 		= &utf8sToUtf8m;
    npt->utf8mToUtf8sLength 	= &utf8mToUtf8sLength;
    npt->utf8mToUtf8s 		= &utf8mToUtf8s;
    
    (*pnpt) = npt;
}

JNIEXPORT void JNICALL 
nptTerminate(NptEnv* npt, char *options)
{

    /* FIXUP: options? Check memory or something? */
    if ( npt->options != NULL ) {
	(void)free(npt->options);
    }
    (void)free(npt);
}

