package fr.upmc.PriseTheSun.datacenter.software.controller.interfaces;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;
/**
 * Interface de notification entre un controller et un dispatcher.
 * @author maxim
 *
 */
public interface VMDisconnectionNotificationHandlerI extends		OfferedI,
RequiredI{
	/**
	 * Notification d'une déconnexion d'une VM d'un dispatcher.
	 * @param vmURI uri de la VM déconnectée
	 * @throws Exception
	 */
	public void receiveVMDisconnectionNotification(String vmURI) throws Exception;
	
	/**
	 * Notification d'une déconnexion imminente d'un dispatcher. Le dispatcher demande au controller de se déconnecter
	 *  ainsi que se supprimer du data ring network.
	 * @throws Exception
	 */
	public void disconnectController() throws Exception;
}
