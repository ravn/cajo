/*
 * A typical startup module for a cajo grail server.
 * The cajo project: http://cajo.java.net
 * For issues or suggestions mailto:cajo@dev.java.net
 */

/**
 * This example class configures the JVM for network operation, and furnishes
 * service objects for remote client use. It has no main application program.
 * @see cajo.sdk.AbstractService
 * @see cajo.sdk.AbstractController
 * @see cajo.sdk.AbstractAgent
 */
public final class Server extends cajo.sdk.AbstractServer {
   /**
    * The application creates a <a href=https://cajo.dev.java.net>cajo</a>
    * server. It has an optional graphical client that can be viewed in one
    * of three ways:<p><ul>
    * <li>In the browser, by typing the server's <tt>address:port/name</tt>
    * <li>Via WebStart, by typing server's <tt>address:port/name/!</tt> in
    * the browser, or providing it as a hyperlink in another document
    * <li>Via the client.jar application, by providing
    * <tt>//address:port[typically 1198]/name</tt> as its command line
    * argument</ul><br>
    * The default graphical client is named "main" and in that case, its name
    * can be omitted from the address if you wish. See the build.xml file for
    * a good example for launching the client.
    * @param args The startup can take up to four <i>optional</i>
    * configuration parameters, in order: (i.e. all previous options must
    * be provided)<br><ul>
    * <li><tt>args[0] - </tt>The http server inbound port number.
    * <i>(typically 80),</i> it will need to be opened if a firewall is in
    * use
    * <li><tt>args[1] - </tt>The server inbound port number <i>(typically
    * <a href=https://cajo.dev.java.net/servlets/NewsItemView?newsItemID=2539>
    * 1198</a>),</i> it will need to be opened if a firewall is in use
    * <li><tt>args[2] - </tt>The external client host address/name for this,
    * server, if using NAT.
    * <li><tt>args[3] - </tt>The internal host address/name for this server,
    * if multi-homed or multi-NIC.</ul>
    * <p>If this approach should prove too rigid, use of a properties file for
    * example, of <tt>registryName=Class.Name</tt>, could also work well,
    * possibly a GUI could be used, to request this information from the user.
    * <p><i><u>NB</u>:</i> If a server wishes to allow controllers or agents
    * to run inside its JVM, via <tt>ItemServer.acceptProxies()</tt>it is
    * <i>highly recommended</i> to start the server with the following two
    * -D arguments:<ul>
    * <li>-Djava.security.manager (lock the JVM into a sandbox)
    * <li>-Djava.security.policy=service.policy (define controller/agent
    * permissions)</ul>
    * <br>*A functional example service.policy file is provided with the SDK,
    * see the build.xml <tt>startserver</tt> target for launch guidance.
    * @throws Exception should the server startup fail, usually for network
    * configuration related issues
    */
   @SuppressWarnings("unused") // looks like services are unused, but they are
   public static void main(String args[]) throws Exception {

//      cajo.sdk.Logger.DEBUG = true; // force all loggers on, for debugging

      // get configuration parameters, in this case from the command line...
      int httpPort = args.length > 0 ? Integer.parseInt(args[0]) : 80;
      int port = args.length > 1 ? Integer.parseInt(args[1]) : 1198; 
      String publicHost = args.length > 2 ? args[2] : 
         java.net.InetAddress.getLocalHost().getHostAddress();
      String localHost = args.length > 3 ? args[3] : "0.0.0.0";

      // start the optional codebase server for client/controllers/agents
      codebaseServer = new gnu.cajo.utils.CodebaseServer(
         new String[] { "client.jar", "grail.jar" }, httpPort,
         "cajo.sdk.Client",
         "Example cajo graphical viewer", // frame title
         "The cajo project", // company identification
         "icon.gif", "splash.jpeg" // icon & splash screen
      );
      export("controller.jar", "view.jar", "agent.jar"); // needed by clients

      cajo = new gnu.cajo.Cajo(); // run cajo registrar on anonymous port

      // configure the cajo TCP network interface
      gnu.cajo.invoke.Remote.config(localHost, port, publicHost, port);

//      cajo = new gnu.cajo.Cajo(); // run cajo registrar on service port

// Important: comment next optional line, unless you trust all of the users
//      gnu.cajo.utils.ItemServer.acceptProxies(); // accept moblie code?

      // create and name services...
      new service.Service("main", null); // main = default service

      // optionally output a little startup information...
      System.out.print("\nserver started ");
      System.out.println(java.text.DateFormat.getDateTimeInstance(
         java.text.DateFormat.FULL, java.text.DateFormat.FULL).
            format(new java.util.Date()));
      System.out.println("internally operating on " + localHost);
      System.out.println("externally operating on " + publicHost);
      System.out.println("services using TCP port " + port);
      System.out.println("http server on TCP port " + httpPort);

      // The JVM is now free to be any program it wants locally as well.
      // Naturally, it can use its local services, they are simply objects.
      // It can use its cajo object to find remote services to use.
      // As always, having a main application, is completely optional.
                                               
   }
}
