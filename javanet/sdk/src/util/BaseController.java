package util;

import gnu.cajo.utils.extra.TransparentItemProxy;
import javax.swing.JFrame;
import javax.swing.JComponent;
import java.awt.event.WindowListener;

/*
 * A base controller class for a cajo grail server.
 * The cajo project: https://cajo.dev.java.net
 * For issues or suggestions mailto:cajo@dev.java.net
 * This class is released into the public domain.
 * Written by John Catherino 01-Dec-09
 */

/**
 * The controller is the smart go-between of a graphical view, and
 * its remote service object. It is expected to offload any possible server
 * workload related to the support of the client.
 * @author <a href=http://wiki.java.net/bin/view/People/JohnCatherino>
 * John Catherino</a>
 */
public abstract class BaseController implements java.io.Serializable {
   private static final long serialVersionUID = 0L;
   Object service; // reference to remote service
   /**
    * The constructor performs no functions, however Controller subclasses
    * need to have no-arg constructors, if they are to be used via the
    * ProxyLoader mechanism.
    */
   protected BaseController() {}
   /**
    * This medhod provides a simple unit test of the interaction between
    * the designated view, and a test object. It will instantiate its view
    * object, and place it in a JFrame.
    * @param service An object which will be representing the remote service
    * for the purposes of testing
    * @throws Exception if the view could not be created, typically due to
    * missing resource issues
    */
   protected final void test(Object service) throws Exception { // unit test
      this.service = service;
      JFrame frame = new JFrame("Unit Test");
      JComponent component = getView();
      if (component instanceof WindowListener)
         frame.addWindowListener((WindowListener)component);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.getContentPane().add(component);
      frame.pack();
      frame.setVisible(true);
   }
   /**
    * This method allows subclasses of BaseController to dynamically
    * cast a remote service reference into an interface of its choice.
    * @param proxyInterface The clas name of the interface to be
    * emulated, e.g. somepkg.ThisInterface
    * @return An object implementing the interface provided, yet passing
    * the method invocations directly on to the service object. <i>NB:</i>
    * this object is serialisable, and may be passed between JVMs.
    */
   @SuppressWarnings("unchecked") // sigh...
   protected final <T> T proxy(Class proxyInterface) {
      return (T)TransparentItemProxy.
         getItem(service, new Class[] { proxyInterface });
   }
   /**
    * This method is only called once, by the Client.
    * @param service The reference to the remote <a href=https://cajo.dev.java.net>
    * cajo</a> service object
    */
   public final void setService(Object service) {
      if (this.service != null)
         throw new RuntimeException("proxy already initialised");
      this.service = service;
   }
   /**
    * This method is called by the client, to get the view object associated
    * with this controller, to display in its own frame.
    * @return javax.swing.JComponent A graphical component which can then
    * be consolidated into any container for viewing. <b><i>NB:</i></b>
    * the method <i>may</i> return null, if the controller has no primary
    * view. Whilst permitted, doing this<i>will</i> mess up use by the applet
    * client, which <i>must</i> assume the controller has a default view.
    * @throws Exception if the view could not be created, typically due to
    * missing resource issues
    */
   public abstract JComponent getView() throws Exception;
   /**
    * This method is called when the widget is operating as an Applet, and
    * is loaded into the browser, <i>before</i> becoming visible.
    */
   public void init() {}
   /**
    * This method is called when the widget is operating as an Applet, and
    * becomes visible.
    */
   public void start() {}
   /**
    * This method is called when the widget is operating as an Applet, and
    * becomes invisible.
    */
   public void stop() {}
   /**
    * This method is called when the widget is operating as an Applet, and
    * disposed.
    */
   public void destroy() {}
}
