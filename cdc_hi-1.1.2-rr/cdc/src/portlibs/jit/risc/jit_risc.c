/*
 * @(#)jit_risc.c	1.43 06/10/10
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

/*
 * This file implements part of the jit porting layer.
 */

#include "javavm/include/jit/jit.h"
#include "javavm/include/jit/jitcontext.h"
#include "javavm/include/jit/jitirlist.h"
#include "javavm/include/jit/jitirdump.h"
#include "javavm/include/jit/jitirnode.h"
#include "javavm/include/jit/jitirblock.h"
#include "javavm/include/jit/jitconstantpool.h"
#include "javavm/include/jit/jitcodebuffer.h"
#include "javavm/include/jit/jitfixup.h"
#include "javavm/include/jit/jitstats.h"
#include "javavm/include/jit/jitmemory.h"
#include "javavm/include/jit/jitcomments.h"
#include "javavm/include/jit/jitdebug.h"
#include "javavm/include/globals.h"

#include "portlibs/jit/risc/include/porting/jitriscemitter.h"
#include "portlibs/jit/risc/include/porting/ccmrisc.h"
#include "portlibs/jit/risc/include/export/jitregman.h"
#include "portlibs/jit/risc/jitstackman.h"
#include "portlibs/jit/risc/jitgrammar.h"

#include "javavm/include/porting/jit/jit.h"

#include "generated/javavm/include/jit/jitcodegen.h"

/*
 * Special backend actions to be done at the beginning and end of each
 * codegen rule. Usually only enabled when debugging.
 */

void
CVMJITdoStartOfCodegenRuleAction(CVMJITCompilationContext *con, int ruleno,
                                 const char *description, CVMJITIRNode* node)
{
    /* Check to see if we need a constantpool dump.  If so, emit the dump
       with a branch around it. However, don't do this before emitting the
       first instruction for a basic block. Otherwise the MAP_PC address
       won't be computed properly (see logic in MAP_PC rule).
    */
    if (CVMJITcbufGetLogicalPC(con) !=
	con->currentCompilationBlock->logicalAddress)
    {
	CVMRISCemitConstantPoolDumpWithBranchAroundIfNeeded(con);
    }

    /* The following is intentionally left here to assist in future codegen
       rules debugging needs if necessary: */

#ifdef CVM_DEBUG_JIT_TRACE_CODEGEN_RULE_EXECUTION
    /* Tell about the codegen rule that is being executed: */
    CVMJITprintCodegenComment(("Doing node %4d codegen rule %s",
                               CVMJITirnodeGetID(node), description));
#endif
}

#if defined(CVM_DEBUG_ASSERTS)
void CVMJITdoEndOfCodegenRuleAction(CVMJITCompilationContext *con)
{
    /* There should not be any pinned registers at the end of a codegen rule */
    CVMassert(CVMRMhasNoPinnedRegisters(con));
}
#endif

/**************************************************************************
 * GC CheckPoints support.
 *
 * Instead of generating gc checks, you can just apply a patch that
 * calls the gc rendezvous code when requested.  This is what we
 * choose to do for our shared risc implementations.  If you do gc
 * checks inline, then these functions can have empty implementations.
 * If you #define CVMJIT_TRAP_BASED_GC_CHECKS, then these functions are
 * implemented elsewhere.
 **************************************************************************/

/*
 * Patch or unpatch gc checkpoints.
 */

#ifdef CVMJIT_PATCH_BASED_GC_CHECKS
static void
csPatchHelper(CVMExecEnv* ee);
#endif

void
CVMJITenableRendezvousCalls(CVMExecEnv* ee)
{
#ifdef CVMJIT_TRAP_BASED_GC_CHECKS
    CVMJITenableRendezvousCallsTrapbased(ee);
#endif
#ifdef CVMJIT_PATCH_BASED_GC_CHECKS
    csPatchHelper(ee);
#endif
}

