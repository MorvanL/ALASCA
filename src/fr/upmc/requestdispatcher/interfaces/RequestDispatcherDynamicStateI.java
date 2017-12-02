package fr.upmc.requestdispatcher.interfaces;

import java.util.Map;

import fr.upmc.components.interfaces.DataOfferedI;
import fr.upmc.components.interfaces.DataRequiredI;

/**
 * The interface <code>RequestDispatcherDynamicStateI</code> implements objects
 * representing the dynamic state information of RDs transmitted
 * through the <code>RequestDispatcherDynamicStateDataI</code> interface of
 * <code>RequestDispatcher</code> components.
 *
 * <p><strong>Description</strong></p>
 * 
 * The interface is used to type objects pulled from or pushed by a RD
 * using a data interface in pull or push mode.  It gives access to dynamic
 * information, that is information subject to changes during the existence of
 * the RD.
 * 
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
public interface RequestDispatcherDynamicStateI
extends 	DataOfferedI.DataI,
			DataRequiredI.DataI
{
	/**
	 * return the requestDispatcher URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	return != null
	 * </pre>
	 *
	 * @return	the requestDispatcher URI.
	 */
	public String			getRequestDispatcherURI();
	
	
	/**
	 * return a Map containing the VMs URIs and their execution time average.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	return != null
	 * </pre>
	 *
	 * @return	a boolean 2D array where true cells indicate reserved cores.
	 */
	public Map<String, Long> getExecutionTimeAverageVMs();
}
