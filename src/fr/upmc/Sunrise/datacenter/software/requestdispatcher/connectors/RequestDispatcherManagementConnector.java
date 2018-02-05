package fr.upmc.Sunrise.datacenter.software.requestdispatcher.connectors;

import fr.upmc.Sunrise.datacenter.software.requestdispatcher.interfaces.RequestDispatcherManagementI;
import fr.upmc.components.connectors.AbstractConnector;

/**
 * The class <code>RequestDispatcherManagementConnector</code> implements a
 * connector for ports exchanging through the interface
 * <code>RequestDispatcherManagementI</code>.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	true
 * </pre>
 * 
 * @author	Loïc Lafontaine
 * @author	Maxime Lavaste
 */

public class				RequestDispatcherManagementConnector
extends		AbstractConnector
implements	RequestDispatcherManagementI
{
	@Override
	public void connectVirtualMachine(String vmURI, String requestSubmissionInboundPortURI) throws Exception {
		( ( RequestDispatcherManagementI ) this.offering ).connectVirtualMachine(vmURI, requestSubmissionInboundPortURI);
	}

	@Override
	public void askVirtualMachineDisconnection(String vmURI) throws Exception {
		( ( RequestDispatcherManagementI ) this.offering ).askVirtualMachineDisconnection(vmURI);
		
	}

	@Override
	public void connectWithRequestGenerator(String rgURI, String requestNotificationInboundPortURI) throws Exception {
		( ( RequestDispatcherManagementI ) this.offering ).connectWithRequestGenerator(rgURI, requestNotificationInboundPortURI);
	}

	@Override
	public void disconnectRequestGenerator() throws Exception {
		( ( RequestDispatcherManagementI ) this.offering ).disconnectRequestGenerator();
	}

	@Override
	public void connectController(String controllerURI, String VMDisconnectionHandlerInboundPortURI) throws Exception {
		( ( RequestDispatcherManagementI ) this.offering ).connectController(controllerURI,VMDisconnectionHandlerInboundPortURI);
	}

	@Override
	public void disconnectController() throws Exception {
		( ( RequestDispatcherManagementI ) this.offering ).disconnectController();
	}
} 
