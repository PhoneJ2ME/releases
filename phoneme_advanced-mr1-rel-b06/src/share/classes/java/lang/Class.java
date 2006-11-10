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

package java.lang;

import java.lang.reflect.Member;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;
import java.io.InputStream;
import java.io.ObjectStreamClass;
import java.io.ObjectStreamField;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import sun.security.util.SecurityConstants;
import sun.misc.CVM;
import java.util.WeakHashMap;
import java.util.Map;
import java.util.Collections;

/**
 * Instances of the class <code>Class</code> represent classes and interfaces
 * in a running Java application.  Every array also belongs to a class that is
 * reflected as a <code>Class</code> object that is shared by all arrays with
 * the same element type and number of dimensions.  The primitive Java types
 * (<code>boolean</code>, <code>byte</code>, <code>char</code>,
 * <code>short</code>, <code>int</code>, <code>long</code>,
 * <code>float</code>, and <code>double</code>), and the keyword
 * <code>void</code> are also represented as <code>Class</code> objects.
 *
 * <p> <code>Class</code> has no public constructor. Instead <code>Class</code>
 * objects are constructed automatically by the Java Virtual Machine as classes
 * are loaded and by calls to the <code>defineClass</code> method in the class
 * loader.
 *
 * <p> The following example uses a <code>Class</code> object to print the
 * class name of an object:
 *
 * <p> <blockquote><pre>
 *     void printClassName(Object obj) {
 *         System.out.println("The class of " + obj +
 *                            " is " + obj.getClass().getName());
 *     }
 * </pre></blockquote>
 * 
 * <p> It is also possible to get the <code>Class</code> object for a named
 * type (or for void) using a class literal 
 * (JLS Section <A HREF="http://java.sun.com/docs/books/jls/second_edition/html/expressions.doc.html#251530">15.8.2</A>). 
 * For example:
 *
 * <p> <blockquote><pre>
 *     System.out.println("The name of class Foo is: "+Foo.class.getName());
 * </pre></blockquote>
 *
 * @author  unascribed
 * @version 1.135, 05/25/01
 * @see     java.lang.ClassLoader#defineClass(byte[], int, int)
 * @since   JDK1.0
 */
public final
class Class implements java.io.Serializable {
    private int            classBlockPointer; /* Private pointer to cb */
    private ClassLoader    loader; /* For GC purposes only! */

    /* No need to do this if ROMized
    private static native void registerNatives();
    static {
        registerNatives();
    }
    */

    /*
     * Constructor. Only the Java Virtual Machine creates Class
     * objects.
     */
    private Class() {}


    /**
     * Converts the object to a string. The string representation is the
     * string "class" or "interface", followed by a space, and then by the
     * fully qualified name of the class in the format returned by
     * <code>getName</code>.  If this <code>Class</code> object represents a
     * primitive type, this method returns the name of the primitive type.  If
     * this <code>Class</code> object represents void this method returns
     * "void".
     *
     * @return a string representation of this class object.
     */
    public String toString() {
        return (isInterface() ? "interface " : (isPrimitive() ? "" : "class "))
            + getName();
    }


    /**
     * Returns the <code>Class</code> object associated with the class or
     * interface with the given string name.  Invoking this method is
     * equivalent to:
     *
     * <blockquote><pre>
     *  Class.forName(className, true, currentLoader)
     * </pre></blockquote>
     *
     * where <code>currentLoader</code> denotes the defining class loader of
     * the current class.
     *
     * <p> For example, the following code fragment returns the
     * runtime <code>Class</code> descriptor for the class named
     * <code>java.lang.Thread</code>:
     *
     * <blockquote><pre>
     *   Class&nbsp;t&nbsp;= Class.forName("java.lang.Thread")
     * </pre></blockquote>
     * <p>
     * A call to <tt>forName("X")</tt> causes the class named 
     * <tt>X</tt> to be initialized.
     *
     * @param      className   the fully qualified name of the desired class.
     * @return     the <code>Class</code> object for the class with the
     *             specified name.
     * @exception LinkageError if the linkage fails
     * @exception ExceptionInInitializerError if the initialization provoked
     *            by this method fails
     * @exception ClassNotFoundException if the class cannot be located
     */
    public static Class forName(String className) 
                throws ClassNotFoundException {
        return forName0(className, true, ClassLoader.getCallerClassLoader());
    }


    /**
     * Returns the <code>Class</code> object associated with the class or
     * interface with the given string name, using the given class loader.
     * Given the fully qualified name for a class or interface (in the same
     * format returned by <code>getName</code>) this method attempts to
     * locate, load, and link the class or interface.  The specified class
     * loader is used to load the class or interface.  If the parameter
     * <code>loader</code> is null, the class is loaded through the bootstrap
     * class loader.  The class is initialized only if the
     * <code>initialize</code> parameter is <code>true</code> and if it has
     * not been initialized earlier.
     *
     * <p> If <code>name</code> denotes a primitive type or void, an attempt
     * will be made to locate a user-defined class in the unnamed package whose
     * name is <code>name</code>. Therefore, this method cannot be used to
     * obtain any of the <code>Class</code> objects representing primitive
     * types or void.
     *
     * <p> If <code>name</code> denotes an array class, the component type of
     * the array class is loaded but not initialized.
     *
     * <p> For example, in an instance method the expression:
     *
     * <blockquote><pre>
     *  Class.forName("Foo")
     * </pre></blockquote>
     *
     * is equivalent to:
     *
     * <blockquote><pre>
     *  Class.forName("Foo", true, this.getClass().getClassLoader())
     * </pre></blockquote>
     *
     * Note that this method throws errors related to loading, linking or
     * initializing as specified in Sections 12.2, 12.3 and 12.4 of <em>The
     * Java Language Specification</em>.
     * Note that this method does not check whether the requested class 
     * is accessible to its caller.
     *
     * <p> If the <code>loader</code> is <code>null</code>, and a security
     * manager is present, and the caller's class loader is not null, then this
     * method calls the security manager's <code>checkPermission</code> method
     * with a <code>RuntimePermission("getClassLoader")</code> permission to
     * ensure it's ok to access the bootstrap class loader.
     *
     * @param name       fully qualified name of the desired class
     * @param initialize whether the class must be initialized
     * @param loader     class loader from which the class must be loaded
     * @return           class object representing the desired class
     * 
     * @exception LinkageError if the linkage fails
     * @exception ExceptionInInitializerError if the initialization provoked
     *            by this method fails
     * @exception ClassNotFoundException if the class cannot be located by
     *            the specified class loader
     *
     * @see 	  java.lang.Class#forName(String) 
     * @see 	  java.lang.ClassLoader
     * @since 	  1.2
     */
    public static Class forName(String name, boolean initialize,
				ClassLoader loader)
        throws ClassNotFoundException
    {
	if (loader == null) {
	    SecurityManager sm = System.getSecurityManager();
	    if (sm != null) {
		ClassLoader ccl = ClassLoader.getCallerClassLoader();
		if (ccl != null) {
		    sm.checkPermission(
			SecurityConstants.GET_CLASSLOADER_PERMISSION);
		}
	    }
	}
	return forName0(name, initialize, loader);
    }

    /** Called after security checks have been made. */
    private static native Class forName0(String name, boolean initialize,
					 ClassLoader loader)
	throws ClassNotFoundException;

