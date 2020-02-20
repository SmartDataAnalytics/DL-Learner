package org.dllearner.cli.parcel;

import java.text.DecimalFormat;
import java.util.*;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.dllearner.algorithms.parcel.ParCELAbstract;
import org.dllearner.algorithms.parcel.ParCELExtraNode;
import org.dllearner.algorithms.parcel.ParCELPosNegLP;
import org.dllearner.algorithms.parcel.celoe.CELOEPartial;
import org.dllearner.algorithms.parcelex.ParCELExAbstract;
import org.dllearner.cli.CrossValidation;
import org.dllearner.cli.parcel.fortification.FortificationUtils;
import org.dllearner.cli.parcel.fortification.JaccardSimilarity;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.kb.OWLFile;
import org.dllearner.learningproblems.Heuristics;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;
import org.dllearner.utilities.statistics.Stat;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

/**
 * Add PDLL cross validation support to Jens Lehmann work (
 * {@link CrossValidation}). In this cross validation,
 * some more addition dimensions will be investigated such as:
 * number partial definitions, partial definition length, etc.
 *
 *
 * @author actran
 *
 */

public class ParCELExFortifiedCrossValidation3Phases extends CrossValidation {

	//pdef
	private Stat noOfPdefStat;
	private Stat noOfUsedPdefStat;
	private Stat avgUsedPartialDefinitionLengthStat;

	//cpdef
	private Stat noOfCpdefStat;
	private Stat noOfCpdefUsedStat;
	private Stat avgCpdefLengthStat;
	private Stat totalCPDefLengthStat;
	private Stat avgCpdefCoverageTrainingStat;


	//learning time
	private Stat learningTime;


	//fortify strategy statistical variables
	/*
	private Stat accuracyFortifyStat;
	private Stat correctnessFortifyStat;
	private Stat completenessFortifyStat;
	private Stat fmeasureFortifyStat;
	//private Stat avgFortifiedPartialDefinitionLengthStat;
	*/


	//blind fortification
	private Stat accuracyBlindFortifyStat;
	private Stat correctnessBlindFortifyStat;
	private Stat completenessBlindFortifyStat;
	private Stat fmeasureBlindFortifyStat;


	//labeled fortification
	private Stat labelFortifyCpdefTrainingCoverageStat;
	private Stat noOfLabelFortifySelectedCpdefStat;
	private Stat avgLabelCpdefLengthStat;
	private Stat labelFortifiedDefinitionLengthStat;
	private Stat accuracyLabelFortifyStat;
	private Stat correctnessLabelFortifyStat;
	private Stat completenessLabelFortifyStat;
	private Stat fmeasureLabelFortifyStat;



	//multi-step fortification
	protected Stat[][] accuracyPercentageFortifyStepStat;		//hold the fortified accuracy at 5,10,20,30,40,50% (multi-strategies)
	protected Stat[][] completenessPercentageFortifyStepStat;	//hold the fortified completeness at 5,10,20,30,40,50% (multi-strategies)
	protected Stat[][] correctnessPercentageFortifyStepStat;	//hold the fortified correctness at 5,10,20,30,40,50% (multi-strategies)
	protected Stat[][] fmeasurePercentageFortifyStepStat;	//hold the fortified correctness at 5,10,20,30,40,50% (multi-strategies)

	protected Stat[] noOfCpdefUsedMultiStepFortStat;


	protected double[][] accuracyHalfFullStep;
	protected double[][] fmeasureHalfFullStep;

	protected Stat[][] accuracyFullStepStat;
	protected Stat[][] fmeasureFullStepStat;
	protected Stat[][] correctnessFullStepStat;
	protected Stat[][] completenessFullStepStat;


	Logger logger = Logger.getLogger(this.getClass());

	protected boolean interupted = false;

	/**
	 * Default constructor
	 */

	public ParCELExFortifiedCrossValidation3Phases(AbstractCELA la, PosNegLP lp, AbstractReasonerComponent rs,
			int folds, boolean leaveOneOut, int noOfRuns) {
		super(la, lp, rs, folds, leaveOneOut); // TODO nrOfRuns not available in CV class
	}

	/**
	 * This is for PDLL cross validation
	 *
	 * @param la
	 * @param lp
	 * @param rs
	 * @param folds
	 * @param leaveOneOut
	 * @param noOfRuns Number of k-fold runs, i.e. the validation will run kk times of k-fold validations
	 */
	public ParCELExFortifiedCrossValidation3Phases(AbstractCELA la, ParCELPosNegLP lp, AbstractReasonerComponent rs,
												   int folds, boolean leaveOneOut, int noOfRuns) {

		super(); // do nothing

		//--------------------------
		//setting up
		//--------------------------
		DecimalFormat df = new DecimalFormat();

		// the training and test sets used later on
		List<Set<OWLIndividual>> trainingSetsPos = new LinkedList<>();
		List<Set<OWLIndividual>> trainingSetsNeg = new LinkedList<>();
		List<Set<OWLIndividual>> testSetsPos = new LinkedList<>();
		List<Set<OWLIndividual>> testSetsNeg = new LinkedList<>();
		List<Set<OWLIndividual>> fortificationSetsPos = new LinkedList<>();
		List<Set<OWLIndividual>> fortificationSetsNeg = new LinkedList<>();


		// get examples and shuffle them too
		Set<OWLIndividual> posExamples = lp.getPositiveExamples();
		List<OWLIndividual> posExamplesList = new LinkedList<>(posExamples);
		//Collections.shuffle(posExamplesList, new Random(1));
		Set<OWLIndividual> negExamples = lp.getNegativeExamples();
		List<OWLIndividual> negExamplesList = new LinkedList<>(negExamples);
		//Collections.shuffle(negExamplesList, new Random(2));

		String baseURI = rs.getBaseURI();
		Map<String, String> prefixes = rs.getPrefixes();

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

			//test sets
			Set<OWLIndividual> testPos = getTestingSet(posExamplesList, splitsPos, i);
			Set<OWLIndividual> testNeg = getTestingSet(negExamplesList, splitsNeg, i);
			testSetsPos.add(i, testPos);
			testSetsNeg.add(i, testNeg);

			//fortification training sets
			Set<OWLIndividual> fortPos = getTestingSet(posExamplesList, splitsPos, (i+1) % folds);
			Set<OWLIndividual> fortNeg = getTestingSet(negExamplesList, splitsNeg, (i+1) % folds);
			fortificationSetsPos.add(i, fortPos);
			fortificationSetsNeg.add(i, fortNeg);

			//training sets
			Set<OWLIndividual> trainingPos = getTrainingSet(posExamples, testPos);
			Set<OWLIndividual> trainingNeg = getTrainingSet(negExamples, testNeg);

			trainingPos.removeAll(fortPos);
			trainingNeg.removeAll(fortNeg);

			trainingSetsPos.add(i, trainingPos);
			trainingSetsNeg.add(i, trainingNeg);
		}



		// run the algorithm
		int terminatedBypartialDefinition=0, terminatedByCounterPartialDefinitions=0;

		//---------------------------------
		//k-fold cross validation
		//---------------------------------

		Stat runtimeAvg = new Stat();
		Stat runtimeMax = new Stat();
		Stat runtimeMin = new Stat();
		Stat runtimeDev = new Stat();

		Stat learningTimeAvg = new Stat();
		Stat learningTimeMax = new Stat();
		Stat learningTimeMin = new Stat();
		Stat learningTimeDev = new Stat();

		Stat noOfPartialDefAvg = new Stat();
		Stat noOfPartialDefDev = new Stat();
		Stat noOfPartialDefMax = new Stat();
		Stat noOfPartialDefMin = new Stat();

		Stat avgPartialDefLenAvg = new Stat();
		Stat avgPartialDefLenDev = new Stat();
		Stat avgPartialDefLenMax = new Stat();
		Stat avgPartialDefLenMin = new Stat();

		Stat avgFortifiedPartialDefLenAvg = new Stat();
		Stat avgFortifiedPartialDefLenDev = new Stat();
		Stat avgFortifiedPartialDefLenMax = new Stat();
		Stat avgFortifiedPartialDefLenMin = new Stat();

		Stat defLenAvg = new Stat();
		Stat defLenDev = new Stat();
		Stat defLenMax = new Stat();
		Stat defLenMin = new Stat();

		Stat trainingAccAvg = new Stat();
		Stat trainingAccDev= new Stat();
		Stat trainingAccMax = new Stat();
		Stat trainingAccMin = new Stat();

		Stat trainingCorAvg = new Stat();
		Stat trainingCorDev = new Stat();
		Stat trainingCorMax = new Stat();
		Stat trainingCorMin = new Stat();

		Stat trainingComAvg = new Stat();
		Stat trainingComDev = new Stat();
		Stat trainingComMax = new Stat();
		Stat trainingComMin = new Stat();

		Stat testingAccAvg = new Stat();
		Stat testingAccMax = new Stat();
		Stat testingAccMin = new Stat();
		Stat testingAccDev = new Stat();

		Stat fortifyAccAvg = new Stat();
		Stat fortifyAccMax = new Stat();
		Stat fortifyAccMin = new Stat();
		Stat fortifyAccDev = new Stat();


		Stat testingCorAvg = new Stat();
		Stat testingCorMax = new Stat();
		Stat testingCorMin = new Stat();
		Stat testingCorDev = new Stat();

		Stat fortifyCorAvg = new Stat();
		Stat fortifyCorMax = new Stat();
		Stat fortifyCorMin = new Stat();
		Stat fortifyCorDev = new Stat();

		Stat testingComAvg = new Stat();
		Stat testingComMax = new Stat();
		Stat testingComMin = new Stat();
		Stat testingComDev = new Stat();

		Stat fortifyComAvg = new Stat();
		Stat fortifyComMax = new Stat();
		Stat fortifyComMin = new Stat();
		Stat fortifyComDev = new Stat();


		Stat testingFMeasureAvg = new Stat();
		Stat testingFMeasureMax = new Stat();
		Stat testingFMeasureMin = new Stat();
		Stat testingFMeasureDev = new Stat();

		Stat trainingFMeasureAvg = new Stat();
		Stat trainingFMeasureMax = new Stat();
		Stat trainingFMeasureMin = new Stat();
		Stat trainingFMeasureDev = new Stat();

		Stat fortifyFmeasureAvg = new Stat();
		Stat fortifyFmeasureMax = new Stat();
		Stat fortifyFmeasureMin = new Stat();
		Stat fortifyFmeasureDev = new Stat();


		Stat noOfDescriptionsAgv = new Stat();
		Stat noOfDescriptionsMax = new Stat();
		Stat noOfDescriptionsMin = new Stat();
		Stat noOfDescriptionsDev = new Stat();

		Stat noOfCounterPartialDefinitionsAvg = new Stat();
		Stat noOfCounterPartialDefinitionsDev = new Stat();
		Stat noOfCounterPartialDefinitionsMax = new Stat();
		Stat noOfCounterPartialDefinitionsMin = new Stat();

		Stat noOfCounterPartialDefinitionsUsedAvg = new Stat();
		Stat noOfCounterPartialDefinitionsUsedDev = new Stat();
		Stat noOfCounterPartialDefinitionsUsedMax = new Stat();
		Stat noOfCounterPartialDefinitionsUsedMin = new Stat();


		/*
		long orthAllCheckCountFold[] = new long[5];
		long orthSelectedCheckCountFold[] = new long[5];

		long orthAllCheckCountTotal[] = new long[5];
		long orthSelectedCheckCountTotal[] = new long[5];


		orthAllCheckCountTotal[0] = orthAllCheckCountTotal[1] = orthAllCheckCountTotal[2] =
			orthAllCheckCountTotal[3] = orthAllCheckCountTotal[4] = 0;

		orthSelectedCheckCountTotal[0] = orthSelectedCheckCountTotal[1] = orthSelectedCheckCountTotal[2] =
			orthSelectedCheckCountTotal[3] = orthSelectedCheckCountTotal[4] = 0;
		 */


		//----------------------------------------------------------------------
		//loading ontology into Pellet reasoner for checking
		//the orthogonality and satisfiability (fortification training strategy)
		//----------------------------------------------------------------------
		long ontologyLoadStarttime = System.nanoTime();
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = ((OWLFile)la.getReasoner().getSources().iterator().next()).createOWLOntology(manager);
		outputWriter("Ontology created, axiom count: " + ontology.getAxiomCount());
		PelletReasoner pelletReasoner = PelletReasonerFactory.getInstance().createReasoner(ontology);
		outputWriter("Pellet creared and binded with the ontology: " + pelletReasoner.getReasonerName());
		long ontologyLoadDuration = System.nanoTime() - ontologyLoadStarttime;
		outputWriter("Total time for creating and binding ontology: " + ontologyLoadDuration/1000000000d + "ms");

		OWLDataFactory dataFactory = OWLManager.getOWLDataFactory();


