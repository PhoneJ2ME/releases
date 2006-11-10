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

#ifndef _INCLUDED_INTERPRETER_H
#define _INCLUDED_INTERPRETER_H

#include "javavm/include/defs.h"
#include "javavm/include/cni.h"
#include "javavm/include/stacks.h"
#include "javavm/include/sync.h"
#include "javavm/include/cstates.h"
#include "javavm/include/jni_impl.h"
#include "javavm/include/gc_common.h"
#include "javavm/include/porting/threads.h"
#include "javavm/include/porting/vm-defs.h"

#ifdef CVM_JIT
#include "javavm/include/jit_common.h"
#include "javavm/include/porting/jit/jit.h"
#endif

#ifdef CVM_LVM /* %begin lvm */
#include "javavm/include/lvm.h"
#endif /* %end lvm */


#define CVMJAVAPKG "java/lang/"

typedef enum {
    CVM_THREAD_RUNNING = 0x0,
    CVM_THREAD_SUSPENDED = 0x100
} CVMThreadState;

/*
 * Per-thread C stack global pool operations and macros
 */
#define CVM_CSTACK_BUF_SIZE     8192    /* Fixed size of the EE buffer used by
                                         * ZipFile and RandomAccess IO read 
				 	 * and write operation. */

#define CVMCstackBuffer(ee)              ((ee)->cstackBuffer)
#define CVMCstackBufferFlag(ee)          ((ee)->cstackBufferFlag)
#define CVMCstackBufferIsNull(ee)        ((ee)->cstackBuffer == NULL)
#define CVMCstackBufferIsSet(ee)         ((ee)->cstackBufferFlag)

/* 
 * The flag that tells the status of the EE-buffer is initially unset 
 * when a thread is created 
 */
#define CVMCstackBufferInit(ee) {					\
   (ee)->cstackBufferFlag = CVM_FALSE;					\
 }		

/* 
 * Return C stack global buffer in ee if exists.
 * Otherwise, allocate C stack global buffer in ee.
 * Assert that the flag is not set before using the EE-buffer.
 * The flag is set when the buffer allocated successfully. Otherwise,
 * it remains unset.
 */
#define CVMCstackGetBuffer(ee, buf) {					\
    CVMassert(!CVMCstackBufferIsSet(ee));				\
    if (CVMCstackBufferIsNull(ee)) {					\
        CVMCstackBuffer(ee) = (char *)malloc(CVM_CSTACK_BUF_SIZE); 	\
    }									\
    buf = CVMCstackBuffer(ee);						\
    if (buf != NULL) {							\
    	CVMCstackBufferFlag(ee) = CVM_TRUE;				\
    }									\
 }

/* 
 * Deallocate the EE-buffer when a thread is killed.
 * Assert that the flag is not set before freeing the EE-buffer.
 */
#define CVMCstackFreeBuffer(ee) {   					\
    CVMassert(!CVMCstackBufferIsSet(ee));				\
    if (!CVMCstackBufferIsNull(ee)) 					\
        free(CVMCstackBuffer(ee));  					\
 }

/* 
 * Release the EE-buffer at the end of the buffer operation.
 * Assert that the flag is set before it's released.
 */
#define CVMCstackReleaseBuffer(ee) {   					\
    CVMassert(CVMCstackBufferIsSet(ee));				\
    CVMCstackBufferFlag(ee) = CVM_FALSE;				\
 }

/*
 * States for isHandlingAnException.
 */
typedef enum {
    CVM_EXCEPTION_NONE = 0, /* no exception handling in progress */
    CVM_EXCEPTION_TOP,	    /* exception thrown in current frame */
    CVM_EXCEPTION_UNWINDING /* stack unwinding in progress */
}CVMExceptionState;

/****************************************************************************
 * CVMExecEnv
 *
 * CVMExecEnv is the per thread execution context.
 ****************************************************************************/

struct CVMExecEnv {
    /* per-thread data for consistent states */
    CVMTCState tcstate[CVM_NUM_CONSISTENT_STATES];

    CVMUint8           isThrowingAnException;
    CVMUint8           isHandlingAnException;
    CVMUint16          remoteExceptionsDisabledCount;
    CVMThrowableICell* localExceptionICell;  /* local exception object */
    CVMThrowableICell* remoteExceptionICell; /* remote exception object */
    CVMThrowableICell* currentExceptionICell;/* exception being processed */
    union {
	struct {
	    CVMUint8 remote;    /* async, like from Thread.stop() */
	    CVMUint8 local;     /* local to the interpreter */
	} oneflag;
	CVMUint16 bothflags;    /* for checking both flags at one time */
    } exceptionFlags;

    CVMThreadICell* threadICell;/* back-pointer to thread object */
    CVMObjectICell* miscICell;  /* Per-thread root for miscellaneous use */
    CVMObjectICell* syncICell;  /* Per-thread root for the sync code use. */

    /*
     * CVMgcAllocNewInstance() needs a root to hold the newly allocated object
     * in if it has to call Finalizer.register() on that object.
     */
    CVMObjectICell* finalizerRegisterICell;

    CVMJNIEnv jniEnv;		/* per-thread JNI data */

    /* linked-list of EE's */
    CVMExecEnv **prevEEPtr;
    CVMExecEnv *nextEE;

    CVMStack  interpreterStack; /* stack for interpreter frames */
    CVMStack  localRootsStack;  /* stack for local root frames */

    CVMBool cstackBufferFlag;   /* flag to tell the EE buffer is in use */
    char   *cstackBuffer;	/* buffer for reading bytes of data from
				 * input stream into a global pool for
				 * Java_java_io_FileInputStream_readBytes
				 * Java_java_io_RandomAccessFile_readBytes */

    void * nativeRunInfo;	/* for "system" threads */

    /* NOTE: objLockCurrent could be replaced by an atomically-updated
       integer reference count */
    CVMObjMonitor * volatile objLockCurrent; /* being referenced, set while unsafe */
    CVMObjMonitor *objLocksFreeUnlocked;
    CVMOwnedMonitor *objLocksOwned; /* list of all CVMObjMonitor locks owned */
    CVMOwnedMonitor *objLocksFreeOwned;
    /* pre-allocated memory for thread shutdown */
    CVMBool threadExiting;
    CVMOwnedMonitor *objLocksReservedOwned;
    CVMObjMonitor *objLocksReservedUnlocked;

#ifdef CVM_JIT
    CVMMethodBlock* invokeMb; /* method currently being invoked */
    CVMInt32        noOSRSkip;
    CVMInt8         noOSRStackAdjust;
    CVMBool         noCompilations; /* if true, thread can't do compilations */
#endif

    CVMThreadID threadInfo;	/* platform-specific thread info */
    CVMThreadState threadState;
    CVMUint32 threadID;

#ifdef CVM_DEBUG_ASSERTS
    int nativeRunInfoType;
#endif

#ifdef CVM_DEBUG
    CVMSysMutex *sysLocks;	/* list of all CVMSysMutex locks held */
    int microLock;		/* micro lock depth */
#endif

