package fr.upmc.datacenter.software.controller;

import java.util.List;
import java.util.Map;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.components.exceptions.ComponentStartException;
import fr.upmc.components.ports.PortI;
import fr.upmc.datacenter.software.applicationvm.ApplicationVM;
import fr.upmc.datacenter.software.controller.interfaces.AdmissionControllerManagementI;
import fr.upmc.datacenter.software.controller.ports.AdmissionControllerManagementInboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionInboundPort;
import fr.upmc.datacenter.software.requestdispatcher.ports.RequestDispatcherManagementOutboundPort;
import fr.upmc.datacenterclient.applicationprovider.interfaces.ApplicationNotificationI;
import fr.upmc.datacenterclient.applicationprovider.interfaces.ApplicationSubmissionI;
import fr.upmc.datacenterclient.applicationprovider.ports.ApplicationNotificationOutboundPort;
import fr.upmc.datacenterclient.applicationprovider.ports.ApplicationSubmissionInboundPort;

import fr.upmc.datacenterclient.requestgenerator.ports.RequestGeneratorManagementOutboundPort;
import fr.upmc.datacenter.hardware.computers.interfaces.ComputerServicesI;
import fr.upmc.datacenter.hardware.computers.ports.ComputerServicesOutboundPort;


public class AdmissionController extends AbstractComponent implements ApplicationSubmissionI{

	protected String acURI;
	protected fr.upmc.datacenter.hardware.computers.ports.ComputerServicesOutboundPort ComputerServicesOutboundPort;
	protected List<ApplicationVM> vms ;
	protected ApplicationNotificationOutboundPort ApplicationNotificationOutboundPort;
	protected ComputerServicesOutboundPort csPort;
	private int nbVMCreated = 0;
	protected ApplicationSubmissionInboundPort asip;
	
	 // Map between RequestDispatcher URIs and the outbound ports to call them.
	protected Map<String, RequestDispatcherManagementOutboundPort> rdmopList;
	
	// Map between RequestSubmissionInboundPort URIs and the inboundPort
	protected Map<String, RequestSubmissionInboundPort> rsipList;
	
	
	protected String computerURI;
	
	ComputerServicesOutboundPort csop;
	
	public AdmissionController(String acURI, String applicationSubmissionInboundPortURI, String applicationNotificationInboundPortURI,
			String AdmissionControllerManagementInboundPortURI, String computerServiceOutboundPortURI, String computerURI,
			int[] nbAvailableCoresPerComputer) throws Exception {
		
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

		
		//Pour l'allocation de core.
		this.addRequiredInterface(ComputerServicesI.class);
	}

	@Override
	public String[] submitApplication(int nbVM) throws Exception {
		return null;
	}

	public String[] addCore(String rdUri, int nbCore) {
		// TODO Auto-generated method stub
		return null;
	}

}
