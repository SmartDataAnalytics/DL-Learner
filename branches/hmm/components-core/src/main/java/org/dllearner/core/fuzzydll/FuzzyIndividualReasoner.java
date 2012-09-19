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

package org.dllearner.core.fuzzydll;

import java.util.SortedSet;

import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.fuzzydll.FuzzyIndividual;

/**
 * Reasoning requests/queries related to fuzzy reasoning over individuals in the knowledge base.
 * 
 * @author Josue Iglesias
 *
 */
public interface FuzzyIndividualReasoner {
	
	/**
	 * Checks the fuzzy membership degree of <code>individual</code> over <code>description</code>.
	 * For instance, "Peter" may be an instance of "TallPerson" with fuzzy membership degree = 0.8.
	 * individual
	 * @param description An OWL class description.
	 * @param individual An individual.
	 * @return fuzzy membership degree of <code>individual</code> satisfying <code>description</code> [0-1].
	 */
	public double hasTypeFuzzyMembership(Description description, FuzzyIndividual individual);
	public SortedSet<FuzzyIndividual> getFuzzyIndividuals(Description concept);
}
