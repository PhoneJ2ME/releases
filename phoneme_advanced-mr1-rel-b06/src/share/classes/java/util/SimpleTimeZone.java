/*
 * Portions Copyright 2000-2006 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License version 2 for more details (a copy is included at
 * /legal/license.txt).
 * 
 * You should have received a copy of the GNU General Public
 * License version 2 along with this work; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 or visit www.sun.com if you need additional information or have
 * any questions.
 */

/*
 * (C) Copyright Taligent, Inc. 1996 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - All Rights Reserved
 *
 *   The original version of this source code and documentation is copyrighted
 * and owned by Taligent, Inc., a wholly-owned subsidiary of IBM. These
 * materials are provided under terms of a License Agreement between Taligent
 * and Sun. This technology is protected by multiple US and International
 * patents. This notice and attribution to Taligent may not be removed.
 *   Taligent is a registered trademark of Taligent, Inc.
 *
 */

package java.util;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import sun.util.calendar.CalendarDate;
import sun.util.calendar.Gregorian;

/**
 * <code>SimpleTimeZone</code> is a concrete subclass of <code>TimeZone</code>
 * that represents a time zone for use with a Gregorian calendar.
 * The class holds an offset from GMT, called <em>raw offset</em>, and start
 * and end rules for a daylight saving time schedule.  Since it only holds
 * single values for each, it cannot handle historical changes in the offset
 * from GMT and the daylight saving schedule, except that the {@link
 * #setStartYear setStartYear} method can specify the year when the daylight
 * saving time schedule starts in effect.
 * <p>
 * To construct a <code>SimpleTimeZone</code> with a daylight saving time
 * schedule, the schedule can be described with a set of rules,
 * <em>start-rule</em> and <em>end-rule</em>. A day when daylight saving time
 * starts or ends is specified by a combination of <em>month</em>,
 * <em>day-of-month</em>, and <em>day-of-week</em> values. The <em>month</em>
 * value is represented by a Calendar {@link Calendar#MONTH MONTH} field
 * value, such as {@link Calendar#MARCH}. The <em>day-of-week</em> value is
 * represented by a Calendar {@link Calendar#DAY_OF_WEEK DAY_OF_WEEK} value,
 * such as {@link Calendar#SUNDAY SUNDAY}. The meanings of value combinations
 * are as follows.
 *
 * <ul>
 * <li><b>Exact day of month</b><br>
 * To specify an exact day of month, set the <em>month</em> and
 * <em>day-of-month</em> to an exact value, and <em>day-of-week</em> to zero. For
 * example, to specify March 1, set the <em>month</em> to {@link Calendar#MARCH
 * MARCH}, <em>day-of-month</em> to 1, and <em>day-of-week</em> to 0.</li>
 *
 * <li><b>Day of week on or after day of month</b><br>
 * To specify a day of week on or after an exact day of month, set the
 * <em>month</em> to an exact month value, <em>day-of-month</em> to the day on
 * or after which the rule is applied, and <em>day-of-week</em> to a {@link
 * Calendar#DAY_OF_WEEK DAY_OF_WEEK} field value. For example, to specify the
 * second Sunday of April, set <em>month</em> to {@link Calendar#APRIL APRIL},
 * <em>day-of-month</em> to 8, and <em>day-of-week</em> to {@link
 * Calendar#SUNDAY SUNDAY}.</li>
 *
 * <li><b>Day of week on or before day of month</b><br>
 * To specify a day of the week on or before an exact day of the month, set
 * <em>day-of-month</em> and <em>day-of-week</em> to a negative value. For
 * example, to specify the last Wednesday on or before the 21st of March, set
 * <em>month</em> to {@link Calendar#MARCH MARCH}, <em>day-of-month</em> is -21
 * and <em>day-of-week</em> is {@link Calendar#WEDNESDAY -WEDNESDAY}. </li>
 *
 * <li><b>Last day-of-week of month</b><br>
 * To specify, the last day-of-week of the month, set <em>day-of-week</em> to a
 * {@link Calendar#DAY_OF_WEEK DAY_OF_WEEK} value and <em>day-of-month</em> to
 * -1. For example, to specify the last Sunday of October, set <em>month</em>
 * to {@link Calendar#OCTOBER OCTOBER}, <em>day-of-week</em> to {@link
 * Calendar#SUNDAY SUNDAY} and <em>day-of-month</em> to -1.  </li>
 *
 * </ul>
 * The time of the day at which daylight saving time starts or ends is
 * specified by a millisecond value within the day. There are three kinds of
 * <em>mode</em>s to specify the time: {@link #WALL_TIME}, {@link
 * #STANDARD_TIME} and {@link #UTC_TIME}. For example, if daylight
 * saving time ends
 * at 2:00 am in the wall clock time, it can be specified by 7200000
 * milliseconds in the {@link #WALL_TIME} mode. In this case, the wall clock time
 * for an <em>end-rule</em> means the same thing as the daylight time.
 * <p>
 * The following are examples of parameters for constructing time zone objects.
 * <pre><code>
 *      // Base GMT offset: -8:00
 *      // DST starts:      at 2:00am in standard time
 *      //                  on the first Sunday in April
 *      // DST ends:        at 2:00am in daylight time
 *      //                  on the last Sunday in October
 *      // Save:            1 hour
 *      SimpleTimeZone(-28800000,
 *                     "America/Los_Angeles",
 *                     Calendar.APRIL, 1, -Calendar.SUNDAY,
 *                     7200000,
 *                     Calendar.OCTOBER, -1, Calendar.SUNDAY,
 *                     7200000,
 *                     3600000)
 *
 *      // Base GMT offset: +1:00
 *      // DST starts:      at 1:00am in UTC time
 *      //                  on the last Sunday in March
 *      // DST ends:        at 1:00am in UTC time
 *      //                  on the last Sunday in October
 *      // Save:            1 hour
 *      SimpleTimeZone(3600000,
 *                     "Europe/Paris",
 *                     Calendar.MARCH, -1, Calendar.SUNDAY,
 *                     3600000, SimpleTimeZone.UTC_TIME,
 *                     Calendar.OCTOBER, -1, Calendar.SUNDAY,
 *                     3600000, SimpleTimeZone.UTC_TIME,
 *                     3600000)
 * </code></pre>
 * These parameter rules are also applicable to the set rule methods, such as
 * <code>setStartRule</code>.
 *
 * @since 1.1
 * @see      Calendar
 * @see      GregorianCalendar
 * @see      TimeZone
 * @version  1.45, 10/25/05
 * @author   David Goldsmith, Mark Davis, Chen-Lieh Huang, Alan Liu
 */

public class SimpleTimeZone extends TimeZone {
    /**
     * Constructs a SimpleTimeZone with the given base time zone offset from GMT
     * and time zone ID with no daylight saving time schedule.
     *
     * @param rawOffset  The base time zone offset in milliseconds to GMT.
     * @param ID         The time zone name that is given to this instance.
     */
    public SimpleTimeZone(int rawOffset, String ID)
    {
        this.rawOffset = rawOffset;
        setID (ID);
        dstSavings = millisPerHour; // In case user sets rules later
    }

