package fr.upmc.PriseTheSun.datacenter.software.controller;

import fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.interfaces.AdmissionControllerManagementI;
import fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.ports.AdmissionControllerManagementOutboundPort;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces.RequestDispatcherDynamicStateI;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces.RequestDispatcherStateDataConsumerI;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces.RequestDispatcherStaticStateI;
import fr.upmc.components.AbstractComponent;
import fr.upmc.components.exceptions.ComponentStartException;


public class Controller extends AbstractComponent implements RequestDispatcherStateDataConsumerI{

	private String controllerURI;
	private String cmop;
	private AdmissionControllerManagementOutboundPort acmop;
	
	private int threesholdBottom;
	private int threesholdTop;

	
	@Override
	public void acceptRequestDispatcherDynamicData(String dispatcherURI,
			RequestDispatcherDynamicStateI currentDynamicState) throws Exception {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void acceptRequestDispatcherStaticData(String dispatcherURI, RequestDispatcherStaticStateI staticState)
			throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	

	
}
