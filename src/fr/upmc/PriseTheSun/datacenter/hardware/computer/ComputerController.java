package fr.upmc.PriseTheSun.datacenter.hardware.computer;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import fr.upmc.PriseTheSun.datacenter.hardware.computer.interfaces.ComputerControllerManagement;
import fr.upmc.components.AbstractComponent;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.components.exceptions.ComponentStartException;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;
import fr.upmc.datacenter.hardware.computers.connectors.ComputerServicesConnector;
import fr.upmc.datacenter.hardware.computers.interfaces.ComputerServicesI;
import fr.upmc.datacenter.hardware.computers.ports.ComputerServicesOutboundPort;

public class ComputerController extends AbstractComponent implements ComputerControllerManagement {
	
	
	private ComputerServicesOutboundPort csop;
	private Map<String, ArrayList<Point>> reservedCore;

	
	public ComputerController(final String ComputerControllerUri, final String ComputerServicesInboundPortURI, final String ComputerControllerManagementIboundPort) throws Exception {
		super(ComputerControllerUri, 1, 1);
		this.reservedCore = new HashMap<String, ArrayList<Point>>();
		this.addOfferedInterface(ComputerServicesI.class);
		this.csop = new ComputerServicesOutboundPort(ComputerControllerUri + "-csop", this);
		this.addPort(csop);		
		csop.publishPort();
		csop.doConnection(
				ComputerServicesInboundPortURI,
				ComputerServicesConnector.class.getCanonicalName());
	}

	
	
	public AllocatedCore[] addCores(String controllerURI) throws Exception {
		return csop.allocateCores(reservedCore.get(controllerURI));
	}

	@Override
	public boolean supCores(int nbCores, String vmUri) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void releaseCore(String controllerURI) throws Exception {
		ArrayList<Point> cores =  reservedCore.remove(controllerURI);
		csop.releaseCore(cores);
	}
	
	@Override
	public int reserveCore(String controllerURI) throws Exception {
		ArrayList<Point> cores =  csop.reserveCores(8);
		if(reservedCore.size() > 0) {
			reservedCore.get(controllerURI).addAll(cores);
		}else {
			reservedCore.put(controllerURI, cores);
		}
		return cores.size();
	}

	@Override
	public void start() throws ComponentStartException {
		// TODO Auto-generated method stub
		super.start();
	}

	@Override
	public void shutdown() throws ComponentShutdownException {
		for (Map.Entry<String, ArrayList<Point>> entry : reservedCore.entrySet())
		{
		    try {
				this.releaseCore(entry.getKey());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		super.shutdown();
	}
}
