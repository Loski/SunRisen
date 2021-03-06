package fr.upmc.Sunrise.datacenterclient.software.applicationprovider.ports;

import fr.upmc.Sunrise.datacenter.software.requestdispatcher.interfaces.RequestDispatcherManagementI;
import fr.upmc.Sunrise.datacenterclient.software.applicationprovider.interfaces.ApplicationProviderManagementI;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionI;


public class ApplicationProviderManagementInboundPort extends AbstractInboundPort implements ApplicationProviderManagementI {

	public				ApplicationProviderManagementInboundPort(
			ComponentI owner
			) throws Exception
		{
			super(ApplicationProviderManagementI.class, owner) ;

			assert	owner instanceof ApplicationProviderManagementI ;
		}

		public				ApplicationProviderManagementInboundPort(
			String uri,
			ComponentI owner
			) throws Exception
		{
			super(uri, ApplicationProviderManagementI.class, owner);

			assert	uri != null && owner instanceof ApplicationProviderManagementI ;
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
	public void createAndSendApplication(final Class<RequestSubmissionI> class1) throws Exception {
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
