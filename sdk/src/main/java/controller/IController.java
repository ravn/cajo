package controller;

/**
 * This interface defines demonstration methods for an example controller.
 * It is normally a starting point, to embellish with application specific
 * function definitions of your own. This interface, or a subset of it, is
 * invoked locally by the receiving service.
 * <br>In addition to the effort for defining a good quality API, similar
 * effort should be put into the javadoc. Develop it from the viewpoint that
 * the controller interface documentation is all a client should need, to
 * fully understand how to use the service via its controller.
 */
public interface IController extends cajo.sdk.IController {
   /**
    * Simply an example function. Normally here is where pre and post
    * invocation details would be managed; such as caching, retry,
    * presentation, etc.
    * <br><i><u>NB</u>:</i> This client-facing interface need not bear
    * <i>any</i> resemblance to the service object it represents, as it
    * can provide composite functionality for its client.
    * @param bar An arbitrary string argument
    * @return An arbitrary string
    * @throws NullPointerException if the provided argument is null, simply
    * for illustration
    * @throws Exception If the local method invocation involved rejection by
    * either the controller, or the server, for application specific reasons
    * @throws java.rmi.RemoteException For network related failure in
    * attempting to communicate with the service
    */
   String baz(String bar) throws Exception;
}
