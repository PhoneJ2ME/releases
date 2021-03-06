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

class AltNode extends AbstractGroupNode implements TypeNode {

    SelectNode select;

    void constrain(Context ctx) {
        super.constrain(ctx);
                    
        if (!(nameNode instanceof NameValueNode)) {
            error("Alt name must have value: " + nameNode);
        }
        if (parent instanceof SelectNode) {
            select = (SelectNode)parent;
        } else {
            error("Alt must be in Select");
        }
    }
    
    void document(PrintWriter writer) {
        docRowStart(writer);
        writer.println("<td colspan=" + 
                       (maxStructIndent - structIndent + 1) + ">");
        writer.println("Case " + nameNode.name + " - if <i>" +
                       ((SelectNode)parent).typeNode.name + 
                       "</i> is " + nameNode.value() + ":");
        writer.println("<td>" + comment() + "&nbsp;");
        ++structIndent;
        super.document(writer);
        --structIndent;
    }

    String javaClassImplements() {
        return " extends " + select.commonBaseClass();
    }

    void genJavaClassSpecifics(PrintWriter writer, int depth) {
        indent(writer, depth);
        writer.print("static final " + select.typeNode.javaType());
        writer.println(" ALT_ID = " + nameNode.value() + ";");
        if (context.isWritingCommand()) {
            genJavaCreateMethod(writer, depth);
        } else {
            indent(writer, depth);
            writer.println(select.typeNode.javaParam() + "() {");
            indent(writer, depth+1);
            writer.println("return ALT_ID;");
            indent(writer, depth);
            writer.println("}");
        }
        super.genJavaClassSpecifics(writer, depth);
    }

    void genJavaWriteMethod(PrintWriter writer, int depth) {
        genJavaWriteMethod(writer, depth, "");
    }

    void genJavaReadsSelectCase(PrintWriter writer, int depth, String common) {
        indent(writer, depth);
        writer.println("case " + nameNode.value() + ":");
        indent(writer, depth+1);
        writer.println(common + " = new " + name + "(vm, ps);");
        indent(writer, depth+1);
        writer.println("break;");
    }

    void genJavaCreateMethod(PrintWriter writer, int depth) {
        indent(writer, depth);
        writer.print("static " + select.name() + " create(");
        writer.print(javaParams());
        writer.println(") {");
        indent(writer, depth+1);
        writer.print("return new " + select.name() + "(");
        writer.print("ALT_ID, new " + javaClassName() + "(");
        for (Iterator it = components.iterator(); it.hasNext();) {
            TypeNode tn = (TypeNode)it.next();
            writer.print(tn.name());
            if (it.hasNext()) {
                writer.print(", ");
            }
        }
        writer.println("));");
        indent(writer, depth);
        writer.println("}");
    }

}
