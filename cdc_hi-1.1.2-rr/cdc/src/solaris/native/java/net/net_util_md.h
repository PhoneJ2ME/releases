/*
 * @(#)net_util_md.h	1.24 06/10/10
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

#ifndef NET_UTILS_MD_H
#define NET_UTILS_MD_H

#include <sys/socket.h>
#include <sys/types.h>
#include <netdb.h>
#include <netinet/in.h>
#include <unistd.h>

#ifndef USE_SELECT
#include <sys/poll.h>
#endif

#ifndef AF_INET6
#include "ipv6_defs.h"
#endif

/*
 * Linux header files define sockaddr_in6 incorrectly (missing the
 * sin6_scope_id field) so we use our own definition.
 */ 
#ifdef __linux__
struct sockaddr_in6_ext { 
        unsigned short int      sin6_family; 
        unsigned short int      sin6_port;
        unsigned int            sin6_flowinfo;
        struct in6_addr         sin6_addr;
        unsigned int            sin6_scope_id;
};
#endif


#ifdef __linux__
extern int NET_Timeout(int s, long timeout);
extern int NET_Read(int s, void* buf, size_t len);
extern int NET_RecvFrom(int s, void *buf, int len, unsigned int flags,
       struct sockaddr *from, int *fromlen);
extern int NET_ReadV(int s, const struct iovec * vector, int count);
extern int NET_Send(int s, void *msg, int len, unsigned int flags);
extern int NET_SendTo(int s, const void *msg, int len,  unsigned  int
       flags, const struct sockaddr *to, int tolen);
extern int NET_Writev(int s, const struct iovec * vector, int count);
extern int NET_Connect(int s, struct sockaddr *addr, int addrlen);
extern int NET_Accept(int s, struct sockaddr *addr, int *addrlen);
extern int NET_SocketClose(int s);
extern int NET_Dup2(int oldfd, int newfd);

#ifdef USE_SELECT
extern int NET_Select(int s, fd_set *readfds, fd_set *writefds,
               fd_set *exceptfds, struct timeval *timeout);
#else
extern int NET_Poll(struct pollfd *ufds, unsigned int nfds, int timeout);
#endif

#else

#define NET_Timeout	JVM_Timeout
#define NET_Read	JVM_Read
#define NET_RecvFrom	JVM_RecvFrom
#define NET_ReadV	readv
#define NET_Send	JVM_Send
#define NET_SendTo	JVM_SendTo
#define NET_WriteV	writev
#define NET_Connect	JVM_Connect
#define NET_Accept	JVM_Accept
#define NET_SocketClose JVM_SocketClose
#define NET_Dup2	dup2
#define NET_Select	select
#define NET_Poll	poll

#endif

#if defined(__linux__) && defined(AF_INET6) 
int getDefaultIPv6Interface(struct in6_addr *target_addr);
#endif


/* needed from libsocket on Solaris 8 */

typedef int (*getaddrinfo_f)(const char *nodename, const char  *servname,
     const struct addrinfo *hints, struct addrinfo **res);

typedef void (*freeaddrinfo_f)(struct addrinfo *);

typedef int (*getnameinfo_f)(const struct sockaddr *, size_t,
    char *, size_t, char *, size_t, int);

extern getaddrinfo_f getaddrinfo_ptr;
extern freeaddrinfo_f freeaddrinfo_ptr;
extern getnameinfo_f getnameinfo_ptr;

/* do we have address translation support */

extern jboolean NET_addrtransAvailable();

/************************************************************************
 * Macros and constants
 */

#define MAX_BUFFER_LEN 2048
#define MAX_HEAP_BUFFER_LEN 65536

#ifdef AF_INET6

#ifdef __linux__
#define SOCKADDR	union { \
                            struct sockaddr_in him4; \
                            struct sockaddr_in6 him6; \
			    struct sockaddr_in6_ext him6_ext; \
                        } 
#else 
#define SOCKADDR        union { \
                            struct sockaddr_in him4; \
                            struct sockaddr_in6 him6; \
			} 
#endif

#define SOCKADDR_LEN	(ipv6_available() ? sizeof(SOCKADDR) : \
                         sizeof(struct sockaddr_in))

#else

#define SOCKADDR    	union { struct sockaddr_in him4 }
#define SOCKADDR_LEN 	sizeof(SOCKADDR)

#endif

/************************************************************************
 *  Utilities
 */

void NET_ThrowByNameWithLastError(JNIEnv *env, const char *name,
                   const char *defaultDetail);


#endif /* NET_UTILS_MD_H */
