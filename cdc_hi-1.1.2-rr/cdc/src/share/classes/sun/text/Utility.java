/*
 * 
 * @(#)Utility.java	1.17 06/10/10
 * 
 * Portions Copyright  2000-2008 Sun Microsystems, Inc. All Rights
 * Reserved.  Use is subject to license terms.
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
 */

/*
 * (C) Copyright Taligent, Inc. 1996, 1997 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - 1998 - All Rights Reserved
 *
 * The original version of this source code and documentation
 * is copyrighted and owned by Taligent, Inc., a wholly-owned
 * subsidiary of IBM. These materials are provided under terms
 * of a License Agreement between Taligent and Sun. This technology
 * is protected by multiple US and International patents.
 *
 * This notice and attribution to Taligent may not be removed.
 * Taligent is a registered trademark of Taligent, Inc.
 *
 */

package sun.text;

public final class Utility {

    /**
     * Convenience utility to compare two Object[]s.
     * Ought to be in System
     */
    public final static boolean arrayEquals(Object[] source, Object target) {
        if (source == null) return (target == null);
        if (!(target instanceof Object[])) return false;
        Object[] targ = (Object[]) target;
        return (source.length == targ.length
                && arrayRegionMatches(source, 0, targ, 0, source.length));
    }

    /**
     * Convenience utility to compare two int[]s
     * Ought to be in System
     */
    public final static boolean arrayEquals(int[] source, Object target) {
        if (source == null) return (target == null);
        if (!(target instanceof int[])) return false;
        int[] targ = (int[]) target;
        return (source.length == targ.length
                && arrayRegionMatches(source, 0, targ, 0, source.length));
    }

    /**
     * Convenience utility to compare two double[]s
     * Ought to be in System
     */
    public final static boolean arrayEquals(double[] source, Object target) {
        if (source == null) return (target == null);
        if (!(target instanceof double[])) return false;
        double[] targ = (double[]) target;
        return (source.length == targ.length
                && arrayRegionMatches(source, 0, targ, 0, source.length));
    }

    /**
     * Convenience utility to compare two Object[]s
     * Ought to be in System
     */
    public final static boolean arrayEquals(Object source, Object target) {
        if (source == null) return (target == null);
        // for some reason, the correct arrayEquals is not being called
        // so do it by hand for now.
        if (source instanceof Object[])
            return(arrayEquals((Object[]) source,target));
        if (source instanceof int[])
            return(arrayEquals((int[]) source,target));
        if (source instanceof double[])
            return(arrayEquals((double[]) source,target));
        return source.equals(target);
    }

    /**
     * Convenience utility to compare two Object[]s
     * Ought to be in System.
     * @param len the length to compare.
     * The start indices and start+len must be valid.
     */
    public final static boolean arrayRegionMatches(Object[] source, int sourceStart,
                                            Object[] target, int targetStart,
                                            int len)
    {
        int sourceEnd = sourceStart + len;
        int delta = targetStart - sourceStart;
        for (int i = sourceStart; i < sourceEnd; i++) {
            if (!arrayEquals(source[i],target[i + delta]))
            return false;
        }
        return true;
    }

    /**
     * Convenience utility to compare two int[]s.
     * @param len the length to compare.
     * The start indices and start+len must be valid.
     * Ought to be in System
     */
    public final static boolean arrayRegionMatches(int[] source, int sourceStart,
                                            int[] target, int targetStart,
                                            int len)
    {
        int sourceEnd = sourceStart + len;
        int delta = targetStart - sourceStart;
        for (int i = sourceStart; i < sourceEnd; i++) {
            if (source[i] != target[i + delta])
            return false;
        }
        return true;
    }

    /**
     * Convenience utility to compare two arrays of doubles.
     * @param len the length to compare.
     * The start indices and start+len must be valid.
     * Ought to be in System
     */
    public final static boolean arrayRegionMatches(double[] source, int sourceStart,
                                            double[] target, int targetStart,
                                            int len)
    {
        int sourceEnd = sourceStart + len;
        int delta = targetStart - sourceStart;
        for (int i = sourceStart; i < sourceEnd; i++) {
            if (source[i] != target[i + delta])
            return false;
        }
        return true;
    }

