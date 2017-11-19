package fr.upmc.datacenterclient.tests;

import fr.upmc.components.cvm.utils.DCVMCyclicBarrier;
import fr.upmc.components.registry.GlobalRegistry;

public class Launcher {
	
	public static void main(String[] args) {
		if(args[0].equals("barrier"))
			DCVMCyclicBarrier.main(new String[] {"context.xml"});
		else if(args[0].equals("registry"))
			GlobalRegistry.main(new String[] {"context.xml"});
		else if(args [0].equals("controller"))
			TestDCVM.main(new String[] {"controller","context.xml"});
		else if(args [0].equals("application1"))
			TestDCVM.main(new String[] {"application1","context.xml"});
		else if(args [0].equals("application2"))
			TestDCVM.main(new String[] {"application2","context.xml"});
	}

}
