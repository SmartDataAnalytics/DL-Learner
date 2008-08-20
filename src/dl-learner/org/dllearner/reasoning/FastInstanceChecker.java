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
import org.dllearner.core.config.ConfigEntry;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.InvalidConfigOptionValueException;
import org.dllearner.core.config.StringConfigOption;
import org.dllearner.core.owl.BooleanValueRestriction;
import org.dllearner.core.owl.DataRange;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.DatatypePropertyHierarchy;
import org.dllearner.core.owl.DatatypeSomeRestriction;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.DoubleMaxValue;
import org.dllearner.core.owl.DoubleMinValue;
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
import org.dllearner.core.owl.ObjectPropertyHierarchy;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.dllearner.core.owl.SubsumptionHierarchy;
import org.dllearner.core.owl.Thing;
import org.dllearner.core.owl.Union;
import org.dllearner.kb.OWLFile;
import org.dllearner.parser.KBParser;
import org.dllearner.parser.ParseException;
import org.dllearner.utilities.Helper;

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

	private boolean defaultNegation = true;
	
	private String reasonerType = "pellet";
	
	private Set<NamedClass> atomicConcepts;
	private Set<ObjectProperty> atomicRoles;
	private SortedSet<DatatypeProperty> datatypeProperties;
	private SortedSet<DatatypeProperty> booleanDatatypeProperties = new TreeSet<DatatypeProperty>();
	private SortedSet<DatatypeProperty> doubleDatatypeProperties = new TreeSet<DatatypeProperty>();
	private SortedSet<DatatypeProperty> intDatatypeProperties = new TreeSet<DatatypeProperty>();
	private SortedSet<Individual> individuals;

