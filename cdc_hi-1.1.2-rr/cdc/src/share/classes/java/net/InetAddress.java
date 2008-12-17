/*
 * @(#)InetAddress.java	1.89 06/10/10 
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

package java.net;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.Iterator;
import java.util.LinkedList;
import java.security.AccessController;
import java.io.ObjectStreamException;
import sun.security.action.*;
import sun.net.InetAddressCachePolicy;
import sun.misc.Service;
import sun.net.spi.nameservice.*;

/**
 * This class represents an Internet Protocol (IP) address.
 *
 * <p> An IP address is either a 32-bit or 128-bit unsigned number
 * used by IP, a lower-level protocol on which protocols like UDP and
 * TCP are built. The IP address architecture is defined by <a
 * href="http://www.ietf.org/rfc/rfc790.txt"><i>RFC&nbsp;790:
 * Assigned Numbers</i></a>, <a
 * href="http://www.ietf.org/rfc/rfc1918.txt"> <i>RFC&nbsp;1918:
 * Address Allocation for Private Internets</i></a>, <a
 * href="http://www.ietf.org/rfc/rfc2365.txt"><i>RFC&nbsp;2365:
 * Administratively Scoped IP Multicast</i></a>, and <a
 * href="http://www.ietf.org/rfc/rfc2373.txt"><i>RFC&nbsp;2373: IP
 * Version 6 Addressing Architecture</i></a>. An instance of an
 * InetAddress consists of an IP address and possibly its
 * corresponding host name (depending on whether it is constructed
 * with a host name or whether it has already done reverse host name
 * resolution).
 *
 * <h4> Address types </h4>
 *
 * <blockquote><table cellspacing=2 summary="Description of unicast and multicast address types">
 *   <tr><th valign=top><i>unicast</i></th>
 *       <td>An identifier for a single interface. A packet sent to
 *         a unicast address is delivered to the interface identified by
 *         that address.
 *
 *         <p> The Unspecified Address -- Also called anylocal or wildcard 
 *         address. It must never be assigned to any node. It indicates the
 *         absence of an address. One example of its use is as the target of
 *         bind, which allows a server to accept a client connection on any
 *         interface, in case the server host has multiple interfaces.
 *
 *         <p> The <i>unspecified</i> address must not be used as
 *         the destination address of an IP packet.
 *        
 *         <p> The <i>Loopback</i> Addresses -- This is the address
 *         assigned to the loopback interface. Anything sent to this
 *         IP address loops around and becomes IP input on the local
 *         host. This address is often used when testing a
 *         client.</td></tr>
 *   <tr><th valign=top><i>multicast</i></th>
 *       <td>An identifier for a set of interfaces (typically belonging 
 *         to different nodes). A packet sent to a multicast address is
 *         delivered to all interfaces identified by that address.</td></tr>
 * </table></blockquote>
 *
 * <h4> IP address scope </h4>
 *
 * <p> <i>Link-local</i> addresses are designed to be used for addressing
 * on a single link for purposes such as auto-address configuration,
 * neighbor discovery, or when no routers are present. 
 *
 * <p> <i>Site-local</i> addresses are designed to be used for addressing
 * inside of a site without the need for a global prefix.
 *
 * <p> <i>Global</i> addresses are unique across the internet.
 *
 * <h4> Textual representation of IP addresses </h4>
 * 
 * The textual representation of an IP address is address family specific.
 *
 * <p>
 *
 * For IPv4 address format, please refer to <A
 * HREF="Inet4Address.html#format">Inet4Address#format</A>; For IPv6
 * address format, please refer to <A
 * HREF="Inet6Address.html#format">Inet6Address#format</A>.
 *
 * <h4> Host Name Resolution </h4>
 * 
 * Host name-to-IP address <i>resolution</i> is accomplished through
 * the use of a combination of local machine configuration information
 * and network naming services such as the Domain Name System (DNS)
 * and Network Information Service(NIS). The particular naming
 * services(s) being used is by default the local machine configured
 * one. For any host name, its corresponding IP address is returned.
 *
 * <p> <i>Reverse name resolution</i> means that for any IP address,
 * the host associated with the IP address is returned.
 *
 * <p> The InetAddress class provides methods to resolve host names to
 * their IP addresses and vise versa.
 *
 * <h4> InetAddress Caching </h4>
 *
 * The InetAddress class has a cache to store successful as well as
 * unsuccessful host name resolutions. The positive caching is there
 * to guard against DNS spoofing attacks; while the negative caching
 * is used to improve performance.
 *
 * <p> By default, the result of positive host name resolutions are
 * cached forever, because there is no general rule to decide when it
 * is safe to remove cache entries. The result of unsuccessful host
 * name resolution is cached for a very short period of time (10
 * seconds) to improve performance.
 *
 * <p> Under certain circumstances where it can be determined that DNS
 * spoofing attacks are not possible, a Java security property can be
 * set to a different Time-to-live (TTL) value for positive
 * caching. Likewise, a system admin can configure a different
 * negative caching TTL value when needed.
 *
 * <p> Two Java security properties control the TTL values used for
 *  positive and negative host name resolution caching:
 * 
 * <blockquote>
 * <dl>
 * <dt><b>networkaddress.cache.ttl</b> (default: -1)</dt>
 * <dd>Indicates the caching policy for successful name lookups from
 * the name service. The value is specified as as integer to indicate
 * the number of seconds to cache the successful lookup.
 * <p>
 * A value of -1 indicates "cache forever".
 * </dt><p>
 * <p>
 * <dt><b>networkaddress.cache.negative.ttl</b> (default: 10)</dt>
 * <dd>Indicates the caching policy for un-successful name lookups
 * from the name service. The value is specified as as integer to
 * indicate the number of seconds to cache the failure for
 * un-successful lookups.
 * <p>
 * A value of 0 indicates "never cache".
 * A value of -1 indicates "cache forever".
 * </dd>
 * </dl>
 * </blockquote>
 *
 * @author  Chris Warth
 * @version 1.89, 10/10/06
 * @see     java.net.InetAddress#getByAddress(byte[])
 * @see     java.net.InetAddress#getByAddress(java.lang.String, byte[])
 * @see     java.net.InetAddress#getAllByName(java.lang.String)
 * @see     java.net.InetAddress#getByName(java.lang.String)
 * @see     java.net.InetAddress#getLocalHost()
 * @since JDK1.0
 */
