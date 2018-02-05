package fr.upmc.PriseTheSun.datacenter.software.controller;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import fr.upmc.PriseTheSun.datacenter.hardware.computer.connector.ComputerControllerConnector;
import fr.upmc.PriseTheSun.datacenter.hardware.computer.interfaces.ComputerControllerManagementI;
import fr.upmc.PriseTheSun.datacenter.hardware.computer.ports.ComputerControllerManagementOutboutPort;
import fr.upmc.PriseTheSun.datacenter.hardware.processors.ProcessorsController.CoreAsk;
import fr.upmc.PriseTheSun.datacenter.hardware.processors.connector.ProcessorControllerManagementConnector;
import fr.upmc.PriseTheSun.datacenter.hardware.processors.interfaces.ProcessorsControllerManagementI;
import fr.upmc.PriseTheSun.datacenter.hardware.processors.ports.ProcessorsControllerManagementOutboundPort;
import fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.AdmissionControllerDynamic;
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
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces.RequestDispatcherIntrospectionI;
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
import fr.upmc.components.exceptions.ComponentStartException;
import fr.upmc.datacenter.connectors.ControlledDataConnector;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;
import fr.upmc.datacenter.interfaces.ControlledDataOfferedI;
import fr.upmc.datacenter.interfaces.ControlledDataRequiredI;
import fr.upmc.datacenter.interfaces.PushModeControllingI;
import fr.upmc.datacenter.software.applicationvm.connectors.ApplicationVMManagementConnector;
import fr.upmc.datacenter.software.applicationvm.interfaces.ApplicationVMDynamicStateI;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;


