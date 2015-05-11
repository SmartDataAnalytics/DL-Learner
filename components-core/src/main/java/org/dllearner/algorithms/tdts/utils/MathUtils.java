package org.dllearner.algorithms.tdts.utils;


import org.apache.commons.math3.stat.StatUtils;

/**
 * Utils to compute statistics for classifier performance
 * @author Utente
 *
 */
public class MathUtils {
	private static double mean;
	private static double stddev;
	public static double avg(double[] population) {
		mean = StatUtils.mean(population);
		return mean;
	}
	
	public static double variance(double[] population) {
		stddev = StatUtils.variance(population);
		return stddev;
	}
//	public double[] sampling (int dimSample) {
//		
//		RandomDataGenerator randomData = new RandomDataGenerator(); 
//		randomData.reSeed(2);
//		for (int i = 0; i < dimSample; i++) {
//		    double value = randomData.nextLong(1, 1000000);
//		}
//		return null;
//	}

	public static double stdDeviation(double[] population) {
		stddev = StatUtils.variance(population);
		return Math.sqrt(stddev);
	}
	
	
	

}
