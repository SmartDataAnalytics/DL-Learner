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
        // TODO: Standardabweichung ist anscheinend nicht korrekt, siehe
        // http://de.wikipedia.org/wiki/Standardabweichung. Es muss N-1 verwendet
        // werden.    	
    	/*
        double mean = getMean();
        double tmp = squareSum / count - mean * mean;
        if(tmp==0)
        	return 0;
        else
        	return Math.sqrt(tmp);
        */
    	// korrekter Code nach http://de.wikipedia.org/wiki/Standardabweichung
        return Math.sqrt((count*squareSum-sum*sum)/(count*(count-1)));
    }

}
