package gnu.cajo.invoke;

import java.io.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.rmi.registry.*;
import java.rmi.MarshalledObject;
import java.util.zip.GZIPOutputStream;


/*
 * Graphical Proxy Loader Applet / Application
 *
 * For issues or suggestions mailto:cajo@dev.java.net
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, as published
 * by the Free Software Foundation; either version 2 of the license, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * To receive a copy of the GNU General Public License visit their website
 * at http://www.gnu.org or via snail mail at Free Software Foundation Inc.,
 * 59 Temple Place Suite 330, Boston MA 02111-1307 USA
 */

/**
 * This class is used to create a hosting VM to receive a graphical proxy
 * object, from a remote VM.  The client will require one outbound port, on
 * which to commuinicate with its proxy server and one on inbound, on which to
 * receive asynchronous callbacks from the server. It will also require one
 * short-term inbound port on which to receive the proxy class files. If the
 * client is behind a firewall, these will have to be open.
 *
 * @version 1.0, 01-Nov-99 Initial release
 * @author John Catherino
 */
public final class Client extends java.applet.Applet {
   private static final String TITLE = "CaJo Proxy Viewer";
   private static Object proxy;
   private static Frame frame;
   private static final class CFrame extends Frame implements WindowListener {
      public CFrame(String title) {
         super(title);
         addWindowListener(this);
      }
      public void update(Graphics g) { paint(g); }
      public void windowActivated(WindowEvent e)   {}
      public void windowDeactivated(WindowEvent e) {}
      public void windowOpened(WindowEvent e)      {}
      public void windowIconified(WindowEvent e)   {}
      public void windowDeiconified(WindowEvent e) {}
      public void windowClosing(WindowEvent e)     { dispose();      }
      public void windowClosed(WindowEvent e)      { System.exit(0); }
   }
   /**
    * The default constructor does nothing.  Initialization is done when
    * the applet is loaded into the browser, or when it is instantiated as
    * an application.
    */
   public Client() {}
   /**
    * The update method is short-circuited to directly execute the applet's
    * paint method, to reduce flicker from the default background repainting.
    * This will require the graphical proxy to repaint any opaque background
    * of its own, however.
    */
   public void update(Graphics g) { paint(g); }
   /**
    * This method provides the standard mechanism to identify this applet.
    * @return The identification string for this applet.
    */
   public String getAppletInfo() {
      return "CaJo Proxy Applet, Copyright \u00A9 1999 by John Catherino";
   }
   /**
    * When running as an applet, this method describes the optional client
    * parameters. There are five such parameters which can be specified:
    * <ul>
    * <li>The <code>proxyName</code> parameter is the name of the proxy server
    * item registered in the server's rmiregistry.  Unspecified it will be
    * "main".
    * <li>The <code>proxyPort</code> parameter is the outbound port number on
    * which to contact the proxy server.  Unspecified it will be 1099.  If the
    * client is operating behind a firewall, the must be a permitted outbound
    * port.
    * <li>The <code>clientHost</code> parameter is the external domain name or
    * IP address the server must use to callback its proxy.  It may need to
    * be specified if the client is operating behind a NAT router. Unspecified
    * it will be the client's default host address.
    * <li>The <code>clientPort</code> parameter is the external inbound port
    * number on which the server can contact its proxy. It may need to be
    * specified if the client is behind NAT, to map to the correct local port.
    * If a firewall is being used, it must be a permitted inbound port.
    * Unspecified, it will be the same as the local port value below.
    * <li>The <code>localPort</code> parameter is the internal inbound port
    * number on which the server can contact its proxy. It may need to be
    * specified if the client is behind NAT, to map to the correct remote port.
    * Unspecified, it will be anonymous.
    * </ul>
    * @return The parameter / information array.
    */
   public String[][] getParameterInfo() {
      return new String[][] {
         { "proxyName",  "String",  "Server's proxy's registry name" },
         { "proxyPort",  "Integer", "Server's proxy's port number"   },
         { "clientHost", "String",  "Client's external host name"    },
         { "clientPort", "Integer", "Client's external port number"  },
         { "localPort",  "Integer", "Client's internal port number"  },
      };
   }
   /**
    * When running as an applet, this method will connect back to its hosting
    * server and request the item from the server's rmiregistry. Next it will
    * invoke a getProxy(null) on the remote reference to request its proxy
    * item.  If the item returns the proxy in a MarshalledObject, it will be
    * extracted automatically. If the returned object is a proxy, the client
    * will invoke its init method, passing it a remote reference itself, and
    * to obtain its primary graphical representation, which will then be added
    * into the applet's panel.  The proxy can pass its remote reference back
    * to its hosting item, or to other remote items, on which they can
    * asynchronously call it back.
    */
    public void init() {
      try {
         String proxyName  = getParameter("proxyName");
         String proxyPort  = getParameter("proxyPort");
         String clientHost = getParameter("clientHost");
         String clientPort = getParameter("clientPort");
         String localPort  = getParameter("localPort");
         int pPort = proxyPort  != null ? Integer.parseInt(proxyPort)  : 1099;
         int cPort = clientPort != null ? Integer.parseInt(clientPort) : 0;
         int lPort = localPort  != null ? Integer.parseInt(localPort)  : 0;
         if (proxyName == null) proxyName = "main";
         Remote.config(null, lPort, clientHost, cPort);
         proxy = LocateRegistry.getRegistry(getCodeBase().getHost(), pPort);
         proxy = ((Registry)proxy).lookup(proxyName);
         proxy = ((Invoke)proxy).invoke("getProxy", null);
         if (proxy instanceof MarshalledObject)
            proxy = ((MarshalledObject)proxy).get();
         if (!(proxy instanceof RemoteInvoke)) try {
            proxy = Remote.invoke(proxy, "init", new Remote(proxy));
         } catch(Exception x) {}
         if (proxy instanceof Component) {
            setLayout(new BorderLayout());
            add((Component)proxy);
            validate();
         }
      } catch (Exception x) { x.printStackTrace(System.err); }
   }
   /**
    * This method is used by items to create a frame containing the AWT
    * component.
    * @param component The AWT/Swing component, typically returned from a
    * proxy initialization, to be framed.
    * @return the AWT Frame or Swing JFrame containing the component, already
    * visible.
    */
   public static Frame frame(Component component) {
      if (component instanceof JComponent) {
         JFrame frame = new JFrame(TITLE);
         frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
         frame.setVisible(true);
         frame.getContentPane().add((JComponent)component);
         frame.pack();
         return frame;
      } else {
         CFrame frame = new CFrame(TITLE);
         frame.setVisible(true);
         frame.add((Component)component);
         frame.pack();
         return frame;
      }
   }
   /**
    * The application creates a graphical proxy hosting VM.
    * With the URL argument provided, it will use the static
    * {@link Remote#getItem getItem} method of the {@link Remote Remote} class
    * to contact the server. It will then invoke a null-argument getProxy on
    * the resulting reference to request the primary proxy object of the item.<br><br>
    * <i>Note:</i> When running as an application (except via JNLP) it will
    * load a NoSecurityManager, therefore, if no external SecurityManager is
    * specified in the startup command line options; the arriving proxies will
    * have <u><b>full permissions</b></u> on this machine.<br><br>
    * The startup requires one, and can take up to four additional optional
    * configuration parameters, in this order:<ul>
    * <li> args[0] The required URL where to get the graphical proxy item:<br>
    * file:// http:// ftp:// ..., //host:port/name (rmiregistry), /path/name
    * (serialized), or path/name (class).
    * <li> args[1] The optional external client port number, if using NAT.
    * <li> args[2] The optional external client host name,   if using NAT.
    * <li> args[3] The optional internal client port number, if using NAT.
    * <li> args[4] The optional internal client host name,   if multi home/NIC.</ul>
    */
   public static void main(String args[]) {
      try {
         try {
            System.setSecurityManager(new NoSecurityManager());
            System.setProperty("java.rmi.server.disableHttp", "true");
         } catch(SecurityException x) {}
         int clientPort    = args.length > 1 ? Integer.parseInt(args[1]) : 0;
         String clientHost = args.length > 2 ? args[2] : null;
         int localPort     = args.length > 3 ? Integer.parseInt(args[3]) : 0;
         String localHost  = args.length > 4 ? args[4] : null;
         Remote.config(localHost, localPort, clientHost, clientPort);
         proxy = Remote.getItem(args[0]);
         proxy = ((Invoke)proxy).invoke("getProxy", null);
         if (proxy instanceof MarshalledObject)
            proxy = ((MarshalledObject)proxy).get();
         if (!(proxy instanceof RemoteInvoke)) try {
            proxy = Remote.invoke(proxy, "init", new Remote(proxy));
         } catch(Exception x) {}
         if (proxy instanceof Component) frame = frame((Component)proxy);
      } catch (Exception x) { x.printStackTrace(System.err); }
   }
}
