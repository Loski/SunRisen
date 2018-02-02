package fr.upmc.PriseTheSun.datacenter.software.ring.ports;

import fr.upmc.PriseTheSun.datacenter.software.ring.interfaces.RingNetworkDynamicStateI;
import fr.upmc.PriseTheSun.datacenter.software.ring.interfaces.RingNetworkStateDataConsumerI;
import fr.upmc.components.ComponentI;
import fr.upmc.components.interfaces.DataRequiredI;
import fr.upmc.datacenter.hardware.computers.interfaces.ComputerStateDataConsumerI;
import fr.upmc.datacenter.ports.AbstractControlledDataOutboundPort;

/**
* The class <code>RingDynamicStateDataOutboundPort</code> implements the
 * outbound port through which the component management methods are called.
* 
* 
* @author	Maxime Lavaste and Loïc Lafontaine
*/
public class RingNetworkDynamicStateDataOutboundPort extends AbstractControlledDataOutboundPort{
	private static final long serialVersionUID = 1L;
	protected String			ringURI ;

	public RingNetworkDynamicStateDataOutboundPort(
			ComponentI owner,
			String ringURI
			) throws Exception
	{
		super(owner) ;
		this.ringURI = ringURI ;

		assert	owner instanceof RingNetworkStateDataConsumerI ;
	}

	public RingNetworkDynamicStateDataOutboundPort(
			String uri,
			ComponentI owner,
			String ringURI
			) throws Exception
	{
		super(uri, owner);
		this.ringURI = ringURI ;

		assert	owner instanceof RingNetworkStateDataConsumerI ;
	}

	/**
	 * @see fr.upmc.components.interfaces.DataRequiredI.PushI#receive(fr.upmc.components.interfaces.DataRequiredI.DataI)
	 */
	public void	receive(final DataRequiredI.DataI d) throws Exception
	{
		final RingNetworkStateDataConsumerI psdc = (RingNetworkStateDataConsumerI) this.owner ;
		final String uri = this.ringURI ;
		this.owner.handleRequestAsync(
						new ComponentI.ComponentService<Void>() {
							@Override
							public Void call() throws Exception {
								psdc.acceptRingNetworkDynamicData(uri, (RingNetworkDynamicStateI) d);
								return null;
							}
						}) ;
	}
}
