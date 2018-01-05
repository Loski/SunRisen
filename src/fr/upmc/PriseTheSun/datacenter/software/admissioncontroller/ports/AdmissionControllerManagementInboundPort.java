package fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.ports;

import java.util.ArrayList;

import fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.interfaces.AdmissionControllerManagementI;
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
	public boolean addCores(String rdURI, int nbCores, String vmUri) throws Exception {
		final AdmissionControllerManagementI acm = ( AdmissionControllerManagementI ) this.owner;
		
		return this.owner.handleRequestSync(
				new ComponentI.ComponentService<Boolean>() {
					@Override
					public Boolean call() throws Exception {
						return acm.addCores(rdURI, nbCores, null) ;
					}
				});
		}

	@Override
	public void linkComputer(String computerURI, String ComputerServicesInboundPortURI,
			String ComputerStaticStateDataInboundPortURI, String ComputerDynamicStateDataInboundPortURI,
			ArrayList<String> pmipURIs, ArrayList<String> pssdURIs)
			throws Exception {
		final AdmissionControllerManagementI acm = ( AdmissionControllerManagementI ) this.owner;

		 this.owner.handleRequestSync(
				new ComponentI.ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						acm.linkComputer(computerURI, ComputerServicesInboundPortURI, ComputerStaticStateDataInboundPortURI, ComputerDynamicStateDataInboundPortURI, pmipURIs, pssdURIs);
						return null;
					}
				});
	}
	
	
}
