package fr.upmc.PriseTheSun.datacenter.hardware.computer.connector;

import fr.upmc.PriseTheSun.datacenter.hardware.computer.interfaces.ComputerControllerManagementI;
import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;

public class ComputerControllerConnector extends AbstractConnector implements ComputerControllerManagementI {

	@Override
	public int reserveCore(String controllerURI) throws Exception {
		return ((ComputerControllerManagementI)this.offering).reserveCore(controllerURI);
	}

	@Override
	public void releaseCore(String controllerURI) throws Exception {
		((ComputerControllerManagementI)this.offering).releaseCore(controllerURI);
	}

	@Override
	public AllocatedCore[] addCores(String controllerURI) throws Exception {
		return ((ComputerControllerManagementI)this.offering).addCores(controllerURI);

	}

	@Override
	public boolean supCores(int nbCores, String vmUri) throws Exception {
		return ((ComputerControllerManagementI)this.offering).supCores(nbCores, vmUri);
	}

	@Override
	public AllocatedCore[] allocateCores(int i) throws Exception {
		return ((ComputerControllerManagementI)this.offering).allocateCores(i);
	}

}
