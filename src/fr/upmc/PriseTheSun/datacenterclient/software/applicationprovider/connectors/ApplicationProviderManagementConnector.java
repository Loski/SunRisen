package fr.upmc.PriseTheSun.datacenterclient.software.applicationprovider.connectors;

import fr.upmc.PriseTheSun.datacenterclient.software.applicationprovider.interfaces.ApplicationProviderManagementI;
import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionI;

/**
 *  * The class <code>ApplicationProviderManagementConnector</code> implements a
 * connector for ports exchanging through the interface
 * <code>ProcessorManagementI</code>.
 * @author maxime LAVASTE Loïc Lafontaine
 *
 */
public class ApplicationProviderManagementConnector extends	AbstractConnector
implements	ApplicationProviderManagementI  {

	@Override
	public void createAndSendApplication() throws Exception {
		( ( ApplicationProviderManagementI ) this.offering ).createAndSendApplication();
	}

	@Override
	public void stopApplication() throws Exception {
		( ( ApplicationProviderManagementI ) this.offering ).stopApplication();
	}

	@Override
	public void createAndSendApplication(Class<RequestSubmissionI> class1) throws Exception {
		( ( ApplicationProviderManagementI ) this.offering ).createAndSendApplication(class1);
	}

}
