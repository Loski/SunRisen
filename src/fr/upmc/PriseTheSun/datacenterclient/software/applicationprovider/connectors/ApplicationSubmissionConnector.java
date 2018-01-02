package fr.upmc.PriseTheSun.datacenterclient.software.applicationprovider.connectors;

import fr.upmc.PriseTheSun.datacenterclient.software.applicationprovider.interfaces.ApplicationSubmissionI;
import fr.upmc.components.connectors.AbstractConnector;
/**
 *   * The class <code>ApplicationSubmissionConnector</code> implements the
 * connector between outbound and inboud ports implementing the interface
 * <code>ApplicationSubmissionI</code>.
 * @author Maxime Lavaste
 *
 */
public class ApplicationSubmissionConnector extends	AbstractConnector
implements	ApplicationSubmissionI {

	@Override
	public String[] submitApplication(String appURI, int nbVM) throws Exception {
		
		return ( ( ApplicationSubmissionI ) this.offering ).submitApplication(appURI, nbVM);

	}

	@Override
	public void submitGenerator(String requestNotificationInboundPort, String appURI, String rgURI) throws Exception {
		( ( ApplicationSubmissionI ) this.offering ).submitGenerator(requestNotificationInboundPort, appURI,  rgURI);	
	}

	@Override
	public String[] submitApplication(String appURI, int nbVM, Class submissionInterface) throws Exception {
		return ( ( ApplicationSubmissionI ) this.offering ).submitApplication(appURI,nbVM,submissionInterface);
	}
}
