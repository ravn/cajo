package example.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

public class Widget extends Component {
	private Rectangle clip;
	public boolean border, opaque;

	public Dimension getPreferredSize() {
		return getSize();
	}

	public final void update(Graphics g) {
		if (opaque) {
			clip = clip == null ? g.getClipBounds() : g.getClipBounds(clip);
			g.clearRect(clip.x, clip.y, clip.width, clip.height);
		}
		paint(g);
	}

	public void paint(Graphics g) {
		if (border)
			g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
	}
}
