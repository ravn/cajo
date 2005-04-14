package gnu.cajo.utils;

import gnu.cajo.invoke.*;
import java.rmi.server.RemoteServer;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.io.PrintStream;
import java.io.OutputStream;
import java.io.ObjectOutputStream;

/*
 * Item Invocation Monitor
 * Copyright (c) 1999 John Catherino
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
 * This class is used to instrument an object for invocation logging
 * purposes. It is intended as a replacement for standard RMI logging, in that
 * this logger is aware of the Invoke package methodology, and can decode it
 * properly.  Specifically, it will gather information about the calling
 * client, the method called, the inbound and outbound data. It will also
 * record the approximate time between client invocations, the time used to
 * service the invocation, and the approximate percentage of free memory
 * available at the completion of the operation.  Subclassing of MonitorItem
 * is allowed; primarily to create self-monitoring classes.
 * <p><i>Note:</i> monitoring an item can be expensive in runtime efficiency.
 * It is best used for debug and performance analysis, during development, or
 * in production, for items that would not be called very frequently.
 *
 * @version 1.0, 01-Nov-99 Initial release
 * @author John Catherino
 */
public class MonitorItem implements Invoke {
   private final Object item;
   private final OutputStream os;
   private long oldtime = System.currentTimeMillis();
   /**
    * This creates the monitor object, to instrument the target object's use.
    * The the logging information is passed to the OutputStream, where it can
    * be logged to a file, a socket, or simply sent to the console (System.out).
    * The logged data is in text format.
    * @param item The object to receive the client invocation.
    * @param os The OutputStream to send the formatted log information.
    */
   public MonitorItem(Object item, OutputStream os) {
      this.item = item;
      this.os = os instanceof PrintStream ? os : new PrintStream(os);
   }
   /**
    * This creates the monitor object, to instrument the target object's use.
    * The the logging information is passed to an ObjectOutputStream.
    * <i>Note:</i> this type of monitoring provides both the greatest detail,
    * and can be most easily manipulated programmatically. However, it is even
    * </i>more</i> expensive than text logging. The log file can become
    * <i>extremely</i> large, if the objects passed in or out are complex, or
    * if the object is called frequently. Therefore, it is <u>highly</u>
    * recommended to implement the ObjectOutputStream on top of a
    * GZipOutputStream.
    * @param item The object to receive the client invocation.
    * @param os The ObjectOutputStream to send input and result objects.
    */
   public MonitorItem(Object item, ObjectOutputStream os) {
      this.item = item;
      this.os = os;
   }
   /**
    * This method is overridden here to ensure that two different monitor
    * items holding the target item return the same value.
    * @return The hash code returned by the target item
    */
   public int hashCode() { return item.hashCode(); }
   /**
    * This method is overridden here to ensure that two different monitor
    * items holding the same target item return true.
    * @param obj An object, presumably another item, to compare
    * @return True if the inner items are equivalent, otherwise false
    */
   public boolean equals(Object obj) { return item.equals(obj); }
   /**
    * This method is overridden here to provide the name of the internal
    * object, rather than the name of the Monitor object.
    * @return The string returned by the internal item's toString method.
    */
   public String toString() { return item.toString(); }
   /**
    * This method logs the incoming calls, passing the caller's data to the
    * internal item. It records the following information:<ul>
    * <li> The name of the item being called
    * <li> The host address of the caller (or localhost)
    * <li> The method the caller is invoking
    * <li> The data the caller is sending
    * <li> The data resulting from the invocation, or the Exception
    * <li> The idle time between invocations, in milliseconds.
    * <li> The run time of the invocation time, in milliseconds
    * <li> The free memory percentage, following the invocation</ul>
    * If the write operation to the log file results in an exception, the
    * stack trace of will be printed to System.err.
    * @param method The internal object's public method being called.
    * @param  args The arguments to pass to the internal object's method.
    * @return The sychronous data, if any, resulting from the invocation.
    * @throws RemoteException For a network related failure.
    * @throws NoSuchMethodException If the method/agruments signature cannot
    * be matched to the internal object's public method interface.
    * @throws Exception If the internal object's method rejects the invocation.
    */
   public Object invoke(String method, Object args) throws Exception {
      long time = System.currentTimeMillis();
      Object result = null;
      try { return result = Remote.invoke(item, method, args); }
      catch(Exception x) {
         result = x;
         throw x;
      } finally {
         int run = (int)(System.currentTimeMillis() - time);
         String clientHost = null;
         try { clientHost = RemoteServer.getClientHost(); }
         catch(ServerNotActiveException x) { clientHost = "localhost"; }
         Runtime rt = Runtime.getRuntime();
         int freeMemory =
            (int)((rt.freeMemory() * 100) / rt.totalMemory());
         ObjectOutputStream oos =
             os instanceof ObjectOutputStream ? (ObjectOutputStream) os : null;
         PrintStream ps = os instanceof PrintStream ? (PrintStream)  os : null;
         synchronized(os) {
            try {
               if (oos != null) {
                  oos.writeObject( new Object[] {
                     clientHost, item.toString(), // may not be serializable!
                     method, args, result, new Long(time - oldtime),
                     new Integer(run), new Integer(freeMemory)
                  });
                  oos.flush(); // just for good measure...
               } else if (ps != null) {
                  ps.print("\nCaller host = ");
                  ps.print(clientHost);
                  ps.print("\nItem called = ");
                  ps.print(item.toString());
                  ps.print("\nMethod call = ");
                  ps.print(method);
                  ps.print("\nMethod args = ");
                  if (args instanceof java.rmi.MarshalledObject)
                     args = ((java.rmi.MarshalledObject)args).get();
                  if (args instanceof Object[]) {
                     ps.print("<array>");
                     for (int i = 0; i < ((Object[])args).length; i++) {
                        ps.print("\n\t[");
                        ps.print(i);
                        ps.print("] =\t");
                        if (((Object[])args)[i] != null)
                           ps.print(((Object[])args)[i].toString());
                        else ps.print("null");
                     }
                  } else ps.print(args != null ? args.toString() : "null");
                  ps.print("\nResult data = ");
                  if (result instanceof java.rmi.MarshalledObject)
                     result = ((java.rmi.MarshalledObject)result).get();
                  if (result instanceof Exception) {
                     ((Exception)result).printStackTrace(ps);
                  } else if (result instanceof Object[]) {
                     ps.print("array");
                     for (int i = 0; i < ((Object[])result).length; i++) {
                        ps.print("\n\t[");
                        ps.print(i);
                        ps.print("] =\t");
                        if (((Object[])result)[i] != null)
                           ps.print(((Object[])result)[i].toString());
                        else ps.print("null");
                     }
                  } else ps.print(result != null ? result.toString() : "null");
                  ps.print("\nIdle time   = ");
                  ps.print(time - oldtime);
                  ps.print(" ms");
                  ps.print("\nBusy time   = ");
                  ps.print(run);
                  ps.print(" ms");
                  ps.print("\nFree memory = ");
                  ps.print(freeMemory);
                  ps.println('%');
               }
            } catch(Exception x) { x.printStackTrace(System.err); }
         }
         oldtime = time;
      }
   }
}
