package fr.upmc.datacenter.software.requestdispatcher;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.datacenter.software.applicationvm.interfaces.ApplicationVMManagementI;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementInboundPort;
import fr.upmc.datacenter.software.connectors.RequestNotificationConnector;
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
	protected LinkedHashMap<String,RequestSubmissionOutboundPort> requestSubmissionOutboundPortList;
	
	/** index of the VM in the requestSubmissionOutboundPortList which will receive the next request*/
	private int currentVM;
	
	/** Inbound port offering the management interface.						*/
	protected RequestDispatcherManagementInboundPort requestDispatcherManagementInboundPort ;
	
	public RequestDispatcher(
			String uri, 
			String requestDispatcherManagementInboundPortURI, 
			String requestSubmissionInboundPortURI, 
			String requestNotificationOutboundPortURI, 
			//List<String> requestSubmissionOutboundPortList, 
			String requestNotificationInboundPortURI
			) throws Exception 
	{
		
				super(1,1);
		
				// Preconditions
				assert	uri != null ;
				assert	requestDispatcherManagementInboundPortURI != null ;
				assert	requestSubmissionInboundPortURI != null ;
				assert	requestNotificationOutboundPortURI != null ;
				assert	requestNotificationInboundPortURI != null;
				//assert	requestSubmissionOutboundPortList != null;
				
				this.rdURI=uri;
				
				// Interfaces and ports

				this.addOfferedInterface(RequestDispatcherManagementI.class) ;
				this.requestDispatcherManagementInboundPort =
						new RequestDispatcherManagementInboundPort(
								requestDispatcherManagementInboundPortURI,
								this) ;
				this.addPort(this.requestDispatcherManagementInboundPort) ;
				this.requestDispatcherManagementInboundPort.publishPort() ;
				
					//To communicate with the sender of the request
				
				this.addOfferedInterface( RequestNotificationI.class );
				this.requestNotificationInboundPort = 
								new RequestNotificationInboundPort( 
												requestNotificationInboundPortURI, this);
				this.addPort( this.requestNotificationInboundPort );
				this.requestNotificationInboundPort.publishPort();
				
				this.addOfferedInterface(RequestSubmissionI.class) ;
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
				
				this.requestSubmissionOutboundPortList = new LinkedHashMap<String,RequestSubmissionOutboundPort>();
				this.addRequiredInterface( RequestSubmissionI.class );
	}
	
	private void nextVM()
	{
		this.currentVM = (this.currentVM+1)%this.requestSubmissionOutboundPortList.size();
	}

	private RequestSubmissionOutboundPort getCurrentVMPort()
	{
		return (RequestSubmissionOutboundPort) this.requestSubmissionOutboundPortList.values().toArray()[this.currentVM];
	}
	
	private String getCurrentVMURI()
	{		
		return (String) this.requestSubmissionOutboundPortList.keySet().toArray()[this.currentVM];
	}
	
	@Override
	public void acceptRequestSubmission(RequestI r) throws Exception {

		assert r != null;
		
		RequestSubmissionOutboundPort port = getCurrentVMPort();
		port.submitRequest(r);
		
		this.nextVM();
	}

	@Override
	public void acceptRequestSubmissionAndNotify(RequestI r) throws Exception {
		
		assert r != null;
		
		RequestSubmissionOutboundPort port = getCurrentVMPort();
		
		System.out.println(String.format("%s transfers %s to %s using %s",this.rdURI,r.getRequestURI(),getCurrentVMURI(),port.getPortURI()));
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
	            for (Entry<String, RequestSubmissionOutboundPort> entry : requestSubmissionOutboundPortList.entrySet())
	            {
	            	RequestSubmissionOutboundPort port = entry.getValue();
	            	if (port.connected() ) {
	            		port.doDisconnection();
	     	       }
	            }
	               
	        }
	        catch ( Exception e ) {
	            throw new ComponentShutdownException( e );
	        }

	        super.shutdown();
	}

	@Override
	public void connectVirtualMachine(String vmURI, String requestSubmissionInboundPortURI) throws Exception {
		
		String portURI = "vmPort-"+this.requestSubmissionOutboundPortList.size();
		RequestSubmissionOutboundPort port = new RequestSubmissionOutboundPort( portURI, this );
		
		this.requestSubmissionOutboundPortList.put("vmURI",port );
		this.addPort( port );
		port.publishPort();
		
		this.doPortConnection(
				port.getPortURI(),
				requestSubmissionInboundPortURI,
				RequestSubmissionConnector.class.getCanonicalName());
		
		System.out.println(String.format("[RequestSubmissionConnector] Connecting %s with %s using %s -> %s", this.rdURI,vmURI,port.getPortURI(),requestSubmissionInboundPortURI));

	}

	@Override
	public void disconnectVirtualMachine() throws Exception {
		
	}

	@Override
	public void connectWithRequestGenerator(String rgURI, String requestNotificationInboundPortURI) throws Exception {
		
		this.doPortConnection(
				this.requestNotificationOutboundPort.getPortURI(),
				requestNotificationInboundPortURI,
				RequestNotificationConnector.class.getCanonicalName());
		
		System.out.println(String.format("[RequestNotificationConnector] Connecting %s with %s using %s -> %s", this.rdURI,rgURI,this.requestNotificationOutboundPort.getPortURI(),requestNotificationInboundPortURI));
	}

	@Override
	public void disconnectRequestGenerator() throws Exception {
		// TODO Auto-generated method stub
	}

}
