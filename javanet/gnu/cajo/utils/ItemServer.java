package gnu.cajo.utils;

import gnu.cajo.invoke.*;
import java.io.*;
import java.rmi.registry.*;
import java.text.DateFormat;
import java.rmi.RemoteException;
import java.rmi.MarshalledObject;
import java.util.zip.GZIPOutputStream;

/*
 * Standard Item Server Utility
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
 * These routines are used for item server construction.  The items would be
 * utilized by remote clients to compose larger, cooperative, functionality.
 * It creates an rmiregistry automatically, on class loading, since any given
 * VM instance can have only one.  It will be used to bind all item servers for
 * external access.  A given application can bind as many items as it needs.
 * <p>A security policy file, named "server.policy" will be loaded from the
 * local directory By default, the policy file need only allow the following
 * permissions:
 * <p><pre> grant codeBase "file:${java.class.path}" {
 *    permission java.security.AllPermission;
 * };
 * grant {
 *    permission java.net.SocketPermission "*:1024-", "accept";
 *    permission java.net.SocketPermission "*", "connect";
 * };</pre><p>
 * This will allow the server codebase full priviliges, and restricts any
 * proxies to create server sockets only on non-priviliged local ports,
 * while allowing client socket connections to any remote port.
 * <p> It can also be run as an application, to export a single item, for use
 * by remote clients.
 * <i>Note:</i> if  the {@link gnu.cajo.invoke.Remote Remote} class needs
 * configuration, it must be done <b>before</b> binding a proxy serving item,
 * since this will use the server's name and port number. This is accomplished
 * by calling its {@link gnu.cajo.invoke.Remote#config config} static method.
 * 
 * @version 1.0, 01-Nov-99 Initial release
 * @author John Catherino
 */
