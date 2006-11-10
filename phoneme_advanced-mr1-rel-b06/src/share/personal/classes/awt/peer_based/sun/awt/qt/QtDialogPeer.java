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
package sun.awt.qt;

import java.awt.*;
import sun.awt.peer.*;
import java.awt.event.*;

/**
 *
 *
 */

class QtDialogPeer extends QtWindowPeer implements DialogPeer
{
	/** Creates a new QtDialogPeer. */

	QtDialogPeer (QtToolkit toolkit, Dialog target)
	{
            super (toolkit, target);
            setTitle(target.getTitle());
            setResizable(target.isResizable());
            setModal(target.isModal());
	}
	
        protected native void create(QtComponentPeer parentPeer,
                                     boolean isUndecorated);

        protected void create(QtComponentPeer parentPeer) {
            create(parentPeer, ((Dialog)target).isUndecorated());
        }       
	
	public void setTitle(String title)
        {
	    if(title!=null)
	        setTitleNative(title);
	}

        protected native void setTitleNative(String title);

	native void setModal (boolean modal);	
	
	/* Object used for modal wait. When the dialog is hidden we signal this object. */
	
}

