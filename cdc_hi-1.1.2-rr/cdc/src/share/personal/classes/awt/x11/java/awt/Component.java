/*
 * @(#)Component.java	1.15 06/10/10
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

/*
 * Warning :
 * Two versions of this file exist in this workspace.
 * One for Personal Basis, and one for Personal Profile.
 * Don't edit the wrong one !!!
 */

package java.awt;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Vector;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.ColorModel;
import java.awt.event.*;
import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.security.AccessController;
import sun.awt.im.InputContext;
import sun.security.action.GetPropertyAction;
import java.lang.reflect.InvocationTargetException;

/**
 * A <em>component</em> is an object having a graphical representation
 * that can be displayed on the screen and that can interact with the
 * user. Examples of components are the buttons, checkboxes, and scrollbars
 * of a typical graphical user interface. <p>
 * The <code>Component</code> class is the abstract superclass of
 * the nonmenu-related Abstract Window Toolkit components. Class
 * <code>Component</code> can also be extended directly to create a
 * lightweight component. A lightweight component is a component that is
 * not associated with a native opaque window.
 *
 * NOTE: Changes to this class need to be synchronized with
 * 	 src/share/optional/pjae/java/awt/Component.java
 *
 * @version 	1.9, 08/19/02
 * @author 	Nicholas Allen
 * @author 	Sami Shaio
 */