    /**
     * Constructs a SimpleTimeZone with the given base time zone offset from
     * GMT, time zone ID, and rules for starting and ending the daylight
     * time.
     * Both <code>startTime</code> and <code>endTime</code> are specified to be
     * represented in the wall clock time. The amount of daylight saving is
     * assumed to be 3600000 milliseconds (i.e., one hour). This constructor is
     * equivalent to:
     * <pre><code>
     *     SimpleTimeZone(rawOffset,
     *                    ID,
     *                    startMonth,
     *                    startDay,
     *                    startDayOfWeek,
     *                    startTime,
     *                    SimpleTimeZone.{@link #WALL_TIME},
     *                    endMonth,
     *                    endDay,
     *                    endDayOfWeek,
     *                    endTime,
     *                    SimpleTimeZone.{@link #WALL_TIME},
     *                    3600000)
     * </code></pre>
     *
     * @param rawOffset       The given base time zone offset from GMT.
     * @param ID              The time zone ID which is given to this object.
     * @param startMonth      The daylight saving time starting month. Month is
     *                        a {@link Calendar#MONTH MONTH} field value (0-based. e.g., 0
     *                        for January).
     * @param startDay        The day of the month on which the daylight saving time starts.
     *                        See the class description for the special cases of this parameter.
     * @param startDayOfWeek  The daylight saving time starting day-of-week.
     *                        See the class description for the special cases of this parameter.
     * @param startTime       The daylight saving time starting time in local wall clock
     *                        time (in milliseconds within the day), which is local
     *                        standard time in this case.
     * @param endMonth        The daylight saving time ending month. Month is
     *                        a {@link Calendar#MONTH MONTH} field
     *                        value (0-based. e.g., 9 for October).
     * @param endDay          The day of the month on which the daylight saving time ends.
     *                        See the class description for the special cases of this parameter.
     * @param endDayOfWeek    The daylight saving time ending day-of-week.
     *                        See the class description for the special cases of this parameter.
     * @param endTime         The daylight saving ending time in local wall clock time,
     *                        (in milliseconds within the day) which is local daylight
     *                        time in this case.
     * @exception IllegalArgumentException if the month, day, dayOfWeek, or time
     * parameters are out of range for the start or end rule
     */
    public SimpleTimeZone(int rawOffset, String ID,
                          int startMonth, int startDay, int startDayOfWeek, int startTime,
                          int endMonth, int endDay, int endDayOfWeek, int endTime)
    {
        this(rawOffset, ID,
             startMonth, startDay, startDayOfWeek, startTime, WALL_TIME,
             endMonth, endDay, endDayOfWeek, endTime, WALL_TIME,
             millisPerHour);
    }

    /**
     * Constructs a SimpleTimeZone with the given base time zone offset from
     * GMT, time zone ID, and rules for starting and ending the daylight
     * time.
     * Both <code>startTime</code> and <code>endTime</code> are assumed to be
     * represented in the wall clock time. This constructor is equivalent to:
     * <pre><code>
     *     SimpleTimeZone(rawOffset,
     *                    ID,
     *                    startMonth,
     *                    startDay,
     *                    startDayOfWeek,
     *                    startTime,
     *                    SimpleTimeZone.{@link #WALL_TIME},
     *                    endMonth,
     *                    endDay,
     *                    endDayOfWeek,
     *                    endTime,
     *                    SimpleTimeZone.{@link #WALL_TIME},
     *                    dstSavings)
     * </code></pre>
     *
     * @param rawOffset       The given base time zone offset from GMT.
     * @param ID              The time zone ID which is given to this object.
     * @param startMonth      The daylight saving time starting month. Month is
     *                        a {@link Calendar#MONTH MONTH} field
     *                        value (0-based. e.g., 0 for January).
     * @param startDay        The day of the month on which the daylight saving time starts.
     *                        See the class description for the special cases of this parameter.
     * @param startDayOfWeek  The daylight saving time starting day-of-week.
     *                        See the class description for the special cases of this parameter.
     * @param startTime       The daylight saving time starting time in local wall clock
     *                        time, which is local standard time in this case.
     * @param endMonth        The daylight saving time ending month. Month is
     *                        a {@link Calendar#MONTH MONTH} field
     *                        value (0-based. e.g., 9 for October).
     * @param endDay          The day of the month on which the daylight saving time ends.
     *                        See the class description for the special cases of this parameter.
     * @param endDayOfWeek    The daylight saving time ending day-of-week.
     *                        See the class description for the special cases of this parameter.
     * @param endTime         The daylight saving ending time in local wall clock time,
     *                        which is local daylight time in this case.
     * @param dstSavings      The amount of time in milliseconds saved during
     *                        daylight saving time.
     * @exception IllegalArgumentException if the month, day, dayOfWeek, or time
     * parameters are out of range for the start or end rule
     * @since 1.2
     */
    public SimpleTimeZone(int rawOffset, String ID,
                          int startMonth, int startDay, int startDayOfWeek, int startTime,
                          int endMonth, int endDay, int endDayOfWeek, int endTime,
                          int dstSavings)
    {
        this(rawOffset, ID,
             startMonth, startDay, startDayOfWeek, startTime, WALL_TIME,
             endMonth, endDay, endDayOfWeek, endTime, WALL_TIME,
             dstSavings);
    }

    /**
     * Constructs a SimpleTimeZone with the given base time zone offset from
     * GMT, time zone ID, and rules for starting and ending the daylight
     * time.
     * This constructor takes the full set of the start and end rules
     * parameters, including modes of <code>startTime</code> and
     * <code>endTime</code>. The mode specifies either {@link #WALL_TIME wall
     * time} or {@link #STANDARD_TIME standard time} or {@link #UTC_TIME UTC
     * time}.
     *
     * @param rawOffset       The given base time zone offset from GMT.
     * @param ID              The time zone ID which is given to this object.
     * @param startMonth      The daylight saving time starting month. Month is
     *                        a {@link Calendar#MONTH MONTH} field
     *                        value (0-based. e.g., 0 for January).
     * @param startDay        The day of the month on which the daylight saving time starts.
     *                        See the class description for the special cases of this parameter.
     * @param startDayOfWeek  The daylight saving time starting day-of-week.
     *                        See the class description for the special cases of this parameter.
     * @param startTime       The daylight saving time starting time in the time mode
     *                        specified by <code>startTimeMode</code>.
     * @param startTimeMode   The mode of the start time specified by startTime.
     * @param endMonth        The daylight saving time ending month. Month is
     *                        a {@link Calendar#MONTH MONTH} field
     *                        value (0-based. e.g., 9 for October).
     * @param endDay          The day of the month on which the daylight saving time ends.
     *                        See the class description for the special cases of this parameter.
     * @param endDayOfWeek    The daylight saving time ending day-of-week.
     *                        See the class description for the special cases of this parameter.
     * @param endTime         The daylight saving ending time in time time mode
     *                        specified by <code>endTimeMode</code>.
     * @param endTimeMode     The mode of the end time specified by endTime
     * @param dstSavings      The amount of time in milliseconds saved during
     *                        daylight saving time.
     *
     * @exception IllegalArgumentException if the month, day, dayOfWeek, time more, or
     * time parameters are out of range for the start or end rule, or if a time mode
     * value is invalid.
     *
     * @see #WALL_TIME
     * @see #STANDARD_TIME
     * @see #UTC_TIME
     *
     * @since 1.4
     */
    public SimpleTimeZone(int rawOffset, String ID,
			  int startMonth, int startDay, int startDayOfWeek,
			  int startTime, int startTimeMode,
			  int endMonth, int endDay, int endDayOfWeek,
			  int endTime, int endTimeMode,
			  int dstSavings) {

	// Workaround fix for 4278609 (JCK failure)
	if (endMonth == Calendar.JANUARY && endDay == 1 &&
	    endDayOfWeek == 0 && endTime == 0 && endTimeMode == WALL_TIME &&
	    dstSavings > 0) {
	    endMonth = Calendar.DECEMBER;
	    endDay = 31;
	    endTime = (24 * 60 * 60 * 1000) - dstSavings;
	    endTimeMode = STANDARD_TIME;
	}

        setID(ID);
        this.rawOffset      = rawOffset;
        this.startMonth     = startMonth;
        this.startDay       = startDay;
        this.startDayOfWeek = startDayOfWeek;
        this.startTime      = startTime;
        this.startTimeMode  = startTimeMode;
        this.endMonth       = endMonth;
        this.endDay         = endDay;
        this.endDayOfWeek   = endDayOfWeek;
        this.endTime        = endTime;
        this.endTimeMode    = endTimeMode;
        this.dstSavings     = dstSavings;

        // this.useDaylight is set by decodeRules
        decodeRules();
        if (dstSavings <= 0) {
            throw new IllegalArgumentException("Illegal daylight saving value: " + dstSavings);
        }
    }

    /**
     * Sets the daylight saving time starting year.
     *
     * @param year  The daylight saving starting year.
     */
    public void setStartYear(int year)
    {
        startYear = year;
    }

