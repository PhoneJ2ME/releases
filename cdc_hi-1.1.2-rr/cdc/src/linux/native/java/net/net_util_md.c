/*
 * @(#)net_util_md.c	1.14 06/10/13
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

#include <errno.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/tcp.h>	/* Defines TCP_NODELAY, needed for 2.6 */
#include <netinet/in.h>
#include <net/if.h>
#include <netdb.h>
#include <stdlib.h>
#include <dlfcn.h>

#ifdef __linux__
#include <arpa/inet.h>
#include <net/route.h>
#include <sys/utsname.h>

#ifndef IPV6_FLOWINFO_SEND
#define IPV6_FLOWINFO_SEND      33
#endif

#endif

#include "jni_util.h"
#include "jvm.h"
#include "net_util.h"

#include "java_net_InetAddress.h"
#include "java_net_Inet4Address.h"
#include "java_net_Inet6Address.h"
#include "java_net_SocketOptions.h"

/* needed from libsocket on Solaris 8 */

getaddrinfo_f getaddrinfo_ptr = NULL;
freeaddrinfo_f freeaddrinfo_ptr = NULL;
getnameinfo_f getnameinfo_ptr = NULL;


/*
 * EXCLBIND socket options only on Solaris 8 & 9.
 */
#if defined(__solaris__) && !defined(TCP_EXCLBIND)
#define TCP_EXCLBIND            0x21
#endif
#if defined(__solaris__) && !defined(UDP_EXCLBIND)
#define UDP_EXCLBIND            0x0101
#endif

#ifdef __solaris__
static int init_max_buf;
static int tcp_max_buf;
static int udp_max_buf;

/*
 * Get the specified parameter from the specified driver. The value
 * of the parameter is assumed to be an 'int'. If the parameter
 * cannot be obtained return the specified default value.
 */
static int 
getParam(char *driver, char *param, int dflt)
{
    struct strioctl stri;
    char buf [64];
    int s;
    int value;

    s = open (driver, O_RDWR);
    if (s < 0) {
	return dflt;
    }
    strncpy (buf, param, sizeof(buf));
    stri.ic_cmd = ND_GET;
    stri.ic_timout = 0;
    stri.ic_dp = buf;
    stri.ic_len = sizeof(buf);
    if (ioctl (s, I_STR, &stri) < 0) {
	value = dflt;
    } else {
	value = atoi(buf);
    }
    close (s);
    return value;
}
#endif

#ifdef __linux__
static int kernelV22 = 0;
static int vinit = 0;

int kernelIsV22 () {
    if (!vinit) {
	struct utsname sysinfo;
	if (uname(&sysinfo) == 0) {
	    sysinfo.release[3] = '\0';
	    if (strcmp(sysinfo.release, "2.2") == 0) {
		kernelV22 = JNI_TRUE;
	    }
	}
	vinit = 1;
    }
    return kernelV22;
}

static int kernelV24 = 0;
static int vinit24 = 0;

int kernelIsV24 () {
    if (!vinit24) {
	struct utsname sysinfo;
	if (uname(&sysinfo) == 0) {
	    sysinfo.release[3] = '\0';
	    if (strcmp(sysinfo.release, "2.4") == 0) {
		kernelV24 = JNI_TRUE;
	    }
	}
	vinit24 = 1;
    }
    return kernelV24;
}
#endif

#include "jni_statics.h"
int initialized = 0;
void init(JNIEnv *env) {
    if (!initialized) {
        Java_java_net_InetAddress_init(env, 0);
        Java_java_net_Inet4Address_init(env, 0);
        Java_java_net_Inet6Address_init(env, 0);
	initialized = 1;
    }
}

void
NET_ThrowByNameWithLastError(JNIEnv *env, const char *name,
                   const char *defaultDetail) {
    char errmsg[255];
    sprintf(errmsg, "errno: %d, error: %s\n", errno, defaultDetail); 
    JNU_ThrowByNameWithLastError(env, name, errmsg); 
}

void 
NET_ThrowCurrent(JNIEnv *env, char *msg) {
    NET_ThrowNew(env, errno, msg);
}

void
NET_ThrowNew(JNIEnv *env, int errorNumber, char *msg) {
    char fullMsg[512];
    if (!msg) {
	msg = "no further information";
    }
    switch(errorNumber) {
    case EBADF: 
	jio_snprintf(fullMsg, sizeof(fullMsg), "socket closed: %s", msg);
	JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException", fullMsg);
	break;
    case EINTR:
	JNU_ThrowByName(env, JNU_JAVAIOPKG "InterruptedIOException", msg);
	break;
    default:
	errno = errorNumber;
        JNU_ThrowByNameWithLastError(env, JNU_JAVANETPKG "SocketException", msg);
	break;
    }
}


jfieldID
NET_GetFileDescriptorID(JNIEnv *env)
{
    jclass cls = (*env)->FindClass(env, "java/io/FileDescriptor");
    CHECK_NULL_RETURN(cls, NULL);
    return (*env)->GetFieldID(env, cls, "fd", "I");
}

