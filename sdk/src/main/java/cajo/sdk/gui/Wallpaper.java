package cajo.sdk.gui;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;

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
 * This class is used to display a PNG, JPEG, or GIF image. It can also be
 * used to tile or stretch the image, across the available space.
 * @author John Catherino
 */
public final class Wallpaper extends cajo.sdk.AbstractView {
   private static final long serialVersionUID = 1L;
   private final Image photo;
   private final boolean tiled;
   private int iw, ih, px, py, pw, ph;
   private void calcOffsets() {
      int height = getHeight(), width = getWidth();
      if (ih > iw) {
         pw = (iw * height + (ih >> 1)) / ih;
         if (pw > width) {
            ph = (ih * width + (iw >> 1)) / iw;
            pw = width;
            px = 0;
            py = ((height - ph) >> 1);
         } else {
            ph = height;
            px = ((width - pw) >> 1);
            py = 0;
         }
      } else {
         ph = (ih * width + (iw >> 1)) / iw;
         if (ph > height) {
            pw = (iw * height + (ih >> 1)) / ih;
            ph = height;
            px = ((width - pw) >> 1);
            py = 0;
         } else {
            pw = width;
            px = 0;
            py = ((height - ph) >> 1);
         }
      }
   }
   /**
    * The constructor generates a widget representing the bitmapped image,
    * which can then be placed and manipulated in the display.
    * @param file The name of the file containing the image, the supported
    * file types are JPEG and GIF.
    * @param tiled If the image is to be replicated across the available
    * display area
    * @throws IOException If the image resource cannot be found, or loaded
    */
   @SuppressWarnings("hiding") // overwriting tiled field
   public Wallpaper(String file, boolean tiled) throws IOException {
      InputStream is =
         new BufferedInputStream(getClass().getResourceAsStream(file));
      byte bytes[] = new byte[is.available()];
      is.read(bytes);
      is.close();
      Toolkit tk = getToolkit();
      photo = tk.createImage(bytes);
      this.tiled = tiled;
      tk.prepareImage(photo, -1, -1, this);
   }
   /**
    * This method is used primarily by the layout manager.
    * @return The minimal dimension that the widget could fully render its
    * image.
    */
   @Override
   public Dimension getPreferredSize() {
      return tiled ? getSize() : new Dimension(iw, ih);
   }
   /**
    * This method is used primarily by the layout manager.
    * @param x The indent, in pixels, on the display device
    * @param y The offset, in pixels, on the display device
    * @param width The horizontal space, in pixels, on the display device
    * @param height The vertical space, in pixels, on the display device
    */
   @Override
   public void setBounds(int x, int y, int width, int height) {
      super.setBounds(x, y, width, height);
      if (!tiled && iw > 0) calcOffsets();
   }
   /**
    * This method is called primarily by the display runtime. It is used to
    * support animated GIFs.
    * @param i The image to be updated
    * @param f The flags related to the image state
    * @param x The offset in pixels of the image
    * @param y The indent in pixels of the image,
    * @param w The width in pixels of the image,
    * @param h The height in pixels of the image,
    */
   @Override
   @SuppressWarnings("unused") // only the arguments f, w and h are used
   public boolean imageUpdate(Image i, int f, int x, int y, int w, int h) {
      if (iw == 0 && (f & WIDTH + HEIGHT) == WIDTH + HEIGHT) {
         iw = w;
         ih = h;
         calcOffsets();
      }
      if (isShowing() && (f & (ALLBITS | FRAMEBITS)) != 0) repaint();
      return isShowing();
   }
   /**
    * This method is called by the display runtime, to render the widget
    * on the display device.
    * @param g The graphics context, on which to draw the display and its
    * contents
    */
   @Override
   public void paint(Graphics g) {
      super.paint(g);
      if (photo != null) {
         if (tiled) {
            int height = getHeight(), width = getWidth();
            for (int wc, xpos = 0; xpos < width; xpos += iw) {
               wc = xpos + iw < width ? iw : width - xpos;
               for (int hc, ypos = 0; ypos < height; ypos += ih) {
                  hc = ypos + ih < height ? ih : height - ypos;
                  g.drawImage(photo, xpos, ypos, xpos + wc, ypos + hc,
                     0, 0, wc, hc, this);
               }
            }
         } else g.drawImage(photo, px, py, pw, ph, this);
      }
   }
   /**
    * This method provides a means to identify this view.
    * @return An identifier <i>(not a description)</i> of the view
    */
   @Override
   public String toString() { return "WallPaper"; }
}
