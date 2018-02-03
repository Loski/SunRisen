package fr.upmc.PriseTheSun.datacenter.software.controller.connectors;

import fr.upmc.PriseTheSun.datacenter.software.controller.interfaces.NodeRingManagementI;
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
public class NodeManagementConnector extends		AbstractConnector
implements	NodeRingManagementI{

	@Override
	public void bindSendingDataUri(String DataInboundPortUri) throws Exception {
		((NodeRingManagementI)this.offering ).bindSendingDataUri(DataInboundPortUri);
	}

	@Override
	public void setNextManagementInboundPort(String managementInboundPort) throws Exception {
		((NodeRingManagementI)this.offering ).setNextManagementInboundPort(managementInboundPort);

	}

	@Override
	public void setPreviousManagementInboundPort(String managementInboundPort) throws Exception {
		((NodeRingManagementI)this.offering ).setPreviousManagementInboundPort(managementInboundPort);

	}

	@Override
	public void stopPushing() throws Exception {
		((NodeRingManagementI)this.offering ).stopPushing();
	}

	@Override
	public void startPushing() throws Exception {
		((NodeRingManagementI)this.offering ).startPushing();

	}

	@Override
	public void doDisconnectionInboundPort() throws Exception {
		((NodeRingManagementI)this.offering ).doDisconnectionInboundPort();
	}
}
