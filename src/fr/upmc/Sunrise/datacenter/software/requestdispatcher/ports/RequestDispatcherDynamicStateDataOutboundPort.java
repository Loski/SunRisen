package fr.upmc.Sunrise.datacenter.software.requestdispatcher.ports;

import fr.upmc.Sunrise.datacenter.software.requestdispatcher.interfaces.RequestDispatcherDynamicStateI;
import fr.upmc.Sunrise.datacenter.software.requestdispatcher.interfaces.RequestDispatcherStateDataConsumerI;
import fr.upmc.components.ComponentI;
import fr.upmc.components.interfaces.DataRequiredI;
import fr.upmc.datacenter.ports.AbstractControlledDataOutboundPort;

public class RequestDispatcherDynamicStateDataOutboundPort extends AbstractControlledDataOutboundPort{

	// ------------------------------------------------------------------------
	// Constants and instance variables
	// ------------------------------------------------------------------------

	private static final long	serialVersionUID = 1L;
	protected String			rdURI ;

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	public				RequestDispatcherDynamicStateDataOutboundPort(
		ComponentI owner,
		String rdURI
		) throws Exception
	{
		super(owner) ;
		this.rdURI = rdURI ;

		assert owner instanceof RequestDispatcherStateDataConsumerI ;
	}

	public				RequestDispatcherDynamicStateDataOutboundPort(
		String uri,
		ComponentI owner,
		String rdURI
		) throws Exception
	{
		super(uri, owner) ;
		this.rdURI = rdURI ;

		assert owner instanceof RequestDispatcherStateDataConsumerI ;
	}

	// ------------------------------------------------------------------------
	// Methods
	// ------------------------------------------------------------------------

	/**
	 * @see fr.upmc.components.interfaces.DataRequiredI.PushI#receive(fr.upmc.components.interfaces.DataRequiredI.DataI)
	 */
	@Override
	public void			receive(final DataRequiredI.DataI d) throws Exception
	{
		final RequestDispatcherStateDataConsumerI rdsdc =
									(RequestDispatcherStateDataConsumerI) this.owner ;
		final String uri = this.rdURI ;
		this.owner.handleRequestAsync(
						new ComponentI.ComponentService<Void>() {
							@Override
							public Void call() throws Exception {
								rdsdc.acceptRequestDispatcherDynamicData(uri,((RequestDispatcherDynamicStateI)d));
								return null;
							}
						}) ;
	}

}
