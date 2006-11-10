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
#  Makefile for building the jdwp tool
#

-include  ../$(TARGET_OS)/jdwp_transport_$(CVM_JDWP_TRANSPORT).mk
-include  ../share/jdwp_transport_$(CVM_JDWP_TRANSPORT).mk

###############################################################################
# Make definitions:

CVM_JDWP_DT_LIB = libdt_$(CVM_JDWP_TRANSPORT)$(LIB_POSTFIX)

#
# Search path for include files:
#
CVM_JDWP_DT_INCLUDES  += \
	$(CVM_JDWP_INCLUDES) \
        -I$(CVM_JDWP_SHAREROOT)/transport/$(CVM_JDWP_TRANSPORT)

jdwp-dt: CVM_INCLUDES += $(CVM_JDWP_DT_INCLUDES)
jdwp-dt: CVM_DEFINES += $(CVM_JDWP_DEFINES)

CVM_JDWP_DT_OBJECTS0 = $(CVM_JDWP_DT_SHAREOBJS) $(CVM_JDWP_DT_TARGETOBJS)
CVM_JDWP_DT_OBJECTS  = $(patsubst %.o,$(CVM_JDWP_OBJDIR)/%.o,$(CVM_JDWP_DT_OBJECTS0))

###############################################################################
# Make rules:

jdwp_dt_build_list = $(CVM_JDWP_LIBDIR)/$(CVM_JDWP_DT_LIB)

jdwp-dt: $(jdwp_dt_build_list)

$(CVM_JDWP_LIBDIR)/$(CVM_JDWP_DT_LIB): $(CVM_JDWP_DT_OBJECTS)
	@echo "Linking $@"
	$(SO_LINK_CMD) $(LINKLIBS)
	@echo "Done Linking $@"
