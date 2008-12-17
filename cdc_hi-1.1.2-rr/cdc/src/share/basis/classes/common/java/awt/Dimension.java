/*
 * @(#)Dimension.java	1.24 06/10/10
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
 * The <code>Dimension</code> class encapsulates the width and 
 * height of a component in a single object. The class is 
 * associated with certain properties of components. Several methods 
 * defined by the <code>Component</code> class and the 
 * <code>LayoutManager</code> interface return a 
 * <code>Dimension</code> object. 
 * <p>
 * Normally the values of <code>width</code> 
 * and <code>height</code> are non-negative integers. 
 * The constructors that allow you to create a dimension do 
 * not prevent you from setting a negative value for these properties. 
 * If the value of <code>width</code> or <code>height</code> is 
 * negative, the behavior of some methods defined by other objects is 
 * undefined. 
 * 
 * @version 	1.14, 07/01/98
 * @author 	Sami Shaio
 * @author 	Arthur van Hoff
 * @see         java.awt.Component
 * @see         java.awt.LayoutManager
 * @since       JDK1.0
 */
public class Dimension implements java.io.Serializable, Cloneable {
    /**
     * The width dimension.
     */
    public int width;
    /**
     * The height dimension.
     */
    public int height;
    /*
     * JDK 1.1 serialVersionUID 
     */
    private static final long serialVersionUID = 4723952579491349524L;
    /** 
     * Creates an instance of <code>Dimension</code> with a width 
     * of zero and a height of zero. 
     * @since   JDK1.0
     */
    public Dimension() {
        this(0, 0);
    }

    /** 
     * Creates an instance of <code>Dimension</code> whose width  
     * and height are the same as for the specified dimension. 
     * @param    d   the specified dimension for the 
     *               <code>width</code> and 
     *               <code>height</code> values.
     * @since    JDK1.0
     */
    public Dimension(Dimension d) {
        this(d.width, d.height);
    }

    /** 
     * Constructs a Dimension and initializes it to the specified width and
     * specified height.
     * @param width the specified width dimension
     * @param height the specified height dimension
     * @since JDK1.0
     */
    public Dimension(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Gets the size of this <code>Dimension</code> object.
     * This method is included for completeness, to parallel the
     * <code>getSize</code> method defined by <code>Component</code>.
     * @return   the size of this dimension, a new instance of 
     *           <code>Dimension</code> with the same width and height.
     * @see      java.awt.Dimension#setSize
     * @see      java.awt.Component#getSize
     * @since    JDK1.1
     */
    public Dimension getSize() {
        return new Dimension(width, height);
    }	

    /**
     * Set the size of this <code>Dimension</code> object to the specified size.
     * This method is included for completeness, to parallel the
     * <code>setSize</code> method defined by <code>Component</code>.
     * @param    d  the new size for this <code>Dimension</code> object.
     * @see      java.awt.Dimension#getSize
     * @see      java.awt.Component#setSize
     * @since    JDK1.1
     */
    public void setSize(Dimension d) {
        setSize(d.width, d.height);
    }	

    /**
     * Set the size of this <code>Dimension</code> object 
     * to the specified width and height.
     * This method is included for completeness, to parallel the
     * <code>setSize</code> method defined by <code>Component</code>.
     * @param    width   the new width for this <code>Dimension</code> object.
     * @param    height  the new height for this <code>Dimension</code> object.
     * @see      java.awt.Dimension#getSize
     * @see      java.awt.Component#setSize
     * @since    JDK1.1
     */
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }	

    /**
     * Checks whether two dimension objects have equal values.
     */
    public boolean equals(Object obj) {
        if (obj instanceof Dimension) {
            Dimension d = (Dimension) obj;
            return (width == d.width) && (height == d.height);
        }
        return false;
    }
    
    /**
     * Returns the hash code for this Dimension.
     *
     * @return    a hash code for this Dimension.
     */
    public int hashCode() {
        int sum = width + height;
        return sum * (sum + 1) / 2 + width;
    }    

    /**
     * Returns a string that represents this 
     * <code>Dimension</code> object's values.
     * @return     a string representation of this dimension, 
     *                  including the values of <code>width</code> 
     *                  and <code>height</code>.
     * @since      JDK1.0
     */
    public String toString() {
        return getClass().getName() + "[width=" + width + ",height=" + height + "]";
    }

    /**
     * Creates a new object of the same class as this object.
     *
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
