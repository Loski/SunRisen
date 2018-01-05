package fr.upmc.PriseTheSun.tests;

import java.util.ArrayList;

//Copyright Jacques Malenfant, Univ. Pierre et Marie Curie.
//
//Jacques.Malenfant@lip6.fr
//
//This software is a computer program whose purpose is to provide a
//basic component programming model to program with components
//distributed applications in the Java programming language.
//
//This software is governed by the CeCILL-C license under French law and
//abiding by the rules of distribution of free software.  You can use,
//modify and/ or redistribute the software under the terms of the
//CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
//URL "http://www.cecill.info".
//
//As a counterpart to the access to the source code and  rights to copy,
//modify and redistribute granted by the license, users are provided only
//with a limited warranty  and the software's author,  the holder of the
//economic rights,  and the successive licensors  have only  limited
//liability. 
//
//In this respect, the user's attention is drawn to the risks associated
//with loading,  using,  modifying and/or developing or reproducing the
//software by the user in light of its specific status of free software,
//that may mean  that it is complicated to manipulate,  and  that  also
//therefore means  that it is reserved for developers  and  experienced
//professionals having in-depth computer knowledge. Users are therefore
//encouraged to load and test the software's suitability as regards their
//requirements in conditions enabling the security of their systems and/or 
//data to be ensured and,  more generally, to use and operate it in the 
//same conditions as regards security. 
//
//The fact that you are presently reading this means that you have had
//knowledge of the CeCILL-C license and that you accept its terms.

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.components.ports.PortI;
import fr.upmc.datacenter.hardware.computers.Computer;
import fr.upmc.datacenter.hardware.computers.ports.ComputerServicesOutboundPort;
import fr.upmc.datacenter.hardware.processors.Processor;
import fr.upmc.datacenter.hardware.processors.Processor.ProcessorPortTypes;
import fr.upmc.datacenter.hardware.tests.ComputerMonitor;
import fr.upmc.datacenter.software.applicationvm.ApplicationVM;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;


/**
 * The class <code>TestRequestGenerator</code> deploys a test application for
 * request generation in a single JVM (no remote execution provided) for a data
 * center simulation.
 *
 * <p><strong>Description</strong></p>
 * 
 * A data center has a set of computers, each with several multi-core
 * processors. Application virtual machines (AVM) are created to run
 * requests of an application. Each AVM is allocated cores of different
 * processors of a computer. AVM then receive requests for their application.
 * See the data center simulator documentation for more details about the
 * implementation of this simulation.
 *  
 * This test creates one computer component with two processors, each having
 * two cores. It then creates an AVM and allocates it all four cores of the
 * two processors of this unique computer. A request generator component is
 * then created and linked to the application virtual machine.  The test
 * scenario starts the request generation, wait for a specified time and then
 * stops the generation. The overall test allots sufficient time to the
 * execution of the application so that it completes the execution of all the
 * generated requests.
 * 
 * The waiting time in the scenario and in the main method must be manually
 * set by the tester.
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	true
 * </pre>
 * 
 * <p>Created on : May 5, 2015</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 * @version	$Name$ -- $Revision$ -- $Date$
 */