jint  IPv6_supported()
{
#ifndef AF_INET6
    return JNI_FALSE;
#endif

#ifdef AF_INET6
    int fd;
    void *ipv6_fn;

    fd = JVM_Socket(AF_INET6, SOCK_STREAM, 0) ;
    if (fd < 0) {
	/* 
	 *  TODO: We really cant tell since it may be an unrelated error 
	 *  for now we will assume that AF_INET6 is not available 
	 */
	return JNI_FALSE;
    } 

    /**
     * Linux - check if any interface has an IPv6 address.
     * Don't need to parse the line - we just need an indication.
     */
    {
        FILE *fP = fopen("/proc/net/if_inet6", "r");
        char buf[255];
        char *bufP;

        if (fP == NULL) {
	    close(fd);
            return JNI_FALSE;
        }
        bufP = fgets(buf, sizeof(buf), fP);
        fclose(fP);
        if (bufP == NULL) {
	    close(fd);
            return JNI_FALSE;
        }
    }

    /**
     * On Solaris 8 it's possible to create INET6 sockets even
     * though IPv6 is not enabled on all interfaces. Thus we
     * query the number of IPv6 addresses to verify that IPv6
     * has been configured on at least one interface.
     *
     * On Linux it doesn't matter - if IPv6 is built-in the 
     * kernel then IPv6 addresses will be bound automatically
     * to all interfaces.
     */
#endif /* AF_INET6 */
    /* 
     *  OK we may have the stack available in the kernel,
     *  we should also check if the APIs are available.
     */
    ipv6_fn = dlsym(RTLD_DEFAULT, "inet_pton");
    if (ipv6_fn == NULL ) { 
	close(fd);
	return JNI_FALSE;
    }

    /*
     * We've got the library, let's get the pointers to some
     * IPV6 specific functions. We have to do that because, at least
     * on Solaris we may build on a system without IPV6 networking
     * libraries, therefore we can't have a hard link to these
     * functions.
     */
    getaddrinfo_ptr = (getaddrinfo_f)
        dlsym(RTLD_DEFAULT, "getaddrinfo");

    freeaddrinfo_ptr = (freeaddrinfo_f)
        dlsym(RTLD_DEFAULT, "freeaddrinfo");

    getnameinfo_ptr = (getnameinfo_f)
        dlsym(RTLD_DEFAULT, "getnameinfo");

    if (freeaddrinfo_ptr == NULL || getnameinfo_ptr == NULL) {
        /* We need all 3 of them */
        getaddrinfo_ptr = NULL;
    }

    close(fd);
    return JNI_TRUE; 
}

void 
NET_AllocSockaddr(struct sockaddr **him, int *len) {
#ifdef AF_INET6
    if (ipv6_available()) {
	struct sockaddr_in6 *him6 = (struct sockaddr_in6*)malloc(sizeof(struct sockaddr_in6));
	*him = (struct sockaddr*)him6;
	*len = sizeof(struct sockaddr_in6);
    } else
#endif /* AF_INET6 */  
	{
	    struct sockaddr_in *him4 = (struct sockaddr_in*)malloc(sizeof(struct sockaddr_in));
	    *him = (struct sockaddr*)him4;
	    *len = sizeof(struct sockaddr_in);
	}
}

#if defined(__linux__) && defined(AF_INET6)


/* following code creates a list of addresses from the kernel
 * routing table that are routed via the loopback address.
 * We check all destination addresses against this table
 * and override the scope_id field to use the relevant value for "lo"
 * in order to work-around the Linux bug that prevents packets destined
 * for certain local addresses from being sent via a physical interface.
 */

struct loopback_route {
    struct in6_addr addr; /* destination address */
    int plen; /* prefix length */
};

static struct loopback_route *loRoutes = 0;
static int nRoutes = 0; /* number of routes */
static int loRoutes_size = 16; /* initial size */
static int lo_scope_id = 0;

static void initLoopbackRoutes();

void printAddr (struct in6_addr *addr) {
    int i;
    for (i=0; i<16; i++) {
	printf ("%02x", addr->s6_addr[i]);
    }
    printf ("\n");
}

   
static void initLoopbackRoutes() {
    FILE *f;
    char srcp[8][5];
    char hopp[8][5];
    int dest_plen, src_plen, use, refcnt, metric;
    unsigned long flags;
    char dest_str[40];
    struct in6_addr dest_addr;
    char device[16];
    
    if (loRoutes != 0) {
	free (loRoutes);
    }
    loRoutes = calloc (loRoutes_size, sizeof(struct loopback_route));
    if (loRoutes == 0) {
	return;
    }
    /*
     * Scan /proc/net/ipv6_route looking for a matching
     * route.
     */
    if ((f = fopen("/proc/net/ipv6_route", "r")) == NULL) {
	return ;
    }
    while (fscanf(f, "%4s%4s%4s%4s%4s%4s%4s%4s %02x "
                     "%4s%4s%4s%4s%4s%4s%4s%4s %02x "
                     "%4s%4s%4s%4s%4s%4s%4s%4s "
                     "%08x %08x %08x %08lx %8s",
		     dest_str, &dest_str[5], &dest_str[10], &dest_str[15],
		     &dest_str[20], &dest_str[25], &dest_str[30], &dest_str[35],
		     &dest_plen,
		     srcp[0], srcp[1], srcp[2], srcp[3],
		     srcp[4], srcp[5], srcp[6], srcp[7],
		     &src_plen,
		     hopp[0], hopp[1], hopp[2], hopp[3],
		     hopp[4], hopp[5], hopp[6], hopp[7],
		     &metric, &use, &refcnt, &flags, device) == 31) {

	/*
 	 * Some routes should be ignored
	 */
	if ( (dest_plen < 0 || dest_plen > 128)  ||
	     (src_plen != 0) ||			
	     (flags & (RTF_POLICY | RTF_FLOW)) ||
	     ((flags & RTF_REJECT) && dest_plen == 0) ) {
	    continue;
	}

	/*
  	 * Convert the destination address
	 */
	dest_str[4] = ':';
	dest_str[9] = ':';
	dest_str[14] = ':';
	dest_str[19] = ':';
	dest_str[24] = ':';
	dest_str[29] = ':';
	dest_str[34] = ':';
	dest_str[39] = '\0';

	if (inet_pton(AF_INET6, dest_str, &dest_addr) < 0) {
	    /* not an Ipv6 address */
	    continue;
	} 
	if (strcmp(device, "lo") != 0) {
	    /* Not a loopback route */
	    continue;
	} else {
	    if (nRoutes == loRoutes_size) {
		loRoutes = realloc (loRoutes, loRoutes_size * 
				sizeof (struct loopback_route) * 2);
		if (loRoutes == 0) {
		    return ;
		}
		loRoutes_size *= 2;
	    }
	    memcpy (&loRoutes[nRoutes].addr,&dest_addr,sizeof(struct in6_addr));
	    loRoutes[nRoutes].plen = dest_plen;
	    nRoutes ++;
	}
    }

    fclose (f);
    {
	/* now find the scope_id for "lo" */

        char devname[20];
        char addr6p[8][5];
        int plen, scope, dad_status, if_idx;

        if ((f = fopen("/proc/net/if_inet6", "r")) != NULL) {
            while (fscanf(f, "%4s%4s%4s%4s%4s%4s%4s%4s %02x %02x %02x %02x %20s\n",
                      addr6p[0], addr6p[1], addr6p[2], addr6p[3],
                      addr6p[4], addr6p[5], addr6p[6], addr6p[7],
                  &if_idx, &plen, &scope, &dad_status, devname) == 13) {

		if (strcmp(devname, "lo") == 0) {	
		    /*
		     * Found - so just return the index
		     */
		    fclose(f);
		    lo_scope_id = if_idx;
		    return;
		}
	    }
	    fclose(f);
        } 
    }
}

