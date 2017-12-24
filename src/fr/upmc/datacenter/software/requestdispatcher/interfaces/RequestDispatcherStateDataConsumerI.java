package fr.upmc.datacenter.software.requestdispatcher.interfaces;

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
