package controller;

/*
 * A sample interface for demonstration controller class of a cajo grail
 * server.
 * The cajo project: https://cajo.dev.java.net
 * For issues or suggestions mailto:cajo@dev.java.net
 * This interface is released into the public domain.
 * Written by John Catherino
 */

/**
 * This interface defines demonstration methods for an example controller.
 * It is normally a starting point, to embellish with application specific
 * function definitions of your own.
 */
public interface IController extends util.IBaseController {
   /**
    * Simply an example function.
    * @param arg An arbitrary string argument
    * @return An arbitrary string
    * @throws java.rmi.RemoteException For network related failure or
    * configuration issues in attempting to communicate with the service
    */
   String foo(String arg) throws java.rmi.RemoteException;
}
