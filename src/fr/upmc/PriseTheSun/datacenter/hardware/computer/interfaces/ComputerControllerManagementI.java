package fr.upmc.PriseTheSun.datacenter.hardware.computer.interfaces;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;

public interface ComputerControllerManagementI extends OfferedI, RequiredI {
	/**
	 * Réserve nbToReserve coeurs dans le computer pour l'uri de la vm passée en paramètre.
	 * @param vmURI
	 * @param nbToReserve Nombre de coeurs à réserver
	 * @param numberAllocated Nombre de coeurs déjà allouées pour cette VM
	 * @return Le nombre de coeurs réservés
	 * @throws Exception
	 */
	public int tryReserveCore(String vmURI, int nbToReserve, int numberAllocated) throws Exception;
	
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
	
	/**
	 * Demande l'allocation de numberToAllocated à la VM.
	 * @param numberToAllocated Nombre de VM a alloué
	 * @return coeurs alloués
	 * @throws Exception
	 */
	public AllocatedCore[] allocateCores(int numberToAllocated) throws Exception;
	
	/**
	 * Retoune le nombre de VM enregistré dans le controller de cet ordinateur.
	 * @return le nombre de vm
	 * @throws Exception
	 */
	public int compteurVM() throws Exception;
}
