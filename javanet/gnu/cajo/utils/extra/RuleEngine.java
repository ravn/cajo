package gnu.cajo.utils.extra;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
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
    * <i><u>Note</u>:</i> Rule predicate invocations are expected to return
    * as quickly as possible, to ensure maximum performance of the engine.
    * Typically rule objects will save the fact argument, notify a waiting
    * local thread, and return. A rule generally will register for
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
      Enumeration ruleKeys = rules.keys();
      while(ruleKeys.hasMoreElements()) {
         Class ruleKey = (Class)ruleKeys.nextElement();
         if (ruleKey.isAssignableFrom(factType)) { // induction
            Vector v = (Vector)rules.get(ruleKey);
            Object list[] = v.toArray();
            for (int i = 0; i < list.length; i++)
               try { Remote.invoke(list[i], "predicate", fact); }
               catch(Exception x) {}
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
    * Typically rule objects will save the fact class, notify a waiting local
    * thread, and return. A rule generally will register for multiple fact
    * types, therefore its retract method most likely <i>will</i> be invoked
    * reentrantly. A rule must <i>never</i> synchronise its retract method,
    * as the impact could be disastrous.
    * @param factType The category of fact that is being removed from the
    * knowledge base.
    */
   public void retract(Class factType) {
      Object fact = memory.remove(factType);
      if (fact != null) {
         Enumeration ruleKeys = rules.keys();
         while(ruleKeys.hasMoreElements()) {
            Class ruleKey = (Class)ruleKeys.nextElement();
            if (ruleKey.isAssignableFrom(factType)) {
               Vector v = (Vector)rules.get(ruleKey);
               Object list[] = v.toArray();
               for (int i = 0; i < list.length; i++)
                  try { Remote.invoke(list[i], "retract", factType); }
                  catch(Exception x) {}
            }
         }
      }
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
   public void add(Class factType, Object rule) {
      if (!(rule instanceof Serializable))
         throw new IllegalArgumentException("Rule must be Serializable");
      synchronized(this) {
         Vector list = (Vector)rules.get(factType);
         if (list == null) {
            list = new Vector();
            rules.put(factType, list);
         }
         list.add(rule);
      }
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
