package fr.upmc.admissionController.ports;
import java.util.Map;

import fr.upmc.admissionController.AdmissionController;
import fr.upmc.admissionController.interfaces.AdmissionControllerServicesI;
import fr.upmc.admissionController.utils.AdmissionConnectionInformations;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;

/**
 * The class <code>AdmissionControllerServicesInboudPort</code> 
 * implements an inbound port offering the
 * <code>AdmissionControllerServicesI</code> interface.
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
public class AdmissionControllerServicesInboundPort 
extends		AbstractInboundPort
implements	AdmissionControllerServicesI
{
	private static final long serialVersionUID = 1L;

	public	AdmissionControllerServicesInboundPort(
		ComponentI owner
		) throws Exception
	{
		super(AdmissionControllerServicesI.class, owner) ;

		assert owner instanceof AdmissionController ;
	}

	public AdmissionControllerServicesInboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, AdmissionControllerServicesI.class, owner);

		assert owner instanceof AdmissionController ;
	}

	/* 
	 * @see fr.upmc.admissionControler.interfaces.AdmissionControllerServicesI#submitApplication(fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort, java.lang.String)
	 */
	@Override
	public String submitApplication(
			final String RgRequestNotificationInboundPortURI
			) throws Exception {
		
		final AdmissionController ac = (AdmissionController) this.owner ;
		return ac.handleRequestSync(
					new ComponentI.ComponentService<String>() {
						@Override
						public String call() throws Exception {
							return ac.submitApplication(RgRequestNotificationInboundPortURI);
							
						}
					}) ;
	}

	/* 
	 * @see fr.upmc.admissionControler.interfaces.AdmissionControllerServicesI#submitApplication(java.lang.Class, java.util.Map, fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort, java.lang.String)
	 */
	@Override
	public AdmissionConnectionInformations submitApplication(
			final Class<?> offeredInterface,
			final Map<String, String> mehtodNamesMap,
			final String RgRequestNotificationInboundPortURI
			) throws Exception {
		
		final AdmissionController ac = (AdmissionController) this.owner ;
		return ac.handleRequestSync(
					new ComponentI.ComponentService<AdmissionConnectionInformations>() {
						@Override
						public AdmissionConnectionInformations call() throws Exception {
							return ac.submitApplication(
											offeredInterface, 
											mehtodNamesMap,
											RgRequestNotificationInboundPortURI
											);
						}
					}) ;
		
	}
}

