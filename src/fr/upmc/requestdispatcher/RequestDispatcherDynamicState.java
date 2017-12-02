package fr.upmc.requestdispatcher;

import java.util.Map;

import fr.upmc.requestdispatcher.interfaces.RequestDispatcherDynamicStateI;

/**
 * The class <code>RequestDispatcherDynamicState</code> implements objects representing
 * a snapshot of the dynamic state of a RD component to be pulled or
 * pushed through the dynamic state data interface.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * TODO: complete!
 * 
 * <pre>
 * invariant	requestDispatcherURI != null && vmsExecutionTimeAverage != null
 * </pre>
 * 
 * <p>Created on : 18 novembre 2017</p>
 * 
 * @author	<a href="mailto:morvanlassauzay@gmail.com">Morvan Lassauzay</a>
 * @version	$Name$ -- $Revision$ -- $Date$
 */
public class 		RequestDispatcherDynamicState 
implements	RequestDispatcherDynamicStateI
{
	private static final long	serialVersionUID = 1L;
	/** URI of the RD to which this dynamic state relates.			*/
	protected final String		requestDispatcherURI;
	/** Map between VMs URIs and their execution time average.		*/
	protected final Map<String, Long> vmsExecutionTimeAverage;
	
	/**
	 * create a dynamic state object.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * TODO: complete!
	 * 
	 * <pre>
	 * pre	requestDispatcherURI != null
	 * pre	vmsExecutionTimeAverage != null
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param requestDispatcherURI		URI of the RD to which this dynamic state relates.
	 * @param vmsExecutionTimeAverage	Map between VMs URIs and their execution time average.
	 * @throws Exception
	 */
	public				RequestDispatcherDynamicState(
		String requestDispatcherURI,
		Map<String, Long> vmsExecutionTimeAverage
		) throws Exception
	{
		super() ;
		
		this.requestDispatcherURI = requestDispatcherURI;
		this.vmsExecutionTimeAverage = vmsExecutionTimeAverage;
	}

	/**
	 * @see fr.upmc.requestdispatcher.interfaces.RequestDispatcherDynamicStateI#getRequestDispatcherURI()
	 */
	@Override
	public String getRequestDispatcherURI() {
		
		return this.requestDispatcherURI;
	}

	/**
	 * @see fr.upmc.requestdispatcher.interfaces.RequestDispatcherDynamicStateI#getExecutionTimeAverageVMs()
	 */
	@Override
	public Map<String, Long> getExecutionTimeAverageVMs() {
		
		return this.vmsExecutionTimeAverage;
	}

}
