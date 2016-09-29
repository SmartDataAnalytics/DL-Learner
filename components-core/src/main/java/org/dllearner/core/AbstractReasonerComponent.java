/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.dllearner.core.annotations.NoConfigOption;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.core.owl.DatatypePropertyHierarchy;
import org.dllearner.core.owl.ObjectPropertyHierarchy;
import org.dllearner.core.owl.fuzzydll.FuzzyIndividual;
import org.dllearner.reasoning.ReasonerType;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.OWLAPIUtils;
import org.dllearner.utilities.datastructures.SortedSetTuple;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

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

	public static Logger logger = LoggerFactory.getLogger(AbstractReasonerComponent.class);
	
	private static final NumberFormat numberFormat = NumberFormat.getInstance();
	@ConfigOption(description = "whether to use single instance checks", defaultValue = "false")
	protected boolean useInstanceChecks = false;

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
	private List<OWLClass> atomicConceptsList;
	private List<OWLObjectProperty> atomicRolesList;

	// hierarchies (they are computed the first time they are needed)
	@NoConfigOption
	protected ClassHierarchy subsumptionHierarchy = null;
	@NoConfigOption
	protected ObjectPropertyHierarchy roleHierarchy = null;
	@NoConfigOption
	protected DatatypePropertyHierarchy datatypePropertyHierarchy = null;

	@ConfigOption(description = "if class hierarchy should be precomputed", defaultValue = "true")
	protected boolean precomputeClassHierarchy = true;
	@ConfigOption(defaultValue = "true")
	protected boolean precomputeObjectPropertyHierarchy = true;
	@ConfigOption(defaultValue = "true")
	protected boolean precomputeDataPropertyHierarchy = true;
	
	protected OWLDataFactory df = new OWLDataFactoryImpl();
	
	protected Multimap<OWLDatatype, OWLDataProperty> datatype2Properties = HashMultimap.create();
	protected Map<OWLDataProperty, OWLDatatype> dataproperty2datatype = new HashMap<>();

	@ConfigOption(description = "if property domains should be precomputed", defaultValue = "true")
	protected boolean precomputePropertyDomains = true;
	protected Map<OWLProperty, OWLClassExpression> propertyDomains = new HashMap<>();

	@ConfigOption(description = "if object property ranges should be precomputed", defaultValue = "true")
	protected boolean precomputeObjectPropertyRanges = true;
	protected Map<OWLObjectProperty, OWLClassExpression> objectPropertyRanges = new HashMap<>();

	/**
	 * The underlying knowledge sources.
	 */
	@ConfigOption(description = "the underlying knowledge sources", required = true)
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
	
	public AbstractReasonerComponent(KnowledgeSource source) {
		this(Collections.singleton(source));
	}

	/**
	 * Gets the knowledge sources used by this reasoner.
	 * 
	 * @return The underlying knowledge sources.
	 */
	public Set<KnowledgeSource> getSources() {
		return sources;
	}

	@Autowired
    public void setSources(Set<KnowledgeSource> sources){
        this.sources = sources;
    }
    
	@Autowired
    public void setSources(KnowledgeSource... sources) {
    	this.sources = new HashSet<>(Arrays.asList(sources));
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
	@NoConfigOption
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
	public final Set<OWLClass> getTypes(OWLIndividual individual) {
		Set<OWLClass> types = null;
		try {
			types = getTypesImpl(individual);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
		}
		return types;
	}

	protected Set<OWLClass> getTypesImpl(OWLIndividual individual)
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException(
				"Reasoner does not support to determine type of individual.");
	}

	@Override
	public final boolean isSuperClassOf(OWLClassExpression superClass, OWLClassExpression subClass) {
		reasoningStartTimeTmp = System.nanoTime();
		boolean result = false;
		if(precomputeClassHierarchy) {
			if(superClass.isAnonymous() || subClass.isAnonymous()) {
				try {
					result = isSuperClassOfImpl(superClass, subClass);
				} catch (ReasoningMethodUnsupportedException e) {
					e.printStackTrace();
				}
			} else {
				return getClassHierarchy().isSubclassOf(subClass, superClass);
			}
		} else {
			try {
				result = isSuperClassOfImpl(superClass, subClass);
			} catch (ReasoningMethodUnsupportedException e) {
				e.printStackTrace();
			}
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

	protected boolean isSuperClassOfImpl(OWLClassExpression superConcept, OWLClassExpression subConcept)
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	@Override
	public final boolean isEquivalentClass(OWLClassExpression class1, OWLClassExpression class2) {
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

	protected boolean isEquivalentClassImpl(OWLClassExpression class1, OWLClassExpression class2) throws ReasoningMethodUnsupportedException {
		return isSuperClassOfImpl(class1,class2) && isSuperClassOfImpl(class2,class1);
	}	
	
	@Override
	public final boolean isDisjoint(OWLClass class1, OWLClass class2) {
		reasoningStartTimeTmp = System.nanoTime();
		boolean result = false;
		try {
			result = isDisjointImpl(class1, class2);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
		}
		nrOfSubsumptionChecks++;
		reasoningDurationTmp = System.nanoTime() - reasoningStartTimeTmp;
		subsumptionReasoningTimeNs += reasoningDurationTmp;
		overallReasoningTimeNs += reasoningDurationTmp;
		if(logger.isTraceEnabled()) {
			logger.trace("reasoner query isDisjoint: " + class1 + " " + class2 + " " + result);
		}
		return result;
	}

	protected boolean isDisjointImpl(OWLClass superConcept, OWLClass subConcept)
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}
	
	@Override
	public Set<OWLClassExpression> getAssertedDefinitions(OWLClass namedClass) {
		try {
			return getAssertedDefinitionsImpl(namedClass);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}
	
	protected Set<OWLClassExpression> getAssertedDefinitionsImpl(OWLClass namedClass)
		throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}	
	
	@Override
	public final Set<OWLClassExpression> isSuperClassOf(Set<OWLClassExpression> superConcepts,
			OWLClassExpression subConcept) {
		reasoningStartTimeTmp = System.nanoTime();
		Set<OWLClassExpression> result = null;
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

	protected Set<OWLClassExpression> isSuperClassOfImpl(Set<OWLClassExpression> superConcepts,
			OWLClassExpression subConcept) throws ReasoningMethodUnsupportedException {
		Set<OWLClassExpression> returnSet = superConcepts.stream()
				.filter(superConcept -> isSuperClassOf(superConcept, subConcept))
				.collect(Collectors.toSet());
		return returnSet;
	}

	@Override
	public final SortedSetTuple<OWLIndividual> doubleRetrieval(OWLClassExpression concept) {
		reasoningStartTimeTmp = System.nanoTime();
		SortedSetTuple<OWLIndividual> result;
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

	protected SortedSetTuple<OWLIndividual> doubleRetrievalImpl(OWLClassExpression concept)
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	@Override
	public final SortedSet<OWLIndividual> getIndividuals(OWLClassExpression concept) {
		reasoningStartTimeTmp = System.nanoTime();
		SortedSet<OWLIndividual> result;
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

	protected SortedSet<OWLIndividual> getIndividualsImpl(OWLClassExpression concept)
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}
	
	@Override
	public final SortedSet<FuzzyIndividual> getFuzzyIndividuals(OWLClassExpression concept) {
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

	protected SortedSet<FuzzyIndividual> getFuzzyIndividualsImpl(OWLClassExpression concept)
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	@Override
	public final boolean hasType(OWLClassExpression concept, OWLIndividual s) {
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

	protected boolean hasTypeImpl(OWLClassExpression concept, OWLIndividual individual)
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	@Override
	public final SortedSet<OWLIndividual> hasType(OWLClassExpression concept, Set<OWLIndividual> s) {
		// logger.debug("instanceCheck "+concept.toKBSyntaxString());
		reasoningStartTimeTmp = System.nanoTime();
		SortedSet<OWLIndividual> result = null;
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

	protected SortedSet<OWLIndividual> hasTypeImpl(OWLClassExpression concept, Set<OWLIndividual> individuals) throws ReasoningMethodUnsupportedException {
		SortedSet<OWLIndividual> returnSet = individuals.stream()
				.filter(individual -> hasType(concept, individual))
				.collect(Collectors.toCollection(TreeSet::new));
		return returnSet;
	}

	@Override
	public final Set<OWLClass> getInconsistentClasses() {
		try {
			return getInconsistentClassesImpl();
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	protected Set<OWLClass> getInconsistentClassesImpl()
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
	public final boolean remainsSatisfiable(OWLAxiom axiom) {
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

	protected boolean remainsSatisfiableImpl(OWLAxiom axiom) throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}	
	
	@Override
	public final Map<OWLObjectProperty,Set<OWLIndividual>> getObjectPropertyRelationships(OWLIndividual individual) {
		try {
			return getObjectPropertyRelationshipsImpl(individual);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}		
	}
	
	protected Map<OWLObjectProperty,Set<OWLIndividual>> getObjectPropertyRelationshipsImpl(OWLIndividual individual) throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}
	
	@Override
	public final Map<OWLDataProperty, Set<OWLLiteral>> getDataPropertyRelationships(OWLIndividual individual) {
		try {
			return getDataPropertyRelationshipsImpl(individual);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	protected Map<OWLDataProperty, Set<OWLLiteral>> getDataPropertyRelationshipsImpl(OWLIndividual individual)
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}
			
	
	@Override
	public final Set<OWLIndividual> getRelatedIndividuals(OWLIndividual individual,
			OWLObjectProperty objectProperty) {
		try {
			return getRelatedIndividualsImpl(individual, objectProperty);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	protected Set<OWLIndividual> getRelatedIndividualsImpl(OWLIndividual individual,
			OWLObjectProperty objectProperty) throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	@Override
	public final Set<OWLLiteral> getRelatedValues(OWLIndividual individual,
			OWLDataProperty datatypeProperty) {
		try {
			return getRelatedValuesImpl(individual, datatypeProperty);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	protected Set<OWLLiteral> getRelatedValuesImpl(OWLIndividual individual,
			OWLDataProperty datatypeProperty) throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	@Override
	public final Set<OWLLiteral> getLabel(OWLEntity entity) {
		try {
			return getLabelImpl(entity);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	protected Set<OWLLiteral> getLabelImpl(OWLEntity entity) throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	@Override
	public final Map<OWLIndividual, SortedSet<OWLIndividual>> getPropertyMembers(OWLObjectProperty atomicRole) {
		reasoningStartTimeTmp = System.nanoTime();
		Map<OWLIndividual, SortedSet<OWLIndividual>> result;
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

	protected Map<OWLIndividual, SortedSet<OWLIndividual>> getPropertyMembersImpl(
			OWLObjectProperty atomicRole) throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	@Override
	public final Map<OWLIndividual, SortedSet<OWLLiteral>> getDatatypeMembers(
			OWLDataProperty datatypeProperty) {
		try {
			return getDatatypeMembersImpl(datatypeProperty);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	protected Map<OWLIndividual, SortedSet<OWLLiteral>> getDatatypeMembersImpl(
			OWLDataProperty datatypeProperty) throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	@Override
	public final Map<OWLIndividual, SortedSet<Double>> getDoubleDatatypeMembers(
			OWLDataProperty datatypeProperty) {
		try {
			return getDoubleDatatypeMembersImpl(datatypeProperty);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	protected Map<OWLIndividual, SortedSet<Double>> getDoubleDatatypeMembersImpl(
			OWLDataProperty datatypeProperty) throws ReasoningMethodUnsupportedException {
		Map<OWLIndividual, SortedSet<OWLLiteral>> mapping = getDatatypeMembersImpl(datatypeProperty);
		Map<OWLIndividual, SortedSet<Double>> ret = new TreeMap<>();
		for (Entry<OWLIndividual, SortedSet<OWLLiteral>> e : mapping.entrySet()) {
			SortedSet<OWLLiteral> values = e.getValue();
			SortedSet<Double> valuesDouble = values.stream()
					.filter(lit -> OWLAPIUtils.floatDatatypes.contains(lit.getDatatype()))
					.map(lit -> Double.parseDouble(lit.getLiteral()))
					.collect(Collectors.toCollection(TreeSet::new));
			ret.put(e.getKey(), valuesDouble);
		}
		return ret;
	}
	
	@Override
	public final <T extends Number> Map<OWLIndividual, SortedSet<T>> getNumericDatatypeMembers(
			OWLDataProperty datatypeProperty, Class<T> clazz) {
		try {
			return getNumericDatatypeMembersImpl(datatypeProperty, clazz);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}
	
	protected <T extends Number> Map<OWLIndividual, SortedSet<T>> getNumericDatatypeMembersImpl(
			OWLDataProperty datatypeProperty, Class<T> clazz) throws ReasoningMethodUnsupportedException {
		Map<OWLIndividual, SortedSet<OWLLiteral>> mapping = getDatatypeMembersImpl(datatypeProperty);
		Map<OWLIndividual, SortedSet<T>> ret = new TreeMap<>();
		for (Entry<OWLIndividual, SortedSet<OWLLiteral>> e : mapping.entrySet()) {
			SortedSet<OWLLiteral> values = e.getValue();
			SortedSet<T> numericValues = new TreeSet<>();
			for (OWLLiteral lit : values) {
				try {
					numericValues.add((T) numberFormat.parse(lit.getLiteral()));
				} catch (ParseException e1) {
					e1.printStackTrace();
				}
			}
			ret.put(e.getKey(), numericValues);
		}
		return ret;
	}
	
	@Override
	public final <T extends Number & Comparable<T>> Map<OWLIndividual, SortedSet<T>> getNumericDatatypeMembers(
			OWLDataProperty datatypeProperty) {
		try {
			return getNumericDatatypeMembersImpl(datatypeProperty);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}
	
	protected <T extends Number & Comparable<T>> Map<OWLIndividual, SortedSet<T>> getNumericDatatypeMembersImpl(
			OWLDataProperty datatypeProperty) throws ReasoningMethodUnsupportedException {
		Map<OWLIndividual, SortedSet<OWLLiteral>> mapping = getDatatypeMembersImpl(datatypeProperty);
		Map<OWLIndividual, SortedSet<T>> ret = new TreeMap<>();
		for (Entry<OWLIndividual, SortedSet<OWLLiteral>> entry : mapping.entrySet()) {
			OWLIndividual ind = entry.getKey();
			SortedSet<OWLLiteral> values = entry.getValue();
			SortedSet<T> numericValues = new TreeSet<>();
			for (OWLLiteral lit : values) {
				if(OWLAPIUtils.isIntegerDatatype(lit)) {
					numericValues.add((T) Integer.valueOf(lit.parseInteger()));
				} else {
					try {
						Number number;
						String litStr = lit.getLiteral();
						if(litStr.equalsIgnoreCase("NAN")) {
							number = Double.NaN;
						} else {
							number = numberFormat.parse(litStr);
							if(number instanceof Long) {
								number = Double.valueOf(number.toString());
							}
						}
						numericValues.add((T) (number) );
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
				
			}
			ret.put(ind, numericValues);
		}
		return ret;
	}

	@Override
	public final Map<OWLIndividual, SortedSet<Integer>> getIntDatatypeMembers(
			OWLDataProperty datatypeProperty) {
		try {
			return getIntDatatypeMembersImpl(datatypeProperty);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	protected Map<OWLIndividual, SortedSet<Integer>> getIntDatatypeMembersImpl(
			OWLDataProperty datatypeProperty) throws ReasoningMethodUnsupportedException {
		Map<OWLIndividual, SortedSet<OWLLiteral>> mapping = getDatatypeMembersImpl(datatypeProperty);
		Map<OWLIndividual, SortedSet<Integer>> ret = new TreeMap<>();
		for (Entry<OWLIndividual, SortedSet<OWLLiteral>> e : mapping.entrySet()) {
			SortedSet<OWLLiteral> values = e.getValue();
			SortedSet<Integer> valuesInt = values.stream()
					.filter(lit -> OWLAPIUtils.isIntegerDatatype(lit))
					.map((Function<OWLLiteral, Integer>) OWLLiteral::parseInteger)
					.collect(Collectors.toCollection(TreeSet::new));
			ret.put(e.getKey(), valuesInt);
		}
		return ret;
	}

	@Override
	public final Map<OWLIndividual, SortedSet<Boolean>> getBooleanDatatypeMembers(
			OWLDataProperty datatypeProperty) {
		try {
			return getBooleanDatatypeMembersImpl(datatypeProperty);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	protected Map<OWLIndividual, SortedSet<Boolean>> getBooleanDatatypeMembersImpl(
			OWLDataProperty datatypeProperty) throws ReasoningMethodUnsupportedException {
		Map<OWLIndividual, SortedSet<OWLLiteral>> mapping = getDatatypeMembersImpl(datatypeProperty);
		Map<OWLIndividual, SortedSet<Boolean>> ret = new TreeMap<>();
		for (Entry<OWLIndividual, SortedSet<OWLLiteral>> e : mapping.entrySet()) {
			SortedSet<OWLLiteral> values = e.getValue();
			SortedSet<Boolean> valuesBoolean = new TreeSet<>();
			for (OWLLiteral c : values) {
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
	public final SortedSet<OWLIndividual> getTrueDatatypeMembers(OWLDataProperty datatypeProperty) {
		try {
			return getTrueDatatypeMembersImpl(datatypeProperty);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	protected SortedSet<OWLIndividual> getTrueDatatypeMembersImpl(OWLDataProperty datatypeProperty)
			throws ReasoningMethodUnsupportedException {
		Map<OWLIndividual, SortedSet<OWLLiteral>> mapping = getDatatypeMembersImpl(datatypeProperty);
		SortedSet<OWLIndividual> ret = new TreeSet<>();
		for (Entry<OWLIndividual, SortedSet<OWLLiteral>> e : mapping.entrySet()) {
			SortedSet<OWLLiteral> values = e.getValue();
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
	public final SortedSet<OWLIndividual> getFalseDatatypeMembers(OWLDataProperty datatypeProperty) {
		try {
			return getFalseDatatypeMembersImpl(datatypeProperty);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	protected SortedSet<OWLIndividual> getFalseDatatypeMembersImpl(OWLDataProperty datatypeProperty)
			throws ReasoningMethodUnsupportedException {
		Map<OWLIndividual, SortedSet<OWLLiteral>> mapping = getDatatypeMembersImpl(datatypeProperty);
		SortedSet<OWLIndividual> ret = new TreeSet<>();
		for (Entry<OWLIndividual, SortedSet<OWLLiteral>> e : mapping.entrySet()) {
			SortedSet<OWLLiteral> values = e.getValue();
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
	public final Map<OWLIndividual, SortedSet<String>> getStringDatatypeMembers(
			OWLDataProperty datatypeProperty) {
		try {
			return getStringDatatypeMembersImpl(datatypeProperty);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	protected Map<OWLIndividual, SortedSet<String>> getStringDatatypeMembersImpl(
			OWLDataProperty datatypeProperty) throws ReasoningMethodUnsupportedException {
		Map<OWLIndividual, SortedSet<OWLLiteral>> mapping = getDatatypeMembersImpl(datatypeProperty);
		Map<OWLIndividual, SortedSet<String>> ret = new TreeMap<>();
		for (Entry<OWLIndividual, SortedSet<OWLLiteral>> e : mapping.entrySet()) {
			SortedSet<OWLLiteral> values = e.getValue();
			SortedSet<String> valuesString = values.stream()
					.map(OWLLiteral::getLiteral)
					.collect(Collectors.toCollection(TreeSet::new));
			ret.put(e.getKey(), valuesString);
		}
		return ret;
	}
	
	@Override
	public final Set<OWLObjectProperty> getObjectProperties() {
		try {
			return getObjectPropertiesImpl();
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	protected Set<OWLObjectProperty> getObjectPropertiesImpl()
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}
	
	@Override
	public final Set<OWLDataProperty> getDatatypeProperties() {
		try {
			return getDatatypePropertiesImpl();
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	protected Set<OWLDataProperty> getDatatypePropertiesImpl()
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	@Override
	public final Set<OWLDataProperty> getBooleanDatatypeProperties() {
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
	// reasoner independent of OWL API with datatype support.
	protected Set<OWLDataProperty> getBooleanDatatypePropertiesImpl()
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}
	
	@Override
	public final Set<OWLDataProperty> getNumericDataProperties() {
		try {
			return getNumericDataPropertiesImpl();
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	protected Set<OWLDataProperty> getNumericDataPropertiesImpl()
			throws ReasoningMethodUnsupportedException {
		return Sets.union(getIntDatatypePropertiesImpl(), getDoubleDatatypePropertiesImpl());
	}

	@Override
	public final Set<OWLDataProperty> getIntDatatypeProperties() {
		try {
			return getIntDatatypePropertiesImpl();
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	protected Set<OWLDataProperty> getIntDatatypePropertiesImpl()
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	@Override
	public final Set<OWLDataProperty> getDoubleDatatypeProperties() {
		try {
			return getDoubleDatatypePropertiesImpl();
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	protected Set<OWLDataProperty> getDoubleDatatypePropertiesImpl()
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	@Override
	public final Set<OWLDataProperty> getStringDatatypeProperties() {
		try {
			return getStringDatatypePropertiesImpl();
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	protected Set<OWLDataProperty> getStringDatatypePropertiesImpl()
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}	
	
	@Override
	public final OWLClassExpression getDomain(OWLObjectProperty objectProperty) {
		try {
			return getDomainImpl(objectProperty);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	protected OWLClassExpression getDomainImpl(OWLObjectProperty objectProperty)
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	@Override
	public final OWLClassExpression getDomain(OWLDataProperty datatypeProperty) {
		try {
			return getDomainImpl(datatypeProperty);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	protected OWLClassExpression getDomainImpl(OWLDataProperty datatypeProperty)
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	@Override
	public final OWLClassExpression getRange(OWLObjectProperty objectProperty) {
		try {
			return getRangeImpl(objectProperty);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	protected OWLClassExpression getRangeImpl(OWLObjectProperty objectProperty)
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	@Override
	public final OWLDataRange getRange(OWLDataProperty datatypeProperty) {
		try {
			return getRangeImpl(datatypeProperty);
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
			return null;
		}
	}

	protected OWLDataRange getRangeImpl(OWLDataProperty datatypeProperty)
			throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}

	@Override
	public final SortedSet<OWLClassExpression> getSuperClasses(OWLClassExpression concept) {
		if(precomputeClassHierarchy) {
			return getClassHierarchy().getSuperClasses(concept, true);
		} else {
			try {
				return getSuperClassesImpl(concept);
			} catch (ReasoningMethodUnsupportedException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	protected SortedSet<OWLClassExpression> getSuperClassesImpl(OWLClassExpression concept) throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}
	
	@Override
	public final SortedSet<OWLClassExpression> getSubClasses(OWLClassExpression concept) {
		if(precomputeClassHierarchy) {
			return getClassHierarchy().getSubClasses(concept, true);
		} else {
			try {
				return getSubClassesImpl(concept);
			} catch (ReasoningMethodUnsupportedException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	protected SortedSet<OWLClassExpression> getSubClassesImpl(OWLClassExpression concept) throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}
	
	public final SortedSet<OWLClassExpression> getEquivalentClasses(OWLClassExpression concept) {
		return new TreeSet<>(Sets.intersection(getClassHierarchy().getSubClasses(concept), getClassHierarchy().getSuperClasses(concept)));
	}
	
	@Override
	public final <T extends OWLProperty> SortedSet<T> getSuperProperties(T role) {
		if(OWLObjectProperty.class.isInstance(role) && precomputeObjectPropertyHierarchy) {
			return (SortedSet<T>) getObjectPropertyHierarchy().getMoreGeneralRoles((OWLObjectProperty) role);
		} else if(OWLDataProperty.class.isInstance(role) && precomputeDataPropertyHierarchy) {
			return (SortedSet<T>) getDatatypePropertyHierarchy().getMoreGeneralRoles((OWLDataProperty) role);
		} else {
			try {
				return getSuperPropertiesImpl(role);
			} catch (ReasoningMethodUnsupportedException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	protected <T extends OWLProperty> SortedSet<T> getSuperPropertiesImpl(T role) throws ReasoningMethodUnsupportedException {
		if(OWLObjectProperty.class.isInstance(role)) {
			return (SortedSet<T>) getSuperPropertiesImpl((OWLObjectProperty) role);
		} else if(OWLDataProperty.class.isInstance(role)) {
			return (SortedSet<T>) getSuperPropertiesImpl((OWLDataProperty) role);
		}
		throw new ReasoningMethodUnsupportedException();
	}
	
	@Override
	public final <T extends OWLProperty> SortedSet<T> getSubProperties(T role) {
		if(OWLObjectProperty.class.isInstance(role) && precomputeObjectPropertyHierarchy) {
			return (SortedSet<T>) getObjectPropertyHierarchy().getMoreSpecialRoles((OWLObjectProperty) role);
		} else if(OWLDataProperty.class.isInstance(role) && precomputeDataPropertyHierarchy) {
			return (SortedSet<T>) getDatatypePropertyHierarchy().getMoreSpecialRoles((OWLDataProperty) role);
		} else {
			try {
				return getSubPropertiesImpl(role);
			} catch (ReasoningMethodUnsupportedException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	protected <T extends OWLProperty> SortedSet<T> getSubPropertiesImpl(T role) throws ReasoningMethodUnsupportedException {
		if(OWLObjectProperty.class.isInstance(role)) {
			return (SortedSet<T>) getSubPropertiesImpl((OWLObjectProperty) role);
		} else if(OWLDataProperty.class.isInstance(role)) {
			return (SortedSet<T>) getSubPropertiesImpl((OWLDataProperty) role);
		}
		throw new ReasoningMethodUnsupportedException();
	}

	protected <T extends OWLProperty> OWLClassExpression getDomain(T role) {
		if(precomputePropertyDomains) {
			return propertyDomains.get(role);
		} else {
			try {
				return getDomainImpl(role);
			} catch (ReasoningMethodUnsupportedException e) {
				e.printStackTrace();
			}
		}
		throw null;
	}

	protected <T extends OWLProperty> OWLClassExpression getDomainImpl(T role) throws ReasoningMethodUnsupportedException {
		if(OWLObjectProperty.class.isInstance(role)) {
			return getDomainImpl((OWLObjectProperty) role);
		} else if(OWLDataProperty.class.isInstance(role)) {
			return getDomainImpl((OWLDataProperty) role);
		}
		throw new ReasoningMethodUnsupportedException();
	}
	
	
	@Override
	public final SortedSet<OWLObjectProperty> getSuperProperties(OWLObjectProperty role) {
		if(precomputeObjectPropertyHierarchy) {
			return getObjectPropertyHierarchy().getMoreGeneralRoles(role);
		} else {
			try {
				return getSuperPropertiesImpl(role);
			} catch (ReasoningMethodUnsupportedException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	protected SortedSet<OWLObjectProperty> getSuperPropertiesImpl(OWLObjectProperty role) throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}	
	
	@Override
	public final SortedSet<OWLObjectProperty> getSubProperties(OWLObjectProperty role) {
		if(precomputeObjectPropertyHierarchy) {
			return getObjectPropertyHierarchy().getMoreSpecialRoles(role);
		} else {
			try {
				return getSuperPropertiesImpl(role);
			} catch (ReasoningMethodUnsupportedException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	protected SortedSet<OWLObjectProperty> getSubPropertiesImpl(OWLObjectProperty role) throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}	
	
	@Override
	public final SortedSet<OWLObjectProperty> getMostGeneralProperties() {
		return getObjectPropertyHierarchy().getMostGeneralRoles();
	}

//	protected SortedSet<OWLObjectProperty> getMostGeneralPropertiesImpl(OWLOWLObjectProperty role) throws ReasoningMethodUnsupportedException {
//		throw new ReasoningMethodUnsupportedException();
//	}	
	
	@Override
	public final SortedSet<OWLObjectProperty> getMostSpecialProperties() {
		return getObjectPropertyHierarchy().getMostSpecialRoles();
	}

//	protected SortedSet<OWLObjectProperty> getMostSpecialPropertiesImpl(OWLOWLObjectProperty role) throws ReasoningMethodUnsupportedException {
//		throw new ReasoningMethodUnsupportedException();
//	}
	
	@Override
	public final SortedSet<OWLDataProperty> getSuperProperties(OWLDataProperty role) {
		return getDatatypePropertyHierarchy().getMoreGeneralRoles(role);
	}

	protected SortedSet<OWLDataProperty> getSuperPropertiesImpl(OWLDataProperty role) throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}		
	
	@Override
	public final SortedSet<OWLDataProperty> getSubProperties(OWLDataProperty role) {
		return getDatatypePropertyHierarchy().getMoreSpecialRoles(role);
	}

	protected SortedSet<OWLDataProperty> getSubPropertiesImpl(OWLDataProperty role) throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}		
	
	@Override
	public final SortedSet<OWLDataProperty> getMostGeneralDatatypeProperties() {
		return getDatatypePropertyHierarchy().getMostGeneralRoles();
	}

	@Override
	public final SortedSet<OWLDataProperty> getMostSpecialDatatypeProperties() {
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
	public ClassHierarchy prepareSubsumptionHierarchy() throws ReasoningMethodUnsupportedException {
		TreeMap<OWLClassExpression, SortedSet<OWLClassExpression>> subsumptionHierarchyUp = new TreeMap<>(
		);
		TreeMap<OWLClassExpression, SortedSet<OWLClassExpression>> subsumptionHierarchyDown = new TreeMap<>(
		);

		// parents/children of top ...
		SortedSet<OWLClassExpression> tmp = getSubClassesImpl(df.getOWLThing());
		subsumptionHierarchyUp.put(df.getOWLThing(), new TreeSet<>());
		subsumptionHierarchyDown.put(df.getOWLThing(), tmp);

		// ... bottom ...
		tmp = getSuperClassesImpl(df.getOWLNothing());
		subsumptionHierarchyUp.put(df.getOWLNothing(), tmp);
		subsumptionHierarchyDown.put(df.getOWLNothing(), new TreeSet<>());
		
		// ... and named classes
		Set<OWLClass> atomicConcepts = getClasses();
		for (OWLClass atom : atomicConcepts) {
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
	public ObjectPropertyHierarchy prepareObjectPropertyHierarchy()
			throws ReasoningMethodUnsupportedException {
		
		TreeMap<OWLObjectProperty, SortedSet<OWLObjectProperty>> roleHierarchyUp = new TreeMap<>(
		);
		TreeMap<OWLObjectProperty, SortedSet<OWLObjectProperty>> roleHierarchyDown = new TreeMap<>(
		);
 
		Set<OWLObjectProperty> atomicRoles = getObjectProperties();
		for (OWLObjectProperty role : atomicRoles) {
			roleHierarchyDown.put(role, getSubPropertiesImpl(role));
			roleHierarchyUp.put(role, getSuperPropertiesImpl(role));
		}

		roleHierarchy = new ObjectPropertyHierarchy(roleHierarchyUp, roleHierarchyDown);
		return roleHierarchy;		
	}

	@Override
	public final ObjectPropertyHierarchy getObjectPropertyHierarchy() {
		try {
			if (roleHierarchy == null) {
				roleHierarchy = prepareObjectPropertyHierarchy();
			}
		} catch (ReasoningMethodUnsupportedException e) {
			handleExceptions(e);
		}

		return roleHierarchy;
	}
	
	public boolean isSubPropertyOf(OWLProperty subProperty, OWLProperty superProperty){
		if(subProperty.isOWLObjectProperty() && superProperty.isOWLObjectProperty()){
			return getObjectPropertyHierarchy().isSubpropertyOf((OWLObjectProperty)subProperty, (OWLObjectProperty)superProperty);
		} else if(subProperty.isOWLDataProperty() && superProperty.isOWLDataProperty()){
			return getDatatypePropertyHierarchy().isSubpropertyOf((OWLDataProperty)subProperty, (OWLDataProperty)superProperty);
		}
		return false;
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
	
		TreeMap<OWLDataProperty, SortedSet<OWLDataProperty>> datatypePropertyHierarchyUp = new TreeMap<>(
		);
		TreeMap<OWLDataProperty, SortedSet<OWLDataProperty>> datatypePropertyHierarchyDown = new TreeMap<>(
		);
 
		Set<OWLDataProperty> datatypeProperties = getDatatypeProperties();
		for (OWLDataProperty role : datatypeProperties) {
			datatypePropertyHierarchyDown.put(role, getSubPropertiesImpl(role));
			datatypePropertyHierarchyUp.put(role, getSuperPropertiesImpl(role));
		}

		return new DatatypePropertyHierarchy(datatypePropertyHierarchyUp, datatypePropertyHierarchyDown);		
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

	public List<OWLClass> getAtomicConceptsList() {
		if (atomicConceptsList == null)
			atomicConceptsList = new LinkedList<>(getClasses());
		return atomicConceptsList;
	}

	public List<OWLClass> getAtomicConceptsList(boolean removeOWLThing) {
		List<OWLClass> classes = Lists.newArrayList(getAtomicConceptsList());
		if (removeOWLThing) {
			classes.remove(df.getOWLThing());
			classes.remove(df.getOWLNothing());
		}
		return classes;
	}
	
	public void setSubsumptionHierarchy(ClassHierarchy subsumptionHierarchy) {
		this.subsumptionHierarchy = subsumptionHierarchy;
	}

	public List<OWLObjectProperty> getAtomicRolesList() {
		if (atomicRolesList == null)
			atomicRolesList = new LinkedList<>(getObjectProperties());
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
	
	/**
	 * @param precomputeClassHierarchy the precomputeClassHierarchy to set
	 */
	public void setPrecomputeClassHierarchy(boolean precomputeClassHierarchy) {
		this.precomputeClassHierarchy = precomputeClassHierarchy;
	}
	
	/**
	 * @param precomputeObjectPropertyHierarchy the precomputeObjectPropertyHierarchy to set
	 */
	public void setPrecomputeObjectPropertyHierarchy(boolean precomputeObjectPropertyHierarchy) {
		this.precomputeObjectPropertyHierarchy = precomputeObjectPropertyHierarchy;
	}
	
	/**
	 * @param precomputeDataPropertyHierarchy the precomputeDataPropertyHierarchy to set
	 */
	public void setPrecomputeDataPropertyHierarchy(boolean precomputeDataPropertyHierarchy) {
		this.precomputeDataPropertyHierarchy = precomputeDataPropertyHierarchy;
	}
	
	/**
	 * @return all object properties with its domains.
	 */
	public Map<OWLObjectProperty, OWLClassExpression> getObjectPropertyDomains() {
		Map<OWLObjectProperty, OWLClassExpression> result = new HashMap<>();
		
		for (OWLObjectProperty op : getObjectProperties()) {
			OWLClassExpression domain = getDomain(op);
			result.put(op, domain);
		}
		
		return result;
	}
	
	/**
	 * @return all object properties with its range.
	 */
	public Map<OWLObjectProperty, OWLClassExpression> getObjectPropertyRanges() {
		Map<OWLObjectProperty, OWLClassExpression> result = new HashMap<>();
		
		for (OWLObjectProperty op : getObjectProperties()) {
			OWLClassExpression range = getRange(op);
			result.put(op, range);
		}
		
		return result;
	}
	
	/**
	 * @return all data properties with its domains.
	 */
	public Map<OWLDataProperty, OWLClassExpression> getDataPropertyDomains() {
		Map<OWLDataProperty, OWLClassExpression> result = new HashMap<>();
		
		for (OWLDataProperty dp : getDatatypeProperties()) {
			OWLClassExpression domain = getDomain(dp);
			result.put(dp, domain);
		}
		
		return result;
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
	public double hasTypeFuzzyMembership(OWLClassExpression description, FuzzyIndividual individual) {
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

	protected double hasTypeFuzzyMembershipImpl(OWLClassExpression concept, FuzzyIndividual individual)
	throws ReasoningMethodUnsupportedException {
		throw new ReasoningMethodUnsupportedException();
	}
	
	/**
	 * Returns the datatype of the data property, i.e. the range if it is a datatype.
	 * @param dp the data property
	 * @return the datatype of the data property
	 */
	public abstract OWLDatatype getDatatype(OWLDataProperty dp);
	
	/**
	 * Enabled a synchronized mode such that all reasoner methods are supposed
	 * to be thread safe.
	 */
	public abstract void setSynchronized();

	public boolean isUseInstanceChecks() {
		return useInstanceChecks;
	}

	public void setUseInstanceChecks(boolean useInstanceChecks) {
		this.useInstanceChecks = useInstanceChecks;
	}
}