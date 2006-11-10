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

/*
 * modf(double x, double *iptr)
 * return fraction part of x, and return x's integral part in *iptr.
 * Method:
 *	Bit twiddling.
 *
 * Exception:
 *	No exception.
 */

#include "fdlibm.h"

#ifdef __STDC__
static const double one = 1.0;
#else
static double one = 1.0;
#endif

#ifdef __STDC__
	double modf(double x, double *iptr)
#else
	double modf(x, iptr)
	double x,*iptr;
#endif
{
	int i0,i1,j0;
	unsigned i;
	i0 =  __HI(x);		/* high x */
	i1 =  __LO(x);		/* low  x */
	j0 = ((i0>>20)&0x7ff)-0x3ff;	/* exponent of x */
	if(j0<20) {			/* integer part in high x */
	    if(j0<0) {			/* |x|<1 */
		__HIp(iptr) = i0&0x80000000;
		__LOp(iptr) = 0;		/* *iptr = +-0 */
		return x;
	    } else {
		i = (0x000fffff)>>j0;
		if(((i0&i)|i1)==0) {		/* x is integral */
		    *iptr = x;
		    __HI(x) &= 0x80000000;
		    __LO(x)  = 0;	/* return +-0 */
		    return x;
		} else {
		    __HIp(iptr) = i0&(~i);
		    __LOp(iptr) = 0;
		    return x - *iptr;
		}
	    }
	} else if (j0>51) {		/* no fraction part */
	    *iptr = x*one;
	    __HI(x) &= 0x80000000;
	    __LO(x)  = 0;	/* return +-0 */
	    return x;
	} else {			/* fraction part in low x */
	    i = ((unsigned)(0xffffffff))>>(j0-20);
	    if((i1&i)==0) { 		/* x is integral */
		*iptr = x;
		__HI(x) &= 0x80000000;
		__LO(x)  = 0;	/* return +-0 */
		return x;
	    } else {
		__HIp(iptr) = i0;
		__LOp(iptr) = i1&(~i);
		return x - *iptr;
	    }
	}
}
