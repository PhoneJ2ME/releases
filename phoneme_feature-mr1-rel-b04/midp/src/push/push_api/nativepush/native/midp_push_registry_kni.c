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

/**
 * @file
 *
 * Implementation of Java native methods for the <tt>PushRegistryImpl</tt>
 * class.
 */

#include <string.h>

#include <kni.h>
#include <sni.h>
#include <ROMStructs.h>
#include <commonKNIMacros.h>

#include <nativepush_port_export.h>
#include <midpError.h>
#include <midpUtilKni.h>
#include <midpMalloc.h>

#ifndef MAX_MIDLET_NAME
/** Maximum length of a hostname */
#define MAX_MIDLET_NAME 256
#endif /* MAX_MIDLET_NAME */

/** Check error code and throw exceptions */
static void handlePushError(MIDP_ERROR error) {

	switch (error) {

	case MIDP_ERROR_NONE:
	    /* Success. Return quietly */
	    break;

	case MIDP_ERROR_ILLEGAL_ARGUMENT:
	    KNI_ThrowNew(midpIllegalArgumentException, NULL);
	    break;

	case MIDP_ERROR_UNSUPPORTED:
	    KNI_ThrowNew(midpConnectionNotFoundException, NULL);
	    break;

	case MIDP_ERROR_OUT_OF_RESOURCE:
	case MIDP_ERROR_PUSH_CONNECTION_IN_USE:
	    KNI_ThrowNew(midpIOException, NULL);
	    break;

	case MIDP_ERROR_AMS_MIDLET_NOT_FOUND:
	case MIDP_ERROR_AMS_SUITE_NOT_FOUND:
	    KNI_ThrowNew(midpClassNotFoundException, NULL);
	    break;

	case MIDP_ERROR_PERMISSION_DENIED:
	    KNI_ThrowNew(midpSecurityException, NULL);
	    break;

	default:
	    KNI_ThrowNew(midpRuntimeException, NULL);
	    break;
	}
}

/**
 * Deletes an entry from the push registry.
 * Java Prototype:
 * <pre>
 * static native boolean unregisterConnection0(String suiteId,
 *					       String connection)
 * </pre>
 *
 * @return <tt>true</tt> if the connection was successfully deleted.
 *         <tt>false</tt> if the connection was not found. 
 * @exception SecurityException if connection was found, but,
 *	   it belongs to another MIDlet suite.
 */
KNIEXPORT KNI_RETURNTYPE_BOOLEAN
Java_com_sun_midp_io_j2me_push_PushRegistryImpl_unregisterConnection0() {
    jboolean success = KNI_FALSE;
    jchar* suiteId_data;
    jint suiteId_len;
    jchar* conn_data;
    jint conn_len;
    MIDP_ERROR error;

    KNI_StartHandles(2);
    KNI_DeclareHandle(jSuiteId);
    KNI_DeclareHandle(jConn);

    KNI_GetParameterAsObject(1, jSuiteId);
    suiteId_len = midp_jstring_to_address_and_length(jSuiteId,  &suiteId_data);

    KNI_GetParameterAsObject(2, jConn);
    conn_len = midp_jstring_to_address_and_length(jConn,   &conn_data);

    KNI_EndHandles();

    if (suiteId_len <= NULL_LEN || conn_len <= NULL_LEN) {
        KNI_ThrowNew(midpOutOfMemoryError, NULL);
    } else {
        error = midpport_push_unregister_connection(suiteId_data,
                                suiteId_len,
                                conn_data,
                                conn_len);
        if (error == MIDP_ERROR_NONE) {
            success = KNI_TRUE;
        } else {
            handlePushError(error);
        }
    }

    midpFree(conn_data);
    midpFree(suiteId_data);

    KNI_ReturnBoolean(success);
}

/**
 * Adds a connection to the push registry.
 * Java declaration:
 * <pre>
 * static native void registerConnection0(String suiteId,
 *                                        String connection,
 *                                        String midlet,
 *                                        String filter)
 * </pre>
 *
 * @exception  IllegalArgumentException if the connection string is not
 *               valid
 * @exception ConnectionNotFoundException if the runtime system does not
 *              support push delivery for the requested
 *              connection protocol
 * @exception IOException if the connection is already
 *              registered or if there are insufficient resources
 *              to handle the registration request
 * @exception ClassNotFoundException if the <code>MIDlet</code> class
 *               name can not be found in the current
 *               <code>MIDlet</code> suite
 * @exception SecurityException if the <code>MIDlet</code> does not
 *              have permission to register a connection
 */
KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_midp_io_j2me_push_PushRegistryImpl_registerConnection0() {

    jchar* suite_data;
    jint suite_len;
    jchar* conn_data;
    jint conn_len;
    jchar* midlet_data;
    jint midlet_len;
    jchar* filter_data;
    jint filter_len;

    KNI_StartHandles(4);
    KNI_DeclareHandle(jsuite);
    KNI_DeclareHandle(jconn);
    KNI_DeclareHandle(jmidlet);
    KNI_DeclareHandle(jfilter);

    KNI_GetParameterAsObject(1, jsuite);
    KNI_GetParameterAsObject(2, jconn);
    KNI_GetParameterAsObject(3, jmidlet);
    KNI_GetParameterAsObject(4, jfilter);

    suite_len  = midp_jstring_to_address_and_length(jsuite,  &suite_data);
    conn_len   = midp_jstring_to_address_and_length(jconn,   &conn_data);
    midlet_len = midp_jstring_to_address_and_length(jmidlet, &midlet_data);
    filter_len = midp_jstring_to_address_and_length(jfilter, &filter_data);

    KNI_EndHandles();

    if (suite_len <= NULL_LEN || conn_len <= NULL_LEN
	|| midlet_len <= NULL_LEN || filter_len <= NULL_LEN) {
        KNI_ThrowNew(midpOutOfMemoryError, NULL);
    } else {
        MIDP_ERROR error;
        MIDP_PUSH_ENTRY entry;

        entry.connection 	= conn_data;
        entry.connectionLen	= conn_len;
        entry.midlet		= midlet_data;
        entry.midletLen		= midlet_len;
        entry.filter		= filter_data;
        entry.filterLen		= filter_len;

        error = midpport_push_register_connection(suite_data,
                              suite_len,
                              &entry);

        handlePushError(error);
    }

    midpFree(filter_data);
    midpFree(midlet_data);
    midpFree(conn_data);
    midpFree(suite_data);

    KNI_ReturnVoid();
}

