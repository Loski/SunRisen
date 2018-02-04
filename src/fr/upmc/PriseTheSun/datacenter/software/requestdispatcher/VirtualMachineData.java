package fr.upmc.PriseTheSun.datacenter.software.requestdispatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMIntrospectionOutboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort;

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
	
	public void addRequest(String requestURI,RequestTimeData reqTimeData)
	{
		this.requestInQueue.put(requestURI,reqTimeData);
	}
	
	public void endRequest(String requestURI)
	{
		RequestTimeData req = this.requestInQueue.remove(requestURI);
		req.terminate();
		synchronized(this.requestTerminated)
		{
			this.requestTerminated.add(req);
			
		}
	}
	
	public void calculateAverageTime()
	{		
			Double res = 0.0;
			
			if(this.requestTerminated.size()>0)
			{
				for(RequestTimeData timeData : this.requestTerminated)
				{
					res+=timeData.getDuration();
				}
				
				this.averageTime = res/this.requestTerminated.size();
			}
			
			synchronized(this.requestTerminated)
			{
				this.requestTerminated.clear();
			}
	}

	public ApplicationVMIntrospectionOutboundPort getAvmiovp() {
		return avmiovp;
	}
	
}