    /**
     * Convenience utility. Does null checks on objects, then calls equals.
     */
    public final static boolean objectEquals(Object source, Object target) {
        if (source == null)
            return (target == null);
        else
            return source.equals(target);
    }

    /**
     * The ESCAPE character is used during run-length encoding.  It signals
     * a run of identical chars.
     */
    static final char ESCAPE = '\uA5A5';

    /**
     * The ESCAPE_BYTE character is used during run-length encoding.  It signals
     * a run of identical bytes.
     */
    static final byte ESCAPE_BYTE = (byte)0xA5;

    /**
     * Construct a string representing a short array.  Use run-length encoding.
     * A character represents itself, unless it is the ESCAPE character.  Then
     * the following notations are possible:
     *   ESCAPE ESCAPE   ESCAPE literal
     *   ESCAPE n c      n instances of character c
     * Since an encoded run occupies 3 characters, we only encode runs of 4 or
     * more characters.  Thus we have n > 0 and n != ESCAPE and n <= 0xFFFF.
     * If we encounter a run where n == ESCAPE, we represent this as:
     *   c ESCAPE n-1 c
     * The ESCAPE value is chosen so as not to collide with commonly
     * seen values.
     */
    public static final String arrayToRLEString(short[] a) {
        StringBuffer buffer = new StringBuffer();
        // for (int i=0; i<a.length; ++i) buffer.append((char) a[i]);
        buffer.append((char) (a.length >> 16));
        buffer.append((char) a.length);
        short runValue = a[0];
        int runLength = 1;
        for (int i=1; i<a.length; ++i) {
            short s = a[i];
            if (s == runValue && runLength < 0xFFFF) {
                ++runLength;
            }
            else {
                encodeRun(buffer, runValue, runLength);
                runValue = s;
                runLength = 1;
            }
        }
        encodeRun(buffer, runValue, runLength);
        return buffer.toString();
    }

    /**
     * Construct a string representing a byte array.  Use run-length encoding.
     * Two bytes are packed into a single char, with a single extra zero byte at
     * the end if needed.  A byte represents itself, unless it is the
     * ESCAPE_BYTE.  Then the following notations are possible:
     *   ESCAPE_BYTE ESCAPE_BYTE   ESCAPE_BYTE literal
     *   ESCAPE_BYTE n b           n instances of byte b
     * Since an encoded run occupies 3 bytes, we only encode runs of 4 or
     * more bytes.  Thus we have n > 0 and n != ESCAPE_BYTE and n <= 0xFF.
     * If we encounter a run where n == ESCAPE_BYTE, we represent this as:
     *   b ESCAPE_BYTE n-1 b
     * The ESCAPE_BYTE value is chosen so as not to collide with commonly
     * seen values.
     */
    public static final String arrayToRLEString(byte[] a) {
        StringBuffer buffer = new StringBuffer();
        buffer.append((char) (a.length >> 16));
        buffer.append((char) a.length);
        byte runValue = a[0];
        int runLength = 1;
        byte[] state = new byte[2];
        for (int i=1; i<a.length; ++i) {
            byte b = a[i];
            if (b == runValue && runLength < 0xFF) {
                ++runLength;
            }
            else {
                encodeRun(buffer, runValue, runLength, state);
                runValue = b;
                runLength = 1;
            }
        }
        encodeRun(buffer, runValue, runLength, state);

        // We must save the final byte, if there is one, by padding
        // an extra zero.
        if (state[0] != 0) {
            appendEncodedByte(buffer, (byte)0, state);
        }

        return buffer.toString();
    }
    /**
     * Construct a string representing a char array.  Use run-length encoding.
     * A character represents itself, unless it is the ESCAPE character.  Then
     * the following notations are possible:
     *   ESCAPE ESCAPE   ESCAPE literal
     *   ESCAPE n c      n instances of character c
     * Since an encoded run occupies 3 characters, we only encode runs of 4 or
     * more characters.  Thus we have n > 0 and n != ESCAPE and n <= 0xFFFF.
     * If we encounter a run where n == ESCAPE, we represent this as:
     *   c ESCAPE n-1 c
     * The ESCAPE value is chosen so as not to collide with commonly
     * seen values.
     */
    public static final String arrayToRLEString(char[] a) {
        StringBuffer buffer = new StringBuffer();
        buffer.append((char) (a.length >> 16));
        buffer.append((char) a.length);
        char runValue = a[0];
        int runLength = 1;
        for (int i=1; i<a.length; ++i) {
            char s = a[i];
            if (s == runValue && runLength < 0xFFFF) ++runLength;
            else {
            encodeRun(buffer, (short)runValue, runLength);
            runValue = s;
            runLength = 1;
            }
        }
        encodeRun(buffer, (short)runValue, runLength);
        return buffer.toString();
    }

