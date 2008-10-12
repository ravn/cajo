package example;

import gnu.cajo.invoke.Remote;
import gnu.cajo.utils.*;
import java.text.DateFormat;

public class Main { // General purpose server startup pattern
   // optional arguments; previous must be declared, but not successive:
   // - http port for codebase service (default = 80)
   // - port for server communication (default = 1198)
   // - outside NAT server name (default = same as internal)
   // - local host interface (default = primary interface)
   // - outside NAT server port (default = same as internal)
   public static void main(String args[]) throws Exception {
   // configure the application:
      int httpPort      = args.length > 0 ? Integer.parseInt(args[0]) : 80; 
      int serverPort    = args.length > 1 ? Integer.parseInt(args[1]) : 1198; 
      String clientHost = args.length > 2 ? args[2] : null;
      String serverHost = args.length > 3 ? args[3] : null;
      int clientPort    = args.length > 4 ? Integer.parseInt(args[4]) : 0;
      Remote.config(serverHost, serverPort, clientHost, clientPort);
      final ProxyLoader pl = new ProxyLoader("/example/include/proxy.ser");
//      Multicast mc = new Multicast();
   // monitor the item, just for fun:
      MonitorItem mi = new MonitorItem(new TestItem(), System.out);
   // here's the crux:
      Remote item = ItemServer.bind(mi, "main", pl);
   // start up the codebase and applet service: (after remoting item)
      CodebaseServer cs = new CodebaseServer("proxy.jar", httpPort);
   // multicast our startup, just for fun:
   // The monitor will log a startThread exception, normal for calling twice.
//      mc.announce(item, 16);
   // accept proxies, just for fun:
//      ItemServer.acceptProxies();
   // listen for announcements, just for fun:
//      mc.listen(new Object() {  // any announcers will receive a proxy
//         public Object multicast(Multicast m) {
//            try { m.item.invoke("setProxy", pl); }
//            catch(Exception x) {} // if it supports the method
//            return null; // continue listening forever...
//         }
//      });
   // lots of info, just for fun:
      System.out.println("\nServer started: " +
         DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL).
            format(new java.util.Date()));
      System.out.print("http on internal port\t");
      System.out.println(cs.serverPort);
      System.out.print("internally operating on\t");
      System.out.print(Remote.getDefaultServerHost());
      System.out.print(" port ");
      System.out.println(Remote.getDefaultServerPort());
      System.out.print("externally operating on\t");
      System.out.print(Remote.getDefaultClientHost());
      System.out.print(" port ");
      System.out.println(Remote.getDefaultClientPort());
      System.out.println();
   }
}
