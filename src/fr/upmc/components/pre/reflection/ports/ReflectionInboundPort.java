package fr.upmc.components.pre.reflection.ports;

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

import fr.upmc.components.ComponentI;
import fr.upmc.components.ComponentStateI;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.components.pre.plugins.PluginI;
import fr.upmc.components.pre.reflection.interfaces.ReflectionI;

/**
 * The class <code>ReflectionInboundPort</code> defines the inbound port
 * associated the interface <code>ReflectionI</code>.
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
public class			ReflectionInboundPort
extends		AbstractInboundPort
implements	ReflectionI
{
	private static final long serialVersionUID = 1L;

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	public				ReflectionInboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, ReflectionI.class, owner) ;
	}

	public				ReflectionInboundPort(
		ComponentI owner
		) throws Exception
	{
		super(ReflectionI.class, owner);
	}

	// ------------------------------------------------------------------------
	// Methods
	// ------------------------------------------------------------------------

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#installPlugin(fr.upmc.components.pre.plugins.PluginI)
	 */
	@Override
	public void			installPlugin(final PluginI plugin) throws Exception
	{
		final ComponentI comp = this.owner ;
		this.owner.handleRequestSync(
					new ComponentI.ComponentService<Void>() {
						@Override
						public Void call() throws Exception {
							comp.installPlugin(plugin) ;
							return null ;
						}
					}) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#hasInstalledPlugins()
	 */
	@Override
	public boolean		hasInstalledPlugins() throws Exception
	{
		final ComponentI comp = this.owner ;
		return this.owner.handleRequestSync(
					new ComponentI.ComponentService<Boolean>() {
						@Override
						public Boolean call() throws Exception {
							return comp.hasInstalledPlugins() ;
						}
					}) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#uninstallPlugin(java.lang.String)
	 */
	@Override
	public void			uninstallPlugin(final String pluginId) throws Exception
	{
		final ComponentI comp = this.owner ;
		this.owner.handleRequestSync(
					new ComponentI.ComponentService<Void>() {
						@Override
						public Void call() throws Exception {
							comp.uninstallPlugin(pluginId) ;
							return null;
						}
					}) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#isInstalled(java.lang.String)
	 */
	@Override
	public boolean		isInstalled(final String pluginId) throws Exception
	{
		final ComponentI comp = this.owner ;
		return this.owner.handleRequestSync(
					new ComponentI.ComponentService<Boolean>() {
						@Override
						public Boolean call() throws Exception {
							return comp.isInstalled(pluginId) ;
						}
					}) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#toggleLogging()
	 */
	@Override
	public void			toggleLogging() throws Exception
	{
		final ComponentI comp = this.owner ;
		this.owner.handleRequestSync(
					new ComponentI.ComponentService<Void>() {
						@Override
						public Void call() throws Exception {
							comp.toggleLogging() ;
							return null;
						}
					}) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#toggleTracing()
	 */
	@Override
	public void			toggleTracing() throws Exception
	{
		final ComponentI comp = this.owner ;
		this.owner.handleRequestSync(
					new ComponentI.ComponentService<Void>() {
						@Override
						public Void call() throws Exception {
							comp.toggleTracing() ;
							return null;
						}
					}) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#logMessage(java.lang.String)
	 */
	@Override
	public void			logMessage(final String message) throws Exception
	{
		final ComponentI comp = this.owner ;
		this.owner.handleRequestSync(
					new ComponentI.ComponentService<Void>() {
						@Override
						public Void call() throws Exception {
							comp.logMessage(message) ;
							return null;
						}
					}) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#isLogging()
	 */
	@Override
	public boolean		isLogging() throws Exception
	{
		final ComponentI comp = this.owner ;
		return this.owner.handleRequestSync(
					new ComponentI.ComponentService<Boolean>() {
						@Override
						public Boolean call() throws Exception {
							return comp.isLogging() ;
						}
					}) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#isTracing()
	 */
	@Override
	public boolean		isTracing() throws Exception
	{
		final ComponentI comp = this.owner ;
		return this.owner.handleRequestSync(
					new ComponentI.ComponentService<Boolean>() {
						@Override
						public Boolean call() throws Exception {
							return comp.isTracing() ;
						}
					}) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#printExecutionLog()
	 */
	@Override
	public void			printExecutionLog() throws Exception
	{
		final ComponentI comp = this.owner ;
		this.owner.handleRequestSync(
					new ComponentI.ComponentService<Void>() {
						@Override
						public Void call() throws Exception {
							comp.printExecutionLog() ;
							return null;
						}
					}) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#printExecutionLog(java.io.PrintStream)
	 */
	@Override
	public void			printExecutionLog(final PrintStream out) throws Exception
	{
		final ComponentI comp = this.owner ;
		this.owner.handleRequestSync(
					new ComponentI.ComponentService<Void>() {
						@Override
						public Void call() throws Exception {
							comp.printExecutionLog(out) ;
							return null;
						}
					}) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#printExecutionLogOnFile(java.lang.String)
	 */
	@Override
	public void			printExecutionLogOnFile(final String fileName)
	throws Exception
	{
		final ComponentI comp = this.owner ;
		this.owner.handleRequestSync(
					new ComponentI.ComponentService<Void>() {
						@Override
						public Void call() throws Exception {
							comp.printExecutionLogOnFile(fileName) ;
							return null;
						}
					}) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#isInStateAmong(fr.upmc.components.ComponentStateI[])
	 */
	@Override
	public boolean		isInStateAmong(final ComponentStateI[] states)
	throws Exception
	{
		final ComponentI comp = this.owner ;
		return this.owner.handleRequestSync(
					new ComponentI.ComponentService<Boolean>() {
						@Override
						public Boolean call() throws Exception {
							return comp.isInStateAmong(states) ;
						}
					}) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#notInStateAmong(fr.upmc.components.ComponentStateI[])
	 */
	@Override
	public boolean		notInStateAmong(final ComponentStateI[] states)
	throws Exception
	{
		final ComponentI comp = this.owner ;
		return this.owner.handleRequestSync(
					new ComponentI.ComponentService<Boolean>() {
						@Override
						public Boolean call() throws Exception {
							return comp.notInStateAmong(states) ;
						}
					}) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#isConcurrent()
	 */
	@Override
	public boolean		isConcurrent() throws Exception
	{
		final ComponentI comp = this.owner ;
		return this.owner.handleRequestSync(
					new ComponentI.ComponentService<Boolean>() {
						@Override
						public Boolean call() throws Exception {
							return comp.isConcurrent() ;
						}
					}) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#canScheduleTasks()
	 */
	@Override
	public boolean		canScheduleTasks() throws Exception
	{
		final ComponentI comp = this.owner ;
		return this.owner.handleRequestSync(
					new ComponentI.ComponentService<Boolean>() {
						@Override
						public Boolean call() throws Exception {
							return comp.canScheduleTasks() ;
						}
					}) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#getInterfaces()
	 */
	@Override
	public Class<?>[]	getInterfaces() throws Exception
	{
		final ComponentI comp = this.owner ;
		return this.owner.handleRequestSync(
					new ComponentI.ComponentService<Class<?>[]>() {
						@Override
						public Class<?>[] call() throws Exception {
							return comp.getInterfaces() ;
						}
					}) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#getInterface(java.lang.Class)
	 */
	@Override
	public Class<?>		getInterface(final Class<?> inter) throws Exception
	{
		final ComponentI comp = this.owner ;
		return this.owner.handleRequestSync(
				new ComponentI.ComponentService<Class<?>>() {
					@Override
					public Class<?> call() throws Exception {
						return comp.getInterface(inter) ;
					}
				}) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#getRequiredInterfaces()
	 */
	@Override
	public Class<?>[]	getRequiredInterfaces() throws Exception
	{
		final ComponentI comp = this.owner ;
		return this.owner.handleRequestSync(
					new ComponentI.ComponentService<Class<?>[]>() {
						@Override
						public Class<?>[] call() throws Exception {
							return comp.getRequiredInterfaces() ;
						}
					}) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#getRequiredInterface(java.lang.Class)
	 */
	@Override
	public Class<?>		getRequiredInterface(final Class<?> inter)
	throws Exception
	{
		final ComponentI comp = this.owner ;
		return this.owner.handleRequestSync(
				new ComponentI.ComponentService<Class<?>>() {
					@Override
					public Class<?> call() throws Exception {
						return comp.getRequiredInterface(inter) ;
					}
				}) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#getOfferedInterfaces()
	 */
	@Override
	public Class<?>[]	getOfferedInterfaces() throws Exception
	{
		final ComponentI comp = this.owner ;
		return this.owner.handleRequestSync(
					new ComponentI.ComponentService<Class<?>[]>() {
						@Override
						public Class<?>[] call() throws Exception {
							return comp.getOfferedInterfaces() ;
						}
					}) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#getOfferedInterface(java.lang.Class)
	 */
	@Override
	public Class<?>		getOfferedInterface(final Class<?> inter)
	throws Exception
	{
		final ComponentI comp = this.owner ;
		return this.owner.handleRequestSync(
				new ComponentI.ComponentService<Class<?>>() {
					@Override
					public Class<?> call() throws Exception {
						return comp.getOfferedInterface(inter) ;
					}
				}) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#addRequiredInterface(java.lang.Class)
	 */
	@Override
	public void			addRequiredInterface(final Class<?> inter)
	throws Exception
	{
		final ComponentI comp = this.owner ;
		this.owner.handleRequestSync(
					new ComponentI.ComponentService<Void>() {
						@Override
						public Void call() throws Exception {
							comp.addRequiredInterface(inter) ;
							return null;
						}
					}) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#removeRequiredInterface(java.lang.Class)
	 */
	@Override
	public void			removeRequiredInterface(final Class<?> inter)
	throws Exception
	{
		final ComponentI comp = this.owner ;
		this.owner.handleRequestSync(
					new ComponentI.ComponentService<Void>() {
						@Override
						public Void call() throws Exception {
							comp.removeRequiredInterface(inter) ;
							return null;
						}
					}) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#addOfferedInterface(java.lang.Class)
	 */
	@Override
	public void			addOfferedInterface(final Class<?> inter) throws Exception
	{
		final ComponentI comp = this.owner ;
		this.owner.handleRequestSync(
					new ComponentI.ComponentService<Void>() {
						@Override
						public Void call() throws Exception {
							comp.addOfferedInterface(inter) ;
							return null;
						}
					}) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#removeOfferedInterface(java.lang.Class)
	 */
	@Override
	public void			removeOfferedInterface(final Class<?> inter)
	throws Exception
	{
		final ComponentI comp = this.owner ;
		this.owner.handleRequestSync(
					new ComponentI.ComponentService<Void>() {
						@Override
						public Void call() throws Exception {
							comp.removeOfferedInterface(inter) ;
							return null;
						}
					}) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#isInterface(java.lang.Class)
	 */
	@Override
	public boolean		isInterface(final Class<?> inter) throws Exception
	{
		final ComponentI comp = this.owner ;
		return this.owner.handleRequestSync(
					new ComponentI.ComponentService<Boolean>() {
						@Override
						public Boolean call() throws Exception {
							return comp.isInterface(inter) ;
						}
					}) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#isRequiredInterface(java.lang.Class)
	 */
	@Override
	public boolean		isRequiredInterface(final Class<?> inter)
	throws Exception
	{
		final ComponentI comp = this.owner ;
		return this.owner.handleRequestSync(
					new ComponentI.ComponentService<Boolean>() {
						@Override
						public Boolean call() throws Exception {
							return comp.isRequiredInterface(inter) ;
						}
					}) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#isOfferedInterface(java.lang.Class)
	 */
	@Override
	public boolean		isOfferedInterface(final Class<?> inter) throws Exception
	{
		final ComponentI comp = this.owner ;
		return this.owner.handleRequestSync(
					new ComponentI.ComponentService<Boolean>() {
						@Override
						public Boolean call() throws Exception {
							return comp.isOfferedInterface(inter) ;
						}
					}) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#findPortURIsFromInterface(java.lang.Class)
	 */
	@Override
	public String[]		findPortURIsFromInterface(final Class<?> inter)
	throws Exception
	{
		final ComponentI comp = this.owner ;
		return this.owner.handleRequestSync(
					new ComponentI.ComponentService<String[]>() {
						@Override
						public String[] call() throws Exception {
							return comp.findPortURIsFromInterface(inter) ;
						}
					}) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#findInboundPortURIsFromInterface(java.lang.Class)
	 */
	@Override
	public String[]		findInboundPortURIsFromInterface(final Class<?> inter)
	throws Exception
	{
		final ComponentI comp = this.owner ;
		return this.owner.handleRequestSync(
					new ComponentI.ComponentService<String[]>() {
						@Override
						public String[] call() throws Exception {
							return comp.findInboundPortURIsFromInterface(inter) ;
						}
					}) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#findOutboundPortURIsFromInterface(java.lang.Class)
	 */
	@Override
	public String[]		findOutboundPortURIsFromInterface(final Class<?> inter)
	throws Exception
	{
		final ComponentI comp = this.owner ;
		return this.owner.handleRequestSync(
					new ComponentI.ComponentService<String[]>() {
						@Override
						public String[] call() throws Exception {
							return comp.findOutboundPortURIsFromInterface(inter) ;
						}
					}) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#getPortImplementedInterface(java.lang.String)
	 */
	@Override
	public Class<?>		getPortImplementedInterface(final String portURI)
	throws Exception
	{
		final ComponentI comp = this.owner ;
		return this.owner.handleRequestSync(
					new ComponentI.ComponentService<Class<?>>() {
						@Override
						public Class<?> call() throws Exception {
							return comp.getPortImplementedInterface(portURI) ;
						}
					}) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#isPortConnected(java.lang.String)
	 */
	@Override
	public boolean		isPortConnected(final String portURI)
	throws Exception
	{
		final ComponentI comp = this.owner ;
		return this.owner.handleRequestSync(
					new ComponentI.ComponentService<Boolean>() {
						@Override
						public Boolean call() throws Exception {
							return comp.isPortConnected(portURI) ;
						}
					}) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#doPortConnection(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void			doPortConnection(
		final String portURI,
		final String otherPortURI,
		final String ccname
		) throws Exception
	{
		final ComponentI comp = this.owner ;
		comp.handleRequestSync(
				new ComponentI.ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						comp.doPortConnection(portURI, otherPortURI, ccname) ;
						return null ;
					}
				}) ;
	}

	/**
	 * @see fr.upmc.components.pre.reflection.interfaces.ReflectionI#doPortDisconnection(java.lang.String)
	 */
	@Override
	public void			doPortDisconnection(final String portURI)
	throws Exception
	{
		final ComponentI comp = this.owner ;
		comp.handleRequestSync(
				new ComponentI.ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						comp.doPortDisconnection(portURI) ;
						return null ;
					}
				}) ;
	}
}
