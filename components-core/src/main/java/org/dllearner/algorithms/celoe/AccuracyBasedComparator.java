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
package org.dllearner.algorithms.celoe;

import org.dllearner.utilities.owl.OWLClassExpressionLengthMetric;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;

import java.util.Comparator;

public class AccuracyBasedComparator implements Comparator<OENode> {

	private final OWLClassExpressionLengthMetric lengthMetric;

	public AccuracyBasedComparator(OWLClassExpressionLengthMetric lengthMetric) {
		this.lengthMetric = lengthMetric;
	}

	@Override
	public int compare(OENode node1, OENode node2) {
		int result = compareByAccuracy(node1, node2);

		if (result != 0) {
			return result;
		}

		return compareByLength(node1, node2);
	}

	private int compareByAccuracy(OENode node1, OENode node2) {
		double node1Accuracy = node1.getAccuracy();
		double node2Accuracy = node2.getAccuracy();

		return Double.compare(node1Accuracy, node2Accuracy);
	}

	private int compareByLength(OENode node1, OENode node2) {
		int node1Length = OWLClassExpressionUtils.getLength(node1.getDescription(), lengthMetric);
		int mode2Length = OWLClassExpressionUtils.getLength(node2.getDescription(), lengthMetric);

		return Integer.compare(mode2Length, node1Length);
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof AccuracyBasedComparator);
	}
}
