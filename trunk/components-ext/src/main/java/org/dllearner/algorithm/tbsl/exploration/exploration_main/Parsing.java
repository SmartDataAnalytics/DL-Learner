package org.dllearner.algorithm.tbsl.exploration.exploration_main;
import java.util.HashMap;


public class Parsing {
	public static void do_parsing(HashMap<String, String> hm, String string){
		String [] array = string.split(" ");
		
		for(String name : hm.values()){
			//System.err.println(name);
			for(String inhalt : array){
				if(name.equals(inhalt)){
					System.out.println("Super " + inhalt);
				}
			}
		}
		
	}
}
