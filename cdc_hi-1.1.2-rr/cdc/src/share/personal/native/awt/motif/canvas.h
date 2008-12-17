/*
 * @(#)canvas.h	1.24 06/10/10
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
#ifndef _CANVAS_H_
#define _CANVAS_H_

void            awt_canvas_pointerMotionEvents(Widget w, int on, XtPointer this);
void            awt_canvas_reconfigure(struct FrameData * wdata);
Widget 
awt_canvas_create(JNIEnv * env, XtPointer this,
		  Widget parent,
		  char *base,
		  long width,
		  long height,
		  Boolean parentIsFrame,
		  struct FrameData * wdata);
void            awt_canvas_scroll(XtPointer this, struct CanvasData * wdata, long dx, long dy);
void 
awt_canvas_handleEvent(Widget w, XtPointer client_data,
		       XEvent * event, Boolean * cont);
void            awt_modify_KeyEvent(JNIEnv * env, XEvent * xevent, jobject jevent);

void            awt_setActivatedShell(Widget shell);
void            awt_setDeactivatedShell(Widget shell);

#endif				/* _CANVAS_H_ */
