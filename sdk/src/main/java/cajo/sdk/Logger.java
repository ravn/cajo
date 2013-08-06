package cajo.sdk;

import java.rmi.server.RemoteServer;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.io.ObjectOutputStream;
import gnu.cajo.invoke.Invoke;
import gnu.cajo.invoke.Remote;

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
 * The logger is used to instrument an object for invocation logging
 * purposes. It is intended as a replacement for ordinary or RMI logging, in
 * that this logger is aware of the Invoke package methodology, and can
 * decode it to automatically provide context. Specifically, it will gather
 * information about the calling client, the method called, the inbound and
 * outbound data, and the approximate time used to service the invocation.<p>
 * This type of class is very useful for both clients, and services. Clients
 * can wrap remote service references in a logger, to observe their
 * interaction, and the corresponding results. Services can wrap their
 * local references to observe how clients are using them.
 * <p><i><u>NB</u>:</i> Logging may be activated and deactivated
 * administratively as needed on an instance basis via the field
 * {@link #OFF OFF}, and forcibly enabled on a class-wide basis via the
 * static field {@link #DEBUG DEBUG}.
 * @author John Catherino
 */
public final class Logger implements Invoke {
   private static final long serialVersionUID = 1L;
   private static final String NULL[] = { "();" } ;
   private static final String server = String.format("%s:%s",
      Remote.getDefaultServerHost(), Remote.getDefaultServerPort());
   private ObjectOutputStream oos; // log stream, file, or socket
   /**
    * Every time a method is invoked on the logged object, an Event object
    * will be written to the output stream to describe the invocation. It
    * implements Comparable and Comparator, to allow the collection of events
    * to be sorted with other event objects, by timestamp. The public fields
    * permit straightforward algorithmic analysis of system operation. For
    * this reason, the object is immutable.
    * <p>It contains the following information:<br><ul>
    * <li> The host address and port of the server
    * <li> The hash code of the logger recording this invocation
    * <li> The id of the thread used in the invocation
    * <li> The host address of the caller (or localhost w/method)
    * <li> The logger name for this object
    * <li> The method the caller is invoking
    * <li> The arguments the caller is sending
    * <li> The result of the invocation, or the Exception
    * <li> The absolute timestamp of the invocation
    * <li> The run time of the invocation in microseconds</ul>
    */
   @SuppressWarnings({ "hiding", "synthetic-access" })
   public static final class Event implements Comparable<Event>,
      java.util.Comparator<Event>, java.io.Serializable {
      private static final long serialVersionUID = 1L;
      private Event(Logger logger, String caller, String method,
         String args[], String result, long time, long busy) {
         this.logger  = logger.hashCode();
         this.object  = logger.id;
         this.caller  = caller;
         this.method  = method;
         this.args    = args;
         this.result  = result;
         this.time    = time;
         this.busy    = busy;
      }
      /**
       * The internal machine name or address of the JVM serving the object.
       */
      public final String server = Logger.server;
      /**
       * The hash code of the Logger instance recording this invocation.
       */
      public final int logger;
      /**
       * The identifier of the thread being used in this invocation. For
       * remote client invocations, it is unique only for the duration of the
       * invocation, and <i>will</i> be reused.
       */
      public final long thread = Thread.currentThread().getId();
      /**
       * The machine name or IP address of the calling JVM, or localhost.
       */
      public final String caller;
      /**
       * The identifier of the logged object being invoked.
       */
      public final String object;
      /**
       * The name of the method of the logged object being invoked.
       */
      public final String method;
      /**
       * The names of the arguments provided to the invocation, or "();" if
       * none.
       */
      public final String args[];
      /**
       * The name of the result object of the invocation. It will be "null",
       * if the method either returns null, or is of return type void.
       */
      public final String result;
      /**
       * The absolute timestamp of the invocation, this can be used to
       * compare with the timing of invocations on other objects/machines.
       */
      public final long time;
      /**
       * The time required to complete the invocation, in microseconds.
       */
      public final long busy;
      /**
       * This utility method can allow object events to be sorted by absolute
       * timestamp, to put the events into chronological order.
       * <br><i><u>NB</u>:</i> This natural ordering is <i>not consistent
       * with equals,</i> i.e. two events can occur at the same time, which
       * are <i>not</i> equivalent. (see <a href=http://download.oracle.com/javase/6/docs/api/java/util/Comparator.html>
       * java.util.Comparator</a>)
       * @param e1 An event to be compared for timeliness
       * @param e2 An event to be compared for timeliness
       * @return a negative value, if the first event occurred <i>before</i>
       * the second, zero if they happened at the <i>same time,</i> and a
       * positive value, if the first event occurred <i>after</i> the second
       * -- the value is the number of milliseconds between the events
       */
      @Override
      public int compare(Event e1, Event e2) {
         return (int)(e1.time - e2.time);
      }
      /**
       * This method can allow streams of object events to be joined and
       * sorted by absolute timestamp, putting the events into chronological
       * order.
       * <br><i><u>NB</u>:</i> This natural ordering is <i>not consistent
       * with equals,</i> i.e. two events can occur at the same time, which
       * are <i>not</i> equivalent. (see <a href=http://download.oracle.com/javase/6/docs/api/java/lang/Comparable.html>
       * java.lang.Comparable</a>)
       * @param e The event to be compared for timeliness with this instance
       * @return a negative value, if this event occurred <i>before</i> the
       * specified event, zero if they happened at the <i>same time,</i> and a
       * positive value, if this event occurred <i>after</i> the specified one
       * -- the value is the number of milliseconds between the events
       */
      @Override
      public int compareTo(Event e) { return (int)(time - e.time); }
      /**
       * This method will generate a pretty text representation of the contents
       * of the event object instance.
       * @return The formatted string containing all of the event data, for
       * easy viewing
       */
      @Override
      public String toString() {
         StringBuilder sb = new StringBuilder();
         sb.append("\nserver = ").append(server);
         sb.append("\nlogger = ").append(logger);
         sb.append("\nthread = ").append(thread);
         sb.append("\ncaller = ").append(caller);
         sb.append("\nobject = ").append(object);
         sb.append("\nmethod = ").append(method);
         sb.append("\nargs   = ").append(args[0]);
         for (int i=1; i<args.length; i++) sb.append("\n\t ").append(args[i]);
         sb.append("\nresult = ").append(result);
         sb.append("\ntime   = ").append(time);
         sb.append("\nbusy   = ").append(busy);
         return sb.append(" usec\n").toString();
      }
   }
   /**
    * This flag, if true, will force <i>all</i> loggers to write to their
    * output streams. Normally this will provide much more detail than is
    * needed in normal operation. It is used primarily to diagnose a problem
    * with system operation. It does not affect the instance logging OFF
    * flag.
    */
   public static boolean DEBUG;
   /**
    * This flag can be used to selectively enable and disable logging on an
    * instance basis. By default it is set to false, when true, no output to
    * the logstream will take place. It is subjugate to the {@link #DEBUG
    * DEBUG} flag.
    */
   public boolean OFF;
   /**
    * The object being logged. It is declared as public to allow use of both
    * the reference of the logger, and its wrapped object, from a single
    * reference. The object itself may either be local, remote, or a dynamic
    * proxy, to either a local or remote object.
    */
   public final Object object;
   /**
    * The identification string created for the wrapped object by this logger.
    * It consists of the object's toString : its class name @ its hash code.
    */
   public final String id;
   /**
    * The constructor creates the logger object, to instrument the target
    * object's use. It will send Event objects to the stream for each
    * method invocation on the logged object. The instance of the logger
    * object is used in place of the object reference it is wrapping.
    * <br><i><u>NB</u>:</i> Multiple loggers can use the <i>same</i> stream.
    * @param object The object to receive the client invocation, it can be
    * local, remote, or a dynamic proxy to either a local or remote object
    * @param oos The output stream to write the invocation event objects,
    * if this argument is null, the events will be written to System.out
    * <br><i><u>NB</u>:</i> it is <i>highly</i> recommended to build oos
    * atop a BufferedOutputStream, and if a lot of invocations are being
    * logged, possibly a ZippedOutputStream as well. The stream could be from
    * a socket, if logging to another machine is desired.
    */
   @SuppressWarnings("hiding")
   public Logger(Object object, ObjectOutputStream oos) {
      this.object = object;
      this.oos = oos;
      this.id = String.format("%s:%s@%s", object.toString(),
         object.getClass().getName(), Integer.toHexString(object.hashCode()));
   }
   /**
    * This method is used to periodically change out the log stream object.
    * Normally this is done for long running objects. As mentioned in the
    * constructor docs, multiple loggers can use the same stream.
    * @param oos The output stream to write the invocation event objects
    * <br><i><u>NB</u>:</i> it is <i>highly</i> recommended to build oos
    * atop a BufferedOutputStream, and if a lot of invocations are being
    * logged, possibly a ZippedOutputStream as well. The stream could be from
    * a socket, if logging to another machine is desired.
    * @return The previous log stream, typically for flushing and closing,
    * it can return null however, if it was previously logging to the
    * console
    */
   @SuppressWarnings("hiding")
   public ObjectOutputStream changeStream(ObjectOutputStream oos) {
      try     { return this.oos; }
      finally { this.oos = oos;  }
   }
   /**
    * This method logs the incoming calls, passing the caller's data to the
    * internal object reference. By implementing the invoke interface, all
    * client method invocations are automatically passed to this method. This
    * technique can also be used to intercept, alter arguments, and modify
    * returns, of a wrapped object.<br><br>
    * It will output the following information to the log stream:<br><ul>
    * <li> The host address and port of the server
    * <li> The hash code of the logger recording this invocation
    * <li> The id of the thread used in the invocation
    * <li> The host address of the caller (or localhost w/method)
    * <li> The logger name for this object
    * <li> The method the caller is invoking
    * <li> The arguments the caller is sending
    * <li> The result of the invocation, or the Exception
    * <li> The absolute timestamp of the invocation
    * <li> The run time of the invocation in microseconds</ul>
    * @param method The internal object's public method being called
    * @param  args The arguments to pass to the internal object's method
    * @return The sychronous data, if any, resulting from the invocation
    * @throws RemoteException For a network related failure
    * @throws NoSuchMethodException If the method/agruments signature cannot
    * be matched to the internal object's public method interface
    * @throws Exception If the internal object's method rejects the
    * invocation for application specific reasons
    */
   @Override
   @SuppressWarnings("synthetic-access")
   public Object invoke(String method, Object args) throws Exception {
      if (!DEBUG && OFF) return Remote.invoke(object, method, args);
      Object result;
      long time = System.currentTimeMillis();
      long busy = System.nanoTime();
      try { result = Remote.invoke(object, method, args); }
      catch(Exception x) { result = x; } // an exception is a result
      busy = (System.nanoTime() - busy) / 1000L;
      String caller;
      try { caller = RemoteServer.getClientHost(); }
      catch(ServerNotActiveException x) {
         StackTraceElement
            stes[] = x.getStackTrace(), ste = stes[stes.length - 1];
         caller = String.format("localhost->%s.%s:%s",
            ste.getClassName(), ste.getMethodName(), ste.getLineNumber());
      }
      if (args instanceof Object[]) {
         String temp[] = new String[((Object[])args).length];
         for (int i = 0; i < temp.length; i++) temp[i] =
            ((Object[])args)[i] == null ? "null" :
            ((Object[])args)[i].toString();
         args = temp;
      } else args = args != null ? new String[] { args.toString() } : NULL;
      Event event = new Event(this, caller, method, (String[])args,
         result != null ? result.toString() : "null", time, busy);
      if (oos != null) try { oos.writeObject(event); }
      catch(java.io.IOException x) { oos = null; }
      else System.out.print(event);
      if (result instanceof Exception) throw (Exception)result;
      return result;
   }
   /**
    * This debug utility function can be used to invoke a public method on
    * either a remote, or a local, object reference. The method to be invoked,
    * either instance or static, must be public, but the class implementing
    * the method need not. This is how the cajo project implements <a href=http://en.wikipedia.org/wiki/Dynamic_dispatch>
    * <i>dynamic dispatch,</i></a> as the Java language does not currently
    * support it natively. It will simply dump information about the method
    * invocation to the system console standard output.<br>
    * <i><u>NB</u>:</i> This method only outputs when the Logger static
    * field {@link #DEBUG DEBUG} is set to true.
    * @param <T> The coercable primitive type, class or superclass, of the
    * expected method invocation return, this applies only if the result is
    * being assigned to a variable, it is not specified, rather it is
    * implicit
    * @param object The object reference on which to invoke the method; note
    * that this reference can be to <i>either</i> a remote object, <i>or</i>
    * a local one, typically it is either a service, controller, or agent
    * @param method The method name to be invoked on the provided object
    * reference, this method can be instance or static
    * @param args The arguments required by the method, if any, in order,
    * exact or coercable primitive types, subclasses, and even <i>nulls</i>
    * can be included, this can be omitted if the method takes no arguments
    * @return The result of the method invocation, if any, it can be
    * primitive or an object
    * @throws Exception If the called method rejects the invocation for
    * implementation specific reasons, if the method does not exist or is not
    * public, or if the invocation is on a remote object, for network related
    * failure
    */
   @SuppressWarnings("unchecked")
   public static <T> T invoke(
      Object object, String method, Object... args) throws Exception {
      if (!DEBUG) return (T)Remote.invoke(object, method, args);
      Object result;
      long time = System.currentTimeMillis();
      long busy = System.nanoTime();
      try { result = Remote.invoke(object, method, args); }
      catch(Exception x) { result = x; }
      busy = (System.nanoTime() - busy) / 1000L;
      StringBuilder sb = new StringBuilder();
      sb.append("\nthread = ").append(Thread.currentThread().getId());
      sb.append("\nobject = ").append(String.format("%s:%s@%s",
         object.toString(), object.getClass().getName(),
         Integer.toHexString(object.hashCode())));
      sb.append("\nmethod = ").append(method);
      sb.append("\nargs   = ").append(
         args.length == 0 ? "();" :
         args[0] != null ? args[0].toString() : "null");
      for (int i = 1; i < args.length; i++)
         sb.append("\n\t ").append(
         args[i] != null ? args[i].toString() : "null");
      sb.append("\nresult = ").append(
         result != null ? result.toString()   : "null");
      sb.append("\ntime   = ").append(time);
      sb.append("\nbusy   = ").append(busy); sb.append(" usec\n");
      System.out.print(sb);
      if (result instanceof Exception) throw (Exception)result;
      return (T)result;
   }
}
