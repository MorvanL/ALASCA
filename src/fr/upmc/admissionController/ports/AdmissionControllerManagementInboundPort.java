package fr.upmc.admissionController.ports;

import fr.upmc.admissionController.AdmissionController;
import fr.upmc.admissionController.interfaces.AdmissionControllerManagementI;
import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;

/**
 * The class <code>AdmissionControllerManagementInboundPort</code> implements the
 * inbound port through which the component management methods are called.
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
public class AdmissionControllerManagementInboundPort
extends		AbstractInboundPort
implements	AdmissionControllerManagementI
{
	
	private static final long serialVersionUID = 1L;

	public				AdmissionControllerManagementInboundPort(
			ComponentI owner
			) throws Exception
		{
			super(AdmissionControllerManagementI.class, owner) ;

			assert	owner != null && owner instanceof AdmissionController ;
		}

		public				AdmissionControllerManagementInboundPort(
			String uri,
			ComponentI owner
			) throws Exception
		{
			super(uri, AdmissionControllerManagementI.class, owner);

			assert	owner != null && owner instanceof AdmissionController ;
		}

		
		/**
		 * @see fr.upmc.admissionController.interfaces.AdmissionControllerManagementI#connectComputer(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		public void connectComputer(
				final String ComputerURI, 
				final String ComputerServicesInboundPortURI,
				final String ComputerStaticStateDataInboundPortURI, 
				final String ComputerDynamicStateDataInboundPortURI)
				throws Exception {
			
			final AdmissionController ac = (AdmissionController) this.owner ;
			this.owner.handleRequestAsync(
						new ComponentI.ComponentService<Void>() {
							@Override
							public Void call() throws Exception {
								ac.connectComputer(
										ComputerURI, 
										ComputerServicesInboundPortURI,
										ComputerStaticStateDataInboundPortURI, 
										ComputerDynamicStateDataInboundPortURI
										);
								
								return null;
							}
						}) ;
			
		}
}
