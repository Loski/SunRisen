package fr.upmc.PriseTheSun.datacenter.software.requestdispatcher;

import fr.upmc.datacenter.TimeManagement;

/**
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	true
 * </pre>
 * 
 * @author	Loïc Lafontaine
 */

public class RequestTimeData {

	private long startedAt;
	private long endedAt;
	private String virtualMachineURI;
	private String dispatcherURI;
	private String request;
	private boolean finished;
	/** timestamp in Unix time format, local time of the timestamper. stored when the average time is calculated*/
	protected  long		timestamp ;
	
	public RequestTimeData()
	{
		this.finished=false;
		this.begin();
	}
	
	public RequestTimeData(String dispatcherURI,String vmURI,String requestURI)
	{
		this();
		this.dispatcherURI=dispatcherURI;
		this.virtualMachineURI=vmURI;
	}
	
	public void begin()
	{
		this.startedAt=System.nanoTime();
	}
	
	public void terminate()
	{
		this.endedAt=System.nanoTime();
		this.timestamp = TimeManagement.timeStamp() ;
		this.finished=true;
	}
	
	public boolean isFinished()
	{
		return this.finished;
	}
	
	public long getTimestamp()
	{
		return this.timestamp;
	}
	
	public long getDuration()
	{
		return endedAt - startedAt;
	}
	
	public String getVirtualMachineURI() {
		return virtualMachineURI;
	}
	
	public String getRequest() {
		return request;
	}
	
	public String getDispatcherURI() {
		return dispatcherURI;
	}
	
}
