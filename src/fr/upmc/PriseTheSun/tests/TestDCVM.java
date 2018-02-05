package fr.upmc.PriseTheSun.tests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.AdmissionControllerDynamic;
import fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.connector.AdmissionControllerManagementConnector;
import fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.ports.AdmissionControllerManagementOutboundPort;
import fr.upmc.PriseTheSun.datacenterclient.software.applicationprovider.ApplicationProvider;
import fr.upmc.PriseTheSun.datacenterclient.software.applicationprovider.ports.ApplicationProviderManagementOutboundPort;
import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.components.cvm.AbstractDistributedCVM;
import fr.upmc.datacenter.hardware.computers.Computer;


/**
 * Cas de test dans un cas DCVM
 * @author Maxime Lavaste Loïc Lafontaine
 *
 */
public class TestDCVM extends AbstractDistributedCVM{
	
	/**
	 * Wrapper pour enregistrer un ordinateur pour respecter le start().
	 */
	public class ComputerWrapper {
		String uri;
		String csip;
		String cssdip;
		String cdsdip;
		
		/**
		 * @param uri
		 * @param csip
		 * @param cssdip
		 * @param cdsdip
		 */
		public ComputerWrapper(String uri, String csip, String cssdip, String cdsdip) {
			super();
			this.uri = uri;
			this.csip = csip;
			this.cssdip = cssdip;
			this.cdsdip = cdsdip;
		}

	}
	
	protected static final String		AdmissionController = "controller" ;
	protected static final String		Application1 = "application1" ;
	protected static final String		Application2 = "application2" ;
	
	public static final int NB_COMPUTER = 50;
	private static final int NB_APPLICATION = 6;

	protected AdmissionControllerDynamic ac;
	protected AdmissionControllerManagementOutboundPort acmop;
	protected ApplicationProvider ap[];
	public ApplicationProviderManagementOutboundPort apmop[];
	public ArrayList<ComputerWrapper> cw = new ArrayList<>();
	public static final  String applicationSubmissionInboundPortURI = "asip";
	public static final String AdmissionControllerManagementInboundPortURI = "acmip";
	public static final String applicationSubmissionOutboundPortURI = "asop";
	public static final String applicationManagementInboundPort = " amip";
	
	private void createAdmissionController() throws Exception {

		this.ac = new AdmissionControllerDynamic("AdmController", applicationSubmissionInboundPortURI, AdmissionControllerManagementInboundPortURI,AdmissionController);
        this.addDeployedComponent(this.ac); 
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
        	
        	numberOfProcessors = ThreadLocalRandom.current().nextInt(1, 4 + 1);
        	numberOfCores = ThreadLocalRandom.current().nextInt(5, 15 + 1);

        	csop[i] = "csop"+i;
            csip[i] = "csip"+i;
            computer[i] = "computer"+i;
            cssdip[i] = "cssdip"+i;
            cdsdip[i] = "cdsdip"+i;
            System.out.println("Creating computer " + i + "with " +numberOfProcessors + "proc of "+ numberOfCores + " cores");
            Computer c = new Computer(computer[i], admissibleFrequencies, processingPower, 1500, 1500,
                    numberOfProcessors, numberOfCores, csip[i], cssdip[i], cdsdip[i]);
            this.addDeployedComponent(c);
            cw.add(new ComputerWrapper(computer[i], csip[i], cssdip[i], cdsdip[i]));
        }
	}
	
	/**
	 * Créer nbApplication application dans un modèle CVM
	 * @param nbApplication
	 * @throws Exception
	 */
	private void createApplication(int nbApplication) throws Exception {
		this.ap = new ApplicationProvider[nbApplication];
		this.apmop = new ApplicationProviderManagementOutboundPort[nbApplication];
		for(int i =0; i < 1; i++) {
			this.ap[i] = new ApplicationProvider("App"+"-"+i, applicationSubmissionInboundPortURI, applicationSubmissionOutboundPortURI+"-"+i, applicationManagementInboundPort+"-"+i);
			this.addDeployedComponent(this.ap[i]);
		}
	}
	
	
	@Override
	public void instantiateAndPublish() throws Exception {
		super.instantiateAndPublish();
	}

	@Override
	public void start() throws Exception {
		super.start();
		
		if (thisJVMURI.equals(AdmissionController)) {
			for(ComputerWrapper c : cw) {
				this.acmop.linkComputer(c.uri, c.csip, c.cssdip, c.cdsdip);
			}
		}
		else if(thisJVMURI.equals(Application1)) {}		
		else if(thisJVMURI.equals(Application2)) {}
	}

	@Override
	public void deploy() throws Exception {
		
		if (thisJVMURI.equals(AdmissionController)) {
			createAdmissionController();
		}
		else if(thisJVMURI.equals(Application1)) {
			createApplication(NB_APPLICATION/2);			
		}else if(thisJVMURI.equals(Application2)) {
			createApplication(NB_APPLICATION/2);
		}
		
		super.deploy();
	}

	@Override
	public void initialise() throws Exception {
		super.initialise();
	}

	public TestDCVM(String[] args) throws Exception {
		super(args);
	}
	public static void	main(String[] args)
	{
		// Uncomment next line to execute components in debug mode.
		AbstractCVM.toggleDebugMode() ;
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
			Thread.sleep(900000L) ;
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
			
		}else if(thisJVMURI.equals(Application2)) {
			
		}
	}
}
