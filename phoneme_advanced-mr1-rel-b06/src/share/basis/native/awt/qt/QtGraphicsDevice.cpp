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
#include "java_awt_QtGraphicsDevice.h"

#include "awt.h"
#include "QtScreenFactory.h"

JNIEXPORT jint JNICALL
Java_java_awt_QtGraphicsDevice_getScreenWidth (JNIEnv *env, jobject self, 
                                               jint psd)
{
	return (jint)QtScreenFactory::getScreen()->width();
}


JNIEXPORT jint JNICALL
Java_java_awt_QtGraphicsDevice_getScreenHeight (JNIEnv *env, jobject self, 
                                                jint psd)
{
	return (jint)QtScreenFactory::getScreen()->height();
}

JNIEXPORT jint JNICALL
Java_java_awt_QtGraphicsDevice_getScreenX (JNIEnv *env, jobject self, 
                                           jint psd)
{
	return (jint)QtScreenFactory::getScreen()->x();
}


JNIEXPORT jint JNICALL
Java_java_awt_QtGraphicsDevice_getScreenY (JNIEnv *env, jobject self, 
                                           jint psd)
{
	return (jint)QtScreenFactory::getScreen()->y();
}

