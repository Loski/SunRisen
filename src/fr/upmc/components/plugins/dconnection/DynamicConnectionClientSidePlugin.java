package fr.upmc.components.plugins.dconnection;

//Copyright Jacques Malenfant, Univ. Pierre et Marie Curie.
//
//Jacques.Malenfant@lip6.fr
//
//This software is a computer program whose purpose is to provide a
//basic component programming model to program with components
//distributed applications in the Java programming language.
//
//This software is governed by the CeCILL-C license under French law and
//abiding by the rules of distribution of free software.  You can use,
//modify and/ or redistribute the software under the terms of the
//CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
//URL "http://www.cecill.info".
//
//As a counterpart to the access to the source code and  rights to copy,
//modify and redistribute granted by the license, users are provided only
//with a limited warranty  and the software's author,  the holder of the
//economic rights,  and the successive licensors  have only  limited
//liability. 
//
//In this respect, the user's attention is drawn to the risks associated
//with loading,  using,  modifying and/or developing or reproducing the
//software by the user in light of its specific status of free software,
//that may mean  that it is complicated to manipulate,  and  that  also
//therefore means  that it is reserved for developers  and  experienced
//professionals having in-depth computer knowledge. Users are therefore
//encouraged to load and test the software's suitability as regards their
//requirements in conditions enabling the security of their systems and/or 
//data to be ensured and,  more generally, to use and operate it in the 
//same conditions as regards security. 
//
//The fact that you are presently reading this means that you have had
//knowledge of the CeCILL-C license and that you accept its terms.

import fr.upmc.components.AbstractPlugin;
import fr.upmc.components.ComponentI;
import fr.upmc.components.plugins.dconnection.interfaces.DynamicConnectionRequestI;
import fr.upmc.components.plugins.dconnection.connectors.DynamicConnectionRequestConnector;
import fr.upmc.components.plugins.dconnection.interfaces.DynamicConnectionDescriptorI;
import fr.upmc.components.plugins.dconnection.ports.DynamicConnectionRequestOutboundPort;
import fr.upmc.components.ports.PortI;
import fr.upmc.components.pre.reflection.ports.ReflectionOutboundPort;

/**
 * The class <code>DynamicConnectionClientSidePlugin</code> implements the
 * client side behaviour of a component dynamic connection pattern.
 * See the package documentation for a complete description of the pattern
 * and its implementation.
 *
 * <p><strong>Description</strong></p>
 * 
 * The class implements the required behaviours for the client side i.e.,
 * the component requiring some interface through which the dynamic connection
 * need to be done. The class <code>DynamicConnectionServerSidePlugin</code>
 * implements the behaviours for the server side.
 * 
 * To use this plug-in, the user must create a class that implements the
 * interface <code>DynamicConnectionDescriptorI</code>, which defines the type
 * of outbound port to be created and the connector class name for the
 * connection itself. A component can participate in dynamic connections
 * for several different offered interfaces, and therefore several such
 * classes may have to be created to be able to create different types of
 * outbound ports and use different types of connectors.
 * 
 * When two components want to use the dynamic connection plug-in, the two
 * components must first be connected through the <code>ReflectionI</code>
 * interface and then install their respective plug-ins (client side and server
 * side) and connect them through the client side plug-in.
 * 
 * For the dynamic connection, the client side component calls its plug-in
 * method <code>doDynamicConnection</code>.
 * 
 * This plug-in is a singleton one, so it can only be installed once at a
 * time on a component.
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	true
 * </pre>
 * 
 * <p>Created on : 2017-02-15</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @version	$Name$ -- $Revision$ -- $Date$
 */
