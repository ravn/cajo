package cajo.sdk;

/* Copyright 2010 John Catherino
 * The cajo project: http://cajo.java.net
 *
 * Licensed under the Apache Licence, Version 2.0 (the "Licence"); you may
 * not use this file except in compliance with the licence. You may obtain a
 * copy of the licence at
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
 * This <i>internal use only</i> helper class is used by service classes to
 * decouple themselves from controllers and agents. It may also be passed
 * freely between JVMs. It is normally obtained when requesting a controller
 * from a service; one is also typically sent to a JVM when installing an
 * agent.
 * @author John Catherino
 */
final class ProxyLoader implements gnu.cajo.invoke.Invoke {
   private static final long serialVersionUID = 0L;
   private static final Class<?> OBJECT[] = { Object.class };
   private transient String name;
   private transient Object object;
   private final Object ref[] = new Object[1];
   private java.rmi.MarshalledObject<Class<?>> mob;
   private void writeObject(java.io.ObjectOutputStream oos)
      throws java.io.IOException { // when serialising...
      if (mob == null) try { // lazily instantiate payload to conserve memory
         mob = new java.rmi.MarshalledObject<Class<?>>
            (ClassLoader.getSystemClassLoader().loadClass(name));
      } catch(ClassNotFoundException x) { throw new java.io.IOException(x); }
      oos.defaultWriteObject();
   }
   private void readObject(java.io.ObjectInputStream ois)
      throws ClassNotFoundException, java.io.IOException {
      ois.defaultReadObject();
      try { object = mob.get().getConstructor(OBJECT).newInstance(ref); }
      catch(Exception x) { throw new java.io.IOException(x); }
   }
   /**
    * The constructor saves the details of the controller or agent class to
    * instantiate for use at a remote JVM.
    * @param name The name of the controller or agent class, e.g.
    * controller.Controller, or agent.Agent; this string can be hard-coded,
    * loaded from a file, a system property, even the command line
    * @param ref The reference to the service to be used by the instance of
    * the agent or controller
    */
   @SuppressWarnings("hiding")
   ProxyLoader(String name, Object ref) {
      this.name   = name;
      this.ref[0] = ref;
   }
   /**
    * This method simply passes all method invocations onto the wrapped
    * controller or agent object. By implementing the invoke interface, all
    * client method invocations are automatically passed to this method. This
    * technique can also be used to intercept, alter arguments, and modify
    * returns, of a wrapped object.
    * @param  method The name of method to invoke
    * @param args The arguments to provide to the method for its invocation,
    * if any
    * @return The synchronous data, if any, resulting from the invocation
    * @throws java.rmi.RemoteException For network communication related
    * reasons
    * @throws Exception If the wrapped object rejects the invocation, for
    * any application specific reason
    */
   @Override
   public Object invoke(String method, Object args) throws Exception {
      return gnu.cajo.invoke.Remote.invoke(object, method, args);
   }
   /**
    * This method is used to describe the ProxyLoader payload. It is
    * primarily used by the {@link Logger Logger} class.
    * @return The class name referenced in the ProxyLoader and its service
    * reference descriptor
    */
   @Override
   public String toString() {
      return String.format("ProxyLoader->%s@%s", name, ref[0]);
   }
}
