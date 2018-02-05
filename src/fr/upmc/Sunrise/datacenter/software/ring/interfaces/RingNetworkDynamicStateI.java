package fr.upmc.Sunrise.datacenter.software.ring.interfaces;

import fr.upmc.Sunrise.datacenter.software.applicationvm.ApplicationVMInfo;
import fr.upmc.components.interfaces.DataOfferedI;
import fr.upmc.components.interfaces.DataRequiredI;
import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;
import fr.upmc.datacenter.interfaces.TimeStampingI;

/**
 * The class <code>RingNetworkDynamicStateI</code> implements objects representing
 * a snapshot of the dynamic state of a node component of the ring to be pulled or
 * pushed through the dynamic state data interface.
 * @author Maxime Lavaste
 *
 */
public interface RingNetworkDynamicStateI extends DataOfferedI.DataI, DataRequiredI.DataI, TimeStampingI{
	ApplicationVMInfo getApplicationVMInfo();
}
