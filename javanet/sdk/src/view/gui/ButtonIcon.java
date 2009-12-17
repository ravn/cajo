package view.gui;

import java.awt.*;
import java.io.Serializable;
import javax.swing.*;

/*
 * Abstract Button Renderer
 * The cajo project: https://cajo.dev.java.net
 * For issues or suggestions mailto:cajo@dev.java.net
 * This class is released into the public domain.
 * Written by John Catherino 01-Dec-09
 */

/**
 * This is the base class of a self-drawing button.
 * @author <a href=http://wiki.java.net/bin/view/People/JohnCatherino>
 * John Catherino</a>
 */
public abstract class ButtonIcon implements Icon, Serializable {
   private final ButtonModel model;
   /**
    * This field will be returned as the width of the icon. By default, it is
    * unset, and therefore zero.
    */
   protected int width;
   /**
    * This field will be returned as the height of the icon. By default, it is
    * unset, and therefore zero.
    */
   protected int height;
   /**
    * The constructor extracts the ButtonModel, and sets itself as the
    * button's renderer.
    * @param button The Swing button seeking supplemental drawing.
    */
   protected ButtonIcon(AbstractButton button) {
      model = button.getModel();
      setIcons(button, this, this, this, this, this);
   }
   /**
    * Provides the desired width, set by the subclass, otherwise zero.
    * @return The value of the width field
    */
   public final int getIconWidth()  { return width;  }
   /**
    * Provides the desired height, set by the subclass, otherwise zero.
    * @return The value of the height field
    */
   public final int getIconHeight() { return height; }
   /**
    * This will render the button. It will redirect to the other abstract
    * paint methods, as required by the underlyihng ButtonModel's state. 
    */
   public final void paintIcon(Component c, Graphics g, int x, int y) {
      if (model.isPressed() && model.isArmed()) paintPressed(c, g, x, y);
      else if (model.isRollover()) paintRollover(c, g, x, y);
      else if (model.isSelected()) paintSelected(c, g, x, y);
      else if (!model.isEnabled()) paintDisabled(c, g, x, y);
      else paintEnabled(c, g, x, y);
   }
   /**
    * This utility method assigns icons to button states, in the same manner
    * as the class' would have drawn them.
    * @param button The button to be decorated
    * @param enabled  The icon to be displayed when the button is enabled.
    * @param pressed  The icon to be displayed when the button is pressed.
    * @param rollover The icon to be displayed when the button is rolled over.
    * @param disabled The icon to be displayed when the button is disabled.
    * @param selected The icon to be displayed when the button is selected.
    * @return The augmented button, to allow this method to be used inline.
    */
   public static final AbstractButton setIcons(AbstractButton button,
      Icon enabled, Icon pressed, Icon rollover, Icon disabled, Icon selected) {
      button.setBorderPainted(false);
      button.setContentAreaFilled(false);
      button.setIcon(enabled);
      button.setDisabledIcon(disabled);
      button.setPressedIcon(pressed);
      button.setRolloverIcon(rollover);
      button.setSelectedIcon(selected);
      return button;
   }
   /**
    * This method is implemented to draw the button icon when it is enabled.
    * @param c The button in which to render the icon. 
    * @param g The graphics context in which to draw the icon.
    * @param x The index into the component where to begin the drawing.
    * @param y The offset into the component where to begin the drawing.
    */
   public abstract void paintEnabled(Component c,  Graphics g, int x, int y);
   /**
    * This method is implemented to draw the button icon when it is disabled.
    * @param c The button in which to render the icon. 
    * @param g The graphics context in which to draw the icon.
    * @param x The index into the component where to begin the drawing.
    * @param y The offset into the component where to begin the drawing.
    */
   public abstract void paintDisabled(Component c, Graphics g, int x, int y);
   /**
    * This method is implemented to draw the button icon when it is pressed.
    * @param c The button in which to render the icon. 
    * @param g The graphics context in which to draw the icon.
    * @param x The index into the component where to begin the drawing.
    * @param y The offset into the component where to begin the drawing.
    */
   public abstract void paintPressed(Component c,  Graphics g, int x, int y);
   /**
    * This method is implemented to draw the button icon when it is rolled
    * over by the mouse.
    * @param c The button in which to render the icon. 
    * @param g The graphics context in which to draw the icon.
    * @param x The index into the component where to begin the drawing.
    * @param y The offset into the component where to begin the drawing.
    */
   public abstract void paintRollover(Component c, Graphics g, int x, int y);
   /**
    * This method is implemented to draw the button icon when it is selected.
    * @param c The button in which to render the icon. 
    * @param g The graphics context in which to draw the icon.
    * @param x The index into the component where to begin the drawing.
    * @param y The offset into the component where to begin the drawing.
    */
   public abstract void paintSelected(Component c, Graphics g, int x, int y);
}