public
class InetAddress implements java.io.Serializable {
    /** 
     * Specify the address family: Internet Protocol, Version 4
     * @since 1.4
     */
    static final int IPv4 = 1;

    /** 
     * Specify the address family: Internet Protocol, Version 6
     * @since 1.4
     */
    static final int IPv6 = 2;

    /* Specify address family preference */
    static transient boolean preferIPv6Address = false;

    /**
     * @serial
     */
    String hostName;

    /**
     * Holds a 32-bit IPv4 address.
     *
     * @serial
     */
    int address;

    /**
     * Specifies the address family type, for instance, '1' for IPv4
     * addresses, and '2' for IPv6 addresses.
     *
     * @serial
     */
    int family;

    /* Used to store the name service provider */
    private static NameService nameService = null; 

    /* Used to store the best available hostname */
    private transient String canonicalHostName = null;

    /** use serialVersionUID from JDK 1.0.2 for interoperability */
    private static final long serialVersionUID = 3286316764910316507L;

    /*
     * Load net library into runtime, and perform initializations.
     */
    static {
	preferIPv6Address = 
	    ((Boolean)java.security.AccessController.doPrivileged(
		 new GetBooleanAction("java.net.preferIPv6Addresses"))).booleanValue();
	AccessController.doPrivileged(new LoadLibraryAction("net"));
        /*
         * Wrap the init() method in a privileged block so that it
         * can read system properties. In j2se1.4.2 this is done
         * in the InetAddress OnLoad() function & this is executed
         * by the system class loader.
         */
        java.security.AccessController.doPrivileged(
                      new java.security.PrivilegedAction() {
                          public Object run() 
                          {
                            init();
                            return null;
                          }
                        } );
    }

    /**
     * Constructor for the Socket.accept() method.
     * This creates an empty InetAddress, which is filled in by
     * the accept() method.  This InetAddress, however, is not
     * put in the address cache, since it is not created by name.
     */
    InetAddress() {
    }

    /**
     * Replaces the de-serialized object with an Inet4Address object.
     *
     * @return the alternate object to the de-serialized object.
     *
     * @throws ObjectStreamException if a new object replacing this
     * object could not be created
     */
    private Object readResolve() throws ObjectStreamException {
	// will replace the deserialized 'this' object
	return new Inet4Address(this.hostName, this.address); 
    }

    /**
     * Utility routine to check if the InetAddress is an
     * IP multicast address.
     * @return a <code>boolean</code> indicating if the InetAddress is 
     * an IP multicast address
     * @since   JDK1.1
     */
    public boolean isMulticastAddress() {
	return false;
    }

    /**
     * Utility routine to check if the InetAddress in a wildcard address.
     * @return a <code>boolean</code> indicating if the Inetaddress is
     *         a wildcard address.
     * @since 1.4
     */    
    public boolean isAnyLocalAddress() {
	return false;
    }

    /**
     * Utility routine to check if the InetAddress is a loopback address. 
     *
     * @return a <code>boolean</code> indicating if the InetAddress is 
     * a loopback address; or false otherwise.
     * @since 1.4
     */
    public boolean isLoopbackAddress() {
	return false;
    }

    /**
     * Utility routine to check if the InetAddress is an link local address. 
     *
     * @return a <code>boolean</code> indicating if the InetAddress is 
     * a link local address; or false if address is not a link local unicast address.
     * @since 1.4
     */
    public boolean isLinkLocalAddress() {
	return false;
    }

    /**
     * Utility routine to check if the InetAddress is a site local address. 
     *
     * @return a <code>boolean</code> indicating if the InetAddress is 
     * a site local address; or false if address is not a site local unicast address.
     * @since 1.4
     */
    public boolean isSiteLocalAddress() {
	return false;
    }

