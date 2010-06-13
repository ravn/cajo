package util;

import java.util.ArrayList;

/*
 * A package internal base class for cajo grail services and controllers.
 * The cajo project: https://cajo.dev.java.net
 * For issues or suggestions mailto:cajo@dev.java.net
 * This class is released into the public domain.
 * Written by John Catherino
 */
class BaseObject implements java.io.Serializable {
   private static final long serialVersionUID = 0L;
   /**
    * This string provides a standard exeption description for
    * java.rmi.RemoteExceptions, caused by network errors. Since any remote
    * method invocation can throw one, it is provided here and recommended,
    * for convenient reuse.
    */
   protected static final String[] RMIEXCEPTION = {
      "java.rmi.RemoteException",
      "This exception is thrown implicitly whenever a service function " +
      "invocation fails for network related reasons."
   };
   /**
    * This list contains the descriptions of the object's public functions
    * for client use. Subclasses append the list, with their additional
    * implementations.
    */
   protected final ArrayList<Descriptor> descriptors;
   /**
    * This string is reassigned by subclasses, to desribe the functionality
    * of this object, for clients. It is returned automatically as part of
    * the {@link #getDescription getDescription} string.
    */
   protected String description = "description currently undefined";
   protected BaseObject() {
      descriptors = new ArrayList<Descriptor>();
      descriptors.add(new Descriptor("getDescription",
         "This <i>canonical*</i> function is used to obtain an html " +
         "encoded string describing the functions furnished by this " +
         "object. Its format is invariant, to allow for automated " +
         "parsing, if desred. Please see <a href=https://cajo.dev.java.net>" +
         "the cajo project</a>, for further details.<br>*canonical " +
         "meaning it is <i>expected</i> to be implemented by <u>all</u> "+
         "client usable objects; services <i>and</i> controllers",
         null, // no arguments
         new String[] { // return
            "java.lang.String",
            "An html encoded document describing the functions " +
            "furnished, their functionality, argument requirements, " +
            "returns, and possible thrown exceptions."
         }, null // no special exceptions
      ));
   }
   /**
    * This method is used by subclasses to add their unique function
    * descriptions. The contents are returned via the {@link #getDescription
    * getDescription} method. The distinction between a method and a function
    * in this case, is that methods are related to the instance of an object,
    * whereas a function is static to it. Since this is of no concern to a
    * client; the name function is used externally, and the disctinction
    * is only of significance internally.
    * @param function The name of the function
    * @param description An explanation of what this function does
    * @param arguments The descriptions of the arguments this function takes
    * the two-dimensional array is of pairs of argument class names, and
    * descriptions, <i>in order;</i> it can be null, if the function takes no
    * arguments
    * @param retval The description of the functional return, the first
    * element of the array is the class name or primitive type of the return,
    * the second element is the description; it can be null if the function
    * returns no value
    * @param exceptions The descriptions of the exceptions this function
    * throws, the two dimensional array is of pairs of exception class names,
    * and descriptions of their significance; it can be null, in which case
    * the standard RMIException description will be provided
    */
   protected final void addDescriptor(String function, String description,
      String arguments[][], String retval[], String exceptions[][]) {
         descriptors.add(new Descriptor(function, description,
            arguments, retval, exceptions));
   }
   /**
    * This method is called by clients, to get a standardised html encoded
    * destription of the object's functions. The format is invariant,
    * to allow automated parsing, if desired.
    * @return A detailed description of the functionality of the object
    */
   public final String getDescription() {
      return Descriptor.format(description, descriptors);
   }
}