public class 			DynamicConnectionClientSidePlugin
extends		AbstractPlugin
{
	private static final long serialVersionUID = 1L;

	// ------------------------------------------------------------------------
	// Plug-in internal constants and variables
	// ------------------------------------------------------------------------

	/** URI of the plug-in used in the plug-in call protocol.				*/
	public static final String		PLUGIN_URI =
											"DCONNECTION_CLIENT_SIDE_PLUGIN" ;
	/** Port through which dynamic connection requests are sent.			*/
	protected DynamicConnectionRequestOutboundPort	dcrop ;

	// ------------------------------------------------------------------------
	// Plug-in generic methods
	// ------------------------------------------------------------------------

	/**
	 * @see fr.upmc.components.pre.plugins.PluginI#getPluginURI()
	 */
	@Override
	public String		getPluginURI()
	{
		return DynamicConnectionClientSidePlugin.PLUGIN_URI ;
	}

	/**
	 * @see fr.upmc.components.AbstractPlugin#installOn(fr.upmc.components.ComponentI)
	 */
	@Override
	public void			installOn(ComponentI owner) throws Exception
	{
		assert	owner != null ;
		assert	!owner.isInstalled(
								DynamicConnectionClientSidePlugin.PLUGIN_URI) ;

		super.installOn(owner) ;

		this.owner.addRequiredInterface(DynamicConnectionRequestI.class) ;
	}

	/**
	 * @see fr.upmc.components.AbstractPlugin#uninstall()
	 */
	@Override
	public void			uninstall() throws Exception
	{
		super.uninstall() ;
	}

	// ------------------------------------------------------------------------
	// Plug-in specific methods
	// ------------------------------------------------------------------------

	/**
	 * true if the plug-in is connected to a server side plug-in.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	true if the plug-in is connected to a server side plug-in.
	 * @throws Exception
	 */
	public boolean		isConnectedToServerSide() throws Exception
	{
		return this.dcrop != null && this.dcrop.connected() ;
	}

	/**
	 * connect to the dynamic connection request port of the server side.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	!this.isConnectedToServerSide()
	 * post	this.isConnectedToServerSide()
	 * </pre>
	 *
	 *@param rop	reflection outbound port connected to the server side.
	 * @throws	Exception
	 */
	public void			connectWithServerSide(
		ReflectionOutboundPort rop
		) throws Exception
	{
		assert	!this.isConnectedToServerSide() ;

		// Connect to the other component using its dynamic connection request
		// inbound port.
		this.dcrop = new DynamicConnectionRequestOutboundPort(this.owner) ;
		this.addPort(this.dcrop) ;
		this.dcrop.publishPort() ;

		String[] otherInboundPortURI =
			rop.findInboundPortURIsFromInterface(
									DynamicConnectionRequestI.class) ;

		dcrop.doConnection(
			otherInboundPortURI[0],
			DynamicConnectionRequestConnector.class.getCanonicalName()) ;

		assert	this.isConnectedToServerSide() ;
	}

	/**
	 * connect dynamically : (1) request the URI of the server dynamic port,
	 * (2) create this client own port, (3) connect the client to the server.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	offeredInterface != null
	 * pre	connectionDescriptor != null
	 * pre	this.isConnectedToServerSide()
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param offeredInterface		interface through which the connection is made.
	 * @param connectionDescriptor	describes how to create the outbound port and the connector.
	 * @return						the URI of the outbound port of the connection.
	 * @throws Exception
	 */
	public String		doDynamicConnection(
		Class<?> offeredInterface,
		DynamicConnectionDescriptorI connectionDescriptor
		) throws Exception
	{
		assert	offeredInterface != null && connectionDescriptor != null ;
		assert	this.isConnectedToServerSide() ;

		String otherDynamicPortURI =
								this.dcrop.requestDynamicPortURI(offeredInterface) ;
		PortI dynamicPort = connectionDescriptor.createClientSideDynamicPort() ;
		this.addPort(dynamicPort) ;
		dynamicPort.publishPort() ;
		dynamicPort.doConnection(
							otherDynamicPortURI,
							connectionDescriptor.dynamicConnectorClassName()) ;

		return dynamicPort.getPortURI() ;
	}

	/**
	 * connect from the dynamic connection request port of the server side.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.isConnectedToServerSide()
	 * post	!this.isConnectedToServerSide()
	 * </pre>
	 *
	 * @throws Exception
	 */
	public void		disconnectFromServerSide() throws Exception
	{
		assert	this.isConnectedToServerSide() ;

		this.dcrop.doDisconnection() ;
		this.owner.removePort(this.dcrop.getPortURI()) ;
		this.dcrop.unpublishPort() ;

		assert	!this.isConnectedToServerSide() ;
	}
}
