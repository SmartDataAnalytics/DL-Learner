/**
 * Copyright (C) 2007-2008, Jens Lehmann
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.dllearner.core.owl.Constant;
import org.dllearner.core.owl.DataRange;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.DatatypePropertyHierarchy;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Entity;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectPropertyHierarchy;
import org.dllearner.core.owl.SubsumptionHierarchy;
import org.dllearner.reasoning.ReasonerType;
import org.dllearner.utilities.datastructures.SortedSetTuple;
import org.dllearner.utilities.owl.OWLVocabulary;

/**
 * Abstract component representing a reasoner. Only a few reasoning operations
 * are guaranteed to be implemented by the underlying reasoner, while a
 * {@link ReasoningMethodUnsupportedException} is thrown for all other methods.
 * In addition to calling the actual implementations of reasoning operations,
 * the class also collects statistical information, which can be queried.
 * 
 * Guidelines for extending the class:
 * <ul>
 *   <li>add the needed method to the corresponding interface (currenty
 *   {@link BaseReasoner}, {@link SchemaReasoner}, {@link IndividualReasoner}
 *   exist)</li>
 *   <li>reasoning methods which need to be supported by all reasoners: 
 *   create method() and methodImpl() here, where the former is an overridden, final 
 *   method delegating to the latter abstract, protected method</li>
 *   <li>reasoning method, which do not need to be supported by all reasoners:
 *   create method() and methodImpl() as before, but this time methodImpl() is
 *   not abstract and throws a {@link ReasoningMethodUnsupportedException} 
 *   </li>
 * </ul>
 * Note, that the method delegation is done to collect statistical information
 * about reasoning performance, e.g. count how often certain methods were called
 * and how long it took to execute them.
 * 
 * @author Jens Lehmann
 * 
 */
public abstract class ReasonerComponent extends Component implements Reasoner {

	public static Logger logger = Logger.getLogger(ReasonerComponent.class);

	// statistical data for particular reasoning operations
	private long instanceCheckReasoningTimeNs = 0;
	private int nrOfInstanceChecks = 0;
	private int nrOfMultiInstanceChecks = 0;
	private long retrievalReasoningTimeNs = 0;
	private int nrOfRetrievals = 0;
	private long subsumptionReasoningTimeNs = 0;
	private int nrOfSubsumptionChecks = 0;
	private int nrOfMultiSubsumptionChecks = 0;
	private int nrOfSubsumptionHierarchyQueries = 0;

	// rest of reasoning time
	private long otherReasoningTimeNs = 0;

	// time for all reasoning requests (usually longer than the sum of all
	// above)
	private long overallReasoningTimeNs = 0;

	// temporary variables (moved here for performance reasons)
	private long reasoningStartTimeTmp;
	private long reasoningDurationTmp;

	// list view
	private List<NamedClass> atomicConceptsList;
	private List<ObjectProperty> atomicRolesList;

	// hierarchies (they are computed the first time they are needed)
	private SubsumptionHierarchy subsumptionHierarchy;
	private ObjectPropertyHierarchy roleHierarchy;
	private DatatypePropertyHierarchy datatypePropertyHierarchy;

	/**
	 * The underlying knowledge sources.
	 */
	protected Set<KnowledgeSource> sources;

	/**
	 * Constructs a new reasoner component.
	 * 
	 * @param sources
	 *            The underlying knowledge sources.
	 */
	public ReasonerComponent(Set<KnowledgeSource> sources) {
		this.sources = sources;
	}

	/**
	 * Gets the knowledge sources used by this reasoner.
	 * 
	 * @return The underlying knowledge sources.
	 */
	public Set<KnowledgeSource> getSources() {
		return sources;
	}

	/**
	 * Method to exchange the reasoner underlying the learning problem.
	 * Implementations, which do not only use the provided sources class
	 * variable, must make sure that a call to this method indeed changes them.
	 * 
	 * @param sources
	 *            The new knowledge sources.
	 */
	public void changeSources(Set<KnowledgeSource> sources) {
		this.sources = sources;
	}

