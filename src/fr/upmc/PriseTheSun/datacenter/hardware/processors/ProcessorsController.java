package fr.upmc.PriseTheSun.datacenter.hardware.processors;


import java.util.HashMap;
import java.util.Map;

import fr.upmc.components.AbstractComponent;
import fr.upmc.datacenter.connectors.ControlledDataConnector;
import fr.upmc.datacenter.hardware.processors.interfaces.ProcessorDynamicStateI;
import fr.upmc.datacenter.hardware.processors.interfaces.ProcessorStateDataConsumerI;
import fr.upmc.datacenter.hardware.processors.interfaces.ProcessorStaticStateI;
import fr.upmc.datacenter.hardware.processors.ports.ProcessorDynamicStateDataOutboundPort;
import fr.upmc.datacenter.hardware.processors.ports.ProcessorStaticStateDataOutboundPort;

public class ProcessorsController extends AbstractComponent implements ProcessorStateDataConsumerI {

	private static final String ProcessorStaticStateDataOutboundPortURI = "pss";
	private static final String ProcessorDynamicStateDataOutboundPortURI = "pds";

	Map<String, ProcessorStaticStateI> processorsStaticState;
	Map<String, ProcessorDynamicStateI> processorsDynamicState;

	public ProcessorsController(String URI) {
		super(URI, 2, 2);
		processorsStaticState = new HashMap<String, ProcessorStaticStateI>();
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
	
	public void bindProcessor(String processorURI, String processorManagementURI, String ProcessorStaticStateDataInboundPortURI, String ProcessorDynamicStateDataInoundPortURI) throws Exception {
		
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
		System.out.println("ça marche" + processorsStaticState.get(processorURI).getMaxFrequencyGap());
	}
}
