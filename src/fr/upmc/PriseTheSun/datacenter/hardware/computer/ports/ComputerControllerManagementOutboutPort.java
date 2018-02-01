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
	public int tryReserveCore(String vmUri, int nbToReserve) throws Exception {
		return ((ComputerControllerManagementI)this.connector).tryReserveCore(vmUri, nbToReserve);
	}

	@Override
	public void releaseCore(String vmUri) throws Exception {
		((ComputerControllerManagementI)this.connector).releaseCore(vmUri);
	}

	@Override
	public AllocatedCore[] addCores(String vmUri) throws Exception {
		return ((ComputerControllerManagementI)this.connector).addCores(vmUri);
	}


	public AllocatedCore[] allocateCores(int i) throws Exception {
		return ((ComputerControllerManagementI)this.connector).allocateCores(i);
	}

}
