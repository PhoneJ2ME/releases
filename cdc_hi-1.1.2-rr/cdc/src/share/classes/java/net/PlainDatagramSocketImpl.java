/*
 * @(#)PlainDatagramSocketImpl.java	1.30 06/10/10
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

import java.io.FileDescriptor;
import java.io.IOException;

/**
 * Concrete datagram and multicast socket implementation base class.
 * Note: This is not a public class, so that applets cannot call
 * into the implementation directly and hence cannot bypass the
 * security checks present in the DatagramSocket and MulticastSocket
 * classes.
 *
 * @author Pavani Diwanji
 */

class PlainDatagramSocketImpl extends DatagramSocketImpl
{
    /* timeout value for receive() */
    private int timeout = 0;
    private int trafficClass = 0;
    private boolean connected = false;
    private InetAddress connectedAddress = null;
    private int connectedPort = -1;

    /* cached socket options */
    private int multicastInterface = 0;
    private boolean loopbackMode = true;
    private int ttl = -1;

    /**
     * Load net library into runtime.
     */
    static {
	java.security.AccessController.doPrivileged(
		  new sun.security.action.LoadLibraryAction("net"));
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
     * Creates a datagram socket
     */
    protected synchronized void create() throws SocketException {
	fd = new FileDescriptor();
	datagramSocketCreate();
    }

    /**
     * Binds a datagram socket to a local port.
     */
    protected synchronized native void bind(int lport, InetAddress laddr)
        throws SocketException;

    /**
     * Sends a datagram packet. The packet contains the data and the
     * destination address to send the packet to.
     * @param packet to be sent.
     */
    protected native void send(DatagramPacket p) throws IOException;

    /**
     * Connects a datagram socket to a remote destination. This associates the remote
     * address with the local socket so that datagrams may only be sent to this destination
     * and received from this destination.
     * @param address the remote InetAddress to connect to
     * @param port the remote port number
     */
    protected void connect(InetAddress address, int port) throws SocketException {
	connect0(address, port);
	connectedAddress = address;
	connectedPort = port;
	connected = true;
    }

    /**
     * Disconnects a previously connected socket. Does nothing if the socket was
     * not connected already.
     */
    protected void disconnect() {
	disconnect0();
	connected = false;
	connectedAddress = null;
	connectedPort = -1;
    }

    /**
     * Peek at the packet to see who it is from.
     * @param return the address which the packet came from.
     */
    protected synchronized native int peek(InetAddress i) throws IOException;
    protected synchronized native int peekData(DatagramPacket p) throws IOException;
    /**
     * Receive the datagram packet.
     * @param Packet Received.
     */
    protected synchronized native void receive(DatagramPacket p)
        throws IOException;

    /**
     * Set the TTL (time-to-live) option.
     * @param TTL to be set.
     */
    protected native void setTimeToLive(int ttl) throws IOException;

    /**
     * Get the TTL (time-to-live) option.
     */
    protected native int getTimeToLive() throws IOException;

    /**
     * Set the TTL (time-to-live) option.
     * @param TTL to be set.
     protected native void setTTL(byte ttl) throws IOException;
     */

    /**
     * Get the TTL (time-to-live) option.
     protected native byte getTTL() throws IOException;
    */

    /**
     * Join the multicast group.
     * @param multicast address to join.
     */
    protected void join(InetAddress inetaddr) throws IOException {
	join(inetaddr, null);
    }

    /**
     * Leave the multicast group.
     * @param multicast address to leave.
     */
    protected void leave(InetAddress inetaddr) throws IOException {
	leave(inetaddr, null);
    }
    /**
     * Join the multicast group.
     * @param multicast address to join.
     * @param netIf specifies the local interface to receive multicast
     *        datagram packets
     * @throws  IllegalArgumentException if mcastaddr is null or is a
     *          SocketAddress subclass not supported by this socket
     * @since 1.4
     */

    protected void joinGroup(SocketAddress mcastaddr, NetworkInterface netIf)
	throws IOException {
	if (mcastaddr == null || !(mcastaddr instanceof InetSocketAddress))
	    throw new IllegalArgumentException("Unsupported address type");
	join(((InetSocketAddress)mcastaddr).getAddress(), netIf);
    }

    private native void join(InetAddress inetaddr, NetworkInterface netIf)
	throws IOException;

    /**
     * Leave the multicast group.
     * @param multicast address to leave.
     * @param netIf specified the local interface to leave the group at
     * @throws  IllegalArgumentException if mcastaddr is null or is a
     *          SocketAddress subclass not supported by this socket
     * @since 1.4
     */
    protected void leaveGroup(SocketAddress mcastaddr, NetworkInterface netIf)
	throws IOException {
	if (mcastaddr == null || !(mcastaddr instanceof InetSocketAddress))
	    throw new IllegalArgumentException("Unsupported address type");
	leave(((InetSocketAddress)mcastaddr).getAddress(), netIf);
    }

    private native void leave(InetAddress inetaddr, NetworkInterface netIf)
	throws IOException;

    /**
     * Close the socket.
     */
    protected void close() {
	if (fd != null) {
	    datagramSocketClose();
	    fd = null;
	}
    }

    protected void finalize() {
	close();
    }

    /**
     * set a value - since we only support (setting) binary options
     * here, o must be a Boolean
     */

     public void setOption(int optID, Object o) throws SocketException {
         if (fd == null) {
            throw new SocketException("Socket Closed");
         }
	 switch (optID) {
	    /* check type safety b4 going native.  These should never
	     * fail, since only java.Socket* has access to
	     * PlainSocketImpl.setOption().
	     */
	 case SO_TIMEOUT:
	     if (o == null || !(o instanceof Integer)) {
		 throw new SocketException("bad argument for SO_TIMEOUT");
	     }
	     int tmp = ((Integer) o).intValue();
	     if (tmp < 0)
		 throw new IllegalArgumentException("timeout < 0");
	     timeout = tmp;
	     return;
	 case IP_TOS:
	     if (o == null || !(o instanceof Integer)) {
		 throw new SocketException("bad argument for IP_TOS");
	     }
	     trafficClass = ((Integer)o).intValue();
	     break;
	 case SO_REUSEADDR:
	     if (o == null || !(o instanceof Boolean)) {
		 throw new SocketException("bad argument for SO_REUSEADDR");
	     }
	     break;
	 case SO_BROADCAST:
	     if (o == null || !(o instanceof Boolean)) {
		 throw new SocketException("bad argument for SO_BROADCAST");
	     }
	     break;
	 case SO_BINDADDR:
	     throw new SocketException("Cannot re-bind Socket");
	 case SO_RCVBUF:
	 case SO_SNDBUF:
	     if (o == null || !(o instanceof Integer) ||
		 ((Integer)o).intValue() < 0) {
		 throw new SocketException("bad argument for SO_SNDBUF or " +
					   "SO_RCVBUF");
	     }
	     break;
	 case IP_MULTICAST_IF:
	     if (o == null || !(o instanceof InetAddress))
		 throw new SocketException("bad argument for IP_MULTICAST_IF");
	     break;
	 case IP_MULTICAST_IF2:
	     if (o == null || !(o instanceof NetworkInterface))
		 throw new SocketException("bad argument for IP_MULTICAST_IF2");
	     break;
	 case IP_MULTICAST_LOOP:
	     if (o == null || !(o instanceof Boolean))
		 throw new SocketException("bad argument for IP_MULTICAST_LOOP");
	     break;
	 default:
	     throw new SocketException("invalid option: " + optID);
	 }
	 socketSetOption(optID, o);
     }

    /*
     * get option's state - set or not
     */

    public Object getOption(int optID) throws SocketException {
        if (fd == null) {
            throw new SocketException("Socket Closed");
        }

	Object result;

	switch (optID) {
	    case SO_TIMEOUT:
		result = new Integer(timeout);
		break;
	
	    case IP_TOS:
		result = socketGetOption(optID);
		if ( ((Integer)result).intValue() == -1) {
		    result = new Integer(trafficClass);
		}
		break;

	    case SO_BINDADDR:
	    case IP_MULTICAST_IF:
	    case IP_MULTICAST_IF2:
	    case SO_RCVBUF:
	    case SO_SNDBUF:
	    case IP_MULTICAST_LOOP:
	    case SO_REUSEADDR:
	    case SO_BROADCAST:
		result = socketGetOption(optID);
		break;

	    default:
		throw new SocketException("invalid option: " + optID);
  	}

	return result;
    }

    private native void datagramSocketCreate() throws SocketException;
    private native void datagramSocketClose();
    private native void socketSetOption(int opt, Object val)
        throws SocketException;
    private native Object socketGetOption(int opt) throws SocketException;

    private native void connect0(InetAddress address, int port) throws SocketException;
    private native void disconnect0();

    /**
     * Perform class load-time initializations.
     */
    private native static void init();

}

