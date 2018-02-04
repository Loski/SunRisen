package fr.upmc.PriseTheSun.datacenter.hardware.computer;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import fr.upmc.PriseTheSun.datacenter.hardware.computer.interfaces.ComputerControllerManagementI;
import fr.upmc.PriseTheSun.datacenter.hardware.computer.ports.ComputerControllerManagementInboundPort;
import fr.upmc.PriseTheSun.datacenter.hardware.processors.ProcessorsController.CoreAsk;
import fr.upmc.components.AbstractComponent;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.components.exceptions.ComponentStartException;
import fr.upmc.components.interfaces.DataRequiredI;
import fr.upmc.datacenter.connectors.ControlledDataConnector;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;
import fr.upmc.datacenter.hardware.computers.connectors.ComputerServicesConnector;
import fr.upmc.datacenter.hardware.computers.interfaces.ComputerDynamicStateI;
import fr.upmc.datacenter.hardware.computers.interfaces.ComputerServicesI;
import fr.upmc.datacenter.hardware.computers.interfaces.ComputerStateDataConsumerI;
import fr.upmc.datacenter.hardware.computers.interfaces.ComputerStaticStateI;
import fr.upmc.datacenter.hardware.computers.ports.ComputerServicesOutboundPort;
import fr.upmc.datacenter.hardware.computers.ports.ComputerStaticStateDataOutboundPort;
import fr.upmc.datacenter.hardware.processors.UnacceptableFrequencyException;
import fr.upmc.datacenter.hardware.processors.UnavailableFrequencyException;
import fr.upmc.datacenter.hardware.processors.interfaces.ProcessorDynamicStateI;
import fr.upmc.datacenter.hardware.processors.interfaces.ProcessorStaticStateI;
import fr.upmc.datacenter.interfaces.ControlledDataRequiredI;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;

public class ComputerController extends AbstractComponent implements ComputerControllerManagementI, ComputerStateDataConsumerI {
	
	private static final String ProcessorStaticStateDataOutboundPortURI = "pss";
	private static final String ProcessorDynamicStateDataOutboundPortURI = "pds";
	private static final String ProcessorManagementOutboundPortURI = "pmop";
	
	private ComputerServicesOutboundPort csop;
	private Map<String, ArrayList<Point>> reservedCore;
	private ComputerControllerManagementInboundPort ccmip;
	
	private static final int  NUMBER_MAX_CORES = 10;
	
	/**
	 * Controller d'un ordinateur. Ce composant s'assure de la bonne coopération et interdit un comportement trop avide d'une VM
	 * qui volerait tous les coeurs d'un ordinateur.
	 * @param ComputerControllerUri
	 * @param ComputerServicesInboundPortURI
	 * @param ComputerControllerManagementInboundPort
	 * @param ComputerStaticStateDataInboundPortURI
	 * @throws Exception
	 */
	public ComputerController(final String ComputerControllerUri, final String ComputerServicesInboundPortURI, final String ComputerControllerManagementInboundPort, String ComputerStaticStateDataInboundPortURI) throws Exception {
		super(ComputerControllerUri, 1, 1);
		
		this.addOfferedInterface(ComputerServicesI.class);
		this.addOfferedInterface(ComputerControllerManagementI.class);

		this.addRequiredInterface(DataRequiredI.PullI.class);
		this.addRequiredInterface(ControlledDataRequiredI.ControlledPullI.class) ;
		
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
		
		
		ComputerStaticStateDataOutboundPort cssdop = new ComputerStaticStateDataOutboundPort(this, ComputerControllerUri + "cssdop");
		this.addPort(cssdop);
		cssdop.publishPort();
		cssdop.doConnection(
				ComputerStaticStateDataInboundPortURI,
				ControlledDataConnector.class.getCanonicalName());
		
		ComputerStaticStateI staticState= (ComputerStaticStateI) cssdop.request();
        cssdop.doDisconnection();
	}

	/**
	 * @see fr.upmc.PriseTheSun.datacenter.hardware.computer.interfaces.ComputerControllerManagementI#addCores(java.lang.String)
	 */
	public AllocatedCore[] addCores(String vmUri) throws Exception {
		assert vmUri != null;
		ArrayList<Point> pts = reservedCore.get(vmUri);
		if(pts == null)
			return null;
		return csop.allocateCores(pts);
	}

	/**
	 * @see fr.upmc.PriseTheSun.datacenter.hardware.computer.interfaces.ComputerControllerManagementI#releaseCore(java.lang.String)
	 */
	@Override
	public void releaseCore(String vmUri) throws Exception {
		assert vmUri != null;
		ArrayList<Point> cores =  reservedCore.remove(vmUri);		
		if(cores != null) {
			System.err.println("je rentre " + vmUri);
			csop.releaseCore(cores);
		}
	}
	
	/**
	 * @see fr.upmc.PriseTheSun.datacenter.hardware.computer.interfaces.ComputerControllerManagementI#tryReserveCore(java.lang.String, int, int)
	 */
	@Override
	public int tryReserveCore(String vmUri, int nbToReserve, int numberAllocated) throws Exception {
		assert vmUri != null;
		assert nbToReserve > 0;
		
		//Interdit plus de Max Core allouées à une seule VM si plusieurs VM.
		if(notAlone()) {
			nbToReserve =   NUMBER_MAX_CORES - numberAllocated;
			if(nbToReserve <= 0)
				return 0;
		}
		ArrayList<Point> cores =  csop.reserveCores(nbToReserve);
		reservedCore.put(vmUri, cores);

		return cores.size();
	}

	/**
	 * Informe si une VM est seule sur l'ordinateur
	 * @return compteurVM() > 1
	 * @throws Exception 
	 */
	private boolean notAlone() throws Exception {
		return compteurVM() > 1;
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

	
	public void setCoreFrequency(CoreAsk ask, String processorURI, int coreNo) throws UnavailableFrequencyException, UnacceptableFrequencyException, Exception {

		/*ProcessorStaticStateI staticState = this.processorsStaticState.get(processorURI);
		ProcessorDynamicStateI dynamicState = this.processorsDynamicState.get(processorURI);
		if(staticState == null || dynamicState == null)
			throw new Exception("admidssableF was null");
		int frequenceCore = dynamicState.getCurrentCoreFrequency(coreNo);
		Set<Integer> admissableFrequencies = staticState.getAdmissibleFrequencies();
		Iterator<Integer> it = admissableFrequencies.iterator();
		int newfrequency = -1;
		int frequency = -1;
	

		//Ajouter tri � la cr�ation des fr�quences pour �viter parcourt ??.
		
		if(ask == CoreAsk.HIGHER) {
		    while(it.hasNext()) {
		    	frequency = (int) it.next();
		    	if(frequency > frequenceCore) {
		    		newfrequency = frequency;
		    		break;
		    	}
		    }
		}else if(ask == CoreAsk.LOWER) {
		    while(it.hasNext()) {
		    	frequency = (int) it.next();
		    	if(frequency > frequenceCore) {
		    		newfrequency = frequency;
		    		break;
		    	}
		    }
		}
		if(newfrequency != -1) {
			try {
				//this.processorsManagement.get(processorURI).setCoreFrequency(coreNo, newfrequency);
				System.err.println("Frequence change");
				return true;
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;*/
	}

	@Override
	public void acceptComputerStaticData(String computerURI, ComputerStaticStateI staticState) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void acceptComputerDynamicData(String computerURI, ComputerDynamicStateI currentDynamicState)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int compteurVM() throws Exception {
		return reservedCore.size();
	}
	
}
