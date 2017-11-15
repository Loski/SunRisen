package fr.upmc.datacenterclient.applicationprovider;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.datacenter.software.connectors.RequestSubmissionConnector;
import fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort;
import fr.upmc.datacenterclient.applicationprovider.interfaces.ApplicationNotificationI;
import fr.upmc.datacenterclient.applicationprovider.interfaces.ApplicationProviderManagementI;
import fr.upmc.datacenterclient.applicationprovider.interfaces.ApplicationSubmissionI;
import fr.upmc.datacenterclient.applicationprovider.ports.ApplicationNotificationOutboundPort;
import fr.upmc.datacenterclient.applicationprovider.ports.ApplicationProviderManagementInboundPort;
import fr.upmc.datacenterclient.applicationprovider.ports.ApplicationSubmissionOutboundPort;
import fr.upmc.datacenterclient.requestgenerator.RequestGenerator;
import fr.upmc.datacenterclient.requestgenerator.connectors.RequestGeneratorManagementConnector;
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

    /** the outbound port to notify that the requestgenerator has been created */
    protected ApplicationNotificationOutboundPort anop;

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

	public ApplicationProvider(String apURI,  String asoUri, String anoUri, String mipUri)  throws Exception{
		super(false, false);
        
		
		rgmipUri = apURI + "-rgmip";
        rsopUri = apURI + "-rsop";
        rnipUri = apURI + "-rnip";
		rgUri = apURI + "-rg";
        rgmopUri = apURI + "-rgmop";
        
        

        //Send a application to the controller.
        this.apURI = apURI;
        this.addRequiredInterface( ApplicationSubmissionI.class );
        this.asop = new ApplicationSubmissionOutboundPort( asoUri , this );
        this.addPort( asop );
        this.asop.localPublishPort();

        
        //send HERE
        this.addOfferedInterface( ApplicationProviderManagementI.class );
        this.apmip = new ApplicationProviderManagementInboundPort( mipUri, ApplicationProviderManagementI.class, this);
        this.addPort( this.apmip );
        this.apmip.publishPort();
	}
	
	@Override
	public void createAndSendApplication() throws Exception {
        String rdnopUri = this.asop.submitApplication( 2 );

        if ( rdnopUri != null ) {

            // Creation dynamique du request generator
            System.out.println( "creating RequestGenerator" );
            RequestGenerator rg = new RequestGenerator( rgUri , 500.0 , 6000000000L , rgmipUri , rsopUri , rnipUri );
            AbstractCVM.theCVM.addDeployedComponent( rg );
           
            RequestSubmissionOutboundPort rsop = ( RequestSubmissionOutboundPort ) rg.findPortFromURI( rsopUri );
            
            rsop.doConnection( rdnopUri , RequestSubmissionConnector.class.getCanonicalName() );
 
            rg.toggleTracing();
            rg.toggleLogging();

            rgmop = new RequestGeneratorManagementOutboundPort( rgmopUri , this );
            rgmop.localPublishPort();
            
            rgmop.doConnection( rgmipUri , RequestGeneratorManagementConnector.class.getCanonicalName() );

            anop.notifyRequestGeneratorCreated( rnipUri , rdnopUri );
            startApplication(rg);
        }
        else
            System.err.println("Pas de resources disponibles" );
    }

    

    /**
     * Stop the application, call request generator stop function
     * 
     * @throws Exception
     */
    public void stopApplication() throws Exception {
        rgmop.stopGeneration();
    }
    
    /**
     * Start the application, call request generator start function
     * 
     * @throws Exception
     */
    public void startApplication(RequestGenerator rg) throws Exception {
        rg.start();
        rg.startGeneration();    
     }


    
    
}
