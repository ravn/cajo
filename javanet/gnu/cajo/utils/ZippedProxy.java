package gnu.cajo.utils;

import gnu.cajo.invoke.Invoke;
import java.rmi.MarshalledObject;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
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
 * This class is used to transfer a zipped marshalled object (zedmob) of its
 * internal proxy item. Decompressing the proxy on arrival at the client. This
 * will involve a small runtime hit, however, if the proxy is large and highly
 * compressable, or the data link is slow, or the cost per byte to transmit
 * data is high, this can become very advantageous.  The proxy is serialized
 * and compressed once the server reference is provided to it, after that it
 * can no longer be modified at the server.<p>
 * <b>If the server loads proxies into its runtime, i.e. it is not using a
 * ProxyLoader, it is <i>highly recommended</i>  to use a zipped proxies, since
 * processor horsepower increases steadily, but long-haul network bandwidth is
 * not.<p>
 * The class is not final, to allow subclasses to automatically construct
 * with a proxy of their choosing.  Also, a subclass could optionally
 * encrypt the payload before sending, and decrypt it on arrival.
 *
 * @version 1.0, 01-Nov-99 Initial release
 * @author John Catherino
 */
public class ZippedProxy implements Invoke {
   /**
    * The compressed serialized proxy object.  It is created on server
    * assignment by when binding at the hosting VM.  This will save time
    * and memory, especially if the same proxy is sent many times.
    * It is nulled at the client, following proxy decompression, to allow
    * the unneeded memory to be garbage collected.
    */
   protected byte payload[];
   /**
    * A reference to the internal proxy object, before serialization at the
    * server, and when decompressed on arrival at the host.  It is nulled
    * after serialization at the server, to allow its unused memory to be
    * garbage collected, since the paylod image can no longer be updated.
    */
   protected transient Invoke proxy;
   /**
    * The constructor retains the reference to the proxy, until the server
    * reference is provided, after that, it is serialized into the payload
    * array, and discarded.  If there are no other references to the proxy,
    * it will be garbage collected.
    * @param proxy The proxy item.
    */
   public ZippedProxy(Invoke proxy) { this.proxy = proxy; }
   /**
    * The interface to the proxy wrapper.  It is only to be called once by
    * the sending VM, to store a remote reference to itself.  Following that,
    * it is only invoked by the receiving VM.  Following its arrival at the
    * host VM, the proxy will be decompressed at its first invocation.
    * @param data First the remote server reference, following that,
    * callback data from any outside VMs given the proxy reference.
    * @throws Exception For any proxy-specific reasons.
    * @throws java.rmi.RemoteException For any network related errors.
    */
   public final Object invoke(String method, Object args) throws Exception {
      if (payload == null) {
         proxy.invoke(method, args);
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         GZIPOutputStream      gzos = new GZIPOutputStream(baos);
         ObjectOutputStream    toos = new ObjectOutputStream(gzos);
         toos.writeObject(new MarshalledObject(proxy));
         toos.close();
         gzos.close();
         baos.close();
         payload = baos.toByteArray();
         proxy = null;
         return null;
      } else if (proxy == null) {
         ByteArrayInputStream bais = new ByteArrayInputStream(payload);
         GZIPInputStream      gzis = new GZIPInputStream(bais);
         ObjectInputStream    tois = new ObjectInputStream(gzis);
         proxy = (Invoke)((MarshalledObject)tois.readObject()).get();
         tois.close();
         gzis.close();
         bais.close();
         payload = new byte[] {};
      }
      return proxy.invoke(method, args);
   }
}
