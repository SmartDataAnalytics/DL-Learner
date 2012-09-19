/**
 * Copyright (C) 2007-2011, Jens Lehmann
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
 */

package org.dllearner.core;

import java.util.Arrays;
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
import org.dllearner.core.owl.Axiom;
import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.core.owl.Constant;
import org.dllearner.core.owl.DataRange;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.DatatypePropertyHierarchy;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Entity;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.fuzzydll.FuzzyIndividual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Nothing;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectPropertyHierarchy;
import org.dllearner.core.owl.Thing;
import org.dllearner.reasoning.ReasonerType;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.datastructures.SortedSetTuple;
import org.dllearner.utilities.owl.ConceptComparator;
import org.dllearner.utilities.owl.OWLVocabulary;
import org.dllearner.utilities.owl.RoleComparator;

/**
 * Abstract component representing a reasoner. Only a few reasoning operations
 * are guaranteed to be implemented by the underlying reasoner, while a
 * {@link ReasoningMethodUnsupportedException} is thrown for all other methods.
 * In addition to calling the actual implementations of reasoning operations,
 * the class also collects statistical information, which can be queried.
 * 
 * Guidelines for extending the class:
 * <ul>
 * <li>add the needed method to the corresponding interface (currenty
 * {@link BaseReasoner}, {@link SchemaReasoner}, {@link IndividualReasoner}
 * exist)</li>
 * <li>reasoning methods which need to be supported by all reasoners: create
 * method() and methodImpl() here, where the former is an overridden, final
 * method delegating to the latter abstract, protected method</li>
 * <li>reasoning method, which do not need to be supported by all reasoners:
 * create method() and methodImpl() as before, but this time methodImpl() is not
 * abstract and throws a {@link ReasoningMethodUnsupportedException} </li>
 * <li>a few very basic methods (where we do not care about statistics) do not
 * have an "Impl" variant, e.g. getting all named classes of a KB; those are
 * directly inherited from the reasoner interface</li>
 * </ul>
 * Note, that the method delegation is done to collect statistical information
 * about reasoning performance, e.g. count how often certain methods were called
 * and how long it took to execute them.
 * 
 * @author Jens Lehmann
 * 
 */
public abstract class AbstractReasonerComponent extends AbstractComponent implements Reasoner, ReasonerComponent {

	public static Logger logger = Logger.getLogger(AbstractReasonerComponent.class);

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
	private ClassHierarchy subsumptionHierarchy = null;
	private ObjectPropertyHierarchy roleHierarchy = null;
	private DatatypePropertyHierarchy datatypePropertyHierarchy = null;

	/**
	 * The underlying knowledge sources.
	 */
	protected Set<KnowledgeSource> sources;


