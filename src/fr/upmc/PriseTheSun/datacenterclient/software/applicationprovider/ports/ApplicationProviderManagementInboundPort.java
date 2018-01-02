package fr.upmc.PriseTheSun.datacenterclient.software.applicationprovider.ports;

import fr.upmc.PriseTheSun.datacenterclient.software.applicationprovider.interfaces.ApplicationProviderManagementI;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionI;


public class ApplicationProviderManagementInboundPort extends AbstractInboundPort implements ApplicationProviderManagementI {

	public ApplicationProviderManagementInboundPort(Class<?> implementedInterface, ComponentI owner) throws Exception {
		super(ApplicationProviderManagementI.class, owner);
		assert	owner != null && owner instanceof ApplicationProviderManagementI ;
	}

	public ApplicationProviderManagementInboundPort(String uri, Class<?> implementedInterface, ComponentI owner)
			throws Exception {
		super(uri, ApplicationProviderManagementI.class, owner);
		assert	owner != null && owner instanceof ApplicationProviderManagementI ;
	}
	
	@Override
    public void createAndSendApplication() throws Exception  {
        final ApplicationProviderManagementI apm = ( ApplicationProviderManagementI ) this.owner;
        this.owner.handleRequestAsync( new ComponentI.ComponentService<Void>() {

            @Override
            public Void call() throws Exception {
                apm.createAndSendApplication();
                return null;

            }
        } );

    }

    @Override
    public void stopApplication() throws Exception {
        final ApplicationProviderManagementI apm = ( ApplicationProviderManagementI ) this.owner;

        this.owner.handleRequestAsync( new ComponentI.ComponentService<Void>() {

            @Override
            public Void call() throws Exception {
                apm.stopApplication();
                return null;

            }
        } );

}

	@Override
	public void createAndSendApplication(Class<RequestSubmissionI> class1) throws Exception {
		final ApplicationProviderManagementI apm = ( ApplicationProviderManagementI ) this.owner;
        this.owner.handleRequestAsync( new ComponentI.ComponentService<Void>() {

            @Override
            public Void call() throws Exception {
                apm.createAndSendApplication(class1);
                return null;

            }
        } );
	}
}
