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
#include <stdio.h>

#include <jvmconfig.h>
#include <kni.h>
#include <jvm.h>
#include <jvmspi.h>
#include <sni.h>

#include <midpError.h>
#include <midpString.h>
#include <midpMalloc.h>
#include <midpEvents.h>
#include <midpServices.h>
#include <midpNativeAppManager.h>
#include <midpNamsTestEventProducer.h>
#include <midpUtilKni.h>

#define NAMS_STORAGE_SIZE 20

#define NAMS_STATE_NO 0
#define NAMS_STATE_ACTIVE 1
#define NAMS_STATE_PAUSED 2
#define NAMS_STATE_DESTROYED 3
#define NAMS_STATE_ERROR 4
#define NAMS_STATE_RESERVED 5

static int fgAppId = 0;
static int lastTouchAppId = 0;
static int namsManagerIsolateId = -1;
static int namsNotifierIsolateId = -1;

static int midlet_state[NAMS_STORAGE_SIZE];
static int display_status[NAMS_STORAGE_SIZE];
static int display_change_request[NAMS_STORAGE_SIZE];
static int midlet_state_track[NAMS_STORAGE_SIZE];
static int display_status_track[NAMS_STORAGE_SIZE];

/*
    NamsStorage:
    int getIsolateId();

    native int getMIDletState(int id);
    native boolean getDisplayStatus(int id);
    native boolean getDisplayChangeRequest(int id);
    native boolean getMIDletStateTrack(int id);
    native boolean getDisplayStatusTrack(int id);
    
    native void setMIDletState(int id, int state);
    native void setDisplayStatus(int id, boolean status);
    native void setDisplayChangeRequest(int id, boolean request);
    native void setMIDletStateTrack(int id, boolean track);
    native void setDisplayStatusTrack(int id, boolean track);
*/

KNIEXPORT KNI_RETURNTYPE_INT
Java_com_sun_midp_main_NamsStorage_getNotifierIsolateId(void) {
   KNI_ReturnInt(namsNotifierIsolateId);
}

