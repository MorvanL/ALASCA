package fr.upmc.requestdispatcher.utils;

/**
 * The class <code>RequestExecutionInformations</code> implements just
 * a container for many informations about the executions of a request.
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
public class RequestExecutionInformations {

	/** URI of request */
	private String requestURI;
	/** The time which the request dispatcher receive this request */
	private long arrivalTime;
	/** URI of Vm on which the request is executing */
	private String vmURI;
	
	/**
	 * Create a new RequestExecutionInformations
	 * 
	 * @param requestURI 	URI of request
	 * @param arrivalTime	The time which the request dispatcher receive this request
	 * @param vmURI 		URI of Vm on which the request is executing
	 * @throws Exception
	 */
	public RequestExecutionInformations( 
		String requestURI,
		long arrivalTime,
		String vmURI
		) throws Exception
	{
		this.requestURI = requestURI;
		this.arrivalTime = arrivalTime;
		this.vmURI = vmURI;
	}
	
	/**
	 * Get the arrival time of request
	 * 
	 * @return arrivalTime
	 */
	public long getArrivalTime() {
		return arrivalTime;
	}
	
	/**
	 * Get the VM URI
	 * 
	 * @return uriVM
	 */
	public String getVmUri() {
		return vmURI;
	}
	
	/**
	 * Get the Request URI
	 * 
	 * @return requestURI
	 */
	public String getRequestUri() {
		return requestURI;
	}
}
