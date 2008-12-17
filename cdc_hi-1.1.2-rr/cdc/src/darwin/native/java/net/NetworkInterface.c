/*
 * @(#)NetworkInterface.c	1.9 06/10/10
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
 */

#include <errno.h>
#include <strings.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <netinet/in.h>
#include <net/if.h>
#include <sys/ioctl.h>

#ifdef __linux__
#include <stdio.h>
#include <bits/ioctls.h>
#include <linux/sockios.h>

#else
#include <sys/sockio.h>
#endif

#ifdef __linux__
#define ifr_index ifr_ifindex
#define _PATH_PROCNET_IFINET6           "/proc/net/if_inet6"
#endif

#include "jvm.h"
#include "jni_util.h"
#include "net_util.h"

typedef struct _netaddr  {
    struct sockaddr *addr;
    int family; /* to make searches simple */
    struct _netaddr *next;
} netaddr;

typedef struct _netif {
    char *name;
    int index;
    netaddr *addr;
    struct _netif *next;
} netif;

/************************************************************************
 * NetworkInterface
 */

#include "java_net_NetworkInterface.h"

/************************************************************************
 * NetworkInterface
 */
jclass ni_class;
jfieldID ni_nameID;
jfieldID ni_indexID;
jfieldID ni_descID;
jfieldID ni_addrsID;
jmethodID ni_ctrID;

static jclass ni_iacls;
static jclass ni_ia4cls;
static jclass ni_ia6cls;
static jmethodID ni_ia4ctrID;
static jmethodID ni_ia6ctrID;
static jfieldID ni_iaaddressID;
static jfieldID ni_iafamilyID;
static jfieldID ni_ia6ipaddressID;

static jobject createNetworkInterface(JNIEnv *env, netif *ifs);

static netif *enumInterfaces(JNIEnv *env);
static netif *enumIPv4Interfaces(JNIEnv *env, netif *ifs);
#ifdef AF_INET6
static netif *enumIPv6Interfaces(JNIEnv *env, netif *ifs);
#endif

static netif *addif(JNIEnv *env, netif *ifs, char *if_name, int index, 
		    int family, struct sockaddr *new_addrP, int new_addrlen);
static void freeif(netif *ifs);


/*
 * Class:     java_net_NetworkInterface
 * Method:    init
 * Signature: ()V
 */
JNIEXPORT void JNICALL
Java_java_net_NetworkInterface_init(JNIEnv *env, jclass cls) {
    init_IPv6Available(env);
    ni_class = (*env)->FindClass(env,"java/net/NetworkInterface");
    ni_class = (*env)->NewGlobalRef(env, ni_class);
    ni_nameID = (*env)->GetFieldID(env, ni_class,"name", "Ljava/lang/String;");
    ni_indexID = (*env)->GetFieldID(env, ni_class, "index", "I");
    ni_addrsID = (*env)->GetFieldID(env, ni_class, "addrs", "[Ljava/net/InetAddress;");
    ni_descID = (*env)->GetFieldID(env, ni_class, "displayName", "Ljava/lang/String;");
    ni_ctrID = (*env)->GetMethodID(env, ni_class, "<init>", "()V");

    ni_iacls = (*env)->FindClass(env, "java/net/InetAddress");
    ni_iacls = (*env)->NewGlobalRef(env, ni_iacls);
    ni_ia4cls = (*env)->FindClass(env, "java/net/Inet4Address");
    ni_ia4cls = (*env)->NewGlobalRef(env, ni_ia4cls);
    ni_ia6cls = (*env)->FindClass(env, "java/net/Inet6Address");
    ni_ia6cls = (*env)->NewGlobalRef(env, ni_ia6cls);
    ni_ia4ctrID = (*env)->GetMethodID(env, ni_ia4cls, "<init>", "()V");
    ni_ia6ctrID = (*env)->GetMethodID(env, ni_ia6cls, "<init>", "()V");
    ni_iaaddressID = (*env)->GetFieldID(env, ni_iacls, "address", "I");
    ni_iafamilyID = (*env)->GetFieldID(env, ni_iacls, "family", "I");
    ni_ia6ipaddressID = (*env)->GetFieldID(env, ni_ia6cls, "ipaddress", "[B");
}


