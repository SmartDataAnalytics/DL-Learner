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

import org.dllearner.core.ComponentAnn;

@ComponentAnn(name = "Predictive Accuracy", shortName = "pred_acc", version = 0)
public class AccMethodPredAcc implements AccMethodTwoValued {

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
		int allExamples = posExamples + fp + tn;
		
		int maxNotCovered = (int) Math.ceil(noise*posExamples);
		
		if(fn != 0 && fn >= maxNotCovered) {
			return -1;
		}
		
		return (tp + tn) / (double) allExamples;
	}

}
