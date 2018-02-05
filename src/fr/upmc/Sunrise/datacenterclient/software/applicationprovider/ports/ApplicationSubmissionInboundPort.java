package fr.upmc.Sunrise.datacenterclient.software.applicationprovider.ports;

import fr.upmc.Sunrise.datacenterclient.software.applicationprovider.interfaces.ApplicationSubmissionI;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;


public class ApplicationSubmissionInboundPort extends AbstractInboundPort implements ApplicationSubmissionI {
    private static final long serialVersionUID = 1L;
    
    /**
     * @see fr.upmc.Sunrise.datacenterclient.software.applicationprovider.interfaces.ApplicationSubmissionI#submitApplication(java.lang.String)
     */
    @Override
    public String[] submitApplication(final String appURI ) throws Exception {
        final ApplicationSubmissionI aps = ( ApplicationSubmissionI ) this.owner;
        return this.owner.handleRequestSync( new ComponentI.ComponentService<String[]>() {
            @Override
            public String[] call() throws Exception {
                return aps.submitApplication(appURI );
            }
        } );
    }
    
    public ApplicationSubmissionInboundPort( ComponentI owner ) throws Exception {
        super( ApplicationSubmissionI.class , owner );
    }

    public ApplicationSubmissionInboundPort( String uri , ComponentI owner ) throws Exception {
        super( uri , ApplicationSubmissionI.class , owner );

    }

	/**
	 * @see fr.upmc.Sunrise.datacenterclient.software.applicationprovider.interfaces.ApplicationSubmissionI#submitGenerator(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void submitGenerator(final String requestNotificationInboundPort, final String appURI, final String rgURI) throws Exception {
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

	/*
	@Override
	public String[] submitApplication(final String appURI, final int nbVM, final Class submissionInterface) throws Exception {
        final ApplicationSubmissionI aps = ( ApplicationSubmissionI ) this.owner;
        return this.owner.handleRequestSync( new ComponentI.ComponentService<String[]>() {
            @Override
            public String[] call() throws Exception {
                return aps.submitApplication(appURI, nbVM,submissionInterface );
            }
        } );
	}*/

	@Override
	public void stopApplication(final String appUri) throws Exception {
		final ApplicationSubmissionI aps = ( ApplicationSubmissionI ) this.owner;
        this.owner.handleRequestSync( new ComponentI.ComponentService<Void>() {
            @Override
            public Void call() throws Exception {
            	aps.stopApplication(appUri);
                return null;
            }
        } );
	}
}