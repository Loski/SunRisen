package fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.connectors;

import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces.RequestDispatcherManagementI;
import fr.upmc.components.connectors.AbstractConnector;



public class				RequestDispatcherManagementConnector
extends		AbstractConnector
implements	RequestDispatcherManagementI
{
	@Override
	public void connectVirtualMachine(String vmURI, String requestSubmissionInboundPortURI) throws Exception {
		( ( RequestDispatcherManagementI ) this.offering ).connectVirtualMachine(vmURI, requestSubmissionInboundPortURI);
	}

	@Override
	public void disconnectVirtualMachine(String vmURI) throws Exception {
		( ( RequestDispatcherManagementI ) this.offering ).disconnectVirtualMachine(vmURI);
		
	}

	@Override
	public void connectWithRequestGenerator(String rgURI, String requestNotificationInboundPortURI) throws Exception {
		// TODO Auto-generated method stub
		( ( RequestDispatcherManagementI ) this.offering ).connectWithRequestGenerator(rgURI, requestNotificationInboundPortURI);
	}

	@Override
	public void disconnectRequestGenerator() throws Exception {
		// TODO Auto-generated method stub
		( ( RequestDispatcherManagementI ) this.offering ).disconnectRequestGenerator();
	}
} 
