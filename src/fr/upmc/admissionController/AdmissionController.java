package fr.upmc.admissionController;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import fr.upmc.admissionController.interfaces.AdmissionControllerCoordinationI;
import fr.upmc.admissionController.interfaces.AdmissionControllerManagementI;
import fr.upmc.admissionController.interfaces.AdmissionControllerServicesI;
import fr.upmc.admissionController.ports.AdmissionControllerCoordinationInboundPort;
import fr.upmc.admissionController.ports.AdmissionControllerManagementInboundPort;
import fr.upmc.admissionController.ports.AdmissionControllerServicesInboundPort;
import fr.upmc.admissionController.utils.AdmissionConnectionInformations;
import fr.upmc.autonomicController.AutonomicController;
import fr.upmc.autonomicController.connectors.AutonomicControllerManagementConnector;
import fr.upmc.autonomicController.ports.AutonomicControllerManagementOutboundPort;
import fr.upmc.components.AbstractComponent;
import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.components.connectors.DataConnector;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.datacenter.connectors.ControlledDataConnector;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;
import fr.upmc.datacenter.hardware.computers.connectors.ComputerServicesConnector;
import fr.upmc.datacenter.hardware.computers.interfaces.ComputerDynamicStateI;
import fr.upmc.datacenter.hardware.computers.interfaces.ComputerStateDataConsumerI;
import fr.upmc.datacenter.hardware.computers.interfaces.ComputerStaticStateI;
import fr.upmc.datacenter.hardware.computers.ports.ComputerDynamicStateDataOutboundPort;
import fr.upmc.datacenter.hardware.computers.ports.ComputerServicesOutboundPort;
import fr.upmc.datacenter.hardware.computers.ports.ComputerStaticStateDataOutboundPort;
import fr.upmc.datacenter.software.applicationvm.ApplicationVM;
import fr.upmc.datacenter.software.applicationvm.connectors.ApplicationVMManagementConnector;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.datacenter.software.connectors.RequestNotificationConnector;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionI;
import fr.upmc.requestdispatcher.RequestDispatcher;
import fr.upmc.requestdispatcher.connectors.RequestDispatcherManagementConnector;
import fr.upmc.requestdispatcher.ports.RequestDispatcherDynamicStateDataOutboundPort;
import fr.upmc.requestdispatcher.ports.RequestDispatcherManagementOutboundPort;

