package gnu.cajo.utils;

import gnu.cajo.invoke.*;
import java.awt.*;

/*
 * Abstract Graphical Proxy Item Base Class
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
 * A standard abstract base class for graphical proxy objects. A graphical
 * proxy provides a user interface to itself which can be displayed at the
 * receiving VM.  This is in contrast to extensions of BaseProxy, which are
 * primarily intended as machine-to-machine interfaces.
 * 
 * @version 1.0, 01-Nov-99 Initial release
 * @author John Catherino
 */
public abstract class PanelProxy extends Container implements Invoke, Runnable {
   /**
    * The processing thread of the proxy object, it will be started
    * automatically upon arrival at the host when the init method is invoked.
    * Subclasses should periodically check, or loop on its
    * {@link java.lang.Thread#isInterrupted isInterrupted} method, and
    * perform an orderly shutdown if it becomes true.
    */
   protected transient Thread thread;
   /**
    * A remote reference to the proxy itself, which it can send to its server,
    * or other remote VMs on which they can asynchronously call it.
    */
   protected transient Remote remoteThis;
   /**
    * The reference to the sending server, on which the proxy may
    * asynchronously callback.  It is set by the ProxyServer during the
    * {@link ProxyServer#bind bind} operation.
    */
   protected RemoteInvoke server;
   /**
    * The path & file name of the resource bundle in the proxy's jar file.
    * It will be used to localize any displayed strings to the language of
    * the proxy recipient, as necessary, and if supplied.  It is public since
    * its value is typically assigned by a builder program.
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
    * Nothing is performed in the constructor, as construction code is
    * generally performed by a builder application.
    */
   public PanelProxy() {}
   /**
    * The init method is invoked by the hosting VM on arrival.  It will first
    * load the localized strings based on the locale of the receiving host.
    * Next it will start the processing thread of the proxy.
    * @param remoteRef A reference to this proxy remoted within the context
    * of the receiving host, on its preferred ports and network interface.
    * @throws ClassCastException If the reference provided is not of type
    * {@link gnu.cajo.invoke.Remote Remote}.
    */
   public final void init(Invoke remoteRef) {
      if (remoteThis == null) {
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
      } else throw new IllegalStateException("Can't reinitialize proxy");
      thread = new Thread(this);
      thread.start();
   }
   /**
    * This function may be called reentrantly, so critical methods <i>must</i>
    * be synchronized. It will invoke the specified method with the
    * provided arguments, if any, using the Java reflection mechanism.
    * @param  method The method to invoke in this item.
    * @param args The arguments to provide to the method for its invocation.
    * @return The sychronous data, if any, resulting from the invocation.
    * @throws java.rmi.RemoteException For network communication related
    * reasons.
    * @throws IllegalArgumentException If the method argument is null.
    * @throws NoSuchMethodException If no matching method can be found.
    * @throws Exception If the method rejects the invocation, for any
    * application specific reason.
    * @throws ClassCastException If the first invocation is not with a
    * remote reference to the proxy's server item.
    */
   public final Object invoke(String method, Object args) throws Exception {
      if (server == null) {
         server = (RemoteInvoke)args;
         return null;
      }
      if (method == null)
         throw new IllegalArgumentException("Method cannot be null");
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
   /**
    * The processing thread of the proxy.  It will be started automatically
    * upon arrival of the proxy at the hosting VM. Subclasses should monitor
    * its {@link java.lang.Thread#isInterrupted isInterrupted} method, and
    * perform an orderly shutdown if it becomes true.
    */
   public abstract void run();
}
