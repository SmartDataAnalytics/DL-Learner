package org.dllearner.cli.parcel.modeling;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;


import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.dllearner.algorithms.parcel.ParCELAbstract;
import org.dllearner.algorithms.parcel.ParCELExtraNode;
import org.dllearner.algorithms.parcel.ParCELPosNegLP;
import org.dllearner.cli.CrossValidation;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.learningproblems.Heuristics;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.utilities.Files;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;
import org.dllearner.utilities.statistics.Stat;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;

/**
 * Use testing data to analyse the learning partial definitions.
  * 
 * @author An C. Tran
 * 
 */

public class ParCELValidationModelAnalysis extends CrossValidation {

	protected Stat noOfPartialDef = new Stat();
	protected Stat partialDefinitionLength = new Stat();

	Logger logger = Logger.getLogger(this.getClass());

	protected boolean interupted = false;

	
	/**
	 * Default constructor for calling DL-Learner cross validation procedure
	 */

	public ParCELValidationModelAnalysis(AbstractCELA la, PosNegLP lp, AbstractReasonerComponent rs,
			int folds, boolean leaveOneOut, int kkFold) {
		super(la, lp, rs, folds, leaveOneOut);
	}

	/**
	 * This is for ParCEL cross validation
	 * 
	 * @param la
	 * @param lp
	 * @param rs
	 * @param folds
	 * @param leaveOneOut
	 * @param kkFold Number of k-fold runs, i.e. the validation will run kk times of k-fold validations 
	 */
	public ParCELValidationModelAnalysis(AbstractCELA la, ParCELPosNegLP lp, AbstractReasonerComponent rs,
			int folds, boolean leaveOneOut, int kkFold) {
		
		super(); // do nothing
		
		if (!(la instanceof ParCELAbstract)) {
			outputWriter("Only ParCEL algorithm family is supported!");
			return;
		}
		
		ParCELAbstract parcel = (ParCELAbstract)la;

		//--------------------------
		//setting up 
		//--------------------------
		DecimalFormat df = new DecimalFormat();	

		// the training and test sets used later on
		List<Set<OWLIndividual>> trainingSetsPos = new LinkedList<>();
		List<Set<OWLIndividual>> trainingSetsNeg = new LinkedList<>();
		List<Set<OWLIndividual>> testingSetsPos = new LinkedList<>();
		List<Set<OWLIndividual>> testingSetsNeg = new LinkedList<>();

		// get examples and shuffle them too
		Set<OWLIndividual> posExamples = lp.getPositiveExamples();
		List<OWLIndividual> posExamplesList = new LinkedList<>(posExamples);
		Collections.shuffle(posExamplesList, new Random(1));			
		Set<OWLIndividual> negExamples = lp.getNegativeExamples();
		List<OWLIndividual> negExamplesList = new LinkedList<>(negExamples);
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
				Set<OWLIndividual> testPos = getTestingSet(posExamplesList, splitsPos, i);
				Set<OWLIndividual> testNeg = getTestingSet(negExamplesList, splitsNeg, i);
				testingSetsPos.add(i, testPos);
				testingSetsNeg.add(i, testNeg);
				trainingSetsPos.add(i, getTrainingSet(posExamples, testPos));
				trainingSetsNeg.add(i, getTrainingSet(negExamples, testNeg));				
			}	

		
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
			
			
			//hold all partial definitions for further analysis
			Map<OWLClassExpression, Set<FoldInfor>> allPartialDefinitions = new TreeMap<OWLClassExpression, Set<FoldInfor>>();
			SortedSet<OWLClassExpression> allReducedPartialDefinitions = new TreeSet<OWLClassExpression>();
					
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

				OWLClassExpression concept = parcel.getUnionCurrenlyBestDescription();

				Set<OWLIndividual> tmp = rs.hasType(concept, trainingSetsPos.get(currFold));
				Set<OWLIndividual> tmp2 = Sets.difference(trainingSetsPos.get(currFold), tmp);
				Set<OWLIndividual> tmp3 = rs.hasType(concept, trainingSetsNeg.get(currFold));

				outputWriter("training set errors pos (" + tmp2.size() + "): " + tmp2);
				outputWriter("training set errors neg (" + tmp3.size() + "): " + tmp3);


				tmp = rs.hasType(concept, testingSetsPos.get(currFold));
				tmp2 = Sets.difference(testingSetsPos.get(currFold), tmp);
				tmp3 = rs.hasType(concept, testingSetsNeg.get(currFold));

				outputWriter("test set errors pos: " + tmp2);
				outputWriter("test set errors neg: " + tmp3);

				tmp = rs.hasType(concept, testingSetsPos.get(currFold));
				tmp2 = Sets.difference(testingSetsPos.get(currFold), tmp);
				tmp3 = rs.hasType(concept, testingSetsNeg.get(currFold));

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
				int correctPosClassified = getCorrectPosClassified(rs, concept, testingSetsPos.get(currFold));
				int correctNegClassified = getCorrectNegClassified(rs, concept, testingSetsNeg.get(currFold));
				int correctExamples = correctPosClassified + correctNegClassified;
				double currAccuracy = 100*((double)correctExamples/(testingSetsPos.get(currFold).size()+
						testingSetsNeg.get(currFold).size()));

