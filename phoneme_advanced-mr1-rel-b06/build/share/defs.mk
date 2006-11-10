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
# Common makefile defs
#

empty:=
comma:= ,
space:= $(empty) $(empty)

#
# Setup HOST_OS, HOST_CPU_FAMILY, and HOST_DEVICE. These are used
# to assist in locating the proper tools to use.
#

UNAME_OS 	?= $(shell uname -s)

# Solaris host support
ifeq ($(UNAME_OS), SunOS)
HOST_OS		?= solaris
HOST_CPU_FAMILY ?= $(shell uname -p)
ifeq ($(HOST_CPU_FAMILY), sparc)
HOST_DEVICE	?= sun
else
HOST_DEVICE	?= pc
endif
endif

# Linux  host support
ifeq ($(UNAME_OS), Linux)
HOST_OS 	?= linux
HOST_CPU_FAMILY ?= $(shell uname -m)
ifeq ($(wildcard /etc/redhat-release), /etc/redhat-release)
	HOST_DEVICE ?= redhat
endif
ifeq ($(wildcard /etc/SuSE-release), /etc/SuSE-release)
	HOST_DEVICE ?= SuSE
endif
LSB_RELEASE = $(shell lsb_release -i -s 2> /dev/null)
ifneq ($(LSB_RELEASE),)
	HOST_DEVICE ?= $(LSB_RELEASE)
endif

ifeq ($(HOST_DEVICE),)
	HOST_DEVICE ?= generic
endif
endif

# Darwin (MacOS X) host support
ifeq ($(UNAME_OS), Darwin)
HOST_OS 	?= darwin
HOST_CPU_FAMILY ?= $(shell uname -p)
ifeq ($(HOST_CPU_FAMILY), powerpc)
HOST_DEVICE	?= mac
else
HOST_DEVICE	?= pc
endif
JDK_HOME = /System/Library/Frameworks/JavaVM.framework/Versions/1.4.2/Home
endif

# Windows host support
ifeq ($(findstring CYGWIN, $(UNAME_OS)), CYGWIN)
HOST_OS 	?= cygwin
HOST_CPU_FAMILY ?= $(shell uname -m)
HOST_DEVICE	?= win32
endif

ifeq ($(UNAME_OS), Interix)
HOST_OS		?= $(UNAME_OS)
HOST_CPU_FAMILY ?= $(shell uname -m)
HOST_DEVICE	?= win32
TOOL_WHICH	?= PATH="$(PATH)" whence "$(1)"
endif

ifeq ($(HOST_OS),)
$(error Invalid host. "$(UNAME_OS)" not recognized.)
endif

TOOL_WHICH	?= PATH="$(PATH)" which "$(1)"

#
# By default we prefer SHELL=sh. Some of the makefile commands require
# an sh compatible shell. csh won't work. Most versions of sh, ksh, tcsh,
# and bash will work fine. On some hosts we default to something other than
# sh. See comments below to find out when and why.
#
# The "-e" option is added to the SHELL command so shell commands will
# exit if there is an error. If this isn't done, then sometimes the 
# makefile continues to execute after a shell command fails.
#
# Note that gnumake also adds "-c" to the command, so you end up with
# a command that looks like this:
#
#   sh -e -c "<command>"
#
# Some versions of sh don't support this and require you to use "-ec"
# instead of "-e -c", but there is no way to get gnumake to do this.
# In this case you should override SHELL to use some other shell, or just
# drop the -e argument.
# 
# ksh is not the default shell because it is not installed on all 
# systems, and some versions of it have problems with the long commands
# that the makefiles produce.
#
SHELL	= sh -e

#
# Use ksh on solaris, since the solaris sh doesn't support -e as a
# separate parameter.
#
ifeq ($(HOST_OS), solaris)
SHELL	= ksh -e
endif

#
# Use bash on win32, since the Cygwin sh doesn't work for us.
#
ifeq ($(HOST_OS), cygwin)
SHELL	= bash
endif

ifeq ($(HOST_OS), Interix)
SHELL	= ksh
endif

#
# Setup host and target platform names. Note that the naming coventions
# for each is different with respect to the order of the cpu, os, 
# and device parts.
#
CVM_HOST 	?= $(HOST_CPU_FAMILY)-$(HOST_DEVICE)-$(HOST_OS)
CVM_TARGET	= $(TARGET_OS)-$(TARGET_CPU_FAMILY)-$(TARGET_DEVICE)

# Set overriding values:

# Figure out if this is a CDC 1.0 source base or not
ifeq ($(wildcard ../share/defs_zoneinfo.mk),)
	override CDC_10 = true
else
	override CDC_10 = false
endif

# We need to check this here because the CVM_JVMDI option overrides many
# others that follows through CVM_DEBUG:
ifeq ($(CVM_JVMDI), true)
        override CVM_DEBUG_CLASSINFO = true
        override CVM_JAVAC_DEBUG = true
	override CVM_XRUN = true
        override CVM_THREAD_SUSPENSION = true
endif

ifeq ($(CVM_CLASSLIB_JCOV), true)
        override CVM_JVMPI = true
        override CVM_JVMPI_TRACE_INSTRUCTION = true
endif

ifeq ($(CVM_JVMPI), true)
        override CVM_DEBUG_CLASSINFO = true
	override CVM_XRUN = true
        override CVM_THREAD_SUSPENSION = true
endif

# Set default options.
# NOTE: These options are officially supported and are documented in
#       docs/guide/build.html:

CVM_DEBUG		?= false
CVM_TRACE		?= $(CVM_DEBUG)
ifeq ($(CVM_TRACE),true)
override CVM_DEBUG_DUMPSTACK	= true
endif
ifeq ($(CVM_VERIFY_HEAP),true)
override CVM_DEBUG_ASSERTS = true
endif
CVM_DEBUG_ASSERTS	?= $(CVM_DEBUG)
CVM_DEBUG_CLASSINFO	?= $(CVM_DEBUG)
CVM_DEBUG_DUMPSTACK	?= $(CVM_DEBUG)
CVM_DEBUG_STACKTRACES	?= true
CVM_INSPECTOR		?= $(CVM_DEBUG)
CVM_JAVAC_DEBUG		?= $(CVM_DEBUG)
CVM_VERIFY_HEAP		?= false
CVM_JIT                 ?= false
CVM_JVMDI               ?= false
CVM_JVMPI               ?= false
CVM_JVMPI_TRACE_INSTRUCTION ?= $(CVM_JVMPI)
CVM_THREAD_SUSPENSION   ?= false
CVM_GPROF		?= false
CVM_GCOV		?= false
ifeq ($(CVM_USE_NATIVE_TOOLS), )
CVM_USE_NATIVE_TOOLS    ?= false
endif
CVM_USE_CVM_MEMALIGN    ?= false

ifeq ($(CVM_DEBUG), true)
CVM_OPTIMIZED		?= false
else
CVM_OPTIMIZED		?= true
endif

CVM_PRELOAD_TEST        ?= false
CVM_PRELOAD_LIB         ?= $(CVM_PRELOAD_TEST)
CVM_STATICLINK_LIBS	= $(CVM_PRELOAD_LIB)
CVM_SYMBOLS             ?= $(CVM_DEBUG)
CVM_TERSEOUTPUT         ?= true
CVM_PRODUCT             ?= premium

# %begin lvm
CVM_LVM                 ?= false
# %end lvm
J2ME_CLASSLIB		?= cdc

CVM_CSTACKANALYSIS	?= false
CVM_TIMESTAMPING	?= true
CVM_INCLUDE_COMMCONNECTION ?= false
CVM_DUAL_STACK		?= false

CVM_JIT_REGISTER_LOCALS	?= true
CVM_JIT_USE_FP_HARDWARE ?= false

# NOTE: These options are not officially supported:
# NOTE: CVM_INTERPRETER_LOOP can be set to "Split", "Aligned", or "Standard"

CVM_CLASSLOADING	?= true
CVM_NO_LOSSY_OPCODES    ?= $(CVM_JVMDI)
CVM_REFLECT		?= true
CVM_SERIALIZATION	?= true
CVM_DYNAMIC_LINKING	?= true
CVM_TEST_GC             ?= false
CVM_TEST_GENERATION_GC  ?= false
CVM_INSTRUCTION_COUNTING?= false
CVM_NO_CODE_COMPACTION	?= false
CVM_INTERPRETER_LOOP    ?= Standard
CVM_XRUN		?= false
CVM_CLASSLIB_JCOV       ?= false

CVM_TRACE_JIT           ?= $(CVM_TRACE)
CVM_JIT_ESTIMATE_COMPILATION_SPEED ?= false
CVM_CCM_COLLECT_STATS   ?= false
CVM_JIT_PROFILE  	?= false
CVM_JIT_DEBUG           ?= false

# mTASK
CVM_MTASK                ?= false

# By default build  in the $(CVM_TARGET) directory
CVM_BUILD_SUBDIR  ?= false 

CVM_USE_MEM_MGR		?= false
CVM_MP_SAFE		?= false

# Turn all JIT tracing off if we don't have the jit:
ifneq ($(CVM_JIT), true)
override CVM_TRACE_JIT          = false
override CVM_JIT_COLLECT_STATS  = false
override CVM_JIT_ESTIMATE_COMPILATION_SPEED = false
override CVM_CCM_COLLECT_STATS  = false
override CVM_JIT_PROFILE        = false
override CVM_JIT_DEBUG          = false
endif

#
# prefix and postfix for shared libraries. These can be overriden
# by platform makefiles if they need to be different.
#
ifeq ($(CVM_DEBUG), true)
DEBUG_POSTFIX = _g
endif
LIB_PREFIX = lib
LIB_POSTFIX = $(DEBUG_POSTFIX).so

#
# All build directories relative to CVM_BUILD_TOP
#
CVM_TOP       = ../..
CVM_BUILD_TOP = $(CVM_TOP)/build/$(CVM_TARGET)/$(CVM_BUILD_SUBDIR_NAME)
CVM_LIBDIR = $(CVM_BUILD_TOP)/lib

