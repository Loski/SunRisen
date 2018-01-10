package fr.upmc.PriseTheSun.datacenter.software.controller;

import java.util.ArrayList;
import java.util.List;

import fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.connector.AdmissionControllerManagementConnector;
import fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.interfaces.AdmissionControllerManagementI;
import fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.ports.AdmissionControllerManagementOutboundPort;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.VirtualMachineData;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces.RequestDispatcherDynamicStateI;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces.RequestDispatcherStateDataConsumerI;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces.RequestDispatcherStaticStateI;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.ports.RequestDispatcherDynamicStateDataOutboundPort;
import fr.upmc.components.AbstractComponent;
import fr.upmc.datacenter.connectors.ControlledDataConnector;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.datacenter.interfaces.ControlledDataOfferedI;
import fr.upmc.datacenter.software.interfaces.RequestNotificationI;
import fr.upmc.datacenter.software.ports.RequestNotificationOutboundPort;



public class Controller extends AbstractComponent implements RequestDispatcherStateDataConsumerI{

	protected String controllerURI;
	protected String cmop;
	protected String rdUri;
	protected AdmissionControllerManagementOutboundPort acmop;
	protected RequestDispatcherDynamicStateDataOutboundPort rddsdop;
	
	private int threesholdBottom;
	private int threesholdTop;


	public Controller(String controllerURI,String requestDispatcherDynamicStateDataOutboundPort,String rdURI, String requestDispatcherDynamicStateDataInboundPortURI, String AdmissionControllerManagementInboundPortURI) throws Exception
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
		
		this.rddsdop.startUnlimitedPushing(10000);
	}
	
	@Override
	public void acceptRequestDispatcherDynamicData(String dispatcherURI,
			RequestDispatcherDynamicStateI currentDynamicState) throws Exception {
		
		if(currentDynamicState.getAvgExecutionTime()!=null) {
			System.err.println(String.format("[%s] Dispatcher Dynamic Data : %s",dispatcherURI,""+currentDynamicState.getAvgExecutionTime()));
			processControl(currentDynamicState.getAvgExecutionTime(), currentDynamicState.getVMData());
		}
		else {
			System.err.println(String.format("[%s] Dispatcher Dynamic Data : %s",dispatcherURI,"pas assez de données pour calculer la moyenne"));
		}
		currentDynamicState.getVMData();
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
    
	public Threeshold getThreeshold(long time){
		if(isHigher(time))
			return Threeshold.HIGHER;
		if(isLower(time))
			return Threeshold.LOWER;
		return Threeshold.GOOD;
	}

	public boolean isHigher(long time){
		return (time > (StaticData.AVERAGE_TARGET*StaticData.HIGHER_PERCENT + StaticData.AVERAGE_TARGET));
	}

	public boolean isLower(long time){
		return (time < (StaticData.AVERAGE_TARGET*StaticData.LOWER_PERCENT - StaticData.AVERAGE_TARGET));
}
	
	private void processControl(long time, ArrayList<VirtualMachineData> vms) throws Exception {
		
		double factor=0;
		int number=0;
		int cores = getNumberOfCoresAllocatedFrom(vms);

		switch(getThreeshold(time)){
		case HIGHER :
			factor = (time/StaticData.AVERAGE_TARGET);
			number = Math.max(1, (int)(cores*factor));
			number = Math.min(StaticData.MAX_ALLOCATION, number);
			processAllocation(factor,number,vms,time,nbreq,cores);
			
			// add Reset request stat?
			break;
		case LOWER :
			factor = (StaticData.AVERAGE_TARGET/time);
			number =Math.max(1, (int)(cores-(cores/factor)));
			number =Math.min(StaticData.MAX_DEALLOCATION, number);
			if(vms.size()==1)
				if(vms.get(0).getNbCore()== StaticData.MIN_ALLOCATION)
					break;
			processDeallocate(factor,number,vms,time,nbreq,cores);
			
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
	private int getNumberOfCoresAllocatedFrom(List<VirtualMachineData> vms) {
		int number = 0;
		for(VirtualMachineData vm : vms) {
			number++;
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