public class				TestAdmissionController
extends		AbstractCVM
{
	// ------------------------------------------------------------------------
	// Constants and instance variables
	// ------------------------------------------------------------------------

	public TestAdmissionController() throws Exception {
		super();
	}

	// Predefined URI of the different ports visible at the component assembly
	// level.
	public static final String	ComputerServicesInboundPortURI = "cs-ibp" ;
	public static final String	ComputerServicesOutboundPortURI = "cs-obp" ;
	public static final String	ComputerStaticStateDataInboundPortURI = "css-dip" ;
	public static final String	ComputerStaticStateDataOutboundPortURI = "css-dop" ;
	public static final String	ComputerDynamicStateDataInboundPortURI = "cds-dip" ;
	public static final String	ComputerDynamicStateDataOutboundPortURI = "cds-dop" ;
	public static final String	ApplicationVMManagementInboundPortURI = "avm-ibp" ;
	public static final String	ApplicationVMManagementOutboundPortURI = "avm-obp" ;
	public static final String	RequestDispatcherManagementInboundPortURI = "rdm-ibp" ;
	public static final String	RequestDispatcherManagementOutboundPortURI = "rdm-obp" ;
	public static final String	RequestSubmissionInboundPortURI = "rsibp" ;
	public static final String	RequestSubmissionOutboundPortURI = "rsobp" ;
	public static final String	RequestSubmissionOutboundPortDispatcherURI = "rsobp-dispatcher" ;
	public static final String	RequestNotificationInboundPortURI = "rnibp" ;
	public static final String  RequestNotificationInboundPortDispatcherURI = "rnibp-dispatcher";
	public static final String	RequestNotificationOutboundPortURI = "rnobp" ;
	public static final String	RequestSubmissionInboundPortVMURI = "rsibpVM" ;
	public static final String	RequestNotificationOutboundPortVMURI = "rnobpVM" ;
	public static final String	RequestGeneratorManagementInboundPortURI = "rgmip" ;
	public static final String	RequestGeneratorManagementOutboundPortURI = "rgmop" ;
	public static final int NB_COMPUTER = 5;
	private static final int NB_APPLICATION = 3;
	/** Port connected to the computer component to access its services.	*/
	protected ComputerServicesOutboundPort			csPort ;
	/** 	Computer monitor component.										*/
	protected ComputerMonitor						cm ;
	/** 	Application virtual machine component.							*/
	protected ApplicationVM							vm ;

	/** Port connected to the AVM component to allocate it cores.			*/
	protected ApplicationVMManagementOutboundPort	avmPort ;


	protected AdmissionControllerDynamic ac;
	protected AdmissionControllerManagementOutboundPort acmop;
	protected ApplicationProvider ap[];
	public ApplicationProviderManagementOutboundPort apmop[];

	private String applicationSubmissionInboundPortURI = "asip";
	private String AdmissionControllerManagementInboundPortURI = "acmip";

	private String applicationSubmissionOutboundPortURI = "asop";
	private String applicationManagementInboundPort = " amip";

	// ------------------------------------------------------------------------
	// Component virtual machine methods
	// ------------------------------------------------------------------------

	private void createAdmissionController() throws Exception {
		
		
		this.ac = new AdmissionControllerDynamic("AdmController", applicationSubmissionInboundPortURI, AdmissionControllerManagementInboundPortURI, "","","");
		this.acmop = new AdmissionControllerManagementOutboundPort("acmop", new AbstractComponent(0, 0) {});
		this.acmop.publishPort();
		this.acmop.doConnection(AdmissionControllerManagementInboundPortURI, AdmissionControllerManagementConnector.class.getCanonicalName());
		
		
		int numberOfProcessors = 2;
		int numberOfCores = 3;
        Set<Integer> admissibleFrequencies = new HashSet<Integer>();
        admissibleFrequencies.add(1500); // Cores can run at 1,5 GHz
        admissibleFrequencies.add(3000); // and at 3 GHz
        Map<Integer, Integer> processingPower = new HashMap<Integer, Integer>();
        processingPower.put(1500, 1500000); // 1,5 GHz executes 1,5 Mips
        processingPower.put(3000, 3000000); // 3 GHz executes 3 Mips

        // map associate processor uri with uri of inbound port
        ArrayList<String> pmipURIs = new ArrayList<String>();
        ArrayList<String> pssdURIs = new ArrayList<String>();

        Map<String, String> processorCoordinators = new HashMap<>();
        
        
        String csop[] = new String[NB_COMPUTER], csip[] = new String[NB_COMPUTER], cssdip[] = new String[NB_COMPUTER], computer[] = new String[NB_COMPUTER], cdsdip[] = new String[NB_COMPUTER];
        for (int i = 0; i < NB_COMPUTER; ++i) {
        	csop[i] = "csop"+i;
            csip[i] = "csip"+i;
            computer[i] = "computer"+i;
            cssdip[i] = "cssdip"+i;
            cdsdip[i] = "cdsdip"+i;
            
            Computer c = new Computer(computer[i], admissibleFrequencies, processingPower, 1500, 1500,
                    numberOfProcessors, numberOfCores, csip[i], cssdip[i], cdsdip[i]);
            this.addDeployedComponent(c);
            
            Map<Integer, String> processorURIs = c.getStaticState().getProcessorURIs();
            
            for (Map.Entry<Integer, String> entry : processorURIs.entrySet()) {
                Map<ProcessorPortTypes, String> pPortsList = c.getStaticState().getProcessorPortMap()
                        .get(entry.getValue());
                pmipURIs.add(pPortsList.get(Processor.ProcessorPortTypes.MANAGEMENT));
                pssdURIs.add(pPortsList.get(Processor.ProcessorPortTypes.STATIC_STATE));
            }
            System.out.println(pmipURIs);
            
            this.acmop.linkComputer(computer[i], csip[i], cssdip[i], cdsdip[i], pmipURIs, pssdURIs);
            

            // --------------------------------------------------------------------
            // Create and deploy Processors coordinator
            // --------------------------------------------------------------------
          
          /*  int j = 0;
            for ( Map.Entry<Integer , String> entry : processorURIs.entrySet() ) {
                ProcessorCoordinator pc = new ProcessorCoordinator("pc" + i , pmipURIs.get(entry.getValue()),1500, 1500, numberOfCores );
                processorCoordinators.put(entry.getValue(), "pc" + j);
                this.addDeployedComponent(pc);
                pc.toggleLogging();
                pc.toggleTracing();
            }
            */
        }
        
		
	}
	
	private void createApplication(int nbApplication) throws Exception {
		this.ap = new ApplicationProvider[nbApplication];
		this.apmop = new ApplicationProviderManagementOutboundPort[nbApplication];
		for(int i =0; i < nbApplication; i++) {
			this.ap[i] = new ApplicationProvider("App"+"-"+i, applicationSubmissionInboundPortURI, applicationSubmissionOutboundPortURI+"-"+i, applicationManagementInboundPort+"-"+i);
			this.apmop[i] = new ApplicationProviderManagementOutboundPort("apmop"+"-"+i, new AbstractComponent(0, 0) {});
			this.apmop[i].publishPort();
			this.apmop[i].doConnection(applicationManagementInboundPort+"-"+i, ApplicationProviderManagementConnector.class.getCanonicalName());
		}
	}
	
	@Override
	public void			deploy() throws Exception
	{
		AbstractComponent.configureLogging("", "", 0, '|') ;
		Processor.DEBUG = true ;

		createAdmissionController();
		createApplication(NB_APPLICATION);
		super.deploy();
	}

	/**
	 * @see fr.upmc.components.cvm.AbstractCVM#start()
	 */
	@Override
	public void			start() throws Exception
	{
		super.start() ;
	}

	/**
	 * @see fr.upmc.components.cvm.AbstractCVM#shutdown()
	 */
	@Override
	public void			shutdown() throws Exception
	{
		// disconnect all ports explicitly connected in the deploy phase.
		this.csPort.doDisconnection() ;
		this.avmPort.doDisconnection() ;
		super.shutdown() ;
	}

	// ------------------------------------------------------------------------
	// Test scenarios and main execution.
	// ------------------------------------------------------------------------

	/**
	 * generate requests for 20 seconds and then stop generating.
	 *
	 * @throws Exception
	 */
	public void			testScenario() throws Exception
	{
		for(int i = 0; i < this.apmop.length;i++) {
				this.apmop[i].createAndSendApplication();
		}
	}

	/**
	 * execute the test application.
	 * 
	 * @param args	command line arguments, disregarded here.
	 */
	public static void	main(String[] args)
	{
		// Uncomment next line to execute components in debug mode.
		// AbstractCVM.toggleDebugMode() ;
		try {
			final TestAdmissionController trd = new TestAdmissionController() ;
			// Deploy the components
			trd.deploy() ;
			System.out.println("starting...") ;
			// Start them.
			trd.start() ;
			// Execute the chosen request generation test scenario in a
			// separate thread.
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
}
