/*
 * @(#)WinCAPISeedGenerator.c	1.6 06/10/10
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

/* Need to define this to get CAPI functions included */
#ifndef _WIN32_WINNT
#define _WIN32_WINNT 0x0400
#endif

#include <windows.h>
#include <wincrypt.h>
#include <jni.h>
#include <tchar.h>
#include "sun_security_provider_NativeSeedGenerator.h"

/* Typedefs for runtime linking. */
typedef BOOL (WINAPI *CryptAcquireContextType)(HCRYPTPROV*, LPCTSTR, LPCTSTR, DWORD, DWORD);
typedef BOOL (WINAPI *CryptGenRandomType)(HCRYPTPROV, DWORD, BYTE*);
typedef BOOL (WINAPI *CryptReleaseContextType)(HCRYPTPROV, DWORD);

/*
 * Get a random seed from the MS CryptoAPI. Return true if successful, false
 * otherwise.
 *
 * Some early versions of Windows 95 do not support the required functions.
 * Use runtime linking to avoid problems.
 *
 * @version 1.6, 10/10/06
 */
JNIEXPORT jboolean JNICALL Java_sun_security_provider_NativeSeedGenerator_nativeGenerateSeed
  (JNIEnv *env, jclass clazz, jbyteArray randArray)
{
    HMODULE lib;
    CryptAcquireContextType acquireContext;
    CryptGenRandomType genRandom;
    CryptReleaseContextType releaseContext;

    HCRYPTPROV hCryptProv;
    jboolean result = JNI_FALSE;
    jsize numBytes;
    jbyte* randBytes;

    lib = LoadLibrary(_T("ADVAPI32.DLL"));
    if (lib == NULL) {
	return result;
    }

#ifdef UNDER_CE
    acquireContext = (CryptAcquireContextType)
	GetProcAddress(lib, TEXT("CryptAcquireContextA"));
    genRandom = (CryptGenRandomType)
	GetProcAddress(lib, TEXT("CryptGenRandom"));
    releaseContext = (CryptReleaseContextType)
	GetProcAddress(lib, TEXT("CryptReleaseContext"));
#else
    acquireContext = (CryptAcquireContextType)
	GetProcAddress(lib, "CryptAcquireContextA");
    genRandom = (CryptGenRandomType)
	GetProcAddress(lib, "CryptGenRandom");
    releaseContext = (CryptReleaseContextType)
	GetProcAddress(lib, "CryptReleaseContext");
#endif

    if (acquireContext == NULL || genRandom == NULL || releaseContext == NULL) {
	FreeLibrary(lib);
	return result;
    }

    if (acquireContext(&hCryptProv, _T("J2SE"), NULL, PROV_RSA_FULL, 0) == FALSE) {
	/* If CSP context hasn't been created, create one. */
	if (acquireContext(&hCryptProv, _T("J2SE"), NULL, PROV_RSA_FULL,
		CRYPT_NEWKEYSET) == FALSE) {
	    FreeLibrary(lib);
	    return result;
	}
    }

    numBytes = (*env)->GetArrayLength(env, randArray);
    randBytes = (*env)->GetByteArrayElements(env, randArray, NULL);
    if (genRandom(hCryptProv, numBytes, randBytes)) {
	result = JNI_TRUE;
    }
    (*env)->ReleaseByteArrayElements(env, randArray, randBytes, 0);

    releaseContext(hCryptProv, 0);
    FreeLibrary(lib);

    return result;
}
