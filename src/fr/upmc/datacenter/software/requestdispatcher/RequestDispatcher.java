package fr.upmc.datacenter.software.requestdispatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.datacenter.hardware.processors.interfaces.ProcessorServicesNotificationConsumerI;
import fr.upmc.datacenter.software.applicationvm.interfaces.TaskI;
import fr.upmc.datacenter.software.connectors.RequestSubmissionConnector;
import fr.upmc.datacenter.software.interfaces.RequestI;
import fr.upmc.datacenter.software.interfaces.RequestNotificationHandlerI;
import fr.upmc.datacenter.software.interfaces.RequestNotificationI;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionHandlerI;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionI;
import fr.upmc.datacenter.software.ports.RequestNotificationInboundPort;
import fr.upmc.datacenter.software.ports.RequestNotificationOutboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionInboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort;
import fr.upmc.datacenter.software.requestdispatcher.interfaces.RequestDispatcherManagementI;
import fr.upmc.datacenter.software.requestdispatcher.ports.RequestDispatcherManagementInboundPort;

public class RequestDispatcher 
extends		AbstractComponent
implements	
			RequestSubmissionHandlerI,
			RequestNotificationHandlerI,
			RequestDispatcherManagementI
{

	/** URI of this request dispatcher */
	protected String rdURI;
	
	/** InboundPort to receive submission*/
	protected RequestSubmissionInboundPort requestSubmissionInboundPort;
	
	/** OutboundPort to send notification*/
	protected RequestNotificationOutboundPort requestNotificationOutboundPort;
	
	/** InboundPort to receive VM notification */
	protected RequestNotificationInboundPort  requestNotificationInboundPort;

	/** List of OutboundPort to resend requests to VM */
	protected List<RequestSubmissionOutboundPort> requestSubmissionOutboundPortList;
	
	protected HashMap<String , RequestSubmissionOutboundPort> requestApplicationVM;
	
	/** index of the VM in the requestSubmissionOutboundPortList which will receive the next request*/
	private int currentVM;
	
	/** Inbound port offering the management interface. */
	protected RequestDispatcherManagementInboundPort rdmip;
	
	/**
	 * Create a RequestDispatcher
	 * 
	 * @param rdURI URI of the RequestDispatcher
	 * @param RequestDispatcherManagementInboundPort  URI of the RequestDispatcherManagementInboundPort 
	 * @param requestSubmissionInboundPortURI URI of the RequestSubmissionInboundPort
	 * @param requestNotificationOutboundPortURI URI of the RequestSubmissionOutboundPort
	 * @param vmList List of VM
	 * @param rnip URI of the requestNotificationInboundPortURI
	 * @throws Exception
	 */
	public RequestDispatcher(String uri, String RequestDispatcherManagementInboundPortURI, String requestSubmissionInboundPortURI, String requestNotificationOutboundPortURI, List<String> vmList, String requestNotificationInboundPortURI) throws Exception {
		
				super(1,1);
		
				// Preconditions
				assert	requestSubmissionInboundPortURI != null ;
				assert	requestNotificationOutboundPortURI != null ;
				
				this.rdURI=uri;
				
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
				
				this.requestSubmissionOutboundPortList = new ArrayList<RequestSubmissionOutboundPort>();
				this.addOfferedInterface( RequestSubmissionI.class );
				
				this.requestApplicationVM = new HashMap<>();
				for ( int i = 0 ; i < vmList.size() ; i++ ) {
					this.requestSubmissionOutboundPortList.add( new RequestSubmissionOutboundPort( vmList.get( i ) , this ) );
					this.addPort( this.requestSubmissionOutboundPortList.get( i ) );
					this.requestSubmissionOutboundPortList.get( i ).publishPort();
					
					this.requestApplicationVM.put(vmList.get(i), this.requestSubmissionOutboundPortList.get( i ));
				}
				
				this.addOfferedInterface( RequestDispatcherManagementI.class );
				this.rdmip = new RequestDispatcherManagementInboundPort(RequestDispatcherManagementInboundPortURI, this );
				this.addPort( this.rdmip );
				this.rdmip.publishPort();
				
	}
	
	private void nextVM()
	{
		this.currentVM = (this.currentVM+1)%this.requestSubmissionOutboundPortList.size();
	}

	@Override
	public void acceptRequestSubmission(RequestI r) throws Exception {

		assert r != null;
		System.out.println(String.format("%s transfers %s to %s port",this.rdURI,r.getRequestURI(),this.requestSubmissionOutboundPortList.get(currentVM).getPortURI()));
		RequestSubmissionOutboundPort port = this.requestSubmissionOutboundPortList.get(this.currentVM);
		port.submitRequest(r);
		this.nextVM();
	}

	@Override
	public void acceptRequestSubmissionAndNotify(RequestI r) throws Exception {
		
		assert r != null;
		
		System.out.println(String.format("%s transfers %s to %s port",this.rdURI,r.getRequestURI(),this.requestSubmissionOutboundPortList.get(currentVM).getPortURI()));
		RequestSubmissionOutboundPort port = this.requestSubmissionOutboundPortList.get(this.currentVM);
		port.submitRequestAndNotify(r);
		
		this.nextVM();
	}

	@Override
	public void acceptRequestTerminationNotification(RequestI r) throws Exception {
		
		assert r != null;
		
		System.out.println(String.format("RequestDispatcher [%s] notifies end of request %s",this.rdURI,r.getRequestURI()));
		this.requestNotificationOutboundPort.notifyRequestTermination( r );
	}
	
	
	@Override
	public void shutdown() throws ComponentShutdownException {
		
	        try {
	            if ( this.requestNotificationOutboundPort.connected() ) {
	                this.requestNotificationOutboundPort.doDisconnection();
	            }
	            for (RequestSubmissionOutboundPort port : requestSubmissionOutboundPortList)
	                if (port.connected() ) {
	                    port.doDisconnection();
	                }
	            
	            if ( this.requestSubmissionInboundPort.connected() ) 
	            	this.requestSubmissionInboundPort.doDisconnection();
	            
	            if ( this.requestNotificationInboundPort.connected() ) 
	            	this.requestNotificationInboundPort.doDisconnection();
	        }
	        catch ( Exception e ) {
	            throw new ComponentShutdownException( e );
	        }

	        super.shutdown();
	}


	@Override
	public void connectVm(String vmURI, String RequestSubmissionInboundPortURI) throws Exception {
		/*String rdsopURI = rdURI + "rdsop" + this.requestApplicationVM.size();
		RequestSubmissionOutboundPort r = new RequestSubmissionOutboundPort( rdsopURI , this );
		this.addPort( r );
		r.localPublishPort();

		System.out.println("Connecting VM and RequestDispatcher...");
		// Connect RD with VM
		RequestSubmissionOutboundPort rsop = (RequestSubmissionOutboundPort) this.findPortFromURI(rdsopURI);
		rsop.doConnection( RequestSubmissionInboundPortURI , RequestSubmissionConnector.class.getCanonicalName() );
		System.out.println("Vm And Request Dispatcher connected");
		// update
		
		//Waiting For RequestNotificationPort Connection
		r.acceptNotificationPortURI(requestNotificationInboundPort.getPortURI());
		//rsop.submitRequest(null);
		nbRequestInQueueOrInProgress.put( r , 0 );
		this.rdsopList.add( r );
		vmrsop.put( r , vmURI );
		*/
		
	}

	@Override
	public void disconnectVm() throws Exception {
		// TODO Auto-generated method stub
		
	}



}
