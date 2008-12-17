/*
 * 
 * @(#)CharSet.java	1.11 06/10/10
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
 * (C) Copyright IBM Corp. 1996 - 2002 - All Rights Reserved
 *
 * The original version of this source code and documentation
 * is copyrighted and owned by Taligent, Inc., a wholly-owned
 * subsidiary of IBM. These materials are provided under terms
 * of a License Agreement between Taligent and Sun. This technology
 * is protected by multiple US and International patents.
 *
 * This notice and attribution to Taligent may not be removed.
 * Taligent is a registered trademark of Taligent, Inc.
 */
package java.text;

import java.util.Hashtable;

/**
 * An object representing a set of characters.  (This is a "set" in the
 * mathematical sense: an unduplicated list of characters on which set
 * operations such as union and intersection can be performed.)  The
 * set information is stored in compressed, optimized form: The object
 * contains a String with an even number of characters.  Each pair of
 * characters represents a range of characters contained in the set
 * (a pair of the same character represents a single character).  The
 * characters are sorted in increasing order.
 */
class CharSet implements Cloneable {
    /**
     * The structure containing the set information.  The characters
     * in this string are organized into pairs, each pair representing
     * a range of characters contained in the set
     */
    private String chars;

    //==========================================================================
    // parseString() and associated routines
    //==========================================================================
    /**
     * A cache which is used to speed up parseString() whenever it is
     * used to parse a description that has been parsed before
     */
    private static Hashtable expressionCache = null;

    /**
     * Builds a CharSet based on a textual description.  For the syntax of
     * the description, see the documentation of RuleBasedBreakIterator.
     * @see java.text.RuleBasedBreakIterator
     */
    public static CharSet parseString(String s) {
        CharSet result = null;

        // if "s" is in the expression cache, pull the result out
        // of the expresison cache
        if (expressionCache != null) {
            result = (CharSet)expressionCache.get(s);
        }

        // otherwise, use doParseString() to actually parse the string,
        // and then add a corresponding entry to the expression cache
        if (result == null) {
            result = doParseString(s);
            if (expressionCache == null) {
                expressionCache = new Hashtable();
            }
            expressionCache.put(s, result);
        }
        result = (CharSet)(result.clone());
        return result;
    }

    /**
     * This function is used by parseString() to actually parse the string
     */
    private static CharSet doParseString(String s) {
        CharSet result = new CharSet();
        int p = 0;

        boolean haveDash = false;
        boolean haveTilde = false;
        boolean wIsReal = false;
        char w = '\u0000';

        // for each character in the description...
        while (p < s.length()) {
            char c = s.charAt(p);

            // if it's an opening bracket...
            if (c == '[') {
                // flush the single-character cache
                if (wIsReal) {
                    result.internalUnion(new CharSet(w));
                }
		
                // locate the matching closing bracket
                int bracketLevel = 1;
                int q = p + 1;
                while (bracketLevel != 0) {
		    // if no matching bracket by end of string then...
		    if (q >= s.length()) {
			throw new IllegalArgumentException("Parse error at position " + p + " in " + s);
		    }
		    switch (s.charAt(q)) {
		    case '\\': // need to step over next character
			++q;
			break;
		    case '[':
                        ++bracketLevel;
			break;
		    case ']':
                        --bracketLevel;
			break;
		    }
                    ++q;
                }
                --q;
		
                // call parseString() recursively to parse the text inside
                // the brackets, then either add or subtract the result from
                // our running result depending on whether or not the []
                // expresison was preceded by a ^
                if (!haveTilde) {
                    result.internalUnion(CharSet.parseString(s.substring(p + 1, q)));
                }
                else {
                    result.internalDifference(CharSet.parseString(s.substring(p + 1, q)));
                }
                haveTilde = false;
                haveDash = false;
                wIsReal = false;
                p = q + 1;
            }
	    
            // if the character is a colon...
            else if (c == ':') {
                // flush the single-character cache
                if (wIsReal) {
                    result.internalUnion(new CharSet(w));
                }

                // locate the matching colon (and throw an error if there
                // isn't one)
                int q = s.indexOf(':', p + 1);
                if (q == -1) {
                    throw new IllegalArgumentException("Parse error at position " + p + " in " + s);
                }

                // use charSetForCategory() to parse the text in the colons,
                // and either add or substract the result from our running
                // result depending on whether the :: expression was
                // preceded by a ^
                if (!haveTilde) {
                    result.internalUnion(charSetForCategory(s.substring(p + 1, q)));
                }
                else {
                    result.internalDifference(charSetForCategory(s.substring(p + 1, q)));
                }

                // reset everything and advance to the next character
                haveTilde = false;
                haveDash = false;
                wIsReal = false;
                p = q + 1;
            }

            // if the character is a dash, set an appropriate flag
            else if (c == '-') {
                if (wIsReal) {
                    haveDash = true;
                }
                ++p;
            }

            // if the character is a caret, flush the single-character
            // cache and set an appropriate flag.  If the set is empty
            // (i.e., if the expression begins with ^), invert the set
            // (i.e., set it to include everything).  The idea here is
            // that a set that includes nothing but ^ expressions
            // means "everything but these things".
            else if (c == '^') {
                if (wIsReal) {
                    result.internalUnion(new CharSet(w));
                    wIsReal = false;
                }
                haveTilde = true;
                ++p;
                if (result.empty()) {
                    result.internalComplement();
                }
            }

            // throw an exception on an illegal character
            else if (c >= ' ' && c < '\u007f' && !Character.isLetter(c)
                     && !Character.isDigit(c) && c != '\\') {
                throw new IllegalArgumentException("Parse error at position " + p + " in " + s);
            }

            // otherwise, we end up here...
            else {
                // on a backslash, advance to the next character
                if (c == '\\') {
                    ++p;
                }

                // if the preceding character was a dash, this character
                // defines the end of a range.  Add or subtract that range
                // from the running result depending on whether or not it
                // was preceded by a ^
                if (haveDash) {
                    if (s.charAt(p) < w) {
                        throw new IllegalArgumentException("U+" + Integer.toHexString(s.charAt(p))
                            + " is less than U+" + Integer.toHexString(w) + ".  Dash expressions "
                            + "can't have their endpoints in reverse order.");
                    }

                    if (!haveTilde) {
                        result.internalUnion(new CharSet(w, s.charAt(p++)));
                    }
                    else {
                        result.internalDifference(new CharSet(w, s.charAt(p++)));
                    }
                    haveDash = false;
                    haveTilde = false;
                    wIsReal = false;
                }

                // if the preceding character was a caret, remove this character
                // from the running result
                else if (haveTilde) {
                    result.internalDifference(new CharSet(s.charAt(p++)));
                    haveTilde = false;
                    wIsReal = false;
                }

                // otherwise, flush the single-character cache and then
                // put this character into the cache
                else if (wIsReal) {
                    result.internalUnion(new CharSet(w));
                    w = s.charAt(p++);
                    wIsReal = true;
                }
                else {
                    w = s.charAt(p++);
                    wIsReal = true;
                }
            }
        }

        // finally, flush the single-character cache one last time
        if (wIsReal) {
            result.internalUnion(new CharSet(w));
        }

        return result;
    }

