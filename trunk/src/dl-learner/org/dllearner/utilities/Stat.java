/**
 * Copyright (C) 2007, Jens Lehmann
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
 *
 */

package org.dllearner.utilities;

/**
 * Utility class for calculating the mean and standard deviation of a given set
 * of numbers.
 * 
 * @author Jens Lehmann
 * 
 */
public class Stat {

    private int count = 0;
    private double sum = 0;
    private double squareSum = 0;

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
     * Gets the standard deviation of all numbers.
     * 
     * @return The stanard deviation of all numbers.
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

}
