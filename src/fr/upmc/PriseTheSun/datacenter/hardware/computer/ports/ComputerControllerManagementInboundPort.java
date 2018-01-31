package fr.upmc.PriseTheSun.datacenter.hardware.computer.ports;

import fr.upmc.PriseTheSun.datacenter.hardware.computer.interfaces.ComputerControllerManagementI;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces.RequestDispatcherManagementI;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;

public class ComputerControllerManagementInboundPort extends AbstractInboundPort implements ComputerControllerManagementI {

	public				ComputerControllerManagementInboundPort(
			ComponentI owner
			) throws Exception
		{
			super(ComputerControllerManagementI.class, owner) ;

			assert	owner instanceof ComputerControllerManagementI ;
		}

		public				ComputerControllerManagementInboundPort(
			String uri,
			ComponentI owner
			) throws Exception
		{
			super(uri, ComputerControllerManagementI.class, owner);

			assert	uri != null && owner instanceof ComputerControllerManagementI ;
		}

	@Override
	public int reserveCore(String controllerURI) throws Exception {
		final ComputerControllerManagementI ccm = ( ComputerControllerManagementI ) this.owner;

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
		final ComputerControllerManagementI ccm = ( ComputerControllerManagementI ) this.owner;
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
		final ComputerControllerManagementI ccm = ( ComputerControllerManagementI ) this.owner;

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
		final ComputerControllerManagementI ccm = ( ComputerControllerManagementI ) this.owner;
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
		final ComputerControllerManagementI ccm = ( ComputerControllerManagementI ) this.owner;

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
