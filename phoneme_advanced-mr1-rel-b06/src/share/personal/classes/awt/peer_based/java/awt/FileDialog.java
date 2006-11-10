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
package java.awt;

import sun.awt.peer.FileDialogPeer;
import sun.awt.PeerBasedToolkit;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * The <code>FileDialog</code> class displays a dialog window
 * from which the user can select a file.
 * <p>
 * Since it is a modal dialog, when the application calls
 * its <code>show</code> method to display the dialog,
 * it blocks the rest of the application until the user has
 * chosen a file.
 *
 * @see Window#show
 *
 * @version 	1.16, 08/19/02
 * @author 	Sami Shaio
 * @author 	Arthur van Hoff
 * @since       JDK1.0
 */
public class FileDialog extends Dialog {
    /**
     * This constant value indicates that the purpose of the file
     * dialog window is to locate a file from which to read.
     */
    public static final int LOAD = 0;
    /**
     * This constant value indicates that the purpose of the file
     * dialog window is to locate a file to which to write.
     */
    public static final int SAVE = 1;
    /**
     * There are 2 FileDialog Modes : <code>LOAD<code> and
     * <code>SAVE<code>.
     * This integer will represent one or the other.
     * If the mode is not specified it will default to LOAD.
     *
     * @serial
     * @see getMode()
     * @see setMode()
     * @see java.awt.FileDialog#LOAD
     * @see java.awt.FileDialog#SAVE
     */
    int mode;
    /**
     * The string specifying the directory to display
     * in the file dialog.
     * This variable may be null.
     *
     * @serial
     * @see getDirectory()
     * @see setDirectory()
     */
    String dir;
    /**
     * The string specifying the initial value of the
     * filename text field in the file dialog.
     * This variable may be null.
     *
     * @serial
     * @see getFile()
     * @see setFile()
     */
    String file;
    /**
     * The filter used as the file dialog's filename filter.
     * The file dialog will only be displaying files whose
     * names are accepted by this filter.
     * This variable may be null.
     *
     * @serial
     * @see getFilenameFIlter()
     * @see setFilenameFilter()
     * @see FileNameFilter
     */
    FilenameFilter filter;
    private static final String base = "filedlg";
    private static int nameCounter = 0;
    /*
     * JDK 1.1 serialVersionUID
     */
    private static final long serialVersionUID = 5035145889651310422L;
    /**
     * Creates a file dialog for loading a file.  The title of the
     * file dialog is initially empty.
     * @param parent the owner of the dialog
     * @since JDK1.1
     */
    public FileDialog(Frame parent) {
        this(parent, "", LOAD);
    }

    /**
     * Creates a file dialog window with the specified title for loading
     * a file. The files shown are those in the current directory.
     * @param     parent   the owner of the dialog.
     * @param     title    the title of the dialog.
     */
    public FileDialog(Frame parent, String title) {
        this(parent, title, LOAD);
    }

    /**
     * Creates a file dialog window with the specified title for loading
     * or saving a file.
     * <p>
     * If the value of <code>mode</code> is <code>LOAD</code>, then the
     * file dialog is finding a file to read. If the value of
     * <code>mode</code> is <code>SAVE</code>, the file dialog is finding
     * a place to write a file.
     * @param     parent   the owner of the dialog.
     * @param     title   the title of the dialog.
     * @param     mode   the mode of the dialog.
     * @see       java.awt.FileDialog#LOAD
     * @see       java.awt.FileDialog#SAVE
     */
    public FileDialog(Frame parent, String title, int mode) {
        super(parent, title, true);
        this.setMode(mode);
        setLayout(null);
    }

    /**
     * Construct a name for this component.  Called by getName() when the
     * name is null.
     */
    String constructComponentName() {
        synchronized (getClass()) {
            return base + nameCounter++;
        }
    }

    /**
     * Creates the file dialog's peer.  The peer allows us to change the look
     * of the file dialog without changing its functionality.
     */
    public void addNotify() {
        synchronized (getTreeLock()) {
            if (parent != null && parent.peer == null) {
                parent.addNotify();
            }
            if (peer == null)
                peer = ((PeerBasedToolkit) getToolkit()).createFileDialog(this);
            super.addNotify();
        }
    }

    /**
     * Indicates whether this file dialog box is for loading from a file
     * or for saving to a file.
     * @return   the mode of this file dialog window, either
     *               <code>FileDialog.LOAD</code> or
     *               <code>FileDialog.SAVE</code>.
     * @see      java.awt.FileDialog#LOAD
     * @see      java.awt.FileDialog#SAVE
     * @see      java.awt.FileDialog#setMode
     */
    public int getMode() {
        return mode;
    }

