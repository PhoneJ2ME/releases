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

class ReplyNode extends AbstractTypeListNode {

    String cmdName;

    void set(String kind, List components, int lineno) {
        super.set(kind, components, lineno);
        components.add(0, new NameNode(kind));
    }

    void constrain(Context ctx) {
        super.constrain(ctx.replyReadingSubcontext());
        CommandNode cmd = (CommandNode)parent;
        cmdName = cmd.name;
    }

    void genJava(PrintWriter writer, int depth) {
        genJavaPreDef(writer, depth);
        super.genJava(writer, depth);
        writer.println();
        genJavaReadingClassBody(writer, depth, cmdName);
    }

    void genJavaReads(PrintWriter writer, int depth) {
        if (Main.genDebug) {
            indent(writer, depth);
            writer.println(
                "if (vm.traceReceives) {");
            indent(writer, depth+1);
            writer.print(
                "vm.printTrace(\"Receiving Command(id=\" + ps.pkt.id + \") "); 
            writer.print(parent.context.whereJava);
            writer.print("\"");
            writer.print(
                "+(ps.pkt.flags!=0?\", FLAGS=\" + ps.pkt.flags:\"\")");
            writer.print(
                "+(ps.pkt.errorCode!=0?\", ERROR CODE=\" + ps.pkt.errorCode:\"\")");
            writer.println(");");
            indent(writer, depth);
            writer.println("}");
        }
        super.genJavaReads(writer, depth);
    }
}
