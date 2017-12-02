package fr.upmc.requestdispatcher.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.requestdispatcher.interfaces.RequestDispatcherManagementI;

/**
 * The class <code>RequestDispactherManagementOutboundPort</code> implements the
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
 * <p>Created on : 18 novembre 2017</p>
 * 
 * @author	<a href="mailto:morvanlassauzay@gmail.com">Morvan Lassauzay</a>
 * @version	$Name$ -- $Revision$ -- $Date$
 */
public class			RequestDispatcherManagementOutboundPort
extends		AbstractOutboundPort
implements	RequestDispatcherManagementI
{
	public				RequestDispatcherManagementOutboundPort(
		ComponentI owner
		) throws Exception
	{
		super(RequestDispatcherManagementI.class, owner) ;

		assert	owner != null ;
	}

	public				RequestDispatcherManagementOutboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, RequestDispatcherManagementI.class, owner) ;

		assert	uri != null && owner != null ;
	}
	
	/* 
	 * @see fr.upmc.requestdispatcher.interfaces.RequestDispatcherManagementI#addRequestReceiver(java.lang.String, fr.upmc.datacenter.software.ports.RequestNotificationOutboundPort)
	 */
	@Override
	public void addRequestReceiver(
			String vmURI,
			String requestSubmissionInboundPortURI
			)throws Exception {
		((RequestDispatcherManagementI)this.connector).
							addRequestReceiver(vmURI,
											   requestSubmissionInboundPortURI);
	}
	
	/* 
	 * @see fr.upmc.requestdispatcher.interfaces.RequestDispatcherManagementI#addRequestReceiver(java.lang.String, fr.upmc.datacenter.software.ports.RequestNotificationOutboundPort, java.lang.Class)
	 */
	@Override
	public void addRequestReceiver(
			String vmURI,
			String requestSubmissionInboundPortURI,
			Class<?> connectorClass
			)throws Exception {
		((RequestDispatcherManagementI)this.connector).
					addRequestReceiver(vmURI,
									   requestSubmissionInboundPortURI, 
									   connectorClass);
	}
}
