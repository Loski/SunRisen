package fr.upmc.Sunrise.datacenterclient.software.applicationprovider.interfaces;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionI;

/**
 * <code>ApplicationProviderManagementI </code> defines methods for the
 * management of an application such create, send or stop an application.
 * @author maxime LAVASTE Loïc Lafontaine
 *
 */
public interface ApplicationProviderManagementI extends	OfferedI, RequiredI{

	/**
	 * Envoie une application à l'admission controller.
	 * Si aucune erreur, créer un <code>RequestGenerator</code>
	 * @throws Exception
	 */
	public void createAndSendApplication() throws Exception;
	
	/**
	 * Stop une application déjà créée.
	 */
	public void stopApplication() throws Exception;

	/**
     * @see fr.upmc.Sunrise.datacenterclient.software.applicationprovider.interfaces.ApplicationSubmissionI#submitApplication(java.lang.String, int)
	 * @param Class to be recompiled by Javassist
	 * @throws Exception
	 */
	public void createAndSendApplication(Class<RequestSubmissionI> class1) throws Exception;
}
