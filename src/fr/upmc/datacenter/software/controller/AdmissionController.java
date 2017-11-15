package fr.upmc.datacenter.software.controller;

import java.util.List;
import java.util.Map;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.pre.dcc.ports.DynamicComponentCreationOutboundPort;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.components.exceptions.ComponentStartException;
import fr.upmc.components.interfaces.DataRequiredI;
import fr.upmc.components.ports.PortI;
import fr.upmc.datacenter.software.applicationvm.ApplicationVM;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.datacenter.software.controller.interfaces.AdmissionControllerManagementI;
import fr.upmc.datacenter.software.controller.ports.AdmissionControllerManagementInboundPort;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionI;
import fr.upmc.datacenter.software.ports.RequestSubmissionInboundPort;
import fr.upmc.datacenter.software.requestdispatcher.RequestDispatcher;
import fr.upmc.datacenter.software.requestdispatcher.connectors.RequestDispatcherManagementConnector;
import fr.upmc.datacenter.software.requestdispatcher.ports.RequestDispatcherManagementOutboundPort;
import fr.upmc.datacenterclient.applicationprovider.interfaces.ApplicationNotificationI;
import fr.upmc.datacenterclient.applicationprovider.interfaces.ApplicationSubmissionI;
import fr.upmc.datacenterclient.applicationprovider.ports.ApplicationNotificationOutboundPort;
import fr.upmc.datacenterclient.applicationprovider.ports.ApplicationSubmissionInboundPort;
import fr.upmc.datacenterclient.requestgenerator.connectors.RequestGeneratorManagementConnector;
import fr.upmc.datacenterclient.requestgenerator.ports.RequestGeneratorManagementOutboundPort;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;
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
 * @author	Maxime LAVASTE Lo�c LAFONTAINE
 */
public class AdmissionController extends AbstractComponent implements ApplicationSubmissionI{

	protected String acURI;


	private static final String RequestDispatcherManagementInboundPortURI = null;
	private static final String RequestNotificationInboundPortURI = null;
	private static final String RequestSubmissionInboundPortURI = null;
	private static final String RequestNotificationOutboundPortURI = null;
	private static final String RequestDispatcherManagementOutboundPortURI = null;

	
	private static final int NB_CORES = 2;


	protected fr.upmc.datacenter.hardware.computers.ports.ComputerServicesOutboundPort ComputerServicesOutboundPort;
	protected ApplicationNotificationOutboundPort ApplicationNotificationOutboundPort;
	private AdmissionControllerManagementInboundPort acmip;
	protected ApplicationSubmissionInboundPort asip;
	
	
	protected ApplicationVMManagementOutboundPort avmOutPort;

	
	protected ComputerServicesOutboundPort csPort;
	protected ComputerStaticStateDataOutboundPort cssdop;
	protected ComputerDynamicStateDataOutboundPort cdsdop;
	
	private int nbVMCreated = 0;
	protected List<ApplicationVM> vms ;
	

	 // Map between RequestDispatcher URIs and the outbound ports to call them.
	protected Map<String, RequestDispatcherManagementOutboundPort> rdmopList;
	
	// Map between RequestSubmissionInboundPort URIs and the inboundPort
	protected Map<String, RequestSubmissionInboundPort> rsipList;
	
	
	protected String computerURI;
	
	ComputerServicesOutboundPort csop;
	
	public AdmissionController(String acURI, String applicationSubmissionInboundPortURI,
			String AdmissionControllerManagementInboundPortURI,
			String computerServiceOutboundPortURI, String computerURI,
			int nbAvailableCores, String computerStaticStateDataOutboundPortURI, String computerServicesOutboundPortURI) throws Exception {

		
		super(2, 2);
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
		this.computerURI = computerURI;

		this.cssdop = new ComputerStaticStateDataOutboundPort(computerStaticStateDataOutboundPortURI, this, computerURI);
		this.addPort(this.cssdop);
		this.cssdop.publishPort();
		
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
		



		
		
		
		//Pour l'allocation de core.
		this.addRequiredInterface(ComputerServicesI.class);
	}

	/**
	 * @see fr.upmc.datacenter.admissioncontroller.interfaces.ApplicationSubmissionI#submitApplication(java.lang.Integer, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String submitApplication(int nbVM) throws Exception {
		
		this.logMessage("New Application received.\n Waiting for evaluation.");
		AllocatedCore[] allocatedCore = csop.allocateCores(NB_CORES);
		if(allocatedCore.length != 0) {
			RequestDispatcher rd = new RequestDispatcher("RD_" + rdmopList.size(), RequestDispatcherManagementInboundPortURI+ rdmopList.size(), RequestSubmissionInboundPortURI+ rdmopList.size(),
				    RequestNotificationOutboundPortURI+ rdmopList.size(), RequestNotificationInboundPortURI+ rdmopList.size()) ;
			rd.toggleLogging();
			rd.toggleTracing();
			
			RequestDispatcherManagementOutboundPort rdmop = new RequestDispatcherManagementOutboundPort(
					RequestDispatcherManagementOutboundPortURI + rdmopList.size(),
					rd) ;
			rdmop.publishPort();
			rdmop.doConnection(
				RequestDispatcherManagementInboundPortURI + rdmopList.size(),
				RequestDispatcherManagementConnector.class.getCanonicalName());
			for(ApplicationVM vm : vms) {
				rdmop.connectVirtualMachine("NAME VM URI", vm.findPortURIsFromInterface(RequestSubmissionI.class)[0]);
			}
		}else {
			this.logMessage("Failed to allocates core for a new application.");
			return null;
		}
		
		return RequestDispatcherManagementInboundPortURI+ rdmopList.size();
	}

	public String[] addCore(String rdUri, int nbCore) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	@Override
	public void shutdown() throws ComponentShutdownException {
		
		try {			
			if (this.csop.connected()) {
				this.csop.doDisconnection();
			}
			if (this.cssdop.connected()) {
				this.cssdop.doDisconnection();
			}
			if (this.cdsdop.connected()) {
				this.cdsdop.doDisconnection();
			}
		} catch (Exception e) {
			throw new ComponentShutdownException("Port disconnection error", e);
		}

		super.shutdown();
	}

}
