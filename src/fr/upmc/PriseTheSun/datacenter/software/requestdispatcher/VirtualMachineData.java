package fr.upmc.PriseTheSun.datacenter.software.requestdispatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import fr.upmc.datacenter.TimeManagement;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMIntrospectionOutboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort;

public class VirtualMachineData {

	private String vmURI;
	private RequestSubmissionOutboundPort rsobp;
	private ApplicationVMIntrospectionOutboundPort avmiovp;
	private Double previousAverage;
	private Double newAverage;
	/**  */
	private HashMap<String,RequestTimeData> requestInQueue;
	private List<RequestTimeData> requestTerminated;
	
	private Object lock;

	public VirtualMachineData(String uri, RequestSubmissionOutboundPort rsobp, ApplicationVMIntrospectionOutboundPort avmiovp)
	{
		this.vmURI=uri;
		this.rsobp=rsobp;
		this.avmiovp=avmiovp;
		this.previousAverage=null;
		this.newAverage=null;
		this.requestInQueue = new  HashMap<String,RequestTimeData>();
		this.requestTerminated = new  ArrayList<RequestTimeData>();
		this.lock = new Object();
	}
	
	public String getVmURI() {
		return vmURI;
	}
	public RequestSubmissionOutboundPort getRsobp() {
		return rsobp;
	}
	public Double getPreviousAverage() {
		return previousAverage;
	}
	public Double getNewAverage() {
		return newAverage;
	}
	
	public HashMap<String,RequestTimeData> getRequestInQueue() {
		return this.requestInQueue;
	}
	
	public List<RequestTimeData> getRequestTerminated() {
		synchronized(this.lock)
		{
			return this.requestTerminated;
		}
	}
	
	public void addRequest(String dispatcherURI,String requestURI)
	{
		RequestTimeData req = new RequestTimeData(dispatcherURI, vmURI,requestURI);
		this.requestInQueue.put(requestURI,req);
		req.begin();
	}
	
	public void endRequest(String requestURI)
	{
		RequestTimeData req = this.requestInQueue.remove(requestURI);
		req.terminate();
		synchronized(this.lock)
		{
			this.requestTerminated.add(req);
		}
	}
	
	public void calculateAverageTime()
	{		
		synchronized(this.lock)
		{
			Double res = 0.0;
			
			if(this.requestTerminated.size()>0)
			{
				for(RequestTimeData timeData : this.requestTerminated)
				{
					res+=timeData.getDuration();
				}
				
				res = res/this.requestTerminated.size();
			}			
			
			this.requestTerminated.clear();
		}
	}

	public ApplicationVMIntrospectionOutboundPort getAvmiovp() {
		return avmiovp;
	}
	
}
