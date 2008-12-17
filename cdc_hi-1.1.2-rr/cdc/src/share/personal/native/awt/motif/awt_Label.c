/*
 * @(#)awt_Label.c	1.46 06/10/10
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
#include "awt_p.h"
#include "java_awt_Color.h"
#include "java_awt_Font.h"
#include "java_awt_Label.h"
#include "sun_awt_motif_MLabelPeer.h"
#include "sun_awt_motif_MComponentPeer.h"

#include "multi_font.h"

JNIEXPORT void  JNICALL
Java_sun_awt_motif_MLabelPeer_initIDs(JNIEnv * env, jclass cls)
{
    cls = (*env)->FindClass(env, "java/awt/Label");
    if (cls == NULL)
        return;
    
    MCachedIDs.java_awt_Label_labelFID = (*env)->GetFieldID(
        env, cls, "text", "Ljava/lang/String;");
}

JNIEXPORT void  JNICALL
Java_sun_awt_motif_MLabelPeer_create(JNIEnv * env, jobject this, jobject parent)
{
	struct ComponentData *componentData;
	struct ComponentData *parentComponentData;
	Pixel           bg;
	XmString        xim;
	Arg             args[15];
	int             argc;
	jobject         target, font, label;
	jobject         thisGlobalRef;

	jbyteArray      bytearray;
	int             blen;
	jbyte          *bytereg;

	target = (*env)->GetObjectField(env, this, MCachedIDs.MComponentPeer_targetFID);
	parentComponentData =
		(struct ComponentData *) (*env)->GetIntField(env, parent, MCachedIDs.MComponentPeer_pDataFID);

	if (parentComponentData == NULL || target == NULL) {
            (*env)->ThrowNew(env, MCachedIDs.java_lang_NullPointerExceptionClass, NULL);
            return;
	}
	AWT_LOCK();

	componentData = (struct ComponentData *)XtCalloc(1, sizeof(struct ComponentData));

	if (componentData == NULL) {
            (*env)->ThrowNew(env, MCachedIDs.java_lang_OutOfMemoryErrorClass, NULL);
            AWT_UNLOCK();
            return;
	}
	font = (*env)->CallObjectMethod(env, target, MCachedIDs.java_awt_Component_getFontMID);
	if ((*env)->ExceptionCheck(env)) {
            AWT_UNLOCK();
            return;
	}
	(*env)->SetIntField(env, this, MCachedIDs.MComponentPeer_pDataFID, (jint) componentData);
	XtVaGetValues(parentComponentData->widget, XmNbackground, &bg, NULL);


	/*
	label = (*env)->GetObjectField(env, target, MCachedIDs.java_awt_Label_labelFID);

	bytearray = (*env)->CallObjectMethod(env, label, MCachedIDs.java_lang_String_getBytesMID);
	blen = (*env)->GetArrayLength(env, bytearray);
	bytereg = (jbyte *) XtCalloc(blen + 1, sizeof(jbyte));
	(*env)->GetByteArrayRegion(env, bytearray, 0, blen, bytereg);
	xim = XmStringCreateLocalized((char *) bytereg);
	XtFree((char *)bytereg);
	*/

	thisGlobalRef = (*env)->NewGlobalRef(env, this);

	argc = 0;
	XtSetArg(args[argc], XmNrecomputeSize, False);
	argc++;
	XtSetArg(args[argc], XmNbackground, bg);
	argc++;
	XtSetArg(args[argc], XmNhighlightOnEnter, False);
	argc++;
	XtSetArg(args[argc], XmNshowAsDefault, 0);
	argc++;
	XtSetArg(args[argc], XmNdefaultButtonShadowThickness, 0);
	argc++;
	XtSetArg(args[argc], XmNmarginTop, 0);
	argc++;
	XtSetArg(args[argc], XmNmarginBottom, 0);
	argc++;
	XtSetArg(args[argc], XmNmarginLeft, 0);
	argc++;
	XtSetArg(args[argc], XmNmarginRight, 0);
	argc++;
	XtSetArg(args[argc], XmNuserData, (XtPointer) thisGlobalRef);
	argc++;
	XtSetArg(args[argc], XmNmappedWhenManaged, False);
	argc++;

        componentData->widget = XmCreateLabel(parentComponentData->widget, "lbl:", args, argc);
        if (xim != NULL) { 
            XtVaSetValues(componentData->widget, XmNlabelString, xim, NULL);
            XmStringFree(xim);
	}

	/*
	if (xim != NULL) {
		XtVaSetValues(componentData->widget, XmNlabelString, xim, NULL);
		XmStringFree(xim);
	}
	*/

	XtManageChild(componentData->widget);
	AWT_FLUSH_UNLOCK();
}

JNIEXPORT void  JNICALL
Java_sun_awt_motif_MLabelPeer_psetText(JNIEnv * env, jobject this, jbyteArray label)
{
	struct ComponentData *wdata;
	XmString        xim;
	jbyte          *bytereg;
	int             blen;
	jobject         target, font;
	XmFontList      xfl;

	AWT_LOCK();

	wdata = (struct ComponentData *) (*env)->GetIntField(env, this, MCachedIDs.MComponentPeer_pDataFID);

	if (wdata == NULL) {
		(*env)->ThrowNew(env, MCachedIDs.java_lang_NullPointerExceptionClass, NULL);
		AWT_UNLOCK();
		return;
	}
	target = (*env)->GetObjectField(env, this, MCachedIDs.MComponentPeer_targetFID);
	font = (*env)->CallObjectMethod(env, target, MCachedIDs.java_awt_Component_getFontMID);

	if ((*env)->ExceptionCheck(env)) {
		AWT_UNLOCK();
		return;
	}
	xfl = getFontList(env, font);

	blen = (*env)->GetArrayLength(env, label);
	bytereg = (jbyte *)XtCalloc(blen + 1, sizeof(jbyte));
	(*env)->GetByteArrayRegion(env, label, 0, blen, bytereg);
	xim = XmStringCreateLocalized((char *) bytereg);
	XtFree((char *)bytereg);

	/*	if (xfl != NULL)
		XtVaSetValues(wdata->widget, XmNfontList, xfl, NULL); */

	if (xim != NULL) {
     		XtVaSetValues(wdata->widget, XmNlabelString, xim, NULL);
		XmStringFree(xim);
	}
	AWT_UNLOCK();

}


JNIEXPORT void  JNICALL
Java_sun_awt_motif_MLabelPeer_setAlignment(JNIEnv * env, jobject this, jint alignment)
{
	struct ComponentData *cdata;

	AWT_LOCK();
	cdata = (struct ComponentData *) (*env)->GetIntField(env, this, MCachedIDs.MComponentPeer_pDataFID);

	if (cdata == NULL || cdata->widget == NULL) {
		(*env)->ThrowNew(env, MCachedIDs.java_lang_NullPointerExceptionClass, NULL);
		AWT_UNLOCK();
		return;
	}
	switch (alignment) {
	case java_awt_Label_LEFT:
		XtVaSetValues(cdata->widget, XmNalignment, XmALIGNMENT_BEGINNING, NULL);
		break;
	case java_awt_Label_CENTER:
		XtVaSetValues(cdata->widget, XmNalignment, XmALIGNMENT_CENTER, NULL);
		break;
	case java_awt_Label_RIGHT:
		XtVaSetValues(cdata->widget, XmNalignment, XmALIGNMENT_END, NULL);
		break;
	default:
		break;
	}
	AWT_UNLOCK();
}