void
CVMJITdisableRendezvousCalls(CVMExecEnv* ee)
{
#ifdef CVMJIT_TRAP_BASED_GC_CHECKS
   CVMJITdisableRendezvousCallsTrapbased(ee);
#endif
#ifdef CVMJIT_PATCH_BASED_GC_CHECKS
    csPatchHelper(ee);
#endif
}

#ifdef CVMJIT_PATCH_BASED_GC_CHECKS
static void
csPatchHelper(CVMExecEnv* ee)
{
    CVMUint8* cbuf;
    CVMJITGlobalState* jgs = &CVMglobals.jit;

    CVMassert(CVMsysMutexIAmOwner(ee, &CVMglobals.jitLock));

    if (jgs->destroyed) {
	return;
    }
     
    /* Patch CCM method call and return spots */
    {
	int i;
	for (i=0; i < CVMCPU_NUM_CCM_PATCH_POINTS; i++) {
	    CVMCCMGCPatchPoint* gcPatchPoint = &jgs->ccmGcPatchPoints[i];
	    CVMCPUInstruction tmpInstr;
	    CVMCPUInstruction* instrPtr;
	    if (gcPatchPoint->patchPoint == NULL) {
		continue;
	    }
	    instrPtr = (CVMCPUInstruction*)gcPatchPoint->patchPoint;
	    /* swap current instruction with saved instruction */
	    tmpInstr = gcPatchPoint->patchInstruction;
	    gcPatchPoint->patchInstruction = *instrPtr;
	    *instrPtr = tmpInstr;
	}
    }

    /* iterate over the code cache, patching all gc points */
    for (cbuf = jgs->codeCacheStart;
	 cbuf < jgs->codeCacheEnd;
	 cbuf += CVMJITcbufSize(cbuf)) {
	if (CVMJITcbufIsCommitted(cbuf)) {
	    CVMCompiledMethodDescriptor* cmd = CVMJITcbufCmd(cbuf);
	    CVMCompiledGCCheckPCs* gcpcs = CVMcmdGCCheckPCs(cmd);
	    CVMUint8* codeBufAddr = CVMcmdCodeBufAddr(cmd);
	    if (gcpcs != NULL) {
		int i;
		CVMCPUInstruction* patchedInstructions = (CVMCPUInstruction*)
		    (((CVMUint32)&gcpcs->pcEntries[gcpcs->noEntries]
		      + sizeof(CVMCPUInstruction)-1)
		     & ~(sizeof(CVMCPUInstruction)-1));
		for (i = 0; i < gcpcs->noEntries; i++) {
		    if (gcpcs->pcEntries[i] != 0) {
	   	        CVMCPUInstruction tmpInstr;
		        CVMCPUInstruction* instrPtr = (CVMCPUInstruction*)
			    (gcpcs->pcEntries[i] + codeBufAddr);
		        /* swap current instruction with saved instruction */
		        tmpInstr = patchedInstructions[i];
		        patchedInstructions[i] = *instrPtr;
		        *instrPtr = tmpInstr;
		    }
		}
	    }
	}
    }
    /* Flush cache */
    CVMJITflushCache(jgs->codeCacheStart, jgs->codeCacheEnd);
}
#endif /* CVMJIT_PATCH_BASED_GC_CHECKS */

/*
 * fixBranchesToBlock - store the proper branch offset in the branch
 * instructions that reference the specified ir block. This is
 * needed for forward branches.
 */
static void
fixBranchesToBlock(CVMJITCompilationContext* con, CVMJITIRBlock* bk,
		   CVMJITFixupElement* fixupList, CVMJITAddressMode addrMode)
{
    CVMInt32 logicalTargetAddress = bk->logicalAddress;
    CVMJITFixupElement* thisRef = fixupList;
    while (thisRef != NULL) {
	CVMJITFixupElement* nextRef = thisRef->next;
	CVMJITfixupAddress(con, thisRef->logicalAddress, logicalTargetAddress,
			   addrMode);
	thisRef = nextRef;
    }
    
}

