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

package sun.misc;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import sun.misc.Version;
import java.io.FileInputStream;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.io.IOException;
import java.util.ArrayList;
import java.io.File;

public final class CVM {
    /** WARNING! NO STATIC INITIALIZER IS ALLOWED IN THIS CLASS!

	More precisely, no initial assignment is allowed to either of
	these two variables. */
    private static String mainClassName;
    private static String[] mainArgs;

    private static String savedNativeOptions;

    // The status of command-line argument parsing
    public static final int ARG_PARSE_UNINIT = 0;
    public static final int ARG_PARSE_OK     = 1;
    public static final int ARG_PARSE_ERR    = 2;
    public static final int ARG_PARSE_EXITVM = 3;
    public static final int ARG_PARSE_USAGE  = 4;

    private static int parseStatus; 

    private static void usage(String nativeOptions) {
	// Print usage statement
	System.err.println(
	    "usage: cvm [-fullversion] [-showversion] [-version] [-help] " + 
	    "[-D<property>=<value>] [-XbuildOptions] [-XshowBuildOptions] " +
	    "[-XappName=<value>] " + "[-cp <classpath> | -classpath <classpath>] " + 
	    nativeOptions +
	    "{<main class name> | -jar <jarfile> | -appletviewer <URL>} " + 
            "[<arguments>...]"
	);
    }

