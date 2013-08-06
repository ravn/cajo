package cajo.sdk;

import javax.swing.JApplet;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import gnu.cajo.invoke.Remote;

/* Copyright 2010 John Catherino
 * The cajo project: http://cajo.java.net
 *
 * Licensed under the Apache Licence, Version 2.0 (the "Licence"); you may
 * not use this file except in compliance with the licence. You may obtain a
 * copy of the licence at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the licence is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * The client is used to display a graphical view from a
 * <a href=https://cajo.dev.java.net>cajo</a> {@link AbstractService service}
 * JVM as an Applet, via WebStart, in a JFrame, or as a stand-alone
 * application.<p>
 * To use the client class as an application to connect to a service directly,
 * perform the following:<br>
 * <tt><pre> java -cp client.jar:grail.jar cajo.sdk.Client //myHost:1198/main</tt></pre>
 * <i>(switch : to ; for windows users)</i><br><br>
 * <i><u>NB</u>:</i> This is a <b>special mode</b>. When clients operate this
 * way, the controllers will load with the <u>same permissions</u> as the
 * user who launched the client, rather than in the Applet/WebStart sandbox.
 * This can be very useful for special <i>"priviliged"</i> controllers, which
 * require more local access permissions. If you wish to run the client
 * locally, but keep controllers in a security sandbox, change the startup
 * invocation to the following:
 * <tt><pre> java -cp client.jar:grail.jar -Djava.security.manager
 * -Djava.security.policy=client.policy cajo.sdk.Client //myHost:1198/name</tt></pre>
 * *A functional example client.policy file is provided with the SDK.
 * See the build.xml <tt>startclient</tt> target for a launch example.
 * @see AbstractService
 * @author John Catherino
 */
