/*
 *
 * Copyright  1990-2006 Sun Microsystems, Inc. All Rights Reserved.
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
 */

#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>

#include <midpStorage.h>

static char dirBuffer[MAX_FILENAME_LENGTH+1];

static char* getCharFileSeparator() {
    /*
     * This function is called before the debug heap is initialized.
     * so we can't use midpMalloc.
     */
    static char fsep[2];

    fsep[0] = (char)storageGetFileSeparator();
    fsep[1] = 0;

    return fsep;
}

/**
 * Generates a correct MIDP home directory based on several rules. If
 * the <tt>MIDP_HOME</tt> environment variable is set, its value is used
 * unmodified. Otherwise, this function will search for the <tt>appdb</tt>
 * directory in the following order:
 * <ul>
 * <li>current directory (if the MIDP executable is in the <tt>PATH</tt>
 *     environment variable and the current directory is the right place)
 * <li>the parent directory of the midp executable
 * <li>the grandparent directory of the midp executable
 * </ul>
 * <p>
 * If <tt>cmd</tt> does not contain a directory (i.e. just the text
 * <tt>midp</tt>), the search starts from the current directory. Otherwise,
 * the search starts from the directory specified in <tt>cmd</tt> (i.e.
 * start in the directory <tt>bin</tt> if <tt>cmd</tt> is <tt>bin/midp</tt>).
 * <p>
 * <b>NOTE:</b> This is only applicable for development platforms.
 *
 * @param cmd A 'C' string containing the command used to start MIDP.
 * @return A 'C' string the found MIDP home directory, otherwise
 *         <tt>NULL</tt>, this will be a static buffer, so that it safe
 *       to call this function before midpInitialize, don't free it
 */
char* midpFixMidpHome(char *cmd) {
    int   i;
    char* filesep = NULL;
    char* lastsep;
    char* midp_home;
    FILE *fp;
    int j;

    /*
     * If MIDP_HOME is set, just use it. Does not check if MIDP_HOME is
     * pointing to a directory contain "appdb".
     */
    midp_home = getenv("MIDP_HOME");
    if (midp_home != NULL) {
        return midp_home;
    }

    filesep = getCharFileSeparator();
    
    dirBuffer[sizeof (dirBuffer) - 1] = 0;
    strncpy(dirBuffer, cmd, sizeof (dirBuffer) - 1);

    for (j=0; j<3; j++) {
        /* Look for the last slash in the pathanme. */
        lastsep = strrchr(dirBuffer, (int) *filesep);
        if (lastsep != 0) {
            *(lastsep + 1) = '\0';
        } else {
            /* no file separator */
            strcpy(dirBuffer, ".");
            strcat(dirBuffer, filesep);
        }
            
        strcat(dirBuffer, "appdb");
        strcat(dirBuffer, filesep);
        strcat(dirBuffer, "_main.ks");

        fp = fopen(dirBuffer, "r");

        /* strip off "appdb/_main.ks" from the path */
        *(strrchr(dirBuffer, (int) *filesep)) = '\0';
        *(strrchr(dirBuffer, (int) *filesep)) = '\0';
            
        if (fp != NULL) {
            fclose(fp);
            return dirBuffer;
        }
    }

    if (*filesep != '/' && *filesep != '\\') {
      fprintf(stderr, "\n    Warning: cannot connect to ADS profiler.\n"
              "    Do you have Mapfile.so in the current directory?\n"
              "    See internal_doc/use/armsd-howto.txt for more info\n\n");
    } else {
      fprintf(stderr, "Warning: cannot find appdb subdirectory.\n"
              "Please specify MIDP_HOME environment variable such "
              "that $MIDP_HOME%clib contains the proper configuration "
              "files.\n", *filesep);
    }
    return NULL;
}

/**
 * Removes the flag and value for a given option from command line argument
 * array.
 *
 * @param pszFlag flag for the option
 * @param apszArgs array of arguments
 * @param pArgc pointer to the count of arguments in the array, it
 * will updated with the length
 *
 * @return value of the option or NULL if it does not exist
 */
char* midpRemoveCommandOption(char* pszFlag, char* apszArgs[], int* pArgc) {
    int i;
    int len = *pArgc;
    char* result = NULL;

    for (i = 0; i < (len - 1); i++) {
        if (strcmp(pszFlag, apszArgs[i]) == 0) {
            result = apszArgs[i + 1];
	    break;
        }
    }

    if (result == NULL) {
        return NULL;
    }

    /* Remove the flag and value of the option. */
    for (; i < (len - 2); i++) {
        apszArgs[i] = apszArgs[i + 2];
    }

    *pArgc = len - 2;
    return result;
}

/**
 * Removes a given option flag from command line argument
 *
 * @param pszFlag flag for the option
 * @param apszArgs array of arguments
 * @param pArgc pointer to the count of arguments in the array, it
 * will updated with the length
 *
 * @return value of the flag or NULL if it does not exist
 */
char* midpRemoveOptionFlag(char* pszFlag, char* apszArgs[], int* pArgc) {
    int i;
    int len = *pArgc;
    char* result = NULL;

    for (i = 0; i < len; i++) {
        if (strcmp(pszFlag, apszArgs[i]) == 0) {
            result = apszArgs[i];
	    break;
        }
    }

    if (result == NULL) {
        return NULL;
    }

    /* Remove the flag */
    for (; i < (len - 1); i++) {
        apszArgs[i] = apszArgs[i + 1];
    }

    *pArgc = len - 1;
    return result;
}
