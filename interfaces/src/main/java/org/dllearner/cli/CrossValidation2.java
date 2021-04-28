package org.dllearner.cli;
import com.google.common.collect.Sets;
import org.dllearner.algorithms.decisiontrees.dsttdt.DSTTDTClassifier;
import org.dllearner.algorithms.decisiontrees.tdt.TDTClassifier;
import org.dllearner.core.*;
import org.dllearner.learningproblems.Heuristics;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.learningproblems.PosNegUndLP;
import org.dllearner.learningproblems.PosOnlyLP;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.owl.ManchesterOWLSyntaxOWLObjectRendererImplExt;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;
import org.dllearner.utilities.statistics.Stat;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CrossValidation2  extends org.dllearner.cli.CrossValidation{

	DecimalFormat df = new DecimalFormat();
	protected Stat commissionTraining = new Stat(); // commission, omission and induction rate for
	protected Stat commission = new Stat();
	protected Stat omissionTraining = new Stat();
	protected Stat omission = new Stat();
	protected Stat inductionTraining = new Stat();
	protected Stat induction = new Stat();

	public CrossValidation2() {
		super(); //superclass constuctor
	}

	public CrossValidation2(AbstractCELA la, AbstractClassExpressionLearningProblem lp, final AbstractReasonerComponent rs, int folds, boolean leaveOneOut) {
		super();
		ManchesterOWLSyntaxOWLObjectRendererImplExt renderer = new ManchesterOWLSyntaxOWLObjectRendererImplExt();
		StringRenderer.setRenderer(renderer);
		StringRenderer.setShortFormProvider(new SimpleShortFormProvider());

		// the training and test sets used later on
		List<Set<OWLIndividual>> trainingSetsPos = new LinkedList<>();
		List<Set<OWLIndividual>> trainingSetsNeg = new LinkedList<>();
		List<Set<OWLIndividual>> trainingSetsUnd = new LinkedList<>();
		List<Set<OWLIndividual>> testSetsPos = new LinkedList<>();
		List<Set<OWLIndividual>> testSetsNeg = new LinkedList<>();
		List<Set<OWLIndividual>> testSetsUnd = new LinkedList<>();
		// get examples and shuffle them too
		Set<OWLIndividual> posExamples;
		Set<OWLIndividual> negExamples;
		Set<OWLIndividual> undExamples; // initialization when other learning problems are considered
		if(lp instanceof PosNegLP){
			posExamples = ((PosNegLP)lp).getPositiveExamples();
			negExamples = ((PosNegLP)lp).getNegativeExamples();
			undExamples= new TreeSet<>();

			if(lp instanceof PosNegUndLP){

				undExamples=((PosNegUndLP)lp).getUncertainExamples();
			}
		} else if(lp instanceof PosOnlyLP){
			posExamples = ((PosOnlyLP)lp).getPositiveExamples();
			negExamples = new HashSet<>();
			undExamples= new TreeSet<>();
		} else {
			throw new IllegalArgumentException("Only PosNeg and PosOnly learning problems are supported");
		}
		List<OWLIndividual> posExamplesList = new LinkedList<>(posExamples);
		List<OWLIndividual> negExamplesList = new LinkedList<>(negExamples);
		List<OWLIndividual> undExamplesList = new LinkedList<>(undExamples);
		//System.out.println("Undefined membership: "+undExamples);
		Collections.shuffle(posExamplesList, new Random(1));
		Collections.shuffle(negExamplesList, new Random(2));
		if(lp instanceof PosNegUndLP){
			Collections.shuffle(undExamplesList, new Random(3));

		}

		// sanity check whether nr. of folds makes sense for this benchmark
		//		if(!leaveOneOut && (posExamples.size()<folds && negExamples.size()<folds && undExamples.size()<folds)) {
		//			System.out.println("The number of folds is higher than the number of "
		//					+ "positive/negative examples. This can result in empty test sets. Exiting.");
		//			System.exit(0);
		//		}
		//removed in order to support also imbalanced distributions of data

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
			int[] splitsUnd = calculateSplits(undExamples.size(),folds);

			//			System.out.println("<"+posExamples.size());
			//			System.out.println("<"+negExamples.size());
			//			System.out.println("<"+undExamples.size());
			//			System.out.println("---"+splitsPos[0]);
			//			System.out.println("---"+splitsNeg[0]);
			//			System.out.println("---"+splitsUnd[0]);
			// calculating training and test sets
			for(int i=0; i<folds; i++) {
				Set<OWLIndividual> testPos = getTestingSet(posExamplesList, splitsPos, i);
				Set<OWLIndividual> testNeg = getTestingSet(negExamplesList, splitsNeg, i);
				Set<OWLIndividual> testUnd = getTestingSet(undExamplesList, splitsUnd, i);
				testSetsPos.add(i, testPos);
				testSetsNeg.add(i, testNeg);
				testSetsUnd.add(i,testUnd);
				trainingSetsPos.add(i, getTrainingSet(posExamples, testPos));
				trainingSetsNeg.add(i, getTrainingSet(negExamples, testNeg));
				trainingSetsUnd.add(i, getTrainingSet(undExamples, testUnd));
			}

			//System.out.println("Test set size: "+testSetsPos.size());
		}

		// run the algorithm
		if( multiThreaded && lp instanceof Cloneable && la instanceof Cloneable){
			ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()-1);
			for(int currFold=0; currFold<folds; currFold++) {
				try {
					final AbstractClassExpressionLearningProblem lpClone = (AbstractClassExpressionLearningProblem) lp.getClass().getMethod("clone").invoke(lp);
					final Set<OWLIndividual> trainPos = trainingSetsPos.get(currFold);
					final Set<OWLIndividual> trainNeg = trainingSetsNeg.get(currFold);

					final Set<OWLIndividual> testPos = testSetsPos.get(currFold);
					final Set<OWLIndividual> testNeg = testSetsNeg.get(currFold);
					final Set<OWLIndividual> trainUnd = trainingSetsUnd.get(currFold);
					final Set<OWLIndividual> testUnd = testSetsUnd.get(currFold);

					if(lp instanceof PosNegLP){
						((PosNegLP)lpClone).setPositiveExamples(trainPos);
						((PosNegLP)lpClone).setNegativeExamples(trainNeg);
						if (lp instanceof PosNegUndLP){
							((PosNegUndLP)lpClone).setUncertainExamples(trainUnd);
						}
					} else if(lp instanceof PosOnlyLP){
						((PosOnlyLP)lpClone).setPositiveExamples(new TreeSet<>(trainPos));
					}
					final AbstractCELA laClone = (AbstractCELA) la.getClass().getMethod("clone").invoke(la);
					final int i = currFold;

					es.submit(new Runnable() {

						@Override
						public void run() {
							try {
								if(lpClone instanceof PosNegUndLP)
									validate(laClone, lpClone, rs, i, trainPos, trainNeg, trainUnd, testPos, testNeg, testUnd);
								else
									validate(laClone, lpClone, rs, i, trainPos, trainNeg, null, testPos, testNeg, null);
							} catch (Exception e) {
								e.printStackTrace();
							}
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
		}
		else {
			for(int currFold=0; currFold<folds; currFold++) {
				final Set<OWLIndividual> trainPos = trainingSetsPos.get(currFold);
				final Set<OWLIndividual> trainNeg = trainingSetsNeg.get(currFold);
				final Set<OWLIndividual> trainUnd = trainingSetsUnd.get(currFold);
				final Set<OWLIndividual> testPos = testSetsPos.get(currFold);
				final Set<OWLIndividual> testNeg = testSetsNeg.get(currFold);
				final Set<OWLIndividual> testUnd = testSetsUnd.get(currFold);
				//				System.out.println("testUnd size: "+ trainUnd);
				//				System.exit(0);

				if(lp instanceof PosNegLP){
					((PosNegLP)lp).setPositiveExamples(trainPos);
					((PosNegLP)lp).setNegativeExamples(trainNeg);
					if(lp instanceof PosNegUndLP){
						((PosNegUndLP)lp).setUncertainExamples(trainUnd);
					}
				} else if(lp instanceof PosOnlyLP){
					((PosOnlyLP)lp).setPositiveExamples(new TreeSet<>(trainPos));
				}

				//System.out.println("Training set negative"+trainNeg.size());
				//System.out.println("Training set unlabeles"+trainUnd.size());
				validate(la, lp, rs, currFold, trainPos, trainNeg, trainUnd, testPos, testNeg, testUnd);
			}
		}

		outputWriter("");
		outputWriter("Finished " + folds + "-folds cross-validation.");
		outputWriter("runtime: " + statOutput(df, runtime, "s"));
		outputWriter("length: " + statOutput(df, length, ""));
		outputWriter("F-Measure on training set: " + statOutput(df, fMeasureTraining, "%"));
		outputWriter("F-Measure: " + statOutput(df, fMeasure, "%"));
		outputWriter("Match rate on training set: " + statOutput(df, accuracyTraining, "%"));
		outputWriter("Match rate: " + statOutput(df, accuracy, "%"));
		outputWriter("Commission rate: " + statOutput(df, commission, "%"));
		outputWriter("Omission rate: " + statOutput(df, omission, "%"));
		outputWriter("Induction rate: "+statOutput(df, induction, "%"));
	}

	private void validate(AbstractCELA la, AbstractClassExpressionLearningProblem lp, AbstractReasonerComponent rs,
			int currFold, Set<OWLIndividual> trainPos, Set<OWLIndividual> trainNeg,Set<OWLIndividual> trainUnd, Set<OWLIndividual> testPos, Set<OWLIndividual> testNeg, Set<OWLIndividual> testUnd){
		//System.out.println("Validation starting");
		Set<String> pos = Helper.getStringSet(trainPos);
		Set<String> neg = Helper.getStringSet(trainNeg);
		Set<String> und = Helper.getStringSet(trainUnd);
		String output = "";
		TreeSet<String> treeSetPos = new TreeSet<>(pos);
		output += "+" + treeSetPos + "\n";
		TreeSet<String> treeSetNeg = new TreeSet<>(neg);
		output += "-" + treeSetNeg + "\n";
		TreeSet<String> treeSetUnd = new TreeSet<>(und);
		output += "?" + treeSetUnd + "\n";
		//System.out.printf("Learning algoritm preparation: %d %d %d \n", treeSetPos.size(),treeSetNeg.size(),treeSetUnd.size());
		try {
			lp.init();
			la.setLearningProblem(lp);
			la.init();
			//System.out.println("setting learning problem");
		} catch (ComponentInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		long algorithmStartTime = System.nanoTime();
		la.start();
		long algorithmDuration = System.nanoTime() - algorithmStartTime;
		runtime.addNumber(algorithmDuration/(double)1000000000);

		int trainingCorrectPosClassified,trainingCorrectNegClassified,trainingCorrectUndClassified,trainingCorrectExamples;
		int trainingSize;
		double trainingAccuracy;
		int negAsPosTraining;
		int posAsNegTraining;

		int undAsPosTraining;
		int undAsNegTraining;

		int posAsUndTraining; //omission cases
		int negAsUndTraining;

		int commissions;

		double trainingCommission;
		int inductions;

		double trainingInduction;

		int omissions ;

		int negAsPos;

		int posAsNeg; // commission cases
		int undAsPos;
		int undAsNeg;  //induction cases

		int posAsUnd; //omission cases
		int negAsUnd;

		double currCommission;
		double currInduction;
		double currOmission;

		int correctPosClassified ;//getCorrectPosClassified(rs, concept, testPos);
		int correctNegClassified ;
		int correctUndClassified ;
		int correctExamples;

		double trainingOmission;

		double currAccuracy;

		OWLClassExpression concept = la.getCurrentlyBestDescription();
		int testsize = testPos.size()+
				testNeg.size()+testUnd.size();
		if(!(la instanceof DSTTDTClassifier)&& !(la instanceof TDTClassifier)){
			//System.out.println("Training  completed");
			// extract the current concept description
			//System.out.println("Training  completed"+ concept);

			Set<OWLIndividual> tmp = rs.hasType(concept, testPos);
			Set<OWLIndividual> tmp2 = Sets.difference(testPos, tmp);
			Set<OWLIndividual> tmp3 = rs.hasType(concept, testNeg);

			// calculate training accuracies

			trainingCorrectPosClassified = getCorrectPosClassified(rs, concept, trainPos);
			trainingCorrectNegClassified = getCorrectNegClassified(rs, concept, trainNeg);
			trainingCorrectUndClassified = getCorrectUndClassified(rs, concept, trainUnd);
			trainingCorrectExamples = trainingCorrectUndClassified+trainingCorrectPosClassified + trainingCorrectNegClassified;
			trainingSize = trainPos.size()+
					trainNeg.size()+trainUnd.size();
			trainingAccuracy = 100*((double)trainingCorrectExamples/trainingSize);

			//System.out.println("Training Correct Examples: "+ trainingCorrectExamples+ " Size: "+trainingSize);
			accuracyTraining.addNumber(trainingAccuracy); //in a ternary setting this is the match rate

			//compute training match (accuracy), commission omission and induction
			OWLDataFactory factory= new OWLDataFactoryImpl(); //get a data factory for derive the complement concept description
			negAsPosTraining = rs.hasType(concept, trainNeg).size();
			posAsNegTraining= rs.hasType(factory.getOWLObjectComplementOf(concept), trainPos).size(); // commission cases

			undAsPosTraining = rs.hasType(concept, trainUnd).size();
			undAsNegTraining= rs.hasType(factory.getOWLObjectComplementOf(concept), trainUnd).size();  //induction cases

			posAsUndTraining= trainPos.size()-trainingCorrectPosClassified - posAsNegTraining; //omission cases
			negAsUndTraining= trainNeg.size()-trainingCorrectNegClassified - negAsPosTraining;

			commissions = negAsPosTraining+posAsNegTraining;
			//System.out.println("Training commissions: "+ commissions);
			trainingCommission=100*((double)commissions/trainingSize);
			inductions = undAsPosTraining+undAsNegTraining;

			trainingInduction=100*((double)inductions/trainingSize);
			//System.out.println("Training inductions: "+ trainingInduction);
			omissions = posAsUndTraining+negAsUndTraining;

			trainingOmission=100*((double)omissions/trainingSize);
			//System.out.println("Training omissions: "+ trainingOmission);

			commissionTraining.addNumber(trainingCommission);
			inductionTraining.addNumber(trainingInduction);
			omissionTraining.addNumber(trainingOmission);

			// calculate test accuracies
			correctPosClassified = getCorrectPosClassified(rs, concept, testPos);
			correctNegClassified = getCorrectNegClassified(rs, concept, testNeg);
			correctUndClassified = getCorrectUndClassified(rs, concept, testUnd);
			correctExamples = correctUndClassified+correctPosClassified + correctNegClassified;

			currAccuracy = 100*((double)correctExamples/testsize);
			accuracy.addNumber(currAccuracy);

			// commission omission and induction on the test set
			negAsPos = rs.hasType(concept, testNeg).size();

			posAsNeg= rs.hasType(factory.getOWLObjectComplementOf(concept), testPos).size(); // commission cases
			undAsPos = rs.hasType(concept, testUnd).size();
			undAsNeg= rs.hasType(factory.getOWLObjectComplementOf(concept), testUnd).size();  //induction cases

			posAsUnd= testPos.size()-correctPosClassified - posAsNeg; //omission cases
			negAsUnd= testNeg.size()-correctNegClassified - negAsPos;

			currCommission=100*((double)(negAsPos+posAsNeg)/testsize);
			currInduction=100*((double)(undAsPos+undAsNeg)/testsize);
			currOmission=100*((double)(posAsUnd+negAsUnd)/testsize);

			commission.addNumber(currCommission);
			omission.addNumber(currOmission);
			induction.addNumber(currInduction);
			// calculate training F-Score

			double precisionTraining = trainingCorrectPosClassified + negAsPosTraining == 0 ? 0 : trainingCorrectPosClassified / (double) (trainingCorrectPosClassified + negAsPosTraining);
			double recallTraining = trainingCorrectPosClassified / (double) trainPos.size();
			System.out.println(precisionTraining +"----"+recallTraining);
			//			System.exit(1);
			fMeasureTraining.addNumber(100*Heuristics.getFScore(recallTraining, precisionTraining));

			// calculate test F-Score
			double precision = correctPosClassified + negAsPos == 0 ? 0 : correctPosClassified / (double) (correctPosClassified + negAsPos);
			double recall = correctPosClassified / (double) testPos.size();
			//		System.out.println(precision);System.out.println(recall);
			fMeasure.addNumber(100*Heuristics.getFScore(recall, precision));
			length.addNumber(OWLClassExpressionUtils.getLength(concept));
			output += "test set errors pos: " + tmp2 + "\n";
			output += "test set errors neg: " + tmp3 + "\n";

		}else{

			//if (la instanceof TDTClassifier)
			//DSTTDTClassifier tdt= la
			//			DSTTDTClassifier tdt= (DSTTDTClassifier) la;

			System.out.println("Training");
			trainingCorrectPosClassified = getCorrectClassifications(1, trainPos, la);

			trainingCorrectNegClassified = getCorrectClassifications(-1, trainNeg, la);
			trainingCorrectUndClassified = getCorrectClassifications(0, trainUnd, la);
			trainingCorrectExamples = trainingCorrectUndClassified+trainingCorrectPosClassified + trainingCorrectNegClassified;
			trainingSize = trainPos.size()+
					trainNeg.size()+trainUnd.size();
			trainingAccuracy = 100*((double)trainingCorrectExamples/trainingSize);

			accuracyTraining.addNumber(trainingAccuracy);
			negAsPosTraining =   getWrongClassification(trainNeg, la);

			posAsNegTraining=    getWrongClassificationNeg (trainPos, la);   ///rs.hasType(factory.getOWLObjectComplementOf(concept), testPos).size(); // commission cases

			posAsUndTraining= getOmittedClassification(1, trainPos, la); //omission cases

			negAsUndTraining= getOmittedClassification(-1, trainNeg, la);

			//System.out.println();
			//System.out.println("Training "+posAsUndTraining+ "-----------"+ negAsUndTraining);
			undAsPosTraining = getInductionClassification(trainUnd, la);  //positive and negative induction
			undAsNegTraining=  0; //getInductionClassified(-1,0,trainUnd, tdt);

			//System.out.println("Training");
			commissions = negAsPosTraining+posAsNegTraining;
			//System.out.println("Training commissions: "+ commissions);
			trainingCommission=100*((double)commissions/trainingSize);
			inductions = undAsPosTraining+undAsNegTraining;

			trainingInduction=100*((double)inductions/trainingSize);
			//System.out.println("Training inductions: "+ trainingInduction);
			omissions = posAsUndTraining+negAsUndTraining;

			trainingOmission=100*((double)omissions/trainingSize);
			//System.out.println("Training omissions: "+ trainingOmission);

			commissionTraining.addNumber(trainingCommission);
			inductionTraining.addNumber(trainingInduction);
			omissionTraining.addNumber(trainingOmission);

			correctPosClassified = getCorrectClassifications(1, testPos, la); //getCorrectPosClassified(rs, concept, testPos);
			correctNegClassified = getCorrectClassifications(-1, testNeg, la);
			correctUndClassified = getCorrectClassifications(0, testUnd, la);
			correctExamples = correctUndClassified+correctPosClassified + correctNegClassified;

			//			 System.out.println("Correct p:"+ correctPosClassified +"n: "+ correctNegClassified+ " u: "+ correctUndClassified);
			//			 System.out.println("        p:"+ testPos.size() +"n: "+ testNeg.size()+ " u: "+ testUnd.size());
			//			 System.out.println("Correct examples:"+ correctExamples +"test size: "+ testsize);
			currAccuracy = 100*((double)correctExamples/testsize);
			accuracy.addNumber(currAccuracy);

			// commission omission and induction on the test set
			negAsPos = getWrongClassification(testNeg, la);
			posAsNeg= getWrongClassificationNeg( testPos, la); // commission cases

			undAsPos = getInductionClassification(testUnd, la);//rs.hasType(concept, testUnd).size();
			//System.out.println("trainUnd size"+ trainUnd.size());
			undAsNeg=  0;   // in order to avoid variable elimination. To be rewritten
			//getInductionClassified(0,trainUnd, tdt);  //induction cases

			posAsUnd= getOmittedClassification(1, testPos, la); //omission cases
			negAsUnd= getOmittedClassification(-1, testNeg, la);
			// System.out.println("Omissions"+(posAsUnd+negAsUnd));
//			System.out.println("Test:  Omissions:"+ (posAsUnd+ negAsUnd)+ "/"+ testsize);
//			System.out.println("Match:"+ (correctPosClassified+correctNegClassified+correctUndClassified)+ "/"+ testsize);
//			System.out.println("       Commissions: "+(negAsPos+posAsNeg)+ "/"+ testsize);
//		    System.out.println("       Induction: "+(undAsPos+undAsNeg)+ "/"+ testsize);
			currCommission=100*(((double)(negAsPos+posAsNeg))/testsize);
			currInduction=100*(((double)(undAsPos+undAsNeg))/testsize);
			currOmission=100*(((double)(posAsUnd+negAsUnd))/testsize);
			//			System.out.println( "C: "+ currAccuracy);
			//			System.out.println( "C: "+ currCommission);
			//			System.out.println( "O: "+ currOmission);
			//			System.out.println( "I:"+ currInduction);
			//			System.exit(1);
			commission.addNumber(currCommission);
			omission.addNumber(currOmission);
			induction.addNumber(currInduction);

			double precisionTraining = trainingCorrectPosClassified + negAsPosTraining == 0 ? 0 : trainingCorrectPosClassified / (double) (trainingCorrectPosClassified + negAsPosTraining);
			double recallTraining = trainingCorrectPosClassified / (double) trainPos.size();
			System.out.println(precisionTraining +"----"+recallTraining);

			fMeasureTraining.addNumber(100*Heuristics.getFScore(recallTraining, precisionTraining));

			// calculate test F-Score
			double precision = correctPosClassified + negAsPos == 0 ? 0 : correctPosClassified / (double) (correctPosClassified + negAsPos);
			double recall = correctPosClassified / (double) testPos.size();
			//		System.out.println(precision);System.out.println(recall);
			fMeasure.addNumber(100*Heuristics.getFScore(recall, precision));
			length.addNumber(OWLClassExpressionUtils.getLength(concept));

		}

		//System.exit(0);
		output += "fold " + currFold + ":" + "\n";
		output += "  training: " + pos.size() + " positive, " + neg.size() + " negative examples and "+ und.size() + " uncertain examples";
		output += "  testing: " + correctPosClassified + "/" + testPos.size() + " correct positives, "
				+ correctNegClassified + "/" + testNeg.size() + " correct negatives " + correctUndClassified+"/"+ testUnd.size()+" correct uncertain \n";
		output += "  concept: " + concept.toString().replace("\n", " ") + "\n";
		output += "  match: " + df.format(currAccuracy) + "% (" + df.format(trainingAccuracy) + "% on training set)" + "\n";
		output += "  commission: " + df.format(currCommission) + "% (" + df.format(trainingCommission) + "% on training set)" + "\n";
		output += "  omission: " + df.format(currOmission) + "% (" + df.format(trainingOmission) + "% on training set)" + "\n";
		output += "  induction: " + df.format(currInduction) + "% (" + df.format(trainingInduction) + "% on training set)" + "\n";
		output += "  length: " + df.format(OWLClassExpressionUtils.getLength(concept)) + "\n";
		output += "  runtime: " + df.format(algorithmDuration/(double)1000000000) + "s" + "\n";

		System.out.println(output);

		outputWriter(output);
	}

	private int getCorrectClassifications( int groundtruth,  Set<OWLIndividual> set, AbstractCELA la) {
		int trainingCorrectClassified=0;
		for (OWLIndividual indTestEx: set){
			int label =0;
			if (la instanceof DSTTDTClassifier)
				label=((DSTTDTClassifier)la).classifyExamplesDST(indTestEx, ((DSTTDTClassifier)la).getCurrentmodel());
			else if (la instanceof TDTClassifier){
				label= ((TDTClassifier)la).classifyExample(indTestEx, ((TDTClassifier)la).getCurrentmodel());}
			//			if (label==groundtruth)
			//				trainingCorrectClassified++;
			//
			//		}
			//		System.out.println(" GetCorrectClassified Label: "+label);
			if (label == groundtruth) {
			 //System.out.println("\t  Ground truth "+label+" Predicted "+ groundtruth+ ": matched");

				trainingCorrectClassified++;
			}

		}
		//		System.out.println("End round 1");
		return trainingCorrectClassified;
	}

	private int getWrongClassification(Set<OWLIndividual> set, AbstractCELA la) {
		//System.out.println("GetWrongClassified");

		int trainingWrongClassified=0;
		for (OWLIndividual indTestEx: set){
			int label = 0;
			if (la instanceof DSTTDTClassifier) {
				label=((DSTTDTClassifier)la).classifyExamplesDST(indTestEx, ((DSTTDTClassifier)la).getCurrentmodel());
				//System.out.println("---->"+label);
			}
			else if (la instanceof TDTClassifier){
				label=((TDTClassifier)la).classifyExample(indTestEx, ((TDTClassifier)la).getCurrentmodel());
				//System.out.println("****>"+label);
			}
			//			System.out.println(" GetWrongClassified neg aS POS Label"+label);
			//System.out.println("\t Ground truth +1 Predicted "+ label);
			//System.out.println(((label==+1)));
			if ((label==+1)) {
				
				trainingWrongClassified++;
			}

		}
		//System.out.println("# errors: "+ trainingWrongClassified);
		return trainingWrongClassified;
	}

	private int getWrongClassificationNeg( Set<OWLIndividual> set, AbstractCELA la) {
		//System.out.println("\n GetWrongClassified Neg");

		int trainingWrongClassified=0;
		for (OWLIndividual indTestEx: set){
			int label = 0;

			if (la instanceof DSTTDTClassifier) {
				label=((DSTTDTClassifier)la).classifyExamplesDST(indTestEx, ((DSTTDTClassifier)la).getCurrentmodel());
				//System.out.println("---->"+label);
			}
			else if (la instanceof TDTClassifier){
				label=((TDTClassifier)la).classifyExample(indTestEx, ((TDTClassifier)la).getCurrentmodel());
				//System.out.println("*****>"+label);
			}
			//tdt.classifyExamplesDST(indTestEx, tdt.getCurrentmodel());
		   	//System.out.println("label: "+label +" groundtruth +1");
		   	//System.out.println(((label!=-1) && (label!=0)));
			if ((label==-1)) {
				//				System.out.println("POS s neg label: "+label);
				//System.out.println("\t Ground truth "+groundtruth+" Predicted "+ label+ ":committed");
				trainingWrongClassified++;
			}

		}
		
		System.out.println("# errors: "+ trainingWrongClassified);
		return trainingWrongClassified;
	}

	private int getOmittedClassification( int groundtruth, Set<OWLIndividual> set, AbstractCELA la) {
		//System.out.println("Groundtruth: "+ groundtruth);
		int trainingWrongClassified=0;
		for (OWLIndividual indTestEx: set){
			int label = 0;
			if (la instanceof DSTTDTClassifier) {
				label=((DSTTDTClassifier)la).classifyExamplesDST(indTestEx, ((DSTTDTClassifier)la).getCurrentmodel());
			
			}else if (la instanceof TDTClassifier){
				label=((TDTClassifier)la).classifyExample(indTestEx, ((TDTClassifier)la).getCurrentmodel());
			}
				//tdt.classifyExamplesDST(indTestEx, tdt.getCurrentmodel());
			//System.out.println( "LAbel:"+ label +" GroundTruth:"+groundtruth); //"Omission? "+((label==0)&&(groundtruth!=0)));
			if ((label==0)&&(groundtruth !=0))
				trainingWrongClassified++;
		}
		
		//System.out.println("Omissions: "+ trainingWrongClassified);
		return trainingWrongClassified;
	}

	private int getInductionClassification(Set<OWLIndividual> set, AbstractCELA la) {
		int trainingWrongClassified=0;
		for (OWLIndividual indTestEx: set){
			int label = 0;
			if (la instanceof DSTTDTClassifier)
				label=((DSTTDTClassifier)la).classifyExamplesDST(indTestEx, ((DSTTDTClassifier)la).getCurrentmodel());
			else if (la instanceof TDTClassifier)
				label=((TDTClassifier)la).classifyExample(indTestEx, ((TDTClassifier)la).getCurrentmodel());
			//tdt.classifyExamplesDST(indTestEx, tdt.getCurrentmodel());
			if ((label!=0))
				trainingWrongClassified++;
		}
		//System.out.println("****Inductions: "+trainingWrongClassified +"/"+ set.size());
		return trainingWrongClassified;
	}

	private int getCorrectUndClassified(AbstractReasonerComponent rs, OWLClassExpression concept, Set<OWLIndividual> testUnd) {
		OWLDataFactory df = new OWLDataFactoryImpl();
		OWLObjectComplementOf complementOfConcept = df.getOWLObjectComplementOf(concept);
		int nOfUnc=0;
		for (OWLIndividual ind:testUnd){
			if ((!rs.hasType(concept, ind)) && !(rs.hasType(complementOfConcept, ind)))
				nOfUnc++;

		}

		return nOfUnc;
	}

	@Override
	public int getCorrectNegClassified(AbstractReasonerComponent rs, OWLClassExpression concept, Set<OWLIndividual> testSetNeg){
		// for dealing explictly with the Open World Assumption
		OWLDataFactory df = new OWLDataFactoryImpl();
		OWLObjectComplementOf complementOfConcept = df.getOWLObjectComplementOf(concept); // the real complement if it is exist
		return  (rs.hasType (complementOfConcept, testSetNeg)).size();
	}

}

