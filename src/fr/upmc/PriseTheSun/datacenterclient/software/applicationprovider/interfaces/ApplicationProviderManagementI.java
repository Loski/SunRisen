package fr.upmc.PriseTheSun.datacenterclient.software.applicationprovider.interfaces;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionI;

/**
 * <code>ApplicationProviderManagementI </code> defines methods for the
 * management of an application such create, send or stop an application.
 * @author maxime LAVASTE Lo�c Lafontaine
 *
 */
public interface ApplicationProviderManagementI extends	OfferedI, RequiredI{

	/**
	 * Send an application to the administration Controller.
	 * If success, create and deploy an request generator to simulate data.
	 * @throws Exception
	 */
	public void createAndSendApplication() throws Exception;
	
	/**
	 * Stop an application already created.
	 */
	public void stopApplication() throws Exception;

	/**
	 * Send an application to the administration Controller.
	 * If success, create and deploy an request generator to simulate data.
	 * @param Class to be recompiled by Javassist
	 * @throws Exception
	 */
	public void createAndSendApplication(Class<RequestSubmissionI> class1) throws Exception;
}
