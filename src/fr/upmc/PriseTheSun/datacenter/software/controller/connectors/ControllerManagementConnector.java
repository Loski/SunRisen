package fr.upmc.PriseTheSun.datacenter.software.controller.connectors;

import fr.upmc.PriseTheSun.datacenter.software.controller.interfaces.ControllerRingManagementI;
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
implements	ControllerRingManagementI{

	@Override
	public void bindSendingDataUri(String DataInboundPortUri) throws Exception {
		((ControllerRingManagementI)this.offering ).bindSendingDataUri(DataInboundPortUri);
	}

	@Override
	public void setNextManagementInboundPort(String managementInboundPort) throws Exception {
		((ControllerRingManagementI)this.offering ).setNextManagementInboundPort(managementInboundPort);

	}

	@Override
	public void setPreviousManagementInboundPort(String managementInboundPort) throws Exception {
		((ControllerRingManagementI)this.offering ).setPreviousManagementInboundPort(managementInboundPort);

	}

	@Override
	public void stopPushing() throws Exception {
		((ControllerRingManagementI)this.offering ).stopPushing();
	}
}
