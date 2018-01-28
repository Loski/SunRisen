package fr.upmc.PriseTheSun.datacenter.software.admissioncontroller;


import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import fr.upmc.PriseTheSun.datacenter.hardware.computer.ComputerController;
import fr.upmc.PriseTheSun.datacenter.hardware.computer.connector.ComputerControllerConnector;
import fr.upmc.PriseTheSun.datacenter.hardware.computer.interfaces.ComputerControllerManagement;
import fr.upmc.PriseTheSun.datacenter.hardware.computer.ports.ComputerControllerManagementInboundPort;
import fr.upmc.PriseTheSun.datacenter.hardware.computer.ports.ComputerControllerManagementOutboutPort;
import fr.upmc.PriseTheSun.datacenter.hardware.processors.ProcessorsController;
import fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.interfaces.AdmissionControllerManagementI;
import fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.ports.AdmissionControllerManagementInboundPort;
import fr.upmc.PriseTheSun.datacenter.software.applicationvm.ApplicationVMInfo;
import fr.upmc.PriseTheSun.datacenter.software.controller.Controller;
import fr.upmc.PriseTheSun.datacenter.software.controller.connectors.ControllerManagementConnector;
import fr.upmc.PriseTheSun.datacenter.software.controller.ports.ControllerManagementOutboundPort;
import fr.upmc.PriseTheSun.datacenter.software.javassist.RequestDispatcherCreator;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.RequestDispatcher;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.VirtualMachineData;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.connectors.RequestDispatcherManagementConnector;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.ports.RequestDispatcherManagementOutboundPort;
import fr.upmc.PriseTheSun.datacenter.software.ring.RingDynamicState;
import fr.upmc.PriseTheSun.datacenter.software.ring.interfaces.RingDataI;
import fr.upmc.PriseTheSun.datacenter.software.ring.interfaces.RingDynamicStateI;
import fr.upmc.PriseTheSun.datacenter.software.ring.ports.RingDynamicStateDataInboundPort;
import fr.upmc.PriseTheSun.datacenter.software.ring.ports.RingDynamicStateDataOutboundPort;
import fr.upmc.PriseTheSun.datacenterclient.software.applicationprovider.interfaces.ApplicationSubmissionI;
import fr.upmc.PriseTheSun.datacenterclient.software.applicationprovider.ports.ApplicationSubmissionInboundPort;
import fr.upmc.components.AbstractComponent;
import fr.upmc.components.ComponentI;
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
import fr.upmc.datacenter.hardware.processors.Processor;
import fr.upmc.datacenter.hardware.processors.Processor.ProcessorPortTypes;
import fr.upmc.datacenter.interfaces.PushModeControllingI;
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
public class AdmissionControllerDynamic extends AbstractComponent implements ApplicationSubmissionI, AdmissionControllerManagementI, RingDataI, PushModeControllingI{

	public static int DEBUG_LEVEL = 1 ;
	
	protected String acURI;
	protected static final String RequestDispatcherManagementInboundPortURI = "rdmip";
	protected static final String RequestNotificationInboundPortURI = "rnip";
	protected static final String RequestSubmissionInboundPortURI = "rsip";
	protected static final String RequestNotificationOutboundPortURI = "rnop";
	protected static final String RequestDispatcherManagementOutboundPortURI = "rdmop";
	protected static final String RequestSubmissionOutboundPortURI = "rsop"; 
	protected static final String RequestStaticDataInboundPortURI = "rsdip";
	protected static final String RequestDynamicDataInboundPortURI = "rddip";
	protected static final String computerServiceOutboundPortURI = "csop";
	protected static final String computerDynamicStateDataOutboundPortURI = "cdsdop";
	protected static final String ComputerStaticStateDataOutboundPortURI = "cssdop";
	protected static final String ProcessorControllerManagementInboundPortURI = "pcmip";
	protected static final String ControllerDataRingInboundPortURI = "cdrip";
	protected static final String ControllerDataRingOutboundPortURI = "cdrop";
	protected static final String ComputerControllerManagementInboundPortURI = "ccmip";
	protected static final String ComputerControllerManagementUri = "ccm";


	

