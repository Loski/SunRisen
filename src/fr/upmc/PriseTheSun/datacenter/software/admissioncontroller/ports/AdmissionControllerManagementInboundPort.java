package fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.ports;

import fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.interfaces.AdmissionControllerManagementI;
import fr.upmc.PriseTheSun.datacenter.software.applicationvm.ApplicationVMInfo;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces.RequestDispatcherManagementI;
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



	@Override
	public void linkComputer(String computerURI, String ComputerServicesInboundPortURI,
			String ComputerStaticStateDataInboundPortURI, String ComputerDynamicStateDataInboundPortURI)
			throws Exception {
		final AdmissionControllerManagementI acm = ( AdmissionControllerManagementI ) this.owner;

		 this.owner.handleRequestSync(
				new ComponentI.ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						acm.linkComputer(computerURI, ComputerServicesInboundPortURI, ComputerStaticStateDataInboundPortURI, ComputerDynamicStateDataInboundPortURI);
						return null;
					}
				});
	}


	
	
}
