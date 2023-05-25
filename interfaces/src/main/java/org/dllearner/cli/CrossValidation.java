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
import org.dllearner.core.*;
import org.dllearner.learningproblems.Heuristics;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.learningproblems.PosOnlyLP;
import org.dllearner.utilities.Files;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.owl.ManchesterOWLSyntaxOWLObjectRendererImplExt;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;
import org.dllearner.utilities.statistics.Stat;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Performs cross validation for the given problem. Supports
 * k-fold cross-validation and leave-one-out cross-validation.
 * 
 * @author Jens Lehmann
 *
 */
public class CrossValidation {

	private static final Logger log = LoggerFactory.getLogger(CrossValidation.class);

	// statistical values
	protected Stat runtime = new Stat();
	protected Stat length = new Stat();
	protected Stat accuracy = new Stat();
	protected Stat accuracyTraining = new Stat();
	protected Stat fMeasure = new Stat();
	protected Stat fMeasureTraining = new Stat();

	protected Stat trainingCompletenessStat;
	protected Stat trainingCorrectnessStat;

	protected Stat testingCompletenessStat;
	protected Stat testingCorrectnessStat;

	protected Stat totalNumberOfDescriptions;
	protected Stat minimalDescriptionNeeded;
	protected Stat learningTimeForBestDescription;

	public static boolean writeToFile = false;
	public static File outputFile;
	public static boolean multiThreaded = false;
	
	private static final DecimalFormat df = new DecimalFormat();

	public CrossValidation() {}
	
