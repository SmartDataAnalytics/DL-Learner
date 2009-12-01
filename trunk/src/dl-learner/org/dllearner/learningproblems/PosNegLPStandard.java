/**
 * Copyright (C) 2007, Jens Lehmann
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
 *
 */
package org.dllearner.learningproblems;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.configurators.PosNegLPStandardConfigurator;
import org.dllearner.core.options.BooleanConfigOption;
import org.dllearner.core.options.ConfigOption;
import org.dllearner.core.options.DoubleConfigOption;
import org.dllearner.core.options.StringConfigOption;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
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
public class PosNegLPStandard extends PosNegLP {
	
	private PosNegLPStandardConfigurator configurator;
	
	// approximation and F-measure
	// (taken from class learning => super class instances corresponds to negative examples
	// and class instances to positive examples)
	private double approx = 0.05;
	private boolean useApproximations;
	private boolean useFMeasure;	
	
	@Override
	public PosNegLPStandardConfigurator getConfigurator() {
		return configurator;
	}

	public PosNegLPStandard(ReasonerComponent reasoningService) {
		super(reasoningService);
		this.configurator = new PosNegLPStandardConfigurator(this);
	}

	public PosNegLPStandard(ReasonerComponent reasoningService, SortedSet<Individual> positiveExamples, SortedSet<Individual> negativeExamples) {
		super(reasoningService);
		this.positiveExamples = positiveExamples;
		this.negativeExamples = negativeExamples;
		this.configurator = new PosNegLPStandardConfigurator(this);
	}
	
