/*
 * @(#)lfjport_qte_mscreen.cpp	1.38 06/04/05 @(#)
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

#include <qapplication.h>
#include <qmainwindow.h>
#include <qpainter.h>
#include <qpaintdevice.h>
#include <qpaintdevicemetrics.h>
#include <qlayout.h> 
#include <qtoolbar.h> 
#include <qmenubar.h> 
#include <qaction.h> 
#include <qmessagebox.h> 

#include <jvm.h>

#include <keymap_input.h>
#include <midpEventUtil.h>

#include <qteapp_export.h>
#include <qteapp_key.h>

#ifdef ENABLE_JSR_184
#include <swvapi.h>
#endif

#include "lfjport_qte_mscreen.h"
#include <moc_lfjport_qte_mscreen.cpp>

/**
 * @class ChameleonMScreen lfjport_qte_mscreen.h
 *
 * @brief ChameleonMScreen class map to a Java widget based drawing widget
 *
 * @note Even thought we may support mscreen per Java graphics context
 * it is not the way we design. Only one ChameleonMScreen should exists.
 */
ChameleonMScreen::ChameleonMScreen(QWidget *parent, const char* name) : QWidget(parent, name)
{
    vm_stopped = false;

    // Graphics context
    gc = new QPainter();

    // Slots
    connect(&vm_slicer, SIGNAL(timeout()), this, SLOT(slotTimeout()));
}

/**
 * Note: width used in form for layout should be without scrollbar (SCREEN_WIDTH)
 * height returned in form should be SCREEN_HEIGHT
 */
void ChameleonMScreen::init() {

  // Normal screen height and width
  DISPLAY_WIDTH  = width();
  DISPLAY_HEIGHT = height();

  // Full screen mode
  // -Height
  // -Width
  DISPLAY_FULLHEIGHT = qApp->desktop()->height();
  DISPLAY_FULLWIDTH  = qApp->desktop()->width();

  // Set up coordinate for the whole screen
  // SCREEN excludes the scroll bar
  SCREEN_X       = SCREEN_Y = 0;
  SCREEN_WIDTH   = DISPLAY_WIDTH;
  SCREEN_HEIGHT  = DISPLAY_HEIGHT;

  // Set the size of the midlet suites app area
  // @note this will change in full screen mode
  qpixmap.resize(SCREEN_WIDTH, SCREEN_HEIGHT);
  bufferSize = normalScreenSize;

  // IMPL_NOTE:Performance team should benchmark the difference on
  // qpixmap.setOptimization(QPixmap::BestOptim);

  // The widget accepts focus by both tabbing and clicking
  setFocusPolicy(QWidget::StrongFocus);

  // Clean up GC related parameters
  force_refresh = true;
  last_pen = last_brush = -1;
  last_dotted = 0;

#ifdef ENABLE_JSR_184
  engine_initialize();
#endif
}

/**
 * Resize the buffer size (either normal or fullscreen)
 */
void ChameleonMScreen::setBufferSize(BufferSize newSize)
{
    if (newSize != bufferSize) {

       if (newSize == fullScreenSize) {
           qpixmap.resize(DISPLAY_FULLWIDTH, DISPLAY_FULLHEIGHT);
       } else {
           qpixmap.resize(SCREEN_WIDTH, SCREEN_HEIGHT);
       }

       bufferSize = newSize;
    }
}

/**
 * Start VM by starting a time share request for it.
 */
void ChameleonMScreen::startVM() {
    vm_stopped = false;

    // Setup next VM time slice to happen immediately
    midp_resetEvents();

    seen_key_press  = KNI_FALSE;
    painted         = KNI_FALSE;

    setNextVMTimeSlice(0);

}

/**
 * Stop VM by stopping requests for VM time slice.
 */
void ChameleonMScreen::stopVM() {
    // Stop any further VM time slice
    setNextVMTimeSlice(-1);
}

ChameleonMScreen::~ChameleonMScreen()
{
    killTimers();
    delete gc;
    gc = NULL;
#ifdef ENABLE_JSR_184
    engine_uninitialize();
#endif
}

void ChameleonMScreen::mousePressEvent(QMouseEvent *mouse)
{
    MidpEvent evt;

    MIDP_EVENT_INITIALIZE(evt);

    evt.type = MIDP_PEN_EVENT;
    evt.ACTION = PRESSED;
    evt.X_POS = mouse->x();
    evt.Y_POS = mouse->y();

    midpStoreEventAndSignalForeground(evt);
}

