package fr.upmc.PriseTheSun.datacenter.hardware.processors.ports;



import fr.upmc.PriseTheSun.datacenter.hardware.processors.ProcessorsController.CoreAsk;
import fr.upmc.PriseTheSun.datacenter.hardware.processors.interfaces.ProcessorsControllerManagementI;
import fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.interfaces.AdmissionControllerManagementI;
import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces.RequestDispatcherManagementI;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.datacenter.hardware.processors.UnacceptableFrequencyException;
import fr.upmc.datacenter.hardware.processors.UnavailableFrequencyException;


public class ProcessorsControllerManagmentInboundPort extends AbstractInboundPort implements ProcessorsControllerManagementI{
	
	public ProcessorsControllerManagmentInboundPort(Class<?> implementedInterface, ComponentI owner) throws Exception {
		super(ProcessorsControllerManagementI.class, owner);
		assert	owner != null && owner instanceof ProcessorsControllerManagementI ;
	}

	public ProcessorsControllerManagmentInboundPort(String uri, Class<?> implementedInterface, ComponentI owner)
			throws Exception {
		super(uri, ProcessorsControllerManagementI.class, owner);
		assert	owner != null && owner instanceof ProcessorsControllerManagementI ;
	}
	
	public ProcessorsControllerManagmentInboundPort(String uri, ComponentI owner
			) throws Exception
		{
			super(uri, ProcessorsControllerManagementI.class, owner);

			assert	uri != null && owner instanceof ProcessorsControllerManagementI ;
		}

	@Override
	public void bindProcessor(String processorURI, String processorControllerInboundPortURI,
		String processorManagementURI, String ProcessorStaticStateDataInboundPortURI, String ProcessorDynamicStateDataInoundPortURI) throws Exception {
		final ProcessorsControllerManagementI pcm = ( ProcessorsControllerManagementI ) this.owner;
		
		this.owner.handleRequestSync(
				new ComponentI.ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						pcm.bindProcessor(processorURI, processorControllerInboundPortURI, processorManagementURI, ProcessorStaticStateDataInboundPortURI, ProcessorDynamicStateDataInoundPortURI);
						return null;
					}
				});
		}

	@Override
	public boolean setCoreFrequency(CoreAsk ask, String processorURI, int coreNo)
			throws UnavailableFrequencyException, UnacceptableFrequencyException, Exception {
		final ProcessorsControllerManagementI pcm = ( ProcessorsControllerManagementI ) this.owner;
		return this.owner.handleRequestSync(
				new ComponentI.ComponentService<Boolean>() {
					@Override
					public Boolean call() throws Exception {
						return pcm.setCoreFrequency(ask, processorURI, coreNo);
					}
				});
	}



}
