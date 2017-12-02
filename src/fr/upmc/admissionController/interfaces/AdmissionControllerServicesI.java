package fr.upmc.admissionController.interfaces;

import java.util.Map;

import fr.upmc.admissionController.utils.AdmissionConnectionInformations;
import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;

/**
 * The interface <code>AdmissionControllerServicesI</code> defines the services 
 * offered by <code>AdmissionController</code> components (submission of
 * application execution request).
 *
 * <p><strong>Description</strong></p>
 * 
 * TODO: Create and connect resources for application's execution.
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
public interface AdmissionControllerServicesI 
extends		OfferedI,
			RequiredI
{
	
	/**
	 * if cores are free on processors of the data center then a 
	 * <code>RequestDispatcher</code> and many <code>ApplicationVM</code>
	 *  are create and connect to execute the submitted application.
	 *
	 * @param RgRequestNotificationInboundPortURI   URI of inbound port to receive notifications from the dispatcher
	 * @return 										URI of inbound port to send request to the dispatcher
	 * @throws Exception
	 */
	public String submitApplication(
			String RgRequestNotificationInboundPortURI
			) throws Exception ;
	
	/**
	 * if cores are free on processors of the data center then a 
	 * <code>RequestDispatcher</code> and many <code>ApplicationVM</code>
	 *  are create and connect to execute the submitted application.
	 *  This method use javassist to create dynamically the specific 
	 *  elements of an application required to execute this application and 
	 *  receive requests from her. 
	 *  
	 * @param offeredInterface						Itf provided by the application for connector
	 * @param mehtodNamesMap						Map beetween methods of provided itf and required itf
	 * @param RgRequestNotificationInboundPortURI	URI of inbound port to receive notifications from the dispatcher
	 * @return 										informations to allow the connection with the data center
	 * @throws Exception
	 */
	public AdmissionConnectionInformations submitApplication(
			Class<?> offeredInterface,
			Map<String, String> mehtodNamesMap,
			String RgRequestNotificationInboundPortURI
			) throws Exception ;
}
