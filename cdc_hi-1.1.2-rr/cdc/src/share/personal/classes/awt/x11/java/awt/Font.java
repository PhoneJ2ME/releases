/*
 * @(#)Font.java	1.10 06/10/10
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

import java.security.AccessController;
import sun.security.action.GetPropertyAction;

/**
 * A class that produces font objects.
 *
 * @version 	1.6, 08/19/02
 * @author 		Nicholas Allen
 * @since       JDK1.0
 */
public class Font implements java.io.Serializable {
    /*
     * Constants to be used for styles. Can be combined to mix
     * styles.
     */

    /**
     * The plain style constant.  This style can be combined with
     * the other style constants for mixed styles.
     * @since JDK1.0
     */
    public static final int PLAIN = 0;
    /**
     * The bold style constant.  This style can be combined with the
     * other style constants for mixed styles.
     * @since JDK1.0
     */
    public static final int BOLD = 1;
    /**
     * The italicized style constant.  This style can be combined
     * with the other style constants for mixed styles.
     * @since JDK1.0
     */
    public static final int ITALIC = 2;
    /**
     * The platform specific family name of this font.
     */
    transient private String family;
    /**
     * The logical name of this font.
     * @since JDK1.0
     */
    protected String name;
    /**
     * The style of the font. This is the sum of the
     * constants <code>PLAIN</code>, <code>BOLD</code>,
     * or <code>ITALIC</code>.
     */
    protected int style;
    /**
     * The point size of this font.
     * @since JDK1.0
     */
    protected int size;
    /*
     * JDK 1.1 serialVersionUID
     */
    private static final long serialVersionUID = -4206021311591459213L;
    private transient X11FontMetrics metrics;
    /**
     * Creates a new font with the specified name, style and point size.
     * @param name the font name
     * @param style the constant style used
     * @param size the point size of the font
     * @see Toolkit#getFontList
     * @since JDK1.0
     */
    public Font(String name, int style, int size) {
        this.name = name;
        this.style = style;
        this.size = size;
    }
	
    X11FontMetrics getX11FontMetrics() {
        if (metrics == null)
            metrics = X11FontMetrics.getFontMetrics(this);
        return metrics;
    }
	
    /**
     * Gets the platform specific family name of the font. Use the
     * <code>getName</code> method to get the logical name of the font.
     * @return    a string, the platform specific family name.
     * @see       java.awt.Font#getName
     * @since     JDK1.0
     */
    public String getFamily() {
        return family;
    }

    /**
     * Gets the logical name of the font.
     * @return    a string, the logical name of the font.
     * @see #getFamily
     * @since     JDK1.0
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the style of the font.
     * @return the style of this font.
     * @see #isPlain
     * @see #isBold
     * @see #isItalic
     * @since JDK1.0
     */
    public int getStyle() {
        return style;
    }

    /**
     * Gets the point size of the font.
     * @return the point size of this font.
     * @since JDK1.0
     */
    public int getSize() {
        return size;
    }

    /**
     * Indicates whether the font's style is plain.
     * @return     <code>true</code> if the font is neither
     *                bold nor italic; <code>false</code> otherwise.
     * @see        java.awt.Font#getStyle
     * @since      JDK1.0
     */
    public boolean isPlain() {
        return style == 0;
    }

    /**
     * Indicates whether the font's style is bold.
     * @return    <code>true</code> if the font is bold;
     *            <code>false</code> otherwise.
     * @see       java.awt.Font#getStyle
     * @since     JDK1.0
     */
    public boolean isBold() {
        return (style & BOLD) != 0;
    }

    /**
     * Indicates whether the font's style is italic.
     * @return    <code>true</code> if the font is italic;
     *            <code>false</code> otherwise.
     * @see       java.awt.Font#getStyle
     * @since     JDK1.0
     */
    public boolean isItalic() {
        return (style & ITALIC) != 0;
    }

    /**
     * Gets a font from the system properties list.
     * @param nm the property name
     * @see       java.awt.Font#getFont(java.lang.String, java.awt.Font)
     * @since     JDK1.0
     */
    public static Font getFont(String nm) {
        return getFont(nm, null);
    }

