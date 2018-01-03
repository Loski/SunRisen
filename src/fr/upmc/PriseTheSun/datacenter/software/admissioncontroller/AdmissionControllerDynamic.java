package fr.upmc.PriseTheSun.datacenter.software.admissioncontroller;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.AbstractExecutorService;

import fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.interfaces.AdmissionControllerManagementI;
import fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.ports.AdmissionControllerManagementInboundPort;
import fr.upmc.PriseTheSun.datacenter.software.controller.Controller;
import fr.upmc.PriseTheSun.datacenter.software.javassist.RequestDispatcherCreator;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.RequestDispatcher;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.connectors.RequestDispatcherManagementConnector;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.ports.RequestDispatcherDynamicStateDataOutboundPort;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.ports.RequestDispatcherManagementOutboundPort;
import fr.upmc.PriseTheSun.datacenterclient.software.applicationprovider.interfaces.ApplicationSubmissionI;
import fr.upmc.PriseTheSun.datacenterclient.software.applicationprovider.ports.ApplicationSubmissionInboundPort;
import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.components.cvm.pre.dcc.connectors.DynamicComponentCreationConnector;
import fr.upmc.components.cvm.pre.dcc.ports.DynamicComponentCreationOutboundPort;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.components.exceptions.ComponentStartException;
import fr.upmc.components.pre.reflection.connectors.ReflectionConnector;
import fr.upmc.components.pre.reflection.ports.ReflectionOutboundPort;
import fr.upmc.datacenter.connectors.ControlledDataConnector;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;
import fr.upmc.datacenter.hardware.computers.connectors.ComputerServicesConnector;
import fr.upmc.datacenter.hardware.computers.interfaces.ComputerDynamicStateI;
import fr.upmc.datacenter.hardware.computers.interfaces.ComputerServicesI;
import fr.upmc.datacenter.hardware.computers.interfaces.ComputerStateDataConsumerI;
import fr.upmc.datacenter.hardware.computers.interfaces.ComputerStaticStateI;
import fr.upmc.datacenter.hardware.computers.ports.ComputerDynamicStateDataOutboundPort;
import fr.upmc.datacenter.hardware.computers.ports.ComputerServicesOutboundPort;
import fr.upmc.datacenter.hardware.computers.ports.ComputerStaticStateDataOutboundPort;
import fr.upmc.datacenter.software.applicationvm.ApplicationVM;
import fr.upmc.datacenter.software.applicationvm.connectors.ApplicationVMManagementConnector;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;



/**
 * The class <code>AdmissionController</code> implements a component that represents an
 * Admission Controller in a datacenter, receiving new application.
 *
 * <p><strong>Description</strong></p>
 * 
 * The Admission Controller Purpose is to manage the new applications and computers sent to the datacenter.
 * 
 * The Admission Controller receive new Request Generator through the <code>ApplicationSubmissionI</code> Interface.
 * When receiving a new request generator, it check if he has enough Virtual Machine at disposition and create
 * a Request Dispatcher linked to the request generator and a Controller linked to the request Dispatcher.
 * He then bind the Virtual Machine to the request Dispatcher.
 * 
 * 
 * @author	Maxime LAVASTE Loïc LAFONTAINE
 */
public class AdmissionControllerDynamic extends AbstractComponent implements ComputerStateDataConsumerI, ApplicationSubmissionI, AdmissionControllerManagementI{

	public static int DEBUG_LEVEL = 1 ;
	protected String acURI;
 

	protected static final String RequestDispatcherManagementInboundPortURI = "rdmi";
	protected static final String RequestNotificationInboundPortURI = "rnip";
	protected static final String RequestSubmissionInboundPortURI = "rsip";
	protected static final String RequestNotificationOutboundPortURI = "rnop";
	protected static final String RequestDispatcherManagementOutboundPortURI = "rdmop";
	protected static final String RequestSubmissionOutboundPortURI = "rsop"; 
	protected static final int NB_CORES = 2;
	protected static final String RequestStaticDataInboundPortURI = "rsdip";
	protected static final String RequestDynamicDataInboundPortURI = "rddip";
	protected static final String computerServiceOutboundPortURI = "csop";
	protected static final String computerDynamicStateDataOutboundPortURI = "cdsdop";
	protected final String ComputerStaticStateDataOutboundPortURI = "cssdop";

