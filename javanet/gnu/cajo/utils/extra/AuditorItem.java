package gnu.cajo.utils.extra;

import gnu.cajo.invoke.*;

/*
 * Item Auditor
 * Copyright (c) 2004 John Catherino
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
 * To receive a copy of the GNU Lesser General Public License visit their
 * website at http://www.fsf.org/licenses/lgpl.html or via snail mail at Free
 * Software Foundation Inc., 59 Temple Place Suite 330, Boston MA 02111-1307
 * USA
 */

/**
 * This class is used to transparently pre and post-pend audit functionality
 * to any given object reference. It is typically used to check the integrity
 * of the invocation arguments, or the authenticity of the invoking object.
 * Likewise, it can review the invocation results, to augment, or delete
 * certain components. The wrapped object is unaware, and need not be
 * changed, to assist in this functionality. It can be used on local objects,
 * for which the code is available, as easily as on remote object references,
 * for which no code is available. It is ideal for use in debug, and
 * development activities, as well as for security, in production
 * environments. The technique is essentially an implementation of the
 * <i>Decorator</i> design pattern.
 *
 *
 * @version 1.0, 19-Sep-04 Initial release
 * @author John Catherino
 */
public class AuditorItem implements Invoke {
   /**
    * The auditor object. This object's preprocess method will be invoked
    * with the arguments to be provided to the audited object. It has the
    * three options:<ul>
    * <li>Change the arguments
    * <li>Approve the arguments, as is
    * <li>Reject the invocation, by throwing an Exception.</ul>
    * The arguments the preprocess method returns will then be passed on to
    * the audited item for processing. The result of this operation will be
    * passed to the auditing item's postprocess method. Again, it has the
    * similiar three options; to change, approve, or reject the returned
    * data.
    */
   protected final Object auditor;
   /**
    * This is the object to be audited. Since it has no knowlege of the audit
    * it's structure need not be changed in any way to accomodate it.
    */
   protected final Object item;
   /**
    * This creates the object, to audit the target object's use. The class
    * in not declared final, to allow no-arg auditing items to be easily
    * created.
    * @param item The object to receive the client invocation. It can be
    * local, remote, or even a proxy.
    * @param auditor The object to receive the calls prior to, and following
    * the audited item's operation. It can be local, remote, or even a proxy.
    */
   public AuditorItem(Object item, Object auditor) {
      this.item = item;
      this.auditor = auditor;
   }
   /**
    * This method is overridden here to ensure that two different auditor
    * items holding the audited object return the same value.
    * @return The hash code returned by the audited object
    */
   public int hashCode() { return item.hashCode(); }
   /**
    * This method is overridden here to ensure that two different auditor
    * items holding the same audited object return true.
    * @param obj An object, presumably another item, to compare
    * @return True if the audited objects are equivalent, otherwise false
    */
   public boolean equals(Object obj) { return item.equals(obj); }
   /**
    * This method is overridden here to provide the name of the audited
    * object, rather than the name of this auditor item.
    * @return The string returned by the audited object's toString method
    */
   public String toString() { return item.toString(); }
   /**
    * This method audits the incoming calls. If the auditor approves, or
    * change the supplied arguments, they will be passed into the audited
    * item for processing. The auditor will again review the returned data,
    * or exception, and will likewise approve or change it. <i>Note:</i>
    * this method can be called reentrantly. Also, if the audited item
    * invocation results in an exception, and the auditor approves, it can
    * simply return the exception, and the wrapper will throw it
    * automatically.
    * @param method The internal object's public method being called.
    * @param  args The arguments to pass to the internal object's method.
    * @return The sychronous audited data, if any, resulting from the
    * invocation.
    * @throws RemoteException For a network related failure.
    * @throws NoSuchMethodException If the method/agruments signature cannot
    * be matched to the internal object's public method interface.
    * @throws Exception If the internal object's method rejects the
    * invocation, or if it has been rejected by the auditor object.
    */
   public Object invoke(String method, Object args) throws Exception {
      args =
         Remote.invoke(auditor, "preProcess", new Object[] { method, args });
      Object result = null;
      try { result = Remote.invoke(item, method, args); }
      catch(Exception x) { result = x; }
      result = Remote.invoke(auditor, "postProcess", result);
      if (result instanceof Exception) throw (Exception)result;
      return result;
   }
}