/**
 * Un <code>Controller</code> est responsable de vérifier si les requêtes sont effectués dans un temps normal indiqué dans un temps convenable
 * indiqué dans <code>StaticData</code> selon différent seuil. 
 * 
 * Le <code>Controller</code> observe les données de <code>RequestDispatcher</code>. Il prends une action toutes les <code>REQUEST_MIN</code>.
 * Enfin, il lisse les données de deux manières : lissage exponentielle et suppression des données périmées aka fait depuis <code>StaticData.minute</code>.
 *De plus, il implémente l'interface <code>NodeRingManagementI</code> pour implémenter le comportement d'un node d'un data ring network.
 * @author Maxime Lavaste
 * 
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
	
	protected RequestDispatcherDynamicStateDataOutboundPort rddsdop;

	private ScheduledFuture<?> pushingFuture;
	private NodeManagementInboundPort cmip;
	
	private RequestDispatcherIntrospectionOutboundPort rdiobp;

	private String requestDispatcherNotificationInboundPort;
	private String requestDispatcherManagementInboundPort;
	private RequestDispatcherManagementOutboundPort rdmop;
	
	int idVm = 0;
	int waitDecision = 0;
	boolean needVM;
	/**Max de mauvaises information du capteur de vélocité de requêtage*/
	private int compteur_null = 0;
	/** VMs réservées au prochain tour d'allocation */
	private List<ApplicationVMInfo> vmReserved;
	/** Vms a propagées au prochain node **/ 
	private List<ApplicationVMInfo> freeApplicationVM;
	 /** VMs connectées au dispatcher pour résoudre les requêtes **/
	private List<ApplicationVMInfo> myVMs;
	
	/** VMs en attentes de déconnexion **/
	private HashMap<String, ApplicationVMInfo> VMsToBeKilled;
		
	/** Ring network outbound port **/
	private RingNetworkDynamicStateDataOutboundPort rdsdop;
	/** Ring network inbound port **/
	private RingNetworkDynamicStateDataInboundPort rdsdip;
	
	/** Ring Network Management port of next node**/
	private String controllerManagementNextInboundPort;
	/** Ring Network Management port of previous node**/
	private String controllerManagementPreviousInboundPort;


	
	/** Uri d'une VM vers le controller de son computer */
	private Map<String, ComputerControllerManagementOutboutPort> cmops;
	
	/** Uri d'une VM vers son port Out de management */
	private Map<String, ApplicationVMManagementOutboundPort> avms;

	private String appURI; 	
	private Writter w;

	private Map<String, List<Mesure>> statistique;
	
	private VMDisconnectionNotificationHandlerInboundPort vmnibp;
	private String nextRingDynamicStateDataInboundPort;
	
	public final static int PUSH_INTERVAL = 1000;
	public final static int REQUEST_MIN = PUSH_INTERVAL/100;
	public static final long TIME_MAX = 60000l;
	public static final int NB_VM_RESERVED = 2;

	public  int very_slow_compteur  = 0;

	
	
	static class TargetData {
		public static final double AVERAGE_TARGET=5E9D;
		
		public static final double VERY_FAST_PERCENT_LIMIT=0.78;
		public static final double FASTER_PERCENT_LIMIT=0.85;
		public static final double SLOWER_PERCENT_LIMIT=1.15;
		public static final double VERY_SLOW_PERCENT_LIMIT=1.22;
		public static final double TARGET_VERY_SLOW = AVERAGE_TARGET * VERY_SLOW_PERCENT_LIMIT;
		public static final double TARGET_SLOW = AVERAGE_TARGET * SLOWER_PERCENT_LIMIT;
		public static final double TARGET_FAST = AVERAGE_TARGET * FASTER_PERCENT_LIMIT;
		public static final double TARGET_VERY_FAST = AVERAGE_TARGET * VERY_FAST_PERCENT_LIMIT;


		public static final int MAX_NULL = 60;

		public static final int MAX_VM = 8;

		public static final int MIN_ALLOCATED_CORE = 2;
	}
	
	
	public Controller(
			String appURI, 
			String controllerURI, 
			String controllerManagement, 
			String requestDispatcherDynamicStateDataOutboundPort,
			String rdURI, 
			String requestDispatcherDynamicStateDataInboundPortURI, 
			String ADMManagementInboundPort,
			String RingDynamicStateDataOutboundPortURI, 
			String RingDynamicStateDataInboundPortURI, 
			String nextRingDynamicStateDataInboundPort,
			String controllerManagementPreviousPort,
			String controllerManagementNextPort,
			ApplicationVMInfo vm, 
			String VMDisconnectionNotificationHandlerInboundPortURI
	) throws Exception
	{
		super(controllerURI,1 ,1);

		assert appURI !=null;
		assert controllerURI != null;
		assert controllerManagement != null;
		assert rdURI !=null;
		assert requestDispatcherDynamicStateDataInboundPortURI !=null;
		assert ADMManagementInboundPort !=null;
		assert RingDynamicStateDataOutboundPortURI !=null;
		assert RingDynamicStateDataInboundPortURI !=null;
		assert nextRingDynamicStateDataInboundPort != null;
		assert controllerManagementNextPort != null;
		assert controllerManagementPreviousPort !=null;
		assert vm != null && vm.getApplicationVM() != null;
		
		this.controllerURI = controllerURI;
		this.rdUri = rdURI;
		this.appURI = appURI;
		System.err.println("controller !!! " +this.appURI);

		this.addOfferedInterface(NodeRingManagementI.class);
		this.addOfferedInterface(ControlledDataOfferedI.ControlledPullI.class);
		this.addOfferedInterface(VMDisconnectionNotificationHandlerI.class);
		
		this.addRequiredInterface(NodeRingManagementI.class);
		this.addRequiredInterface(ControlledDataRequiredI.ControlledPullI.class);
		this.addRequiredInterface(RequestDispatcherManagementI.class);
		this.addRequiredInterface(RequestDispatcherIntrospectionI.class);
		this.addRequiredInterface(ComputerControllerManagementI.class);
		
		this.toggleLogging();
		this.toggleTracing();
		w = new Writter(controllerURI+ ".csv");
		

		this.rdiobp = new RequestDispatcherIntrospectionOutboundPort( rdURI+"-introObp", this );
		
		this.addPort( rdiobp );
		rdiobp.publishPort();
		this.doPortConnection(
				rdiobp.getPortURI(),
				rdURI+"-intro",
				RequestDispatcherIntrospectionConnector.class.getCanonicalName());
		
		requestDispatcherNotificationInboundPort = this.rdiobp.getRequestDispatcherPortsURI().get(RequestDispatcherPortTypes.REQUEST_NOTIFICATION);
		requestDispatcherManagementInboundPort = this.rdiobp.getRequestDispatcherPortsURI().get(RequestDispatcherPortTypes.MANAGEMENT);

		this.cmip = new NodeManagementInboundPort(controllerManagement, this);
		this.cmip.publishPort();
		this.addPort(cmip);
		

		this.rddsdop =
			new RequestDispatcherDynamicStateDataOutboundPort(requestDispatcherDynamicStateDataOutboundPort,this,rdURI) ;
		this.addPort(this.rddsdop) ;
		this.rddsdop.publishPort() ;
		

		this.rddsdop.doConnection(requestDispatcherDynamicStateDataInboundPortURI, ControlledDataConnector.class.getCanonicalName());
		this.rddsdop.startUnlimitedPushing(PUSH_INTERVAL);
		

		
		rdsdop = new RingNetworkDynamicStateDataOutboundPort(RingDynamicStateDataOutboundPortURI,this,this.controllerURI);
		this.addPort(rdsdop);
		this.rdsdop.publishPort();
		this.rdsdop.doConnection(nextRingDynamicStateDataInboundPort, ControlledDataConnector.class.getCanonicalName());
		
		this.nextRingDynamicStateDataInboundPort = nextRingDynamicStateDataInboundPort;
		rdsdip=new RingNetworkDynamicStateDataInboundPort(RingDynamicStateDataInboundPortURI, this);
		this.addPort(rdsdip);
		this.rdsdip.publishPort();
		

		rdmop = new RequestDispatcherManagementOutboundPort(controllerURI + "-rdmop",
				this);
		this.addPort(rdmop);
		this.rdmop.publishPort();
		
		this.rdmop.doConnection(requestDispatcherManagementInboundPort, RequestDispatcherManagementConnector.class.getCanonicalName());

		this.freeApplicationVM = new ArrayList<ApplicationVMInfo>();
		this.vmReserved = new ArrayList<ApplicationVMInfo>();
		this.myVMs = new ArrayList<ApplicationVMInfo>();
		this.VMsToBeKilled = new HashMap<String, ApplicationVMInfo>();
		this.cmops = new HashMap<String, ComputerControllerManagementOutboutPort>();
		this.avms = new HashMap<String, ApplicationVMManagementOutboundPort>();
		
		
		this.statistique = new HashMap<String, List<Mesure>>();
		
		/** Moyenne de toute les VMs **/
		this.statistique.put("All", Collections.synchronizedList(new ArrayList<Mesure>()));
		
		this.vmnibp = new VMDisconnectionNotificationHandlerInboundPort(VMDisconnectionNotificationHandlerInboundPortURI,this);
		this.addPort(vmnibp);
		this.vmnibp.publishPort();
		
		
		this.controllerManagementPreviousInboundPort = controllerManagementPreviousPort;
		this.controllerManagementNextInboundPort = controllerManagementNextPort;
		
		this.addVm(vm);

		w.write(Arrays.asList("Moyenne", "Nombre de VM", "Threeshold", "Nombre de coeurs alloué",  "Nombre de requêtes reçues", "Nombre de requêtes terminées"));

	}
	
	private Double calculAverage(String VMUri) throws Exception {
		List<Mesure> tmp = this.statistique.get(VMUri);
		Double average = 0.0;
		double alpha = 0.8;
		long timestamp_max = System.currentTimeMillis();
		
		//Nettoyage...
		int erase = 0;
		while(!tmp.isEmpty()) {
			if( timestamp_max - tmp.get(0).timestamp > TIME_MAX) {
				tmp.remove(0);
				erase++;
			}else {
				break;
			}
		}
		if(tmp.isEmpty()) {
			throw new Exception("Aucune valeur! timestamp incorrect?");
		}
		
		
		int taille = tmp.size();
		Double[] result = new Double[taille];
		result[0] =  tmp.get(0).value;
		for(int i = 1; i < taille; i++) {
			result[i] = alpha * tmp.get(i).value + (1 - alpha) * result[i-1];
		}
		
		
		return result[taille-1];
	}
	
	@Override
	public void start() throws ComponentStartException {
		super.start();
		try {
			this.startPushing();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Double calculAverage() throws Exception {
		return calculAverage("All");
	}
	
	
	/** 
	 * @see fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces.RequestDispatcherStateDataConsumerI#acceptRequestDispatcherDynamicData(java.lang.String, fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces.RequestDispatcherDynamicStateI)
	 */

	@Override
	public void acceptRequestDispatcherDynamicData(String dispatcherURI,
			RequestDispatcherDynamicStateI currentDynamicState) throws Exception {
		//this.logMessage(String.format("[%s] Dispatcher Dynamic Data : %4.3f",dispatcherURI,currentDynamicState.getAvgExecutionTime()/1000000/1000));
		
		try {
			//Cas où le dispatcher n'arrive pas à produire des statistiques tellement la VM à du mal.
			if(compteur_null == TargetData.MAX_NULL) {
				compteur_null =  Integer.MIN_VALUE;
				needVM = true;
				this.logMessage("IT'S DANGEROUS TO GO ALONE, TAKE A VM PLEASE " + this.controllerURI);
				return;
			}
			if((waitDecision % REQUEST_MIN) == 0) {
				reserveCore(1, currentDynamicState.getVirtualMachineDynamicStates());
			}
			waitDecision++;
			if(currentDynamicState.getAvgExecutionTime() == null) {
				compteur_null++;
				return;
			}else {
				compteur_null = 0;
			}
			
			long timestamp = currentDynamicState.getTimeStamp();
		    for (Entry<String, Double> entry : currentDynamicState.getVirtualMachineExecutionAverageTime().entrySet()) {
		    	if(entry.getValue() == null) {
		    		continue;
		    	}
		    	if(this.statistique.get(entry.getKey()) == null) {
		    		this.statistique.put(entry.getKey(),  Collections.synchronizedList(new ArrayList<Mesure>()));
		    	}
	    		this.statistique.get(entry.getKey()).add(new Mesure(entry.getValue(), timestamp));
		    }
		    
		    this.statistique.get("All").add(new Mesure(currentDynamicState.getAvgExecutionTime(), timestamp));
		    
		    if(waitDecision % (REQUEST_MIN-1) == 0) {
		    	needVM = true && myVMs.size() <= TargetData.MAX_VM;
		    }
		    else if((waitDecision % REQUEST_MIN) == 0) {
				processControl(currentDynamicState);
				//On redonne les VMs au prochain controller.
				needVM = false;
				synchronized (vmReserved) {
					while(!vmReserved.isEmpty()) {
						synchronized (freeApplicationVM) {
							freeApplicationVM.add(vmReserved.remove(0));
						}
					}
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	


	/**
	 * @see fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces.RequestDispatcherStateDataConsumerI#acceptRequestDispatcherStaticData(java.lang.String, fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces.RequestDispatcherStaticStateI)
	 */
	@Override
	public void acceptRequestDispatcherStaticData(String dispatcherURI, RequestDispatcherStaticStateI staticState)
			throws Exception {
		System.out.println("Dispatcher Static Data : ");
	}
	
	/**
	 * @see fr.upmc.components.AbstractComponent#shutdown()
	 */
	@Override
    public void shutdown() throws ComponentShutdownException {
        try {

            if(rddsdop.connected()) {
            	this.rddsdop.doDisconnection();
            }
            for (Entry<String, ComputerControllerManagementOutboutPort> entry : cmops.entrySet()) {
            	if(entry.getValue().connected()) {
            		entry.getValue().doDisconnection();
            	}
            }
            for (Entry<String, ApplicationVMManagementOutboundPort> entry : avms.entrySet()) {
            	if(entry.getValue().connected()) {
            		entry.getValue().doDisconnection();
            	}
            }
            if(rdsdop.connected()) {
            	this.rdsdop.doDisconnection();
            }
            if(rdsdip.connected()) {
            	this.rdsdip.doDisconnection();
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
    
	/**
	 * Seuil
	 */
    public enum Threeshold{
    	SLOWER, FASTER, GOOD, VERY_SLOW, VERY_FAST
    }
    
	public Threeshold getThreeshold(Double time){
		
		double speed = time.doubleValue();
		
		if(speed>TargetData.TARGET_SLOW) {
			if(speed>TargetData.TARGET_VERY_SLOW) {
				return Threeshold.VERY_SLOW;
			}
			return Threeshold.SLOWER;
		}
		
		else if(speed<TargetData.TARGET_SLOW && speed>TargetData.TARGET_FAST)
		{
			return Threeshold.GOOD;
		}
		else if(speed < TargetData.TARGET_FAST ) {
			if(speed < TargetData.TARGET_VERY_FAST){
				return Threeshold.VERY_FAST;
			}
			return Threeshold.VERY_FAST;
		}
			return null;
	}
	
	/**
	 * Renvoie l'URI de la VM ayant le moins de coeurs
	 * @param vms
	 * @return
	 */
	private String getBadVM(Map<String, ApplicationVMDynamicStateI > vms) {
		int number = -1;
		int tmp;
		String str = null;
		for (Entry<String, ApplicationVMDynamicStateI> entry : vms.entrySet())
		{
		   tmp = entry.getValue().getAllocatedCoresNumber().length;
		   if(tmp > number) {
			   str = entry.getKey();
			   number = tmp;
		   }
		}
		return str;
	}
	
	/**
	 * Find a vm in myVm with his uri
	 * @param VMURI
	 * @return
	 */
	private int findVm(String VMURI) {
			for(int i = 0; i < myVMs.size(); i++) {
				if(myVMs.get(i).getApplicationVM().equals(VMURI)) {
					return i;
				}
			}
		return -1;
	}
	private void processControl(RequestDispatcherDynamicStateI currentDynamicState){
		Map<String, ApplicationVMDynamicStateI > vms = currentDynamicState.getVirtualMachineDynamicStates();
		double average = 0;
		try {
			average = calculAverage();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		Threeshold th = getThreeshold(average);
		w.write(Arrays.asList(""+average, ((Integer)vms.size()).toString(), th.name(), ""+this.getNumberOfCoresAllocatedFrom(vms),  ""+currentDynamicState.getNbRequestReceived(), ""+currentDynamicState.getNbRequestTerminated()));
		
		try {
			if(th == Threeshold.VERY_SLOW) {
				very_slow_compteur++;
				//Tentative de déconnecter une mauvaise VM pour en réallouer une nouvelle..
				if(very_slow_compteur > 10) {
					very_slow_compteur  = 0;
					synchronized (myVMs) {
						if(myVMs.size() > 2) {
							askDisconnect(myVMs.remove(findVm(getBadVM(vms))));
							this.logMessage("Tentative de déconnection forcée d'une VM pour mauvaise performance !");
						}
					}
					
				}
				tooSlowCase(vms, 5);
			}else {
				very_slow_compteur = 0;
				if(th== Threeshold.SLOWER){
					tooSlowCase(vms, 2);
				}else if(th == Threeshold.FASTER){
					tooFastCase(vms, 2);
				}else if(th == Threeshold.VERY_FAST) {
					tooFastCase(vms, 5);
				}
			}
		
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		//Release les cores
		releaseCore(vms);
		w.write(Arrays.asList(""+average, ""+myVMs.size(), th.name(), ""+this.getNumberOfCoresAllocatedFrom(vms),  ""+currentDynamicState.getNbRequestReceived(), ""+currentDynamicState.getNbRequestTerminated()));
	}

	/**
	 * Renvoie le nombre de cores alloués de toutes les VM allouées à un dispatcher.
	 * @param vms Les Vms.
	 * @return Le nombre total de coeur.
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
	 * Le but est d'allouée un minimum de <code>objectif</code> à notre système.
	 * En cas d'echec, nous allouons une nouvelle VM
	 * @param vms
	 * @throws Exception 
	 */
	private void tooSlowCase(Map<String, ApplicationVMDynamicStateI > vms,  int objectif) throws Exception {
		try {
			
			//Try to up frequency
			//int nbCoreFrequencyChange = setCoreFrequency(CoreAsk.HIGHER, randomVM);
			//System.err.println("je passe par lower mdr");
	
			//Ajoute les cores
			synchronized(myVMs) {
				for (int i = 0; i < this.myVMs.size() && objectif > 0; i++)
				{
					objectif -= this.addCores(myVMs.get(i).getApplicationVM());
				}			
			}
			
			if(objectif > 0) {
				//Add a vm
				ApplicationVMInfo vm = null;
				synchronized (this.vmReserved) {
					int tailleVm = this.myVMs.size();
					if(tailleVm < TargetData.MAX_VM && !vmReserved.isEmpty()) {
						vm = vmReserved.remove(0);
					}
				}
				
				if(vm != null) {
					this.addVm(vm);
				}
			}
			}catch (Exception e) {
				e.printStackTrace();
			}
			//System.err.println("!!!!!!!!! " +this.cmops.get(randomVM.getApplicationVMURI()).reserveCore(randomVM.getApplicationVMURI()));
	}

	/**
	 * Cas où les machines virtuelles vont trop vite.
	 * Nous devons donc baisser la puissance du système responsable de la résolution de requêtes.
	 * 
	 * @param vms
	 * @throws Exception 
	 */
	private void tooFastCase(Map<String, ApplicationVMDynamicStateI > vms, int objectif) throws Exception {
		try {
			
			synchronized(myVMs) {
				for (int i = 0; i < this.myVMs.size(); i++)
				{
					if(vms.get(myVMs.get(i).getApplicationVM()).getAllocatedCoresNumber().length > TargetData.MIN_ALLOCATED_CORE) {
						this.avms.get(myVMs.get(i).getApplicationVM()).desallocateCores(1);
						objectif--;
					}else {
						
					}
				}			
			}
			if(objectif > 0) {

				synchronized (myVMs) {
					boolean canRemoveVM = myVMs.size() > 1 ;
					if(canRemoveVM) {
						ApplicationVMInfo randomVM = this.myVMs.remove(0);
						askDisconnect(randomVM);
					}
				}
			}
		

		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
/*	private int setCoreFrequency(CoreAsk ask, ApplicationVMDynamicStateI vm){
		this.logMessage("Try to " + ask.toString() + " for " + vm.getApplicationVMURI());
		int nb = 0;
		for(int i = 0; i < vm.getAllocatedCoresNumber().length; i++) {
			try {
				nb +=  (pcmop.setCoreFrequency(ask, vm.getProcessorURI(), vm.getAllocatedCoresNumber()[i])) ? 1 : 0;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		this.logMessage(nb + " cores was set for " + vm.getApplicationVMURI());

		return nb;
	}
	*/

	/**
	 * Ask to disconnect a vm to the dispatcher
	 * @param vm vm to be disconnected
	 * @throws Exception 
	 */
	private void askDisconnect(ApplicationVMInfo vm) throws Exception {
		this.logMessage("Demande de déconnexion de " + vm.getApplicationVM());
		this.VMsToBeKilled.put(vm.getApplicationVM(), vm);
		this.rdmop.askVirtualMachineDisconnection(vm.getApplicationVM());
	}
	
	/**
	 * @see fr.upmc.PriseTheSun.datacenter.software.ring.interfaces.RingNetworkStateDataConsumerI#acceptRingNetworkDynamicData(java.lang.String, fr.upmc.PriseTheSun.datacenter.software.ring.interfaces.RingNetworkDynamicStateI)
	 */
	@Override
	public void acceptRingNetworkDynamicData(String controllerDataRingOutboundPortURI, RingNetworkDynamicStateI currentDynamicState)
			throws Exception {
		
		ApplicationVMInfo vm =  currentDynamicState.getApplicationVMInfo();
		//System.out.println(vm + controllerDataRingOutboundPortURI);

		if(vm != null) {
				
				if(needVM) {
					needVM = false;
					synchronized(vmReserved){
						vmReserved.add(vm);
					}
				}
				else {
					synchronized (freeApplicationVM) {
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
		synchronized(freeApplicationVM){
			if(!this.freeApplicationVM.isEmpty()) {
				removed = this.freeApplicationVM.remove(0);
			}
		}
		return new RingDynamicState(removed);
	}
	
	public void addVm(ApplicationVMInfo vm){
		assert vm != null;
		
		// Create a mock up port to manage the AVM component (allocate cores).
		ApplicationVMManagementOutboundPort avmPort;
		int id = ++idVm;
		w.write(Arrays.asList("ask to add a vm"));

		try {
			avmPort = new ApplicationVMManagementOutboundPort(
					"avmop"+"-" + controllerURI+id, this);
			avmPort.publishPort() ;
			avmPort.doConnection(vm.getAvmInbound(),
						ApplicationVMManagementConnector.class.getCanonicalName());

			ComputerControllerManagementOutboutPort ccmop = cmops.get(vm.getApplicationVM());
			if(ccmop == null) {
				ccmop = new ComputerControllerManagementOutboutPort(this.controllerURI  + vm.getApplicationVM() +  "computerControllerManagementOutboutPort" + id, this);
		        this.addPort(ccmop);
				ccmop.publishPort();
				ccmop.doConnection(
							vm.getComputerManagementInboundPortURI(),
							ComputerControllerConnector.class.getCanonicalName());
			}

			AllocatedCore[] cores = ccmop.addCores(vm.getApplicationVM());
			if(cores == null || cores.length == 0) {
				throw new Exception("No cores found..");
			}
			
			avmPort.allocateCores(cores);
			
			
			rdmop.connectVirtualMachine(vm.getApplicationVM(), vm.getSubmissionInboundPortUri());
			avmPort.connectWithRequestSubmissioner(rdUri, requestDispatcherNotificationInboundPort);
			
			
			this.cmops.put(vm.getApplicationVM(), ccmop);
			this.avms.put(vm.getApplicationVM(), avmPort);
			synchronized (myVMs) {
				this.myVMs.add(vm);
			}
			w.write(Arrays.asList("VM add"));


		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void receiveVMDisconnectionNotification(String vmURI) throws Exception {
		
		assert vmURI != null;
		try {
			this.logMessage(this.controllerURI + " receive a signal to disconnect "+vmURI);
	
			ApplicationVMManagementOutboundPort avm = this.avms.remove(vmURI);
			avm.disconnectWithRequestSubmissioner();
			avm.desallocateAllCores();
			avm.doDisconnection();

			ComputerControllerManagementOutboutPort ccmop = this.cmops.remove(vmURI);
			ccmop.releaseCore(vmURI);
			ApplicationVMInfo vm = VMsToBeKilled.remove(vmURI);

			//reallocation
			int number = ccmop.tryReserveCore(vmURI, AdmissionControllerDynamic.NB_CORES, 0);
			if(number == 0 ) {
				this.logMessage("Impossible de rendre la VM au data ring.. Ordinateur plein..");
			}
			//this.rddsdop.startUnlimitedPushing(PUSH_INTERVAL);
			
			//Sans doute une demande de déconnexion par le dispatcher sans que le Controller lui demande
			if(vm == null) {
				synchronized (myVMs) {
					int index = findVm(vmURI);
					if(index > 0) {
						myVMs.remove(index);
					}
				}
			}
			synchronized (this.freeApplicationVM) {
				this.freeApplicationVM.add(vm);
			}

		}catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	


	/**
	 * @see fr.upmc.PriseTheSun.datacenter.software.controller.interfaces.VMDisconnectionNotificationHandlerI#disconnectController()
	 */
	@Override
	public void disconnectController() throws Exception {
		System.err.println("tentative de déconnexion..");
		try {
		NodeManagementOutboundPort cmopPrevious = new NodeManagementOutboundPort("cmop-previous-"+this.controllerURI + idVm, this);
		this.addPort(cmopPrevious);
		cmopPrevious.publishPort();
		cmopPrevious.doConnection(controllerManagementPreviousInboundPort, NodeManagementConnector.class.getCanonicalName());
		
		// On arrête le push 
		cmopPrevious.stopPushing();

		synchronized (this.freeApplicationVM) {
			while(!this.freeApplicationVM.isEmpty()) {
				Thread.sleep(300);
			}
		}
		
		//On attends jusqu'a ce qu'il ne reste plus de vm.
		
		// On raccorde les ports de managements
		cmopPrevious.setNextManagementInboundPort(controllerManagementNextInboundPort);

		NodeManagementOutboundPort cmopNext = new NodeManagementOutboundPort("cmop-next-"+this.controllerURI +idVm, this);
		this.addPort(cmopNext);
		cmopNext.publishPort();
		cmopNext.doConnection(controllerManagementNextInboundPort, NodeManagementConnector.class.getCanonicalName());
		cmopNext.setPreviousManagementInboundPort(controllerManagementPreviousInboundPort);
		
		//Déconnection de l'ancien inbound port...
		cmopNext.doDisconnectionInboundPort();
		
		
		cmopPrevious.bindSendingDataUri(this.nextRingDynamicStateDataInboundPort);
		cmopPrevious.startPushing();
		
		if(cmopNext.connected()) {
			cmopNext.doDisconnection();
			cmopNext.destroyPort();
		}
		
		if(cmopPrevious.connected()) {
			cmopPrevious.doDisconnection();

			cmopPrevious.destroyPort();
		}
		
		
		
		if(this.rdsdop.connected()) {
			this.logMessage("Disconnect " + this.controllerURI + " of the ring" );
			System.err.println("je te kill");
			this.rdsdop.doDisconnection();
		}
		this.logMessage("Disconnect " + this.controllerURI + " of the ring" );

		w.write(Arrays.asList("disconnected !!"));
		System.err.println("Disconnect " + this.controllerURI + " of the ring" );

		}catch (Exception e) {
			e.printStackTrace();
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

	
	private void reserveCore(int nbToAllocate, Map<String, ApplicationVMDynamicStateI> vms) {
		assert nbToAllocate > 0;
		synchronized (myVMs) {
			for(int i = 0; i < this.myVMs.size(); i++) {
				ApplicationVMDynamicStateI info = vms.get(myVMs.get(i).getApplicationVM());
				if(info == null) {
					return;
				}
				this.tryReserveCore(this.myVMs.get(i).getApplicationVM(), nbToAllocate, info.getAllocatedCoresNumber().length);
			}
		}
	}
	
	private void releaseCore(Map<String, ApplicationVMDynamicStateI> virtualMachineDynamicStates) {
		assert virtualMachineDynamicStates != null;

		
		for (Entry<String, ApplicationVMDynamicStateI> entry : virtualMachineDynamicStates.entrySet()) {
			this.releaseCore(entry.getKey());
	    }
	}
	
	private void addCores(Map<String, ApplicationVMDynamicStateI> virtualMachineDynamicStates) throws Exception {
		
		for (Entry<String, ApplicationVMDynamicStateI> entry : virtualMachineDynamicStates.entrySet()) {
		     this.addCores(entry.getKey());
		}
	}
	/**
	 * Demande de réservation de <code>nbToReserve</code> coeurs pour la machine virtuelle
	 * @param vmURI  Uri de la AVM
	 * @param nbToReserve Nombre de coeurs à réserver
	 * @return Nombre de coeur réserver
	 */
	private int tryReserveCore(String vmURI, int nbToReserve, int coreAllocated) {
		assert vmURI != null;
		try {
			return this.cmops.get(vmURI).tryReserveCore(vmURI, nbToReserve, coreAllocated);
		}catch (Exception e) {
			return 0;
		}
	}


	private void releaseCore(String vmURI) {
		try {
			this.cmops.get(vmURI).releaseCore(vmURI);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	private int addCores(String vmURI) throws Exception {
		ApplicationVMManagementOutboundPort avm = this.avms.get(vmURI);
		try {
			if(avm == null || !avm.connected()) {
				this.logMessage("AVM " + vmURI +"not found..");
				return 0;
			}
			AllocatedCore cores[] = this.cmops.get(vmURI).addCores(vmURI);

			if(cores != null && cores.length > 0) {
				this.logMessage("Allocation of " + cores.length );
				avm.allocateCores(cores);
				return cores.length;
			}else {
				this.logMessage("No allocation this time !" );
				
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public void doDisconnectionInboundPort() throws Exception {
		if(this.rdsdip.connected()) {
			this.rdsdip.doDisconnection();
		}
	}
	
	public class Mesure{
		@Override
		public String toString() {
			return ""+value;
		}
		public Double value;
		public long timestamp;

		public Mesure(Double value, long timespant) {
			super();
			this.value = value;
			this.timestamp = timespant;
		}
	}
}

