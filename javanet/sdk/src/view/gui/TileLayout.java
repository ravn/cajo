package view.gui;

import java.awt.*;
import java.util.Vector;
import java.io.Serializable;

/*
 * Relative Component Layout Manager
 * The cajo project: https://cajo.dev.java.net
 * For issues or suggestions mailto:cajo@dev.java.net
 * This class is released into the public domain.
 * Written by John Catherino
 */

/**
 * A rather unique LayoutManager. In addition to laying out components
 * relative to the container, it also supports layout relative to components
 * <i>within</i> the container. Its purpose is to support arbitrarily complex
 * component layouts, of an unlimited number of components, within a single
 * container. It can easily create complex layouts that would otherwise
 * require many subpanels, and multiple standard layout managers. It also can
 * create layouts that are actually <i>impossible</i> to do with standard
 * layout managers. These features make this layout manager extremely
 * flexible, and makes advancedlayouts perform extremely fast. It just may
 * be, the last and only LayoutManager you'll ever need. ;-)<p>
 * Components can be laid out above, below, left, right, or overlapping
 * either a referenced component in the panel, or to the panel itself. Its
 * width and height can be specified with similar flexibility. Absolute and
 * proportional bounds are also supported. In typical use, one or more
 * <i>'reference'</i> tiles are laid, and the rest of the components are set
 * relative to them.
 * 
 * <p>Usage example:<blockquote><tt><pre>
 *    panel.add(new JLabel("hello"), new Object[] {
 *       TileLayout.bounds(
 *          TileLayout.LEFTINDENT, TileLayout.NOOFFSET,
 *          TileLayout.PROPWIDTH,  TileLayout.FULLHEIGHT
 *       ),
 *       refComponent, new Insets(-5, 10, 5, 10),
 *       new Rectangle(0, 0, 333, 0) // proportion rectangle 33.3%w
 *    });
 * </tt></pre></blockquote>
 * 
 * <p>Up to four alignment constraints can be specified, their order does not
 * matter:
 * 
 * <p><ul>
 * <li> Positioning constants for indent, offset, width, and height
 * <li> A Component for placement relative to, otherwise the container
 * <li> A Rectangle for fixed and proportional component bounds
 * <li> Insets to trim the resulting component boundaries
 * </ul>
 * 
 * <p><i>NB:</i> the JRE <i>draws</i> components from last added
 * to first; but the manager sizes them first to last. Therefore, in order to
 * layout relative to another component, the reference component must be
 * added <i>before</i> the dependent one. This is critically important when
 * components are laid out on top of others, or overlapping. This is a
 * <i>super nice</i> capability of this layout manager.
 * 
 * @author <a href=http://wiki.java.net/bin/view/People/JohnCatherino>
 * John Catherino</a>
 */
