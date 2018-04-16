package example.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import java.io.Serializable;
import java.util.Vector;

// A slightly unorthodox LayoutManager; in addition to laying out components
// relative to the container, it also supports layout relative to components
// within the container.  The goal is to support arbitrarily complex component
// layouts of an unlimited number of components within a single container.

public final class TileLayout implements LayoutManager2, Serializable {
	private static final Dimension NONE = new Dimension();
	private Vector components = new Vector(5, 5);

	private void align(Dimension cont, Object cons[], Component comp) {
		int align = 0;
		Insets insets = null;
		Rectangle tile = null, fixd = null;
		if (cons != null) {
			for (int i = 0; i < cons.length; i++) {
				if (cons[i] != null) {
					if (cons[i] instanceof Rectangle)
						fixd = (Rectangle) cons[i];
					else if (cons[i] instanceof Insets)
						insets = (Insets) cons[i];
					else if (cons[i] instanceof Integer)
						align = ((Integer) cons[i]).intValue();
					else if (cons[i] instanceof Component)
						tile = ((Component) cons[i]).getBounds();
				}
			}
		}
		if (tile == null)
			tile = new Rectangle(cont);
		Rectangle pref = new Rectangle(tile.getLocation(), comp.getPreferredSize());

		if ((align & 0x004000) != 0)
			pref.width = fixd.width;
		else if ((align & 0x008000) != 0)
			pref.width = (tile.width * fixd.width + 500) / 1000;
		else if ((align & 0x010000) != 0)
			pref.width = tile.width;

		if ((align & 0x080000) != 0)
			pref.height = fixd.height;
		else if ((align & 0x100000) != 0)
			pref.height = (tile.height * fixd.height + 500) / 1000;
		else if ((align & 0x200000) != 0)
			pref.height = tile.height;

		if ((align & 0x000001) != 0)
			pref.x -= pref.width;
		else if ((align & 0x000002) != 0)
			pref.x += (tile.width - pref.width >> 1);
		else if ((align & 0x000004) != 0)
			pref.x += tile.width - pref.width;
		else if ((align & 0x000008) != 0)
			pref.x += tile.width;
		else if ((align & 0x000010) != 0)
			pref.x += fixd.x;
		else if ((align & 0x000020) != 0)
			pref.x += (tile.width * fixd.x + 500) / 1000;

		if ((align & 0x000040) != 0)
			pref.y -= pref.height;
		else if ((align & 0x000080) != 0)
			pref.y += (tile.height - pref.height >> 1);
		else if ((align & 0x000100) != 0)
			pref.y += tile.height - pref.height;
		else if ((align & 0x000200) != 0)
			pref.y += tile.height;
		else if ((align & 0x000400) != 0)
			pref.y += fixd.y;
		else if ((align & 0x000800) != 0)
			pref.y += (tile.height * fixd.y + 500) / 1000;

		if ((align & 0x001000) != 0)
			pref.setBounds(tile.x, pref.y, tile.width, pref.height);
		else if ((align & 0x002000) != 0)
			pref.width = tile.width - pref.x;

		if ((align & 0x020000) != 0)
			pref.setBounds(pref.x, tile.y, pref.width, tile.height);
		else if ((align & 0x040000) != 0)
			pref.height = tile.height - pref.y;

		if (insets != null) {
			pref.x += insets.left;
			pref.y += insets.top;
			pref.width -= insets.left + insets.right;
			pref.height -= insets.top + insets.bottom;
		}

		Dimension d = comp.getMinimumSize();
		if (pref.width < d.width)
			pref.width = d.width;
		if (pref.height < d.height)
			pref.height = d.height;

		comp.setBounds(pref);
	}

	public static final int NOINDENT = 0x000000, LEFTINDENT = 0x000001, CENTERINDENT = 0x000002, FULLINDENT = 0x000004,
			RIGHTINDENT = 0x000008, FIXEDINDENT = 0x000010, PROPINDENT = 0x000020,

			NOOFFSET = 0x000000, TOPOFFSET = 0x000040, CENTEROFFSET = 0x000080, FULLOFFSET = 0x000100,
			BOTTOMOFFSET = 0x000200, FIXEDOFFSET = 0x000400, PROPOFFSET = 0x000800,

			PREFWIDTH = 0x000000, CLAMPLEFT = 0x001000, CLAMPRIGHT = 0x002000, FIXEDWIDTH = 0x004000,
			PROPWIDTH = 0x008000, FULLWIDTH = 0x010000,

			PREFHEIGHT = 0x000000, CLAMPTOP = 0x020000, CLAMPBOTTOM = 0x040000, FIXEDHEIGHT = 0x080000,
			PROPHEIGHT = 0x100000, FULLHEIGHT = 0x200000;

	public void invalidateLayout(Container target) {
	}

	public float getLayoutAlignmentX(Container target) {
		return 0.5F;
	}

	public float getLayoutAlignmentY(Container target) {
		return 0.5F;
	}

	public Dimension minimumLayoutSize(Container parent) {
		return NONE;
	}

	public Dimension maximumLayoutSize(Container parent) {
		return NONE;
	}

	public Dimension preferredLayoutSize(Container parent) {
		return NONE;
	}

	public void addLayoutComponent(String name, Component comp) {
	}

	public void addLayoutComponent(Component comp, Object constraints) {
		components.addElement(comp);
		components.addElement(constraints);
	}

	public void removeLayoutComponent(Component comp) {
		for (int i = 0; i < components.size(); i++) {
			if (components.elementAt(i) == comp) {
				components.removeElementAt(i);
				components.removeElementAt(i);
				break;
			}
		}
	}

	public void layoutContainer(Container parent) {
		Dimension container = parent.getSize();
		for (int i = components.size() - 1; i > 0; i--)
			align(container, (Object[]) components.elementAt(i--), (Component) components.elementAt(i));
	}
}