/*
 * Following is used for binding to local addresses. Equivalent
 * to code above, for bind().
 */

struct localinterface {
    int index;
    char localaddr [16];
};

static struct localinterface *localifs = 0;
static int localifsSize = 0;	/* size of array */
static int nifs = 0;		/* number of entries used in array */

/* not thread safe: make sure called once from one thread */

static void initLocalIfs () {
    FILE *f;
    unsigned char staddr [16];
    char ifname [32];
    struct localinterface *lif=0;
    int index, x1, x2, x3;
    unsigned int u0,u1,u2,u3,u4,u5,u6,u7,u8,u9,ua,ub,uc,ud,ue,uf;

    if ((f = fopen("/proc/net/if_inet6", "r")) == NULL) {
	return ;
    }
    while (fscanf (f, "%2x%2x%2x%2x%2x%2x%2x%2x%2x%2x%2x%2x%2x%2x%2x%2x "
		"%d %x %x %x %s",&u0,&u1,&u2,&u3,&u4,&u5,&u6,&u7,
		&u8,&u9,&ua,&ub,&uc,&ud,&ue,&uf,
		&index, &x1, &x2, &x3, ifname) == 21) {
	staddr[0] = (unsigned char)u0;
	staddr[1] = (unsigned char)u1; 
	staddr[2] = (unsigned char)u2;
	staddr[3] = (unsigned char)u3; 
	staddr[4] = (unsigned char)u4;
	staddr[5] = (unsigned char)u5; 
	staddr[6] = (unsigned char)u6;
	staddr[7] = (unsigned char)u7; 
	staddr[8] = (unsigned char)u8;
	staddr[9] = (unsigned char)u9;
	staddr[10] = (unsigned char)ua;
	staddr[11] = (unsigned char)ub; 
	staddr[12] = (unsigned char)uc;
	staddr[13] = (unsigned char)ud; 
	staddr[14] = (unsigned char)ue;
	staddr[15] = (unsigned char)uf; 
	nifs ++;
	if (nifs > localifsSize) {
	    localifs = (struct localinterface *) realloc (
			localifs, sizeof (struct localinterface)* (localifsSize+5));
	    if (localifs == 0) {
		nifs = 0;
    		fclose (f);
		return;
	    }
	    lif = localifs + localifsSize;
	    localifsSize += 5;
	} else {
	    lif ++;
	}
	memcpy (lif->localaddr, staddr, 16);
	lif->index = index;
    }
    fclose (f);
}
	
/* return the scope_id (interface index) of the
 * interface corresponding to the given address
 * returns 0 if no match found
 */
	
static int getLocalScopeID (char *addr) {
    struct localinterface *lif;
    int i;
    if (localifs == 0) {
	initLocalIfs();
    }
    for (i=0, lif=localifs; i<nifs; i++, lif++) {
	if (memcmp (addr, lif->localaddr, 16) == 0) {
	    return lif->index;
	}
    }
    return 0;
}
    
void initLocalAddrTable () {
    initLoopbackRoutes();
    initLocalIfs();
}

#else

void initLocalAddrTable () {}

#endif

