package gnu.cajo.utils;

import java.io.*;
import java.net.*;
import gnu.cajo.invoke.Remote;

/*
 * RMI Codebase and Graphical Proxy Server
 * Copyright (c) 1999 John Catherino
 * The cajo project: https://cajo.dev.java.net
 *
 * For issues or suggestions mailto:cajo@dev.java.net
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, at version 2.1 of the license, or any
 * later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You can receive a copy of the GNU Lesser General Public License from their
 * website, http://fsf.org/licenses/lgpl.html; or via snail mail, Free
 * Software Foundation Inc., 51 Franklin Street, Boston MA 02111-1301, USA
 */

/**
 * The standard mechanism to send proxies, and other complex objects to
 * remote VMs. It requires one outbound port. The port can be anonymous, i.e.
 * selected from any available free port at runtime, or it can be explicitly
 * specified, usually to operate through a firewall. A given VM instance can
 * only have one codebase, therefore construction of a second instance will
 * result in an IllegalStateException being thrown by its constructor. It
 * also provides the generic graphical proxy host client service, as an
 * Applet, and via WebStart.
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
    end = (
       "PLUGINSPAGE=\"http://java.sun.com/j2se/1.5.0/download.html\">\r\n" +
       "</EMBED></COMMENT></OBJECT></CENTER></BODY></HTML>"
    ).getBytes(),
    out = (
       "  </application-desc>\r\n" +
       "</jnlp>"
    ).getBytes();
 private final byte[] top, mid, tip, xml;
 private final String thisJar;
 private final ServerSocket ss;
 private final boolean anyFile;
 /**
  * This is the inbound ServerSocket port number providing both the HTTP
  * client tag and codebase jar service. If the server is behind a firewall,
  * this port, must be made accessible from outside. If the port argument
  * used in the constructor was zero, it will use an anonymous port; i.e. one
  * selected by the OS from any ports available at runtime. In that case, the
  * port actually offered by the operating system will be stored here
  * automatically, following construction.
  */
 public final int serverPort;
 /**
  * This is the inbound ServerSocket port number providing both the HTTP
  * client tag and codebase jar service. If the server is behind a firewall,
  * this port, must be made accessible from outside. If the port argument
  * used in the constructor was zero, it will use an anonymous port; i.e. one
  * selected by the OS from any ports available at runtime. In that case, the
  * port actually offered by the operating system will be stored here
  * automatically, following construction. <i><u>Note</u>:</i> The preferred
  * field to check the CodebaseServer port is <tt>serverPort</tt> this field
  * remains purely to maintain backward compatibility.
  * @deprecated
  */
 public static int port;
 /**
  * The main constructor will start up the server's codebase transport
  * mechanism on the specified port. To shut the service down, call its
  * inherited interrupt method. All other constructors in this class call it.
  * @param jars An array of strings representing the path and filename of
  * a library jar used by the client application. The CodebaseServer will
  * serve them to the remote JVM.* The server will first search for the jar
  * in its own executable jar file, if that fails, then it will check the
  * local filesystem.<p>
  * The server determines the name of the jar file in which it is running
  * courtesy of a very cool hack published by Laird Nelson in his
  * weblog: http://weblogs.java.net/pub/wlg/1874 Thanks Laird! This is
  * used to allow the server to serve all the jar files in its working
  * directory tree <i>except</i> its own.<p>
  * <i><u>Note</u>:</i> if this value is null, it indicates that the proxy
  * codebase is <i>not</i> in a jar. The server will then look first in its
  * own jar file for the class files to send, and if not found, it will
  * next look in its working directory. This feature provides an extremely
  * simple, essentially zero-configuration, approach to proxy codebase
  * service. It also provides complete general-purpose web service as well,
  * supporting documentaions pages, images, and even a favicon.ico.
  * @param port The TCP port on which to serve the codebase, and client
  * applet. It can be zero, to use an anonymous port. If zero, the actual
  * port selected by the OS at runtime will be stored in the
  * {@link #serverPort serverPort} member.
  * @param client The name of the graphical client class to be furnished as
  * an Applet, or via WebStart. For example, the generic cajo standard
  * graphical proxy is: <tt>gnu.cajo.invoke.Client</tt>
  * @throws IOException If the HTTP socket providing the codebase and
  * applet tag service could not be created.
  * @param title The application specific titile to show in the browser,
  * when running as an applet.
  * @throws IllegalStateException If a instance of this class already exists,
  * since each JVM can have only one codebase server.
  */
 public CodebaseServer(String jars[], int port, String client, String title)
    throws IOException {
    if (System.getProperty("java.rmi.server.codebase") == null) {
       String temp = client.replace('.', '/') + ".class";
       if (title == null) title = "cajo Proxy Viewer";
       StringBuffer base = new StringBuffer("client.jar");
       if (jars != null) {
          for (int i = 0; i < jars.length; i++) {
             base.append(", ");
             base.append(jars[i]);
          }
       } // create instance specific response data:
       top = (
          "<HTML><HEAD><TITLE>" + title + "</TITLE>\r\n" +
          "<META NAME=\"description\" content=\"Graphical cajo proxy client\"/>\r\n" +
          "<META NAME=\"copyright\" content=\"Copyright (c) 1999 John Catherino\"/>\r\n" +
          "<META NAME=\"author\" content=\"John Catherino\"/>\r\n" +
          "<META NAME=\"generator\" content=\"ProxyServer\"/>\r\n" +
          "</HEAD><BODY leftmargin=\"0\" topmargin=\"0\" marginheight=\"0\" marginwidth=0 rightmargin=\"0\">\r\n" +
          "<CENTER><OBJECT classid=\"clsid:8AD9C840-044E-11D1-B3E9-00805F499D93\"\r\n" +
          "WIDTH=\"100%\" HEIGHT=\"100%\"\r\n" +
          "CODEBASE=\"http://java.sun.com/products/plugin/autodl/jinstall-1_5_0-windows-i586.cab#Version=1,5,0,0\">\r\n" +
          "<PARAM NAME=\"archive\" VALUE=\"" + base.toString() + "\">\r\n" +
          "<PARAM NAME=\"type\" VALUE=\"application/x-java-applet;version=1.5\">\r\n" +
          "<PARAM NAME=\"code\" VALUE=\"" + temp + "\">\r\n"
       ).getBytes();
       mid = (
          "<COMMENT><EMBED type=\"application/x-java-applet;version=1.5\"\r\n" +
          "ARCHIVE=\"" + base.toString() + "\"\r\n" +
          "CODE=\"" + temp + "\"\r\n" +
          "WIDTH=\"100%\" HEIGHT=\"100%\"\r\n"
       ).getBytes();
       temp = CodebaseServer.class.getName().replace('.', '/') + ".class";
       temp = CodebaseServer.class.getClassLoader().getResource(temp).toString();
       if (temp.indexOf('!') != -1) {
          temp = temp.substring(temp.lastIndexOf(':'), temp.lastIndexOf('!'));
          temp = temp.substring(temp.lastIndexOf('/') + 1);
       } else temp = "\""; // server not in a jar file
       thisJar = temp;
       ss = Remote.getServerHost() == null ? new ServerSocket(port) :
          new ServerSocket(port, 50, InetAddress.getByName(Remote.getServerHost()));
       serverPort = port == 0 ? ss.getLocalPort() : port;
       CodebaseServer.port = serverPort; // legacy
       tip = (
          "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n" +
          "<jnlp spec=\"1.0+\"\r\n" +
          "  codebase=" + "\"http://" + Remote.getClientHost() + ':' + serverPort + "\"\r\n"
       ).getBytes();
       base = new StringBuffer(
          "  <information>\r\n" +
          "    <title>" + title + "</title>\r\n" +
          "    <shortcut><desktop/></shortcut>\r\n" +
          "    <vendor>John Catherino</vendor>\r\n" +
          "    <homepage href=\"https://cajo.dev.java.net\"/>\r\n" +
          "    <description>Graphical cajo proxy client</description>\r\n" +
          "  </information>\r\n" +
          "  <resources>\r\n" +
          "    <j2se version=\"1.5+\"/>\r\n" +
          "    <jar href=\"client.jar\"/>\r\n"
       );
       if (jars != null)
          for (int i = 0; i < jars.length; i++)
             base.append("    <jar href=\"" + jars[i] + "\" download=\"lazy\"/>\r\n");
       base.append("  </resources>\r\n");
       base.append("  <application-desc main-class=\"");
       base.append(client);
       base.append("\">\r\n");
       xml = base.toString().getBytes();
       base = new StringBuffer();
       String loc = "http://" + Remote.getClientHost() + ':' + CodebaseServer.port + '/';
       if (jars != null) {
          for (int i = 0; i < jars.length; i++) {
             base.append(loc);
             base.append(jars[i]);
             if (i < jars.length - 1) base.append(' ');
          }
       } else base.append(loc);
       if (System.getProperty("java.rmi.server.codebase") != null) {
          base.append(' ');
          base.append(System.getProperty("java.rmi.server.codebase"));
       }
       System.setProperty("java.rmi.server.codebase", base.toString());
       anyFile = jars == null;
       start(); // ready to accept clients
    } else throw new IllegalStateException("Codebase currently served");
 }
 /**
  * This constructor will start up the server's codebase transport mechanism
  * on the specified port, using the specified codebase jar file, with the
  * specified clent. It will invoke the four argument constructor, with
  * the default client title.
  * @param base The path and name of the file containing the proxy codebase
  * jar file.
  * @param port The TCP port on which to serve the codebase, and client
  * applet.
  * @param client The name of the graphical client class to be furnished as
  * an Applet, or via WebStart.
  * @throws IOException If the HTTP socket providing the codebase and
  * applet tag service could not be created.
  * @throws IllegalStateException If a instance of this class already exists,
  * since each JVM can have only one codebase server.
  */
 public CodebaseServer(String base, int port, String client)
    throws IOException {
    this(base != null ? new String[] { base } : null, port, client, null);
 }
 /**
  * This constructor simply calls the main four parameter constructor,
  * providing the standard cajo generic graphical client as the client
  * argument.
  * @param base The path and name of the file containing the proxy codebase
  * jar file.
  * @param port The TCP port on which to serve the codebase, and client
  * applet.
  * @throws IOException If the HTTP socket providing the codebase and
  * applet tag service could not be created.
  * @throws IllegalStateException If a instance of this class already exists,
  * since each JVM can have only one codebase server.
  */
 public CodebaseServer(String base, int port) throws IOException {
    this(base != null ? new String[] { base } : null, port,
       "gnu.cajo.invoke.Client", null);
 }
 /**
  * The server thread method, it will send the proxy codebase, and it will
  * also support installing the hosting {@link gnu.cajo.invoke.Client
  * Client}, or application specific host, in a Java-enabled browser, or
  * as a web start application via JNLP.<p>
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
  * client is behind a firewall, or is using port translation.  Unspecified,
  * it will be the same as the localPort value, described next.
  * <li><i>localPort</i> The client's internal port number on which the
  * remote proxy can be reached. Unspecified, it will be selected
  * anonymously by the client at runtime.
  * <li><i>proxyName</i> The registered name of the proxy serving item, by
  * default "main", however a single server can support multiple items.
  * <li><i>!</i> This operator causes the proxy to be sent using JNLP. This
  * will launch the proxy as an application on the client.</ul>
  * <p>To unspecify any optional item, simply omit it, from the URL, along
  * with its preceeding delimiter, if any.  The <u>order</u> of the
  * arguments must be maintained however.<p>
  * <i>Note:</i> other item servers can share this instance, by placing
  * their proxy classes or jar files in the same working directory.
  * However, those item servers will not be able to use the client service
  * feature, as it is unique to the VM in which the CodebaseServer is
  * running.<p>
  * As a safety precaution, the server will send any requested file in
  * or below its working directory <i>except</i> the jar file of the server
  * itself. Typically people do not want to give this file out.
  */
 public void run() {
    try {
       byte msg[] = new byte[0xf000];
       while(!isInterrupted()) {
          Socket s = ss.accept();
          try {
             InputStream  is = s.getInputStream();
             OutputStream os = new BufferedOutputStream(s.getOutputStream(), 0x8000);
             int ix = is.read(msg);
             String itemName = null;
             scan: for (int i = 0; i < ix; i++) { // scan client request
                if (msg[i] == '/') {
                   for (int j = i + 1; j < msg.length; j++) {
                      if (msg[j] == ' ') {
                         itemName = new String(msg, i, j - i);
                         break scan;
                      }
                   }
                }
             }
             if (itemName == null) os.write(bye); // invalid request
             else if (itemName.indexOf('.') == -1 && itemName.indexOf('/', 1) == -1) {
                try { // URL request: parse arguments
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
                   if (itemName.indexOf('!') == -1) { // Applet request
                      byte iex[] = ( // used by Exploder:
                         "<PARAM NAME=\"clientHost\" VALUE=\"" + clientHost + "\">\r\n" +
                         "<PARAM NAME=\"clientPort\" VALUE=\"" + clientPort + "\">\r\n" +
                         "<PARAM NAME=\"localPort\"  VALUE=\"" + localPort  + "\">\r\n" +
                         "<PARAM NAME=\"proxyPort\"  VALUE=\"" + proxyPort  + "\">\r\n" +
                         "<PARAM NAME=\"proxyName\"  VALUE=\"" + proxyName  + "\">\r\n"
                      ).getBytes();
                      byte nav[] = ( // used by Navigator and Appletviewer:
                         "clientHost=\"" + clientHost + "\"\r\n" +
                         "clientPort=\"" + clientPort + "\"\r\n" +
                         "localPort=\""  + localPort  + "\"\r\n" +
                         "proxyPort=\""  + proxyPort  + "\"\r\n" +
                         "proxyName=\""  + proxyName  + "\"\r\n"
                      ).getBytes();
                      byte len[] = (top.length + iex.length + mid.length + nav.length + end.length + "\r\n\r\n").getBytes();
                      os.write(tag);
                      os.write(len);
                      os.write(top);
                      os.write(iex); // return client specific applet page
                      os.write(mid);
                      os.write(nav);
                      os.write(end);
                   } else { // WebStart request
                      byte obj[] = ("  href=\"" + clientPort + ':' + localPort + '-' + proxyName + "!\">\r\n").getBytes();
                      byte arg[] = (
                         "    <argument>//" + Remote.getClientHost() + ':' + proxyPort + '/' + proxyName + "</argument>\r\n" +
                         "    <argument>" + clientPort + "</argument>\r\n" +
                         "    <argument>" + clientHost + "</argument>\r\n" +
                         "    <argument>" + localPort  + "</argument>\r\n"
                      ).getBytes();
                      byte len[] = (tip.length + obj.length + xml.length + arg.length + out.length + "\r\n\r\n").getBytes();
                      os.write(jws);
                      os.write(len);
                      os.write(tip);
                      os.write(obj); // return client specific jnlp document
                      os.write(xml);
                      os.write(arg);
                      os.write(out);
                   }
                } catch(Exception x) { os.write(bye); }
             } else if (anyFile || (itemName.endsWith(".jar") && !itemName.endsWith(thisJar))) {
                try {  // file request: send contents
                   int flen;
                   InputStream ris = getClass().getResourceAsStream(itemName);
                   if (ris == null) { // read from outside server jar
                      File file = new File('.' + itemName);
                      flen = (int)file.length();
                      ris = new FileInputStream(file);
                   } else flen = ris.available();
                   os.write(jar);
                   os.write((flen + "\r\n\r\n").getBytes());
                   for (int i = ris.read(msg); i != -1; i = ris.read(msg))
                      os.write(msg, 0, i);
                   ris.close();
                } catch(Exception x) { os.write(bye); }
             } else os.write(bye); // no other requests are honored
             os.flush();
             os.close(); // terminate client connection
             is.close();
          } catch (Exception x) { x.printStackTrace(); }
          try { s.close(); }
          catch (Exception x) { x.printStackTrace(); }
       }
    } catch(Exception x) { x.printStackTrace(); }
    try { ss.close(); }
    catch(Exception x) { x.printStackTrace(); }
 }
 /**
  * The application creates a utility server to share any files in
  * the in its working directory and subdirectories. It is extremely
  * useful for application development. If a port number is provided
  * as an argument, it will be used, otherwise it will be opened on
  * an anonymous port.<p>
  * <i><u>Note</u>:</i> in the Virtual Machine sharing its objects,
  * its system property <tt>rmi.server.codebase</tt> will have to be set
  * manually, to point to this host and port number.
  */
 public static void main(String args[]) {
    try {
       CodebaseServer c =  args.length == 0 ? new CodebaseServer(null, 0) :
          new CodebaseServer(null, Integer.parseInt(args[0]));
       System.out.println("Codebase service on port " + c.serverPort);
    } catch(IOException x) { x.printStackTrace(); }
 }
}
