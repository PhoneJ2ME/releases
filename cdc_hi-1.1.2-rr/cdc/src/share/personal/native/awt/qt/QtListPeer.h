/*
 * @(#)QtListPeer.h	1.8 06/10/25
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
 */
#ifndef _QtLIST_PEER_H_
#define _QtLIST_PEER_H_

#include "QtComponentPeer.h"
#include <qvector.h>
#include <qobject.h>
#include <qarray.h>
#include <qlistbox.h>
   
#include "QpWidget.h"

class QtListPeer : public QtComponentPeer
{
    Q_OBJECT
public: 
    QtListPeer(JNIEnv* env, jobject thisObj, QpWidget* listWidget);
    ~QtListPeer();

    virtual void dispose(JNIEnv *env);
    void updateItemStateList(int index, int state);
    void removeFromItemStateList(int index); 
    void clearItemStateList(void);
    int getItemState(int row); 

private slots:
    void handleSelectionChanged();
    void handleClicked(QListBoxItem *);
    void handleSelected(QListBoxItem *);

private:
    QArray<int> itemStateList;
    bool        flag;
    QWidget*    viewport;
};


#endif
