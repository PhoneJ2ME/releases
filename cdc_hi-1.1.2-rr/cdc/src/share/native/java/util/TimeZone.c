/*
 * @(#)TimeZone.c	1.14 06/10/10
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

#include "javavm/include/porting/ansi/stdlib.h"
#include "javavm/include/porting/ansi/string.h"

#include "jni.h"
#include "jni_util.h"
#include "jvm.h"
#include "javavm/include/porting/timezone.h"

#include "java_util_TimeZone.h"

/*
 * Gets the platform defined TimeZone ID
 */
JNIEXPORT jstring JNICALL
Java_java_util_TimeZone_getSystemTimeZoneID(JNIEnv *env, jclass ign, 
                                            jstring java_home, jstring country)
{
    const char *cname;
    const char *java_home_dir;
    char *javaTZ;

    if (java_home == NULL)
	return NULL;

    java_home_dir = JNU_GetStringPlatformChars(env, java_home, 0);
    if (java_home_dir == NULL)
        return NULL;

    if (country != NULL) {
	cname = JNU_GetStringPlatformChars(env, country, 0);
	/* ignore error cases for cname */
    } else {
	cname = NULL;
    }

    /*
     * Invoke platform dependent mapping function
     */
    javaTZ = CVMtimezoneFindJavaTZ(java_home_dir, cname);
 
    if (java_home_dir != NULL) {
      free((void *)java_home_dir);
    } 
     
    if (cname != NULL) {
	free((void *)cname);     
    }
 

    if (javaTZ != NULL) {
	jstring jstrJavaTZ = JNU_NewStringPlatform(env, javaTZ);
	free((void *)javaTZ);
        return jstrJavaTZ;
    }
    return NULL;
}

/*
 * Gets a GMT offset-based time zone ID (e.g., "GMT-08:00")
 */
JNIEXPORT jstring JNICALL
Java_java_util_TimeZone_getSystemGMTOffsetID(JNIEnv *env, jclass ign)
{
    char *id = CVMgetGMTOffsetID();
    jstring jstrID = NULL;

    if (id != NULL) {
	jstrID = JNU_NewStringPlatform(env, id);
	free((void *)id);
    }
    return jstrID;
}