    /**
     * Creates a CharSet containing all the characters in a particular
     * Unicode category.  The text is either a two-character code from
     * the Unicode database or a single character that begins one or more
     * two-character codes.
     */
    private static CharSet charSetForCategory(String category) {
        // throw an exception if we have anything other than one or two
        // characters inside the colons
        if (category.length() == 0 || category.length() >= 3) {
            throw new IllegalArgumentException("Invalid character category: " + category);
        }

        // if we have two characters, search the category map for that code
        // and either construct and return a CharSet from the data in the
        // category map or throw an exception
        if (category.length() == 2) {
            for (int i = 0; i < categoryMap.length; i++) {
                if (categoryMap[i][0].equals(category)) {
                    return new CharSet(categoryMap[i][1]);
                }
            }
            throw new IllegalArgumentException("Invalid character category: " + category);
        }

        // if we have one character, search the category map for codes beginning
        // with that letter, and union together all of the matching sets that
        // we find (or throw an exception if there are no matches)
        else if (category.length() == 1) {
            CharSet result = new CharSet();
            for (int i = 0; i < categoryMap.length; i++) {
                if (categoryMap[i][0].startsWith(category)) {
                    result = result.union(new CharSet(categoryMap[i][1]));
                }
            }
            if (result.empty()) {
                throw new IllegalArgumentException("Invalid character category: " + category);
            }
            else {
                return result;
            }
        }
        return new CharSet(); // should never get here, but to make the compiler happy...
    }

    /**
     * Returns a copy of CharSet's expression cache and sets CharSet's
     * expression cache to empty.
     */
    public static Hashtable releaseExpressionCache() {
        Hashtable result = expressionCache;
        expressionCache = null;
        return result;
    }

    //==========================================================================
    // CharSet manipulation
    //==========================================================================
    /**
     * Creates an empty CharSet.
     */
    public CharSet() {
        chars = "";
    }

    /**
     * Creates a CharSet containing a single character.
     * @param c The character to put into the CharSet
     */
    public CharSet(char c) {
        StringBuffer temp = new StringBuffer();
        temp.append(c);
        temp.append(c);
        chars = temp.toString();
    }

    /**
     * Creates a CharSet containing a range of characters.
     * @param lo The lowest-numbered character to include in the range
     * @param hi The highest-numbered character to include in the range
     */
    public CharSet(char lo, char hi) {
        StringBuffer temp = new StringBuffer();
        if (lo <= hi) {
            temp.append(lo);
            temp.append(hi);
        }
        else {
            temp.append(hi);
            temp.append(lo);
        }
        chars = temp.toString();
    }

    /**
     * Creates a CharSet, initializing it from the internal storage
     * of another CharSet (this function performs no error checking
     * on "chars", so if it's malformed, undefined behavior will result)
     */
    private CharSet(String chars) {
        this.chars = chars;
    }

    /**
     * Returns a CharSet representing the union of two CharSets.
     */
    public CharSet union(CharSet that) {
        return new CharSet(doUnion(that.chars).toString());
    }

    /**
     * Adds the characters in "that" to this CharSet
     */
    private void internalUnion(CharSet that) {
        chars = doUnion(that.chars).toString();
    }

    /**
     * The actual implementation of the union functions
     */
    private StringBuffer doUnion(String c2) {
        StringBuffer result = new StringBuffer();

        int i = 0;
        int j = 0;

        // consider all the characters in both strings
        while (i < chars.length() && j < c2.length()) {
            char ub;

            // the first character in the result is the lower of the
            // starting characters of the two strings, and "ub" gets
            // set to the upper bound of that range
            if (chars.charAt(i) < c2.charAt(j)) {
                result.append(chars.charAt(i));
                ub = chars.charAt(++i);
            }
            else {
                result.append(c2.charAt(j));
                ub = c2.charAt(++j);
            }

            // for as long as one of our two pointers is pointing to a range's
            // end point, or i is pointing to a character that is less than
            // "ub" plus one (the "plus one" stitches touching ranges together)...
            while (i % 2 == 1 ||
                   j % 2 == 1 ||
                   (i < chars.length() && chars.charAt(i) <= ub + 1)) {

                // advance i to the first character that is greater than
                // "ub" plus one
                while (i < chars.length() && chars.charAt(i) <= ub + 1) {
                    ++i;
                }

                // if i points to the endpoint of a range, update "ub"
                // to that character, or if i points to the start of
                // a range and the endpoint of the preceding range is
                // greater than "ub", update "up" to _that_ character
                if (i % 2 == 1) {
                    ub = chars.charAt(i);
                }
                else if (i > 0 && chars.charAt(i - 1) > ub) {
                    ub = chars.charAt(i - 1);
                }

                // now advance j to the first character that is greater
                // that "ub" plus one
                while (j < c2.length() && c2.charAt(j) <= ub + 1) {
                    ++j;
                }

                // if j points to the endpoint of a range, update "ub"
                // to that character, or if j points to the start of
                // a range and the endpoint of the preceding range is
                // greater than "ub", update "up" to _that_ character
                if (j % 2 == 1) {
                    ub = c2.charAt(j);
                }
                else if (j > 0 && c2.charAt(j - 1) > ub) {
                    ub = c2.charAt(j - 1);
                }
            }
            // when we finally fall out of this loop, we will have stitched
            // together a series of ranges that overlap or touch, i and j
            // will both point to starting points of ranges, and "ub" will
            // be the endpoint of the range we're working on.  Write "ub"
            // to the result
            result.append(ub);

        // loop back around to create the next range in the result
        }

        // we fall out to here when we've exhausted all the characters in
        // one of the operands.  We can append all of the remaining characters
        // in the other operand without doing any extra work.
        if (i < chars.length()) {
            result.append(chars.substring(i));
        }
        if (j < c2.length()) {
            result.append(c2.substring(j));
        }

        return result;
    }

    /**
     * Returns the intersection of two CharSets.
     */
    public CharSet intersection(CharSet that) {
        return new CharSet(doIntersection(that.chars).toString());
    }

    /**
     * Removes from this CharSet any characters that aren't also in "that"
     */
    private void internalIntersection(CharSet that) {
        chars = doIntersection(that.chars).toString();
    }

    /**
     * The internal implementation of the two intersection functions
     */
    private StringBuffer doIntersection(String c2) {
        StringBuffer result = new StringBuffer();

        int i = 0;
        int j = 0;
        int oldI;
        int oldJ;

        // iterate until we've exhausted one of the operands
        while (i < chars.length() && j < c2.length()) {

            // advance j until it points to a character that is larger than
            // the one i points to.  If this is the beginning of a one-
            // character range, advance j to point to the end
            if (i < chars.length() && i % 2 == 0) {
                while (j < c2.length() && c2.charAt(j) < chars.charAt(i)) {
                    ++j;
                }
                if (j < c2.length() && j % 2 == 0 && c2.charAt(j) == chars.charAt(i)) {
                    ++j;
                }
            }

            // if j points to the endpoint of a range, save the current
            // value of i, then advance i until it reaches a character
            // which is larger than the character pointed at
            // by j.  All of the characters we've advanced over (except
            // the one currently pointed to by i) are added to the result
            oldI = i;
            while (j % 2 == 1 && i < chars.length() && chars.charAt(i) <= c2.charAt(j)) {
                ++i;
            }
            result.append(chars.substring(oldI, i));

            // if i points to the endpoint of a range, save the current
            // value of j, then advance j until it reaches a character
            // which is larger than the character pointed at
            // by i.  All of the characters we've advanced over (except
            // the one currently pointed to by i) are added to the result
            oldJ = j;
            while (i % 2 == 1 && j < c2.length() && c2.charAt(j) <= chars.charAt(i)) {
                ++j;
            }
            result.append(c2.substring(oldJ, j));

            // advance i until it points to a character larger than j
            // If it points at the beginning of a one-character range,
            // advance it to the end of that range
            if (j < c2.length() && j % 2 == 0) {
                while (i < chars.length() && chars.charAt(i) < c2.charAt(j)) {
                    ++i;
                }
                if (i < chars.length() && i % 2 == 0 && c2.charAt(j) == chars.charAt(i)) {
                    ++i;
                }
            }
        }

        return result;
    }

