package controller;

import view.View;
import java.util.concurrent.Future;
import java.rmi.RemoteException; // remote calls can fail

/**
 * This example class defines a controller, with an example client view.
 */
public final class Controller extends cajo.sdk.AbstractController
   implements IController {
   // This defines the controller's interface to its sending service. Only
   // the method signatures will be matched, therefore, the name and the
   // package of the controller interface do not matter. Instance methods of
   // the service can also be included, even though they are normally
   // prohibited in interfaces, the cajo framework will take care of it.
   // Similarly, the interface can declare static final fields, these will
   // be matched against static or instance final fields on the service.
   // If an interface method returns void, it will match any server return.
   // Feel free to rename this interface to something more meaningful, as it
   // is referenced only from within this class. The interface need not be
   // declared in the same class, and the sending service may specify more
   // functions than the controller uses. Generally it is a good idea to
   // declare that the methods throw RemoteException, since they are making
   // a network call, though this is optional.
   private interface HomeService { // example interface
      // String foo(String bar) throws RemoteException; // synchronous call or,
      Future<String> foo(String bar) throws RemoteException; // asynchronous
   }
   // The reference to the sending service, it is used to make callback
   // invocations.
   private final HomeService homeService;
   // The reference to the the view object the controller will instantiate,
   // listen to, and modify it at runtime.
   private transient View view;
   /**
    * The constructor assigns the service reference, describes its
    * functionality, and creates a local dynamic proxy to its {@link
    * service.Service service}.
    * @param serviceRef A reference to the sending service, on which the
    * controller may communicate with it
    */
   public Controller(Object serviceRef) {
      super(serviceRef);
      description = // describe functionality of this controller
         "This is an example implementation of a cajo controller, it is " +
         "for illustrative purposes.";
      addDescriptor( // provide controller function description
         "baz", // function name
         "This function simply calls the service's function.", // description
         new String[][] { // arguments
            {  "java.lang.String", // argument type
               "An incoming argument, it will be appended to the response " +
               "string returned by this function." // description
            },
         },
         new String[] { // method return description
            "java.lang.String", // return type
            "An indicator of successful completion, it will have the " +
            "contents of the incoming string appended to it."
         }, new String[][] { // exceptions thrown by this function
            { "java.lang.NullPointerException", // type
               "Thrown simply as an example, if the argument is null; " +
               "service methods are free to throw both checked and " +
               "unchecked exceptions."
            }, REMOTEEXCEPTION, // can throw java.rmi.RemoteException too
         }
      );
      // homeService = proxy(serviceRef, homeService.IService.class); // tight coupling
      homeService = proxy(serviceRef, HomeService.class); // loose coupling
   }
   /** {@inheritDoc} */
   @Override
   public void init(@SuppressWarnings("hiding") gnu.cajo.Cajo cajo) {
      super.init(cajo);
      System.out.println("Controller arrived!");
      // use the cajo object to connect to other services as needed
   }
   /** {@inheritDoc} */
   @Override
   public View getView() throws java.io.IOException {
      // the view is hard-coded it could be read from a file, or a property
      view = new View();
      view.center.display("service call result:\n > ");
      try { view.center.display(baz("hello")); } // for illustration
      catch(Exception x) { x.printStackTrace(); }
      // attach controller specific view action listeners here...
      return view;
   }
   /**
    * This example method can be called by the sending and receiving JVM.
    * @param bar An arbitrary argument
    * @return An arbitrary value
    */
   @Override
   public String baz(String bar) throws RemoteException {
      // return homeService.foo(null); // test error handling
      Future<String> future = homeService.foo(bar);
      String result = null;
      // it's silly to do a blocking get, rather periodically check isDone()
      try { result = future.get(1, java.util.concurrent.TimeUnit.SECONDS); }
      catch(InterruptedException x) {}
      catch(java.util.concurrent.ExecutionException x) {}
      catch(java.util.concurrent.TimeoutException x) {}
      return result;
   }
   /** {@inheritDoc} */
   @Override
   public String toString() { return "ExampleController"; }
}
