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
#  Common makefile rules
#

# speed up makefile by not using implicit suffixes
.SUFFIXES:

all:: printconfig $(J2ME_CLASSLIB) tools

ifneq ($(CVM_TOOLS_BUILD), true)
tools :
	$(MAKE) CVM_TOOLS_BUILD=true tools
endif

#########################
# Print some important configuration information that isn't included
# when we dump build_defs.h later on.
#########################

CALL_TEST = $(1)
ifeq ($(call CALL_TEST,$$$$),$$)
# $(warning Weird make version $(MAKE_VERSION))
# Work around gnumake 3.79.1 weirdness
DDOLLAR := $$$$
else
DDOLLAR := $$
endif

# The cygwin "which" command doesn't work when a full path is given.
# The purpose of the following is to break the full path into the dir
# and tool name components, and then setup $PATH before calling "which"
TOOL_PATH0	= $(shell \
	set $(1);	\
	TOOL_WORD1="$$1";	\
	TOOL_CMD="`basename $${TOOL_WORD1}`"; \
	(if [ "$${TOOL_CMD}" = "$${TOOL_WORD1}" ]; then \
		$(call TOOL_WHICH,$(DDOLLAR){TOOL_CMD});	\
	else	\
	 	ls "$${TOOL_WORD1}" 2>&1 ;	\
	fi;) 2>&1 )
