package fr.upmc.datacenter.software.controller;

import java.util.List;
import java.util.Map;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.components.exceptions.ComponentStartException;
import fr.upmc.components.ports.PortI;
import fr.upmc.datacenter.software.applicationvm.ApplicationVM;
import fr.upmc.datacenter.software.ports.RequestSubmissionInboundPort;
import fr.upmc.datacenter.software.requestdispatcher.ports.RequestDispatcherManagementOutboundPort;
import fr.upmc.datacenterclient.applicationprovider.ports.ApplicationNotificationOutboundPort;
import fr.upmc.datacenterclient.requestgenerator.ports.RequestGeneratorManagementOutboundPort;

public class AdmissionController extends AbstractComponent{

	protected String acURI;
	protected fr.upmc.datacenter.hardware.computers.ports.ComputerServicesOutboundPort ComputerServicesOutboundPort;
	protected List<ApplicationVM> vms ;
	protected ApplicationNotificationOutboundPort ApplicationNotificationOutboundPort;
	
	private int nbVMCreated = 0;

	
	 // Map between RequestDispatcher URIs and the outbound ports to call them.
	protected Map<String, RequestDispatcherManagementOutboundPort> rdmopList;
	
	// Map between RequestSubmissionInboundPort URIs and the inboundPort
	protected Map<String, RequestSubmissionInboundPort> rsipList;

	public AdmissionController(int nbThreads, int nbSchedulableThreads) {
		super(nbThreads, nbSchedulableThreads);
	}

}
