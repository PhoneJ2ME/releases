/*
 * @(#)Rectangle.java	1.49 06/10/10
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
 * A <code>Rectangle</code> specifies an area in a coordinate space that is
 * enclosed by the <code>Rectangle</code> object's top-left point
 * (<i>x</i>,&nbsp;<i>y</i>)
 * in the coordinate space, its width, and its height.
 * <p>
 * A <code>Rectangle</code> object's <code>width</code> and
 * <code>height</code> are <code>public</code> fields. The constructors
 * that create a <code>Rectangle</code>, and the methods that can modify
 * one, do not prevent setting a negative value for width or height.
 * <p>
 * A <code>Rectangle</code> whose width or height is negative is considered
 * empty. If the <code>Rectangle</code> is empty, then the
 * <code>isEmpty</code> method returns <code>true</code>. No point can be
 * contained by or inside an empty <code>Rectangle</code>.  The
 * values of <code>width</code> and <code>height</code>, however, are still
 * valid.  An empty <code>Rectangle</code> still has a location in the
 * coordinate space, and methods that change its size or location remain
 * valid. The behavior of methods that operate on more than one
 * <code>Rectangle</code> is undefined if any of the participating
 * <code>Rectangle</code> objects has a negative
 * <code>width</code> or <code>height</code>. These methods include
 * <code>intersects</code>, <code>intersection</code>, and
 * <code>union</code>.
 *
 * @version 	1.52, 02/02/00
 * @author 	Sami Shaio
 * @since       JDK1.0
 */
public class Rectangle implements Shape, java.io.Serializable, Cloneable {
    /**
     * The <i>x</i> coordinate of the <code>Rectangle</code>.
     *
     * @serial
     * @see #setLocation(int, int)
     * @see #getLocation()
     */
    public int x;
    /**
     * The <i>y</i> coordinate of the <code>Rectangle</code>.
     *
     * @serial
     * @see #setLocation(int, int)
     * @see #getLocation()
     */
    public int y;
    /**
     * The width of the <code>Rectangle</code>.
     * @serial
     * @see #setSize(int, int)
     * @see #getSize()
     * @since     JDK1.0.
     */
    public int width;
    /**
     * The height of the <code>Rectangle</code>.
     *
     * @serial
     * @see #setSize(int, int)
     * @see #getSize()
     */
    public int height;
    /*
     * JDK 1.1 serialVersionUID
     */
    private static final long serialVersionUID = -4345857070255674764L;
    /**
     * Constructs a new <code>Rectangle</code> whose top-left corner
     * is at (0,&nbsp;0) in the coordinate space, and whose width and
     * height are both zero.
     */
    public Rectangle() {
        this(0, 0, 0, 0);
    }

    /**
     * Constructs a new <code>Rectangle</code>, initialized to match
     * the values of the specificed <code>Rectangle</code>.
     * @param r  the <code>Rectangle</code> from which to copy initial values
     *           to a newly constructed <code>Rectangle</code>
     * @since JDK1.1
     */
    public Rectangle(Rectangle r) {
        this(r.x, r.y, r.width, r.height);
    }

    /**
     * Constructs a new <code>Rectangle</code> whose top-left corner is
     * specified as
     * (<code>x</code>,&nbsp;<code>y</code>) and whose width and height
     * are specified by the arguments of the same name.
     * @param     x,&nbsp;y the specified coordinates
     * @param     width    the width of the <code>Rectangle</code>
     * @param     height   the height of the <code>Rectangle</code>
     */
    public Rectangle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Constructs a new <code>Rectangle</code> whose top-left corner
     * is at (0,&nbsp;0) in the coordinate space, and whose width and
     * height are specified by the arguments of the same name.
     * @param width the width of the <code>Rectangle</code>
     * @param height the height of the <code>Rectangle</code>
     */
    public Rectangle(int width, int height) {
        this(0, 0, width, height);
    }

    /**
     * Constructs a new <code>Rectangle</code> whose top-left corner is
     * specified by the {@link Point} argument, and
     * whose width and height are specified by the
     * {@link Dimension} argument.
     * @param p a <code>Point</code> that is the top-left corner of
     * the <code>Rectangle</code>
     * @param d a <code>Dimension</code>, representing the
     * width and height of the <code>Rectangle</code>
     */
    public Rectangle(Point p, Dimension d) {
        this(p.x, p.y, d.width, d.height);
    }
    