    CVMBool userThread;		/* true if not a daemon thread */

    /* For GC-safe returns of the results of allocation retries. */
    CVMObjectICell* allocationRetryICell;

    /* for tracking nesting of CVMD_gcEnterCriticalRegion calls */
    CVMUint32 criticalCount;

    CVMBool hasPostedExitEvents;
#ifdef CVM_JVMDI
    CVMBool debugEventsEnabled;
    CVMBool jvmdiSingleStepping;
#endif
#ifdef CVM_JVMPI
    CVMProfiledMonitor *blockingLockEntryMonitor;
    CVMProfiledMonitor *blockingWaitMonitor;
    void *jvmpiProfilerData;    /* JVMPI Profiler thread-local data. */
    CVMBool hasRun;     /* Has this thread run since its last suspension? */
#endif

#ifdef CVM_TEST_GC
    CVMBarrierType barrier;
#endif

#ifdef CVM_LVM /* %begin lvm */
    CVMLVMPerEEInfo lvmInfo;	/* Info on LVM to which this EE belongs */
#endif /* %end lvm */

#ifdef CVM_TRACE_ENABLED
#ifdef CVM_PROFILE_METHOD
    CVMInt64 t0;
    CVMMethodBlock *cmb;
    int cindx;
    int traceEra;
#endif
#endif

    CVMBool interruptsMasked;	/* See CVM.maskInterrupts() */
    CVMBool maskedInterrupt;	/* Was Thread.interrupt() called while
				   interrupts were masked? */

    /* pinned object monitors during thread shutdown */
    /* 16 is overkill.  To find the minimum number, build */
    /* with CVM_FASTLOCK_TYPE set to CVM_FASTLOCK_NONE */
#define CVM_PINNED_OBJMON_COUNT 16
    CVMObjMonitor *objLocksPinned[CVM_PINNED_OBJMON_COUNT];
    CVMSize objLocksPinnedCount;
};

/* 
 * This structure is used to pass info from a parent thread to a child.
 * This is actually the (void *) argument passed to start_func through
 * CVMthreadCreate. This data structure is required to be allocated
 * using malloc() by the caller; start_func is responsible for
 * freeing it. 
 */
typedef struct {
    /* This is required to be non-NULL and to point to a valid global
     * root which contains a java.lang.Thread object. start_func is
     * responsible for holding on to the Thread object (by storing it
     * in the new thread's execution environment's currentThreadICell)
     * and deallocating the global root. */
    CVMThreadICell* threadICell;
    /* This function pointer must be NULL for all Java threads (i.e.,
     * those started by a call to JVM_StartThread). The JVMDI and
     * JVMPI, however, need to be able to create "system threads",
     * which are arbitrary function pointers which execute in the
     * context of an CVMExecEnv, so they can make JNI calls. */
    void (*nativeFunc)(void *);
    /* This must be NULL for non-system threads. For system threads,
     * if this argument is non-NULL, it must be allocated on the heap
     * by the caller. start_func does not free this argument, only
     * passes it down to the native function. */
    void* nativeFuncArg;
    CVMBool isDaemon;
#ifdef CVM_LVM /* %begin lvm */
    CVMLVMEEInitAux lvmEEInitAux; /* for passing Logical VM related info */
#endif /* %end lvm */
    /* died an early death? */
    int started;
    /* Synchronize child startup */
    CVMMutex parentLock;
    CVMCondVar parentCond;
    /* EE for child */
    CVMExecEnv *ee;
    int priority;
} CVMThreadStartInfo;

#define CVM_TCSTATE(ee, x)	(&(ee)->tcstate[(x)])

#define CVMjniEnv2ExecEnv(je)					\
	((CVMExecEnv *)((char *)(je) - 				\
	    CVMoffsetof(CVMExecEnv, jniEnv)))

#define CVMexecEnv2JniEnv(ee)	CVMjniPrivEnv2PubEnv(&(ee)->jniEnv)

#define CVMthreadID2ExecEnv(tid)					\
	((CVMExecEnv *)((char *)((tid)) - 				\
	    CVMoffsetof(CVMExecEnv, threadInfo)))

#define CVMexecEnv2threadID(ee)	(&(ee)->threadInfo)

#define CVMeeGetCurrentFrame(ee) \
     ((ee)->interpreterStack.currentFrame)

/* Get the mb of the current frame. */
#ifdef CVM_JIT
#define CVMeeGetCurrentFrameMb(ee)    CVMJITeeGetCurrentFrameMb((ee))
#else
#define CVMeeGetCurrentFrameMb(ee)    (CVMeeGetCurrentFrame((ee))->mb)
#endif

/*
 * Get the mb of the given frame at the saved pc.
 * This also works for frames with inlined methods.
 */
#ifdef CVM_JIT
#define CVMframeGetMb(frame)		CVMJITframeGetMb((frame))
#else
#define CVMframeGetMb(frame)		((frame)->mb)
#endif

/* Get the cb of the current frame. */
#define CVMeeGetCurrentFrameCb(ee) \
   CVMmbClassBlock(CVMeeGetCurrentFrameMb(ee))

/* Get the constant pool of the current frame. */
#define CVMeeGetCurrentFrameCp(ee) \
   CVMcbConstantPool(CVMeeGetCurrentFrameCb(ee))


/*
 * Initialize an ExecEnv before first use, and destroy after use.
 * If CVMinitExecEnv fails, it returns CVM_FALSE. An exception is
 * NOT thrown.
 */
extern CVMBool
CVMinitExecEnv(CVMExecEnv* ee, CVMExecEnv *targetEE,
	       CVMThreadStartInfo* threadInfo);
extern void
CVMdestroyExecEnv(CVMExecEnv* ee);


extern CVMBool
CVMattachExecEnv(CVMExecEnv* ee, CVMBool orphan);
extern void 
CVMdetachExecEnv(CVMExecEnv* ee);

extern CVMExecEnv * CVMgetEE();

extern void CVMaddThread(CVMExecEnv *ee, CVMBool userThread);
extern void CVMremoveThread(CVMExecEnv *ee, CVMBool userThread);

/*
 * Macros for iterating over all threads. For efficiency, the caller is
 * responsible for seizing CVMglobals.threadLock; this macro checks to
 * ensure that it is seized. **WARNING**: the action may NOT cause
 * allocation of Java objects (which might trigger a GC). GC will
 * attempt to seize the threads lock, which could result in a
 * deadlock. NOTE: maybe this should go in a separate threads.h, but it
 * doesn't seem to belong in src/share/javavm/include/porting/threads.h. 
 */

#define CVM_WALK_ALL_THREADS_START(ee, eeNameInAction) {   \
   CVMExecEnv* eeNameInAction = CVMglobals.threadList;	   \
   CVMassert(CVMsysMutexIAmOwner(ee, &CVMglobals.threadLock)); \
   while (eeNameInAction != NULL) {                        \

#define CVM_WALK_ALL_THREADS_END(ee, eeNameInAction) 	   \
     eeNameInAction = eeNameInAction->nextEE;              \
   }                                                       \
 }

