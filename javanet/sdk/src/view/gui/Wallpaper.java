package view.gui;

import java.io.*;
import java.awt.*;
import java.awt.image.ImageObserver;

/*
 * Bitmap image display control
 * The cajo project: https://cajo.dev.java.net
 * For issues or suggestions mailto:cajo@dev.java.net
 * This class is released into the public domain.
 * Written by John Catherino 01-Dec-09
 */

/**
 * This class is used to display a JPEG or GIF image. It can also be used
 * to tile or stretch the image, across the available space.
 * @author <a href=http://wiki.java.net/bin/view/People/JohnCatherino>
 * John Catherino</a>
 */
public final class Wallpaper extends Widget implements ImageObserver {
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
    */
   public Wallpaper(String file, boolean tiled) throws IOException {
      InputStream is =
         new BufferedInputStream(getClass().getResourceAsStream(file));
      byte bytes[] = new byte[is.available()];
      is.read(bytes);
      is.close();
      Toolkit tk = getToolkit();
      this.photo = tk.createImage(bytes);
      this.tiled = tiled;
      tk.prepareImage(photo, -1, -1, this);
   }
   /**
    * This method is used primarily by the layout manager.
    * @return The minimal dimension that the widget could fully render its
    * image.
    */
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
   public boolean imageUpdate(Image i, int f, int x, int y, int w, int h) {
      if (iw == 0 && (f & ImageObserver.WIDTH + ImageObserver.HEIGHT) ==
         ImageObserver.WIDTH + ImageObserver.HEIGHT) {
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
}
