package fr.upmc.PriseTheSun.datacenter.software.controller;

import fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.ports.AdmissionControllerManagementOutboundPort;
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
	protected AdmissionControllerManagementOutboundPort acmop;
	protected RequestDispatcherDynamicStateDataOutboundPort rddsdop;
	
	private int threesholdBottom;
	private int threesholdTop;

	public Controller(String controllerURI,String requestDispatcherDynamicStateDataOutboundPort,String rdURI,String requestDispatcherDynamicStateDataInboundPortURI) throws Exception
	{
		super(controllerURI,1,1);
		
		this.controllerURI = controllerURI;
		
		this.addRequiredInterface(ControlledDataOfferedI.ControlledPullI.class) ;
		this.rddsdop =
			new RequestDispatcherDynamicStateDataOutboundPort(requestDispatcherDynamicStateDataOutboundPort,this,rdURI) ;
		this.addPort(this.rddsdop) ;
		this.rddsdop.publishPort() ;

		this.rddsdop.doConnection(requestDispatcherDynamicStateDataInboundPortURI, ControlledDataConnector.class.getCanonicalName());
		
		this.rddsdop.startUnlimitedPushing(10000);
	}
	
	@Override
	public void acceptRequestDispatcherDynamicData(String dispatcherURI,
			RequestDispatcherDynamicStateI currentDynamicState) throws Exception {
		System.out.println(String.format("[%s] Dispatcher Dynamic Data : %s",dispatcherURI,currentDynamicState.getAvgExecutionTime()));
	}
	@Override
	public void acceptRequestDispatcherStaticData(String dispatcherURI, RequestDispatcherStaticStateI staticState)
			throws Exception {
		// TODO Auto-generated method stub
		System.out.println("Dispatcher Static Data : ");
	}
	
	public void controlling() {
		
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
    
}
