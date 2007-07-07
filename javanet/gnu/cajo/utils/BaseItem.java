package gnu.cajo.utils;

import gnu.cajo.invoke.*;
import java.rmi.MarshalledObject;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.io.IOException;
import java.net.MalformedURLException;

/*
 * Server Item Base Class
 * Copyright (C) 1999 John Catherino
 * The cajo project: https://cajo.dev.java.net
 *
 * For issues or suggestions mailto:cajo@dev.java.net
 *
 * This file BaseItem.java is part of the cajo library.
 *
 * The cajo library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public Licence as published
 * by the Free Software Foundation, at version 3 of the licence, or (at your
 * option) any later version.
 *
 * Th cajo library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public Licence for more details.
 *
 * You should have received a copy of the GNU Lesser General Public Licence
 * along with this library. If not, see http://www.gnu.org/licenses/lgpl.html
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
    * on which they can contact this proxy. The remote proxy reference will
    * be returned to the caller, providing an interface on which to
    * asynchronously call its proxy.
    * 
    * @param proxy The proxy to run in this VM, it is typically sent as a
    * MarshalledObject, from which it will be extracted automatically.
    * @return A reference to the proxy remoted within this context.
    * @throws ClassNotFoundException If the item does not accept proxies.
    * @throws IllegalArgumentException If the item provided is a remote
    * reference.
    * @throws Exception If the proxy rejected the initialization invocation.
    */
   public Remote installProxy(Object proxy) throws Exception {
      if (proxy instanceof MarshalledObject)
         proxy = ((MarshalledObject)proxy).get();
      if (proxy instanceof RemoteInvoke)
         throw new IllegalArgumentException("Proxy must be local");
      Remote ref = new Remote(proxy);
      Remote.invoke(proxy, "init", ref);
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
   /**
    * A method will load either an item, or a zipped marshalled object
    * (zedmob) of an item, from a URL, file, or from a remote rmiregistry.
    * If the item is in a local file, it can be either inside the server's
    * jar file, or on its local file system.<p> Loading an item from a file
    * can be specified in one of three ways:<p><ul>
    * <li>As a URL; in the format file://path/name.
    * <li>As a class file; in the format path/name
    * <li>As a serialized item; in the format /path/name</ul><p>
    * @param url The URL where to get the object: file://, http://, ftp://,
    * /path/name, path/name, or //[host][:port]/[name]. The host, port,
    * and name, are all optional. If missing the host is presumed local, the
    * port 1099, and the name "main". The referenced resource can be
    * returned as a MarshalledObject, it will be extracted automatically.
    * If the URL is null, it will be assumed to be ///.
    * @return A remote reference to the item contained in the URL. It may be
    * either local, or remote to this VM.
    * @throws RemoteException if the remote registry could not be reached,
    * or the remote instance could not be be created.
    * @throws NotBoundException if the requested name is not in the registry.
    * @throws IOException if the zedmob format is invalid.
    * @throws ClassNotFoundException if a proxy was sent to the VM, and
    * proxy hosting was not enabled.
    * @throws InstantiationException when the URL specifies a class name
    * which cannot be instantiated at runtime.
    * @throws IllegalAccessException when the url specifies a class name
    * and it does not support a no-arg constructor.
    * @throws MalformedURLException if the URL is not in the format explained
    */
   public Remote getItem(String url) throws RemoteException,
      NotBoundException, IOException, ClassNotFoundException,
      InstantiationException, IllegalAccessException, MalformedURLException {
      return new Remote(Remote.getItem(url));
   }
   /**
    * This method is invoked by remote users of this object. It is expected
    * that subclasses will override this method to provide detailed usage
    * information. Use of HTML for particularly long descriptions is permitted.
    * By default this method will return: not defined.
    * @return A description of the callable methods, their arguments, returns,
    * and functionality.
    */
   public String getDescription() { return "not defined"; }
   /**
    * This method is canonically called when an item announces its reference
    * via the {@link Multicast Multicast} class. It is expected to receive
    * the URLs of objects that heard the announcement, and wish to be contacted.
    * @param url A //host:port/name type URL on which the 'first-contact' object
    * of a remote VM can be reached.
    */
   public void contact(String url) {}
}
