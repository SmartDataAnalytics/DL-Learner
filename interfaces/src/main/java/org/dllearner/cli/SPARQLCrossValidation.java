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
package org.dllearner.cli;

import com.google.common.collect.Sets;
import org.dllearner.algorithms.qtl.QTL2Disjunctive;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl.LiteralNodeSubsumptionStrategy;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.dllearner.core.AbstractClassExpressionLearningProblem;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.IndividualReasoner;
import org.dllearner.learningproblems.Heuristics;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.learningproblems.PosOnlyLP;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.utilities.Files;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;
import org.dllearner.utilities.statistics.Stat;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;

import java.io.File;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Performs cross validation for the given problem. Supports
 * k-fold cross-validation and leave-one-out cross-validation.
 * 
 * @author Jens Lehmann
 *
 */
public class SPARQLCrossValidation {

	// statistical values
	protected Stat runtime = new Stat();
	protected Stat accuracy = new Stat();
	protected Stat length = new Stat();
	protected Stat accuracyTraining = new Stat();
	protected Stat fMeasure = new Stat();
	protected Stat fMeasureTraining = new Stat();
	protected static boolean writeToFile = false;
	protected static File outputFile;
	
	
	protected Stat trainingCompletenessStat = new Stat();
	protected Stat trainingCorrectnessStat = new Stat();
	
	protected Stat testingCompletenessStat = new Stat();
	protected Stat testingCorrectnessStat = new Stat();
	
	LiteralNodeSubsumptionStrategy literalNodeSubsumptionStrategy = LiteralNodeSubsumptionStrategy.INTERVAL;
	
	public SPARQLCrossValidation() {
		
	}
	
