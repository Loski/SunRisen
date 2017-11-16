package fr.upmc.components;

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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.jcip.annotations.GuardedBy;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.components.exceptions.ComponentStartException;
import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;
import fr.upmc.components.ports.AbstractPort;
import fr.upmc.components.ports.InboundPortI;
import fr.upmc.components.ports.OutboundPortI;
import fr.upmc.components.ports.PortI;
import fr.upmc.components.pre.plugins.PluginI;
import fr.upmc.components.pre.plugins.interfaces.ComponentPluginI;
import fr.upmc.components.pre.plugins.ports.ComponentPluginInboundPort;
import fr.upmc.components.pre.reflection.interfaces.ReflectionI;
import fr.upmc.components.pre.reflection.ports.ReflectionInboundPort;

/**
 * The class <code>AbstractComponent</code> represents the basic information
 * and methods for components in the component model.
 *
 * <p><strong>Description</strong></p>
 * 
 * A component provides methods to query it to known its implemented interfaces
 * and the ports through which one can connect to it.  Components can be
 * passive or active.  Passive components do not have their own thread, so
 * any call the serve must use the thread of the caller.  It handles requests
 * by simply calling the task in the thread that is executing the method
 * <code>handleRequest</code>.
 * 
 * In the implementation of sequential components, programmers have the choice
 * between using tasks and this method, or to directly use the methods
 * implementing the component services.  Inbound ports, implementing the
 * offered interfaces of the component and  which initially receive the calls
 * from the client components can therefore either create a task and call
 * <code>handleRequest</code> or call directly the appropriate method that
 * implement the service.
 * 
 * Indeed, methods that implement the services need not have the same signature
 * as exposed in offered interfaces.  Being able to distinguish can be
 * interesting when a component offers the same interface through several
 * ports.  It can then have different implementations of the service depending
 * on the port through which it is called.
 * 
 * Active, or concurrent, components have their own threads, which are managed
 * through the Java Executor framework that implements the concurrent servicing
 * of requests.  At creation time, components may be given 0, 1 or more threads
 * as well as 0, 1 or more schedulable threads.  Schedulable threads are useful
 * when some service or task must be executed at some specific time.
 * 
 * Concurrent execution can be used to service requests coming from client
 * components or to execute some task required by the component itself or
 * by some other component.  Requests create <code>Callable</code> that are
 * passed to the executor by the methods <code>handleRequest</code> or the
 * methods <code>handleRequest</code> if they need to be performed at a specific
 * time.  The implementation of these method forces calls to be synchronous by
 * getting the value of the future returned by the call to the executor.  Tasks 
 * are executed as fire-and-forget and passed to the executor service by the
 * methods <code>run</code> and <code>scheduleTask</code>.  To get reliable
 * behaviours, concurrent components should execute all code within requests
 * and tasks run through the executor service.
 * 
 * <pre>
 * TODO: Maybe change the <code>handleRequest</code> protocol to return a
 *       <code>Future</code> to let the caller decide when to synchronise with
 *       the provider and wait for its answer?  But what about distributed
 *       futures?
 * </pre>
 * 
 * As it relies on the Executor framework, the concurrent component implements
 * part of the <code>ExecutorService</code> interface regarding the life
 * cycle management that is simply forwarded to the executor.  Subclasses
 * should redefine these methods especially when they implement composite
 * components with concurrent subcomponents.
 * 
 * <pre>
 * TODO: Still needs more work and thinking about the life cycle implementation
 *       and in particular the shutting down of components and the interaction
 *       with reflective features.
 * </pre>
 * 
 * <p><strong>Usage</strong></p>
 * 
 * This class is meant to be extended by any class implementing a kind of
 * components in the application.  Constructors and methods should be used only
 * in the code of the component so to hide technicalities of the implementation
 * from the component users.  The proper vision of the component model is to
 * consider the code in this package, and therefore in this class, as a virtual
 * machine to implement components rather that application code.
 * 
 * Components are indeed implemented as objects but calling from the outside of
 * this objects methods they define directly is something that should be done
 * only in virtual machine code and not in component code, essentially in
 * classes derived from AbstractCVM. The call should also use only methods
 * defined within this abstract class and not methods defined as services in
 * user components that must be called through the Executor framework.
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	requiredInterfaces != null
 * invariant	offeredInterfaces != null
 * invariant	interfaces2ports != null
 * invariant	forall(Class<?> inter : interfaces2ports.keys()) { requiredInterfaces.contains(inter) || offeredInterfaces.contains(inter) }
 * </pre>
 * 
 * <p>Created on : 2012-11-06</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @version	$Name$ -- $Revision$ -- $Date$
 */
