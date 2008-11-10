/**
 * Copyright (C) 2007-2008, Jens Lehmann
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
 *
 */
package org.dllearner.core;

import java.util.Set;
import java.util.SortedSet;

import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.utilities.datastructures.SortedSetTuple;

/**
 * Reasoning requests/queries related to individuals in the knowledge base.
 * 
 * @author Jens Lehmann
 *
 */
public interface IndividualReasoner {

	/**
	 * Returns types of an individual, i.e. those classes where the individual
	 * is instance of. For instance, the individual eric could have type Person. 
	 * 
	 * @param individual An individual in the knowledge base.
	 * @return Types this individual is instance of.
	 */
	public Set<NamedClass> getTypes(Individual individual);
	
	/**
	 * Checks whether <code>individual</code> is instance of <code>description</code>.
	 * For instance, "Leipzig" may be an instance of "City".
	 * 
	 * @param description An OWL class description.
	 * @param individual An individual.
	 * @return True if the instance has the description as type and false otherwise.
	 */
	public boolean hasType(Description description, Individual individual);
	
	/**
	 * Gets all instances of a given class description in the knowledge base.
	 * @param description An OWL class description.
	 * @return All instances of the class description.
	 */
	public SortedSet<Individual> getIndividuals(Description description);
	
	/**
	 * Performs a query for all instances of the given class description and
	 * its negation. (Note that in OWL it is possible that the reasoner can
	 * neither deduce that an individual is instance of a class nor its 
	 * negation.) This method might be more efficient that performing a 
	 * two retrievals.
	 * 
	 * @param description An OWL class description.
	 * @return All instances of the class description and its negation.
	 */
	public SortedSetTuple<Individual> doubleRetrieval(Description description);	
}
