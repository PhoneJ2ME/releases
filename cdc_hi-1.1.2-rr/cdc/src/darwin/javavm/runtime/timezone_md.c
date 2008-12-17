/*
 * @(#)timezone_md.c	1.17 06/10/10
 *
 * Copyright  1990-2008 Sun Microsystems, Inc. All Rights Reserved.  
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
 *
 */

#include <stdlib.h>
#include <stdio.h>
#include <strings.h>
#include <time.h>
#include <limits.h>
#include <errno.h>

//#ifdef __linux__
#include <string.h>
#include <dirent.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <unistd.h>
//#endif

#include "jvm.h"

#define SKIP_SPACE(p)	while (*p == ' ' || *p == '\t') p++;

#if !defined(__solaris__) || defined(__sparcv9)
#define fileopen	fopen
#define filegets	fgets
#define fileclose	fclose
#endif

//#ifdef __linux__
#if 1

static const char *sysconfig_clock_file = "/etc/sysconfig/clock";
static const char *zoneinfo_dir = "/usr/share/zoneinfo";
static const char *defailt_zoneinfo_file = "/etc/localtime";

/*
 * Returns a point to the zone ID portion of the given zoneinfo file
 * name.
 */
static char *
getZoneName(char *str)
{
    static const char *zidir = "zoneinfo/";

    char * pos = strstr((const char *)str, zidir);
    if (pos == NULL) {
	return NULL;
    }
    return pos + strlen(zidir);
}

/*
 * Returns a path name created from the given 'dir' and 'name' under
 * UNIX. This function allocates memory for the pathname calling
 * malloc().
 */
static char *
getPathName(const char *dir, const char *name) {
    char *path;

    path = (char *) malloc(strlen(dir) + strlen(name) + 2);
    if (path == NULL) {
	return NULL;
    }
    return strcat(strcat(strcpy(path, dir), "/"), name);
}

/*
 * Scans the specified directory and its subdirectories to find a
 * zoneinfo file which has the same content as /etc/localtime given in
 * 'buf'. Returns a zone ID if found, otherwise, NULL is returned.
 */
static char *
findZoneinfoFile(char *buf, size_t size, const char *dir)
{
    DIR *dirp = NULL;
    struct stat statbuf;
    struct dirent *dp;
    char *pathname = NULL;
    int fd = -1;
    char *dbuf = NULL;
    char *tz = NULL;

    dirp = opendir(dir);
    if (dirp == NULL) {
	return NULL;
    }

    while ((dp = readdir(dirp)) != NULL) {
	/*
	 * Skip '.' and '..' (and possibly other .* files)
	 */
	if (dp->d_name[0] == '.') {
	    continue;
	}

	pathname = getPathName(dir, dp->d_name); 
	if (pathname == NULL) {
	    break;
	}
	if (stat(pathname, &statbuf) == -1) {
	    break;
	}

	if (S_ISDIR(statbuf.st_mode)) {
	    tz = findZoneinfoFile(buf, size, pathname);
	    if (tz != NULL) {
		break;
	    }
	} else if (S_ISREG(statbuf.st_mode) && (size_t)statbuf.st_size == size) {
	    dbuf = (char *) malloc(size);
	    if (dbuf == NULL) {
		break;
	    }
	    if ((fd = open(pathname, O_RDONLY)) == -1) {
		fd = 0;
		break;
	    }
	    if (read(fd, dbuf, size) != (ssize_t) size) {
		break;
	    }
	    if (memcmp(buf, dbuf, size) == 0) {
		tz = getZoneName(pathname);
		if (tz != NULL) {
		    tz = strdup(tz);
		}
		break;
	    }
	    free((void *) dbuf);
	    dbuf = NULL;
	    (void) close(fd);
	    fd = 0;
	}
	free((void *) pathname);
	pathname = NULL;
    }

    if (dirp != NULL) {
	(void) closedir(dirp);
    }
    if (pathname != NULL) {
	free((void *) pathname);
    }
    if (fd != 0) {
	(void) close(fd);
    }
    if (dbuf != NULL) {
	free((void *) dbuf);
    }
    return tz;
}

/*
 * Performs libc implementation specific mapping and returns a zone ID
 * if found. Otherwise, NULL is returned.
 */