        /**
     * Construct a string representing an int array.  Use run-length encoding.
     * A character represents itself, unless it is the ESCAPE character.  Then
     * the following notations are possible:
     *   ESCAPE ESCAPE   ESCAPE literal
     *   ESCAPE n c      n instances of character c
     * Since an encoded run occupies 3 characters, we only encode runs of 4 or
     * more characters.  Thus we have n > 0 and n != ESCAPE and n <= 0xFFFF.
     * If we encounter a run where n == ESCAPE, we represent this as:
     *   c ESCAPE n-1 c
     * The ESCAPE value is chosen so as not to collide with commonly
     * seen values.
     */
    public static final String arrayToRLEString(int[] a) {
        StringBuffer buffer = new StringBuffer();

        appendInt(buffer, a.length);
        int runValue = a[0];
        int runLength = 1;
        for (int i=1; i<a.length; ++i) {
            int s = a[i];
            if (s == runValue && runLength < 0xFFFF) {
                ++runLength;
            } else {
                encodeRun(buffer, runValue, runLength);
                runValue = s;
                runLength = 1;
            }
        }
        encodeRun(buffer, runValue, runLength);
        return buffer.toString();
    }


    /**
     * Encode a run, possibly a degenerate run (of < 4 values).
     * @param length The length of the run; must be > 0 && <= 0xFFFF.
     */
    private static final void encodeRun(StringBuffer buffer, short value, int length) {
        if (length < 4) {
            for (int j=0; j<length; ++j) {
                if (value == (int) ESCAPE) {
                    buffer.append(ESCAPE);
                }
                buffer.append((char) value);
            }
        }
        else {
            if (length == (int) ESCAPE) {
                if (value == (int) ESCAPE) {
                    buffer.append(ESCAPE);
                }
                buffer.append((char) value);
                --length;
            }
            buffer.append(ESCAPE);
            buffer.append((char) length);
            buffer.append((char) value); // Don't need to escape this value
        }
    }

    /**
     * Encode a run, possibly a degenerate run (of < 4 values).
     * @param length The length of the run; must be > 0 && <= 0xFF.
     */
    private static final void encodeRun(StringBuffer buffer, byte value, int length,
                    byte[] state) {
        if (length < 4) {
            for (int j=0; j<length; ++j) {
                if (value == ESCAPE_BYTE) appendEncodedByte(buffer, ESCAPE_BYTE, state);
                appendEncodedByte(buffer, value, state);
            }
        }
        else {
            if (length == ESCAPE_BYTE) {
                if (value == ESCAPE_BYTE) {
                    appendEncodedByte(buffer, ESCAPE_BYTE, state);
                }
                appendEncodedByte(buffer, value, state);
                --length;
            }
            appendEncodedByte(buffer, ESCAPE_BYTE, state);
            appendEncodedByte(buffer, (byte)length, state);
            appendEncodedByte(buffer, value, state); // Don't need to escape this value
        }
    }

    /**
     * Encode a run, possibly a degenerate run (of < 4 values).
     * @param length The length of the run; must be > 0 && <= 0xFFFF.
     */
    private static final void encodeRun(StringBuffer buffer, int value, int length) {
        if (length < 4) {
            for (int j=0; j<length; ++j) {
                if (value == ESCAPE) {
                    appendInt(buffer, value);
                }
                appendInt(buffer, value);
            }
        }
        else {
            if (length == (int) ESCAPE) {
                if (value == (int) ESCAPE) {
                    appendInt(buffer, ESCAPE);
                }
                appendInt(buffer, value);
                --length;
            }
            appendInt(buffer, ESCAPE);
            appendInt(buffer, length);
            appendInt(buffer, value); // Don't need to escape this value
        }
    }


