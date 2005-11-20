package gnu.cajo.utils.extra;

import gnu.cajo.invoke.*;
import java.lang.reflect.*;

/*
 * Item Transparent Dynamic Proxy (requires JRE 1.3+)
 * Copyright (c) 2005 John Catherino
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
 * This class creates an object, representing a server item. The returned
 * object will implement a list of interfaces defined by the client. The
 * interfaces are completely independent of interfaces the server object
 * implements, if any; they are simply logical groupings, of interest to
 * the client. The interface methods <i>'should'</i> declare that they throw
 * <tt>java.lang.Exception</tt>. However, the Java dynamic proxy mechanism
 * very <i>doubiously</i> allows this to be optional.
 *
 * <p>Clients can dynamically create an item wrapper classes implementing
 * <i>any</i> combination of interfaces. Combining interfaces provides a very
 * important second order of abstraction. Whereas an interface is considered
 * to be a grouping of functionality; the <i>'purpose'</i> of an item, would
 * be determined by the group of interfaces it implements. Clients can
 * customise the way they use remote items as a function of the interfaces
 * that the item proxy implements.
 *
 * <p>If an item can be best represented with a single interface, it would be
 * well to consider using a {@link Wrapper Wrapper} class instead. It is
 * conceptually much simpler.
 *
 * <p><i><u>Note</u>:</i> Unfortunately, this class only works with JREs 1.3
 * and higher. Therefore I was reluctant to include it in the official
 * codebase. However, some very convincing <a href=https://cajo.dev.java.net/servlets/ProjectForumMessageView?forumID=475&messageID=10199>
 * discussion</a> in the cajo project Developer's forum, with project member
 * Bharavi Gade, caused me to reconsider. :-)
 *
 * @version 1.1, 11-Nov-05 Support multiple interfaces
 * @author John Catherino
 */
public final class TransparentItemProxy implements InvocationHandler {
   private final Object item;
   private TransparentItemProxy(Object item) { this.item = item; }
   /**
    * This method, inherited from InvocationHandler, simply passes all object
    * method invocations on to the remote object, automatically and
    * transparently. This allows the local runtime to perform the remote item
    * invocations while appearing syntactically identical to local object
    * invocations.
    * @param proxy The local object on which the method was invoked, it is
    * <i>ignored</i> in this context.
    * @param method The method to invoke on the object, in this case the
    * server item.
    * @param args The arguments to provide to the method, if any.
    * @return The resulting data from the method invocation, if any.
    * @throws java.rmi.RemoteException For network communication related
    * reasons.
    * @throws NoSuchMethodException If no matching method can be found
    * on the server item.
    * @throws Exception If the server item rejected the invocation, for
    * application specific reasons.
    * @throws Throwable For some <i>very</i> unlikely reasons, not outlined
    * above. (required, sorry)
    */
   public Object invoke(Object proxy, Method method, Object args[])
      throws Throwable {
      return Remote.invoke(item, method.getName(), args);
   }
   /**
    * This method fetches a server item reference, generates a class
    * definition for it at runtime, and returns a local object instance.
    * The resulting dynamic proxy object will implement all of the interfaces
    * provided.
    * @param url The URL where to get the object: //[host][:port]/[name].
    * The host, port, and name, are all optional. If missing the host is
    * presumed local, the port 1099, and the name "main". If the URL is
    * null, it will be assumed to be ///.
    * @param interfaces The list of interface classes for the dynamic proxy
    * to implement. Typically, these are provided thus; <tt>new Class[] {
    * Interface1.class, Interface2.class, ... }</tt>
    * @return A reference to the server item, wrapped in the object created
    * at runtime, implementing all of the interfaces provided. It can then
    * be typecast into any of the interfaces, as needed by the client.
    * @throws MalformedURLException if the URL is not in the format required.
    * @throws RemoteException if the rmiregistry could not be reached.
    * @throws NotBoundException if the requested name is not in the registry.
    */
   public static Object getItem(String url, Class interfaces[]) throws
      Exception {
      return Proxy.newProxyInstance(
         interfaces[0].getClassLoader(), interfaces,
         new TransparentItemProxy(Remote.getItem(url))
      );
   }
   /**
    * This generates a class definition for a remote object reference at
    * runtime, and returns a local object instance. The resulting dynamic
    * proxy object will implement all of the interfaces provided.
    * @param item A reference to a remote server object.
    * @param interfaces The list of interface classes for the dynamic proxy
    * to implement. Typically, these are provided thus; <tt>new Class[] {
    * Interface1.class, Interface2.class, ... }</tt>
    * @return A reference to the server item, wrapped in the object created
    * at runtime, implementing all of the interfaces provided. It can then
    * be typecast into any of the interfaces, as needed by the client.
    * @throws IllegalArgumentException if the any of the provided interface
    * classes are not really interfaces.
    */
   public static Object getItem(RemoteInvoke item, Class interfaces[]) throws
      IllegalArgumentException {
      return Proxy.newProxyInstance(
         interfaces[0].getClassLoader(), interfaces,
         new TransparentItemProxy(item)
      );
   }
}
