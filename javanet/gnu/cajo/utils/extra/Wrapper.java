package gnu.cajo.utils.extra;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.net.MalformedURLException;
import gnu.cajo.invoke.Remote;
import gnu.cajo.invoke.Invoke;
import gnu.cajo.invoke.RemoteInvoke;

/*
 * Generic Object Invocation Wrapper
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
 * The base class for wrapping objects, remote references, and proxies for
 * syntactical transparency with the remaining codebase. It allows the object
 * type to change between local object/remote/proxy, without affecting the
 * calling code. It enforces compile time type checking, and the standard
 * invocation systax, while still allowing invocation of methods via the
 * reflection based invoke paradigm. <i>Note:</i> A subclass could potentially
 * even <i>dynamically</i> change the inner object at <u>runtime</u>.
 *
 * @version 1.0, 27-Apr-04 Initial release
 * @author John Catherino
 */
public class Wrapper implements Invoke {
   /**
    * The object being wrapped by the reflection based invocation paradigm.
    * It may be a reference to a remote object, or a proxy sent by a remote
    * object, or even an ordinary local object. It is not declared final, as
    * to allow its representation to change, as required for performance or
    * other application specific reasons, for example to be further wrapped by
    * a {@link gnu.cajo.utils.MonitorItem MonitorItem}.
    */
   protected Object object;
   /**
    * This method is used to save the current state of the internal object as
    * a zipped MarshalledObject (zedmob) to a file. A subclass could override
    * this method, to write the zedmob to a socket, for remote storage, or
    * simply notify a remote item to save its state. It will obtain a
    * synchronization lock on the object before writing, to prevent storage of
    * a partially updated state. <i>Note:</i> the internal object must have its
    * non-threadsafe methods, or blocks, synchronized.
    * @param fileName The path/file.ext in which to save the zedmob. If it does
    * not exist it will be created. If it exists, it will be overwritten.
    * @throws FileNotFoundException If the file name is invalid, or the name
    * of a directory.
    * @throws IOException If the write operation is prohibited, or failed.
    * @throws NotSerializableException If the inner object is not serializable.
    */
   protected void serialize(String fileName) throws IOException {
      synchronized(object) {
         Remote.zedmob(new FileOutputStream(fileName), object);
      }
   }
   /**
    * This method is used to restore the current state of the internal object
    * from a zipped MarshalledObject (zedmob) in a file. If the internal
    * object already exists, it will be overwritten. This can be useful for the
    * performance of <i>'undo'</i> operations. A subclass could override this
    * method, to read the zedmob from a socket, from remote storage, or simply
    * request an current item reference or proxy.
    * @param fileName The path/file.ext from which to load the zedmob.
    * @throws FileNotFoundException If the file name is invalid, or the name
    * does not match an existing file.
    * @throws IOException If the read operation is prohibited, or failed.
    * @throws ClassNotFoundException If the stored object's codebase cannot be
    * found.
    * @throws StreamCorruptedException If control information in the stream is
    * not consistent.
    */
   protected void deserialize(String fileName)
      throws IOException, ClassNotFoundException {
      object = Remote.zedmob(new FileInputStream(fileName));
   }
   /**
    * The no-arg constructor does nothing, it is protected for use only by
    * subclasses.
    */
   protected Wrapper() {}
   /**
    * The constructor loads an object, or a zipped marshalled object (zedmob)
    * from a URL, a file, or from a remote rmiregistry. If the object is in a
    * local file, it can be either inside the application's jar file, or on its
    * local file system.<p>
    * Loading an item from a file can be specified in one of three ways:<p><ul>
    * <li>As a URL; in the format file://path/name
    * <li>As a class file; in the format path/name
    * <li>As a serialized item; in the format /path/name</ul><p>
    * File loading will first be attempted from within the application's jar
    * file, if that fails, it will then look in the local filesystem.
    * <i>Note:</i> any socket connections made by the incoming item cannot be
    * known at compile time, therefore proper operation if this VM is behind a
    * firewall could be blocked. Use behind a firewall would require knowing
    * all the ports that would be needed in advance, and enabling them before
    * loading the item.
    * @param url The URL where to get the object: file://, http://, ftp://,
    * /path/name, path/name, or //[host][:port]/[name]. The host, port,
    * and name, are all optional. If missing the host is presumed local, the
    * port 1099, and the name "main". The referenced resource can be
    * returned as a MarshalledObject, it will be extracted automatically.
    * If the URL is null, it will be assumed to be ///.
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
   public Wrapper(String url) throws RemoteException,
      NotBoundException, IOException, ClassNotFoundException,
      InstantiationException, IllegalAccessException, MalformedURLException {
      object = Remote.getItem(url);
   }
   /**
    * This method returns the hashcode of the inner object instead of
    * the wrapper itself. This allows two different wrappers referencing an
    * equivalent inner object to hash identically.
    */
   public int hashCode() { return object.hashCode(); }
   /**
    * This method returns checks for equality with the inner object instead of
    * the wrapper itself. This allows two different wrappers referencing an
    * equivalent inner object to return true.
    */
   public boolean equals(Object o) { return o.equals(object); }
   /**
    * This method returns the toString result of the inner object instead of
    * the wrapper itself. This allows two different wrappers referencing an
    * equivalent inner object to return the equivalent strings.
    */
   public String toString() { return object.toString(); }
   /**
    * This method is used to test if the inner object is a reference to a
    * remote object. This can be important to know as remote invocations are
    * not time deterministic, and not assured of execution, as with local
    * objects.
    * @return True if the inner object is remote, false otherwise.
    */
   public boolean isRemote() { return object instanceof RemoteInvoke; }
   /**
    * This method attempts to extract usage information about the inner object,
    * if the method is supported.
    * @return A detailed guide to object usage, preferably with examples, with
    * HTML markup permitted.
    * @throws NoSuchMethodException If the inner object does not support the
    * description method.
    * @throws Exception If the innter object rejected the invocation, for
    * any application specific reasons.
    */
   public String getDescription() throws Exception {
      return (String)invoke("getDescription", null);
   }
   /**
    * This method is used to bypass the normal compiletime type checking, and
    * execute methods directly on the inner object by name. It is primarily
    * used to support scripted invocations on an object, typically from a text
    * file. <i>Note:</i> Strong typechecking is still enforced, merely it must
    * be moved from compiletime to runtime.
    * @param method The method name to be invoked.
    * @param args The arguments to provide to the method for its invocation,
    * possibly null.
    * @return The resulting data, from the invocation, possibly null.
    * @throws IllegalArgumentException If the method argument is null.
    * @throws NoSuchMethodException If no matching method can be found.
    * @throws Exception If the inner object rejected the invocation, for
    * any application specific reasons.
    */
   public Object invoke(String method, Object args) throws Exception {
      return Remote.invoke(object, method, args);
   }
}
