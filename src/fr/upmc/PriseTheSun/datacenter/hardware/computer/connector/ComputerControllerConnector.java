package fr.upmc.PriseTheSun.datacenter.hardware.computer.connector;

import fr.upmc.PriseTheSun.datacenter.hardware.computer.interfaces.ComputerControllerManagement;
import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;

public class ComputerControllerConnector extends AbstractConnector implements ComputerControllerManagement {

	@Override
	public int reserveCore(String controllerURI) throws Exception {
		return ((ComputerControllerManagement)this.offering).reserveCore(controllerURI);
	}

	@Override
	public void releaseCore(String controllerURI) throws Exception {
		((ComputerControllerManagement)this.offering).releaseCore(controllerURI);
	}

	@Override
	public AllocatedCore[] addCores(String controllerURI) throws Exception {
		return ((ComputerControllerManagement)this.offering).addCores(controllerURI);

	}

	@Override
	public boolean supCores(int nbCores, String vmUri) throws Exception {
		return ((ComputerControllerManagement)this.offering).supCores(nbCores, vmUri);
	}

	@Override
	public AllocatedCore[] allocateCores(int i) throws Exception {
		return ((ComputerControllerManagement)this.offering).allocateCores(i);
	}

}
