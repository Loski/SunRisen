package fr.upmc.Sunrise.datacenter.software.requestdispatcher.interfaces;

/**
 * The interface <code>RequestDispatcherStateDataConsumerI</code> defines the consumer
 * side methods used to receive state data pushed by a dispatcher, both static
 * and dynamic.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * @author	Loïc Lafontaine
 * @author	Maxime Lavaste
 */

public interface RequestDispatcherStateDataConsumerI {

	public void 		acceptRequestDispatcherDynamicData( 
			String dispatcherURI, 
			RequestDispatcherDynamicStateI currentDynamicState 
			) throws Exception;
	
	public void			acceptRequestDispatcherStaticData(
			String					dispatcherURI,
			RequestDispatcherStaticStateI	staticState
			) throws Exception ;
}
