/*
 * @(#)FrameID.c	1.9 06/10/26
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


#include "util.h"
#include "FrameID.h"
#include "threadControl.h"

/* FrameID: */

/* ------------------------------------------------------------ */
/* | thread frame generation (48 bits)| frame number (16 bits)| */
/* ------------------------------------------------------------ */

#define FNUM_BWIDTH 16
#define FNUM_BMASK ((1<<FNUM_BWIDTH)-1)

FrameID 
createFrameID(jthread thread, FrameNumber fnum)
{
    FrameID frame;
    jlong frameGeneration;
    
    frameGeneration = threadControl_getFrameGeneration(thread);
    frame = (frameGeneration<<FNUM_BWIDTH) | (jlong)fnum;
    return frame;
}

FrameNumber 
getFrameNumber(FrameID frame)
{
    /*LINTED*/
    return (FrameNumber)(((jint)frame) & FNUM_BMASK);
}

jdwpError 
validateFrameID(jthread thread, FrameID frame)
{
    jlong frameGeneration;
    
    frameGeneration = threadControl_getFrameGeneration(thread);
    if ( frameGeneration != (frame>>FNUM_BWIDTH)  ) {
        return JDWP_ERROR(INVALID_FRAMEID);
    }
    return JDWP_ERROR(NONE);
}

