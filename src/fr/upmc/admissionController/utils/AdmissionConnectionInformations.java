package fr.upmc.admissionController.utils;

/**
 * The class <code>AdmissionConnectionInformations</code> implements just
 * a container for many informations to allow the connection with the data center
 * for the enquire.
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
public class AdmissionConnectionInformations {

	/** Uri of the port used to send requests to the data center (used for connection) */
	private String requestSubmissionInboundPortUri;
	/** Class of the connector used to make the connection with the data center */
	private Class<?> connectorClass;
	
	public AdmissionConnectionInformations( 
			String requestSubmissionInboundPortUri,
			Class<?> connectorClass
			) throws Exception
		{
			this.requestSubmissionInboundPortUri = requestSubmissionInboundPortUri;
			this.connectorClass = connectorClass;
		}
	
		/**
		 * Get the Uri of the port used to send requests to the data center
		 * 
		 * @return requestSubmissionInboundPortUri
		 */
		public String getRequestSubmissionInboundPortUri() {
			return requestSubmissionInboundPortUri;
		}
		
		/**
		 * Get the Class of the connector used to make the connection with the data center
		 * 
		 * @return connectorClass
		 */
		public Class<?> getConnectorClass() {
			return connectorClass;
		}
}
