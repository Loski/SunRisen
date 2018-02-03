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
import java.util.Map;
import java.util.Set;

import fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.AdmissionControllerDynamic;
import fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.connector.AdmissionControllerManagementConnector;
import fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.ports.AdmissionControllerManagementOutboundPort;
import fr.upmc.PriseTheSun.datacenterclient.software.applicationprovider.ApplicationProvider;
import fr.upmc.PriseTheSun.datacenterclient.software.applicationprovider.connectors.ApplicationProviderManagementConnector;
import fr.upmc.PriseTheSun.datacenterclient.software.applicationprovider.ports.ApplicationProviderManagementOutboundPort;
import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.datacenter.hardware.computers.Computer;
import fr.upmc.datacenter.hardware.processors.Processor;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionI;


public class				TestAdmissionController
extends		AbstractCVM
{
	// ------------------------------------------------------------------------
	// Constants and instance variables
	// ------------------------------------------------------------------------

	public TestAdmissionController() throws Exception {
		super();
	}

	
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
	public static final int NB_COMPUTER = 50;
	private static final int NB_APPLICATION = 5;

	protected AdmissionControllerDynamic ac;
	protected AdmissionControllerManagementOutboundPort acmop;
	protected ApplicationProvider ap[];
	public ApplicationProviderManagementOutboundPort apmop[];
	public ArrayList<ComputerWrapper> cw = new ArrayList<>();
	public static final  String applicationSubmissionInboundPortURI = "asip";
	public static final String AdmissionControllerManagementInboundPortURI = "acmip";
	public static final String applicationSubmissionOutboundPortURI = "asop";
	public static final String applicationManagementInboundPort = " amip";

	// ------------------------------------------------------------------------
	// Component virtual machine methods
	// ------------------------------------------------------------------------
	
	private void createAdmissionController() throws Exception {

		this.ac = new AdmissionControllerDynamic("AdmController", applicationSubmissionInboundPortURI, AdmissionControllerManagementInboundPortURI, "");
        this.ac.start();
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
        	csop[i] = "csop"+i;
            csip[i] = "csip"+i;
            computer[i] = "computer"+i;
            cssdip[i] = "cssdip"+i;
            cdsdip[i] = "cdsdip"+i;
            System.out.println("Creating computer " + i + "with " +numberOfProcessors + "proc of "+ numberOfCores + " cores");
            Computer c = new Computer(computer[i], admissibleFrequencies, processingPower, 1500, 1500,
                    numberOfProcessors, numberOfCores, csip[i], cssdip[i], cdsdip[i]);
            this.addDeployedComponent(c);
            c.start();

            cw.add(new ComputerWrapper(computer[i], csip[i], cssdip[i], cdsdip[i]));
            this.acmop.linkComputer(computer[i], csip[i], cssdip[i], cdsdip[i]);
        }
	}
	
	
	private void createApplication(int nbApplication) throws Exception {
		this.ap = new ApplicationProvider[nbApplication];
		this.apmop = new ApplicationProviderManagementOutboundPort[nbApplication];
		for(int i =0; i < nbApplication; i++) {
			this.ap[i] = new ApplicationProvider("App"+"-"+i, applicationSubmissionInboundPortURI, applicationSubmissionOutboundPortURI+"-"+i, applicationManagementInboundPort+"-"+i);
			this.addDeployedComponent(this.ap[i]);
		
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
		for(ComputerWrapper c : cw) {
			this.acmop.linkComputer(c.uri, c.csip, c.cssdip, c.cdsdip);
		}
		for(int i = 0; i < NB_APPLICATION; i++) {
			this.ap[i].start();
			this.apmop[i] = new ApplicationProviderManagementOutboundPort("apmop"+"-"+i, new AbstractComponent(0, 0) {});
			this.apmop[i].publishPort();
			this.apmop[i].doConnection(applicationManagementInboundPort+"-"+i, ApplicationProviderManagementConnector.class.getCanonicalName());
		}
	}

	/**
	 * @see fr.upmc.components.cvm.AbstractCVM#shutdown()
	 */
	@Override
	public void			shutdown() throws Exception
	{
		// disconnect all ports explicitly connected in the deploy phase.
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
			Thread.sleep(500);
			//this.apmop[i].createAndSendApplication();
			this.apmop[i].createAndSendApplication();
		}
		
		//this.apmop[4].stopApplication();
	}

	
	
	/**
	 * execute the test application.
	 * 
	 * @param args	command line arguments, disregarded here.
	 */
	public static void	main(String[] args)
	{
		// Uncomment next line to execute components in debug mode.
		//AbstractCVM.toggleDebugMode() ;
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
			Thread.sleep(400000L) ;
			// Shut down the application.
			System.out.println("shutting down...") ;
		//	trd.shutdown() ;
			System.out.println("ending...") ;
			// Exit from Java.
			System.exit(0) ;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e) ;
		}
	}
}
