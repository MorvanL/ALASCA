package fr.upmc.admissionController.interfaces;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;

/**
 * The interface <code>AdmissionControllerManagementI</code> defines the management
 * actions provided by the admission controller component.
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
public interface AdmissionControllerManagementI
extends		OfferedI,
			RequiredI
{

	/**
	 * Connect a computer with the AdmissionController
	 * 
	 * @param ComputerURI							   URI of the computer we want to connect
	 * @param ComputerServicesInboundPortURI	       URI of inbound port to acces services of computer
	 * @param ComputerStaticStateDataInboundPortURI    URI of inbound port to receive static state of computer
	 * @param ComputerDynamicStateDataInboundPortURI   URI of inbound port to receive dynamic state of computer
	 * @throws Exception
	 */
	public void connectComputer (String ComputerURI,
			String ComputerServicesInboundPortURI,
			String ComputerStaticStateDataInboundPortURI,
			String ComputerDynamicStateDataInboundPortURI) throws Exception ;
}
