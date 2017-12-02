package fr.upmc.admissionController.ports;

import fr.upmc.admissionController.AdmissionController;
import fr.upmc.admissionController.interfaces.AdmissionControllerCoordinationI;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;

/**
 * The class <code>AdmissionControllerCoordinationInboundPort</code> 
 * implements an inbound port offering the
 * <code>AdmissionControllerCoordinationI</code> interface.
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
public class AdmissionControllerCoordinationInboundPort 
extends		AbstractInboundPort
implements	AdmissionControllerCoordinationI
{

	private static final long serialVersionUID = 1L;
	
	public	AdmissionControllerCoordinationInboundPort(
		ComponentI owner
		) throws Exception
	{
		super(AdmissionControllerCoordinationI.class, owner) ;

		assert owner instanceof AdmissionController ;
	}

	public AdmissionControllerCoordinationInboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, AdmissionControllerCoordinationI.class, owner);

		assert owner instanceof AdmissionController ;
	}

	/**
	 * @see fr.upmc.admissionController.interfaces.AdmissionControllerCoordinationI#addVmIfPossible(java.lang.String)
	 */
	@Override
	public boolean addVmIfPossible(final String RequestDispatcherURI) throws Exception {
		final AdmissionController ac = (AdmissionController) this.owner ;
		return ac.handleRequestSync(
					new ComponentI.ComponentService<Boolean>() {
						@Override
						public Boolean call() throws Exception {
							return ac.addVmIfPossible(RequestDispatcherURI);
							
						}
					}) ;
	}
}
