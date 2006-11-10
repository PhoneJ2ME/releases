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

#ifndef _JBUG_BAG_H_
#define _JBUG_BAG_H_

#include <jni.h>

/* Declare general routines for manipulating a bag data structure.
 * Synchronized use is the responsibility of caller.
 */

struct bag;

#ifdef __linux__
/*
 * This lets us run under linux where our bag* functions are
 * colliding with the ones defined in the VM. The real solution is to avoid
 * exporting symbols like this.
 */
#define bagCreateBag     jdwp_bagCreateBag
#define bagDup           jdwp_bagDup
#define bagDestroyBag    jdwp_bagDestroyBag
#define bagFind          jdwp_bagFind
#define bagAdd           jdwp_bagAdd
#define bagDelete        jdwp_bagDelete
#define bagDeleteAll     jdwp_bagDeleteAll
#define bagEnumerateOver jdwp_bagEnumerateOver
#endif

/* Must be used to create a bag.  itemSize is the size
 * of the items stored in the bag. initialAllocation is a hint
 * for the initial number of items to allocate. Returns the
 * allocated bag, returns NULL if out of memory.
 */
struct bag *bagCreateBag(int itemSize, int initialAllocation);

/* 
 * Copy bag contents to another new bag. The new bag is returned, or 
 * NULL if out of memory.
 */
struct bag *bagDup(struct bag *);

/* Destroy the bag and reclaim the space it uses.
 */
void bagDestroyBag(struct bag *theBag);

/* Find 'key' in bag.  Assumes first entry in item is a pointer.
 * Return found item pointer, NULL if not found. 
 */
void *bagFind(struct bag *theBag, void *key);

/* Add space for an item in the bag.
 * Return allocated item pointer, NULL if no memory. 
 */
void *bagAdd(struct bag *theBag);

/* Delete specified item from bag. 
 * Does no checks.
 */
void bagDelete(struct bag *theBag, void *condemned);

/* Delete all items from the bag.
 */
void bagDeleteAll(struct bag *theBag);

/* Return the count of items stored in the bag.
 */
int bagSize(struct bag *theBag);

/* Enumerate over the items in the bag, calling 'func' for 
 * each item.  The function is passed the item and the user 
 * supplied 'arg'.  Abort the enumeration if the function
 * returns FALSE.  Return TRUE if the enumeration completed
 * successfully and FALSE if it was aborted.
 * Addition and deletion during enumeration is not supported.
 */
typedef jboolean (*bagEnumerateFunction)(void *item, void *arg);

jboolean bagEnumerateOver(struct bag *theBag, 
                        bagEnumerateFunction func, void *arg);

#endif /* !_JAVASOFT_BAG_H_ */