#define CVM_WALK_ALL_THREADS(ee, eeNameInAction, action)   \
    CVM_WALK_ALL_THREADS_START((ee), (eeNameInAction))	   \
    action;						   \
    CVM_WALK_ALL_THREADS_END((ee), (eeNameInAction))

/*
 * Per-thread root cell for miscellaneous use
 */
#define CVMmiscICell(ee)   ((ee)->miscICell)

/*
 * Per-thread root cell for sync code use
 */
#define CVMsyncICell(ee)   ((ee)->syncICell)

/*
 * Per-thread root cell used when calling Finalizer.register().
 */
#define CVMfinalizerRegisterICell(ee)   ((ee)->finalizerRegisterICell)

/*
 * Per-thread root cell used when retrying allocation
 */
#define CVMallocationRetryICell(ee)   ((ee)->allocationRetryICell)

/*
 * Be sure to use these macros to access the exception fields.  Do
 * not access the fields directly.
 */

#define CVMlocalExceptionICell(ee)   ((ee)->localExceptionICell)
#define CVMremoteExceptionICell(ee)  ((ee)->remoteExceptionICell)
#define CVMcurrentExceptionICell(ee) ((ee)->currentExceptionICell)

#define CVMgetLocalExceptionObj(ee)   \
     (CVMID_icellDirect(ee, CVMlocalExceptionICell(ee)))
#define CVMgetRemoteExceptionObj(ee)  \
     (CVMID_icellDirect(ee, CVMremoteExceptionICell(ee)))
#define CVMgetCurrentExceptionObj(ee) \
     (CVMID_icellDirect(ee, CVMcurrentExceptionICell(ee)))

#define CVMsetLocalExceptionObj(ee, obj_)   \
     (ee)->isThrowingAnException = CVM_TRUE; \
     CVMID_icellSetDirect(ee, CVMlocalExceptionICell(ee), obj_);
#define CVMsetRemoteExceptionObj(ee, targetEE, obj_)  \
     CVMID_icellSetDirect(ee, CVMremoteExceptionICell(targetEE), obj_);
#define CVMsetCurrentExceptionObj(ee, obj_) \
     CVMID_icellSetDirect(ee, CVMcurrentExceptionICell(ee), obj_);

#define CVMlocalExceptionFlag(ee)  ((ee)->exceptionFlags.oneflag.local)
#define CVMremoteExceptionFlag(ee) ((ee)->exceptionFlags.oneflag.remote)

#define CVMgcSafeThrowLocalExceptionNoDumpStack(ee, objICell)	\
    CVMD_gcUnsafeExec(ee, {					\
	CVMgcUnsafeThrowLocalExceptionNoDumpStack(		\
	    ee, CVMID_icellDirect(ee, objICell)); 		\
    });

#define CVMgcUnsafeThrowLocalExceptionNoDumpStack(ee, obj) {	\
    CVMassert(!CVMlocalExceptionOccurred(ee));			\
    CVMsetLocalExceptionObj(ee, obj);				\
    CVMlocalExceptionFlag(ee) = 1;				\
}

#define CVMgcSafeThrowLocalException(ee, objICell)			\
    CVMD_gcUnsafeExec(ee, {						\
	CVMgcUnsafeThrowLocalException(ee, CVMID_icellDirect(ee, objICell)); \
    });

#ifdef CVM_TRACE
#define CVMgcUnsafeThrowLocalException(ee, obj)	{			\
    CVMassert(!CVMlocalExceptionOccurred(ee));				\
    CVMsetLocalExceptionObj(ee, obj);					\
    CVMlocalExceptionFlag(ee) = 1;					\
    CVMtraceExceptions(("[<%d> Exception thrown: %O]\n",		\
			ee->threadID, obj));				\
    if (CVMcheckDebugFlags(CVM_DEBUGFLAG(TRACE_EXCEPTIONS))) {		\
	CVMdumpStack(&ee->interpreterStack, CVM_FALSE, CVM_FALSE, 100);	\
    }									\
}
#else
#define CVMgcUnsafeThrowLocalException(ee, obj)	{		\
    CVMassert(!CVMlocalExceptionOccurred(ee));			\
    CVMsetLocalExceptionObj(ee, obj);				\
    CVMlocalExceptionFlag(ee) = 1;				\
}
#endif

#ifdef CVM_REMOTE_EXCEPTIONS_SUPPORTED
#define CVMgcSafeThrowRemoteException(ee, targetEE, objICell)	\
    CVMD_gcUnsafeExec(ee, {					\
	CVMgcUnsafeThrowRemoteException(ee, targetEE,		\
	    CVMID_icellDirect(ee, objICell));			\
    });
#else
#define CVMgcSafeThrowRemoteException(ee, targetEE, objICell) \
    CVMassert(CVM_FALSE);
#endif

#ifdef CVM_REMOTE_EXCEPTIONS_SUPPORTED
#ifdef CVM_TRACE
#define CVMgcUnsafeThrowRemoteException(ee, targetEE, obj) {		      \
    CVMsetRemoteExceptionObj(ee, targetEE, obj);			      \
    CVMremoteExceptionFlag(targetEE) = 1;				      \
    CVMtraceExceptions(("[<%d> Remote Exception thrown from <%d>: %O]\n",     \
			targetEE->threadID, ee->threadID, obj));	      \
    if (CVMcheckDebugFlags(CVM_DEBUGFLAG(TRACE_EXCEPTIONS))) {		      \
        CVMtraceExceptions(("Thread <%d> stack dump follows:\n",	      \
			    targetEE->threadID));			      \
	CVMdumpStack(&targetEE->interpreterStack, CVM_FALSE, CVM_FALSE, 100); \
        CVMtraceExceptions(("Thread <%d> stack dump follows:\n",	      \
			    ee->threadID));				      \
	CVMdumpStack(&ee->interpreterStack, CVM_FALSE, CVM_FALSE, 100);	      \
    }									      \
}
#else
#define CVMgcUnsafeThrowRemoteException(ee, targetEE, obj) {	\
    CVMsetRemoteExceptionObj(ee, targetEE, obj);		\
    CVMremoteExceptionFlag(targetEE) = 1;			\
}
#endif
#else
#define CVMgcUnsafeThrowRemoteException(ee, targetEE, obj) \
    CVMassert(CVM_FALSE);
#endif

#define CVMclearLocalException(ee) {			\
    CVMID_icellSetNull(CVMlocalExceptionICell(ee));	\
    CVMlocalExceptionFlag(ee) = 0; 			\
    (ee)->isThrowingAnException = CVM_FALSE;            \
}
#ifdef CVM_REMOTE_EXCEPTIONS_SUPPORTED
#define CVMclearRemoteException(ee) {			\
    CVMID_icellSetNull(CVMremoteExceptionICell(ee));	\
    CVMremoteExceptionFlag(ee) = 0;			\
}
#else
#define CVMclearRemoteException(ee)
#endif

