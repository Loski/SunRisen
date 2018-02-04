package fr.upmc.PriseTheSun.datacenter.software.requestdispatcher.interfaces;

import fr.upmc.components.interfaces.DataOfferedI;
import fr.upmc.components.interfaces.DataRequiredI;
import fr.upmc.datacenter.interfaces.TimeStampingI;

/**
 * The interface <code>RequestDispatcherStaticStateI</code> types the data
 * objects exchanged to get static information from dispatchers
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	true
 * </pre>
 * 
 * @author	Loïc Lafontaine
 * @author	Maxime Lavaste
 */

public interface RequestDispatcherStaticStateI 
extends 		DataOfferedI.DataI,
				DataRequiredI.DataI,
				TimeStampingI
{
	

	
}