public abstract class	AbstractComponent
implements	ComponentI
{
	// ------------------------------------------------------------------------
	// Internal information about inner components and component lifecycle
	// management.
	// ------------------------------------------------------------------------

	/** current state in the component life-cycle.							*/
	protected ComponentState				state ;
	/** inner components owned by this component.							*/
	protected final Vector<ComponentI>	innerComponents ;

	// ------------------------------------------------------------------------
	// Internal concurrency management
	// ------------------------------------------------------------------------

	/** true if the component executes concurrently.						*/
	protected boolean					isConcurrent ;
	/** true if the component can schedule tasks.							*/
	protected boolean					canScheduleTasks ;

	/** the executor service in charge of handling component requests.		*/
	protected ExecutorService			requestHandler ;
	/** number of threads in the <code>ExecutorService</code>.				*/
	protected int						nbThreads ;
	/** the executor service in charge of handling scheduled tasks.			*/
	protected ScheduledExecutorService	scheduledTasksHandler ;
	/** number of threads in the <code>ScheduledExecutorService</code>.		*/
	protected int						nbSchedulableThreads ;

	// ------------------------------------------------------------------------
	// Plug-ins facilities
	// ------------------------------------------------------------------------

	protected Map<String,PluginI>		installedPlugins ;
	protected ComponentPluginInboundPort	componentPluginInboundPort ;

	/**
	 * configure the plug-in facilities for this component, adding the offered
	 * interface, the inbound port and publish it.
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
	protected void		configurePluginFacilities() throws Exception
	{
		this.installedPlugins = new HashMap<String,PluginI>() ;
		this.addOfferedInterface(ComponentPluginI.class) ;
		this.componentPluginInboundPort = new ComponentPluginInboundPort(this) ;
		this.addPort(this.componentPluginInboundPort) ;
		this.componentPluginInboundPort.publishPort() ;
	}

	/**
	 * unconfigure the plug-in facilities for this component, removing the
	 * offered interface, the inbound port and unpublish it.
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
	protected void		unConfigurePluginFacilitites() throws Exception
	{
		for (Entry<String,PluginI> e : this.installedPlugins.entrySet()) {
			((PluginI)e.getValue()).uninstall() ;
		}
		this.componentPluginInboundPort.unpublishPort() ;
		this.removePort(this.componentPluginInboundPort) ;
		this.removeOfferedInterface(ComponentPluginI.class) ;
	}

	/**
	 * @see fr.upmc.components.ComponentI#installPlugin(fr.upmc.components.pre.plugins.PluginI)
	 */
	@Override
	public void			installPlugin(
		PluginI plugin
		) throws Exception
	{
		assert	!this.isInstalled(plugin.getPluginURI()) ;

		((AbstractPlugin)plugin).installOn(this) ;
		this.installedPlugins.put(plugin.getPluginURI(), plugin) ;

		assert	this.isInstalled(plugin.getPluginURI()) ;
	}

	/**
	 * @see fr.upmc.components.ComponentI#hasInstalledPlugins()
	 */
	@Override
	public boolean		hasInstalledPlugins()
	{
		return this.installedPlugins != null &&
											!this.installedPlugins.isEmpty() ;
	}

	/**
	 * @see fr.upmc.components.ComponentI#isInstalled(java.lang.String)
	 */
	@Override
	public boolean		isInstalled(String pluginURI)
	{
		return this.installedPlugins != null &&
								this.installedPlugins.containsKey(pluginURI) ;
	}

	/**
	 * @see fr.upmc.components.ComponentI#uninstallPlugin(java.lang.String)
	 */
	@Override
	public void			uninstallPlugin(String pluginURI) throws Exception
	{
		assert	this.isInstalled(pluginURI) ;

		PluginI temp = this.installedPlugins.remove(pluginURI) ;
		temp.uninstall() ;
	}

	/**
	 * @see fr.upmc.components.ComponentI#getPlugin(java.lang.String)
	 */
	@Override
	public PluginI		getPlugin(String pluginURI)
	{
		assert	this.installedPlugins != null ;
		assert	this.installedPlugins.containsKey(pluginURI) ;

		return this.installedPlugins.get(pluginURI) ;
	}

	// ------------------------------------------------------------------------
	// Logging facilities
	// ------------------------------------------------------------------------

	// TODO: define the logging facility as an interface implemented by the
	// class.
	// TODO: document the logging facility.
	// TODO: Put the logging file information in the configuration parameters
	// and take this information into account in the AbstractAssembly class.

	/** canonical name of the directory in which logging files are written.	*/
	protected static String	LOGGING_FILES_DIRECTORY =
				File.separator + "Users" + File.separator + "jmalenfant" +
													File.separator + "tmp" ;
	/** file extension of logging files.									*/
	private static String	LOG_FILE_EXTENSION = "log" ;
	/** initial size of in-memory buffer for logging messages.				*/
	protected static int		LOGGING_BUFFER_INITIAL_SIZE = 4000 ;
	/** Character used to separate the time stamp from the log message,
	 * which eases the processing of the file as a csv file by Excel-like
	 * spreadsheets.														*/
	protected static char	LOGGING_SEPARATION_CHARACTER = '|' ;

	/**	True if the component is doing logging of its actions.				*/
	protected boolean		loggingStatus = false ;
	/** True if the logging is done on the terminal rather than in files.	*/
	protected boolean		tracingStatus = false ;
	/**	The buffer in which logging messages are accumulated until their
	 *  writing on the logging file.										*/
	protected StringBuffer	executionLog ;

	/**
	 * configure the logging and tracing facility for components : must be
	 * called before creating any component..
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param loggingFilesDirectory			directory where to put logging files (default is <code>~/tmp</code>)
	 * @param loggingFilesExtension			logging file names extension (default is <code>log</code>)
	 * @param loggingBufferInitialSize		initial size of the logging character buffer (default is 4000)
	 * @param loggingSeparationCharacter	character used to separate logs from timestamps (default is '<code>|</code>')
	 */
	public static void	configureLogging(
		String loggingFilesDirectory,
		String loggingFilesExtension,
		int loggingBufferInitialSize,
		char loggingSeparationCharacter
		)
	{
		LOGGING_FILES_DIRECTORY = loggingFilesDirectory ;
		LOG_FILE_EXTENSION = loggingFilesExtension ;
		LOGGING_BUFFER_INITIAL_SIZE = loggingBufferInitialSize ;
		LOGGING_SEPARATION_CHARACTER = loggingSeparationCharacter ;
	}

	/**
	 * @see fr.upmc.components.ComponentI#toggleLogging()
	 */
	@Override
	public void			toggleLogging()
	{
		this.loggingStatus = !this.loggingStatus ;
		if (this.isLogging() && this.executionLog == null) {
			this.executionLog = new StringBuffer(LOGGING_BUFFER_INITIAL_SIZE) ;
		}
	}

	/**
	 * @see fr.upmc.components.ComponentI#toggleTracing()
	 */
	@Override
	public void			toggleTracing()
	{
		this.tracingStatus = !this.tracingStatus ;
	}

	/**
	 * @see fr.upmc.components.ComponentI#logMessage(java.lang.String)
	 */
	@Override
	public void			logMessage(String message)
	{
		String log = "" + System.currentTimeMillis() +
							LOGGING_SEPARATION_CHARACTER + message + "\n" ;
		if (this.loggingStatus) {
			this.executionLog.append(log) ;
		}
		if (this.tracingStatus) {
			System.out.print(log);
		}
	}

	/**
	 * @see fr.upmc.components.ComponentI#isLogging()
	 */
	@Override
	public boolean		isLogging() {
		return this.loggingStatus ;
	}

	/**
	 * @see fr.upmc.components.ComponentI#isTracing()
	 */
	@Override
	public boolean		isTracing() {
		return this.tracingStatus ;
	}

	/**
	 * @see fr.upmc.components.ComponentI#printExecutionLog()
	 */
	@Override
	public void			printExecutionLog()
	{
		if (this.isLogging() && !this.isTracing()) {
			this.printExecutionLog(System.out) ;
		}
	}

	/**
	 * @see fr.upmc.components.ComponentI#printExecutionLog(java.io.PrintStream)
	 */
	@Override
	public void			printExecutionLog(PrintStream out)
	{
		assert	out != null ;

		if (this.isLogging() && !this.isTracing()) {
			out.println(this.executionLog.toString()) ;
		}
	}

	/**
	 * @see fr.upmc.components.ComponentI#printExecutionLogOnFile(java.lang.String)
	 */
	@Override
	public void			printExecutionLogOnFile(String fileName)
	throws	FileNotFoundException
	{
		assert	fileName != null ;

		if (this.isLogging() && !this.isTracing()) {
			File f = new File(LOGGING_FILES_DIRECTORY + File.separator +
										fileName + '.' + LOG_FILE_EXTENSION) ;
			PrintStream ps = new PrintStream(f) ;
			this.printExecutionLog(ps) ;
			ps.close() ;
		}
	}

	// ------------------------------------------------------------------------
	// Interfaces and ports information
	// ------------------------------------------------------------------------

	/**
	 * class objects representing all the required interfaces implemented
	 * by this component.
	 */
	@GuardedBy("this")
	protected Vector<Class<?>>	requiredInterfaces ;

	/**
	 * class objects representing all the offered interfaces implemented
	 * by this component.
	 */
	@GuardedBy("this")
	protected Vector<Class<?>>	offeredInterfaces ;

	/*
	 * a hashtable mapping interfaces implemented by this component to
	 * vectors of ports to which one can connect using these interfaces.
	 */
	@GuardedBy("this")
	protected Hashtable<Class<?>,Vector<PortI>>	interfaces2ports ;

	/*
	 * a hashtable mapping URIs of ports owned by this component to ports to
	 * which one can connect.
	 */
	@GuardedBy("this")
	protected Hashtable<String,PortI>	portURIs2ports ;

	// ------------------------------------------------------------------------
	// Creation, constructors
	// ------------------------------------------------------------------------

	/**
	 * create a passive component and initialise the collections of implemented
	 * interfaces and the mapping from interfaces to ports.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 */
	@Deprecated
	public				AbstractComponent()
	{
		super();
		this.innerComponents = new Vector<ComponentI>() ;
		this.isConcurrent = false ;
		this.canScheduleTasks = false ;
		this.requestHandler = null ;
		this.nbThreads = 0 ;
		this.scheduledTasksHandler = null ;
		this.nbSchedulableThreads = 0 ;
		this.requiredInterfaces = new Vector<Class<?>>() ;
		this.offeredInterfaces = new Vector<Class<?>>() ;
		this.interfaces2ports = new Hashtable<Class<?>,Vector<PortI>>() ;
		this.portURIs2ports = new Hashtable<String, PortI>() ;

		if (this.isLogging()) {
			this.executionLog = new StringBuffer(LOGGING_BUFFER_INITIAL_SIZE) ;
		}
		this.state = ComponentState.INITIALISED ;
		try {
			this.configurePluginFacilities() ;
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}

		assert	this.innerComponents != null && this.innerComponents.size() == 0 ;
		assert	this.requiredInterfaces != null && this.requiredInterfaces.size() == 0 ;
		assert	this.offeredInterfaces != null && this.offeredInterfaces.size() == 1 ;
		assert	this.interfaces2ports != null && this.interfaces2ports.size() == 1 ;
		assert	this.portURIs2ports != null && this.portURIs2ports.size() == 1 ;
	}

	/**
	 * create a passive component if <code>isConcurrent</code> is false, and
	 * an active one with only one thread if it is true.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param isConcurrent	if true, the component is created with its own pool of threads.
	 */
	@Deprecated
	public				AbstractComponent(boolean isConcurrent)
	{
		this();
		this.isConcurrent = isConcurrent ;
		if (isConcurrent) {
			this.requestHandler = Executors.newSingleThreadExecutor() ;
			this.nbThreads = 1 ;
		}
	}

	/**
	 * create a passive component if <code>nbThreads</code> is zero, and
	 * an active one with <code>nbThreads</code> threads otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	nbThreads >= 0
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param nbThreads	number of threads to be created in the component pool.
	 * @throws IllegalArgumentException	if nbThreads < 0.
	 */
	@Deprecated
	public				AbstractComponent(int nbThreads)
	{
		this() ;
		assert	nbThreads >= 0 ;

		this.nbThreads = nbThreads ;
		if (nbThreads > 0) {
			this.isConcurrent = true ;
		}
		if (nbThreads == 1) {
			this.requestHandler = Executors.newSingleThreadExecutor() ;
		} else if (nbThreads > 1) {
			this.requestHandler = Executors.newFixedThreadPool(nbThreads) ;
		}
	}

	/**
	 * create a passive component if both <code>isConcurrent</code> and
	 * <code>canScheduleTasks</code> are false, and an active one with only
	 * one non schedulable thread if <code>isConcurrent</code> is true and
	 * one schedulable thread if <code>canScheduleTasks</code> is true.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param isConcurrent		if true, the component is created with its own pool of threads.
	 * @param canScheduleTasks	if true, the component is created with its own schedulable pool of threads.
	 */
	@Deprecated
	public				AbstractComponent(
		boolean isConcurrent,
		boolean canScheduleTasks
		)
	{
		this(isConcurrent) ;
		this.canScheduleTasks = canScheduleTasks ;
		if (canScheduleTasks) {
			this.scheduledTasksHandler =
								Executors.newSingleThreadScheduledExecutor() ;
			this.nbSchedulableThreads = 1 ;
		}
	}

	/**
	 * create a passive component if both <code>nbThreads</code> and
	 * <code>nbSchedulableThreads</code> are both zero, and an active one with
	 * <code>nbThreads</code> non schedulable thread and
	 * <code>nbSchedulableThreads</code> schedulable threads otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	nbThreads >= 0 && nbSchedulableThreads >= 0
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param nbThreads				number of threads to be created in the component pool.
	 * @param nbSchedulableThreads	number of threads to be created in the component schedulable pool.
	 */
	public				AbstractComponent(
		int nbThreads,
		int nbSchedulableThreads
		)
	{
		this(AbstractPort.generatePortURI(ReflectionI.class),
											nbThreads, nbSchedulableThreads) ;
	}

	/**
	 * create a passive component if both <code>nbThreads</code> and
	 * <code>nbSchedulableThreads</code> are both zero, and an active one with
	 * <code>nbThreads</code> non schedulable thread and
	 * <code>nbSchedulableThreads</code> schedulable threads otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	reflectionInboundPortURI != null
	 * pre	nbThreads >= 0
	 * pre	nbSchedulableThreads >= 0
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param reflectionInboundPortURI	URI of the inbound port offering the <code>ReflectionI</code> interface.
	 * @param nbThreads					number of threads to be created in the component pool.
	 * @param nbSchedulableThreads		number of threads to be created in the component schedulable pool.
	 */
	public				AbstractComponent(
		String reflectionInboundPortURI,
		int nbThreads,
		int nbSchedulableThreads
		)
	{
		assert	reflectionInboundPortURI != null ;
		assert	nbThreads >= 0 ;
		assert	nbSchedulableThreads >= 0 ;

		this.innerComponents = new Vector<ComponentI>() ;
		this.isConcurrent = false ;
		this.canScheduleTasks = false ;
		this.requestHandler = null ;
		this.nbThreads = 0 ;
		this.scheduledTasksHandler = null ;
		this.nbSchedulableThreads = 0 ;
		this.requiredInterfaces = new Vector<Class<?>>() ;
		this.offeredInterfaces = new Vector<Class<?>>() ;
		this.interfaces2ports = new Hashtable<Class<?>,Vector<PortI>>() ;
		this.portURIs2ports = new Hashtable<String, PortI>() ;

		if (this.isLogging()) {
			this.executionLog = new StringBuffer(LOGGING_BUFFER_INITIAL_SIZE) ;
		}
		this.state = ComponentState.INITIALISED ;
		try {
			this.configurePluginFacilities() ;
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}

		this.nbThreads = nbThreads ;
		if (nbThreads > 0) {
			this.isConcurrent = true ;
		}
		if (nbThreads == 1) {
			this.requestHandler = Executors.newSingleThreadExecutor() ;
		} else if (nbThreads > 1) {
			this.requestHandler = Executors.newFixedThreadPool(nbThreads) ;
		}
		
		this.nbSchedulableThreads = nbSchedulableThreads ;
		if (nbSchedulableThreads > 0) {
			this.canScheduleTasks = true ;
		}
		if (nbSchedulableThreads == 1) {
			this.scheduledTasksHandler =
								Executors.newSingleThreadScheduledExecutor() ;
		} else if (nbSchedulableThreads > 1) {
			this.scheduledTasksHandler =
						Executors.newScheduledThreadPool(nbSchedulableThreads) ;
		}

		this.addOfferedInterface(ReflectionI.class) ;
		try {
			ReflectionInboundPort rip =
					new ReflectionInboundPort(reflectionInboundPortURI, this) ;
			this.addPort(rip) ;
			rip.publishPort() ;
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}

		assert	this.innerComponents != null &&
										this.innerComponents.size() == 0 ;
		assert	this.requiredInterfaces != null &&
										this.requiredInterfaces.size() == 0 ;
		// Two pre-declared offered interfaces and their ports:
		// - fr.upmc.components.pre.reflection.interfaces.ReflectionI
		// - fr.upmc.components.pre.plugins.interfaces.ComponentPluginI
		assert	this.offeredInterfaces != null
									&& this.offeredInterfaces.size() == 2 ;
		assert	this.interfaces2ports != null 
									&& this.interfaces2ports.size() == 2 ;
		assert	this.portURIs2ports != null
									&& this.portURIs2ports.size() == 2 ;
	}

	// ------------------------------------------------------------------------
	// Internal behaviour requests
	// ------------------------------------------------------------------------

	/**
	 * @see fr.upmc.components.ComponentI#isInStateAmong(fr.upmc.components.ComponentStateI[])
	 */
	@Override
	public boolean		isInStateAmong(ComponentStateI[] states)
	{
		assert	states != null ;

		boolean ret = false ;
		for (int i = 0 ; !ret && i < states.length ; i++) {
			ret = (this.state == states[i]) ;
		}
		return ret ;
	}

	/**
	 * @see fr.upmc.components.ComponentI#notInStateAmong(fr.upmc.components.ComponentStateI[])
	 */
	@Override
	public boolean		notInStateAmong(ComponentStateI[] states)
	{
		assert	states != null ;

		boolean ret = true ;
		for (int i = 0 ; ret && i < states.length ; i++) {
			ret = (this.state != states[i]) ;
		}
		return ret ;
	}

	/**
	 * @see fr.upmc.components.ComponentI#isConcurrent()
	 */
	@Override
	public boolean		isConcurrent()
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) ;

		return this.isConcurrent || this.canScheduleTasks() ;
	}

	/**
	 * @see fr.upmc.components.ComponentI#canScheduleTasks()
	 */
	@Override
	public boolean		canScheduleTasks()
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) ;

		return this.canScheduleTasks ;
	}

	// ------------------------------------------------------------------------
	// Implemented interfaces
	// ------------------------------------------------------------------------

	/**
	 * @see fr.upmc.components.ComponentI#getInterfaces()
	 */
	@Override
	public Class<?>[]	getInterfaces()
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) ;

		Vector<Class<?>> temp = new Vector<Class<?>>() ;
		synchronized (this.requiredInterfaces) {
			temp.addAll(this.requiredInterfaces) ;
		}
		synchronized (this.offeredInterfaces) {
			temp.addAll(this.offeredInterfaces) ;
		}
		return temp.toArray(new Class<?>[]{}) ;
	}

	/**
	 * @see fr.upmc.components.ComponentI#getInterface(java.lang.Class)
	 */
	@Override
	public Class<?>		getInterface(Class<?> inter)
	{
		Class<?> ret = this.getRequiredInterface(inter) ;
		if (ret == null) {
			ret = this.getOfferedInterface(inter) ;
		}
		return ret ;
	}

	/**
	 * @see fr.upmc.components.ComponentI#getRequiredInterfaces()
	 */
	@Override
	public Class<?>[]	getRequiredInterfaces()
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) ;

		Class<?>[] ret ;
		synchronized (this.requiredInterfaces) {
			ret = this.requiredInterfaces.toArray(new Class<?>[]{}) ;
		}
		return ret ;
	}

	/**
	 * @see fr.upmc.components.ComponentI#getRequiredInterface(java.lang.Class)
	 */
	@Override
	public Class<?>		getRequiredInterface(Class<?> inter)
	{
		Class<?> ret = null ;
		boolean found = false ;
		for(int i = 0 ; !found && i < this.requiredInterfaces.size() ; i++) {
			if (inter.isAssignableFrom(this.requiredInterfaces.get(i))) {
				found = true ;
				ret = this.requiredInterfaces.get(i) ;
			}
		}
		return ret ;
	}

	/**
	 * @see fr.upmc.components.ComponentI#getOfferedInterfaces()
	 */
	@Override
	public Class<?>[]	getOfferedInterfaces()
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) ;

		Class<?>[] ret ;
		synchronized (this.offeredInterfaces) {
			ret = this.offeredInterfaces.toArray(new Class<?>[]{}) ;
		}
		return ret ;
	}

	/**
	 * @see fr.upmc.components.ComponentI#getOfferedInterface(java.lang.Class)
	 */
	@Override
	public Class<?>		getOfferedInterface(Class<?> inter)
	{
		Class<?> ret = null ;
		boolean found = false ;
		for(int i = 0 ; !found && i < this.offeredInterfaces.size() ; i++) {
			if (inter.isAssignableFrom(this.offeredInterfaces.get(i))) {
				found = true ;
				ret = this.offeredInterfaces.get(i) ;
			}
		}
		return ret ;
	}

	/**
	 * @see fr.upmc.components.ComponentI#addRequiredInterface(java.lang.Class)
	 */
	@Override
	public void			addRequiredInterface(Class<?> inter)
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) ;
		assert	RequiredI.class.isAssignableFrom(inter) ;
		assert	!this.isRequiredInterface(inter) ;

		synchronized (this.requiredInterfaces) {
			this.requiredInterfaces.add(inter) ;
		}

		assert	this.isRequiredInterface(inter) ;
	}

	/**
	 * @see fr.upmc.components.ComponentI#removeRequiredInterface(java.lang.Class)
	 */
	@Override
	public void			removeRequiredInterface(Class<?> inter)
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) ;
		assert	RequiredI.class.isAssignableFrom(inter) ;
		assert	this.isRequiredInterface(inter) ;

		synchronized (this.requiredInterfaces) {
			this.requiredInterfaces.remove(inter) ;
		}

		assert	!this.isRequiredInterface(inter) ;
	}

	/**
	 * @see fr.upmc.components.ComponentI#addOfferedInterface(java.lang.Class)
	 */
	@Override
	public void			addOfferedInterface(Class<?> inter)
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) ;
		assert	OfferedI.class.isAssignableFrom(inter) ;
		assert	!this.isOfferedInterface(inter) ;

		synchronized (this.offeredInterfaces) {
			this.offeredInterfaces.add(inter) ;
		}

		assert	this.isOfferedInterface(inter) ;
	}

	/**
	 * @see fr.upmc.components.ComponentI#removeOfferedInterface(java.lang.Class)
	 */
	@Override
	public void			removeOfferedInterface(Class<?> inter)
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) ;
		assert	OfferedI.class.isAssignableFrom(inter) ;
		assert	this.isOfferedInterface(inter) ;

		synchronized (this.offeredInterfaces) {
			this.offeredInterfaces.remove(inter) ;
		}

		assert	!this.isOfferedInterface(inter) ;
	}

	/**
	 * @see fr.upmc.components.ComponentI#isInterface(java.lang.Class)
	 */
	@Override
	public boolean		isInterface(Class<?> inter)
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) ;

		return this.isRequiredInterface(inter) ||
											this.isOfferedInterface(inter) ;
	}

	/**
	 * @see fr.upmc.components.ComponentI#isRequiredInterface(java.lang.Class)
	 */
	@Override
	public boolean		isRequiredInterface(Class<?> inter)
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) ;

		synchronized (this.requiredInterfaces) {
			boolean ret = false ;
			for(int i = 0 ; !ret && i < this.requiredInterfaces.size() ; i++) {
				if (inter.isAssignableFrom(this.requiredInterfaces.get(i))) {
					ret = true ;
				}
			}
			return ret ;
		}
	}

	/**
	 * @see fr.upmc.components.ComponentI#isOfferedInterface(java.lang.Class)
	 */
	@Override
	public boolean		isOfferedInterface(Class<?> inter)
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) ;

		synchronized (this.offeredInterfaces) {
			boolean ret = false ;
			for(int i = 0 ; !ret && i < this.offeredInterfaces.size() ; i++) {
				if (inter.isAssignableFrom(this.offeredInterfaces.get(i))) {
					ret = true ;
				}
			}
			return ret ;
		}
	}

	// ------------------------------------------------------------------------
	// Port management
	//
	//   Port objects are implementation artifacts for components and must
	//   not be manipulated (referenced) outside their owner component.
	//   Port URIs are used to designate ports most of the time. The only
	//   exceptions in the model are plug-ins, which are meant to extend the
	//   internal behaviour of components and as such can manipulate ports.
	//   Hence, methods that directly manipulate port objects are protected
	//   while the ones manipulating port URIs are public.
	//
	// ------------------------------------------------------------------------

	/**
	 * find the ports of this component that expose the interface inter.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})
	 * post	return == null || forall(PortI p : return) { inter.equals(p.getImplementedInterface()) }
	 * </pre>
	 *
	 * @param inter	interface for which ports are sought.
	 * @return		array of ports exposing inter.
	 */
	protected PortI[]	findPortsFromInterface(Class<?> inter)
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) ;

		PortI[] ret = null ;
		Vector<PortI> temp ;

		synchronized (this.interfaces2ports) {
			temp = this.interfaces2ports.get(inter) ;
		}
		if (temp != null) {
			synchronized (temp) {
				ret = temp.toArray(new PortI[]{}) ;
			}
		}
		return ret ;
	}

	/**
	 * @see fr.upmc.components.ComponentI#getPortImplementedInterface(java.lang.String)
	 */
	@Override
	public Class<?> getPortImplementedInterface(String portURI)
	throws Exception
	{
		assert	portURI != null && this.isPortExisting(portURI) ;

		return this.findPortFromURI(portURI).getImplementedInterface() ;
	}

	/**
	 * @see fr.upmc.components.ComponentI#findPortURIsFromInterface(java.lang.Class)
	 */
	@Override
	public String[]		findPortURIsFromInterface(Class<?> inter)
	throws Exception
	{
		String[] ret = null ;
		PortI[] ports = this.findPortsFromInterface(inter) ;
		if (ports != null && ports.length > 0) {
			ret = new String[ports.length] ;
			for (int i = 0 ; i < ports.length ; i++) {
				ret[i] = ports[i].getPortURI() ;
			}
		}
		return ret ;
	}

	/**
	 * @see fr.upmc.components.ComponentI#findInboundPortURIsFromInterface(java.lang.Class)
	 */
	@Override
	public String[]		findInboundPortURIsFromInterface(Class<?> inter)
	throws Exception
	{
		String[] ret = null ;

		PortI[] ports = this.findPortsFromInterface(inter) ;
		if (ports != null && ports.length > 0) {
			ArrayList<String> al = new ArrayList<String>() ;
			for (int i = 0 ; i < ports.length ; i++) {
				if (ports[i] instanceof InboundPortI) {
					al.add(ports[i].getPortURI()) ;
				}
			}
			ret = al.toArray(new String[0]) ;
		}
		return ret ;
	}

	/**
	 * @see fr.upmc.components.ComponentI#findOutboundPortURIsFromInterface(java.lang.Class)
	 */
	@Override
	public String[]		findOutboundPortURIsFromInterface(Class<?> inter)
	throws Exception
	{
		String[] ret = null ;

		PortI[] ports = this.findPortsFromInterface(inter) ;
		if (ports != null && ports.length > 0) {
			ArrayList<String> al = new ArrayList<String>() ;
			for (int i = 0 ; i < ports.length ; i++) {
				if (ports[i] instanceof OutboundPortI) {
					al.add(ports[i].getPortURI()) ;
				}
			}
			ret = al.toArray(new String[0]) ;
		}
		return ret ;
	}

	/**
	 * finds a port of this component from its URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	return == null || return.getPortURI().equals(portURI)
	 * </pre>
	 *
	 * @param portURI	the URI a the sought port.
	 * @return			the port with the given URI or null if not found.
	 */
	protected PortI		findPortFromURI(String portURI)
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) ;

		synchronized (this.portURIs2ports) {
			return this.portURIs2ports.get(portURI) ;
		}
	}

	/**
	 * add a port to the set of ports of this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})
	 * pre	this.equals(p.getOwner())
	 * pre	this.isInterface(p.getImplementedInterface())
	 * pre	!exist(PortI p1 : this.findPortsFromInterface(p.getImplementedInterface())) { p.equals(p1) ; }
	 * post	exist(PortI p1 : this.findPortsFromInterface(p.getImplementedInterface())) { p.equals(p1) ; }
	 * </pre>
	 *
	 * @param p		port to be added.
	 * @throws Exception 
	 */
	protected void		addPort(PortI p) throws Exception
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) ;
		assert	this.equals(p.getOwner()) ;
		assert	this.isInterface(p.getImplementedInterface()) ;
		assert	this.portURIs2ports.get(p.getPortURI()) == null ;

		Vector<PortI> vps = null ;
		synchronized (this.interfaces2ports) {
			vps = this.interfaces2ports.get(p.getImplementedInterface()) ;
			if (vps == null) {
				vps = new Vector<PortI>() ;
				vps.add(p) ;
				this.interfaces2ports.put(p.getImplementedInterface(), vps) ;
			} else {
				synchronized (vps) {
					vps.add(p) ;
				}
			}
		}
		synchronized (this.portURIs2ports) {
			this.portURIs2ports.put(p.getPortURI(), p) ;
		}

		assert	this.interfaces2ports.containsKey(p.getImplementedInterface()) ;
		assert	this.portURIs2ports.containsKey(p.getPortURI()) ;
	}

	/**
	 * method with package visibility meant to be used by plug-in implementation
	 * objects; add a port to the set of ports of the component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})
	 * pre	this.equals(p.getOwner())
	 * pre	this.isInterface(p.getImplementedInterface())
	 * pre	!exist(PortI p1 : this.findPortsFromInterface(p.getImplementedInterface())) { p.equals(p1) ; }
	 * post	exist(PortI p1 : this.findPortsFromInterface(p.getImplementedInterface())) { p.equals(p1) ; }
	 * </pre>
	 *
	 * @param p			port to be added.
	 * @throws Exception
	 */
	void				doAddPort(PortI p) throws Exception
	{
		this.addPort(p) ;
	}

	/**
	 * remove a port from the set of ports of this component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	this.notInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})
	 * pre	exist(PortI p1 : this.findPortsFromInterface(p.getImplementedInterface())) { p1.equals(p)) ; }
	 * post	!exist(PortI p1 : this.findPortsFromInterface(p.getImplementedInterface())) { p1.equals(p)) ; }
	 * </pre>
	 *
	 * @param p		port to be removed.
	 * @throws Exception 
	 */
	protected void		removePort(PortI p) throws Exception
	{
		assert	this.notInStateAmong(new ComponentStateI[]{
							ComponentState.TERMINATED
							}) ;
		assert	this.interfaces2ports.containsKey(p.getImplementedInterface()) ;
		assert	this.portURIs2ports.containsKey(p.getPortURI()) ;

		synchronized (this.interfaces2ports) {
			Vector<PortI> vps =
				this.interfaces2ports.get(p.getImplementedInterface()) ;
			synchronized (vps) {
				vps.remove(p) ;
				if (vps.isEmpty()) {
					this.interfaces2ports.remove(p.getImplementedInterface()) ;
				}
			}
		}
		synchronized (this.portURIs2ports) {
			this.portURIs2ports.remove(p.getPortURI()) ;
		}

		assert	!this.portURIs2ports.containsKey(p.getPortURI()) ;
	}

	/**
	 * @see fr.upmc.components.ComponentI#isPortExisting(java.lang.String)
	 */
	@Override
	public boolean		isPortExisting(String portURI) throws Exception
	{
		PortI p = this.findPortFromURI(portURI) ;
		return p != null ;
	}

	/**
	 * @see fr.upmc.components.ComponentI#isPortConnected(java.lang.String)
	 */
	@Override
	public boolean		isPortConnected(String portURI) throws Exception
	{
		assert	portURI != null && this.isPortExisting(portURI) ;

		PortI p = this.findPortFromURI(portURI) ;
		return p.connected() ;
	}

	/**
	 * @see fr.upmc.components.ComponentI#doPortConnection(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void			doPortConnection(
		String portURI,
		String otherPortURI,
		String ccname
		) throws Exception 
	{
		assert	portURI != null && otherPortURI != null && ccname != null ;
		assert	this.isPortExisting(portURI) ;
		assert	!this.isPortConnected(portURI) ;

		PortI p = this.findPortFromURI(portURI) ;
		if (p == null) {
			throw new Exception("unknown port URI in this component") ;
		} else {
			p.doConnection(otherPortURI, ccname) ;
		}

		assert	this.isPortConnected(portURI) ;
	}

	/**
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true				// no more preconditions.
	 * post	true				// no more postconditions.
	 * </pre>
	 * 
	 * @see fr.upmc.components.ComponentI#doPortDisconnection(java.lang.String)
	 */
	@Override
	public void			doPortDisconnection(String portURI) throws Exception
	{
		assert	portURI != null && this.isPortExisting(portURI) ;
		assert	this.isPortConnected(portURI) ;
	
		PortI p = this.findPortFromURI(portURI) ;
		p.doDisconnection() ;

		assert	!this.isPortConnected(portURI) ;
	}

	/**
	 * @see fr.upmc.components.ComponentI#removePort(java.lang.String)
	 */
	@Override
	public void			removePort(String portURI) throws Exception
	{
		assert	portURI != null ;
		assert	this.isPortExisting(portURI) ;

		PortI p = this.findPortFromURI(portURI) ;
		this.removePort(p) ;

		assert	!this.isPortExisting(portURI) ;
	}

	// ------------------------------------------------------------------------
	// Component life cycle
	// ------------------------------------------------------------------------

	/**
	 * @see fr.upmc.components.ComponentI#start()
	 */
	@Override
	public void			start() throws ComponentStartException
	{
		assert	this.isInStateAmong(new ComponentStateI[]{
							ComponentState.INITIALISED
							}) ;

		// Start inner components
		// assumes that the creation and publication are done
		// assumes that composite components always reside in one JVM
		for(ComponentI c : this.innerComponents) {
			c.start() ;
		}

		// Could create the requestHandler and the scheduledTasksHandler, but
		// it appears safer to do so in the constructors.

		this.state = ComponentState.STARTED ;
	}

	/**
	 * @see fr.upmc.components.ComponentI#shutdown()
	 */
	@Override
	public void			shutdown() throws ComponentShutdownException
	{
		assert	this.isInStateAmong(new ComponentStateI[]{
							ComponentState.STARTED
							}) ;

		// Shutdown inner components
		// assumes that all inner components are disconnected.
		for(ComponentI c : this.innerComponents) {
			c.shutdown() ;
		}

		try {
			this.unConfigurePluginFacilitites() ;
		} catch (Exception e1) {
			throw new ComponentShutdownException(e1) ;
		}

		ArrayList<PortI> toBeDestroyed =
						new ArrayList<PortI>(this.portURIs2ports.values()) ;
		for (PortI p : toBeDestroyed) {
			try {
				p.destroyPort() ;
			} catch (Exception e) {
				throw new ComponentShutdownException(e) ;
			}
		}
		if (this.isConcurrent) {
			this.requestHandler.shutdown() ;
		}
		if (this.canScheduleTasks) {
			this.scheduledTasksHandler.shutdown() ;
		}
		this.state = ComponentState.SHUTTINGDOWN ;
		if (!this.isConcurrent && !this.canScheduleTasks) {
			this.state = ComponentState.SHUTDOOWN ;
		}
	}

	/**
	 * @see fr.upmc.components.ComponentI#shutdownNow()
	 */
	@Override
	public void			shutdownNow() throws ComponentShutdownException
	{
		assert	this.isInStateAmong(new ComponentStateI[]{
							ComponentState.STARTED
							}) ;

		// Shutdown inner components
		// assumes that all inner components are disconnected.
		for(ComponentI c : this.innerComponents) {
			c.shutdownNow() ;
		}

		try {
			this.unConfigurePluginFacilitites() ;
		} catch (Exception e1) {
			throw new ComponentShutdownException(e1) ;
		}

		for (PortI p : this.portURIs2ports.values()) {
			try {
				p.destroyPort() ;
			} catch (Exception e) {
				throw new ComponentShutdownException(e) ;
			}
		}
		if (this.isConcurrent) {
			this.requestHandler.shutdownNow() ;
		}
		if (this.canScheduleTasks) {
			this.scheduledTasksHandler.shutdownNow() ;
		}
		this.state = ComponentState.SHUTDOOWN ;
	}

	/**
	 * @see fr.upmc.components.ComponentI#isStarted()
	 */
	@Override
	public boolean		isStarted()
	{
		if (this.isInStateAmong(new ComponentStateI[]{ComponentState.STARTED})) {
			return true ;
		} else {
			return false ;
		}
	}

	/**
	 * @see fr.upmc.components.ComponentI#isShutdown()
	 */
	@Override
	public boolean		isShutdown()
	{
		boolean isShutdown = false ;

		if (this.isInStateAmong(new ComponentStateI[]{ComponentState.SHUTDOOWN})) {
			return true ;
		}

		if (this.isConcurrent) {
			isShutdown = this.requestHandler.isShutdown() ;
			if (this.canScheduleTasks) {
				isShutdown = isShutdown &&
									this.scheduledTasksHandler.isShutdown() ;
			}
		} else {
			if (this.canScheduleTasks) {
				isShutdown = this.scheduledTasksHandler.isShutdown() ;
			}
		}
		if (isShutdown) {
			this.state = ComponentState.SHUTDOOWN ;
		}
		return isShutdown ;
	}

	/**
	 * @see fr.upmc.components.ComponentI#isTerminated()
	 */
	@Override
	public boolean		isTerminated()
	{
		boolean isTerminated = false ;

		if (this.isInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})) {
			return true ;
		}

		if (this.isConcurrent) {
			isTerminated = this.requestHandler.isTerminated() ;
		} else {
			isTerminated = this.isShutdown() ;
		}
		if (this.canScheduleTasks) {
			isTerminated = isTerminated &&
									this.scheduledTasksHandler.isTerminated() ;
		}
		if (isTerminated) {
			this.state = ComponentState.TERMINATED ;
		}
		return isTerminated ;
	}

	/**
	 * @see fr.upmc.components.ComponentI#awaitTermination(long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public boolean		awaitTermination(long timeout, TimeUnit unit)
	throws	InterruptedException
	{
		if (this.isInStateAmong(new ComponentStateI[]{ComponentState.TERMINATED})) {
			return true ;
		}

		boolean status = false ;
		if (this.canScheduleTasks) {
			status =
					this.scheduledTasksHandler.awaitTermination(timeout, unit) ;
		}
		if (this.isConcurrent) {
			status = status &&
						this.requestHandler.awaitTermination(timeout, unit) ;
		} else {
			status = true ;
		}
		if (status) {
			this.state = ComponentState.TERMINATED ;
		}
		return status ;
	}

	// ------------------------------------------------------------------------
	// Task execution
	// ------------------------------------------------------------------------

	/**
	 * @see fr.upmc.components.ComponentI#runTask(fr.upmc.components.ComponentI.ComponentTask)
	 */
	@Override
	public Future<?>	runTask(ComponentTask t)
	{
		assert	this.isInStateAmong(new ComponentStateI[]{
							ComponentState.STARTED
							}) ;
		assert	t != null ;

		Future<?> f = null ;
		if (this.isConcurrent()) {
			if (this.isConcurrent) {
				f = this.requestHandler.submit(t) ;
			} else {
				assert	this.canScheduleTasks ;
				f = this.scheduledTasksHandler.submit(t) ;
			}
		} else {
			t.run() ;
			f = new Future<Object>() {
						@Override
						public boolean	cancel(boolean arg0)
						{ return false ; }

						@Override
						public Object	get()
						throws	InterruptedException, ExecutionException
						{ return null ; }

						@Override
						public Object get(long arg0, TimeUnit arg1)
						throws 	InterruptedException, ExecutionException,
							   	TimeoutException
						{ return null ; }

						@Override
						public boolean	isCancelled()
						{ return false ; }

						@Override
						public boolean	isDone()
						{ return true ; }
					} ;
		}
		return f ;
	}

	/**
	 * @see fr.upmc.components.ComponentI#scheduleTask(fr.upmc.components.ComponentI.ComponentTask, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public ScheduledFuture<?>	scheduleTask(
		ComponentTask t,
		long delay,
		TimeUnit u
		)
	{
		assert	this.isInStateAmong(new ComponentStateI[]{
							ComponentState.STARTED
							}) ;
		assert	this.canScheduleTasks ;
		assert	t != null && delay >= 0 && u != null ;

		return this.scheduledTasksHandler.schedule(t, delay, u) ;
	}

	/**
	 * @see fr.upmc.components.ComponentI#scheduleTaskAtFixedRate(fr.upmc.components.ComponentI.ComponentTask, long, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public ScheduledFuture<?>	scheduleTaskAtFixedRate(
		ComponentTask t,
		long initialDelay,
		long period,
		TimeUnit u
		)
	{
		assert	this.isInStateAmong(new ComponentStateI[]{
							ComponentState.STARTED
							}) ;
		assert	this.canScheduleTasks() ;
		assert	t != null && initialDelay >= 0  && period > 0 && u != null ;

		return this.scheduledTasksHandler.
							scheduleAtFixedRate(t, initialDelay, period, u) ;
	}

	/**
	 * @see fr.upmc.components.ComponentI#scheduleTaskWithFixedDelay(fr.upmc.components.ComponentI.ComponentTask, long, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public ScheduledFuture<?>	scheduleTaskWithFixedDelay(
		ComponentTask t,
		long initialDelay,
		long delay,
		TimeUnit u
		)
	{
		assert	this.isInStateAmong(new ComponentStateI[]{
							ComponentState.STARTED
							}) ;
		assert	this.canScheduleTasks() ;
		assert	t != null && initialDelay >= 0 && delay >= 0 && u != null ;

		return this.scheduledTasksHandler.
							scheduleWithFixedDelay(t, initialDelay, delay, u) ;
	}

	// ------------------------------------------------------------------------
	// Request handling
	// ------------------------------------------------------------------------

	/**
	 * @see fr.upmc.components.ComponentI#handleRequest(fr.upmc.components.ComponentI.ComponentService)
	 */
	@Override
	public <T> Future<T>	handleRequest(ComponentService<T> task)
	throws Exception
	{
		assert	this.isInStateAmong(new ComponentStateI[]{
							ComponentState.STARTED
							}) ;
		assert	task != null ;

		if (this.isConcurrent()) {
			if (this.isConcurrent) {
				return this.requestHandler.submit(task) ;
			} else {
				assert	this.canScheduleTasks ;
				return this.scheduledTasksHandler.submit(task) ;
			}
		} else {
			final ComponentService<T> t = task ;
			return new Future<T>() {
							@Override
							public boolean	cancel(boolean arg0)
							{ return false ; }

							@Override
							public T		get()
							throws	InterruptedException, ExecutionException
							{
								try {
									return t.call() ;
								} catch (Exception e) {
									throw new ExecutionException(e) ;
								}
							}
							@Override
							public T		get(long arg0, TimeUnit arg1)
							throws	InterruptedException,
									ExecutionException, TimeoutException
							{ return null ; }

							@Override
							public boolean	isCancelled()
							{ return false ; }

							@Override
							public boolean	isDone()
							{ return true ; }
						} ;
		}
	}

	/**
	 * @see fr.upmc.components.ComponentI#handleRequestSync(fr.upmc.components.ComponentI.ComponentService)
	 */
	@Override
	public <T> T		handleRequestSync(ComponentService<T> task)
	throws Exception
	{
		assert	this.isInStateAmong(new ComponentStateI[]{
							ComponentState.STARTED
							}) ;
		assert	task != null ;

		if (this.isConcurrent()) {
			return this.handleRequest(task).get() ;
		} else {
			return task.call() ;
		}
	}

	/**
	 * @see fr.upmc.components.ComponentI#handleRequestAsync(fr.upmc.components.ComponentI.ComponentService)
	 */
	@Override
	public <T> void		handleRequestAsync(ComponentService<T> task)
	throws Exception
	{
		assert	this.isInStateAmong(new ComponentStateI[]{
							ComponentState.STARTED
							}) ;
		assert	task != null ;

		if (this.isConcurrent()) {
			this.handleRequest(task) ;
		} else {
			task.call();
		}
	}

	/**
	 * @see fr.upmc.components.ComponentI#scheduleRequest(fr.upmc.components.ComponentI.ComponentService, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public <T> ScheduledFuture<T>	scheduleRequest(
		ComponentService<T> request,
		long delay,
		TimeUnit u
		)
	{
		assert	this.isInStateAmong(new ComponentStateI[]{
							ComponentState.STARTED
							}) ;
		assert	this.canScheduleTasks ;
		assert	request != null && delay >= 0 && u != null ;

		return this.scheduledTasksHandler.schedule(request, delay, u) ;
	}

	/**
	 * FIXME: does not make sense in the remote call case!
	 * 
	 * @see fr.upmc.components.ComponentI#scheduleRequestSync(fr.upmc.components.ComponentI.ComponentService, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public <T> T		scheduleRequestSync(
		ComponentService<T> request,
		long delay,
		TimeUnit u
		) throws InterruptedException, ExecutionException
	{
		assert	this.isInStateAmong(new ComponentStateI[]{
							ComponentState.STARTED
							}) ;
		assert	this.canScheduleTasks ;
		assert	request != null && delay >= 0 && u != null ;

		return this.scheduleRequest(request, delay, u).get() ;
	}

	/**
	 * @see fr.upmc.components.ComponentI#scheduleRequestAsync(fr.upmc.components.ComponentI.ComponentService, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public void			scheduleRequestAsync(
		ComponentService<?> request,
		long delay,
		TimeUnit u
		)
	{
		assert	this.isInStateAmong(new ComponentStateI[]{
							ComponentState.STARTED
							}) ;
		assert	this.canScheduleTasks() ;
		assert	request != null && delay >= 0 && u != null ;

		this.scheduleRequest(request, delay, u) ;
	}
}
