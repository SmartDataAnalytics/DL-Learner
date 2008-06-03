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
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.dllearner.core.owl.Constant;
import org.dllearner.core.owl.DataRange;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.DatatypePropertyHierarchy;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectPropertyHierarchy;
import org.dllearner.core.owl.SubsumptionHierarchy;
import org.dllearner.utilities.datastructures.SortedSetTuple;

/**
 * @author Jens Lehmann
 * 
 */
public abstract class ReasonerComponent extends Component implements Reasoner {

	public abstract boolean hasDatatypeSupport();
	
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

	public Map<Individual, SortedSet<Constant>> getDatatypeMembers(DatatypeProperty datatypeProperty) 	
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}	
	
	// convenience method to get int value mappings of a datatype property
	public Map<Individual, SortedSet<Integer>> getIntDatatypeMembers(DatatypeProperty datatypeProperty) throws ReasoningMethodUnsupportedException {
		Map<Individual, SortedSet<Constant>> mapping = getDatatypeMembers(datatypeProperty);
		Map<Individual, SortedSet<Integer>> ret = new TreeMap<Individual, SortedSet<Integer>>();
		for(Entry<Individual, SortedSet<Constant>> e : mapping.entrySet()) {
			SortedSet<Constant> values = e.getValue();
			SortedSet<Integer> valuesInt = new TreeSet<Integer>();
			for(Constant c : values) {
				int v = Integer.parseInt(c.getLiteral());
				valuesInt.add(v);
			}
			ret.put(e.getKey(),valuesInt);
		}
		return ret;
	}	
	
	// convenience method to get double value mappings of a datatype property
	public Map<Individual, SortedSet<Double>> getDoubleDatatypeMembers(DatatypeProperty datatypeProperty) throws ReasoningMethodUnsupportedException {
		Map<Individual, SortedSet<Constant>> mapping = getDatatypeMembers(datatypeProperty);
		Map<Individual, SortedSet<Double>> ret = new TreeMap<Individual, SortedSet<Double>>();
		for(Entry<Individual, SortedSet<Constant>> e : mapping.entrySet()) {
			SortedSet<Constant> values = e.getValue();
			SortedSet<Double> valuesDouble = new TreeSet<Double>();
			for(Constant c : values) {
				double v = Double.parseDouble(c.getLiteral());
				valuesDouble.add(v);
			}
			ret.put(e.getKey(),valuesDouble);
		}
		return ret;
	}
	
	// convenience method to get boolean value mappings of a datatype property
	public Map<Individual, SortedSet<Boolean>> getBooleanDatatypeMembers(DatatypeProperty datatypeProperty) throws ReasoningMethodUnsupportedException {
		Map<Individual, SortedSet<Constant>> mapping = getDatatypeMembers(datatypeProperty);
		Map<Individual, SortedSet<Boolean>> ret = new TreeMap<Individual, SortedSet<Boolean>>();
		for(Entry<Individual, SortedSet<Constant>> e : mapping.entrySet()) {
			SortedSet<Constant> values = e.getValue();
			SortedSet<Boolean> valuesBoolean = new TreeSet<Boolean>();
			for(Constant c : values) {
				boolean v = Boolean.parseBoolean(c.getLiteral());
				valuesBoolean.add(v);
			}
			ret.put(e.getKey(),valuesBoolean);
		}
		return ret;
	}	
	
	// convenience method returning those values which have value "true" for this
	// datatype property
	public SortedSet<Individual> getTrueDatatypeMembers(DatatypeProperty datatypeProperty) throws ReasoningMethodUnsupportedException {
		Map<Individual, SortedSet<Constant>> mapping = getDatatypeMembers(datatypeProperty);
		SortedSet<Individual> ret = new TreeSet<Individual>();
		for(Entry<Individual, SortedSet<Constant>> e : mapping.entrySet()) {
			SortedSet<Constant> values = e.getValue();
			for(Constant c : values) {
				boolean v = Boolean.parseBoolean(c.getLiteral());
				if(v == true)
					ret.add(e.getKey());
			}
		}
		return ret;
	}	
	
	// convenience method returning those values which have value "false" for this
	// datatype property	
	public SortedSet<Individual> getFalseDatatypeMembers(DatatypeProperty datatypeProperty) throws ReasoningMethodUnsupportedException {
		Map<Individual, SortedSet<Constant>> mapping = getDatatypeMembers(datatypeProperty);
		SortedSet<Individual> ret = new TreeSet<Individual>();
		for(Entry<Individual, SortedSet<Constant>> e : mapping.entrySet()) {
			SortedSet<Constant> values = e.getValue();
			for(Constant c : values) {
				boolean v = Boolean.parseBoolean(c.getLiteral());
				if(v == false)
					ret.add(e.getKey());
			}
		}
		return ret;
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

	public void prepareDatatypePropertyHierarchy(Set<DatatypeProperty> allowedDatatypeProperties) throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}	
	
	public ObjectPropertyHierarchy getRoleHierarchy() throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	public DatatypePropertyHierarchy getDatatypePropertyHierarchy() throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}	
	
	public Set<NamedClass> getConcepts(Individual i) throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	public Description getDomain(ObjectProperty objectProperty) throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}
		
	public Description getDomain(DatatypeProperty datatypeProperty) throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}
	
	public Description getRange(ObjectProperty objectProperty) throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}
		
	public DataRange getRange(DatatypeProperty datatypeProperty) throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}	
	
	public SortedSet<DatatypeProperty> getDatatypeProperties() throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	public SortedSet<DatatypeProperty> getBooleanDatatypeProperties() throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}	

	public SortedSet<DatatypeProperty> getDoubleDatatypeProperties() throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}	

	public SortedSet<DatatypeProperty> getIntDatatypeProperties() throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}
	
	public abstract void releaseKB();
	
}
