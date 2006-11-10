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


package sun.io;

/**
 * A table to convert MacDingbat to Unicode
 *
 * @author  ConverterGenerator tool
 * @version >= JDK1.1.6
 */

public class ByteToCharMacDingbat extends ByteToCharSingleByte {
    public String getCharacterEncoding() {
        return "MacDingbat";
    }

    public ByteToCharMacDingbat() {
        super.byteToCharTable = byteToCharTable;
    }
    private final static String byteToCharTable =

        "\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD" + 	// 0x80 - 0x87
        "\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD" + 	// 0x88 - 0x8F
        "\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD" + 	// 0x90 - 0x97
        "\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD" + 	// 0x98 - 0x9F
        "\uFFFD\u2761\u2762\u2763\u2764\u2765\u2766\u2767" + 	// 0xA0 - 0xA7
        "\u2663\u2666\u2665\u2660\u2460\u2461\u2462\u2463" + 	// 0xA8 - 0xAF
        "\u2464\u2465\u2466\u2467\u2468\u2469\u2776\u2777" + 	// 0xB0 - 0xB7
        "\u2778\u2779\u277A\u277B\u277C\u277D\u277E\u277F" + 	// 0xB8 - 0xBF
        "\u2780\u2781\u2782\u2783\u2784\u2785\u2786\u2787" + 	// 0xC0 - 0xC7
        "\u2788\u2789\u278A\u278B\u278C\u278D\u278E\u278F" + 	// 0xC8 - 0xCF
        "\u2790\u2791\u2792\u2793\u2794\u2192\u2194\u2195" + 	// 0xD0 - 0xD7
        "\u2798\u2799\u279A\u279B\u279C\u279D\u279E\u279F" + 	// 0xD8 - 0xDF
        "\u27A0\u27A1\u27A2\u27A3\u27A4\u27A5\u27A6\u27A7" + 	// 0xE0 - 0xE7
        "\u27A8\u27A9\u27AA\u27AB\u27AC\u27AD\u27AE\u27AF" + 	// 0xE8 - 0xEF
        "\uFFFD\u27B1\u27B2\u27B3\u27B4\u27B5\u27B6\u27B7" + 	// 0xF0 - 0xF7
        "\u27B8\u27B9\u27BA\u27BB\u27BC\u27BD\u27BE\uFFFD" + 	// 0xF8 - 0xFF
        "\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007" + 	// 0x00 - 0x07
        "\b\t\n\u000B\f\r\u000E\u000F" + 	// 0x08 - 0x0F
        "\u0010\u0011\u0012\u0013\u0014\u0015\u0016\u0017" + 	// 0x10 - 0x17
        "\u0018\u0019\u001A\u001B\u001C\u001D\u001E\u001F" + 	// 0x18 - 0x1F
        "\u0020\u2701\u2702\u2703\u2704\u260E\u2706\u2707" + 	// 0x20 - 0x27
        "\u2708\u2709\u261B\u261E\u270C\u270D\u270E\u270F" + 	// 0x28 - 0x2F
        "\u2710\u2711\u2712\u2713\u2714\u2715\u2716\u2717" + 	// 0x30 - 0x37
        "\u2718\u2719\u271A\u271B\u271C\u271D\u271E\u271F" + 	// 0x38 - 0x3F
        "\u2720\u2721\u2722\u2723\u2724\u2725\u2726\u2727" + 	// 0x40 - 0x47
        "\u2605\u2729\u272A\u272B\u272C\u272D\u272E\u272F" + 	// 0x48 - 0x4F
        "\u2730\u2731\u2732\u2733\u2734\u2735\u2736\u2737" + 	// 0x50 - 0x57
        "\u2738\u2739\u273A\u273B\u273C\u273D\u273E\u273F" + 	// 0x58 - 0x5F
        "\u2740\u2741\u2742\u2743\u2744\u2745\u2746\u2747" + 	// 0x60 - 0x67
        "\u2748\u2749\u274A\u274B\u25CF\u274D\u25A0\u274F" + 	// 0x68 - 0x6F
        "\u2750\u2751\u2752\u25B2\u25BC\u25C6\u2756\u25D7" + 	// 0x70 - 0x77
        "\u2758\u2759\u275A\u275B\u275C\u275D\u275E\u007F"; 	// 0x78 - 0x7F
}
