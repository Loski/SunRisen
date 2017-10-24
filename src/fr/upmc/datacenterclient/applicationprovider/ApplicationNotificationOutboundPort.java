package fr.upmc.datacenterclient.applicationprovider;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.datacenter.software.interfaces.RequestI;
import fr.upmc.datacenter.software.interfaces.RequestNotificationI;
import fr.upmc.datacenterclient.applicationprovider.interfaces.ApplicationNotificationI;

public class ApplicationNotificationOutboundPort extends AbstractOutboundPort implements	ApplicationNotificationI{
	  
	public ApplicationNotificationOutboundPort( String uri , ComponentI owner ) throws Exception {
	        super( uri , ApplicationNotificationI.class , owner );
	    }

	 @Override
	 public void notifyRequestGeneratorCreated( String requestNotificationInboundPortURI , String rdnopUri ) throws Exception {
	        ( ( ApplicationNotificationI ) this.connector ).notifyRequestGeneratorCreated( requestNotificationInboundPortURI , rdnopUri );
	}

}
