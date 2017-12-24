package fr.upmc.datacenterclient.tests;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.connectors.DataConnector;
import fr.upmc.components.cvm.AbstractDistributedCVM;
import fr.upmc.datacenter.connectors.ControlledDataConnector;
import fr.upmc.datacenter.hardware.computers.Computer;
import fr.upmc.datacenter.hardware.computers.connectors.ComputerServicesConnector;
import fr.upmc.datacenter.hardware.computers.ports.ComputerServicesOutboundPort;
import fr.upmc.datacenter.hardware.processors.Processor;
import fr.upmc.datacenter.hardware.tests.ComputerMonitor;
import fr.upmc.datacenter.software.admissioncontroller.AdmissionControllerDynamic;
import fr.upmc.datacenter.software.applicationvm.ApplicationVM;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionI;
import fr.upmc.datacenterclient.applicationprovider.ApplicationProvider;
import fr.upmc.datacenterclient.applicationprovider.connectors.ApplicationProviderManagementConnector;
import fr.upmc.datacenterclient.applicationprovider.ports.ApplicationProviderManagementOutboundPort;

public class TestDCVM extends AbstractDistributedCVM{
	
	protected static String		AdmissionController = "controller" ;
	protected static String		Application1 = "application1" ;
	protected static String		Application2 = "application2" ;
	
	
	public static final String	ComputerServicesInboundPortURI = "cs-ibp" ;
	public static final String	ComputerServicesOutboundPortURI = "cs-obp" ;
	public static final String	ComputerStaticStateDataInboundPortURI = "css-dip" ;
	public static final String	ComputerStaticStateDataOutboundPortURI = "css-dop" ;
	public static final String	ComputerDynamicStateDataInboundPortURI = "cds-dip" ;
	public static final String	ComputerDynamicStateDataOutboundPortURI = "cds-dop" ;
	
	
	protected ApplicationProvider ap;
	protected ApplicationProvider ap2;
	private String applicationSubmissionInboundPortURI = "asip";
	private String AdmissionControllerManagementInboundPortURI = "acmip";
	
	/** Port connected to the computer component to access its services.	*/
	protected static ComputerServicesOutboundPort			csPort ;
	/** 	Computer monitor component.										*/
	protected static ComputerMonitor						cm ;