static void
allocateCodeBuffer(CVMJITCompilationContext* con);

/*
 * CVMJITcompileGenerateCode: Main function for compiler back-end
 * responsible for generating platform specific machine code. By the time
 * this function is called the IR has already been generated.
 */
#undef  MAX
#define MAX(a,b) (((a) > (b)) ? (a) : (b))
void 
CVMJITcompileGenerateCode(CVMJITCompilationContext* con)
{
    CVMJITIRBlock* bk;
    int		   maxStructSize;
    int		   maxStructNumber;
    int		   maxStateMachineAllocation;
    int		   allocationSize;
    CVMCPUPrologueRec prec;
    CVMBool	   success = CVM_TRUE;
    
    /*
     * Allocate sufficient room for compile-time stacks.
     * These are VERY generous.
     */
     maxStructSize = MAX(
	sizeof(struct CVMJITCompileExpression_rule_computation_state),
	sizeof(struct CVMJITCompileExpression_match_computation_state)
    );

    /* Estimate the max stack depth for the compilation stacks: */
    maxStructNumber = (con->saveRootCnt + 1) * 3;
    CVMJITstatsRecordSetCount(con, CVMJIT_STATS_COMPUTED_COMP_STACK_MAX,
                              maxStructNumber);

    maxStateMachineAllocation = maxStructSize * maxStructNumber;
    allocationSize  = maxStateMachineAllocation +
        sizeof(struct CVMJITStackElement) * maxStructNumber;
    con->compilationStateStack = CVMJITmemNew(con, JIT_ALLOC_CGEN_OTHER,
					      allocationSize);
    con->cgstackInit = (struct CVMJITStackElement*)
	((char*)(con->compilationStateStack) + maxStateMachineAllocation);
    con->cgstackLimit = con->cgstackInit + maxStructNumber;

#ifdef CVM_JIT_PATCHED_METHOD_INVOCATIONS
    /*
     * Allocate space for callee table (table of mb's that we will
     * make direct method calls to. con->numCallees is the most slots
     * we will need and was computed by the front end. Some calls may
     * use a duplicate mb, in which case we'll need fewer slots. After
     * we have generated the code, this table will be copied into the
     * code buffer after the stackmaps, so no space will be wasted.
     */
    if (con->numCallees != 0) {
        CVMassert(CVMglobals.jit.pmiEnabled);
	con->callees = (CVMMethodBlock**)
	    CVMJITmemNew(con, JIT_ALLOC_CGEN_OTHER,
			 (con->numCallees + 1) * sizeof(CVMAddr));
    } else {
	con->callees = NULL;
    }
#endif

    /*
     * Initialize subsystems
     */
    CVMRMinit(con);
    CVMSMinit(con);

    allocateCodeBuffer(con);
    CVMJITcsInit(con);

    /* Emit prologue and get patchable instruction state into prec */
    CVMJITprintCodegenComment(("Method prologue"));
    CVMCPUemitMethodPrologue(con, &prec);

    CVMJITsetInit(con, &con->localRefSet);

    /* Support for passing phis in registers.  Decide
       the register assignments and store the result
       in the USED nodes */

    bk = (CVMJITIRBlock*)CVMJITirlistGetHead(&(con->bkList));
    while (bk != NULL) {
	if (CVMJITirblockIsTranslated(bk)) {
	    if (bk->phiCount > 0) {
		CVMRMallocateIncomingPhisRegisters(con, bk);
	    }
#ifdef CVM_JIT_REGISTER_LOCALS
	    if (bk->incomingLocalsCount > 0) {
		CVMassert(bk->phiCount <= 0);
		CVMRMallocateIncomingLocalsRegisters(con, bk);
	    }
#endif
	}
	bk = CVMJITirblockGetNext(bk);
    }

    bk = (CVMJITIRBlock*)CVMJITirlistGetHead(&(con->bkList));
    while (bk != NULL) {
	CVMJITIRRoot* root = 
            (CVMJITIRRoot*)CVMJITirlistGetHead(CVMJITirblockGetRootList(bk));

	if (!CVMJITirblockIsTranslated(bk)) {
	    CVMassert(root == NULL);
	    CVMassert(bk->branchFixupList == NULL);
	    CVMassert(bk->condBranchFixupList == NULL);
	    CVMassert(bk->condBranchFixupList == NULL);
	} else {
	    con->currentCompilationBlock = bk;

            CVMJITcsBeginBlock(con);
	    CVMRMbeginBlock(con, bk);

            /* NOTE: CVMRMbeginBlock() may need to emit some code for the
               start of the block e.g. the GC rendezvous code.  Hence, we
               should record the starting address of the block before calling
               CVMRMbeginBlock(). Also, CVMRMbeginBlock() takes care of 
	       setting up bk->logicalAddress for us.
            */
	    bk->flags |= CVMJITIRBLOCK_ADDRESS_FIXED;
	    fixBranchesToBlock(con, bk, bk->branchFixupList,
			       CVMJIT_BRANCH_ADDRESS_MODE);
	    fixBranchesToBlock(con, bk, bk->condBranchFixupList,
			       CVMJIT_COND_BRANCH_ADDRESS_MODE);

	    while (root != NULL){
		int compilationResult;
		con->cgsp = con->cgstackInit;
		compilationResult =
		    CVMJITCompileExpression_match( root->expr, con);
		if (compilationResult < 0 ){
		    CVMtraceJITCodegenExec({
			CVMconsolePrintf(
			    "******Indigestable expression in node %d\n",
			    CVMJITirnodeGetID(con->errNode));
			CVMJITirdumpIRNodeAlways(con, root->expr, 0, "   ");
			CVMconsolePrintf("\n   > \n********\n");
		    });
		    success = CVM_FALSE;
		    if (compilationResult == JIT_IR_SYNTAX_ERROR){
			/* There was an IR syntax error */
#ifdef CVM_DEBUG
			CVMpanic("CVMJIT: IR syntax error");
#endif
			CVMassert(CVM_FALSE);
			CVMJITerror(con, CANNOT_COMPILE,
				    "CVMJIT: IR syntax error");
		    }
		}
		CVMJITCompileExpression_synthesis( root->expr, con);
		root = CVMJITirnodeGetRootNext(root);
	    }
	    root = CVMJITirlistGetHead(CVMJITirblockGetRootList(bk));
	    while (root != NULL){
		con->cgsp = con->cgstackInit;
		CVMJITCompileExpression_action( root->expr, con);
		root = CVMJITirnodeGetRootNext(root);
	    }
	    /*
	     * If there is another block and we fall through to it, then
	     * make sure we load any expected incoming locals, and branch
	     * around any code that involves gc (like load/spill of phis).
	     */
	    if (CVMJITirblockGetNext(bk) != NULL &&
		CVMJITirblockFallsThru(bk))
	    {
		CVMJITIRBlock* target = CVMJITirblockGetNext(bk);
		CVMRMpinAllIncomingLocals(con, target, CVM_FALSE);
		/*
		 * Backward branch targets load their own locals so OSR will
		 * work, so we don't want to end up loading the locals twice.
		 * Also, we don't want to execute any PHI or GC related code.
		 * Instead, branch around all this code.
		 */
		if (CVMJITirblockIsBackwardBranchTarget(target) &&
		    (target->phiCount > 0
#ifdef CVM_JIT_REGISTER_LOCALS
		     || target->incomingLocalsCount > 0
#endif
		     ))
		{
		    /* There's no way the fallthrough block has an addr yet. */
		    CVMassert(!(target->flags & CVMJITIRBLOCK_ADDRESS_FIXED));
		    CVMJITaddCodegenComment((con,
		        "fallthrough to block L%d, which is "
			"backward branch target",
                        CVMJITirblockGetBlockID(target)));
		    CVMCPUemitBranchNeedFixup(con, target->logicalAddress, 
					      CVMCPU_COND_AL,
					      &(target->branchFixupList));
		}
		CVMRMunpinAllIncomingLocals(con, target);
	    }
	    CVMRMendBlock(con);
	}
	bk = CVMJITirblockGetNext(bk);
    }
    /* Sanity check inlining information */
    CVMassert(con->inliningDepth == 0);

    /* More sanity checks of inlining information after all blocks are
       done */
    CVMassert(con->inliningInfoIdx == con->numInliningInfoEntries);

#ifdef CVMCPU_HAS_CP_REG
    con->target.cpLogicalPC = CVMJITcbufGetLogicalPC(con);
#endif
    /* The following should come after the last instruction generated. */
    CVMJITdumpRuntimeConstantPool(con, CVM_TRUE);

    /*
     * Handle patchable instructions in the prologue
     */
    CVMCPUemitMethodProloguePatch(con, &prec);

    /*
     * Remember this for when we initialize the cmd
     */
    con->intToCompOffset = prec.intToCompOffset;

#ifdef CVM_DEBUG_ASSERTS
#if defined(CVM_AOT) || defined(CVM_MTASK)
    if (!CVMglobals.jit.isPrecompiling)
        /* AOT/warmup compilation use Trap based GC */
#endif
#ifdef CVMJIT_PATCH_BASED_GC_CHECKS
    {
        /* Check for the consistency of the gc check count */
        CVMassert(con->gcCheckPcsIndex == con->gcCheckPcsSize);
    }
#endif
#endif
    
    if (!success) {
        CVMJITerror(con, CANNOT_COMPILE,
		    "Indigestible Expression encountered");
    }

    CVMJITcsCommitMethod(con);
    CVMJITflushCache(con->codeEntry, con->curPhysicalPC);
}

