/*
 * @(#)hprof_md.c	1.12 06/10/10
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

#include <winsock.h>
#include <errno.h>
#include <string.h>
#include <tchar.h>
#include <time.h>

#include "jni.h"
#include "jlong.h"
#include "hprof.h"
#include "jvm.h"
#include "javavm/include/winntUtil.h"

void hprof_close(int fd)
{
    closesocket(fd);
}

int hprof_send(int s, const char *msg, int len, int flags)
{
    int res;
    do {
        res = send(s, msg, len, flags);
    } while ((res < 0) && (errno == EINTR));
    
    return res;
}

int hprof_write(FILE *filedes, const void *buf, size_t nbyte)
{
    int res;
    res = fwrite(buf, 1, nbyte, filedes);

    return res;
}


jint hprof_get_milliticks()
{
    return GetTickCount();
}

jlong hprof_get_timemillis()
{
    return JVM_CurrentTimeMillis(NULL, NULL);
}

void hprof_get_prelude_path(char *path)
{
  char hpath[MAX_PATH]; 
  TCHAR home_dir[MAX_PATH + 1000];
  TCHAR *p0;
  
  if (GetModuleFileName(NULL, home_dir, MAX_PATH)) {
    p0 = _tcsrchr(home_dir, '\\');
    if (p0 != NULL) {
      *p0 = _T('\0');
      p0 = _tcsrchr(home_dir, '\\');
      if (p0 != NULL) {
        *p0 = _T('\0');
        // convert wide character to sequence of multibyte characters. 
        copyToMCHAR(hpath, home_dir, MAX_PATH);
        sprintf(path, "%s\\lib\\jvm.hprof.txt", hpath);
        return;
      }
    }
  }     
  strcpy(path, ".");
}

/*
 * Return a socket descriptor connect()ed to a "hostname" that is
 * accept()ing heap profile data on "port." Return a value <= 0 if
 * such a connection can't be made.
 */
int hprof_real_connect(char *hostname, unsigned short port)
{
    struct hostent *hentry;
    struct sockaddr_in s;
    int fd;
    
    /* This check is redundant because port is of type unsigned short.
       Hence, it will never be outside of this range.
    if (port <= 0 || port > 65535) {
        fprintf(stderr, "HPROF ERROR: bad port number\n");
	return -1;
    }
    */
    if (hostname == NULL) {
        fprintf(stderr, "HPROF ERROR: hostname is NULL\n");
        return -1;
    }

    /* create a socket */
    fd = socket(AF_INET, SOCK_STREAM, 0);

    /* find remote host's addr from name */
    if ((hentry = gethostbyname(hostname)) == NULL) {
	return -1;
    }
    memset((char *)&s, 0, sizeof(s));
    /* set remote host's addr; its already in network byte order */
    memcpy(&s.sin_addr.s_addr, *(hentry->h_addr_list), 
	   sizeof(s.sin_addr.s_addr));
    /* set remote host's port */
    s.sin_port = htons(port);
    s.sin_family = AF_INET;

    /* now try connecting */
    if (-1 == connect(fd, (struct sockaddr*)&s, sizeof(s))) {
	return 0;
    } else {
	return fd;
    }
}
