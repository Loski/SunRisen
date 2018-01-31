package fr.upmc.PriseTheSun.datacenter.software.controller;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import fr.upmc.PriseTheSun.datacenter.hardware.computer.connector.ComputerControllerConnector;
import fr.upmc.PriseTheSun.datacenter.hardware.computer.ports.ComputerControllerManagementOutboutPort;
import fr.upmc.PriseTheSun.datacenter.hardware.processors.ProcessorsController.CoreAsk;
import fr.upmc.PriseTheSun.datacenter.hardware.processors.connector.ProcessorControllerManagementConnector;
import fr.upmc.PriseTheSun.datacenter.hardware.processors.interfaces.ProcessorsControllerManagementI;
import fr.upmc.PriseTheSun.datacenter.hardware.processors.ports.ProcessorsControllerManagementOutboundPort;
import fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.connector.AdmissionControllerManagementConnector;
import fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.interfaces.AdmissionControllerManagementI;
import fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.ports.AdmissionControllerManagementOutboundPort;
import fr.upmc.PriseTheSun.datacenter.software.applicationvm.ApplicationVMInfo;
import fr.upmc.PriseTheSun.datacenter.software.controller.connectors.NodeManagementConnector;
import fr.upmc.PriseTheSun.datacenter.software.controller.interfaces.NodeRingManagementI;
import fr.upmc.PriseTheSun.datacenter.software.controller.interfaces.VMDisconnectionNotificationHandlerI;
import fr.upmc.PriseTheSun.datacenter.software.controller.ports.NodeManagementInboundPort;
import fr.upmc.PriseTheSun.datacenter.software.controller.ports.NodeManagementOutboundPort;
import fr.upmc.PriseTheSun.datacenter.software.controller.ports.VMDisconnectionNotificationHandlerInboundPort;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.RequestDispatcher.RequestDispatcherPortTypes;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.connectors.RequestDispatcherIntrospectionConnector;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.connectors.RequestDispatcherManagementConnector;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces.RequestDispatcherDynamicStateI;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces.RequestDispatcherManagementI;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces.RequestDispatcherStateDataConsumerI;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces.RequestDispatcherStaticStateI;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.ports.RequestDispatcherDynamicStateDataOutboundPort;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.ports.RequestDispatcherIntrospectionOutboundPort;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.ports.RequestDispatcherManagementOutboundPort;
import fr.upmc.PriseTheSun.datacenter.software.ring.RingDynamicState;
import fr.upmc.PriseTheSun.datacenter.software.ring.interfaces.RingNetworkDynamicStateI;
import fr.upmc.PriseTheSun.datacenter.software.ring.interfaces.RingNetworkStateDataConsumerI;
import fr.upmc.PriseTheSun.datacenter.software.ring.ports.RingNetworkDynamicStateDataInboundPort;
import fr.upmc.PriseTheSun.datacenter.software.ring.ports.RingNetworkDynamicStateDataOutboundPort;
import fr.upmc.PriseTheSun.datacenter.tools.Writter;
import fr.upmc.components.AbstractComponent;
import fr.upmc.components.ComponentI;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.datacenter.connectors.ControlledDataConnector;
import fr.upmc.datacenter.interfaces.ControlledDataOfferedI;
import fr.upmc.datacenter.interfaces.PushModeControllingI;
import fr.upmc.datacenter.software.applicationvm.connectors.ApplicationVMManagementConnector;
import fr.upmc.datacenter.software.applicationvm.interfaces.ApplicationVMDynamicStateI;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;


/**
 * @author Maxime Lavaste
 *
 */
