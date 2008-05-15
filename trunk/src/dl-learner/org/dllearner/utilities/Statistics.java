package org.dllearner.utilities;


import java.util.HashMap;
import java.util.LinkedList;


public class Statistics {

	private static String currentLabel="";
	private static LinkedList<String> order = new LinkedList<String>();
	private static HashMap<String, Integer> numberOfTriples = new HashMap<String, Integer>();
	private static HashMap<String, Long> timeCollecting = new HashMap<String, Long>();
	private static HashMap<String, Integer> numberOfSparqlQueries = new HashMap<String, Integer>();
	private static HashMap<String, Integer> numberOfCachedSparqlQueries = new HashMap<String, Integer>();
	
	
	public static void addTriples(int value) {
		
		Integer current = numberOfTriples.get(currentLabel);
		if(current==null)
			numberOfTriples.put(currentLabel, new Integer(value));
		else {
		    numberOfTriples.put(currentLabel, new Integer(current.intValue()+value));
		}
		
		
		
	}
	
	public static void addTimeCollecting(long value) {
		Long current = timeCollecting.get(currentLabel);
		if(current==null)
			timeCollecting.put(currentLabel, new Long(value));
		else {
			timeCollecting.put(currentLabel, new Long(current.longValue()+value));
		}
	}
	
	public static void increaseCachedQuery() {
		Integer current = numberOfCachedSparqlQueries.get(currentLabel);
		if(current==null)
			numberOfCachedSparqlQueries.put(currentLabel, new Integer(1));
		else {
			numberOfCachedSparqlQueries.put(currentLabel, new Integer(current.intValue()+1));
		}
	}
	
	public static void increaseQuery() {
		Integer current = numberOfSparqlQueries.get(currentLabel);
		if(current==null)
			numberOfSparqlQueries.put(currentLabel, new Integer(1));
		else {
			numberOfSparqlQueries.put(currentLabel, new Integer(current.intValue()+1));
		}
	}
	
	
	public static void print(int number){
		
		printInt(numberOfTriples,"triples");
		printIntAVG(numberOfTriples,number,"triples avg");
	
		System.out.println("*****************TIME");
		
		printLong(timeCollecting);
		printLongAVG(timeCollecting,number);
		
		System.out.println("*****************Queries");
		printInt(numberOfCachedSparqlQueries,"cached queries");
		printInt(numberOfSparqlQueries,"total queries");
		
		
		
		
		
		
		
		
		
		
	}
	
	public static void printIntAVG(HashMap<String, Integer>  hm, int number, String str){
		for (int i = 0; i < order.size(); i++) {
			String label=order.get(i);
			try {
				System.out.println(str+" "+label+"|"+ (hm.get(label).intValue()/number));
			} catch (Exception e) {	}
		}
	}
	
	public static void printInt(HashMap<String, Integer>  hm, String str){
		for (int i = 0; i < order.size(); i++) {
			String label=order.get(i);
			try {
				System.out.println(str+" "+label+"|"+hm.get(label));
			} catch (Exception e) {	}
		}
	}
	
	public static void printLongAVG(HashMap<String, Long>  hm, int number){
		for (int i = 0; i < order.size(); i++) {
			String label=order.get(i);
			try {
				System.out.println("timeCollect avg "+label+"|"+ (hm.get(label).intValue()/number));
			} catch (Exception e) {	}
		}
	}
	
	public static void printLong(HashMap<String, Long>  hm){
		for (int i = 0; i < order.size(); i++) {
			String label=order.get(i);
			try {
				System.out.println("timeCollect "+label+"|"+hm.get(label));
			} catch (Exception e) {	}
		}
	}
	
	
	public static void setCurrentLabel(String label) {
		currentLabel=label;
		if (!order.contains(label))order.add(label);
	}
	
	public static String getCurrentLabel() {
		return currentLabel;
	}
	
	
	//stats
	
	
	
	
	
	
	
	
}