/*
 * Class:     java_net_NetworkInterface
 * Method:    getByName0
 * Signature: (Ljava/lang/String;)Ljava/net/NetworkInterface;
 */
JNIEXPORT jobject JNICALL Java_java_net_NetworkInterface_getByName0
    (JNIEnv *env, jclass cls, jstring name) {

    netif *ifs, *curr;
    jboolean isCopy;
    const char* name_utf = (*env)->GetStringUTFChars(env, name, &isCopy);
    jobject obj = NULL;

    ifs = enumInterfaces(env);
    if (ifs == NULL) {
	return NULL;
    }

    /*
     * Search the list of interface based on name
     */
    curr = ifs;
    while (curr != NULL) {
        if (strcmp(name_utf, curr->name) == 0) {
            break;
        }
        curr = curr->next;
    }

    /* if found create a NetworkInterface */
    if (curr != NULL) {;
        obj = createNetworkInterface(env, curr);
    }

    /* release the UTF string and interface list */
    (*env)->ReleaseStringUTFChars(env, name, name_utf);
    freeif(ifs);

    return obj;
}


/*
 * Class:     java_net_NetworkInterface
 * Method:    getByIndex
 * Signature: (Ljava/lang/String;)Ljava/net/NetworkInterface;
 */
JNIEXPORT jobject JNICALL Java_java_net_NetworkInterface_getByIndex
    (JNIEnv *env, jclass cls, jint index) {

    netif *ifs, *curr;
    jobject obj = NULL;

    if (index <= 0) {
        return NULL;
    }

    ifs = enumInterfaces(env);
    if (ifs == NULL) {
        return NULL;
    }

    /*
     * Search the list of interface based on index
     */
    curr = ifs;
    while (curr != NULL) {
	if (index == curr->index) {
            break;
        }
        curr = curr->next;
    }

    /* if found create a NetworkInterface */
    if (curr != NULL) {;
        obj = createNetworkInterface(env, curr);
    }

    freeif(ifs);
    return obj;
}

/*
 * Class:     java_net_NetworkInterface
 * Method:    getByInetAddress0
 * Signature: (Ljava/net/InetAddress;)Ljava/net/NetworkInterface;
 */
JNIEXPORT jobject JNICALL Java_java_net_NetworkInterface_getByInetAddress0
    (JNIEnv *env, jclass cls, jobject iaObj) {

    netif *ifs, *curr;
    int family = (*env)->GetIntField(env, iaObj, ni_iafamilyID) == IPv4?
        AF_INET : AF_INET6;
    jobject obj = NULL;
    jboolean match = JNI_FALSE;

    ifs = enumInterfaces(env);
    if (ifs == NULL) {
	return NULL;
    }

    curr = ifs;
    while (curr != NULL) {
	netaddr *addrP = curr->addr;

	/*
	 * Iterate through each address on the interface
	 */
	while (addrP != NULL) {

	    if (family == addrP->family) {
		if (family == AF_INET) {
		    int address1 = htonl(((struct sockaddr_in*)addrP->addr)->sin_addr.s_addr);
                    int address2 = (*env)->GetIntField(env, iaObj, ni_iaaddressID);

                    if (address1 == address2) {
			match = JNI_TRUE;
			break;
		    }
		}

#ifdef AF_INET6
		if (family == AF_INET6) {
		    jbyte *bytes = (jbyte *)&(((struct sockaddr_in6*)addrP->addr)->sin6_addr);
		    jbyteArray ipaddress = (*env)->GetObjectField(env, iaObj, ni_ia6ipaddressID);
		    jbyte caddr[16];
		    int i;

		    (*env)->GetByteArrayRegion(env, ipaddress, 0, 16, caddr);
		    i = 0;
		    while (i < 16) {
                        if (caddr[i] != bytes[i]) {
			    break;
			}
			i++;
                    }
		    if (i >= 16) {
			match = JNI_TRUE;
			break;
		    }
		}
#endif

	    }

	    if (match) {
		break;
	    }
	    addrP = addrP->next;
	}

	if (match) {
	    break;
	}
	curr = curr->next;
    }

    /* if found create a NetworkInterface */
    if (match) {;
        obj = createNetworkInterface(env, curr);
    }

    freeif(ifs);
    return obj;
}