/**
 * Adds an entry to the alarm registry.
 * <p>
 * Java declaration:
 * <pre>
 * static native long registerAlarm0(String suiteId,
 *				     String midlet,
 *				     long time)
 * </pre>
 *
 * @return <tt>0</tt> if this is the first alarm registered with
 *         the given <tt>midlet</tt>, otherwise the time of the
 *         previosly registered alarm.
 */
KNIEXPORT KNI_RETURNTYPE_LONG
Java_com_sun_midp_io_j2me_push_PushRegistryImpl_registerAlarm0() {

    jchar* suite_data;
    jint suite_len;
    jchar* midlet_data;
    jint midlet_len;
    jlong alarm;
    jlong lastalarm = 0;

    KNI_StartHandles(2);
    KNI_DeclareHandle(jsuite);
    KNI_DeclareHandle(jmidlet);

    KNI_GetParameterAsObject(1, jsuite);
    KNI_GetParameterAsObject(2, jmidlet);
    alarm = KNI_GetParameterAsLong(3);

    suite_len = midp_jstring_to_address_and_length(jsuite, &suite_data);
    midlet_len = midp_jstring_to_address_and_length(jmidlet, &midlet_data);

    KNI_EndHandles();

    if (suite_len <= NULL_LEN || midlet_len <= NULL_LEN) {
        KNI_ThrowNew(midpOutOfMemoryError, NULL);
    } else {
        MIDP_ERROR error = midpport_push_register_alarm(suite_data,
                                suite_len,
                                midlet_data,
                                midlet_len,
                                alarm,
                                &lastalarm);
	handlePushError(error);
    }

    midpFree(midlet_data);
    midpFree(suite_data);

    KNI_ReturnLong(lastalarm);
}

/**
 * Gets all registered push connections associated with the given MIDlet
 * suite.
 * <p>
 * Java declaration:
 * <pre>
 * static native String[] listEntries0(String suiteId, boolean available)
 * </pre>
 *
 * @return array of strings which are ordered in triplet
 *		(connection, midlet, filter)
 */
KNIEXPORT KNI_RETURNTYPE_OBJECT
Java_com_sun_midp_io_j2me_push_PushRegistryImpl_listEntries0() {
    
    jchar* suite_data;
    jint suite_len;
    jboolean available;

    KNI_StartHandles(5);

    KNI_DeclareHandle(jsuite);
    KNI_DeclareHandle(jentries);
    KNI_DeclareHandle(jconn);
    KNI_DeclareHandle(jmidlet);
    KNI_DeclareHandle(jfilter);

    KNI_GetParameterAsObject(1, jsuite);
    available = KNI_GetParameterAsBoolean(2);

    suite_len = midp_jstring_to_address_and_length(jsuite, &suite_data);
    if (suite_len <= NULL_LEN) {
        KNI_ThrowNew(midpOutOfMemoryError, NULL);
    } else {
        MIDP_PUSH_ENTRY* entries;
        jint size;

        MIDP_ERROR error = midpport_push_list_entries(suite_data,
                                  suite_len,
                                  available,
                                  &entries,
                                  &size);

        if (error != MIDP_ERROR_NONE) {
            handlePushError(error);
        } else {
            jint i;

            /* Copy entries into Java string array */
            SNI_NewArray(SNI_STRING_ARRAY, 3*size, jentries);
            if (KNI_IsNullHandle(jentries)) {
                KNI_ThrowNew(midpOutOfMemoryError, NULL);
            } else {
                for (i = 0; i < size; i++) {
                    KNI_NewString(entries[i].connection,
                          entries[i].connectionLen,
                          jconn);
                    KNI_NewString(entries[i].midlet,
                          entries[i].midletLen,
                          jmidlet);
                    KNI_NewString(entries[i].filter,
                          entries[i].filterLen,
                          jfilter);

                    if (KNI_IsNullHandle(jconn)   ||
                    KNI_IsNullHandle(jmidlet) ||
                    KNI_IsNullHandle(jfilter))  {
                    KNI_ThrowNew(midpOutOfMemoryError, NULL);
                    break;
                    } else {
                    KNI_SetObjectArrayElement(jentries, 3*i, jconn);
                    KNI_SetObjectArrayElement(jentries, 3*i+1, jmidlet);
                    KNI_SetObjectArrayElement(jentries, 3*i+2, jfilter);
                    }
                }
            }
	    
            /* Free returned entry list */
            for (i = 0; i < size; i++) {
                midpFree(entries[i].connection);
                midpFree(entries[i].midlet);
                midpFree(entries[i].filter);
            }
	        midpFree(entries);
	    }
        midpFree(suite_data);
    }
    KNI_EndHandlesAndReturnObject(jentries);
}
