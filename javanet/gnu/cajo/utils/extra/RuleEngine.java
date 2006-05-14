package gnu.cajo.utils.extra;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.io.Serializable;
import gnu.cajo.invoke.Remote;
import gnu.cajo.invoke.RemoteInvoke;

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
 * each rule's state to distributed JVMs. The engine is also serialisable;
 * to allow it to scale, as well as providing both modularity, and
 * redundancy.<p>
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
    * synchronisation or deadlock problems. It is highly recommended to
    * extend this class for your rules, however, only to the extend of
    * defining its abstract method reason. All methods should be left
    * unchanged, unless it is <i>extremely</i> critical to the operation
    * of the rule.
    */
   public static abstract class Rule implements Serializable, Runnable {
      /**
       * The rule specific local knowledge base on which to reason.
       */
      protected Hashtable facts = new Hashtable();
      /**
       * This flag indicates that asynchronous modification has been
       * made to the knowledge base. <i><u>Very Important</u>:</i>
       * The <i>first</i> instruction of the reason method should be to
       * set it false.
       */
      protected boolean change;
      /**
       * The local reasoning thread. It is lazily instantiated, and can
       * be shut down by calling its interrupt method, if necessary.
       * It's job is to call the reason method whenever there is a
       * change (addidion, removal, modification) to the local knowledge
       * base.
       */
      protected transient Thread thread;
      /**
       * This method is completely rule-specific. It is called when there
       * has been a change to the local knowledge base. It should first
       * reset the change flag, then evaluate its state to make decisions.
       * It is possible for the knowledgebase to be actively changing,
       * while this method is executing. This can be detected by monitoring
       * the state of the change flag.
       */
      protected abstract void reason();
      /**
       * This method is invoked by the rule engine to make a change,
       * addition or modification, to the local knowledge base. It will
       * notify the local processing thread, creating it if necessary, and
       * return. Its run time is designed to be as short as possible, and
       * should not be overridden, unless it is absolutely critical.
       */
      public synchronized void predicate(Object fact) {
         facts.put(fact.getClass(), fact);
         change = true;
         if (thread == null) {
            thread = new Thread(this);
            thread.start();
         } else notify();
      }
      /**
       * This method is invoked by the rule engine to make a deletion
       * from the local knowledge base. It will notify the local processing
       * thread, creating it if necessary, and return. Its run time is
       * designed to be as short as possible, and should not be overridden,
       * unless it is absolutely critical.
       */
      public synchronized void retract(Object fact) {
         facts.remove(fact.getClass());
         change = true;
         if (thread == null) {
            thread = new Thread(this);
            thread.start();
         } else notify();
      }
      /**
       * This is the local change processing thread. It waits until there
       * has been a change to the local knowledge base, and invokes the
       * reason method. It is used to offload the reasoning processing
       * from the change invocation thread. It is created on the first
       * change to this rule.
       */
      public void run() {
         try {
            while (true) {
               reason();
               synchronized(this) { while(!change) wait(); }
            }
         } catch(InterruptedException x) {}
      }
   }
   /**
    * This field represents the current state of all facts in the knowledge
    * base. The facts are organised by their type, each type of fact is
    * allowed only one instance. The ability to clear the knowledge base is
    * not provided, however it can be offered by a subclass of this engine,
    * if needed. Facts are generally assumed to be local data objects, but
    * they can in fact be references, to objects in remote JVMs.
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
    * This constructor performs no function, as the operation of the rule
    * engine is completely runtime dependent.
    */
   public RuleEngine() {}
   /**
    * This method is used to assert, or modify, a fact in the rule engine
    * knowledge base. If no fact of this type has been already registered
    * with this engine, the fact will be added automatically.<p>
    * The engine will extract from its rule base, all rules registered for
    * notification about the particular type of fact. Each rule will be
    * notified serially, by invoking its public <tt>predicate</tt> method,
    * with the fact object. If the rule instance determines all of its
    * criteria necessary for its firing has been met, then it is the
    * responsibility of that object to carry it out.<p>
    * Additionally, it will also notify all rules which are registered for
    * facts which are superclasses of the fact being asserted. This allows
    * supports logical <i>induction</i> chains of arbitrary length.<p>
    * For example, assume a fact is asserted:<p>
    * John sold his Jeep<p>
    * Assume this fact is an instance of a Jeep object. Correspondingly,
    * any rules listening for its superclass Truck, would also be
    * notified. Similarly any rules listening for Truck's superclass
    * Automobile would also be notified. Therefore the fact that John
    * sold his Jeep also would indicate that an automobile was sold. In this
    * case, the class hierarchy models logical sufficiency.<p>
    * The contents of the class, at each level of the hierarchy support
    * logical <i>deduction</i> as well. For example:<p>
    * An automobile has wheels, a truck is a type of automobile, a Jeep
    * is a type of truck: therefore John's Jeep has wheels. In this
    * context, the class hierarchy models logical necessity.<p>
    * The engine will work like a traditional rule engine, if class
    * hierarchies are not used, i.e. each fact type is a base class.<p>
    * <i><u>Note</u>:</i> Rule predicate invocations are expected to return
    * as quickly as possible, to ensure maximum performance of the engine.
    * Typically rule objects will save the fact argument in a queue, notify
    * a waiting local thread, and return. A rule generally will register for
    * multiple fact types, therefore its predicate method most likely
    * <i>will</i> be invoked reentrantly.
    * @param fact The fact that is being added or updated to the knowledge
    * base. The fact is a collection of data logically related to some
    * facet of the system. It is must be a local object. To minimise traffic,
    * facts can also be sent in an Object array.
    * @throws IllegalArgumentException If the provided fact is not
    * serialisable. This is a requirement to keep the rule engine
    * serialisable. Also if the instance of the fact provided is not a local
    * object reference.
    */
   public void posit(Object fact) {
      Object facts[] =  fact instanceof Object[] ?
         (Object[])fact : new Object[] { fact };
      for (int i = 0; i < facts.length; i++) {
         fact = facts[i];
         if (fact instanceof RemoteInvoke)
            throw new IllegalArgumentException("Fact must be local");
         if (!(fact instanceof Serializable))
            throw new IllegalArgumentException("Fact must be Serializable");
         Class factType = fact.getClass();
         memory.put(factType, fact);
         Enumeration ruleKeys = rules.keys();
         while(ruleKeys.hasMoreElements()) {
            Class ruleKey = (Class)ruleKeys.nextElement();
            if (ruleKey.isAssignableFrom(factType)) { // induction
               Vector v = (Vector)rules.get(ruleKey);
               Object list[] = v.toArray();
               for (int j = 0; j < list.length; j++)
                  try { Remote.invoke(list[i], "predicate", fact); }
                  catch(Exception x) {}
            }
         }
      }
   }
   /**
    * This method is used to rescind a fact in the rule engine knowledge
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
    * @param factType The Class of the fact that is being removed from the
    * knowledge base. To minimise traffic, fact types can be sent in a
    * Class array.
    * @return An instance of the fact that was removed from the knowledge
    * base, or an array of removed facts.
    */
   public Object retract(Object factType) {
      Class factTypes[] = factType instanceof Class[] ?
         (Class[])factType : new Class[] { (Class)factType };
      Object facts[] = new Object[factTypes.length];
      for (int i = 0; i < factTypes.length; i++) {
         Class facttype = factTypes[i];
         facts[i] = memory.remove(facttype);
         if (facts[i] != null) {
            Enumeration ruleKeys = rules.keys();
            while(ruleKeys.hasMoreElements()) {
               Class ruleKey = (Class)ruleKeys.nextElement();
               if (ruleKey.isAssignableFrom(facttype)) {
                  Vector v = (Vector)rules.get(ruleKey);
                  Object list[] = v.toArray();
                  for (int j = 0; j < list.length; i++)
                     try { Remote.invoke(list[j], "retract", facts[i]); }
                     catch(Exception x) {}
               }
            }
         }
      }
      return facts.length == 1 ? facts[0] : facts;
   }
   /**
    * This method is used to request the current state of a fact in the
    * knowledge base.
    * @param factType The Class of the fact that is being requested from the
    * knowledge base. To minimise traffic, the fact tyhpes can be sent in a
    * Class array.
    * @return An instance of the fact that was obtained from the knowledge
    * base, if any, or an array of facts.
    */
   public Object query(Object factType) {
      Class factTypes[] = factType instanceof Class[] ?
         (Class[])factType : new Class[] { (Class)factType };
      Object facts[] = new Object[factTypes.length];
      for (int i = 0; i < factTypes.length; i++)
         facts[i] = memory.get(factTypes[i]);
      return facts.length == 1 ? facts[0] : facts;
   }
   /**
    * This method is used to register a rule instance for a type of 
    * fact. The rule engine will store the rule reference, to have either
    * its <tt>predicate</tt> or its <tt>retract</tt> methods invoked, as the
    * state of the fact of interest changes. It is worth mentioning, a
    * single rule instance is often used to monitor multiple types of facts.
    * <p>When a rule is first rigistered, the engine will check its knowledge
    * base, if there are a fact of the class type posited, or any of its
    * subclasses, they will all be invoked on the rules <tt>predicate</tt>
    * method.
    * @param factType The category of fact to be monitored.
    * @param rule The rule to be notified when a fact state changes. It is
    * typically a reference to an object in a remote JVM, but it can be to
    * a local object, or even a proxy object.
    * @throws IllegalArgumentException If the provided rule is not
    * serialisable. This is a requirement to keep the rule engine
    * serialisable.
    */
   public void add(Class factType, Object rule) {
      if (!(rule instanceof Serializable))
         throw new IllegalArgumentException("Rule must be Serializable");
      Vector list = (Vector)rules.get(factType);
      if (list == null) synchronized(this) {
         list = (Vector)rules.get(factType);
         if (list == null) {
            list = new Vector();
            rules.put(factType, list);
         }
      }
      list.add(rule);
      Enumeration memoryKeys = memory.keys();
      while(memoryKeys.hasMoreElements()) {
         Class memoryKey = (Class)memoryKeys.nextElement();
         if (factType.isAssignableFrom(memoryKey)) {
            try { Remote.invoke(rule, "predicate", memory.get(memoryKey)); }
            catch(Exception x) {}
         }
      }
   }
   /**
    * This method is used to unregister a rule instance for a type of 
    * fact. The rule engine will delete the rule reference from its
    * knowledge base.
    * @param factType The category of fact being monitored.
    * @param rule The rule to be removed from the engine. It is typically a
    * reference to an object in a remote JVM, but it can be to a local
    * object, or even a proxy object.
    */
   public void remove(Class factType, Object rule) {
      Vector v = (Vector)rules.get(factType);
      if (v != null) v.remove(rule);
   }
}