    /**
     * Sets the daylight saving time start rule. For example, if daylight saving
     * time starts on the first Sunday in April at 2 am in local wall clock
     * time, you can set the start rule by calling:
     * <pre><code>setStartRule(Calendar.APRIL, 1, Calendar.SUNDAY, 2*60*60*1000);</code></pre>
     *
     * @param startMonth      The daylight saving time starting month. Month is
     *                        a {@link Calendar#MONTH MONTH} field
     *                        value (0-based. e.g., 0 for January).
     * @param startDay        The day of the month on which the daylight saving time starts.
     *                        See the class description for the special cases of this parameter.
     * @param startDayOfWeek  The daylight saving time starting day-of-week.
     *                        See the class description for the special cases of this parameter.
     * @param startTime       The daylight saving time starting time in local wall clock
     *                        time, which is local standard time in this case.
     * @exception IllegalArgumentException if the <code>startMonth</code>, <code>startDay</code>,
     * <code>startDayOfWeek</code>, or <code>startTime</code> parameters are out of range
     */
    public void setStartRule(int startMonth, int startDay, int startDayOfWeek, int startTime)
    {
        this.startMonth = startMonth;
        this.startDay = startDay;
        this.startDayOfWeek = startDayOfWeek;
        this.startTime = startTime;
        startTimeMode = WALL_TIME;
        // useDaylight = true; // Set by decodeRules
        decodeStartRule();
    }

    /**
     * Sets the daylight saving time start rule to a fixed date within a month.
     * This method is equivalent to:
     * <pre><code>setStartRule(startMonth, startDay, 0, startTime)</code></pre>
     *
     * @param startMonth      The daylight saving time starting month. Month is
     *                        a {@link Calendar#MONTH MONTH} field
     *                        value (0-based. e.g., 0 for January).
     * @param startDay        The day of the month on which the daylight saving time starts.
     * @param startTime       The daylight saving time starting time in local wall clock
     *                        time, which is local standard time in this case.
     *                        See the class description for the special cases of this parameter.
     * @exception IllegalArgumentException if the <code>startMonth</code>,
     * <code>startDayOfMonth</code>, or <code>startTime</code> parameters are out of range
     * @since 1.2
     */
    public void setStartRule(int startMonth, int startDay, int startTime) {
        setStartRule(startMonth, startDay, 0, startTime);
    }

    /**
     * Sets the daylight saving time start rule to a weekday before or after the given date within
     * a month, e.g., the first Monday on or after the 8th.
     *
     * @param startMonth      The daylight saving time starting month. Month is
     *                        a {@link Calendar#MONTH MONTH} field
     *                        value (0-based. e.g., 0 for January).
     * @param startDay        The day of the month on which the daylight saving time starts.
     * @param startDayOfWeek  The daylight saving time starting day-of-week.
     * @param startTime       The daylight saving time starting time in local wall clock
     *                        time, which is local standard time in this case.
     * @param after           If true, this rule selects the first <code>dayOfWeek</code> on or
     *                        <em>after</em> <code>dayOfMonth</code>.  If false, this rule
     *                        selects the last <code>dayOfWeek</code> on or <em>before</em>
     *                        <code>dayOfMonth</code>.
     * @exception IllegalArgumentException if the <code>startMonth</code>, <code>startDay</code>,
     * <code>startDayOfWeek</code>, or <code>startTime</code> parameters are out of range
     * @since 1.2
     */
    public void setStartRule(int startMonth, int startDay, int startDayOfWeek,
			     int startTime, boolean after)
    {
	// TODO: this method doesn't check the initial values of dayOfMonth or dsyOfWeek.
        if (after) {
            setStartRule(startMonth, startDay, -startDayOfWeek, startTime);
        } else {
            setStartRule(startMonth, -startDay, -startDayOfWeek, startTime);
	}
    }

    /**
     * Sets the daylight saving time end rule. For example, if daylight saving time
     * ends on the last Sunday in October at 2 am in wall clock time,
     * you can set the end rule by calling:
     * <code>setEndRule(Calendar.OCTOBER, -1, Calendar.SUNDAY, 2*60*60*1000);</code>
     *
     * @param endMonth        The daylight saving time ending month. Month is
     *                        a {@link Calendar#MONTH MONTH} field
     *                        value (0-based. e.g., 9 for October).
     * @param endDay          The day of the month on which the daylight saving time ends.
     *                        See the class description for the special cases of this parameter.
     * @param endDayOfWeek    The daylight saving time ending day-of-week.
     *                        See the class description for the special cases of this parameter.
     * @param endTime         The daylight saving ending time in local wall clock time,
     *                        (in milliseconds within the day) which is local daylight
     *                        time in this case.
     * @exception IllegalArgumentException if the <code>endMonth</code>, <code>endDay</code>,
     * <code>endDayOfWeek</code>, or <code>endTime</code> parameters are out of range
     */
    public void setEndRule(int endMonth, int endDay, int endDayOfWeek,
                           int endTime)
    {
        this.endMonth = endMonth;
        this.endDay = endDay;
        this.endDayOfWeek = endDayOfWeek;
        this.endTime = endTime;
        this.endTimeMode = WALL_TIME;
        // useDaylight = true; // Set by decodeRules
        decodeEndRule();
    }

    /**
     * Sets the daylight saving time end rule to a fixed date within a month.
     * This method is equivalent to:
     * <pre><code>setEndRule(endMonth, endDay, 0, endTime)</code></pre>
     *
     * @param endMonth        The daylight saving time ending month. Month is
     *                        a {@link Calendar#MONTH MONTH} field
     *                        value (0-based. e.g., 9 for October).
     * @param endDay          The day of the month on which the daylight saving time ends.
     * @param endTime         The daylight saving ending time in local wall clock time,
     *                        (in milliseconds within the day) which is local daylight
     *                        time in this case.
     * @exception IllegalArgumentException the <code>endMonth</code>, <code>endDay</code>,
     * or <code>endTime</code> parameters are out of range
     * @since 1.2
     */
    public void setEndRule(int endMonth, int endDay, int endTime)
    {
        setEndRule(endMonth, endDay, 0, endTime);
    }

    /**
     * Sets the daylight saving time end rule to a weekday before or after the given date within
     * a month, e.g., the first Monday on or after the 8th.
     *
     * @param endMonth        The daylight saving time ending month. Month is
     *                        a {@link Calendar#MONTH MONTH} field
     *                        value (0-based. e.g., 9 for October).
     * @param endDay          The day of the month on which the daylight saving time ends.
     * @param endDayOfWeek    The daylight saving time ending day-of-week.
     * @param endTime         The daylight saving ending time in local wall clock time,
     *                        (in milliseconds within the day) which is local daylight
     *                        time in this case.
     * @param after           If true, this rule selects the first <code>endDayOfWeek</code> on
     *                        or <em>after</em> <code>endDay</code>.  If false, this rule
     *                        selects the last <code>endDayOfWeek</code> on or before
     *                        <code>endDay</code> of the month.
     * @exception IllegalArgumentException the <code>endMonth</code>, <code>endDay</code>,
     * <code>endDayOfWeek</code>, or <code>endTime</code> parameters are out of range
     * @since 1.2
     */
    public void setEndRule(int endMonth, int endDay, int endDayOfWeek, int endTime, boolean after)
    {
        if (after) {
            setEndRule(endMonth, endDay, -endDayOfWeek, endTime);
        } else {
            setEndRule(endMonth, -endDay, -endDayOfWeek, endTime);
	}
    }

    /**
     * Returns the offset of this time zone from UTC at the given
     * time. If daylight saving time is in effect at the given time,
     * the offset value is adjusted with the amount of daylight
     * saving.
     *
     * @param date the time at which the time zone offset is found
     * @return the amount of time in milliseconds to add to UTC to get
     * local time.
     * @since 1.4
     */
    public int getOffset(long date) {
	return getOffsets(date, null);
    }

    /**
     * @see TimeZone#getOffsets
     */
    int getOffsets(long date, int[] offsets) {
	int offset;

    calc:
	{
	    if (!useDaylight) {
		offset = rawOffset;
		break calc;
	    }

	    // get standard local time
	    CalendarDate cdate = Gregorian.getCalendarDate(date + rawOffset);

	    int year = cdate.getYear();
	    // if it's BC, assume no DST.
	    if (year <= 0) {
		offset = rawOffset;
		break calc;
	    }
	    int month = cdate.getMonth();
	    int monthLength = staticMonthLength[month];
	    int prevMonthLength = staticMonthLength[(month + Calendar.DECEMBER) % 12];
	    if (Gregorian.isLeapYear(year)) {
		if (month == Calendar.FEBRUARY) { 
		    ++monthLength;
		} else if (month == Calendar.MARCH) {
		    ++prevMonthLength;
		}
	    }
	    offset = getOffset(GregorianCalendar.AD, year, month, cdate.getDate(), 
			       cdate.getDayOfWeek(), cdate.getTimeOfDay(),
			       monthLength, prevMonthLength);
	}

	if (offsets != null) {
	    offsets[0] = rawOffset;
	    offsets[1] = offset - rawOffset;
	}
	return offset;
    }

