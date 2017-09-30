package fr.upmc.components.examples.ddeployment_cs.components;

//Copyright Jacques Malenfant, Univ. Pierre et Marie Curie.
//
//Jacques.Malenfant@lip6.fr
//
//This software is a computer program whose purpose is to provide a
//basic component programming model to program with components
//distributed applications in the Java programming language.
//
//This software is governed by the CeCILL-C license under French law and
//abiding by the rules of distribution of free software.  You can use,
//modify and/ or redistribute the software under the terms of the
//CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
//URL "http://www.cecill.info".
//
//As a counterpart to the access to the source code and  rights to copy,
//modify and redistribute granted by the license, users are provided only
//with a limited warranty  and the software's author,  the holder of the
//economic rights,  and the successive licensors  have only  limited
//liability. 
//
//In this respect, the user's attention is drawn to the risks associated
//with loading,  using,  modifying and/or developing or reproducing the
//software by the user in light of its specific status of free software,
//that may mean  that it is complicated to manipulate,  and  that  also
//therefore means  that it is reserved for developers  and  experienced
//professionals having in-depth computer knowledge. Users are therefore
//encouraged to load and test the software's suitability as regards their
//requirements in conditions enabling the security of their systems and/or 
//data to be ensured and,  more generally, to use and operate it in the 
//same conditions as regards security. 
//
//The fact that you are presently reading this means that you have had
//knowledge of the CeCILL-C license and that you accept its terms.

import java.util.concurrent.TimeUnit;
import fr.upmc.components.AbstractComponent;
import fr.upmc.components.examples.basic_cs.interfaces.URIConsumerI;
import fr.upmc.components.examples.basic_cs.ports.URIGetterOutboundPort;
import fr.upmc.components.examples.ddeployment_cs.components.interfaces.URIConsumerLaunchI;
import fr.upmc.components.examples.ddeployment_cs.components.ports.URIConsumerLaunchInboundPort;

/**
 * The class <code>DynamicURIConsumer</code> is the dynamically deployed
 * version of the component <code>URIConsumer</code> of the basic
 * client/server example.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	true
 * </pre>
 * 
 * <p>Created on : March 14, 2014</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @version	$Name$ -- $Revision$ -- $Date$
 */
public class			DynamicURIConsumer
extends		AbstractComponent
{
	// ------------------------------------------------------------------------
	// Constructors and instance variables
	// ------------------------------------------------------------------------

	protected final static int	N = 2 ;

	/**	the outbound port used to call the service.							*/
	protected URIGetterOutboundPort	uriGetterPort ;
	/**	counting service invocations.										*/
	protected int					counter ;

	/**
	 * 
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param uri
	 * @param outboundPortURI
	 * @throws Exception
	 */
	public				DynamicURIConsumer(
		String uri,
		String outboundPortURI,
		String lauchInboundPortURI
		) throws Exception
	{
		// the reflection inbound port URI is the URI of the component
		// no simple thread and one schedulable thread
		super(uri, 0, 1) ;

		this.counter = 0 ;

		this.addRequiredInterface(URIConsumerI.class) ;
		this.uriGetterPort =
				new URIGetterOutboundPort(outboundPortURI, this) ;
		// add the port to the set of ports of the component
		this.addPort(this.uriGetterPort) ;
		// publish the port (an outbound port is always local)
		this.uriGetterPort.localPublishPort() ;

		this.addOfferedInterface(URIConsumerLaunchI.class) ;
		URIConsumerLaunchInboundPort p =
				new URIConsumerLaunchInboundPort(lauchInboundPortURI, this) ;
		this.addPort(p) ;
		p.publishPort() ;
	}

	public void			getURIandPrint() throws Exception
	{
		this.counter++ ;
		if (this.counter <= 10) {
			// Get the next URI and print it
			this.logMessage("consumer getting a new URI.") ;
			String uri = this.uriGetterPort.getURI() ;
			System.out.println("URI no " + this.counter + ": " + uri) ;

			// Get a set of new URIs and print them
			this.logMessage("consumer getting a new set of URIs.") ;
			String[] uris = this.uriGetterPort.getURIs(DynamicURIConsumer.N) ;
			System.out.print("URI set no " + this.counter + " [") ;
			for (int i = 0 ; i < DynamicURIConsumer.N ; i++) {
				System.out.print(uris[i]) ;
				if (i < DynamicURIConsumer.N - 1) {
					System.out.print(", ") ;
				}
			}
			System.out.println("]") ;

			// Schedule the next service method invocation in one second.
			// All tasks and services of a component must be called through
			// the methods for running tasks and handling requests.  These
			// methods (from the CVM) handles the internal concurrency of
			// the component when required, and therefore ensure their good
			// properties (like synchronisation).
			final DynamicURIConsumer uc = this ;
			this.scheduleTask(
					new ComponentTask() {
						@Override
						public void run() {
							try {
								uc.getURIandPrint() ;
							} catch (Exception e) {
								throw new RuntimeException(e) ;
							}
						}
					},
					1000, TimeUnit.MILLISECONDS) ;
		}
	}
}
