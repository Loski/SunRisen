package fr.upmc.PriseTheSun.datacenter.software.controller.interfaces;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;
/**
 * The interface <code>ControllerManagementI</code> defines the methods
 * to manage the Controller.
 *
 * <p><strong>Description</strong></p>
 * 
 * 
 * @author	Maxime LAVASTE Lo�c Lafontaine
 */
public interface NodeRingManagementI extends OfferedI,RequiredI{
	
	/**
	 * Modifie l'uri du port de reception
	 * @param DataInboundPortUri nouveau controller a envoyer le <code>RingDynamicState</code>
	 * @throws Exception
	 */
	public void bindSendingDataUri(String DataInboundPortUri) throws Exception;
	public void setNextManagementInboundPort(String managementInboundPort ) throws Exception;
	public void setPreviousManagementInboundPort(String managementInboundPort) throws Exception;
	public void stopPushing() throws Exception;
	public void startPushing() throws Exception;
	public void doDisconnectionInboundPort()  throws Exception;

}
