/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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

import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.owl.fuzzydll.FuzzyIndividual;
import org.dllearner.learningproblems.Heuristics.HeuristicType;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

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
	
	private static final Logger logger = LoggerFactory.getLogger(FuzzyPosNegLPStandard.class);
	
	// approximation and F-measure
	// (taken from class learning => super class instances corresponds to negative examples
	// and class instances to positive examples)
	private double approxDelta = 0.05;
	private boolean useApproximations;
//	private boolean useFMeasure;
	private boolean useOldDIGOptions = false;
	
	private HeuristicType heuristic = HeuristicType.PRED_ACC;

	private int errorIndex = 0;
	
	@ConfigOption(description = "Specifies, which method/function to use for computing accuracy. Available measues are \"PRED_ACC\" (predictive accuracy), \"FMEASURE\" (F measure), \"GEN_FMEASURE\" (generalised F-Measure according to Fanizzi and d'Amato).",defaultValue = "PRED_ACC")
    private HeuristicType accuracyMethod = HeuristicType.PRED_ACC;
	
	public FuzzyPosNegLPStandard() {}
	
	public FuzzyPosNegLPStandard(AbstractReasonerComponent reasoningService) {
		super(reasoningService);
	}

	public FuzzyPosNegLPStandard(AbstractReasonerComponent reasoningService, SortedSet<OWLIndividual> positiveExamples, SortedSet<OWLIndividual> negativeExamples) {
		super(reasoningService);
		this.positiveExamples = positiveExamples;
		this.negativeExamples = negativeExamples;
	}
	
	@Override
	public void init() throws ComponentInitException {
		super.init();
		
		if(useApproximations && accuracyMethod.equals(HeuristicType.PRED_ACC)) {
			logger.warn("Approximating predictive accuracy is an experimental feature. USE IT AT YOUR OWN RISK. If you consider to use it for anything serious, please extend the unit tests at org.dllearner.test.junit.HeuristicTests first and verify that it works.");
		}
		
		initialized = true;
	}

	@Override
	public double getAccuracyOrTooWeak(OWLClassExpression description, double noise) {
		// delegates to the appropriate methods
		return useApproximations ? getAccuracyOrTooWeakApprox(description, noise) : getAccuracyOrTooWeakExact(description, noise);
	}
	
	private double getAccuracyOrTooWeakApprox(OWLClassExpression description, double noise) {
		if(heuristic.equals(HeuristicType.PRED_ACC)) {
			int maxNotCovered = (int) Math.ceil(noise*positiveExamples.size());
			
			int notCoveredPos = 0;
//			int notCoveredNeg = 0;
			
			int posClassifiedAsPos = 0;
			int negClassifiedAsNeg = 0;
			
			int nrOfPosChecks = 0;
			int nrOfNegChecks = 0;
			
			// special case: we test positive and negative examples in turn
			Iterator<OWLIndividual> itPos = positiveExamples.iterator();
			Iterator<OWLIndividual> itNeg = negativeExamples.iterator();
			
			do {
				// in each loop we pick 0 or 1 positives and 0 or 1 negative
				// and classify it
				
				if(itPos.hasNext()) {
					OWLIndividual posExample = itPos.next();
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
					OWLIndividual negExample = itNeg.next();
					if(!getReasoner().hasType(description, negExample)) {
						negClassifiedAsNeg++;
					}
					nrOfNegChecks++;
				}
			
				// compute how accurate our current approximation is and return if it is sufficiently accurate
				double[] approx = Heuristics.getPredAccApproximation(positiveExamples.size(), negativeExamples.size(), 1, nrOfPosChecks, posClassifiedAsPos, nrOfNegChecks, negClassifiedAsNeg);
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
			
			for(OWLIndividual ind : positiveExamples) {
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
			
			for(OWLIndividual ind : negativeExamples) {

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
	
	private double getAccuracyOrTooWeakExact(OWLClassExpression description, double noise) {
		if(heuristic.equals(HeuristicType.PRED_ACC)) {
			return getPredAccuracyOrTooWeakExact(description, noise);
		} else if(heuristic.equals(HeuristicType.FMEASURE)) {
			return getFMeasureOrTooWeakExact(description, noise);
			/*
			// computing R(C) restricted to relevant instances
			int additionalInstances = 0;
			for(OWLIndividual ind : negativeExamples) {
				if(reasoner.hasType(description, ind)) {
					additionalInstances++;
				}
			}
			
			// computing R(A)
			int coveredInstances = 0;
			for(OWLIndividual ind : positiveExamples) {
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
	private double getPredAccuracyOrTooWeakExact(OWLClassExpression description, double noise) {
		
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
		double nonAccumulativeDescriptionMembership;
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
		
		double fuzzyAccuracy = descriptionMembership / fuzzyExamples.size();
		
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
	private double crispAccuracy(OWLClassExpression description, double noise) {
		int maxNotCovered = (int) Math.ceil(noise*positiveExamples.size());
		
		int notCoveredPos = 0;
		int notCoveredNeg = 0;
		
		for (OWLIndividual example : positiveExamples) {
			if (!getReasoner().hasType(description, example)) {
				notCoveredPos++;
				if(notCoveredPos >= maxNotCovered) {
					return -1;
				}
			}
		}
		for (OWLIndividual example : negativeExamples) {
			if (!getReasoner().hasType(description, example)) {
				notCoveredNeg++;
			}
		}
		return (positiveExamples.size() - notCoveredPos + notCoveredNeg) / (double) allExamples.size();
	}
	
	// added by Josue
	private double crispfMeasure(OWLClassExpression description, double noise) {
		// crisp F-measure
		int additionalInstances = 0;
		for(OWLIndividual ind : negativeExamples) {
			if(getReasoner().hasType(description, ind)) {
				additionalInstances++;
			}
		}
		
		int coveredInstances = 0;
		for(OWLIndividual ind : positiveExamples) {
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
	
	private double getFMeasureOrTooWeakExact(OWLClassExpression description, double noise) {
		
		// added by Josue
		// fuzzy F-measure
		double coveredMembershipDegree = 0;
		double totalMembershipDegree = 0;
		double invertedCoveredMembershipDegree = 0;
		double lastMembershipDegree;

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
	public double getFMeasureOrTooWeakApprox(OWLClassExpression description, double noise) {
		// we abort when there are too many uncovered positives
		int maxNotCovered = (int) Math.ceil(noise*positiveExamples.size());
		int instancesCovered = 0;
		int instancesNotCovered = 0;
		int total;
		boolean estimatedA = false;
		
		double lowerBorderA = 0;
		int lowerEstimateA = 0;
		double upperBorderA = 1;
		int upperEstimateA = positiveExamples.size();
		
		for(OWLIndividual ind : positiveExamples) {
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
				double p1 = Heuristics.p1(instancesCovered, total);
				double p2 = Heuristics.p3(p1, total);
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
		
		for(OWLIndividual ind : negativeExamples) {

			if(getReasoner().hasType(description, ind)) {
				instancesDescription++;
			}
			
			testsPerformed++;
			
			if(testsPerformed > 10) {
				
				// compute confidence interval
				double p1 = Heuristics.p1(instancesDescription, testsPerformed);
				double p2 = Heuristics.p3(p1, testsPerformed);
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
	public EvaluatedDescription evaluate(OWLClassExpression description) {
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
	
	/**
	 * @param accuracyMethod the accuracy method to set
	 */
	public void setAccuracyMethod(HeuristicType accuracyMethod) {
		this.accuracyMethod = accuracyMethod;
	}

	
	public double getAccuracy(int posAsPos, int posAsNeg, int negAsPos, int negAsNeg, double noise) {
		int maxNotCovered = (int) Math.ceil(noise * positiveExamples.size());
		
		if(posAsNeg > maxNotCovered) {
			return -1;
		}
		
		switch (accuracyMethod) {
		case PRED_ACC:
			return (posAsPos + negAsNeg) / (double) allExamples.size();
		case FMEASURE:
			double recall = posAsPos / (double)positiveExamples.size();
			
			if(recall < 1 - noise) {
				return -1;
			}
			
			double precision = (negAsPos + posAsPos == 0) ? 0 : posAsPos / (double) (posAsPos + negAsPos);
			
			return Heuristics.getFScore(recall, precision);
		default:
			throw new Error("Heuristic " + accuracyMethod + " not implemented.");
		}

	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractLearningProblem#computeScore(org.semanticweb.owlapi.model.OWLObject, double)
	 */
	@Override
	public ScorePosNeg<OWLNamedIndividual> computeScore(OWLClassExpression concept, double noise) {
		SortedSet<OWLIndividual> posAsPos = new TreeSet<>();
		SortedSet<OWLIndividual> posAsNeg = new TreeSet<>();
		SortedSet<OWLIndividual> negAsPos = new TreeSet<>();
		SortedSet<OWLIndividual> negAsNeg = new TreeSet<>();
		
		for (OWLIndividual example : positiveExamples) {
			if (getReasoner().hasType(concept, example)) {
				posAsPos.add(example);
			} else {
				posAsNeg.add(example);
			}
		}
		for (OWLIndividual example : negativeExamples) {
			if (getReasoner().hasType(concept, example))
				negAsPos.add(example);
			else
				negAsNeg.add(example);
		}
		
		// TODO: this computes accuracy twice - more elegant method should be implemented
		double accuracy = getAccuracy(posAsPos.size(), posAsNeg.size(), negAsPos.size(), negAsNeg.size(), noise);

		return new ScoreTwoValued(
				OWLClassExpressionUtils.getLength(concept),
				getPercentPerLengthUnit(),
				posAsPos, posAsNeg, negAsPos, negAsNeg,
				accuracy);
	}
	
}