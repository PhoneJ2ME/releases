/*
 * @(#)apdu_errors.c	1.1 06/04/20 @(#)
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

#include <stdarg.h>
#include <stdio.h>
#include <string.h>
#include <carddevice.h>

#define ERR_LINE_COUNT  30
#define ERR_STACK_SIZE (ERR_LINE_COUNT*256 + ERR_LINE_COUNT*4 + 1*4)

static char err_stack[ERR_STACK_SIZE];
static char *err_line_top = err_stack;
static char **err_ptr_top = (char **)(err_stack + sizeof err_stack);

/* Local functions */
static void add_error_msg(const char *err_class, const char *fmt, va_list ap);

void jsr177_clear_error() {
	
	err_line_top = err_stack;
	err_ptr_top = (char **)(err_stack + sizeof err_stack);
	*err_line_top = '\0';
}

void jsr177_set_error(const jbyte *fmt, ...) {
	va_list ap;
	va_start(ap, fmt);
	add_error_msg("Error", fmt, ap);
	va_end(ap);

}

jboolean jsr177_get_error(jbyte *buf, jsize buf_size) {
	char **p;
	jboolean first_flag = PCSL_TRUE;
	
	if (err_ptr_top == (char **)(err_stack + sizeof err_stack)) {
		return PCSL_FALSE;
	}

	if (buf == NULL  ||  buf_size < 3) {
		return PCSL_FALSE;
	}
	
	for (p = err_ptr_top; p < (char **)(err_stack + sizeof err_stack); p++ ) {
		int len = strlen(*p);
		
		if (!first_flag) {
			*buf++ = '\n';
			buf_size--;
		} else {
			first_flag = PCSL_FALSE;
		}
		
		if (buf_size-1 < len) {
			len = buf_size-1;
		}
		memcpy(buf, *p, len);
		
		buf_size -= len;
		buf += len;
		
		if (buf_size < 4) {
			break;
		}
	}
	*buf++ = '\0';
	jsr177_clear_error();
	
	return PCSL_TRUE;
}

static void add_error_msg(const char *err_class, const char *fmt, va_list ap) {
	int len;
	char *line_ptr = err_line_top;
	int bytes_left = ((char*)err_ptr_top - err_line_top) - sizeof *err_ptr_top;

	if (bytes_left <= 0) 
		return;

	err_line_top += (len=snprintf(err_line_top, bytes_left, "%s: ", err_class));
	*err_line_top = '\0';
	bytes_left -= len;
	
	err_line_top += vsnprintf(err_line_top, bytes_left, fmt, ap);
	*err_line_top = '\0';
	
	err_line_top++;
	--err_ptr_top;
	*err_ptr_top = line_ptr;
}

