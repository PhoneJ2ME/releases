/*
 * @(#)NetworkInterface.java	1.18 06/10/10
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

package java.net;

import java.net.SocketException;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import sun.security.action.*;
import java.security.AccessController;

/**
 * This class represents a Network Interface made up of a name, 
 * and a list of IP addresses assigned to this interface.
 * It is used to identify the local interface on which a multicast group
 * is joined. 
 *
 * Interfaces are normally known by names such as "le0".
 *
 * @since 1.4
 */
public final class NetworkInterface {
    private String name;
    private String displayName;
    private int index;
    private InetAddress addrs[];

    static {
	AccessController.doPrivileged(new LoadLibraryAction("net"));
	init();
    }

    /**
     * Returns an NetworkInterface object with index set to 0 and name to null.
     * Setting such an interface on a MulticastSocket will cause the 
     * kernel to choose one interface for sending multicast packets.
     *  
     */
    NetworkInterface() {
    }
    
    NetworkInterface(String name, int index, InetAddress[] addrs) {
	this.name = name;
	this.index = index;
	this.addrs = addrs;
    }

    /**
     * Get the name of this network interface.
     *
     * @return the name of this network interface
     */
    public String getName() {
	    return name;
    }

    /**
     * Convenience method to return an Enumeration with all or a
     * subset of the InetAddresses bound to this network interface.
     * <p>
     * If there is a security manager, its <code>checkConnect</code> 
     * method is called for each InetAddress. Only InetAddresses where
     * the <code>checkConnect</code> doesn't throw a SecurityException
     * will be returned in the Enumeration.
     * @return an Enumeration object with all or a subset of the InetAddresses
     * bound to this network interface
     */
    public Enumeration getInetAddresses() {

	class checkedAddresses implements Enumeration {
    
	    private int i=0, count=0;
	    private Object local_addrs[];
    
	    checkedAddresses() {
		local_addrs = new Object[addrs.length];
    	    
		SecurityManager sec = System.getSecurityManager();
		for (int j=0; j<addrs.length; j++) {
		    try {
			if (sec != null) {
			    sec.checkConnect(addrs[j].getHostAddress(), -1);
			}
			local_addrs[count++] = addrs[j];
		    } catch (SecurityException e) { }
		}
    	
	    }
    
	    public Object nextElement() {
		if (i < count) {
		    return local_addrs[i++];
		} else {
		    throw new NoSuchElementException();
		}
	    }
	
	    public boolean hasMoreElements() {
		return (i < count);
	    }
	}
	return new checkedAddresses();

    }

    /**
     * Get the index of this network interface.
     *
     * @return the index of this network interface
     */
    int getIndex() {
	return index;
    }

    /**
     * Get the display name of this network interface.
     * A display name is a human readable String describing the network
     * device.
     *
     * @return the display name of this network interface, 
     *         or null if no display name is available.
     */
    public String getDisplayName() {
	return displayName;
    }
 
    /**
     * Searches for the network interface with the specified name.
     *
     * @param   name 
     *		The name of the network interface.
     *
     * @return  A <tt>NetworkInterface</tt> with the specified name,
     *          or <tt>null</tt> if there is no network interface
     *		with the specified name.
     *
     * @throws	SocketException  
     *	        If an I/O error occurs.
     *
     * @throws  NullPointerException
     *		If the specified name is <tt>null</tt>.
     */
    public static NetworkInterface getByName(String name) throws SocketException {
	if (name == null) 
	    throw new NullPointerException();
	return getByName0(name);
    }

    /**
     * Get a network interface given its index.
     *
     * @param index an integer, the index of the interface
     * @return the NetworkInterface obtained from its index
     * @exception  SocketException  if an I/O error occurs.
     */
    native static NetworkInterface getByIndex(int index) 
	throws SocketException;

    /**
     * Convenience method to search for a network interface that
     * has the specified Internet Protocol (IP) address bound to
     * it.
     * <p>
     * If the specified IP address is bound to multiple network 
     * interfaces it is not defined which network interface is
     * returned.
     *
     * @param   addr
     *		The <tt>InetAddress</tt> to search with.
     *
     * @return  A <tt>NetworkInterface</tt> 
     *          or <tt>null</tt> if there is no network interface
     *          with the specified IP address.
     *
     * @throws  SocketException  
     *          If an I/O error occurs. 
     *
     * @throws  NullPointerException
     *          If the specified address is <tt>null</tt>.
     */
    public static NetworkInterface getByInetAddress(InetAddress addr) throws SocketException {
	if (addr == null)
	    throw new NullPointerException();
	return getByInetAddress0(addr);
    }

