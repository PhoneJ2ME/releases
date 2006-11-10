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

/*
 * Warning :
 * Two versions of this file exist in this workspace.
 * One for Personal Basis, and one for Personal Profile. 
 * Don't edit the wrong one !!!
 */

package java.beans;

import java.awt.*;
import java.io.*;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.lang.reflect.Array;

/**
 * This class provides some general purpose beans control methods.
 */

public class Beans {
    /**
     * Instantiate a bean.
     * <p>
     * The bean is created based on a name relative to a class-loader.
     * This name should be a dot-separated name such as "a.b.c".
     * <p>
     * In Beans 1.0 the given name can indicate either a serialized object
     * or a class.  Other mechanisms may be added in the future.  In
     * beans 1.0 we first try to treat the beanName as a serialized object
     * name then as a class name.
     * <p>
     * When using the beanName as a serialized object name we convert the
     * given beanName to a resource pathname and add a trailing ".ser" suffix.
     * We then try to load a serialized object from that resource.
     * <p>
     * For example, given a beanName of "x.y", Beans.instantiate would first
     * try to read a serialized object from the resource "x/y.ser" and if
     * that failed it would try to load the class "x.y" and create an
     * instance of that class.
     * <p>
     * If the bean is a subtype of java.applet.Applet, then it is given
     * some special initialization.  First, it is supplied with a default
     * AppletStub and AppletContext.  Second, if it was instantiated from
     * a classname the applet's "init" method is called.  (If the bean was
     * deserialized this step is skipped.)
     * <p>
     * Note that for beans which are applets, it is the caller's responsiblity
     * to call "start" on the applet.  For correct behaviour, this should be done
     * after the applet has been added into a visible AWT container.
     * <p>
     * Note that applets created via beans.instantiate run in a slightly
     * different environment than applets running inside browsers.  In
     * particular, bean applets have no access to "parameters", so they may
     * wish to provide property get/set methods to set parameter values.  We
     * advise bean-applet developers to test their bean-applets against both
     * the SDK appletviewer (for a reference browser environment) and the
     * BDK BeanBox (for a reference bean container).
     *
     * @param     classLoader the class-loader from which we should create
     * 		              the bean.  If this is null, then the system
     *                        class-loader is used.
     * @param     beanName    the name of the bean within the class-loader.
     *   	              For example "sun.beanbox.foobah"
     *
     * @exception java.lang.ClassNotFoundException if the class of a serialized
     *              object could not be found.
     * @exception java.io.IOException if an I/O error occurs.
     */

    public static Object instantiate(ClassLoader cls, String beanName)
        throws java.io.IOException, ClassNotFoundException {
        java.io.InputStream ins;
        java.io.ObjectInputStream oins = null;
        Object result = null;
        boolean serialized = false;
        java.io.IOException serex = null;
        // If the given classloader is null, we check if an
        // system classloader is available and (if so)
        // use that instead.
        // Note that calls on the system class loader will
        // look in the bootstrap class loader first.
        if (cls == null) {
            try {
                cls = ClassLoader.getSystemClassLoader();
            } catch (SecurityException ex) {// We're not allowed to access the system class loader.
                // Drop through.
            }
        }
        // Try to find a serialized object with this name
        final String serName = beanName.replace('.', '/').concat(".ser");
        final ClassLoader loader = cls;
        ins = (InputStream) java.security.AccessController.doPrivileged
                (new java.security.PrivilegedAction() {
                        public Object run() {
                            if (loader == null)
                                return ClassLoader.getSystemResourceAsStream(serName);
                            else
                                return loader.getResourceAsStream(serName);
                        }
                    }
                );
        if (ins != null) {
            try {
                if (cls == null) {
                    oins = new ObjectInputStream(ins);
                } else {
                    oins = new ObjectInputStreamWithLoader(ins, cls);
                }
                result = oins.readObject();
                serialized = true;
                oins.close();
            } catch (java.io.IOException ex) {
                ins.close();
                // Drop through and try opening the class.  But remember
                // the exception in case we can't find the class either.
                serex = ex;
            } catch (ClassNotFoundException ex) {
                ins.close();
                throw ex;
            }
        }
        if (result == null) {
            // No serialized object, try just instantiating the class
            Class cl;
            try {
                if (cls == null) {
                    cl = Class.forName(beanName);
                } else {
                    cl = cls.loadClass(beanName);
                }
            } catch (ClassNotFoundException ex) {
                // There is no appropriate class.  If we earlier tried to
                // deserialize an object and got an IO exception, throw that,
                // otherwise rethrow the ClassNotFoundException.
                if (serex != null) {
                    throw serex;
                }
                throw ex;
            }
            /*
             * Try to instantiate the class.
             */

            try {
                result = cl.newInstance();
            } catch (Exception ex) {
                // We have to remap the exception to one in our signature.
                // But we pass extra information in the detail message.
                throw new ClassNotFoundException("" + cl + " : " + ex);
            }
        }
        if (result != null) {// Ok, if the result is an applet initialize it.
        }
        return result;
    }

    /**
     * From a given bean, obtain an object representing a specified
     * type view of that source object.
     * <p>
     * The result may be the same object or a different object.  If
     * the requested target view isn't available then the given
     * bean is returned.
     * <p>
     * This method is provided in Beans 1.0 as a hook to allow the
     * addition of more flexible bean behaviour in the future.
     *
     * @param obj  Object from which we want to obtain a view.
     * @param targetType  The type of view we'd like to get.
     *
     */

