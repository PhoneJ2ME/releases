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
#ifndef _QpFRAME_H_
#define _QpFRAME_H_

#include <qframe.h>
#include <qlabel.h>   //6233632
#include <qstatusbar.h>   //6233632
#include <qstring.h>   //6233632
#include "QtComponentPeer.h"
#include "QpWidget.h"

class QpFrame : public QpWidget {
    public :
        QpFrame(QpWidget *parent, char *name = NULL, int flags = 0);
    /* QFrame Methods */
    void createWarningLabel(QString warningString);    //6233632
    void resizeWarningLabel(void);    //6233632
    int  warningLabelHeight(void);    //6233632
    void setLineWidth(int width);
    void setFrameStyle(int style);
    QRect frameRect(void); 
    void  setFrameRect(QRect rect);
    QRect frameGeometry(void); 
    int frameStyle(void); 
#ifdef QWS
    QPoint getOriginWithDecoration(void);
#endif /* QWS */
    protected :
        enum MethodId {
            SOM = QpWidget::EOM,
            SetLineWidth = QpFrame::SOM,
            SetFrameStyle,
            FrameRect,
            SetFrameRect,
            FrameGeometry,
            FrameStyle,
            CreateWarningLabel,
            ResizeWarningLabel,
            WarningLabelHeight,
#ifdef QWS
            GetOriginWithDecoration,
#endif /* QWS */
            EOM
        };

    virtual void execute(int method, void *args);
    private :
        QLabel *warningStringLabel;   //6233632

    void execCreateWarningLabel(QString warningString);   //6233632
    void execResizeWarningLabel(void);   //6233632
    int  execWarningLabelHeight(void);   //6233632
    void execSetLineWidth(int width);
    void execSetFrameStyle(int style);
    QRect execFrameRect(void);
    void execSetFrameRect(QRect rect);
    QRect execFrameGeometry(void);
    int execFrameStyle(void);
#ifdef QWS
    QPoint execGetOriginWithDecoration(void);
#endif /* QWS */
};

#endif