/*
 * Class:     java_net_NetworkInterface
 * Method:    getAll
 * Signature: ()[Ljava/net/NetworkInterface;
 */
JNIEXPORT jobjectArray JNICALL Java_java_net_NetworkInterface_getAll
    (JNIEnv *env, jclass cls) {

    netif *ifs, *curr;
    jobjectArray netIFArr;
    jint arr_index, ifCount;

    ifs = enumInterfaces(env);
    if (ifs == NULL) {
        return NULL;
    }

    /* count the interface */
    ifCount = 0;
    curr = ifs;
    while (curr != NULL) { 
	ifCount++;
	curr = curr->next;
    } 

    /* allocate a NetworkInterface array */
    netIFArr = (*env)->NewObjectArray(env, ifCount, cls, NULL);
    if (netIFArr == NULL) {
	freeif(ifs);
        return NULL;
    }

    /*
     * Iterate through the interfaces, create a NetworkInterface instance
     * for each array element and populate the object.
     */
    curr = ifs;
    arr_index = 0;
    while (curr != NULL) {
        jobject netifObj;

        netifObj = createNetworkInterface(env, curr);
        if (netifObj == NULL) {
	    freeif(ifs);
            return NULL;
        }

        /* put the NetworkInterface into the array */
        (*env)->SetObjectArrayElement(env, netIFArr, arr_index++, netifObj);

        curr = curr->next;
    }

    free(ifs);
    return netIFArr;
}

/*
 * Create a NetworkInterface object, populate the name and index, and
 * populate the InetAddress array based on the IP addresses for this
 * interface.
 */
jobject createNetworkInterface(JNIEnv *env, netif *ifs)
{
    jobject netifObj;
    jobject name;
    jobjectArray addrArr;
    /* eliminate compile time warning
       netaddr *addrs;
    */
    jint addr_index, addr_count;
    netaddr *addrP;

    /*
     * Create a NetworkInterface object and populate it
     */
    netifObj = (*env)->NewObject(env, ni_class, ni_ctrID); 
    name = (*env)->NewStringUTF(env, ifs->name);
    if (netifObj == NULL || name == NULL) {
        return NULL;
    }
    (*env)->SetObjectField(env, netifObj, ni_nameID, name);
    (*env)->SetObjectField(env, netifObj, ni_descID, name); 
    (*env)->SetIntField(env, netifObj, ni_indexID, ifs->index);

    /*
     * Count the number of address on this interface
     */
    addr_count = 0;
    addrP = ifs->addr;
    while (addrP != NULL) {
	addr_count++;
  	addrP = addrP->next;
    }

    /*
     * Create the array of InetAddresses
     */
    addrArr = (*env)->NewObjectArray(env, addr_count,  ni_iacls, NULL);
    if (addrArr == NULL) {
        return NULL;
    }

    addrP = ifs->addr;
    addr_index = 0;
    while (addrP != NULL) {
	jobject iaObj = NULL;

	if (addrP->family == AF_INET) {
            iaObj = (*env)->NewObject(env, ni_ia4cls, ni_ia4ctrID);
            if (iaObj) {
                 (*env)->SetIntField(env, iaObj, ni_iaaddressID, 
                     htonl(((struct sockaddr_in*)addrP->addr)->sin_addr.s_addr));
	    }
	}

#ifdef AF_INET6
	if (addrP->family == AF_INET6) {
	    iaObj = (*env)->NewObject(env, ni_ia6cls, ni_ia6ctrID);	
	    if (iaObj) {
		jbyteArray ipaddress = (*env)->NewByteArray(env, 16);
		if (ipaddress == NULL) {
		    return NULL;
		}
		(*env)->SetByteArrayRegion(env, ipaddress, 0, 16,
                    (jbyte *)&(((struct sockaddr_in6*)addrP->addr)->sin6_addr));
		(*env)->SetObjectField(env, iaObj, ni_ia6ipaddressID, ipaddress);
	    } 
	}
#endif

	if (iaObj == NULL) {
	    return NULL;
	}

        (*env)->SetObjectArrayElement(env, addrArr, addr_index++, iaObj);
        addrP = addrP->next;
    }
 
    (*env)->SetObjectField(env, netifObj, ni_addrsID, addrArr);

    /* return the NetworkInterface */
    return netifObj;
}