    /**
     * Sets the mode of the file dialog.
     * @param      mode  the mode for this file dialog, either
     *                 <code>FileDialog.LOAD</code> or
     *                 <code>FileDialog.SAVE</code>.
     * @see        java.awt.FileDialog#LOAD
     * @see        java.awt.FileDialog#SAVE
     * @see        java.awt.FileDialog#getMode
     * @exception  IllegalArgumentException if an illegal file
     *                 dialog mode is used.
     * @since      JDK1.1
     */
    public void setMode(int mode) {
        switch (mode) {
        case LOAD:
        case SAVE:
            this.mode = mode;
            break;

        default:
            throw new IllegalArgumentException("illegal file dialog mode");
        }
    }

    /**
     * Gets the directory of this file dialog.
     * @return    the (potentially null or invalid) directory of this
     *            FileDialog.
     * @see       java.awt.FileDialog#setDirectory
     */
    public String getDirectory() {
        return dir;
    }

    /**
     * Sets the directory of this file dialog window to be the
     * specified directory. Specifying a <code>null</code> or an
     * invalid directory implies an implementation-defined default.
     * This default will not be realized, however, until the user
     * has selected a file. Until this point, <code>getDirectory()</code>
     * will return the value passed into this method.<p>
     * Specifying "" as the directory is exactly equivalent to
     * specifying <code>null</code> as the directory.
     * @param     dir   the specific directory.
     * @see       java.awt.FileDialog#getDirectory
     */
    public void setDirectory(String dir) {
        this.dir = (dir != null && dir.equals("")) ? null : dir;
        FileDialogPeer peer = (FileDialogPeer) this.peer;
        if (peer != null) {
            peer.setDirectory(this.dir);
        }
    }

    /**
     * Gets the selected file of this file dialog.
     * @return    the currently selected file of this file dialog window,
     *                or <code>null</code> if none is selected.
     * @see       java.awt.FileDialog#setFile
     */
    public String getFile() {
        return file;
    }

    /**
     * Sets the selected file for this file dialog window to be the
     * specified file. This file becomes the default file if it is set
     * before the file dialog window is first shown.<p> Specifying "" as
     * the file is exactly equivalent to specifying <code>null</code>
     * as the file.
     * @param    file   the file being set.
     * @see      java.awt.FileDialog#getFile
     */
    public void setFile(String file) {
        this.file = (file != null && file.equals("")) ? null : file;
        FileDialogPeer peer = (FileDialogPeer) this.peer;
        if (peer != null) {
            peer.setFile(this.file);
        }
    }
	
    /**
     * Determines this file dialog's filename filter. A filename filter
     * allows the user to specify which files appear in the file dialog
     * window.  Filename filters do not function in Sun's reference
     * implementation for Windows 95, 98, or NT 4.0.
     * @return    this file dialog's filename filter.
     * @see       java.io.FilenameFilter
     * @see       java.awt.FileDialog#setFilenameFilter
     */
    public FilenameFilter getFilenameFilter() {
        return filter;
    }

    /**
     * Sets the filename filter for this file dialog window to the
     * specified filter.
     * Filename filters do not function in Sun's reference
     * implementation for Windows 95, 98, or NT 4.0.
     * @param   filter   the specified filter.
     * @see     java.io.FilenameFilter
     * @see     java.awt.FileDialog#getFilenameFilter
     */
    public synchronized void setFilenameFilter(FilenameFilter filter) {
        this.filter = filter;
        FileDialogPeer peer = (FileDialogPeer) this.peer;
        if (peer != null) {
            peer.setFilenameFilter(filter);
        }
    }

    private void readObject(ObjectInputStream s)
        throws ClassNotFoundException, IOException {
        s.defaultReadObject();
        // 1.1 Compatibility: "" is not converted to null in 1.1
        if (dir != null && dir.equals("")) {
            dir = null;
        }
        if (file != null && file.equals("")) {
            file = null;
        }
    }

    /**
     * Returns the parameter string representing the state of this file
     * dialog window. This string is useful for debugging.
     * @return  the parameter string of this file dialog window.
     */
    protected String paramString() {
        String str = super.paramString();
        str += ",dir= " + dir;
        str += ",file= " + file;
        return str + ((mode == LOAD) ? ",load" : ",save");
    }

    boolean postsOldMouseEvents() {
        return false;
    }
}
