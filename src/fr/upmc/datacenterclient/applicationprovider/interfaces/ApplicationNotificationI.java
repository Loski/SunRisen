package fr.upmc.datacenterclient.applicationprovider.interfaces;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;
import fr.upmc.datacenter.software.interfaces.RequestI;

public interface ApplicationNotificationI extends OfferedI, RequiredI{
	void notifyRequestGeneratorCreated(String requestNotificationInboundPortURI, String rdnop) throws Exception;
}