/* 
 * Enumerates all interfaces
 */
static netif *enumInterfaces(JNIEnv *env) {
    netif *ifs;

    /*
     * Enumerate IPv4 addresses
     */
    ifs = enumIPv4Interfaces(env, NULL);
    if (ifs == NULL) {
	if ((*env)->ExceptionOccurred(env)) {
	    return NULL;
	}
    }

    /*
     * If IPv6 is available then enumerate IPv6 addresses.
     */
#ifdef AF_INET6
    if (ipv6_available()) {
        ifs = enumIPv6Interfaces(env, ifs);

	if ((*env)->ExceptionOccurred(env)) {
	    freeif(ifs);
	    return NULL;
        }
    }
#endif

    return ifs;
}


/*
 * Enumerates and returns all IPv4 interfaces
 */
static netif *enumIPv4Interfaces(JNIEnv *env, netif *ifs) {
    int sock;
    struct ifconf ifc;
    struct ifreq *ifreqP;
    char *buf;
    unsigned i;
    unsigned bufsize;

    sock = JVM_Socket(AF_INET, SOCK_DGRAM, 0);
    if (sock < 0) {
	/*
	 * If EPROTONOSUPPORT is returned it means we don't have
	 * IPv4 support so don't throw an exception.
	 */
	if (errno != EPROTONOSUPPORT) {
	    NET_ThrowByNameWithLastError(env, JNU_JAVANETPKG "SocketException",
					 "Socket creation failed");
	}
	return ifs;
    }

    /* need to do a dummy SIOCGIFCONF to determine the buffer size.
     * SIOCGIFCOUNT doesn't work
     */
    ifc.ifc_buf = NULL;
    ifc.ifc_len = 0;
    if (ioctl(sock, SIOCGIFCONF, (char *)&ifc) < 0) {
        NET_ThrowByNameWithLastError(env , JNU_JAVANETPKG "SocketException",
                         "ioctl SIOCGIFCONF failed");
        close(sock);
        return ifs;
    }

    /* FIXME: SIOCGIFCONF does not seem to be working on darwin. It returns
       with ifc_len == 0. We force bufsize to a bigger size below just as
       a hack so the subsequent SIOCGIFCONF will return something. However,
       even this isn't working so great. It doesn't return any AF_INET
       interfaces, and what we do with data seems to result mostly
       in garbage for the caller of NetworkInterface.getNetworkInterfaces.
    */
    bufsize = ifc.ifc_len;
    bufsize = 3 * sizeof(struct ifreq);


    buf = (char *)malloc(bufsize);
    if (!buf) {
        JNU_ThrowOutOfMemoryError(env, "heap allocation failed");
        (void) close(sock);
        return ifs;
    }
    ifc.ifc_len = bufsize;
    ifc.ifc_buf = buf;
    if (ioctl(sock, SIOCGIFCONF, (char *)&ifc) < 0) {
        NET_ThrowByNameWithLastError(env , JNU_JAVANETPKG "SocketException",
                         "ioctl SIOCGIFCONF failed");
        (void) close(sock);
        (void) free(buf);
        return ifs;
    }

    /*
     * Iterate through each interface
     */
    ifreqP = ifc.ifc_req;
    for (i=0; i<ifc.ifc_len/sizeof (struct ifreq); i++, ifreqP++) {
	int index;
	struct ifreq if2;
		
	memset((char *)&if2, 0, sizeof(if2));
	strcpy(if2.ifr_name, ifreqP->ifr_name);
	
	index = -1;
	
	/*
	 * Add to the list 
	 */
	ifs = addif(env, ifs, ifreqP->ifr_name, index, AF_INET,
		    (struct sockaddr *)&(ifreqP->ifr_addr),
		    sizeof(struct sockaddr_in));
	
	/*
	 * If an exception occurred then free the list
	 */
	if ((*env)->ExceptionOccurred(env)) {
	    close(sock);
	    free(buf);
	    freeif(ifs);
	    return NULL;
	}
    }

    /*
     * Free socket and buffer
     */
    close(sock);
    free(buf);
    return ifs;
}


