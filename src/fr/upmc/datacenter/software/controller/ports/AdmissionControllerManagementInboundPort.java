package fr.upmc.datacenter.software.controller.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ComponentI.ComponentService;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.datacenter.software.controller.AdmissionController;
import fr.upmc.datacenter.software.controller.interfaces.AdmissionControllerManagementI;
import fr.upmc.datacenterclient.applicationprovider.ApplicationProvider;

public class AdmissionControllerManagementInboundPort extends AbstractInboundPort implements AdmissionControllerManagementI{

	public AdmissionControllerManagementInboundPort(Class<?> implementedInterface, ComponentI owner) throws Exception {
		super(AdmissionControllerManagementI.class, owner);
		assert	owner != null && owner instanceof AdmissionController ;
	}

	public AdmissionControllerManagementInboundPort(String uri, Class<?> implementedInterface, ComponentI owner)
			throws Exception {
		super(uri, AdmissionControllerManagementI.class, owner);
		assert	owner != null && owner instanceof AdmissionController ;
	}

	@Override
	public String[] addCore(String rdUri, int nbCore) throws Exception {
		final AdmissionController arh = ( AdmissionController ) this.owner;
        return this.owner.handleRequestSync( new ComponentService<String[]>() {
            @Override
            public String[] call() throws Exception {
                return arh.addCore( rdUri, nbCore );
            }
        } );
	}

}
