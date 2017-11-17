package fr.upmc.datacenterclient.applicationprovider.interfaces;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;

/**
 * <code>ApplicationProviderManagementI </code> defines methods for the
 * management of an application such create, send or stop an application.
 * @author maxime LAVASTE Loïc Lafontaine
 *
 */
public interface ApplicationProviderManagementI extends	OfferedI, RequiredI{

	/**
	 * Send an application to the administration Controller.
	 * If success, create and deploy an request generator to simulate data.
	 */
	public void createAndSendApplication() throws Exception;
	
	/**
	 * Stop an application already created.
	 */
	public void stopApplication() throws Exception;
}
