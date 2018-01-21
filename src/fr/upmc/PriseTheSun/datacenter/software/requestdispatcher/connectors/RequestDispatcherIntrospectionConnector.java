package fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.connectors;

import java.util.Map;

import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.RequestDispatcher.RequestDispatcherPortTypes;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces.RequestDispatcherDynamicStateI;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces.RequestDispatcherIntrospectionI;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces.RequestDispatcherStaticStateI;
import fr.upmc.components.connectors.AbstractConnector;

public class RequestDispatcherIntrospectionConnector 
extends		AbstractConnector
implements	RequestDispatcherIntrospectionI{

	@Override
	public Map<RequestDispatcherPortTypes, String> getRequestDispatcherPortsURI() throws Exception {

		return ((RequestDispatcherIntrospectionI)this.offering).getRequestDispatcherPortsURI() ;
	}

	@Override
	public RequestDispatcherStaticStateI getStaticState() throws Exception {

		return ((RequestDispatcherIntrospectionI)this.offering).getStaticState() ;
	}

	@Override
	public RequestDispatcherDynamicStateI getDynamicState() throws Exception {
		return ((RequestDispatcherIntrospectionI)this.offering).getDynamicState() ;
	}

}
