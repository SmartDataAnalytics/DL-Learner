/**
 * Copyright (C) 2007-2011, Jens Lehmann
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
 */

package org.dllearner.learningproblems;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.fuzzydll.FuzzyUnsupportedCodeException;
import org.dllearner.core.options.BooleanConfigOption;
import org.dllearner.core.options.ConfigOption;
import org.dllearner.core.options.DoubleConfigOption;
import org.dllearner.core.options.StringConfigOption;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.fuzzydll.FuzzyIndividual;
import org.dllearner.learningproblems.Heuristics.HeuristicType;
import org.dllearner.utilities.Helper;

/**
 * The aim of this learning problem is to learn a concept definition such that
 * the positive examples and the negative examples do not follow. It is
 * 2-valued, because we only distinguish between covered and non-covered
 * examples. (A 3-valued problem distinguishes between covered examples,
 * examples covered by the negation of the concept, and all other examples.) The
 * 2-valued learning problem is often more useful for Description Logics due to
 * (the Open World Assumption and) the fact that negative knowledge, e.g. that a
 * person does not have a child, is or cannot be expressed.
 * 
 * @author Jens Lehmann
 * 
 */
@ComponentAnn(name = "FuzzyPosNegLPStandard", shortName = "fuzzyPosNeg", version = 0.2)
public class FuzzyPosNegLPStandard extends FuzzyPosNegLP {
	
	// approximation and F-measure
	// (taken from class learning => super class instances corresponds to negative examples
	// and class instances to positive examples)
	private double approxDelta = 0.05;
	private boolean useApproximations;
//	private boolean useFMeasure;	
	private boolean useOldDIGOptions = false;
	
	private HeuristicType heuristic = HeuristicType.PRED_ACC;

	private int errorIndex = 0;
	
	private String accuracyMethod;
	
	public FuzzyPosNegLPStandard() {
		
	}
	
	public FuzzyPosNegLPStandard(AbstractReasonerComponent reasoningService) {
		super(reasoningService);
	}

	public FuzzyPosNegLPStandard(AbstractReasonerComponent reasoningService, SortedSet<Individual> positiveExamples, SortedSet<Individual> negativeExamples) {
		super(reasoningService);
		this.positiveExamples = positiveExamples;
		this.negativeExamples = negativeExamples;
	}
	
