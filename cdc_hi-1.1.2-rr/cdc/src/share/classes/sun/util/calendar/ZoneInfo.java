/*
 * @(#)ZoneInfo.java	1.9 06/10/10
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
 */

package sun.util.calendar;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

/**
 * <code>ZoneInfo</code> is an implementation subclass of {@link
 * java.util.TimeZone TimeZone} that represents GMT offsets and
 * daylight saving time transitions of a time zone.
 * <p>
 * The daylight saving time transitions are described in the {@link
 * #transitions transitions} table consisting of a chronological
 * sequence of transitions of GMT offset and/or daylight saving time
 * changes. Since all transitions are represented in UTC, in theory,
 * <code>ZoneInfo</code> can be used with any calendar systems except
 * for the {@link #getOffset(int,int,int,int,int,int) getOffset}
 * method that takes Gregorian calendar date fields.
 * <p>
 * This table covers transitions from 1900 until 2037 (as of version
 * 1.4), Before 1900, it assumes that there was no daylight saving
 * time and the <code>getOffset</code> methods always return the
 * {@link #getRawOffset} value. No Local Mean Time is supported. If a
 * specified date is beyond the transition table and this time zone is
 * supposed to observe daylight saving time in 2037, it delegates
 * operations to a {@link java.util.SimpleTimeZone SimpleTimeZone}
 * object created using the daylight saving time schedule as of 2037.
 * <p>
 * The date items, transitions, GMT offset(s), etc. are read from a database
 * file. See {@link ZoneInfoFile} for details.
 * @see java.util.SimpleTimeZone
 * @since 1.4
 */

public class ZoneInfo extends TimeZone {

    private static final long OFFSET_MASK = 0x0fL;
    private static final long DST_MASK = 0xf0L;
    private static final int DST_NSHIFT = 4;
    // this bit field is reserved for abbreviation support
    private static final long ABBR_MASK = 0xf00L;
    private static final int TRANSITION_NSHIFT = 12;

    /**
     * The raw GMT offset in milliseconds between this zone and GMT.
     * Negative offsets are to the west of Greenwich.  To obtain local
     * <em>standard</em> time, add the offset to GMT time.
     * @serial
     */
    private int rawOffset;

    /**
     * Difference in milliseconds from the original GMT offset in case
     * the raw offset value has been modified by calling {@link
     * #setRawOffset}. The initial value is 0.
     * @serial
     */
    private int rawOffsetDiff = 0;

    /**
     * A CRC32 value of all pairs of transition time (in milliseconds
     * in <code>long</code>) in local time and its GMT offset (in
     * seconds in <code>int</code>) in the chronological order. Byte
     * values of each <code>long</code> and <code>int</code> are taken
     * in the big endian order (i.e., MSB to LSB).
     * @serial
     */
    private int checksum;

    /**
     * The amount of time in milliseconds saved during daylight saving
     * time. If <code>useDaylight</code> is false, this value is 0.
     * @serial
     */
    private int dstSavings;

    /**
     * This array describes transitions of GMT offsets of this time
     * zone, including both raw offset changes and daylight saving
     * time changes.
     * A long integer consists of four bit fields.
     * <ul>
     * <li>The most significant 52-bit field represents transition
     * time in milliseconds from Gregorian January 1 1970, 00:00:00
     * GMT.</li>
     * <li>The next 4-bit field is reserved and must be 0.</li>
     * <li>The next 4-bit field is an index value to {@link #offsets
     * offsets[]} for the amount of daylight saving at the
     * transition. If this value is zero, it means that no daylight
     * saving, not the index value zero.</li>
     * <li>The least significant 4-bit field is an index value to
     * {@link #offsets offsets[]} for <em>total</em> GMT offset at the
     * transition.</li>
     * </ul>
     * If this time zone doesn't observe daylight saving time and has
     * never changed any GMT offsets in the past, this value is null.
     * @serial
     */
    private long[] transitions;

    /**
     * This array holds all unique offset values in
     * milliseconds. Index values to this array are stored in the
     * transitions array elements.
     * @serial
     */
    private int[] offsets;