# Optional Package names
ifneq ($(strip $(OPT_PKGS)),)
  ifeq ($(OPT_PKGS), all)
    OPT_PKGS_DEFS_FILES := $(wildcard ../share/defs_*_pkg.mk)
    OPT_PKGS_LIST  := $(patsubst ../share/defs_%_pkg.mk,%,$(OPT_PKGS_DEFS_FILES))
    OPT_PKGS_NAME  := _$(subst $(space),_,$(strip $(OPT_PKGS_LIST)))
  else
    OPT_PKGS_LIST  := $(subst $(comma),$(space),$(OPT_PKGS))
    OPT_PKGS_NAME  := $(subst $(space),,_$(subst $(comma),_,$(OPT_PKGS)))
    OPT_PKGS_DEFS_FILES := $(patsubst %,defs_%_pkg.mk,$(OPT_PKGS_LIST))
  endif
  OPT_PKGS_RULES_FILES := $(subst defs,rules,$(OPT_PKGS_DEFS_FILES))
  OPT_PKGS_ID_FILES := $(subst defs,id,$(OPT_PKGS_DEFS_FILES))
  # Add optional packages to the J2ME_PRODUCT_NAME
  J2ME_PRODUCT_NAME		+= $(OPT_PKGS_NAME)
endif

#
# Include id makefiles.
#
include ../share/id_$(J2ME_CLASSLIB).mk
ifneq ($(J2ME_PLATFORM),)
-include ../share/id_$(J2ME_PLATFORM).mk
endif 
# This is for identifying binary products, like Personal Profile for Zaurus
-include ../$(TARGET_OS)-$(TARGET_CPU_FAMILY)-$(TARGET_DEVICE)/id_$(J2ME_CLASSLIB).mk

#
# The version values referenced here are setup in the profile id.mk files.
#
J2ME_BUILD_RELEASE		= $(J2ME_BUILD_VERSION)
ifneq ($(J2ME_BUILD_RELEASE),)
 J2ME_BUILD_VERSION_STRING      = $(J2ME_BUILD_RELEASE)-$(J2ME_BUILD_ID)
else
 J2ME_BUILD_VERSION_STRING      = $(J2ME_BUILD_ID)
endif
ifneq ($(CVM_BUILD_VERSION),)
 ifeq ($(CVM_DONT_ADD_BUILD_ID), true)
  CVM_BUILD_VERSION_STRING	= $(CVM_BUILD_VERSION)
 else
  CVM_BUILD_RELEASE		= $(CVM_BUILD_VERSION)
  CVM_BUILD_VERSION_STRING	= $(CVM_BUILD_RELEASE)-$(CVM_BUILD_ID)
 endif
else
 CVM_BUILD_VERSION_STRING	= $(CVM_BUILD_ID)
endif

#
# System property settings (be sure to put values between double quotations)
# 
# Specifications related to the system properties are found at:
# - "J2SE 1.3 API spec (System.getProperties())" at:
#   http://java.sun.com/j2se/1.3/docs/api/java/lang/System.html#getProperties()
# - "JavaTM Product Versioning Specification"
#   http://java.sun.com/j2se/1.3/docs/guide/versioning/spec/VersioningTOC.html
#
#   used in src/share/native/java/lang/System.c
CVM_PROP_JAVA_VERSION		= "$(J2ME_BUILD_VERSION_STRING)"
CVM_PROP_JAVA_VENDOR		= "Sun Microsystems Inc."
CVM_PROP_JAVA_VENDOR_URL	= "http://java.sun.com/"
CVM_PROP_JAVA_VENDOR_URL_BUG	= "http://java.sun.com/cgi-bin/bugreport.cgi"
CVM_PROP_JAVA_SPEC_NAME		= "$(J2ME_PROFILE_NAME) Specification"
CVM_PROP_JAVA_SPEC_VERSION	= "$(J2ME_PROFILE_SPEC_VERSION)"
CVM_PROP_JAVA_SPEC_VENDOR	= "Sun Microsystems Inc."
CVM_PROP_JAVA_CLASS_VERSION	= "47.0"
#   used in src/share/javavm/runtime/jvm.c
CVM_PROP_JAVA_VM_NAME		= "$(CVM_BUILD_NAME)"
CVM_PROP_JAVA_VM_VERSION 	= "$(CVM_BUILD_VERSION_STRING)"
CVM_PROP_SUN_MISC_PRODUCT	= "$(J2ME_PRODUCT_NAME)"
ifeq ($(CVM_JIT), true)
CVM_PROP_JAVA_VM_INFO		= "mixed mode"
else
CVM_PROP_JAVA_VM_INFO		= "interpreter loop"
endif
CVM_PROP_JAVA_VM_VENDOR		= "Sun Microsystems Inc."
CVM_PROP_JAVA_VM_SPEC_NAME	= "Java Virtual Machine Specification"
CVM_PROP_JAVA_VM_SPEC_VERSION	= "1.0"
CVM_PROP_JAVA_VM_SPEC_VENDOR	= "Sun Microsystems Inc."
#   used in src/$(CVM_TARGET)/javavm/runtime/java_props_md.c
CVM_CLASSLIB_JAR_NAME		= "$(J2ME_CLASSLIB)$(OPT_PKGS_NAME).jar"
#   used in src/share/javavm/runtime/utils.c
CVM_JARFILES			= CVM_CLASSLIB_JAR_NAME

ifneq ($(OPT_PKGS_ID_FILES),)
-include $(patsubst %,../share/%,$(OPT_PKGS_ID_FILES))
endif

#   list of property settings that are included in $(CVM_BUILD_DEFS_H) file
CVM_BUILD_DEF_VARS += \
	CVM_PROP_JAVA_VERSION \
	CVM_PROP_JAVA_VENDOR \
	CVM_PROP_JAVA_VENDOR_URL \
	CVM_PROP_JAVA_VENDOR_URL_BUG \
	\
	CVM_PROP_JAVA_SPEC_NAME \
	CVM_PROP_JAVA_SPEC_VERSION \
	CVM_PROP_JAVA_SPEC_VENDOR \
	\
	CVM_PROP_JAVA_CLASS_VERSION \
	\
	CVM_PROP_JAVA_VM_NAME \
	CVM_PROP_JAVA_VM_VERSION \
	CVM_PROP_SUN_MISC_PRODUCT \
	CVM_PROP_JAVA_VM_INFO \
	CVM_PROP_JAVA_VM_VENDOR \
	\
	CVM_PROP_JAVA_VM_SPEC_NAME \
	CVM_PROP_JAVA_VM_SPEC_VERSION \
	CVM_PROP_JAVA_VM_SPEC_VENDOR \
	\
	CVM_CLASSLIB_JAR_NAME \
	CVM_JARFILES

#
# The directory and jar files which the library classes are going to
# be put into. Add in the optional package name.
#
LIB_CLASSESDIR	= $(CVM_BUILD_TOP)/$(J2ME_CLASSLIB)$(OPT_PKGS_NAME)_classes
LIB_CLASSESJAR	= $(CVM_LIBDIR)/$(J2ME_CLASSLIB)$(OPT_PKGS_NAME).jar

#
# command line flags
#
ifeq ($(CVM_OPTIMIZED), true)
	CVM_DEFINES   += -DCVM_OPTIMIZED
endif
ifeq ($(CVM_DEBUG), true)
	CVM_DEFINES   += -DCVM_DEBUG
endif
ifeq ($(CVM_INSPECTOR), true)
	CVM_DEFINES      += -DCVM_INSPECTOR
	override CVM_DEBUG_DUMPSTACK = true
endif
ifeq ($(CVM_CSTACKANALYSIS), true)
        CVM_DEFINES   += -DCVM_CSTACKANALYSIS
endif
ifeq ($(CVM_JAVAC_DEBUG), true)
	JAVAC_OPTIONS += -g
else
	JAVAC_OPTIONS += -g:none
endif
ifeq ($(CVM_CLASSLIB_JCOV), true)
        CVM_DEFINES   += -DCVM_CLASSLIB_JCOV
	JAVAC_OPTIONS += -Xjcov
endif
ifeq ($(CVM_DEBUG_CLASSINFO), true)
	CVM_DEFINES      += -DCVM_DEBUG_CLASSINFO
endif
ifeq ($(CVM_DEBUG_STACKTRACES), true)
	CVM_DEFINES      += -DCVM_DEBUG_STACKTRACES
endif
ifeq ($(CVM_DEBUG_DUMPSTACK), true)
	CVM_DEFINES      += -DCVM_DEBUG_DUMPSTACK
endif
ifeq ($(CVM_DEBUG_ASSERTS), true)
	CVM_DEFINES      += -DCVM_DEBUG_ASSERTS
else
	CVM_DEFINES	 += -DNDEBUG
endif
ifeq ($(CVM_VERIFY_HEAP), true)
	CVM_DEFINES      += -DCVM_VERIFY_HEAP
endif
ifeq ($(CVM_CLASSLOADING), true)
	CVM_DEFINES      += -DCVM_CLASSLOADING
	CVM_DYNAMIC_LINKING = true
else
        override CVM_PRELOAD_LIB = true
endif
# If reflection is explicitly stated to be false by the user, don't
# allow serialization into the build to override that later
ifneq ($(CVM_REFLECT), true)
	override CVM_SERIALIZATION = false
endif
ifeq ($(CVM_SERIALIZATION), true)
	CVM_DEFINES      += -DCVM_SERIALIZATION
        override CVM_REFLECT = true
endif
ifeq ($(CVM_REFLECT), true)
	CVM_DEFINES      += -DCVM_REFLECT
endif
ifeq ($(CVM_XRUN), true)
	CVM_DEFINES      += -DCVM_XRUN
endif
ifeq ($(CVM_JVMDI), true)
	CVM_DEFINES      += -DCVM_JVMDI
	CVM_DYNAMIC_LINKING = true
	override CVM_NO_LOSSY_OPCODES = true
