package fr.upmc.datacenter.software.controller.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.datacenter.software.controller.AdmissionController;
import fr.upmc.datacenter.software.controller.interfaces.AdmissionControllerManagementI;
import fr.upmc.datacenterclient.applicationprovider.ApplicationProvider;

public class AdmissionControllerManagementInboundPort extends AbstractInboundPort implements AdmissionControllerManagementI{

	public AdmissionControllerManagementInboundPort(Class<?> implementedInterface, ComponentI owner) throws Exception {
		super(AdmissionControllerManagementI.class, owner);
		assert	owner != null && owner instanceof AdmissionController ;
	}

	public AdmissionControllerManagementInboundPort(String uri, Class<?> implementedInterface, ComponentI owner)
			throws Exception {
		super(uri, AdmissionControllerManagementI.class, owner);
		assert	owner != null && owner instanceof AdmissionController ;
	}

}