				double testingCompleteness = 100*(double)correctPosClassified/testingSetsPos.get(currFold).size();
				double testingCorrectness = 100*(double)correctNegClassified/testingSetsNeg.get(currFold).size();

				accuracy.addNumber(currAccuracy);
				testingCompletenessStat.addNumber(testingCompleteness);
				testingCorrectnessStat.addNumber(testingCorrectness);


				// calculate training F-Score
				int negAsPosTraining = rs.hasType(concept, trainingSetsNeg.get(currFold)).size();
				double precisionTraining = trainingCorrectPosClassified + negAsPosTraining == 0 ? 0 : trainingCorrectPosClassified / (double) (trainingCorrectPosClassified + negAsPosTraining);
				double recallTraining = trainingCorrectPosClassified / (double) trainingSetsPos.get(currFold).size();
				fMeasureTraining.addNumber(100*Heuristics.getFScore(recallTraining, precisionTraining));
				// calculate test F-Score
				int negAsPos = rs.hasType(concept, testingSetsNeg.get(currFold)).size();
				double precision = correctPosClassified + negAsPos == 0 ? 0 : correctPosClassified / (double) (correctPosClassified + negAsPos);
				double recall = correctPosClassified / (double) testingSetsPos.get(currFold).size();
				//			System.out.println(precision);System.out.println(recall);
				fMeasure.addNumber(100*Heuristics.getFScore(recall, precision));			

				length.addNumber(OWLClassExpressionUtils.getLength(concept));

				outputWriter("fold " + currFold + ":");
				outputWriter("  training: " + trainingCorrectPosClassified + "/" + trainingSetsPos.get(currFold).size() + 
						" positive and " + trainingCorrectNegClassified + "/" + trainingSetsNeg.get(currFold).size() + " negative examples");
				outputWriter("  testing: " + correctPosClassified + "/" + testingSetsPos.get(currFold).size() + " correct positives, " 
						+ correctNegClassified + "/" + testingSetsNeg.get(currFold).size() + " correct negatives");
				outputWriter("  concept: " + concept);
				outputWriter("  accuracy: " + df.format(currAccuracy) +  "(corr/comp:"+ df.format(testingCorrectness) + "/" + df.format(testingCompleteness) + ")% --- " + 
						df.format(trainingAccuracy) + " (corr/comp:"+ df.format(trainingCorrectness) + "/" + df.format(trainingCompleteness) + ")% on training set)");
				outputWriter("  length: " + df.format(OWLClassExpressionUtils.getLength(concept)));
				outputWriter("  runtime: " + df.format(algorithmDuration/(double)1000000000) + "s");			

				//this should be checked at the beginning of the cross validation since this is only for ParCEL algorithm
				
				int pn = ((ParCELAbstract)la).getNoOfReducedPartialDefinition();
				this.noOfPartialDef.addNumber(pn);
				outputWriter("  number of partial definitions: " + pn + "/" + ((ParCELAbstract)la).getNumberOfPartialDefinitions());

				double pl = OWLClassExpressionUtils.getLength(concept)/(double)pn;
				this.partialDefinitionLength.addNumber(pl);
				outputWriter("  avarage partial definition length: " + pl);		
				
				
				/*
				 * Analyse the partial definitions
				 */
				
				//reduced partial definitions of the current fold
				SortedSet<ParCELExtraNode> foldReducedPartialDefinitions = parcel.getReducedPartialDefinition();
						
				
				for (ParCELExtraNode node : foldReducedPartialDefinitions)
					allReducedPartialDefinitions.add(node.getDescription());
				
				//infer and display the partial definition information				
				outputWriter("---------- Fold " + currFold + " -----------");
				Set<ParCELExtraNode> foldPartialDefinitions = parcel.getPartialDefinitions();
								
				//int count=1;
				Set<OWLIndividual> trainingPos = trainingSetsPos.get(currFold);
				Set<OWLIndividual> trainingNeg = trainingSetsNeg.get(currFold);
				Set<OWLIndividual> testingPos = testingSetsPos.get(currFold);
				Set<OWLIndividual> testingNeg = testingSetsNeg.get(currFold);

				//------------------
				// PREDICTION model
				//------------------
				
				//calculate prediction-model-score for all partial definition generated of the current fold
				Map<OWLClassExpression, Double> predictionScores = ParCELPredictionModelScoring.scoringComplex(
						foldPartialDefinitions, trainingPos);

