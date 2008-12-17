/*
 * @(#)Zoneinfo.java	1.7 06/10/10
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

package sun.tools.javazic;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * Zoneinfo provides javazic compiler front-end functionality.
 * @since 1.4
 */
class Zoneinfo {

    private static final int minYear = 1900;
    private static final int maxYear = 2037;
    private static int startYear = minYear;
    private static int endYear = maxYear;

    /**
     * True if javazic should generate a list of SimpleTimeZone
     * instantiations for the SimpleTimeZone-based time zone support.
     */
    static boolean isYearForTimeZoneDataSpecified = false;

    private HashMap zones;
    private HashMap rules;
    private HashMap aliases;

    /**
     * Constracts a Zoneinfo.
     */
    Zoneinfo() {
	zones = new HashMap();
	rules = new HashMap();
	aliases = new HashMap();
    }

    /**
     * Adds the given zone to the list of Zones.
     * @param zone Zone to be added to the list.
     */
    void add(Zone zone) {
	String name = zone.getName();
	zones.put(name, zone);
    }

    /**
     * Adds the given rule to the list of Rules.
     * @param rule Rule to be added to the list.
     */
    void add(Rule rule) {
	String name = rule.getName();
	rules.put(name, rule);
    }

    /**
     * Puts the specifid name pair to the alias table.
     * @param name1 an alias time zone name
     * @param name2 the real time zone of the alias name
     */
    void putAlias(String name1, String name2) {
	aliases.put(name1, name2);
    }

    /**
     * Sets the given year for SimpleTimeZone list output.
     * This method is called when the -S option is specified.
     * @param year the year for which SimpleTimeZone list should be generated
     */
    static void setYear(int year) {
	setStartYear(year);
	setEndYear(year);
	isYearForTimeZoneDataSpecified = true;
    }

    /**
     * Sets the start year.
     * @param year the start year value
     * @throws IllegalArgumentException if the specified year value is
     * smaller than the minimum year or greater than the end year.
     */
    static void setStartYear(int year) {
	if (year < minYear || year > endYear) {
	    throw new IllegalArgumentException("invalid start year specified: " + year);
	}
	startYear = year;
    }

    /**
     * @return the start year value
     */
    static int getStartYear() {
	return startYear;
    }

    /**
     * Sets the end year.
     * @param year the end year value
     * @throws IllegalArgumentException if the specified year value is
     * smaller than the start year or greater than the maximum year.
     */
    static void setEndYear(int year) {
	if (year < startYear || year > maxYear) {
	    throw new IllegalArgumentException();
	}
	endYear = year;
    }

    /**
     * @return the end year value
     */
    static int getEndYear() {
	return endYear;
    }

    /**
     * @return the minimum year value
     */
    static int getMinYear() {
	return minYear;
    }

    /**
     * @return the maximum year value
     */
    static int getMaxYear() {
	return maxYear;
    }

    /**
     * @return the alias table
     */
    HashMap getAliases() {
	return(aliases);
    }

    /**
     * @return the Zone list
     */
    HashMap getZones() {
	return(zones);
    }

    /**
     * @return a Zone specified by name.
     * @param name a zone name
     */
    Zone getZone(String name) {
	return (Zone) zones.get(name);
    }

    /**
     * @return a Rule specified by name.
     * @param name a rule name
     */
    Rule getRule(String name) {
	return (Rule) rules.get(name);
    }

    /**
     * @return an interator of the Zone list
     */
    Iterator getZoneIterator() {
	return zones.keySet().iterator();
    }

    private static String line;

    private static int lineNum;

