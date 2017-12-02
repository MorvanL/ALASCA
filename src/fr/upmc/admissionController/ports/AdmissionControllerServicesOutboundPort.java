package fr.upmc.admissionController.ports;

import java.util.Map;

import fr.upmc.admissionController.interfaces.AdmissionControllerServicesI;
import fr.upmc.admissionController.utils.AdmissionConnectionInformations;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;

/**
 * The class <code>AdmissionControllerServiceOutboundPort</code> implements 
 * an outbound port requiring the <code>AdmissionControllerServicesI</code> 
 * interface.
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
public class AdmissionControllerServicesOutboundPort 
extends		AbstractOutboundPort
implements	AdmissionControllerServicesI
{
	public				AdmissionControllerServicesOutboundPort(
		ComponentI owner
		) throws Exception
	{
		super(AdmissionControllerServicesI.class, owner) ;
	}

	public				AdmissionControllerServicesOutboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, AdmissionControllerServicesI.class, owner);
	}

	/* 
	 * @see fr.upmc.admissionControler.interfaces.AdmissionControllerServicesI#submitApplication(fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort, java.lang.String)
	 */
	@Override
	public String submitApplication(
			String RgRequestNotificationInboundPortURI
			) throws Exception {
		
		return ((AdmissionControllerServicesI)this.connector).
				submitApplication(RgRequestNotificationInboundPortURI);
	}

	/* 
	 * @see fr.upmc.admissionControler.interfaces.AdmissionControllerServicesI#submitApplication(java.lang.Class, java.util.Map, fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort, java.lang.String)
	 */
	@Override
	public AdmissionConnectionInformations submitApplication(
			Class<?> offeredInterface,
			Map<String, String> mehtodNamesMap,
			String RgRequestNotificationInboundPortURI) throws Exception {
		
		return ((AdmissionControllerServicesI)this.connector).
				submitApplication(offeredInterface, 
								  mehtodNamesMap,
								  RgRequestNotificationInboundPortURI
								 );
	}
}

