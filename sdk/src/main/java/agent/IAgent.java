package agent;

/**
 * This interface defines demonstration methods for the example agent. It is
 * normally a starting point, to embellish with application specific function
 * definitions of your own. This interface, or a subset of it, is invoked
 * remotely by the sending service.
 */
public interface IAgent extends cajo.sdk.IAgent {
   /**
    * Simply an example function. Normally a single remote service invocation
    * could result in multiple local service invocations, or manipulate large
    * local data objects.
    * @param baz An arbitrary string argument
    * @return An arbitrary string
    * @throws Exception If the remote method invocation was rejected by
    * the local service, for application specific reasons
    * @throws java.rmi.RemoteException For network related failures
    */
   String bar(String baz) throws Exception;
}
