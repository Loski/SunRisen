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

import com.sun.swing.internal.plaf.synth.resources.synth;

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
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces.RequestDispatcherManagementI;
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
 * La classe <code>AdmissionControllerDynamic</code> implemente un composant qui représente un 
 *
 * <p><strong>Description</strong></p>
 * 
 * Le but de <code>AdmissionControllerDynamic</code> est d'accepter ou de refuser
 * des applications selon son pool de <code>ApplicationVirtuelMachine</code>.
 * Ensuite, un <code>RequestDispatcher</code> ainsi qu'un <code>Controller</code> sont créés pour pouvoir résoudre les requêtes.
 * Le Controller est ensuite ajouté au data ring où des VMs cirule librement entre chaque controllers et l'AdmissionController.
 * <code>AdmissionControllerDynamic</code> reçoie un  <code>RequestGenerator</code> via <code>ApplicationSubmissionI</code>.
 * De plus, il implémente l'interface <code>NodeRingManagementI</code> pour implémenter le comportement d'un node d'un data ring network.
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
	
	@SuppressWarnings("unused")
	private String previousControllerManagement;
	@SuppressWarnings("unused")
	private String controllerManagementNextInboundPort;
	@SuppressWarnings("unused")
	private String controllerManagementPreviousInboundPort;
	
	
	private static int nbappli = 0;
	protected ArrayList<ComputerStaticStateDataOutboundPort> cssdops;
	protected ArrayList<ComputerDynamicStateDataOutboundPort> cdsdops;
	protected Map<String, ComputerControllerManagementOutboutPort > cmops;

	protected ScheduledFuture<?>	pushingFuture ;
	
	/** Set pour check le nombre de vm dans le ring */
	protected HashSet<String> vmURis;
	
	// Map between RequestDispatcher URIs and the management ports to call them.
	protected Map<String, RequestDispatcherManagementOutboundPort> rdmopMap;
	
	//Map Between a vm and his computer
	protected List<ApplicationVMInfo> freeApplicationVM;


	
	Object lockController = new Object();
	
	/**nombre de coeurs alloués par défaut à une VM */
	public static final int NB_CORES = 5;
	
	/**Min vm dans l'arrayLMist pour accepter des applications **/
	private static final int MIN_VM_FOR_SUB_APPLICATION = 15;
	
	
	/** Nombre minimum de VM dans le ring.. */
	private static final int NUMBER_MIN_VM = 10;
	
	/** Nombre maximum de VM dans le ring */
	private static final int NUMBER_MAX_VM = 20;
	
	/**Nombre de VM a ajouté lors des links d'ordinateur */
	private int initialisation  = MIN_VM_FOR_SUB_APPLICATION + NUMBER_MIN_VM;

	/**Nombre de VM à détruire dans le network ring */
	private int askToBeDestroy;

	private DynamicComponentCreationOutboundPort portTControllerJVM;
	
	/*Port du la structure en anneau pour relier chaque controller*/ 
	private RingNetworkDynamicStateDataOutboundPort rdsdop;
	private RingNetworkDynamicStateDataInboundPort rdsdip;
	private String AdmissionControllerDataRingOutboundUri;
	private String AdmissionControllerDataRingInboundUri;
	private NodeManagementInboundPort nmip;
	private String nextControllerDataRingUri;

	private ArrayList<ApplicationVMInfo> VMforNewApplication;

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
		this.addRequiredInterface(RequestDispatcherManagementI.class);
		
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
		
		rdsdop = new RingNetworkDynamicStateDataOutboundPort(this.AdmissionControllerDataRingOutboundUri,this,this.admissionControllerURI);
		this.addPort(rdsdop);
		this.rdsdop.publishPort();

		rdsdip=new RingNetworkDynamicStateDataInboundPort(this.AdmissionControllerDataRingInboundUri, this);
		this.addPort(rdsdip) ;
		this.rdsdip.publishPort();
		
		ADMNodeControllerManagementInboundPort = admissionControllerURI + NODE_MANAGEMENT;
		nmip = new NodeManagementInboundPort(ADMNodeControllerManagementInboundPort, this);
		this.addPort(nmip);
		this.nmip.publishPort();
		
		this.rdmopMap = new HashMap<String, RequestDispatcherManagementOutboundPort>();
		this.cssdops = new ArrayList<ComputerStaticStateDataOutboundPort>();
		this.cdsdops = new ArrayList<ComputerDynamicStateDataOutboundPort>();
		this.cmops = new HashMap<String, ComputerControllerManagementOutboutPort>();
		this.freeApplicationVM = new ArrayList<>();
		this.VMforNewApplication = new ArrayList<>();
		this.vmURis = new HashSet<>();
		this.submissionInterfaces = new HashMap<>();
		askToBeDestroy = 0;
		w = new Writter(this.admissionControllerURI+ ".csv");
		w.write(Arrays.asList("Nombre de VMs", "A détruire", "A ajouter"));
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

			for(Entry<String, ComputerControllerManagementOutboutPort> entry : this.cmops.entrySet()) {
				if(entry.getValue().connected()) {
					entry.getValue().doDisconnection();
				}
			}
			for(ComputerDynamicStateDataOutboundPort cdsdop : cdsdops) {
				if (cdsdop.connected()) {
					cdsdop.doDisconnection();
				}
			}
			
			for(ComputerStaticStateDataOutboundPort cssdop : cssdops) {
				if (cssdop.connected()) {
					cssdop.doDisconnection();
				}
			}

			for(Entry<String, RequestDispatcherManagementOutboundPort> entry : this.rdmopMap.entrySet()) {
				if(entry.getValue().connected()) {
					entry.getValue().doDisconnection();
				}
			}
			if(this.portTControllerJVM.connected()) {
				this.portTControllerJVM.doDisconnection();
			}
			if (this.rdsdop.connected()) {
				this.rdsdop.doDisconnection();
			}
			if(rdsdip.connected()) {
				rdsdip.doDisconnection();
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
	 * <li>controllerURIs[0] = Controller URI</li>
	 * <li>controllerURIs[1] = Controller URI management</li>
	 * <li>controllerURIs[2] = Dispatcher URI out dynamic data</li>
	 * <li>controllerURIs[3] = Controller URI ring out</li>
	 * <li>controllerURIs[4] = Controller URI ring in</li>
	 * <li>controllerURIs[5] = next controller URI ring in</li>
	 * <li>controllerURIs[6] = VMDisconnectionHandler Inbound Port </li>
	 * <li>controllerURIs[7] = ADMNodeControllerManagementInboundPort </li>
	 * <li>controllerURIs[8] = précédent NodeControllerManagement </li>
	 * </ul>
	 * @throws Exception
	 */
	private  String[] createController(String appURI, String requestDispatcherDynamicStateDataInboundPortURI, String VMDisconnectionHandlerOutboundPortURI, String rdURI, ApplicationVMInfo vm) throws Exception
	{
		String controllerURIs[] = new String[9];
		controllerURIs[0] = appURI + "-controller";
		controllerURIs[1] = appURI +"-controllermngt";
		controllerURIs[2] = controllerURIs[0]+"-rddsdop";
		controllerURIs[3] = controllerURIs[0]+"-"+ControllerDataRingOutboundPortURI;
		controllerURIs[4] = controllerURIs[0]+"-"+ ControllerDataRingInboundPortURI;
		//next dataring inbound uri
		controllerURIs[5] = null;
		controllerURIs[6] = controllerURIs[0]+"-VMDisconnectionHandlerIp";
		controllerURIs[7] = ADMNodeControllerManagementInboundPort;
		controllerURIs[8] = null;
		nbappli++;

		
		boolean first = false;
		
		synchronized (lockController) {
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
	private String[] createDispatcher(String appURI, String className)  throws Exception{
		
		assert className != null;
		assert appURI != null && !appURI.isEmpty();
		
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
		
		try {
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
			
	
			RequestDispatcherManagementOutboundPort rdmop = new RequestDispatcherManagementOutboundPort(dispatcherURI[0]+
					RequestDispatcherManagementOutboundPortURI,
					this);
			
		    this.addPort(rdmop);
			rdmop.publishPort();
			rdmop.doConnection(dispatcherURI[1], RequestDispatcherManagementConnector.class.getCanonicalName());
			rdmopMap.put(appURI, rdmop);
		} catch (Exception e) {
			e.printStackTrace();
		}	
		return dispatcherURI;
	}
	
	/**
	 * @see fr.upmc.PriseTheSun.datacenterclient.software.applicationprovider.interfaces.ApplicationSubmissionI#submitApplication(java.lang.String, int)
	 */
	@Override
	public  String[] submitApplication(String appURI, int nbVM) throws Exception{
		
		this.logMessage("New Application received in dynamic controller ("+appURI+")"+".\n Waiting for evaluation ");
		w.write(Arrays.asList("application " + appURI +" accepted"));

		ApplicationVMInfo vm = null;
		boolean failedToCreated = true;
		
		// 3 retry
		for(int i = 0; i < 3 && failedToCreated; i++) {
			synchronized(VMforNewApplication){
				if(!this.VMforNewApplication.isEmpty()) {
					vm = VMforNewApplication.remove(0);
					failedToCreated = false;
				}
			}
			if(failedToCreated) {
				System.err.println("waiting for " + appURI);
				Thread.sleep(200);
			}
		}
		
		if(failedToCreated) {
			
			return null;
		}

		String dispatcherUri[] = createDispatcher(appURI, RequestDispatcher.class.getCanonicalName());
		String controllerUris[] = createController(appURI,dispatcherUri[6],dispatcherUri[8],dispatcherUri[0], vm);
		this.rdmopMap.get(appURI).connectController(controllerUris[0],controllerUris[6]);
		return dispatcherUri;
	}
	
	/**
	 * @see fr.upmc.PriseTheSun.datacenterclient.software.applicationprovider.interfaces.ApplicationSubmissionI#submitApplication(java.lang.String, int, java.lang.Class)
	 */
	@Deprecated
	public  synchronized String[]  submitApplication(String appURI, int nbVM, Class submissionInterface) throws Exception {
		
		assert submissionInterface.isInterface();
		
		this.logMessage("New Application received in dynamic controller ("+appURI+")"+".\n Waiting for evaluation ");
		ApplicationVMInfo vm;
		
		synchronized(VMforNewApplication){
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
	

	/**
	 * @see fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.interfaces.AdmissionControllerManagementI#linkComputer(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void linkComputer(String computerURI, String ComputerServicesInboundPortURI,
		String ComputerStaticStateDataInboundPortURI, String ComputerDynamicStateDataInboundPortURI)
		throws Exception {
	


	/*	ArrayList<String> processorsURIs = new ArrayList<String>();
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
        }*/
        String computerController[] = new String[4];
        computerController[0] = ComputerControllerManagementUri + cmops.size();						
		computerController[1] = ComputerServicesInboundPortURI;
		computerController[2] = ComputerControllerManagementInboundPortURI + cmops.size();
		computerController[3] = ComputerStaticStateDataInboundPortURI;
		
		
        ComputerController tmp = new ComputerController(computerController[0], computerController[1], computerController[2], computerController[3]);
        tmp.start();
        String ccmopUri = "ComputerControllerManagementOutboutPort" + cmops.size();
        ComputerControllerManagementOutboutPort ccmop = new ComputerControllerManagementOutboutPort(ccmopUri, this);
		
        this.addPort(ccmop);
		ccmop.publishPort();
		ccmop.doConnection(
				computerController[2],
				ComputerControllerConnector.class.getCanonicalName());
		this.cmops.put(ccmopUri, ccmop);
			
		if(initialisation > 0) {
			initialisation--;
			createVM(ccmopUri);
		}
	}
	
	/**
	 * Créé une AVM dynamiquement et l'enregistre dans la CVM.
	 * On lui réserve ensuite 5 coeurs a allouée dans le futur. 
	 * @param ccmopUri URI du controller de l'ordinateur.
	 * @return
	 * @throws Exception Lance une exception si l'ordinateur n'a pas pu réserver des coeurs pour la VM.
	 */
	private String createVM(String ccmopUri) throws Exception {
		
		String applicationVM[] = new String[5];
		ComputerControllerManagementOutboutPort cmop = this.cmops.get(ccmopUri);
		String computerManagementInboundPortURI;
		synchronized (cmop) {
			int nbVM = cmop.compteurVM();
			
			// --------------------------------------------------------------------
			// Create an Application VM component
			// --------------------------------------------------------------------
			applicationVM[0] = ccmopUri + "avm-"+nbVM;
			applicationVM[1] = ccmopUri + "avmibp-"+nbVM;
			applicationVM[2] = ccmopUri +"rsibpVM-"+nbVM;
			applicationVM[3] = ccmopUri +"rnobpVM-"+nbVM;
			applicationVM[4] = ccmopUri + "avmobp-"+nbVM;
			
			int core = this.cmops.get(ccmopUri).tryReserveCore(applicationVM[0], 5, 0);
			if(core == 0) {
				throw new Exception("Computer can't reserve a new VM");
			}
			
			this.portTControllerJVM.createComponent(
					ApplicationVM.class.getCanonicalName(),
					new Object[] {
							applicationVM[0],							
							applicationVM[1],
							applicationVM[2],
							applicationVM[3]
			});
				

			//this.avmOutPort.put(applicationVM[0], avmPort);
			computerManagementInboundPortURI =  cmop.getClientPortURI();
		}
		
		ApplicationVMInfo vm = new ApplicationVMInfo(applicationVM[0], applicationVM[1], applicationVM[2], computerManagementInboundPortURI);

		synchronized (VMforNewApplication) {
			if(this.VMforNewApplication.size() < MIN_VM_FOR_SUB_APPLICATION) {
				this.VMforNewApplication.add(vm);
			}else {
				synchronized (freeApplicationVM) {
					this.freeApplicationVM.add(vm);
				}
			}
		}
		return applicationVM[0];
	}

	/**
	 * @see fr.upmc.PriseTheSun.datacenter.software.ring.interfaces.RingNetworkStateDataConsumerI#acceptRingNetworkDynamicData(java.lang.String, fr.upmc.PriseTheSun.datacenter.software.ring.interfaces.RingNetworkDynamicStateI)
	 */
	@Override
	public void acceptRingNetworkDynamicData(String controllerDataRingOutboundPortURI, RingNetworkDynamicStateI currentDynamicState)
			throws Exception {
			ApplicationVMInfo vm = currentDynamicState.getApplicationVMInfo();
			if(vm != null) {
				if(askToBeDestroy > 0) {
					askToBeDestroy--;
					this.cmops.get(vm.getComputerManagementInboundPortURI()).releaseCore(vm.getApplicationVM());
				}else {
				synchronized (vmURis) {
					if(this.vmURis.contains(vm.getApplicationVM())) {
						int tocreate = 0;
						int numberVmInRing = this.vmURis.size();
						if(numberVmInRing < NUMBER_MIN_VM) {
							tocreate = Integer.min(NUMBER_MIN_VM - numberVmInRing + 4, NUMBER_MAX_VM);
							addFreeVM(tocreate);
						}else if(numberVmInRing > NUMBER_MAX_VM){
							askToBeDestroy = numberVmInRing - NUMBER_MAX_VM;
						}
						w.write(Arrays.asList(""+numberVmInRing, ""+askToBeDestroy, ""+tocreate));

						this.vmURis.clear();
					}else {				
						this.vmURis.add(vm.getApplicationVM());
					}
				}
				if(this.VMforNewApplication.size() < MIN_VM_FOR_SUB_APPLICATION) {
					synchronized (VMforNewApplication) {
						this.VMforNewApplication.add(currentDynamicState.getApplicationVMInfo());
					}
				}else {
					synchronized(freeApplicationVM){
						freeApplicationVM.add(currentDynamicState.getApplicationVMInfo());
					}
				}
			}
		}
	}
	
	private void addFreeVM(int nbVmToCreate) throws Exception {
		
		//TODO Changer structure computer => Tri selon leur nombre de vm / coeurs libres
		for(Entry<String, ComputerControllerManagementOutboutPort> entry : this.cmops.entrySet()) {
			if(entry.getValue().compteurVM() < 2 && nbVmToCreate > 0) {
				try {
					createVM(entry.getValue().getPortURI());
					nbVmToCreate--;
				}catch (Exception e) {
					this.logMessage("Impossible de créer une VM sur cet ordinateur");
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
		synchronized(VMforNewApplication){
			synchronized (freeApplicationVM) {
				while(!freeApplicationVM.isEmpty() && this.VMforNewApplication.size() < 5) {
					VMforNewApplication.add(freeApplicationVM.remove(0));
				}
				if(!this.freeApplicationVM.isEmpty()) {
					removed = this.freeApplicationVM.remove(0);
				}
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
		try {
			RequestDispatcherManagementOutboundPort rdmop = this.rdmopMap.get(appUri);
			rdmop.disconnectRequestGenerator();
			rdmop.disconnectController();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void bindSendingDataUri(String DataInboundPortUri) throws Exception {
		if(rdsdop.connected())
			rdsdop.doDisconnection();
		rdsdop.doConnection(DataInboundPortUri, ControlledDataConnector.class.getCanonicalName());
	}
	

	@Override
	public void setNextManagementInboundPort(String managementInboundPort) throws Exception {
		this.controllerManagementNextInboundPort = managementInboundPort;
	}

	@Override
	public void setPreviousManagementInboundPort(String managementInboundPort) throws Exception {
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

	}

	@Override
	public void doDisconnectionInboundPort() throws Exception {
		if(rdsdip.connected()) {
			rdsdip.doDisconnection();
		}
	}

}
