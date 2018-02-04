package fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.interfaces;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;
/**
 * Interface de management d'un <code>AdmissionControllerDynamic</code>
 * @author Maxime Lavaste
 */
public interface AdmissionControllerManagementI extends	OfferedI, RequiredI{
	
	/**
	 * Ajoute un nouvel ordinateur à l'admission controller.
	 * @param computerURI
	 * @param ComputerServicesInboundPortURI
	 * @param ComputerStaticStateDataInboundPortURI
	 * @param ComputerDynamicStateDataInboundPortURI
	 * @throws Exception
	 */
	 public void linkComputer(String computerURI, String ComputerServicesInboundPortURI,String ComputerStaticStateDataInboundPortURI,
			 String ComputerDynamicStateDataInboundPortURI) throws Exception;
}