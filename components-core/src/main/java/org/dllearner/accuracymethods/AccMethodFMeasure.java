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

@ComponentAnn(name = "FMeasure", shortName = "fmeasure", version = 0)
public class AccMethodFMeasure implements AccMethodTwoValued, AccMethodWithBeta {

	@ConfigOption(description = "beta factor (0 = do not use)", defaultValue = "0")
	protected double beta = 0;

	@Override
	public void init() {
	}
	
	public AccMethodFMeasure() {
	}
	
	public AccMethodFMeasure(boolean init) {
		if (init) init();
	}

	@Override
	public double getAccOrTooWeak2(int tp, int fn, int fp, int tn, double noise) {
		double recall = Heuristics.divideOrZero( tp , tp+fn );

		if (beta == 0) {
			if (recall == 0 || recall < 1 - noise) {
				return -1;
			}
		} else {
			if (recall == 0 || ((1 + Math.sqrt(beta)) * recall) / (Math.sqrt(beta) + 1) < 1 - noise) {
				return -1;
			}
		}

		double precision = Heuristics.divideOrZero( tp , tp+fp );

		if (beta == 0) {
			return Heuristics.getFScore(recall, precision);
		} else {
			return Heuristics.getFScore(recall, precision, beta);
		}
	}

	@Override
	public void setBeta(double beta) {
		this.beta = beta;
	}
}
