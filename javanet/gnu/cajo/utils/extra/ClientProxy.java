package gnu.cajo.utils.extra;

import gnu.cajo.invoke.*;

/*
 * Callback proxy for a firewalled client, used by a server item
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
 * This class is used to send server item callbacks to a firewalled client.
 * A client whose firewall settings prohibit incoming socket connections is a
 * common problem. The server item creates a ClientProxy to represent the
 * remote client, and on which it may invoke callbacks. It calls the
 * getItemProxy method once, to create an object, given to the client, on which
 * it can can receive the server callbacks. Its development was championed by
 * project member Fredrik Larsen.
 *
 * @version 1.0, 28-Mar-04 Initial release
 */
public final class ClientProxy implements Invoke {
   private final long interval;
   private ItemProxy item;
   private boolean responded;
   private String method;
   private Object args;
   /**
    * The constructor creates an object on which the server item may invoke
    * proxy callbacks.
    * @param interval The time to wait, in milliseconds, for a response from
    * the proxy item. The proxy will have approximately half of this time in
    * which to respond. A value less than 100 is not likely to work correctly.
    */
   public  ClientProxy(long interval) throws java.rmi.RemoteException {
      this.interval = interval;
      item = new ItemProxy(new Remote(this), interval / 2);
   }
   /**
    * This method is used to return an object on which the remote item may
    * receive callbacks from the server. It can be called only once, as this
    * object is unique between each item and its client. The returned item
    * proxy will have a polling interval approximately half this object's
    * timeout interval.
    * @return A proxy on which the client may install a listener for the server
    * item's callback invocations.
    * @throws IllegalStateException If the method is called more than once, as
    * its existence is unique to a specific remote client.
    */
   public synchronized Object getItemProxy() {
      if (item == null) throw new IllegalStateException("Item already used");
      Object o = item;
      item = null;
      return o;
   }
   /**
    * This method is used by the server to callback a firewalled item. It will
    * store the method and args arguments, then wait up to its timeout interval
    * for a response from the remote client. The client item, upon processing
    * of this method will return the result, and wake the calling thread.
    * @return The result of the proxy callback.
    * @throws Exception For any client specific reasons.
    * @throws InterruptedException If the operation timed out.
    */
   public synchronized Object invoke(String method, Object args)
      throws Exception {
      this.method = method;
      this.args = args;
      responded = false;
      wait(interval);
      if (!responded) throw new InterruptedException("Invocation Timeout");
      if (args instanceof Exception) throw (Exception)args;
      return args;
      
   }
   /**
    * This method is polled by the client proxy item at approximately half the
    * maximum waiting interval, to see if the item is invoking a callbak on the
    * proxy.
    * @return A two-element array containing the method name string, and the
    * argument object. Otherwise null, if the item has made no callback.
    */
   public synchronized Object getData() {
      if (method != null) {
         args = new Object[] { method, args };
         method = null;
         return args;
      } else return null;
   }
   /**
    * This method is called by the client item proxy, to provide the resulting
    * data from the item callback invocation. It will wake the waiting item
    * callback thread.
    * @param data The data resulting from the client callback, or the
    * exception, as applicable.
    */
   public synchronized void setData(Object data) {
      responded = true;
      args = data;
      notify();
   }
}
