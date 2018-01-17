package fr.upmc.PriseTheSun.datacenter.software.ring.interfaces;

/**
 * @author	Maxime Lavaste and Loïc Lafontaine
 */

import java.util.List;

import fr.upmc.PriseTheSun.datacenter.software.applicationvm.ApplicationVMInfo;
import fr.upmc.components.interfaces.DataOfferedI;
import fr.upmc.components.interfaces.DataRequiredI;
import fr.upmc.datacenter.interfaces.TimeStampingI;

public interface RingDynamicStateI extends 	DataOfferedI.DataI,
DataRequiredI.DataI,
TimeStampingI{
	List<ApplicationVMInfo> getApplicationVMsInfo();
}
