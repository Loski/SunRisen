package fr.upmc.datacenterclient.applicationprovider.ports;

import com.sun.glass.ui.Application;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.datacenterclient.applicationprovider.ApplicationProvider;
import fr.upmc.datacenterclient.applicationprovider.interfaces.ApplicationProviderManagementI;
import fr.upmc.datacenterclient.requestgenerator.RequestGenerator;

public class ApplicationProviderManagementOutboundPort extends AbstractOutboundPort implements ApplicationProviderManagementI{

	public ApplicationProviderManagementOutboundPort(Class<?> implementedInterface, ComponentI owner) throws Exception {
		super(ApplicationProviderManagementI.class, owner);
	}

	public ApplicationProviderManagementOutboundPort(String uri, Class<?> implementedInterface, ComponentI owner)
			throws Exception {
		super(uri, ApplicationProviderManagementI.class, owner);
	}

	@Override
	public void createAndSendApplication() throws Exception {
		((ApplicationProviderManagementI)this.connector).createAndSendApplication();
		
	}

	@Override
	public void stopApplication() throws Exception {
		((ApplicationProviderManagementI)this.connector).stopApplication();
	}

}
