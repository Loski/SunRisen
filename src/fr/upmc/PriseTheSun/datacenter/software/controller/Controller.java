package fr.upmc.PriseTheSun.datacenter.software.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fr.upmc.PriseTheSun.datacenter.hardware.processors.ProcessorsController.CoreAsk;
import fr.upmc.PriseTheSun.datacenter.hardware.processors.connector.ProcessorControllerManagementConnector;
import fr.upmc.PriseTheSun.datacenter.hardware.processors.interfaces.ProcessorsControllerManagementI;
import fr.upmc.PriseTheSun.datacenter.hardware.processors.ports.ProcessorsControllerManagementOutboundPort;
import fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.connector.AdmissionControllerManagementConnector;
import fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.interfaces.AdmissionControllerManagementI;
import fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.ports.AdmissionControllerManagementOutboundPort;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.VirtualMachineData;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces.RequestDispatcherDynamicStateI;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces.RequestDispatcherStateDataConsumerI;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces.RequestDispatcherStaticStateI;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.ports.RequestDispatcherDynamicStateDataOutboundPort;
import fr.upmc.PriseTheSun.datacenter.software.ring.ports.RingDynamicStateDataInboundPort;
import fr.upmc.PriseTheSun.datacenter.software.ring.ports.RingDynamicStateDataOutboundPort;
import fr.upmc.components.AbstractComponent;
import fr.upmc.datacenter.connectors.ControlledDataConnector;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.datacenter.interfaces.ControlledDataOfferedI;
import fr.upmc.datacenter.software.applicationvm.interfaces.ApplicationVMDynamicStateI;
import fr.upmc.datacenter.software.interfaces.RequestNotificationI;
import fr.upmc.datacenter.software.ports.RequestNotificationOutboundPort;



public class Controller extends AbstractComponent implements RequestDispatcherStateDataConsumerI{

	protected String controllerURI;
	protected String cmop;
	protected String rdUri;
	protected AdmissionControllerManagementOutboundPort acmop;
	protected RequestDispatcherDynamicStateDataOutboundPort rddsdop;
	protected ProcessorsControllerManagementOutboundPort pcmop;
	private RingDynamicStateDataOutboundPort rdsdop;
	private RingDynamicStateDataInboundPort rdsdip;


	public Controller(String controllerURI,String requestDispatcherDynamicStateDataOutboundPort,String rdURI, String requestDispatcherDynamicStateDataInboundPortURI, String AdmissionControllerManagementInboundPortURI, String ProcessorControllerManagementInboundUri, String RingDynamicStateDataOutboundPortURI, String RingDynamicStateDataInboundPortURI ) throws Exception
	{
		super(controllerURI,1,1);
		
		this.controllerURI = controllerURI;
		this.rdUri = rdURI;
		
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
		
		
		
		rdsdop = new RingDynamicStateDataOutboundPort(this, RingDynamicStateDataOutboundPortURI);
		this.addPort(rdsdop);
		this.rdsdop.publishPort();

		rdsdip=new RingDynamicStateDataInboundPort(RingDynamicStateDataInboundPortURI, this);
		this.addPort(rdsdip);
		this.rdsdip.publishPort();
	}
	
	@Override
	public void acceptRequestDispatcherDynamicData(String dispatcherURI,
			RequestDispatcherDynamicStateI currentDynamicState) throws Exception {
		
		if(currentDynamicState.getAvgExecutionTime()!=null) {
			System.err.println(String.format("[%s] Dispatcher Dynamic Data : %4.3f",dispatcherURI,currentDynamicState.getAvgExecutionTime()/1000000/1000));
			processControl(currentDynamicState.getAvgExecutionTime(), currentDynamicState.getVirtualMachineDynamicStates());
		}
		else {
			System.err.println(String.format("[%s] Dispatcher Dynamic Data : %s",dispatcherURI,"pas assez de données pour calculer la moyenne"));
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

	public boolean isHigher(Double time){
		return Double.compare(time, (StaticData.AVERAGE_TARGET*StaticData.HIGHER_PERCENT + StaticData.AVERAGE_TARGET)) == 1 ? true : false;
	}

	public boolean isLower(Double time){
		return Double.compare(time, (StaticData.AVERAGE_TARGET*StaticData.LOWER_PERCENT - StaticData.AVERAGE_TARGET)) == -1 ? true : false;
	}
	
	private synchronized void processControl(Double time, Map<String, ApplicationVMDynamicStateI > vms) throws Exception {
		double factor=0;
		int number=0;
		ApplicationVMDynamicStateI randomVM = vms.get(vms.keySet().iterator().next());
		System.out.println(Arrays.toString(randomVM.getAllocatedCoresNumber()));
		int cores = getNumberOfCoresAllocatedFrom(vms);
		switch(getThreeshold(time)){
		case HIGHER :
			factor = (time/StaticData.AVERAGE_TARGET);
			number = Math.max(1, (int)(cores*factor));
			number = Math.min(StaticData.MAX_ALLOCATION, number);
			
			//Try to change frequency
			for(int i = 0; i < randomVM.getAllocatedCoresNumber().length;i++) {
				System.err.println(i);

				boolean set = pcmop.setCoreFrequency(CoreAsk.HIGHER, randomVM.getProcessorURI(), randomVM.getAllocatedCoresNumber()[i]);

				if(set) {
					System.err.println("Frequece was set");
				}
			}
			this.acmop.addCores(null, 1, randomVM.getApplicationVMURI());
			break;
		case LOWER :
			factor = (StaticData.AVERAGE_TARGET/time);
			number =Math.max(1, (int)(cores-(cores/factor)));
			number =Math.min(StaticData.MIN_ALLOCATION, number);
			if(vms.size()==1 && cores == StaticData.MIN_ALLOCATION) {
				//TODO add proco
				break;
			}
		//	processDeallocate(factor,number,vms,double1,nbreq,cores);
			
			// add Reset request stat?

			break;
		case GOOD :
			break;
		default:
			break;
		}
	}
	//TODO PAs oublier de renvoyer le vrai truc un jour
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
	
	static class StaticData {
		public static long AVERAGE_TARGET=2500;
		public static double LOWER_PERCENT=0.5;
		public static double HIGHER_PERCENT=0.3;
		public static int DISPATCHER_PUSH_INTERVAL=5000;
		
		//Max core
		public static int MAX_ALLOCATION=25;
		
		public static int MIN_ALLOCATION = 2;

	}
}