   /**
     * Returns the difference in milliseconds between local time and
     * UTC, taking into account both the raw offset and the effect of
     * daylight saving, for the specified date and time.  This method
     * assumes that the start and end month are distinct.  It also
     * uses a default {@link GregorianCalendar} object as its
     * underlying calendar, such as for determining leap years.  Do
     * not use the result of this method with a calendar other than a
     * default <code>GregorianCalendar</code>.
     *
     * <p><em>Note:  In general, clients should use
     * <code>Calendar.get(ZONE_OFFSET) + Calendar.get(DST_OFFSET)</code>
     * instead of calling this method.</em>
     *
     * @param era       The era of the given date.
     * @param year      The year in the given date.
     * @param month     The month in the given date. Month is 0-based. e.g.,
     *                  0 for January.
     * @param day       The day-in-month of the given date.
     * @param dayOfWeek The day-of-week of the given date.
     * @param millis    The milliseconds in day in <em>standard</em> local time.
     * @return          The milliseconds to add to UTC to get local time.
     * @exception       IllegalArgumentException the <code>era</code>,
     *			<code>month</code>, <code>day</code>, <code>dayOfWeek</code>,
     *			or <code>millis</code> parameters are out of range
     */
    public int getOffset(int era, int year, int month, int day, int dayOfWeek,
                         int millis)
    {
        // Check the month before indexing into staticMonthLength. This
	// duplicates the test that occurs in the 7-argument getOffset(),
	// however, this is unavoidable. We don't mind because this method, in
	// fact, should not be called; internal code should always call the
	// 7-argument getOffset(), and outside code should use Calendar.get(int
	// field) with fields ZONE_OFFSET and DST_OFFSET. We can't get rid of
	// this method because it's public API. - liu 8/10/98
        if (month < Calendar.JANUARY
            || month > Calendar.DECEMBER) {
            throw new IllegalArgumentException("Illegal month " + month);
        }

	int monthLength, prevMonthLength;
	if ((era == GregorianCalendar.AD) && Gregorian.isLeapYear(year)) {
	    monthLength = staticLeapMonthLength[month];
	    prevMonthLength = (month > 1) ? staticLeapMonthLength[month - 1] : 31;
	} else {
	    monthLength = staticMonthLength[month];
	    prevMonthLength = (month > 1) ? staticMonthLength[month - 1] : 31;
	}

	if (true) {
            /* Use this parameter checking code for normal operation.  Only one
             * of these two blocks should actually get compiled into the class
             * file.  */
            if ((era != GregorianCalendar.AD && era != GregorianCalendar.BC)
                || month < Calendar.JANUARY
                || month > Calendar.DECEMBER
                || day < 1
                || day > monthLength
                || dayOfWeek < Calendar.SUNDAY
                || dayOfWeek > Calendar.SATURDAY
                || millis < 0
                || millis >= millisPerDay) {
                throw new IllegalArgumentException();
            }
        } else {
            /* This parameter checking code is better for debugging, but
             * overkill for normal operation.  Only one of these two blocks
             * should actually get compiled into the class file.  */
            if (era != GregorianCalendar.AD && era != GregorianCalendar.BC) {
                throw new IllegalArgumentException("Illegal era " + era);
            }
            if (month < Calendar.JANUARY
                || month > Calendar.DECEMBER) {
                throw new IllegalArgumentException("Illegal month " + month);
            }
            if (day < 1 || day > monthLength) {
                throw new IllegalArgumentException("Illegal day " + day);
            }
            if (dayOfWeek < Calendar.SUNDAY
                || dayOfWeek > Calendar.SATURDAY) {
                throw new IllegalArgumentException("Illegal day of week " + dayOfWeek);
            }
            if (millis < 0 || millis >= millisPerDay) {
                throw new IllegalArgumentException("Illegal millis " + millis);
            }
        }

        return getOffset(era, year, month, day, dayOfWeek, millis,
			 monthLength, prevMonthLength);
    }

    /**
     * Gets offset, for current date, modified in case of
     * daylight saving time. This is the offset to add <em>to</em> UTC to get local time.
     * Gets the time zone offset, for current date, modified in case of daylight
     * saving time. This is the offset to add to UTC to get local time. Assume
     * that the start and end month are distinct.
     * @param era           The era of the given date.
     * @param year          The year in the given date.
     * @param month         The month in the given date. Month is 0-based. e.g.,
     *                      0 for January.
     * @param day           The day-in-month of the given date.
     * @param dayOfWeek     The day-of-week of the given date.
     * @param millis        The milliseconds in day in <em>standard</em> local time.
     * @param monthLength   The length of the given month in days.
     * @param prevMonthLength The length of the previous month in days.
     * @return              The offset to add to GMT to get local time.
     * @exception IllegalArgumentException the era, month, day,
     * dayOfWeek, millis, or monthLength parameters are out of range
     */
    private int getOffset(int era, int year, int month, int day, int dayOfWeek,
			  int millis, int monthLength, int prevMonthLength) {
        int result = rawOffset;

        // Bail out if we are before the onset of daylight saving time
        if (!useDaylight || year < startYear || era != GregorianCalendar.AD) {
	    return result;
	}

        // Check for southern hemisphere.  We assume that the start and end
        // month are different.
        boolean southern = (startMonth > endMonth);

        // Compare the date to the starting and ending rules.+1 = date>rule, -1
        // = date<rule, 0 = date==rule.
        int startCompare = compareToRule(month, monthLength, prevMonthLength,
                                         day, dayOfWeek, millis,
                                         startTimeMode == UTC_TIME ? -rawOffset : 0,
                                         startMode, startMonth, startDayOfWeek,
                                         startDay, startTime);
        int endCompare = 0;

        /* We don't always have to compute endCompare.  For many instances,
         * startCompare is enough to determine if we are in DST or not.  In the
         * northern hemisphere, if we are before the start rule, we can't have
         * DST.  In the southern hemisphere, if we are after the start rule, we
         * must have DST.  This is reflected in the way the next if statement
         * (not the one immediately following) short circuits. */
        if (southern != (startCompare >= 0)) {
            /* For the ending rule comparison, we add the dstSavings to the millis
             * passed in to convert them from standard to wall time.  We then must
             * normalize the millis to the range 0..millisPerDay-1. */
            endCompare = compareToRule(month, monthLength, prevMonthLength,
                                       day, dayOfWeek, millis,
                                       endTimeMode == WALL_TIME ? dstSavings :
                                        (endTimeMode == UTC_TIME ? -rawOffset : 0),
                                       endMode, endMonth, endDayOfWeek,
                                       endDay, endTime);
        }

        // Check for both the northern and southern hemisphere cases.  We
        // assume that in the northern hemisphere, the start rule is before the
        // end rule within the calendar year, and vice versa for the southern
        // hemisphere.
        if ((!southern && (startCompare >= 0 && endCompare < 0)) ||
		(southern && (startCompare >= 0 || endCompare < 0))) {
            result += dstSavings;
	}

        return result;
    }

