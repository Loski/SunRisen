package fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.interfaces;

import fr.upmc.PriseTheSun.datacenter.software.applicationvm.ApplicationVMInfo;
import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;

public interface AdmissionControllerManagementI extends	OfferedI, RequiredI{
	
	 public void linkComputer(String computerURI, String ComputerServicesInboundPortURI,String ComputerStaticStateDataInboundPortURI,
			 String ComputerDynamicStateDataInboundPortURI) throws Exception;
}