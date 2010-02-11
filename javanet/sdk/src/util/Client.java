package util;

import java.awt.*;
import javax.swing.*;
import java.rmi.registry.*;
import java.rmi.MarshalledObject;
import gnu.cajo.invoke.Remote;

/*
 * Graphical Proxy Loader JApplet
 * The cajo project: https://cajo.dev.java.net
 * For issues or suggestions mailto:cajo@dev.java.net
 * This class is released into the public domain.
 */

/**
 * This <i>internal-use only</i> helper class is used to create a hosting JVM
 * to receive a graphical controller view from a cajo service JVM.<p>
 * To use the client class to connect to a service directly, perform the
 * following:<br><br>
 * <tt><pre> java -cp client.jar:grail.jar util.Client //myHost:1198/main</tt></pre><br><br>
 * <i>NB:</i> this is a special mode! When clients operate this way, the
 * controllers will load with the <i>same</i> permissions as the user
 * who launched it, rather than in the Applet/WebStart sandbox. This is very
 * useful for special <i>"priviliged"</i> proxies that require more
 * permissions.
 * @author <a href=http://wiki.java.net/bin/view/People/JohnCatherino>
 * John Catherino</a>
 */
public final class Client extends JApplet {
   private static Object proxy;
   /**
    * The default constructor performs no function.
    */
   public Client() {}
   /**
    * This method provides the standard mechanism to identify this JApplet.
    * @return The identification string for this JApplet.
    */
   public String getAppletInfo() {
      return "cajo Controller Applet, by John Catherino";
   }
   /**
    * This method describes the optional client parameters. There are five
    * such parameters which can be specified: <i>(normally these are all
    * set automatically by the CodebaseServer class in gnu.cajo.utils)</i><ul>
    * <li>The <code>proxyName</code> parameter is the name of the service
    * object registered in the server's registry.
    * <li>The <code>proxyPort</code> parameter is the outbound port number on
    * which to contact the server.
    * <li>The <code>clientHost</code> parameter is the external domain name or
    * IP address the server must use to callback its view.  It would need to
    * be specified if the client is operating behind a NAT router.
    * <li>The <code>clientPort</code> parameter is the external inbound port
    * number on which the server can contact its view. It would need to be
    * specified if the client is behind NAT, to map to the correct local port.
    * If a firewall is being used, it must be a permitted inbound port.
    * <li>The <code>localPort</code> parameter is the internal inbound port
    * number on which the server can contact its proxy. It may need to be
    * specified if the client is using port forwarding, <i>(unlikely)</i> to
    * map to the correct remote port.
    * </ul>
    * When using Client from the command line, it is possible to set the
    * Client frame explicitly. To do this, simply type:<br><br><tt>
    * java -cp client.jar:grail.jar -Dgnu.cajo.invoke.Client.title="My Frame
    * Title" util.Client //myHost:1198/test</tt><br><br>
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
    * This method connects back to its hosting server and requests the item
    * from the server's rmiregistry. Next it will invoke a getController() on
    * the remote reference to request its proxy item. The returned object
    * will have its getView method invoked to obtain its graphical JComponent
    * representation, which will then be added into the JApplet via the Swing
    * event dispatch thread. The method invocation is passed on to the
    * controller. <i>(if it implements one)</i>
    */
    public void init() {
      try {
         String proxyName  = getParameter("proxyName");
         String proxyPort  = getParameter("proxyPort");
         String clientHost = getParameter("clientHost");
         String clientPort = getParameter("clientPort");
         String localPort  = getParameter("localPort");
         int pPort = proxyPort  != null ? Integer.parseInt(proxyPort)  : 1198;
         int cPort = clientPort != null ? Integer.parseInt(clientPort) : 0;
         int lPort = localPort  != null ? Integer.parseInt(localPort)  : 0;
         Remote.config(null, lPort, clientHost, cPort);
         proxy = LocateRegistry.getRegistry(getCodeBase().getHost(), pPort).
            lookup(proxyName);
         proxy = Remote.invoke(proxy, "getController", null);
         proxy = ((MarshalledObject)proxy).get();
         SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
               try {
                  getContentPane().add((JComponent)
                     Remote.invoke(proxy, "getView", null));
                  try { Remote.invoke(proxy, "init", null); }
                  catch(Exception x) {} // if not supported... ok
                  validate();
               } catch(Exception x) { showStatus(x.getLocalizedMessage()); }
            }
         });
      } catch(Exception x) { showStatus(x.getLocalizedMessage()); }
   }
   /**
    * This method is called from the AppleContext, each time the JApplet
    * becomes visible. The method invocation is passed on to the controller.
    * <i>(if it implements one)</i>
    */
   public void start() {
      try { Remote.invoke(proxy, "start", null); } catch(Exception x) {}
   }
   /**
    * This method is called from the AppleContext, each time the JApplet
    * becomes invisible. The method invocation is passed on to the controller.
    * <i>(if it implements one)</i>
    */
   public void stop() {
      try { Remote.invoke(proxy, "stop", null); } catch(Exception x) {}
   }
   /**
    * This method is called from the AppleContext, when the JApplet is being
    * disposed. The method invocation is passed on to the controller.
    * <i>(if it implements one)</i>
    */
   public void destroy() {
      try { Remote.invoke(proxy, "destroy", null); } catch(Exception x) {}
   }
   /**
    * The application creates a graphical Component proxy hosting VM. With the
    * URL argument provided, it will use the static Remote.getItem method of
    * the Remote class to contact the server. It will then invoke a
    * null-argument getController on the resulting reference to request the
    * primary view object of the service.<br><br>
    * When using the Client from the command line, it is possible to set the
    * Client frame title explicitly. To do this, simply type:<br><br><tt>
    * java -cp cajo.jar -Dutil.Client.title="My Frame Title"
    * util.Client //myHost:1198/test</tt><br><br>
    * <i>NB:</i> When running Client as an application (<i><u>except</u> via
    * WebStart/Applet</i>) it will run at the <b>same</b> level of priviliges
    * as the process that started it. This is useful for example, when a
    * special, administrative process needs to be run locally. It can also be
    * used when a machine wishes to aid a service, offering up its computing
    * resources.<br><br>
    * To restrict controller's permissions, use a startup invocation
    * similar to the following:<br><br>
    * <tt>java -cp client.jar:grail.jar -Djava.security.manager
    * -Djava.security.policy=controller.policy util.Client ...</tt><br><br>
    * See the project client <a href=https://cajo.dev.java.net/client.html>
    * documentation</a>, for more details.<br><br>
    * The startup requires one <i>mandatory,</i> and up to four <i>optional</i>
    * configuration parameters, in this order:<ul>
    * <li><tt>args[0] - </tt>The URL where to get the graphical proxy item:<br>
    * file:// http:// ftp:// ..., //host:port/name (rmiregistry), /path/name
    * (serialised), or path.Name (class).
    * <li><tt>args[1] - </tt>The optional external client port number,
    * if using NAT.
    * <li><tt>args[2] - </tt>The optional external client host name,
    * if using NAT.
    * <li><tt>args[3] - </tt>The optional internal client port number,
    * if using NAT.
    * <li><tt>args[4] - </tt>The optional internal client host name,
    * if multi home/NIC.</ul>
    */
   public static void main(final String args[]) throws Exception {
      if (System.getSecurityManager() == null) System.setSecurityManager(
         new SecurityManager() { // allow loaded controllers FULL permissions
            public void checkPermission(java.security.Permission perm) {}
         }
      );
      if (args.length > 0) { // parse command line arguments
         int clientPort    = args.length > 1 ? Integer.parseInt(args[1]) : 0;
         String clientHost = args.length > 2 ? args[2] : null;
         int localPort     = args.length > 3 ? Integer.parseInt(args[3]) : 0;
         String localHost  = args.length > 4 ? args[4] : null;
         Remote.config(localHost, localPort, clientHost, clientPort);
         proxy = Remote.getItem(args[0]);
         proxy = Remote.invoke(proxy, "getController", null);
         proxy = ((MarshalledObject)proxy).get();
         final JComponent view =
            (JComponent)Remote.invoke(proxy, "getView", null);
         if (view != null) SwingUtilities.invokeLater(new Runnable() {
            public void run() {
               try {
                  String title = "cajo Proxy Viewer";
                  try { title = System.getProperty("util.Client.title"); }
                  catch(Exception x) {} // won't work in WebStart
                  JFrame frame = new JFrame(title + '-' + args[0]);
                  frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                  frame.getContentPane().add(view);
                  frame.pack();
                  frame.setVisible(true);
               } catch(Exception x) { x.printStackTrace(); }
            }
         });
      } else System.err.println("service URL required");
   }
}
