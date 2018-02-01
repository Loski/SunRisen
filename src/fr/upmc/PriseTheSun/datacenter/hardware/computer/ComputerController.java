package fr.upmc.PriseTheSun.datacenter.hardware.computer;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import fr.upmc.PriseTheSun.datacenter.hardware.computer.interfaces.ComputerControllerManagementI;
import fr.upmc.PriseTheSun.datacenter.hardware.computer.ports.ComputerControllerManagementInboundPort;
import fr.upmc.components.AbstractComponent;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.components.exceptions.ComponentStartException;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;
import fr.upmc.datacenter.hardware.computers.connectors.ComputerServicesConnector;
import fr.upmc.datacenter.hardware.computers.interfaces.ComputerServicesI;
import fr.upmc.datacenter.hardware.computers.ports.ComputerServicesOutboundPort;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;

public class ComputerController extends AbstractComponent implements ComputerControllerManagementI {
	
	
	private ComputerServicesOutboundPort csop;
	private Map<String, ArrayList<Point>> reservedCore;
	private ComputerControllerManagementInboundPort ccmip;
	
	public ComputerController(final String ComputerControllerUri, final String ComputerServicesInboundPortURI, final String ComputerControllerManagementInboundPort) throws Exception {
		super(ComputerControllerUri, 1, 1);
		
		this.addOfferedInterface(ComputerServicesI.class);
		this.addOfferedInterface(ComputerControllerManagementI.class);

		
		this.reservedCore = new HashMap<String, ArrayList<Point>>();
		
		this.csop = new ComputerServicesOutboundPort(ComputerControllerUri + "-csop", this);
		this.addPort(csop);		
		csop.publishPort();
		csop.doConnection(
				ComputerServicesInboundPortURI,
				ComputerServicesConnector.class.getCanonicalName());
		
		ccmip = new ComputerControllerManagementInboundPort(ComputerControllerManagementInboundPort,this);
		this.addPort(ccmip);
		this.ccmip.publishPort();
	}

	/**
	 * @see fr.upmc.PriseTheSun.datacenter.hardware.computer.interfaces.ComputerControllerManagementI#addCores(java.lang.String)
	 */
	public AllocatedCore[] addCores(String vmUri) throws Exception {
		assert vmUri != null;

		return csop.allocateCores(reservedCore.get(vmUri));
	}

	/**
	 * @see fr.upmc.PriseTheSun.datacenter.hardware.computer.interfaces.ComputerControllerManagementI#releaseCore(java.lang.String)
	 */
	@Override
	public void releaseCore(String vmUri) throws Exception {
		assert vmUri != null;

		ArrayList<Point> cores =  reservedCore.remove(vmUri);
		csop.releaseCore(cores);
	}
	
	/**
	 * @see fr.upmc.PriseTheSun.datacenter.hardware.computer.interfaces.ComputerControllerManagementI#tryReserveCore(java.lang.String, int)
	 */
	@Override
	public int tryReserveCore(String vmUri, int nbToReserve) throws Exception {
		assert vmUri != null;
		assert nbToReserve > 0;
		
		ArrayList<Point> cores =  csop.reserveCores(nbToReserve);
		reservedCore.put(vmUri, cores);

		return cores.size();
	}


	/**
	 * @see fr.upmc.PriseTheSun.datacenter.hardware.computer.interfaces.ComputerControllerManagementI#allocateCores(int)
	 */
	@Override
	public AllocatedCore[] allocateCores(int i) throws Exception {
		assert i > 0;
		
		return this.csop.allocateCores(i);
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
				e.printStackTrace();
			}
		}
		super.shutdown();
	}

}
