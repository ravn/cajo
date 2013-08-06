package view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import cajo.sdk.gui.Display;
import cajo.sdk.gui.TileLayout;
import cajo.sdk.gui.Wallpaper;

/*
 * This example class creates an example view object for a cajo grail server
 * controller.
 */

/**
 * This class creates an example graphical view object for a <a href=https://cajo.dev.java.net>
 * cajo</a> grail server {@link cajo.sdk.AbstractController controller}.<br>
 */
public final class View extends cajo.sdk.AbstractView {
   private static final long serialVersionUID = 1L;
   /**
    * This example display control is positioned in the center of the screen.
    * It is public to allow it to be manipulated by its controller object.
    */
   public final Display center;
   /**
    * This example display control is positioned on the bottom of the center
    * display. It is public to allow it to be manipulated by its controller
    * object.
    */
   public final Display bottom;
   /**
    * This example display control is positioned to the right of the center
    * display. It is public to allow it to be manipulated by its controller
    * object.
    */
   public final Display right;
   /**
    * This example display control is positioned on the top of the center
    * display. It is public to allow it to be manipulated by its controller
    * object.
    */
   public final Display top;
   /**
    * This example display control is positioned to the left of the center
    * display. It is public to allow it to be manipulated by its controller
    * object.
    */
   public final Display left;
   /**
    * The constructor creates the subcomponents, and arranges them within
    * the bounds of this component. It takes no arguments, but is free to
    * do so, if needed. In this case, for example, the colours are hard-coded,
    * however they could be set by the receiving container, or make use of the
    * system colour properties: to support a consistent scheme across views.
    * @throws java.io.IOException If the necessary resource files
    * cannot be found.
    */
   public View() throws java.io.IOException { // may be thrown by wallpapers
      setLayout(new TileLayout());
      setSize(640, 480); // set a default gui window size:
      Wallpaper paper = new Wallpaper("/view/inc/paper.jpg", true);
      Wallpaper icon = new Wallpaper("/view/inc/search.gif", false);

      center = new Display(23, 40, 12, Font.PLAIN);
      center.display("Center\n\n", true);
      center.setForeground(Color.black);
      center.setBackground(Color.green);
      center.border = true;
            
      bottom = new Display(1, 60, 12, Font.PLAIN);
      bottom.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 14));
      bottom.display("Status Bar", false);
      bottom.setForeground(Color.orange);
      bottom.leftAligned = true;
      bottom.border = true;

      right = new Display(23, 20, 12, Font.PLAIN);
      right.setForeground(Color.pink);
      right.setBackground(Color.darkGray);
      right.display("Right", true);
      right.border = true;

      top = new Display(1, 60, 12, Font.PLAIN);
      top.setFont(new Font("SansSerif", Font.BOLD, 14));
      top.display("Title Bar", false);
      top.setForeground(Color.blue);
      top.border = true;

      left = new Display(23, 20, 12, Font.PLAIN);
      left.setForeground(Color.red);
      left.setBackground(Color.yellow);
      left.display("Left", true);
      left.border = true;

      addComponent(center,  // components are laid out first to last
         TileLayout.CENTERINDENT + TileLayout.CENTEROFFSET +
         TileLayout.PREFWIDTH    + TileLayout.PREFHEIGHT
      );
      addComponent(left, center,
         TileLayout.LEFTINDENT   + TileLayout.NOOFFSET +
         TileLayout.PREFWIDTH    + TileLayout.PREFHEIGHT
      );
      addComponent(right, center,
         TileLayout.RIGHTINDENT  + TileLayout.NOOFFSET +
         TileLayout.PREFWIDTH    + TileLayout.PREFHEIGHT
      );
      addComponent(top, center,
         TileLayout.CENTERINDENT + TileLayout.TOPOFFSET +
         TileLayout.PREFWIDTH    + TileLayout.PREFHEIGHT
      );
      addComponent(bottom, right,
         TileLayout.FULLINDENT   + TileLayout.BOTTOMOFFSET +
         TileLayout.PREFWIDTH    + TileLayout.PREFHEIGHT
      );
      addComponent(icon, TileLayout.PROPBOUNDS,
         new Rectangle(350, 350, 100, 100)
      ); // components can overlap
      addComponent(paper, TileLayout.FULLSIZE); // walpaper underneath
      // unfortunately components are drawn last to first (AWT not me!)
   }
   /** {@inheritDoc} */
   @Override
   public String toString() { return "ExampleView"; }
   /**
    * This function provides a simple unit test of the GUI functionality.
    * It is particularly handy for easily checking the appearance of the
    * view, without the need to start up a server.
    * @param args Command line arguments are not used in this test.
    * @throws java.io.IOException if the wallpaper and icon images cannot
    * be located
    */
   public static void main(String... args) throws java.io.IOException {
      new View().test(); // perform view unit test
   }
}
