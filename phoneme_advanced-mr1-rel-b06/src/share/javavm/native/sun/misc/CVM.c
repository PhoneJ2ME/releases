/*
 * Copyright 1990-2006 Sun Microsystems, Inc. All Rights Reserved. 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER 
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 only,
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * version 2 for more details (a copy is included at /legal/license.txt).
 * 
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 or visit www.sun.com if you need additional information or have
 * any questions.
 */

/*
 * All methods in this file use the CNI native method interface. See
 * cjp07/13/99 for details.
 */

#include "javavm/include/interpreter.h"
#include "javavm/include/directmem.h"
#include "javavm/include/indirectmem.h"
#include "javavm/include/utils.h"
#include "javavm/include/common_exceptions.h"
#include "javavm/include/gc_common.h"
#include "javavm/include/stackmaps.h"
#include "javavm/include/preloader.h"
#include "javavm/export/jvm.h"
#include "generated/offsets/java_lang_Thread.h"

#ifdef CVM_TRACE_JIT
#include "javavm/include/jit/jitutils.h"
#endif
#ifdef CVM_JIT
#include "javavm/include/jit/jit.h"
#include "javavm/include/jit/jitcodebuffer.h"
#endif
#ifndef CDC_10
#include "javavm/include/javaAssertions.h"
#endif
#ifdef CVM_XRUN
#include "javavm/include/xrun.h"
#endif
#ifdef CVM_JVMDI
#include "javavm/include/jvmdi_jni.h"
#endif

#ifdef CVM_DEBUG_ASSERTS
#define CVMassertOKToCopyArrayOfType(expectedType_) \
    {                                                                   \
        CVMClassBlock *srcCb, *dstCb;                                   \
        CVMClassBlock *srcElemCb, *dstElemCb;                           \
        size_t srclen, dstlen;                                          \
                                                                        \
        CVMassert(srcArr != NULL);                                      \
        CVMassert(dstArr != NULL);                                      \
                                                                        \
        srcCb = CVMobjectGetClass(srcArr);                              \
        dstCb = CVMobjectGetClass(dstArr);                              \
        CVMassert(CVMisArrayClass(srcCb));                              \
        CVMassert(CVMisArrayClass(dstCb));                              \
                                                                        \
        srcElemCb = CVMarrayElementCb(srcCb);                           \
        dstElemCb = CVMarrayElementCb(dstCb);                           \
        if (expectedType_ != CVM_T_CLASS) {                             \
            CVMassert(srcElemCb == dstElemCb);                          \
        } else {                                                        \
            CVMassert((srcElemCb == dstElemCb) ||                       \
                      (dstElemCb == CVMsystemClass(java_lang_Object))); \
        }                                                               \
        CVMassert(CVMarrayElemTypeCode(srcCb) == expectedType_);        \
                                                                        \
        srclen = CVMD_arrayGetLength(srcArr);                           \
        dstlen = CVMD_arrayGetLength(dstArr);                           \
                                                                        \
        CVMassert(!(length < 0));                                       \
        CVMassert(!(src_pos < 0));                                      \
        CVMassert(!(dst_pos < 0));                                      \
        CVMassert(!(length + src_pos > srclen));                        \
        CVMassert(!(length + dst_pos > dstlen));                        \
    }
#else
#define CVMassertOKToCopyArrayOfType(expectedType_)
#endif

CNIResultCode
CNIsun_misc_CVM_copyBooleanArray(CVMExecEnv* ee, CVMStackVal32 *arguments,
                                 CVMMethodBlock **p_mb)
{
    jobject src  = &arguments[0].j.r;
    jint src_pos =  arguments[1].j.i;
    jobject dst  = &arguments[2].j.r;
    jint dst_pos =  arguments[3].j.i;
    jint length  =  arguments[4].j.i;
    CVMArrayOfBoolean *srcArr;
    CVMArrayOfBoolean *dstArr;

    /* CNI policy: offer a gc-safe checkpoint */
    CVMD_gcSafeCheckPoint(ee, {}, {});

    srcArr = (CVMArrayOfBoolean *)CVMID_icellDirect(ee, src);
    dstArr = (CVMArrayOfBoolean *)CVMID_icellDirect(ee, dst);
    CVMassertOKToCopyArrayOfType(CVM_T_BOOLEAN);
    CVMD_arrayCopyBoolean(srcArr, src_pos, dstArr, dst_pos, length);
    return CNI_VOID;
}

CNIResultCode
CNIsun_misc_CVM_copyByteArray(CVMExecEnv* ee, CVMStackVal32 *arguments,
                              CVMMethodBlock **p_mb)
{
    jobject src  = &arguments[0].j.r;
    jint src_pos =  arguments[1].j.i;
    jobject dst  = &arguments[2].j.r;
    jint dst_pos =  arguments[3].j.i;
    jint length  =  arguments[4].j.i;
    CVMArrayOfByte *srcArr;
    CVMArrayOfByte *dstArr;

    /* CNI policy: offer a gc-safe checkpoint */
    CVMD_gcSafeCheckPoint(ee, {}, {});

    srcArr = (CVMArrayOfByte *)CVMID_icellDirect(ee, src);
    dstArr = (CVMArrayOfByte *)CVMID_icellDirect(ee, dst);
    CVMassertOKToCopyArrayOfType(CVM_T_BYTE);
    CVMD_arrayCopyByte(srcArr, src_pos, dstArr, dst_pos, length);
    return CNI_VOID;
}

