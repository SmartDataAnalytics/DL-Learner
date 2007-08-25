package org.dllearner.reasoning;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.dllearner.dl.AtomicConcept;
import org.dllearner.dl.AtomicRole;
import org.dllearner.dl.Concept;
import org.dllearner.dl.Individual;
import org.dllearner.utilities.SortedSetTuple;

public abstract class AbstractReasoner implements Reasoner {

	public boolean subsumes(Concept superConcept, Concept subConcept)
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	public Set<Concept> subsumes(Concept superConcept, Set<Concept> subConcepts) throws ReasoningMethodUnsupportedException {
		Set<Concept> returnSet = new HashSet<Concept>();
		for(Concept subConcept : subConcepts) {
			if(subsumes(superConcept,subConcept))
				returnSet.add(subConcept);
		}
		return returnSet;
	}
	
	public Set<Concept> subsumes(Set<Concept> superConcepts, Concept subConcept) throws ReasoningMethodUnsupportedException {
		Set<Concept> returnSet = new HashSet<Concept>();
		for(Concept superConcept : superConcepts) {
			if(subsumes(superConcept,subConcept))
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

	public Set<Individual> instanceCheck(Concept concept, Set<Individual> individuals) throws ReasoningMethodUnsupportedException {
		Set<Individual> returnSet = new HashSet<Individual>();
		for(Individual individual : individuals) {
			if(instanceCheck(concept,individual))
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

	/*
	public SortedSet<Concept> getMoreGeneralConcepts(Concept concept)
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	public SortedSet<Concept> getMoreSpecialConcepts(Concept concept)
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}
	*/

	public SubsumptionHierarchy getSubsumptionHierarchy() throws ReasoningMethodUnsupportedException {
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
