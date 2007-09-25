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

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.OntologyFileFormat;
import org.dllearner.core.dl.AtomicConcept;
import org.dllearner.core.dl.AtomicRole;
import org.dllearner.core.dl.Concept;
import org.dllearner.core.dl.Individual;
import org.dllearner.reasoning.DIGReasoner;
import org.dllearner.reasoning.KAON2Reasoner;
import org.dllearner.reasoning.ReasonerType;
import org.dllearner.reasoning.RoleHierarchy;
import org.dllearner.reasoning.SubsumptionHierarchy;
import org.dllearner.utilities.SortedSetTuple;

/**
 * The reasoning service is the interface to the used reasoner. Basically,
 * it delegates all incoming reasoner request to the <code>Reasoner</code>
 * object which has been specified in the constructor. However, it calculates
 * some additional statistics about the time the reasoner needs to answer
 * the query.
 * 
 * @author Jens Lehmann
 * 
 */
public class ReasoningService {

	// statistische Daten
	private long instanceCheckReasoningTimeNs = 0;
	private int nrOfInstanceChecks = 0;
	private int nrOfMultiInstanceChecks = 0;
	private long retrievalReasoningTimeNs = 0;
	private int nrOfRetrievals = 0;
	private long subsumptionReasoningTimeNs = 0;
	private int nrOfSubsumptionChecks = 0;
	private int nrOfMultiSubsumptionChecks = 0;
	// private long subsumptionHierarchyTimeNs = 0;
	private int nrOfSubsumptionHierarchyQueries = 0;
	
	// restliche Reasoning-Zeit
	private long otherReasoningTimeNs = 0;

	// Zeit für alle Reasoningaufgabe (normalerweise länger als nur
	// obige Sachen)
	private long overallReasoningTimeNs = 0;
	
	// temporäre Variablen (ausgelagert wg. Performance und Vereinfachung)
	private long reasoningStartTimeTmp;
	private long reasoningDurationTmp;
	
	// Listenansicht
	private List<AtomicConcept> atomicConceptsList;
	private List<AtomicRole> atomicRolesList;

	// private SortedSet<Concept> retrievalsSet = new TreeSet<Concept>(new ConceptComparator());
	
	// Caching f�r allgemeinere/speziellere atomare Konzepte => wird innerhalb der Reasoner gemacht
	// private Map<Concept,Set<Concept>> moreGeneralConcepts = new HashMap<Concept,Set<Concept>>();
	// private Map<Concept,Set<Concept>> moreSpecialConcepts = new HashMap<Concept,Set<Concept>>();
	
	private Reasoner reasoner;

	// Beachte: wenn Wissensbasis modifiziert wird, muss ein neues
	// Reasoner-Objekt
	// angelegt werden (da Wissensbasis sofort entsprechend verwendetem
	// Reasoning-Typ
	// umgewandelt wird)
	public ReasoningService(Reasoner reasoner) {
		this.reasoner = reasoner;

		resetStatistics();
	}
	
	public ReasoningService(ReasonerComponent reasoner) {
		this.reasoner = reasoner;
	}
	
	public void init() {
		// temporary ugly hack to keep old version working
		((ReasonerComponent)reasoner).init();
		
		// Listenansicht
		atomicConceptsList = new LinkedList<AtomicConcept>(getAtomicConcepts());
		atomicRolesList = new LinkedList<AtomicRole>(getAtomicRoles());		
	}
	
	// zurücksetzen aller Statistiken (wenn z.B. vorher ein Satisfiability Check gemacht wird,
	// der allerdings nicht zum eigentlichen Algorithmus gehört)
	public void resetStatistics() {
		instanceCheckReasoningTimeNs = 0;
		nrOfInstanceChecks = 0;
		retrievalReasoningTimeNs = 0;
		nrOfRetrievals = 0;
		subsumptionReasoningTimeNs = 0;
		nrOfSubsumptionChecks = 0;
		// subsumptionHierarchyTimeNs = 0;
		nrOfSubsumptionHierarchyQueries = 0;
		otherReasoningTimeNs = 0;
		overallReasoningTimeNs = 0;		
	}

