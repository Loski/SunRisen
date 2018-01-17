package fr.upmc.PriseTheSun.datacenter.software.ring.ports;

import fr.upmc.PriseTheSun.datacenter.software.ring.interfaces.RingDataI;
import fr.upmc.PriseTheSun.datacenter.software.ring.interfaces.RingDynamicStateI;
import fr.upmc.components.ComponentI;
import fr.upmc.components.interfaces.DataRequiredI;
import fr.upmc.datacenter.hardware.computers.interfaces.ComputerStateDataConsumerI;
import fr.upmc.datacenter.hardware.processors.interfaces.ProcessorDynamicStateI;
import fr.upmc.datacenter.hardware.processors.interfaces.ProcessorStateDataConsumerI;
import fr.upmc.datacenter.ports.AbstractControlledDataOutboundPort;

/**
* The class <code>RingDynamicStateDataOutboundPort</code> implements the
 * outbound port through which the component management methods are called.
* 
* 
* @author	Maxime Lavaste and Loïc Lafontaine
*/
public class RingDynamicStateDataOutboundPort extends AbstractControlledDataOutboundPort{
	private static final long serialVersionUID = 1L;
	protected String			ringURI ;

	public				RingDynamicStateDataOutboundPort(
			ComponentI owner,
			String ringURI
			) throws Exception
	{
		super(owner) ;
		this.ringURI = ringURI ;

		assert	owner instanceof ComputerStateDataConsumerI ;
	}

	public				RingDynamicStateDataOutboundPort(
			String uri,
			ComponentI owner,
			String ringURI
			) throws Exception
	{
		super(uri, owner);
		this.ringURI = ringURI ;

		assert	owner instanceof ComputerStateDataConsumerI ;
	}

	/**
	 * @see fr.upmc.components.interfaces.DataRequiredI.PushI#receive(fr.upmc.components.interfaces.DataRequiredI.DataI)
	 */
	public void	receive(final DataRequiredI.DataI d) throws Exception
	{
		final RingDataI psdc = (RingDataI) this.owner ;
		final String uri = this.ringURI ;
		this.owner.handleRequestAsync(
						new ComponentI.ComponentService<Void>() {
							@Override
							public Void call() throws Exception {
								psdc.acceptRingDynamicData(uri, (RingDynamicStateI) d);
								return null;
							}
						}) ;
	}
}
