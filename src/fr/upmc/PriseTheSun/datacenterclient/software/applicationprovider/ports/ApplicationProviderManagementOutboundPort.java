package fr.upmc.PriseTheSun.datacenterclient.software.applicationprovider.ports;


import fr.upmc.PriseTheSun.datacenterclient.software.applicationprovider.interfaces.ApplicationProviderManagementI;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionI;

public class ApplicationProviderManagementOutboundPort extends AbstractOutboundPort implements ApplicationProviderManagementI{

	public	ApplicationProviderManagementOutboundPort(String uri, ComponentI owner) throws Exception
	{
		super(uri, ApplicationProviderManagementI.class, owner);
	}

	/**
	 * @see fr.upmc.PriseTheSun.datacenterclient.software.applicationprovider.interfaces.ApplicationProviderManagementI#createAndSendApplication()
	 */
	@Override
	public void createAndSendApplication() throws Exception {
		((ApplicationProviderManagementI)this.connector).createAndSendApplication();
		
	}

	/**
	 * @see fr.upmc.PriseTheSun.datacenterclient.software.applicationprovider.interfaces.ApplicationProviderManagementI#stopApplication()
	 */
	@Override
	public void stopApplication() throws Exception {
		((ApplicationProviderManagementI)this.connector).stopApplication();
	}

	/**
	 * @see fr.upmc.PriseTheSun.datacenterclient.software.applicationprovider.interfaces.ApplicationProviderManagementI#createAndSendApplication(java.lang.Class)
	 */
	public void createAndSendApplication(Class<RequestSubmissionI> class1) throws Exception {
		((ApplicationProviderManagementI)this.connector).createAndSendApplication(class1);
	}

}
