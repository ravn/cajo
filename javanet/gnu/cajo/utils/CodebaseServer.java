package gnu.cajo.utils;

import java.io.*;
import java.net.*;
import gnu.cajo.invoke.Remote;

/*
 * RMI Codebase Server Utility
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
 * The standard mechanism to send proxies, and other complex objects to remote
 * VMs. It requires one outbound port. The port can be anonymous, i.e. selected
 * from any available free port at runtime, or it can be explicitly specified,
 * usually to operate through a firewall. A given VM instance can only have one
 * codebase, therefore construction of a second instance will result in an
 * IllegalStateException being thrown by its constructor.<br><br>
 * <i>Note:</i> if the Remote class needs {@link gnu.cajo.invoke.Remote#config
 * configuration}, this must be done <b>before</b> binding any proxy serving item,
 * since the binding will use the server's name and port number.
 * 
 * @version 1.0, 01-Nov-99 Initial release
 * @author John Catherino
 */
public final class CodebaseServer extends Thread {
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
      jws = (
         "HTTP/1.0 200 OK\r\n"   +
         "Content-type: application/x-java-jnlp-file\r\n" +
         "Cache-control: no-store\r\n" +
         "Connection: close\r\n" +
         "Content-length: "
      ).getBytes(),
      top = (
         "<HTML><HEAD><TITLE>CaJo Proxy Viewer</TITLE>\r\n" +
         "<META NAME=\"description\" content=\"Graphical cajo proxy client\"/>\r\n" +
         "<META NAME=\"copyright\" content=\"Copyright ©1999 by John Catherino\"/>\r\n" +
         "<META NAME=\"author\" content=\"John Catherino\"/>\r\n" +
         "<META NAME=\"generator\" content=\"ProxyServer&#153\"/>\r\n" +
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
   private static ServerSocket ss;
   /**
    * This is the inbound {@link java.net.ServerSocket ServerSocket}
    * port number providing both the HTTP client tag and codebase jar service.
    * If the server is behind a firewall, this port, or its translated one,
    * must be made accessible from outside.  It can be zero, to use an
    * anonymous port; i.e. one selected by the OS from any ports available at
    * runtime. In that case, the port actually offered by the operating system
    * will be stored here automatically, following construction.
    */
   public static int port;
   /**
    * Construction will start up the server's codebase transport mechanism
    * on the specified port.
    * @param base The path and name of the file containing the codebase jar
    * file.  The server will first search for it in its own executable jar
    * file, if that fails, then it will check the local filesystem.
    * @param port The TCP port on which to serve the codebase, and client
    * applet. It can be zero, to use an anonymous port. If zero, the actual
    * port selected by the OS at runtime will be stored in the
    * {@link #port port} member.
    * @throws IOException If the HTTP socket providing the codebase and applet
    * tag service could not be created.
    * @throws IllegalStateException If a second instance of this class is
    * constructed, since each VM can have only one codebase server.
    */
   public CodebaseServer(String base, int port) throws IOException {
      if (ss == null) {
         ss = new ServerSocket(port, 50,
            InetAddress.getByName(Remote.getServerHost()));
         this.port = port == 0 ? ss.getLocalPort() : port;
         System.setProperty("java.rmi.server.codebase",
            "http://" + Remote.getClientHost() + ':' + port + '/' + base);
         start();
      } else throw new IllegalStateException("Codebase currently served");
   }
   /**
    * The server thread method, it will send the proxy codebase, and it will
    * also support installing the hosting {@link gnu.cajo.invoke.Client
    * Client} in a Java-enabled browser, or as a web start application via
    * JNLP.<br><br>
    * The format of a browser's proxy request URL one required, and
    * five optional parameters, utilizing the following format:<p><code>
    * http://serverHost[:serverPort]/[clientPort][:localPort][-proxyName][!]
    * </code><p>
    * Where the parameters have the following meanings:<ul>
    * <li><i>serverHost</i> The domain name, or IP address of the proxy server.
    * <li><i>serverPort</i> The port number of the applet/codebases service,
    * unspecified it will be 80.
    * <li><i>clientPort</i> The client's external port number on which the
    * remote proxy can be reached. It is often explicitly specified when the
    * client is behind a firewall.  Unspecified, it will be the same as the
    * localPort value, described next.
    * <li><i>localPort</i> The client's internal port number the remote proxy
    * can be reached. This may need to be specified if the client is using NAT
    * Unspecified, it will be selected anonymously by the client at runtime.
    * <li><i>proxyName</i> The registered name of the proxy serving item, by
    * default "main", however a single server can support multiple items.
    * <li><i>!</i> This operator causes the proxy to be sent using JNLP. This
    * will launch the proxy as an application on the client.</ul>
    * <p>To unspecify any optional item, simply omit it, from the URL, along
    * with its preceeding delimiter, if any.  The <u>order</u> of the arguments
    * must be maintained however.<p>
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
               if (itemName == null) os.write(bye);  // invalid request
               else if (itemName.endsWith(".jar")) { // code request
                  try {
                     InputStream ris =
                        getClass().getResourceAsStream(itemName);
                     if (ris == null) ris =
                        new FileInputStream('.' + itemName);
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
               } else if (itemName.indexOf('/', 1) == -1) { // URL request
                  try { // parse request arguments
                     int proxyPort = Remote.getClientPort();
                     int ia =
                        itemName.indexOf(':') != -1 ? itemName.indexOf(':') :
                        itemName.indexOf('-') != -1 ? itemName.indexOf('-') :
                        itemName.indexOf('!') != -1 ? itemName.indexOf('!') :
                        itemName.length();
                     int ib =
                        itemName.indexOf('-') != -1 ? itemName.indexOf('-') :
                        itemName.indexOf('!') != -1 ? itemName.indexOf('!') :
                        itemName.length();
                     int ic =
                        itemName.indexOf('!') != -1 ? itemName.indexOf('!') :
                        itemName.length();
                     String clientPort =
                        ia >  1 ? itemName.substring(     1, ia) : "0";
                     String localPort =
                        ib > ia ? itemName.substring(ia + 1, ib) : "0";
                     String proxyName =
                        ic > ib ? itemName.substring(ib + 1, ic) : "main";
                     String clientHost = s.getInetAddress().getHostAddress();
                     if (itemName.indexOf('!') == -1) { // applet request
                        byte iex[] = ( // used by Exploder:
                           "<PARAM NAME = \"clientHost\" VALUE = \"" + clientHost + "\">\r\n" +
                           "<PARAM NAME = \"clientPort\" VALUE = \"" + clientPort + "\">\r\n" +
                           "<PARAM NAME = \"localPort\"  VALUE = \"" + localPort  + "\">\r\n" +
                           "<PARAM NAME = \"proxyPort\"  VALUE = \"" + proxyPort  + "\">\r\n" +
                           "<PARAM NAME = \"proxyName\"  VALUE = \"" + proxyName  + "\">\r\n"
                        ).getBytes();
                        byte nav[] = ( // used by Navigator and Appletviewer:
                           "clientHost = " + clientHost + "\r\n" +
                           "clientPort = " + clientPort + "\r\n" +
                           "localPort  = " + localPort  + "\r\n" +
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
                     } else { // JNLP application request
                        String title = Remote.getClientHost() + ':' + Remote.getClientPort() + '/' + proxyName;
                        byte xml[] = (
                           "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" +
                           "<jnlp spec=\"1.0+\"\r\n" +
                           "  codebase=" + "\"http://" + Remote.getClientHost() + ':' + CodebaseServer.this.port + "\"\r\n" +
                           "  href=\"" + clientPort + ':' + localPort + '-'+ proxyName + "!\">\r\n" +
                           "  <information>\r\n" +
                           "    <title>CajoViewer - " + title + "</title>\r\n" +
                           "    <vendor>John Catherino</vendor>\r\n" +
                           "    <homepage href=\"https://cajo.dev.java.net\"/>\r\n" +
                           "    <description>Graphical cajo proxy client</description>\r\n" +
                           "  </information>\r\n" +
                           "  <resources>\r\n" +
                           "    <j2se version=\"1.2+\"/>\r\n" +
                           "    <jar href=\"client.jar\"/>\r\n" +
                           "  </resources>\r\n" +
                           "  <application-desc main-class=\"gnu.cajo.invoke.Client\">\r\n" +
                           "    <argument>//" + title + "</argument>\r\n" +
                           "    <argument>" + clientPort + "</argument>\r\n" +
                           "    <argument>" + clientHost + "</argument>\r\n" +
                           "    <argument>" + localPort  + "</argument>\r\n" +
                           "  </application-desc>\r\n" +
                           "</jnlp>"
                        ).getBytes();
                        byte len[] = (xml.length + "\r\n\r\n").getBytes();
                        os.write(jws);
                        os.write(len);
                        os.write(xml);
                     }
                  } catch(Exception x) { os.write(bye); }
               } else os.write(bye); // unsupported request
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
