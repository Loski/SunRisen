package fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces;

import java.util.ArrayList;
import java.util.List;

import fr.upmc.components.interfaces.DataOfferedI;
import fr.upmc.components.interfaces.DataRequiredI;
import fr.upmc.datacenter.interfaces.TimeStampingI;

public interface RequestDispatcherDynamicStateI 
extends 		DataOfferedI.DataI,
				DataRequiredI.DataI,
				TimeStampingI
{
	public String			getDispatcherURI() ;
	public double getAvgExecutionTime();
	public List<String> getVMUri();
}
