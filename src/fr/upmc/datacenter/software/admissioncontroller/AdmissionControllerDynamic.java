package fr.upmc.datacenter.software.admissioncontroller;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.AbstractExecutorService;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.components.cvm.pre.dcc.connectors.DynamicComponentCreationConnector;
import fr.upmc.components.cvm.pre.dcc.ports.DynamicComponentCreationOutboundPort;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.components.exceptions.ComponentStartException;
import fr.upmc.components.interfaces.DataRequiredI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.components.ports.PortI;
import fr.upmc.components.pre.reflection.connectors.ReflectionConnector;
import fr.upmc.components.pre.reflection.ports.ReflectionOutboundPort;
import fr.upmc.datacenter.software.admissioncontroller.interfaces.AdmissionControllerManagementI;
import fr.upmc.datacenter.software.admissioncontroller.ports.AdmissionControllerManagementInboundPort;
import fr.upmc.datacenter.software.applicationvm.ApplicationVM;
import fr.upmc.datacenter.software.applicationvm.connectors.ApplicationVMManagementConnector;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.datacenter.software.connectors.RequestNotificationConnector;
import fr.upmc.datacenter.software.connectors.RequestSubmissionConnector;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionI;
import fr.upmc.datacenter.software.ports.RequestSubmissionInboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort;
import fr.upmc.datacenter.software.requestdispatcher.RequestDispatcher;
import fr.upmc.datacenter.software.requestdispatcher.connectors.RequestDispatcherManagementConnector;
import fr.upmc.datacenter.software.requestdispatcher.ports.RequestDispatcherManagementOutboundPort;
import fr.upmc.datacenterclient.applicationprovider.interfaces.ApplicationSubmissionI;
import fr.upmc.datacenterclient.applicationprovider.ports.ApplicationSubmissionInboundPort;
import fr.upmc.datacenterclient.requestgenerator.connectors.RequestGeneratorManagementConnector;
import fr.upmc.datacenterclient.requestgenerator.ports.RequestGeneratorManagementOutboundPort;
import fr.upmc.javassist.ConnectorCreator;
import fr.upmc.javassist.RequestDispatcherCreator;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;
import fr.upmc.datacenter.hardware.computers.connectors.ComputerServicesConnector;
import fr.upmc.datacenter.hardware.computers.interfaces.ComputerServicesI;
import fr.upmc.datacenter.hardware.computers.interfaces.ComputerStaticStateDataI;
import fr.upmc.datacenter.hardware.computers.ports.ComputerDynamicStateDataOutboundPort;
import fr.upmc.datacenter.hardware.computers.ports.ComputerServicesOutboundPort;
import fr.upmc.datacenter.hardware.computers.ports.ComputerStaticStateDataOutboundPort;
import fr.upmc.datacenter.interfaces.ControlledDataRequiredI;

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
public class AdmissionControllerDynamic extends AbstractComponent implements ApplicationSubmissionI{

	public static int	DEBUG_LEVEL = 1 ;
	protected String acURI;


	protected static final String RequestDispatcherManagementInboundPortURI = "rdmi";
	protected static final String RequestNotificationInboundPortURI = "rnip";
	protected static final String RequestSubmissionInboundPortURI = "rsip";
	protected static final String RequestNotificationOutboundPortURI = "rnop";
	protected static final String RequestDispatcherManagementOutboundPortURI = "rdmop";
	protected static final String RequestSubmissionOutboundPortURI = "rsop"; 
	protected static final int NB_CORES = 2;

	protected AdmissionControllerManagementInboundPort acmip;
	protected ApplicationSubmissionInboundPort asip;
	
	
	protected List<ApplicationVMManagementOutboundPort> avmOutPort;

	
	protected ComputerServicesOutboundPort csPort;
	protected ComputerStaticStateDataOutboundPort cssdop;
	protected ComputerDynamicStateDataOutboundPort cdsdop;
	
	

	 // Map between RequestDispatcher URIs and the outbound ports to call them.
	protected Map<String, RequestDispatcherManagementOutboundPort> rdmopMap;
	


	
	protected ComputerServicesOutboundPort[] csopTab;
	
	private DynamicComponentCreationOutboundPort portToRequestDispatcherJVM;
	private DynamicComponentCreationOutboundPort portToApplicationVMJVM;
	protected LinkedHashMap<Class,Class> interface_dispatcher_map;
	private int[] nbAvailablesCores;