void ChameleonMScreen::mouseMoveEvent( QMouseEvent *mouse)
{
    MidpEvent evt;

    MIDP_EVENT_INITIALIZE(evt);

    evt.type = MIDP_PEN_EVENT;
    evt.ACTION = DRAGGED;
    evt.X_POS = mouse->x();
    evt.Y_POS = mouse->y();

    midpStoreEventAndSignalForeground(evt);
}

void ChameleonMScreen::mouseReleaseEvent( QMouseEvent *mouse)
{
    MidpEvent evt;

    MIDP_EVENT_INITIALIZE(evt);


    evt.type = MIDP_PEN_EVENT;
    evt.ACTION = RELEASED;
    evt.X_POS = mouse->x();
    evt.Y_POS = mouse->y();

    midpStoreEventAndSignalForeground(evt);
}

void ChameleonMScreen::keyPressEvent(QKeyEvent *key)
{
    seen_key_press = KNI_TRUE;


#if ENABLE_MULTIPLE_ISOLATES
    if (key->key() == Qt::Key_F12||
        key->key() == Qt::Key_Home) {
        // F12 to display the foreground selector
      if (!key->isAutoRepeat()) {
            MidpEvent evt;
            MIDP_EVENT_INITIALIZE(evt);
            evt.type = SELECT_FOREGROUND_EVENT;
            midpStoreEventAndSignalAms(evt);
        }
    }
#else
    // F12 pause or activate all Java apps
    if ((key->key() == Qt::Key_F12 || key->key() == Qt::Key_Home) &&
        !key->isAutoRepeat()) {
        pauseAll();
    }
#endif

    else {
        MidpEvent evt;
        MIDP_EVENT_INITIALIZE(evt);
 
        if ((evt.CHR = mapKey(key)) != KEY_INVALID) {
          evt.type   = MIDP_KEY_EVENT;
          evt.ACTION = key->isAutoRepeat() ? REPEATED : PRESSED;
          midpStoreEventAndSignalForeground(evt);
        }
    }
}

void ChameleonMScreen::pauseAll() {

  // if (!allPaused) {
      MidpEvent evt;
  
      MIDP_EVENT_INITIALIZE(evt);
      
      evt.type = PAUSE_ALL_EVENT;
      midpStoreEventAndSignalAms(evt);
      allPaused = true;
 // }
}

void ChameleonMScreen::activateAll() {

  if (allPaused) {
    MidpEvent evt;

    MIDP_EVENT_INITIALIZE(evt);
  
    evt.type = ACTIVATE_ALL_EVENT;
    midpStoreEventAndSignalAms(evt);
    allPaused = false;
  }
}

void ChameleonMScreen::keyReleaseEvent(QKeyEvent *key)
{
    if (!seen_key_press || key->isAutoRepeat()) {
        // We may have a left-over keyReleaseEvent from a previous
        // invocation of the VM!
        return;
    }

    MidpEvent evt;

    MIDP_EVENT_INITIALIZE(evt);

    if ((evt.CHR = mapKey(key)) != KEY_INVALID) {
        evt.type = MIDP_KEY_EVENT;
        evt.ACTION = RELEASED;
        midpStoreEventAndSignalForeground(evt);
    }
}

void ChameleonMScreen::resizeEvent(QResizeEvent *event) {
    // We simply ignore it as we don't support resize

  /* Suppress unused-parameter warning */
  (void)event;

}

void ChameleonMScreen::paintEvent(QPaintEvent *e)
{
    QRect r(e->rect());
    refresh(r.left(), r.top(), r.right(), r.bottom());

    /* Done, the Java level does not process repaint events. */
}

/**
 * Create a Color from the packed pixel value.
 */
QColor ChameleonMScreen::getColor(int pixel) {
    int r = (pixel >> 16) & 0xff;
    int g = (pixel >> 8)  & 0xff;
    int b = (pixel >> 0)  & 0xff;
    return QColor(r, g, b);
}

/**
 * Returns 'true' if dst is the current painting device. Otherwise,
 *  return 'false'.
 */
bool
ChameleonMScreen::isCurrentPaintDevice(QPaintDevice *dst) {
    return (gc->device() == dst) ? true : false;
}

/**
 * Set the drawing parameters in the QPainter
 */
