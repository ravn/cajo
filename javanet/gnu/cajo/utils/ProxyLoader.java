package gnu.cajo.utils;

import gnu.cajo.invoke.*;
import java.io.*;
import java.util.zip.GZIPOutputStream;

/*
 * Item Proxy Loader Wrapper
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
 * This class is used to avoid having to load proxies into the VM runtime of
 * the proxy server. This is useful in the case when proxies are large, or are
 * not directly referenced by the server before sending them to the client.
 * This is extremely helpful when the server provides a large number of
 * proxies.  This wrapper object requires only a small number of bytes in the
 * server's VM, to represent a proxy item of arbitrary size.  Upon its arrival
 * at the hosting VM, it will reconstruct the internally referenced proxy item,
 * either by construction or deserialization, and initialize it such that any
 * standard {@link BaseProxy proxy} can be handled via this class. This will
 * also conserve bandwidth, as proxies in the codebase jar file are compressed.
 * <i>Note:</i> the proxy object is assumed to implement the
 * {@link gnu.cajo.invoke.Invoke Invoke} interface.
 * <p>
 * Typical serialized proxy names:  /test.ser /objects/test.ser
 * <p>
 * Typical unserialized proxy names:  Test classes/Test
 *
 * @version 1.0, 01-Nov-99 Initial release
 * @author John Catherino
 */
public final class ProxyLoader implements Invoke {
   private final String handle;
   private RemoteInvoke server;
   private transient Invoke proxy;
   /**
    * The constructor creates a small wrapper object referencing a proxy item
    * solely by name, but not loading its object into the server's VM runtime.
    *  It is simply a server-side representation of the proxy, but not the
    * proxy itself.
    * @param handle The path of either the proxy class file, or the file
    * containing serialized instance of the proxy, to be found inside the
    * proxy's codebase jar file.
    */
   public ProxyLoader(String handle) { this.handle = handle; }
   /**
    * This function may be called reentrantly, so the inner item <i>must</i>
    * synchronize its critical sections as necessary. Its first invocation is
    * performed by the {@link ItemServer ItemServer}, to provide a remote
    * reference to itself, for proxy callbacks.  The second invocation
    * is by the client, to provide a remoted reference to the ProxyLoader, to
    * allow the proxy a handle on which to receive asynchronous callbacks. At
    * this point the ProxyLoader will reconstitute the proxy object, and pass
    * it the two previous arguments, respectively. All subsequent invocations
    * are routed directly to the created proxy itself.
    * @param  method The method to invoke on the internal item.
    * @param args The arguments to provide to the method for its invocation.
    * It can be a single object, an array of objects, or null.
    * @return The sychronous data, if any, resulting from the invocation.
    * @throws java.rmi.RemoteException For network communication related
    * reasons.
    * @throws NoSuchMethodException If no matching method can be found.
    * @throws Exception If the internal item rejects the request, for any
    * application specific reason.
    */
   public Object invoke(String method, Object args) throws Exception {
      if (server == null) {
         this.server = (RemoteInvoke)args;
         return null;
      } else if (proxy == null) {
         if (handle.charAt(0) == '/') {
            InputStream is = ProxyLoader.class.getResourceAsStream(handle);
            ObjectInputStream ois = new ObjectInputStream(is);
            proxy = (Invoke)ois.readObject();
            ois.close();
            is.close();
         } else proxy = (Invoke)Class.forName(handle).newInstance();
         proxy.invoke("setItem", server);
      }
      return proxy.invoke(method, args);
   }
   /**
    * This method is used to identify the contents of the ProxyLoader.
    * It returns the value stored in the handle member variable.  This is
    * primarily intended to assist in debugging.
    * @return The path and filename of the proxy resource, either class or
    * object.
    */
   public String toString() { return handle; }
}
