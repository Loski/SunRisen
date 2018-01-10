package fr.upmc.PriseTheSun.datacenter.software.requestdispatcher;

public class RequestTimeData {

	private long startedAt;
	private long endedAt;
	private String operatedBy;
	private String handledBy;
	private String request;
	private boolean finished;
	
	public RequestTimeData(String dispatcherURI,String vmURI,String requestURI)
	{
		this.handledBy=dispatcherURI;
		this.operatedBy=vmURI;
		this.finished=false;
		this.request=requestURI;
	}
	
	public String getOperatedBy() {
		return operatedBy;
	}
	
	public void begin()
	{
		this.startedAt=System.nanoTime();
	}
	
	public long getDuration()
	{
		System.err.println(this.request+""+handledBy+" "+operatedBy+" "+endedAt+" "+startedAt+" "+(endedAt - startedAt)/1000000/1000);
		
		return endedAt - startedAt;
	}

	public String getHandledBy() {
		return handledBy;
	}
	
	public void terminate()
	{
		this.endedAt=System.nanoTime();
		this.finished=true;
	}
	
	public boolean isFinished()
	{
		return this.finished;
	}
}