    /**
     * Utility routine to check if the multicast address has global scope.
     *
     * @return a <code>boolean</code> indicating if the address has 
     *         is a multicast address of global scope, false if it is not 
     *         of global scope or it is not a multicast address
     * @since 1.4
     */
    public boolean isMCGlobal() {
	return false;
    }

    /**
     * Utility routine to check if the multicast address has node scope.
     *
     * @return a <code>boolean</code> indicating if the address has 
     *         is a multicast address of node-local scope, false if it is not 
     *         of node-local scope or it is not a multicast address
     * @since 1.4
     */
    public boolean isMCNodeLocal() {
	return false;
    }

    /**
     * Utility routine to check if the multicast address has link scope.
     *
     * @return a <code>boolean</code> indicating if the address has 
     *         is a multicast address of link-local scope, false if it is not 
     *         of link-local scope or it is not a multicast address
     * @since 1.4
     */
    public boolean isMCLinkLocal() {
	return false;
    }

    /**
     * Utility routine to check if the multicast address has site scope.
     *
     * @return a <code>boolean</code> indicating if the address has 
     *         is a multicast address of site-local scope, false if it is not 
     *         of site-local scope or it is not a multicast address
     * @since 1.4
     */
    public boolean isMCSiteLocal() {
	return false;
    }

    /**
     * Utility routine to check if the multicast address has organization scope.
     *
     * @return a <code>boolean</code> indicating if the address has 
     *         is a multicast address of organization-local scope, 
     *         false if it is not of organization-local scope 
     *         or it is not a multicast address
     * @since 1.4
     */
    public boolean isMCOrgLocal() {
	return false;
    }

    /**
     * Gets the host name for this IP address.
     *
     * <p>If this InetAddress was created with a host name,
     * this host name will be remembered and returned; 
     * otherwise, a reverse name lookup will be performed
     * and the result will be returned based on the system 
     * configured name lookup service. If a lookup of the name service
     * is required, call 
     * {@link #getCanonicalHostName() getCanonicalHostName}.
     *
     * <p>If there is a security manager, its
     * <code>checkConnect</code> method is first called
     * with the hostname and <code>-1</code> 
     * as its arguments to see if the operation is allowed.
     * If the operation is not allowed, it will return
     * the textual representation of the IP address.
     *
     * @return  the host name for this IP address, or if the operation
     *    is not allowed by the security check, the textual 
     *    representation of the IP address.
     * 
     * @see InetAddress#getCanonicalHostName
     * @see SecurityManager#checkConnect
     */
    public String getHostName() {
	return getHostName(true);
    }

    /**
     * Returns the hostname for this address.
     * If the host is equal to null, then this address refers to any
     * of the local machine's available network addresses.
     * this is package private so SocketPermission can make calls into
     * here without a security check.
     *
     * <p>If there is a security manager, this method first
     * calls its <code>checkConnect</code> method
     * with the hostname and <code>-1</code> 
     * as its arguments to see if the calling code is allowed to know
     * the hostname for this IP address, i.e., to connect to the host.
     * If the operation is not allowed, it will return
     * the textual representation of the IP address.
     * 
     * @return  the host name for this IP address, or if the operation
     *    is not allowed by the security check, the textual 
     *    representation of the IP address.
     * 
     * @param check make security check if true
     * 
     * @see SecurityManager#checkConnect
     */
    String getHostName(boolean check) {
	if (hostName == null) {
	    hostName = InetAddress.getHostFromNameService(this, check);
	}
	return hostName;
    }

    /**
     * Gets the fully qualified domain name for this IP address.
     * Best effort method, meaning we may not be able to return 
     * the FQDN depending on the underlying system configuration.
     *
     * <p>If there is a security manager, this method first
     * calls its <code>checkConnect</code> method
     * with the hostname and <code>-1</code> 
     * as its arguments to see if the calling code is allowed to know
     * the hostname for this IP address, i.e., to connect to the host.
     * If the operation is not allowed, it will return
     * the textual representation of the IP address.
     * 
     * @return  the fully qualified domain name for this IP address, 
     *    or if the operation is not allowed by the security check,
     *    the textual representation of the IP address.
     *
     * @see SecurityManager#checkConnect
     *
     * @since 1.4
     */
    public String getCanonicalHostName() {
	if (canonicalHostName == null) {
	    canonicalHostName = 
		InetAddress.getHostFromNameService(this, true);
	}
	return canonicalHostName;
    }