	protected AdmissionControllerManagementInboundPort acmip;
	protected ApplicationSubmissionInboundPort asip;
	
	
	protected Map<String, ApplicationVMManagementOutboundPort> avmOutPort;
	

	protected ArrayList<ComputerStaticStateDataOutboundPort> cssdops;
	protected ArrayList<ComputerDynamicStateDataOutboundPort> cdsdops;
	protected ArrayList<ComputerControllerManagementOutboutPort > cmops;

	protected ScheduledFuture<?>	pushingFuture ;
	
	
	// Map between RequestDispatcher URIs and the management ports to call them.
	protected Map<String, RequestDispatcherManagementOutboundPort> rdmopMap;
	
	//Map Between a vm and his computer
	protected List<ApplicationVMInfo> FreeApplicationVM;
	protected List<ApplicationVMInfo> OccupedApplicationVM;
	private ProcessorsController processorController;

	Object o=new Object();
	protected static final int NB_CORES = 2;
	
	private static final int RING_PUSH_INTERVAL = 50;

	private DynamicComponentCreationOutboundPort portTControllerJVM;
	
	/*Port du la structure en anneau pour relier chaque controller*/ 
	private RingDynamicStateDataOutboundPort rdsdop;
	private RingDynamicStateDataInboundPort rdsdip;
	private String AdmissionControllerDataRingOutboundUri;
	private String AdmissionControllerDataRingInboundUri;

	private String nextControllerDataRingUri;
	private Object previousControllerDataRingUri;
	

	/*protected static final String RequestDispatcher_JVM_URI = "controller" ;
	protected static final String Application_VM_JVM_URI = "controller";*/
	
	public AdmissionControllerDynamic(String acURI,
			String applicationSubmissionInboundPortURI,
			String AdmissionControllerManagementInboundPortURI,
			String Controller_JVM_URI
			) throws Exception {
		
		super(acURI,2, 2);
		
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
		
		
		this.portTControllerJVM = new DynamicComponentCreationOutboundPort(this);
		this.portTControllerJVM.publishPort();
		this.addPort(this.portTControllerJVM);
		
		this.portTControllerJVM.doConnection(					
				Controller_JVM_URI + AbstractCVM.DCC_INBOUNDPORT_URI_SUFFIX,
				DynamicComponentCreationConnector.class.getCanonicalName());
		this.addRequiredInterface(ComputerServicesI.class);
		
		
		this.AdmissionControllerDataRingInboundUri = this.acURI +"-"+ControllerDataRingInboundPortURI;
		this.AdmissionControllerDataRingOutboundUri = this.acURI +"-"+ControllerDataRingOutboundPortURI;
		
		this.addRequiredInterface(RingDynamicStateI.class);
		this.addOfferedInterface(RingDynamicStateI.class);
		
		rdsdop = new RingDynamicStateDataOutboundPort(this,  this.AdmissionControllerDataRingOutboundUri);
		this.addPort(rdsdop);
		this.rdsdop.publishPort();

		rdsdip=new RingDynamicStateDataInboundPort(this.AdmissionControllerDataRingInboundUri, this);
		this.addPort(rdsdip) ;
		this.rdsdip.publishPort();
		this.avmOutPort = new HashMap<String, ApplicationVMManagementOutboundPort>();
		this.rdmopMap = new HashMap<String, RequestDispatcherManagementOutboundPort>();
		this.cssdops = new ArrayList<ComputerStaticStateDataOutboundPort>();
		this.cdsdops = new ArrayList<ComputerDynamicStateDataOutboundPort>();
		this.cmops = new ArrayList<ComputerControllerManagementOutboutPort>();
		this.FreeApplicationVM = new ArrayList<>();
		this.OccupedApplicationVM = new ArrayList<>();
		this.processorController = new ProcessorsController("controller", ProcessorControllerManagementInboundPortURI);
	}

	@Override
	public void start() throws ComponentStartException {
		super.start();
	}
	 
