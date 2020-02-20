package org.dllearner.cli.parcel;

import java.text.DecimalFormat;
import java.util.*;

import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.dllearner.algorithms.parcel.celoe.CELOEPartial;
import org.dllearner.cli.parcel.fortification.FortificationUtils;
import org.dllearner.cli.parcel.fortification.JaccardSimilarity;
import org.dllearner.algorithms.parcel.ParCELAbstract;
import org.dllearner.algorithms.parcel.ParCELExtraNode;
import org.dllearner.algorithms.parcel.ParCELPosNegLP;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.algorithms.parcel.ParCELPosNegLP;
import org.dllearner.cli.CrossValidation;
import org.dllearner.cli.parcel.fortification.FortificationUtils;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.kb.OWLFile;
import org.dllearner.learningproblems.Heuristics;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;
import org.dllearner.utilities.statistics.Stat;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

/**
 * Performs cross validation for the given problem. Supports k-fold cross-validation and
 * leave-one-out cross-validation.
 *
 * @author Jens Lehmann
 *
 */
public class ParCELFortifiedCrossValidation3PhasesFair extends CrossValidation {

	//pdef
	protected Stat noOfPartialDefinitionStat;
	protected Stat noOfReducedPartialDefinitionStat;
	protected Stat avgPdefLengthStat;

	//cpdef
	protected Stat noOfCounterPartialDefinitions;
	protected Stat noOfLabelFortifyDefinitions;
	protected Stat avgLabelCpdefLengthStat;
	protected Stat avgLabelFortifyCpdefCoverage;

	//fortify variables
	protected Stat lableFortifyDefinitionLengthStat;
	protected Stat labelFortifyDefinitionLengthStat;

	//label fortification
	protected Stat accuracyLabelFortifyStat;
	protected Stat correctnessLabelFortifyStat;
	protected Stat completenessLabelFortifyStat;
	protected Stat fmeasureLabelFortifyStat;

	protected Stat avgFortifyCoverageTraingStat;
	protected Stat avgFortifyCoverageTestStat;

	protected Stat fortifiedRuntime;

	//blind fortification
	protected Stat accuracyBlindFortifyStat;
	protected Stat correctnessBlindFortifyStat;
	protected Stat completenessBlindFortifyStat;
	protected Stat fmeasureBlindFortifyStat;

	protected Stat totalCPDefLengthStat;
	protected Stat avgCPDefLengthStat;


	//hold the fortified accuracy, fmeasure,... of 5%, 10%, 20%, ..., 50% of cpdef used
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


	//----------------------------------
	//FAIR comparison
	protected Stat fairLearningTimeStat;
	protected Stat fairAccuracyStat;
	protected Stat fairFmeasureStat;
	protected Stat fairCorrectnessStat;
	protected Stat fairCompletenessStat;
	//----------------------------------

	Logger logger = Logger.getLogger(this.getClass());

	public ParCELFortifiedCrossValidation3PhasesFair() {

	}

	public ParCELFortifiedCrossValidation3PhasesFair(AbstractCELA la, ParCELPosNegLP lp, AbstractReasonerComponent rs, int folds,
													 boolean leaveOneOut) {

		this(la, lp, rs, folds, leaveOneOut, 1, 0, false);

	}


	public ParCELFortifiedCrossValidation3PhasesFair(AbstractCELA la, ParCELPosNegLP lp, AbstractReasonerComponent rs, int folds,
			boolean leaveOneOut, int noOfRuns) {

		this(la, lp, rs, folds, leaveOneOut, noOfRuns, 0, false);
	}