TOOL_PATH = $(subst //,/,$(call TOOL_PATH0,$($(1))))

CC_PATH:=$(call TOOL_PATH,CC)
CCC_PATH:=$(call TOOL_PATH,CCC)
AS_PATH:=$(call TOOL_PATH,AS)
LD_PATH:=$(call TOOL_PATH,LD)
HOST_CC_PATH:=$(call TOOL_PATH,HOST_CC)
HOST_CCC_PATH:=$(call TOOL_PATH,HOST_CCC)
CVM_JAVA_PATH:=$(call TOOL_PATH,CVM_JAVA)
CVM_JAVAC_PATH:=$(call TOOL_PATH,CVM_JAVAC)
CVM_JAVAH_PATH:=$(call TOOL_PATH,CVM_JAVAH)
CVM_JAR_PATH:=$(call TOOL_PATH,CVM_JAR)
ZIP_PATH:=$(call TOOL_PATH,ZIP)
TARGET_AR_PATH:=$(call TOOL_PATH,TARGET_AR)
TARGET_RANLIB_PATH:=$(call TOOL_PATH,TARGET_RANLIB)
ifeq ($(CVM_JIT), true)
FLEX_PATH:=$(call TOOL_PATH,FLEX)
BISON_PATH:=$(call TOOL_PATH,BISON)
endif

printconfig::
	@echo "CVM_HOST   = $(CVM_HOST)"
	@echo "CVM_TARGET = $(CVM_TARGET)"
	@echo "SHELL      = $(SHELL)"
	@echo "HOST_CC    = $(HOST_CC_PATH)"
	@echo "HOST_CCC   = $(HOST_CCC_PATH)"
	@echo "ZIP        = $(ZIP_PATH)"
ifeq ($(CVM_JIT), true)
	@echo "FLEX       = $(FLEX_PATH)"
	@echo "BISON      = $(BISON_PATH)"
endif
	@echo "CVM_JAVA   = $(CVM_JAVA_PATH)"
	@echo "CVM_JAVAC  = $(CVM_JAVAC_PATH)"
	@echo "CVM_JAVAH  = $(CVM_JAVAH_PATH)"
	@echo "CVM_JAR    = $(CVM_JAR_PATH)"
	@echo "TARGET_CC     = $(CC_PATH)"
	@echo "TARGET_CCC    = $(CCC_PATH)"
	@echo "TARGET_AS     = $(AS_PATH)"
	@echo "TARGET_LD     = $(LD_PATH)"
	@echo "TARGET_AR     = $(TARGET_AR_PATH)"
	@echo "TARGET_RANLIB = $(TARGET_RANLIB_PATH)"
	@echo "LINKFLAGS  = $(LINKFLAGS)"
	@echo "LINKLIBS   = $(LINKLIBS)"
	@echo "ASM_FLAGS  = $(ASM_FLAGS)"
	@echo "CCCFLAGS   = $(CCCFLAGS)"
	@echo "CCFLAGS_SPEED  = $(CCFLAGS_SPEED)"
	@echo "CCFLAGS_SPACE  = $(CCFLAGS_SPACE)"
	@echo "CCFLAGS_LOOP   = $(CCFLAGS_LOOP)"
	@echo "CCFLAGS_FDLIB  = $(CCFLAGS_FDLIB)"
	@echo "JAVAC_OPTIONS  = $(JAVAC_OPTIONS)"
	@echo "CVM_DEFINES    = $(CVM_DEFINES)"

#########################
# Compiling .java files.
##########################

#
# All directories that contain java source except for test classes.
# Allow profile classes to override all others.
#
JAVA_SRCDIRS += \
	$(OPT_PKGS_SRCPATH) $(PROFILE_SRCDIRS) \
	$(CVM_SHAREDCLASSES_SRCDIR) $(CVM_TARGETCLASSES_SRCDIR) \
	$(CVM_CLDCCLASSES_SRCDIR) \
	$(CVM_DERIVEDROOT)/classes

CVM_TESTCLASSES_SRCDIRS += $(CVM_TESTCLASSES_SRCDIR)

ifneq ($(CVM_TOOLS_BUILD), true)
# vpath for all directories that contain java source
vpath %.java	$(JAVA_SRCDIRS) \
		$(CVM_TESTCLASSES_SRCDIRS) \
		$(CVM_DEMOCLASSES_SRCDIRS)
endif

#
# Add optional package classes, if any, to CLASSLIB_CLASSES
#
CLASSLIB_CLASSES += $(OPT_PKGS_CLASSES)

#
# If CVM_PRELOAD_LIB is set, all CLASSLIB_CLASSES will be added
# into CVM_BUILDTIME_CLASSES. 
#
ifeq ($(CVM_PRELOAD_LIB), true)
CVM_BUILDTIME_CLASSES += $(CLASSLIB_CLASSES)
endif

#
# Build various lists of classes to be compiled
#

# Non-preloaded classes except for test classes
ifneq ($(CVM_PRELOAD_LIB), true)
$(LIB_CLASSESDIR)/%.class: %.java
	@echo $? >>$(CVM_BUILD_TOP)/.libclasses.list
	@echo $(subst /,.,$*) >>$(CVM_BUILD_TOP)/.javahclasses.list
	@touch $(CVM_BUILD_TOP)/.libclasses 
endif

# preloaded classes except for test classes
$(CVM_BUILDTIME_CLASSESDIR)/%.class: %.java
	@echo $? >>$(CVM_BUILD_TOP)/.btclasses.list
	@touch $(CVM_BUILD_TOP)/.btclasses

# Test classes
$(CVM_TEST_CLASSESDIR)/%.class: %.java
	@echo $? >>$(CVM_BUILD_TOP)/.testclasses.list
	@touch $(CVM_BUILD_TOP)/.testclasses

# demo classes
$(CVM_DEMO_CLASSESDIR)/%.class: %.java
	@echo $? >>$(CVM_BUILD_TOP)/.democlasses.list
	@touch $(CVM_BUILD_TOP)/.democlasses

#
# Convert the class lists to names of class files so they can be javac'd.
# First convert . to /, then prepend the classes directory and append .class
#

# CLASSLIB_CLASSES are compiled as BUILDTIME_CLASSES when romized
ifneq ($(CVM_PRELOAD_LIB), true)
CLASSLIB_CLASS0 = $(subst .,/,$(CLASSLIB_CLASSES))
CLASSLIB_CLASS_FILES = \
	$(patsubst %,$(LIB_CLASSESDIR)/%.class,$(CLASSLIB_CLASS0))
endif

BUILDTIME_CLASS0 = $(subst .,/,$(CVM_BUILDTIME_CLASSES))
BUILDTIME_CLASS_FILES = \
	$(patsubst %,$(CVM_BUILDTIME_CLASSESDIR)/%.class,$(BUILDTIME_CLASS0))

TEST_CLASS0 = $(subst .,/,$(CVM_TEST_CLASSES))
TEST_CLASS_FILES = \
	$(patsubst %,$(CVM_TEST_CLASSESDIR)/%.class,$(TEST_CLASS0))

DEMO_CLASS0 = $(subst .,/,$(CVM_DEMO_CLASSES))
DEMO_CLASS_FILES = $(patsubst %,$(CVM_DEMO_CLASSESDIR)/%.class,$(DEMO_CLASS0))

PS := "$(JDK_PATH_SEP)"

# Convert list of Java source directories to colon-separated paths
JAVACLASSES_SRCPATH = \
	$(subst $(space),$(PS),$(strip $(JAVA_SRCDIRS)))
TESTCLASSES_SRCPATH = \
	$(subst $(space),$(PS),$(strip $(CVM_TESTCLASSES_SRCDIRS)))
CVM_DEMOCLASSES_SRCPATH = \
	$(subst $(space),$(PS),$(strip $(CVM_DEMOCLASSES_SRCDIRS)))

# Convert list of classpath entries to colon-separated path
JAVACLASSES_CLASSPATH = $(subst $(space),$(PS),$(strip $(JAVA_CLASSPATH)))

# CR 6214008
# Convert list of OPT_PKGS classpath entries to colon-separated path
OPTPKGS_CLASSPATH = $(subst $(space),$(PS),$(strip $(OPT_PKGS_CLASSPATH)))

# Convert list of jar files to colon-separated path
TEST_JARFILES = $(subst $(space),$(PS),$(strip $(CVM_TEST_JARFILES)))

.delete.libclasses.list:
	@$(RM) $(CVM_BUILD_TOP)/.libclasses.list
	@$(RM) $(CVM_BUILD_TOP)/.javahclasses.list

.delete.btclasses.list:
	@$(RM) $(CVM_BUILD_TOP)/.btclasses.list

.delete.testclasses.list:
	@$(RM) $(CVM_BUILD_TOP)/.testclasses.list

.delete.democlasses.list:
	@$(RM) $(CVM_BUILD_TOP)/.democlasses.list

.report.libclasses.list:
	@echo "Checking for $(J2ME_PRODUCT_NAME) classes to compile ..."

.report.btclasses.list:
	@echo "Checking for build-time classes to compile ..."

.report.testclasses.list:
	@echo "Checking for test classes to compile ..."

.report.democlasses.list:
	@echo "Checking for demo classes to compile ..."

.compile.libclasses:
	$(AT)if [ -s $(CVM_BUILD_TOP)/.libclasses.list ] ; then		\
		echo "Compiling $(J2ME_PRODUCT_NAME) classes...";	\
		$(JAVAC_CMD)						\
			-d $(LIB_CLASSESDIR) 				\
			-bootclasspath $(CVM_BUILDTIME_CLASSESDIR) 	\
			-classpath $(JAVACLASSES_CLASSPATH)             \
			-sourcepath $(JAVACLASSES_SRCPATH)		\
			@$(CVM_BUILD_TOP)/.libclasses.list ;		\
	fi

.compile.btclasses:
	$(AT)if [ -s $(CVM_BUILD_TOP)/.btclasses.list ] ; then		\
		echo "Compiling build-time classes...";			\
		$(JAVAC_CMD)						\
			-d $(CVM_BUILDTIME_CLASSESDIR)			\
			-bootclasspath $(CVM_BUILDTIME_CLASSESDIR) 	\
			-classpath $(CVM_BUILDTIME_CLASSESDIR)$(PS)$(OPTPKGS_CLASSPATH)		\
			-sourcepath $(JAVACLASSES_SRCPATH)		\
			@$(CVM_BUILD_TOP)/.btclasses.list ;		\
	fi

.compile.testclasses:
	$(AT)if [ -s $(CVM_BUILD_TOP)/.testclasses.list ] ; then	\
		echo "Compiling test classes...";			\
		cp -f $(CVM_TESTCLASSES_SRCDIR)/TestSyncLocker.class	\
		      $(CVM_TEST_CLASSESDIR); \
		$(JAVAC_CMD)						\
			-d $(CVM_TEST_CLASSESDIR)			\
			-bootclasspath 					\
			   $(CVM_BUILDTIME_CLASSESDIR)$(PS)$(LIB_CLASSESDIR)\
			-classpath $(CVM_TEST_CLASSESDIR)$(PS)$(TEST_JARFILES) \
			-sourcepath $(TESTCLASSES_SRCPATH)		\
			@$(CVM_BUILD_TOP)/.testclasses.list ;		\
	fi

.compile.democlasses:
	$(AT)if [ -s $(CVM_BUILD_TOP)/.democlasses.list ] ; then	\
		echo "Compiling demo classes...";			\
		$(JAVAC_CMD)						\
			-d $(CVM_DEMO_CLASSESDIR)			\
			-bootclasspath 					\
			   $(CVM_BUILDTIME_CLASSESDIR)$(PS)$(LIB_CLASSESDIR)\
			-classpath $(CVM_DEMO_CLASSESDIR) 		\
			-sourcepath $(CVM_DEMOCLASSES_SRCPATH)		\
			@$(CVM_BUILD_TOP)/.democlasses.list ;	\
	fi

#
# The rules to compile our four kinds of classes: 
#     library (CDC or CDC+profile)
#     build-time classes (to be pre-loaded by JavaCodeCompact)
#     test classes
#     demo classes
# 
$(J2ME_CLASSLIB)classes:: .delete.libclasses.list .report.libclasses.list $(CLASSLIB_CLASS_FILES) $(CLASSLIB_JAR_FILES) .compile.libclasses

btclasses: .delete.btclasses.list .report.btclasses.list $(BUILDTIME_CLASS_FILES) .compile.btclasses

testclasses:: .delete.testclasses.list .report.testclasses.list $(TEST_CLASS_FILES) .compile.testclasses

democlasses:: .delete.democlasses.list .report.democlasses.list $(DEMO_CLASS_FILES) .compile.democlasses

#####################################
# include jcc and jcs makefiles
#####################################

include ../share/jcc.mk
ifeq ($(CVM_JIT),true)
include ../share/jcs.mk
endif

#####################################
# include all of the dependency files
#####################################

files := $(foreach file, $(wildcard $(CVM_OBJDIR)/*.d), $(file))
ifneq ($(strip $(files)),)
    include $(files)
endif

#####################################
# 1) Initialize build.
# 2) Compile the build-time classes and zip them
# 3) Compile the library classes and jar them
# 4) Compile the test classes and zip them
# 5) Create any needed jni headers, including those that JCC creates.
# 6) Create any dependencies the profile addes (like shared libraries)
# 7) Build the CVM
# 8) Create miscellneous files needed for the installation.
#####################################

$(J2ME_CLASSLIB):: initbuild
$(J2ME_CLASSLIB):: btclasses $(CVM_BUILDTIME_CLASSESZIP)
$(J2ME_CLASSLIB):: $(J2ME_CLASSLIB)classes $(LIB_CLASSESJAR)
$(J2ME_CLASSLIB):: testclasses $(CVM_TEST_CLASSESZIP)
$(J2ME_CLASSLIB):: democlasses $(CVM_DEMO_CLASSESJAR)
$(J2ME_CLASSLIB):: headers $(CVM_ROMJAVA_LIST)
$(J2ME_CLASSLIB):: $(CLASSLIB_DEPS)
$(J2ME_CLASSLIB):: $(CVM_BINDIR)/$(CVM)
ifeq ($(CDC_10),true)
$(J2ME_CLASSLIB):: $(CVM_TZDATAFILE)
endif
$(J2ME_CLASSLIB):: $(CVM_MIMEDATAFILE) $(CVM_PROPS_BUILD) $(CVM_POLICY_BUILD) $(CVM_MIDPFILTERCONFIG) $(CVM_MIDPCLASSLIST)

#####################################
# make empty.mk depend on CVM_SRCDIRS
# This will cause make to re-start if it needs to create any 
# dynamically created directories, so that vpath directives will work
#####################################

$(CVM_DERIVEDROOT)/empty.mk: $(CVM_SRCDIRS) $(JAVA_SRCDIRS)
	touch $(CVM_DERIVEDROOT)/empty.mk
include $(CVM_DERIVEDROOT)/empty.mk


#####################################
# See if any build flags have toggled since the last build. If so, then
# delete anything that might be dependent on the build flags. 
#####################################
CVM_FLAGS := $(sort $(CVM_FLAGS))
checkflags:: $(CVM_DERIVEDROOT) remove_build_flags $(CVM_FLAGS) $(CVM_BUILD_DEFS_H)

remove_build_flags:
	@rm -rf $(CVM_BUILD_FLAGS_FILE)

# Make sure all of the build flags files are up to date. If not
# then do the requested cleanup action.
#
# Also, add the option to $(CVM_BUILD_FLAGS_FILE) so we can restore
# it on the next build if CVM_REBUILD=true is specified.
$(CVM_FLAGS):: $(CVM_DERIVEDROOT)/flags
	@if [ ! -f $(CVM_DERIVEDROOT)/flags/$@.$($@) ]; then \
		echo "Flag $@ changed. Cleaning up."; \
		rm -f $(CVM_DERIVEDROOT)/flags/$@.*; \
		touch $(CVM_DERIVEDROOT)/flags/$@.$($@); \
		$($@_CLEANUP_ACTION); \
		rm -f $(CVM_BUILD_DEFS_H); \
	fi
	@echo $@=$($@) >> $(CVM_BUILD_FLAGS_FILE)

#
# Generate $(CVM_BUILD_DEFS_H) file
#
CVM_BUILD_DEFS_FLAGS += \
	$(foreach flag,$(strip $(CVM_FLAGS)), "$(flag)=$($(flag))\n")

CVM_BUILD_DEFS_VARS += \
	$(foreach flag,$(strip $(CVM_BUILD_DEF_VARS)), '$(flag)	$($(flag))')

$(CVM_BUILD_DEFS_H): $(wildcard ${CVM_BUILD_TOP}/../share/id*.mk)
	@echo ... generating $@
	@echo "/*** Definitions generated at build time ***/" > $@
	@echo "#ifndef _BUILD_DEFS_H" >> $@
	@echo "#define _BUILD_DEFS_H" >> $@
	@echo >> $@
	@printf "#define CVM_BUILD_OPTIONS %c\n"  '\\' >> $@
	@for s in  $(CVM_BUILD_DEFS_FLAGS) ; do \
		printf "\t\"%s\" %c\n" "$$s" '\\' ; \
	done >> $@ 
	@echo >> $@
	@for s in  $(CVM_BUILD_DEFS_VARS) ; do \
		printf "#define %s\n" "$$s" ; \
	done >> $@
	@echo >> $@
	@echo "#endif /* _BUILD_DEFS_H */" >> $@
	@echo
	@cat $(CVM_BUILD_DEFS_H)
	@echo

#####################################
# system_properties.c
#####################################

#
# Generate the system_properties.c file, which is included by System.c
# when initialising system properties. This allows profiles to add their
# own system properties by adding them to the SYSTEM_PROPERTIES variable.
# This sets the properties defined by the SYSTEM_PROPERTIES variable.
#
# Also, for each library listed in BUILTIN_LIBS, a property called
# java.library.builtin.<libname> is set to true so that when a 
# System.loadLibray is performed for one of these libraries, it won't
# try to find and load the library.
#
SYSTEM_PROPERTIES_C = $(CVM_DERIVEDROOT)/javavm/runtime/system_properties.c
.generate.system_properties.c:
	$(AT) echo "/* This file is included by System.c to setup system properties. */" > $(CVM_BUILD_TOP)/.system_properties.c
	$(AT) echo "/* AUTO-GENERATED - DO NOT EDIT */" >> $(CVM_BUILD_TOP)/.system_properties.c
	$(AT) echo "" >> $(CVM_BUILD_TOP)/.system_properties.c
	$(AT) if [ "$(SYSTEM_PROPERTIES)" != "" ] ; then \
		for s in "$(SYSTEM_PROPERTIES)" ; do \
			printf "%s\n" $$s | sed -e 's/\(.*\)=\(.*\)/PUTPROP (props, "\1", "\2");/' ; \
		done ; \
	 fi >> $(CVM_BUILD_TOP)/.system_properties.c
	$(AT) echo "" >> $(CVM_BUILD_TOP)/.system_properties.c
	$(AT) if [ "$(BUILTIN_LIBS)" != "" ] ; then \
		echo "/* Defined properties for builtin libraries. */" ; \
		for s in "$(BUILTIN_LIBS)" ; do \
			printf "PUTPROP(props, \"java.library.builtin.%s\", \"true\");\n" $$s; \
		done ; \
	 fi >> $(CVM_BUILD_TOP)/.system_properties.c
	$(AT) if ! cmp -s $(CVM_BUILD_TOP)/.system_properties.c $(SYSTEM_PROPERTIES_C); then \
		echo ... $(SYSTEM_PROPERTIES_C); \
		cp -f $(CVM_BUILD_TOP)/.system_properties.c $(SYSTEM_PROPERTIES_C); \
	fi

#####################################
# create directories
#####################################

$(CVM_BUILDDIRS):
	@echo ... mkdir $@
	@if [ ! -d $@ ]; then mkdir -p $@; fi

#####################################
# Compile C, C++, and assembler source
#####################################

#
# vpath
#
ifneq ($(CVM_TOOLS_BUILD), true)
vpath %.c	$(CVM_SRCDIRS)
vpath %.cc 	$(CVM_SRCDIRS)
vpath %.cpp 	$(CVM_SRCDIRS)
vpath %.S 	$(CVM_SRCDIRS)
endif

#
# Make sure opcodes.h gets generated before executejava.o gets built
#
$(CVM_OBJDIR)/executejava.o: $(CVM_DERIVEDROOT)/javavm/include/opcodes.h

# command to use to generate dependency makefiles if requested
ifeq ($(GENERATEMAKEFILES), true)
GENERATEMAKEFILES_CMD = \
	@$(CC) $(CCDEPEND) $(CPPFLAGS) $< 2> /dev/null | \
	    sed 's!$*\.o!$(dir $@)&!g' > $(@:.o=.d)
endif

# command to use to generate stack map analysis files if requested
ifeq ($(CVM_CSTACKANALYSIS), true)
CSTACKANALYSIS_CMD = \
	$(AT)$(CC) -S $(CCFLAGS) $(CPPFLAGS) -o $(@:.o=.asm) $<
endif

#
# rules for compiling
#
COMPILE_FLAVOR = SPEED

$(CVM_OBJECTS_SPEED): COMPILE_FLAVOR = SPEED
$(CVM_OBJECTS_SPACE): COMPILE_FLAVOR = SPACE
$(CVM_OBJECTS_LOOP): COMPILE_FLAVOR = LOOP

ifneq ($(CVM_PROVIDE_TARGET_RULES), true)
$(CVM_OBJDIR)/%.o: %.cc
	@echo "c++ $@"
	$(CCC_CMD_$(COMPILE_FLAVOR))
	$(GENERATEMAKEFILES_CMD)
	$(CSTACKANALYSIS_CMD)

$(CVM_OBJDIR)/%.o: %.cpp
	@echo "c++ $@"
	$(CCC_CMD_$(COMPILE_FLAVOR))
	$(GENERATEMAKEFILES_CMD)
	$(CSTACKANALYSIS_CMD)

$(CVM_OBJDIR)/%.o: %.c
	@echo "cc  $@"
	$(CC_CMD_$(COMPILE_FLAVOR))
	$(GENERATEMAKEFILES_CMD)
	$(CSTACKANALYSIS_CMD)


$(CVM_OBJDIR)/%.o: %.S
	@echo "as  $@"
	$(ASM_CMD)
ifeq ($(GENERATEMAKEFILES), true)
	@$(CC) $(CCDEPEND) $(CPPFLAGS) $< 2> /dev/null | \
	    sed 's!$*\.o!$(dir $@)&!g' > $(@:.o=.d)
endif
ifeq ($(CVM_CSTACKANALYSIS), true)
	cp $< $(@:.o=.asm)
endif
endif

#
# floating point code often needs to be compiled with special flags
# because of bugs in gcc. This is the case with on x86 where we
# need to compile with -ffloat-store to avoid some gcc bugs.
#
CVM_FDLIBM_SRCDIR = $(CVM_SHAREROOT)/native/java/lang/fdlibm/src
CVM_FDLIB	  = $(CVM_OBJDIR)/fdlibm.a

ifneq ($(CVM_PROVIDE_TARGET_RULES), true)
$(CVM_FDLIB_FILES): $(CVM_OBJDIR)/%.o: $(CVM_FDLIBM_SRCDIR)/%.c
	@echo "cc  $@"
	$(CC_CMD_FDLIB)
ifeq ($(GENERATEMAKEFILES), true)
	@$(CC) $(CCDEPEND) $(CPPFLAGS) $< 2> /dev/null | \
		sed 's!$*\.o!$(dir $@)&!g' > $(@:.o=.d)
endif

$(CVM_FDLIB): $(CVM_FDLIB_FILES)
	@echo lib $@
	$(AT)$(call AR_CREATE,$@) $^
	$(AT)$(call AR_UPDATE,$@)
endif

#####################################
# Build CVM and everything it needs
#####################################

#
# Initialize the environment for the build process: 
#
initbuild: checkflags $(CVM_BUILDDIRS)

# make sure we build the headers before the objects
$(CVM_BINDIR)/$(CVM) :: $(CVM_ROMJAVA_LIST) # jcc creates some of the headers
ifneq ($(CVM_PRELOAD_LIB), true)
$(CVM_BINDIR)/$(CVM) :: headers
endif

$(CVM_BINDIR)/$(CVM) :: .generate.system_properties.c

ifneq ($(CVM_PROVIDE_TARGET_RULES), true)
$(CVM_BINDIR)/$(CVM) :: $(CVM_OBJECTS) $(CVM_OBJDIR)/$(CVM_ROMJAVA_O) $(CVM_FDLIB)
	@echo "Linking $@"
	$(LINK_CMD)
	@echo "Done Linking $@"
endif

#####################################
# cleanup
#####################################

# Rerun make so tool makefiles (jcov, hprof, and jdwp) are included
clean::
	$(MAKE) CVM_TOOLS_BUILD=true tool-clean

clean::
	rm -rf $(INSTALLDIR)
	rm -rf $(CVM_BUILD_TOP)/.libclasses
	rm -rf $(CVM_BUILD_TOP)/.btclasses 
	rm -rf $(CVM_BUILD_TOP)/.testclasses
	rm -rf $(CVM_BUILD_TOP)/.democlasses
	rm -rf $(CVM_BUILD_TOP)/.*.list
	rm -rf $(CVM_BUILD_TOP)/.system_properties.c
	rm -rf .DefaultLocaleList.java
	rm -rf $(CVM_BUILD_TOP)/.previous.build.flags
	rm -rf $(BUILDFLAGS_JAVA)
	rm -rf $(CVM_BUILDTIME_CLASSESDIR) \
	       $(CVM_TEST_CLASSESDIR) $(CVM_DEMO_CLASSESDIR) *_classes
	rm -rf $(CVM_BUILDTIME_CLASSESZIP) \
	       $(CVM_TEST_CLASSESZIP) $(CVM_DEMO_CLASSESJAR)
	rm -rf $(CVM_LIBDIR)
	rm -rf $(CVM_BUILDDIRS) $(CVM_BINDIR)/$(CVM)
	rm -rf $(CVM_JCC_CLASSPATH)
	rm -rf $(CVM_DERIVEDROOT)
	rm -rf $(CVM_PROPS_BUILD) $(CVM_POLICY_BUILD)
	rm -rf $(CVM_JCS_BUILDDIR)

#####################################
# zip or jar class files
#####################################

CVM_CLASSES_TMP = $(CVM_BUILD_SUBDIR_NAME)/.classes.tmp

$(CVM_BUILDTIME_CLASSESZIP): $(CVM_BUILD_TOP)/.btclasses
	@echo ... $@
	$(AT)(cd $(CVM_BUILDTIME_CLASSESDIR); $(ZIP) -r -0 -q - * ) \
		> $(CVM_CLASSES_TMP)
	$(AT)mv -f $(CVM_CLASSES_TMP) $@

$(CVM_TEST_CLASSESZIP): $(CVM_BUILD_TOP)/.testclasses
	@echo ... $@
	$(AT)(cd $(CVM_TEST_CLASSESDIR); $(ZIP) -r -0 -q - * ) \
		> $(CVM_CLASSES_TMP)
	$(AT)mv -f $(CVM_CLASSES_TMP) $@

$(CVM_DEMO_CLASSESJAR): $(CVM_BUILD_TOP)/.democlasses
	@echo ... $@
	$(AT)for dir in $(CVM_DEMOCLASSES_SRCDIRS); do files=`(cd $$dir; find . -name .svn -prune -o -type f -print)`; (cd $$dir; tar -cf - $$files) | (cd $(CVM_DEMO_CLASSESDIR); tar -xf - ); done 
	$(AT)(cd $(CVM_DEMO_CLASSESDIR); $(ZIP) -r -q - *) > $(CVM_CLASSES_TMP)
	$(AT)mv -f $(CVM_CLASSES_TMP) $@


#
# Create the profile jar file if there is anything in the profile
# classes directory.
#
ifneq ($(CVM_BUILD_LIB_CLASSESJAR), true)
$(LIB_CLASSESJAR):
else
$(LIB_CLASSESJAR): $(CVM_BUILD_TOP)/.libclasses
	@echo ... $@	
	$(AT)(cd $(LIB_CLASSESDIR); \
	      $(CVM_JAR) cf $(CVM_BUILD_SUBDIR_UP)../$@ *)
endif

#####################################
# Run JAVAH.
#####################################

#
# If this is a not fully romized build, then we need to use javah to create
# the jni headers for the runtime classes since JCC won't have seen these.
#
# NOTE: we could probably fix JCC so that we can generate
# offset and CNI header files separately too.
#
ifeq ($(CVM_PRELOAD_LIB), true)
headers:
else
headers: $(CVM_DERIVEDROOT)/jni/.time.stamp
$(CVM_DERIVEDROOT)/jni/.time.stamp : $(LIB_CLASSESJAR)
	$(AT)if [ -s $(CVM_BUILD_TOP)/.javahclasses.list ] ; then	\
		echo ... generating jni class headers ;		\
		$(CVM_JAVAH) -jni					\
			-d $(CVM_DERIVEDROOT)/jni			\
			-classpath $(LIB_CLASSESJAR)			\
			-bootclasspath $(CVM_BUILDTIME_CLASSESZIP)	\
			@$(CVM_BUILD_TOP)/.javahclasses.list ;		\
	fi
	@touch $@
endif

#####################################
# Copy tzmappings file to the lib directory. Not all platforms have this
# file, so only do this if CVM_TZDATAFILE is defined.
#####################################

ifeq ($(CDC_10),true)
ifneq ($(CVM_TZDATAFILE), )
$(CVM_TZDATAFILE): $(CVM_TZDIR)/tzmappings
	@echo "Updating tzmappings...";
	@cp -f $< $@;
	@echo "<<<Finished copying $@";
endif
endif

#####################################
# Copy content-types.properties to the lib directory
#####################################

$(CVM_MIMEDATAFILE): $(CVM_MIMEDIR)/content-types.properties
	@echo "Updating default MIME table...";
	@cp -f $< $@;
	@echo "<<<Finished copying $@";

#####################################
# Copy midp member configuration files to the lib directory. 
# Only do this for dual stack support
#####################################

ifneq ($(CVM_MIDPFILTERCONFIG), )
$(CVM_MIDPFILTERCONFIG): $(CVM_MIDPDIR)/MIDPFilterConfig.txt
	@echo "Updating MIDPFilterConfig...";
	@cp -f $< $@;
	@echo "<<<Finished copying $@";

$(CVM_MIDPCLASSLIST): $(CVM_MIDPDIR)/MIDPPermittedClasses.txt
	@echo "Updating MIDPPermittedClasses...";
	@cp -f $< $@;
	@echo "<<<Finished copying $@";
endif

################################################
# Rules for building documentation
################################################

update_docs:: lib-src
	@echo ">>>Updating "$@" ..." ;
	(cd $(INSTALLDIR); $(ZIP) -r -u -q install/$(J2ME_CLASSLIB)-src.zip doc -x $(EXCLUDE_PATTERNS) ) ;
	@echo "<<<Finished "$@" ..." ;

$(INSTALLDIR)/javadoc:
	@echo ... mkdir $@
	@mkdir -p $@

javadoc.zip: javadoc-$(J2ME_CLASSLIB) $(OPT_PKGS_JAVADOC_RULES)
	@echo ">>>Making "$@" ..." ;
	(cd $(INSTALLDIR); \
	$(ZIP) -r -q - javadoc) \
	> $(INSTALLDIR)/javadoc.zip;
	@echo "<<<Finished "$@" ..." ;

################################################
# Install security-related files
################################################

ifeq ($(DO_SECURITY_PROVIDER_FILTERING),false)
$(CVM_PROPS_BUILD): $(CVM_PROPS_SRC)
	@echo "Updating java.security file...";
	@cp -f $< $@;
	@echo "<<<Finished copying $@";
else
$(CVM_PROPS_BUILD): $(CVM_PROPS_SRC)
	@echo "Updating java.security file...";
	$(AT)provs="$(SECURITY_PROVIDERS)" ; \
	 if [ -z "$$provs" ]; then \
	     provs="sun.security.provider.Sun" ; \
	 fi ; \
         awk -F= 'BEGIN { count = 1; } \
	     NR == 1 { \
		 includeCount = split( includedProviders, included, " " ); \
	     } \
	     /^security.provider.[0-9]*/ { \
		for ( i = 1 ; i <= includeCount ; i++ ) { \
		    if ( included[i] == $$2 ) { \
	                printf "security.provider.%d=%s\n", count, $$2; \
	                count++; \
			next; \
		    } \
                }\
	        next; \
             } \
	     { print; }' includedProviders="$$provs" $< > $@ ;
	@echo "<<<Finished updating $@";
endif

$(CVM_POLICY_BUILD): $(CVM_POLICY_SRC)
	@echo "Updating java.policy file...";
	@cp -f $< $@;
	@echo "<<<Finished copying $@";

################################################
# Rule for building binary bundle
################################################

.PHONY : bin
bin: all
	@echo ">>>Making binary bundle ..."
	@mkdir -p $(INSTALLDIR)
	@echo "$(BINARY_BUNDLE)"
	tar -cvzf $(BINARY_BUNDLE) \
		bin/* lib/* democlasses.jar testclasses.zip btclasses.zip

################################################
# Include target makfiles last
################################################

-include ../$(TARGET_OS)/rules.mk
-include ../$(TARGET_CPU_FAMILY)/rules.mk
-include ../$(TARGET_OS)-$(TARGET_CPU_FAMILY)/rules.mk
-include ../$(TARGET_OS)-$(TARGET_CPU_FAMILY)-$(TARGET_DEVICE)/rules.mk


######################################
# Handle build flag violations here
######################################

ifeq ($(MAKELEVEL), 0)

ifeq ($(CVM_JIT),true)
ifeq ($(CVM_JVMDI),true)
$(error JVMDI is not supported in JIT builds. Use CVM_JIT=false.)
endif
ifeq ($(CVM_JVMPI),true)
$(warning JVMPI is not fully supported in JIT builds. Programs may not behave properly.)
endif
endif

endif

################################################
# sun.misc.DefaultLocaleList.java
################################################

# 
# Generate sun.misc.DefaultLoacleList.java which contains a list 
# of default locales in the system. The list is generated by parsing
# CVM_BUILDTIME_CLASSES list, which contains the romized classes.
#

ifeq ($(CDC_10),true)
LOCALE_ELEMENTS_PREFIX = java.text.resources.LocaleElements_
else
LOCALE_ELEMENTS_PREFIX = sun.text.resources.LocaleElements_
endif
LOCALE_ELEMENTS_LIST = $(patsubst $(LOCALE_ELEMENTS_PREFIX)%,%,$(filter $(LOCALE_ELEMENTS_PREFIX)%,$(CVM_BUILDTIME_CLASSES)))

$(DEFAULTLOCALELIST_JAVA):
	@echo ... generating sun.misc.DefaultLocaleList.java
	$(AT) echo "/* This file is used by LocaleData.java */" > .DefaultLocaleList.java
	$(AT) echo "/* AUTO-GENERATED - DO NOT EDIT */" >> .DefaultLocaleList.java
	$(AT) echo "" >> .DefaultLocaleList.java
	$(AT) echo "package sun.misc; " >> .DefaultLocaleList.java
	$(AT) echo "" >> .DefaultLocaleList.java
	$(AT) echo "public class DefaultLocaleList { " >> .DefaultLocaleList.java
	$(AT) echo "   public final static String list[] = { " >> .DefaultLocaleList.java
	$(AT) if [ "$(LOCALE_ELEMENTS_LIST)" != "" ] ; then \
		for s in "$(LOCALE_ELEMENTS_LIST)" ; do \
			printf "\t\"%s\", " $$s; \
		done ; \
	 fi >> .DefaultLocaleList.java
	$(AT) printf "\t};" >> .DefaultLocaleList.java
	$(AT) echo "}" >> .DefaultLocaleList.java
	$(AT) echo "" >> .DefaultLocaleList.java
	$(AT) if ! cmp -s .DefaultLocaleList.java $(DEFAULTLOCALELIST_JAVA); then \
		echo ... $(DEFAULTLOCALELIST_JAVA); \
		cp -f .DefaultLocaleList.java $(DEFAULTLOCALELIST_JAVA); \
	fi
	$(AT) rm .DefaultLocaleList.java

#####################################
# BuildFlags.java
#####################################

ifneq ($(CDC_10),true)

#
# Generate the BuildFlags.java file, which consists of one field,
# final static boolean qAssertEnabled.
#
# The value of this field corresponds to CVM_DEBUG_ASSERTS.
#

BUILDFLAGS_JAVA = $(CVM_DERIVEDROOT)/classes/sun/misc/BuildFlags.java
$(BUILDFLAGS_JAVA): 
	@echo ... generating BuildFlags.java
	$(AT) echo "/* This file contains information determined at a build time*/" > .BuildFlags.java
	$(AT) echo "/* AUTO-GENERATED - DO NOT EDIT */" >> .BuildFlags.java
	$(AT) echo "" >> .BuildFlags.java
	$(AT) echo "package sun.misc; " >> .BuildFlags.java
	$(AT) echo "" >> .BuildFlags.java
	$(AT) echo "public class BuildFlags { " >> .BuildFlags.java
	$(AT) echo "   public final static boolean qAssertsEnabled = $(CVM_DEBUG_ASSERTS); " >> .BuildFlags.java
	$(AT) echo "}" >> .BuildFlags.java
	$(AT) echo "" >> .BuildFlags.java
	$(AT) if ! cmp -s .BuildFlags.java $(BUILDFLAGS_JAVA); then \
		echo ... $(BUILDFLAGS_JAVA); \
		cp -f .BuildFlags.java $(BUILDFLAGS_JAVA); \
	fi
	$(AT) rm .BuildFlags.java

endif
