package fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.ports;

import fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.interfaces.AdmissionControllerManagementI;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;

public class AdmissionControllerManagementOutboundPort extends		AbstractOutboundPort
implements	fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.interfaces.AdmissionControllerManagementI{

	public	AdmissionControllerManagementOutboundPort(String uri, ComponentI owner) throws Exception
	{
		super(uri, AdmissionControllerManagementI.class, owner);
	}

	public AdmissionControllerManagementOutboundPort(String uri, Class<?> implementedInterface, ComponentI owner)
			throws Exception {
		super(uri, AdmissionControllerManagementI.class, owner);
		assert	owner != null && owner instanceof AdmissionControllerManagementI;
	}
}