    /**
     * Constructs a new <code>Rectangle</code> whose top-left corner is the
     * specified <code>Point</code>, and whose width and height are both zero.
     * @param p a <code>Point</code> that is the top left corner
     * of the <code>Rectangle</code>
     */
    public Rectangle(Point p) {
        this(p.x, p.y, 0, 0);
    }
    
    /**
     * Constructs a new <code>Rectangle</code> whose top left corner is
     * (0,&nbsp;0) and whose width and height are specified
     * by the <code>Dimension</code> argument.
     * @param d a <code>Dimension</code>, specifying width and height
     */
    public Rectangle(Dimension d) {
        this(0, 0, d.width, d.height);
    }

    /**
     * Gets the bounding <code>Rectangle</code> of this <code>Rectangle</code>.
     * <p>
     * This method is included for completeness, to parallel the
     * <code>getBounds</code> method of
     * {@link Component}.
     * @return    a new <code>Rectangle</code>, equal to the
     * bounding <code>Rectangle</code> for this <code>Rectangle</code>.
     * @see       java.awt.Component#getBounds
     * @since     JDK1.1
     */
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    /**
     * Sets the bounding <code>Rectangle</code> of this <code>Rectangle</code>
     * to match the specified <code>Rectangle</code>.
     * <p>
     * This method is included for completeness, to parallel the
     * <code>setBounds</code> method of <code>Component</code>.
     * @param r the specified <code>Rectangle</code>
     * @see       java.awt.Component#setBounds(java.awt.Rectangle)
     * @since     JDK1.1
     */
    public void setBounds(Rectangle r) {
        setBounds(r.x, r.y, r.width, r.height);
    }
    
    private double getX() {
        return (double) x;
    }

    private double getY() {
        return (double) y;
    }

    private double getWidth() {
        return (double) width;
    }

    private double getHeight() {
        return (double) height;
    }    
    
    boolean contains(double x, double y) {
        double x0 = getX();
        double y0 = getY();
        return (x >= x0 &&
                y >= y0 &&
                x < x0 + getWidth() &&
                y < y0 + getHeight());
    }    

    /**
     * Sets the bounding <code>Rectangle</code> of this
     * <code>Rectangle</code> to the specified
     * <code>x</code>, <code>y</code>, <code>width</code>,
     * and <code>height</code>.
     * <p>
     * This method is included for completeness, to parallel the
     * <code>setBounds</code> method of <code>Component</code>.
     * @param x,&nbsp;y the new coordinates for the top-left
     *                    corner of this <code>Rectangle</code>
     * @param width the new width for this <code>Rectangle</code>
     * @param height the new height for this <code>Rectangle</code>
     * @see       java.awt.Component#setBounds(int, int, int, int)
     * @since     JDK1.1
     */
    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Returns the location of this <code>Rectangle</code>.
     * <p>
     * This method is included for completeness, to parallel the
     * <code>getLocation</code> method of <code>Component</code>.
     * @return the <code>Point</code> that is the top-left corner of
     *			this <code>Rectangle</code>.
     * @see       java.awt.Component#getLocation
     * @since     JDK1.1
     */
    public Point getLocation() {
        return new Point(x, y);
    }

    /**
     * Moves this <code>Rectangle</code> to the specified location.
     * <p>
     * This method is included for completeness, to parallel the
     * <code>setLocation</code> method of <code>Component</code>.
     * @param p the <code>Point</code> specifying the new location
     *                for this <code>Rectangle</code>
     * @see       java.awt.Component#setLocation(java.awt.Point)
     * @since     JDK1.1
     */
    public void setLocation(Point p) {
        setLocation(p.x, p.y);
    }

    /**
     * Moves this <code>Rectangle</code> to the specified location.
     * <p>
     * This method is included for completeness, to parallel the
     * <code>setLocation</code> method of <code>Component</code>.
     * @param x,&nbsp;y the coordinates of the new location
     * @see       java.awt.Component#setLocation(int, int)
     * @since     JDK1.1
     */
    public void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Translates this <code>Rectangle</code> the indicated distance,
     * to the right along the x coordinate axis, and
     * downward along the y coordinate axis.
     * @param dx the distance to move this <code>Rectangle</code>
     *                 along the x axis
     * @param dy the distance to move this <code>Rectangle</code>
     *                 along the y axis
     * @see       java.awt.Rectangle#setLocation(int, int)
     * @see       java.awt.Rectangle#setLocation(java.awt.Point)
     */
    public void translate(int x, int y) {
        this.x += x;
        this.y += y;
    }

