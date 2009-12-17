package view.gui;

import java.awt.*;

/*
 * Base class display control
 * The cajo project: https://cajo.dev.java.net
 * For issues or suggestions mailto:cajo@dev.java.net
 * This class is released into the public domain.
 * Written by John Catherino 01-Dec-09
 */

/**
 * This is the base class of graphically interactive control objects.
 * @author <a href=http://wiki.java.net/bin/view/People/JohnCatherino>
 * John Catherino</a>
 */
public class Widget extends javax.swing.JComponent {
   private Rectangle clip;
   /**
    * The default constructor performs no function
    */
   protected Widget() {}
   /**
    * This class provides a simple unit test used in the development of
    * widgets. It will place the widget under development in its own frame.
    */
   protected void test() { // unit test
      javax.swing.JFrame frame = new javax.swing.JFrame("Unit Test");
      frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
      frame.getContentPane().add(this);
      frame.pack();
      frame.setVisible(true);
   }
   /**
    * This flag, if set, will result in a one pixel-wide rectangle being
    * drawn around the widget's inside border.
    */
   public boolean border;
   /**
    * This flag, if set, will result in the widget background being cleared
    * using its background colour, befor rendering. It effectively makes the
    * widget <i>'opaque'</i> meaning any display contents behind it in the
    * z-order will not be visible.
    */
   public boolean opaque;
   /**
    * This method is used primarily by the layout manager.
    * @return The minimal dimension that the widget could render all of
    * its content. By default, it simply returns the result of the getSize
    * method.
    */
   public Dimension getPreferredSize() { return getSize(); }
   /**
    * This method is called by the display runtime, to prepare the widget
    * for painting itself. It will clear its background if the opaque flag
    * is set. 
    * @param g The graphics context, on which to prepare the display area
    */
   public void update(Graphics g) {
      if (opaque) {
         clip = clip == null ? g.getClipBounds() : g.getClipBounds(clip);
         g.clearRect(clip.x, clip.y, clip.width, clip.height);   
      }
      paint(g);
   }
   /**
    * This method is called by the display runtime, to render the widget
    * on the display device. Upon completion of the rendering, if the border
    * flag is set, a one pixel width border will be drawn on the inside
    * boundary of the widget.
    * @param g The graphics context, on which to draw the display and its
    * contents
    */
   public void paint(Graphics g) {
      super.paint(g);
      if (border) g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
   }
}
