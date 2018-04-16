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
 * This interface defines the fundamental method of a <a href=https://cajo.dev.java.net>
 * cajo</a> service agent. An agent is sent from one service to another, to
 * operate locally at it. This is to both reduce network traffic, and improve
 * performance. As an agent needs to know its receiving service interface, or
 * at least part of it: an agent is typically custom-built, for a particular
 * receiving service class.
 * @author John Catherino
 */
public interface IAgent {
   /**
    * This method is invoked only once, by the receiving service on arrival.
    * It can be overridden to perform additional initialisation. However, if
    * this method is overridden, be sure to first call
    * <tt>super.init(cajo, localService);.</tt> Here is where the agent's
    * asynchronous processing threads would be created and launched.
    * @param cajo The receiver's cajo reference, with which the agent may
    * look up other services to fulfill its functionality, if needed
    * @param localService A local reference to the receiving service object
    */
   void init(gnu.cajo.Cajo cajo, Object localService);
}
