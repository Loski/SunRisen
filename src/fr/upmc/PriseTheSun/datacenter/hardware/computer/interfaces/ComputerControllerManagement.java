package fr.upmc.PriseTheSun.datacenter.hardware.computer.interfaces;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;

public interface ComputerControllerManagement extends OfferedI, RequiredI {
	public int reserveCore(String controllerURI) throws Exception;
	public void releaseCore(String controllerURI) throws Exception;
	public AllocatedCore[] addCores(String controllerURI) throws Exception;
	public boolean supCores(int nbCores, String vmUri ) throws Exception;
	public AllocatedCore[] allocateCores(int i) throws Exception;
}
