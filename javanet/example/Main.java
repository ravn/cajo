package example;

import gnu.cajo.invoke.*;
import gnu.cajo.utils.*;
import java.text.DateFormat;

public class Main { // General purpose server startup pattern
   static ProxyLoader pl;
   static Multicast mc;
   static Invoke item;  // prevent the server from garbage collection?
   // optional arguments; previous must be declared, but not successive:
   // - http port for codebase service (default = 80)
   // - port for server communication (default = 1099)
   // - outside NAT server name (default = same as internal)
   // - local host interface (default = primary interface)
   // - outside NAT server port (default = same as internal)
   public static void main(String args[]) {
      try {
// configure the application:
         ProxyServer.port  = args.length > 0 ? Integer.parseInt(args[0]) : 80; 
         int serverPort    = args.length > 1 ? Integer.parseInt(args[1]) : 1099; 
         String clientHost = args.length > 2 ? args[2] : null;
         String serverHost = args.length > 3 ? args[3] : null;
         int clientPort    = args.length > 4 ? Integer.parseInt(args[4]) : 0;
         Remote.config(serverHost, serverPort, clientHost, clientPort);
         pl = new ProxyLoader("/example/include/proxy.ser");
         Multicast mc = new Multicast();
// monitor the item, just for fun:
         item = new MonitorItem(new TestItem(), System.out);
// here's the crux:
         ProxyServer.bind(item, "main", true, mc, pl);
// multicast our startup, just for fun:
         mc.announce(ProxyServer.defaultServer, 16);
// listen for announcements, just for fun:
         mc.listen(new Invoke() {  // any announcers will receive a proxy
            public Object invoke(String method, Object args) {
               try { ((Multicast)args).item.invoke("proxyItem", pl); }
               catch(Exception x) {}
               return null;
            }
         });
// lots of info, just for fun:
         System.out.println("\nServer started: " +
            DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL).
               format(new java.util.Date()));
         System.out.print("http on internal port\t");
         System.out.println(ProxyServer.port);
         System.out.print("internally operating on\t");
         System.out.print(Remote.getServerHost());
         System.out.print(" port ");
         System.out.println(Remote.getServerPort());
         System.out.print("externally operating on\t");
         System.out.print(Remote.getClientHost());
         System.out.print(" port ");
         System.out.println(Remote.getClientPort());
         System.out.println();
      } catch (Exception x) { x.printStackTrace(System.err); }
   }
}
