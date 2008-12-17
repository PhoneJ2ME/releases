/*
 * @(#)Rule.java	1.6 06/10/10
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.StringTokenizer;

/**
 * Rule manipulates Rule records.
 *
 * @since 1.4
 */
class Rule {

    private ArrayList list;
    private String name;

    /**
     * Constructs a Rule which consists of a Rule record list. The
     * specified name is given to this Rule.
     * @param name the Rule name
     */
    Rule(String name) {
	this.name = name;
	list = new ArrayList();
    }

    /**
     * Added a RuleRec to the Rule record list.
     */
    void add(RuleRec rec) {
	list.add(rec);
    }

    /**
     * @return the Rule name
     */
    String getName() {
	return name;
    }

    /**
     * Gets all rule records that cover the given year.
     * @param year the year number for which the rule is applicable.
     * @return rules in ArrayList that are collated in time. If no rule is found, an empty
     * ArrayList is returned.
     */
    ArrayList getRules(int year) {
	ArrayList rules = new ArrayList(3);
	for (int i = 0; i < list.size(); i++) {
	    RuleRec rec = (RuleRec) list.get(i);
	    if (year >= rec.getFromYear() && year <= rec.getToYear()) {
		if ((rec.isOdd() && year % 2 == 0) || (rec.isEven() && year % 2 == 1))
		    continue;
		rules.add(rec);
	    }
	}
	int n = rules.size();
	if (n <= 1) {
	    return rules;
	}
	if (n == 2) {
	    RuleRec rec1 = (RuleRec) rules.get(0);
	    RuleRec rec2 = (RuleRec) rules.get(1);
	    if (rec1.getMonthNum() > rec2.getMonthNum()) {
		rules.set(0, rec2);
		rules.set(1, rec1);
	    } else if (rec1.getMonthNum() == rec2.getMonthNum()) {
		// TODO: it's not accurate to ignore time types (STD, WALL, UTC)
		long t1 = Time.getLocalTime(year, rec1.getMonthNum(),
					    rec1.getDay(), rec1.getTime().getTime());
		long t2 = Time.getLocalTime(year, rec2.getMonthNum(),
					    rec2.getDay(), rec2.getTime().getTime());
		if (t1 > t2) {
		    rules.set(0, rec2);
		    rules.set(1, rec1);
		}		
	    }
	    return rules;
	}

	final int y = year;
	RuleRec[] recs = new RuleRec[rules.size()];
	rules.toArray(recs);
	Arrays.sort(recs, new Comparator() {
		public int compare(Object o1, Object o2) {
		    RuleRec r1 = (RuleRec) o1;
		    RuleRec r2 = (RuleRec) o2;
		    int n = r1.getMonthNum() - r2.getMonthNum();
		    if (n != 0) {
			return n;
		    }
		    // TODO: it's not accurate to ignore time types (STD, WALL, UTC)
		    long t1 = Time.getLocalTime(y, r1.getMonthNum(),
						r1.getDay(), r1.getTime().getTime());
		    long t2 = Time.getLocalTime(y, r2.getMonthNum(),
						r2.getDay(), r2.getTime().getTime());
		    return (int)(t1 - t2);
		}
		public boolean equals(Object o1, Object o2) {
		    RuleRec r1 = (RuleRec) o1;
		    RuleRec r2 = (RuleRec) o2;
		    return r1.getMonthNum() == r2.getMonthNum();
		}
	    });
	rules.clear();
	for (int i = 0; i < n; i++) {
	    rules.add(recs[i]);
	}
	return rules;
    }

    /**
     * Gets rule records that have either "max" or cover the endYear
     * value in its DST schedule.
     *
     * @return rules that contain last DST schedule. An empty
     * ArrayList is returned if no last rules are found.
     */
    ArrayList getLastRules() {
	RuleRec start = null;
	RuleRec end = null;

	for (int i = 0; i < list.size(); i++) {
	    RuleRec rec = (RuleRec) list.get(i);
	    if (rec.isLastRule()) {
		if (rec.getSave() > 0) {
		    start = rec;
		} else {
		    end = rec;
		}
	    }
	}
	if (start == null || end == null) {
	    int endYear = Zoneinfo.getEndYear();
	    for (int i  = 0; i < list.size(); i++) {
		RuleRec rec = (RuleRec) list.get(i);
		if (endYear >= rec.getFromYear() && endYear <= rec.getToYear()) {
		    if (start == null && rec.getSave() > 0) {
			start = rec;
		    } else {
			if (end == null && rec.getSave() == 0) {
			    end = rec;
			}
		    }
		}
	    }
	}

	ArrayList r = new ArrayList(2);
	if (start == null || end == null) {
	    if (start != null || end != null) {
		Main.warning("found last rules for "+name+" inconsistent.");
	    }
	    return r;
	}

	r.add(start);
	r.add(end);
	return r;
    }
}
