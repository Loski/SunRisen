package fr.upmc.PriseTheSun.datacenterclient.software.applicationprovider.ports;

import fr.upmc.PriseTheSun.datacenterclient.software.applicationprovider.interfaces.ApplicationSubmissionI;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;


public class ApplicationSubmissionInboundPort extends AbstractInboundPort implements ApplicationSubmissionI {
    private static final long serialVersionUID = 1L;
    
    @Override
    public String[] submitApplication(String appURI,  final int nbVM ) throws Exception {
        final ApplicationSubmissionI aps = ( ApplicationSubmissionI ) this.owner;
        return this.owner.handleRequestSync( new ComponentI.ComponentService<String[]>() {
            @Override
            public String[] call() throws Exception {
                return aps.submitApplication(appURI, nbVM );
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
		final ApplicationSubmissionI aps = ( ApplicationSubmissionI ) this.owner;
		
		this.owner.handleRequestSync(
				new ComponentI.ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						aps.submitGenerator(requestNotificationInboundPort, appURI, rgURI);
						return null;
					}
				}) ;
	}

	@Override
	public String[] submitApplication(String appURI, int nbVM, Class submissionInterface) throws Exception {
        final ApplicationSubmissionI aps = ( ApplicationSubmissionI ) this.owner;
        return this.owner.handleRequestSync( new ComponentI.ComponentService<String[]>() {
            @Override
            public String[] call() throws Exception {
                return aps.submitApplication(appURI, nbVM,submissionInterface );
            }
        } );
	}
}