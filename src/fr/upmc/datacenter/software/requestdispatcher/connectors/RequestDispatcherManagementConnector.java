package fr.upmc.datacenter.software.requestdispatcher.connectors;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;
import fr.upmc.datacenter.software.applicationvm.interfaces.ApplicationVMManagementI;
import fr.upmc.datacenter.software.requestdispatcher.interfaces.RequestDispatcherManagementI;
import fr.upmc.datacenterclient.requestgenerator.interfaces.RequestGeneratorManagementI;


public class				RequestDispatcherManagementConnector
extends		AbstractConnector
implements	RequestDispatcherManagementI
{
	@Override
	public void connectVirtualMachine(String vmURI, String requestSubmissionInboundPortURI) throws Exception {
		( ( RequestDispatcherManagementI ) this.offering ).connectVirtualMachine(vmURI, requestSubmissionInboundPortURI);
	}

	@Override
	public void disconnectVirtualMachine() throws Exception {
		( ( RequestDispatcherManagementI ) this.offering ).disconnectVirtualMachine();
		
	}

	@Override
	public void connectWithRequestGenerator(String rgURI, String requestNotificationInboundPortURI) throws Exception {
		// TODO Auto-generated method stub
		( ( RequestDispatcherManagementI ) this.offering ).connectWithRequestGenerator(rgURI, requestNotificationInboundPortURI);
	}

	@Override
	public void disconnectRequestGenerator() throws Exception {
		// TODO Auto-generated method stub
		
	}
}
