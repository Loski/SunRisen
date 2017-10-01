package fr.upmc.datacenter.software.vmswitcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.upmc.components.AbstractComponent;
import fr.upmc.datacenter.hardware.processors.interfaces.ProcessorServicesI;
import fr.upmc.datacenter.hardware.processors.interfaces.ProcessorServicesNotificationConsumerI;
import fr.upmc.datacenter.hardware.processors.interfaces.ProcessorServicesNotificationI;
import fr.upmc.datacenter.hardware.processors.ports.ProcessorServicesNotificationInboundPort;
import fr.upmc.datacenter.hardware.processors.ports.ProcessorServicesOutboundPort;
import fr.upmc.datacenter.interfaces.PushModeControllingI;
import fr.upmc.datacenter.software.applicationvm.ApplicationVM;
import fr.upmc.datacenter.software.applicationvm.connectors.ApplicationVMManagementConnector;
import fr.upmc.datacenter.software.applicationvm.interfaces.ApplicationVMManagementI;
import fr.upmc.datacenter.software.applicationvm.interfaces.TaskI;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementInboundPort;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.datacenter.software.interfaces.RequestI;
import fr.upmc.datacenter.software.interfaces.RequestNotificationHandlerI;
import fr.upmc.datacenter.software.interfaces.RequestNotificationI;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionHandlerI;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionI;
import fr.upmc.datacenter.software.ports.RequestNotificationInboundPort;
import fr.upmc.datacenter.software.ports.RequestNotificationOutboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionInboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort;

public class VmSwitcher 
extends		AbstractComponent
implements	ProcessorServicesNotificationConsumerI,
			RequestSubmissionHandlerI,
			RequestNotificationHandlerI
{

	/** URI of this VM switcher */
	protected String vmsURI;
	
	private RequestSubmissionInboundPort requestSubmissionInboundPort;
	private RequestNotificationOutboundPort requestNotificationOutboundPort;
	
	/** InboundPort to handle VM notification */
	protected RequestNotificationInboundPort  requestNotificationInboundPort;

	/** List of OutboundPort to resend requests to VM */
	protected List<RequestSubmissionOutboundPort> outBoundPortList;
	
	public VmSwitcher(String vmsURIString, String requestSubmissionInboundPortURI, String requestNotificationOutboundPortURI, List<String> vmList, String requestNotificationInboundPortURI) throws Exception {
		
				super(1,1);
		
				// Preconditions
				assert	requestSubmissionInboundPortURI != null ;
				assert	requestNotificationOutboundPortURI != null ;
				
				// Interfaces and ports

				this.addOfferedInterface(RequestSubmissionI.class) ;
				this.requestSubmissionInboundPort =
								new RequestSubmissionInboundPort(
												requestSubmissionInboundPortURI, this) ;
				this.addPort(this.requestSubmissionInboundPort) ;
				this.requestSubmissionInboundPort.publishPort() ;
				
				this.addRequiredInterface(RequestNotificationI.class) ;
				this.requestNotificationOutboundPort =
					new RequestNotificationOutboundPort(
											requestNotificationOutboundPortURI,
											this) ;
				this.addPort(this.requestNotificationOutboundPort) ;
				this.requestNotificationOutboundPort.publishPort() ;
				
				this.outBoundPortList = new ArrayList<RequestSubmissionOutboundPort>();
				
				this.addOfferedInterface( RequestNotificationI.class );
				this.requestNotificationInboundPort = 
								new RequestNotificationInboundPort( 
												requestNotificationInboundPortURI, this);
				this.addPort( this.requestNotificationInboundPort );
				this.requestNotificationInboundPort.publishPort();
				
				this.addRequiredInterface( RequestSubmissionI.class );
				for ( int i = 0 ; i < vmList.size() ; i++ ) {
					this.outBoundPortList.add( new RequestSubmissionOutboundPort( vmList.get( i ) , this ) );
					this.addPort( this.outBoundPortList.get( i ) );
					this.outBoundPortList.get( i ).publishPort();
				}
				

	}

	@Override
	public void acceptRequestSubmission(RequestI r) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void acceptRequestSubmissionAndNotify(RequestI r) throws Exception {
		// TODO Auto-generated method stub
		
		System.out.println("GOTCHA");
		
		RequestSubmissionOutboundPort port = this.outBoundPortList.get(0);
		port.submitRequestAndNotify( r );
	}

	@Override
	public void acceptNotifyEndOfTask(TaskI t) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void acceptRequestTerminationNotification(RequestI r) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
