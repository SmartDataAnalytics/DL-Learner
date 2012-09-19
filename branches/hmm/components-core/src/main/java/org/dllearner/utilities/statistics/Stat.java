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

package org.dllearner.utilities.statistics;

import java.text.DecimalFormat;
import java.util.Set;

/**
 * Utility class for calculating the mean and standard deviation of a given set
 * of numbers. The class also contains convenience methods for printing values.
 * 
 * @author Jens Lehmann
 * 
 */
public class Stat {

    private int count = 0;
    private double sum = 0;
    private double squareSum = 0;
    private double min = Double.MAX_VALUE;
    private double max = Double.MIN_NORMAL;
    //used to give a good percentage output
    private DecimalFormat df = new DecimalFormat( ".00%" ); 

    public Stat() {
    	
    }
    
    /**
     * Creates a new stat object by merging two stat objects. The result is the same as if
     * the numbers, which have been added to stat1 and stat2 would have been added to this
     * stat object.
     * @param stat1 Statistical object 1.
     * @param stat2 Statistical object 2.
     */
    public Stat(Stat stat1, Stat stat2) {
    	count = stat1.count + stat2.count;
    	sum = stat1.sum + stat2.sum;
    	squareSum = stat1.squareSum + stat2.squareSum;
    	min = Math.min(stat1.min, stat2.min);
    	max = Math.max(stat1.max, stat2.max);
    }
    
    /**
     * Creates a new stat object by merging several stat objects. The result is the same as if
     * the numbers, which have been added to each stat would have been added to this
     * stat object.
     * @param stat1 Statistical object 1.
     * @param stat2 Statistical object 2.
     */
    public Stat(Set<Stat> stats) {
    	for(Stat stat : stats){
    		count += stat.count;
    		sum += stat.sum;
        	squareSum += stat.squareSum;
        	min = Math.min(min, stat.min);
        	max = Math.max(max, stat.max);
    	}
    }
    
    /**
     * Add a number to this object.
     * 
     * @param number
     *            The new number.
     */
    public void addNumber(double number) {
        count++;
        sum += number;
        squareSum += number * number;
        if(number<min)
        	min=number;
        if(number>max)
        	max=number;
    }

    /**
     * Gets the number of numbers.
     * 
     * @return The number of numbers.
     */
    public int getCount() {
        return count;
    }

    /**
     * Gets the sum of all numbers.
     * 
     * @return The sum of all numbers.
     */
    public double getSum() {
        return sum;
    }

    /**
     * Gets the mean of all numbers.
     * 
     * @return The mean of all numbers.
     */
    public double getMean() {
        return sum / count;
    }
    
    /**
     * Gets the mean of all numbers as percentage 
     * *100 so 0.5678 -> "56.78%"
     * @return The mean as formatted string.
     */
    public String getMeanAsPercentage(){
    	return df.format(getMean());
    }

    /**
     * Gets the standard deviation of all numbers.
     * 
     * @return The standard deviation of all numbers.
     */
    public double getStandardDeviation() {    	
    	if(count <= 1)
     		return 0.0;
    	
    	// formula from http://de.wikipedia.org/wiki/Standardabweichung
    	double val = (count*squareSum-sum*sum)/(count*(count-1));
    	double root = Math.sqrt(val);
    	
    	// due to rounding errors it can happen that "val" is negative
    	// this means that the real value is 0 (or very close to it), so
    	// we return 0
    	if(Double.isNaN(root)) 
    		return 0.0;
    	else
    		return root;
    }

	/**
	 * @return the min
	 */
	public double getMin() {
		return min;
	}

	/**
	 * @return the max
	 */
	public double getMax() {
		return max;
	}

	public String prettyPrint() {
		return prettyPrint("");
	}
	
	public String prettyPrint(String unit) {
		if(count > 0) {
			DecimalFormat df = new DecimalFormat();
			String str = "av. " + df.format(getMean()) + unit;
			str += " (deviation " + df.format(getStandardDeviation()) + unit + "; ";
			str += "min " + df.format(getMin()) + unit + "; ";
			str += "max " + df.format(getMax()) + unit + "; ";
			str += "count " + count + ")";
			return str;
		} else {
			return "no data collected";
		}
	}	
	
	public String prettyPrint(String unit, DecimalFormat df) {
		String str = "av. " + df.format(getMean()) + unit;
		str += " (deviation " + df.format(getStandardDeviation()) + unit + "; ";
		str += "min " + df.format(getMin()) + unit + "; ";
		str += "max " + df.format(getMax()) + unit + ")";		
		return str;
	}	
	
	/**
	 * Pretty prints the results under the assumption that the input
	 * values are time spans measured in nano seconds.
	 * 
	 * @see System#nanoTime()
	 * @return A string summarising statistical values.
	 */
//	public String prettyPrintNanoSeconds() {
//		DecimalFormat df = new DecimalFormat();
//		String str = "av. " + df.format(getMean()) + unit;
//		str += " (deviation " + df.format(getStandardDeviation()) + unit + "; ";
//		str += "min " + df.format(getMin()) + unit + "; ";
//		str += "max " + df.format(getMax()) + unit + ")";		
//		return str;		
//	}
	
}