    /**
     * SimpleTimeZone parameter values. It has to have either 8 for
     * {@link java.util.SimpleTimeZone#SimpleTimeZone(int, String,
     * int, int , int , int , int , int , int , int , int) the
     * 11-argument SimpleTimeZone constructor} or 10 for {@link
     * java.util.SimpleTimeZone#SimpleTimeZone(int, String, int, int,
     * int , int , int , int , int , int , int, int, int) the
     * 13-argument SimpleTimeZone constructor} parameters.
     * @serial
     */
    private int[] simpleTimeZoneParams;

    /**
     * True if the raw GMT offset value would change after the time
     * zone data has been generated; false, otherwise. The default
     * value is false.
     * @serial
     */
    private boolean willGMTOffsetChange = false;

    private static final long serialVersionUID = 2653134537216586139L;

    /**
     * A constructor.
     */
    public ZoneInfo() {
    }

    /**
     * A Constructor for CustomID.
     */
    public ZoneInfo(String ID, int rawOffset) {
        this(ID, rawOffset, 0, 0, null, null, null, false);
    }

    /**
     * Constructs a ZoneInfo instance.
     *
     * @param ID time zone name
     * @param rawOffset GMT offset in milliseconds
     * @param dstSavings daylight saving value in milliseconds or 0
     * (zero) if this time zone doesn't observe Daylight Saving Time.
     * @param checksum CRC32 value with all transitions table entry
     * values
     * @param transitions transition table
     * @param offsets offset value table
     * @param simpleTimeZoneParams parameter values for constructing
     * SimpleTimeZone
     * @param willGMTOffsetChange the value of willGMTOffsetChange
     */
    ZoneInfo(String ID,
	     int rawOffset,
	     int dstSavings,
	     int checksum,
	     long[] transitions,
	     int[] offsets,
	     int[] simpleTimeZoneParams,
	     boolean willGMTOffsetChange) {
	setID(ID);
	this.rawOffset = rawOffset;
	this.dstSavings = dstSavings;
	this.checksum = checksum;
	this.transitions = transitions;
	this.offsets = offsets;
	this.simpleTimeZoneParams = simpleTimeZoneParams;
	this.willGMTOffsetChange = willGMTOffsetChange;
    }

    /**
     * Returns the difference in milliseconds between local time and UTC
     * of given time, taking into account both the raw offset and the
     * effect of daylight savings.
     *
     * @param date the milliseconds in UTC
     * @return the milliseconds to add to UTC to get local wall time
     */
    public int getOffset(long date) {
	return getOffsets(date, null, false);
    }

    public int getOffsets(long utc, int[] offsets) {
	return getOffsets(utc, offsets, false);
    }

    public int getOffsetsByWall(long wall, int[] offsets) {
	return getOffsets(wall, offsets, true);
    }

    private int getOffsets(long date, int[] offsets, boolean isWall) {
	// if dst is never observed, there is no transition.
	if (transitions == null) {
	    int offset = getLastRawOffset();
	    if (offsets != null) {
		offsets[0] = offset;
		offsets[1] = 0;
	    }
	    return offset;
	}

	date -= rawOffsetDiff;
	int index = getTransitionIndex(date, isWall);

	// prior to the transition table, returns the raw offset.
	// should support LMT.
	if (index < 0) {
	    int offset = getLastRawOffset();
	    if (offsets != null) {
		offsets[0] = offset;
		offsets[1] = 0;
	    }
	    return offset;
	}

	if (index < transitions.length) {
	    long val = transitions[index];
	    int offset = this.offsets[(int)(val & OFFSET_MASK)] + rawOffsetDiff;
	    if (offsets != null) {
		int dst = (int)((val >>> DST_NSHIFT) & 0xfL);
		int save = (dst == 0) ? 0 : this.offsets[dst];
		offsets[0] = offset - save;
		offsets[1] = save;
	    }
	    return offset;
	}

	// beyond the transitions, delegate to SimpleTimeZone if there
	// is a rule; otherwise, return rawOffset.
	SimpleTimeZone tz = getLastRule();
	if (tz != null) {
	    int rawoffset = tz.getRawOffset();
	    long msec = date;
	    if (isWall) {
		msec -= rawOffset;
	    }
	    int dstoffset = tz.inDaylightTime(new Date(msec)) ? tz.getDSTSavings() : 0;
	    if (offsets != null) {
		offsets[0] = rawoffset;
		offsets[1] = dstoffset;
	    }
	    return rawoffset + dstoffset;
	}
	int offset = getLastRawOffset();
	if (offsets != null) {
	    offsets[0] = offset;
	    offsets[1] = 0;
	}
	return offset;
    }

