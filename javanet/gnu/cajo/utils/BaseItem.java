package gnu.cajo.utils;

import gnu.cajo.invoke.*;
import java.rmi.MarshalledObject;

/*
 * Server Item Base Class
 * Copyright (c) 1999 John Catherino
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
 * A standard base class for server items.  Server items differ from proxy
 * items in that they never leave their host VM.
 * 
 * @version 1.0, 01-Nov-99 Initial release
 * @author John Catherino
 */
public class BaseItem {
   /**
    * A reference to the item's processing code.  If non-null, it will be
    * started automatically binding in the rmiregistry.  Its thread can be
    * accessed through the thread member.
    */
   protected MainThread runnable;
   /**
    * A reference to the proxy served by this item.  It is assigned by the
    * {@link ItemServer ItemServer} during its bind operation. It is
    * the item's proxy, if it has one, otherwise a remote reference to itself,
    * encased in a {@link java.rmi.MarshalledObject MarshalledObject}
    */
   protected MarshalledObject mob;
   /**
    * A reference to the item's processing thread. It can be
    * {@link java.lang.Thread#interrupted interrupted}, to signal the item to
    * perform an orderly shutdown.
    */
   public Thread thread;
   /**
    * The main processing thread of this Item.  An item can be either entirely,
    * event driven, i.e. executing only when its methods are being invoked,
    * or can also have a thread of its own. If non-null, it will be started
    * upon its binding by the {@link ItemServer ItemServer}, where its
    * {@link #startThread startThread} method will be invoked.<br><br>
    * This is an an inner class of BaseItem, to allow its implementations
    * access to the item's private and protected members and methods.
    * This is critical because <b>all</b> public methods of BaseItem can be
    * invoked by remote objects, just like with local objects.
    */
   public abstract class MainThread implements Runnable {
      /**
       * Nothing is performed in the constructor. Construction and
       * configuration are generally performed by a builder application.
       */
      public MainThread() {}
      /**
       * The run method is exectued by the thread created for the BaseItem
       * at its binding on the server, and runs until it returns.
       */
      public abstract void run();
   }
   /**
    * The constructor does nothing, server item configuration is to be done by
    * application specific subclasses.
    */
   public BaseItem() {}
   /**
    * This remotely invokable method is called by remote clients to install
    * their proxies in this VM. This invocation will only succeed if
    * the acceptProxies method of the {@link ItemServer ItemServer} has been
    * called. The received proxy's init method will be invoked with a reference
    * to itself, remoted in the context of this VM.  This is done to initialize
    * the proxy, and provide it with a handle to pass to other remote items,
    * on which they can contact this proxy. If the initialization returns an
    * AWT component, it will be displayed automatically. The remote proxy
    * reference will also be returned to the caller, providing an interface on
    * which to asynchronously call its proxy.
    * 
    * @param proxy The proxy to run in this VM.
    * @return A reference to the proxy remoted within this context.
    * @throws ClassNotFoundException If the item does not accept proxies.
    * @throws IllegalArgumentException If the item provided is a remote
    * reference.
    * @throws Exception If the proxy rejected the initialization invocation.
    */
   public Remote setProxy(Invoke proxy) throws Exception {
      if (proxy instanceof RemoteInvoke)
         throw new IllegalArgumentException("Proxy must be local");
      Remote ref = new Remote(proxy);
      Object result = proxy.invoke("init", ref);
      return ref;
   }
   /**
    * This remotely invokable method is called by the remote clients, to
    * request the server item's default proxy, if it supports one. If it does
    * not, it will return a remote reference to itself.
    * @return A the proxy serving this item, or a remote reference to the
    * item, encased in a {@link java.rmi.MarshalledObject MarshalledObject}.
    */
   public MarshalledObject getProxy() { return mob; }
   /**
    * This method is called by the {@link ItemServer ItemServer} during a
    * bind operation to set the {@link #mob mob} member.
    * @param mob The item's proxy object, if it supports one, otherwise a
    * remote reference to the item itself, either way, encased in a
    * {@link java.rmi.MarshalledObject MarshalledObject}
    * @throws IllegalArgumentException If the method is called more than
    * once, presumably by a remote item.
    */
   public void setProxy(MarshalledObject mob) {
      if (this.mob ==  null) this.mob = mob;
      else throw new IllegalArgumentException("Proxy already set");
   }
   /**
    * This method is called by the {@link ItemServer ItemServer} during a
    * bind operation. If the item has a processing thread, meaning its
    * {@link #runnable runnable} member is not null, the thread will be
    * started, and its reference stored in the {@link #thread thread} member.
    */
   public void startThread() {
      if (thread != null)
         throw new IllegalArgumentException("Thread already started");
      if (thread == null && runnable != null) {
         thread = new Thread(runnable);
         thread.start();
         runnable = null;
      }
   }
}
