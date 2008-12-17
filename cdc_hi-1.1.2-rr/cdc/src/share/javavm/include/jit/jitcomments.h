/*
 * @(#)jitcomments.h	1.6 06/10/10
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

#ifndef _INCLUDED_JITCOMMENTS_H
#define _INCLUDED_JITCOMMENTS_H

#include "javavm/include/jit/jitcontext.h"

#ifdef CVM_TRACE_JIT

/* Purpose: Sets the symbol name about to be added. */
#define CVMJITsetSymbolName(symbolArgs) \
    CVMJITprivateSetSymbolName symbolArgs

void CVMJITprivateSetSymbolName(CVMJITCompilationContext *con,
                                const char *format, ...);

/* Purpose: Gets the name of the symbol currently being added. */
#define CVMJITgetSymbolName(con)   ((con)->symbolName)

/* Purpose: Clears the current symbol name. */
#define CVMJITresetSymbolName(con) ((con)->symbolName = NULL)

/* Purpose: Pops the current symbol name. */
#define CVMJITpopSymbolName(con, name) \
    (((name) = (con)->symbolName), ((con)->symbolName = NULL))

/* Purpose: Restores the current symbol name. */
#define CVMJITpushSymbolName(con, name) ((con)->symbolName = (name))

struct CVMCodegenComment
{
    CVMCodegenComment *next;
    const char *commentStr;
};

#define CVMJITaddCodegenComment(commentArgs) \
    CVMJITprivateAddCodegenComment commentArgs
#define CVMJITpopCodegenComment(con, comment) \
    CVMJITprivatePopCodegenComment(con, &comment)
#define CVMJITpushCodegenComment(con, comment) \
    CVMJITprivatePushCodegenComment(con, comment)
#define CVMJITprintCodegenComment(commentArgs) \
    CVMJITprivatePrintCodegenComment commentArgs

void CVMJITprivateAddCodegenComment(CVMJITCompilationContext *con,
                                    const char *format, ...);
void CVMJITprivatePopCodegenComment(CVMJITCompilationContext *con,
                                    CVMCodegenComment **commentPtr);
void CVMJITprivatePushCodegenComment(CVMJITCompilationContext *con,
                                     CVMCodegenComment *comment);
void CVMJITdumpCodegenComments(CVMJITCompilationContext *con);
void CVMJITprivatePrintCodegenComment(const char *format, ...);
void CVMJITresetCodegenComments(CVMJITCompilationContext *con);

#else

#define CVMJITsetSymbolName(symbolArgs)
#define CVMJITgetSymbolName(con)                        (NULL)
#define CVMJITresetSymbolName(con)
#define CVMJITpopSymbolName(con, name)                  ((void)(name))
#define CVMJITpushSymbolName(con, name)                 ((void)(name))

#define CVMJITaddCodegenComment(commentArgs)
#define CVMJITpopCodegenComment(con, comment)           ((void)(comment))
#define CVMJITpushCodegenComment(con, comment)          ((void)(comment))
#define CVMJITdumpCodegenComments(con)
#define CVMJITprintCodegenComment(commentArgs)
#define CVMJITresetCodegenComments(con)

#endif /* CVM_TRACE_JIT */


#endif /* _INCLUDED_JITCOMMENTS_H */