	public SPARQLCrossValidation(QTL2Disjunctive la, AbstractClassExpressionLearningProblem lp, IndividualReasoner rs, int folds, boolean leaveOneOut) {
		
		DecimalFormat df = new DecimalFormat();

		// the training and test sets used later on
		List<Set<OWLIndividual>> trainingSetsPos = new LinkedList<>();
		List<Set<OWLIndividual>> trainingSetsNeg = new LinkedList<>();
		List<Set<OWLIndividual>> testSetsPos = new LinkedList<>();
		List<Set<OWLIndividual>> testSetsNeg = new LinkedList<>();
		
			// get examples and shuffle them too
		Set<OWLIndividual> posExamples;
		Set<OWLIndividual> negExamples;
			if(lp instanceof PosNegLP){
				posExamples = ((PosNegLP)lp).getPositiveExamples();
				negExamples = ((PosNegLP)lp).getNegativeExamples();
			} else if(lp instanceof PosOnlyLP){
				posExamples = ((PosNegLP)lp).getPositiveExamples();
				negExamples = new HashSet<>();
			} else {
				throw new IllegalArgumentException("Only PosNeg and PosOnly learning problems are supported");
			}
			List<OWLIndividual> posExamplesList = new LinkedList<>(posExamples);
			List<OWLIndividual> negExamplesList = new LinkedList<>(negExamples);
			Collections.shuffle(posExamplesList, new Random(1));
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
					Set<OWLIndividual> testPos = getTestingSet(posExamplesList, splitsPos, i);
					Set<OWLIndividual> testNeg = getTestingSet(negExamplesList, splitsNeg, i);
					testSetsPos.add(i, testPos);
					testSetsNeg.add(i, testNeg);
					trainingSetsPos.add(i, getTrainingSet(posExamples, testPos));
					trainingSetsNeg.add(i, getTrainingSet(negExamples, testNeg));
				}
				
			}

		// run the algorithm
		for(int currFold=0; currFold<folds; currFold++) {

			Set<String> pos = Helper.getStringSet(trainingSetsPos.get(currFold));
			Set<String> neg = Helper.getStringSet(trainingSetsNeg.get(currFold));
			if(lp instanceof PosNegLP){
				((PosNegLP)lp).setPositiveExamples(trainingSetsPos.get(currFold));
				((PosNegLP)lp).setNegativeExamples(trainingSetsNeg.get(currFold));
			} else if(lp instanceof PosOnlyLP){
				((PosOnlyLP)lp).setPositiveExamples(new TreeSet<>(trainingSetsPos.get(currFold)));
			}
			

			try {
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
			
			OWLClassExpression concept = la.getCurrentlyBestDescription();
			System.out.println(concept);
//			Set<OWLIndividual> tmp = rs.hasType(concept, testSetsPos.get(currFold));
			Set<OWLIndividual> tmp = hasType(testSetsPos.get(currFold), la);
			Set<OWLIndividual> tmp2 = Sets.difference(testSetsPos.get(currFold), tmp);
//			Set<OWLIndividual> tmp3 = rs.hasType(concept, testSetsNeg.get(currFold));
			Set<OWLIndividual> tmp3 = hasType(testSetsNeg.get(currFold), la);
			
			outputWriter("test set errors pos: " + tmp2);
			outputWriter("test set errors neg: " + tmp3);
			
			// calculate training accuracies
			System.out.println(getCorrectPosClassified(rs, concept, trainingSetsPos.get(currFold)));
//			int trainingCorrectPosClassified = getCorrectPosClassified(rs, concept, trainingSetsPos.get(currFold));
			int trainingCorrectPosClassified = getCorrectPosClassified(trainingSetsPos.get(currFold), la);
//			int trainingCorrectNegClassified = getCorrectNegClassified(rs, concept, trainingSetsNeg.get(currFold));
			int trainingCorrectNegClassified = getCorrectNegClassified(trainingSetsNeg.get(currFold), la);
			int trainingCorrectExamples = trainingCorrectPosClassified + trainingCorrectNegClassified;
			double trainingAccuracy = 100*((double)trainingCorrectExamples/(trainingSetsPos.get(currFold).size()+
					trainingSetsNeg.get(currFold).size()));
			accuracyTraining.addNumber(trainingAccuracy);
			// calculate test accuracies
//			int correctPosClassified = getCorrectPosClassified(rs, concept, testSetsPos.get(currFold));
			int correctPosClassified = getCorrectPosClassified(testSetsPos.get(currFold), la);
//			int correctNegClassified = getCorrectNegClassified(rs, concept, testSetsNeg.get(currFold));
			int correctNegClassified = getCorrectNegClassified(testSetsNeg.get(currFold), la);
			int correctExamples = correctPosClassified + correctNegClassified;
			double currAccuracy = 100*((double)correctExamples/(testSetsPos.get(currFold).size()+
					testSetsNeg.get(currFold).size()));
			accuracy.addNumber(currAccuracy);
			// calculate training F-Score
//			int negAsPosTraining = rs.hasType(concept, trainingSetsNeg.get(currFold)).size();
			int negAsPosTraining = trainingSetsNeg.get(currFold).size() - trainingCorrectNegClassified;
			double precisionTraining = trainingCorrectPosClassified + negAsPosTraining == 0 ? 0 : trainingCorrectPosClassified / (double) (trainingCorrectPosClassified + negAsPosTraining);
			double recallTraining = trainingCorrectPosClassified / (double) trainingSetsPos.get(currFold).size();
			fMeasureTraining.addNumber(100*Heuristics.getFScore(recallTraining, precisionTraining));
			// calculate test F-Score
//			int negAsPos = rs.hasType(concept, testSetsNeg.get(currFold)).size();
			int negAsPos = testSetsNeg.get(currFold).size() - correctNegClassified;
			double precision = correctPosClassified + negAsPos == 0 ? 0 : correctPosClassified / (double) (correctPosClassified + negAsPos);
			double recall = correctPosClassified / (double) testSetsPos.get(currFold).size();
//			System.out.println(precision);System.out.println(recall);
			fMeasure.addNumber(100*Heuristics.getFScore(recall, precision));
			
			length.addNumber(OWLClassExpressionUtils.getLength(concept));
			
			outputWriter("fold " + currFold + ":");
			outputWriter("  training: " + pos.size() + " positive and " + neg.size() + " negative examples");
			outputWriter("  testing: " + correctPosClassified + "/" + testSetsPos.get(currFold).size() + " correct positives, "
					+ correctNegClassified + "/" + testSetsNeg.get(currFold).size() + " correct negatives");
			outputWriter("  concept: " + concept);
			outputWriter("  accuracy: " + df.format(currAccuracy) + "% (" + df.format(trainingAccuracy) + "% on training set)");
			outputWriter("  length: " + df.format(OWLClassExpressionUtils.getLength(concept)));
			outputWriter("  runtime: " + df.format(algorithmDuration/(double)1000000000) + "s");
					
		}
		
		outputWriter("");
		outputWriter("Finished " + folds + "-folds cross-validation.");
		outputWriter("runtime: " + statOutput(df, runtime, "s"));
		outputWriter("length: " + statOutput(df, length, ""));
		outputWriter("F-Measure on training set: " + statOutput(df, fMeasureTraining, "%"));
		outputWriter("F-Measure: " + statOutput(df, fMeasure, "%"));
		outputWriter("predictive accuracy on training set: " + statOutput(df, accuracyTraining, "%"));
		outputWriter("predictive accuracy: " + statOutput(df, accuracy, "%"));
			
	}
	
	protected int getCorrectPosClassified(IndividualReasoner rs, OWLClassExpression concept, Set<OWLIndividual> testSetPos) {
		return rs.hasType(concept, testSetPos).size();
	}
	
	protected Set<OWLIndividual> hasType(Set<OWLIndividual> individuals, QTL2Disjunctive qtl) {
		Set<OWLIndividual> coveredIndividuals = new HashSet<>();
		RDFResourceTree solutionTree = qtl.getBestSolution().getTree();
		
		for (OWLIndividual ind : individuals) {
			throw new RuntimeException("Not implemented yet.");
		}
		return coveredIndividuals;
	}
	
	protected int getCorrectPosClassified(Set<OWLIndividual> testSetPos, QTL2Disjunctive qtl) {
		return qtl.getBestSolution().getTreeScore().getCoveredPositives().size();
	}
	
	protected int getCorrectNegClassified(SPARQLReasoner rs, OWLClassExpression concept, Set<OWLIndividual> testSetNeg) {
		return testSetNeg.size() - rs.hasType(concept, testSetNeg).size();
	}
	
	protected int getCorrectNegClassified(Set<OWLIndividual> testSetNeg, QTL2Disjunctive qtl) {
		return qtl.getBestSolution().getTreeScore().getNotCoveredNegatives().size();
	}
	
	public static Set<OWLIndividual> getTestingSet(List<OWLIndividual> examples, int[] splits, int fold) {
		int fromIndex;
		// we either start from 0 or after the last fold ended
		if(fold == 0)
			fromIndex = 0;
		else
			fromIndex = splits[fold-1];
		// the split corresponds to the ends of the folds
		int toIndex = splits[fold];
		
//		System.out.println("from " + fromIndex + " to " + toIndex);
		
		Set<OWLIndividual> testingSet = new HashSet<>();
		// +1 because 2nd element is exclusive in subList method
		testingSet.addAll(examples.subList(fromIndex, toIndex));
		return testingSet;
	}
	
	public static Set<OWLIndividual> getTrainingSet(Set<OWLIndividual> examples, Set<OWLIndividual> testingSet) {
		return Sets.difference(examples, testingSet);
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
	
	protected void outputWriter(String output) {
		if(writeToFile) {
			Files.appendToFile(outputFile, output +"\n");
			System.out.println(output);
		} else {
			System.out.println(output);
		}
		
	}

	public Stat getfMeasure() {
		return fMeasure;
	}

	public Stat getfMeasureTraining() {
		return fMeasureTraining;
	}

}
