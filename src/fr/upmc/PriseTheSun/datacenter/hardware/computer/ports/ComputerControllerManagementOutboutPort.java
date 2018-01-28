package fr.upmc.PriseTheSun.datacenter.hardware.computer.ports;

import fr.upmc.PriseTheSun.datacenter.hardware.computer.interfaces.ComputerControllerManagement;
import fr.upmc.PriseTheSun.datacenterclient.software.applicationprovider.interfaces.ApplicationProviderManagementI;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;

public class ComputerControllerManagementOutboutPort extends AbstractOutboundPort implements ComputerControllerManagement {

	public ComputerControllerManagementOutboutPort(Class<?> implementedInterface, ComponentI owner) throws Exception {
		super(implementedInterface, owner);
	}
	
	public ComputerControllerManagementOutboutPort(String uri, ComponentI owner) throws Exception
	{
		super(uri, ComputerControllerManagement.class, owner);
	}

	@Override
	public int reserveCore(String controllerURI) throws Exception {
		return ((ComputerControllerManagement)this.connector).reserveCore(controllerURI);
	}

	@Override
	public void releaseCore(String controllerURI) throws Exception {
		((ComputerControllerManagement)this.connector).releaseCore(controllerURI);
	}

	@Override
	public AllocatedCore[] addCores(String controllerURI) throws Exception {
		return ((ComputerControllerManagement)this.connector).addCores(controllerURI);
	}

	@Override
	public boolean supCores(int nbCores, String vmUri) throws Exception {
		return ((ComputerControllerManagement)this.connector).supCores(nbCores, vmUri);

	}

	public AllocatedCore[] allocateCores(int i) throws Exception {
		return ((ComputerControllerManagement)this.connector).allocateCores(i);
	}

}
