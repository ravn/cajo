package gnu.cajo.utils;

import java.awt.*;
import gnu.cajo.invoke.*;
import java.io.Serializable;

/*
 * Abstract Proxy Item Base Class
 *
 * For issues or suggestions mailto:cajo@dev.java.net
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, at version 2.1 of the license, or any
 * later version.  The license differs from the GNU General Public License
 * (GPL) to allow this library to be used in proprietary applications. The
 * standard GPL would forbid this.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * To receive a copy of the GNU General Public License visit their website at
 * http://www.gnu.org or via snail mail at Free Software Foundation Inc.,
 * 59 Temple Place Suite 330, Boston MA 02111-1307 USA
 */

/**
 * A standard abstract base class for proxy objects.  Proxies are remote
 * object interfaces to server items.  They are intended to offload routine
 * processing.  They differ from server items in that they are sent to remote
 * VMs to operate, and are often not even instantiated in the runtime of the
 * server's VM.
 * 
 * @version 1.0, 01-Nov-99 Initial release
 * @author John Catherino
 */
public abstract class BaseProxy implements Invoke {
   /**
    * A remote reference to the proxy itself, which it can send to its server,
    * or other remote VMs on which they can asynchronously callback.
    */
   protected transient Remote remoteThis;
   /**
    * The reference to the sending server, on which the proxy may
    * asynchronously callback.  It is set by the {@link ItemServer ItemServer}
    * during the bind operation.
    */
   protected RemoteInvoke item;
   /**
    * A reference to the proxy's processing code.  If non-null, it will be
    * started automatically upon arrival at the host.  Its thread can be
    * accessed through the thread member.
    */
   protected MainThread runnable;
   /**
    * The processing thread of the proxy object, it will be started
    * automatically upon arrival at the host when the init method is invoked.
    */
   public transient Thread thread;
   /**
    * A reference to the proxy's graphical user interface, if any.  It will be
    * returned to the client as a result of its initialization invocation.
    */
   public Container container;
   /**
    * The path/filename of the resource bundle in the proxy's jar file.
    * It will be used to localize any displayed strings to the language of
    * the proxy recipient, as necessary, and when supplied.  It is public
    * since its value is typically assigned by a builder program.
    */
   public String bundle;
   /**
    * The collection of strings to be displayed at the host VM.  On
    * instantiation at the host, the array will be loaded with localized
    * strings from the most appropriate resource bundle for the locale of
    * the receiving VM, as necessary.  It is public since its value is
    * typically assigned by a builder program.
    */
   public String strings[];
   /**
    * The main processing thread of this Item.  An item can be either entirely,
    * event driven, i.e. executing only when its methods are being invoked,
    * or can also have a thread of its own. If non-null, it will be started
    * upon its arrival at the host via the client's proxy inialization
    * invocation.<br><br>
    * This is an an inner class of BaseProxy, to allow its implementations
    * access to the item's private and protected members and methods.
    * This is critical because <b>all</b> public methods of BaseProxy can be
    * invoked by remote objects, just like with local objects.
    */
   public abstract class MainThread implements Runnable, Serializable {
      /**
       * Nothing is performed in the constructor. Construction and
       * configuration are generally performed by a builder application.
       */
      public MainThread() {}
      /**
       * The run method is exectued by the thread created for the BaseProxy
       * at its initialization at the client, and runs until it returns.
       */
      public abstract void run();
   }
   /**
    * A standard base class for graphical proxy objects. A graphical proxy
    * provides a user interface to itself which can be displayed at the
    * receiving VM. It is implemented as an inner class of BaseProxy, to allow
    * its subclass implementations access to its outer item's private and
    * protected members and methods. This is critical because <b>all</b> public
    * methods of BaseProxy can be invoked by remote objects, just like with
    * local objects.
    * 
    * @version 1.0, 01-Nov-99 Initial release
    * @author John Catherino
    */
   public class Panel extends Container {
      /**
       * Nothing is performed in the constructor. Construction and
       * configuration are generally performed by a builder application.
       */
      public Panel() {}
      /**
       * The update method is overridden to directly invoke the paint method.
       * It makes drawing faster, and cleaner, but also means that the panel
       * background will not be cleared on a size change.
       */
      public final void update(Graphics g) { paint(g); }
      /**
       * The paint method is overridden to directly paint its components.
       * It makes drawing faster, and cleaner, but also means that the panel
       * has no default appearance.
       */
      public final void paint(Graphics  g) { paintComponents(g); }
      /**
       * This method simply returns the actual size of the component.  This
       * method returns the result of the getSize() method meaning that
       * subclasses should set the panel size <i>before</i> sending it to the
       * host.
       */
      public final Dimension getPreferredSize() { return getSize(); }
   }
   /**
    * Nothing is performed in the constructor. Construction and configuration
    * of the proxy are generally performed by a builder application.
    */
   public BaseProxy() {}
   /**
    * This function is called by the {@link ItemServer ItemServer} during its
    * bind operation.
    * @param  server A remote reference to the server, on which the proxy may
    * asynchronously call back to it.
    */
   public void setItem(Invoke item) {
      if (this.item == null) this.item = (RemoteInvoke)item;
   }
   /**
    * This function is called by the hosting client on its arrival.  The client
    * will provide a reference to the proxy, remoted in the context of the
    * client's VM.  This value will be saved in the remoteThis member, and
    * can be provided to remote objects on which they can contact the proxy.
    * If the proxy has a localized string bundle, the localized strings most
    * closely matching the local of the receiving host will be loaded.
    * @param  remoteRef A reference to the proxy, remoted in the context of the
    * client's VM.
    * @return The proxy's graphical user interface, if it has one, otherwise
    * null.
    */
   public Container init(Invoke remoteRef) {
      if (remoteThis == null && remoteRef instanceof Remote) {
         remoteThis = (Remote)remoteRef;
         if (bundle != null) {
            java.util.ResourceBundle rb =
               java.util.ResourceBundle.getBundle(bundle);
            for (int i = 0; i < strings.length; i++) {
               try { strings[i] = rb.getString(strings[i]); }
               catch (java.util.MissingResourceException e) {
                  strings[i] = e.getLocalizedMessage();
               }
            }
         }
         thread = new Thread(runnable);
         thread.start();
      }
      return container;
   }
   /**
    * The reflection based method router.  This method finds the public method
    * matching the name, and using the arguments provided, if any, and returns
    * the result.
    * @param  method The method to invoke on this object.
    * @param args The arguments to provide to the method for its invocation.
    * It can be a single object, an array of objects, or even null.
    * @return The sychronous data, if any, resulting from the invocation.
    * @throws java.rmi.RemoteException For network communication related
    * reasons.
    * @throws IllegalArgumentException If the method argument is null.
    * @throws NoSuchMethodException If no matching method can be found.
    * @throws Exception If the method rejects the request, for any
    * application specific reason.
    */
   public final Object invoke(String method, Object args) throws Exception {
      if (method == null)
         throw new IllegalArgumentException("Method argument cannot be null");
      Class types[] = null;
      if (args instanceof Object[]) {
         types = new Class[((Object[])args).length];
         for (int i = 0; i < types.length; i++)
            types[i] = ((Object[])args)[i] instanceof Invoke ?
               Invoke.class : ((Object[])args)[i].getClass();
      } else if (args != null) {
         types = new Class[] {
            args instanceof Invoke ? Invoke.class : args.getClass() };
         args = new Object[] { args };
      }
      return getClass().getMethod(method, types).invoke(this, (Object[])args);
   }
}