public abstract class Component implements ImageObserver, MenuContainer,
        Serializable {
    static Timer updateTimer = new Timer();
    /** If this component delegates to another component then this is the component
     it delegates to. */
	
    transient Component delegate;
    /** If this component is a delegate for an AWT Component then this represents the
     AWT component that delagtes to it. */
	
    transient Component delegateSource;
    /**
     * The XWindow used by this component
     * @see #addNotify
     * @see #removeNotify
     */
    transient ComponentXWindow xwindow;
    /**
     * The parent of the object. It may be null for top-level components.
     * @see #getParent
     */
    transient Container parent;
    /**
     * The x position of the component in the parent's coordinate system.
     * @see #getLocation
     */
    int x;
    /**
     * The y position of the component in the parent's coordinate system.
     * @see #getLocation
     */
    int y;
    /**
     * The width of the component.
     * @see #getSize
     */
    int width;
    /**
     * The height of the component.
     * @see #getSize
     */
    int height;
    /**
     * The foreground color for this component.
     * @see #getForeground
     * @see #setForeground
     */
    Color	foreground;
    /**
     * The background color for this component.
     * @see #getBackground
     * @see #setBackground
     */
    Color	background;
    /**
     * The font used by this component.
     * @see #getFont
     * @see #setFont
     */
    Font	font;
    /**
     * The cursor displayed when pointer is over this component.
     * @see #getCursor
     * @see #setCursor
     */
    Cursor	cursor;
    /**
     * The locale for the component.
     * @see #getLocale
     * @see #setLocale
     */
    Locale      locale;
    /**
     * True when the object is visible. An object that is not
     * visible is not drawn on the screen.
     * @see #isVisible
     * @see #setVisible
     */
    boolean visible = true;
    /**
     * True when the object is enabled. An object that is not
     * enabled does not interact with the user.
     * @see #isEnabled
     * @see #setEnabled
     */
    boolean enabled = true;
    /**
     * True when the object is valid. An invalid object needs to
     * be layed out. This flag is set to false when the object
     * size is changed.
     * @see #isValid
     * @see #validate
     * @see #invalidate
     */
    boolean valid = false;
    Vector popups;
    private String name;
    private boolean nameExplicitlySet = false;
    /**
     * The locking object for AWT component-tree and layout operations.
     *
     * @see #getTreeLock
     */
    static final Object LOCK = new Object();
    /** Internal, cached size information */
    Dimension minSize;
    /** Internal, cached size information */
    Dimension prefSize;
    static final boolean defaultNewEventsOnly = Boolean.getBoolean("java.awt.newEventsOnly");
    boolean newEventsOnly = defaultNewEventsOnly;
    transient ComponentListener componentListener;
    transient FocusListener focusListener;
    transient KeyListener keyListener;
    transient MouseListener mouseListener;
    transient MouseMotionListener mouseMotionListener;
    /** Internal, constants for serialization */
    final static String actionListenerK = "actionL";
    final static String adjustmentListenerK = "adjustmentL";
    final static String componentListenerK = "componentL";
    final static String containerListenerK = "containerL";
    final static String focusListenerK = "focusL";
    final static String itemListenerK = "itemL";
    final static String keyListenerK = "keyL";
    final static String mouseListenerK = "mouseL";
    final static String mouseMotionListenerK = "mouseMotionL";
    final static String textListenerK = "textL";
    final static String windowListenerK = "windowL";
    transient RuntimeException windowClosingException = null;
    // The eventMask is ONLY set by subclasses via enableEvents.
    // The mask should NOT be set when listeners are registered
    // so that we can distinguish the difference between when
    // listeners request events and subclasses request them.
    long eventMask;
    /**
     * Static properties for incremental drawing.
     * @see #imageUpdate
     */
    static boolean isInc;
    static int incRate;
    static {
        String s = (String) AccessController.doPrivileged(
                new GetPropertyAction("awt.image.incrementaldraw"));
        isInc = (s == null || s.equals("true"));
        s = (String) AccessController.doPrivileged(
                    new GetPropertyAction("awt.image.redrawrate"));
        incRate = (s != null) ? Integer.parseInt(s) : 100;
    }
    /**
     * Ease-of-use constant for <code>getAlignmentY()</code>.  Specifies an
     * alignment to the top of the component.
     * @see     #getAlignmentY
     */
    public static final float TOP_ALIGNMENT = 0.0f;
    /**
     * Ease-of-use constant for <code>getAlignmentY</code> and
     * <code>getAlignmentX</code>. Specifies an alignment to
     * the center of the component
     * @see     #getAlignmentX
     * @see     #getAlignmentY
     */
    public static final float CENTER_ALIGNMENT = 0.5f;
    /**
     * Ease-of-use constant for <code>getAlignmentY</code>.  Specifies an
     * alignment to the bottom of the component.
     * @see     #getAlignmentY
     */
    public static final float BOTTOM_ALIGNMENT = 1.0f;
    /**
     * Ease-of-use constant for <code>getAlignmentX</code>.  Specifies an
     * alignment to the left side of the component.
     * @see     #getAlignmentX
     */
    public static final float LEFT_ALIGNMENT = 0.0f;
    /**
     * Ease-of-use constant for <code>getAlignmentX</code>.  Specifies an
     * alignment to the right side of the component.
     * @see     #getAlignmentX
     */
    public static final float RIGHT_ALIGNMENT = 1.0f;
    /*
     * JDK 1.1 serialVersionUID
     */
    private static final long serialVersionUID = -7644114512714619750L;
    /** Invokes the supplied Runnable's run method on the event dispatch thread. This is slightly
     different from the EventQueue.invokeAndWait because it can be called from any thread whereas
     EventQueue.invokeAndWait cannot be called from the event dispatch thread. If called from the
     event dispatch thread the run method will be run immediately. Also, any exceptions thrown are
     caught. This method is convienient if you need to invoke code on a delegate component that is
     not thread safe (e.g. Swing components). */
	
    static void invokeAndWait(Runnable runnable) {
        if (EventQueue.isDispatchThread())
            runnable.run();
        else {
            try {
                EventQueue.invokeAndWait(runnable);
            } catch (InvocationTargetException e) {
                Throwable e1 = e.getTargetException();
                if (e1 instanceof RuntimeException)
                    throw (RuntimeException) e1;
                if (e1 instanceof Error)
                    throw (Error) e1;
                e1.printStackTrace();
                throw new AWTError(e1.toString());
            } catch (InterruptedException e) {
                throw new AWTError("Interrupted");
            }
        }
    }
	
    /**
     * Constructs a new component. Class <code>Component</code> can be
     * extended directly to create a lightweight component that does not
     * utilize an opaque native window. A lightweight component must be
     * hosted by a native container somewhere higher up in the component
     * tree (for example, by a <code>Frame</code> object).
     */
    protected Component() {} // Component()
	
    /**
     * Construct a name for this component.  Called by getName() when the
     * name is null.
     */
    String constructComponentName() {
        return null; // For strict compliance with prior JDKs, a Component
        // that doesn't set its name should return null from
        // getName();
    }
	
    /**
     * Gets the name of the component.
     * @return This component's name.
     * @see    #setName
     * @since JDK1.1
     */
    public String getName() {
        if (name == null && !nameExplicitlySet) {
            synchronized (this) {
                if (name == null && !nameExplicitlySet)
                    name = constructComponentName();
            }
        }
        return name;
    }
	
    /**
     * Sets the name of the component to the specified string.
     * @param <code>name</code>  The string that is to be this
     * component's name.
     * @see #getName
     * @since JDK1.1
     */
    public void setName(String name) {
        synchronized (this) {
            this.name = name;
            nameExplicitlySet = true;
        }
    }
	
    /**
     * Gets the parent of this component.
     * @return The parent container of this component.
     * @since JDK1.0
     */
    public Container getParent() {
        return parent;
    }
	
    /**
     * Gets this component's locking object (the object that owns the thread
     * sychronization monitor) for AWT component-tree and layout
     * operations.
     * @return This component's locking object.
     */
    public final Object getTreeLock() {
        return LOCK;
    }
	
    /**
     * Gets this component's toolkit.
     * Note that the frame that contains a component controls which
     * toolkit is used by that component. Therefore if the component
     * is moved from one frame to another, the toolkit it uses may change.
     * <h3>Compatibility</h3>
     * Personal Profile does not require that an AWT implementation use the
     * peer interfaces, so many methods in the toolkit may throw an
     * UnsupportedOperationException.
     * @return  The toolkit of this component.
     * @see #getPeer
     * @see java.awt.Toolkit
     * @since JDK1.0
     */
    public Toolkit getToolkit() {
        return Toolkit.getDefaultToolkit();
    }
    
    /**
     * Returns true if this component is completely opaque, returns
     * false by default.
     * <p>
     * An opaque component paints every pixel within its
     * rectangular region. A non-opaque component paints only some of
     * its pixels, allowing the pixels underneath it to "show through".
     * A component that does not fully paint its pixels therefore
     * provides a degree of transparency.  Only lightweight
     * components can be transparent.
     * <p>
     * Subclasses that guarantee to always completely paint their
     * contents should override this method and return true.  All
     * of the "heavyweight" AWT components are opaque.
     *
     * @return true if this component is completely opaque.
     * @see #isLightweight
     * @since 1.2
     */
    public boolean isOpaque() {
        return isDisplayable() ? !isLightweight() : false;
    }    
	
    public boolean isDisplayable() {
        return xwindow != null;
    }
	
    public boolean isLightweight() {
        return (xwindow != null) ? isLightweightWhenDisplayable() : false;
    }
	
    /** Returns whether this component is lightweight. This method exists because
     isLightweight is defined to return false when this component is not displayable.
     This allows us to determine if this component is really lightweight, regardless
     of whether it is displayable or not. */
	
    boolean isLightweightWhenDisplayable() {
        return true;
    }
	
    /*
     * Fetch the native container somewhere higher up in the component
     * tree that contains this component.
     */
    Container getNativeContainer() {
        Container p = parent;
        while (p != null && p.isLightweight()) {
            p = p.getParent();
        }
        return p;
    }
	
    /**
     * Determines whether this component is valid. Components are
     * invalidated when they are first shown on the screen.
     * @return <code>true</code> if the component is valid; <code>false</code>
     * otherwise.
     * @see #validate
     * @see #invalidate
     * @since JDK1.0
     */
    public boolean isValid() {
        return valid;
    }
	
    /**
     * Determines whether this component is visible. Components are
     * initially visible, with the exception of top level components such
     * as <code>Frame</code> objects.
     * @return <code>true</code> if the component is visible;
     * <code>false</code> otherwise.
     * @see #setVisible
     * @since JDK1.0
     */
    public boolean isVisible() {
        return visible;
    }
	
    /**
     * Determines whether this component is showing on screen. This means
     * that the component must be visible, and it must be in a container
     * that is visible and showing.
     * @return <code>true</code> if the component is showing;
     * <code>false</code> otherwise.
     * @see #setVisible
     * @since JDK1.0
     */
    public boolean isShowing() {
        return (visible && parent != null && parent.isShowing());
    }
	
    /**
     * Determines whether this component is enabled. An enabled component
     * can respond to user input and generate events. Components are
     * enabled initially by default. A component may be enabled or disabled by
     * calling its <code>setEnabled</code> method.
     * @return <code>true</code> if the component is enabled;
     * <code>false</code> otherwise.
     * @see #setEnabled
     * @since JDK1.0
     */
    public boolean isEnabled() {
        return enabled;
    }
	
    /**
     * Enables or disables this component, depending on the value of the
     * parameter <code>b</code>. An enabled component can respond to user
     * input and generate events. Components are enabled initially by default.
     * @param     <code>b</code>   If <code>true</code>, this component is
     *            enabled; otherwise this component is disabled.
     * @see #isEnabled
     * @since JDK1.1
     */
    public void setEnabled(boolean b) {
        enable(b);
    }
	
    /**
     * @deprecated As of JDK version 1.1,
     * replaced by <code>setEnabled(boolean)</code>.
     */
    public void enable() {
        if (enabled != true) {
            enabled = true;
        }
        if (delegate != null)
            delegate.enable();
    }
	
    /**
     * @deprecated As of JDK version 1.1,
     * replaced by <code>setEnabled(boolean)</code>.
     */
    public void enable(boolean b) {
        if (b) {
            enable();
        } else {
            disable();
        }
    }
	
    /**
     * @deprecated As of JDK version 1.1,
     * replaced by <code>setEnabled(boolean)</code>.
     */
    public void disable() {
        if (enabled != false) {
            enabled = false;
        }
        if (delegate != null)
            delegate.disable();
    }
	
    /**
     * Shows or hides this component depending on the value of parameter
     * <code>b</code>.
     * @param <code>b</code>  If <code>true</code>, shows this component;
     * otherwise, hides this component.
     * @see #isVisible
     * @since JDK1.1
     */
    public void setVisible(boolean b) {
        show(b);
    }
	
    /**
     * @deprecated As of JDK version 1.1,
     * replaced by <code>setVisible(boolean)</code>.
     */
    public void show() {
        if (visible != true) {
            visible = true;
            ComponentXWindow xwindow = this.xwindow;
            if (delegate != null)
                delegate.show();
            if (xwindow != null) {
                xwindow.map();
                repaint();
            }
            if (componentListener != null ||
                (eventMask & AWTEvent.COMPONENT_EVENT_MASK) != 0) {
                ComponentEvent e = new ComponentEvent(this,
                        ComponentEvent.COMPONENT_SHOWN);
                Toolkit.getEventQueue().postEvent(e);
            }
            Container parent = this.parent;
            if (parent != null) {
                parent.invalidate();
            }
        }
    }
	
    /**
     * @deprecated As of JDK version 1.1,
     * replaced by <code>setVisible(boolean)</code>.
     */
    public void show(boolean b) {
        if (b) {
            show();
        } else {
            hide();
        }
    }
	
    /**
     * @deprecated As of JDK version 1.1,
     * replaced by <code>setVisible(boolean)</code>.
     */
    public void hide() {
        if (visible != false) {
            synchronized (getTreeLock()) {
                visible = false;
                ComponentXWindow xwindow = this.xwindow;
                if (delegate != null)
                    delegate.hide();
                if (xwindow != null) {
                    xwindow.unmap();
                    repaint();
                }
                if (componentListener != null ||
                    (eventMask & AWTEvent.COMPONENT_EVENT_MASK) != 0) {
                    ComponentEvent e = new ComponentEvent(this,
                            ComponentEvent.COMPONENT_HIDDEN);
                    Toolkit.getEventQueue().postEvent(e);
                }
                Container parent = this.parent;
                if (parent != null) {
                    parent.invalidate();
                }
            }
        }
    }
	
    /** Gets the component used to delegate setFont, setBackground, setForeground to. */
	
    Component getFontAndColorDelegate() {
        return delegate;
    }
	
    /**
     * Gets the foreground color of this component.
     * @return This component's foreground color. If this component does
     * not have a foreground color, the foreground color of its parent
     * is returned.
     * @see #setForeground(java.awt.Color)
     * @since JDK1.0
     */
    public Color getForeground() {
        Color foreground = this.foreground;
        if (foreground != null) {
            return foreground;
        }
        Component delegate = getFontAndColorDelegate();
        if (delegate != null)
            return delegate.getForeground();
        Container parent = this.parent;
        return (parent != null) ? parent.getForeground() : null;
    }
	
    /**
     * Sets the foreground color of this component.
     * @param <code>c</code> The color to become this component's
     * foreground color.
     * @see #getForeground
     * @since JDK1.0
     */
    public void setForeground(Color c) {
        foreground = c;
        Component delegate = getFontAndColorDelegate();
        if (delegate != null)
            delegate.setForeground(c);
    }
	
    /**
     * Gets the background color of this component.
     * @return This component's background color. If this component does
     * not have a background color, the background color of its parent
     * is returned.
     * @see java.awt.Component#setBackground(java.awt.Color)
     * @since JDK1.0
     */
    public Color getBackground() {
        Color background = this.background;
        if (background != null) {
            return background;
        }
        Component delegate = getFontAndColorDelegate();
        if (delegate != null)
            return delegate.getBackground();
        Container parent = this.parent;
        return (parent != null) ? parent.getBackground() : null;
    }
	
    /**
     * Sets the background color of this component.
     * @param <code>c</code> The color to become this component's
     * background color.
     * @see #getBackground
     * @since JDK1.0
     */
    public void setBackground(Color c) {
        background = c;
        Component delegate = getFontAndColorDelegate();
        if (delegate != null)
            delegate.setBackground(c);
    }
	
    /**
     * Gets the font of this component.
     * @return This component's font. If a font has not been set
     * for this component, the font of its parent is returned.
     * @see #setFont
     * @since JDK1.0
     */
    public Font getFont() {
        Font font = this.font;
        if (font != null) {
            return font;
        }
        Component delegate = getFontAndColorDelegate();
        if (delegate != null)
            return delegate.getFont();
        Container parent = this.parent;
        return (parent != null) ? parent.getFont() : null;
    }
	
    /**
     * Sets the font of this component.
     * @param <code>f</code> The font to become this component's font.
     * @see #getFont
     * @since JDK1.0
     */
    public void setFont(Font f) {
        font = f;
        Component delegate = getFontAndColorDelegate();
        if (delegate != null)
            delegate.setFont(f);
    }
	
    /**
     * Gets the locale of this component.
     * @return This component's locale. If this component does not
     * have a locale, the locale of its parent is returned.
     * @see #setLocale
     * @exception IllegalComponentStateException If the Component
     * does not have its own locale and has not yet been added to
     * a containment hierarchy such that the locale can be determined
     * from the containing parent.
     * @since  JDK1.1
     */
    public Locale getLocale() {
        Locale locale = this.locale;
        if (locale != null) {
            return locale;
        }
        Container parent = this.parent;
        if (parent == null) {
            throw new IllegalComponentStateException("This component must have a parent in order to determine its locale");
        } else {
            return parent.getLocale();
        }
    }
	
    /**
     * Sets the locale of this component.
     * @param <code>l</code> The locale to become this component's locale.
     * @see #getLocale
     * @since JDK1.1
     */
    public void setLocale(Locale l) {
        locale = l;
    }
	
    /**
     * Gets the instance of <code>ColorModel</code> used to display
     * the component on the output device.
     * @return The color model used by this component.
     * @see java.awt.image.ColorModel
     * @see sun.awt.peer.ComponentPeer#getColorModel()
     * @see java.awt.Toolkit#getColorModel()
     * @since JDK1.0
     */
    public ColorModel getColorModel() {
        return getToolkit().getColorModel();
    }
	
    /**
     * Gets the location of this component in the form of a
     * point specifying the component's top-left corner.
     * The location will be relative to the parent's coordinate space.
     * @return An instance of <code>Point</code> representing
     * the top-left corner of the component's bounds in the coordinate
     * space of the component's parent.
     * @see #setLocation
     * @see #getLocationOnScreen
     * @since JDK1.1
     */
    public Point getLocation() {
        return location();
    }
	
    /**
     * Gets the location of this component in the form of a point
     * specifying the component's top-left corner in the screen's
     * coordinate space.
     * @return An instance of <code>Point</code> representing
     * the top-left corner of the component's bounds in the
     * coordinate space of the screen.
     * @see #setLocation
     * @see #getLocation
     */
    public Point getLocationOnScreen() {
        synchronized (getTreeLock()) {
            if (visible && xwindow != null) {
                return xwindow.getLocationOnScreen();
            } else {
                throw new IllegalComponentStateException("component must be showing on the screen to determine its location");
            }
        }
    }
	
    /**
     * @deprecated As of JDK version 1.1,
     * replaced by <code>getLocation()</code>.
     */
    public Point location() {
        return new Point(x, y);
    }
	
    /**
     * Moves this component to a new location. The top-left corner of
     * the new location is specified by the <code>x</code> and <code>y</code>
     * parameters in the coordinate space of this component's parent.
     * @param <code>x</code> The <i>x</i>-coordinate of the new location's
     * top-left corner in the parent's coordinate space.
     * @param <code>y</code> The <i>y</i>-coordinate of the new location's
     * top-left corner in the parent's coordinate space.
     * @see #getLocation
     * @see #setBounds
     * @since JDK1.1
     */
    public void setLocation(int x, int y) {
        move(x, y);
    }
	
    /**
     * @deprecated As of JDK version 1.1,
     * replaced by <code>setLocation(int, int)</code>.
     */
    public void move(int x, int y) {
        setBounds(x, y, width, height);
    }
	
    /**
     * Moves this component to a new location. The top-left corner of
     * the new location is specified by point <code>p</code>. Point
     * <code>p</code> is given in the parent's coordinate space.
     * @param <code>p</code> The point defining the top-left corner
     * of the new location, given in the coordinate space of this
     * component's parent.
     * @see #getLocation
     * @see #setBounds
     * @since JDK1.1
     */
    public void setLocation(Point p) {
        setLocation(p.x, p.y);
    }
	
    /**
     * Returns the size of this component in the form of a
     * <code>Dimension</code> object. The <code>height</code>
     * field of the <code>Dimension</code> object contains
     * this component's height, and the <code>width</code>
     * field of the <code>Dimension</code> object contains
     * this component's width.
     * @return A <code>Dimension</code> object that indicates the
     * size of this component.
     * @see #setSize
     * @since JDK1.1
     */
    public Dimension getSize() {
        return size();
    }
	
    /**
     * @deprecated As of JDK version 1.1,
     * replaced by <code>getSize()</code>.
     */
    public Dimension size() {
        return new Dimension(width, height);
    }
	
    /**
     * Resizes this component so that it has width <code>width</code>
     * and <code>height</code>.
     * @param <code>width</code> The new width of this component in pixels.
     * @param <code>height</code> The new height of this component in pixels.
     * @see #getSize
     * @see #setBounds
     * @since JDK1.1
     */
    public void setSize(int width, int height) {
        resize(width, height);
    }
	
    /**
     * @deprecated As of JDK version 1.1,
     * replaced by <code>setSize(int, int)</code>.
     */
    public void resize(int width, int height) {
        setBounds(x, y, width, height);
    }
	
    /**
     * Resizes this component so that it has width <code>d.width</code>
     * and height <code>d.height</code>.
     * @param <code>d</code> The dimension specifying the new size
     * of this component.
     * @see #setSize
     * @see #setBounds
     * @since JDK1.1
     */
    public void setSize(Dimension d) {
        resize(d);
    }
	
    /**
     * @deprecated As of JDK version 1.1,
     * replaced by <code>setSize(Dimension)</code>.
     */
    public void resize(Dimension d) {
        setSize(d.width, d.height);
    }
	
    /**
     * Gets the bounds of this component in the form of a
     * <code>Rectangle</code> object. The bounds specify this
     * component's width, height, and location relative to
     * its parent.
     * @return A rectangle indicating this component's bounds.
     * @see #setBounds
     * @see #getLocation
     * @see #getSize
     */
    public Rectangle getBounds() {
        return bounds();
    }
	
    /**
     * @deprecated As of JDK version 1.1,
     * replaced by <code>getBounds()</code>.
     */
    public Rectangle bounds() {
        return new Rectangle(x, y, width, height);
    }
	
    /**
     * Moves and resizes this component. The new location of the top-left
     * corner is specified by <code>x</code> and <code>y</code>, and the
     * new size is specified by <code>width</code> and <code>height</code>.
     * @param <code>x</code> The new <i>x</i>-coordinate of this component.
     * @param <code>y</code> The new <i>y</i>-coordinate of this component.
     * @param <code>width</code> The new <code>width</code> of this component.
     * @param <code>height</code> The new <code>height</code> of this
     * component.
     * @see java.awt.Component#getBounds
     * @see java.awt.Component#setLocation(int, int)
     * @see java.awt.Component#setLocation(java.awt.Point)
     * @see java.awt.Component#setSize(int, int)
     * @see java.awt.Component#setSize(java.awt.Dimension)
     * @JDK1.1
     */
    public void setBounds(int x, int y, int width, int height) {
        reshape(x, y, width, height);
    }
	
    /**
     * @deprecated As of JDK version 1.1,
     * replaced by <code>setBounds(int, int, int, int)</code>.
     */
    public void reshape(int x, int y, int width, int height) {
        synchronized (getTreeLock()) {
            boolean resized = (this.width != width) || (this.height != height);
            boolean moved = (this.x != x) || (this.y != y);
            if (resized || moved) {
                if (visible)
                    repaint();
                this.x = x;
                this.y = y;
                this.width = width;
                this.height = height;
                if (delegate != null)
                    delegate.setBounds(x, y, width, height);
                if (xwindow != null) {
                    xwindow.setBounds(x, y, width, height);
                    if (resized)
                        invalidate();
                    if (parent != null && parent.valid)
                        parent.invalidate();
                }
                if (resized && componentListener != null ||
                    (eventMask & AWTEvent.COMPONENT_EVENT_MASK) != 0) {
                    ComponentEvent e = new ComponentEvent(this,
                            ComponentEvent.COMPONENT_RESIZED);
                    Toolkit.getEventQueue().postEvent(e);
                }
                if (moved &&
                    (componentListener != null ||
                        (eventMask & AWTEvent.COMPONENT_EVENT_MASK) != 0)) {
                    ComponentEvent e = new ComponentEvent(this,
                            ComponentEvent.COMPONENT_MOVED);
                    Toolkit.getEventQueue().postEvent(e);
                }
                if (visible) {
                    // Have the parent redraw the area this component *now* occupies.
                    repaint();
                }
            }
        }
    }
	
    /**
     * Moves and resizes this component to conform to the new
     * bounding rectangle <code>r</code>. This component's new
     * position is specified by <code>r.x</code> and <code>r.y</code>,
     * and its new size is specified by <code>r.width</code> and
     * <code>r.height</code>
     * @param <code>r<code> The new bounding rectangle for this component.
     * @see       java.awt.Component#getBounds
     * @see       java.awt.Component#setLocation(int, int)
     * @see       java.awt.Component#setLocation(java.awt.Point)
     * @see       java.awt.Component#setSize(int, int)
     * @see       java.awt.Component#setSize(java.awt.Dimension)
     * @since     JDK1.1
     */
    public void setBounds(Rectangle r) {
        setBounds(r.x, r.y, r.width, r.height);
    }
	
    /**
     * Gets the preferred size of this component.
     * @return A dimension object indicating this component's preferred size.
     * @see #getMinimumSize
     * @see java.awt.LayoutManager
     */
    public Dimension getPreferredSize() {
        return preferredSize();
    }
	
    /**
     * @deprecated As of JDK version 1.1,
     * replaced by <code>getPreferredSize()</code>.
     */
    public Dimension preferredSize() {
        /* Avoid grabbing the lock if a reasonable cached size value
         * is available.
         */
		
        if (delegate != null)
            return delegate.getPreferredSize();
        Dimension dim = prefSize;
        if (dim != null && isValid()) {
            return dim;
        }
        synchronized (getTreeLock()) {
            prefSize = getMinimumSize();
            return prefSize;
        }
    }
	
    /**
     * Gets the mininimum size of this component.
     * @return A dimension object indicating this component's minimum size.
     * @see #getPreferredSize
     * @see java.awt.LayoutManager
     */
    public Dimension getMinimumSize() {
        return minimumSize();
    }
	
    /**
     * @deprecated As of JDK version 1.1,
     * replaced by <code>getMinimumSize()</code>.
     */
    public Dimension minimumSize() {
        if (delegate != null)
            return delegate.getMinimumSize();
            /* Avoid grabbing the lock if a reasonable cached size value
             * is available.
             */
        Dimension dim = minSize;
        if (dim != null && isValid()) {
            return dim;
        }
        synchronized (getTreeLock()) {
            minSize = new Dimension(0, 0);
            return minSize;
        }
    }
	
    /**
     * Gets the maximum size of this component.
     * @return A dimension object indicating this component's maximum size.
     * @see #getMinimumSize
     * @see #getPreferredSize
     * @see LayoutManager
     */
    public Dimension getMaximumSize() {
        if (delegate != null)
            return delegate.getMaximumSize();
        return new Dimension(Short.MAX_VALUE, Short.MAX_VALUE);
    }
	
    /**
     * Returns the alignment along the x axis.  This specifies how
     * the component would like to be aligned relative to other
     * components.  The value should be a number between 0 and 1
     * where 0 represents alignment along the origin, 1 is aligned
     * the furthest away from the origin, 0.5 is centered, etc.
     */
    public float getAlignmentX() {
        return CENTER_ALIGNMENT;
    }
	
    /**
     * Returns the alignment along the y axis.  This specifies how
     * the component would like to be aligned relative to other
     * components.  The value should be a number between 0 and 1
     * where 0 represents alignment along the origin, 1 is aligned
     * the furthest away from the origin, 0.5 is centered, etc.
     */
    public float getAlignmentY() {
        return CENTER_ALIGNMENT;
    }
	
    /**
     * Prompts the layout manager to lay out this component. This is
     * usually called when the component (more specifically, container)
     * is validated.
     * @see #validate
     * @see LayoutManager
     */
    public void doLayout() {
        layout();
    }
	
    /**
     * @deprecated As of JDK version 1.1,
     * replaced by <code>doLayout()</code>.
     */
    public void layout() {}
	
    /**
     * Ensures that this component has a valid layout.  This method is
     * primarily intended to operate on instances of <code>Container</code>.
     * @see       java.awt.Component#invalidate
     * @see       java.awt.Component#doLayout()
     * @see       java.awt.LayoutManager
     * @see       java.awt.Container#validate
     * @since     JDK1.0
     */
    public void validate() {
        if (!valid) {
            synchronized (getTreeLock()) {
                valid = true;
            }
        }
        if (delegate != null)
            delegate.validate();
    }
	
    /**
     * Invalidates this component.  This component and all parents
     * above it are marked as needing to be laid out.  This method can
     * be called often, so it needs to execute quickly.
     * @see       java.awt.Component#validate
     * @see       java.awt.Component#doLayout
     * @see       java.awt.LayoutManager
     * @since     JDK1.0
     */
    public void invalidate() {
        synchronized (getTreeLock()) {
            /* Nullify cached layout and size information.
             * For efficiency, propagate invalidate() upwards only if
             * some other component hasn't already done so first.
             */
            valid = false;
            prefSize = null;
            minSize = null;
            if (parent != null && parent.valid) {
                parent.invalidate();
            }
        }
    }
	
    /**
     * Creates a graphics context for this component. This method will
     * return <code>null</code> if this component is currently not on
     * the screen.
     * @return A graphics context for this component, or <code>null</code>
     *             if it has none.
     * @see       java.awt.Component#paint
     * @since     JDK1.0
     */
    public Graphics getGraphics() {
        Component c = this;
        int x = 0;
        int y = 0;
        while (c != null) {
            ComponentXWindow xwindow = c.xwindow;
            if (xwindow == null)
                return null;
            if (xwindow instanceof HeavyweightComponentXWindow) {
                X11Graphics g = new X11Graphics((HeavyweightComponentXWindow) xwindow);
                g.translate(x, y);
                g.clipRect(0, 0, width, height);
                g.setColor(getForeground());
                g.background = getBackground();
                g.setFont(getFont());
                return g;
            }
            if (c.delegateSource != null)
                c = c.delegateSource;
            else {
                x += c.x;
                y += c.y;
                c = c.parent;
            }
        }
        return null;
    }
	
    /**
     * Gets the font metrics for the specified font.
     * @param <code>font</code> The font for which font metrics is to be
     * obtained.
     * @return The font metrics for <code>font</code>.
     * @param     font   the font.
     * @return    the font metrics for the specified font.
     * @see       java.awt.Component#getFont
     * @see       java.awt.Component#getPeer()
     * @see       sun.awt.peer.ComponentPeer#getFontMetrics(java.awt.Font)
     * @see       java.awt.Toolkit#getFontMetrics(java.awt.Font)
     * @since     JDK1.0
     */
    public FontMetrics getFontMetrics(Font font) {
        return getToolkit().getFontMetrics(font);
    }
	
    /**
     * Sets this components cursor image.
     * <h3>Compatibility</h3>
     * In PersonalJava and PersonalProfile, if the underlying platform does
     * not support cursors or has limited cursor support, the setCursor method
     * can be ignored.
     * <p>
     * If the setCuror method ignores some, but not all cursors, it is important
     * that getCursor() returns a cursor that will not be ignored so that an
     * application can restore the cursor accordingly.
     * @param <code>cursor</code> the non-null cursor image.
     * @see       java.awt.Component#getCursor
     * @see       java.awt.Cursor
     * @since     JDK1.1
     */
    public synchronized void setCursor(Cursor cursor) {
        this.cursor = cursor;
        ComponentXWindow xwindow = this.xwindow;
        if (xwindow != null) {
            xwindow.setCursor(cursor);
        }
        if (delegate != null)
            delegate.setCursor(cursor);
    }
	
    /**
     
     * Gets this component's cursor image.
     * <h3>Compatibility</h3>
     * If setCursor is not always ignored, getCursor() must provide a
     * cursor that will not be ignored when setCursor is called so that an
     * application may restore the cursor after setting it.
     * @return     The cursor for this component.
     * @see        java.awt.Component#setCursor
     * @see        java.awt.Cursor
     * @since      JDK1.1
     */
    public Cursor getCursor() {
        return cursor;
    }
	
    /**
     * Paints this component.  This method is called when the contents
     * of the component should be painted in response to the component
     * first being shown or damage needing repair.  The clip rectangle
     * in the Graphics parameter will be set to the area which needs
     * to be painted.
     * @param <code>g</code> The graphics context to use for painting.
     * @see       java.awt.Component#update
     * @since     JDK1.0
     */
    public void paint(Graphics g) {}
	
    /**
     * Updates this component.
     * <p>
     * The AWT calls the <code>update</code> method in response to a
     * call to <code>repaint</code. The appearance of the
     * component on the screen has not changed since the last call to
     * <code>update</code> or <code>paint</code>. You can assume that
     * the background is not cleared.
     * <p>
     * The <code>update</code>method of <code>Component</code>
     * does the following:
     * <p>
     * <blockquote><ul>
     * <li>Clears this component by filling it
     *      with the background color.
     * <li>Sets the color of the graphics context to be
     *     the foreground color of this component.
     * <li>Calls this component's <code>paint</code>
     *     method to completely redraw this component.
     * </ul></blockquote>
     * <p>
     * The origin of the graphics context, its
     * (<code>0</code>,&nbsp;<code>0</code>) coordinate point, is the
     * top-left corner of this component. The clipping region of the
     * graphics context is the bounding rectangle of this component.
     * @param g the specified context to use for updating.
     * @see       java.awt.Component#paint
     * @see       java.awt.Component#repaint()
     * @since     JDK1.0
     */
    public void update(Graphics g) {
        paint(g);
    }
	
    /**
     * Paints this component and all of its subcomponents.
     * <p>
     * The origin of the graphics context, its
     * (<code>0</code>,&nbsp;<code>0</code>) coordinate point, is the
     * top-left corner of this component. The clipping region of the
     * graphics context is the bounding rectangle of this component.
     * @param     g   the graphics context to use for painting.
     * @see       java.awt.Component#paint
     * @since     JDK1.0
     */
    public void paintAll(Graphics g) {
        if (visible && xwindow != null) {
            validate();
            paint(g);
        }
    }
	
    /**
     * Repaints this component.
     * <p>
     * This method causes a call to this component's <code>update</code>
     * method as soon as possible.
     * @see       java.awt.Component#update(java.awt.Graphics)
     * @since     JDK1.0
     */
    public void repaint() {
        repaint(0, 0, 0, width, height);
    }
	
    /**
     * Repaints the component. This will result in a
     * call to <code>update</code> within <em>tm</em> milliseconds.
     * @param tm maximum time in milliseconds before update
     * @see #paint
     * @see java.awt.Component#update(java.awt.Graphics)
     * @since JDK1.0
     */
    public void repaint(long tm) {
        repaint(tm, 0, 0, width, height);
    }
	
    /**
     * Repaints the specified rectangle of this component.
     * <p>
     * This method causes a call to this component's <code>update</code>
     * method as soon as possible.
     * @param     x   the <i>x</i> coordinate.
     * @param     y   the <i>y</i> coordinate.
     * @param     width   the width.
     * @param     height  the height.
     * @see       java.awt.Component#update(java.awt.Graphics)
     * @since     JDK1.0
     */
    public void repaint(int x, int y, int width, int height) {
        repaint(0, x, y, width, height);
    }
	
    /**
     * Repaints the specified rectangle of this component within
     * <code>tm</code> milliseconds.
     * <p>
     * This method causes a call to this component's
     * <code>update</code> method.
     * @param     tm   maximum time in milliseconds before update.
     * @param     x    the <i>x</i> coordinate.
     * @param     y    the <i>y</i> coordinate.
     * @param     width    the width.
     * @param     height   the height.
     * @see       java.awt.Component#update(java.awt.Graphics)
     * @since     JDK1.0
     */
    public void repaint(long tm, int x, int y, final int width, final int height) {
        Component c = this;
        /* Find outer most heavyweight component to send an PaintEvent.UPDATE event to.
         Beacuse Window, Panel and Canvas are traditionally heavyweight components repaints
         should cause update to be called on these components. Calling repaint on a lightweight
         component should not call update on that component but its heavyweight ancestor. */

        while (c != null) {
            if (!c.visible || c.xwindow == null)
                return;
            if (!c.isLightweight()) {
                if (tm == 0) {
                    Rectangle rect = new Rectangle(x, y, width, height);
                    Toolkit.getEventQueue().postEvent(new PaintEvent (c, PaintEvent.UPDATE, rect));
                } else {
                    final int x1 = x;
                    final int y1 = y;
                    final Component c1 = c;
                    updateTimer.schedule(new TimerTask() {
                            public void run() {
                                repaint(0, x1, y1, width, height);
                            }
                        }, tm);
                }
                return;
            }
            x += c.x;
            y += c.y;
            c = c.parent;
        }
        // Needs to be translated to parent coordinates since
        // a parent native container provides the actual repaint
        // services.  Additionally, the request is restricted to
        // the bounds of the component.
        //		Container parent = this.parent;
        //	  if (parent != null) {
        //	    int px = this.x + ((x < 0) ? 0 : x);
        //	    int py = this.y + ((y < 0) ? 0 : y);
        //	    int pwidth = (width > this.width) ? this.width : width;
        //	    int pheight = (height > this.height) ? this.height : height;
        //	    parent.repaint(tm, px, py, pwidth, pheight);
        //	  }
    }
	
    /**
     * Prints this component. Applications should override this method
     * for components that must do special processing before being
     * printed or should be printed differently than they are painted.
     * <p>
     * The default implementation of this method calls the
     * <code>paint</code> method.
     * <p>
     * The origin of the graphics context, its
     * (<code>0</code>,&nbsp;<code>0</code>) coordinate point, is the
     * top-left corner of this component. The clipping region of the
     * graphics context is the bounding rectangle of this component.
     * @param     g   the graphics context to use for printing.
     * @see       java.awt.Component#paint(java.awt.Graphics)
     * @since     JDK1.0
     */
    public void print(Graphics g) {
        paint(g);
    }
	
    /**
     * Prints this component and all of its subcomponents.
     * <p>
     * The origin of the graphics context, its
     * (<code>0</code>,&nbsp;<code>0</code>) coordinate point, is the
     * top-left corner of this component. The clipping region of the
     * graphics context is the bounding rectangle of this component.
     * @param     g   the graphics context to use for printing.
     * @see       java.awt.Component#print(java.awt.Graphics)
     * @since     JDK1.0
     */
    public void printAll(Graphics g) {
        if (visible && xwindow != null) {
            validate();
            Graphics cg = g.create(0, 0, width, height);
            cg.setFont(getFont());
            try {
                lightweightPrint(g);
            } finally {
                cg.dispose();
            }
        }
    }
	
    /**
     * Simulates the peer callbacks into java.awt for printing of
     * lightweight Components.
     * @param     g   the graphics context to use for printing.
     * @see       #printAll
     */
    void lightweightPrint(Graphics g) {
        print(g);
    }
	
    /**
     * Repaints the component when the image has changed.
     * This <code>imageUpdate</code> method of an <code>ImageObserver</code>
     * is called when more information about an
     * image which had been previously requested using an asynchronous
     * routine such as the <code>drawImage</code> method of
     * <code>Graphics</code> becomes available.
     * See the definition of <code>imageUpdate</code> for
     * more information on this method and its arguments.
     * <p>
     * The <code>imageUpdate</code> method of <code>Component</code>
     * incrementally draws an image on the component as more of the bits
     * of the image are available.
     * <p>
     * If the system property <code>awt.image.incrementalDraw</code>
     * is missing or has the value <code>true</code>, the image is
     * incrementally drawn, If the system property has any other value,
     * then the image is not drawn until it has been completely loaded.
     * <p>
     * Also, if incremental drawing is in effect, the value of the
     * system property <code>awt.image.redrawrate</code> is interpreted
     * as an integer to give the maximum redraw rate, in milliseconds. If
     * the system property is missing or cannot be interpreted as an
     * integer, the redraw rate is once every 100ms.
     * <p>
     * The interpretation of the <code>x</code>, <code>y</code>,
     * <code>width</code>, and <code>height</code> arguments depends on
     * the value of the <code>infoflags</code> argument.
     * @param     img   the image being observed.
     * @param     infoflags   see <code>imageUpdate</code> for more information.
     * @param     x   the <i>x</i> coordinate.
     * @param     y   the <i>y</i> coordinate.
     * @param     width    the width.
     * @param     height   the height.
     * @return    <code>true</code> if the flags indicate that the
     *            image is completely loaded;
     *            <code>false</code> otherwise.
     * @see     java.awt.image.ImageObserver
     * @see     java.awt.Graphics#drawImage(java.awt.Image, int, int, java.awt.Color, java.awt.image.ImageObserver)
     * @see     java.awt.Graphics#drawImage(java.awt.Image, int, int, java.awt.image.ImageObserver)
     * @see     java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, java.awt.Color, java.awt.image.ImageObserver)
     * @see     java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, java.awt.image.ImageObserver)
     * @see     java.awt.image.ImageObserver#imageUpdate(java.awt.Image, int, int, int, int, int)
     * @since   JDK1.0
     */
    public boolean imageUpdate(Image img, int flags,
        int x, int y, int w, int h) {
        int rate = -1;
        if ((flags & (FRAMEBITS | ALLBITS)) != 0) {
            rate = 0;
        } else if ((flags & SOMEBITS) != 0) {
            if (isInc) {
                try {
                    rate = incRate;
                    if (rate < 0)
                        rate = 0;
                } catch (Exception e) {
                    rate = 100;
                }
            }
        }
        if (rate >= 0) {
            repaint(rate, 0, 0, width, height);
        }
        return (flags & (ALLBITS | ABORT)) == 0;
    }
	
    /**
     * Creates an image from the specified image producer.
     * @param     producer  the image producer
     * @return    the image produced.
     * @since     JDK1.0
     */
    public Image createImage(ImageProducer producer) {
        return getToolkit().createImage(producer);
    }
	
    /**
     * Creates an off-screen drawable image
     *     to be used for double buffering.
     * @param     width the specified width.
     * @param     height the specified height.
     * @return    an off-screen drawable image,
     *            which can be used for double buffering.
     * @since     JDK1.0
     */

    /* Some optimization here would be good */

    public Image createImage(int width, int height) {
        // Have to return null if not had addNotify called to be compliant with TCK.
        return (xwindow != null) ? new X11Image(width, height, getBackground()) : null;
    }

    //    public Image createImage(int width, int height) {
    //		return parent.createImage(width, height);
    //    }
	
    /**
     * Prepares an image for rendering on this component.  The image
     * data is downloaded asynchronously in another thread and the
     * appropriate screen representation of the image is generated.
     * @param     image   the <code>Image</code> for which to
     *                    prepare a screen representation.
     * @param     observer   the <code>ImageObserver</code> object
     *                       to be notified as the image is being prepared.
     * @return    <code>true</code> if the image has already been fully prepared;
     <code>false</code> otherwise.
     * @since     JDK1.0
     */
    public boolean prepareImage(Image image, ImageObserver observer) {
        return prepareImage(image, -1, -1, observer);
    }
	
    /**
     * Prepares an image for rendering on this component at the
     * specified width and height.
     * <p>
     * The image data is downloaded asynchronously in another thread,
     * and an appropriately scaled screen representation of the image is
     * generated.
     * @param     image    the instance of <code>Image</code>
     *            for which to prepare a screen representation.
     * @param     width    the width of the desired screen representation.
     * @param     height   the height of the desired screen representation.
     * @param     observer   the <code>ImageObserver</code> object
     *            to be notified as the image is being prepared.
     * @return    <code>true</code> if the image has already been fully prepared;
     <code>false</code> otherwise.
     * @see       java.awt.image.ImageObserver
     * @since     JDK1.0
     */
    public boolean prepareImage(Image image, int width, int height,
        ImageObserver observer) {
        return ((X11Image) image).prepareImage(width, height, observer);
    }
	
    /**
     * Returns the status of the construction of a screen representation
     * of the specified image.
     * <p>
     * This method does not cause the image to begin loading. An
     * application must use the <code>prepareImage</code> method
     * to force the loading of an image.
     * <p>
     * Information on the flags returned by this method can be found
     * with the discussion of the <code>ImageObserver</code> interface.
     * @param     image   the <code>Image</code> object whose status
     *            is being checked.
     * @param     observer   the <code>ImageObserver</code>
     *            object to be notified as the image is being prepared.
     * @return  the bitwise inclusive <b>OR</b> of
     *            <code>ImageObserver</code> flags indicating what
     *            information about the image is currently available.
     * @see      java.awt.Component#prepareImage(java.awt.Image, int, int, java.awt.image.ImageObserver)
     * @see      java.awt.Toolkit#checkImage(java.awt.Image, int, int, java.awt.image.ImageObserver)
     * @see      java.awt.image.ImageObserver
     * @since    JDK1.0
     */
    public int checkImage(Image image, ImageObserver observer) {
        return checkImage(image, -1, -1, observer);
    }
	
    /**
     * Returns the status of the construction of a screen representation
     * of the specified image.
     * <p>
     * This method does not cause the image to begin loading. An
     * application must use the <code>prepareImage</code> method
     * to force the loading of an image.
     * <p>
     * The <code>checkImage</code> method of <code>Component</code>
     * calls its peer's <code>checkImage</code> method to calculate
     * the flags. If this component does not yet have a peer, the
     * component's toolkit's <code>checkImage</code> method is called
     * instead.
     * <p>
     * Information on the flags returned by this method can be found
     * with the discussion of the <code>ImageObserver</code> interface.
     * @param     image   the <code>Image</code> object whose status
     *                    is being checked.
     * @param     width   the width of the scaled version
     *                    whose status is to be checked.
     * @param     height  the height of the scaled version
     *                    whose status is to be checked.
     * @param     observer   the <code>ImageObserver</code> object
     *                    to be notified as the image is being prepared.
     * @return    the bitwise inclusive <b>OR</b> of
     *            <code>ImageObserver</code> flags indicating what
     *            information about the image is currently available.
     * @see      java.awt.Component#prepareImage(java.awt.Image, int, int, java.awt.image.ImageObserver)
     * @see      java.awt.Toolkit#checkImage(java.awt.Image, int, int, java.awt.image.ImageObserver)
     * @see      java.awt.image.ImageObserver
     * @since    JDK1.0
     */
    public int checkImage(Image image, int width, int height,
        ImageObserver observer) {
        return ((X11Image) image).getStatus(observer);
        //        return parent.checkImage(image, width, height, observer);
    }
	
    /**
     * Checks whether this component "contains" the specified point,
     * where <code>x</code> and <code>y</code> are defined to be
     * relative to the coordinate system of this component.
     * @param     x   the <i>x</i> coordinate of the point.
     * @param     y   the <i>y</i> coordinate of the point.
     * @see       java.awt.Component#getComponentAt(int, int)
     * @since     JDK1.1
     */
    public boolean contains(int x, int y) {
        return inside(x, y);
    }
	
    /**
     * @deprecated As of JDK version 1.1,
     * replaced by contains(int, int).
     */
    public boolean inside(int x, int y) {
        return (x >= 0) && (x < width) && (y >= 0) && (y < height);
    }
	
    /**
     * Checks whether this component "contains" the specified point,
     * where the point's <i>x</i> and <i>y</i> coordinates are defined
     * to be relative to the coordinate system of this component.
     * @param     p     the point.
     * @see       java.awt.Component#getComponentAt(java.awt.Point)
     * @since     JDK1.1
     */
    public boolean contains(Point p) {
        return contains(p.x, p.y);
    }
	
    /**
     * Determines if this component or one of its immediate
     * subcomponents contains the (<i>x</i>,&nbsp;<i>y</i>) location,
     * and if so, returns the containing component. This method only
     * looks one level deep. If the point (<i>x</i>,&nbsp;<i>y</i>) is
     * inside a subcomponent that itself has subcomponents, it does not
     * go looking down the subcomponent tree.
     * <p>
     * The <code>locate</code> method of <code>Component</code> simply
     * returns the component itself if the (<i>x</i>,&nbsp;<i>y</i>)
     * coordinate location is inside its bounding box, and <code>null</code>
     * otherwise.
     * @param     x   the <i>x</i> coordinate.
     * @param     y   the <i>y</i> coordinate.
     * @return    the component or subcomponent that contains the
     *                (<i>x</i>,&nbsp;<i>y</i>) location;
     *                <code>null</code> if the location
     *                is outside this component.
     * @see       java.awt.Component#contains(int, int)
     * @since     JDK1.0
     */
    public Component getComponentAt(int x, int y) {
        return locate(x, y);
    }
	
    /**
     * @deprecated As of JDK version 1.1,
     * replaced by getComponentAt(int, int).
     */
    public Component locate(int x, int y) {
        return contains(x, y) ? this : null;
    }
	
    /**
     * Returns the component or subcomponent that contains the
     * specified point.
     * @param     p   the point.
     * @see       java.awt.Component#contains
     * @since     JDK1.1
     */
    public Component getComponentAt(Point p) {
        return getComponentAt(p.x, p.y);
    }
	
    /**
     * @deprecated As of JDK version 1.1,
     * replaced by <code>dispatchEvent(AWTEvent e)</code>.
     */
    public void deliverEvent(Event e) {
        postEvent(e);
    }
	
    /**
     * Dispatches an event to this component or one of its sub components.
     * @param e the event
     */
    public final void dispatchEvent(AWTEvent e) {
        dispatchEventImpl(e);
    }
	
    void dispatchEventImpl(AWTEvent e) {
        int id = e.getID();
        /*
         * 0. Allow the Toolkit to pass this to AWTEventListeners.
         */
        getToolkit().notifyAWTEventListeners(e);
        /*
         * 1. Allow input methods to process the event
         */
        if (areInputMethodsEnabled()
            && (
                // Otherwise, we only pass on low-level events, because
                // a) input methods shouldn't know about semantic events
                // b) passing on the events takes time
                // c) isConsumed() is always true for semantic events.
                // We exclude paint events since they may be numerous and shouldn't matter.
                (e instanceof ComponentEvent) && !(e instanceof PaintEvent))) {
            InputContext inputContext = getInputContext();
            if (inputContext != null) {
                inputContext.dispatchEvent(e);
                if (e.isConsumed()) {
                    return;
                }
            }
        }
        Component focusDelegate = null;
        /*
         * 2. Pre-process any special events before delivery
         */
        switch (id) {
        case KeyEvent.KEY_PRESSED:
        case KeyEvent.KEY_RELEASED:
        case KeyEvent.KEY_TYPED:
            /* Forward key events to the focus delegate if there is one. */
				
            focusDelegate = getFocusDelegate();
            if (focusDelegate != null) {
                KeyEvent keyEvent = (KeyEvent) e;
                focusDelegate.dispatchEvent(new KeyEvent (focusDelegate,
                        keyEvent.id,
                        keyEvent.getWhen(),
                        keyEvent.getModifiers(),
                        keyEvent.getKeyCode(),
                        keyEvent.getKeyChar()));
            } /* Otherwise give the Window a chance to pre process the event.
             We only want to do this for components that aren't being delegated too. */ else if (delegateSource != null) {
                for (Container p = this.parent; p != null; p = p.parent) {
                    if (p.delegateSource != null)
                        break;
                    if (p instanceof Window) {
                        ((Window) p).preProcessKeyEvent((KeyEvent) e);
                        break;
                    }
                }
            }
            break;
			
        case FocusEvent.FOCUS_GAINED:
        case FocusEvent.FOCUS_LOST:
            /* Forward focus events to the focus delegate if there is one. */
				
            focusDelegate = getFocusDelegate();
            if (focusDelegate != null)
                focusDelegate.dispatchEvent(new FocusEvent(focusDelegate, id, ((FocusEvent) e).isTemporary()));
            break;
			
        case PaintEvent.PAINT:
        case PaintEvent.UPDATE:
            Graphics g = getGraphics();
            if (g != null) {
                try {
                    Rectangle clip = ((PaintEvent) e).getUpdateRect();
                    g.clipRect(clip.x, clip.y, clip.width, clip.height);
                    if (id == PaintEvent.PAINT) {
                        if (delegate != null)
                            delegate.paint(g);
                        paint(g);
                    } else update(g);
                    getToolkit().sync();
                } finally {
                    g.dispose();
                }
            }
            break;
				
        default:
            break;
        }
        /*
         * 3. Deliver event for normal processing
         */
        //if (newEventsOnly) {
        // Filtering needs to really be moved to happen at a lower
        // level in order to get maximum performance gain;  it is
        // here temporarily to ensure the API spec is honored.
        //
        if (eventEnabled(e)) {
            processEvent(e);
        }
        //	}else
        if (!newEventsOnly) {
            //
            // backward compatibility
            //
            Event olde = e.convertToOld();
            if (olde != null) {
                int key = olde.key;
                int modifiers = olde.modifiers;
                postEvent(olde);
                if (olde.isConsumed()) {
                    e.consume();
                }
                // if target changed key or modifier values, copy them
                // back to original event
                //
                switch (olde.id) {
                case Event.KEY_PRESS:
                case Event.KEY_RELEASE:
                case Event.KEY_ACTION:
                case Event.KEY_ACTION_RELEASE:
                    if (olde.key != key) {
                        ((KeyEvent) e).setKeyChar(olde.getKeyEventChar());
                    }
                    if (olde.modifiers != modifiers) {
                        ((KeyEvent) e).setModifiers(olde.modifiers);
                    }
                    break;

                default:
                    break;
                }
            }
        }
        /*
         * 4. If no one has consumed a key event, propagate it
         * up the containment hierarchy to ensure that menu shortcuts
         * and keyboard traversal will work properly.
         */
        if (!e.isConsumed() && e instanceof java.awt.event.KeyEvent && delegateSource == null) {
            for (Container c = this.parent; c != null; c = c.parent) {
                if (c.delegateSource != null)
                    break;
                if (c instanceof Window)
                    ((Window) c).postProcessKeyEvent((KeyEvent) e);
            }
        }
    }
	
    protected AWTEvent coalesceEvents(AWTEvent existingEvent, AWTEvent newEvent) {
        int id = existingEvent.id;
        switch (id) {
        case Event.MOUSE_MOVE:
        case Event.MOUSE_DRAG: {
                MouseEvent e = (MouseEvent) existingEvent;
                if (e.getModifiers() == ((MouseEvent) newEvent).getModifiers()) {
                    // Just return the newEvent, causing the old to be
                    // discarded.
                    return newEvent;
                }
                break;
            }
				
        case PaintEvent.PAINT:
        case PaintEvent.UPDATE: {
                // This approach to coalescing paint events seems to be
                // better than any heuristic for unioning rectangles.
                PaintEvent existingPaintEvent = (PaintEvent) existingEvent;
                PaintEvent newPaintEvent = (PaintEvent) newEvent;
                Rectangle existingRect = existingPaintEvent.getUpdateRect();
                Rectangle newRect = newPaintEvent.getUpdateRect();
                if (existingRect.contains(newRect)) {
                    return existingEvent;
                }
                if (newRect.contains(existingRect)) {
                    return newEvent;
                }
                break;
            }
        }
        return null;
    }
	
    boolean areInputMethodsEnabled() {
        // in 1.1.x, we assume input method support is required for all
        // components that handle key events. It's not possible to tell
        // whether they're really interested in character input or just
        // in keystrokes.
        return (eventMask & AWTEvent.KEY_EVENT_MASK) != 0 || keyListener != null;
    }
	
    /**
     * Returns the Window subclass that contains this object. Will
     * return the object itself, if it is a window.
     */
    private Window getWindowForObject(Object obj) {
        if (obj instanceof Component) {
            while (obj != null) {
                if (obj instanceof Window) {
                    return (Window) obj;
                }
                obj = ((Component) obj).getParent();
            }
        }
        return null;
    } // getWindowForObject()
	
    // TODO: remove when filtering is handled at lower level
    boolean eventEnabled(AWTEvent e) {
        switch (e.id) {
        case ComponentEvent.COMPONENT_MOVED:
        case ComponentEvent.COMPONENT_RESIZED:
        case ComponentEvent.COMPONENT_SHOWN:
        case ComponentEvent.COMPONENT_HIDDEN:
            if ((eventMask & AWTEvent.COMPONENT_EVENT_MASK) != 0 ||
                componentListener != null) {
                return true;
            }
            break;

        case FocusEvent.FOCUS_GAINED:
        case FocusEvent.FOCUS_LOST:
            if ((eventMask & AWTEvent.FOCUS_EVENT_MASK) != 0 ||
                focusListener != null) {
                return true;
            }
            break;

        case KeyEvent.KEY_PRESSED:
        case KeyEvent.KEY_RELEASED:
        case KeyEvent.KEY_TYPED:
            if ((eventMask & AWTEvent.KEY_EVENT_MASK) != 0 ||
                keyListener != null) {
                return true;
            }
            break;

        case MouseEvent.MOUSE_PRESSED:
        case MouseEvent.MOUSE_RELEASED:
        case MouseEvent.MOUSE_ENTERED:
        case MouseEvent.MOUSE_EXITED:
        case MouseEvent.MOUSE_CLICKED:
            if ((eventMask & AWTEvent.MOUSE_EVENT_MASK) != 0 ||
                mouseListener != null) {
                return true;
            }
            break;

        case MouseEvent.MOUSE_MOVED:
        case MouseEvent.MOUSE_DRAGGED:
            if ((eventMask & AWTEvent.MOUSE_MOTION_EVENT_MASK) != 0 ||
                mouseMotionListener != null) {
                return true;
            }
            break;

        default:
            break;
        }
        //
        // Always pass on events defined by external programs.
        //
        if (e.id > AWTEvent.RESERVED_ID_MAX) {
            return true;
        }
        return false;
    }
	
    /**
     * @deprecated As of JDK version 1.1,
     * replaced by dispatchEvent(AWTEvent).
     */
    public boolean postEvent(Event e) {
        if (handleEvent(e)) {
            e.consume();
            return true;
        }
        Component parent = this.parent;
        int eventx = e.x;
        int eventy = e.y;
        if (parent != null) {
            e.translate(x, y);
            if (parent.postEvent(e)) {
                e.consume();
                return true;
            }
            // restore coords
            e.x = eventx;
            e.y = eventy;
        }
        return false;
    }
	
    // Event source interfaces
	
    /**
     * Adds the specified component listener to receive component events from
     * this component.
     * @param    l   the component listener.
     * @see      java.awt.event.ComponentEvent
     * @see      java.awt.event.ComponentListener
     * @see      java.awt.Component#removeComponentListener
     * @since    JDK1.1
     */
    public synchronized void addComponentListener(ComponentListener l) {
        componentListener = AWTEventMulticaster.add(componentListener, l);
        checkEnableNewEventsOnly(l);
    }

    /**
     * Removes the specified component listener so that it no longer
     * receives component events from this component.
     * @param    l   the component listener.
     * @see      java.awt.event.ComponentEvent
     * @see      java.awt.event.ComponentListener
     * @see      java.awt.Component#addComponentListener
     * @since    JDK1.1
     */
    public synchronized void removeComponentListener(ComponentListener l) {
        componentListener = AWTEventMulticaster.remove(componentListener, l);
    }
	
    /**
     * Adds the specified focus listener to receive focus events from
     * this component.
     * @param    l   the focus listener.
     * @see      java.awt.event.FocusEvent
     * @see      java.awt.event.FocusListener
     * @see      java.awt.Component#removeFocusListener
     * @since    JDK1.1
     */
    public synchronized void addFocusListener(FocusListener l) {
        focusListener = AWTEventMulticaster.add(focusListener, l);
        checkEnableNewEventsOnly(l);
    }
	
    /**
     * Removes the specified focus listener so that it no longer
     * receives focus events from this component.
     * @param    l   the focus listener.
     * @see      java.awt.event.FocusEvent
     * @see      java.awt.event.FocusListener
     * @see      java.awt.Component#addFocusListener
     * @since    JDK1.1
     */
    public synchronized void removeFocusListener(FocusListener l) {
        focusListener = AWTEventMulticaster.remove(focusListener, l);
    }
	
    /**
     * Adds the specified key listener to receive key events from
     * this component.
     * @param    l   the key listener.
     * @see      java.awt.event.KeyEvent
     * @see      java.awt.event.KeyListener
     * @see      java.awt.Component#removeKeyListener
     * @since    JDK1.1
     */
    public synchronized void addKeyListener(KeyListener l) {
        keyListener = AWTEventMulticaster.add(keyListener, l);
        checkEnableNewEventsOnly(l);
    }
	
    /**
     * Removes the specified key listener so that it no longer
     * receives key events from this component.
     * @param    l   the key listener.
     * @see      java.awt.event.KeyEvent
     * @see      java.awt.event.KeyListener
     * @see      java.awt.Component#addKeyListener
     * @since    JDK1.1
     */
    public synchronized void removeKeyListener(KeyListener l) {
        keyListener = AWTEventMulticaster.remove(keyListener, l);
        checkEnableNewEventsOnly(l);
    }
	
    /** Checks if newEventsOnly should be enabled when the specified listener is added. */
	
    final void checkEnableNewEventsOnly(Object listener) {
        if (!newEventsOnly && listener != null) {
            Package pkg = listener.getClass().getPackage();
            if (pkg == null) {
                newEventsOnly = true;
                return;
            }
            String pkgName = pkg.getName();
            // Only ebnable new event optimisations if the listener is an application class and not
            // a system class.
			
            if (!(pkgName.startsWith("java.awt") ||
                    pkgName.startsWith("javax.swing") ||
                    pkgName.startsWith("com.sun") ||
                    pkgName.startsWith("sun.")))
                newEventsOnly = true;
        }
    }
	
    /**
     * Adds the specified mouse listener to receive mouse events from
     * this component.
     * @param    l   the mouse listener.
     * @see      java.awt.event.MouseEvent
     * @see      java.awt.event.MouseListener
     * @see      java.awt.Component#removeMouseListener
     * @since    JDK1.1
     */
    public synchronized void addMouseListener(MouseListener l) {
        ComponentXWindow xwindow = this.xwindow;
        long oldEventMask = 0;
        if (xwindow != null)
            oldEventMask = getMouseEventMask();
        mouseListener = AWTEventMulticaster.add(mouseListener, l);
        checkEnableNewEventsOnly(l);
        if (xwindow != null) {
            long newEventMask = getMouseEventMask();
            if (newEventMask != oldEventMask)
                xwindow.setMouseEventMask(newEventMask);
        }
    }
	
    /**
     * Removes the specified mouse listener so that it no longer
     * receives mouse events from this component.
     * @param    l   the mouse listener.
     * @see      java.awt.event.MouseEvent
     * @see      java.awt.event.MouseListener
     * @see      java.awt.Component#addMouseListener
     * @since    JDK1.1
     */
    public synchronized void removeMouseListener(MouseListener l) {
        ComponentXWindow xwindow = this.xwindow;
        long oldEventMask = 0;
        if (xwindow != null)
            oldEventMask = getMouseEventMask();
        mouseListener = AWTEventMulticaster.remove(mouseListener, l);
        if (xwindow != null) {
            long newEventMask = getMouseEventMask();
            if (newEventMask != oldEventMask)
                xwindow.setMouseEventMask(newEventMask);
        }
    }
	
    /**
     * Adds the specified mouse motion listener to receive mouse motion events from
     * this component.
     * @param    l   the mouse motion listener.
     * @see      java.awt.event.MouseEvent
     * @see      java.awt.event.MouseMotionListener
     * @see      java.awt.Component#removeMouseMotionListener
     * @since    JDK1.1
     */
    public synchronized void addMouseMotionListener(MouseMotionListener l) {
        ComponentXWindow xwindow = this.xwindow;
        long oldMouseEventMask = 0;
        if (xwindow != null)
            oldMouseEventMask = getMouseEventMask();
        mouseMotionListener = AWTEventMulticaster.add(mouseMotionListener, l);
        checkEnableNewEventsOnly(l);
        if (xwindow != null) {
            long newEventMask = getMouseEventMask();
            if (newEventMask != oldMouseEventMask)
                xwindow.setMouseEventMask(newEventMask);
        }
    }
	
    /**
     * Removes the specified mouse motion listener so that it no longer
     * receives mouse motion events from this component.
     * @param    l   the mouse motion listener.
     * @see      java.awt.event.MouseEvent
     * @see      java.awt.event.MouseMotionListener
     * @see      java.awt.Component#addMouseMotionListener
     * @since    JDK1.1
     */
    public synchronized void removeMouseMotionListener(MouseMotionListener l) {
        ComponentXWindow xwindow = this.xwindow;
        long oldEventMask = 0;
        if (xwindow != null)
            oldEventMask = getMouseEventMask();
        mouseMotionListener = AWTEventMulticaster.remove(mouseMotionListener, l);
        if (xwindow != null) {
            long newEventMask = getMouseEventMask();
            if (newEventMask != oldEventMask)
                xwindow.setMouseEventMask(newEventMask);
        }
    }
	
    /**
     * Gets the input context used by this component for handling the communication
     * with input methods when text is entered in this component. By default, the
     * input context used for the parent component is returned. Components may
     * override this to return a private input context.
     *
     * @return The input context used by this component. Null if no context can
     * be determined.
     */
    InputContext getInputContext() {
        Container parent = this.parent;
        if (parent == null) {
            return null;
        } else {
            return parent.getInputContext();
        }
    }
	
    /**
     * Enables the events defined by the specified event mask parameter
     * to be delivered to this component.
     * <p>
     * Event types are automatically enabled when a listener for
     * that event type is added to the component.
     * <p>
     * This method only needs to be invoked by subclasses of
     * <code>Component</code> which desire to have the specified event
     * types delivered to <code>processEvent</code> regardless of whether
     * or not a listener is registered.
     * @param      eventsToEnable   the event mask defining the event types.
     * @see        java.awt.Component#processEvent
     * @see        java.awt.Component#disableEvents
     * @since      JDK1.1
     */
    protected synchronized final void enableEvents(long eventsToEnable) {
        ComponentXWindow xwindow = this.xwindow;
        long oldEventMask = 0;
        if (xwindow != null)
            oldEventMask = getMouseEventMask();
        eventMask |= eventsToEnable;
        /* Assume we are using the new event model so we can optimize
         events without having to convert to the old event model. */
		
        newEventsOnly = true;
        /* Notify XWindow of any change in the event mask. */
		
        if (xwindow != null) {
            long newEventMask = getMouseEventMask();
            if (newEventMask != oldEventMask)
                xwindow.setMouseEventMask(newEventMask);
        }
    }
	
    /** Gets the low level event mask being used by this component. This is
     determined by what events have been enabled by enableEvents and what
     listeners have been added. Low level evbents are mouse events and keyboard events.
     This mask is used by the XWindow to enable the appropriate X events on the window.*/
	
    private long getMouseEventMask() {
        if (newEventsOnly) {
            long eventMask = this.eventMask & (AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
            if (mouseListener != null)
                eventMask |= AWTEvent.MOUSE_EVENT_MASK;
            if (mouseMotionListener != null)
                eventMask |= AWTEvent.MOUSE_MOTION_EVENT_MASK;
            return eventMask;
        }
        return AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK;
    }
	
    /**
     * Disables the events defined by the specified event mask parameter
     * from being delivered to this component.
     * @param      eventsToDisable   the event mask defining the event types
     * @see        java.awt.Component#enableEvents
     * @since      JDK1.1
     */
    protected final void disableEvents(long eventsToDisable) {
        ComponentXWindow xwindow = this.xwindow;
        long oldEventMask = 0;
        if (xwindow != null)
            oldEventMask = getMouseEventMask();
        eventMask &= ~eventsToDisable;
        /* Notify XWindow of any change in the event mask. */
		
        if (xwindow != null) {
            long newEventMask = getMouseEventMask();
            if (newEventMask != oldEventMask)
                xwindow.setMouseEventMask(newEventMask);
        }
    }
	
    /**
     * Processes events occurring on this component. By default this
     * method calls the appropriate
     * <code>process&lt;event&nbsp;type&gt;Event</code>
     * method for the given class of event.
     * @param     e the event.
     * @see       java.awt.Component#processComponentEvent
     * @see       java.awt.Component#processFocusEvent
     * @see       java.awt.Component#processKeyEvent
     * @see       java.awt.Component#processMouseEvent
     * @see       java.awt.Component#processMouseMotionEvent
     * @since     JDK1.1
     */
    protected void processEvent(AWTEvent e) {
        //System.out.println("Component.processEvent:" + e);
        if (e instanceof FocusEvent) {
            processFocusEvent((FocusEvent) e);
        } else if (e instanceof MouseEvent) {
            switch (e.getID()) {
            case MouseEvent.MOUSE_PRESSED:
            case MouseEvent.MOUSE_RELEASED:
            case MouseEvent.MOUSE_CLICKED:
            case MouseEvent.MOUSE_ENTERED:
            case MouseEvent.MOUSE_EXITED:
                processMouseEvent((MouseEvent) e);
                break;

            case MouseEvent.MOUSE_MOVED:
            case MouseEvent.MOUSE_DRAGGED:
                processMouseMotionEvent((MouseEvent) e);
                break;
            }
        } else if (e instanceof KeyEvent) {
            processKeyEvent((KeyEvent) e);
        } else if (e instanceof ComponentEvent) {
            processComponentEvent((ComponentEvent) e);
        }
    }
    
    /**
     * Processes component events occurring on this component by
     * dispatching them to any registered
     * <code>ComponentListener</code> objects.
     * <p>
     * This method is not called unless component events are
     * enabled for this component. Component events are enabled
     * when one of the following occurs:
     * <p><ul>
     * <li>A <code>ComponentListener</code> object is registered
     * via <code>addComponentListener</code>.
     * <li>Component events are enabled via <code>enableEvents</code>.
     * </ul>
     * @param       e the component event.
     * @see         java.awt.event.ComponentEvent
     * @see         java.awt.event.ComponentListener
     * @see         java.awt.Component#addComponentListener
     * @see         java.awt.Component#enableEvents
     * @since       JDK1.1
     */
    protected void processComponentEvent(ComponentEvent e) {
        if (componentListener != null) {
            int id = e.getID();
            switch (id) {
            case ComponentEvent.COMPONENT_RESIZED:
                componentListener.componentResized(e);
                break;

            case ComponentEvent.COMPONENT_MOVED:
                componentListener.componentMoved(e);
                break;

            case ComponentEvent.COMPONENT_SHOWN:
                componentListener.componentShown(e);
                break;

            case ComponentEvent.COMPONENT_HIDDEN:
                componentListener.componentHidden(e);
                break;
            }
        }
    }
	
    /**
     * Processes focus events occurring on this component by
     * dispatching them to any registered
     * <code>FocusListener</code> objects.
     * <p>
     * This method is not called unless focus events are
     * enabled for this component. Focus events are enabled
     * when one of the following occurs:
     * <p><ul>
     * <li>A <code>FocusListener</code> object is registered
     * via <code>addFocusListener</code>.
     * <li>Focus events are enabled via <code>enableEvents</code>.
     * </ul>
     * @param       e the focus event.
     * @see         java.awt.event.FocusEvent
     * @see         java.awt.event.FocusListener
     * @see         java.awt.Component#addFocusListener
     * @see         java.awt.Component#enableEvents
     * @since       JDK1.1
     */
    protected void processFocusEvent(FocusEvent e) {
        if (focusListener != null) {
            int id = e.getID();
            switch (id) {
            case FocusEvent.FOCUS_GAINED:
                focusListener.focusGained(e);
                break;

            case FocusEvent.FOCUS_LOST:
                focusListener.focusLost(e);
                break;
            }
        }
    }
	
    /**
     * Processes key events occurring on this component by
     * dispatching them to any registered
     * <codeKeyListener</code> objects.
     * <p>
     * This method is not called unless key events are
     * enabled for this component. Key events are enabled
     * when one of the following occurs:
     * <p><ul>
     * <li>A <code>KeyListener</code> object is registered
     * via <code>addKeyListener</code>.
     * <li>Key events are enabled via <code>enableEvents</code>.
     * </ul>
     * @param       e the key event.
     * @see         java.awt.event.KeyEvent
     * @see         java.awt.event.KeyListener
     * @see         java.awt.Component#addKeyListener
     * @see         java.awt.Component#enableEvents
     * @since       JDK1.1
     */
    protected void processKeyEvent(KeyEvent e) {
        if (keyListener != null) {
            int id = e.getID();
            switch (id) {
            case KeyEvent.KEY_TYPED:
                keyListener.keyTyped(e);
                break;

            case KeyEvent.KEY_PRESSED:
                keyListener.keyPressed(e);
                break;

            case KeyEvent.KEY_RELEASED:
                keyListener.keyReleased(e);
                break;
            }
        }
    }
	
    /**
     * Processes mouse events occurring on this component by
     * dispatching them to any registered
     * <code>MouseListener</code> objects.
     * <p>
     * This method is not called unless mouse events are
     * enabled for this component. Mouse events are enabled
     * when one of the following occurs:
     * <p><ul>
     * <li>A <code>MouseListener</code> object is registered
     * via <code>addMouseListener</code>.
     * <li>Mouse events are enabled via <code>enableEvents</code>.
     * </ul>
     * @param       e the mouse event.
     * @see         java.awt.event.MouseEvent
     * @see         java.awt.event.MouseListener
     * @see         java.awt.Component#addMouseListener
     * @see         java.awt.Component#enableEvents
     * @since       JDK1.1
     */
    protected void processMouseEvent(MouseEvent e) {
        if (mouseListener != null) {
            int id = e.getID();
            switch (id) {
            case MouseEvent.MOUSE_PRESSED:
                mouseListener.mousePressed(e);
                break;

            case MouseEvent.MOUSE_RELEASED:
                mouseListener.mouseReleased(e);
                break;

            case MouseEvent.MOUSE_CLICKED:
                mouseListener.mouseClicked(e);
                break;

            case MouseEvent.MOUSE_EXITED:
                mouseListener.mouseExited(e);
                break;

            case MouseEvent.MOUSE_ENTERED:
                mouseListener.mouseEntered(e);
                break;
            }
        }
    }
	
    /**
     * Processes mouse motion events occurring on this component by
     * dispatching them to any registered
     * <code>MouseMotionListener</code> objects.
     * <p>
     * This method is not called unless mouse motion events are
     * enabled for this component. Mouse motion events are enabled
     * when one of the following occurs:
     * <p><ul>
     * <li>A <code>MouseMotionListener</code> object is registered
     * via <code>addMouseMotionListener</code>.
     * <li>Mouse motion events are enabled via <code>enableEvents</code>.
     * </ul>
     * @param       e the mouse motion event.
     * @see         java.awt.event.MouseEvent
     * @see         java.awt.event.MouseMotionListener
     * @see         java.awt.Component#addMouseMotionListener
     * @see         java.awt.Component#enableEvents
     * @since       JDK1.1
     */
    protected void processMouseMotionEvent(MouseEvent e) {
        if (mouseMotionListener != null) {
            int id = e.getID();
            switch (id) {
            case MouseEvent.MOUSE_MOVED:
                mouseMotionListener.mouseMoved(e);
                break;

            case MouseEvent.MOUSE_DRAGGED:
                mouseMotionListener.mouseDragged(e);
                break;
            }
        }
    }
	
    /**
     * @deprecated As of JDK version 1.1
     * replaced by processEvent(AWTEvent).
     */
    public boolean handleEvent(Event evt) {
        switch (evt.id) {
        case Event.MOUSE_ENTER:
            return mouseEnter(evt, evt.x, evt.y);
				
        case Event.MOUSE_EXIT:
            return mouseExit(evt, evt.x, evt.y);
				
        case Event.MOUSE_MOVE:
            return mouseMove(evt, evt.x, evt.y);
				
        case Event.MOUSE_DOWN:
            return mouseDown(evt, evt.x, evt.y);
				
        case Event.MOUSE_DRAG:
            return mouseDrag(evt, evt.x, evt.y);
				
        case Event.MOUSE_UP:
            return mouseUp(evt, evt.x, evt.y);
				
        case Event.KEY_PRESS:
        case Event.KEY_ACTION:
            return keyDown(evt, evt.key);
				
        case Event.KEY_RELEASE:
        case Event.KEY_ACTION_RELEASE:
            return keyUp(evt, evt.key);
				
        case Event.ACTION_EVENT:
            return action(evt, evt.arg);

        case Event.GOT_FOCUS:
            return gotFocus(evt, evt.arg);

        case Event.LOST_FOCUS:
            return lostFocus(evt, evt.arg);
        }
        return false;
    }
	
    /**
     * @deprecated As of JDK version 1.1,
     * replaced by processMouseEvent(MouseEvent).
     */
    public boolean mouseDown(Event evt, int x, int y) {
        return false;
    }
	
    /**
     * @deprecated As of JDK version 1.1,
     * replaced by processMouseMotionEvent(MouseEvent).
     */
    public boolean mouseDrag(Event evt, int x, int y) {
        return false;
    }
	
    /**
     * @deprecated As of JDK version 1.1,
     * replaced by processMouseEvent(MouseEvent).
     */
    public boolean mouseUp(Event evt, int x, int y) {
        return false;
    }
	
    /**
     * @deprecated As of JDK version 1.1,
     * replaced by processMouseMotionEvent(MouseEvent).
     */
    public boolean mouseMove(Event evt, int x, int y) {
        return false;
    }
	
    /**
     * @deprecated As of JDK version 1.1,
     * replaced by processMouseEvent(MouseEvent).
     */
    public boolean mouseEnter(Event evt, int x, int y) {
        return false;
    }
	
    /**
     * @deprecated As of JDK version 1.1,
     * replaced by processMouseEvent(MouseEvent).
     */
    public boolean mouseExit(Event evt, int x, int y) {
        return false;
    }
	
    /**
     * @deprecated As of JDK version 1.1,
     * replaced by processKeyEvent(KeyEvent).
     */
    public boolean keyDown(Event evt, int key) {
        return false;
    }
	
    /**
     * @deprecated As of JDK version 1.1,
     * replaced by processKeyEvent(KeyEvent).
     */
    public boolean keyUp(Event evt, int key) {
        return false;
    }
	
    /**
     * @deprecated As of JDK version 1.1,
     * should register this component as ActionListener on component
     * which fires action events.
     */
    public boolean action(Event evt, Object what) {
        return false;
    }
	
    /**
     * Notifies this component that it has been added to a container
     * and if a peer is required, it should be created.
     * This method should be called by <code>Container.add</code>, and
     * not by user code directly.
     * @see #removeNotify
     * @since JDK1.0
     */
    public void addNotify() {
        synchronized (getTreeLock()) {
            if (xwindow == null) {
                xwindow = createXWindow();
                xwindow.setMouseEventMask(getMouseEventMask());
                xwindow.setBounds(x, y, width, height);
                if (visible)
                    xwindow.map();
            }
            if (delegate != null) {
                delegate.parent = parent;
                delegate.addNotify();
            }
            invalidate();
            //			int npopups = (popups != null? popups.size() : 0);
            //			for (int i = 0 ; i < npopups ; i++) {
            //				PopupMenu popup = (PopupMenu)popups.elementAt(i);
            //				popup.addNotify();
            //			}
        }
    }
	
    ComponentXWindow createXWindow() {
        return isLightweightWhenDisplayable() ? new ComponentXWindow(this) : new HeavyweightComponentXWindow(this);
    }
	
    /**
     * Notifies this component that it has been removed from its
     * container and if a peers exists, it destroys it.
     * This method should be called by <code>Container.remove</code>,
     * and not by user code directly.
     * @see #addNotify
     */
    public void removeNotify() {
        synchronized (getTreeLock()) {
            if (areInputMethodsEnabled()) {
                InputContext inputContext = getInputContext();
                if (inputContext != null) {
                    ComponentEvent e = new ComponentEvent(this,
                            ComponentEvent.COMPONENT_HIDDEN);
                    inputContext.dispatchEvent(e);
                }
            }
            //			int npopups = (popups != null? popups.size() : 0);
            //			for (int i = 0 ; i < npopups ; i++) {
            //				PopupMenu popup = (PopupMenu)popups.elementAt(i);
            //				popup.removeNotify();
            //			}
			
            if (delegate != null) {
                delegate.parent = null;
                delegate.removeNotify();
            }
            if (xwindow != null) {
                ComponentXWindow w = xwindow;
                if (visible) {
                    w.unmap();    // Hide peer first to stop system events such as cursor moves.
                }
                xwindow = null; // Stop peer updates.
                w.dispose();
                Toolkit.getEventQueue().removeSourceEvents(this);
            }
        }
    }
	
    /**
     * @deprecated As of JDK version 1.1,
     * replaced by processFocusEvent(FocusEvent).
     */
    public boolean gotFocus(Event evt, Object what) {
        return false;
    }
	
    /**
     * @deprecated As of JDK version 1.1,
     * replaced by processFocusEvent(FocusEvent).
     */
    public boolean lostFocus(Event evt, Object what) {
        return false;
    }
	
    /**
     * Returns the value of a flag that indicates whether
     * this component can be traversed using
     * Tab or Shift-Tab keyboard focus traversal.  If this method
     * returns "false", this component may still request the keyboard
     * focus using <code>requestFocus()</code>, but it will not automatically
     * be assigned focus during tab traversal.
     * @return    <code>true</code> if this component is
     *            focus-traverable; <code>false</code> otherwise.
     * @since     JDK1.1
     */
    public boolean isFocusTraversable() {
        return getFocusDelegate() != null;
    }
	
    /**
     * Requests that this component get the input focus.
     * <p>
     * This component's <code>gotFocus</code> method is called when this
     * method is successful.  The component must be visible
     * on the screen for this request to be granted
     * @see FocusEvent
     * @see #addFocusListener
     * @see #processFocusEvent
     * @see #isFocusTraversable
     * @since JDK1.0
     */
    public void requestFocus() {
        /* Tell the window that we are requesting focus. */
		
        for (Component c = this; c != null; c = c.parent) {
            if (!c.visible || c.xwindow == null)
                return;
                /* If this component is acting as a delegate for another AWT component
                 then request focus on that component. */
			
            if (c.delegateSource != null) {
                c.delegateSource.requestFocus();
                return;
            }
            if (c instanceof Window) {
                ((Window) c).setFocusOwner(this);
                return;
            }
        }
    }
	
    /** Gets the component to which key events should be deleagted to when this component
     has the focus. */
	
    Component getFocusDelegate() {
        if (delegate != null) {
            if (delegate.isFocusTraversable())
                return delegate;
            if (delegate instanceof Container) {
                return findFocusDelegate((Container) delegate);
            }
        }
        return null;
    }
	
    /** Recursively looks for the first component in container that is focus traversable. */
	
    Component findFocusDelegate(Container container) {
        synchronized (container.getTreeLock()) {
            for (int i = 0; i < container.getComponentCount(); i++) {
                Component c = container.getComponent(i);
                if (c.visible) {
                    if (c.isFocusTraversable())
                        return c;
                    if (c instanceof Container) {
                        Component focusDelegate = findFocusDelegate((Container) c);
                        if (focusDelegate != null)
                            return focusDelegate;
                    }
                }
            }
            return null;
        }
    }
	
    /**
     * Transfers the focus to the next component.
     * @see       java.awt.Component#requestFocus
     * @see       java.awt.Component#gotFocus
     * @since     JDK1.1s
     */
    public void transferFocus() {
        nextFocus();
    }
	
    /**
     * @deprecated As of JDK version 1.1,
     * replaced by transferFocus().
     */
    public void nextFocus() {
        Container parent = this.parent;
        if (parent != null) {
            parent.transferFocus(this);
        }
    }
	
    /**
     * Adds the specified popup menu to the component.
     * @param     popup the popup menu to be added to the component.
     * @see       java.awt.Component#remove(java.awt.MenuComponent)
     * @since     JDK1.1
     */
    public synchronized void add(PopupMenu popup) {
        if (popup.parent != null) {
            popup.parent.remove(popup);
        }
        if (popups == null) {
            popups = new Vector();
        }
        popups.addElement(popup);
        popup.parent = this;
        if (xwindow != null) {
            popup.addNotify();
        }
    }
	
    /**
     * Removes the specified popup menu from the component.
     * @param     popup the popup menu to be removed.
     * @see       java.awt.Component#add(java.awt.PopupMenu)
     * @since     JDK1.1
     */
    public synchronized void remove(MenuComponent popup) {
        if (popups != null) {
            int index = popups.indexOf(popup);
            if (index >= 0) {
                PopupMenu pmenu = (PopupMenu) popup;
                pmenu.removeNotify();
                pmenu.parent = null;
                popups.removeElementAt(index);
                if (popups.size() == 0) {
                    popups = null;
                }
            }
        }
    }
	
    /**
     * Returns the parameter string representing the state of this
     * component. This string is useful for debugging.
     * @return    the parameter string of this component.
     * @since     JDK1.0
     */
    protected String paramString() {
        String thisName = getName();
        String str = (thisName != null ? thisName : "") + "," + x + "," + y + "," + width + "x" + height;
        if (!valid) {
            str += ",invalid";
        }
        if (!visible) {
            str += ",hidden";
        }
        if (!enabled) {
            str += ",disabled";
        }
        return str;
    }
	
    /**
     * Returns a string representation of this component and its values.
     * @return    a string representation of this component.
     * @since     JDK1.0
     */
    public String toString() {
        return getClass().getName() + "[" + paramString() + "]";
    }
	
    /**
     * Prints a listing of this component to the standard system output
     * stream <code>System.out</code>.
     * @see       java.lang.System#out
     * @since     JDK1.0
     */
    public void list() {
        list(System.out, 0);
    }
	
    /**
     * Prints a listing of this component to the specified output
     * stream.
     * @param    out   a print stream.
     * @since    JDK1.0
     */
    public void list(PrintStream out) {
        list(out, 0);
    }
	
    /**
     * Prints out a list, starting at the specified indention, to the
     * specified print stream.
     * @param     out      a print stream.
     * @param     indent   number of spaces to indent.
     * @see       java.io.PrintStream#println(java.lang.Object)
     * @since     JDK1.0
     */
    public void list(PrintStream out, int indent) {
        for (int i = 0; i < indent; i++) {
            out.print(" ");
        }
        out.println(this);
    }
	
    /**
     * Prints a listing to the specified print writer.
     * @param  out  The print writer to print to.
     * @since JDK1.1
     */
    public void list(PrintWriter out) {
        list(out, 0);
    }
	
    /**
     * Prints out a list, starting at the specified indention, to
     * the specified print writer.
     * @param out The print writer to print to.
     * @param indent The number of spaces to indent.
     * @see       java.io.PrintStream#println(java.lang.Object)
     * @since JDK1.1
     */
    public void list(PrintWriter out, int indent) {
        for (int i = 0; i < indent; i++) {
            out.print("  ");
        }
        out.println(this);
    }
    /* Serialization support.
     */
	
    private int componentSerializedDataVersion = 1;
    private void writeObject(ObjectOutputStream s)
        throws IOException {
        s.defaultWriteObject();
        AWTEventMulticaster.save(s, componentListenerK, componentListener);
        AWTEventMulticaster.save(s, focusListenerK, focusListener);
        AWTEventMulticaster.save(s, keyListenerK, keyListener);
        AWTEventMulticaster.save(s, mouseListenerK, mouseListener);
        AWTEventMulticaster.save(s, mouseMotionListenerK, mouseMotionListener);
        s.writeObject(null);
    }
	
    private void readObject(ObjectInputStream s)
        throws ClassNotFoundException, IOException {
        s.defaultReadObject();
        Object keyOrNull;
        while (null != (keyOrNull = s.readObject())) {
            String key = ((String) keyOrNull).intern();
            if (componentListenerK == key)
                addComponentListener((ComponentListener) (s.readObject()));
            else if (focusListenerK == key)
                addFocusListener((FocusListener) (s.readObject()));
            else if (keyListenerK == key)
                addKeyListener((KeyListener) (s.readObject()));
            else if (mouseListenerK == key)
                addMouseListener((MouseListener) (s.readObject()));
            else if (mouseMotionListenerK == key)
                addMouseMotionListener((MouseMotionListener) (s.readObject()));
            else // skip value for unrecognized key
                s.readObject();
        }
        if (popups != null) {
            int npopups = popups.size();
            for (int i = 0; i < npopups; i++) {
                PopupMenu popup = (PopupMenu) popups.elementAt(i);
                popup.parent = this;
            }
        }
    }
}
