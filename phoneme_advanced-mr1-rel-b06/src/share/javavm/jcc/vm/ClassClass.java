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

package vm;
/*
 * VM-specific internal representation of
 * a class. Target-machine independent.
 * There is a references from each instance of components.ClassInfo to 
 * one of these, and a reference back as well.
 *
 * See also JDKVM for VM-specific info not associated directly with a class.
 */
import components.*;
import util.*;
import consts.Const;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Hashtable;
import java.util.StringTokenizer;

public abstract class
ClassClass {
    public ClassInfo		ci;
    public InterfaceMethodTable	inf;
    public boolean		impureConstants = false;
    public int			nGCStatics = 0;
    public boolean              hasStaticInitializer = false;

    public static void setTypes(){
	classFactory.setTypes();
    }

    /*
     * Make the class list into a vector
     * ordered s.t. superclasses precede their
     * subclasses.
     */
    private static ClassClass[]cvector;
    private static int         cindex;
    private static VMClassFactory classFactory;

    // this is the recursive part
    private static void insertClassElement( ClassInfo e ){
	if ( e.vmClass != null ) return; // already in place.
	ClassInfo sup = e.superClassInfo;
	// make sure our super precedes us.
	if ( (sup != null) && (sup.vmClass == null) ) insertClassElement( sup );
	ClassClass newVmClass = classFactory.newVMClass( e );
	cvector[ cindex++ ] = newVmClass;
	//
	// If the superclass of class C has a <clinit> method, C must
	// be marked as having a static initializer too.
	//
	if (!newVmClass.hasStaticInitializer &&
	    (sup != null) && sup.vmClass.hasStaticInitializer) {
	    newVmClass.hasStaticInitializer = true;
	}
    }

    // this is the entry point for vector building.
    public static ClassClass[] getClassVector( VMClassFactory ftry ){
	if ( cvector != null ) return cvector; // just once, at most.
	classFactory = ftry;
	cvector = new ClassClass[ ClassTable.size() ];
	cindex  = 0;
	Enumeration classlist = ClassTable.elements();
	while( classlist.hasMoreElements() ){
	    ClassInfo e =  (ClassInfo)classlist.nextElement();
	    if (e.vmClass == null) insertClassElement( e );
	}
	return cvector;
    }

    public static void appendClassElement( ClassInfo c ){
	// foo. Have a cvector in place, must now 
	// add a new entry at the end. "c" is it.
	if ( cvector == null ) return; // ...never mind
	ClassClass[] oldCvector = cvector;
	cvector = new ClassClass[ cindex+1 ];
	System.arraycopy( oldCvector, 0, cvector,0, cindex );
	cvector[ cindex ] = classFactory.newVMClass( c );
    }

    public static boolean hasClassVector(){
	return cvector != null;
    }

    /**
     * Size of an instance in WORDS.
     */
    public int instanceSize(){
	// fieldtable must always be in order.
	FieldInfo ft[] = ci.fieldtable;
	if ( ft == null || ft.length == 0 ) return 0;
	FieldInfo lastField = ft[ft.length-1];
	return (lastField.instanceOffset+lastField.nSlots);
    }

    public boolean isInterface() {
	return (ci.access&Const.ACC_INTERFACE) != 0;
    }

    public boolean hasMethodtable(){
	return ((!isInterface()) && (ci.methodtable != null));
    }

    public boolean isArrayClass(){
	return (ci instanceof ArrayClassInfo);
    }

    public boolean isPrimitiveClass() { 
	return (ci instanceof PrimitiveClassInfo);
    }

    public int nmethods(){
	return (ci.methods==null) ? 0 : ci.methods.length;
    }

    public int nfields(){
	return (ci.fields==null)  ? 0 : ci.fields.length;
    }

    /*
     * A concrete ClassClass needs to declare what needs to be done
     * to a constant pool containing unresolved references.
     * Some need to add UTF string references. Some may not.
     * Returns: TRUE for pure (nothing symbolic left to resolve)
     *	        FALSE otherwise.
     */
    abstract public boolean 
    adjustSymbolicConstants(UnresolvedReferenceList missingObjects);

    public static boolean 
    isPartiallyResolved( ConstantObject[] consts ){
        if ( consts == null ) return false; // no const!
        int nconst = consts.length;
        if ( nconst == 0 ) return false; // no const!

        // first see if we have anything that needs our attention.
        int nsymbolic = 0;
        for( int i = 1; i < nconst; i += consts[i].nSlots ){
            ConstantObject o = consts[i];
            if (!o.isResolved()) {
                return true;
	    }
        }
	return false;
    }

    /*
     * The concrete ClassClass can define this to return an inlining for
     * all its methods
     */
    abstract public void getInlining();

}