CNIResultCode
CNIsun_misc_CVM_copyShortArray(CVMExecEnv* ee, CVMStackVal32 *arguments,
                               CVMMethodBlock **p_mb)
{
    jobject src  = &arguments[0].j.r;
    jint src_pos =  arguments[1].j.i;
    jobject dst  = &arguments[2].j.r;
    jint dst_pos =  arguments[3].j.i;
    jint length  =  arguments[4].j.i;
    CVMArrayOfShort *srcArr;
    CVMArrayOfShort *dstArr;

    /* CNI policy: offer a gc-safe checkpoint */
    CVMD_gcSafeCheckPoint(ee, {}, {});

    srcArr = (CVMArrayOfShort *)CVMID_icellDirect(ee, src);
    dstArr = (CVMArrayOfShort *)CVMID_icellDirect(ee, dst);
    CVMassertOKToCopyArrayOfType(CVM_T_SHORT);
    CVMD_arrayCopyShort(srcArr, src_pos, dstArr, dst_pos, length);
    return CNI_VOID;
}

CNIResultCode
CNIsun_misc_CVM_copyCharArray(CVMExecEnv* ee, CVMStackVal32 *arguments,
                              CVMMethodBlock **p_mb)
{
    jobject src  = &arguments[0].j.r;
    jint src_pos =  arguments[1].j.i;
    jobject dst  = &arguments[2].j.r;
    jint dst_pos =  arguments[3].j.i;
    jint length  =  arguments[4].j.i;
    CVMArrayOfChar *srcArr;
    CVMArrayOfChar *dstArr;

    /* CNI policy: offer a gc-safe checkpoint */
    CVMD_gcSafeCheckPoint(ee, {}, {});

    srcArr = (CVMArrayOfChar *)CVMID_icellDirect(ee, src);
    dstArr = (CVMArrayOfChar *)CVMID_icellDirect(ee, dst);
    CVMassertOKToCopyArrayOfType(CVM_T_CHAR);
    CVMD_arrayCopyChar(srcArr, src_pos, dstArr, dst_pos, length);
    return CNI_VOID;
}

CNIResultCode
CNIsun_misc_CVM_copyIntArray(CVMExecEnv* ee, CVMStackVal32 *arguments,
                             CVMMethodBlock **p_mb)
{
    jobject src  = &arguments[0].j.r;
    jint src_pos =  arguments[1].j.i;
    jobject dst  = &arguments[2].j.r;
    jint dst_pos =  arguments[3].j.i;
    jint length  =  arguments[4].j.i;
    CVMArrayOfInt *srcArr;
    CVMArrayOfInt *dstArr;

    /* CNI policy: offer a gc-safe checkpoint */
    CVMD_gcSafeCheckPoint(ee, {}, {});

    srcArr = (CVMArrayOfInt *)CVMID_icellDirect(ee, src);
    dstArr = (CVMArrayOfInt *)CVMID_icellDirect(ee, dst);
    CVMassertOKToCopyArrayOfType(CVM_T_INT);
    CVMD_arrayCopyInt(srcArr, src_pos, dstArr, dst_pos, length);
    return CNI_VOID;
}

CNIResultCode
CNIsun_misc_CVM_copyLongArray(CVMExecEnv* ee, CVMStackVal32 *arguments,
                              CVMMethodBlock **p_mb)
{
    jobject src  = &arguments[0].j.r;
    jint src_pos =  arguments[1].j.i;
    jobject dst  = &arguments[2].j.r;
    jint dst_pos =  arguments[3].j.i;
    jint length  =  arguments[4].j.i;
    CVMArrayOfLong *srcArr;
    CVMArrayOfLong *dstArr;

    /* CNI policy: offer a gc-safe checkpoint */
    CVMD_gcSafeCheckPoint(ee, {}, {});

    srcArr = (CVMArrayOfLong *)CVMID_icellDirect(ee, src);
    dstArr = (CVMArrayOfLong *)CVMID_icellDirect(ee, dst);
    CVMassertOKToCopyArrayOfType(CVM_T_LONG);
    CVMD_arrayCopyLong(srcArr, src_pos, dstArr, dst_pos, length);
    return CNI_VOID;
}

CNIResultCode
CNIsun_misc_CVM_copyFloatArray(CVMExecEnv* ee, CVMStackVal32 *arguments,
                               CVMMethodBlock **p_mb)
{
    jobject src  = &arguments[0].j.r;
    jint src_pos =  arguments[1].j.i;
    jobject dst  = &arguments[2].j.r;
    jint dst_pos =  arguments[3].j.i;
    jint length  =  arguments[4].j.i;
    CVMArrayOfFloat *srcArr;
    CVMArrayOfFloat *dstArr;

    /* CNI policy: offer a gc-safe checkpoint */
    CVMD_gcSafeCheckPoint(ee, {}, {});

    srcArr = (CVMArrayOfFloat *)CVMID_icellDirect(ee, src);
    dstArr = (CVMArrayOfFloat *)CVMID_icellDirect(ee, dst);
    CVMassertOKToCopyArrayOfType(CVM_T_FLOAT);
    CVMD_arrayCopyFloat(srcArr, src_pos, dstArr, dst_pos, length);
    return CNI_VOID;
}

