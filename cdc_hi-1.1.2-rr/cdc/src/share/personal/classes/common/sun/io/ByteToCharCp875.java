/*
 * @(#)ByteToCharCp875.java	1.16 06/10/10
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

package sun.io;

/**
 * A table to convert Cp875 to Unicode
 *
 * @author  ConverterGenerator tool
 * @version >= JDK1.1.6
 */

public class ByteToCharCp875 extends ByteToCharSingleByte {
    public String getCharacterEncoding() {
        return "Cp875";
    }

    public ByteToCharCp875() {
        super.byteToCharTable = byteToCharTable;
    }
    private final static String byteToCharTable =

        "\u0385\u0061\u0062\u0063\u0064\u0065\u0066\u0067" +     // 0x80 - 0x87
        "\u0068\u0069\u03B1\u03B2\u03B3\u03B4\u03B5\u03B6" +     // 0x88 - 0x8F
        "\u00B0\u006A\u006B\u006C\u006D\u006E\u006F\u0070" +     // 0x90 - 0x97
        "\u0071\u0072\u03B7\u03B8\u03B9\u03BA\u03BB\u03BC" +     // 0x98 - 0x9F
        "\u00B4\u007E\u0073\u0074\u0075\u0076\u0077\u0078" +     // 0xA0 - 0xA7
        "\u0079\u007A\u03BD\u03BE\u03BF\u03C0\u03C1\u03C3" +     // 0xA8 - 0xAF
        "\u00A3\u03AC\u03AD\u03AE\u03CA\u03AF\u03CC\u03CD" +     // 0xB0 - 0xB7
        "\u03CB\u03CE\u03C2\u03C4\u03C5\u03C6\u03C7\u03C8" +     // 0xB8 - 0xBF
        "\u007B\u0041\u0042\u0043\u0044\u0045\u0046\u0047" +     // 0xC0 - 0xC7
        "\u0048\u0049\u00AD\u03C9\u0390\u03B0\u2018\u2015" +     // 0xC8 - 0xCF
        "\u007D\u004A\u004B\u004C\u004D\u004E\u004F\u0050" +     // 0xD0 - 0xD7
        "\u0051\u0052\u00B1\u00BD\uFFFD\u0387\u2019\u00A6" +     // 0xD8 - 0xDF
        "\\\uFFFD\u0053\u0054\u0055\u0056\u0057\u0058" +     // 0xE0 - 0xE7
        "\u0059\u005A\u00B2\u00A7\uFFFD\uFFFD\u00AB\u00AC" +     // 0xE8 - 0xEF
        "\u0030\u0031\u0032\u0033\u0034\u0035\u0036\u0037" +     // 0xF0 - 0xF7
        "\u0038\u0039\u00B3\u00A9\uFFFD\uFFFD\u00BB\u009F" +     // 0xF8 - 0xFF
        "\u0000\u0001\u0002\u0003\u009C\t\u0086\u007F" +     // 0x00 - 0x07
        "\u0097\u008D\u008E\u000B\f\r\u000E\u000F" +     // 0x08 - 0x0F
        "\u0010\u0011\u0012\u0013\u009D\u0085\b\u0087" +     // 0x10 - 0x17
        "\u0018\u0019\u0092\u008F\u001C\u001D\u001E\u001F" +     // 0x18 - 0x1F
        "\u0080\u0081\u0082\u0083\u0084\n\u0017\u001B" +     // 0x20 - 0x27
        "\u0088\u0089\u008A\u008B\u008C\u0005\u0006\u0007" +     // 0x28 - 0x2F
        "\u0090\u0091\u0016\u0093\u0094\u0095\u0096\u0004" +     // 0x30 - 0x37
        "\u0098\u0099\u009A\u009B\u0014\u0015\u009E\u001A" +     // 0x38 - 0x3F
        "\u0020\u0391\u0392\u0393\u0394\u0395\u0396\u0397" +     // 0x40 - 0x47
        "\u0398\u0399\u005B\u002E\u003C\u0028\u002B\u0021" +     // 0x48 - 0x4F
        "\u0026\u039A\u039B\u039C\u039D\u039E\u039F\u03A0" +     // 0x50 - 0x57
        "\u03A1\u03A3\u005D\u0024\u002A\u0029\u003B\u005E" +     // 0x58 - 0x5F
        "\u002D\u002F\u03A4\u03A5\u03A6\u03A7\u03A8\u03A9" +     // 0x60 - 0x67
        "\u03AA\u03AB\u007C\u002C\u0025\u005F\u003E\u003F" +     // 0x68 - 0x6F
        "\u00A8\u0386\u0388\u0389\u00A0\u038A\u038C\u038E" +     // 0x70 - 0x77
        "\u038F\u0060\u003A\u0023\u0040\'\u003D\"";     // 0x78 - 0x7F
}