	public void init() {
		super.init();
		useApproximations = configurator.getUseApproximations();
		useFMeasure = configurator.getAccuracyMethod().equals("fmeasure");
		
		if((!useApproximations && useFMeasure) || (useApproximations && !useFMeasure)) {
			System.err.println("Currently F measure can only be used in combination with approximated reasoning.");
			System.exit(0);
		}
		
		approx = configurator.getApproxAccuracy();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.Component#getName()
	 */
	public static String getName() {
		return "pos neg learning problem";
	}
	
	public static Collection<ConfigOption<?>> createConfigOptions() {
		Collection<ConfigOption<?>> options = new LinkedList<ConfigOption<?>>(PosNegLP.createConfigOptions());
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

		if (useRetrievalForClassification) {
			SortedSet<Individual> posClassified = reasoner.getIndividuals(concept);
			SortedSet<Individual> negAsPos = Helper.intersection(negativeExamples, posClassified);
			SortedSet<Individual> posAsNeg = new TreeSet<Individual>();

			// the set is constructed piecewise to avoid expensive set
			// operations
			// on a large number of individuals
			for (Individual posExample : positiveExamples) {
				if (!posClassified.contains(posExample))
					posAsNeg.add(posExample);
			}

			// too weak
			if (posAsNeg.size() > 0)
				return -1;
			// number of covered negatives
			else
				return negAsPos.size();
		} else {
			if (useMultiInstanceChecks != UseMultiInstanceChecks.NEVER) {
				// two checks
				if (useMultiInstanceChecks == UseMultiInstanceChecks.TWOCHECKS) {
					Set<Individual> s = reasoner.hasType(concept, positiveExamples);
					// if the concept is too weak, then do not query negative
					// examples
					if (s.size() != positiveExamples.size())
						return -1;
					else {
						s = reasoner.hasType(concept, negativeExamples);
						return s.size();
					}
					// one check
				} else {
					Set<Individual> s = reasoner.hasType(concept, allExamples);
					// test whether all positive examples are covered
					if (s.containsAll(positiveExamples))
						return s.size() - positiveExamples.size();
					else
						return -1;
				}
			} else {
				// SortedSet<Individual> posAsNeg = new TreeSet<Individual>();
				SortedSet<Individual> negAsPos = new TreeSet<Individual>();

				for (Individual example : positiveExamples) {
					if (!reasoner.hasType(concept, example))
						return -1;
					// posAsNeg.add(example);
				}
				for (Individual example : negativeExamples) {
					if (reasoner.hasType(concept, example))
						negAsPos.add(example);
				}

				return negAsPos.size();
			}
		}
	}

	/**
	 * Computes score of a given concept using the reasoner. Either retrieval or
	 * instance check are used. For the latter, this method treats
	 * <code>UseMultiInstanceChecks.TWO_CHECKS</code> as if it were 
	 * <code>UseMultiInstanceChecks.ONE_CHECKS</code> (it does not make much sense
	 * to implement TWO_CHECKS in this function, because we have to test all
	 * examples to create a score object anyway).
	 * 
	 * @see org.dllearner.learningproblems.PosNegLP.UseMultiInstanceChecks
	 * @param concept
	 *            The concept to test.
	 * @return Corresponding Score object.
	 */
	@Override
	public ScorePosNeg computeScore(Description concept) {
		if (useRetrievalForClassification) {
			SortedSet<Individual> posClassified = reasoner.getIndividuals(concept);
			SortedSet<Individual> posAsPos = Helper.intersection(positiveExamples, posClassified);
			SortedSet<Individual> negAsPos = Helper.intersection(negativeExamples, posClassified);
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
			SortedSet<Individual> posAsPos = new TreeSet<Individual>();
			SortedSet<Individual> posAsNeg = new TreeSet<Individual>();
			SortedSet<Individual> negAsPos = new TreeSet<Individual>();
			SortedSet<Individual> negAsNeg = new TreeSet<Individual>();
			
			if (useMultiInstanceChecks != UseMultiInstanceChecks.NEVER) {
				SortedSet<Individual> posClassified = reasoner.hasType(concept,
						allExamples);
				SortedSet<Individual> negClassified = Helper.difference(allExamples,
						posClassified);
				posAsPos = Helper.intersection(positiveExamples, posClassified);
				posAsNeg = Helper.intersection(positiveExamples, negClassified);
				negAsPos = Helper.intersection(negativeExamples, posClassified);
				negAsNeg = Helper.intersection(negativeExamples, negClassified);
				
				// System.out.println("pos classified: " + posClassified);
				
				return new ScoreTwoValued(concept.getLength(), percentPerLengthUnit, posAsPos, posAsNeg, negAsPos,
						negAsNeg);
			} else {
				
				for (Individual example : positiveExamples) {
					if (reasoner.hasType(concept, example)) {
						posAsPos.add(example);
					} else {
						posAsNeg.add(example);
					}
				}
				for (Individual example : negativeExamples) {
					if (reasoner.hasType(concept, example))
						negAsPos.add(example);
					else
						negAsNeg.add(example);
				}
				return new ScoreTwoValued(concept.getLength(), percentPerLengthUnit, posAsPos, posAsNeg, negAsPos,
						negAsNeg);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.LearningProblem#getAccuracy(org.dllearner.core.owl.Description)
	 */
	@Override
	public double getAccuracy(Description description) {
		
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
	}

	@Override
	public double getAccuracyOrTooWeak(Description description, double noise) {
		if(useApproximations) {
			if(useFMeasure) {
				return getFMeasureOrTooWeakApprox(description, noise);
			} else {
				throw new Error("approximating pred. acc not implemented");
			}
		} else {
			return getPredAccuracyOrTooWeakExact(description, noise);
		}			
	}	
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.LearningProblem#getAccuracyOrTooWeak(org.dllearner.core.owl.Description, double)
	 */
	public double getPredAccuracyOrTooWeakExact(Description description, double noise) {
		
		int maxNotCovered = (int) Math.ceil(noise*positiveExamples.size());
		
		int notCoveredPos = 0;
		int notCoveredNeg = 0;
		
		for (Individual example : positiveExamples) {
			if (!reasoner.hasType(description, example)) {
				notCoveredPos++;
				if(notCoveredPos >= maxNotCovered) {
					return -1;
				}
			} 
		}
		for (Individual example : negativeExamples) {
			if (!reasoner.hasType(description, example)) {
				notCoveredNeg++;
			}
		}
		
//		if(useFMeasure) {
//			double recall = (positiveExamples.size() - notCoveredPos) / (double) positiveExamples.size();
//			double precision = (positiveExamples.size() - notCoveredPos) / (double) (allExamples.size() - notCoveredPos - notCoveredNeg);
//			return getFMeasure(recall, precision);
//		} else {
			return (positiveExamples.size() - notCoveredPos + notCoveredNeg) / (double) allExamples.size();
//		}
	}

	public double getFMeasureOrTooWeakExact(Description description, double noise) {
		int additionalInstances = 0;
		for(Individual ind : negativeExamples) {
			if(reasoner.hasType(description, ind)) {
				additionalInstances++;
			}
		}
		
		int coveredInstances = 0;
		for(Individual ind : positiveExamples) {
			if(reasoner.hasType(description, ind)) {
				coveredInstances++;
			}
		}
		
		double recall = coveredInstances/(double)positiveExamples.size();
		
		if(recall < 1 - noise) {
			return -1;
		}
		
		double precision = (additionalInstances + coveredInstances == 0) ? 0 : coveredInstances / (double) (coveredInstances + additionalInstances);
		
		return getFMeasure(recall, precision);		
	}
	
	// instead of using the standard operation, we use optimisation
	// and approximation here
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
			if(reasoner.hasType(description, ind)) {
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
				if(size < 2 * approx) {
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

			if(reasoner.hasType(description, ind)) {
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
	
}