CNIResultCode
CNIsun_misc_CVM_copyDoubleArray(CVMExecEnv* ee, CVMStackVal32 *arguments,
                                CVMMethodBlock **p_mb)
{
    jobject src  = &arguments[0].j.r;
    jint src_pos =  arguments[1].j.i;
    jobject dst  = &arguments[2].j.r;
    jint dst_pos =  arguments[3].j.i;
    jint length  =  arguments[4].j.i;
    CVMArrayOfDouble *srcArr;
    CVMArrayOfDouble *dstArr;

    /* CNI policy: offer a gc-safe checkpoint */
    CVMD_gcSafeCheckPoint(ee, {}, {});

    srcArr = (CVMArrayOfDouble *)CVMID_icellDirect(ee, src);
    dstArr = (CVMArrayOfDouble *)CVMID_icellDirect(ee, dst);
    CVMassertOKToCopyArrayOfType(CVM_T_DOUBLE);
    CVMD_arrayCopyDouble(srcArr, src_pos, dstArr, dst_pos, length);
    return CNI_VOID;
}

/* Purpose: This method copies array elements from one array to another.
   Unlike System.arraycopy(), this method can only copy elements for
   non-primitive arrays with some restrictions.  The restrictions are that
   the src and dest array must be of the same type, or the destination
   array must be of type java.lang.Object[] (not just a compatible sub-type).
   The caller is responsible for doing the appropriate null checks, bounds
   checks, array element type assignment checks if necessary, and ensure that
   the passed in arguments do violate any of these checks and restrictions.
   If the condition of these checks and restrictions are not taken cared of
   by the caller, copyObjectArray() can fail in unpredictable ways.
*/
CNIResultCode
CNIsun_misc_CVM_copyObjectArray(CVMExecEnv* ee, CVMStackVal32 *arguments,
                                CVMMethodBlock **p_mb)
{
    jobject src  = &arguments[0].j.r;
    jint src_pos =  arguments[1].j.i;
    jobject dst  = &arguments[2].j.r;
    jint dst_pos =  arguments[3].j.i;
    jint length  =  arguments[4].j.i;
    CVMArrayOfRef *srcArr;
    CVMArrayOfRef *dstArr;

    /* CNI policy: offer a gc-safe checkpoint */
    CVMD_gcSafeCheckPoint(ee, {}, {});

    srcArr = (CVMArrayOfRef *)CVMID_icellDirect(ee, src);
    dstArr = (CVMArrayOfRef *)CVMID_icellDirect(ee, dst);
    CVMassertOKToCopyArrayOfType(CVM_T_CLASS);
    CVMD_arrayCopyRef(srcArr, src_pos, dstArr, dst_pos, length);
    return CNI_VOID;
}

CNIResultCode
CNIsun_misc_CVM_checkDebugFlags(CVMExecEnv* ee, CVMStackVal32 *arguments,
				CVMMethodBlock **p_mb)
{
#ifdef CVM_TRACE_ENABLED
    CVMJavaInt flags = arguments[0].j.i;
    arguments[0].j.i = CVMcheckDebugFlags(flags);
#else
    arguments[0].j.i = 0;
#endif
    return CNI_SINGLE;
}

CNIResultCode
CNIsun_misc_CVM_setDebugFlags(CVMExecEnv* ee, CVMStackVal32 *arguments,
			      CVMMethodBlock **p_mb)
{
#ifdef CVM_TRACE_ENABLED
    CVMJavaInt flags = arguments[0].j.i;
    arguments[0].j.i = CVMsetDebugFlags(flags);
#else
    arguments[0].j.i = 0;
#endif
    return CNI_SINGLE;
}

CNIResultCode
CNIsun_misc_CVM_clearDebugFlags(CVMExecEnv* ee, CVMStackVal32 *arguments,
				CVMMethodBlock **p_mb)
{
#ifdef CVM_TRACE_ENABLED
    CVMJavaInt flags = arguments[0].j.i;
    arguments[0].j.i = CVMclearDebugFlags(flags);
#else
    arguments[0].j.i = 0;
#endif
    return CNI_SINGLE;
}

CNIResultCode
CNIsun_misc_CVM_restoreDebugFlags(CVMExecEnv* ee, CVMStackVal32 *arguments,
				  CVMMethodBlock **p_mb)
{
#ifdef CVM_TRACE_ENABLED
    CVMJavaInt flags = arguments[0].j.i;
    CVMJavaInt oldvalue = arguments[1].j.i;
    arguments[0].j.i = CVMrestoreDebugFlags(flags, oldvalue);
#else
    arguments[0].j.i = 0;
#endif
    return CNI_SINGLE;
}

CNIResultCode
CNIsun_misc_CVM_checkDebugJITFlags(CVMExecEnv* ee, CVMStackVal32 *arguments,
                                   CVMMethodBlock **p_mb)
{
#ifdef CVM_TRACE_JIT
    CVMJavaInt flags = arguments[0].j.i;
    arguments[0].j.i = CVMcheckDebugJITFlags(flags);
#else
    arguments[0].j.i = 0;
#endif
    return CNI_SINGLE;
}

CNIResultCode
CNIsun_misc_CVM_setDebugJITFlags(CVMExecEnv* ee, CVMStackVal32 *arguments,
                                 CVMMethodBlock **p_mb)
{
#ifdef CVM_TRACE_JIT
    CVMJavaInt flags = arguments[0].j.i;
    arguments[0].j.i = CVMsetDebugJITFlags(flags);
#else
    arguments[0].j.i = 0;
#endif
    return CNI_SINGLE;
}