/**
 * The class <code>AdmissionController</code> implements a component that manage
 * applications admissions for the data center and allocate necessary resources.
 *
 * <p><strong>Description</strong></p>
 * 
 * The Admission Controller (AC) component simulates the execution of a data center
 * by receiving requests from an external application to execute her on the data center.
 * If resources are free the Admission Controller creates all necessary components (RD, 
 *  AVM, ...) and allocates resources (computer's cores) for this application.
 * 
 * As a component, the Admission Controller offers a request submission service through 
 * the interface <code>AdmissionControllerServicesI</code> implemented by
 * <code>AdmissionControllerServicesInboundPort</code> inbound port.
 * 
 * the Admission Controller can receive state of computers through the implementation 
 * of the interface <code>ComputerStateDataConsumerI</code>.
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

public class AdmissionController
extends		AbstractComponent
implements	AdmissionControllerServicesI,
			AdmissionControllerManagementI,
			AdmissionControllerCoordinationI,
			ComputerStateDataConsumerI
{

	// ------------------------------------------------------------------------
	// Component internal state
	// ------------------------------------------------------------------------
	
	/** URI of this controller.																	*/
	protected String					AdmissionControllerURI ;
	/** Inbound port offering the services interface.											*/
	protected AdmissionControllerServicesInboundPort acsip ;
	/** Inbound port offering the management interface.											*/
	protected AdmissionControllerManagementInboundPort acmip ;
	/** Inbound port offering the services interface.											*/
	protected AdmissionControllerCoordinationInboundPort accip ;
	
	// for Computers
	public static final String	ComputerServicesOutboundPortURIPrefix = "cs-obp" ;
	public static final String	ComputerStaticStateDataOutboundPortURIPrefix = "css-dop-" ;
	public static final String	ComputerDynamicStateDataOutboundPortURIPrefix = "cds-dop" ;
	/** Map between Computers URIs and the output ports used to access 
	 * to the services provider of each Computer.												*/
	protected Map<String,ComputerServicesOutboundPort> csPorts;
	/** Map between Computers URIs and the output ports used to access 
	 * to the static state data of each Computer.												*/
	protected Map<String,ComputerStaticStateDataOutboundPort> cssPorts;
	/** Map between Computers URIs and the output ports used to access 
	 * to the dynamic state data of each Computer.												*/
	protected Map<String,ComputerDynamicStateDataOutboundPort> cdsPorts;
	/** Number of Computers used on data center (use for create different URIs for ports)		*/
	int nbComputers;
	/** Map between Computers URIs and the states of cores of these processors (free or not).	*/
	protected Map<String,boolean[][]> 		reservedCores;
	
	// for VMs
	public static final String 	VmURIPrefix = "vm-";
	public static final String	ApplicationVMManagementInboundPortURIPrefix = "avm-ibp" ;
	public static final String	ApplicationVMManagementOutboundPortURIPrefix = "avm-obp" ;
	public static final String	VmRequestSubmissionInboundPortURIPrefix = "vm-rsibp" ;
	public static final String	VmRequestNotificationOutboundPortURIPrefix = "vm-rnobp" ;
	/** list of AVMs's ports used to manage them.												*/
	protected List<ApplicationVMManagementOutboundPort>			avmPorts;
	/** list of AVMs created in data center.													*/
	protected List<ApplicationVM> vms;
	/** Number of AVMs used on data center (use for create different URIs for ports)			*/
	int nbVms;
	
	// for Request Dispatchers
	public static final String 	RdURIPrefix = "rd-";
	public static final String	RdManagementInboundPortURIPrefix = "rdm-ibp" ;
	public static final String	RdManagementOutboundPortURIPrefix = "rdm-obp" ;
	public static final String	RdRequestSubmissionInboundPortURIPrefix = "rd-rsibp" ;
	public static final String	RdRequestSubmissionOutboundPortURIPrefix = "rd-rsobp" ;
	public static final String	RdRequestNotificationInboundPortURIPrefix = "rd-rnibp" ;
	public static final String	RdRequestNotificationOutboundPortURIPrefix = "rd-rnobp" ;
	public static final String  RdDynamicStateDataInboundPortURIPrefix = "rdds-dip";
	/** list of RDs's ports used to manage them.												*/
	protected Map<String,RequestDispatcherManagementOutboundPort>		rdmPorts;	
	/** Map between RDs URIs and the output ports used to access 
	 * to the dynamic state data of each RD.													*/
	protected Map<String,RequestDispatcherDynamicStateDataOutboundPort> rddsPorts;
	/** list of RDs created in data center.														*/
	protected List<RequestDispatcher> rds;
	/** Number of RDs used on data center (use for create different URIs for ports)				*/
	int nbRds;
	
	// for Autonomic Controllers
	public static final String 	AcURIPrefix = "ac-";
	public static final String	AcManagementInboundPortURIPrefix = "acm-ibp" ;
	public static final String	AcManagementOutboundPortURIPrefix = "acm-obp" ;
	/** list of ACs's ports used to manage them.												*/
	protected List<AutonomicControllerManagementOutboundPort>		acmPorts;	
	/** list of ACs created in data center.														*/
	protected List<AutonomicController> acs;
	/** Number of ACs used on data center (use for create different URIs for ports)				*/
	int nbAcs;
	
	/**
	 * create a new admission controller with the given URI, and the URIs 
	 * to be used to create and publish its inbound and outbound ports.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * TODO: complete!
	 * 
	 * <pre>
	 * pre	AdmissionControllerURI != null
	 * pre	ServicesInboundPortURI != null
	 * pre  ManagementInboundPortURI != null
	 * pre  CoordinationInboundPortURI != null
	 * post	true			// no postcondition.
	 * </pre>
	 * 
	 * @param AdmissionControllerURI 			  URI of the newly created AC.
	 * @param ServicesInboundPortURI              URI of the AC services inbound port.
	 * @param ManagementInboundPortURI			  URI of the AC management inbound port.
	 * @param CoordinationInboundPortURI		  URI of the AC coordination inbound port.
	 * @throws Exception
	 */
	public				AdmissionController(
			String AdmissionControllerURI,
			String ServicesInboundPortURI,
			String ManagementInboundPortURI,
			String CoordinationInboundPortURI
			) throws Exception
	{
		// The normal thread pool is used to process component services, while
		// the scheduled one is used to schedule the pushes of dynamic state
		// when requested.
		super(1, 1) ;

		// Preconditions
		assert AdmissionControllerURI != null ;
		assert ServicesInboundPortURI != null ;
		assert ManagementInboundPortURI != null ;
		assert CoordinationInboundPortURI != null;

		
		this.AdmissionControllerURI = AdmissionControllerURI ;
		this.nbRds = 0;
		this.nbAcs = 0;
		this.nbVms = 0;
		this.nbComputers = 0;

		// Interfaces and ports
		this.addOfferedInterface(AdmissionControllerServicesI.class) ;
		this.acsip = new AdmissionControllerServicesInboundPort(
										ServicesInboundPortURI, this) ;
		this.addPort(this.acsip) ;
		this.acsip.publishPort() ;
		
		this.addOfferedInterface(AdmissionControllerManagementI.class) ;
		this.acmip = new AdmissionControllerManagementInboundPort(
										ManagementInboundPortURI, this) ;
		this.addPort(this.acmip) ;
		this.acmip.publishPort() ;
		
		this.addOfferedInterface(AdmissionControllerCoordinationI.class) ;
		this.accip = new AdmissionControllerCoordinationInboundPort(
										CoordinationInboundPortURI, this) ;
		this.addPort(this.accip) ;
		this.accip.publishPort() ;
		

		this.csPorts = new HashMap<String,ComputerServicesOutboundPort>() ;
		this.cssPorts = new HashMap<String,ComputerStaticStateDataOutboundPort>() ;
		this.cdsPorts = new HashMap<String,ComputerDynamicStateDataOutboundPort>() ;
		this.reservedCores = new HashMap<String, boolean[][]>() ;
		
		this.vms = new ArrayList<ApplicationVM>();
		this.avmPorts = new ArrayList<ApplicationVMManagementOutboundPort>();
		
		this.rds = new ArrayList<RequestDispatcher>();
		this.rdmPorts = new HashMap<String,RequestDispatcherManagementOutboundPort>() ;
		this.rddsPorts = new HashMap<String,RequestDispatcherDynamicStateDataOutboundPort>() ;
		
		this.acs = new ArrayList<AutonomicController>();
		this.acmPorts = new ArrayList<AutonomicControllerManagementOutboundPort>();
	}

	// ------------------------------------------------------------------------
	// Component life-cycle
	// ------------------------------------------------------------------------

	/**
	 * @see fr.upmc.components.AbstractComponent#shutdown()
	 */
	@Override
	public void			shutdown() throws ComponentShutdownException
	{
		try {
			for (Entry<String, ComputerServicesOutboundPort> entry :  csPorts.entrySet()) {
				ComputerServicesOutboundPort csop = entry.getValue();
				if (csop.connected()) {
					csop.doDisconnection() ;
				}				
			}
			
			for (Entry<String, RequestDispatcherManagementOutboundPort> entry :  rdmPorts.entrySet()) {
				RequestDispatcherManagementOutboundPort rdmPort = entry.getValue();
				if (rdmPort.connected()) {
					rdmPort.doDisconnection() ;
				}				
			}
			
			for(int i = 0 ; i < this.vms.size() ; i++){
				if (this.avmPorts.get(i).connected()) {
					this.avmPorts.get(i).doDisconnection() ;
				}
				if (this.vms.get(i).isStarted())
					this.vms.get(i).shutdown();
			}
			
			for(int i = 0 ; i < this.rds.size() ; i++){
				if (this.rds.get(i).isStarted())
					this.rds.get(i).shutdown();
			}
			for(int i = 0 ; i < this.acs.size() ; i++){
				if (this.acmPorts.get(i).connected()) {
					this.acmPorts.get(i).doDisconnection() ;
				}
				if (this.acs.get(i).isStarted())
					this.acs.get(i).shutdown();
			}
			
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		
		super.shutdown();
	}

	// ------------------------------------------------------------------------
	// Component services
	// ------------------------------------------------------------------------

	/* 
	 * @see fr.upmc.admissionControler.interfaces.AdmissionControllerServicesI#submitApplication(fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort, java.lang.String)
	 */
	@Override
	public String submitApplication(
			String RgRequestNotificationInboundPortURI
			) throws Exception {
		
		this.logMessage("Application submission");
		
		List<String> computersOk = checkFreeCores(); 
		
		if(computersOk.size() > 0){
			// Create RD
			this.logMessage("Create Request Dispatcher "+RdURIPrefix + this.nbRds);
			createRequestDispatcherAndConnect(RgRequestNotificationInboundPortURI);
			// Create VM
			for (int i = 0 ; i < computersOk.size() ; i++){
				this.logMessage("Create VM "+ VmURIPrefix + this.nbVms);
				ApplicationVM vm = createVM();
				ConnectVM(this.rdmPorts.get(RdURIPrefix + (this.nbRds-1)), vm);
				AllocatedCore[] acs = this.csPorts.get(computersOk.get(i)).allocateCores(2) ;
				this.vms.get(this.nbVms-1).allocateCores(acs);
			}
//			// Create AC
//			this.logMessage("Create Autonomic Controller "+AcURIPrefix + this.nbAcs);
//			createAutonomicControllerAndConnect();
		}
		else{
			this.logMessage("Submission refused");
			return null;
		}
		
		return (RdRequestSubmissionInboundPortURIPrefix + (this.nbRds - 1));
	}
	
	
	/* 
	 * @see fr.upmc.admissionControler.interfaces.AdmissionControllerServicesI#submitApplication(java.lang.Class, java.util.Map, fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort, java.lang.String)
	 */
	@Override
	public AdmissionConnectionInformations submitApplication(
			Class<?> offeredInterface,
			Map<String,String> methodNamesMap,
			String RgRequestNotificationInboundPortURI
			) throws Exception {
				
		this.logMessage("Application submission");
		
		List<String> computersOk = checkFreeCores();
		
		if(computersOk.size() > 0){
			this.logMessage("Create Request Dispatcher "+RdURIPrefix + this.nbRds);
			createRequestDispatcherAndConnect(RgRequestNotificationInboundPortURI);
			
			// Create connector between dispatcher and VM
			Class<?> connectorClass = 
					this.makeConnectorClassJavassist(
							"fr.upmc.admissionControler.admissionController.GenerateConnectorDToVM" + (this.nbRds-1), 
							AbstractConnector.class, 
							RequestSubmissionI.class, 
							RequestSubmissionI.class, 
							(HashMap<String, String>) methodNamesMap);
			
			for (int i = 0 ; i < computersOk.size() ; i++){
				this.logMessage("Create VM "+ VmURIPrefix + this.nbVms);
				ApplicationVM vm = createVM();
				ConnectVM(this.rdmPorts.get(RdURIPrefix + (this.nbRds-1)), connectorClass, vm);
				AllocatedCore[] acs = this.csPorts.get(computersOk.get(i)).allocateCores(2) ;
				this.vms.get(this.nbVms-1).allocateCores(acs);
			}
//			// Create AC
//			this.logMessage("Create Autonomic Controller "+AcURIPrefix + this.nbAcs);
//			createAutonomicControllerAndConnect();
		}
		else{
			this.logMessage("Submission refused");
			return null;
		}
		
		// Create connector between requests generator and dispatcher
		Class<?> connectorClass = 
				this.makeConnectorClassJavassist(
						"fr.upmc.admissionControler.admissionController.GenerateConnectorGtoD" + (this.nbRds-1), 
						AbstractConnector.class, 
						RequestSubmissionI.class, 
						RequestSubmissionI.class, 
						(HashMap<String, String>) methodNamesMap);
		
		AdmissionConnectionInformations aci = 
								new AdmissionConnectionInformations(
										RdRequestSubmissionInboundPortURIPrefix + (this.nbRds - 1), 
										connectorClass);
		return (aci);
	}
	
	/**
	 * @see fr.upmc.admissionController.interfaces.AdmissionControllerManagementI#connectComputer(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public void connectComputer (String ComputerURI,
								String ComputerServicesInboundPortURI,
								String ComputerStaticStateDataInboundPortURI,
								String ComputerDynamicStateDataInboundPortURI) throws Exception{
		
		this.csPorts.put(ComputerURI, 
				new ComputerServicesOutboundPort(
						ComputerServicesOutboundPortURIPrefix + this.nbComputers,
						new AbstractComponent(0, 0) {}
				)) ;
		this.csPorts.get(ComputerURI).publishPort() ;
		this.csPorts.get(ComputerURI).doConnection(
						ComputerServicesInboundPortURI,
						ComputerServicesConnector.class.getCanonicalName()) ;
		
		this.cssPorts.put(ComputerURI, 
					new ComputerStaticStateDataOutboundPort(
						ComputerStaticStateDataOutboundPortURIPrefix + this.nbComputers,
						this,
						ComputerURI
					)) ;
		this.cssPorts.get(ComputerURI).publishPort() ;
		this.cssPorts.get(ComputerURI).doConnection(
				ComputerStaticStateDataInboundPortURI,
				DataConnector.class.getCanonicalName()) ;
		
		this.cdsPorts.put(ComputerURI, 
				new ComputerDynamicStateDataOutboundPort(
					ComputerDynamicStateDataOutboundPortURIPrefix + this.nbComputers,
					this,
					ComputerURI
				)) ;
		this.cdsPorts.get(ComputerURI).publishPort() ;
		this.cdsPorts.get(ComputerURI).
		doConnection(
			ComputerDynamicStateDataInboundPortURI,
			ControlledDataConnector.class.getCanonicalName()) ;
		
		this.cdsPorts.get(ComputerURI).startUnlimitedPushing(500);
	
		this.nbComputers++;
	}
	
	/* 
	 * @see fr.upmc.datacenter.hardware.computers.interfaces.ComputerStateDataConsumerI#acceptComputerStaticData(java.lang.String, fr.upmc.datacenter.hardware.computers.interfaces.ComputerStaticStateI)
	 */
	@Override
	public void acceptComputerStaticData(String computerURI,
			ComputerStaticStateI staticState) throws Exception {
		// TODO Auto-generated method stub
		
	}

	/* 
	 * @see fr.upmc.datacenter.hardware.computers.interfaces.ComputerStateDataConsumerI#acceptComputerDynamicData(java.lang.String, fr.upmc.datacenter.hardware.computers.interfaces.ComputerDynamicStateI)
	 */
	@Override
	public void acceptComputerDynamicData(String computerURI,
		ComputerDynamicStateI cds) throws Exception {

		this.reservedCores.put(cds.getComputerURI(), cds.getCurrentCoreReservations());
	}
	
	// ------------------------------------------------------------------------
	// Component coordination services
	// ------------------------------------------------------------------------
	
	/**
	 * @see fr.upmc.admissionController.interfaces.AdmissionControllerCoordinationI#addVmIfPossible(java.lang.String)
	 */
	@Override
	public boolean addVmIfPossible(String RequestDispatcherURI) throws Exception {
		
		this.logMessage("Try add VM for dispatcher " + RequestDispatcherURI);
		
		List<String> computersOk = checkFreeCores(); 
		
		if(computersOk.size() > 0){
			this.logMessage("Create VM "+ VmURIPrefix + this.nbVms + " for dispatcher " + RequestDispatcherURI);
			ApplicationVM vm = createVM();
			ConnectVM(this.rdmPorts.get(RequestDispatcherURI), vm);
			AllocatedCore[] acs = this.csPorts.get(computersOk.get(0)).allocateCores(2) ;
			this.vms.get(this.nbVms-1).allocateCores(acs);
			
			return true;
		}
		this.logMessage("Impossible to create new VM");
		return false;
	}	
	
	// ------------------------------------------------------------------------
	// Component internal services
	// ------------------------------------------------------------------------
	
	/**
	 * Check computers's cores state to know if it's possible to create a new VM.
	 * 
	 * @return	List of computer's URI on which a VM can be created (max 2 computers).
	 * @throws  Exception
	 */
	private List<String> checkFreeCores() throws Exception{
		// Instantiate application on 2 VMs if possible 
		// else 1 VM
		// else refuse submission
		// VM is create with 2 cores
		List<String> computersOk = new ArrayList<String>();
		Iterator<Entry<String, boolean[][]>> entries = this.reservedCores.entrySet().iterator();
		// Check if computers (max 2) have resources to create a VM
		while (entries.hasNext()  &&  computersOk.size() < 2){
			// for each computer 
			Entry<String, boolean[][]> thisEntry = entries.next();
			boolean[][] cores = (boolean[][]) thisEntry.getValue(); 
			int nbCoresFree = 0;
			// count how many cores are free on this computer
			for (int p = 0 ; p < cores.length ; p++) {
				for (int c = 0 ; c < cores[p].length ; c++) {
					if (!cores[p][c]) {
						nbCoresFree++;
					} 
				}
			}
			// if it's possible, add this computer to the list to create one or two VM on him. 
			while (nbCoresFree >= 2  &&  computersOk.size() < 2){
				nbCoresFree = nbCoresFree - 2;
				computersOk.add(thisEntry.getKey());
			}
		}
		
		return computersOk;
	}
	
	/**
	 * Create a VM 
	 * 
	 * @throws Exception
	 */
	private ApplicationVM createVM () throws Exception {
		// --------------------------------------------------------------------
		// Create an Application VM component
		// --------------------------------------------------------------------
		ApplicationVM vm = 
				new ApplicationVM(VmURIPrefix + this.nbVms,	// application vm component URI
						  ApplicationVMManagementInboundPortURIPrefix + this.nbVms,
						  VmRequestSubmissionInboundPortURIPrefix + this.nbVms,
						  VmRequestNotificationOutboundPortURIPrefix + this.nbVms);
		this.vms.add(vm) ;

		// Create a mock up port to manage the AVM component (allocate cores).
		ApplicationVMManagementOutboundPort avmPort = 
				new ApplicationVMManagementOutboundPort(
						ApplicationVMManagementOutboundPortURIPrefix + this.nbVms,
						new AbstractComponent(0, 0) {});
		this.avmPorts.add(avmPort) ;
		avmPort.publishPort() ;
		avmPort.doConnection(
					ApplicationVMManagementInboundPortURIPrefix + this.nbVms,
					ApplicationVMManagementConnector.class.getCanonicalName()) ;

		// Toggle on tracing and logging in the application virtual machine to
		// follow the execution of individual requests.
		vm.toggleTracing() ;
		vm.toggleLogging() ;
		
		this.nbVms ++;
		
		return vm;
	}
	
	/**
	 * Connect VM to the dispatcher.
	 * 
	 * @param rdmop						RD management outbound port
	 * @param vm						VM to connect 
	 * @throws Exception
	 */
	private void ConnectVM (
			RequestDispatcherManagementOutboundPort rdmop,
			ApplicationVM vm
			) throws Exception {
		
		rdmop.addRequestReceiver(VmURIPrefix + (this.nbVms - 1),
				VmRequestSubmissionInboundPortURIPrefix + (this.nbVms - 1));
		vm.doPortConnection(
				VmRequestNotificationOutboundPortURIPrefix + (this.nbVms - 1),
				RdRequestNotificationInboundPortURIPrefix + (this.nbRds - 1),
				RequestNotificationConnector.class.getCanonicalName()) ;
	}
	
	/**
	 * Connect VM to the dispatcher.
	 * 
	 * @param rdmop								RD management outbound port
	 * @param connectorClass					Class of connector between RD and VM
	 * @param vm 								VM to connect
	 * @throws Exception
	 */
	private void ConnectVM (
				RequestDispatcherManagementOutboundPort rdmop,
				Class<?> connectorClass,
				ApplicationVM vm
				) throws Exception {
		
		rdmop.addRequestReceiver(VmURIPrefix + (this.nbVms - 1),
				VmRequestSubmissionInboundPortURIPrefix + (this.nbVms - 1),
				connectorClass);
		vm.doPortConnection(
				VmRequestNotificationOutboundPortURIPrefix + (this.nbVms - 1),
				RdRequestNotificationInboundPortURIPrefix + (this.nbRds - 1),
				RequestNotificationConnector.class.getCanonicalName()) ;
	}
	
	/**
	 * Create a Dispatcher and connect him with a request generator
	 * 
	 * @param rgrsobp								RG request submission outbound port
	 * @param RgRequestNotificationInboundPortURI	RG request notification inbound port
	 * @throws Exception
	 */
	private void createRequestDispatcherAndConnect (
							String RgRequestNotificationInboundPortURI
							) throws Exception {
		// --------------------------------------------------------------------
		// Create an Request Dispatcher component
		// --------------------------------------------------------------------
		RequestDispatcher rd = 
				new RequestDispatcher(RdURIPrefix + this.nbRds, // application rd component URI,
					  RdManagementInboundPortURIPrefix + this.nbRds,
					  RdRequestSubmissionInboundPortURIPrefix + this.nbRds,
					  RdRequestNotificationOutboundPortURIPrefix + this.nbRds,
					  RdRequestNotificationInboundPortURIPrefix + this.nbRds,
					  RdDynamicStateDataInboundPortURIPrefix + this.nbRds);
		this.rds.add(rd);
		
		RequestDispatcherManagementOutboundPort rdmPort =
				new RequestDispatcherManagementOutboundPort(
						RdManagementOutboundPortURIPrefix + this.nbRds,
						new AbstractComponent(0, 0) {});
		this.rdmPorts.put(RdURIPrefix + this.nbRds, rdmPort);
		rdmPort.publishPort() ;
		rdmPort.doConnection(
					RdManagementInboundPortURIPrefix + this.nbRds,
					RequestDispatcherManagementConnector.class.getCanonicalName()) ;

		rd.doPortConnection(
				RdRequestNotificationOutboundPortURIPrefix + this.nbRds,
				RgRequestNotificationInboundPortURI,
				RequestNotificationConnector.class.getCanonicalName()) ;
		
		// Toggle on tracing and logging in the application virtual machine to
		// follow the execution of individual requests.
		rd.toggleTracing() ;
		rd.toggleLogging() ;
		
		this.nbRds++;
	}
	
	/**
	 * Create an Autonomic Controller and connect him with AdmissionController
	 * and Request dispatcher.
	 * 
	 * @throws Exception
	 */
	private void createAutonomicControllerAndConnect() throws Exception{
		AutonomicController ac = 
				new AutonomicController(AcURIPrefix + this.nbAcs,
						AcManagementInboundPortURIPrefix + this.nbAcs,
						this.accip.getPortURI());
		this.acs.add(ac);
		
		AutonomicControllerManagementOutboundPort acmPort =
				new AutonomicControllerManagementOutboundPort(
						AcManagementOutboundPortURIPrefix + this.nbAcs,
						new AbstractComponent(0, 0) {});
		this.acmPorts.add(acmPort) ;
		acmPort.publishPort() ;
		acmPort.doConnection(
					AcManagementInboundPortURIPrefix + this.nbAcs,
					AutonomicControllerManagementConnector.class.getCanonicalName()) ;
		
		acmPort.connectRequestDispatcher(
				RdDynamicStateDataInboundPortURIPrefix + (this.nbRds-1),
				RdURIPrefix + (this.nbRds-1)
				);	
		acmPort.checkAndDoAdaptation();
		
		this.nbAcs++;
		
		// Toggle on tracing and logging in the application virtual machine to
		// follow the execution of individual requests.
		ac.toggleTracing() ;
		ac.toggleLogging() ;
	}
	
	
	/**
	 * Use Javassist to create a connector between inbound port and outbound port to allow
	 * requests submission : RG to RD and RD to AVMs
	 * 
	 * @param connectorCanonicalClassName				canonical name of the class of the connector
	 * @param connectorSuperclass						super class of the connector
	 * @param connectorImplementedInterface				interface implemented by the connector
	 * @param offeredInterface							interface required by the connector
	 * @param methodNamesMap							map between methods of offered and required itf
	 * @return											Class of the new connector
	 * @throws Exception
	 */
	public Class<?> makeConnectorClassJavassist(
				String connectorCanonicalClassName, 
				Class<?> connectorSuperclass,
				Class<?> connectorImplementedInterface, 
				Class<?> offeredInterface, 
				HashMap<String,String> methodNamesMap
			) throws Exception
	{
		ClassPool pool = ClassPool.getDefault() ;
		CtClass cs = pool.get(connectorSuperclass.getCanonicalName()) ;
		CtClass cii = pool.get(connectorImplementedInterface.getCanonicalName()) ;
//		CtClass oi = pool.get(offeredInterface.getCanonicalName()) ;
		CtClass connectorCtClass = pool.makeClass(connectorCanonicalClassName) ; 
		connectorCtClass.setSuperclass(cs) ;
		Method[] methodsToImplement = connectorImplementedInterface.getDeclaredMethods() ; 
		for (int i = 0 ; i < methodsToImplement.length ; i++) {
			String source = "public " ;
			source += methodsToImplement[i].getReturnType().getName() + " " ; 
			source += methodsToImplement[i].getName() + "(" ;
			Class<?>[] pt = methodsToImplement[i].getParameterTypes() ; 
			String callParam = "" ;
			for (int j = 0 ; j < pt.length ; j++) {
				String pName = "aaa" + j ;
				source += pt[j].getCanonicalName() + " " + pName ; 
				callParam += pName ;
				if (j < pt.length - 1) {
					source += ", " ;
					callParam += ", " ; 
				}
			}
			source += ")" ;
			Class<?>[] et = methodsToImplement[i].getExceptionTypes() ; 
			if (et != null && et.length > 0) {
				source += " throws " ;
				for (int z = 0 ; z < et.length ; z++) { 
					source += et[z].getCanonicalName() ; 
					if (z < et.length - 1) {
						source += "," ; 
					}
				} 
			}
			source += "\n{ return ((" ;
			source += offeredInterface.getCanonicalName() + ")this.offering)." ; 
			source += methodNamesMap.get(methodsToImplement[i].getName()) ; 
			source += "(" + callParam + ") ;\n}" ;
			CtMethod theCtMethod = CtMethod.make(source, connectorCtClass) ; 
			connectorCtClass.addMethod(theCtMethod) ;
		}
		connectorCtClass.setInterfaces(new CtClass[]{cii}) ; 
		cii.detach() ; 
		cs.detach() ; 
		Class<?> ret = connectorCtClass.toClass() ; 
		connectorCtClass.detach() ;
		return ret ;
	}
}	
	
