package fr.upmc.datacenterclient.applicationprovider.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ComponentI.ComponentService;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.datacenter.software.controller.AdmissionController;
import fr.upmc.datacenter.software.ports.RequestSubmissionInboundPort;
import fr.upmc.datacenterclient.applicationprovider.interfaces.ApplicationSubmissionI;


public class ApplicationSubmissionInboundPort extends AbstractInboundPort implements ApplicationSubmissionI {
    private static final long serialVersionUID = 1L;
    
    @Override
    public String[] submitApplication(String appURI,  final int nbVM ) throws Exception {
        final AdmissionController arh = ( AdmissionController ) this.owner;
        return this.owner.handleRequestSync( new ComponentService<String[]>() {
            @Override
            public String[] call() throws Exception {
                return arh.submitApplication(appURI, nbVM );
            }
        } );
    }
    
    public ApplicationSubmissionInboundPort( ComponentI owner ) throws Exception {
        super( ApplicationSubmissionI.class , owner );
    }

    public ApplicationSubmissionInboundPort( String uri , ComponentI owner ) throws Exception {
        super( uri , ApplicationSubmissionI.class , owner );

    }

	@Override
	public void submitGenerator(String requestNotificationInboundPort, String appURI, String rgURI) throws Exception {
		final AdmissionController arh = ( AdmissionController ) this.owner;
		
		this.owner.handleRequestSync(
				new ComponentI.ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						arh.submitGenerator(requestNotificationInboundPort, appURI, rgURI);
						return null;
					}
				}) ;
	}
}