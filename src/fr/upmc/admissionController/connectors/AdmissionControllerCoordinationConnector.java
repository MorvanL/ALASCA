package fr.upmc.admissionController.connectors;

import fr.upmc.admissionController.interfaces.AdmissionControllerCoordinationI;
import fr.upmc.components.connectors.AbstractConnector;

/**
 * The class <code>AdmissionControllerCoordinationConnector</code> implements 
 * a connector for ports exchanging through the interface 
 * <code>AdmissionControllerCoordinationI</code>.
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
public class AdmissionControllerCoordinationConnector 
extends		AbstractConnector
implements	AdmissionControllerCoordinationI
{
	/** (non-Javadoc)
	 * @see fr.upmc.admissionController.interfaces.AdmissionControllerCoordinationI#addVmIfPossible(java.lang.String)
	 */
	@Override
	public boolean addVmIfPossible(String RequestDispatcherURI) throws Exception {
		
		return ((AdmissionControllerCoordinationI)this.offering).
				addVmIfPossible(RequestDispatcherURI);
	}
}
