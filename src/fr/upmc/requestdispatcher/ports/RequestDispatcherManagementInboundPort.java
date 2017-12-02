package fr.upmc.requestdispatcher.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.requestdispatcher.RequestDispatcher;
import fr.upmc.requestdispatcher.interfaces.RequestDispatcherManagementI;

/**
 * The class <code>RequestDispatcherManagementInboundPort</code> implements the
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
 * <p>Created on : 18 novembre 2017</p>
 * 
 * @author	<a href="mailto:morvanlassauzay@gmail.com">Morvan Lassauzay</a>
 * @version	$Name$ -- $Revision$ -- $Date$
 */
public class			RequestDispatcherManagementInboundPort
extends		AbstractInboundPort
implements	RequestDispatcherManagementI
{
	
	private static final long serialVersionUID = 1L ;

	public				RequestDispatcherManagementInboundPort(
		ComponentI owner
		) throws Exception
	{
		super(RequestDispatcherManagementI.class, owner) ;

		assert	owner != null && owner instanceof RequestDispatcherManagementI ;
	}

	public				RequestDispatcherManagementInboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, RequestDispatcherManagementI.class, owner);

		assert	owner != null && owner instanceof RequestDispatcherManagementI ;
	}
	
	/* 
	 * @see fr.upmc.requestdispatcher.interfaces.RequestDispatcherManagementI#addRequestReceiver(java.lang.String, fr.upmc.datacenter.software.ports.RequestNotificationOutboundPort)
	 */
	@Override
	public void addRequestReceiver(
			final String vmURI,
			final String requestSubmissionInboundPortURI)
			throws Exception {
		
		final RequestDispatcher rd = (RequestDispatcher) this.owner ;
		this.owner.handleRequestAsync(
					new ComponentI.ComponentService<Void>() {
						@Override
						public Void call() throws Exception {
							rd.addRequestReceiver(vmURI, 
												  requestSubmissionInboundPortURI);
							return null;
						}
					}) ;
		
	}
	
	/* 
	 * @see fr.upmc.requestdispatcher.interfaces.RequestDispatcherManagementI#addRequestReceiver(java.lang.String, fr.upmc.datacenter.software.ports.RequestNotificationOutboundPort, java.lang.Class)
	 */
	@Override
	public void addRequestReceiver(
			final String vmURI,
			final String requestSubmissionInboundPortURI,
			final Class<?> connectorClass
			) throws Exception {
		
		final RequestDispatcher rd = (RequestDispatcher) this.owner ;
		this.owner.handleRequestAsync(
					new ComponentI.ComponentService<Void>() {
						@Override
						public Void call() throws Exception {
							rd.addRequestReceiver(vmURI,
												  requestSubmissionInboundPortURI,
												  connectorClass);
							return null;
						}
					}) ;
		
	}
}
