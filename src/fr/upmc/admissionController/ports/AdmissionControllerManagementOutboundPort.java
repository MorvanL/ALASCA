package fr.upmc.admissionController.ports;

import fr.upmc.admissionController.interfaces.AdmissionControllerManagementI;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;

/**
 * The class <code>AdmissionControllerManagementOutboundPort</code> implements the
 * outbound port through which one calls the component management methods.
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
public class AdmissionControllerManagementOutboundPort
extends		AbstractOutboundPort
implements	AdmissionControllerManagementI
{

	public				AdmissionControllerManagementOutboundPort(
		ComponentI owner
		) throws Exception
	{
		super(AdmissionControllerManagementI.class, owner) ;

		assert	owner != null ;
	}

	public				AdmissionControllerManagementOutboundPort(
		String uri,
		ComponentI owner
		) throws Exception
	{
		super(uri, AdmissionControllerManagementI.class, owner) ;

		assert	uri != null && owner != null ;
	}
	

	/**
	 * @see fr.upmc.admissionController.interfaces.AdmissionControllerManagementI#connectComputer(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void connectComputer(
			String ComputerURI, 
			String ComputerServicesInboundPortURI,
			String ComputerStaticStateDataInboundPortURI, 
			String ComputerDynamicStateDataInboundPortURI)
			throws Exception {
		
		((AdmissionControllerManagementI)this.connector).
			connectComputer(ComputerURI, 
							ComputerServicesInboundPortURI,
							ComputerStaticStateDataInboundPortURI, 
							ComputerDynamicStateDataInboundPortURI
							 );
		
	}
}
