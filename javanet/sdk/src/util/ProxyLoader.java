package util;

import gnu.cajo.invoke.Remote;
import gnu.cajo.invoke.Invoke;
import java.io.InputStream;
import java.io.ObjectInputStream;

/*
 * A base controller class for a cajo grail server.
 * The cajo project: https://cajo.dev.java.net
 * For issues or suggestions mailto:cajo@dev.java.net
 * This class is released into the public domain.
 * Written by John Catherino 01-Dec-09
 */

/**
 * This <i>internal use only</i> helper class is designed to decouple
 * services from controllers. It is a uniquely <i>shared</i> class, between
 * controllers and services.
 * @author <a href=http://wiki.java.net/bin/view/People/JohnCatherino>
 * John Catherino</a>
 */
final class ProxyLoader implements Invoke {
   private static final long serialVersionUID = 0L;
   private final String handle; // name of the proxy class
   private Object server; // reference to the remote service
   private transient Object proxy; // instance  of our named controller
   /**
    * The constructor saves the name of the controller class to instantiate
    * for later use.
    * @param handle The name of the controller class, e.g. controller.Test
    */
   ProxyLoader(String handle) { this.handle = handle; }
   /**
    * Its first invocation is performed by the BaseService, to provide a
    * remote reference to itself, for controller callbacks.  The second
    * invocation dynamically instantiates the controller, via its no-arg
    * constructor. The constructed controller will have its setService
    * method invoked with a remote reference to the service object. After
    * this point, the ProxyLoader will and pass all subsequent invocations
    * to the created proxy itself.
    * @param  method The method to invoke on the internal object.
    * @param args The arguments to provide to the method for its invocation.
    * It can be a single object, an array of objects, or null.
    * @return The sychronous data, if any, resulting from the invocation.
    * @throws java.rmi.RemoteException For network communication related
    * reasons.
    * @throws NoSuchMethodException If no matching method can be found.
    * @throws Exception If the internal object rejects the request, for any
    * application specific reason.
    */
   public Object invoke(String method, Object args) throws Exception {
      if (server == null && method.equals("setService")) {
         server = args; // first call is setService
         return null;
      } else if (proxy == null) { // create proxy first time used
         proxy = Class.forName(handle).getConstructor((Class[])null).
            newInstance((Object[])null);
         Remote.invoke(proxy, "setService", server);
      }
      return Remote.invoke(proxy, method, args); // pass to proxy
   }
   /**
    * This method simply overrides toString, to prepend that it is a
    * ProxyLoader, referencing whatever its wrapped object returns from its
    * toString method.
    * @return String The name ProxyLoader-> prepended to the toString
    * returned by the wrapped object.
    */
   public String toString() { return "ProxyLoader->" + proxy; }
}