public class Controller extends AbstractComponent 
implements 	RequestDispatcherStateDataConsumerI, 
			RingNetworkStateDataConsumerI,
			NodeRingManagementI, 
			PushModeControllingI,
			VMDisconnectionNotificationHandlerI
{

	protected String controllerURI;
	protected String rdUri;
	
	protected AdmissionControllerManagementOutboundPort acmop;
	protected RequestDispatcherDynamicStateDataOutboundPort rddsdop;
	protected ProcessorsControllerManagementOutboundPort pcmop;

	private ScheduledFuture<?> pushingFuture;
	private NodeManagementInboundPort cmip;
	
	private RequestDispatcherIntrospectionOutboundPort rdiobp;

	private String requestDispatcherNotificationInboundPort;
	private String requestDispatcherManagementInboundPort;
	private RequestDispatcherManagementOutboundPort rdmop;
	
	int idVm = 0;
	int waitDecision = 0;
	//Vm reserved
	private List<ApplicationVMInfo> vmReserved;
	//vm to propagate to other controller
	private List<ApplicationVMInfo> freeApplicationVM;
	 
	private List<ApplicationVMInfo> myVMs;
	
	private Map<String, ComputerControllerManagementOutboutPort> cmops;
	
	
	/** Ring network port **/
	private RingNetworkDynamicStateDataOutboundPort rdsdop;
	private RingNetworkDynamicStateDataInboundPort rdsdip;
	
	/** Ring Network Management port of previous and next nodes**/
	private String controllerManagementNextInboundPort;
	private String controllerManagementPreviousInboundPort;

	public final static int PUSH_INTERVAL = 1000;
	public final static int REQUEST_MIN = PUSH_INTERVAL/100;
	
	
	private String appURI; 	
	private Writter w;
	private Object o = new Object();

	private Map<String, ArrayList<Double>> statistique;
	
	private VMDisconnectionNotificationHandlerInboundPort vmnibp;
	private String nextRingDynamicStateDataInboundPort;
	
	public Controller(
			String appURI, 
			String controllerURI, 
			String controllerManagement, 
			String requestDispatcherDynamicStateDataOutboundPort,
			String rdURI, 
			String requestDispatcherDynamicStateDataInboundPortURI, 
			String ADMManagementInboundPort,
			String ProcessorControllerManagementInboundUri, 
			String RingDynamicStateDataOutboundPortURI, 
			String RingDynamicStateDataInboundPortURI, 
			String nextRingDynamicStateDataInboundPort,
			String controllerManagementPreviousPort,
			String controllerManagementNextPort,
			ApplicationVMInfo vm, 
			String VMDisconnectionNotificationHandlerInboundPortURI
	) throws Exception
	{
		super(controllerURI,1,1);
		
		this.toggleLogging();
		this.toggleTracing();
		w = new Writter(controllerURI+ ".csv");
		
		this.controllerURI = controllerURI;
		this.rdUri = rdURI;
		this.appURI = appURI;
		this.rdiobp = new RequestDispatcherIntrospectionOutboundPort( rdURI+"-introObp", this );
		
		this.addPort( rdiobp );
		rdiobp.publishPort();
		
		this.doPortConnection(
				rdiobp.getPortURI(),
				rdURI+"-intro",
				RequestDispatcherIntrospectionConnector.class.getCanonicalName());
		
		requestDispatcherNotificationInboundPort = this.rdiobp.getRequestDispatcherPortsURI().get(RequestDispatcherPortTypes.REQUEST_NOTIFICATION);
		requestDispatcherManagementInboundPort = this.rdiobp.getRequestDispatcherPortsURI().get(RequestDispatcherPortTypes.MANAGEMENT);

		this.addOfferedInterface(NodeRingManagementI.class);
		this.cmip = new NodeManagementInboundPort(controllerManagement, this);
		this.cmip.publishPort();
		this.addPort(cmip);
		
		this.addRequiredInterface(ControlledDataOfferedI.ControlledPullI.class) ;
		this.rddsdop =
			new RequestDispatcherDynamicStateDataOutboundPort(requestDispatcherDynamicStateDataOutboundPort,this,rdURI) ;
		this.addPort(this.rddsdop) ;
		this.rddsdop.publishPort() ;
		

		this.rddsdop.doConnection(requestDispatcherDynamicStateDataInboundPortURI, ControlledDataConnector.class.getCanonicalName());
		this.rddsdop.startUnlimitedPushing(PUSH_INTERVAL);
		
		
		this.addRequiredInterface(ProcessorsControllerManagementI.class);
		this.pcmop = new ProcessorsControllerManagementOutboundPort("pcmop-"+this.controllerURI, this);
		this.pcmop.publishPort();
		this.pcmop.doConnection(ProcessorControllerManagementInboundUri, ProcessorControllerManagementConnector.class.getCanonicalName());
		
		
		this.addRequiredInterface(RingNetworkDynamicStateI.class);
		this.addOfferedInterface(RingNetworkDynamicStateI.class);
		
		
		rdsdop = new RingNetworkDynamicStateDataOutboundPort(this, RingDynamicStateDataOutboundPortURI);
		this.addPort(rdsdop);
		this.rdsdop.publishPort();
		this.rdsdop.doConnection(nextRingDynamicStateDataInboundPort, ControlledDataConnector.class.getCanonicalName());
		
		this.nextRingDynamicStateDataInboundPort = nextRingDynamicStateDataInboundPort;
		rdsdip=new RingNetworkDynamicStateDataInboundPort(RingDynamicStateDataInboundPortURI, this);
		this.addPort(rdsdip);
		this.rdsdip.publishPort();
		
		this.addRequiredInterface(RequestDispatcherManagementI.class);

		rdmop = new RequestDispatcherManagementOutboundPort(controllerURI + "-rdmop",
				this);

		this.addPort(rdmop);
		this.rdmop.publishPort();
		
		this.rdmop.doConnection(requestDispatcherManagementInboundPort, RequestDispatcherManagementConnector.class.getCanonicalName());

		this.freeApplicationVM = new ArrayList<ApplicationVMInfo>();
		this.vmReserved = new ArrayList<ApplicationVMInfo>();
		this.myVMs =  new ArrayList<ApplicationVMInfo>();
		this.cmops = new HashMap<String, ComputerControllerManagementOutboutPort>();
		this.addVm(vm);
		
		this.statistique = new HashMap<String, ArrayList<Double>>();
		
		/** Moyenne de toute les VMs **/
		this.statistique.put("All", new ArrayList<Double>());
		
		this.addOfferedInterface(VMDisconnectionNotificationHandlerI.class);
		this.vmnibp = new VMDisconnectionNotificationHandlerInboundPort(VMDisconnectionNotificationHandlerInboundPortURI,this);
		this.addPort(vmnibp);
		this.vmnibp.publishPort();
		
		
		this.controllerManagementPreviousInboundPort = controllerManagementPreviousPort;
		this.controllerManagementNextInboundPort = controllerManagementNextPort;
		this.startPushing();

	}
	
	private Double calculAverage(String VMUri) {
		ArrayList<Double> tmp = (ArrayList<Double>) this.statistique.get(VMUri).clone();
		Double average = 0.0;
		for(int i = 0; i < tmp.size(); i++) {
			average += tmp.get(i);
		}
		Double value =  average / tmp.size();
		tmp.clear();
		return value;
	}
	
	private Double calculAverage() {
		return calculAverage("All");
	}
	
	
	/** 
	 * @see fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces.RequestDispatcherStateDataConsumerI#acceptRequestDispatcherDynamicData(java.lang.String, fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces.RequestDispatcherDynamicStateI)
	 */
	@Override
	public void acceptRequestDispatcherDynamicData(String dispatcherURI,
			RequestDispatcherDynamicStateI currentDynamicState) throws Exception {
		//this.logMessage(String.format("[%s] Dispatcher Dynamic Data : %4.3f",dispatcherURI,currentDynamicState.getAvgExecutionTime()/1000000/1000));

		waitDecision++;
		if(currentDynamicState.getAvgExecutionTime() == null) {
			return;
		}

	    for (Entry<String, Double> entry : currentDynamicState.getVirtualMachineExecutionAverageTime().entrySet()) {
	    	if(entry.getValue() == null) {
	    		continue;
	    	}
	    	if(this.statistique.get(entry.getKey()) == null) {
	    		ArrayList<Double> tmp = new ArrayList<Double>();
	    		tmp.add(entry.getValue());
	    		this.statistique.put(entry.getKey(), tmp);
	    	}else {
	    		this.statistique.get(entry.getKey()).add(entry.getValue());
	    	}
	    }
	    
	    this.statistique.get("All").add(currentDynamicState.getAvgExecutionTime());
	    
		if((waitDecision % REQUEST_MIN) == 0) {
			processControl(currentDynamicState.getVirtualMachineDynamicStates());
			
			
			//On redonne les VMs au prochain controller.
			while(!vmReserved.isEmpty()) {
				synchronized (o) {
					freeApplicationVM.add(vmReserved.remove(0));
				}
			}
		}
	}
	
	/**
	 * @see fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces.RequestDispatcherStateDataConsumerI#acceptRequestDispatcherStaticData(java.lang.String, fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces.RequestDispatcherStaticStateI)
	 */
	@Override
	public void acceptRequestDispatcherStaticData(String dispatcherURI, RequestDispatcherStaticStateI staticState)
			throws Exception {
		// TODO Auto-generated method stub
		System.out.println("Dispatcher Static Data : ");
	}
	
	/**
	 * @see fr.upmc.components.AbstractComponent#shutdown()
	 */
	@Override
    public void shutdown() throws ComponentShutdownException {
        try {
            if (this.acmop.connected())
                this.acmop.doDisconnection();
            if(rddsdop.connected()) {
            	this.rddsdop.doDisconnection();
            }
            if(rdiobp.connected()) {
            	this.rdiobp.doDisconnection();
            }
            if(rdmop.connected()) {
            	this.rdmop.doDisconnection();
            }
        } catch (Exception e) {
            throw new ComponentShutdownException(e);
        }
        super.shutdown();
    }
    
    public enum Threeshold{
    	LOWER, HIGHER, GOOD
    }
    
	public Threeshold getThreeshold(Double time){
		if(isHigher(time))
			return Threeshold.HIGHER;
		else if(isLower(time)) {
			return Threeshold.LOWER;
		}else {
			return Threeshold.GOOD;
		}
	}

	private boolean isHigher(Double time){
		return Double.compare(time, (StaticData.TARGET_HIGHT)) == 1 ? true : false;
	}

	private boolean isLower(Double time){
		return Double.compare(time, (StaticData.TARGET_LOWER)) == -1 ? true : false;
	}
	
	private synchronized void processControl(Map<String, ApplicationVMDynamicStateI > vms) throws Exception {
		double factor=0;
		int number=0;
		double average = calculAverage();
		this.logMessage("" +average);
		this.logMessage("" + waitDecision);

		try {
			switch(getThreeshold(average)){
			case HIGHER :

				tooSlowCase(vms);
				//this.acmop.addCores(null, randomVM.getApplicationVMURI(), 1);
				break;
			case LOWER :
				tooFastCase(vms);
	
				break;
			case GOOD :
				System.err.println("WAS GOOD MY FRIEND");
				break;
			default:
				break;
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		w.write(Arrays.asList(""+average, ((Integer)vms.size()).toString()));

	}

	/**
	 * 
	 * @param vms
	 * @return
	 */
	private int getNumberOfCoresAllocatedFrom(Map<String, ApplicationVMDynamicStateI> vms) {
		int number = 0;
		for (Entry<String, ApplicationVMDynamicStateI> entry : vms.entrySet())
		{
		   number+= entry.getValue().getAllocatedCoresNumber().length;
		}
		return number;
	}

	/**
	 * Cas où les machines virtuelles sont trop lente.
	 * Nous devons donc augmenter la puissance du système responsable de la résolution de requêtes.
	 * 
	 * @param vms
	 * @throws Exception 
	 */
	private void tooFastCase(Map<String, ApplicationVMDynamicStateI > vms) throws Exception {
		
		//Add a vm
		/*if(!vmReserved.isEmpty())
			this.addVM(vmReserved.remove(0));
		*/
		ApplicationVMDynamicStateI randomVM = vms.get(vms.keySet().iterator().next());
		
		if(!vmReserved.isEmpty()) {
			this.addVm(vmReserved.remove(0));
		}
		//Try to up frequency
		int nbCoreFrequencyChange = setCoreFrequency(CoreAsk.HIGHER, randomVM);
		System.err.println("je passe par lower mdr");

		
		//System.err.println("!!!!!!!!! " +this.cmops.get(randomVM.getApplicationVMURI()).reserveCore(randomVM.getApplicationVMURI()));
	}

	/**
	 * Cas où les machines virtuelles vont trop vite.
	 * Nous devons donc baisser la puissance du système responsable de la résolution de requêtes.
	 * 
	 * @param vms
	 * @throws Exception 
	 */
	private void tooSlowCase(Map<String, ApplicationVMDynamicStateI > vms) throws Exception {
		
		boolean canRemoveVM = vms.size() > 1;
		//boolean canDesalocate = coresAllocates  == StaticData.MIN_ALLOCATION;
		

		if(canRemoveVM) {
			ApplicationVMDynamicStateI randomVM = vms.get(vms.keySet().iterator().next());
			this.rdmop.askVirtualMachineDisconnection(randomVM.getApplicationVMURI());
			System.err.println("remove a vm");
		}
		
		
		//System.err.println("!!!!!!!!! " +this.cmops.get(randomVM.getApplicationVMURI()).reserveCore(randomVM.getApplicationVMURI()));

		
		//Try to lower frequency
		//int nbCoreFrequencyChange = setCoreFrequency(CoreAsk.LOWER, randomVM);
		
	}
	
	private int setCoreFrequency(CoreAsk ask, ApplicationVMDynamicStateI vm){
		this.logMessage("Try to " + ask.toString() + " for " + vm.getApplicationVMURI());
		int nb = 0;
		for(int i = 0; i < vm.getAllocatedCoresNumber().length; i++) {
			try {
				nb +=  (pcmop.setCoreFrequency(ask, vm.getProcessorURI(), vm.getAllocatedCoresNumber()[i])) ? 1 : 0;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		this.logMessage(nb + " cores was set for " + vm.getApplicationVMURI());

		return nb;
	}
	
	static class StaticData {
		public static final double AVERAGE_TARGET=1E9D;
		
		public static final double VERRY_MUCH_LOWER_PERCENT= 0.5;
		public static final double LOWER_PERCENT= 0.25;
		public static final double HIGHER_PERCENT= 0.25;
		public static final double VERY_MUCH_HIGHER_PERCENT=0.5;

		public static final double TARGET_VERY_HIGHT = AVERAGE_TARGET * VERY_MUCH_HIGHER_PERCENT + AVERAGE_TARGET;
		public static final double TARGET_HIGHT = AVERAGE_TARGET * HIGHER_PERCENT + AVERAGE_TARGET;
		public static final double TARGET_LOWER = AVERAGE_TARGET * LOWER_PERCENT;
		public static final double TARGET_VERY_LOWER = AVERAGE_TARGET * LOWER_PERCENT;

		public static int DISPATCHER_PUSH_INTERVAL=5000;
		public static int NB_VM_RESERVED = 1;
		//Max core
		public static int MAX_ALLOCATION=25;
		
		public static int MIN_ALLOCATION = 2;

	}

	/**
	 * @see fr.upmc.PriseTheSun.datacenter.software.ring.interfaces.RingNetworkStateDataConsumerI#acceptRingNetworkDynamicData(java.lang.String, fr.upmc.PriseTheSun.datacenter.software.ring.interfaces.RingNetworkDynamicStateI)
	 */
	@Override
	public void acceptRingNetworkDynamicData(String controllerDataRingOutboundPortURI, RingNetworkDynamicStateI currentDynamicState)
			throws Exception {;
		synchronized(o){
			ApplicationVMInfo vm =  currentDynamicState.getApplicationVMInfo();
			if(vm != null) {
				if(vmReserved.size() < StaticData.NB_VM_RESERVED) {
					vmReserved.add(vm);
				}
				else {
					freeApplicationVM.add(vm);
				}
			}
		}
	}
	
	
	/**
	 * @see fr.upmc.datacenter.interfaces.PushModeControllingI#startUnlimitedPushing(int)
	 */
	@Override
	public void startUnlimitedPushing(int interval) throws Exception {
		// first, send the static state if the corresponding port is connected
		//this.sendStaticState() ;
		final Controller c = this ;
		this.pushingFuture =
				this.scheduleTaskAtFixedRate(
						new ComponentI.ComponentTask() {
							@Override
							public void run() {
								try {
									c.sendDynamicState() ;
								} catch (Exception e) {
									e.printStackTrace();
									throw new RuntimeException(e) ;
								}
							}
						}, interval, interval, TimeUnit.MILLISECONDS) ;
	}

	
	/**
	 * @see fr.upmc.datacenter.interfaces.PushModeControllingI#startLimitedPushing(int, int)
	 */
	@Override
	public void startLimitedPushing(final int interval, final int n) throws Exception {
		assert	n > 0 ;
		this.logMessage(this.controllerURI + " startLimitedPushing with interval "
				+ interval + " ms for " + n + " times.") ;

		// first, send the static state if the corresponding port is connected
		//this.sendStaticState() ;

		final Controller c = this ;
		this.pushingFuture =
				this.scheduleTask(
						new ComponentI.ComponentTask() {
							@Override
							public void run() {
								try {
									c.sendDynamicState(interval, n) ;
								} catch (Exception e) {
									throw new RuntimeException(e) ;
								}
							}
						}, interval, TimeUnit.MILLISECONDS) ;
	}

	public void	sendDynamicState() throws Exception
	{
		//System.out.println(this.controllerURI + " rdsip is connected " + this.rdsdip.connected());
		if (this.rdsdip.connected()) {
			RingNetworkDynamicStateI rds = this.getDynamicState() ;
			this.rdsdip.send(rds) ;
		}
	}

	
	public void	sendDynamicState(
			final int interval,
			int numberOfRemainingPushes) throws Exception{
		this.sendDynamicState() ;
		final int fNumberOfRemainingPushes = numberOfRemainingPushes - 1 ;
		if (fNumberOfRemainingPushes > 0) {
			final Controller c = this ;
			this.pushingFuture =
					this.scheduleTask(
							new ComponentI.ComponentTask() {
								@Override
								public void run() {
									try {
										c.sendDynamicState(
												interval,
												fNumberOfRemainingPushes) ;
									} catch (Exception e) {
										e.printStackTrace();
										throw new RuntimeException(e) ;
									}
								}
							}, interval, TimeUnit.MILLISECONDS) ;
		}
	}


	
	/**
	 * @see fr.upmc.datacenter.interfaces.PushModeControllingI#stopPushing()
	 */
	@Override
	public void stopPushing() throws Exception {
		if (this.pushingFuture != null &&
				!(this.pushingFuture.isCancelled() ||
						this.pushingFuture.isDone())) {
			this.pushingFuture.cancel(false) ;
		}
	}
	

	public RingDynamicState getDynamicState() throws UnknownHostException {
		ApplicationVMInfo removed = null;
		synchronized(o){
			if(!this.freeApplicationVM.isEmpty()) {
				removed = this.freeApplicationVM.remove(0);
			}
		}
		return new RingDynamicState(removed);
	}
	
	//TODO  WHY SYNCHRO DESU
	public synchronized void addVm(ApplicationVMInfo vm){
		
		// Create a mock up port to manage the AVM component (allocate cores).
		ApplicationVMManagementOutboundPort avmPort;
		int id = ++idVm;

		try {
			avmPort = new ApplicationVMManagementOutboundPort(
					"avmop"+"-" + controllerURI+id, this);
			avmPort.publishPort() ;
			avmPort.doConnection(vm.getAvmInbound(),
						ApplicationVMManagementConnector.class.getCanonicalName());
			rdmop.connectVirtualMachine(vm.getApplicationVM(), vm.getSubmissionInboundPortUri());
			avmPort.connectWithRequestSubmissioner(rdUri, requestDispatcherNotificationInboundPort);
			this.myVMs.add(vm);
			
			
			ComputerControllerManagementOutboutPort ccmop = new ComputerControllerManagementOutboutPort("ComputerControllerManagementOutboutPort" + cmops.size(), this);
			
	         this.addPort(ccmop);
				
				ccmop.publishPort();
				ccmop.doConnection(
						vm.getComputerManagementInboundPortURI(),
						ComputerControllerConnector.class.getCanonicalName());
				
				
				this.cmops.put(vm.getApplicationVM(), ccmop);			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void receiveVMDisconnectionNotification(String vmURI) throws Exception {
		System.err.println("Receive a vm disconnected" + this.myVMs.size());
		for(int i = 0; i < this.myVMs.size(); i++) {
			if(myVMs.get(i).getApplicationVM().equals(vmURI)) {
				freeApplicationVM.add(myVMs.remove(i));
				this.cmops.remove(vmURI);
				return;
			}
		}
		throw new Exception("Vm was not found. Can't delete.");
	}

	/**
	 * @see fr.upmc.PriseTheSun.datacenter.software.controller.interfaces.VMDisconnectionNotificationHandlerI#disconnectController()
	 */
	@Override
	public void disconnectController() throws Exception {
		System.err.println("tentative de déconnexion..");
		NodeManagementOutboundPort cmopPrevious = new NodeManagementOutboundPort("cmop-previous-"+this.controllerURI, this);
		cmopPrevious.publishPort();
		cmopPrevious.doConnection(controllerManagementPreviousInboundPort, NodeManagementConnector.class.getCanonicalName());
		
		// On arrête le push 
		cmopPrevious.stopPushing();

		// On raccorde les ports de managements
		cmopPrevious.setNextManagementInboundPort(controllerManagementNextInboundPort);
		cmopPrevious.bindSendingDataUri(this.nextRingDynamicStateDataInboundPort);
		cmopPrevious.startPushing();
		
		
		NodeManagementOutboundPort cmopNext = new NodeManagementOutboundPort("cmop-next-"+this.controllerURI, this);
		cmopNext.publishPort();
		cmopNext.doConnection(controllerManagementNextInboundPort, NodeManagementConnector.class.getCanonicalName());
		cmopNext.setPreviousManagementInboundPort(controllerManagementPreviousInboundPort);
		
		cmopPrevious.doDisconnection();
		cmopNext.doDisconnection();
		this.stopPushing();
		if(this.rdsdop.connected()) {
			this.logMessage("Disconnect " + this.controllerURI + " of the ring" );
			System.err.println("je te kill");
			this.rdsdop.doDisconnection();
		}
	}
	
	@Override
	public void bindSendingDataUri(String DataInboundPortUri) throws Exception {
		if(rdsdop.connected())
			rdsdop.doDisconnection();
		rdsdop.doConnection(DataInboundPortUri, ControlledDataConnector.class.getCanonicalName());
		System.out.println("reconnecting?");
	}
	@Override
	public void setNextManagementInboundPort(String managementInboundPort) throws Exception {
		this.controllerManagementNextInboundPort = managementInboundPort;
	}

	@Override
	public void setPreviousManagementInboundPort(String managementInboundPort) throws Exception {
		this.controllerManagementPreviousInboundPort = managementInboundPort;
	}

	@Override
	public void startPushing() throws Exception {
		this.startUnlimitedPushing(RingDynamicState.RING_INTERVAL_TIME);
	}
}

