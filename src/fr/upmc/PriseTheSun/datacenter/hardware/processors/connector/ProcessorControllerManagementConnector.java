package fr.upmc.PriseTheSun.datacenter.hardware.processors.connector;

import java.util.ArrayList;

import fr.upmc.PriseTheSun.datacenter.hardware.processors.ProcessorsController.CoreAsk;
import fr.upmc.PriseTheSun.datacenter.hardware.processors.interfaces.ProcessorsControllerManagementI;
import fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.interfaces.AdmissionControllerManagementI;
import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.datacenter.hardware.processors.UnacceptableFrequencyException;
import fr.upmc.datacenter.hardware.processors.UnavailableFrequencyException;

public class ProcessorControllerManagementConnector extends	AbstractConnector implements ProcessorsControllerManagementI
{

	@Override
	public void bindProcessor(String processorURI, String processorControllerInboundPortURI,
			String processorManagementURI, String ProcessorStaticStateDataInboundPortURI, String ProcessorDynamicStateDataInoundPortURI)
			throws Exception {
		( ( ProcessorsControllerManagementI )this.offering).bindProcessor(processorURI, processorControllerInboundPortURI, processorManagementURI, ProcessorStaticStateDataInboundPortURI, ProcessorDynamicStateDataInoundPortURI);
		
	}

	@Override
	public boolean setCoreFrequency(CoreAsk ask, String processorURI, int coreNo)
			throws UnavailableFrequencyException, UnacceptableFrequencyException, Exception {
		return ( ( ProcessorsControllerManagementI )this.offering).setCoreFrequency(ask, processorURI, coreNo);
	}
	
	
	
}
