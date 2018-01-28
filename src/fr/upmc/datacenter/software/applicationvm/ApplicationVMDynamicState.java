package fr.upmc.datacenter.software.applicationvm;

import java.net.InetAddress;
import fr.upmc.datacenter.TimeManagement;
import fr.upmc.datacenter.software.applicationvm.interfaces.ApplicationVMDynamicStateI;

public class				ApplicationVMDynamicState
implements	ApplicationVMDynamicStateI
{
	// ------------------------------------------------------------------------
	// Instance variables and constants
	// ------------------------------------------------------------------------

	private static final long	serialVersionUID = 1L ;
	/** timestamp in Unix time format, local time of the timestamper.		*/
	protected final long		timestamp ;
	/** IP of the node that did the timestamping.							*/
	protected final String		timestamperIP ;
	/** URI of the dispatcher to which this dynamic state relates.			*/
	protected final String		vmURI ;
	
	protected final boolean idle;
	protected final String processorURI;
	protected final int[] allocatedCoresNumber;
	protected final int numberOfRequestInQueue;
	

	public ApplicationVMDynamicState(
			String vmURI,
			boolean idle,
			String processorURI,
			int[] allocatedCoresNumber,
			int numberOfRequestInQueue
	)throws Exception
	{
		super() ;
		this.timestamp = TimeManagement.timeStamp() ;
		this.timestamperIP = InetAddress.getLocalHost().getHostAddress();
		this.vmURI=vmURI;
		this.processorURI=processorURI;
		this.idle=idle;
		this.allocatedCoresNumber = allocatedCoresNumber;
		this.numberOfRequestInQueue=numberOfRequestInQueue;
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
	public String getApplicationVMURI() {
		return this.vmURI;
	}

	@Override
	public boolean isIdle() {
		return this.idle;
	}

	@Override
	public String getProcessorURI() {
		return this.processorURI;
	}

	@Override
	public int[] getAllocatedCoresNumber() {
		return this.allocatedCoresNumber;
	}

	@Override
	public int getNumberOfRequestInQueue() {
		return this.numberOfRequestInQueue;
	}


}
