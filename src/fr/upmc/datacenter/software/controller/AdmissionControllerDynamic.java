package fr.upmc.datacenter.software.controller;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.components.cvm.pre.dcc.connectors.DynamicComponentCreationConnector;
import fr.upmc.components.cvm.pre.dcc.ports.DynamicComponentCreationOutboundPort;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.components.exceptions.ComponentStartException;
import fr.upmc.components.interfaces.DataRequiredI;
import fr.upmc.components.ports.PortI;
import fr.upmc.components.pre.reflection.ports.ReflectionOutboundPort;
import fr.upmc.datacenter.software.applicationvm.ApplicationVM;
import fr.upmc.datacenter.software.applicationvm.connectors.ApplicationVMManagementConnector;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.datacenter.software.connectors.RequestSubmissionConnector;
import fr.upmc.datacenter.software.controller.interfaces.AdmissionControllerManagementI;
import fr.upmc.datacenter.software.controller.ports.AdmissionControllerManagementInboundPort;
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
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;
import fr.upmc.datacenter.hardware.computers.connectors.ComputerServicesConnector;
import fr.upmc.datacenter.hardware.computers.interfaces.ComputerServicesI;
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
 * @author	Maxime LAVASTE Loï¿½c LAFONTAINE
 */
public class AdmissionControllerDynamic extends AdmissionController implements ApplicationSubmissionI, AdmissionControllerManagementI{

	private DynamicComponentCreationOutboundPort portToRequestDispatcherJVM;
	private DynamicComponentCreationOutboundPort portToApplicationVMJVM;

	protected static final String RequestDispatcher_JVM_URI = "" ;
	protected static final String Application_VM_JVM_URI = "";
	
	
	public AdmissionControllerDynamic(String acURI, String applicationSubmissionInboundPortURI,
			String AdmissionControllerManagementInboundPortURI, String computerServiceOutboundPortURI,
			String ComputerServicesInboundPortURI, String computerURI, int nbAvailableCores,
			String computerStaticStateDataOutboundPortURI) throws Exception {
		super(2, 2);
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
 
		this.csop = new ComputerServicesOutboundPort(computerServiceOutboundPortURI, this);
		this.addPort(csop);
		this.csop.localPublishPort();
		
		this.csop.doConnection(
				ComputerServicesInboundPortURI,
				ComputerServicesConnector.class.getCanonicalName()) ;
		
		
		this.computerURI = computerURI;
		
		this.cssdop = new ComputerStaticStateDataOutboundPort(computerStaticStateDataOutboundPortURI, this, computerURI);
		this.addPort(this.cssdop);
		this.cssdop.publishPort();
		
		ReflectionOutboundPort a;
		this.avmOutPort = new LinkedList<ApplicationVMManagementOutboundPort>();
		

		// this.addOfferedInterface(ComputerStaticStateDataI.class);
		// or :
	/*	this.addOfferedInterface(DataRequiredI.PushI.class);
		this.addRequiredInterface(DataRequiredI.PullI.class);
		
		*
		*		this.addRequiredInterface(ControlledDataRequiredI.ControlledPullI.class);
		this.cdsdop = new ComputerDynamicStateDataOutboundPort(computerDynamicStateDataOutboundPortURI, this, computerURI);
		this.addPort(this.cdsdop);
		this.cdsdop.publishPort();
		*/
		
		this.rdmopList = new HashMap<String, RequestDispatcherManagementOutboundPort>();
		
		//Pour l'allocation de core.
		this.addRequiredInterface(ComputerServicesI.class);
		
		this.interface_dispatcher_map = new LinkedHashMap<>();
		this.portToApplicationVMJVM = new DynamicComponentCreationOutboundPort(this);
		this.portToApplicationVMJVM.localPublishPort();
		this.addPort(this.portToApplicationVMJVM);
		
		this.portToApplicationVMJVM.doConnection(					
				this.Application_VM_JVM_URI + AbstractCVM.DCC_INBOUNDPORT_URI_SUFFIX,
				DynamicComponentCreationConnector.class.getCanonicalName());
		
		this.portToRequestDispatcherJVM = new DynamicComponentCreationOutboundPort(this);
		this.portToRequestDispatcherJVM.localPublishPort();
		this.addPort(this.portToRequestDispatcherJVM);
		
		this.portToRequestDispatcherJVM.doConnection(					
				this.RequestDispatcher_JVM_URI + AbstractCVM.DCC_INBOUNDPORT_URI_SUFFIX,
				DynamicComponentCreationConnector.class.getCanonicalName());
	
	}

	@Override
	public void start() throws ComponentStartException {
		// TODO Auto-generated method stub
		super.start();
	}

	@Override
	public String[] submitApplication(String appURI, int nbVM) throws Exception {
		
		System.out.println("kill ùe in,sde");
		this.logMessage("New Application received in dynamic controller .\n Waiting for evaluation.");
		AllocatedCore[] allocatedCore = csop.allocateCores(NB_CORES);
		String dispatcherURI[] = new String[5];

		if(allocatedCore!=null && allocatedCore.length != 0) {
			
			dispatcherURI[0] = "RD_" + rdmopList.size()+"_"+appURI;
			dispatcherURI[1] = RequestDispatcherManagementInboundPortURI + "_" + appURI;
			dispatcherURI[2] = RequestSubmissionInboundPortURI +"_" + appURI;
			dispatcherURI[3] = RequestNotificationInboundPortURI + "_"+ appURI;
			dispatcherURI[4] = RequestNotificationOutboundPortURI + "_"+ appURI;
			
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
					RequestDispatcherManagementOutboundPortURI + rdmopList.size(),
					this);
			rdmop.publishPort();
			
			rdmop.doConnection(
				dispatcherURI[1],
				RequestDispatcherManagementConnector.class.getCanonicalName());
			
			String applicationVM[] = new String[4];

			for(int i=0;i<nbVM;i++)
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
						applicationVM[4], vm) ;
				
				avmPort.publishPort() ;
				avmPort.
						doConnection(
							ApplicationVMManagementInboundPortURI,
							ApplicationVMManagementConnector.class.getCanonicalName()) ;
				
				this.avmOutPort.add(avmPort);
				
				avmPort.allocateCores(allocatedCore);
				
				rdmop.connectVirtualMachine("vm"+this.avmOutPort.size(),RequestSubmissionInboundPortVMURI);
				avmPort.connectWithRequestSubmissioner(dispatcherURI[0], RequestNotificationInboundPortURI+ rdmopList.size());
			}
			
			rdmopList.put(appURI, rdmop);
			
			return dispatcherURI;
			
	}else {
		this.logMessage("Failed to allocates core for a new application.");
		return null;
	}
	}

	@Override
	public void submitGenerator(String RequestSubmissionInboundPort, String appUri, String rgURI) throws Exception {
		// TODO Auto-generated method stub
		super.submitGenerator(RequestSubmissionInboundPort, appUri, rgURI);
	}

	@Override
	public void shutdown() throws ComponentShutdownException {
		// TODO Auto-generated method stub
		super.shutdown();
	}

	@Override
	public String[] submitApplication(String appURI, int nbVM, Class interfaceToImplement) throws Exception {
		// TODO Auto-generated method stub
		return super.submitApplication(appURI, nbVM, interfaceToImplement);
	}
	
	
	


}
