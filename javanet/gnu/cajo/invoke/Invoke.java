package gnu.cajo.invoke;

/*
 * Generic Polymorphic Item Communication Interface
 *
 * For issues or suggestions mailto:cajo@dev.java.net
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, at version 2.1 of the license, or any
 * later version.  The license differs from the GNU General Public License
 * (GPL) to allow this library to be used in proprietary applications. The
 * standard GPL would forbid this.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * To receive a copy of the GNU General Public License visit their website at
 * http://www.gnu.org or via snail mail at Free Software Foundation Inc.,
 * 59 Temple Place Suite 330, Boston MA 02111-1307 USA
 */

/**
 * The generic inter-component communication interface, and foundation for
 * this paradigm. This provides a standard communication interface between
 * objects, referred to in this package as <b>items</b>.  It is used to pass
 * arguments to, and recieve synchronous responses from, the receiving item.
 * <p>The implementation is so <i>extrmely simple</i>, it's included here:
 * <p><pre>
 * public interface Invoke extends Serializable {
 *   Object invoke(String method, Object args[]) throws Exception;
 * }</pre><p>
 *
 * @version 1.0, 01-Nov-99  Initial release
 * @author John Catherino
 */
public interface Invoke extends java.io.Serializable {
   /**
    * Used by other objects to pass data into this item, and receive
    * synchronous data responses from it, if any. The invocation may, or may
    * not contain inbound data.  It may, or may not, return a data object
    * response.  The functionality of this method is completely application
    * specific. This interface serves only to define the format of
    * communication between items.<p>
    * <i>Note:</i> this method could be called reentrantly, by many objects,
    * simultaneously.  If this would cause a problem, the critical sections of
    * this item's method must be synchronized. In general, synchronizing
    * the whole method is <i>strongly</i> discouraged, as it could block
    * multiple clients too easily.<p>
    * @param  method A key to the meaning of the invocation, possibly even
    * null.
    * @param  args The data relevant to the operation. It can be a single
    * object, an array, or even null.
    * @return Any synchronous data defined by a subclass' implementation,
    * it can be an array of of objects, possibly even null
    * @throws Exception As needed by the application. <i>Note:</i>  subclasses
    * of Exception can be thrown, to allow client items the opportunity to
    * catch only specific types, these exceptions could also contain
    * application specific methods, and fields, to supplement the error
    * information.  If an item does not throw any exceptions, it would be
    * preferable to simply omit the throws clause entirely, in the subclass'
    * method declaration.
    */
   Object invoke(String method, Object args) throws Exception;
}
