package fr.upmc.components.pre.reflection.connectors;

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

import java.io.PrintStream;

import fr.upmc.components.ComponentStateI;
import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.components.pre.plugins.PluginI;
import fr.upmc.components.pre.reflection.interfaces.ReflectionI;

/**
 * The class <code>ReflectionConnector</code> defines the connector associated
 * the interface <code>ReflectionI</code>.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	true
 * </pre>
 * 
 * <p>Created on : February 25, 2016</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @version	$Name$ -- $Revision$ -- $Date$
 */
public class			ReflectionConnector
extends		AbstractConnector
implements	ReflectionI
{
	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	public				 ReflectionConnector()
	{
		super() ;
	}

	// ------------------------------------------------------------------------
	// Methods
	// ------------------------------------------------------------------------

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#installPlugin(fr.upmc.components.pre.plugins.PluginI)
	 */
	@Override
	public void			installPlugin(PluginI plugin) throws Exception
	{
		((ReflectionI)this.offering).installPlugin(plugin) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#hasInstalledPlugins()
	 */
	@Override
	public boolean		hasInstalledPlugins() throws Exception
	{
		return ((ReflectionI)this.offering).hasInstalledPlugins() ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#uninstallPlugin(java.lang.String)
	 */
	@Override
	public void			uninstallPlugin(String pluginId) throws Exception
	{
		((ReflectionI)this.offering).uninstallPlugin(pluginId) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#isInstalled(java.lang.String)
	 */
	@Override
	public boolean		isInstalled(String pluginId) throws Exception
	{
		return ((ReflectionI)this.offering).isInstalled(pluginId) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#toggleLogging()
	 */
	@Override
	public void			toggleLogging() throws Exception
	{
		((ReflectionI)this.offering).toggleLogging() ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#toggleTracing()
	 */
	@Override
	public void			toggleTracing() throws Exception
	{
		((ReflectionI)this.offering).toggleTracing() ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#logMessage(java.lang.String)
	 */
	@Override
	public void			logMessage(String message) throws Exception
	{
		((ReflectionI)this.offering).logMessage(message) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#isLogging()
	 */
	@Override
	public boolean		isLogging() throws Exception
	{
		return ((ReflectionI)this.offering).isLogging() ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#isTracing()
	 */
	@Override
	public boolean		isTracing() throws Exception
	{
		return ((ReflectionI)this.offering).isTracing() ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#printExecutionLog()
	 */
	@Override
	public void			printExecutionLog() throws Exception
	{
		((ReflectionI)this.offering).printExecutionLog() ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#printExecutionLog(java.io.PrintStream)
	 */
	@Override
	public void			printExecutionLog(PrintStream out) throws Exception
	{
		((ReflectionI)this.offering).printExecutionLog(out) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#printExecutionLogOnFile(java.lang.String)
	 */
	@Override
	public void			printExecutionLogOnFile(String fileName)
	throws Exception
	{
		((ReflectionI)this.offering).printExecutionLogOnFile(fileName) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#isInStateAmong(fr.upmc.components.ComponentStateI[])
	 */
	@Override
	public boolean		isInStateAmong(ComponentStateI[] states)
	throws Exception
	{
		return ((ReflectionI)this.offering).isInStateAmong(states) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#notInStateAmong(fr.upmc.components.ComponentStateI[])
	 */
	@Override
	public boolean		notInStateAmong(ComponentStateI[] states)
	throws Exception
	{
		return ((ReflectionI)this.offering).notInStateAmong(states) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#isConcurrent()
	 */
	@Override
	public boolean		isConcurrent() throws Exception
	{
		return ((ReflectionI)this.offering).isConcurrent() ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#canScheduleTasks()
	 */
	@Override
	public boolean		canScheduleTasks() throws Exception
	{
		return ((ReflectionI)this.offering).canScheduleTasks() ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#getInterfaces()
	 */
	@Override
	public Class<?>[]	getInterfaces() throws Exception
	{
		return ((ReflectionI)this.offering).getInterfaces() ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#getInterface(java.lang.Class)
	 */
	@Override
	public Class<?>		getInterface(Class<?> inter) throws Exception
	{
		return ((ReflectionI)this.offering).getInterface(inter) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#getRequiredInterfaces()
	 */
	@Override
	public Class<?>[]	getRequiredInterfaces() throws Exception
	{
		return ((ReflectionI)this.offering).getRequiredInterfaces() ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#getRequiredInterface(java.lang.Class)
	 */
	@Override
	public Class<?>		getRequiredInterface(Class<?> inter) throws Exception
	{
		return ((ReflectionI)this.offering).getRequiredInterface(inter) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#getOfferedInterfaces()
	 */
	@Override
	public Class<?>[]	getOfferedInterfaces() throws Exception
	{
		return ((ReflectionI)this.offering).getOfferedInterfaces() ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#getOfferedInterface(java.lang.Class)
	 */
	@Override
	public Class<?>		getOfferedInterface(Class<?> inter) throws Exception
	{
		return ((ReflectionI)this.offering).getOfferedInterface(inter) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#addRequiredInterface(java.lang.Class)
	 */
	@Override
	public void			addRequiredInterface(Class<?> inter)
	throws Exception
	{
		((ReflectionI)this.offering).addRequiredInterface(inter) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#removeRequiredInterface(java.lang.Class)
	 */
	@Override
	public void			removeRequiredInterface(Class<?> inter)
	throws Exception
	{
		((ReflectionI)this.offering).removeRequiredInterface(inter) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#addOfferedInterface(java.lang.Class)
	 */
	@Override
	public void			addOfferedInterface(Class<?> inter) throws Exception
	{
		((ReflectionI)this.offering).addOfferedInterface(inter) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#removeOfferedInterface(java.lang.Class)
	 */
	@Override
	public void			removeOfferedInterface(Class<?> inter)
	throws Exception
	{
		((ReflectionI)this.offering).removeOfferedInterface(inter) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#isInterface(java.lang.Class)
	 */
	@Override
	public boolean		isInterface(Class<?> inter) throws Exception
	{
		return ((ReflectionI)this.offering).isInterface(inter) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#isRequiredInterface(java.lang.Class)
	 */
	@Override
	public boolean		isRequiredInterface(Class<?> inter)
	throws Exception
	{
		return ((ReflectionI)this.offering).isRequiredInterface(inter) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#isOfferedInterface(java.lang.Class)
	 */
	@Override
	public boolean		isOfferedInterface(Class<?> inter) throws Exception
	{
		return ((ReflectionI)this.offering).isOfferedInterface(inter) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#findPortURIsFromInterface(java.lang.Class)
	 */
	@Override
	public String[]		findPortURIsFromInterface(Class<?> inter)
	throws Exception
	{
		return ((ReflectionI)this.offering).findPortURIsFromInterface(inter) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#findInboundPortURIsFromInterface(java.lang.Class)
	 */
	@Override
	public String[]		findInboundPortURIsFromInterface(Class<?> inter)
	throws Exception
	{
		return ((ReflectionI)this.offering).findInboundPortURIsFromInterface(inter) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#findOutboundPortURIsFromInterface(java.lang.Class)
	 */
	@Override
	public String[]		findOutboundPortURIsFromInterface(Class<?> inter)
	throws Exception
	{
		return ((ReflectionI)this.offering).findOutboundPortURIsFromInterface(inter) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#getPortImplementedInterface(java.lang.String)
	 */
	@Override
	public Class<?>		getPortImplementedInterface(String portURI)
	throws Exception
	{
		return ((ReflectionI)this.offering).
										getPortImplementedInterface(portURI) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#isPortConnected(java.lang.String)
	 */
	@Override
	public boolean		isPortConnected(String portURI)
	throws Exception
	{
		return ((ReflectionI)this.offering).isPortConnected(portURI) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#doPortConnection(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void			doPortConnection(
		String portURI,
		String otherPortURI,
		String ccname
		) throws Exception
	{
		((ReflectionI)this.offering).
							doPortConnection(portURI, otherPortURI, ccname) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#doPortDisconnection(java.lang.String)
	 */
	@Override
	public void			doPortDisconnection(String portURI)
	throws Exception
	{
		((ReflectionI)this.offering).doPortDisconnection(portURI) ;
	}
}
