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

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

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
import fr.upmc.PriseTheSun.datacenter.software.controller.interfaces.ControllerManagementI;
import fr.upmc.PriseTheSun.datacenter.software.controller.ports.ControllerManagementInboundPort;
import fr.upmc.PriseTheSun.datacenter.software.controller.ports.ControllerManagementOutboundPort;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.RequestDispatcher;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.RequestDispatcher.RequestDispatcherPortTypes;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.VirtualMachineData;
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
import fr.upmc.PriseTheSun.datacenter.software.ring.interfaces.RingDataI;
import fr.upmc.PriseTheSun.datacenter.software.ring.interfaces.RingDynamicStateI;
import fr.upmc.PriseTheSun.datacenter.software.ring.ports.RingDynamicStateDataInboundPort;
import fr.upmc.PriseTheSun.datacenter.software.ring.ports.RingDynamicStateDataOutboundPort;
import fr.upmc.PriseTheSun.datacenter.tools.Writter;
import fr.upmc.components.AbstractComponent;
import fr.upmc.components.ComponentI;
import fr.upmc.datacenter.connectors.ControlledDataConnector;
import fr.upmc.datacenter.hardware.processors.UnacceptableFrequencyException;
import fr.upmc.datacenter.hardware.processors.UnavailableFrequencyException;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.datacenter.interfaces.ControlledDataOfferedI;
import fr.upmc.datacenter.interfaces.PushModeControllingI;
import fr.upmc.datacenter.software.applicationvm.connectors.ApplicationVMIntrospectionConnector;
import fr.upmc.datacenter.software.applicationvm.connectors.ApplicationVMManagementConnector;
import fr.upmc.datacenter.software.applicationvm.interfaces.ApplicationVMDynamicStateI;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMIntrospectionOutboundPort;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.datacenter.software.interfaces.RequestNotificationI;
import fr.upmc.datacenter.software.ports.RequestNotificationOutboundPort;



public class Controller extends AbstractComponent implements RequestDispatcherStateDataConsumerI, RingDataI,ControllerManagementI, PushModeControllingI{

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
	private List<ApplicationVMInfo> vmFree;
	 
	private List<ApplicationVMInfo> myVMs;
	
	private Map<String, ComputerControllerManagementOutboutPort> cmops;
	
	private RingDynamicStateDataOutboundPort rdsdop;
	private RingDynamicStateDataInboundPort rdsdip;
	
	
	private String appURI; 	
	private Writter w;
	private Object o = new Object();

	
	public Controller(String appURI, String controllerURI, String controllerManagement, String requestDispatcherDynamicStateDataOutboundPort,String rdURI, String requestDispatcherDynamicStateDataInboundPortURI, String AdmissionControllerManagementInboundPortURI, String ProcessorControllerManagementInboundUri, String RingDynamicStateDataOutboundPortURI, String RingDynamicStateDataInboundPortURI, String nextRingDynamicStateDataInboundPort, ApplicationVMInfo vm) throws Exception
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

		this.addRequiredInterface(ControllerManagementI.class);
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
		
		
		this.addRequiredInterface(RingDynamicStateI.class);
		this.addOfferedInterface(RingDynamicStateI.class);
		
		
		rdsdop = new RingDynamicStateDataOutboundPort(this, RingDynamicStateDataOutboundPortURI);
		this.addPort(rdsdop);
		this.rdsdop.publishPort();
		this.rdsdop.doConnection(nextRingDynamicStateDataInboundPort, ControlledDataConnector.class.getCanonicalName());
		this.startUnlimitedPushing(100);
		
		
		rdsdip=new RingDynamicStateDataInboundPort(RingDynamicStateDataInboundPortURI, this);
		this.addPort(rdsdip);
		this.rdsdip.publishPort();
		
		this.addRequiredInterface(RequestDispatcherManagementI.class);

		rdmop = new RequestDispatcherManagementOutboundPort(controllerURI + "-rdmop",
				this);

		this.addPort(rdmop);
		this.rdmop.publishPort();
		
		this.rdmop.doConnection(requestDispatcherManagementInboundPort, RequestDispatcherManagementConnector.class.getCanonicalName());

