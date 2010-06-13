package service;

/*
 * A sample interface for a demonstration service class of a cajo grail
 * server.
 * The cajo project: https://cajo.dev.java.net
 * For issues or suggestions mailto:cajo@dev.java.net
 * This interface is released into the public domain.
 * Written by John Catherino
 */

/**
 * This interface defines the demonstration methods for the example service.
 * It is normally a starting point, to embellish with application specific
 * function definitions of your own. A server can furnish as many services
 * as it wishes.
 */
public interface IService extends util.IBaseService {
   /**
    * Simply an example function.
    * @param arg An arbitrary string argument
    * @return An arbitrary string
    * @throws RemoteException For network related failure or configuration
    * issues
    */
   String foo(String arg) throws java.rmi.RemoteException;
}
