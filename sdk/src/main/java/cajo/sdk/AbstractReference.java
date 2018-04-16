package cajo.sdk;

import java.util.ArrayList;

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
 * This internal use only class extends the foundation for building {@link
 * AbstractService service} and {@link AbstractController controller}
 * objects.
 * @author John Catherino
 */
abstract class AbstractReference extends AbstractObject {
   /**
   * This utility field provides a standard exception description for
   * network related errors. It is provided here for convenient reuse.
   * It is normally specified when a method can throw other exceptions
   * in <i>addition</i> to java.rmi.RemoteException. This can occur any
   * time an object needs to invoke a method on a remote object.
   */
   protected static final String REMOTEEXCEPTION[] =
      Descriptor.DEFAULTEXCEPTION[0];
   /**
    * This string provides a standard description for throwing no checked
    * exceptions. This is only possible for controllers, where a method
    * could execute entirely locally, and thereby not throw <i>any</i>
    * checked exceptions. It is provided here for convenient reuse.
    */
   protected static final String[][] NOEXCEPTION = {{
      "<i>none</i>", "This method throws no checked exceptions."
   }};
   /**
    * This list contains the descriptions of the object's public methods
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
   /**
    * The default constructor defines the default functionality common to all
    * cajo objects.
    * @param serviceRef The reference to the service object on which this
    * object operates
    */
   @SuppressWarnings("hiding")
   AbstractReference(Object serviceRef) {
      super(serviceRef);
      descriptors = new ArrayList<Descriptor>();
      descriptors.add(new Descriptor("getDescription",
         "This <i>canonical</i> method is used to obtain an html " +
         "encoded string describing the methods furnished by this " +
         "object. Its format is invariant, to allow for automated " +
         "parsing, if desred. Please see <a href=https://cajo.dev.java.net>" +
         "the cajo project</a>, for further details.",
         null, // no arguments
         new String[] { // return
            "java.lang.String",
            "An html encoded document describing the methods " +
            "furnished, their functionality, argument requirements, " +
            "returns, and possible thrown exceptions."
         }, null // could throw RemoteException
      ));
   }
   /**
    * This method is used by subclasses to add their unique method
    * descriptions. The contents are returned via the {@link #getDescription
    * getDescription} method.
    * @param method The name of the method
    * @param description An explanation of what this method does
    * @param arguments The descriptions of the arguments this method takes
    * the two-dimensional array is of pairs of argument class names, and
    * descriptions, <i>in order;</i> it can be null, if the method takes no
    * arguments
    * @param retval The description of the method return, the first
    * element of the array is the class name or primitive type of the return,
    * the second element is the description; it can be null if the method
    * returns no value
    * @param exceptions The descriptions of the exceptions this method
    * throws, the two dimensional array is of pairs of exception class names,
    * and descriptions of their significance; it can be null, in which case
    * the standard RMIException description will be provided
    */
   @SuppressWarnings("hiding")
   protected final void addDescriptor(String method, String description,
      String arguments[][], String retval[], String exceptions[][]) {
         descriptors.add(new Descriptor(method, description,
            arguments, retval, exceptions));
   }
   /**
    * This method is called by clients, to get a standardised html encoded
    * destription of the object's methods. The format is invariant,
    * to allow automated parsing, if desired.
    * @return A detailed description of the functionality of the object
    */
   public final String getDescription() {
      return Descriptor.format(description, descriptors);
   }
}