    /**
     * Returns the hostname for this address.
     *
     * <p>If there is a security manager, this method first
     * calls its <code>checkConnect</code> method
     * with the hostname and <code>-1</code> 
     * as its arguments to see if the calling code is allowed to know
     * the hostname for this IP address, i.e., to connect to the host.
     * If the operation is not allowed, it will return
     * the textual representation of the IP address.
     * 
     * @return  the host name for this IP address, or if the operation
     *    is not allowed by the security check, the textual 
     *    representation of the IP address.
     * 
     * @param check make security check if true
     * 
     * @see SecurityManager#checkConnect
     */
    private static String getHostFromNameService(InetAddress addr, boolean check) {
	String host;
	try {
	    // first lookup the hostname
	    host = nameService.getHostByAddr(addr.getAddress());

	    /* check to see if calling code is allowed to know
	     * the hostname for this IP address, ie, connect to the host
	     */
	    if (check) {
		SecurityManager sec = System.getSecurityManager();
		if (sec != null) {
		    sec.checkConnect(host, -1);
		}
	    }

	    /* now get all the IP addresses for this hostname,
	     * and make sure one of them matches the original IP
	     * address. We do this to try and prevent spoofing.
	     */
	    
	    InetAddress[] arr = InetAddress.getAllByName0(host, check);
	    boolean ok = false;

	    if(arr != null) {
		for(int i = 0; !ok && i < arr.length; i++) {
		    ok = addr.equals(arr[i]);
		}
	    }

	    //NOTE: if it looks a spoof just return the address?
	    if (!ok) {
		host = addr.getHostAddress();
		return host;
	    }

	} catch (SecurityException e) {
	    host = addr.getHostAddress();
	} catch (UnknownHostException e) {
	    host = addr.getHostAddress();
	}
	return host;
    }

    /**
     * Returns the raw IP address of this <code>InetAddress</code>
     * object. The result is in network byte order: the highest order
     * byte of the address is in <code>getAddress()[0]</code>.
     *
     * @return  the raw IP address of this object.
     */
    public byte[] getAddress() {
	return null;
    }

    /**
     * Returns the IP address string in textual presentation.
     *
     * @return  the raw IP address in a string format.
     * @since   JDK1.0.2
     */
    public String getHostAddress() {
	return null;
     }
    
    /**
     * Returns a hashcode for this IP address.
     *
     * @return  a hash code value for this IP address.
     */
    public int hashCode() {
	return -1;
    }
   
    /**
     * Compares this object against the specified object.
     * The result is <code>true</code> if and only if the argument is
     * not <code>null</code> and it represents the same IP address as
     * this object.
     * <p>
     * Two instances of <code>InetAddress</code> represent the same IP
     * address if the length of the byte arrays returned by
     * <code>getAddress</code> is the same for both, and each of the
     * array components is the same for the byte arrays.
     *
     * @param   obj   the object to compare against.
     * @return  <code>true</code> if the objects are the same;
     *          <code>false</code> otherwise.
     * @see     java.net.InetAddress#getAddress()
     */
    public boolean equals(Object obj) {
	return false;
    }

    /**
     * Converts this IP address to a <code>String</code>. The 
     * string returned is of the form: hostname / literal IP 
     * address.
     *
     * If the host name is unresolved, no reverse name service loopup
     * is performed. The hostname part will be represented by an empty string.
     *
     * @return  a string representation of this IP address.
     */
    public String toString() {
	return ((hostName != null) ? hostName : "") 
	    + "/" + getHostAddress();
    }

    /*
     * Cached addresses - our own litle nis, not!
     */
    private static Cache addressCache =
        new Cache(InetAddressCachePolicy.get());

    private static Cache negativeCache =
        new Cache(InetAddressCachePolicy.getNegative());

    private static boolean addressCacheInit = false;

    static InetAddress[]    unknown_array; // put THIS in cache

    static InetAddressImpl  impl;

    private static HashMap          lookupTable = new HashMap();

    /**
     * Represents a cache entry 
     */
    static final class CacheEntry {

        CacheEntry(Object address, long expiration) {
            this.address = address;
            this.expiration = expiration;
        }

        Object address;
        long expiration;
    }

    /**
     * A cache that manages entries based on a policy specified
     * at creation time.
     */
    static final class Cache {
	private int policy;
	private LinkedHashMap cache; 

	/**
	 * Create cache with specific policy 
	 */
	public Cache(int policy) {
	    this.policy = policy;
	    cache = new LinkedHashMap();
	}

	/**
	 * Add an entry to the cache. If there's already an
	 * entry then for this host then the entry will be 
	 * replaced.
	 */
	public Cache put(String host, Object address) {
	    if (policy == InetAddressCachePolicy.NEVER) {
                return this;
	    }

	    // purge any expired entries

	    if (policy != InetAddressCachePolicy.FOREVER) {

		// As we iterate in insertion order we can
		// terminate when a non-expired entry is found.		
                LinkedList expired = new LinkedList();
                Iterator i = cache.keySet().iterator();
		long now = System.currentTimeMillis();
                while (i.hasNext()) {
                    String key = (String)i.next();
                    CacheEntry entry = (CacheEntry)cache.get(key);

                    if (entry.expiration >= 0 && entry.expiration < now) {
                        expired.add(key);
                    } else {
                        break;
                    }
                }

                i = expired.iterator();
                while (i.hasNext()) {
                    cache.remove(i.next());
		}
            }

	    // create new entry and add it to the cache
	    // -- as a HashMap replaces existing entries we
	    //    don't need to explicitly check if there is 
	    //    already an entry for this host.
	    long expiration;
	    if (policy == InetAddressCachePolicy.FOREVER) {
		expiration = -1;
	    } else {
		expiration = System.currentTimeMillis() + (policy * 1000);
	    }
	    CacheEntry entry = new CacheEntry(address, expiration);
	    cache.put(host, entry);
	    return this;
	}