public final class Client extends JApplet {
   private static final long serialVersionUID = 1L;
   private Object object;
   private void show(final String title) {
      SwingUtilities.invokeLater(new Runnable() {
         @Override
         @SuppressWarnings("synthetic-access") // we know, but it's necessary
         public void run() {
            JFrame frame = new JFrame(title);
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.getContentPane().add((JComponent)object);
            frame.pack();
            frame.addWindowListener(new WindowListener() {
               @Override
               @SuppressWarnings("unused") // ignoring actual window event
               public void windowOpened(WindowEvent we) {
                  try { Remote.invoke(object, "init", null); }
                  catch(Exception x) { x.printStackTrace(); }
               }
               @Override
               @SuppressWarnings("unused") // ignoring actual window event
               public void windowDeiconified(WindowEvent we) {
                  try { Remote.invoke(object, "start", null); }
                  catch(Exception x) { x.printStackTrace(); }
               }
               @Override
               @SuppressWarnings("unused") // ignoring actual window event
               public void windowIconified(WindowEvent we) {
                  try { Remote.invoke(object, "stop", null); }
                  catch(Exception x) { x.printStackTrace(); }
               }
               @Override
               @SuppressWarnings("unused") // ignoring actual window event
               public void windowClosed(WindowEvent we) {
                  try { Remote.invoke(object, "destroy", null); }
                  catch(Exception x) { x.printStackTrace(); }
               }
               @Override
               @SuppressWarnings("unused") // ignoring actual window event
               public void windowActivated(WindowEvent we)   {}
               @Override
               @SuppressWarnings("unused") // ignoring actual window event
               public void windowDeactivated(WindowEvent we) {}
               @Override
               @SuppressWarnings("unused") // ignoring actual window event
               public void windowClosing(WindowEvent we)     {}
            });
            frame.setVisible(true);
         }
      });
   }
   /**
    * The default constructor does nothing, it is used when the client is
    * operating as an Applet.
    */
   public Client() {}
   /**
    * This general purpose constructor can be used to display any JComponent
    * a frame. This could, for example, be used by an {@link AbstractAgent
    * agent}, to pop up a view in its receiver. However, if this type of
    * behaviour is not clearly documented, it would be considered rude.
    * @param view The component to display in the generated frame
    * @param title The caption to go into the frame title bar
    */
   public Client(JComponent view, String title) {
      object = view;
      show(title);
   }
   /**
    * This method provides the standard mechanism to identify this client,
    * when it is running as an Applet.
    * @return The identification string for this client.
    */
   @Override
   public String getAppletInfo() {
      return "cajo service view Applet, by John Catherino";
   }
   /**
    * This method describes the optional client parameters. There are two
    * such parameters which can be specified: <i>(normally these are set
    * automatically by the CodebaseServer class in gnu.cajo.utils)</i><p><ul>
    * <li>The <code>proxyName</code> parameter is the name of the service
    * object registered in the server's registry.
    * <li>The <code>proxyPort</code> parameter is the outbound port number on
    * which to contact the server.
    * <li>The <code>clientHost</code> parameter is the external domain name or
    * IP address the service must use to callback its controller.  It would
    * need to be specified if the client is operating behind a NAT router.
    * Unspecified it will be the client's default host address.
    * <li>The <code>clientPort</code> parameter is the external inbound port
    * number on which the server can contact its controller. It may need to be
    * specified if the client is behind NAT, to map to the correct local port.
    * If a firewall is being used, it must be a permitted inbound port.
    * Unspecified, it will be the same as the local port value below.
    * <li>The <code>localPort</code> parameter is the internal inbound port
    * number on which the server can contact its controller. It may need to be
    * specified if the client is behind NAT, to map to the correct remote port.
    * Unspecified, it will be anonymous.
    * </ul>
    * @return The parameter / information array.
    */
   @Override
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
    * This method is called when the client is created. It connects back to
    * its hosting server and requests the service designated in the URL.
    * Next it will invoke a getController() on the remote service reference
    * to request its controller. The returned local controller object will
    * have its getView() method invoked to obtain its graphical JComponent,
    * which will then be added into the JApplet via the Swing event dispatch
    * thread. The method invocation is then passed on to the view.
    */
   @Override
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
         Remote.config("0.0.0.0", lPort, clientHost, cPort);
         object = LocateRegistry.getRegistry(getCodeBase().getHost(), pPort);
         object = ((Registry)object).lookup(proxyName);
         object = Remote.invoke(object, "getController", null);
         object = Remote.invoke(object, "getView", null);
         SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            @SuppressWarnings("synthetic-access")
            public void run() {
               getContentPane().add((JComponent)object);
               try { Remote.invoke(object, "init", null); }
               catch(Exception x) { showStatus(x.getLocalizedMessage()); }
               validate();
            }
         });
      } catch(Exception x) { showStatus(x.getLocalizedMessage()); }
   }
   /**
    * This method is called each time the Client as an applet becomes visible,
    * the method invocation is then passed on to the view.
    */
   @Override
   public void start() {
      try { Remote.invoke(object, "start", null); }
      catch(Exception x) { showStatus(x.getLocalizedMessage()); }
   }
   /**
    * This method is called each time the Client as an applet becomes
    * invisible, the method invocation is then passed on to the view.
    */
   @Override
   public void stop() {
      try { Remote.invoke(object, "stop", null); }
      catch(Exception x) { showStatus(x.getLocalizedMessage()); }
   }
   /**
    * This method is called when the Client as an applet is disposed, the
    * method invocation is then passed on to the view.
    */
   @Override
   public void destroy() {
      try { Remote.invoke(object, "destroy", null); }
      catch(Exception x) { showStatus(x.getLocalizedMessage()); }
   }
   /**
    * The application creates a graphical view controller hosting VM. With the
    * URL argument provided. First it will call <tt>Remote.config</tt> to
    * prepare the JVM for network operation. Then it will call
    * <tt>Remote.getItem</tt> to contact the service. Next it will invoke
    * getController() on the remote service reference to request its local
    * controller. The returned object will have its getView() method invoked
    * to obtain its graphical JComponent view, which will then be added into
    * a JFrame via the Swing event dispatch thread. Finally an init() method
    * invocation is passed on to the view.<p>
    * When using the Client from the command line, it is possible to
    * optionally set the Client frame title explicitly. To do this, simply
    * type:<br><br><tt>
    * java -cp grail.jar;client.jar -Dcajo.sdk.Client.title="My Frame Title"
    * cajo.sdk.Client //myHost:1198/test</tt><p>
    * <i><u>NB</u>:</i> When running Client as an application (<i><u>except</u>
    * via WebStart/Applet</i>) it will run at the <b>same</b> level of
    * priviliges as the process that started it. This is useful for example,
    * when a special, administrative process needs to be run locally. It can
    * also be used when a machine wishes to aid a service, offering up its
    * computing resources.
    * @param args The startup requires up to five optional configuration
    * parameters, in this order:<ul>
    * <li><tt>args[0] - </tt>The URL where to get the graphical view
    * supporting controller service: i.e. //host:port/name
    * <li><tt>args[1] - </tt>The optional external client port number,
    * if using NAT.
    * <li><tt>args[2] - </tt>The optional external client host name,
    * if using NAT.
    * <li><tt>args[3] - </tt>The optional internal client port number,
    * if using NAT.
    * <li><tt>args[4] - </tt>The optional internal client host name,
    * if multi home/NIC.</ul>
    * @throws Exception and subclasses, for a multitude of reasons resulting
    * from connecting to the server, its service, requesting the controller,
    * and instantiating its view
    */
   @SuppressWarnings("unused") // looks like Client is unused, but it's not
   public static void main(String args[]) throws Exception {
      if (System.getSecurityManager() == null)
         System.setSecurityManager(new SecurityManager() {
            @Override
            public void checkPermission(java.security.Permission perm) {}
         });  // give loaded controllers FULL permissions
      String server     = args.length > 0 ? args[0] : "//localhost:1198/main";
      int clientPort    = args.length > 1 ? Integer.parseInt(args[1]) : 0;
      String clientHost = args.length > 2 ? args[2] : null;
      int localPort     = args.length > 3 ? Integer.parseInt(args[3]) : 0;
      String localHost  = args.length > 4 ? args[4] :
         java.net.InetAddress.getLocalHost().getHostAddress();
      Remote.config(localHost, localPort, clientHost, clientPort);
      Object object = java.rmi.Naming.lookup(server); // fetch service by URL
      object = Remote.invoke(object, "getController", null);
      try { Remote.invoke(object, "init", new gnu.cajo.Cajo()); }
      catch(Throwable t) {} // won't work running in WebStart
      object = Remote.invoke(object, "getView", null);
      String title = null;
      try { title = System.getProperty("cajo.sdk.Client.title"); }
      catch(Throwable t) {} // won't work running in WebStart
      if (title == null) title = "cajo viewer";  // default
      new Client((JComponent)object, title + " - " + server);
   }
}
