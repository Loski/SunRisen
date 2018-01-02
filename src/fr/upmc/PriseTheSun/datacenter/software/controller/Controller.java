package fr.upmc.PriseTheSun.datacenter.software.controller;

import fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.ports.AdmissionControllerManagementOutboundPort;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces.RequestDispatcherDynamicStateI;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces.RequestDispatcherStateDataConsumerI;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces.RequestDispatcherStaticStateI;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.ports.RequestDispatcherDynamicStateDataOutboundPort;
import fr.upmc.components.AbstractComponent;



public class Controller extends AbstractComponent implements RequestDispatcherStateDataConsumerI{

	protected String controllerURI;
	protected String cmop;
	protected AdmissionControllerManagementOutboundPort acmop;
	protected  RequestDispatcherDynamicStateDataOutboundPort rddsdop;
	
	private int threesholdBottom;
	private int threesholdTop;

	
	@Override
	public void acceptRequestDispatcherDynamicData(String dispatcherURI,
			RequestDispatcherDynamicStateI currentDynamicState) throws Exception {
		long time = currentDynamicState.getTimeStamp();
		
	}
	@Override
	public void acceptRequestDispatcherStaticData(String dispatcherURI, RequestDispatcherStaticStateI staticState)
			throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	

	
}
