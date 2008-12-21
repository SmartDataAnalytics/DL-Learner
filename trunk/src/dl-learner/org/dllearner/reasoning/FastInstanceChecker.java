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
import java.util.Collection;
import java.util.LinkedList;
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
import org.dllearner.core.configurators.ComponentFactory;
import org.dllearner.core.configurators.FastInstanceCheckerConfigurator;
import org.dllearner.core.options.BooleanConfigOption;
import org.dllearner.core.options.ConfigEntry;
import org.dllearner.core.options.ConfigOption;
import org.dllearner.core.options.InvalidConfigOptionValueException;
import org.dllearner.core.options.StringConfigOption;
import org.dllearner.core.owl.BooleanValueRestriction;
import org.dllearner.core.owl.Constant;
import org.dllearner.core.owl.DataRange;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.DatatypeSomeRestriction;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.DoubleMaxValue;
import org.dllearner.core.owl.DoubleMinValue;
import org.dllearner.core.owl.Entity;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Negation;
import org.dllearner.core.owl.Nothing;
import org.dllearner.core.owl.ObjectAllRestriction;
import org.dllearner.core.owl.ObjectCardinalityRestriction;
import org.dllearner.core.owl.ObjectMaxCardinalityRestriction;
import org.dllearner.core.owl.ObjectMinCardinalityRestriction;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectPropertyExpression;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.dllearner.core.owl.ObjectValueRestriction;
import org.dllearner.core.owl.Thing;
import org.dllearner.core.owl.Union;
import org.dllearner.kb.OWLFile;
import org.dllearner.parser.KBParser;
import org.dllearner.parser.ParseException;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.owl.ConceptTransformation;

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

