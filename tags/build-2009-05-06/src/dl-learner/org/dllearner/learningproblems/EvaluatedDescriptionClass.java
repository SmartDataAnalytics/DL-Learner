/**
 * Copyright (C) 2007-2009, Jens Lehmann
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
package org.dllearner.learningproblems;

import java.util.Set;

import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;

/**
 * An evaluated description for learning classes in ontologies.
 * 
 * @author Jens Lehmann
 *
 */
public class EvaluatedDescriptionClass extends EvaluatedDescription {

	private ClassScore classScore;
	
	/**
	 * Constructs an evaluated description for learning classes in ontologies.
	 * @param description Description.
	 * @param score Score of description.
	 */
	public EvaluatedDescriptionClass(Description description, ClassScore score) {
		super(description, score);
		classScore = score;
	}
	
	/**
	 * @return The addition factor.
	 * @see org.dllearner.learningproblems.ClassScore#getAddition()
	 */
	public double getAddition() {
		return classScore.getAddition();
	}

	/**
	 * @return The instances of the class description, which are not instances
	 * of the class to learn.
	 * @see org.dllearner.learningproblems.ClassScore#getAdditionalInstances()
	 */
	public Set<Individual> getAdditionalInstances() {
		return classScore.getAdditionalInstances();
	}

	/**
	 * @return The coverage percentage.
	 * @see org.dllearner.learningproblems.ClassScore#getCoverage()
	 */
	public double getCoverage() {
		return classScore.getCoverage();
	}

	/**
	 * 
	 * @return The instances covered by the class description.
	 * @see org.dllearner.learningproblems.ClassScore#getCoveredInstances()
	 */
	public Set<Individual> getCoveredInstances() {
		return classScore.getCoveredInstances();
	}

	/**
	 * 
	 * @return True if adding the axiom to the knowledge base leads to an inconsistent knowledge base. False otherwise.
	 */
	public boolean isConsistent() {
		return classScore.isConsistent();
	}
	
	/**
	 * 
	 * @return True if adding the axiom to the knowledge base does not logically change the knowledge base (i.e. the axiom already follows from it). False otherwise.
	 */
	public boolean followsFromKB() {
		return classScore.followsFromKB();
	}	
	
}
