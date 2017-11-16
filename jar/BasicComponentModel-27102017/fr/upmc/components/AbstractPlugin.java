package fr.upmc.components;

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

import fr.upmc.components.ports.PortI;
import fr.upmc.components.pre.plugins.PluginI;
import fr.upmc.components.pre.plugins.connectors.ComponentPluginConnector;
import fr.upmc.components.pre.plugins.interfaces.ComponentPluginI;
import fr.upmc.components.pre.plugins.ports.ComponentPluginOutboundPort;

/**
 * The abstract class <code>AbstractPlugin</code> defines the most generic
 * methods and data for component plug-ins.
 *
 * <p><strong>Description</strong></p>
 * 
 * Plug-ins are objects designed to extend the functionalities of components.
 * A plug-in has an URI and a component can only have one plug-in object of
 * a given URI. Plug-in objects are created from their class and installed on
 * a component using components services implemented by all components. Each
 * component offers the interface <code>ComponentPluginI</code> and has a
 * <code>ComponentPluginInboundPort</code> automatically added art creation
 * time.
 * 
 * <code>AbstractPlugin</code> is placed in the same package as
 * <code>AbstractComponent</code> to provide it with an access to a package
 * visibility method <code>doAddPort</code> allowing to add a port to the
 * plug-in owner component without resorting to a public method to do so.
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	true
 * </pre>
 * 
 * <p>Created on : February 3, 2016</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @version	$Name$ -- $Revision$ -- $Date$
 */
