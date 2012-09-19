/**
 * Copyright (C) 2007-2011, Jens Lehmann
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

package org.dllearner.core;

import java.util.List;

import org.dllearner.core.owl.Individual;

/**
 * 
 * An oracle can be used by a learning algorithm to interactively ask for the
 * classification of an individual. Note that an oracle can either be a user or
 * an automatic method, which means that the implementation of an oracle can
 * reach from simple checks to more complex user interaction processes.
 * 
 * Usually, an oracle should be instantiated by passing a learning problem
 * to its constructor.
 * 
 * @author Jens Lehmann
 *
 */
public interface Oracle {

	/**
	 * This method should be called by a learning algorithm if it wants a list of individuals
	 * (including the special case of a single individual) to be classified by the oracle.
	 * 
	 * For each of the individuals, which are specified in the parameter, the oracle must
	 * return a value in the obvious order. (The first element in the returned list classifies the first element
	 * of the list of individuals, the second element in the returned list classifies the
	 * second element of the individual list etc.) The following values should be used:
	 * 
	 * <ul>
	 * <li>-1.0: Indicates that the individual does not belong to the learned class.</li>
	 * <li>Values between -1.0 and 1.0 indicate the degree to which an individual belongs to the class.</li>
	 * <li>1.0: The individual does belong to the learned class.</li>
	 * <li>-2.0: The oracle does not know how to classify the individual (e.g. no feedback given by a user).</li>
	 * </ul>
	 * 
	 * Note that the most common case is that the individuals list contains a single element, which
	 * is answered by 1.0 or -1.0 by the oracle. However, the oracle interface is designed with a 
	 * higher degree of flexibility in mind, which is required for some use cases.
	 * 
	 * @param individuals A list of individuals, which should be classified by the oracle.
	 * @return For each element of the list, a classification value as explained above is
	 * returned.
	 *
	 */
	public List<Double> classifyIndividuals(List<Individual> individuals);
	
}