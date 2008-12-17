/*
 * @(#)Point.java	1.23 06/10/10
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

/**
 * The <code>Point</code> class represents a location in a
 * two-dimensional (<i>x</i>,&nbsp;<i>y</i>) coordinate space.
 *
 * @version 	1.18, 08/19/02
 * @author 	Sami Shaio
 * @since       JDK1.0
 */
public class Point implements java.io.Serializable, Cloneable {
    /**
     * The <i>x</i> coordinate. 
     * @since   JDK1.0
     */
    public int x;
    /**
     * The <i>y</i> coordinate. 
     * @since   JDK1.0
     */
    public int y;
    /*
     * JDK 1.1 serialVersionUID 
     */
    private static final long serialVersionUID = -5276940640259749850L;
    /**
     * Constructs and initializes a point at the origin 
     * (0,&nbsp;0) of the coordinate space. 
     * @param       x   the <i>x</i> coordinate.
     * @param       y   the <i>y</i> coordinate.
     * @since       JDK1.1
     */
    public Point() {
        this(0, 0);
    }

    /**
     * Constructs and initializes a point with the same location as
     * the specified <code>Point</code> object.
     * @param       p a point.
     * @since       JDK1.1
     */
    public Point(Point p) {
        this(p.x, p.y);
    }

    /**
     * Constructs and initializes a point at the specified 
     * (<i>x</i>,&nbsp;<i>y</i>) location in the coordinate space. 
     * @param       x   the <i>x</i> coordinate.
     * @param       y   the <i>y</i> coordinate.
     * @since       JDK1.0
     */
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Returns the location of this point.
     * This method is included for completeness, to parallel the
     * <code>getLocation</code> method of <code>Component</code>.
     * @return      a copy of this point, at the same location.
     * @see         java.awt.Component#getLocation
     * @see         java.awt.Point#setLocation(java.awt.Point)
     * @see         java.awt.Point#setLocation(int, int)
     * @since       JDK1.1
     */
    public Point getLocation() {
        return new Point(x, y);
    }	

    /**
     * Sets the location of the point to the specificed location.
     * This method is included for completeness, to parallel the
     * <code>setLocation</code> method of <code>Component</code>.
     * @param       p  a point, the new location for this point.
     * @see         java.awt.Component#setLocation(java.awt.Point)
     * @see         java.awt.Point#getLocation
     * @since       JDK1.1
     */
    public void setLocation(Point p) {
        setLocation(p.x, p.y);
    }	

    /**
     * Changes the point to have the specificed location.
     * <p>
     * This method is included for completeness, to parallel the
     * <code>setLocation</code> method of <code>Component</code>.
     * Its behavior is identical with <code>move(int,&nbsp;int)</code>.
     * @param       x  the <i>x</i> coordinate of the new location.
     * @param       y  the <i>y</i> coordinate of the new location.
     * @see         java.awt.Component#setLocation(int, int)
     * @see         java.awt.Point#getLocation
     * @see         java.awt.Point#move(int, int)
     * @since       JDK1.1
     */
    public void setLocation(int x, int y) {
        move(x, y);
    }	

    /**
     * Moves this point to the specificed location in the 
     * (<i>x</i>,&nbsp;<i>y</i>) coordinate plane. This method
     * is identical with <code>setLocation(int,&nbsp;int)</code>.
     * @param       x  the <i>x</i> coordinate of the new location.
     * @param       y  the <i>y</i> coordinate of the new location.
     * @see         java.awt.Component#setLocation(int, int)
     * @since       JDK1.0
     */
    public void move(int x, int y) {
        this.x = x;
        this.y = y;
    }	

    /**
     * Translates this point, at location (<i>x</i>,&nbsp;<i>y</i>), 
     * by <code>dx</code> along the <i>x</i> axis and <code>dy</code> 
     * along the <i>y</i> axis so that it now represents the point 
     * (<code>x</code>&nbsp;<code>+</code>&nbsp;<code>dx</code>, 
     * <code>y</code>&nbsp;<code>+</code>&nbsp;<code>dy</code>). 
     * @param       dx   the distance to move this point 
     *                            along the <i>x</i> axis.
     * @param       dy    the distance to move this point 
     *                            along the <i>y</i> axis.
     * @since       JDK1.0
     */
    public void translate(int x, int y) {
        this.x += x;
        this.y += y;
    }	

    /**
     * Returns the hashcode for this point.
     * @return      a hash code for this point.
     * @since       JDK1.0
     */
    public int hashCode() {
        return x ^ (y * 31);
    }

    /**
     * Determines whether two points are equal. Two instances of
     * <code>Point</code> are equal if the values of their 
     * <code>x</code> and <code>y</code> member fields, representing
     * their position in the coordinate space, are the same.
     * @param      obj   an object to be compared with this point.
     * @return     <code>true</code> if the object to be compared is
     *                     an instance of <code>Point</code> and has
     *                     the same values; <code>false</code> otherwise.
     * @since      JDK1.0
     */
    public boolean equals(Object obj) {
        if (obj instanceof Point) {
            Point pt = (Point) obj;
            return (x == pt.x) && (y == pt.y);
        }
        return false;
    }

    /**
     * Returns a representation of this point and its location
     * in the (<i>x</i>,&nbsp;<i>y</i>) coordinate space as a string.
     * @return    a string representation of this point, 
     *                 including the values of its member fields.
     * @since     JDK1.0
     */
    public String toString() {
        return getClass().getName() + "[x=" + x + ",y=" + y + "]";
    }

    /**
     * Creates a new object of the same class and with the
     * same contents as this object.
     * @return     a clone of this instance.
     * @exception  OutOfMemoryError            if there is not enough memory.
     * @see        java.lang.Cloneable
     * @since      1.2
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError();
        }
    }

}
