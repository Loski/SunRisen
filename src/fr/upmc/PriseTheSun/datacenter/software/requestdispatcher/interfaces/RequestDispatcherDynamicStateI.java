package fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces;

import java.util.Map;

import fr.upmc.components.interfaces.DataOfferedI;
import fr.upmc.components.interfaces.DataRequiredI;
import fr.upmc.datacenter.interfaces.TimeStampingI;
import fr.upmc.datacenter.software.applicationvm.interfaces.ApplicationVMDynamicStateI;

/**
 * The interface <code>RequestDispatcherDynamicStateI</code> defines the object
 * behaviour of the ones representing the dynamic state of request dispatcher
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	true
 * </pre>
 * 
 * @author Loïc Lafontaine
 * @version	$Name$ -- $Revision$ -- $Date$
 */

public interface RequestDispatcherDynamicStateI 
extends 		DataOfferedI.DataI,
				DataRequiredI.DataI,
				TimeStampingI
{
	/** 	get the URI of the dispatcher.													*/
	public String	getDispatcherURI() ;
	/** 	get the average execution time of a request										*/
	public Double getAvgExecutionTime();
	/** 	map of the average execution time of a request per virtual machine				*/
	public Map<String,Double> getVirtualMachineExecutionAverageTime();
	/** 	map of the dynamic state of each virtual machine				*/
	public Map<String,ApplicationVMDynamicStateI> getVirtualMachineDynamicStates();
	/** 	number of request received by the dispatcher				*/
	public int getNbRequestReceived();
	/** 	number of request terminated				*/
	public int getNbRequestTerminated();
}
