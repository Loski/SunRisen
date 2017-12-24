package fr.upmc.datacenter.software.requestdispatcher;

import java.net.InetAddress;

import fr.upmc.datacenter.TimeManagement;
import fr.upmc.datacenter.software.requestdispatcher.interfaces.RequestDispatcherDynamicStateI;

public class RequestDispatcherDynamicState implements RequestDispatcherDynamicStateI{

    private static final long serialVersionUID = 1L;

	/** timestamp in Unix time format, local time of the timestamper.		*/
	protected final long		timestamp ;
	/** IP of the node that did the timestamping.							*/
	protected final String		timestamperIP ;
	/** URI of the dispatcher to which this dynamic state relates.			*/
	protected final String		rdURI ;
    /** the average request execution time */
    protected final long executionTimeAvg;
	
	public				RequestDispatcherDynamicState(
			String rdURI,
			long executionTimeAvg
			) throws Exception
	{
		super() ;
		this.timestamp = TimeManagement.timeStamp() ;
		this.timestamperIP = InetAddress.getLocalHost().getHostAddress() ;
		this.rdURI = rdURI ;
		this.executionTimeAvg=executionTimeAvg;
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
	public double getAvgExecutionTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getDispatcherURI() {
		return new String(this.rdURI) ;
	}

}
