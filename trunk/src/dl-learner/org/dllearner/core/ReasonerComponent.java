/**
 * Copyright (C) 2007, Jens Lehmann
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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.dl.AtomicConcept;
import org.dllearner.core.dl.AtomicRole;
import org.dllearner.core.dl.Concept;
import org.dllearner.core.dl.Individual;
import org.dllearner.core.dl.RoleHierarchy;
import org.dllearner.core.dl.SubsumptionHierarchy;
import org.dllearner.utilities.SortedSetTuple;

/**
 * @author Jens Lehmann
 * 
 */
public abstract class ReasonerComponent extends Component implements Reasoner {

	public boolean subsumes(Concept superConcept, Concept subConcept)
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	public Set<Concept> subsumes(Concept superConcept, Set<Concept> subConcepts)
			throws ReasoningMethodUnsupportedException {
		Set<Concept> returnSet = new HashSet<Concept>();
		for (Concept subConcept : subConcepts) {
			if (subsumes(superConcept, subConcept))
				returnSet.add(subConcept);
		}
		return returnSet;
	}

	public Set<Concept> subsumes(Set<Concept> superConcepts, Concept subConcept)
			throws ReasoningMethodUnsupportedException {
		Set<Concept> returnSet = new HashSet<Concept>();
		for (Concept superConcept : superConcepts) {
			if (subsumes(superConcept, subConcept))
				returnSet.add(superConcept);
		}
		return returnSet;
	}

	public SortedSet<Individual> retrieval(Concept concept)
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	public Map<Individual, SortedSet<Individual>> getRoleMembers(AtomicRole atomicRole)
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	public boolean instanceCheck(Concept concept, Individual individual)
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	public SortedSet<Individual> instanceCheck(Concept concept, Set<Individual> individuals)
			throws ReasoningMethodUnsupportedException {
		SortedSet<Individual> returnSet = new TreeSet<Individual>();
		for (Individual individual : individuals) {
			if (instanceCheck(concept, individual))
				returnSet.add(individual);
		}
		return returnSet;
	}

	public SortedSetTuple<Individual> doubleRetrieval(Concept concept)
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	public SortedSetTuple<Individual> doubleRetrieval(Concept concept, Concept adc)
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	public boolean isSatisfiable() throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	public SubsumptionHierarchy getSubsumptionHierarchy()
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	public void prepareRoleHierarchy() throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	public RoleHierarchy getRoleHierarchy() throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	public Set<AtomicConcept> getConcepts(Individual i) throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

}