CNIResultCode
CNIsun_misc_CVM_clearDebugJITFlags(CVMExecEnv* ee, CVMStackVal32 *arguments,
                                   CVMMethodBlock **p_mb)
{
#ifdef CVM_TRACE_JIT
    CVMJavaInt flags = arguments[0].j.i;
    arguments[0].j.i = CVMclearDebugJITFlags(flags);
#else
    arguments[0].j.i = 0;
#endif
    return CNI_SINGLE;
}

CNIResultCode
CNIsun_misc_CVM_restoreDebugJITFlags(CVMExecEnv* ee, CVMStackVal32 *arguments,
                                     CVMMethodBlock **p_mb)
{
#ifdef CVM_TRACE_JIT
    CVMJavaInt flags = arguments[0].j.i;
    CVMJavaInt oldvalue = arguments[1].j.i;
    arguments[0].j.i = CVMrestoreDebugJITFlags(flags, oldvalue);
#else
    arguments[0].j.i = 0;
#endif
    return CNI_SINGLE;
}

/*
 * executeClinit is responsible for getting the <clinit> method of the
 * specified class executed. It could do this by just using JNI to
 * invoke the <clinit> method, but this causes undesireable C recursion
 * in the interpreter. Instead we just store the mb of the <clinit>
 * method in *p_mb, and return CVM_NEW_MB to the interpreter. This
 * signals the interpreter to invoke the method stored in *p_mb.
 */
CNIResultCode
CNIsun_misc_CVM_executeClinit(CVMExecEnv* ee, CVMStackVal32 *arguments,
			      CVMMethodBlock **p_mb)
{
    CVMClassBlock* cb = CVMgcUnsafeClassRef2ClassBlock(ee, &arguments[0].j.r);
    CVMMethodBlock* clinitmb;
    CVMD_gcSafeExec(ee, {
	clinitmb = CVMclassGetStaticMethodBlock(cb, CVMglobals.clinitTid);
    });
    CVMtraceClinit(("[Initializing %C]\n", cb));
    if (clinitmb != NULL) {
	CVMtraceClinit(("[Running static initializer for %C]\n", cb));
	/* Return the new mb */
	*p_mb = clinitmb;
	return CNI_NEW_MB;
    } else {
	return CNI_VOID;
    }
}

/*
 * If the class is dynamically loaded and has a <clinit> method, then
 * free up the memory allocated for the <clinit> method. Note the mb,
 * stays around, but all the code and other data located in the jmd
 * is freed up.
 *
 * (Formerly, the entire body of this was bracketed by the
 * ifdef CVM_CLASSLOADING block. But now that we want to
 * free the stackmaps even for preloaded classes, we need to
 * go through the motions anyway.)
 */

CNIResultCode
CNIsun_misc_CVM_freeClinit(CVMExecEnv* ee, CVMStackVal32 *arguments,
			   CVMMethodBlock **p_mb)
{
    /*
     * Both JVMDI and JVMPI require that the jmd for the clinit
     * not be freed.
     */
#if !defined(CVM_JVMDI) && !defined(CVM_JVMPI)
    CVMClassBlock* cb = CVMgcUnsafeClassRef2ClassBlock(ee, &arguments[0].j.r);
    CVMMethodBlock* clinitmb;
    CVMJavaMethodDescriptor* jmd;

    CVMD_gcSafeExec(ee, {
	clinitmb = CVMclassGetStaticMethodBlock(cb, CVMglobals.clinitTid);
	if (clinitmb != NULL) {
	    if (CVMmbIsJava(clinitmb)) {
                CVMStackMaps *maps;
		jmd = CVMmbJmd(clinitmb);
                /* Need to acquire the heapLock before accessing the
                 * stackmaps list to avoid contention with a gc thread,
                 * which may also manipulate the list.
                 */
                CVMsysMutexLock(ee, &CVMglobals.heapLock);
		if ((maps = CVMstackmapFind(ee, clinitmb)) != NULL) {
                    CVMstackmapDestroy(ee, maps);
		}
                CVMsysMutexUnlock(ee, &CVMglobals.heapLock);

		if ((!CVMcbIsInROM(cb)) 
		    || (CVMjmdFlags(jmd) & CVM_JMD_DID_REWRITE)) {
		    CVMmbJmd(clinitmb) = NULL;
		    free(jmd);
		}
		if (!CVMcbIsInROM(cb)) {
		    CVMclassFreeLocalVariableTableFieldIDs(ee, clinitmb);
		}
	    }
	}
    });
#endif
    return CNI_VOID;
}

/*
 * executeLoadSuperClasses is responsible for getting the
 * Class.loadSuperClasses() executed for
 * Launcher.defineClassPrivate(), which doesn't have access to it from
 * java. It could do this by just using JNI to invoke
 * theClass.loadSuperClasses() method, but this causes undesireable C
 * recursion in the interpreter. Instead we just store the mb of the
 * Class.loadSuperClasses() method in *p_mb, and return CVM_NEW_MB to the
 * interpreter. This signals the interpreter to invoke the method
 * stored in *p_mb. 
 */
CNIResultCode
CNIsun_misc_CVM_executeLoadSuperClasses(
    CVMExecEnv* ee, CVMStackVal32 *arguments, CVMMethodBlock **p_mb)
{
    /* Return the new mb */
    *p_mb = CVMglobals.java_lang_Class_loadSuperClasses;
    return CNI_NEW_MB;
}