void
NET_InetAddressToSockaddr(JNIEnv *env, jobject iaObj, int port, struct sockaddr *him, int *len) {
#ifdef AF_INET6
    if (ipv6_available()) {
	struct sockaddr_in6 *him6 = (struct sockaddr_in6 *)him;
	jbyteArray ipaddress;
	jbyte caddr[16];
	jint family;
	jint address;

	family = (*env)->GetIntField(env, iaObj, JNI_STATIC(java_net_InetAddress, ia_familyID)) == IPv4? AF_INET : AF_INET6;
	/* needs work. 1. family 2. clean up him6 etc deallocate memory */

	if (family == AF_INET) { /* will convert to IPv4-mapped address */
	    memset((char *) caddr, 0, 16);
	    address = (*env)->GetIntField(env, iaObj, JNI_STATIC(java_net_InetAddress, ia_addressID));
	    if (address == INADDR_ANY) {
	  	/* we would always prefer IPv6 wildcard address 
		   caddr[10] = 0xff;
		   caddr[11] = 0xff; */
	    } else {
		caddr[10] = 0xff;
		caddr[11] = 0xff;
		caddr[12] = ((address >> 24) & 0xff);
		caddr[13] = ((address >> 16) & 0xff);
		caddr[14] = ((address >> 8) & 0xff);
		caddr[15] = (address & 0xff); 
	    }
	} else {
	    ipaddress = (*env)->GetObjectField(env, iaObj, JNI_STATIC(java_net_Inet6Address, ia6_ipaddressID));
	    (*env)->GetByteArrayRegion(env, ipaddress, 0, 16, caddr);
	}
	memset((char *)him6, 0, sizeof(struct sockaddr_in6));
	him6->sin6_port = htons(port);
	memcpy((void *)&(him6->sin6_addr), caddr, sizeof(struct in6_addr) );
	him6->sin6_family = AF_INET6; 
	*len = sizeof(struct sockaddr_in6) ;

	/*
 	 * On Linux if we are connecting to a link-local address
	 * we need to specify the interface in the scope_id (2.4 kernel only)
	 */
#if defined(__linux__) && defined(AF_INET6)
	if (IN6_IS_ADDR_LINKLOCAL(&(him6->sin6_addr))) {
	    static jclass cls;
	    static jfieldID scopeID;
	    int scope_id = 0;
            int old_kernel = kernelIsV22();

	    /*
	     * On first call get the JNI references
	     */	    
	    if (cls == NULL) {
	        jclass c = (*env)->FindClass(env, "java/net/Inet6Address");
		if (c != NULL) {
		    cls = (*env)->NewGlobalRef(env, c);
		    if (cls != NULL) {
			scopeID = (*env)->GetFieldID(env, cls, "cached_scope_id", "I");
		    }
		}
	    }

	    /*
	     * If there's already a scope_id cached then use
	     * it. Otherwise consult the IPv6 routing tables to
	     * determine the appropriate interface.
	     */
	    if (scopeID) {
	        scope_id = (int)(*env)->GetIntField(env, iaObj, scopeID);
	    }
	    if (scope_id == 0) {
	        if (!old_kernel) {
                        if (kernelIsV24()) {
                            scope_id = getDefaultIPv6Interface( &(him6->sin6_addr) );
                        } else {
                            scope_id = getLocalScopeID( (char *)&(him6->sin6_addr) );
                            if (scope_id == 0) {
                                scope_id = getDefaultIPv6Interface( &(him6->sin6_addr) );
                            }
                        }
	                (*env)->SetIntField(env, iaObj, scopeID, scope_id);
                }
	    }

	    /*
	     * If we have a scope_id use the extended form
	     * of sockaddr_in6.
	     */
	    if (scope_id > 0) {
		struct sockaddr_in6_ext *him6_ext = 
			(struct sockaddr_in6_ext *)him;
	        him6_ext->sin6_scope_id = scope_id;
	        *len = sizeof(struct sockaddr_in6_ext);
	    }
	}
#endif

    } else
#endif /* AF_INET6 */  
	{
	    struct sockaddr_in *him4 = (struct sockaddr_in*)him;
	    jint address;
	    memset((char *) him4, 0, sizeof(struct sockaddr_in));
	    address = (*env)->GetIntField(env, iaObj, JNI_STATIC(java_net_InetAddress, ia_addressID));
	    him4->sin_port = htons((short) port);
	    him4->sin_addr.s_addr = (uint32_t) htonl(address);
	    him4->sin_family = AF_INET;
	    *len = sizeof(struct sockaddr_in);
	}
}

