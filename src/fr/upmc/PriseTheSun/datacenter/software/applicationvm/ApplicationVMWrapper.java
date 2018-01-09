package fr.upmc.PriseTheSun.datacenter.software.applicationvm;

import fr.upmc.PriseTheSun.datacenter.hardware.processors.ProcessorsController;
import fr.upmc.components.AbstractComponent;
import fr.upmc.datacenter.software.applicationvm.ApplicationVM;
import fr.upmc.datacenter.software.applicationvm.connectors.ApplicationVMManagementConnector;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;

public class ApplicationVMWrapper extends AbstractComponent {

	private boolean free;
	private ProcessorsController controller;
	private ApplicationVM vm;
	private String ComputerServicesInboundPortURI;
	public ApplicationVMManagementOutboundPort avmPort;
	public String avmURi;
	public String sub;
	
	public ApplicationVMWrapper(ProcessorsController controller, String[] applicationVM,
			String computerServicesInboundPortURI) {
		
		super("test" + Math.random(), 2, 2);
		this.controller = controller;
		
		try {
			this.vm = new ApplicationVM(applicationVM[0],							
								applicationVM[1],
								applicationVM[2],
								applicationVM[3]);
			
			// Create a mock up port to manage the AVM component (allocate cores).
			avmPort = new ApplicationVMManagementOutboundPort(
					applicationVM[4], this) ;
			avmPort.publishPort() ;
			avmPort.doConnection(applicationVM[1],
						ApplicationVMManagementConnector.class.getCanonicalName()) ;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ComputerServicesInboundPortURI = computerServicesInboundPortURI;
		sub = applicationVM[2];
		avmURi = applicationVM[0];
	}

	
	

	public boolean isFree() {
		return free;
	}

	public void setFree(boolean free) {
		this.free = free;
	}

	public ProcessorsController getController() {
		return controller;
	}

	public void setController(ProcessorsController controller) {
		this.controller = controller;
	}

	public ApplicationVM getVm() {
		return vm;
	}

	public void setVm(ApplicationVM vm) {
		this.vm = vm;
	}
}
