package fr.upmc.datacenter.software.requestdispatcher.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ComponentI.ComponentService;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.datacenter.software.requestdispatcher.RequestDispatcher;
import fr.upmc.datacenter.software.requestdispatcher.interfaces.RequestDispatcherManagementI;


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
	public void connectVirtualMachine(String vmURI, String requestSubmissionInboundPortURI, String RequestSubmissionOutboundPortURI) throws Exception {
		
		final RequestDispatcherManagementI rdm = ( RequestDispatcherManagementI ) this.owner;
		
		this.owner.handleRequestSync(
				new ComponentI.ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						rdm.connectVirtualMachine(vmURI, requestSubmissionInboundPortURI, RequestSubmissionOutboundPortURI ) ;
						return null;
					}
				}) ;
	}

	@Override
	public void disconnectVirtualMachine(String vmURI) throws Exception {
		
		final RequestDispatcherManagementI rdm = ( RequestDispatcherManagementI ) this.owner;
		
		this.owner.handleRequestSync(
				new ComponentI.ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						rdm.disconnectVirtualMachine(vmURI);
						return null;
					}
				}) ;
	}

	@Override
	public void connectWithRequestGenerator(String rgURI, String requestNotificationInboundPortURI) throws Exception {

		final RequestDispatcherManagementI rdm = ( RequestDispatcherManagementI ) this.owner;
		
		this.owner.handleRequestSync(
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
	
	
}
