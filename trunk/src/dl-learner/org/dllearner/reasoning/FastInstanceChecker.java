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
package org.dllearner.reasoning;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.ReasoningMethodUnsupportedException;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.config.ConfigEntry;
import org.dllearner.core.config.InvalidConfigOptionValueException;
import org.dllearner.core.owl.BooleanValueRestriction;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.DatatypePropertyHierarchy;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Negation;
import org.dllearner.core.owl.Nothing;
import org.dllearner.core.owl.ObjectAllRestriction;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectPropertyExpression;
import org.dllearner.core.owl.ObjectPropertyHierarchy;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.dllearner.core.owl.SubsumptionHierarchy;
import org.dllearner.core.owl.Thing;
import org.dllearner.core.owl.Union;
import org.dllearner.kb.OWLFile;
import org.dllearner.parser.KBParser;
import org.dllearner.parser.ParseException;

/**
 * Reasoner for fast instance checks. It works by completely dematerialising the
 * knowledge base to speed up later reasoning requests. It then continues by
 * only considering one model of the knowledge base (TODO: more explanation),
 * which is neither correct nor complete, but sufficient in many cases. A big
 * advantage of the algorithm is that it does not need even need to perform any
 * set modifications (union, intersection, difference), so it avoids any Java
 * object creation, which makes it extremely fast compared to standard
 * reasoners.
 * 
 * Note: This algorithm works only on concepts in negation normal form!
 * 
 * @author Jens Lehmann
 * 
 */
public class FastInstanceChecker extends ReasonerComponent {

	private static Logger logger = Logger.getLogger(FastInstanceChecker.class);

	private Set<NamedClass> atomicConcepts;
	private Set<ObjectProperty> atomicRoles;
	private SortedSet<DatatypeProperty> datatypeProperties;
	private SortedSet<DatatypeProperty> booleanDatatypeProperties = new TreeSet<DatatypeProperty>();
	private SortedSet<DatatypeProperty> doubleDatatypeProperties = new TreeSet<DatatypeProperty>();
	private SortedSet<DatatypeProperty> intDatatypeProperties = new TreeSet<DatatypeProperty>();
	private SortedSet<Individual> individuals;

	private ReasoningService rs;
	private OWLAPIReasoner rc;
	private Set<KnowledgeSource> sources;

	// we use sorted sets (map indices) here, because they have only log(n)
	// complexity for checking whether an element is contained in them
	// instances of classes
	private Map<NamedClass, SortedSet<Individual>> classInstancesPos = new TreeMap<NamedClass, SortedSet<Individual>>();
	private Map<NamedClass, SortedSet<Individual>> classInstancesNeg = new TreeMap<NamedClass, SortedSet<Individual>>();
	// object property mappings
	private Map<ObjectProperty, Map<Individual, SortedSet<Individual>>> opPos = new TreeMap<ObjectProperty, Map<Individual, SortedSet<Individual>>>();
	// datatype property mappings
	// (for booleans we assume that just one mapping exists, e.g.
	// hasValue(object,true) and hasValue(object,false) will
	// lead to undefined behaviour (they are logical contradictions)
	private Map<DatatypeProperty, SortedSet<Individual>> bd = new TreeMap<DatatypeProperty, SortedSet<Individual>>();
	// for int and double we assume that a property can have several values,
	// althoug this should be rare,
	// e.g. hasValue(object,2) and hasValue(object,3)
	private Map<DatatypeProperty, Map<Individual, SortedSet<Double>>> dd = new TreeMap<DatatypeProperty, Map<Individual, SortedSet<Double>>>();
	private Map<DatatypeProperty, Map<Individual, SortedSet<Integer>>> id = new TreeMap<DatatypeProperty, Map<Individual, SortedSet<Integer>>>();

