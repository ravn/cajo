package gnu.cajo.utils;

import gnu.cajo.invoke.*;
import java.io.*;
import java.net.*;
import java.rmi.registry.*;

/*
 * Multicast Announcement Class
 *
 * For issues or suggestions mailto:cajo@dev.java.net
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, at version 2.1 of the license, or any
 * later version.  The license differs from the GNU General Public License
 * (GPL) to allow this library to be used in proprietary applications. The
 * standard GPL would forbid this.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * To receive a copy of the GNU General Public License visit their website at
 * http://www.gnu.org or via snail mail at Free Software Foundation Inc.,
 * 59 Temple Place Suite 330, Boston MA 02111-1307 USA
 */

/**
 * This class can listen for UDP multicasts over the network, as well
 * as to send out UDP announcements.  The mechanism is <i>rigged</i> to send a
 * reference to a remote object as a zipped MarshalledObject (zedmob).  It also
 * allows a listening item to receive announced item referencess via a callback
 * mechanism. A single VM can use as many Multicast objects as it wishes.<p>
 * <i>Note:</i> this class requires that the network routers be configured
 * to pass IP multicast packets, at least for the multicast address used.  If
 * not, the packets will will only exist within the subnet of origination.
 *
 * @version 1.0, 01-Nov-99 Initial release
 * @author John Catherino
 */