	/*protected static final String RequestDispatcher_JVM_URI = "controller" ;
	protected static final String Application_VM_JVM_URI = "controller";*/
	
	public AdmissionControllerDynamic(String acURI, String applicationSubmissionInboundPortURI,
			String AdmissionControllerManagementInboundPortURI, String[] computerServiceOutboundPortURI,
			String[] ComputerServicesInboundPortURI, int nbAvailableCores,
			String computerStaticStateDataOutboundPortURI,
			String RequestDispatcher_JVM_URI,
			String Application_VM_JVM_URI
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

		this.avmOutPort = new LinkedList<ApplicationVMManagementOutboundPort>();
		this.rdmopMap = new HashMap<String, RequestDispatcherManagementOutboundPort>();
		this.interface_dispatcher_map = new LinkedHashMap<>();
		
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
		
		this.csopTab = new ComputerServicesOutboundPort[computerServiceOutboundPortURI.length];

		this.addRequiredInterface(ComputerServicesI.class);
		
		for (int i = 0; i < computerServiceOutboundPortURI.length; i++) {
			this.csopTab[i] = new ComputerServicesOutboundPort(computerServiceOutboundPortURI[i], this);
			this.addPort(this.csopTab[i]);
			this.csopTab[i].publishPort();
			
			this.csopTab[i].doConnection(
					ComputerServicesInboundPortURI[i],
					ComputerServicesConnector.class.getCanonicalName());
		
		}
	}

	@Override
	public void start() throws ComponentStartException {
		super.start();
	}

	@Override
	public synchronized String[] submitApplication(String appURI, int nbVM) throws Exception {
		
		this.logMessage("New Application received in dynamic controller ("+appURI+")"+".\n Waiting for evaluation ");
		
		AllocatedCore[] allocatedCore = csopTab[0].allocateCores(NB_CORES);
		String dispatcherURI[] = new String[6];

		if(allocatedCore!=null && allocatedCore.length != 0) {
			System.out.println("Application accepted..");
			dispatcherURI[0] = "RD" + rdmopMap.size()+"-"+appURI;
			dispatcherURI[1] = RequestDispatcherManagementInboundPortURI + "_" + appURI;
			dispatcherURI[2] = RequestSubmissionInboundPortURI +"_" + appURI;
			dispatcherURI[3] = RequestNotificationOutboundPortURI + "_"+ appURI;
			dispatcherURI[4] = RequestNotificationInboundPortURI + "_"+ appURI;
			dispatcherURI[5] = RequestSubmissionOutboundPortURI + "_"+ appURI;

			
			this.portToRequestDispatcherJVM.createComponent(
					RequestDispatcher.class.getCanonicalName(),
					new Object[] {
							dispatcherURI[0],							
							dispatcherURI[1],
							dispatcherURI[2],
							dispatcherURI[3],
							dispatcherURI[4]
					});		
		
			
			RequestDispatcherManagementOutboundPort rdmop = new RequestDispatcherManagementOutboundPort(
					RequestDispatcherManagementOutboundPortURI + rdmopMap.size(),
					this);
			
			rdmop.publishPort();
			rdmop.doConnection(dispatcherURI[1], RequestDispatcherManagementConnector.class.getCanonicalName());
			
			ReflectionOutboundPort rop = new ReflectionOutboundPort(this);
			this.addPort(rop);
			rop.publishPort();
			
			
			
			String applicationVM[] = new String[5];
			rop.doConnection(dispatcherURI[0], ReflectionConnector.class.getCanonicalName());
			rop.toggleLogging();
			rop.toggleTracing();
			
			rop.doDisconnection();
			
			
			
			for(int i=0;i<nbVM;i++)
			{
				// --------------------------------------------------------------------
				// Create an Application VM component
				// --------------------------------------------------------------------
				applicationVM[0] = "vm-"+this.avmOutPort.size();
				applicationVM[1] = "avmibp-"+this.avmOutPort.size();
				applicationVM[2] = "rsibp-VM-"+this.avmOutPort.size();
				applicationVM[3] = "rnobp-VM-"+this.avmOutPort.size();
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
				rdmop.connectVirtualMachine(applicationVM[0], applicationVM[2], dispatcherURI[5]+"-"+i);
				avmPort.connectWithRequestSubmissioner(dispatcherURI[0], dispatcherURI[4]);		
				rop.doConnection(applicationVM[0], ReflectionConnector.class.getCanonicalName());
				
				rop.toggleTracing();
				rop.toggleLogging();

				rop.doDisconnection();
			}
			
			rdmopMap.put(appURI, rdmop);
			
			return dispatcherURI;
			
	}else {
		this.logMessage("Failed to allocates core for a new application.");
		return null;
	}
	}

