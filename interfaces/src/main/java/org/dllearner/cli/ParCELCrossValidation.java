package org.dllearner.cli;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;


import org.apache.log4j.Logger;
import org.dllearner.algorithms.ParCEL.ParCELAbstract;
import org.dllearner.algorithms.ParCEL.ParCELPosNegLP;
import org.dllearner.algorithms.ParCELEx.ParCELExAbstract;
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

public class ParCELCrossValidation extends CrossValidation {

	protected Stat noOfPartialDef = new Stat();
	protected Stat partialDefinitionLength = new Stat();

	Logger logger = Logger.getLogger(this.getClass());

	protected boolean interupted = false;

	/**
	 * Default constructor
	 */

	public ParCELCrossValidation(AbstractCELA la, PosNegLP lp, AbstractReasonerComponent rs,
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
	 * @param kkFold Number of k-fold runs, i.e. the validation will run kk times of k-fold validations 
	 */
	public ParCELCrossValidation(AbstractCELA la, ParCELPosNegLP lp, AbstractReasonerComponent rs,
			int folds, boolean leaveOneOut, int kkFold) {

		super(); // do nothing

		//--------------------------
		//setting up 
		//--------------------------
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

		//----------------------
		//end of setting up
		//----------------------

		// sanity check whether nr. of folds makes sense for this benchmark
		if(!leaveOneOut && (posExamples.size()<folds && negExamples.size()<folds)) {
			System.out.println("The number of folds is higher than the number of "
					+ "positive/negative examples. This can result in empty test sets. Exiting.");
			System.exit(0);
		}

		//------------------------
		//leave one out
		//------------------------
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
		}
		//-------------------------------
		//end of leave one out
		//----
		//start the k-fold validation
		//-------------------------------
		else {
			// calculating where to split the sets, ; note that we split
			// positive and negative examples separately such that the 
			// distribution of positive and negative examples remains similar
			// (note that there are better but more complex ways to implement this,
			// which guarantee that the sum of the elements of a fold for pos
			// and neg differs by at most 1 - it can differ by 2 in our implementation,
			// e.g. with 3 folds, 4 pos. examples, 4 neg. examples)
			int[] splitsPos = calculateSplits(posExamples.size(),folds);
			int[] splitsNeg = calculateSplits(negExamples.size(),folds);

			//System.out.println(splitsPos[0]);
			//System.out.println(splitsNeg[0]);

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

		// run the algorithm
		int terminatedBypartialDefinition=0, terminatedByCounterPartialDefinitions=0;

		//---------------------------------
		//k-fold cross validation
		//---------------------------------

		Stat runtimeAvg = new Stat();
		Stat runtimeMax = new Stat();
		Stat runtimeMin = new Stat();

		Stat noOfPartialDefAvg = new Stat();
		Stat noOfPartialDefMax = new Stat();
		Stat noOfPartialDefMin = new Stat();

		Stat avgPartialDefLenAvg = new Stat();
		Stat avgPartialDefLenMax = new Stat();
		Stat avgPartialDefLenMin = new Stat();

		Stat defLenAvg = new Stat();
		Stat defLenMax = new Stat();
		Stat defLenMin = new Stat();

		Stat trainingAccAvg = new Stat();
		Stat trainingAccMax = new Stat();
		Stat trainingAccMin = new Stat();

		Stat trainingCorAvg = new Stat();
		Stat trainingCorMax = new Stat();
		Stat trainingCorMin = new Stat();

		Stat trainingComAvg = new Stat();
		Stat trainingComMax = new Stat();
		Stat trainingComMin = new Stat();

		Stat testingAccAvg = new Stat();
		Stat testingAccMax = new Stat();
		Stat testingAccMin = new Stat();

		Stat testingCorAvg = new Stat();
		Stat testingCorMax = new Stat();
		Stat testingCorMin = new Stat();

		Stat testingComAvg = new Stat();
		Stat testingComMax = new Stat();
		Stat testingComMin = new Stat();

		for (int kk=0; kk < kkFold; kk++) {

			//runtime
			runtime = new Stat();
			noOfPartialDef = new Stat();
			partialDefinitionLength = new Stat();
			length = new Stat();			
			accuracyTraining = new Stat();
			trainingCorrectnessStat= new Stat();
			trainingCompletenessStat = new Stat();
			accuracy = new Stat();
			testingCorrectnessStat = new Stat();
			testingCompletenessStat = new Stat();
			
			for(int currFold=0; (currFold<folds); currFold++) {

				if (this.interupted) {
					outputWriter("Cross validation has been interupted");
					return;
				}

				//Set<String> pos = Datastructures.individualSetToStringSet(trainingSetsPos.get(currFold));
				//Set<String> neg = Datastructures.individualSetToStringSet(trainingSetsNeg.get(currFold));
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
				}
				catch (OutOfMemoryError e) {
					System.out.println("out of memory at " + (System.currentTimeMillis() - algorithmStartTime)/1000 + "s");
				}

				long algorithmDuration = System.nanoTime() - algorithmStartTime;
				runtime.addNumber(algorithmDuration/(double)1000000000);

				Description concept = ((ParCELAbstract)la).getUnionCurrenlyBestDescription(); 

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

				tmp = rs.hasType(concept, testSetsPos.get(currFold));
				tmp2 = Helper.difference(testSetsPos.get(currFold), tmp);
				tmp3 = rs.hasType(concept, testSetsNeg.get(currFold));

				outputWriter("test set errors pos: " + tmp2);
				outputWriter("test set errors neg: " + tmp3);

				// calculate training accuracies 
				int trainingCorrectPosClassified = getCorrectPosClassified(rs, concept, trainingSetsPos.get(currFold));
				int trainingCorrectNegClassified = getCorrectNegClassified(rs, concept, trainingSetsNeg.get(currFold));
				int trainingCorrectExamples = trainingCorrectPosClassified + trainingCorrectNegClassified;
				double trainingAccuracy = 100*((double)trainingCorrectExamples/(trainingSetsPos.get(currFold).size()+
						trainingSetsNeg.get(currFold).size()));			

				double trainingCompleteness = 100*(double)trainingCorrectPosClassified/trainingSetsPos.get(currFold).size();
				double trainingCorrectness = 100*(double)trainingCorrectNegClassified/trainingSetsNeg.get(currFold).size();

				accuracyTraining.addNumber(trainingAccuracy);
				trainingCompletenessStat.addNumber(trainingCompleteness);
				trainingCorrectnessStat.addNumber(trainingCorrectness);

				// calculate test accuracies
				int correctPosClassified = getCorrectPosClassified(rs, concept, testSetsPos.get(currFold));
				int correctNegClassified = getCorrectNegClassified(rs, concept, testSetsNeg.get(currFold));
				int correctExamples = correctPosClassified + correctNegClassified;
				double currAccuracy = 100*((double)correctExamples/(testSetsPos.get(currFold).size()+
						testSetsNeg.get(currFold).size()));

				double testingCompleteness = 100*(double)correctPosClassified/testSetsPos.get(currFold).size();
				double testingCorrectness = 100*(double)correctNegClassified/testSetsNeg.get(currFold).size();

				accuracy.addNumber(currAccuracy);
				testingCompletenessStat.addNumber(testingCompleteness);
				testingCorrectnessStat.addNumber(testingCorrectness);


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

				outputWriter("fold " + currFold + ":");
				outputWriter("  training: " + trainingCorrectPosClassified + "/" + trainingSetsPos.get(currFold).size() + 
						" positive and " + trainingCorrectNegClassified + "/" + trainingSetsNeg.get(currFold).size() + " negative examples");
				outputWriter("  testing: " + correctPosClassified + "/" + testSetsPos.get(currFold).size() + " correct positives, " 
						+ correctNegClassified + "/" + testSetsNeg.get(currFold).size() + " correct negatives");
				outputWriter("  concept: " + concept);
				outputWriter("  accuracy: " + df.format(currAccuracy) +  "(corr/comp:"+ testingCorrectness + "/" + testingCompleteness + ")% --- " + 
						df.format(trainingAccuracy) + " (corr/comp:"+ trainingCorrectness + "/" + trainingCompleteness + ")% on training set)");
				outputWriter("  length: " + df.format(concept.getLength()));
				outputWriter("  runtime: " + df.format(algorithmDuration/(double)1000000000) + "s");			

				if (la instanceof ParCELAbstract) {
					int pn = ((ParCELAbstract)la).getNoOfCompactedPartialDefinition();
					this.noOfPartialDef.addNumber(pn);
					outputWriter("  number of partial definitions: " + pn + "/" + ((ParCELAbstract)la).getNumberOfPartialDefinitions());

					double pl = concept.getLength()/(double)pn;
					this.partialDefinitionLength.addNumber(pl);
					outputWriter("  avarage partial definition length: " + pl);		

					//show more information on counter partial definitions				
					if (la instanceof ParCELExAbstract) {
						ParCELExAbstract pdllexla = (ParCELExAbstract)la;
						outputWriter("  number of partial definitions for each type: 1:" + pdllexla.getNumberOfPartialDefinitions(1) + 
								"; 2:" + pdllexla.getNumberOfPartialDefinitions(2) + 
								"; 3:" + pdllexla.getNumberOfPartialDefinitions(3) +
								"; 4:" + pdllexla.getNumberOfPartialDefinitions(4));
						outputWriter("  number of counter partial definition used: " + (concept.toString().split("NOT ").length - 1) + "/" + pdllexla.getNumberOfCounterPartialDefinitionUsed());
						if (pdllexla.terminatedByCounterDefinitions()) {
							outputWriter("  terminated by counter partial definitions");
							terminatedByCounterPartialDefinitions++;
						}
						else if (pdllexla.terminatedByPartialDefinitions()) {
							outputWriter("  terminated by partial definitions");
							terminatedBypartialDefinition++;
						}
						else
							outputWriter("  neither terminated by partial definition nor counter partial definition");
					}
				}

			}	//for k folds


			//---------------------------------
			//end of k-fold cross validation
			//output result of the k-fold 
			//---------------------------------

			outputWriter("");
			outputWriter("Finished the " + kk + getOrderUnit(kk) + " of a " + folds + "-folds cross-validation.");
			outputWriter("runtime: " + statOutput(df, runtime, "s"));
			outputWriter("#partial definitions: " + statOutput(df, noOfPartialDef, ""));
			outputWriter("avg. partial definition length: " + statOutput(df, partialDefinitionLength, ""));
			outputWriter("length: " + statOutput(df, length, ""));
			//outputWriter("F-Measure on training set: " + statOutput(df, fMeasureTraining, "%"));		
			//outputWriter("F-Measure: " + statOutput(df, fMeasure, "%"));
			outputWriter("predictive accuracy on training set: " + statOutput(df, accuracyTraining, "%") + " - corr/comp: " + 
					statOutput(df, trainingCorrectnessStat, "%") + "/" + statOutput(df, trainingCompletenessStat, "%"));		
			outputWriter("predictive accuracy: " + statOutput(df, accuracy, "%") + " - corr/comp: " + 
					statOutput(df, testingCorrectnessStat, "%") + "/" + statOutput(df, testingCompletenessStat, "%"));
			if (la instanceof ParCELExAbstract)
				outputWriter("terminated by: partial def.: " + terminatedBypartialDefinition + "; counter partial def.: " + terminatedByCounterPartialDefinitions);


			//runtime
			runtimeAvg.addNumber(runtime.getMean());
			runtimeMax.addNumber(runtime.getMax());
			runtimeMin.addNumber(runtime.getMin());

			//number of partial definitions			
			noOfPartialDefAvg.addNumber(noOfPartialDef.getMean());
			noOfPartialDefMax.addNumber(noOfPartialDef.getMax());
			noOfPartialDefMin.addNumber(noOfPartialDef.getMin());
			
			avgPartialDefLenAvg.addNumber(partialDefinitionLength.getMean());
			avgPartialDefLenMax.addNumber(partialDefinitionLength.getMax());
			avgPartialDefLenMin.addNumber(partialDefinitionLength.getMin());
			
			defLenAvg.addNumber(length.getMean());
			defLenMax.addNumber(length.getMax());
			defLenMin.addNumber(length.getMin());			
			
			trainingAccAvg.addNumber(accuracyTraining.getMean());
			trainingAccMax.addNumber(accuracyTraining.getMax());
			trainingAccMin.addNumber(accuracyTraining.getMin());
			
			trainingCorAvg.addNumber(trainingCorrectnessStat.getMean());
			trainingCorMax.addNumber(trainingCorrectnessStat.getMax());
			trainingCorMin.addNumber(trainingCorrectnessStat.getMin());
			
			trainingComAvg.addNumber(trainingCompletenessStat.getMean());
			trainingComMax.addNumber(trainingCompletenessStat.getMax());
			trainingComMin.addNumber(trainingCompletenessStat.getMin());
			
			testingAccAvg.addNumber(accuracy.getMean());
			testingAccMax.addNumber(accuracy.getMax());
			testingAccMin.addNumber(accuracy.getMin());
			
			testingCorAvg.addNumber(testingCorrectnessStat.getMean());
			testingCorMax.addNumber(testingCorrectnessStat.getMax());
			testingCorMin.addNumber(testingCorrectnessStat.getMin());
			
			testingComAvg.addNumber(testingCompletenessStat.getMean());
			testingComMax.addNumber(testingCompletenessStat.getMax());
			testingComMin.addNumber(testingCompletenessStat.getMin());
			
		}	//for kk folds
		
		outputWriter("");
		outputWriter("Finished " + + kkFold + " times of the " + folds + "-folds cross-validations");
		
		outputWriter("runtime: " + 
				"\n\t avg.: " + statOutput(df, runtimeAvg, "s") +
				"\n\t max.: " + statOutput(df, runtimeMax, "s") +
				"\n\t min.: " + statOutput(df, runtimeMin, "s"));
		
		outputWriter("number of partial definitions: " + 
				"\n\t avg.: " + statOutput(df, noOfPartialDefAvg, "") +
				"\n\t max.: " + statOutput(df, noOfPartialDefMax, "") +
				"\n\t min.: " + statOutput(df, noOfPartialDefMin, ""));
		
		outputWriter("avg. partial definition length: " + 
				"\n\t avg.: " + statOutput(df, avgPartialDefLenAvg, "") + 
				"\n\t max.: " + statOutput(df, avgPartialDefLenMax, "") +
				"\n\t min.: " + statOutput(df, avgPartialDefLenMin, ""));
		
		outputWriter("definition length: " + 
				"\n\t avg.: " + statOutput(df, defLenAvg, "") +
				"\n\t max.: " + statOutput(df, defLenMax, "") +
				"\n\t min.: " + statOutput(df, defLenMin, ""));
		
		outputWriter("accuracy on training set:" + 
				"\n\t avg.: " + statOutput(df, trainingAccAvg, "%") + 
				"\n\t max.: " + statOutput(df, trainingAccMax, "%") +
				"\n\t min.: " + statOutput(df, trainingAccMin, "%"));
		
		outputWriter("correctness on training set: " + 
				"\n\t avg.: " + statOutput(df, trainingCorAvg, "%") +
				"\n\t max.: " + statOutput(df, trainingCorMax, "%") +
				"\n\t min.: " + statOutput(df, trainingCorMin, "%"));
		
		outputWriter("completeness on training set: " + 
				"\n\t avg.: " + statOutput(df, trainingComAvg, "%") +
				"\n\t max.: " + statOutput(df, trainingComMax, "%") +
				"\n\t min.: " + statOutput(df, trainingComMin, "%"));
		
		outputWriter("accuracy on testing set: " + 
				"\n\t avg.: " + statOutput(df, testingAccAvg, "%") +
				"\n\t max.: " + statOutput(df, testingAccMax, "%") +
				"\n\t min.: " + statOutput(df, testingAccMin, "%"));
		
		outputWriter("correctness on testing set: " + 
				"\n\t avg.: " + statOutput(df, testingCorAvg, "%") +
				"\n\t max.: " + statOutput(df, testingCorMax, "%") +
				"\n\t min.: " + statOutput(df, testingCorMin, "%"));
		
		outputWriter("completeness on testing set: " + 
				"\n\t avg.: " + statOutput(df, testingComAvg, "%") +
				"\n\t max.: " + statOutput(df, testingComMax, "%") +
				"\n\t min.: " + statOutput(df, testingComMin, "%"));

		if (la instanceof ParCELExAbstract)
			outputWriter("terminated by: partial def.: " + terminatedBypartialDefinition + "; counter partial def.: " + terminatedByCounterPartialDefinitions);

	}


	private String getOrderUnit(int order) {
		switch (order) {
			case 1: return "st";
			case 2: return "nd";
			case 3: return "rd";
			default: return "th";
		}
	}
	

	@Override
	protected void outputWriter(String output) {
		logger.info(output);

		if (writeToFile)
			Files.appendToFile(outputFile, output + "\n");
	}

}