    /** Parse command line options handed up from ansiJavaMain through
	JNI_CreateJavaVM. This parses command line options like -D to
	define user-specified properties; it must be called after
	System.initializeSystemClass(). In its current usage it
	receives argv[1..(argc - 1)] in String form from the C
	initialization code, with "-Xcvm" prepended onto the main
	class and its arguments. This allows the building of the
	String array which will later be passed to main() to be done
	in Java. Note that it is not necessary to use this
	functionality. */
    public static int parseCommandLineOptions(String[] args,
						  String nativeOptions,
						  boolean ignoreUnrecognized)
    {
	parseStatus = ARG_PARSE_UNINIT;

	String pathSeparator = System.getProperty("path.separator", ":");
	ArrayList xrunArgs = new ArrayList();

	for (int i = 0; i < args.length; i++) {

	    /* %comment: rt039 */

            /* NOTE: It would be best to move the support for
               -XappName into the PP layer */
	    if (args[i].startsWith("-D") || args[i].startsWith("-XappName")) {
		if (!addUserProperty(args[i].substring(2))) {
		    System.err.println("Error parsing property " + args[i]);
		    usage(nativeOptions);
		    parseStatus = ARG_PARSE_ERR;
		    return parseStatus;
		}
	    } else if (args[i].startsWith("-version")) {
		Version.print(true); // Long version
		parseStatus = ARG_PARSE_EXITVM; // Don't parse any more
		return parseStatus;
	    } else if (args[i].startsWith("-showversion")) {
		Version.print(true); // Long version
		// continue with VM execution
	    } else if (args[i].startsWith("-Xtrace:")) {
		String traceArg = args[i].substring(8);
		int debugFlags = Integer.decode(traceArg).intValue();
		CVM.setDebugFlags(debugFlags);
		// continue with VM execution
	    } else if (args[i].startsWith("-Xrun")) {
		if (!xrunSupported()) {
		    System.err.println("-Xrun not supported");
		    usage(nativeOptions);
		    parseStatus = ARG_PARSE_ERR;
		    return parseStatus;
		}
		xrunArgs.add(args[i]);
		// continue with VM execution
	    } else if (args[i].startsWith("-Xdebug")) {
		if (!xdebugSet()) {
		    System.err.println("-Xdebug not supported, debugging not enabled");
		    usage(nativeOptions);
		    parseStatus = ARG_PARSE_ERR;
		    return parseStatus;
		}
		// continue with VM execution
	    } else if (args[i].startsWith("-XtimeStamping")) {
		TimeStamps.enable();
	    } else if (args[i].startsWith("-Xjit:")) {
		String jitArg = args[i].substring(6);
		if (!JIT.reparseJitOptions(jitArg)) {
		    System.err.println("Error parsing JIT args " + args[i]);
		    usage(nativeOptions);
		    parseStatus = ARG_PARSE_ERR;
		    return parseStatus;
		}
		// continue with VM execution
	    } else if (args[i].startsWith("-Xverify:")) {
		String verifyArg = args[i].substring(9);
		if (!CVM.parseVerifyOptions(verifyArg)) {
		    System.err.println("Error parsing verify args " + args[i]);
		    usage(nativeOptions);
		    parseStatus = ARG_PARSE_ERR;
		    return parseStatus;
		}
		// continue with VM execution
	    } else if (args[i].startsWith("-Xopt:")) {
		String xoptArg = args[i].substring(6);
		if (!CVM.parseXoptOptions(xoptArg)) {
		    System.err.println("Error parsing -Xopt args " + args[i]);
		    usage(nativeOptions);
		    parseStatus = ARG_PARSE_ERR;
		    return parseStatus;
		}
		// continue with VM execution
	    } else if (args[i].startsWith("-Xss")) {
		String xssArg = args[i].substring(4);
		if (!CVM.parseXssOption(xssArg)) {
		    System.err.println("Error parsing -Xss args " + args[i]);
		    usage(nativeOptions);
		    parseStatus = ARG_PARSE_ERR;
		    return parseStatus;
		}
		// continue with VM execution
	    } else if (args[i].startsWith("-Xgc:")) {
		String xgcArg = args[i].substring(5);
		if (!CVM.parseXgcOptions(xgcArg)) {
		    System.err.println("Error parsing -Xgc args " + args[i]);
		    usage(nativeOptions);
		    parseStatus = ARG_PARSE_ERR;
		    return parseStatus;
		}
		// continue with VM execution
	    } else if (args[i].startsWith("-fullversion")) {
		Version.print(false); // Short version
		parseStatus = ARG_PARSE_EXITVM; // Don't parse any more
		return parseStatus;
	    } else if (args[i].startsWith("-ea") ||
		       args[i].startsWith("-enableassertions") ||
		       args[i].startsWith("-da") ||
		       args[i].startsWith("-disableassertions") ||
	               args[i].startsWith("-esa") ||
		       args[i].startsWith("-enablesystemassertions") ||
		       args[i].startsWith("-dsa") ||
		       args[i].startsWith("-disablesystemassertions")) {
		if (!CVM.parseAssertionOptions(args[i])) {
		    System.err.println("Error parsing assertion args " + args[i]);
		    usage(nativeOptions);
		    parseStatus = ARG_PARSE_ERR;
		    return parseStatus;
		}
		// continue with VM execution
	    } else if (args[i].startsWith("-XbuildOptions")) {
		Version.printBuildOptions();
		parseStatus = ARG_PARSE_EXITVM; // Don't parse any more
		return parseStatus;
	    } else if (args[i].startsWith("-XshowBuildOptions")) {
		Version.printBuildOptions();
		// continue with VM execution
	    } else if (args[i].startsWith("-Xcvm") 
                       || args[i].startsWith("-Xjar")
                       || args[i].startsWith("-appletviewer")) {

		/* TODO: It would be best to move "-appletviewer" to
                   the PP layer */

                if (args[i].startsWith("-Xjar")) {
                  try {
                    // get main class name from manifest
                    JarFile jarFile = new JarFile(args[i].substring(6));
                    Manifest man = jarFile.getManifest();
                    Attributes attr;
		    mainClassName = null;
		    if (man != null){
			attr = man.getMainAttributes();
			mainClassName = attr.getValue("Main-Class");
		    }
                    if (mainClassName == null) {
                      System.err.println("-jar: Could not find Main-Class manifest attribute");
		      usage(nativeOptions);
		      parseStatus = ARG_PARSE_ERR;
		      return parseStatus;
                    }
                  } catch (IOException e) {
	              e.printStackTrace();
                      parseStatus = ARG_PARSE_ERR;
		      return parseStatus;
	          }             

                } else if (args[i].startsWith("-appletviewer")){
                  mainClassName = ("sun.applet.AppletViewer");
                } else {
		// We have to assume that everything else in the
		// command line options is the main class plus its
		// arguments.
                  mainClassName = args[i].substring(5);
                }


		if (mainClassName.startsWith("-") ||
		    mainClassName.length() == 0) 
		{
		    System.err.println("Main class name \"" +
				       mainClassName + "\" is not valid");
		    usage(nativeOptions);
		    parseStatus = ARG_PARSE_ERR;
		    return parseStatus;
		}
		mainClassName = mainClassName.replace('/', '.');
		
		int numMainArgs = args.length - i - 1;
		mainArgs = new String[numMainArgs];
		for (int j = 0; j < numMainArgs; j++) {
		    String arg = args[i + j + 1];
		    if (!arg.startsWith("-Xcvm")) {
			throw new InternalError(
		            "Illegal use of -Xcvm internal " +
			    "command line options"
			);
		    }
		    mainArgs[j] = arg.substring(5);
		}
		break;
// NOTE: do we really want to check all possible options here, or
// should the caller have filtered out the options that it understood?
	    } else {
		if (args[i].startsWith("-")) {
		    if (args[i].equals("-help")) {
			usage(nativeOptions);
			parseStatus = ARG_PARSE_USAGE; 
			return parseStatus;
		    }
		    if (!ignoreUnrecognized && 
                        !args[i].equals("-jar") &&
                        !args[i].equals("-cp") && 
                        !args[i].equals("-classpath") && 
                        !args[i].startsWith("-Xcp") &&
                        !args[i].startsWith("-Xms") &&
                        !args[i].startsWith("-Xmx") &&
                        !args[i].startsWith("-Xserver") &&
			!args[i].startsWith("-Xbootclasspath=") &&
			!args[i].startsWith("-Xbootclasspath:") &&
			!args[i].startsWith("-Xbootclasspath/a=") &&
			!args[i].startsWith("-Xbootclasspath/a:")) {
			System.err.println("Unrecognized option " + args[i]);
			usage(nativeOptions);
			parseStatus = ARG_PARSE_ERR;
			return parseStatus;
		    }
		} else if (!args[i].startsWith("_timeStamp")) {
		    // NOTE: I don't think this code path should be
		    // encountered anymore since we have -Xcvm
		    System.err.println("Unrecognized option " + args[i]);
		    usage(nativeOptions);
		    parseStatus = ARG_PARSE_ERR;
		    return parseStatus;
		}
	    }
	}

	savedNativeOptions = nativeOptions;

	//
	// Handle Xrun options
	//
	if (xrunArgs.size() > 0) {
	    if (!xrunInitialize(xrunArgs.size())) {
		return ARG_PARSE_ERR;
	    }
	    
	    Object[] xrunOpts = xrunArgs.toArray();
	    for (int j = 0; j < xrunOpts.length; j++) {
		if (!xrunProcess((String)xrunOpts[j])) {
		    return ARG_PARSE_ERR;
		}
	    }
	}
	

	parseStatus = ARG_PARSE_OK;
	return parseStatus;
    }