    /**
     * Gets the size of this <code>Rectangle</code>, represented by
     * the returned <code>Dimension</code>.
     * <p>
     * This method is included for completeness, to parallel the
     * <code>getSize</code> method of <code>Component</code>.
     * @return a <code>Dimension</code>, representing the size of
     *            this <code>Rectangle</code>.
     * @see       java.awt.Component#getSize
     * @since     JDK1.1
     */
    public Dimension getSize() {
        return new Dimension(width, height);
    }

    /**
     * Sets the size of this <code>Rectangle</code> to match the
     * specified <code>Dimension</code>.
     * <p>
     * This method is included for completeness, to parallel the
     * <code>setSize</code> method of <code>Component</code>.
     * @param d the new size for the <code>Dimension</code> object
     * @see       java.awt.Component#setSize(java.awt.Dimension)
     * @since     JDK1.1
     */
    public void setSize(Dimension d) {
        setSize(d.width, d.height);
    }

    /**
     * Sets the size of this <code>Rectangle</code> to the specified
     * width and height.
     * <p>
     * This method is included for completeness, to parallel the
     * <code>setSize</code> method of <code>Component</code>.
     * @param width the new width for this <code>Rectangle</code>
     * @param height the new height for this <code>Rectangle</code>
     * @see       java.awt.Component#setSize(int, int)
     * @since     JDK1.1
     */
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Checks whether or not this <code>Rectangle</code> contains the
     * specified <code>Point</code>.
     * @param p the <code>Point</code> to test
     * @return    <code>true</code> if the <code>Point</code>
     *            (<i>x</i>,&nbsp;<i>y</i>) is inside this
     * 		  <code>Rectangle</code>;
     *            <code>false</code> otherwise.
     * @since     JDK1.1
     */
    public boolean contains(Point p) {
        return contains(p.x, p.y);
    }

    /**
     * Checks whether or not this <code>Rectangle</code> contains the
     * point at the specified location
     * (<i>x</i>,&nbsp;<i>y</i>).
     * @param  x,&nbsp;y  the specified coordinates
     * @return    <code>true</code> if the point
     *            (<i>x</i>,&nbsp;<i>y</i>) is inside this
     *		  <code>Rectangle</code>;
     *            <code>false</code> otherwise.
     * @since     JDK1.1
     */
    public boolean contains(int x, int y) {
        return ((this.width > 0) && (this.height > 0) &&
                (x >= this.x) && (y >= this.y) &&
                ((x - this.x) < width) && ((y - this.y) < height));
    }

    /**
     * Checks whether or not this <code>Rectangle</code> entirely contains
     * the specified <code>Rectangle</code>.
     * @param     r   the specified <code>Rectangle</code>
     * @return    <code>true</code> if the <code>Rectangle</code>
     *            is contained entirely inside this <code>Rectangle</code>;
     *            <code>false</code> otherwise.
     * @since     JDK1.1
     */
    public boolean contains(Rectangle r) {
        return contains(r.x, r.y, r.width, r.height);
    }

    /**
     * Checks whether this <code>Rectangle</code> entirely contains
     * the <code>Rectangle</code>
     * at the specified location (<i>X</i>,&nbsp;<i>Y</i>) with the
     * specified dimensions (<i>W</i>,&nbsp;<i>H</i>).
     * @param     x,&nbsp;y  the specified coordinates
     * @param     W   the width of the <code>Rectangle</code>
     * @param     H   the height of the <code>Rectangle</code>
     * @return    <code>true</code> if the <code>Rectangle</code> specified by
     *            (<i>X</i>,&nbsp;<i>Y</i>,&nbsp;<i>W</i>,&nbsp;<i>H</i>)
     *            is entirely enclosed inside this <code>Rectangle</code>;
     *            <code>false</code> otherwise.
     * @since     JDK1.1
     */
    public boolean contains(int X, int Y, int W, int H) {
        int width = this.width;
        int height = this.height;
        if (width <= 0 || height <= 0 || W <= 0 || H <= 0) {
            return false;
        }
        int x = this.x;
        int y = this.y;
        return (X >= x &&
                Y >= y &&
                X + W <= x + width &&
                Y + H <= y + height);
    }

