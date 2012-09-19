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

	public static enum HeuristicType { PRED_ACC, AMEASURE, JACCARD, FMEASURE, GEN_FMEASURE };	
	
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
	
	public static double getPredictiveAccuracy(int nrOfExamples, int nrOfPosClassifiedPositives, int nrOfNegClassifiedNegatives) {
		return (nrOfPosClassifiedPositives + nrOfNegClassifiedNegatives) / (double) nrOfExamples;
	}	

	public static double getPredictiveAccuracy(int nrOfPosExamples, int nrOfNegExamples, int nrOfPosClassifiedPositives, int nrOfNegClassifiedNegatives, double beta) {
		return (nrOfPosClassifiedPositives + beta * nrOfNegClassifiedNegatives) / (double) (nrOfPosExamples + beta * nrOfNegExamples);
	}		
	
	public static double getPredictiveAccuracy2(int nrOfExamples, int nrOfPosClassifiedPositives, int nrOfPosClassifiedNegatives) {
		return (nrOfPosClassifiedPositives + nrOfExamples - nrOfPosClassifiedNegatives) / (double) nrOfExamples;
	}
	
	public static double getPredictiveAccuracy2(int nrOfPosExamples, int nrOfNegExamples, int nrOfPosClassifiedPositives, int nrOfNegClassifiedNegatives, double beta) {
		return (nrOfPosClassifiedPositives + beta * nrOfNegClassifiedNegatives) / (double) (nrOfPosExamples + beta * nrOfNegExamples);
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
	public static double[] getConfidenceInterval95Wald(int total, int success) {
		if(success > total || total < 1) {
			throw new IllegalArgumentException("95% confidence interval for " + success + " out of " + total + " trials cannot be estimated.");
		}
		double[] ret = new double[2];
		double p1 = (success+2)/(double)(total+4);
		double p2 = 1.96 * Math.sqrt(p1*(1-p1)/(total+4));
		ret[0] = Math.max(0, p1 - p2);
		ret[1] = Math.min(1, p1 + p2);
		return ret;
	}
	
	/**
	 * Computes the 95% confidence interval average of an experiment with boolean outcomes,
	 * e.g. heads or tails coin throws. It uses the very efficient, but still accurate
	 * Wald method. 
	 * @param success Number of successes, e.g. number of times the coin shows head.
	 * @param total Total number of tries, e.g. total number of times the coin was thrown.
	 * @return The average of the lower border and upper border of the 95% confidence interval.
	 */
	public static double getConfidenceInterval95WaldAverage(int total, int success) {
		if(success > total || total < 1) {
			throw new IllegalArgumentException("95% confidence interval for " + success + " out of " + total + " trials cannot be estimated.");
		}
		double[] interval = getConfidenceInterval95Wald(total, success);
		return (interval[0] + interval[1]) / 2;
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

	/**
	 * This method can be used to approximate F-Measure and thereby saving a lot of 
	 * instance checks. It assumes that all positive examples (or instances of a class)
	 * have already been tested via instance checks, i.e. recall is already known and
	 * precision is approximated.
	 * @param nrOfPosClassifiedPositives Positive examples (instance of a class), which are classified as positives.
	 * @param recall The already known recall.
	 * @param beta Weights precision and recall. If beta is >1, then recall is more important
	 * than precision.
	 * @param nrOfRelevantInstances Number of relevant instances, i.e. number of instances, which
	 * would have been tested without approximations. TODO: relevant = pos + neg examples?
	 * @param nrOfInstanceChecks Performed instance checks for the approximation.
	 * @param nrOfSuccessfulInstanceChecks Number of successful performed instance checks.
	 * @return A two element array, where the first element is the computed F-beta score and the
	 * second element is the length of the 95% confidence interval around it.
	 */
	public static double[] getFScoreApproximation(int nrOfPosClassifiedPositives, double recall, double beta, int nrOfRelevantInstances, int nrOfInstanceChecks, int nrOfSuccessfulInstanceChecks) {
		// compute 95% confidence interval
		double[] interval = Heuristics.getConfidenceInterval95Wald(nrOfInstanceChecks, nrOfSuccessfulInstanceChecks);
		// multiply by number of instances from which the random samples are drawn
		double lowerBorder = interval[0] * nrOfRelevantInstances;
		double upperBorder = interval[1] * nrOfRelevantInstances;
		// compute F-Measure for both borders (lower value = higher F-Measure)
		double fMeasureHigh = (1 + Math.sqrt(beta)) * (nrOfPosClassifiedPositives/(nrOfPosClassifiedPositives+lowerBorder)*recall) / (Math.sqrt(beta)*nrOfPosClassifiedPositives/(nrOfPosClassifiedPositives+lowerBorder)+recall);
		double fMeasureLow = (1 + Math.sqrt(beta)) * (nrOfPosClassifiedPositives/(nrOfPosClassifiedPositives+upperBorder)*recall) / (Math.sqrt(beta)*nrOfPosClassifiedPositives/(nrOfPosClassifiedPositives+upperBorder)+recall);
		double diff = fMeasureHigh - fMeasureLow;
		// compute F-score for proportion ?
		// double proportionInstanceChecks = successfulInstanceChecks / (double) nrOfInstanceChecks * nrOfRelevantInstances; // 
		// => don't do it for now, because the difference between proportion and center of interval is usually quite small
		// for sufficiently small diffs
		// return interval length and center
		double[] ret = new double[2];
		ret[0] = fMeasureLow + 0.5 * diff;
		ret[1] = diff;
		return ret;
	}
	
	/**
	 * In the first step of the AScore approximation, we estimate recall (taking the factor
	 * beta into account). This is not much more than a wrapper around the modified Wald method.
	 * @param beta Weights precision and recall. If beta is >1, then recall is more important
	 * than precision.
	 * @param nrOfPosExamples Number of positive examples (or instances of the considered class).
	 * @param nrOfInstanceChecks Number of positive examples (or instances of the considered class) which have been checked.
	 * @param nrOfSuccessfulInstanceChecks Number of positive examples (or instances of the considered class), where the instance check returned true.
	 * @return A two element array, where the first element is the recall multiplied by beta and the
	 * second element is the length of the 95% confidence interval around it.
	 */
	public static double[] getAScoreApproximationStep1(double beta, int nrOfPosExamples, int nrOfInstanceChecks, int nrOfSuccessfulInstanceChecks) {
		// the method is just a wrapper around a single confidence interval approximation;
		// method approximates t * a / |R(A)|
		double[] interval = Heuristics.getConfidenceInterval95Wald(nrOfSuccessfulInstanceChecks, nrOfInstanceChecks);
		double diff = beta * (interval[1] - interval[0]);
		double ret[] = new double[2];
		ret[0] = beta * interval[0] + 0.5*diff;
		ret[1] = diff;
		return ret;
	}
	
	/**
	 * In step 2 of the A-Score approximation, the precision and overall A-Score is estimated based on
	 * the estimated recall.
	 * @param nrOfPosClassifiedPositives Positive examples (instance of a class), which are classified as positives.
	 * @param recallInterval The estimated recall, which needs to be given as a two element array with the first element being the mean value and the second element being the length of the interval (to be compatible with the step1 method). 
	 * @param beta Weights precision and recall. If beta is >1, then recall is more important
	 * than precision.
	 * @param nrOfRelevantInstances Number of relevant instances, i.e. number of instances, which
	 * would have been tested without approximations.
	 * @param nrOfInstanceChecks Performed instance checks for the approximation.
	 * @param nrOfSuccessfulInstanceChecks Number of performed instance checks, which returned true.
	 * @return A two element array, where the first element is the estimated A-Score and the
	 * second element is the length of the 95% confidence interval around it.
	 */
	public static double[] getAScoreApproximationStep2(int nrOfPosClassifiedPositives, double[] recallInterval, double beta, int nrOfRelevantInstances, int nrOfInstanceChecks, int nrOfSuccessfulInstanceChecks) {
		// recall interval is given as mean + interval size (to fit the other method calls) => computer lower and upper border
		double recallLowerBorder = (recallInterval[0] - 0.5*recallInterval[1]) / beta;
		double recallUpperBorder = (recallInterval[0] + 0.5*recallInterval[1]) / beta;
		// estimate precision
		double[] interval = Heuristics.getConfidenceInterval95Wald(nrOfInstanceChecks, nrOfSuccessfulInstanceChecks);
		
		double precisionLowerBorder = nrOfPosClassifiedPositives / (nrOfPosClassifiedPositives + interval[1] * nrOfRelevantInstances);
		double precisionUpperBorder = nrOfPosClassifiedPositives / (nrOfPosClassifiedPositives + interval[0] * nrOfRelevantInstances);
		
//		System.out.println("rec low: " + recallLowerBorder);
//		System.out.println("rec up: " + recallUpperBorder);
//		System.out.println("prec low: " + precisionLowerBorder);
//		System.out.println("prec up: " + precisionUpperBorder);
		double lowerBorder = Heuristics.getAScore(recallLowerBorder, precisionLowerBorder, beta);
		double upperBorder = Heuristics.getAScore(recallUpperBorder, precisionUpperBorder, beta);
		double diff = upperBorder - lowerBorder;
		double ret[] = new double[2];
		ret[0] = lowerBorder + 0.5*diff;
		ret[1] = diff;
		return ret;
	}
	
	// WARNING: unstable/untested
	// uses the following formula: (|R(C) \cap E^+| + beta * |E^- \ R(C)|) / (|E^+|+|E^-|)
	// approximates |R(C) \cap E^+| and beta * |E^- \ R(C)| separately; and adds their lower and upper borders (pessimistic estimate)
	// TODO: only works well if there are many negatives at the moment, so speedup is not great
	public static double[] getPredAccApproximation(int nrOfPositiveExamples, int nrOfNegativeExamples, double beta, int nrOfPosExampleInstanceChecks, int nrOfSuccessfulPosExampleChecks, int nrOfNegExampleInstanceChecks, int nrOfNegativeNegExampleChecks) {
		// compute both 95% confidence intervals
		double[] intervalPos = Heuristics.getConfidenceInterval95Wald(nrOfPosExampleInstanceChecks, nrOfSuccessfulPosExampleChecks);
		double[] intervalNeg = Heuristics.getConfidenceInterval95Wald(nrOfNegExampleInstanceChecks, nrOfNegativeNegExampleChecks);
		// multiply by number of instances from which the random samples are drawn
		double lowerBorder = intervalPos[0] * nrOfPositiveExamples + beta * intervalNeg[0] * nrOfNegativeExamples;
		double upperBorder = intervalNeg[1] * nrOfPositiveExamples + beta * intervalNeg[1] * nrOfNegativeExamples;
		double predAccLow = lowerBorder / (double) (nrOfPositiveExamples + beta * nrOfNegativeExamples);
		double predAccHigh = upperBorder / (double) (nrOfPositiveExamples + beta * nrOfNegativeExamples);
		double diff = predAccHigh - predAccLow;
		// return interval length and center
		double[] ret = new double[2];
		ret[0] = predAccLow + 0.5 * diff;
		ret[1] = diff;
		return ret;
	}
	
}
