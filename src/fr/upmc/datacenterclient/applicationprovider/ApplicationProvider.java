package fr.upmc.datacenterclient.applicationprovider;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.datacenter.software.connectors.RequestSubmissionConnector;
import fr.upmc.datacenter.software.ports.RequestNotificationInboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort;
import fr.upmc.datacenter.software.requestdispatcher.ports.RequestDispatcherManagementInboundPort;
import fr.upmc.datacenter.software.requestdispatcher.ports.RequestDispatcherManagementOutboundPort;
import fr.upmc.datacenterclient.applicationprovider.interfaces.ApplicationProviderManagementI;
import fr.upmc.datacenterclient.applicationprovider.interfaces.ApplicationSubmissionI;
import fr.upmc.datacenterclient.applicationprovider.ports.ApplicationProviderManagementInboundPort;
import fr.upmc.datacenterclient.applicationprovider.ports.ApplicationSubmissionOutboundPort;
import fr.upmc.datacenterclient.requestgenerator.RequestGenerator;
import fr.upmc.datacenterclient.requestgenerator.connectors.RequestGeneratorManagementConnector;
import fr.upmc.datacenterclient.requestgenerator.ports.RequestGeneratorManagementInboundPort;
import fr.upmc.datacenterclient.requestgenerator.ports.RequestGeneratorManagementOutboundPort;

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
    private static int indice_rdmo_uri = 1;
    private static int indice_rdso_uri = 2;



	public ApplicationProvider(String apURI,  String applicationSubmissionOutboundPortURI, String applicationManagementInboundPort)  throws Exception{
		super(1, 1);
        
		
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
        this.asop.localPublishPort();

        
        //send HERE
        this.addOfferedInterface( ApplicationProviderManagementI.class );
        this.apmip = new ApplicationProviderManagementInboundPort(applicationManagementInboundPort, ApplicationProviderManagementI.class, this);
        this.addPort( this.apmip );
        this.apmip.publishPort();
        
        
	}
	
	@Override
	public void createAndSendApplication() throws Exception {
		rdUri = this.asop.submitApplication(apURI,  2 );
        if ( rdUri != null ) {
            // Creation dynamique du request generator
            System.out.println( "creating RequestGenerator" );
            rnipUri =rdUri[0] + rnipUri; 
            RequestGenerator rg = new RequestGenerator( rgUri , 500.0 , 6000000000L , rgmipUri , rsopUri , rnipUri );
            AbstractCVM.theCVM.addDeployedComponent( rg );
           
            RequestSubmissionOutboundPort rsop = ( RequestSubmissionOutboundPort ) rg.findPortFromURI( rsopUri );
            RequestSubmissionOutboundPort rnip = ( RequestSubmissionOutboundPort ) rg.findPortFromURI( rnipUri );

            rsop.doConnection( rdUri[indice_rdso_uri] , RequestSubmissionConnector.class.getCanonicalName() );
 
            rg.toggleTracing();
            rg.toggleLogging();

            rgmop = new RequestGeneratorManagementOutboundPort( rgmopUri , this );
            rgmop.localPublishPort();
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
			// tuer dispatcher???
		} catch (Exception e) {
			throw new ComponentShutdownException("Port disconnection error", e);
		}
		super.shutdown();
	}


    
    
}