jobject
NET_SockaddrToInetAddress(JNIEnv *env, struct sockaddr *him, int *port) {
    jobject iaObj;
    init(env);
#ifdef AF_INET6
    if (him->sa_family == AF_INET6) {
	jbyteArray ipaddress;
	struct sockaddr_in6 *him6 = (struct sockaddr_in6 *)him;
	jbyte *caddr = (jbyte *)&(him6->sin6_addr);
	if (NET_IsIPv4Mapped(caddr)) {
	    int address;
	    static jclass inet4Cls = 0;
	    if (inet4Cls == 0) {
		jclass c = (*env)->FindClass(env, "java/net/Inet4Address");
		CHECK_NULL_RETURN(c, NULL);
		inet4Cls = (*env)->NewGlobalRef(env, c);
		CHECK_NULL_RETURN(inet4Cls, NULL);
		(*env)->DeleteLocalRef(env, c);
	    }
	    iaObj = (*env)->NewObject(env, inet4Cls, JNI_STATIC(java_net_Inet4Address, ia4_ctrID));
	    CHECK_NULL_RETURN(iaObj, NULL);
	    address = NET_IPv4MappedToIPv4(caddr);
	    (*env)->SetIntField(env, iaObj, JNI_STATIC(java_net_InetAddress, ia_addressID), address);
	    (*env)->SetIntField(env, iaObj, JNI_STATIC(java_net_InetAddress, ia_familyID), IPv4);
	} else { 
	    static jclass inet6Cls = 0;
	    if (inet6Cls == 0) {
		jclass c = (*env)->FindClass(env, "java/net/Inet6Address");
		CHECK_NULL_RETURN(c, NULL);
		inet6Cls = (*env)->NewGlobalRef(env, c);
		CHECK_NULL_RETURN(inet6Cls, NULL);
		(*env)->DeleteLocalRef(env, c);
	    }
	    iaObj = (*env)->NewObject(env, inet6Cls, JNI_STATIC(java_net_Inet6Address, ia6_ctrID));
	    CHECK_NULL_RETURN(iaObj, NULL);
	    ipaddress = (*env)->NewByteArray(env, 16);	
	    CHECK_NULL_RETURN(ipaddress, NULL);
	    (*env)->SetByteArrayRegion(env, ipaddress, 0, 16, 
				       (jbyte *)&(him6->sin6_addr)); 
	    
	    (*env)->SetObjectField(env, iaObj, JNI_STATIC(java_net_Inet6Address, ia6_ipaddressID), ipaddress);
	    
	    (*env)->SetIntField(env, iaObj, JNI_STATIC(java_net_InetAddress, ia_familyID), IPv6);
	}
	*port = ntohs(him6->sin6_port);
    } else 
#endif /* AF_INET6 */ 
	{
	    struct sockaddr_in *him4 = (struct sockaddr_in *)him;
	    static jclass inet4Cls = 0;

	    if (inet4Cls == 0) {	
	        jclass c = (*env)->FindClass(env, "java/net/Inet4Address");
		CHECK_NULL_RETURN(c, NULL);
		inet4Cls = (*env)->NewGlobalRef(env, c);
		CHECK_NULL_RETURN(inet4Cls, NULL);
		(*env)->DeleteLocalRef(env, c);
	    }
	    iaObj = (*env)->NewObject(env, inet4Cls, JNI_STATIC(java_net_Inet4Address, ia4_ctrID));
	    CHECK_NULL_RETURN(iaObj, NULL);
	    (*env)->SetIntField(env, iaObj, JNI_STATIC(java_net_InetAddress, ia_familyID), IPv4);
	    (*env)->SetIntField(env, iaObj, JNI_STATIC(java_net_InetAddress, ia_addressID), ntohl(him4->sin_addr.s_addr));
	    *port = ntohs(him4->sin_port);
	}
    return iaObj;
}

jint
NET_SockaddrEqualsInetAddress(JNIEnv *env, struct sockaddr *him, jobject iaObj) {
    jint family = (*env)->GetIntField(env, iaObj, JNI_STATIC(java_net_InetAddress, ia_familyID)) == IPv4? AF_INET : AF_INET6;

#ifdef AF_INET6
    if (him->sa_family == AF_INET6) {
	struct sockaddr_in6 *him6 = (struct sockaddr_in6 *)him;
	jbyte *caddrNew = (jbyte *)&(him6->sin6_addr);
	if (NET_IsIPv4Mapped(caddrNew)) {
	    int addrNew;
	    int addrCur;
	    if (family == AF_INET6) {
		return JNI_FALSE;
	    }
	    addrNew = NET_IPv4MappedToIPv4(caddrNew);
	    addrCur = (*env)->GetIntField(env, iaObj, JNI_STATIC(java_net_InetAddress, ia_addressID));
	    if (addrNew == addrCur) {
		return JNI_TRUE;
	    } else {
		return JNI_FALSE;
	    }
	} else {
	    jbyteArray ipaddress;
	    jbyte caddrCur[16];

	    if (family == AF_INET) {
		return JNI_FALSE;
	    }
	    ipaddress = (*env)->GetObjectField(env, iaObj, JNI_STATIC(java_net_Inet6Address, ia6_ipaddressID));
	    (*env)->GetByteArrayRegion(env, ipaddress, 0, 16, caddrCur);
	    if (NET_IsEqual(caddrNew, caddrCur)) {
		return JNI_TRUE;
	    } else {
		return JNI_FALSE;
	    }
	}
    } else 
#endif /* AF_INET6 */ 
	{
	    struct sockaddr_in *him4 = (struct sockaddr_in *)him;
	    int addrNew, addrCur;
	    if (family != AF_INET) {
		return JNI_FALSE;
	    }
	    addrNew = ntohl(him4->sin_addr.s_addr);
	    addrCur = (*env)->GetIntField(env, iaObj, JNI_STATIC(java_net_InetAddress, ia_addressID));
	    if (addrNew == addrCur) {
		return JNI_TRUE;
	    } else {
		return JNI_FALSE;
	    }
	}
}

void
NET_SetTrafficClass(struct sockaddr *him, int trafficClass) {
#ifdef AF_INET6    
    if (him->sa_family == AF_INET6) {
	struct sockaddr_in6 *him6 = (struct sockaddr_in6 *)him;
	him6->sin6_flowinfo = htonl((trafficClass & 0xff) << 20);
    }
#endif /* AF_INET6 */
}

jint
NET_GetPortFromSockaddr(struct sockaddr *him) {
#ifdef AF_INET6
    if (him->sa_family == AF_INET6) {
	return ntohs(((struct sockaddr_in6*)him)->sin6_port);

	} else
#endif /* AF_INET6 */ 
	    {
		return ntohs(((struct sockaddr_in*)him)->sin_port);
	    }
}

