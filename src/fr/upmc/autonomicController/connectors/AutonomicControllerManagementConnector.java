package fr.upmc.autonomicController.connectors;

import fr.upmc.autonomicController.interfaces.AutonomicControllerManagementI;
import fr.upmc.components.connectors.AbstractConnector;

/**
 * The class <code>AutonomicControllerManagementConnector</code> implements a
 * standard client/server connector for the management autonomic controller
 * management interface.
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
public class AutonomicControllerManagementConnector
extends		AbstractConnector
implements	AutonomicControllerManagementI
{

	/**
	 * @see fr.upmc.autonomicController.interfaces.AutonomicControllerManagementI#checkAndDoAdaptation()
	 */
	@Override
	public void checkAndDoAdaptation() throws Exception {
		((AutonomicControllerManagementI)this.offering).
											checkAndDoAdaptation();
	}

	/**
	 * @see fr.upmc.autonomicController.interfaces.AutonomicControllerManagementI#connectRequestDispatcher(java.lang.String, java.lang.String)
	 */
	@Override
	public void connectRequestDispatcher(
			String RdDynamicStateDataInboundPortURI, 
			String RdURI
			) throws Exception {
		((AutonomicControllerManagementI)this.offering).
				connectRequestDispatcher(RdDynamicStateDataInboundPortURI, RdURI);
	}
}