    /**
     * Determines whether or not this <code>Rectangle</code> and the specified
     * <code>Rectangle</code> intersect. Two rectangles intersect if
     * their intersection is nonempty.
     * @param r the specified <code>Rectangle</code>
     * @return    <code>true</code> if the specified <code>Rectangle</code>
     *            and this <code>Rectangle</code> insersect;
     *            <code>false</code> otherwise.
     */
    public boolean intersects(Rectangle r) {
        int tw = this.width;
        int th = this.height;
        int rw = r.width;
        int rh = r.height;
        if (rw <= 0 || rh <= 0 || tw <= 0 || th <= 0) {
            return false;
        }
        int tx = this.x;
        int ty = this.y;
        int rx = r.x;
        int ry = r.y;
        rw += rx;
        rh += ry;
        tw += tx;
        th += ty;
        //      overflow || intersect
        return ((rw < rx || rw > tx) &&
                (rh < ry || rh > ty) &&
                (tw < tx || tw > rx) &&
                (th < ty || th > ry));
    }

    /**
     * Computes the intersection of this <code>Rectangle</code> with the
     * specified <code>Rectangle</code>. Returns a new <code>Rectangle</code>
     * that represents the intersection of the two rectangles.
     * @param     r   the specified <code>Rectangle</code>
     * @return    the largest <code>Rectangle</code> contained in both the
     *            specified <code>Rectangle</code> and in
     *		  this<code>Rectangle</code>.
     */
    public Rectangle intersection(Rectangle r) {
        if (intersects(r)) {
            int x1 = Math.max(x, r.x);
            int x2 = Math.min(x + width, r.x + r.width);
            int y1 = Math.max(y, r.y);
            int y2 = Math.min(y + height, r.y + r.height);
            if (((x2 - x1) < 0) || ((y2 - y1) < 0))
                // Width or height is negative. No intersection.
                return new Rectangle(0, 0, 0, 0);
            else
                return new Rectangle(x1, y1, x2 - x1, y2 - y1);
        } else {
            return new Rectangle(0, 0, 0, 0);
        }
    }

    /**
     * Computes the union of this <code>Rectangle</code> with the
     * specified <code>Rectangle</code>. Returns a new
     * <code>Rectangle</code> that
     * represents the union of the two rectangles
     * @param r the specified <code>Rectangle</code>
     * @return    the smallest <code>Rectangle</code> containing both
     *		  the specified <code>Rectangle</code> and this
     *		  <code>Rectangle</code>.
     */
    public Rectangle union(Rectangle r) {
        int x1 = Math.min(x, r.x);
        int x2 = Math.max(x + width, r.x + r.width);
        int y1 = Math.min(y, r.y);
        int y2 = Math.max(y + height, r.y + r.height);
        return new Rectangle(x1, y1, x2 - x1, y2 - y1);
    }

    /**
     * Adds a point, specified by the integer arguments <code>newx</code>
     * and <code>newy</code>, to this <code>Rectangle</code>. The
     * resulting <code>Rectangle</code> is
     * the smallest <code>Rectangle</code> that contains both the
     * original <code>Rectangle</code> and the specified point.
     * <p>
     * After adding a point, a call to <code>contains</code> with the
     * added point as an argument does not necessarily return
     * <code>true</code>. The <code>contains</code> method does not
     * return <code>true</code> for points on the right or bottom
     * edges of a <code>Rectangle</code>. Therefore, if the added point
     * falls on the right or bottom edge of the enlarged
     * <code>Rectangle</code>, <code>contains</code> returns
     * <code>false</code> for that point.
     * @param newx,&nbsp;newy   the coordinates of the new point
     */
    public void add(int newx, int newy) {
        int x1 = Math.min(x, newx);
        int x2 = Math.max(x + width, newx);
        int y1 = Math.min(y, newy);
        int y2 = Math.max(y + height, newy);
        x = x1;
        y = y1;
        width = x2 - x1;
        height = y2 - y1;
    }

