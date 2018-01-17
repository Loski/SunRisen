package fr.upmc.PriseTheSun.datacenter.software.controller.connectors;

import fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.interfaces.AdmissionControllerManagementI;
import fr.upmc.PriseTheSun.datacenter.software.controller.interfaces.ControllerManagementI;
import fr.upmc.components.connectors.AbstractConnector;

/**
 * The class <code>ControllerManagementConnector</code>implements a
 * connector for ports exchanging through the interface
 * <code>ControllerManagementI</code>.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	true
 * </pre>
 * @author	Maxime Lavaste Loïc Lafontaine
 *
 */
public class ControllerManagementConnector extends		AbstractConnector
implements	ControllerManagementI{

	@Override
	public void bindSendingDataUri(String DataInboundPortUri) throws Exception {
		((ControllerManagementI)this.offering ).bindSendingDataUri(DataInboundPortUri);
	}


}
