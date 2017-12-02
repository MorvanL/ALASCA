package fr.upmc.admissionController.tests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.upmc.admissionController.AdmissionController;
import fr.upmc.admissionController.connectors.AdmissionControllerManagementConnector;
import fr.upmc.admissionController.connectors.AdmissionControllerServicesConnector;
import fr.upmc.admissionController.ports.AdmissionControllerManagementOutboundPort;
import fr.upmc.admissionController.ports.AdmissionControllerServicesOutboundPort;
import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.datacenter.hardware.computers.Computer;
import fr.upmc.datacenter.hardware.processors.Processor;
import fr.upmc.datacenter.software.connectors.RequestSubmissionConnector;
import fr.upmc.datacenterclient.requestgenerator.RequestGenerator;
import fr.upmc.datacenterclient.requestgenerator.connectors.RequestGeneratorManagementConnector;
import fr.upmc.datacenterclient.requestgenerator.ports.RequestGeneratorManagementOutboundPort;

/**
 * The class <code>TestAdmissionControllerMonoJVM</code> deploys a test application for
 * Admission Controller in a single JVM (no remote execution provided) for a data
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
 * This test creates two computer component with two processors, each having
 * two cores. It then creates an Admission Controller and n RGs.
 * The test scenario involves to make a request to the AC for every RG 
 * to allow the execution of these RGs. If the AC has the necessary resources,
 * he create RD, VMs, and allows cores for the RG's application. if the AC hasn't
 * necessary resources, then he refused the execution of the RG's application.
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
 * <p>Created on : 18 novembre 2017</p>
 * 
 * @author	<a href="mailto:morvanlassauzay@gmail.com">Morvan Lassauzay</a>
 * @version	$Name$ -- $Revision$ -- $Date$
 */