	public CrossValidation(AbstractCELA la,
						   AbstractClassExpressionLearningProblem lp,
						   final AbstractReasonerComponent rs,
						   int folds,
						   boolean leaveOneOut) {

		if (!(lp instanceof PosNegLP || lp instanceof PosOnlyLP)) {
			throw new IllegalArgumentException("Only PosNeg and PosOnly learning problems are supported");
		}

		// console rendering of class expressions
		ManchesterOWLSyntaxOWLObjectRendererImplExt renderer = new ManchesterOWLSyntaxOWLObjectRendererImplExt();
		StringRenderer.setRenderer(renderer);
		StringRenderer.setShortFormProvider(new SimpleShortFormProvider());
				
		// the training and test sets used later on
		List<Set<OWLIndividual>> trainingSetsPos = new ArrayList<>();
		List<Set<OWLIndividual>> trainingSetsNeg = new ArrayList<>();
		List<Set<OWLIndividual>> testSetsPos = new ArrayList<>();
		List<Set<OWLIndividual>> testSetsNeg = new ArrayList<>();
		
			// get examples and shuffle them too
		Set<OWLIndividual> posExamples;
		Set<OWLIndividual> negExamples;
		if (lp instanceof PosNegLP) {
			posExamples = ((PosNegLP) lp).getPositiveExamples();
			negExamples = ((PosNegLP) lp).getNegativeExamples();
		} else if (lp instanceof PosOnlyLP) {
			posExamples = ((PosOnlyLP) lp).getPositiveExamples();
			negExamples = new HashSet<>();
		} else {
			throw new IllegalArgumentException("Only PosNeg and PosOnly learning problems are supported");
		}

		List<OWLIndividual> posExamplesList = new ArrayList<>(posExamples);
		List<OWLIndividual> negExamplesList = new ArrayList<>(negExamples);
		Collections.shuffle(posExamplesList, new Random(1));
		Collections.shuffle(negExamplesList, new Random(2));

		// sanity check whether nr. of folds makes sense for this benchmark
		if (!leaveOneOut && (posExamples.size() < folds && negExamples.size() < folds)) {
			System.out.println("The number of folds is higher than the number of "
					+ "positive/negative examples. This can result in empty test sets. Exiting.");
			System.exit(0);
		}

		if (leaveOneOut) {
			// note that leave-one-out is not identical to k-fold with
			// k = nr. of examples in the current implementation, because
			// with n folds and n examples there is no guarantee that a fold
			// is never empty (this is an implementation issue)
			int nrOfExamples = posExamples.size() + negExamples.size();
			for (int i = 0; i < nrOfExamples; i++) {
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
			int[] splitsPos = calculateSplits(posExamples.size(), folds);
			int[] splitsNeg = calculateSplits(negExamples.size(), folds);

//				System.out.println(splitsPos[0]);
//				System.out.println(splitsNeg[0]);

			// calculating training and test sets
			for (int i = 0; i < folds; i++) {
				Set<OWLIndividual> testPos = getTestingSet(posExamplesList, splitsPos, i);
				Set<OWLIndividual> testNeg = getTestingSet(negExamplesList, splitsNeg, i);
				testSetsPos.add(i, testPos);
				testSetsNeg.add(i, testNeg);
				trainingSetsPos.add(i, getTrainingSet(posExamples, testPos));
				trainingSetsNeg.add(i, getTrainingSet(negExamples, testNeg));
			}

		}

		// run the algorithm
			if( multiThreaded && lp instanceof Cloneable && la instanceof Cloneable){
				ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()-1);
				for (int currFold = 0; currFold < folds; currFold++) {
					try {
						final AbstractClassExpressionLearningProblem lpClone = (AbstractClassExpressionLearningProblem) lp.getClass().getMethod("clone").invoke(lp);
						final Set<OWLIndividual> trainPos = trainingSetsPos.get(currFold);
						final Set<OWLIndividual> trainNeg = trainingSetsNeg.get(currFold);
						final Set<OWLIndividual> testPos = testSetsPos.get(currFold);
						final Set<OWLIndividual> testNeg = testSetsNeg.get(currFold);
						if (lp instanceof PosNegLP) {
							((PosNegLP) lpClone).setPositiveExamples(trainPos);
							((PosNegLP) lpClone).setNegativeExamples(trainNeg);
						} else if (lp instanceof PosOnlyLP) {
							((PosOnlyLP) lpClone).setPositiveExamples(new TreeSet<>(trainPos));
						}
						final AbstractCELA laClone = (AbstractCELA) la.getClass().getMethod("clone").invoke(la);
						final int i = currFold;
						es.submit(() -> {
							try {
								validate(laClone, lpClone, rs, i, trainPos, trainNeg, testPos, testNeg);
							} catch (Exception e) {
								log.error("failed to validate fold " + i, e);
							}
						});
					} catch (IllegalAccessException | SecurityException | NoSuchMethodException | InvocationTargetException | IllegalArgumentException e) {
						e.printStackTrace();
					}
				}
				es.shutdown();
				try {
					es.awaitTermination(1, TimeUnit.DAYS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				for (int currFold = 0; currFold < folds; currFold++) {
					final Set<OWLIndividual> trainPos = trainingSetsPos.get(currFold);
					final Set<OWLIndividual> trainNeg = trainingSetsNeg.get(currFold);
					final Set<OWLIndividual> testPos = testSetsPos.get(currFold);
					final Set<OWLIndividual> testNeg = testSetsNeg.get(currFold);

					if (lp instanceof PosNegLP) {
						((PosNegLP) lp).setPositiveExamples(trainPos);
						((PosNegLP) lp).setNegativeExamples(trainNeg);
					} else if (lp instanceof PosOnlyLP) {
						((PosOnlyLP) lp).setPositiveExamples(new TreeSet<>(trainPos));
					}

					validate(la, lp, rs, currFold, trainPos, trainNeg, testPos, testNeg);
				}
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
	
	private void validate(AbstractCELA la, AbstractClassExpressionLearningProblem lp, AbstractReasonerComponent rs,
						  int currFold,
						  Set<OWLIndividual> trainPos, Set<OWLIndividual> trainNeg,
						  Set<OWLIndividual> testPos, Set<OWLIndividual> testNeg){
		Set<String> pos = Helper.getStringSet(trainPos);
		Set<String> neg = Helper.getStringSet(trainNeg);
		String output = "";
		output += "+" + new TreeSet<>(pos) + "\n";
		output += "-" + new TreeSet<>(neg) + "\n";
		try {
			lp.init();
			la.setLearningProblem(lp);
			la.init();
		} catch (ComponentInitException e) {
			log.error("failed to initialize component", e);
		}
		
		long algorithmStartTime = System.nanoTime();
		la.start();
		long algorithmDuration = System.nanoTime() - algorithmStartTime;
		runtime.addNumber(algorithmDuration/(double)1000000000);
		
		OWLClassExpression concept = la.getCurrentlyBestDescription();
		
		Set<OWLIndividual> tmp = rs.hasType(concept, testPos);
		Set<OWLIndividual> tmp2 = Sets.difference(testPos, tmp);
		Set<OWLIndividual> tmp3 = rs.hasType(concept, testNeg);
		
		// calculate training accuracies
		int trainingCorrectPosClassified = getCorrectPosClassified(rs, concept, trainPos);
		int trainingCorrectNegClassified = getCorrectNegClassified(rs, concept, trainNeg);
		int trainingCorrectExamples = trainingCorrectPosClassified + trainingCorrectNegClassified;
		double trainingAccuracy = 100 * ((double) trainingCorrectExamples / (trainPos.size() + trainNeg.size()));
		accuracyTraining.addNumber(trainingAccuracy);

		// calculate test accuracies
		int correctPosClassified = getCorrectPosClassified(rs, concept, testPos);
		int correctNegClassified = getCorrectNegClassified(rs, concept, testNeg);
		int correctExamples = correctPosClassified + correctNegClassified;
		double currAccuracy = 100 * ((double) correctExamples / (testPos.size() + testNeg.size()));
		accuracy.addNumber(currAccuracy);

		// calculate training F-Score
		int negAsPosTraining = rs.hasType(concept, trainNeg).size();
		double precisionTraining = trainingCorrectPosClassified + negAsPosTraining == 0 ? 0 : trainingCorrectPosClassified / (double) (trainingCorrectPosClassified + negAsPosTraining);
		double recallTraining = trainingCorrectPosClassified / (double) trainPos.size();
		fMeasureTraining.addNumber(100 * Heuristics.getFScore(recallTraining, precisionTraining));

		// calculate test F-Score
		int negAsPos = rs.hasType(concept, testNeg).size();
		double precision = correctPosClassified + negAsPos == 0 ? 0 : correctPosClassified / (double) (correctPosClassified + negAsPos);
		double recall = correctPosClassified / (double) testPos.size();
		fMeasure.addNumber(100 * Heuristics.getFScore(recall, precision));
		
		length.addNumber(OWLClassExpressionUtils.getLength(concept));
		
		
		output += "test set errors pos: " + tmp2 + "\n";
		output += "test set errors neg: " + tmp3 + "\n";
		output += "fold " + currFold + ":" + "\n";
		output += "  training: " + pos.size() + " positive and " + neg.size() + " negative examples";
		output += "  testing: " + correctPosClassified + "/" + testPos.size() + " correct positives, "
				+ correctNegClassified + "/" + testNeg.size() + " correct negatives" + "\n";
		output += "  concept: " + concept.toString().replace("\n", " ") + "\n";
		output += "  accuracy: " + df.format(currAccuracy) + "% (" + df.format(trainingAccuracy) + "% on training set)" + "\n";
		output += "  length: " + df.format(OWLClassExpressionUtils.getLength(concept)) + "\n";
		output += "  runtime: " + df.format(algorithmDuration/(double)1000000000) + "s" + "\n";
		
		outputWriter(output);
	}
	
	protected int getCorrectPosClassified(AbstractReasonerComponent rs, OWLClassExpression concept, Set<OWLIndividual> testSetPos) {
		return rs.hasType(concept, testSetPos).size();
	}
	
	protected int getCorrectNegClassified(AbstractReasonerComponent rs, OWLClassExpression concept, Set<OWLIndividual> testSetNeg) {
		return testSetNeg.size() - rs.hasType(concept, testSetNeg).size();
	}
	
	public static Set<OWLIndividual> getTestingSet(List<OWLIndividual> examples, int[] splits, int fold) {
		// we either start from 0 or after the last fold ended
		int fromIndex = (fold == 0) ? 0 : splits[fold-1];

		// the split corresponds to the ends of the folds
		int toIndex = splits[fold];
		
//		System.out.println("from " + fromIndex + " to " + toIndex);

		// +1 because 2nd element is exclusive in subList method
		Set<OWLIndividual> testingSet = new HashSet<>(examples.subList(fromIndex, toIndex));

		return testingSet;
	}
	
	public static Set<OWLIndividual> getTrainingSet(Set<OWLIndividual> examples, Set<OWLIndividual> testingSet) {
		return Sets.difference(examples, testingSet);
	}

	// takes nr. of examples and the nr. of folds for this examples;
	// returns an array which says where each fold ends, i.e.
	// splits[i] is the index of the last element of fold i in the examples
	public static int[] calculateSplits(int nrOfExamples, int folds) {
		return IntStream.rangeClosed(1, folds)
				.map(i -> (int) Math.ceil(i * nrOfExamples / (double) folds))
				.toArray();
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
		if (writeToFile) {
			Files.appendToFile(outputFile, output + "\n");
		}
		System.out.println(output);
	}

	public Stat getfMeasure() {
		return fMeasure;
	}

	public Stat getfMeasureTraining() {
		return fMeasureTraining;
	}

	public static void main(String[] args) {
		int folds = 5;
		int nrOfExamples = 20;

		int[] res = IntStream.rangeClosed(1, folds)
				.map(i -> (int) Math.ceil(i * nrOfExamples / (double) folds))
				.toArray();

		System.out.println(Arrays.toString(res));
	}

}