    /**
     * Creates a new instance of the class represented by this <tt>Class</tt>
     * object.  The class is instantiated as if by a <code>new</code>
     * expression with an empty argument list.  The class is initialized if it
     * has not already been initialized.
     *
     * <p>If there is a security manager, this method first calls the security
     * manager's <code>checkMemberAccess</code> method with <code>this</code>
     * and <code>Member.PUBLIC</code> as its arguments. If the class is in a
     * package, then this method also calls the security manager's
     * <code>checkPackageAccess</code> method with the package name as its
     * argument. Either of these calls could result in a SecurityException.
     *
     * @return     a newly allocated instance of the class represented by this
     *             object.
     * @exception  IllegalAccessException  if the class or its nullary 
     *               constructor is not accessible.
     * @exception  InstantiationException 
     *               if this <code>Class</code> represents an abstract class,
     *               an interface, an array class, a primitive type, or void;
     *               or if the class has no nullary constructor;
     *               or if the instantiation fails for some other reason.
     * @exception  ExceptionInInitializerError if the initialization
     *               provoked by this method fails.
     * @exception  SecurityException if there is no permission to create a new
     *               instance.
     *
     */
    public Object newInstance()
        throws InstantiationException, IllegalAccessException
    {
	if (System.getSecurityManager() != null) {
	    checkMemberAccess(Member.PUBLIC, ClassLoader.getCallerClassLoader());
	}

	int flags = Modifier.ABSTRACT | Modifier.INTERFACE;
	if ((getModifiers() & flags) != 0 || isPrimitive()) {
	    throw new InstantiationException(this.getName() + 
					     " is an interface or abstract");
	}
	try {
	    CVM.setContextArtificial();
	    Constructor c = getConstructor0(new Class[0], Member.DECLARED);
	    return c.newInstance(new Object[0]);
	} catch (NoSuchMethodException e) {
	    throw new InstantiationException(this.getName()+" has no appropriate constructor");
	} catch (InvocationTargetException e) {
	    // There should be a better way!  Maybe we need a
	    // private entry point to the Constructor.newInstance()
	    // implemenation that doesn't wrap things in
	    // InvocationTargetException
	    throw CVM.throwLocalException(e.getTargetException());
	}
    }


    /**
     * Determines if the specified <code>Object</code> is assignment-compatible
     * with the object represented by this <code>Class</code>.  This method is
     * the dynamic equivalent of the Java language <code>instanceof</code>
     * operator. The method returns <code>true</code> if the specified
     * <code>Object</code> argument is non-null and can be cast to the
     * reference type represented by this <code>Class</code> object without
     * raising a <code>ClassCastException.</code> It returns <code>false</code>
     * otherwise.
     *
     * <p> Specifically, if this <code>Class</code> object represents a
     * declared class, this method returns <code>true</code> if the specified
     * <code>Object</code> argument is an instance of the represented class (or
     * of any of its subclasses); it returns <code>false</code> otherwise. If
     * this <code>Class</code> object represents an array class, this method
     * returns <code>true</code> if the specified <code>Object</code> argument
     * can be converted to an object of the array class by an identity
     * conversion or by a widening reference conversion; it returns
     * <code>false</code> otherwise. If this <code>Class</code> object
     * represents an interface, this method returns <code>true</code> if the
     * class or any superclass of the specified <code>Object</code> argument
     * implements this interface; it returns <code>false</code> otherwise. If
     * this <code>Class</code> object represents a primitive type, this method
     * returns <code>false</code>.
     *
     * @param   obj the object to check
     * @return  true if <code>obj</code> is an instance of this class
     *
     * @since JDK1.1
     */
    public native boolean isInstance(Object obj);


    /**
     * Determines if the class or interface represented by this
     * <code>Class</code> object is either the same as, or is a superclass or
     * superinterface of, the class or interface represented by the specified
     * <code>Class</code> parameter. It returns <code>true</code> if so;
     * otherwise it returns <code>false</code>. If this <code>Class</code>
     * object represents a primitive type, this method returns
     * <code>true</code> if the specified <code>Class</code> parameter is
     * exactly this <code>Class</code> object; otherwise it returns
     * <code>false</code>.
     *
     * <p> Specifically, this method tests whether the type represented by the
     * specified <code>Class</code> parameter can be converted to the type
     * represented by this <code>Class</code> object via an identity conversion
     * or via a widening reference conversion. See <em>The Java Language
     * Specification</em>, sections 5.1.1 and 5.1.4 , for details.
     * 
     * @param cls the <code>Class</code> object to be checked
     * @return the <code>boolean</code> value indicating whether objects of the
     * type <code>cls</code> can be assigned to objects of this class
     * @exception NullPointerException if the specified Class parameter is
     *            null.
     * @since JDK1.1
     */
    public native boolean isAssignableFrom(Class cls);


    /**
     * Determines if the specified <code>Class</code> object represents an
     * interface type.
     *
     * @return  <code>true</code> if this object represents an interface;
     *          <code>false</code> otherwise.
     */
    public boolean isInterface() {
	return Modifier.isInterface(getModifiers());
    }


    /**
     * Determines if this <code>Class</code> object represents an array class.
     *
     * @return  <code>true</code> if this object represents an array class;
     *          <code>false</code> otherwise.
     * @since   JDK1.1
     */
    public native boolean isArray();


    /**
     * Determines if the specified <code>Class</code> object represents a
     * primitive type.
     *
     * <p> There are nine predefined <code>Class</code> objects to represent
     * the eight primitive types and void.  These are created by the Java
     * Virtual Machine, and have the same names as the primitive types that
     * they represent, namely <code>boolean</code>, <code>byte</code>,
     * <code>char</code>, <code>short</code>, <code>int</code>,
     * <code>long</code>, <code>float</code>, and <code>double</code>.
     *
     * <p> These objects may only be accessed via the following public static
     * final variables, and are the only <code>Class</code> objects for which
     * this method returns <code>true</code>.
     *
     * @return true if and only if this class represents a primitive type
     *
     * @see     java.lang.Boolean#TYPE
     * @see     java.lang.Character#TYPE
     * @see     java.lang.Byte#TYPE
     * @see     java.lang.Short#TYPE
     * @see     java.lang.Integer#TYPE
     * @see     java.lang.Long#TYPE
     * @see     java.lang.Float#TYPE
     * @see     java.lang.Double#TYPE
     * @see     java.lang.Void#TYPE
     * @since JDK1.1
     */
    public native boolean isPrimitive();

    /**
     * Returns the  name of the entity (class, interface, array class,
     * primitive type, or void) represented by this <tt>Class</tt> object,
     * as a <tt>String</tt>.
     * 
     * <p> If this class object represents a reference type that is not an
     * array type then the binary name of the class is returned, as specified
     * by the Java Language Specification, Second Edition.
     *
     * <p> If this class object represents a primitive type or void, then the
     * name returned is a <tt>String</tt> equal to the Java language
     * keyword corresponding to the primitive type or void.
     * 
     * <p> If this class object represents a class of arrays, then the internal
     * form of the name consists of the name of the element type preceded by
     * one or more '<tt>[</tt>' characters representing the depth of the array
     * nesting.  The encoding of element type names is as follows:
     *
     * <blockquote><table summary="Element types and encodings">
     * <tr><th> Element Type <th> Encoding
     * <tr><td> boolean      <td align=center> Z
     * <tr><td> byte         <td align=center> B
     * <tr><td> char         <td align=center> C
     * <tr><td> class or interface  <td align=center> L<i>classname;</i>
     * <tr><td> double       <td align=center> D
     * <tr><td> float        <td align=center> F
     * <tr><td> int          <td align=center> I
     * <tr><td> long         <td align=center> J
     * <tr><td> short        <td align=center> S
     * </table></blockquote>
     *
     * <p> The class or interface name <i>classname</i> is the binary name of
     * the class specified above.
     *
     * <p> Examples:
     * <blockquote><pre>
     * String.class.getName()
     *     returns "java.lang.String"
     * byte.class.getName()
     *     returns "byte"
     * (new Object[3]).getClass().getName()
     *     returns "[Ljava.lang.Object;"
     * (new int[3][4][5][6][7][8][9]).getClass().getName()
     *     returns "[[[[[[[I"
     * </pre></blockquote>
     *
     * @return  the name of the class or interface
     *          represented by this object.
     */
    public native String getName();