		this.vmFree = new ArrayList<ApplicationVMInfo>();
		this.vmReserved = new ArrayList<ApplicationVMInfo>();
		this.myVMs =  new ArrayList<ApplicationVMInfo>();
		this.cmops = new HashMap<String, ComputerControllerManagementOutboutPort>();
		this.addVm(vm);
	}
	
	@Override
	public void acceptRequestDispatcherDynamicData(String dispatcherURI,
			RequestDispatcherDynamicStateI currentDynamicState) throws Exception {
		
		waitDecision++;
		if(currentDynamicState.getAvgExecutionTime()!=null) {
			this.logMessage(String.format("[%s] Dispatcher Dynamic Data : %4.3f",dispatcherURI,currentDynamicState.getAvgExecutionTime()/1000000/1000));
			processControl(currentDynamicState.getAvgExecutionTime(), currentDynamicState.getVirtualMachineDynamicStates());
			//On redonne les VMs au prochain controller.
			while(!vmReserved.isEmpty()) {
				synchronized (o) {
					vmFree.add(vmReserved.remove(0));
				}
			}

		}
		else {
		//	this.logMessage(String.format("[%s] Dispatcher Dynamic Data : %s",dispatcherURI,"pas assez de données pour calculer la moyenne"));
		}

	}
	@Override
	public void acceptRequestDispatcherStaticData(String dispatcherURI, RequestDispatcherStaticStateI staticState)
			throws Exception {
		// TODO Auto-generated method stub
		System.out.println("Dispatcher Static Data : ");
	}
	
	@Override
    public void shutdown() throws ComponentShutdownException {
        try {
            if (this.acmop.connected())
                this.acmop.doDisconnection();
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
		
		
		System.err.println("!!!!!!!!! " +this.cmops.get(randomVM.getApplicationVMURI()).reserveCore(randomVM.getApplicationVMURI()));
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
		this.addVm(vmReserved.remove(0));

		ApplicationVMDynamicStateI randomVM = vms.get(vms.keySet().iterator().next());
		
		
		System.err.println("!!!!!!!!! " +this.cmops.get(randomVM.getApplicationVMURI()).reserveCore(randomVM.getApplicationVMURI()));

		
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

	@Override
	public void acceptRingDynamicData(String requestDispatcherURI, RingDynamicStateI currentDynamicState)
			throws Exception {;
		//this.logMessage(this.controllerURI + " a re�u " + currentDynamicState.getApplicationVMsInfo().size() + "vms");
		synchronized(o){
			if(!currentDynamicState.getApplicationVMsInfo().isEmpty()) {
				//this.logMessage(this.controllerURI + " a re�u " + currentDynamicState.getApplicationVMsInfo().size() + "vms, will now try to reserve " + StaticData.NB_VM_RESERVED + " vms");
				vmFree.addAll(currentDynamicState.getApplicationVMsInfo());
				while(vmReserved.size() < + StaticData.NB_VM_RESERVED && !vmFree.isEmpty()) {
					vmReserved.add(vmFree.remove(0));
				}
			}
		}
	}
	
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
			RingDynamicStateI rds = this.getDynamicState() ;
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
	public void stopPushing() throws Exception {
		if (this.pushingFuture != null &&
				!(this.pushingFuture.isCancelled() ||
						this.pushingFuture.isDone())) {
			this.pushingFuture.cancel(false) ;
		}
	}
	
	public RingDynamicState getDynamicState() throws UnknownHostException {
		synchronized(o){
			ArrayList<ApplicationVMInfo> copy= new ArrayList<>(vmFree);
			RingDynamicState rds = new RingDynamicState(copy);
			//Suppression car envoie les vms appartiennent aux controller suivant
			vmFree.clear();
			return rds;
		}
	}

	@Override
	public void bindSendingDataUri(String DataInboundPortUri) throws Exception {
		if(rdsdop.connected())
			rdsdop.doDisconnection();
		rdsdop.doConnection(DataInboundPortUri, ControlledDataConnector.class.getCanonicalName());
	}
	
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
	
	public void noticeMeSempai(String vmURI) throws Exception {
		for(int i = 0; i < this.myVMs.size(); i++) {
			if(myVMs.get(i).getApplicationVM().equals(vmURI)) {
				vmFree.add(myVMs.remove(i));
				this.cmops.remove(vmURI);
				return;
			}
		}
		throw new Exception("Vm was not found. Can't delete.");
	}
}

