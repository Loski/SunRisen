package fr.upmc.datacenter.software.requestdispatcher.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.datacenter.software.requestdispatcher.interfaces.RequestDispatcherManagementI;

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
	public void connectVirtualMachine(String vmURI, String requestSubmissionInboundPortURI, String RequestSubmissionOutboundPortURI) throws Exception {
		( ( RequestDispatcherManagementI ) this.connector ).connectVirtualMachine(vmURI, requestSubmissionInboundPortURI,RequestSubmissionOutboundPortURI);

	}

	@Override
	public void disconnectVirtualMachine(String vmURI) throws Exception {
		( ( RequestDispatcherManagementI ) this.connector ).disconnectVirtualMachine(vmURI);

}

	@Override
	public void connectWithRequestGenerator(String rgURI, String requestNotificationInboundPortURI) throws Exception {
		( ( RequestDispatcherManagementI ) this.connector ).connectWithRequestGenerator(rgURI, requestNotificationInboundPortURI);
		
	}

	@Override
	public void disconnectRequestGenerator() throws Exception {

		( ( RequestDispatcherManagementI ) this.connector ).disconnectRequestGenerator();
	}

}
