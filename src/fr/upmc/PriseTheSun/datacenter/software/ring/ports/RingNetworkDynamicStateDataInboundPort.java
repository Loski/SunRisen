package fr.upmc.PriseTheSun.datacenter.software.ring.ports;

import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.RequestDispatcher;
import fr.upmc.components.ComponentI;
import fr.upmc.components.interfaces.DataOfferedI;
import fr.upmc.datacenter.ports.AbstractControlledDataInboundPort;
/**
* The class <code>RingDynamicStateDataInboundPort</code> implements the
 * inbound port through which the component management methods are called.
* 
* <p>Created on : 2016-2017</p>
* 
* @author	Maxime Lavaste and Loïc Lafontaine
*/
public class RingNetworkDynamicStateDataInboundPort extends AbstractControlledDataInboundPort{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public				RingNetworkDynamicStateDataInboundPort(
		ComponentI owner
		) throws Exception
	{
		super(owner);
		//assert owner instanceof RequestDispatcher ;
	}

	public				RingNetworkDynamicStateDataInboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, owner);

		//assert owner instanceof RequestDispatcher ;
	}

	/**
	 * @see fr.upmc.components.interfaces.DataOfferedI.PullI#get()
	 */
	@Override
	public DataOfferedI.DataI	get() throws Exception
	{
		final RequestDispatcher rd = (RequestDispatcher) this.owner ;
		return rd.handleRequestSync(
					new ComponentI.ComponentService<DataOfferedI.DataI>() {
						@Override
						public DataOfferedI.DataI call() throws Exception {
							return rd.getDynamicState() ;
						}
					});
	}
}
