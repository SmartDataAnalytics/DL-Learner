/**
 * Copyright (C) 2007-2011, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.dllearner.utilities;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.algorithms.ocel.OCEL;
import org.dllearner.kb.extraction.ExtractionAlgorithm;
import org.dllearner.kb.manipulator.Manipulator;
import org.dllearner.kb.manipulator.TypeFilterRule;
import org.dllearner.kb.sparql.Cache;
import org.dllearner.kb.sparql.SparqlQuery;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * provides convenience functions for timelogs
 *
 */
public class JamonMonitorLogger {


	public static final String MS = "ms.";
	public static final String SECONDS = "sec.";
	public static final String COUNT = "count";
	public static final String DOUBLE = "double";
	public static final String PERCENTAGE = "%";
	
	public static List<Monitor> getMonitors(){
		//MonitorFactory mf=(MonitorFactory)MonitorFactory.getFactory();
		LinkedList<Monitor> l=new LinkedList<Monitor>();
		
		@SuppressWarnings("unchecked")
		Iterator<Monitor> it = MonitorFactory.getFactory().iterator();
			//mf.iterator();
		while (it.hasNext()) {
			Monitor monitor = (Monitor) it.next();
			
			l.add(monitor);
		}
		
		return l;
	}
	
	
	
	public static void printAllSortedByLabel() {
		
			System.out.println(getStringForAllSortedByLabel());
		
	}
	
	public static String getStringForAllSortedByLabel() {
		List<Monitor> l= getMonitors();
		SortedSet<String> sset = new TreeSet<String>();
		StringBuffer sbuf = new StringBuffer();
		for (int i = 0; i < l.size(); i++) {
			Monitor monitor = l.get(i);
			
			sset.add(monitor.toString());
		}
		for (String onemon : sset) {
			sbuf.append(onemon+"\n");
		}
		return sbuf.toString();
	}
	
	public static String convMonitorToString (Monitor m) {
		String retVal = m.getLabel()+"|\t";
		String unit = m.getUnits();
		retVal+=unit+"|\t";
		long content = new Double(m.getTotal()).longValue();
		content = content / (1000*1000);
		String contentstr = (unit.equals(MS))? Helper.prettyPrintNanoSeconds(content ) : m.getHits()+"" ;
		retVal+= "total:"+contentstr+"|\t";
		
		long avg = new Double(m.getAvg()).longValue();
		avg = avg / (1000*1000);
		String avgstr = (unit.equals(MS))? Helper.prettyPrintNanoSeconds(avg ) : avg+"" ;
		retVal+= "avg:"+avgstr+"|\t";
		
		return retVal;
	}
	
	
	
	@SuppressWarnings("all")
	public static String getMonitorPrefix(Class clazz){
		String retval="";
		if (clazz == SparqlQuery.class) {
			retval= "Sparql:";
		} else if (clazz == Cache.class) {
			retval= "Sparql:";
		}else if (clazz == ExtractionAlgorithm.class) {
			retval= "Extraction:";
		} else if (clazz == Manipulator.class) {
			retval= "Extraction:";
//		Jens: temporarily commented out, because "core" does not depend on "interfaces" 	
//		} else if (clazz == Start.class) {
//			retval= "Init:";
		} else if (clazz == TypeFilterRule.class) {
			retval= "Extraction:";
		} else if (clazz == SparqlQuery.class) {
			retval= "sparql:";
		} else if (clazz == SparqlQuery.class) {
			retval= "sparql:";
		} else if (clazz == OCEL.class) {
			retval= "Learning:";
		} else if (clazz == SparqlQuery.class) {
			retval= "sparql:";
		} else {
			retval= "undefined:";
		}
		return retval+clazz.getSimpleName()+":";
	}
	
	
	
	@SuppressWarnings("all")
	public static  Monitor getTimeMonitor(Class clazz, String label) {
		String labeltmp = getMonitorPrefix(clazz)+label;
		return MonitorFactory.getTimeMonitor(labeltmp);
		
	}
	
	@SuppressWarnings("all")
	public static  Monitor getStatisticMonitor( String label) {
		return MonitorFactory.getMonitor(label, "double");
	}
	@SuppressWarnings("all")
	public static  Monitor getStatisticMonitor( String label, String unit) {
		return MonitorFactory.getMonitor(label, unit);
		
	}
	
	@SuppressWarnings("all")
	public static void increaseCount (Class clazz, String label) {
		// MonitorFactory.getMonitor(getMonitorPrefix(clazz)+label, "#").add(1.0);
		 Monitor m =  MonitorFactory.getMonitor(getMonitorPrefix(clazz)+label, "count");
		// System.out.println(m);
		 m.setHits(m.getHits()+1);
		//System.out.println(m);
	}
	
	public static void writeHTMLReport(String filename){
		File jamonlog = new File(filename);
		Files.createFile(jamonlog, MonitorFactory.getReport());
		Files.appendToFile(jamonlog, "<xmp>\n"+JamonMonitorLogger.getStringForAllSortedByLabel());
	}
	
	
	
	
}
