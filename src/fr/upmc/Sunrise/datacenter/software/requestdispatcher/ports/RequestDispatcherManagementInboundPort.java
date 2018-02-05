package fr.upmc.Sunrise.datacenter.software.requestdispatcher.ports;

import fr.upmc.Sunrise.datacenter.software.requestdispatcher.interfaces.RequestDispatcherManagementI;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;



public class			RequestDispatcherManagementInboundPort
extends		AbstractInboundPort
implements	RequestDispatcherManagementI
{
	private static final long serialVersionUID = 1L;

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	public				RequestDispatcherManagementInboundPort(
		ComponentI owner
		) throws Exception
	{
		super(RequestDispatcherManagementI.class, owner) ;

		assert	owner instanceof RequestDispatcherManagementI ;
	}

	public				RequestDispatcherManagementInboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, RequestDispatcherManagementI.class, owner);

		assert	uri != null && owner instanceof RequestDispatcherManagementI ;
	}

	@Override
	public void connectVirtualMachine(final String vmURI, final String requestSubmissionInboundPortURI) throws Exception {
		
		final RequestDispatcherManagementI rdm = ( RequestDispatcherManagementI ) this.owner;
		
		this.owner.handleRequestAsync(
				new ComponentI.ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						rdm.connectVirtualMachine(vmURI, requestSubmissionInboundPortURI ) ;
						return null;
					}
				}) ;
	}

	@Override
	public void askVirtualMachineDisconnection(final String vmURI) throws Exception {
		
		final RequestDispatcherManagementI rdm = ( RequestDispatcherManagementI ) this.owner;
		
		this.owner.handleRequestAsync(
				new ComponentI.ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						rdm.askVirtualMachineDisconnection(vmURI);
						return null;
					}
				}) ;
	}

	@Override
	public void connectWithRequestGenerator(final String rgURI, final String requestNotificationInboundPortURI) throws Exception {

		final RequestDispatcherManagementI rdm = ( RequestDispatcherManagementI ) this.owner;
		
		this.owner.handleRequestAsync(
				new ComponentI.ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						rdm.connectWithRequestGenerator(rgURI, requestNotificationInboundPortURI) ;
						return null;
					}
				}) ;
	}

	@Override
	public void disconnectRequestGenerator() throws Exception {

		final RequestDispatcherManagementI rdm = ( RequestDispatcherManagementI ) this.owner;
		
		this.owner.handleRequestSync(
				new ComponentI.ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						rdm.disconnectRequestGenerator();
						return null;
					}
				}) ;
	}

	@Override
	public void connectController(final String controllerURI, final String VMDisconnectionHandlerInboundPortURI) throws Exception {
		final RequestDispatcherManagementI rdm = ( RequestDispatcherManagementI ) this.owner;
		
		this.owner.handleRequestAsync(
				new ComponentI.ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						rdm.connectController(controllerURI, VMDisconnectionHandlerInboundPortURI) ;
						return null;
					}
				}) ;
	}

	@Override
	public void disconnectController() throws Exception {
		final RequestDispatcherManagementI rdm = ( RequestDispatcherManagementI ) this.owner;
		
		this.owner.handleRequestAsync(
				new ComponentI.ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						rdm.disconnectController();
						return null;
					}
				}) ;
	}
	
	
}