    /**
     * Returns a CharSet containing all the characters in "this" that
     * aren't also in "that"
     */
    public CharSet difference(CharSet that) {
        return new CharSet(doIntersection(that.doComplement().toString()).toString());
    }

    /**
     * Removes from "this" all the characters that are also in "that"
     */
    private void internalDifference(CharSet that) {
        chars = doIntersection(that.doComplement().toString()).toString();
    }

    /**
     * Returns a CharSet containing all the characters which are not
     * in "this"
     */
    public CharSet complement() {
        return new CharSet(doComplement().toString());
    }

    /**
     * Complements "this".  All the characters it contains are removed,
     * and all the characters it doesn't contain are added.
     */
    private void internalComplement() {
        chars = doComplement().toString();
    }

    /**
     * The internal implementation function for the complement routines
     */
    private StringBuffer doComplement() {
        // the complement of an empty CharSet is one containing everything
        if (empty()) {
            return new StringBuffer("\u0000\uffff");
        }

        StringBuffer result = new StringBuffer();

        int i = 0;

        // the result begins with \u0000 unless the original CharSet does
        if (chars.charAt(0) != '\u0000') {
            result.append('\u0000');
        }

        // walk through the characters in this CharSet.  Append a pair of
        // characters the first of which is one less than the first
        // character we see and the second of which is one plus the second
        // character we see (don't write the first character if it's \u0000,
        // and don't write the second character if it's \uffff.
        while (i < chars.length()) {
            if (chars.charAt(i) != '\u0000') {
                result.append((char)(chars.charAt(i) - 1));
            }
            if (chars.charAt(i + 1) != '\uffff') {
                result.append((char)(chars.charAt(i + 1) + 1));
            }
            i += 2;
        }

        // add \uffff to the end of the result, unless it was in
        // the original set
        if (chars.charAt(chars.length() - 1) != '\uffff') {
            result.append('\uffff');
        }

        return result;
    }

    /**
     * Returns true if this CharSet contains the specified character
     * @param c The character we're testing for set membership
     */
    public boolean contains(char c) {
        // search for the first range endpoint that is greater than or
        // equal to c
        int i = 1;
        while (i < chars.length() && chars.charAt(i) < c) {
            i += 2;
        }

        // if we've walked off the end, we don't contain c
        if (i == chars.length()) {
            return false;
        }

        // otherwise, we contain c if the beginning of the range is less
        // than or equal to c
        return chars.charAt(i - 1) <= c;
    }

    /**
     * Returns true if "that" is another instance of CharSet containing
     * the exact same characters as this one
     */
    public boolean equals(Object that) {
        return (that instanceof CharSet) && chars.equals(((CharSet)that).chars);
    }

    /**
     * Creates a new CharSet that is equal to this one
     */
    public Object clone() {
        return new CharSet(chars);
    }

    /**
     * Returns true if this CharSet contains no characters
     */
    public boolean empty() {
        return chars.length() == 0;
    }

    /**
     * Returns a textual representation of this CharSet.  If the result
     * of calling this function is passed to CharSet.parseString(), it
     * will produce another CharSet that is equal to this one.
     */
    public String toString() {
        StringBuffer result = new StringBuffer();

        // the result begins with an opening bracket
        result.append('[');

        // iterate through the ranges in the CharSet
        for (int i = 0; i < chars.length(); i += 2) {
            // for a range with the same beginning and ending point,
            // output that character
            if (chars.charAt(i) == chars.charAt(i + 1)) {
                result.append(chars.charAt(i));
            }

            // otherwise, output the start and end points of the range
            // separated by a dash
            else {
                result.append(chars.charAt(i) + "-" + chars.charAt(i + 1));
            }
        }

        // the result ends with a closing bracket
        result.append(']');
        return result.toString();
    }

    /**
     * Returns a String representing the contents of this CharSet
     * in the same form in which they're stored internally: as pairs
     * of characters representing the start and end points of ranges
     */
    public String getRanges() {
        return chars;
    }

    /**
     * Returns an Enumeration that will return the ranges of characters
     * contained in this CharSet one at a time
     */
    public Enumeration getChars() {
        return new Enumeration(this);
    }

    //==========================================================================
    // CharSet.Enumeration
    //==========================================================================

    /**
     * An Enumeration that can be used to extract the character ranges
     * from a CharSet one at a time
     */
    public class Enumeration implements java.util.Enumeration {
        /**
         * Initializes a CharSet.Enumeration
         */
        Enumeration(CharSet cs) {
            this.chars = cs.chars;
            p = 0;
        }

        /**
         * Returns true if the enumeration hasn't yet returned
         * all the ranges in the CharSet
         */
        public boolean hasMoreElements() {
            return p < chars.length();
        }

        /**
         * Returns the next range in the CarSet
         */
        public Object nextElement() {
            char[] result = new char[2];
            result[0] = chars.charAt(p);
            result[1] = chars.charAt(p + 1);
            p += 2;

            return result;
        }

        int p;
        String chars;
    }

    //==========================================================================
    // tables for charSetForCategory()
    //==========================================================================

