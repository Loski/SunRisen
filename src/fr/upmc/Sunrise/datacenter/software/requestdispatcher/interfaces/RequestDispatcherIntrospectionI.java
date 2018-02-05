package fr.upmc.Sunrise.datacenter.software.requestdispatcher.interfaces;

import java.util.Map;

import fr.upmc.Sunrise.datacenter.software.requestdispatcher.RequestDispatcher.RequestDispatcherPortTypes;
import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;

/**
 * The class <code>RequestDispatcherIntrospectionI</code> defines the
 * component services to introspect request dispatcher
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

public interface RequestDispatcherIntrospectionI 
extends		OfferedI,
			RequiredI{

	/**
	 * return a map of dispatcher port URI by their types.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	return != null
	 * </pre>
	 *
	 * @return	a map from  dispatcher port types to their URI.
	 * @throws Exception
	 */
	public Map<RequestDispatcherPortTypes, String>	getRequestDispatcherPortsURI() throws Exception ;
	
	/**
	 * return the static state of the dispatcher as an instance of
	 * <code>RequestDispatcherStaticStateI</code>.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	return != null
	 * </pre>
	 *
	 * @return	the current static state of the dispatcher.
	 * @throws Exception
	 */
	public RequestDispatcherStaticStateI		getStaticState() throws Exception ;
	
	/**
	 * return the dynamic state of the dispatcher as an instance of
	 * <code>RequestDispatcherDynamicStateI</code>.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	return != null
	 * </pre>
	 *
	 * @return	the dynamic state of the dispatcher
	 * @throws Exception
	 */
	public RequestDispatcherDynamicStateI		getDynamicState() throws Exception ;

}