		for (int kk=0; kk < noOfRuns; kk++) {

			//general statistics
			runtime = new Stat();
			learningTime = new Stat();
			length = new Stat();
			totalNumberOfDescriptions = new Stat();


			//pdef
			noOfPdefStat = new Stat();
			noOfUsedPdefStat = new Stat();
			avgUsedPartialDefinitionLengthStat = new Stat();

			//cpdef
			noOfCpdefStat = new Stat();
			noOfCpdefUsedStat = new Stat();
			totalCPDefLengthStat = new Stat();
			avgCpdefLengthStat = new Stat();
			avgCpdefCoverageTrainingStat = new Stat();


			//training
			accuracyTraining = new Stat();
			trainingCorrectnessStat= new Stat();
			trainingCompletenessStat = new Stat();
			fMeasureTraining = new Stat();

			//test
			accuracy = new Stat();
			fMeasure = new Stat();
			testingCorrectnessStat = new Stat();
			testingCompletenessStat = new Stat();


			//blind fortification
			accuracyBlindFortifyStat = new Stat();
			correctnessBlindFortifyStat = new Stat();
			completenessBlindFortifyStat = new Stat();
			fmeasureBlindFortifyStat = new Stat();


			//labled fortification
			labelFortifyCpdefTrainingCoverageStat = new Stat();
			noOfLabelFortifySelectedCpdefStat = new Stat();
			avgLabelCpdefLengthStat = new Stat();
			labelFortifiedDefinitionLengthStat = new Stat();
			accuracyLabelFortifyStat = new Stat();
			correctnessLabelFortifyStat= new Stat();
			completenessLabelFortifyStat = new Stat();
			fmeasureLabelFortifyStat = new Stat();


			int noOfStrategies = FortificationUtils.strategyNames.length;


			//fortification accuracy
			accuracyPercentageFortifyStepStat = new Stat[noOfStrategies][6];		//6 elements for six values of 5%, 10%, ..., 50%
			completenessPercentageFortifyStepStat = new Stat[noOfStrategies][6];
			correctnessPercentageFortifyStepStat = new Stat[noOfStrategies][6];
			fmeasurePercentageFortifyStepStat = new Stat[noOfStrategies][6];


			//initial fortification accuracy by PERCENTAGE
			for (int i=0; i<noOfStrategies; i++) {
				for (int j=0; j<6; j++) {
					accuracyPercentageFortifyStepStat[i][j] = new Stat();
					completenessPercentageFortifyStepStat[i][j] = new Stat();
					correctnessPercentageFortifyStepStat[i][j] = new Stat();
					fmeasurePercentageFortifyStepStat[i][j] = new Stat();
				}
			}

			//number of cpdef corresponding to 5%, 10%, ..., 50% (for stat.)
			noOfCpdefUsedMultiStepFortStat = new Stat[noOfStrategies];
			for (int i=0; i<6; i++)
				noOfCpdefUsedMultiStepFortStat[i] = new Stat();

			int minOfHalfCpdef = Integer.MAX_VALUE;
			int minCpdef = Integer.MAX_VALUE;

			//run n-fold cross validations
			for(int currFold=0; (currFold<folds); currFold++) {


				outputWriter("//---------------\n" + "// Fold " + currFold + "/" + folds + "\n//---------------");
				outputWriter("Training: " + trainingSetsPos.get(currFold).size() + "/" + trainingSetsNeg.get(currFold).size()
						+ ", test:" + testSetsPos.get(currFold).size() + "/" + testSetsNeg.get(currFold).size()
						+ ", fort: " + fortificationSetsPos.get(currFold).size() + "/" + fortificationSetsNeg.get(currFold).size()
					);


				if (this.interupted) {
					outputWriter("Cross validation has been interupted");
					return;
				}

				//-----------------------------------------------------
				//	1. Learn the DEFINITIONS
				//		Both pdef and cpdef may be generated
				//-----------------------------------------------------
				outputWriter("** Phase 1 - Learning definition");
				outputWriter("Timeout="	+ ((ParCELExAbstract)la).getMaxExecutionTimeInSeconds() + "s");

				//set training example sets
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
					System.out.println("Out of memory at " + (System.currentTimeMillis() - algorithmStartTime)/1000 + "s");
				}

				long algorithmDuration = System.nanoTime() - algorithmStartTime;
				runtime.addNumber(algorithmDuration/(double)1000000000);

				//learning time, does not include the reduction time
				long learningMili = ((ParCELAbstract)la).getLearningTime();
				learningTime.addNumber(learningMili/(double)1000);


				//--------------------------------
				//	FINISH learning
				//--------------------------------


				//cast the la into ParCELExAbstract for easier accessing
				ParCELExAbstract parcelEx = (ParCELExAbstract)la;


				//get the learned DEFINITION (union)
				OWLClassExpression concept = parcelEx.getUnionCurrenlyBestDescription();
				int conceptLength = OWLClassExpressionUtils.getLength(concept);

				length.addNumber(conceptLength);

				outputWriter("Learning finished.  Total number of pdefs: " + parcelEx.getPartialDefinitions().size() + ". Number of pdef used: " + parcelEx.getNoOfReducedPartialDefinition());

				//cpdef: some stat information
				int noOfUsedCpdef = parcelEx.getNumberOfCounterPartialDefinitionUsed();
				int noOfCpdef = parcelEx.getCounterPartialDefinitions().size();
				noOfCpdefStat.addNumber(noOfCpdef);
				noOfCpdefUsedStat.addNumber(noOfUsedCpdef);

				//pdef: some stat information
				//Set<ParCELExtraNode> partialDefinitions = parcelEx.getReducedPartialDefinition();
				long noOfPdef = parcelEx.getNumberOfPartialDefinitions();
				long noOfUsedPdef = parcelEx.getNoOfReducedPartialDefinition();
				double avgPdefLength = conceptLength / (double)noOfUsedPdef;
				noOfPdefStat.addNumber(noOfPdef);
				noOfUsedPdefStat.addNumber(noOfUsedPdef);
				avgUsedPartialDefinitionLengthStat.addNumber(avgPdefLength);

				//descriptions
				totalNumberOfDescriptions.addNumber(parcelEx.getTotalNumberOfDescriptionsGenerated());


				//print the coverage of the counter partial definitions
				outputWriter("Number of counter partial definitions: " + noOfCpdef);


				//--------------------------------------
				//get the COUNTER PARTIAL DEFINITIONs
				//--------------------------------------
				//sorted by training coverage by default
				TreeSet<CELOEPartial.PartialDefinition> counterPartialDefinitions = new TreeSet<>(new FortificationUtils.CoverageComparator());

				//-------------------------------
				//training sets
				//-------------------------------
				Set<OWLIndividual> curFoldPosTrainingSet = trainingSetsPos.get(currFold);
				Set<OWLIndividual> curFoldNegTrainingSet = trainingSetsNeg.get(currFold);

				int trainingPosSize = curFoldPosTrainingSet.size() ;
				int trainingNegSize = curFoldNegTrainingSet.size();


				//-----------------------
				// 2. Check if any CPDEFs generated
				// 	Note that this algorithm generate both pdefs and cpdef
				//	However, sometime there is no cpdef as the definition had been found "too" fast
				//	Therefore, we will reverse training set to produce some cpdef if necessary
				//-----------------------

				if (noOfCpdef < 5) {
					//================================================================
					//2. Phase 2: Learn Counter Partial Definitions
					// 		Reverse the pos/neg and let the learner start
					//================================================================

					outputWriter("* Number of counter partial definitions is too small, reverse the examples and learn again!!!");

					//reverse the pos/neg examples
					lp.setPositiveExamples(trainingSetsNeg.get(currFold));
					lp.setNegativeExamples(trainingSetsPos.get(currFold));

					//re-initialize the learner
					try {
						lp.init();
						la.init();
					} catch (ComponentInitException e) {
						e.printStackTrace();
					}

					outputWriter("\n** Phase 2 - Learning COUNTER PARTIAL DEFINITIONS");
					outputWriter("Timeout="	+ ((ParCELExAbstract)la).getMaxExecutionTimeInSeconds() + "s");

					//start the learner
					long algorithmStartTime1 = System.nanoTime();
					try {
						la.start();
					}
					catch (OutOfMemoryError e) {
						System.out.println("out of memory at " + (System.currentTimeMillis() - algorithmStartTime1)/1000 + "s");
					}



					//calculate the counter partial definitions' avg. coverage
					//(note that the positive and negative examples are swapped)
					for (ParCELExtraNode cpdef : ((ParCELExAbstract)la).getPartialDefinitions()) {

						int trainingCp = cpdef.getCoveredPositiveExamples().size();	//positive examples of cpdef is the

						counterPartialDefinitions.add(new CELOEPartial.PartialDefinition( dataFactory.getOWLObjectComplementOf(cpdef.getDescription()), trainingCp));

						avgCpdefCoverageTrainingStat.addNumber(trainingCp/(double)trainingSetsNeg.get(currFold).size());
					}

					outputWriter("Finish learning, number of counter partial definitions: " + counterPartialDefinitions.size());
				}
				else {
					//calculate the counter partial definitions' avg coverage
					for (ParCELExtraNode cpdef : parcelEx.getCounterPartialDefinitions()) {

						int trainingCn = cpdef.getCoveredNegativeExamples().size();

						counterPartialDefinitions.add(new CELOEPartial.PartialDefinition(cpdef.getDescription(), trainingCn));

						avgCpdefCoverageTrainingStat.addNumber(trainingCn/(double)trainingPosSize);
					}

				}


				outputWriter("------------------------------");


				//-----------------------------
				//TRAINING accuracy
				//-----------------------------

				//cp, cn of training sets
				Set<OWLIndividual> cpTraining = rs.hasType(concept, trainingSetsPos.get(currFold));		//positive examples covered by the learned concept
				//Set<Individual> upTraining = Helper.difference(trainingSetsPos.get(currFold), cpTraining);	//false negative (pos as neg)
				Set<OWLIndividual> cnTraining = rs.hasType(concept, trainingSetsNeg.get(currFold));		//false positive (neg as pos)


				//training completeness, correctness and accuracy
				int trainingCorrectPosClassified = cpTraining.size();
				int trainingCorrectNegClassified = trainingNegSize - cnTraining.size();	//getCorrectNegClassified(rs, concept, trainingSetsNeg.get(currFold));
				int trainingCorrectExamples = trainingCorrectPosClassified + trainingCorrectNegClassified;

				double trainingAccuracy = 100*((double)trainingCorrectExamples/(trainingPosSize + trainingNegSize));
				double trainingCompleteness = 100*(double)trainingCorrectPosClassified/trainingPosSize;
				double trainingCorrectness = 100*(double)trainingCorrectNegClassified/trainingNegSize;

				accuracyTraining.addNumber(trainingAccuracy);
				trainingCompletenessStat.addNumber(trainingCompleteness);
				trainingCorrectnessStat.addNumber(trainingCorrectness);

				//training F-Measure
				int negAsPosTraining = cnTraining.size();
				double precisionTraining = (trainingCorrectPosClassified + negAsPosTraining) == 0 ?
						0 : trainingCorrectPosClassified / (double) (trainingCorrectPosClassified + negAsPosTraining);
				double recallTraining = trainingCorrectPosClassified / (double) trainingPosSize;
				double currFmeasureTraining = 100 * Heuristics.getFScore(recallTraining, precisionTraining);
				fMeasureTraining.addNumber(currFmeasureTraining);



				//----------------------
				//TEST accuracy
				//----------------------

				//calculate the coverage
				Set<OWLIndividual> curFoldPosTestSet = testSetsPos.get(currFold);
				Set<OWLIndividual> curFoldNegTestSet = testSetsNeg.get(currFold);

				int testingPosSize = curFoldPosTestSet.size();
				int testingNegSize = curFoldNegTestSet.size();

				Set<OWLIndividual> cpTest = rs.hasType(concept, curFoldPosTestSet);		//positive examples covered by the learned concept
				Set<OWLIndividual> upTest = Sets.difference(curFoldPosTestSet, cpTest);	//false negative (pos as neg)
				Set<OWLIndividual> cnTest = rs.hasType(concept, curFoldNegTestSet);		//false positive (neg as pos)


				//calculate test accuracies
				int correctTestPosClassified = cpTest.size();	//covered pos. in test set
				int correctTestNegClassified = testingNegSize - cnTest.size();	//uncovered neg in test set
				int correctTestExamples = correctTestPosClassified + correctTestNegClassified;

				double testingCompleteness = 100*(double)correctTestPosClassified/testingPosSize;
				double testingCorrectness = 100*(double)correctTestNegClassified/testingNegSize;
				double currAccuracy = 100*((double)correctTestExamples/(testingPosSize + testingNegSize));

				accuracy.addNumber(currAccuracy);
				testingCompletenessStat.addNumber(testingCompleteness);
				testingCorrectnessStat.addNumber(testingCorrectness);


				//F-Measure test set
				int negAsPos = cnTest.size();
				double testPrecision = correctTestPosClassified + negAsPos == 0 ?
						0 : correctTestPosClassified / (double) (correctTestPosClassified + negAsPos);
				double testRecall = correctTestPosClassified / (double) testingPosSize;
				double currFmeasureTest = 100 * Heuristics.getFScore(testRecall, testPrecision);

				fMeasure.addNumber(currFmeasureTest);


				//==================================================
				//FORTIFICATION
				//==================================================

				FortificationUtils.FortificationResult[] multiStepFortificationResult = new FortificationUtils.FortificationResult[noOfStrategies];



				//---------------------------------
				// Fortification - ALL CPDEFs
				//  (BLIND Fortification)
				//---------------------------------
				//NOTE:
				//Since this will iterate all cpdef, we will calculate score for all other fortification strategies
				// training coverage (done), jaccard, fortification training,

				outputWriter("---------------------------------------------------------------");
				outputWriter("BLIND fortification - All counter partial defintions are used");
				outputWriter("---------------------------------------------------------------");


				//get the set of pos and neg (in the test set) covered by counter partial definition
				Set<OWLIndividual> cpdefPositiveCovered = new HashSet<>();
				Set<OWLIndividual> cpdefNegativeCovered = new HashSet<>();

				long totalCPDefLength = 0;


				//---------------------------------------------------
				//variables for fortification training
				//---------------------------------------------------

				//fortification validation (FV) dataset
				Set<OWLIndividual> fortificationTrainingPos = fortificationSetsPos.get(currFold);
				Set<OWLIndividual> fortificationTrainingNeg= fortificationSetsNeg.get(currFold);

				Set<OWLIndividual> allFortificationExamples = new HashSet<>();

				allFortificationExamples.addAll(fortificationTrainingPos);
				allFortificationExamples.addAll(fortificationTrainingNeg);	//duplicate will be remove automatically


				//New Jaccard Similarity
				JaccardSimilarity newJaccardSimilarity = new JaccardSimilarity(pelletReasoner);


				///------------------------------------------------------------
				//start the BLIND fortification and
				// 	calculate the scores for other methods (use a common loop)
				//------------------------------------------------------------
				int tmp_id = 1;
				int count = 1;
				for (CELOEPartial.PartialDefinition negCpdef : counterPartialDefinitions) {

					//in parcelEx, the cpdefs are negated by default ==> remove negation before process them
					OWLClassExpression cpdef = ((OWLObjectComplementOf)negCpdef.getDescription()).getOperand();

					//assign id for cpdef for debugging purpose
					negCpdef.setId("#" + tmp_id++);


					//--------------------
					//BLIND fortification
					//--------------------

					//cp and cn of the current cpdef
					Set<OWLIndividual> cpdefCp = rs.hasType(cpdef, curFoldPosTestSet);
					Set<OWLIndividual> cpdefCn = rs.hasType(cpdef, curFoldNegTestSet);


					//--------------------------------
					//Fortification Validation (FV)
					//--------------------------------
					Set<OWLIndividual> fortCp = rs.hasType(cpdef, fortificationTrainingPos);
					Set<OWLIndividual> fortCn = rs.hasType(cpdef, fortificationTrainingNeg);


					Set<OWLIndividual> conceptCp = rs.hasType(concept, fortificationTrainingPos);
					Set<OWLIndividual> conceptCn = rs.hasType(concept, fortificationTrainingNeg);

					int cp = fortCp.size();
					int cn = fortCn.size();

					//these are used to compute common instances between cpdef and learnt concept
					Set<OWLIndividual> commonCp = new HashSet<>();
					Set<OWLIndividual> commonCn = new HashSet<>();
					commonCp.addAll(fortCp);
					commonCn.addAll(fortCn);
					commonCp.removeAll(conceptCp);
					commonCn.removeAll(conceptCn);

					double fortificationValidationScore = FortificationUtils.fortificationScore(pelletReasoner, cpdef, concept,
							cp, cn, fortificationTrainingPos.size(), fortificationTrainingNeg.size(),
							cp-commonCp.size(), cn-commonCn.size(), ((ParCELExAbstract)la).getMaximumHorizontalExpansion());


					//----------------------------
					//Overlap + Similarity Score
					//----------------------------
					double overlapNeg = FortificationUtils.getConceptOverlapSimple(fortCn, conceptCn);
					double overlapPos = FortificationUtils.getConceptOverlapSimple(fortCp, conceptCp);


					//---------------
					//NEW- JACCARD
					//---------------
					double newJaccardSimilarityScore = 0;
					try {
						newJaccardSimilarityScore = newJaccardSimilarity.getJaccardSimilarityComplex(concept, cpdef);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					double conceptOverlapSimilairtyScore = overlapNeg + newJaccardSimilarityScore;

					double similarityPosNegScore = overlapNeg - 0.5 * overlapPos;


					//---------------------------
					//TRAINING COVERAGE Score
					//---------------------------
					double trainingCoverageScore = cpdefCn.size()/(double)curFoldNegTrainingSet.size();


					//----------------------------------
					//Assign all scores to the CPDEF
					//new scoring: 21/4/2013
					//----------------------------------
					double allScores = conceptOverlapSimilairtyScore + fortificationValidationScore
					+ trainingCoverageScore*0.5;

					double randomScore = new Random().nextDouble();

					negCpdef.setAdditionValue(0, trainingCoverageScore);		//no of neg. examples in training set covered by the cpdef
					negCpdef.setAdditionValue(1, conceptOverlapSimilairtyScore);		//can be used to infer jaccard overlap score
					negCpdef.setAdditionValue(2, fortificationValidationScore);	//fortification validation strategy
					negCpdef.setAdditionValue(3, similarityPosNegScore);
					negCpdef.setAdditionValue(4, newJaccardSimilarityScore);
					negCpdef.setAdditionValue(5, allScores);
					negCpdef.setAdditionValue(6, randomScore);


					//------------------------
					//BLIND fortification
					//------------------------
					boolean cpChanged = cpdefPositiveCovered.addAll(cpdefCp);
					boolean cnChanged = cpdefNegativeCovered.addAll(cpdefCn);

					cpdefPositiveCovered.addAll(cpdefCp);
					cpdefNegativeCovered.addAll(cpdefCn);


					totalCPDefLength += OWLClassExpressionUtils.getLength(cpdef);

					String changed = "";
					if (cpChanged || cnChanged) {
						changed = "(" + (cpChanged?"-":"") + (cnChanged?"+":"") + ")";

						outputWriter(count++ + changed + ". " + FortificationUtils.getCpdefString(negCpdef, baseURI, prefixes)
								+ ", cp=" + rs.hasType(cpdef, curFoldPosTestSet)
								+ ", cn=" + rs.hasType(cpdef, curFoldNegTestSet));
					}
					else if (logger.isDebugEnabled()) {
						logger.debug(count++ + changed + ". " + FortificationUtils.getCpdefString(negCpdef, baseURI, prefixes)
								+ ", cp=" + rs.hasType(cpdef, curFoldPosTestSet)
								+ ", cn=" + rs.hasType(cpdef, curFoldNegTestSet));
					}
					/*
					if (cpChanged || cnChanged)
						changed = "(" + (cpChanged?"-":"") + (cnChanged?"+":"") + ")";

					outputWriter(count++ + changed + ". " + FortificationUtils.getCpdefString(negCpdef, baseURI, prefixes)
							+ ", cp=" + rs.hasType(cpdef, curFoldPosTestSet)
							+ ", cn=" + rs.hasType(cpdef, curFoldNegTestSet));
					*/
				}

				outputWriter( " * Blind fortifcation summary: cp=" + cpdefPositiveCovered + " --- cn=" + cpdefNegativeCovered);


				outputWriter("test set errors pos (" + upTest.size() + "): " + upTest);
				outputWriter("test set errors neg (" + cnTest.size() + "): " + cnTest);

				//-----------------------------------------
				//calculate BLIND fortification accuracy
				//-----------------------------------------

				//fortify definition length: total length of all cpdef
				totalCPDefLengthStat.addNumber(totalCPDefLength);
				double avgCPDefLength = totalCPDefLength/(double)counterPartialDefinitions.size();
				avgCpdefLengthStat.addNumber(avgCPDefLength);

				//accuracy, completeness, correctness
				int oldSizePosFort = cpdefPositiveCovered.size();
				int oldSizeNegFort = cpdefNegativeCovered.size();

				cpdefPositiveCovered.removeAll(cpTest);
				cpdefNegativeCovered.removeAll(cnTest);

				int commonPos = oldSizePosFort - cpdefPositiveCovered.size();
				int commonNeg = oldSizeNegFort - cpdefNegativeCovered.size();


				int cpFort = cpTest.size() - commonPos;	//positive examples covered by fortified definition
				int cnFort = cnTest.size() - commonNeg;	//negative examples covered by fortified definition

				//correctness = un/negSize
				double blindFortificationCorrectness = 100 *  (curFoldNegTestSet.size() - cnFort)/(double)(curFoldNegTestSet.size());

				//completeness = cp/posSize
				double blindFortificationCompleteness = 100 * (cpFort)/(double)curFoldPosTestSet.size();

				//accuracy = (cp + un)/(pos + neg)
				double blindFortificationAccuracy = 100 * (cpFort + (curFoldNegTestSet.size() - cnFort))/
						(double)(curFoldPosTestSet.size() + curFoldNegTestSet.size());

				//precision = right positive classified / total positive classified
				//          = cp / (cp + negAsPos)
				double blindPrecission = (cpFort + cnFort) == 0 ? 0 : cpFort / (double)(cpFort + cnFort);

				//recall = right positive classified / total positive
				double blindRecall = cpFort / (double)curFoldPosTestSet.size();

				double blindFmeasure = 100 * Heuristics.getFScore(blindRecall, blindPrecission);

				//STAT values for Blind fortification
				correctnessBlindFortifyStat.addNumber(blindFortificationCorrectness);
				completenessBlindFortifyStat.addNumber(blindFortificationCompleteness);
				accuracyBlindFortifyStat.addNumber(blindFortificationAccuracy);
				fmeasureBlindFortifyStat.addNumber(blindFmeasure);

				//----------------------------------
				//end of blind fortification
				//----------------------------------

				//========================================================
				// process other fortification strategies (except BLIND)
				//========================================================




				int INDEX;

				//---------------------------------------
				/// 1. Fortification - TRAINGING COVERAGE
				//---------------------------------------
				outputWriter("---------------------------------------------");
				outputWriter("Fortification - TRAINGING COVERAGE");
				outputWriter("---------------------------------------------");

				INDEX = FortificationUtils.TRAINING_COVERAGE_INDEX;

				//counter partial definition is sorted by training coverage by default ==> don't need to sort the cpdef set
				multiStepFortificationResult[INDEX] = FortificationUtils.fortifyAccuracyMultiSteps(
						rs, concept, counterPartialDefinitions, curFoldPosTestSet, curFoldNegTestSet, true);

				//accumulate the accuracy, fmeasure,... for the TRAINING_COVERAGE strategy at 5%, 10%, 20%,...
				for (int i=0; i<6; i++) {	//6: the number of steps: 5%, 10%, 20%,..., 50%
					accuracyPercentageFortifyStepStat[INDEX][i].
					addNumber(multiStepFortificationResult[INDEX].fortificationAccuracy[i+1]);
					completenessPercentageFortifyStepStat[INDEX][i].
					addNumber(multiStepFortificationResult[INDEX].fortificationCompleteness[i+1]);
					correctnessPercentageFortifyStepStat[INDEX][i].
					addNumber(multiStepFortificationResult[INDEX].fortificationCorrectness[i+1]);
					fmeasurePercentageFortifyStepStat[INDEX][i].
					addNumber(multiStepFortificationResult[INDEX].fortificationFmeasure[i+1]);
				}

				/* error: minCpdef has not assigned the value
				for (int i=0; i<minCpdef; i++) {
					outputWriter((i+1) + ": " + multiStepFortificationResult[INDEX].fortificationAccuracyStepByStep[i]
						+ "\t" + multiStepFortificationResult[INDEX].fortificationCorrectnessStepByStep[i]
						+ "\t" + multiStepFortificationResult[INDEX].fortificationCompletenessStepByStep[i]
					);
				}
				*/


				//------------------------------------------------
				/// 2. Fortification - CONCEPT SIMILARITY & OVERLAP
				//------------------------------------------------
				outputWriter("---------------------------------------------");
				outputWriter("Fortification - CONCEPT SIMILARITY & OVERLAP");
				outputWriter("---------------------------------------------");

				INDEX = FortificationUtils.CONCEPT_OVERL_SIM_INDEX;

				SortedSet<CELOEPartial.PartialDefinition> similarityAndOverlapCpdef = new TreeSet<>(new FortificationUtils.AdditionalValueComparator(1));
				similarityAndOverlapCpdef.addAll(counterPartialDefinitions);


				//counter partial definition is sorted by training coverage by default ==> don't need to sort the cpdef set
				multiStepFortificationResult[INDEX] = FortificationUtils.fortifyAccuracyMultiSteps(
						rs, concept, similarityAndOverlapCpdef, curFoldPosTestSet, curFoldNegTestSet, true);

				//accumulate the accuracy, fmeasure,... for the TRAINING_COVERAGE strategy at 5%, 10%, 20%,...
				for (int i=0; i<6; i++) {	//6: the number of steps: 5%, 10%, 20%,..., 50%
					accuracyPercentageFortifyStepStat[INDEX][i].
					addNumber(multiStepFortificationResult[INDEX].fortificationAccuracy[i+1]);
					completenessPercentageFortifyStepStat[INDEX][i].
					addNumber(multiStepFortificationResult[INDEX].fortificationCompleteness[i+1]);
					correctnessPercentageFortifyStepStat[INDEX][i].
					addNumber(multiStepFortificationResult[INDEX].fortificationCorrectness[i+1]);
					fmeasurePercentageFortifyStepStat[INDEX][i].
					addNumber(multiStepFortificationResult[INDEX].fortificationFmeasure[i+1]);
				}

				/* error: minCpdef has not assigned the value
				for (int i=0; i< minCpdef; i++) {
					outputWriter((i+1) + ": " + multiStepFortificationResult[INDEX].fortificationAccuracyStepByStep[i]
						+ "\t" + multiStepFortificationResult[INDEX].fortificationCorrectnessStepByStep[i]
						+ "\t" + multiStepFortificationResult[INDEX].fortificationCompletenessStepByStep[i]
					);
				}
				*/

				//------------------------------------------------
				/// 3. Fortification - FORTIFICATION VALIDATION
				//------------------------------------------------
				outputWriter("---------------------------------------------");
				outputWriter("Fortification - FORTIFICATION VALIDATION");
				outputWriter("---------------------------------------------");

				INDEX = FortificationUtils.FORTIFICATION_VALIDATION_INDEX;

				SortedSet<CELOEPartial.PartialDefinition> fortificationValidationCpdef = new TreeSet<>(new FortificationUtils.AdditionalValueComparator(2));
				fortificationValidationCpdef.addAll(counterPartialDefinitions);


				//counter partial definition is sorted by training coverage by default ==> don't need to sort the cpdef set
				multiStepFortificationResult[INDEX] = FortificationUtils.fortifyAccuracyMultiSteps(
						rs, concept, fortificationValidationCpdef, curFoldPosTestSet, curFoldNegTestSet, true);

				//accumulate the accuracy, fmeasure,... for the TRAINING_COVERAGE strategy at 5%, 10%, 20%,...
				for (int i=0; i<6; i++) {	//6: the number of steps: 5%, 10%, 20%,..., 50%
					accuracyPercentageFortifyStepStat[INDEX][i].
					addNumber(multiStepFortificationResult[INDEX].fortificationAccuracy[i+1]);
					completenessPercentageFortifyStepStat[INDEX][i].
					addNumber(multiStepFortificationResult[INDEX].fortificationCompleteness[i+1]);
					correctnessPercentageFortifyStepStat[INDEX][i].
					addNumber(multiStepFortificationResult[INDEX].fortificationCorrectness[i+1]);
					fmeasurePercentageFortifyStepStat[INDEX][i].
					addNumber(multiStepFortificationResult[INDEX].fortificationFmeasure[i+1]);
				}


				//------------------------------------------------
				/// 4. Fortification - SIMILARITY NEG-POS
				//------------------------------------------------
				outputWriter("---------------------------------------------");
				outputWriter("Fortification - SIMILARITY NEG-POS");
				outputWriter("---------------------------------------------");

				INDEX = FortificationUtils.SIMILARITY_POS_NEG_INDEX;

				SortedSet<CELOEPartial.PartialDefinition> similarityNegPosCpdef = new TreeSet<>(new FortificationUtils.AdditionalValueComparator(3));
				similarityNegPosCpdef.addAll(counterPartialDefinitions);


				//counter partial definition is sorted by training coverage by default ==> don't need to sort the cpdef set
				multiStepFortificationResult[INDEX] = FortificationUtils.fortifyAccuracyMultiSteps(
						rs, concept, similarityNegPosCpdef, curFoldPosTestSet, curFoldNegTestSet, true);

				//accumulate the accuracy, fmeasure,... for the TRAINING_COVERAGE strategy at 5%, 10%, 20%,...
				for (int i=0; i<6; i++) {	//6: the number of steps: 5%, 10%, 20%,..., 50%
					accuracyPercentageFortifyStepStat[INDEX][i].
					addNumber(multiStepFortificationResult[INDEX].fortificationAccuracy[i+1]);
					completenessPercentageFortifyStepStat[INDEX][i].
					addNumber(multiStepFortificationResult[INDEX].fortificationCompleteness[i+1]);
					correctnessPercentageFortifyStepStat[INDEX][i].
					addNumber(multiStepFortificationResult[INDEX].fortificationCorrectness[i+1]);
					fmeasurePercentageFortifyStepStat[INDEX][i].
					addNumber(multiStepFortificationResult[INDEX].fortificationFmeasure[i+1]);
				}


				//------------------------------------------------
				/// 5. Fortification - JACCARD OVERLAP
				//------------------------------------------------
				outputWriter("---------------------------------------------");
				outputWriter("Fortification - JACCARD OVERLAP");
				outputWriter("---------------------------------------------");

				INDEX = FortificationUtils.NEW_JACCARD_OVERLAP_INDEX;

				SortedSet<CELOEPartial.PartialDefinition> jaccardOverlapCpdef = new TreeSet<>(new FortificationUtils.AdditionalValueComparator(4));
				jaccardOverlapCpdef.addAll(counterPartialDefinitions);


				//counter partial definition is sorted by training coverage by default ==> don't need to sort the cpdef set
				multiStepFortificationResult[INDEX] = FortificationUtils.fortifyAccuracyMultiSteps(
						rs, concept, jaccardOverlapCpdef, curFoldPosTestSet, curFoldNegTestSet, true);

				//accumulate the accuracy, fmeasure,... for the TRAINING_COVERAGE strategy at 5%, 10%, 20%,...
				for (int i=0; i<6; i++) {	//6: the number of steps: 5%, 10%, 20%,..., 50%
					accuracyPercentageFortifyStepStat[INDEX][i].
					addNumber(multiStepFortificationResult[INDEX].fortificationAccuracy[i+1]);
					completenessPercentageFortifyStepStat[INDEX][i].
					addNumber(multiStepFortificationResult[INDEX].fortificationCompleteness[i+1]);
					correctnessPercentageFortifyStepStat[INDEX][i].
					addNumber(multiStepFortificationResult[INDEX].fortificationCorrectness[i+1]);
					fmeasurePercentageFortifyStepStat[INDEX][i].
					addNumber(multiStepFortificationResult[INDEX].fortificationFmeasure[i+1]);
				}


				//------------------------------------------------
				/// 6. Fortification - JACCARD DISTANCE
				//------------------------------------------------
				outputWriter("---------------------------------------------");
				outputWriter("Fortification - JACCARD DISTANCE");
				outputWriter("---------------------------------------------");

				INDEX = FortificationUtils.NEW_JACCARD_DISTANCE_INDEX;

				SortedSet<CELOEPartial.PartialDefinition> jaccardDistanceCpdef = new TreeSet<>(new FortificationUtils.AdditionalValueComparator(4, false));
				jaccardDistanceCpdef.addAll(counterPartialDefinitions);


				//counter partial definition is sorted by training coverage by default ==> don't need to sort the cpdef set
				multiStepFortificationResult[INDEX] = FortificationUtils.fortifyAccuracyMultiSteps(
						rs, concept, jaccardDistanceCpdef, curFoldPosTestSet, curFoldNegTestSet, true);

				//accumulate the accuracy, fmeasure,... for the TRAINING_COVERAGE strategy at 5%, 10%, 20%,...
				for (int i=0; i<6; i++) {	//6: the number of steps: 5%, 10%, 20%,..., 50%
					accuracyPercentageFortifyStepStat[INDEX][i].
					addNumber(multiStepFortificationResult[INDEX].fortificationAccuracy[i+1]);
					completenessPercentageFortifyStepStat[INDEX][i].
					addNumber(multiStepFortificationResult[INDEX].fortificationCompleteness[i+1]);
					correctnessPercentageFortifyStepStat[INDEX][i].
					addNumber(multiStepFortificationResult[INDEX].fortificationCorrectness[i+1]);
					fmeasurePercentageFortifyStepStat[INDEX][i].
					addNumber(multiStepFortificationResult[INDEX].fortificationFmeasure[i+1]);
				}



				//------------------------------------------------
				/// 7. Fortification - COMBINATION
				//------------------------------------------------
				outputWriter("---------------------------------------------");
				outputWriter("Fortification - COMBINATION SCORES");
				outputWriter("---------------------------------------------");

				INDEX = FortificationUtils.CONBINATION_INDEX;

				SortedSet<CELOEPartial.PartialDefinition> combinationScoreCpdef = new TreeSet<>(new FortificationUtils.AdditionalValueComparator(5));
				combinationScoreCpdef.addAll(counterPartialDefinitions);


				//counter partial definition is sorted by training coverage by default ==> don't need to sort the cpdef set
				multiStepFortificationResult[INDEX] = FortificationUtils.fortifyAccuracyMultiSteps(
						rs, concept, combinationScoreCpdef, curFoldPosTestSet, curFoldNegTestSet, true);

				//accumulate the accuracy, fmeasure,... for the TRAINING_COVERAGE strategy at 5%, 10%, 20%,...
				for (int i=0; i<6; i++) {	//6: the number of steps: 5%, 10%, 20%,..., 50%
					accuracyPercentageFortifyStepStat[INDEX][i].
					addNumber(multiStepFortificationResult[INDEX].fortificationAccuracy[i+1]);
					completenessPercentageFortifyStepStat[INDEX][i].
					addNumber(multiStepFortificationResult[INDEX].fortificationCompleteness[i+1]);
					correctnessPercentageFortifyStepStat[INDEX][i].
					addNumber(multiStepFortificationResult[INDEX].fortificationCorrectness[i+1]);
					fmeasurePercentageFortifyStepStat[INDEX][i].
					addNumber(multiStepFortificationResult[INDEX].fortificationFmeasure[i+1]);
				}


				//------------------------------------------------
				/// 7. Fortification - COMBINATION
				//------------------------------------------------
				outputWriter("---------------------------------------------");
				outputWriter("Fortification - RANDOM");
				outputWriter("---------------------------------------------");

				INDEX = FortificationUtils.RANDOM_INDEX;

				SortedSet<CELOEPartial.PartialDefinition> randomCpdef = new TreeSet<>(new FortificationUtils.AdditionalValueComparator(6));
				randomCpdef.addAll(counterPartialDefinitions);


				//counter partial definition is sorted by training coverage by default ==> don't need to sort the cpdef set
				multiStepFortificationResult[INDEX] = FortificationUtils.fortifyAccuracyMultiSteps(
						rs, concept, randomCpdef, curFoldPosTestSet, curFoldNegTestSet, true);

				//accumulate the accuracy, fmeasure,... for the TRAINING_COVERAGE strategy at 5%, 10%, 20%,...
				for (int i=0; i<6; i++) {	//6: the number of steps: 5%, 10%, 20%,..., 50%
					accuracyPercentageFortifyStepStat[INDEX][i].
					addNumber(multiStepFortificationResult[INDEX].fortificationAccuracy[i+1]);
					completenessPercentageFortifyStepStat[INDEX][i].
					addNumber(multiStepFortificationResult[INDEX].fortificationCompleteness[i+1]);
					correctnessPercentageFortifyStepStat[INDEX][i].
					addNumber(multiStepFortificationResult[INDEX].fortificationCorrectness[i+1]);
					fmeasurePercentageFortifyStepStat[INDEX][i].
					addNumber(multiStepFortificationResult[INDEX].fortificationFmeasure[i+1]);
				}


				//------------------------------
				// Fortification - LABEL DATA
				// 	LABLED TEST DATA
				//------------------------------
				//if there exists covered negative examples ==> check if there are any counter partial definitions
				//can be used to remove covered negative examples

				int fixedNeg = 0;
				int fixedPos = 0;
				int noOfSelectedCpdef = 0;
				int totalSelectedCpdefLength = 0;
				double avgTrainingCoverageSelectedCpdef = 0;

				/**
				 * selected cpdef which are selected based on the test labled data
				 * given a set of wrong classified neg., select a set of cpdef to remove the wrong classified neg examples
				 * the cpdef are sorted based on the training neg. example coverage
				 */
				TreeSet<CELOEPartial.PartialDefinition> selectedCounterPartialDefinitions = new TreeSet<>(new FortificationUtils.CoverageComparator());

				if (cnTest.size() > 0) {

					TreeSet<OWLIndividual> tempCoveredNeg = new TreeSet<>();
					tempCoveredNeg.addAll(cnTest);

					TreeSet<OWLIndividual> tempUncoveredPos = new TreeSet<>();
					tempUncoveredPos.addAll(upTest);

					//check each counter partial definitions
					for (CELOEPartial.PartialDefinition negCpdef : counterPartialDefinitions) {

						OWLClassExpression cpdef = ((OWLObjectComplementOf)negCpdef.getDescription()).getOperand();

						//set of neg examples covered by the counter partial definition
						Set<OWLIndividual> desCoveredNeg = new HashSet<>(rs.hasType(cpdef, curFoldNegTestSet));

						//if the current counter partial definition can help to remove some neg examples
						//int oldNoOfCoveredNeg=tempCoveredNeg.size();
						if (tempCoveredNeg.removeAll(desCoveredNeg)) {

							//assign cn on test set to additionalValue
							selectedCounterPartialDefinitions.add(negCpdef);

							//check if it may remove some positive examples or not
							Set<OWLIndividual> desCoveredPos = new HashSet<>(rs.hasType(cpdef, curFoldPosTestSet));
							tempUncoveredPos.addAll(desCoveredPos);

							//count the total number of counter partial definition selected and their total length
							noOfSelectedCpdef++;
							totalSelectedCpdefLength += OWLClassExpressionUtils.getLength(cpdef);
							avgTrainingCoverageSelectedCpdef += negCpdef.getCoverage();
						}

						if (tempCoveredNeg.size() == 0)
							break;
					}

					fixedNeg = cnTest.size() - tempCoveredNeg.size();
					fixedPos = tempUncoveredPos.size() - upTest.size();
					avgTrainingCoverageSelectedCpdef /= noOfSelectedCpdef;


				}

				noOfLabelFortifySelectedCpdefStat.addNumber(noOfSelectedCpdef);
				labelFortifyCpdefTrainingCoverageStat.addNumber(avgTrainingCoverageSelectedCpdef);


				//-----------------------------
				// Labeled fortification
				// 	  stat calculation
				//-----------------------------

				//def length
				double labelFortifyDefinitionLength = OWLClassExpressionUtils.getLength(concept) + totalSelectedCpdefLength + noOfSelectedCpdef;	//-1 from the selected cpdef and +1 for NOT
				labelFortifiedDefinitionLengthStat.addNumber(labelFortifyDefinitionLength);

				double avgLabelFortifyDefinitionLength = 0;

				if (noOfSelectedCpdef > 0) {
					avgLabelFortifyDefinitionLength = (double)totalSelectedCpdefLength/noOfSelectedCpdef;
					avgLabelCpdefLengthStat.addNumber(totalSelectedCpdefLength/(double)noOfSelectedCpdef);
				}

				//accuracy = test accuracy + fortification adjustment
				double labelFortifyAccuracy = 100 * ((double)(correctTestExamples + fixedNeg - fixedPos)/
						(curFoldPosTestSet.size() + curFoldNegTestSet.size()));
				accuracyLabelFortifyStat.addNumber(labelFortifyAccuracy);

				//completeness
				double labelFortifyCompleteness = 100 * ((double)(correctTestPosClassified - fixedPos)/curFoldPosTestSet.size());
				completenessLabelFortifyStat.addNumber(labelFortifyCompleteness);

				//correctness
				double labelFortifyCorrectness = 100 * ((double)(correctTestNegClassified + fixedNeg)/curFoldNegTestSet.size());
				correctnessLabelFortifyStat.addNumber(labelFortifyCorrectness);

				//precision, recall, f-measure
				double labelFortifyPrecision = 0.0;	//percent of correct pos examples in total pos examples classified (= correct pos classified + neg as pos)
				if (((correctTestPosClassified - fixedPos) + (cnTest.size() - fixedNeg)) > 0)
					labelFortifyPrecision = (double)(correctTestPosClassified - fixedPos)/
							(correctTestPosClassified - fixedPos + cnTest.size() - fixedNeg);	//tmp3: neg as pos <=> false pos

				double labelFortifyRecall = (double)(correctTestPosClassified - fixedPos) / curFoldPosTestSet.size();

				double labelFortifyFmeasure = 100 * Heuristics.getFScore(labelFortifyRecall, labelFortifyPrecision);
				fmeasureLabelFortifyStat.addNumber(labelFortifyFmeasure);



				outputWriter("---------------------------------------------");
				outputWriter("LABEL fortify counter partial definitions: ");
				outputWriter("---------------------------------------------");
				count = 1;
				//output the selected counter partial definition information
				if (noOfSelectedCpdef > 0) {
					for (CELOEPartial.PartialDefinition cpdef : selectedCounterPartialDefinitions) {

						outputWriter(count++ + cpdef.getId() + ". " + cpdef.getId() + " " + FortificationUtils.getCpdefString(cpdef, baseURI, prefixes)
								+ ", cp=" + rs.hasType(cpdef.getDescription(), curFoldPosTestSet)
								+ ", cn=" + rs.hasType(cpdef.getDescription(), curFoldNegTestSet));
					}

				}



				outputWriter("----------------------");

				int[] noOfCpdefMultiStep = FortificationUtils.getMultiStepFortificationStep(counterPartialDefinitions.size());

				for (int i=0; i<6; i++) {
					noOfCpdefUsedMultiStepFortStat[i].addNumber(noOfCpdefMultiStep[i]);

					//minimal value of 50% of the cpdef used in the fortification
					//NOTE: no of cpdef descreases after added into other sets for fortification
					//	Cause has not been investigated
					minOfHalfCpdef = (minOfHalfCpdef > noOfCpdefMultiStep[i])? noOfCpdefMultiStep[i] : minOfHalfCpdef;

					//minimal number of counter partial definitions till the current run
					//the above problem happens for this case as well
					minCpdef = (minCpdef > multiStepFortificationResult[i].fortificationAccuracyStepByStep.length)?
							multiStepFortificationResult[i].fortificationAccuracyStepByStep.length : minCpdef;
				}


				//create data structure to hold the fortification result
				if (currFold == 0) {	//have  not initiallised
					accuracyHalfFullStep = new double[noOfStrategies][minOfHalfCpdef];	//4 strategies
					fmeasureHalfFullStep = new double[noOfStrategies][minOfHalfCpdef];

					accuracyFullStepStat = new Stat[noOfStrategies][minCpdef];
					fmeasureFullStepStat = new Stat[noOfStrategies][minCpdef];
					correctnessFullStepStat = new Stat[noOfStrategies][minCpdef];
					completenessFullStepStat = new Stat[noOfStrategies][minCpdef];

					//run a loop to create a set of Stat objects
					for (int i=0; i < noOfStrategies; i++) {
						for (int j = 0; j < minCpdef; j++) {
							accuracyFullStepStat[i][j] = new Stat();
							fmeasureFullStepStat[i][j] = new Stat();
							correctnessFullStepStat[i][j] = new Stat();
							completenessFullStepStat[i][j] = new Stat();
						}
					}
				}


				//sum up the accuracy and fmeasure directly, do not use Stat for simplicity
				outputWriter("*** Calculate full step accuracy: minCpdef = " + minCpdef);

				outputWriter("\tcounter partial deifnition size=" +
						counterPartialDefinitions.size());

				//calculate accuracy, fmeasure  of the cpdef HALF FULL STEP
				for (int i=0; i<noOfStrategies; i++) {
					for (int j=0; j<minOfHalfCpdef; j++) {
						//calculate the accuracy and fmeasure of full step fortification
						accuracyHalfFullStep[i][j] += multiStepFortificationResult[i].fortificationAccuracyStepByStep[j];
						fmeasureHalfFullStep[i][j] += multiStepFortificationResult[i].fortificationFmeasureStepByStep[j];
					}
				}

				//calculate accuracy, fmeasure FULL STEP by STEP
				for (int i=0; i<noOfStrategies; i++) {
					for (int j=0; j<minCpdef; j++) {
						//calculate the accuracy and fmeasure of full step fortification
						accuracyFullStepStat[i][j].addNumber(multiStepFortificationResult[i].fortificationAccuracyStepByStep[j]);
						fmeasureFullStepStat[i][j].addNumber(multiStepFortificationResult[i].fortificationFmeasureStepByStep[j]);
						correctnessFullStepStat[i][j].addNumber(multiStepFortificationResult[i].fortificationCorrectnessStepByStep[j]);
						completenessFullStepStat[i][j].addNumber(multiStepFortificationResult[i].fortificationCompletenessStepByStep[j]);
					}
				}



				//--------------------------------
				//output fold stat. information
				// of the CURRENT fold
				//--------------------------------
				outputWriter("Fold " + currFold + "/" + folds + ":");
				//outputWriter("  concept: " + concept);

				outputWriter("  training: " + trainingCorrectPosClassified + "/" + trainingPosSize +
						" positive and " + trainingCorrectNegClassified + "/" + trainingNegSize + " negative examples");
				outputWriter("  testing: " + correctTestPosClassified + "/" + testingPosSize + " correct positives, "
						+ correctTestNegClassified + "/" + testingNegSize + " correct negatives");


				//general learning statistics
				outputWriter("  runtime: " + df.format(algorithmDuration/(double)1000000000) + "s");
				outputWriter("  learning time: " + df.format(learningMili/(double)1000) + "s");
//				outputWriter("  total number of descriptions: " + la.getTotalNumberOfDescriptionsGenerated()); // TODO not available in CV class
				outputWriter("  total number pdef: " + noOfPdef + " (used by parcelex: " + noOfUsedPdef + ")");
				outputWriter("  total number of cpdef: " + noOfCpdef + " (used by parcelex: " + noOfUsedCpdef + ")");


				//pdef + cpdef
				outputWriter("  def. length: " + df.format(OWLClassExpressionUtils.getLength(concept)));
				outputWriter("  def. length label fortify: " + df.format(labelFortifyDefinitionLength));
				outputWriter("  avg. def. length label fortify: " + df.format(avgLabelFortifyDefinitionLength));
				outputWriter("  total cpdef. length: " + df.format(totalCPDefLength));
				outputWriter("  avg. pdef length: " + df.format(avgPdefLength));
				outputWriter("  avg. cpdef. length: " + df.format(avgCPDefLength));
				outputWriter("  avg. cpdef training coverage: " + statOutput(df, avgCpdefCoverageTrainingStat, "%"));

				outputWriter("  no of cpdef used in the multi-step fortification: " + Arrays.toString(noOfCpdefMultiStep));

				//f-measure
				outputWriter("  f-measure training set: " + df.format(currFmeasureTraining));
				outputWriter("  f-measure test set: " + df.format(currFmeasureTest));
				outputWriter("  f-measure on test set label fortification: " + df.format(labelFortifyFmeasure));
				outputWriter("  f-measure on test set blind fortification: " + df.format(blindFmeasure));


				//accuracy
				outputWriter("  accuracy test: " + df.format(currAccuracy) +
						"% (corr:"+ df.format(testingCorrectness) +
						"%, comp:" + df.format(testingCompleteness) + "%) --- " +
						df.format(trainingAccuracy) + "% (corr:"+ trainingCorrectness +
						"%, comp:" + df.format(trainingCompleteness) + "%) on training set");

				outputWriter("  accuracy label fortification: " + df.format(labelFortifyAccuracy) +
						"%, correctness: " + df.format(labelFortifyCorrectness) +
						"%, completeness: " + df.format(labelFortifyCompleteness) + "%");

				outputWriter("  accuracy blind fortification: " + df.format(blindFortificationAccuracy) +
						"%, correctness: " + df.format(blindFortificationCorrectness) +
						"%, completeness: " + df.format(blindFortificationCompleteness) + "%");


				//output the fortified accuracy at 5%, 10%, ..., 50%
				for (int i=0; i<noOfStrategies; i++) {
					outputWriter("  multi-step fortified accuracy by " + FortificationUtils.strategyNames[i] + ": "
							+ FortificationUtils.arrayToString(df, multiStepFortificationResult[i].fortificationAccuracy)
							+ " -- correctness: " + FortificationUtils.arrayToString(df, multiStepFortificationResult[i].fortificationCorrectness)
							+ " -- completeness: " + FortificationUtils.arrayToString(df, multiStepFortificationResult[i].fortificationCompleteness)
						);
				}	//output fortified accuracy at 5%, 10%, ..., 50%


				outputWriter("  number of cpdef use in the label fortification: " + noOfSelectedCpdef);
				outputWriter("  avg. training coverage of the selected cpdef. in the label fortification: " + df.format(avgTrainingCoverageSelectedCpdef));

				//=============================================================
				outputWriter("---------------------------------------------");
				outputWriter("FULL-STEP fortification with FORTIFICATION STRATEGIES: ");
				outputWriter("Metrics: Accuracy, Correctness, Completeness");
				outputWriter("");
				/*
				String strategiesNamesStr = "Strategies: ";
				for (int i=0; i<noOfStrategies; i++) 	//6 strategies
					strategiesNamesStr += FortificationUtils.strategyNames[i] + ", ";
				outputWriter(strategiesNamesStr + ":");
				*/

				for (int i=0; i<noOfStrategies; i++) {	//6 strategies
					outputWriter(FortificationUtils.strategyNames[i] + ":");
					for (int j=0; j<minCpdef; j++) {
						outputWriter((j+1) + ": (" + multiStepFortificationResult[i].fortificationAccuracyStepByStep[j] + ")\t"
								+ df.format(accuracyFullStepStat[i][j].getMean()) + "\t"
								+ df.format(accuracyFullStepStat[i][j].getStandardDeviation()) + "\t"
								+ "(" + multiStepFortificationResult[i].fortificationCorrectnessStepByStep[j] + ")\t"
								+ df.format(correctnessFullStepStat[i][j].getMean()) + "\t"
								+ df.format(correctnessFullStepStat[i][j].getStandardDeviation()) + "\t"
								+ "(" + multiStepFortificationResult[i].fortificationCompletenessStepByStep[j] + ")\t"
								+ df.format(completenessFullStepStat[i][j].getMean()) + "\t"
								+ df.format(completenessFullStepStat[i][j].getStandardDeviation())
						);
					}
					outputWriter("\n");
				}



				//=============================================================
				//----------------------------------------------
				//output fold accumulative stat. information
				//----------------------------------------------
				outputWriter("----------");
				outputWriter("Aggregate data from fold 0 to fold " + currFold + "/" + folds);
				outputWriter("  runtime: " + statOutput(df, runtime, "s"));
				outputWriter("  learning time parcelex: " + statOutput(df, learningTime, "s"));
				outputWriter("  no of descriptions: " + statOutput(df, totalNumberOfDescriptions, ""));

				outputWriter("  no of total pdef: " + statOutput(df, noOfPdefStat, ""));
				outputWriter("  no of used pdef: " + statOutput(df, noOfUsedPdefStat, ""));
				outputWriter("  avg pdef length: " + statOutput(df, avgUsedPartialDefinitionLengthStat, ""));
				outputWriter("  no of cpdef: " + statOutput(df, noOfCpdefStat, ""));
				outputWriter("  avg cpdef length: " + statOutput(df, avgCpdefLengthStat, ""));
				outputWriter("  avg. def. length: " + statOutput(df, length, ""));

				outputWriter("  avg. no of ");
				outputWriter("  avg. label fortified def. length: " + statOutput(df, labelFortifiedDefinitionLengthStat, ""));
				outputWriter("  avg. length of the cpdefs used in the label fortification: " + statOutput(df, avgLabelCpdefLengthStat, ""));

				outputWriter("  F-Measure on training set: " + statOutput(df, fMeasureTraining, "%"));
				outputWriter("  F-Measure on test set: " + statOutput(df, fMeasure, "%"));
				outputWriter("  F-Measure on test set fortified: " + statOutput(df, fmeasureLabelFortifyStat, "%"));
				outputWriter("  predictive accuracy on training set: " + statOutput(df, accuracyTraining, "%") +
						" -- correctness: " + statOutput(df, trainingCorrectnessStat, "%") +
						"-- completeness: " + statOutput(df, trainingCompletenessStat, "%"));
				outputWriter("  predictive accuracy on test set: " + statOutput(df, accuracy, "%") +
						" -- correctness: " + statOutput(df, testingCorrectnessStat, "%") +
						"-- completeness: " + statOutput(df, testingCompletenessStat, "%"));

				outputWriter("  label fortification accuracy on test set: " + statOutput(df, accuracyLabelFortifyStat, "%") +
						" -- fortified correctness: " + statOutput(df, correctnessLabelFortifyStat, "%") +
						"-- fortified completeness: " + statOutput(df, completenessLabelFortifyStat, "%"));

				outputWriter("  blind fortification accuracy on test set: " + statOutput(df, accuracyBlindFortifyStat, "%") +
						" -- fortified correctness: " + statOutput(df, correctnessBlindFortifyStat, "%") +
						"-- fortified completeness: " + statOutput(df, completenessBlindFortifyStat, "%"));





				//fortification by PERCENTAGE
				for (int i=0; i< noOfStrategies; i++) {
					outputWriter("  multi-step fortified accuracy by " + FortificationUtils.strategyNames[i] + ":");

					outputWriter("\t 5%: " + statOutput(df, accuracyPercentageFortifyStepStat[i][0], "%")
							+ " -- correctness: " + statOutput(df, correctnessPercentageFortifyStepStat[i][0], "%")
							+ " -- completeness: " + statOutput(df, completenessPercentageFortifyStepStat[i][0], "%")
							);
					outputWriter("\t 10%: " + statOutput(df, accuracyPercentageFortifyStepStat[i][1], "%")
							+ " -- correctness: " + statOutput(df, correctnessPercentageFortifyStepStat[i][1], "%")
							+ " -- completeness: " + statOutput(df, completenessPercentageFortifyStepStat[i][1], "%")
							);
					outputWriter("\t 20%: " + statOutput(df, accuracyPercentageFortifyStepStat[i][2], "%")
							+ " -- correctness: " + statOutput(df, correctnessPercentageFortifyStepStat[i][2], "%")
							+ " -- completeness: " + statOutput(df, completenessPercentageFortifyStepStat[i][2], "%")
							);
					outputWriter("\t 30%: " + statOutput(df, accuracyPercentageFortifyStepStat[i][3], "%")
							+ " -- correctness: " + statOutput(df, correctnessPercentageFortifyStepStat[i][3], "%")
							+ " -- completeness: " + statOutput(df, completenessPercentageFortifyStepStat[i][3], "%")
							);
					outputWriter("\t 40%: " + statOutput(df, accuracyPercentageFortifyStepStat[i][4], "%")
							+ " -- correctness: " + statOutput(df, correctnessPercentageFortifyStepStat[i][4], "%")
							+ " -- completeness: " + statOutput(df, completenessPercentageFortifyStepStat[i][4], "%")
							);
					outputWriter("\t 50%: " + statOutput(df, accuracyPercentageFortifyStepStat[i][5], "%")
							+ " -- correctness: " + statOutput(df, correctnessPercentageFortifyStepStat[i][5], "%")
							+ " -- completeness: " + statOutput(df, completenessPercentageFortifyStepStat[i][5], "%")
							);

				}

				outputWriter("  avg. no of counter partial definition: " + statOutput(df, noOfCpdefStat, ""));
				outputWriter("  avg. no of counter partial definition used in label fortification: " + statOutput(df, noOfLabelFortifySelectedCpdefStat,""));

				outputWriter("  no of cpdef used in multi-step fortification:");
				outputWriter("\t5%: " + statOutput(df, noOfCpdefUsedMultiStepFortStat[0], ""));
				outputWriter("\t10%: " + statOutput(df, noOfCpdefUsedMultiStepFortStat[1], ""));
				outputWriter("\t20%: " + statOutput(df, noOfCpdefUsedMultiStepFortStat[2], ""));
				outputWriter("\t30%: " + statOutput(df, noOfCpdefUsedMultiStepFortStat[3], ""));
				outputWriter("\t40%: " + statOutput(df, noOfCpdefUsedMultiStepFortStat[4], ""));
				outputWriter("\t50%: " + statOutput(df, noOfCpdefUsedMultiStepFortStat[5], ""));


				outputWriter("----------------------");


				//sleep after each run (fer MBean collecting information purpose)
				try {
					Thread.sleep(5000);
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}

			}	//for k folds


			//---------------------------------
			//end of k-fold cross validation
			//output result of the k-fold
			//---------------------------------

			//final cumulative statistical data of a run
			outputWriter("");
			outputWriter("Finished the " + (kk+1) + "/" + noOfRuns + " of " + folds + "-folds cross-validation.");
			outputWriter("  runtime: " + statOutput(df, runtime, "s"));
			outputWriter("  learning time parcelex: " + statOutput(df, learningTime, "s"));
			outputWriter("  no of descriptions: " + statOutput(df, totalNumberOfDescriptions, ""));

			outputWriter("  no of total pdef: " + statOutput(df, noOfPdefStat, ""));
			outputWriter("  no of used pdef: " + statOutput(df, noOfUsedPdefStat, ""));
			outputWriter("  avg pdef length: " + statOutput(df, avgUsedPartialDefinitionLengthStat, ""));
			outputWriter("  no of cpdef: " + statOutput(df, noOfCpdefStat, ""));
			outputWriter("  avg cpdef length: " + statOutput(df, avgCpdefLengthStat, ""));
			outputWriter("  avg. def. length: " + statOutput(df, length, ""));

			outputWriter("  avg. label fortified def. length: " + statOutput(df, labelFortifiedDefinitionLengthStat, ""));
			outputWriter("  avg. cpdef used in the label fortification: " + statOutput(df, noOfLabelFortifySelectedCpdefStat, ""));
			outputWriter("  F-Measure on training set: " + statOutput(df, fMeasureTraining, "%"));
			outputWriter("  F-Measure on test set: " + statOutput(df, fMeasure, "%"));
			outputWriter("  F-Measure on test set fortified: " + statOutput(df, fmeasureLabelFortifyStat, "%"));
			outputWriter("  predictive accuracy on training set: " + statOutput(df, accuracyTraining, "%") +
					"\n\t-- correctness: " + statOutput(df, trainingCorrectnessStat, "%") +
					"\n\t-- completeness: " + statOutput(df, trainingCompletenessStat, "%"));
			outputWriter("  predictive accuracy on test set: " + statOutput(df, accuracy, "%") +
					"\n\t-- correctness: " + statOutput(df, testingCorrectnessStat, "%") +
					"\n\t-- completeness: " + statOutput(df, testingCompletenessStat, "%"));

			outputWriter("  fortified accuracy on test set: " + statOutput(df, accuracyLabelFortifyStat, "%") +
					"\n\t-- fortified correctness: " + statOutput(df, correctnessLabelFortifyStat, "%") +
					"\n\t-- fortified completeness: " + statOutput(df, completenessLabelFortifyStat, "%"));

			outputWriter("  blind fortified accuracy on test set: " + statOutput(df, accuracyBlindFortifyStat, "%") +
					"\n\t-- fortified correctness: " + statOutput(df, correctnessBlindFortifyStat, "%") +
					"\n\t-- fortified completeness: " + statOutput(df, completenessBlindFortifyStat, "%"));


			//------------------------------------------------------------
			//compute cut-off point and the metrics at the cut-off point
			//------------------------------------------------------------
			int cutOffPoint = 0;
			//double cutOffAvg[][], cutOffDev[][];
			//cutOffAvg = new double[3][noOfStrategies];	//0: accuracy, 1: correctness, 2: completeness
			//cutOffDev = new double[3][noOfStrategies];

			//cut-off point is the max number of the labelled fortification definitions
			if (noOfLabelFortifySelectedCpdefStat.getMean() > 0)	//this is for a weird side-affect of the floating point such that the getMax return a very small number >0 even if the acutuall value is zero
				cutOffPoint = (int)Math.round(Math.ceil(noOfLabelFortifySelectedCpdefStat.getMax()));

			//outputWriter("\n  CUT-OFF point computation: " + noOfLabelFortifySelectedCpdefStat.getMax());
			//outputWriter("\n  test: " + Math.round(Math.ceil(noOfLabelFortifySelectedCpdefStat.getMean())));
			outputWriter("\n  CUT-OFF point computation: " + cutOffPoint);
			if (cutOffPoint == 0) {
				outputWriter("\tNo fortifying definition is used, the accuracy is unchanged");
				outputWriter("\t\taccuracy: " + df.format(accuracy.getMean()) + ", " + df.format(accuracy.getStandardDeviation()) +
						"; correctness: " +	df.format(testingCorrectnessStat.getMean()) + ", " + df.format(testingCorrectnessStat.getStandardDeviation()) +
						"; completeness: " + df.format(testingCompletenessStat.getMean()) + ", " + df.format(testingCompletenessStat.getStandardDeviation()));
			}
			else {
				cutOffPoint--;
				for (int i=0; i < noOfStrategies; i++) {
					outputWriter("\t" + FortificationUtils.strategyNames[i] + ":");
					outputWriter("\t  accuracy: " + df.format(accuracyFullStepStat[i][cutOffPoint].getMean()) +
							", " + df.format(accuracyFullStepStat[i][cutOffPoint].getStandardDeviation()) +
							"; correctness: " + df.format(correctnessFullStepStat[i][cutOffPoint].getMean()) +
							", " + df.format(correctnessFullStepStat[i][cutOffPoint].getStandardDeviation()) +
							"; completeness: " + df.format(completenessFullStepStat[i][cutOffPoint].getMean()) +
							", " + df.format(completenessFullStepStat[i][cutOffPoint].getStandardDeviation())
							);
					/*
					cutOffAvg[0][i] = accuracyFullStepStat[i][cutOffPoint].getMean();
					cutOffAvg[1][i] = correctnessFullStepStat[i][cutOffPoint].getMean();
					cutOffAvg[2][i] = completenessFullStepStat[i][cutOffPoint].getMean();

					cutOffDev[0][i] = accuracyFullStepStat[i][cutOffPoint].getStandardDeviation();
					cutOffDev[1][i] = correctnessFullStepStat[i][cutOffPoint].getStandardDeviation();
					cutOffDev[2][i] = completenessFullStepStat[i][cutOffPoint].getStandardDeviation();
					*/
				}
			}
			//end of cut-off point processing

			outputWriter("");

			for (int i=0; i< noOfStrategies; i++) {

				outputWriter("  multi-step fortified accuracy by " + FortificationUtils.strategyNames[i] + ":");

				outputWriter("\t 5%: " + statOutput(df, accuracyPercentageFortifyStepStat[i][0], "%")
						+ " -- correctness: " + statOutput(df, correctnessPercentageFortifyStepStat[i][0], "%")
						+ " -- completeness: " + statOutput(df, completenessPercentageFortifyStepStat[i][0], "%")
						);
				outputWriter("\t 10%: " + statOutput(df, accuracyPercentageFortifyStepStat[i][1], "%")
						+ " -- correctness: " + statOutput(df, correctnessPercentageFortifyStepStat[i][1], "%")
						+ " -- completeness: " + statOutput(df, completenessPercentageFortifyStepStat[i][1], "%")
						);
				outputWriter("\t 20%: " + statOutput(df, accuracyPercentageFortifyStepStat[i][2], "%")
						+ " -- correctness: " + statOutput(df, correctnessPercentageFortifyStepStat[i][2], "%")
						+ " -- completeness: " + statOutput(df, completenessPercentageFortifyStepStat[i][2], "%")
						);
				outputWriter("\t 30%: " + statOutput(df, accuracyPercentageFortifyStepStat[i][3], "%")
						+ " -- correctness: " + statOutput(df, correctnessPercentageFortifyStepStat[i][3], "%")
						+ " -- completeness: " + statOutput(df, completenessPercentageFortifyStepStat[i][3], "%")
						);
				outputWriter("\t 40%: " + statOutput(df, accuracyPercentageFortifyStepStat[i][4], "%")
						+ " -- correctness: " + statOutput(df, correctnessPercentageFortifyStepStat[i][4], "%")
						+ " -- completeness: " + statOutput(df, completenessPercentageFortifyStepStat[i][4], "%")
						);
				outputWriter("\t 50%: " + statOutput(df, accuracyPercentageFortifyStepStat[i][5], "%")
						+ " -- correctness: " + statOutput(df, correctnessPercentageFortifyStepStat[i][5], "%")
						+ " -- completeness: " + statOutput(df, completenessPercentageFortifyStepStat[i][5], "%")
						);

			}

			//outputWriter("  total no of counter partial definition: " + statOutput(df, noOfCpdefStat, ""));
			//outputWriter("  avg. no of counter partial definition used in label fortification: " + statOutput(df, noOfLabelFortifySelectedCpdefStat,""));

			outputWriter("  no of cpdef used in multi-step fortification:");
			outputWriter("\t5%: " + statOutput(df, noOfCpdefUsedMultiStepFortStat[0], ""));
			outputWriter("\t10%: " + statOutput(df, noOfCpdefUsedMultiStepFortStat[1], ""));
			outputWriter("\t20%: " + statOutput(df, noOfCpdefUsedMultiStepFortStat[2], ""));
			outputWriter("\t30%: " + statOutput(df, noOfCpdefUsedMultiStepFortStat[3], ""));
			outputWriter("\t40%: " + statOutput(df, noOfCpdefUsedMultiStepFortStat[4], ""));
			outputWriter("\t50%: " + statOutput(df, noOfCpdefUsedMultiStepFortStat[5], ""));



			//-----------------------------------------
			//this is for copying to word document
			//-----------------------------------------
			outputWriter("======= RESULT SUMMARY PERCENTAGE (5%, 10%, 20%, 30%, 40%, 50%) =======");

			//fmeasure
			outputWriter("\n***f-measure test/blind");
			outputWriter(df.format(fMeasure.getMean()) + "  " + df.format(fMeasure.getStandardDeviation())
				+ "\n" + df.format(fmeasureBlindFortifyStat.getMean()) + "  " + df.format(fmeasureBlindFortifyStat.getStandardDeviation())
				);

			//for each strategy: strategy name, f-measure (5-50%)
			for (int i=0; i<noOfStrategies; i++) {
				outputWriter("fmeasure - " + FortificationUtils.strategyNames[i] + " by percentage (5%, 10%, 20%, 30%, 40%, 50%)");
				for (int j=0; j<6; j++)
					outputWriter(df.format(fmeasurePercentageFortifyStepStat[i][j].getMean())
							+ "\n" + df.format(fmeasurePercentageFortifyStepStat[i][j].getStandardDeviation()));
			}


			//accuracy
			outputWriter("\n***accuracy test/blind");
			outputWriter(df.format(accuracy.getMean()) + "  " + df.format(accuracy.getStandardDeviation())
				+ "\n" + df.format(accuracyBlindFortifyStat.getMean()) + "  " + df.format(accuracyBlindFortifyStat.getStandardDeviation())
				);

			//for each strategy: strategy name, accuracy (5-50%)
			for (int i=0; i<noOfStrategies; i++) {
				outputWriter("accuracy - " + FortificationUtils.strategyNames[i] + " by percentage (5%, 10%, 20%, 30%, 40%, 50%)");
				for (int j=0; j<6; j++)
					outputWriter(df.format(accuracyPercentageFortifyStepStat[i][j].getMean())
							+ "\n" + df.format(accuracyPercentageFortifyStepStat[i][j].getStandardDeviation()));
			}

			//correctness
			outputWriter("\n***correctness test/blind");
			outputWriter(df.format(testingCorrectnessStat.getMean()) + "  " + df.format(testingCorrectnessStat.getStandardDeviation())
				+ "\n" + df.format(correctnessBlindFortifyStat.getMean()) + "  " + df.format(correctnessBlindFortifyStat.getStandardDeviation())
				);

			//for each strategy: strategy name, accuracy (5-50%)
			for (int i=0; i<noOfStrategies; i++) {
				outputWriter("correctness - " + FortificationUtils.strategyNames[i] + " by percentage (5%, 10%, 20%, 30%, 40%, 50%)");
				for (int j=0; j<6; j++)
					outputWriter(df.format(correctnessPercentageFortifyStepStat[i][j].getMean())
							+ "\n" + df.format(correctnessPercentageFortifyStepStat[i][j].getStandardDeviation()));
			}

			//completeness
			outputWriter("\n***completeness test/blind");
			outputWriter(df.format(testingCompletenessStat.getMean()) + "  " + df.format(testingCompletenessStat.getStandardDeviation())
				+ "\n" + df.format(completenessBlindFortifyStat.getMean()) + "  " + df.format(completenessBlindFortifyStat.getStandardDeviation())
				);

			//for each strategy: strategy name, accuracy (5-50%)
			for (int i=0; i<noOfStrategies; i++) {
				outputWriter("completeness - " + FortificationUtils.strategyNames[i] + " by percentage (5%, 10%, 20%, 30%, 40%, 50%)");
				for (int j=0; j<6; j++)
					outputWriter(df.format(completenessPercentageFortifyStepStat[i][j].getMean())
							+ "\n" + df.format(completenessPercentageFortifyStepStat[i][j].getStandardDeviation()));
			}

			outputWriter("======= RESULT SUMMARY FULL STEP =======");
			outputWriter("======= Fmeasure full steps =======");
			for (int i=0; i<noOfStrategies; i++) {	//6 strategies
				outputWriter(FortificationUtils.strategyNames[i] + "(" + minCpdef + "/" + fmeasureFullStepStat[0].length + ")");	//fmeasureFullStepStat[0].length == minCpdef???
				for (int j=0; j<minCpdef; j++) {
					outputWriter(df.format(fmeasureFullStepStat[i][j].getMean()) + "\t"
							+ df.format(fmeasureFullStepStat[i][j].getStandardDeviation()));
				}
				outputWriter("\n");
			}


			outputWriter("======= Accuracy full steps =======");
			for (int i=0; i<noOfStrategies; i++) {	//6 strategies
				outputWriter(FortificationUtils.strategyNames[i] + "(" + minCpdef + "/" + accuracyFullStepStat[0].length + ")");
				for (int j=0; j<minCpdef; j++) {
					outputWriter(df.format(accuracyFullStepStat[i][j].getMean()) + "\t"
							+ df.format(accuracyFullStepStat[i][j].getStandardDeviation()));
				}
				outputWriter("\n");
			}



			outputWriter("======= Correctness full steps =======");
			for (int i=0; i<noOfStrategies; i++) {	//6 strategies
				outputWriter(FortificationUtils.strategyNames[i] + "(" + minCpdef + "/" + correctnessFullStepStat[0].length +")");
				for (int j=0; j<minCpdef; j++) {
					outputWriter(df.format(correctnessFullStepStat[i][j].getMean()) + "\t"
							+ df.format(correctnessFullStepStat[i][j].getStandardDeviation()));
				}
				outputWriter("\n");
			}


			outputWriter("======= Completeness full steps =======");
			for (int i=0; i<noOfStrategies; i++) {	//4 strategies
				outputWriter(FortificationUtils.strategyNames[i] + "(" + minCpdef + "/" + completenessFullStepStat[0].length +")");
				for (int j=0; j<minCpdef; j++) {
					outputWriter(df.format(completenessFullStepStat[i][j].getMean()) + "\t"
							+ df.format(completenessFullStepStat[i][j].getStandardDeviation()));
				}
				outputWriter("\n");
			}


			//---------------------------------------------------
			// this is used to copy into EXCEL to draw charts
			//---------------------------------------------------
			outputWriter("======= FULL STEP SUMMARY ALL strategies & dimentions=======");
			//accuracy(6), correctness(6), completeness(6), fmeasure(6)

			String strategies = "Strategies: ";

			String noCpdefFortificationAcc = "";
			String noCpdefFortificationCor = "";
			String noCpdefFortificationComp = "";
			String noCpdefFortificationFm = "";


			String allCpdefFortificationAcc = "";
			String allCpdefFortificationCor = "";
			String allCpdefFortificationComp = "";
			String allCpdefFortificationFm = "";

			for (int i=0; i<noOfStrategies; i++) {	//6 strategies
				strategies += FortificationUtils.strategyNames[i] + ", ";

				//test data (no fortification
				noCpdefFortificationAcc += df.format(accuracy.getMean()) + "\t" + df.format(accuracy.getStandardDeviation()) + "\t";
				noCpdefFortificationCor += df.format(testingCorrectnessStat.getMean()) + "\t" + df.format(testingCorrectnessStat.getStandardDeviation()) + "\t";
				noCpdefFortificationComp += df.format(testingCompletenessStat.getMean()) + "\t" + df.format(testingCompletenessStat.getStandardDeviation()) + "\t";
				noCpdefFortificationFm += df.format(fMeasure.getMean()) + "\t" + df.format(fMeasure.getStandardDeviation()) + "\t";

				//all cpdef fortification
				allCpdefFortificationAcc += df.format(accuracyBlindFortifyStat.getMean()) + "\t" + df.format(accuracyBlindFortifyStat.getStandardDeviation()) + "\t";
				allCpdefFortificationCor += df.format(correctnessBlindFortifyStat.getMean()) + "\t" + df.format(correctnessBlindFortifyStat.getStandardDeviation()) + "\t";
				allCpdefFortificationComp += df.format(completenessBlindFortifyStat.getMean()) + "\t" + df.format(completenessBlindFortifyStat.getStandardDeviation()) + "\t";
				allCpdefFortificationFm += df.format(fmeasureBlindFortifyStat.getMean()) + "\t" + df.format(fmeasureBlindFortifyStat.getStandardDeviation()) + "\t";
			}

			outputWriter(strategies);

			outputWriter(noCpdefFortificationAcc + "\t" + noCpdefFortificationCor + "\t" + noCpdefFortificationComp + "\t" + noCpdefFortificationFm);

			for (int j=0; j<minCpdef; j++) {	//all cpdefs
				String allResult = "";	//contains all data of one cpdef

				String bestAcc = "\t", bestCor = "\t", bestComp = "\t", bestFm = "";

				if ((j == Math.round(Math.ceil(noOfLabelFortifySelectedCpdefStat.getMin()))) ||
						(j == Math.round(Math.ceil(noOfLabelFortifySelectedCpdefStat.getMax()))) ||
						(j == Math.round(Math.ceil(noOfLabelFortifySelectedCpdefStat.getMean())))) {

					 bestAcc = df.format(accuracyLabelFortifyStat.getMean()) + "\t";
					 bestCor = df.format(correctnessLabelFortifyStat.getMean()) + "\t";
					 bestComp = df.format(completenessLabelFortifyStat.getMean()) + "\t";
					 bestFm = df.format(fmeasureLabelFortifyStat.getMean()) + "\t";
				}

				//accuracy
				for (int i=0; i<noOfStrategies; i++) {	//6 strategies
					allResult += df.format(accuracyFullStepStat[i][j].getMean()) + "\t"
						+ df.format(accuracyFullStepStat[i][j].getStandardDeviation()) + "\t";
				}

				allResult += bestAcc;

				//correctness
				for (int i=0; i<noOfStrategies; i++) {	//6 strategies
					allResult += df.format(correctnessFullStepStat[i][j].getMean()) + "\t"
						+ df.format(correctnessFullStepStat[i][j].getStandardDeviation()) + "\t";
				}

				allResult += bestCor;

				//completeness
				for (int i=0; i<noOfStrategies; i++) {	//6 strategies
					allResult += df.format(completenessFullStepStat[i][j].getMean()) + "\t"
						+ df.format(completenessFullStepStat[i][j].getStandardDeviation()) + "\t";
				}

				allResult += bestComp;

				//f-measure
				for (int i=0; i<noOfStrategies; i++) {	//6 strategies
					allResult += df.format(fmeasureFullStepStat[i][j].getMean()) + "\t"
						+ df.format(fmeasureFullStepStat[i][j].getStandardDeviation()) + "\t";
				}

				allResult += bestFm;

				outputWriter(allResult);

			}

			//blind fortification data
			outputWriter(allCpdefFortificationAcc + "\t" + allCpdefFortificationCor + "\t" + allCpdefFortificationComp + "\t" + allCpdefFortificationFm);

			//---------------------------------
			//end of DATA for GRAPH generation
			//---------------------------------



			//aggrerate stat information for multi-run
			//TODO: need to be revised
			if (noOfRuns > 1) {
				//runtime
				runtimeAvg.addNumber(runtime.getMean());
				runtimeMax.addNumber(runtime.getMax());
				runtimeMin.addNumber(runtime.getMin());
				runtimeDev.addNumber(runtime.getStandardDeviation());

				//learning time
				learningTimeAvg.addNumber(learningTime.getMean());
				learningTimeDev.addNumber(learningTime.getStandardDeviation());
				learningTimeMax.addNumber(learningTime.getMax());
				learningTimeMin.addNumber(learningTime.getMin());

				//number of partial definitions
				noOfPartialDefAvg.addNumber(noOfPdefStat.getMean());
				noOfPartialDefMax.addNumber(noOfPdefStat.getMax());
				noOfPartialDefMin.addNumber(noOfPdefStat.getMin());
				noOfPartialDefDev.addNumber(noOfPdefStat.getStandardDeviation());

				//avg partial definition length
				avgPartialDefLenAvg.addNumber(avgUsedPartialDefinitionLengthStat.getMean());
				avgPartialDefLenMax.addNumber(avgUsedPartialDefinitionLengthStat.getMax());
				avgPartialDefLenMin.addNumber(avgUsedPartialDefinitionLengthStat.getMin());
				avgPartialDefLenDev.addNumber(avgUsedPartialDefinitionLengthStat.getStandardDeviation());

				avgFortifiedPartialDefLenAvg.addNumber(labelFortifiedDefinitionLengthStat.getMean());
				avgFortifiedPartialDefLenMax.addNumber(labelFortifiedDefinitionLengthStat.getMax());
				avgFortifiedPartialDefLenMin.addNumber(labelFortifiedDefinitionLengthStat.getMin());
				avgFortifiedPartialDefLenDev.addNumber(labelFortifiedDefinitionLengthStat.getStandardDeviation());


				defLenAvg.addNumber(length.getMean());
				defLenMax.addNumber(length.getMax());
				defLenMin.addNumber(length.getMin());
				defLenDev.addNumber(length.getStandardDeviation());

				//counter partial definitions
				noOfCounterPartialDefinitionsAvg.addNumber(noOfCpdefStat.getMean());
				noOfCounterPartialDefinitionsDev.addNumber(noOfCpdefStat.getStandardDeviation());
				noOfCounterPartialDefinitionsMax.addNumber(noOfCpdefStat.getMax());
				noOfCounterPartialDefinitionsMin.addNumber(noOfCpdefStat.getMin());

				noOfCounterPartialDefinitionsUsedAvg.addNumber(noOfCpdefUsedStat.getMean());
				noOfCounterPartialDefinitionsUsedDev.addNumber(noOfCpdefUsedStat.getStandardDeviation());
				noOfCounterPartialDefinitionsUsedMax.addNumber(noOfCpdefUsedStat.getMax());
				noOfCounterPartialDefinitionsUsedMin.addNumber(noOfCpdefUsedStat.getMin());

				//training accuracy
				trainingAccAvg.addNumber(accuracyTraining.getMean());
				trainingAccDev.addNumber(accuracyTraining.getStandardDeviation());
				trainingAccMax.addNumber(accuracyTraining.getMax());
				trainingAccMin.addNumber(accuracyTraining.getMin());

				trainingCorAvg.addNumber(trainingCorrectnessStat.getMean());
				trainingCorDev.addNumber(trainingCorrectnessStat.getStandardDeviation());
				trainingCorMax.addNumber(trainingCorrectnessStat.getMax());
				trainingCorMin.addNumber(trainingCorrectnessStat.getMin());

				trainingComAvg.addNumber(trainingCompletenessStat.getMean());
				trainingComDev.addNumber(trainingCompletenessStat.getStandardDeviation());
				trainingComMax.addNumber(trainingCompletenessStat.getMax());
				trainingComMin.addNumber(trainingCompletenessStat.getMin());

				testingAccAvg.addNumber(accuracy.getMean());
				testingAccMax.addNumber(accuracy.getMax());
				testingAccMin.addNumber(accuracy.getMin());
				testingAccDev.addNumber(accuracy.getStandardDeviation());

				//fortify accuracy
				fortifyAccAvg.addNumber(accuracyLabelFortifyStat.getMean());
				fortifyAccMax.addNumber(accuracyLabelFortifyStat.getMax());
				fortifyAccMin.addNumber(accuracyLabelFortifyStat.getMin());
				fortifyAccDev.addNumber(accuracyLabelFortifyStat.getStandardDeviation());


				testingCorAvg.addNumber(testingCorrectnessStat.getMean());
				testingCorDev.addNumber(testingCorrectnessStat.getStandardDeviation());
				testingCorMax.addNumber(testingCorrectnessStat.getMax());
				testingCorMin.addNumber(testingCorrectnessStat.getMin());

				//fortify correctness
				fortifyCorAvg.addNumber(correctnessLabelFortifyStat.getMean());
				fortifyCorMax.addNumber(correctnessLabelFortifyStat.getMax());
				fortifyCorMin.addNumber(correctnessLabelFortifyStat.getMin());
				fortifyCorDev.addNumber(correctnessLabelFortifyStat.getStandardDeviation());

				testingComAvg.addNumber(testingCompletenessStat.getMean());
				testingComDev.addNumber(testingCompletenessStat.getStandardDeviation());
				testingComMax.addNumber(testingCompletenessStat.getMax());
				testingComMin.addNumber(testingCompletenessStat.getMin());

				//fortify completeness (level 1 fixing does not change the completeness
				fortifyComAvg.addNumber(completenessLabelFortifyStat.getMean());
				fortifyComMax.addNumber(completenessLabelFortifyStat.getMax());
				fortifyComMin.addNumber(completenessLabelFortifyStat.getMin());
				fortifyComDev.addNumber(completenessLabelFortifyStat.getStandardDeviation());


				testingFMeasureAvg.addNumber(fMeasure.getMean());
				testingFMeasureDev.addNumber(fMeasure.getStandardDeviation());
				testingFMeasureMax.addNumber(fMeasure.getMax());
				testingFMeasureMin.addNumber(fMeasure.getMin());

				trainingFMeasureAvg.addNumber(fMeasureTraining.getMean());
				trainingFMeasureDev.addNumber(fMeasureTraining.getStandardDeviation());
				trainingFMeasureMax.addNumber(fMeasureTraining.getMax());
				trainingFMeasureMin.addNumber(fMeasureTraining.getMin());

				fortifyFmeasureAvg.addNumber(fmeasureLabelFortifyStat.getMean());
				fortifyFmeasureMax.addNumber(fmeasureLabelFortifyStat.getMax());
				fortifyFmeasureMin.addNumber(fmeasureLabelFortifyStat.getMin());
				fortifyFmeasureDev.addNumber(fmeasureLabelFortifyStat.getStandardDeviation());

				noOfDescriptionsAgv.addNumber(totalNumberOfDescriptions.getMean());
				noOfDescriptionsMax.addNumber(totalNumberOfDescriptions.getMax());
				noOfDescriptionsMin.addNumber(totalNumberOfDescriptions.getMin());
				noOfDescriptionsDev.addNumber(totalNumberOfDescriptions.getStandardDeviation());
			}

		}	//for kk folds

