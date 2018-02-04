package fr.upmc.PriseTheSun.datacenter.software.controller.interfaces;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;
/**
 * The interface <code>NodeRingManagementI</code> defines the methods
 * to manage a node in a network ring.
 *
 * <p><strong>Description</strong></p>
 * Chaque élément d'un DataRing Network doit implémenter cette interface.
 * @author	Maxime LAVASTE Lo�c Lafontaine
 */
public interface NodeRingManagementI extends OfferedI,RequiredI{
	
	/**
	 * Modifie l'uri du port de reception
	 * @param DataInboundPortUri nouveau controller a envoyer le <code>RingDynamicState</code>
	 * @throws Exception
	 */
	public void bindSendingDataUri(String DataInboundPortUri) throws Exception;
	/**
	 * Change the next management inbound port uri
	 * @param managementInboundPort new uri
	 * @throws Exception
	 */
	public void setNextManagementInboundPort(String managementInboundPort ) throws Exception;
	
	/**
	 * Change the previous management inbound port uri
	 * @param managementInboundPort new uri
	 * @throws Exception
	 */
	public void setPreviousManagementInboundPort(String managementInboundPort) throws Exception;
	
	public void stopPushing() throws Exception;
	
	public void startPushing() throws Exception;
	
	/**
	 * Ask the disconnection of the dataring inbound port
	 * @throws Exception
	 */
	public void doDisconnectionInboundPort()  throws Exception;

}
