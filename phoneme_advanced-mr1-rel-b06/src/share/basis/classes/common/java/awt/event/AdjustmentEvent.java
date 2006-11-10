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

package java.awt.event;

import java.awt.Adjustable;
import java.awt.AWTEvent;

/**
 * The adjustment event emitted by Adjustable objects.
 * @see java.awt.Adjustable
 * @see AdjustmentListener
 *
 * @version 1.17 08/19/02
 * @author Amy Fowler
 */
public class AdjustmentEvent extends AWTEvent {
    /**
     * Marks the first integer id for the range of adjustment event ids.
     */
    public static final int ADJUSTMENT_FIRST = 601;
    /**
     * Marks the last integer id for the range of adjustment event ids.
     */
    public static final int ADJUSTMENT_LAST = 601;
    /**
     * The adjustment value changed event.
     */
    public static final int ADJUSTMENT_VALUE_CHANGED = ADJUSTMENT_FIRST; //Event.SCROLL_LINE_UP
    /**
     * The unit increment adjustment type.
     */
    public static final int UNIT_INCREMENT = 1;
    /**
     * The unit decrement adjustment type.
     */
    public static final int UNIT_DECREMENT = 2;
    /**
     * The block decrement adjustment type.
     */
    public static final int BLOCK_DECREMENT = 3;
    /**
     * The block increment adjustment type.
     */
    public static final int BLOCK_INCREMENT = 4;
    /**
     * The absolute tracking adjustment type.
     */
    public static final int TRACK = 5;
    Adjustable adjustable;
    int value;
    int adjustmentType;
    /*
     * JDK 1.1 serialVersionUID 
     */
    private static final long serialVersionUID = 5700290645205279921L;
    /**
     * Constructs a AdjustmentEvent object with the specified Adjustable source,
     * type, and value.
     * @param source the Adjustable object where the event originated
     * @id the event type
     * @type the adjustment type 
     * @value the current value of the adjustment
     */
    public AdjustmentEvent(Adjustable source, int id, int type, int value) {
        super(source, id);
        adjustable = source;
        this.adjustmentType = type;
        this.value = value;
    }

    /**
     * Returns the Adjustable object where this event originated.
     */
    public Adjustable getAdjustable() {
        return adjustable;
    }

    /**
     * Returns the current value in the adjustment event.
     */
    public int getValue() {
        return value;
    }

    /**
     * Returns the type of adjustment which caused the value changed
     * event.
     * @see UNIT_INCREMENT
     * @see UNIT_DECREMENT
     * @see BLOCK_INCREMENT
     * @see BLOCK_DECREMENT
     * @see TRACK
     */
    public int getAdjustmentType() {
        return adjustmentType;
    }

    public String paramString() {
        String typeStr;
        switch (id) {
        case ADJUSTMENT_VALUE_CHANGED:
            typeStr = "ADJUSTMENT_VALUE_CHANGED";
            break;

        default:
            typeStr = "unknown type";
        }
        String adjTypeStr;
        switch (adjustmentType) {
        case UNIT_INCREMENT:
            adjTypeStr = "UNIT_INCREMENT";
            break;

        case UNIT_DECREMENT:
            adjTypeStr = "UNIT_DECREMENT";
            break;

        case BLOCK_INCREMENT:
            adjTypeStr = "BLOCK_INCREMENT";
            break;

        case BLOCK_DECREMENT:
            adjTypeStr = "BLOCK_DECREMENT";
            break;

        case TRACK:
            adjTypeStr = "TRACK";
            break;

        default:
            adjTypeStr = "unknown type";
        }
        return typeStr + ",adjType=" + adjTypeStr + ",value=" + value;
    }
}
