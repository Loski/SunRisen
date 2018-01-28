package fr.upmc.PriseTheSun.datacenter.software.applicationvm;

import fr.upmc.PriseTheSun.datacenter.hardware.processors.ProcessorsController;
import fr.upmc.components.AbstractComponent;
import fr.upmc.datacenter.software.applicationvm.ApplicationVM;
import fr.upmc.datacenter.software.applicationvm.connectors.ApplicationVMManagementConnector;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;

public class ApplicationVMInfo{



	private String applicationVM;
	public String avmInbound;
	private String submissionInboundPortUri;
	private String computerManagementInboundPortURI;
	
	public ApplicationVMInfo(String applicationVM, String avmInbound, String submissionInboundPortUri, String computerManagementInboundPortURI2) {
		super();
		this.applicationVM = applicationVM;
		this.avmInbound = avmInbound;
		this.submissionInboundPortUri = submissionInboundPortUri;
		this.setComputerManagementInboundPortURI(computerManagementInboundPortURI2);
	}

	public String getApplicationVM() {
		return applicationVM;
	}

	public String getAvmInbound() {
		return avmInbound;
	}

	public void setAvmInbound(String avmInbound) {
		this.avmInbound = avmInbound;
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

	public String getComputerManagementInboundPortURI() {
		return computerManagementInboundPortURI;
	}

	public void setComputerManagementInboundPortURI(String computerManagementInboundPortURI) {
		this.computerManagementInboundPortURI = computerManagementInboundPortURI;
	}

}
