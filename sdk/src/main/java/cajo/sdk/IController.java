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
 * This interface defines the canonical methods to be furnished by <i>all</i>
 * <a href=https://cajo.dev.java.net>cajo</a> controllers. As a receiving
 * object needs to know the controller's interface, or at least part of it;
 * typically receivers need to be custom-built to handle specific types of
 * controllers. <i>(much as to use its sending service)</i>
 * <p>Controllers may be passed between services, however, they would
 * normally need to be sent wrapped in a <tt>java.rmi.MarshalledObject.</tt>
 * The receiver would then call the <tt>get()</tt> method, to extract the
 * controller object. This is necessary to preserve the information necessary
 * for the client to find the needed remote class definitions.
 * <p>In addition to the effort for defining a good quality API, similar
 * effort should be put into the javadoc. Develop it from the viewpoint that
 * the controller interface documentation is all a client should need, to
 * fully understand how to use the service, via its controller.
 * <p><i><u>NB</u>:</i> This client-facing interface need not bear <i>any</i>
 * resemblance to the service object it represents, as it can provide
 * composite functionality for its client.
 * @author John Catherino
 */
public interface IController {
   /**
    * This method is called by clients, to get a standardised html encoded
    * destription of the controller's functions. The format is invariant,
    * to allow automated parsing, if desired.
    * @return A detailed description of the functionality of the controller
    */
   String getDescription();
   /**
    * This method is normally called by a graphical client to get the default
    * view component associated with this controller, to display in its own
    * frame. <br><i><u>NB</u>:</i> A controller may have multiple views,
    * furnished by other methods unspecified here.
    * @return A graphical component which can then be consolidated into any
    * container for viewing.<br><i><u>NB</u>:</i>
    * The method <i>may</i> return null, if the controller has no view. Whilst
    * permitted, doing this <i>will</i> mess up use by the Applet/WebStart
    * {@link Client Client}, which <i>must</i> assume the controller has a
    * default view.
    * @throws java.io.IOException If the necessary UI resource files
    * cannot be found.
    */
   javax.swing.JComponent getView() throws java.io.IOException;
   /**
    * This method is invoked only once, by the receiving service upon arrival.
    * It can be overridden to perform additional initialisation. However, if
    * this method is overridden, be sure to first call
    * super.init(cajo);. This method will <i>not</i> be called by the
    * applet/WebsStart {@link Client Client}, as it has no cajo object.
    * <br><i><u>NB</u>:</i> When the Client is running as an application,
    * it <i>will</i> provide the controller with a cajo object, as a special
    * feature.
    * @param cajo The receiver's cajo reference, with which the controller
    * may look up other services to fulfill its functionality, if needed
    * @throws IllegalStateException If the controller has already been
    * initialised
    */
   void init(gnu.cajo.Cajo cajo);
}
