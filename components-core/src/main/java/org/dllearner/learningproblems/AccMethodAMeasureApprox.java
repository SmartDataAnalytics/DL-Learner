package org.dllearner.learningproblems;

import java.util.Collection;

import org.dllearner.core.Reasoner;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;

public class AccMethodAMeasureApprox extends AccMethodAMeasure implements AccMethodCLPApproximate {

	private Reasoner reasoner;
	private double approxDelta;

	@Override
	public double getApproxDelta() {
		return approxDelta;
	}

	@Override
	public void setApproxDelta(double approxDelta) {
		this.approxDelta = approxDelta;
	}

	@Override
	public void setReasoner(Reasoner reasoner) {
		this.reasoner = reasoner;
	}

	@Override
	public double getAccApproxCLP(OWLClassExpression description,
			Collection<OWLIndividual> classInstances,
			Collection<OWLIndividual> superClassInstances,
			double coverageFactor, double noise) {
		// we abort when there are too many uncovered positives
		int maxNotCovered = (int) Math.ceil(noise*classInstances.size());
		int instancesCovered = 0;
		int instancesNotCovered = 0;
		int total = 0;
		boolean estimatedA = false;

		double lowerBorderA = 0;
		int lowerEstimateA = 0;
		double upperBorderA = 1;
		int upperEstimateA = classInstances.size();

		for(OWLIndividual ind : classInstances) {
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

					// we can estimate the best possible concept to reach with downward refinement
					// by setting precision to 1 and recall = mean stays as it is
					double optimumEstimate = (coverageFactor*mean+1)/(coverageFactor+1);

					// if the mean is greater than the required minimum, we can accept;
					// we also accept if the interval is small and close to the minimum
					// (worst case is to accept a few inaccurate descriptions)
					if(optimumEstimate > 1-noise-0.03) {
						//								|| (upperBorderA > mean && size < 0.03)) {
						instancesCovered = (int) (instancesCovered/(double)total * classInstances.size());
						upperEstimateA = (int) (upperBorderA * classInstances.size());
						lowerEstimateA = (int) (lowerBorderA * classInstances.size());
						estimatedA = true;
						break;
					}

					// reject only if the upper border is far away (we are very
					// certain not to lose a potential solution)
					//						if(upperBorderA + 0.1 < 1-noise) {
					double optimumEstimateUpperBorder = (coverageFactor*(upperBorderA+0.1)+1)/(coverageFactor+1);
					if(optimumEstimateUpperBorder < 1 - noise) {
						return -1;
					}
				}
			}
		}

		double recall = instancesCovered/(double)classInstances.size();

		//			MonitorFactory.add("estimatedA","count", estimatedA ? 1 : 0);
		//			MonitorFactory.add("aInstances","count", total);

		// we know that a definition candidate is always subclass of the
		// intersection of all super classes, so we test only the relevant instances
		// (leads to undesired effects for descriptions not following this rule,
		// but improves performance a lot);
		// for learning a superclass of a defined class, similar observations apply;


		int testsPerformed = 0;
		int instancesDescription = 0;
		//			boolean estimatedB = false;

		for(OWLIndividual ind : superClassInstances) {

			if(reasoner.hasType(description, ind)) {
				instancesDescription++;
			}

			testsPerformed++;

			if(testsPerformed > 10) {

				// compute confidence interval
				double p1 = Heuristics.p1(instancesDescription, testsPerformed);
				double p2 = Heuristics.p3(p1, testsPerformed);
				double lowerBorder = Math.max(0, p1 - p2);
				double upperBorder = Math.min(1, p1 + p2);
				int lowerEstimate = (int) (lowerBorder * superClassInstances.size());
				int upperEstimate = (int) (upperBorder * superClassInstances.size());

				double size;
				if(estimatedA) {
					//						size = 1/(coverageFactor+1) * (coverageFactor * (upperBorderA-lowerBorderA) + Math.sqrt(upperEstimateA/(upperEstimateA+lowerEstimate)) + Math.sqrt(lowerEstimateA/(lowerEstimateA+upperEstimate)));
					size = Heuristics.getAScore(upperBorderA, upperEstimateA/(double)(upperEstimateA+lowerEstimate), coverageFactor) - Heuristics.getAScore(lowerBorderA, lowerEstimateA/(double)(lowerEstimateA+upperEstimate),coverageFactor);
				} else {
					//						size = 1/(coverageFactor+1) * (coverageFactor * coverage + Math.sqrt(instancesCovered/(instancesCovered+lowerEstimate)) + Math.sqrt(instancesCovered/(instancesCovered+upperEstimate)));
					size = Heuristics.getAScore(recall, instancesCovered/(double)(instancesCovered+lowerEstimate),coverageFactor) - Heuristics.getAScore(recall, instancesCovered/(double)(instancesCovered+upperEstimate),coverageFactor);
				}

				if(size < 0.1) {
					//						System.out.println(instancesDescription + " of " + testsPerformed);
					//						System.out.println("interval from " + lowerEstimate + " to " + upperEstimate);
					//						System.out.println("size: " + size);

					//						estimatedB = true;
					// calculate total number of instances
					instancesDescription = (int) (instancesDescription/(double)testsPerformed * superClassInstances.size());
					break;
				}
			}
		}

		// since we measured/estimated accuracy only on instances outside A (superClassInstances
		// does not include instances of A), we need to add it in the denominator
		double precision = instancesCovered/(double)(instancesDescription+instancesCovered);
		if(instancesCovered + instancesDescription == 0) {
			precision = 0;
		}

		return Heuristics.getAScore(recall, precision, coverageFactor);

	}
	
}
