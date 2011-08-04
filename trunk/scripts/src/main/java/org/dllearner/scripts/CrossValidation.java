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
package org.dllearner.scripts;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.cli.Start;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.learningproblems.Heuristics;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.learningproblems.PosOnlyLP;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.datastructures.Datastructures;
import org.dllearner.utilities.statistics.Stat;
import org.dllearner.utilities.Files;

/**
 * Performs cross validation for the given problem. Supports
 * k-fold cross-validation and leave-one-out cross-validation.
 * 
 * @author Jens Lehmann
 *
 */
public class CrossValidation {

	private static Logger logger = Logger.getRootLogger();	
	
	// statistical values
	private Stat runtime = new Stat();
	private Stat accuracy = new Stat();
	private Stat length = new Stat();
	private Stat accuracyTraining = new Stat();
	private Stat fMeasure = new Stat();
	private Stat fMeasureTraining = new Stat();
	private static boolean writeToFile = false;
	private static File outputFile;
	
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
		
		if(args.length > 2) {
			writeToFile = true;
			outputFile = new File(args[2]);
		}
		
		if(folds < 2) {
			System.out.println("At least 2 fold needed.");
			System.exit(0);
		}
		
		// create logger (a simple logger which outputs
		// its messages to the console)
		SimpleLayout layout = new SimpleLayout();
		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		logger.removeAllAppenders();
		logger.addAppender(consoleAppender);
		logger.setLevel(Level.WARN);
		// disable OWL API info output
		java.util.logging.Logger.getLogger("").setLevel(java.util.logging.Level.WARNING);
		