    private final int getTransitionIndex(long date, boolean isWall) {
	int low = 0;
	int high = transitions.length - 1;

	while (low <= high) {
	    int mid = (low + high) / 2;
	    long val = transitions[mid];
	    long midVal = val >> TRANSITION_NSHIFT;
	    if (isWall) {
		midVal += offsets[(int)(val & OFFSET_MASK)]; // wall time
	    }

	    if (midVal < date) {
		low = mid + 1;
	    } else if (midVal > date) {
		high = mid - 1;
	    } else {
		return mid;
	    }
	}

	// if beyond the transitions, returns that index.
	if (low >= transitions.length) {
	    return low;
	}
	return low - 1;
    }

   /**
     * Returns the difference in milliseconds between local time and
     * UTC, taking into account both the raw offset and the effect of
     * daylight savings, for the specified date and time.  This method
     * assumes that the start and end month are distinct.  This method
     * assumes a Gregorian calendar for calculations.
     * <p>
     * <em>Note: In general, clients should use
     * {@link Calendar#ZONE_OFFSET Calendar.get(ZONE_OFFSET)} +
     * {@link Calendar#DST_OFFSET Calendar.get(DST_OFFSET)}
     * instead of calling this method.</em>
     *
     * @param era       The era of the given date. The value must be either
     *                  GregorianCalendar.AD or GregorianCalendar.BC.
     * @param year      The year in the given date.
     * @param month     The month in the given date. Month is 0-based. e.g.,
     *                  0 for January.
     * @param day       The day-in-month of the given date.
     * @param dayOfWeek The day-of-week of the given date.
     * @param millis    The milliseconds in day in <em>standard</em> local time.
     * @return The milliseconds to add to UTC to get local time.
     */
    public int getOffset(int era, int year, int month, int day,
			 int dayOfWeek, int milliseconds) {
	if (milliseconds < 0 || milliseconds >= Gregorian.ONE_DAY) {
	    throw new IllegalArgumentException();
	}

	if (era == GregorianCalendar.BC) { // BC
	    year = 1 - year;
	} else if (era != GregorianCalendar.AD) {
	    throw new IllegalArgumentException();
	}

	CalendarDate date = new CalendarDate(year, month, day);
	if (Gregorian.validate(date) == false) {
	    throw new IllegalArgumentException();
	}

	// bug-for-bug compatible argument checking
	if (dayOfWeek < GregorianCalendar.SUNDAY
	    || dayOfWeek > GregorianCalendar.SATURDAY) {
	    throw new IllegalArgumentException();
	}

	if (transitions == null) {
	    return getLastRawOffset();
	}

	long dateInMillis = Gregorian.dateToMillis(date) + milliseconds;
	dateInMillis -= (long) rawOffset; // make it UTC
	return getOffsets(dateInMillis, null, false);
    }

    /**
     * Sets the base time zone offset from GMT. This operation
     * modifies all the transitions of this ZoneInfo object, including
     * historical ones, if applicable.
     *
     * @param offsetMillis the base time zone offset to GMT.
     * @see getRawOffset
     */
    public synchronized void setRawOffset(int offsetMillis) {
	rawOffsetDiff = offsetMillis - rawOffset;
	if (lastRule != null) {
	    lastRule.setRawOffset(offsetMillis);
	}
    }

    /**
     * Returns the GMT offset of the current date. This GMT offset
     * value is not modified during Daylight Saving Time.
     *
     * @return the GMT offset value in milliseconds to add to UTC time
     * to get local standard time
     */
    public int getRawOffset() {
	if (!willGMTOffsetChange) {
	    return rawOffset + rawOffsetDiff;
	}

	int[] offsets = new int[2];
	getOffsets(System.currentTimeMillis(), offsets, false);
	return offsets[0];
    }

    private int getLastRawOffset() {
	return rawOffset + rawOffsetDiff;
    }