    /**
     * Adds the specified <code>Point</code> to this
     * <code>Rectangle</code>. The resulting <code>Rectangle</code>
     * is the smallest <code>Rectangle</code> that contains both the
     * original <code>Rectangle</code> and the specified
     * <code>Point</code>.
     * <p>
     * After adding a <code>Point</code>, a call to <code>contains</code>
     * with the added <code>Point</code> as an argument does not
     * necessarily return <code>true</code>. The <code>contains</code>
     * method does not return <code>true</code> for points on the right
     * or bottom edges of a <code>Rectangle</code>. Therefore if the added
     * <code>Point</code> falls on the right or bottom edge of the
     * enlarged <code>Rectangle</code>, <code>contains</code> returns
     * <code>false</code> for that <code>Point</code>.
     * @param pt the new <code>Point</code> to add to this
     *           <code>Rectangle</code>
     */
    public void add(Point pt) {
        add(pt.x, pt.y);
    }

    /**
     * Adds a <code>Rectangle</code> to this <code>Rectangle</code>.
     * The resulting <code>Rectangle</code> is the union of the two
     * rectangles.
     * @param  r the specified <code>Rectangle</code>
     */
    public void add(Rectangle r) {
        int x1 = Math.min(x, r.x);
        int x2 = Math.max(x + width, r.x + r.width);
        int y1 = Math.min(y, r.y);
        int y2 = Math.max(y + height, r.y + r.height);
        x = x1;
        y = y1;
        width = x2 - x1;
        height = y2 - y1;
    }

    /**
     * Resizes the <code>Rectangle</code> both horizontally and vertically.
     * <p>
     * This method modifies the <code>Rectangle</code> so that it is
     * <code>h</code> units larger on both the left and right side,
     * and <code>v</code> units larger at both the top and bottom.
     * <p>
     * The new <code>Rectangle</code> has (<code>x&nbsp;-&nbsp;h</code>,
     * <code>y&nbsp;-&nbsp;v</code>) as its top-left corner, a
     * width of
     * <code>width</code>&nbsp;<code>+</code>&nbsp;<code>2h</code>,
     * and a height of
     * <code>height</code>&nbsp;<code>+</code>&nbsp;<code>2v</code>.
     * <p>
     * If negative values are supplied for <code>h</code> and
     * <code>v</code>, the size of the <code>Rectangle</code>
     * decreases accordingly.
     * The <code>grow</code> method does not check whether the resulting
     * values of <code>width</code> and <code>height</code> are
     * non-negative.
     * @param h the horizontal expansion
     * @param v the vertical expansion
     */
    public void grow(int h, int v) {
        x -= h;
        y -= v;
        width += h * 2;
        height += v * 2;
    }

    /**
     * Determines whether or not this <code>Rectangle</code> is empty. A
     * <code>Rectangle</code> is empty if its width or its height is less
     * than or equal to zero.
     * @return     <code>true</code> if this <code>Rectangle</code> is empty;
     *             <code>false</code> otherwise.
     */
    public boolean isEmpty() {
        return (width <= 0) || (height <= 0);
    }

    /**
     * Returns the hashcode for this <code>Rectangle</code>.
     * @return the hashcode for this <code>Rectangle</code>.
     */
    public int hashCode() {
        long bits = java.lang.Double.doubleToLongBits(getX());
        bits += java.lang.Double.doubleToLongBits(getY()) * 37;
        bits += java.lang.Double.doubleToLongBits(getWidth()) * 43;
        bits += java.lang.Double.doubleToLongBits(getHeight()) * 47;
        return (((int) bits) ^ ((int) (bits >> 32)));
    }

    /**
     * Checks whether two rectangles are equal.
     * <p>
     * The result is <code>true</code> if and only if the argument is not
     * <code>null</code> and is a <code>Rectangle</code> object that has the
     * same top-left corner, width, and height as this <code>Rectangle</code>.
     * @param obj the <code>Object</code> to compare with
     *                this <code>Rectangle</code>
     * @return    <code>true</code> if the objects are equal;
     *            <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj instanceof Rectangle) {
            Rectangle r = (Rectangle) obj;
            return ((x == r.x) &&
                    (y == r.y) &&
                    (width == r.width) &&
                    (height == r.height));
        }
        return super.equals(obj);
    }

    /**
     * Returns a <code>String</code> representing this
     * <code>Rectangle</code> and its values.
     * @return a <code>String</code> representing this
     *               <code>Rectangle</code> object's coordinate and size values.
     */
    public String toString() {
        return getClass().getName() + "[x=" + x + ",y=" + y + ",width=" + width + ",height=" + height + "]";
    }

    /**
     * Creates a new object of the same class and with the same
     * contents as this object.
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