	@Deprecated
	private void allocVm(String appURI, String[] dispatcherUri, ApplicationVMInfo wrap) throws Exception {
		//Ajout dans les VMs réservés
		this.OccupedApplicationVM.add(wrap);
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
		allocVm(appURI, wrap, dispatcherUri[0], dispatcherUri[4]);
		rop.doConnection(wrap.getApplicationVM(), ReflectionConnector.class.getCanonicalName());
		rop.toggleTracing();
		rop.toggleLogging();
		rop.doDisconnection();
	}

	@Deprecated
	public void allocVm(String appURI, ApplicationVMInfo vm, String dispatcherURI, String dispatcherNotificationInboundPort) throws Exception {
		rdmopMap.get(appURI).connectVirtualMachine(vm.getApplicationVM(), vm.getSubmissionInboundPortUri());
		this.avmOutPort.get(vm.getApplicationVM()).connectWithRequestSubmissioner(dispatcherURI, dispatcherNotificationInboundPort);
	}
	
	@Override
	public void submitGenerator(String RequestNotificationInboundPort, String appUri, String rgURI) throws Exception {
		this.rdmopMap.get(appUri).connectWithRequestGenerator(rgURI, RequestNotificationInboundPort);
	}

	@Override
	public void shutdown() throws ComponentShutdownException {
		try {			
			for(ComputerControllerManagementOutboutPort cmop : this.cmops) {
				if (cmop.connected()) {
					cmop.doDisconnection();
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

			if (this.portTControllerJVM.connected()) {
				this.portTControllerJVM.doDisconnection();
			}
		} catch (Exception e) {
			throw new ComponentShutdownException("Port disconnection error", e);
		}

		super.shutdown();
	}
	
	private String[] createController(String appURI, String requestDispatcherDynamicStateDataInboundPortURI, String rdURI, ApplicationVMInfo vm) throws Exception
	{
		String controllerURIs[] = new String[6];
		controllerURIs[0] = appURI + "-controller";
		controllerURIs[1] = appURI +"-controllermngt";
		controllerURIs[2] = controllerURIs[0]+"-rddsdop";
		controllerURIs[3] = controllerURIs[0]+"-"+ControllerDataRingOutboundPortURI;
		controllerURIs[4] = controllerURIs[0]+"-"+ ControllerDataRingInboundPortURI;
		//next dataring inbound uri
		controllerURIs[5] = null;
		
		String previous = this.AdmissionControllerDataRingInboundUri;
		/*Linking Ring*/
		if(nextControllerDataRingUri==null){
			controllerURIs[5] = this.AdmissionControllerDataRingInboundUri;
		}else{ 
			stopPushing();
			controllerURIs[5] = nextControllerDataRingUri;
			//Connexion de l'ADMC au controller
			if(rdsdop.connected())
				rdsdop.doDisconnection();
		}
		
		nextControllerDataRingUri = controllerURIs[4];

		this.portTControllerJVM.createComponent(
				Controller.class.getCanonicalName(),
				new Object[] {
						appURI,
						controllerURIs[0],
						controllerURIs[1],
						controllerURIs[2],
						rdURI,
						requestDispatcherDynamicStateDataInboundPortURI,
						this.acmip.getPortURI(),
						ProcessorControllerManagementInboundPortURI,
						controllerURIs[3],
						controllerURIs[4],
						controllerURIs[5],
						vm
		});
		
		rdsdop.doConnection(controllerURIs[4], ControlledDataConnector.class.getCanonicalName());
		this.OccupedApplicationVM.add(vm);
		this.startUnlimitedPushing(10);
		
		
		/*ReflectionOutboundPort rop = new ReflectionOutboundPort(this);
		this.addPort(rop);
		rop.publishPort();
		rop.doConnection(controllerURIs[0], ReflectionConnector.class.getCanonicalName());
		rop.doPortConnection(controllerURIs[1],requestDispatcherDynamicStateDataInboundPortURI, ControlledDataConnector.class.getCanonicalName());
		rop.doDisconnection();*/
		
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
		
		this.portTControllerJVM.createComponent(
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
		ApplicationVMInfo vm;
		synchronized(o){
			if(this.FreeApplicationVM.size() > 2) {
				vm = FreeApplicationVM.remove(0);
			}else {
				return null;
			}
		}
		
		String dispatcherUri[] = createDispatcher(appURI, RequestDispatcher.class.getCanonicalName());
		this.createController(appURI,dispatcherUri[6],dispatcherUri[0], vm);
		return dispatcherUri;
	}
	
	@Override
	public synchronized String[] submitApplication(String appURI, int nbVM, Class submissionInterface) throws Exception {
		
		assert submissionInterface.isInterface();
		
		this.logMessage("New Application received in dynamic controller ("+appURI+")"+".\n Waiting for evaluation ");
		ApplicationVMInfo vm;
		
		synchronized(o){
			if(this.FreeApplicationVM.size() > 2) {
				vm = FreeApplicationVM.remove(0);
			}else {
				return null;
			}
		}
		
		Class<?> dispa = RequestDispatcherCreator.createRequestDispatcher("JAVASSIST-dispa", RequestDispatcher.class, submissionInterface);
		String dispatcherUri[] = createDispatcher(appURI, dispa.getCanonicalName());
		this.createController(appURI,dispatcherUri[6],dispatcherUri[0], vm);
		return dispatcherUri;
	}
	
	private ApplicationVMManagementOutboundPort findVM(String vmUri) throws Exception {
		return avmOutPort.get(vmUri);
	}

	@Override
	public void linkComputer(String computerURI, String ComputerServicesInboundPortURI,
		String ComputerStaticStateDataInboundPortURI, String ComputerDynamicStateDataInboundPortURI)
		throws Exception {
	


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
		this.cssdops.add(cssdop);

        ArrayList<String> processorsURIs = new ArrayList<String>();
        ArrayList<String> pmipURIs = new ArrayList<String>();
        ArrayList<String> pssdURIs = new ArrayList<String>();
        ArrayList<String> pdsdURIs = new ArrayList<String>();
        Map<Integer, String> processorURIsMap = staticState.getProcessorURIs();
        
        
        for (Map.Entry<Integer, String> entry : processorURIsMap.entrySet()) {
            Map<ProcessorPortTypes, String> pPortsList = staticState.getProcessorPortMap()
                    .get(entry.getValue());
            processorsURIs.add(entry.getValue());
            pmipURIs.add(pPortsList.get(Processor.ProcessorPortTypes.MANAGEMENT));
            pssdURIs.add(pPortsList.get(Processor.ProcessorPortTypes.STATIC_STATE));
            pdsdURIs.add(pPortsList.get(Processor.ProcessorPortTypes.DYNAMIC_STATE));
        }
        
        String computerController[] = new String[4];
        computerController[0] = ComputerControllerManagementUri + cmops.size();						
		computerController[1] = ComputerServicesInboundPortURI;
		computerController[2] = ComputerControllerManagementInboundPortURI + cmops.size();

		
		
        ComputerController tmp = new ComputerController(computerController[0], computerController[1], computerController[2]);
        ComputerControllerManagementOutboutPort ccmop = new ComputerControllerManagementOutboutPort("ComputerControllerManagementOutboutPort" + cmops.size(), this);
		
        this.addPort(ccmop);	
		ccmop.publishPort();
		ccmop.doConnection(
				computerController[2],
				ComputerControllerConnector.class.getCanonicalName());
		this.cmops.add(ccmop);			
			
			for(int i = 0; i < processorsURIs.size(); i++) {
				this.processorController.bindProcessor(processorsURIs.get(i), "ACHANGER", pmipURIs.get(i), pssdURIs.get(i), pdsdURIs.get(i));
				createVM(this.processorController, computerController[2], ccmop.allocateCores(nbCores/2));
			}
	}
	
	private synchronized String createVM(ProcessorsController controller, String computerManagementInboundPortURI, AllocatedCore[] allocatedCore) throws Exception {
		
		String applicationVM[] = new String[5];
		int nbVM = avmOutPort.size(); //Add VM non occupé
		// --------------------------------------------------------------------
		// Create an Application VM component
		// --------------------------------------------------------------------
		applicationVM[0] = "avm-"+nbVM;
		applicationVM[1] = "avmibp-"+nbVM;
		applicationVM[2] = "rsibpVM-"+nbVM;
		applicationVM[3] = "rnobpVM-"+nbVM;
		applicationVM[4] = "avmobp-"+nbVM;
		
		this.portTControllerJVM.createComponent(
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
					ApplicationVMManagementConnector.class.getCanonicalName());
		avmPort.allocateCores(allocatedCore);
		this.avmOutPort.put(applicationVM[0], avmPort);
		
		synchronized (o) {
			this.FreeApplicationVM.add(new ApplicationVMInfo(applicationVM[0], applicationVM[1], applicationVM[2], computerManagementInboundPortURI ));
		}
		
		return applicationVM[0];
	}

	@Override
	public void acceptRingDynamicData(String requestDispatcherURI, RingDynamicStateI currentDynamicState)
			throws Exception {
		synchronized(o){
			if(!currentDynamicState.getApplicationVMsInfo().isEmpty())
				FreeApplicationVM.addAll(currentDynamicState.getApplicationVMsInfo()); 
		}

	}

	public void	sendDynamicState() throws Exception
	{
		if (this.rdsdip.connected()) {
			RingDynamicStateI rds = this.getDynamicState() ;
			this.rdsdip.send(rds) ;
		}
	}

	public void	sendDynamicState(final int interval, int numberOfRemainingPushes) throws Exception{
		this.sendDynamicState() ;
		final int fNumberOfRemainingPushes = numberOfRemainingPushes - 1 ;
		if (fNumberOfRemainingPushes > 0) {
			final AdmissionControllerDynamic c = this ;
			this.pushingFuture =
					this.scheduleTask(
							new ComponentI.ComponentTask() {
								@Override
								public void run() {
									try {
										c.sendDynamicState(
												interval,
												fNumberOfRemainingPushes) ;
									} catch (Exception e) {
										e.printStackTrace();
										throw new RuntimeException(e) ;
									}
								}
							}, interval, TimeUnit.MILLISECONDS) ;
		}
	}
	
	public RingDynamicState getDynamicState() throws UnknownHostException {
		synchronized(o){
			ArrayList<ApplicationVMInfo> copy=new ArrayList<>(FreeApplicationVM);
			RingDynamicState rds = new RingDynamicState(copy);
			//Suppression car envoie
			FreeApplicationVM.clear();
			return rds;
		}
	}

	@Override
	public void startUnlimitedPushing(int interval) throws Exception {
		// first, send the static state if the corresponding port is connected
		//this.sendStaticState() ;
		final AdmissionControllerDynamic c = this ;
		this.pushingFuture =
				this.scheduleTaskAtFixedRate(
						new ComponentI.ComponentTask() {
							@Override
							public void run() {
								try {
									c.sendDynamicState() ;
								} catch (Exception e) {
									throw new RuntimeException(e) ;
								}
							}
						}, interval, interval, TimeUnit.MILLISECONDS) ;

	}

	@Override
	public void startLimitedPushing(final int interval, final int n) throws Exception {
		assert	n > 0 ;
		this.logMessage(this.acURI + " startLimitedPushing with interval "
				+ interval + " ms for " + n + " times.") ;

		// first, send the static state if the corresponding port is connected
		//this.sendStaticState() ;

		final AdmissionControllerDynamic c = this ;
		this.pushingFuture =
				this.scheduleTask(
						new ComponentI.ComponentTask() {
							@Override
							public void run() {
								try {
									c.sendDynamicState(interval, n) ;
								} catch (Exception e) {
									throw new RuntimeException(e) ;
								}
							}
						}, interval, TimeUnit.MILLISECONDS) ;
	}

	@Override
	public void stopPushing() throws Exception {
		if (this.pushingFuture != null &&
				!(this.pushingFuture.isCancelled() ||
						this.pushingFuture.isDone())) {
			this.pushingFuture.cancel(false) ;
		}
}

}
