package org.dllearner.utilities;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.algorithms.refexamples.ExampleBasedROLComponent;
import org.dllearner.kb.sparql.Cache;
import org.dllearner.kb.sparql.SparqlQuery;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * provides convenience functions for timelogs
 *
 */
public class JamonMonitorLogger {

	
	public static List<Monitor> getMonitorsSortedByLabel(){
		MonitorFactory mf=(MonitorFactory)MonitorFactory.getFactory();
		SortedSet<String> retMon = new TreeSet<String>();
		
		@SuppressWarnings("unchecked")
		Iterator<Monitor> it = mf.iterator();
		
		while (it.hasNext()) {
			Monitor monitor = (Monitor) it.next();
			retMon.add(monitor.getLabel());
		}
		
		LinkedList<Monitor> l=new LinkedList<Monitor>();
		
		for (String label : retMon) {
			l.add(MonitorFactory.getTimeMonitor(label));
		}
		
		return l;
	}
	
	
	
	public static void printAll() {
		List<Monitor> l= getMonitorsSortedByLabel();
		for (int i = 0; i < l.size(); i++) {
			Monitor monitor = l.get(i);
			System.out.println(monitor);
		}
	}
	
	
	
	
	public static String getMonitorPrefix(Class clazz){
		String retval="";
		if (clazz == SparqlQuery.class) {
			retval= "Sparql:";
		} else if (clazz == Cache.class) {
			retval= "Sparql:";
		} else if (clazz == Cache.class) {
			retval= "Sparql:";
		} else if (clazz == SparqlQuery.class) {
			retval= "sparql:";
		} else if (clazz == SparqlQuery.class) {
			retval= "sparql:";
		} else if (clazz == SparqlQuery.class) {
			retval= "sparql:";
		} else if (clazz == SparqlQuery.class) {
			retval= "sparql:";
		} else if (clazz == SparqlQuery.class) {
			retval= "sparql:";
		} else if (clazz == SparqlQuery.class) {
			retval= "sparql:";
		} else if (clazz == ExampleBasedROLComponent.class) {
			retval= "Learning:";
		} else if (clazz == SparqlQuery.class) {
			retval= "sparql:";
		} else {
			retval= "undefined:";
		}
		return retval+clazz.getSimpleName()+":";
	}
	
	public static  Monitor getTimeMonitor(Class clazz, String label) {
		
		String labeltmp = getMonitorPrefix(clazz)+label;
		return MonitorFactory.getTimeMonitor(labeltmp);
		
	}
	
	public static void increaseCount (Class clazz, String label) {
		 MonitorFactory.getMonitor(getMonitorPrefix(clazz)+label, "#").add(1.0);
	}
	
	
	
	
}
