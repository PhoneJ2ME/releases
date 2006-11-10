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

#ifndef _INCLUDED_ASMCONSTANTS_H
#define _INCLUDED_ASMCONSTANTS_H

#include "javavm/include/porting/jit/jit.h"

#define OFFSET_CVMClassBlock_interfacesX			16
#define OFFSET_CVMClassBlock_arrayInfoX				12
#define OFFSET_CVMClassBlock_accessFlagsX			40
#define OFFSET_CVMClassBlock_instanceSizeX			44
#define OFFSET_CVMClassBlock_javaInstanceX			48
#if   defined(CVM_DEBUG) && defined(CVM_DEBUG_CLASSINFO)
#define OFFSET_CVMClassBlock_methodTablePtrX			76
#elif defined(CVM_DEBUG) || defined(CVM_DEBUG_CLASSINFO)
#define OFFSET_CVMClassBlock_methodTablePtrX			72
#else
#define OFFSET_CVMClassBlock_methodTablePtrX			68
#endif

#define OFFSET_CVMArrayInfo_elementCb				12

/* Offsets and constants for CVMMethodBlock: */
#ifdef CVM_METHODBLOCK_HAS_CB
#define OFFSET_CVMMethodBlock_jitInvokerX			0
#define OFFSET_CVMMethodBlock_cbX				12
#define OFFSET_CVMMethodBlock_argsSizeX				22
#define OFFSET_CVMMethodBlock_invokerIdxX                       23
#define OFFSET_CVMMethodBlock_accessFlagsX			24
#define OFFSET_CVMMethodBlock_methodIndexX			25
#define OFFSET_CVMMethodBlock_codeX				28
#define CONSTANT_CVMMethodBlock_size                            32
#else
#define OFFSET_CVMMethodBlock_jitInvokerX			0
#define OFFSET_CVMMethodBlock_argsSizeX                         18
#define OFFSET_CVMMethodBlock_invokerIdxX                       19
#define OFFSET_CVMMethodBlock_accessFlagsX                      20
#define OFFSET_CVMMethodBlock_methodIndexX                      21
#define OFFSET_CVMMethodBlock_codeX                             24
#define CONSTANT_CVMMethodBlock_size                            28
#endif

#define OFFSET_CVMMethodRange_mb				4

#define OFFSET_CVMJmd_maxLocalsX				0

#if 0
#define OFFSET_CVMCmd_capacityX					0
#define OFFSET_CVMCmd_entryCountX				3
#define OFFSET_CVMCmd_maxLocalsX				8
#endif

/* Offsets and constants for CVMCompiledFrame: */
#define OFFSET_CVMCompiledFrame_PC				16
#define OFFSET_CVMCompiledFrame_receiverObjX			20
#ifdef CVMCPU_HAS_CP_REG
#define OFFSET_CVMCompiledFrame_cpBaseRegX			24
#define OFFSET_CVMCompiledFrame_opstackX			28
#else
#define OFFSET_CVMCompiledFrame_opstackX			24
#endif

/* Offsets and constants for CVMFrame: */
#define OFFSET_CVMFrame_prevX					0
#define OFFSET_CVMFrame_type					4
#define OFFSET_CVMFrame_flags					5
#define OFFSET_CVMFrame_topOfStack				8
#define OFFSET_CVMFrame_mb					12

/* Offsets and constants for CVMExecEnv: */
#define OFFSET_CVMExecEnv_tcstate_GCSAFE			0
#define OFFSET_CVMExecEnv_interpreterStack			56
#define OFFSET_CVMExecEnv_miscICell				32
#define OFFSET_CVMExecEnv_objLocksOwned                         140
#define OFFSET_CVMExecEnv_objLocksFreeOwned                     144
#define OFFSET_CVMExecEnv_invokeMb				160

/* Offsets and constants for CVMInterfaceTable: */
#define CONSTANT_LOG2_CVMInterfaceTable_SIZE                    3
#define CONSTANT_LOG2_CVMInterfaceTable_methodTableIndex_SIZE   1

/* Offsets and constants for CVMInterfaces: */
#define OFFSET_CVMInterfaces_interfaceCountX			0
#define OFFSET_CVMInterfaces_itable				4
#define OFFSET_CVMInterfaces_itable0_intfInfoX                  8

/* Offsets and constants for CVMStack: */
#define OFFSET_CVMStack_currentFrame				20
#define OFFSET_CVMStack_stackChunkEnd				28

/* Offsets and constants for CVMCCExecEnv: */
#define OFFSET_CVMCCExecEnv_ee					0
#define OFFSET_CVMCCExecEnv_stackChunkEnd			4
#ifndef CVM_JIT_COPY_CCMCODE_TO_CODECACHE
#define OFFSET_CVMCCExecEnv_ccmGCRendezvousGlue			8
#endif
#if defined(CVMJIT_TRAP_BASED_GC_CHECKS) && defined(CVMCPU_HAS_VOLATILE_GC_REG)
#define OFFSET_CVMCCExecEnv_gcTrapAddr				12
#endif
#define OFFSET_CVMCCExecEnv_ccmStorage				16
#define CONSTANT_CVMCCExecEnv_size                              80