	/**
	 * Query the cache for the specific host. If found then
	 * return its CacheEntry, or null if not found.
	 */
	public CacheEntry get(String host) {
	    if (policy == InetAddressCachePolicy.NEVER) {
		return null;
	    }
	    CacheEntry entry = (CacheEntry)cache.get(host);

	    // check if entry has expired
	    if (entry != null && policy != InetAddressCachePolicy.FOREVER) {
		if (entry.expiration >= 0 && 
		    entry.expiration < System.currentTimeMillis()) {
		    cache.remove(host);
		    entry = null;
		}
	    }

	    return entry;
	}

	boolean containsDomainMatch(String domain, InetAddress ia) {
	    Iterator i = cache.keySet().iterator();
	    long now = System.currentTimeMillis();
	    while (i.hasNext()) {
		String key = (String)i.next();
		CacheEntry entry = (CacheEntry)cache.get(key);

		if (entry.expiration >= 0 && entry.expiration < now) {
		    // expired? ignore.
		    continue;
		}

		InetAddress[] addrs = (InetAddress[])entry.address;
		if (addrs.length == 1) {
		    // Optimize for common case
		    if (ia.equals(addrs[0]) &&
			SocketPermission.checkDomain(key, domain))
		    {
			return true;
		    }
		} else {
		    if (SocketPermission.checkDomain(key, domain)) {
			for (int j = 0; j < addrs.length; ++j) {
			    if (ia.equals(addrs[j])) {
				return true;
			    }
			}
		    }
		}
	    }
	    return false;
	}

    }

    /*
     * Initialize cache and insert anyLocalAddress into the
     * unknown array with no expiry.
     */
    private static void cacheInitIfNeeded() {
	// Uncomment this line if assertions are always
	//    turned on for libraries
        //assert Thread.holdsLock(addressCache);
        if (addressCacheInit) {
            return;
        }
        unknown_array = new InetAddress[1];
        unknown_array[0] = impl.anyLocalAddress();

	addressCache.put(impl.anyLocalAddress().getHostName(), 
	                 unknown_array);

        addressCacheInit = true;
    }

    /*
     * Cache the given hostname and address.
     */
    private static void cacheAddress(String hostname, Object address,
				     boolean success) {
        hostname = hostname.toLowerCase();
	synchronized (addressCache) {
	    cacheInitIfNeeded();
	    if (success) {
		addressCache.put(hostname, address);
	    } else {
		negativeCache.put(hostname, address);
	    }
	}
    }

    /*
     * Lookup hostname in cache (positive & negative cache). If
     * found return address, null if not found.
     */
    private static Object getCachedAddress(String hostname) {
        hostname = hostname.toLowerCase();

	// search both positive & negative caches 

	synchronized (addressCache) {
	    CacheEntry entry;

	    cacheInitIfNeeded();

	    entry = (CacheEntry)addressCache.get(hostname);
	    if (entry == null) {
		entry = (CacheEntry)negativeCache.get(hostname);
	    }

	    if (entry != null) {
	        return entry.address;
	    }
 	}

	// not found
	return null;
    }

    static boolean containsDomainMatch(String domain, InetAddress i) {
	synchronized (addressCache) {
	    return addressCache.containsDomainMatch(domain, i);
	}
    }

    static {
  	// create the impl
	impl = (new InetAddressImplFactory()).create();

	// get name service if provided and requested
	String provider = null;;
	String propPrefix = "sun.net.spi.nameservice.provider.";
	int n = 1;
	    while (nameService == null) {
		provider 
		    = (String)AccessController.doPrivileged(
			new GetPropertyAction(propPrefix+n, "default"));
		n++;
		if (provider.equals("default")) {
		    // initialize the default name service
		    nameService = new NameService() {
			public byte[][] lookupAllHostAddr(String host) 
			    throws UnknownHostException {
			    return impl.lookupAllHostAddr(host);
			}
			public String getHostByAddr(byte[] addr) 
			    throws UnknownHostException {
			    return impl.getHostByAddr(addr);
			}
		    };
		    break;
		}

		final String providerName = provider;

		try {
		    java.security.AccessController.doPrivileged(
			new java.security.PrivilegedExceptionAction() {
			    public Object run() {
				Iterator itr
		    		    = Service.providers(NameServiceDescriptor.class);
				while (itr.hasNext()) {
		    		    NameServiceDescriptor nsd 
					= (NameServiceDescriptor)itr.next();
		    		    if (providerName.
				        equalsIgnoreCase(nsd.getType()+","
			       		    +nsd.getProviderName())) {
					try {
			    	    	    nameService = nsd.createNameService();
			    	    	    break;
					} catch (Exception e) {
					    e.printStackTrace();
			    	    	    System.err.println(
						"Cannot create name service:"
					         +providerName+": " + e);
					}
		    		    }
				} /* while */
			        return null;
			}
		    });
		} catch (java.security.PrivilegedActionException e) {
		}

	    }
    }
    
