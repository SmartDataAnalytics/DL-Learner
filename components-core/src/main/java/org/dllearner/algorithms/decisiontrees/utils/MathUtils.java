/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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
package org.dllearner.algorithms.decisiontrees.utils;

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
