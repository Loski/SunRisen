package fr.upmc.PriseTheSun.datacenter.software.requestdispatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMIntrospectionOutboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort;

public class VirtualMachineData {

	private String vmURI;
	private RequestSubmissionOutboundPort rsobp;
	private ApplicationVMIntrospectionOutboundPort avmiovp;
	private Double averageTime;
	/**  */
	private List<RequestTimeData> requestTimeDataList;
	private int currentRequest;

	public VirtualMachineData(String uri,RequestSubmissionOutboundPort rsobp,ApplicationVMIntrospectionOutboundPort avmiovp)
	{
		this.vmURI=uri;
		this.rsobp=rsobp;
		this.avmiovp=avmiovp;
		this.averageTime=null;
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
		ListIterator<RequestTimeData> iterator = this.requestTimeDataList.listIterator();
		
		while(iterator.hasNext()){
			RequestTimeData data = iterator.next();
			if(data.isFinished())
			{
				iterator.remove();
			}
		}
		
		this.currentRequest=0;
		this.averageTime=null;
	}
	
	public void addRequest(String dispatcherURI,String requestURI)
	{
		RequestTimeData req = new RequestTimeData(dispatcherURI, vmURI,requestURI);
		this.requestTimeDataList.add(req);
		req.begin();
	}
	
	public void endRequest()
	{
		this.requestTimeDataList.get(currentRequest).terminate();
		currentRequest++;
	}
	
	public int calculateAverageTime()
	{		
		int nbRequest = 0;
		
		if(this.requestTimeDataList.size()>0)
		{
			Double res = 0.0;

			for(RequestTimeData timeData : this.requestTimeDataList)
			{
				if(timeData.isFinished())
				{
					res+=timeData.getDuration();
					nbRequest++;
				}
			}		
			
			System.err.println("REQ :"+nbRequest);
			System.err.println(String.format("res : %4.3f",res/1000000/1000));
			
			if(nbRequest>0)
				this.averageTime = res/nbRequest;
			else
				this.averageTime=null;
		}
		else
		{
			this.averageTime=null;
		}
		
		return nbRequest;
	}

	public ApplicationVMIntrospectionOutboundPort getAvmiovp() {
		return avmiovp;
	}
	
}
