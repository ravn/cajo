package gnu.cajo.utils.extra;

import java.util.LinkedList;
import gnu.cajo.invoke.Remote;

/*
 * cajo asynchronous object method invocation queue
 * Copyright (C) 2006 John Catherino
 * The cajo project: https://cajo.dev.java.net
 *
 * For issues or suggestions mailto:cajo@dev.java.net
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, at version 2.1 of the license, or any
 * later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You can receive a copy of the GNU Lesser General Public License from their
 * website, http://fsf.org/licenses/lgpl.html; or via snail mail, Free
 * Software Foundation Inc., 51 Franklin Street, Boston MA 02111-1301, USA
 */

/**
 * This class is a cajo-based implementation of the message based
 * communication paradigm. By virtue of returning successfully, a client can
 * be certain its invocation has been enqueued, and will be invoked on the
 * wrapped object. The client can be either local, or remote, the operation
 * is completely asynchronous.<p>
 * A client enqueues its invocations for the server item, by invoking the
 * corresponding method on its reference to an instance of this object. Its
 * argument(s), if any, will be invoked on the matching method of the server
 * item, in a separate thread. This creates a dynamic buffer of invocations.
 * <p><i><u>Note</u>:</i> the wrapped item can be a remote object reference.
 * This can allow one or more separate JVMs, to perform the invocation
 * enqueue process.
 *
 * @version 1.0, 25-Jun-06
 * @author John Catherino
 */
public final class Queue implements gnu.cajo.invoke.Invoke {
   private LinkedList list;
   /**
    * This is the thread performing the dequeue operation, and invoking
    * the corresponding method on the wrapped object. It is instantiated
    * dynamically, upon the first enqueue invocation. If the local JVM wishes
    * to terminate the operation of this queue, it can invoke interrupt() on
    * this field.
    */
   public Thread thread;
   /**
    * This is the object for which invocation queueing is being performed.
    * It can be local to this JVM, or remote.
    */
   public final Object object;
   /**
    * The constructor simply assigns the provided object reference for
    * queued method invocation.
    * @param object The object reference to be wrapped, local or remote.
    */
   public Queue(Object object) { this.object = object; }
   /**
    * This is the method a client, local or remote, would invoke, to be
    * performed in a message-based fashion.
    * @param method The public method name on the wrapped object to be
    * invoked.
    * @param args The argument(s) to invoke on the wrapped item's method.
    * It can be a single object, and Object array of arguments, or null.
    * presumably, the wrapped object has a matching public method signature.
    * @return null Since the operation is performed asynchronously, there can
    * be no synchronous return data. A callback object reference can be
    * provided as an argument, if result data is required. When no arguments
    * are provided, the operation is essentially a semaphore.
    * @throws java.rmi.RemoteException If the invocation failed to enqueue,
    * due to a network related error.
    */
   public synchronized Object invoke(String method, Object args) {
      if (list == null) {
         list = new LinkedList();
         thread = new Thread(new Runnable() {
            public void run() {
               try {
                  synchronized(Queue.this) {
                     while (list.size() == 0) Queue.this.wait();
                  }
                  String method = (String)list.removeFirst();
                  Object args = list.removeFirst();
                  Remote.invoke(object, method, args);
               } catch(InterruptedException x) { return; }
               catch(Exception x) { x.printStackTrace(); }
            }
         });
         thread.start();
      }
      list.add(method);
      list.add(args);
      notify();
      return null;
   }
}
