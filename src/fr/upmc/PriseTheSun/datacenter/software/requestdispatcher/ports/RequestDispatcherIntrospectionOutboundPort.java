package fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.ports;

import java.util.Map;

import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.RequestDispatcher.RequestDispatcherPortTypes;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces.RequestDispatcherDynamicStateI;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces.RequestDispatcherIntrospectionI;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces.RequestDispatcherStaticStateI;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.datacenter.software.applicationvm.interfaces.ApplicationVMIntrospectionI;

public class RequestDispatcherIntrospectionOutboundPort 
extends		AbstractOutboundPort
implements	RequestDispatcherIntrospectionI{

	public				RequestDispatcherIntrospectionOutboundPort(
			ComponentI owner
			) throws Exception
		{
			super(RequestDispatcherIntrospectionI.class, owner);
		}

		public				RequestDispatcherIntrospectionOutboundPort(
			String uri,
			ComponentI owner
			) throws Exception
		{
			super(uri, RequestDispatcherIntrospectionI.class, owner);
		}

		@Override
		public Map<RequestDispatcherPortTypes, String> getRequestDispatcherPortsURI() throws Exception {
			return ((RequestDispatcherIntrospectionI)this.connector).getRequestDispatcherPortsURI() ;
		}

		@Override
		public RequestDispatcherStaticStateI getStaticState() throws Exception {
			return ((RequestDispatcherIntrospectionI)this.connector).getStaticState() ;
		}

		@Override
		public RequestDispatcherDynamicStateI getDynamicState() throws Exception {
			return ((RequestDispatcherIntrospectionI)this.connector).getDynamicState() ;
		}
	
}
