package org.dllearner.cli;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;


import org.apache.log4j.Logger;
import org.dllearner.algorithms.PADCEL.PADCELAbstract;
import org.dllearner.algorithms.PADCEL.PADCELPosNegLP;
import org.dllearner.algorithms.PADCELEx.PADCELExAbstract;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.learningproblems.Heuristics;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.utilities.Files;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.statistics.Stat;

/**
 * Add PDLL cross validation support to Jens Lehmann work (
 * {@link org.dllearner.cli.CrossValidation}). In this cross validation, 
 * some more addition dimensions will be investigated such as: 
 * number partial definitions, partial definition length, etc.   
 *  
 * 
 * @author actran
 * 
 */

public class PDLLCrossValidation extends CrossValidation {

	protected Stat noOfPartialDef = new Stat();
	protected Stat partialDefinitionLength = new Stat();

	Logger logger = Logger.getLogger(this.getClass());

	protected boolean interupted = false;

	/**
	 * Default constructor
	 */

	public PDLLCrossValidation(AbstractCELA la, PosNegLP lp, AbstractReasonerComponent rs,
			int folds, boolean leaveOneOut) {
		super(la, lp, rs, folds, leaveOneOut);
	}

	/**
	 * This is for PDLL cross validation
	 * 
	 * @param la
	 * @param lp
	 * @param rs
	 * @param folds
	 * @param leaveOneOut
	 */
	public PDLLCrossValidation(AbstractCELA la, PADCELPosNegLP lp, AbstractReasonerComponent rs,
			int folds, boolean leaveOneOut) {

		super(); // do nothing

		DecimalFormat df = new DecimalFormat();

		// the training and test sets used later on
		List<Set<Individual>> trainingSetsPos = new LinkedList<Set<Individual>>();
		List<Set<Individual>> trainingSetsNeg = new LinkedList<Set<Individual>>();
		List<Set<Individual>> testSetsPos = new LinkedList<Set<Individual>>();
		List<Set<Individual>> testSetsNeg = new LinkedList<Set<Individual>>();

		// get examples and shuffle them too
		Set<Individual> posExamples = lp.getPositiveExamples();
		List<Individual> posExamplesList = new LinkedList<Individual>(posExamples);
		Collections.shuffle(posExamplesList, new Random(1));
		Set<Individual> negExamples = lp.getNegativeExamples();
		List<Individual> negExamplesList = new LinkedList<Individual>(negExamples);
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
			// (note that there are better but more complex ways to implement
			// this,
			// which guarantee that the sum of the elements of a fold for pos
			// and neg differs by at most 1 - it can differ by 2 in our
			// implementation,
			// e.g. with 3 folds, 4 pos. examples, 4 neg. examples)
			int[] splitsPos = calculateSplits(posExamples.size(), folds);
			int[] splitsNeg = calculateSplits(negExamples.size(), folds);

			// System.out.println(splitsPos[0]);
			// System.out.println(splitsNeg[0]);

			// calculating training and test sets
			for (int i = 0; i < folds; i++) {
				Set<Individual> testPos = getTestingSet(posExamplesList, splitsPos, i);
				Set<Individual> testNeg = getTestingSet(negExamplesList, splitsNeg, i);
				testSetsPos.add(i, testPos);
				testSetsNeg.add(i, testNeg);
				trainingSetsPos.add(i, getTrainingSet(posExamples, testPos));
				trainingSetsNeg.add(i, getTrainingSet(negExamples, testNeg));
			}

		}

		// run the algorithm
		int terminatedBypartialDefinition = 0, terminatedByCounterPartialDefinitions = 0;

