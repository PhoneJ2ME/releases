/*
 * @(#)lfpport_qte_export.cpp	1.6 06/04/05 @(#)
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
 * 
 * This source file is specific for Qt-based configurations.
 */

#include <kni.h>
#include <qteapp_export.h>
#include <lfpport_export.h>
#include "lfpport_qte_mainwindow.h"
#include <midp_logging.h>

/**
 * @file Platform Look and Feel Port interface implementation
 */

static jboolean inFullScreenMode;

#ifdef __cplusplus
extern "C" {
#endif

/**
 * Initializes the LCDUI native resources.
 */
void lfpport_ui_init() {
  inFullScreenMode = KNI_FALSE;
  qteapp_init(lfpport_create_main_window);
}

/**
 * Finalize the LCDUI native resources.
 */
void lfpport_ui_finalize() {
  qteapp_finalize();
}

/**
 * Bridge function to request a repaint 
 * of the area specified.
 *
 * @param x1 top-left x coordinate of the area to refresh
 * @param y1 top-left y coordinate of the area to refresh
 * @param x2 bottom-right x coordinate of the area to refresh
 * @param y2 bottom-right y coordinate of the area to refresh
 */
void lfpport_refresh(int x1, int y1, int x2, int y2)
{
  qteapp_get_mscreen()->refresh(x1, y1, x2, y2);
}

/**
 * Porting API function to update scroll bar.
 *
 * @param scrollPosition current scroll position
 * @param scrollProportion maximum scroll position
 * @return status of this call
 */
int lfpport_set_vertical_scroll(
  int scrollPosition, int scrollProportion) {
  REPORT_CALL_TRACE2(LC_HIGHUI, "LF:STUB:LCDUIsetVerticalScroll(%3d, %3d)\n",
		     scrollPosition, scrollProportion);

  /* Suppress unused parameter warnings */
  (void)scrollPosition;
  (void)scrollProportion;

  return 0;
}


/**
 * Turn on or off the full screen mode
 *
 * @param mode true for full screen mode
 *             false for normal
 */
void lfpport_set_fullscreen_mode(jboolean mode) {
  PlatformMIDPMainWindow * mainWindow = 
    PlatformMIDPMainWindow::getMainWindow();
  mainWindow->setFullScreen(mode);
  inFullScreenMode = mode;
}

/**
 * Bridge function to ask MainWindow object for the full screen mode
 * status.
 */
jboolean lfpport_is_fullscreen_mode() {
  return inFullScreenMode;
}

/**
 * Resets native resources when foreground is gained by a new display.
 */
void lfpport_gained_foreground() {
  qteapp_get_mscreen()->gainedForeground();
}

#ifdef __cplusplus
}
#endif
