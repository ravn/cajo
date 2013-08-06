package cajo.sdk.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;

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
 * This class is used to display typically monospaced text, in a row x
 * column format.
 * @author John Catherino
 */
public final class Display extends cajo.sdk.AbstractView {
   private static final long serialVersionUID = 1L;
   private final int rows, columns, limit;
   private final byte[] line, buffer;
   private int ascent, charWidth, charHeight, index;
   /**
    * Align the characters with the left of the widget.
    */
   public boolean leftAligned;
   /**
    * Align the characters with the right of the widget.
    */
   public boolean rightAligned;
   /**
    * Align the characters with the top of the widget.
    */
   public boolean topAligned;
   /**
    * Align the characters with the bottom of the widget.
    */
   public boolean bottomAligned;
   /**
    * This constructor creates a display widget of the specified dimensions.
    * @param row The number of columnar rows to support
    * @param col The number of textual rows to support
    */
   public Display(int row, int col) { this(row, col, 12, Font.PLAIN); }
   /**
    * This constructor creates a display widget of the specified dimensions,
    * using a specified font size and style.
    * @param row The number of columnar rows to support
    * @param col The number of textual rows to support
    * @param fontSize The vertical size of the font in pixels.
    * @param fontStyle The visual format of the font to use.
    */
   public Display(int row, int col, int fontSize, int fontStyle) {
      rows = row;
      columns = col;
      line = new byte[columns];
      limit = row * col;
      buffer = new byte[limit];
      clear();
      setFont(new Font("Monospaced", fontStyle, fontSize));
   }
   /**
    * This method is used to re-assign the font used in the rendering of the
    * display.
    * @param font The new font to use
    */
   @Override
   public void setFont(Font font) {
      super.setFont(font);
      FontMetrics fm = getFontMetrics(font);
      ascent = fm.getAscent() + fm.getLeading();
      charHeight = ascent + fm.getDescent();
      charWidth = fm.charWidth('X');
   }
   /**
    * This method is used primarily by the layout manager.
    * @return The minimal dimension that the display could render all of
    * its characters.
    */
   @Override
   public Dimension getPreferredSize() {
      return new Dimension(columns * charWidth + 4, rows * charHeight + 4);
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
      x += leftAligned   ? 0 :
           rightAligned  ? width  - columns * charWidth  - 3 :
                          (width  - columns * charWidth)  / 2 - 2;
      y += topAligned    ? 0 :
           bottomAligned ? height - rows    * charHeight - 3 :
                          (height - rows    * charHeight) / 2 - 2;
      super.setBounds(x, y, columns * charWidth + 4, rows * charHeight + 4);
   }
   /**
    * This method is used to position the custor on the display.
    * @param row The new row position
    * @param col The new column position
    */
   public void setPosition(int row, int col)  { index  = row * columns + col; }
   /**
    * This method is used to reposition the cursor on the display.
    * @param row The new row position
    * @param col The new column position
    */
   public void movePosition(int row, int col) { index += row * columns + col; }
   /**
    * This method is used to obtain the current cusdor position of the
    * display.
    * @return A point object, containing the current indent and offset in
    * character rows and columns
    */
   public Point getPosition() {
      return new Point(index / columns, index % columns);
   }
   /**
    * This method maps a mouse position to a row/column location in the
    * display.
    * @param position The index and offset to check
    * @return A position containing the row and column corresponting to the x
    * and y coordinates
    */
   public Point getPosition(Point position) {
      if (position.x > 2) {
        position.x -= 2;
         position.x /= charWidth;
      } else position.x = 0;
      if (position.y > 2) {
         position.y -= 2;
         position.y /= charHeight;
      } else position.y = 0;
      return position;
   }
   /**
    * This method caused the designated region of the display, to be
    * be presented in reverse video. <i>NB:</i> There can be multiple,
    * dis-contiguous inverted regions. Also, the characters in the display
    * character buffer will not be changed.
    * @param offset The number of characters into the buffer to begin
    * @param len The number of characters to invert.
    */
   public void invert(int offset, int len)  {
      for (int ix = 0; ix < len; ix++) buffer[offset++] ^= 0x80;
   }
   /**
    * This method clears all of the characters on a spedified row. If parts
    * of the region were inverted, they will now be uninverted.
    * @param row The display row to clear of characters
    */
   public void clearLine(int row) {
      row *= columns;
      buffer[row] = (byte)' ';
      for (int i = 1; i < columns; i += i)
         System.arraycopy(buffer, row, buffer, row + i, ((columns - i) < i) ? (columns - i) : i);
   }
   /**
    * This method clears the entire contents of the display buffer.
    */
   public void clear() {
      buffer[0] = (byte)' ';
      for (int i = 1; i < limit; i += i)
         System.arraycopy(buffer, 0, buffer, i, ((limit - i) < i) ?
            (limit - i) : i);
   }
   /**
    * This method vertically scrolls the contents of the display up, leaving
    * a clear line at the bottom.
    */
   public void scroll() {
      index = limit - columns;
      System.arraycopy(buffer, columns, buffer, 0, index);
      clearLine(rows - 1);
   }
   /**
    * This method vertically scrolls the contents of the display down,
    * leaving a clear line at the top.
    */
   public void scrollBack() {
      index = 0;
      System.arraycopy(buffer, 0, buffer, columns, limit - columns);
      clearLine(0);
   }
   /**
    * This method places a character in the display buffer, at the current
    * index.
    * @param data The character to display
    */
   public void display(char data) { display(data, false); }
   /**
    * This method places a sting in the display buffer, at the current
    * index.
    * @param string The string to display
    */
   public void display(String string) { display(string, false); }
   /**
    * This method places a character array in the display buffer.
    * @param data The array of characters to transfer
    * @param offset The index position in the buffer to place the characters
    * @param len The number of characters to copy
    */
   public void display(char data[], int offset, int len) {
      display(data, offset, len, false);
   }
   /**
    * This method places a sting in the display buffer, at the current
    * index.
    * @param string The string to display
    * @param invert True if the test should be displayed in reverse video,
    * otherwise false for normal display
    */
   public void display(String string, boolean invert) {
      display(string.toCharArray(), 0, string.length(), invert);
   }
   /**
    * This method places a sting in the display buffer, at the specified
    * index.
    * @param data The character array to display
    * @param offset The position in the display buffer to place the
    * characters
    * @param len The number of characters to copy
    * @param invert True if the text should be displayed in reverse video,
    * otherwise false for normal display
    */
   public void display(char data[], int offset, int len, boolean invert) {
      for (int ix = 0; ix < len; ix++) display(data[offset + ix], invert);
   }
   /**
    * This method places a character in the display buffer, at the current
    * index.
    * @param character The character to display
    * @param invert True if the character should be displayed in reverse
    * video, otherwise false for normal display
    */
   public void display(char character, boolean invert) {
      if (index > limit) index = 0;
      switch (character) {
         case '\033':  // Escape
         case '\177':  // Delete
         return;
      case '\f':       // formfeed
         clear();
         setPosition(0, 0);
         break;
      case '\r':       // carriage return
         index -= index % columns;
         break;
      case '\n':       // newline
         index += columns - index % columns;
         if (index >= limit) scroll();
         break;
      case '\t':      // tab
         for (int ix = 8 - index % 8; ix > 0; ix--) {
            if (index == limit) scroll();
            buffer[index++] = (byte)(invert ? 0xa0 : 0x20);
         }
         break;
      case '\b':     // backspace
         if (index > 0) {
            buffer[--index] = (byte)(invert ? 0xa0 : 0x20);
            repaint(
               (index % columns) * charWidth  + 2,
               (index / columns) * charHeight + 2, charWidth, charHeight);
         }
         break;
      default:       // all other characters
         if (index == limit) scroll();
         if (invert) character |= 0x80;
         if (buffer[index] != (byte)character) {
            repaint(
               (index % columns) * charWidth  + 2,
               (index / columns) * charHeight + 2, charWidth, charHeight);
            buffer[index++] = (byte)character;
         } else index++;
      }
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
      Color foreground = getForeground(), background = getBackground();
      for (int iy = 0; iy < rows; iy++) {
         for (int ix = 0; ix < columns; ix++) {
            int chpos = iy * columns + ix, ypos = iy * charHeight + 2, len = 0;
            if ((buffer[chpos] & 0x80) != 0) {
               do line[len++] = (byte)(buffer[chpos++] & 0x7f);
               while (ix + len < columns && (buffer[chpos] & 0x80) != 0);
               g.setColor(foreground);
               g.fillRect(ix * charWidth + 2, ypos, len * charWidth, charHeight);
               g.setColor(background);
               g.drawBytes(line, 0, len, ix * charWidth + 2, ypos+ ascent);
            } else {
              do len++;
              while (ix + len < columns && (buffer[chpos + len] & 0x80) == 0);
              g.setColor(foreground);
              g.drawBytes(buffer, chpos, len, ix * charWidth + 2, ypos + ascent);
            }
            ix += len - 1;
         }
      }
   }
   /**
    * This method provides a means to identify this view.
    * @return An identifier <i>(not a description)</i> of the view
    */
   @Override
   public String toString() { return "Display"; }
}