public class TestAdmissionControllerMonoJVM
extends		AbstractCVM
{
	// ------------------------------------------------------------------------
	// Constants and instance variables
	// ------------------------------------------------------------------------
	
	// for Computers 
	/** variables for test																		*/
	public static final int		NUMBER_OF_COMPUTERS = 2 ;
	public static final int		NUMBER_OF_PROCESSORS_PER_COMPUTER = 2 ;
	public static final int		NUMBER_OF_CORES_PER_PROCESSOR = 2 ;
	/** Predefined prefix URIs.																	*/
	public static final String 	ComputerURIPrefix = "computer-";
	public static final String	ComputerServicesInboundPortURIPrefix = "cs-ibp-" ;
	public static final String	ComputerStaticStateDataInboundPortURIPrefix = "css-dip-" ;
	public static final String	ComputerStaticStateDataOutboundPortURIPrefix = "css-dop-" ;
	public static final String	ComputerDynamicStateDataInboundPortURIPrefix = "cds-dip-" ;
	public static final String	ComputerDynamicStateDataOutboundPortURIPrefix = "cds-dop" ;
	/** variables for cores																		*/
	protected int							defautFrequency ;
	protected int							maxFrequencyGap ;
	protected Set<Integer>					admissibleFrequencies ;
	protected Map<Integer,Integer>			processingPower ;
	protected Computer[]					computers ;
		
	// for Request Generators 
	/** Number of RGs for test																	*/
	public static final int     NUMBER_OF_APPLICATIONS = 3;
	/** Predefined prefix URIs.																	*/
	public static final String 	RgURIPrefix = "rg-";
	public static final String	RgManagementInboundPortURIPrefix = "rgm-ibp" ;
	public static final String	RgManagementOutboundPortURIPrefix = "rgm-obp" ;
	public static final String	RgRequestSubmissionOutboundPortURIPrefix = "rg-rsobp" ;
	public static final String	RgRequestNotificationInboundPortURIPrefix = "rg-rnibp" ;
	/** list of RGs's ports used to manage them.												*/
	protected List<RequestGeneratorManagementOutboundPort>		rgmPorts 
						= new ArrayList<RequestGeneratorManagementOutboundPort>();	
	/** list of RGs																				*/
	protected List<RequestGenerator> rgs = new ArrayList<RequestGenerator>();
	/** number of RGs created																	*/
	int nbRgs = 0;
	
	// Admission controller
	/** Predefined prefix URIs.																	*/
	public static final String	ACServicesInboundPortURI = "acsip" ;
	public static final String	ACServicesOutboundPortURI = "acsop" ;
	public static final String	ACManagementInboundPortURI = "acmip" ;
	public static final String	ACManagementOutboundPortURI = "acmop" ;
	public static final String	ACCoordinationInboundPortURI = "accip" ;
	
	/** Port connected to the admission controller component to do admission request.			*/
	protected AdmissionControllerServicesOutboundPort	acsop ;
	/** Port connected to the admission controller component manage him.			*/
	protected AdmissionControllerManagementOutboundPort	acmop ;
	
	// ------------------------------------------------------------------------
	// Component virtual machine constructors
	// ------------------------------------------------------------------------
	
	public				TestAdmissionControllerMonoJVM()
	throws Exception
	{
		super();
	}
	
	// ------------------------------------------------------------------------
	// Component virtual machine methods
	// ------------------------------------------------------------------------
	
	@Override
	public void			deploy() throws Exception
	{
		AbstractComponent.configureLogging("", "", 0, '|') ;
		Processor.DEBUG = true ;
		
		// --------------------------------------------------------------------
		// Create an admission controller component
		// --------------------------------------------------------------------
		AdmissionController ac = 
				new AdmissionController("ac",	// admission controller component URI,
									  ACServicesInboundPortURI,
									  ACManagementInboundPortURI,
									  ACCoordinationInboundPortURI);
		this.addDeployedComponent(ac) ;
		
		this.acsop = new AdmissionControllerServicesOutboundPort(
										ACServicesOutboundPortURI,
										new AbstractComponent(0, 0) {}) ;
		this.acsop.publishPort() ;
		this.acsop.
				doConnection(
					ACServicesInboundPortURI,
					AdmissionControllerServicesConnector.class.getCanonicalName()) ;
		
		this.acmop = new AdmissionControllerManagementOutboundPort(
				ACManagementOutboundPortURI,
				new AbstractComponent(0, 0) {}) ;
		this.acmop.publishPort() ;
		this.acmop.
				doConnection(
						ACManagementInboundPortURI,
						AdmissionControllerManagementConnector.class.getCanonicalName()) ;
		
		// Toggle on tracing and logging in the application virtual machine to
		// follow the execution of individual requests.
		ac.toggleTracing() ;
		ac.toggleLogging() ;
		// --------------------------------------------------------------------
		
		// Computer parameters
		Set<Integer> admissibleFrequencies = new HashSet<Integer>() ;
		admissibleFrequencies.add(1500) ;	// Cores can run at 1,5 GHz
		admissibleFrequencies.add(3000) ;	// and at 3 GHz
		Map<Integer,Integer> processingPower = new HashMap<Integer,Integer>() ;
		processingPower.put(1500, 1500000) ;	// 1,5 GHz executes 1,5 Mips
		processingPower.put(3000, 3000000) ;
		defautFrequency = 1500;
		maxFrequencyGap = 1500;

		this.computers = new Computer[NUMBER_OF_COMPUTERS] ;
		
		for(int c = 0 ; c < NUMBER_OF_COMPUTERS ; c++) {
			// ----------------------------------------------------------------
			// Create and deploy a computer component with its processors.
			// ----------------------------------------------------------------
			this.computers[c] = new Computer(
					ComputerURIPrefix + c,
					admissibleFrequencies,
					processingPower,  
					defautFrequency,
					maxFrequencyGap,		// max frequency gap within a processor
					NUMBER_OF_PROCESSORS_PER_COMPUTER,
					NUMBER_OF_CORES_PER_PROCESSOR,
					ComputerServicesInboundPortURIPrefix + c,
					ComputerStaticStateDataInboundPortURIPrefix + c,
					ComputerDynamicStateDataInboundPortURIPrefix + c) ;
			
			this.addDeployedComponent(computers[c]) ;
			
			acmop.connectComputer( ComputerURIPrefix + c,
								ComputerServicesInboundPortURIPrefix + c, 
								ComputerStaticStateDataInboundPortURIPrefix + c, 
								ComputerDynamicStateDataInboundPortURIPrefix + c);
		}
		
		// complete the deployment at the component virtual machine level.
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
	 * @see fr.upmc.components.AbstractComponent#shutdown()
	 */
	@Override
	public void			shutdown() throws Exception
	{
		this.acsop.doDisconnection();
		this.acmop.doDisconnection();
		
		for(int i = 0 ; i < this.rgs.size() ; i++){
			if (this.rgmPorts.get(i).connected()) {
				this.rgmPorts.get(i).doDisconnection() ;
			}
		}
	
		super.shutdown();
	}
	
	private void createRequestGenerator() throws Exception {
		// --------------------------------------------------------------------
		// Creating the request generator component.
		// --------------------------------------------------------------------
		RequestGenerator rg =
			new RequestGenerator(
					RgURIPrefix + this.nbRgs,			// generator component URI
					500.0,			// mean time between two requests
					6000000000L,	// mean number of instructions in requests
					RgManagementInboundPortURIPrefix + this.nbRgs,
					RgRequestSubmissionOutboundPortURIPrefix + this.nbRgs,
					RgRequestNotificationInboundPortURIPrefix + this.nbRgs) ;
		this.rgs.add(rg);
		this.addDeployedComponent(rg) ;
		
		// Create a mock up port to manage to request generator component
		// (starting and stopping the generation).
		RequestGeneratorManagementOutboundPort rgmPort = 
				new RequestGeneratorManagementOutboundPort(
						RgManagementOutboundPortURIPrefix + this.nbRgs,
						new AbstractComponent(0, 0) {}) ;
		this.rgmPorts.add(rgmPort) ;
		rgmPort.publishPort() ;
		rgmPort.doConnection(
				RgManagementInboundPortURIPrefix + this.nbRgs,
				RequestGeneratorManagementConnector.class.getCanonicalName()) ;

		// Toggle on tracing and logging in the request generator to
		// follow the submission and end of execution notification of
		// individual requests.
		rg.toggleTracing() ;
		rg.toggleLogging() ;
		
		this.nbRgs++;
	}
	
	/**
	 * Create RGs, make admission requests and start generation of requests.
	 *
	 * @throws Exception
	 */
	public void			testScenario() throws Exception
	{
		for (int i = 0 ; i < NUMBER_OF_APPLICATIONS ; i++) {
			createRequestGenerator();
			
			// submission of the application to the data center
			String requestSubmissionInboundPortUri = 
				this.acsop.submitApplication(
					RgRequestNotificationInboundPortURIPrefix + i);
			// test if the data center accept the submission of the application
			if (requestSubmissionInboundPortUri != null) 
			{	
				this.rgs.get(i).doPortConnection(
						RgRequestSubmissionOutboundPortURIPrefix + i,
						requestSubmissionInboundPortUri,
						RequestSubmissionConnector.class.getCanonicalName()) ;
				// start the request generation in the request generator.
				this.rgs.get(i).startGeneration() ;
			}
			
			Thread.sleep(3000L) ;
		}
		
		for (int i = 0 ; i < NUMBER_OF_APPLICATIONS ; i++){
			this.rgs.get(i).stopGeneration();
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
			final TestAdmissionControllerMonoJVM trcm = new TestAdmissionControllerMonoJVM() ;
			// Deploy the components
			trcm.deploy() ;
			System.out.println("starting...") ;
			// Start them.
			trcm.start() ;
			Thread.sleep(1000);
			
			// Execute the chosen request generation test scenario in a
			// separate thread.
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						trcm.testScenario() ;
					} catch (Exception e) {
						throw new RuntimeException(e) ;
					}
				}
			}).start() ;
			// Sleep to let the test scenario execute to completion.
			Thread.sleep(45000L) ;
			// Shut down the application.
			System.out.println("shutting down...") ;
			trcm.shutdown() ;
			System.out.println("ending...") ;
			// Exit from Java.
			System.exit(0) ;
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}
}