#ifdef FOR_EXAMPLE
CNIResultCode
CNIsun_misc_CVM_arraycopy(CVMExecEnv* ee, CVMStackVal32 *arguments,
			  CVMMethodBlock **p_mb)
{
    CVMObjectICell *src = &arguments[0].j.r;
    CVMJavaInt src_position = arguments[1].j.i;
    CVMObjectICell *dst = &arguments[2].j.r;
    CVMJavaInt dst_position = arguments[3].j.i;
    CVMJavaInt length = arguments[4].j.i;

    /* For now, until we have an optimized, unsafe version of arraycopy */
    CVMD_gcSafeExec(ee, {
	JNIEnv *env = CVMexecEnv2JniEnv(ee);
	JVM_ArrayCopy(env, NULL, src, src_position, dst, dst_position, length);
    });

    return CNI_VOID;
}
#endif

CNIResultCode
CNIsun_misc_CVM_disableRemoteExceptions(CVMExecEnv* ee,
					CVMStackVal32 *arguments,
					CVMMethodBlock **p_mb)
{
    CVMdisableRemoteExceptions(ee);
    return CNI_VOID;
}

CNIResultCode
CNIsun_misc_CVM_enableRemoteExceptions(CVMExecEnv* ee,
				       CVMStackVal32 *arguments,
				       CVMMethodBlock **p_mb)
{
    CVMenableRemoteExceptions(ee);
    /* Check if remote exception have thrown */
    if (CVMexceptionOccurred(ee)) {
	return CNI_EXCEPTION;
    } else {
	return CNI_VOID;
    }
}

CNIResultCode
CNIsun_misc_CVM_throwRemoteException(CVMExecEnv* ee,
				     CVMStackVal32 *arguments,
				     CVMMethodBlock **p_mb)
{
#ifdef CVM_REMOTE_EXCEPTIONS_SUPPORTED
    CVMObjectICell *threadICell = &arguments[0].j.r;
    CVMObjectICell *exceptionICell = &arguments[1].j.r;
    CVMJavaLong eetop;
    CVMExecEnv *targetEE;

    CVMD_fieldReadLong(CVMID_icellDirect(ee, threadICell),
		       CVMoffsetOfjava_lang_Thread_eetop,
		       eetop);
    targetEE = (CVMExecEnv *)CVMlong2VoidPtr(eetop);

    /* Have to be invoked through Thread.stop1() that ensures ee != NULL */
    CVMassert(ee != NULL);
    CVMgcUnsafeThrowRemoteException(ee, targetEE, 
				    CVMID_icellDirect(ee, exceptionICell));

    /* %comment: rt037 */
#else
    CVMassert(CVM_FALSE);
#endif
    return CNI_VOID;
}

CNIResultCode
CNIsun_misc_CVM_throwLocalException(CVMExecEnv* ee,
				    CVMStackVal32 *arguments,
				    CVMMethodBlock **p_mb)
{
    CVMObjectICell *exceptionICell = &arguments[0].j.r;

    CVMgcUnsafeThrowLocalException(ee, CVMID_icellDirect(ee, exceptionICell));

    return CNI_EXCEPTION;
}

#ifdef CVM_DUAL_STACK
CVMBool
CVMclassloaderIsCLDCClassLoader(CVMExecEnv *ee,
                                CVMClassLoaderICell* loaderICell)
{
    if (loaderICell != NULL) {
        CVMClassBlock* loaderCB = CVMobjectGetClass(
                                  CVMID_icellDirect(ee, loaderICell));
        const char *midletLoaderName = "sun/misc/MIDletClassLoader";
        CVMClassTypeID MIDletClassLoaderID =
            CVMtypeidLookupClassID(ee, midletLoaderName, 
                                   strlen(midletLoaderName));
        if (CVMcbClassName(loaderCB) == MIDletClassLoaderID ){
            return CVM_TRUE;
        } else {
            return CVM_FALSE;
        }
    }
    return CVM_FALSE;
}
#endif

CNIResultCode
CNIsun_misc_CVM_callerCLIsMIDCLs(CVMExecEnv* ee,
                                   CVMStackVal32 *arguments,
                                   CVMMethodBlock **p_mb)
{
#ifndef CVM_DUAL_STACK
    arguments[0].j.i = CVM_FALSE;
#else
    CVMClassBlock* cb;
    CVMClassLoaderICell* loaderICell;
 
    /* 
     * Get the caller. Note we only look one frame up here because
     * there is no frame pushed for the CNI method.
     */
    cb = CVMgetCallerClass(ee, 1);
    loaderICell = (cb == NULL) ? NULL : CVMcbClassLoader(cb);
    
    arguments[0].j.i = CVMclassloaderIsCLDCClassLoader(ee, loaderICell);
#endif
    return CNI_SINGLE;
}

/* %begin lvm */
CNIResultCode
CNIsun_misc_CVM_inMainLVM(CVMExecEnv* ee,
			  CVMStackVal32 *arguments,
			  CVMMethodBlock **p_mb)
{
#ifdef CVM_LVM
    CVMD_gcSafeExec(ee, {
	arguments[0].j.i = (CVMLVMinMainLVM(ee))?(JNI_TRUE):(JNI_FALSE);
    });
#else
    arguments[0].j.i = JNI_TRUE;
#endif

    return CNI_SINGLE;
}
/* %end lvm */

