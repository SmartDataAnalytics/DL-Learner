package org.dllearner.learningproblems;

import java.util.Collection;

import org.dllearner.core.ComponentAnn;
import org.dllearner.core.Reasoner;
import org.dllearner.core.config.ConfigOption;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;

@ComponentAnn(name = "FMeasure Approximate", shortName = "approx.fmeasure", version = 0)
public class AccMethodFMeasureApprox extends AccMethodFMeasure implements AccMethodTwoValuedApproximate {
	@ConfigOption(description = "The Approximate Delta", defaultValue = "0.05", required = false)
	private double approxDelta = 0.05;
	private Reasoner reasoner;

	@Override
	public double getAccApprox2(OWLClassExpression description,
			Collection<OWLIndividual> positiveExamples,
			Collection<OWLIndividual> negativeExamples, double noise) {
		//		System.out.println("Testing " + description);

		// we abort when there are too many uncovered positives
		int maxNotCovered = (int) Math.ceil(noise*positiveExamples.size());
		int instancesCovered = 0;
		int instancesNotCovered = 0;

		for(OWLIndividual ind : positiveExamples) {
			if(reasoner.hasType(description, ind)) {
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

			if(reasoner.hasType(description, ind)) {
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
		return Heuristics.getFScore(recall, precision, 1);
	}

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

}