    private static final void appendInt(StringBuffer buffer, int value) {
        buffer.append((char)(value >>> 16));
        buffer.append((char)(value & 0xFFFF));
    }


    /**
     * Append a byte to the given StringBuffer, packing two bytes into each
     * character.  The state parameter maintains intermediary data between
     * calls.
     * @param state A two-element array, with state[0] == 0 if this is the
     * first byte of a pair, or state[0] != 0 if this is the second byte
     * of a pair, in which case state[1] is the first byte.
     */
    private static final void appendEncodedByte(StringBuffer buffer, byte value,
                        byte[] state) {
        if (state[0] != 0) {
            char c = (char) ((state[1] << 8) | (((int) value) & 0xFF));
            buffer.append(c);
            state[0] = 0;
        }
        else {
            state[0] = 1;
            state[1] = value;
        }
    }

    /**
     * Construct an array of shorts from a run-length encoded string.
     */
    public static final short[] RLEStringToShortArray(String s) {
        int length = (((int) s.charAt(0)) << 16) | ((int) s.charAt(1));
        short[] array = new short[length];
        int ai = 0;
        for (int i=2; i<s.length(); ++i) {
            char c = s.charAt(i);
            if (c == ESCAPE) {
                c = s.charAt(++i);
                if (c == ESCAPE) {
                    array[ai++] = (short) c;
                }
                else {
                    int runLength = (int) c;
                    short runValue = (short) s.charAt(++i);
                    for (int j=0; j<runLength; ++j) {
                        array[ai++] = runValue;
                    }
                }
            }
            else {
                array[ai++] = (short) c;
            }
        }

        if (ai != length) {
            throw new InternalError("Bad run-length encoded short array");
        }

        return array;
    }

    /**
     * Construct an array of bytes from a run-length encoded string.
     */
    public static final byte[] RLEStringToByteArray(String s) {
        int length = (((int) s.charAt(0)) << 16) | ((int) s.charAt(1));
        byte[] array = new byte[length];
        boolean nextChar = true;
        char c = 0;
        int node = 0;
        int runLength = 0;
        int i = 2;
        for (int ai=0; ai<length; ) {
            // This part of the loop places the next byte into the local
            // variable 'b' each time through the loop.  It keeps the
            // current character in 'c' and uses the boolean 'nextChar'
            // to see if we've taken both bytes out of 'c' yet.
            byte b;
            if (nextChar) {
                c = s.charAt(i++);
                b = (byte) (c >> 8);
                nextChar = false;
            }
            else {
                b = (byte) (c & 0xFF);
                nextChar = true;
            }

            // This part of the loop is a tiny state machine which handles
            // the parsing of the run-length encoding.  This would be simpler
            // if we could look ahead, but we can't, so we use 'node' to
            // move between three nodes in the state machine.
            switch (node) {
            case 0:
                // Normal idle node
                if (b == ESCAPE_BYTE) {
                    node = 1;
                }
                else {
                    array[ai++] = b;
                }
                break;
            case 1:
                // We have seen one ESCAPE_BYTE; we expect either a second
                // one, or a run length and value.
                if (b == ESCAPE_BYTE) {
                    array[ai++] = ESCAPE_BYTE;
                    node = 0;
                }
                else {
                    runLength = b;
                    // Interpret signed byte as unsigned
                    if (runLength < 0) {
                        runLength += 0x100;
                    }
                    node = 2;
                }
                break;
            case 2:
                // We have seen an ESCAPE_BYTE and length byte.  We interpret
                // the next byte as the value to be repeated.
                for (int j=0; j<runLength; ++j) {
                    array[ai++] = b;
                }
                node = 0;
                break;
            }
        }

        if (node != 0) {
            throw new InternalError("Bad run-length encoded byte array");
        }

        if (i != s.length()) {
            throw new InternalError("Excess data in RLE byte array string");
        }

        return array;
    }

