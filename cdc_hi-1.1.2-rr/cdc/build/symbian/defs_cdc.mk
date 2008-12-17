#
# Copyright  1990-2008 Sun Microsystems, Inc. All Rights Reserved.
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
# 
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License version
# 2 only, as published by the Free Software Foundation. 
# 
# This program is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# General Public License version 2 for more details (a copy is
# included at /legal/license.txt). 
# 
# You should have received a copy of the GNU General Public License
# version 2 along with this work; if not, write to the Free Software
# Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
# 02110-1301 USA 
# 
# Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
# Clara, CA 95054 or visit www.sun.com if you need additional
# information or have any questions. 
#
# @(#)defs_cdc.mk	1.8 06/10/10
#

CVM_TARGETCLASSES_SRCDIR += \
	$(CVM_TOP)/src/portlibs/unix/classes

CVM_BUILDTIME_CLASSES += \
	sun.misc.FileURLMapper

CVM_BUILDTIME_CLASSES_nullapp += \
	java.io.SymbianFileSystem

#
# CDC library platform classes
#

CLASSLIB_CLASSES += \
        java.lang.SymbianProcess

#
# Things to build for CDC
#
CVM_TARGETOBJS_SPACE += \
	Runtime_md.o \
	SymbianFileSystem_md.o \
	SymbianProcess_md.o \
	FileSystem_md.o \
	Inet4AddressImpl_md.o \
	Inet6AddressImpl_md.o \
	InetAddressImplFactory.o \
	NetworkInterface.o \
	timezone_md.o \
	PlainDatagramSocketImpl_md.o \
	commProtocol_md.o \
	net_util_md.o
