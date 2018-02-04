package fr.upmc.PriseTheSun.datacenter.software.requestdispatcher;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import fr.upmc.PriseTheSun.datacenter.software.controller.connectors.VMDisconnectionNotificationConnector;
import fr.upmc.PriseTheSun.datacenter.software.controller.interfaces.VMDisconnectionNotificationHandlerI;
import fr.upmc.PriseTheSun.datacenter.software.controller.ports.VMDisconnectionNotificationHandlerOutboundPort;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces.RequestDispatcherDynamicStateI;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces.RequestDispatcherIntrospectionI;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces.RequestDispatcherManagementI;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces.RequestDispatcherStaticStateI;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.ports.RequestDispatcherDynamicStateDataInboundPort;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.ports.RequestDispatcherIntrospectionInboundPort;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.ports.RequestDispatcherManagementInboundPort;
import fr.upmc.components.AbstractComponent;
import fr.upmc.components.ComponentI;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.datacenter.TimeManagement;
import fr.upmc.datacenter.interfaces.ControlledDataOfferedI;
import fr.upmc.datacenter.interfaces.PushModeControllingI;
import fr.upmc.datacenter.software.applicationvm.connectors.ApplicationVMIntrospectionConnector;
import fr.upmc.datacenter.software.applicationvm.interfaces.ApplicationVMDynamicStateI;
import fr.upmc.datacenter.software.applicationvm.interfaces.ApplicationVMIntrospectionI;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMIntrospectionOutboundPort;
import fr.upmc.datacenter.software.connectors.RequestNotificationConnector;
import fr.upmc.datacenter.software.connectors.RequestSubmissionConnector;
import fr.upmc.datacenter.software.interfaces.RequestI;
import fr.upmc.datacenter.software.interfaces.RequestNotificationHandlerI;
import fr.upmc.datacenter.software.interfaces.RequestNotificationI;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionHandlerI;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionI;
import fr.upmc.datacenter.software.ports.RequestNotificationInboundPort;
import fr.upmc.datacenter.software.ports.RequestNotificationOutboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionInboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort;
import fr.upmc.datacenterclient.requestgenerator.RequestGenerator;


/**
 * The class <code>RequestDispatcher</code> is a component receiving request submissions from
 * a given application, and dispatching these requests to the different virtual machines
 * allocated for this application by the admissionController.
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	true
 * </pre>
 * 
 * @author	Loïc Lafontaine
 */
