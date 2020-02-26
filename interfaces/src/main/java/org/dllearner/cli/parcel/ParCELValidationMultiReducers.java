package org.dllearner.cli.parcel;

import java.text.DecimalFormat;
import java.util.*;

import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.dllearner.algorithms.parcel.ParCELAbstract;
import org.dllearner.algorithms.parcel.ParCELExtraNode;
import org.dllearner.algorithms.parcel.ParCELPosNegLP;
import org.dllearner.algorithms.parcel.reducer.ParCELReducer;
import org.dllearner.cli.CrossValidation;
import org.dllearner.cli.parcel.modeling.PhaseInfor;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.learningproblems.Heuristics;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;

/**
 * Use testing data to analyse the learning partial definitions.
 * 
 * @author An C. Tran
 * 
 */

public class ParCELValidationMultiReducers {

	Logger logger = Logger.getLogger(this.getClass());
	
	Map<ParCELReducer, ParCELvaliadationData> validationData = new HashMap<ParCELReducer, ParCELvaliadationData>();

	private OWLDataFactory dataFactory = OWLManager.getOWLDataFactory();

	/**
	 * Running k-fold cross evaluation for ParCEL-based algorithm with multi-reducers supported
	 * 
	 * @param la Learning algorithm
	 * @param lp Learning problem
	 * @param rs Reasoner
	 * @param folds Number of folds
	 * @param leaveOneOut Using leave-one-out 
	 */
	public ParCELValidationMultiReducers(AbstractCELA la, ParCELPosNegLP lp, AbstractReasonerComponent rs,
										 int folds, boolean leaveOneOut, int kkFold, Set<ParCELReducer> reducers) {

		//this validation support ParCEL only
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
		int[] splitsPos = CrossValidation.calculateSplits(posExamples.size(),folds);
		int[] splitsNeg = CrossValidation.calculateSplits(negExamples.size(),folds);


		// calculating training and test sets
		for(int i=0; i<folds; i++) {
			Set<OWLIndividual> testPos = CrossValidation.getTestingSet(posExamplesList, splitsPos, i);
			Set<OWLIndividual> testNeg = CrossValidation.getTestingSet(negExamplesList, splitsNeg, i);
			testingSetsPos.add(i, testPos);
			testingSetsNeg.add(i, testNeg);
			trainingSetsPos.add(i, CrossValidation.getTrainingSet(posExamples, testPos));
			trainingSetsNeg.add(i, CrossValidation.getTrainingSet(negExamples, testNeg));				
		}	

		if (reducers == null) {
			reducers = new HashSet<ParCELReducer>();
			reducers.add(parcel.getReducer());
		}
		else if (reducers.size() == 0)
			reducers.add(parcel.getReducer());

		//initialise the validation data for each reducer
		for (ParCELReducer reducer : reducers)
			validationData.put(reducer, new ParCELvaliadationData());

		//----------------------
		//end of setting up
		//----------------------

		//hold all partial definitions for further analysis
		//Map<Description, Set<FoldInfor>> allPartialDefinitions = new TreeMap<Description, Set<FoldInfor>>(new ConceptComparator());

		//-----------------------------------
		//start k-folds cross validation
		//-----------------------------------
		int maxPartialDefinitionLength=0;
		for(int currFold=0; (currFold<folds); currFold++) {

			//---------------------------
			//setting up the training data
			//---------------------------
			lp.setPositiveExamples(trainingSetsPos.get(currFold));
			lp.setNegativeExamples(trainingSetsNeg.get(currFold));

			//-------------------
			//start the learner
			//-------------------
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

			//--------------------------------
			//finish learning
			//process the learning result
			//--------------------------------		

			//calculate the learning time
			long algorithmDuration = System.nanoTime() - algorithmStartTime;

			//=======================================
			//loop on the reducers set
			//and make the evaluation on test set
			//=======================================
			for (ParCELReducer reducer : reducers) {

				ParCELvaliadationData reducerValData = validationData.get(reducer);

				reducerValData.runtime.addNumber(algorithmDuration/(double)1000000000);

				//reduced partial definitions of the current fold using the current reducer
				SortedSet<ParCELExtraNode> foldReducedPartialDefinitions = parcel.getReducedPartialDefinition(reducer);

				//get the learned concept
				List<OWLClassExpression> reducedDescriptions = new LinkedList<>();

				for (ParCELExtraNode def : foldReducedPartialDefinitions)
					reducedDescriptions.add(def.getDescription());

				OWLClassExpression concept = dataFactory.getOWLObjectUnionOf(new TreeSet<>(reducedDescriptions));

				//---------------------------			
				//validate learned result
				//---------------------------
				outputWriter("fold " + currFold + " - " + reducer.getClass().getSimpleName() + ":");

				Set<OWLIndividual> tmp = rs.hasType(concept, trainingSetsPos.get(currFold));
				Set<OWLIndividual> tmp2 = Sets.difference(trainingSetsPos.get(currFold), tmp);
				Set<OWLIndividual> tmp3 = rs.hasType(concept, trainingSetsNeg.get(currFold));

				outputWriter("  training set errors pos (" + tmp2.size() + "): " + tmp2);
				outputWriter("  training set errors neg (" + tmp3.size() + "): " + tmp3);

				tmp = rs.hasType(concept, testingSetsPos.get(currFold));
				tmp2 = Sets.difference(testingSetsPos.get(currFold), tmp);
				tmp3 = rs.hasType(concept, testingSetsNeg.get(currFold));

				outputWriter("  test set errors pos: " + tmp2);
				outputWriter("  test set errors neg: " + tmp3);


				// calculate training accuracies 
				int trainingCorrectPosClassified = getCorrectPosClassified(rs, concept, trainingSetsPos.get(currFold));
				int trainingCorrectNegClassified = getCorrectNegClassified(rs, concept, trainingSetsNeg.get(currFold));
				int trainingCorrectExamples = trainingCorrectPosClassified + trainingCorrectNegClassified;
				double trainingAccuracy = 100*((double)trainingCorrectExamples/(trainingSetsPos.get(currFold).size()+
						trainingSetsNeg.get(currFold).size()));			

				double trainingCompleteness = 100*(double)trainingCorrectPosClassified/trainingSetsPos.get(currFold).size();
				double trainingCorrectness = 100*(double)trainingCorrectNegClassified/trainingSetsNeg.get(currFold).size();

				reducerValData.accuracyTraining.addNumber(trainingAccuracy);
				reducerValData.trainingCompletenessStat.addNumber(trainingCompleteness);
				reducerValData.trainingCorrectnessStat.addNumber(trainingCorrectness);

				// calculate test accuracies
				int correctPosClassified = getCorrectPosClassified(rs, concept, testingSetsPos.get(currFold));
				int correctNegClassified = getCorrectNegClassified(rs, concept, testingSetsNeg.get(currFold));
				int correctExamples = correctPosClassified + correctNegClassified;
				double currAccuracy = 100*((double)correctExamples/(testingSetsPos.get(currFold).size()+
						testingSetsNeg.get(currFold).size()));

				double testingCompleteness = 100*(double)correctPosClassified/testingSetsPos.get(currFold).size();
				double testingCorrectness = 100*(double)correctNegClassified/testingSetsNeg.get(currFold).size();

				reducerValData.accuracy.addNumber(currAccuracy);
				reducerValData.testingCompletenessStat.addNumber(testingCompleteness);
				reducerValData.testingCorrectnessStat.addNumber(testingCorrectness);


				// calculate training F-Score
				int negAsPosTraining = rs.hasType(concept, trainingSetsNeg.get(currFold)).size();
				double precisionTraining = trainingCorrectPosClassified + negAsPosTraining == 0 ? 0 : trainingCorrectPosClassified / (double) (trainingCorrectPosClassified + negAsPosTraining);
				double recallTraining = trainingCorrectPosClassified / (double) trainingSetsPos.get(currFold).size();

				reducerValData.fMeasureTraining.addNumber(100*Heuristics.getFScore(recallTraining, precisionTraining));

				// calculate test F-Score
				int negAsPos = rs.hasType(concept, testingSetsNeg.get(currFold)).size();
				double precision = correctPosClassified + negAsPos == 0 ? 0 : correctPosClassified / (double) (correctPosClassified + negAsPos);
				double recall = correctPosClassified / (double) testingSetsPos.get(currFold).size();

				reducerValData.fMeasure.addNumber(100*Heuristics.getFScore(recall, precision));				

				//length of the definition
				reducerValData.length.addNumber(OWLClassExpressionUtils.getLength(concept));

				//print out the validation information				
				outputWriter("  training: " + trainingCorrectPosClassified + "/" + trainingSetsPos.get(currFold).size() + 
						" positive and " + trainingCorrectNegClassified + "/" + trainingSetsNeg.get(currFold).size() + " negative examples");
				outputWriter("  testing: " + correctPosClassified + "/" + testingSetsPos.get(currFold).size() + " correct positives, " 
						+ correctNegClassified + "/" + testingSetsNeg.get(currFold).size() + " correct negatives");
				outputWriter("  concept: " + concept);
				outputWriter("  accuracy: " + df.format(currAccuracy) +  "% (corr:"+ df.format(testingCorrectness) + 
						"%, comp:" + df.format(testingCompleteness) + "%) --- training:" + 
						df.format(trainingAccuracy) + "% (corr:"+ df.format(trainingCorrectness) + 
						"%, comp:" + df.format(trainingCompleteness) + "%)");
				outputWriter("  length: " + df.format(OWLClassExpressionUtils.getLength(concept)));
				outputWriter("  runtime: " + df.format(algorithmDuration/(double)1000000000) + "s");			

				outputWriter("  number of partial definitions: " + foldReducedPartialDefinitions.size() +
						"/" + parcel.getNumberOfPartialDefinitions());

				
				//no of partial definitions
				reducerValData.noOfPartialDef.addNumber(foldReducedPartialDefinitions.size());
				
				//conculate the avg. partial definition length = total length of the concept / no of partila definitions
				double pl = OWLClassExpressionUtils.getLength(concept)/(double)foldReducedPartialDefinitions.size();
				reducerValData.partialDefinitionLength.addNumber(pl);
				outputWriter("  avarage partial definition length: " + df.format(pl));		


				/*
				 * Analyse the partial definitions for the prediction model and the actual model
				 */


				//for (ParCELExtraNode node : foldReducedPartialDefinitions)
				//	allReducedPartialDefinitions.add(node.getDescription());

				//infer and display the partial definition information					
				//outputWriter("---------- Fold " + currFold + " -----------");

				//all partial definitions generated in the fold
				Set<ParCELExtraNode> foldPartialDefinitions = parcel.getPartialDefinitions();


				//get the training dataset of the current fold
				Set<OWLIndividual> trainingPos = trainingSetsPos.get(currFold);
				Set<OWLIndividual> trainingNeg = trainingSetsNeg.get(currFold);
				Set<OWLIndividual> testingPos = testingSetsPos.get(currFold);
				Set<OWLIndividual> testingNeg = testingSetsNeg.get(currFold);

				//---------------------
				// PREDICTION model
				//---------------------

				//calculate prediction-model-score for all partial definition generated of the current fold
				//Map<Description, Double> predictionScores = ParCELPredictionModelScoring.scoringComplex(
				//		foldPartialDefinitions, trainingPos);

				//----------------------------------------------------------
				// calculate fold information for each partial definition
				// Note that the above is for the whole definition,
				//	in this step, we are evaluating each p/definition
				//----------------------------------------------------------
				for (ParCELExtraNode def : foldPartialDefinitions) {

					Set<OWLIndividual> trainingCoveredPos = rs.hasType(def.getDescription(), trainingPos);
					Set<OWLIndividual> trainingCoveredNeg = rs.hasType(def.getDescription(), trainingNeg);
					Set<OWLIndividual> testingCoveredPos = rs.hasType(def.getDescription(), testingPos);
					Set<OWLIndividual> testingCoveredNeg = rs.hasType(def.getDescription(), testingNeg);

					//--------------
					//add partial definition into the partial definition set
					//--------------
					
					//1. calculate the training infor and testing info
					PhaseInfor trainingInfor = new PhaseInfor(trainingPos.size(),
							trainingNeg.size(), trainingCoveredPos.size(), 
							trainingCoveredNeg.size());

					PhaseInfor testingInfor = new PhaseInfor(testingPos.size(),
							testingNeg.size(), testingCoveredPos.size() , testingCoveredNeg.size());
					
					/*
					//2. add the partial definitions of the current fold into all partial definition set with the above information 
					if (!allPartialDefinitions.containsKey(def.getDescription())) {
						//if the description is in the list before, just add the evaluation info for that description
						Set<FoldInfor> valInfor = new TreeSet<FoldInfor>(new FoldInforComparator());
						valInfor.add(new FoldInfor(currFold, trainingInfor, testingInfor));
						allPartialDefinitions.put(def.getDescription(), valInfor);
					}
					else {						
						//if the description is in the set of partial definition before, just add new evaluation info
						allPartialDefinitions.get(def.getDescription()).add(new FoldInfor(currFold, trainingInfor, testingInfor));					
					}
					*/
					//update the max partial definition length
					if (maxPartialDefinitionLength < OWLClassExpressionUtils.getLength(def.getDescription()))
						maxPartialDefinitionLength = OWLClassExpressionUtils.getLength(def.getDescription());
					
				}	//for each partial definition in the list of all partial definitions of the current fold

				
				/*
				//store the prediction score of the current fold into all partial definitions set
				for (Description des : predictionScores.keySet()) {
					//if the description is in the list of all partial definition 
					if (allPartialDefinitions.containsKey(des)) {

						//check for the corresponding fold of the description and assign the score
						boolean found = false;
						for (FoldInfor fold : allPartialDefinitions.get(des)) {
							if (fold.getFold() == currFold) {
								fold.setPredScore(predictionScores.get(des));
								
								//add the current reducer into the list of reducers that select the partial definition as a part of the final definition
								if (reducedDescriptions.contains(des))
									fold.getSelectedByReducers().add(reducer);
								
								found = true;
								break;
							}
						}
						
						if (!found)
							logger.error("Cannot find the corresponding fold of the partial definition");					
					}					
					else
						logger.error("Cannot find the partial definition in all partial definitions set");					
					
					
				}	//for each description in the all partial definitions set of the current fold - update pScore
				*/
			}	//for each reducers

		}	//for k folds


		//------------------------------------------
		//end of k-fold cross validation
		//output result of the k-fold
		//TODO: display result for each reducer
		//------------------------------------------

		outputWriter("");
		outputWriter("Finished " + folds + "-folds cross-validation.");
		
		for (ParCELReducer reducer : reducers) {
			
			ParCELvaliadationData valData = validationData.get(reducer);

			outputWriter("----- Reducer: " + reducer.getClass().getSimpleName() + " ------");
			outputWriter("runtime: " + CrossValidation.statOutput(df, valData.runtime, "s"));
			outputWriter("#partial definitions: " + CrossValidation.statOutput(df, valData.noOfPartialDef, ""));
			outputWriter("avg. partial definition length: " + CrossValidation.statOutput(df, valData.partialDefinitionLength, ""));
			outputWriter("length: " + CrossValidation.statOutput(df, valData.length, ""));
			outputWriter("F-Measure on training set: " + CrossValidation.statOutput(df, valData.fMeasureTraining, "%"));		
			outputWriter("F-Measure: " + CrossValidation.statOutput(df, valData.fMeasure, "%"));
			outputWriter("predictive accuracy on training set: " + CrossValidation.statOutput(df, valData.accuracyTraining, "%") + 
					" - corr: " + CrossValidation.statOutput(df, valData.trainingCorrectnessStat, "%") + 
					" - comp: " + CrossValidation.statOutput(df, valData.trainingCompletenessStat, "%"));
			outputWriter("predictive accuracy on test set: " + CrossValidation.statOutput(df,valData. accuracy, "%") + 
					" - corr: " + CrossValidation.statOutput(df, valData.testingCorrectnessStat, "%") + 
					" - comp: " + CrossValidation.statOutput(df, valData.testingCompletenessStat, "%"));
		}
		
		//===================
		//ACTUAL model
		//===================
		
		//display all partial definitions information produced in the validation group by partial definition
		//and calculate the actual model score for the partial definition as well
		/*
		outputWriter("=======================================");
		outputWriter("Partial definitions");
		outputWriter("=======================================");
		int count=1;
		for (Description def : allPartialDefinitions.keySet()) {

			double aScore = ParCELActualModelScoring.scoring(def, allPartialDefinitions.get(def), 
					folds, maxPartialDefinitionLength);
			
			Set<FoldInfor> infors = allPartialDefinitions.get(def);

			outputWriter(count++ + ". " + 
					def.toManchesterSyntaxString(rs.getBaseURI(), rs.getPrefixes()) + 
					", length=" + def.getLength() +
					", folds=" + allPartialDefinitions.get(def).size() +
					", aScore=" + df.format(aScore));

			for (FoldInfor info : infors) {
				//assign the actual score for partial definitions
				//NOTE: currently, actual score bases on the overall information of k-folds
				//		therefore, actual model score of a partial definition is equal for all folds 
				info.setActualScore(aScore);

				outputWriter("\t-" + info.toString() + 
						", pScore=" + df.format(info.getPredScore()) +
						(info.getSelectedByReducers().size() > 0 ? (", selected-by: " + info.getSelectedBy()):""));
			}
			
		}
		*/

		//display all partial definitions information produced in the validation group by partial definition
		/*
		for (int curFold=0; curFold<folds; curFold++) {
			outputWriter("------------ " + curFold + " --------------");

			count=1;
			for (Description def : allPartialDefinitions.keySet()) {								
				for (FoldInfor info : allPartialDefinitions.get(def)) {
					if (info.getFold() == curFold) {
						outputWriter(count++ + ". " +								
								def.toManchesterSyntaxString(rs.getBaseURI(), rs.getPrefixes()) + 
								", length=" + def.getLength() +
								", folds=" + allPartialDefinitions.get(def).size());

						outputWriter("\ttraining=" + info.getTraining() + ", testing=" + info.getTesting() + 
								", pScore=" + df.format(info.getPredScore()) +
								", aScore=" + df.format(info.getActualScore()) +
								(info.getSelectedByReducers().size() > 0 ? (", selected-by: " + info.getSelectedBy()):""));

						break;	//no duplicated fold in the same description
					}
				}
			}
		}
		*/
		//post-processing to find out the best partial definition set



		//test again for all crosses based on the new partial definitions 


	}

	public static int getCorrectPosClassified(AbstractReasonerComponent rs, OWLClassExpression concept,
											  Set<OWLIndividual> testSetPos) {
		return rs.hasType(concept, testSetPos).size();
	}

	public static int getCorrectNegClassified(AbstractReasonerComponent rs, OWLClassExpression concept,
											  Set<OWLIndividual> testSetNeg) {
		return testSetNeg.size() - rs.hasType(concept, testSetNeg).size();
	}


	protected void outputWriter(String output) {
		logger.info(output);
	}







}