public final class Multicast implements Runnable {
   private static Remote proxy;
   private static Registry registry;
   private static Multicast mcast;
   private final InetAddress host;
   private Invoke callback;
   private Thread thread;
   /**
    * A reference to the address on which this object is operating. It is
    * referenced by the called listener, and is valid for the duration of the
    * object's existence.
    */
   public final String address;
   /**
    * A reference to the port on which this object is operating. It is
    * referenced by the called listener, and is valid for the duration of the
    * object's existence.
    */
   public final int port;
   /**
    * A reference to the address of the calling VM, when the object is
    * listening. It is referenced by the called listener, and should be
    * considered valid for the duration of the invocation only.
    */
   public InetAddress iaddr;
   /**
    * A reference to a received item, when the object is listening.  It is
    * referenced by the called listener, and should be considered valid for
    * the duration of the invocation only.
    */
   public Invoke item;
   /**
    * The default constructor sets the internal fields to default values which
    * should be sufficient for most purposes. The multicast socket address will
    * be set to 244.0.1.84, which is the one officially registered with IANA
    * for Jini announcements.  The UDP port number on which this object will
    * announce and listen is set to 1099, which is a play on the default TCP
    * port number of Sun's rmiregistry.  It listens on the same network
    * interface being used for the server's RMI communication.
    * @throws java.net.UnknownHostException If the default network interface
    * could not be resolved, <i>not very likely</i>.
    */
   public Multicast() throws UnknownHostException { this("224.0.1.84", 1099); }
   /**
    * The full constructor allows creation of Multicast objects on any
    * appropriate address, and port number. It uses the same network interface
    * being used for the server's RMI communication.
    * @param address The multicast socket domain name, or address, on which
    * this object will listen.  It can be any address in the range 224.0.0.1
    * through 239.255.255.255.
    * @param port The UDP port number on which this object will announce and
    * listen, its value can be 0 - 65535. It is completely independent of all
    * TCP port numbers. Application specific meaning could be assigned to port
    * numbers, to identify broadcast types.
    * @throws java.net.UnknownHostException If the specified host address
    * could not be resolved, or is invalid.
    */
   public Multicast(String address, int port)
      throws UnknownHostException {
      this.host = InetAddress.getByName(Remote.getServerHost());
      this.address = address;
      this.port = port;
   }
   /**
    * This method is used to make UDP announcements on the network.
    * @param item The remote item reference to be sent in the announcement
    * packet.
    * @param ttl The time-to-live of the broadcast packet. This roughly
    * specifies how many multicast enabled routers will pass this packet before
    * automatically discarding it. For example 16, should cover a medium sized
    * LAN. The maximum value is 255, which could theoretically cover the globe,
    * that is in 1999. A value of 1 confines the packet to its immediate
    * subnet.
    * @throws IOException If a datagram socket could not be created, or the
    * packet could not be sent.
    */
   public void announce(Remote item, int ttl) throws IOException {
      InetAddress group = InetAddress.getByName(address);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      item.zedmob(baos);
      byte packet[] = baos.toByteArray();
      baos.close();
      MulticastSocket ms = new MulticastSocket();
      try {
         ms.setInterface(host);
         ms.setTimeToLive(ttl);
         ms.send(new DatagramPacket(packet, packet.length, group, port));
      } finally { ms.close(); }
   }
   /**
    * This method starts a thead to listen on the construction {@link #address
    * address} and {@link #port port}. The listening item will be called on
    * its public multicast method, with a reference to the calling Multicast
    * object.  This is to allow the possibility for a single listener, to
    * monitor multiple multicast objects. If a listener is used to monitor
    * multiple multicast objects, it may be invoked reentrantly, otherwise it
    * cannot. Listening will continue until the callback item's multicast
    * method retruns a non-null value.  If it does, this method would havt to
    * be called again to restart listening.
    * @param callback An item, presumably local to this VM, which is to receive
    * notifications about announcements.
    * @throws IllegalArgumentException If the object is actively listening, at
    * the time of the invocation.
    */
   public void listen(Invoke callback) {
      if (thread == null) {
         this.callback = callback;
         thread = new Thread(this);
         thread.start();
      } else throw new IllegalArgumentException("Already listening");
   }
   /**
    * The monitor thread, it listens for multicasts.  It will sleep until
    * the arrival of a message.  The packet will be reconstituted into a
    * remote item reference, from its zedmob encapsulation.  The item reference
    * will be saved into the public item member variable, also the calling
    * VM's address will be extracted into the public address member variable.
    * The listener will be called next with a String of "multicast" and a
    * reference to this object. The multicast reference is used to access its
    * public member variables; the remote announcer's reference and IP address,
    * as well as the multicast address and port on which it was received.  The
    * second two members are of interest in the case where the same object is
    * listening
    * on multiple multicast objects. If the invocation returns null, the
    * multicast listening will continue, otherwise it will be stopped. Once
    * stopped it can be restarted by the application as necessary, by invoking
    * the {@link #listen listen} method again.
    */
   public void run() {
      try {
         MulticastSocket ms = new MulticastSocket(port);
         ms.setInterface(host);
         ms.joinGroup(InetAddress.getByName(address));
         DatagramPacket dp = new DatagramPacket(new byte[0xFF00], 0xFF00);
         while(!thread.isInterrupted()) try {
            ms.receive(dp);
            ByteArrayInputStream bais = new ByteArrayInputStream(dp.getData());
            try {
               item = Remote.zedmob(bais);
               iaddr = dp.getAddress();
               if (callback.invoke("multicast", this) != null) break;
            } catch(Exception x) {}
            finally { bais.close(); }
         } catch(Exception x) { x.printStackTrace(System.err); }
         ms.close();
         ms = null;
         thread = null;
      } catch(IOException x) { x.printStackTrace(System.err); }
   }
   /**
    * The application method loads a zipped marshalled object (zedmob) to a
    * proxy from a URL, or a file, and allows it run in this virtual machine.
    * It will load an RMISecurityManager to protect the hosting
    * machine from potentially or accidentally dangerous proxies, if not
    * prohibited by the user at startup. It uses the {@link
    * gnu.cajo.invoke.Remote#getItem getitem} method of the {@link
    * gnu.cajo.invoke.Remote Remote} class to load the item. Following loading,
    * it will also create an rmiregistry, and bind a remote reference to it
    * under the name "main". This can allow remote clients to connect to, and
    * interact with, the item. It will announce its startup on a default
    * Multicast object, and then begin listening on it for further
    * announcements, which will be  passed to the loaded proxy item. It can
    * be configured using the following arguments, all arguments subsequent to
    * the ones specified in the command line can be omitted:<br><ul>
    * <li> args[0] The optional URL where to get the item: file:// http://
    * ftp:// ..., /path/name <serialized>, path/name <class>, or alternatively;
    * //[host][:port]/[name], where the object will be requested from a remote
    * rmiregistry and the returned reference cast to the Lookup interface and
    * invoked with a null reference, to return its proxy object.  If no
    * arguments are provided, the URL will be assumed to be
    * //localhost:1099/main.
    * <li> args[1] The optional external client host name, if using NAT.
    * <li> args[2] The optional external client port number, if using NAT.
    * <li> args[3] The optional internal client host name, if multi home/NIC.
    * <li> args[4] The optional internal client port number, if using NAT.
    * <li> args[5] The optional URL where to get a proxy item: file://
    * http:// ftp:// ..., //host:port/name (rmiregistry), /path/name
    * (serialized), or path/name (class).  It will be passed into the loaded
    * item as the sole argument to a setItem method.
    * </ul>
    */
   public static void main(String args[]) {
      try {
         if (args.length == 0) args = new String[] { "///main" };
         String clientHost = args.length > 1 ? args[1] : null;
         int clientPort    = args.length > 2 ? Integer.parseInt(args[2]) : 0;
         String localHost  = args.length > 3 ? args[3] : null;
         int localPort     = args.length > 4 ? Integer.parseInt(args[4]) : 0;
         Remote.config(localHost, localPort, clientHost, clientPort);
         try {
            System.setSecurityManager(new java.rmi.RMISecurityManager());
            System.setProperty("java.rmi.server.disableHttp", "true");
         } catch(SecurityException x) {}
         Invoke proxy = Remote.getItem(args[0]);
         if (args.length > 5) proxy.invoke("setItem", Remote.getItem(args[5]));
         Multicast.proxy = new Remote(proxy);
         registry = LocateRegistry.
            createRegistry(Remote.getServerPort(), Remote.rcsf, Remote.rssf);
         registry.bind("main", Multicast.proxy);
         mcast = new Multicast();
         mcast.announce(Multicast.proxy, 16);
         mcast.listen(proxy);
      } catch (Exception x) { x.printStackTrace(System.err); }
   }
}