public abstract class	AbstractPlugin
implements	PluginI
{
	/**
	 * The static class <code>Fake</code> implements a fake component used to
	 * call the services of the component on which the plug-in is to be
	 * installed or uninstalled.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * <p><strong>Invariant</strong></p>
	 * 
	 * <pre>
	 * invariant	true
	 * </pre>
	 * 
	 * <p>Created on : January 10, 2017</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 * @version	$Name$ -- $Revision$ -- $Date$
	 */
	protected static class	Fake
	extends		AbstractComponent
	{
		/** the outbound port used to call plug-in management services of the
		 * other component.													*/
		protected ComponentPluginOutboundPort	cpObp ;

		/**
		 * create a fake component with a component plug-in outbound port
		 * to be connected to the plug-in component owner.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	true			// no precondition.
		 * post	true			// no postcondition.
		 * </pre>
		 *
		 * @throws Exception
		 */
		public			Fake() throws Exception
		{
			super(0, 0) ;

			this.addRequiredInterface(ComponentPluginI.class) ;
			this.cpObp = new ComponentPluginOutboundPort(this) ;
			this.addPort(this.cpObp) ;
			this.cpObp.publishPort() ;
		}

		/**
		 * install a plug-in on the component designated by the URI of its
		 * plug-in inbound port URI.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	plugin != null && pluginInboundPortURI != null
		 * post	true			// no postcondition.
		 * </pre>
		 *
		 * @param plugin	plug-in to be installed.
		 * @param pluginInboundPortURI	URI of the plug-in inbound port of the owner component.
		 * @throws Exception
		 */
		public void		doInstallPluginOn(
			PluginI plugin,
			String pluginInboundPortURI
			) throws Exception
		{
			assert	plugin != null && pluginInboundPortURI != null ;

			this.cpObp.doConnection(
							pluginInboundPortURI,
							ComponentPluginConnector.class.getCanonicalName()) ;
			this.cpObp.install(plugin) ;
			this.cpObp.doDisconnection() ;
			this.removePort(this.cpObp) ;
			this.cpObp.unpublishPort() ;
			this.removeRequiredInterface(ComponentPluginI.class) ;
		}

		/**
		 * uninstall a plug-in on the owner component designated by the URI of
		 * its plug-in inbound port URI.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	pluginInboundPortURI != null && pluginURI != null
		 * post	true			// no postcondition.
		 * </pre>
		 *
		 * @param pluginInboundPortURI	URI of the plug-in inbound port of the owner component.
		 * @param pluginURI				URI of the plug-in to be uninstalled.
		 * @throws Exception
		 */
		public void		doUnistallPluginFrom(
			String pluginInboundPortURI,
			String pluginURI
			) throws Exception
		{
			this.cpObp.doConnection(
							pluginInboundPortURI,
							ComponentPluginConnector.class.getCanonicalName()) ;
			this.cpObp.uninstall(pluginURI) ;
			this.cpObp.doDisconnection() ;
			this.removePort(this.cpObp) ;
			this.cpObp.unpublishPort() ;
			this.removeRequiredInterface(ComponentPluginI.class) ;
		}
	}

	// --------------------------------------------------------------------
	// Plug-in static services
	// --------------------------------------------------------------------

	private static final long	serialVersionUID = 1L;
	/** component holding this plug-in										*/
	protected ComponentI		owner ;

	/**
	 * install a plug-in on a component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	pluginInboundPortURI != null && plugin != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param pluginInboundPortURI	URI of the plug-in management inbound port of the component.
	 * @param plugin				plug-in to be installed.
	 * @throws Exception
	 */
	public static void	installPluginOn(
		final String pluginInboundPortURI,
		final PluginI plugin
		) throws Exception
	{
		assert	pluginInboundPortURI != null && plugin != null ;

		final Fake fake = new Fake() {} ;
		fake.runTask(new ComponentI.ComponentTask() {
						@Override
						public void run() {
							try {
								fake.doInstallPluginOn(
												plugin, pluginInboundPortURI) ;
							} catch (Exception e) {
								throw new RuntimeException(e);
							}
						}
					 }) ;		
	}

	/**
	 * uninstall a plug-in from a component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	pluginInboundPortURI != null && pluginURI != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param pluginInboundPortURI	URI of the plug-in management inbound port of the component.
	 * @param pluginURI				URI of the plug-in.
	 * @throws Exception
	 */
	public static void	uninstallPluginFrom(
		final String pluginInboundPortURI,
		final String pluginURI
		) throws Exception
	{
		assert	pluginInboundPortURI != null && pluginURI != null ;

		final Fake fake = new Fake() {} ;
		fake.runTask(new ComponentI.ComponentTask() {
						@Override
						public void run() {
							try {
								fake.doUnistallPluginFrom(
											pluginInboundPortURI, pluginURI) ;
							} catch (Exception e) {
								throw new RuntimeException(e) ;
							}
						}
					 }) ;
	}

	// --------------------------------------------------------------------
	// Plug-in base constructor
	// --------------------------------------------------------------------

	public				AbstractPlugin()
	{
		super() ;
	}

	// --------------------------------------------------------------------
	// Plug-in base services
	// --------------------------------------------------------------------
	
	/**
	 * @see fr.upmc.components.pre.plugins.PluginI#installOn(fr.upmc.components.ComponentI)
	 */
	@Override
	public void			installOn(ComponentI owner) throws Exception
	{
		assert	owner != null && !owner.isInstalled(this.getPluginURI()) ;

		this.owner = owner ;
	}

	/**
	 * @see fr.upmc.components.pre.plugins.PluginI#isInitialised()
	 */
	@Override
	public boolean		isInitialised() throws Exception
	{
		return this.owner != null ;
	}

	/**
	 * @see fr.upmc.components.pre.plugins.PluginI#uninstall()
	 */
	@Override
	public void			uninstall() throws Exception
	{
		this.owner = null ;
	}

	// --------------------------------------------------------------------
	// Plug-in methods linking it to the base services of components
	// --------------------------------------------------------------------

	/**
	 * add a port to the owner component, a method used in plug-in objects to
	 * access their owner component in a way other objects can't.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	p != null ;
	 * post	this.owner.isPortExisting(p.getPortURI())
	 * </pre>
	 *
	 * @param p port to be added.
	 * @throws Exception
	 */
	protected void		addPort(PortI p) throws Exception
	{
		assert	p != null ;

		((AbstractComponent) this.owner).doAddPort(p) ;

		assert	this.owner.isPortExisting(p.getPortURI()) ;
	}

	/**
	 * log a message using the owner component logging facilities.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param message	string to be logged.
	 */
	protected void		logMessage(String message)
	{
		this.owner.logMessage(message) ;
	}
}
