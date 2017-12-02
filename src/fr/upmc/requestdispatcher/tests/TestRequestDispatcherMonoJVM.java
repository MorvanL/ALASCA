package fr.upmc.requestdispatcher.tests;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.datacenter.hardware.computers.Computer;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;
import fr.upmc.datacenter.hardware.computers.connectors.ComputerServicesConnector;
import fr.upmc.datacenter.hardware.computers.ports.ComputerServicesOutboundPort;
import fr.upmc.datacenter.hardware.processors.Processor;
import fr.upmc.datacenter.software.applicationvm.ApplicationVM;
import fr.upmc.datacenter.software.applicationvm.connectors.ApplicationVMManagementConnector;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.datacenter.software.connectors.RequestNotificationConnector;
import fr.upmc.datacenter.software.connectors.RequestSubmissionConnector;
import fr.upmc.datacenterclient.requestgenerator.RequestGenerator;
import fr.upmc.datacenterclient.requestgenerator.connectors.RequestGeneratorManagementConnector;
import fr.upmc.datacenterclient.requestgenerator.ports.RequestGeneratorManagementOutboundPort;
import fr.upmc.requestdispatcher.RequestDispatcher;
import fr.upmc.requestdispatcher.connectors.RequestDispatcherManagementConnector;
import fr.upmc.requestdispatcher.ports.RequestDispatcherManagementOutboundPort;


