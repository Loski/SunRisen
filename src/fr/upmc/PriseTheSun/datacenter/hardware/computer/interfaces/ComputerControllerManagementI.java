package fr.upmc.PriseTheSun.datacenter.hardware.computer.interfaces;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;

public interface ComputerControllerManagementI extends OfferedI, RequiredI {
	/**
	 * Réserve nbToReserve coeurs dans le computer pour l'uri de la vm passé en paramètre.
	 * @param vmURI
	 * @param nbToReserve Nombre de coeurs à réserver
	 * @return Le nombre de coeurs réservés
	 * @throws Exception
	 */
	public int tryReserveCore(String vmURI, int nbToReserve) throws Exception;
	
	/**
	 * Libère tous les coeurs réservés de la VM
	 * @param vmURI
	 * @throws Exception
	 */
	public void releaseCore(String vmURI) throws Exception;
	
	//TODO Définir mieux ?
	/**
	 * Demande l'allocation des coeurs.
	 * @param vmURI
	 * @return
	 * @throws Exception
	 */
	public AllocatedCore[] addCores(String vmURI) throws Exception;
	
	
	public AllocatedCore[] allocateCores(int i) throws Exception;
	
	public int compteurVM() throws Exception;
}