CNIResultCode
CNIsun_misc_CVM_gcDumpHeapSimple(CVMExecEnv* ee,
				 CVMStackVal32 *arguments,
				 CVMMethodBlock **p_mb)
{
#ifdef CVM_INSPECTOR
    CVMD_gcSafeExec(ee, {
	CVMgcDumpHeapSimple();
    });
#endif

    return CNI_VOID;
}

CNIResultCode
CNIsun_misc_CVM_gcDumpHeapVerbose(CVMExecEnv* ee,
				  CVMStackVal32 *arguments,
				  CVMMethodBlock **p_mb)
{
#ifdef CVM_INSPECTOR
    CVMD_gcSafeExec(ee, {
	CVMgcDumpHeapVerbose();
    });
#endif

    return CNI_VOID;
}

CNIResultCode
CNIsun_misc_CVM_gcDumpHeapStats(CVMExecEnv* ee,
				CVMStackVal32 *arguments,
				CVMMethodBlock **p_mb)
{
#ifdef CVM_INSPECTOR
    CVMD_gcSafeExec(ee, {
	CVMgcDumpHeapStats();
    });
#endif

    return CNI_VOID;
}

#ifdef CVM_DEBUG
#include "javavm/include/porting/system.h"
#include "javavm/include/porting/time.h"

#define TRACE_SIZE 1000
static CVMInt64 millis[TRACE_SIZE];
static int id[TRACE_SIZE];
static int indx;
CNIResultCode
CNIsun_misc_CVM_trace(CVMExecEnv *ee,
		      CVMStackVal32 *arguments,
		      CVMMethodBlock **p_mb)
{
    CVMJavaInt i = arguments[0].j.i;
    CVMInt64 l = CVMtimeMillis();

    if (i < 0) {
	if (indx > 0) {
	    int j;
	    for (j = 1; j < indx; ++j) {
		CVMconsolePrintf("t%d - t%d--> %dms\n", id[j], id[j-1],
		    CVMlong2Int(CVMlongSub(millis[j], millis[j-1])));
	    }
	}
	indx = 0;
	i = -i;
    }
    if (indx < TRACE_SIZE) {
	millis[indx] = l;
	id[indx] = i;
	++indx;
    }

    return CNI_VOID;
}
#else
CNIResultCode
CNIsun_misc_CVM_trace(CVMExecEnv *ee,
		      CVMStackVal32 *arguments,
		      CVMMethodBlock **p_mb)
{
    return CNI_VOID;
}
#endif /* !DEBUG */


CNIResultCode
CNIsun_misc_CVM_setDebugEvents(CVMExecEnv* ee, CVMStackVal32 *arguments,
			       CVMMethodBlock **p_mb)
{
#ifdef CVM_JVMDI
    ee->debugEventsEnabled = arguments[0].j.i;
#endif
    return CNI_VOID;
}

CNIResultCode
CNIsun_misc_CVM_postThreadExit(CVMExecEnv* ee, CVMStackVal32 *arguments,
			       CVMMethodBlock **p_mb)
{
    CVMD_gcSafeExec(ee, {
	CVMpostThreadExitEvents(ee);
    });
    return CNI_VOID;
}

CNIResultCode
CNIsun_misc_CVM_setContextArtificial(CVMExecEnv* ee, CVMStackVal32 *arguments,
				     CVMMethodBlock **p_mb)
{
    CVMFrameIterator iter;
    CVMframeIterate(CVMeeGetCurrentFrame(ee), &iter);
    CVMframeIterateSkipSpecial(&iter, 0, CVM_FALSE, CVM_FALSE);
    CVMframeIterateSetFlags(&iter, (CVMFrameFlags)
	(CVMframeIterateGetFlags(&iter) | CVM_FRAMEFLAG_ARTIFICIAL));
    return CNI_VOID;
}

/*
 * Inflates an object's monitor and marks it sticky so it's never freed.
 */
CNIResultCode
CNIsun_misc_CVM_objectInflatePermanently(CVMExecEnv* ee,
					 CVMStackVal32 *arguments,
					 CVMMethodBlock **p_mb)
{
    CVMObjectICell *indirectObj = &arguments[0].j.r;
    CVMObjMonitor *mon;

    mon = CVMobjectInflatePermanently(ee, indirectObj);
    if (mon != NULL) {
	arguments[0].j.i = CVM_TRUE;
    } else {
	arguments[0].j.i = CVM_FALSE;
    }

    return CNI_SINGLE;
}


/*
 * enable/disable compilations by current thread
 */
CNIResultCode 
CNIsun_misc_CVM_setThreadNoCompilationsFlag(CVMExecEnv* ee,
					    CVMStackVal32 *arguments,
					    CVMMethodBlock **p_mb)
{
#ifdef CVM_JIT
    CVMBool noCompilations = arguments[0].j.i;
    ee->noCompilations = noCompilations;
#endif    
    return CNI_VOID;
}
CNIResultCode
CNIsun_misc_CVM_getCallerClass(CVMExecEnv* ee, CVMStackVal32 *arguments,
			       CVMMethodBlock **p_mb)
{
    CVMJavaInt skip = arguments[0].j.i;

    CVMClassBlock* cb = CVMgetCallerClass(ee, skip);
    CVMObject* result;
    if (cb == NULL) {
        result = NULL;
    } else {
        result = CVMID_icellDirect(ee, CVMcbJavaInstance(cb));
    }
    CVMID_icellSetDirect(ee, &arguments[0].j.r, result);
    return CNI_SINGLE;
}

/*
 * Is the compiler built in?
 */
