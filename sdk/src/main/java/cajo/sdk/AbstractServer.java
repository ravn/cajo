package cajo.sdk;

import gnu.cajo.invoke.Remote;
import gnu.cajo.utils.CodebaseServer;

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
 * The server exports, and unexports, {@link AbstractService service} objects.
 * It can optionally provide {@link AbstractAgent agent} or {@link
 * AbstractController controller} objects. A server can provide as many
 * services as desired.
 * @author John Catherino
 */
public abstract class AbstractServer {
   static { // disable mobile code by default, and use server.policy for security
      if (System.getProperty("java.security.policy") == null)
         System.setProperty("java.security.policy", "server.policy");
      System.setProperty("java.rmi.server.useCodebaseOnly", "true");
      try { gnu.cajo.Cajo.main(null); } catch(java.io.IOException x) {}
   }
   /**
    * This is the singleton <a href=https://cajo.dev.java.net/nonav/docs/gnu/cajo/Cajo.html>
    * cajo</a> object with which service, controller, and agent objects can
    * search for other services via the <a href=http://weblogs.java.net/blog/cajo/archive/2007/09/simple_interjvm.html>
    * grail</a> framework.
    */
   protected static gnu.cajo.Cajo cajo;
   /**
    * An optional <a href=https://cajo.dev.java.net/nonav/docs/gnu/cajo/utils/CodebaseServer.html>
    * CodebaseServer</a>, which would be used to furnish mobile codebases for
    * controllers and agents, if needed.
    */
   protected static CodebaseServer codebaseServer;
   /**
    * The default constructor performs no function.
    */
   protected AbstractServer() {}
   /**
    * This utility function is used to export jar files needed by clients.
    * These jar files contain the class definitions and 3rd party libraries
    * used by agents and controllers of this server, which would not
    * ordinarily be found in the codebase of the client. It is typically only
    * called once, at the startup of the server. It can be called
    * subsequently, to change the exported codebase on the fly, but that
    * tends to delve into the arena of <i>rocket science.</i> ;)
    * @param jars The collection of jar files needed by a client
    */
   protected static final void export(String... jars) {
      String header = "http://" + Remote.getDefaultClientHost() + ':' +
         codebaseServer.serverPort + '/';
      StringBuilder base = new StringBuilder();
      for (String jar : jars)
         base.append(header).append(jar).append(' ');
      System.setProperty("java.rmi.server.codebase", base.toString());
   }
   /**
    * This method will make all services no longer available for remote
    * connexion, in preparation for an orderly server shutdown.
    * <i><u>NB</u>:</i> If any services have accepted agents, those agents
    * will <i>continue</i> to operate, since they have a local reference to
    * the service.
    */
   protected static final void shutdown() { Remote.shutdown(); }
}
