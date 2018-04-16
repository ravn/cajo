package gnu.cajo.utils.extra;

import gnu.cajo.invoke.Remote;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.net.MalformedURLException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.CancellationException;
import java.lang.reflect.InvocationTargetException;

/*
 * Item Transparent Dynamic Proxy (requires JRE 1.5+)
 * Copyright (c) 2005 John Catherino
 * The cajo project: https://cajo.dev.java.net
 *
 * For issues or suggestions mailto:cajo@dev.java.net
 *
 * This file TransparentItemProxy.java is part of the cajo library.
 *
 * The cajo library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public Licence as published
 * by the Free Software Foundation, at version 3 of the licence, or (at your
 * option) any later version.
 *
 * The cajo library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public Licence for more details.
 *
 * You should have received a copy of the GNU Lesser General Public Licence
 * along with this library. If not, see http://www.gnu.org/licenses/lgpl.html
 */

/**
 * This class creates an object, representing a local or remote service
 * object. The returned object will implement a list of interfaces defined by
 * the client. The interfaces are completely independent of interfaces the
 * server object actually implements, if any; they are simply logical
 * groupings of methods, of interest solely to the client. Additionally, if
 * the client specifies static final fields or primitives, they will be
 * compared as well.
 *
 * <p>Clients can dynamically create object proxy objects implementing
 * <i>any</i> combination of interfaces. Combining interfaces provides a very
 * important second order of abstraction. Whereas an interface is considered
 * to be a grouping of functionality; the <i>'purpose'</i> of an item, could
 * be determined by the group of interfaces it implements. Clients can
 * customise the way they use local/remote objects as a function of the
 * interfaces implemented.
 *
 * <p>The proxy instances returned from this class are serialisable. Proxies
 * can be persisted to storage for later use, and passed to other JVMs over
 * the network.
 *
 * <p><i><u>Note</u>:</i> Unfortunately, this class only works with JREs 1.3
 * and higher. Therefore I was reluctant to include it in the official
 * codebase. However, some very convincing <a href=https://cajo.dev.java.net/servlets/ProjectForumMessageView?forumID=475&messageID=10199>
 * discussion</a> in the cajo project Developer's forum, with project member
 * Bharavi Gade, caused me to reconsider.
 *
 * <p><hr><br><b>Update:</b> For users of JRE 1.5+<br>
 * It you wish to invoke time consuming methods asynchronously, simply
 * declare that the method returns a <a href=http://download.oracle.com/javase/6/docs/api/java/util/concurrent/Future.html>
 * java.util.Concurrent.Future</a> of the required return type. The invocation
 * will return immediately, and the future will contain the result when the
 * invocation is completed. You can then peridocally check to see if it is
 * done, and extract the result.
 *
 * @author John Catherino
 */
