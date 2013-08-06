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
 * The controller is the smart go-between for its serice object. It is used
 * primarily to offload server workload and local storage related to the
 * support of the client. As a controller object must know its {@link
 * AbstractService service} in detail, typically a controller belongs
 * exclusively to one service.
 * <i><u>NB</u>:</i> Much as with service objects, a controller should
 * assume that it will be used by multiple service threads, <i>concurrently,</i>
 * therefore synchronisation of non-threadsafe code regions is essential.
 * @see AbstractService
 * @author John Catherino
 */
public abstract class AbstractController extends AbstractReference
   implements IController {
   /**
    * The constructor assigns the {@link AbstractService service} reference,
    * and defines the canonical controller methods. A controller is of course
    * free to launch threads in its constructor, if it wishes, to perform
    * functionality asynchronously of its <i>event-driven</i> method
    * invocations by its client.
    * @param serviceRef The service object reference on which this controller
    * operates
    */
   @SuppressWarnings("hiding")
   protected AbstractController(Object serviceRef) {
      super(serviceRef);
      descriptors.add(new Descriptor("getView",
         "This <i>canonical</i> method is normally called by a " +
         "graphical client, to get the view component associated with " +
         "this controller,  to display in its own frame.<br>*canonical " +
         "meaning it is <i>expected</i> to be implemented by <u>all</u> " +
         "services",
         null, // method accepts no arguments
         new String[] { // return
            "javax.swing.JComponent",
            "A graphical component which can then be consolidated into " +
            "any container for viewing. <i><u>NB</u>:</i> The method " +
            "<i>may</i> return null, if the controller has no view."
         }, new String[][] { // exceptions
            {
               "java.io.IOException",
               "If the needed UI resource objects cannot be found"
            },
         }
      ));
      descriptors.add(new Descriptor("init",
         "This <i>canonical</i> method is invoked only once, by the " +
         "receiving service, on arrival.",
         new String[][] { // arguments
            {
               "gnu.cajo.Cajo",
               "The receiver's cajo reference, with wich the controller " +
               "may look up other services to fulfill its functionality, " +
               "if needed"
            },
         }, null,   // this method returns no value
         NOEXCEPTION // this method throws no exceptions, it's local
      ));
   }
   /** {@inheritDoc} */
   @Override
   public void init(@SuppressWarnings("hiding") gnu.cajo.Cajo cajo) {
      if (this.cajo != null)
         throw new IllegalStateException("controller already initialised");
      this.cajo = cajo;
   }
   /**
    * This method provides a means to identify this controller.
    * @return An identifier <i>(not a description)</i> of the controller
    */
   @Override
   public String toString() { return "AbstractController"; }
}
