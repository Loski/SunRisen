package fr.upmc.PriseTheSun.datacenter.hardware.computer.ports;

import fr.upmc.PriseTheSun.datacenter.hardware.computer.interfaces.ComputerControllerManagement;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;

public class ComputerControllerManagementInboundPort extends AbstractInboundPort implements ComputerControllerManagement {

	public ComputerControllerManagementInboundPort(String uri, Class<?> implementedInterface, ComponentI owner)
			throws Exception {
		super(uri, implementedInterface, owner);
	}
	
	public ComputerControllerManagementInboundPort(Class<?> implementedInterface, ComponentI owner) throws Exception {
		super(ComputerControllerManagement.class, owner);
		assert	owner != null && owner instanceof ComputerControllerManagement ;
	}

	@Override
	public int reserveCore(String controllerURI) throws Exception {
		final ComputerControllerManagement ccm = ( ComputerControllerManagement ) this.owner;

		 return this.owner.handleRequestSync(
				new ComponentI.ComponentService<Integer>() {
					@Override
					public Integer call() throws Exception {
						return ccm.reserveCore(controllerURI);
					}
				}
		);
	}

	@Override
	public void releaseCore(String controllerURI) throws Exception {
		final ComputerControllerManagement ccm = ( ComputerControllerManagement ) this.owner;
		 this.owner.handleRequestSync(
					new ComponentI.ComponentService<Void>() {
						@Override
						public Void call() throws Exception {
							ccm.releaseCore(controllerURI);
							return null;
						}
					}
			);
	}

	@Override
	public AllocatedCore[] addCores(String controllerURI) throws Exception {
		final ComputerControllerManagement ccm = ( ComputerControllerManagement ) this.owner;

		 return this.owner.handleRequestSync(
				new ComponentI.ComponentService<AllocatedCore[]>() {
					@Override
					public AllocatedCore[] call() throws Exception {
						return ccm.addCores(controllerURI);
						
					}
				}
		);
	}

	@Override
	public boolean supCores(int nbCores, String vmUri) throws Exception {
		final ComputerControllerManagement ccm = ( ComputerControllerManagement ) this.owner;
		 return this.owner.handleRequestSync(
				new ComponentI.ComponentService<Boolean>() {
					@Override
					public Boolean call() throws Exception {
						return ccm.supCores(nbCores, vmUri);
						
					}
				}
		);
	}

	@Override
	public AllocatedCore[] allocateCores(int i) throws Exception {
		final ComputerControllerManagement ccm = ( ComputerControllerManagement ) this.owner;

		 return this.owner.handleRequestSync(
				new ComponentI.ComponentService<AllocatedCore[]>() {
					@Override
					public AllocatedCore[] call() throws Exception {
						return ccm.allocateCores(i);
						
					}
				}
		);
	}
	


}
