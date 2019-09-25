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

import org.dllearner.core.Component;
import org.dllearner.core.Reasoner;

/**
 * Implementation of approximate accuracy calculation.
 * These methods require the reasoner to incrementally query the knowledge source.
 */
public interface AccMethodApproximate extends Component {
	double getApproxDelta();

	/**
	 * set the approximation delta;
	 * this option should be exposed via @ConfigOption
	 * @param approxDelta delta
	 */
	void setApproxDelta(double approxDelta);

	/**
	 * set the reasoner;
	 * consumers of the accuracy method should call this method on all approximate components
	 * @param reasoner the problem reasoner
	 */
	void setReasoner(Reasoner reasoner);

}
