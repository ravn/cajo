package gnu.cajo.invoke;

import java.io.*;
import java.net.*;
import java.rmi.*;
import java.util.zip.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.ArrayList;
import java.lang.reflect.Method;

/*
 * Generic Item Interface Exporter
 * Copyright (c) 1999 John Catherino
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
 * To receive a copy of the GNU Lesser General Public License visit their
 * website at http://www.fsf.org/licenses/lgpl.html or via snail mail at Free
 * Software Foundation Inc., 59 Temple Place Suite 330, Boston MA 02111-1307
 * USA
 */

/**
 * This class takes any object, and allows it to be called from
 * remote VMs. This class eliminates the need to maintain multiple
 * specialized rmic interface compilations for multiple, application specific
 * objects. It effectively allows any object to become a remotely callable
 * <b>Item</b>, in terms of this paradigm, and makes all of the object's
 * public methods remotely callable. It also contains several useful utility
 * methods, to further support the invoke package paradigm.<p> It can also be run
 * as an application, to load an object from a URL, and remote it within a VM.
 *
 * @version 1.0, 01-Nov-99 Initial release
 * @author John Catherino
 */
public final class Remote extends UnicastRemoteObject implements RemoteInvoke {
   private static final class RSSF implements RMIServerSocketFactory {
      private int port;
      private String host;
      public ServerSocket createServerSocket(int port) throws IOException {
         ServerSocket ss = host == null ?
            RMISocketFactory.getDefaultSocketFactory().
               createServerSocket(this.port) :
            new ServerSocket(this.port, 50, InetAddress.getByName(host));
         if (this.port == 0) {
            this.port = ss.getLocalPort();
            rcsf.port = this.port;
         } else if (rcsf.port == 0) rcsf.port = this.port;
         return ss;
      }
      public boolean equals(Object o) {
         return o != null && o.getClass().equals(getClass());
      }
      public int hashCode() { return getClass().hashCode(); }
   }
   private static final class RCSF // this class has become unnecessary for now
      implements RMIClientSocketFactory, Serializable {
      private int port;
      private String host;
      public Socket createSocket(String host, int port) throws IOException {
         return RMISocketFactory.getDefaultSocketFactory().
            createSocket(this.host, this.port != 0 ? this.port : port);
      }
      public boolean equals(Object o) {
         return o != null && o instanceof RCSF &&
            ((RCSF)o).host.equals(host) &&
               ((RCSF)o).port == port;
      }
      public int hashCode() { return getClass().hashCode() + port; }
   }
   private static Object proxy;
   private static Registry registry;
   /**
    * A global reference to the remote client socket factory.  This is the
    * factory remote VMs will use to communicate with local items.
    */
   public static final RCSF rcsf = new RCSF();
   /**
    * A global reference to the remote server socket factory.  This is the
    * factory the local items use to asynchronously communicate with remote
    * VMs.
    */
   public static final RSSF rssf = new RSSF();
   /**
    * This method is provided to obtain the server's host name.
    * This is useful when the host can have one of multiple addresses, either
    * because it has multiple network interface cards, or is multi-homed. 
    * @return The server address on which the item is remoted.
    */
   public static String getServerHost() { return rssf.host; }
   /**
    * This method is provided to obtain the local  server socket port
    * number. This can be particularly useful if the host was remoted on an
    * anonymous port.  If a firewall is in use, this inbound port must be made
    * accessible to outside clients.
    * @return The local ServerSocket port number on which the item is remoted.
    */
   public static int getServerPort() { return rssf.port; }
   /**
    * This method is provided to obtain the host name remote clients will
    * use to contact this server. This can be different from the local server
    * name or address if NAT is being used.
    * @return The server address clients use to connect to the server.
    */
   public static String getClientHost() { return rcsf.host; }
   /**
    * This method is provided to obtain the socket port number the remote
    * client must use to contact the server.  This can be different from
    * the server port number if NAT port translation is being used.
    * @return The port clients must connect on to reach the server.
    */
   public static int getClientPort() { return rcsf.port; }
   /**
    * This method configures the server's TCP parameters for RMI.  It allows
    * complete specification of client-side and server-side ports and
    * hostnames.  It used to override the default values.  It is necessary
    * when either VM is either operating behind a firewall, has multiple network
    * interfaces, is multi-addressed, or is using NAT. The first two parameters
    * control how the sockets will be configured locally, the second two
    * control how a remote object's sockets will be configured to communicate
    * with this server.
    * <p><i>Note:</i> If this class is to be configured, it must be done
    * <b>before</b> any items are remoted.
    * @param serverHost The local domain name, or IP address of this host.
    * If null, it will use the default local address.  Typically it is
    * specified when the server has multiple phyisical network interfaces, or
    * is multi-homed, i.e. having multiple logical network interfaces.
    * @param serverPort Specifies the local port on which the server is
    * serving clients. It can be zero, to use an anonymous port.  If firewalls
    * are being used, it must be an accessible port, into this server. If this
    * port is zero, and the ClientPort argument is non-zero, then the
    * ClientPort value will automatically substituted.
    * @param clientHost The domain name, or IP address the remote client will
    * use to communicate with this server.  If null, it will be the same as
    * serverHost resolution.  This would need to be explicitly specified if
    * the server is operating behind NAT i.e. when the server's subnet IP
    * address is <i>not</i> the same as its address outside the subnet.
    * @param clientPort Specifies the particular port on which the client
    * will connect to the server.  Typically this is the <i>same</i> number
    * as the serverPort argument, but could be different, if port translation
    * is being used.  If the clientPort field is 0, i.e. anonymous, its port
    * value will be automatically assigned to match the server, even if the
    * server port is anonymous.
    * @throws java.net.UnknownHostException If the IP address or name of the
    * serverHost can not be resolved.
    */
   public static void config(String serverHost, int serverPort,
      String clientHost, int clientPort) throws java.net.UnknownHostException {
      rssf.host = serverHost != null ?
         serverHost : InetAddress.getLocalHost().getHostName();
      rcsf.host = clientHost != null ? clientHost : rssf.host;
      rssf.port =
         serverPort != 0 ? serverPort : clientPort != 0 ? clientPort : 0;
      rcsf.port = clientPort != 0 ? clientPort : rssf.port;
      try { // this won't work if running as an applet
         System.setProperty("java.rmi.server.useLocalHostname", "true");
         System.setProperty("java.rmi.server.hostname", rcsf.host);
      } catch(SecurityException x) {}
   }
   /**
    * This method configures the server's TCP parameters for RMI through HTTP
    * proxy servers. This is necessary when the client or server, or both are
    * behind firewalls, and the only method of access to the internet is
    * through HTTP proxy servers. There will be a fairly significant performance
    * hit incurred using the HTTP tunnel, but it is better than having no
    * connectivity at all. Due to an unfortunate oversight in the design of
    * the standard RMISocketFactory, no server network interface can be
    * specified, instead it will listen on <i>all</i> network interfaces.
    * It is probably not a problem for most, but is probably not desirable for
    * multi-homed hosts.
    * <p><i>Note:</i> If this class is to be configured, it must be done
    * <b>before</b> any items are remoted.
    * @param serverPort Specifies the local inbound port on which the server is
    * serving clients. It can be zero, to use an anonymous port.  This must
    * are being used, it must be an accessible port, into this server. If this
    * port is zero, and the ClientPort argument is non-zero, then the
    * ClientPort value will automatically substituted.
    * @param clientHost The domain name, or IP address the remote client will
    * use to communicate with this server.  If null, it will be the server's
    * default host name.  This would need to be explicitly specified if
    * the server is operating behind NAT i.e. when the server's subnet IP
    * address is <i>not</i> the same as its address outside the subnet.
    * @param clientPort Specifies the particular port on which the client
    * will connect to the server.  Typically this is the <i>same</i> number
    * as the serverPort argument, but could be different, if port translation
    * is being used.  If the clientPort field is 0, i.e. anonymous, its port
    * value will be automatically assigned to match the server, even if the
    * server port is anonymous.
    * @param proxyHost The name or address of the proxy server used to gain
    * HTTP access to the internet.
    * @param proxyPort The port number of the proxy server used to gain
    * HTTP access to the internet.
    * @param username The proxy account user name required for permission, if
    * non-null.
    * @param password The proxy account password required for permission, if
    * required.
    * @throws java.net.UnknownHostException If the IP address or name of the
    * local host interface can not be determined.
    */
   public static void config(int serverPort, String clientHost, int clientPort,
      String proxyHost, int proxyPort, final String username,
      final String password) throws java.net.UnknownHostException {
      rcsf.host = (clientHost != null) ?
         clientHost : InetAddress.getLocalHost().getHostName();
      rssf.port = (serverPort != 0) ?
         serverPort : ((clientPort != 0) ? clientPort : 0);
      rcsf.port = (clientPort != 0) ?
         clientPort : rssf.port;
      try { // this won't work if running as an applet
         if (proxyHost != null) {
            System.setProperty("proxySet", "true");
            System.setProperty("http.proxyHost", proxyHost);
            System.setProperty("http.proxyPort", Integer.toString(proxyPort));
            if (username != null) Authenticator.setDefault(
               new Authenticator() {
                  protected PasswordAuthentication getPasswordAuthentication() {
                     return new PasswordAuthentication(
                        username, password.toCharArray());
                  }
               }
            );
         }
         System.setProperty("java.rmi.server.useLocalHostname", "true");
         System.setProperty("java.rmi.server.hostname", rcsf.host);
      } catch (SecurityException x) {}
   }
   /**
    * A utility method to reconstitute a zipped marshalled object (zedmob)
    * into a remote item reference, proxy object, or local object. <i>Note:</i>
    * on completion of reading the item from the stream, the stream will be
    * automatically closed. Typically a file containing a zedmob has the file
    * extension .zedmob as an identifier.
    * @param is The input stream containing the zedmob of the item reference.
    * @return A reconstituted reference to the item.
    * @throws IOException if the zedmob format is invalid.
    * @throws ClassNotFoundException if a proxy object was sent, and remote
    * class loading was not enabled in this VM.
    */
   public static Object zedmob(InputStream is)
      throws ClassNotFoundException, IOException {
      GZIPInputStream   gis = new GZIPInputStream(is);
      ObjectInputStream ois = new ObjectInputStream(gis);
      MarshalledObject  mob = (MarshalledObject)ois.readObject();
      ois.close();
      return mob.get();
   }
   /**
    * This method will write the local item, remote item reference, or proxy,
    * to an output stream as a zipped marshalled object (zedmob). A zedmob is
    * the standard serialized format in this paradigm. This can be used to
    * <i>'freeze-dry'</i> the object to a file for later use, to send it over
    * the network, or to an object archival service, for example. <i>Note:</i>
    * on completion of writing the item, or reference, the stream will be
    * closed. Typically, when saved to a file, a zedmob has the file extension
    * .zmob to provide obvious identification.
    * @param os The output stream on which to write the reference.  It may be
    * a file stream, a socket stream, or any other type of stream.
    * @param ref The item or reference to be serialized.
    * @throws IOException For any stream related writing error.
    */
   public static void zedmob(OutputStream os, Object ref) throws IOException {
      GZIPOutputStream   zos = new GZIPOutputStream(os);
      ObjectOutputStream oos = new ObjectOutputStream(zos);
      oos.writeObject(new MarshalledObject(ref));
      oos.flush();
      zos.flush();
      oos.close();
   }
   /**
    * A utility method to load either an item, or a zipped marshalled object
    * (zedmob) of an item, from a URL, file, or from a remote rmiregistry.
    * If the item is in a local file, it can be either inside the server's
    * jar file, or on its local file system.<p> Loading an item from a file
    * can be specified in one of three ways:<p><ul>
    * <li>As a URL; in the format file://path/name.
    * <li>As a class file; in the format path/name
    * <li>As a serialized item; in the format /path/name</ul><p>
    * File loading will first be attempted from within the server's jar file,
    * if that fails, it will then look in the local filesystem. <i>Note:</i>
    * any socket connections made by the incoming item cannot be known at
    * compile time, therefore proper operation if this VM is behind a firewall
    * could be blocked. Use behind a firewall would require knowing all the
    * ports that would be needed in advance, and enabling them before
    * loading the proxy.
    * @param url The URL where to get the object: file://, http://, ftp://,
    * /path/name, path/name, or //[host][:port]/[name]. The host, port,
    * and name, are all optional. If missing the host is presumed local, the
    * port 1099, and the name "main". The referenced resource can be
    * returned as a MarshalledObject, it will be extracted automatically.
    * If the URL is null, it will be assumed to be ///.
    * @return A reference to the item contained in the URL. It may be either
    * local, or remote to this VM.
    * @throws RemoteException if the remote registry could not be reached.
    * @throws NotBoundException if the requested name is not in the registry.
    * @throws IOException if the zedmob format is invalid.
    * @throws ClassNotFoundException if a proxy was sent to the VM, and
    * proxy hosting was not enabled.
    * @throws InstantiationException when the URL specifies a class name
    * which cannot be instantiated at runtime.
    * @throws IllegalAccessException when the url specifies a class name
    * and it does not support a no-arg constructor.
    * @throws MalformedURLException if the URL is not in the format explained
    */
   public static Object getItem(String url) throws RemoteException,
      NotBoundException, IOException, ClassNotFoundException,
      InstantiationException, IllegalAccessException, MalformedURLException {
      Object item = null;
      if (url == null) url = "///main";
      else if (url.startsWith("//") && url.endsWith("/")) url += "main";
      if (url.startsWith("//")) { // if from an rmiregistry
         item = java.rmi.Naming.lookup(url); // get reference
      } else if (url.startsWith("/")) { // if from a serialized object file
         InputStream ris = Remote.class.getResourceAsStream(url);
         if (ris == null) ris = new FileInputStream('.' + url);
         item = zedmob(ris);
         ris.close();
      } else if (url.indexOf(':') == -1) { // from a class file
         item = Class.forName(url).newInstance();
      } else { // otherwise from a real URL, http:// ftp:// file:// etc.
         InputStream uis = new URL(url).openStream();
         item = zedmob(uis);
         uis.close();
      }
      return item;
   }
   /**
    * This method attempts to resolve the polymorphism blindness in Java
    * reflection. It has been most graceously submitted by project member
    * <b>Fredrik Larsen</b>, with help from project member <b>Li Ma</b>. If
    * more than one matching method is found on the internal item, based on
    * polymorphism, it will try to select the most applicable one to call. It
    * works quite well, if the inheritence trees for the arguments are small.
    * It <i>may not</i> always pick the very best method if the arguments have
    * very deep inheritance trees.
    * @param item The object on which to find the most applicable public
    * method.
    * @param method The name of the method, which is to be invoked.
    * @param args The class representations of the arguments to be
    * provided to the argument.
    * @return The most applicable method, which will accept all of these
    * arguments.
    */
   public static Method findBestMethod(
      Object item, String method, Class[] args) {
      ArrayList match_list = new ArrayList();
      Method[] ms = item.getClass().getMethods();
      // Find any matching methods in the item and put them in a list:
      list: for(int i = 0; i < ms.length; i++) {
         if (ms[i].getName().equals(method) &&
            ms[i].getParameterTypes().length == args.length) {
            for (int j = 0; j < args.length; j++)
               if (!ms[i].getParameterTypes()[j].isAssignableFrom(args[j]))
                  continue list;
               match_list.add(ms[i]);
         }
      }
      if(match_list.size() > 1) { // if more than one method matches:
         int goodness = 0;
         Method m_selected = (Method)match_list.get(0);
         for (int i = 1; i < match_list.size(); i++) {
            int closeness = 0;
            Method m = (Method)match_list.get(i);
            for (int j = 0; j < args.length; j++) {
               // If next method argument is a subclass of the
               // corresponding argument in the current "best match
               // method"    -> Increase closeness count. If it is a
               // superclass -> Decrease closeness count. If it is a
               // peer       -> Leave closeness count unchanged. This
               // will handle implemented interfaces in addition to
               // extended classes of the argument
               Class best = m_selected.getParameterTypes()[j];
               Class next = m.getParameterTypes()[j];
               if (!next.isAssignableFrom(best))closeness++; else
               if (!best.isAssignableFrom(next))closeness--;
            }
            if (closeness > goodness) {
               m_selected = m;
               goodness = closeness;
            }
         }
         return m_selected;
      } else return match_list.size() > 0 ? (Method)match_list.get(0) : null;
   }
   /**
    * This function may be called reentrantly, so the item object <i>must</i>
    * synchronize its critical sections as necessary. The specified method
    * will be invoked, with the provided arguments if any, on the internal
    * object's public method via the framework Java reflection mechanism, and
    * the result returned, if any. The method is declared static to centralize
    * the implementation, and allow other derived classes to use this
    * mechanism without having to reimplement it. If the arguments are being
    * sent to a remote VM, and are not already encapsulated in a
    * MarshalledObject, they will be, automatically.
    * @param item The object on which to invoke the method. If the item
    * implements the {@link Invoke Invoke} interface, the call will be passed
    * directly to it.
    * @param method The method name to be invoked.
    * @param args The arguments to provide to the method for its invocation.
    * @return The resulting data, if any, from the invocation.
    * @throws IllegalArgumentException If the method argument is null.
    * @throws NoSuchMethodException If no matching method can be found.
    * @throws Exception If the item rejected the invocation, for application
    * specific reasons.
    */
   public static Object invoke(Object item, String method, Object args)
      throws Exception {
      if (item instanceof Invoke) args = ((Invoke)item).invoke(method, args);
      else if (method == null)
         throw new IllegalArgumentException("Method argument cannot be null");
      else if (args instanceof Object[]) {
         Object[] o_args = (Object[])args;
         Class[]  c_args = new Class[o_args.length];
         for(int i = 0; i < o_args.length; i++)
            c_args[i] = o_args[i].getClass();
         Method m = findBestMethod(item, method,c_args);
         if (m!= null) args = m.invoke(item, o_args);
         else throw new NoSuchMethodException();
      } else if (args != null) {
         Method m =
            findBestMethod(item, method, new Class[]{ args.getClass() });
         if (m != null) args = m.invoke(item, new Object[]{ args });
         else throw new NoSuchMethodException();
      } else args = item.getClass().getMethod(method, null).invoke(item, null);
      return args;
   }
   private final Object item;
   /**
    * The constructor takes <i>any</i> object, and allows it to be remotely
    * invoked. If the object implements the {@link Invoke Invoke} interface,
    * (i.e. it is an <b>Item</b>) it will simply route all remote invocations
    * directly to it. Otherwise it will use Java reflection to attempt to
    * invoke the remote calls directly on the object's public interface.
    * @param  item The object to make remotely callable.  It may be an
    * arbitrary object of any type, or an <b>Item</b> (either local or remote).
    * @throws RemoteExcepiton If the remote instance could not be be created.
    */
   public Remote(Object item) throws RemoteException {
      super(rssf.port, rcsf, rssf);
      this.item = item;
   }
   /**
    * This method is used to check if two Remote objects are holding an
    * equavilent inner item. It short-circuit's the invocation, returning the
    * result of the internal object's equals invocation.
    * @param object A reference to another object to compare for equality.
    * @return The result of the equals method called on the internal object. 
    */
   public boolean equals(Object object) { return item.equals(object); }
   /**
    * This method is used to identify the internal object.  It short-circuit's
    * the invocation, returning the result of the internal object's toString
    * invocation.
    * @return The internal object's cannonical string identifier. 
    */
   public String toString() { return item.toString(); }
   /**
    * This method is used to identify the internal object.  It short-circuit's
    * the invocation, returning the result of the internal item's hashCode
    * invocation.
    * @return The semi-unique integer identifier for the internal object.
    */
   public int hashCode() { return item.hashCode(); }
   /**
    * The sole generic, multi-purpose interface for communication between VMs.
    * This function may be called reentrantly, so the inner object <i>must</i>
    * synchronize its critical sections as necessary. Technically, it simply
    * passes the call to this class' static invoke method. If the arriving
    * arguments are encapsulated in a MarshalledObject, they will be extracted
    * here automatically.
    * @param method The method to invoke on the internal object.
    * @param args The arguments to provide to the method for its invocation.
    * It can be a single object, an array of objects, or even null.
    * @return The sychronous data, if any, resulting from the invocation.
    * @throws java.rmi.RemoteException For network communication related
    * reasons.
    * @throws IllegalArgumentException If reflection is going to be used,
    * and the method argument is null.
    * @throws NoSuchMethodException If no matching method can be found.
    * @throws Exception If the internal item rejected the invocation, for
    * application specific reasons.
    */
   public Object invoke(String method, Object args) throws Exception {
      return invoke(item, method, args);
   }
   /**
    * This method sends its remote reference to another item, either from a
    * URL, file, or from a remote rmiregistry. It will invoke the local
    * {@link #getItem getItem} method to obtain a reference to the remote
    * item. It will next invoke the received reference's invoke method with
    * a "send" value, and a reference to itself as its sole argument.
    * @param url The URL where to get the remote host interface: file://,
    * http://, ftp://, /path/name, path/name, or //[host][:port]/[name].
    * The host, port, and name, are all optional. If missing the host is
    * presumed local, the port 1099, and the name "main".  If the URL is
    * null, it will be assumed to be ///.
    * @return Whatever the item returns in receipt of the reference,
    * even null.
    * @throws Exception Either from the getItem invocation, or if the
    * item reference invocation fails.
    */    
   public Object send(String url) throws Exception {
      if (url == null) url = "///main";
      else if (url.startsWith("//") && url.endsWith("/")) url += "main";
      return invoke(getItem(url), "send", this);
   }
   /**
    * This method will write this remote item reference to an output stream
    * as a zipped marshalled object (zedmob). A zedmob is the standard
    * serialized format for a remote item reference, in this paradigm.
    * This can be used to <i>'freeze-dry'</i> the remote reference, to a file
    * for later use, send it over the network, or to an object archival
    * service, for example.
    * @param os The output stream on which to write the reference.  It may be
    * a file stream, a socket stream, or any other type of stream.
    * @throws IOException For any stream related writing error.
    */
   public void zedmob(OutputStream os) throws IOException {
      zedmob(os, this);
   }
   /**
    * The application method loads a zipped marshalled object (zedmob) from a
    * URL, or a file, and allows it run in this virtual machine. It uses
    * the {@link #getItem getItem} method to load the item.  Following loading
    * of the item, it will also create an rmiregistry, and bind a remote
    * reference to it under the name "main".  This will also allow remote
    * clients to connect to, and interact with it.  <i>Note:</i>It will
    * require a security policy, to define what permissions the loaded item
    * will be allowed. There are six optional configuration parameters:<ul>
    * <li> args[0] The optional URL where to get the object: file:// http://
    * ftp:// ..., /path/name <serialized>, path/name <class>, or alternatively;
    * //[host][:port]/[name].  If no arguments are provided, the URL will be
    * assumed to be //localhost:1099/main.
    * <li> args[1] The optional external client host name, if using NAT.
    * <li> args[2] The optional external client port number, if using NAT.
    * <li> args[3] The optional internal client host name, if multi home/NIC.
    * <li> args[4] The optional internal client port number, if using NAT.
    * <li> args[5] The optional URL where to get a proxy item: file://
    * http:// ftp:// ..., //host:port/name (rmiregistry), /path/name
    * (serialized), or path/name (class).  It will be passed into the loaded
    * proxy as the sole argument to a setItem method invoked on the loaded item.
    * </ul>
    */
   public static void main(String args[]) {
      try {
         if (args.length == 0) args = new String[] { "///main" };
         String clientHost = args.length > 1 ? args[1] : null;
         int clientPort    = args.length > 2 ? Integer.parseInt(args[2]) : 0;
         String localHost  = args.length > 3 ? args[3] : null;
         int localPort     = args.length > 4 ? Integer.parseInt(args[4]) : 0;
         config(localHost, localPort, clientHost, clientPort);
         try { System.setProperty("java.rmi.server.disableHttp", "true"); }
         catch(SecurityException x) {}
         proxy = getItem(args[0]);
         if (args.length > 5) invoke(proxy, "setItem", getItem(args[5]));
         registry =
            LocateRegistry.createRegistry(getServerPort(), rcsf, rssf);
         registry.bind("main", new Remote(proxy));
      } catch (Exception x) { x.printStackTrace(System.err); }
   }
}
