package fr.upmc.PriseTheSun.datacenter.software.admissioncontroller.interfaces;

import java.util.ArrayList;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;

public interface AdmissionControllerManagementI extends	OfferedI, RequiredI{
	 public boolean addCores(String rdURI, int nbCores, String vmUri ) throws Exception;
	 public void linkComputer(String computerURI, String ComputerServicesInboundPortURI,String ComputerStaticStateDataInboundPortURI,
			 String ComputerDynamicStateDataInboundPortURI, ArrayList<String> processorsURI, ArrayList<String> pmipURIs, ArrayList<String> pssdURIs, ArrayList<String> pdssURIs) throws Exception;
	 }
