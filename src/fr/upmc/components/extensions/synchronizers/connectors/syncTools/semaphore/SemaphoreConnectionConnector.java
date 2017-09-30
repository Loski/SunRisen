package fr.upmc.components.extensions.synchronizers.connectors.syncTools.semaphore;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.components.extensions.synchronizers.interfaces.syncTools.semaphore.SemaphoreConnectionClientI;
import fr.upmc.components.extensions.synchronizers.interfaces.syncTools.semaphore.SemaphoreConnectionI;

public class SemaphoreConnectionConnector 
extends AbstractConnector
implements SemaphoreConnectionClientI
{

	/**
	 * @see fr.upmc.components.extensions.synchronizers.interfaces.syncTools.semaphore.SemaphoreConnectionClientI#getOwnPortURI()
	 */
	@Override
	public String getOwnPortURI() throws Exception {
		return ((SemaphoreConnectionI)this.offering).getOwnPortURI();
	}

}
