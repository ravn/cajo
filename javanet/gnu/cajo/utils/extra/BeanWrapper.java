package gnu.cajo.utils.extra;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.net.MalformedURLException;
import gnu.cajo.invoke.Remote;
import gnu.cajo.invoke.Invoke;

/*
 * Generic Object Invocation Wrapper (with delayed loading on deserialization)
 * Copyright (c) 2004 John Catherino
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
 * The class for wrapping objects, remote references, and proxies for
 * syntactical transparency with the remaining codebase. <i>Note:</i> When
 * serialized, this class will save its wrapped object in a <u>separate</u>
 * named file. Subsequently, the wrapped object bean will <i>only</i> be
 * deserialized into the local runtime environment if it is needed. This
 * late loading paradigm can be extremely beneficial for large collections of
 * infrequently referenced objects. Otherwise it functions identically to the
 * Wrapper class, except that Wrapper <i>always</i> loads its wrapped object
 * on deserialization.
 *
 * @version 1.0, 01-May-04 Initial release
 * @author John Catherino
 */
public class BeanWrapper implements Invoke {
   private void writeObject(ObjectOutputStream out) throws IOException {
      if (modified) {
         modified = false;
         Remote.zedmob(new FileOutputStream(name), object);
      }
      out.defaultWriteObject();
   }
   /**
    * The object being wrapped by the reflection based invocation paradigm.
    * It may be a reference to a remote object, or a proxy sent by a remote
    * object, or even an ordinary local object.
    */
   protected transient Object object;
   /**
    * The flag to indicate if the wrapped object has had its local state
    * modified. The member is transient, therefore it will always be
    * deserialized as false. Only when it's true will a new copy of the
    * object be serialized to disc. Therefore it is <u>very</u> important
    * that all subclass methods which will result in a modification of the
    * inner object's <i>local</i> state set this member to true, to ensure
    * its updated state will be recorded. This is purely a performance
    * optimization, to prevent unchanged objects from being written to disc
    * during serialization.
    */
   protected transient boolean modified;
   /**
    * The path/filename.ext in which to save the zedmob of the wrapped
    * object, when the wrapper object is serialzed. If the specified file does
    * not exist, it will be created, if it does, it will be overwritten. On
    * deserialization, it will not be loaded, unless it is needed.
    */
   protected final String name;
   /**
    * This constructor loads an object, or a zipped marshalled object (zedmob)
    * from a URL, a file, or from a remote rmiregistry. If the object is in a
    * local file, it can be either inside the application's jar file, or on its
    * local file system. This class extends Wrapper in that the internal object
    * will be saved separately from this object, when serialized. On
    * deserialization, the wrapped object will only be loaded if needed. This
    * can save a lot of space and time, for very large collections of
    * infrequently used objects. It will also flag the modified flag on
    * construction to ensure the wrapped object is saved on serialization, even
    * if it can be no further modified, like a remote reference for example.<p>
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
    * @param name The path/filename.ext in which to save the wrapped object,
    * if this object should ever need to be serialized.
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
   public BeanWrapper(String url, String name) throws RemoteException,
      NotBoundException, IOException, ClassNotFoundException,
      InstantiationException, IllegalAccessException, MalformedURLException {
      object = Remote.getItem(url);
      this.name = name;
      modified = true;
   }
   /**
    * The constructor loads accepts a serializable object to use. It is
    * typically used with local objects, acting in place of remote objects,
    * or with a reference to a remote item.
    * @param object The object to wrap and serialize with delayed
    * deserialization.
    * @param name The path/filename.ext in which to save the wrapped object,
    * if this object should ever need to be serialized.
    */
   public BeanWrapper(Object object, String name) {
      this.object = object;
      this.name = name;
      modified = true;
   }
   /**
    * This method returns checks for equality with the inner object instead of
    * the wrapper itself. This allows two different wrappers referencing an
    * equivalent inner object to return true. If the wrapped object has
    * not yet been deserialized, this will cause it to happen, which could
    * result in a NullPointerException on failure.
    */
   public boolean equals(Object o) {
      if (object == null)
         try { object = Remote.zedmob(new FileInputStream(name)); }
         catch(Exception x) { x.printStackTrace(System.err); } 
      return o.equals(object);
   }
   /**
    * This method returns the toString result of the inner object instead of
    * the wrapper itself. This allows two different wrappers referencing an
    * equivalent inner object to return an equivalent string. If the wrapped
    * object has not yet been deserialized, this will cause it to happen, which
    * could result in a NullPointerException on failure.
    */
   public String toString() {
      if (object == null)
         try { object = Remote.zedmob(new FileInputStream(name)); }
         catch(Exception x) { x.printStackTrace(System.err); } 
      return object.toString();
   }
   /**
    * This method <u><i>must</i></u> be called by all interface methods of the
    * subclass, as it will deserialize the wrapped object if it has not yet
    * been loaded.
    * @param method The method name to be invoked.
    * @param args The arguments to provide to the method for its invocation,
    * possibly null.
    * @return The resulting data, from the invocation, possibly null.
    * @throws IllegalArgumentException If the method argument is null.
    * @throws NoSuchMethodException If no matching method can be found.
    * @throws FileNotFoundException If the deserialization file name is
    * invalid, or the name does not match an existing file.
    * @throws IOException If the deserialization read operation is prohibited,
    * or failed.
    * @throws ClassNotFoundException If the wrapped object's codebase cannot be
    * found on deserialization.
    * @throws StreamCorruptedException If control information in the stream is
    * not consistent on deserialization.
    * @throws Exception If the inner object rejected the invocation, for
    * any application specific reasons.
    */
   public Object invoke(String method, Object args) throws Exception {
      if (object == null) object = Remote.zedmob(new FileInputStream(name));
      return Remote.invoke(object, method, args);
   }
}