endif
ifeq ($(CVM_JVMPI), true)
        CVM_DEFINES      += -DCVM_JVMPI
	CVM_DYNAMIC_LINKING = true
else
        override CVM_JVMPI_TRACE_INSTRUCTION = false
endif
ifeq ($(CVM_JVMPI_TRACE_INSTRUCTION), true)
	override CVM_NO_LOSSY_OPCODES = true
        override CVM_NO_CODE_COMPACTION = true
        CVM_DEFINES      += -DCVM_JVMPI_TRACE_INSTRUCTION
endif
# NOTE: The CVM_THREAD_SUSPENSION option must only be checked after the JVMDI
# and JVMPI have been checked because those options can override it.
ifeq ($(CVM_THREAD_SUSPENSION), true)
	CVM_DEFINES      += -DCVM_THREAD_SUSPENSION
endif

ifeq ($(CVM_NO_LOSSY_OPCODES), true)
	CVM_DEFINES      += -DCVM_NO_LOSSY_OPCODES
endif
ifeq ($(CVM_INSTRUCTION_COUNTING), true)
	CVM_DEFINES      += -DCVM_INSTRUCTION_COUNTING
endif
# make sure we check CVM_DYNAMIC_LINKING after checking CVM_JVMDI, CVM_JVMPI,
# and CVM_CLASSLOADING.
ifeq ($(CVM_DYNAMIC_LINKING), true)
	CVM_DEFINES      += -DCVM_DYNAMIC_LINKING
endif
ifeq ($(CVM_TERSEOUTPUT), true)
	AT=@
else
	AT=
endif
ifeq ($(CVM_JIT), true)
	CVM_DEFINES   += -DCVM_JIT
endif
ifeq ($(CVM_JIT_USE_FP_HARDWARE), true)
	CVM_DEFINES   += -DCVM_JIT_USE_FP_HARDWARE
endif

ifeq ($(CVM_DUAL_STACK), true)
	CVM_DEFINES   += -DCVM_DUAL_STACK
endif
ifeq ($(CVM_JIT_REGISTER_LOCALS), true)
	CVM_DEFINES   += -DCVM_JIT_REGISTER_LOCALS
endif

ifeq ($(CVM_USE_MEM_MGR), true)
	CVM_DEFINES   += -DCVM_USE_MEM_MGR
endif

ifeq ($(CVM_MP_SAFE), true)
	CVM_DEFINES   += -DCVM_MP_SAFE
endif

# %begin lvm
ifeq ($(CVM_LVM), true)
	CVM_DEFINES   += -DCVM_LVM -DCVM_REMOTE_EXCEPTIONS_SUPPORTED
endif
# %end lvm
ifeq ($(CVM_MTASK), true)
	CVM_DEFINES   += -DCVM_MTASK
endif
ifeq ($(CVM_TEST_GC), true)
        CVM_DEFINES   += -DCVM_TEST_GC
endif

# if CVM_INTERPRETER_LOOP is not defined to any supported option,
# use the default:
override CVM_INTERPRETER_LOOP := $(subst Loop,,$(CVM_INTERPRETER_LOOP))
ifneq ($(CVM_INTERPRETER_LOOP), Split)
ifneq ($(CVM_INTERPRETER_LOOP), Aligned)
ifneq ($(CVM_INTERPRETER_LOOP), Standard)
ifeq ($(CVM_INTERPRETER_LOOP), )
	CVM_INTERPRETER_LOOP = Standard
else
	invalid value for CVM_INTERPRETER_LOOP
endif
endif
endif
endif

ifeq ($(CVM_TIMESTAMPING), true)
	CVM_DEFINES += -DCVM_TIMESTAMPING
endif

ifeq ($(CVM_TRACE), true)
        CVM_DEFINES   += -DCVM_TRACE
        CVM_TRACE_ENABLED := true
endif
ifeq ($(CVM_PROFILE_METHOD), true)
        CVM_DEFINES   += -DCVM_PROFILE_METHOD
        CVM_TRACE_ENABLED := true
endif
ifeq ($(CVM_PROFILE_CALL), true)
        CVM_DEFINES   += -DCVM_PROFILE_CALL
        CVM_TRACE_ENABLED := true
endif

ifeq ($(CVM_TRACE_JIT), true)
        CVM_DEFINES   += -DCVM_TRACE_JIT
endif

ifeq ($(CVM_JIT_COLLECT_STATS), true)
        CVM_DEFINES   += -DCVM_JIT_COLLECT_STATS
endif

ifeq ($(CVM_JIT_ESTIMATE_COMPILATION_SPEED), true)
        CVM_DEFINES   += -DCVM_JIT_ESTIMATE_COMPILATION_SPEED
endif

ifeq ($(CVM_CCM_COLLECT_STATS), true)
        CVM_DEFINES   += -DCVM_CCM_COLLECT_STATS
endif

ifeq ($(CVM_JIT_PROFILE), true)
        CVM_DEFINES   += -DCVM_JIT_PROFILE
endif

ifeq ($(CVM_JIT_DEBUG), true)
        CVM_DEFINES   += -DCVM_JIT_DEBUG
        CVM_DEFINES   += -DCVM_DEBUG_JIT_TRACE_CODEGEN_RULE_EXECUTION
endif

ifeq ($(CVM_PRELOAD_LIB), true)
	CVM_DEFINES   += -DCVM_PRELOAD_LIB
	CVM_BUILD_LIB_CLASSESJAR = false
else
	CVM_BUILD_LIB_CLASSESJAR = true
endif

ifeq ($(CVM_STATICLINK_LIBS), true)
	CVM_DEFINES   += -DCVM_STATICLINK_LIBS
endif

ifeq ($(CDC_10),true)
CVM_DEFINES += -DCDC_10
endif

CVM_DEFINES += -DJ2ME_CLASSLIB=$(J2ME_CLASSLIB)
CVM_DEFINES += -DTARGET_CPU_FAMILY=$(TARGET_CPU_FAMILY)

# The check for CVM_TRACE_ENABLED must come at the bottom because it is
# set based on other build options above.
ifeq ($(CVM_TRACE_ENABLED), true)
        CVM_DEFINES   += -DCVM_TRACE_ENABLED
endif

#
# All the build flags we need to keep track of in case they are toggled.
#
# WARNING: None of the cleanup actions should try to delete flags
# or the generated directory or problems will occur.
#
CVM_FLAGS += \
	CVM_HOST \
	CVM_SYMBOLS \
	CVM_OPTIMIZED \
	CVM_DEBUG \
	CVM_JAVAC_DEBUG \
	CVM_DEBUG_CLASSINFO \
	CVM_DEBUG_STACKTRACES \
	CVM_DEBUG_DUMPSTACK \
	CVM_INSPECTOR \
	CVM_DEBUG_ASSERTS \
	CVM_VERIFY_HEAP \
	CVM_CLASSLOADING \
	CVM_NO_LOSSY_OPCODES \
	CVM_INSTRUCTION_COUNTING \
	CVM_GCCHOICE \
	CVM_NO_CODE_COMPACTION \
	CVM_XRUN \
	CVM_JVMDI \
	CVM_JVMPI \
	CVM_JVMPI_TRACE_INSTRUCTION \
	CVM_THREAD_SUSPENSION \
	CVM_CLASSLIB_JCOV \
	CVM_REFLECT \
	CVM_SERIALIZATION \
	CVM_STATICLINK_LIBS \
	CVM_PRELOAD_LIB \
	CVM_PRELOAD_TEST \
	CVM_DYNAMIC_LINKING \
	CVM_TEST_GC \
	CVM_TEST_GENERATION_GC \
	CVM_TIMESTAMPING \
	CVM_INCLUDE_COMMCONNECTION \
	CVM_DUAL_STACK \
	CVM_KNI \
	CVM_JIT_REGISTER_LOCALS \
	CVM_INTERPRETER_LOOP \
	CVM_JIT \
	CVM_JIT_USE_FP_HARDWARE \
	J2ME_CLASSLIB	\
	CVM_CSTACKANALYSIS \
	CVM_TRACE \
	CVM_TRACE_JIT \
	CVM_JIT_COLLECT_STATS \
	CVM_JIT_ESTIMATE_COMPILATION_SPEED \
	CVM_CCM_COLLECT_STATS \
	CVM_JIT_PROFILE \
	CVM_JIT_DEBUG \
	OPT_PKGS \
        CVM_PRODUCT \
	CVM_GPROF \
	CVM_GCOV \
	CVM_USE_CVM_MEMALIGN \
	CVM_USE_MEM_MGR \
	CVM_MP_SAFE \
	CVM_USE_NATIVE_TOOLS \
	CVM_MTASK \
	CVM_JIT_REGISTER_LOCALS

# %begin lvm
CVM_FLAGS += \
	CVM_LVM
# %end lvm

CVM_DEFAULT_CLEANUP_ACTION 	= \
	rm -rf $(CVM_OBJDIR)
CVM_HOST_CLEANUP_ACTION 	= \
	rm -rf $(CVM_JCS_BUILDDIR)
CVM_JAVAC_DEBUG_CLEANUP_ACTION 	= \
	rm -rf .*classes *_classes .*.list \
	       $(CVM_BUILDTIME_CLASSESDIR) $(CVM_BUILDTIME_CLASSESZIP) \
	       $(CVM_TEST_CLASSESDIR) $(CVM_TEST_CLASSESZIP) \
	       $(CVM_DEMO_CLASSESDIR) $(CVM_DEMO_CLASSESJAR)
CVM_DEBUG_CLASSINFO_CLEANUP_ACTION  = \
	rm -rf $(CVM_OBJDIR) $(CVM_ROMJAVA_CPATTERN)*
CVM_NO_LOSSY_OPCODES_CLEANUP_ACTION = \
	rm -rf $(CVM_OBJDIR) $(CVM_ROMJAVA_CPATTERN)*
CVM_CLASSLOADING_CLEANUP_ACTION     = \
	rm -rf $(CVM_OBJDIR) $(CVM_ROMJAVA_CPATTERN)* \
	       $(CVM_BUILDTIME_CLASSESZIP) \
		.buildtimeclasses
