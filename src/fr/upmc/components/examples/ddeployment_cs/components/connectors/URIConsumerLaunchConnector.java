package fr.upmc.components.examples.ddeployment_cs.components.connectors;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.components.examples.ddeployment_cs.components.interfaces.URIConsumerLaunchI;

/**
 * The class <code>URIConsumerLaunchConnector</code>
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	true
 * </pre>
 * 
 * <p>Created on : 22 févr. 2017</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @version	$Name$ -- $Revision$ -- $Date$
 */
public class			URIConsumerLaunchConnector
extends		AbstractConnector
implements	URIConsumerLaunchI
{
	/**
	 * @see fr.upmc.components.examples.ddeployment_cs.components.interfaces.URIConsumerLaunchI#getURIandPrint()
	 */
	@Override
	public void			getURIandPrint() throws Exception
	{
		((URIConsumerLaunchI)this.offering).getURIandPrint() ;
	}
}