public class ItemServer {
   static {
      System.setProperty("java.rmi.server.disableHttp", "true");
      if (System.getProperty("java.security.policy") == null)
         System.setProperty("java.security.policy", "server.policy");
   }
   private static Invoke main;
   /**
    * The reference to the sole local rmiregistry of this VM. All binding
    * operations must make use of this instance, as there can be only one
    * rmiregistry per session.  All other items bound on this registry will
    * also have to share the same port.  The port used for the registry will
    * be the same one used to communicate with all local server instances.      
    */
   public static Registry registry;
   /**
    * Nothing happens in the constructor of this class, as all of its
    * methods are static, and it has no instance variables.  This class exists
    * solely as a server facilitating collection.
    */
   public ItemServer() {}
   /**
    * The generic bind operation remotes an item in the local rmiregistry.
    * strictly speaking, it performs a rebind operation on the rmiregistry.
    * Since the registry is not shared with other applications, checking
    * for already bound items is unnecessary.  If the server item implements
    * {@link gnu.cajo.invoke.Invoke Invoke} it will be invoked with the method
    * name "startProxy", and a null argument, to signal it to start its main
    * processing thread.
    * <i>Note:</i> if the Remote class needs configuration, it must be done
    * <b>before</b> binding a proxy serving item, since this will use the
    * server's name and port number. This is accomplished by calling its
    * {@link gnu.cajo.invoke.Remote#config config} static method.
    * @param item The item to be bound.  It may be either local to the machine,
    * or remote, it can even be a proxy for a remote server, that is, if a
    * suitable SecurityManager was already installed.
    * @param name The name under which to bind the remote reference in the
    * local rmiregistry.
    * @param acceptProxies If true, an RMISecurityManager will be installed,
    * that is, if no SecurityManager is currently installed for the VM.  This
    * would allow client proxies to run inside this VM.  <i>Note:</i> Allowing
    * client proxies to run inside this VM invites the possibility of a denial
    * of service attack.  Proxy hosting generally should be provided only on
    * an expendible VM.
    * @param mcast If non-null, a reference to a Multicast object on which to
    * announce the binding of this server to the listening community.
    * @return A remoted reference to the item within the context of this VM's
    * settings.
    * @throws RemoteException If the registry could not be contacted,
    * technically <i>unlikely</i> since the registry is always local.
    * @throws IOException If the UDP multicast announcement attempt failed.
    * @throws Exception, if the item rejects the startThread invocation.
    */
   public static Remote bind(Object item, String name, boolean acceptProxies,
      Multicast mcast) throws Exception {
      if (registry == null) {
         registry = LocateRegistry.
            createRegistry(Remote.getServerPort(), Remote.rcsf, Remote.rssf);
      }
      if (item instanceof Invoke) ((Invoke)item).invoke("startThread", null);
      Remote handle = item instanceof Remote ? (Remote)item : new Remote(item);
      if (item instanceof Invoke)
         ((Invoke)item).invoke("setProxy", new MarshalledObject(handle));
      registry.rebind(name, handle);
      if (mcast != null) mcast.announce(handle, 16);
      return handle;
   }
   /**
    * The generic bind method is used to serve the provided proxy item by the
    * provided server item.  It will remote a reference to the server item, and
    * bind in it the local rmiregistry under the name provided. If the item
    * implements {@link gnu.cajo.invoke.Invoke Invoke} it will be called with a
    * method string of "setProxy" and a {@link java.rmi.MarshalledObject
    * MarshalledObject} containing the proxy item. If the proxy implements
    * Invoke, it will be called with a remote reference to the serving item,
    * with a method argument of "setProxy".
    * @param item The item to be bound.  It may be either local to the machine,
    * or remote, it can even be a proxy for a remote server, that is, if a
    * suitable SecurityManager was already installed.
    * @param name The name under which to bind the server in the local
    * rmiregistry.
    * @param acceptProxies If true, an {@link java.rmi.RMISecurityManager
    * RMISecurityManager} will be installed, that is, only if no other
    * SecurityManager is currently installed for the VM.  This would allow
    * client proxies to run inside this VM.<br>
    * <i>Note:</i> Allowing client proxies to run inside this VM invites the
    * possibility of a denial of service attack.  Proxy hosting generally
    * should be provided only on a mission-expendible VM.
    * @param proxy The proxy item to be sent to requesting clients. If it
    * implements the Invoke interface, it will be called with a null argument,
    * and a remote reference to its server item.
    * @param mcast If non-null, a reference to a Multicast object on which to
    * announce the startup of this server to the listening community.
    * @return A remoted reference to the item within the context of this VM's
    * settings.
    * @throws IOException If the http server providing the codebase and applet
    * tag service could not be created, or the multicast announcement failed
    * to occur.
    * @throws java.rmi.RemoteException If the remote server reference could not
    * be created.
    * @throws RemoteException If the binding operation to the rmiregistry
    * failed, <i>very unlikely</i>, since it runs inside this VM.
    * @throws Exception if either the item or the proxy implements the
    * Invoke interface, and rejects the initialization invocation.
    */
   public static Remote bind(Object item, String name, boolean acceptProxies,
      Multicast mcast, Object proxy) throws Exception {
      if (registry == null) {
         registry = LocateRegistry.
            createRegistry(Remote.getServerPort(), Remote.rcsf, Remote.rssf);
      }
      if (item instanceof Invoke) ((Invoke)item).invoke("startThread", null);
      Remote handle = item instanceof Remote ? (Remote)item : new Remote(item);
      if (proxy instanceof Invoke) ((Invoke)proxy).invoke("setItem", handle);
      if (item instanceof Invoke)
         ((Invoke)item).invoke("setProxy", new MarshalledObject(proxy));
      registry.rebind(name, handle);
      if (mcast != null) mcast.announce(handle, 16);
      return handle;
   }
   /**
    * The application loads either a zipped marshalled object (zedmob) from a
    * URL, a file, or alternately, it will fetch a remote item reference from
    * an rmiregistry. It uses the {@link gnu.cajo.invoke.Remote#getItem getitem}
    * method of the {@link gnu.cajo.invoke.Remote Remote} class. The
    * application startup will be announced over a default
    * {@link Multicast Multicast} object.  The remote interface to the object
    * will be bound under the name "main" in the local rmiregistry using the
    * local {@link #bind bind} method.
    * <p>The startup can take up to six optional configuration parameters,
    * which must be in order, progressing from most important, to most
    * unlikely.<p>
    * @param args[0] The URL where to get the object: file:// http:// ftp://
    * /path/file, path/file or alternatively; //[host][:port]/[name]. The host
    * port and name are optional, if missing the host is presumed local, the
    * port 1099, and the name proxy. If no argument is provided, it will be
    * assumed to be ///proxy.
    * @param args[1] The optional external client host name, if using NAT.
    * @param args[2] The optional external client port number, if using NAT.
    * @param args[3] The optional internal client host name, if multi home/NIC.
    * @param args[4] The optional internal client port number, if using NAT.
    * @param args[5] The optional URL where to get a proxy item: file://
    * http:// ftp:// ..., //host:port/name (rmiregistry), /path/name
    * (serialized), or path/name (class).  It will be passed into the loaded
    * item as the sole argument to its setItem method.
    */
   public static void main(String args[]) {
      try {
         String url        = args.length > 0 ? args[0] : null;
         String clientHost = args.length > 1 ? args[1] : null;
         int clientPort    = args.length > 2 ? Integer.parseInt(args[2]) : 0;
         String localHost  = args.length > 3 ? args[3] : null;
         int localPort     = args.length > 4 ? Integer.parseInt(args[4]) : 0;
         Remote.config(localHost, localPort, clientHost, clientPort);
         System.setSecurityManager(new java.rmi.RMISecurityManager());
         main = Remote.getItem(url);
         if (args.length > 5) main.invoke("setItem", Remote.getItem(args[5]));
         main = bind(main, "main", true, new Multicast());
         System.out.println("Server started: " +
            DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL).
               format(new java.util.Date()));
         System.out.print("Serving item ");
         System.out.print(args[0]);
         System.out.println(" bound under name item");
         System.out.print("locally  operating on ");
         System.out.print(Remote.getServerHost());
         System.out.print(" port ");
         System.out.println(Remote.getServerPort());
         System.out.print("remotely operating on ");
         System.out.print(Remote.getClientHost());
         System.out.print(" port ");
         System.out.println(Remote.getClientPort());
      } catch (Exception x) { x.printStackTrace(System.err); }
   }
}
