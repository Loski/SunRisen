package fr.upmc.PriseTheSun.datacenter.software.controller.ports;

import fr.upmc.PriseTheSun.datacenter.software.controller.Controller;
import fr.upmc.PriseTheSun.datacenter.software.controller.interfaces.ControllerRingManagementI;
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
public class ControllerManagementInboundPort extends AbstractInboundPort implements ControllerRingManagementI{
	private static final long serialVersionUID = 1L;

	/***
	 * 
	 * @param owner     owner component
	 * @throws Exception e
	 */
	public	ControllerManagementInboundPort(ComponentI owner) throws Exception
	{
		super(ControllerRingManagementI.class, owner) ;
		assert	owner != null && owner instanceof Controller ;
	}
	
	/***
	 * @param uri       uri of the component
	 * @param owner     owner component
	 * @throws Exception e
	 */
	public	ControllerManagementInboundPort(String uri, ComponentI owner) throws Exception
	{
		super(uri, ControllerRingManagementI.class, owner);

		assert	owner != null && owner instanceof Controller ;
	}

	@Override
	public void bindSendingDataUri(String DataInboundPortUri) throws Exception {
		final ControllerRingManagementI cm = ( ControllerRingManagementI ) this.owner;
		
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
	public void informNextManagementInboundPort(String managementInboundPort) throws Exception {
		final ControllerRingManagementI cm = ( ControllerRingManagementI ) this.owner;
		
		 this.owner.handleRequestSync(
				new ComponentI.ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						cm.informNextManagementInboundPort(managementInboundPort);
						return null;
					}
		});
	}

	@Override
	public void informPreviousManagementInboundPOrt(String managementInboundPort) throws Exception {
		final ControllerRingManagementI cm = ( ControllerRingManagementI ) this.owner;
		
		 this.owner.handleRequestSync(
				new ComponentI.ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						cm.informPreviousManagementInboundPOrt(managementInboundPort);
						return null;
					}
		});
	}

}
