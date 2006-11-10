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

#include "javavm/include/defs.h"
#include "javavm/include/objects.h"

#include "javavm/include/assert.h"
#include "javavm/include/globals.h"
#include "javavm/include/jit/jitcodebuffer.h"

#include "javavm/include/porting/jit/jit.h"
#include "portlibs/jit/risc/include/porting/ccmrisc.h"

#include <signal.h>
#define ucontext asm_ucontext
#include <asm/ucontext.h>

#define MAXSIGNUM 32
#define MASK(sig) ((CVMUint32)1 << sig)

static CVMBool cvmSignalInstalling = CVM_FALSE;
static struct sigaction sact[MAXSIGNUM];
static unsigned int cvmSignals = 0; /* signals used by cvm */

#include <dlfcn.h>

/* 
 * Call pthread's __sigaction 
 */

typedef int (*sigaction_t)(int, const struct sigaction *, struct sigaction *);

static int
callSysSigaction(int sig, const struct sigaction *act, struct sigaction *oact)
{
    static sigaction_t sysSigaction = 0;

    if (sysSigaction == NULL) {
        sysSigaction = (sigaction_t)dlsym(RTLD_NEXT, "__sigaction");
        if (sysSigaction == NULL) {
            return -1;
        }
    }
    return (*sysSigaction)(sig, act, oact); 
}

/* 
 * Override sigaction to chain signal handler. 
 */
int 
__sigaction(int sig, const struct sigaction *act, struct sigaction *oact)
{
    struct sigaction oldAct;
    if (cvmSignalInstalling) {
        int result = 0;
        if (act != NULL) {
	    /* CVM is installing handler for this signal. Install the new
               handler and save the old one. */
            result = callSysSigaction(sig, act, &oldAct);
            if (result != -1) {
                sact[sig] = oldAct;
                if (oact != NULL) {
                    *oact = oldAct;
                }
                cvmSignals |= MASK(sig); /* Mark the signal as used by CVM */
            }
        }
        return result;
    } else {
        if ((MASK(sig) & cvmSignals) != 0) {
	    /* CVM has installed its signal handler for this signal. Chain the
               new handler. */
	    if (oact != NULL) {
                *oact = sact[sig];
	    }
            if (act != NULL) {
                sact[sig] = *act;
            }
            return 0;
        } else {
	    /* The signal is not used by CVM. Install the new handler.*/
            return callSysSigaction(sig, act, oact);
        }
    }
}

int
sigaction(int sig, const struct sigaction *act, struct sigaction *oact)
{
    return __sigaction(sig, act, oact);
}

/*
 * SEGV handler
 */
static void handleSegv(int sig, siginfo_t* info, struct ucontext* ucp)
{
    int pid = getpid();
    CVMUint8* pc = (CVMUint8*)(ptrdiff_t)ucp->uc_mcontext.sc_pc;
    if (CVMJITcodeCacheInCompiledMethod(pc)) {
#if 0
        fprintf(stderr, "Process #%d received signal %d in jit code\n",
                pid, sig);
#endif
	/* Coming from compiled code. */
#ifdef CVMJIT_TRAP_BASED_GC_CHECKS
	{
	    CVMCPUInstruction gcTrapInstr = *(CVMCPUInstruction*)pc;
	    /* ldr offset in lower 16 bits*/
	    int offset = gcTrapInstr & 0xffff;
	    if (offset >= 0x8000) {
		offset |= 0xffff0000; /* sign extend */
	    }
	    gcTrapInstr = gcTrapInstr & ~0xffff;
	    if (gcTrapInstr == (CVMglobals.jit.gcTrapInstr & ~0xffff)) {
		if (offset >= 0) {
		    /* set link register to return to just before the trap
		     * instruction where incoming locals are reloaded. */
		    ucp->uc_mcontext.sc_regs[31] =
			ucp->uc_mcontext.sc_pc - offset;
		    /* Branch to do a gc rendezvous */
		    ucp->uc_mcontext.sc_pc =
			(unsigned long)CVMCCMruntimeGCRendezvousGlue;
		} else {
		    /* phi handling: branch to generated code that will
		     * spill phis, call CVMCCMruntimeGCRendezvousGlue, and
		     * then reload phis.
		     */
		    ucp->uc_mcontext.sc_pc += offset;
		}
#if 0
		fprintf(stderr, "redirecting to rendezvous code 0x%x 0x%x\n",
			(int)ucp->uc_mcontext.sc_pc,
			(int)ucp->uc_mcontext.sc_regs[31]);
#endif
		return;
	    }
	}
#endif
#ifdef CVMJIT_TRAP_BASED_NULL_CHECKS
	/* Branch and link to throw null pointer exception glue */
	ucp->uc_mcontext.sc_regs[31] =
	    ucp->uc_mcontext.sc_pc + 4; /* set link register */
	ucp->uc_mcontext.sc_pc =
	    (unsigned long)CVMCCMruntimeThrowNullPointerExceptionGlue;
	return;
#endif
    } else if (CVMJITcodeCacheInCCM(pc)) {
#ifdef CVMJIT_TRAP_BASED_NULL_CHECKS
#if 0
        fprintf(stderr, "Process #%d received signal %d in ccm code\n",
                pid, sig);
#endif
	/* Coming from CCM code. */
	/* Branch to throw null pointer exception glue */
	ucp->uc_mcontext.sc_pc =
	    (unsigned long)CVMCCMruntimeThrowNullPointerExceptionGlue;
	return;
#endif
    }
#ifdef CVM_DEBUG
    {
	fprintf(stderr, "Process #%d received signal %d\n", pid, sig);
	/* A real one. Suspend thread */
	fprintf(stderr, "Process #%d being suspended\n", pid);
	
	kill(pid, SIGSTOP);
	/* on linux-mips, the pc is off by 4. Fix it.
	 * NOTE: for some reason it is not off by 4 if the crash is in
	 * the code cache, which is why didn't adjust it above also.
	 */
	ucp->uc_mcontext.sc_pc = ucp->uc_mcontext.sc_pc - 4; 
    }
#else
    /* Call chained handler */
    {
        struct sigaction sa = sact[sig];
        if ((sa.sa_flags & SA_SIGINFO) != 0) {
            (*(sa.sa_sigaction))(sig, info, ucp);
        } else {
            if (sa.sa_handler == SIG_IGN || sa.sa_handler == SIG_DFL) {
                fprintf(stderr, "Process #%d received signal %d\n", pid, sig);
                fprintf(stderr, "Process #%d being suspended\n", pid);
                kill(pid, SIGSTOP);
            } else {
                (*(sa.sa_handler))(sig);
            }
        }
    }
#endif
}

/*
 * Install specific handlers for JIT exception handling
 */
CVMBool
linuxSegvHandlerInit(void)
{
    int signals[] = {SIGSEGV};
    int i;
    int result = 0;
    
    cvmSignalInstalling = CVM_TRUE;

    for (i = 0; result != -1 && i < (sizeof signals / sizeof signals[0]); ++i){
        struct sigaction sa;
        sa.sa_sigaction = (void(*)(int,siginfo_t*,void*))handleSegv;
        sa.sa_flags = SA_RESTART | SA_SIGINFO;
        sigemptyset(&sa.sa_mask);
        result = sigaction(signals[i], &sa, NULL);
    }

    cvmSignalInstalling = CVM_FALSE;

    return (result != -1);
}
