package fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces;

import java.util.Map;

import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.RequestDispatcher.RequestDispatcherPortTypes;
import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;

public interface RequestDispatcherIntrospectionI 
extends		OfferedI,
RequiredI{

	public Map<RequestDispatcherPortTypes, String>	getRequestDispatcherPortsURI() throws Exception ;
	
	public RequestDispatcherStaticStateI		getStaticState() throws Exception ;
	
	public RequestDispatcherDynamicStateI		getDynamicState() throws Exception ;

}
