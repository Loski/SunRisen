package fr.upmc.PriseTheSun.datacenter.hardware.processors;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import fr.upmc.PriseTheSun.datacenter.hardware.processors.interfaces.ProcessorsControllerManagementI;
import fr.upmc.PriseTheSun.datacenter.hardware.processors.ports.ProcessorsControllerManagmentInboundPort;
import fr.upmc.PriseTheSun.datacenterclient.software.applicationprovider.interfaces.ApplicationSubmissionI;
import fr.upmc.PriseTheSun.datacenterclient.software.applicationprovider.ports.ApplicationSubmissionInboundPort;
import fr.upmc.components.AbstractComponent;
import fr.upmc.datacenter.connectors.ControlledDataConnector;
import fr.upmc.datacenter.hardware.processors.UnacceptableFrequencyException;
import fr.upmc.datacenter.hardware.processors.UnavailableFrequencyException;
import fr.upmc.datacenter.hardware.processors.connectors.ProcessorManagementConnector;
import fr.upmc.datacenter.hardware.processors.interfaces.ProcessorDynamicStateI;
import fr.upmc.datacenter.hardware.processors.interfaces.ProcessorStateDataConsumerI;
import fr.upmc.datacenter.hardware.processors.interfaces.ProcessorStaticStateI;
import fr.upmc.datacenter.hardware.processors.ports.ProcessorDynamicStateDataOutboundPort;
import fr.upmc.datacenter.hardware.processors.ports.ProcessorManagementOutboundPort;
import fr.upmc.datacenter.hardware.processors.ports.ProcessorStaticStateDataOutboundPort;

public class ProcessorsController extends AbstractComponent implements ProcessorStateDataConsumerI, ProcessorsControllerManagementI {

	private static final String ProcessorStaticStateDataOutboundPortURI = "pss";
	private static final String ProcessorDynamicStateDataOutboundPortURI = "pds";
	private static final String ProcessorManagementOutboundPortURI = "pmop";

	private ProcessorsControllerManagmentInboundPort pcmip;
	private Map<String, ProcessorStaticStateI> processorsStaticState;
	private Map<String, ProcessorDynamicStateI> processorsDynamicState;
	private Map<String, ProcessorManagementOutboundPort> processorsManagement;
	public ProcessorsController(String URI, String ProcessorControllerManagementInboundPortURI) throws Exception {
		super(URI, 2, 2);
		processorsStaticState = new HashMap<String, ProcessorStaticStateI>();
		processorsDynamicState = new HashMap<String, ProcessorDynamicStateI>();
		processorsManagement = new HashMap<String, ProcessorManagementOutboundPort>();
		
		this.addOfferedInterface(ProcessorsControllerManagementI.class);
		pcmip = new ProcessorsControllerManagmentInboundPort(ProcessorControllerManagementInboundPortURI, this);
		this.addPort(pcmip);
		this.pcmip.publishPort();
	}
	

	
	@Override
	public void acceptProcessorStaticData(String processorURI, ProcessorStaticStateI staticState) throws Exception {
		processorsStaticState.put(processorURI, staticState);
	}

	@Override
	public void acceptProcessorDynamicData(String processorURI, ProcessorDynamicStateI currentDynamicState)
			throws Exception {
		//System.out.println(currentDynamicState.getCurrentCoreFrequencies()[0]);
		
		
	}
	
	public void bindProcessor(String processorURI, String processorControllerInboundPortURI, String processorManagementURI, String ProcessorStaticStateDataInboundPortURI, String ProcessorDynamicStateDataInoundPortURI) throws Exception {
		
		int number = this.processorsStaticState.size();
		String processorStaticStateDataOutboundPortUri = ProcessorStaticStateDataOutboundPortURI + "_" +  number;
		String processorDynamicStateDataUriOutboundPort = ProcessorDynamicStateDataOutboundPortURI + "_" +  number;
		
		ProcessorStaticStateDataOutboundPort pssdop = new ProcessorStaticStateDataOutboundPort(this, processorStaticStateDataOutboundPortUri);
		this.addPort(pssdop);
		pssdop.publishPort();
		
		pssdop.doConnection(
				ProcessorStaticStateDataInboundPortURI,
				ControlledDataConnector.class.getCanonicalName());

		ProcessorStaticStateI staticState= (ProcessorStaticStateI) pssdop.request();
		processorsStaticState.put(processorURI, staticState);
		
		
		ProcessorDynamicStateDataOutboundPort pdsdop = new ProcessorDynamicStateDataOutboundPort(this, processorDynamicStateDataUriOutboundPort);
		this.addPort(pssdop);
		pdsdop.publishPort();
		pdsdop.doConnection(
				ProcessorDynamicStateDataInoundPortURI,
				ControlledDataConnector.class.getCanonicalName());
		pdsdop.startUnlimitedPushing(1000);
		
		ProcessorManagementOutboundPort pmop = new ProcessorManagementOutboundPort(processorURI + ProcessorManagementOutboundPortURI+"_"+number, this);
		this.addPort(pmop);
		pmop.publishPort();
		pmop.doConnection(
				processorManagementURI,
				ProcessorManagementConnector.class.getCanonicalName());
		this.processorsManagement.put(processorURI, pmop);
	}
	
	public boolean setCoreFrequency(CoreAsk ask, String processorURI, int coreNo) throws UnavailableFrequencyException, UnacceptableFrequencyException, Exception {
		ProcessorStaticStateI staticState = this.processorsStaticState.get(processorURI);
		ProcessorDynamicStateI dynamicState = this.processorsDynamicState.get(processorURI); 
		int frequenceCore = dynamicState.getCurrentCoreFrequency(coreNo);
		Set<Integer> admissableFrequencies = staticState.getAdmissibleFrequencies();
		Iterator<Integer> it = admissableFrequencies.iterator();
		int newfrequency = -1;
		int frequency = -1;
		
		//Ajouter tri � la cr�ation des fr�quences pour �viter parcourt ??.
		
		if(ask == CoreAsk.HIGHER) {
		    while(it.hasNext()) {
		    	frequency = (int) it.next();
		    	if(frequency > frequenceCore) {
		    		newfrequency = frequency;
		    		break;
		    	}
		    }
		}else if(ask == CoreAsk.LOWER) {
		    while(it.hasNext()) {
		    	frequency = (int) it.next();
		    	if(frequency > frequenceCore) {
		    		newfrequency = frequency;
		    		break;
		    	}
		    }
		}
		if(newfrequency != -1) {
			this.processorsManagement.get(processorURI).setCoreFrequency(coreNo, newfrequency);
			return true;
		}
		return false;
	}
	
	public enum CoreAsk {
		HIGHER, LOWER
	}
}