#define CVMJIT_STARTPC_MIN_ALIGNMENT 8
#define CVMJIT_STARTPC_MAX_PAD 16
#ifdef CVMJIT_ALIGN_STARTPC
#define CVMJIT_STARTPC_ALIGNMENT 32
#else
#define CVMJIT_STARTPC_ALIGNMENT CVMJIT_STARTPC_MIN_ALIGNMENT
#endif

static void
allocateCodeBuffer(CVMJITCompilationContext* con)
{
    CVMSize inliningInfoSize;
    CVMSize pcMapTableSize;
    CVMSize extraSpace = 0;
    int space;
#ifdef CVMJIT_PATCH_BASED_GC_CHECKS
    int gcCheckPcsSize;
#endif

    /* reserve memory for the compiledPC <-> javaPC mapping table */
    if (con->mapPcNodeCount != 0) {
	pcMapTableSize = CVMoffsetof(CVMCompiledPcMapTable, maps) +
	    sizeof(CVMCompiledPcMap) * con->mapPcNodeCount;
	pcMapTableSize = CVMalignWordUp(pcMapTableSize);
	extraSpace += pcMapTableSize;
    } else {
	pcMapTableSize = 0;
    }

    /* Reserve space for inlining information */
    if (con->numInliningInfoEntries > 0) {
	CVMassert(con->maxInliningDepth > 0);
	inliningInfoSize =
	    sizeof(CVMCompiledInliningInfo) +
	    (con->numInliningInfoEntries - 1) *
	    sizeof(CVMCompiledInliningInfoEntry);
	inliningInfoSize = CVMalignWordUp(inliningInfoSize);
	extraSpace += inliningInfoSize;
    } else {
	inliningInfoSize = 0;
    }

#ifdef CVMJIT_PATCH_BASED_GC_CHECKS
#if defined(CVM_AOT) || defined(CVM_MTASK)
    if (CVMglobals.jit.isPrecompiling) {
        /* AOT/warmup compilation use trap based GC */
        gcCheckPcsSize = 0;
    } else
#endif
    {
        /* Reserve space for patch lists */
        if (con->gcCheckPcsSize > 0) {
	    CVMUint32 memSize;
	    /* room for everything but the pcEntries list */ 
	    memSize  = CVMoffsetof(CVMCompiledGCCheckPCs, pcEntries);
	    /* room for the pcEntries list */ 
  	    memSize +=  con->gcCheckPcsSize * sizeof(CVMUint16);
	    /* room for the patchedInstructions list */ 
	    memSize +=  sizeof(CVMCPUInstruction);
	    memSize &= ~(sizeof(CVMCPUInstruction)-1);
	    memSize +=  con->gcCheckPcsSize * sizeof(CVMCPUInstruction);
	    gcCheckPcsSize = memSize;
	    gcCheckPcsSize = CVMalignWordUp(gcCheckPcsSize);
	    extraSpace += gcCheckPcsSize;
        } else {
	    gcCheckPcsSize = 0;
        }
    }
#endif

    space = extraSpace;

    /* padding for startPC alignment */
    extraSpace += CVMJIT_STARTPC_ALIGNMENT - 4;

    CVMJITcbufAllocate(con, extraSpace);

    CVMJITcbufGetLogicalPC(con) = 0;

    memset(CVMJITcbufGetPhysicalPC(con), 0, extraSpace);

    /* memory layout:
	cbuf size
	pcMaps
	inliningInfo
	gcCheckPCs
	cmd
	code
	stackmaps
        callees
    */

    if (con->mapPcNodeCount != 0) {
	CVMtraceJITCodegenExec({
	    CVMconsolePrintf("PC MAP TABLE ADDRESS = 0x%x\n",
			     CVMJITcbufGetPhysicalPC(con));
	});
	con->pcMapTable = (void *)CVMJITcbufGetPhysicalPC(con);
	con->pcMapTable->numEntries = con->mapPcNodeCount;
	con->mapPcNodeCount = 0; /* reset for use by backend */
	space -= pcMapTableSize;
	CVMJITcbufGetPhysicalPC(con) += pcMapTableSize;
	CVMassert(CVMJITcbufGetPhysicalPC(con) < con->codeBufEnd);
    }

    /* Reserve space for inlining information */
    if (con->numInliningInfoEntries > 0) {
	CVMtraceJITCodegenExec({
	    CVMconsolePrintf("INLINING INFO ADDRESS = 0x%x\n",
			     CVMJITcbufGetPhysicalPC(con));
	});
	con->inliningInfo = (void *)CVMJITcbufGetPhysicalPC(con);
	con->inliningInfo->numEntries = con->numInliningInfoEntries;
	con->inliningInfo->maxDepth = con->maxInliningDepth;
	space -= inliningInfoSize;
	CVMJITcbufGetPhysicalPC(con) += inliningInfoSize;
	CVMassert(CVMJITcbufGetPhysicalPC(con) < con->codeBufEnd);
    } else {
	con->inliningInfo = NULL;
    }
    con->inliningDepth = 0;
    con->inliningInfoIdx = 0;

#ifdef CVMJIT_PATCH_BASED_GC_CHECKS
#if defined(CVM_AOT) || defined(CVM_MTASK)
    if (CVMglobals.jit.isPrecompiling) {
        /* AOT compilation use trap based GC */ 
        con->gcCheckPcs = NULL;
    } else
#endif
    {
        /* Reserve space for patch lists */
        if (con->gcCheckPcsSize > 0) {
	    /* allocate... */ 
	    CVMtraceJITCodegenExec({
	        CVMconsolePrintf("GC CHECK PCS ADDRESS = 0x%x\n",
			         CVMJITcbufGetPhysicalPC(con));
 	    });
	    con->gcCheckPcs = (void *)CVMJITcbufGetPhysicalPC(con);
	    con->gcCheckPcs->noEntries = con->gcCheckPcsSize;
	    space -= gcCheckPcsSize;
	    CVMJITcbufGetPhysicalPC(con) += gcCheckPcsSize;
	    CVMassert(CVMJITcbufGetPhysicalPC(con) < con->codeBufEnd);
        } else {
	   con->gcCheckPcs = NULL;
        }
    }
#endif

    CVMassert(space == 0);

    /* extra, cmd, code */
    CVMJITcbufGetPhysicalPC(con) += sizeof (CVMCompiledMethodDescriptor);

    /* align to CVMJIT_STARTPC_ALIGNMENT boundary */
    {
	CVMSize pc = (CVMSize)CVMJITcbufGetPhysicalPC(con);
	CVMSize pc1 = CVMpackSizeBy(pc, CVMJIT_STARTPC_ALIGNMENT);
	if (pc1 - pc <= CVMJIT_STARTPC_MAX_PAD) {
	    pc = pc1;
	} else if (CVMJIT_STARTPC_MIN_ALIGNMENT < CVMJIT_STARTPC_MAX_PAD) {
	    pc = CVMpackSizeBy(pc, CVMJIT_STARTPC_MIN_ALIGNMENT);
	}
	CVMJITcbufGetPhysicalPC(con) = (CVMUint8 *)pc;
    }

    con->codeEntry = CVMJITcbufGetPhysicalPC(con);

    CVMtraceJITCodegenExec({
        CVMconsolePrintf("CODE ENTRY ADDRESS = 0x%x\n",
                         con->codeEntry);
    });
}