/**
 * The class <code>TestRequestDispatcherMonoJVM</code> deploys a test application for
 * request dispatcher in a single JVM (no remote execution provided) for a data
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
 * two cores. It then creates to AVMs and allocates them all four cores of the
 * two processors of this unique computer. A request dispatcher component is
 * then created and linked to the application virtual machine and a request 
 * generator component is also created and linked to the request dispatcher.
 * The test scenario starts the request generation, wait for a specified time and then
 * stops the generation. The overall test allots sufficient time to the
 * execution of the application so that it completes the execution of all the
 * generated requests.
 * 
 * The goal of this test is to check the correct working of the request dispatcher.
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
public class			TestRequestDispatcherMonoJVM
extends		AbstractCVM
{

	// ------------------------------------------------------------------------
	// Constants and instance variables
	// ------------------------------------------------------------------------

	// Predefined URI of the different ports visible at the component assembly
	// level.
	public static final String	ComputerServicesInboundPortURI = "cs-ibp" ;
	public static final String	ComputerServicesOutboundPortURI = "cs-obp" ;
	public static final String	ComputerStaticStateDataInboundPortURI = "css-dip" ;
	public static final String	ComputerDynamicStateDataInboundPortURI = "cds-dip" ;
	public static final String	ApplicationVMManagementInboundPortURI_1 = "avm1-ibp" ;
	public static final String	ApplicationVMManagementOutboundPortURI_1 = "avm1-obp" ;
	public static final String	VmRequestSubmissionInboundPortURI_1 = "vm1-rsibp" ;
	public static final String	VmRequestNotificationOutboundPortURI_1 = "vm1-rnobp" ;
	public static final String	ApplicationVMManagementInboundPortURI_2 = "avm2-ibp" ;
	public static final String	ApplicationVMManagementOutboundPortURI_2 = "avm2-obp" ;
	public static final String	VmRequestSubmissionInboundPortURI_2 = "vm2-rsibp" ;
	public static final String	VmRequestNotificationOutboundPortURI_2 = "vm2-rnobp" ;
	public static final String	RdRequestSubmissionInboundPortURI = "rd-rsibp" ;
	public static final String	RdRequestNotificationInboundPortURI = "rd-rnibp" ;
	public static final String	RdRequestNotificationOutboundPortURI = "rd-rnobp" ;
	public static final String	RequestDispatcherManagementInboundPortURI = "rdmip" ;
	public static final String	RequestDispatcherManagementOutboundPortURI = "rdmop" ;
	public static final String  RdDynamicStateDataInboundPortURI = "rddsdip";
	public static final String	RgRequestNotificationInboundPortURI = "rg-rnibp" ;
	public static final String	RgRequestSubmissionOutboundPortURI = "rg-rsobp" ;
	public static final String	RequestGeneratorManagementInboundPortURI = "rgmip" ;
	public static final String	RequestGeneratorManagementOutboundPortURI = "rgmop" ;

	/** Port connected to the computer component to access its services.	*/
	protected ComputerServicesOutboundPort				csPort ;
	/** 	Application virtual machine component.							*/
	protected ApplicationVM							vm1 ;
	/** 	Application virtual machine component.							*/
	protected ApplicationVM							vm2 ;
	/** 	Request dispatcher component.									*/
	protected RequestDispatcher						rd ;
	/** 	Request generator component.									*/
	protected RequestGenerator						rg ;
	/** Port connected to the computer component to receive the static
	 *  state data.															*/
	protected ApplicationVMManagementOutboundPort		avmPort_1 ;
	/** Port connected to the AVM component to allocate it cores.			*/
	protected ApplicationVMManagementOutboundPort		avmPort_2 ;
	/** Port connected to the request dispatcher component to connect 
	 *  VMs with request dispatcher											*/
	protected RequestDispatcherManagementOutboundPort	rdmop ;
	/** Port connected to the request generator component to manage its
	 *  execution (starting and stopping the request generation).			*/
	protected RequestGeneratorManagementOutboundPort	rgmop ;
	
	
	
	// ------------------------------------------------------------------------
	// Component virtual machine constructors
	// ------------------------------------------------------------------------

	public				TestRequestDispatcherMonoJVM()
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
		// Create and deploy a computer component with its 2 processors and
		// each with 2 cores.
		// --------------------------------------------------------------------
		String computerURI = "computer0" ;
		int numberOfProcessors = 2 ;
		int numberOfCores = 2 ;
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

		// Create a mock-up computer services port to later allocate its cores
		// to the application virtual machine.
		this.csPort = new ComputerServicesOutboundPort(
										ComputerServicesOutboundPortURI,
										new AbstractComponent(0, 0) {}) ;
		this.csPort.publishPort() ;
		this.csPort.doConnection(
						ComputerServicesInboundPortURI,
						ComputerServicesConnector.class.getCanonicalName()) ;
		// --------------------------------------------------------------------

		// --------------------------------------------------------------------
		// Create an Application VM component
		// --------------------------------------------------------------------
		this.vm1 =
				new ApplicationVM("vm1",	// application vm component URI
								  ApplicationVMManagementInboundPortURI_1,
								  VmRequestSubmissionInboundPortURI_1,
								  VmRequestNotificationOutboundPortURI_1) ;
		this.addDeployedComponent(this.vm1) ;

		// Create a mock up port to manage the AVM component (allocate cores).
		this.avmPort_1 = new ApplicationVMManagementOutboundPort(
									ApplicationVMManagementOutboundPortURI_1,
									new AbstractComponent(0, 0) {}) ;
		this.avmPort_1.publishPort() ;
		this.avmPort_1.
				doConnection(
					ApplicationVMManagementInboundPortURI_1,
					ApplicationVMManagementConnector.class.getCanonicalName()) ;

		// Toggle on tracing and logging in the application virtual machine to
		// follow the execution of individual requests.
		this.vm1.toggleTracing() ;
		this.vm1.toggleLogging() ;
		// --------------------------------------------------------------------
		
		// --------------------------------------------------------------------
		// Create a second Application VM component
		// --------------------------------------------------------------------
		this.vm2 =
				new ApplicationVM("vm2",	// application VM component URI
								  ApplicationVMManagementInboundPortURI_2,
								  VmRequestSubmissionInboundPortURI_2,
								  VmRequestNotificationOutboundPortURI_2) ;
		this.addDeployedComponent(this.vm2) ;

		// Create a mock up port to manage the AVM component (allocate cores).
		this.avmPort_2 = new ApplicationVMManagementOutboundPort(
									ApplicationVMManagementOutboundPortURI_2,
									new AbstractComponent(0, 0) {}) ;
		this.avmPort_2.publishPort() ;
		this.avmPort_2.
				doConnection(
					ApplicationVMManagementInboundPortURI_2,
					ApplicationVMManagementConnector.class.getCanonicalName()) ;

		// Toggle on tracing and logging in the application virtual machine to
		// follow the execution of individual requests.
		this.vm2.toggleTracing() ;
		this.vm2.toggleLogging() ;
		// --------------------------------------------------------------------
		
		// --------------------------------------------------------------------
		// Create an Request Dispatcher component
		// --------------------------------------------------------------------
		this.rd = 
				new RequestDispatcher("rd",	// Request dispatcher component URI,
									  RequestDispatcherManagementInboundPortURI,
									  RdRequestSubmissionInboundPortURI,
									  RdRequestNotificationOutboundPortURI,
									  RdRequestNotificationInboundPortURI,
									  RdDynamicStateDataInboundPortURI);
		this.addDeployedComponent(this.rd) ;
		
		// Create a mock up port to manage the RD component (connect request receiver).
		this.rdmop = new RequestDispatcherManagementOutboundPort(
									RequestDispatcherManagementOutboundPortURI,
									new AbstractComponent(0, 0) {}) ;
		this.rdmop.publishPort() ;
		this.rdmop.
				doConnection(
					RequestDispatcherManagementInboundPortURI,
					RequestDispatcherManagementConnector.class.getCanonicalName()) ;
		
		// Connecting the request dispatcher to the VMs.
		this.rdmop.addRequestReceiver("vm1",VmRequestSubmissionInboundPortURI_1);
		this.vm1.doPortConnection(
				VmRequestNotificationOutboundPortURI_1,
				RdRequestNotificationInboundPortURI,
				RequestNotificationConnector.class.getCanonicalName()) ;
		
		this.rdmop.addRequestReceiver("vm2",VmRequestSubmissionInboundPortURI_2);
		this.vm2.doPortConnection(
				VmRequestNotificationOutboundPortURI_2,
				RdRequestNotificationInboundPortURI,
				RequestNotificationConnector.class.getCanonicalName()) ;
		
		// Toggle on tracing and logging in the application virtual machine to
		// follow the execution of individual requests.
		rd.toggleTracing() ;
		rd.toggleLogging() ;
		
		// --------------------------------------------------------------------

		// --------------------------------------------------------------------
		// Creating the request generator component.
		// --------------------------------------------------------------------
		this.rg =
			new RequestGenerator(
					"rg",			// generator component URI
					500.0,			// mean time between two requests
					6000000000L,	// mean number of instructions in requests
					RequestGeneratorManagementInboundPortURI,
					RgRequestSubmissionOutboundPortURI,
					RgRequestNotificationInboundPortURI) ;
		this.addDeployedComponent(this.rg) ;

		// Toggle on tracing and logging in the request generator to
		// follow the submission and end of execution notification of
		// individual requests.
		this.rg.toggleTracing() ;
		this.rg.toggleLogging() ;

		// Connecting the request generator to the request dispatcher.
		// Request generators have three different interfaces:
		// - one for submitting requests to request dispatcher,
		// - one for receiving end of execution notifications from request 
		//   dispatcher, and
		// - one for request generation management i.e., starting and stopping
		//   the generation process.
		this.rg.doPortConnection(
				RgRequestSubmissionOutboundPortURI,
				RdRequestSubmissionInboundPortURI,
				RequestSubmissionConnector.class.getCanonicalName()) ;
		
		this.rd.doPortConnection(
				RdRequestNotificationOutboundPortURI,
				RgRequestNotificationInboundPortURI,
				RequestNotificationConnector.class.getCanonicalName()) ;

		// Create a mock up port to manage to request generator component
		// (starting and stopping the generation).
		this.rgmop = new RequestGeneratorManagementOutboundPort(
							RequestGeneratorManagementOutboundPortURI,
							new AbstractComponent(0, 0) {}) ;
		this.rgmop.publishPort() ;
		this.rgmop.doConnection(
				RequestGeneratorManagementInboundPortURI,
				RequestGeneratorManagementConnector.class.getCanonicalName()) ;
		// --------------------------------------------------------------------

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

		// Allocate 2 cores of the computer to the first application virtual
		// machine.
		AllocatedCore[] ac_1 = this.csPort.allocateCores(2) ;
		this.avmPort_1.allocateCores(ac_1) ;
		
		// Allocate 2 cores of the computer to the second application virtual
		// machine.
		AllocatedCore[] ac_2 = this.csPort.allocateCores(2) ;
		this.avmPort_2.allocateCores(ac_2) ;
	}

	/**
	 * @see fr.upmc.components.cvm.AbstractCVM#shutdown()
	 */
	@Override
	public void			shutdown() throws Exception
	{
		// disconnect all ports explicitly connected in the deploy phase.
		this.csPort.doDisconnection() ;
		this.avmPort_1.doDisconnection() ;
		this.avmPort_2.doDisconnection() ;
		this.rdmop.doDisconnection();
		this.rgmop.doDisconnection() ;

		super.shutdown() ;
	}

	/**
	 * generate requests for 8 seconds and then stop generating.
	 *
	 * @throws Exception
	 */
	public void			testScenario() throws Exception
	{
		// start the request generation in the request generator.
		this.rgmop.startGeneration() ;
		// wait 8 seconds
		Thread.sleep(8000L) ;
		// then stop the generation.
		this.rgmop.stopGeneration() ;
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
			final TestRequestDispatcherMonoJVM trdm = new TestRequestDispatcherMonoJVM() ;
			// Deploy the components
			trdm.deploy() ;
			System.out.println("starting...") ;
			// Start them.
			trdm.start() ;
			// Execute the chosen request generation test scenario in a
			// separate thread.
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						trdm.testScenario() ;
					} catch (Exception e) {
						throw new RuntimeException(e) ;
					}
				}
			}).start() ;
			// Sleep to let the test scenario execute to completion.
			Thread.sleep(20000L) ;
			// Shut down the application.
			System.out.println("shutting down...") ;
			trdm.shutdown() ;
			System.out.println("ending...") ;
			// Exit from Java.
			System.exit(0) ;
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}
}
