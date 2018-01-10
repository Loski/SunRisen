package fr.upmc.PriseTheSun.datacenter.software.applicationvm;

import fr.upmc.PriseTheSun.datacenter.hardware.processors.ProcessorsController;
import fr.upmc.components.AbstractComponent;
import fr.upmc.datacenter.software.applicationvm.ApplicationVM;
import fr.upmc.datacenter.software.applicationvm.connectors.ApplicationVMManagementConnector;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;

public class ApplicationVMInfo{

	private String avm;
	private String submission;
	private String applicationVM;

	public ApplicationVMInfo(String applicationVM, String avm, String submission) {
		this.applicationVM = applicationVM;
		this.avm = avm;
		this.submission = submission;
	}
	
	public String getAvm() {
		return avm;
	}


	public void setAvm(String avm) {
		this.avm = avm;
	}


	public String getSubmission() {
		return submission;
	}
	public void setSubmission(String submission) {
		this.submission = submission;
	}
	public String getApplicationVM() {
		return applicationVM;
	}

	public void setApplicationVM(String applicationVM) {
		this.applicationVM = applicationVM;
	}

}