	private int nbAvailableCores = 26;
	private String applicationSubmissionOutboundPortURI = "asop";
	private String applicationManagementInboundPort = " amip";
	protected AdmissionControllerDynamic ac;
	private ApplicationProviderManagementOutboundPort apmop;
	private ApplicationProviderManagementOutboundPort apmop2;
	
	
	@Override
	public void instantiateAndPublish() throws Exception {

		
		if (thisJVMURI.equals(AdmissionController)) {
			AbstractComponent.configureLogging("", "", 0, '|') ;
			Processor.DEBUG = true ;

			// --------------------------------------------------------------------
			// Create and deploy a computer component with its 2 processors and
			// each with 2 cores.
			// --------------------------------------------------------------------
			String computerURI = "computer0" ;
			int numberOfProcessors = 2 ;
			int numberOfCores = 20;
			Set<Integer> admissibleFrequencies = new HashSet<Integer>() ;
			admissibleFrequencies.add(1500) ;	// Cores can run at 1,5 GHz
			admissibleFrequencies.add(3000) ;	// and at 3 GHz
			Map<Integer,Integer> processingPower = new HashMap<Integer,Integer>() ;
			processingPower.put(1500, 1500000) ;	// 1,5 GHz executes 1,5 Mips
			processingPower.put(3000, 3000000) ;	// 3 GHz executes 3 Mips
			Computer c = new Computer(
								computerURI,
								admissibleFrequencies,
								processingPower,  
								1500,		// Test scenario 1, frequency = 1,5 GHz
								// 3000,	// Test scenario 2, frequency = 3 GHz
								1500,		// max frequency gap within a processor
								numberOfProcessors,
								numberOfCores,
								ComputerServicesInboundPortURI,
								ComputerStaticStateDataInboundPortURI,
								ComputerDynamicStateDataInboundPortURI) ;
			this.addDeployedComponent(c) ;
			// --------------------------------------------------------------------
			// Create the computer monitor component and connect its to ports
			// with the computer component.
			// --------------------------------------------------------------------
			this.cm = new ComputerMonitor(computerURI,
										 true,
										 ComputerStaticStateDataOutboundPortURI,
										 ComputerDynamicStateDataOutboundPortURI) ;
			this.addDeployedComponent(this.cm) ;
			this.cm.doPortConnection(
							ComputerStaticStateDataOutboundPortURI,
							ComputerStaticStateDataInboundPortURI,
							DataConnector.class.getCanonicalName()) ;

			this.cm.doPortConnection(
						ComputerDynamicStateDataOutboundPortURI,
						ComputerDynamicStateDataInboundPortURI,
						ControlledDataConnector.class.getCanonicalName()) ;
			System.out.println("create controller");
			this.ac = new AdmissionControllerDynamic("Controller", applicationSubmissionInboundPortURI, AdmissionControllerManagementInboundPortURI, ComputerServicesOutboundPortURI+"-controller", ComputerServicesInboundPortURI, nbAvailableCores, ComputerStaticStateDataOutboundPortURI+"-controller",AdmissionController,AdmissionController);
			this.addDeployedComponent(this.ac);
		//	this.cyclicBarrierClient.notifyAll();
		}else if(thisJVMURI.equals(Application1)) {
			System.out.println("Appli 1 ");
			Thread.sleep(500);
			///this.cyclicBarrierClient.waitBarrier();
			this.ap = new ApplicationProvider("App1", applicationSubmissionInboundPortURI, applicationSubmissionOutboundPortURI, applicationManagementInboundPort);
			this.addDeployedComponent(this.ap);
			this.apmop = new ApplicationProviderManagementOutboundPort("apmop1", new AbstractComponent(0, 0) {});
			this.apmop.publishPort();
			this.apmop.doConnection(applicationManagementInboundPort, ApplicationProviderManagementConnector.class.getCanonicalName());
		}else if(thisJVMURI.equals(Application2)) {
			System.out.println("Appli 2 ");
			Thread.sleep(500);
			///this.cyclicBarrierClient.waitBarrier();
			this.ap2 = new ApplicationProvider("App2", applicationSubmissionInboundPortURI, applicationSubmissionOutboundPortURI+"-2", applicationManagementInboundPort+"-2");
			this.addDeployedComponent(this.ap2);
			this.apmop2 = new ApplicationProviderManagementOutboundPort("apmop2", new AbstractComponent(0, 0) {});
			this.apmop2.publishPort();
			this.apmop2.doConnection(applicationManagementInboundPort+"-2", ApplicationProviderManagementConnector.class.getCanonicalName());
		}
		super.instantiateAndPublish();
	}

	@Override
	public void start() throws Exception {
		// TODO Auto-generated method stub
		super.start();
	}

	@Override
	public void deploy() throws Exception {
		// TODO Auto-generated method stub
		super.deploy();
	}

	@Override
	public void initialise() throws Exception {
		// TODO Auto-generated method stub
		super.initialise();
	}

	public TestDCVM(String[] args) throws Exception {
		super(args);
		// TODO Auto-generated constructor stub
	}
	public static void	main(String[] args)
	{
		// Uncomment next line to execute components in debug mode.
		// AbstractCVM.toggleDebugMode() ;
		try {
			final TestDCVM trd = new TestDCVM(args) ;
			// Deploy the components
			trd.deploy() ;
			System.out.println("starting...") ;
			// Start them.
			
			
			trd.start() ;
			
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						trd.testScenario() ;
					} catch (Exception e) {
						throw new RuntimeException(e) ;
					}
				}
			}).start() ;
			// Sleep to let the test scenario execute to completion.
			Thread.sleep(90000L) ;
			// Shut down the application.
			System.out.println("shutting down...") ;
		//	trd.shutdown() ;
			System.out.println("ending...") ;
			// Exit from Java.
			System.exit(0) ;
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}

	protected void testScenario() throws Exception {
		if (thisJVMURI.equals(AdmissionController)) {}
		else if(thisJVMURI.equals(Application1)) {
			this.apmop.createAndSendApplication();
		}else if(thisJVMURI.equals(Application2)) {
			this.apmop2.createAndSendApplication(RequestSubmissionI.class);
		}
	}
}
