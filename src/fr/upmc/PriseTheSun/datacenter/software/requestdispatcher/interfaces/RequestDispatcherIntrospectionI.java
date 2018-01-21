package fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces;

import java.util.Map;

import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.RequestDispatcher.RequestDispatcherPortTypes;

public interface RequestDispatcherIntrospectionI {

	public Map<RequestDispatcherPortTypes, String>	getRequestDispatcherPortsURI() throws Exception ;
	
	public RequestDispatcherStaticStateI		getStaticState() throws Exception ;
	
	public RequestDispatcherDynamicStateI		getDynamicState() throws Exception ;

}
