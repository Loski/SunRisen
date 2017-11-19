package fr.upmc.components.pre.plugins.interfaces;

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

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;
import fr.upmc.components.pre.plugins.PluginI;

/**
 * The interface <code>ComponentPluginI</code> defines operations for plug-ins
 * on components: installing, uninstalling, testing the initialisation status.
 *
 * <p><strong>Description</strong></p>
 * 
 * The interface is defined as both offered and required to be used by outbound
 * ports of client components and inbound ports of server components (holding
 * the plug-in).
 * 
 * This interface is offered by all components. They offer it through the
 * port <code>ComponentPluginInboundPort</code>. However, a client component
 * that wants to call another component through this interface must add this
 * interface as required, create a port <code>ComponentPluginOutboundPort</code>
 * and connect to the inbound port of the other component using the connector
 * <code>ComponentPluginConnector</code>.
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	true
 * </pre>
 * 
 * <p>Created on : February 5, 2016</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @version	$Name$ -- $Revision$ -- $Date$
 */
public interface		ComponentPluginI
extends		OfferedI,
			RequiredI
{
	/**
	 * install a plugin represented by an object on the server component called
	 * upon.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	plugin != null && !this.owner.isInstalled(plugin.getPluginURI())
	 * post	this.owner.isInstalled(plugin.getPluginURI())
	 * </pre>
	 *
	 * @param plugin	the plug-in to be installed on the component.
	 * @throws Exception
	 */
	public void			install(PluginI plugin) throws Exception ;

	/**
	 * return true if the plug-in with the passed URI is initialised.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	pluginURI != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param pluginURI		URI of the plug-in to be tested.
	 * @return				true if the plug-in is installed and initialised.
	 * @throws Exception
	 */
	public boolean		isInitialised(String pluginURI) throws Exception ;

	/**
	 * unistall the plug-in with the passed URI from the component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	pluginURI != null && this.owner.isInstalled(plugin.getPluginURI())
	 * post	pluginURI != null && !this.owner.isInstalled(plugin.getPluginURI())
	 * </pre>
	 *
	 * @param pluginURI	URI of the plug-in to be uninstalled from the component.
	 * @throws Exception
	 */
	public void			uninstall(String pluginURI) throws Exception ;
}