    private static String[] initializePath(String propName) {
	String ldpath = System.getProperty(propName, "");
	String ps = File.pathSeparator;
	int ldlen = ldpath.length();
	int i, j, n;
	// Count the separators in the path
	i = ldpath.indexOf(ps);
	n = 0;
	while (i >= 0) {
	    n++;
	    i = ldpath.indexOf(ps, i + 1);
	}

	// allocate the array of paths - n :'s = n + 1 path elements
	String[] paths = new String[n + 1];

	// Fill the array with paths from the ldpath
	n = i = 0;
	j = ldpath.indexOf(ps);
	while (j >= 0) {
	    if (j - i > 0) {
	        paths[n++] = ldpath.substring(i, j);
	    } else if (j - i == 0) {
	        paths[n++] = ".";
	    }
	    i = j + 1;
	    j = ldpath.indexOf(ps, i);
	}
	paths[n] = ldpath.substring(i, ldlen);
	return paths;
    }

    private static String[] usrPaths;
    private static String[] sysPaths;
    private static String[] builtinPaths;
    
    /** Helper function for parseCommandLineOptions(), above */
    private static boolean addUserProperty(String property) {
	// Cut it at the '=' into key, value
	int equalsIdx = property.indexOf('=');
	String key;
	String value;
	if (equalsIdx == -1) {
	    key = property;
	    value = "";
	} else {
	    key = property.substring(0, equalsIdx);
	    value = property.substring(equalsIdx + 1, property.length());
	}

//
//          In case appending to old value is necessary
//
//  	    String pathSeparator = System.getProperty("path.separator", ":");
//  	    String oldval = System.getProperty(key);
//  	    if ((oldval != null) && !oldval.equals("")) {
//  		String newval = oldval + pathSeparator + value;
//  		value = newval;
//  	    }
//
	
	System.setProperty(key, value);
	if (key.equals("java.library.path")) {
	    //System.err.println("INVALIDATING java.library.path setting");
	    usrPaths = null;
	} else if (key.equals("sun.boot.library.path")) {
	    //System.err.println("INVALIDATING sun.boot.library.path setting");
	    sysPaths = null;
	}
	    
	return true;
    }

