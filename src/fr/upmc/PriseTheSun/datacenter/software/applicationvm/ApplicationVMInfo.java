package fr.upmc.PriseTheSun.datacenter.software.applicationvm;

/**
 * JavaBean pour représenter une VM dans le ring network. On peut accéder ainsi à ses principaux ports pour la lier directement à un <code>Controller</code> et à un <code>RequestDispatcher</code> sans repasser par <code>AdmissionControllerDynamic</code>
 * @author Maxime Lavaste
 */
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
