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

import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.dllearner.core.*;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.utilities.Helper;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import java.util.*;

/**
 * A learning problem, where we learn from positive examples only.
 *
 * @author Jens Lehmann
 *
 */
@ComponentAnn(name = "positive only learning problem", shortName = "posonlylp", version = 0.6)
public class PosOnlyLP extends AbstractClassExpressionLearningProblem<ScorePosOnly<OWLNamedIndividual>> {

	private static Logger logger = Logger.getLogger(PosOnlyLP.class);
    private long nanoStartTime;

	@ConfigOption(description = "the positive examples", required = true)
	protected SortedSet<OWLIndividual> positiveExamples;

	private List<OWLIndividual> positiveExamplesShuffled;
//	protected SortedSet<OWLIndividual> pseudoNegatives;
	private List<OWLIndividual> individuals;

	private boolean useApproximations = false;

	// approximation of accuracy +- 0.03 %
	private static final double approx = 0.03;

	// factor for higher weight on recall (needed for subclass learning)
	private double coverageFactor;

	public PosOnlyLP() {}

	public PosOnlyLP(AbstractReasonerComponent reasoningService) {
		super(reasoningService);
	}

	public PosOnlyLP(AbstractReasonerComponent reasoningService, SortedSet<OWLIndividual> positiveExamples) {
		super(reasoningService);
		this.positiveExamples = positiveExamples;
	}

	@Override
	public void init() throws ComponentInitException {
		ExampleLoader exampleLoaderHelper = this.getExampleLoaderHelper();
		if (exampleLoaderHelper != null && !exampleLoaderHelper.isInitialized()) {
			logger.info("Loading examples by expression");
			exampleLoaderHelper.setPosOnlyLP(this);
			exampleLoaderHelper.init();
		}

		Random rand = new Random(1);

		if(getReasoner() != null) {
			if(reasoner instanceof SPARQLReasoner) {
				throw new ComponentInitException("SPARQL reasoner currently not supported for PosOnly learning problem");
			}

			// sanity check whether examples are contained in KB
			Helper.checkIndividuals(reasoner, positiveExamples);

			SortedSet<OWLIndividual> allIndividuals = getReasoner().getIndividuals();

			this.individuals = new LinkedList<>(allIndividuals);
			Collections.shuffle(this.individuals, rand);
		}

		positiveExamplesShuffled = new LinkedList<>(positiveExamples);
		Collections.shuffle(positiveExamplesShuffled, rand);
		
		initialized = true;
	}

	public SortedSet<OWLIndividual> getPositiveExamples() {
		return positiveExamples;
	}

	/**
	 * @param useApproximations the useApproximations to set
	 */
	public void setUseApproximations(boolean useApproximations) {
		this.useApproximations = useApproximations;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.LearningProblem#computeScore(org.dllearner.core.owl.Description)
	 */
	@Override
	public ScorePosOnly<OWLNamedIndividual> computeScore(OWLClassExpression description, double noise) {
		Set<OWLIndividual> retrieval = getReasoner().getIndividuals(description);

		Set<OWLIndividual> instancesCovered = new TreeSet<>();
		Set<OWLIndividual> instancesNotCovered = new TreeSet<>();
		for(OWLIndividual ind : positiveExamples) {
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

	private double getAccuracyOrTooWeakApprox(OWLClassExpression description, double noise) {

		// instead of using the standard operation, we use optimisation
		// and approximation here

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

		for(OWLIndividual ind : positiveExamplesShuffled) {
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

		for(OWLIndividual ind : individuals) {

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

	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractLearningProblem#getAccuracyOrTooWeak(org.dllearner.core.owl.Description, double)
	 */
	@Override
	public double getAccuracyOrTooWeak(OWLClassExpression description, double noise) {
		return useApproximations ? getAccuracyOrTooWeakApprox(description, noise) : getAccuracyOrTooWeakExact(description, noise);
	}

	// exact computation for 5 heuristics; each one adapted to super class
	// learning;
	// each one takes the noise parameter into account
	private double getAccuracyOrTooWeakExact(OWLClassExpression description, double noise) {

		nanoStartTime = System.nanoTime();

		SortedSet<OWLIndividual> individualsC = reasoner.getIndividuals(description);

		// computing R(C) restricted to relevant instances
		int additionalInstances = Sets.difference(individualsC, positiveExamples).size();

		// computing R(A)
		int coveredInstances = Sets.intersection(individualsC, positiveExamples).size();

		double recall = coveredInstances / (double) positiveExamples.size();

		// noise computation is incorrect
		// if(recall < 1 - noise) {
		// return -1;
		// }

		double precision = (additionalInstances + coveredInstances == 0) ? 0 : coveredInstances
				/ (double) (coveredInstances + additionalInstances);

		// best reachable concept has same recall and precision 1:
		if (((1 + Math.sqrt(coverageFactor)) * recall) / (Math.sqrt(coverageFactor) + 1) < 1 - noise) {
			return -1;
		} else {
			return Heuristics.getFScore(recall, precision, coverageFactor);
		}

	}

	// see paper: expression used in confidence interval estimation
	private static double p3(double p1, int total) {
		return 1.96 * Math.sqrt(p1*(1-p1)/(total+4));
	}

	// see paper: p'
	private static double p1(int success, int total) {
		return (success+2)/(double)(total+4);
	}

	@Override
	@SuppressWarnings("unchecked")
	public EvaluatedDescription<ScorePosOnly<OWLNamedIndividual>> evaluate(OWLClassExpression description, double noise) {
		ScorePosOnly score = computeScore(description, noise);
		return new EvaluatedDescriptionPosOnly(description, score);
	}

	private double getAccuracy(double coverage, double protusion) {
		return 0.5 * (coverage + Math.sqrt(protusion));
	}

	public void setPositiveExamples(SortedSet<OWLIndividual> positiveExamples) {
		this.positiveExamples = positiveExamples;
	}

	public void setPositiveExamples(Set<OWLIndividual> positiveExamples) {
		this.positiveExamples = new TreeSet<>(positiveExamples);
	}
}
