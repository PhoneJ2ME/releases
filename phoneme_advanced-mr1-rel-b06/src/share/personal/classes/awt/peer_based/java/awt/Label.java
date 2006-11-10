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

import sun.awt.peer.LabelPeer;
import sun.awt.PeerBasedToolkit;

/**
 * A <code>Label</code> object is a component for placing text in a
 * container. A label displays a single line of read-only text.
 * The text can be changed by the application, but a user cannot edit it
 * directly.
 * <p>
 * For example, the code&nbsp;.&nbsp;.&nbsp;.
 * <p>
 * <hr><blockquote><pre>
 * setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
 * add(new Label("Hi There!"));
 * add(new Label("Another Label"));
 * </pre></blockquote><hr>
 * <p>
 * produces the following label:
 * <p>
 * <img src="images-awt/Label-1.gif"
 * ALIGN=center HSPACE=10 VSPACE=7>
 *
 * @version	1.31, 07/01/98
 * @author 	Sami Shaio
 * @since       JDK1.0
 */
public class Label extends Component {
    /**
     * Indicates that the label should be left justified.
     * @since   JDK1.0
     */
    public static final int LEFT = 0;
    /**
     * Indicates that the label should be centered.
     * @since   JDK1.0
     */
    public static final int CENTER = 1;
    /**
     * Indicates that the label should be right justified.
     * @since   JDK1.0t.
     */
    public static final int RIGHT = 2;
    /**
     * The text of this label.
     */
    String text;
    /**
     * The label's alignment.  The default alignment is set
     * to be left justified.
     */
    int	   alignment = LEFT;
    private static final String base = "label";
    private static int nameCounter = 0;
    /*
     * JDK 1.1 serialVersionUID
     */
    private static final long serialVersionUID = 3094126758329070636L;
    /**
     * Constructs an empty label.
     * @since JDK1.0
     */
    public Label() {
        this("", LEFT);
    }

    /**
     * Constructs a new label with the specified string of text,
     * left justified.
     * @param text the string that the label presents.
     * @since JDK1.0
     */
    public Label(String text) {
        this(text, LEFT);
    }

    /**
     * Constructs a new label that presents the specified string of
     * text with the specified alignment.
     * <p>
     * Possible values for <code>alignment</code> are <code>Label.LEFT</code>,
     * <code>Label.RIGHT</code>, and <code>Label.CENTER</code>.
     * @param     text        the string that the label presents.
     * @param     alignment   the alignment value.
     * @since     JDK1.0
     */
    public Label(String text, int alignment) {
        this.text = text;
        setAlignment(alignment);
    }

    /**
     * Construct a name for this component.  Called by getName() when the
     * name is null.
     */
    String constructComponentName() {
        return base + nameCounter++;
    }

    /**
     * Creates the peer for this label.  The peer allows us to
     * modify the appearance of the label without changing its
     * functionality.
     */
    public void addNotify() {
        synchronized (getTreeLock()) {
            if (peer == null)
                peer = ((PeerBasedToolkit) getToolkit()).createLabel(this);
            super.addNotify();
        }
    }

    /**
     * Gets the current alignment of this label. Possible values are
     * <code>Label.LEFT</code>, <code>Label.RIGHT</code>, and
     * <code>Label.CENTER</code>.
     * @see        java.awt.Label#setAlignment
     * @since      JDK1.0
     */
    public int getAlignment() {
        return alignment;
    }

    /**
     * Sets the alignment for this label to the specified alignment.
     * Possible values are <code>Label.LEFT</code>,
     * <code>Label.RIGHT</code>, and <code>Label.CENTER</code>.
     * @param      alignment    the alignment to be set.
     * @exception  IllegalArgumentException if an improper value for
     *                          <code>alignment</code> is given.
     * @see        java.awt.Label#getAlignment
     * @since      JDK1.0
     */
    public synchronized void setAlignment(int alignment) {
        switch (alignment) {
        case LEFT:
        case CENTER:
        case RIGHT:
            this.alignment = alignment;
            LabelPeer peer = (LabelPeer) this.peer;
            if (peer != null) {
                peer.setAlignment(alignment);
            }
            return;
        }
        throw new IllegalArgumentException("improper alignment: " + alignment);
    }

    /**
     * Gets the text of this label.
     * @return     the text of this label.
     * @see        java.awt.Label#setText
     * @since      JDK1.0
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the text for this label to the specified text.
     * @param      text the text that this label presents.
     * @see        java.awt.Label#getText
     * @since      JDK1.0
     */
    public synchronized void setText(String text) {
        if (text != this.text && (this.text == null
                || !this.text.equals(text))) {
            this.text = text;
            LabelPeer peer = (LabelPeer) this.peer;
            if (peer != null) {
                peer.setText(text);
            }
        }
    }

    /**
     * Returns the parameter string representing the state of this
     * label. This string is useful for debugging.
     * @return     the parameter string of this label.
     * @since      JDK1.0
     */
    protected String paramString() {
        String str = ",align=";
        switch (alignment) {
        case LEFT:
            str += "left";
            break;

        case CENTER:
            str += "center";
            break;

        case RIGHT:
            str += "right";
            break;
        }
        return super.paramString() + str + ",text=" + text;
    }
}