	protected AdmissionControllerManagementInboundPort acmip;
	protected ApplicationSubmissionInboundPort asip;
	
	
	protected List<ApplicationVMManagementOutboundPort> avmOutPort;
	
	
	protected ArrayList<ComputerServicesOutboundPort> csops;
	protected ArrayList<ComputerStaticStateDataOutboundPort> cssdops;
	protected ArrayList<ComputerDynamicStateDataOutboundPort> cdsdops;
	protected ArrayList<Integer> nbAvailablesCores;

	protected ArrayList<String> computerUri;
	

	 // Map between RequestDispatcher URIs and the management ports to call them.
	protected Map<String, RequestDispatcherManagementOutboundPort> rdmopMap;
	
	//Map Between a vm and his computer
	protected Map<String, ComputerServicesOutboundPort> csopMap;

	private DynamicComponentCreationOutboundPort portToRequestDispatcherJVM;
	private DynamicComponentCreationOutboundPort portToApplicationVMJVM;
	private DynamicComponentCreationOutboundPort portTControllerJVM;
	protected LinkedHashMap<Class,Class> interface_dispatcher_map;
	


	/*protected static final String RequestDispatcher_JVM_URI = "controller" ;
	protected static final String Application_VM_JVM_URI = "controller";*/
	
	public AdmissionControllerDynamic(String acURI,
			String applicationSubmissionInboundPortURI,
			String AdmissionControllerManagementInboundPortURI,
			String RequestDispatcher_JVM_URI,
			String Application_VM_JVM_URI,
			String Controller_JVM_URI
			) throws Exception {
		
		super(acURI,2, 2);
		
		this.computerUri = new ArrayList<String>();
		
		this.toggleLogging();
		this.toggleTracing();
		this.acURI = acURI;	
		
		this.addOfferedInterface(ApplicationSubmissionI.class);
		this.asip = new ApplicationSubmissionInboundPort(applicationSubmissionInboundPortURI, this);
		this.addPort(asip);
		this.asip.publishPort();

		this.addOfferedInterface(AdmissionControllerManagementI.class);
		this.acmip = new AdmissionControllerManagementInboundPort(AdmissionControllerManagementInboundPortURI, AdmissionControllerManagementI.class, this);
		this.addPort(acmip);
		this.acmip.publishPort();
		
		this.portToApplicationVMJVM = new DynamicComponentCreationOutboundPort(this);
		this.portToApplicationVMJVM.publishPort();
		this.addPort(this.portToApplicationVMJVM);
		
		this.portToApplicationVMJVM.doConnection(					
				Application_VM_JVM_URI + AbstractCVM.DCC_INBOUNDPORT_URI_SUFFIX,
				DynamicComponentCreationConnector.class.getCanonicalName());
		
		this.portToRequestDispatcherJVM = new DynamicComponentCreationOutboundPort(this);
		this.portToRequestDispatcherJVM.publishPort();
		this.addPort(this.portToRequestDispatcherJVM);
		
		this.portToRequestDispatcherJVM.doConnection(					
				RequestDispatcher_JVM_URI + AbstractCVM.DCC_INBOUNDPORT_URI_SUFFIX,
				DynamicComponentCreationConnector.class.getCanonicalName());
		
		this.portTControllerJVM = new DynamicComponentCreationOutboundPort(this);
		this.portTControllerJVM.publishPort();
		this.addPort(this.portTControllerJVM);
		
		this.portTControllerJVM.doConnection(					
				Controller_JVM_URI + AbstractCVM.DCC_INBOUNDPORT_URI_SUFFIX,
				DynamicComponentCreationConnector.class.getCanonicalName());
		

		this.addRequiredInterface(ComputerServicesI.class);
		
		this.avmOutPort = new LinkedList<ApplicationVMManagementOutboundPort>();
		this.rdmopMap = new HashMap<String, RequestDispatcherManagementOutboundPort>();
		this.interface_dispatcher_map = new LinkedHashMap<>();
		this.csops = new ArrayList<ComputerServicesOutboundPort>();
		this.cssdops = new ArrayList<ComputerStaticStateDataOutboundPort>();
		this.cdsdops = new ArrayList<ComputerDynamicStateDataOutboundPort>();
		this.nbAvailablesCores = new ArrayList<>();
	}