#if defined(AF_INET6) 

/*
 * Enumerates and returns all IPv4 interfaces
 */
static netif *enumIPv6Interfaces(JNIEnv *env, netif *ifs) {
    int sock;
    struct ifconf ifc;
    struct ifreq *ifreqP;
    char *buf;
    unsigned i;
    unsigned bufsize;
	
    sock = JVM_Socket(AF_INET6, SOCK_DGRAM, 0);
    if (sock < 0) {
	/*
	 * If EPROTONOSUPPORT is returned it means we don't have
	 * IPv4 support so don't throw an exception.
	 */
	if (errno != EPROTONOSUPPORT) {
	    NET_ThrowByNameWithLastError(env, JNU_JAVANETPKG "SocketException",
					 "Socket creation failed");
	}
	return ifs;
    }
	
    /* need to do a dummy SIOCGIFCONF to determine the buffer size.
		* SIOCGIFCOUNT doesn't work
		*/
    ifc.ifc_buf = NULL;
    ifc.ifc_len = 0;
    if (ioctl(sock, SIOCGIFCONF, (char *)&ifc) < 0) {
        NET_ThrowByNameWithLastError(env , JNU_JAVANETPKG "SocketException",
				     "ioctl SIOCGIFCONF failed");
        close(sock);
        return ifs;
    }
    bufsize = ifc.ifc_len;
	
	
    buf = (char *)malloc(bufsize);
    if (!buf) {
        JNU_ThrowOutOfMemoryError(env, "heap allocation failed");
        (void) close(sock);
        return ifs;
    }
    ifc.ifc_len = bufsize;
    ifc.ifc_buf = buf;
    if (ioctl(sock, SIOCGIFCONF, (char *)&ifc) < 0) {
        NET_ThrowByNameWithLastError(env , JNU_JAVANETPKG "SocketException",
				     "ioctl SIOCGIFCONF failed");
        (void) close(sock);
        (void) free(buf);
        return ifs;
    }
	
    /*
     * Iterate through each interface
     */
    ifreqP = ifc.ifc_req;
    for (i=0; i<ifc.ifc_len/sizeof (struct ifreq); i++, ifreqP++) {
	int index;
	struct ifreq if2;
	
	/*
	 * Ignore non-AF_INET6 addresses 
	 */
	if(((struct sockaddr *)&(ifreqP->ifr_addr))->sa_family != AF_INET6)
	    continue;
	
	memset((char *)&if2, 0, sizeof(if2));
	strcpy(if2.ifr_name, ifreqP->ifr_name);
	
	/*
	 * Try to get the interface index
	 * (Not supported on Solaris 2.6 or 7)
	 */
	/*	
	  if (ioctl(sock, SIOCGIFINDEX, (char *)&if2) >= 0) {
	  index = if2.ifr_index;
	  } else {
	*/
	index = -1;
	/*	} */
	
	/*
	 * Add to the list 
	 */
	ifs = addif(env, ifs, ifreqP->ifr_name, index, AF_INET6,
		    (struct sockaddr *)&(ifreqP->ifr_addr),
		    sizeof(struct sockaddr_in6));
	
	/*
	 * If an exception occurred then free the list
	 */
	if ((*env)->ExceptionOccurred(env)) {
	    close(sock);
	    free(buf);
	    freeif(ifs);
	    return NULL;
	}
    }
	
    /*
     * Free socket and buffer
     */
    close(sock);
    free(buf);
    return ifs;
}