KNIEXPORT KNI_RETURNTYPE_INT
Java_com_sun_midp_main_NamsStorage_getMIDletState(void) {
   jint appId = KNI_GetParameterAsInt(1);
   KNI_ReturnInt(midlet_state[appId]);
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN
Java_com_sun_midp_main_NamsStorage_getDisplayStatus(void) {
   jint appId = KNI_GetParameterAsInt(1);
   KNI_ReturnBoolean(display_status[appId]);
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN
Java_com_sun_midp_main_NamsStorage_getDisplayChangeRequest(void) {
   jint appId = KNI_GetParameterAsInt(1);
   KNI_ReturnBoolean(display_change_request[appId]);
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN
Java_com_sun_midp_main_NamsStorage_getMIDletStateTrack(void) {
   jint appId = KNI_GetParameterAsInt(1);
   KNI_ReturnBoolean(midlet_state_track[appId]);
}

KNIEXPORT KNI_RETURNTYPE_BOOLEAN
Java_com_sun_midp_main_NamsStorage_getDisplayStatusTrack(void) {
   jint appId = KNI_GetParameterAsInt(1);
   KNI_ReturnBoolean(display_status_track[appId]);
}

KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_midp_main_NamsStorage_setMIDletState(void) {
   jint appId = KNI_GetParameterAsInt(1);
   jint value = KNI_GetParameterAsInt(2);
   midlet_state[appId] = value;
}

KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_midp_main_NamsStorage_setDisplayStatus(void) {
   jint appId = KNI_GetParameterAsInt(1);
   jboolean value = KNI_GetParameterAsBoolean(2);
   display_status[appId] = value;
}

KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_midp_main_NamsStorage_setDisplayChangeRequest(void) {
   jint appId = KNI_GetParameterAsInt(1);
   jboolean value = KNI_GetParameterAsBoolean(2);
   display_change_request[appId] = value;
}

KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_midp_main_NamsStorage_setMIDletStateTrack(void) {
   jint appId = KNI_GetParameterAsInt(1);
   jboolean value = KNI_GetParameterAsBoolean(2);
   midlet_state_track[appId] = value;
}

KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_midp_main_NamsStorage_setDisplayStatusTrack(void) {
   jint appId = KNI_GetParameterAsInt(1);
   jboolean value = KNI_GetParameterAsBoolean(2);
   display_status_track[appId] = value;
}

/**
 * Invokes NAMS API method that starts midlet.
 *
 * @param suiteId suite id (the string reported by listMidlets, not a number)
 * @param className name of the main midlet class (including class path,
 *                          as gets stored in jad)
 * @param appId ID assigned by the external application manager
 */
KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_midp_main_NamsAPIWrapper_midletCreateStart(void) {

    KNI_StartHandles(2);
    
    GET_PARAMETER_AS_PCSL_STRING(1, v_suiteId)
    pcsl_string* const suiteId = &v_suiteId;
    GET_PARAMETER_AS_PCSL_STRING(2, v_className)
    pcsl_string* const className = &v_className;
    jint appId = KNI_GetParameterAsInt(3);

    GET_PCSL_STRING_DATA_AND_LENGTH(suiteId)
    GET_PCSL_STRING_DATA_AND_LENGTH(className)
    if (PCSL_STRING_PARAMETER_ERROR(suiteId)
     || PCSL_STRING_PARAMETER_ERROR(className)) {
        KNI_ThrowNew(midpOutOfMemoryError, NULL);
    } else {
        midp_midlet_create_start(
            (jchar*)suiteId_data, suiteId_len,
            (jchar*)className_data, className_len,
            appId);
    }
    RELEASE_PCSL_STRING_DATA_AND_LENGTH
    RELEASE_PCSL_STRING_DATA_AND_LENGTH

    RELEASE_PCSL_STRING_PARAMETER
    RELEASE_PCSL_STRING_PARAMETER

    KNI_EndHandles();
}

/**
 * Invokes NAMS API method that resumes midlet.
 *
 * @param appId ID assigned by the external application manager
 */
KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_midp_main_NamsAPIWrapper_midletResume(void) {
    jint appId = KNI_GetParameterAsInt(1);
    midp_midlet_resume(appId);
}

/**
 * Invokes NAMS API method that pauses midlet.
 *
 * @param appId ID assigned by the external application manager
 */
KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_midp_main_NamsAPIWrapper_midletPause(void) {
    jint appId = KNI_GetParameterAsInt(1);
    midp_midlet_pause(appId);
}

/**
 * Invokes NAMS API method that destroys midlet.
 *
 * @param appId ID assigned by the external application manager
 */
KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_midp_main_NamsAPIWrapper_midletDestroy(void) {
    jint appId = KNI_GetParameterAsInt(1);
    midp_midlet_destroy(appId);
}

/**
 * Invokes NAMS API method that sets foreground midlet.
 *
 * @param appId ID assigned by the external application manager
 */
KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_midp_main_NamsAPIWrapper_midletSetForeground(void) {
    jint appId = KNI_GetParameterAsInt(1);
    midp_midlet_set_foreground(appId);
}

/**
 * Invokes NAMS API method that sets foreground midlet.
 *
 * @param appId ID assigned by the external application manager
 */
KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_midp_main_NamsAPIWrapper_midpSystemStop(void) {
    midp_system_stop();
}

/*
    NamsNotifier:
    native void initNamsNotifier(int isolateId);
*/
KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_midp_main_NamsNotifier_initNamsNotifier(void) {
    namsNotifierIsolateId = KNI_GetParameterAsInt(1);
}

/*
    NamsManager:
    native void initNamsManager();
    static native int findNextEmptyMIDlet(int appId);
    static native int findNextForegroundMIDlet(int lastAppId);
    static native int getForegroundAppId();
*/

/*
static char* debugGetMIDletStateString(int state) {
    switch (state) {
    case NAMS_STATE_NO:
        return "NO";
    case NAMS_STATE_ACTIVE: 
        return "ACTIVE";
    case NAMS_STATE_PAUSED:
        return "PAUSED";
    case NAMS_STATE_DESTROYED:
        return "DESTROYED";
    case NAMS_STATE_ERROR:
        return "ERROR";
    case NAMS_STATE_RESERVED:
        return "RESERVED";
    default: 
        return "INVALID";
    }
} 

static void debugPrintMIDletEntry(int appId) {
    if (appId < 0 || appId >= NAMS_STORAGE_SIZE) {
        printf("DEBUG: invalid appId = %i\n", appId);
    } else {
        printf("DEBUG: appId = %i", appId);
        printf(" midlet state = %i (%s)", 
            midlet_state[appId], 
            debugGetMIDletStateString(midlet_state[appId]));
        printf(" display status = %i (%s)", 
            display_status[appId], 
            (display_status[appId] != 0) ? "FG" : "BG");
        printf("\n");
    }
}

static void debugPrintMIDletEntries() {
    int i;

    printf("\n");
    printf("DEBUG: Whole MIDlet storage (begin) ...\n");

    for (i = 0; i < NAMS_STORAGE_SIZE; ++i) {
        if ((midlet_state[i] != NAMS_STATE_NO) || 
            (display_status[i] != 0) ||
            (i == lastTouchAppId))
            debugPrintMIDletEntry(i);
    }

    printf("DEBUG: Whole MIDlet storage (end) ...\n");
    printf("\n");
} 
*/
#define nextId(appId) ((1+(appId)) % NAMS_STORAGE_SIZE)

int findNextEmptyMIDlet(int appId) {
    int i;
    int error_candidate = 0;
    int destroyed_candidate = 0;

    for (i = nextId(appId); i != appId; i = nextId(i)) {
        if (i == 0) continue;

        if (midlet_state[i] == NAMS_STATE_NO) {
            midlet_state[i] = NAMS_STATE_RESERVED;
            return i;
        }
        if (error_candidate == 0 && 
            midlet_state[i] == NAMS_STATE_ERROR) {
            error_candidate = i;
        }
        if (destroyed_candidate == 0 && 
            midlet_state[i] == NAMS_STATE_DESTROYED) {
            destroyed_candidate = i;
        }
    }
    midlet_state[i] = NAMS_STATE_RESERVED;
    return (error_candidate != 0) 
        ? error_candidate 
        : (destroyed_candidate != 0) 
            ? destroyed_candidate 
            : 0;
}

int findNextForegroundMIDlet(int appId) {
    int active_candidate = 0;
    /*int paused_candidate = 0;
    int remaining_candidate = 0;*/

    int candidate = 0;

    int i;

    /*
     * Assume that appId is FG midlet.
     * First find Active BG midlets, that request FG.
     * If no such midlets, then find Active BG midlets, that do not request FG.
     * If no such midlets, then find Paused BG midlets, that request FG.
     * If no such midlets, then find Paused BG midlets, that do not request FG.
     * If no midlet found, return current FG midlet (appId).
     */
    for (i = nextId(appId); i != appId; i = nextId(i)) {
        if (i == 0) continue;

        if (midlet_state[i] != NAMS_STATE_NO) {
            if (midlet_state[i] == NAMS_STATE_ACTIVE && 
                display_change_request[i] != 0) {
                return i;
            } else {
                if (active_candidate == 0 && 
                    midlet_state[i] == NAMS_STATE_ACTIVE && 
                    display_change_request[i] == 0)
                    active_candidate = i;
                /*
                if (paused_candidate == 0 && 
                    midlet_state[i] == NAMS_STATE_PAUSED && 
                    display_change_request[i] != 0)
                    paused_candidate = i;
                if (remaining_candidate == 0 && 
                    midlet_state[i] == NAMS_STATE_PAUSED)
                    remaining_candidate = i;
                */
            }
        }
    }
    candidate = (active_candidate != 0) 
        ? active_candidate
        /* : (paused_candidate != 0) 
            ? paused_candidate
            : (paused_candidate != 0) 
                ? remaining_candidate */
                : appId;

     return candidate;
}

/**
 * The typedef of the background listener that is notified 
 * when the background system changes.
 *
 * @param reason              The reason the background change happened
 */
static void nams_background_listener(jint reason) {
    int nextFgAppId;

    printf("DEBUG: NamsManager background_listener(%i)\n", reason);

    // debugPrintMIDletEntries();
    // printf("\n");

    if (namsNotifierIsolateId >= 0 && display_status_track[fgAppId] != 0) {
        /* send test event here ...*/
        nams_send_bg_test_event(namsNotifierIsolateId, reason);

        // printf("DEBUG: Throw Notification: "
        //     "(appId=-1, BG=0, x, reason=%i)\n", reason);
    }

    nextFgAppId = findNextForegroundMIDlet(fgAppId);

    if (display_status[fgAppId] != 0) {
        if (nextFgAppId == fgAppId) 
            nextFgAppId = 0;
        midp_midlet_set_foreground(nextFgAppId);
        // printf("[%i]: ", nextFgAppId);
        // printf("  requested foreground.\n");
    }
}

/**
 * The typedef of the foreground listener that is notified 
 * when the foreground midlet changes.
 *
 * @param appId               The application id used to identify the app
 * @param reason              The reason the foreground change happened
 */
static void nams_foreground_listener(jint appId, jint reason) {
    printf("DEBUG: NamsManager foreground_listener (%i, %i)\n", 
        appId, reason);

    lastTouchAppId = appId;
    // debugPrintMIDletEntries();
    // printf("\n");

    if (namsNotifierIsolateId >= 0 && display_status_track[appId] != 0) {
        /* send test event here ...*/
        nams_send_fg_test_event(namsNotifierIsolateId, appId, reason);

        // printf("DEBUG: Throw Notification: "
        //     "(appId=%i, FG=1, x, reason=%i)\n", appId, reason);
    }

    if (appId >= 0 && appId < NAMS_STORAGE_SIZE) {
        // printf("[%i]: ", appId);
        if (appId == fgAppId && display_status[fgAppId] != 0) {
            // printf(" unchanged foreground status.\n");
        }
        else {
            display_status[fgAppId] = 0;
            display_status[appId] = ~0;
            fgAppId = appId;

            // printf("  got foreground.\n");
        }
    }
}

/**
 * The typedef of the midlet state listener that is notified 
 * with the midlet state changes.
 *
 * @param appId               The application id used to identify the app
 * @param state               The new state of the application
 * @param reason              The reason the state change happened
 */
static void nams_state_change_listener(jint appId, jint state, jint reason) {
    printf("DEBUG: NamsManager state_change_listener(%i, %i, %i)\n", 
        appId, state, reason);
    
    lastTouchAppId = appId;
    // debugPrintMIDletEntries();
    // printf("\n");

    if (namsNotifierIsolateId >= 0 && midlet_state_track[appId] != 0) {
        /* send test event here ...*/
        nams_send_state_test_event(namsNotifierIsolateId,
                                   appId, state, reason);

        // printf("DEBUG: Throw Notification: "
        //     "(appId=%i, STATE=2, state=%i, reason=%i)\n", 
        //     appId, state, reason);
    }
    
    if (appId >= 0 && appId < NAMS_STORAGE_SIZE) {

        // printf("[%i] :", appId);
        if (state == midlet_state[appId]) {
            // printf(" state unchanged: %i\n", state);
        } else {
            // printf(" previous state = %i, new state = %i\n", 
            //     midlet_state[appId], state);
            midlet_state[appId] = state;
        }
    }

    /*
     * In case FG midlet is NamsManager, move new active midlet to FG.
     * All other FG changes are done FG/BG callbacks.
     */
    if ((state == NAMS_STATE_ACTIVE) && (fgAppId == 0)) {
        midp_midlet_set_foreground(appId);
        // printf("[%i]: ", appId);
        // printf("  requested foreground.\n");
    }
}

void initNams(void) {
    int i;

    for (i = 0; i < NAMS_STORAGE_SIZE; ++i) {
        midlet_state[i] = NAMS_STATE_NO;
        display_status[i] = 0;
        display_change_request[i] = 0;
        midlet_state_track[i] = 0;
        display_status_track[i] = 0;
    }

    /* set the listeners before starting the system */
    midp_system_set_background_listener(nams_background_listener);
    midp_midlet_set_foreground_listener(nams_foreground_listener);
    midp_midlet_set_state_change_listener(nams_state_change_listener);
}

KNIEXPORT KNI_RETURNTYPE_VOID
Java_com_sun_midp_main_NamsManager_initNamsManager(void) {
    namsManagerIsolateId = KNI_GetParameterAsInt(1);

    /* request foreground for NamsManager */
    midp_midlet_set_foreground(0);
}

KNIEXPORT KNI_RETURNTYPE_INT 
Java_com_sun_midp_main_NamsManager_findNextEmptyMIDlet() {
    jint appId = KNI_GetParameterAsInt(1);

     KNI_ReturnInt(findNextEmptyMIDlet(appId));
}

KNIEXPORT KNI_RETURNTYPE_INT
Java_com_sun_midp_main_NamsManager_findNextForegroundMIDlet(void) {
    jint appId = KNI_GetParameterAsInt(1);

     KNI_ReturnInt(findNextForegroundMIDlet(appId));
}


KNIEXPORT KNI_RETURNTYPE_INT
Java_com_sun_midp_main_NamsManager_getForegroundAppId(void) {
     KNI_ReturnInt(fgAppId);
}