#define CVMexceptionOccurred(ee)					 \
    ((ee)->exceptionFlags.bothflags != 0 &&				 \
     (CVMlocalExceptionOccurred(ee) || CVMremoteExceptionOccurred(ee)))
#define CVMlocalExceptionOccurred(ee)		\
    (CVMlocalExceptionFlag(ee) != 0)
#ifdef CVM_REMOTE_EXCEPTIONS_SUPPORTED
#define CVMremoteExceptionOccurred(ee)		\
    (CVMremoteExceptionFlag(ee) != 0 && !CVMremoteExceptionsDisabled(ee))
#else
#define CVMremoteExceptionOccurred(ee) (CVM_FALSE)
#endif

#define CVMexceptionIsBeingThrownInFrame(ee, frame) \
    (((ee)->isThrowingAnException && (frame == CVMeeGetCurrentFrame(ee))) || \
     (((frame)->flags & CVM_FRAMEFLAG_EXCEPTION) != 0))

#define CVMcurrentThreadICell(ee) ((ee)->threadICell)

/* NOTE: CVMeeMarkHasRun(ee) is used to mark the specified thread has run
    since the last JVMPI suspension of that thread.  This is needed to
    support the JVMPI ThreadHasRun() query: */
#ifdef CVM_JVMPI

/* Purpose: Mark the specified thread as having run. */
#define CVMeeMarkHasRun(ee)     ((ee)->hasRun = CVM_TRUE)

/* Purpose: Reset the hasRun flag of the specified thread.  This is done
            immediately after the thread is suspended. */
#define CVMeeResetHasRun(ee)    ((ee)->hasRun = CVM_FALSE)

/* Purpose: Checks to see if the specified thread has run. */
#define CVMeeHasRun(ee)         ((ee)->hasRun)

#else
#define CVMeeMarkHasRun(ee)     ((void)0) /* Null expression. */
#endif /* CVM_JVMPI */

/**********************************************************************
 * Interpreter Frames:
 **********************************************************************/

/*
 * The CVMInterpreterFrame is used to keep track of java method calls. Note
 * that local variables (including arguments) are stored right in front of the
 * frame, so locals points to (frame - CVMjmdMaxLocals(frame->mb). However,
 * a possible optimization might allow the locals to be in the previous
 * CVMStackChunk if they will fit there but the entire frame will not,
 * but this would also make gc scans more difficult.
 */
struct CVMInterpreterFrame {
    CVMFrame         frameX;
    CVMUint8*        pcX;     /* pointer to the current instruction */

    /* The following fields are not used by transition frames, but making
     * them common to both CVMJavaFrame and CVMTransitionFrame allows us
     * to avoid checking what type of frame we are returning to when
     * an opc_return is executed. In other words, we waste 8 bytes in
     * transition frames in order to make all returns faster.
     *
     * If you really want to save memory, constantPool and
     * locals can be eliminated at the cost of more instructions
     * to recalculate when returning from a method invocation.
     * constantPool can be accessed via the CVMMethodBlock. locals can
     * be calculated using the maxLocals of the CVMMethodBlock of the
     * previous frame.
     */
    CVMConstantPool* cpX;     /* our constant pool */
    CVMSlotVal32*    localsX; /* pointer to the local variables. */
};

/* CVMJavaFrame - used for real java methods */
struct CVMJavaFrame {
    CVMInterpreterFrame	frameX;

    CVMObjectICell    receiverObjX; /* the object we are dispatching on or
				     * the Class object for static calls */
    CVMStackVal32     opstackX[1];  /* the operand stack */
};

/* CVMTransitionFrame - used for "stub" java methods */
struct CVMTransitionFrame {
    CVMInterpreterFrame	frameX;
    CVMBool       incrementPcFlagX; /* flag that indicates if the pc should
				     * be incremented by opc_exittransition */
    CVMStackVal32 opstackX[1];      /* the operand stack */
};

#ifdef CVM_JIT

struct CVMCompiledFrame {
    CVMFrame         frameX;
    CVMUint8*        pcX;	   /* pointer to the current instruction */

    CVMObjectICell   receiverObjX; /* the object we are dispatching on or
				    * the Class object for static calls */
#ifdef CVMCPU_HAS_CP_REG
    void*            cpBaseRegX;   /* base register for the constant pool */
#endif
    CVMStackVal32    opstackX[1];  /* the operand stack */
};

#define CVM_COMPILEDFRAME_SIZE	  (CVMoffsetof(CVMCompiledFrame, opstackX))

#endif

#define CVM_JAVAFRAME_SIZE 	  (CVMoffsetof(CVMJavaFrame, opstackX))
#define CVM_TRANSITIONFRAME_SIZE  (CVMoffsetof(CVMTransitionFrame, opstackX))