	/**
	 * Gets the type of the underlying reasoner. Although rarely necessary,
	 * applications can use this to adapt their behaviour to the reasoner.
	 * 
	 * @return The reasoner type.
	 */
	public abstract ReasonerType getReasonerType();

	/**
	 * Reset all statistics. Usually, you do not need to call this. However, if
	 * you e.g. perform benchmarks of learning algorithms and performing
	 * reasoning operations, such as a consistency check, before starting the
	 * algorithm, you can use this method to reset all statistical values.
	 */
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

	/**
	 * Call this method to release the knowledge base. Not calling the method
	 * may (depending on the underlying reasoner) result in resources for this
	 * knowledge base not being freed, which can cause memory leaks.
	 */
	public abstract void releaseKB();

	// we cannot expect callers of reasoning methods to reliably recover if
	// certain reasoning methods are not implemented by the backend; we also
	// should not require callers to build catch clauses each time they make
	// a reasoner request => for this reasoner, we throw a runtime exception
	// here
	private void handleExceptions(ReasoningMethodUnsupportedException e) {
		e.printStackTrace();
		throw new RuntimeException("Reasoning method not supported.", e);
	}

	@Override
	public final Set<NamedClass> getTypes(Individual individual) {
		return getTypesImpl(individual);
	}
	
	protected abstract Set<NamedClass> getTypesImpl(Individual individual);
	
