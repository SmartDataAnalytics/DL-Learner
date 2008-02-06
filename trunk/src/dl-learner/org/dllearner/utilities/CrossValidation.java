/**
 * Copyright (C) 2007-2008, Jens Lehmann
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

import java.io.File;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.cli.Start;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.dl.Concept;
import org.dllearner.core.dl.Individual;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.learningproblems.PosOnlyLP;

/**
 * Performs cross validation for the given problem. Supports
 * k-fold cross-validation and leave-one-out cross-validation.
 * 
 * @author Jens Lehmann
 *
 */
public class CrossValidation {

	private static Logger logger = Logger.getRootLogger();	
	
	public static void main(String[] args) {
		File file = new File(args[0]);
		
		boolean leaveOneOut = false;
		int folds = 10;
		
		// use second argument as number of folds; if not specified
		// leave one out cross validation is used
		if(args.length > 1)
			folds = Integer.parseInt(args[1]);
		else
			leaveOneOut = true;
		
		// create logger (a simple logger which outputs
		// its messages to the console)
		SimpleLayout layout = new SimpleLayout();
		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		logger.removeAllAppenders();
		logger.addAppender(consoleAppender);
		logger.setLevel(Level.WARN);		
		
		new CrossValidation(file, folds, leaveOneOut);
		
	}
	
	public CrossValidation(File file, int folds, boolean leaveOneOut) {
	
		DecimalFormat df = new DecimalFormat();	
		ComponentManager cm = ComponentManager.getInstance();
		
		// the first read of the file is used to detect the examples
		// and set up the splits correctly according to our validation
		// method
		Start start = new Start(file);
		
		LearningProblem lp = start.getLearningProblem();
		ReasoningService rs = start.getReasoningService();

		// the training and test sets used later on
		List<Set<Individual>> trainingSetsPos = new LinkedList<Set<Individual>>();
		List<Set<Individual>> trainingSetsNeg = new LinkedList<Set<Individual>>();
		List<Set<Individual>> testSetsPos = new LinkedList<Set<Individual>>();
		List<Set<Individual>> testSetsNeg = new LinkedList<Set<Individual>>();
		
		if(lp instanceof PosNegLP) {

			Set<Individual> posExamples = ((PosNegLP)lp).getPositiveExamples();
			List<Individual> posExamplesList = new LinkedList<Individual>(posExamples);
			Set<Individual> negExamples = ((PosNegLP)lp).getNegativeExamples();
			List<Individual> negExamplesList = new LinkedList<Individual>(negExamples);
			
			// sanity check whether nr. of folds makes sense for this benchmark
			if(!leaveOneOut && (posExamples.size()<folds || negExamples.size()<folds)) {
				System.out.println("The number of folds is higher than the number of "
						+ "positive/negative examples. This can result in empty test sets. Exiting.");
				System.exit(0);
			}
			
			if(leaveOneOut) {
				// note that leave-one-out is not identical to k-fold with
				// k = nr. of examples in the current implementation, because
				// with n folds and n examples there is no guarantee that a fold
				// is never empty (this is an implementation issue)
				int nrOfExamples = posExamples.size() + negExamples.size();
				for(int i = 0; i < nrOfExamples; i++) {
					// ...
				}
				System.out.println("Leave-one-out not supported yet.");
				System.exit(1);
			} else {
				// calculating where to split the sets, ; note that we split
				// positive and negative examples separately such that the 
				// distribution of positive and negative examples remains similar
				// (note that there better but more complex ways to implement this,
				// which guarantee that the sum of the elements of a fold for pos
				// and neg differs by at most 1 - it can differ by 2 in our implementation,
				// e.g. with 3 folds, 4 pos. examples, 4 neg. examples)
				int[] splitsPos = calculateSplits(posExamples.size(),folds);
				int[] splitsNeg = calculateSplits(negExamples.size(),folds);
				
				// calculating training and test sets
				for(int i=0; i<folds; i++) {
					Set<Individual> testPos = getTestingSet(posExamplesList, splitsPos, i);
					Set<Individual> testNeg = getTestingSet(negExamplesList, splitsNeg, i);
					testSetsPos.add(i, testPos);
					testSetsNeg.add(i, testNeg);
					trainingSetsPos.add(i, getTrainingSet(posExamples, testPos));
					trainingSetsNeg.add(i, getTrainingSet(negExamples, testNeg));				
				}	
				
			}
			
		} else if(lp instanceof PosOnlyLP) {
			System.out.println("Cross validation for positive only learning not supported yet.");
			System.exit(0);
			// Set<Individual> posExamples = ((PosOnlyLP)lp).getPositiveExamples();
			// int[] splits = calculateSplits(posExamples.size(),folds);
		} else {
			System.out.println("Cross validation for learning problem " + lp + " not supported.");
			System.exit(0);
		}
		
		// statistical values
		Stat runtime = new Stat();
		Stat accuracy = new Stat();
		Stat length = new Stat();
		
		// run the algorithm
		for(int currFold=0; currFold<folds; currFold++) {
			// we always perform a full initialisation to make sure that
			// no objects are reused
			start = new Start(file);
			lp = start.getLearningProblem();
			Set<String> pos = Datastructures.individualSetToStringSet(trainingSetsPos.get(currFold));
			Set<String> neg = Datastructures.individualSetToStringSet(trainingSetsNeg.get(currFold));
			cm.applyConfigEntry(lp, "positiveExamples", pos);
			cm.applyConfigEntry(lp, "negativeExamples", neg);
			
			LearningAlgorithm la = start.getLearningAlgorithm();
			long algorithmStartTime = System.nanoTime();
			la.start();
			long algorithmDuration = System.nanoTime() - algorithmStartTime;
			runtime.addNumber(algorithmDuration/(double)1000000000);
			
			Concept concept = la.getBestSolution();
			int correctExamples = getCorrectPosClassified(rs, concept, testSetsPos.get(currFold))
			+ getCorrectNegClassified(rs, concept, testSetsNeg.get(currFold));
			double currAccuracy = 100*((double)correctExamples/(testSetsPos.get(currFold).size()+
					testSetsNeg.get(currFold).size()));
			accuracy.addNumber(currAccuracy);
			
			length.addNumber(concept.getLength());
			
			System.out.println("fold " + currFold + " (" + file + "):");
			System.out.println("  concept: " + concept);
			System.out.println("  accuracy: " + df.format(currAccuracy) + "%");
			System.out.println("  length: " + df.format(concept.getLength()));
			System.out.println("  runtime: " + df.format(algorithmDuration/(double)1000000000) + "s");
		}
		
		System.out.println();
		System.out.println("Finished " + folds + "-folds cross-validation on " + file + ".");
		System.out.println("runtime: " + statOutput(df, runtime, "s"));
		System.out.println("length: " + statOutput(df, length, ""));
		System.out.println("accuracy: " + statOutput(df, accuracy, "%"));
		
	}
	
