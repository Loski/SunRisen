package fr.upmc.PriseTheSun.datacenter.software.ring.interfaces;
/**
 ** @author	Maxime Lavaste and Loïc Lafontaine
 */

public interface RingDataI {
	
	
		public void			acceptRingDynamicData(
			String					requestDispatcherURI,
			RingDynamicStateI	currentDynamicState
			) throws Exception ;
}
