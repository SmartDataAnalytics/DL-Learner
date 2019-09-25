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
package org.dllearner.utilities.owl;

import java.util.Comparator;

import com.google.common.collect.ComparisonChain;
import org.dllearner.learningproblems.EvaluatedDescriptionPosNeg;

/**
 * Comparator for evaluated descriptions, which orders them by
 * accuracy as first criterion, length as second criterion, and
 * syntactic structure as third criterion.
 * 
 * @author Jens Lehmann
 *
 */
public class EvaluatedDescriptionPosNegComparator implements Comparator<EvaluatedDescriptionPosNeg> {

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(EvaluatedDescriptionPosNeg ed1, EvaluatedDescriptionPosNeg ed2) {
		return ComparisonChain.start()
				.compare(ed1.getAccuracy(), ed2.getAccuracy())
				.compare(ed1.getDescriptionLength(), ed2.getDescriptionLength())
				.compare(ed1.getDescription(), ed2.getDescription())
				.result();
	}

}
