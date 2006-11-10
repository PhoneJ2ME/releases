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
 * A table to convert Cp856 to Unicode
 *
 * @author  ConverterGenerator tool
 * @version >= JDK1.1.6
 */

public class ByteToCharCp856 extends ByteToCharSingleByte {
    public String getCharacterEncoding() {
        return "Cp856";
    }

    public ByteToCharCp856() {
        super.byteToCharTable = byteToCharTable;
    }
    private final static String byteToCharTable =

        "\u05D0\u05D1\u05D2\u05D3\u05D4\u05D5\u05D6\u05D7" +     // 0x80 - 0x87
        "\u05D8\u05D9\u05DA\u05DB\u05DC\u05DD\u05DE\u05DF" +     // 0x88 - 0x8F
        "\u05E0\u05E1\u05E2\u05E3\u05E4\u05E5\u05E6\u05E7" +     // 0x90 - 0x97
        "\u05E8\u05E9\u05EA\uFFFD\u00A3\uFFFD\u00D7\uFFFD" +     // 0x98 - 0x9F
        "\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD" +     // 0xA0 - 0xA7
        "\uFFFD\u00AE\u00AC\u00BD\u00BC\uFFFD\u00AB\u00BB" +     // 0xA8 - 0xAF
        "\u2591\u2592\u2593\u2502\u2524\uFFFD\uFFFD\uFFFD" +     // 0xB0 - 0xB7
        "\u00A9\u2563\u2551\u2557\u255D\u00A2\u00A5\u2510" +     // 0xB8 - 0xBF
        "\u2514\u2534\u252C\u251C\u2500\u253C\uFFFD\uFFFD" +     // 0xC0 - 0xC7
        "\u255A\u2554\u2569\u2566\u2560\u2550\u256C\u00A4" +     // 0xC8 - 0xCF
        "\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD" +     // 0xD0 - 0xD7
        "\uFFFD\u2518\u250C\u2588\u2584\u00A6\uFFFD\u2580" +     // 0xD8 - 0xDF
        "\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD\u00B5\uFFFD" +     // 0xE0 - 0xE7
        "\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD\uFFFD\u203E\u00B4" +     // 0xE8 - 0xEF
        "\u00AD\u00B1\u2017\u00BE\u00B6\u00A7\u00F7\u00B8" +     // 0xF0 - 0xF7
        "\u00B0\u00A8\u2022\u00B9\u00B3\u00B2\u25A0\u00A0" +     // 0xF8 - 0xFF
        "\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007" +     // 0x00 - 0x07
        "\b\t\n\u000B\f\r\u000E\u000F" +     // 0x08 - 0x0F
        "\u0010\u0011\u0012\u0013\u0014\u0015\u0016\u0017" +     // 0x10 - 0x17
        "\u0018\u0019\u001A\u001B\u001C\u001D\u001E\u001F" +     // 0x18 - 0x1F
        "\u0020\u0021\"\u0023\u0024\u0025\u0026\'" +     // 0x20 - 0x27
        "\u0028\u0029\u002A\u002B\u002C\u002D\u002E\u002F" +     // 0x28 - 0x2F
        "\u0030\u0031\u0032\u0033\u0034\u0035\u0036\u0037" +     // 0x30 - 0x37
        "\u0038\u0039\u003A\u003B\u003C\u003D\u003E\u003F" +     // 0x38 - 0x3F
        "\u0040\u0041\u0042\u0043\u0044\u0045\u0046\u0047" +     // 0x40 - 0x47
        "\u0048\u0049\u004A\u004B\u004C\u004D\u004E\u004F" +     // 0x48 - 0x4F
        "\u0050\u0051\u0052\u0053\u0054\u0055\u0056\u0057" +     // 0x50 - 0x57
        "\u0058\u0059\u005A\u005B\\\u005D\u005E\u005F" +     // 0x58 - 0x5F
        "\u0060\u0061\u0062\u0063\u0064\u0065\u0066\u0067" +     // 0x60 - 0x67
        "\u0068\u0069\u006A\u006B\u006C\u006D\u006E\u006F" +     // 0x68 - 0x6F
        "\u0070\u0071\u0072\u0073\u0074\u0075\u0076\u0077" +     // 0x70 - 0x77
        "\u0078\u0079\u007A\u007B\u007C\u007D\u007E\u007F";     // 0x78 - 0x7F
}
