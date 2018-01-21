package fr.upmc.PriseTheSun.datacenter.tools;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Writter {

	String name;
	
	public Writter(String csvFile) {
		name = csvFile;
	}
	
	public void write(String s) {	
		this.write(Arrays.asList(s));
	}
	
	public void write(List<String> s) {
		try {
			FileWriter fw = new FileWriter(name, true);
			CVSUtils.writeLine(fw, s);
			fw.flush();
			fw.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