int 
NET_IsIPv4Mapped(jbyte* caddr) {
    int i;
    for (i = 0; i < 10; i++) {
	if (caddr[i] != 0x00) {
	    return 0; /* false */
	}
    }

    if (((caddr[10] & 0xff) == 0xff) && ((caddr[11] & 0xff) == 0xff)) {
	return 1; /* true */
    }
    return 0; /* false */
}

int
NET_IPv4MappedToIPv4(jbyte* caddr) {
    return ((caddr[12] & 0xff) << 24) | ((caddr[13] & 0xff) << 16) | ((caddr[14] & 0xff) << 8)
	| (caddr[15] & 0xff);
}

int 
NET_IsEqual(jbyte* caddr1, jbyte* caddr2) {
    int i;
    for (i = 0; i < 16; i++) {
	if (caddr1[i] != caddr2[i]) {
	    return 0; /* false */
	}
    }
    return 1;
}

jboolean NET_addrtransAvailable() {
    return (jboolean)(getaddrinfo_ptr != NULL);
}

/*
 * Map the Java level socket option to the platform specific
 * level and option name. 
 */
int 
NET_MapSocketOption(jint cmd, int *level, int *optname) {
    static struct {
	jint cmd;
	int level;
	int optname;
    } const opts[] = {
	{ java_net_SocketOptions_TCP_NODELAY,		IPPROTO_TCP,	TCP_NODELAY },
	{ java_net_SocketOptions_SO_OOBINLINE,		SOL_SOCKET,	SO_OOBINLINE },
	{ java_net_SocketOptions_SO_LINGER,		SOL_SOCKET,	SO_LINGER },
	{ java_net_SocketOptions_SO_SNDBUF,		SOL_SOCKET,	SO_SNDBUF },
	{ java_net_SocketOptions_SO_RCVBUF,		SOL_SOCKET,	SO_RCVBUF },
	{ java_net_SocketOptions_SO_KEEPALIVE,		SOL_SOCKET,	SO_KEEPALIVE },
	{ java_net_SocketOptions_SO_REUSEADDR,		SOL_SOCKET,	SO_REUSEADDR },
	{ java_net_SocketOptions_SO_BROADCAST,          SOL_SOCKET,     SO_BROADCAST },
	{ java_net_SocketOptions_IP_TOS,		IPPROTO_IP,	IP_TOS },
	{ java_net_SocketOptions_IP_MULTICAST_IF,	IPPROTO_IP, 	IP_MULTICAST_IF },
	{ java_net_SocketOptions_IP_MULTICAST_IF2,      IPPROTO_IP, 	IP_MULTICAST_IF },
	{ java_net_SocketOptions_IP_MULTICAST_LOOP,  	IPPROTO_IP, 	IP_MULTICAST_LOOP },
    };

    int i;

    /*
     * Different multicast options if IPv6 is enabled
     */
#ifdef AF_INET6
    if (ipv6_available()) {
        switch (cmd) {
	    case java_net_SocketOptions_IP_MULTICAST_IF:
	    case java_net_SocketOptions_IP_MULTICAST_IF2:
		*level = IPPROTO_IPV6;
		*optname = IPV6_MULTICAST_IF;
		return 0;

	    case java_net_SocketOptions_IP_MULTICAST_LOOP:
		*level = IPPROTO_IPV6;
		*optname = IPV6_MULTICAST_LOOP;
		return 0;
	}
    }
#endif

    /*
     * Map the Java level option to the native level 
     */
    for (i=0; i<(int)(sizeof(opts) / sizeof(opts[0])); i++) {
	if (cmd == opts[i].cmd) {
	    *level = opts[i].level;
	    *optname = opts[i].optname;
	    return 0;
	}
    }

    /* not found */
    return -1;
}


/*
 * Determine the default interface for an IPv6 address.
 *
 * 1. Scans /proc/net/ipv6_route for a matching route
 *    (eg: fe80::/10 or a route for the specific address).
 *    This will tell us the interface to use (eg: "eth0").
 * 
 * 2. Lookup /proc/net/if_inet6 to map the interface
 *    name to an interface index.
 *
 * Returns :-
 *	-1 if error 
 *	 0 if no matching interface
 *      >1 interface index to use for the link-local address.
 */
