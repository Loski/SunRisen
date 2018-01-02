package fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.ports;

import fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.interfaces.AdmissionControllerManagementI;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ComponentI.ComponentService;
import fr.upmc.components.ports.AbstractInboundPort;


public class AdmissionControllerManagementInboundPort extends AbstractInboundPort implements fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.interfaces.AdmissionControllerManagementI{

	public AdmissionControllerManagementInboundPort(Class<?> implementedInterface, ComponentI owner) throws Exception {
		super(AdmissionControllerManagementI.class, owner);
		assert	owner != null && owner instanceof AdmissionControllerManagementI ;
	}

	public AdmissionControllerManagementInboundPort(String uri, Class<?> implementedInterface, ComponentI owner)
			throws Exception {
		super(uri, AdmissionControllerManagementI.class, owner);
		assert	owner != null && owner instanceof AdmissionControllerManagementI ;
	}
}
