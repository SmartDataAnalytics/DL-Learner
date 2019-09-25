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
package org.dllearner.accuracymethods;

import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.Reasoner;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.learningproblems.Heuristics;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;

import java.util.Collection;

@ComponentAnn(name = "FMeasure Approximate", shortName = "approx.fmeasure", version = 0)
public class AccMethodFMeasureApprox extends AccMethodFMeasure implements AccMethodTwoValuedApproximate, AccMethodWithBeta {
	@ConfigOption(description = "The Approximate Delta", defaultValue = "0.05", required = false)
	private double approxDelta = 0.05;
	@ConfigOption(description = "reasoner component (configured  by learning problem)")
	private Reasoner reasoner;

	public AccMethodFMeasureApprox(boolean init, AbstractReasonerComponent reasoner) {
		this.setReasoner(reasoner);
		if(init) {
			init();
		}
	}

	public AccMethodFMeasureApprox() {
	}

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
			double[] approx = Heuristics.getFScoreApproximation(instancesCovered, recall, (beta == 0 ? 1 : beta), negativeExamples.size(), testsPerformed, instancesDescription);
			if(approx[1]<approxDelta) {
				return approx[0];
			}

		}

		// standard computation (no approximation)
		double precision = instancesCovered/(double)(instancesDescription+instancesCovered);
		return Heuristics.getFScore(recall, precision, (beta == 0 ? 1 : beta));
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
