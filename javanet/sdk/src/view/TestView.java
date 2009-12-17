package view;

import java.awt.*;
import view.gui.*;

/*
 * An example view object for a cajo grail server controller.
 * The cajo project: https://cajo.dev.java.net
 * For issues or suggestions mailto:cajo@dev.java.net
 * This class is released into the public domain.
 * Written by John Catherino 01-Dec-09
 */

/**
 * This class creates a view object for a cajo grail server controller.
 * <i>NB:</i> the class is completely unaware of the <a href=https://cajo.dev.java.net>
 * cajo</a> grail framework. It is intended as a <i>'skin'</i> to a service
 * object, easily interchangeable with another.
 * @author <a href=http://wiki.java.net/bin/view/People/JohnCatherino>
 * John Catherino</a>
 */
public class TestView extends Widget {
   // display controls, just for fun...
   private final Display a, b, c, d, e;
   /**
    * The constructor creates the subcomponents, and arranges them within
    * the bounds of this component.
    * @throws java.io.IOException If the sources for the wallpaper images
    * cannot be found.
    */
   public TestView() throws java.io.IOException { // thrown by wallpapers
      setLayout(new TileLayout());
      setSize(640, 480); // set a default gui window size:
      Wallpaper w = new Wallpaper("/view/inc/paper.jpg", true);
      Wallpaper s = new Wallpaper("/view/inc/search.gif", false);

      a = new Display(23, 40, 12, Font.PLAIN);
      a.display("Center\n", true);
      a.setForeground(Color.black);
      a.setBackground(Color.green);
      a.border = true;
            
      b = new Display(1, 60, 12, Font.PLAIN);
      b.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 14));
      b.display("Status Bar", false);
      b.setForeground(Color.orange);
      b.leftAligned = true;
      b.border = true;

      c = new Display(23, 20, 12, Font.PLAIN);
      c.setForeground(Color.pink);
      c.setBackground(Color.darkGray);
      c.display("Right", true);
      c.border = true;

      d = new Display(1, 60, 12, Font.PLAIN);
      d.setFont(new Font("SansSerif", Font.BOLD, 14));
      d.display("Title Bar", false);
      d.setForeground(Color.blue);
      d.border = true;

      e = new Display(23, 20, 12, Font.PLAIN);
      e.setForeground(Color.red);
      e.setBackground(Color.yellow);
      e.display("Left", true);
      e.border = true;

      add(a, new Object[] { // laid out first to last added
         new Integer(
            TileLayout.CENTERINDENT + TileLayout.CENTEROFFSET +
            TileLayout.PREFWIDTH    + TileLayout.PREFHEIGHT
         )
      });
      add(c, new Object[] {
         new Integer( 
            TileLayout.RIGHTINDENT + TileLayout.NOOFFSET +
            TileLayout.PREFWIDTH   + TileLayout.PREFHEIGHT
         ), a
      });
      add(s, new Object[] {
         new Integer(
            TileLayout.CENTERINDENT + TileLayout.CENTEROFFSET +
            TileLayout.PREFWIDTH    + TileLayout.PREFHEIGHT
         ), a
      });
      add(e, new Object[] {
         new Integer( 
            TileLayout.LEFTINDENT + TileLayout.NOOFFSET +
            TileLayout.PREFWIDTH  + TileLayout.PREFHEIGHT
         ), a
      });
      add(b, new Object[] {
         new Integer( 
            TileLayout.NOINDENT  + TileLayout.BOTTOMOFFSET +
            TileLayout.PROPWIDTH + TileLayout.PREFHEIGHT
         ), e, new Rectangle(0, 0, 4000, 0)
      });
      add(d, new Object[] {
         new Integer( 
            TileLayout.NOINDENT  + TileLayout.TOPOFFSET +
            TileLayout.PREFWIDTH + TileLayout.PREFHEIGHT
         ), e
      });

      add(w, TileLayout.FULLSIZE);  // drawn last to first added
   }
   /**
    * This function provides a simple unit test of the GUI functionality.
    * @param args Command line arguments are not used in this test.
    * @throws java.io.IOException if the wallpaper and icon images cannot
    * be located
    */
   public static void main(String args[]) throws java.io.IOException {
      new TestView().test(); // perform view unit test
   }
}
