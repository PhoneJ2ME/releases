/*
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

struct _PackedTopbarDib {
    BITMAPINFOHEADER hdr;
    DWORD            info[1485];
} _topbar_dib_data = {
    {
        sizeof(BITMAPINFOHEADER), /* biSize */
        180,			  /* biWidth */
        11,			  /* biHeight */
        1,			  /* biPlanes */
        24,			  /* biBitCount */
        0,			  /* biCompression */
        5940,		  	  /* biSizeImage */
        3780,			  /* biXPelsPerMeter */
        3780,			  /* biYPelsPerMeter */
        0,			  /* biClrUsed */
        0,			  /* biClrImportant */
    }, {
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0x00b6b6aa, 0xb6aa0000, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0x000000b6, 0xaa000000, 
	0x0000b6b6, 0x00000000, 0x00b6b6aa, 0x00000000, 
	0xb6b6aa00, 0x00000000, 0xb6aa0000, 0x000000b6, 
	0xaa000000, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0x00000000, 0x00000000, 0x00000000, 
	0x00000000, 0x00000000, 0x00000000, 0x00000000, 
	0x00000000, 0x00000000, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0x00b6b6aa, 0xb6aa0000, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0x000000b6, 0xaa000000, 0x0000b6b6, 
	0x00000000, 0x00b6b6aa, 0x00000000, 0xb6b6aa00, 
	0x00000000, 0xb6aa0000, 0x000000b6, 0xaa000000, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0x55000000, 0x6d556d6d, 0x6d6d556d, 0x556d6d55, 
	0x6d556d6d, 0x6d6d556d, 0xff6d6d55, 0xffffffff, 
	0xffffffff, 0xaa000000, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0x00b6b6aa, 
	0xb6aa0000, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0x000000b6, 0xaa000000, 0x0000b6b6, 0x00000000, 
	0x00b6b6aa, 0x00000000, 0xb6b6aa00, 0x00000000, 
	0xb6aa0000, 0x000000b6, 0xaa000000, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0x55000000, 
	0x6d556d6d, 0x6d6d556d, 0x556d6d55, 0x6d556d6d, 
	0x6d6d556d, 0xff6d6d55, 0xffffffff, 0xffffffff, 
	0x00000000, 0xb6aa0000, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0x00b6b6aa, 0xb6aa0000, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0x0000b6b6, 0x00000000, 0x00b6b6aa, 
	0x00000000, 0xb6b6aa00, 0x00000000, 0xb6aa0000, 
	0x000000b6, 0xaa000000, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0x55000000, 0x6d556d6d, 
	0x6d6d556d, 0x556d6d55, 0x6d556d6d, 0x6d6d556d, 
	0xff6d6d55, 0xffffffff, 0xffffffff, 0x00000000, 
	0xb6aa0000, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0x00000000, 0x00000000, 0xb6b6aa00, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0x00b6b6aa, 0x00000000, 
	0xb6b6aa00, 0x00000000, 0xb6aa0000, 0x000000b6, 
	0xaa000000, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0x55000000, 0x6d556d6d, 0x6d6d556d, 
	0x556d6d55, 0x6d556d6d, 0x6d6d556d, 0xff6d6d55, 
	0xffffffff, 0xffffffff, 0x00000000, 0xb6aa0000, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0x000000b6, 
	0x00b6b6aa, 0xb6aa0000, 0x000000b6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0x00000000, 0xb6aa0000, 0x000000b6, 0xaa000000, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0x55000000, 0x6d556d6d, 0x6d6d556d, 0x556d6d55, 
	0x6d556d6d, 0x6d6d556d, 0xff6d6d55, 0xffffffff, 
	0xffffffff, 0xaa000000, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0x0000b6b6, 0xb6b6aa00, 0x00b6b6aa, 
	0xb6aa0000, 0xb6b6aab6, 0xaa000000, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0x000000b6, 0xaa000000, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0x00000000, 
	0x00000000, 0x00000000, 0x00000000, 0x00000000, 
	0x00000000, 0x00000000, 0x00000000, 0x00000000, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0x0000b6b6, 0x00000000, 0x00000000, 0x00000000, 
	0x00000000, 0xaa000000, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 
	0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 
	0xaab6b6aa, 0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 
	0xb6aab6b6, 0xb6b6aab6, 0xaab6b6aa, 0xb6aab6b6,
        0xb6b6aab6,     }
};
BITMAPINFOHEADER *topbar_dib = &(_topbar_dib_data.hdr);
