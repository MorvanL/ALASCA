package fr.upmc.requestdispatcher;


import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.ComponentI;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.datacenter.TimeManagement;
import fr.upmc.datacenter.interfaces.ControlledDataOfferedI;
import fr.upmc.datacenter.interfaces.PushModeControllingI;
import fr.upmc.datacenter.software.connectors.RequestSubmissionConnector;
import fr.upmc.datacenter.software.interfaces.RequestI;
import fr.upmc.datacenter.software.interfaces.RequestNotificationHandlerI;
import fr.upmc.datacenter.software.interfaces.RequestNotificationI;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionHandlerI;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionI;
import fr.upmc.datacenter.software.ports.RequestNotificationInboundPort;
import fr.upmc.datacenter.software.ports.RequestNotificationOutboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionInboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort;
import fr.upmc.requestdispatcher.interfaces.RequestDispatcherDynamicStateI;
import fr.upmc.requestdispatcher.interfaces.RequestDispatcherManagementI;
import fr.upmc.requestdispatcher.ports.RequestDispatcherDynamicStateDataInboundPort;
import fr.upmc.requestdispatcher.ports.RequestDispatcherManagementInboundPort;
import fr.upmc.requestdispatcher.utils.RequestExecutionInformations;

/**
 * The class <code>RequestDispatcher</code> implements a component that distributes
 * requests received from generator by submitting them to many Application 
 * VMs components.
 *
 * <p><strong>Description</strong></p>
 * 
 * The Request Dispatcher (RD) component simulates the execution of a data center
 * by receiving requests, dispatch them between many AVMs, and notifying the
 * emitter of the end of execution of its request (as a way to simulate
 * the return of the result).
 * 
 * As a component, the Request Dispatcher offers a request submission service through the
 * interface <code>RequestSubmissionI</code> implemented by
 * <code>RequestSubmissionInboundPort</code> inbound port.
 * It also offers a request notification service through the
 * interface <code>RequestNotificationI</code> implemented by
 * <code>RequestNotificationInboundPort</code> inbound port.
 *  To forward requests, the Request Dispatcher requires the interface
 * <code>RequestSubmissionI</code> through the
 * <code>RequestSubmissionOutboundPort</code> outbound port.
 *  To notify the end of the execution of requests, the Request Dispatcher requires the interface
 * <code>RequestNotificationI</code> through the
 * <code>RequestNotificationOutboundPort</code> outbound port.
 * 
 * The Request Dispatcher can be managed (essentially to connect AVMs) and it offers the
 * interface <code>RequestDispatcherManagementI</code> through the inbound port
 * <code>RequestDispatcherManagementInboundPort</code> for this.
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
public class			RequestDispatcher
extends		AbstractComponent
implements	RequestNotificationHandlerI,
			RequestSubmissionHandlerI,
			RequestDispatcherManagementI,
			PushModeControllingI
{
	/* Number of requests by VM used to compute average execution time 				*/ 
	protected static final int	NbRequestForAverage = 5 ;
	/* Fake initial request's execution time to compute average at the beginning	*/
	protected static final Long RequestTimeExecutionInitial = (long) 2;
	
	// ------------------------------------------------------------------------
	// Component internal state
	// ------------------------------------------------------------------------

	/** URI of this dispatcher.														*/
	protected String						dispatcherURI ;
	/** Inbound port offering the management interface.								*/
	protected RequestDispatcherManagementInboundPort rdmip ;
	/** Inbound port offering the request submission service of the Dispatcher.		*/
	protected RequestSubmissionInboundPort	requestSubmissionInboundPort ;
	/** Outbound port used by the Dispatcher to notify tasks' termination.			*/
	protected RequestNotificationOutboundPort
											requestNotificationOutboundPort ;
	/** Map between VM URIs and the output ports used to send requests 
	 * to the service provider of each VM.											*/
	protected Map<String, RequestSubmissionOutboundPort>		
													requestSubmissionOutboundPorts ;
	/** the inbound port receiving end of execution notifications by VMs			*/
	protected RequestNotificationInboundPort	requestNotificationInboundPort ;
	/** next VM which will receive a request 										*/
	protected int nextVmForRequest ;
	/** rd data inbound port through which it pushes its dynamic data.				*/
	protected RequestDispatcherDynamicStateDataInboundPort 
										rdDynamicStateDataInboundPort;
	/** Map between VMs URIs and execution time of her n last request .						*/
	protected final Map<String, Long[]> vmsExecutionTimeRequests;
	/** future of the task scheduled to push dynamic data.							*/
	protected ScheduledFuture<?>			pushingFuture ;
	/** Map between requests URIs which are being executing and utils 
	 * informations about their execution 											*/
	protected Map<String,RequestExecutionInformations> executingRequestsInformations;
	
	// ------------------------------------------------------------------------
	// Component constructor
	// ------------------------------------------------------------------------
	
	/**
	 * create a new request dispatcher with the given URI, and the URIs 
	 * to be used to create and publish its inbound and outbound ports.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * TODO: complete!
	 * 
	 * <pre>
	 * pre	dispatcherURI != null
	 * pre	managementInboundPortURI != null
	 * pre	requestSubmissionInboundPortURI != null
	 * pre	requestNotificationOutboundPortURI != null
	 * pre	requestNotificationInboundPortURI != null
	 * pre	rdDynamicStateDataInboundPortURI != null
	 * post	true			// no postcondition.
	 * </pre>
	 * 
	 * @param dispatcherURI 				      URI of the newly created RD.
	 * @param managementInboundPortURI            URI of the RD management inbound port.
	 * @param requestSubmissionInboundPortURI     URI of the request submission inbound port.
	 * @param requestNotificationOutboundPortURI  URI of the request notification outbound port.
	 * @param requestNotificationInboundPortURI   URI of the request notification inbound port.
	 * @param rdDynamicStateDataInboundPortURI	  URI of the rd dynamic data notification inbound port.
	 * @throws Exception
	 */
	public				RequestDispatcher(
		String dispatcherURI,
		String managementInboundPortURI,
		String requestSubmissionInboundPortURI,
		String requestNotificationOutboundPortURI,
		String requestNotificationInboundPortURI,
		String rdDynamicStateDataInboundPortURI
		) throws Exception
	{
		// The normal thread pool is used to process component services, while
		// the scheduled one is used to schedule the pushes of dynamic state
		// when requested.
		super(1, 1) ;

		// Preconditions
		assert dispatcherURI != null ;
		assert managementInboundPortURI != null ;
		assert requestSubmissionInboundPortURI != null ;
		assert requestNotificationOutboundPortURI != null ;
		assert requestNotificationInboundPortURI != null ;
		assert rdDynamicStateDataInboundPortURI != null ;

		this.dispatcherURI = dispatcherURI ;

		// Interfaces and ports
		this.addOfferedInterface(RequestDispatcherManagementI.class) ;
		this.rdmip = new RequestDispatcherManagementInboundPort(
												managementInboundPortURI, this) ;
		this.addPort(this.rdmip) ;
		this.rdmip.publishPort() ;
		
		this.addOfferedInterface(RequestSubmissionI.class) ;
		this.requestSubmissionInboundPort =
						new RequestSubmissionInboundPort(
										requestSubmissionInboundPortURI, this) ;
		this.addPort(this.requestSubmissionInboundPort) ;
		this.requestSubmissionInboundPort.publishPort() ;

		this.addRequiredInterface(RequestNotificationI.class) ;
		this.requestNotificationOutboundPort =
			new RequestNotificationOutboundPort(
									requestNotificationOutboundPortURI,
									this) ;
		this.addPort(this.requestNotificationOutboundPort) ;
		this.requestNotificationOutboundPort.publishPort() ;
		
		this.addRequiredInterface(RequestSubmissionI.class) ;
		requestSubmissionOutboundPorts = 
							new LinkedHashMap<String, RequestSubmissionOutboundPort>(); 			

		this.addOfferedInterface(RequestNotificationI.class) ;
		this.requestNotificationInboundPort =
			new RequestNotificationInboundPort(requestNotificationInboundPortURI, this) ;
		this.addPort(this.requestNotificationInboundPort) ;
		this.requestNotificationInboundPort.publishPort() ;
	
		this.nextVmForRequest = 0;
		
		this.vmsExecutionTimeRequests = new LinkedHashMap<String, Long[]>();
		
		this.addOfferedInterface(ControlledDataOfferedI.ControlledPullI.class) ;
		this.rdDynamicStateDataInboundPort =
				new RequestDispatcherDynamicStateDataInboundPort(
								rdDynamicStateDataInboundPortURI, this) ;
		this.addPort(rdDynamicStateDataInboundPort) ;
		this.rdDynamicStateDataInboundPort.publishPort() ;
		
		executingRequestsInformations = 
						new LinkedHashMap<String, RequestExecutionInformations>();
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
		// Disconnect ports to the requests emitter.
		// and to the requests receivers
		try {
			if (this.requestNotificationOutboundPort.connected()) {
				this.requestNotificationOutboundPort.doDisconnection() ;
			}
			
			for (Entry<String, RequestSubmissionOutboundPort> entry :  
								requestSubmissionOutboundPorts.entrySet()) {
				RequestSubmissionOutboundPort rsop = entry.getValue();
				if (rsop.connected()) {
					rsop.doDisconnection() ;
				}				
			}
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}

		super.shutdown();
	}
	
	// ------------------------------------------------------------------------
	// Component introspection services
	// ------------------------------------------------------------------------
	
	/**
	 * collect and return the dynamic state of the RD.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @return	the dynamic state of the RD.
	 * @throws Exception
	 */
	public RequestDispatcherDynamicStateI	getDynamicState() throws Exception
	{
		Map<String, Long> vmsExecutionTimeAverage = new LinkedHashMap<String, Long>();
		
		// Compute requests's execution time for each vm 
		Iterator<Entry<String, Long[]>> it = this.vmsExecutionTimeRequests.entrySet().iterator();
		while (it.hasNext())
		{
			Entry<String, Long[]> e = it.next();
			Long [] tab = e.getValue();
			Long sum = (long) 0;
			for(int i = 0; i < NbRequestForAverage; i++){
				sum += tab[i];
			}
			Long average = sum / NbRequestForAverage;
			vmsExecutionTimeAverage.put(e.getKey(), average);
		}
		
		return new RequestDispatcherDynamicState(this.dispatcherURI, vmsExecutionTimeAverage) ;
	}
	
	/**
	 * push the dynamic state of the RD through its notification data
	 * inbound port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @throws Exception
	 */
	public void			sendDynamicState() throws Exception
	{
		if (this.rdDynamicStateDataInboundPort.connected()) {
			RequestDispatcherDynamicStateI rdds = this.getDynamicState() ;
			this.rdDynamicStateDataInboundPort.send(rdds) ;
		}
	}
	
	/**
	 * push the dynamic state of the rd through its notification data
	 * inbound port at a specified time interval in ms and for a specified
	 * number of times.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param interval
	 * @param numberOfRemainingPushes
	 * @throws Exception
	 */
	public void			sendDynamicState(
		final int interval,
		int numberOfRemainingPushes
		) throws Exception
	{
		this.sendDynamicState() ;
		final int fNumberOfRemainingPushes = numberOfRemainingPushes - 1 ;
		if (fNumberOfRemainingPushes > 0) {
			final RequestDispatcher rd = this ;
			this.pushingFuture =
					this.scheduleTask(
							new ComponentI.ComponentTask() {
								@Override
								public void run() {
									try {
										rd.sendDynamicState(
												interval,
												fNumberOfRemainingPushes) ;
									} catch (Exception e) {
										throw new RuntimeException(e) ;
									}
								}
							},
							TimeManagement.acceleratedDelay(interval),
							TimeUnit.MILLISECONDS) ;
		}
	}
	
	/**
	 * @see fr.upmc.datacenter.interfaces.PushModeControllingI#startUnlimitedPushing(int)
	 */
	@Override
	public void			startUnlimitedPushing(int interval) throws Exception
	{
		final RequestDispatcher rd = this ;
		this.pushingFuture =
			this.scheduleTaskAtFixedRate(
					new ComponentI.ComponentTask() {
						@Override
						public void run() {
							try {
								rd.sendDynamicState() ;
							} catch (Exception e) {
								throw new RuntimeException(e) ;
							}
						}
					},
					TimeManagement.acceleratedDelay(interval),
					TimeManagement.acceleratedDelay(interval),
					TimeUnit.MILLISECONDS) ;
	}
	
	/**
	 * @see fr.upmc.datacenter.interfaces.PushModeControllingI#startLimitedPushing(int, int)
	 */
	@Override
	public void			startLimitedPushing(final int interval, final int n)
	throws Exception
	{
		assert	n > 0 ;

		this.logMessage(this.dispatcherURI + " startLimitedPushing with interval "
									+ interval + " ms for " + n + " times.") ;

		final RequestDispatcher rd = this ;
		this.pushingFuture =
			this.scheduleTask(
					new ComponentI.ComponentTask() {
						@Override
						public void run() {
							try {
								rd.sendDynamicState(interval, n) ;
							} catch (Exception e) {
								throw new RuntimeException(e) ;
							}
						}
					},
					TimeManagement.acceleratedDelay(interval),
					TimeUnit.MILLISECONDS) ;
	}
	
	/**
	 * @see fr.upmc.datacenter.interfaces.PushModeControllingI#stopPushing()
	 */
	@Override
	public void			stopPushing() throws Exception
	{
		if (this.pushingFuture != null &&
							!(this.pushingFuture.isCancelled() ||
												this.pushingFuture.isDone())) {
			this.pushingFuture.cancel(false) ;
		}
	}

	// ------------------------------------------------------------------------
	// Component services
	// ------------------------------------------------------------------------

	/**
	 * @see fr.upmc.datacenter.software.interfaces.RequestSubmissionHandlerI#acceptRequestSubmission(fr.upmc.datacenter.software.interfaces.RequestI)
	 */
	@Override
	public void			acceptRequestSubmission(final RequestI r)
	throws Exception
	{}
	
	/**
	 * @see fr.upmc.datacenter.software.interfaces.RequestSubmissionHandlerI#acceptRequestSubmissionAndNotify(fr.upmc.datacenter.software.interfaces.RequestI)
	 */
	@Override
	public void acceptRequestSubmissionAndNotify(RequestI r) throws Exception {	
		// Forward the current request to next VM in the list.
		this.logMessage("Request Dispatcher " + this.dispatcherURI + " has received "+ r.getRequestURI()+".") ;
		
		Iterator<Entry<String, RequestSubmissionOutboundPort>> it = this.requestSubmissionOutboundPorts.entrySet().iterator();
		int i = 0;
		while (it.hasNext())
		{
			Entry<String, RequestSubmissionOutboundPort> e = it.next();
			
			if (i == nextVmForRequest) {
				// Record informations about execution of request to then calculate execution time
				RequestExecutionInformations rei = new RequestExecutionInformations(
						r.getRequestURI(), 
						System.currentTimeMillis(), 
						e.getKey()
						);
				this.executingRequestsInformations.put(r.getRequestURI(),rei);

				e.getValue().submitRequestAndNotify(r) ;
			}
			i++;
		}
		
		nextVmForRequest = (nextVmForRequest + 1) % requestSubmissionOutboundPorts.size();
	}
	
	/**
	 * Forward execution's end notification for a request r previously
	 * submitted. 
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	r != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param r	request that just terminated.
	 * @throws Exception
	 */
	@Override
	public void			acceptRequestTerminationNotification(RequestI r)
	throws Exception
	{
		assert	r != null ;
		this.logMessage("Request Dispatcher " + this.dispatcherURI + " is notified that request "+ r.getRequestURI() + " has ended.") ;
		this.requestNotificationOutboundPort.notifyRequestTermination(r) ;
		
		computeAndRecordRequestExecutionTime(r.getRequestURI());
	}
	
	// ------------------------------------------------------------------------
	// Component internal services
	// ------------------------------------------------------------------------
	
	/**
	 * Compute and record requests's execution time
	 * 
	 * @param requestURI 	URI of request
	 */
	protected void computeAndRecordRequestExecutionTime(String requestURI)
	{
		long currentTime = System.currentTimeMillis();
		RequestExecutionInformations rei = this.executingRequestsInformations.get(requestURI);
		long executionTime = currentTime - rei.getArrivalTime();

		Long [] tab = vmsExecutionTimeRequests.get(rei.getVmUri());
		Long indice = tab[NbRequestForAverage];
		tab[indice.intValue()] = executionTime;
		tab[NbRequestForAverage] = (indice + 1) % NbRequestForAverage;
		
		vmsExecutionTimeRequests.put(rei.getVmUri(), tab);
	}
	
	/**
	 * reboot requests's execution time for all Vms
	 * 
	 */
	protected void rebootExecutionTimeForVms(){
		Iterator<Long[]> it = this.vmsExecutionTimeRequests.values().iterator();
		while (it.hasNext())
		{
			Long [] tab = it.next();
			for(int i = 0; i < NbRequestForAverage; i++){
				tab[i] = RequestTimeExecutionInitial;
			}
			tab[NbRequestForAverage] = (long) 0;
		}
	}
	
	// ------------------------------------------------------------------------
	// Component management services
	// ------------------------------------------------------------------------
	
	/* 
	 * @see fr.upmc.requestdispatcher.interfaces.RequestDispatcherManagementI#addRequestReceiver(java.lang.String, fr.upmc.datacenter.software.ports.RequestNotificationOutboundPort)
	 */
	@Override
	public void addRequestReceiver(
			String vmURI,
			String requestSubmissionInboundPortURI 
			) throws Exception {
		String requestSubmissionOutboundPortURI = "rsobp" + (requestSubmissionOutboundPorts.size()+1);
		RequestSubmissionOutboundPort rdrsobp = new RequestSubmissionOutboundPort(requestSubmissionOutboundPortURI, this);
		requestSubmissionOutboundPorts.put(vmURI, rdrsobp);

		this.addPort(rdrsobp) ;
		rdrsobp.publishPort() ;
		
		this.doPortConnection(
				requestSubmissionOutboundPortURI,
				requestSubmissionInboundPortURI,
				RequestSubmissionConnector.class.getCanonicalName()) ;
		
		// Initialize tab of request's execution time for new VM
		Long [] requestsTimeExecution = new Long [NbRequestForAverage+1];
		this.vmsExecutionTimeRequests.put(vmURI, requestsTimeExecution);
		// Reboot tab of request's execution time for all VMs
		rebootExecutionTimeForVms();
	}
	
	/* 
	 * @see fr.upmc.requestdispatcher.interfaces.RequestDispatcherManagementI#addRequestReceiver(java.lang.String, fr.upmc.datacenter.software.ports.RequestNotificationOutboundPort, java.lang.Class)
	 */
	@Override
	public void addRequestReceiver(
			String vmURI,
			String requestSubmissionInboundPortURI,
			Class<?> connectorClass
			) throws Exception {
		String requestSubmissionOutboundPortURI = "rsobp" + (requestSubmissionOutboundPorts.size()+1);
		RequestSubmissionOutboundPort rdrsobp = new RequestSubmissionOutboundPort(requestSubmissionOutboundPortURI, this);
		requestSubmissionOutboundPorts.put(vmURI, rdrsobp);

		this.addPort(rdrsobp) ;
		rdrsobp.publishPort() ;

		this.doPortConnection(
				requestSubmissionOutboundPortURI,
				requestSubmissionInboundPortURI,
				connectorClass.getCanonicalName());
		
		// Initialize tab of request's execution time for new VM
		Long [] requestsTimeExecution = new Long [NbRequestForAverage+1];
		this.vmsExecutionTimeRequests.put(vmURI, requestsTimeExecution);
		// Reboot tab of request's execution time for all VMs
		rebootExecutionTimeForVms();
	}
}