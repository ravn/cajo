package service;

/**
 * This interface defines the demonstration methods for the example service.
 * It is normally a starting point, to embellish with application specific
 * function definitions of your own. A server can furnish as many services
 * as it wishes.
 * <br>In addition to the effort for defining a good quality API, similar
 * effort should be put into the javadoc. Develop it from the viewpoint that
 * the service interface documentation is all a client should need, to fully
 * understand how to use the service.
 */
public interface IService extends cajo.sdk.IService {
   /**
    * A service interface can declare data/identification fields. The Java
    * Java language specification requires that any interface fields, object
    * or primitive, must be both static and final. As with methods, these
    * fields will be matched against final static <i>(or instance)</i> fields.
    * This feature can be very useful for looking up very specific types of
    * services, from others using very similar method signatures for example.
    */
   static final String ID = "Demonstration Version";
   /**
    * Simply an example function.
    * @param bar An arbitrary string argument
    * @return An arbitrary string
    * @throws Exception If the method invocation was rejected, for
    * application specific reasons
    * @throws java.rmi.RemoteException For network related failure or
    * configuration issues
    */
   String foo(String bar) throws Exception;
}
