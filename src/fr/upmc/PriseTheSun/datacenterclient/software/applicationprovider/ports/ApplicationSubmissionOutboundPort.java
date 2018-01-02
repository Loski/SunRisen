package fr.upmc.PriseTheSun.datacenterclient.software.applicationprovider.ports;

import fr.upmc.PriseTheSun.datacenterclient.software.applicationprovider.interfaces.ApplicationSubmissionI;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;


public class ApplicationSubmissionOutboundPort extends AbstractOutboundPort implements ApplicationSubmissionI {
    
	public ApplicationSubmissionOutboundPort( String uri , ComponentI owner ) throws Exception {
        super( uri , ApplicationSubmissionI.class , owner );
    }

    @Override
    public String[] submitApplication(String appURI, int nbVM ) throws Exception {
      return  ( ( ApplicationSubmissionI ) this.connector ).submitApplication(appURI, nbVM );
}

	@Override
	public void submitGenerator(String requestNotificationInboundPort, String appURI, String rgURI) throws Exception {
	      ( ( ApplicationSubmissionI ) this.connector ).submitGenerator(requestNotificationInboundPort, appURI, rgURI );
	}

    @Override
    public String[] submitApplication(String appURI, int nbVM, Class submissionInterface) throws Exception {
      return  ( ( ApplicationSubmissionI ) this.connector ).submitApplication(appURI, nbVM, submissionInterface);
    }
}
