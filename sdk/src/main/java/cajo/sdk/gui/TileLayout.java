package cajo.sdk.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import java.util.Vector;
import java.io.Serializable;

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
 * A rather unique and very powerful LayoutManager. In addition to laying out
 * components relative to the container, it also supports layout relative to
 * components <i>within</i> the container. Its purpose is to support
 * arbitrarily complex component layouts, of an unlimited number of
 * components, within a single container. It can easily create complex layouts
 * that would otherwise require many subpanels, and multiple standard layout
 * managers. It also can create layouts that are actually <i>impossible</i>
 * to do with standard layout managers. These features make this layout
 * manager extremely flexible, and makes advancedlayouts perform extremely
 * fast. It just may be, the last and only LayoutManager you'll ever need.
 * <p>Components can be laid out above, below, left, right, or overlapping
 * either a referenced component in the panel, or to the panel itself. Its
 * width and height can be specified with similar flexibility. Absolute and
 * proportional bounds are also supported. In typical use, one or more
 * reference components <i>(i.e. tiles)</i> are laid, and the rest of the
 * components are set relative to them. Only visible components in the panel
 * are laid out, allowing for interesting affects by modifying visibility
 * at runtime.
 * 
 * <p>Usage example:<blockquote><tt><pre>
 *    panel.add(new JLabel("hello"), new Object[] {
 *       TileLayout.LEFTINDENT, TileLayout.NOOFFSET,
 *       TileLayout.PROPWIDTH,  TileLayout.FULLHEIGHT,
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
 * <p><i>NB:</i> The JRE <i>draws</i> components from last added
 * to first; but the manager sizes them first to last. Therefore, in order to
 * layout relative to another component, the reference component must be
 * added <i>before</i> the dependent one. This is critically important when
 * components are laid out on top of others, or overlapping. This is a
 * <i>super nice</i> capability of this layout manager.
 * 
 * @author John Catherino
 */
public final class TileLayout implements LayoutManager2, Serializable {
   private static final long serialVersionUID = 1L;
   private static final Dimension NONE = new Dimension();
   private final Vector<Component> components  = new Vector<Component>();
   private final Vector<Object> constraints = new Vector<Object>();
   @SuppressWarnings("null") // fixd is only used when applicable
   private void align(Dimension cont, Component comp, Object... cons) {
      if (!comp.isVisible()) return; // only align visible components
      int align = 0;
      Insets insets = null;
      Rectangle tile = null, fixd = null;
      for (Object con : cons) { // gather constraints
         if (con instanceof Rectangle) fixd = (Rectangle)con;
         else if (con instanceof Insets) insets = (Insets)con;
         else if (con instanceof Integer) align = ((Integer)con).intValue();
         else if (con instanceof Component) tile = ((Component)con).getBounds();
      }
      if (tile == null) tile = new Rectangle(cont);
      Rectangle pref = new Rectangle(tile.getLocation(), comp.getPreferredSize());
      // perform component positioning:
           if ((align & 0x004000) != 0) pref.width = fixd.width;
      else if ((align & 0x008000) != 0) pref.width = (tile.width * fixd.width + 500) / 1000;
      else if ((align & 0x010000) != 0) pref.width = tile.width;
           if ((align & 0x080000) != 0) pref.height = fixd.height;
      else if ((align & 0x100000) != 0) pref.height = (tile.height * fixd.height + 500) / 1000;
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
           if ((align & 0x001000) != 0) pref.setBounds(0, pref.y, pref.x + pref.width, pref.height);
      else if ((align & 0x002000) != 0) pref.width = cont.width - pref.x;
           if ((align & 0x020000) != 0) pref.setBounds(pref.x, 0, pref.width, pref.y + pref.height);
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
   public static final Integer ABOVE =
      NOINDENT + TOPOFFSET + FULLWIDTH + FULLHEIGHT;
   /**
    * A frequently used position, used in conjunction with a reference
    * component, it will locate the component directly beneath, with the same
    * width and height.
    */
   public static final Integer BELOW =
      NOINDENT + BOTTOMOFFSET + FULLWIDTH + FULLHEIGHT;
   /**
    * A frequently used position, used in conjunction with a reference
    * component, it will locate the component directly adjacent, to its right,
    * with the same width and height.
    */
   public static final Integer RIGHT =
      RIGHTINDENT + NOOFFSET + FULLWIDTH + FULLHEIGHT;
   /**
    * A frequently used position, used in conjunction with a reference
    * component, it will locate the component directly adjacent, to its left,
    * with the same width and height.
    */
   public static final Integer LEFT =
      LEFTINDENT + NOOFFSET + FULLWIDTH + FULLHEIGHT;
   /**
    * A frequently used position, it will render use the component using
    * completely proportional bounds to the reference rectangle.
    */
   public static final Integer PROPBOUNDS =
      PROPINDENT + PROPOFFSET + PROPWIDTH + PROPHEIGHT;
   /**
    * A frequently used position, it will render the component using the exact
    * size of the reference component.
    */
   public static final Integer SAMEBOUNDS =
      NOINDENT + NOOFFSET + FULLWIDTH + FULLHEIGHT;
   /**
    * A frequently used position, it will render the component using its own
    * fixed position and size.
    */
   public static final Object FIXEDBOUNDS[] = {
      FIXEDINDENT + FIXEDOFFSET + FIXEDWIDTH + FIXEDHEIGHT
   };
   /**
    * A frequently used position, it will render the component using the full
    * size of the or container.
    */
   public static final Object FULLSIZE[] = {
      NOINDENT + NOOFFSET + FULLWIDTH + FULLHEIGHT
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
    * head of the list for logical convenience when adding, however the JRE,
    * for some reason, <i>draws</i> the components from last to first in the
    * list.
    * @param comp The component to be laid out within the container.
    * @param cons An Object[] containing 1 or more constraints.
    */
   @Override
   public synchronized void addLayoutComponent(Component comp, Object cons) {
      components.insertElementAt(comp, 0);
      constraints.insertElementAt(cons, 0);
   }
   /**
    * This method will make the added component exactly the same size as the
    * container itself. It is useful for adding wallpaper.
    * @param name The name of the component.
    * @param comp The component to fill the container.
    */
   @Override
   @SuppressWarnings("unused") // name is unused in TileLayout
   public void addLayoutComponent(String name, Component comp) {
      addLayoutComponent(comp, FULLSIZE);
   }
   /**
    * This method removes the component from the ordered layout list. It
    * performs nothing, if the component is not in the layout.
    * @param comp The component to remove from the layout
    */
   @Override
   public synchronized void removeLayoutComponent(Component comp) {
      int i = components.indexOf(comp);
      if (i >= 0) {
         components.remove(i);
         constraints.remove(i);
      }
   }
   /**
    * Lays out the components, first added to last added, according to their
    * specified constraints. Remember, the JRE will <i>draw</i> the
    * components from last to first; this is important to consider it they
    * will be overlapping.
    * @param parent The container in which to layout the components
    */
   @Override
   public void layoutContainer(Container parent) {
      Object[] comps, cons;
      synchronized(this) {
         comps = components.toArray();
         cons  = constraints.toArray();
      }
      Dimension container = parent.getSize();
      for (int i = cons.length - 1; i >= 0; i--)
         align(container, (Component)comps[i], (Object[])cons[i]);
   }
   /**
    * Bodyless implementation, as TileLayout uses no cached information.
    * @param target Ignored in this implementation
    */
   @Override
   @SuppressWarnings("unused")
   public void invalidateLayout(Container target) {}
   /**
    * Indicate center x-axis alignment.
    * @param target Ignored in this implementation
    */
   @Override
   @SuppressWarnings("unused")
   public float getLayoutAlignmentX(Container target) { return 0.5F; }
   /**
    * Indicate center y-axis alignment.
    * @param target Ignored in this implementation
    */
   @Override
   @SuppressWarnings("unused")
   public float getLayoutAlignmentY(Container target) { return 0.5F; }
   /**
    * Indicate no minimum size.
    * @param parent Ignored in this implementation
    */
   @Override
   @SuppressWarnings("unused")
   public Dimension minimumLayoutSize(Container parent) { return NONE; }
   /**
    * Indicate no maximum size.
    * @param parent Ignored in this implementation
    */
   @Override
   @SuppressWarnings("unused")
   public Dimension maximumLayoutSize(Container parent) { return NONE; }
   /**
    * Indicate no preferred size.
    * @param parent Ignored in this implementation
    */
   @Override
   @SuppressWarnings("unused")
   public Dimension preferredLayoutSize(Container parent) { return NONE; }
}
