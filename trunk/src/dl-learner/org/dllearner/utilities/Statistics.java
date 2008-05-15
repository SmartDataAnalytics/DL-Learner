package org.dllearner.utilities;


import java.util.HashMap;
import java.util.LinkedList;


public class Statistics {

	private static String currentLabel="";
	private static LinkedList<String> order = new LinkedList<String>();
	private static HashMap<String, Integer> numberOfTriples = new HashMap<String, Integer>();
	private static HashMap<String, Long> timeCollecting = new HashMap<String, Long>();
	private static HashMap<String, Long> timeLearning = new HashMap<String, Long>();
	
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
	
	public static void addTimeLearning(long value) {
		Long current = timeLearning.get(currentLabel);
		if(current==null)
			timeLearning.put(currentLabel, new Long(value));
		else {
			timeLearning.put(currentLabel, new Long(current.longValue()+value));
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
		
		System.out.println("*****************TRIPLES");
		printInt(numberOfTriples,"triples\t");
		printIntAVG(numberOfTriples,number,"triples avg\t");
	
		System.out.println("*****************TIME");
		
		printLong(timeCollecting, "collecting\t");
		printLongAVG(timeCollecting,number,"collecting avg\t");
		printLong(timeLearning, "learning\t");
		printLongAVG(timeLearning,number,"learning avg\t");
		
		System.out.println("*****************Queries");
		printInt(numberOfCachedSparqlQueries,"cached queries\t");
		printInt(numberOfSparqlQueries,"total queries\t");
		
		
		//printIntAVG(numberOfTriples,number,"triples avg\t");
		
		
		
		
		
		
		
	}
	
	
	public static String getAVGTriplesForRecursionDepth(int number){
			
			String ret="#Label, i.e. rec depth \t avg number of triples\n";
			for (int i = 0; i < order.size(); i++) {
				String label=order.get(i);
				try {
					ret+=label+"\t"+ (numberOfTriples.get(label).intValue()/number)+"\n";
				} catch (Exception e) {	}
			}
			return ret;	
	}
	
	public static String getAVGTimeLearning(int number){
		
		String ret="#Label, i.e. rec depth \t avg time for learning including reasoning\n";
		for (int i = 0; i < order.size(); i++) {
			String label=order.get(i);
			try {
				ret+=label+"\t"+ (timeLearning.get(label).longValue()/number)+"\n";
			} catch (Exception e) {	}
		}
		return ret;	
}
	
	public static String getAVGTimeCollecting(int number){
		
		String ret="#Label, i.e. rec depth \t avg time for extraction\n";
		for (int i = 0; i < order.size(); i++) {
			String label=order.get(i);
			try {
				ret+=label+"\t"+ (timeCollecting.get(label).longValue()/number)+"\n";
			} catch (Exception e) {	}
		}
		return ret;	
	}
	
	public static void printIntAVG(HashMap<String, Integer>  hm, int number, String str){
		for (int i = 0; i < order.size(); i++) {
			String label=order.get(i);
			try {
				System.out.println(str+""+label+"\t"+ (hm.get(label).intValue()/number));
			} catch (Exception e) {	}
		}
	}
	
	public static void printInt(HashMap<String, Integer>  hm, String str){
		for (int i = 0; i < order.size(); i++) {
			String label=order.get(i);
			try {
				System.out.println(str+""+label+"\t"+hm.get(label));
			} catch (Exception e) {	}
		}
	}
	
	public static void printLongAVG(HashMap<String, Long>  hm, int number, String str){
		for (int i = 0; i < order.size(); i++) {
			String label=order.get(i);
			try {
				System.out.println(str+label+"\t"+ (hm.get(label).intValue()/number));
			} catch (Exception e) {	}
		}
	}
	
	public static void printLong(HashMap<String, Long>  hm,String str){
		for (int i = 0; i < order.size(); i++) {
			String label=order.get(i);
			try {
				System.out.println(str+label+"\t"+hm.get(label));
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
	
	public static void reset(){
		currentLabel="";
		order = new LinkedList<String>();
		 numberOfTriples = new HashMap<String, Integer>();
		timeCollecting = new HashMap<String, Long>();
		timeLearning = new HashMap<String, Long>();
		
		 numberOfSparqlQueries = new HashMap<String, Integer>();
		 numberOfCachedSparqlQueries = new HashMap<String, Integer>();
		
	}
	
	//stats
	
	
	
	
	
	
	
	
}
