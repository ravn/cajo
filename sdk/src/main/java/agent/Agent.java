package agent;

/**
 * This example class defines a mobile service agent.
 */
public final class Agent extends cajo.sdk.AbstractAgent implements IAgent {
   // This defines the agent's interface to its sending service. Only the
   // method signatures will be matched, therefore, the name and the package
   // of the interface do not matter. Instance methods of the service can also
   // be included, even though they are normally prohibited in interfaces,
   // the cajo framework will take care of it. Similarly, the interface can
   // declare static final fields, these will be matched against final
   // static, or instance, fields on the service. If an interface method
   // returns void, it will match any server return. Feel free to rename this
   // interface to something more meaningful, as it is referenced only from
   // within this class. The interface need not be declared in the same class
   // as the agent, and the sending service may implement many more functions
   // than the agent uses. A java.rmi.RemoteException could be thrown for a
   // network related failure. Generally it is a good idea to declare that
   // the calls to home service methods throw RemoteException, since they are
   // made over the network, though this is optional.
   private interface HomeService {
      Object foo(String bar) throws java.rmi.RemoteException;
      // Future<Object> foo(String bar) throws java.rmi.RemoteException;
   }
   // This defines the agent's interface to its receiving service. Only the
   // method signatures will be matched, therefore, the name and the package
   // of the interface do not matter. Instance methods of the service can
   // also be included, even though they are normally prohibited in
   // interfaces, the cajo framework will take care of it. Similarly, the
   // interface can declare static final fields, these will be matched static
   // or instance final fields on the service. Feel free to rename this
   // interface to something more meaningful, as it is referenced only from
   // within this class. The interface need not be declared in the same class
   // as the agent, and the sending service may implement many more functions
   // than the agent uses.
   private interface LocalService {
      Object baz(String foo); // synchronous call
      // Future<Object> baz(String foo); // or call it asynchronously
   }
   // The remote reference to the sending service, it is used to make
   // callback invocations.
   private final HomeService homeService;
   // The local reference to the receiving service, on which this agent
   // will interact autonomously.
   private transient LocalService localService;
   /**
    * The constructor simply proxies the reference to the sending {@link
    * service.Service service} into a local interface for its needs.
    * @param serviceRef A reference on which the agent can communicate
    * back with its sending service
    */
   public Agent(Object serviceRef) {
      super(serviceRef);
      // homeService = proxy(serviceRef, service.IService.class); // tight coupling
      homeService = proxy(serviceRef, HomeService.class); // loose coupling
   }
   /** {@inheritDoc} */
   @Override
   @SuppressWarnings("hiding") // assigning localService
   public void init(gnu.cajo.Cajo cajo, Object localService) {
      super.init(cajo, localService);
      this.localService = proxy(localService, LocalService.class);
      System.out.println("Agent has arrived at service!");
      // use the cajo object to connect to other services as needed
   }
   /** {@inheritDoc} */
   @Override
   public String bar(String baz) { // this method can be called by the sender
      try { homeService.foo("hello from agent"); } // can talk to sender
      catch(java.rmi.RemoteException x) {} // call home can fail
      return localService.baz(baz).toString(); //can talk to receiver
   }
   /** {@inheritDoc} */
   @Override
   public String toString() { return "ExampleAgent"; }
}
