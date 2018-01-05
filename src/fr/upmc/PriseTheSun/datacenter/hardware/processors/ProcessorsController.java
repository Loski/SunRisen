package fr.upmc.PriseTheSun.datacenter.hardware.processors;


import java.util.HashMap;
import java.util.Map;

import fr.upmc.components.AbstractComponent;
import fr.upmc.datacenter.connectors.ControlledDataConnector;
import fr.upmc.datacenter.hardware.processors.interfaces.ProcessorDynamicStateI;
import fr.upmc.datacenter.hardware.processors.interfaces.ProcessorStateDataConsumerI;
import fr.upmc.datacenter.hardware.processors.interfaces.ProcessorStaticStateI;
import fr.upmc.datacenter.hardware.processors.ports.ProcessorStaticStateDataOutboundPort;

public class ProcessorsController extends AbstractComponent implements ProcessorStateDataConsumerI {

	private static final String ProcessorStaticStateDataOutboundPortURI = "pss";
	Map<String, ProcessorStaticStateI> processorsState;
	
	public ProcessorsController(String URI) {
		super(URI, 2, 2);
		processorsState = new HashMap<String, ProcessorStaticStateI>();
	}
	@Override
	public void acceptProcessorStaticData(String processorURI, ProcessorStaticStateI staticState) throws Exception {
		processorsState.put(processorURI, staticState);
	}

	@Override
	public void acceptProcessorDynamicData(String processorURI, ProcessorDynamicStateI currentDynamicState)
			throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	public void bindProcessor(String ProcessorStaticStateDataInboundPortURI) throws Exception {
		String processorUri = ProcessorStaticStateDataOutboundPortURI + "_" +  this.processorsState.size();
		ProcessorStaticStateDataOutboundPort pssdop = new ProcessorStaticStateDataOutboundPort(this, processorUri);
		this.addPort(pssdop);
		pssdop.publishPort();
		pssdop.doConnection(
				ProcessorStaticStateDataInboundPortURI,
				ControlledDataConnector.class.getCanonicalName());

		ProcessorStaticStateI staticState= (ProcessorStaticStateI) pssdop.request();
		processorsState.put(ProcessorStaticStateDataInboundPortURI, staticState);
		
		System.out.println("ça marche" + processorsState.get(ProcessorStaticStateDataInboundPortURI).getMaxFrequencyGap());
	}
}
