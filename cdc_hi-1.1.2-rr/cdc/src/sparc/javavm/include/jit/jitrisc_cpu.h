/*
 * @(#)jitrisc_cpu.h	1.46 06/10/10
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

#ifndef _INCLUDED_SPARC_JITRISC_CPU_H
#define _INCLUDED_SPARC_JITRISC_CPU_H

#include "javavm/include/porting/jit/jit.h"
#include "javavm/include/jit/jitasmconstants_cpu.h"

/*
 * This file #defines all of the macros that shared risc parts of the jit
 * use in a platform indendent way. The exported symbols are prefixed
 * with CVMCPU_. Other symbols should be considered private to the
 * sparc specific parts of the jit, including those with the
 * CVMSPARC_ prefix.
 */

/************************************************
 * Register Definitions.
 ************************************************/

/* all of the general purpose registers (32 of 'em) */
#define CVMSPARC_g0  0
#define CVMSPARC_g1  1
#define CVMSPARC_g2  2
#define CVMSPARC_g3  3
#define CVMSPARC_g4  4
#define CVMSPARC_g5  5
#define CVMSPARC_g6  6
#define CVMSPARC_g7  7
#define CVMSPARC_o0  8
#define CVMSPARC_o1  9
#define CVMSPARC_o2 10
#define CVMSPARC_o3 11
#define CVMSPARC_o4 12
#define CVMSPARC_o5 13
#define CVMSPARC_o6 14
#define CVMSPARC_o7 15
#define CVMSPARC_l0 16
#define CVMSPARC_l1 17
#define CVMSPARC_l2 18
#define CVMSPARC_l3 19
#define CVMSPARC_l4 20
#define CVMSPARC_l5 21
#define CVMSPARC_l6 22
#define CVMSPARC_l7 23
#define CVMSPARC_i0 24
#define CVMSPARC_i1 25
#define CVMSPARC_i2 26
#define CVMSPARC_i3 27
#define CVMSPARC_i4 28
#define CVMSPARC_i5 29
#define CVMSPARC_i6 30
#define CVMSPARC_i7 31

#define CVMSPARC_SP  CVMSPARC_o6
#define CVMSPARC_LR  CVMSPARC_o7

/*
 * Macro to map a register name (like R31) to a register number (like 31).
 * Because of the strange way the preprocessor expands macros, we need
 * an extra indirection of macro invocations to get it expanded correctly.
 */
#define CVMCPU_MAP_REGNAME_0(regname) CVMSPARC_##regname
#define CVMCPU_MAP_REGNAME(regname)   CVMCPU_MAP_REGNAME_0(regname)

/* Some registers known to regman and codegen.jcs */
#define CVMCPU_SP_REG          CVMSPARC_SP
#define CVMCPU_JFP_REG         CVMCPU_MAP_REGNAME(CVMSPARC_JFP_REGNAME)
#define CVMCPU_JSP_REG         CVMCPU_MAP_REGNAME(CVMSPARC_JSP_REGNAME)
#define CVMCPU_PROLOGUE_PREVFRAME_REG \
				CVMCPU_MAP_REGNAME(CVMSPARC_PREVFRAME_REGNAME)
#define CVMCPU_PROLOGUE_NEWJFP_REG  \
    				CVMCPU_MAP_REGNAME(CVMSPARC_NEWJFP_REGNAME)

#ifdef CVMCPU_HAS_ZERO_REG
#define CVMCPU_ZERO_REG        CVMSPARC_g0
#endif
#define CVMCPU_INVALID_REG     (-1)

/* 
 * More registers known to regman and codegen.jcs. These ones are
 * optional. If you have registers to spare then defining these macros
 * allows for more efficient code generation. The registers will
 * be setup by CVMJITgoNative() when compiled code is first entered.
 */