    /**
     * Returns the class loader for the class.  Some implementations may use
     * null to represent the bootstrap class loader. This method will return
     * null in such implementations if this class was loaded by the bootstrap
     * class loader.
     *
     * <p> If a security manager is present, and the caller's class loader is
     * not null and the caller's class loader is not the same as or an ancestor of
     * the class loader for the class whose class loader is requested, then
     * this method calls the security manager's <code>checkPermission</code> 
     * method with a <code>RuntimePermission("getClassLoader")</code> 
     * permission to ensure it's ok to access the class loader for the class.
     * 
     * <p>If this object
     * represents a primitive type or void, null is returned.
     *
     * @return  the class loader that loaded the class or interface
     *          represented by this object.
     * @throws SecurityException
     *    if a security manager exists and its 
     *    <code>checkPermission</code> method denies
     *    access to the class loader for the class.
     * @see java.lang.ClassLoader
     * @see SecurityManager#checkPermission
     * @see java.lang.RuntimePermission
     */
    public ClassLoader getClassLoader() {
        ClassLoader cl = getClassLoader0();
        if (cl == null)
            return null;
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            ClassLoader ccl = ClassLoader.getCallerClassLoader();
            if (ccl != null && ccl != cl && !cl.isAncestor(ccl)) {
                sm.checkPermission(SecurityConstants.GET_CLASSLOADER_PERMISSION);
            }
        }
        return cl;
    }

    // Package-private to allow ClassLoader access
    native ClassLoader getClassLoader0();


    /**
     * Returns the <code>Class</code> representing the superclass of the entity
     * (class, interface, primitive type or void) represented by this
     * <code>Class</code>.  If this <code>Class</code> represents either the
     * <code>Object</code> class, an interface, a primitive type, or void, then
     * null is returned.  If this object represents an array class then the
     * <code>Class</code> object representing the <code>Object</code> class is
     * returned.
     *
     * @return the superclass of the class represented by this object.
     */
    public native Class getSuperclass();


    /**
     * Gets the package for this class.  The class loader of this class is used
     * to find the package.  If the class was loaded by the bootstrap class
     * loader the set of packages loaded from CLASSPATH is searched to find the
     * package of the class. Null is returned if no package object was created
     * by the class loader of this class.
     *
     * <p> Packages have attributes for versions and specifications only if the
     * information was defined in the manifests that accompany the classes, and
     * if the class loader created the package instance with the attributes
     * from the manifest.
     *
     * @return the package of the class, or null if no package
     *         information is available from the archive or codebase.
     */
    public Package getPackage() {
        return Package.getPackage(this);
    }


    /**
     * Determines the interfaces implemented by the class or interface
     * represented by this object.
     *
     * <p> If this object represents a class, the return value is an array
     * containing objects representing all interfaces implemented by the
     * class. The order of the interface objects in the array corresponds to
     * the order of the interface names in the <code>implements</code> clause
     * of the declaration of the class represented by this object. For 
     * example, given the declaration:
     * <blockquote><pre>
     * class Shimmer implements FloorWax, DessertTopping { ... }
     * </pre></blockquote>
     * suppose the value of <code>s</code> is an instance of 
     * <code>Shimmer</code>; the value of the expression:
     * <blockquote><pre>
     * s.getClass().getInterfaces()[0]
     * </pre></blockquote>
     * is the <code>Class</code> object that represents interface 
     * <code>FloorWax</code>; and the value of:
     * <blockquote><pre>
     * s.getClass().getInterfaces()[1]
     * </pre></blockquote>
     * is the <code>Class</code> object that represents interface 
     * <code>DessertTopping</code>.
     *
     * <p> If this object represents an interface, the array contains objects
     * representing all interfaces extended by the interface. The order of the
     * interface objects in the array corresponds to the order of the interface
     * names in the <code>extends</code> clause of the declaration of the
     * interface represented by this object.
     *
     * <p> If this object represents a class or interface that implements no
     * interfaces, the method returns an array of length 0.
     *
     * <p> If this object represents a primitive type or void, the method
     * returns an array of length 0.
     *
     * @return an array of interfaces implemented by this class.
     */
    public native Class[] getInterfaces();


    /**
     * Returns the <code>Class</code> representing the component type of an
     * array.  If this class does not represent an array class this method
     * returns null.
     *
     * @return the <code>Class</code> representing the component type of this
     * class if this class is an array
     * @see     java.lang.reflect.Array
     * @since JDK1.1
     */
    public native Class getComponentType();


    /**
     * Returns the Java language modifiers for this class or interface, encoded
     * in an integer. The modifiers consist of the Java Virtual Machine's
     * constants for <code>public</code>, <code>protected</code>,
     * <code>private</code>, <code>final</code>, <code>static</code>,
     * <code>abstract</code> and <code>interface</code>; they should be decoded
     * using the methods of class <code>Modifier</code>.
     *
     * <p> If the underlying class is an array class, then its
     * <code>public</code>, <code>private</code> and <code>protected</code>
     * modifiers are the same as those of its component type.  If this
     * <code>Class</code> represents a primitive type or void, its
     * <code>public</code> modifier is always <code>true</code>, and its
     * <code>protected</code> and <code>private</code> modifiers are always
     * <code>false</code>. If this object represents an array class, a
     * primitive type or void, then its <code>final</code> modifier is always
     * <code>true</code> and its interface modifier is always
     * <code>false</code>. The values of its other modifiers are not determined
     * by this specification.
     *
     * <p> The modifier encodings are defined in <em>The Java Virtual Machine
     * Specification</em>, table 4.1.
     *
     * @return the <code>int</code> representing the modifiers for this class
     * @see     java.lang.reflect.Modifier
     * @since JDK1.1
     */
    public native int getModifiers();

    /* A private mapping of classes to their signers */
    private static Map classToSigners;

    private static void createClassToSignersMap() {
	WeakHashMap map = new WeakHashMap(11);
	Class.classToSigners = Collections.synchronizedMap(map);
    }

    /**
     * Gets the signers of this class.
     *
     * @return  the signers of this class, or null if there are no signers.  In
     * 		particular, this method returns null if this object represents
     * 		a primitive type or void.
     * @since 	JDK1.1
     */
    public Object[] getSigners() {
	Object[] signers;
	if (Class.classToSigners == null) {
	    Class.createClassToSignersMap();
	}
	signers = (Object[])Class.classToSigners.get(this);
	if (signers == null) {
	    return null;
	} else {
	    return (Object[])signers.clone();
	}
    }
        

    /**
     * Set the signers of this class.
     */
    void setSigners(Object[] signers) {
	if (Class.classToSigners == null) {
	    Class.createClassToSignersMap();
	}
	Class.classToSigners.put(this, signers);
    }


    /**
     * If the class or interface represented by this <code>Class</code> object
     * is a member of another class, returns the <code>Class</code> object
     * representing the class in which it was declared.  This method returns
     * null if this class or interface is not a member of any other class.  If
     * this <code>Class</code> object represents an array class, a primitive
     * type, or void,then this method returns null.
     *
     * @return the declaring class for this class
     * @since JDK1.1
     */
    public native Class getDeclaringClass();


    /**
     * Returns an array containing <code>Class</code> objects representing all
     * the public classes and interfaces that are members of the class
     * represented by this <code>Class</code> object.  This includes public
     * class and interface members inherited from superclasses and public class
     * and interface members declared by the class.  This method returns an
     * array of length 0 if this <code>Class</code> object has no public member
     * classes or interfaces.  This method also returns an array of length 0 if
     * this <code>Class</code> object represents a primitive type, an array
     * class, or void.
     * 
     * <p>For this class and each of its superclasses, the following
     * security checks are performed:
     * If there is a security manager, the security manager's
     * <code>checkMemberAccess</code> method is called with <code>this</code>
     * and <code>Member.PUBLIC</code> as its arguments, where <code>this</code>
     * is this class or the superclass whose members are being determined. If
     * the class is in a package, then the security manager's
     * <code>checkPackageAccess</code> method is also called with the package
     * name as its argument. Either of these calls could result in a
     * SecurityException.
     *
     * @return the array of <code>Class</code> objects representing the public
     * members of this class
     * @exception SecurityException    if access to the information is denied.
     * @see       SecurityManager#checkMemberAccess(Class, int)
     * @see       SecurityManager#checkPackageAccess(String)
     *
     * @since JDK1.1
     */
    public Class[] getClasses() {
	// be very careful not to change the stack depth of this
	// checkMemberAccess call for security reasons 
	// see java.lang.SecurityManager.checkMemberAccess
        checkMemberAccess(Member.PUBLIC, ClassLoader.getCallerClassLoader());

	// Privileged so this implementation can look at DECLARED classes,
	// something the caller might not have privilege to do.  The code here
	// is allowed to look at DECLARED classes because (1) it does not hand
	// out anything other than public members and (2) public member access
	// has already been ok'd by the SecurityManager.

	Class[] result = (Class[]) java.security.AccessController.doPrivileged
	    (new java.security.PrivilegedAction() {
	        public Object run() {
		    java.util.List list = new java.util.ArrayList();
		    Class currentClass = Class.this;
		    while (currentClass != null) {
			Class[] members = currentClass.getDeclaredClasses();
			for (int i = 0; i < members.length; i++) {
			    if (Modifier.isPublic(members[i].getModifiers())) {
				list.add(members[i]);
			    }
			}
			currentClass = currentClass.getSuperclass();
		    }
		    return list.toArray(new Class[0]);
		}
	    });

        return result;
    }


    /**
     * Returns an array containing <code>Field</code> objects reflecting all
     * the accessible public fields of the class or interface represented by
     * this <code>Class</code> object.  The elements in the array returned are
     * not sorted and are not in any particular order.  This method returns an
     * array of length 0 if the class or interface has no accessible public
     * fields, or if it represents an array class, a primitive type, or void.
     *
     * <p> Specifically, if this <code>Class</code> object represents a class,
     * this method returns the public fields of this class and of all its
     * superclasses.  If this <code>Class</code> object represents an
     * interface, this method returns the fields of this interface and of all
     * its superinterfaces.
     *
     * <p>If there is a security manager, this method first
     * calls the security manager's <code>checkMemberAccess</code> method
     * with <code>this</code> and <code>Member.PUBLIC</code> 
     * as its arguments. If the class is in a package, then this method
     * also calls the security manager's <code>checkPackageAccess</code> 
     * method with the package name as its argument. Either of these calls
     * could result in a SecurityException.
     * 
     * <p> The implicit length field for array class is not reflected by this
     * method. User code should use the methods of class <code>Array</code> to
     * manipulate arrays.
     *
     * <p> See <em>The Java Language Specification</em>, sections 8.2 and 8.3.
     *
     * @return the array of <code>Field</code> objects representing the
     * public fields
     * @exception SecurityException    if access to the information is denied.
     * @see       java.lang.reflect.Field
     * @see       SecurityManager#checkMemberAccess(Class, int)
     * @see       SecurityManager#checkPackageAccess(String)
     * @since JDK1.1
     */
    public Field[] getFields() throws SecurityException {
	// be very careful not to change the stack depth of this
	// checkMemberAccess call for security reasons 
	// see java.lang.SecurityManager.checkMemberAccess
        checkMemberAccess(Member.PUBLIC, ClassLoader.getCallerClassLoader());
        return getFields0(Member.PUBLIC);
    }


    /**
     * Returns an array containing <code>Method</code> objects reflecting all
     * the public <em>member</em> methods of the class or interface represented
     * by this <code>Class</code> object, including those declared by the class
     * or interface and and those inherited from superclasses and
     * superinterfaces.  The elements in the array returned are not sorted and
     * are not in any particular order.  This method returns an array of length
     * 0 if this <code>Class</code> object represents a class or interface that
     * has no public member methods, or if this <code>Class</code> object
     * represents an array class, primitive type, or void.
     *
     * <p>If there is a security manager, this method first
     * calls the security manager's <code>checkMemberAccess</code> method
     * with <code>this</code> and <code>Member.PUBLIC</code> 
     * as its arguments. If the class is in a package, then this method
     * also calls the security manager's <code>checkPackageAccess</code> 
     * method with the package name 
     * as its argument. Either of these calls could result in a SecurityException.
     * 
     * <p> The class initialization method <code>&lt;clinit&gt;</code> is not
     * included in the returned array. If the class declares multiple public
     * member methods with the same parameter types, they are all included in
     * the returned array.
     *
     * <p> See <em>The Java Language Specification</em>, sections 8.2 and 8.4.
     *
     * @return the array of <code>Method</code> objects representing the
     * public methods of this class
     * @exception SecurityException    if access to the information is denied.
     * @see       java.lang.reflect.Method
     * @see       SecurityManager#checkMemberAccess(Class, int)
     * @see       SecurityManager#checkPackageAccess(String)
     * @since JDK1.1
     */
    public Method[] getMethods() throws SecurityException {
	// be very careful not to change the stack depth of this
	// checkMemberAccess call for security reasons 
	// see java.lang.SecurityManager.checkMemberAccess
        checkMemberAccess(Member.PUBLIC, ClassLoader.getCallerClassLoader());
        return getMethods0(Member.PUBLIC);
    }


    /**
     * Returns an array containing <code>Constructor</code> objects reflecting
     * all the public constructors of the class represented by this
     * <code>Class</code> object.  An array of length 0 is returned if the
     * class has no public constructors, or if the class is an array class, or
     * if the class reflects a primitive type or void.
     *
     * <p>If there is a security manager, this method first
     * calls the security manager's <code>checkMemberAccess</code> method
     * with <code>this</code> and <code>Member.PUBLIC</code> 
     * as its arguments. If the class is in a package, then this method
     * also calls the security manager's <code>checkPackageAccess</code> 
     * method with the package name 
     * as its argument. Either of these calls could result in a SecurityException.
     * 
     * @return the array containing <code>Method</code> objects for all the
     * declared public constructors of this class matches the specified
     * <code>parameterTypes</code>
     * @exception SecurityException    if access to the information is denied.
     * @see       java.lang.reflect.Constructor
     * @see       SecurityManager#checkMemberAccess(Class, int)
     * @see       SecurityManager#checkPackageAccess(String)
     * @since JDK1.1
     */
    public Constructor[] getConstructors() throws SecurityException {
	// be very careful not to change the stack depth of this
	// checkMemberAccess call for security reasons 
	// see java.lang.SecurityManager.checkMemberAccess
        checkMemberAccess(Member.PUBLIC, ClassLoader.getCallerClassLoader());
        return getConstructors0(Member.PUBLIC);
    }


    /**
     * Returns a <code>Field</code> object that reflects the specified public
     * member field of the class or interface represented by this
     * <code>Class</code> object. The <code>name</code> parameter is a
     * <code>String</code> specifying the simple name of the desired field.
     *
     * <p>If there is a security manager, this method first
     * calls the security manager's <code>checkMemberAccess</code> method
     * with <code>this</code> and <code>Member.PUBLIC</code> 
     * as its arguments. If the class is in a package, then this method
     * also calls the security manager's <code>checkPackageAccess</code> 
     * method with the package name 
     * as its argument. Either of these calls could result in a SecurityException.
     *
     * <p> The field to be reflected is determined by the algorithm that
     * follows.  Let C be the class represented by this object:
     * <OL>
     * <LI> If C declares a public field with the name specified, that is the
     *      field to be reflected.</LI>
     * <LI> If no field was found in step 1 above, this algorithm is applied
     * 	    recursively to each direct superinterface of C. The direct
     * 	    superinterfaces are searched in the order they were declared.</LI>
     * <LI> If no field was found in steps 1 and 2 above, and C has a
     *      superclass S, then this algorithm is invoked recursively upon S.
     *      If C has no superclass, then a <code>NoSuchFieldException</code>
     *      is thrown.</LI>
     * </OL>
     *
     * <p> See <em>The Java Language Specification</em>, sections 8.2 and 8.3.
     * 
     * @param name the field name
     * @return  the <code>Field</code> object of this class specified by 
     * <code>name</code>
     * @exception NoSuchFieldException if a field with the specified name is
     *              not found.
     * @exception NullPointerException if <code>name</code> is <code>null</code>
     * @exception SecurityException    if access to the information is denied.
     * @see       java.lang.reflect.Field
     * @see       SecurityManager#checkMemberAccess(Class, int)
     * @see       SecurityManager#checkPackageAccess(String)
     * @since JDK1.1
     */
    public Field getField(String name)
        throws NoSuchFieldException, SecurityException {
	// be very careful not to change the stack depth of this
	// checkMemberAccess call for security reasons 
	// see java.lang.SecurityManager.checkMemberAccess
        checkMemberAccess(Member.PUBLIC, ClassLoader.getCallerClassLoader());
        return getField0(name, Member.PUBLIC);
    }


    /**
     * Returns a <code>Method</code> object that reflects the specified public
     * member method of the class or interface represented by this
     * <code>Class</code> object. The <code>name</code> parameter is a
     * <code>String</code> specifying the simple name the desired method. The
     * <code>parameterTypes</code> parameter is an array of <code>Class</code>
     * objects that identify the method's formal parameter types, in declared
     * order. If <code>parameterTypes</code> is <code>null</code>, it is 
     * treated as if it were an empty array.
     *
     * <p>If there is a security manager, this method first
     * calls the security manager's <code>checkMemberAccess</code> method
     * with <code>this</code> and <code>Member.PUBLIC</code> 
     * as its arguments. If the class is in a package, then this method
     * also calls the security manager's <code>checkPackageAccess</code> 
     * method with the package name 
     * as its argument. Either of these calls could result in a SecurityException.
     *
     * <p> If the <code>name</code> is "&lt;init&gt;"or "&lt;clinit&gt;" a
     * <code>NoSuchMethodException</code> is raised. Otherwise, the method to
     * be reflected is determined by the algorithm that follows.  Let C be the
     * class represented by this object:
     * <OL>
     * <LI> C is searched for any <I>matching methods</I>. If no matching
     * 	    method is found, the algorithm of step 1 is invoked recursively on
     * 	    the superclass of C.</LI>
     * <LI> If no method was found in step 1 above, the superinterfaces of C
     *      are searched for a matching method. If any such method is found, it
     *      is reflected.</LI>
     * </OL>
     *
     * To find a matching method in a class C:&nbsp; If C declares exactly one
     * public method with the specified name and exactly the same formal
     * parameter types, that is the method reflected. If more than one such
     * method is found in C, and one of these methods has a return type that is
     * more specific than any of the others, that method is reflected;
     * otherwise one of the methods is chosen arbitrarily.
     *
     * <p> See <em>The Java Language Specification</em>, sections 8.2 and 8.4.
     *
     * @param name the name of the method
     * @param parameterTypes the list of parameters
     * @return the <code>Method</code> object that matches the specified
     * <code>name</code> and <code>parameterTypes</code>
     * @exception NoSuchMethodException if a matching method is not found
     *            or if the name is "&lt;init&gt;"or "&lt;clinit&gt;".
     * @exception NullPointerException if <code>name</code> is <code>null</code>
     * @exception SecurityException    if access to the information is denied.
     * @see       java.lang.reflect.Method
     * @see       SecurityManager#checkMemberAccess(Class, int)
     * @see       SecurityManager#checkPackageAccess(String)
     * @since JDK1.1
     */
    public Method getMethod(String name, Class[] parameterTypes)
        throws NoSuchMethodException, SecurityException {
	// be very careful not to change the stack depth of this
	// checkMemberAccess call for security reasons 
	// see java.lang.SecurityManager.checkMemberAccess
        checkMemberAccess(Member.PUBLIC, ClassLoader.getCallerClassLoader());
        return getMethod0(name, parameterTypes, Member.PUBLIC);
    }


    /**
     * Returns a <code>Constructor</code> object that reflects the specified
     * public constructor of the class represented by this <code>Class</code>
     * object. The <code>parameterTypes</code> parameter is an array of
     * <code>Class</code> objects that identify the constructor's formal
     * parameter types, in declared order.
     *
     * <p> The constructor to reflect is the public constructor of the class
     * represented by this <code>Class</code> object whose formal parameter
     * types match those specified by <code>parameterTypes</code>.
     *
     * <p>If there is a security manager, this method first
     * calls the security manager's <code>checkMemberAccess</code> method
     * with <code>this</code> and <code>Member.PUBLIC</code> 
     * as its arguments. If the class is in a package, then this method
     * also calls the security manager's <code>checkPackageAccess</code> method
     * with the package name as its argument. Either of these calls could
     * result in a SecurityException.
     *
     * @param parameterTypes the parameter array
     * @return the <code>Method</code> object of the public constructor that
     * matches the specified <code>parameterTypes</code>
     * @exception NoSuchMethodException if a matching method is not found.
     * @exception SecurityException     if access to the information is denied.
     * @see       java.lang.reflect.Constructor
     * @see       SecurityManager#checkMemberAccess(Class, int)
     * @see       SecurityManager#checkPackageAccess(String)
     * @since JDK1.1
     */
    public Constructor getConstructor(Class[] parameterTypes)
        throws NoSuchMethodException, SecurityException {
	// be very careful not to change the stack depth of this
	// checkMemberAccess call for security reasons 
	// see java.lang.SecurityManager.checkMemberAccess
        checkMemberAccess(Member.PUBLIC, ClassLoader.getCallerClassLoader());
        return getConstructor0(parameterTypes, Member.PUBLIC);
    }


    /**
     * Returns an array of <code>Class</code> objects reflecting all the
     * classes and interfaces declared as members of the class represented by
     * this <code>Class</code> object. This includes public, protected, default
     * (package) access, and private classes and interfaces declared by the
     * class, but excludes inherited classes and interfaces.  This method
     * returns an array of length 0 if the class declares no classes or
     * interfaces as members, or if this <code>Class</code> object represents a
     * primitive type, an array class, or void.
     *
     * <p>If there is a security manager, this method first
     * calls the security manager's <code>checkMemberAccess</code> method
     * with <code>this</code> and <code>Member.DECLARED</code> 
     * as its arguments. If the class is in a package, then this method also
     * calls the security manager's <code>checkPackageAccess</code> method with
     * the package name as its argument. Either of these calls could result in
     * a SecurityException.
     *
     * @return the array of <code>Class</code> objects representing all the 
     * declared members of this class
     * @exception SecurityException    if access to the information is denied.
     * @see       SecurityManager#checkMemberAccess(Class, int)
     * @see       SecurityManager#checkPackageAccess(String)
     * @since JDK1.1
     */
    public Class[] getDeclaredClasses() throws SecurityException {
	// be very careful not to change the stack depth of this
	// checkMemberAccess call for security reasons 
	// see java.lang.SecurityManager.checkMemberAccess
        checkMemberAccess(Member.DECLARED, ClassLoader.getCallerClassLoader());
        return getDeclaredClasses0();
    }


    /**
     * Returns an array of <code>Field</code> objects reflecting all the fields
     * declared by the class or interface represented by this
     * <code>Class</code> object. This includes public, protected, default
     * (package) access, and private fields, but excludes inherited fields.
     * The elements in the array returned are not sorted and are not in any
     * particular order.  This method returns an array of length 0 if the class
     * or interface declares no fields, or if this <code>Class</code> object
     * represents a primitive type, an array class, or void.
     *
     * <p> See <em>The Java Language Specification</em>, sections 8.2 and 8.3.
     *
     * <p>If there is a security manager, this method first
     * calls the security manager's <code>checkMemberAccess</code> method with
     * <code>this</code> and <code>Member.DECLARED</code> as its arguments. If
     * the class is in a package, then this method also calls the security
     * manager's <code>checkPackageAccess</code> method with the package name
     * as its argument. Either of these calls could result in a
     * SecurityException.
     *
     * @return    the array of <code>Field</code> objects representing all the
     * declared fields of this class
     * @exception SecurityException    if access to the information is denied.
     * @see       java.lang.reflect.Field
     * @see       SecurityManager#checkMemberAccess(Class, int)
     * @see       SecurityManager#checkPackageAccess(String)
     * @since JDK1.1
     */
    public Field[] getDeclaredFields() throws SecurityException {
	// be very careful not to change the stack depth of this
	// checkMemberAccess call for security reasons 
	// see java.lang.SecurityManager.checkMemberAccess
        checkMemberAccess(Member.DECLARED, ClassLoader.getCallerClassLoader());
        return getFields0(Member.DECLARED);
    }


    /**
     * Returns an array of <code>Method</code> objects reflecting all the
     * methods declared by the class or interface represented by this
     * <code>Class</code> object. This includes public, protected, default
     * (package) access, and private methods, but excludes inherited methods.
     * The elements in the array returned are not sorted and are not in any
     * particular order.  This method returns an array of length 0 if the class
     * or interface declares no methods, or if this <code>Class</code> object
     * represents a primitive type, an array class, or void.  The class
     * initialization method <code>&lt;clinit&gt;</code> is not included in the
     * returned array. If the class declares multiple public member methods
     * with the same parameter types, they are all included in the returned
     * array.
     *
     * <p> See <em>The Java Language Specification</em>, section 8.2.
     *
     * <p>If there is a security manager, this method first
     * calls the security manager's <code>checkMemberAccess</code> method
     * with <code>this</code> and <code>Member.DECLARED</code> 
     * as its arguments. If the class is in a package, then this method
     * also calls the security manager's <code>checkPackageAccess</code> method
     * with the package name as its argument. Either of these calls could
     * result in a SecurityException.
     *
     * @return    the array of <code>Method</code> objects representing all the
     * declared methods of this class
     * @exception SecurityException    if access to the information is denied.
     * @see       java.lang.reflect.Method
     * @see       SecurityManager#checkMemberAccess(Class, int)
     * @see       SecurityManager#checkPackageAccess(String)
     * @since JDK1.1
     */
    public Method[] getDeclaredMethods() throws SecurityException {
	// be very careful not to change the stack depth of this
	// checkMemberAccess call for security reasons 
	// see java.lang.SecurityManager.checkMemberAccess
        checkMemberAccess(Member.DECLARED, ClassLoader.getCallerClassLoader());
        return getMethods0(Member.DECLARED);
    }


    /**
     * Returns an array of <code>Constructor</code> objects reflecting all the
     * constructors declared by the class represented by this
     * <code>Class</code> object. These are public, protected, default
     * (package) access, and private constructors.  The elements in the array
     * returned are not sorted and are not in any particular order.  If the
     * class has a default constructor, it is included in the returned array.
     * This method returns an array of length 0 if this <code>Class</code>
     * object represents an interface, a primitive type, an array class, or
     * void.
     *
     * <p> See <em>The Java Language Specification</em>, section 8.2.
     *
     * <p>If there is a security manager, this method first
     * calls the security manager's <code>checkMemberAccess</code> method
     * with <code>this</code> and <code>Member.DECLARED</code> 
     * as its arguments. If the class is in a package, then this method
     * also calls the security manager's <code>checkPackageAccess</code> method
     * with the package name as its argument. Either of these calls could
     * result in a SecurityException.
     *
     * @return    the array of <code>Method</code> objects representing all the
     * declared constructors of this class
     * @exception SecurityException    if access to the information is denied.
     * @see       java.lang.reflect.Constructor
     * @see       SecurityManager#checkMemberAccess(Class, int)
     * @see       SecurityManager#checkPackageAccess(String)
     * @since JDK1.1
     */
    public Constructor[] getDeclaredConstructors() throws SecurityException {
	// be very careful not to change the stack depth of this
	// checkMemberAccess call for security reasons 
	// see java.lang.SecurityManager.checkMemberAccess
        checkMemberAccess(Member.DECLARED, ClassLoader.getCallerClassLoader());
        return getConstructors0(Member.DECLARED);
    }


    /**
     * Returns a <code>Field</code> object that reflects the specified declared
     * field of the class or interface represented by this <code>Class</code>
     * object. The <code>name</code> parameter is a <code>String</code> that
     * specifies the simple name of the desired field.  Note that this method
     * will not reflect the <code>length</code> field of an array class.
     *
     * <p>If there is a security manager, this method first
     * calls the security manager's <code>checkMemberAccess</code> method
     * with <code>this</code> and <code>Member.DECLARED</code> 
     * as its arguments. If the class is in a package, then this method
     * also calls the security manager's <code>checkPackageAccess</code> method
     * with the package name as its argument. Either of these calls could
     * result in a SecurityException.
     *
     * @param name the name of the field
     * @return the <code>Field</code> object for the specified field in this
     * class
     * @exception NoSuchFieldException if a field with the specified name is
     *              not found.
     * @exception NullPointerException if <code>name</code> is <code>null</code>
     * @exception SecurityException    if access to the information is denied.
     * @see       java.lang.reflect.Field
     * @see       SecurityManager#checkMemberAccess(Class, int)
     * @see       SecurityManager#checkPackageAccess(String)
     * @since JDK1.1
     */
    public Field getDeclaredField(String name)
        throws NoSuchFieldException, SecurityException {
	// be very careful not to change the stack depth of this
	// checkMemberAccess call for security reasons 
	// see java.lang.SecurityManager.checkMemberAccess
        checkMemberAccess(Member.DECLARED, ClassLoader.getCallerClassLoader());
        return getField0(name, Member.DECLARED);
    }


    /**
     * Returns a <code>Method</code> object that reflects the specified
     * declared method of the class or interface represented by this
     * <code>Class</code> object. The <code>name</code> parameter is a
     * <code>String</code> that specifies the simple name of the desired
     * method, and the <code>parameterTypes</code> parameter is an array of
     * <code>Class</code> objects that identify the method's formal parameter
     * types, in declared order.  If more than one method with the same
     * parameter types is declared in a class, and one of these methods has a
     * return type that is more specific than any of the others, that method is
     * returned; otherwise one of the methods is chosen arbitrarily.  If the
     * name is "&lt;init&gt;"or "&lt;clinit&gt;" a <code>NoSuchMethodException</code>
     * is raised.
     *
     * <p>If there is a security manager, this method first
     * calls the security manager's <code>checkMemberAccess</code> method
     * with <code>this</code> and <code>Member.DECLARED</code> 
     * as its arguments. If the class is in a package, then this method also
     * calls the security manager's <code>checkPackageAccess</code> method with
     * the package name as its argument. Either of these calls could result in
     * a SecurityException.
     *
     * @param name the name of the method
     * @param parameterTypes the parameter array
     * @return    the <code>Method</code> object for the method of this class
     * matching the specified name and parameters
     * @exception NoSuchMethodException if a matching method is not found.
     * @exception NullPointerException if <code>name</code> is <code>null</code>
     * @exception SecurityException     if access to the information is denied.
     * @see       java.lang.reflect.Method
     * @see       SecurityManager#checkMemberAccess(Class, int)
     * @see       SecurityManager#checkPackageAccess(String)
     * @since JDK1.1
     */
    public Method getDeclaredMethod(String name, Class[] parameterTypes)
        throws NoSuchMethodException, SecurityException {
	// be very careful not to change the stack depth of this
	// checkMemberAccess call for security reasons 
	// see java.lang.SecurityManager.checkMemberAccess
        checkMemberAccess(Member.DECLARED, ClassLoader.getCallerClassLoader());
        return getMethod0(name, parameterTypes, Member.DECLARED);
    }


    /**
     * Returns a <code>Constructor</code> object that reflects the specified
     * constructor of the class or interface represented by this
     * <code>Class</code> object.  The <code>parameterTypes</code> parameter is
     * an array of <code>Class</code> objects that identify the constructor's
     * formal parameter types, in declared order.
     *
     * <p>If there is a security manager, this method first
     * calls the security manager's <code>checkMemberAccess</code> method
     * with <code>this</code> and <code>Member.DECLARED</code> 
     * as its arguments. If the class is in a package, then this method
     * also calls the security manager's <code>checkPackageAccess</code> 
     * method with the package name 
     * as its argument. Either of these calls could result in a SecurityException.
     *
     * @param parameterTypes the parameter array
     * @return    The <code>Method</code> object for the constructor with the
     * specified parameter list
     * @exception NoSuchMethodException if a matching method is not found.
     * @exception SecurityException     if access to the information is denied.
     * @see       java.lang.reflect.Constructor
     * @see       SecurityManager#checkMemberAccess(Class, int)
     * @see       SecurityManager#checkPackageAccess(String)
     * @since JDK1.1
     */
    public Constructor getDeclaredConstructor(Class[] parameterTypes)
        throws NoSuchMethodException, SecurityException {
	// be very careful not to change the stack depth of this
	// checkMemberAccess call for security reasons 
	// see java.lang.SecurityManager.checkMemberAccess
        checkMemberAccess(Member.DECLARED, ClassLoader.getCallerClassLoader());
        return getConstructor0(parameterTypes, Member.DECLARED);
    }


    /**
     * Finds a resource with a given name.  This method returns null if no
     * resource with this name is found.  The rules for searching
     * resources associated with a given class are implemented by the
     * defining class loader of the class.
     *
     * <p> This method delegates the call to its class loader, after making
     * these changes to the resource name: if the resource name starts with
     * "/", it is unchanged; otherwise, the package name is prepended to the
     * resource name after converting "." to "/".  If this object was loaded by
     * the bootstrap loader, the call is delegated to
     * <code>ClassLoader.getSystemResourceAsStream</code>.
     *
     * @param name  name of the desired resource
     * @return      a <code>java.io.InputStream</code> object.
     * @throws NullPointerException if <code>name</code> is <code>null</code>.
     * @see         java.lang.ClassLoader
     * @since JDK1.1
     */
    public InputStream getResourceAsStream(String name) {
        name = resolveName(name);
        ClassLoader cl = getClassLoader0();
        if (cl==null) {
            // A system class.
            return ClassLoader.getSystemResourceAsStream(name);
        }
        return cl.getResourceAsStream(name);
    }


    /**
     * Finds a resource with a given name.  This method returns null if no
     * resource with this name is found.  The rules for searching resources
     * associated with a given class are implemented by the * defining class
     * loader of the class.
     *
     * <p> This method delegates the call to its class loader, after making
     * these changes to the resource name: if the resource name starts with
     * "/", it is unchanged; otherwise, the package name is prepended to the
     * resource name after converting "." to "/".  If this object was loaded by
     * the bootstrap loader, the call is delegated to
     * <code>ClassLoader.getSystemResource</code>.
     *
     * @param name  name of the desired resource
     * @return      a <code>java.net.URL</code> object.
     * @see         java.lang.ClassLoader
     * @since JDK1.1
     */
    public java.net.URL getResource(String name) {
        name = resolveName(name);
        ClassLoader cl = getClassLoader0();
        if (cl==null) {
            // A system class.
            return ClassLoader.getSystemResource(name);
        }
        return cl.getResource(name);
    }



    /** protection domain returned when the internal domain is null */
    private static java.security.ProtectionDomain allPermDomain;


    /**
     * Returns the <code>ProtectionDomain</code> of this class.  If there is a
     * security manager installed, this method first calls the security
     * manager's <code>checkPermission</code> method with a
     * <code>RuntimePermission("getProtectionDomain")</code> permission to
     * ensure it's ok to get the
     * <code>ProtectionDomain</code>.
     *
     * @return the ProtectionDomain of this class
     *
     * @throws SecurityException
     *        if a security manager exists and its 
     *        <code>checkPermission</code> method doesn't allow 
     *        getting the ProtectionDomain.
     *
     * @see java.security.ProtectionDomain
     * @see SecurityManager#checkPermission
     * @see java.lang.RuntimePermission
     * @since 1.2
     */
    public java.security.ProtectionDomain getProtectionDomain() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(SecurityConstants.GET_PD_PERMISSION);
        }
        java.security.ProtectionDomain pd = getProtectionDomain0();
        if (pd == null) {
            if (allPermDomain == null) {
                java.security.Permissions perms = 
                    new java.security.Permissions();
                perms.add(SecurityConstants.ALL_PERMISSION);
                allPermDomain = 
                    new java.security.ProtectionDomain(null, perms);
            }
            pd = allPermDomain;
        }
        return pd;
    }


    /**
     * Returns the ProtectionDomain of this class.
     */
    private native java.security.ProtectionDomain getProtectionDomain0();


    /*
     * Return the Virtual Machine's Class object for the named
     * primitive type.
     */
    static native Class getPrimitiveClass(String name);


    /*
     * Check if client is allowed to access members.  If access is denied,
     * throw a SecurityException.
     *
     * Be very careful not to change the stack depth of this checkMemberAccess
     * call for security reasons reasons see
     * java.lang.SecurityManager.checkMemberAccess
     *
     * <p> Default policy: allow all clients access with normal Java access
     * control.
     */
    private void checkMemberAccess(int which, ClassLoader ccl) {
        SecurityManager s = System.getSecurityManager();
        if (s != null) {
            s.checkMemberAccess(this, which);
	    ClassLoader cl = getClassLoader0();
            if ((ccl != null) && (ccl != cl) && 
                  ((cl == null) || !cl.isAncestor(ccl))) {
		String name = this.getName();
		int i = name.lastIndexOf('.');
		if (i != -1) {
		    s.checkPackageAccess(name.substring(0, i));
		}
	    }
	}
    }

    /**
     * Add a package name prefix if the name is not absolute Remove leading "/"
     * if name is absolute
     */
    private String resolveName(String name) {
        if (name == null) {
            return name;
        }
        if (!name.startsWith("/")) {
            Class c = this;
            while (c.isArray()) {
                c = c.getComponentType();
            }
            String baseName = c.getName();
            int index = baseName.lastIndexOf('.');
            if (index != -1) {
                name = baseName.substring(0, index).replace('.', '/')
                    +"/"+name;
            }
        } else {
            name = name.substring(1);
        }
        return name;
    }

    /**
     * Reflection support.
     */

    private native Field[] getFields0(int which);
    private native Method[] getMethods0(int which);
    private native Constructor[] getConstructors0(int which);
    private native Field getField0(String name, int which);
    private native Method getMethod0(String name, Class[] parameterTypes,
        int which);
    private native Constructor getConstructor0(Class[] parameterTypes,
        int which) throws NoSuchMethodException;
    private native Class[] getDeclaredClasses0();


    /** use serialVersionUID from JDK 1.1 for interoperability */
    private static final long serialVersionUID = 3206093459760846163L;


    /**
     * Class Class is special cased within the Serialization Stream Protocol. 
     *
     * A Class instance is written intially into an ObjectOutputStream in the 
     * following format:
     * <pre>
     *      <code>TC_CLASS</code> ClassDescriptor
     *      A ClassDescriptor is a special cased serialization of 
     *      a <code>java.io.ObjectStreamClass</code> instance. 
     * </pre>
     * A new handle is generated for the initial time the class descriptor
     * is written into the stream. Future references to the class descriptor
     * are written as references to the initial class descriptor instance.
     *
     * @see java.io.ObjectStreamClass
     */
    private static final ObjectStreamField[] serialPersistentFields = 
        ObjectStreamClass.NO_FIELDS;


    /**
     * Returns the assertion status that would be assigned to this
     * class if it were to be initialized at the time this method is invoked.
     * If this class has had its assertion status set, the most recent
     * setting will be returned; otherwise, if any package default assertion
     * status pertains to this class, the most recent setting for the most
     * specific pertinent package default assertion status is returned;
     * otherwise, if this class is not a system class (i.e., it has a
     * class loader) its class loader's default assertion status is returned;
     * otherwise, the system class default assertion status is returned.
     * <p>
     * Few programmers will have any need for this method; it is provided
     * for the benefit of the JRE itself.  (It allows a class to determine at
     * the time that it is initialized whether assertions should be enabled.)
     * Note that this method is not guaranteed to return the actual
     * assertion status that was (or will be) associated with the specified
     * class when it was (or will be) initialized.
     *
     * @return the desired assertion status of the specified class.
     * @see    java.lang.ClassLoader#setClassAssertionStatus
     * @see    java.lang.ClassLoader#setPackageAssertionStatus
     * @see    java.lang.ClassLoader#setDefaultAssertionStatus
     * @since  1.4
     */
    public boolean desiredAssertionStatus() {
        ClassLoader loader = getClassLoader();
        // If the loader is null this is a system class, so ask the VM
        if (loader == null)
            return desiredAssertionStatus0(this);

        synchronized(loader) {
            // If the classloader has been initialized with
            // the assertion directives, ask it. Otherwise,
            // ask the VM.
            return (loader.classAssertionStatus == null ?
                    desiredAssertionStatus0(this) :
                    loader.desiredAssertionStatus(getName()));
        }
    }

    // Retrieves the desired assertion status of this class from the VM
    private static native boolean desiredAssertionStatus0(Class clazz);

    private native boolean checkInitializedFlag();
    private native boolean checkInitializingFlag(boolean me);
    private native boolean checkErrorFlag();
    private native void setInitializedFlag();
    private native void setInitializingFlag();
    private native void setErrorFlag();
    private native void clearInitializingFlag();
    private native void clearSuperClassesLoadedFlag();

    /* put <this,loader> in the loader cache */
    native void addToLoaderCache(ClassLoader loader);

    /* the following are only useable during loadSuperClasses(). */
    private native int getClassTypeID();
            native boolean superClassesLoaded();
    private native int getSuperClassTypeID();
    private native String getSuperClassName(ClassLoader loader);
    private native int getNumInterfaces();
    private native int getInterfaceTypeID(int index);
    private native String getInterfaceName(int index, ClassLoader loader);
    private native void linkSuperClasses();
    private native void notifyClassLoaded();

    private void runStaticInitializers() throws Throwable {
	CVM.disableRemoteExceptions();
	boolean unmask = !CVM.maskInterrupts();
	try {

	    /* Step 1 - lock the class. */
	    synchronized (this) {

		/* Step 2 - if someone is currenly initializing this class 
		 * and it's not our thread, then wait until the class is
		 * done initializing. */
		while (checkInitializingFlag(false)) {
		    try {
			wait(0);
		    } catch (InterruptedException e) {
			/* This can't happen since async exceptions are 
			 * disabled during static intializers.
			 */
		    }
		}

		/* Step 3 and 4 - return success if this thread is
		 * initializing the class or if the class is already
		 * initialized. */
		if (checkInitializingFlag(true) || checkInitializedFlag()) {
		    return;
		}
		    
		/* Step 5 - fail if class is already in an erroneous state. */
		if (checkErrorFlag()) {
		    throw new NoClassDefFoundError(getName());
		}

		/* Step 6 - mark the class as "initializing" and unlock it. */
		setInitializingFlag();
	    }

	    /* Step 7 - run the superclass static initialzer. */
	    Class superClass = getSuperclass();
	    if (superClass != null && !superClass.checkInitializedFlag()) {
		try {
		    superClass.runStaticInitializers();
		} catch (Throwable e) {
		    synchronized (this) {
			clearInitializingFlag();
			setErrorFlag();
			notifyAll();
		    }
		    throw e;
		}
	    }

	    /* Step 8 - run the static intializer. */
	    Throwable clinitError = null;
	    try {
		CVM.executeClinit(this);
	    } catch (Throwable e) {
		clinitError = e;
	    }
	    /* free up memory used by the <clinit> method */
	    CVM.freeClinit(this);

	    /* Step 9 - no exception thrown. */
	    if (clinitError == null) {
		synchronized (this) {
		    clearInitializingFlag();
		    setInitializedFlag();
		    notifyAll();
		    return;
		}
	    }

	    /* Step 10 - an exception was thrown. If it is not an instance of
	     * Error, then change it to ExceptionInInitializerError. */
	    if (!(clinitError instanceof Error)) {
		try {
		    clinitError = new ExceptionInInitializerError(clinitError);
		} catch (Throwable e) {
		    clinitError = e;
		}
	    }

	    /* Step 11 - error out the class and throw the exception */
	    synchronized (this) {
		clearInitializingFlag();
		setErrorFlag();
		notifyAll();
	    }
	    throw clinitError;
	} finally {
	    if (unmask) {
		CVM.unmaskInterrupts();
	    }
	    CVM.enableRemoteExceptions();
	}
    };


    /*
     * class LoadingList
     *
     * Used to maintain a list of classes whose superclasses are
     * currently being loaded so we can easily check for class
     * circularities. Since the list uses <typeid,loader> pairs,
     * there's no need to keep a separate list for each ClassLoader
     * instance and for the NULL classloader.
     */
    private class LoadingList {
	int typeID;
	ClassLoader loader;
	LoadingList next;
	
	LoadingList(int typeID, ClassLoader loader, LoadingList next) {
	    this.typeID = typeID;
	    this.loader = loader;
	    this.next = next;
	}
    }

    private static LoadingList loadingList;

    private boolean onLoadingList(int typeID) {
        synchronized(LoadingList.class) {
            LoadingList item = loadingList;
            while (item != null) {
	        if (item.typeID == typeID && item.loader == loader) {
                    return true;
                }
                item = item.next;
            }
        }
        return false;
    }

    private void addToLoadingList() {
        int typeID = getClassTypeID();
        if (onLoadingList(typeID))
            throw new ClassCircularityError(getName());
	synchronized(LoadingList.class) {
	    loadingList = new LoadingList(typeID, loader, loadingList);
	}
    }

    private void removeFromLoadingList() {
	synchronized(LoadingList.class) {
	    int typeID = getClassTypeID();
	    LoadingList item = loadingList;
	    LoadingList prevItem = null;
	    while (item != null) {
		if (item.typeID == typeID && item.loader == loader) {
		    if (prevItem == null) {
			loadingList = item.next;
		    } else {
			prevItem.next = item.next;
		    }
		    return;
		}
		prevItem = item;
		item = item.next;
	    }
	}
    }

    /*
     * Load all the superclasses and interfaces in a way that doesn't
     * cause any C recursion, only java recursion.
     */
    void loadSuperClasses() {
        /* Name and typeid of the class to load. The class is
         * either the superclass of the current class, or one
         * of its superinterfaces. */
        String clName = null;  
        int clTypeID = -1;

	try {
	    addToLoadingList(); /* check for class circularities */
	    
	    /* Make sure the superclasses are loaded. */

	    /* Returns NULL if class is already in the loader cache */
	    clName = getSuperClassName(loader);
	    if (clName != null) {
                clTypeID = getSuperClassTypeID();
		if (loader == null) {
		    ClassLoader.loadBootstrapClass(clName);
		} else {
		    Class c = loader.loadClass(clName);
		    /*
		     * It's possible that the class is not in the loaderCache.
		     * This happens when the class is loaded by the parent
		     * of "loader". Class.linkSuperClasses() requires that the
		     * class be in the loaderCache, so we load it here.
		     */
		    if (c.loader != loader) {
			c.addToLoaderCache(loader);
		    }
		}
	    }
	    
	    /* make sure the interfaces are loaded */
	    int numInterfaces = getNumInterfaces();
	    for (int i = 0; i < numInterfaces; i++) {
		/* Returns NULL if class is already in the loader cache */
		clName = getInterfaceName(i, loader);
		if (clName != null) {
                    clTypeID = getInterfaceTypeID(i);
		    if (loader == null) {
			ClassLoader.loadBootstrapClass(clName);
		    } else {
			Class c = loader.loadClass(clName);
			/*  See comment for similar code above. */
			if (c.loader != loader) {
			    c.addToLoaderCache(loader);
			}
		    }
		}
	    }
	    
	    /*
	     * All super classes are now loaded, so we can link this
	     * class to it's super class.  has to be done before
	     * calling ClassLoader.addClass().  Otherwise, if
	     * CVMclassLinkSuperClasses() failed, the ClassLoader
	     * instance would have access to an invalid class. There
	     * is an invariant that any Class instance obtained by
	     * java code must already have its superclasses
	     * loaded. Otherwise the vm can crash.
	     */
	    linkSuperClasses();
	    
	    /* It is now safe to put the class in the loader cache. */
	    addToLoaderCache(loader);

	    /* add the class to its ClassLoader so it won't get gc'd */
	    if (loader != null) {
		boolean success = false;
		try {
		    loader.addClass(this);
		    success = true;
		} finally {
		    if (!success) {
			/* prevent loaderCache lookup */
			setErrorFlag();
			clearSuperClassesLoadedFlag();
		    }
		}
	    }	    
	} catch (ClassNotFoundException e) {
	    if (onLoadingList(clTypeID)) {
                throw new ClassCircularityError(clName);
            } else {
	        throw new NoClassDefFoundError(e.getMessage());
            }
	} finally {
	    removeFromLoadingList();
	}

        notifyClassLoaded();
    }
}