CNIResultCode
CNIsun_misc_CVM_isCompilerSupported(CVMExecEnv* ee,
				    CVMStackVal32 *arguments,
				    CVMMethodBlock **p_mb)
{
#ifdef CVM_JIT
    arguments[0].j.i = CVM_TRUE;
#else
    arguments[0].j.i = CVM_FALSE;
#endif
    return CNI_SINGLE;
}

/*
 * Request a dump of the profiling data collected by the compiler if available.
 */
CNIResultCode
CNIsun_misc_CVM_dumpCompilerProfileData(CVMExecEnv* ee,
				        CVMStackVal32 *arguments,
				        CVMMethodBlock **p_mb)
{
#if defined(CVM_JIT) && defined(CVM_JIT_PROFILE)
    CVMD_gcSafeExec(ee, {
        CVMJITcodeCacheDumpProfileData();
    });
#endif
    return CNI_VOID;
}

/*
 * Dump misc. stats
 */
CNIResultCode
CNIsun_misc_CVM_dumpStats(CVMExecEnv* ee,
			  CVMStackVal32 *arguments,
			  CVMMethodBlock **p_mb)
{
    /* Insert any stats you want here */
#ifdef CVM_USE_MEM_MGR
    /* Dump the dirty page info that the Memory Manager collected
     * for the monitored regions.
     */
    CVMmemManagerDumpStats();
#endif
    return CNI_VOID;
}

/*
 * Mark the code buffer
 */
CNIResultCode
CNIsun_misc_CVM_markCodeBuffer(CVMExecEnv* ee,
			       CVMStackVal32 *arguments,
			       CVMMethodBlock **p_mb)
{
#ifdef CVM_JIT
    CVMJITmarkCodeBuffer();
    CVMconsolePrintf("MARKED THIS JITBUFFER SPOT, %d BYTES IN USE\n",
		     CVMglobals.jit.codeCacheBytesAllocated);
#endif

#ifdef CVM_USE_MEM_MGR
    /* the .bss region */
    CVMmemRegisterBSS();
#endif /* CVM_USE_MEM_MGR */

    return CNI_VOID;
}

CNIResultCode
CNIsun_misc_CVM_maskInterrupts(CVMExecEnv* ee,
			       CVMStackVal32 *arguments,
			       CVMMethodBlock **p_mb)
{
    CVMD_gcSafeExec(ee, {
	arguments[0].j.i = CVMmaskInterrupts(ee);
    });
    return CNI_SINGLE;
}

CNIResultCode
CNIsun_misc_CVM_unmaskInterrupts(CVMExecEnv* ee,
			       CVMStackVal32 *arguments,
			       CVMMethodBlock **p_mb)
{
    CVMD_gcSafeExec(ee, {
	CVMunmaskInterrupts(ee);
    });
    return CNI_VOID;
}


CNIResultCode
CNIsun_misc_CVM_parseVerifyOptions(CVMExecEnv* ee, CVMStackVal32 *arguments,
				   CVMMethodBlock **p_mb)
{
    jobject opts  = &arguments[0].j.r;
    char* kind = CVMconvertJavaStringToCString(ee, opts);
    CVMBool result;
    if (kind == NULL) {
	result = CVM_FALSE;
    } else {
	int verification = CVMclassVerificationSpecToEncoding(kind);
	if (verification != CVM_VERIFY_UNRECOGNIZED) {
	    CVMglobals.classVerificationLevel = verification;
	    result = CVM_TRUE;
	} else {
	    result = CVM_FALSE;
	}
	free(kind);
    }
    arguments[0].j.i = result;
    return CNI_SINGLE;
}

CNIResultCode
CNIsun_misc_CVM_parseXoptOptions(CVMExecEnv* ee, CVMStackVal32 *arguments,
				   CVMMethodBlock **p_mb)
{
    jobject opts  = &arguments[0].j.r;
    char* kind = CVMconvertJavaStringToCString(ee, opts);
    CVMBool result;
    if (kind == NULL) {
	result = CVM_FALSE;
    } else {
	/* parse -Xopt options here */
	result = CVMoptParseXoptOptions(kind);
	free(kind);
    }

    arguments[0].j.i = result;
    return CNI_SINGLE;
}

CNIResultCode
CNIsun_misc_CVM_parseXssOption(CVMExecEnv* ee, CVMStackVal32 *arguments,
			       CVMMethodBlock **p_mb)
{
    jobject opts  = &arguments[0].j.r;
    char* kind = CVMconvertJavaStringToCString(ee, opts);
    CVMBool result;
    if (kind == NULL) {
	result = CVM_FALSE;
    } else {
	/* parse -Xss options here */
	result = CVMoptParseXssOption(kind);
	free(kind);
    }

    arguments[0].j.i = result;
    return CNI_SINGLE;
}

CNIResultCode
CNIsun_misc_CVM_parseXgcOptions(CVMExecEnv* ee, CVMStackVal32 *arguments,
				CVMMethodBlock **p_mb)
{
    jobject opts  = &arguments[0].j.r;
    char* kind = CVMconvertJavaStringToCString(ee, opts);
    CVMBool result;
    if (kind == NULL) {
	result = CVM_FALSE;
    } else {
	/* parse -Xgc options here */
#ifdef CVM_MTASK
	result = CVMgcParseXgcOptions(ee, kind);
#else
	result = CVM_FALSE;
#endif
	free(kind);
    }

    arguments[0].j.i = result;
    return CNI_SINGLE;
}

