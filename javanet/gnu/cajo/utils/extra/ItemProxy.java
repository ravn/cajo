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
 * This class is used to receive server item callbacks to a firewalled client.
 * A client whose firewall settings prohibit incoming socket connections is a
 * common problem. The server item creates an ItemProxy to represent itself in
 * the context of the client. A client item invokes the setClient method, on
 * which it will receive server callbacks. This object periodically polls the
 * server item for invocations. When found, it will pass the invocation data on
 * to the client item, and return any resulting data, or exception. Its
 * development was championed by project member Fredrik Larsen.
 *
 * @version 1.0, 28-Mar-04 Initial release
 */
public final class ItemProxy implements java.io.Serializable {
   private transient Thread thread;
   private final long interval;
   private Object item;
   ItemProxy(Remote item, long interval) {
      this.item = item;
      this.interval = interval;
   }
   /**
    * This method is used to assign the local client item which is expecting
    * server callbacks, from behind a firewall. It can only be called once.
    * @param client An object whose public interface will become callable from
    * the remote item which sent this object.
    * an array of arguments.
    * @throws IllegalStateException If this method was called more than once,
    * as it applies only to a single client item.
    */
   public void setClient(final Object client) {
      if (thread == null) throw new IllegalStateException("Client already set");
      thread = new Thread(new Runnable() {
         public void run() {
            try { 
               while(true) {
                  Object args = Remote.invoke(item, "getData", null);
                  if (args != null) {
                     String method = (String)((Object[])args)[0];
                     args = (Object)((Object[])args)[1];
                     try { args = Remote.invoke(client, method, args); }
                     catch(Exception x) { args = x; }
                     Remote.invoke(item, "setData", args);
                  }
                  Thread.sleep(interval);
               }
            } catch(Exception x) {
               thread = null;
               item = null;
            }
         }
      });
      thread.start();
   }
}
