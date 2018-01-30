package fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.ports;

import java.util.Map;

import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.RequestDispatcher;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.RequestDispatcher.RequestDispatcherPortTypes;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces.RequestDispatcherDynamicStateI;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces.RequestDispatcherIntrospectionI;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces.RequestDispatcherStaticStateI;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;

public class RequestDispatcherIntrospectionInboundPort 
extends		AbstractInboundPort
implements	RequestDispatcherIntrospectionI{


	private static final long serialVersionUID = 7420802098137514687L;

	public				RequestDispatcherIntrospectionInboundPort(
			ComponentI owner
			) throws Exception
		{
			super(RequestDispatcherIntrospectionI.class, owner) ;
			
			assert	owner instanceof RequestDispatcher ;
		}

		public				RequestDispatcherIntrospectionInboundPort(
			String uri,
			ComponentI owner
			) throws Exception
		{
			super(uri, RequestDispatcherIntrospectionI.class, owner);
			
			assert	owner instanceof RequestDispatcher ;
		}

		@Override
		public Map<RequestDispatcherPortTypes, String> getRequestDispatcherPortsURI() throws Exception {
			final RequestDispatcher rd = (RequestDispatcher) this.owner ;
			return this.owner.handleRequestSync(
								new ComponentI.ComponentService<
													Map<RequestDispatcherPortTypes,
														String>>() {
									@Override
									public Map<RequestDispatcherPortTypes, String>
																			call()
									throws Exception
									{
										return rd.getDispatcherPortsURI() ;
									}
								}) ;
		}

		@Override
		public RequestDispatcherStaticStateI getStaticState() throws Exception {
			final RequestDispatcher rd = (RequestDispatcher) this.owner ;
			return this.owner.handleRequestSync(
					new ComponentI.ComponentService<RequestDispatcherStaticStateI>() {
							@Override
							public RequestDispatcherStaticStateI call()
							throws Exception
							{
								return rd.getStaticState() ;
							}
						}) ;
		}

		@Override
		public RequestDispatcherDynamicStateI getDynamicState() throws Exception {
			final RequestDispatcher rd = (RequestDispatcher) this.owner ;
			return this.owner.handleRequestSync(
					new ComponentI.ComponentService<RequestDispatcherDynamicStateI>() {
							@Override
							public RequestDispatcherDynamicStateI call()
							throws Exception
							{
								return rd.getDynamicState() ;
							}
						}) ;
		}
}


