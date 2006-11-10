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

#include "javavm/include/porting/sync.h"
#include "javavm/include/porting/globals.h"
#include "javavm/include/porting/net.h"
#include "javavm/include/porting/io.h"
#include "javavm/include/porting/threads.h"
#include "portlibs/posix/threads.h"
#include "generated/javavm/include/build_defs.h"
#include <signal.h>
#include <stdlib.h>
#include <string.h>
#include <sys/param.h>
#include <stdio.h>
#include <unistd.h>
#include <dlfcn.h>
#include <assert.h>
#include <malloc.h>
#include <javavm/include/utils.h>

#ifdef CVM_JIT
#include "javavm/include/porting/jit/jit.h"
#include "javavm/include/globals.h"
#endif

CVMBool CVMinitVMTargetGlobalState()
{
    /*
     * Initialize the target global state pointed to by 'target'.
     */

#ifdef CVMJIT_TRAP_BASED_GC_CHECKS
    /*
     * Setup gcTrapAddr to point CVMJIT_MAX_GCTRAPADDR_WORD_OFFSET
     * words into a page aligned page of memory whose first 
     * 2* CVMJIT_MAX_GCTRAPADDR_WORD_OFFSET words all point to gcTrapAddr.
     */
    {
	long pagesize = sysconf(_SC_PAGESIZE);
	if (pagesize == -1) {
	    return CVM_FALSE;
	}
	CVMglobals.jit.gcTrapAddr = memalign(pagesize, pagesize);
	if (CVMglobals.jit.gcTrapAddr == NULL) {
	    return CVM_FALSE;
	}
	/* offset by CVMJIT_MAX_GCTRAPADDR_WORD_OFFSET words to allow 
	   negative offsets up to CVMJIT_MAX_GCTRAPADDR_WORD_OFFSET words */
	CVMglobals.jit.gcTrapAddr += CVMJIT_MAX_GCTRAPADDR_WORD_OFFSET;
#ifndef CVMCPU_HAS_VOLATILE_GC_REG
	/* Stuff the address of the page into the page itself. Only needed
	 * when using an NV GC Reg */
	{
	    int i;
	    for (i = -CVMJIT_MAX_GCTRAPADDR_WORD_OFFSET;
		 i < CVMJIT_MAX_GCTRAPADDR_WORD_OFFSET;
		 i++) {
		CVMglobals.jit.gcTrapAddr[i] = CVMglobals.jit.gcTrapAddr;
	    }
	}
#endif
    }
#endif

    return CVM_TRUE;
}

void CVMdestroyVMTargetGlobalState()
{
    /*
     * ... and destroy it.
     */
#ifdef CVMJIT_TRAP_BASED_GC_CHECKS
    if (CVMglobals.jit.gcTrapAddr != NULL) {
	CVMglobals.jit.gcTrapAddr -= CVMJIT_MAX_GCTRAPADDR_WORD_OFFSET;
	free(CVMglobals.jit.gcTrapAddr);
    }
#endif
}

static CVMProperties props;

CVMBool CVMinitStaticState()
{
    /*
     * Initialize the static state for this address space
     */
    if (!POSIXthreadInitStaticState()) {
#ifdef CVM_DEBUG
	fprintf(stderr, "POSIXthreadInitStaticState failed\n");
#endif
	return CVM_FALSE;
    }

    linuxCaptureInitialStack();

    if (!linuxSyncInit()) {
#ifdef CVM_DEBUG
	fprintf(stderr, "linuxSyncInit failed\n");
#endif
        return CVM_FALSE;
    }

#if defined(CVM_JIT) || defined(CVM_USE_MEM_MGR)
    if (!linuxSegvHandlerInit()) {
#ifdef CVM_DEBUG
	fprintf(stderr, "linuxSegvHandlerInit failed\n");
#endif
        return CVM_FALSE;
    }
#endif

    if (!linuxIoInit()) {
#ifdef CVM_DEBUG
	fprintf(stderr, "linuxIoInit failed\n");
#endif
	return CVM_FALSE;
    }

    linuxNetInit();

    sigignore(SIGPIPE);

    {
	char buf[MAXPATHLEN + 1], *p0, *p;

	{
	    Dl_info dlinfo;
	    int l = readlink("/proc/self/exe", buf, sizeof buf - 1);
	    if (l != -1) {
		buf[l] = '\0';
		p0 = buf;
	    } else {
#ifdef CVM_DEBUG
		fprintf(stderr, "readlink on /proc/self/exe failed,"
		    " running valgrind?\n");
#endif
		/* workaround for valgrind/kernel bug */
		if (dladdr((void *)CVMinitStaticState, &dlinfo)) {
		    realpath((char *)dlinfo.dli_fname, buf);
		    p0 = buf;
		} else {
		    l = open("/proc/self/maps", O_RDONLY);
		    if (l == -1) {
#ifdef CVM_DEBUG
			fprintf(stderr, "open of /proc/self/maps failed\n");
#endif
			return CVM_FALSE;
		    }
		    l = read(l, buf, sizeof buf);
		    if (l == -1) {
#ifdef CVM_DEBUG
			fprintf(stderr, "read of /proc/self/maps failed\n");
#endif
			return CVM_FALSE;
		    }
		    buf[l] = '\0';
		    p = strchr(buf, '\n');
		    if (p == NULL) {
#ifdef CVM_DEBUG
			fprintf(stderr, "/proc/self/maps line too long\n");
#endif
			return CVM_FALSE;
		    }
		    *p = '\0';
		    p0 = strrchr(buf, ' ') + 1;
		}
	    }
	}

	/* get rid of .../bin/cvm */
	p = strrchr(p0, '/');
	if (p == NULL) {
	    goto badpath;
	}
	p = p - 4;
	if (p > p0 && strncmp(p, "/bin/", 5) == 0) {
	    *p = '\0';
	} else {
	    goto badpath;
	}
        return( CVMinitPathValues( &props, p0, "lib", "lib" ) );
    badpath:
	fprintf(stderr, "Invalid path %s\n", p0);
	fprintf(stderr, "Executable must be in a directory named \"bin\".\n");
	return CVM_FALSE;
    }

    return CVM_TRUE;
}

void CVMdestroyStaticState()
{
    /*
     * ... and destroy it.
     */
    CVMdestroyPathValues((void *)&props);
    POSIXthreadDestroyStaticState();
}

const CVMProperties *CVMgetProperties()
{
    return &props;
}
