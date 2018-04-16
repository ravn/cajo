package example;

import example.gui.*;
import java.io.*;
import java.awt.*;

// The most formal way to build a proxy.
// Note:
// You don't HAVE TO use a Builder, the code could go in the constructor.
// You don't HAVE TO use a ProxyLoader, you could instantiate in the server.
// You don't HAVE TO use Internationalization, Strings could be hard-coded.
// Also note:
// If you do instantiate the proxy in the server, you should wrap it in a
// ZippedProxy. On the other hand, you can also use a ProxyLoader to construct
// the proxy at the client, as opposed to deserializing it.

public class Builder {
   // This class is NOT included in the proxy.jar file.
   // Doing this makes the proxy jar smaller, and can be used to discourage
   // reverse engineering of the proxy.  Try to do as much proxy
   // initialization, and configuration, in here as possible.  Also,
   // have the server send as many pre-configured objects to the proxy
   // as possible, during runtime, to further minimize the amount of
   // code to be sent to the client. To create the proxy, compile Builder
   // then run it to create a serialized Proxy object. The Builder will
   // delete its own class file automatically, when complete, to prevent
   // it from being included in the proxy's jar file.
   public static void main(String args[]) {
      try {
         // referenced solely to cause compilation:
         gnu.cajo.utils.ProxyLoader pl;
         // instantiate a proxy object:
         TestProxy proxy = new TestProxy();
         // layout components realtive to each other:
         proxy.container.setLayout(new TileLayout());
         // set a default gui window size:
         proxy.container.setSize(640, 480);

         proxy.strings = new String[] {
            "proxy_arrived", "proxy_started", "call_server",
            "proxy_ack",     "proxy_hello",   "server_call", "title",
            "response"
         };

         proxy.bundle = "example/include/Proxy";
         Wallpaper w = new Wallpaper("/example/include/paper.jpg", true);
         Wallpaper s = new Wallpaper("/example/include/search.gif", false);

         proxy.a = new Display(1, 60, 12, Font.PLAIN);
         proxy.a.setFont(new Font("SansSerif", Font.BOLD, 14));
         proxy.a.display("Title Bar", false);
         proxy.a.setForeground(Color.blue);
         proxy.a.border = true;

         proxy.b = new Display(1, 60, 12, Font.PLAIN);
         proxy.b.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 14));
         proxy.b.display("Status Bar", false);
         proxy.b.setForeground(Color.orange);
         proxy.b.leftAligned = true;
         proxy.b.border = true;

         proxy.e = new Display(23, 20, 12, Font.PLAIN);
         proxy.e.setForeground(Color.red);
         proxy.e.setBackground(Color.yellow);
         proxy.e.display("Left", true);
         proxy.e.border = true;

         proxy.c = new Display(23, 20, 12, Font.PLAIN);
         proxy.c.setForeground(Color.pink);
         proxy.c.setBackground(Color.darkGray);
         proxy.c.display("Right", true);
         proxy.c.border = true;

         proxy.d = new Display(23, 40, 12, Font.PLAIN);
         proxy.d.display("Center\n", true);
         proxy.d.setForeground(Color.black);
         proxy.d.setBackground(Color.green);
         proxy.d.border = true;
               
         // load up the panel, front to back z-order:
         proxy.container.add(s, new Object[] {
            new Integer(
               TileLayout.CENTERINDENT  + TileLayout.CENTEROFFSET +
               TileLayout.PREFWIDTH + TileLayout.PREFHEIGHT
            ), proxy.d
         });
         proxy.container.add(proxy.a, new Object[] {
            new Integer( 
               TileLayout.NOINDENT  + TileLayout.TOPOFFSET +
               TileLayout.PREFWIDTH + TileLayout.PREFHEIGHT
            ), proxy.e
         });
         proxy.container.add(proxy.b, new Object[] {
            new Integer( 
               TileLayout.NOINDENT  + TileLayout.BOTTOMOFFSET +
               TileLayout.PROPWIDTH + TileLayout.PREFHEIGHT
            ), proxy.e, new Rectangle(0, 0, 4000, 0)
         });
         proxy.container.add(proxy.c, new Object[] {
            new Integer( 
               TileLayout.RIGHTINDENT + TileLayout.NOOFFSET +
               TileLayout.PREFWIDTH   + TileLayout.PREFHEIGHT
            ), proxy.d
         });
         proxy.container.add(proxy.e, new Object[] {
            new Integer( 
               TileLayout.LEFTINDENT + TileLayout.NOOFFSET +
               TileLayout.PREFWIDTH  + TileLayout.PREFHEIGHT
            ), proxy.d
         });
         proxy.container.add(proxy.d, new Object[] {
            new Integer(
               TileLayout.CENTERINDENT + TileLayout.CENTEROFFSET +
               TileLayout.PREFWIDTH    + TileLayout.PREFHEIGHT
            )
         });
         proxy.container.add(w, new Object[] {
            new Integer( 
               TileLayout.NOINDENT  + TileLayout.NOOFFSET +
               TileLayout.FULLWIDTH + TileLayout.FULLHEIGHT
            )
         });

         // serialize and save:
         FileOutputStream
            fos = new FileOutputStream("example/include/proxy.ser");
         ObjectOutputStream oos = new ObjectOutputStream(fos);
         oos.writeObject(proxy);
         oos.flush();
         fos.flush();
         oos.close();
         fos.close();

         // delete the construction code:
         new File("example/Builder.class").delete();
      } catch (Exception x) { x.printStackTrace(System.err); }
      System.exit(0);
   }
}
