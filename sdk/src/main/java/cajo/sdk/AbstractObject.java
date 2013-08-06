
package cajo.sdk;

/* Copyright 2010 John Catherino
 * The cajo project: http://cajo.java.net
 *
 * Licensed under the Apache Licence, Version 2.0 (the "Licence"); you may
 * not use this file except in compliance with the licence. You may obtain
 * a copy of the licence at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the licence is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * This internal use only class provides the foundation for building {@link
 * AbstractService service}, {@link AbstractController controller} and
 * {@link AbstractAgent agent} objects. It contains a collection of general
 * purpose, but highly useful methods frequently needed by the derived
 * objects.
 * @author John Catherino
 */
abstract class AbstractObject {
   /**
    * This utility function can be used to invoke a public method on either a
    * remote, or a local, object reference. It is an alternative to the proxy
    * function; e.g. when method names or arguments are not known at compile
    * time. The method to be invoked, either instance or static, must be
    * public, but the class implementing the method need not. This is how the
    * cajo project implements <a href=http://en.wikipedia.org/wiki/Dynamic_dispatch>
    * <i>dynamic dispatch,</i></a> as the Java language does not currently
    * support it natively.
    * @param <T> The coercable primitive type, class or superclass, of the
    * expected method invocation return, this applies only if the result is
    * being assigned to a variable, it is not specified, rather it is
    * implicit
    * @param object The object reference on which to invoke the method; note
    * that this reference can be to <i>either</i> a remote object, <i>or</i>
    * a local one, typically it is either a service, controller, or agent
    * @param method The method name to be invoked on the provided object
    * reference, this method can be instance or static
    * @param args The arguments required by the method, if any, in order,
    * exact or coercable primitive types, subclasses, and even <i>nulls</i>
    * can be included, this can be omitted if the method takes no arguments
    * @return The result of the method invocation, if any, it can be
    * primitive or an object
    * @throws Exception If the called method rejects the invocation for
    * implementation specific reasons, if the method does not exist or is not
    * public, or if the invocation is on a remote object, for network related
    * failure
    */
   @SuppressWarnings("unchecked")
   protected static final <T> T
      invoke(Object object, String method, Object... args) throws Exception {
      return (T)gnu.cajo.invoke.Remote.invoke(object, method, args);
   }
   /**
    * This utility function dynamically casts a service, sent agent,
    * or received controller reference, into a local interface. The provided
    * interface can implement as much, or as little, of the provided object's
    * public methods <i>(instance and static)</i>as needed. A dynamic proxy
    * created by this method can be passed between JVMs, if needed.
    * <br><i><u>NB</u>:</i> methods can be invoked either synchronously, or
    * asynchronously. The difference depends simply on the type of method
    * return specified. If the actual type of the return is used in the
    * interface, the method will block until completion as expected. However,
    * if a <a href=http://download.oracle.com/javase/6/docs/api/java/util/concurrent/Future.html>
    * java.util.concurrent.Future</a> is specified, the invocation will
    * return immediately, with the Future object. The future can be
    * periodically checked to see when the invocation is done, and get the
    * result. This is <i>very useful</i> for methods which require lengthy
    * time to complete, so as not to delay other thread processing.
    * @param <T> The local interface to be implemented by the returned
    * dynamic proxy, the method signatures need not match exactly, but must
    * be logically compatible, under the rules of the language
    * @param object A reference typically to a service, received controller,
    * or sent agent, or its local proxy
    * @param localInterface The local class of an interface of interest
    * <i>(e.g. somepkg.SomeInterface.class)</i>
    * @return An object implementing the interface provided, yet passing
    * the method invocations directly onto the object reference
    * <br><i><u>NB</u>:</i> If any of the arguments passed onto a remote
    * invocation are not serialisable, or the result is not; those objects
    * will be transparently replaced with dynamic proxies, implementing all
    * of the remote object's interfaces. Hence the practice of using
    * interfaces, rather than classes, in method signatures is recommended.
    * <br>A local dynamic proxy can even be created with a local interface,
    * on a received dynamic proxy.
    */
   @SuppressWarnings("unchecked")
   protected static final <T> T proxy(Object object, Class<T> localInterface) {
      return (T)gnu.cajo.utils.extra.TransparentItemProxy.
         getItem(object, new Class[] { localInterface });
   }
   /**
    * This utility method is used to fetch a service reference from a remote
    * server. To use the old-style telephone book as a metaphor: this is
    * a way of obtaining a service reference via the <i>white pages.</i>
    * When controllers are fetched, and agents are sent, to services
    * obtained via <i>white pages</i> lookup; their local cajo object can
    * support the <i>friend of a friend</i> paradigm of its receiving host.
    * @param <T> The class of the interface being sought, the returned proxy
    * object will implement this interface
    * @param host The host name or IP address of the remote server machine
    * @param port The TCP port number on which the remote server is operating
    * @param name The name of the service object to get
    * @param localInterface A collection of method signatures of interest to
    * this client, a matching service reference will implement at least these
    * methods, but not necessarily the class
    * @return A proxy to the controller of the remote service implementing
    * provided interface
    * @throws Exception If the named service is not currently available
    */
   protected static final <T> T lookup(String host, int port, String name,
      Class<T> localInterface) throws Exception {
      Object service = java.rmi.Naming.lookup("//"+host+':'+port+'/'+name);
      return proxy(service, localInterface);
   }
   /**
    * A reference to the {@link AbstractService service} for which this
    * object operates.
    */
   Object serviceRef;
   /**
    * This is the <a href=https://cajo.dev.java.net/nonav/docs/gnu/cajo/Cajo.html>
    * cajo</a> object with which service, controller, and agent objects can
    * search for other services via the <a href=http://weblogs.java.net/blog/cajo/archive/2007/09/simple_interjvm.html>
    * grail</a> framework.
    */
   protected transient gnu.cajo.Cajo cajo;
   /**
    * The default constructor simply assigns the service reference to its
    * package internal field.
    * @param serviceRef A reference to the service object on which this object
    * operates
    */
   AbstractObject(@SuppressWarnings("hiding") Object serviceRef) {
      this.serviceRef = serviceRef;
   }
   /**
    * This utility method is used to find a service object resource matching
    * the interface provided. To use the old-style telephone book as a
    * metaphor: this is a way of obtaining a service reference via the
    * <i>yellow pages.</i> In order to provide load balancing, if more
    * than one matching reference is available, one will be selected at
    * random.
    * @param <T> The class of the interface being sought, the returned proxy
    * object will implement this interface
    * @param localInterface A collection of method signatures of interest to
    * this client, a matching service reference will implement at least these
    * methods, but not necessarily the class. The interface can also specify
    * static final fields, which will be matched against public static or
    * instance fields on the service object. If the interface method
    * specifies a void return type, it will match to any service return type.
    * @return A proxy to a service of either a remote or local object,
    * matching the provided interface, or null, if no matching service
    * references can be found
    * @throws Exception For network related errors
    */
   protected final <T> T lookup(Class<T> localInterface) throws Exception {
      Object refs[] = cajo.lookup(localInterface);
      if (refs.length == 0) return null;
      Object service = refs[new java.util.Random().nextInt(refs.length)];
      return proxy(service, localInterface);
   }
   /**
    * This utility method is used to find <i>all</i> service object resources
    * currently available matching the interface provided. To use the
    * old-style telephone book as a metaphor: this is a way of obtaining a
    * service references via the <i>yellow pages.</i>
    * @param <T> The class of the interface being sought, the returned proxies
    * will implement this interface
    * @param localInterface A collection of method signatures of interest to
    * this client, a matching service reference will implement at least these
    * methods, but not necessarily the class. The interface can also specify
    * static final fields, which will be matched against public static or
    * instance fields on the service object. If the interface method
    * specifies a void return type, it will match to any service return type.
    * @return An array of proxies to services of either a remote or local
    * objects, matching the provided interface, or null, if no matching
    * service references can be found
    * @throws Exception For network related errors
    */
   @SuppressWarnings("unchecked")
   protected final <T> T[] lookupAll(Class<T> localInterface)
      throws Exception {
      Object refs[] = cajo.lookup(localInterface);
      if (refs.length == 0) return null;
      java.util.List<T> t = new java.util.ArrayList<T>(refs.length);
      for (Object ref : refs) t.add(proxy(ref, localInterface));
      return (T[])t.toArray();
   }
   /**
    * This utility method is used to request a {@link AbstractController
    * controller} object from a service. The service reference is typically
    * looked up using the
    * <a href=https://cajo.dev.java.net/nonav/docs/gnu/cajo/Cajo.html#lookup(java.lang.Class)>
    * cajo</a> object.
    * <br><i><u>NB</u>:</i> Accepting of controllers is disabled by default.
    * To enable acceptance of controllers and agents uncomment the
    * <tt>ItemServer.acceptProxies()</tt> line in the main Server class.
    * @param <T> The local interface to be implemented by the returned
    * dynamic proxy, the method signatures need not match exactly, but must
    * be logically compatible, under the rules of the language
    * @param service The reference to the service object, or its local proxy,
    * from which to request its controller
    * @param localInterface A collection of method signatures of interest to
    * this client, a matching controller reference must implement at least
    * these methods, but not necessarily the class
    * @return A proxied reference to the controller operating in this JVM, or
    * null, if the service does not furnish a controller
    * @throws java.rmi.RemoteException For network errors, or network
    * configuration issues
    * @throws Exception if the called service does not support getting a
    * controller
    */
   protected final <T> T getController(Object service,
      Class<T> localInterface) throws Exception {
      Object controller = invoke(service, "getController");
      if (controller == null) return null;
      invoke(controller, "init", cajo);
      return proxy(controller, localInterface);
   }
   /**
    * This method is used to send an {@link AbstractAgent agent} object to a
    * service.
    * @param <T> The local interface to be implemented by the returned
    * dynamic proxy, the method signatures need not match exactly, but must
    * be logically compatible, under the rules of the language
    * @param service The reference of the service, or its local proxy, to
    * which to send this agent
    * @param agent The class name of the agent to instantiate on arrival
    * <i>(e.g. agent.Agent)</i> it can be either hard-coded, read from a file,
    * or a system property
    * @param localInterface A collection of method signatures of interest to
    * this client, the sent agent must implement at least these methods, but
    * not necessarily the class
    * @return A proxied reference to the agent operating at the target
    * service
    * @throws Exception If the called service does not accept agent objects
    */
   protected final <T> T sendAgent(Object service, String agent,
      Class<T> localInterface) throws Exception {
      ProxyLoader pl = new ProxyLoader(agent, serviceRef);
      return proxy(invoke(service, "acceptAgent", pl), localInterface);
   }
}
