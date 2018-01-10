package fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.VirtualMachineData;
import fr.upmc.components.interfaces.DataOfferedI;
import fr.upmc.components.interfaces.DataRequiredI;
import fr.upmc.datacenter.interfaces.TimeStampingI;
import fr.upmc.datacenter.software.applicationvm.interfaces.ApplicationVMDynamicStateI;

public interface RequestDispatcherDynamicStateI 
extends 		DataOfferedI.DataI,
				DataRequiredI.DataI,
				TimeStampingI
{
	public String			getDispatcherURI() ;
	public Double getAvgExecutionTime();
	public Map<String,Double> getVrtualMachineExecutionAverageTime();
	public Map<String,ApplicationVMDynamicStateI> getVirtualMachineDynamicStates();
}