    /**
     * Queries if this time zone uses Daylight Saving Time in the last known rule.
     */
    public boolean useDaylightTime() {
	return (simpleTimeZoneParams != null);
    }

    /**
     * Queries if the specified date is in Daylight Saving Time.
     */
    public boolean inDaylightTime(Date date) {
	if (date == null) {
	    throw new NullPointerException();
	}

	if (transitions == null) {
	    return false;
	}

	long utc = date.getTime() - rawOffsetDiff;
	int index = getTransitionIndex(utc, false);

	// before transitions in the transition table
	if (index < 0) {
	    return false;
	}

	// the time is in the table range.
	if (index < transitions.length) {
	    return (transitions[index] & DST_MASK) != 0;
	}

	// beyond the transition table
	SimpleTimeZone tz = getLastRule();
	if (tz != null) {
	    return tz.inDaylightTime(date);
	}
	return false;
    }

    /**
     * Returns the amount of time in milliseconds that the clock is advanced
     * during daylight saving time is in effect in its last daylight saving time rule.
     *
     * @return the number of milliseconds the time is advanced with respect to
     * standard time when daylight saving time is in effect.
     */
    public int getDSTSavings() {
	return dstSavings;
    }

//    /**
//     * @return the last year in the transition table or -1 if this
//     * time zone doesn't observe any daylight saving time.
//     */
//    public int getMaxTransitionYear() {
//	if (transitions == null) {
//	    return -1;
//	}
//	long val = transitions[transitions.length - 1];
//	int offset = this.offsets[(int)(val & OFFSET_MASK)] + rawOffsetDiff;
//	val = (val >> TRANSITION_NSHIFT) + offset;
//	CalendarDate lastDate = Gregorian.getCalendarDate(val);
//	return lastDate.getYear();
//    }

    /**
     * Returns a string representation of this time zone.
     * @return the string
     */
    public String toString() {
	return getClass().getName() +
	    "[id=\"" + getID() + "\"" +
	    ",offset=" + getLastRawOffset() +
	    ",dstSavings=" + dstSavings +
	    ",useDaylight=" + useDaylightTime() +
	    ",transitions=" + ((transitions != null) ? transitions.length : 0) +
	    ",lastRule=" + getLastRuleInstance() +
	    "]";
    }

    /**
     * Gets all available IDs supported in the Java run-time.
     *
     * @return an array of time zone IDs.
     */
    public static String[] getAvailableIDs() {
	return ZoneInfoFile.getZoneIDs();
    }

    /**
     * Gets all available IDs that have the same value as the
     * specified raw GMT offset.
     *
     * @param rawOffset the GMT offset in milliseconds. This
     * value should not include any daylight saving time.
     *
     * @return an array of time zone IDs.
     */
    public static String[] getAvailableIDs(int rawOffset) {
	String[] result;
	ArrayList matched = new ArrayList();
	String[] IDs = getAvailableIDs();
	int[] rawOffsets = ZoneInfoFile.getRawOffsets();

    loop:
	for (int index = 0; index < rawOffsets.length; index++) {
	    if (rawOffsets[index] == rawOffset) {
		byte[] indices = ZoneInfoFile.getRawOffsetIndices();
		for (int i = 0; i < indices.length; i++) {
		    if (indices[i] == index) {
			matched.add(IDs[i++]);
			while (i < indices.length && indices[i] == index) {
			    matched.add(IDs[i++]);
			}
			break loop;
		    }
		}
	    }
	}
	result = new String[matched.size()];
	matched.toArray(result);

        return result;
    }

    /**
     * Gets the ZoneInfo for the given ID.
     *
     * @param ID the ID for a ZoneInfo. See TimeZone for detail.
     *
     * @return the specified ZoneInfo object or null if there is no
     * time zone of the ID.
     */
    public static TimeZone getTimeZone(String ID) {
	ZoneInfo zi = null;

	zi = ZoneInfoFile.getZoneInfo(ID);
	if (zi == null) {
	    // if we can't create an object for the ID, try aliases.
	    try {
		HashMap map = getAliasTable();
		String alias = ID;
		while ((alias = (String) map.get(alias)) != null) {
		    zi = ZoneInfoFile.getZoneInfo(alias);
		    if (zi != null) {
			zi.setID(ID);
			break;
		    }
		}
	    } catch (Exception e) {
		// ignore exceptions
	    }
	}
	return zi;
    }

