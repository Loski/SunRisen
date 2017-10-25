package fr.upmc.datacenterclient.applicationprovider.interfaces;

public interface ApplicationNotificationI {
	void notifyRequestGeneratorCreated(String requestNotificationInboundPortURI, String rdnopUri) throws Exception;
}
