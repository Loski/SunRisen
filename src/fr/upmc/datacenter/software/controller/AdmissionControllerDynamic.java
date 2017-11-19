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
import fr.upmc.components.pre.reflection.connectors.ReflectionConnector;
import fr.upmc.components.pre.reflection.ports.ReflectionOutboundPort;
import fr.upmc.datacenter.software.applicationvm.ApplicationVM;
import fr.upmc.datacenter.software.applicationvm.connectors.ApplicationVMManagementConnector;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.datacenter.software.connectors.RequestNotificationConnector;
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
import fr.upmc.javassist.ConnectorCreator;
import fr.upmc.javassist.RequestDispatcherCreator;
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
 * @author	Maxime LAVASTE Loïc LAFONTAINE
 */
public class AdmissionControllerDynamic extends AdmissionController implements ApplicationSubmissionI, AdmissionControllerManagementI{

	public static int	DEBUG_LEVEL = 1 ;
	
	private DynamicComponentCreationOutboundPort portToRequestDispatcherJVM;
	private DynamicComponentCreationOutboundPort portToApplicationVMJVM;

	/*protected static final String RequestDispatcher_JVM_URI = "controller" ;
	protected static final String Application_VM_JVM_URI = "controller";*/
	
	public AdmissionControllerDynamic(String acURI, String applicationSubmissionInboundPortURI,
			String AdmissionControllerManagementInboundPortURI, String computerServiceOutboundPortURI,
			String ComputerServicesInboundPortURI, String computerURI, int nbAvailableCores,
			String computerStaticStateDataOutboundPortURI,
			String RequestDispatcher_JVM_URI,
			String Application_VM_JVM_URI
			) throws Exception {
		
		super(acURI, applicationSubmissionInboundPortURI,
				AdmissionControllerManagementInboundPortURI, computerServiceOutboundPortURI,
				ComputerServicesInboundPortURI, computerURI, nbAvailableCores,
				computerStaticStateDataOutboundPortURI);
		
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
	
	}

	@Override
	public void start() throws ComponentStartException {
		// TODO Auto-generated method stub
		super.start();
	}

	@Override
	public synchronized String[] submitApplication(String appURI, int nbVM) throws Exception {
		
		this.logMessage("New Application received in dynamic controller ("+appURI+")"+".\n Waiting for evaluation ");
		
		AllocatedCore[] allocatedCore = csop.allocateCores(NB_CORES);
		String dispatcherURI[] = new String[6];

		if(allocatedCore!=null && allocatedCore.length != 0) {
			
			dispatcherURI[0] = "RD" + rdmopList.size()+"-"+appURI;
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
					RequestDispatcherManagementOutboundPortURI + rdmopList.size(),
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

		try {
			if(portToRequestDispatcherJVM.connected())
				portToRequestDispatcherJVM.doDisconnection();
			if(portToApplicationVMJVM.connected())
				portToApplicationVMJVM.doDisconnection();
		} catch (Exception e) {
			throw new ComponentShutdownException("Port disconnection error", e);
		}
		
		super.shutdown();
	}
	
	
	@Override
	public synchronized String[] submitApplication(String appURI, int nbVM,Class submissionInterface) throws Exception {
		
		assert submissionInterface.isInterface();
		
		this.logMessage("New Application received in dynamic controller ("+appURI+")"+".\n Waiting for evaluation ");
		
		AllocatedCore[] allocatedCore = csop.allocateCores(NB_CORES);
		String dispatcherURI[] = new String[6];

		if(allocatedCore!=null && allocatedCore.length != 0) {
			
			dispatcherURI[0] = "RD_" + rdmopList.size()+"_"+appURI;
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
					RequestDispatcherManagementOutboundPortURI + rdmopList.size(),
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
			
			rdmopList.put(appURI, rdmop);
			
			return dispatcherURI;
			
	}else {
		this.logMessage("Failed to allocates core for a new application.");
		return null;
	}
	}
	


}
