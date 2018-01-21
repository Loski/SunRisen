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
	public int addCores(String controllerURI, String vmUri, int nbCores) throws Exception {
		final AdmissionControllerManagementI acm = ( AdmissionControllerManagementI ) this.owner;
		
		return this.owner.handleRequestSync(
				new ComponentI.ComponentService<Integer>() {
					@Override
					public Integer call() throws Exception {
						return acm.addCores(controllerURI, vmUri, nbCores) ;
					}
				});
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

	@Override
	public boolean supCores(int nbCores, String vmUri) throws Exception {
		final AdmissionControllerManagementI acm = ( AdmissionControllerManagementI ) this.owner;
		return this.owner.handleRequestSync(
				new ComponentI.ComponentService<Boolean>() {
					@Override
					public Boolean call() throws Exception {
						return acm.supCores(nbCores, vmUri) ;
					}
				});
	}

	@Override
	public void releaseCore(String controllerURI, String VMURI) throws Exception {
		final AdmissionControllerManagementI acm = ( AdmissionControllerManagementI ) this.owner;
		this.owner.handleRequestSync(
				new ComponentI.ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						 acm.releaseCore(controllerURI, VMURI);
						return null;
					}
				});
	}

	@Override
	public void allocVm(String appURI, ApplicationVMInfo vm, String dispatcherURI,
			String dispatcherNotificationInboundPort) throws Exception {
		final AdmissionControllerManagementI acm = ( AdmissionControllerManagementI ) this.owner;
		this.owner.handleRequestSync(
				new ComponentI.ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						 acm.allocVm(appURI, vm, dispatcherURI, dispatcherNotificationInboundPort);
						return null;
					}
				});
	}
	
	
}
