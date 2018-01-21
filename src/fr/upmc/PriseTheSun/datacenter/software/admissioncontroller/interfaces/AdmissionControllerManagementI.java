package fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.interfaces;

import fr.upmc.PriseTheSun.datacenter.software.applicationvm.ApplicationVMInfo;
import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;

public interface AdmissionControllerManagementI extends	OfferedI, RequiredI{
	 public int addCores(String controllerURI, String vmUri, int nbCores ) throws Exception;
	 public boolean supCores(int nbCores, String vmUri ) throws Exception;
	 public void linkComputer(String computerURI, String ComputerServicesInboundPortURI,String ComputerStaticStateDataInboundPortURI,
			 String ComputerDynamicStateDataInboundPortURI) throws Exception;
	 public void releaseCore(String controllerURI, String VMURI) throws Exception;
	 public void allocVm(String appURI, ApplicationVMInfo vm, String dispatcherURI, String dispatcherNotificationInboundPort) throws Exception;
}