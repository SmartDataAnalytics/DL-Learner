package org.dllearner.cli.parcel;

import java.text.DecimalFormat;
import java.util.*;

import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.dllearner.algorithms.parcel.ParCELAbstract;
import org.dllearner.algorithms.parcel.ParCELExtraNode;
import org.dllearner.algorithms.parcel.ParCELPosNegLP;
import org.dllearner.algorithms.parcelex.ParCELExAbstract;
import org.dllearner.cli.CrossValidation;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.learningproblems.Heuristics;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.utilities.Files;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;
import org.dllearner.utilities.statistics.Stat;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;

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

public class ParCELExFortifiedCrossValidation extends CrossValidation {

	protected Stat noOfPartialDef;
	protected Stat avgPartialDefinitionLength;
	protected Stat noOfCounterPartialDefinitions;
	protected Stat noOfCounterPartialDefinitionsUsed;
	protected Stat learningTime;

	//fortify strategy statistical variables
	protected Stat accuracyFortify1Stat;
	protected Stat accuracyFortify2Stat;
	protected Stat correctnessFortify1Stat;
	protected Stat correctnessFortify2Stat;
	protected Stat completenessFortify2Stat;
	protected Stat fmeasureFortify1Stat;
	protected Stat fmeasureFortify2Stat;
	protected Stat avgFortifiedPartialDefinitionLengthStat;
	protected Stat avgFortifyDefinitionsLengthStat;
	protected Stat avgFortifyCoverageTrainingStatLevel1;
	protected Stat avgFortifyCoverageTestStatLevel1;
	protected Stat avgFortifyCoverageTrainingStatLevel2;
	protected Stat avgFortifyCoverageTestStatLevel2;

	protected Stat avgNoOfFortifiedDefinitions;
	protected Stat avgNoOfFortifiedDefinitionsL1;
	protected Stat avgNoOfFortifiedDefinitionsL2;

	Logger logger = Logger.getLogger(this.getClass());

	protected boolean interupted = false;

	/**
	 * Default constructor
	 */