//	private ReasoningService rs;
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
	// we have one mapping for true and false for efficiency reasons
	private Map<DatatypeProperty, SortedSet<Individual>> bdPos = new TreeMap<DatatypeProperty, SortedSet<Individual>>();
	private Map<DatatypeProperty, SortedSet<Individual>> bdNeg = new TreeMap<DatatypeProperty, SortedSet<Individual>>();
	// for int and double we assume that a property can have several values,
	// althoug this should be rare,
	// e.g. hasValue(object,2) and hasValue(object,3)
	private Map<DatatypeProperty, Map<Individual, SortedSet<Double>>> dd = new TreeMap<DatatypeProperty, Map<Individual, SortedSet<Double>>>();
	private Map<DatatypeProperty, Map<Individual, SortedSet<Integer>>> id = new TreeMap<DatatypeProperty, Map<Individual, SortedSet<Integer>>>();

	public FastInstanceChecker(Set<KnowledgeSource> sources) {
		this.sources = sources;
	}

	
	public static Collection<ConfigOption<?>> createConfigOptions() {
		Collection<ConfigOption<?>> options = new LinkedList<ConfigOption<?>>();
		StringConfigOption type = new StringConfigOption("reasonerType", "FaCT++ or Pellet to dematerialize", "pellet");
		type.setAllowedValues(new String[] {"fact", "pellet"});
		// closure option? see:
		// http://owlapi.svn.sourceforge.net/viewvc/owlapi/owl1_1/trunk/tutorial/src/main/java/uk/ac/manchester/owl/tutorial/examples/ClosureAxiomsExample.java?view=markup
		options.add(type);
		return options;
	}	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.Component#applyConfigEntry(org.dllearner.core.config.ConfigEntry)
	 */
	@Override
	public <T> void applyConfigEntry(ConfigEntry<T> entry) throws InvalidConfigOptionValueException {
		String name = entry.getOptionName();
		if(name.equals("reasonerType"))
			reasonerType = (String) entry.getValue();
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
		rc.setReasonerType(reasonerType);
		rc.init();

		try {
			atomicConcepts = rc.getAtomicConcepts();
			datatypeProperties = rc.getDatatypeProperties();
			booleanDatatypeProperties = rc.getBooleanDatatypeProperties();
			doubleDatatypeProperties = rc.getDoubleDatatypeProperties();
			intDatatypeProperties = rc.getIntDatatypeProperties();
			atomicRoles = rc.getAtomicRoles();
			individuals = rc.getIndividuals();

//			rs = new ReasoningService(rc);

			// TODO: some code taken from Helper.createFlatABox, but pasted here
			// because additional things need to
			// be done (maybe this can be merge again with the
			// FastRetrievalReasoner later)
			long dematStartTime = System.currentTimeMillis();

			logger.debug("dematerialising concepts");
			
			for (NamedClass atomicConcept : rc.getAtomicConcepts()) {				
				
				SortedSet<Individual> pos = rc.retrieval(atomicConcept);
				classInstancesPos.put(atomicConcept, pos);
				
				if(defaultNegation) {
					classInstancesNeg.put(atomicConcept, Helper.difference(individuals,pos));
				} else {
					// Pellet needs approximately infinite time to answer negated queries
					// on the carcinogenesis data set (and probably others), so we have to
					// be careful here
					Negation negatedAtomicConcept = new Negation(atomicConcept);
					classInstancesNeg.put(atomicConcept, rc.retrieval(negatedAtomicConcept));
				}


			}

			logger.debug("dematerialising object properties");
			
			for (ObjectProperty atomicRole : atomicRoles) {
				opPos.put(atomicRole, rc.getRoleMembers(atomicRole));
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

		} catch (ReasoningMethodUnsupportedException e) {
			throw new ComponentInitException(
					"Underlying reasoner does not support all necessary reasoning methods.", e);
		}
	}

	@Override
	public boolean instanceCheck(Description description, Individual individual)
			throws ReasoningMethodUnsupportedException {
		
//		System.out.println(description + " " + individual);
		
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
		} else if (description instanceof ObjectMinCardinalityRestriction) {
			ObjectPropertyExpression ope = ((ObjectCardinalityRestriction) description).getRole();
			if (!(ope instanceof ObjectProperty))
				throw new ReasoningMethodUnsupportedException("Instance check for description "
						+ description + " unsupported. Inverse object properties not supported.");
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
			if(number == 0)
				return true;
			// return false if there are none or not enough role fillers
			if (roleFillers == null || roleFillers.size() < number)
				return false;
			 
			int index = 0;
			for (Individual roleFiller : roleFillers) {
				index++;
				if (instanceCheck(child, roleFiller)) {
					nrOfFillers++;
					if(nrOfFillers == number)
						return true;
				// earyl abort:	e.g. >= 10 hasStructure.Methyl;
				// if there are 11 fillers and 2 are not Methyl, the result is false
				} else {
					if(roleFillers.size() - index < number)
						return false;
				}
			}
			return false;
		} else if (description instanceof ObjectMaxCardinalityRestriction) {
			ObjectPropertyExpression ope = ((ObjectCardinalityRestriction) description).getRole();
			if (!(ope instanceof ObjectProperty))
				throw new ReasoningMethodUnsupportedException("Instance check for description "
						+ description + " unsupported. Inverse object properties not supported.");
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
			if (roleFillers == null || roleFillers.size() < number)
				return true;
			
//			System.out.println(description + " " + individual);	
			
			int index = 0;
			for (Individual roleFiller : roleFillers) {
				index++;
				if (instanceCheck(child, roleFiller)) {
					nrOfFillers++;
					if(nrOfFillers > number)
						return false;
				// earyl abort:	e.g. <= 5 hasStructure.Methyl;
				// if there are 6 fillers and 2 are not Methyl, the result is true						
				} else {
					if(roleFillers.size() - index <= number)
						return true;
				}
			}
			return true;
		} else if (description instanceof BooleanValueRestriction) {
			DatatypeProperty dp = ((BooleanValueRestriction)description).getRestrictedPropertyExpresssion();
			boolean value = ((BooleanValueRestriction)description).getBooleanValue();
			
			if(value) {
				// check whether the individual is in the set of individuals mapped
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
			if(values == null)
				return false;
			
			if(dr instanceof DoubleMaxValue) {
				if(values.first() <= ((DoubleMaxValue)dr).getValue())
					return true;
				else
					return false;
			} else if(dr instanceof DoubleMinValue) {
				if(values.last() >= ((DoubleMinValue)dr).getValue())
					return true;
				else
					return false;
			}
		}

		throw new ReasoningMethodUnsupportedException("Instance check for description "
				+ description + " unsupported.");
	}

	@Override
	public SortedSet<Individual> retrieval(Description concept) throws ReasoningMethodUnsupportedException {
		if(concept instanceof NamedClass)
			return classInstancesPos.get((NamedClass)concept);
		else if(concept instanceof Negation && concept.getChild(0) instanceof NamedClass)
			return classInstancesNeg.get((NamedClass)concept.getChild(0));
		
//		return rs.retrieval(concept);
		SortedSet<Individual> inds = new TreeSet<Individual>();
		for(Individual i : individuals) {
			if(instanceCheck(concept,i))
				inds.add(i);
		}
		return inds;
	}	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.Reasoner#getAtomicConcepts()
	 */
	public Set<NamedClass> getAtomicConcepts() {
		return atomicConcepts;
	}

	@Override
	public Map<Individual, SortedSet<Double>> getDoubleDatatypeMembers(DatatypeProperty datatypeProperty) throws ReasoningMethodUnsupportedException {
		return rc.getDoubleDatatypeMembers(datatypeProperty);
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
		rc.prepareSubsumptionHierarchy(allowedConcepts);
	}

	@Override
	public SubsumptionHierarchy getSubsumptionHierarchy() {
		return rc.getSubsumptionHierarchy();
	}

	@Override
	public void prepareRoleHierarchy(Set<ObjectProperty> allowedRoles) {
		rc.prepareRoleHierarchy(allowedRoles);
	}

	@Override
	public ObjectPropertyHierarchy getRoleHierarchy() {
		return rc.getRoleHierarchy();
	}

	@Override
	public void prepareDatatypePropertyHierarchy(Set<DatatypeProperty> allowedRoles) {
		rc.prepareDatatypePropertyHierarchy(allowedRoles);
	}

	@Override
	public DatatypePropertyHierarchy getDatatypePropertyHierarchy() {
		return rc.getDatatypePropertyHierarchy();
	}
	
	@Override
	public boolean subsumes(Description superConcept, Description subConcept) {
		// Negation neg = new Negation(subConcept);
		// Intersection c = new Intersection(neg,superConcept);
		// return fastRetrieval.calculateSets(c).getPosSet().isEmpty();
		return rc.subsumes(superConcept, subConcept);
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

	@Override
	public Map<Individual, SortedSet<Individual>> getRoleMembers(ObjectProperty atomicRole)	 {
		return opPos.get(atomicRole);
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.ReasonerComponent#releaseKB()
	 */
	@Override
	public void releaseKB() {
		rc.releaseKB();
	}	
	
	public void setReasonerType(String type){
		reasonerType=type;
	}


	@Override
	public boolean hasDatatypeSupport() {
		return true;
	}
	
}