/* Offsets and constants for CVMGlobalState: */
#define OFFSET_CVMGlobalState_allocPtrPtr			0
#define OFFSET_CVMGlobalState_allocTopPtr			4
#ifdef CVM_ADV_ATOMIC_SWAP
#define OFFSET_CVMGlobalState_fastHeapLock			8
#endif
#ifdef CVM_TRACE_ENABLED
#define OFFSET_CVMGlobalState_debugFlags		        12
#endif
#ifdef CVM_TRACE_JIT
#define OFFSET_CVMGlobalState_debugJITFlags                     16
#endif
#define OFFSET_CVMGlobalState_cstate_GCSAFE		        24

/* Offsets and constants for CVMCState: */
#define OFFSET_CVMCState_request				0

/* Offsets and constants for CVMObjectHeader: */
#define OFFSET_CVMObjectHeader_clas                             0
#define OFFSET_CVMObjectHeader_various32                        4

/* Offsets and constants for fastlocking: */
#define CONSTANT_CVM_OBJECT_NO_HASH                             0
#define CONSTANT_CVM_SYNC_BITS                                  2
#define CONSTANT_CVM_HASH_MASK                                  ((1<<24)-1)
#define OFFSET_CVMObjMonitor_bits                               0

#define OFFSET_CVMOwnedMonitor_owner                            0
#define OFFSET_CVMOwnedMonitor_type                             4
#define OFFSET_CVMOwnedMonitor_object                           8
#define OFFSET_CVMOwnedMonitor_u_fast_bits                      12
#define OFFSET_CVMOwnedMonitor_next                             16

/* NOTE: In the following, OFFSET_CVMOwnedMonitor_count is only applicable
   if CVM_FASTLOCK_TYPE != CVM_FASTLOCK_NONE.  But since we do not know what
   CVM_FASTLOCK_TYPE is in this file, we go ahead and just declare
   OFFSET_CVMOwnedMonitor_count.
*/
#ifdef CVM_DEBUG
#define OFFSET_CVMOwnedMonitor_magic                            20
#define OFFSET_CVMOwnedMonitor_state                            24
#define OFFSET_CVMOwnedMonitor_count                            28
#define CONSTANT_CVM_OWNEDMON_FREE                              0
#define CONSTANT_CVM_OWNEDMON_OWNED                             1
#else
#define OFFSET_CVMOwnedMonitor_count                            20
#endif /* CVM_DEBUG */

#define CONSTANT_CVM_LOCKSTATE_UNLOCKED                         0x2
#define CONSTANT_CVM_LOCKSTATE_LOCKED                           0x0

#define CONSTANT_CVM_INVALID_REENTRY_COUNT     			0xffffffff

#define CONSTANT_CLASS_ACC_FINALIZABLE	         		0x04

#define CONSTANT_METHOD_ACC_STATIC				0x08
#define CONSTANT_METHOD_ACC_SYNCHRONIZED			0x20
#define CONSTANT_METHOD_ACC_NATIVE				0x40
#define CONSTANT_METHOD_ACC_ABSTRACT                            0x80

#define CONSTANT_INVOKE_CNI_METHOD				2
#define CONSTANT_INVOKE_JNI_METHOD				3

#define CONSTANT_CNI_NEW_TRANSITION_FRAME			-1
#define CONSTANT_CNI_NEW_MB					-3

#define CONSTANT_TRACE_METHOD					0x2

#ifdef CVMCPU_HAS_CP_REG
#define CONSTANT_CMD_SIZE_ADJUST0 2
#else
#define CONSTANT_CMD_SIZE_ADJUST0 0
#endif
#ifdef CVM_DEBUG_ASSERTS
#define CONSTANT_CMD_SIZE_ADJUST1 (2+CONSTANT_CMD_SIZE_ADJUST0)
#else
#define CONSTANT_CMD_SIZE_ADJUST1 CONSTANT_CMD_SIZE_ADJUST0
#endif
#ifdef CVMJIT_PATCH_BASED_GC_CHECKS
#define CONSTANT_CMD_SIZE_ADJUST (2+CONSTANT_CMD_SIZE_ADJUST1)
#else
#define CONSTANT_CMD_SIZE_ADJUST CONSTANT_CMD_SIZE_ADJUST1
#endif

#define CONSTANT_CMD_SIZE0	(20 + CONSTANT_CMD_SIZE_ADJUST)
/* align to word boundary */
#define CONSTANT_CMD_SIZE	((CONSTANT_CMD_SIZE0 + 3) & ~3)

#define CONSTANT_CVM_FRAME_MASK_SPECIAL				0x1
#define CONSTANT_CVM_FRAME_MASK_SLOW				0x2
#define CONSTANT_CVM_FRAME_MASK_ALL				0x3

#define CONSTANT_HANDLE_GC_FOR_RETURN				1

#ifdef CVM_DEBUG_ASSERTS
#define CONSTANT_CVM_FRAMETYPE_NONE				0
#define CONSTANT_CVM_FRAMETYPE_COMPILED				7
#else
#define CONSTANT_CVM_FRAMETYPE_COMPILED				6
#endif

/* Offsets and constants for the java.lang.String class: */
#define OFFSET_java_lang_String_value   8
#define OFFSET_java_lang_String_offset  12
#define OFFSET_java_lang_String_count   16

/* Offset and constants for Array classes: */
#define OFFSET_ARRAY_ELEMENTS           12

#endif /* _INCLUDED_ASMCONSTANTS_H */