    /**
     * Parses the specified time zone data file and creates a Zoneinfo
     * that has all Rules, Zones and Links (aliases) information.
     * @param fname the time zone data file name
     * @return a Zoneinfo object
     */
    static Zoneinfo parse(String fname) {
	BufferedReader in = null;
	try {
	    FileReader fr = new FileReader(fname);
	    in = new BufferedReader(fr);
	} catch (FileNotFoundException e) {
	    panic("can't open file: "+fname);
	}
	Zoneinfo zi = new Zoneinfo();
	boolean continued = false;
	Zone zone = null;
	String l;

	try {
	    while ((line = in.readLine()) != null) {
		lineNum++;
		// skip blank and comment lines
		if (line.length() == 0 || line.charAt(0) == '#') {
		    continue;
		}

		// trim trailing comments
		int rindex = line.lastIndexOf('#');
		if (rindex != -1) {
		    // take the data part of the line
		    l = line.substring(0, rindex);
		} else {
		    l = line;
		}

		StringTokenizer tokens = new StringTokenizer(l);
		if (!tokens.hasMoreTokens()) {
		    continue;
		}
		String token = tokens.nextToken();

		if (continued || "Zone".equals(token)) {
		    if (zone == null) {
			if (!tokens.hasMoreTokens()) {
			    panic("syntax error: zone no more token");
			}
			token = tokens.nextToken();
			// if the zone name is in "GMT+hh" or "GMT-hh"
			// format, ignore it due to spec conflict.
			if (token.startsWith("GMT+") || token.startsWith("GMT-")) {
			    continue;
			}
			zone = new Zone(token);
		    } else {
			// no way to push the current token back...
			tokens = new StringTokenizer(l);
		    }

		    ZoneRec zrec = ZoneRec.parse(tokens);
		    zrec.setLine(line);
		    zone.add(zrec);
		    if ((continued = zrec.hasUntil()) == false) {
			if (Zone.isTargetZone(zone.getName())) {
			    // zone.resolve(zi);
			    zi.add(zone);
			}
			zone = null;
		    }
		} else if ("Rule".equals(token)) {
		    if (!tokens.hasMoreTokens()) {
			panic("syntax error: rule no more token");
		    }
		    token = tokens.nextToken();
		    Rule rule = zi.getRule(token);
		    if (rule == null) {
			rule = new Rule(token);
			zi.add(rule);
		    }
		    RuleRec rrec = RuleRec.parse(tokens);
		    rrec.setLine(line);
		    rule.add(rrec);
		} else if ("Link".equals(token)) {
		    // Link <newname> <oldname>
		    try {
			String name1 = tokens.nextToken();
			String name2 = tokens.nextToken();

			// if the zone name is in "GMT+hh" or "GMT-hh"
			// format, ignore it due to spec
			// conflict. Also, ignore "ROC" for PC-ness.
			if (name2.startsWith("GMT+") || name2.startsWith("GMT-")
			    || "ROC".equals(name2)) {
			    continue;
			} 
			zi.putAlias(name2, name1);
		    } catch (Exception e) {
			panic("syntax error: no more token for Link");
		    }
		}
	    }
	    in.close();
	} catch (IOException ex) {
	    panic("IO error: " + ex.getMessage());
	}

	return zi;
    }