QPainter
*ChameleonMScreen::setupGC(int pixel_pen, int pixel_brush, const jshort *clip,
                  QPaintDevice *dst, int dotted) {     
    painted = KNI_TRUE;

    QPaintDevice* dev = gc->device();

    /* default destination is the back buffer */
    if (dst == NULL) {
        dst = (QPaintDevice*)&qpixmap;
    }

    /* finish operation on old device, if changed */
    if ((dev != dst) || force_refresh) {
        if (gc->isActive()) {
            gc->end();
        }
    }       
    
    /* start operation on new device, if needed */
    if (!gc->isActive()) {
        gc->begin(dst);
    }
    
    /* check if pen parameters changed */
    if (((dev != dst)            || 
         (last_pen != pixel_pen) || 
         (last_dotted != dotted) ||     
         force_refresh)) {
           
        if (pixel_pen != -1) {
            QColor color = getColor(pixel_pen);
            QPen pen = QPen(color, 0,
                            (dotted ? Qt::DotLine : Qt::SolidLine));
            gc->setPen(pen);
        } else {
            gc->setPen(Qt::NoPen);
        }
        last_pen = pixel_pen;
        last_dotted = dotted;      
    }

    // check if pen parameters changed
    if (((dev != dst)            || 
         (last_brush != pixel_brush)   || 
         force_refresh)) {
        if (pixel_brush != -1) {
            gc->setBrush(getColor(pixel_brush));
        } else {
            gc->setBrush(Qt::NoBrush);
        }
        last_brush = pixel_brush;

    }


    // check if clipping region changed
    if (clip != NULL &&
        ((dev != dst)            ||
         force_refresh           ||
         (clip[0] != last_clip.left())  ||
         (clip[1] != last_clip.top())   ||
         ((clip[2] - clip[0]) != last_clip.width()) ||
         ((clip[3] - clip[1]) != last_clip.height()))) {
        QRect uclip(clip[0], clip[1], 
                    clip[2] - clip[0], clip[3] - clip[1]);
        last_clip = uclip;
        gc->setClipRect(uclip);
    }

    // drop force_refresh flag after all
    force_refresh = false;
    
    return gc;
}


void ChameleonMScreen::setNextVMTimeSlice(int millis) {
    if (millis < 0) {
        // A negative time means we should stop any active timer.
        if (vm_slicer.isActive()) {
            vm_slicer.stop();
        }
    } else {
        if (vm_slicer.isActive()) {
            vm_slicer.changeInterval(millis);
        } else {
            vm_slicer.start(millis, TRUE);
        }
    }
}

void ChameleonMScreen::slotTimeout() {
    jlong ms;

    if (vm_stopped) {
        return;
    }

    ms = JVM_TimeSlice();

    /* Let the VM run for some time */
    if (ms <= -2) {
        /*
         * JVM_Stop was called. Avoid call JVM_TimeSlice again until
         * startVM is called.
         */
        vm_stopped = true;
        qteapp_get_application()->exit_loop();
    } else if (ms == -1) {
        /* Wait forever -- we probably have a thread blocked on IO or GUI.
         * No need to set up timer from here */
    } else {
        if (ms > 0x7fffffff) {
            vm_slicer.start(0x7fffffff, TRUE);
        } else {
            vm_slicer.start((int)(ms & 0x7fffffff), TRUE);
        }
    }
}

/**
 * Refresh the screen.
 *
 * @param x1 top x coordinate
 * @param y1 top y coordinate
 * @param x2 right hand size bottom x coordinate
 * @param y2 right hand size bottom y coordinate
 */
void ChameleonMScreen::refresh(int x1, int y1, int x2, int y2) {
    if (painted) {
        if (gc->isActive()) {
            gc->end();
        }

        /* Draw the MIDlets screen from the back buffer */
        bitBlt((QPaintDevice*)this, x1 + SCREEN_X, y1 + SCREEN_Y, 
                &qpixmap, x1, y1, (x2 - x1 + 1), (y2 - y1 + 1));
    }
    force_refresh = true;    
    last_pen = last_brush = -1;
}

/**
 * Resets native resources of the device when foreground is gained 
 * by a new Display.
 */
void ChameleonMScreen::gainedForeground() {
  force_refresh  = KNI_TRUE;
  seen_key_press = KNI_FALSE;
  painted        = KNI_FALSE;  
}
