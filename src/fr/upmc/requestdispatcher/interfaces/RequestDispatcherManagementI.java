package fr.upmc.requestdispatcher.interfaces;


/**
 * The interface <code>RequestDispatcherManagementI</code> defines the management
 * actions provided by the request dispatcher component.
 *
 * <p><strong>Description</strong></p>
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
public interface RequestDispatcherManagementI {
	
	/**
	 * Connect a request receiver with the request dispatcher
	 * 
	 * @param requestSubmissionInboundPortURI 	URI of inbound port of receiver for requests submission
	 * @throws Exception	
	 */
	public void addRequestReceiver(
			String vmURI,
			String requestSubmissionInboundPortURI
			) throws Exception;
	
	/**
	 * Connect a request receiver with the request dispatcher
	 * 
	 * @param requestSubmissionInboundPortURI	URI of inbound port of receiver for requests submission
	 * @param connectorClass 					type of connector used for connection
	 * @throws Exception
	 */
	public void addRequestReceiver(
			String vmURI,
			String requestSubmissionInboundPortURI,
			Class<?> connectorClass
			) throws Exception;
}
