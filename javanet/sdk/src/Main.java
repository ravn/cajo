/*
 * A typical startup module for a cajo grail server.
 * The cajo project: https://cajo.dev.java.net
 * For issues or suggestions mailto:cajo@dev.java.net
 * This class is released into the public domain.
 * Written by John Catherino 01-Dec-09
 */

/**
 * This class configures the JVM for network operation, and furnishes any
 * selected service objects, for remote client use. The specific package in
 * which this class resides can be framework specific.
 * @author <a href=http://wiki.java.net/bin/view/People/JohnCatherino>
 * John Catherino</a>
 */
public final class Main {
   private Main() {}
   /**
    * The application creates a <a href=https://cajo.dev.java.net>cajo</a>
    * graphical service furnishing JVM. It will create and assign services
    * to registry names in code.<p>
    * @param args The startup can take up to four <i>optional</i>
    * configuration parameters, in order: (i.e. all previous options must
    * be provided)<ul>
    * <li><tt>args[0] - </tt>The server port number, if using NAT.
    * <i>(typically 1198)</i>
    * <li><tt>args[1] - </tt>The external client host name, if using NAT.
    * <li><tt>args[2] - </tt>The internal client host name, if multi home/NIC.
    * <li><tt>args[3] - </tt>The http server port number. <i>(typically 80)</i>
    * </ul><p>
    * If this approach should prove too rigid, use of a properties file for
    * example, of <tt>registryName=Class.Name</tt>, could also work well.
    * @throws Exception should the server startup fail, usually for network
    * configuration related issues
    */
   public static void main(final String args[]) throws Exception {
      gnu.cajo.Cajo.main(null); // optional, but just to be polite ;-)

      // get configuration parameters, in this case from the command line...
      final int port = args.length > 0 ? Integer.parseInt(args[0]) : 1198; 
      final String clientHost = args.length > 1 ? args[1] : null;
      final String serverHost = args.length > 2 ? args[2] : null;
      final int httpPort = args.length > 3 ? Integer.parseInt(args[3]) : 80;

      // now configure the server...
      new gnu.cajo.utils.CodebaseServer( // configure codebase service
         // these are the list of jars needed exclusively by the controller
         new String[] { "client.jar", "controller.jar", "grail.jar" },
         httpPort, "util.Client", // set the server port, and client class
         "Example cajo graphical proxy", // web page title
         "The cajo project", // company identification
         "icon.gif", "splash.jpeg" // icon & splash screen
      ); // start codebase service first, then create the cajo object!
      util.BaseService.cajo = new gnu.cajo.Cajo(port, serverHost, clientHost);
      // Important: comment the next line, unless you trust the services
//      gnu.cajo.utils.ItemServer.acceptProxies(); // allow controllers here?

      // create & name services...
      new service.TestService("main");

      // finally output a little startup information...
      System.out.print("server started ");
      System.out.println(java.text.DateFormat.getDateTimeInstance(
         java.text.DateFormat.FULL, java.text.DateFormat.FULL).
            format(new java.util.Date()));
      System.out.println();
      System.out.print("internally operating on ");
      System.out.println(gnu.cajo.invoke.Remote.getDefaultServerHost());
      System.out.print("externally operating on ");
      System.out.println(gnu.cajo.invoke.Remote.getDefaultClientHost());
      System.out.print("services using TCP port ");
      System.out.println(gnu.cajo.invoke.Remote.getDefaultServerPort());
      System.out.print("http server on TCP port ");
      System.out.println(httpPort);
      System.out.println();
   }
}
