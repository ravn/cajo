package gnu.cajo.utils;

import gnu.cajo.invoke.*;
import java.io.*;

/*
 * Compressed Proxy Wrapper
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
 * This class is used to transfer its internal proxy item as a zipped
 * marshalled object (zedmob). It will decompress the proxy automatically on
 * arrival at the client. This will incur a small runtime penalty, however, if
 * the proxy is large and highly compressable, or the data link is slow, or the
 * cost per byte to transmit data is high, this can become highly advantageous.
 * The proxy is serialized and compressed once the server reference is
 * provided to it by the {@link ItemServer ItemServer} during the binding of
 * its server item. After that it can no longer be modified at the server.<p>
 * If the server loads proxies into its runtime, i.e. it is not using a
 * {@link ProxyLoader ProxyLoader}, it is <i>highly recommended</i>  to use
 *  zipped proxies, since processor horsepower is increaseing steadily, while
 * long-haul network bandwidth is not.<p>
 * The class is not final; this allows subclasses to have no-arg constructors,
 * using a proxy of their choosing.  Also, a subclass could optionally
 * encrypt the payload before sending, and decrypt it on arrival, if necessary.
 *
 * @version 1.0, 01-Nov-99 Initial release
 * @author John Catherino
 */
public class ZippedProxy implements Invoke {
   /**
    * The compressed serialized proxy object.  It is created on server
    * assignment when binding at the hosting VM.  This is to save time
    * and memory, especially if the same proxy is sent many times, at the
    * expense of no longer being able to modify the proxy. It is nulled at the
    * client, following proxy decompression, to allow the unneeded memory to
    * be garbage collected.
    */
   protected byte payload[];
   /**
    * A reference to the internal proxy object, before serialization at the
    * server, and when decompressed on arrival at the host.  It is nulled
    * after serialization at the server, to allow its unused memory to be
    * garbage collected, since the paylod image can no longer be updated.
    */
   protected transient Object proxy;
   /**
    * The constructor retains the reference to the proxy, until the server
    * reference is provided by the {@link ItemServer ItemServer}, after that,
    * it is serialized into the payload array, and discarded.  If there are no
    * other references to the proxy, it will be garbage collected.
    * @param proxy The internal proxy item.
    */
   public ZippedProxy(Object proxy) { this.proxy = proxy; }
   /**
    * The interface to the proxy wrapper.  It is only to be called once by
    * the sending VM, to store a remote reference to itself.  Following that,
    * it is only invoked by the receiving VM.  Following its arrival at the
    * host VM, the proxy will be decompressed at its first invocation.
    * @param method The public method to invoke on the internal proxy item,
    * following the initial invocation, which is used to compress the proxy.
    * @param args First the remote server reference, following that,
    * callback data from any outside VMs to be given to the internal proxy.
    * @throws Exception For any proxy-specific reasons.
    * @throws java.rmi.RemoteException For any network related errors.
    */
   public final Object invoke(String method, Object args) throws Exception {
      if (payload == null) {
         Remote.invoke(proxy, method, args);
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         Remote.zedmob(baos, proxy);
         payload = baos.toByteArray();
         proxy = null;
         return null;
      } else if (proxy == null) {
         ByteArrayInputStream bais = new ByteArrayInputStream(payload);
         proxy = Remote.zedmob(bais);
         payload = new byte[] {};
      }
      return Remote.invoke(proxy, method, args);
   }
}
