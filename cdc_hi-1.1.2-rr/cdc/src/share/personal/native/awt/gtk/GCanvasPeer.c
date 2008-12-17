/*
 * @(#)GCanvasPeer.c	1.13 06/10/10
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

#include "GCanvasPeer.h"
#include "sun_awt_gtk_GCanvasPeer.h"


jboolean
awt_gtk_GCanvasPeerData_init (JNIEnv *env, jobject this, GCanvasPeerData *data)
{
	
	
	
	
	return JNI_TRUE;
}

JNIEXPORT void JNICALL
Java_sun_awt_gtk_GCanvasPeer_create (JNIEnv *env, jobject this)
{
	GCanvasPeerData *data = (GCanvasPeerData *)calloc (1, sizeof (GCanvasPeerData));
	GtkWidget *drawingArea;
	GtkWidget *eventBox;
	
	if (data == NULL)
	{
		(*env)->ThrowNew (env, GCachedIDs.OutOfMemoryErrorClass, NULL);
		return;
	}
	
	awt_gtk_threadsEnter();
	
	drawingArea = gtk_drawing_area_new ();
	eventBox = gtk_event_box_new ();
		
	gtk_container_add (GTK_CONTAINER(eventBox), drawingArea);
        /* make Canvas focusable */
        GTK_WIDGET_SET_FLAGS(eventBox, GTK_CAN_FOCUS);
	gtk_widget_show (drawingArea);
	gtk_widget_show (eventBox);
	awt_gtk_GComponentPeerData_init (env, this, (GComponentPeerData *)data, eventBox, drawingArea, drawingArea, JNI_FALSE);
	
	awt_gtk_threadsLeave();
}
