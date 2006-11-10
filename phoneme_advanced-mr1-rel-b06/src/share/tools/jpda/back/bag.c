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

/* General routines for manipulating a bag data structure */

#include <string.h>
#include "bag.h"
#include "util.h"

struct bag {
    void *items;    /* hold items in bag, must align on itemSize */
    int used;       /* number of items in bag */
    int allocated;  /* space reserved */
    int itemSize;   /* size of each item, should init to sizeof item */
};

struct bag * 
bagCreateBag(int itemSize, int initialAllocation) {   
    struct bag *theBag = (struct bag *)jdwpAlloc(sizeof(struct bag));
    if (theBag == NULL) {
        return NULL;
    }
    itemSize = (itemSize + 7) & ~7;    /* fit 8 byte boundary */
    theBag->items = jdwpAlloc(initialAllocation * itemSize);
    if (theBag->items == NULL) {
        jdwpFree(theBag);
        return NULL;
    }
    theBag->used = 0;
    theBag->allocated = initialAllocation;
    theBag->itemSize = itemSize;
    return theBag;
}

struct bag *
bagDup(struct bag *oldBag) 
{
    struct bag *newBag = bagCreateBag(oldBag->itemSize,
                                      oldBag->allocated);
    if (newBag != NULL) {
        newBag->used = oldBag->used;
        memcpy(newBag->items, oldBag->items, newBag->used * newBag->itemSize);
    }
    return newBag;
}

void
bagDestroyBag(struct bag *theBag) 
{
    if (theBag != NULL) {
        jdwpFree(theBag->items);
        jdwpFree(theBag);
    }
}

void *
bagFind(struct bag *theBag, void *key) 
{
    char *items = theBag->items;
    int itemSize = theBag->itemSize;
    char *itemsEnd = items + (itemSize * theBag->used);
    
    for (; items < itemsEnd; items += itemSize) {
        if (*((void**)items) == key) {
            return items;
        }
    }
    return NULL;
}

void *
bagAdd(struct bag *theBag) 
{
    int allocated = theBag->allocated;
    int itemSize = theBag->itemSize;
    void *items = theBag->items;
    void *ret;

    /* if there are no unused slots reallocate */
    if (theBag->used >= allocated) {
        allocated *= 2;
        items = jdwpRealloc(items, allocated * itemSize);
        if (items == NULL) {
            return NULL;
        }
        theBag->allocated = allocated;
        theBag->items = items;
    }
    ret = ((char *)items) + (itemSize * (theBag->used)++);
    memset(ret, 0, itemSize);
    return ret;
}
    
void
bagDelete(struct bag *theBag, void *condemned) 
{
    int used = --(theBag->used);
    int itemSize = theBag->itemSize;
    void *items = theBag->items;
    void *tailItem = ((char *)items) + (used * itemSize);
    
    if (condemned != tailItem) {
        memcpy(condemned, tailItem, itemSize);
    }
}

void
bagDeleteAll(struct bag *theBag) 
{
    theBag->used = 0;
}

    
int
bagSize(struct bag *theBag) 
{
    return theBag->used;
}    

jboolean
bagEnumerateOver(struct bag *theBag, bagEnumerateFunction func, void *arg) 
{
    char *items = theBag->items;
    int itemSize = theBag->itemSize;
    char *itemsEnd = items + (itemSize * theBag->used);
    
    for (; items < itemsEnd; items += itemSize) {
        if (!(func)((void *)items, arg)) {
            return JNI_FALSE;
        }
    }
    return JNI_TRUE;
}

