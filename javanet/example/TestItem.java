package example;

import gnu.cajo.invoke.Invoke;
import gnu.cajo.utils.BaseItem;

// Example server item.  It doesn't do much, except illustrate one way to
// construct them.
public class TestItem extends BaseItem {
   Invoke proxy;
   public TestItem() {
      thread = new Thread( // the item's main processing thread
         new MainThread() {
            public void run() {
               while (!thread.isInterrupted()) try { // excellent practice!
                  synchronized(thread) { thread.wait(); }
                  thread.sleep(500);
                  System.out.print("\nProxy async call, result = ");
                  System.out.println(
                     proxy.invoke("callback", "Goodbye from server!"));
               } catch(Exception x) { x.printStackTrace(System.err); }
            }
         }
      );
      thread.start(); // it does nothing until started.
   }
   // All of the item's public methods are remotely callable, just as if the
   // object were local.  Below is the interface created by this object:
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
   // All items should uniquely identify themselves, but it is not required.
   public String toString() { return "Test Item"; }
}