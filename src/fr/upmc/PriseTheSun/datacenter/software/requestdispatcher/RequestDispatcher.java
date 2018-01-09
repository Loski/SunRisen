package fr.upmc.PriseTheSun.datacenter.software.requestdispatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces.RequestDispatcherDynamicStateI;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces.RequestDispatcherManagementI;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.ports.RequestDispatcherDynamicStateDataInboundPort;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.ports.RequestDispatcherManagementInboundPort;
import fr.upmc.components.AbstractComponent;
import fr.upmc.components.ComponentI;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.datacenter.TimeManagement;
import fr.upmc.datacenter.interfaces.ControlledDataOfferedI;
import fr.upmc.datacenter.interfaces.PushModeControllingI;
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
 * */

public class RequestDispatcher 
extends		AbstractComponent
implements	
			RequestSubmissionHandlerI,
			RequestNotificationHandlerI,
			RequestDispatcherManagementI,
			PushModeControllingI
{

	public static int	DEBUG_LEVEL = 2 ;
	public static int NB_REQUEST_NEEDED_FOR_AVG = 10;
	
	/** URI of this request dispatcher */
	protected String rdURI;
	
	/** InboundPort to receive submission*/
	protected RequestSubmissionInboundPort requestSubmissionInboundPort;
	
	/** OutboundPort to send notification*/
	protected RequestNotificationOutboundPort requestNotificationOutboundPort;
	
	/** InboundPort to receive VM notification */
	protected RequestNotificationInboundPort  requestNotificationInboundPort;

	/** List of virtual machines' data (URI,submissionOutboundPort,...)*/
	protected List<VirtualMachineData> virtualMachineDataList;
	
	/** map associating the uri of a Request with the VirtualMachineData*/
	protected HashMap<String,VirtualMachineData> requestVirtalMachineDataMap;
	
	/** index of the VM in the requestSubmissionOutboundPortList which will receive the next request*/
	private int currentVM;
	
	/** Inbound port offering the management interface.						*/
	protected RequestDispatcherManagementInboundPort requestDispatcherManagementInboundPort ;
	
	/** dispatcher data inbound port through which it pushes its dynamic data.	*/
	protected RequestDispatcherDynamicStateDataInboundPort requestDispatcherDynamicStateDataInboundPort ;
	
	/** future of the task scheduled to push dynamic data.					*/
	protected ScheduledFuture<?>			pushingFuture ;
	
	/** */
	private int nbRequestRecevedSinceAverage;
	private Double averageTime;
	
	/**
	 * Construct a <code>RequestDispatcher</code>.
	 * @param requestDispatcherURI the request dispatcher URI.
	 * @param requestDispatcherManagementInboundPortURI the request dispatcher management inbound port URI.
	 * @param requestSubmissionInboundPortURI the request submission inbound port URI.
	 * @param requestNotificationInboundPortURI the request notification inbound port URI.
	 * @param requestNotificationOutboundPortURI the request notification outbound port URI.
	 * @throws Exception
	 */
	public RequestDispatcher(
			String requestDispatcherURI, 
			String requestDispatcherManagementInboundPortURI, 
			String requestSubmissionInboundPortURI, 
			String requestNotificationOutboundPortURI, 
			//List<String> requestSubmissionOutboundPortList, 
			String requestNotificationInboundPortURI,
			String requestDispatcherDynamicStateDataInboundPortURI
			) throws Exception 
	{
		
				super(requestDispatcherURI,1,1);
		
				// Preconditions
				assert	requestDispatcherURI != null ;
				assert	requestDispatcherManagementInboundPortURI != null ;
				assert	requestSubmissionInboundPortURI != null ;
				assert	requestNotificationOutboundPortURI != null ;
				assert	requestNotificationInboundPortURI != null;
				//assert	requestSubmissionOutboundPortList != null;
				
				this.rdURI=requestDispatcherURI;
				
				// Interfaces and ports

				this.addOfferedInterface(RequestDispatcherManagementI.class) ;
				this.requestDispatcherManagementInboundPort =
						new RequestDispatcherManagementInboundPort(
								requestDispatcherManagementInboundPortURI,
								this) ;
				this.addPort(this.requestDispatcherManagementInboundPort) ;
				this.requestDispatcherManagementInboundPort.publishPort() ;
				
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
				
				this.virtualMachineDataList = new ArrayList<VirtualMachineData>();
				this.addRequiredInterface( RequestSubmissionI.class );
				
				this.addOfferedInterface(ControlledDataOfferedI.ControlledPullI.class) ;
				this.requestDispatcherDynamicStateDataInboundPort =
						new RequestDispatcherDynamicStateDataInboundPort(
								requestDispatcherDynamicStateDataInboundPortURI, this) ;
				this.addPort(this.requestDispatcherDynamicStateDataInboundPort) ;
				this.requestDispatcherDynamicStateDataInboundPort.publishPort() ;
				
				this.requestVirtalMachineDataMap = new HashMap<>();
				this.nbRequestRecevedSinceAverage = 0;
				this.averageTime = null;
	}
	
	private void nextVM()
	{
		this.currentVM = (this.currentVM+1)%this.virtualMachineDataList.size();
	}

	private RequestSubmissionOutboundPort getCurrentVMPort()
	{
		return this.virtualMachineDataList.get(this.currentVM).getRsobp();
	}
	
	private String getCurrentVMURI()
	{
		return this.virtualMachineDataList.get(this.currentVM).getVmURI();
	}
	
	private void beginRequestTime(String requestURI)
	{
		VirtualMachineData vmData = this.virtualMachineDataList.get(this.currentVM);
		
		this.requestVirtalMachineDataMap.put(requestURI,vmData);
		
		vmData.addRequest(this.rdURI);
	}
	
	private void endRequestTime(String requestURI)
	{
		VirtualMachineData vmData  = this.requestVirtalMachineDataMap.remove(requestURI);
		vmData.endRequest();
		
		this.nbRequestRecevedSinceAverage++;
	}
	
	private Double averageTime()
	{
		double res = 0.0;
		
		if(this.nbRequestRecevedSinceAverage >= NB_REQUEST_NEEDED_FOR_AVG)
		{
			int nbRequest = 0;
			
			for(VirtualMachineData vmData: this.virtualMachineDataList)
			{
				vmData.calculateAverageTime();
				res+=vmData.getAverageTime();
				
				nbRequest++;
			}
			
			this.averageTime = res/nbRequest;
			this.nbRequestRecevedSinceAverage = 0;
		}
		
		return averageTime;
	}
	
	@Override
	public void acceptRequestSubmission(RequestI r) throws Exception {

		assert r != null;
		
		beginRequestTime(r.getRequestURI());
		
		RequestSubmissionOutboundPort port = getCurrentVMPort();
		port.submitRequest(r);

		this.nextVM();
	}

	@Override
	public void acceptRequestSubmissionAndNotify(RequestI r) throws Exception {
		
		assert r != null;
		
		beginRequestTime(r.getRequestURI());
		
		RequestSubmissionOutboundPort port = getCurrentVMPort();
		
		if (RequestGenerator.DEBUG_LEVEL >= 1) 
			this.logMessage(String.format("%s transfers %s to %s using %s",this.rdURI,r.getRequestURI(),getCurrentVMURI(),port.getPortURI()));
		
		port.submitRequestAndNotify(r);
		
		this.nextVM();
	}

	@Override
	public void acceptRequestTerminationNotification(RequestI r) throws Exception {
		
		try {
		assert r != null;
		
		this.endRequestTime(r.getRequestURI());
		
		if (RequestGenerator.DEBUG_LEVEL >= 1) 
			this.logMessage(String.format("RequestDispatcher [%s] notifies end of request %s",this.rdURI,r.getRequestURI()));
		
		this.requestNotificationOutboundPort.notifyRequestTermination( r );
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.err.println(r.getRequestURI());
			System.out.println(this.requestVirtalMachineDataMap);
		}
	}
	
	
	@Override
	public void shutdown() throws ComponentShutdownException {
		
	        try {
	            if ( this.requestNotificationOutboundPort.connected() ) {
	                this.requestNotificationOutboundPort.doDisconnection();
	            }
	            for (VirtualMachineData data : virtualMachineDataList)
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

	@Override
	public void connectVirtualMachine(String vmURI, String requestSubmissionInboundPortURI, String RequestSubmissionOutboundPortURI) throws Exception {
		
		/*if(this.requestSubmissionOutboundPortList.get(indexVM)!=null && this.requestSubmissionOutboundPortList.get(indexVM).getPortURI().equals(RequestSubmissionOutboundPortURI))
			throw new Exception("VM déjà connecté sur ce port");*/
		
		RequestSubmissionOutboundPort port = new RequestSubmissionOutboundPort( RequestSubmissionOutboundPortURI, this );
		
		this.virtualMachineDataList.add(new VirtualMachineData(vmURI, port));
		this.addPort( port );
		port.publishPort();
		
		this.doPortConnection(
				port.getPortURI(),
				requestSubmissionInboundPortURI,
				getConnectorClassName());
		
		if (RequestGenerator.DEBUG_LEVEL >= 2)
			this.logMessage(String.format("[%s] Connecting %s with %s using %s -> %s",getConnectorSimpleName(),this.rdURI,vmURI,port.getPortURI(),requestSubmissionInboundPortURI));
	}

	@Override
	public void disconnectVirtualMachine(String vmURI) throws Exception {
		
		ListIterator<VirtualMachineData> iterator = this.virtualMachineDataList.listIterator();
		
		while(iterator.hasNext()){
			VirtualMachineData data = iterator.next();
			if(data.getVmURI().equals(vmURI))
			{
				RequestSubmissionOutboundPort port = data.getRsobp();
				
				if(port!=null && port.connected())
				{
					port.doDisconnection();
				}	
				iterator.remove();
			}
			else
			{
				data.resetRequestTimeDataList();
			}
		}
	}
	
	public String getConnectorClassName()
	{
		return RequestSubmissionConnector.class.getCanonicalName();
	}
	
	public String getConnectorSimpleName()
	{
		return RequestSubmissionConnector.class.getSimpleName();
	}

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
	
	public RequestDispatcherDynamicStateI	getDynamicState() throws Exception
	{
		return new RequestDispatcherDynamicState(this.rdURI,this.averageTime(),this.virtualMachineDataList) ;
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
	public void startLimitedPushing(int interval, int n) throws Exception {
		
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
			this.pushingFuture.cancel(false) ;
		}
	}
	
	private String getSlowestVM()
	{
		return this.virtualMachineDataList.get(0).getVmURI();
	}
	
	private String getFastestVM()
	{
		return this.virtualMachineDataList.get(0).getVmURI();
	}

}
