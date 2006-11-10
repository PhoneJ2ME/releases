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


J2ME_PROFILE_NAME		= CDC
J2ME_PROFILE_SPEC_VERSION	= 1.1
CVM_BUILD_ID 			= b31

ifeq ($(CVM_JIT), true)
  CVM_BUILD_NAME	= CDC HI
else
  CVM_BUILD_NAME	= CDC
endif

CVM_BUILD_VERSION	= 1.1.1_01

J2ME_PRODUCT_NAME	= $(CVM_BUILD_NAME)
J2ME_BUILD_VERSION	= $(CVM_BUILD_VERSION)
J2ME_BUILD_ID		= $(CVM_BUILD_ID)

