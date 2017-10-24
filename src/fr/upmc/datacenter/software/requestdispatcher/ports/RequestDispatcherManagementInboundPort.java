package fr.upmc.datacenter.software.requestdispatcher.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ComponentI.ComponentService;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.datacenter.software.requestdispatcher.RequestDispatcher;
import fr.upmc.datacenter.software.requestdispatcher.interfaces.RequestDispatcherManagementI;

public class RequestDispatcherManagementInboundPort extends AbstractInboundPort
        implements RequestDispatcherManagementI {

    private static final long serialVersionUID = 1L;

    public RequestDispatcherManagementInboundPort( ComponentI owner ) throws Exception {
        super( RequestDispatcherManagementI.class , owner );

        assert owner instanceof RequestDispatcherManagementI;
    }

    public RequestDispatcherManagementInboundPort( String uri , ComponentI owner ) throws Exception {
        super( uri , RequestDispatcherManagementI.class , owner );

        assert uri != null && owner instanceof RequestDispatcherManagementI;
    }

    @Override
    public void connectVm(final String vmURI, final String RequestSubmissionInboundPortURI ) throws Exception {
        final RequestDispatcher rd = ( RequestDispatcher ) this.owner;

        this.owner.handleRequestSync( new ComponentService<Void>() {

            @Override
            public Void call() throws Exception {
               rd.connectVm(vmURI, RequestSubmissionInboundPortURI );
               return null;
            }
        } );
    }

    @Override
    public void disconnectVm() throws Exception {
        final RequestDispatcher rd = ( RequestDispatcher ) this.owner;

        this.owner.handleRequestAsync( new ComponentI.ComponentService<Void>() {

        	@Override
        	public Void call() throws Exception {
        		rd.disconnectVm();
        		return null;
        	}
        } );

    }

}