    /**
     * Compares the given date in the year to the given rule and returns 1, 0,
     * or -1, depending on whether the date is after, equal to, or before the
     * rule date. The millis are compared directly against the ruleMillis, so
     * any standard-daylight adjustments must be handled by the caller.
     *
     * @return  1 if the date is after the rule date, -1 if the date is before
     *          the rule date, or 0 if the date is equal to the rule date.
     */
    private static int compareToRule(int month, int monthLen, int prevMonthLen,
                                     int dayOfMonth,
                                     int dayOfWeek, int millis, int millisDelta,
                                     int ruleMode, int ruleMonth, int ruleDayOfWeek,
                                     int ruleDay, int ruleMillis)
    {
        // Make adjustments for startTimeMode and endTimeMode
        millis += millisDelta;
        while (millis >= millisPerDay) {
            millis -= millisPerDay;
            ++dayOfMonth;
            dayOfWeek = 1 + (dayOfWeek % 7); // dayOfWeek is one-based
            if (dayOfMonth > monthLen) {
                dayOfMonth = 1;
                /* When incrementing the month, it is desirable to overflow
                 * from DECEMBER to DECEMBER+1, since we use the result to
                 * compare against a real month. Wraparound of the value
                 * leads to bug 4173604. */
                ++month;
            }
        }
        while (millis < 0) {
            millis += millisPerDay;
            --dayOfMonth;
            dayOfWeek = 1 + ((dayOfWeek+5) % 7); // dayOfWeek is one-based
            if (dayOfMonth < 1) {
                dayOfMonth = prevMonthLen;
                --month;
            }
        }
        
        if (month < ruleMonth) {
	    return -1;
	}
        if (month > ruleMonth) {
	    return 1;
	}

        int ruleDayOfMonth = 0;
        switch (ruleMode) {
        case DOM_MODE:
            ruleDayOfMonth = ruleDay;
            break;

        case DOW_IN_MONTH_MODE:
            // In this case ruleDay is the day-of-week-in-month
            if (ruleDay > 0) {
                ruleDayOfMonth = 1 + (ruleDay - 1) * 7 +
                    (7 + ruleDayOfWeek - (dayOfWeek - dayOfMonth + 1)) % 7;
            } else {
		// Assume ruleDay < 0 here
                ruleDayOfMonth = monthLen + (ruleDay + 1) * 7 -
                    (7 + (dayOfWeek + monthLen - dayOfMonth) - ruleDayOfWeek) % 7;
            }
            break;

        case DOW_GE_DOM_MODE:
            ruleDayOfMonth = ruleDay +
                (49 + ruleDayOfWeek - ruleDay - dayOfWeek + dayOfMonth) % 7;
            break;

        case DOW_LE_DOM_MODE:
            ruleDayOfMonth = ruleDay -
                (49 - ruleDayOfWeek + ruleDay + dayOfWeek - dayOfMonth) % 7;
            // Note at this point ruleDayOfMonth may be <1, although it will
            // be >=1 for well-formed rules.
            break;
        }

        if (dayOfMonth < ruleDayOfMonth) {
	    return -1;
	}
        if (dayOfMonth > ruleDayOfMonth) {
	    return 1;
	}

        if (millis < ruleMillis) {
	    return -1;
	}
	if (millis > ruleMillis) {
	    return 1;
	}
	return 0;
    }

    /**
     * Gets the GMT offset for this time zone.
     * @return the GMT offset value in milliseconds
     * @see #setRawOffset
     */
    public int getRawOffset()
    {
        // The given date will be taken into account while
        // we have the historical time zone data in place.
        return rawOffset;
    }

    /**
     * Sets the base time zone offset to GMT.
     * This is the offset to add to UTC to get local time.
     * @see #getRawOffset
     */
    public void setRawOffset(int offsetMillis)
    {
        this.rawOffset = offsetMillis;
    }

    /**
     * Sets the amount of time in milliseconds that the clock is advanced
     * during daylight saving time.
     * @param millisSavedDuringDST the number of milliseconds the time is
     * advanced with respect to standard time when the daylight saving time rules
     * are in effect. A positive number, typically one hour (3600000).
     * @see #getDSTSavings
     * @since 1.2
     */
    public void setDSTSavings(int millisSavedDuringDST) {
        if (millisSavedDuringDST <= 0) {
            throw new IllegalArgumentException("Illegal daylight saving value: "
					       + millisSavedDuringDST);
        }
        dstSavings = millisSavedDuringDST;
    }

    /**
     * Returns the amount of time in milliseconds that the clock is
     * advanced during daylight saving time.
     *
     * @return the number of milliseconds the time is advanced with
     * respect to standard time when the daylight saving rules are in
     * effect, or 0 (zero) if this time zone doesn't observe daylight
     * saving time.
     *
     * @see #setDSTSavings
     * @since 1.2
     */
    public int getDSTSavings() {
	if (useDaylight) {
	    return dstSavings;
	}
	return 0;
    }

    /**
     * Queries if this time zone uses daylight saving time.
     * @return true if this time zone uses daylight saving time;
     * false otherwise.
     */
    public boolean useDaylightTime()
    {
        return useDaylight;
    }

    /**
     * Queries if the given date is in daylight saving time.
     * @return true if daylight saving time is in effective at the
     * given date; false otherwise.
     */
    public boolean inDaylightTime(Date date)
    {
	return (getOffset(date.getTime()) != rawOffset);
    }

    /**
     * Returns a clone of this <code>SimpleTimeZone</code> instance.
     * @return a clone of this instance.
     */
    public Object clone()
    {
        return super.clone();
    }

    /**
     * Generates the hash code for the SimpleDateFormat object.
     * @return the hash code for this object
     */
    public synchronized int hashCode()
    {
        return startMonth ^ startDay ^ startDayOfWeek ^ startTime ^
            endMonth ^ endDay ^ endDayOfWeek ^ endTime ^ rawOffset;
    }

