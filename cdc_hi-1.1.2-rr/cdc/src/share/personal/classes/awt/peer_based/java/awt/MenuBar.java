/*
 * @(#)MenuBar.java	1.61 06/10/10
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
package java.awt;

import java.util.Vector;
import java.util.Enumeration;
import sun.awt.peer.MenuBarPeer;
import sun.awt.PeerBasedToolkit;
import java.awt.event.KeyEvent;

/**
 * The <code>MenuBar</code> class encapsulates the platform's
 * concept of a menu bar bound to a frame. In order to associate
 * the menu bar with a <code>Frame</code> object, call the
 * frame's <code>setMenuBar</code> method.
 * <p>
 * <A NAME="mbexample"></A><!-- target for cross references -->
 * This is what a menu bar might look like:
 * <p>
 * <img src="doc-files/MenuBar-1.gif"
 * ALIGN=center HSPACE=10 VSPACE=7>
 * <p>
 * A menu bar handles keyboard shortcuts for menu items, passing them
 * along to its child menus.
 * (Keyboard shortcuts, which are optional, provide the user with
 * an alternative to the mouse for invoking a menu item and the
 * action that is associated with it.)
 * Each menu item can maintain an instance of <code>MenuShortcut</code>.
 * The <code>MenuBar</code> class defines several methods,
 * {@link MenuBar#shortcuts} and
 * {@link MenuBar#getShortcutMenuItem}
 * that retrieve information about the shortcuts a given
 * menu bar is managing.
 *
 * @version 1.56, 08/19/02
 * @author Sami Shaio
 * @see        java.awt.Frame
 * @see        java.awt.Frame#setMenuBar(java.awt.MenuBar)
 * @see        java.awt.Menu
 * @see        java.awt.MenuItem
 * @see        java.awt.MenuShortcut
 * @since      JDK1.0
 */
public class MenuBar extends MenuComponent implements MenuContainer {
    /**
     * This field represents a vector of the
     * actual menus that will be part of the MenuBar.
     *
     * @serial
     * @see countMenus()
     */
    Vector menus = new Vector();
    /**
     * This menu is a special menu dedicated to
     * help.  The one thing to note about this menu
     * is that on some platforms it appears at the
     * right edge of the menubar.
     *
     * @serial
     * @see getHelpMenu()
     * @see setHelpMenu()
     */
    Menu helpMenu;
    private static final String base = "menubar";
    private static int nameCounter = 0;
    /*
     * JDK 1.1 serialVersionUID
     */
    private static final long serialVersionUID = -4930327919388951260L;
    /**
     * Creates a new menu bar.
     */
    public MenuBar() {}

    /**
     * Construct a name for this MenuComponent.  Called by getName() when
     * the name is null.
     */
    String constructComponentName() {
        return base + nameCounter++;
    }

    /**
     * Creates the menu bar's peer.  The peer allows us to change the
     * appearance of the menu bar without changing any of the menu bar's
     * functionality.
     */
    public void addNotify() {
        synchronized (getTreeLock()) {
            if (peer == null)
                peer = ((PeerBasedToolkit) Toolkit.getDefaultToolkit()).createMenuBar(this);
            int nmenus = getMenuCount();
            for (int i = 0; i < nmenus; i++) {
                getMenu(i).addNotify();
            }
        }
    }

    /**
     * Removes the menu bar's peer.  The peer allows us to change the
     * appearance of the menu bar without changing any of the menu bar's
     * functionality.
     */
    public void removeNotify() {
        synchronized (getTreeLock()) {
            int nmenus = getMenuCount();
            for (int i = 0; i < nmenus; i++) {
                getMenu(i).removeNotify();
            }
            super.removeNotify();
        }
    }

    /**
     * Gets the help menu on the menu bar.
     * @return    the help menu on this menu bar.
     */
    public Menu getHelpMenu() {
        return helpMenu;
    }

    /**
     * Sets the specified menu to be this menu bar's help menu.
     * If this menu bar has an existing help menu, the old help menu is
     * removed from the menu bar, and replaced with the specified menu.
     * @param m    the menu to be set as the help menu
     */
    public void setHelpMenu(Menu m) {
        synchronized (getTreeLock()) {
            if (helpMenu == m) {
                return;
            }
            if (helpMenu != null) {
                remove(helpMenu);
            }
            if (m.parent != this) {
                add(m);
            }
            helpMenu = m;
            if (m != null) {
                m.isHelpMenu = true;
                m.parent = this;
                MenuBarPeer peer = (MenuBarPeer) this.peer;
                if (peer != null) {
                    if (m.peer == null) {
                        m.addNotify();
                    }
                    peer.addHelpMenu(m);
                }
            }
        }
    }

    /**
     * Adds the specified menu to the menu bar.
     * @param        m   the menu to be added.
     * @return       the menu added.
     * @see          java.awt.MenuBar#remove(int)
     * @see          java.awt.MenuBar#remove(java.awt.MenuComponent)
     * @since        JDK1.0
     */
    public Menu add(Menu m) {
        synchronized (getTreeLock()) {
            if (m.parent != null) {
                m.parent.remove(m);
            }
            menus.addElement(m);
            m.parent = this;
            MenuBarPeer peer = (MenuBarPeer) this.peer;
            if (peer != null) {
                if (m.peer == null) {
                    m.addNotify();
                }
                peer.addMenu(m);
            }
            return m;
        }
    }