    private static void printPath(String name, String[] path) 
    {
	System.err.print(name+" search path: ");
	for (int k = 0; k < path.length; k++) {
	    System.err.print("["+path[k]+"] ");
	}
	System.err.println();
    }
    
	
    public static String[] getUserLibrarySearchPaths() {
	if (usrPaths == null) {
	    usrPaths = initializePath("java.library.path");
	    // printPath("java.library.path", usrPaths);
	}
	return usrPaths;
    }
    
    public static String[] getSystemLibrarySearchPaths() {
	if (sysPaths == null) {
	    sysPaths = initializePath("sun.boot.library.path");
	    // printPath("sun.boot.library.path", sysPaths);
	}
	return sysPaths;
    }
    
    public static String[] getBuiltinLibrarySearchPaths() {
	if (builtinPaths == null) {
	    builtinPaths = initializePath("java.library.builtins");
	    // printPath("java.library.builtins", builtinPaths);
	}
	return builtinPaths;
    }
    
    /* This is called by ansi_java_md.c using the JNI after
       parseCommandLineOptions is called. Note that calling this is
       optional -- the user's code may instead parse argv to find the
       main class and its arguments. This returns the VM-internal,
       "slashified" class name, because this is a VM-internal
       method. */
    public static int getParseStatus() {
	return parseStatus;
    }

    public static String getMainClassName() {
        return mainClassName.replace('.', '/');
    }
    
    /* This is called by ansi_java_md.c using the JNI after
       parseCommandLineOptions is called. Note that calling this is
       optional -- the user's code may instead parse argv to find the
       main class and its arguments. */
    public static String[] getMainArguments() {
	return mainArgs;
    }

    /* Forget what we parsed */
    static void resetMain() {
	mainClassName = null;
	mainArgs = null;
    }
    
    static void runMain() throws Throwable {
	if (parseStatus != ARG_PARSE_OK) {
	    return;
	}

	if (mainClassName == null) {
	    System.err.println("Main class name missing.");
	    usage(savedNativeOptions);
	    return;
	}

	{
	    ClassLoader sys = ClassLoader.getSystemClassLoader();
	    Class mainClass = sys.loadClass(mainClassName);
	    Class [] args = {mainArgs.getClass()};
	    Method mainMethod = mainClass.getMethod("main", args);
	    mainMethod.setAccessible(true);
	    Object [] args2 = {mainArgs};
	    try {
		mainMethod.invoke(null, args2);
	    } catch (InvocationTargetException i) {
		throw i.getTargetException();
	    }
	}
    }

    /* 
     * Debug flags: The debug build of CVM has many debugging features that
     * can be enabled or disabled at runtime. There is a separate
     * flag for each feature. Most of them are for enabling or disabling
     * debugging trace statements.
     */

    public static final int DEBUGFLAG_TRACE_OPCODE     	= 0x00000001;
    public static final int DEBUGFLAG_TRACE_METHOD     	= 0x00000002;
    public static final int DEBUGFLAG_TRACE_STATUS     	= 0x00000004;
    public static final int DEBUGFLAG_TRACE_FASTLOCK    = 0x00000008;
    public static final int DEBUGFLAG_TRACE_DETLOCK    	= 0x00000010;
    public static final int DEBUGFLAG_TRACE_MUTEX      	= 0x00000020;
    public static final int DEBUGFLAG_TRACE_CS		= 0x00000040;
    public static final int DEBUGFLAG_TRACE_GCSTARTSTOP = 0x00000080;
    public static final int DEBUGFLAG_TRACE_GCSCAN     	= 0x00000100;
    public static final int DEBUGFLAG_TRACE_GCSCANOBJ	= 0x00000200;
    public static final int DEBUGFLAG_TRACE_GCALLOC    	= 0x00000400;
    public static final int DEBUGFLAG_TRACE_GCCOLLECT	= 0x00000800;
    public static final int DEBUGFLAG_TRACE_GCSAFETY	= 0x00001000;
    public static final int DEBUGFLAG_TRACE_CLINIT     	= 0x00002000;
    public static final int DEBUGFLAG_TRACE_EXCEPTIONS	= 0x00004000;
    public static final int DEBUGFLAG_TRACE_MISC       	= 0x00008000;
    public static final int DEBUGFLAG_TRACE_BARRIERS	= 0x00010000;
    public static final int DEBUGFLAG_TRACE_STACKMAPS	= 0x00020000;
    public static final int DEBUGFLAG_TRACE_CLASSLOADING= 0x00040000;
    public static final int DEBUGFLAG_TRACE_CLASSLOOKUP = 0x00080000;
    public static final int DEBUGFLAG_TRACE_TYPEID      = 0x00100000;
    public static final int DEBUGFLAG_TRACE_VERIFIER    = 0x00200000;
    public static final int DEBUGFLAG_TRACE_WEAKREFS    = 0x00400000;
    public static final int DEBUGFLAG_TRACE_CLASSUNLOAD = 0x00800000;
    public static final int DEBUGFLAG_TRACE_CLASSLINK   = 0x01000000;
    public static final int DEBUGFLAG_TRACE_LVM         = 0x02000000;