CVM_INSTRUCTION_COUNTING_CLEANUP_ACTION = \
        rm -f $(CVM_OBJDIR)/opcodes.o $(CVM_OBJDIR)/executejava*.o \
	     $(CVM_OBJDIR)/jni_impl.o
CVM_GCCHOICE_CLEANUP_ACTION 	= \
	mkdir -p $(CVM_DERIVEDROOT)/javavm/include/; \
	echo \\\#include \"javavm/include/gc/$(CVM_GCCHOICE)/gc_config.h\" \
	 	 > $(CVM_DERIVEDROOT)/javavm/include/gc_config.h
CVM_NO_CODE_COMPACTION_CLEANUP_ACTION = \
	rm -rf $(CVM_ROMJAVA_CPATTERN)*

CVM_REFLECT_CLEANUP_ACTION = \
	$(CVM_JAVAC_DEBUG_CLEANUP_ACTION) \
	$(CVM_OBJDIR)/jni_impl.o $(CVM_OBJDIR)/jvm.o $(CVM_OBJDIR)/reflect.o \
	$(CVM_ROMJAVA_CPATTERN)*

CVM_SERIALIZATION_CLEANUP_ACTION = \
	$(CVM_JAVAC_DEBUG_CLEANUP_ACTION) \
	$(CVM_OBJDIR)/jvm.o $(CVM_ROMJAVA_CPATTERN)*

CVM_PRELOAD_LIB_CLEANUP_ACTION = \
	rm -rf $(CVM_ROMJAVA_CPATTERN)* \
	$(DEFAULTLOCALELIST_JAVA) \
	$(CVM_BUILDTIME_CLASSESDIR) \
	$(CVM_BUILDTIME_CLASSESZIP) .buildtimeclasses \
	$(LIB_CLASSESJAR) $(LIB_CLASSESDIR)

CVM_STATICLINK_LIBS_CLEANUP_ACTION = \
	rm -rf $(CVM_LIBDIR) $(CVM_BINDIR)

CVM_PRELOAD_TEST_CLEANUP_ACTION = \
	rm -rf $(CVM_ROMJAVA_CPATTERN)* \
	$(CVM_BUILDTIME_CLASSESDIR) \
	$(CVM_BUILDTIME_CLASSESZIP) .buildtimeclasses \
	$(CVM_TEST_CLASSESDIR) \
	$(CVM_TEST_CLASSESZIP)

CVM_DYNAMIC_LINKING_CLEANUP_ACTION = \
	rm -f $(CVM_OBJDIR)/jvm.o $(CVM_OBJDIR)/jni_impl.o \
		$(CVM_OBJDIR)/linker_md.o $(CVM_OBJDIR)/common_exceptions.o

CVM_INTERPRETER_LOOP_CLEANUP_ACTION = \
        rm -f $(CVM_OBJDIR)/executejava*.o

