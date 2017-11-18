package fr.upmc.datacenterclient.applicationprovider.interfaces;

import fr.upmc.datacenter.hardware.tests.TestApplicationVM.Request;
import fr.upmc.datacenter.software.connectors.RequestSubmissionConnector;
import fr.upmc.datacenter.software.ports.RequestSubmissionInboundPort;
import fr.upmc.datacenterclient.requestgenerator.ports.RequestGeneratorManagementInboundPort;
/**
 * This interface define the interaction to give an aplication to the administrationController.
 * @author maxim
 *
 */
public interface ApplicationSubmissionI {
	/**
	 * Send an app to the administrationControler.
	 * The Controller checks if he can accept this application
	 * @param appURI Uri of the Application
	 * @param nbVM VM require for the application
	 * @return The requestDispatcherUri if success with his inboundPort.
	 * @throws Exception
	 */
	String[] submitApplication(String appURI, int nbVM) throws Exception;
	
	String[] submitApplication(String appURI, int nbVM, Class submissionInterface) throws Exception;
	
	/**
	 * Submit a generator to the administration controller.
	 * submitApplication need to be called before. 
	 * @param requestNotificationInboundPort uri of the requestNotificationInboundPort of the requestGenerator.
	 * @param appURI Uri of the application.
	 * @param rgURI	URI of the requestGenerator.
	 * @throws Exception
	 */
	void submitGenerator(String requestNotificationInboundPort, String appURI, String rgURI) throws Exception;
}
