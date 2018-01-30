package fr.upmc.PriseTheSun.datacenter.software.controller.interfaces;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;

public interface VMDisconnectionNotificationHandlerI extends		OfferedI,
RequiredI{

	public void receiveVMDisconnectionNotification(String vmURI) throws Exception;
	public void disconnectController() throws Exception;
}
