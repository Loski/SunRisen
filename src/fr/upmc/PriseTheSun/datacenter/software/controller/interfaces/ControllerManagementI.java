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
public interface ControllerManagementI extends OfferedI,RequiredI{
	public void bindSendingDataUri(String DataInboundPortUri) throws Exception;
}
