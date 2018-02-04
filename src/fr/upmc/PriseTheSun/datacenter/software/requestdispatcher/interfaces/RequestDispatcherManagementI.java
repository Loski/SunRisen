package fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;

/**
 * The interface <code>RequestDispatcherManagementI</code> defines the methods
 * to manage a dispatcher component.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * @author	Loïc Lafontaine
 * @author	Maxime Lavaste
 */
public interface			RequestDispatcherManagementI
extends		OfferedI,
			RequiredI
{
	/**Connect the submissionOutbound port of the connector to the submissionInboundPort of the vm
	 * 
	 * @param vmURI uri of the vm
	 * @param requestSubmissionInboundPortURI uri of the vm requestSubmissionInboundPortURI
	 * @throws Exception
	 */
	public void connectVirtualMachine(String vmURI, String requestSubmissionInboundPortURI) throws Exception;
    
	/**
     * Close the outbound port.
     * @throws Exception
     */
	public void askVirtualMachineDisconnection(String vmURI) throws Exception;
	/**
	 * Connect the outbound port of dispatcher to the inbound port of the generator
	 * @param rgURI request generator URI
	 * @param requestNotificationInboundPortURI URI of the requestNotificationInboundPortURI of rg
	 * @throws Exception
	 */
	public void connectWithRequestGenerator(String rgURI, String requestNotificationInboundPortURI) throws Exception;
	/**
	 * 
	 * @throws Exception
	 */
	public void disconnectRequestGenerator() throws Exception;
	
	public void connectController(String controllerURI, String VMDisconnectionHandlerInboundPortURI) throws Exception;
	
	public void disconnectController() throws Exception;
	
	
	
}