#if defined(__linux__) && defined(AF_INET6)
int getDefaultIPv6Interface(struct in6_addr *target_addr) {
    FILE *f;
    char srcp[8][5];
    char hopp[8][5];
    int dest_plen, src_plen, use, refcnt, metric;
    unsigned long flags;
    char dest_str[40];
    struct in6_addr dest_addr;
    char device[16];
    jboolean match = JNI_FALSE;

    /*
     * Scan /proc/net/ipv6_route looking for a matching
     * route.
     */
    if ((f = fopen("/proc/net/ipv6_route", "r")) == NULL) {
	return -1;
    }
    while (fscanf(f, "%4s%4s%4s%4s%4s%4s%4s%4s %02x "
                     "%4s%4s%4s%4s%4s%4s%4s%4s %02x "
                     "%4s%4s%4s%4s%4s%4s%4s%4s "
                     "%08x %08x %08x %08lx %8s",
		     dest_str, &dest_str[5], &dest_str[10], &dest_str[15],
		     &dest_str[20], &dest_str[25], &dest_str[30], &dest_str[35],
		     &dest_plen,
		     srcp[0], srcp[1], srcp[2], srcp[3],
		     srcp[4], srcp[5], srcp[6], srcp[7],
		     &src_plen,
		     hopp[0], hopp[1], hopp[2], hopp[3],
		     hopp[4], hopp[5], hopp[6], hopp[7],
		     &metric, &use, &refcnt, &flags, device) == 31) {

	/*
 	 * Some routes should be ignored
	 */
	if ( (dest_plen < 0 || dest_plen > 128)  ||
	     (src_plen != 0) ||			
	     (flags & (RTF_POLICY | RTF_FLOW)) ||
	     ((flags & RTF_REJECT) && dest_plen == 0) ) {
	    continue;
	}

	/*
  	 * Convert the destination address
	 */
	dest_str[4] = ':';
	dest_str[9] = ':';
	dest_str[14] = ':';
	dest_str[19] = ':';
	dest_str[24] = ':';
	dest_str[29] = ':';
	dest_str[34] = ':';
	dest_str[39] = '\0';

	if (inet_pton(AF_INET6, dest_str, &dest_addr) < 0) {
	    /* not an Ipv6 address */
	    continue;
	} else {
	    /*
	     * The prefix len (dest_plen) indicates the number of bits we 
 	     * need to match on.
	     *
	     * dest_plen / 8	=> number of bytes to match
	     * dest_plen % 8	=> number of additional bits to match
	     *
	     * eg: fe80::/10 => match 1 byte + 2 additional bits in the
	     *	                the next byte.
	     */
	    int byte_count = dest_plen >> 3;		
	    int extra_bits = dest_plen & 0x3;

	    if (byte_count > 0) {
		if (memcmp(target_addr, &dest_addr, byte_count)) {
                    continue;  /* no match */
                }
	    }

	    if (extra_bits > 0) {
		unsigned char c1 = ((unsigned char *)target_addr)[byte_count];
		unsigned char c2 = ((unsigned char *)&dest_addr)[byte_count];
		unsigned char mask = 0xff << (8 - extra_bits);
		if ((c1 & mask) != (c2 & mask)) {
		    continue;
		}
	    }

	    /*
	     * We have a match
  	     */
	    match = JNI_TRUE;
	    break;
	}
    }
    fclose(f);

    /*
     * If there's a match then we lookup the interface
     * index.
     */
    if (match) {
        /* eliminate compile time warning.
           char addr6[40], devname[20];
        */
        char devname[20];
        char addr6p[8][5];
        int plen, scope, dad_status, if_idx;

        if ((f = fopen("/proc/net/if_inet6", "r")) != NULL) {
            while (fscanf(f, "%4s%4s%4s%4s%4s%4s%4s%4s %02x %02x %02x %02x %20s\n",
                      addr6p[0], addr6p[1], addr6p[2], addr6p[3],
                      addr6p[4], addr6p[5], addr6p[6], addr6p[7],
                  &if_idx, &plen, &scope, &dad_status, devname) == 13) {

		if (strcmp(devname, device) == 0) {	
		    /*
		     * Found - so just return the index
		     */
		    fclose(f);
		    return if_idx;
		}
	    }
	    fclose(f);
        } else {
	    /* 
	     * Couldn't open /proc/net/if_inet6
	     */
	    return -1;
	}
    }

    /*
     * If we get here it means we didn't there wasn't any
     * route or we couldn't get the index of the interface.
     */
    return 0;
}
#endif


/*
 * Wrapper for getsockopt system routine - does any necessary 
 * pre/post processing to deal with OS specific oddies :-
 *
 * IP_TOS is a no-op with IPv6 sockets as it's setup when
 * the connection is established.
 *
 * On Linux the SO_SNDBUF/SO_RCVBUF values must be post-processed
 * to compensate for an incorrect value returned by the kernel.
 */
int 
NET_GetSockOpt(int fd, int level, int opt, void *result,
	       int *len) 
{
    int rv;
    socklen_t optlen = *len;

#ifdef AF_INET6
    if ((level == IPPROTO_IP) && (opt == IP_TOS)) {
	if (ipv6_available()) {

	    /*
	     * For IPv6 socket option implemented at Java-level
	     * so return -1.
	     */
	    int *tc = (int *)result;
	    *tc = -1;
	    return 0;
	}
    }
#endif

    rv = getsockopt(fd, level, opt, result, &optlen);
    *len = optlen;
    if (rv < 0) {
	return rv;
    }

    /*
     * On Linux SO_SNDBUF/SO_RCVBUF aren't symmetric. This
     * stems from additional socket structures in the send 
     * and receive buffers. 
     */
    if ((level == SOL_SOCKET) && ((opt == SO_SNDBUF) 
				  || (opt == SO_RCVBUF))) { 
	int n = *((int *)result); 
	n /= 2;
	*((int *)result) = n; 
    }
    return rv;
}


/*
 * Wrapper for setsockopt system routine - performs any 
 * necessary pre/post processing to deal with OS specific
 * issue :-
 *
 * On Solaris need to limit the suggested value for SO_SNDBUF
 * and SO_RCVBUF to the kernel configured limit
 *
 * For IP_TOS socket option need to mask off bits as this
 * aren't automatically masked by the kernel and results in
 * an error. In addition IP_TOS is a noop with IPv6 as it
 * should be setup as connection time.
 */