    /**
     * Gets the specified font using the name passed in.
     * @param str the name
     * @since JDK1.1
     */
    public static Font decode(String str) {
        String fontName = str;
        int fontSize = 12;
        int fontStyle = Font.PLAIN;
        int i = str.indexOf('-');
        if (i >= 0) {
            fontName = str.substring(0, i);
            str = str.substring(i + 1);
            if ((i = str.indexOf('-')) >= 0) {
                if (str.startsWith("bold-")) {
                    fontStyle = Font.BOLD;
                } else if (str.startsWith("italic-")) {
                    fontStyle = Font.ITALIC;
                } else if (str.startsWith("bolditalic-")) {
                    fontStyle = Font.BOLD | Font.ITALIC;
                }
                str = str.substring(i + 1);
            }
            try {
                fontSize = Integer.valueOf(str).intValue();
            } catch (NumberFormatException e) {}
        }
        return new Font(fontName, fontStyle, fontSize);
    }

    /**
     * Gets the specified font from the system properties list.
     * The first argument is treated as the name of a system property to
     * be obtained as if by the method <code>System.getProperty</code>.
     * The string value of this property is then interpreted as a font.
     * <p>
     * The property value should be one of the following forms:
     * <ul>
     * <li><em>fontname-style-pointsize</em>
     * <li><em>fontname-pointsize</em>
     * <li><em>fontname-style</em>
     * <li><em>fontname</em>
     * </ul>
     * where <i>style</i> is one of the three strings
     * <code>"BOLD"</code>, <code>"BOLDITALIC"</code>, or
     * <code>"ITALIC"</code>, and point size is a decimal
     * representation of the point size.
     * <p>
     * The default style is <code>PLAIN</code>. The default point size
     * is 12.
     * <p>
     * If the specified property is not found, the <code>font</code>
     * argument is returned instead.
     * @param nm the property name
     * @param font a default font to return if property <code>nm</code>
     *             is not defined
     * @return    the <code>Font</code> value of the property.
     * @since     JDK1.0
     */
    public static Font getFont(String nm, Font font) {
        String str = System.getProperty(nm);
        if (str == null) {
            return font;
        }
        return Font.decode(str);
    }

    /**
     * Returns a hashcode for this font.
     * @return     a hashcode value for this font.
     * @since      JDK1.0
     */
    public int hashCode() {
        return name.hashCode() ^ style ^ size;
    }
    
    /**
     * Compares this object to the specifed object.
     * The result is <code>true</code> if and only if the argument is not
     * <code>null</code> and is a <code>Font</code> object with the same
     * name, style, and point size as this font.
     * @param     obj   the object to compare this font with.
     * @return    <code>true</code> if the objects are equal;
     *            <code>false</code> otherwise.
     * @since     JDK1.0
     */
    public boolean equals(Object obj) {
        if (obj instanceof Font) {
            Font font = (Font) obj;
            return (size == font.size) && (style == font.style) && name.equals(font.name);
        }
        return false;
    }

    /**
     * Converts this object to a String representation.
     * @return     a string representation of this object
     * @since      JDK1.0
     */
    public String toString() {
        String	strStyle;
        if (isBold()) {
            strStyle = isItalic() ? "bolditalic" : "bold";
        } else {
            strStyle = isItalic() ? "italic" : "plain";
        }
        return getClass().getName() + "[family=" + family + ",name=" + name + ",style=" +
            strStyle + ",size=" + size + "]";
    }
    /* Serialization support.  A readObject method is neccessary because
     * the constructor creates the fonts peer, and we can't serialize the
     * peer.  Similarly the computed font "family" may be different
     * at readObject time than at writeObject time.  An integer version is
     * written so that future versions of this class will be able to recognize
     * serialized output from this one.
     */

    private int fontSerializedDataVersion = 1;
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.lang.ClassNotFoundException,
            java.io.IOException {
        s.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream s)
        throws java.lang.ClassNotFoundException,
            java.io.IOException {
        s.defaultReadObject();
        //      initializeFont();
    }
}