static char *
getPlatformTimeZoneID()
{
    struct stat statbuf;
    char *tz = NULL;
    FILE *fp;
    int fd;
    char *buf;
    size_t size;

    /*
     * First, try the ZONE entry in /etc/sysconfig/clock. However, the
     * ZONE entry is not set up after initial Red Hat Linux
     * installation. In case that /etc/localtime is set up without
     * using timeconfig, there might be inconsistency between
     * /etc/localtime and the ZONE entry. The inconsistency between
     * timeconfig and linuxconf is reported as a bug in the Red Hat
     * web page as of May 1, 2000.
     */
    if ((fp = fopen(sysconfig_clock_file, "r")) != NULL) {
	char line[256];

	while (fgets(line, sizeof(line), fp) != NULL) {
	    char *p = line;
	    char *s;

	    SKIP_SPACE(p);
	    if (*p != 'Z') {
		continue;
	    }
	    if (strncmp(p, "ZONE=\"", 6) == 0) {
		p += 6;
	    } else {
		/*
		 * In case we need to parse it token by token.
		 */
		if (strncmp(p, "ZONE", 4) != 0) {
		    continue;
		}
		p += 4;
		SKIP_SPACE(p);
		if (*p++ != '=') {
		    break;
		}
		SKIP_SPACE(p);
		if (*p++ != '"') {
		    break;
		}
	    }
	    for (s = p; *s && *s != '"'; s++)
		;
	    if (*s != '"') {
		/* this ZONE entry is broken. */
		break;
	    }
	    *s = '\0';
	    tz = strdup(p);
	    break;
	}
	(void) fclose(fp);
	if (tz != NULL) {
	    return tz;
	}
    }

    /*
     * Next, try /etc/localtime to find the zone ID.
     */
    if (lstat(defailt_zoneinfo_file, &statbuf) == -1) {
	return NULL;
    }

    /*
     * If it's a symlink, get the link name and its zone ID part. (The
     * older versions of timeconfig created a symlink as described in
     * the Red Hat man page. It was changed in 1999 to create a copy
     * of a zoneinfo file. It's no longer possible to get the zone ID
     * from /etc/localtime.)
     */
    if (S_ISLNK(statbuf.st_mode)) {
	char linkbuf[PATH_MAX+1];
	int len;

	if ((len = readlink(defailt_zoneinfo_file, linkbuf, sizeof(linkbuf)-1)) == -1) {
	    jio_fprintf(stderr, (const char *) "can't get a symlink of %s\n",
			defailt_zoneinfo_file);
	    return NULL;
	}
	linkbuf[len] = '\0';
	tz = getZoneName(linkbuf);
	if (tz != NULL) {
	    tz = strdup(tz);
	}
	return tz;
    }

    /*
     * If it's a regular file, we need to find out the same zoneinfo file
     * that has been copied as /etc/localtime.
     */
    size = (size_t) statbuf.st_size;
    buf = (char *) malloc(size);
    if (buf == NULL) {
	return NULL;
    }
    if ((fd = open(defailt_zoneinfo_file, O_RDONLY)) == -1) {
	free((void *) buf);
	return NULL;
    }

    if (read(fd, buf, size) != (ssize_t) size) {
	(void) close(fd);
	free((void *) buf);
	return NULL;
    }
    (void) close(fd);

    tz = findZoneinfoFile(buf, size, zoneinfo_dir);
    free((void *) buf);
    return tz;
}
#else
#ifdef __solaris__
#ifndef __sparcv9

/*
 * Those file* functions mimic the UNIX stream io functions. This is
 * because of the limitation of the number of open files on Solaris
 * (32-bit mode only) due to the System V ABI.
 */

#define BUFFER_SIZE	4096

struct iobuffer {
    int     magic;	/* -1 to distinguish from the real FILE */
    int     fd;		/* file discriptor */
    char    *buffer;	/* pointer to buffer */
    char    *ptr;	/* current read pointer */
    char    *endptr;	/* end pointer */
};

static int
fileclose(FILE *stream)
{
    struct iobuffer *iop = (struct iobuffer *) stream;

    if (iop->magic != -1) {
	return fclose(stream);
    }

    if (iop == NULL) {
	return 0;
    }
    close(iop->fd);
    free((void *)iop->buffer);
    free((void *)iop);
    return 0;
}

static FILE *
fileopen(const char *fname, const char *fmode)
{
    FILE *fp;
    int fd;
    struct iobuffer *iop;

    if ((fp = fopen(fname, fmode)) != NULL) {
	return fp;
    }

    /*
     * It assumes read open.
     */
    if ((fd = open(fname, O_RDONLY)) == -1) {
	return NULL;
    }

    /*
     * Allocate struct iobuffer and its buffer
     */
    iop = malloc(sizeof(struct iobuffer));
    if (iop == NULL) {
	(void) close(fd);
	errno = ENOMEM;
	return NULL;
    }
    iop->magic = -1;
    iop->fd = fd;
    iop->buffer = malloc(BUFFER_SIZE);
    if (iop->buffer == NULL) {
	(void) close(fd);
	free((void *) iop);
	errno = ENOMEM;
	return NULL;
    }
    iop->ptr = iop->buffer;
    iop->endptr = iop->buffer;
    return (FILE *)iop;
}