				//-----------------------
				// calculate fold infor
				//-----------------------
				for (ParCELExtraNode def : foldPartialDefinitions) {

					Set<OWLIndividual> trainingCoveredPos = rs.hasType(def.getDescription(), trainingPos);
					Set<OWLIndividual> trainingCoveredNeg = rs.hasType(def.getDescription(), trainingNeg);
					Set<OWLIndividual> testingCoveredPos = rs.hasType(def.getDescription(), testingPos);
					Set<OWLIndividual> testingCoveredNeg = rs.hasType(def.getDescription(), testingNeg);
										
					//add partial definition into the partial definition set
					Set<FoldInfor> valInfor = new TreeSet<FoldInfor>(new FoldInforComparator());
					PhaseInfor trainingInfor = new PhaseInfor(trainingPos.size(), 
							trainingNeg.size(), trainingCoveredPos.size(), 
							trainingCoveredNeg.size());
					PhaseInfor testingInfor = new PhaseInfor(testingPos.size(),
							testingNeg.size(), testingCoveredPos.size() , testingCoveredNeg.size());
					if (!allPartialDefinitions.containsKey(def.getDescription())) {
						//if the description is in the list before, just add the evaluation info for that description						
						valInfor.add(new FoldInfor(currFold, trainingInfor, testingInfor));
						allPartialDefinitions.put(def.getDescription(), valInfor);
					}
					else {						
						//if the description is in the set of partial definition before, just add new evaluation info
						allPartialDefinitions.get(def.getDescription()).add(new FoldInfor(currFold, trainingInfor, testingInfor));
					}
				}
				
				//store intended score of the current fold into all partial definitions set
				for (OWLClassExpression des : predictionScores.keySet()) {
					//if the description is in the list of all partial definition 
					if (allPartialDefinitions.containsKey(des)) {
						
						//check for the corresponding fold and assign the score
						boolean found = false;
						for (FoldInfor fold : allPartialDefinitions.get(des)) {
							if (fold.getFold() == currFold) {
								fold.setPredScore(predictionScores.get(des));
								found = true;
								break;
							}
						}
						
						if (!found)
							logger.error("Cannot find the corresponding fold for the partial definition");					
					}					
					else {
						logger.error("Cannot find the partial definition in all partial definitions set");					
					}
	
				}
								
			}	//for k folds


			//---------------------------------
			//end of k-fold cross validation
			//output result of the k-fold 
			//---------------------------------

			outputWriter("");
			outputWriter("Finished " + folds + "-folds cross-validation.");
			outputWriter("runtime: " + statOutput(df, runtime, "s"));
			outputWriter("#partial definitions: " + statOutput(df, noOfPartialDef, ""));
			outputWriter("avg. partial definition length: " + statOutput(df, partialDefinitionLength, ""));
			outputWriter("length: " + statOutput(df, length, ""));
			outputWriter("F-Measure on training set: " + statOutput(df, fMeasureTraining, "%"));		
			outputWriter("F-Measure: " + statOutput(df, fMeasure, "%"));
			outputWriter("predictive accuracy on training set: " + statOutput(df, accuracyTraining, "%") + " - corr/comp: " + 
					statOutput(df, trainingCorrectnessStat, "%") + "/" + statOutput(df, trainingCompletenessStat, "%"));		
			outputWriter("predictive accuracy: " + statOutput(df, accuracy, "%") + " - corr/comp: " + 
					statOutput(df, testingCorrectnessStat, "%") + "/" + statOutput(df, testingCompletenessStat, "%"));
			
			//display all partial definitions information produced in the validation group by partial definition
			outputWriter("=======================================");
			outputWriter("Partial definitions");
			outputWriter("=======================================");
			int count=1;
			for (OWLClassExpression def : allPartialDefinitions.keySet()) {
				
				double aScore = ParCELActualModelScoring.scoring(def, allPartialDefinitions.get(def), 
						folds, (int)partialDefinitionLength.getMax());
				
				outputWriter(count++ + (allReducedPartialDefinitions.contains(def)?"* ":". ") + 
						def +
						", length=" + OWLClassExpressionUtils.getLength(def) +
						", folds=" + allPartialDefinitions.get(def).size() +
						", aScore=" + df.format(aScore));
				
				for (FoldInfor info : allPartialDefinitions.get(def)) {
					
					//currently, actual score bases on the overall information of k-folds
					//therefore, actual model score of a partial definition is equal for all folds 
					info.setActualScore(aScore);
					
					outputWriter("\t-" + info.toString() + ", pScore=" + df.format(info.getPredScore()));
				}
			}
			
			//display all partial definitions information produced in the validation group by partial definition
			
			for (int curFold=0; curFold<folds; curFold++) {
				outputWriter("------------ " + curFold + " --------------");
				
				count=1;
				for (OWLClassExpression def : allPartialDefinitions.keySet()) {
					for (FoldInfor info : allPartialDefinitions.get(def)) {
						if (info.getFold() == curFold) {
							outputWriter(count++ + (allReducedPartialDefinitions.contains(def)?"* ":". ") +								
									def +
									", length=" + OWLClassExpressionUtils.getLength(def) +
									", folds=" + allPartialDefinitions.get(def).size());
							
							outputWriter("\ttraining=" + info.getTraining() + ", testing=" + info.getTesting() + 
									", pScore=" + df.format(info.getPredScore()) +
									", aScore=" + df.format(info.getActualScore()));
							
							break;	//no duplicated fold in the same description
						}
					}
				}
			}
			
	}


	@Override
	protected void outputWriter(String output) {
		logger.info(output);

		if (writeToFile)
			Files.appendToFile(outputFile, output + "\n");
	}

}