    /**
     * Returns all the interfaces on this machine. Returns null if no
     * network interfaces could be found on this machine.
     * 
     * NOTE: can use getNetworkInterfaces()+getInetAddresses() 
     *       to obtain all IP addresses for this node
     *
     * @return an Enumeration of NetworkInterfaces found on this machine
     * @exception  SocketException  if an I/O error occurs.
     */

    public static Enumeration getNetworkInterfaces() 
	throws SocketException {
	final NetworkInterface[] netifs = getAll();

	// specified to return null if no network interfaces
	if (netifs == null) 
	    return null;
	
	return new Enumeration() {
	    private int i = 0;
	    public Object nextElement() {
		if (netifs != null && i < netifs.length) {
		    NetworkInterface netif = netifs[i++];
		    return netif;
		} else {
		    throw new NoSuchElementException();
		}
	    }

	    public boolean hasMoreElements() {
		return (netifs != null && i < netifs.length);
	    }
	};
    }

    private native static NetworkInterface[] getAll() 
	throws SocketException;

    private native static NetworkInterface getByName0(String name) 
	throws SocketException;

    private native static NetworkInterface getByInetAddress0(InetAddress addr) 
	throws SocketException;

    
    /**
     * Compares this object against the specified object.
     * The result is <code>true</code> if and only if the argument is
     * not <code>null</code> and it represents the same NetworkInterface
     * as this object.
     * <p>
     * Two instances of <code>NetworkInterface</code> represent the same 
     * NetworkInterface if both name and addrs are the same for both.
     *
     * @param   obj   the object to compare against.
     * @return  <code>true</code> if the objects are the same;
     *          <code>false</code> otherwise.
     * @see     java.net.InetAddress#getAddress()
     */
    public boolean equals(Object obj) {
	if ((obj == null) || !(obj instanceof NetworkInterface)) {
	    return false;
	}
	NetworkInterface netIF = (NetworkInterface)obj;
	if (name != null ) {
	    if (netIF.getName() != null) {
		if (!name.equals(netIF.getName())) {
		    return false;
		}
	    } else {
		return false;
	    }
	} else {
	    if (netIF.getName() != null) {
		return false;
	    }
	}
	Enumeration newAddrs = netIF.getInetAddresses();
	int i = 0;
	for (i = 0; newAddrs.hasMoreElements();newAddrs.nextElement(), i++);
	if (addrs == null) {
	    if (i != 0) {
		return false;
	    }
	} else {
	    /* 
	     * Compare number of addresses (in the checked subset)
	     */
	    int count = 0;
	    Enumeration e = getInetAddresses();
	    for (; e.hasMoreElements(); count++) {
		e.nextElement();
	    }
	    if (i != count) {
	  	return false;
	    }
    	}
	newAddrs = netIF.getInetAddresses();
	for (; newAddrs.hasMoreElements();) {
	    boolean equal = false;
	    Enumeration thisAddrs = getInetAddresses();
	    InetAddress newAddr = (InetAddress)newAddrs.nextElement();
	    for (; thisAddrs.hasMoreElements();) {
		InetAddress thisAddr = (InetAddress)thisAddrs.nextElement();
		if (thisAddr.equals(newAddr)) {
		    equal = true;
		}
	    }
	    if (!equal) {
		return false;
	    }
	}
	return true;
    }

    public int hashCode() {
	int count = 0;
	if (addrs != null) {
	    for (int i = 0; i < addrs.length; i++) {
		count += addrs[i].hashCode();
	    }
	}
	return count;
    }

    public String toString() {
	String result = "name:";
	result += name == null? "null": name;
	if (displayName != null) {
	    result += " (" + displayName + ")";
	}
	result += " index: "+index+" addresses:\n";
	for (Enumeration e = getInetAddresses(); e.hasMoreElements(); ) {
	    InetAddress addr = (InetAddress)e.nextElement();
	    result += addr+";\n";
	}
	return result;
    }
    private static native void init();

}
