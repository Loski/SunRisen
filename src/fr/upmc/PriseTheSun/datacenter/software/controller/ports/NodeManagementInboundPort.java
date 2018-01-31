package fr.upmc.PriseTheSun.datacenter.software.controller.ports;

import fr.upmc.PriseTheSun.datacenter.software.controller.Controller;
import fr.upmc.PriseTheSun.datacenter.software.controller.interfaces.NodeRingManagementI;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;

/**
* The class <code>ControllerManagementInboundPort</code> implements the
 * inbound port through which the component management methods are called.
* 
* <p>Created on : 2016-2017</p>
* 
* @author Maxime LAVASTE Loïc Lafontaine
*/
public class NodeManagementInboundPort extends AbstractInboundPort implements NodeRingManagementI{
	private static final long serialVersionUID = 1L;

	/***
	 * 
	 * @param owner     owner component
	 * @throws Exception e
	 */
	public	NodeManagementInboundPort(ComponentI owner) throws Exception
	{
		super(NodeRingManagementI.class, owner) ;
		assert	owner != null && owner instanceof NodeRingManagementI ;
	}
	
	/***
	 * @param uri       uri of the component
	 * @param owner     owner component
	 * @throws Exception e
	 */
	public	NodeManagementInboundPort(String uri, ComponentI owner) throws Exception
	{
		super(uri, NodeRingManagementI.class, owner);

		assert	owner != null && owner instanceof NodeRingManagementI ;
	}

	@Override
	public void bindSendingDataUri(String DataInboundPortUri) throws Exception {
		final NodeRingManagementI cm = ( NodeRingManagementI ) this.owner;
		
		 this.owner.handleRequestSync(
				new ComponentI.ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						cm.bindSendingDataUri(DataInboundPortUri);
						return null;
					}
		});
	}

	@Override
	public void setNextManagementInboundPort(String managementInboundPort) throws Exception {
		final NodeRingManagementI cm = ( NodeRingManagementI ) this.owner;
		
		 this.owner.handleRequestSync(
				new ComponentI.ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						cm.setNextManagementInboundPort(managementInboundPort);
						return null;
					}
		});
	}

	@Override
	public void setPreviousManagementInboundPort(String managementInboundPort) throws Exception {
		final NodeRingManagementI cm = ( NodeRingManagementI ) this.owner;
		
		 this.owner.handleRequestSync(
				new ComponentI.ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						cm.setPreviousManagementInboundPort(managementInboundPort);
						return null;
					}
		});
	}

	@Override
	public void stopPushing() throws Exception {
		final NodeRingManagementI cm = ( NodeRingManagementI ) this.owner;
		
		 this.owner.handleRequestSync(
				new ComponentI.ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						cm.stopPushing();
						return null;
					}
		});
	}

	@Override
	public void startPushing() throws Exception {
		final NodeRingManagementI cm = ( NodeRingManagementI ) this.owner;
		
		 this.owner.handleRequestSync(
				new ComponentI.ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						cm.startPushing();
						return null;
					}
		});
	}

}
