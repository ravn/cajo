package example.gui;

import java.io.*;
import java.awt.*;
import java.awt.image.ImageObserver;

public final class Wallpaper extends Widget implements ImageObserver {
   private transient Image photo;
   private final boolean tiled;
   private final String file;
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
   private void readObject(ObjectInputStream in)
      throws IOException, ClassNotFoundException {
      in.defaultReadObject();
      if (file != null) try {
         InputStream is = getClass().getResourceAsStream(file);
         Toolkit tk = getToolkit();
         is = new BufferedInputStream(is);
         byte bytes[] = new byte[is.available()];
         is.read(bytes);
         photo = tk.createImage(bytes);
         tk.prepareImage(photo, -1, -1, this);
         is.close();
      } catch(IOException x) { x.printStackTrace(System.err); }
   }
   public Wallpaper(String file, boolean tiled) throws IOException {
      this.file  = file;
      this.tiled = tiled;
      if (file != null)  {
         InputStream is =
            new BufferedInputStream(getClass().getResourceAsStream(file));
         byte bytes[] = new byte[is.available()];
         is.read(bytes);
         is.close();
         Toolkit tk = getToolkit();
         photo = tk.createImage(bytes);
         tk.prepareImage(photo, -1, -1, this);
      }
   }
   public Dimension getPreferredSize() {
      return tiled ? getSize() : new Dimension(iw, ih);
   }
   public void setBounds(int x, int y, int width, int height) {
      super.setBounds(x, y, width, height);
      if (!tiled && iw > 0) calcOffsets();
   }
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
   public void paint(Graphics g) {
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
      super.paint(g);
   }
}
