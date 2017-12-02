package fr.upmc.admissionController.ports;

import fr.upmc.admissionController.interfaces.AdmissionControllerCoordinationI;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;

/**
 * The class <code>AdmissionControllerCoordinationOutboundPort</code> implements 
 * an outbound port requiring the <code>AdmissionControllerCoordinationI</code> 
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
public class AdmissionControllerCoordinationOutboundPort 
extends		AbstractOutboundPort
implements	AdmissionControllerCoordinationI
{
	public				AdmissionControllerCoordinationOutboundPort(
		ComponentI owner
		) throws Exception
	{
		super(AdmissionControllerCoordinationI.class, owner) ;
	}

	public				AdmissionControllerCoordinationOutboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, AdmissionControllerCoordinationI.class, owner);
	}

	/**
	 * @see fr.upmc.admissionController.interfaces.AdmissionControllerCoordinationI#addVmIfPossible(java.lang.String)
	 */
	@Override
	public boolean addVmIfPossible(String RequestDispatcherURI) throws Exception {
		
		return ((AdmissionControllerCoordinationI)this.connector).
				addVmIfPossible(RequestDispatcherURI);
	}
}