    public AbstractReasonerComponent(){

    }
	/**
	 * Constructs a new reasoner component.
	 * 
	 * @param sources
	 *            The underlying knowledge sources.
	 */
	public AbstractReasonerComponent(Set<KnowledgeSource> sources) {
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

    public void setSources(Set<KnowledgeSource> sources){
        this.sources = sources;
    }
    
    public void setSources(KnowledgeSource... sources) {
    	this.sources = new HashSet<KnowledgeSource>(Arrays.asList(sources));
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
	 * Notify the reasoner component that the underlying knowledge base has
	 * changed and all caches (for named classes, subsumption hierarchies, etc.)
	 * should be invalidaded. TODO Currently, nothing is done to behave
	 * correctly after updates.
	 */
	public void setUpdated() {
		// TODO currently, nothing is done to behave correctly after updates
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
		Set<NamedClass> types = null;
		try {
			types = getTypesImpl(individual);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
		}
		return types;
	}

	protected Set<NamedClass> getTypesImpl(Individual individual)
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException(
				"Reasoner does not support to determine type of individual.");
	}

	@Override
	public final boolean isSuperClassOf(Description superClass, Description subClass) {
		reasoningStartTimeTmp = System.nanoTime();
		boolean result = false;
		try {
			result = isSuperClassOfImpl(superClass, subClass);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
		}
		nrOfSubsumptionChecks++;
		reasoningDurationTmp = System.nanoTime() - reasoningStartTimeTmp;
		subsumptionReasoningTimeNs += reasoningDurationTmp;
		overallReasoningTimeNs += reasoningDurationTmp;
		if(logger.isTraceEnabled()) {
			logger.trace("reasoner query isSuperClassOf: " + superClass + " " + subClass + " " + result);
		}
		return result;
	}

	protected boolean isSuperClassOfImpl(Description superConcept, Description subConcept)
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	@Override
	public final boolean isEquivalentClass(Description class1, Description class2) {
		reasoningStartTimeTmp = System.nanoTime();
		boolean result = false;
		try {
			result = isEquivalentClassImpl(class1, class2);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
		}
		nrOfSubsumptionChecks+=2;
		reasoningDurationTmp = System.nanoTime() - reasoningStartTimeTmp;
		subsumptionReasoningTimeNs += reasoningDurationTmp;
		overallReasoningTimeNs += reasoningDurationTmp;
		if(logger.isTraceEnabled()) {
			logger.trace("reasoner query isEquivalentClass: " + class1 + " " + class2 + " " + result);
		}
		return result;
	}

	protected boolean isEquivalentClassImpl(Description class1, Description class2) throws ReasoningMethodUnsupportedException {
		return isSuperClassOfImpl(class1,class2) && isSuperClassOfImpl(class2,class1);
	}	
	
	@Override
	public Set<Description> getAssertedDefinitions(NamedClass namedClass) {
		try {
			return getAssertedDefinitionsImpl(namedClass);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}
	
	protected Set<Description> getAssertedDefinitionsImpl(NamedClass namedClass)
		throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}	
	
	@Override
	public final Set<Description> isSuperClassOf(Set<Description> superConcepts,
			Description subConcept) {
		reasoningStartTimeTmp = System.nanoTime();
		Set<Description> result = null;
		try {
			result = isSuperClassOfImpl(superConcepts, subConcept);
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

	protected Set<Description> isSuperClassOfImpl(Set<Description> superConcepts,
			Description subConcept) throws ReasoningMethodUnsupportedException {
		Set<Description> returnSet = new HashSet<Description>();
		for (Description superConcept : superConcepts) {
			if (isSuperClassOf(superConcept, subConcept))
				returnSet.add(superConcept);
		}
		return returnSet;
	}

	@Override
	public final SortedSetTuple<Individual> doubleRetrieval(Description concept) {
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

	protected SortedSetTuple<Individual> doubleRetrievalImpl(Description concept)
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	@Override
	public final SortedSet<Individual> getIndividuals(Description concept) {
		reasoningStartTimeTmp = System.nanoTime();
		SortedSet<Individual> result;
		try {
			result = getIndividualsImpl(concept);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
		nrOfRetrievals++;
		reasoningDurationTmp = System.nanoTime() - reasoningStartTimeTmp;
		retrievalReasoningTimeNs += reasoningDurationTmp;
		overallReasoningTimeNs += reasoningDurationTmp;
		if(logger.isTraceEnabled()) {
			logger.trace("reasoner query getIndividuals: " + concept + " " + result);
		}
		return result;
	}

	protected SortedSet<Individual> getIndividualsImpl(Description concept)
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}
	
	@Override
	public final SortedSet<FuzzyIndividual> getFuzzyIndividuals(Description concept) {
		reasoningStartTimeTmp = System.nanoTime();
		SortedSet<FuzzyIndividual> result;
		try {
			result = getFuzzyIndividualsImpl(concept);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
		nrOfRetrievals++;
		reasoningDurationTmp = System.nanoTime() - reasoningStartTimeTmp;
		retrievalReasoningTimeNs += reasoningDurationTmp;
		overallReasoningTimeNs += reasoningDurationTmp;
		if(logger.isTraceEnabled()) {
			logger.trace("reasoner query getIndividuals: " + concept + " " + result);
		}
		return result;
	}

	protected SortedSet<FuzzyIndividual> getFuzzyIndividualsImpl(Description concept)
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	@Override
	public final boolean hasType(Description concept, Individual s) {
		reasoningStartTimeTmp = System.nanoTime();
		boolean result = false;
		try {
			result = hasTypeImpl(concept, s);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
		}
		nrOfInstanceChecks++;
		reasoningDurationTmp = System.nanoTime() - reasoningStartTimeTmp;
		instanceCheckReasoningTimeNs += reasoningDurationTmp;
		overallReasoningTimeNs += reasoningDurationTmp;
		return result;
	}

	protected boolean hasTypeImpl(Description concept, Individual individual)
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	@Override
	public final SortedSet<Individual> hasType(Description concept, Set<Individual> s) {
		// logger.debug("instanceCheck "+concept.toKBSyntaxString());
		reasoningStartTimeTmp = System.nanoTime();
		SortedSet<Individual> result = null;
		try {
			result = hasTypeImpl(concept, s);
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

	protected SortedSet<Individual> hasTypeImpl(Description concept, Set<Individual> individuals)
			throws ReasoningMethodUnsupportedException {
		SortedSet<Individual> returnSet = new TreeSet<Individual>();
		for (Individual individual : individuals) {
			if (hasType(concept, individual))
				returnSet.add(individual);
		}
		return returnSet;
	}

	@Override
	public final Set<NamedClass> getInconsistentClasses() {
		try {
			return getInconsistentClassesImpl();
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	protected Set<NamedClass> getInconsistentClassesImpl()
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	@Override
	public final boolean isSatisfiable() {
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

	protected boolean isSatisfiableImpl() throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	@Override
	public final boolean remainsSatisfiable(Axiom axiom) {
		reasoningStartTimeTmp = System.nanoTime();
		boolean result;
		try {
			result = remainsSatisfiableImpl(axiom);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return false;
		}
		reasoningDurationTmp = System.nanoTime() - reasoningStartTimeTmp;
		otherReasoningTimeNs += reasoningDurationTmp;
		overallReasoningTimeNs += reasoningDurationTmp;
		return result;
	}

	protected boolean remainsSatisfiableImpl(Axiom axiom) throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}	
	
	@Override
	public final Map<ObjectProperty,Set<Individual>> getObjectPropertyRelationships(Individual individual) {
		try {
			return getObjectPropertyRelationshipsImpl(individual);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}		
	}
	
	protected Map<ObjectProperty,Set<Individual>> getObjectPropertyRelationshipsImpl(Individual individual) throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}
			
	
	@Override
	public final Set<Individual> getRelatedIndividuals(Individual individual,
			ObjectProperty objectProperty) {
		try {
			return getRelatedIndividualsImpl(individual, objectProperty);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	protected Set<Individual> getRelatedIndividualsImpl(Individual individual,
			ObjectProperty objectProperty) throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	@Override
	public final Set<Constant> getRelatedValues(Individual individual,
			DatatypeProperty datatypeProperty) {
		try {
			return getRelatedValuesImpl(individual, datatypeProperty);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	protected Set<Constant> getRelatedValuesImpl(Individual individual,
			DatatypeProperty datatypeProperty) throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	@Override
	public final Set<Constant> getLabel(Entity entity) {
		try {
			return getLabelImpl(entity);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	protected Set<Constant> getLabelImpl(Entity entity) throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	@Override
	public final Map<Individual, SortedSet<Individual>> getPropertyMembers(ObjectProperty atomicRole) {
		reasoningStartTimeTmp = System.nanoTime();
		Map<Individual, SortedSet<Individual>> result;
		try {
			result = getPropertyMembersImpl(atomicRole);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
		reasoningDurationTmp = System.nanoTime() - reasoningStartTimeTmp;
		otherReasoningTimeNs += reasoningDurationTmp;
		overallReasoningTimeNs += reasoningDurationTmp;
		return result;
	}

	protected Map<Individual, SortedSet<Individual>> getPropertyMembersImpl(
			ObjectProperty atomicRole) throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	@Override
	public final Map<Individual, SortedSet<Constant>> getDatatypeMembers(
			DatatypeProperty datatypeProperty) {
		try {
			return getDatatypeMembersImpl(datatypeProperty);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	protected Map<Individual, SortedSet<Constant>> getDatatypeMembersImpl(
			DatatypeProperty datatypeProperty) throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	@Override
	public final Map<Individual, SortedSet<Double>> getDoubleDatatypeMembers(
			DatatypeProperty datatypeProperty) {
		try {
			return getDoubleDatatypeMembersImpl(datatypeProperty);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	protected Map<Individual, SortedSet<Double>> getDoubleDatatypeMembersImpl(
			DatatypeProperty datatypeProperty) throws ReasoningMethodUnsupportedException {
		Map<Individual, SortedSet<Constant>> mapping = getDatatypeMembersImpl(datatypeProperty);
		Map<Individual, SortedSet<Double>> ret = new TreeMap<Individual, SortedSet<Double>>();
		for (Entry<Individual, SortedSet<Constant>> e : mapping.entrySet()) {
			SortedSet<Constant> values = e.getValue();
			SortedSet<Double> valuesDouble = new TreeSet<Double>();
			for (Constant c : values) {
				double v = Double.valueOf(c.getLiteral());
				valuesDouble.add(v);
			}
			ret.put(e.getKey(), valuesDouble);
		}
		return ret;
	}

	@Override
	public final Map<Individual, SortedSet<Integer>> getIntDatatypeMembers(
			DatatypeProperty datatypeProperty) {
		try {
			return getIntDatatypeMembersImpl(datatypeProperty);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	protected Map<Individual, SortedSet<Integer>> getIntDatatypeMembersImpl(
			DatatypeProperty datatypeProperty) throws ReasoningMethodUnsupportedException {
		Map<Individual, SortedSet<Constant>> mapping = getDatatypeMembersImpl(datatypeProperty);
		Map<Individual, SortedSet<Integer>> ret = new TreeMap<Individual, SortedSet<Integer>>();
		for (Entry<Individual, SortedSet<Constant>> e : mapping.entrySet()) {
			SortedSet<Constant> values = e.getValue();
			SortedSet<Integer> valuesInt = new TreeSet<Integer>();
			for (Constant c : values) {
				int v = Integer.valueOf(c.getLiteral());
				valuesInt.add(v);
			}
			ret.put(e.getKey(), valuesInt);
		}
		return ret;
	}

	@Override
	public final Map<Individual, SortedSet<Boolean>> getBooleanDatatypeMembers(
			DatatypeProperty datatypeProperty) {
		try {
			return getBooleanDatatypeMembersImpl(datatypeProperty);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	protected Map<Individual, SortedSet<Boolean>> getBooleanDatatypeMembersImpl(
			DatatypeProperty datatypeProperty) throws ReasoningMethodUnsupportedException {
		Map<Individual, SortedSet<Constant>> mapping = getDatatypeMembersImpl(datatypeProperty);
		Map<Individual, SortedSet<Boolean>> ret = new TreeMap<Individual, SortedSet<Boolean>>();
		for (Entry<Individual, SortedSet<Constant>> e : mapping.entrySet()) {
			SortedSet<Constant> values = e.getValue();
			SortedSet<Boolean> valuesBoolean = new TreeSet<Boolean>();
			for (Constant c : values) {
				String s = c.getLiteral();
				if (s.equalsIgnoreCase("true")) {
					valuesBoolean.add(true);
				} else if (s.equalsIgnoreCase("false")) {
					valuesBoolean.add(false);
				} else {
					logger.warn("Requested to parse boolean value of property " + datatypeProperty
							+ ", but " + c + " could not be parsed successfully.");
				}
			}
			ret.put(e.getKey(), valuesBoolean);
		}
		return ret;
	}

	@Override
	public final SortedSet<Individual> getTrueDatatypeMembers(DatatypeProperty datatypeProperty) {
		try {
			return getTrueDatatypeMembersImpl(datatypeProperty);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	protected SortedSet<Individual> getTrueDatatypeMembersImpl(DatatypeProperty datatypeProperty)
			throws ReasoningMethodUnsupportedException {
		Map<Individual, SortedSet<Constant>> mapping = getDatatypeMembersImpl(datatypeProperty);
		SortedSet<Individual> ret = new TreeSet<Individual>();
		for (Entry<Individual, SortedSet<Constant>> e : mapping.entrySet()) {
			SortedSet<Constant> values = e.getValue();
			if (values.size() > 1) {
				logger.warn("Property " + datatypeProperty + " has more than one value " + e.getValue()
						+ " for individual " + e.getKey() + ". We ignore the value.");			
			} else {
				if (values.first().getLiteral().equalsIgnoreCase("true")) {
					ret.add(e.getKey());
				}
			}
		}
		return ret;
	}

	@Override
	public final SortedSet<Individual> getFalseDatatypeMembers(DatatypeProperty datatypeProperty) {
		try {
			return getFalseDatatypeMembersImpl(datatypeProperty);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	protected SortedSet<Individual> getFalseDatatypeMembersImpl(DatatypeProperty datatypeProperty)
			throws ReasoningMethodUnsupportedException {
		Map<Individual, SortedSet<Constant>> mapping = getDatatypeMembersImpl(datatypeProperty);
		SortedSet<Individual> ret = new TreeSet<Individual>();
		for (Entry<Individual, SortedSet<Constant>> e : mapping.entrySet()) {
			SortedSet<Constant> values = e.getValue();
			if (values.size() > 1) {
				logger.warn("Property " + datatypeProperty + " has value " + e.getValue()
						+ ". Cannot determine whether it is false.");
			} else {
				if (values.first().getLiteral().equalsIgnoreCase("false")) {
					ret.add(e.getKey());
				}
			}
		}
		return ret;
	}

	@Override
	public final Map<Individual, SortedSet<String>> getStringDatatypeMembers(
			DatatypeProperty datatypeProperty) {
		try {
			return getStringDatatypeMembersImpl(datatypeProperty);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	protected Map<Individual, SortedSet<String>> getStringDatatypeMembersImpl(
			DatatypeProperty datatypeProperty) throws ReasoningMethodUnsupportedException {
		Map<Individual, SortedSet<Constant>> mapping = getDatatypeMembersImpl(datatypeProperty);
		Map<Individual, SortedSet<String>> ret = new TreeMap<Individual, SortedSet<String>>();
		for (Entry<Individual, SortedSet<Constant>> e : mapping.entrySet()) {
			SortedSet<Constant> values = e.getValue();
			SortedSet<String> valuesString = new TreeSet<String>();
			for (Constant c : values) {
				valuesString.add(c.getLiteral());				
			}
			ret.put(e.getKey(), valuesString);
		}
		return ret;
	}	
	
	@Override
	public final SortedSet<DatatypeProperty> getDatatypeProperties() {
		try {
			return getDatatypePropertiesImpl();
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	protected SortedSet<DatatypeProperty> getDatatypePropertiesImpl()
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	@Override
	public final SortedSet<DatatypeProperty> getBooleanDatatypeProperties() {
		try {
			return getBooleanDatatypePropertiesImpl();
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	// TODO Even if there is a small performance penalty, we could implement
	// the method right here by iterating over all data properties and
	// querying their ranges. At least, this should be done once we have a
	// reasoner independant of OWL API with datatype support.
	protected SortedSet<DatatypeProperty> getBooleanDatatypePropertiesImpl()
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	@Override
	public final SortedSet<DatatypeProperty> getIntDatatypeProperties() {
		try {
			return getIntDatatypePropertiesImpl();
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	protected SortedSet<DatatypeProperty> getIntDatatypePropertiesImpl()
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	@Override
	public final SortedSet<DatatypeProperty> getDoubleDatatypeProperties() {
		try {
			return getDoubleDatatypePropertiesImpl();
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	protected SortedSet<DatatypeProperty> getDoubleDatatypePropertiesImpl()
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	@Override
	public final SortedSet<DatatypeProperty> getStringDatatypeProperties() {
		try {
			return getStringDatatypePropertiesImpl();
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	protected SortedSet<DatatypeProperty> getStringDatatypePropertiesImpl()
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}	
	
	@Override
	public final Description getDomain(ObjectProperty objectProperty) {
		try {
			return getDomainImpl(objectProperty);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	protected Description getDomainImpl(ObjectProperty objectProperty)
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	@Override
	public final Description getDomain(DatatypeProperty datatypeProperty) {
		try {
			return getDomainImpl(datatypeProperty);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	protected Description getDomainImpl(DatatypeProperty datatypeProperty)
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	@Override
	public final Description getRange(ObjectProperty objectProperty) {
		try {
			return getRangeImpl(objectProperty);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	protected Description getRangeImpl(ObjectProperty objectProperty)
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	@Override
	public final DataRange getRange(DatatypeProperty datatypeProperty) {
		try {
			return getRangeImpl(datatypeProperty);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	protected DataRange getRangeImpl(DatatypeProperty datatypeProperty)
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	@Override
	public final SortedSet<Description> getSuperClasses(Description concept) {
		return getClassHierarchy().getSuperClasses(concept);
	}

	protected SortedSet<Description> getSuperClassesImpl(Description concept) throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}
	
	@Override
	public final SortedSet<Description> getSubClasses(Description concept) {
		return getClassHierarchy().getSubClasses(concept);
	}

	protected SortedSet<Description> getSubClassesImpl(Description concept) throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}	
	
	@Override
	public final SortedSet<ObjectProperty> getSuperProperties(ObjectProperty role) {
		return getObjectPropertyHierarchy().getMoreGeneralRoles(role);
	}

	protected SortedSet<ObjectProperty> getSuperPropertiesImpl(ObjectProperty role) throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}	
	
	@Override
	public final SortedSet<ObjectProperty> getSubProperties(ObjectProperty role) {
		return getObjectPropertyHierarchy().getMoreSpecialRoles(role);
	}

	protected SortedSet<ObjectProperty> getSubPropertiesImpl(ObjectProperty role) throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}	
	
	@Override
	public final TreeSet<ObjectProperty> getMostGeneralProperties() {
		return getObjectPropertyHierarchy().getMostGeneralRoles();
	}

//	protected SortedSet<ObjectProperty> getMostGeneralPropertiesImpl(ObjectProperty role) throws ReasoningMethodUnsupportedException {
//		throw new ReasoningMethodUnsupportedException();
//	}	
	
	@Override
	public final TreeSet<ObjectProperty> getMostSpecialProperties() {
		return getObjectPropertyHierarchy().getMostSpecialRoles();
	}

//	protected SortedSet<ObjectProperty> getMostSpecialPropertiesImpl(ObjectProperty role) throws ReasoningMethodUnsupportedException {
//		throw new ReasoningMethodUnsupportedException();
//	}
	
	@Override
	public final SortedSet<DatatypeProperty> getSuperProperties(DatatypeProperty role) {
		return getDatatypePropertyHierarchy().getMoreGeneralRoles(role);
	}

	protected SortedSet<DatatypeProperty> getSuperPropertiesImpl(DatatypeProperty role) throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}		
	
	@Override
	public final SortedSet<DatatypeProperty> getSubProperties(DatatypeProperty role) {
		return getDatatypePropertyHierarchy().getMoreSpecialRoles(role);
	}

	protected SortedSet<DatatypeProperty> getSubPropertiesImpl(DatatypeProperty role) throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}		
	
	@Override
	public final TreeSet<DatatypeProperty> getMostGeneralDatatypeProperties() {
		return getDatatypePropertyHierarchy().getMostGeneralRoles();
	}

	@Override
	public final TreeSet<DatatypeProperty> getMostSpecialDatatypeProperties() {
		return getDatatypePropertyHierarchy().getMostSpecialRoles();
	}

	/**
	 * Creates the class hierarchy. Invoking this method is optional (if not
	 * called explicitly, it is called the first time, it is needed).
	 * 
	 * @return The class hierarchy.
	 * @throws ReasoningMethodUnsupportedException If any method needed to
	 * create the hierarchy is not supported by the underlying reasoner.
	 */
	public final ClassHierarchy prepareSubsumptionHierarchy() throws ReasoningMethodUnsupportedException {
		ConceptComparator conceptComparator = new ConceptComparator();
		TreeMap<Description, SortedSet<Description>> subsumptionHierarchyUp = new TreeMap<Description, SortedSet<Description>>(
				conceptComparator);
		TreeMap<Description, SortedSet<Description>> subsumptionHierarchyDown = new TreeMap<Description, SortedSet<Description>>(
				conceptComparator);

		// parents/children of top ...
		SortedSet<Description> tmp = getSubClassesImpl(Thing.instance);
		subsumptionHierarchyUp.put(Thing.instance, new TreeSet<Description>());
		subsumptionHierarchyDown.put(Thing.instance, tmp);

		// ... bottom ...
		tmp = getSuperClassesImpl(Nothing.instance);
		subsumptionHierarchyUp.put(Nothing.instance, tmp);
		subsumptionHierarchyDown.put(Nothing.instance, new TreeSet<Description>());
		
		// ... and named classes
		Set<NamedClass> atomicConcepts = getNamedClasses();
		for (NamedClass atom : atomicConcepts) {
			tmp = getSubClassesImpl(atom);
			// quality control: we explicitly check that no reasoner implementation returns null here
			if(tmp == null) {
				logger.error("Class hierarchy: getSubClasses returned null instead of empty set."); 
			}			
			subsumptionHierarchyDown.put(atom, tmp);

			tmp = getSuperClassesImpl(atom);
			// quality control: we explicitly check that no reasoner implementation returns null here
			if(tmp == null) {
				logger.error("Class hierarchy: getSuperClasses returned null instead of empty set."); 
			}			
			subsumptionHierarchyUp.put(atom, tmp);
		}		

		return new ClassHierarchy(subsumptionHierarchyUp, subsumptionHierarchyDown);
	}

	@Override
	public final ClassHierarchy getClassHierarchy() {
		// class hierarchy is created on first invocation
		if (subsumptionHierarchy == null) {
			try {
				subsumptionHierarchy = prepareSubsumptionHierarchy();
			} catch (ReasoningMethodUnsupportedException e) {
				handleExceptions(e);
			}
		}
		return subsumptionHierarchy;
	}

	/**
	 * Creates the object property hierarchy. Invoking this method is optional
	 * (if not called explicitly, it is called the first time, it is needed).
	 * 
	 * @return The object property hierarchy.
	 * @throws ReasoningMethodUnsupportedException
	 *             Thrown if a reasoning method for object property 
	 *             hierarchy creation is not supported by the reasoner.
	 */
	public ObjectPropertyHierarchy prepareRoleHierarchy()
			throws ReasoningMethodUnsupportedException {
		
		RoleComparator roleComparator = new RoleComparator();
		TreeMap<ObjectProperty, SortedSet<ObjectProperty>> roleHierarchyUp = new TreeMap<ObjectProperty, SortedSet<ObjectProperty>>(
				roleComparator);
		TreeMap<ObjectProperty, SortedSet<ObjectProperty>> roleHierarchyDown = new TreeMap<ObjectProperty, SortedSet<ObjectProperty>>(
				roleComparator);
 
		Set<ObjectProperty> atomicRoles = getObjectProperties();
		for (ObjectProperty role : atomicRoles) {
			roleHierarchyDown.put(role, getSubPropertiesImpl(role));
			roleHierarchyUp.put(role, getSuperPropertiesImpl(role));
		}

		roleHierarchy = new ObjectPropertyHierarchy(atomicRoles, roleHierarchyUp,
				roleHierarchyDown);
		return roleHierarchy;		
	}

	@Override
	public final ObjectPropertyHierarchy getObjectPropertyHierarchy() {

		try {
			if (roleHierarchy == null) {
				roleHierarchy = prepareRoleHierarchy();
			}
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
		}

		return roleHierarchy;
	}

	/**
	 * Creates the data property hierarchy. Invoking this method is optional (if
	 * not called explicitly, it is called the first time, it is needed).
	 * 
	 * @return The data property hierarchy.
	 * @throws ReasoningMethodUnsupportedException
	 *             Thrown if data property hierarchy creation is not supported
	 *             by the reasoner.
	 */
	public DatatypePropertyHierarchy prepareDatatypePropertyHierarchy()
			throws ReasoningMethodUnsupportedException {
	
		RoleComparator roleComparator = new RoleComparator();
		TreeMap<DatatypeProperty, SortedSet<DatatypeProperty>> datatypePropertyHierarchyUp = new TreeMap<DatatypeProperty, SortedSet<DatatypeProperty>>(
				roleComparator);
		TreeMap<DatatypeProperty, SortedSet<DatatypeProperty>> datatypePropertyHierarchyDown = new TreeMap<DatatypeProperty, SortedSet<DatatypeProperty>>(
				roleComparator);
 
		Set<DatatypeProperty> datatypeProperties = getDatatypeProperties();
		for (DatatypeProperty role : datatypeProperties) {
			datatypePropertyHierarchyDown.put(role, getSubPropertiesImpl(role));
			datatypePropertyHierarchyUp.put(role, getSuperPropertiesImpl(role));
		}

		return new DatatypePropertyHierarchy(datatypeProperties, datatypePropertyHierarchyUp,
				datatypePropertyHierarchyDown);		
	}

	@Override
	public final DatatypePropertyHierarchy getDatatypePropertyHierarchy() {
	
		try {
			if (datatypePropertyHierarchy == null) {
				datatypePropertyHierarchy = prepareDatatypePropertyHierarchy();
			}
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
		}

		return datatypePropertyHierarchy;
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

	@Override
	public String toString() {
		String str = "";
		if (nrOfRetrievals > 0) {
			str += "number of retrievals: " + nrOfRetrievals + "\n";
			str += "retrieval reasoning time: "
							+ Helper.prettyPrintNanoSeconds(retrievalReasoningTimeNs)
							+ " ( " + Helper.prettyPrintNanoSeconds(getTimePerRetrievalNs())
							+ " per retrieval)" + "\n";
		}
		if (nrOfInstanceChecks > 0) {
			str += "number of instance checks: " + nrOfInstanceChecks + " ("
					+ nrOfMultiInstanceChecks + " multiple)\n";
			str += "instance check reasoning time: "
					+ Helper.prettyPrintNanoSeconds(instanceCheckReasoningTimeNs) + " ( "
					+ Helper.prettyPrintNanoSeconds(getTimePerInstanceCheckNs())
					+ " per instance check)\n";
		}
		if (nrOfSubsumptionHierarchyQueries > 0) {
			str += "subsumption hierarchy queries: "
					+ nrOfSubsumptionHierarchyQueries + "\n";
		}
		if (nrOfSubsumptionChecks > 0) {
			str += "(complex) subsumption checks: " + nrOfSubsumptionChecks
					+ " (" + nrOfMultiSubsumptionChecks + " multiple)\n";
			str += "subsumption reasoning time: "
					+ Helper.prettyPrintNanoSeconds(subsumptionReasoningTimeNs) + " ( "
					+ Helper.prettyPrintNanoSeconds(getTimePerSubsumptionCheckNs())
					+ " per subsumption check)\n";
		}
		str += "overall reasoning time: "
				+ Helper.prettyPrintNanoSeconds(overallReasoningTimeNs) + "\n";	
		return str;
	}
	
	/**************************************************************
	 * FUZZY EXTENSIONS
	 **************************************************************/
	
	@Override
	public double hasTypeFuzzyMembership(Description description, FuzzyIndividual individual) {
		reasoningStartTimeTmp = System.nanoTime();
		double result = -1;
		try {
			result = hasTypeFuzzyMembershipImpl(description, individual);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
		}
		nrOfInstanceChecks++;
		reasoningDurationTmp = System.nanoTime() - reasoningStartTimeTmp;
		instanceCheckReasoningTimeNs += reasoningDurationTmp;
		overallReasoningTimeNs += reasoningDurationTmp;
		return result;		
	}

	protected double hasTypeFuzzyMembershipImpl(Description concept, FuzzyIndividual individual)
	throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}
}
