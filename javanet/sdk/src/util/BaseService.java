package util;

import gnu.cajo.Cajo;
import gnu.cajo.invoke.Remote;
import gnu.cajo.invoke.Invoke;
import gnu.cajo.utils.ItemServer;
import java.rmi.MarshalledObject;
import java.util.List;
import java.util.ArrayList;

/*
 * A base service class for a cajo grail server.
 * The cajo project: https://cajo.dev.java.net
 * For issues or suggestions mailto:cajo@dev.java.net
 * This class is released into the public domain.
 * Written by John Catherino 01-Dec-09
 */

/**
 * The service is a publicly invocable ordinary Java object. All of its
 * public methods, either instance or static, can be invoked by remote JVMs.
 * It is often recommended to use this class as a wrapper, exposing specific
 * methods of its aggregate cache. This class may, or may not, provide a
 * graphical user interface for clients.
 * @author <a href=http://wiki.java.net/bin/view/People/JohnCatherino>
 * John Catherino</a>
 */
public abstract class BaseService {
   private final Invoke loader;
   private MarshalledObject mob;
   private List<Descriptor> descriptors = new ArrayList<Descriptor>();
   /**
    * This string is reassigned by subclasses, to desribe the functionality
    * of this service, for clients. It is returned as part of the
    * getDescription string.
    */
   protected String description =
      "The base class for most service definitions.";
   /**
    * The constructor will locally bind the service under the name provided,
    * and join the <a href=http://weblogs.java.net/blog/cajo/archive/2007/09/simple_interjvm.html>
    * cajo federation</a>.
    * @param handle The class name of the controller to instantiate on
    * arrival at the client JVM. <i>(e.g. controller.TestController)</i><br>
    * <i>NB:</i> the handle can be null, if the service wishes to provide no
    * GUI for clients.
    * @param name The name under which to bind the service in the local
    * rmi registry. This services are sometimes accessed through their
    * registry URL <i>(e.g. //myhost:1198/main)</i>.
    */
   protected BaseService(String handle, String name) throws Exception {
      loader = handle != null ? new ProxyLoader(handle) : null;
      descriptors.add(new Descriptor("getController",
         "This function is used to obtail a locally running <i>smart</i> " +
         "reference to the remote object. It allows the server to offload " +
         "some compute and storage requirements to the client. It is not " +
         "<i>required</i> for a client to request a service's controller, and " +
         "some services may not support controller objects at all. However, " +
         "requesting a service controller is considered <i>common courtesy.</i>",
         new String[][] { // arguments
            NOARGS,
         }, new String[][] { // returns
            {
               "java.rmi.MarshalledObject",
               "A locally running object, minimally implementing the " +
               "service object's published interface, contained within a " +
               "MarshalledObject. It may also be <i>null,</i> if the " +
               "service does not support controller objects."
            },
         }, new String[][] { // exceptions
            RMIEXCEPTION,
         }
      ));
      descriptors.add(new Descriptor("getDescription",
         "This function is used to obtain a html encoded string describing " +
         "the methods furnished by this service ",
         new String[][] { // arguments
            NOARGS,
         }, new String[][] { // returns
            {
               "java.lang.String",
               "An html encoded document describing the methods furnished, " +
               "and their functionality."
            },
         }, new String[][] { // exceptions
            RMIEXCEPTION,
         }
      ));
      ItemServer.bind(this, name); // publish graphical controller service
      cajo.export(this); // add service to cajo federation
   }
   /**
    * This internal use only helper class is used to allow services to
    * describe their exposed methods to clients.
    * @author <a href=http://wiki.java.net/bin/view/People/JohnCatherino>
    * John Catherino</a>
    */
   private static final class Descriptor { // provides method descriptions
      private final String function;
      private final String description;
      private final String arguments[][]; // type/description pairs
      private final String returns[][]; // type/description pairs
      private final String exceptions[][]; // type/description pairs
      private static String format(String description,
         List<Descriptor> descriptors) {
         StringBuffer sb = new StringBuffer("<h3>Description:</h3><br>");
         sb.append(description);
         sb.append("<br><h3>Functions:</h3>");
         for(Descriptor desc : descriptors) desc.describe(sb);
         return sb.toString();
      }
      private void describe(StringBuffer sb) {
         sb.append("<h4>");
         sb.append(function);
         sb.append("</h4><br>");
         sb.append(description);
         sb.append("<br><br><table border=\"1\" cellspacing=\"1\" "+
            "cellpadding=\"1\" witdh=\"95%\">" +
            "<tr bgcolor=\"#C0C0C0\">" +
               "<th align=\"left\">Parameter</th>" +
               "<th align=\"left\">Type</th>" +
               "<th align=\"left\">Description</th>" +
            "</tr>"
         );
         sb.append("<tr>" +
            "<th rowspan=\"2\" align=\"left\" valign=\"top\" " +
               "bgcolor=\"#E0E0E0\">Arguments</th>"
         );
         for (String argument[] : arguments) {
            sb.append("<td valign=\"top\">");
            sb.append(argument[0]);
            sb.append("</td><td>");
            sb.append(argument[1]);
            sb.append("</td>");
         }
         sb.append("</tr><tr></tr><tr>" +
            "<th align=\"left\" valign=\"top\" bgcolor=\"#E0E0E0\">" +
               "Return</th>"
         );
         for (String retrun[] : returns) { // ;-)
            sb.append("<td valign=\"top\">");
            sb.append(retrun[0]);
            sb.append("</td><td>");
            sb.append(retrun[1]);
            sb.append("</td>");
         }
         sb.append("</tr><tr></tr><tr>" +
            "<th rowspan=\"2\" align=\"left\" valign=\"top\" " +
               "bgcolor=\"#E0E0E0\">Exceptions</th>"
         );
         for (String exception[] : exceptions) {
            sb.append("<td valign=\"top\">");
            sb.append(exception[0]);
            sb.append("</td><td>");
            sb.append(exception[1]);
            sb.append("</td>");
         }
         sb.append("</tr></table>");
      }
      private Descriptor(String function, String description,
         String arguments[][], String returns[][], String exceptions[][]) {
         this.function    = function;
         this.description = description;
         this.arguments   = arguments;
         this.returns     = returns;
         this.exceptions  = exceptions;
      }
   }
   /**
    * This method is used by subclasses to add their unique method
    * descriptions. The contents are returned via the getDescription menthod.
    * @param function The name of the function
    * @param description An explanation of what this function does
    * @param arguments The descriptions of the arguments this function takes
    * @param returns The descriptions of the objects this function returns
    * @param exceptions The descriptions of the exceptions this function
    * throws
    */
   protected void addDescriptor(String function, String description,
      String arguments[][], String returns[][], String exceptions[][]) {
         descriptors.add(new Descriptor(function, description, arguments,
            returns, exceptions));
   }
   /**
    * This is a commonly used exception description, made public here for
    * easy reuse. It is used to describe RMI/network related failures,
    * which technically, any method can throw.
    */
   protected static final String[] RMIEXCEPTION = {
      "java.rmi.RemoteException",
      "This exception is thrown implicitly, whenever the function " +
      "invocation fails, for network related reasons."
   };
   /**
    * This is a commonly used argument description, made public here for
    * easy reuse. It is used to describe when a function accepts no
    * arguments.
    */
   protected static final String[] NOARGS = {
      "();", "This function takes no arguments"
   };
   /**
    * This is a commonly used return description, made public here for
    * easy reuse. It is used to describe when a function returns no
    * value.
    */
   protected static final String[] NORETURNS = {
      "void", "This function returns no value"
   };
   /**
    * This is the <a href=https://cajo.dev.java.net/nonav/docs/gnu/cajo/Cajo.html>
    * cajo</a> object by which service objects can publish themselves,
    * and search for other services via the <a href=http://weblogs.java.net/blog/cajo/archive/2007/09/simple_interjvm.html>
    * grail</a> framework.
    * <i>NB:</i> two important points: all arguments or returns must be either
    * serialisable, or proxied via the single-argument Cajo.proxy method;
    * objects are passed by value, <i>(i.e. copies)</i> unless by proxied via
    * Cajo.
    */
   public static Cajo cajo;
   /**
    * This method is called by remote clients to request the service's
    * controller for its GUI. It is the fundamental public method of
    * services providing graphical unser interfaces. If the service does not
    * provide a client GUI, the method will return null.
    * @return java.rmi.MarshalledObject The controller object, embedded in
    * a marshalled object, to preserve its codebase annotation. <i>NB:</i>
    * this reference is serialisable, and may be freely passed between
    * JVMs, the controller extracted via the get method will not properly
    * pass.
    */
   public final MarshalledObject getController() {
      if (mob == null && loader != null) try {
         Remote.invoke(loader, "setService", new Remote(this));
         mob = new MarshalledObject(loader);
      } catch(Exception x) {}
      return mob;
   }
   /**
    * This method is called by remote clients, to get a standardised,
    * destription of the service's functions.
    * @return An html encoded description of the functionality of the service
    */
   public final String getDescription() {
      return Descriptor.format(description, descriptors);
   }
}
