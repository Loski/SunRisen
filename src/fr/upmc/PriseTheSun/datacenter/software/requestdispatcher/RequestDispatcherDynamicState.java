package fr.upmc.PriseTheSun.datacenter.software.requestdispatcher;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces.RequestDispatcherDynamicStateI;
import fr.upmc.datacenter.TimeManagement;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;
import fr.upmc.datacenter.software.applicationvm.interfaces.ApplicationVMDynamicStateI;


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
    
    protected final Map<String,Double> virtualMachineExecutionAverageTime;
    protected final Map<String,ApplicationVMDynamicStateI> virtualMachineDynamicStates;
	
	public				RequestDispatcherDynamicState(
			String rdURI,
			Double averageTime,
			Map<String,Double> virtualMachineExecutionAverageTime,
			Map<String,ApplicationVMDynamicStateI> virtualMachineDynamicStates
			) throws Exception
	{
		super() ;
		this.timestamp = TimeManagement.timeStamp() ;
		this.timestamperIP = InetAddress.getLocalHost().getHostAddress() ;
		this.rdURI = rdURI ;
		this.executionTimeAvg=averageTime;
		this.virtualMachineExecutionAverageTime=virtualMachineExecutionAverageTime;
		this.virtualMachineDynamicStates=virtualMachineDynamicStates;
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

}
