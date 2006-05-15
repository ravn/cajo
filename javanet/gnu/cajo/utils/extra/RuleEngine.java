package gnu.cajo.utils.extra;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.io.Serializable;
import gnu.cajo.invoke.Remote;
import java.rmi.RemoteException;

/*
 * cajo object oriented rule engine
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
 * This class is a cajo-based reformulation of rule-based reasoning systems,
 * for declarative programming. This engine does not suffer from the large
 * memory requirements associated with the Rete Algorithm, as it offloads
 * each rule's state and inference to distributed JVMs. The engine is also
 * serialisable; to allow it to scale, as well as providing both modularity,
 * and redundancy.<p>
 * The application of fuzzy facts, and fuzzy predicates is not only easily
 * supported, but is in fact <i>highly</i> recommended.<p>
 * <i><u>Note</u>:</i> to compile this class, it is best to set the java
 * compiler's <tt>-source</tt> switch to <tt>1.2</tt>.
 *
 * @version 1.0, 11-May-06
 * @author John Catherino
 */
public class RuleEngine implements Serializable {
   /**
    * This class is an abstract base for building rules. It minimises load
    * on the rule engine, and properly manages asynchronous change without
    * synchronisation, or deadlock problems. It is highly recommended to
    * extend this class for your rules.
    */
   public static abstract class Rule implements Serializable, Runnable {
      /**
       * The rule specific local fact base on which to infer.
       */
      protected Hashtable facts = new Hashtable();
      /**
       * This flag indicates that asynchronous modification has been
       * made to the fact base. <i><u>Very Important</u>:</i>
       * The <i>first</i> instruction of the infer method should be to
       * set it false.
       */
      protected boolean change;
      /**
       * The local reasoning thread. It is lazily instantiated, and can
       * be shut down by calling its interrupt method, if necessary.
       * It's job is to call the infer method whenever there is a
       * change (addidion, removal, modification) to the local fact
       * base.
       */
      protected transient Thread thread;
      /**
       * This method is completely rule-specific. It is called when there
       * has been a change to the local fact base. It should first
       * reset the change flag, then evaluate its state to make decisions.
       * It is possible for the fact base to be actively changing,
       * while this method is executing. This can be detected by monitoring
       * the state of the change flag.
       */
      protected abstract void infer();
      /**
       * This method is called when a new fact is being posited. It can
       * approve the change by simply returning. It can also throw an
       * Exception of arbitrary type, which will abort the change.
       * By default, it simply returns.
       * @param oldFact The fact that is proposed for change.
       * @param newFact The fact that will take its place.
       * @param keys The path of candidate keys, to provide a semantic
       * context for the facts, as needed.
       * @throws Exception For any reason, as decided by subclassses.
       * The base class never throws an Exception.
       */
      public void positOK(Object oldFact, Object newFact, Object keys[])
         throws Exception {}
      /**
       * This method is invoked by the rule engine to make a change,
       * addition or modification, to the local fact base. It will
       * notify the local processing thread, creating it if necessary, and
       * return. Its run time is designed to be as short as possible, and
       * should not be overridden, unless it is absolutely critical.
       * @throws Exception An arbitrary exception may be thrown by the
       * positOk method of this class, if it does not want the fact to
       * be changed.
       */
      public synchronized void posit(Object fact, Object keys[])
         throws Exception {
         Object element = facts.get(keys[0]);
         for (int i = 1; i < keys.length; i++) {
            Object temp = ((Hashtable)element).get(keys[i]);
            if (temp == null) { // if no path, make one!
               if (i < keys.length - 1) {
                  temp = new Hashtable();
                  ((Hashtable)element).put(keys[i], temp);
               }
            }
            if (i < keys.length -1) element = temp;
         }
         Object oldFact = ((Hashtable)element).get(keys[keys.length - 1]);
         positOK(oldFact, fact, keys);
         ((Hashtable)element).put(keys[keys.length - 1], fact);
         change = true;
         if (thread == null) {
            thread = new Thread(this);
            thread.start();
         } else notify();
      }
      /**
       * This method is called when a fact is being rescinded. It can
       * approve the change by simply returning. It can also throw an
       * Exception of arbitrary type, which will abort the change.
       * By default, it simply returns.
       * @param oldFact The fact that is proposed for retraction.
       * @param keys The path of candidate keys, to provide a semantic
       * context for the fact, as needed.
       * @throws Exception For any reason, as decided by subclassses.
       * The base class never throws an Exception.
       */
      public void retractOK(Object oldFact, Object keys[])
         throws Exception {}
      /**
       * This method is invoked by the rule engine to make a deletion
       * from the local fact base. It will notify the local processing
       * thread, creating it if necessary, and return. Its run time is
       * designed to be as short as possible, and should not be overridden,
       * unless it is absolutely critical.
       * @throws Exception An arbitrary exception may be thrown by the
       * retractOk method of this class, if it does not want the fact to
       * be rescinded.
       */
      public synchronized void retract(Object keys[]) throws Exception {
         Object element = facts.get(keys[0]);
         for (int i = 1; i < keys.length; i++) {
            Object temp = ((Hashtable)element).get(keys[i]);
            if (temp == null) { // if no path, make one!
               if (i < keys.length - 1) {
                  temp = new Hashtable();
                  ((Hashtable)element).put(keys[i], temp);
               }
            }
            if (i < keys.length -1) element = temp;
         }
         Object oldFact = ((Hashtable)element).get(keys[keys.length - 1]);
         retractOK(oldFact, keys);
         ((Hashtable)element).remove(keys[keys.length - 1]);
         change = true;
         if (thread == null) {
            thread = new Thread(this);
            thread.start();
         } else notify();
      }
      /**
       * This is the local change processing thread. It waits until there
       * has been a change to the local fact base, and invokes the
       * infer method. It is used to offload the reasoning processing
       * from the change invocation thread. It is created automatically,
       * on the first change to this rule.
       */
      public void run() {
         try {
            while (true) {
               infer();
               synchronized(this) { while(!change) wait(); }
            }
         } catch(InterruptedException x) {}
      }
   }
   /**
    * This field represents the current state of all facts in the fact
    * base. The ability to clear the fact base is not provided, however it
    * can be offered by a subclass of this engine, if needed. Facts are
    * generally assumed to be local data objects, but they can in fact be
    * references, to objects in remote JVMs.
    */
   protected final Hashtable memory = new Hashtable();
   /**
    * This field represents the set of rules registered in the engine's
    * rule base. The rules are organised by the <i>types</i> of facts for
    * which they are registered. The ability to clear the fact base is not
    * provided, however it can be offered by a subclass of this engine, if
    * needed. Rules are generally assumed to be references to objects in
    * remote JVMs, but that can be local object references, if necessary.
    * Multiple rules can be assigned to the same fact, just as the same
    * rule can be assigned to multiple facts.
    */
   protected final Hashtable rules = new Hashtable();
   /**
    * This utility method returns all registered rules, for the given key
    * path.
    * @param keys The array of candidate keys leading to the list of
    * rules.
    * @return An array of references to the rule objects. Typically these
    * are used for notification of a rule addition, modification, or deletion.
    * @throws ClassCastException if the provided key path is invalid, or if
    * the last node is not a rule collection.
    */
   protected Object[] rules(Object keys[]) {
      Object element = rules.get(keys[0]);
      for (int i = 1; i < keys.length; i++)
         element = ((Hashtable)element).get(keys[i]);
      return ((Vector)element).toArray();
   }
   /**
    * This constructor performs no function, as the operation of the rule
    * engine is completely runtime dependent.
    */
   public RuleEngine() {}
   /**
    * This method is used to assert, or modify, a fact in the rule engine
    * fact base. If no fact of this type has been already registered
    * with this engine, the fact will be added automatically.<p>
    * The engine will extract from its rule base, all rules registered for
    * notification about the particular type of fact. Each rule will be
    * notified serially, by invoking its public <tt>posit</tt> method,
    * with the fact object. If a rule instance determines all of its
    * criteria necessary for its firing has been met, then it is the
    * responsibility of that rule to carry it out.<p>
    * Any rule is capable of aborting this change, by throwing an exception.
    * <i><u>Note</u>:</i> Rule <tt>posit</tt> invocations are expected to
    * return as quickly as possible to ensure maximum performance of the
    * engine. Typically rule objects will save the fact argument in a queue,
    * notify a waiting local thread, and return. A rule generally will
    * register for multiple fact types, therefore its posit method most
    * likely <i>will</i> be invoked reentrantly.
    * @param fact The fact that is being added or updated to the fact
    * base. The fact is a collection of data logically related to some
    * facet of the system. It is must be a local object. To minimise traffic,
    * facts can also be sent in an Object array.
    * @param keys The chain of keys leading to the element of interest
    * in the fact base.
    * @throws NullPointerException if either the fact, or any of the keys
    * are null.
    */
   public Object posit(Object fact, Object keys[]) throws Exception {
      Object element = memory.get(keys[0]);
      for (int i = 1; i < keys.length; i++) {
         Object temp = ((Hashtable)element).get(keys[i]);
         if (temp == null) { // if no path, make one!
            if (i < keys.length - 1) {
               temp = new Hashtable();
               ((Hashtable)element).put(keys[i], temp);
            }
         }
         if (i < keys.length -1) element = temp;
      }
      Object rulez[] = rules(keys);
      Object oldFact = ((Hashtable)element).get(keys[keys.length - 1]);
      for (int i = 0; i < rulez.length; i++)
         try {
            Remote.invoke(
               rulez[i], "positOK", new Object[] { oldFact, fact, keys });
         } catch(RemoteException x) {}
      for (int i = 0; i < rulez.length; i++)
         try {
            Remote.invoke(rulez[i], "posit", new Object[] { fact, keys });
         } catch(RemoteException x) {}
      return ((Hashtable)element).put(keys[keys.length - 1], fact);
   }
   /**
    * This method is used to rescind a fact in the rule engine fact
    * base. The engine will extract from its rule base, all rules registered
    * for notification about the particular type of fact, and all
    * superclasses of the fact. Each will be notified serially, by invoking
    * its public <tt>retract</tt> method, with the class of the fact being
    * retracted.<p>
    * <i><u>Note</u>:</i> Rule retract invocations are expected to return as
    * quickly as possible, to ensure maximum performance of the engine.
    * Typically rule objects will save the fact class in a queue, and notify
    * a waiting local thread, and return. A rule generally will register for
    * multiple fact types, therefore its retract method most likely
    * <i>will</i> be invoked reentrantly.
    * @param keys The chain of keys leading to the element of interest
    * in the fact base.
    * @return An instance of the fact that was removed from the fact
    * base, or an array of removed facts.
    * @throws ClassCastException if the path to the fact does not exist.
    */
   public Object retract(Object keys[]) throws Exception {
      Object element = memory.get(keys[0]);
      for (int i = 1; i < keys.length - 1; i++)
         element = ((Hashtable)element).get(keys[i]);
      Object rulez[] = rules(keys);
      Object oldFact = ((Hashtable)element).get(keys[keys.length - 1]);
      for (int i = 0; i < rulez.length; i++)
         try {
            Remote.invoke(
               rulez[i], "retractOK", new Object[] { oldFact, keys });
         } catch(RemoteException x) {}
      for (int i = 0; i < rulez.length; i++)
         try { Remote.invoke(rulez[i], "retract", keys); }
         catch(RemoteException x) {}
      return ((Hashtable)element).remove(keys[keys.length - 1]);
   }
   /**
    * This method is used to request the current state of a fact in the
    * fact base.
    * @param keys The chain of keys leading to the element of interest
    * in the fact base.
    * @return An instance of the fact that was obtained from the fact
    * base, if any. It can be either an object, or a Hashtable.
    * @throws ClassCastException if the path to the fact does not exist.
    */
   public Object query(Object keys[]) {
      Object element = memory.get(keys[0]);
      for (int i = 1; i < keys.length; i++)
         element = ((Hashtable)element).get(keys[i]);
      return element;
   }
   /**
    * This method is used to register a rule instance for a type of 
    * fact. The rule engine will store the rule reference, to have either
    * its <tt>posit</tt> or its <tt>retract</tt> methods invoked, as the
    * state of the fact of interest changes. It is worth mentioning, a
    * single rule instance is often used to monitor multiple types of facts.
    * @param rule The rule to be notified when the fact state changes. It is
    * typically a reference to an object in a remote JVM, but it can be to
    * a local object, or even a proxy object.
    * @param keys The chain of keys leading to the element of interest
    * in the fact base.
    * @return The rule overridden, if any.
    */
   public Object add(Object rule, Object keys[]) {
      Object element = rules.get(keys[0]);
      for (int i = 1; i < keys.length; i++) {
         Object temp = ((Hashtable)element).get(keys[i]);
         if (temp == null) { // if no path, make one!
            if (i < keys.length - 1) {
               temp = new Hashtable();
               ((Hashtable)element).put(keys[i], temp);
            }
         }
         element = temp;
      }
      Vector v = (Vector)((Hashtable)element).get(keys[keys.length - 1]);
      v.add(rule);
      return query(keys);
   }
   /**
    * This method is used to unregister a rule instance for a type of 
    * fact. The rule engine will delete the rule reference from its
    * fact base.
    * @param rule The rule to be removed from the engine. It is typically a
    * reference to an object in a remote JVM, but it can be to a local
    * object, or even a proxy object.
    * @param keys The chain of keys leading to the element of interest
    * in the fact base.
    */
   public void remove(Object rule, Object keys[]) {
      Object element = rules.get(keys[0]);
      for (int i = 1; i < keys.length - 1; i++)
         element = ((Hashtable)element).get(keys[i]);
      Vector v = (Vector)((Hashtable)element).get(keys[keys.length - 1]);
      v.remove(rule);
   }
}
