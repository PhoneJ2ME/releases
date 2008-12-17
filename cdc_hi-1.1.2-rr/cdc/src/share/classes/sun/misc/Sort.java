/*
 * @(#)Sort.java	1.15 06/10/10
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
 *
 */

/**
 * Sort: a class that uses the quicksort algorithm to sort an
 *	 array of objects.
 *
 * @version 1.9, 02/02/00
 * @author Sunita Mani
 */

package sun.misc;

public class Sort {

    private static void swap(Object arr[], int i, int j) {
	Object tmp;

	tmp = arr[i];
	arr[i] = arr[j];
	arr[j] = tmp;
    }

    /**
     * quicksort the array of objects.
     *
     * @param arr[] - an array of objects
     * @param left - the start index - from where to begin sorting
     * @param right - the last index.
     * @param comp - an object that implemnts the Compare interface to resolve thecomparison.
     */
    public static void quicksort(Object arr[], int left, int right, Compare comp) {
	int i, last;

	if (left >= right) { /* do nothing if array contains fewer than two */
	    return; 	     /* two elements */
	}
	swap(arr, left, (left+right) / 2);
	last = left;
	for (i = left+1; i <= right; i++) {
	    if (comp.doCompare(arr[i], arr[left]) < 0) {
		swap(arr, ++last, i);
	    }
	}
	swap(arr, left, last);
	quicksort(arr, left, last-1, comp);
	quicksort(arr, last+1, right, comp);
    }

    public static void quicksort(Object arr[], Compare comp) {
        quicksort(arr, 0, arr.length-1, comp);
    }
}

