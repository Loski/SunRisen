package fr.upmc.datacenterclient.applicationprovider;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.datacenter.software.connectors.RequestSubmissionConnector;
import fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort;
import fr.upmc.datacenterclient.applicationprovider.interfaces.ApplicationNotificationI;
import fr.upmc.datacenterclient.applicationprovider.interfaces.ApplicationProviderManagementI;
import fr.upmc.datacenterclient.applicationprovider.interfaces.ApplicationSubmissionI;
import fr.upmc.datacenterclient.applicationprovider.ports.ApplicationProviderManagementInboundPort;
import fr.upmc.datacenterclient.applicationprovider.ports.ApplicationSubmissionOutboundPort;
import fr.upmc.datacenterclient.requestgenerator.RequestGenerator;
import fr.upmc.datacenterclient.requestgenerator.connectors.RequestGeneratorManagementConnector;
import fr.upmc.datacenterclient.requestgenerator.ports.RequestGeneratorManagementOutboundPort;
import javafx.application.Application;

public class ApplicationProvider extends AbstractComponent {

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
  //  protected ApplicationNotificationOutboundPort anop;

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

    // protected static int i = 0;

    /**
     * Create an application provider
     * 
     * @param applicationSubmissionOutboundPortURI URI of the application submission outbound port
     * @param applicationNotificationOutboundPortURI URI of the application notification outbound
     *            port
     * @param managementInboundPortURI URI of the application provider management inbound port
     * @throws Exception
     */
    public ApplicationProvider( String apURI , String applicationSubmissionOutboundPortURI ,
            String applicationNotificationOutboundPortURI , String managementInboundPortURI ) throws Exception {
        super( false , true );
        this.apURI = apURI;
        this.addRequiredInterface( ApplicationSubmissionI.class );
        this.asop = new ApplicationSubmissionOutboundPort( applicationSubmissionOutboundPortURI, ApplicationSubmissionI.class , this );
        this.addPort( asop );
        this.asop.localPublishPort();

        this.addRequiredInterface( ApplicationNotificationI.class );
        this.anop = new ApplicationNotificationOutboundPort( applicationNotificationOutboundPortURI , this );
        this.addPort( anop );

        this.addOfferedInterface( ApplicationProviderManagementI.class );
        this.apmip = new ApplicationProviderManagementInboundPort(ApplicationProviderManagementI.class, this );
        this.addPort( this.apmip );
        this.apmip.publishPort();
        
        // Ports of the request generator
        rgUri = apURI + "-rg";
        rgmipUri = apURI + "-rgmip";
        rsopUri = apURI + "-rsop";
        rnipUri = apURI + "-rnip";
        rgmopUri = apURI + "-rgmop";

    }

    /**
     * Submit an application to the admission controller
     * 
     * @throws Exception
     */
    public void sendApplication() throws Exception {
        print( "Submit an application" );
        print( "Waiting for URI" );
      //  String res[] = this.asop.sendApplication();
        String requestDispatcherURI = "switch0";

        print( "URI received" );
        if ( requestDispatcherURI != null ) {

            // Creation dynamique du request generator
            print( "creating RequestGenerator" );
            RequestGenerator rg = new RequestGenerator( rgUri , 500.0 , 6000000000L , rgmipUri , rsopUri , rnipUri );
            AbstractCVM.theCVM.addDeployedComponent( rg );
            
            
            RequestSubmissionOutboundPort rsop = ( RequestSubmissionOutboundPort ) rg.findPortFromURI( rsopUri );
            rsop.doConnection( requestDispatcherURI , RequestSubmissionConnector.class.getCanonicalName() );

            rg.toggleTracing();
            rg.toggleLogging();
 
            rgmop = new RequestGeneratorManagementOutboundPort( rgmopUri , this );
            rgmop.publishPort();
            rgmop.doConnection( rgmipUri , RequestGeneratorManagementConnector.class.getCanonicalName() );

            //String rdnopUri = res[1];
 
            rg.start();

            rg.startGeneration();

        }
        else
            print( "Pas de resources disponibles" );
    }

    /**
     * Stop the application, it means it stops the requestgenerator
     * 
     * @throws Exception
     */
    public void stopApplication() throws Exception {
        rgmop.stopGeneration();

    }

    private void print( String s ) {
        this.logMessage( "[ApplicationProvider " + apURI + "] " + s );
    }

    @Override
    public void shutdown() throws ComponentShutdownException {
        try {
            if ( this.asop.connected() ) {
                this.asop.doDisconnection();
            }
            if ( this.rgmop.connected() ) {
                this.rgmop.doDisconnection();
            }
     /*       if ( this.anop.connected() ) {
                this.anop.doDisconnection();
            } */
      
        }
        catch ( Exception e ) {
            throw new ComponentShutdownException( e );
        }
        super.shutdown();
}
}
