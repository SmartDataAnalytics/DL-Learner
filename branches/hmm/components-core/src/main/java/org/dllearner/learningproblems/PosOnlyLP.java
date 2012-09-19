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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.options.CommonConfigMappings;
import org.dllearner.core.options.ConfigEntry;
import org.dllearner.core.options.ConfigOption;
import org.dllearner.core.options.InvalidConfigOptionValueException;
import org.dllearner.core.options.StringSetConfigOption;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;

/**
 * A learning problem, where we learn from positive examples only.
 * 
 * @author Jens Lehmann
 *
 */
@ComponentAnn(name = "positive only learning problem", shortName = "posonlylp", version = 0.6)
public class PosOnlyLP extends AbstractLearningProblem {

	protected SortedSet<Individual> positiveExamples;
	
	private List<Individual> positiveExamplesShuffled;
//	protected SortedSet<Individual> pseudoNegatives;
	private List<Individual> individuals;

	// approximation of accuracy +- 0.03 %
	private static final double approx = 0.03;	
	
	public PosOnlyLP() {
		super(null);
	}
	
	public PosOnlyLP(AbstractReasonerComponent reasoningService) {
		super(reasoningService);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.Component#applyConfigEntry(org.dllearner.core.ConfigEntry)
	 */
	@Override
	@SuppressWarnings( { "unchecked" })
	public <T> void applyConfigEntry(ConfigEntry<T> entry) throws InvalidConfigOptionValueException {
		String name = entry.getOptionName();
		if (name.equals("positiveExamples"))
			positiveExamples = CommonConfigMappings
					.getIndividualSet((Set<String>) entry.getValue());
	}
	
	public static Collection<ConfigOption<?>> createConfigOptions() {
		Collection<ConfigOption<?>> options = new LinkedList<ConfigOption<?>>();
		options.add(new StringSetConfigOption("positiveExamples",
				"positive examples", null, true, false));
		return options;
	}		
	
	public static String getName() {
		return "pos only learning problem";
	}
	
	@Override
	public void init() {
		
		// old init code, where pos only was realised through pseudo negatives
		
		// by default we test all other instances of the knowledge base
//		pseudoNegatives = Helper.difference(reasoner.getIndividuals(), positiveExamples);
		
		// create an instance of a standard definition learning problem
		// instanciated with pseudo-negatives
//		definitionLP = ComponentFactory.getPosNegLPStandard(
//				reasoner, 
//				SetManipulation.indToString(positiveExamples), 
//				SetManipulation.indToString(pseudoNegatives));
		//definitionLP = new PosNegDefinitionLP(reasoningService, positiveExamples, pseudoNegatives);
		// TODO: we must make sure that the problem also gets the same 
		// reasoning options (i.e. options are the same up to reversed example sets)
//		definitionLP.init();
		
		Random rand = new Random(1);		
		
		if(getReasoner()!=null) {
			individuals = new LinkedList<Individual>(getReasoner().getIndividuals());
			Collections.shuffle(individuals, rand);			
		}
		
		positiveExamplesShuffled = new LinkedList<Individual>(positiveExamples);
		Collections.shuffle(positiveExamplesShuffled, rand);
	}
	
	public SortedSet<Individual> getPositiveExamples() {
		return positiveExamples;
	}	
	
	/**
	 * @return the pseudoNegatives
	 */
//	public SortedSet<Individual> getPseudoNegatives() {
//		return pseudoNegatives;
//	}	
	

//	public int coveredPseudoNegativeExamplesOrTooWeak(Description concept) {
//		return definitionLP.coveredNegativeExamplesOrTooWeak(concept);
//	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.LearningProblem#computeScore(org.dllearner.core.owl.Description)
	 */
	@Override
	public ScorePosOnly computeScore(Description description) {
		Set<Individual> retrieval = getReasoner().getIndividuals(description);
		
		Set<Individual> instancesCovered = new TreeSet<Individual>();
		Set<Individual> instancesNotCovered = new TreeSet<Individual>();
		for(Individual ind : positiveExamples) {
			if(retrieval.contains(ind)) {
				instancesCovered.add(ind);
			} else {
				instancesNotCovered.add(ind);
			}
		}
		
		double coverage = instancesCovered.size()/(double)positiveExamples.size();
		double protusion = retrieval.size() == 0 ? 0 : instancesCovered.size()/(double)retrieval.size();	
		
		// pass only additional instances to score object
		retrieval.removeAll(instancesCovered);
		return new ScorePosOnly(instancesCovered, instancesNotCovered, coverage, retrieval, protusion, getAccuracy(coverage, protusion));		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.LearningProblem#evaluate(org.dllearner.core.owl.Description)
	 */
	@Override
	public EvaluatedDescriptionPosOnly evaluate(Description description) {
		ScorePosOnly score = computeScore(description);
		return new EvaluatedDescriptionPosOnly(description, score);		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.LearningProblem#getAccuracy(org.dllearner.core.owl.Description)
	 */
	@Override
	public double getAccuracy(Description description) {
		Set<Individual> retrieval = getReasoner().getIndividuals(description);
		
		int instancesCovered = 0;
		for(Individual ind : positiveExamples) {
			if(retrieval.contains(ind)) {
				instancesCovered++;
			}
		}
		
		double coverage = instancesCovered/(double)positiveExamples.size();
		double protusion = retrieval.size() == 0 ? 0 : instancesCovered/(double)retrieval.size();	
		
		return getAccuracy(coverage, protusion);		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.LearningProblem#getAccuracyOrTooWeak(org.dllearner.core.owl.Description, double)
	 */
	@Override
	public double getAccuracyOrTooWeak(Description description, double noise) {
		
		// instead of using the standard operation, we use optimisation
		// and approximation here
		
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
		
		for(Individual ind : positiveExamplesShuffled) {
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
				double p1 = p1(instancesCovered, total);
				double p2 = p3(p1, total);
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
					if(mean > noise || (upperBorderA > mean && size < 0.03)) {
						instancesCovered = (int) (instancesCovered/(double)total * positiveExamples.size());
						upperEstimateA = (int) (upperBorderA * positiveExamples.size());
						lowerEstimateA = (int) (lowerBorderA * positiveExamples.size());
						estimatedA = true;
						break;
					}
					
					// reject only if the upper border is far away (we are very
					// certain not to lose a potential solution)
					if(upperBorderA + 0.1 < noise) {
						return -1;
					}
				}				
			}
		}	
		
		double coverage = instancesCovered/(double)positiveExamples.size();
		
		int testsPerformed = 0;
		int instancesDescription = 0;
		
		for(Individual ind : individuals) {

			if(getReasoner().hasType(description, ind)) {
				instancesDescription++;
			}
			
			testsPerformed++;
			
			if(testsPerformed > 10) {
				
				// compute confidence interval
				double p1 = p1(instancesDescription, testsPerformed);
				double p2 = p3(p1, testsPerformed);
				double lowerBorder = Math.max(0, p1 - p2);
				double upperBorder = Math.min(1, p1 + p2);
				int lowerEstimate = (int) (lowerBorder * individuals.size());
				int upperEstimate = (int) (upperBorder * individuals.size());
				
				double size;
				if(estimatedA) {
//					size = 1/(coverageFactor+1) * (coverageFactor * (upperBorderA-lowerBorderA) + Math.sqrt(upperEstimateA/(upperEstimateA+lowerEstimate)) + Math.sqrt(lowerEstimateA/(lowerEstimateA+upperEstimate)));
					size = getAccuracy(upperBorderA, upperEstimateA/(double)(upperEstimateA+lowerEstimate)) - getAccuracy(lowerBorderA, lowerEstimateA/(double)(lowerEstimateA+upperEstimate));					
				} else {
//					size = 1/(coverageFactor+1) * (coverageFactor * coverage + Math.sqrt(instancesCovered/(instancesCovered+lowerEstimate)) + Math.sqrt(instancesCovered/(instancesCovered+upperEstimate)));
					size = getAccuracy(coverage, instancesCovered/(double)(instancesCovered+lowerEstimate)) - getAccuracy(coverage, instancesCovered/(double)(instancesCovered+upperEstimate));
				}
				
				if(size < 0.1) {
//					System.out.println(instancesDescription + " of " + testsPerformed);
//					System.out.println("interval from " + lowerEstimate + " to " + upperEstimate);
//					System.out.println("size: " + size);
					
//					estimatedB = true;
					// calculate total number of instances
					instancesDescription = (int) (instancesDescription/(double)testsPerformed * individuals.size());
					break;
				}
			}
		}
		
		// since we measured/estimated accuracy only on instances outside A (superClassInstances
		// does not include instances of A), we need to add it in the denominator
		double protusion = instancesCovered/(double)(instancesDescription+instancesCovered);
		if(instancesCovered + instancesDescription == 0) {
			protusion = 0;
		}		
	
		return getAccuracy(coverage, protusion);		
	}
	
	// see paper: expression used in confidence interval estimation
	private static double p3(double p1, int total) {
		return 1.96 * Math.sqrt(p1*(1-p1)/(total+4));
	}		
		
	// see paper: p'
	private static double p1(int success, int total) {
		return (success+2)/(double)(total+4);
	}	
	
	private double getAccuracy(double coverage, double protusion) {
		return 0.5 * (coverage + Math.sqrt(protusion));
	}

	public void setPositiveExamples(SortedSet<Individual> positiveExamples) {
		this.positiveExamples = positiveExamples;
	}	
}
