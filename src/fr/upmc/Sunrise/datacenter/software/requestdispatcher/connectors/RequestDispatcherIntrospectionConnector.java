package fr.upmc.Sunrise.datacenter.software.requestdispatcher.connectors;

import java.util.Map;

import fr.upmc.Sunrise.datacenter.software.requestdispatcher.RequestDispatcher.RequestDispatcherPortTypes;
import fr.upmc.Sunrise.datacenter.software.requestdispatcher.interfaces.RequestDispatcherDynamicStateI;
import fr.upmc.Sunrise.datacenter.software.requestdispatcher.interfaces.RequestDispatcherIntrospectionI;
import fr.upmc.Sunrise.datacenter.software.requestdispatcher.interfaces.RequestDispatcherStaticStateI;
import fr.upmc.components.connectors.AbstractConnector;

/**
 * The class <code>RequestDispatcherIntrospectionConnector</code> defines a
 * connector associated with the interface
 * <code>RequestDispatcherIntrospectionI</code>.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	true
 * </pre>
 * 
 * @author	Loïc Lafontaine
 * @author	Maxime Lavaste
 */

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
