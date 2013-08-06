package cajo.sdk;

import java.util.List;

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
 * This internal use only helper class is used to allow services and
 * to controllers describe their exposed methods to clients.
 * The cajo project: https://cajo.dev.java.net
 * For issues or suggestions mailto:cajo@dev.java.net
 * @author John Catherino
 */
final class Descriptor {
   static final String DEFAULTEXCEPTION[][] = {{
      "java.rmi.RemoteException",
      "This exception is thrown implicitly whenever the method " +
      "invocation fails for network related reasons." }},
      NOARGS[][] = {{ "<i>void</i>", "This method takes no arguments." }},
      NORETURN[] = {  "<i>void</i>", "This method returns no value."   };
   private final String method;
   private final String description;
   private final String arguments[][];
   private final String retval[];
   private final String exceptions[][];
   private void describe(StringBuilder sb) {
      sb.append("\r\n<li><h4>").append(method).append("</h4>\r\n");
      sb.append(description);
      sb.append("<br><br>\r\n<table border=\"1\" width=\"100%\">\r\n" +
         "<tr align=\"left\" bgcolor=\"#E0E0E0\">\r\n" +
         "<th>Parameter</th>\r\n" +
         "<th>Type</th>\r\n" +
         "<th>Description</th>\r\n"
      );
      sb.append("<tr align=\"left\">\r\n<th rowspan=\"");
      sb.append(arguments.length);
      sb.append("\" bgcolor=\"#F0F0F0\">Arguments</th>\r\n");
      for (String argument[] : arguments) {
         sb.append("<td>").append(argument[0]).append("</td>\r\n");
         sb.append("<td>").append(argument[1]).append("</tr>\r\n");
      }
      sb.append("<tr align=\"left\">\r\n" +
         "<th bgcolor=\"#F0F0F0\">Return</th>\r\n");
      sb.append("<td>").append(retval[0]).append("</td>\r\n");
      sb.append("<td>").append(retval[1]).append("</td>\r\n");
      sb.append("<tr align=\"left\">\r\n<th rowspan=\"");
      sb.append(exceptions.length);
      sb.append("\" bgcolor=\"#F0F0F0\">Exceptions</th>\r\n");
      for (String exception[] : exceptions) {
         sb.append("<td>").append(exception[0]).append("</td>\r\n");
         sb.append("<td>").append(exception[1]).append("</tr>\r\n");
      }
      sb.append("</table>");
   }
   @SuppressWarnings("hiding") // we're defining instance values
   Descriptor(String method, String description, String arguments[][],
      String retval[], String exceptions[][]) {
      this.method      = method;
      this.description = description;
      this.arguments   = arguments  != null ? arguments  : NOARGS;
      this.retval      = retval     != null ? retval     : NORETURN;
      this.exceptions  = exceptions != null ? exceptions : DEFAULTEXCEPTION;
   }
   static String format(String description, List<Descriptor> descriptors) {
      StringBuilder sb = new StringBuilder("<h2>API:</h2>\r\n");
      sb.append(description).append("\r\n<h3>Methods:</h3><ul>");
      for(Descriptor desc : descriptors) desc.describe(sb);
      return sb.append("</ul>").toString();
   }
}