    /**
     * Create an InetAddress based on the provided host name and IP address
     * No name service is checked for the validity of the address. 
     *
     * <p> The host name can either be a machine name, such as
     * "<code>java.sun.com</code>", or a textual representation of its IP
     * address.
     *
     * <p> For <code>host</code> specified in literal IPv6 address,
     * either the form defined in RFC 2732 or the literal IPv6 address
     * format defined in RFC 2373 is accepted.
     *
     * <p> If addr specifies an IPv4 address an instance of Inet4Address 
     * will be returned; otherwise, an instance of Inet6Address 
     * will be returned.
     *
     * <p> IPv4 address byte array must be 4 bytes long and IPv6 byte array 
     * must be 16 bytes long
     *
     * @param host the specified host
     * @param addr the raw IP address in network byte order
     * @return  an InetAddress object created from the raw IP address.
     * @exception  UnknownHostException  if IP address is of illegal length
     * @since 1.4
     */
    public static InetAddress getByAddress(String host, byte[] addr) 
	throws UnknownHostException {
	if (host != null && host.length() > 0 && host.charAt(0) == '[') {
	    if (host.charAt(host.length()-1) == ']') {
		host = host.substring(1, host.length() -1);
	    }
	}
	if (addr != null) {
	    if (addr.length == Inet4Address.INADDRSZ) {
		return new Inet4Address(host, addr);
	    } else if (addr.length == Inet6Address.INADDRSZ) {
		byte[] newAddr 
		    = Inet6Address.convertFromIPv4MappedAddress(addr);
		if (newAddr != null) {
		    return new Inet4Address(host, newAddr);
		} else {
		    return new Inet6Address(host, addr);
		}
	    } 
	} 
	throw new UnknownHostException("addr is of illegal length");
    }

    /**
     * Determines the IP address of a host, given the host's name.
     *
     * <p> The host name can either be a machine name, such as
     * "<code>java.sun.com</code>", or a textual representation of its
     * IP address. If a literal IP address is supplied, only the
     * validity of the address format is checked.
     *
     * <p> For <code>host</code> specified in literal IPv6 address,
     * either the form defined in RFC 2732 or the literal IPv6 address
     * format defined in RFC 2373 is accepted.
     *
     * <p> If the host is <tt>null</tt> then an <tt>InetAddress</tt>
     * representing an address of the loopback interface is returned.
     * See <a href="http://www.ietf.org/rfc/rfc3330.txt">RFC&nbsp;3330</a>
     * section&nbsp;2 and <a href="http://www.ietf.org/rfc/rfc2373.txt">RFC&nbsp;2373</a>
     * section&nbsp;2.5.3. </p>
     *
     * @param      host   the specified host, or <code>null</code>.
     * @return     an IP address for the given host name.
     * @exception  UnknownHostException  if no IP address for the
     *               <code>host</code> could be found.
     * @exception  SecurityException if a security manager exists
     *             and its checkConnect method doesn't allow the operation
     */
    public static InetAddress getByName(String host)
	throws UnknownHostException {
	return InetAddress.getAllByName(host)[0];
    }

    /**
     * Given the name of a host, returns an array of its IP addresses,
     * based on the configured name service on the system.
     * 
     * <p> The host name can either be a machine name, such as
     * "<code>java.sun.com</code>", or a textual representation of its IP
     * address. If a literal IP address is supplied, only the
     * validity of the address format is checked.
     *
     * <p> For <code>host</code> specified in literal IPv6 address,
     * either the form defined in RFC 2732 or the literal IPv6 address
     * format defined in RFC 2373 is accepted.
     *
     * <p> If the host is <tt>null</tt> then an <tt>InetAddress</tt>
     * representing an address of the loopback interface is returned.
     * See <a href="http://www.ietf.org/rfc/rfc3330.txt">RFC&nbsp;3330</a>
     * section&nbsp;2 and <a href="http://www.ietf.org/rfc/rfc2373.txt">RFC&nbsp;2373</a>
     * section&nbsp;2.5.3. </p>
     *
     * <p> If there is a security manager and <code>host</code> is not 
     * null and <code>host.length() </code> is not equal to zero, the
     * security manager's
     * <code>checkConnect</code> method is called
     * with the hostname and <code>-1</code> 
     * as its arguments to see if the operation is allowed.
     *
     * @param      host   the name of the host, or <code>null</code>.
     * @return     an array of all the IP addresses for a given host name.
     * 
     * @exception  UnknownHostException  if no IP address for the
     *               <code>host</code> could be found.
     * @exception  SecurityException  if a security manager exists and its  
     *               <code>checkConnect</code> method doesn't allow the operation.
     * 
     * @see SecurityManager#checkConnect
     */
    public static InetAddress[] getAllByName(String host)
	throws UnknownHostException {

	if (host == null || host.length() == 0) {
	    InetAddress[] ret = new InetAddress[1];
	    ret[0] = impl.loopbackAddress();
	    return ret;
	}
	
	boolean ipv6Expected = false;
	if (host.charAt(0) == '[') {
	    // This is supposed to be an IPv6 litteral
	    if (host.length() > 2 && host.charAt(host.length()-1) == ']') {
		host = host.substring(1, host.length() -1);
		ipv6Expected = true;
	    } else {
		// This was supposed to be a IPv6 address, but it's not!
		throw new UnknownHostException(host);
	    }
	}

	// if host is an IP address, we won't do further lookup
	if (Character.digit(host.charAt(0), 16) != -1 
	    || (host.charAt(0) == ':')) {
	    byte[] addr = null;
	    // see if it is IPv4 address
	    addr = Inet4Address.textToNumericFormat(host);
	    if (addr == null) {
		// see if it is IPv6 address
		addr = Inet6Address.textToNumericFormat(host);
	    } else if (ipv6Expected) {
		// Means an IPv4 litteral between brackets!
		throw new UnknownHostException("["+host+"]");
	    }
	    InetAddress[] ret = new InetAddress[1];
	    if(addr != null) {
		if (addr.length == Inet4Address.INADDRSZ) {
		    ret[0] = new Inet4Address(null, addr);
		} else {
		    ret[0] = new Inet6Address(null, addr);
		}
		return ret;
	    }
	    } else if (ipv6Expected) {
		// We were expecting an IPv6 Litteral, but got something else
		throw new UnknownHostException("["+host+"]");
	    }
	return getAllByName0(host);
    }