int 
NET_SetSockOpt(int fd, int level, int  opt, const void *arg,
               int len)
{
#ifndef IPTOS_TOS_MASK
#define IPTOS_TOS_MASK 0x1e
#endif
#ifndef IPTOS_PREC_MASK
#define IPTOS_PREC_MASK 0xe0
#endif

    /*
     * IPPROTO/IP_TOS :-
     * 1. IPv6 on Solaris: no-op and will be set in flowinfo
     *    field when connecting TCP socket, or sending
     *    UDP packet.
     * 2. IPv6 on Linux: By default Linux ignores flowinfo
     *    field so enable IPV6_FLOWINFO_SEND so that flowinfo
     *    will be examined. 
     * 3. IPv4: set socket option based on ToS and Precedence
     *    fields (otherwise get invalid argument)
     */
    if (level == IPPROTO_IP && opt == IP_TOS) {
        int *iptos;

#if defined(AF_INET6) && defined(__solaris__)
        if (ipv6_available()) {
	    return 0;
	}
#endif

#if defined(AF_INET6) && defined(__linux__)
	if (ipv6_available()) {
	    int optval = 1;
	    return setsockopt(fd, IPPROTO_IPV6, IPV6_FLOWINFO_SEND, 
			      (void *)&optval, sizeof(optval));
        }
#endif

        iptos = (int *)arg;
        *iptos &= (IPTOS_TOS_MASK | IPTOS_PREC_MASK);
    }

    /*
     * SOL_SOCKET/{SO_SNDBUF,SO_RCVBUF} - On Solaris need to
     * ensure that value is <= max_buf as otherwise we get
     * an invalid argument.
     */
#ifdef __solaris__
    if (level == SOL_SOCKET) {
        if (opt == SO_SNDBUF || opt == SO_RCVBUF) {
	    int sotype, arglen;
	    int *bufsize, maxbuf;

	    if (!init_max_buf) {
    		tcp_max_buf = getParam("/dev/tcp", "tcp_max_buf", 64*1024);
    		udp_max_buf = getParam("/dev/udp", "udp_max_buf", 64*1024);
		init_max_buf = 1;
	    }

	    arglen = sizeof(sotype);
	    if (getsockopt(fd, SOL_SOCKET, SO_TYPE, (void *)&sotype,
			   &arglen) < 0) {
		return -1;
	    }

	    maxbuf = (sotype == SOCK_STREAM) ? tcp_max_buf : udp_max_buf;
	    bufsize = (int *)arg;
            if (*bufsize > maxbuf) {
                *bufsize = maxbuf;
            }
        }
    }
#endif

    /*
     * On Linux the receive buffer is used for both socket
     * structures and the the packet payload. The implication
     * is that if SO_RCVBUF is too small then small packets
     * must be discard.
     */
    if (level == SOL_SOCKET && opt == SO_RCVBUF) {
	int *bufsize = (int *)arg;
	if (*bufsize < 1024) {
	    *bufsize = 1024;
	}
    }
    return setsockopt(fd, level, opt, arg, len);
}

/*
 * Wrapper for bind system call - performs any necessary pre/post
 * processing to deal with OS specific issues :-
 *
 * Linux allows a socket to bind to 127.0.0.255 which must be
 * caught.
 *
 * On Solaris 8/9 with IPv6 enabled we must use an exclusive
 * bind to guaranteed a unique port number across the IPv4 and
 * IPv6 port spaces.
 *
 */
int
NET_Bind(int fd, struct sockaddr *him, int len) 
{
#if defined(__solaris__) && defined(AF_INET6) 
    int level = -1;
    int exclbind = -1;
#endif
    int rv;

#ifdef __linux__
    /*
     * ## get bugId for this issue - goes back to 1.2.2 port ##
     * ## When IPv6 is enabled this will be an IPv4-mapped 
     * ## with family set to AF_INET6
     */
    if (him->sa_family == AF_INET) {
	struct sockaddr_in *sa = (struct sockaddr_in *)him;
	if ((ntohl(sa->sin_addr.s_addr) & 0x7f0000ff) == 0x7f0000ff) {
	    errno = EADDRNOTAVAIL;
	    return -1;
	}
    }
#endif

#if defined(__solaris__) && defined(AF_INET6)
    /*
     * Solaris 8/9 have seperate IPv4 and IPv6 port spaces so we 
     * use an exclusive bind when SO_REUSEADDR is not used to
     * give the illusion of a unified port space.
     * This also avoid problems with IPv6 sockets connecting
     * to IPv4 mapped addresses whereby the socket conversion
     * results in a late bind that fails because the 
     * corresponding IPv4 port is in use.
     */
    if (ipv6_available()) {
        int arg, len;

	len = sizeof(arg);
        if (getsockopt(fd, SOL_SOCKET, SO_REUSEADDR, (char *)&arg, 
		       &len) == 0) {
            if (arg == 0) {
		/*
		 * SO_REUSEADDR is disabled so enable TCP_EXCLBIND or
	 	 * UDP_EXCLBIND
		 */
		len = sizeof(arg);
		if (getsockopt(fd, SOL_SOCKET, SO_TYPE, (char *)&arg, 
			       &len) == 0) {
		    if (arg == SOCK_STREAM) {
			level = IPPROTO_TCP;
			exclbind = TCP_EXCLBIND;
		    } else {
		 	level = IPPROTO_UDP;
			exclbind = UDP_EXCLBIND;
		    }
		}

		arg = 1;
		setsockopt(fd, level, exclbind, (char *)&arg, 
			   sizeof(arg));
            }
        }
    }

#endif

    rv = bind(fd, him, len);

#if defined(__solaris__) && defined(AF_INET6)
    if (rv < 0) {
	int en = errno;
	/* Restore *_EXCLBIND if the bind fails */ 
	if (exclbind != -1) {
	    int arg = 0;
	    setsockopt(fd, level, exclbind, (char *)&arg, 	
		       sizeof(arg));
	}
	errno = en;
    }
#endif

    return rv;
}

