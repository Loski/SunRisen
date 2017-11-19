package fr.upmc.datacenterclient.applicationprovider;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.datacenter.software.connectors.RequestSubmissionConnector;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionI;
import fr.upmc.datacenter.software.ports.RequestNotificationInboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionInboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort;
import fr.upmc.datacenter.software.requestdispatcher.ports.RequestDispatcherManagementInboundPort;
import fr.upmc.datacenter.software.requestdispatcher.ports.RequestDispatcherManagementOutboundPort;
import fr.upmc.datacenterclient.applicationprovider.connectors.ApplicationSubmissionConnector;
import fr.upmc.datacenterclient.applicationprovider.interfaces.ApplicationProviderManagementI;
import fr.upmc.datacenterclient.applicationprovider.interfaces.ApplicationSubmissionI;
import fr.upmc.datacenterclient.applicationprovider.ports.ApplicationProviderManagementInboundPort;
import fr.upmc.datacenterclient.applicationprovider.ports.ApplicationSubmissionOutboundPort;
import fr.upmc.datacenterclient.requestgenerator.RequestGenerator;
import fr.upmc.datacenterclient.requestgenerator.connectors.RequestGeneratorManagementConnector;
import fr.upmc.datacenterclient.requestgenerator.ports.RequestGeneratorManagementInboundPort;
import fr.upmc.datacenterclient.requestgenerator.ports.RequestGeneratorManagementOutboundPort;
import fr.upmc.javassist.ConnectorCreator;
/**
 * Application provider is responsible to create and connect a requestGenerator to a requestDispatcher.
 * He needs to submit an application to the admissionController and receive the requestDispatcher create by the admissionController.
 * 
 * @author maxime Lavaste
 *
 */
public class ApplicationProvider extends AbstractComponent implements ApplicationProviderManagementI{

	
    /** the URI of the component. */
    protected String apURI;

    // ------------------------------------------------------------------
    // PORTS
    // ------------------------------------------------------------------
    /** the outbound port used to send application to the admission controller. */
    protected ApplicationSubmissionOutboundPort asop;

    /** the outbound port used to start or stop the requestgenerator dynamically created */
    protected RequestGeneratorManagementOutboundPort rgmop;

    /** the inbound port used to send/stop application **/
    protected ApplicationProviderManagementInboundPort apmip;
    
    
    
    

    // ------------------------------------------------------------------
    // REQUEST GENERATOR URIs
    // ------------------------------------------------------------------
    /** RequestGenerator URI */
    protected String rgUri;

    /** Request generator management inbound port */
    protected String rgmipUri;

    /** Request submission outbound port */
    protected String rsopUri;

    /** Request notification inbound port */
    protected String rnipUri;

    /** Request generator management outbound port */
    protected String rgmopUri;
    
    protected String[] rdUri;
    RequestNotificationInboundPort rnip;
    
    
    private static int indice_rd_uri = 0;
    private static int indice_rdsin_uri = 2;


    /**
     * Create an application
     * @param apURI URI of the app
     * @param applicationSubmissionInboundPortURI Port to submit application to the administration controller.
     * @param applicationSubmissionOutboundPortURI URI port of the administration controller. Receive app from the application Provider.
     * @param applicationManagementInboundPort URI port to receive orders from the main class.
     * @throws Exception
     */
	public ApplicationProvider(String apURI,  String applicationSubmissionInboundPortURI, String applicationSubmissionOutboundPortURI, String applicationManagementInboundPort)  throws Exception{
		super(apURI,1, 1);
        
		
		rgmipUri = apURI + "-rgmip";
        rsopUri = apURI + "-rsop";
        rnipUri = apURI + "-rnip";
		rgUri = apURI + "-rg";
        rgmopUri = apURI + "-rgmop";
        
        

        //Send a application to the controller.
        this.apURI = apURI;
        this.addRequiredInterface( ApplicationSubmissionI.class );
        this.asop = new ApplicationSubmissionOutboundPort( applicationSubmissionOutboundPortURI , this );
        this.addPort( asop );
        this.asop.publishPort();

        
        //send HERE
        this.addOfferedInterface( ApplicationProviderManagementI.class );
        this.apmip = new ApplicationProviderManagementInboundPort(applicationManagementInboundPort, ApplicationProviderManagementI.class, this);
        this.addPort( this.apmip );
        this.apmip.localPublishPort();
        this.asop.doConnection(applicationSubmissionInboundPortURI, ApplicationSubmissionConnector.class.getCanonicalName());    
	}
	

