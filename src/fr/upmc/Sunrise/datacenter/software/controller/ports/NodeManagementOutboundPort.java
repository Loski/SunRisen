package fr.upmc.Sunrise.datacenter.software.controller.ports;

import fr.upmc.Sunrise.datacenter.software.controller.interfaces.NodeRingManagementI;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
/**
* The class <code>ControllerManagementOutboundPort</code> implements the
 * inbound port through which the component management methods are called.
* 
* 
* @author	Maxime LAVASTE Lo�c Lafontaine
*/
public class NodeManagementOutboundPort extends AbstractOutboundPort
implements	NodeRingManagementI{

    /***
     * 
     * @param owner       owner component
     * @throws Exception e
     */
	public	NodeManagementOutboundPort(ComponentI owner) throws Exception
	{
		super(NodeRingManagementI.class, owner) ;
		assert	owner != null ;
	}

	/***
	 *  
	 * @param uri             uri of the component
	 * @param owner           owner component
	 * @throws Exception e
	 */
	public	NodeManagementOutboundPort(String uri, ComponentI owner) throws Exception
	{
		super(uri, NodeRingManagementI.class, owner);
		assert	owner != null;
	}

	@Override
	public void bindSendingDataUri(String DataInboundPortUri) throws Exception {
		((NodeRingManagementI)this.connector).bindSendingDataUri(DataInboundPortUri);
	}

	@Override
	public void setNextManagementInboundPort(String managementInboundPort) throws Exception {
		((NodeRingManagementI)this.connector).setNextManagementInboundPort(managementInboundPort);		
	}

	@Override
	public void setPreviousManagementInboundPort(String managementInboundPort) throws Exception {
		((NodeRingManagementI)this.connector).setPreviousManagementInboundPort(managementInboundPort);
	}

	@Override
	public void stopPushing() throws Exception {
		((NodeRingManagementI)this.connector).stopPushing();
	}

	@Override
	public void startPushing() throws Exception {
		((NodeRingManagementI)this.connector).startPushing();
	}

	@Override
	public void doDisconnectionInboundPort() throws Exception {
		((NodeRingManagementI)this.connector).doDisconnectionInboundPort();
	}

}
