package cajo.sdk.gui;

import java.awt.Graphics;
import java.awt.Polygon;

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
 * A fun utility component to create <i>old-school</i> seven segment digital
 * displays. Additionally it optonally supports a decimal point, a right-hand
 * colon, and an upper right flag point.
 * @author John Catherino
 */
public class SevenSegment extends cajo.sdk.AbstractView {
   private static final long serialVersionUID = 1L;
   private int dotx, decy, flgy, cl1y, cl2y, dotw, doth;
   private transient Polygon segments[];
   private static final boolean patterns[][] = new boolean[][] {
      { true,  true,  true,  false, true,  true,  true  },
      { false, false, true,  false, false, true,  false },
      { true,  false, true,  true,  true,  false, true  },
      { true,  false, true,  true,  false, true,  true  },
      { false, true,  true,  true,  false, true,  false },
      { true,  true,  false, true,  false, true,  true  },
      { true,  true,  false, true,  true,  true,  true  },
      { true,  false, true,  false, false, true,  false },
      { true,  true,  true,  true,  true,  true,  true  },
      { true,  true,  true,  true,  false, true,  true  },
      { false, false, false, false, false, false, false }
   };
   private void bar(int p1[], int p2[], int a, int b, int width, int thick) {
      p1[0] = a;
      p1[1] = a + thick;
      p1[2] = a + width - thick;
      p1[3] = a + width;
      p1[4] = p1[2];
      p1[5] = p1[1];
      p2[0] = b;
      p2[1] = b - thick;
      p2[2] = p2[1];
      p2[3] = b;
      p2[4] = b + thick;
      p2[5] = p2[4];
   }
   private void calcSegments(int width, int height) {
      if (segments == null) segments = new Polygon[] {
         new Polygon(new int[6], new int[6], 6),
         new Polygon(new int[6], new int[6], 6),
         new Polygon(new int[6], new int[6], 6),
         new Polygon(new int[6], new int[6], 6),
         new Polygon(new int[6], new int[6], 6),
         new Polygon(new int[6], new int[6], 6),
         new Polygon(new int[6], new int[6], 6)
      };
      final int hc = 14, vc = 20;
      dotx = width  * 12 / hc;
      dotw = width  *  2 / hc;
      doth = height *  2 / vc;
      flgy = height *  1 / vc;
      cl1y = height *  5 / vc;
      cl2y = height * 13 / vc;
      decy = height * 17 / vc;
      int thick = height / 30;
      int wide  = width  * 8 / hc - 2;
      int xpos  = width  * 2 / hc + 1;
      bar(segments[0].xpoints, segments[0].ypoints, xpos, height *  2 / vc, wide, thick);
      bar(segments[3].xpoints, segments[3].ypoints, xpos, height * 10 / vc, wide, thick);
      bar(segments[6].xpoints, segments[6].ypoints, xpos, height * 18 / vc, wide, thick);
      int y1 = height * 2 / vc + 1, y2 = height * 10 / vc + 1;
      wide = height * 8 / vc - 2;
      xpos = width  * 2 / hc;
      bar(segments[1].ypoints, segments[1].xpoints, y1, xpos, wide, thick);
      bar(segments[4].ypoints, segments[4].xpoints, y2, xpos, wide, thick);
      xpos = width * 10 / hc;
      bar(segments[2].ypoints, segments[2].xpoints, y1, xpos, wide, thick);
      bar(segments[5].ypoints, segments[5].xpoints, y2, xpos, wide, thick);
   }
   /**
    * The default constructor does nothing.
    */
   public SevenSegment() {}
   /**
    * This overridden routine renders the display at its current value. It
    * will also display the flag, decimal, or colon, if they are active.
    * @param g The graphics context on which to draw the widget
    */
   @Override
   public void paint(Graphics g) {
      if (isOpaque()) {
         g.setColor(getBackground());
         g.fillRect(0, 0, getWidth(), getHeight());
         g.setColor(getForeground());
      }
      boolean pattern[] = patterns[value > -1 && value < 10 ? value : 10];
      for (int ix = 0; ix < 7; ix++)
         if (pattern[ix]) g.fillPolygon(segments[ix]);
      if (flag) g.fillOval(dotx, flgy, dotw, doth);
      if (decimal) g.fillOval(dotx, decy, dotw, doth);
      if (colon) {
         g.fillOval(dotx, cl1y, dotw, doth);
         g.fillOval(dotx, cl2y, dotw, doth);
      }
      super.paint(g);
   }
   /**
    * This will recaculate the segment dimensions, in the event of a component
    * bounds change.
    * @param x The indent of the bounds
    * @param y The offset of the bounds
    * @param width The horizontal spacing of the bounds
    * @param height The vertical spacing of the bounds
    */
   @Override
   public void setBounds(int x, int y, int width, int height) {
      super.setBounds(x, y, width, height);
      calcSegments(width, height);
   }
   /**
    * This is used for simple increment operations. It helps when several
    * seven segment components will be used together.
    * @return True if the component value wrapped, from 9 to 0, false
    * otherwise.
    */
   public boolean increment() {
      if (++value <= 9) return false;
      value = 0;
      return true;
   }
   /**
    * The value of the component, it can range between 0 through 9 inclusively.
    */
   public int value;
   /**
    * Indicates whether do display a decimal point in the lower right corner of
    * the component.
    */
   public boolean decimal;
   /**
    * Indicates whether to display a colon (:) centered on the right side of
    * the component. 
    */
   public boolean colon;
   /**
    * Indicates whether to display a flag, i.e. a decimal point, located on the
    * upper right corner of the component. 
    */
   public boolean flag;
   /**
    * This method provides a means to identify this view.
    * @return An identifier <i>(not a description)</i> of the view
    */
   @Override
   public String toString() { return "SevenSegment"; }
}
