package fr.upmc.datacenterclient.applicationprovider.connectors;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.datacenterclient.applicationprovider.interfaces.ApplicationSubmissionI;

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
}
