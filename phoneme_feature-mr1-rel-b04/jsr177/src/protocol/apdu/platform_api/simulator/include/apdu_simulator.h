/*
 * @(#)apdu_simulator.h	1.1 06/04/20 @(#)
 *
 * Copyright  1990-2006 Sun Microsystems, Inc. All Rights Reserved.
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

/**
 * The size of the largest possible TLP224 Message.
 * This message would be &lt;ACK&gt;&lt;LEN&gt;&lt;255 bytes of 
 * command&gt;&lt;LRC&gt;
 */
#define MAX_MESSAGE_LEN                              (264)

/**
 * The ACK code which is returned to the sender as 
 * the first octet of a TLP224 response when a message has been 
 * successfully received.
 */
#define TLP224Message_ACK                           ((jbyte)0x60)

/**
 * The NACK code which is returned to the sender as 
 * the first octet of a TLP224 response when a transmission error occurs.
 */
#define TLP224Message_NACK                          ((jbyte)0xE0)

/**
 * The EOT code which is always sent as the last 
 * octet of any TLP224 message.
 */
#define TLP224Message_EOT                           ((jbyte)0x03)

/**
 * The TLP224 command to power up the CAD.
 */
#define TLP224Message_POWER_UP                      ((jbyte)0x6E)

/**
 * The TLP224 command to power down the CAD.
 */
#define TLP224Message_POWER_DOWN                    ((jbyte)0x4D)

/**
 * The TLP224 command to read data from the card.
 */
#define TLP224Message_ISO_OUTPUT                    ((jbyte)0xDB)

/**
 * The TLP224 command to send data to the card.
 */
#define TLP224Message_ISO_INPUT                     ((jbyte)0xDA)

/**
 * This status code is returned by both the Client and Server CAD's when
 * a command has been successfully executed.
 */
#define TLP224Message_STATUS_SUCCESS                ((jbyte)0x00)

/**
 * This status code is returned by the reader or ServerCad if the card was
 * removed between exchanges.
 */
#define TLP224Message_STATUS_CARD_REMOVED           ((jbyte)0xF7)

/**
 * This status code is returned by both the Client and Server CAD's 
 * when a received message exceeds the length of the internal buffers.
 */
#define TLP224Message_STATUS_MESSAGE_TOO_LONG       ((jbyte)0x12)

/**
 * This status code is returned by both the Client and Server CAD's when 
 * the first byte of a received message is neither an ACK or NACK.
 */
#define TLP224Message_STATUS_PROTOCOL_ERROR         ((jbyte)0x09)

/**
 * This status code is returned by the reader or ServerCad
 * if SW1 SW2 are not equal to 0x9000.
 */
#define TLP224Message_STATUS_CARD_ERROR             ((jbyte)0xE7)

/**
 * This status code is returned by the reader or ServerCad if the card
 * sends a Procedure Byte which aborts a ISO_IN or ISO_OUT command.
 */
#define TLP224Message_STATUS_INTERRUPTED_EXCHANGE   ((jbyte)0xE5)

/**
 * Maximum number of supported slots. It can be increased
 */
#define MAX_SLOT_COUNT  3

