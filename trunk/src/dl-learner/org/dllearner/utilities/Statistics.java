package org.dllearner.utilities;


import java.util.HashMap;
import java.util.TreeSet;


public class Statistics {

	private static String currentLabel="";
	private static HashMap<String, Integer> numberOfTriples = new HashMap<String, Integer>();
	private static HashMap<String, Long> timeCollecting = new HashMap<String, Long>();
	
	
	public static void addTriples(int value) {
		Integer current = numberOfTriples.get(currentLabel);
		if(current==null)
			numberOfTriples.put(currentLabel, new Integer(0));
		else {
			numberOfTriples.put(currentLabel, new Integer(current.intValue()+value));
		}
		
	}
	
	public static void addTimeCollecting(long value) {
		Long current = timeCollecting.get(currentLabel);
		if(current==null)
			timeCollecting.put(currentLabel, new Long(0));
		else {
			timeCollecting.put(currentLabel, new Long(current.longValue()+value));
		}
	}
	
	public static void print(int number){
		System.out.println(numberOfTriples);
		TreeSet<String> s=new TreeSet<String>();
		for (String label : numberOfTriples.keySet()) {
			s.add(label+"|"+numberOfTriples.get(label));
			System.out.println(label+"|"+numberOfTriples.get(label));
			System.out.println(label+"|"+ (numberOfTriples.get(label).intValue()/number));
		
		}
		System.out.println(s);
		
		System.out.println("*****************TIME");
		for (String label : timeCollecting.keySet()) {
			System.out.println(label+"|"+timeCollecting.get(label));
			System.out.println(label+"|"+ (timeCollecting.get(label).intValue()/number));
		}
		
		
	}
	
	public static void setCurrentLabel(String label) {
		currentLabel=label;
	}
	
	
	//stats
	
	
	
	
	
	
	
	
}