		for (int currFold = 0; (currFold < folds); currFold++) {

			if (this.interupted) {
				outputWriter("Cross validation has been interupted");
				return;
			}

			// Set<String> pos =
			// Datastructures.individualSetToStringSet(trainingSetsPos.get(currFold));
			// Set<String> neg =
			// Datastructures.individualSetToStringSet(trainingSetsNeg.get(currFold));
			lp.setPositiveExamples(trainingSetsPos.get(currFold));
			lp.setNegativeExamples(trainingSetsNeg.get(currFold));

			try {
				lp.init();
				la.init();
			} catch (ComponentInitException e) {
				e.printStackTrace();
			}

			long algorithmStartTime = System.nanoTime();
			try {
				la.start();
			} catch (OutOfMemoryError e) {
				System.out.println("out of memory at "
						+ (System.currentTimeMillis() - algorithmStartTime) / 1000 + "s");
			}

			long algorithmDuration = System.nanoTime() - algorithmStartTime;
			runtime.addNumber(algorithmDuration / (double) 1000000000);

			Description concept = ((PADCELAbstract) la).getUnionCurrenlyBestDescription();

			Set<Individual> tmp = rs.hasType(concept, trainingSetsPos.get(currFold));
			Set<Individual> tmp2 = Helper.difference(trainingSetsPos.get(currFold), tmp);
			Set<Individual> tmp3 = rs.hasType(concept, trainingSetsNeg.get(currFold));

			outputWriter("training set errors pos (" + tmp2.size() + "): " + tmp2);
			outputWriter("training set errors neg (" + tmp3.size() + "): " + tmp3);

			tmp = rs.hasType(concept, testSetsPos.get(currFold));
			tmp2 = Helper.difference(testSetsPos.get(currFold), tmp);
			tmp3 = rs.hasType(concept, testSetsNeg.get(currFold));

			outputWriter("test set errors pos: " + tmp2);
			outputWriter("test set errors neg: " + tmp3);

			// calculate training accuracies
			int trainingCorrectPosClassified = getCorrectPosClassified(rs, concept,
					trainingSetsPos.get(currFold));
			int trainingCorrectNegClassified = getCorrectNegClassified(rs, concept,
					trainingSetsNeg.get(currFold));
			int trainingCorrectExamples = trainingCorrectPosClassified
					+ trainingCorrectNegClassified;
			double trainingAccuracy = 100 * ((double) trainingCorrectExamples / (trainingSetsPos
					.get(currFold).size() + trainingSetsNeg.get(currFold).size()));

			double trainingCompleteness = 100 * (double) trainingCorrectPosClassified
					/ trainingSetsPos.get(currFold).size();
			double trainingCorrectness = 100 * (double) trainingCorrectNegClassified
					/ trainingSetsNeg.get(currFold).size();

			accuracyTraining.addNumber(trainingAccuracy);
			trainingCompletenessStat.addNumber(trainingCompleteness);
			trainingCorrectnessStat.addNumber(trainingCorrectness);

			// calculate test accuracies
			int correctPosClassified = getCorrectPosClassified(rs, concept,
					testSetsPos.get(currFold));
			int correctNegClassified = getCorrectNegClassified(rs, concept,
					testSetsNeg.get(currFold));
			int correctExamples = correctPosClassified + correctNegClassified;
			double currAccuracy = 100 * ((double) correctExamples / (testSetsPos.get(currFold)
					.size() + testSetsNeg.get(currFold).size()));

			double testingCompleteness = 100 * (double) correctPosClassified
					/ testSetsPos.get(currFold).size();
			double testingCorrectness = 100 * (double) correctNegClassified
					/ testSetsNeg.get(currFold).size();

			accuracy.addNumber(currAccuracy);
			testingCompletenessStat.addNumber(testingCompleteness);
			testingCorrectnessStat.addNumber(testingCorrectness);

			// calculate training F-Score
			int negAsPosTraining = rs.hasType(concept, trainingSetsNeg.get(currFold)).size();
			double precisionTraining = trainingCorrectPosClassified + negAsPosTraining == 0 ? 0
					: trainingCorrectPosClassified
							/ (double) (trainingCorrectPosClassified + negAsPosTraining);
			double recallTraining = trainingCorrectPosClassified
					/ (double) trainingSetsPos.get(currFold).size();
			fMeasureTraining.addNumber(100 * Heuristics
					.getFScore(recallTraining, precisionTraining));
			// calculate test F-Score
			int negAsPos = rs.hasType(concept, testSetsNeg.get(currFold)).size();
			double precision = correctPosClassified + negAsPos == 0 ? 0 : correctPosClassified
					/ (double) (correctPosClassified + negAsPos);
			double recall = correctPosClassified / (double) testSetsPos.get(currFold).size();
			// System.out.println(precision);System.out.println(recall);
			fMeasure.addNumber(100 * Heuristics.getFScore(recall, precision));

			length.addNumber(concept.getLength());

			outputWriter("fold " + currFold + ":");
			outputWriter("  training: " + trainingCorrectPosClassified + "/"
					+ trainingSetsPos.get(currFold).size() + " positive and "
					+ trainingCorrectNegClassified + "/" + trainingSetsNeg.get(currFold).size()
					+ " negative examples");
			outputWriter("  testing: " + correctPosClassified + "/"
					+ testSetsPos.get(currFold).size() + " correct positives, "
					+ correctNegClassified + "/" + testSetsNeg.get(currFold).size()
					+ " correct negatives");
			outputWriter("  concept: " + concept);
			outputWriter("  accuracy: " + df.format(currAccuracy) + "% (correctness: "
					+ df.format(testingCorrectness) + "%; completeness: "
					+ df.format(testingCompleteness) + "%) --- training set: "
					+ df.format(trainingAccuracy) + "% (correctness: "
					+ df.format(trainingCorrectness) + "%; completeness: "
					+ df.format(trainingCompleteness) + "%)");
			outputWriter("  length: " + df.format(concept.getLength()));
			outputWriter("  runtime: " + df.format(algorithmDuration / (double) 1000000000) + "s");

			if (la instanceof PADCELAbstract) {
				int pn = ((PADCELAbstract) la).getNoOfCompactedPartialDefinition();
				this.noOfPartialDef.addNumber(pn);
				outputWriter("  number of partial definitions: " + pn + " (total: "
						+ ((PADCELAbstract) la).getNumberOfPartialDefinitions() + ")");

				double pl = concept.getLength() / (double) pn;
				this.partialDefinitionLength.addNumber(pl);
				outputWriter("  avarage partial definition length: " + df.format(pl));

				// show more information on counter partial definitions
				if (la instanceof PADCELExAbstract) {
					PADCELExAbstract pdllexla = (PADCELExAbstract) la;
					outputWriter("  number of partial definitions for each type: 1:"
							+ pdllexla.getNumberOfPartialDefinitions(1) + "; 2:"
							+ pdllexla.getNumberOfPartialDefinitions(2) + "; 3:"
							+ pdllexla.getNumberOfPartialDefinitions(3) + "; 4:"
							+ pdllexla.getNumberOfPartialDefinitions(4));
					outputWriter("  number of counter partial definition used: "
							+ (concept.toString().split("NOT ").length - 1) + "/"
							+ pdllexla.getNumberOfCounterPartialDefinitionUsed());
					if (pdllexla.terminatedByCounterDefinitions()) {
						outputWriter("  terminated by counter partial definitions");
						terminatedByCounterPartialDefinitions++;
					} else if (pdllexla.terminatedByPartialDefinitions()) {
						outputWriter("  terminated by partial definitions");
						terminatedBypartialDefinition++;
					} else
						outputWriter("  neither terminated by partial definition nor counter partial definition");
				}
			}

		}