    /*
     * Methods for checking, setting, and clearing the state of debug
     * flags.  All of the following methods return the previous state of 
     * the flags.
     *
     * You can pass in more than one flag at a time to any of the methods.
     */
    public native static int checkDebugFlags(int flags);
    public native static int setDebugFlags(int flags);
    public native static int clearDebugFlags(int flags);
    public native static int restoreDebugFlags(int flags, int oldvalue);

    /* 
     * Debug JIT flags: If built with JIT tracing enabled, CVM has
     * JIT debugging trace features that can be enabled or disabled at
     * runtime. There is a separate flag for each feature.
     */
    public static final int DEBUGFLAG_TRACE_JITSTATUS   = 0x00000001;
    public static final int DEBUGFLAG_TRACE_JITBCTOIR   = 0x00000002;
    public static final int DEBUGFLAG_TRACE_JITCODEGEN  = 0x00000004;
    public static final int DEBUGFLAG_TRACE_JITSTATS    = 0x00000008;
    public static final int DEBUGFLAG_TRACE_JITIROPT    = 0x00000010;
    public static final int DEBUGFLAG_TRACE_JITINLINING = 0x00000020;
    public static final int DEBUGFLAG_TRACE_JITOSR      = 0x00000040;
    public static final int DEBUGFLAG_TRACE_JITREGLOCALS= 0x00000080;

    /*
     * Methods for checking, setting, and clearing the state of debug
     * JIT flags.  All of the following methods return the previous state of
     * the flags.
     *
     * You can pass in more than one flag at a time to any of the methods.
     */
    public native static int checkDebugJITFlags(int flags);
    public native static int setDebugJITFlags(int flags);
    public native static int clearDebugJITFlags(int flags);
    public native static int restoreDebugJITFlags(int flags, int oldvalue);

    // NOTE
    // Some of these public native methods are quite dangerous.
    // We need to make sure that applets cannot call them.

    /* Type specific array copiers: */
    public native static void copyBooleanArray(boolean[] src, int src_position,
                                               boolean[] dst, int dst_position,
                                               int length);
    public native static void copyByteArray(byte[] src, int src_position,
                                            byte[] dst, int dst_position,
                                            int length);
    public native static void copyCharArray(char[] src, int src_position,
                                            char[] dst, int dst_position,
                                            int length);
    public native static void copyShortArray(short[] src, int src_position,
                                             short[] dst, int dst_position,
                                             int length);
    public native static void copyIntArray(int[] src, int src_position,
                                           int[] dst, int dst_position,
                                           int length);
    public native static void copyFloatArray(float[] src, int src_position,
                                             float[] dst, int dst_position,
                                             int length);
    public native static void copyLongArray(long[] src, int src_position,
                                            long[] dst, int dst_position,
                                            int length);
    public native static void copyDoubleArray(double[] src, int src_position,
                                              double[] dst, int dst_position,
                                              int length);

    /* copyObjectArray() copies array elements from one array to another.
       Unlike System.arraycopy(), this method can only copy elements for
       non-primitive arrays with some restrictions.  The restrictions are that
       the src and dst array must be of the same type, or the dst array must
       be of type java.lang.Object[] (not just a compatible sub-type).  The
       caller is responsible for doing the appropriate null checks, bounds
       checks, array element type assignment checks if necessary, and ensure
       that the passed in arguments do violate any of these checks and
       restrictions.  If the condition of these checks and restrictions are
       not taken cared of by the caller, copyObjectArray() can fail in
       unpredictable ways.
    */
    public native static void copyObjectArray(Object[] src, int src_position,
                                              Object[] dst, int dst_position,
                                              int length);
    public static void copyArray(Object[] src, int src_position,
                                 Object[] dst, int dst_position,
                                 int length) {
        System.arraycopy(src, src_position, dst, dst_position, length);
    }

