package fr.upmc.datacenter.software.requestdispatcher.interfaces;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;

public interface			RequestDispatcherManagementI
extends		OfferedI,
			RequiredI
{
	public void connectVirtualMachine(String vmURI, String requestSubmissionInboundPortURI) throws Exception;
    
	public void disconnectVirtualMachine() throws Exception;
	
	public void connectWithRequestGenerator(String rgURI, String requestNotificationInboundPortURI) throws Exception;
	
	public void disconnectRequestGenerator() throws Exception;
}