	@Override
	public void createAndSendApplication() throws Exception {
		System.out.println("Waiting acception of Application : "+this.apURI);
		rdUri = this.asop.submitApplication(apURI,  2 );
		System.out.println("Request submitted, the reply is : ");

        if ( rdUri != null ) {
            // Creation dynamique du request generator
            System.out.println( this.apURI+" was accepted / creating RequestGenerator" );
            rnipUri = rdUri[0] + rnipUri; 
            RequestGenerator rg = new RequestGenerator( rgUri , 500.0 , 6000000000L , rgmipUri , rsopUri , rnipUri );
            AbstractCVM.theCVM.addDeployedComponent( rg );
            
    		rg.doPortConnection(
    				rsopUri,
    				rdUri[indice_rdsin_uri],
    				RequestSubmissionConnector.class.getCanonicalName());
 
            rg.toggleTracing();
            rg.toggleLogging();
            
            rg.DEBUG_LEVEL=2;

            rgmop = new RequestGeneratorManagementOutboundPort( rgmopUri , this );
            rgmop.publishPort();
            rgmop.doConnection( rgmipUri , RequestGeneratorManagementConnector.class.getCanonicalName() );
               
            this.asop.submitGenerator(rnipUri, apURI, rgUri);
                     
            rg.start();
            startApplication();
        }
        else
            System.err.println("Pas de resources disponibles" );
    }

    

    /**
     * Stop the generation of requests
     * 
     * @throws Exception
     */
    public void stopApplication() throws Exception {
        rgmop.stopGeneration();
    }
    
    /**
     * Start the generation of requests
     * 
     * @throws Exception
     */
    public void startApplication() throws Exception {
        this.rgmop.startGeneration();    
     }

	@Override
	public void shutdown() throws ComponentShutdownException {
		try {			
			if (this.rgmop.connected()) {
				this.rgmop.doDisconnection();
			}
			if (this.asop.connected()) {
				this.asop.doDisconnection();
			}
		} catch (Exception e) {
			throw new ComponentShutdownException("Port disconnection error", e);
		}
		super.shutdown();
	}


	public void createAndSendApplication(Class submissionInterface) throws Exception{
		
		assert submissionInterface.isInterface();
		
		System.out.println("Waiting acception of Application ["+this.apURI+"] with submissionInterface ["+submissionInterface.getCanonicalName()+"]");
		rdUri = this.asop.submitApplication(apURI, 2, submissionInterface);
		System.out.println("Request submitted, the reply is : ");
		
        if ( rdUri != null ) {
            // Creation dynamique du request generator
        	  System.out.println( this.apURI+" was accepted / creating RequestGenerator" );
            rnipUri = rdUri[0] + rnipUri; 
            RequestGenerator rg = new RequestGenerator( rgUri , 500.0 , 6000000000L , rgmipUri , rsopUri , rnipUri );
            AbstractCVM.theCVM.addDeployedComponent( rg );
            
    		rg.doPortConnection(
    				rsopUri,
    				rdUri[indice_rdsin_uri],
    				ConnectorCreator.createConnectorImplementingInterface("RG-Connector-Of-"+this.apURI, submissionInterface).getCanonicalName());
 
            rg.toggleTracing();
            rg.toggleLogging();
            
            rg.DEBUG_LEVEL=2;

            rgmop = new RequestGeneratorManagementOutboundPort( rgmopUri , this );
            rgmop.publishPort();
            rgmop.doConnection( rgmipUri , RequestGeneratorManagementConnector.class.getCanonicalName() );
               
            this.asop.submitGenerator(rnipUri, apURI, rgUri);
                     
            rg.start();
            startApplication();
        }
        else
            System.err.println("Pas de resources disponibles" );
		
	}


    
    
}
