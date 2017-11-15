package fr.upmc.datacenterclient.applicationprovider.interfaces;

import fr.upmc.datacenter.hardware.tests.TestApplicationVM.Request;
import fr.upmc.datacenter.software.connectors.RequestSubmissionConnector;
import fr.upmc.datacenter.software.ports.RequestSubmissionInboundPort;
import fr.upmc.datacenterclient.requestgenerator.ports.RequestGeneratorManagementInboundPort;

public interface ApplicationSubmissionI {

	String[] submitApplication(String appURI, int nbVM) throws Exception;
	void submitGenerator(String requestNotificationInboundPort, String appURI, String rgURI) throws Exception;
}
