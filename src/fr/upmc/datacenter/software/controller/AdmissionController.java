package fr.upmc.datacenter.software.controller;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.components.exceptions.ComponentStartException;
import fr.upmc.components.ports.PortI;

public class AdmissionController extends AbstractComponent{

	public AdmissionController(int nbThreads, int nbSchedulableThreads) {
		super(nbThreads, nbSchedulableThreads);
		// TODO Auto-generated constructor stub
	}

	public AdmissionController(String reflectionInboundPortURI, int nbThreads, int nbSchedulableThreads) {
		super(reflectionInboundPortURI, nbThreads, nbSchedulableThreads);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void addPort(PortI p) throws Exception {
		// TODO Auto-generated method stub
		super.addPort(p);
	}

	@Override
	public void doPortConnection(String portURI, String otherPortURI, String ccname) throws Exception {
		// TODO Auto-generated method stub
		super.doPortConnection(portURI, otherPortURI, ccname);
	}

	@Override
	public void doPortDisconnection(String portURI) throws Exception {
		// TODO Auto-generated method stub
		super.doPortDisconnection(portURI);
	}

	@Override
	public void start() throws ComponentStartException {
		// TODO Auto-generated method stub
		super.start();
	}

	@Override
	public void shutdown() throws ComponentShutdownException {
		// TODO Auto-generated method stub
		super.shutdown();
	}

	@Override
	public void shutdownNow() throws ComponentShutdownException {
		// TODO Auto-generated method stub
		super.shutdownNow();
	}

}