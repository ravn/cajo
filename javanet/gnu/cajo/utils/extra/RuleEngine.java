package gnu.cajo.utils.extra;

import java.util.Vector;
import java.util.Hashtable;
import java.io.Serializable;
import gnu.cajo.invoke.Remote;

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
    * <i><u>Note</u>:</i> Rule predicate invocations are expected to return
    * as quickly as possible, to ensure maximum performance of the engine.
    * Typically rule objects will save the fact argument, notify a waiting
    * local thread, and return.  If a rule invocation results in an
    * exception, normally for network related errors, it will be removed from
    * the engine for that fact type. A rule generally will register for
    * multiple fact types, therefore its predicate method most likely
    * <i>will</i> be invoked reentrantly. A rule must <i>never</i>
    * synchronise its predicate method, as the impact could be disastrous.
    * @param fact The fact that is being added or updated to the knowledge
    * base. The fact is a collection of data logically related to some
    * facet of the system. It is either a local data object, a proxy object,
    * or a reference to a remote object.
    * @throws IllegalArgumentException If the provided fact is not
    * serialisable. This is a requirement to keep the rule engine
    * serialisable.
    */
   public void posit(Object fact) {
      if (!(fact instanceof Serializable))
         throw new IllegalArgumentException("Fact must be Serializable");
      Class factType = fact.getClass();
      memory.put(factType, fact);
      Vector v = (Vector)rules.get(factType);
      if (v != null) {
         Object list[] = v.toArray();
         for (int i = 0; i < list.length; i++)
            try { Remote.invoke(list[i], "predicate", fact); }
            catch(Exception x) { remove(factType, list[i]); }
      }
   }
   /**
    * This method is used to rescind a fact in the rule engine knowledge
    * base. The engine will extract from its rule base, all rules registered
    * for notification about the particular type of fact. Each will be
    * notified serially, by invokeing its public <tt>retract</tt> method,
    * with the class of the fact being retracted.<p>
    * <i><u>Note</u>:</i> Rule retract invocations are expected to return as
    * quickly as possible, to ensure maximum performance of the engine.
    * Typically rule objects will save the fact class, notify a waiting local
    * thread, and return.  If a rule invocation results in an exception,
    * normally for network related errors, it will be removed from the engine
    * for that fact type automatically. A rule generally will register for
    * multiple fact types, therefore its retract method most likely
    * <i>will</i> be invoked reentrantly. A rule must <i>never</i>
    * synchronise its retract method, as the impact could be disastrous.
    * @param factType The category of fact that is being removed from the
    * knowledge base.
    */
   public void retract(Class factType) {
      Object fact = memory.remove(factType);
      if (fact != null) {
         Vector v = (Vector)rules.get(factType);
         if (v != null) {
            Object list[] = v.toArray();
            for (int i = 0; i < list.length; i++)
               try { Remote.invoke(list[i], "retract", factType); }
               catch(Exception x) { remove(factType, list[i]); }
         }
      }
   }
   /**
    * This method is invoked to determine the current state of a rule in
    * the knowledge base. The rule engine will extract the fact, and if
    * it exists, it will invoke the rule's <tt>predicate</tt> method with
    * the fact. This is approach done to simplify the structure of rule
    * objects. However, it is very important to understand that due to the
    * inherent reentrancy of the rule engine; a requested fact's state could
    * actually arrive <i>following</i> an invocation with a <i>more</i>
    * current state. If this would cause problems, it is recommended that
    * the fact object implement some manner of timestamp.
    * @param factType The category of fact whose current state is being
    * requested.
    * @param rule The rule whose <tt>predicate</tt> method is to be invoked
    * with the fact, if it exists. It is typically a reference to an object
    * in a remote JVM, but it can be to a local object, or even a proxy.
    */
   public void getFact(Class factType, Object rule) {
      Object fact = memory.get(factType);
      if (fact != null)
         try { Remote.invoke(rule, "predicate", fact); }
         catch(Exception x) { remove(factType, rule); }
   }
   /**
    * This method is used to register a rule instance for a type of 
    * fact. The rule engine will store the rule reference, to have either
    * its <tt>predicate</tt> or its <tt>retract</tt> methods invoked, as the
    * state of the fact of interest changes. It is worth mentioning, a
    * single rule instance is often used to monitor multiple types of facts.
    * @param factType The category of fact to be monitored.
    * @param rule The rule to be notified when a fact state changes. It is
    * typically a reference to an object in a remote JVM, but it can be to
    * a local object, or even a proxy object.
    * @throws IllegalArgumentException If the provided rule is not
    * serialisable. This is a requirement to keep the rule engine
    * serialisable.
    */
   public synchronized void add(Class factType, Object rule) {
      if (!(rule instanceof Serializable))
         throw new IllegalArgumentException("Rule must be Serializable");
      Vector list = (Vector)rules.get(factType);
      if (list == null) {
         list = new Vector();
         rules.put(factType, list);
      }
      list.add(rule);
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
