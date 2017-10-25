package fr.upmc.datacenterclient.applicationprovider.interfaces;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;

public interface ApplicationProviderManagementI extends	OfferedI, RequiredI{

	public void createAndSendApplication() throws Exception;
	public void stopApplication() throws Exception;
}
