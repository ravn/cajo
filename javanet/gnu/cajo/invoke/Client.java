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
 * This class is used to create a hosting VM to receive graphical proxy
 * objects from a remote VMs.  The client will require one outbound port, on
 * which to commuinicate with its proxy server. It will also require two
 * inbound ports, the first on which to receive the proxy class files, and the
 * second to receive asynchronous callbacks from the server. If the client is
 * behind a firewall, these will have to be made accessible.<p> Its purpose is
 * to demonstrate the overall functionality of the package, and the invoke
 * package paradigm, in the form of a hopefully useful and potentially standard
 * client.
 *
 * @version 1.0, 01-Nov-99 Initial release
 * @author John Catherino
 */
public final class Client extends java.applet.Applet implements Invoke {
   private static final String TITLE = "CaJo Proxy Viewer";
   private static Client client;
   private static Registry registry;
   private static Object proxy;
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
      public void windowClosing(WindowEvent e)     { dispose(); }
      public void windowClosed(WindowEvent e)      {}
   }
   /**
    * The default constructor does nothing.  Initialization is done when
    * the applet is loaded into the browser, or when it is instantiated as
    * an application.
    */
   public Client() {}
   /**
    * The update method is short-circuited to directly execute the paint
    * method, to reduce flicker from the default background repainting.  It
    * will require the applet to repaint any opaque background on its own.
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
    * When running as an applet, this method is describes the optional client
    * applet perameters. There are six such parameters which can be specified:
    * <ul>
    * <li>The <code>proxyName</code> parameter is the name of the proxy server
    * registered in the server's rmiregistry.  Unspecified it will be "main".
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
    * <li>The <code>localHost</code> parameter is the internal domain name or
    * IP address the proxy will use when calling back its server.  It may need
    * to be specified if the client has multiple network interfaces, or is
    * operating behind NAT. Unspecified it will be the client's default local
    * host address.
    * <li>The <code>localPort</code> parameter is the internal inbound port
    * number on which the server will callback its proxy. It may need to be
    * specified if the client is behind NAT, to map to the correct remote port.
    * If a firewall is being used, it must be a permitted inbound port.
    * Unspecified, it will be anonymous.
    * </ul>
    * @return The two dimensional parameter and information array.
    */
   public String[][] getParameterInfo() {
      return new String[][] {
         { "proxyName",  "String",  "Server's proxy's registry name" },
         { "proxyPort",  "Integer", "Server's proxy's port number"   },
         { "clientHost", "String",  "Client's external host name"    },
         { "clientPort", "Integer", "Client's external port number"  },
         { "localHost",  "String",  "Client's internal host name"    },
         { "localPort",  "Integer", "Client's internal port number"  },
      };
   }
   /**
    * When running as an applet, this method will connect back to its hosting
    * server and request its proxy server from its rmiregistry. Next it will
    * invoke a getProxy(null) on the remote reference to request its proxy
    * item.  If the server returns the proxy as a MarshalledObject, it will be
    * extracted automatically. If the proxy supports the Invoke interface, the
    * client will invoke an init on the returned proxy, with a remote reference
    * to the proxy as the sole argument, to obtain a graphical representation,
    * which will then be added into the applet's panel.  The proxy can pass
    * this remote reference back to hosting server, or to other VMs, on which
    * they can asynchronously call back the proxy.
    */
    public void init() {
      try {
         String proxyName  = getParameter("proxyName");
         String proxyPort  = getParameter("proxyPort");
         String clientHost = getParameter("clientHost");
         String clientPort = getParameter("clientPort");
         String localHost  = getParameter("localHost");
         String localPort  = getParameter("localPort");
         int pPort = proxyPort  != null ? Integer.parseInt(proxyPort)  : 1099;
         int cPort = clientPort != null ? Integer.parseInt(clientPort) : 0;
         int lPort = localPort  != null ? Integer.parseInt(localPort)  : 0;
         if (localHost == null) localHost = clientHost;
         Remote.config(localHost, lPort, clientHost, cPort);
         if (proxyName == null) proxyName = "main";
         Object proxy =
            LocateRegistry.getRegistry(getCodeBase().getHost(), pPort);
         proxy = ((Registry)proxy).lookup(proxyName);
         proxy = ((Invoke)proxy).invoke("getProxy", null);
         if (proxy instanceof MarshalledObject)
            proxy = ((MarshalledObject)proxy).get();
         if (proxy instanceof Invoke && !(proxy instanceof RemoteInvoke))
            proxy = ((Invoke)proxy).invoke("init", new Remote(proxy));
         if (proxy instanceof Component) {
            setLayout(new BorderLayout());
            add((Component)proxy);
            validate();
         }
      } catch (Exception x) { x.printStackTrace(System.err); }
   }
   /**
    * A utility method to load a proxy items into this VM.  If the invocation
    * data is a remote item reference, getProxy(null) will be invoked to
    * request its proxy item. If the proxy is encapsulated in a
    * MarshalledObject, it will be extracted automatically. If the proxy
    * implements the Invoke interface, an init will be called on it with a
    * remote reference to itself as the argument.  If the proxy is graphical
    * in nature, i.e. returns a java.awt.Component or a javax.swing.JComponent
    * it will be automatically placed in a Frame/JFrame, and made visible on
    * the client.
    * @param method Required by the interface, but ignored in this
    * implementation.
    * @param args A remote reference to a proxy server, or a proxy object,
    * potentially encased in a MarshalledObject.
    * @return A remote reference to the proxy object by which the sending
    * VM may asynchronously communicate with its proxy. If the proxy sent
    * does not implement the invoke interface, it will be null.
    * @throws Exception If the proxy initialization fails for application
    * specific reasons.
    */    
   public Object invoke(String method, Object args) throws Exception {
      Invoke proxy = null;
      if (args instanceof RemoteInvoke)
         args = ((Invoke)args).invoke("getProxy", null);
      if (args instanceof MarshalledObject)
         args = ((MarshalledObject)args).get();
      if (args instanceof Invoke && !(args instanceof RemoteInvoke)) {
         proxy = new Remote(args);
         args = ((Invoke)args).invoke("init", proxy);
      }
      if (args instanceof JComponent) {
         JFrame frame = new JFrame(TITLE);
         frame.setDefaultCloseOperation(frame.DISPOSE_ON_CLOSE);
         frame.setVisible(true);
         frame.getContentPane().add((JComponent)args);
         frame.pack();
      } else if (args instanceof Component) {
         CFrame frame = new CFrame(TITLE);
         frame.setVisible(true);
         frame.add((Component)args);
         frame.pack();
      }
      return proxy;
   }
   /**
    * The application creates a remotely accessible proxy hosting VM, bound
    * in its own rmiregistry under the name "main". It will use the getProxy
    * method of the Remote class to load the item. <i>Note:</i> by default,
    * it will load a NoSecurityManager if no external SecurityManager is
    * specified.
    * <p>The startup can take up to five optional configuration parameters,
    * in order:<p>
    * @param args[0] The optional external client host name, if using NAT.
    * @param args[1] The optional external client port number, if using NAT.
    * @param args[2] The optional internal client host name, if multi home/NIC.
    * @param args[3] The optional internal client port number, if using NAT.
    * @param args[4] The optional URL where to get a default proxy item:
    * file:// http:// ftp:// ..., //host:port/name (rmiregistry), /path/name
    * (serialized), or path/name (class).
    */
   public static void main(String args[]) {
      try {
         String clientHost = args.length > 0 ? args[0] : null;
         int clientPort    = args.length > 1 ? Integer.parseInt(args[1]) : 0;
         String localHost  = args.length > 2 ? args[2] : null;
         int localPort     = args.length > 3 ? Integer.parseInt(args[3]) : 0;
         Remote.config(localHost, localPort, clientHost, clientPort);
         try {
            System.setSecurityManager(new NoSecurityManager());
            System.setProperty("java.rmi.server.disableHttp", "true");
         } catch(SecurityException x) {}
         client = new Client();
         registry = LocateRegistry.
            createRegistry(Remote.getServerPort(), Remote.rcsf, Remote.rssf);
         registry.bind("main", new Remote(client));
         System.out.println("Client bound under name \"client\"");
         System.out.print("locally  operating  on host ");
         System.out.print(Remote.getServerHost());
         System.out.print(" on port ");
         System.out.println(Remote.getServerPort());
         System.out.print("remotely accessible on host ");
         System.out.print(Remote.getClientHost());
         System.out.print(" on port ");
         System.out.println(Remote.getClientPort());
         if (args.length > 4) {
            proxy = Remote.getItem(args[4]);
            proxy = ((Invoke)proxy).invoke("getProxy", null);
            if (proxy instanceof MarshalledObject)
               proxy = ((MarshalledObject)proxy).get();
            if (proxy instanceof Invoke && !(proxy instanceof RemoteInvoke))
               proxy = ((Invoke)proxy).invoke("init", new Remote(proxy));
            if (proxy instanceof JComponent) {
               JFrame frame = new JFrame(TITLE);
               frame.setDefaultCloseOperation(frame.DISPOSE_ON_CLOSE);
               frame.setVisible(true);
               frame.getContentPane().add((JComponent)proxy);
               frame.pack();
            } else if (proxy instanceof Component) {
               CFrame frame = new CFrame(TITLE);
               frame.setVisible(true);
               frame.add((Component)proxy);
               frame.pack();
            }
         }
      } catch (Exception x) { x.printStackTrace(System.err); }
   }
}