    /*
     * Used by Class.runStaticInitializers() to execute the <linit> method
     * of the specified class.
     */
    public native static void executeClinit(Class c);

    /*
     * Use by Class.runStaticInitializers() to free up the code for the 
     * specified class' <clinit> method.
     */
    public native static void freeClinit(Class c);

    /*
     * Used by Launcher.defineClassPrivate() to execute the 
     * Class.loadSuperClasses method in a way that avoids C recursion.
     */
    native static void executeLoadSuperClasses(Class c);

    /*
     * Manage remote exceptions 
     */
    public native static void disableRemoteExceptions();
    public native static void enableRemoteExceptions();
    // Should be invoked through Thread.stop() that ensures target's ee is alive
    public native static void throwRemoteException(Thread target, Throwable t);

    /*
     * Manage interrupts
     */
    // Returns true if interrupts were already masked.  No
    // internal nesting count is maintained.
    public native static boolean maskInterrupts();
    // Should only be called if maskInterrupts() returned false
    public native static void unmaskInterrupts();

    /*
     * Allows system methods to bypass compiler checks and throw
     * exceptions.  Useful for rewriting native methods in Java.
     */
    public native static Error throwLocalException(Throwable t);

    /*
     * Mark current context as artificial
     */
    public native static void setContextArtificial();


    /* CLDC/MIDP support */
    /*
     * Used to find out if the classloader of the caller's caller
     * is sun.misc.MIDletClassLoader or 
     * sun.misc.MIDPImplementationClassLoader.
     */
    public static native boolean callerCLIsMIDCLs();

    // %begin lvm
    /*
     * Used to suppress initialization of Reference handler 
     * (java.lang.ref.Reference) in a child Logical VM.
     */
    public native static boolean inMainLVM();
    // %end lvm

    /*
     * Some heap dumping utilties.
     */
    public native static void gcDumpHeapSimple();
    public native static void gcDumpHeapVerbose();
    public native static void gcDumpHeapStats();

    public native static void trace(int i);

    public native static void setDebugEvents(boolean on);
    public native static void postThreadExit();

    /* Inflates an object's monitor and mark it sticky so it's never freed. */
    public native static boolean objectInflatePermanently(Object obj);

    /* enable/disable compilations by current thread */
    public static native void setThreadNoCompilationsFlag(boolean enable);
    /** Returns the class of the method <code>realFramesToSkip</code>
        frames up the stack (zero-based), ignoring frames associated
        with java.lang.reflect.Method.invoke() and its implementation.
        The first frame is that associated with this method, so
        <code>getCallerClass(0)</code> returns the Class object for
        sun.reflect.Reflection. Frames associated with
        java.lang.reflect.Method.invoke() and its implementation are
        completely ignored and do not count toward the number of "real"
        frames skipped. */
    public native static Class getCallerClass(int realFramesToSkip);

    //
    // True if JIT supported, false otherwise
    //
    public native static boolean isCompilerSupported();

    // Request a dump of the profiling data collected by the compiler if
    // available:
    public native static void dumpCompilerProfileData();
    //
    // Dump various stats
    //
    public native static void dumpStats();

    //
    // Mark the current location of the code buffer. Below this mark,
    // methods are supposed to be persistent.
    //
    public native static void markCodeBuffer();

    //
    // Re-parse the -Xverify option from the Java side
    //
    public native static boolean parseVerifyOptions(String verifyArg);

    //
    // Re-parse the -Xopt options from the Java side
    //
    public native static boolean parseXoptOptions(String xoptArg);

    //
    // Re-parse the -Xgc options from the Java side
    //
    public native static boolean parseXgcOptions(String xgcArg);

    //
    // Re-parse the -Xss options from the Java side
    //
    public native static boolean parseXssOption(String xssArg);

    //
    // Parse assertion options
    //
    public native static boolean parseAssertionOptions(String xgcArg);

    //
    // Is -Xrun supported?
    //
    public native static boolean xrunSupported();

    //
    // Initialize Xrun processing
    //
    public native static boolean xrunInitialize(int numOptions);

    //
    // Process Xrun option
    //
    public native static boolean xrunProcess(String xrunArg);

    //
    // Enable debugging
    //
    public native static boolean xdebugSet();
}
