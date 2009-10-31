package gnu.cajo;

/*
 * Proxy Enabled Service Definiton.
 * Copyright (C) 2009 John Catherino
 * The cajo project: https://cajo.dev.java.net
 *
 * For issues or suggestions mailto:cajo@dev.java.net
 *
 * This file Service.java is part of the cajo library.
 *
 * The cajo library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public Licence as published
 * by the Free Software Foundation, at version 3 of the licence, or (at your
 * option) any later version.
 *
 * The cajo library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public Licence for more details.
 *
 * You should have received a copy of the GNU Lesser General Public Licence
 * along with this library. If not, see http://www.gnu.org/licenses/lgpl.html
 */

/**
 * This class defines a proxy enabled network service. It is intended both
 * as a fundamental pattern, and an example of a service specification. It
 * is implemented by services that wish to include proxy functionality. A
 * proxy enabled service means one that either furnishes, or accepts
 * proxies, or both.
 * <p>
 * This service would typically be used in situations where the benefit
 * is greater to send all, or a portion, of the service code to the client;
 * rather than having a lot of data being passed back or forth.
 * <p>
 * All services are defined as <i>plain-old</i> Java interfaces. These
 * interfaces may contain:<ul>
 * <li> Manifest constants: any static final objects or primitives of use
 * <li> Custom inner class definitions for:
 * <ul><li>arguments <li>returns <li>exceptions</ul>
 * <li> Custom inner interface definitons: used for either arguments, or
 * returns
 * <li> The collection of shared functions implemented by the service</ul>
 * <p>
 * Technically speaking, these service signatures may be either functions
 * (i.e. static) or instance methods. They should be considered
 * <i>referentially opaque</i>; i.e. invoking the same service function, with
 * the same arguments, at different times is <i>not</i> guaranteed to return
 * the same result. Also, all service methods are <i>reentrant,</i> meaning
 * that multiple clients may be executing them <i>simultaneously.</i>
 * <p>
 * A single JVM may furnish as many service objects as it wishes. Normally,
 * related service interfaces are grouped into packages. Typically the
 * javadoc package.html file is used to provide a detailed explanation of the
 * service collection architecture. The package may also contain any custom
 * classes shared between the service interfaces, specifically; objects,
 * interfaces, and exceptions. Once a service interface is defined and
 * distributed, it should be considered <tt>immutable</tt>. Service feature
 * enhancements should be handled through <i>subclasses</i> of the
 * original service interface, to ensure backward compatibility.
 * <p>
 * <b>NB:</b> this service interface is completely cajo project agnostic, as
 * all service definitions properly should be. It should also be well
 * remembered, whilst not explicitly declared, any service method invocation
 * could fail for network related reasons, resulting in a
 * java.rmi.RemoteException being thrown implicitly.
 *
 * @author <a href=http://wiki.java.net/bin/view/People/JohnCatherino>
 * John Catherino</a>
 * @version 1.0, 17-Oct-09
 */
public interface Service {
   /**
    * This interface is used by clients, to send their proxy objects to
    * remote services. A proxy is a serialisable object that on arrival at
    * the service, is initialised with a local reference to the service
    * object on which to perform its work.
    */
   interface Proxy extends java.io.Serializable {
      /**
       * This method is called by the service JVM, upon receiving a client
       * proxy object. The proxy can then prepare itself for operation.
       * However, this method should return quickly; therefore, clients
       * requiring lengthy initialisation times should perform such work in
       * an internally created thread.
       * @param localService A local reference to the service object
       */
      void init(Object localService); // called by service on arrival
   }
   /**
    * This method is used to send a client's proxy code to run at the
    * service. The client's code runs in the address space of the service
    * JVM. A proxy enabled service need not support client proxies, in which
    * case a ClassNotFound exception will be thrown by the service,
    * implicitly.
    * @param proxy An object to be instantiated at the service, which will
    * be provided a local reference to the service upon its arrival
    * @return A remote reference to the proxy object, installed in the
    * service JVM, over which the client may communicate with it
    * @throws ClassNotFoundException If the service does not accept
    * client proxies
    */
   Object sendProxy(Proxy proxy) throws ClassNotFoundException;
   /**
    * This method is used to request a client-side running, server proxy
    * interface. It is considered an <i>important & common courtesy</i> of
    * clients to request it, before attempting to use the service reference
    * directly. It allows the service to potentially offload some computing
    * load to the client, temporarily.
    * @return A local object implementing the service interface, which is
    * typically internally in contact with the remote service object.
    * <b>NB:</b>If a service does not support client proxies, it will return
    * <i><u>null</u></i>.
    */
   Object requestProxy();
   /**
    * This method whilst semantically unrelated to this service, is just too
    * useful to leave out. Services typically exist as either development,
    * or production. Most often, the <i>unreal,</i> i.e. test/demo services,
    * are being used to validate infrastructure, communication, and
    * performance, or to provide simulated functionality.
    * @return False if this service is a test or demonstration
    * implementation, true if this is a production service
    */
   boolean isReal();
}
