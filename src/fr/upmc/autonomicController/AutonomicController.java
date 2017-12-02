package fr.upmc.autonomicController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import fr.upmc.admissionController.connectors.AdmissionControllerCoordinationConnector;
import fr.upmc.admissionController.ports.AdmissionControllerCoordinationOutboundPort;
import fr.upmc.autonomicController.interfaces.AutonomicControllerManagementI;
import fr.upmc.autonomicController.ports.AutonomicControllerManagementInboundPort;
import fr.upmc.components.AbstractComponent;
import fr.upmc.components.ComponentI;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.datacenter.connectors.ControlledDataConnector;
import fr.upmc.requestdispatcher.interfaces.RequestDispatcherDynamicStateI;
import fr.upmc.requestdispatcher.interfaces.RequestDispatcherStateDataConsumerI;
import fr.upmc.requestdispatcher.ports.RequestDispatcherDynamicStateDataOutboundPort;

/**
 * The class <code>AutonomicController</code> implements a component that check 
 * and do adaptations at regular intervals to assure the good work of the data center.
 *
 * <p><strong>Description</strong></p>
 * 
 * The Autonomic Controller (AC) component simulates the execution of a data center
 * by receiving informations from Request Dispatcher about the requests's execution time.
 * Then he check if the different elements of the data center needs adaptations and make
 * these adaptations.
 * 
 * <pre>
 * invariant	true
 * </pre>
 * 
 * <p>Created on : 10 september 2017</p>
 * 
 * @author	<a href="mailto:morvanlassauzay@gmail.com">Morvan Lassauzay</a>
 * @version	$Name$ -- $Revision$ -- $Date$
 */