public class RequestDispatcher 
extends		AbstractComponent
implements	
			RequestSubmissionHandlerI,
			RequestNotificationHandlerI,
			RequestDispatcherManagementI,
			PushModeControllingI,
			RequestDispatcherIntrospectionI
{
	
	public static enum RequestDispatcherPortTypes {
		REQUEST_SUBMISSION, REQUEST_NOTIFICATION, MANAGEMENT, INTROSPECTION, STATIC_STATE,
		DYNAMIC_STATE
	}

	public static int	DEBUG_LEVEL = -10 ;
	
	// -------------------------------------------------------------------------
	// Constants and instance variables
	// -------------------------------------------------------------------------
	
	/** uri of this request dispatcher */
	protected String rdURI;
	
	/** inboundPort to receive submission*/
	protected RequestSubmissionInboundPort requestSubmissionInboundPort;
	
	/** outboundPort to send notification*/
	protected RequestNotificationOutboundPort requestNotificationOutboundPort;
	
	/** inboundPort to receive VM notification */
	protected RequestNotificationInboundPort  requestNotificationInboundPort;
	
	/** map assiociating the uri of a VirtualMachine with the bean containing information about the vm*/
	protected HashMap<String,VirtualMachineData> requestVirtualMachineDataMap;
	
	/** set containing the uris of VirtualMachines still executing Task and need to be disconnected after*/
	protected HashSet<String> virtualMachineWaitingForDisconnection;
	
	/** queue containing the virtualMachines that can execute request (it's mean that the vm have at least one idle core)*/
	protected Queue<String> virtualMachineAvailable;
	
	/** queue containing the virtualMachines in execution (the dispatcher doesn't send request to them)*/
	protected Queue<String> virtualMachineNotAvailable;
	
	/** map associating the uri of a Request with its VirtualMachineData*/
	protected HashMap<String,VirtualMachineData> requestExecutedBy;
	
	/** inbound port offering the management interface.						*/
	protected RequestDispatcherManagementInboundPort requestDispatcherManagementInboundPort ;
	
	/** dispatcher data inbound port through which it pushes its dynamic data.	*/
	protected RequestDispatcherDynamicStateDataInboundPort requestDispatcherDynamicStateDataInboundPort ;
	
	protected RequestDispatcherIntrospectionInboundPort rdIntrospectionInboundPort ;
	
	/** future of the task scheduled to push dynamic data.					*/
	protected ScheduledFuture<?>			pushingFuture ;
	
	/** outbound port to notify the controller that a vm can be disconnected.						*/
	protected VMDisconnectionNotificationHandlerOutboundPort vmnobp;
	
	/** dummy object to lock critical segment that implies an action on one of the object containing virtual machine information */
	protected Object listLock;
	
	/** */
	protected boolean inDisconnectionState = false;
	
	/** the number of request which was received */
	protected int nbRequestReceived = 0;
	
	/** the number of request which was resolved */
	protected int nbRequestTerminated = 0;

	/** a temporary queue if there is no vm available to handle the requests */
	protected Queue<RequestI> queue;
	
	/** a temporary map containing information about the time the request needed to be ended (contains same amount of elements as the queue) */
	protected HashMap<String,RequestTimeData> timeDataMap;
	
	// ------------------------------------------------------------------------
	// Component constructor
	// ------------------------------------------------------------------------
	
	/**
	 * Construct a <code>RequestDispatcher</code>.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	requestDispatcherURI != null
	 * pre	requestDispatcherManagementInboundPortURI != null
	 * pre	requestSubmissionInboundPortURI != null
	 * pre	requestNotificationOutboundPortURI != null
	 * pre	requestNotificationInboundPortURI != null
	 * pre	requestDispatcherDynamicStateDataInboundPortURI != null
	 * pre	VMDisconnectionNotificationHandlerOutboundPortURI != null
	 * post	true			// no postcondition.
	 * </pre>
	 * 
	 * @param requestDispatcherURI the request dispatcher URI.
	 * @param requestDispatcherManagementInboundPortURI the request dispatcher management inbound port URI.
	 * @param requestSubmissionInboundPortURI the request submission inbound port URI (to cummunicate with the request generator).
	 * @param requestNotificationOutboundPortURI the request notification outbound port URI (to cummunicate with the request generator).
	 * @param requestNotificationInboundPortURI the request notification inbound port URI (to cummunicate with the virtual machines).
	 * @param requestDispatcherDynamicStateDataInboundPortURI the dynamic state data inbound port URI.
	 * @param VMDisconnectionNotificationHandlerOutboundPortURI the outbound port URI to notify vm disconnection.
	 * @throws Exception
	 */
	public 			RequestDispatcher(
		String requestDispatcherURI, 
		String requestDispatcherManagementInboundPortURI, 
		String requestSubmissionInboundPortURI, 
		String requestNotificationOutboundPortURI,
		String requestNotificationInboundPortURI,
		String requestDispatcherDynamicStateDataInboundPortURI,
		String VMDisconnectionNotificationHandlerOutboundPortURI
		) throws Exception 
	{
		
		super(requestDispatcherURI,1,1);

		// Preconditions
		assert	requestDispatcherURI != null ;
		assert	requestDispatcherManagementInboundPortURI != null ;
		assert	requestSubmissionInboundPortURI != null ;
		assert	requestNotificationOutboundPortURI != null ;
		assert	requestNotificationInboundPortURI != null;
		assert	requestDispatcherDynamicStateDataInboundPortURI != null;
		assert	VMDisconnectionNotificationHandlerOutboundPortURI != null;
		
		this.rdURI=requestDispatcherURI;
		
		this.requestVirtualMachineDataMap = new HashMap<>();
		this.virtualMachineWaitingForDisconnection = new HashSet<String>();
		this.requestExecutedBy = new HashMap<String,VirtualMachineData>();
		this.virtualMachineAvailable = new LinkedList<String>();
		this.virtualMachineNotAvailable = new LinkedList<String>();
		
		this.timeDataMap = new HashMap<>();
		
		this.nbRequestReceived=0;
		this.nbRequestTerminated=0;
		
		this.queue = new LinkedList<RequestI>();
		
		this.listLock = new Object();
		
		// Interfaces and ports
		this.addOfferedInterface(RequestDispatcherManagementI.class) ;
		this.requestDispatcherManagementInboundPort =
				new RequestDispatcherManagementInboundPort(
						requestDispatcherManagementInboundPortURI,
						this) ;
		this.addPort(this.requestDispatcherManagementInboundPort) ;
		this.requestDispatcherManagementInboundPort.publishPort() ;
		
		this.addOfferedInterface(RequestDispatcherIntrospectionI.class) ;
		this.rdIntrospectionInboundPort =
			new RequestDispatcherIntrospectionInboundPort(
									rdURI + "-intro",
									this) ;
		this.addPort(this.rdIntrospectionInboundPort) ;
		this.rdIntrospectionInboundPort.publishPort() ;
		
		this.addRequiredInterface(VMDisconnectionNotificationHandlerI.class) ;
		this.vmnobp = new VMDisconnectionNotificationHandlerOutboundPort(VMDisconnectionNotificationHandlerOutboundPortURI,this);
		this.addPort(this.vmnobp) ;
		this.vmnobp.publishPort() ;
		
			//To communicate with the sender of the request
		this.addOfferedInterface( RequestNotificationI.class );
		this.requestNotificationInboundPort = 
						new RequestNotificationInboundPort( 
										requestNotificationInboundPortURI, this);
		this.addPort( this.requestNotificationInboundPort );
		this.requestNotificationInboundPort.publishPort();
		
		this.addOfferedInterface(RequestSubmissionI.class) ;
		this.requestSubmissionInboundPort =
						new RequestSubmissionInboundPort(
										requestSubmissionInboundPortURI, this) ;
		this.addPort(this.requestSubmissionInboundPort) ;
		this.requestSubmissionInboundPort.publishPort() ;
		
			//To communicate with the VMs
		this.addRequiredInterface(RequestNotificationI.class) ;
		this.requestNotificationOutboundPort =
			new RequestNotificationOutboundPort(
									requestNotificationOutboundPortURI,
									this) ;
		this.addPort(this.requestNotificationOutboundPort) ;
		this.requestNotificationOutboundPort.publishPort() ;
		
		this.addOfferedInterface(ControlledDataOfferedI.ControlledPullI.class) ;
		this.requestDispatcherDynamicStateDataInboundPort =
				new RequestDispatcherDynamicStateDataInboundPort(
						requestDispatcherDynamicStateDataInboundPortURI, this) ;
		this.addPort(this.requestDispatcherDynamicStateDataInboundPort) ;
		this.requestDispatcherDynamicStateDataInboundPort.publishPort() ;
		
			//Interface Required for future connexion with a vm
		this.addRequiredInterface( RequestSubmissionI.class );
		this.addRequiredInterface(ApplicationVMIntrospectionI.class );
	}
	
	// ------------------------------------------------------------------------
	// Component life-cycle
	// ------------------------------------------------------------------------
	
	/**
	 * @see fr.upmc.components.AbstractComponent#shutdown()
	 */
	@Override
	public void shutdown() throws ComponentShutdownException {
		
	        try {
	            if ( this.requestNotificationOutboundPort.connected() ) {
	                this.requestNotificationOutboundPort.doDisconnection();
	            }
	            for (VirtualMachineData data : this.requestVirtualMachineDataMap.values())
	            {
	            	RequestSubmissionOutboundPort port = data.getRsobp();
	            	
	            	if (port.connected() ) {
	            		port.doDisconnection();
	     	       }
	            }
	            if (this.requestDispatcherDynamicStateDataInboundPort.connected()) {
					this.requestDispatcherDynamicStateDataInboundPort.doDisconnection() ;
				}
	               
	        }
	        catch ( Exception e ) {
	            throw new ComponentShutdownException( e );
	        }

	        super.shutdown();
	}
	
	// ------------------------------------------------------------------------
	// Component services
	// ------------------------------------------------------------------------
	
	/**
	 * @see fr.upmc.datacenter.software.interfaces.RequestSubmissionHandlerI#acceptRequestSubmission(fr.upmc.datacenter.software.interfaces.RequestI)
	 */
	@Override
	public void acceptRequestSubmission(RequestI r) throws Exception {

		assert r != null;
		
		RequestTimeData timeData = new RequestTimeData();
		
		this.nbRequestReceived++;
		
		VirtualMachineData vm = findAvailableVM();
		
		if(vm==null)
		{
			this.timeDataMap.put(r.getRequestURI(),timeData);
			this.queue.add(r);
		}
		else
		{
			vm.addRequest(r.getRequestURI(),timeData);
			
			RequestSubmissionOutboundPort port = vm.getRsobp();
			port.submitRequestAndNotify(r);

			this.requestExecutedBy.put(r.getRequestURI(),vm);
		}
	}
	
	/**
	 * @see fr.upmc.datacenter.software.interfaces.RequestSubmissionHandlerI#acceptRequestSubmissionAndNotify(fr.upmc.datacenter.software.interfaces.RequestI)
	 */
	@Override
	public void acceptRequestSubmissionAndNotify(RequestI r) throws Exception {
		
		assert r != null;
		
		try {
		RequestTimeData timeData = new RequestTimeData();

		this.nbRequestReceived++;
		
		VirtualMachineData vm = findAvailableVM();
		
		if(vm==null)
		{
			this.timeDataMap.put(r.getRequestURI(),timeData);
			this.queue.add(r);
		}
		else
		{
			vm.addRequest(r.getRequestURI(),timeData);
			
			RequestSubmissionOutboundPort port = vm.getRsobp();
			port.submitRequestAndNotify(r);
			
			if (RequestGenerator.DEBUG_LEVEL >= 1) 
				this.logMessage(String.format("%s transfers %s to %s using %s",this.rdURI,r.getRequestURI(),vm.getVmURI(),port.getPortURI()));
			
			this.requestExecutedBy.put(r.getRequestURI(),vm);
		}
		
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * process an end of execution notification for a request r previously
	 * submitted. 
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	r != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param r	request that just terminated.
	 * @throws Exception
	 */
	@Override
	public void acceptRequestTerminationNotification(RequestI r) throws Exception {

		assert r != null;
		
		this.nbRequestTerminated++;
		
		try {
		VirtualMachineData vm = this.requestExecutedBy.remove(r.getRequestURI());
		vm.endRequest(r.getRequestURI());
		
		synchronized(this.listLock)
		{
			if(!this.virtualMachineWaitingForDisconnection.isEmpty() && this.virtualMachineWaitingForDisconnection.contains(vm.getVmURI()))
			{
				if(vm.getRequestInQueue().isEmpty())
				{
					this.disconnectVirtualMachine(vm);
				}
			}
			else
			{
				synchronized(this.queue)
				{
					if(!this.queue.isEmpty())
						executeRequestInQueue(vm);
					else
					{
						if(this.virtualMachineNotAvailable.remove(vm.getVmURI()))
							this.virtualMachineAvailable.add(vm.getVmURI());
					}
				}
			}
		}
		
		this.requestNotificationOutboundPort.notifyRequestTermination( r );
		
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		if (RequestGenerator.DEBUG_LEVEL >= 1) 
			this.logMessage(String.format("RequestDispatcher [%s] notifies end of request %s",this.rdURI,r.getRequestURI()));
	}
	
	// ------------------------------------------------------------------------
	// Component internal services
	// ------------------------------------------------------------------------
	
	/**
	 * find a virtual machine which executes a request (at least one core is idle)
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	a VirtualMachineData reference or null.
	 * @throws Exception
	 */
	protected VirtualMachineData findAvailableVM() throws Exception
	{		
		synchronized(this.listLock)
		{			
			if(this.virtualMachineAvailable.isEmpty())
			{
				return null;
			}
			else 
			{
				try {
				String uri = this.virtualMachineAvailable.peek();
				VirtualMachineData vm = this.requestVirtualMachineDataMap.get(uri);
				
				if(vm.getAvmiovp().getNumberOfCores()<vm.getRequestInQueue().size())
				{					
					this.virtualMachineAvailable.remove();
					this.virtualMachineNotAvailable.add(vm.getVmURI());
				}
				
				return vm;
				}catch(Exception e)
				{
					e.printStackTrace();
					System.out.println("AAAYA "+this.virtualMachineAvailable.peek());
					System.out.println(this.requestVirtualMachineDataMap);
					return null;
				}
			}
		}
	}
	
	/**
	 * remove the first request in the dispatcher queue and execute it
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param executor		the virtual machine which will execute the request.
	 * @throws Exception
	 */
	protected void executeRequestInQueue(VirtualMachineData executor) throws Exception {
		RequestI req = this.queue.remove();
		
		RequestTimeData timeData = this.timeDataMap.remove(req.getRequestURI());
		
		executor.addRequest(req.getRequestURI(),timeData);
		
		RequestSubmissionOutboundPort port = executor.getRsobp();
		port.submitRequestAndNotify(req);
		
		if (RequestGenerator.DEBUG_LEVEL >= 1)
			this.logMessage(String.format("%s transfers %s to %s using %s",this.rdURI,req.getRequestURI(),executor.getVmURI(),port.getPortURI()));
		
		this.requestExecutedBy.put(req.getRequestURI(),executor);
		
		if(this.virtualMachineAvailable.remove(executor.getVmURI()))
			this.virtualMachineNotAvailable.add(executor.getVmURI());
	}
	
	/**
	 * disconnect a virtual machine
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param vmData		the virtual machine to disconnect.
	 * @throws Exception
	 */
	protected void disconnectVirtualMachine(VirtualMachineData vmData) throws Exception
	{
		synchronized (this.listLock) {
			
			RequestSubmissionOutboundPort port = vmData.getRsobp();
			if(port!=null && port.connected())
			{
				port.doDisconnection();
				port.destroyPort();
			}
			
			ApplicationVMIntrospectionOutboundPort portIntrospection = vmData.getAvmiovp();
			if(portIntrospection!=null && portIntrospection.connected())
			{
				portIntrospection.doDisconnection();
				portIntrospection.destroyPort();
			}
			
			this.virtualMachineWaitingForDisconnection.remove(vmData.getVmURI());
		}
		
		this.vmnobp.receiveVMDisconnectionNotification(vmData.getVmURI());
		

		if(inDisconnectionState && this.requestVirtualMachineDataMap.isEmpty() && this.virtualMachineWaitingForDisconnection.isEmpty())
		{			
			if(this.vmnobp.connected())
			{				
				this.vmnobp.disconnectController();
				this.vmnobp.doDisconnection();
			}
		}
	}	
	
	// ------------------------------------------------------------------------
	// Component introspection services
	// ------------------------------------------------------------------------
	
	public RequestDispatcherDynamicStateI	getDynamicState() throws Exception
	{
		HashMap<String,Double> virtualMachineExecutionAverageTime = new HashMap<String,Double>();
		HashMap<String,ApplicationVMDynamicStateI> virtualMachineDynamicStates = new HashMap<String,ApplicationVMDynamicStateI>();
		
		Double average = null;
		
		double averageTime = 0.0;
		boolean oneRequestFound = false;
		
		for(VirtualMachineData vmData: this.requestVirtualMachineDataMap.values())
		{
			vmData.calculateAverageTime();
			Double averageVM = vmData.getAverageTime();
			if(averageVM!=null)
			{
				averageTime+=averageVM;
				oneRequestFound=true;
			}
			virtualMachineExecutionAverageTime.put(vmData.getVmURI(),vmData.getAverageTime());
			virtualMachineDynamicStates.put(vmData.getVmURI(), vmData.getAvmiovp().getDynamicState());
		}
		
		if(oneRequestFound)
		{
			average = averageTime/this.requestVirtualMachineDataMap.size();
		}
		
		return new RequestDispatcherDynamicState(this.rdURI,average,virtualMachineExecutionAverageTime,virtualMachineDynamicStates,this.nbRequestReceived,this.nbRequestTerminated) ;
	}
	
	public void			sendDynamicState() throws Exception
	{
		if (this.requestDispatcherDynamicStateDataInboundPort.connected()) {
			RequestDispatcherDynamicStateI rdds = this.getDynamicState() ;
			this.requestDispatcherDynamicStateDataInboundPort.send(rdds) ;
		}
	}
	
	public void			sendDynamicState( final int interval, int numberOfRemainingPushes) throws Exception
	{
		this.sendDynamicState() ;
		
		final int fNumberOfRemainingPushes = numberOfRemainingPushes - 1 ;
		if (fNumberOfRemainingPushes > 0) {
			final RequestDispatcher rd = this ;
			this.pushingFuture =
					this.scheduleTask(
							new ComponentI.ComponentTask() {
								@Override
								public void run() {
									try {
										rd.sendDynamicState(
												interval,
												fNumberOfRemainingPushes) ;
									} catch (Exception e) {
										throw new RuntimeException(e) ;
									}
								}
							},
							TimeManagement.acceleratedDelay(interval),
							TimeUnit.MILLISECONDS) ;
		}
	}

	@Override
	public void startUnlimitedPushing(int interval) throws Exception {
		
		// first, send the static state if the corresponding port is connected
		//this.sendStaticState() ;

		final RequestDispatcher rd = this;
		this.pushingFuture =
			this.scheduleTaskAtFixedRate(
					new ComponentI.ComponentTask() {
						@Override
						public void run() {
							try {
								rd.sendDynamicState() ;
							} catch (Exception e) {
								throw new RuntimeException(e) ;
							}
						}
					},
					TimeManagement.acceleratedDelay(interval),
					TimeManagement.acceleratedDelay(interval),
					TimeUnit.MILLISECONDS) ;
	}

	@Override
	public void startLimitedPushing(final int interval, final int n) throws Exception {
		
		assert	n > 0 ;

		this.logMessage(this.rdURI + " startLimitedPushing with interval "
									+ interval + " ms for " + n + " times.") ;

		// first, send the static state if the corresponding port is connected
		//this.sendStaticState() ;

		final RequestDispatcher rd = this ;
		this.pushingFuture =
			this.scheduleTask(
					new ComponentI.ComponentTask() {
						@Override
						public void run() {
							try {
								rd.sendDynamicState(interval, n) ;
							} catch (Exception e) {
								throw new RuntimeException(e) ;
							}
						}
					},
					TimeManagement.acceleratedDelay(interval),
					TimeUnit.MILLISECONDS) ;
	}

	@Override
	public void stopPushing() throws Exception {
		
		if (this.pushingFuture != null &&
				!(this.pushingFuture.isCancelled() ||
									this.pushingFuture.isDone())) {
			this.pushingFuture.cancel(true) ;
		}
	}
	
	public Map<RequestDispatcherPortTypes, String>	getRequestDispatcherPortsURI()
	throws Exception
	{
		HashMap<RequestDispatcherPortTypes, String> ret =
						new HashMap<RequestDispatcherPortTypes, String>() ;
		ret.put(RequestDispatcherPortTypes.REQUEST_SUBMISSION,
						this.requestSubmissionInboundPort.getPortURI()) ;
		ret.put(RequestDispatcherPortTypes.REQUEST_NOTIFICATION,
				this.requestNotificationInboundPort.getPortURI()) ;
		ret.put(RequestDispatcherPortTypes.MANAGEMENT,
						this.requestDispatcherManagementInboundPort.getPortURI()) ;
		ret.put(RequestDispatcherPortTypes.INTROSPECTION,
						this.rdIntrospectionInboundPort.getPortURI()) ;
		ret.put(RequestDispatcherPortTypes.DYNAMIC_STATE,
						this.requestDispatcherDynamicStateDataInboundPort.getPortURI()) ;
		return ret ;
	}

	public RequestDispatcherStaticStateI getStaticState() {
		// TODO Auto-generated method stub
		return null;
	}

	// ------------------------------------------------------------------------
	// Component management services
	// ------------------------------------------------------------------------
	
	@Override
	public void connectWithRequestGenerator(String rgURI, String requestNotificationInboundPortURI) throws Exception {
		
		this.doPortConnection(
				this.requestNotificationOutboundPort.getPortURI(),
				requestNotificationInboundPortURI,
				RequestNotificationConnector.class.getCanonicalName()
				);
		
		if (RequestGenerator.DEBUG_LEVEL >= 2)
			this.logMessage(String.format("[RequestNotificationConnector] Connecting %s with %s using %s -> %s", this.rdURI,rgURI,this.requestNotificationOutboundPort.getPortURI(),requestNotificationInboundPortURI));
	}

	@Override
	public void disconnectRequestGenerator() throws Exception {
		if(this.requestNotificationOutboundPort.connected())
		{
			this.requestNotificationOutboundPort.doDisconnection();
		}
	}

	@Override
	public void connectController(String controllerURI, String VMDisconnectionHandlerInboundPortURI) throws Exception {

		this.doPortConnection(
				this.vmnobp.getPortURI(),
				VMDisconnectionHandlerInboundPortURI,
				VMDisconnectionNotificationConnector.class.getCanonicalName()
				);
	}

	@Override
	public void disconnectController() throws Exception 
	{
		inDisconnectionState = true;
		Iterator<Entry<String, VirtualMachineData>> it = this.requestVirtualMachineDataMap.entrySet().iterator();
		while(it.hasNext()) {
			this.askVirtualMachineDisconnection(it.next().getKey());
		}
	}
	
	@Override
	public void askVirtualMachineDisconnection(String vmURI) throws Exception {
		
		synchronized(this.listLock)
		{
			VirtualMachineData vmData = this.requestVirtualMachineDataMap.remove(vmURI);
			this.virtualMachineAvailable.remove(vmURI);
			this.virtualMachineNotAvailable.remove(vmURI);
			
			if(vmData.getRequestInQueue().isEmpty())
			{
				this.disconnectVirtualMachine(vmData);
			}
			else
			{
				this.virtualMachineWaitingForDisconnection.add(vmURI);
			}
		}
	}
	
	@Override
	public void connectVirtualMachine(String vmURI, String requestSubmissionInboundPortURI) throws Exception {
		
		assert !inDisconnectionState;
		assert !this.requestVirtualMachineDataMap.containsKey(vmURI);

		RequestSubmissionOutboundPort rsobp = new RequestSubmissionOutboundPort( rdURI+"-rsbop-"+vmURI, this );
		ApplicationVMIntrospectionOutboundPort avmiovp = new ApplicationVMIntrospectionOutboundPort( vmURI+"-introObp", this );
		
		VirtualMachineData vm = new VirtualMachineData(vmURI, rsobp,avmiovp);

		this.addPort( rsobp );
		rsobp.publishPort();
		
		this.doPortConnection(
				rsobp.getPortURI(),
				requestSubmissionInboundPortURI,
				RequestSubmissionConnector.class.getCanonicalName());
		
		this.addPort( avmiovp );
		avmiovp.publishPort();
		
		this.doPortConnection(
				avmiovp.getPortURI(),
				vmURI+"-intro",
				ApplicationVMIntrospectionConnector.class.getCanonicalName());
		
		if (RequestGenerator.DEBUG_LEVEL >= 2)
			this.logMessage(String.format("[%s] Connecting %s with %s using %s -> %s",RequestSubmissionConnector.class.getSimpleName(),this.rdURI,vmURI,rsobp.getPortURI(),requestSubmissionInboundPortURI));
	
		synchronized(this.listLock)
		{
			this.requestVirtualMachineDataMap.put(vmURI, vm);
			this.virtualMachineAvailable.add(vmURI);
			synchronized(this.queue)
			{
				if(!this.queue.isEmpty())
				{
					executeRequestInQueue(vm);
				}
			}
		}
	}

}