	@Override
	public void start() throws ComponentStartException {
		super.start();
	}
	 
	
	private void createVM(String appURI, String[] dispatcherUri, int nbVM, AllocatedCore[] allocatedCore) throws Exception {
		
		//TODO : a surveiller
		ReflectionOutboundPort rop = null;
		try {
			rop = new ReflectionOutboundPort(this);
			this.addPort(rop);
			rop.publishPort();
			rop.doConnection(dispatcherUri[0], ReflectionConnector.class.getCanonicalName());
			rop.toggleLogging();
			rop.toggleTracing();
			rop.doDisconnection();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String applicationVM[] = new String[5];

		for(int i =0; i < nbVM; i++) {
			// --------------------------------------------------------------------
			// Create an Application VM component
			// --------------------------------------------------------------------
			applicationVM[0] = "avm-"+this.avmOutPort.size();
			applicationVM[1] = "avmibp-"+this.avmOutPort.size();
			applicationVM[2] = "rsibpVM-"+this.avmOutPort.size();
			applicationVM[3] = "rnobpVM-"+this.avmOutPort.size();
			applicationVM[4] = "avmobp-"+this.avmOutPort.size();
			this.portToRequestDispatcherJVM.createComponent(
					ApplicationVM.class.getCanonicalName(),
					new Object[] {
							applicationVM[0],							
							applicationVM[1],
							applicationVM[2],
							applicationVM[3]
			});
			
			// Create a mock up port to manage the AVM component (allocate cores).
			ApplicationVMManagementOutboundPort avmPort = new ApplicationVMManagementOutboundPort(
					applicationVM[4], this) ;
			avmPort.publishPort() ;
			avmPort.doConnection(applicationVM[1],
						ApplicationVMManagementConnector.class.getCanonicalName()) ;
			this.avmOutPort.add(avmPort);
			avmPort.allocateCores(allocatedCore);

			rdmopMap.get(appURI).connectVirtualMachine(applicationVM[0], applicationVM[2], dispatcherUri[7]+"-"+i);
			avmPort.connectWithRequestSubmissioner(dispatcherUri[0], dispatcherUri[4]);
			rop.doConnection(applicationVM[0], ReflectionConnector.class.getCanonicalName());
			
			rop.toggleTracing();
			rop.toggleLogging();

			rop.doDisconnection();
		}
		
	}


	@Override
	public void submitGenerator(String RequestNotificationInboundPort, String appUri, String rgURI) throws Exception {
		this.rdmopMap.get(appUri).connectWithRequestGenerator(rgURI, RequestNotificationInboundPort);
	}

	@Override
	public void shutdown() throws ComponentShutdownException {
		try {			
			for(ComputerServicesOutboundPort csop : csops) {
				if (csop.connected()) {
					csop.doDisconnection();
				}
			}
			for(ComputerDynamicStateDataOutboundPort cdsdop : cdsdops) {
				if (cdsdop.connected()) {
					cdsdop.doDisconnection();
				}
			}
			
			/*if (this.cssdop.connected()) {
				this.cssdop.doDisconnection();
			}*/
			for(Entry<String, RequestDispatcherManagementOutboundPort> entry : this.rdmopMap.entrySet()) {
				if(entry.getValue().connected()) {
					entry.getValue().doDisconnection();
				}
			}
			if (this.portToApplicationVMJVM.connected()) {
				this.portToApplicationVMJVM.doDisconnection();
			}
			if (this.portToRequestDispatcherJVM.connected()) {
				this.portToRequestDispatcherJVM.doDisconnection();
			}
		} catch (Exception e) {
			throw new ComponentShutdownException("Port disconnection error", e);
		}

		super.shutdown();
	}
	
	private String[] createController(String appURI,String requestDispatcherDynamicStateDataInboundPortURI,String rdURI) throws Exception
	{
		String controllerURIs[] = new String[2];
		controllerURIs[0] = appURI+"-controller";
		controllerURIs[1] = controllerURIs[0]+"-rddsdop";
		
		this.portTControllerJVM.createComponent(
				Controller.class.getCanonicalName(),
				new Object[] {
						controllerURIs[0],
						controllerURIs[1],
						rdURI
		});
		
		ReflectionOutboundPort rop = new ReflectionOutboundPort(this);
		this.addPort(rop);
		rop.publishPort();
		rop.doConnection(controllerURIs[0], ReflectionConnector.class.getCanonicalName());
		rop.doPortConnection(controllerURIs[1],requestDispatcherDynamicStateDataInboundPortURI, ControlledDataConnector.class.getCanonicalName());
		rop.doDisconnection();
		
		return controllerURIs;
	}
	
	private String[] createDispatcher(String appURI, String className) throws Exception {
		
		String dispatcherURI[] = new String[8];
		dispatcherURI[0] = "RD_" + rdmopMap.size()+"_"+appURI;
		dispatcherURI[1] = RequestDispatcherManagementInboundPortURI + "_" + appURI;
		dispatcherURI[2] = RequestSubmissionInboundPortURI +"_" + appURI;
		dispatcherURI[3] = RequestNotificationOutboundPortURI + "_"+ appURI;
		dispatcherURI[4] = RequestNotificationInboundPortURI + "_"+ appURI;
		dispatcherURI[5] = RequestStaticDataInboundPortURI + "_"+ appURI;
		dispatcherURI[6] = RequestDynamicDataInboundPortURI + "_"+ appURI;
		dispatcherURI[7] = RequestSubmissionOutboundPortURI + "_"+ appURI;
		
		this.portToRequestDispatcherJVM.createComponent(
				className,
				new Object[] {
						dispatcherURI[0],							
						dispatcherURI[1],
						dispatcherURI[2],
						dispatcherURI[3],
						dispatcherURI[4],
						dispatcherURI[6]
				});		
	
		RequestDispatcherManagementOutboundPort rdmop = new RequestDispatcherManagementOutboundPort(
				RequestDispatcherManagementOutboundPortURI + rdmopMap.size(),
				this);

		rdmop.publishPort();
		
		rdmop.doConnection(dispatcherURI[1], RequestDispatcherManagementConnector.class.getCanonicalName());
		rdmopMap.put(appURI, rdmop);
		return dispatcherURI;
	}
	

	@Override
	public synchronized String[] submitApplication(String appURI, int nbVM) throws Exception{
		
		this.logMessage("New Application received in dynamic controller ("+appURI+")"+".\n Waiting for evaluation ");
		AllocatedCore[] allocatedCore;
		try {
			allocatedCore = getAvailableCores(NB_CORES);
		} catch (Exception e) {
			this.logMessage("Failed to allocates core for a new application.");
			return null;
		}
		
		String dispatcherUri[] = createDispatcher(appURI, RequestDispatcher.class.getCanonicalName());
		this.createVM(appURI, dispatcherUri, nbVM, allocatedCore);
		this.createController(appURI,dispatcherUri[6],dispatcherUri[0]);
		
		return dispatcherUri;
	}
	
	@Override
	public synchronized String[] submitApplication(String appURI, int nbVM, Class submissionInterface) throws Exception {
		
		assert submissionInterface.isInterface();
		
		this.logMessage("New Application received in dynamic controller ("+appURI+")"+".\n Waiting for evaluation ");
		AllocatedCore[] allocatedCore = null;
		try{
			 allocatedCore = getAvailableCores(NB_CORES);
		}catch(Exception e) {
			this.logMessage("Failed to allocates core for a new application.");
			return null;
		}

		Class<?> dispa = RequestDispatcherCreator.createRequestDispatcher("JAVASSIST-dispa", RequestDispatcher.class, submissionInterface);
		interface_dispatcher_map.put(submissionInterface, dispa);
		
		String dispatcherUri[] = createDispatcher(appURI, dispa.getCanonicalName());
		this.createVM(appURI, dispatcherUri, nbVM, allocatedCore);
		return dispatcherUri;
	}
	
	/**
	 * Return the index of the first available computer
	 * @param nbCores
	 * @return index 
	 * @throws Exception 
	 */
	private AllocatedCore[] getAvailableCores(int nbCores) throws Exception {
		for(int i = 0; i < nbAvailablesCores.size(); i++) {
			if(this.nbAvailablesCores.get(i) >= nbCores) {
				return tryAllocated(csops.get(i), i, nbCores);
			}
		}
		throw new Exception("Impossible d'allouer des cores");
	}
	
	
	private AllocatedCore[] getAvailableCores(ComputerServicesOutboundPort csop, int nbCores) throws Exception {
		for(int i = 0; i < csops.size(); i++) {
			if(csops.get(i) == csop) {
				if(this.nbAvailablesCores.get(i) >= nbCores) {
					return tryAllocated(csop, i, nbCores);
				}else {
					break;
				}
			}
		}
		throw new Exception("Impossible d'allouer des cores");
	}
	
	private AllocatedCore[] tryAllocated(ComputerServicesOutboundPort csop, int index,  int nbCores) throws Exception {
		AllocatedCore[] allocatedCore = csop.allocateCores(nbCores);
		if(allocatedCore!=null && allocatedCore.length != 0) {
			this.nbAvailablesCores.set(index, this.nbAvailablesCores.get(index));
			return allocatedCore;
		}
		else {
			throw new Exception("Impossible d'allouer des cores");
		}
	}

	@Override
	public boolean addCores(String rdURI, int nbCores, String vmUri) {
		ComputerServicesOutboundPort csop = csopMap.get(vmUri);
		try {
			AllocatedCore[] ac = getAvailableCores(csop, nbCores);
			int index = avmOutPort.indexOf(vmUri);
			avmOutPort.get(index).allocateCores(ac);
			return true;
		} catch (Exception e) {
			this.logMessage("Failed to allocates core for a new application.");
			return false;
		}
	}

	@Override
	public void acceptComputerStaticData(String computerURI, ComputerStaticStateI staticState) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void acceptComputerDynamicData(String computerURI, ComputerDynamicStateI currentDynamicState)
			throws Exception {
		//this.reservedCores.put(computerURI, currentDynamicState.getCurrentCoreReservations());
	} 

	@Override
	public void linkComputer(String computerURI, String ComputerServicesInboundPortURI,
			String ComputerStaticStateDataInboundPortURI, String ComputerDynamicStateDataInboundPortURI)
			throws Exception {
		
			String csopUri = AdmissionControllerDynamic.computerServiceOutboundPortURI + "_" +  this.csops.size();

			ComputerServicesOutboundPort csop = new ComputerServicesOutboundPort(csopUri, this);
			this.addPort(csop);
			
			csop.publishPort();
			csop.doConnection(
					ComputerServicesInboundPortURI,
					ComputerServicesConnector.class.getCanonicalName());
			this.csops.add(csop);

			String cssopUri = ComputerStaticStateDataOutboundPortURI + "_" +  this.cssdops.size();
			ComputerStaticStateDataOutboundPort cssdop = new ComputerStaticStateDataOutboundPort(cssopUri, this, computerURI);
			this.addPort(cssdop);
			cssdop.publishPort();
			cssdop.doConnection(
					ComputerStaticStateDataInboundPortURI,
					ControlledDataConnector.class.getCanonicalName());

			ComputerStaticStateI staticState= (ComputerStaticStateI) cssdop.request();
			int nbCores = staticState.getNumberOfCoresPerProcessor();
			int nbProc = staticState.getNumberOfProcessors();
			
			this.nbAvailablesCores.add(nbCores*nbProc);
			
			
			this.cssdops.add(cssdop);
			String cdsdopUri = AdmissionControllerDynamic.computerDynamicStateDataOutboundPortURI + "_" +  this.cdsdops.size();
			ComputerDynamicStateDataOutboundPort cdsdop = new ComputerDynamicStateDataOutboundPort(cdsdopUri, this, computerURI);
			
			this.addPort(cdsdop);
			cdsdop.publishPort();
			cdsdop.doConnection(
					ComputerDynamicStateDataInboundPortURI,
					ControlledDataConnector.class.getCanonicalName());
			cdsdop.startUnlimitedPushing(1000);
			this.cdsdops.add(cdsdop);
	}
}