    private static InetAddress[] getAllByName0 (String host)
	throws UnknownHostException
    {
	return getAllByName0(host, true);
    }

    /**
     * package private so SocketPermission can call it
     */
    static InetAddress[] getAllByName0 (String host, boolean check)
	throws UnknownHostException  {
	/* If it gets here it is presumed to be a hostname */
	/* Cache.get can return: null, unknownAddress, or InetAddress[] */
        Object obj = null;
	Object objcopy = null;

	/* make sure the connection to the host is allowed, before we
	 * give out a hostname
	 */
	if (check) {
	    SecurityManager security = System.getSecurityManager();
	    if (security != null) {
		security.checkConnect(host, -1);
	    }
	}

	obj = getCachedAddress(host);

	/* If no entry in cache, then do the host lookup */
	if (obj == null) {
	    try {
	        obj = getAddressFromNameService(host);
	    } catch (UnknownHostException uhe) {
		throw new UnknownHostException(host + ": " + uhe.getMessage());
	    }
	}

	if (obj == unknown_array) 
	    throw new UnknownHostException(host);

	/* Make a copy of the InetAddress array */
	objcopy = ((InetAddress [])obj).clone();

	return (InetAddress [])objcopy;
    }

    private static Object getAddressFromNameService(String host) 
	throws UnknownHostException 
    {
	Object obj = null;
	boolean success = false;

	// Check whether the host is in the lookupTable.
	// 1) If the host isn't in the lookupTable when
	//    checkLookupTable() is called, checkLookupTable()
	//    would add the host in the lookupTable and
	//    return null. So we will do the lookup.
	// 2) If the host is in the lookupTable when
	//    checkLookupTable() is called, the current thread
	//    would be blocked until the host is removed
	//    from the lookupTable. Then this thread
	//    should try to look up the addressCache.
	//     i) if it found the address in the
	//        addressCache, checkLookupTable()  would
	//        return the address.
	//     ii) if it didn't find the address in the
	//         addressCache for any reason,
	//         it should add the host in the
	//         lookupTable and return null so the
	//         following code would do  a lookup itself.
	if ((obj = checkLookupTable(host)) == null) {
	    // This is the first thread which looks up the address 
	    // this host or the cache entry for this host has been
	    // expired so this thread should do the lookup.
	    try {
		/*
		 * Do not put the call to lookup() inside the
		 * constructor.  if you do you will still be
		 * allocating space when the lookup fails.
		 */
		byte[][] byte_array;
		byte_array = nameService.lookupAllHostAddr(host);
		InetAddress[] addr_array =
		    new InetAddress[byte_array.length];

		for (int i = 0; i < byte_array.length; i++) {
		    byte addr[] = byte_array[i];
		    if (addr.length == Inet4Address.INADDRSZ) {
			addr_array[i] = new Inet4Address(host, addr);
		    } else {
			addr_array[i] = new Inet6Address(host, addr);
		    }
		}
		obj = addr_array;
		success = true;
	    } catch (UnknownHostException uhe) {
		obj  = unknown_array; 
		success = false;
		throw uhe;
	    } finally {
		try {
		    // Cache the address.
		    cacheAddress(host, obj, success);
		} finally {
		    // Delete the host from the lookupTable, and
		    // notify all threads waiting for the monitor
		    // for lookupTable.
		    updateLookupTable(host);
		}
	    }
	}

	return obj;
    }
	
		
    private static Object checkLookupTable(String host) {
	// make sure obj  is null.
	Object obj = null;
	
	synchronized (lookupTable) {
	    // If the host isn't in the lookupTable, add it in the
	    // lookuptable and return null. The caller should do
	    // the lookup.
	    if (lookupTable.containsKey(host) == false) {
		lookupTable.put(host, null);
		return obj;
	    }

	    // If the host is in the lookupTable, it means that another
	    // thread is trying to look up the address of this host.
	    // This thread should wait.
	    while (lookupTable.containsKey(host)) {
		try {
		    lookupTable.wait();
		} catch (InterruptedException e) {
		}
	    }
	}

	// The other thread has finished looking up the address of
	// the host. This thread should retry to get the address
	// from the addressCache. If it doesn't get the address from
	// the cache,  it will try to look up the address itself.
	obj = getCachedAddress(host);
	if (obj == null) {
	    synchronized (lookupTable) {
		lookupTable.put(host, null);
	    }
	}
	 
	return obj;
    }

