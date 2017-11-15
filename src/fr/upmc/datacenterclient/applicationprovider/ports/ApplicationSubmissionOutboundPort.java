package fr.upmc.datacenterclient.applicationprovider.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionInboundPort;
import fr.upmc.datacenterclient.applicationprovider.interfaces.ApplicationSubmissionI;

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
}
