package fr.upmc.components.examples.ddeployment_cs.components.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.examples.ddeployment_cs.components.DynamicURIConsumer;
import fr.upmc.components.examples.ddeployment_cs.components.interfaces.URIConsumerLaunchI;
import fr.upmc.components.ports.AbstractInboundPort;

/**
 * The class <code>URIConsumerInboundPort</code>
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
public class		URIConsumerLaunchInboundPort
extends		AbstractInboundPort
implements	URIConsumerLaunchI
{
	private static final long serialVersionUID = 1L;

	public			URIConsumerLaunchInboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, URIConsumerLaunchI.class, owner) ;
		assert	owner instanceof DynamicURIConsumer ;
	}

	public URIConsumerLaunchInboundPort(ComponentI owner) throws Exception
	{
		super(URIConsumerLaunchI.class, owner);
		assert	owner instanceof DynamicURIConsumer ;
	}

	@Override
	public void		getURIandPrint() throws Exception
	{
		((DynamicURIConsumer)this.owner).getURIandPrint() ;
	}
}