	public ParCELExFortifiedCrossValidation(AbstractCELA la, PosNegLP lp, AbstractReasonerComponent rs,
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
	public ParCELExFortifiedCrossValidation(AbstractCELA la, ParCELPosNegLP lp, AbstractReasonerComponent rs,
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

		// get examples and shuffle them too
		Set<OWLIndividual> posExamples = lp.getPositiveExamples();
		List<OWLIndividual> posExamplesList = new LinkedList<>(posExamples);
		Collections.shuffle(posExamplesList, new Random(1));
		Set<OWLIndividual> negExamples = lp.getNegativeExamples();
		List<OWLIndividual> negExamplesList = new LinkedList<>(negExamples);
		Collections.shuffle(negExamplesList, new Random(2));

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
			Set<OWLIndividual> testPos = getTestingSet(posExamplesList, splitsPos, i);
			Set<OWLIndividual> testNeg = getTestingSet(negExamplesList, splitsNeg, i);
			testSetsPos.add(i, testPos);
			testSetsNeg.add(i, testNeg);
			trainingSetsPos.add(i, getTrainingSet(posExamples, testPos));
			trainingSetsNeg.add(i, getTrainingSet(negExamples, testNeg));
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

		Stat fortify1AccAvg = new Stat();
		Stat fortify1AccMax = new Stat();
		Stat fortify1AccMin = new Stat();
		Stat fortify1AccDev = new Stat();

		Stat fortify2AccAvg = new Stat();
		Stat fortify2AccMax = new Stat();
		Stat fortify2AccMin = new Stat();
		Stat fortify2AccDev = new Stat();


		Stat testingCorAvg = new Stat();
		Stat testingCorMax = new Stat();
		Stat testingCorMin = new Stat();
		Stat testingCorDev = new Stat();

		Stat fortify1CorAvg = new Stat();
		Stat fortify1CorMax = new Stat();
		Stat fortify1CorMin = new Stat();
		Stat fortify1CorDev = new Stat();

		Stat fortify2CorAvg = new Stat();
		Stat fortify2CorMax = new Stat();
		Stat fortify2CorMin = new Stat();
		Stat fortify2CorDev = new Stat();


		Stat testingComAvg = new Stat();
		Stat testingComMax = new Stat();
		Stat testingComMin = new Stat();
		Stat testingComDev = new Stat();


		Stat fortify2ComAvg = new Stat();
		Stat fortify2ComMax = new Stat();
		Stat fortify2ComMin = new Stat();
		Stat fortify2ComDev = new Stat();


		Stat testingFMeasureAvg = new Stat();
		Stat testingFMeasureMax = new Stat();
		Stat testingFMeasureMin = new Stat();
		Stat testingFMeasureDev = new Stat();

		Stat trainingFMeasureAvg = new Stat();
		Stat trainingFMeasureMax = new Stat();
		Stat trainingFMeasureMin = new Stat();
		Stat trainingFMeasureDev = new Stat();

		Stat fortify1FmeasureAvg = new Stat();
		Stat fortify1FmeasureMax = new Stat();
		Stat fortify1FmeasureMin = new Stat();
		Stat fortify1FmeasureDev = new Stat();


		Stat fortify2FmeasureAvg = new Stat();
		Stat fortify2FmeasureMax = new Stat();
		Stat fortify2FmeasureMin = new Stat();
		Stat fortify2FmeasureDev = new Stat();

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


		for (int kk=0; kk < noOfRuns; kk++) {

			//runtime
			runtime = new Stat();
			fMeasure = new Stat();
			fMeasureTraining = new Stat();

			noOfPartialDef = new Stat();
			avgPartialDefinitionLength = new Stat();
			length = new Stat();
			accuracyTraining = new Stat();
			trainingCorrectnessStat= new Stat();
			trainingCompletenessStat = new Stat();
			accuracy = new Stat();
			testingCorrectnessStat = new Stat();
			testingCompletenessStat = new Stat();

			noOfCounterPartialDefinitions = new Stat();
			noOfCounterPartialDefinitionsUsed = new Stat();
			learningTime = new Stat();

			//fortify strategy statistical variables
			accuracyFortify1Stat = new Stat();
			accuracyFortify2Stat = new Stat();
			correctnessFortify1Stat = new Stat();
			correctnessFortify2Stat = new Stat();
			completenessFortify2Stat = new Stat();
			fmeasureFortify1Stat = new Stat();
			fmeasureFortify2Stat = new Stat();
			avgFortifiedPartialDefinitionLengthStat = new Stat();
			avgFortifyCoverageTrainingStatLevel1 = new Stat();
			avgFortifyCoverageTestStatLevel1 = new Stat();
			avgFortifyCoverageTrainingStatLevel2 = new Stat();
			avgFortifyCoverageTestStatLevel2 = new Stat();
			avgFortifyDefinitionsLengthStat = new Stat();

			avgNoOfFortifiedDefinitions = new Stat();
			avgNoOfFortifiedDefinitionsL1 = new Stat();
			avgNoOfFortifiedDefinitionsL2 = new Stat();

			totalNumberOfDescriptions = new Stat();

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

				//learning time, does not include the reduction time
				long learningMili = ((ParCELAbstract)la).getLearningTime();
				learningTime.addNumber(learningMili/(double)1000);

				//no of counter partial definition
				this.noOfCounterPartialDefinitions.addNumber(((ParCELExAbstract)la).getNumberOfCounterPartialDefinitions());
				this.noOfCounterPartialDefinitionsUsed.addNumber(((ParCELExAbstract)la).getNumberOfCounterPartialDefinitionUsed());


				//cast the la into ParCELExAbstract for easy accessing
				ParCELExAbstract parcelEx = (ParCELExAbstract)la;

				//get the target (learned) definition
				OWLClassExpression concept = parcelEx.getUnionCurrenlyBestDescription();

				//for the training accuracy, we do not need to refine because the reducer warranty the best accuracy returned
				Set<OWLIndividual> tmp = rs.hasType(concept, trainingSetsPos.get(currFold));
				Set<OWLIndividual> tmp2 = Sets.difference(trainingSetsPos.get(currFold), tmp);
				Set<OWLIndividual> tmp3 = rs.hasType(concept, trainingSetsNeg.get(currFold));

				outputWriter("training set errors pos (" + tmp2.size() + "): " + tmp2);
				outputWriter("training set errors neg (" + tmp3.size() + "): " + tmp3);


				//calculate the accuracy on the test set
				tmp = rs.hasType(concept, testSetsPos.get(currFold));		//examples covered by the definition
				tmp2 = Sets.difference(testSetsPos.get(currFold), tmp);	//false negative
				tmp3 = rs.hasType(concept, testSetsNeg.get(currFold));		//false positive

				outputWriter("test set errors pos (" + tmp2.size() + "): " + tmp2);
				outputWriter("test set errors neg (" + tmp3.size() + "): " + tmp3);

				//if the correctness is not 100%, check if we can improve it using the counter partial definition
				int fixedNegLevel1 = 0;
				int fixedNegLevel2 = 0;
				int errorFixedPosLevel2 = 0;
				int addedLength = 0;	//total length of fortify counter partial definitions

				int fortifyLevel1Count = 0;
				int fortifyLevel2Count = 0;
				int totalFortifyTrainingCoverageLevel1 = 0;
				int totalFortifyTestCoverageLevel1 = 0;
				int totalFortifyTrainingCoverageLevel2 = 0;
				int totalFortifyTestCoverageLevel2 = 0;

				double avgFortifyTrainingCoverageLevel1 = 0;
				double avgFortifyTestCoverageLevel1 = 0;
				double avgFortifyTrainingCoverageLevel2 = 0;
				double avgFortifyTestCoverageLevel2 = 0;


				//if there is a false positive (neg. as pos.), identify the error partial definition and
				//use the counter partial definitions to fix it
				if (tmp3.size() > 0) {
					SortedSet<ParCELExtraNode> counterPartialDefinitions = parcelEx.getCounterPartialDefinitions();
					SortedSet<ParCELExtraNode> reducedPartialDefinitions = parcelEx.getReducedPartialDefinition();

					//identify the partial definition cause the false positive
					Set<OWLIndividual> negTestSet = testSetsNeg.get(currFold);
					Set<ParCELExtraTestingNode> fortifiedCounterDefinitions = new TreeSet<ParCELExtraTestingNode>(new ParCELTestingCorrectnessComparator());

					for (ParCELExtraNode pdef : reducedPartialDefinitions) {

						//check if the partial definition covers some of the negative examples
						Set<OWLIndividual> coveredNeg = rs.hasType(pdef.getDescription(), negTestSet);

						if (coveredNeg.size() > 0) {

							//create a temporary set to test for the intersection (this may not a good solution but for testing only)
							TreeSet<OWLIndividual> tempCoveredNeg = new TreeSet<>();
							tempCoveredNeg.addAll(coveredNeg);

							//if yes, find the counter partial definitions that can help to fix this
							logger.info("  ==> incorect partial definition: " + pdef +
									"\n\tcovered negative examples (" + coveredNeg.size() + "): " +
									coveredNeg);

							for (ParCELExtraNode cpdef : counterPartialDefinitions) {
								OWLClassExpression child = OWLClassExpressionUtils.getChildren(cpdef.getDescription()).iterator().next();
								Set<OWLIndividual> cpdefNegCovered = new HashSet<>(rs.hasType(child, negTestSet));
								logger.info("     - cpdef: " + child.toString() +
										", covered neg.: " + cpdefNegCovered);

								//if (tempCoveredNeg.removeAll(cpdef.getCoveredNegativeExamples())) {
								if (tempCoveredNeg.removeAll(cpdefNegCovered)) {
									//get the covered positive examples on test set
									Set<OWLIndividual> cpdefPosCovered = new HashSet<>(rs.hasType(child, testSetsPos.get(currFold)));

									//add the fortify counter partial definition into the potential fortify counter definition set
									fortifiedCounterDefinitions.add(new ParCELExtraTestingNode(cpdef, cpdefPosCovered, cpdefNegCovered));

									//logger.info("    --> fixed by: " + cpdef + "\n\tcovered negative examples: " + cpdefNegCovered +
									//		"\n\tcovered positive examples: " + cpdefPosCovered);

									//pdef.setDescription(new Intersection(pdef.getDescription(), cpdef.getDescription()));
									//NOTE: do not need to adjust the covered positive examples and covered negative examples. why?
									//calculate the adjusted accuracy (and correctness and completeness)

								}

								//if (tempCoveredNeg.size() == 0)
								//	break;
							}	//find the fortified counter partial definitions (for each counter partial definition)

						}	//if (cn(pd) > 0): the current partial definition covers some negative examples ==> potential fortify definition

					}	//for each reduced partial definition

					//process the potential fortify counter partial definitions
					//the fortify definitions are divided into 2 levels: level 1 covers neg. only, level 2 covers both pos. and neg.

					Set<ParCELExtraTestingNode> fortifiedLevel1 = new TreeSet<ParCELExtraTestingNode>(new ParCELTestingCorrectnessComparator());
					Set<ParCELExtraTestingNode> fortifiedLevel2 = new TreeSet<ParCELExtraTestingNode>(new ParCELTestingCorrectnessComparator());
					for (ParCELExtraTestingNode n : fortifiedCounterDefinitions) {
						if (n.getCoveredPositiveExamplesTestSet().size() == 0)
							fortifiedLevel1.add(n);
						else
							fortifiedLevel2.add(n);
					}

					//level 1:
					fixedNegLevel1 = tmp3.size();

					logger.info("  **fixing using fortifying counter partial definitions - level 1:");
					for (ParCELExtraTestingNode n : fortifiedLevel1) {
						if (tmp3.removeAll(n.getCoveredNegativeExamplestestSet())) {
							logger.info("    --> fixed by: " + n.getExtraNode() +
									"\n\tcovered negative examples (" + n.getCoveredNegativeExamplestestSet().size() + "): " +
									n.getCoveredNegativeExamplestestSet());

							addedLength += OWLClassExpressionUtils.getLength(n.getExtraNode().getDescription());

							fortifyLevel1Count++;
							totalFortifyTestCoverageLevel1 += n.getExtraNode().getCoveredNegativeExamples().size();
							totalFortifyTrainingCoverageLevel1+= n.getCoveredNegativeExamplestestSet().size();

							if (tmp3.size() <= 0)
								break;
						}
					}

					fixedNegLevel1 -= tmp3.size();	//number of negative examples removed by level 1

					//level 2:
					fixedNegLevel2 = tmp3.size();
					errorFixedPosLevel2 = tmp2.size();
					if (tmp3.size() > 0) {
						logger.info("  **fixing using fortifying counter partial definitions - level 2:");
						for (ParCELExtraTestingNode n : fortifiedLevel2) {
							if (tmp3.removeAll(n.getCoveredNegativeExamplestestSet())) {
								logger.info("    --> fixed by: " + n.getExtraNode() +
										"\n\tcovered negative examples (" + n.getCoveredNegativeExamplestestSet().size() + "): " +
										n.getCoveredNegativeExamplestestSet() +
										"\n\tcovered positive examples: " + n.getCoveredPositiveExamplesTestSet().size() + "): " +
										n.getCoveredPositiveExamplesTestSet());
								tmp2.addAll(n.getCoveredPositiveExamplesTestSet());

								addedLength += OWLClassExpressionUtils.getLength(n.getExtraNode().getDescription());

								fortifyLevel2Count++;
								totalFortifyTrainingCoverageLevel2 += n.getExtraNode().getCoveredNegativeExamples().size();
								totalFortifyTestCoverageLevel2 += n.getCoveredNegativeExamplestestSet().size();


								if (tmp3.size() <= 0)
									break;
							}
						}
					}

					fixedNegLevel2 -= tmp3.size();	//number of negative examples removed by level 2
					errorFixedPosLevel2 = tmp2.size() - errorFixedPosLevel2;	//number of positive examples that is wrong classified by level 2 of fixing

					if (fortifyLevel1Count > 0) {
						avgFortifyTrainingCoverageLevel1 = 100*totalFortifyTrainingCoverageLevel1/(double)fortifyLevel1Count;
						avgFortifyTrainingCoverageLevel1 /= trainingSetsNeg.get(currFold).size();

						avgFortifyTestCoverageLevel1 = 100*totalFortifyTestCoverageLevel1/(double)fortifyLevel1Count;
						avgFortifyTestCoverageLevel1 /= testSetsNeg.get(currFold).size();

						avgFortifyCoverageTrainingStatLevel1.addNumber(avgFortifyTrainingCoverageLevel1);
						avgFortifyCoverageTestStatLevel1.addNumber(avgFortifyTestCoverageLevel1);
					}

					if (fortifyLevel2Count > 0) {
						avgFortifyTrainingCoverageLevel2 = 100*totalFortifyTrainingCoverageLevel2/(double)fortifyLevel2Count;
						avgFortifyTrainingCoverageLevel2 /= trainingSetsNeg.get(currFold).size();

						avgFortifyTestCoverageLevel2 = 100*totalFortifyTestCoverageLevel2/(double)fortifyLevel2Count;
						avgFortifyTestCoverageLevel2 /= testSetsNeg.get(currFold).size();


						avgFortifyCoverageTrainingStatLevel2.addNumber(avgFortifyTrainingCoverageLevel2);
						avgFortifyCoverageTestStatLevel2.addNumber(avgFortifyTestCoverageLevel2);

					}

					outputWriter("test set errors pos after fixing: (" + tmp2.size() + "): " + tmp2);
					outputWriter("test set errors neg after fixing: (" + tmp3.size() + "): " + tmp3);

					/*
					//combine
					LinkedList<Description> tmpPartialDefinition = new LinkedList<Description>();
					for (ParCELExtraNode rpd : reducedPartialDefinitions)
						tmpPartialDefinition.add(rpd.getDescription());

					concept = new Union(tmpPartialDefinition);
					*/

				}	//of false positive

				avgNoOfFortifiedDefinitionsL1.addNumber(fortifyLevel1Count);
				avgNoOfFortifiedDefinitionsL2.addNumber(fortifyLevel2Count);
				avgNoOfFortifiedDefinitions.addNumber(fortifyLevel1Count+fortifyLevel2Count);

				double avgFortifyDefinitionsLength = (addedLength==0? 0 : (double)addedLength/(fortifyLevel1Count+fortifyLevel2Count));
				avgFortifyDefinitionsLengthStat.addNumber(avgFortifyDefinitionsLength);

				// calculate training accuracies
				int trainingPosSize = trainingSetsPos.get(currFold).size() ;
				int trainingNegSize = trainingSetsNeg.get(currFold).size();
				int testingPosSize = testSetsPos.get(currFold).size();
				int testingNegSize = testSetsNeg.get(currFold).size();

				//TODO: this should be refined so that we can use the above result to avoid re-calculation
				int trainingCorrectPosClassified = getCorrectPosClassified(rs, concept, trainingSetsPos.get(currFold));
				int trainingCorrectNegClassified = getCorrectNegClassified(rs, concept, trainingSetsNeg.get(currFold));
				int trainingCorrectExamples = trainingCorrectPosClassified + trainingCorrectNegClassified;
				double trainingAccuracy = 100*((double)trainingCorrectExamples/(trainingPosSize +
						trainingNegSize));

				double trainingCompleteness = 100*(double)trainingCorrectPosClassified/trainingPosSize;
				double trainingCorrectness = 100*(double)trainingCorrectNegClassified/trainingNegSize;

				accuracyTraining.addNumber(trainingAccuracy);
				trainingCompletenessStat.addNumber(trainingCompleteness);
				trainingCorrectnessStat.addNumber(trainingCorrectness);

				// calculate test accuracies
				//int correctPosClassified = getCorrectPosClassified(rs, concept, testSetsPos.get(currFold));
				//int correctNegClassified = getCorrectNegClassified(rs, concept, testSetsNeg.get(currFold));
				//int correctExamples = correctPosClassified + correctNegClassified;
				int correctPosClassified = testingPosSize - tmp2.size();	//tmp2: wrong classification of pos. examples
				int correctNegClassified = testingNegSize - tmp3.size();	//tmp3: wrong classification of neg. examples
				int correctExamples = correctPosClassified + correctNegClassified;

				double testingCompleteness = 100*(double)correctPosClassified/testingPosSize;
				double testingCorrectness = 100*(double)correctNegClassified/testingNegSize;

				double currAccuracy = 100*((double)correctExamples/(testingPosSize + testingNegSize));

				accuracy.addNumber(currAccuracy);
				testingCompletenessStat.addNumber(testingCompleteness);
				testingCorrectnessStat.addNumber(testingCorrectness);


				//fortify
				double testingCompletenessFortify2 = 100*(double)(correctPosClassified - errorFixedPosLevel2)/testingPosSize;
				double testingCorrectnessFortify1 = 100*(double)(correctNegClassified + fixedNegLevel1)/testingNegSize;
				double testingCorrectnessFortify2 = 100*(double)(correctNegClassified + fixedNegLevel1 + fixedNegLevel2)/testingNegSize;
				double testingAccuracyFortify1 = 100*((double)(correctExamples + fixedNegLevel1)/(testingPosSize + testingNegSize));
				double testingAccuracyFortify2 = 100*((double)(correctExamples + fixedNegLevel1 + fixedNegLevel2 - errorFixedPosLevel2)/(testingPosSize + testingNegSize));;

				//fortify
				accuracyFortify1Stat.addNumber(testingAccuracyFortify1);
				accuracyFortify2Stat.addNumber(testingAccuracyFortify2);

				correctnessFortify1Stat.addNumber(testingCorrectnessFortify1);
				correctnessFortify2Stat.addNumber(testingCorrectnessFortify2);

				completenessFortify2Stat.addNumber(testingCompletenessFortify2);


				// calculate training F-Score
				int negAsPosTraining = rs.hasType(concept, trainingSetsNeg.get(currFold)).size();
				double precisionTraining = trainingCorrectPosClassified + negAsPosTraining == 0 ? 0 : trainingCorrectPosClassified / (double) (trainingCorrectPosClassified + negAsPosTraining);
				double recallTraining = trainingCorrectPosClassified / (double) trainingSetsPos.get(currFold).size();
				double currFmeasureTraining = 100*Heuristics.getFScore(recallTraining, precisionTraining);
				fMeasureTraining.addNumber(currFmeasureTraining);


				// calculate test F-Score
				int negAsPos = rs.hasType(concept, testSetsNeg.get(currFold)).size();
				double precision = correctPosClassified + negAsPos == 0 ? 0 : correctPosClassified / (double) (correctPosClassified + negAsPos);
				double recall = correctPosClassified / (double) testingPosSize;
				//System.out.println(precision);System.out.println(recall);
				double currFmeasureTest = 100*Heuristics.getFScore(recall, precision);
				fMeasure.addNumber(currFmeasureTest);

				//fortify
				double precision1 = (correctPosClassified + (negAsPos - fixedNegLevel1)) == 0 ?
						0 : correctPosClassified / (double) (correctPosClassified + negAsPos - fixedNegLevel1);;
				double precision2 = ((correctPosClassified - errorFixedPosLevel2) + (negAsPos - fixedNegLevel1 - fixedNegLevel2)) == 0 ?
						0 : correctPosClassified / (double) ((correctPosClassified - errorFixedPosLevel2) + (negAsPos - fixedNegLevel1 - fixedNegLevel2));;
				double recall2 = (correctPosClassified - errorFixedPosLevel2) / (double) testingPosSize;

				double testingFmeasureFortiafy1 = 100*Heuristics.getFScore(recall, precision1);
				double testingFmeasureFortiafy2 = 100*Heuristics.getFScore(recall2, precision2);

				fmeasureFortify1Stat.addNumber(testingFmeasureFortiafy1);
				fmeasureFortify2Stat.addNumber(testingFmeasureFortiafy2);

				length.addNumber(OWLClassExpressionUtils.getLength(concept));
				totalNumberOfDescriptions.addNumber(parcelEx.getTotalNumberOfDescriptionsGenerated());

				outputWriter("Fold " + currFold + "/" + folds + ":");
				outputWriter("  training: " + trainingCorrectPosClassified + "/" + trainingSetsPos.get(currFold).size() +
						" positive and " + trainingCorrectNegClassified + "/" + trainingSetsNeg.get(currFold).size() + " negative examples");
				outputWriter("  testing: " + correctPosClassified + "/" + testSetsPos.get(currFold).size() + " correct positives, "
						+ correctNegClassified + "/" + testSetsNeg.get(currFold).size() + " correct negatives");

				outputWriter("  runtime: " + df.format(algorithmDuration/(double)1000000000) + "s");
				outputWriter("  learning time: " + df.format(learningMili/(double)1000) + "s");
				outputWriter("  definition length: " + df.format(OWLClassExpressionUtils.getLength(concept)));

				outputWriter("  concept: " + concept);

				outputWriter("  f-measure test: " + df.format(currFmeasureTest) +
						"% (" + df.format(currFmeasureTraining) + "% on training set)");

				outputWriter("  f-measure fortify 1: " + df.format(testingFmeasureFortiafy1) +
						"%, fortify 2:" + df.format(testingFmeasureFortiafy2) + "%");


				outputWriter("  accuracy test: " + df.format(currAccuracy) +
						"% (corr:"+ df.format(testingCorrectness) +
						"%, comp:" + df.format(testingCompleteness) + "%) --- " +
						df.format(trainingAccuracy) + "% (corr:"+ trainingCorrectness +
						"%, comp:" + df.format(trainingCompleteness) + "%) on training set");

				outputWriter("  fortified accuracy 1: " + df.format(testingAccuracyFortify1) +
						"%, correctness: " + df.format(testingCorrectnessFortify1) +
						"%, completeness: " + df.format(testingCompleteness) + "%");	//after applying the level 1 fixing, completeness does not change

				outputWriter("  fortified accuracy 2: " + df.format(testingAccuracyFortify2) +
						"%, correctness: " + df.format(testingCorrectnessFortify2) +
						"%, completeness: " + df.format(testingCompletenessFortify2) + "%");

				outputWriter("  avg. fortified training coverage: +level 1: " + df.format(avgFortifyTrainingCoverageLevel1) +
						"%, +level 2: " + df.format(avgFortifyTrainingCoverageLevel2) + "%");

				outputWriter("  avg. fortified test coverage: +level 1: " + df.format(avgFortifyTestCoverageLevel1) +
						"%, +level 2: " + df.format(avgFortifyTestCoverageLevel2) + "%");


//				outputWriter("  total number of descriptions: " + la.getTotalNumberOfDescriptionsGenerated()); // TODO method not available yet

				if (la instanceof ParCELAbstract) {
					int pn = ((ParCELAbstract)la).getNoOfReducedPartialDefinition();
					this.noOfPartialDef.addNumber(pn);
					outputWriter("  number of partial definitions: " + pn + "/" + ((ParCELAbstract)la).getNumberOfPartialDefinitions());

					double pl = OWLClassExpressionUtils.getLength(concept)/(double)pn;
					this.avgPartialDefinitionLength.addNumber(pl);
					outputWriter("  average partial definition length: " + df.format(pl));

					double avgFortifiedPartialDefinitionLength = pl + (addedLength==0 ? 0: (double)addedLength/pn);
					avgFortifiedPartialDefinitionLengthStat.addNumber(avgFortifiedPartialDefinitionLength);

					outputWriter("  average fortified partial definition length: " + df.format(avgFortifiedPartialDefinitionLength));
					outputWriter("  average fortify partial definition length: " + df.format(avgFortifiedPartialDefinitionLength));

					//show more information on counter partial definitions
					if (la instanceof ParCELExAbstract) {
						ParCELExAbstract pdllexla = (ParCELExAbstract)la;
						outputWriter("  number of partial definitions for each type: 1:" + pdllexla.getNumberOfPartialDefinitions(1) +
								"; 2:" + pdllexla.getNumberOfPartialDefinitions(2) +
								"; 3:" + pdllexla.getNumberOfPartialDefinitions(3) +
								"; 4:" + pdllexla.getNumberOfPartialDefinitions(4));
						outputWriter("  number of counter partial definition used: " + pdllexla.getNumberOfCounterPartialDefinitionUsed() + "/" + pdllexla.getNumberOfCounterPartialDefinitions());

						outputWriter("  number of fortify definition: " + (fortifyLevel1Count+fortifyLevel2Count) +
								"(level 1: " + fortifyLevel1Count + ", level 2: " + fortifyLevel2Count + ")");

						//check how did the learner terminate: by partial definition or counter partial definition
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

				//cumulative statistical data
				outputWriter("----------");
				outputWriter("Aggregate data from fold 0 to fold " + currFold + "/" + folds);
				outputWriter("  runtime: " + statOutput(df, runtime, "s"));
				outputWriter("  no of descriptions: " + statOutput(df, totalNumberOfDescriptions, ""));
				outputWriter("  length: " + statOutput(df, length, ""));
				outputWriter("  avg. no of partial definitions: " + statOutput(df, noOfPartialDef, ""));
				outputWriter("  avg. partial definition length: " + statOutput(df, avgPartialDefinitionLength, ""));
				outputWriter("  avg. no of fortify definitions: " + statOutput(df, avgNoOfFortifiedDefinitions, ""));
				outputWriter("      level 1: " + statOutput(df, avgNoOfFortifiedDefinitionsL1, ""));
				outputWriter("      level 2: " + statOutput(df, avgNoOfFortifiedDefinitionsL2, ""));
				outputWriter("  avg. fortified partial definition length: " + statOutput(df, avgFortifiedPartialDefinitionLengthStat, ""));
				outputWriter("  avg. fortify definition length: " + statOutput(df, avgFortifyDefinitionsLengthStat, ""));
				outputWriter("  F-Measure on training set: " + statOutput(df, fMeasureTraining, "%"));
				outputWriter("  F-Measure on test set: " + statOutput(df, fMeasure, "%"));
				outputWriter("  F-Measure fortify 1: " + statOutput(df, fmeasureFortify1Stat, "%"));
				outputWriter("  F-Measure fortify 2: " + statOutput(df, fmeasureFortify2Stat, "%"));
				outputWriter("  predictive accuracy on training set: " + statOutput(df, accuracyTraining, "%") +
						" -- correctness: " + statOutput(df, trainingCorrectnessStat, "%") +
						"-- completeness: " + statOutput(df, trainingCompletenessStat, "%"));
				outputWriter("  predictive accuracy on test set: " + statOutput(df, accuracy, "%") +
						" -- correctness: " + statOutput(df, testingCorrectnessStat, "%") +
						"-- completeness: " + statOutput(df, testingCompletenessStat, "%"));

				outputWriter("  accuracy fortify 1: " + statOutput(df, accuracyFortify1Stat, "%") +
						" -- correctness: " + statOutput(df, correctnessFortify1Stat, "%") +
						"-- completeness: " + statOutput(df, testingCompletenessStat, "%"));

				outputWriter("  accuracy fortify 2: " + statOutput(df, accuracyFortify2Stat, "%") +
						" -- correctness: " + statOutput(df, correctnessFortify2Stat, "%") +
						"-- completeness: " + statOutput(df, completenessFortify2Stat, "%"));

				outputWriter("  coverage fortify 1: + training:" +
						statOutput(df, avgFortifyCoverageTrainingStatLevel1, "%") +
						", +test: " + statOutput(df, avgFortifyCoverageTestStatLevel1, "%")
						);

				outputWriter("  coverage fortify 2: +training:" +
						statOutput(df, avgFortifyCoverageTrainingStatLevel2, "%") +
						", +test: " + statOutput(df, avgFortifyCoverageTestStatLevel2, "%")
						);


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
			outputWriter("  learning time: " + statOutput(df, learningTime, "s"));
			outputWriter("  no of descriptions: " + statOutput(df, totalNumberOfDescriptions, ""));
			outputWriter("  no of partial definitions: " + statOutput(df, noOfPartialDef, ""));
			outputWriter("  avg. partial definition length: " + statOutput(df, avgPartialDefinitionLength, ""));
			outputWriter("  avg. no of fortify definitions: " + statOutput(df, avgNoOfFortifiedDefinitions, ""));
			outputWriter("      level 1: " + statOutput(df, avgNoOfFortifiedDefinitionsL1, ""));
			outputWriter("      level 2: " + statOutput(df, avgNoOfFortifiedDefinitionsL2, ""));
			outputWriter("  definition length: " + statOutput(df, length, ""));
			outputWriter("  avg. fortified partial definition length: " + statOutput(df, avgFortifiedPartialDefinitionLengthStat, ""));
			outputWriter("  avg. fortify partial definition length: " + statOutput(df, avgFortifyDefinitionsLengthStat, ""));
			outputWriter("  no of counter partial definition: " + statOutput(df, noOfCounterPartialDefinitions, ""));
			outputWriter("  no of counter partial definition used: " + statOutput(df, noOfCounterPartialDefinitionsUsed, ""));
			outputWriter("  F-Measure on training set: " + statOutput(df, fMeasureTraining, "%"));
			outputWriter("  F-Measure on test set: " + statOutput(df, fMeasure, "%"));
			outputWriter("  F-Measure fortify 1: " + statOutput(df, fmeasureFortify1Stat, "%"));
			outputWriter("  F-Measure fortify 2: " + statOutput(df, fmeasureFortify2Stat, "%"));
			outputWriter("  predictive accuracy on training set: " + statOutput(df, accuracyTraining, "%") +
					" - corr: " + statOutput(df, trainingCorrectnessStat, "%") +
					", comp: " + statOutput(df, trainingCompletenessStat, "%"));
			outputWriter("  predictive accuracy on test set: " + statOutput(df, accuracy, "%") +
					" - corr: " + statOutput(df, testingCorrectnessStat, "%") +
					", comp: " + statOutput(df, testingCompletenessStat, "%"));

			outputWriter("  accuracy fortify 1: " + statOutput(df, accuracyFortify1Stat, "%") +
					" -- correctness: " + statOutput(df, correctnessFortify1Stat, "%") +
					"-- completeness: " + statOutput(df, testingCompletenessStat, "%"));

			outputWriter("  accuracy fortify 2: " + statOutput(df, accuracyFortify2Stat, "%") +
					" -- correctness: " + statOutput(df, correctnessFortify2Stat, "%") +
					"-- completeness: " + statOutput(df, completenessFortify2Stat, "%"));


			if (la instanceof ParCELExAbstract)
				outputWriter("  terminated by: partial def.: " + terminatedBypartialDefinition + "; counter partial def.: " + terminatedByCounterPartialDefinitions);


			//this is for copying to word document
			//f-measure, accuracy, correctness, completeness, avg pdef length, no of pdef, time, no of des, no of cpdef
			outputWriter("***without fortify (f-measure, accuracy, correctness, completeness, avg. pdef length)***\n"
					+ df.format(fMeasure.getMean()) + "\n" + df.format(fMeasure.getStandardDeviation()) + "\n"
					+ df.format(accuracy.getMean()) + "\n" + df.format(accuracy.getStandardDeviation()) + "\n"
					+ df.format(testingCorrectnessStat.getMean()) + "\n" + df.format(testingCorrectnessStat.getStandardDeviation()) + "\n"
					+ df.format(testingCompletenessStat.getMean()) + "\n" + df.format(testingCompletenessStat.getStandardDeviation()) + "\n"
					+ df.format(avgPartialDefinitionLength.getMean()) + "\n" + df.format(avgPartialDefinitionLength.getStandardDeviation()) + "\n"
					);


			outputWriter("***with fortify 1 (f-measure, accuracy, correctness, completeness, fortified pdef length)***\n"
					+ df.format(fmeasureFortify1Stat.getMean()) + "\n" + df.format(fmeasureFortify1Stat.getStandardDeviation()) + "\n"
					+ df.format(accuracyFortify1Stat.getMean()) + "\n" + df.format(accuracyFortify1Stat.getStandardDeviation()) + "\n"
					+ df.format(correctnessFortify1Stat.getMean()) + "\n" + df.format(correctnessFortify1Stat.getStandardDeviation()) + "\n"
					+ df.format(testingCompletenessStat.getMean()) + "\n" + df.format(testingCompletenessStat.getStandardDeviation()) + "\n"
					+ df.format(avgFortifiedPartialDefinitionLengthStat.getMean()) + "\n" + df.format(avgFortifiedPartialDefinitionLengthStat.getStandardDeviation()) + "\n"
					);

			outputWriter("***Common dimensionss (no of pdef., learning time, no of des., no of cpdef., no of fdef.)***\n"
					+ df.format(noOfPartialDef.getMean()) + "\n" + df.format(noOfPartialDef.getStandardDeviation()) + "\n"
					+ df.format(learningTime.getMean()) + "\n" + df.format(learningTime.getStandardDeviation()) + "\n"
					+ df.format(totalNumberOfDescriptions.getMean()) + "\n" + df.format(totalNumberOfDescriptions.getStandardDeviation()) + "\n"
					+ df.format(noOfCounterPartialDefinitions.getMean()) + "\n" + df.format(noOfCounterPartialDefinitions.getStandardDeviation()) + "\n"
					+ df.format(avgNoOfFortifiedDefinitions.getMean()) + "\n" + df.format(avgNoOfFortifiedDefinitions.getStandardDeviation()) + "\n"
					);


			outputWriter("***with fortify 2 (f-measure, accuracy, correctness, completeness, avg. fortified pdef length)***\n"
					+ df.format(fmeasureFortify2Stat.getMean()) + "\n" + df.format(fmeasureFortify1Stat.getStandardDeviation()) + "\n"
					+ df.format(accuracyFortify2Stat.getMean()) + "\n" + df.format(accuracyFortify1Stat.getStandardDeviation()) + "\n"
					+ df.format(correctnessFortify2Stat.getMean()) + "\n" + df.format(correctnessFortify1Stat.getStandardDeviation()) + "\n"
					+ df.format(completenessFortify2Stat.getMean()) + "\n" + df.format(completenessFortify2Stat.getStandardDeviation()) + "\n"
					+ df.format(avgFortifiedPartialDefinitionLengthStat.getMean()) + "\n" + df.format(avgFortifiedPartialDefinitionLengthStat.getStandardDeviation()) + "\n"
					);


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
				noOfPartialDefAvg.addNumber(noOfPartialDef.getMean());
				noOfPartialDefMax.addNumber(noOfPartialDef.getMax());
				noOfPartialDefMin.addNumber(noOfPartialDef.getMin());
				noOfPartialDefDev.addNumber(noOfPartialDef.getStandardDeviation());

				//avg partial definition length
				avgPartialDefLenAvg.addNumber(avgPartialDefinitionLength.getMean());
				avgPartialDefLenMax.addNumber(avgPartialDefinitionLength.getMax());
				avgPartialDefLenMin.addNumber(avgPartialDefinitionLength.getMin());
				avgPartialDefLenDev.addNumber(avgPartialDefinitionLength.getStandardDeviation());

				avgFortifiedPartialDefLenAvg.addNumber(avgFortifiedPartialDefinitionLengthStat.getMean());
				avgFortifiedPartialDefLenMax.addNumber(avgFortifiedPartialDefinitionLengthStat.getMax());
				avgFortifiedPartialDefLenMin.addNumber(avgFortifiedPartialDefinitionLengthStat.getMin());
				avgFortifiedPartialDefLenDev.addNumber(avgFortifiedPartialDefinitionLengthStat.getStandardDeviation());


				defLenAvg.addNumber(length.getMean());
				defLenMax.addNumber(length.getMax());
				defLenMin.addNumber(length.getMin());
				defLenDev.addNumber(length.getStandardDeviation());

				//counter partial definitions
				noOfCounterPartialDefinitionsAvg.addNumber(noOfCounterPartialDefinitions.getMean());
				noOfCounterPartialDefinitionsDev.addNumber(noOfCounterPartialDefinitions.getStandardDeviation());
				noOfCounterPartialDefinitionsMax.addNumber(noOfCounterPartialDefinitions.getMax());
				noOfCounterPartialDefinitionsMin.addNumber(noOfCounterPartialDefinitions.getMin());

				noOfCounterPartialDefinitionsUsedAvg.addNumber(noOfCounterPartialDefinitionsUsed.getMean());
				noOfCounterPartialDefinitionsUsedDev.addNumber(noOfCounterPartialDefinitionsUsed.getStandardDeviation());
				noOfCounterPartialDefinitionsUsedMax.addNumber(noOfCounterPartialDefinitionsUsed.getMax());
				noOfCounterPartialDefinitionsUsedMin.addNumber(noOfCounterPartialDefinitionsUsed.getMin());

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
				fortify1AccAvg.addNumber(accuracyFortify1Stat.getMean());
				fortify1AccMax.addNumber(accuracyFortify1Stat.getMax());
				fortify1AccMin.addNumber(accuracyFortify1Stat.getMin());
				fortify1AccDev.addNumber(accuracyFortify1Stat.getStandardDeviation());

				fortify2AccAvg.addNumber(accuracyFortify2Stat.getMean());
				fortify2AccMax.addNumber(accuracyFortify2Stat.getMax());
				fortify2AccMin.addNumber(accuracyFortify2Stat.getMin());
				fortify2AccDev.addNumber(accuracyFortify2Stat.getStandardDeviation());


				testingCorAvg.addNumber(testingCorrectnessStat.getMean());
				testingCorDev.addNumber(testingCorrectnessStat.getStandardDeviation());
				testingCorMax.addNumber(testingCorrectnessStat.getMax());
				testingCorMin.addNumber(testingCorrectnessStat.getMin());

				//fortify correctness
				fortify1CorAvg.addNumber(correctnessFortify1Stat.getMean());
				fortify1CorMax.addNumber(correctnessFortify1Stat.getMax());
				fortify1CorMin.addNumber(correctnessFortify1Stat.getMin());
				fortify1CorDev.addNumber(correctnessFortify1Stat.getStandardDeviation());

				fortify2CorAvg.addNumber(correctnessFortify2Stat.getMean());
				fortify2CorMax.addNumber(correctnessFortify2Stat.getMax());
				fortify2CorMin.addNumber(correctnessFortify2Stat.getMin());
				fortify2CorDev.addNumber(correctnessFortify2Stat.getStandardDeviation());


				testingComAvg.addNumber(testingCompletenessStat.getMean());
				testingComDev.addNumber(testingCompletenessStat.getStandardDeviation());
				testingComMax.addNumber(testingCompletenessStat.getMax());
				testingComMin.addNumber(testingCompletenessStat.getMin());

				//fortify completeness (level 1 fixing does not change the completeness
				fortify2ComAvg.addNumber(completenessFortify2Stat.getMean());
				fortify2ComMax.addNumber(completenessFortify2Stat.getMax());
				fortify2ComMin.addNumber(completenessFortify2Stat.getMin());
				fortify2ComDev.addNumber(completenessFortify2Stat.getStandardDeviation());


				testingFMeasureAvg.addNumber(fMeasure.getMean());
				testingFMeasureDev.addNumber(fMeasure.getStandardDeviation());
				testingFMeasureMax.addNumber(fMeasure.getMax());
				testingFMeasureMin.addNumber(fMeasure.getMin());

				trainingFMeasureAvg.addNumber(fMeasureTraining.getMean());
				trainingFMeasureDev.addNumber(fMeasureTraining.getStandardDeviation());
				trainingFMeasureMax.addNumber(fMeasureTraining.getMax());
				trainingFMeasureMin.addNumber(fMeasureTraining.getMin());

				fortify1FmeasureAvg.addNumber(fmeasureFortify1Stat.getMean());
				fortify1FmeasureMax.addNumber(fmeasureFortify1Stat.getMax());
				fortify1FmeasureMin.addNumber(fmeasureFortify1Stat.getMin());
				fortify1FmeasureDev.addNumber(fmeasureFortify1Stat.getStandardDeviation());

				fortify2FmeasureAvg.addNumber(fmeasureFortify2Stat.getMean());
				fortify2FmeasureMax.addNumber(fmeasureFortify2Stat.getMax());
				fortify2FmeasureMin.addNumber(fmeasureFortify2Stat.getMin());
				fortify2FmeasureDev.addNumber(fmeasureFortify2Stat.getStandardDeviation());


				noOfDescriptionsAgv.addNumber(totalNumberOfDescriptions.getMean());
				noOfDescriptionsMax.addNumber(totalNumberOfDescriptions.getMax());
				noOfDescriptionsMin.addNumber(totalNumberOfDescriptions.getMin());
				noOfDescriptionsDev.addNumber(totalNumberOfDescriptions.getStandardDeviation());
			}

		}	//for kk folds

		if (noOfRuns > 1) {

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


			//this is for copying to word document
			//f-measure, accuracy, correctness, completeness, avg pdef length, no of pdef, time, no of des, no of cpdef
			outputWriter("***without fortify (f-measure, accuracy, correctness, completeness, avg. pdef length)***\n"
					+ df.format(testingFMeasureAvg.getMean()) + "\n" + df.format(testingFMeasureDev.getMean())+ "\n"
					+ df.format(testingAccAvg.getMean()) + "\n" + df.format(testingAccDev.getMean()) + "\n"
					+ df.format(testingCorAvg.getMean()) + "\n" + df.format(testingCorDev.getMean()) + "\n"
					+ df.format(testingComAvg.getMean()) + "\n" + df.format(testingComDev.getMean()) + "\n"
					+ df.format(avgPartialDefLenAvg.getMean()) + "\n" + df.format(avgPartialDefLenDev.getMean()) + "\n");


			outputWriter("***with fortify 1 (f-measure, accuracy, correctness, completeness)***\n"
					+ df.format(fortify1FmeasureAvg.getMean()) + "\n" + df.format(fortify1FmeasureDev.getMean()) + "\n"
					+ df.format(fortify1AccAvg.getMean()) + "\n" + df.format(fortify1AccDev.getMean()) + "\n"
					+ df.format(fortify1CorAvg.getMean()) + "\n" + df.format(fortify1CorDev.getMean()) + "\n"
					+ df.format(testingComAvg.getMean()) + "\n" + df.format(testingComDev.getMean()) + "\n"
					+ df.format(avgFortifiedPartialDefLenAvg.getMean()) + "\n" + df.format(avgFortifiedPartialDefLenDev.getMean()) + "\n");

			outputWriter("***Common dimensions (no of pdef., learning time, no of des., no of cpdef., avg. no of fdef.)***\n"
					+ df.format(noOfPartialDefAvg.getMean()) + "\n" + df.format(noOfPartialDefDev.getMean()) + "\n"
					+ df.format(learningTime.getMean()) + "\n" + df.format(learningTime.getStandardDeviation()) + "\n"
					+ df.format(noOfDescriptionsAgv.getMean()) + "\n" + df.format(noOfDescriptionsDev.getMean()) + "\n"
					+ df.format(noOfCounterPartialDefinitionsAvg.getMean()) + "\n" + df.format(noOfCounterPartialDefinitionsDev.getMean()) + "\n"
					+ df.format(avgNoOfFortifiedDefinitions.getMean()) + "\n" + df.format(avgNoOfFortifiedDefinitions.getStandardDeviation()) + "\n"
					);


			outputWriter("***with fortify 2 (f-measure, accuracy, correctness, completeness, avg. fortified pdef length)***\n"
					+ df.format(fortify2FmeasureAvg.getMean()) + "\n" + df.format(fortify2FmeasureDev.getMean()) + "\n"
					+ df.format(fortify2AccAvg.getMean()) + "\n" + df.format(fortify2AccDev.getMean()) + "\n"
					+ df.format(fortify2CorAvg.getMean()) + "\n" + df.format(fortify2CorDev.getMean()) + "\n"
					+ df.format(fortify2ComAvg.getMean()) + "\n" + df.format(fortify2ComDev.getMean()) + "\n"
					+ df.format(avgFortifiedPartialDefLenAvg.getMean()) + "\n" + df.format(avgFortifiedPartialDefLenDev.getMean()) + "\n");
		}

		if (la instanceof ParCELExAbstract)
			outputWriter("terminated by: partial def.: " + terminatedBypartialDefinition + "; counter partial def.: " + terminatedByCounterPartialDefinitions);


		//reset the set of positive and negative examples for the learning problem for further experiment if any
		lp.setPositiveExamples(posExamples);
		lp.setNegativeExamples(negExamples);


	}	//constructor


	/*
	private String getOrderUnit(int order) {
		switch (order) {
			case 1: return "st";
			case 2: return "nd";
			case 3: return "rd";
			default: return "th";
		}
	}
	*/

	@Override
	protected void outputWriter(String output) {
		logger.info(output);

		if (writeToFile)
			Files.appendToFile(outputFile, output + "\n");
	}
}