		outputWriter("");
		outputWriter("Finished " + folds + "-folds cross-validation.");
		outputWriter("runtime: " + statOutput(df, runtime, "s"));
		outputWriter("#partial definitions: " + statOutput(df, noOfPartialDef, ""));
		outputWriter("avg. partial definition length: "
				+ statOutput(df, partialDefinitionLength, ""));
		outputWriter("length: " + statOutput(df, length, ""));
		outputWriter("F-Measure on training set: " + statOutput(df, fMeasureTraining, "%"));
		outputWriter("F-Measure: " + statOutput(df, fMeasure, "%"));
		outputWriter("predictive accuracy on training set: "
				+ statOutput(df, accuracyTraining, "%") + " --- correctness: "
				+ statOutput(df, trainingCorrectnessStat, "%") + "; completeness: "
				+ statOutput(df, trainingCompletenessStat, "%"));
		outputWriter("predictive accuracy: " + statOutput(df, accuracy, "%") + " --- correctness: "
				+ statOutput(df, testingCorrectnessStat, "%") + "; completeness: "
				+ statOutput(df, testingCompletenessStat, "%"));
		if (la instanceof PADCELExAbstract)
			outputWriter("terminated by: partial def.: " + terminatedBypartialDefinition
					+ "; counter partial def.: " + terminatedByCounterPartialDefinitions);
	}

	@Override
	protected void outputWriter(String output) {
		logger.info(output);

		if (writeToFile)
			Files.appendToFile(outputFile, output + "\n");
	}

}
