package service;

import java.io.ObjectOutputStream;

/**
 * This example class creates a service, with a corresponding example
 * controller.
 */
public final class Service extends cajo.sdk.AbstractService implements IService {
   /**
    * The constructor makes this service object remotely accessible. It
    * registers itself in the local registry, and boadcasts its availability
    * through the <a href=http://weblogs.java.net/blog/cajo/archive/2007/09/simple_interjvm.html>
    * cajo federation</a>. All of the service's public methods, both instance
    * <i>and</i> static will then be remotely accessible.
    * @param name The name under which to bind the service in the local
    * registry, this is what clients will use to obtain a reference if
    * calling on the registry directly
    * @param log The log stream to write the service invocation events, if
    * this argument is null, the events will be written to System.out
    * <br><i><u>NB</u>:</i> it is <i>highly</i> recommended to build log
    * atop a BufferedOutputStream, and if a lot of invocations are being
    * logged, possibly a ZippedOutputStream as well. The stream could be from
    * a socket, if logging to another machine is desired. The same stream
    * can be used by multiple services.
    * @throws Exception if the service could not be successfully constructed,
    * normally for network configuration related issues
    */
   public Service(String name, ObjectOutputStream log) throws Exception {
      // the controller below is hard-coded, it could be read from a file
      super("controller.Controller", name, log);
      description = // describe functionality of this service
         "This is an example implementation of a cajo service, it is " +
         "for illustrative purposes.";
      addDescriptor( // provide service function description
         "foo", // function name
         "This whimsical function does nothing important.", // description
         new String[][] { // arguments
            {  "java.lang.String", // argument type
               "An incoming argument, it will be appended to the response" +
               "string returned by this function." // description
            },
         },
         new String[] { // method return description
            "java.lang.String", // return type
            "An indicator of successful completion, it will have the " +
            "contents of the incoming string appended to it."
         }, new String[][] { // exceptions thrown by this function
            { "java.lang.NullPointerException", // type
               "Thrown simply as an example, if the argument is null. " +
               "service methods are free to throw both checked and " +
               "unchecked exceptions."
            }, REMOTEEXCEPTION, // throws network related exceptions too
         }
      );
      startup(); // make service remotely accessible, technically optional
      // use the inherited cajo field to connect to any remote services needed
   }
   /**
    * Simply an example function.<br><i><u>NB</u>:</i> service methods can be
    * static, as well as instance. In this case, since we are implementing
    * to an interface, the method cannot be declared static.
    * @param bar An arbitrary string argument
    * @return An arbitrary string with the argument string appended
    * @throws NullPointerException If the provided argument is null, just
    * for illustrative purposes.
    */
   @Override
   public String foo(String bar) {
      if (bar == null) throw new NullPointerException("null arg");
      return "invocation completed for " + bar;
   }
   /** {@inheritDoc} */
   @Override
   public String toString() { return "ExampleService"; }
}
