package fr.upmc.admissionController.connectors;

import java.util.Map;

import fr.upmc.admissionController.interfaces.AdmissionControllerServicesI;
import fr.upmc.admissionController.utils.AdmissionConnectionInformations;
import fr.upmc.components.connectors.AbstractConnector;

/**
 * The class <code>AdmissionControllerServicesConnector</code> implements 
 * a connector for ports exchanging through the interface 
 * <code>AdmissionControllerServicesI</code>.
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
public class AdmissionControllerServicesConnector
extends		AbstractConnector
implements	AdmissionControllerServicesI
{
	
	/* 
	 * @see fr.upmc.admissionControler.interfaces.AdmissionControllerServicesI#submitApplication(fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort, java.lang.String)
	 */
	@Override
	public String submitApplication(
			String RgRequestNotificationInboundPortURI
			) throws Exception {
		
		return ((AdmissionControllerServicesI)this.offering).
				submitApplication(RgRequestNotificationInboundPortURI);
	}

	/*
	 * @see fr.upmc.admissionControler.interfaces.AdmissionControllerServicesI#submitApplication(java.lang.Class, java.util.Map, fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort, java.lang.String)
	 */
	@Override
	public AdmissionConnectionInformations submitApplication(
			Class<?> offeredInterface,
			Map<String, String> mehtodNamesMap,
			String RgRequestNotificationInboundPortURI
			) throws Exception {
		
		return ((AdmissionControllerServicesI)this.offering).
				submitApplication(
						offeredInterface, 
						mehtodNamesMap,
						RgRequestNotificationInboundPortURI
						);
	}
}
