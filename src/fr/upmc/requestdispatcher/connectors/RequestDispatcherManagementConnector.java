package fr.upmc.requestdispatcher.connectors;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.requestdispatcher.interfaces.RequestDispatcherManagementI;

/**
 * The class <code>RequestDispatcherManagementConnector</code> implements a
 * standard client/server connector for the management request dispatcher
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
 * <p>Created on : 18 novembre 2017</p>
 * 
 * @author	<a href="mailto:morvanlassauzay@gmail.com">Morvan Lassauzay</a>
 * @version	$Name$ -- $Revision$ -- $Date$
 */
public class			RequestDispatcherManagementConnector
extends		AbstractConnector
implements	RequestDispatcherManagementI
{
	
	/* 
	 * @see fr.upmc.requestdispatcher.interfaces.RequestDispatcherManagementI#addRequestReceiver(java.lang.String, fr.upmc.datacenter.software.ports.RequestNotificationOutboundPort)
	 */
	@Override
	public void addRequestReceiver(
			String vmURI,
			String requestSubmissionInboundPortURI)
			throws Exception {
		((RequestDispatcherManagementI)this.offering).
						addRequestReceiver(vmURI,requestSubmissionInboundPortURI);
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
		((RequestDispatcherManagementI)this.offering).
							addRequestReceiver(vmURI,
									           requestSubmissionInboundPortURI,
											   connectorClass);
	}
}
