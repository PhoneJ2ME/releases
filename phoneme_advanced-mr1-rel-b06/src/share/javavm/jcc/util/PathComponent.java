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
package util;

import dependenceAnalyzer.*;
import java.util.Enumeration;

public class PathComponent {
    PathComponent       root;
    public DependenceNode link;

    public static int INSET = (1<<25);

    public PathComponent( PathComponent r, DependenceNode l ){
        root = r;
        link = l;
    }

    public void print( java.io.PrintStream o ){
        if ( root != null ){
            root.print( o );
            o.print(" => ");
        }
        o.print( link.name() );
    }

    public void grow( Set tipset ){
        Enumeration t = link.dependsOn();
        while ( t.hasMoreElements() ){
            DependenceArc arc = (DependenceArc)(t.nextElement());
            DependenceNode to = (DependenceNode) arc.to();
            if ( (to.flags&INSET) != 0 )
                continue;
            tipset.addElement( new PathComponent( this, to ) );
        }
    }    
}