	@Override
	public final boolean subsumes(Description superClass, Description subClass) {
		reasoningStartTimeTmp = System.nanoTime();
		boolean result = false;
		try {
			result = subsumesImpl(superClass, subClass);
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
			result = subsumesImpl(superConcepts, subConcept);
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
	
	public SortedSetTuple<Individual> doubleRetrieval(Description concept) {
		reasoningStartTimeTmp = System.nanoTime();
		SortedSetTuple<Individual> result;
		try {
			result = doubleRetrievalImpl(concept);
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
			result = doubleRetrievalImpl(concept, adc);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
		reasoningDurationTmp = System.nanoTime() - reasoningStartTimeTmp;
		otherReasoningTimeNs += reasoningDurationTmp;
		overallReasoningTimeNs += reasoningDurationTmp;
		return result;
	}

	public SortedSet<Individual> retrieval(Description concept) {
		reasoningStartTimeTmp = System.nanoTime();
		SortedSet<Individual> result;
		try {
			result = retrievalImpl(concept);
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
			result = instanceCheckImpl(concept, s);
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
		// logger.debug("instanceCheck "+concept.toKBSyntaxString());
		reasoningStartTimeTmp = System.nanoTime();
		SortedSet<Individual> result = null;
		try {
			result = instanceCheckImpl(concept, s);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
		}
		nrOfInstanceChecks += s.size();
		nrOfMultiInstanceChecks++;
		reasoningDurationTmp = System.nanoTime() - reasoningStartTimeTmp;
		instanceCheckReasoningTimeNs += reasoningDurationTmp;
		overallReasoningTimeNs += reasoningDurationTmp;
		// logger.debug("instanceCheck done");
		return result;
	}

	/**
	 * Returns more general concepts in the subsumption hierarchy.
	 * 
	 * @param concept
	 *            Atomic concept, top, or bottom.
	 * @return A set of more general concepts.
	 */
	public SortedSet<Description> getMoreGeneralConcepts(Description concept) {
		return getSubsumptionHierarchy().getMoreGeneralConcepts(concept);
	}

	/**
	 * Returns more special concepts in the subsumption hierarchy.
	 * 
	 * @param concept
	 *            Atomic concept, top, or bottom.
	 * @return A set of more special concepts.
	 */
	public SortedSet<Description> getMoreSpecialConcepts(Description concept) {
		return getSubsumptionHierarchy().getMoreSpecialConcepts(concept);
	}

	/**
	 * Returns more general concepts in the subsumption hierarchy.
	 * 
	 * @see ObjectPropertyHierarchy#getMoreGeneralRoles(ObjectProperty)
	 * @param role
	 *            Atomic concept, top, or bottom.
	 * @return A set of more general concepts.
	 */
	public SortedSet<ObjectProperty> getMoreGeneralRoles(ObjectProperty role) {
		return getRoleHierarchy().getMoreGeneralRoles(role);
	}

	/**
	 * Returns more special concepts in the subsumption hierarchy.
	 * 
	 * @see ObjectPropertyHierarchy#getMoreSpecialRoles(ObjectProperty)
	 * @param role
	 *            Atomic concept, top, or bottom.
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
	 * @param role
	 *            Atomic concept, top, or bottom.
	 * @return A set of more general concepts.
	 */
	public SortedSet<DatatypeProperty> getMoreGeneralDatatypeProperties(DatatypeProperty role) {
		return getDatatypePropertyHierarchy().getMoreGeneralRoles(role);
	}

	/**
	 * Returns more special concepts in the subsumption hierarchy.
	 * 
	 * @see ObjectPropertyHierarchy#getMoreSpecialRoles(ObjectProperty)
	 * @param role
	 *            Atomic concept, top, or bottom.
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

	protected void prepareSubsumptionHierarchy() throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException(
				"Subsumption hierarchy creation not supported by this reasoner.");
	}

	public SubsumptionHierarchy getSubsumptionHierarchy() {
		if (subsumptionHierarchy == null) {
			try {
				prepareSubsumptionHierarchy();
			} catch (ReasoningMethodUnsupportedException e) {
				handleExceptions(e);
			}
		}
		return subsumptionHierarchy;
	}

	protected void prepareRoleHierarchy() throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException(
				"Object property hierarchy creation not supported by this reasoner.");
	}

	public ObjectPropertyHierarchy getRoleHierarchy() {
		if (roleHierarchy == null) {
			try {
				prepareRoleHierarchy();
			} catch (ReasoningMethodUnsupportedException e) {
				handleExceptions(e);
			}
		}
		return roleHierarchy;
	}

	protected void prepareDatatypePropertyHierarchy() throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException(
				"Datatype property hierarchy creation not supported by this reasoner.");
	}

	public DatatypePropertyHierarchy getDatatypePropertyHierarchy() {
		if (datatypePropertyHierarchy == null) {
			try {
				prepareDatatypePropertyHierarchy();
			} catch (ReasoningMethodUnsupportedException e) {
				handleExceptions(e);
			}
		}
		return datatypePropertyHierarchy;
	}

	public boolean isSatisfiable() {
		reasoningStartTimeTmp = System.nanoTime();
		boolean result;
		try {
			result = isSatisfiableImpl();
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return false;
		}
		reasoningDurationTmp = System.nanoTime() - reasoningStartTimeTmp;
		otherReasoningTimeNs += reasoningDurationTmp;
		overallReasoningTimeNs += reasoningDurationTmp;
		return result;
	}

	public Set<Individual> getRelatedIndividuals(Individual individual,
			ObjectProperty objectProperty) throws ReasoningMethodUnsupportedException {
		try {
			return getRelatedIndividualsImpl(individual, objectProperty);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	public Set<Constant> getRelatedValues(Individual individual, DatatypeProperty datatypeProperty)
			throws ReasoningMethodUnsupportedException {
		try {
			return getRelatedValuesImpl(individual, datatypeProperty);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	public Set<Constant> getLabel(Entity entity) throws ReasoningMethodUnsupportedException {
		try {
			return getLabelImpl(entity);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	public Map<Individual, SortedSet<Individual>> getRoleMembers(ObjectProperty atomicRole) {
		reasoningStartTimeTmp = System.nanoTime();
		Map<Individual, SortedSet<Individual>> result;
		try {
			result = getRoleMembersImpl(atomicRole);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
		reasoningDurationTmp = System.nanoTime() - reasoningStartTimeTmp;
		otherReasoningTimeNs += reasoningDurationTmp;
		overallReasoningTimeNs += reasoningDurationTmp;
		return result;
	}

	public abstract boolean hasDatatypeSupport();

	public Map<Individual, SortedSet<Double>> getDoubleDatatypeMembers(
			DatatypeProperty datatypeProperty) {
		try {
			return getDoubleDatatypeMembersImpl(datatypeProperty);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	public Map<Individual, SortedSet<Integer>> getIntDatatypeMembers(
			DatatypeProperty datatypeProperty) {
		try {
			return getIntDatatypeMembersImpl(datatypeProperty);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	public SortedSet<Individual> getTrueDatatypeMembers(DatatypeProperty datatypeProperty) {
		try {
			return getTrueDatatypeMembersImpl(datatypeProperty);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	public SortedSet<Individual> getFalseDatatypeMembers(DatatypeProperty datatypeProperty) {
		try {
			return getFalseDatatypeMembersImpl(datatypeProperty);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	public SortedSet<DatatypeProperty> getDatatypeProperties() {
		try {
			return getDatatypePropertiesImpl();
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	public SortedSet<DatatypeProperty> getBooleanDatatypeProperties() {
		try {
			return getBooleanDatatypePropertiesImpl();
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	public SortedSet<DatatypeProperty> getIntDatatypeProperties() {
		try {
			return getIntDatatypePropertiesImpl();
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	public SortedSet<DatatypeProperty> getDoubleDatatypeProperties() {
		try {
			return getDoubleDatatypePropertiesImpl();
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	public Description getDomain(ObjectProperty objectProperty) {
		try {
			return getDomainImpl(objectProperty);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	public Description getDomain(DatatypeProperty datatypeProperty) {
		try {
			return getDomainImpl(datatypeProperty);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	public Description getRange(ObjectProperty objectProperty) {
		try {
			return getRangeImpl(objectProperty);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	public DataRange getRange(DatatypeProperty datatypeProperty) {
		try {
			return getRangeImpl(datatypeProperty);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	public List<NamedClass> getAtomicConceptsList() {
		if (atomicConceptsList == null)
			atomicConceptsList = new LinkedList<NamedClass>(getNamedClasses());
		return atomicConceptsList;
	}

	public List<NamedClass> getAtomicConceptsList(boolean removeOWLThing) {
		if (!removeOWLThing) {
			return getAtomicConceptsList();
		} else {
			List<NamedClass> l = new LinkedList<NamedClass>();
			for (NamedClass class1 : getAtomicConceptsList()) {
				if (class1.compareTo(new NamedClass(OWLVocabulary.OWL_NOTHING)) == 0
						|| class1.compareTo(new NamedClass(OWLVocabulary.OWL_THING)) == 0) {
					;// do nothing
				} else {
					l.add(class1);
				}
			}
			return l;
		}

	}

	public List<ObjectProperty> getAtomicRolesList() {
		if (atomicRolesList == null)
			atomicRolesList = new LinkedList<ObjectProperty>(getObjectProperties());
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
	 * public long getSubsumptionHierarchyTimeNs() { return
	 * subsumptionHierarchyTimeNs; }
	 */
	public int getNrOfSubsumptionHierarchyQueries() {
		return nrOfSubsumptionHierarchyQueries;
	}

	public long getOverallReasoningTimeNs() {
		return overallReasoningTimeNs;
	}

	public long getTimePerRetrievalNs() {
		return retrievalReasoningTimeNs / nrOfRetrievals;
	}

	public long getTimePerInstanceCheckNs() {
		return instanceCheckReasoningTimeNs / nrOfInstanceChecks;
	}

	public long getTimePerSubsumptionCheckNs() {
		return subsumptionReasoningTimeNs / nrOfSubsumptionChecks;
	}

	public int getNrOfMultiSubsumptionChecks() {
		return nrOfMultiSubsumptionChecks;
	}

	public int getNrOfMultiInstanceChecks() {
		return nrOfMultiInstanceChecks;
	}

	public boolean subsumesImpl(Description superConcept, Description subConcept)
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	public Set<Description> subsumesImpl(Description superConcept, Set<Description> subConcepts)
			throws ReasoningMethodUnsupportedException {
		Set<Description> returnSet = new HashSet<Description>();
		for (Description subConcept : subConcepts) {
			if (subsumes(superConcept, subConcept))
				returnSet.add(subConcept);
		}
		return returnSet;
	}

	public Set<Description> subsumesImpl(Set<Description> superConcepts, Description subConcept)
			throws ReasoningMethodUnsupportedException {
		Set<Description> returnSet = new HashSet<Description>();
		for (Description superConcept : superConcepts) {
			if (subsumes(superConcept, subConcept))
				returnSet.add(superConcept);
		}
		return returnSet;
	}

	public SortedSet<Individual> retrievalImpl(Description concept)
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	public Set<Individual> getRelatedIndividualsImpl(Individual individual,
			ObjectProperty objectProperty) throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	public Set<Constant> getRelatedValuesImpl(Individual individual,
			DatatypeProperty datatypeProperty) throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	public Set<Constant> getLabelImpl(Entity entity) throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	public Map<Individual, SortedSet<Individual>> getRoleMembersImpl(ObjectProperty atomicRole)
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	public Map<Individual, SortedSet<Constant>> getDatatypeMembersImpl(
			DatatypeProperty datatypeProperty) throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	// convenience method to get int value mappings of a datatype property
	public Map<Individual, SortedSet<Integer>> getIntDatatypeMembersImpl(
			DatatypeProperty datatypeProperty) throws ReasoningMethodUnsupportedException {
		Map<Individual, SortedSet<Constant>> mapping = getDatatypeMembersImpl(datatypeProperty);
		Map<Individual, SortedSet<Integer>> ret = new TreeMap<Individual, SortedSet<Integer>>();
		for (Entry<Individual, SortedSet<Constant>> e : mapping.entrySet()) {
			SortedSet<Constant> values = e.getValue();
			SortedSet<Integer> valuesInt = new TreeSet<Integer>();
			for (Constant c : values) {
				int v = Integer.parseInt(c.getLiteral());
				valuesInt.add(v);
			}
			ret.put(e.getKey(), valuesInt);
		}
		return ret;
	}

	// convenience method to get double value mappings of a datatype property
	public Map<Individual, SortedSet<Double>> getDoubleDatatypeMembersImpl(
			DatatypeProperty datatypeProperty) throws ReasoningMethodUnsupportedException {
		Map<Individual, SortedSet<Constant>> mapping = getDatatypeMembersImpl(datatypeProperty);
		Map<Individual, SortedSet<Double>> ret = new TreeMap<Individual, SortedSet<Double>>();
		for (Entry<Individual, SortedSet<Constant>> e : mapping.entrySet()) {
			SortedSet<Constant> values = e.getValue();
			SortedSet<Double> valuesDouble = new TreeSet<Double>();
			for (Constant c : values) {
				double v = Double.parseDouble(c.getLiteral());
				valuesDouble.add(v);
			}
			ret.put(e.getKey(), valuesDouble);
		}
		return ret;
	}

	// convenience method to get boolean value mappings of a datatype property
	public Map<Individual, SortedSet<Boolean>> getBooleanDatatypeMembersImpl(
			DatatypeProperty datatypeProperty) throws ReasoningMethodUnsupportedException {
		Map<Individual, SortedSet<Constant>> mapping = getDatatypeMembersImpl(datatypeProperty);
		Map<Individual, SortedSet<Boolean>> ret = new TreeMap<Individual, SortedSet<Boolean>>();
		for (Entry<Individual, SortedSet<Constant>> e : mapping.entrySet()) {
			SortedSet<Constant> values = e.getValue();
			SortedSet<Boolean> valuesBoolean = new TreeSet<Boolean>();
			for (Constant c : values) {
				boolean v = Boolean.parseBoolean(c.getLiteral());
				valuesBoolean.add(v);
			}
			ret.put(e.getKey(), valuesBoolean);
		}
		return ret;
	}

	// convenience method returning those values which have value "true" for
	// this
	// datatype property
	public SortedSet<Individual> getTrueDatatypeMembersImpl(DatatypeProperty datatypeProperty)
			throws ReasoningMethodUnsupportedException {
		Map<Individual, SortedSet<Constant>> mapping = getDatatypeMembersImpl(datatypeProperty);
		SortedSet<Individual> ret = new TreeSet<Individual>();
		for (Entry<Individual, SortedSet<Constant>> e : mapping.entrySet()) {
			SortedSet<Constant> values = e.getValue();
			for (Constant c : values) {
				boolean v = Boolean.parseBoolean(c.getLiteral());
				if (v == true)
					ret.add(e.getKey());
			}
		}
		return ret;
	}

	// convenience method returning those values which have value "false" for
	// this
	// datatype property
	public SortedSet<Individual> getFalseDatatypeMembersImpl(DatatypeProperty datatypeProperty)
			throws ReasoningMethodUnsupportedException {
		Map<Individual, SortedSet<Constant>> mapping = getDatatypeMembersImpl(datatypeProperty);
		SortedSet<Individual> ret = new TreeSet<Individual>();
		for (Entry<Individual, SortedSet<Constant>> e : mapping.entrySet()) {
			SortedSet<Constant> values = e.getValue();
			for (Constant c : values) {
				boolean v = Boolean.parseBoolean(c.getLiteral());
				if (v == false)
					ret.add(e.getKey());
			}
		}
		return ret;
	}

	public boolean instanceCheckImpl(Description concept, Individual individual)
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	public SortedSet<Individual> instanceCheckImpl(Description concept, Set<Individual> individuals)
			throws ReasoningMethodUnsupportedException {
		SortedSet<Individual> returnSet = new TreeSet<Individual>();
		for (Individual individual : individuals) {
			if (instanceCheck(concept, individual))
				returnSet.add(individual);
		}
		return returnSet;
	}

	public SortedSetTuple<Individual> doubleRetrievalImpl(Description concept)
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	public SortedSetTuple<Individual> doubleRetrievalImpl(Description concept, Description adc)
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	public boolean isSatisfiableImpl() throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	public SubsumptionHierarchy getSubsumptionHierarchyImpl()
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	public void prepareRoleHierarchyImpl(Set<ObjectProperty> allowedRoles)
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	public void prepareDatatypePropertyHierarchyImpl(Set<DatatypeProperty> allowedDatatypeProperties)
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	public ObjectPropertyHierarchy getRoleHierarchyImpl()
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	public DatatypePropertyHierarchy getDatatypePropertyHierarchyImpl()
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	public Description getDomainImpl(ObjectProperty objectProperty)
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	public Description getDomainImpl(DatatypeProperty datatypeProperty)
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	public Description getRangeImpl(ObjectProperty objectProperty)
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	public DataRange getRangeImpl(DatatypeProperty datatypeProperty)
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	public SortedSet<DatatypeProperty> getDatatypePropertiesImpl()
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	public SortedSet<DatatypeProperty> getBooleanDatatypePropertiesImpl()
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	public SortedSet<DatatypeProperty> getDoubleDatatypePropertiesImpl()
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	public SortedSet<DatatypeProperty> getIntDatatypePropertiesImpl()
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	public Set<NamedClass> getInconsistentClassesImpl() throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

}
