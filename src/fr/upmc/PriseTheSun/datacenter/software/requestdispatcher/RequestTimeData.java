package fr.upmc.PriseTheSun.datacenter.software.requestdispatcher;

public class RequestTimeData {

	private long startedAt;
	private long endedAt;
	private String operatedBy;
	private String handledBy;
	private boolean finished;
	
	public RequestTimeData(String dispatcherURI,String vmURI)
	{
		this.handledBy=dispatcherURI;
		this.operatedBy=vmURI;
		this.finished=false;
	}
	
	public String getOperatedBy() {
		return operatedBy;
	}
	
	public void begin()
	{
		this.startedAt=System.currentTimeMillis();
	}
	
	public long getDuration()
	{
		return endedAt - startedAt;
	}

	public String getHandledBy() {
		return handledBy;
	}
	
	public void terminate()
	{
		this.endedAt=System.currentTimeMillis();
		this.finished=true;
	}
	
	public boolean isFinished()
	{
		return this.finished;
	}
}
