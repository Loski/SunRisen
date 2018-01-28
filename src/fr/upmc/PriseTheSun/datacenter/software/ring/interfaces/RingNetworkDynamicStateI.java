package fr.upmc.PriseTheSun.datacenter.software.ring.interfaces;


import java.util.List;
import fr.upmc.PriseTheSun.datacenter.software.applicationvm.ApplicationVMInfo;
import fr.upmc.components.interfaces.DataOfferedI;
import fr.upmc.components.interfaces.DataRequiredI;
import fr.upmc.datacenter.interfaces.TimeStampingI;

/**
 * The class <code>RingNetworkDynamicStateI</code> implements objects representing
 * a snapshot of the dynamic state of a node component of the ring to be pulled or
 * pushed through the dynamic state data interface.
 * @author Maxime Lavaste
 *
 */
public interface RingNetworkDynamicStateI extends DataOfferedI.DataI, DataRequiredI.DataI, TimeStampingI{
	List<ApplicationVMInfo> getApplicationVMsInfo();
}
