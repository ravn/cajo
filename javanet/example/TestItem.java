package example;

import gnu.cajo.invoke.Invoke;

public class TestItem extends gnu.cajo.utils.BaseItem {
   Invoke proxy;
   public void run() {
      while (!thread.isInterrupted()) try {
         synchronized(thread) { thread.wait(); }
         thread.sleep(500);
         System.out.print("\nProxy async call, result = ");
         System.out.println(proxy.invoke("callback", "Goodbye from server!"));
      } catch(Exception x) { x.printStackTrace(System.err); }
   }
   public String callback(Invoke proxy, String message) {
      this.proxy = proxy;
      System.out.print("\nProxy async callback from ");
      try { System.out.print(java.rmi.server.RemoteServer.getClientHost()); }
      catch(java.rmi.server.ServerNotActiveException x) {
         System.out.print("local item");
      }
      System.out.println(", message = " + message);
      System.out.println();
      synchronized(thread) { thread.notify(); }
      return "Server sync acknowledgement!";
   }
   public String toString() { return "Test Item"; }
}