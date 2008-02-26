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

import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectPropertyHierarchy;
import org.dllearner.core.owl.SubsumptionHierarchy;
import org.dllearner.utilities.SortedSetTuple;

/**
 * @author Jens Lehmann
 * 
 */
public abstract class ReasonerComponent extends Component implements Reasoner {

	public boolean subsumes(Description superConcept, Description subConcept)
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	public Set<Description> subsumes(Description superConcept, Set<Description> subConcepts)
			throws ReasoningMethodUnsupportedException {
		Set<Description> returnSet = new HashSet<Description>();
		for (Description subConcept : subConcepts) {
			if (subsumes(superConcept, subConcept))
				returnSet.add(subConcept);
		}
		return returnSet;
	}

	public Set<Description> subsumes(Set<Description> superConcepts, Description subConcept)
			throws ReasoningMethodUnsupportedException {
		Set<Description> returnSet = new HashSet<Description>();
		for (Description superConcept : superConcepts) {
			if (subsumes(superConcept, subConcept))
				returnSet.add(superConcept);
		}
		return returnSet;
	}

	public SortedSet<Individual> retrieval(Description concept)
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	public Map<Individual, SortedSet<Individual>> getRoleMembers(ObjectProperty atomicRole)
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	public boolean instanceCheck(Description concept, Individual individual)
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	public SortedSet<Individual> instanceCheck(Description concept, Set<Individual> individuals)
			throws ReasoningMethodUnsupportedException {
		SortedSet<Individual> returnSet = new TreeSet<Individual>();
		for (Individual individual : individuals) {
			if (instanceCheck(concept, individual))
				returnSet.add(individual);
		}
		return returnSet;
	}

	public SortedSetTuple<Individual> doubleRetrieval(Description concept)
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	public SortedSetTuple<Individual> doubleRetrieval(Description concept, Description adc)
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

	public void prepareRoleHierarchy(Set<ObjectProperty> allowedRoles) throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	public ObjectPropertyHierarchy getRoleHierarchy() throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	public Set<NamedClass> getConcepts(Individual i) throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	public Set<DatatypeProperty> getDatatypeProperties() throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	public Set<DatatypeProperty> getBooleanDatatypeProperties() throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}	

	public Set<DatatypeProperty> getDoubleDatatypeProperties() throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}	

	public Set<DatatypeProperty> getIntDatatypeProperties() throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}
	
}
