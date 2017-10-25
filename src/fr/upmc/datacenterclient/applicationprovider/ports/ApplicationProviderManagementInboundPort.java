package fr.upmc.datacenterclient.applicationprovider.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ComponentI.ComponentService;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.datacenterclient.applicationprovider.ApplicationProvider;
import fr.upmc.datacenterclient.applicationprovider.interfaces.ApplicationProviderManagementI;

public class ApplicationProviderManagementInboundPort extends AbstractInboundPort implements ApplicationProviderManagementI {

	public ApplicationProviderManagementInboundPort(Class<?> implementedInterface, ComponentI owner) throws Exception {
		super(ApplicationProviderManagementI.class, owner);
		assert	owner != null && owner instanceof ApplicationProvider ;
	}

	public ApplicationProviderManagementInboundPort(String uri, Class<?> implementedInterface, ComponentI owner)
			throws Exception {
		super(uri, ApplicationProviderManagementI.class, owner);
		assert	owner != null && owner instanceof ApplicationProvider ;
	}
	
	@Override
    public void createAndSendApplication() throws Exception  {
        final ApplicationProvider ap = ( ApplicationProvider ) this.owner;
        this.owner.handleRequestAsync( new ComponentService<String>() {

            @Override
            public String call() throws Exception {
                ap.createAndSendApplication();
                return null;

            }
        } );

    }

    @Override
    public void stopApplication() throws Exception {
        final ApplicationProvider ap = ( ApplicationProvider ) this.owner;

        this.owner.handleRequestAsync( new ComponentService<String>() {

            @Override
            public String call() throws Exception {
                ap.stopApplication();
                return null;

            }
        } );

}
}
