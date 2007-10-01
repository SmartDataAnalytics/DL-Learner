package org.dllearner.reasoning;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.Reasoner;
import org.dllearner.core.ReasoningMethodUnsupportedException;
import org.dllearner.core.dl.AtomicConcept;
import org.dllearner.core.dl.AtomicRole;
import org.dllearner.core.dl.Concept;
import org.dllearner.core.dl.Individual;
import org.dllearner.core.dl.RoleHierarchy;
import org.dllearner.core.dl.SubsumptionHierarchy;
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

	public SortedSet<Individual> instanceCheck(Concept concept, Set<Individual> individuals) throws ReasoningMethodUnsupportedException {
		SortedSet<Individual> returnSet = new TreeSet<Individual>();
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
