package fr.upmc.PriseTheSun.datacenter.hardware.computer.ports;

import fr.upmc.PriseTheSun.datacenter.hardware.computer.interfaces.ComputerControllerManagementI;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces.RequestDispatcherManagementI;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;

public class ComputerControllerManagementOutboutPort extends AbstractOutboundPort implements ComputerControllerManagementI {

	public				ComputerControllerManagementOutboutPort(
			ComponentI owner
			) throws Exception
		{
			super(ComputerControllerManagementI.class, owner) ;
		}

		public				ComputerControllerManagementOutboutPort(
			String uri,
			ComponentI owner
			) throws Exception
		{
			super(uri, ComputerControllerManagementI.class, owner);
		}

	@Override
	public int reserveCore(String controllerURI) throws Exception {
		return ((ComputerControllerManagementI)this.connector).reserveCore(controllerURI);
	}

	@Override
	public void releaseCore(String controllerURI) throws Exception {
		((ComputerControllerManagementI)this.connector).releaseCore(controllerURI);
	}

	@Override
	public AllocatedCore[] addCores(String controllerURI) throws Exception {
		return ((ComputerControllerManagementI)this.connector).addCores(controllerURI);
	}

	@Override
	public boolean supCores(int nbCores, String vmUri) throws Exception {
		return ((ComputerControllerManagementI)this.connector).supCores(nbCores, vmUri);

	}

	public AllocatedCore[] allocateCores(int i) throws Exception {
		return ((ComputerControllerManagementI)this.connector).allocateCores(i);
	}

}
