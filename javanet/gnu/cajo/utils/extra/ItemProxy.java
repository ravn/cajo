package gnu.cajo.utils.extra;

import gnu.cajo.invoke.*;

/*
 * Callback proxy for a remote item, used by a firewalled client
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
 * This class is used to receive server item callbacks by a firewalled
 * client. A client whose firewall settings prohibit incoming socket
 * connections is a common problem. To solve this, a client would request
 * a remote reference to a {@link ClientProxy ClientProxy} from the server.
 * It would then use an ItemProxy to link the remote item to the local client
 * item. This class is a special purpose thread, which will make an outgoing
 * call to the remote ClientProxy. This outgoing call will be blocked until
 * the server has some data for it, at which point it will wake this thread,
 * causing it to return with the callback method to be invoked on the local
 * client object, and the data to be provided it. This object will call its
 * local client, and return the resulting data, or exception to the server.
 * This will result in this thread being put back to sleep again, until there
 * is another callback. This lets local client objects be designed without
 * regard for whether they will be behind a firewall or not. This technique
 * enables asynchronous server callbacks using the client's <i>outgoing</i>
 * socket, thereby solving the firewall issue. The development of this
 * process was originally championed by project member Fredrik Larsen.<p>
 *
 * @version 1.0, 28-Mar-04 Initial release
 */
public final class ItemProxy extends Thread {
   private final Object item, client;
   /**
    * The constructor links the remote object to the firewalled client.
    * It will automatically start the thread, which will call the remote
    * {@link ClientProxy ClientProxy}, blocking until there is a callback
    * method to be invoked.
    * @param item A remote reference to a ClientProxy, from which the remote
    * object will invoke asynchronous callbacks
    * @param client The firewalled local object that wishes to receive
    * asynchronous callbacks
    */
   public ItemProxy(Remote item, Object client) {
      this.item   = item;
      this.client = client;
      start();
   }
   /**
    * The processing thread, and the crux of this technique. This thread
    * starts out by calling the remote {@link ClientProxy ClientProxy}, to
    * enter a blocking wait. The ClientProxy will wake the thread, providing
    * it an object array containing two things; the name of the method to be
    * called on the local object, and the data to be provided it. This thread
    * will invoke the local object's method, and return the result, or
    * exception, to the ClientProxy, beginning the cycle again.
    */
   public void run() {
      try {
         Object args = null;
         while(true) {
            args = Remote.invoke(item, null, args);
            String method = (String)((Object[])args)[0];
            args = ((Object[])args)[1];
            try { args = Remote.invoke(client, method, args); }
            catch(Exception x) { args = x; }
         }
      } catch(Exception x) { x.printStackTrace(); }
   }
}
