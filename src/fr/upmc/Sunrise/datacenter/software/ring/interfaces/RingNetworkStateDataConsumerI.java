package fr.upmc.Sunrise.datacenter.software.ring.interfaces;

/**
 * Interface à implémenter pour devenir un observateur du ring network.
 * @author Maxime Lavaste
 *
 */
public interface RingNetworkStateDataConsumerI {
	/**
	 * Fonction de réception d'une vm libre dans le ring network.
	 * @param controllerUri
	 * @param currentDynamicState 
	 * @throws Exception
	 */
	public void	acceptRingNetworkDynamicData(String controllerUri, RingNetworkDynamicStateI	currentDynamicState) throws Exception ;
}