#undef  CVMCPU_CVMGLOBALS_REG
#define CVMCPU_CHUNKEND_REG    CVMCPU_MAP_REGNAME(CVMSPARC_CHUNKEND_REGNAME)
#define CVMCPU_EE_REG          CVMCPU_MAP_REGNAME(CVMSPARC_EE_REGNAME)
#ifdef CVMCPU_HAS_CP_REG
#define CVMCPU_CP_REG          CVMCPU_MAP_REGNAME(CVMSPARC_CP_REGNAME)
#endif
#ifdef CVMJIT_TRAP_BASED_GC_CHECKS
#define CVMCPU_GC_REG          CVMCPU_MAP_REGNAME(CVMSPARC_GC_REGNAME)
#endif

/* registers for C helper arguments */
#define CVMCPU_ARG1_REG    CVMSPARC_o0
#define CVMCPU_ARG2_REG    CVMSPARC_o1
#define CVMCPU_ARG3_REG    CVMSPARC_o2
#define CVMCPU_ARG4_REG    CVMSPARC_o3

/* registers for C helper result */
#define CVMCPU_RESULT1_REG   CVMSPARC_o0
#define CVMCPU_RESULT2_REG   CVMSPARC_o1

/* 
 * The set of registers dedicated for PHI spills. Usually the
 * first PHI is at the bottom of the range of available non-volatile registers.
 */

#define CVMCPU_PHI_REG_SET (			\
    1U<<CVMSPARC_l0 | 1U<<CVMSPARC_l1 | 	\
    1U<<CVMSPARC_l2 | 1U<<CVMSPARC_l3 |		\
    1U<<CVMSPARC_l4 | 1U<<CVMSPARC_l5		\
)

/* range of registers that regman should look at */
#define CVMCPU_MIN_INTERESTING_REG      CVMSPARC_o0
#define CVMCPU_MAX_INTERESTING_REG      CVMSPARC_i1

/*******************************************************
 * Register Sets
 *******************************************************/

/* The set of all registers */
#define CVMCPU_ALL_SET  0xffffffff

/*
 * Registers that regman should never allocate. Only list registers
 * that haven't already been exported to regman. For example, there's
 * no need to tell regman about CVMCPU_SP_REG because regman already
 * knows that it is busy.
 */
#define CVMCPU_BUSY_SET (						      \
    1U<<CVMSPARC_g0 | /* always treated as 0 */				      \
    1U<<CVMSPARC_g1 | /* reserved as a scratch register */		      \
    1U<<CVMSPARC_g2 | 1U<<CVMSPARC_g3 | 1U<<CVMSPARC_g4 |/* app reserved */   \
    1U<<CVMSPARC_g5 | 1U<<CVMSPARC_g6 | 1U<<CVMSPARC_g7 |/* system reserved */\
    1U<<CVMSPARC_i2 | 1U<<CVMSPARC_i3 | 1U<<CVMSPARC_i4 |/* >max interesting*/\
    1U<<CVMSPARC_i5 | 1U<<CVMSPARC_i6 | 1U<<CVMSPARC_i7  /*>max interesting */)

/*
 * The set of all non-volatile registers according to C calling conventions
 * There's no need to put CVMCPU_BUSY_SET registers in this set.
 */
#define CVMCPU_NON_VOLATILE_SET   /* all "local" and "in" registers */	 \
    (1U<<CVMSPARC_l0 | 1U<<CVMSPARC_l1 | 1U<<CVMSPARC_l2 | 1U<<CVMSPARC_l3 | \
     1U<<CVMSPARC_l4 | 1U<<CVMSPARC_l5 | 1U<<CVMSPARC_l6 | 1U<<CVMSPARC_l7 | \
     1U<<CVMSPARC_i0 | 1U<<CVMSPARC_i1 | 1U<<CVMSPARC_i2 | 1U<<CVMSPARC_i3 | \
     1U<<CVMSPARC_i4 | 1U<<CVMSPARC_i5 | 1U<<CVMSPARC_i6 | 1U<<CVMSPARC_i7)

/* The set of all volatile registers according to C calling conventions */
#define CVMCPU_VOLATILE_SET \
    (CVMCPU_ALL_SET & ~CVMCPU_NON_VOLATILE_SET)