public final class TileLayout implements LayoutManager2, Serializable {
   private static final long serialVersionUID = 2L;
   private static final Dimension NONE  = new Dimension();
   private final Vector components  = new Vector();
   private final Vector constraints = new Vector();
   private void align(Dimension cont, Object cons[], Component comp) {
      int align = 0;
      Insets insets = null;
      Rectangle tile = null, fixd = null;
      if (cons != null) {
         for (int i = 0; i < cons.length; i++) { // gather constraints
            if (cons[i] != null) {
               if (cons[i] instanceof Rectangle) fixd = (Rectangle)cons[i];
               else if (cons[i] instanceof Insets) insets = (Insets)cons[i];
               else if (cons[i] instanceof Integer) align = ((Integer)cons[i]).intValue();
               else if (cons[i] instanceof Component) tile = ((Component)cons[i]).getBounds();
            }
         }
      }
      if (tile == null) tile = new Rectangle(cont);
      Rectangle pref = new Rectangle(tile.getLocation(), comp.getPreferredSize());
      // perform component positioning:
      if ((align & 0x004000) != 0) pref.width = fixd.width;
      else if ((align & 0x008000) != 0) pref.width = (tile.width * fixd.width + 500) / 1000;
      else if ((align & 0x010000) != 0) pref.width = tile.width;
      if ((align & 0x080000) != 0) pref.height = fixd.height;
      else if ((align & 0x100000) != 0)
         pref.height = (tile.height * fixd.height + 500) / 1000;
      else if ((align & 0x200000) != 0) pref.height = tile.height;
      if ((align & 0x000001) != 0) pref.x -= pref.width;
      else if ((align & 0x000002) != 0) pref.x += (tile.width - pref.width >> 1);
      else if ((align & 0x000004) != 0) pref.x += tile.width - pref.width;
      else if ((align & 0x000008) != 0) pref.x += tile.width;
      else if ((align & 0x000010) != 0) pref.x += fixd.x;
      else if ((align & 0x000020) != 0) pref.x += (tile.width * fixd.x + 500) / 1000;
      if ((align & 0x000040) != 0) pref.y -= pref.height;
      else if ((align & 0x000080) != 0) pref.y += (tile.height - pref.height >> 1);
      else if ((align & 0x000100) != 0) pref.y += tile.height - pref.height;
      else if ((align & 0x000200) != 0) pref.y += tile.height;
      else if ((align & 0x000400) != 0) pref.y += fixd.y;
      else if ((align & 0x000800) != 0) pref.y += (tile.height * fixd.y + 500) / 1000;
      if ((align & 0x001000) != 0)
         pref.setBounds(0, pref.y, pref.x + pref.width, pref.height);
      else if ((align & 0x002000) != 0) pref.width = cont.width - pref.x;
      if ((align & 0x020000) != 0)
         pref.setBounds(pref.x, 0, pref.width, pref.y + pref.height);
      else if ((align & 0x040000) != 0) pref.height = cont.height - pref.y;
      if (insets != null) { // apply insets, if any:
         pref.x += insets.left;
         pref.y += insets.top;
         pref.width  -= insets.left + insets.right;
         pref.height -= insets.top  + insets.bottom;
      }
      Dimension d = comp.getMinimumSize(); // this can sometimes be surprising!
      if (pref.width  < d.width)  pref.width  = d.width;
      if (pref.height < d.height) pref.height = d.height;
      comp.setBounds(pref); // tile is set!
   }
   /**
    * A little helper method, to convert component bounds into a TileLayout
    * constraint object.
    * @param indent The x-position of the component, can be positive or
    * negative
    * @param offset The y-position of the component, can be positive or
    * negative
    * @param width The horizontal size of the component
    * @param height The vertical size of the component
    */
   public static final Integer
      bounds(int indent, int offset, int width, int height) {
      return new Integer(indent + offset + width + height);
   }
   /**
    * This constant specifies placement of the component even with the left
    * border of the provided reference component, otherwise to the container
    * itself.
    */
   public static final int NOINDENT = 0x000000;
   /**
    * This constant specifies placement of the component with its right border
    * one pixel to the left of the provided reference component, otherwise to
    * the container itself.
    */
   public static final int LEFTINDENT = 0x000001;
   /**
    * This constant specifies placement of the component evenly between the
    * left and right borders of the provided reference component, otherwise
    * to the container itself.
    */
   public static final int CENTERINDENT = 0x000002;
   /**
    * This constant specifies placement of the component with its right border
    * evenly aligned with the right border of the provided reference component,
    * otherwise to the container itself.
    */
   public static final int FULLINDENT = 0x000004;
   /**
    * This constant specifies placement of the left border of the component one
    * pixel to the right of the provided reference component origin, otherwise
    * to the container itself.
    */
   public static final int RIGHTINDENT = 0x000008;
   /**
    * Used in conjunction with a Rectangle constraint, the x value represents
    * the desired component indent as a fixed number of pixels right of the
    * provided component origin, otherwise to the container itself.
    */
   public static final int FIXEDINDENT = 0x000010;
   /**
    * Used in conjunction with a Rectangle constraint, the x value represents
    * the desired component indent as a percentage (2000 = 200.0%) of a
    * provided component, otherwise to the container itself.
    */
   public static final int PROPINDENT = 0x000020;
   /**
    * This constant specifies placement of the component even with the top
    * border of the provided reference component, otherwise to the container
    * itself.
    */
   public static final int NOOFFSET = 0x000000;
   /**
    * This constant specifies placement of the component with its bottom
    * border one pixel above the provided reference component, otherwise to
    * the container itself.
    */
   public static final int TOPOFFSET = 0x000040;
   /**
    * This constant specifies placement of the component evenly between the top
    * and bottom borders of the provided reference component, otherwise to the
    * container itself.
    */
   public static final int CENTEROFFSET = 0x000080;
   /**
    * This constant specifies placement of the component with its bottom
    * border evenly aligned with the bottom border of the provided reference
    * component, otherwise to the container itself.
    */
   public static final int FULLOFFSET = 0x000100;
   /**
    * This constant specifies placement of the top border of the component one
    * pixel below the provided reference component origin, otherwise to the
    * container itself.
    */
   public static final int BOTTOMOFFSET = 0x000200;
   /**
    * Used in conjunction with a Rectangle constraint, the y value represents
    * the desired component offset as a fixed number of pixels below the
    * provided component origin, otherwise to the container itself.
    */
   public static final int FIXEDOFFSET = 0x000400;
   /**
    * Used in conjunction with a Rectangle constraint, the y value represents
    * the desired component offset as a percentage (2000 = 200.0%) of a
    * provided component, otherwise to the container itself.
    */
   public static final int PROPOFFSET = 0x000800;
   /**
    * This constant specifies that the component have its preferred width, as
    * returned by its getPreferredSize method.
    */
   public static final int PREFWIDTH = 0x000000;
   /**
    * This constant specifies that the component be made wide enough such that
    * given its computed right margin, its left margin will align with that of
    * its container.
    */
   public static final int CLAMPLEFT = 0x001000;
   /**
    * This constant specifies that the component be made wide enough such that
    * given its computed origin, its right margin will align with that of its
    * container.
    */
   public static final int CLAMPRIGHT = 0x002000;
   /**
    * Used in conjunction with a Rectangle constraint, the width value
    * represents the desired component width as a fixed number of pixels.
    */
   public static final int FIXEDWIDTH = 0x004000;
   /**
    * Used in conjunction with a Rectangle constraint, the width value
    * represents the desired component offset as a percentage (2000 = 200.0%) of
    * a provided component, otherwise to the container itself.
    */
   public static final int PROPWIDTH = 0x008000;
   /**
    * This constant specifies that the component be made exactly as wide as its
    * reference component, otherwise as the container itself.
    */
   public static final int FULLWIDTH = 0x010000;
   /**
    * This constant specifies that the component have its preferred height, as
    * returned by its getPreferredSize method.
    */
   public static final int PREFHEIGHT = 0x000000;
   /**
    * This constant specifies that the component be made high enough such that
    * given its computed bottom margin, its top margin will align with that of
    * its container.
    */
   public static final int CLAMPTOP = 0x020000;
   /**
    * This constant specifies that the component be made high enough such that
    * given its computed origin, its bottom margin will align with that of its
    * container.
    */
   public static final int CLAMPBOTTOM = 0x040000;
   /**
    * Used in conjunction with a Rectangle constraint, the height value
    * represents the desired component width as a fixed number of pixels.
    */
   public static final int FIXEDHEIGHT = 0x080000;
   /**
    * Used in conjunction with a Rectangle constraint, the height value
    * represents the desired component offset as a percentage (2000 = 200.0%) of
    * a provided component, otherwise to the container itself.
    */
   public static final int PROPHEIGHT = 0x100000;
   /**
    * This constant specifies that the component be made exactly as high as
    * its reference component, otherwise as the container itself.
    */
   public static final int FULLHEIGHT = 0x200000;
   /**
    * A frequently used position, used in conjunction with a reference
    * component, it will locate the component directly over, with the same
    * width and height.
    */
   public static final Object ABOVE[] = new Object[] {
      new Integer(NOINDENT + TOPOFFSET + FULLWIDTH + FULLHEIGHT)
   };
   /**
    * A frequently used position, used in conjunction with a reference
    * component, it will locate the component directly beneath, with the same
    * width and height.
    */
   public static final Object BELOW[] = new Object[] {
      new Integer(NOINDENT + BOTTOMOFFSET + FULLWIDTH + FULLHEIGHT)
   };
   /**
    * A frequently used position, used in conjunction with a reference
    * component, it will locate the component directly adjacent, to its right,
    * with the same width and height.
    */
   public static final Object RIGHT[] = new Object[] {
      new Integer(RIGHTINDENT + NOOFFSET + FULLWIDTH + FULLHEIGHT)
   };
   /**
    * A frequently used position, used in conjunction with a reference
    * component, it will locate the component directly adjacent, to its left,
    * with the same width and height.
    */
   public static final Object LEFT[] = new Object[] {
      new Integer(LEFTINDENT + NOOFFSET + FULLWIDTH + FULLHEIGHT)
   };
   /**
    * A frequently used position, it will render the component using the exact
    * size of the reference component, or container.
    */
   public static final Object FULLSIZE[] = new Object[] {
      new Integer(NOINDENT + NOOFFSET + FULLWIDTH + FULLHEIGHT)
   };
   /**
    * A frequently used position, it will render use the component's own
    * fixed position and size.
    */
   public static final Object FIXEDBOUNDS[] = new Object[] {
      new Integer(FIXEDINDENT + FIXEDOFFSET + FIXEDWIDTH + FIXEDHEIGHT)
   };
   /**
    * Nothing is performed in the constructor, it is bodyless.
    */
   public TileLayout() {}
   /**
    * The primary component addition method. Components are added for layout,
    * and its constraints are provided as an Object array. Up to 4 constraints
    * may be specified, and their order is not important. The following are
    * the applicable constraints:
    * <p><ul>
    * <li> Rectangle for fixed or proportional component bounds
    * <li> Insets to trim the final boundaries after computation
    * <li> Integer of TileLayout positioning constants
    * <li> Component for relative placement, instead of container
    * </ul>
    * <p><i><u>Note</u>:</i> each added component will be inserted at the
    * head of the list, this is because the JRE, for some reason, draws the
    * components from last to first in the list.
    * @param comp The component to be laid out within the container.
    * @param cons An Object[] containing 1 or more constraints.
    */
   @SuppressWarnings("unchecked")
   public synchronized void addLayoutComponent(Component comp, Object cons) {
      components.insertElementAt(comp, 0);
      constraints.insertElementAt(cons, 0);
   }
   /**
    * This method will make the added component exactly the same size as the
    * container itself. It is useful for adding wallpaper.
    * @param name The name of the component, unused by TileLayout.
    * @param comp The component to fill the container.
    */
   public void addLayoutComponent(String name, Component comp) {
      addLayoutComponent(comp, FULLSIZE);
   }
   /**
    * This method removes the component from the ordered layout list. It
    * performs nothing, if the component is not in the layout.
    * @param comp The component to remove from the layout
    */
   public synchronized void removeLayoutComponent(Component comp) {
      int i = components.indexOf(comp);
      if (i >= 0) {
         components.remove(i);
         constraints.remove(i);
      }
   }
   /**
    * Lays out the components, first added to last added, according to their
    * specified constraints.
    * @param parent The container in which to layout the components
    */
   public void layoutContainer(Container parent) {
      Object[] comps, cons;
      synchronized(this) {
         comps = components.toArray();
         cons  = constraints.toArray();
      }
      Dimension container = parent.getSize();
      for (int i = comps.length - 1; i >= 0; i--)
         align(container, (Object[])cons[i], (Component)comps[i]);
   }
   /**
    * Bodyless implementation, as TileLayout uses no cached information.
    * @param target Ignored in this implementation
    */
   public void invalidateLayout(Container target) {}
   /**
    * Indicate center x-axis alignment.
    * @param target Ignored in this implementation
    */
   public float getLayoutAlignmentX(Container target) { return 0.5F; }
   /**
    * Indicate center y-axis alignment.
    * @param target Ignored in this implementation
    */
   public float getLayoutAlignmentY(Container target) { return 0.5F; }
   /**
    * Indicate no minimum size.
    * @param parent Ignored in this implementation
    */
   public Dimension minimumLayoutSize(Container parent) { return NONE; }
   /**
    * Indicate no maximum size.
    * @param parent Ignored in this implementation
    */
   public Dimension maximumLayoutSize(Container parent) { return NONE; }
   /**
    * Indicate no preferred size.
    * @param parent Ignored in this implementation
    */
   public Dimension preferredLayoutSize(Container parent) { return NONE; }
}
