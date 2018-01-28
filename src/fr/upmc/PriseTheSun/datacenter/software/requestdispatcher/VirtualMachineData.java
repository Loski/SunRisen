package fr.upmc.PriseTheSun.datacenter.software.requestdispatcher;

import java.util.ArrayList;
import java.util.HashMap;
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
	private HashMap<String,RequestTimeData> requestInQueue;
	private List<RequestTimeData> requestTerminated;

	public VirtualMachineData(String uri, RequestSubmissionOutboundPort rsobp, ApplicationVMIntrospectionOutboundPort avmiovp)
	{
		this.vmURI=uri;
		this.rsobp=rsobp;
		this.avmiovp=avmiovp;
		this.averageTime=null;
		this.requestInQueue = new  HashMap<String,RequestTimeData>();
		this.requestTerminated = new  ArrayList<RequestTimeData>();
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
	
	public HashMap<String,RequestTimeData> getRequestInQueue() {
		return this.requestInQueue;
	}
	
	public List<RequestTimeData> getRequestTerminated() {
		return this.requestTerminated;
	}
	
	/*public void resetRequestTimeDataList()
	{
		ListIterator<RequestTimeData> iterator = this.requestTimeDataList.listIterator();
		
		while(iterator.hasNext()){
			RequestTimeData data = iterator.next();
			if(data.isFinished())
			{
				iterator.remove();
			}
		}
		this.averageTime=null;
	}*/
	
	public void addRequest(String dispatcherURI,String requestURI)
	{
		RequestTimeData req = new RequestTimeData(dispatcherURI, vmURI,requestURI);
		this.requestInQueue.put(requestURI,req);
		req.begin();
	}
	
	public void endRequest(String requestURI)
	{
		RequestTimeData req = this.requestInQueue.remove(requestURI);
		this.requestTerminated.add(req);
		req.terminate();
	}
	
	public void calculateAverageTime()
	{				
		if(this.requestTerminated.size()>0)
		{
			Double res = 0.0;

			for(RequestTimeData timeData : this.requestTerminated)
			{
				res+=timeData.getDuration();
			}
			
			this.averageTime = res/this.requestTerminated.size();
		}
		else
		{
			this.averageTime=null;
		}
	}

	public ApplicationVMIntrospectionOutboundPort getAvmiovp() {
		return avmiovp;
	}
	
}
