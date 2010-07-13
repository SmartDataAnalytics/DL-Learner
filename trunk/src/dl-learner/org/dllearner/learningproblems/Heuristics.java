/**
 * Copyright (C) 2007-2010, Jens Lehmann
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
package org.dllearner.learningproblems;

/**
 * Implementation of various heuristics. The methods can be used in learning
 * problems and various evaluation scripts. They are verified in unit tests
 * and, thus, should be fairly stable.
 * 
 * @author Jens Lehmann
 * 
 */
public class Heuristics {

	/**
	 * Computes F1-Score.
	 * @param recall Recall.
	 * @param precision Precision.
	 * @return Harmonic mean of precision and recall.
	 */
	public static double getFScore(double recall, double precision) {
		return (precision + recall == 0) ? 0 :
			  ( 2 * (precision * recall) / (double) (precision + recall) ); 		
	}
	
	/**
	 * Computes F-beta-Score.
	 * @param recall Recall.
	 * @param precision Precision.
	 * @param beta Weights precision and recall. If beta is >1, then recall is more important
	 * than precision.
	 * @return Harmonic mean of precision and recall weighted by beta.
	 */
	public static double getFScore(double recall, double precision, double beta) {
		return (precision + recall == 0) ? 0 :
			  ( (1+Math.sqrt(beta)) * (precision * recall)
					/ (Math.sqrt(beta) * precision + recall) ); 		
	}	

	/**
	 * Computes arithmetic mean of precision and recall, which is called "A-Score"
	 * here (A=arithmetic), but is not an established notion in machine learning.  
	 * @param recall Recall.
	 * @param precision Precison.
	 * @return Arithmetic mean of precision and recall.
	 */
	public static double getAScore(double recall, double precision) {
		return (recall + precision) / (double) 2;
	}

	/**
	 * Computes arithmetic mean of precision and recall, which is called "A-Score"
	 * here (A=arithmetic), but is not an established notion in machine learning.  
	 * @param recall Recall.
	 * @param precision Precison.
	 * @param beta Weights precision and recall. If beta is >1, then recall is more important
	 * than precision.
	 * @return Arithmetic mean of precision and recall.
	 */
	public static double getAScore(double recall, double precision, double beta) {
		return (beta * recall + precision) / (beta + 1);
	}
	
	/**
	 * Computes the Jaccard coefficient of two sets.
	 * @param elementsIntersection Number of elements in the intersection of the two sets.
	 * @param elementsUnion Number of elements in the union of the two sets.
	 * @return #intersection divided by #union.
	 */
	public static double getJaccardCoefficient(int elementsIntersection, int elementsUnion) {
		if(elementsIntersection > elementsUnion || elementsUnion < 1) {
			throw new IllegalArgumentException();
		}
		return elementsIntersection / (double) elementsUnion;
	}
	
	public double getPredictiveAccuracy(int nrOfExamples, int nrOfPosClassifiedPositives, int nrOfNegClassifiedNegatives) {
		return (nrOfPosClassifiedPositives + nrOfNegClassifiedNegatives) / (double) nrOfExamples;
	}	

	public double getPredictiveAccuracy(int nrOfExamples, int nrOfPosClassifiedPositives, int nrOfNegClassifiedNegatives, double beta) {
//		return (nrOfPosClassifiedPositives + nrOfNegClassifiedNegatives) / (double) nrOfExamples;
		return 0;
	}		
	
	public double getPredictiveAccuracy2(int nrOfExamples, int nrOfPosClassifiedPositives, int nrOfPosClassifiedNegatives) {
		return (nrOfPosClassifiedPositives + nrOfExamples - nrOfPosClassifiedNegatives) / (double) nrOfExamples;
	}
	
	public double getPredictiveAccuracy2(int nrOfExamples, int nrOfPosClassifiedPositives, int nrOfNegClassifiedNegatives, double beta) {
//		return (nrOfPosClassifiedPositives + nrOfNegClassifiedNegatives) / (double) nrOfExamples;
		return 0;
	}	
	
	/**
	 * Computes the 95% confidence interval of an experiment with boolean outcomes,
	 * e.g. heads or tails coin throws. It uses the very efficient, but still accurate
	 * Wald method. 
	 * @param success Number of successes, e.g. number of times the coin shows head.
	 * @param total Total number of tries, e.g. total number of times the coin was thrown.
	 * @return A two element double array, where element 0 is the lower border and element
	 * 1 the upper border of the 95% confidence interval.
	 */
	public static double[] getConfidenceInterval95Wald(int success, int total) {
		if(success > total || total < 1) {
			throw new IllegalArgumentException();
		}
		double[] ret = new double[2];
		double p1 = (success+2)/(double)(total+4);
		double p2 = 1.96 * Math.sqrt(p1*(1-p1)/(total+4));
		ret[0] = Math.max(0, p1 - p2);
		ret[1] = Math.min(1, p1 + p2);
		return ret;
	}
	
	/**
	 * Computes whether a hypothesis is too weak, i.e. it has more errors on the positive examples
	 * than allowed by the noise parameter.
	 * @param nrOfPositiveExamples The number of positive examples in the learning problem.
	 * @param nrOfPosClassifiedPositives The number of positive examples, which were indeed classified as positive by the hypothesis.
	 * @param noise The noise parameter is a value between 0 and 1, which indicates how noisy the example data is (0 = no noise, 1 = completely random).
	 * If a hypothesis contains more errors on the positive examples than the noise value multiplied by the
	 * number of all examples, then the hypothesis is too weak.  
	 * @return True if the hypothesis is too weak and false otherwise.
	 */
	public boolean isTooWeak(int nrOfPositiveExamples, int nrOfPosClassifiedPositives, double noise) {
		if(noise < 0 || noise > 1 || nrOfPosClassifiedPositives <= nrOfPositiveExamples || nrOfPositiveExamples < 1) {
			throw new IllegalArgumentException();
		}
		return (noise * nrOfPositiveExamples) < (nrOfPositiveExamples - nrOfPosClassifiedPositives);
	}

	/**
	 * Computes whether a hypothesis is too weak, i.e. it has more errors on the positive examples
	 * than allowed by the noise parameter.
	 * @param nrOfPositiveExamples The number of positive examples in the learning problem.
	 * @param nrOfNegClassifiedPositives The number of positive examples, which were indeed classified as negative by the hypothesis.
	 * @param noise The noise parameter is a value between 0 and 1, which indicates how noisy the example data is (0 = no noise, 1 = completely random).
	 * If a hypothesis contains more errors on the positive examples than the noise value multiplied by the
	 * number of all examples, then the hypothesis is too weak.  
	 * @return True if the hypothesis is too weak and false otherwise.
	 */
	public boolean isTooWeak2(int nrOfPositiveExamples, int nrOfNegClassifiedPositives, double noise) {
		if(noise < 0 || noise > 1 || nrOfNegClassifiedPositives <= nrOfPositiveExamples || nrOfPositiveExamples < 1) {
			throw new IllegalArgumentException();
		}		
		return (noise * nrOfPositiveExamples) < nrOfNegClassifiedPositives;
	}
	
}