    private transient SimpleTimeZone lastRule;

    /**
     * Returns a SimpleTimeZone object representing the last GMT
     * offset and DST schedule or null if this time zone doesn't
     * observe DST.
     */
    private synchronized SimpleTimeZone getLastRule() {
	if (lastRule == null) {
	    lastRule = getLastRuleInstance();
	}
	return lastRule;
    }

    /**
     * Returns a SimpleTimeZone object that represents the last
     * known daylight saving time rules.
     *
     * @return a SimpleTimeZone object or null if this time zone
     * doesn't observe DST.
     */
    public SimpleTimeZone getLastRuleInstance() {
	if (simpleTimeZoneParams == null) {
	    return null;
	}
	if (simpleTimeZoneParams.length == 10) {
	    return new SimpleTimeZone(getLastRawOffset(), getID(),
				      simpleTimeZoneParams[0],
				      simpleTimeZoneParams[1],
				      simpleTimeZoneParams[2],
				      simpleTimeZoneParams[3],
				      simpleTimeZoneParams[4],
				      simpleTimeZoneParams[5],
				      simpleTimeZoneParams[6],
				      simpleTimeZoneParams[7],
				      simpleTimeZoneParams[8],
				      simpleTimeZoneParams[9],
				      dstSavings);
	}
	return new SimpleTimeZone(getLastRawOffset(), getID(),
				  simpleTimeZoneParams[0],
				  simpleTimeZoneParams[1],
				  simpleTimeZoneParams[2],
				  simpleTimeZoneParams[3],
				  simpleTimeZoneParams[4],
				  simpleTimeZoneParams[5],
				  simpleTimeZoneParams[6],
				  simpleTimeZoneParams[7],
				  dstSavings);
    }

    /**
     * Returns a hash code value calculated from the GMT offset and
     * transitions.
     * @return a hash code of this time zone
     */
    public int hashCode() {
	return getLastRawOffset() ^ checksum;
    }

    /**
     * Compares the equity of two ZoneInfo objects.
     *
     * @param obj the object to be compared with
     * @return true if given object is same as this ZoneInfo object,
     * false otherwise.
     */
    public boolean equals(Object obj) {
	if (this == obj) {
	    return true;
	}
	if (!(obj instanceof ZoneInfo)) {
	    return false;
	}
	ZoneInfo that = (ZoneInfo) obj;
	return (getID().equals(that.getID())
		&& (getLastRawOffset() == that.getLastRawOffset())
		&& (checksum == that.checksum));
    }

    /**
     * Returns true if this zone has the same raw GMT offset value and
     * transition table as another zone info. If the specified
     * TimeZone object is not a ZoneInfo instance, this method returns
     * true if the specified TimeZone object has the same raw GMT
     * offset value with no daylight saving time.
     *
     * @param other the ZoneInfo object to be compared with
     * @return true if the given <code>TimeZone</code> has the same
     * GMT offset and transition information, false, otherwise.
     */
    public boolean hasSameRules(TimeZone other) {
	if (this == other) {
	    return true;
	}
	if (other == null) {
	    return false;
	}
	if (!(other instanceof ZoneInfo)) {
	    if (getRawOffset() != other.getRawOffset()) {
		return false;
	    }
	    // if both have the same raw offset and neither observes
	    // DST, they have the same rule.
	    if ((transitions == null)
		&& (useDaylightTime() == false)
		&& (other.useDaylightTime() == false)) {
		return true;
	    }
	    return false;
	}
	if (getLastRawOffset() != ((ZoneInfo)other).getLastRawOffset()) {
	    return false;
	}
	return (checksum == ((ZoneInfo)other).checksum);
    }

    private transient static SoftReference aliasTable;

    private synchronized static HashMap getAliasTable() {
	HashMap aliases = null;

	if (aliasTable != null) {
	    aliases = (HashMap) aliasTable.get();
	    if (aliases != null) {
		return aliases;
	    }
	}

	aliases = ZoneInfoFile.getZoneAliases();
	if (aliases != null) {
	    aliasTable = new SoftReference(aliases);
	}
	return aliases;
    }
}
