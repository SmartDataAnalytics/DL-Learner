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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.owl.DataRange;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.DatatypePropertyHierarchy;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectPropertyHierarchy;
import org.dllearner.core.owl.SubsumptionHierarchy;
import org.dllearner.reasoning.ReasonerType;
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
	private List<NamedClass> atomicConceptsList;
	private List<ObjectProperty> atomicRolesList;

	// private SortedSet<Concept> retrievalsSet = new TreeSet<Concept>(new ConceptComparator());
	
	private Reasoner reasoner;
	
	/**
	 * Constructs a reasoning service object. Note that you must not 
	 * modify the underlying knowledge base of a reasoning service 
	 * (if you do, you have to create a new reasoning service object).
	 * Further note, that the initialisation is lazy, e.g. you can 
	 * feed the constructor with a non-initialised reasoner component.
	 * However, of course you need to make sure that the resoner component 
	 * is initialised before the first reasoner query. 
	 * 
	 * @param reasoner
	 */
	public ReasoningService(ReasonerComponent reasoner) {
		this.reasoner = reasoner;

		resetStatistics();		
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

	public SortedSetTuple<Individual> doubleRetrieval(Description concept) {
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

	public SortedSetTuple<Individual> doubleRetrieval(Description concept, Description adc) {
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
	
	public SortedSet<Individual> retrieval(Description concept) {
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

	public boolean instanceCheck(Description concept, Individual s) {
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

	public SortedSet<Individual> instanceCheck(Description concept, Set<Individual> s) {
		reasoningStartTimeTmp = System.nanoTime();
		SortedSet<Individual> result = null;
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
	public boolean subsumes(Description superConcept, Description subConcept) {
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

	public Set<Description> subsumes(Set<Description> superConcepts, Description subConcept) {
		reasoningStartTimeTmp = System.nanoTime();
		Set<Description> result = null;
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
	public SortedSet<Description> getMoreGeneralConcepts(Description concept) {
		return getSubsumptionHierarchy().getMoreGeneralConcepts(concept);
	}

	/**
	 * Returns more special concepts in the subsumption hierarchy.
	 * 
	 * @param concept Atomic concept, top, or bottom.
	 * @return A set of more special concepts.
	 */
	public SortedSet<Description> getMoreSpecialConcepts(Description concept) {
		return getSubsumptionHierarchy().getMoreSpecialConcepts(concept);
	}	
	
	/**
	 * Returns more general concepts in the subsumption hierarchy.
	 * 
	 * @see ObjectPropertyHierarchy#getMoreGeneralRoles(ObjectProperty)
	 * @param role Atomic concept, top, or bottom.
	 * @return A set of more general concepts.
	 */
	public SortedSet<ObjectProperty> getMoreGeneralRoles(ObjectProperty role) {
		return getRoleHierarchy().getMoreGeneralRoles(role);
	}

	/**
	 * Returns more special concepts in the subsumption hierarchy.
	 * 
	 * @see ObjectPropertyHierarchy#getMoreSpecialRoles(ObjectProperty)
	 * @param role Atomic concept, top, or bottom.
	 * @return A set of more special concepts.
	 */
	public SortedSet<ObjectProperty> getMoreSpecialRoles(ObjectProperty role) {
		return getRoleHierarchy().getMoreSpecialRoles(role);
	}
	
	/**
	 * @see ObjectPropertyHierarchy#getMostGeneralRoles()
	 * @return The most general roles.
	 */
	public TreeSet<ObjectProperty> getMostGeneralRoles() {
		return getRoleHierarchy().getMostGeneralRoles();
	}
	
	/**
	 * @see ObjectPropertyHierarchy#getMostSpecialRoles()
	 * @return The most special roles.
	 */
	public TreeSet<ObjectProperty> getMostSpecialRoles() {
		return getRoleHierarchy().getMostSpecialRoles();
	}	
	
	/**
	 * Returns more general concepts in the subsumption hierarchy.
	 * 
	 * @see ObjectPropertyHierarchy#getMoreGeneralRoles(ObjectProperty)
	 * @param role Atomic concept, top, or bottom.
	 * @return A set of more general concepts.
	 */
	public SortedSet<DatatypeProperty> getMoreGeneralDatatypeProperties(DatatypeProperty role) {
		return getDatatypePropertyHierarchy().getMoreGeneralRoles(role);
	}

	/**
	 * Returns more special concepts in the subsumption hierarchy.
	 * 
	 * @see ObjectPropertyHierarchy#getMoreSpecialRoles(ObjectProperty)
	 * @param role Atomic concept, top, or bottom.
	 * @return A set of more special concepts.
	 */
	public SortedSet<DatatypeProperty> getMoreSpecialDatatypeProperties(DatatypeProperty role) {
		return getDatatypePropertyHierarchy().getMoreSpecialRoles(role);
	}
	
	/**
	 * @see ObjectPropertyHierarchy#getMostGeneralRoles()
	 * @return The most general roles.
	 */
	public TreeSet<DatatypeProperty> getMostGeneralDatatypeProperties() {
		return getDatatypePropertyHierarchy().getMostGeneralRoles();
	}
	
	/**
	 * @see ObjectPropertyHierarchy#getMostSpecialRoles()
	 * @return The most special roles.
	 */
	public TreeSet<DatatypeProperty> getMostSpecialDatatypeProperties() {
		return getDatatypePropertyHierarchy().getMostSpecialRoles();
	}		
	
	public void prepareSubsumptionHierarchy() {
		reasoner.prepareSubsumptionHierarchy(getAtomicConcepts());
	}
	
	public void prepareSubsumptionHierarchy(Set<NamedClass> allowedConcepts) {
		reasoner.prepareSubsumptionHierarchy(allowedConcepts);
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

	public void prepareRoleHierarchy() {
		prepareRoleHierarchy(getAtomicRoles());
	}
	
	public void prepareRoleHierarchy(Set<ObjectProperty> allowedRoles) {
		try {
			reasoner.prepareRoleHierarchy(allowedRoles);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
		}
	}
	
	public ObjectPropertyHierarchy getRoleHierarchy() {
		try {
			return reasoner.getRoleHierarchy();
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}
	
	public void prepareDatatypePropertyHierarchy() {
		prepareDatatypePropertyHierarchy(getDatatypeProperties());
	}
	
	public void prepareDatatypePropertyHierarchy(Set<DatatypeProperty> allowedRoles) {
		try {
			reasoner.prepareDatatypePropertyHierarchy(allowedRoles);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
		}
	}
	
	public DatatypePropertyHierarchy getDatatypePropertyHierarchy() {
		try {
			return reasoner.getDatatypePropertyHierarchy();
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
	public Map<Individual, SortedSet<Individual>> getRoleMembers(ObjectProperty atomicRole) {
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

	public Set<NamedClass> getAtomicConcepts() {
		return reasoner.getAtomicConcepts();
	}

	public Set<ObjectProperty> getAtomicRoles() {
		return reasoner.getAtomicRoles();
	}

	public SortedSet<DatatypeProperty> getDatatypeProperties() {
		try {
			return reasoner.getDatatypeProperties();
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}
	
	public SortedSet<DatatypeProperty> getBooleanDatatypeProperties() {
		try {
			return reasoner.getBooleanDatatypeProperties();
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}
	
	public SortedSet<DatatypeProperty> getIntDatatypeProperties() {
		try {
			return reasoner.getIntDatatypeProperties();
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}
	
	public SortedSet<DatatypeProperty> getDoubleDatatypeProperties() {
		try {
			return reasoner.getDoubleDatatypeProperties();
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}	
	
	public SortedSet<Individual> getIndividuals() {
		return reasoner.getIndividuals();
	}

	public Description getDomain(ObjectProperty objectProperty) {
		try {
			return reasoner.getDomain(objectProperty);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;			
		}
	}
	
	public Description getDomain(DatatypeProperty datatypeProperty) {
		try {
			return reasoner.getDomain(datatypeProperty);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;			
		}
	}
	
	public Description getRange(ObjectProperty objectProperty) {
		try {
			return reasoner.getRange(objectProperty);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;			
		}		
	}
	
	public DataRange getRange(DatatypeProperty datatypeProperty) {
		try {
			return reasoner.getRange(datatypeProperty);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;			
		}			
	}
	
	public ReasonerType getReasonerType() {
		return reasoner.getReasonerType();
	}

	public List<NamedClass> getAtomicConceptsList() {
		if(atomicConceptsList == null)
			atomicConceptsList = new LinkedList<NamedClass>(getAtomicConcepts());
		return atomicConceptsList;
	}

	public List<ObjectProperty> getAtomicRolesList() {
		if(atomicRolesList == null)
			atomicRolesList = new LinkedList<ObjectProperty>(getAtomicRoles());
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
		e.printStackTrace();
		throw new Error("Reasoning method not supported.");
	}

	public int getNrOfMultiSubsumptionChecks() {
		return nrOfMultiSubsumptionChecks;
	}

	public int getNrOfMultiInstanceChecks() {
		return nrOfMultiInstanceChecks;
	}

}
