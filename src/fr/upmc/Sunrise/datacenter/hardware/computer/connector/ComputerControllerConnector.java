package fr.upmc.Sunrise.datacenter.hardware.computer.connector;

import fr.upmc.Sunrise.datacenter.hardware.computer.interfaces.ComputerControllerManagementI;
import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;

public class ComputerControllerConnector extends AbstractConnector implements ComputerControllerManagementI {

	@Override
	public int tryReserveCore(String vmUri, int nbToReserve, int numberAllocated) throws Exception {
		return ((ComputerControllerManagementI)this.offering).tryReserveCore(vmUri, nbToReserve, numberAllocated);
	}

	@Override
	public void releaseCore(String vmUri) throws Exception {
		((ComputerControllerManagementI)this.offering).releaseCore(vmUri);
	}

	@Override
	public AllocatedCore[] addCores(String vmUri) throws Exception {
		return ((ComputerControllerManagementI)this.offering).addCores(vmUri);

	}



	@Override
	public AllocatedCore[] allocateCores(int i) throws Exception {
		return ((ComputerControllerManagementI)this.offering).allocateCores(i);
	}

	@Override
	public int compteurVM() throws Exception {
		return ((ComputerControllerManagementI)this.offering).compteurVM();
	}

	@Override
	public int compteurCoreReserved(String Vmrui) throws Exception {
		return ((ComputerControllerManagementI)this.offering).compteurCoreReserved(Vmrui);
	}

}