	private int getCorrectPosClassified(ReasoningService rs, Concept concept, Set<Individual> posClassified) {
		return rs.instanceCheck(concept, posClassified).size();
	}
	
	private int getCorrectNegClassified(ReasoningService rs, Concept concept, Set<Individual> negClassified) {
		return negClassified.size() - rs.instanceCheck(concept, negClassified).size();
	}
	
	private Set<Individual> getTestingSet(List<Individual> examples, int[] splits, int fold) {
		int fromIndex;
		// we either start from 0 or after the last fold ended
		if(fold == 0)
			fromIndex = 0;
		else
			fromIndex = splits[fold-1];
		// the split corresponds to the ends of the folds
		int toIndex = splits[fold];
		
		Set<Individual> testingSet = new HashSet<Individual>();
		// +1 because 2nd element is exclusive in subList method
		testingSet.addAll(examples.subList(fromIndex, toIndex));
		return testingSet;
	}
	
	private Set<Individual> getTrainingSet(Set<Individual> examples, Set<Individual> testingSet) {
		return Helper.difference(examples, testingSet);
	}
	
	// takes nr. of examples and the nr. of folds for this examples;
	// returns an array which says where each fold ends, i.e.
	// splits[i] is the index of the last element of fold i in the examples
	private int[] calculateSplits(int nrOfExamples, int folds) {
		int[] splits = new int[folds];
		for(int i=1; i<=folds; i++) {
			// we always round up to the next integer
			splits[i-1] = (int)Math.ceil(i*nrOfExamples/(double)folds);
		}
		return splits;
	}
	
	private String statOutput(DecimalFormat df, Stat stat, String unit) {
		String str = "av. " + df.format(stat.getMean()) + unit;
		str += " (deviation " + df.format(stat.getStandardDeviation()) + unit + "; ";
		str += "min " + df.format(stat.getMin()) + unit + "; ";
		str += "max " + df.format(stat.getMax()) + unit + ")";		
		return str;
	}

}
