package fr.upmc.datacenter.software.controller;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.pre.dcc.ports.DynamicComponentCreationOutboundPort;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.components.exceptions.ComponentStartException;
import fr.upmc.components.interfaces.DataRequiredI;
import fr.upmc.components.ports.PortI;
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
 * @author	Maxime LAVASTE Lo�c LAFONTAINE
 */
public class AdmissionControllerDynamic extends AdmissionController implements ApplicationSubmissionI{

	public AdmissionControllerDynamic(String acURI, String applicationSubmissionInboundPortURI,
			String AdmissionControllerManagementInboundPortURI, String computerServiceOutboundPortURI,
			String ComputerServicesInboundPortURI, String computerURI, int nbAvailableCores,
			String computerStaticStateDataOutboundPortURI) throws Exception {
		super(acURI, applicationSubmissionInboundPortURI, AdmissionControllerManagementInboundPortURI,
				computerServiceOutboundPortURI, ComputerServicesInboundPortURI, computerURI, nbAvailableCores,
				computerStaticStateDataOutboundPortURI);
		
	}

	@Override
	public void start() throws ComponentStartException {
		// TODO Auto-generated method stub
		super.start();
	}

	@Override
	public String[] submitApplication(String appURI, int nbVM) throws Exception {
		
		
		this.logMessage("New Application received.\n Waiting for evaluation.");
		AllocatedCore[] allocatedCore = csop.allocateCores(NB_CORES);
		String dispatcherURI[] = new String[4];

		if(allocatedCore!=null && allocatedCore.length != 0) {
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
			
			dispatcherURI[0] = "RD_" + rdmopList.size();
			dispatcherURI[1] = RequestSubmissionInboundPortURI+ rdmopList.size();
			
			for(int i=0;i<nbVM;i++)
			{
				// --------------------------------------------------------------------
				// Create an Application VM component
				// --------------------------------------------------------------------
				
				
				String ApplicationVMManagementInboundPortURI = "avmibp-"+this.avmOutPort.size();
				String RequestSubmissionInboundPortVMURI = "rsibpVM-"+this.avmOutPort.size();
				String RequestNotificationOutboundPortVMURI = "rnobpVM-"+this.avmOutPort.size();
				String ApplicationVMManagementOutboundPortURI = "avmobp-"+this.avmOutPort.size();
				
				ApplicationVM vm = new ApplicationVM("vm"+this.avmOutPort.size(),	// application vm component URI
						ApplicationVMManagementInboundPortURI,
					    RequestSubmissionInboundPortVMURI,
					    RequestNotificationOutboundPortVMURI) ;
				//this.addDeployedComponent(vm) ;
	
				vm.toggleTracing() ;
				vm.toggleLogging() ;
				
				// Create a mock up port to manage the AVM component (allocate cores).
				ApplicationVMManagementOutboundPort avmPort = new ApplicationVMManagementOutboundPort(
											ApplicationVMManagementOutboundPortURI,
											vm) ;
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
