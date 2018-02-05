package fr.upmc.Sunrise.datacenter.software.requestdispatcher;

import java.net.InetAddress;
import java.util.Map;

import fr.upmc.Sunrise.datacenter.software.requestdispatcher.interfaces.RequestDispatcherDynamicStateI;
import fr.upmc.datacenter.TimeManagement;
import fr.upmc.datacenter.software.applicationvm.interfaces.ApplicationVMDynamicStateI;

/**
 * The class <code>RequestDispatcherDynamicState</code> implements objects representing
 * a snapshot of the dynamic state of a dispatcher component to be pulled or
 * pushed through the dynamic state data interface.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		timestamp >= 0 && timestamperIP != null
 * invariant		rdURI != null
 * invariant		nbRequestTerminated <= nbRequestReceived
 * </pre>
 * 
 * <p>Created on : April 23, 2015</p>
 * 
 * @author	Loïc Lafontaine
 */

public class RequestDispatcherDynamicState implements RequestDispatcherDynamicStateI{

    private static final long serialVersionUID = 1L;

	/** timestamp in Unix time format, local time of the timestamper.		*/
	protected final long		timestamp ;
	/** IP of the node that did the timestamping.							*/
	protected final String		timestamperIP ;
	/** URI of the dispatcher to which this dynamic state relates.			*/
	protected final String		rdURI ;
    /** the average request execution time */
    protected final Double executionTimeAvg;
    /** map of the average request execution time of each virtual machine */
    protected final Map<String,Double> virtualMachineExecutionAverageTime;
    /** map of the dynamic state of each virtual machine */
    protected final Map<String,ApplicationVMDynamicStateI> virtualMachineDynamicStates;
    /** number of request received by the dispatcher */
    protected final int nbRequestReceived;
    /** number of request terminated */
    protected final int nbRequestTerminated;
    
	
	public				RequestDispatcherDynamicState(
			String rdURI,
			Double averageTime,
			Map<String,Double> virtualMachineExecutionAverageTime,
			Map<String,ApplicationVMDynamicStateI> virtualMachineDynamicStates,
			int nbRequestReceived,
			int nbRequestTerminated
			) throws Exception
	{
		super() ;
		this.timestamp = TimeManagement.timeStamp() ;
		this.timestamperIP = InetAddress.getLocalHost().getHostAddress() ;
		this.rdURI = rdURI ;
		this.executionTimeAvg=averageTime;
		this.virtualMachineExecutionAverageTime=virtualMachineExecutionAverageTime;
		this.virtualMachineDynamicStates=virtualMachineDynamicStates;
		this.nbRequestReceived=nbRequestReceived;
		this.nbRequestTerminated=nbRequestTerminated;
	}
    
	@Override
	public long getTimeStamp() {
		return this.timestamp ;
	}

	@Override
	public String getTimeStamperId() {
		return new String(this.timestamperIP) ;
	}

	@Override
	public Double getAvgExecutionTime() {
		return this.executionTimeAvg;
	}

	@Override
	public String getDispatcherURI() {
		return new String(this.rdURI) ;
	}

	@Override
	public Map<String, Double> getVirtualMachineExecutionAverageTime() {
		return this.virtualMachineExecutionAverageTime;
	}

	@Override
	public Map<String, ApplicationVMDynamicStateI> getVirtualMachineDynamicStates() {
		return this.virtualMachineDynamicStates;
	}

	@Override
	public int getNbRequestReceived() {
		return this.nbRequestReceived;
	}

	@Override
	public int getNbRequestTerminated() {
		return this.nbRequestTerminated;
	}

}
