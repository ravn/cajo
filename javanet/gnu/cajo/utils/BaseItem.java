package gnu.cajo.utils;

import gnu.cajo.invoke.*;
import java.rmi.MarshalledObject;

/*
 * Abstract Server Item Base Class
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
 * A potentially standard abstract base class for server objects.  Server
 * objects differ from proxy objects in that they never leave their host
 * VM, only remote references to them.
 * 
 * @version 1.0, 01-Nov-99 Initial release
 * @author John Catherino
 */
public abstract class BaseItem implements Invoke, Runnable {
   /**
    * A reference to the proxy served by this item.  It is assigned by the
    * ProxyServer during its {@link ProxyServer#bind bind} operation.
    */
   protected MarshalledObject mob;
   /**
    * A reference to the item's processing thread.  It is started automatically
    * on construction.  It can be interrupted, to signal the item for an
    * orderly shutdown.
    */
   public Thread thread;
   /**
    * The constructor simply starts up the item's processing thread.
    */
   public BaseItem() {
      thread = new Thread(this);
      thread.start();
   }
   /**
    * This method is called by remote clients to install a proxy in this VM.
    * This invocation will only succeed if acceptProxies was true when it
    * was bound.  The received proxy's init method will be invoked with a
    * reference to itself, remoted in the context of this VM.  This remote
    * reference will be returned, providing the calling item with an interface
    * on which to asynchronously call its proxy back.
    * @param proxy The proxy to run in this VM.
    * @return A reference to the proxy remoted within this context.
    * @throws ClassNotFoundException If the item does not accept proxies.
    * @throws Exception If the init invocation rejected the initialization
    * invocation.
    */
   public Remote setProxy(Invoke proxy) throws Exception {
      Remote ref = new Remote(proxy);
      proxy.invoke("init", new Remote(proxy));
      return ref;
   }
   /**
    * This method is called by the remote clients, to request the proxy
    * object through which to interface to this item.
    * @return A reference to the proxy serving this item, encased in a
    * {@link java.rmi.MarshalledObject MarshalledObject}
    */
   public final MarshalledObject getProxy() { return mob; }
   /**
    * This function may be called reentrantly, so critical methods <i>must</i>
    * be synchronized. It will invoke the specified method with the provided
    * arguments, if any, using the Java reflection mechanism. This allows
    * subclasses to define methods and signatures of their own liking.  This
    * method will connect to that method based on its name, and argument types.
    * However, the argument types unfortunately must match exactly, as the
    * reflection mechansim does not recognize polymorphism.
    * @param  method The method to invoke in this item.
    * @param args The arguments to provide to the method for its invocation.
    * @return The sychronous data, if any, resulting from the invocation.
    * @throws java.rmi.RemoteException For network communication related
    * reasons.
    * @throws IllegalArgumentException If the method argument is null.
    * @throws NoSuchMethodException If no matching method can be found.
    * @throws Exception If the method rejects the invocation, for any
    * application specific reason.
    * @throws ClassCastException If the first invocation is not with a
    * {@link java.rmi.MarshalledObject MarshalledObject} of its proxy.
    */
   public final Object invoke(String method, Object args) throws Exception {
      if (mob == null) {
         mob = (MarshalledObject)args;
         return null;
      }
      if (method == null)
         throw new IllegalArgumentException("Method cannot be null");
      Class types[] = null;
      if (args instanceof Object[]) {
         types = new Class[((Object[])args).length];
         for (int i = 0; i < types.length; i++)
            types[i] = ((Object[])args)[i] instanceof Invoke ?
               Invoke.class : ((Object[])args)[i].getClass();
      } else if (args != null) {
         types = new Class[] {
            args instanceof Invoke ? Invoke.class : args.getClass() };
         args = new Object[] { args };
      }
      return getClass().getMethod(method, types).invoke(this, (Object[])args);
   }
   /**
    * The processing thread of the item.  It will be started automatically
    * construction of the object.  Subclasses should monitor its
    * {@link java.lang.Thread#isInterrupted isInterrupted} method, and
    * perform an orderly shutdown if it becomes true.
    */
   public abstract void run();
}
