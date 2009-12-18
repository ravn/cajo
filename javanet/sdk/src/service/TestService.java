package service;

/*
 * An example service object for a cajo grail server.
 * The cajo project: https://cajo.dev.java.net
 * For issues or suggestions mailto:cajo@dev.java.net
 * This class is released into the public domain.
 * Written by John Catherino 01-Dec-09
 */

/**
 * This class creates a service, with a corresponding client GUI. It is
 * normally completely unaware of the <a href=https://cajo.dev.java.net>cajo</a>
 * grail framework. It uses the static Cajo member of its inherited
 * BaseService class, on which to find, and interact with other services.<p>
 * <i>NB:</i> all public methods, <i>either static or instance,</i> will be made
 * publically invocable, so be careful: <i>e.g. a <tt>public static void
 * main</tt> function is not recommended.</i>
 * @author <a href=http://wiki.java.net/bin/view/People/JohnCatherino>
 * John Catherino</a>
 */
public class TestService extends util.BaseService {
   /**
    * The constructor registers the service object in the local registry,
    * and boadcasts its availability through the <a href=http://weblogs.java.net/blog/cajo/archive/2007/09/simple_interjvm.html>
    * cajo federation</a>.
    * @param name The name under which to bind the service in the local
    * registry
    * @throws Exception should the service startup fail, usually for network
    * configuration related issues
    */
   public TestService(String name) throws Exception {
      super("controller.TestController", name);
      description =
         "This is an example implementation of a cajo service, it is " +
         "for illustrative purposes.";
      addDescriptor("fooBar",
         "This whimsical function does nothing",
         new String[][] { // arguments
            NOARGS,
         }, new String[][] { // returns
            NORETURNS
         }, new String[][] { // exceptions
            RMIEXCEPTION,
         }
      );
   }
   // furnish public (pojo) methods/functions you'd like to share...
   /**
    * Simply an example function, it performs nothing.
    */
   public String fooBar() {
      System.out.println("fooBar invoked");
      return "fooBar invocation completed.";
   }
}
