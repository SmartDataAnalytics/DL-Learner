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

@ComponentAnn(name = "Predictive Accuracy", shortName = "pred_acc", version = 0)
public class AccMethodPredAcc implements AccMethodTwoValued, AccMethodWithBeta {

	@ConfigOption(description = "beta factor (0 = do not use)", defaultValue = "0")
	private double beta = 0;

	public AccMethodPredAcc() {
	}

	public AccMethodPredAcc(boolean init) {
		if (init) init();
	}

	@Override
	public void init() {
	}

	@Override
	public double getAccOrTooWeak2(int tp, int fn, int fp, int tn, double noise) {
		int posExamples = tp + fn;
		int negExamples = fp + tn;
		int allExamples = posExamples + negExamples;

		if (beta == 0) {
			int maxNotCovered = (int) Math.ceil(noise * posExamples);

			if (fn != 0 && fn >= maxNotCovered) {
				return -1;
			}

			return (tp + tn) / (double) allExamples;

		} else {

			if ((beta * tp + negExamples) / (beta * posExamples + negExamples) < 1 - noise) {
				return -1;
			}

			// correctly classified divided by all examples
			return (beta * tp + tn) / (beta * posExamples + negExamples);
		}
		
	}

	@Override
	public void setBeta(double beta) {
		this.beta = beta;
	}
}
