package controller;

import javax.swing.JComponent;

/*
 * An example controller object for a cajo grail server.
 * The cajo project: https://cajo.dev.java.net
 * For issues or suggestions mailto:cajo@dev.java.net
 * This class is released into the public domain.
 * Written by John Catherino 01-Dec-09
 */

/**
 * This class creates a controller, for a corresponding client GUI. It is
 * normally completely unaware of the <a href=https://cajo.dev.java.net>cajo</a>
 * grail framework. Its role is to <i>'map'</i> its View object onto its
 * service object. <i>NB:</i> the more work that can be done here at the
 * controller, the less busy, and ultimately more scalable, the server will
 * be.
 * @author <a href=http://wiki.java.net/bin/view/People/JohnCatherino>
 * John Catherino</a>
 */
public class TestController extends util.BaseController {
   // This defines the controller's interface to the service item. Only
   // the method signatures will be matched, therefore, the name and the
   // package of the controller interface do not matter.
   private interface TestInterface {
      Object foo(); // this method does nothing, rather it is just an example
   }
   // This is this interface on which the controller will communicate with
   // its service object.
   private transient TestInterface serviceProxy;
   /**
    * The constructor performs no functionality.
    */
   public TestController() {}
   /**
    * This method is called, normally once, by the  util.Client object, on
    * arrival at the host JVM.
    * @return A graphical user interface, supported by the controller, which
    * maps its operations onto its service object.
    * @throws Exception If the view could not be created, normally for
    * missing resource issues.
    */
   public JComponent getView() throws Exception {
      if (serviceProxy == null) serviceProxy = proxy(TestInterface.class);
      System.out.println(serviceProxy.foo().toString());
      return new view.TestView();
   }
   /**
    * This method provides a simple unit test of the GUI functionality,
    * it is intended for development and debug purposes only.
    * @param args No arguments are used in the unit test.
    * @throws Exception should the test startup fail, usually for missing
    * resource
    */
   public static void main(String args[]) throws Exception {
      new TestController().test(null);
   }
}