    /*    public static Object getInstanceOf(Object bean, Class targetType) {
     return bean;
     }
     */
    
    /**
     * Check if a bean can be viewed as a given target type.
     * The result will be true if the Beans.getInstanceof method
     * can be used on the given bean to obtain an object that
     * represents the specified targetType type view.
     *
     * @param bean  Bean from which we want to obtain a view.
     * @param targetType  The type of view we'd like to get.
     * @return "true" if the given bean supports the given targetType.
     *
     */

    /*    public static boolean isInstanceOf(Object bean, Class targetType) {
     return Introspector.isSubclass(bean.getClass(), targetType);
     }
     */

    /**
     * Test if we are in design-mode.
     *
     * @return  True if we are running in an application construction
     *		environment.
     *
     * @see java.beans.DesignMode
     */
    public static boolean isDesignTime() {
        return designTime;
    }

    /**
     * Determines whether beans can assume a GUI is available.
     *
     * @return  True if we are running in an environment where beans
     *	   can assume that an interactive GUI is available, so they
     *	   can pop up dialog boxes, etc.  This will normally return
     *	   true in a windowing environment, and will normally return
     *	   false in a server environment or if an application is
     *	   running as part of a batch job.
     *
     * @see java.beans.Visibility
     *
     */
    public static boolean isGuiAvailable() {
        return guiAvailable;
    }
    /**
     * Used to indicate whether of not we are running in an application
     * builder environment.  
     * 
     * <p>Note that this method is security checked
     * and is not available to (for example) untrusted applets.
     * More specifically, if there is a security manager, 
     * its <code>checkPropertiesAccess</code> 
     * method is called. This could result in a SecurityException.
     *
     * @param isDesignTime  True if we're in an application builder tool.
     * @exception  SecurityException  if a security manager exists and its  
     *             <code>checkPropertiesAccess</code> method doesn't allow setting
     *              of system properties.
     * @see SecurityManager#checkPropertiesAccess
     */

    /*    public static void setDesignTime(boolean isDesignTime)
     throws SecurityException {
     SecurityManager sm = System.getSecurityManager();
     if (sm != null) {
     sm.checkPropertiesAccess();
     }
     designTime = isDesignTime;
     }
     */
    
    /**
     * Used to indicate whether of not we are running in an environment
     * where GUI interaction is available.  
     * 
     * <p>Note that this method is security checked
     * and is not available to (for example) untrusted applets.
     * More specifically, if there is a security manager, 
     * its <code>checkPropertiesAccess</code> 
     * method is called. This could result in a SecurityException.
     *
     * @param isGuiAvailable  True if GUI interaction is available.
     * @exception  SecurityException  if a security manager exists and its  
     *             <code>checkPropertiesAccess</code> method doesn't allow setting
     *              of system properties.
     * @see SecurityManager#checkPropertiesAccess
     */

    /*    public static void setGuiAvailable(boolean isGuiAvailable)
     throws SecurityException {
     SecurityManager sm = System.getSecurityManager();
     if (sm != null) {
     sm.checkPropertiesAccess();
     }
     guiAvailable = isGuiAvailable;
     }
     */

    private static boolean designTime;
    private static boolean guiAvailable = true;
}

/**
 * This subclass of ObjectInputStream delegates loading of classes to
 * an existing ClassLoader.
 */

class ObjectInputStreamWithLoader extends ObjectInputStream {
    private ClassLoader loader;
    /**
     * Loader must be non-null;
     */

    public ObjectInputStreamWithLoader(InputStream in, ClassLoader loader)
        throws IOException, StreamCorruptedException {
        super(in);
        if (loader == null) {
            throw new IllegalArgumentException("Illegal null argument to ObjectInputStreamWithLoader");
        }
        this.loader = loader;
    }

    /**
     * Make a primitive array class
     */

    private Class primitiveType(char type) {
        switch (type) {
        case 'B':
            return byte.class;

        case 'C':
            return char.class;

        case 'D':
            return double.class;

        case 'F':
            return float.class;

        case 'I':
            return int.class;

        case 'J':
            return long.class;

        case 'S':
            return short.class;

        case 'Z':
            return boolean.class;

        default:
            return null;
        }
    }

    /**
     * Use the given ClassLoader rather than using the system class
     */
    protected Class resolveClass(ObjectStreamClass classDesc)
        throws IOException, ClassNotFoundException {
        String cname = classDesc.getName();
        if (cname.startsWith("[")) {
            // An array
            Class component;		// component class
            int dcount;			// dimension
            for (dcount = 1; cname.charAt(dcount) == '['; dcount++);
            if (cname.charAt(dcount) == 'L') {
                component = loader.loadClass(cname.substring(dcount + 1,
                                cname.length() - 1));
            } else {
                if (cname.length() != dcount + 1) {
                    throw new ClassNotFoundException(cname);// malformed
                }
                component = primitiveType(cname.charAt(dcount));
            }
            int dim[] = new int[dcount];
            for (int i = 0; i < dcount; i++) {
                dim[i] = 1;
            }
            return Array.newInstance(component, dim).getClass();
        } else {
            return loader.loadClass(cname);
        }
    }
}