/*
 * This implementation assumes that n is large enough and the line
 * separator is '\n'.
 */
static char *
filegets(char *s, int n, FILE *stream)
{
    struct iobuffer *iop = (struct iobuffer *) stream;
    char *p;

    if (iop->magic != -1) {
	return fgets(s, n, stream);
    }

    p = s;
    for (;;) {
	char c;

	if (iop->ptr == iop->endptr) {
	    ssize_t len;
	    
	    if ((len = read(iop->fd, (void *)iop->buffer, BUFFER_SIZE)) == -1) {
		return NULL;
	    }
	    if (len == 0) {
		*p = 0;
		if (s == p) {
		    return NULL;
		}
		return s;
	    }
	    iop->ptr = iop->buffer;
	    iop->endptr = iop->buffer + len;
	}
	c = *iop->ptr++;
	*p++ = c;
	if ((p - s) == (n - 1)) {
	    *p = 0;
	    return s;
	}
	if (c == '\n') {
	    *p = 0;
	    return s;
	}
    }
    /*NOTREACHED*/
}
#endif /* not __sparcv9 */

static const char *sys_init_file = "/etc/default/init";

/*
 * Performs libc implementation dependent mapping. Returns a zone ID
 * if found. Otherwise, NULL is returned.  Solaris libc looks up
 * "/etc/default/init" to get a default TZ value if TZ is not defined
 * as an environment variable.
 */
static char *
getPlatformTimeZoneID()
{
    char *tz = NULL;
    FILE *fp;

    /*
     * Try the TZ entry in /etc/default/init.
     */
    if ((fp = fileopen(sys_init_file, "r")) != NULL) {
	char line[256];
	char quote = '\0';

	while (filegets(line, sizeof(line), fp) != NULL) {
	    char *p = line;
	    char *s;
	    char c;

	    /* quick check for comment lines */
	    if (*p == '#') {
		continue;
	    }
	    if (strncmp(p, "TZ=", 3) == 0) {
		p += 3;
		SKIP_SPACE(p);
		c = *p;
		if (c == '"' || c == '\'') {
		    quote = c;
		    p++;
		}

		/*
		 * PSARC/2001/383: quoted string support
		 */
		for (s = p; (c = *s) != '\0' && c != '\n'; s++) {
		    /* No '\\' is supported here. */
		    if (c == quote) {
			quote = '\0';
			break;
		    }
		    if (c == ' ' && quote == '\0') {
			break;
		    }
		}
		if (quote != '\0') {
		    jio_fprintf(stderr, "ZoneInfo: unterminated time zone name in /etc/TIMEZONE\n");
		}
		*s = '\0';
		tz = strdup(p);
		break;
	    }
	}
	(void) fileclose(fp);
    }
    return tz;
}

#endif
#endif

/*
 * findJavaTZ_md() maps platform time zone ID to Java time zone ID
 * using <java_home>/lib/tzmappings. If the TZ value is not found, it
 * trys some libc implementation dependent mappings. If it still
 * can't map to a Java time zone ID, it falls back to the GMT+/-hh:mm
 * form. `country', which can be null, is not used for UNIX platforms.
 */
/*ARGSUSED1*/
char *
CVMtimezoneFindJavaTZ(const char *java_home_dir, const char *country)
{
    char *tz;
    char *javatz = NULL;
    char *freetz = NULL;

    tz = getenv("TZ");

    if (tz == NULL || *tz == '\0') {
	tz = getPlatformTimeZoneID();
	freetz = tz;
    }

    if (tz != NULL) {
	if (*tz == ':') {
	    tz++;
	}
#ifdef __linix__
	/*
	 * Ignore "posix/" and "right/" prefix.
	 */
	if (strncmp(tz, "posix/", 6) == 0 || strncmp(tz, "right/", 6) == 0) {
	    tz += 6;
	}
#endif
	javatz = strdup(tz);
	if (freetz != NULL) {
	    free((void *) freetz);
	}
    }	
    return javatz;
}

/**
 * Returns a GMT-offset-based time zone ID. (e.g., "GMT-08:00")
 */
char *
CVMgetGMTOffsetID()
{
    time_t offset;
    char sign, buf[16];
    /* TODO: Darwin doesn't have a timezone global so we fake it. Not
     * sure if there is a better solution. */
    int timezone = 0;

    if (timezone == 0) {
	return strdup("GMT");
    }

    /* Note that the time offset direction is opposite. */
    if (timezone > 0) {
	offset = timezone;
	sign = '-';
    } else {
	offset = -timezone;
	sign = '+';
    }
    sprintf(buf, (const char *)"GMT%c%02d:%02d",
	    sign, (int)(offset/3600), (int)((offset%3600)/60));
    return strdup(buf);
}
