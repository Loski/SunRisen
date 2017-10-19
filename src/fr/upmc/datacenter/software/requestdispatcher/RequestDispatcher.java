package fr.upmc.datacenter.software.requestdispatcher;

import java.util.ArrayList;
import java.util.List;

import fr.upmc.components.AbstractComponent;
import fr.upmc.datacenter.hardware.processors.interfaces.ProcessorServicesNotificationConsumerI;
import fr.upmc.datacenter.software.applicationvm.interfaces.TaskI;
import fr.upmc.datacenter.software.interfaces.RequestI;
import fr.upmc.datacenter.software.interfaces.RequestNotificationHandlerI;
import fr.upmc.datacenter.software.interfaces.RequestNotificationI;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionHandlerI;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionI;
import fr.upmc.datacenter.software.ports.RequestNotificationInboundPort;
import fr.upmc.datacenter.software.ports.RequestNotificationOutboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionInboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort;

public class RequestDispatcher 
extends		AbstractComponent
implements	ProcessorServicesNotificationConsumerI,
			RequestSubmissionHandlerI,
			RequestNotificationHandlerI
{

	/** URI of this VM switcher */
	protected String vmsURI;
	
	/** InboundPort to receive submission*/
	private RequestSubmissionInboundPort requestSubmissionInboundPort;
	
	/** OutboundPort to send notification*/
	private RequestNotificationOutboundPort requestNotificationOutboundPort;
	
	/** InboundPort to receive VM notification */
	protected RequestNotificationInboundPort  requestNotificationInboundPort;

	/** List of OutboundPort to resend requests to VM */
	protected List<RequestSubmissionOutboundPort> outBoundPortList;
	
	public RequestDispatcher(String vmsURIString, String requestSubmissionInboundPortURI, String requestNotificationOutboundPortURI, List<String> vmList, String requestNotificationInboundPortURI) throws Exception {
		
				super(1,1);
		
				// Preconditions
				assert	requestSubmissionInboundPortURI != null ;
				assert	requestNotificationOutboundPortURI != null ;
				
				// Interfaces and ports

					//To communicate with the sender of the request
				
				this.addOfferedInterface( RequestNotificationI.class );
				this.requestNotificationInboundPort = 
								new RequestNotificationInboundPort( 
												requestNotificationInboundPortURI, this);
				this.addPort( this.requestNotificationInboundPort );
				this.requestNotificationInboundPort.publishPort();
				
				this.addRequiredInterface(RequestSubmissionI.class) ;
				this.requestSubmissionInboundPort =
								new RequestSubmissionInboundPort(
												requestSubmissionInboundPortURI, this) ;
				this.addPort(this.requestSubmissionInboundPort) ;
				this.requestSubmissionInboundPort.publishPort() ;
				
					//To communicate with the VMs

				this.addRequiredInterface(RequestNotificationI.class) ;
				this.requestNotificationOutboundPort =
					new RequestNotificationOutboundPort(
											requestNotificationOutboundPortURI,
											this) ;
				this.addPort(this.requestNotificationOutboundPort) ;
				this.requestNotificationOutboundPort.publishPort() ;
				
				this.outBoundPortList = new ArrayList<RequestSubmissionOutboundPort>();
				this.addOfferedInterface( RequestSubmissionI.class );
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
		
		RequestSubmissionOutboundPort port = this.outBoundPortList.get(0);
		port.submitRequestAndNotify( r );
	}

	@Override
	public void acceptNotifyEndOfTask(TaskI t) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void acceptRequestTerminationNotification(RequestI r) throws Exception {
		
		this.requestNotificationOutboundPort.notifyRequestTermination( r );
	}

}
