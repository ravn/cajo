package cajo.sdk;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

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
 * The view is a base class of graphically interactive control objects.
 * Normally views are completely <i>unaware</i> of the existence of their
 * controller objects, and of the <a href=https://cajo.dev.java.net>cajo</a>
 * framework entirely; potentially the same view could be used by multiple
 * {@link AbstractController controller} objects. It is intended as a
 * <i>'skin'</i> to a controller object, easily interchangeable with another.
 * A given {@link AbstractService service} could optionally provide multiple
 * views. Its purpose is to move the computational requirements of data input
 * and presentation, off the server, to the client. A view can even be used
 * as a monitor, configuration, or debug screen, for other objects which are
 * interacting directly with its controller object.
 * <br><i><u>NB</u>:</i> It is possible, but not recommended, for the view to
 * interact directly with its service.
 * @see AbstractController
 * @see AbstractService
 * @author John Catherino
 */
public abstract class AbstractView extends javax.swing.JComponent {
   private static final long serialVersionUID = 1L;
   private Rectangle clip;
   /**
    * The default constructor performs no function.
    */
   protected AbstractView() {}
   /**
    * This class provides a simple unit test used in the development of
    * widgets. It will place the widget under development in its own frame.
    */
   protected final void test() { // unit test
      javax.swing.JFrame frame = new javax.swing.JFrame("Unit Test");
      frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
      frame.getContentPane().add(this);
      frame.pack();
      frame.setVisible(true);
   }
   /**
    * A handy helper method to add components to this one.
    * @param comp The component to be added
    * @param cons The collection of layout constraints to be applied
    */
   protected void addComponent(Component comp, Object... cons) {
      add(comp, cons);
   }
   /**
    * This flag, if set, will result in a one pixel-wide rectangle being
    * drawn around the widget's inside border. It is typically used during
    * development, to see the component outlines for layout purposes.
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
   @Override
   public Dimension getPreferredSize() { return getSize(); }
   /**
    * This method is called by the display runtime, to prepare the widget
    * for painting itself. It will clear its background if the opaque flag
    * is set. 
    * @param g The graphics context, on which to prepare the display area
    */
   @Override
   public final void update(Graphics g) {
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
   @Override
   public void paint(Graphics g) {
      super.paint(g);
      if (border) g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
   }
   /**
    * This method is called <i>before</i> the view becomes visible.
    */
   public void init() {}
   /**
    * This method is called each time the view becomes visible.
    */
   public void start() {}
   /**
    * This method is called each time the view becomes invisible. It can be
    * used to cut down computation related to graphical presentation.
    */
   public void stop() {}
   /**
    * This method is called when the view is no longer needed.<br>
    * <i><u>NB</u>:</i> Whilst handy, one cannot rely too critically on this
    * call, the remote client could suddenly die, for some reason, without
    * ever calling this method. It should only be used for client-side
    * releasing of resources, not for critical interaction with the server.
    */
   public void destroy() {}
   /**
    * This method provides a means to identify this view.
    * @return An identifier <i>(not a description)</i> of the view
    */
   @Override
   public String toString() { return "AbstractView"; }
}
