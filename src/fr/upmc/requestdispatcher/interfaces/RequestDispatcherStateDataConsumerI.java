package fr.upmc.requestdispatcher.interfaces;

/**
 * The interface <code>RequestDispatcherStateDataConsumerI</code> defines the consumer
 * side methods used to receive state data pushed by a RD.
 *
 * <p><strong>Description</strong></p>
 * 
 * The interface must be implemented by all classes representing components
 * that will consume as clients state data pushed by a RD.  They are
 * used by <code>RequestDispatcherStaticStateOutboundPort</code> and
 * <code>RequestDispatcherDynamicStateOutboundPort</code> to pass these data
 * upon reception from the VM component.
 * 
 * As a client component may receive data from several different RD,
 * it can assign URI to each at the creation of outbound ports, so that these
 * can pass these URI when receiving data.  Hence, the methods defined in this
 * interface will be unique in one client component but receive the data pushed
 * by all of the different RD.
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
public interface RequestDispatcherStateDataConsumerI {

	/**
	 * accept the dynamic data pushed by a RD with the given URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	requestDispatcherURI != null && currentDynamicState != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param requestDispatcherURI			URI of the RD sending the data.
	 * @param currentDynamicState			current dynamic state of this RD.
	 * @throws Exception
	 */
	public void			acceptRequestDispatcherDynamicData(
		String					requestDispatcherURI,
		RequestDispatcherDynamicStateI	currentDynamicState
		) throws Exception ;
}