        /**
     * Construct an array of shorts from a run-length encoded string.
     */
    static public final char[] RLEStringToCharArray(String s) {
        int length = (((int) s.charAt(0)) << 16) | ((int) s.charAt(1));
        char[] array = new char[length];
        int ai = 0;
        for (int i=2; i<s.length(); ++i) {
            char c = s.charAt(i);
            if (c == ESCAPE) {
                c = s.charAt(++i);
                if (c == ESCAPE) {
                    array[ai++] = c;
                } else {
                    int runLength = (int) c;
                    char runValue = s.charAt(++i);
                    for (int j=0; j<runLength; ++j) array[ai++] = runValue;
                }
            }
            else {
                array[ai++] = c;
            }
        }

        if (ai != length)
            throw new InternalError("Bad run-length encoded short array");

        return array;
    }

        /**
     * Construct an array of ints from a run-length encoded string.
     */
    static public final int[] RLEStringToIntArray(String s) {
        int length = getInt(s, 0);
        int[] array = new int[length];
        int ai = 0, i = 1;

        int maxI = s.length() / 2;
        while (ai < length && i < maxI) {
            int c = getInt(s, i++);

            if (c == ESCAPE) {
                c = getInt(s, i++);
                if (c == ESCAPE) {
                    array[ai++] = c;
                } else {
                    int runLength = c;
                    int runValue = getInt(s, i++);
                    for (int j=0; j<runLength; ++j) {
                        array[ai++] = runValue;
                    }
                }
            }
            else {
                array[ai++] = c;
            }
        }

        if (ai != length || i != maxI) {
            throw new InternalError("Bad run-length encoded int array");
        }

        return array;
    }

    /**
     * Format a String for representation in a source file.  This includes
     * breaking it into lines escaping characters using octal notation
     * when necessary (control characters and double quotes).
     */
    public static final String formatForSource(String s) {
        StringBuffer buffer = new StringBuffer();
        for (int i=0; i<s.length();) {
            if (i > 0) buffer.append("+\n");
            buffer.append("        \"");
            int count = 11;
            while (i<s.length() && count<80) {
                char c = s.charAt(i++);
                if (c < '\u0020' || c == '"') {
                    // Represent control characters and the double quote
                    // using octal notation; otherwise the string we form
                    // won't compile, since Unicode escape sequences are
                    // processed before tokenization.
                    buffer.append('\\');
                    buffer.append(HEX_DIGIT[(c & 0700) >> 6]); // HEX_DIGIT works for octal
                    buffer.append(HEX_DIGIT[(c & 0070) >> 3]);
                    buffer.append(HEX_DIGIT[(c & 0007)]);
                    count += 4;
                }
                else if (c <= '\u007E') {
                    buffer.append(c);
                    count += 1;
                }
                else {
                    buffer.append("\\u");
                    buffer.append(HEX_DIGIT[(c & 0xF000) >> 12]);
                    buffer.append(HEX_DIGIT[(c & 0x0F00) >> 8]);
                    buffer.append(HEX_DIGIT[(c & 0x00F0) >> 4]);
                    buffer.append(HEX_DIGIT[(c & 0x000F)]);
                    count += 6;
                }
            }
            buffer.append('"');
        }
        return buffer.toString();
    }


    public static final String hex(char ch) {
        StringBuffer buff = new StringBuffer();
        return hex(ch, buff).toString();
    }

    public static final StringBuffer hex(String src, StringBuffer buff) {
        if (src != null && buff != null) {
            int strLen = src.length();
            int x = 0;
            hex(src.charAt(x), buff);
            while(x<strLen) {
                buff.append(',');
                hex(src.charAt(x++), buff);
            }

        }
        return buff;
    }

    public static final String hex(String str) {
        StringBuffer buff = new StringBuffer();
        hex(str, buff);
        return buff.toString();
    }

    public static final String hex(StringBuffer buff) {
        return hex(buff.toString());
    }

    public static final StringBuffer hex(char ch, StringBuffer buff) {
        for (int shift = 12; shift >=0; shift-=4) {
            buff.append( HEX_DIGIT[(byte)((ch >> shift) & 0x0F)]);
        }
        return buff;
    }


    static final int getInt(String s, int i) {
        return (((int) s.charAt(2*i)) << 16) | (int) s.charAt(2*i+1);
    }


    static final char[] HEX_DIGIT = {'0','1','2','3','4','5','6','7',
                     '8','9','A','B','C','D','E','F'};

}