#endif

#if 0
/*
 * Enumerates and returns all IPv6 interfaces on Solaris
 */
static netif *enumIPv6Interfaces(JNIEnv *env, netif *ifs) {
    int sock;
    struct lifconf ifc;
    struct lifreq *ifr;
    int n;
    char *buf;
    struct lifnum numifs;
    unsigned bufsize;

    sock = JVM_Socket(AF_INET6, SOCK_DGRAM, 0);
    if (sock < 0) {
        NET_ThrowByNameWithLastError(env , JNU_JAVANETPKG "SocketException",
                         "Failed to create IPv6 socket");
        return ifs;
    }

    /*
     * Get the interface count
     */
    numifs.lifn_family = AF_UNSPEC;
    numifs.lifn_flags = 0;
    if (ioctl(sock, SIOCGLIFNUM, (char *)&numifs) < 0) {
        NET_ThrowByNameWithLastError(env , JNU_JAVANETPKG "SocketException",
                         "ioctl SIOCGLIFNUM failed");
        close(sock);
        return ifs;
    }

    /*
     *  Enumerate the interface configurations
     */
    bufsize = numifs.lifn_count * sizeof (struct lifreq);
    buf = (char *)malloc(bufsize);
    if (!buf) {
        JNU_ThrowOutOfMemoryError(env, "heap allocation failed");
        (void) close(sock);
        return ifs;
    }
    ifc.lifc_family = AF_UNSPEC;
    ifc.lifc_flags = 0;
    ifc.lifc_len = bufsize;
    ifc.lifc_buf = buf;
    if (ioctl(sock, SIOCGLIFCONF, (char *)&ifc) < 0) {
        NET_ThrowByNameWithLastError(env , JNU_JAVANETPKG "SocketException",
                         "ioctl SIOCGLIFCONF failed");
        close(sock);
        free(buf);
        return ifs;
    }

    /*
     * Iterate through each interface
     */
    ifr = ifc.lifc_req;
    for (n=0; n<numifs.lifn_count; n++, ifr++) {
	int index = -1;
	struct lifreq if2;
	
	/*
	 * Ignore non-IPv6 addresses 
	 */
	if (ifr->lifr_addr.ss_family != AF_INET6) {
	    continue;
	}

	/*
	 * Get the index
	 */
	memset((char *)&if2, 0, sizeof(if2));
	strcpy(if2.lifr_name, ifr->lifr_name);
	if (ioctl(sock, SIOCGLIFINDEX, (char *)&if2) >= 0) {
	    index = if2.lifr_index;
	}
	
	/* add to the list */
	ifs = addif(env, ifs, ifr->lifr_name, index, AF_INET6,
		    (struct sockaddr *)&(ifr->lifr_addr), 
		    sizeof(struct sockaddr_in6));

        /*
         * If an exception occurred we return
         */
        if ((*env)->ExceptionOccurred(env)) {
            close(sock);
            free(buf);
            return ifs;
        }

    }

    close(sock);
    free(buf);
    return ifs;

}
#endif

#if 0
//#if defined(__linux__) && defined(AF_INET6)
/*
 * Enumerates and returns all IPv6 interfaces on Linux
 */