	@Override
	public void init() {
		super.init();
//		useApproximations = configurator.getUseApproximations();
//		approxDelta = configurator.getApproxAccuracy();
		
		String accM = getAccuracyMethod();
		if(accM == null) {
			heuristic = HeuristicType.FMEASURE; // use F-Measure by default
		} else if(accM.equals("standard")) {
			heuristic = HeuristicType.AMEASURE;
		} else if(accM.equals("fmeasure")) {
			heuristic = HeuristicType.FMEASURE;
		} else if(accM.equals("generalised_fmeasure")) {
			heuristic = HeuristicType.GEN_FMEASURE;
		} else if(accM.equals("jaccard")) {
			heuristic = HeuristicType.JACCARD;
		} else if(accM.equals("pred_acc")) {
			heuristic = HeuristicType.PRED_ACC;
		}
		
//		useFMeasure = configurator.getAccuracyMethod().equals("fmeasure");
		
//		if((!useApproximations && useFMeasure) || (useApproximations && !useFMeasure)) {
//			System.err.println("Currently F measure can only be used in combination with approximated reasoning.");
//			System.exit(0);
//		}
		
		if(useApproximations && heuristic.equals(HeuristicType.PRED_ACC)) {
			System.err.println("Approximating predictive accuracy is an experimental feature. USE IT AT YOUR OWN RISK. If you consider to use it for anything serious, please extend the unit tests at org.dllearner.test.junit.HeuristicTests first and verify that it works.");
		}
		
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.Component#getName()
	 */
	public static String getName() {
		return "fuzzy pos neg learning problem";
	}
	
	public static Collection<ConfigOption<?>> createConfigOptions() {
		Collection<ConfigOption<?>> options = new LinkedList<ConfigOption<?>>(FuzzyPosNegLP.createConfigOptions());
		BooleanConfigOption approx = new BooleanConfigOption("useApproximations", "whether to use stochastic approximations for computing accuracy", false);
		options.add(approx);
		DoubleConfigOption approxAccuracy = new DoubleConfigOption("approxAccuracy", "accuracy of the approximation (only for expert use)", 0.05);
		options.add(approxAccuracy);
		StringConfigOption accMethod = new StringConfigOption("accuracyMethod", "Specifies, which method/function to use for computing accuracy.","predacc"); //  or domain/range of a property.
		accMethod.setAllowedValues(new String[] {"fmeasure", "predacc"});
		options.add(accMethod);		
		return options;
	}
	
	/**
	 * This method computes (using the reasoner) whether a concept is too weak.
	 * If it is not weak, it returns the number of covered negative examples. It
	 * can use retrieval or instance checks for classification.
	 * 
	 * @see org.dllearner.learningproblems.PosNegLP.UseMultiInstanceChecks
	 * TODO: Performance could be slightly improved by counting the number of
	 *       covers instead of using sets and counting their size.
	 * @param concept
	 *            The concept to test.
	 * @return -1 if concept is too weak and the number of covered negative
	 *         examples otherwise.
	 */
	@Override
	public int coveredNegativeExamplesOrTooWeak(Description concept) {
		try {
			throw new FuzzyUnsupportedCodeException();
		} catch (FuzzyUnsupportedCodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
//		if (useRetrievalForClassification) {
//			SortedSet<Individual> posClassified = reasoner.getIndividuals(concept);
//			SortedSet<Individual> negAsPos = Helper.intersection(negativeExamples, posClassified);
//			SortedSet<Individual> posAsNeg = new TreeSet<Individual>();
//
//			// the set is constructed piecewise to avoid expensive set
//			// operations
//			// on a large number of individuals
//			for (Individual posExample : positiveExamples) {
//				if (!posClassified.contains(posExample))
//					posAsNeg.add(posExample);
//			}
//
//			// too weak
//			if (posAsNeg.size() > 0)
//				return -1;
//			// number of covered negatives
//			else
//				return negAsPos.size();
//		} else {
//			if (useMultiInstanceChecks != UseMultiInstanceChecks.NEVER) {
//				// two checks
//				if (useMultiInstanceChecks == UseMultiInstanceChecks.TWOCHECKS) {
//					Set<Individual> s = reasoner.hasTypeFuzzyMembership(concept, positiveExamples);
//					// if the concept is too weak, then do not query negative
//					// examples
//					if (s.size() != positiveExamples.size())
//						return -1;
//					else {
//						s = reasoner.hasTypeFuzzyMembership(concept, negativeExamples);
//						return s.size();
//					}
//					// one check
//				} else {
//					Set<Individual> s = reasoner.hasTypeFuzzyMembership(concept, allExamples);
//					// test whether all positive examples are covered
//					if (s.containsAll(positiveExamples))
//						return s.size() - positiveExamples.size();
//					else
//						return -1;
//				}
//			} else {
//				// SortedSet<Individual> posAsNeg = new TreeSet<Individual>();
//				SortedSet<Individual> negAsPos = new TreeSet<Individual>();
//
//				for (Individual example : positiveExamples) {
//					if (!reasoner.hasType(concept, example))
//						return -1;
//					// posAsNeg.add(example);
//				}
//				for (Individual example : negativeExamples) {
//					if (reasoner.hasType(concept, example))
//						negAsPos.add(example);
//				}
//
//				return negAsPos.size();
//			}
//		}
		return -1;
	}

	/**
	 * Computes score of a given concept using the reasoner. Either retrieval or
	 * instance check are used. For the latter, this method treats
	 * <code>UseMultiInstanceChecks.TWO_CHECKS</code> as if it were 
	 * <code>UseMultiInstanceChecks.ONE_CHECKS</code> (it does not make much sense
	 * to implement TWO_CHECKS in this function, because we have to test all
	 * examples to create a score object anyway).
	 * 
	 * NOTE: The options above are no longer supported, because of interface changes (the options
	 * are more or less only relevant in combination with DIG).
	 * 
	 * @see org.dllearner.learningproblems.PosNegLP.UseMultiInstanceChecks
	 * @param concept
	 *            The concept to test.
	 * @return Corresponding Score object.
	 */
	@Override
	public ScorePosNeg computeScore(Description concept) {
		if(useOldDIGOptions) {
			if (useRetrievalForClassification) {
				SortedSet<Individual> posClassified = getReasoner().getIndividuals(concept);
				Set<Individual> posAsPos = Helper.intersection(positiveExamples, posClassified);
				Set<Individual> negAsPos = Helper.intersection(negativeExamples, posClassified);
				SortedSet<Individual> posAsNeg = new TreeSet<Individual>();

				// piecewise set construction
				for (Individual posExample : positiveExamples) {
					if (!posClassified.contains(posExample))
						posAsNeg.add(posExample);
				}
				SortedSet<Individual> negAsNeg = new TreeSet<Individual>();
				for (Individual negExample : negativeExamples) {
					if (!posClassified.contains(negExample))
						negAsNeg.add(negExample);
				}
				return new ScoreTwoValued(concept.getLength(), percentPerLengthUnit, posAsPos, posAsNeg, negAsPos, negAsNeg);
			// instance checks for classification
			} else {		
				Set<Individual> posAsPos = new TreeSet<Individual>();
				Set<Individual> posAsNeg = new TreeSet<Individual>();
				Set<Individual> negAsPos = new TreeSet<Individual>();
				Set<Individual> negAsNeg = new TreeSet<Individual>();
				
				if (useMultiInstanceChecks != UseMultiInstanceChecks.NEVER) {
					SortedSet<Individual> posClassified = getReasoner().hasType(concept, allExamples);
					SortedSet<Individual> negClassified = Helper.difference(allExamples, posClassified);
					posAsPos = Helper.intersection(positiveExamples, posClassified);
					posAsNeg = Helper.intersection(positiveExamples, negClassified);
					negAsPos = Helper.intersection(negativeExamples, posClassified);
					negAsNeg = Helper.intersection(negativeExamples, negClassified);
					
					// System.out.println("pos classified: " + posClassified);
					
					return new ScoreTwoValued(concept.getLength(), percentPerLengthUnit, posAsPos, posAsNeg, negAsPos,
							negAsNeg);
				} else {
					
					for (Individual example : positiveExamples) {
						if (getReasoner().hasType(concept, example)) {
							posAsPos.add(example);
						} else {
							posAsNeg.add(example);
						}
					}
					for (Individual example : negativeExamples) {
						if (getReasoner().hasType(concept, example))
							negAsPos.add(example);
						else
							negAsNeg.add(example);
					}
					return new ScoreTwoValued(concept.getLength(), percentPerLengthUnit, posAsPos, posAsNeg, negAsPos,
							negAsNeg);
				}
			}
		} else {
			
			SortedSet<Individual> posAsPos = new TreeSet<Individual>();
			SortedSet<Individual> posAsNeg = new TreeSet<Individual>();
			SortedSet<Individual> negAsPos = new TreeSet<Individual>();
			SortedSet<Individual> negAsNeg = new TreeSet<Individual>();
			
			for (Individual example : positiveExamples) {
				if (getReasoner().hasType(concept, example)) {
					posAsPos.add(example);
				} else {
					posAsNeg.add(example);
				}
			}
			for (Individual example : negativeExamples) {
				if (getReasoner().hasType(concept, example))
					negAsPos.add(example);
				else
					negAsNeg.add(example);
			}
			
			// TODO: this computes accuracy twice - more elegant method should be implemented 
			double accuracy = getAccuracyOrTooWeakExact(concept,1);
			
			return new ScoreTwoValued(concept.getLength(), percentPerLengthUnit, posAsPos, posAsNeg, negAsPos,
						negAsNeg, accuracy);
		}
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.LearningProblem#getAccuracy(org.dllearner.core.owl.Description)
	 */
	@Override
	public double getAccuracy(Description description) {
		// a noise value of 1.0 means that we never return too weak (-1.0) 
		return getAccuracyOrTooWeak(description, 1.0);		
		/*				
		int coveredPos = 0;
		int coveredNeg = 0;
		
		for (Individual example : positiveExamples) {
			if (reasoner.hasType(description, example)) {
				coveredPos++;
			} 
		}
		for (Individual example : negativeExamples) {
			if (reasoner.hasType(description, example)) {
				coveredNeg++;
			}
		}
		
		return coveredPos + negativeExamples.size() - coveredNeg / (double) allExamples.size();
		*/
	}

	@Override
	public double getAccuracyOrTooWeak(Description description, double noise) {	
		// delegates to the appropriate methods
		return useApproximations ? getAccuracyOrTooWeakApprox(description, noise) : getAccuracyOrTooWeakExact(description, noise);				
	}	
	
	public double getAccuracyOrTooWeakApprox(Description description, double noise) {
		if(heuristic.equals(HeuristicType.PRED_ACC)) {
			int maxNotCovered = (int) Math.ceil(noise*positiveExamples.size());
			
			int notCoveredPos = 0;
//			int notCoveredNeg = 0;
			
			int posClassifiedAsPos = 0;
			int negClassifiedAsNeg = 0;
			
			int nrOfPosChecks = 0;
			int nrOfNegChecks = 0;
			
			// special case: we test positive and negative examples in turn
			Iterator<Individual> itPos = positiveExamples.iterator();
			Iterator<Individual> itNeg = negativeExamples.iterator();
			
			do {
				// in each loop we pick 0 or 1 positives and 0 or 1 negative
				// and classify it
				
				if(itPos.hasNext()) {
					Individual posExample = itPos.next();
//					System.out.println(posExample);
					
					if(getReasoner().hasType(description, posExample)) {
						posClassifiedAsPos++;
					} else {
						notCoveredPos++;
					}
					nrOfPosChecks++;
					
					// take noise into account
					if(notCoveredPos > maxNotCovered) {
						return -1;
					}
				}
				
				if(itNeg.hasNext()) {
					Individual negExample = itNeg.next();
					if(!getReasoner().hasType(description, negExample)) {
						negClassifiedAsNeg++;
					}
					nrOfNegChecks++;
				}
			
				// compute how accurate our current approximation is and return if it is sufficiently accurate
				double approx[] = Heuristics.getPredAccApproximation(positiveExamples.size(), negativeExamples.size(), 1, nrOfPosChecks, posClassifiedAsPos, nrOfNegChecks, negClassifiedAsNeg);
				if(approx[1]<approxDelta) {
//					System.out.println(approx[0]);
					return approx[0];
				}
				
			} while(itPos.hasNext() || itNeg.hasNext());
			
			double ret = Heuristics.getPredictiveAccuracy(positiveExamples.size(), negativeExamples.size(), posClassifiedAsPos, negClassifiedAsNeg, 1);
			return ret;
					
		} else if(heuristic.equals(HeuristicType.FMEASURE)) {
//			System.out.println("Testing " + description);
			
			// we abort when there are too many uncovered positives
			int maxNotCovered = (int) Math.ceil(noise*positiveExamples.size());
			int instancesCovered = 0;
			int instancesNotCovered = 0;
			
			for(Individual ind : positiveExamples) {
				if(getReasoner().hasType(description, ind)) {
					instancesCovered++;
				} else {
					instancesNotCovered ++;
					if(instancesNotCovered > maxNotCovered) {
						return -1;
					}
				}
			}	
			
			double recall = instancesCovered/(double)positiveExamples.size();
			
			int testsPerformed = 0;
			int instancesDescription = 0;
			
			for(Individual ind : negativeExamples) {

				if(getReasoner().hasType(description, ind)) {
					instancesDescription++;
				}
				testsPerformed++;
				
				// check whether approximation is sufficiently accurate
				double[] approx = Heuristics.getFScoreApproximation(instancesCovered, recall, 1, negativeExamples.size(), testsPerformed, instancesDescription);
				if(approx[1]<approxDelta) {
					return approx[0];
				}
				
			}		
			
			// standard computation (no approximation)
			double precision = instancesCovered/(double)(instancesDescription+instancesCovered);
//			if(instancesCovered + instancesDescription == 0) {
//				precision = 0;
//			}
			return Heuristics.getFScore(recall, precision, 1);			
		} else {
			throw new Error("Approximation for " + heuristic + " not implemented.");
		}
	}
	
	public double getAccuracyOrTooWeakExact(Description description, double noise) {
		if(heuristic.equals(HeuristicType.PRED_ACC)) {
			return getPredAccuracyOrTooWeakExact(description, noise);
		} else if(heuristic.equals(HeuristicType.FMEASURE)) {
			return getFMeasureOrTooWeakExact(description, noise);
			/*
			// computing R(C) restricted to relevant instances
			int additionalInstances = 0;
			for(Individual ind : negativeExamples) {
				if(reasoner.hasType(description, ind)) {
					additionalInstances++;
				}
			}
			
			// computing R(A)
			int coveredInstances = 0;
			for(Individual ind : positiveExamples) {
				if(reasoner.hasType(description, ind)) {
					coveredInstances++;
				}
			}
			
			double recall = coveredInstances/(double)positiveExamples.size();
			double precision = (additionalInstances + coveredInstances == 0) ? 0 : coveredInstances / (double) (coveredInstances + additionalInstances);			
			
			return Heuristics.getFScore(recall, precision);
			*/
		} else {
			throw new Error("Heuristic " + heuristic + " not implemented.");
		}		
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.LearningProblem#getAccuracyOrTooWeak(org.dllearner.core.owl.Description, double)
	 */
	public double getPredAccuracyOrTooWeakExact(Description description, double noise) {
		
		// System.out.println(errorIndex++);

		// double crispAccuracy = crispAccuracy(description, noise);
		// if I erase next line, fuzzy reasoning fails
//		if (crispAccuracy == -1) {
//			System.out.print(description);
//			System.out.println();
//			 // return -1;
//		}
		
		// BEGIN
		// added by Josue
		// fuzzy extension
//		double posMembership = 0;
//		double negMembership = 0;
		double descriptionMembership = 0;
		// double accumulatedSingleMembership = 0;
		double nonAccumulativeDescriptionMembership = 0;
		double accumulativeDescriptionMembership = 0;
		
//		System.out.println("noise = " + noise);
		
		// int individualCounter = fuzzyExamples.size();
		double individualCounter = totalTruth;
		for (FuzzyIndividual fuzzyExample : fuzzyExamples) {
			// accumulatedSingleMembership += singleMembership;
			nonAccumulativeDescriptionMembership = 1 - Math.abs(fuzzyExample.getTruthDegree() - getReasoner().hasTypeFuzzyMembership(description, fuzzyExample));
			descriptionMembership += nonAccumulativeDescriptionMembership;
			individualCounter -= fuzzyExample.getTruthDegree();
			if ((accumulativeDescriptionMembership + (nonAccumulativeDescriptionMembership * fuzzyExample.getTruthDegree()) + individualCounter) < ((1 - noise) * totalTruth))
				return -1;
			accumulativeDescriptionMembership += nonAccumulativeDescriptionMembership * fuzzyExample.getTruthDegree();

		}
		
		double fuzzyAccuracy = descriptionMembership / (double)fuzzyExamples.size();
		
//		System.err.println("crispAccuracy = fuzzyAccuracy");
//		crispAccuracy = fuzzyAccuracy;
		
//		if (crispAccuracy != fuzzyAccuracy) {
//			System.err.println("***********************************************");
//			//System.err.println("* " + (errorIndex++));
//			System.err.println("* (crispAccuracy[" + crispAccuracy + "] != fuzzyAccuracy[" + fuzzyAccuracy + "])");
//			System.err.println("* DESC: " + description);
//			System.err.println("***********************************************");
//			Scanner sc = new Scanner(System.in);
//			sc.nextLine();
//		}
		
		return fuzzyAccuracy;
	}

	// added by Josue
	private double crispAccuracy(Description description, double noise) {
		int maxNotCovered = (int) Math.ceil(noise*positiveExamples.size());
		
		int notCoveredPos = 0;
		int notCoveredNeg = 0;
		
		for (Individual example : positiveExamples) {
			if (!getReasoner().hasType(description, example)) {
				notCoveredPos++;
				if(notCoveredPos >= maxNotCovered) {
					return -1;
				}
			} 
		}
		for (Individual example : negativeExamples) {
			if (!getReasoner().hasType(description, example)) {
				notCoveredNeg++;
			}
		}		
		return (positiveExamples.size() - notCoveredPos + notCoveredNeg) / (double) allExamples.size();
	}
	
	// added by Josue
	private double crispfMeasure(Description description, double noise) {
		// crisp F-measure
		int additionalInstances = 0;
		for(Individual ind : negativeExamples) {
			if(getReasoner().hasType(description, ind)) {
				additionalInstances++;
			}
		}
		
		int coveredInstances = 0;
		for(Individual ind : positiveExamples) {
			if(getReasoner().hasType(description, ind)) {
				coveredInstances++;
			}
		}
		
		double recall = coveredInstances/(double)positiveExamples.size();
		
		if(recall < 1 - noise) {
			return -1;
		}
		
		double precision = (additionalInstances + coveredInstances == 0) ? 0 : coveredInstances / (double) (coveredInstances + additionalInstances);
		
		return Heuristics.getFScore(recall, precision);
	}
	
	public double getFMeasureOrTooWeakExact(Description description, double noise) {
		
		// added by Josue
		// fuzzy F-measure
		double coveredMembershipDegree = 0;
		double totalMembershipDegree = 0;
		double invertedCoveredMembershipDegree = 0;
		double lastMembershipDegree = 0;

		for (FuzzyIndividual ind: fuzzyExamples) {
			lastMembershipDegree = (1 - Math.abs(ind.getTruthDegree() - getReasoner().hasTypeFuzzyMembership(description, ind)));
			coveredMembershipDegree += lastMembershipDegree * ind.getTruthDegree();
			totalMembershipDegree += ind.getTruthDegree();
			invertedCoveredMembershipDegree += (1 - ind.getTruthDegree()) * (1 - lastMembershipDegree);
		}
		double fuzzyRecall = totalMembershipDegree == 0 ? 0 :coveredMembershipDegree/totalMembershipDegree;

		if(fuzzyRecall < 1 - noise) {
			return -1;
		}
		double fuzzyPrecision = (coveredMembershipDegree + invertedCoveredMembershipDegree) == 0 ? 0: coveredMembershipDegree / (coveredMembershipDegree + invertedCoveredMembershipDegree);
		double fuzzyFmeasure = Heuristics.getFScore(fuzzyRecall, fuzzyPrecision);		

		// double crispFmeasure = crispfMeasure(description, noise);
		
		// crispFmeasure = fuzzyFmeasure;
		
//		if (crispFmeasure != fuzzyFmeasure) {
//			System.err.println("************************");
//			System.err.println("* crispFmeasuer = " + crispFmeasure);
//			System.err.println("* fuzzyFmeasuer = " + fuzzyFmeasure);
//			System.err.println("************************");
//			Scanner sc = new Scanner(System.in);
//			sc.nextLine();
//		}

		return fuzzyFmeasure;
	}
	
	// instead of using the standard operation, we use optimisation
	// and approximation here;
	// now deprecated because the Heuristics helper class is used
	@Deprecated
	public double getFMeasureOrTooWeakApprox(Description description, double noise) {
		// we abort when there are too many uncovered positives
		int maxNotCovered = (int) Math.ceil(noise*positiveExamples.size());
		int instancesCovered = 0;
		int instancesNotCovered = 0;
		int total = 0;
		boolean estimatedA = false;
		
		double lowerBorderA = 0;
		int lowerEstimateA = 0;
		double upperBorderA = 1;
		int upperEstimateA = positiveExamples.size();
		
		for(Individual ind : positiveExamples) {
			if(getReasoner().hasType(description, ind)) {
				instancesCovered++;
			} else {
				instancesNotCovered ++;
				if(instancesNotCovered > maxNotCovered) {
					return -1;
				}
			}
			
			// approximation step (starting after 10 tests)
			total = instancesCovered + instancesNotCovered;
			if(total > 10) {
				// compute confidence interval
				double p1 = ClassLearningProblem.p1(instancesCovered, total);
				double p2 = ClassLearningProblem.p3(p1, total);
				lowerBorderA = Math.max(0, p1 - p2);
				upperBorderA = Math.min(1, p1 + p2);
				double size = upperBorderA - lowerBorderA;
				// if the interval has a size smaller than 10%, we can be confident
				if(size < 2 * approxDelta) {
					// we have to distinguish the cases that the accuracy limit is
					// below, within, or above the limit and that the mean is below
					// or above the limit
					double mean = instancesCovered/(double)total;
					
					// if the mean is greater than the required minimum, we can accept;
					// we also accept if the interval is small and close to the minimum
					// (worst case is to accept a few inaccurate descriptions)
					if(mean > 1-noise || (upperBorderA > mean && size < 0.03)) {
						instancesCovered = (int) (instancesCovered/(double)total * positiveExamples.size());
						upperEstimateA = (int) (upperBorderA * positiveExamples.size());
						lowerEstimateA = (int) (lowerBorderA * positiveExamples.size());
						estimatedA = true;
						break;
					}
					
					// reject only if the upper border is far away (we are very
					// certain not to lose a potential solution)
					if(upperBorderA + 0.1 < 1-noise) {
						return -1;
					}
				}				
			}
		}	
		
		double recall = instancesCovered/(double)positiveExamples.size();
		
//		MonitorFactory.add("estimatedA","count", estimatedA ? 1 : 0);
//		MonitorFactory.add("aInstances","count", total);
		
		// we know that a definition candidate is always subclass of the
		// intersection of all super classes, so we test only the relevant instances
		// (leads to undesired effects for descriptions not following this rule,
		// but improves performance a lot);
		// for learning a superclass of a defined class, similar observations apply;


		int testsPerformed = 0;
		int instancesDescription = 0;
//		boolean estimatedB = false;
		
		for(Individual ind : negativeExamples) {

			if(getReasoner().hasType(description, ind)) {
				instancesDescription++;
			}
			
			testsPerformed++;
			
			if(testsPerformed > 10) {
				
				// compute confidence interval
				double p1 = ClassLearningProblem.p1(instancesDescription, testsPerformed);
				double p2 = ClassLearningProblem.p3(p1, testsPerformed);
				double lowerBorder = Math.max(0, p1 - p2);
				double upperBorder = Math.min(1, p1 + p2);
				int lowerEstimate = (int) (lowerBorder * negativeExamples.size());
				int upperEstimate = (int) (upperBorder * negativeExamples.size());
				
				double size;
				if(estimatedA) {
					size = getFMeasure(upperBorderA, upperEstimateA/(double)(upperEstimateA+lowerEstimate)) - getFMeasure(lowerBorderA, lowerEstimateA/(double)(lowerEstimateA+upperEstimate));					
				} else {
					size = getFMeasure(recall, instancesCovered/(double)(instancesCovered+lowerEstimate)) - getFMeasure(recall, instancesCovered/(double)(instancesCovered+upperEstimate));
				}
				
				if(size < 0.1) {
					instancesDescription = (int) (instancesDescription/(double)testsPerformed * negativeExamples.size());
					break;
				}
			}
		}
		
		double precision = instancesCovered/(double)(instancesDescription+instancesCovered);
		if(instancesCovered + instancesDescription == 0) {
			precision = 0;
		}	

//		System.out.println("description: " + description);
//		System.out.println("recall: " + recall);
//		System.out.println("precision: " + precision);
//		System.out.println("F-measure: " + getFMeasure(recall, precision));
//		System.out.println("exact: " + getAccuracyOrTooWeakExact(description, noise));
		
		return getFMeasure(recall, precision);
	}
		
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.LearningProblem#evaluate(org.dllearner.core.owl.Description)
	 */
	@Override
	public EvaluatedDescription evaluate(Description description) {
		ScorePosNeg score = computeScore(description);
		return new EvaluatedDescriptionPosNeg(description, score);
	}

	private double getFMeasure(double recall, double precision) {
		return 2 * precision * recall / (precision + recall);
	}

	public double getApproxDelta() {
		return approxDelta;
	}

	public void setApproxDelta(double approxDelta) {
		this.approxDelta = approxDelta;
	}

	public boolean isUseApproximations() {
		return useApproximations;
	}

	public void setUseApproximations(boolean useApproximations) {
		this.useApproximations = useApproximations;
	}

	public HeuristicType getHeuristic() {
		return heuristic;
	}

	public void setHeuristic(HeuristicType heuristic) {
		this.heuristic = heuristic;
	}

	public String getAccuracyMethod() {
		return accuracyMethod;
	}

	public void setAccuracyMethod(String accuracyMethod) {
		this.accuracyMethod = accuracyMethod;
	}	
	
}
