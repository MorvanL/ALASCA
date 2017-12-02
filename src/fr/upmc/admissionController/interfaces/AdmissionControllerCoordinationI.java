package fr.upmc.admissionController.interfaces;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;

/**
 * The interface <code>AdmissionControllerCoordinationServicesI</code> defines the services 
 * offered by <code>AdmissionController</code> components to coordinate the creation of VMs.
 *
 * <p><strong>Description</strong></p>
 * 
 * TODO: Create and connect new VM's.
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
public interface AdmissionControllerCoordinationI 
extends		OfferedI,
			RequiredI
{
	
	/**
	 * if cores are free on processors of the data center then a 
	 * <code>ApplicationVM</code> is create.
	 *  
	 * @param RequestDispatcherURI		 the URI of the dispatcher on which connect the new VM
 	 * @return 							 True if a VM was created, false otherwise
	 * @throws Exception
	 */
	public boolean addVmIfPossible(
			String RequestDispatcherURI
			) throws Exception ;
	
	//public boolean removeVM
	
}
