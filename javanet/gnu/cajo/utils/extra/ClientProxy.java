package gnu.cajo.utils.extra;

import gnu.cajo.invoke.*;

/*
 * Callback proxy for a firewalled client, used by a server item
 * Copyright (c) 2004 John Catherino
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
 * To receive a copy of the GNU Lesser General Public License visit their
 * website at http://www.fsf.org/licenses/lgpl.html or via snail mail at Free
 * Software Foundation Inc., 59 Temple Place Suite 330, Boston MA 02111-1307
 * USA
 */

/**
 * This class is used to send server item callbacks to a firewalled client.
 * A client whose firewall settings prohibit incoming socket connections is a
 * common problem. This class is called ClientProxy, as it is a stand-in
 * representation of a remote reference to a client, behind a firewall. This
 * allows server objects to be designed without regard to client firewall
 * issues. An intermediary process would give the server item a real remote
 * reference to the client object when there is no client firewall, or a
 * ClientProxy when there is. The client links the remote reference to this
 * ClientProxy, to the locally firewalled client, using an {@link ItemProxy
 * ItemProxy} object. The server item invokes methods on this client proxy
 * which result in an immediate callback invocation on the client. <p>
 * <i>Note:</i> this paradigm is <u>not</u> threadsafe! It is expected that
 * callbacks to the remote client will <i>not</i> be invoked reentrantly.
 * Correspondingly, a unique instance of this object must be given to each
 * remote client object.
 *
 * @version 1.0, 28-Mar-04 Initial release
 */
public final class ClientProxy implements Invoke {
   private String method;
   private Object args;
   /**
    * A server creates this object, then provides a remote reference to it
    * to the client. This creates the first half of the bridge, the {@link
    * ItemProxy ItemProxy} class completes the second half.
    */
   public ClientProxy() {}
   /**
    * This method serves two fundamentally different, but symmetrical
    * purposes. Initially a remote {@link ItemProxy ItemProxy} calls this
    * method to have its calling thread blocked until the server item needs
    * to make an asynchronous callback. Secondly, the server item will also
    * invoke this method, and will have its thread blocked, until the
    * resulting data, or exception, is returned from the firewalled client,
    * via its ItemProxy.
    * @param method The name of the method on the firewalled remote client
    * to be invoked asynchronously.
    * @param args The data to be provided the method of the callback method,
    * <i>or</i> data resulting from the client callback.
    * @return The result of the client object callback.
    * @throws Exception For any client specific reasons.
    */
   public synchronized Object invoke(String method, Object args)
      throws Exception {
      if (method == null) {     // client callback response thread
         this.args = args;      // save the callback result
         notify();              // wake the server item thread
         wait();                // suspend the client callback thread
         return new Object[] { this.method, this.args };
      } else {                  // server callback invocation thread
         this.method = method;  // save the client method to be invoked
         this.args   = args;    // save the data to provide the invocation
         notify();              // wake the client callback thread
         wait();                // suspend the server item thread
         if (this.args instanceof Exception) throw (Exception)this.args;
         return this.args;      // return the callback result
      }
   }
}