//	private boolean defaultNegation = true;

	private FastInstanceCheckerConfigurator configurator;

	@Override
	public FastInstanceCheckerConfigurator getConfigurator() {
		return configurator;
	}

	private Set<NamedClass> atomicConcepts;
	private Set<ObjectProperty> atomicRoles;
	private SortedSet<DatatypeProperty> datatypeProperties;
	private SortedSet<DatatypeProperty> booleanDatatypeProperties = new TreeSet<DatatypeProperty>();
	private SortedSet<DatatypeProperty> doubleDatatypeProperties = new TreeSet<DatatypeProperty>();
	private SortedSet<DatatypeProperty> intDatatypeProperties = new TreeSet<DatatypeProperty>();
	private SortedSet<Individual> individuals;

	// private ReasonerComponent rs;
	private OWLAPIReasoner rc;

	// we use sorted sets (map indices) here, because they have only log(n)
	// complexity for checking whether an element is contained in them
	// instances of classes
	private Map<NamedClass, SortedSet<Individual>> classInstancesPos = new TreeMap<NamedClass, SortedSet<Individual>>();
	private Map<NamedClass, SortedSet<Individual>> classInstancesNeg = new TreeMap<NamedClass, SortedSet<Individual>>();
	// object property mappings
	private Map<ObjectProperty, Map<Individual, SortedSet<Individual>>> opPos = new TreeMap<ObjectProperty, Map<Individual, SortedSet<Individual>>>();
	// datatype property mappings
	// we have one mapping for true and false for efficiency reasons
	private Map<DatatypeProperty, SortedSet<Individual>> bdPos = new TreeMap<DatatypeProperty, SortedSet<Individual>>();
	private Map<DatatypeProperty, SortedSet<Individual>> bdNeg = new TreeMap<DatatypeProperty, SortedSet<Individual>>();
	// for int and double we assume that a property can have several values,
	// althoug this should be rare,
	// e.g. hasValue(object,2) and hasValue(object,3)
	private Map<DatatypeProperty, Map<Individual, SortedSet<Double>>> dd = new TreeMap<DatatypeProperty, Map<Individual, SortedSet<Double>>>();
	private Map<DatatypeProperty, Map<Individual, SortedSet<Integer>>> id = new TreeMap<DatatypeProperty, Map<Individual, SortedSet<Integer>>>();

	/**
	 * Creates an instance of the fast instance checker.
	 * @param sources The knowledge sources used as input.
	 */
	public FastInstanceChecker(Set<KnowledgeSource> sources) {
		super(sources);
		this.configurator = new FastInstanceCheckerConfigurator(this);
	}

	/**
	 * @return The options of this component.
	 */
	public static Collection<ConfigOption<?>> createConfigOptions() {
		Collection<ConfigOption<?>> options = new LinkedList<ConfigOption<?>>();
		StringConfigOption type = new StringConfigOption("reasonerType",
				"FaCT++ or Pellet to dematerialize", "pellet", false, true);
		type.setAllowedValues(new String[] { "fact", "pellet" });
		// closure option? see:
		// http://owlapi.svn.sourceforge.net/viewvc/owlapi/owl1_1/trunk/tutorial/src/main/java/uk/ac/manchester/owl/tutorial/examples/ClosureAxiomsExample.java?view=markup
		options.add(type);
		options.add(new BooleanConfigOption("defaultNegation", "Whether to use default negation, i.e. an instance not being in a class means that it is in the negation of the class.", true, false, true));
		return options;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.Component#applyConfigEntry(org.dllearner.core.config.ConfigEntry)
	 */
	@Override
	public <T> void applyConfigEntry(ConfigEntry<T> entry) throws InvalidConfigOptionValueException {
	}

	/**
	 * @return The name of this component.
	 */
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
		// rc = new OWLAPIReasoner(sources);
		rc = ComponentFactory.getOWLAPIReasoner(sources);
		rc.getConfigurator().setReasonerType(configurator.getReasonerType());
		rc.init();

//		try {
			atomicConcepts = rc.getNamedClasses();
			datatypeProperties = rc.getDatatypeProperties();
			booleanDatatypeProperties = rc.getBooleanDatatypeProperties();
			doubleDatatypeProperties = rc.getDoubleDatatypeProperties();
			intDatatypeProperties = rc.getIntDatatypeProperties();
			atomicRoles = rc.getObjectProperties();
			individuals = rc.getIndividuals();

			// rs = new ReasonerComponent(rc);

			// TODO: some code taken from Helper.createFlatABox, but pasted here
			// because additional things need to
			// be done (maybe this can be merge again with the
			// FastRetrievalReasoner later)
			long dematStartTime = System.currentTimeMillis();

			logger.debug("dematerialising concepts");

			for (NamedClass atomicConcept : rc.getNamedClasses()) {

				SortedSet<Individual> pos = rc.getIndividuals(atomicConcept);
				classInstancesPos.put(atomicConcept, pos);

				if (configurator.getDefaultNegation()) {
					classInstancesNeg.put(atomicConcept, Helper.difference(individuals, pos));
				} else {
					// Pellet needs approximately infinite time to answer
					// negated queries
					// on the carcinogenesis data set (and probably others), so
					// we have to
					// be careful here
					Negation negatedAtomicConcept = new Negation(atomicConcept);
					classInstancesNeg.put(atomicConcept, rc.getIndividuals(negatedAtomicConcept));
				}

			}

			logger.debug("dematerialising object properties");

			for (ObjectProperty atomicRole : atomicRoles) {
				opPos.put(atomicRole, rc.getPropertyMembers(atomicRole));
			}

			logger.debug("dematerialising datatype properties");

			for (DatatypeProperty dp : booleanDatatypeProperties) {
				bdPos.put(dp, rc.getTrueDatatypeMembers(dp));
				bdNeg.put(dp, rc.getFalseDatatypeMembers(dp));
			}

			for (DatatypeProperty dp : intDatatypeProperties) {
				id.put(dp, rc.getIntDatatypeMembers(dp));
			}

			for (DatatypeProperty dp : doubleDatatypeProperties) {
				dd.put(dp, rc.getDoubleDatatypeMembers(dp));
			}

			long dematDuration = System.currentTimeMillis() - dematStartTime;
			logger.debug("TBox dematerialised in " + dematDuration + " ms");

//		} catch (ReasoningMethodUnsupportedException e) {
//			throw new ComponentInitException(
//					"Underlying reasoner does not support all necessary reasoning methods.", e);
//		}
	}

	@Override
	public boolean hasTypeImpl(Description description, Individual individual)
			throws ReasoningMethodUnsupportedException {

		// System.out.println(description + " " + individual);

		if (description instanceof NamedClass) {
			return classInstancesPos.get((NamedClass) description).contains(individual);
		} else if (description instanceof Negation) {
			Description child = description.getChild(0);
			if (child instanceof NamedClass) {
				return classInstancesNeg.get((NamedClass) child).contains(individual);
			} else {
				// default negation
				if(configurator.getDefaultNegation()) {
					return !hasTypeImpl(child, individual);
				} else {
					logger.debug("Converting description to negation normal form in fast instance check (should be avoided if possible).");
					Description nnf = ConceptTransformation.transformToNegationNormalForm(child);
					return hasTypeImpl(nnf, individual);					
				}
//				throw new ReasoningMethodUnsupportedException("Instance check for description "
//						+ description
//						+ " unsupported. Description needs to be in negation normal form.");
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
				if (hasType(child, individual)) {
					return true;
				}
			}
			return false;
		} else if (description instanceof Intersection) {
			// if the individual is instance of all of the subdescription of
			// the union, we return true
			List<Description> children = description.getChildren();
			for (Description child : children) {
				if (!hasType(child, individual)) {
					return false;
				}
			}
			return true;
		} else if (description instanceof ObjectSomeRestriction) {
			ObjectPropertyExpression ope = ((ObjectSomeRestriction) description).getRole();
			if (!(ope instanceof ObjectProperty)) {
				throw new ReasoningMethodUnsupportedException("Instance check for description "
						+ description + " unsupported. Inverse object properties not supported.");
			}
			ObjectProperty op = (ObjectProperty) ope;
			Description child = description.getChild(0);
			Map<Individual, SortedSet<Individual>> mapping = opPos.get(op);

			if (mapping == null) {
				logger.warn("Instance check of a description with an undefinied property (" + op
						+ ").");
				return false;
			}
			SortedSet<Individual> roleFillers = opPos.get(op).get(individual);
			if (roleFillers == null) {
				return false;
			}
			for (Individual roleFiller : roleFillers) {
				if (hasType(child, roleFiller)) {
					return true;
				}
			}
			return false;
		} else if (description instanceof ObjectAllRestriction) {
			ObjectPropertyExpression ope = ((ObjectAllRestriction) description).getRole();
			if (!(ope instanceof ObjectProperty)) {
				throw new ReasoningMethodUnsupportedException("Instance check for description "
						+ description + " unsupported. Inverse object properties not supported.");
			}
			ObjectProperty op = (ObjectProperty) ope;
			Description child = description.getChild(0);
			Map<Individual, SortedSet<Individual>> mapping = opPos.get(op);

			if (mapping == null) {
				logger.warn("Instance check of a description with an undefinied property (" + op
						+ ").");
				return true;
			}
			SortedSet<Individual> roleFillers = opPos.get(op).get(individual);
			if (roleFillers == null) {
				return true;
			}
			for (Individual roleFiller : roleFillers) {
				if (!hasType(child, roleFiller)) {
					return false;
				}
			}
			return true;
		} else if (description instanceof ObjectMinCardinalityRestriction) {
			ObjectPropertyExpression ope = ((ObjectCardinalityRestriction) description).getRole();
			if (!(ope instanceof ObjectProperty)) {
				throw new ReasoningMethodUnsupportedException("Instance check for description "
						+ description + " unsupported. Inverse object properties not supported.");
			}
			ObjectProperty op = (ObjectProperty) ope;
			Description child = description.getChild(0);
			Map<Individual, SortedSet<Individual>> mapping = opPos.get(op);

			if (mapping == null) {
				logger.warn("Instance check of a description with an undefinied property (" + op
						+ ").");
				return true;
			}

			int number = ((ObjectCardinalityRestriction) description).getNumber();
			int nrOfFillers = 0;

			SortedSet<Individual> roleFillers = opPos.get(op).get(individual);
			// special case: there are always at least zero fillers
			if (number == 0) {
				return true;
			}
			// return false if there are none or not enough role fillers
			if (roleFillers == null || roleFillers.size() < number) {
				return false;
			}

			int index = 0;
			for (Individual roleFiller : roleFillers) {
				index++;
				if (hasType(child, roleFiller)) {
					nrOfFillers++;
					if (nrOfFillers == number) {
						return true;
					}
					// earyl abort: e.g. >= 10 hasStructure.Methyl;
					// if there are 11 fillers and 2 are not Methyl, the result
					// is false
				} else {
					if (roleFillers.size() - index < number) {
						return false;
					}
				}
			}
			return false;
		} else if (description instanceof ObjectMaxCardinalityRestriction) {
			ObjectPropertyExpression ope = ((ObjectCardinalityRestriction) description).getRole();
			if (!(ope instanceof ObjectProperty)) {
				throw new ReasoningMethodUnsupportedException("Instance check for description "
						+ description + " unsupported. Inverse object properties not supported.");
			}
			ObjectProperty op = (ObjectProperty) ope;
			Description child = description.getChild(0);
			Map<Individual, SortedSet<Individual>> mapping = opPos.get(op);

			if (mapping == null) {
				logger.warn("Instance check of a description with an undefinied property (" + op
						+ ").");
				return true;
			}

			int number = ((ObjectCardinalityRestriction) description).getNumber();
			int nrOfFillers = 0;

			SortedSet<Individual> roleFillers = opPos.get(op).get(individual);
			// return true if there are none or not enough role fillers
			if (roleFillers == null || roleFillers.size() < number) {
				return true;
			}

			int index = 0;
			for (Individual roleFiller : roleFillers) {
				index++;
				if (hasType(child, roleFiller)) {
					nrOfFillers++;
					if (nrOfFillers > number) {
						return false;
					}
					// early abort: e.g. <= 5 hasStructure.Methyl;
					// if there are 6 fillers and 2 are not Methyl, the result
					// is true
				} else {
					if (roleFillers.size() - index <= number) {
						return true;
					}
				}
			}
			return true;
		} else if (description instanceof ObjectValueRestriction) {
			Individual i = ((ObjectValueRestriction)description).getIndividual();
			ObjectProperty op = (ObjectProperty) ((ObjectValueRestriction)description).getRestrictedPropertyExpression();
			
			return opPos.get(op).get(individual).contains(i);
		} else if (description instanceof BooleanValueRestriction) {
			DatatypeProperty dp = ((BooleanValueRestriction) description)
					.getRestrictedPropertyExpresssion();
			boolean value = ((BooleanValueRestriction) description).getBooleanValue();

			if (value) {
				// check whether the individual is in the set of individuals
				// mapped
				// to true by this datatype property
				return bdPos.get(dp).contains(individual);
			} else {
				return bdNeg.get(dp).contains(individual);
			}
		} else if (description instanceof DatatypeSomeRestriction) {
			DatatypeSomeRestriction dsr = (DatatypeSomeRestriction) description;
			DatatypeProperty dp = (DatatypeProperty) dsr.getRestrictedPropertyExpression();
			DataRange dr = dsr.getDataRange();
			SortedSet<Double> values = dd.get(dp).get(individual);

			// if there is no filler for this individual and property we
			// need to return false
			if (values == null) {
				return false;
			}

			if (dr instanceof DoubleMaxValue) {
				return (values.first() <= ((DoubleMaxValue) dr).getValue());
			} else if (dr instanceof DoubleMinValue) {
				return (values.last() >= ((DoubleMinValue) dr).getValue());
			}
		}

		throw new ReasoningMethodUnsupportedException("Instance check for description "
				+ description + " unsupported.");
	}

	@Override
	public SortedSet<Individual> getIndividualsImpl(Description concept)
			throws ReasoningMethodUnsupportedException {
		if (concept instanceof NamedClass) {
			return classInstancesPos.get((NamedClass) concept);
		} else if (concept instanceof Negation && concept.getChild(0) instanceof NamedClass) {
			return classInstancesNeg.get((NamedClass) concept.getChild(0));
		}
		
		// return rs.retrieval(concept);
		SortedSet<Individual> inds = new TreeSet<Individual>();
		for (Individual i : individuals) {
			if (hasType(concept, i)) {
				inds.add(i);
			}
		}
		return inds;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.Reasoner#getAtomicConcepts()
	 */
	@Override
	public Set<NamedClass> getNamedClasses() {
		return atomicConcepts;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.Reasoner#getAtomicRoles()
	 */
	@Override
	public Set<ObjectProperty> getObjectProperties() {
		return atomicRoles;
	}

	@Override
	public SortedSet<DatatypeProperty> getDatatypePropertiesImpl() {
		return datatypeProperties;
	}

	@Override
	public SortedSet<DatatypeProperty> getBooleanDatatypePropertiesImpl() {
		return booleanDatatypeProperties;
	}

	@Override
	public SortedSet<DatatypeProperty> getDoubleDatatypePropertiesImpl() {
		return doubleDatatypeProperties;
	}

	@Override
	public SortedSet<DatatypeProperty> getIntDatatypePropertiesImpl() {
		return intDatatypeProperties;
	}

	@Override
	protected SortedSet<Description> getSuperClassesImpl(Description concept) throws ReasoningMethodUnsupportedException {
		return rc.getSuperClassesImpl(concept);
	}
	
	@Override
	protected SortedSet<Description> getSubClassesImpl(Description concept) throws ReasoningMethodUnsupportedException {
		return rc.getSubClassesImpl(concept);
	}		

	@Override
	protected SortedSet<ObjectProperty> getSuperPropertiesImpl(ObjectProperty role) throws ReasoningMethodUnsupportedException {
		return rc.getSuperPropertiesImpl(role);
	}	

	@Override
	protected SortedSet<ObjectProperty> getSubPropertiesImpl(ObjectProperty role) throws ReasoningMethodUnsupportedException {
		return rc.getSubPropertiesImpl(role);
	}
	
	@Override
	protected SortedSet<DatatypeProperty> getSuperPropertiesImpl(DatatypeProperty role) throws ReasoningMethodUnsupportedException {
		return rc.getSuperPropertiesImpl(role);
	}	

	@Override
	protected SortedSet<DatatypeProperty> getSubPropertiesImpl(DatatypeProperty role) throws ReasoningMethodUnsupportedException {
		return rc.getSubPropertiesImpl(role);
	}	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.Reasoner#getIndividuals()
	 */
	@Override
	public SortedSet<Individual> getIndividuals() {
		return individuals;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.Reasoner#getReasonerType()
	 */
	@Override
	public ReasonerType getReasonerType() {
		return ReasonerType.FAST_INSTANCE_CHECKER;
	}

//	@Override
//	public ClassHierarchy getClassHierarchy() {
//		return rc.getClassHierarchy();
//	}

//	@Override
//	public void prepareRoleHierarchyImpl(Set<ObjectProperty> allowedRoles) {
//		rc.prepareRoleHierarchy(allowedRoles);
//	}

//	@Override
//	public ObjectPropertyHierarchy getRoleHierarchy() {
//		return rc.getRoleHierarchy();
//	}

//	@Override
//	public void prepareDatatypePropertyHierarchyImpl(Set<DatatypeProperty> allowedRoles) {
//		rc.prepareDatatypePropertyHierarchyImpl(allowedRoles);
//	}

//	@Override
//	public DatatypePropertyHierarchy getDatatypePropertyHierarchy() {
//		return rc.getDatatypePropertyHierarchy();
//	}

	@Override
	public boolean isSuperClassOfImpl(Description superConcept, Description subConcept) {
		// Negation neg = new Negation(subConcept);
		// Intersection c = new Intersection(neg,superConcept);
		// return fastRetrieval.calculateSets(c).getPosSet().isEmpty();
		return rc.isSuperClassOf(superConcept, subConcept);
	}

	/**
	 * Test method for fast instance checker.
	 * 
	 * @param args
	 *            No arguments supported.
	 * @throws ComponentInitException Component cannot be initialised.
	 * @throws ParseException File cannot be parsed.
	 * @throws ReasoningMethodUnsupportedException Reasoning method not supported.
	 */
	public static void main(String[] args) throws ComponentInitException, ParseException,
			ReasoningMethodUnsupportedException {
		ComponentManager cm = ComponentManager.getInstance();
		OWLFile owl = cm.knowledgeSource(OWLFile.class);
		String owlFile = new File("examples/family/father.owl").toURI().toString();
		cm.applyConfigEntry(owl, "url", owlFile);
		owl.init();
		ReasonerComponent reasoner = cm.reasoner(FastInstanceChecker.class, owl);
//		cm.reasoningService(reasoner);
		reasoner.init();

		KBParser.internalNamespace = "http://example.com/father#";
		String query = "(male AND EXISTS hasChild.TOP)";
		Description d = KBParser.parseConcept(query);
		System.out.println(d);
		Individual i = new Individual("http://example.com/father#markus");
		System.out.println(reasoner.hasType(d, i));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.Reasoner#getBaseURI()
	 */
	@Override
	public String getBaseURI() {
		return rc.getBaseURI();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.Reasoner#getPrefixes()
	 */
	@Override
	public Map<String, String> getPrefixes() {
		return rc.getPrefixes();
	}

	@Override
	public Description getDomainImpl(ObjectProperty objectProperty) {
		return rc.getDomain(objectProperty);
	}

	@Override
	public Description getDomainImpl(DatatypeProperty datatypeProperty) {
		return rc.getDomain(datatypeProperty);
	}

	@Override
	public Description getRangeImpl(ObjectProperty objectProperty) {
		return rc.getRange(objectProperty);
	}

	@Override
	public Map<Individual, SortedSet<Individual>> getPropertyMembersImpl(ObjectProperty atomicRole) {
		return opPos.get(atomicRole);
	}

	@Override
	public final SortedSet<Individual> getTrueDatatypeMembersImpl(DatatypeProperty datatypeProperty) {
		return bdPos.get(datatypeProperty);
	}
	
	@Override
	public final SortedSet<Individual> getFalseDatatypeMembersImpl(DatatypeProperty datatypeProperty) {
		return bdNeg.get(datatypeProperty);
	}
	
	@Override
	public Map<Individual, SortedSet<Integer>> getIntDatatypeMembersImpl(
			DatatypeProperty datatypeProperty) {
		return id.get(datatypeProperty);
	}		
	
	@Override
	public Map<Individual, SortedSet<Double>> getDoubleDatatypeMembersImpl(
			DatatypeProperty datatypeProperty) {
		return dd.get(datatypeProperty);
	}	
	
	@Override
	public Set<Individual> getRelatedIndividualsImpl(Individual individual, ObjectProperty objectProperty) throws ReasoningMethodUnsupportedException {
		return rc.getRelatedIndividuals(individual, objectProperty);
	}
	
	@Override
	protected Map<ObjectProperty,Set<Individual>> getObjectPropertyRelationshipsImpl(Individual individual) {
		return rc.getObjectPropertyRelationships(individual);
	}	
	
	@Override
	public Set<Constant> getRelatedValuesImpl(Individual individual, DatatypeProperty datatypeProperty) throws ReasoningMethodUnsupportedException {
		return rc.getRelatedValues(individual, datatypeProperty);
	}	
	
	@Override
	public Set<Constant> getLabelImpl(Entity entity) throws ReasoningMethodUnsupportedException {
		return rc.getLabel(entity);
	}	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.ReasonerComponent#releaseKB()
	 */
	@Override
	public void releaseKB() {
		rc.releaseKB();
	}

//	@Override
//	public boolean hasDatatypeSupport() {
//		return true;
//	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.ReasonerComponent#getTypesImpl(org.dllearner.core.owl.Individual)
	 */
	@Override
	protected Set<NamedClass> getTypesImpl(Individual individual) {
		return rc.getTypesImpl(individual);
	}

}
