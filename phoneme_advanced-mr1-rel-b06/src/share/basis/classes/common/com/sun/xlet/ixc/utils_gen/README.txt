#
# Copyright 2006 Sun Microsystems, Inc. All rights reserved.
# 
# Sun Microsystems, Inc. has intellectual property rights relating to technology
# embodied in the product that is described in this document. In particular, and
# without limitation, these intellectual property rights may include one or more
# of the U.S. patents listed at http://www.sun.com/patents and one or more
# additional patents or pending patent applications in the U.S. and in other
# countries.
# U.S. Government Rights - Commercial software. Government users are subject to
# the Sun Microsystems, Inc. standard license agreement and applicable provisions
# of the FAR and its supplements.
# 
# Use is subject to license terms.
# 
# This distribution may include materials developed by third parties.Sun, Sun
# Microsystems, phoneME and Java are trademarks or registered trademarks of Sun
# Microsystems, Inc. in the U.S. and other countries.
# 
# 
# Copyright 2006 Sun Microsystems, Inc. Tous droits r�serv�s.
# 
# Sun Microsystems, Inc. d�tient les droits de propri�t� intellectuels relatifs �
# la technologie incorpor�e dans le produit qui est d�crit dans ce document. En
# particulier, et ce sans limitation, ces droits de propri�t� intellectuelle
# peuvent inclure un ou plus des brevets am�ricains list�s � l'adresse
# http://www.sun.com/patents et un ou les brevets suppl�mentaires ou les
# applications de brevet en attente aux Etats - Unis et dans les autres pays.
# 
# L'utilisation est soumise aux termes du contrat de licence.
# 
# Cette distribution peut comprendre des composants d�velopp�s par des tierces
# parties.
# 
# Sun, Sun Microsystems, phoneME et Java sont des marques de fabrique ou des
# marques d�pos�es de Sun Microsystems, Inc. aux Etats-Unis et dans d'autres pays.
# 

# 
# This directory contains tools used to make the byte array
# in IxcClassLoader.  That byte array contains the class definition
# for com.sun.xlet.Utils.  If a change is needed to
# com.sun.xlet.Utils, rename the two files in this directory
# to ".java".  You need to compile Utils in a temporary
# directory called com/sun/xlet, of course.
# 
# Once you've compiled Utils, run Main over the .class
# file to generate a byte array declaration.  Then, paste this
# into IxcClassLoader.  So, for example, to build this, do
# something like the following:

mkdir -p com/sun/xlet
cat Utils.gen > com/sun/xlet/Utils.java
javac com/sun/xlet/Utils.java
if [[ $? != 0 ]] ; then
    exit;
fi
mkdir -p tmp
mv com/sun/xlet/Utils.class tmp
rm -rf com
cat Main.gen > tmp/Main.java
cd tmp
javac Main.java
java Main < Utils.class > generated.txt
echo "Paste tmp/generated.txt into IxcClassLoader.java, then rm -rf tmp."

