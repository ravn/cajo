package util;

/*
 * The base interface for a service of a cajo grail server.
 * The cajo project: https://cajo.dev.java.net
 * For issues or suggestions mailto:cajo@dev.java.net
 * This interface is released into the public domain.
 * Written by John Catherino
 */

/**
 * This interface defines the canonical functions expected to be furnished
 * by <i>all</i> <a href=https://cajo.dev.java.net>cajo</a> services. It is
 * normally subclassed, to define application specific service function
 * definitions.
 */
public interface IBaseService {
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
    * optionally implemented controller, and its potentially its optional GUI
    * view.
    * @return java.rmi.MarshalledObject The controller object, embedded in
    * a marshalled object, to preserve its codebase annotation. The
    * controller is instantiated by calling the MarshalledObject's
    * <tt>get();</tt> method. It can also return <tt>null,</tt> if the
    * service does not support a controller.<br><i><u>NB</u>:</i> this
    * reference is serialisable, and may be freely passed between JVMs, the
    * controller extracted via its <tt>get();</tt> method normally will not
    * properly pass. (unless all class definitions are already in the
    * recipient's classpath)
    * @throws java.rmi.RemoteException For network related failure or
    * configuration issues
    */
   java.rmi.MarshalledObject getController() throws java.rmi.RemoteException;
   /**
    * This method allows remote clients to install local proxies (i.e.
    * controllers) to interact with the service resource locally.<p>
    * The arriving proxy object will have its (static or instance)
    * <tt>init</tt> method called with a local reference to the service
    * object, on with to operate. It could be considered the <i>logical
    * inverse</i> of requesting a service's controller. The proxy can use
    * the service's static Cajo object, to request supplemental remote
    * resources.<p>
    * Interestingly, an arriving proxy <i>can</i> open windows on the
    * host machine; whilst this <i>could</i> be a very interesting feature
    * if the receiver is expecting it, if not, this would be considered very
    * rude.<p>
    * This technique is often used to support the paradigm: <i>bring the
    * code to the server.</i> Often the size of the code to manipulate large
    * data sets is <i>much</i> smaller; this can be used to greatly reduce
    * network traffic of sending large data blocks, and improve system
    * efficiency as well.<p>
    * <i><u>NB</u>:</i> Ability to receive remote proxies is <i>disabled</i>
    * by default, for security reasons; as malicious proxies could easily
    * hinder, monitor, or crash the server. Accepting proxies should only
    * be done in a trusted environment. To enable the acceptance of proxies,
    * call the static <tt>gnu.cajo.utils.ItemServer.acceptProxies();</tt>
    * method. (this would normally be done in the server's Main class)<p>
    * *An important way to mitigate this risk, is to have the class
    * definitions needed by the client proxy already part of the server's
    * local codebase. The service will then load its own version, rather than
    * dynamically, via the mobile code mechanism. This would <u><b>not</b></u>
    * require calling the <tt>ItemServer.acceptProxies()</tt> method. This is
    * handy for clients and services who know each other well.
    * @param proxy The client's object, which it wants to run within the
    * service JVM, presumably to improve performance by reducing network
    * traffic, it will be extracted from the MarshalledObject, then have its
    * <tt>init</tt> method called, passing in a local reference to the
    * service object
    * @return A remote reference to the installed proxy, on which the client
    * may communicate with it, in whatever manner it wishes
    * @throws NoSuchMethodException If the proxy does not implement a public
    * init method (static or instance) to accept the service reference
    * @throws Exception If the proxy rejects the initialisation, for any
    * application specific reason
    * @throws java.rmi.RemoteException For network related failure or
    * configuration issues
    */
   Object sendProxy(java.rmi.MarshalledObject proxy) throws Exception;
}