#define CVMframePc(frame)      (CVMgetInterpreterFrame(frame)->pcX)
#define CVMframeCp(frame)      (CVMgetInterpreterFrame(frame)->cpX)
#define CVMframeLocals(frame)  (CVMgetInterpreterFrame(frame)->localsX)
#define CVMframeReceiverObj(frame, frameType)	\
                               (CVMget##frameType##Frame(frame)->receiverObjX)
#define CVMframeIncrementPcFlag(frame) \
                               (CVMgetTransitionFrame(frame)->incrementPcFlagX)
#define CVMframeOpstack(frame, frameType) \
                               (CVMget##frameType##Frame(frame)->opstackX)

#define CVMframeIsEmpty(frame, topOfStack, frameType) \
        (topOfStack == CVMframeOpstack(frame, frameType))

#define CVMgetInterpreterFrame0(frame)					\
    (CVMassert(CVMframeIsJava(frame) || CVMframeIsTransition(frame)),	\
     (CVMInterpreterFrame*)(frame))
#ifdef CVM_DEBUG_ASSERTS
extern CVMInterpreterFrame *CVMDEBUGgetInterpreterFrame(CVMFrame *frame);
#define CVMgetInterpreterFrame(frame) \
    CVMDEBUGgetInterpreterFrame((CVMFrame *)(frame))
#else
#define CVMgetInterpreterFrame CVMgetInterpreterFrame0
#endif
#define CVMgetJavaFrame(frame) \
    (CVMassert(CVMframeIsJava(frame)), (CVMJavaFrame*)(frame))
#define CVMgetTransitionFrame(frame) \
    (CVMassert(CVMframeIsTransition(frame)), (CVMTransitionFrame*)(frame))
#define CVMgetFreelistFrame(frame) \
    (CVMassert(CVMframeIsFreelist(frame)), (CVMFreelistFrame*)(frame))

#ifdef CVM_JIT

#define CVMgetCompiledFrame(frame)	\
    (CVMassert(CVMframeIsCompiled(frame)), (CVMCompiledFrame*)(frame))

#define CVMcompiledFrameMB(frame) \
    (CVMassert(CVMframeIsCompiled(frame)), ((CVMFrame*)(frame))->mb)

#define CVMcompiledFramePC(frame) \
    (CVMgetCompiledFrame(frame)->pcX)

#define CVMcompiledFrameCpBaseReg(frame) \
    (CVMgetCompiledFrame(frame)->cpBaseRegX)

#endif

#define CVMframeType(frame)						\
    (CVMassert(((CVMFrame*)(frame))->type != CVM_FRAMETYPE_NONE),	\
	(((CVMFrame*)(frame))->type))

#define CVMframeIsInterpreter(frame)					\
    (CVMframeIsJava(frame) || CVMframeIsTransition(frame))
#define CVMframeIsJava(frame) \
    (CVMframeType((frame)) == CVM_FRAMETYPE_JAVA)
#define CVMframeIsTransition(frame)               \
    (CVMframeType((frame)) == CVM_FRAMETYPE_TRANSITION)
#define CVMframeIsFreelist(frame)               \
    (CVMframeType((frame)) == CVM_FRAMETYPE_FREELIST)

#ifdef CVM_JIT

#define CVMframeIsCompiled(frame)		\
    (CVMframeType((frame)) == CVM_FRAMETYPE_COMPILED)

#endif

/*
 * Reset the topOfStack to point to the beginning of the operand stack.
 */
#define CVM_RESET_INTERPRETER_TOS(topOfStack, frame, isJava)	\
{								\
    if (isJava) {						\
	CVM_RESET_JAVA_TOS(topOfStack, frame);			\
    } else {							\
	CVM_RESET_TRANSITION_TOS(topOfStack, frame);		\
    }								\
}
#define CVM_RESET_JAVA_TOS(topOfStack, frame)		\
    (topOfStack) = CVMframeOpstack(frame, Java);
#define CVM_RESET_TRANSITION_TOS(topOfStack, frame)	\
    (topOfStack) = CVMframeOpstack(frame, Transition);
#define CVM_RESET_COMPILED_TOS(topOfStack, frame)	\
    (topOfStack) = CVMframeOpstack(frame, Compiled)

/**********
 * APIs
 *********/

/*
 * CVMgcUnsafeExecuteJavaMethod - the main entry point for interpreting
 * java bytecodes.
 */
extern void
CVMgcUnsafeExecuteJavaMethod(CVMExecEnv* volatile ee, CVMMethodBlock* mb,
			     CVMBool isStatic, CVMBool isVirtual);

/*
 * CVMgcUnsafeExecuteJavaMethod - the second level interpreter loop
 *
 * Take a snapshot of interpreter state as argument.
 * Return value is the current PC, and the current SP in *retTos.
 *
 * if (PC == NULL), *retTos is a ClassBlock* which needs to be initialized
 * if (PC != NULL), execution continues from (PC, SP), unless an exception is
 *     pending.
 */
extern CVMUint8* CVMgcUnsafeExecuteJavaMethodQuick(CVMExecEnv* ee,
    CVMUint8* pc, CVMStackVal32* topOfStack, CVMSlotVal32* locals,
    CVMConstantPool* cp, CVMClassBlock** retCb);

/*
 * Functions used to display information about a given pc for debug builds.
 */
#ifdef CVM_DEBUG_CLASSINFO
extern CVMInt32
CVMpc2lineno(CVMMethodBlock *mb, CVMUint16 pc_offset);
#endif
#if defined(CVM_TRACE) || defined(CVM_DEBUG) || defined(CVM_DEBUG_DUMPSTACK) || defined(CVM_DEBUG_STACKTRACES)
extern void
CVMframe2string(CVMFrame* frame, char *buf, char* limit);
extern void
CVMframeIterate2string(CVMFrameIterator* frame, char *buf, char* limit);
extern void
CVMpc2string(CVMUint8* pc, CVMMethodBlock* mb, 
	     CVMBool isTransition, CVMBool isCompiled, char *buf, char* limit);
extern void
CVMlineno2string(CVMInt32 lineno, CVMMethodBlock* mb, 
	     CVMBool isTransition, CVMBool isCompiled, char *buf, char* limit);
#endif

/*
 * Return the pc of the exception handler for the specified exception.
 */
extern CVMUint8*
CVMgcSafeFindPCForException(CVMExecEnv* ee, CVMFrameIterator* frame, 
			    CVMClassBlock* exceptionClass, CVMUint8* pc);

/*
 * CVMgcUnsafeHandleException - handle an outstanding exception.
 */
extern CVMFrame*
CVMgcUnsafeHandleException(CVMExecEnv* ee, CVMFrame* frame, 
			   CVMFrame* initialframe);

/*
 * CVMsignalError - signal an exception. The exception message can
 * be a format string followed by arguments just like printf.
 */
extern void
CVMsignalError(CVMExecEnv* ee, CVMClassBlock* exceptionCb,
	       const char *format, ...);

extern void
CVMsignalErrorVaList(CVMExecEnv* ee, CVMClassBlock* exceptionCb, 
		     const char* format, va_list ap);

/*
 * CVMpushTransitionFrame - push a transition frame.
 */
extern CVMTransitionFrame*
CVMpushTransitionFrame(CVMExecEnv* ee, CVMMethodBlock* mb);

/*
 * Ensure space for and perform stackmap allocations for a given frame
 *
 * CVM_TRUE  if stackmaps successfully created
 * CVM_FALSE on error
 */
/* NOTE: The frameEE is the CVMExecEnv of the thread which owns the frame for
         which we want to get a stackmap.  It is not necessarily the CVMExecEnv
         of the self (i.e. currently executing) thread.
*/
extern CVMBool
CVMjavaFrameEnsureStackmaps(CVMExecEnv *ee, CVMExecEnv *frameEE,
                            CVMFrame *frame);

typedef struct {
    CVMExecEnv *targetEE;
    CVMFrame *prevFrame;
    void *callbackData;
} CVMInterpreterStackData;

/*
 * Find the innermost exception handler for a PC, for stackmap purposes only.
 */
CVMUint8*
CVMfindInnermostHandlerFor(CVMJavaMethodDescriptor* jmd, CVMUint8* pc);
  
/*
 * Get the classblock of the caller of the current method in the
 * execution environment. If SKIP is true then the current method is a
 * constructor and should be ignored; that is, we should go back to
 * the previous frame. Automatically skips transition,
 * Method.invoke(), Constructor.newInstance(), and Class.newInstance()
 * frames; see CVMgetCallerFrame, below. This is equivalent to
 * getCallerClass (jvm.c) in JDK 1.2 but was moved because it was
 * inappropriately placed there.
 */
CVMClassBlock* CVMgetCallerClass(CVMExecEnv* ee, int skip);

/*
 * Step back n frames, skipping Method.invoke(),
 * Constructor.newInstance(), Class.newInstance(), and transition
 * frames in between. This function is used in Class.forName,
 * Class.newInstance, Method.Invoke, AccessController.doPrivileged.
 * This is equivalent to getCallerFrame (jvm.c) in JDK 1.2 but was
 * moved because it was inappropriately placed there. NOTE that the
 * JDK 1.2 code and the Exact VM code have a third argument which is
 * needed in the case of compiled code, but this has been temporarily
 * removed.
 */
#define CVMgetCallerFrame(f, n) \
	CVMgetCallerFrameSpecial((f), (n), CVM_TRUE)

/*
 * Like CVMgetCallerFrame() above, but only skips special frames
 * if the "skipSpecial" flag is set.
 */
CVMFrame*
CVMgetCallerFrameSpecial(CVMFrame* frame, int n, CVMBool skipSpecial);

struct CVMFrameIterator {
    CVMStack *stack;
    CVMFrame *endFrame;
    CVMFrame *frame;
    CVMFrame *next;
#ifdef CVM_JIT
    CVMBool jitFrame;
    CVMJITFrameIterator jit;
#endif
};

/*
 * "frame" is the start frame.  Iteration proceeds to and includes
 * "endFrame", so specifying endFrame == frame will scan only the
 * current frame (but including inlined frames).  Currently,
 * "stack" is only used to support "popFrame" below.
 */
void
CVMframeIterateSpecial(CVMStack *stack, CVMFrame* frame,
    CVMFrame *endFrame, CVMFrameIterator *iter);

void
CVMframeIterate(CVMFrame* frame, CVMFrameIterator *iter);

/*
 * "skip" is how many extra frames to skip.  Use skip==0 to see
 * every frame.  To skip special reflection frames, set skipSpecial
 * true.  "popFrame" is used by exception handling to pop frames
 * as it iterates.
 */
CVMBool
CVMframeIterateSkipSpecial(CVMFrameIterator *iter,
    int skip, CVMBool skipSpecial, CVMBool popFrame);

#define CVMframeIterateSkip(iter, skip) \
    CVMframeIterateSkipSpecial((iter), (skip), CVM_TRUE, CVM_FALSE)

CVMUint32
CVMframeIterateCount(CVMFrameIterator *iter);

#define CVMframeIterateNextSpecial(iter, skipSpecial) \
    CVMframeIterateSkipSpecial((iter), 0, (skipSpecial), CVM_FALSE)

#define CVMframeIterateNext(iter) \
    CVMframeIterateNextSpecial((iter), CVM_TRUE)

#define CVMframeIteratePopSpecial(iter, skipSpecial) \
   CVMframeIterateSkipSpecial((iter), 0, (skipSpecial), CVM_TRUE)

#define CVMframeIteratePop(iter, skipSpecial) \
   CVMframeIteratePopSpecial((iter), CVM_TRUE)

CVMBool
CVMframeIterateIsInlined(CVMFrameIterator *iter);

CVMBool
CVMframeIterateHandlesExceptions(CVMFrameIterator *iter);

#ifdef CVM_JVMDI
CVMBool
CVMframeIterateCanHaveJavaCatchClause(CVMFrameIterator *iter);
#endif

CVMFrameFlags
CVMframeIterateGetFlags(CVMFrameIterator *iter);

void
CVMframeIterateSetFlags(CVMFrameIterator *iter, CVMFrameFlags flags);

CVMFrame *
CVMframeIterateGetFrame(CVMFrameIterator *iter);

CVMMethodBlock *
CVMframeIterateGetMb(CVMFrameIterator *iter);

CVMUint8 *
CVMframeIterateGetJavaPc(CVMFrameIterator *iter);

void
CVMframeIterateSetJavaPc(CVMFrameIterator *iter, CVMUint8 *pc);

CVMStackVal32 *
CVMframeIterateGetLocals(CVMFrameIterator *iter);

CVMObjectICell *
CVMframeIterateSyncObject(CVMFrameIterator *iter);

CVMMethodBlock *
CVMgetCallerMb(CVMFrame* frame, int skip);


/*
 * Check if type srcCb is assignable to type dstCb.  */
extern CVMBool
CVMisAssignable(CVMExecEnv* ee, CVMClassBlock* srcCb, 
		CVMClassBlock* dstCb);

/*
 * CVMgcUnsafeIsInstanceOf - Check if obj is an instance of cb. 
 */
extern CVMBool
CVMgcUnsafeIsInstanceOf(CVMExecEnv* ee, CVMObject* obj, 
			CVMClassBlock* cb);

/*
 * CVMisSubclassOf - Check if subclasscb is a subclass of cb 
 */
extern CVMBool
CVMisSubclassOf(CVMExecEnv* ee, CVMClassBlock* subclasscb, 
		CVMClassBlock* cb);

/*
 * CVMextendsClass - Check if subclasscb is a subclass of cb without
 * considering implemented interfaces.
 */
extern CVMBool
CVMextendsClass(CVMExecEnv* ee, CVMClassBlock* subclasscb, CVMClassBlock* cb);

/*
 * CVMimplementsInterface - Check if 'cb' implements 'interfacecb'
 */
extern CVMBool
CVMimplementsInterface(CVMExecEnv* ee, CVMClassBlock* cb, 
		       CVMClassBlock* interfacecb);

/*
 * Brought over from JDK 1.2: originally named "VerifyClassAccess".
 *
 * Verify that currentClass can access newClass. 
 *
 * resolverAccess is set true when called from constant pool resolution
 * code. It fixes some problems with buggy 1.1.x compilers by being
 * a bit more loose with verification requirements.
 *
 * Does not throw an exception under any circumstances.
 */
CVMBool
CVMverifyClassAccess(CVMExecEnv* ee,
		     CVMClassBlock* currentClass, CVMClassBlock* newClass, 
		     CVMBool resolverAccess);

extern CVMBool
CVMverifyMemberAccess3(CVMExecEnv* ee,
                       CVMClassBlock* currentClass,
                       CVMClassBlock* resolvedClass,
                       CVMClassBlock* memberClass,
                       CVMUint32 access, CVMBool resolverAccess,
                       CVMBool protectedRestriction);

/*
 * Brought over from JDK 1.2: originally named "VerifyFieldAccess2".
 *
 * Verify that currentClass can access a field or method of newClass,
 * where that field's or method's access bits are "access".  We assume
 * that we've already verified that class can access memberClass.
 *
 * resolverAccess is set true when called from constant pool resolution
 * code. It fixes some problems with buggy 1.1.x compilers by being
 * a bit more loose with verification requirements.
 *
 * The protectedRestriction flag appears to implement the restriction
 * in the JLS, section 6.6.2, in which a protected constructor may not
 * be called by invocation of Class.newInstance(). In the JDK 1.2
 * sources the only place where VerifyFieldAccess2 was called with
 * TRUE as the final argument was JVM_NewInstance.
 *
 * Does not throw an exception under any circumstances.
 */
extern CVMBool
CVMverifyMemberAccess2(CVMExecEnv* ee,
		       CVMClassBlock* currentClass,
		       CVMClassBlock* memberClass, 
		       CVMUint32 access, CVMBool resolverAccess,
		       CVMBool protectedRestriction);

/*
 * Brought over from JDK 1.2: originally named "VerifyFieldAccess".
 *
 * This is equivalent to a call to CVMverifyMemberAccess2(currentClass,
 * memberClass, access, classloaderOnly, CVM_FALSE);
 */
CVMBool
CVMverifyMemberAccess(CVMExecEnv* ee,
		      CVMClassBlock* currentClass, 
		      CVMClassBlock* memberClass, 
		      int access, CVMBool resolverAccess);

/*
 * Brought over from JDK 1.2: originally named "IsTrustedClassLoader".
 */
#ifdef CVM_CLASSLOADING
extern CVMBool
CVMisTrustedClassLoader(CVMExecEnv* ee, CVMClassLoaderICell* loader);
#endif

/*
 * Multi-dimensional array creation
 */
/* 
 * CVMmultiArrayAlloc() is called from java opcode multianewarray
 * and passes the top of stack as parameter dimensions.
 * Because the width of the array dimensions is obtained via
 * dimensions[i], dimensions has to be of the same type as 
 * the stack elements for proper access.
 */
extern void
CVMmultiArrayAlloc(CVMExecEnv*     ee,
		   CVMInt32        nDimensions,
		   CVMStackVal32*  dimensions,
		   CVMClassBlock*  arrayCb,
		   CVMObjectICell* resultCell);

/*
 * Functions for enabling and disabling remote exceptions. If a remote
 * exception is thrown while remote exceptions are disabled, then the
 * exception will be ignored until remote exceptions are enabled. In other
 * words, CVMremoteExceptionOccurred() will return false while remote
 * exceptions are disabled. This prevents remote exceptions from blowing
 * us out of things like static initializers and class loading.
 */
extern void
CVMdisableRemoteExceptions(CVMExecEnv* ee);
extern void
CVMenableRemoteExceptions(CVMExecEnv* ee);
extern CVMBool
CVMremoteExceptionsDisabled(CVMExecEnv* ee);

#ifndef CVM_REMOTE_EXCEPTIONS_SUPPORTED
#define CVMdisableRemoteExceptions(ee)
#define CVMenableRemoteExceptions(ee)
#define CVMremoteExceptionsDisabled(ee) (CVM_FALSE)
#endif
/*
 * Fill in the specified Throwable object with the current backtrace,
 */
extern void 
CVMfillInStackTrace(CVMExecEnv *ee, CVMThrowableICell* objICell);
extern void 
CVMprintStackTrace(CVMExecEnv *ee, CVMThrowableICell* throwableICell,
		   CVMObjectICell* printableICell);

/*
 * Quicken the opcode. Possible return values are:
 *
 *    CVM_QUICKEN_ALREADY_QUICKENED
 *        the opcode has been quickened
 *    CVM_QUICKEN_NEED_TO_RUN_STATIC_INITIALIZERS
 *        need to run static initializers for *p_cb
 *    CVM_QUICKEN_ERROR
 *        an error occurred, exception pending
 *
 *    CVMquickenOpcode() uses CVM_QUICKEN_SUCCESS_OPCODE_AND_OPERANDS and 
 *    CVM_QUICKEN_SUCCESS_OPCODE_ONLY internally, but they are not returend.
 */
typedef enum {
    CVM_QUICKEN_SUCCESS_OPCODE_ONLY,
    CVM_QUICKEN_SUCCESS_OPCODE_AND_OPERANDS,
    CVM_QUICKEN_NEED_TO_RUN_STATIC_INITIALIZERS,
    CVM_QUICKEN_ALREADY_QUICKENED,
    CVM_QUICKEN_ERROR
} CVMQuickenReturnCode;

extern CVMQuickenReturnCode
CVMquickenOpcode(CVMExecEnv* ee, CVMUint8* pc, 
		 CVMConstantPool* cp, CVMClassBlock** p_cb,
		 CVMBool clobbersCpIndex);

/*
 * Lock around writing _quick instruction when new instruction operands
 * have to be written. If just the instruction opcode is changed, then
 * no locking is needed. Contention for this lock is very low, so a global
 * lock is appropriate.
 *
 * You can only use this lock in non-jvmdi builds. Otherwise the
 * debuggerLock should be used. No lock is needed if there is no
 * classloading support.
 */
#if defined(CVM_CLASSLOADING)
/* Use the global micro-lock. It is forbidden to block under this */
#define CVM_CODE_LOCK(ee)     CVMsysMicroLock(ee, CVM_CODE_MICROLOCK)
#define CVM_CODE_UNLOCK(ee)   CVMsysMicroUnlock(ee, CVM_CODE_MICROLOCK)
#endif

extern CVMBool
CVMisSpecialSuperCall(CVMClassBlock* currClass, CVMMethodBlock* mb);

#ifndef CVM_TRUSTED_CLASSLOADERS

/* Purpose: Checks to see if OK to instantiate of the specified class.  Will
            throw an InstantiationError if not OK.  */
/* Returns: CVM_TRUE if it will throw an InstantiationError exception, else
            returns CVM_FALSE. */
extern CVMBool
CVMclassIsOKToInstantiate(CVMExecEnv *ee, CVMClassBlock *cb);

/* Purpose: Checks to see if the type of the field has changed from static to
            non-static or vice-versa.  Will throw an
            IncompatibleClassChangeError if a change is detected. */
/* Returns: CVM_TRUE if it will throw an IncompatibleClassChangeError
            exception, else returns CVM_FALSE. */
extern CVMBool
CVMfieldHasNotChangeStaticState(CVMExecEnv *ee, CVMFieldBlock *fb,
                                CVMBool expectToBeStatic);

/* Purpose: Checks to see if OK to write to the specified field.  Will throw
            an IllegalAccessError if not OK unless surpressed. */
/* Returns: CVM_TRUE if it will throw an IllegalAccessError exception, else
            returns CVM_FALSE.  If okToThrow is CVM_FALSE, only the check will
            be done.  The throwing of the exception will be surpressed. */
extern CVMBool
CVMfieldIsOKToWriteTo(CVMExecEnv *ee, CVMFieldBlock *fb,
		      CVMClassBlock *currentCb, CVMBool okToThrow);

/* Purpose: Checks to see if the type of the method has changed from static to
            non-static or vice-versa.  Will throw an
            IncompatibleClassChangeError if a change is detected. */
/* Returns: CVM_TRUE if it will throw an IncompatibleClassChangeError
            exception, else returns CVM_FALSE. */
extern CVMBool
CVMmethodHasNotChangeStaticState(CVMExecEnv *ee, CVMMethodBlock *mb,
                                 CVMBool expectToBeStatic);

#endif

/*
 * Byte-code statistics gathering
 */
extern void
CVMinitStats();

extern void
CVMdumpStats();

/*
 * Routines which acquire and release locks needed for GC and thread
 * suspension. The latter was called "lock_for_debugger" in JDK 1.2,
 * even though it was also called from the native code for
 * Thread.suspend().
 */

extern void
CVMlocksForGCAcquire(CVMExecEnv* ee);

extern void
CVMlocksForGCRelease(CVMExecEnv* ee);

#if defined(CVM_HAVE_DEPRECATED) || defined(CVM_THREAD_SUSPENSION)

/* The following are APIs implementing the suspension checker mechanism that
   is used by the VM suspension mechanism to ensure it did not suspend a
   thread while it is holding a native lock: */
extern CVMBool CVMsuspendCheckerInit();
extern void CVMsuspendCheckerDestroy();
extern CVMBool CVMsuspendCheckerIsOK(CVMExecEnv *ee, CVMExecEnv *targetEE);

/*
 * NOTE: The forcing of all other threads to become GC-safe has been
 * separated out from these routines because of the race conditions it
 * caused.
 */

extern void
CVMlocksForThreadSuspendAcquire(CVMExecEnv* ee);

extern void
CVMlocksForThreadSuspendRelease(CVMExecEnv* ee);

/*
 * These have been separated out from the lock acquisition for thread
 * suspension because of race conditions caused by combining the two.
 * See jvm.c, JVM_SuspendThread, for proper use. They should be called
 * just before calling CVMthreadSuspend. Once a thread makes a request
 * and other threads have become consistent, the requesting thread may
 * not become inconsistent (i.e., GC-unsafe) until the request has
 * been cleared. For example, this implies that once a thread has
 * called CVMthreadSuspendGCSafeRequest, it can not read fields from
 * Java objects until it calls CVMthreadSuspendGCSafeRelease. It is
 * expected that CVMlocksForThreadSuspendAcquire will be called before
 * CVMthreadSuspendGCSafeRequest.
 */

extern void
CVMthreadSuspendConsistentRequest(CVMExecEnv* ee);

extern void
CVMthreadSuspendConsistentRelease(CVMExecEnv* ee);

#endif /* CVM_HAVE_DEPRECATED || CVM_THREAD_SUSPENSION */

/*
 * CVMmangleMethodName: create a mangled method name for a method.
 * It is the caller's responsiblity to free the mangled name returned.
 */
#ifdef CVM_CLASSLOADING

typedef enum {
    CVM_MangleMethodName_JNI_SHORT,
    CVM_MangleMethodName_JNI_LONG,
    CVM_MangleMethodName_CNI_SHORT
} CVMMangleType;

extern char* 
CVMmangleMethodName(CVMExecEnv* ee, CVMMethodBlock* mb, 
		    CVMMangleType mangleType);

#endif /* CVM_CLASSLOADING */
		    

/*
 * CVMlookupNativeMethodCode - look up the native code for a dynamically
 * loaded JNI method.
 */

#ifdef CVM_CLASSLOADING
extern CVMBool
CVMlookupNativeMethodCode(CVMExecEnv* ee, CVMMethodBlock* mb);
#endif /* CVM_CLASSLOADING */

/*
 * Shutdown support
 */

extern void CVMwaitForUserThreads(CVMExecEnv *ee);
extern void CVMwaitForAllThreads(CVMExecEnv *ee);
extern int CVMprepareToExit(void);
extern int CVMatExit(void (*func)(void));
extern void CVMexit(int);
extern void CVMabort(void);
extern CVMBool CVMsafeExit(CVMExecEnv *ee, CVMInt32 status);

extern void
CVMunloadApplicationclasses(CVMExecEnv* ee);

/*
 * Method tracing
 */
#undef TRACE_METHOD_CALL
#undef TRACE_METHOD_RETURN
#undef TRACE_FRAMELESS_METHOD_CALL
#undef TRACE_FRAMELESS_METHOD_RETURN

#ifdef CVM_TRACE_ENABLED
extern void CVMtraceInit();
extern void CVMtraceReset(CVMUint32 old, CVMUint32 nnew);
#endif

#ifdef CVM_TRACE
#define TRACE_METHOD_CALL(frame, isJump)			\
    if (CVMglobals.debugFlags & CVM_DEBUGFLAG(TRACE_METHOD)) {	\
	CVMtraceMethodCall(ee, frame, isJump);			\
    }
#define TRACE_METHOD_RETURN(frame)				\
    if (CVMglobals.debugFlags & CVM_DEBUGFLAG(TRACE_METHOD)) {	\
         CVMtraceMethodReturn(ee, frame);			\
    }
#define TRACE_FRAMELESS_METHOD_CALL(frame, mb, isJump)		\
    if (CVMglobals.debugFlags & CVM_DEBUGFLAG(TRACE_METHOD)) {	\
	CVMtraceFramelessMethodCall(ee, frame, mb, isJump);	\
    }
#define TRACE_FRAMELESS_METHOD_RETURN(mb, frame)		\
    if (CVMglobals.debugFlags & CVM_DEBUGFLAG(TRACE_METHOD)) {	\
         CVMtraceFramelessMethodReturn(ee, mb, frame);		\
    }
extern void CVMtraceMethodCall(CVMExecEnv *ee,
			       CVMFrame* frame, CVMBool isJump);
extern void CVMtraceMethodReturn(CVMExecEnv *ee, CVMFrame* frame);

extern void CVMtraceFramelessMethodCall(CVMExecEnv *ee,
				        CVMFrame* frame, CVMMethodBlock *mb,
					CVMBool isJump);
extern void CVMtraceFramelessMethodReturn(CVMExecEnv *ee, CVMMethodBlock *mb,
					  CVMFrame* frame);
#else
#define TRACE_METHOD_CALL(frame, isJump)
#define TRACE_METHOD_RETURN(frame)
#define TRACE_FRAMELESS_METHOD_CALL(frame, mb, isJump)
#define TRACE_FRAMELESS_METHOD_RETURN(mb, frame)
#endif

/*
 * Common code when the trylock fails.
 */
CVMBool
CVMsyncReturnHelper(CVMExecEnv *ee, CVMFrame *frame, CVMObjectICell *objICell,
		    CVMBool areturn);

/*
 * Common code for handling jvmdi and jvmpi events during method return.
 * Returns the return opcode (fixed up if it was an opc_breakpoint).
 */
CVMUint32
CVMregisterReturnEvent(CVMExecEnv *ee, CVMUint8* pc,
		       CVMObjectICell* resultCell);

/*
 * Common code for invoking JNI methods.
 */
CVMBool
CVMinvokeJNIHelper(CVMExecEnv *ee, CVMMethodBlock *mb);

/*
 * Post any required events when the thread starts or exits 
 */

void CVMpostThreadStartEvents(CVMExecEnv *ee);
void CVMpostThreadExitEvents(CVMExecEnv *ee);

/*
 * Copies an array of refs to another array of refs where an assignability
 * check is required for every element copied to the destination array.
 */
void
CVMcopyRefArrays(CVMExecEnv* ee,
		 CVMArrayOfRef* srcArr, jint src_pos,
		 CVMArrayOfRef* dstArr, jint dst_pos,
		 CVMClassBlock* dstElemCb, jint length);

/*
 * Manage interrupts.  See CVM.maskInterrupts() for usage.
 */
CVMBool CVMmaskInterrupts(CVMExecEnv *ee);
void CVMunmaskInterrupts(CVMExecEnv *ee);

#include "javavm/include/globals.h"

#endif /* _INCLUDED_INTERPRETER_H */
