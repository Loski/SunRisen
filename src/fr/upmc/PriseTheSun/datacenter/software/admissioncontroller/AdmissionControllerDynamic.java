package fr.upmc.PriseTheSun.datacenter.software.admissioncontroller;


import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import fr.upmc.PriseTheSun.datacenter.hardware.computer.ComputerController;
import fr.upmc.PriseTheSun.datacenter.hardware.computer.connector.ComputerControllerConnector;
import fr.upmc.PriseTheSun.datacenter.hardware.computer.interfaces.ComputerControllerManagementI;
import fr.upmc.PriseTheSun.datacenter.hardware.computer.ports.ComputerControllerManagementOutboutPort;
import fr.upmc.PriseTheSun.datacenter.hardware.processors.ProcessorsController;
import fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.interfaces.AdmissionControllerManagementI;
import fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.ports.AdmissionControllerManagementInboundPort;
import fr.upmc.PriseTheSun.datacenter.software.applicationvm.ApplicationVMInfo;
import fr.upmc.PriseTheSun.datacenter.software.controller.Controller;
import fr.upmc.PriseTheSun.datacenter.software.controller.connectors.NodeManagementConnector;
import fr.upmc.PriseTheSun.datacenter.software.controller.interfaces.NodeRingManagementI;
import fr.upmc.PriseTheSun.datacenter.software.controller.ports.NodeManagementInboundPort;
import fr.upmc.PriseTheSun.datacenter.software.controller.ports.NodeManagementOutboundPort;
import fr.upmc.PriseTheSun.datacenter.software.javassist.RequestDispatcherCreator;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.RequestDispatcher;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.connectors.RequestDispatcherManagementConnector;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.ports.RequestDispatcherManagementOutboundPort;
import fr.upmc.PriseTheSun.datacenter.software.ring.RingDynamicState;
import fr.upmc.PriseTheSun.datacenter.software.ring.interfaces.RingNetworkDynamicStateI;
import fr.upmc.PriseTheSun.datacenter.software.ring.interfaces.RingNetworkStateDataConsumerI;
import fr.upmc.PriseTheSun.datacenter.software.ring.ports.RingNetworkDynamicStateDataInboundPort;
import fr.upmc.PriseTheSun.datacenter.software.ring.ports.RingNetworkDynamicStateDataOutboundPort;
import fr.upmc.PriseTheSun.datacenter.tools.Writter;
import fr.upmc.PriseTheSun.datacenterclient.software.applicationprovider.interfaces.ApplicationSubmissionI;
import fr.upmc.PriseTheSun.datacenterclient.software.applicationprovider.ports.ApplicationSubmissionInboundPort;
import fr.upmc.components.AbstractComponent;
import fr.upmc.components.ComponentI;
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.components.cvm.pre.dcc.connectors.DynamicComponentCreationConnector;
import fr.upmc.components.cvm.pre.dcc.interfaces.DynamicComponentCreationI;
import fr.upmc.components.cvm.pre.dcc.ports.DynamicComponentCreationOutboundPort;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.components.exceptions.ComponentStartException;
import fr.upmc.datacenter.connectors.ControlledDataConnector;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;
import fr.upmc.datacenter.hardware.computers.interfaces.ComputerDynamicStateI;
import fr.upmc.datacenter.hardware.computers.interfaces.ComputerServicesI;
import fr.upmc.datacenter.hardware.computers.interfaces.ComputerStateDataConsumerI;
import fr.upmc.datacenter.hardware.computers.interfaces.ComputerStaticStateI;
import fr.upmc.datacenter.hardware.computers.ports.ComputerDynamicStateDataOutboundPort;
import fr.upmc.datacenter.hardware.computers.ports.ComputerStaticStateDataOutboundPort;
import fr.upmc.datacenter.hardware.processors.Processor;
import fr.upmc.datacenter.hardware.processors.Processor.ProcessorPortTypes;
import fr.upmc.datacenter.interfaces.ControlledDataOfferedI;
import fr.upmc.datacenter.interfaces.ControlledDataRequiredI;
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
 * 
 * 
 * @author	Maxime LAVASTE Loïc LAFONTAINE
 */
