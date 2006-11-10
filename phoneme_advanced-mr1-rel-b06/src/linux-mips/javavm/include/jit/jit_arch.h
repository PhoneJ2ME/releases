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

#ifndef _LINUX_MIPS_JIT_ARCH_H
#define _LINUX_MIPS_JIT_ARCH_H

/*
 * Do these first, since other headers rely on their settings
 */
#ifdef CVM_MTASK
#undef  CVMJIT_PATCH_BASED_GC_CHECKS
#define CVMJIT_TRAP_BASED_GC_CHECKS
#endif
#define CVMJIT_TRAP_BASED_NULL_CHECKS

#include "javavm/include/jit/jit_cpu.h"
#include "javavm/include/jit/jitasmconstants_cpu.h"
#ifndef _ASM
#include "javavm/include/jit/ccm_cpu.h"
#include "portlibs/jit/risc/include/export/jit_risc.h"
#endif

/*
 * The size in bytes of the region for which we want accurate profiling
 * information. In this case, we want to be accurate to within an instrruction.
 */
#define CVMJIT_PROFILE_REGION_SIZE CVMCPU_INSTRUCTION_SIZE
#ifndef _ASM
#include "portlibs/posix/posix_jit_profil.h"
#endif

#endif /* _LINUX_MIPS_JIT_ARCH_H */
