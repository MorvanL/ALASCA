package fr.upmc.autonomicController.interfaces;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;

/**
 * The interface <code>AutonomicControllerManagementI</code> defines the management
 * actions provided by the autonomic controller component.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
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
public interface AutonomicControllerManagementI
extends		OfferedI,
			RequiredI
{

	/**
	 * Check at regular intervals if the data center needs adaptation and do these adaptations
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @throws Exception
	 */
	public void checkAndDoAdaptation() throws Exception;
	
	/**
	 * Connect a request dispatcher to the autonomic controller
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * post	true			// no postcondition.
	 * </pre>
	 * 
	 * @param RdDynamicStateDataInboundPortURI   URI of inbound port to receive dynamic state data of RD
	 * @param RdURI								 URI of the Request Dispatcher
	 * @throws Exception
	 */
	public void connectRequestDispatcher(
			String RdDynamicStateDataInboundPortURI,
			String RdURI
			) throws Exception ;
}
