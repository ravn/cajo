package gnu.cajo.invoke;

/*
 * Generic Polymorphic Inter-VM Item Communication Interface
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
 * The Remote Component Communication Interface, and reason for this package.
 * An empty extension of the Invoke interface, it allows both local, and
 * remote items, i.e. those from another VM, to be handled interchangably in
 * code, through their superclass interface Invoke. When a VM wishes to allow
 * remote access to an item, the local item would be passed to the constructor
 * of the {@link Remote Remote} class included in this package. <p>The
 * implementation is so trivial, it is included it here:<p>
 * <code>public interface RemoteInvoke extends Invoke, Remote {}</code><p>
 * <i>Note:</i> this interface is nevere implemented by classes directly,
 * rather, a client only uses this interface to test if an item is remote, in
 * cases where that would be of interest to the application.<p> To test the
 * locality of an item reference:<p>
 * <pre>
 * if (foo instanceof RemoteInvoke) { // the item is remote
 *    ...
 * } else { // the item is local
 *   ...
 * }
 * </pre>
 *
 * @version 1.0, 01-Nov-99
 * @author John Catherino  Initial release
 */

public interface RemoteInvoke extends Invoke, java.rmi.Remote {}