    /**
     * Compares the equality of two <code>SimpleTimeZone</code> objects.
     *
     * @param obj  The <code>SimpleTimeZone</code> object to be compared with.
     * @return     True if the given <code>obj</code> is the same as this
     *		   <code>SimpleTimeZone</code> object; false otherwise.
     */
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
	}
        if (!(obj instanceof SimpleTimeZone)) {
            return false;
	}

        SimpleTimeZone that = (SimpleTimeZone) obj;

        return getID().equals(that.getID()) &&
            hasSameRules(that);
    }

    /**
     * Returns true if this zone has the same rules and offset as another zone.
     * @param other the TimeZone object to be compared with
     * @return true if the given zone is a SimpleTimeZone and has the
     * same rules and offset as this one
     * @since 1.2
     */
    public boolean hasSameRules(TimeZone other) {
        if (this == other) {
	    return true;
	}
        if (!(other instanceof SimpleTimeZone)) {
	    return false;
	}
        SimpleTimeZone that = (SimpleTimeZone) other;
        return rawOffset == that.rawOffset &&
            useDaylight == that.useDaylight &&
            (!useDaylight
             // Only check rules if using DST
             || (dstSavings == that.dstSavings &&
                 startMode == that.startMode &&
                 startMonth == that.startMonth &&
                 startDay == that.startDay &&
                 startDayOfWeek == that.startDayOfWeek &&
                 startTime == that.startTime &&
                 startTimeMode == that.startTimeMode &&
                 endMode == that.endMode &&
                 endMonth == that.endMonth &&
                 endDay == that.endDay &&
                 endDayOfWeek == that.endDayOfWeek &&
                 endTime == that.endTime &&
                 endTimeMode == that.endTimeMode &&
                 startYear == that.startYear));
    }

    /**
     * Returns a string representation of this time zone.
     * @return a string representation of this time zone.
     */
    public String toString() {
        return getClass().getName() +
            "[id=" + getID() +
            ",offset=" + rawOffset +
            ",dstSavings=" + dstSavings +
            ",useDaylight=" + useDaylight +
            ",startYear=" + startYear +
            ",startMode=" + startMode +
            ",startMonth=" + startMonth +
            ",startDay=" + startDay +
            ",startDayOfWeek=" + startDayOfWeek +
            ",startTime=" + startTime +
            ",startTimeMode=" + startTimeMode +
            ",endMode=" + endMode +
            ",endMonth=" + endMonth +
            ",endDay=" + endDay +
            ",endDayOfWeek=" + endDayOfWeek +
            ",endTime=" + endTime +
            ",endTimeMode=" + endTimeMode + ']';
    }

    // =======================privates===============================

    /**
     * The month in which daylight saving time starts.  This value must be
     * between <code>Calendar.JANUARY</code> and
     * <code>Calendar.DECEMBER</code> inclusive.  This value must not equal
     * <code>endMonth</code>.
     * <p>If <code>useDaylight</code> is false, this value is ignored.
     * @serial
     */
    private int startMonth;

    /**
     * This field has two possible interpretations:
     * <dl>
     * <dt><code>startMode == DOW_IN_MONTH</code></dt>
     * <dd>
     * <code>startDay</code> indicates the day of the month of
     * <code>startMonth</code> on which daylight
     * saving time starts, from 1 to 28, 30, or 31, depending on the
     * <code>startMonth</code>.
     * </dd>
     * <dt><code>startMode != DOW_IN_MONTH</code></dt>
     * <dd>
     * <code>startDay</code> indicates which <code>startDayOfWeek</code> in th
     * month <code>startMonth</code> daylight
     * saving time starts on.  For example, a value of +1 and a
     * <code>startDayOfWeek</code> of <code>Calendar.SUNDAY</code> indicates the
     * first Sunday of <code>startMonth</code>.  Likewise, +2 would indicate the
     * second Sunday, and -1 the last Sunday.  A value of 0 is illegal.
     * </dd>
     * </ul>
     * <p>If <code>useDaylight</code> is false, this value is ignored.
     * @serial
     */
    private int startDay;

    /**
     * The day of the week on which daylight saving time starts.  This value
     * must be between <code>Calendar.SUNDAY</code> and
     * <code>Calendar.SATURDAY</code> inclusive.
     * <p>If <code>useDaylight</code> is false or
     * <code>startMode == DAY_OF_MONTH</code>, this value is ignored.
     * @serial
     */
    private int startDayOfWeek;

    /**
     * The time in milliseconds after midnight at which daylight saving
     * time starts.  This value is expressed as wall time, standard time,
     * or UTC time, depending on the setting of <code>startTimeMode</code>.
     * <p>If <code>useDaylight</code> is false, this value is ignored.
     * @serial
     */
    private int startTime;

    /**
     * The format of startTime, either WALL_TIME, STANDARD_TIME, or UTC_TIME.
     * @serial
     * @since 1.3
     */
    private int startTimeMode;

    /**
     * The month in which daylight saving time ends.  This value must be
     * between <code>Calendar.JANUARY</code> and
     * <code>Calendar.UNDECIMBER</code>.  This value must not equal
     * <code>startMonth</code>.
     * <p>If <code>useDaylight</code> is false, this value is ignored.
     * @serial
     */
    private int endMonth;

    /**
     * This field has two possible interpretations:
     * <dl>
     * <dt><code>endMode == DOW_IN_MONTH</code></dt>
     * <dd>
     * <code>endDay</code> indicates the day of the month of
     * <code>endMonth</code> on which daylight
     * saving time ends, from 1 to 28, 30, or 31, depending on the
     * <code>endMonth</code>.
     * </dd>
     * <dt><code>endMode != DOW_IN_MONTH</code></dt>
     * <dd>
     * <code>endDay</code> indicates which <code>endDayOfWeek</code> in th
     * month <code>endMonth</code> daylight
     * saving time ends on.  For example, a value of +1 and a
     * <code>endDayOfWeek</code> of <code>Calendar.SUNDAY</code> indicates the
     * first Sunday of <code>endMonth</code>.  Likewise, +2 would indicate the
     * second Sunday, and -1 the last Sunday.  A value of 0 is illegal.
     * </dd>
     * </ul>
     * <p>If <code>useDaylight</code> is false, this value is ignored.
     * @serial
     */
    private int endDay;

    /**
     * The day of the week on which daylight saving time ends.  This value
     * must be between <code>Calendar.SUNDAY</code> and
     * <code>Calendar.SATURDAY</code> inclusive.
     * <p>If <code>useDaylight</code> is false or
     * <code>endMode == DAY_OF_MONTH</code>, this value is ignored.
     * @serial
     */
    private int endDayOfWeek;

    /**
     * The time in milliseconds after midnight at which daylight saving
     * time ends.  This value is expressed as wall time, standard time,
     * or UTC time, depending on the setting of <code>endTimeMode</code>.
     * <p>If <code>useDaylight</code> is false, this value is ignored.
     * @serial
     */
    private int endTime;

    /**
     * The format of endTime, either WALL_TIME, STANDARD_TIME, or UTC_TIME.
     * @serial
     * @since 1.3
     */
    private int endTimeMode;

    /**
     * The year in which daylight saving time is first observed.  This is an AD
     * value.  If this value is less than 1 then daylight saving time is observed
     * for all AD years.
     * <p>If <code>useDaylight</code> is false, this value is ignored.
     * @serial
     */
    private int startYear;

    /**
     * The offset in milliseconds between this zone and GMT.  Negative offsets
     * are to the west of Greenwich.  To obtain local <em>standard</em> time,
     * add the offset to GMT time.  To obtain local wall time it may also be
     * necessary to add <code>dstSavings</code>.
     * @serial
     */
    private int rawOffset;

    /**
     * A boolean value which is true if and only if this zone uses daylight
     * saving time.  If this value is false, several other fields are ignored.
     * @serial
     */
    private boolean useDaylight=false; // indicate if this time zone uses DST

    private static final int millisPerHour = 60*60*1000;
    private static final int millisPerDay  = 24*millisPerHour;

    /**
     * This field was serialized in JDK 1.1, so we have to keep it that way
     * to maintain serialization compatibility. However, there's no need to
     * recreate the array each time we create a new time zone.
     * @serial An array of bytes containing the values {31, 28, 31, 30, 31, 30,
     * 31, 31, 30, 31, 30, 31}.  This is ignored as of the Java 2 platform v1.2, however, it must
     * be streamed out for compatibility with JDK 1.1.
     */
    private final byte monthLength[] = staticMonthLength;
    private final static byte staticMonthLength[] = {31,28,31,30,31,30,31,31,30,31,30,31};
    private final static byte staticLeapMonthLength[] = {31,29,31,30,31,30,31,31,30,31,30,31};

    /**
     * Variables specifying the mode of the start rule.  Takes the following
     * values:
     * <dl>
     * <dt><code>DOM_MODE</code></dt>
     * <dd>
     * Exact day of week; e.g., March 1.
     * </dd>
     * <dt><code>DOW_IN_MONTH_MODE</code></dt>    
     * <dd>
     * Day of week in month; e.g., last Sunday in March.
     * </dd>
     * <dt><code>DOW_GE_DOM_MODE</code></dt>
     * <dd>
     * Day of week after day of month; e.g., Sunday on or after March 15.
     * </dd>
     * <dt><code>DOW_LE_DOM_MODE</code></dt>
     * <dd>
     * Day of week before day of month; e.g., Sunday on or before March 15.
     * </dd>
     * </dl>
     * The setting of this field affects the interpretation of the
     * <code>startDay</code> field.
     * <p>If <code>useDaylight</code> is false, this value is ignored.
     * @serial
     * @since 1.1.4
     */
    private int startMode;

    /**
     * Variables specifying the mode of the end rule.  Takes the following
     * values:
     * <dl>
     * <dt><code>DOM_MODE</code></dt>
     * <dd>
     * Exact day of week; e.g., March 1.
     * </dd>
     * <dt><code>DOW_IN_MONTH_MODE</code></dt>    
     * <dd>
     * Day of week in month; e.g., last Sunday in March.
     * </dd>
     * <dt><code>DOW_GE_DOM_MODE</code></dt>
     * <dd>
     * Day of week after day of month; e.g., Sunday on or after March 15.
     * </dd>
     * <dt><code>DOW_LE_DOM_MODE</code></dt>
     * <dd>
     * Day of week before day of month; e.g., Sunday on or before March 15.
     * </dd>
     * </dl>
     * The setting of this field affects the interpretation of the
     * <code>endDay</code> field.
     * <p>If <code>useDaylight</code> is false, this value is ignored.
     * @serial
     * @since 1.1.4
     */
    private int endMode;

    /**
     * A positive value indicating the amount of time saved during DST in
     * milliseconds.
     * Typically one hour (3600000); sometimes 30 minutes (1800000).
     * <p>If <code>useDaylight</code> is false, this value is ignored.
     * @serial
     * @since 1.1.4
     */
    private int dstSavings;

    /**
     * Constants specifying values of startMode and endMode.
     */
    private static final int DOM_MODE          = 1; // Exact day of month, "Mar 1"
    private static final int DOW_IN_MONTH_MODE = 2; // Day of week in month, "lastSun"
    private static final int DOW_GE_DOM_MODE   = 3; // Day of week after day of month, "Sun>=15"
    private static final int DOW_LE_DOM_MODE   = 4; // Day of week before day of month, "Sun<=21"

    /**
     * Constant for a mode of start or end time specified as wall clock
     * time.  Wall clock time is standard time for the onset rule, and
     * daylight time for the end rule.
     * @since 1.4
     */
    public static final int WALL_TIME = 0; // Zero for backward compatibility

    /**
     * Constant for a mode of start or end time specified as standard time.
     * @since 1.4
     */
    public static final int STANDARD_TIME = 1;

    /**
     * Constant for a mode of start or end time specified as UTC. European
     * Union rules are specified as UTC time, for example.
     * @since 1.4
     */
    public static final int UTC_TIME = 2;

    // Proclaim compatibility with 1.1
    static final long serialVersionUID = -403250971215465050L;

    // the internal serial version which says which version was written
    // - 0 (default) for version up to JDK 1.1.3
    // - 1 for version from JDK 1.1.4, which includes 3 new fields
    // - 2 for JDK 1.3, which includes 2 new fields
    static final int currentSerialVersion = 2;

    /**
     * The version of the serialized data on the stream.  Possible values:
     * <dl>
     * <dt><b>0</b> or not present on stream</dt>
     * <dd>
     * JDK 1.1.3 or earlier.
     * </dd>
     * <dt><b>1</b></dt>
     * <dd>
     * JDK 1.1.4 or later.  Includes three new fields: <code>startMode</code>,
     * <code>endMode</code>, and <code>dstSavings</code>.
     * </dd>
     * <dt><b>2</b></dt>
     * <dd>
     * JDK 1.3 or later.  Includes two new fields: <code>startTimeMode</code>
     * and <code>endTimeMode</code>.
     * </dd>
     * </dl>
     * When streaming out this class, the most recent format
     * and the highest allowable <code>serialVersionOnStream</code>
     * is written.
     * @serial
     * @since 1.1.4
     */
    private int serialVersionOnStream = currentSerialVersion;

    //----------------------------------------------------------------------
    // Rule representation
    //
    // We represent the following flavors of rules:
    //       5        the fifth of the month
    //       lastSun  the last Sunday in the month
    //       lastMon  the last Monday in the month
    //       Sun>=8   first Sunday on or after the eighth
    //       Sun<=25  last Sunday on or before the 25th
    // This is further complicated by the fact that we need to remain
    // backward compatible with the 1.1.  Finally, we need to minimize
    // API changes.  In order to satisfy these requirements, we support
    // three representation systems, and we translate between them.
    //
    // INTERNAL REPRESENTATION
    // This is the format SimpleTimeZone objects take after construction or
    // streaming in is complete.  Rules are represented directly, using an
    // unencoded format.  We will discuss the start rule only below; the end
    // rule is analogous.
    //   startMode      Takes on enumerated values DAY_OF_MONTH,
    //                  DOW_IN_MONTH, DOW_AFTER_DOM, or DOW_BEFORE_DOM.
    //   startDay       The day of the month, or for DOW_IN_MONTH mode, a
    //                  value indicating which DOW, such as +1 for first,
    //                  +2 for second, -1 for last, etc.
    //   startDayOfWeek The day of the week.  Ignored for DAY_OF_MONTH.
    //
    // ENCODED REPRESENTATION
    // This is the format accepted by the constructor and by setStartRule()
    // and setEndRule().  It uses various combinations of positive, negative,
    // and zero values to encode the different rules.  This representation
    // allows us to specify all the different rule flavors without altering
    // the API.
    //   MODE              startMonth    startDay    startDayOfWeek
    //   DOW_IN_MONTH_MODE >=0           !=0         >0
    //   DOM_MODE          >=0           >0          ==0
    //   DOW_GE_DOM_MODE   >=0           >0          <0
    //   DOW_LE_DOM_MODE   >=0           <0          <0
    //   (no DST)          don't care    ==0         don't care
    //
    // STREAMED REPRESENTATION
    // We must retain binary compatibility with the 1.1.  The 1.1 code only
    // handles DOW_IN_MONTH_MODE and non-DST mode, the latter indicated by the
    // flag useDaylight.  When we stream an object out, we translate into an
    // approximate DOW_IN_MONTH_MODE representation so the object can be parsed
    // and used by 1.1 code.  Following that, we write out the full
    // representation separately so that contemporary code can recognize and
    // parse it.  The full representation is written in a "packed" format,
    // consisting of a version number, a length, and an array of bytes.  Future
    // versions of this class may specify different versions.  If they wish to
    // include additional data, they should do so by storing them after the
    // packed representation below.
    //----------------------------------------------------------------------

    /**
     * Given a set of encoded rules in startDay and startDayOfMonth, decode
     * them and set the startMode appropriately.  Do the same for endDay and
     * endDayOfMonth.  Upon entry, the day of week variables may be zero or
     * negative, in order to indicate special modes.  The day of month
     * variables may also be negative.  Upon exit, the mode variables will be
     * set, and the day of week and day of month variables will be positive.
     * This method also recognizes a startDay or endDay of zero as indicating
     * no DST.
     */
    private void decodeRules()
    {
        decodeStartRule();
        decodeEndRule();
    }

    /**
     * Decode the start rule and validate the parameters.  The parameters are
     * expected to be in encoded form, which represents the various rule modes
     * by negating or zeroing certain values.  Representation formats are:
     * <p>
     * <pre>
     *            DOW_IN_MONTH  DOM    DOW>=DOM  DOW<=DOM  no DST
     *            ------------  -----  --------  --------  ----------
     * month       0..11        same    same      same     don't care
     * day        -5..5         1..31   1..31    -1..-31   0
     * dayOfWeek   1..7         0      -1..-7    -1..-7    don't care
     * time        0..ONEDAY    same    same      same     don't care
     * </pre>
     * The range for month does not include UNDECIMBER since this class is
     * really specific to GregorianCalendar, which does not use that month.
     * The range for time includes ONEDAY (vs. ending at ONEDAY-1) because the
     * end rule is an exclusive limit point.  That is, the range of times that
     * are in DST include those >= the start and < the end.  For this reason,
     * it should be possible to specify an end of ONEDAY in order to include the
     * entire day.  Although this is equivalent to time 0 of the following day,
     * it's not always possible to specify that, for example, on December 31.
     * While arguably the start range should still be 0..ONEDAY-1, we keep
     * the start and end ranges the same for consistency.
     */
    private void decodeStartRule() {
        useDaylight = (startDay != 0) && (endDay != 0);
        if (startDay != 0) {
            if (startMonth < Calendar.JANUARY || startMonth > Calendar.DECEMBER) {
                throw new IllegalArgumentException(
                        "Illegal start month " + startMonth);
            }
            if (startTime < 0 || startTime >= millisPerDay) {
                throw new IllegalArgumentException(
                        "Illegal start time " + startTime);
            }
            if (startDayOfWeek == 0) {
                startMode = DOM_MODE;
            } else {
                if (startDayOfWeek > 0) {
                    startMode = DOW_IN_MONTH_MODE;
                } else {
                    startDayOfWeek = -startDayOfWeek;
                    if (startDay > 0) {
                        startMode = DOW_GE_DOM_MODE;
                    } else {
                        startDay = -startDay;
                        startMode = DOW_LE_DOM_MODE;
                    }
                }
                if (startDayOfWeek > Calendar.SATURDAY) {
                    throw new IllegalArgumentException(
                           "Illegal start day of week " + startDayOfWeek);
                }
            }
            if (startMode == DOW_IN_MONTH_MODE) {
                if (startDay < -5 || startDay > 5) {
                    throw new IllegalArgumentException(
                            "Illegal start day of week in month " + startDay);
                }
            } else if (startDay < 1 || startDay > staticMonthLength[startMonth]) {
                throw new IllegalArgumentException(
                        "Illegal start day " + startDay);
            }
        }
    }

    /**
     * Decode the end rule and validate the parameters.  This method is exactly
     * analogous to decodeStartRule().
     * @see decodeStartRule
     */
    private void decodeEndRule() {
        useDaylight = (startDay != 0) && (endDay != 0);
        if (endDay != 0) {
            if (endMonth < Calendar.JANUARY || endMonth > Calendar.DECEMBER) {
                throw new IllegalArgumentException(
                        "Illegal end month " + endMonth);
            }
            if (endTime < 0 || endTime >= millisPerDay) {
                throw new IllegalArgumentException(
                        "Illegal end time " + endTime);
            }
            if (endDayOfWeek == 0) {
                endMode = DOM_MODE;
            } else {
                if (endDayOfWeek > 0) {
                    endMode = DOW_IN_MONTH_MODE;
                } else {
                    endDayOfWeek = -endDayOfWeek;
                    if (endDay > 0) {
                        endMode = DOW_GE_DOM_MODE;
                    } else {
                        endDay = -endDay;
                        endMode = DOW_LE_DOM_MODE;
                    }
                }
                if (endDayOfWeek > Calendar.SATURDAY) {
                    throw new IllegalArgumentException(
                           "Illegal end day of week " + endDayOfWeek);
                }
            }
            if (endMode == DOW_IN_MONTH_MODE) {
                if (endDay < -5 || endDay > 5) {
                    throw new IllegalArgumentException(
                            "Illegal end day of week in month " + endDay);
                }
            } else if (endDay < 1 || endDay > staticMonthLength[endMonth]) {
                throw new IllegalArgumentException(
                        "Illegal end day " + endDay);
            }
        }
    }

    /**
     * Make rules compatible to 1.1 code.  Since 1.1 code only understands
     * day-of-week-in-month rules, we must modify other modes of rules to their
     * approximate equivalent in 1.1 terms.  This method is used when streaming
     * out objects of this class.  After it is called, the rules will be modified,
     * with a possible loss of information.  startMode and endMode will NOT be
     * altered, even though semantically they should be set to DOW_IN_MONTH_MODE,
     * since the rule modification is only intended to be temporary.
     */
    private void makeRulesCompatible()
    {
        switch (startMode) {
        case DOM_MODE:
            startDay = 1 + (startDay / 7);
            startDayOfWeek = Calendar.SUNDAY;
            break;

        case DOW_GE_DOM_MODE:
            // A day-of-month of 1 is equivalent to DOW_IN_MONTH_MODE
            // that is, Sun>=1 == firstSun.
            if (startDay != 1) {
                startDay = 1 + (startDay / 7);
	    }
            break;

        case DOW_LE_DOM_MODE:
            if (startDay >= 30) {
                startDay = -1;
            } else {
                startDay = 1 + (startDay / 7);
	    }
            break;
        }

        switch (endMode) {
        case DOM_MODE:
            endDay = 1 + (endDay / 7);
            endDayOfWeek = Calendar.SUNDAY;
            break;

        case DOW_GE_DOM_MODE:
            // A day-of-month of 1 is equivalent to DOW_IN_MONTH_MODE
            // that is, Sun>=1 == firstSun.
            if (endDay != 1) {
                endDay = 1 + (endDay / 7);
	    }
            break;

        case DOW_LE_DOM_MODE:
            if (endDay >= 30) {
                endDay = -1;
            } else {
                endDay = 1 + (endDay / 7);
	    }
            break;
        }

        /*
	 * Adjust the start and end times to wall time.  This works perfectly
         * well unless it pushes into the next or previous day.  If that
         * happens, we attempt to adjust the day rule somewhat crudely.  The day
         * rules have been forced into DOW_IN_MONTH mode already, so we change
         * the day of week to move forward or back by a day.  It's possible to
         * make a more refined adjustment of the original rules first, but in
         * most cases this extra effort will go to waste once we adjust the day
         * rules anyway.
	 */
        switch (startTimeMode) {
        case UTC_TIME:
            startTime += rawOffset;
            break;
        }
        while (startTime < 0) {
            startTime += millisPerDay;
            startDayOfWeek = 1 + ((startDayOfWeek+5) % 7); // Back 1 day
        }
        while (startTime >= millisPerDay) {
            startTime -= millisPerDay;
            startDayOfWeek = 1 + (startDayOfWeek % 7); // Forward 1 day
        }

        switch (endTimeMode) {
        case UTC_TIME:
            endTime += rawOffset + dstSavings;
            break;
        case STANDARD_TIME:
            endTime += dstSavings;
        }
        while (endTime < 0) {
            endTime += millisPerDay;
            endDayOfWeek = 1 + ((endDayOfWeek+5) % 7); // Back 1 day
        }
        while (endTime >= millisPerDay) {
            endTime -= millisPerDay;
            endDayOfWeek = 1 + (endDayOfWeek % 7); // Forward 1 day
        }
    }

    /**
     * Pack the start and end rules into an array of bytes.  Only pack
     * data which is not preserved by makeRulesCompatible.
     */
    private byte[] packRules()
    {
        byte[] rules = new byte[6];
        rules[0] = (byte)startDay;
        rules[1] = (byte)startDayOfWeek;
        rules[2] = (byte)endDay;
        rules[3] = (byte)endDayOfWeek;

        // As of serial version 2, include time modes
        rules[4] = (byte)startTimeMode;
        rules[5] = (byte)endTimeMode;

        return rules;
    }

    /**
     * Given an array of bytes produced by packRules, interpret them
     * as the start and end rules.
     */
    private void unpackRules(byte[] rules)
    {
        startDay       = rules[0];
        startDayOfWeek = rules[1];
        endDay         = rules[2];
        endDayOfWeek   = rules[3];

        // As of serial version 2, include time modes
        if (rules.length >= 6) {
            startTimeMode = rules[4];
            endTimeMode   = rules[5];
        }
    }

    /**
     * Pack the start and end times into an array of bytes.  This is required
     * as of serial version 2.
     */
    private int[] packTimes() {
        int[] times = new int[2];
        times[0] = startTime;
        times[1] = endTime;
        return times;
    }

    /**
     * Unpack the start and end times from an array of bytes.  This is required
     * as of serial version 2.
     */
    private void unpackTimes(int[] times) {
        startTime = times[0];
        endTime = times[1];
    }

    /**
     * Save the state of this object to a stream (i.e., serialize it).
     *
     * @serialData We write out two formats, a JDK 1.1 compatible format, using
     * <code>DOW_IN_MONTH_MODE</code> rules, in the required section, followed
     * by the full rules, in packed format, in the optional section.  The
     * optional section will be ignored by JDK 1.1 code upon stream in.
     * <p> Contents of the optional section: The length of a byte array is
     * emitted (int); this is 4 as of this release. The byte array of the given
     * length is emitted. The contents of the byte array are the true values of
     * the fields <code>startDay</code>, <code>startDayOfWeek</code>,
     * <code>endDay</code>, and <code>endDayOfWeek</code>.  The values of these
     * fields in the required section are approximate values suited to the rule
     * mode <code>DOW_IN_MONTH_MODE</code>, which is the only mode recognized by
     * JDK 1.1.
     */
    private void writeObject(ObjectOutputStream stream)
         throws IOException
    {
        // Construct a binary rule
        byte[] rules = packRules();
        int[] times = packTimes();

        // Convert to 1.1 rules.  This step may cause us to lose information.
        makeRulesCompatible();

        // Write out the 1.1 rules
        stream.defaultWriteObject();

        // Write out the binary rules in the optional data area of the stream.
        stream.writeInt(rules.length);
        stream.write(rules);
        stream.writeObject(times);

        // Recover the original rules.  This recovers the information lost
        // by makeRulesCompatible.
        unpackRules(rules);
        unpackTimes(times);
    }

    /**
     * Reconstitute this object from a stream (i.e., deserialize it).
     *
     * We handle both JDK 1.1
     * binary formats and full formats with a packed byte array.
     */
    private void readObject(ObjectInputStream stream)
         throws IOException, ClassNotFoundException
    {
        stream.defaultReadObject();

        if (serialVersionOnStream < 1) {
            // Fix a bug in the 1.1 SimpleTimeZone code -- namely,
            // startDayOfWeek and endDayOfWeek were usually uninitialized.  We can't do
            // too much, so we assume SUNDAY, which actually works most of the time.
            if (startDayOfWeek == 0) {
		startDayOfWeek = Calendar.SUNDAY;
	    }
            if (endDayOfWeek == 0) {
		endDayOfWeek = Calendar.SUNDAY;
	    }

            // The variables dstSavings, startMode, and endMode are post-1.1, so they
            // won't be present if we're reading from a 1.1 stream.  Fix them up.
            startMode = endMode = DOW_IN_MONTH_MODE;
            dstSavings = millisPerHour;
        } else {
            // For 1.1.4, in addition to the 3 new instance variables, we also
            // store the actual rules (which have not be made compatible with 1.1)
            // in the optional area.  Read them in here and parse them.
            int length = stream.readInt();
            byte[] rules = new byte[length];
            stream.readFully(rules);
            unpackRules(rules);
        }

        if (serialVersionOnStream >= 2) {
            int[] times = (int[]) stream.readObject();
            unpackTimes(times);
        }

        serialVersionOnStream = currentSerialVersion;
    }
}