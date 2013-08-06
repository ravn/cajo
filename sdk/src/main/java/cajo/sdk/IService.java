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
 * This interface defines the canonical methods to be furnished by <i>all</i>
 * <a href=https://cajo.dev.java.net>cajo</a> services. The subclasses of
 * IService serve as the documentation for the API of the service.
 * <p>In addition to the effort for defining a good quality API, similar
 * effort should be put into the javadoc. Develop it from the viewpoint that
 * the service interface documentation is all a client should need, to fully
 * understand how to use the service.
 * @author John Catherino
 */
public interface IService {
   /**
    * This method is called by remote clients, to get a standardised html
    * encoded destription of the service's functions. The format is
    * invariant, to allow automated parsing, if desired.
    * @return A detailed description of the functionality of the service
    * @throws java.rmi.RemoteException For network related failure or
    * configuration issues
    */
   String getDescription() throws java.rmi.RemoteException;
   /**
    * This method is called by remote clients to request the service's
    * optionally implemented {@link AbstractController controller}, and its
    * potentially optional GUI {@link AbstractView view}. Requesting a
    * service's controller is recommended over direct invocation on the
    * service reference for four fundamental reasons:<p><ul>
    * <li>To allow the service to offload processing
    * <li>To provide higher performance to the receiving service
    * <li>To furnish additional retry/rollback functionality, as necessary
    * <li>To retain receiver state information for the sending service, as
    * necessary</ul><p>
    * Typically a service only furnishes one controller, but it is free to
    * furnish others via other methods unspecified here.<p>
    * <i><u>NB</u>:</i> Ability to receive remote controllers is <i>disabled</i>
    * by default, for security reasons; as malicious controllers could easily
    * hinder, or crash the server. Accepting controllers should only be done
    * in a trusted environment. To enable the acceptance of controllers
    * and agents, call the static <a href=https://cajo.dev.java.net/nonav/docs/gnu/cajo/utils/ItemServer.html#acceptProxies()>
    * ItemServer.acceptProxies()</a> method. (this would normally be done in
    * the server's Main class)<p>
    * *An important way to mitigate this risk, is to have the class
    * definitions needed by the remote server already part of this server's
    * local codebase. The service will then load its own version, rather than
    * dynamically, via the mobile code mechanism. This would <u>not</u>
    * require calling the <tt>ItemServer.acceptProxies()</tt> method.
    * @return Object A local reference to a service's controller object,
    * if needed, it can be passed between JVMs, it can also be wrapped with a
    * {@link Logger Logger}, for debug purposes
    * @throws java.rmi.RemoteException For network related failure or
    * configuration issues
    */
   Object getController() throws java.rmi.RemoteException;
   /**
    * This method allows remote clients to install local a
    * {@link AbstractAgent agent} to interact with the service resource
    * locally.<p>
    * The arriving agent object will have its <tt>init</tt> method
    * called with a local reference to the receiving service, on with to
    * operate. It could be considered the <i>logical inverse</i> of
    * requesting a service's controller. The agent can use the service's
    * static Cajo object, to request other remote server resources. It is
    * used for similar reasons as controllers; to reduce network traffic if a
    * lot of interaction is required with the service, which would also
    * improve performance.<p>
    * This technique is often used to support the paradigm: <i>bring the
    * code to the data.</i> I.e. typically the size of the code to manipulate
    * data sets is <i>much</i> smaller; this can be used to greatly reduce
    * network traffic of sending large data blocks, and improve system
    * efficiency as well.<p>
    * If this method is overridden, for example to identify, authenticate,
    * authorise etc; be sure to call super.acceptAgent(agent) to complete the
    * acceptance process, or throw a rejection exception. It can also be
    * overridden to forward the agent to an alternate service.<p>
    * <i><u>NB</u>:</i> Ability to receive remote agents is <i>disabled</i>
    * by default, for security reasons; as malicious agents could easily
    * hinder, or crash the server. Accepting agents should only
    * be done in a trusted environment. To enable the acceptance of agents
    * <i>(and controllers)</i> call the static <a href=https://cajo.dev.java.net/nonav/docs/gnu/cajo/utils/ItemServer.html#acceptProxies()>
    * ItemServer.acceptProxies()</a> method. (this would normally be done in
    * the server's Main class)<p>
    * *An important way to mitigate this risk, is to have the class
    * definitions needed by the client agent already part of the server's
    * local codebase. The service will then load its own version, rather than
    * dynamically, via the mobile code mechanism. This would <u>not</u>
    * require calling the <tt>ItemServer.acceptProxies()</tt> method.
    * @param agent The client's object, which it wants to run within the
    * service JVM, presumably to improve performance by reducing network
    * traffic, it will be extracted from the MarshalledObject, then have its
    * <tt>init</tt> method called, passing in a local reference to the
    * service object
    * @return A remote reference to the installed agent, on which the sender
    * may communicate with it, in whatever manner it wishes, if needed, it
    * can be passed between JVMs, it can also be wrapped with a {@link
    * Logger Logger}, for debug purposes
    * @throws NoSuchMethodException If the agent does not implement a public
    * init method to accept the local service reference
    * @throws java.rmi.RemoteException For network related failure or
    * configuration issues
    * @throws Exception If the agent rejects its initialisation for some
    * agent-specific reason
    */
   Object acceptAgent(Object agent) throws Exception;
}
