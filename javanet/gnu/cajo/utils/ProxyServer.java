package gnu.cajo.utils;

import gnu.cajo.invoke.*;
import java.io.*;
import java.net.*;
import java.rmi.MarshalledObject;
import java.util.zip.GZIPOutputStream;

/*
 * Standard Proxy Server Utility
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
 * These routines form the foundation architecture to send proxies to remote
 * VMs. It requires two inbound ports; the first for connection to its
 * rmiregistry and communication with this sending VM. The second is used to
 * fecth the proxy codebase jar file.  Both ports can be anonymous, i.e.
 * selected from any available free ports at runtime, or they can be explicitly
 * specified, usually to operate through a firewall.  It may also need one
 * outbound port, on which to asynchronously callback its proxies, if needed.
 * The callback port number is generally be chosen by the client, to get
 * through his firewall. A given VM instance can can bind as multiple proxy
 * server items.<p>
 * <i>Note:</i> if  the {@link gnu.cajo.invoke.Remote Remote} class needs
 * configuration, it must be done <b>before</b> binding a proxy serving item,
 * since this will use the server's name and port number. This is accomplished
 * by calling its {@link gnu.cajo.invoke.Remote#config config} static method.
 * 
 * @version 1.0, 01-Nov-99 Initial release
 * @author John Catherino
 */
