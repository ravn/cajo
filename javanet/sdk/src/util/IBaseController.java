package util;

/*
 * The base interface for a controller for a cajo grail controller.
 * The cajo project: https://cajo.dev.java.net
 * For issues or suggestions mailto:cajo@dev.java.net
 * This interface is released into the public domain.
 * Written by John Catherino
 */

/**
 * This interface defines the canonical functions expected to be furnished
 * by <i>all</i> <a href=https://cajo.dev.java.net>cajo</a> controllers. It
 * is normally subclassed, to define application specific controller function
 * definitions.
 */
public interface IBaseController {
   /**
    * This method is called by clients, to get a standardised html encoded
    * destription of the controller's functions. The format is invariant,
    * to allow automated parsing, if desired.
    * @return A detailed description of the functionality of the controller
    */
   String getDescription();
   /**
    * This method is normally called by a graphical client, to get the view
    * component associated with this controller, to display in its own frame.
    * @return javax.swing.JComponent A graphical component which can then
    * be consolidated into any container for viewing.<br><i><u>NB</u>:</i>
    * the method <i>may</i> return null, if the controller has no view. Whilst
    * permitted, doing this <i>will</i> mess up use by the Applet/WebStart
    * {@link util.Client Client}, which <i>must</i> assume the controller has
    * a default view. Also, a client <i>may</i> call this method more than
    * once, creating multiple asynchronous clients for the given controller
    * instance. As always, if there are any regions of code that are not
    * threadsafe, they will need to be suitably synchronised.
    * @throws java.io.IOException If the necessary UI resource files
    * cannot be found.
    */
   public javax.swing.JComponent getView() throws java.io.IOException;
}
