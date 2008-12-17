/*
 * @(#)ByteToCharCp918.java	1.14 06/10/10
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
 * A table to convert Cp918 to Unicode
 *
 * @author  ConverterGenerator tool
 * @version >= JDK1.1.6
 */

public class ByteToCharCp918 extends ByteToCharSingleByte {
    public String getCharacterEncoding() {
        return "Cp918";
    }

    public ByteToCharCp918() {
        super.byteToCharTable = byteToCharTable;
    }
    private final static String byteToCharTable =

        "\uFEA7\u0061\u0062\u0063\u0064\u0065\u0066\u0067" + 	// 0x80 - 0x87
        "\u0068\u0069\uFEA9\uFB88\uFEAB\uFEAD\uFB8C\uFEAF" + 	// 0x88 - 0x8F
        "\uFB8A\u006A\u006B\u006C\u006D\u006E\u006F\u0070" + 	// 0x90 - 0x97
        "\u0071\u0072\uFEB1\uFEB3\uFEB5\uFEB7\uFEB9\uFEBB" + 	// 0x98 - 0x9F
        "\uFEBD\u007E\u0073\u0074\u0075\u0076\u0077\u0078" + 	// 0xA0 - 0xA7
        "\u0079\u007A\uFEBF\uFEC3\uFEC7\uFEC9\uFECA\uFECB" + 	// 0xA8 - 0xAF
        "\uFECC\uFECD\uFECE\uFECF\uFED0\uFED1\uFED3\uFED5" + 	// 0xB0 - 0xB7
        "\uFED7\uFB8E\uFEDB\u007C\uFB92\uFB94\uFEDD\uFEDF" + 	// 0xB8 - 0xBF
        "\u007B\u0041\u0042\u0043\u0044\u0045\u0046\u0047" + 	// 0xC0 - 0xC7
        "\u0048\u0049\u00AD\uFEE0\uFEE1\uFEE3\uFB9E\uFEE5" + 	// 0xC8 - 0xCF
        "\u007D\u004A\u004B\u004C\u004D\u004E\u004F\u0050" + 	// 0xD0 - 0xD7
        "\u0051\u0052\uFEE7\uFE85\uFEED\uFBA6\uFBA8\uFBA9" + 	// 0xD8 - 0xDF
        "\\\uFBAA\u0053\u0054\u0055\u0056\u0057\u0058" + 	// 0xE0 - 0xE7
        "\u0059\u005A\uFE80\uFE89\uFE8A\uFE8B\uFBFC\uFBFD" + 	// 0xE8 - 0xEF
        "\u0030\u0031\u0032\u0033\u0034\u0035\u0036\u0037" + 	// 0xF0 - 0xF7
        "\u0038\u0039\uFBFE\uFBB0\uFBAE\uFE7C\uFE7D\u009F" + 	// 0xF8 - 0xFF
        "\u0000\u0001\u0002\u0003\u009C\t\u0086\u007F" + 	// 0x00 - 0x07
        "\u0097\u008D\u008E\u000B\f\r\u000E\u000F" + 	// 0x08 - 0x0F
        "\u0010\u0011\u0012\u0013\u009D\u0085\b\u0087" + 	// 0x10 - 0x17
        "\u0018\u0019\u0092\u008F\u001C\u001D\u001E\u001F" + 	// 0x18 - 0x1F
        "\u0080\u0081\u0082\u0083\u0084\n\u0017\u001B" + 	// 0x20 - 0x27
        "\u0088\u0089\u008A\u008B\u008C\u0005\u0006\u0007" + 	// 0x28 - 0x2F
        "\u0090\u0091\u0016\u0093\u0094\u0095\u0096\u0004" + 	// 0x30 - 0x37
        "\u0098\u0099\u009A\u009B\u0014\u0015\u009E\u001A" + 	// 0x38 - 0x3F
        "\u0020\u00A0\u060C\u061B\u061F\uFE81\uFE8D\uFE8E" + 	// 0x40 - 0x47
        "\uF8FB\uFE8F\u005B\u002E\u003C\u0028\u002B\u0021" + 	// 0x48 - 0x4F
        "\u0026\uFE91\uFB56\uFB58\uFE93\uFE95\uFE97\uFB66" + 	// 0x50 - 0x57
        "\uFB68\uFE99\u005D\u0024\u002A\u0029\u003B\u005E" + 	// 0x58 - 0x5F
        "\u002D\u002F\uFE9B\uFE9D\uFE9F\uFB7A\uFB7C\uFEA1" + 	// 0x60 - 0x67
        "\uFEA3\uFEA5\u0060\u002C\u0025\u005F\u003E\u003F" + 	// 0x68 - 0x6F
        "\u06F0\u06F1\u06F2\u06F3\u06F4\u06F5\u06F6\u06F7" + 	// 0x70 - 0x77
        "\u06F8\u06F9\u003A\u0023\u0040\'\u003D\""; 	// 0x78 - 0x7F
}
