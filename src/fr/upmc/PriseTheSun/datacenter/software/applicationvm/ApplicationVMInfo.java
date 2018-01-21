package fr.upmc.PriseTheSun.datacenter.software.applicationvm;

import fr.upmc.PriseTheSun.datacenter.hardware.processors.ProcessorsController;
import fr.upmc.components.AbstractComponent;
import fr.upmc.datacenter.software.applicationvm.ApplicationVM;
import fr.upmc.datacenter.software.applicationvm.connectors.ApplicationVMManagementConnector;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;

public class ApplicationVMInfo{


	public ApplicationVMInfo(String applicationVM, String avmOutbound, String submissionInboundPortUri) {
		super();
		this.applicationVM = applicationVM;
		this.submissionInboundPortUri = submissionInboundPortUri;
		this.avmOutbound = avmOutbound;
	}

	private String applicationVM;
	private String submissionInboundPortUri;

	private String avmOutbound;


	public String getApplicationVM() {
		return applicationVM;
	}

	public String getAvmOutbound() {
		return avmOutbound;
	}

	public void setAvmOutbound(String avmOutbound) {
		this.avmOutbound = avmOutbound;
	}

	public String getSubmissionInboundPortUri() {
		return submissionInboundPortUri;
	}

	public void setSubmissionInboundPortUri(String submissionInboundPortUri) {
		this.submissionInboundPortUri = submissionInboundPortUri;
	}

	public void setApplicationVM(String applicationVM) {
		this.applicationVM = applicationVM;
	}

}