public class AdmissionControllerDynamic extends AbstractComponent 
implements 	ApplicationSubmissionI, 
			AdmissionControllerManagementI, 
			RingNetworkStateDataConsumerI, 
			PushModeControllingI, 
			NodeRingManagementI,
			ComputerStateDataConsumerI
{

	public static int DEBUG_LEVEL = 1 ;
	
	protected String admissionControllerURI;
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
	
	
	/** Ring static variable */
	protected static final String ControllerDataRingInboundPortURI = "cdrip";
	protected static final String ControllerDataRingOutboundPortURI = "cdrop";
	protected static final String NODE_MANAGEMENT = "nmip";

	protected static final String ComputerControllerManagementInboundPortURI = "ccmip";
	protected static final String ComputerControllerManagementUri = "ccm";
	protected static final String VMDisconnectionHandlerOutboundPort = "VMDisconnectionHandlerOutboundPort";
	private Writter w;

	protected AdmissionControllerManagementInboundPort acmip;
	protected ApplicationSubmissionInboundPort asip;
	
	
	protected Map<String, ApplicationVMManagementOutboundPort> avmOutPort;
	
	private static int nbappli = 0;
	protected ArrayList<ComputerStaticStateDataOutboundPort> cssdops;
	protected ArrayList<ComputerDynamicStateDataOutboundPort> cdsdops;
	protected ArrayList<ComputerControllerManagementOutboutPort > cmops;

	protected ScheduledFuture<?>	pushingFuture ;
	
	/** Set pour check le nombre de vm dans le ring */
	protected HashSet<String> vmURis;
	
	// Map between RequestDispatcher URIs and the management ports to call them.
	protected Map<String, RequestDispatcherManagementOutboundPort> rdmopMap;
	
	//Map Between a vm and his computer
	protected List<ApplicationVMInfo> freeApplicationVM;
	private ProcessorsController processorController;

	Object o=new Object();
	protected static final int NB_CORES = 2;
	

	private static final int MIN_VM = 5;

	

	private DynamicComponentCreationOutboundPort portTControllerJVM;
	
	/*Port du la structure en anneau pour relier chaque controller*/ 
	private RingNetworkDynamicStateDataOutboundPort rdsdop;
	private RingNetworkDynamicStateDataInboundPort rdsdip;
	private String AdmissionControllerDataRingOutboundUri;
	private String AdmissionControllerDataRingInboundUri;
	private NodeManagementInboundPort nmip;
	private String nextControllerDataRingUri;
	private String previousControllerManagement;

	private ArrayList<ApplicationVMInfo> VMforNewApplication;

	private String controllerManagementNextInboundPort;

	private String controllerManagementPreviousInboundPort;
	private String ADMNodeControllerManagementInboundPort;

	private String nextControllerManagement;
	
	private HashMap<String,Class<?>> submissionInterfaces;

	/*protected static final String RequestDispatcher_JVM_URI = "controller" ;
	protected static final String Application_VM_JVM_URI = "controller";*/
	/**
	 * Créer un <code>AdmissionControllerDynamic</code> à partir des URIs données en paramètre
	 * @param admissionControllerURI
	 * @param applicationSubmissionInboundPortURI
	 * @param AdmissionControllerManagementInboundPortURI
	 * @param Controller_JVM_URI URI pour repérer la JVM en mode DCVM
	 * @throws Exception
	 */
	public AdmissionControllerDynamic(String admissionControllerURI,
			String applicationSubmissionInboundPortURI,
			String AdmissionControllerManagementInboundPortURI,
			String Controller_JVM_URI
			) throws Exception {
		
		super(admissionControllerURI,2, 2);
		
		this.addRequiredInterface(ComputerServicesI.class);
		this.addRequiredInterface(ControlledDataRequiredI.ControlledPullI.class);
		this.addRequiredInterface(ComputerControllerManagementI.class);
		
		this.addOfferedInterface(ComputerControllerManagementI.class);
		this.addOfferedInterface(ControlledDataOfferedI.ControlledPullI.class);
		this.addOfferedInterface(ApplicationSubmissionI.class);
		this.addOfferedInterface(AdmissionControllerManagementI.class);
		this.addOfferedInterface(DynamicComponentCreationI.class);
		this.addOfferedInterface(NodeRingManagementI.class);

		
		this.toggleLogging();
		this.toggleTracing();
		this.admissionControllerURI = admissionControllerURI;	

		this.asip = new ApplicationSubmissionInboundPort(applicationSubmissionInboundPortURI, this);
		this.addPort(asip);
		this.asip.publishPort();

		this.acmip = new AdmissionControllerManagementInboundPort(AdmissionControllerManagementInboundPortURI, AdmissionControllerManagementI.class, this);
		this.addPort(acmip);
		this.acmip.publishPort();
		
		

		this.portTControllerJVM = new DynamicComponentCreationOutboundPort(this);
		this.portTControllerJVM.publishPort();
		this.addPort(this.portTControllerJVM);
		
		this.portTControllerJVM.doConnection(					
				Controller_JVM_URI + AbstractCVM.DCC_INBOUNDPORT_URI_SUFFIX,
				DynamicComponentCreationConnector.class.getCanonicalName());
		
		this.AdmissionControllerDataRingInboundUri = this.admissionControllerURI +"-"+ControllerDataRingInboundPortURI;
		this.AdmissionControllerDataRingOutboundUri = this.admissionControllerURI +"-"+ControllerDataRingOutboundPortURI;
		

		
		rdsdop = new RingNetworkDynamicStateDataOutboundPort(this,  this.AdmissionControllerDataRingOutboundUri);
		this.addPort(rdsdop);
		this.rdsdop.publishPort();

		rdsdip=new RingNetworkDynamicStateDataInboundPort(this.AdmissionControllerDataRingInboundUri, this);
		this.addPort(rdsdip) ;
		this.rdsdip.publishPort();
		
		
		ADMNodeControllerManagementInboundPort = admissionControllerURI + NODE_MANAGEMENT;
		nmip = new NodeManagementInboundPort(ADMNodeControllerManagementInboundPort, this);
		this.addPort(nmip);
		this.nmip.publishPort();
		
		this.avmOutPort = new HashMap<String, ApplicationVMManagementOutboundPort>();
		this.rdmopMap = new HashMap<String, RequestDispatcherManagementOutboundPort>();
		this.cssdops = new ArrayList<ComputerStaticStateDataOutboundPort>();
		this.cdsdops = new ArrayList<ComputerDynamicStateDataOutboundPort>();
		this.cmops = new ArrayList<ComputerControllerManagementOutboutPort>();
		this.freeApplicationVM = new ArrayList<>();
		this.VMforNewApplication = new ArrayList<>();
		this.processorController = new ProcessorsController("controller", ProcessorControllerManagementInboundPortURI);
		this.vmURis = new HashSet<>();
		this.submissionInterfaces = new HashMap<>();
		
		w = new Writter(this.admissionControllerURI+ ".csv");

	}

	/**
	 * @see fr.upmc.components.AbstractComponent#start()
	 */
	@Override
	public void start() throws ComponentStartException {
		super.start();
	}
	 

	/**
	 * @see fr.upmc.PriseTheSun.datacenterclient.software.applicationprovider.interfaces.ApplicationSubmissionI#submitGenerator(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void submitGenerator(String RequestNotificationInboundPort, String appUri, String rgURI) throws Exception {
		this.rdmopMap.get(appUri).connectWithRequestGenerator(rgURI, RequestNotificationInboundPort);
	}

	/**
	 * @see fr.upmc.components.AbstractComponent#shutdown()
	 */
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
	
	/**
	 * Créer un nouveau controller lié à un dispatcher. Le controller aura accès à <code>RequestDispatcherDynamicStateI</code> ainsi que <code>RequestDispatcherManagementI</code>.
	 * Le controller est ensuite inséré dans le réseau en anneau des controllers pour pouvoir recevoir des VMs.
	 * @param appURI
	 * @param requestDispatcherDynamicStateDataInboundPortURI
	 * @param rdURI
	 * @param vm Data d'une VM.
	 * @return Tableau d'URI du controller.
	 * <ul>
	 * 
	 * </ul>
	 * @throws Exception
	 */
	private synchronized String[] createController(String appURI, String requestDispatcherDynamicStateDataInboundPortURI, String VMDisconnectionHandlerOutboundPortURI, String rdURI, ApplicationVMInfo vm) throws Exception
	{
		String controllerURIs[] = new String[9];
		controllerURIs[0] = appURI + "-controller";
		controllerURIs[1] = appURI +"-controllermngt";
		controllerURIs[2] = controllerURIs[0]+"-rddsdop";
		controllerURIs[3] = controllerURIs[0]+"-"+ControllerDataRingOutboundPortURI;
		controllerURIs[4] = controllerURIs[0]+"-"+ ControllerDataRingInboundPortURI;
		//next dataring inbound uri
		controllerURIs[5] = null;
		controllerURIs[6] = controllerURIs[0]+"-VMDisconnectionHandler";
		controllerURIs[7] = ADMNodeControllerManagementInboundPort;
		controllerURIs[8] = null;
		nbappli++;

		boolean first = false;
		/*First node*/
		if(nextControllerDataRingUri==null){
			controllerURIs[5] = this.AdmissionControllerDataRingInboundUri;
			controllerURIs[8] = ADMNodeControllerManagementInboundPort;
			nextControllerManagement = ADMNodeControllerManagementInboundPort;
			first = true;
		}else{ 
			stopPushing();
			controllerURIs[5] = nextControllerDataRingUri;
			controllerURIs[8] = nextControllerManagement;
			if(this.rdsdop.connected()) {
				this.rdsdop.doDisconnection();
			}
		}
		
 
		try {
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
							controllerURIs[7],
							controllerURIs[8],
							vm,
							controllerURIs[6]
			});
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		
		rdsdop.doConnection(controllerURIs[4], ControlledDataConnector.class.getCanonicalName()); 
		this.bindSendingDataUri(controllerURIs[4]);
		this.startPushing();
		try {
			if(first) {
				this.setPreviousManagementInboundPort(controllerURIs[1]);
			}else {
				NodeManagementOutboundPort cmopPrevious = new NodeManagementOutboundPort("cmop-previous-"+this.admissionControllerURI + appURI, this);
				this.addPort(cmopPrevious);
		
				cmopPrevious.publishPort();
				cmopPrevious.doConnection(nextControllerManagement, NodeManagementConnector.class.getCanonicalName());
				cmopPrevious.setPreviousManagementInboundPort(controllerURIs[1]);
				cmopPrevious.doDisconnection();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}

		//sauvegarde pour le prochain noeud
		nextControllerDataRingUri = controllerURIs[4];
		nextControllerManagement = controllerURIs[1];
		return controllerURIs;
	}
	
	/**
	 * Créer un nouveau dispatcher
	 * @param appURI URI de l'application acceptée
	 * @param className Constructeur de dispatcher à appeler, soit celui créer dynamiquement par Javassist, soit le statique.
	 * @return tableau des URIs du dispatcher
	 * <ul>
	 * 	<li>dispatcherURI[0] = RequestDispatcherURI</li>
	 *	<li>dispatcherURI[1] = RequestDispatcherManagementInboundPortURI</li>
	 *	<li>dispatcherURI[2] = RequestSubmissionInboundPortURI</li>
	 *	<li>dispatcherURI[3] = RequestNotificationOutboundPortURI</li>
	 *	<li>dispatcherURI[4] = RequestNotificationInboundPortURI</li>
	 *	<li>dispatcherURI[5] = RequestStaticDataInboundPortURI</li>
	 *	<li>dispatcherURI[6] = RequestDynamicDataInboundPortURI</li>
	 *	<li>dispatcherURI[7] = RequestSubmissionOutboundPortURI</li>
	 * </ul>
	 * @throws Exception
	 */
	private String[] createDispatcher(String appURI, String className) throws Exception {
		
		String dispatcherURI[] = new String[9];
		dispatcherURI[0] = "RD_" + rdmopMap.size()+"_"+appURI;
		dispatcherURI[1] = RequestDispatcherManagementInboundPortURI + "_" + appURI;
		dispatcherURI[2] = RequestSubmissionInboundPortURI +"_" + appURI;
		dispatcherURI[3] = RequestNotificationOutboundPortURI + "_"+ appURI;
		dispatcherURI[4] = RequestNotificationInboundPortURI + "_"+ appURI;
		dispatcherURI[5] = RequestStaticDataInboundPortURI + "_"+ appURI;
		dispatcherURI[6] = RequestDynamicDataInboundPortURI + "_"+ appURI;
		dispatcherURI[7] = RequestSubmissionOutboundPortURI + "_"+ appURI;
		dispatcherURI[8] = VMDisconnectionHandlerOutboundPort + "_"+ appURI;
		
		this.portTControllerJVM.createComponent(
				className,
				new Object[] {
						dispatcherURI[0],							
						dispatcherURI[1],
						dispatcherURI[2],
						dispatcherURI[3],
						dispatcherURI[4],
						dispatcherURI[6],
						dispatcherURI[8]
				});		
	
		RequestDispatcherManagementOutboundPort rdmop = new RequestDispatcherManagementOutboundPort(
				RequestDispatcherManagementOutboundPortURI + rdmopMap.size(),
				this);
		
		rdmop.publishPort();
		rdmop.doConnection(dispatcherURI[1], RequestDispatcherManagementConnector.class.getCanonicalName());
		rdmopMap.put(appURI, rdmop);
		return dispatcherURI;
	}
	
	/**
	 * @see fr.upmc.PriseTheSun.datacenterclient.software.applicationprovider.interfaces.ApplicationSubmissionI#submitApplication(java.lang.String, int)
	 */
	@Override
	public synchronized String[] submitApplication(String appURI, int nbVM) throws Exception{
		
		this.logMessage("New Application received in dynamic controller ("+appURI+")"+".\n Waiting for evaluation ");
		w.write(Arrays.asList("application accepted"));

		ApplicationVMInfo vm;
		synchronized(o){
			if(!this.VMforNewApplication.isEmpty()) {
				vm = VMforNewApplication.remove(0);
			}else {
				System.out.println("kek");
				return null;
			}
		}
		
		String dispatcherUri[] = createDispatcher(appURI, RequestDispatcher.class.getCanonicalName());
		String controllerUris[] = this.createController(appURI,dispatcherUri[6],dispatcherUri[8],dispatcherUri[0], vm);
		
		this.rdmopMap.get(appURI).connectController(controllerUris[0],controllerUris[6]);
		

		return dispatcherUri;
	}
	
	/**
	 * @see fr.upmc.PriseTheSun.datacenterclient.software.applicationprovider.interfaces.ApplicationSubmissionI#submitApplication(java.lang.String, int, java.lang.Class)
	 */
	@Override
	public synchronized String[] submitApplication(String appURI, int nbVM, Class submissionInterface) throws Exception {
		
		assert submissionInterface.isInterface();
		
		this.logMessage("New Application received in dynamic controller ("+appURI+")"+".\n Waiting for evaluation ");
		ApplicationVMInfo vm;
		
		synchronized(o){
			if(!this.VMforNewApplication.isEmpty()) {
				vm = VMforNewApplication.remove(0);
			}else {
				return null;
			}
		}
		
		Class<?> dispa = this.submissionInterfaces.get(submissionInterface.getName());
		
		if(dispa==null)
		{
			dispa = RequestDispatcherCreator.createRequestDispatcher("JAVASSIST-dispa", RequestDispatcher.class, submissionInterface);
			this.submissionInterfaces.put(submissionInterface.getName(), dispa);
		}
		
		String dispatcherUri[] = createDispatcher(appURI, dispa.getCanonicalName());
		
		String controllerUris[] = this.createController(appURI,dispatcherUri[6],dispatcherUri[8],dispatcherUri[0], vm);

		this.rdmopMap.get(appURI).connectController(controllerUris[0],controllerUris[6]);
		
		return dispatcherUri;
		
	}
	
	private ApplicationVMManagementOutboundPort findVM(String vmUri) throws Exception {
		return avmOutPort.get(vmUri);
	}

	/**
	 * @see fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.interfaces.AdmissionControllerManagementI#linkComputer(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
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
        cssdop.doDisconnection();
        String computerController[] = new String[4];
        computerController[0] = ComputerControllerManagementUri + cmops.size();						
		computerController[1] = ComputerServicesInboundPortURI;
		computerController[2] = ComputerControllerManagementInboundPortURI + cmops.size();
		computerController[3] = ComputerStaticStateDataInboundPortURI;
		
		
        ComputerController tmp = new ComputerController(computerController[0], computerController[1], computerController[2], computerController[3]);
        tmp.start();
        ComputerControllerManagementOutboutPort ccmop = new ComputerControllerManagementOutboutPort("ComputerControllerManagementOutboutPort" + cmops.size(), this);
		
        this.addPort(ccmop);
		ccmop.publishPort();
		ccmop.doConnection(
				computerController[2],
				ComputerControllerConnector.class.getCanonicalName());
		this.cmops.add(ccmop);
			
			for(int i = 0; i < nbProc; i++) {
				//this.processorController.bindProcessor(processorsURIs.get(i), "ACHANGER", pmipURIs.get(i), pssdURIs.get(i), pdsdURIs.get(i));
				createVM(computerController[2], ccmop.allocateCores(nbCores/2));
			}
		
	}
	
	private String createVM(String computerManagementInboundPortURI, AllocatedCore[] allocatedCore) throws Exception {
		
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
		ApplicationVMInfo vm = new ApplicationVMInfo(applicationVM[0], applicationVM[1], applicationVM[2], computerManagementInboundPortURI);
		synchronized (o) {
			if(this.VMforNewApplication.size() < MIN_VM) {
				this.VMforNewApplication.add(vm);
			}else {
				this.freeApplicationVM.add(vm);
			}
		}
		
		return applicationVM[0];
	}

	/**
	 * @see fr.upmc.PriseTheSun.datacenter.software.ring.interfaces.RingNetworkStateDataConsumerI#acceptRingNetworkDynamicData(java.lang.String, fr.upmc.PriseTheSun.datacenter.software.ring.interfaces.RingNetworkDynamicStateI)
	 */
	@Override
	public void acceptRingNetworkDynamicData(String requestDispatcherURI, RingNetworkDynamicStateI currentDynamicState)
			throws Exception {
		synchronized(o){
			ApplicationVMInfo vm = currentDynamicState.getApplicationVMInfo();
			if(vm != null) {
				if(this.vmURis.contains(vm.getApplicationVM())) {
					int numberVmInRing = this.vmURis.size();
					w.write(Arrays.asList("Nombre de vm " + numberVmInRing));
				//	System.err.println("Nombre de vm " + numberVmInRing);
					this.vmURis.clear();
				}else {
					this.vmURis.add(vm.getApplicationVM());
				}
				if(this.VMforNewApplication.size() < MIN_VM) {
					this.VMforNewApplication.add(currentDynamicState.getApplicationVMInfo());
				}else {
					freeApplicationVM.add(currentDynamicState.getApplicationVMInfo());
				}
			}
		}

	}

	public void	sendDynamicState() throws Exception
	{
		if (this.rdsdip.connected()) {
			RingNetworkDynamicStateI rds = this.getDynamicState() ;
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
		ApplicationVMInfo removed = null;
		synchronized(o){
			while(!freeApplicationVM.isEmpty() && this.VMforNewApplication.size() < 5) {
				VMforNewApplication.add(freeApplicationVM.remove(0));
			}
			if(!this.freeApplicationVM.isEmpty()) {
				removed = this.freeApplicationVM.remove(0);
			}
		}
		return new RingDynamicState(removed);
	}

	/**
	 * @see fr.upmc.datacenter.interfaces.PushModeControllingI#startUnlimitedPushing(int)
	 */
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

	/**
	 * @see fr.upmc.datacenter.interfaces.PushModeControllingI#startLimitedPushing(int, int)
	 */
	@Override
	public void startLimitedPushing(final int interval, final int n) throws Exception {
		assert	n > 0 ;
		this.logMessage(this.admissionControllerURI + " startLimitedPushing with interval "
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

	/**
	 * @see fr.upmc.datacenter.interfaces.PushModeControllingI#stopPushing()
	 */
	@Override
	public void stopPushing() throws Exception {
		if (this.pushingFuture != null &&
				!(this.pushingFuture.isCancelled() ||
						this.pushingFuture.isDone())) {
			this.pushingFuture.cancel(false) ;
		}
}

	@Override
	public void stopApplication(String appUri) throws Exception {
		RequestDispatcherManagementOutboundPort rdmop = this.rdmopMap.get(appUri);
		rdmop.disconnectRequestGenerator();
		rdmop.disconnectController();
	}

	@Override
	public void bindSendingDataUri(String DataInboundPortUri) throws Exception {
		if(rdsdop.connected())
			rdsdop.doDisconnection();
		rdsdop.doConnection(DataInboundPortUri, ControlledDataConnector.class.getCanonicalName());
	}
	

	@Override
	public void setNextManagementInboundPort(String managementInboundPort) throws Exception {
		System.out.println("je rentre");
		this.controllerManagementNextInboundPort = managementInboundPort;
	}

	@Override
	public void setPreviousManagementInboundPort(String managementInboundPort) throws Exception {
		System.out.println("imin");
		this.controllerManagementPreviousInboundPort = managementInboundPort;
	}

	@Override
	public void startPushing() throws Exception {
		this.startUnlimitedPushing(RingDynamicState.RING_INTERVAL_TIME);
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

}
