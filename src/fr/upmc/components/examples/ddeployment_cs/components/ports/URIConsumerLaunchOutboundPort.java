package fr.upmc.components.examples.ddeployment_cs.components.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.examples.ddeployment_cs.components.interfaces.URIConsumerLaunchI;
import fr.upmc.components.ports.AbstractOutboundPort;

/**
 * The class <code>URIConsumerLauchOutboundPort</code>
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	true
 * </pre>
 * 
 * <p>Created on : February 22, 2017</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @version	$Name$ -- $Revision$ -- $Date$
 */
public class			URIConsumerLaunchOutboundPort
extends		AbstractOutboundPort
implements	URIConsumerLaunchI
{
	public				URIConsumerLaunchOutboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, URIConsumerLaunchI.class, owner) ;
	}

	public				URIConsumerLaunchOutboundPort(
		ComponentI owner
		) throws Exception
	{
		super(URIConsumerLaunchI.class, owner);
	}

	/**
	 * @see fr.upmc.components.examples.ddeployment_cs.components.interfaces.URIConsumerLaunchI#getURIandPrint()
	 */
	@Override
	public void			getURIandPrint() throws Exception
	{
		((URIConsumerLaunchI)this.connector).getURIandPrint() ;
	}
}
