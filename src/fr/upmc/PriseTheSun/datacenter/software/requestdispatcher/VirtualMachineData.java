package fr.upmc.PriseTheSun.datacenter.software.requestdispatcher;

import java.util.ArrayList;
import java.util.List;

import fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort;

public class VirtualMachineData {

	private String vmURI;
	private RequestSubmissionOutboundPort rsobp;
	private Double averageTime;
	/**  */
	private List<RequestTimeData> requestTimeDataList;
	private int currentRequest;

	public VirtualMachineData(String uri,RequestSubmissionOutboundPort port)
	{
		this.vmURI=uri;
		this.rsobp=port;
		this.averageTime=0.0;
		this.requestTimeDataList = new ArrayList<RequestTimeData>();
		this.currentRequest = 0;
	}
	
	public String getVmURI() {
		return vmURI;
	}
	public RequestSubmissionOutboundPort getRsobp() {
		return rsobp;
	}
	public Double getAverageTime() {
		return averageTime;
	}
	
	public List<RequestTimeData> getRequestTimeDataList() {
		return this.requestTimeDataList;
	}
	
	public void resetRequestTimeDataList()
	{
		this.requestTimeDataList = new ArrayList<RequestTimeData>();
		this.currentRequest=0;
	}
	
	public void addRequest(String dispatcherURI)
	{
		this.requestTimeDataList.add(new RequestTimeData(dispatcherURI, vmURI));
		this.beginRequest();
	}
	
	private void beginRequest()
	{
		this.requestTimeDataList.get(currentRequest).begin();
	}
	
	public void endRequest()
	{
		this.requestTimeDataList.get(currentRequest).terminate();
		currentRequest++;
	}
	
	public void calculateAverageTime()
	{
		calculateAverageTime(0,this.requestTimeDataList.size()-1);
	}
	
	public void calculateAverageTime(int range)
	{
		calculateAverageTime(0,range);
	}
	
	public void calculateAverageTime(int begin, int end)
	{
		if(end-begin<=0)
			return;
			
		if(this.requestTimeDataList.size()>0)
		{
			Double res = 0.0;
			
			for(RequestTimeData timeData : this.requestTimeDataList)
			{
				if(timeData.isFinished())
				{
					res+=timeData.getDuration();
				}
			}		
			this.averageTime = res/(end-begin);
		}
		else
			this.averageTime=null;
	}
	
}
