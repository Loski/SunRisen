package fr.upmc.PriseTheSun.datacenter.software.controller.ports;

import fr.upmc.PriseTheSun.datacenter.software.controller.interfaces.ControllerManagementI;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
/**
* The class <code>ControllerManagementOutboundPort</code> implements the
 * inbound port through which the component management methods are called.
* 
* 
* @author	Maxime LAVASTE Lo�c Lafontaine
*/
public class ControllerManagementOutboundPort extends AbstractOutboundPort
implements	ControllerManagementI{

    /***
     * 
     * @param owner       owner component
     * @throws Exception e
     */
	public	ControllerManagementOutboundPort(ComponentI owner) throws Exception
	{
		super(ControllerManagementI.class, owner) ;
		assert	owner != null ;
	}

	/***
	 *  
	 * @param uri             uri of the component
	 * @param owner           owner component
	 * @throws Exception e
	 */
	public	ControllerManagementOutboundPort(String uri, ComponentI owner) throws Exception
	{
		super(uri, ControllerManagementI.class, owner);
		assert	owner != null;
	}

	@Override
	public void bindSendingDataUri(String DataInboundPortUri) throws Exception {
		((ControllerManagementI)this.connector).bindSendingDataUri(DataInboundPortUri);
	}

}
