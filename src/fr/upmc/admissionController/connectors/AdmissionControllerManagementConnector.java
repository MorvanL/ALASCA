package fr.upmc.admissionController.connectors;

import fr.upmc.admissionController.interfaces.AdmissionControllerManagementI;
import fr.upmc.components.connectors.AbstractConnector;

/**
 * The class <code>AdmissionControllerManagementConnector</code> implements a
 * standard client/server connector for the management admission controller
 * management interface.
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
public class AdmissionControllerManagementConnector
extends		AbstractConnector
implements	AdmissionControllerManagementI
{
	
	/**
	 * @see fr.upmc.admissionController.interfaces.AdmissionControllerManagementI#connectComputer(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public void connectComputer(
			String ComputerURI, 
			String ComputerServicesInboundPortURI,
			String ComputerStaticStateDataInboundPortURI, 
			String ComputerDynamicStateDataInboundPortURI)
			throws Exception {
		
		((AdmissionControllerManagementI)this.offering).
		connectComputer(
				ComputerURI, 
				ComputerServicesInboundPortURI,
				ComputerStaticStateDataInboundPortURI, 
				ComputerDynamicStateDataInboundPortURI
				);
		
	}
}
