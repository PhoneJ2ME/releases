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

package com.sun.tools.jdwpgen;

import java.util.*;
import java.io.*;

class Main {

    static String specSource;
    static Map nameMap = new HashMap();
    static boolean genDebug = true;

    static void usage() {
        System.err.println();
        System.err.println(
            "java Main <spec_input> -jvmdi <jvmdi.h> <options>...");
        System.err.println();
        System.err.println("Options:");
        System.err.println("-doc <doc_output>");
        System.err.println("-jdi <java_output>");
        System.err.println("-include <include_file_output>");
        System.exit(1);
    }

    public static void main(String args[]) throws IOException {
        Reader reader = null;
        Reader jvmdiReader = null;
        PrintWriter doc = null;
        PrintWriter jdi = null;
        PrintWriter include = null;

	// Parse arguments
	for (int i = 0 ; i < args.length ; ++i) {
	    String arg = args[i];
	    if (arg.startsWith("-")) {
                String fn = args[++i];
                if (arg.equals("-doc")) {
                    doc = new PrintWriter(new FileWriter(fn));
                } else if (arg.equals("-jdi")) {
                    jdi = new PrintWriter(new FileWriter(fn));
                } else if (arg.equals("-include")) {
                    include = new PrintWriter(new FileWriter(fn));
                } else if (arg.equals("-jvmdi")) {
                    jvmdiReader = new FileReader(fn);
                } else {
                    System.err.println("Invalid option: " + arg);
                    usage();
                }
            } else {
                specSource = arg;
                reader = new FileReader(specSource);
            }
        }
        if (reader == null) {
            System.err.println("<spec_input> must be specified");
            usage();
        }
        
        if (jvmdiReader != null) {
            new JVMDIParse(jvmdiReader);
        } else {
            System.err.println("-jvmdi must be specified");
            usage();
        }
        Parse parse = new Parse(reader);
        RootNode root = parse.items(); 
        root.parentAndExtractComments();
        root.prune();
        root.constrain(new Context());
        if (doc != null) {
            root.document(doc);
            doc.close();
        }
        if (jdi != null) {
            root.genJava(jdi, 0);
            jdi.close();
        }
        if (include != null) {
            root.genCInclude(include);
            include.close();
        }
    }
}
