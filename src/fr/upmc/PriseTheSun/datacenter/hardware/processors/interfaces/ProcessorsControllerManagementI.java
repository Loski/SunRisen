package fr.upmc.PriseTheSun.datacenter.hardware.processors.interfaces;

import fr.upmc.PriseTheSun.datacenter.hardware.processors.ProcessorsController.CoreAsk;
import fr.upmc.datacenter.hardware.processors.UnacceptableFrequencyException;
import fr.upmc.datacenter.hardware.processors.UnavailableFrequencyException;

public interface ProcessorsControllerManagementI {
	public  void bindProcessor(String processorURI, String processorControllerInboundPortURI, String processorManagementURI, String ProcessorStaticStateDataInboundPortURI, String ProcessorDynamicStateDataInoundPortURI) throws Exception;
	public boolean setCoreFrequency(CoreAsk ask, String processorURI, int coreNo) throws UnavailableFrequencyException, UnacceptableFrequencyException, Exception;
}
