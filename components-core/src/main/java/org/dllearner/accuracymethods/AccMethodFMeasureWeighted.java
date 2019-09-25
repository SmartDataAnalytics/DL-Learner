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

import org.dllearner.core.ComponentAnn;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.learningproblems.Heuristics;

@ComponentAnn(name = "Weighted FMeasure", shortName = "weighted.fmeasure", version = 0)
public class AccMethodFMeasureWeighted implements AccMethodTwoValued, AccMethodWithBeta {
	
	@ConfigOption(defaultValue = "false", description = "balance the weights to relative set size")
	private boolean balanced = false;
	@ConfigOption(defaultValue = "1", description = "weight on the positive examples")
	private double posWeight = 1;
	@ConfigOption(defaultValue = "1", description = "weight on the negative examples")
	private double negWeight = 1;
	@ConfigOption(description = "beta factor (0 = do not use)", defaultValue = "0")
	private double beta = 0;

	@Override
	public void init() {
	}
	
	public AccMethodFMeasureWeighted() {
	}
	
	public AccMethodFMeasureWeighted(boolean init) {
		if (init) init();
	}

	@Override
	public double getAccOrTooWeak2(int tp, int fn, int fp, int tn, double noise) {
		int posExamples = tp + fn;
		int negExamples = fp + tn;

		double recall = Heuristics.divideOrZero( tp , tp+fn );

		if(recall == 0 || recall < 1 - noise) {
			return -1;
		}

		if (balanced) {
			posWeight = 1/(double)posExamples;
			negWeight = 1/(double)negExamples;
		}
		double precision = tp == 0 ? 0 : ( tp*posWeight ) / ( tp*posWeight+fp*negWeight );
		if (beta == 0) {
			return Heuristics.getFScore(recall, precision);
		} else {
			return Heuristics.getFScore(recall, precision, beta);
		}
	}

	public boolean isBalanced() {
		return balanced;
	}

	public void setBalanced(boolean balanced) {
		this.balanced = balanced;
	}

	public double getPosWeight() {
		return posWeight;
	}

	public void setPosWeight(double posWeight) {
		this.posWeight = posWeight;
	}

	public double getNegWeight() {
		return negWeight;
	}

	public void setNegWeight(double negWeight) {
		this.negWeight = negWeight;
	}

	@Override
	public void setBeta(double beta) {
		this.beta = beta;
	}
}
