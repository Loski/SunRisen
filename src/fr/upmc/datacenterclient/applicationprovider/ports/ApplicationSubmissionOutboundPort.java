package fr.upmc.datacenterclient.applicationprovider.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.datacenterclient.applicationprovider.interfaces.ApplicationSubmissionI;

public class ApplicationSubmissionOutboundPort extends AbstractOutboundPort implements ApplicationSubmissionI {

	public ApplicationSubmissionOutboundPort(Class<?> implementedInterface, ComponentI owner) throws Exception {
		super(ApplicationSubmissionI.class, owner);
		// TODO Auto-generated constructor stub
	}

	public ApplicationSubmissionOutboundPort(String uri, Class<?> implementedInterface, ComponentI owner)
			throws Exception {
		super(uri, implementedInterface, owner);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String[] sendApplication() {
		 return ( ( ApplicationSubmissionI ) this.connector ).sendApplication();
	}

}