void
CVMCPUemitFrameReference(CVMJITCompilationContext* con,
    int opcode,
    int destreg,
    enum CVMCPUFrameReferenceType addressing,
    int cellNumber)
{
#ifdef CVM_TRACE_JIT
    const char *addressType = NULL;
#endif

    CVMtraceJITCodegenExec({
        switch (addressing){
        case CVMCPU_FRAME_LOCAL:
            addressType = "Java local cell #";
            break;
        case CVMCPU_FRAME_TEMP:
            addressType = "Java temp cell #";
            break;
        case CVMCPU_FRAME_CSTACK:
            addressType = "C frame cell #";
            break;
        default:
            addressType = "Bad address form X ";
            break;
        }
    });

    {
        int frameReg;
        int frameOffset;
        if (addressing == CVMCPU_FRAME_LOCAL) {
            /* Locals are located just before the frame ptr */
            /* This is a negative offset */
            frameOffset = -(int)((con->numberLocalWords - cellNumber) *
                sizeof(CVMJavaVal32));
            frameReg = CVMCPU_JFP_REG;
        } else if (addressing == CVMCPU_FRAME_TEMP) {
            CVMassert(addressing == CVMCPU_FRAME_TEMP);
            /* Temps are located just after the frame ptr */
            /* This is a positive offset */
            frameOffset = CVM_COMPILEDFRAME_SIZE +
                cellNumber * sizeof(CVMJavaVal32);
            frameReg = CVMCPU_JFP_REG;
        } else {
            CVMassert(addressing == CVMCPU_FRAME_CSTACK);
            /* the reference is to the start of the C frame */
            /* This is a positive offset */
            frameOffset = cellNumber * sizeof(CVMJavaVal32);
            frameReg = CVMCPU_SP_REG;
        }
	/* Make sure we're within the addressing range: */
	if ((frameOffset > CVMCPU_MAX_LOADSTORE_OFFSET) ||
	    (frameOffset < -(CVMCPU_MAX_LOADSTORE_OFFSET))) {
	    CVMJITerror(con, CANNOT_COMPILE,
		"method has locals out of reach");
	}

	CVMJITaddCodegenComment((con, "%s %d", addressType,
				cellNumber));
	CVMCPUemitMemoryReferenceImmediate(con, opcode,
	    destreg, frameReg, frameOffset);
    }
}