CVM_CSTACKANALYSIS_CLEANUP_ACTION = \
	rm -rf $(CVM_OBJDIR)/*.asm $(CVM_OBJDIR)/*.o

CVM_JIT_CLEANUP_ACTION = \
	rm -rf $(CVM_OBJDIR) $(CVM_ROMJAVA_CPATTERN)* \
	       $(CVM_DERIVEDROOT)/javavm/runtime/jit/* \
	       $(CVM_DERIVEDROOT)/javavm/include/jit/* \
	       $(CVM_BUILDTIME_CLASSESZIP) \
	       .buildtimeclasses \
	       $(CVM_BUILDTIME_CLASSESDIR) \
	       $(CVM_TEST_CLASSESDIR)

# %begin lvm
CVM_LVM_CLEANUP_ACTION = \
	rm -rf $(CVM_OBJDIR) $(CVM_ROMJAVA_CPATTERN)* \
	       $(CVM_BUILDTIME_CLASSESZIP) \
	       .buildtimeclasses \
	       $(CVM_BUILDTIME_CLASSESDIR)/sun/misc/*LogicalVM*.class \
	       $(CVM_TEST_CLASSESDIR)/lvmtest \
	       $(CVM_TEST_CLASSESZIP)
# %end lvm

CVM_DEBUG_ASSERTS_CLEANUP_ACTION = \
	rm -rf $(BUILDFLAGS_JAVA) \
		$(CVM_JAVAC_DEBUG_CLEANUP_ACTION) \
		$(CVM_DEFAULT_CLEANUP_ACTION) 

CVM_DEBUG_CLEANUP_ACTION 		= $(CVM_DEFAULT_CLEANUP_ACTION)
CVM_OPTIMIZED_CLEANUP_ACTION 		= $(CVM_DEFAULT_CLEANUP_ACTION)
CVM_SYMBOLS_CLEANUP_ACTION 		= $(CVM_DEFAULT_CLEANUP_ACTION)
CVM_DEBUG_STACKTRACES_CLEANUP_ACTION 	= $(CVM_DEFAULT_CLEANUP_ACTION)
CVM_DEBUG_DUMPSTACK_CLEANUP_ACTION 	= $(CVM_DEFAULT_CLEANUP_ACTION)
CVM_INSPECTOR_CLEANUP_ACTION	 	= $(CVM_DEBUG_CLASSINFO_CLEANUP_ACTION)
CVM_VERIFY_HEAP_CLEANUP_ACTION 		= $(CVM_DEFAULT_CLEANUP_ACTION)
CVM_XRUN_CLEANUP_ACTION			= $(CVM_DEFAULT_CLEANUP_ACTION)
CVM_JVMDI_CLEANUP_ACTION		= $(CVM_DEFAULT_CLEANUP_ACTION)
CVM_JVMPI_CLEANUP_ACTION                = \
        $(CVM_DEFAULT_CLEANUP_ACTION)     \
        $(CVM_DEBUG_CLASSINFO_CLEANUP_ACTION)
CVM_JVMPI_TRACE_INSTRUCTION_CLEANUP_ACTION = $(CVM_JVMPI_CLEANUP_ACTION)
CVM_THREAD_SUSPENSION_CLEANUP_ACTION	= $(CVM_DEFAULT_CLEANUP_ACTION)
CVM_TEST_GC_CLEANUP_ACTION              = $(CVM_DEFAULT_CLEANUP_ACTION)
CVM_TEST_GENERATION_GC_CLEANUP_ACTION   = $(CVM_DEFAULT_CLEANUP_ACTION)

CVM_TIMESTAMPING_CLEANUP_ACTION         = \
	rm -rf $(CVM_OBJDIR) $(CVM_ROMJAVA_CPATTERN)* \
	       $(CVM_OBJDIR)/libromjava.a \
	       $(CVM_BUILDTIME_CLASSESZIP) \
	       .buildtimeclasses \
	       $(CVM_BUILDTIME_CLASSESDIR)/sun/misc/TimeStamps.class \
	       $(CVM_TEST_CLASSESDIR)/TimeStampsTest.class

CVM_CLASSLIB_JCOV_CLEANUP_ACTION		= \
	$(CVM_DEFAULT_CLEANUP_ACTION) 	   	  \
	$(CVM_JAVAC_DEBUG_CLEANUP_ACTION)

CVM_TRACE_CLEANUP_ACTION               = $(CVM_DEFAULT_CLEANUP_ACTION)
CVM_TRACE_JIT_CLEANUP_ACTION           = $(CVM_DEFAULT_CLEANUP_ACTION)
CVM_INCLUDE_COMMCONNECTION_CLEANUP_ACTION        = \
	$(CVM_DEFAULT_CLEANUP_ACTION)    \
	$(CVM_JAVAC_DEBUG_CLEANUP_ACTION)
CVM_DUAL_STACK_CLEANUP_ACTION          = $(CVM_DEFAULT_CLEANUP_ACTION)
CVM_KNI_CLEANUP_ACTION                 = $(CVM_DEFAULT_CLEANUP_ACTION)
CVM_JIT_REGISTER_LOCALS_CLEANUP_ACTION = $(CVM_JIT_CLEANUP_ACTION)
CVM_JIT_COLLECT_STATS_CLEANUP_ACTION   = $(CVM_JIT_CLEANUP_ACTION)
CVM_JIT_ESTIMATE_COMPILATION_SPEED_CLEANUP_ACTION = $(CVM_JIT_CLEANUP_ACTION)
CVM_CCM_COLLECT_STATS_CLEANUP_ACTION   = $(CVM_JIT_CLEANUP_ACTION)
CVM_JIT_PROFILE_CLEANUP_ACTION	       = $(CVM_JIT_CLEANUP_ACTION)
CVM_JIT_DEBUG_CLEANUP_ACTION = \
        rm -rf $(CVM_OBJDIR)/jit* $(CVM_OBJDIR)/ccm*  \
	       $(CVM_DERIVEDROOT)/javavm/runtime/jit/* \
	       $(CVM_DERIVEDROOT)/javavm/include/jit/*
CVM_JIT_USE_FP_HARDWARE_CLEANUP_ACTION = $(CVM_JIT_CLEANUP_ACTION)

OPT_PKGS_CLEANUP_ACTION                = $(J2ME_CLASSLIB_CLEANUP_ACTION)
CVM_PRODUCT_CLEANUP_ACTION             = rm -f $(CVM_OBJDIR)/jvm.o \
                                         rm -f $(INSTALLDIR)/src.zip
CVM_GPROF_CLEANUP_ACTION 	       = $(CVM_DEFAULT_CLEANUP_ACTION)
CVM_GCOV_CLEANUP_ACTION 	       = $(CVM_DEFAULT_CLEANUP_ACTION)
CVM_USE_NATIVE_TOOLS_CLEANUP_ACTION    = $(CVM_DEFAULT_CLEANUP_ACTION)
CVM_USE_CVM_MEMALIGN_CLEANUP_ACTION    = $(CVM_DEFAULT_CLEANUP_ACTION)
CVM_USE_MEM_MGR_CLEANUP_ACTION         = $(CVM_DEFAULT_CLEANUP_ACTION)
CVM_MP_SAFE_CLEANUP_ACTION	       = $(CVM_DEFAULT_CLEANUP_ACTION)
CVM_MTASK_CLEANUP_ACTION                = $(CVM_DEFAULT_CLEANUP_ACTION)

#
# Wipe out objects and classes when J2ME_CLASSLIB changes.
#
J2ME_CLASSLIB_CLEANUP_ACTION            = \
	$(CVM_JAVAC_DEBUG_CLEANUP_ACTION) ; \
	$(CVM_DEFAULT_CLEANUP_ACTION) ; \
	rm -rf $(CVM_LIBDIR)

# generate header dependency files
GENERATEMAKEFILES = true

# use the generational gc by default. Other possible GC choices are:
#	semispace
# 	marksweep
#       generational-seg
#
CVM_GCCHOICE   ?= generational

#
# And by default, do not use a segmented heap for generational GC
#
CVM_GC_SEGMENTED_HEAP = false

ifeq ($(CVM_TEST_GC), true)
        override CVM_GCCHOICE   = semispace
	override CVM_GC_SEGMENTED_HEAP = false
endif

#
# There should be a GC-specific makefile.
# This special-case will do for now.
#
ifeq ($(CVM_GCCHOICE), generational)
    CVM_SHAREOBJS_SPEED += \
        gen_semispace.o \
	gen_markcompact.o
    override CVM_GC_SEGMENTED_HEAP=false
endif

ifeq ($(CVM_GCCHOICE), generational-seg)
    CVM_SHAREOBJS_SPEED += \
	gen_segment.o \
        gen_eden.o \
        gen_edenspill.o \
	gen_markcompact.o
    override CVM_GC_SEGMENTED_HEAP=true
endif

ifeq ($(CVM_GC_SEGMENTED_HEAP), true)
    CVM_DEFINES   += -DCVM_SEGMENTED_HEAP
endif

ifeq ($(CVM_USE_CVM_MEMALIGN), true)
    CVM_SHAREOBJS_SPACE += \
        memory_aligned.o
    CVM_DEFINES   += -DCVM_USE_CVM_MEMALIGN
endif

#
# We will be including the TimeStampsTest when the 
# the CVM is build with CVM_TIMESTAMPING=true
#
ifeq ($(CVM_TIMESTAMPING), true)
    CVM_BUILDTIME_CLASSES += \
        sun.misc.TimeStamps
    CVM_SHAREOBJS_SPACE += \
	TimeStamps.o \
	timestamp.o
    CVM_TEST_CLASSES += \
	TimeStampsTest
endif

#
# Object and data files needed for dual stack support
#
ifeq ($(CVM_DUAL_STACK), true)
    CVM_SHAREOBJS_SPACE += \
	MemberFilter.o \
	AuxPreloadClassLoader.o \
	auxPreloader.o
    CVM_MIDPFILTERCONFIG = $(CVM_LIBDIR)/MIDPFilterConfig.txt
    CVM_MIDPCLASSLIST    = $(CVM_LIBDIR)/MIDPPermittedClasses.txt
    CVM_MIDPDIR          = $(CVM_TOP)/src/share/lib
endif

#
# Stuff needed for KNI support
#
ifeq ($(CVM_KNI), true)
    CVM_SHAREOBJS_SPACE += \
	kni_impl.o \
	KNITest.o
    CVM_TEST_CLASSES += KNITest
    CVM_SRCDIRS += $(CVM_TESTCLASSES_SRCDIR)
    CVM_CNI_CLASSES += KNITest
endif

#
# Directories
#
CVM_JCSDIR               = $(CVM_BUILD_TOP)/jcs
CVM_OBJDIR               = $(CVM_BUILD_TOP)/obj
CVM_BINDIR               = $(CVM_BUILD_TOP)/bin
CVM_DERIVEDROOT          = $(CVM_BUILD_TOP)/generated
CVM_BUILDTIME_CLASSESDIR = $(CVM_BUILD_TOP)/btclasses
CVM_TEST_CLASSESDIR      = $(CVM_BUILD_TOP)/testclasses
CVM_DEMO_CLASSESDIR	 = $(CVM_BUILD_TOP)/democlasses
CVM_SHAREROOT  		 = $(CVM_TOP)/src/share

#
# Directory where javadocs, source bundles, and binary bundle get installed.
#
INSTALLDIR	= $(CVM_TOP)/install

#
# Full path name for Binary Bundle
#
TM = (TM)
BUNDLE_PRODUCT_NAME0 = $(subst $(space),_,$(J2ME_PRODUCT_NAME))
BUNDLE_PRODUCT_NAME1 = $(subst /,_,$(BUNDLE_PRODUCT_NAME0))
BUNDLE_PRODUCT_NAME  = $(subst $(TM),$(empty),$(BUNDLE_PRODUCT_NAME1))

BINARY_BUNDLE = $(INSTALLDIR)/$(BUNDLE_PRODUCT_NAME)-$(J2ME_BUILD_VERSION_STRING)-$(CVM_TARGET).tar.gz

#
# Java source directories. 
#

CVM_TESTCLASSES_SRCDIR    = $(CVM_SHAREROOT)/javavm/test
CVM_CLDCCLASSES_SRCDIR    = $(CVM_SHAREROOT)/classes/cldc
CVM_SHAREDCLASSES_SRCDIR  = $(CVM_SHAREROOT)/classes
CVM_TARGETCLASSES_SRCDIR  = $(CVM_TARGETROOT)/classes

CVM_BUILDTIME_CLASSESZIP = $(CVM_BUILD_TOP)/btclasses.zip
CVM_TEST_CLASSESZIP      = $(CVM_BUILD_TOP)/testclasses.zip
CVM_DEMO_CLASSESJAR	 = $(CVM_BUILD_TOP)/democlasses.jar

# Security properties and policy files
DO_SECURITY_PROVIDER_FILTERING = false
CVM_PROPS_SRC   = $(CVM_TOP)/src/share/lib/security/java.security
CVM_PROPS_BUILD = $(CVM_LIBDIR)/security/java.security
CVM_POLICY_BUILD  = $(CVM_LIBDIR)/security/java.policy

# Build option record file to generate
CVM_BUILD_DEFS_H = $(CVM_DERIVEDROOT)/javavm/include/build_defs.h

# sun.misc.DefaultLoacleList.java
DEFAULTLOCALELIST_JAVA = $(CVM_DERIVEDROOT)/classes/sun/misc/DefaultLocaleList.java

ifeq ($(CVM_TEST_GC), true)
include ../share/testgc.mk
endif

ifeq ($(CVM_TEST_GENERATION_GC), true)
    CVM_SRCDIRS += \
	$(CVM_SHAREROOT)/javavm/test/GenerationGCTest/Csrc 
    CVM_INCLUDES  += \
	-I$(CVM_SHAREROOT)/javavm/test/GenerationGCTest/Include 
    CVM_SHAREOBJS_SPACE += \
	BarrierTest.o
endif

CVM_SRCDIRS    += \
	$(CVM_SHAREROOT)/javavm/runtime \
	$(CVM_DERIVEDROOT)/javavm/runtime \
	$(CVM_SHAREROOT)/javavm/runtime/gc/$(CVM_GCCHOICE) \
	$(CVM_SHAREROOT)/javavm/native/sun/io \
	$(CVM_SHAREROOT)/javavm/native/sun/misc \
	$(CVM_SHAREROOT)/javavm/native/java/lang \
	$(CVM_SHAREROOT)/javavm/native/java/lang/reflect \
	$(CVM_SHAREROOT)/javavm/native/java/security \
	$(CVM_SHAREROOT)/javavm/native/java/util \
	$(CVM_SHAREROOT)/native/common \
	$(CVM_SHAREROOT)/native/java/lang \
	$(CVM_SHAREROOT)/native/java/lang/ref \
	$(CVM_SHAREROOT)/native/java/lang/reflect \
	$(CVM_SHAREROOT)/native/java/lang/fdlibm/src \
	$(CVM_SHAREROOT)/native/java/util \
	$(CVM_SHAREROOT)/native/java/io \
	$(CVM_SHAREROOT)/native/java/net \
	$(CVM_SHAREROOT)/native/java/util/zip \
	$(CVM_SHAREROOT)/native/java/util/zip/zlib-1.1.3 \
	$(CVM_SHAREROOT)/native/sun/misc \

ifeq ($(CVM_MTASK), true)
CVM_SRCDIRS += \
	$(CVM_SHAREROOT)/native/sun/mtask
endif

ifeq ($(CVM_JIT), true)
CVM_SRCDIRS += \
	$(CVM_DERIVEDROOT)/javavm/runtime/jit \
	$(CVM_SHAREROOT)/javavm/runtime/jit
endif

# This is for compatibility with the rmi makefiles,
# which still use PROFILE_SRCDIR
PROFILE_SRCDIR = $(PROFILE_SRCDIRS)

#
# some build directories that need to be created.
#
CVM_BUILDDIRS  += \
	$(CVM_OBJDIR) \
	$(CVM_BINDIR) \
	$(CVM_DERIVEDROOT)/javavm/runtime \
        $(CVM_DERIVEDROOT)/javavm/runtime/opcodeconsts \
	$(CVM_DERIVEDROOT)/javavm/include \
	$(CVM_DERIVEDROOT)/classes \
	$(CVM_DERIVEDROOT)/classes/sun/misc \
	$(CVM_DERIVEDROOT)/jni \
	$(CVM_DERIVEDROOT)/cni \
	$(CVM_DERIVEDROOT)/offsets \
	$(CVM_DERIVEDROOT)/flags \
	$(CVM_BUILDTIME_CLASSESDIR) \
	$(CVM_TEST_CLASSESDIR) \
	$(CVM_DEMO_CLASSESDIR) \
	$(CVM_LIBDIR) \
	$(CVM_LIBDIR)/security \
	$(CVM_MISC_TOOLS_CLASSPATH)

ifneq ($(CVM_PRELOAD_LIB), true)
CVM_BUILDDIRS  += \
	$(LIB_CLASSESDIR)
endif

ifeq ($(CVM_JIT), true)
CVM_BUILDDIRS  += \
	$(CVM_DERIVEDROOT)/javavm/runtime/jit \
	$(CVM_DERIVEDROOT)/javavm/include/jit \
	$(CVM_JCSDIR)
endif

#
# C include directories
#
CVM_INCLUDES   += \
	-I$(CVM_SHAREROOT) \
	-I$(CVM_BUILD_TOP) \

#
# These are for the convenience of external code like
# JDK native method libraries that like to #include
# "jni.h" and "java_lang_String.h", etc.  We should
# only need these for those .c files, but gnumake
# doesn't support target-specific macros.
#
CVM_INCLUDES  += \
	-I$(CVM_SHAREROOT)/javavm/export \
	-I$(CVM_SHAREROOT)/native/common \
	-I$(CVM_SHAREROOT)/native/java/lang \
	-I$(CVM_SHAREROOT)/native/java/lang/fdlibm/include \
	-I$(CVM_SHAREROOT)/native/java/net \
	-I$(CVM_SHAREROOT)/native/java/io \
	-I$(CVM_SHAREROOT)/native/java/util/zip \
	-I$(CVM_SHAREROOT)/native/java/util/zip/zlib-1.1.3 \
	-I$(CVM_DERIVEDROOT)/jni \

ifneq ($(KBENCH_JAR),)
CVM_TEST_JARFILES += $(KBENCH_JAR)
endif

#
# Classes to build
#
CVM_TEST_CLASSES += \
	ClassLoaderTest \
	HelloWorld \
	EllisGC_ST \
	ExceptionTest \
	StaticFieldTest \
	MTGC \
	MPStress \
	FastSync \
	InterruptTest \
	ThreadsAndSync \
	ThreadSuspend \
	DaemonThreadTest \
	Test \
	ManyFieldsAndMethods \
	ExceptionThrowingOpcodesTest \
	UncaughtExceptionTest \
	StringSignatureTest \
	InterfaceTest \
	DebuggerTest \
	setTime \
	TimeZoneTest \
	CVMadddec \
	ShowSysProps \
	ConvertBoundTest \
	ConvertStressTest \
	SurrogateTest \
	TestLongShiftSpeed \
	TestSync \
	Scopetest \
	scopetest.a.C2 \
	scopetest.b.C1 \
	cvmtest.TypeidRefcountHelper

ifneq ($(CDC_10),true)
CVM_TEST_CLASSES += \
	ChainedExceptionTest
endif

#
# The following tests make up a LOT of classes. Make sure we are not
# preloading them.
#
ifneq ($(CVM_PRELOAD_LIB), true)
CVM_TEST_CLASSES += \
	ClassLink \
	ClassisSubclassOf
endif

# Note: this test removed now that it is known to work. If you want to
# add it back in you must change the CVM_DYNAMIC_LINKING_CLEANUP_ACTION
# above.
# ifeq ($(CVM_DYNAMIC_LINKING), true)
# 	CVM_TEST_CLASSES += DynLinkTest
# endif

ifeq ($(CVM_REFLECT), true)
	CVM_TEST_CLASSES += \
		ReflectionTest \
		ReflectionTestSamePackageHelper \
		cvmtest.ReflectionTestOtherPackageHelper \
		ReflectionClinitTest \
		ReflectionSecurity \
		ReflectionStackOverflowTest
endif

#
# Classes with CNI native methods
#
CVM_CNI_CLASSES += sun.io.ByteToCharISO8859_1 \
		   sun.io.CharToByteISO8859_1 \
		   sun.misc.CVM \
		   java.security.AccessController \
		   java.lang.reflect.Constructor \
		   java.lang.reflect.Field \
		   java.lang.reflect.Method \
		   java.lang.String \
		   java.util.Vector \
		   java.lang.StringBuffer

ifeq ($(CVM_JVMPI), true)
CVM_CNI_CLASSES += sun.misc.CVMJVMPI
endif

ifeq ($(CVM_INSPECTOR), true)
CVM_TEST_CLASSES += \
	cvmsh

CVM_BUILDTIME_CLASSES += \
	sun.misc.VMInspector

CVM_SHAREOBJS_SPACE += \
	VMInspector.o \
	inspector.o

endif

#
# Classes that the VM needs to have field offsets for.
#
CVM_OFFSETS_CLASSES += \
	java.lang.String \
	java.lang.Throwable \
	java.lang.StackTraceElement \
	java.lang.Class \
	java.lang.Thread \
	java.lang.Boolean \
	java.lang.Byte \
	java.lang.Character \
	java.lang.Short \
	java.lang.Integer \
	java.lang.Long \
	java.lang.Float \
	java.lang.Double \
	java.lang.ref.Reference \
	java.util.AbstractList \
	java.util.Vector \
	sun.io.ByteToCharConverter \
	sun.io.CharToByteConverter \
	sun.io.CharToByteISO8859_1 \
	java.lang.StringBuffer \
	java.lang.AssertionStatusDirectives

ifeq ($(CVM_CLASSLOADING), true)
CVM_OFFSETS_CLASSES += \
	java.lang.ClassLoader 
endif

ifeq ($(CVM_REFLECT), true)
	CVM_OFFSETS_CLASSES += \
		java.lang.reflect.AccessibleObject \
		java.lang.reflect.Constructor \
		java.lang.reflect.Field \
		java.lang.reflect.InvocationTargetException \
		java.lang.reflect.Method
endif

ifeq ($(CVM_JVMPI), true)
CVM_OFFSETS_CLASSES += \
        java.lang.ThreadGroup
endif

#
# The objects that make up zlib-1.1.3
#
ZLIBOBJS = \
	CRC32.o \
	ZipFile.o \
	ZipEntry.o \
	zadler32.o \
	zcrc32.o \
	deflate.o \
	trees.o \
	zutil.o \
	inflate.o \
	infblock.o \
	inftrees.o \
	infcodes.o \
	infutil.o \
	inffast.o \
	zip_util.o

#
# The objects that make up the 'fdlibm' math library
#
MATHOBJS = \
	e_acos.o \
	e_asin.o \
	e_atan2.o \
	e_exp.o \
	e_fmod.o \
	e_log.o \
	e_pow.o \
	e_rem_pio2.o \
	e_remainder.o \
	e_sqrt.o \
	k_cos.o \
	k_rem_pio2.o \
	k_sin.o \
	k_tan.o \
	s_atan.o \
	s_ceil.o \
	s_copysign.o \
	s_cos.o \
	s_fabs.o \
	s_floor.o \
	s_isnan.o \
	s_rint.o \
	s_scalbn.o \
	s_signgam.o \
	s_sin.o \
	s_tan.o \
	w_acos.o \
	w_asin.o \
	w_atan2.o \
	w_exp.o \
	w_fmod.o \
	w_gamma.o \
	w_log.o \
	w_pow.o \
	w_remainder.o \
	w_sqrt.o

#
# The following fdlibm files aren't used, so we don't bother build them.
# They are kept here as a referenced.
#
X_MATHOBJS += \
	e_acosh.o \
	e_atanh.o \
	e_cosh.o \
	e_gamma.o \
	e_gamma_r.o \
	e_hypot.o \
	e_j0.o \
	e_j1.o \
	e_jn.o \
	e_lgamma.o \
	e_lgamma_r.o \
	e_log10.o \
	e_scalb.o \
	e_sinh.o \
	k_standard.o \
	s_asinh.o \
	s_cbrt.o \
	s_erf.o \
	s_expm1.o \
	s_finite.o \
	s_frexp.o \
	s_ilogb.o \
	s_ldexp.o \
	s_log1p.o \
	s_logb.o \
	s_matherr.o \
	s_modf.o \
	s_nextafter.o \
	s_significand.o \
	s_tanh.o \
	w_acosh.o \
	w_atanh.o \
	w_cosh.o \
	w_gamma_r.o \
	w_hypot.o \
	w_j0.o \
	w_j1.o \
	w_jn.o \
	w_lgamma.o \
	w_lgamma_r.o \
	w_log10.o \
	w_scalb.o \
	w_sinh.o \

#
# Compile JIT.o unconditionally. Source does nothing if
# CVM_JIT undefined.
#
CVM_SHAREOBJS_SPACE += \
	JIT.o	

#
# JIT-specific objects
#
ifeq ($(CVM_JIT), true)

CVM_SHAREOBJS_SPACE += \
	jitcompile.o \
	jitirnode.o \
	jitirlist.o \
	jitirrange.o \
	jitirblock.o \
	jitirdump.o \
	jitstackmap.o \
        jitcodebuffer.o \
        jitconstantpool.o \
        jitintrinsic.o \
        jitopcodemap.o \
	jitpcmap.o \
	jitutils.o \
	jitmemory.o \
	jitset.o \
	jitcomments.o \
	jitstats.o \
	jitdebug.o

CVM_SHAREOBJS_SPEED += \
	jitir.o \
	jitopt.o \
	jit_common.o \
        ccm_runtime.o \
        ccmintrinsics.o

#
# JIT-specific tests
#
CVM_TEST_CLASSES += \
	MicroBench \
	CompilerTest \
	jittest.simple \
	jittest.multiJoin \
	assign \
	runRunAll \
	runNamedTest \
	MethodCall \
	Fib \
	ExerciseOpcodes \
	DoResolveAndClinit

ifneq ($(KBENCH_JAR),)
CVM_TEST_CLASSES += RunKBench 
endif

endif

# %begin lvm
#
# Logical VM-specific objects
#
ifeq ($(CVM_LVM), true)
CVM_SHAREOBJS_SPACE += \
	lvm.o \
	LogicalVM.o

#
# Logical VM-specific tests
#
CVM_TEST_CLASSES += \
	lvmtest.LVMLauncher \
	lvmtest.PlainLauncher

endif
# %end lvm

ifeq ($(CVM_MTASK), true)
CVM_SHAREOBJS_SPACE += \
	mtask.o \
	Listener.o
endif

#
# Objects to build
#
ifeq ($(CVM_CLASSLOADING), true)
CVM_SHAREOBJS_SPACE += \
	classlink.o \
	classverify.o \
	constantpool.o \
	mangle.o \
	quicken.o \
	verifycode.o
endif

CVM_SHAREOBJS_SPEED += \
	gc_common.o \
	gc_impl.o \
	gc_stat.o \
	indirectmem.o \
	interpreter.o \
	named_sys_monitor.o \
	objsync.o \
	stackmaps.o \
	sync.o

CVM_SHAREOBJS_SPACE += \
	basictypes.o \
	bcattr.o \
	bcutils.o \
	classinitialize.o \
	classcreate.o \
	classload.o \
	classlookup.o \
	classtable.o \
	classes.o \
	common_exceptions.o \
	cstates.o \
	float_fdlibm.o \
	globals.o \
	globalroots.o \
	jni_impl.o \
	jni_util.o \
	jvm.o \
	loadercache.o \
	localroots.o \
	opcodelen.o \
	opcodes.o \
	packages.o \
	preloader.o \
	reflect.o \
	stacks.o \
	stackwalk.o \
	stringintern.o \
	typeid.o \
	utils.o \
        porting_debug.o \
	verifyformat.o \
	weakrefs.o \
	\
	Object.o \
	Class.o \
	ClassLoader.o \
	ByteToCharISO8859_1.o \
        CharToByteISO8859_1.o \
	CVM.o \
	DatagramPacket.o \
	Finalizer.o \
	Float.o \
	GC.o \
	Double.o \
	Launcher.o \
	Package.o \
	Runtime.o \
	Shutdown.o \
	System.o \
	SecurityManager.o \
	TimeZone.o \
	Thread.o \
	Throwable.o \
	StrictMath.o \
	Array.o \
	Field.o \
	Method.o \
	Proxy.o \
	Constructor.o \
	FileDescriptor.o \
	FileInputStream.o \
	FileOutputStream.o \
	ObjectInputStream.o \
	ObjectStreamClass.o \
	ObjectOutputStream.o \
	InetAddress.o \
	AccessController.o \
	ResourceBundle.o \
	String.o \
	Inflater.o \
	Version.o \
	Vector.o \
	StringBuffer.o

ifeq ($(CDC_10),true)
CVM_SHAREOBJS_SPACE += \
	Character.o
else
CVM_SHAREOBJS_SPACE += \
	javaAssertions.o \
	Inet4Address.o \
	Inet6Address.o \
	net_util.o \
	CharacterData.o \
	CharacterDataLatin1.o
endif

#
# Interpreter loop objects. We may compile these slightly differently
# in order to prevent inlining of helper functions.
#
ifeq ($(CVM_INTERPRETER_LOOP), Split)
CVM_SHAREOBJS_LOOP += \
	executejava_split1.o \
	executejava_split2.o
endif
ifeq ($(CVM_INTERPRETER_LOOP), Aligned)
CVM_SHAREOBJS_LOOP += \
	executejava_aligned.o
endif
ifeq ($(CVM_INTERPRETER_LOOP), Standard)
CVM_SHAREOBJS_LOOP += \
	executejava_standard.o
endif

ifeq ($(CVM_JVMDI), true)
CVM_SHAREOBJS_SPACE += \
	jvmdi.o \
	jvmdi_jni.o \
	bag.o
endif

ifeq ($(CVM_JVMPI), true)
CVM_SHAREOBJS_SPACE += \
	jvmpi.o \
	CVMJVMPI.o
endif

ifeq ($(CVM_XRUN), true)
CVM_SHAREOBJS_SPACE += \
	xrun.o 
endif

ifeq ($(CVM_USE_MEM_MGR), true)
CVM_SHAREOBJS_SPACE += \
	mem_mgr.o
endif

# Include support for a specific profiler specified with
# CVM_JVMPI_PROFILER=<profiler>.  Note that this is not an
# officially supported feature of CDC.
ifeq ($(CVM_JVMPI), true)
ifneq ($(CVM_JVMPI_PROFILER),)
CVM_DEFINES         += -DCVM_JVMPI_PROFILER=$(CVM_JVMPI_PROFILER)
CVM_SHAREOBJS_SPACE += jvmpi_$(CVM_JVMPI_PROFILER).o 
endif
endif

CVM_SHAREOBJS_SPACE += $(ZLIBOBJS)

# Note: this test removed now that it is known to work. If you want to
# add it back in you must change the CVM_DYNAMIC_LINKING_CLEANUP_ACTION
# above.
# ifeq ($(CVM_DYNAMIC_LINKING), true)
# 	# Pick up the native code for the dynamic linking test
# 	CVM_SHAREOBJS_SPACE += DynLinkTest.o
# endif

#
# After including the profile defs files, our object lists are complete.
# Create the full path object list.
#
CVM_OBJECTS_SPEED0 = $(CVM_SHAREOBJS_SPEED) $(CVM_TARGETOBJS_SPEED)
CVM_OBJECTS_SPACE0 = $(CVM_SHAREOBJS_SPACE) $(CVM_TARGETOBJS_SPACE)
CVM_OBJECTS_LOOP0  = $(CVM_SHAREOBJS_LOOP)  $(CVM_TARGETOBJS_LOOP)
CVM_OBJECTS_OTHER0 = $(CVM_SHAREOBJS_OTHER) $(CVM_TARGETOBJS_OTHER)

CVM_OBJECTS_SPEED  = $(patsubst %.o,$(CVM_OBJDIR)/%.o,$(CVM_OBJECTS_SPEED0))
CVM_OBJECTS_SPACE  = $(patsubst %.o,$(CVM_OBJDIR)/%.o,$(CVM_OBJECTS_SPACE0))
CVM_OBJECTS_LOOP   = $(patsubst %.o,$(CVM_OBJDIR)/%.o,$(CVM_OBJECTS_LOOP0))
CVM_OBJECTS_OTHER  = $(patsubst %.o,$(CVM_OBJDIR)/%.o,$(CVM_OBJECTS_OTHER0))
CVM_OBJECTS  += $(CVM_OBJECTS_SPEED) $(CVM_OBJECTS_SPACE) \
	        $(CVM_OBJECTS_LOOP) $(CVM_OBJECTS_OTHER)

CVM_FDLIB_FILES = $(patsubst %.o,$(CVM_OBJDIR)/%.o,$(MATHOBJS))

##################################################################
# Miscellaneous options. Some platforms may want to override these.
##################################################################

# file separator character
CVM_FILESEP	= /

ifeq ($(CDC_10),true)
# Some platforms don't have a tzmappings file. They should override
# CVM_TZDATAFILE be empty in this case.
CVM_TZDIR      = $(CVM_TOP)/src/$(TARGET_OS)/lib
CVM_TZDATAFILE = $(CVM_LIBDIR)/tzmappings
endif

# mime content properties file
CVM_MIMEDIR	 = $(CVM_TOP)/src/$(TARGET_OS)/lib
CVM_MIMEDATAFILE = $(CVM_LIBDIR)/content-types.properties

# Name of the cvm binary
CVM    = cvm

##############################################################
# Locate the tools.
##############################################################


ifneq ($(CVM_USE_NATIVE_TOOLS), true)

# See if the user specified something other than the default
# for tool locations.
ifneq ($(CVM_TOOLS_DIR)$(CVM_TARGET_TOOLS_DIR)$(CVM_TARGET_TOOLS_PREFIX),)
    # don't count if imported from the shell
    ifneq ($(origin CVM_TOOLS_DIR),environment)
        ifneq ($(origin CVM_TARGET_TOOLS_DIR),environment)
            USER_SPECIFIED_LOCATION = true
        endif
    endif
endif
ifneq ($(origin CC),default)
    USER_SPECIFIED_LOCATION = true
endif

#
# Test if CC was explicitly set on the command line to a cc tool.
# If it was then set CVM_USE_NATIVE_TOOLS=true.
#
CC_SAVE := $(CC)
CC	:=
ifeq ($(CC),cc)
CVM_USE_NATIVE_TOOLS = true
endif
ifeq ($(patsubst %/cc,cc,$(CC)),cc)
CVM_USE_NATIVE_TOOLS = true
endif
CC	:= $(CC_SAVE)

# Locate the target gnu tools:
#
# The goal is to set CVM_TARGET_TOOLS_PREFIX to a value that when 
# prepended to "gcc", will specify the full path of gcc.
#
# Unless we were specifically told to use native tools, the gnu
# cross compiler is located by using CVM_TOOLS_DIR, CVM_HOST,
# TARGET_CPU_FAMILY, TARGET_DEVICE, and TARGET_OS.
# 
# CVM_TOOLS_DIR and CVM_HOST can be overriden, but TARGET_CPU_FAMILY,
# TARGET_CPU_FAMILY, and TARGET_OS cannot.
# 
# CVM_TARGET_TOOLS_PREFIX can be overriden, in which case none of
# the other variables normally used to compute it matter.
#
# You can also override CVM_TARGET_TOOLS_DIR, in which case CVM_TOOLS_DIR
# and CVM_HOST don't matter, but TARGET_CPU_FAMILY, TARGET_DEVICE,
# and TARGET_OS are still used.
#
CVM_TOOLS_DIR ?= /usr/tools
CVM_TARGET_TOOLS_DIR	?= $(CVM_TOOLS_DIR)/$(CVM_HOST)/gnu/bin
CVM_TARGET_TOOLS_PREFIX ?= $(CVM_TARGET_TOOLS_DIR)/$(TARGET_CPU_FAMILY)-$(TARGET_DEVICE)-$(TARGET_OS)-
# Unless the user told us where the tools are, make sure the cross compiler
# exists. Use native tools if not.
ifneq ($(USER_SPECIFIED_LOCATION), true)
GCC_PATH 		:= $(CVM_TARGET_TOOLS_PREFIX)gcc
ifneq ($(GCC_PATH), $(shell ls $(GCC_PATH) 2>&1))
CVM_TARGET_TOOLS_PREFIX :=
CVM_USE_NATIVE_TOOLS    := true
endif
endif

endif  # CVM_USE_NATIVE_TOOLS

#
# Locate the host tools:
#
# Find where the native gcc is located (HOST_CC).
# NOTE: If we decide to use $(CC), we must force
# evauation of HOST_CC to CC now because CC will be changed to be
# the target compiler a bit later.
#
ifeq ($(CVM_USE_NATIVE_TOOLS), true)
TEMP_HOST_CC		:= $(CC)
TEMP_HOST_CCC		:= $(CXX)
else
CVM_HOST_TOOLS_DIR 	:= $(CVM_TARGET_TOOLS_DIR)
CVM_HOST_TOOLS_PREFIX	:= $(CVM_HOST_TOOLS_DIR)/
TEMP_HOST_CC 		:= $(CVM_HOST_TOOLS_PREFIX)gcc
TEMP_HOST_CCC 		:= $(CVM_HOST_TOOLS_PREFIX)g++
ifneq ($(TEMP_HOST_CC), $(shell ls $(TEMP_HOST_CC) 2>&1))
CVM_HOST_TOOLS_PREFIX   :=
TEMP_HOST_CC		:= $(CC)
TEMP_HOST_CCC		:= $(CXX)
endif
endif

# Using TEMP variables allows HOST_CC and HOST_CC to be set in the
# GNUmakefile and not get overwritten by the above := assignments.
HOST_CC 	?= $(TEMP_HOST_CC)
HOST_CCC	?= $(TEMP_HOST_CCC)

#
# Locate the JDK tools:
#
# Look in JDK_HOME. If java doesn't exists there, then just assume
# the tools are on the path. The user can override either 
# JDK_HOME pr JDK_VERSION, or specify CVM_JAVA_TOOLS_PREFIX.
#
ifeq ($(CDC_10),true)
JDK_VERSION	?= jdk1.3.1
else
JDK_VERSION	?= jdk1.4.2
endif

ifneq ($(JDK_HOME),)
# If user set JDK_HOME, then always use it
CVM_JAVA_TOOLS_PREFIX	?= $(JDK_HOME)/bin/
else
# If user did not set JDK_HOME, then look in the default location. If
# nothing in the default location, then assume java tools are on the path
# or user set CVM_JAVA_TOOLS_PREFIX.
JDK_HOME	= $(CVM_TOOLS_DIR)/$(CVM_HOST)/java/$(JDK_VERSION)
ifneq ($(wildcard $(JDK_HOME)/bin/java*),)
CVM_JAVA_TOOLS_PREFIX	?= $(JDK_HOME)/bin/
endif
endif

CVM_JAVAC		?= $(CVM_JAVA_TOOLS_PREFIX)javac
CVM_JAVAH		?= $(CVM_JAVA_TOOLS_PREFIX)javah
CVM_JAVA		?= $(CVM_JAVA_TOOLS_PREFIX)java
CVM_JAVADOC		?= $(CVM_JAVA_TOOLS_PREFIX)javadoc
CVM_JAR			?= $(CVM_JAVA_TOOLS_PREFIX)jar

ifeq ($(HOST_DEVICE), win32)
JDK_PATH_SEP ?= ;
else
JDK_PATH_SEP ?= :
endif

JAVAC_OPTIONS +=  -J-Xms32m -J-Xmx128m -encoding iso8859-1
ifeq ($(CDC_10),true)
JAVAC_OPTIONS += -target 1.3
else
JAVAC_OPTIONS += -source 1.4 -target 1.4
endif
#
# Location of source for scripts and Java source files used during the build
#
CVM_MISC_TOOLS_SRCDIR    = $(CVM_SHAREROOT)/tools
CVM_MISC_TOOLS_CLASSPATH = $(CVM_BUILD_TOP)/classes.tools

#
# Specify all the host and target tools. 
#
ifneq ($(CVM_USE_NATIVE_TOOLS), true)
TARGET_CC	?= $(CVM_TARGET_TOOLS_PREFIX)gcc
TARGET_CCC	?= $(CVM_TARGET_TOOLS_PREFIX)g++
else
TARGET_CC	?= $(HOST_CC)
TARGET_CCC	?= $(HOST_CCC)
endif

CC		:= $(TARGET_CC)
CCC		:= $(TARGET_CCC)

TARGET_AS	?= $(CC)
AS		:= $(TARGET_AS)

TARGET_LD	?= $(CC)
LD		:= $(TARGET_LD)

TARGET_AR	?= $(CVM_TARGET_TOOLS_PREFIX)ar
AR		= $(TARGET_AR)

TARGET_RANLIB	?= $(CVM_TARGET_TOOLS_PREFIX)ranlib
RANLIB		= $(TARGET_RANLIB)

TARGET_AR_CREATE ?= $(AR) rc $(1)
AR_CREATE	 = $(TARGET_AR_CREATE)

TARGET_AR_UPDATE ?= $(RANLIB) $(1)
AR_UPDATE	 = $(TARGET_AR_UPDATE)

# NOTE: We already set HOST_CC above.
ifneq ($(origin LEX),default)
FLEX		= $(LEX)
else
FLEX		?= $(CVM_HOST_TOOLS_PREFIX)flex
endif
BISON		?= $(CVM_HOST_TOOLS_PREFIX)bison
ZIP             ?= zip

#######################################################################
# Build tool options:
#
# The defaults below works on most platforms that use unix based
# and used gcc. Overrides to these values should be done in
# platform specific makefiles, not on the command line.
########################################################################

#
# Compiler and linker flags
#

# for creating gnumake .d files
CCDEPEND   	= -MM

ASM_FLAGS	= -c -fno-common $(ASM_ARCH_FLAGS)
CCFLAGS     	= -c -fno-common -Wall -fno-strict-aliasing $(CC_ARCH_FLAGS)
CCCFLAGS 	= -fno-rtti
ifeq ($(CVM_OPTIMIZED), true)
CCFLAGS_SPEED	= $(CCFLAGS) -O4
CCFLAGS_SPACE	= $(CCFLAGS) -O2
else
CCFLAGS_SPEED	= $(CCFLAGS)
CCFLAGS_SPACE	= $(CCFLAGS)
endif
CCFLAGS_LOOP	= $(CCFLAGS_SPEED) $(CC_ARCH_FLAGS_LOOP)
CCFLAGS_FDLIB 	= $(CCFLAGS_SPEED) $(CC_ARCH_FLAGS_FDLIB)

ifeq ($(CVM_SYMBOLS), true)
CCFLAGS		+= -g
endif

ifeq ($(CVM_GPROF), true)
LINKFLAGS 	+= -pg
CCFLAGS   	+= -pg -g
endif

ifeq ($(CVM_GCOV), true)
CCFLAGS   	+= -fprofile-arcs -ftest-coverage
endif

CPPFLAGS 	+= $(CVM_DEFINES) $(CVM_INCLUDES)
CFLAGS_SPEED   	= $(CFLAGS) $(CCFLAGS_SPEED) $(CPPFLAGS)
CFLAGS_SPACE   	= $(CFLAGS) $(CCFLAGS_SPACE) $(CPPFLAGS)
CFLAGS_LOOP    	= $(CFLAGS) $(CCFLAGS_LOOP)  $(CPPFLAGS)
CFLAGS_FDLIB   	= $(CFLAGS) $(CCFLAGS_FDLIB) $(CPPFLAGS)
CFLAGS_JCS	= 

LINKFLAGS       += -g -Wl,-export-dynamic $(LINK_ARCH_FLAGS)
LINKLIBS     	+= -lpthread -ldl $(LINK_ARCH_LIBS)
LINKLIBS_JCS    +=

SO_CCFLAGS   	= $(CCFLAGS_SPEED)
SO_CFLAGS	= $(CFLAGS) $(SO_CCFLAGS) $(CPPFLAGS)
SO_LINKFLAGS 	= $(LINKFLAGS) -shared

#
# commands for running the tools
#
ASM_CMD 	= $(AT)$(AS) $(ASM_FLAGS) -D_ASM $(CPPFLAGS) -o $@ $<
CCC_CMD_SPEED	= $(AT)$(CCC) $(CFLAGS_SPEED) $(CCCFLAGS) -o $@ $<
CCC_CMD_SPACE	= $(AT)$(CCC) $(CFLAGS_SPACE) $(CCCFLAGS) -o $@ $<
CC_CMD_SPEED	= $(AT)$(CC) $(CFLAGS_SPEED) -o $@ $<
CC_CMD_SPACE	= $(AT)$(CC) $(CFLAGS_SPACE) -o $@ $<
CC_CMD_LOOP	= $(AT)$(CC) $(CFLAGS_LOOP) -o $@ $<
CC_CMD_FDLIB	= $(AT)$(CC) $(CFLAGS_FDLIB) -o $@ $<
LINK_CMD	= $(AT)$(LD)  $(LINKFLAGS) -o $@ $^ $(LINKLIBS)
SO_ASM_CMD 	= $(ASM_CMD)
SO_CC_CMD   	= $(AT)$(CC) $(SO_CFLAGS) -o $@ $<
SO_LINK_CMD 	= $(AT)$(LD) $(SO_LINKFLAGS) -o $@ $^
JAVAC_CMD	= $(CVM_JAVAC) $(JAVAC_OPTIONS)

#
# Standard classpath for libclasses compilation
#
JAVA_CLASSPATH += $(LIB_CLASSESDIR)

#
# Include target makfiles last.
#
# NOTE: the target defs.mk were switched to come after the
# shared defs.mk because the platform-specific object file list
# currently needs to look at a flag (dynamic linking) whose default is
# set in the global flags. We should consider doing the separation
# of the defs from the building of the object file lists.
#
-include ../$(TARGET_CPU_FAMILY)/defs.mk
-include ../$(TARGET_OS)/defs.mk
-include ../$(TARGET_OS)-$(TARGET_CPU_FAMILY)/defs.mk
-include ../$(TARGET_OS)-$(TARGET_CPU_FAMILY)-$(TARGET_DEVICE)/defs.mk

