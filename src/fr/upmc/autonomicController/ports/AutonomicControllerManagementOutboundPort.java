package fr.upmc.autonomicController.ports;

import fr.upmc.autonomicController.interfaces.AutonomicControllerManagementI;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;

/**
 * The class <code>AutonomicControllerManagementOutboundPort</code> implements the
 * outbound port through which one calls the component management methods.
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
public class AutonomicControllerManagementOutboundPort
extends		AbstractOutboundPort
implements	AutonomicControllerManagementI
{

	public				AutonomicControllerManagementOutboundPort(
		ComponentI owner
		) throws Exception
	{
		super(AutonomicControllerManagementI.class, owner) ;

		assert	owner != null ;
	}

	public				AutonomicControllerManagementOutboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, AutonomicControllerManagementI.class, owner) ;

		assert	uri != null && owner != null ;
	}

	/**
	 * @see fr.upmc.autonomicController.interfaces.AutonomicControllerManagementI#checkAndDoAdaptation()
	 */
	@Override
	public void checkAndDoAdaptation() throws Exception {
		((AutonomicControllerManagementI)this.connector).
		checkAndDoAdaptation() ;
	}

	/**
	 * @see fr.upmc.autonomicController.interfaces.AutonomicControllerManagementI#connectRequestDispatcher(java.lang.String, java.lang.String)
	 */
	@Override
	public void connectRequestDispatcher(String RdDynamicStateDataInboundPortURI, String RdURI) throws Exception {
		((AutonomicControllerManagementI)this.connector).
		connectRequestDispatcher(RdDynamicStateDataInboundPortURI, RdURI);
	}
}