    /**
     * Table used with charSetFromCategory.  This is an array of pairs
     * of Strings.  The first column of Strings is Unicode character category
     * codes as defined in the Unicode database.  The second column is the
     * internal storage for a CharSet containing the characters in that
     * category.
     */
    private static final String[][] categoryMap = {
        { "Ll", "az\u00AA\u00AA\u00B5\u00B5\u00BA\u00BA\u00DF\u00F6\u00F8"
          + "\u00FF\u0101\u0101\u0103\u0103\u0105\u0105\u0107\u0107\u0109"
          + "\u0109\u010B\u010B\u010D\u010D\u010F\u010F\u0111\u0111\u0113"
          + "\u0113\u0115\u0115\u0117\u0117\u0119\u0119\u011B\u011B\u011D"
          + "\u011D\u011F\u011F\u0121\u0121\u0123\u0123\u0125\u0125\u0127"
          + "\u0127\u0129\u0129\u012B\u012B\u012D\u012D\u012F\u012F\u0131"
          + "\u0131\u0133\u0133\u0135\u0135\u0137\u0138\u013A\u013A\u013C"
          + "\u013C\u013E\u013E\u0140\u0140\u0142\u0142\u0144\u0144\u0146"
          + "\u0146\u0148\u0149\u014B\u014B\u014D\u014D\u014F\u014F\u0151"
          + "\u0151\u0153\u0153\u0155\u0155\u0157\u0157\u0159\u0159\u015B"
          + "\u015B\u015D\u015D\u015F\u015F\u0161\u0161\u0163\u0163\u0165"
          + "\u0165\u0167\u0167\u0169\u0169\u016B\u016B\u016D\u016D\u016F"
          + "\u016F\u0171\u0171\u0173\u0173\u0175\u0175\u0177\u0177\u017A"
          + "\u017A\u017C\u017C\u017E\u0180\u0183\u0183\u0185\u0185\u0188"
          + "\u0188\u018C\u018D\u0192\u0192\u0195\u0195\u0199\u019B\u019E"
          + "\u019E\u01A1\u01A1\u01A3\u01A3\u01A5\u01A5\u01A8\u01A8\u01AA"
          + "\u01AB\u01AD\u01AD\u01B0\u01B0\u01B4\u01B4\u01B6\u01B6\u01B9"
          + "\u01BA\u01BD\u01BF\u01C6\u01C6\u01C9\u01C9\u01CC\u01CC\u01CE"
          + "\u01CE\u01D0\u01D0\u01D2\u01D2\u01D4\u01D4\u01D6\u01D6\u01D8"
          + "\u01D8\u01DA\u01DA\u01DC\u01DD\u01DF\u01DF\u01E1\u01E1\u01E3"
          + "\u01E3\u01E5\u01E5\u01E7\u01E7\u01E9\u01E9\u01EB\u01EB\u01ED"
          + "\u01ED\u01EF\u01F0\u01F3\u01F3\u01F5\u01F5\u01F9\u01F9\u01FB"
          + "\u01FB\u01FD\u01FD\u01FF\u01FF\u0201\u0201\u0203\u0203\u0205"
          + "\u0205\u0207\u0207\u0209\u0209\u020B\u020B\u020D\u020D\u020F"
          + "\u020F\u0211\u0211\u0213\u0213\u0215\u0215\u0217\u0217\u0219"
          + "\u0219\u021B\u021B\u021D\u021D\u021F\u021F\u0223\u0223\u0225"
          + "\u0225\u0227\u0227\u0229\u0229\u022B\u022B\u022D\u022D\u022F"
          + "\u022F\u0231\u0231\u0233\u0233\u0250\u02AD\u0390\u0390\u03AC"
          + "\u03CE\u03D0\u03D1\u03D5\u03D7\u03DB\u03DB\u03DD\u03DD\u03DF"
          + "\u03DF\u03E1\u03E1\u03E3\u03E3\u03E5\u03E5\u03E7\u03E7\u03E9"
          + "\u03E9\u03EB\u03EB\u03ED\u03ED\u03EF\u03F3\u0430\u045F\u0461"
          + "\u0461\u0463\u0463\u0465\u0465\u0467\u0467\u0469\u0469\u046B"
          + "\u046B\u046D\u046D\u046F\u046F\u0471\u0471\u0473\u0473\u0475"
          + "\u0475\u0477\u0477\u0479\u0479\u047B\u047B\u047D\u047D\u047F"
          + "\u047F\u0481\u0481\u048D\u048D\u048F\u048F\u0491\u0491\u0493"
          + "\u0493\u0495\u0495\u0497\u0497\u0499\u0499\u049B\u049B\u049D"
          + "\u049D\u049F\u049F\u04A1\u04A1\u04A3\u04A3\u04A5\u04A5\u04A7"
          + "\u04A7\u04A9\u04A9\u04AB\u04AB\u04AD\u04AD\u04AF\u04AF\u04B1"
          + "\u04B1\u04B3\u04B3\u04B5\u04B5\u04B7\u04B7\u04B9\u04B9\u04BB"
          + "\u04BB\u04BD\u04BD\u04BF\u04BF\u04C2\u04C2\u04C4\u04C4\u04C8"
          + "\u04C8\u04CC\u04CC\u04D1\u04D1\u04D3\u04D3\u04D5\u04D5\u04D7"
          + "\u04D7\u04D9\u04D9\u04DB\u04DB\u04DD\u04DD\u04DF\u04DF\u04E1"
          + "\u04E1\u04E3\u04E3\u04E5\u04E5\u04E7\u04E7\u04E9\u04E9\u04EB"
          + "\u04EB\u04ED\u04ED\u04EF\u04EF\u04F1\u04F1\u04F3\u04F3\u04F5"
          + "\u04F5\u04F9\u04F9\u0561\u0587\u1E01\u1E01\u1E03\u1E03\u1E05"
          + "\u1E05\u1E07\u1E07\u1E09\u1E09\u1E0B\u1E0B\u1E0D\u1E0D\u1E0F"
          + "\u1E0F\u1E11\u1E11\u1E13\u1E13\u1E15\u1E15\u1E17\u1E17\u1E19"
          + "\u1E19\u1E1B\u1E1B\u1E1D\u1E1D\u1E1F\u1E1F\u1E21\u1E21\u1E23"
          + "\u1E23\u1E25\u1E25\u1E27\u1E27\u1E29\u1E29\u1E2B\u1E2B\u1E2D"
          + "\u1E2D\u1E2F\u1E2F\u1E31\u1E31\u1E33\u1E33\u1E35\u1E35\u1E37"
          + "\u1E37\u1E39\u1E39\u1E3B\u1E3B\u1E3D\u1E3D\u1E3F\u1E3F\u1E41"
          + "\u1E41\u1E43\u1E43\u1E45\u1E45\u1E47\u1E47\u1E49\u1E49\u1E4B"
          + "\u1E4B\u1E4D\u1E4D\u1E4F\u1E4F\u1E51\u1E51\u1E53\u1E53\u1E55"
          + "\u1E55\u1E57\u1E57\u1E59\u1E59\u1E5B\u1E5B\u1E5D\u1E5D\u1E5F"
          + "\u1E5F\u1E61\u1E61\u1E63\u1E63\u1E65\u1E65\u1E67\u1E67\u1E69"
          + "\u1E69\u1E6B\u1E6B\u1E6D\u1E6D\u1E6F\u1E6F\u1E71\u1E71\u1E73"
          + "\u1E73\u1E75\u1E75\u1E77\u1E77\u1E79\u1E79\u1E7B\u1E7B\u1E7D"
          + "\u1E7D\u1E7F\u1E7F\u1E81\u1E81\u1E83\u1E83\u1E85\u1E85\u1E87"
          + "\u1E87\u1E89\u1E89\u1E8B\u1E8B\u1E8D\u1E8D\u1E8F\u1E8F\u1E91"
          + "\u1E91\u1E93\u1E93\u1E95\u1E9B\u1EA1\u1EA1\u1EA3\u1EA3\u1EA5"
          + "\u1EA5\u1EA7\u1EA7\u1EA9\u1EA9\u1EAB\u1EAB\u1EAD\u1EAD\u1EAF"
          + "\u1EAF\u1EB1\u1EB1\u1EB3\u1EB3\u1EB5\u1EB5\u1EB7\u1EB7\u1EB9"
          + "\u1EB9\u1EBB\u1EBB\u1EBD\u1EBD\u1EBF\u1EBF\u1EC1\u1EC1\u1EC3"
          + "\u1EC3\u1EC5\u1EC5\u1EC7\u1EC7\u1EC9\u1EC9\u1ECB\u1ECB\u1ECD"
          + "\u1ECD\u1ECF\u1ECF\u1ED1\u1ED1\u1ED3\u1ED3\u1ED5\u1ED5\u1ED7"
          + "\u1ED7\u1ED9\u1ED9\u1EDB\u1EDB\u1EDD\u1EDD\u1EDF\u1EDF\u1EE1"
          + "\u1EE1\u1EE3\u1EE3\u1EE5\u1EE5\u1EE7\u1EE7\u1EE9\u1EE9\u1EEB"
          + "\u1EEB\u1EED\u1EED\u1EEF\u1EEF\u1EF1\u1EF1\u1EF3\u1EF3\u1EF5"
          + "\u1EF5\u1EF7\u1EF7\u1EF9\u1EF9\u1F00\u1F07\u1F10\u1F15\u1F20"
          + "\u1F27\u1F30\u1F37\u1F40\u1F45\u1F50\u1F57\u1F60\u1F67\u1F70"
          + "\u1F7D\u1F80\u1F87\u1F90\u1F97\u1FA0\u1FA7\u1FB0\u1FB4\u1FB6"
          + "\u1FB7\u1FBE\u1FBE\u1FC2\u1FC4\u1FC6\u1FC7\u1FD0\u1FD3\u1FD6"
          + "\u1FD7\u1FE0\u1FE7\u1FF2\u1FF4\u1FF6\u1FF7\u207F\u207F\u210A"
          + "\u210A\u210E\u210F\u2113\u2113\u212F\u212F\u2134\u2134\u2139"
          + "\u2139\uFB00\uFB06\uFB13\uFB17\uFF41\uFF5A" },
        { "Lu", "AZ\u00C0\u00D6\u00D8\u00DE\u0100\u0100\u0102\u0102\u0104"
          + "\u0104\u0106\u0106\u0108\u0108\u010A\u010A\u010C\u010C\u010E"
          + "\u010E\u0110\u0110\u0112\u0112\u0114\u0114\u0116\u0116\u0118"
          + "\u0118\u011A\u011A\u011C\u011C\u011E\u011E\u0120\u0120\u0122"
          + "\u0122\u0124\u0124\u0126\u0126\u0128\u0128\u012A\u012A\u012C"
          + "\u012C\u012E\u012E\u0130\u0130\u0132\u0132\u0134\u0134\u0136"
          + "\u0136\u0139\u0139\u013B\u013B\u013D\u013D\u013F\u013F\u0141"
          + "\u0141\u0143\u0143\u0145\u0145\u0147\u0147\u014A\u014A\u014C"
          + "\u014C\u014E\u014E\u0150\u0150\u0152\u0152\u0154\u0154\u0156"
          + "\u0156\u0158\u0158\u015A\u015A\u015C\u015C\u015E\u015E\u0160"
          + "\u0160\u0162\u0162\u0164\u0164\u0166\u0166\u0168\u0168\u016A"
          + "\u016A\u016C\u016C\u016E\u016E\u0170\u0170\u0172\u0172\u0174"
          + "\u0174\u0176\u0176\u0178\u0179\u017B\u017B\u017D\u017D\u0181"
          + "\u0182\u0184\u0184\u0186\u0187\u0189\u018B\u018E\u0191\u0193"
          + "\u0194\u0196\u0198\u019C\u019D\u019F\u01A0\u01A2\u01A2\u01A4"
          + "\u01A4\u01A6\u01A7\u01A9\u01A9\u01AC\u01AC\u01AE\u01AF\u01B1"
          + "\u01B3\u01B5\u01B5\u01B7\u01B8\u01BC\u01BC\u01C4\u01C4\u01C7"
          + "\u01C7\u01CA\u01CA\u01CD\u01CD\u01CF\u01CF\u01D1\u01D1\u01D3"
          + "\u01D3\u01D5\u01D5\u01D7\u01D7\u01D9\u01D9\u01DB\u01DB\u01DE"
          + "\u01DE\u01E0\u01E0\u01E2\u01E2\u01E4\u01E4\u01E6\u01E6\u01E8"
          + "\u01E8\u01EA\u01EA\u01EC\u01EC\u01EE\u01EE\u01F1\u01F1\u01F4"
          + "\u01F4\u01F6\u01F8\u01FA\u01FA\u01FC\u01FC\u01FE\u01FE\u0200"
          + "\u0200\u0202\u0202\u0204\u0204\u0206\u0206\u0208\u0208\u020A"
          + "\u020A\u020C\u020C\u020E\u020E\u0210\u0210\u0212\u0212\u0214"
          + "\u0214\u0216\u0216\u0218\u0218\u021A\u021A\u021C\u021C\u021E"
          + "\u021E\u0222\u0222\u0224\u0224\u0226\u0226\u0228\u0228\u022A"
          + "\u022A\u022C\u022C\u022E\u022E\u0230\u0230\u0232\u0232\u0386"
          + "\u0386\u0388\u038A\u038C\u038C\u038E\u038F\u0391\u03A1\u03A3"
          + "\u03AB\u03D2\u03D4\u03DA\u03DA\u03DC\u03DC\u03DE\u03DE\u03E0"
          + "\u03E0\u03E2\u03E2\u03E4\u03E4\u03E6\u03E6\u03E8\u03E8\u03EA"
          + "\u03EA\u03EC\u03EC\u03EE\u03EE\u0400\u042F\u0460\u0460\u0462"
          + "\u0462\u0464\u0464\u0466\u0466\u0468\u0468\u046A\u046A\u046C"
          + "\u046C\u046E\u046E\u0470\u0470\u0472\u0472\u0474\u0474\u0476"
          + "\u0476\u0478\u0478\u047A\u047A\u047C\u047C\u047E\u047E\u0480"
          + "\u0480\u048C\u048C\u048E\u048E\u0490\u0490\u0492\u0492\u0494"
          + "\u0494\u0496\u0496\u0498\u0498\u049A\u049A\u049C\u049C\u049E"
          + "\u049E\u04A0\u04A0\u04A2\u04A2\u04A4\u04A4\u04A6\u04A6\u04A8"
          + "\u04A8\u04AA\u04AA\u04AC\u04AC\u04AE\u04AE\u04B0\u04B0\u04B2"
          + "\u04B2\u04B4\u04B4\u04B6\u04B6\u04B8\u04B8\u04BA\u04BA\u04BC"
          + "\u04BC\u04BE\u04BE\u04C0\u04C1\u04C3\u04C3\u04C7\u04C7\u04CB"
          + "\u04CB\u04D0\u04D0\u04D2\u04D2\u04D4\u04D4\u04D6\u04D6\u04D8"
          + "\u04D8\u04DA\u04DA\u04DC\u04DC\u04DE\u04DE\u04E0\u04E0\u04E2"
          + "\u04E2\u04E4\u04E4\u04E6\u04E6\u04E8\u04E8\u04EA\u04EA\u04EC"
          + "\u04EC\u04EE\u04EE\u04F0\u04F0\u04F2\u04F2\u04F4\u04F4\u04F8"
          + "\u04F8\u0531\u0556\u10A0\u10C5\u1E00\u1E00\u1E02\u1E02\u1E04"
          + "\u1E04\u1E06\u1E06\u1E08\u1E08\u1E0A\u1E0A\u1E0C\u1E0C\u1E0E"
          + "\u1E0E\u1E10\u1E10\u1E12\u1E12\u1E14\u1E14\u1E16\u1E16\u1E18"
          + "\u1E18\u1E1A\u1E1A\u1E1C\u1E1C\u1E1E\u1E1E\u1E20\u1E20\u1E22"
          + "\u1E22\u1E24\u1E24\u1E26\u1E26\u1E28\u1E28\u1E2A\u1E2A\u1E2C"
          + "\u1E2C\u1E2E\u1E2E\u1E30\u1E30\u1E32\u1E32\u1E34\u1E34\u1E36"
          + "\u1E36\u1E38\u1E38\u1E3A\u1E3A\u1E3C\u1E3C\u1E3E\u1E3E\u1E40"
          + "\u1E40\u1E42\u1E42\u1E44\u1E44\u1E46\u1E46\u1E48\u1E48\u1E4A"
          + "\u1E4A\u1E4C\u1E4C\u1E4E\u1E4E\u1E50\u1E50\u1E52\u1E52\u1E54"
          + "\u1E54\u1E56\u1E56\u1E58\u1E58\u1E5A\u1E5A\u1E5C\u1E5C\u1E5E"
          + "\u1E5E\u1E60\u1E60\u1E62\u1E62\u1E64\u1E64\u1E66\u1E66\u1E68"
          + "\u1E68\u1E6A\u1E6A\u1E6C\u1E6C\u1E6E\u1E6E\u1E70\u1E70\u1E72"
          + "\u1E72\u1E74\u1E74\u1E76\u1E76\u1E78\u1E78\u1E7A\u1E7A\u1E7C"
          + "\u1E7C\u1E7E\u1E7E\u1E80\u1E80\u1E82\u1E82\u1E84\u1E84\u1E86"
          + "\u1E86\u1E88\u1E88\u1E8A\u1E8A\u1E8C\u1E8C\u1E8E\u1E8E\u1E90"
          + "\u1E90\u1E92\u1E92\u1E94\u1E94\u1EA0\u1EA0\u1EA2\u1EA2\u1EA4"
          + "\u1EA4\u1EA6\u1EA6\u1EA8\u1EA8\u1EAA\u1EAA\u1EAC\u1EAC\u1EAE"
          + "\u1EAE\u1EB0\u1EB0\u1EB2\u1EB2\u1EB4\u1EB4\u1EB6\u1EB6\u1EB8"
          + "\u1EB8\u1EBA\u1EBA\u1EBC\u1EBC\u1EBE\u1EBE\u1EC0\u1EC0\u1EC2"
          + "\u1EC2\u1EC4\u1EC4\u1EC6\u1EC6\u1EC8\u1EC8\u1ECA\u1ECA\u1ECC"
          + "\u1ECC\u1ECE\u1ECE\u1ED0\u1ED0\u1ED2\u1ED2\u1ED4\u1ED4\u1ED6"
          + "\u1ED6\u1ED8\u1ED8\u1EDA\u1EDA\u1EDC\u1EDC\u1EDE\u1EDE\u1EE0"
          + "\u1EE0\u1EE2\u1EE2\u1EE4\u1EE4\u1EE6\u1EE6\u1EE8\u1EE8\u1EEA"
          + "\u1EEA\u1EEC\u1EEC\u1EEE\u1EEE\u1EF0\u1EF0\u1EF2\u1EF2\u1EF4"
          + "\u1EF4\u1EF6\u1EF6\u1EF8\u1EF8\u1F08\u1F0F\u1F18\u1F1D\u1F28"
          + "\u1F2F\u1F38\u1F3F\u1F48\u1F4D\u1F59\u1F59\u1F5B\u1F5B\u1F5D"
          + "\u1F5D\u1F5F\u1F5F\u1F68\u1F6F\u1FB8\u1FBB\u1FC8\u1FCB\u1FD8"
          + "\u1FDB\u1FE8\u1FEC\u1FF8\u1FFB\u2102\u2102\u2107\u2107\u210B"
          + "\u210D\u2110\u2112\u2115\u2115\u2119\u211D\u2124\u2124\u2126"
          + "\u2126\u2128\u2128\u212A\u212D\u2130\u2131\u2133\u2133\uFF21"
          + "\uFF3A" },
        { "Lt", "\u01C5\u01C5\u01C8\u01C8\u01CB\u01CB\u01F2\u01F2\u1F88"
          + "\u1F8F\u1F98\u1F9F\u1FA8\u1FAF\u1FBC\u1FBC\u1FCC\u1FCC\u1FFC"
          + "\u1FFC" },
        { "Lo", "\u01BB\u01BB\u01C0\u01C3\u05D0\u05EA\u05F0\u05F2\u0621"
          + "\u063A\u0641\u064A\u0671\u06D3\u06D5\u06D5\u06FA\u06FC\u0710"
          + "\u0710\u0712\u072C\u0780\u07A5\u0905\u0939\u093D\u093D\u0950"
          + "\u0950\u0958\u0961\u0985\u098C\u098F\u0990\u0993\u09A8\u09AA"
          + "\u09B0\u09B2\u09B2\u09B6\u09B9\u09DC\u09DD\u09DF\u09E1\u09F0"
          + "\u09F1\u0A05\u0A0A\u0A0F\u0A10\u0A13\u0A28\u0A2A\u0A30\u0A32"
          + "\u0A33\u0A35\u0A36\u0A38\u0A39\u0A59\u0A5C\u0A5E\u0A5E\u0A72"
          + "\u0A74\u0A85\u0A8B\u0A8D\u0A8D\u0A8F\u0A91\u0A93\u0AA8\u0AAA"
          + "\u0AB0\u0AB2\u0AB3\u0AB5\u0AB9\u0ABD\u0ABD\u0AD0\u0AD0\u0AE0"
          + "\u0AE0\u0B05\u0B0C\u0B0F\u0B10\u0B13\u0B28\u0B2A\u0B30\u0B32"
          + "\u0B33\u0B36\u0B39\u0B3D\u0B3D\u0B5C\u0B5D\u0B5F\u0B61\u0B85"
          + "\u0B8A\u0B8E\u0B90\u0B92\u0B95\u0B99\u0B9A\u0B9C\u0B9C\u0B9E"
          + "\u0B9F\u0BA3\u0BA4\u0BA8\u0BAA\u0BAE\u0BB5\u0BB7\u0BB9\u0C05"
          + "\u0C0C\u0C0E\u0C10\u0C12\u0C28\u0C2A\u0C33\u0C35\u0C39\u0C60"
          + "\u0C61\u0C85\u0C8C\u0C8E\u0C90\u0C92\u0CA8\u0CAA\u0CB3\u0CB5"
          + "\u0CB9\u0CDE\u0CDE\u0CE0\u0CE1\u0D05\u0D0C\u0D0E\u0D10\u0D12"
          + "\u0D28\u0D2A\u0D39\u0D60\u0D61\u0D85\u0D96\u0D9A\u0DB1\u0DB3"
          + "\u0DBB\u0DBD\u0DBD\u0DC0\u0DC6\u0E01\u0E30\u0E32\u0E33\u0E40"
          + "\u0E45\u0E81\u0E82\u0E84\u0E84\u0E87\u0E88\u0E8A\u0E8A\u0E8D"
          + "\u0E8D\u0E94\u0E97\u0E99\u0E9F\u0EA1\u0EA3\u0EA5\u0EA5\u0EA7"
          + "\u0EA7\u0EAA\u0EAB\u0EAD\u0EB0\u0EB2\u0EB3\u0EBD\u0EBD\u0EC0"
          + "\u0EC4\u0EDC\u0EDD\u0F00\u0F00\u0F40\u0F47\u0F49\u0F6A\u0F88"
          + "\u0F8B\u1000\u1021\u1023\u1027\u1029\u102A\u1050\u1055\u10D0"
          + "\u10F6\u1100\u1159\u115F\u11A2\u11A8\u11F9\u1200\u1206\u1208"
          + "\u1246\u1248\u1248\u124A\u124D\u1250\u1256\u1258\u1258\u125A"
          + "\u125D\u1260\u1286\u1288\u1288\u128A\u128D\u1290\u12AE\u12B0"
          + "\u12B0\u12B2\u12B5\u12B8\u12BE\u12C0\u12C0\u12C2\u12C5\u12C8"
          + "\u12CE\u12D0\u12D6\u12D8\u12EE\u12F0\u130E\u1310\u1310\u1312"
          + "\u1315\u1318\u131E\u1320\u1346\u1348\u135A\u13A0\u13F4\u1401"
          + "\u166C\u166F\u1676\u1681\u169A\u16A0\u16EA\u1780\u17B3\u1820"
          + "\u1842\u1844\u1877\u1880\u18A8\u2135\u2138\u3006\u3006\u3041"
          + "\u3094\u30A1\u30FA\u3105\u312C\u3131\u318E\u31A0\u31B7\u3400"
          + "\u4DB5\u4E00\u9FA5\uA000\uA48C\uAC00\uD7A3\uF900\uFA2D\uFB1D"
          + "\uFB1D\uFB1F\uFB28\uFB2A\uFB36\uFB38\uFB3C\uFB3E\uFB3E\uFB40"
          + "\uFB41\uFB43\uFB44\uFB46\uFBB1\uFBD3\uFD3D\uFD50\uFD8F\uFD92"
          + "\uFDC7\uFDF0\uFDFB\uFE70\uFE72\uFE74\uFE74\uFE76\uFEFC\uFF66"
          + "\uFF6F\uFF71\uFF9D\uFFA0\uFFBE\uFFC2\uFFC7\uFFCA\uFFCF\uFFD2"
          + "\uFFD7\uFFDA\uFFDC" },
        { "Lm", "\u02B0\u02B8\u02BB\u02C1\u02D0\u02D1\u02E0\u02E4\u02EE"
          + "\u02EE\u037A\u037A\u0559\u0559\u0640\u0640\u06E5\u06E6\u0E46"
          + "\u0E46\u0EC6\u0EC6\u1843\u1843\u3005\u3005\u3031\u3035\u309D"
          + "\u309E\u30FC\u30FE\uFF70\uFF70\uFF9E\uFF9F" },
        { "Nd", "09\u0660\u0669\u06F0\u06F9\u0966\u096F\u09E6\u09EF\u0A66"
          + "\u0A6F\u0AE6\u0AEF\u0B66\u0B6F\u0BE7\u0BEF\u0C66\u0C6F\u0CE6"
          + "\u0CEF\u0D66\u0D6F\u0E50\u0E59\u0ED0\u0ED9\u0F20\u0F29\u1040"
          + "\u1049\u1369\u1371\u17E0\u17E9\u1810\u1819\uFF10\uFF19" },
        { "Nl", "\u2160\u2183\u3007\u3007\u3021\u3029\u3038\u303A" },
        { "No", "\u00B2\u00B3\u00B9\u00B9\u00BC\u00BE\u09F4\u09F9\u0BF0"
          + "\u0BF2\u0F2A\u0F33\u1372\u137C\u16EE\u16F0\u2070\u2070\u2074"
          + "\u2079\u2080\u2089\u2153\u215F\u2460\u249B\u24EA\u24EA\u2776"
          + "\u2793\u3192\u3195\u3220\u3229\u3280\u3289" },
        { "Ps", "(([[{{\u0F3A\u0F3A\u0F3C\u0F3C\u169B\u169B\u201A\u201A"
          + "\u201E\u201E\u2045\u2045\u207D\u207D\u208D\u208D\u2329\u2329"
          + "\u3008\u3008\u300A\u300A\u300C\u300C\u300E\u300E\u3010\u3010"
          + "\u3014\u3014\u3016\u3016\u3018\u3018\u301A\u301A\u301D\u301D"
          + "\uFD3E\uFD3E\uFE35\uFE35\uFE37\uFE37\uFE39\uFE39\uFE3B\uFE3B"
          + "\uFE3D\uFE3D\uFE3F\uFE3F\uFE41\uFE41\uFE43\uFE43\uFE59\uFE59"
          + "\uFE5B\uFE5B\uFE5D\uFE5D\uFF08\uFF08\uFF3B\uFF3B\uFF5B\uFF5B"
          + "\uFF62\uFF62" },
        { "Pe", "))]]}}\u0F3B\u0F3B\u0F3D\u0F3D\u169C\u169C\u2046\u2046"
          + "\u207E\u207E\u208E\u208E\u232A\u232A\u3009\u3009\u300B\u300B"
          + "\u300D\u300D\u300F\u300F\u3011\u3011\u3015\u3015\u3017\u3017"
          + "\u3019\u3019\u301B\u301B\u301E\u301F\uFD3F\uFD3F\uFE36\uFE36"
          + "\uFE38\uFE38\uFE3A\uFE3A\uFE3C\uFE3C\uFE3E\uFE3E\uFE40\uFE40"
          + "\uFE42\uFE42\uFE44\uFE44\uFE5A\uFE5A\uFE5C\uFE5C\uFE5E\uFE5E"
          + "\uFF09\uFF09\uFF3D\uFF3D\uFF5D\uFF5D\uFF63\uFF63" },
        { "Pi", "\u00AB\u00AB\u2018\u2018\u201B\u201C\u201F\u201F\u2039"
          + "\u2039" },
        { "Pf", "\u00BB\u00BB\u2019\u2019\u201D\u201D\u203A\u203A" },
        { "Pd", "--\u00AD\u00AD\u058A\u058A\u1806\u1806\u2010\u2015\u301C"
          + "\u301C\u3030\u3030\uFE31\uFE32\uFE58\uFE58\uFE63\uFE63\uFF0D"
          + "\uFF0D" },
        { "Pc", "__\u203F\u2040\u30FB\u30FB\uFE33\uFE34\uFE4D\uFE4F\uFF3F"
          + "\uFF3F\uFF65\uFF65" },
        { "Po", "!#%'**,,./:;?@\\\\\u00A1\u00A1\u00B7\u00B7\u00BF\u00BF\u037E"
          + "\u037E\u0387\u0387\u055A\u055F\u0589\u0589\u05BE\u05BE\u05C0"
          + "\u05C0\u05C3\u05C3\u05F3\u05F4\u060C\u060C\u061B\u061B\u061F"
          + "\u061F\u066A\u066D\u06D4\u06D4\u0700\u070D\u0964\u0965\u0970"
          + "\u0970\u0DF4\u0DF4\u0E4F\u0E4F\u0E5A\u0E5B\u0F04\u0F12\u0F85"
          + "\u0F85\u104A\u104F\u10FB\u10FB\u1361\u1368\u166D\u166E\u16EB"
          + "\u16ED\u17D4\u17DA\u17DC\u17DC\u1800\u1805\u1807\u180A\u2016"
          + "\u2017\u2020\u2027\u2030\u2038\u203B\u203E\u2041\u2043\u2048"
          + "\u204D\u3001\u3003\uFE30\uFE30\uFE49\uFE4C\uFE50\uFE52\uFE54"
          + "\uFE57\uFE5F\uFE61\uFE68\uFE68\uFE6A\uFE6B\uFF01\uFF03\uFF05"
          + "\uFF07\uFF0A\uFF0A\uFF0C\uFF0C\uFF0E\uFF0F\uFF1A\uFF1B\uFF1F"
          + "\uFF20\uFF3C\uFF3C\uFF61\uFF61\uFF64\uFF64" },
        { "Sc", "$$\u00A2\u00A5\u09F2\u09F3\u0E3F\u0E3F\u17DB\u17DB\u20A0"
          + "\u20AF\uFE69\uFE69\uFF04\uFF04\uFFE0\uFFE1\uFFE5\uFFE6" },
        { "Sm", "++<>||~~\u00AC\u00AC\u00B1\u00B1\u00D7\u00D7\u00F7\u00F7"
          + "\u2044\u2044\u207A\u207C\u208A\u208C\u2190\u2194\u219A\u219B"
          + "\u21A0\u21A0\u21A3\u21A3\u21A6\u21A6\u21AE\u21AE\u21CE\u21CF"
          + "\u21D2\u21D2\u21D4\u21D4\u2200\u22F1\u2308\u230B\u2320\u2321"
          + "\u25B7\u25B7\u25C1\u25C1\u266F\u266F\uFB29\uFB29\uFE62\uFE62"
          + "\uFE64\uFE66\uFF0B\uFF0B\uFF1C\uFF1E\uFF5C\uFF5C\uFF5E\uFF5E"
          + "\uFFE2\uFFE2\uFFE9\uFFEC" },
        { "So", "\u00A6\u00A7\u00A9\u00A9\u00AE\u00AE\u00B0\u00B0\u00B6"
          + "\u00B6\u0482\u0482\u06E9\u06E9\u06FD\u06FE\u09FA\u09FA\u0B70"
          + "\u0B70\u0F01\u0F03\u0F13\u0F17\u0F1A\u0F1F\u0F34\u0F34\u0F36"
          + "\u0F36\u0F38\u0F38\u0FBE\u0FC5\u0FC7\u0FCC\u0FCF\u0FCF\u2100"
          + "\u2101\u2103\u2106\u2108\u2109\u2114\u2114\u2116\u2118\u211E"
          + "\u2123\u2125\u2125\u2127\u2127\u2129\u2129\u212E\u212E\u2132"
          + "\u2132\u213A\u213A\u2195\u2199\u219C\u219F\u21A1\u21A2\u21A4"
          + "\u21A5\u21A7\u21AD\u21AF\u21CD\u21D0\u21D1\u21D3\u21D3\u21D5"
          + "\u21F3\u2300\u2307\u230C\u231F\u2322\u2328\u232B\u237B\u237D"
          + "\u239A\u2400\u2426\u2440\u244A\u249C\u24E9\u2500\u2595\u25A0"
          + "\u25B6\u25B8\u25C0\u25C2\u25F7\u2600\u2613\u2619\u266E\u2670"
          + "\u2671\u2701\u2704\u2706\u2709\u270C\u2727\u2729\u274B\u274D"
          + "\u274D\u274F\u2752\u2756\u2756\u2758\u275E\u2761\u2767\u2794"
          + "\u2794\u2798\u27AF\u27B1\u27BE\u2800\u28FF\u2E80\u2E99\u2E9B"
          + "\u2EF3\u2F00\u2FD5\u2FF0\u2FFB\u3004\u3004\u3012\u3013\u3020"
          + "\u3020\u3036\u3037\u303E\u303F\u3190\u3191\u3196\u319F\u3200"
          + "\u321C\u322A\u3243\u3260\u327B\u327F\u327F\u328A\u32B0\u32C0"
          + "\u32CB\u32D0\u32FE\u3300\u3376\u337B\u33DD\u33E0\u33FE\uA490"
          + "\uA4A1\uA4A4\uA4B3\uA4B5\uA4C0\uA4C2\uA4C4\uA4C6\uA4C6\uFFE4"
          + "\uFFE4\uFFE8\uFFE8\uFFED\uFFEE\uFFFC\uFFFD" },
        { "Mn", "\u0300\u034E\u0360\u0362\u0483\u0486\u0591\u05A1\u05A3"
          + "\u05B9\u05BB\u05BD\u05BF\u05BF\u05C1\u05C2\u05C4\u05C4\u064B"
          + "\u0655\u0670\u0670\u06D6\u06DC\u06DF\u06E4\u06E7\u06E8\u06EA"
          + "\u06ED\u0711\u0711\u0730\u074A\u07A6\u07B0\u0901\u0902\u093C"
          + "\u093C\u0941\u0948\u094D\u094D\u0951\u0954\u0962\u0963\u0981"
          + "\u0981\u09BC\u09BC\u09C1\u09C4\u09CD\u09CD\u09E2\u09E3\u0A02"
          + "\u0A02\u0A3C\u0A3C\u0A41\u0A42\u0A47\u0A48\u0A4B\u0A4D\u0A70"
          + "\u0A71\u0A81\u0A82\u0ABC\u0ABC\u0AC1\u0AC5\u0AC7\u0AC8\u0ACD"
          + "\u0ACD\u0B01\u0B01\u0B3C\u0B3C\u0B3F\u0B3F\u0B41\u0B43\u0B4D"
          + "\u0B4D\u0B56\u0B56\u0B82\u0B82\u0BC0\u0BC0\u0BCD\u0BCD\u0C3E"
          + "\u0C40\u0C46\u0C48\u0C4A\u0C4D\u0C55\u0C56\u0CBF\u0CBF\u0CC6"
          + "\u0CC6\u0CCC\u0CCD\u0D41\u0D43\u0D4D\u0D4D\u0DCA\u0DCA\u0DD2"
          + "\u0DD4\u0DD6\u0DD6\u0E31\u0E31\u0E34\u0E3A\u0E47\u0E4E\u0EB1"
          + "\u0EB1\u0EB4\u0EB9\u0EBB\u0EBC\u0EC8\u0ECD\u0F18\u0F19\u0F35"
          + "\u0F35\u0F37\u0F37\u0F39\u0F39\u0F71\u0F7E\u0F80\u0F84\u0F86"
          + "\u0F87\u0F90\u0F97\u0F99\u0FBC\u0FC6\u0FC6\u102D\u1030\u1032"
          + "\u1032\u1036\u1037\u1039\u1039\u1058\u1059\u17B7\u17BD\u17C6"
          + "\u17C6\u17C9\u17D3\u18A9\u18A9\u20D0\u20DC\u20E1\u20E1\u302A"
          + "\u302F\u3099\u309A\uFB1E\uFB1E\uFE20\uFE23" },
        { "Mc", "\u0903\u0903\u093E\u0940\u0949\u094C\u0982\u0983\u09BE"
          + "\u09C0\u09C7\u09C8\u09CB\u09CC\u09D7\u09D7\u0A3E\u0A40\u0A83"
          + "\u0A83\u0ABE\u0AC0\u0AC9\u0AC9\u0ACB\u0ACC\u0B02\u0B03\u0B3E"
          + "\u0B3E\u0B40\u0B40\u0B47\u0B48\u0B4B\u0B4C\u0B57\u0B57\u0B83"
          + "\u0B83\u0BBE\u0BBF\u0BC1\u0BC2\u0BC6\u0BC8\u0BCA\u0BCC\u0BD7"
          + "\u0BD7\u0C01\u0C03\u0C41\u0C44\u0C82\u0C83\u0CBE\u0CBE\u0CC0"
          + "\u0CC4\u0CC7\u0CC8\u0CCA\u0CCB\u0CD5\u0CD6\u0D02\u0D03\u0D3E"
          + "\u0D40\u0D46\u0D48\u0D4A\u0D4C\u0D57\u0D57\u0D82\u0D83\u0DCF"
          + "\u0DD1\u0DD8\u0DDF\u0DF2\u0DF3\u0F3E\u0F3F\u0F7F\u0F7F\u102C"
          + "\u102C\u1031\u1031\u1038\u1038\u1056\u1057\u17B4\u17B6\u17BE"
          + "\u17C5\u17C7\u17C8" },
        { "Me", "\u0488\u0489\u06DD\u06DE\u20DD\u20E0\u20E2\u20E3" },
        { "Zl", "\u2028\u2028" },
        { "Zp", "\u2029\u2029" },
        { "Zs", "\u0020\u0020\u00A0\u00A0\u1680\u1680\u2000\u200B\u202F"
          + "\u202F\u3000\u3000" },
        { "Cc", "\u0000\u001F\u007F\u009F" },
        { "Cf", "\u070F\u070F\u180B\u180E\u200C\u200F\u202A\u202E\u206A"
          + "\u206F\uFEFF\uFEFF\uFFF9\uFFFB" },
    };
}