		new CrossValidation(file, folds, leaveOneOut);
		
	}
	
	public CrossValidation(File file, int folds, boolean leaveOneOut) {
		this(file, folds, leaveOneOut, null);
	}
			
	public CrossValidation(File file, int folds, boolean leaveOneOut, AbstractCELA la) {		
		
		DecimalFormat df = new DecimalFormat();	
		ComponentManager cm = ComponentManager.getInstance();
		
		// the first read of the file is used to detect the examples
		// and set up the splits correctly according to our validation
		// method
		Start start = null;
		try {
			start = new Start(file);
		} catch (ComponentInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (org.dllearner.confparser.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		AbstractLearningProblem lp = start.getLearningProblem();
//		ReasonerComponent rs = start.getReasonerComponent();
//		start.getReasonerComponent().releaseKB();

		// the training and test sets used later on
		List<Set<Individual>> trainingSetsPos = new LinkedList<Set<Individual>>();
		List<Set<Individual>> trainingSetsNeg = new LinkedList<Set<Individual>>();
		List<Set<Individual>> testSetsPos = new LinkedList<Set<Individual>>();
		List<Set<Individual>> testSetsNeg = new LinkedList<Set<Individual>>();
		
		if(lp instanceof PosNegLP) {

			// get examples and shuffle them too
			Set<Individual> posExamples = ((PosNegLP)lp).getPositiveExamples();
			List<Individual> posExamplesList = new LinkedList<Individual>(posExamples);
			Collections.shuffle(posExamplesList, new Random(1));			
			Set<Individual> negExamples = ((PosNegLP)lp).getNegativeExamples();
			List<Individual> negExamplesList = new LinkedList<Individual>(negExamples);
			Collections.shuffle(negExamplesList, new Random(2));
			
			// sanity check whether nr. of folds makes sense for this benchmark
			if(!leaveOneOut && (posExamples.size()<folds && negExamples.size()<folds)) {
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
				// (note that there are better but more complex ways to implement this,
				// which guarantee that the sum of the elements of a fold for pos
				// and neg differs by at most 1 - it can differ by 2 in our implementation,
				// e.g. with 3 folds, 4 pos. examples, 4 neg. examples)
				int[] splitsPos = calculateSplits(posExamples.size(),folds);
				int[] splitsNeg = calculateSplits(negExamples.size(),folds);
				
//				System.out.println(splitsPos[0]);
//				System.out.println(splitsNeg[0]);
				
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

		// run the algorithm
		for(int currFold=0; currFold<folds; currFold++) {
			// we always perform a full initialisation to make sure that
			// no objects are reused
			try {
				start = new Start(file);
			} catch (ComponentInitException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (org.dllearner.confparser.ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			lp = start.getLearningProblem();
			Set<String> pos = Datastructures.individualSetToStringSet(trainingSetsPos.get(currFold));
			Set<String> neg = Datastructures.individualSetToStringSet(trainingSetsNeg.get(currFold));
			cm.applyConfigEntry(lp, "positiveExamples", pos);
			cm.applyConfigEntry(lp, "negativeExamples", neg);
//			System.out.println("pos: " + pos.size());
//			System.out.println("neg: " + neg.size());
//			System.exit(0);
			
			la = start.getLearningAlgorithm();
			// init again, because examples have changed
			try {
//				start.getReasonerComponent().init();				
				lp.init();
				la.init();
			} catch (ComponentInitException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			long algorithmStartTime = System.nanoTime();
			la.start();
			long algorithmDuration = System.nanoTime() - algorithmStartTime;
			runtime.addNumber(algorithmDuration/(double)1000000000);
			
			Description concept = la.getCurrentlyBestDescription();
			
			AbstractReasonerComponent rs = start.getReasonerComponent();
			Set<Individual> tmp = rs.hasType(concept, testSetsPos.get(currFold));
			Set<Individual> tmp2 = Helper.difference(testSetsPos.get(currFold), tmp);
			Set<Individual> tmp3 = rs.hasType(concept, testSetsNeg.get(currFold));
			
			outputWriter("test set errors pos: " + tmp2);
			outputWriter("test set errors neg: " + tmp3);
			
			// calculate training accuracies 
			int trainingCorrectPosClassified = getCorrectPosClassified(rs, concept, trainingSetsPos.get(currFold));
			int trainingCorrectNegClassified = getCorrectNegClassified(rs, concept, trainingSetsNeg.get(currFold));
			int trainingCorrectExamples = trainingCorrectPosClassified + trainingCorrectNegClassified;
			double trainingAccuracy = 100*((double)trainingCorrectExamples/(trainingSetsPos.get(currFold).size()+
					trainingSetsNeg.get(currFold).size()));			
			accuracyTraining.addNumber(trainingAccuracy);
			// calculate test accuracies
			int correctPosClassified = getCorrectPosClassified(rs, concept, testSetsPos.get(currFold));
			int correctNegClassified = getCorrectNegClassified(rs, concept, testSetsNeg.get(currFold));
			int correctExamples = correctPosClassified + correctNegClassified;
			double currAccuracy = 100*((double)correctExamples/(testSetsPos.get(currFold).size()+
					testSetsNeg.get(currFold).size()));
			accuracy.addNumber(currAccuracy);
			// calculate training F-Score
			int negAsPosTraining = rs.hasType(concept, trainingSetsNeg.get(currFold)).size();
			double precisionTraining = trainingCorrectPosClassified + negAsPosTraining == 0 ? 0 : trainingCorrectPosClassified / (double) (trainingCorrectPosClassified + negAsPosTraining);
			double recallTraining = trainingCorrectPosClassified / (double) trainingSetsPos.get(currFold).size();
			fMeasureTraining.addNumber(100*Heuristics.getFScore(recallTraining, precisionTraining));
			// calculate test F-Score
			int negAsPos = rs.hasType(concept, testSetsNeg.get(currFold)).size();
			double precision = correctPosClassified + negAsPos == 0 ? 0 : correctPosClassified / (double) (correctPosClassified + negAsPos);
			double recall = correctPosClassified / (double) testSetsPos.get(currFold).size();
//			System.out.println(precision);System.out.println(recall);
			fMeasure.addNumber(100*Heuristics.getFScore(recall, precision));			
			
			length.addNumber(concept.getLength());
			
			outputWriter("fold " + currFold + " (" + file + "):");
			outputWriter("  training: " + pos.size() + " positive and " + neg.size() + " negative examples");
			outputWriter("  testing: " + correctPosClassified + "/" + testSetsPos.get(currFold).size() + " correct positives, " 
					+ correctNegClassified + "/" + testSetsNeg.get(currFold).size() + " correct negatives");
			outputWriter("  concept: " + concept);
			outputWriter("  accuracy: " + df.format(currAccuracy) + "% (" + df.format(trainingAccuracy) + "% on training set)");
			outputWriter("  length: " + df.format(concept.getLength()));
			outputWriter("  runtime: " + df.format(algorithmDuration/(double)1000000000) + "s");
			
			// free all resources
			rs.releaseKB();
			cm.freeAllComponents();			
		}
		
		outputWriter("");
		outputWriter("Finished " + folds + "-folds cross-validation on " + file + ".");
		outputWriter("runtime: " + statOutput(df, runtime, "s"));
		outputWriter("length: " + statOutput(df, length, ""));
		outputWriter("F-Measure on training set: " + statOutput(df, fMeasureTraining, "%"));		
		outputWriter("F-Measure: " + statOutput(df, fMeasure, "%"));
		outputWriter("predictive accuracy on training set: " + statOutput(df, accuracyTraining, "%"));		
		outputWriter("predictive accuracy: " + statOutput(df, accuracy, "%"));
			
	}
	
	private int getCorrectPosClassified(AbstractReasonerComponent rs, Description concept, Set<Individual> testSetPos) {
		return rs.hasType(concept, testSetPos).size();
	}
	
	private int getCorrectNegClassified(AbstractReasonerComponent rs, Description concept, Set<Individual> testSetNeg) {
		return testSetNeg.size() - rs.hasType(concept, testSetNeg).size();
	}
	
	public static Set<Individual> getTestingSet(List<Individual> examples, int[] splits, int fold) {
		int fromIndex;
		// we either start from 0 or after the last fold ended
		if(fold == 0)
			fromIndex = 0;
		else
			fromIndex = splits[fold-1];
		// the split corresponds to the ends of the folds
		int toIndex = splits[fold];
		
//		System.out.println("from " + fromIndex + " to " + toIndex);
		
		Set<Individual> testingSet = new HashSet<Individual>();
		// +1 because 2nd element is exclusive in subList method
		testingSet.addAll(examples.subList(fromIndex, toIndex));
		return testingSet;
	}
	
	public static Set<Individual> getTrainingSet(Set<Individual> examples, Set<Individual> testingSet) {
		return Helper.difference(examples, testingSet);
	}
	
	// takes nr. of examples and the nr. of folds for this examples;
	// returns an array which says where each fold ends, i.e.
	// splits[i] is the index of the last element of fold i in the examples
	public static int[] calculateSplits(int nrOfExamples, int folds) {
		int[] splits = new int[folds];
		for(int i=1; i<=folds; i++) {
			// we always round up to the next integer
			splits[i-1] = (int)Math.ceil(i*nrOfExamples/(double)folds);
		}
		return splits;
	}
	
	public static String statOutput(DecimalFormat df, Stat stat, String unit) {
		String str = "av. " + df.format(stat.getMean()) + unit;
		str += " (deviation " + df.format(stat.getStandardDeviation()) + unit + "; ";
		str += "min " + df.format(stat.getMin()) + unit + "; ";
		str += "max " + df.format(stat.getMax()) + unit + ")";		
		return str;
	}

	public Stat getAccuracy() {
		return accuracy;
	}

	public Stat getLength() {
		return length;
	}

	public Stat getRuntime() {
		return runtime;
	}
	
	private void outputWriter(String output) {
		if(writeToFile) {
			Files.appendFile(outputFile, output +"\n");
			System.out.println(output);
		} else {
			System.out.println(output);
		}
		
	}

}
