package fr.upmc.PriseTheSun.tests;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.AdmissionControllerDynamic;
import fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.connector.AdmissionControllerManagementConnector;
import fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.ports.AdmissionControllerManagementOutboundPort;
import fr.upmc.PriseTheSun.datacenterclient.software.applicationprovider.ApplicationProvider;
import fr.upmc.PriseTheSun.datacenterclient.software.applicationprovider.connectors.ApplicationProviderManagementConnector;
import fr.upmc.PriseTheSun.datacenterclient.software.applicationprovider.ports.ApplicationProviderManagementOutboundPort;
import fr.upmc.components.AbstractComponent;
import fr.upmc.components.connectors.DataConnector;
import fr.upmc.components.cvm.AbstractDistributedCVM;
import fr.upmc.datacenter.connectors.ControlledDataConnector;
import fr.upmc.datacenter.hardware.computers.Computer;
import fr.upmc.datacenter.hardware.computers.ports.ComputerServicesOutboundPort;
import fr.upmc.datacenter.hardware.processors.Processor;
import fr.upmc.datacenter.hardware.tests.ComputerMonitor;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionI;


public class TestDCVM extends AbstractDistributedCVM{
	
	protected static String		AdmissionController = "controller" ;
	protected static String		Application1 = "application1" ;
	protected static String		Application2 = "application2" ;
	
	public static final int NB_COMPUTER = 30;
	private static final int NB_APPLICATION = 8;

	protected AdmissionControllerDynamic ac;
	protected AdmissionControllerManagementOutboundPort acmop;
	//protected ApplicationProvider ap[];
	//public ApplicationProviderManagementOutboundPort apmop[];

	public static final  String applicationSubmissionInboundPortURI = "asip";
	public static final String AdmissionControllerManagementInboundPortURI = "acmip";
	public static final String applicationSubmissionOutboundPortURI = "asop";
	public static final String applicationManagementInboundPort = " amip";
	
	private ApplicationProviderManagementOutboundPort apmop;
	private ApplicationProviderManagementOutboundPort apmop2;
	private ApplicationProvider ap2;
	private ApplicationProvider ap;
	
	private void createAdmissionController() throws Exception {

		this.ac = new AdmissionControllerDynamic("AdmController", applicationSubmissionInboundPortURI, AdmissionControllerManagementInboundPortURI, "controller");
		this.acmop = new AdmissionControllerManagementOutboundPort("acmop", new AbstractComponent(0, 0) {});
		this.acmop.publishPort();
		this.acmop.doConnection(AdmissionControllerManagementInboundPortURI, AdmissionControllerManagementConnector.class.getCanonicalName());
		
		
		int numberOfProcessors = 3;
		int numberOfCores = 10;
        Set<Integer> admissibleFrequencies = new HashSet<Integer>();
        admissibleFrequencies.add(1500); // Cores can run at 1,5 GHz
        admissibleFrequencies.add(3000); // and at 3 GHz
        Map<Integer, Integer> processingPower = new HashMap<Integer, Integer>();
        processingPower.put(1500, 1500000); // 1,5 GHz executes 1,5 Mips
        processingPower.put(3000, 3000000); // 3 GHz executes 3 Mips

        String csop[] = new String[NB_COMPUTER], csip[] = new String[NB_COMPUTER], cssdip[] = new String[NB_COMPUTER], computer[] = new String[NB_COMPUTER], cdsdip[] = new String[NB_COMPUTER];
        for (int i = 0; i < NB_COMPUTER; ++i) {
        	csop[i] = "csop"+i;
            csip[i] = "csip"+i;
            computer[i] = "computer"+i;
            cssdip[i] = "cssdip"+i;
            cdsdip[i] = "cdsdip"+i;
            System.out.println("Creating computer " + i + "with " +numberOfProcessors + "proc of "+ numberOfCores + " cores");
            Computer c = new Computer(computer[i], admissibleFrequencies, processingPower, 1500, 1500,
                    numberOfProcessors, numberOfCores, csip[i], cssdip[i], cdsdip[i]);
            this.addDeployedComponent(c); 
            this.acmop.linkComputer(computer[i], csip[i], cssdip[i], cdsdip[i]);
        }
	}
	@Override
	public void instantiateAndPublish() throws Exception {
		if (thisJVMURI.equals(AdmissionController)) {
			createAdmissionController();
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
