package example.gui;

import java.awt.*;

public final class Display extends Widget {
   private final int rows, columns, limit;
   private final byte[] line, buffer;
   private int ascent, charWidth, charHeight, index;
   public boolean leftAligned, rightAligned, topAligned, bottomAligned;
   public Display(int row, int col) { this(row, col, 12, Font.PLAIN); }
   public Display(int row, int col, int fontSize, int fontStyle) {
      rows = row;
      columns = col;
      line = new byte[columns];
      limit = row * col;
      buffer = new byte[limit];
      clear();
      setFont(new Font("Monospaced", fontStyle, fontSize));
   }
   public void setFont(Font font) {
      super.setFont(font);
      FontMetrics fm = getFontMetrics(font);
      ascent = fm.getAscent() + fm.getLeading();
      charHeight = ascent + fm.getDescent();
      charWidth = fm.charWidth('X');
   }
   public Dimension getPreferredSize() {
      return new Dimension(columns * charWidth + 4, rows * charHeight + 4);
   }
   public void setBounds(int x, int y, int width, int height) {
      x += leftAligned   ? 0 :
           rightAligned  ? width  - columns * charWidth  - 3 :
                          (width  - columns * charWidth)  / 2 - 2;
      y += topAligned    ? 0 :
           bottomAligned ? height - rows    * charHeight - 3 :
                          (height - rows    * charHeight) / 2 - 2;
      super.setBounds(x, y, columns * charWidth + 4, rows * charHeight + 4);
   }
   public void setPosition(int row, int col)  { index  = row * columns + col; }
   public void movePosition(int row, int col) { index += row * columns + col; }
   public Point getPosition() {
      return new Point(index / columns, index % columns);
   }
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
   public void invert(int offset, int len)  {
      for (int ix = 0; ix < len; ix++) buffer[offset++] ^= 0x80;
   }
   public void clearLine(int row) {
      row *= columns;
      buffer[row] = (byte)' ';
      for (int i = 1; i < columns; i += i)
         System.arraycopy(buffer, row, buffer, row + i, ((columns - i) < i) ? (columns - i) : i);
   }
   public void clear() {
      buffer[0] = (byte)' ';
      for (int i = 1; i < limit; i += i)
         System.arraycopy(buffer, 0, buffer, i, ((limit - i) < i) ?
            (limit - i) : i);
   }
   public void scroll() {
      index = limit - columns;
      System.arraycopy(buffer, columns, buffer, 0, index);
      clearLine(rows - 1);
   }
   public void scrollBack() {
      index = 0;
      System.arraycopy(buffer, 0, buffer, columns, limit - columns);
      clearLine(0);
   }
   public void display(char data) { display(data, false); }
   public void display(String string) { display(string, false); }
   public void display(char data[], int offset, int len) { display(data, offset, len, false); }
   public void display(String s, boolean invert) { display(s.toCharArray(), 0, s.length(), invert); }
   public void display(char data[], int offset, int len, boolean invert) {
      for (int ix = 0; ix < len; ix++) display(data[offset + ix], invert);
   }
   public void display(char character, boolean invert) {
      if (index > limit) index = 0;
      switch (character) {
	  case '\033':	// Escape
	  case '\177':	// Delete
	     return;
      case '\f':     // formfeed
         clear();
         setPosition(0, 0);
         break;
      case '\r':     // carriage return
         index -= index % columns;
         break;
      case '\n':	// newline
	     index += columns - index % columns;
         if (index >= limit) scroll();
	     break;
	  case '\t':	// tab
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
	  default:	// all other characters
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
}