static netif *enumIPv6Interfaces(JNIEnv *env, netif *ifs) {
    FILE *f;
    char addr6[40], devname[20];
    char addr6p[8][5];
    int plen, scope, dad_status, if_idx;
    uint8_t ipv6addr[16];

    if ((f = fopen(_PATH_PROCNET_IFINET6, "r")) != NULL) {
        while (fscanf(f, "%4s%4s%4s%4s%4s%4s%4s%4s %02x %02x %02x %02x %20s\n",
                      addr6p[0], addr6p[1], addr6p[2], addr6p[3],
                      addr6p[4], addr6p[5], addr6p[6], addr6p[7],
		      &if_idx, &plen, &scope, &dad_status, devname) != EOF) {
            /* commenting this to eliminate compile time warning.
              struct netif *ifs_ptr = NULL;
              struct netif *last_ptr = NULL;
            */
            struct sockaddr_in6 addr;

	    sprintf(addr6, "%s:%s:%s:%s:%s:%s:%s:%s",
                    addr6p[0], addr6p[1], addr6p[2], addr6p[3],
                    addr6p[4], addr6p[5], addr6p[6], addr6p[7]);
            inet_pton(AF_INET6, addr6, ipv6addr);

	    memset(&addr, 0, sizeof(struct sockaddr_in6));
            memcpy((void*)addr.sin6_addr.s6_addr, (const void*)ipv6addr, 16);

	    ifs = addif(env, ifs, devname, if_idx, AF_INET6,
			(struct sockaddr *)&addr, 
			sizeof(struct sockaddr_in6));
	}
	fclose(f);
    }
    return ifs;
}
#endif

/*
 * Free an interface list (including any attached addresses)
 */
void freeif(netif *ifs) {
    netif *currif = ifs;

    while (currif != NULL) {
	netaddr *addrP = currif->addr;
	while (addrP != NULL) {
	    netaddr *next = addrP->next;
	    free(addrP->addr); 
	    free(addrP);
	    addrP = next;
	}

	free(currif->name);

	ifs = currif->next;
	free(currif);
	currif = ifs;
    }
}

/*
 * Add an interface to the list. If known interface just link
 * a new netaddr onto the list. If new interface create new
 * netif structure.
 */
netif *addif(JNIEnv *env, netif *ifs, char *if_name, int index, int family,
	     struct sockaddr *new_addrP, int new_addrlen) {
    netif *currif = ifs;
    netaddr *addrP;
#ifdef LIFNAMSIZ
    char name[LIFNAMSIZ];
#else
    char name[IFNAMSIZ];
#endif
    char *unit;

    /*
     * If the interface name is a logical interface then we
     * remove the unit number so that we have the physical
     * interface (eg: hme0:1 -> hme0). NetworkInterface
     * currently doesn't have any concept of physical vs.
     * logical interfaces.
     */
    strcpy(name, if_name);
    unit = strchr(name, ':');
    if (unit != NULL) {
	*unit = '\0';
    }

    /*
     * Create and populate the netaddr node. If allocation fails
     * return an un-updated list.
     */
    addrP = (netaddr *)malloc(sizeof(netaddr));
    if (addrP) {
	addrP->addr = (struct sockaddr *)malloc(new_addrlen);
	if (addrP->addr == NULL) {
	    free(addrP);
	    addrP = NULL;
	}
    }
    if (addrP == NULL) {
	JNU_ThrowOutOfMemoryError(env, "heap allocation failed");
	return ifs; /* return untouched list */
    }
    memcpy(addrP->addr, new_addrP, new_addrlen);
    addrP->family = family;

    /*
     * Check if this is a "new" interface. Use the interface
     * name for matching because index isn't supported on
     * Solaris 2.6 & 7.
     */
    while (currif != NULL) {
        if (strcmp(name, currif->name) == 0) {
	    break;
        }
	currif = currif->next;
    }

    /*
     * If "new" then create an netif structure and
     * insert it onto the list.
     */
    if (currif == NULL) {
	currif = (netif *)malloc(sizeof(netif));
	if (currif) {
	    currif->name = strdup(name);
	    if (currif->name == NULL) {
		free(currif);
		currif = NULL;
	    }
	}
	if (currif == NULL) {
	    JNU_ThrowOutOfMemoryError(env, "heap allocation failed");
	    return ifs;
 	}
	currif->index = index;
	currif->addr = NULL;
	currif->next = ifs;
	ifs = currif;
    }

    /*
     * Finally insert the address on the interface
     */
    addrP->next = currif->addr;
    currif->addr = addrP;

    return ifs;
}