	public SortedSetTuple<Individual> doubleRetrieval(Concept concept) {
		reasoningStartTimeTmp = System.nanoTime();
		SortedSetTuple<Individual> result;
		try {
			result = reasoner.doubleRetrieval(concept);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
		reasoningDurationTmp = System.nanoTime() - reasoningStartTimeTmp;
		otherReasoningTimeNs += reasoningDurationTmp;
		overallReasoningTimeNs += reasoningDurationTmp;
		return result;
	}

	public SortedSetTuple<Individual> doubleRetrieval(Concept concept, Concept adc) {
		reasoningStartTimeTmp = System.nanoTime();
		SortedSetTuple<Individual> result;
		try {
			result = reasoner.doubleRetrieval(concept, adc);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
		reasoningDurationTmp = System.nanoTime() - reasoningStartTimeTmp;
		otherReasoningTimeNs += reasoningDurationTmp;
		overallReasoningTimeNs += reasoningDurationTmp;
		return result;
	}

	// nachher wieder entfernen
	// public static List<Concept> retrievals = new LinkedList<Concept>();	
	
	public SortedSet<Individual> retrieval(Concept concept) {
		// Test, ob tatsächlich keine doppelten Retrievals ausgeführt werden
		// retrievals.add(concept);		
		
		reasoningStartTimeTmp = System.nanoTime();
		SortedSet<Individual> result;
		try {
			result = reasoner.retrieval(concept);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
		nrOfRetrievals++;
		reasoningDurationTmp = System.nanoTime() - reasoningStartTimeTmp;
		retrievalReasoningTimeNs += reasoningDurationTmp;
		overallReasoningTimeNs += reasoningDurationTmp;
		return result;
	}

	public boolean instanceCheck(Concept concept, Individual s) {
		reasoningStartTimeTmp = System.nanoTime();
		boolean result = false;
		try {
			result = reasoner.instanceCheck(concept, s);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
		}
		nrOfInstanceChecks++;
		reasoningDurationTmp = System.nanoTime() - reasoningStartTimeTmp;
		instanceCheckReasoningTimeNs += reasoningDurationTmp;
		overallReasoningTimeNs += reasoningDurationTmp;
		return result;
	}

	public Set<Individual> instanceCheck(Concept concept, Set<Individual> s) {
		reasoningStartTimeTmp = System.nanoTime();
		Set<Individual> result = null;
		try {
			result = reasoner.instanceCheck(concept, s);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
		}
		nrOfInstanceChecks+=s.size();
		nrOfMultiInstanceChecks++;
		reasoningDurationTmp = System.nanoTime() - reasoningStartTimeTmp;
		instanceCheckReasoningTimeNs += reasoningDurationTmp;
		overallReasoningTimeNs += reasoningDurationTmp;
		return result;
	}
	
	// c1 subsummiert c2
	public boolean subsumes(Concept superConcept, Concept subConcept) {
		reasoningStartTimeTmp = System.nanoTime();
		boolean result = false;
		try {
			result = reasoner.subsumes(superConcept, subConcept);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
		}
		nrOfSubsumptionChecks++;
		reasoningDurationTmp = System.nanoTime() - reasoningStartTimeTmp;
		subsumptionReasoningTimeNs += reasoningDurationTmp;
		overallReasoningTimeNs += reasoningDurationTmp;
		return result;
	}

	public Set<Concept> subsumes(Set<Concept> superConcepts, Concept subConcept) {
		reasoningStartTimeTmp = System.nanoTime();
		Set<Concept> result = null;
		try {
			result = reasoner.subsumes(superConcepts, subConcept);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
		}
		nrOfSubsumptionChecks += superConcepts.size();
		nrOfMultiSubsumptionChecks++;
		reasoningDurationTmp = System.nanoTime() - reasoningStartTimeTmp;
		subsumptionReasoningTimeNs += reasoningDurationTmp;
		overallReasoningTimeNs += reasoningDurationTmp;
		return result;		
	}
		
	/*
	// Problem: wie behandle ich Top und Bottom?
	// TODO: schauen wie das in KAON2 gemacht wird
	public SortedSet<Concept> getMoreGeneralConcepts(Concept concept) {
		if(Config.useHierarchyReasonerBenchmarks)
			reasoningStartTimeTmp = System.nanoTime();
		SortedSet<Concept> result;
		try {
			result = reasoner.getMoreGeneralConcepts(concept);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
		if(Config.useHierarchyReasonerBenchmarks) {
			nrOfSubsumptionHierarchyQueries++;
			reasoningDurationTmp = System.nanoTime() - reasoningStartTimeTmp;
			subsumptionHierarchyTimeNs += reasoningDurationTmp;
			overallReasoningTimeNs += reasoningDurationTmp;
		}
		return result;
	}

	public SortedSet<Concept> getMoreSpecialConcepts(Concept concept) {
		if(Config.useHierarchyReasonerBenchmarks)
			reasoningStartTimeTmp = System.nanoTime();
		SortedSet<Concept> result;
		try {
			result = reasoner.getMoreSpecialConcepts(concept);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
		if(Config.useHierarchyReasonerBenchmarks) {
			nrOfSubsumptionHierarchyQueries++;
			reasoningDurationTmp = System.nanoTime() - reasoningStartTimeTmp;
			subsumptionHierarchyTimeNs += reasoningDurationTmp;
			overallReasoningTimeNs += reasoningDurationTmp;
		}
		return result;
	}
	*/
	
	/**
	 * Returns more general concepts in the subsumption hierarchy.
	 * 
	 * @param concept Atomic concept, top, or bottom.
	 * @return A set of more general concepts.
	 */
	public SortedSet<Concept> getMoreGeneralConcepts(Concept concept) {
		return getSubsumptionHierarchy().getMoreGeneralConcepts(concept);
	}

	/**
	 * Returns more special concepts in the subsumption hierarchy.
	 * 
	 * @param concept Atomic concept, top, or bottom.
	 * @return A set of more special concepts.
	 */
	public SortedSet<Concept> getMoreSpecialConcepts(Concept concept) {
		return getSubsumptionHierarchy().getMoreSpecialConcepts(concept);
	}	
	
	/**
	 * Returns more general concepts in the subsumption hierarchy.
	 * 
	 * @see RoleHierarchy#getMoreGeneralRoles(AtomicRole)
	 * @param role Atomic concept, top, or bottom.
	 * @return A set of more general concepts.
	 */
	public SortedSet<AtomicRole> getMoreGeneralRoles(AtomicRole role) {
		return getRoleHierarchy().getMoreGeneralRoles(role);
	}

	/**
	 * Returns more special concepts in the subsumption hierarchy.
	 * 
	 * @see RoleHierarchy#getMoreSpecialRoles(AtomicRole)
	 * @param role Atomic concept, top, or bottom.
	 * @return A set of more special concepts.
	 */
	public SortedSet<AtomicRole> getMoreSpecialRoles(AtomicRole role) {
		return getRoleHierarchy().getMoreSpecialRoles(role);
	}
	
	/**
	 * @see RoleHierarchy#getMostGeneralRoles()
	 * @return The most general roles.
	 */
	public TreeSet<AtomicRole> getMostGeneralRoles() {
		return getRoleHierarchy().getMostGeneralRoles();
	}
	
	/**
	 * @see RoleHierarchy#getMostSpecialRoles()
	 * @return The most special roles.
	 */
	public TreeSet<AtomicRole> getMostSpecialRoles() {
		return getRoleHierarchy().getMostSpecialRoles();
	}	
	
	public SubsumptionHierarchy getSubsumptionHierarchy() {
		try {
			nrOfSubsumptionHierarchyQueries++;
			return reasoner.getSubsumptionHierarchy();
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}	
	}

	public RoleHierarchy getRoleHierarchy() {
		try {
			return reasoner.getRoleHierarchy();
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}
	
	public boolean isSatisfiable() {
		reasoningStartTimeTmp = System.nanoTime();
		boolean result;		
		try {
			result = reasoner.isSatisfiable();
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return false;
		}
		reasoningDurationTmp = System.nanoTime() - reasoningStartTimeTmp;
		otherReasoningTimeNs += reasoningDurationTmp;
		overallReasoningTimeNs += reasoningDurationTmp;
		return result;
	}

	// gibt zu einer Rolle alle Elemente zur�ck
	// private, da es keine Standardoperation ist
	public Map<Individual, SortedSet<Individual>> getRoleMembers(AtomicRole atomicRole) {
		reasoningStartTimeTmp = System.nanoTime();
		Map<Individual, SortedSet<Individual>> result;		
		try {
			result = reasoner.getRoleMembers(atomicRole);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
		reasoningDurationTmp = System.nanoTime() - reasoningStartTimeTmp;
		otherReasoningTimeNs += reasoningDurationTmp;
		overallReasoningTimeNs += reasoningDurationTmp;
		return result;		
	}

	// speichern einer Ontolgie wird speziell behandelt, da kein Reasoning
	public void saveOntology(File file, OntologyFileFormat format) {
		if (getReasonerType() == ReasonerType.KAON2) {
			((KAON2Reasoner) reasoner).saveOntology(file, format);
		} else if (getReasonerType() == ReasonerType.DIG) {
			// DIG erzeugt momentan auch nur einen KAON2-Reasoner und
			// exportiert dann mit der obigen Funktion
			((DIGReasoner) reasoner).saveOntology(file, format);
		} 
	}

	public Set<AtomicConcept> getAtomicConcepts() {
		return reasoner.getAtomicConcepts();
	}

	public Set<AtomicRole> getAtomicRoles() {
		return reasoner.getAtomicRoles();
	}

	public SortedSet<Individual> getIndividuals() {
		return reasoner.getIndividuals();
	}

	public ReasonerType getReasonerType() {
		return reasoner.getReasonerType();
	}

	public List<AtomicConcept> getAtomicConceptsList() {
		return atomicConceptsList;
	}

	public List<AtomicRole> getAtomicRolesList() {
		return atomicRolesList;
	}
	
	public long getInstanceCheckReasoningTimeNs() {
		return instanceCheckReasoningTimeNs;
	}

	public long getRetrievalReasoningTimeNs() {
		return retrievalReasoningTimeNs;
	}

	public int getNrOfInstanceChecks() {
		return nrOfInstanceChecks;
	}

	public int getNrOfRetrievals() {
		return nrOfRetrievals;
	}

	public int getNrOfSubsumptionChecks() {
		return nrOfSubsumptionChecks;
	}

	public long getSubsumptionReasoningTimeNs() {
		return subsumptionReasoningTimeNs;
	}	

	/*
	public long getSubsumptionHierarchyTimeNs() {
		return subsumptionHierarchyTimeNs;
	}
	*/
	public int getNrOfSubsumptionHierarchyQueries() {
		return nrOfSubsumptionHierarchyQueries;
	}
	

	public long getOverallReasoningTimeNs() {
		return overallReasoningTimeNs;
	}
	
	public long getTimePerRetrievalNs() {
		return retrievalReasoningTimeNs/nrOfRetrievals;
	}
	
	public long getTimePerInstanceCheckNs() {
		return instanceCheckReasoningTimeNs/nrOfInstanceChecks;
	}

	public long getTimePerSubsumptionCheckNs() {
		return subsumptionReasoningTimeNs/nrOfSubsumptionChecks;
	}

	/*
	public long getTimePerSubsumptionHierarchyQueryNs() {
		return subsumptionHierarchyTimeNs/nrOfSubsumptionHierarchyQueries;
	}
	*/
	
	// zentrales Exception-Handling
	// das Programm muss unterbrochen werden, da sonst inkorrekte
	// Reasoning-Resultate
	// zur�ckgegeben werden k�nnten
	private void handleExceptions(ReasoningMethodUnsupportedException e) {
		throw new Error("Reasoning method not supported.");
	}

	public int getNrOfMultiSubsumptionChecks() {
		return nrOfMultiSubsumptionChecks;
	}

	public int getNrOfMultiInstanceChecks() {
		return nrOfMultiInstanceChecks;
	}

}