    private static void updateLookupTable(String host) {
	synchronized (lookupTable) {
	    lookupTable.remove(host);
	    lookupTable.notifyAll();
	}
    }

    /**
     * Returns an <code>InetAddress</code> object given the raw IP address . 
     * The argument is in network byte order: the highest order
     * byte of the address is in <code>getAddress()[0]</code>.
     *
     * <p> This method doesn't block, i.e. no reverse name service lookup
     * is performed.
     *
     * <p> IPv4 address byte array must be 4 bytes long and IPv6 byte array 
     * must be 16 bytes long
     *
     * @param addr the raw IP address in network byte order
     * @return  an InetAddress object created from the raw IP address.
     * @exception  UnknownHostException  if IP address is of illegal length
     * @since 1.4
     */
    public static InetAddress getByAddress(byte[] addr) 
	throws UnknownHostException {
	return getByAddress(null, addr);
    }

    /**
     * Returns the local host.
     *
     * <p>If there is a security manager, its
     * <code>checkConnect</code> method is called
     * with the local host name and <code>-1</code> 
     * as its arguments to see if the operation is allowed. 
     * If the operation is not allowed, an InetAddress representing
     * the loopback address is returned.
     *
     * @return     the IP address of the local host.
     * 
     * @exception  UnknownHostException  if no IP address for the
     *               <code>host</code> could be found.
     * 
     * @see SecurityManager#checkConnect
     */
    public static InetAddress getLocalHost() throws UnknownHostException {

	SecurityManager security = System.getSecurityManager();
	try {
	    String local = impl.getLocalHostName();

	    if (security != null) {
		security.checkConnect(local, -1);
	    }
	    // we are calling getAddressFromNameService directly
	    // to avoid getting localHost from cache 

	    InetAddress[] localAddrs;
	    try {
		localAddrs =
		    (InetAddress[]) InetAddress.getAddressFromNameService(local);
	    } catch (UnknownHostException uhe) {
		throw new UnknownHostException(local + ": " + uhe.getMessage());
	    }
	    return localAddrs[0];
	} catch (java.lang.SecurityException e) {
	    return impl.loopbackAddress();
	}
    }


    /**
     * Perform class load-time initializations.
     */
    private static native void init();


    /*
     * Returns the InetAddress representing anyLocalAddress
     * (typically 0.0.0.0 or ::0)
     */
    static InetAddress anyLocalAddress() {
        return impl.anyLocalAddress();
    }

    /*
     * Load and instantiate an underlying impl class
     */
    static Object loadImpl(String implName) {
	Object impl;

        /*
         * Property "impl.prefix" will be prepended to the classname
         * of the implementation object we instantiate, to which we
         * delegate the real work (like native methods).  This
         * property can vary across implementations of the java.
         * classes.  The default is an empty String "".
         */
        String prefix = (String)AccessController.doPrivileged(
                      new GetPropertyAction("impl.prefix", ""));
        impl = null;
        try {
            impl = Class.forName("java.net." + prefix + implName).newInstance();
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found: java.net." + prefix +
                               implName + ":\ncheck impl.prefix property " +
                               "in your properties file.");
        } catch (InstantiationException e) {
            System.err.println("Could not instantiate: java.net." + prefix +
                               implName + ":\ncheck impl.prefix property " +
                               "in your properties file.");
        } catch (IllegalAccessException e) {
            System.err.println("Cannot access class: java.net." + prefix +
                               implName + ":\ncheck impl.prefix property " +
                               "in your properties file.");
        }

        if (impl == null) {
            try {
                impl = Class.forName(implName).newInstance();
            } catch (Exception e) {
                throw new Error("System property impl.prefix incorrect");
            }
        }

	return impl;
    }

    /* Check java.net.preferIPv4Stack property. Used by
       init_IPv6Available() */
    private static boolean preferIPv4Stack() {
        Boolean res = (Boolean) java.security.AccessController.doPrivileged(
            new sun.security.action.GetBooleanAction(
                "java.net.preferIPv4Stack"));
        return res.booleanValue();
    }
}

/*
 * Simple factory to create the impl
 */
class InetAddressImplFactory {

    static InetAddressImpl create() {
	Object o;
	if (isIPv6Supported()) {
	    o = InetAddress.loadImpl("Inet6AddressImpl");
	} else {
	    o = InetAddress.loadImpl("Inet4AddressImpl");
	}
	return (InetAddressImpl)o;
    }

    private static native boolean isIPv6Supported();
}

