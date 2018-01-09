package fr.upmc.PriseTheSun.datacenter.hardware.processors.interfaces;

public interface ProcessorsControllerManagementI {
	public  void bindProcessor(String processorURI, String processorControllerInboundPortURI, String processorManagementURI, String ProcessorStaticStateDataInboundPortURI, String ProcessorDynamicStateDataInoundPortURI) throws Exception;
}
