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

package org.dllearner.utilities.experiments;

import java.io.Serializable;

import com.jamonapi.MonKey;
import com.jamonapi.Monitor;

/**
 * This a class to make a Jamon monitor persistent
 * @author Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
 *
 */
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
	final double total;
	
	
	public FinalizedMonitor(Monitor m){
		header = (String)m.getMonKey().getValue(MonKey.LABEL_HEADER);
		units = (String)m.getMonKey().getValue(MonKey.UNITS_HEADER);
		avg = m.getAvg();
		hits = m.getHits();
		stdDev = m.getStdDev();
		lastValue = m.getLastValue();
		max = m.getMax();
		min = m.getMin();
		total = m.getTotal();
		
		
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
	public double getTotal() {
		return total;
	}
	
	
	
}