/*
 * The set of registers that regman should only use as a last resort.
 *
 * Usually this includes the set of phi registers, but if you have a limited
 * number of non-phi non-volatile registers available, then at most only
 * include the first couple of phi registers.
 *
 * It also should include the argument registers, since we would rather
 * that other non-volatile registers get allocated before these.
 */
#define CVMCPU_AVOID_SET (			\
    /* argument registers we make use of */	\
    1U<<CVMCPU_ARG1_REG | 1U<<CVMCPU_ARG2_REG |	\
    1U<<CVMCPU_ARG3_REG | 1U<<CVMCPU_ARG4_REG |	\
    /* phi registers */				\
    CVMCPU_PHI_REG_SET)

/*
 * Alignment parameters of integer registers are trivial
 * All quantities in 32-bit words
 */
#define CVMCPU_SINGLE_REG_ALIGNMENT	1
#define CVMCPU_DOUBLE_REG_ALIGNMENT	1

/*
 * In case we opt for the FPU, we'll need these parameters too
 */
#ifdef CVM_JIT_USE_FP_HARDWARE

#define CVMCPU_FP_MIN_INTERESTING_REG	0
#define CVMCPU_FP_MAX_INTERESTING_REG	31
#define CVMCPU_FP_BUSY_SET 0
#define CVMCPU_FP_ALL_SET 0xffffffff
#define CVMCPU_FP_NON_VOLATILE_SET 0
#define CVMCPU_FP_VOLATILE_SET CVMCPU_FP_ALL_SET

#define CVMCPU_FP_PHI_REG_SET (		\
    1U<<0 | 1U<<1 | 1U<<2 | 1U<<3	\
)

/*
 * Alignment parameters of floating registers
 * All quantities in 32-bit words
 */
#define CVMCPU_FP_SINGLE_REG_ALIGNMENT	1
#define CVMCPU_FP_DOUBLE_REG_ALIGNMENT	2

#endif

/*
 * Set of registers that can be used for the jsr return address. If the LR
 * is a GPR, then just set it to the LR reg. Otherwise usually it is best
 * to allow any register to be used.
 */
#define CVMCPU_JSR_RETURN_ADDRESS_SET (1U<<CVMSPARC_LR)

/************************************************************************
 * CPU features - These macros define various features of the processor.
 ************************************************************************/

/* no conditional instructions supported other than branches */
#undef  CVMCPU_HAS_CONDITIONAL_ALU_INSTRUCTIONS
#undef  CVMCPU_HAS_CONDITIONAL_LOADSTORE_INSTRUCTIONS
#undef  CVMCPU_HAS_CONDITIONAL_CALL_INSTRUCTIONS

/* ALU instructions can set condition codes */
#define CVMCPU_HAS_ALU_SETCC

/* Sparc has an integer mul by immediate instruction */
/* Implement CVMCPUemitMulConstant() if this #defined */
#define CVMCPU_HAS_IMUL_IMMEDIATE

/* Sparc does not have a postincrement store */
#undef CVMCPU_HAS_POSTINCREMENT_STORE

/* Maximum offset (+/-) for a load/store word instruction. */
#define CVMCPU_MAX_LOADSTORE_OFFSET  (4*1024-1)

/* Number of instructions reserved for setting up constant pool base
 * register in method prologue */
#ifdef CVMCPU_HAS_CP_REG
#ifdef CVM_AOT
#define CVMCPU_RESERVED_CP_REG_INSTRUCTIONS 5
#else
#define CVMCPU_RESERVED_CP_REG_INSTRUCTIONS 3
#endif
#endif

/* Number of nop's needed for GC patching. We need two because we need
 * to make sure the delay slot does not get executed when the instruction
 * is patched to make the gc rendezvous call. */
#define CVMCPU_NUM_NOPS_FOR_GC_PATCH 2

#endif /* _INCLUDED_SPARC_JITRISC_CPU_H */