    /**
     * Removes the menu located at the specified
     * index from this menu bar.
     * @param        index   the position of the menu to be removed.
     * @see          java.awt.MenuBar#add(java.awt.Menu)
     */
    public void remove(int index) {
        synchronized (getTreeLock()) {
            MenuBarPeer peer = (MenuBarPeer) this.peer;
            if (peer != null) {
                Menu m = getMenu(index);
                m.removeNotify();
                m.parent = null;
                peer.delMenu(index);
            }
            menus.removeElementAt(index);
        }
    }

    /**
     * Removes the specified menu component from this menu bar.
     * @param        m the menu component to be removed.
     * @see          java.awt.MenuBar#add(java.awt.Menu)
     */
    public void remove(MenuComponent m) {
        synchronized (getTreeLock()) {
            int index = menus.indexOf(m);
            if (index >= 0) {
                remove(index);
            }
        }
    }

    /**
     * Gets the number of menus on the menu bar.
     * @return     the number of menus on the menu bar.
     * @since      JDK1.1
     */
    public int getMenuCount() {
        return countMenus();
    }

    /**
     * @deprecated As of JDK version 1.1,
     * replaced by <code>getMenuCount()</code>.
     */
    public int countMenus() {
        return menus.size();
    }

    /**
     * Gets the specified menu.
     * @param      i the index position of the menu to be returned.
     * @return     the menu at the specified index of this menu bar.
     */
    public Menu getMenu(int i) {
        return (Menu) menus.elementAt(i);
    }

    /**
     * Gets an enumeration of all menu shortcuts this menu bar
     * is managing.
     * @return      an enumeration of menu shortcuts that this
     *                      menu bar is managing.
     * @see         java.awt.MenuShortcut
     * @since       JDK1.1
     */
    public synchronized Enumeration shortcuts() {
        Vector shortcuts = new Vector();
        int nmenus = getMenuCount();
        for (int i = 0; i < nmenus; i++) {
            Enumeration e = getMenu(i).shortcuts();
            while (e.hasMoreElements()) {
                shortcuts.addElement(e.nextElement());
            }
        }
        return shortcuts.elements();
    }

    /**
     * Gets the instance of <code>MenuItem</code> associated
     * with the specified <code>MenuShortcut</code> object,
     * or <code>null</code> if none of the menu items being managed
     * by this menu bar is associated with the specified menu
     * shortcut.
     * @param        s the specified menu shortcut.
     * @see          java.awt.MenuItem
     * @see          java.awt.MenuShortcut
     * @since        JDK1.1
     */
    public MenuItem getShortcutMenuItem(MenuShortcut s) {
        int nmenus = getMenuCount();
        for (int i = 0; i < nmenus; i++) {
            MenuItem mi = getMenu(i).getShortcutMenuItem(s);
            if (mi != null) {
                return mi;
            }
        }
        return null;  // MenuShortcut wasn't found
    }

    /*
     * Post an ACTION_EVENT to the target of the MenuPeer
     * associated with the specified keyboard event (on
     * keydown).  Returns true if there is an associated
     * keyboard event.
     */
    boolean handleShortcut(KeyEvent e) {
        // Is it a key event?
        int id = e.getID();
        if (id != KeyEvent.KEY_PRESSED && id != KeyEvent.KEY_RELEASED) {
            return false;
        }
        // Is the accelerator modifier key pressed?
        int accelKey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        if ((e.getModifiers() & accelKey) == 0) {
            return false;
        }
        // Pass MenuShortcut on to child menus.
        int nmenus = getMenuCount();
        for (int i = 0; i < nmenus; i++) {
            Menu m = getMenu(i);
            if (m.handleShortcut(e)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Deletes the specified menu shortcut.
     * @param     s the menu shortcut to delete.
     * @since     JDK1.1
     */
    public void deleteShortcut(MenuShortcut s) {
        int nmenus = getMenuCount();
        for (int i = 0; i < nmenus; i++) {
            getMenu(i).deleteShortcut(s);
        }
    }
    /* Serialization support.  Restore the (transient) parent
     * fields of Menubar menus here.
     */

    private int menuBarSerializedDataVersion = 1;
    /**
     * Writes default serializable fields to stream.  Writes
     * a list of serializable ItemListener(s) as optional data.
     * The non-serializable ItemListner(s) are detected and
     * no attempt is made to serialize them.
     *
     * @serialData Null terminated sequence of 0 or more pairs.
     *             The pair consists of a String and Object.
     *             The String indicates the type of object and
     *             is one of the following :
     *             itemListenerK indicating and ItemListener object.
     *
     * @see AWTEventMulticaster.save(ObjectOutputStream, String, EventListener)
     * @see java.awt.Component.itemListenerK
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.lang.ClassNotFoundException,
            java.io.IOException {
        s.defaultWriteObject();
    }

    /**
     * Read the ObjectInputStream and if it isnt null
     * add a listener to receive item events fired
     * by the MenuBar.
     * Unrecognised keys or values will be Ignored.
     *
     * @see removeActionListener()
     * @see addActionListener()
     */

    private void readObject(java.io.ObjectInputStream s) throws java.lang.ClassNotFoundException, java.io.IOException {
        s.defaultReadObject();
        for (int i = 0; i < menus.size(); i++) {
            Menu m = (Menu) menus.elementAt(i);
            m.parent = this;
        }
    }
}
