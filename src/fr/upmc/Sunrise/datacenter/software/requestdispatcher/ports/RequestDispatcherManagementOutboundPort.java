package fr.upmc.Sunrise.datacenter.software.requestdispatcher.ports;

import fr.upmc.Sunrise.datacenter.software.requestdispatcher.interfaces.RequestDispatcherManagementI;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;

public class				RequestDispatcherManagementOutboundPort 
extends		AbstractOutboundPort
implements	RequestDispatcherManagementI
{
	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	public				RequestDispatcherManagementOutboundPort(
		ComponentI owner
		) throws Exception
	{
		super(RequestDispatcherManagementI.class, owner) ;
	}

	public				RequestDispatcherManagementOutboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, RequestDispatcherManagementI.class, owner);
	}
	
	@Override
	public void connectVirtualMachine(String vmURI, String requestSubmissionInboundPortURI) throws Exception {
		( ( RequestDispatcherManagementI ) this.connector ).connectVirtualMachine(vmURI, requestSubmissionInboundPortURI);

	}

	@Override
	public void askVirtualMachineDisconnection(String vmURI) throws Exception {
		( ( RequestDispatcherManagementI ) this.connector ).askVirtualMachineDisconnection(vmURI);

}

	@Override
	public void connectWithRequestGenerator(String rgURI, String requestNotificationInboundPortURI) throws Exception {
		( ( RequestDispatcherManagementI ) this.connector ).connectWithRequestGenerator(rgURI, requestNotificationInboundPortURI);
		
	}

	@Override
	public void disconnectRequestGenerator() throws Exception {

		( ( RequestDispatcherManagementI ) this.connector ).disconnectRequestGenerator();
	}

	@Override
	public void connectController(String controllerURI, String VMDisconnectionHandlerInboundPortURI) throws Exception {
		( ( RequestDispatcherManagementI ) this.connector ).connectController(controllerURI, VMDisconnectionHandlerInboundPortURI);
	}

	@Override
	public void disconnectController() throws Exception {
		( ( RequestDispatcherManagementI ) this.connector ).disconnectController();
	}

}
