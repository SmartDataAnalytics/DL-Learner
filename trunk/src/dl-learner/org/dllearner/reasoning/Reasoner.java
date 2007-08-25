package org.dllearner.reasoning;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.dllearner.dl.AtomicConcept;
import org.dllearner.dl.AtomicRole;
import org.dllearner.dl.Concept;
import org.dllearner.dl.Individual;
import org.dllearner.utilities.SortedSetTuple;

/**
 * Auflisten aller Reasoning-Methoden.
 * 
 * @author jl
 *
 */
public interface Reasoner {

	public ReasonerType getReasonerType();
	
	// Methode, die Subsumptionhierarchie initialisiert (sollte nur einmal
	// pro erstelltem ReasoningService bzw. Reasoner aufgerufen werden)
	// => erstellt auch vereinfachte Sichten auf Subsumptionhierarchie
	// (siehe einfacher Traversal in Diplomarbeit)
	public void prepareSubsumptionHierarchy();
	
	public boolean subsumes(Concept superConcept, Concept subConcept) throws ReasoningMethodUnsupportedException;
	
	// mehrere subsumption checks - spart bei DIG Anfragen (nur die zweite Methode wird gebraucht)
	public Set<Concept> subsumes(Concept superConcept, Set<Concept> subConcepts) throws ReasoningMethodUnsupportedException;
	public Set<Concept> subsumes(Set<Concept> superConcepts, Concept subConcept) throws ReasoningMethodUnsupportedException;	
	
	// liefert eine Menge paarweise nicht äquivalenter Konzepte zurück, die über dem Konzept in der
	// Subsumption-Hierarchie stehen
	// Methoden veraltet, da das jetzt von der SubsumptionHierarchy-Klasse geregelt wird
	// public SortedSet<Concept> getMoreGeneralConcepts(Concept concept) throws ReasoningMethodUnsupportedException;
	// public SortedSet<Concept> getMoreSpecialConcepts(Concept concept) throws ReasoningMethodUnsupportedException;
	
	public SubsumptionHierarchy getSubsumptionHierarchy() throws ReasoningMethodUnsupportedException;
	
	public RoleHierarchy getRoleHierarchy() throws ReasoningMethodUnsupportedException;
	
	public SortedSet<Individual> retrieval(Concept concept) throws ReasoningMethodUnsupportedException;
	
	public Map<Individual, SortedSet<Individual>> getRoleMembers(AtomicRole atomicRole) throws ReasoningMethodUnsupportedException;
	
	public boolean instanceCheck(Concept concept, Individual individual) throws ReasoningMethodUnsupportedException;
	
	// mehrere instance checks für ein Konzept - spart bei DIG Anfragen
	public Set<Individual> instanceCheck(Concept concept, Set<Individual> individuals) throws ReasoningMethodUnsupportedException;
	
	public SortedSetTuple<Individual> doubleRetrieval(Concept concept) throws ReasoningMethodUnsupportedException;
	
	public SortedSetTuple<Individual> doubleRetrieval(Concept concept, Concept adc) throws ReasoningMethodUnsupportedException;	
	
	public boolean isSatisfiable() throws ReasoningMethodUnsupportedException;
	
	// alle Konzepte, die i als Instanz haben
	public Set<AtomicConcept> getConcepts(Individual i) throws ReasoningMethodUnsupportedException;
	
	public Set<AtomicConcept> getAtomicConcepts();

	public Set<AtomicRole> getAtomicRoles();

	public SortedSet<Individual> getIndividuals();
}
