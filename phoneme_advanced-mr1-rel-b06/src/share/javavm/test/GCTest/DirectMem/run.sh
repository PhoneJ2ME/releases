#
# Copyright 1990-2006 Sun Microsystems, Inc. All Rights Reserved. 
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER 
# 
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License version 2 only,
# as published by the Free Software Foundation.
# 
# This program is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
# or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
# version 2 for more details (a copy is included at /legal/license.txt).
# 
# You should have received a copy of the GNU General Public License version
# 2 along with this work; if not, write to the Free Software Foundation,
# Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
# 
# Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
# CA 95054 or visit www.sun.com if you need additional information or have
# any questions.
#

#!/bin/sh

echo ""
echo "------- Running DirectMem --------"
echo ""

if [ -f ../Defs.mk ]
then
        . ../Defs.mk
else
        echo "Please Create Defs.mk file by running configure.sh"
        exit 1
fi

CUR_DIR=$GCTEST_DIR/DirectMem
LOCAL_CLASSPATH=$CUR_DIR/Class

#FILES=DirectMemTest
FILES="DMFieldRWIntTest DMFieldRWLongTest DMFieldRWFloatTest 
       DMFieldRWDoubleTest DMFieldRW32Test DMFieldRWRefTest
       DMFieldRW64Test DMArrayRWIntTest DMArrayRWFloatTest 
       DMArrayRWBooleanTest DMArrayRWByteTest DMArrayRWShortTest
       DMArrayRWLongTest DMArrayRWDoubleTest DMArrayRWCharTest
       DMArrayRWRefTest DMArrayCopyByteTest DMArrayCopyCharTest 
       DMArrayCopyIntTest DMArrayCopyShortTest DMArrayCopyLongTest 
       DMArrayCopyFloatTest DMArrayCopyDoubleTest DMArrayCopyBooleanTest 
       DMArrayCopyRefTest DMArrayRWBodyByteTest DMArrayRWBodyCharTest 
       DMArrayRWBodyIntTest DMArrayRWBodyShortTest DMArrayRWBodyLongTest 
       DMArrayRWBodyFloatTest DMArrayRWBodyDoubleTest DMArrayRWBodyBooleanTest
       DMArrayRWBodyRefTest"

switch="-Xbootclasspath=$BOOT_CLASSPATH:$LOCAL_CLASSPATH"

COMMAND="$CDC_BUILDDIR/bin/cvm $switch"

for file in $FILES
do
	echo "--------- Running $file -------------"
	echo "$COMMAND $file"
	$COMMAND $file
	echo ""
done

echo ""