    /**
     * Interprets a zone and constructs a Timezone object that
     * contains enough information on GMT offsets and DST schedules to
     * generate a zone info database.
     *
     * @param zoneName the zone name for which a Timezone object is
     * constructed.
     *
     * @return a Timezone object that contains all GMT offsets and DST
     * rules information.
     */
    Timezone phase2(String zoneName) {
	Timezone tz = new Timezone(zoneName);
	Zone zone = getZone(zoneName);
	zone.resolve(this);

	// TODO: merge phase2's for the regular and SimpleTimeZone ones.
	if (isYearForTimeZoneDataSpecified) {
	    ZoneRec zrec = zone.get(zone.size()-1);
	    tz.setLastZoneRec(zrec);
	    tz.setRawOffset(zrec.getGmtOffset());
	    if (zrec.hasRuleReference()) {
		/*
		 * This part assumes that the specified year is covered by
		 * the rules referred to by the last zone record.
		 */
		ArrayList rrecs = zrec.getRuleRef().getRules(startYear);

		if (rrecs.size() == 2) {
		    // make sure that one is a start rule and the other is
		    // an end rule.
		    RuleRec r0 = (RuleRec) rrecs.get(0);
		    RuleRec r1 = (RuleRec) rrecs.get(1);
		    if (r0.getSave() == 0 && r1.getSave() > 0) {
			rrecs.set(0, r1);
			rrecs.set(1, r0);
		    } else if (!(r0.getSave() > 0 && r1.getSave() == 0)) {
			rrecs = null;
			Main.error(zoneName + ": rules for " +  startYear + " not found.");
		    }
		} else {
		    rrecs = null;
		}
		if (rrecs != null) {
		    tz.setLastRules(rrecs);
		}
	    }
	    return tz;
	}

	int gmtOffset;
	int year = minYear;
	int fromYear = year;
	long fromTime = Time.getLocalTime(startYear,
					  Month.parse("Jan"),
					  1, 0);

	// take the index 0 for the GMT offset of the last zone record
	ZoneRec zrec = zone.get(zone.size()-1);
	tz.getOffsetIndex(zrec.getGmtOffset());

	int currentSave = 0;
	boolean usedZone;
	for (int zindex = 0; zindex < zone.size(); zindex++) {
	    zrec = zone.get(zindex);
	    usedZone = false;
	    gmtOffset = zrec.getGmtOffset();
	    int stdOffset = zrec.getDirectSave();

	    // If this is the last zone record, take the last rule info.
	    if (!zrec.hasUntil()) {
		tz.setRawOffset(gmtOffset, fromTime);
		if (zrec.hasRuleReference()) {
		    tz.setLastRules(zrec.getRuleRef().getLastRules());
		} else if (stdOffset != 0) {
		    // in case the last rule is all year round DST-only
		    // (Asia/Amman once announced this rule.)
		    tz.setLastDSTSaving(stdOffset);
		}
	    }
	    if (!zrec.hasRuleReference()) {
		if (!zrec.hasUntil() || zrec.getUntilTime(stdOffset) >= fromTime) {
		    tz.addTransition(fromTime,
				     tz.getOffsetIndex(gmtOffset+stdOffset),
				     tz.getDstOffsetIndex(stdOffset));
		    usedZone = true;
		}
		currentSave = stdOffset;
		// optimization in case the last rule is fixed.
		if (!zrec.hasUntil()) {
		    if (tz.getNTransitions() > 0) {
			if (stdOffset == 0) {
			    tz.setDSTType(tz.X_DST);
			} else {
			    tz.setDSTType(tz.LAST_DST);
			}
			long time = Time.getLocalTime(maxYear,
						      Month.parse("Jan"), 1, 0);
			time -= zrec.getGmtOffset();
			tz.addTransition(time,
					 tz.getOffsetIndex(gmtOffset+stdOffset),
					 tz.getDstOffsetIndex(stdOffset));
			tz.addUsedRec(zrec);
		    } else {
			tz.setDSTType(tz.NO_DST);
		    }
		    break;
		}
	    } else {
		Rule rule = zrec.getRuleRef();
		boolean fromTimeUsed = false;
		currentSave = 0;
	    year_loop:
		for (year = getMinYear(); year <= endYear; year++) {
		    if (zrec.hasUntil() && year > zrec.getUntilYear()) {
			break;
		    }
		    ArrayList rules = rule.getRules(year);
		    if (rules.size() > 0) {
			for (int i = 0; i < rules.size(); i++) {
			    RuleRec rrec = (RuleRec) rules.get(i);
			    long transition = rrec.getTransitionTime(year,
								     gmtOffset,
								     currentSave);
			    if (zrec.hasUntil()) {
				if (transition >= zrec.getUntilTime(currentSave)) {
				    break year_loop;
				}
			    }

			    if (fromTimeUsed == false) {
				int prevsave;

				if (fromTime <= transition) {
				    ZoneRec prevzrec = zone.get(zindex - 1);
				    fromTimeUsed = true;

				    // See if until time in the previous ZoneRec is the same thing
				    // as the local time in the next rule. (examples are
				    // Asia/Ashkhabad in 1991, Europe/Riga in 1989)

				    if (i > 0)
					prevsave = ((RuleRec)(rules.get(i-1))).getSave();
				    else {
					ArrayList prevrules = rule.getRules(year-1);

					if (prevrules.size() > 0)
					    prevsave = ((RuleRec)(prevrules.get(prevrules.size()-1))).getSave();
					else
					    prevsave = 0;
				    }

				    if (rrec.isSameTransition(prevzrec, prevsave, gmtOffset)) {
					currentSave = rrec.getSave();
					tz.addTransition(fromTime,
							 tz.getOffsetIndex(gmtOffset+currentSave),
							 tz.getDstOffsetIndex(currentSave));
					usedZone = true;
					tz.addUsedRec(rrec);
					continue;
				    }
				    if (!prevzrec.hasRuleReference()
					|| rule != prevzrec.getRuleRef()
					|| (rule == prevzrec.getRuleRef()
					    && gmtOffset != prevzrec.getGmtOffset())) {
					int save = (fromTime == transition) ? rrec.getSave() : currentSave;
					tz.addTransition(fromTime,
							 tz.getOffsetIndex(gmtOffset+save),
							 tz.getDstOffsetIndex(save));
					tz.addUsedRec(rrec);
					usedZone = true;
				    }
				} else if (year == fromYear && i == rules.size()-1) {
				    int save = rrec.getSave();
				    tz.addTransition(fromTime,
						     tz.getOffsetIndex(gmtOffset+save),
						     tz.getDstOffsetIndex(save));
				}
			    }

			    currentSave = rrec.getSave();
			    if (fromTime < transition) {
				tz.addTransition(transition,
						 tz.getOffsetIndex(gmtOffset+currentSave),
						 tz.getDstOffsetIndex(currentSave));
				tz.addUsedRec(rrec);
				usedZone = true;
			    }
			}
		    } else {
			if (year == fromYear) {
			    tz.addTransition(fromTime,
					     tz.getOffsetIndex(gmtOffset+currentSave),
					     tz.getDstOffsetIndex(currentSave));
			    fromTimeUsed = true;
			}
			if (year == endYear && !zrec.hasUntil()) {
			    if (tz.getNTransitions() > 0) {
				// Assume this Zone stopped DST
				tz.setDSTType(tz.X_DST);
				long time = Time.getLocalTime(maxYear, Month.parse("Jan"),
							      1, 0);
				time -= zrec.getGmtOffset();
				tz.addTransition(time,
						 tz.getOffsetIndex(gmtOffset),
						 tz.getDstOffsetIndex(0));
				usedZone = true;
			    } else {
				tz.setDSTType(tz.NO_DST);
			    }
			}
		    }
		}
	    }
	    if (usedZone) {
		tz.addUsedRec(zrec);
	    }
	    if (zrec.hasUntil() && zrec.getUntilTime(currentSave) > fromTime) {
		fromTime = zrec.getUntilTime(currentSave);
		fromYear = zrec.getUntilYear();
		year = zrec.getUntilYear();
	    }
	}

	if (tz.getDSTType() == tz.UNDEF_DST) {
	    tz.setDSTType(tz.DST);
	}
	tz.optimize();
	tz.checksum();
	return tz;
    }

    private static void panic(String msg) {
	Main.panic(msg);
    }
}
