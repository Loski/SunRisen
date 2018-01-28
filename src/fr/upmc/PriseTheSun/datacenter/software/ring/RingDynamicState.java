package fr.upmc.PriseTheSun.datacenter.software.ring;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import fr.upmc.PriseTheSun.datacenter.software.applicationvm.ApplicationVMInfo;
import fr.upmc.PriseTheSun.datacenter.software.ring.interfaces.RingNetworkDynamicStateI;



/**
 * @author	Maxime Lavaste and Loïc Lafontaine
 */
public class RingDynamicState implements RingNetworkDynamicStateI{

	private static final long serialVersionUID = 1L ;
	/** timestamp in Unix time format, local time of the timestamper.		*/
	protected final long timestamp;
	/** IP of the node that did the timestamping.							*/
	protected final String timestamperIP ;
	protected final List<ApplicationVMInfo> vmDataInfo;
	
	
	public RingDynamicState(List<ApplicationVMInfo> vmDataList) throws UnknownHostException{
		super() ;
		this.vmDataInfo=vmDataList;
		this.timestamp = System.currentTimeMillis() ;
		this.timestamperIP = InetAddress.getLocalHost().getHostAddress() ;
	}
	
	
	/** 
	 * @see fr.upmc.datacenter.interfaces.TimeStampingI#getTimeStamp()
	 */
	@Override
	public long getTimeStamp() {
		return timestamp;
	}

	
	/**
	 * @see fr.upmc.datacenter.interfaces.TimeStampingI#getTimeStamperId()
	 */
	@Override
	public String getTimeStamperId() {
		return timestamperIP;
	}

	
	/**
	 * @see fr.upmc.datacenter.RingNetworkDynamicStateI.interfaces.RingDynamicStateI#getApplicationVMsInfo()
	 */
	@Override
	public List<ApplicationVMInfo> getApplicationVMsInfo() {
		return vmDataInfo;
	}

	
}
