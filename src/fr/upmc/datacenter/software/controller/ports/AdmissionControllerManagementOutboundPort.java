package fr.upmc.datacenter.software.controller.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.datacenter.software.controller.AdmissionController;
import fr.upmc.datacenter.software.controller.interfaces.AdmissionControllerManagementI;

public class AdmissionControllerManagementOutboundPort extends		AbstractOutboundPort
implements	AdmissionControllerManagementI{

	public AdmissionControllerManagementOutboundPort(Class<?> implementedInterface, ComponentI owner) throws Exception {
		super(AdmissionControllerManagementI.class, owner);
		assert	owner != null && owner instanceof AdmissionController ;
	}

	public AdmissionControllerManagementOutboundPort(String uri, Class<?> implementedInterface, ComponentI owner)
			throws Exception {
		super(uri, AdmissionControllerManagementI.class, owner);
		assert	owner != null && owner instanceof AdmissionController ;
	}


}
