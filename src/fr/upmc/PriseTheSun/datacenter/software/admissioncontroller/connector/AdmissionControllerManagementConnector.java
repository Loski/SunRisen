package fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.connector;


import fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.interfaces.AdmissionControllerManagementI;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces.RequestDispatcherManagementI;
import fr.upmc.components.connectors.AbstractConnector;

public class AdmissionControllerManagementConnector extends	AbstractConnector implements AdmissionControllerManagementI
{

	@Override
	public int addCores(String controllerURI, String vmUri, int nbCores) throws Exception {
		return ( ( AdmissionControllerManagementI ) this.offering ).addCores( controllerURI,  null, nbCores);

	}

	@Override
	public void linkComputer(String computerURI, String ComputerServicesInboundPortURI,
			String ComputerStaticStateDataInboundPortURI, String ComputerDynamicStateDataInboundPortURI)
			throws Exception {
		 ( ( AdmissionControllerManagementI ) this.offering ).linkComputer(computerURI, ComputerServicesInboundPortURI, ComputerStaticStateDataInboundPortURI, ComputerDynamicStateDataInboundPortURI);
	}

	@Override
	public boolean supCores(int nbCores, String vmUri) throws Exception {
		return ( ( AdmissionControllerManagementI ) this.offering ).supCores(nbCores, vmUri);

	}

	@Override
	public void releaseCore(String controllerURI, String VMURI) throws Exception {
		( ( AdmissionControllerManagementI ) this.offering ).releaseCore(controllerURI, VMURI);
		
	}
	
}