	//ParCELFortifiedCrossValidation2PhasesFair
	public ParCELFortifiedCrossValidation3PhasesFair(AbstractCELA la, ParCELPosNegLP lp, AbstractReasonerComponent rs, int folds,
			boolean leaveOneOut, int noOfRuns, int fortificationTimeout, boolean fairComparison) {

		DecimalFormat df = new DecimalFormat();

		String baseURI = rs.getBaseURI();
		Map<String, String> prefixes = rs.getPrefixes();

		// the training and test sets used later on
		List<Set<OWLIndividual>> trainingSetsPos = new LinkedList<>();
		List<Set<OWLIndividual>> trainingSetsNeg = new LinkedList<>();
		List<Set<OWLIndividual>> testSetsPos = new LinkedList<>();
		List<Set<OWLIndividual>> testSetsNeg = new LinkedList<>();
		List<Set<OWLIndividual>> fortificationSetsPos = new LinkedList<>();
		List<Set<OWLIndividual>> fortificationSetsNeg = new LinkedList<>();

		// get examples and shuffle them too
		Set<OWLIndividual> posExamples = ((ParCELPosNegLP) lp).getPositiveExamples();
		List<OWLIndividual> posExamplesList = new LinkedList<>(posExamples);
		//Collections.shuffle(posExamplesList, new Random(1));
		Set<OWLIndividual> negExamples = ((ParCELPosNegLP) lp).getNegativeExamples();
		List<OWLIndividual> negExamplesList = new LinkedList<>(negExamples);
		//Collections.shuffle(negExamplesList, new Random(2));

		// sanity check whether nr. of folds makes sense for this benchmark
		if (!leaveOneOut && (posExamples.size() < folds && negExamples.size() < folds)) {
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
		int[] splitsPos = calculateSplits(posExamples.size(), folds);
		int[] splitsNeg = calculateSplits(negExamples.size(), folds);

		//for orthogonality check
		long orthAllCheckCount[] = new long[5];
		orthAllCheckCount[0] = orthAllCheckCount[1] = orthAllCheckCount[2] = orthAllCheckCount[3] = orthAllCheckCount[4] = 0;

		long orthSelectedCheckCount[] = new long[5];
		orthSelectedCheckCount[0] = orthSelectedCheckCount[1] = orthSelectedCheckCount[2] = orthSelectedCheckCount[3] = orthSelectedCheckCount[4] = 0;


		// calculating training and test sets
		for (int i = 0; i < folds; i++) {
			Set<OWLIndividual> testPos = getTestingSet(posExamplesList, splitsPos, i);
			Set<OWLIndividual> testNeg = getTestingSet(negExamplesList, splitsNeg, i);
			testSetsPos.add(i, testPos);
			testSetsNeg.add(i, testNeg);

			//fortification training set
			Set<OWLIndividual> fortPos = getTestingSet(posExamplesList, splitsPos, (i+1) % folds);
			Set<OWLIndividual> fortNeg = getTestingSet(negExamplesList, splitsNeg, (i+1) % folds);
			fortificationSetsPos.add(i, fortPos);
			fortificationSetsNeg.add(i, fortNeg);

			Set<OWLIndividual> trainingPos = getTrainingSet(posExamples, testPos);
			Set<OWLIndividual> trainingNeg = getTrainingSet(negExamples, testNeg);

			trainingPos.removeAll(fortPos);
			trainingNeg.removeAll(fortNeg);

			trainingSetsPos.add(i, trainingPos);
			trainingSetsNeg.add(i, trainingNeg);
		}

		// ---------------------------------
		// k-fold cross validation
		// ---------------------------------

		Stat runtimeAvg = new Stat();
		Stat runtimeMax = new Stat();
		Stat runtimeMin = new Stat();
		Stat runtimeDev = new Stat();

		Stat defLenAvg = new Stat();
		Stat defLenDev = new Stat();
		Stat defLenMax = new Stat();
		Stat defLenMin = new Stat();

		Stat trainingAccAvg = new Stat();
		Stat trainingAccDev = new Stat();
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

		Stat testingCorAvg = new Stat();
		Stat testingCorDev = new Stat();
		Stat testingCorMax = new Stat();
		Stat testingCorMin = new Stat();

		Stat testingComAvg = new Stat();
		Stat testingComDev = new Stat();
		Stat testingComMax = new Stat();
		Stat testingComMin = new Stat();

		Stat testingFMesureAvg = new Stat();
		Stat testingFMesureDev = new Stat();
		Stat testingFMesureMax = new Stat();
		Stat testingFMesureMin = new Stat();

		Stat trainingFMesureAvg = new Stat();
		Stat trainingFMesureDev = new Stat();
		Stat trainingFMesureMax = new Stat();
		Stat trainingFMesureMin = new Stat();

		Stat noOfDescriptionsAgv = new Stat();
		Stat noOfDescriptionsMax = new Stat();
		Stat noOfDescriptionsMin = new Stat();
		Stat noOfDescriptionsDev = new Stat();

		//fortify stat. variables
		Stat noOfCounterPartialDefinitionsAvg = new Stat();
		Stat noOfCounterPartialDefinitionsDev = new Stat();
		Stat noOfCounterPartialDefinitionsMax = new Stat();
		Stat noOfCounterPartialDefinitionsMin = new Stat();

		//this is for ParCELEx only
		Stat noOfCounterPartialDefinitionsUsedAvg = new Stat();
		Stat noOfCounterPartialDefinitionsUsedDev = new Stat();
		Stat noOfCounterPartialDefinitionsUsedMax = new Stat();
		Stat noOfCounterPartialDefinitionsUsedMin = new Stat();

		Stat avgCounterPartialDefinitionLengthAvg = new Stat();
		Stat avgCounterPartialDefinitionLengthDev = new Stat();
		Stat avgCounterPartialDefinitionLengthMax = new Stat();
		Stat avgCounterPartialDefinitionLengthMin = new Stat();


		Stat avgFirtifiedDefinitionLengthAvg = new Stat();
		Stat avgFirtifiedDefinitionLengthDev = new Stat();
		Stat avgFirtifiedDefinitionLengthMax = new Stat();
		Stat avgFirtifiedDefinitionLengthMin = new Stat();

		Stat accuracyFortifyAvg = new Stat();
		Stat accuracyFortifyDev = new Stat();
		Stat accuracyFortifyMax = new Stat();
		Stat accuracyFortifyMin = new Stat();

		Stat correctnessFortifyAvg = new Stat();
		Stat correctnessFortifyDev = new Stat();
		Stat correctnessFortifyMax = new Stat();
		Stat correctnessFortifyMin = new Stat();

		Stat completenessFortifyAvg = new Stat();
		Stat completenessFortifyDev = new Stat();
		Stat completenessFortifyMax = new Stat();
		Stat completenessFortifyMin = new Stat();

		Stat fmeasureFortifyAvg = new Stat();
		Stat fmeasureFortifyDev = new Stat();
		Stat fmeasureFortifyMax = new Stat();
		Stat fmeasureFortifyMin = new Stat();

		Stat avgFortifyCoverageTraingAvg = new Stat();
		Stat avgFortifyCoverageTraingDev = new Stat();
		Stat avgFortifyCoverageTraingMax = new Stat();
		Stat avgFortifyCoverageTraingMin = new Stat();

		Stat avgFortifyCoverageTestAvg = new Stat();
		Stat avgFortifyCoverageTestDev = new Stat();
		Stat avgFortifyCoverageTestMax = new Stat();
		Stat avgFortifyCoverageTestMin = new Stat();


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
		//----------------------------------------------------------------------


		for (int kk = 0; kk < noOfRuns; kk++) {

			//stat. variables for each fold ==> need to be re-created after each fold
			runtime = new Stat();
			length = new Stat();
			accuracyTraining = new Stat();
			trainingCorrectnessStat = new Stat();
			trainingCompletenessStat = new Stat();
			accuracy = new Stat();
			testingCorrectnessStat = new Stat();
			testingCompletenessStat = new Stat();
			fMeasure = new Stat();
			fMeasureTraining = new Stat();

			noOfPartialDefinitionStat = new Stat();
			noOfReducedPartialDefinitionStat = new Stat();
			avgPdefLengthStat = new Stat();

			noOfCounterPartialDefinitions = new Stat();
			noOfLabelFortifyDefinitions = new Stat();
			avgLabelCpdefLengthStat = new Stat();
			avgLabelFortifyCpdefCoverage = new Stat();

			totalNumberOfDescriptions = new Stat();

			//fortify variables
			lableFortifyDefinitionLengthStat = new Stat();
			labelFortifyDefinitionLengthStat = new Stat();

			accuracyLabelFortifyStat = new Stat();
			correctnessLabelFortifyStat = new Stat();
			completenessLabelFortifyStat = new Stat();
			fmeasureLabelFortifyStat = new Stat();

			avgFortifyCoverageTraingStat = new Stat();
			avgFortifyCoverageTestStat = new Stat();

			fortifiedRuntime = new Stat();

			//blind fortification
			accuracyBlindFortifyStat = new Stat();
			correctnessBlindFortifyStat = new Stat();
			completenessBlindFortifyStat = new Stat();
			fmeasureBlindFortifyStat = new Stat();

			totalCPDefLengthStat = new Stat();
			avgCPDefLengthStat = new Stat();


			//fair evaluation variables initialization
			fairAccuracyStat = new Stat();
			fairFmeasureStat = new Stat();
			fairCorrectnessStat = new Stat();
			fairCompletenessStat = new Stat();
			fairLearningTimeStat = new Stat();



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


			//number of cpdef corresponding to 5%, 10%, 20%, ..., 50% (for stat.)
			noOfCpdefUsedMultiStepFortStat = new Stat[6];
			for (int i=0; i<6; i++)
				noOfCpdefUsedMultiStepFortStat[i] = new Stat();


			int minOfHalfCpdef = Integer.MAX_VALUE;
			int minCpdef = Integer.MAX_VALUE;

			//run the algorithm - n folds
			for (int currFold = 0; currFold < folds; currFold++) {

				outputWriter("//---------------\n" + "// Fold " + currFold + "/" + folds + "\n//---------------");
				outputWriter("Training: " + trainingSetsPos.get(currFold).size() + "/" + trainingSetsNeg.get(currFold).size()
						+ ", test:" + testSetsPos.get(currFold).size() + "/" + testSetsNeg.get(currFold).size()
						+ ", fort: " + fortificationSetsPos.get(currFold).size() + "/" + fortificationSetsNeg.get(currFold).size()
					);

				//1. reserve the pos/neg examples and start the learner to get the counter partial definitions
				//2. store the counter partial definitions
				//3. reserve the pos/neg back to the original set and start the learner again to get the definition
				//4. do the test step and apply the fortification if necessary


				//-----------------------------------------------------
				//	1. Learn COUNTER PARTIAL DEFINITIONS
				//		Reverse the pos/neg and let the learner starts
				//-----------------------------------------------------

				//reverse the pos/neg examples
				lp.setNegativeExamples(trainingSetsPos.get(currFold));
				lp.setPositiveExamples(trainingSetsNeg.get(currFold));

				//init the learner
				try {
					lp.init();
					la.init();
				} catch (ComponentInitException e) {
					e.printStackTrace();
				}

				//Timeout values. There may be 2 timeout values: for cpdef step and pdef step
				// Note that ParCEL does not need noise for cpdef generation
				long orgTimeout = ((ParCELAbstract)la).getMaxExecutionTimeInSeconds();

				outputWriter("** Phase 1 - Learning counter partial definition");
				outputWriter("Timeout=" + (fortificationTimeout > 0? fortificationTimeout : orgTimeout));


				//adjust fortification timeout
				if (fortificationTimeout > 0)
					((ParCELAbstract)la).setMaxExecutionTimeInSeconds(fortificationTimeout);


				//start the learner
				long algorithmStartTimeCpdef = System.nanoTime();
				la.start();
				long algorithmDurationCpdef = System.nanoTime() - algorithmStartTimeCpdef;


				//get and store the counter partial definitions

				/**
				 * all counter partial definition sorted by the training neg. coverage (coverage of training neg. example)
				 */
				TreeSet<CELOEPartial.PartialDefinition> counterPartialDefinitions = new TreeSet<CELOEPartial.PartialDefinition>(new FortificationUtils.CoverageComparator());


				//counterPartialDefinitions.addAll(((ParCELAbstract)la).getPartialDefinitions());
				//get the cpdefs, conversion is needed
				for (ParCELExtraNode cpdef : ((ParCELAbstract)la).getPartialDefinitions()) {
					int trainingCn = cpdef.getCoveredPositiveExamples().size();	//since pos and neg are reversed, cn = cp'

					counterPartialDefinitions.add(new CELOEPartial.PartialDefinition(cpdef.getDescription(), trainingCn));
				}


				outputWriter("Finish learning, number of partial definitions: " + counterPartialDefinitions.size());

				noOfCounterPartialDefinitions.addNumber(counterPartialDefinitions.size());


				//-----------------------------------------------------
				//	2. Do the NORMAL learn: learn definition for pos
				//		Re-assign the pos/neg and restart the learner
				//-----------------------------------------------------

				//set the pos/neg examples
				lp.setPositiveExamples(trainingSetsPos.get(currFold));
				lp.setNegativeExamples(trainingSetsNeg.get(currFold));


				//init the learner
				try {
					lp.init();
					la.init();
				} catch (ComponentInitException e) {
					e.printStackTrace();
				}

				//set the noise + timeout to the original values
				((ParCELAbstract)la).setMaxExecutionTimeInSeconds(orgTimeout);


				outputWriter("** Phase 2 - Learning the main concept");
				outputWriter("Timeout=" + orgTimeout);


				//-----------------------------
				//start learning the 2nd phase
				//-----------------------------
				long algorithmStartTimePdef = System.nanoTime();
				la.start();
				long algorithmDurationPdef = System.nanoTime() - algorithmStartTimePdef;
				runtime.addNumber(algorithmDurationPdef / (double) 1000000000);

				fortifiedRuntime.addNumber((algorithmDurationCpdef + algorithmDurationPdef)/1000000000d);

				//----------------------------
				//FINISHED learning
				//----------------------------

				//get the learned concept
				OWLClassExpression concept = ((ParCELAbstract) la).getUnionCurrenlyBestDescription();

//				long noOfDescriptionGenerated = la.getTotalNumberOfDescriptionsGenerated(); // TODO not available in CV class
//				totalNumberOfDescriptions.addNumber(noOfDescriptionGenerated);

				noOfPartialDefinitionStat.addNumber(((ParCELAbstract)la).getNumberOfPartialDefinitions());
				noOfReducedPartialDefinitionStat.addNumber(((ParCELAbstract)la).getNoOfReducedPartialDefinition());
				avgPdefLengthStat.addNumber(OWLClassExpressionUtils.getLength(concept)/(double)((ParCELAbstract)la).getNoOfReducedPartialDefinition());

				//----------------------------------------------
				//check if another "FAIR" evaluation is needed
				//----------------------------------------------
				boolean fairEvaluationNeeded = false;
				OWLClassExpression conceptFair = null;

				if (fairComparison && algorithmDurationPdef/(double) 1000000000 >= ((ParCELAbstract)la).getMaxExecutionTimeInSeconds()) {
					fairEvaluationNeeded = true;


					long fairLearningTimeout = orgTimeout;
					if (fortificationTimeout == 0)
						fairLearningTimeout *= 2;
					else
						fairLearningTimeout += fortificationTimeout;


					outputWriter("** Phase 3 - Learning the main concept again with double timeout value (for fair comparison): "
							+ fairLearningTimeout + "s");

					//init the learner
					//set the pos/neg examples
					lp.setPositiveExamples(trainingSetsPos.get(currFold));
					lp.setNegativeExamples(trainingSetsNeg.get(currFold));
					try {
						lp.init();
						la.init();
					} catch (ComponentInitException e) {
						e.printStackTrace();
					}

					//set the fair learning timeout
					((ParCELAbstract)la).setMaxExecutionTimeInSeconds(fairLearningTimeout);


					//-----------------------------
					//start learning the 2nd phase
					//-----------------------------
					long algorithmStartTimeFair = System.nanoTime();
					la.start();
					long algorithmDurationFair = System.nanoTime() - algorithmStartTimeFair;
					fairLearningTimeStat.addNumber(algorithmDurationFair / (double) 1000000000);

					//reset the timeout value
					((ParCELAbstract)la).setMaxExecutionTimeInSeconds(orgTimeout);

					conceptFair = ((ParCELAbstract)la).getUnionCurrenlyBestDescription();
				}
				else {
					fairLearningTimeStat.addNumber(algorithmDurationPdef);
				}


				//----------------------------
				//TRAINING accuracy
				//----------------------------
				Set<OWLIndividual> curFoldPosTrainingSet = trainingSetsPos.get(currFold);
				Set<OWLIndividual> curFoldNegTrainingSet = trainingSetsNeg.get(currFold);

				int correctTrainingPosClassified = getCorrectPosClassified(rs, concept,	curFoldPosTrainingSet);
				int correctTrainingNegClassified = getCorrectNegClassified(rs, concept,	curFoldNegTrainingSet);
				int correctTrainingExamples = correctTrainingPosClassified	+ correctTrainingNegClassified;

				double trainingAccuracy = 100 * ((double) correctTrainingExamples /
						(curFoldPosTrainingSet.size() + curFoldNegTrainingSet.size()));
				double trainingCompleteness = 100*(double)correctTrainingPosClassified/curFoldPosTrainingSet.size();
				double trainingCorrectness = 100*(double)correctTrainingNegClassified/curFoldNegTrainingSet.size();

				accuracyTraining.addNumber(trainingAccuracy);
				trainingCompletenessStat.addNumber(trainingCompleteness);
				trainingCorrectnessStat.addNumber(trainingCorrectness);

				// calculate training F-Score
				int negAsPosTraining = curFoldNegTrainingSet.size() - correctTrainingNegClassified;	//neg - un = cn
				double precisionTraining = (correctTrainingPosClassified + negAsPosTraining) == 0 ? 0
						: correctTrainingPosClassified	/ (double) (correctTrainingPosClassified + negAsPosTraining);
				double recallTraining = correctTrainingPosClassified / (double) curFoldPosTrainingSet.size();
				double fMeasureTrainingFold = 100 * Heuristics.getFScore(recallTraining, precisionTraining);

				fMeasureTraining.addNumber(fMeasureTrainingFold);


				//---------------------
				//TEST accuracy
				//---------------------
				Set<OWLIndividual> curFoldPosTestSet = testSetsPos.get(currFold);
				Set<OWLIndividual> curFoldNegTestSet = testSetsNeg.get(currFold);


				//calculate testing coverage
				Set<OWLIndividual> cpTest = rs.hasType(concept, curFoldPosTestSet);			//cp
				Set<OWLIndividual> upTest = Sets.difference(curFoldPosTestSet, cpTest);		//up: false negative
				Set<OWLIndividual> cnTest = rs.hasType(concept, curFoldNegTestSet);			//cn

				// calculate test accuracies
				int correctTestPosClassified = cpTest.size(); 	//covered positive examples		//curFoldPosTestSet.size() - upTest.size();	//getCorrectPosClassified(rs, concept,	curFoldPosTestSet);
				int correctTestNegClassified = curFoldNegTestSet.size() - cnTest.size();		//getCorrectNegClassified(rs, concept,	curFoldNegTestSet);
				int correctTestExamples = correctTestPosClassified + correctTestNegClassified;

				double testingAccuracyCurrFold = 100 * ((double) correctTestExamples /
						(curFoldPosTestSet.size() +	curFoldNegTestSet.size()));
				double testingCompleteness = 100*(double)correctTestPosClassified/curFoldPosTestSet.size();
				double testingCorrectness = 100*(double)correctTestNegClassified/curFoldNegTestSet.size();

				accuracy.addNumber(testingAccuracyCurrFold);
				testingCompletenessStat.addNumber(testingCompleteness);
				testingCorrectnessStat.addNumber(testingCorrectness);


				// calculate test F-Score
				int negAsPos = cnTest.size();
				double testPrecision = correctTestPosClassified + negAsPos == 0 ? 0 : correctTestPosClassified
						/ (double) (correctTestPosClassified + negAsPos);
				double testRecall = correctTestPosClassified / (double) curFoldPosTestSet.size();

				double fMeasureTestingFold = 100 * Heuristics.getFScore(testRecall, testPrecision);
				fMeasure.addNumber(fMeasureTestingFold);

				length.addNumber(OWLClassExpressionUtils.getLength(concept));



				//==================================================
				//FORTIFICATION
				//==================================================
				double fairAccuracyCurrFold = testingAccuracyCurrFold;
				double fairCompleteness = testingCompleteness;
				double fairCorrectness = testingCorrectness;
				double fairFMeasureCurrFold = fMeasureTestingFold;

				if (fairEvaluationNeeded) {
					//calculate FAIR coverage
					Set<OWLIndividual> cpFair = rs.hasType(conceptFair, curFoldPosTestSet);			//cp
					//Set<Individual> upFair = Helper.difference(curFoldPosTestSet, cpTest);		//up
					Set<OWLIndividual> cnFair = rs.hasType(conceptFair, curFoldNegTestSet);			//cn

					// calculate FAIR accuracy
					int correctFairPosClassified = cpFair.size(); 	//covered positive examples		//curFoldPosTestSet.size() - upTest.size();	//getCorrectPosClassified(rs, concept,	curFoldPosTestSet);
					int correctFairNegClassified = curFoldNegTestSet.size() - cnFair.size();		//getCorrectNegClassified(rs, concept,	curFoldNegTestSet);
					int correctFairExamples = correctFairPosClassified + correctFairNegClassified;

					fairAccuracyCurrFold = 100 * ((double) correctFairExamples /
							(curFoldPosTestSet.size() +	curFoldNegTestSet.size()));
					fairCompleteness = 100*(double)correctFairPosClassified/curFoldPosTestSet.size();
					fairCorrectness = 100*(double)correctFairNegClassified/curFoldNegTestSet.size();

					// calculate FAIR F-measure
					int fairNegAsPos = cnTest.size();
					double fairPrecision = correctFairPosClassified + fairNegAsPos == 0 ? 0 : correctFairPosClassified
							/ (double) (correctFairPosClassified + fairNegAsPos);
					double fairRecall = correctFairPosClassified / (double) curFoldPosTestSet.size();

					fairFMeasureCurrFold = 100 * Heuristics.getFScore(fairRecall, fairPrecision);
				}

				fairAccuracyStat.addNumber(fairAccuracyCurrFold);
				fairCorrectnessStat.addNumber(fairCorrectness);
				fairCompletenessStat.addNumber(fairCompleteness);
				fairFmeasureStat.addNumber(fairFMeasureCurrFold);

				//---------------------------------------
				//FORTIFICATION
				//---------------------------------------
				FortificationUtils.FortificationResult[] multiStepFortificationResult = new FortificationUtils.FortificationResult[noOfStrategies];



				//---------------------------------
				// Fortification - ALL CPDEF
				// (BLIND Fortification)
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


				//variables for fortification training
				Set<OWLIndividual> fortificationTrainingPos = fortificationSetsPos.get(currFold);
				Set<OWLIndividual> fortificationTrainingNeg= fortificationSetsNeg.get(currFold);

				Set<OWLIndividual> allFortificationExamples = new HashSet<>();

				allFortificationExamples.addAll(fortificationTrainingPos);
				allFortificationExamples.addAll(fortificationTrainingNeg);	//duplicate will be remove automatically


				JaccardSimilarity newJaccardSimilarity = new JaccardSimilarity(pelletReasoner);

				//start the BLIND fortification and calculate the scores
				int count = 1;	//count the step of fortification
				int tmp_id = 1;
				for (CELOEPartial.PartialDefinition cpdef : counterPartialDefinitions) {

					//assign id for cpdef for debugging purpose
					cpdef.setId("#" + tmp_id++);



					//--------------------
					//blind fortification
					//--------------------
					Set<OWLIndividual> cpdefCp = rs.hasType(cpdef.getDescription(), curFoldPosTestSet);
					Set<OWLIndividual> cpdefCn = rs.hasType(cpdef.getDescription(), curFoldNegTestSet);


					//--------------------------------
					//Fortification Validation (FV)
					//--------------------------------
					Set<OWLIndividual> fortCp = rs.hasType(cpdef.getDescription(), fortificationTrainingPos);
					Set<OWLIndividual> fortCn = rs.hasType(cpdef.getDescription(), fortificationTrainingNeg);


					Set<OWLIndividual> conceptCp = rs.hasType(concept, fortificationTrainingPos);
					Set<OWLIndividual> conceptCn = rs.hasType(concept, fortificationTrainingNeg);


					int cp = fortCp.size();
					int cn = fortCn.size();

					//this needs to be revised: calculate cfp, cfn once
					fortCp.removeAll(conceptCp);
					fortCn.removeAll(conceptCn);


					double fortificationValidationScore = FortificationUtils.fortificationScore(pelletReasoner, cpdef.getDescription(), concept,
							cp, cn, fortificationTrainingPos.size(), fortificationTrainingNeg.size(),
							cp-fortCp.size(), cn-fortCn.size(), ((ParCELAbstract)la).getMaximumHorizontalExpansion());


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
						newJaccardSimilarityScore = newJaccardSimilarity.getJaccardSimilarityComplex(concept, cpdef.getDescription());
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

					cpdef.setAdditionValue(0, trainingCoverageScore);		//no of neg. examples in training set covered by the cpdef
					cpdef.setAdditionValue(1, conceptOverlapSimilairtyScore);		//can be used to infer jaccard overlap score
					cpdef.setAdditionValue(2, fortificationValidationScore);	//fortification validation strategy
					cpdef.setAdditionValue(3, similarityPosNegScore);
					cpdef.setAdditionValue(4, newJaccardSimilarityScore);
					cpdef.setAdditionValue(5, allScores);
					cpdef.setAdditionValue(6, randomScore);

					//------------------------
					//BLIND fortification
					//------------------------
					boolean cpChanged = cpdefPositiveCovered.addAll(cpdefCp);
					boolean cnChanged = cpdefNegativeCovered.addAll(cpdefCn);

					cpdefPositiveCovered.addAll(cpdefCp);
					cpdefNegativeCovered.addAll(cpdefCn);


					totalCPDefLength += OWLClassExpressionUtils.getLength(cpdef.getDescription());

					//print the cpdef which covers some pos. examples
					String changed = "";
					if (cpChanged || cnChanged) {
						changed = "(" + (cpChanged?"-":"") + (cnChanged?"+":"") + ")";

						outputWriter(count++ + changed + ". " + FortificationUtils.getCpdefString(cpdef, baseURI, prefixes)
								+ ", cp=" + rs.hasType(cpdef.getDescription(), curFoldPosTestSet)
								+ ", cn=" + rs.hasType(cpdef.getDescription(), curFoldNegTestSet));
					}
					else if (logger.isDebugEnabled()) {
						logger.debug(count++ + changed + ". " + FortificationUtils.getCpdefString(cpdef, baseURI, prefixes)
								+ ", cp=" + rs.hasType(cpdef.getDescription(), curFoldPosTestSet)
								+ ", cn=" + rs.hasType(cpdef.getDescription(), curFoldNegTestSet));
					}
					/*
					if (cpChanged || cnChanged)
						changed = "(" + (cpChanged?"-":"") + (cnChanged?"+":"") + ")";

					outputWriter(count++ + changed + ". " + FortificationUtils.getCpdefString(cpdef, baseURI, prefixes)
							+ ", cp=" + rs.hasType(cpdef.getDescription(), curFoldPosTestSet)
							+ ", cn=" + rs.hasType(cpdef.getDescription(), curFoldNegTestSet));
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
				avgCPDefLengthStat.addNumber(avgCPDefLength);

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
						rs, concept, counterPartialDefinitions, curFoldPosTestSet, curFoldNegTestSet, false);

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
				/// 2. Fortification - CONCEPT SIMILARITY & OVERLAP
				//------------------------------------------------
				outputWriter("---------------------------------------------");
				outputWriter("Fortification - CONCEPT SIMILARITY & OVERLAP");
				outputWriter("---------------------------------------------");

				INDEX = FortificationUtils.CONCEPT_OVERL_SIM_INDEX;

				SortedSet<CELOEPartial.PartialDefinition> similarityAndOverlapCpdef = new TreeSet<CELOEPartial.PartialDefinition>(new FortificationUtils.AdditionalValueComparator(1));
				similarityAndOverlapCpdef.addAll(counterPartialDefinitions);


				//counter partial definition is sorted by training coverage by default ==> don't need to sort the cpdef set
				multiStepFortificationResult[INDEX] = FortificationUtils.fortifyAccuracyMultiSteps(
						rs, concept, similarityAndOverlapCpdef, curFoldPosTestSet, curFoldNegTestSet, false);

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
				/// 3. Fortification - FORTIFICATION VALIDATION
				//------------------------------------------------
				outputWriter("---------------------------------------------");
				outputWriter("Fortification - FORTIFICATION VALIDATION");
				outputWriter("---------------------------------------------");

				INDEX = FortificationUtils.FORTIFICATION_VALIDATION_INDEX;

				SortedSet<CELOEPartial.PartialDefinition> fortificationValidationCpdef = new TreeSet<CELOEPartial.PartialDefinition>(new FortificationUtils.AdditionalValueComparator(2));
				fortificationValidationCpdef.addAll(counterPartialDefinitions);


				//counter partial definition is sorted by training coverage by default ==> don't need to sort the cpdef set
				multiStepFortificationResult[INDEX] = FortificationUtils.fortifyAccuracyMultiSteps(
						rs, concept, fortificationValidationCpdef, curFoldPosTestSet, curFoldNegTestSet, false);

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

				SortedSet<CELOEPartial.PartialDefinition> similarityNegPosCpdef = new TreeSet<CELOEPartial.PartialDefinition>(new FortificationUtils.AdditionalValueComparator(3));
				similarityNegPosCpdef.addAll(counterPartialDefinitions);


				//counter partial definition is sorted by training coverage by default ==> don't need to sort the cpdef set
				multiStepFortificationResult[INDEX] = FortificationUtils.fortifyAccuracyMultiSteps(
						rs, concept, similarityNegPosCpdef, curFoldPosTestSet, curFoldNegTestSet, false);

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

				SortedSet<CELOEPartial.PartialDefinition> jaccardOverlapCpdef = new TreeSet<CELOEPartial.PartialDefinition>(new FortificationUtils.AdditionalValueComparator(4));
				jaccardOverlapCpdef.addAll(counterPartialDefinitions);


				//counter partial definition is sorted by training coverage by default ==> don't need to sort the cpdef set
				multiStepFortificationResult[INDEX] = FortificationUtils.fortifyAccuracyMultiSteps(
						rs, concept, jaccardOverlapCpdef, curFoldPosTestSet, curFoldNegTestSet, false);

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

				SortedSet<CELOEPartial.PartialDefinition> jaccardDistanceCpdef = new TreeSet<CELOEPartial.PartialDefinition>(new FortificationUtils.AdditionalValueComparator(4, false));
				jaccardDistanceCpdef.addAll(counterPartialDefinitions);


				//counter partial definition is sorted by training coverage by default ==> don't need to sort the cpdef set
				multiStepFortificationResult[INDEX] = FortificationUtils.fortifyAccuracyMultiSteps(
						rs, concept, jaccardDistanceCpdef, curFoldPosTestSet, curFoldNegTestSet, false);

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

				SortedSet<CELOEPartial.PartialDefinition> combinationScoreCpdef = new TreeSet<CELOEPartial.PartialDefinition>(new FortificationUtils.AdditionalValueComparator(5));
				combinationScoreCpdef.addAll(counterPartialDefinitions);


				//counter partial definition is sorted by training coverage by default ==> don't need to sort the cpdef set
				multiStepFortificationResult[INDEX] = FortificationUtils.fortifyAccuracyMultiSteps(
						rs, concept, combinationScoreCpdef, curFoldPosTestSet, curFoldNegTestSet, false);

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

				SortedSet<CELOEPartial.PartialDefinition> randomCpdef = new TreeSet<CELOEPartial.PartialDefinition>(new FortificationUtils.AdditionalValueComparator(6));
				randomCpdef.addAll(counterPartialDefinitions);


				//counter partial definition is sorted by training coverage by default ==> don't need to sort the cpdef set
				multiStepFortificationResult[INDEX] = FortificationUtils.fortifyAccuracyMultiSteps(
						rs, concept, randomCpdef, curFoldPosTestSet, curFoldNegTestSet, false);

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
				// Fortification with
				// 	LABLED TEST DATA
				//------------------------------
				//if there exists covered negative examples ==> check if there are any counter partial definitions
				//can be used to remove covered negative examples

				int fixedNeg = 0;
				int fixedPos = 0;
				int selectedCpdef = 0;
				int totalSelectedCpdefLength = 0;
				double avgTrainingCoverage = 0;

				/**
				 * selected cpdef which are selected based on the test labled data
				 * given a set of wrong classified neg., select a set of cpdef to remove the wrong classified neg examples
				 * the cpdef are sorted based on the training neg. example coverage
				 */
				TreeSet<CELOEPartial.PartialDefinition> selectedCounterPartialDefinitions = new TreeSet<CELOEPartial.PartialDefinition>(new FortificationUtils.CoverageComparator());

				if (cnTest.size() > 0) {

					TreeSet<OWLIndividual> tempCoveredNeg = new TreeSet<>();
					tempCoveredNeg.addAll(cnTest);

					TreeSet<OWLIndividual> tempUncoveredPos = new TreeSet<>();
					tempUncoveredPos.addAll(upTest);

					//check each counter partial definitions
					for (CELOEPartial.PartialDefinition cpdef : counterPartialDefinitions) {

						//set of neg examples covered by the counter partial definition
						Set<OWLIndividual> desCoveredNeg = new HashSet<>(rs.hasType(cpdef.getDescription(), curFoldNegTestSet));

						//if the current counter partial definition can help to remove some neg examples
						//int oldNoOfCoveredNeg=tempCoveredNeg.size();
						if (tempCoveredNeg.removeAll(desCoveredNeg)) {

							//assign cn on test set to additionalValue
							selectedCounterPartialDefinitions.add(cpdef);

							//check if it may remove some positive examples or not
							Set<OWLIndividual> desCoveredPos = new HashSet<>(rs.hasType(cpdef.getDescription(), curFoldPosTestSet));
							tempUncoveredPos.addAll(desCoveredPos);

							//count the total number of counter partial definition selected and their total length
							selectedCpdef++;
							totalSelectedCpdefLength += OWLClassExpressionUtils.getLength(cpdef.getDescription());
							avgTrainingCoverage += cpdef.getCoverage();
						}

						if (tempCoveredNeg.size() == 0)
							break;
					}

					fixedNeg = cnTest.size() - tempCoveredNeg.size();
					fixedPos = tempUncoveredPos.size() - upTest.size();
					avgTrainingCoverage /= selectedCpdef;
				}


				noOfLabelFortifyDefinitions.addNumber(selectedCpdef);
				avgLabelFortifyCpdefCoverage.addNumber(avgTrainingCoverage);


				//-----------------------------
				// Labeled fortification
				// 	  stat calculation
				//-----------------------------
				//def length
				double labelFortifiedDefinitionLength = OWLClassExpressionUtils.getLength(concept) + totalSelectedCpdefLength + selectedCpdef;	//-1 from the selected cpdef and +1 for NOT
				lableFortifyDefinitionLengthStat.addNumber(labelFortifiedDefinitionLength);

				double avgLabelFortifyDefinitionLength = 0;

				if (selectedCpdef > 0) {
					avgLabelFortifyDefinitionLength = (double)totalSelectedCpdefLength/selectedCpdef;
					avgLabelCpdefLengthStat.addNumber(totalSelectedCpdefLength/(double)selectedCpdef);
				}

				//accuracy
				double fortifiedAccuracy = 100 * ((double)(correctTestExamples + fixedNeg - fixedPos)/
						(curFoldPosTestSet.size() + curFoldNegTestSet.size()));
				accuracyLabelFortifyStat.addNumber(fortifiedAccuracy);

				//completeness
				double fortifiedCompleteness = 100 * ((double)(correctTestPosClassified - fixedPos)/curFoldPosTestSet.size());
				completenessLabelFortifyStat.addNumber(fortifiedCompleteness);

				//correctness
				double fortifiedCorrectness = 100 * ((double)(correctTestNegClassified + fixedNeg)/curFoldNegTestSet.size());
				correctnessLabelFortifyStat.addNumber(fortifiedCorrectness);

				//precision, recall, f-measure
				double labelFortifiedPrecision = 0.0;	//percent of correct pos examples in total pos examples classified (= correct pos classified + neg as pos)
				if (((correctTestPosClassified - fixedPos) + (cnTest.size() - fixedNeg)) > 0)
					labelFortifiedPrecision = (double)(correctTestPosClassified - fixedPos)/
							(correctTestPosClassified - fixedPos + cnTest.size() - fixedNeg);	//tmp3: neg as pos <=> false pos

				double labelFortifiedRecall = (double)(correctTestPosClassified - fixedPos) / curFoldPosTestSet.size();

				double labelFortifiedFmeasure = 100 * Heuristics.getFScore(labelFortifiedRecall, labelFortifiedPrecision);
				fmeasureLabelFortifyStat.addNumber(labelFortifiedFmeasure);



				outputWriter("---------------------------------------------");
				outputWriter("LABEL fortify counter partial definitions: ");
				outputWriter("---------------------------------------------");
				count = 1;
				//output the selected counter partial definition information
				if (selectedCpdef > 0) {
					for (CELOEPartial.PartialDefinition cpdef : selectedCounterPartialDefinitions) {

						outputWriter(count++ + cpdef.getId() + ". " + FortificationUtils.getCpdefString(cpdef, baseURI, prefixes)
								+ ", cp=" + rs.hasType(cpdef.getDescription(), curFoldPosTestSet)
								+ ", cn=" + rs.hasType(cpdef.getDescription(), curFoldNegTestSet));

					}

				}	//end of labelled fortification STAT


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

				//calculate accuracy, fmeasure by HALF FULL of the cpdef
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
				//outputWriter("  concept: " + concept.toKBSyntaxString(baseURI, prefixes));

				//training and test error
				outputWriter("  training: " + correctTrainingPosClassified + "/" + curFoldPosTrainingSet.size() +
						" correct positive and " +
						(correctTrainingNegClassified) + "/" + curFoldNegTrainingSet.size() + " correct negative examples");

				outputWriter("  testing: " + correctTestPosClassified + "/"
						+ curFoldPosTestSet.size() + " correct positives, "
						+ correctTestNegClassified + "/" + curFoldNegTestSet.size() + " correct negatives");

				//runtime
				outputWriter("  runtime: " + df.format(algorithmDurationPdef/1000000000d)	+ "s");
				outputWriter("  runtime fortified: " + df.format((algorithmDurationCpdef+algorithmDurationPdef)/1000000000d) + "s");

				//def. length
				outputWriter("  def. length: " + OWLClassExpressionUtils.getLength(concept));
				outputWriter("  def. length label fortify: " + labelFortifiedDefinitionLength);
				outputWriter("  avg. def. length label fortify: " + avgLabelFortifyDefinitionLength);
				outputWriter("  total cpdef length: " + totalCPDefLength);
				outputWriter("  avg cpdef. length: " + avgCPDefLength);

				outputWriter("  no of cpdef: " + counterPartialDefinitions.size());
				outputWriter("  no of cpdef used in the multi-step fortification: " + Arrays.toString(noOfCpdefMultiStep));
//				outputWriter("  total number of descriptions: " + noOfDescriptionGenerated);
				outputWriter("  no of counter partial def used in the lable fortification: " + selectedCpdef);


				outputWriter("  F-Measure on training set: " + df.format(fMeasureTrainingFold));
				outputWriter("  F-Measure on test set: " + df.format(fMeasureTestingFold));
				outputWriter("  F-Measure on test set label fortification: " + df.format(labelFortifiedFmeasure));
				outputWriter("  F-measure on test set blind fortification: " + df.format(blindFmeasure));

				outputWriter("  accuracy: " + df.format(testingAccuracyCurrFold) + "% ("
						+ "corr:" + df.format(testingCorrectness)
						+ "%, comp:" + df.format(testingCompleteness)
						+ "%)  -- training: " + df.format(trainingAccuracy)
						+ "% (corr:" + df.format(trainingCorrectness)
						+ "%, comp:" + df.format(trainingCompleteness)
						+ "%)");

				outputWriter("  accuracy label fortification: " + df.format(fortifiedAccuracy) + "% ("
						+ "corr:" + df.format(fortifiedCorrectness)
						+ "%, comp:" + df.format(fortifiedCompleteness)
						+ ")");

				outputWriter("  accuracy blind fortification: " + df.format(blindFortificationAccuracy) +
						"% ( corr:" + df.format(blindFortificationCorrectness)
						+ "%, comp:" + df.format(blindFortificationCompleteness)
						+ ")");

				outputWriter("\n  FAIR evaluation");
				outputWriter("\taccuracy: " + df.format(fairAccuracyCurrFold)
						+ "%, correctness: " + df.format(fairCorrectness)
						+ "%, completeness: " + df.format(fairCompleteness));
				outputWriter("\tf-measure: " + df.format(fairFMeasureCurrFold));

				outputWriter("");

				//output the fortified accuracy at 5%, 10%, ..., 50%
				for (int i=0; i<noOfStrategies; i++) {
					outputWriter("  multi-step fortified accuracy by " + FortificationUtils.strategyNames[i] + ": "
							+ FortificationUtils.arrayToString(df, multiStepFortificationResult[i].fortificationAccuracy)
							+ " -- correctness: " + FortificationUtils.arrayToString(df, multiStepFortificationResult[i].fortificationCorrectness)
							+ " -- completeness: " + FortificationUtils.arrayToString(df, multiStepFortificationResult[i].fortificationCompleteness)
						);

					outputWriter("");
				}	//output fortified accuracy at 5%, 10%, ..., 50%



				//----------------------------------------------
				//output fold accumulative stat. information
				//----------------------------------------------
				outputWriter("----------");
				outputWriter("Aggregate data from fold 0 to fold " + currFold + "/" + folds);
				outputWriter("  runtime parcel: " + statOutput(df, runtime, "s"));
				outputWriter("  runtime fortified: " + statOutput(df, fortifiedRuntime, "s"));

				outputWriter("  no of descriptions: " + statOutput(df, totalNumberOfDescriptions, ""));
				outputWriter("  no of total pdef: " + statOutput(df, noOfPartialDefinitionStat, ""));
				outputWriter("  no of used pdef: " + statOutput(df, noOfReducedPartialDefinitionStat, ""));
				outputWriter("  avg pdef length: " + statOutput(df, avgPdefLengthStat, ""));
				outputWriter("  no of counter partial definitions: " + statOutput(df, noOfCounterPartialDefinitions, ""));
				outputWriter("  avg. cpdef length: " + statOutput(df, avgCPDefLengthStat, ""));
				outputWriter("  avg. def. length: " + statOutput(df, length, ""));

				outputWriter("  avg. no of counter partial definition used in label fortification: "
						+ statOutput(df, noOfLabelFortifyDefinitions, ""));
				outputWriter("  avg. label fortified def. length : " + statOutput(df, lableFortifyDefinitionLengthStat, ""));
				outputWriter("  avg. label fortify def. length : " + statOutput(df, labelFortifyDefinitionLengthStat, ""));

				outputWriter("  F-Measure on training set: " + statOutput(df, fMeasureTraining, "%"));
				outputWriter("  F-Measure on test set: " + statOutput(df, fMeasure, "%"));
				outputWriter("  F-Measure on test set fortified: " + statOutput(df, fmeasureLabelFortifyStat, "%"));
				outputWriter("  F-Measure FAIR on test set fortified: " + statOutput(df, fairFmeasureStat, "%"));
				outputWriter("  predictive accuracy on training set: " + statOutput(df, accuracyTraining, "%") +
						" -- correctness: " + statOutput(df, trainingCorrectnessStat, "%") +
						"-- completeness: " + statOutput(df, trainingCompletenessStat, "%"));
				outputWriter("  predictive accuracy on test set: " + statOutput(df, accuracy, "%") +
						" -- correctness: " + statOutput(df, testingCorrectnessStat, "%") +
						"-- completeness: " + statOutput(df, testingCompletenessStat, "%"));

				outputWriter("  fortified accuracy on test set: " + statOutput(df, accuracyLabelFortifyStat, "%") +
						" -- fortified correctness: " + statOutput(df, correctnessLabelFortifyStat, "%") +
						"-- fortified completeness: " + statOutput(df, completenessLabelFortifyStat, "%"));

				outputWriter("  blind fortified accuracy on test set: " + statOutput(df, accuracyBlindFortifyStat, "%") +
						" -- fortified correctness: " + statOutput(df, correctnessBlindFortifyStat, "%") +
						"-- fortified completeness: " + statOutput(df, completenessBlindFortifyStat, "%"));

				outputWriter("\n  FAIR evaluation");
				outputWriter("\taccuracy: " + statOutput(df, fairAccuracyStat, "%")
						+ "-- correctness: " + statOutput(df, fairCorrectnessStat, "%")
						+ "%, completeness: " + statOutput(df, fairCompletenessStat, "%"));
				outputWriter("\tf-measure: " + statOutput(df, fairFmeasureStat, "%"));

				outputWriter("----------");

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


				outputWriter("----------------------");

				try {
					Thread.sleep(5000);
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}

			}	//k-fold cross validation


			//---------------------------------
			//end of k-fold cross validation
			//output result of the k-fold
			//---------------------------------

			//final cumulative statistical data of a run

			outputWriter("");
			outputWriter("Finished the " + (kk+1) + "/" + noOfRuns + " of " + folds + "-folds cross-validation.");
			outputWriter("  runtime celoe: " + statOutput(df, runtime, "s"));
			outputWriter("  runtime fortified: " + statOutput(df, fortifiedRuntime, "s"));

			outputWriter("  no of descriptions: " + statOutput(df, totalNumberOfDescriptions, ""));
			outputWriter("  no of total pdef: " + statOutput(df, noOfPartialDefinitionStat, ""));
			outputWriter("  no of used pdef: " + statOutput(df, noOfReducedPartialDefinitionStat, ""));
			outputWriter("  avg pdef length: " + statOutput(df, avgPdefLengthStat, ""));
			outputWriter("  no of counter partial definitions: " + statOutput(df, noOfCounterPartialDefinitions, ""));
			outputWriter("  avg. cpdef length: " + statOutput(df, avgCPDefLengthStat, ""));
			outputWriter("  avg. def. length: " + statOutput(df, length, ""));

			outputWriter("  avg. no of counter partial definition used in label fortification: "
					+ statOutput(df, noOfLabelFortifyDefinitions, ""));

			outputWriter("  avg. label fortified def. length : " + statOutput(df, lableFortifyDefinitionLengthStat, ""));
			outputWriter("  avg. fortify def. length : " + statOutput(df, labelFortifyDefinitionLengthStat, ""));

			outputWriter("  F-Measure on training set: " + statOutput(df, fMeasureTraining, "%"));
			outputWriter("  F-Measure on test set: " + statOutput(df, fMeasure, "%"));
			outputWriter("  F-Measure label fortification on test set: " + statOutput(df, fmeasureLabelFortifyStat, "%"));
			outputWriter("  F-Measure blind fortification on test set: " + statOutput(df, fmeasureBlindFortifyStat, "%"));

			outputWriter("  predictive accuracy on training set: " + statOutput(df, accuracyTraining, "%") +
					" -- correctness: " + statOutput(df, trainingCorrectnessStat, "%") +
					"-- completeness: " + statOutput(df, trainingCompletenessStat, "%"));

			outputWriter("  predictive accuracy on test set: " + statOutput(df, accuracy, "%") +
					" -- correctness: " + statOutput(df, testingCorrectnessStat, "%") +
					"-- completeness: " + statOutput(df, testingCompletenessStat, "%"));

			outputWriter("  fortified accuracy on test set: " + statOutput(df, accuracyLabelFortifyStat, "%") +
					" -- fortified correctness: " + statOutput(df, correctnessLabelFortifyStat, "%") +
					"-- fortified completeness: " + statOutput(df, completenessLabelFortifyStat, "%"));

			outputWriter("  blind fortified accuracy on test set: " + statOutput(df, accuracyBlindFortifyStat, "%") +
					" -- fortified correctness: " + statOutput(df, correctnessBlindFortifyStat, "%") +
					" -- fortified completeness: " + statOutput(df, completenessBlindFortifyStat, "%"));

			outputWriter("\n  FAIR evaluation");
			outputWriter("\taccuracy: " + statOutput(df, fairAccuracyStat, "%")
					+ "-- correctness: " + statOutput(df, fairCorrectnessStat, "%")
					+ "%, completeness: " + statOutput(df, fairCompletenessStat, "%"));
			outputWriter("\tf-measure: " + statOutput(df, fairFmeasureStat, "%"));


			//------------------------------------------------------------
			//compute cut-off point and the metrics at the cut-off point
			//------------------------------------------------------------
			int cutOffPoint = 0;
			//double cutOffAvg[][], cutOffDev[][];
			//cutOffAvg = new double[3][noOfStrategies];	//0: accuracy, 1: correctness, 2: completeness
			//cutOffDev = new double[3][noOfStrategies];

			//cut-off point is the max number of the labelled fortification definitions
			if (noOfLabelFortifyDefinitions.getMean() > 0)	//this is for a weird side-affect of the floating point such that the getMax return a very small number >0 even if the acutuall value is zero
				cutOffPoint = (int)Math.round(Math.ceil(noOfLabelFortifyDefinitions.getMax()));

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

			outputWriter("  no of cpdef used in multi-step fortification:");
			outputWriter("\t5%: " + statOutput(df, noOfCpdefUsedMultiStepFortStat[0], ""));
			outputWriter("\t10%: " + statOutput(df, noOfCpdefUsedMultiStepFortStat[1], ""));
			outputWriter("\t20%: " + statOutput(df, noOfCpdefUsedMultiStepFortStat[2], ""));
			outputWriter("\t30%: " + statOutput(df, noOfCpdefUsedMultiStepFortStat[3], ""));
			outputWriter("\t40%: " + statOutput(df, noOfCpdefUsedMultiStepFortStat[4], ""));
			outputWriter("\t50%: " + statOutput(df, noOfCpdefUsedMultiStepFortStat[5], ""));
			/*
			outputWriter("======= Fmeasure full steps (of 50%) =======");
			for (int i=0; i<strategyNames.length; i++) {	//4 strategies
				outputWriter(strategyNames[i] + ": ");
				for (int j=0; j<minOfHalfCpdef; j++) {
					outputWriter(df.format(fmeasureHalfFullStep[i][j]/10d) + "\t");
				}
				outputWriter("\n");
			}


			outputWriter("======= Accuracy full steps (of 50%) =======");
			for (int i=0; i<strategyNames.length; i++) {	//4 strategies
				outputWriter(strategyNames[i] + ": ");
				for (int j=0; j<minOfHalfCpdef; j++) {
					outputWriter(df.format(accuracyHalfFullStep[i][j]/10d) + "\t");
				}
				outputWriter("\n");
			}
			*/



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
				outputWriter(FortificationUtils.strategyNames[i] + "(" + minCpdef + ")");
				for (int j=0; j<minCpdef; j++) {
					outputWriter(df.format(fmeasureFullStepStat[i][j].getMean()) + "\t"
							+ df.format(fmeasureFullStepStat[i][j].getStandardDeviation()));
				}
				outputWriter("\n");
			}


			outputWriter("======= Accuracy full steps =======");
			for (int i=0; i<noOfStrategies; i++) {	//6 strategies
				outputWriter(FortificationUtils.strategyNames[i] + "(" + minCpdef + ")");
				for (int j=0; j<minCpdef; j++) {
					outputWriter(df.format(accuracyFullStepStat[i][j].getMean()) + "\t"				//accuracy
							+ df.format(accuracyFullStepStat[i][j].getStandardDeviation()));
				}
				outputWriter("\n");
			}



			outputWriter("======= Correctness full steps =======");
			for (int i=0; i<noOfStrategies; i++) {	//6 strategies
				outputWriter(FortificationUtils.strategyNames[i] + "(" + minCpdef + ")");
				for (int j=0; j<minCpdef; j++) {
					outputWriter(df.format(correctnessFullStepStat[i][j].getMean()) + "\t"
							+ df.format(correctnessFullStepStat[i][j].getStandardDeviation()));
				}
				outputWriter("\n");
			}


			outputWriter("======= Completeness full steps =======");
			for (int i=0; i<noOfStrategies; i++) {	//4 strategies
				outputWriter(FortificationUtils.strategyNames[i] + "(" + minCpdef + ")");
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

				if ((j == Math.round(Math.ceil(noOfLabelFortifyDefinitions.getMin()))) ||
						(j == Math.round(Math.ceil(noOfLabelFortifyDefinitions.getMax()))) ||
						(j == Math.round(Math.ceil(noOfLabelFortifyDefinitions.getMean())))) {

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


			//TODO: multiple runs have not been revised
			if (noOfRuns > 1) {
				// runtime
				runtimeAvg.addNumber(runtime.getMean());
				runtimeMax.addNumber(runtime.getMax());
				runtimeMin.addNumber(runtime.getMin());
				runtimeDev.addNumber(runtime.getStandardDeviation());

				defLenAvg.addNumber(length.getMean());
				defLenMax.addNumber(length.getMax());
				defLenMin.addNumber(length.getMin());
				defLenDev.addNumber(length.getStandardDeviation());

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

				testingCorAvg.addNumber(testingCorrectnessStat.getMean());
				testingCorDev.addNumber(testingCorrectnessStat.getStandardDeviation());
				testingCorMax.addNumber(testingCorrectnessStat.getMax());
				testingCorMin.addNumber(testingCorrectnessStat.getMin());

				testingComAvg.addNumber(testingCompletenessStat.getMean());
				testingComDev.addNumber(testingCompletenessStat.getStandardDeviation());
				testingComMax.addNumber(testingCompletenessStat.getMax());
				testingComMin.addNumber(testingCompletenessStat.getMin());

				testingFMesureAvg.addNumber(fMeasure.getMean());
				testingFMesureDev.addNumber(fMeasure.getStandardDeviation());
				testingFMesureMax.addNumber(fMeasure.getMax());
				testingFMesureMin.addNumber(fMeasure.getMin());

				trainingFMesureAvg.addNumber(fMeasureTraining.getMean());
				trainingFMesureDev.addNumber(fMeasureTraining.getStandardDeviation());
				trainingFMesureMax.addNumber(fMeasureTraining.getMax());
				trainingFMesureMin.addNumber(fMeasureTraining.getMin());

				noOfDescriptionsAgv.addNumber(totalNumberOfDescriptions.getMean());
				noOfDescriptionsMax.addNumber(totalNumberOfDescriptions.getMax());
				noOfDescriptionsMin.addNumber(totalNumberOfDescriptions.getMin());
				noOfDescriptionsDev.addNumber(totalNumberOfDescriptions.getStandardDeviation());
			}
		} // for kk folds


		if (noOfRuns > 1) {
			outputWriter("");
			outputWriter("Finished " + noOfRuns + " time(s) of the " + folds + "-folds cross-validations");

			outputWriter("runtime: " +
					"\n\t avg.: " + statOutput(df, runtimeAvg, "s") +
					"\n\t dev.: " + statOutput(df, runtimeDev, "s") +
					"\n\t max.: " + statOutput(df, runtimeMax, "s") +
					"\n\t min.: " + statOutput(df, runtimeMin, "s"));


			outputWriter("no of descriptions: " +
					"\n\t avg.: " + statOutput(df, noOfDescriptionsAgv, "") +
					"\n\t dev.: " + statOutput(df, noOfDescriptionsDev, "") +
					"\n\t max.: " + statOutput(df, noOfDescriptionsMax, "") +
					"\n\t min.: " + statOutput(df, noOfDescriptionsMin, ""));

			outputWriter("definition length: " +
					"\n\t avg.: " + statOutput(df, defLenAvg, "") +
					"\n\t dev.: " + statOutput(df, defLenDev, "") +
					"\n\t max.: " + statOutput(df, defLenMax, "") +
					"\n\t min.: " + statOutput(df, defLenMin, ""));

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
					"\n\t dev.: " + statOutput(df, trainingComDev, "%") +
					"\n\t max.: " + statOutput(df, trainingComMax, "%") +
					"\n\t min.: " + statOutput(df, trainingComMin, "%"));

			outputWriter("FMesure on training set: " +
					"\n\t avg.: " + statOutput(df, trainingFMesureAvg, "%") +
					"\n\t dev.: " + statOutput(df, trainingFMesureDev, "%") +
					"\n\t max.: " + statOutput(df, trainingFMesureMax, "%") +
					"\n\t min.: " + statOutput(df, trainingFMesureMin, "%"));

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

			outputWriter("FMesure on testing set: " +
					"\n\t avg.: " + statOutput(df, testingFMesureAvg, "%") +
					"\n\t dev.: " + statOutput(df, testingFMesureDev, "%") +
					"\n\t max.: " + statOutput(df, testingFMesureMax, "%") +
					"\n\t min.: " + statOutput(df, testingFMesureMin, "%"));
		}
	}


	/*
	class URIComparator implements Comparator<Individual> {
		@Override
		public int compare(Individual o1, Individual o2) {
			return o1.getURI().compareTo(o2.getURI());
		}

	}
	*/


	/*
	class CoverageComparator implements Comparator<CELOEPartial.PartialDefinition> {
		@Override
		public int compare(CELOEPartial.PartialDefinition p1, CELOEPartial.PartialDefinition p2) {
			if (p1.getCoverage() > p2.getCoverage())
				return -1;
			else if (p1.getCoverage() < p2.getCoverage())
				return 1;
			else
				return new ConceptComparator().compare(p1.getDescription(), p2.getDescription());

		}
	}
	*/


	/**
	 * Sort descreasingly
	 *
	 * @author An C. Tran
	 *
	 */
	/*
	class AdditionalValueComparator implements Comparator<CELOEPartial.PartialDefinition> {
		int index = 0;
		boolean descending;

		public AdditionalValueComparator(int index) {
			this.index = index;
			this.descending = true;
		}


		public AdditionalValueComparator(int index, boolean descending) {
			this.index = index;
			this.descending = descending;
		}



		@Override
		public int compare(CELOEPartial.PartialDefinition pdef1, CELOEPartial.PartialDefinition pdef2) {
			if (pdef1.getAdditionValue(index) > pdef2.getAdditionValue(index)) {
				if (this.descending)
					return -1;
				else
					return 1;
			}
			else if (pdef1.getAdditionValue(index) < pdef2.getAdditionValue(index)) {
				if (this.descending)
					return 1;
				else
					return -1;
			}
			else
				return new ConceptComparator().compare(pdef1.getDescription(), pdef2.getDescription());

		}	//compare()

	}	//AdditionalValueComparator
	*/

}
