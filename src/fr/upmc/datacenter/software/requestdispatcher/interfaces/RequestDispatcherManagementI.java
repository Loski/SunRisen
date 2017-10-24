package fr.upmc.datacenter.software.requestdispatcher.interfaces;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;

public interface RequestDispatcherManagementI extends OfferedI, RequiredI {
    
    public void connectVm(String vmURI, String RequestSubmissionInboundPortURI) throws Exception;    
    public void disconnectVm() throws Exception;
    
}
