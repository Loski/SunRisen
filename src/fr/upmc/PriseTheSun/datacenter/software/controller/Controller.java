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
import fr.upmc.PriseTheSun.datacenter.software.controller.connectors.ControllerManagementConnector;
import fr.upmc.PriseTheSun.datacenter.software.controller.interfaces.ControllerRingManagementI;
import fr.upmc.PriseTheSun.datacenter.software.controller.interfaces.VMDisconnectionNotificationHandlerI;
import fr.upmc.PriseTheSun.datacenter.software.controller.ports.ControllerManagementInboundPort;
import fr.upmc.PriseTheSun.datacenter.software.controller.ports.ControllerManagementOutboundPort;
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
			ControllerRingManagementI, 
			PushModeControllingI,
			VMDisconnectionNotificationHandlerI
{

	protected String controllerURI;
	protected String rdUri;
	
	protected AdmissionControllerManagementOutboundPort acmop;
	protected RequestDispatcherDynamicStateDataOutboundPort rddsdop;
	protected ProcessorsControllerManagementOutboundPort pcmop;

	private ScheduledFuture<?> pushingFuture;
	private ControllerManagementInboundPort cmip;
	
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
			String AdmissionControllerManagementInboundPortURI, 
			String ProcessorControllerManagementInboundUri, 
			String RingDynamicStateDataOutboundPortURI, 
			String RingDynamicStateDataInboundPortURI, 
			String nextRingDynamicStateDataInboundPort,
			String controllerManagementPreviousPort,
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

		this.addOfferedInterface(ControllerRingManagementI.class);
		this.cmip = new ControllerManagementInboundPort(controllerManagement, this);
		this.cmip.publishPort();
		this.addPort(cmip);
		
		this.addRequiredInterface(ControlledDataOfferedI.ControlledPullI.class) ;
		this.rddsdop =
			new RequestDispatcherDynamicStateDataOutboundPort(requestDispatcherDynamicStateDataOutboundPort,this,rdURI) ;
		this.addPort(this.rddsdop) ;
		this.rddsdop.publishPort() ;
		
		this.addRequiredInterface(AdmissionControllerManagementI.class);
		this.acmop = new AdmissionControllerManagementOutboundPort("acmop-"+this.controllerURI, this);
		this.acmop.publishPort();
		this.acmop.doConnection(AdmissionControllerManagementInboundPortURI, AdmissionControllerManagementConnector.class.getCanonicalName());
		
		this.rddsdop.doConnection(requestDispatcherDynamicStateDataInboundPortURI, ControlledDataConnector.class.getCanonicalName());
		this.rddsdop.startUnlimitedPushing(1000);
		
		
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
		this.startUnlimitedPushing(100);
		
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
	}
	
	private Double calculAverage(String VMUri) {
		ArrayList<Double> tmp = this.statistique.get(VMUri);
		Double average = 0.0;
		for(int i = 0; i < tmp.size(); i++) {
			average += tmp.get(i);
		}
		return average / tmp.size();
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
		
		waitDecision++;
		
	    for (Entry<String, Double> entry : currentDynamicState.getVirtualMachineExecutionAverageTime().entrySet()) {
	    	if(this.statistique.get(entry.getKey()) == null) {
	    		ArrayList<Double> tmp = new ArrayList<Double>();
	    		tmp.add(entry.getValue());
	    		this.statistique.put(entry.getKey(), tmp);
	    	}else {
	    		this.statistique.get(entry.getKey()).add(entry.getValue());
	    	}
	    }
	    
	    this.statistique.get("All").add(currentDynamicState.getAvgExecutionTime());
	    
		if(currentDynamicState.getAvgExecutionTime()!=null) {
			this.logMessage(String.format("[%s] Dispatcher Dynamic Data : %4.3f",dispatcherURI,currentDynamicState.getAvgExecutionTime()/1000000/1000));
			processControl(currentDynamicState.getAvgExecutionTime(), currentDynamicState.getVirtualMachineDynamicStates());
			//On redonne les VMs au prochain controller.
			while(!vmReserved.isEmpty()) {
				synchronized (o) {
					freeApplicationVM.add(vmReserved.remove(0));
				}
			}
		}
		else {
		//	this.logMessage(String.format("[%s] Dispatcher Dynamic Data : %s",dispatcherURI,"pas assez de données pour calculer la moyenne"));
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
		return Threeshold.HIGHER;
	}

	private boolean isHigher(Double time){
		return Double.compare(time, (StaticData.AVERAGE_TARGET*StaticData.HIGHER_PERCENT + StaticData.AVERAGE_TARGET)) == 1 ? true : false;
	}

	private boolean isLower(Double time){
		return Double.compare(time, (StaticData.AVERAGE_TARGET*StaticData.LOWER_PERCENT - StaticData.AVERAGE_TARGET)) == -1 ? true : false;
	}
	
	private synchronized void processControl(Double time, Map<String, ApplicationVMDynamicStateI > vms) throws Exception {
		double factor=0;
		int number=0;
		System.out.println(time);
		Integer coresAllocates = getNumberOfCoresAllocatedFrom(vms);
		
		
		try {
			switch(getThreeshold(time)){
			case HIGHER :
				factor = (time/StaticData.AVERAGE_TARGET);
				number = Math.max(1, (int)(coresAllocates*factor));
				number = Math.min(StaticData.MAX_ALLOCATION, number);
				
	
				HighterCase(vms, coresAllocates);
				//this.acmop.addCores(null, randomVM.getApplicationVMURI(), 1);
				break;
			case LOWER :
				factor = (StaticData.AVERAGE_TARGET/time);
				number =Math.max(1, (int)(coresAllocates-(coresAllocates/factor)));
				number =Math.min(StaticData.MIN_ALLOCATION, number);
	
			//	processDeallocate(factor,number,vms,double1,nbreq,cores);
				
				// add Reset request stat?
	
				break;
			case GOOD :
				break;
			default:
				break;
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		w.write(Arrays.asList(coresAllocates.toString(), ((Integer)vms.size()).toString()));

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
	 * Cas où les machines virtuelles ne vont pas assez vite.
	 * Nous devons donc augmenter la puissance du système responsable de la résolution de requêtes.
	 * 
	 * @param vms
	 * @throws Exception 
	 */
	private void LowerCase(Map<String, ApplicationVMDynamicStateI > vms, int coresAllocates) throws Exception {
		
		//Add a vm
		/*if(!vmReserved.isEmpty())
			this.addVM(vmReserved.remove(0));
		*/
		ApplicationVMDynamicStateI randomVM = vms.get(vms.keySet().iterator().next());
		
		
		//Try to up frequency
		int nbCoreFrequencyChange = setCoreFrequency(CoreAsk.HIGHER, randomVM);
		
		
		//System.err.println("!!!!!!!!! " +this.cmops.get(randomVM.getApplicationVMURI()).reserveCore(randomVM.getApplicationVMURI()));
	}

	/**
	 * Cas où les machines virtuelles ne vont pas assez vite.
	 * Nous devons donc augmenter la puissance du système responsable de la résolution de requêtes.
	 * 
	 * @param vms
	 * @throws Exception 
	 */
	private void HighterCase(Map<String, ApplicationVMDynamicStateI > vms, int coresAllocates) throws Exception {
		
		boolean canRemoveVM = vms.size()== 1;
		boolean canDesalocate = coresAllocates  == StaticData.MIN_ALLOCATION;
		
		
		/*if(!canRemoveVM && !canDesalocate) {
			this.logMessage("Can't lower anymore...");
			return;
		}*/
		
		
		//Add a VM
		if(!vmReserved.isEmpty())
		this.addVm(vmReserved.remove(0));

		ApplicationVMDynamicStateI randomVM = vms.get(vms.keySet().iterator().next());
		
		
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
		public static long AVERAGE_TARGET=2500;
		public static double LOWER_PERCENT=0.5;
		public static double HIGHER_PERCENT=0.3;
		public static int DISPATCHER_PUSH_INTERVAL=5000;
		public static int NB_VM_RESERVED = 5;
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

	@Override
	public void bindSendingDataUri(String DataInboundPortUri) throws Exception {
		if(rdsdop.connected())
			rdsdop.doDisconnection();
		rdsdop.doConnection(DataInboundPortUri, ControlledDataConnector.class.getCanonicalName());
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
		ControllerManagementOutboundPort cmopPrevious = new ControllerManagementOutboundPort("cmop-previous-"+this.controllerURI, this);
		cmopPrevious.publishPort();
		cmopPrevious.doConnection(controllerManagementPreviousInboundPort, ControllerManagementConnector.class.getCanonicalName());
		
		// On arrête le push 
		cmopPrevious.stopPushing();

		// On raccorde les ports de managements
		cmopPrevious.setNextManagementInboundPort(controllerManagementNextInboundPort);
		cmopPrevious.bindSendingDataUri(this.nextRingDynamicStateDataInboundPort);

		
		
		ControllerManagementOutboundPort cmopNext = new ControllerManagementOutboundPort("cmop-next-"+this.controllerURI, this);
		cmopNext.publishPort();
		cmopNext.doConnection(controllerManagementNextInboundPort, ControllerManagementConnector.class.getCanonicalName());
		cmopNext.setPreviousManagementInboundPort(controllerManagementPreviousInboundPort);
		
		cmopPrevious.doDisconnection();
		cmopPrevious.destroyPort();
		cmopNext.doDisconnection();
		cmopNext.destroyPort();
	}

	@Override
	public void setNextManagementInboundPort(String managementInboundPort) throws Exception {
		this.controllerManagementNextInboundPort = managementInboundPort;
	}

	@Override
	public void setPreviousManagementInboundPort(String managementInboundPort) throws Exception {
		this.controllerManagementPreviousInboundPort = managementInboundPort;
	}
}

