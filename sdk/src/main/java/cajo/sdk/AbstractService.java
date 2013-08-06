package cajo.sdk;

import gnu.cajo.invoke.Remote;
import gnu.cajo.utils.ItemServer;

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
 * The service is a publicly invocable ordinary Java object. All of its
 * public methods, either <i>instance or static,</i> can be invoked by remote
 * JVMs. It is recommended to use this class as a wrapper, exposing specific
 * methods of its internal utility objects. It is normally completely unaware
 * of the <a href=https://cajo.dev.java.net>cajo</a>
 * <a href=http://weblogs.java.net/blog/cajo/archive/2007/09/simple_interjvm.html>
 * grail</a> framework.
 * <br>It can use its <a href=https://cajo.dev.java.net/nonav/docs/gnu/cajo/Cajo.html>
 * Cajo</a> member reference to find and interact with other services. A
 * service being an atomic element of functionality, could belong to multiple
 * {@link AbstractServer server}s.<br>
 * All service object instances must assume that they will be used in a
 * multi-threaded <i>(i.e. reentrant)</i> environment, therefore protection
 * of non-threadsafe code regions is essential.
 * @see AbstractAgent
 * @see AbstractController
 * @author John Catherino
 */
public abstract class AbstractService extends AbstractReference
   implements IService {
   private final Object initArgs[];
   private final String controller, name;
   private Remote remref;
   /**
    * This is the reference to the logged service. It is made accessible to
    * subclasses, to periodically change out the log stream, if necessary.
    */
   protected final Logger logger;
   /**
    * The constructor will locally bind the service at its host under the
    * name provided, and join the <a href=http://weblogs.java.net/blog/cajo/archive/2007/09/simple_interjvm.html>
    * cajo federation</a>. The constructor can take arguments, there is no
    * artificial restriction that a service must have a no-arg constructor.
    * A service is of course free to launch threads in its constructor, if
    * it wishes, to perform functionality asynchronously of its
    * <i>event-driven</i> method invocations by clients.
    * @param controller The class name of the <i>optional</i> {@link
    * AbstractController controller} to instantiate upon arrival at the
    * client JVM <i>(e.g. controller.Controller)</i> it can be either
    * hard-coded, read from a file, or a system property.<br>
    * <i><u>NB</u>:</i> The controller argument can be null, if the service
    * wishes to provide no controller, nor GUI for clients.
    * @param name The name under which to bind the service in the local
    * rmi registry. These services can be then accessed through their
    * registry URL <i>(e.g. //myhost:1198/main)</i>.<br>
    * <i><u>NB</u>:</i> If a service already exists under this name, this
    * service will replace it.
    * @param log The output stream to write the service invocation events,
    * if this argument is null, the events will be written to System.out
    * <br><i><u>NB</u>:</i> it is <i>highly</i> recommended to build oos
    * atop a BufferedOutputStream, and if a lot of invocations are being
    * logged, possibly a ZippedOutputStream as well. The stream could be from
    * a socket, if logging to another machine is desired. The same stream
    * can be used by multiple services.
    */
   @SuppressWarnings("hiding")
   protected AbstractService(String controller, String name,
      java.io.ObjectOutputStream log) {
      super(null);
      descriptors.add(new Descriptor("getController",
         "This <i>canonical</i> method is used to obtain a locally " +
         "running <i>smart</i> reference to the remote object. It allows " +
         "the server to offload some compute and memory load to the " +
         "client. It is not required for a client to request a service's " +
         "controller, and some services may not support controller objects " +
         "at all. However, requesting a service controller is considered " +
         "a <i>common courtesy.</i> Correspondingly, controllers should " +
         "take pains to minimise the compute and memory resources used of " +
         "the client.",
         null, // method accepts no arguments
         new String[] { // return
            "java.lang.Object", controller != null ?
            "A locally running object create a reference to the actual " +
            "controller object. The object can be saved to storage, or " +
            "passed to other remote JVMs, to provide controllers to other " +
            "services." :
            "This service furnishes no controller, the return will be null."
         }, null // only throws java.rmi.RemoteException
      ));
      descriptors.add(new Descriptor("acceptAgent",
         "This <i>canonical</i> method is used to install client or " +
         "remote service agents in this JVM. It allows local usage of the " +
         "service item in cases where network traffic could be greatly " +
         "reduced.",
         new String[][] { // arguments
            {
               "java.lang.Object",
               "The client/remote service agent object. Its <tt>init</tt> " +
               "method will be called, passing in a local reference to " +
               "the service object"
            },
         }, new String[] { // return
            "Object", System.getProperty("java.rmi.server.useCodebaseOnly").
            equals("false") ?
            "A remote reference to the agent object on which the sender " +
            "may communicate with it, in whatever manner it wishes." :
            "This service does <i><u>not</u></i> accept agents."
         }, new String[][] { // exceptions
            {
               "java.lang.ClassNotFoundException",
               "If the service does not accept agents"
            }, {
               "java.lang.NoSuchMethodException",
               "If the agent does not implement a public <tt>init</tt> " +
               "method <i>(static or instance)</i>"
            }, {
               "java.lang.Exception",
               "Should the agent reject the initialisation, or for any " +
               "agent specific reasons"
            }, REMOTEEXCEPTION, // throws java.rmi.RemoteException too
         }
      ));
      descriptors.add(new Descriptor("services",
         "This <i>canonical</i> static function is used to request all " +
         "service objects exported by the server, in inclusive of this one.",
         null, // method accepts no arguments
         new String[] { // return
            "String[]", "An array of the names of all services published " +
            "by this service's JVM."
         }, null // only throws java.rmi.RemoteException
      ));
      descriptors.add(new Descriptor("getService",
         "This <i>canonical</i> static function is used to to request a " +
         "service object exported by this JVM.",
         new String[][] { // arguments
            {
               "java.lang.String",
               "The name under which the server JVM is publishing the service."
            },
         }, new String[] { // return
            "Object", "A remote reference to the service object on which " +
            "the caller may communicate with it, in whatever manner it " +
            "wishes."
         }, new String[][] { // exceptions
            {
               "java.rmi.NotBoundException",
               "If the service is not currently available"
            },
         }
      ));
      this.controller = controller;
      this.name = name;
      logger = new Logger(this, log); // wrap service for logging
      initArgs = new Object[] { cajo, logger };
      cajo = AbstractServer.cajo; // used to find other services
      serviceRef = logger; // for now this object is not remote
   }
   /**
    * This method will make the service available for remote use, it can also
    * be used following a shutdown call.
    * @throws java.io.IOException For network related issues,
    * IllegalStateException if already started
    */
   protected final void startup() throws java.io.IOException {
      if (remref != null) throw new IllegalStateException("already started");
      serviceRef = new Remote(logger); // make service remotely callable
      ItemServer.bind(serviceRef, name); // put in local static registry
      remref = cajo.export(this, logger); // add to cajo dynamic federation
   }
   /**
    * This method disconnects the service from any further remote client use. The
    * It can be put back into service by calling its startup method.<br>
    * <i><u>NB</u>:</i> If the service has accepted any remote agents, they
    * will <i>continue</i> to operate, as they have a local reference to the
    * service.
    * @throws java.rmi.NoSuchObjectException If the service has already been
    * shut down.
    */
   protected final void shutdown() throws java.rmi.NoSuchObjectException {
      try {
         cajo.unexport(remref);
         remref.unexport(true);
         ItemServer.unbind(name);
         ((Remote)serviceRef).unexport(true);
      } finally { remref = null; }
   }
   /** {@inheritDoc} */
   @Override
   public final Object getController() { // support hot-swapping at runtime
      return controller == null ? null :
         new ProxyLoader(controller, serviceRef);
   }
   /** {@inheritDoc} */
   @Override
   public final Object acceptAgent(Object agent) throws Exception {
      invoke(agent, "init", initArgs);
      return new Remote(agent).clientScope();
   }
   /**
    * This method provides a means to identify this service.
    * @return An identifier <i>(not a description)</i> of the service
    */
   @Override
   public String toString() { return "AbstractService"; }
   /**
    * This utility function returns the names of the services currently
    * bound locally at this JVM. It is public, so that it may be called from
    * other remote objects. Even static methods are remotely callable in cajo,
    * they can be included in a client's local proxy interface as well.
    * @return All of the services currently available for use
    */
   public static final String[] services() { return ItemServer.list(); }
   /**
    * This utility function returns a remote service reference by name from
    * the server furnishing this service. Even static methods are remotely
    * callable in cajo, they can be included in a client's local proxy
    * interface as well.
    * @param name The name of the service reference to fetch
    * @return A remote reference to the named service, if needed, this
    * reference can be passed between JVMs, it can also be wrapped with a
    * {@link Logger Logger}, for debug purposes
    * @throws java.rmi.NotBoundException If the service is not currently
    * available
    */
   public static final Object getService(String name)
      throws java.rmi.NotBoundException {
      return ItemServer.lookup(name);
   }
   /**
    * This method is called just before the service goes live on the network.
    * It was intended to signal the service to start any internal processing
    * threads. Better is to start internal processing threads in the service
    * constructor. This method now performs no function.
    * @deprecated The fundamental problem with this method is that it is
    * public, meaning it can also be called by clients. Without proper
    * safeguarding, that could result in potentially serious problems.
    */
   @Deprecated
   public static final void startThread() {}
}