	@Override
	public void submitGenerator(String RequestNotificationInboundPort, String appUri, String rgURI) throws Exception {
		this.rdmopMap.get(appUri).connectWithRequestGenerator(rgURI, RequestNotificationInboundPort);
	}

	@Override
	public void shutdown() throws ComponentShutdownException {
		try {			
			for(ComputerServicesOutboundPort csop : csopTab) {
				if (csop.connected()) {
					csop.doDisconnection();
				}
			}
			if (this.cssdop.connected()) {
				this.cssdop.doDisconnection();
			}
			if (this.cdsdop.connected()) {
				this.cdsdop.doDisconnection();
			}
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
	
	
	@Override
	public synchronized String[] submitApplication(String appURI, int nbVM,Class submissionInterface) throws Exception {
		
		assert submissionInterface.isInterface();
		
		this.logMessage("New Application received in dynamic controller ("+appURI+")"+".\n Waiting for evaluation ");
		
		AllocatedCore[] allocatedCore = csopTab[0].allocateCores(NB_CORES);
		String dispatcherURI[] = new String[6];
		System.out.println(allocatedCore);
		if(allocatedCore!=null && allocatedCore.length != 0) {
			
			dispatcherURI[0] = "RD_" + rdmopMap.size()+"_"+appURI;
			dispatcherURI[1] = RequestDispatcherManagementInboundPortURI + "_" + appURI;
			dispatcherURI[2] = RequestSubmissionInboundPortURI +"_" + appURI;
			dispatcherURI[3] = RequestNotificationOutboundPortURI + "_"+ appURI;
			dispatcherURI[4] = RequestNotificationInboundPortURI + "_"+ appURI;
			dispatcherURI[5] = RequestSubmissionOutboundPortURI + "_"+ appURI;

			Class dispa = RequestDispatcherCreator.createRequestDispatcher("JAVASSIST-dispa",RequestDispatcher.class, submissionInterface);
			interface_dispatcher_map.put(submissionInterface, dispa);
			
			this.portToRequestDispatcherJVM.createComponent(
					dispa.getCanonicalName(),
					new Object[] {
							dispatcherURI[0],							
							dispatcherURI[1],
							dispatcherURI[2],
							dispatcherURI[3],
							dispatcherURI[4]
					});		
		
			
			RequestDispatcherManagementOutboundPort rdmop = new RequestDispatcherManagementOutboundPort(
					RequestDispatcherManagementOutboundPortURI + rdmopMap.size(),
					this);
			
			rdmop.publishPort();
			rdmop.doConnection(dispatcherURI[1], RequestDispatcherManagementConnector.class.getCanonicalName());
			
			ReflectionOutboundPort rop = new ReflectionOutboundPort(this);
			this.addPort(rop);
			rop.publishPort();
			
			
			
			String applicationVM[] = new String[5];
			
			rop.doConnection(dispatcherURI[0], ReflectionConnector.class.getCanonicalName());
			rop.toggleLogging();
			rop.toggleTracing();
			rop.doDisconnection();
			for(int i=0; i<nbVM; i++)
			{
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

				rdmop.connectVirtualMachine(applicationVM[0], applicationVM[2], dispatcherURI[5]+"-"+i);
				avmPort.connectWithRequestSubmissioner(dispatcherURI[0], dispatcherURI[4]);		

				rop.doConnection(applicationVM[0], ReflectionConnector.class.getCanonicalName());
				
				rop.toggleTracing();
				rop.toggleLogging();

				rop.doDisconnection();
			}
			
			rdmopMap.put(appURI, rdmop);
			
			return dispatcherURI;	
		}else {
			this.logMessage("Failed to allocates core for a new application.");
			return null;
		}
	}
	
	/**
	 * Return the index of the available core
	 * @param nbCores
	 * @return index 
	 */
	private Integer getAvailableCores(int nbCores) {
		int max = 0;
		Integer index = -1;
		for (int i = 0; i < nbAvailablesCores.length; i++) {
			if (nbAvailablesCores[i] == nbCores) {
				return i;
			}
			/*if (nbAvailablesCores[i] > max) {
				max = nbAvailablesCores[i];
				index = i;
			}*/
		}
		return index;
}


}