public final class TransparentItemProxy implements
   InvocationHandler, Serializable {
   private static final long serialVersionUID = 5L;
   private static final Object NULL[] = {};
   private static final Class CLASS[] = {};
   private static final class ProxyFuture implements Future { // async helper
      private Thread thread;
      private boolean cancelled;
      private volatile boolean done;
      private volatile Object result;
      private volatile Exception exception;
      private ProxyFuture setThread(Thread thread) {
         this.thread = thread;
         thread.start();
         return this;
      }
      public boolean isDone() { return done; }
      public boolean isCancelled() { return cancelled; }
      public boolean cancel(boolean interrupt) {
         if (cancelled || done) return false;
         try { return cancelled = true; }
         finally { if (interrupt) thread.interrupt(); }
      }
      public Object get() throws InterruptedException, ExecutionException {
         if (!done && !cancelled) thread.join();
         if (exception != null) throw new ExecutionException(exception);
         return result;
      }
      public Object get(long timeout, TimeUnit unit)
         throws InterruptedException, ExecutionException, TimeoutException {
         if (!done && !cancelled) {
            if (unit == TimeUnit.NANOSECONDS) thread.join(0L, (int)timeout);
            else if (unit == TimeUnit.MICROSECONDS)
               thread.join(0L, (int)(timeout * 1000));
            else thread.join(unit.toMillis(timeout));
         }
         if (!done) throw new TimeoutException();
         if (exception != null) throw new ExecutionException(exception);
         return result;
      }
   }
   private transient Object item;
   private String toString;
   private void writeObject(java.io.ObjectOutputStream out)
      throws IOException {
      out.defaultWriteObject();
      out.writeObject(new java.rmi.MarshalledObject(
         item instanceof Serializable ?
            item : new Remote(item).clientScope()));
   }
   private void readObject(java.io.ObjectInputStream in)
      throws ClassNotFoundException, IOException {
      in.defaultReadObject();
      item = ((java.rmi.MarshalledObject)in.readObject()).get();
   }
   private TransparentItemProxy(Object item) { this.item = item; }
   /**
    * An optional centralised invocation error handler. If an invocation on
    * a remote object results in a checked or unchecked exception being thrown;
    * this object, if assigned, will be called to deal with it. This allows
    * all retry/recovery/rollback logic, etc. to be located in a single place.
    * It is expected that this object will implement a method of the following
    * signature:<p>
    * <blockquote><tt>
    * public Object handle(Object item, String method, Object args[],
    * Throwable t)
    * throws Exception; </tt></blockquote><p>
    * The arguments are as follows: <i>(in order)</i><ul>
    * <li>the item reference on which the method was being invoked
    * <li>the name of the method that was called on the remote object
    * <li>the arguments that were provided in the method call
    * <li>the error that resulted from the invocation</ul>
    * The handler will either successfully recover from the error, and return
    * a substitute result, or throw a hopefully more descriptive exception.
    */
   public static Object handler;
   /**
    * This method, inherited from InvocationHandler, simply passes all object
    * method invocations on to the wrapped object, automatically and
    * transparently. This also allows the local runtime to perform remote item
    * invocations, while appearing syntactically identical to local ones.
    * @param proxy The locallly created proxy object on which the method was
    * originally invoked.
    * @param method The method to invoke on the object, in this case the
    * local or remote service object.
    * @param args The arguments to provide to the method, if any.
    * @return The resulting data from the method invocation, if any.
    * @throws java.rmi.RemoteException For network communication related
    * reasons with a remote service.
    * @throws NoSuchMethodException If no matching method can be found
    * on the service object.
    * @throws Exception If the service object rejected the invocation, for
    * application specific reasons.
    */
   public Object invoke(Object proxy, Method method, final Object args[])
      throws Throwable {
      final String name = method.getName();
      if (args ==null || args.length == 0) {
         if ("toString".equals(name)) { // attempt toString
            if (toString != null) return toString;
            StringBuffer sb = new StringBuffer(toString());
            sb.append("->");
            try { sb.append(Remote.invoke(item, "toString", null)); }
            catch(Throwable t) { // handle if possible, do NOT throw!
               sb.append(handler != null ? Remote.invoke(handler, "handle",
                  new Object[] { item, method, args, t }) : t.toString());
            }
            return toString = sb.toString();
         } else if ("notify".equals(name) || "notifyAll".equals(name))
            throw new IllegalMonitorStateException(
               "Cannot notify transparent proxy object");
      } else if (args !=null && args.length == 1 && "equals".equals(name))
         return args[0] == null ? Boolean.FALSE :
            proxy == args[0] ||
            Proxy.isProxyClass(args[0].getClass()) &&
            Proxy.getInvocationHandler(args[0]) instanceof
               TransparentItemProxy &&
            item.equals(((TransparentItemProxy)Proxy.
               getInvocationHandler(args[0])).item) ?
            Boolean.TRUE : Boolean.FALSE;
      if (args !=null && args.length < 4 && "wait".equals(name))
         if (args.length < 1 ||
            args[0] instanceof Long  && (args.length < 2 ? true :
            args[1] instanceof Long) && (args.length < 3 ? true :
            args[2] instanceof Integer))
               throw new IllegalMonitorStateException(
                  "Cannot wait on transparent proxy object");
      if (Future.class.isAssignableFrom(method.getReturnType())) {
         final ProxyFuture future = new ProxyFuture();
         return future.setThread(new Thread() {
            public void run() {
               try { future.result = Remote.invoke(item, name, args); }
               catch(Throwable t) {
                  if (handler != null) try {
                     future.result = Remote.invoke(handler,
                        "handle", new Object[] { item, name, args, t });
                  } catch(Exception x) { future.exception = x; }
                  else future.exception = t instanceof Exception ?
                     (Exception)t :
                     new Exception(t.getMessage(), t.getCause());
               } finally { future.done = true; }
            }
         });
      } else try { return Remote.invoke(item, name, args); }
      catch(Throwable t) {
         if (handler != null) return Remote.invoke(
            handler, "handle", new Object[] { item, name, args, t });
         else throw t instanceof Exception ?
            (Exception)t : new Exception(t.getMessage(), t.getCause());
      }
   }
   /**
    * This method creates a dynamic proxy reference object for the argument
    * supplied, implementing <i>all</i> of its interfaces. This method is
    * typically used by a <i>sender</i> of remote references, whereas the
    * getItem methods are generally used by <i>receivers.</i>
    * @param object The object to be proxied
    * @return A proxy reference implementing the supplied object's interfaces
    */
   public static Object proxy(Object object) {
      java.util.HashSet interfaces = new java.util.HashSet();
      for (Class c = object.getClass(); c != null; c = c.getSuperclass())
         interfaces.addAll(java.util.Arrays.asList(c.getInterfaces()));
      return getItem(object, (Class[])interfaces.toArray(CLASS));
   }
   /**
    * This generates a class definition for a any object reference at
    * runtime, and returns a local object instance. The resulting dynamic
    * proxy object will implement all the interfaces provided.
    * @param item A reference to a either a remote object, or a local one<br>
    * <i><u>Note</u>:</i> a non-serialisable local item will be automatically
    * remoted when serialised, so as to support proxies being freely passed
    * between JVMs.
    * @param interfaces The list of interface classes for the dynamic proxy
    * to implement. Typically, these are provided thus; <tt>new Class[] {
    * Interface1.class, Interface2.class, ... }</tt>
    * @return A reference to the provided object, wrapping the local object,
    * it can then be typecast into any of the interfaces, as needed by the
    * client.
    */
   public static Object getItem(Object item, Class interfaces[]) {
      return Proxy.newProxyInstance(interfaces[0].getClassLoader(),
         interfaces, new TransparentItemProxy(item));
   }
   /**
    * This method fetches a server item reference, generates a class
    * definition for it at runtime, and returns a local object instance.
    * The resulting dynamic proxy object will implement all the interfaces
    * provided. Technically, it invokes the other getItem method, after
    * fetching the object reference from the remote JVM specified.
    * @param url The URL where to get the object: //[host][:port]/[name].
    * The host, port, and name, are all optional. If missing the host is
    * presumed local, the port 1099, and the name "main". If the URL is
    * null, it will be assumed to be ///.
    * @param interfaces The list of interface classes for the dynamic proxy
    * to implement. Typically, these are provided thus; <tt>new Class[] {
    * Interface1.class, Interface2.class, ... }</tt>
    * @return A reference to the server item, wrapping the object created
    * at runtime, implementing all of the interfaces provided. It can then
    * be typecast into any of the interfaces, as needed by the client.
    * @throws RemoteException if the remote registry could not be reached.
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
   public static Object getItem(String url, Class interfaces[])
      throws RemoteException, NotBoundException, IOException,
      ClassNotFoundException, InstantiationException, IllegalAccessException,
      MalformedURLException {
      return getItem(Remote.getItem(url), interfaces);
   }
}