		if (noOfRuns > 1) {

			//TODO: this needs to be revised using a loop instead if multi-run is used
			outputWriter("");
			outputWriter("Finished " + noOfRuns + " time(s) of the " + folds + "-folds cross-validations");

			outputWriter("runtime: " +
					"\n\t avg.: " + statOutput(df, runtimeAvg, "s") +
					"\n\t dev.: " + statOutput(df, runtimeDev, "s") +
					"\n\t max.: " + statOutput(df, runtimeMax, "s") +
					"\n\t min.: " + statOutput(df, runtimeMin, "s"));

			outputWriter("learning time: " +
					"\n\t avg.: " + statOutput(df, learningTimeAvg, "s") +
					"\n\t dev.: " + statOutput(df, learningTimeDev, "s") +
					"\n\t max.: " + statOutput(df, learningTimeMax, "s") +
					"\n\t min.: " + statOutput(df, learningTimeMin, "s"));

			outputWriter("no of descriptions: " +
					"\n\t avg.: " + statOutput(df, noOfDescriptionsAgv, "") +
					"\n\t dev.: " + statOutput(df, noOfDescriptionsDev, "") +
					"\n\t max.: " + statOutput(df, noOfDescriptionsMax, "") +
					"\n\t min.: " + statOutput(df, noOfDescriptionsMin, ""));

			outputWriter("number of partial definitions: " +
					"\n\t avg.: " + statOutput(df, noOfPartialDefAvg, "") +
					"\n\t dev.: " + statOutput(df, noOfPartialDefDev, "") +
					"\n\t max.: " + statOutput(df, noOfPartialDefMax, "") +
					"\n\t min.: " + statOutput(df, noOfPartialDefMin, ""));

			outputWriter("avg. partial definition length: " +
					"\n\t avg.: " + statOutput(df, avgPartialDefLenAvg, "") +
					"\n\t dev.: " + statOutput(df, avgPartialDefLenDev, "") +
					"\n\t max.: " + statOutput(df, avgPartialDefLenMax, "") +
					"\n\t min.: " + statOutput(df, avgPartialDefLenMin, ""));

			outputWriter("definition length: " +
					"\n\t avg.: " + statOutput(df, defLenAvg, "") +
					"\n\t dev.: " + statOutput(df, defLenDev, "") +
					"\n\t max.: " + statOutput(df, defLenMax, "") +
					"\n\t min.: " + statOutput(df, defLenMin, ""));

			outputWriter("number of counter partial definitions: " +
					"\n\t avg.: " + statOutput(df, noOfCounterPartialDefinitionsAvg, "") +
					"\n\t dev.: " + statOutput(df, noOfCounterPartialDefinitionsDev, "") +
					"\n\t max.: " + statOutput(df, noOfCounterPartialDefinitionsMax, "") +
					"\n\t min.: " + statOutput(df, noOfCounterPartialDefinitionsMin, ""));

			outputWriter("number of counter partial definitions used: " +
					"\n\t avg.: " + statOutput(df, noOfCounterPartialDefinitionsUsedAvg, "") +
					"\n\t dev.: " + statOutput(df, noOfCounterPartialDefinitionsUsedDev, "") +
					"\n\t max.: " + statOutput(df, noOfCounterPartialDefinitionsUsedMax, "") +
					"\n\t min.: " + statOutput(df, noOfCounterPartialDefinitionsUsedMin, ""));

			outputWriter("accuracy on training set:" +
					"\n\t avg.: " + statOutput(df, trainingAccAvg, "%") +
					"\n\t dev.: " + statOutput(df, trainingAccDev, "%") +
					"\n\t max.: " + statOutput(df, trainingAccMax, "%") +
					"\n\t min.: " + statOutput(df, trainingAccMin, "%"));

			outputWriter("correctness on training set: " +
					"\n\t avg.: " + statOutput(df, trainingCorAvg, "%") +
					"\n\t dev.: " + statOutput(df, trainingCorDev, "%") +
					"\n\t max.: " + statOutput(df, trainingCorMax, "%") +
					"\n\t min.: " + statOutput(df, trainingCorMin, "%"));

			outputWriter("completeness on training set: " +
					"\n\t avg.: " + statOutput(df, trainingComAvg, "%") +
					"\n\t dev.: " + statOutput(df, trainingCorDev, "%") +
					"\n\t max.: " + statOutput(df, trainingComMax, "%") +
					"\n\t min.: " + statOutput(df, trainingComMin, "%"));

			outputWriter("FMeasure on training set: " +
					"\n\t avg.: " + statOutput(df, trainingFMeasureAvg, "%") +
					"\n\t dev.: " + statOutput(df, trainingFMeasureDev, "%") +
					"\n\t max.: " + statOutput(df, trainingFMeasureMax, "%") +
					"\n\t min.: " + statOutput(df, trainingFMeasureMin, "%"));

			outputWriter("accuracy on testing set: " +
					"\n\t avg.: " + statOutput(df, testingAccAvg, "%") +
					"\n\t dev.: " + statOutput(df, testingAccDev, "%") +
					"\n\t max.: " + statOutput(df, testingAccMax, "%") +
					"\n\t min.: " + statOutput(df, testingAccMin, "%"));

			outputWriter("correctness on testing set: " +
					"\n\t avg.: " + statOutput(df, testingCorAvg, "%") +
					"\n\t dev.: " + statOutput(df, testingCorDev, "%") +
					"\n\t max.: " + statOutput(df, testingCorMax, "%") +
					"\n\t min.: " + statOutput(df, testingCorMin, "%"));

			outputWriter("completeness on testing set: " +
					"\n\t avg.: " + statOutput(df, testingComAvg, "%") +
					"\n\t dev.: " + statOutput(df, testingComDev, "%") +
					"\n\t max.: " + statOutput(df, testingComMax, "%") +
					"\n\t min.: " + statOutput(df, testingComMin, "%"));

			outputWriter("FMeasure on testing set: " +
					"\n\t avg.: " + statOutput(df, testingFMeasureAvg, "%") +
					"\n\t dev.: " + statOutput(df, testingFMeasureDev, "%") +
					"\n\t max.: " + statOutput(df, testingFMeasureMax, "%") +
					"\n\t min.: " + statOutput(df, testingFMeasureMin, "%"));


		}

		//if (la instanceof ParCELExAbstract)
		//	outputWriter("terminated by: partial def.: " + terminatedBypartialDefinition + "; counter partial def.: " + terminatedByCounterPartialDefinitions);


		//reset the set of positive and negative examples for the learning problem for further experiment if any
		lp.setPositiveExamples(posExamples);
		lp.setNegativeExamples(negExamples);


	}	//constructor

}


