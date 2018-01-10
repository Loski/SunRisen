package fr.upmc.PriseTheSun.datacenter.hardware.processors.ports;

import fr.upmc.PriseTheSun.datacenter.hardware.processors.interfaces.ProcessorsControllerManagementI;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;

public class ProcessorsControllerManagementOutboundPort extends		AbstractOutboundPort
implements	ProcessorsControllerManagementI{

	public	ProcessorsControllerManagementOutboundPort(String uri, ComponentI owner) throws Exception
	{
		super(uri, ProcessorsControllerManagementI.class, owner);
	}

	public ProcessorsControllerManagementOutboundPort(String uri, Class<?> implementedInterface, ComponentI owner)
			throws Exception {
		super(uri, ProcessorsControllerManagementI.class, owner);
		assert	owner != null && owner instanceof ProcessorsControllerManagementI;
	}


	@Override
	public void bindProcessor(String processorURI, String processorControllerInboundPortURI,
			String processorManagementURI, String ProcessorStaticStateDataInboundPortURI, String ProcessorDynamicStateDataInoundPortURI)
			throws Exception {
		((ProcessorsControllerManagementI)this.connector).bindProcessor(processorURI, processorControllerInboundPortURI, processorManagementURI, ProcessorStaticStateDataInboundPortURI, ProcessorDynamicStateDataInoundPortURI);
		
	}
}
