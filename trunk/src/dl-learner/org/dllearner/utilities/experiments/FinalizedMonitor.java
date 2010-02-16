package org.dllearner.utilities.experiments;

import java.io.Serializable;

import com.jamonapi.MonKey;
import com.jamonapi.Monitor;

public class FinalizedMonitor implements Serializable{
	private static final long serialVersionUID = 6617125369204663530L;
	
	final String header;
	final String units;
	
	final double avg;
	final double hits;
	final double stdDev;
	final double lastValue;
	final double max;
	final double min;
	
	
	public FinalizedMonitor(Monitor m){
		header = (String)m.getMonKey().getValue(MonKey.LABEL_HEADER);
		units = (String)m.getMonKey().getValue(MonKey.UNITS_HEADER);
		avg = m.getAvg();
		hits = m.getHits();
		stdDev = m.getStdDev();
		lastValue = m.getLastValue();
		max = m.getMax();
		min = m.getMin();
		
		
	}


	public String getHeader() {
		return header;
	}


	public String getUnits() {
		return units;
	}


	public double getAvg() {
		return avg;
	}


	public double getHits() {
		return hits;
	}


	public double getStdDev() {
		return stdDev;
	}


	public double getLastValue() {
		return lastValue;
	}


	public double getMax() {
		return max;
	}


	public double getMin() {
		return min;
	}
	
	
	
}
