package fr.upmc.PriseTheSun.datacenter.hardware.processors.connector;

import java.util.ArrayList;

import fr.upmc.PriseTheSun.datacenter.hardware.processors.interfaces.ProcessorsControllerManagementI;
import fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.interfaces.AdmissionControllerManagementI;
import fr.upmc.components.connectors.AbstractConnector;

public class ProcessorControllerManagementConnector extends	AbstractConnector implements ProcessorsControllerManagementI
{

	@Override
	public void bindProcessor(String processorURI, String processorControllerInboundPortURI,
			String processorManagementURI, String ProcessorStaticStateDataInboundPortURI, String ProcessorDynamicStateDataInoundPortURI)
			throws Exception {
		( ( ProcessorsControllerManagementI )this.offering).bindProcessor(processorURI, processorControllerInboundPortURI, processorManagementURI, ProcessorStaticStateDataInboundPortURI, ProcessorDynamicStateDataInoundPortURI);
		
	}
	
	
	
}
