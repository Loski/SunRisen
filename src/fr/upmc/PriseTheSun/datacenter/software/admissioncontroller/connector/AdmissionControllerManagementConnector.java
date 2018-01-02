package fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.connector;


import fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.interfaces.AdmissionControllerManagementI;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces.RequestDispatcherManagementI;
import fr.upmc.components.connectors.AbstractConnector;

public class AdmissionControllerManagementConnector extends	AbstractConnector implements AdmissionControllerManagementI
{

	@Override
	public boolean addCores(String rdURI, int nbCores) throws Exception {
		return ( ( AdmissionControllerManagementI ) this.offering ).addCores( rdURI,  nbCores);

	}
	
}
