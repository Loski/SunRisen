package fr.upmc.PriseTheSun.datacenterclient.software.applicationprovider;


import fr.upmc.PriseTheSun.datacenter.software.javassist.ConnectorCreator;
import fr.upmc.PriseTheSun.datacenterclient.software.applicationprovider.connectors.ApplicationSubmissionConnector;
import fr.upmc.PriseTheSun.datacenterclient.software.applicationprovider.interfaces.ApplicationProviderManagementI;
import fr.upmc.PriseTheSun.datacenterclient.software.applicationprovider.interfaces.ApplicationSubmissionI;
import fr.upmc.PriseTheSun.datacenterclient.software.applicationprovider.ports.ApplicationProviderManagementInboundPort;
import fr.upmc.PriseTheSun.datacenterclient.software.applicationprovider.ports.ApplicationSubmissionOutboundPort;
import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.components.exceptions.ComponentStartException;
import fr.upmc.datacenter.software.connectors.RequestSubmissionConnector;
import fr.upmc.datacenterclient.requestgenerator.RequestGenerator;
import fr.upmc.datacenterclient.requestgenerator.connectors.RequestGeneratorManagementConnector;
import fr.upmc.datacenterclient.requestgenerator.ports.RequestGeneratorManagementOutboundPort;


/**
 <code>ApplicationProvider</code> est responsable de créer et de connecter un <code>requestGenerator</code> à un <code>requestDispatcher</code>.
 Il doit soumettre une demande au contrôleur d'admission et recevoir les URIS du <code>requestDispatcher</code> créée par le contrôleur d'admission.
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
        this.apmip = new ApplicationProviderManagementInboundPort(applicationManagementInboundPort, this);
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
        asop.stopApplication(this.apURI);
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
		rdUri = this.asop.submitApplication(apURI, 2);
		System.err.println("Request submitted, the reply is : " + rdUri[0]);
		
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