public class AutonomicController
extends		AbstractComponent
implements	AutonomicControllerManagementI,
			RequestDispatcherStateDataConsumerI
{
	
	/* Make adaptation every n seconds. */
	private static final int IntervalForAdaptation = 4000;
	/* The max requests's execution time average accepted for a VM */
	private static final double AverageMax  = 3000;
	/* The max requests's execution time average accepted for a VM */
	private static final double AverageMin  = 1000;
	
	
	// ------------------------------------------------------------------------
	// Component internal state
	// ------------------------------------------------------------------------
	
	/** URI of this dispatcher.														*/	
	protected String								autonomicControllerURI ;
	/** Inbound port offering the management interface.								*/
	protected AutonomicControllerManagementInboundPort acmip ;
	
	public static final String	RdDynamicStateDataOutboundPortURI = "rdds-dop" ;
	/**  output port used to access to the dynamic state data of the RD.			*/	
	protected RequestDispatcherDynamicStateDataOutboundPort		rddsPort;
	/** Dynamic state of the request dispatcher										*/
	protected RequestDispatcherDynamicStateI 					rdds;
	/** future of the task scheduled to do adaptation.								*/
	
	public static final String	ACCoordinationOutboundPortURI = "accop" ;
	/** Port connected to the admission controller component to coordinate VMs creation.			*/
	protected AdmissionControllerCoordinationOutboundPort	accop ;
	
	protected ScheduledFuture<?>								pushingFuture ;
	
	// ------------------------------------------------------------------------
	// Component constructor
	// ------------------------------------------------------------------------
	
	/**
	 * create a new autonomic controller with the given URI, and the URIs 
	 * to be used to create and publish its inbound and outbound ports.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * TODO: complete!
	 * 
	 * <pre>
	 * pre autonomicControllerURI != null
	 * pre managementInboundPortURI != null
	 * pre AdmissionControllerCoordinationInboundPortURI != null
	 * post	true			// no postcondition.
	 * </pre>
	 * 
	 * @param autonomicControllerURI 			  URI of the newly created AC.
	 * @param managementInboundPortURI            URI of the RD management inbound port.
	 * @param AdmissionControllerCoordinationInboundPortURI URI to the Ac Coordination inbound port.
	 * @throws Exception
	 */
	public				AutonomicController(
		String autonomicControllerURI,
		String managementInboundPortURI,
		String AdmissionControllerCoordinationInboundPortURI
		) throws Exception
	{
		// The normal thread pool is used to process component services, while
		// the scheduled one is used to schedule the pushes of dynamic state
		// when requested.
		super(1, 1) ;
		
		// Preconditions
		assert autonomicControllerURI != null ;
		assert managementInboundPortURI != null ;
		
		this.autonomicControllerURI = autonomicControllerURI;
		
		// Interfaces and ports
		this.addOfferedInterface(AutonomicControllerManagementI.class) ;
		this.acmip = new AutonomicControllerManagementInboundPort(
				managementInboundPortURI, this) ;
		this.addPort(this.acmip) ;
		this.acmip.publishPort() ;
		
		this.accop = new AdmissionControllerCoordinationOutboundPort(
						ACCoordinationOutboundPortURI,
						new AbstractComponent(0, 0) {}) ;
		this.accop.publishPort() ;
		this.accop.
			doConnection(
					AdmissionControllerCoordinationInboundPortURI,
					AdmissionControllerCoordinationConnector.class.getCanonicalName()) ;
	
		this.rdds = null;
	}
	
	// ------------------------------------------------------------------------
	// Component life-cycle
	// ------------------------------------------------------------------------

	/**
	 * @see fr.upmc.components.AbstractComponent#shutdown()
	 */
	@Override
	public void			shutdown() throws ComponentShutdownException
	{
		try {			
			if (this.rddsPort.connected()) {
				this.rddsPort.doDisconnection() ;
			}
			if (this.accop.connected()) {
				this.accop.doDisconnection() ;
			}
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}

		super.shutdown();
	}
	
	// ------------------------------------------------------------------------
	// Component services
	// ------------------------------------------------------------------------

	/* 
	 * @see fr.upmc.requestdispatcher.interfaces.RequestDispatcherStateDataConsumerI#acceptRequestDispatcherDynamicData(java.lang.String, fr.upmc.requestdispatcher.interfaces.RequestDispatcherDynamicStateI)
	 */
	@Override
	public void acceptRequestDispatcherDynamicData(String requestDispatcherURI,
			RequestDispatcherDynamicStateI currentDynamicState) throws Exception {
		
			this.rdds = currentDynamicState;	
	}
	
	// ------------------------------------------------------------------------
	// Component management services
	// ------------------------------------------------------------------------
	
	/**
	 * @see fr.upmc.autonomicController.interfaces.AutonomicControllerManagementI#checkAndDoAdaptation()
	 */
	public void checkAndDoAdaptation() {
		this.pushingFuture =
				this.scheduleTaskAtFixedRate(
						new ComponentI.ComponentTask() {
							@Override
							public void run() {
								
								RequestDispatcherDynamicStateI rdds = getRequestDispatcherDynamicData();
//								System.out.println(rdds.getExecutionTimeAverageVMs().toString());
								
								// Global average of the application
								Long globalAverage = (long) 0;
								// If at least one cores were down or up
								boolean downCores = false;
								boolean upCores = false;
								
								// Get list of VMs and list of corresponding average
								List<String> vms = new ArrayList(rdds.getExecutionTimeAverageVMs().keySet());
								List<Long> averages = new ArrayList(rdds.getExecutionTimeAverageVMs().values());
								
								for(int i = 0; i < vms.size(); i++)
								{		
									// Get URI and average of the VM
									String vmURI = vms.get(i);
									Long average = averages.get(i);
									globalAverage += average;
									if (average > AverageMax){
										// Try to up cores frequencies
									}
									else if (average < AverageMin){
										// Try to down cores frequencies
									}
								}	
								
								globalAverage = globalAverage/vms.size();
//								System.out.println("Average of dispatcher " + rdds.getRequestDispatcherURI() 
//																			+ " : " + globalAverage);
								
								// Remove a VM if we can't change cores frequencies
								if( (globalAverage < AverageMin)  &&  (downCores == false) ){
									
								}
								// Add a VM if we can't change cores frequencies
								else if( (globalAverage > AverageMax)  &&  (upCores == false) ){
									try {
										addVmRequest(rdds.getRequestDispatcherURI());
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							}
						}, IntervalForAdaptation, IntervalForAdaptation, TimeUnit.MILLISECONDS) ;
	}

	/**
	 * @see fr.upmc.autonomicController.interfaces.AutonomicControllerManagementI#connectRequestDispatcher(java.lang.String, java.lang.String)
	 */
	public void connectRequestDispatcher(
			String RdDynamicStateDataInboundPortURI,
			String RdURI
			) throws Exception {
		
		this.rddsPort = new RequestDispatcherDynamicStateDataOutboundPort(
				RdDynamicStateDataOutboundPortURI,
				this,
				RdURI
				) ;
		this.rddsPort.publishPort() ;
		this.rddsPort.doConnection(
				RdDynamicStateDataInboundPortURI,
				ControlledDataConnector.class.getCanonicalName()) ;
		
		this.rddsPort.startUnlimitedPushing(1000);
	}
	
	// ------------------------------------------------------------------------
	// Component internal services
	// ------------------------------------------------------------------------
	
	/**
	 * Forward execution's end notification for a request r previously
	 * submitted. 
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * post	true			// no postcondition.
	 * </pre>
	 */
	private RequestDispatcherDynamicStateI getRequestDispatcherDynamicData() {
		
		return this.rdds;
	}
	
	/**
	 * Make a Request to the Admission Controller to add a VM on a Dispatcher
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * post	true			// no postcondition.
	 * </pre>
	 * 
	 * @param RequestDispatcherURI   The URI of the RD on wich we want create a VM
	 * @return 						 true if a VM has been created, false otherwise
	 * @throws Exception 
	 */
	private boolean addVmRequest (String RequestDispatcherURI) throws Exception{
		
		return this.accop.addVmIfPossible(RequestDispatcherURI);
	}
}
