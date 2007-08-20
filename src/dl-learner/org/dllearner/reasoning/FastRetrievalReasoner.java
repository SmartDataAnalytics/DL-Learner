package org.dllearner.reasoning;

import java.util.Set;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.dl.AtomicConcept;
import org.dllearner.dl.AtomicRole;
import org.dllearner.dl.Concept;
import org.dllearner.dl.FlatABox;
import org.dllearner.dl.Individual;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.SortedSetTuple;

public class FastRetrievalReasoner extends AbstractReasoner {

	FlatABox abox;
	FastRetrieval fastRetrieval;
	Set<AtomicConcept> atomicConcepts;
	Set<AtomicRole> atomicRoles;
	SortedSet<Individual> individuals;
	
	public FastRetrievalReasoner(FlatABox abox) {
		this.abox = abox;
		fastRetrieval = new FastRetrieval(abox);
		
		// atomare Konzepte und Rollen initialisieren
		atomicConcepts = new HashSet<AtomicConcept>();
		for(String concept : abox.concepts) {
			atomicConcepts.add(new AtomicConcept(concept));
		}
		atomicRoles = new HashSet<AtomicRole>();
		for(String role : abox.roles) {
			atomicRoles.add(new AtomicRole(role));
		}
		individuals = new TreeSet<Individual>();
		for(String individualName : abox.domain)
			individuals.add(new Individual(individualName));
		
	}
	
	public ReasonerType getReasonerType() {
		return ReasonerType.FAST_RETRIEVAL;
	}

	public SortedSetTuple<Individual> doubleRetrieval(Concept concept) {
		return Helper.getIndividualTuple(fastRetrieval.calculateSets(concept));
	}	
	
	public SortedSetTuple<Individual> doubleRetrieval(Concept concept, Concept adc) {
		SortedSetTuple adcSet = fastRetrieval.calculateSets(adc);
		return Helper.getIndividualTuple(fastRetrieval.calculateSetsADC(concept, adcSet));
	}	
	
	public SortedSet<Individual> retrieval(Concept concept) {
		return Helper.getIndividualSet(fastRetrieval.calculateSets(concept).getPosSet());
	}
	
	public Set<AtomicConcept> getAtomicConcepts() {
		return atomicConcepts;
	}

	public Set<AtomicRole> getAtomicRoles() {
		return atomicRoles;
	}

	public SortedSet<Individual> getIndividuals() {
		return individuals;
	}

	public FlatABox getFlatAbox() {
		return abox;
	}

	public void prepareSubsumptionHierarchy() {
		// hier muss nichts getan werden
	}
	
}
