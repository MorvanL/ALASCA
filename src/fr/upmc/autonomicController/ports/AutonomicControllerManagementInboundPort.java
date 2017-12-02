package fr.upmc.autonomicController.ports;

import fr.upmc.autonomicController.AutonomicController;
import fr.upmc.autonomicController.interfaces.AutonomicControllerManagementI;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;

/**
 * The class <code>AutonomicControllerManagementInboundPort</code> implements the
 * inbound port through which the component management methods are called.
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
public class 			AutonomicControllerManagementInboundPort
extends		AbstractInboundPort
implements	AutonomicControllerManagementI
{
	
	private static final long serialVersionUID = 1L ;

	public				AutonomicControllerManagementInboundPort(
		ComponentI owner
		) throws Exception
	{
		super(AutonomicControllerManagementI.class, owner) ;

		assert	owner != null && owner instanceof AutonomicController ;
	}

	public				AutonomicControllerManagementInboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, AutonomicControllerManagementI.class, owner);

		assert	owner != null && owner instanceof AutonomicController ;
	}

	/**
	 * @see fr.upmc.autonomicController.interfaces.AutonomicControllerManagementI#checkAndDoAdaptation()
	 */
	@Override
	public void checkAndDoAdaptation() throws Exception {
		final AutonomicController ac = (AutonomicController) this.owner ;
		this.owner.handleRequestAsync(
					new ComponentI.ComponentService<Void>() {
						@Override
						public Void call() throws Exception {
							ac.checkAndDoAdaptation();
							return null;
						}
					}) ;
	}

	/**
	 * @see fr.upmc.autonomicController.interfaces.AutonomicControllerManagementI#connectRequestDispatcher(java.lang.String, java.lang.String)
	 */
	@Override
	public void connectRequestDispatcher(final String RdDynamicStateDataInboundPortURI, final String RdURI) throws Exception {
		final AutonomicController ac = (AutonomicController) this.owner ;
		this.owner.handleRequestAsync(
					new ComponentI.ComponentService<Void>() {
						@Override
						public Void call() throws Exception {
							ac.connectRequestDispatcher(RdDynamicStateDataInboundPortURI, RdURI);
							return null;
						}
					}) ;
	}
}
