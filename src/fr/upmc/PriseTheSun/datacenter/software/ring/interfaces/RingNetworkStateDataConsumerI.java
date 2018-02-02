package fr.upmc.PriseTheSun.datacenter.software.ring.interfaces;

/**
 * 
 * @author Maxime Lavaste
 *
 */
public interface RingNetworkStateDataConsumerI {
	public void	acceptRingNetworkDynamicData(String controllerUri, RingNetworkDynamicStateI	currentDynamicState) throws Exception ;
}
