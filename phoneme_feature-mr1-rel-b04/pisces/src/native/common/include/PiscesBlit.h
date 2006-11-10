/*
 * 
 * Copyright  1990-2006 Sun Microsystems, Inc. All Rights Reserved. 
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
 */


#ifndef PISCES_BLIT_H
#define PISCES_BLIT_H

#include <PiscesDefs.h>
#include <PiscesRenderer.h>

#define COMPOSITE_SRC_OVER 1

void fillRectSrcOver(Renderer* rdr,
    void *data, int imageType,
    jint imageOffset,
    jint imageScanlineStride,
    jint imagePixelStride,
    jint scrOrient,
    jint width, jint height,
    jint x0, jint y0, jint x1, jint y1,
    jint cred, jint cgreen, jint cblue);

void genLinearGradientPaint(Renderer *rdr, jint height);
void genRadialGradientPaint(Renderer *rdr, jint height);
void genTexturePaint(Renderer *rdr, jint height);

void blitSrcOver888(Renderer *rdr, jint height);
void blitSrcOver8888(Renderer *rdr, jint height);
void blitSrcOver565(Renderer *rdr, jint height);
void blitSrcOver8(Renderer *rdr, jint height);

void blitPTSrcOver888(Renderer *rdr, jint height);
void blitPTSrcOver8888(Renderer *rdr, jint height);
void blitPTSrcOver565(Renderer *rdr, jint height);
void blitPTSrcOver8(Renderer *rdr, jint height);

void clearRect8888(Renderer *rdr, jint x, jint y, jint w, jint h);
void clearRect565(Renderer *rdr, jint x, jint y, jint w, jint h);
void clearRect8(Renderer *rdr, jint x, jint y, jint w, jint h);

#endif
