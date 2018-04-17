-- JohnCatherino - 17 Aug 2005

Using the cajo proxy mechanism
===
All that is required for this example, is the cajo project 40kB codebase cajo.jar, and the very small source file ProxyTest, which will be explained completely below;

ProxyTest.java:
---

    import java.awt.*;
    import java.awt.event.*;
    import gnu.cajo.invoke.Remote;
    import gnu.cajo.utils.ItemServer;
    import gnu.cajo.utils.CodebaseServer;

    public class ProxyTest {

       public static class TestProxy extends Panel implements ActionListener {
          private Object selfRef, item;
          TestProxy() {
             super(new BorderLayout());
             Label label = new Label("Test Proxy", Label.CENTER);
             add(label, BorderLayout.CENTER);
             Button button = new Button("fun button!");
             button.addActionListener(this);
             add(button, BorderLayout.SOUTH);
          }
          public void setItem(Object item) { this.item = item; }
          public Component init(Object ref) {
             selfRef = ref;
             return this;
          }
          public void actionPerformed(ActionEvent e) {
             try { Remote.invoke(item, "pressed", null); }
             catch(Exception x) { x.printStackTrace(); } // network failure
          }
          public Dimension getPreferredSize() { return new Dimension(300, 300); }
       }

       private TestProxy proxy = new TestProxy();
       public Component getProxy() { return proxy; }
       public void pressed() { System.out.println("I heard that!"); }
       public static void main(String args[]) {
          try {
             new CodebaseServer(null, 0);
             ProxyTest pt = new ProxyTest();
             ItemServer.bind(pt, "main", pt.proxy);
             System.out.print("Server running on interface ");
             System.out.println(Remote.getServerHost());
             System.out.print("Using TCP port ");
             System.out.println(Remote.getServerPort());
             System.out.print("Codebase server running: ");
             System.out.println(System.getProperty("java.rmi.server.codebase"));
          } catch (Exception x) { x.printStackTrace(); }
       }
    }
The ProxyTest represents an item, meaning a remotely callable object furnished by a Virtual Machine. It has a proxy, called TestProxy, which it provides to remote Virtual Machines. TestProxy is a static member class simply to allow this example to consist of a single source file. Many times, proxies exist in separate files. Combining them this way makes their interaction a little clearer, as we will see.

TestProxy is a graphical user interface, provided to remote Virtual Machines, it also interacts with its sending object ProxyTest. TestProxy is designed to work seamlessly with the generic cajo graphical Client class, which we will also be using in this example. TestProxy declares two special methods; setItem, and init. When the ProxyTest item binds itself, it is using the three argument bind method. This tells the ItemServer that this third argument is a proxy to the first argument. The ItemServer will automatically invoke the proxy's setItem method, if it has one, and provide it a remote reference to its serving object, on which it may communicate with it, when it reaches a remote Virtual Machine host. The second method, init, is called by the generic cajo graphical Client class, to signal that it has arrived at its remote Virtual Machine destination. The proxy should perform any local preparation it needs to do, before becoming visible, and return a component to display, in this case itself. The client also provides a remoted reference to the proxy as an argument, which it may give to its remote server object, to provide a link for asynchronous server to client communication.

ProxyTest is the server object. It too has two special methods declared. The first method, getProxy, is called by the generic cajo graphical Client, to request the proxy widget. The second method, pressed, is called by the proxy object, to callback the remote server object asynchronously, whenever its button is pushed. ProxyTest also starts up a default CodebaseServer, to provide the necessary resources a remote Virtual Machine would need, in order to instantiate the proxy.

**Compiling the example:**

The file above is compiled using the following command from the console:

    javac -classpath cajo.jar;. ProxyTest.java

Now the server can be run. To do this, use the following command:

    java -classpath cajo.jar;. ProxyTest

This will result in the following console output:

    Server running on interface hostName
    Using TCP port ####
    Codebase server running: http://hostName:NNNN/

Where hostName and ####, shown in the first two lines of output, are the address and port that will be needed to connect to the server item. Since the port number is anonymous, meaning selected from the pool of free port numbers, by the operating system. Typically it will change every time the server is run. This is important to remember, because as we will see, the client needs to specify this exact port number.

(_Note:_ The two port numbers #### and NNNN are not the same. Typically #### = NNNN + 1)

**Using the proxy:**


Now we go to another machine, though we really could use the same machine too. Again we open up a console window, and launch the generic cajo graphical Client, to request the proxy:

    java -cp cajo.jar gnu.cajo.invoke.Client //hostName:####/

(Note: do not forget the final / as it is required.)

The Client will request the proxy by remotely invoking the ProxyTest getProxy method, it will then locally invoke the init method on the returned proxy object, and voilà, the graphical proxy widget appears!

**Now for the interactive part:**

When the client VM user presses the button in the proxy GUI, the remote server proudly exclaims:

    I heard that!

However, there are two very important things to keep in mind:

* The client does not have a reference to the proxy, rather it has an exact bitwise copy. Unlike conventional Java, this was an object pass-by-value! Any changes that occur to the remote proxy, none in this particular case; will not be reflected in the original master proxy, held by ProxyTest.
* If many clients request a proxy, they will all receive perfect bitwise copies. This means that the ProxyTest method, pressed, can be invoked reentrantly by all of its proxies. Each remote call represents a separate thread of execution, if there are any concurrency issues, they will have to be properly synchronised.

**Extra fun:**

Remember that last line shown when the ProxyTest started:

    Codebase server running: http://hostName:NNNN/

(remembering of course, that the hostName, and NNNN, will vary depending on the server)
Just for fun, you can go to another remote machine, or even the same machine, open a browser window and type in that URL.

Behold the graphical proxy widget, running inside the browser! :-)

**And finally:**

Interested to make a Swing version, i.e. JProxyTest? It is a slightly different construction, but fortunately just as easy.
