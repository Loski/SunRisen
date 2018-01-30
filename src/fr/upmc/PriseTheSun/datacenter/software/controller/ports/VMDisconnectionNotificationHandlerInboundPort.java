package fr.upmc.PriseTheSun.datacenter.software.controller.ports;

import fr.upmc.PriseTheSun.datacenter.software.controller.interfaces.VMDisconnectionNotificationHandlerI;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;

public class VMDisconnectionNotificationHandlerInboundPort extends AbstractInboundPort implements VMDisconnectionNotificationHandlerI{

	public	VMDisconnectionNotificationHandlerInboundPort(ComponentI owner) throws Exception
	{
		super(VMDisconnectionNotificationHandlerI.class, owner) ;
		assert	owner != null && owner instanceof VMDisconnectionNotificationHandlerI ;
	}
	
	/***
	 * @param uri       uri of the component
	 * @param owner     owner component
	 * @throws Exception e
	 */
	public	VMDisconnectionNotificationHandlerInboundPort(String uri, ComponentI owner) throws Exception
	{
		super(uri, VMDisconnectionNotificationHandlerI.class, owner);

		assert	owner != null && owner instanceof VMDisconnectionNotificationHandlerI ;
	}

	@Override
	public void receiveVMDisconnectionNotification(String vmURI) throws Exception {
		final VMDisconnectionNotificationHandlerI handler = ( VMDisconnectionNotificationHandlerI ) this.owner;
		
		 this.owner.handleRequestSync(
				new ComponentI.ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						handler.receiveVMDisconnectionNotification(vmURI);
						return null;
					}
		});
	}

	@Override
	public void disconnectController() throws Exception {
		final VMDisconnectionNotificationHandlerI handler = ( VMDisconnectionNotificationHandlerI ) this.owner;
		
		 this.owner.handleRequestSync(
				new ComponentI.ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						handler.disconnectController();
						return null;
					}
		});
	}

}