CNIResultCode
CNIsun_misc_CVM_parseAssertionOptions(CVMExecEnv* ee, CVMStackVal32 *arguments,
				      CVMMethodBlock **p_mb)
{
    jobject opts  = &arguments[0].j.r;
    char* str = CVMconvertJavaStringToCString(ee, opts);
    CVMBool result;
    if (str == NULL) {
	result = CVM_FALSE;
    } else {
	/* parse assertion related options here */
#ifdef CDC_10
	result = CVM_FALSE;
#else
	/* java assertion handling */
	if (!strncmp(str, "-ea:", 4)) {
	    CVMBool success = CVMJavaAssertions_addOption(
		str + 4, CVM_TRUE,
		&CVMglobals.javaAssertionsClasses,
		&CVMglobals.javaAssertionsPackages);
	    if (!success) goto addOption_failed;
	} else if (!strncmp(str, "-enableassertions:", 18)) {
	    CVMBool success = CVMJavaAssertions_addOption(
                str + 18, CVM_TRUE,
		&CVMglobals.javaAssertionsClasses,
		&CVMglobals.javaAssertionsPackages);
	    if (!success) goto addOption_failed;
	} else if (!strncmp(str, "-da:", 4)) {
	    CVMBool success = CVMJavaAssertions_addOption(
                str + 4, CVM_FALSE,
		&CVMglobals.javaAssertionsClasses,
		&CVMglobals.javaAssertionsPackages);
	    if (!success) goto addOption_failed;
	} else if (!strncmp(str, "-disableassertions:", 18)) {
	    CVMBool success;
	    success = CVMJavaAssertions_addOption(
                str + 19, CVM_FALSE,
		&CVMglobals.javaAssertionsClasses,
		&CVMglobals.javaAssertionsPackages);
            if (!success) {
	addOption_failed:
                CVMconsolePrintf("out of memory "
				 "while parsing assertion option\n");
		result = CVM_FALSE;
		goto done;
            }
	} else if (!strcmp(str, "-ea") |
		   !strcmp(str, "-enableassertions")) {
	    CVMglobals.javaAssertionsUserDefault = CVM_TRUE;
	} else if (!strcmp(str, "-da") |
		   !strcmp(str, "-disableassertions")) {
	    CVMglobals.javaAssertionsUserDefault = CVM_FALSE;
	} else if (!strcmp(str, "-esa") |
		   !strcmp(str, "-enablesystemassertions")) {
	    CVMglobals.javaAssertionsSysDefault = CVM_TRUE;
	} else if (!strcmp(str, "-dsa") |
		   !strcmp(str, "-disablesystemassertions")) {
	    CVMglobals.javaAssertionsSysDefault = CVM_FALSE;
	}
	result = CVM_TRUE;
#endif
done:
	free(str);
    }

    arguments[0].j.i = result;
    return CNI_SINGLE;
}

CNIResultCode
CNIsun_misc_CVM_xrunSupported(CVMExecEnv* ee,
			      CVMStackVal32 *arguments,
			      CVMMethodBlock **p_mb)
{
#ifdef CVM_XRUN
    arguments[0].j.i = CVM_TRUE;
#else
    arguments[0].j.i = CVM_FALSE;
#endif

    return CNI_SINGLE;
}


CNIResultCode
CNIsun_misc_CVM_xrunInitialize(CVMExecEnv* ee,
			      CVMStackVal32 *arguments,
			      CVMMethodBlock **p_mb)
{
#ifdef CVM_XRUN
    CVMJavaInt numArgs = arguments[0].j.i;
    
    if (CVMXrunInitTable(&CVMglobals.onUnloadTable, numArgs)) {
	arguments[0].j.i = CVM_TRUE;
    } else {
	arguments[0].j.i = CVM_FALSE;
    }
#else
    arguments[0].j.i = CVM_FALSE;
#endif

    return CNI_SINGLE;
}

CNIResultCode
CNIsun_misc_CVM_xrunProcess(CVMExecEnv* ee, CVMStackVal32 *arguments,
			    CVMMethodBlock **p_mb)
{
    jobject xrunArgStr  = &arguments[0].j.r;
    char* xrunArg = CVMconvertJavaStringToCString(ee, xrunArgStr);
    CVMBool result = CVM_FALSE;

    if (xrunArg == NULL) {
	result = CVM_FALSE;
    } else {
#ifdef CVM_XRUN
	JNIEnv* env = CVMexecEnv2JniEnv(ee);
	CVMD_gcSafeExec(ee, {
	    if ((*env)->PushLocalFrame(env, 16) == JNI_OK) {
  	        result = CVMXrunHandleArgument(&CVMglobals.onUnloadTable,
					       env,
					       xrunArg);
		(*env)->PopLocalFrame(env, NULL);
	    }
        });
#else
	result = CVM_FALSE;
#endif
	free(xrunArg);
    }

    arguments[0].j.i = result;
    return CNI_SINGLE;
}

CNIResultCode
CNIsun_misc_CVM_xdebugSet(CVMExecEnv* ee, CVMStackVal32 *arguments,
			  CVMMethodBlock **p_mb)
{
#ifdef CVM_JVMDI
    CVMglobals.jvmdiDebuggingEnabled = CVM_TRUE;
    CVMjvmdiInstrumentJNINativeInterface();
    arguments[0].j.i = CVM_TRUE;
#else
    arguments[0].j.i = CVM_FALSE;
#endif

    return CNI_SINGLE;
}

