package fr.upmc.PriseTheSun.datacenter.software.requestdispatcher;

import java.util.ArrayList;
import java.util.List;

import fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort;

public class VirtualMachineData {

	private String vmURI;
	private RequestSubmissionOutboundPort rsobp;
	private double averageTime;
	/**  */
	private List<RequestTimeData> requestTimeDataList;

	public VirtualMachineData(String uri,RequestSubmissionOutboundPort port)
	{
		this.vmURI=uri;
		this.rsobp=port;
		this.averageTime=0.0;
		this.requestTimeDataList = new ArrayList<RequestTimeData>();
	}
	
	public String getVmURI() {
		return vmURI;
	}
	public RequestSubmissionOutboundPort getRsobp() {
		return rsobp;
	}
	public double getAverageTime() {
		return averageTime;
	}
	public void setAverageTime(double averageTime) {
		this.averageTime = averageTime;
	}
	
	public List<RequestTimeData> getRequestTimeDataList() {
		return this.requestTimeDataList;
	}
	
	public void resetRequestTimeDataList()
	{
		this.requestTimeDataList = new ArrayList<RequestTimeData>();
	}
	
}