	public FastInstanceChecker(Set<KnowledgeSource> sources) {
		this.sources = sources;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.Component#applyConfigEntry(org.dllearner.core.config.ConfigEntry)
	 */
	@Override
	public <T> void applyConfigEntry(ConfigEntry<T> entry) throws InvalidConfigOptionValueException {

	}

	public static String getName() {
		return "fast instance checker";
	}	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.Component#init()
	 */
	@Override
	public void init() throws ComponentInitException {
		rc = new OWLAPIReasoner(sources);
		rc.init();

		try {
			atomicConcepts = rc.getAtomicConcepts();
			datatypeProperties = rc.getDatatypeProperties();
			booleanDatatypeProperties = rc.getBooleanDatatypeProperties();
			doubleDatatypeProperties = rc.getDoubleDatatypeProperties();
			intDatatypeProperties = rc.getIntDatatypeProperties();
			atomicRoles = rc.getAtomicRoles();
			individuals = rc.getIndividuals();

			rs = new ReasoningService(rc);

			// TODO: some code taken from Helper.createFlatABox, but pasted here
			// because additional things need to
			// be done (maybe this can be merge again with the
			// FastRetrievalReasoner later)
			long dematStartTime = System.currentTimeMillis();

			for (NamedClass atomicConcept : rs.getAtomicConcepts()) {
				classInstancesPos.put(atomicConcept, rs.retrieval(atomicConcept));
				Negation negatedAtomicConcept = new Negation(atomicConcept);
				classInstancesNeg.put(atomicConcept, rs.retrieval(negatedAtomicConcept));
			}

			for (ObjectProperty atomicRole : atomicRoles) {
				opPos.put(atomicRole, rc.getRoleMembers(atomicRole));
			}

			for (DatatypeProperty dp : booleanDatatypeProperties) {
				bd.put(dp, rc.getTrueDatatypeMembers(dp));
			}

			for (DatatypeProperty dp : intDatatypeProperties) {
				id.put(dp, rc.getIntDatatypeMembers(dp));
			}			
			
			for (DatatypeProperty dp : doubleDatatypeProperties) {
				dd.put(dp, rc.getDoubleDatatypeMembers(dp));
			}
			
			long dematDuration = System.currentTimeMillis() - dematStartTime;
			logger.info("TBox dematerialised in " + dematDuration + " ms");

		} catch (ReasoningMethodUnsupportedException e) {
			throw new ComponentInitException(
					"Underlying reasoner does not support all necessary reasoning methods.", e);
		}
	}

	@Override
	public boolean instanceCheck(Description description, Individual individual)
			throws ReasoningMethodUnsupportedException {
		if (description instanceof NamedClass) {
			return classInstancesPos.get((NamedClass) description).contains(individual);
		} else if (description instanceof Negation) {
			Description child = description.getChild(0);
			if (child instanceof NamedClass) {
				return classInstancesNeg.get((NamedClass) child).contains(individual);
			} else {
				throw new ReasoningMethodUnsupportedException("Instance check for description "
						+ description
						+ " unsupported. Description needs to be in negation normal form.");
			}
		} else if (description instanceof Thing) {
			return true;
		} else if (description instanceof Nothing) {
			return false;
		} else if (description instanceof Union) {
			// if the individual is instance of any of the subdescription of
			// the union, we return true
			List<Description> children = description.getChildren();
			for (Description child : children) {
				if (instanceCheck(child, individual))
					return true;
			}
			return false;
		} else if (description instanceof Intersection) {
			// if the individual is instance of all of the subdescription of
			// the union, we return true
			List<Description> children = description.getChildren();
			for (Description child : children) {
				if (!instanceCheck(child, individual))
					return false;
			}
			return true;
		} else if (description instanceof ObjectSomeRestriction) {
			ObjectPropertyExpression ope = ((ObjectSomeRestriction) description).getRole();
			if (!(ope instanceof ObjectProperty))
				throw new ReasoningMethodUnsupportedException("Instance check for description "
						+ description + " unsupported. Inverse object properties not supported.");
			ObjectProperty op = (ObjectProperty) ope;
			Description child = description.getChild(0);
			Map<Individual, SortedSet<Individual>> mapping = opPos.get(op);
			;
			if (mapping == null) {
				logger.warn("Instance check of a description with an undefinied property (" + op
						+ ").");
				return false;
			}
			SortedSet<Individual> roleFillers = opPos.get(op).get(individual);
			if (roleFillers == null)
				return false;
			for (Individual roleFiller : roleFillers) {
				if (instanceCheck(child, roleFiller))
					return true;
			}
			return false;
		} else if (description instanceof ObjectAllRestriction) {
			ObjectPropertyExpression ope = ((ObjectAllRestriction) description).getRole();
			if (!(ope instanceof ObjectProperty))
				throw new ReasoningMethodUnsupportedException("Instance check for description "
						+ description + " unsupported. Inverse object properties not supported.");
			ObjectProperty op = (ObjectProperty) ope;
			Description child = description.getChild(0);
			Map<Individual, SortedSet<Individual>> mapping = opPos.get(op);
			;
			if (mapping == null) {
				logger.warn("Instance check of a description with an undefinied property (" + op
						+ ").");
				return true;
			}
			SortedSet<Individual> roleFillers = opPos.get(op).get(individual);
			if (roleFillers == null)
				return true;
			for (Individual roleFiller : roleFillers) {
				if (!instanceCheck(child, roleFiller))
					return false;
			}
			return true;
		} else if (description instanceof BooleanValueRestriction) {
			DatatypeProperty dp = ((BooleanValueRestriction)description).getRestrictedPropertyExpresssion();
			boolean value = ((BooleanValueRestriction)description).getBooleanValue();
			
			if(value) {
				// check whether the individual is in the set of individuals mapped
				// to true by this datatype property
				return bd.get(dp).contains(individual);
			} else {
				return !bd.get(dp).contains(individual);
			}
		}

		throw new ReasoningMethodUnsupportedException("Instance check for description "
				+ description + " unsupported.");
	}

	@Override
	public SortedSet<Individual> retrieval(Description concept) {
		return rs.retrieval(concept);
	}	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.Reasoner#getAtomicConcepts()
	 */
	public Set<NamedClass> getAtomicConcepts() {
		return atomicConcepts;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.Reasoner#getAtomicRoles()
	 */
	public Set<ObjectProperty> getAtomicRoles() {
		return atomicRoles;
	}

	@Override
	public SortedSet<DatatypeProperty> getDatatypeProperties() {
		return datatypeProperties;
	}

	@Override
	public SortedSet<DatatypeProperty> getBooleanDatatypeProperties() {
		return booleanDatatypeProperties;
	}

	@Override
	public SortedSet<DatatypeProperty> getDoubleDatatypeProperties() {
		return doubleDatatypeProperties;
	}

	@Override
	public SortedSet<DatatypeProperty> getIntDatatypeProperties() {
		return intDatatypeProperties;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.Reasoner#getIndividuals()
	 */
	public SortedSet<Individual> getIndividuals() {
		return individuals;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.Reasoner#getReasonerType()
	 */
	public ReasonerType getReasonerType() {
		return ReasonerType.FAST_INSTANCE_CHECKER;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.Reasoner#prepareSubsumptionHierarchy(java.util.Set)
	 */
	public void prepareSubsumptionHierarchy(Set<NamedClass> allowedConcepts) {
		rs.prepareSubsumptionHierarchy();
	}

	@Override
	public SubsumptionHierarchy getSubsumptionHierarchy() {
		return rs.getSubsumptionHierarchy();
	}

	@Override
	public void prepareRoleHierarchy(Set<ObjectProperty> allowedRoles) {
		rs.prepareRoleHierarchy(allowedRoles);
	}

	@Override
	public ObjectPropertyHierarchy getRoleHierarchy() {
		return rs.getRoleHierarchy();
	}

	@Override
	public void prepareDatatypePropertyHierarchy(Set<DatatypeProperty> allowedRoles) {
		rs.prepareDatatypePropertyHierarchy(allowedRoles);
	}

	@Override
	public DatatypePropertyHierarchy getDatatypePropertyHierarchy() {
		return rs.getDatatypePropertyHierarchy();
	}
	
	@Override
	public boolean subsumes(Description superConcept, Description subConcept) {
		// Negation neg = new Negation(subConcept);
		// Intersection c = new Intersection(neg,superConcept);
		// return fastRetrieval.calculateSets(c).getPosSet().isEmpty();
		return rs.subsumes(superConcept, subConcept);
	}

	/**
	 * Test method for fast instance checker.
	 * 
	 * @param args
	 *            No arguments supported.
	 * @throws ComponentInitException
	 * @throws ParseException
	 * @throws ReasoningMethodUnsupportedException
	 */
	public static void main(String[] args) throws ComponentInitException, ParseException,
			ReasoningMethodUnsupportedException {
		ComponentManager cm = ComponentManager.getInstance();
		OWLFile owl = cm.knowledgeSource(OWLFile.class);
		String owlFile = new File("examples/family/father.owl").toURI().toString();
		cm.applyConfigEntry(owl, "url", owlFile);
		owl.init();
		ReasonerComponent reasoner = cm.reasoner(FastInstanceChecker.class, owl);
		cm.reasoningService(reasoner);
		reasoner.init();

		KBParser.internalNamespace = "http://example.com/father#";
		String query = "(male AND EXISTS hasChild.TOP)";
		Description d = KBParser.parseConcept(query);
		System.out.println(d);
		Individual i = new Individual("http://example.com/father#markus");
		System.out.println(reasoner.instanceCheck(d, i));
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Reasoner#getBaseURI()
	 */
	public String getBaseURI() {
		return rc.getBaseURI();
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Reasoner#getPrefixes()
	 */
	public Map<String, String> getPrefixes() {
		return rc.getPrefixes();
	}

	@Override
	public Description getDomain(ObjectProperty objectProperty) {
		return rc.getDomain(objectProperty);
	}
	
	@Override
	public Description getDomain(DatatypeProperty datatypeProperty) {
		return rc.getDomain(datatypeProperty);
	}
	
	@Override
	public Description getRange(ObjectProperty objectProperty) {
		return rc.getRange(objectProperty);
	}	
	
}
