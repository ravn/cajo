package util;

import java.util.List;

/*
 * This internal use only helper class is used to allow services and
 * to controllers describe their exposed methods to clients.
 * The cajo project: https://cajo.dev.java.net
 * For issues or suggestions mailto:cajo@dev.java.net
 * This class is released into the public domain.
 * Written by John Catherino
 */
final class Descriptor {
   private static final String NULLEXCEPTION[][] = {{
      "java.rmi.RemoteException",
      "This exception is thrown implicitly whenever the function " +
      "invocation fails for network related reasons."
   }},
   NOARGS[][] = {{ "<i>void</i>", "This function takes no arguments." }},
   NORETURN[] = {  "<i>void</i>", "This function returns no value."   };
   private final String function;
   private final String description;
   private final String arguments[][];
   private final String retval[];
   private final String exceptions[][];
   private void describe(StringBuffer sb) {
      sb.append("\r\n<li><h4>");
      sb.append(function);
      sb.append("</h4>\r\n");
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
         sb.append("<td>");
         sb.append(argument[0]);
         sb.append("</td>\r\n<td>");
         sb.append(argument[1]);
         sb.append("</tr>\r\n");
      }
      sb.append("<tr align=\"left\">\r\n" +
         "<th bgcolor=\"#F0F0F0\">Return</th>\r\n<td>");
      sb.append(retval[0]);
      sb.append("</td>\r\n<td>");
      sb.append(retval[1]);
      sb.append("</td>\r\n<tr align=\"left\">\r\n<th rowspan=\"");
      sb.append(exceptions.length);
      sb.append("\" bgcolor=\"#F0F0F0\">Exceptions</th>\r\n");
      for (String exception[] : exceptions) {
         sb.append("<td>");
         sb.append(exception[0]);
         sb.append("</td>\r\n<td>");
         sb.append(exception[1]);
         sb.append("</tr>\r\n");
      }
      sb.append("</table>");
   }
   Descriptor(String function, String description, String arguments[][],
      String retval[], String exceptions[][]) {
      this.function    = function;
      this.description = description;
      this.arguments   = arguments  != null ? arguments  : NOARGS;
      this.retval      = retval     != null ? retval     : NORETURN;
      this.exceptions  = exceptions != null ? exceptions : NULLEXCEPTION;
   }
   static String format(String description, List<Descriptor> descriptors) {
      StringBuffer sb = new StringBuffer("<h2>API:</h2>\r\n");
      sb.append(description);
      sb.append("\r\n<h3>Functions:</h3><ul>");
      for(Descriptor desc : descriptors) desc.describe(sb);
      sb.append("</ul>");
      return sb.toString();
   }
}

