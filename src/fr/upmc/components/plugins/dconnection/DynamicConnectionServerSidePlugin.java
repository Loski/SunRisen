package fr.upmc.components.plugins.dconnection;

import fr.upmc.components.AbstractPlugin;
import fr.upmc.components.ComponentI;
import fr.upmc.components.plugins.dconnection.interfaces.DynamicConnectionRequestI;
import fr.upmc.components.plugins.dconnection.ports.DynamicConnectionRequestInboundPort;
import fr.upmc.components.ports.PortI;

// Copyright Jacques Malenfant, Univ. Pierre et Marie Curie.
// 
// Jacques.Malenfant@lip6.fr
// 
// This software is a computer program whose purpose is to provide a
// basic component programming model to program with components
// distributed applications in the Java programming language.
// 
// This software is governed by the CeCILL-C license under French law and
// abiding by the rules of distribution of free software.  You can use,
// modify and/ or redistribute the software under the terms of the
// CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
// URL "http://www.cecill.info".
// 
// As a counterpart to the access to the source code and  rights to copy,
// modify and redistribute granted by the license, users are provided only
// with a limited warranty  and the software's author,  the holder of the
// economic rights,  and the successive licensors  have only  limited
// liability. 
// 
// In this respect, the user's attention is drawn to the risks associated
// with loading,  using,  modifying and/or developing or reproducing the
// software by the user in light of its specific status of free software,
// that may mean  that it is complicated to manipulate,  and  that  also
// therefore means  that it is reserved for developers  and  experienced
// professionals having in-depth computer knowledge. Users are therefore
// encouraged to load and test the software's suitability as regards their
// requirements in conditions enabling the security of their systems and/or 
// data to be ensured and,  more generally, to use and operate it in the 
// same conditions as regards security. 
// 
// The fact that you are presently reading this means that you have had
// knowledge of the CeCILL-C license and that you accept its terms.

/**
 * The class <code>DynamicConnectionServerSidePlugin</code> implements the
 * server side behaviour of a component dynamic interconnection pattern.
 * See the package documentation for a complete description of the pattern
 * and its implementation.
 *
 * <p><strong>Description</strong></p>
 * 
 * The class implements the required behaviours for the server side i.e.,
 * the component offering some interface through which the dynamic connection
 * need to be done. The class <code>DynamicConnectionClientSidePlugin</code>
 * implements the behaviours for the client side.
 * 
 * To use this plug-in, the user must create a subclass that implements the
 * method <code>createServerSideDynamicPort</code>, which takes an interface
 * offered by the component and return the inbound port to which the dynamic
 * connection will be done. A component can participate in dynamic connections
 * for several different offered interfaces, and therefore the method will have
 * to be able to return different types of inbound ports.
 * 
 * When two components want to use the dynamic connection plug-in, the two
 * components must first be connected through the <code>ReflectionI</code>
 * interface and then install their respective plug-ins (client side and server
 * side) and connect them. For the connection, the client side component 
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
 * <p>Created on : 2013-03-04</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @version	$Name$ -- $Revision$ -- $Date$
 */
public abstract class	DynamicConnectionServerSidePlugin
extends		AbstractPlugin
{
	private static final long serialVersionUID = 1L;

	// ------------------------------------------------------------------------
	// Plug-in internal constants and variables
	// ------------------------------------------------------------------------

	/** URI of the plug-in used in the plug-in call protocol.				*/
	public static final String		PLUGIN_URI =
											"DCONNECTION_SERVER_SIDE_PLUGIN" ;
	/** Port through which dynamic connection requests are received.		*/
	protected DynamicConnectionRequestInboundPort	dcrip ;

	// ------------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------------

	/**
	 * on the server side, create the manager for dynamic connections, including
	 * the creation of the dynamic connection request inbound port through which
	 * requests for the creation of new connections are made.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param dCRInboundPortURI		name of the server side port handling dynamic connection requests.
	 * @throws Exception
	 */
	public				DynamicConnectionServerSidePlugin() throws Exception
	{
		super() ;
	}

	// ------------------------------------------------------------------------
	// Plug-in generic methods
	// ------------------------------------------------------------------------

	/**
	 * @see fr.upmc.components.pre.plugins.PluginI#getPluginURI()
	 */
	@Override
	public String		getPluginURI()
	{
		return DynamicConnectionServerSidePlugin.PLUGIN_URI ;
	}

	/**
	 * @see fr.upmc.components.AbstractPlugin#installOn(fr.upmc.components.ComponentI)
	 */
	@Override
	public void			installOn(ComponentI owner) throws Exception
	{
		assert	owner != null ;
		assert	!owner.isInstalled(
								DynamicConnectionServerSidePlugin.PLUGIN_URI) ;

		super.installOn(owner) ;

		this.owner.addOfferedInterface(DynamicConnectionRequestI.class) ;
		this.dcrip = new DynamicConnectionRequestInboundPort(this.owner) ;
		this.addPort(this.dcrip) ;
		this.dcrip.publishPort() ;
	}

	/**
	 * @see fr.upmc.components.AbstractPlugin#uninstall()
	 */
	@Override
	public void			uninstall() throws Exception
	{
		this.owner.removeOfferedInterface(DynamicConnectionRequestI.class) ;
		this.owner.removePort(this.dcrip.getPortURI()) ;
		this.dcrip.unpublishPort() ;

		super.uninstall() ;
	}

	// ------------------------------------------------------------------------
	// Plug-in specific methods
	// ------------------------------------------------------------------------

	/**
	 * on the server side, create a new server side dynamic port, publish it
	 * using the method <code>createAndPublishDynamicPort</code> and return
	 * its unique identifier (URI).
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	offeredInterface != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	the URI of the newly created port.
	 * @throws	Exception
	 */
	public String		requestDynamicPortURI(Class<?> offeredInterface)
	throws Exception
	{
		assert	offeredInterface != null ;
		assert	this.owner.isOfferedInterface(offeredInterface) ;

		PortI p = this.createServerSideDynamicPort(offeredInterface) ;
		this.addPort(p) ;
		p.publishPort() ;

		return p.getPortURI() ;
	}

	/**
	 * on the server side, create dynamically the port to be dynamically
	 * connected given an offered interface, and therefore determine what
	 * type of port must be created for that interface.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	offeredInterface != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	the newly created port.
	 * @throws	Exception 
	 */
	protected abstract PortI	createServerSideDynamicPort(
		Class<?> offeredInterface
		) throws Exception ;
}