public final class ProxyServer implements Runnable {
   private static final byte[]
      bye = (
         "HTTP/1.0 404 Object not found\r\n" +
         "Connection: close\r\n\r\n"
      ).getBytes(),
      tag = (
         "HTTP/1.0 200 OK\r\n"   +
         "Content-type: text/html\r\n" +
         "Connection: close\r\n" +
         "Content-length: "
      ).getBytes(),
      jar = (
         "HTTP/1.0 200 OK\r\n"   +
         "Content-type: application/octet-stream\r\n" +
         "Cache-control: no-store\r\n" +
         "Connection: close\r\n" +
         "Content-length: "
      ).getBytes(),
      top = (
         "<HTML><HEAD><TITLE>CaJo Proxy Viewer</TITLE>\r\n" +
         "<META NAME=\"description\" content=\"Lookup Proxy Client\"/>\r\n" +
         "<META NAME=\"copyright\" content=\"Copyright ©1999 by John Catherino\"/>\r\n" +
         "<META NAME=\"author\" content=\"John Catherino\"/>\r\n" +
         "<META NAME=\"generator\" content=\"ProxyServer&#153\"/>\r\n" +
         "<LINK REL=\"shortcut icon\" HREF=\"http://www.java.com/favicon.ico\" TYPE=\"image/x-icon\"/>\r\n" +
         "<LINK REL=\"icon\" HREF=\"http://www.java.com/favicon.ico\" TYPE=\"image/x-icon\"/>\r\n" +
         "</HEAD><BODY leftmargin=0 topmargin=0 marginheight=0 marginwidth=0 rightmargin=0>\r\n" +
         "<CENTER><OBJECT classid = \"clsid:8AD9C840-044E-11D1-B3E9-00805F499D93\"\r\n" +
         "WIDTH = 100% HEIGHT = 100%\r\n" +
         "CODEBASE = \"http://java.sun.com/update/1.4.2/jinstall-1_4_2-windows-i586.cab#Version=1,4,0,0\">\r\n" +
         "<PARAM NAME = \"archive\" VALUE = \"client.jar\">\r\n" +
         "<PARAM NAME = \"type\" VALUE = \"application/x-java-applet;version=1.4\">\r\n" +
         "<PARAM NAME = \"code\" VALUE = \"gnu/cajo/invoke/Client.class\">\r\n"
      ).getBytes(),
      mid = (
         "<COMMENT><EMBED type = \"application/x-java-applet;version=1.4\"\r\n" +
         "ARCHIVE = client.jar\r\n" +
         "CODE = gnu/cajo/invoke/Client.class\r\n" +
         "WIDTH = 100% HEIGHT = 100%\r\n"
      ).getBytes(),
      end = (
         "PLUGINSPAGE = http://java.sun.com/j2se/1.4.2/download.html>\r\n" +
         "</EMBED></COMMENT></OBJECT></CENTER></BODY></HTML>"
      ).getBytes();
   private static final int fixlen = top.length + mid.length + end.length;
   private static String name;
   private static ServerSocket ss;
   private static Thread thread;
   /**
    * Typically the first proxy server item bound, it is returned to a
    * client making an http://<serverhost>// request. It can be reassigned
    * by the client at runtime, if necessary.  If nulled, the next item
    * bound will become the new default server, all double slash requests
    * until reassignment, will be rejected.
    */
   public static Remote defaultServer;
   /**
    * The path and file name of the jar file containing the proxy specific
    * classes, serialized objects, and other resources, enclosed in a jar file.
    * The server will look first for the proxy jar inside the server's own jar,
    * to support the creation of a single client/proxy/server jar file.  If it
    * cannot find it inside its jar, at the location specified, it will load it
    * from the local file system, using the path specified. Placing the proxy
    * outside the server jar file has an advantage in that it allows for the
    * possibility to 'hot-swap' the proxy codebase at runtime. All subsequently
    * connecting clients would receive the updated codebase, without the need
    * to shut down the server.
    * <p><i>Note:</i> since a VM can have only one codebase property, it will
    * be permanently specified on the first item bind operation with the value
    * in the fileName member. By default, its value is "proxy.jar".
    */
   public static String fileName = "proxy.jar";
   /**
    * This is the local inbound {@link java.net.ServerSocket ServerSocket}
    * port number providing the http data and codebase jar service.  If the
    * server is behind a firewall, this port, or the translated one, must be
    * made accessible from outside.  It can be zero, to use an anonymous port,
    * selected by the OS from any available at runtime.  In that case, the
    * port offered by the operating system will be stored here automatically
    * following the binding of the first proxy server item. By default, its
    * value is zero.
    */
   public static int port;
   /**
    * Nothing is performed in the constructor, since all of the class methods
    * are static.  This class is simply a collection of resources to facilitate
    * the creation of proxy servers.
    */
   public ProxyServer() {}
   /**
    * The generic bind method is used to serve the provided proxy item by the
    * provided server item.  It will remote a reference to the server item, and
    * bind in it the local rmiregistry under the name provided. Since it
    * calls the {@link ItemServer#bind bind} operation of the ItemServer class,
    * strictly speaking it is performing a rebind operation on the rmiregistry.
    * If the item implements {@link gnu.cajo.invoke.Invoke Invoke} it will be
    * called with a method string of "setProxy" and a
    * {@link java.rmi.MarshalledObject MarshalledObject} containing the proxy
    * item.
    * <br><br>The format of a browser's proxy request URL one required and has
    * three optional parameters, utilizing the following format:<p><code>
    * http://serverHost[:serverPort]/[clientPort][:localPort][-proxyName]
    * </code><p>
    * Where the parameters have the following meanings:<ul>
    * <li><i>serverHost</i> The domain name, or IP address of the proxy server.
    * <li><i>serverPort</i> The port number of the applet/codebases service,
    * unspecified it will be 80.
    * <li><i>clientPort</i> The client's external port number on which the
    * remote proxy can be reached. It is often explicitly specified when the
    * client is behind a firewall.  Unspecified, it will be an anonymous port
    * selection, made by the client's operating system at runtime.
    * <li><i>localPort</i> The port number the proxy must use to callback the
    * server.  This may need to be specified if the client is behind a
    * firewall.  Unspecified, it will be selected anonymously by the client
    * at runtime.
    * <li><i>proxyName</i> The registered name of the proxy item, by default
    * "proxy", however a server can support multiple proxies.</ul>
    * <p>To unspecify any item, simply omit it, from the URL, along with its
    * preceeding delimiter, if any.  The order of the arguments must be
    * maintained however.<p>
    * <i>Note:</i>A cajo-capable client can perform a special
    * {@link gnu.cajo.invoke.Remote#getItem getItem} invocation using the URL:
    * http://serverhost// this will fetch the default proxy server reference
    * directly over the http connection as a zipped marshalled object (zedmob).
    * This allows proxy loading without the need to use the rmiregistry.  The
    * default proxy server is typically the first proxy server to be bound,
    * though it can be changed by reassigning the defaultServer member.<p>
    * @param item The, typically local, server item reference with which
    * the proxy will communicate. It can be a reference to a remote item, or
    * potentially even a proxy from another server. If the server implements
    * the {@link gnu.cajo.invoke.Invoke Invoke} interface, it will be called
    * with a null method argument, and a reference to its proxy, encased in
    * a {@link java.rmi.MarshalledObject MarshalledObject}.
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
    * implements the {@link gnu.cajo.invoke.Invoke Invoke} interface, it
    * will be called with a null argument, and a remote reference to its
    * server item.
    * @param mcast If non-null, a reference to a {@link Multicast Multicast}
    * object on which to announce the startup of this server to the listening
    * community.
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
   public static void bind(Object item, String name, boolean acceptProxies,
      Multicast mcast, Object proxy) throws Exception {
      if (ss == null) {
         ProxyServer.name = name;
         ss = new ServerSocket(port, 50,
            InetAddress.getByName(gnu.cajo.invoke.Remote.getServerHost()));
         if (port == 0) port = ss.getLocalPort();
         System.setProperty("java.rmi.server.codebase",
            "http://" + Remote.getClientHost() +
               ':' + port + '/' + fileName);
         thread = new Thread(new ProxyServer());
         thread.start();
      }
      Remote ref = ItemServer.bind(item, name, acceptProxies, mcast);
      if (defaultServer == null) defaultServer = ref;
      if (proxy instanceof Invoke) ((Invoke)proxy).invoke(null, ref);
      if (item instanceof Invoke)
         ((Invoke)item).invoke("setProxy", new MarshalledObject(proxy));
   }
   /**       
    * The run method is invoked in a separate thread.  It will provide a
    * highly specialized http server, used to provide proxies and their 
    * codebase to client applications.  It doesn't use the traditional applet
    * tag, in order to prompt unconfigured browsers to download the plug-in.
    */
   public void run() {
      try {
         byte msg[] = new byte[256];
         while(true) {
            Socket s = ss.accept();
            try {
               InputStream  is = s.getInputStream();
               OutputStream os = s.getOutputStream();
               int ix = is.read(msg);
               String itemName = null;
               scan: for (int i = 0; i < ix; i++) {
                  if (msg[i] == '/') {
                     for (int j = i + 1; j < msg.length; j++) {
                        if (msg[j] == ' ') {
                           itemName = new String(msg, i, j - i);
                           break scan;
                        }
                     }
                  }
               }
               if (itemName == null) os.write(bye);
               else if (itemName.equals("//")) {
                  if (defaultServer != null)
                     try { defaultServer.zedmob(os); }
                     catch(SocketException x) {} // strange, but necessary
                  else os.write(bye);
               } else if (itemName.endsWith(".jar")) {
                  try {
                     InputStream ris = getClass().getResourceAsStream(itemName);
                     if (ris == null) ris = new FileInputStream('.' + itemName);
                     BufferedInputStream bis = new BufferedInputStream(ris);
                     msg = new byte[bis.available()];
                     byte len[] = (msg.length + "\r\n\r\n").getBytes();
                     bis.read(msg);
                     bis.close();
                     ris.close();
                     os.write(jar);
                     os.write(len);
                     os.write(msg);
                  } catch(Exception x) { os.write(bye); }
               } else if (itemName.indexOf('/', 1) == -1) {
                  try { // parse request arguments
                     int proxyPort = Remote.getClientPort();
                     int ia = itemName.indexOf(':') != -1 ? itemName.indexOf(':') :
                        itemName.indexOf('-') != -1 ? itemName.indexOf('-') :
                        itemName.length();
                     int ib = itemName.indexOf('-') != -1 ? itemName.indexOf('-') :
                        itemName.length();
                     int ic = itemName.length() > ib ? itemName.length() : ib;
                     String clientPort =
                        ia >  1 ? itemName.substring(     1, ia) : null;
                     String localPort =
                        ib > ia ? itemName.substring(ia + 1, ib) : null;
                     String proxyName =
                        ic > ib ? itemName.substring(ib + 1, ic) : name;
                     String clientHost = s.getInetAddress().getHostAddress();
                     byte iex[] = ( // used by Internet Explorer:
                        "<PARAM NAME = \"clientHost\" VALUE = \"" + clientHost + "\">\r\n" +
(clientPort != null ?   "<PARAM NAME = \"clientPort\" VALUE = \"" + clientPort + "\">\r\n" : "") +
(localPort  != null ?   "<PARAM NAME = \"localPort\"  VALUE = \"" + localPort  + "\">\r\n" : "") +
                        "<PARAM NAME = \"proxyPort\"  VALUE = \"" + proxyPort  + "\">\r\n" +
                        "<PARAM NAME = \"proxyName\"  VALUE = \"" + proxyName  + "\">\r\n"
                     ).getBytes();
                     byte nav[] = ( // used by Navigator and Appletviewer:
                        "clientHost = " + clientHost + "\r\n" +
(clientPort != null ?   "clientPort = " + clientPort + "\r\n" : "") +
(localPort  != null ?   "localPort  = " + localPort  + "\r\n" : "") +
                        "proxyPort  = " + proxyPort  + "\r\n" +
                        "proxyName  = " + proxyName  + "\r\n"
                     ).getBytes();
                     byte len[] = (fixlen + iex.length + nav.length + "\r\n\r\n").getBytes();
                     os.write(tag);
                     os.write(len);
                     os.write(top);
                     os.write(iex);
                     os.write(mid);
                     os.write(nav);
                     os.write(end);
                  } catch(Exception x) { os.write(bye); }
               } else os.write(bye);
               os.flush();
               os.close();
               is.close();
            } catch (Exception x) { x.printStackTrace(System.err); }
            try { s.close(); }
            catch (Exception x) { x.printStackTrace(System.err); }
         }
      } catch(Exception x) { x.printStackTrace(System.err); }
      try { ss.close(); }
      catch(Exception x) { x.printStackTrace(System.err); }
   }
}
