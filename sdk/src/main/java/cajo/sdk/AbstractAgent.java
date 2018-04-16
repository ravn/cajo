package cajo.sdk;

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
 * The agent is a piece of executable code, sent from one {@link
 * AbstractService service} to run locally at another. It is the logical
 * converse of a controller object. It is normally used either when a
 * client has to interact frequently with a service, so as to minimise
 * network traffic; or if the receiving service operates operates on large
 * data sets, to prevent having to send large amounts of data over the
 * network. Conceptually, a general purpose agent could belong to several
 * different services.<br>
 * <i><u>NB</u>:</i> Not all service objects accept agents, as a malicious
 * agent could crash the service JVM e.g. by excessive memory allocation.
 * They are primarily accepted in well known domains, where the agents are
 * trusted, <i>or</i> where the agent class definitions are already in the
 * classpath of the server.
 * @see AbstractService
 * @author John Catherino
 */
public abstract class AbstractAgent extends AbstractObject implements IAgent {
   /**
    * The default constructor simply assigns its service reference.
    * An agent is of course free to launch threads in its constructor, if
    * it wishes, to perform functionality asynchronously of its
    * <i>event-driven</i> method invocations its service object.
    * @param serviceRef The reference to the service on which this agent
    * communicates
    */
   @SuppressWarnings("hiding")
   protected AbstractAgent(Object serviceRef) { super(serviceRef); }
   /** {@inheritDoc} */
   @SuppressWarnings({"unused", "hiding"})
   @Override
   public void init(gnu.cajo.Cajo cajo, Object localService) {
      this.cajo = cajo;
   } // localService is used only by subclasses
   /**
    * This method provides a means to identify this agent.
    * @return An identifier <i>(not a description)</i> of the agent
    */
   @Override
   public String toString() { return "AbstractAgent"; }
}
