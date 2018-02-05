package fr.upmc.Sunrise.datacenter.hardware.processors.ports;

import fr.upmc.Sunrise.datacenter.hardware.processors.ProcessorsController.CoreAsk;
import fr.upmc.Sunrise.datacenter.hardware.processors.interfaces.ProcessorsControllerManagementI;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.datacenter.hardware.processors.UnacceptableFrequencyException;
import fr.upmc.datacenter.hardware.processors.UnavailableFrequencyException;

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

	@Override
	public boolean setCoreFrequency(CoreAsk ask, String processorURI, int coreNo)
			throws UnavailableFrequencyException, UnacceptableFrequencyException, Exception {
		// TODO Auto-generated method stub
		return ( ( ProcessorsControllerManagementI )this.connector).setCoreFrequency(ask, processorURI, coreNo);
	}
}
