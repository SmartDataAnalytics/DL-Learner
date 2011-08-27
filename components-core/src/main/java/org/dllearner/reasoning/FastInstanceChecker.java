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

package org.dllearner.reasoning;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ReasoningMethodUnsupportedException;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.owl.Axiom;
import org.dllearner.core.owl.BooleanValueRestriction;
import org.dllearner.core.owl.Constant;
import org.dllearner.core.owl.DataRange;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.DatatypeSomeRestriction;
import org.dllearner.core.owl.DatatypeValueRestriction;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import sun.beans.editors.BoolEditor;

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
 * Meanwhile, the algorithm has been extended to also perform fast retrieval
 * operations. However, those need write access to memory and potentially have
 * to deal with all individuals in a knowledge base. For many knowledge bases,
 * they should still be reasonably fast. 
 * 
 * @author Jens Lehmann
 * 
 */
@ComponentAnn(name = "fast instance checker", shortName = "fic", version = 0.9)
public class FastInstanceChecker extends AbstractReasonerComponent {

	private static Logger logger = Logger.getLogger(FastInstanceChecker.class);

//	private boolean defaultNegation = true;

	private Set<NamedClass> atomicConcepts;
	private Set<ObjectProperty> atomicRoles;
	private SortedSet<DatatypeProperty> datatypeProperties;
	private SortedSet<DatatypeProperty> booleanDatatypeProperties = new TreeSet<DatatypeProperty>();
	private SortedSet<DatatypeProperty> doubleDatatypeProperties = new TreeSet<DatatypeProperty>();
	private SortedSet<DatatypeProperty> intDatatypeProperties = new TreeSet<DatatypeProperty>();
	private SortedSet<DatatypeProperty> stringDatatypeProperties = new TreeSet<DatatypeProperty>();	
	private TreeSet<Individual> individuals;

	// private ReasonerComponent rs;

	private OWLAPIReasoner rc;

	// we use sorted sets (map indices) here, because they have only log(n)
	// complexity for checking whether an element is contained in them
	// instances of classes
	private Map<NamedClass, TreeSet<Individual>> classInstancesPos = new TreeMap<NamedClass, TreeSet<Individual>>();
	private Map<NamedClass, TreeSet<Individual>> classInstancesNeg = new TreeMap<NamedClass, TreeSet<Individual>>();
	// object property mappings
	private Map<ObjectProperty, Map<Individual, SortedSet<Individual>>> opPos = new TreeMap<ObjectProperty, Map<Individual, SortedSet<Individual>>>();
	// datatype property mappings
	// we have one mapping for true and false for efficiency reasons
	private Map<DatatypeProperty, TreeSet<Individual>> bdPos = new TreeMap<DatatypeProperty, TreeSet<Individual>>();
	private Map<DatatypeProperty, TreeSet<Individual>> bdNeg = new TreeMap<DatatypeProperty, TreeSet<Individual>>();
	// for int and double we assume that a property can have several values,
	// althoug this should be rare,
	// e.g. hasValue(object,2) and hasValue(object,3)
	private Map<DatatypeProperty, Map<Individual, SortedSet<Double>>> dd = new TreeMap<DatatypeProperty, Map<Individual, SortedSet<Double>>>();
	private Map<DatatypeProperty, Map<Individual, SortedSet<Integer>>> id = new TreeMap<DatatypeProperty, Map<Individual, SortedSet<Integer>>>();
	private Map<DatatypeProperty, Map<Individual, SortedSet<String>>> sd = new TreeMap<DatatypeProperty, Map<Individual, SortedSet<String>>>();

    @ConfigOption(name="defaultNegation", description = "Whether to use default negation, i.e. an instance not being in a class means that it is in the negation of the class.", defaultValue = "true", required = false, propertyEditorClass = BoolEditor.class)
    private boolean defaultNegation = true;

    @ConfigOption(name = "forAllRetrievalSemantics", description = "This option controls how to interpret the all quantifier in forall r.C. The standard option is" +
            "to return all those which do not have an r-filler not in C. The domain semantics is to use those" +
            "which are in the domain of r and do not have an r-filler not in C. The forallExists semantics is to"+
            "use those which have at least one r-filler and do not have an r-filler not in C.",defaultValue = "standard",propertyEditorClass = StringTrimmerEditor.class)
    private String forAllSemantics;

	/**
	 * Creates an instance of the fast instance checker.
	 */
	public FastInstanceChecker() {
	}

    public FastInstanceChecker(Set<AbstractKnowledgeSource> sources) {
        super(sources);
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

        if(rc == null){
            rc = new OWLAPIReasoner(sources);
            rc.init();
        }
//		try {
			atomicConcepts = rc.getNamedClasses();
			datatypeProperties = rc.getDatatypeProperties();
			booleanDatatypeProperties = rc.getBooleanDatatypeProperties();
			doubleDatatypeProperties = rc.getDoubleDatatypeProperties();
			intDatatypeProperties = rc.getIntDatatypeProperties();
			stringDatatypeProperties = rc.getStringDatatypeProperties();
			atomicRoles = rc.getObjectProperties();
			individuals = (TreeSet<Individual>) rc.getIndividuals();

			// rs = new ReasonerComponent(rc);

			// TODO: some code taken from Helper.createFlatABox, but pasted here
			// because additional things need to
			// be done (maybe this can be merge again with the
			// FastRetrievalReasoner later)
			long dematStartTime = System.currentTimeMillis();

			logger.debug("dematerialising concepts");

			for (NamedClass atomicConcept : rc.getNamedClasses()) {
				SortedSet<Individual> pos = rc.getIndividuals(atomicConcept);
				classInstancesPos.put(atomicConcept, (TreeSet<Individual>) pos);

				if (isDefaultNegation()) {
					classInstancesNeg.put(atomicConcept, (TreeSet<Individual>) Helper.difference(individuals, pos));
				} else {
					// Pellet needs approximately infinite time to answer
					// negated queries
					// on the carcinogenesis data set (and probably others), so
					// we have to
					// be careful here
					Negation negatedAtomicConcept = new Negation(atomicConcept);
					classInstancesNeg.put(atomicConcept, (TreeSet<Individual>) rc.getIndividuals(negatedAtomicConcept));
				}

			}

			logger.debug("dematerialising object properties");

			for (ObjectProperty atomicRole : atomicRoles) {
				opPos.put(atomicRole, rc.getPropertyMembers(atomicRole));
			}

			logger.debug("dematerialising datatype properties");

			for (DatatypeProperty dp : booleanDatatypeProperties) {
				bdPos.put(dp, (TreeSet<Individual>) rc.getTrueDatatypeMembers(dp));
				bdNeg.put(dp, (TreeSet<Individual>) rc.getFalseDatatypeMembers(dp));
			}

			for (DatatypeProperty dp : intDatatypeProperties) {
				id.put(dp, rc.getIntDatatypeMembers(dp));
			}

			for (DatatypeProperty dp : doubleDatatypeProperties) {
				dd.put(dp, rc.getDoubleDatatypeMembers(dp));
			}

			for (DatatypeProperty dp : stringDatatypeProperties) {
				sd.put(dp, rc.getStringDatatypeMembers(dp));
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

//		 System.out.println("FIC: " + description + " " + individual);

		if (description instanceof NamedClass) {
			return classInstancesPos.get((NamedClass) description).contains(individual);
		} else if (description instanceof Negation) {
			Description child = description.getChild(0);
			if (child instanceof NamedClass) {
				return classInstancesNeg.get((NamedClass) child).contains(individual);
			} else {
				// default negation
				if(isDefaultNegation()) {
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
				if (hasTypeImpl(child, individual)) {
					return true;
				}
			}
			return false;
		} else if (description instanceof Intersection) {
			// if the individual is instance of all of the subdescription of
			// the union, we return true
			List<Description> children = description.getChildren();
			for (Description child : children) {
				if (!hasTypeImpl(child, individual)) {
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
				if (hasTypeImpl(child, roleFiller)) {
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
				if (!hasTypeImpl(child, roleFiller)) {
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

//			SortedSet<Individual> roleFillers = opPos.get(op).get(individual);
			SortedSet<Individual> roleFillers = mapping.get(individual);
//			System.out.println(roleFillers);
			
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
				if (hasTypeImpl(child, roleFiller)) {
					nrOfFillers++;
					if (nrOfFillers == number) {
						return true;
					}
					// early abort: e.g. >= 10 hasStructure.Methyl;
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
				if (hasTypeImpl(child, roleFiller)) {
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
			
			Set<Individual> inds = opPos.get(op).get(individual);
			return inds == null ? false : inds.contains(i);
		} else if (description instanceof BooleanValueRestriction) {
			DatatypeProperty dp = ((BooleanValueRestriction) description)
					.getRestrictedPropertyExpression();
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
		} else if (description instanceof DatatypeValueRestriction) {
			String i = ((DatatypeValueRestriction)description).getValue().getLiteral();
			DatatypeProperty dp = ((DatatypeValueRestriction)description).getRestrictedPropertyExpression();
			
			Set<String> inds = sd.get(dp).get(individual);
			return inds == null ? false : inds.contains(i);
		}

		throw new ReasoningMethodUnsupportedException("Instance check for description "
				+ description + " unsupported.");
	}

	@Override
	public SortedSet<Individual> getIndividualsImpl(Description concept) throws ReasoningMethodUnsupportedException {
		return getIndividualsImplFast(concept);
	}
	
	public SortedSet<Individual> getIndividualsImplStandard(Description concept)
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
	
	@SuppressWarnings("unchecked")
	public SortedSet<Individual> getIndividualsImplFast(Description description)
			throws ReasoningMethodUnsupportedException {
		// policy: returned sets are clones, i.e. can be modified
		// (of course we only have to clone the leafs of a class description tree)
		if (description instanceof NamedClass) {
			return (TreeSet<Individual>) classInstancesPos.get((NamedClass) description).clone();
		} else if (description instanceof Negation) {
			if(description.getChild(0) instanceof NamedClass) {
				return (TreeSet<Individual>) classInstancesNeg.get((NamedClass) description.getChild(0)).clone();
			}
			// implement retrieval as default negation
			return Helper.difference((TreeSet<Individual>) individuals.clone(), getIndividualsImpl(description.getChild(0)));
		} else if (description instanceof Thing) {
			return (TreeSet<Individual>) individuals.clone();
		} else if (description instanceof Nothing) {
			return new TreeSet<Individual>();
		} else if (description instanceof Union) {
			// copy instances of first element and then subtract all others
			SortedSet<Individual> ret = getIndividualsImpl(description.getChild(0));
			int childNr = 0;
			for(Description child : description.getChildren()) {
				if(childNr != 0) {
					ret.addAll(getIndividualsImpl(child));
				}
				childNr++;
			}
			return ret;
		} else if (description instanceof Intersection) {
			// copy instances of first element and then subtract all others
			SortedSet<Individual> ret = getIndividualsImpl(description.getChild(0));
			int childNr = 0;
			for(Description child : description.getChildren()) {
				if(childNr != 0) {
					ret.retainAll(getIndividualsImpl(child));
				}
				childNr++;
			}
			return ret;
		} else if (description instanceof ObjectSomeRestriction) {
			SortedSet<Individual> targetSet = getIndividualsImpl(description.getChild(0));
			SortedSet<Individual> returnSet = new TreeSet<Individual>();
			
			ObjectPropertyExpression ope = ((ObjectSomeRestriction) description).getRole();
			if (!(ope instanceof ObjectProperty)) {
				throw new ReasoningMethodUnsupportedException("Retrieval for description "
						+ description + " unsupported. Inverse object properties not supported.");
			}
			ObjectProperty op = (ObjectProperty) ope;
			Map<Individual, SortedSet<Individual>> mapping = opPos.get(op);			
			
			// each individual is connected to a set of individuals via the property;
			// we loop through the complete mapping
			for(Entry<Individual, SortedSet<Individual>> entry : mapping.entrySet()) {
				SortedSet<Individual> inds = entry.getValue();
				for(Individual ind : inds) {
					if(targetSet.contains(ind)) {
						returnSet.add(entry.getKey());
						// once we found an individual, we do not need to check the others
						continue; 
					}
				}
			}
			return returnSet;
		} else if (description instanceof ObjectAllRestriction) {
			// \forall restrictions are difficult to handle; assume we want to check
			// \forall hasChild.male with domain(hasChild)=Person; then for all non-persons
			// this is satisfied trivially (all of their non-existing children are male)
//			if(!configurator.getForallRetrievalSemantics().equals("standard")) {
//				throw new Error("Only forallExists semantics currently implemented.");
//			}
			
			// problem: we need to make sure that \neg \exists r.\top \equiv \forall r.\bot
			// can still be reached in an algorithm (\forall r.\bot \equiv \bot under forallExists
			// semantics)
			
			SortedSet<Individual> targetSet = getIndividualsImpl(description.getChild(0));
						
			ObjectPropertyExpression ope = ((ObjectAllRestriction) description).getRole();
			if (!(ope instanceof ObjectProperty)) {
				throw new ReasoningMethodUnsupportedException("Instance check for description "
						+ description + " unsupported. Inverse object properties not supported.");
			}
			ObjectProperty op = (ObjectProperty) ope;
			Map<Individual, SortedSet<Individual>> mapping = opPos.get(op);
//			SortedSet<Individual> returnSet = new TreeSet<Individual>(mapping.keySet());
			SortedSet<Individual> returnSet = (SortedSet<Individual>) individuals.clone();
			
			// each individual is connected to a set of individuals via the property;
			// we loop through the complete mapping
			for(Entry<Individual, SortedSet<Individual>> entry : mapping.entrySet()) {
				SortedSet<Individual> inds = entry.getValue();
				for(Individual ind : inds) {
					if(!targetSet.contains(ind)) {
						returnSet.remove(entry.getKey());
						continue; 
					}
				}
			}
			return returnSet;
		} else if (description instanceof ObjectMinCardinalityRestriction) {
			ObjectPropertyExpression ope = ((ObjectCardinalityRestriction) description).getRole();
			if (!(ope instanceof ObjectProperty)) {
				throw new ReasoningMethodUnsupportedException("Instance check for description "
						+ description + " unsupported. Inverse object properties not supported.");
			}
			ObjectProperty op = (ObjectProperty) ope;
			Description child = description.getChild(0);
			Map<Individual, SortedSet<Individual>> mapping = opPos.get(op);
			SortedSet<Individual> targetSet = getIndividualsImpl(child);
			SortedSet<Individual> returnSet = new TreeSet<Individual>();

			int number = ((ObjectCardinalityRestriction) description).getNumber();			

			for(Entry<Individual, SortedSet<Individual>> entry : mapping.entrySet()) {
				int nrOfFillers = 0;
				int index = 0;
				SortedSet<Individual> inds = entry.getValue();
				
				// we do not need to run tests if there are not sufficiently many fillers
				if(inds.size() < number) {
					continue;
				}
				
				for(Individual ind : inds) {
					// stop inner loop when nr of fillers is reached
					if(nrOfFillers >= number) {
						returnSet.add(entry.getKey());
						break;
					}		
					// early abort when too many instance checks failed
					if (inds.size() - index < number) {
						break;
					}					
					if(targetSet.contains(ind)) {
						nrOfFillers++;
					}
					index++;
				}
			}			
			
			return returnSet;
		} else if (description instanceof ObjectMaxCardinalityRestriction) {
			ObjectPropertyExpression ope = ((ObjectCardinalityRestriction) description).getRole();
			if (!(ope instanceof ObjectProperty)) {
				throw new ReasoningMethodUnsupportedException("Instance check for description "
						+ description + " unsupported. Inverse object properties not supported.");
			}
			ObjectProperty op = (ObjectProperty) ope;
			Description child = description.getChild(0);
			Map<Individual, SortedSet<Individual>> mapping = opPos.get(op);
			SortedSet<Individual> targetSet = getIndividualsImpl(child);
			// initially all individuals are in the return set and we then remove those
			// with too many fillers			
			SortedSet<Individual> returnSet = (SortedSet<Individual>) individuals.clone();

			int number = ((ObjectCardinalityRestriction) description).getNumber();			

			for(Entry<Individual, SortedSet<Individual>> entry : mapping.entrySet()) {
				int nrOfFillers = 0;
				int index = 0;
				SortedSet<Individual> inds = entry.getValue();
				
				// we do not need to run tests if there are not sufficiently many fillers
				if(number < inds.size()) {
					returnSet.add(entry.getKey());
					continue;
				}
				
				for(Individual ind : inds) {
					// stop inner loop when nr of fillers is reached
					if(nrOfFillers >= number) {
						break;
					}		
					// early abort when too many instance are true already
					if (inds.size() - index < number) {
						returnSet.add(entry.getKey());
						break;
					}					
					if(targetSet.contains(ind)) {
						nrOfFillers++;
					}
					index++;
				}
			}			
			
			return returnSet;
		} else if (description instanceof ObjectValueRestriction) {
			Individual i = ((ObjectValueRestriction)description).getIndividual();
			ObjectProperty op = (ObjectProperty) ((ObjectValueRestriction)description).getRestrictedPropertyExpression();
			
			Map<Individual, SortedSet<Individual>> mapping = opPos.get(op);			
			SortedSet<Individual> returnSet = new TreeSet<Individual>();
			
			for(Entry<Individual, SortedSet<Individual>> entry : mapping.entrySet()) {
				if(entry.getValue().contains(i)) {
					returnSet.add(entry.getKey());
				}
			}
			return returnSet;
		} else if (description instanceof BooleanValueRestriction) {
			DatatypeProperty dp = ((BooleanValueRestriction) description)
					.getRestrictedPropertyExpression();
			boolean value = ((BooleanValueRestriction) description).getBooleanValue();

			if (value) {
				return (TreeSet<Individual>) bdPos.get(dp).clone();
			} else {
				return (TreeSet<Individual>) bdNeg.get(dp).clone();
			}
		} else if (description instanceof DatatypeSomeRestriction) {
			DatatypeSomeRestriction dsr = (DatatypeSomeRestriction) description;
			DatatypeProperty dp = (DatatypeProperty) dsr.getRestrictedPropertyExpression();
			DataRange dr = dsr.getDataRange();

			Map<Individual, SortedSet<Double>> mapping = dd.get(dp);			
			SortedSet<Individual> returnSet = new TreeSet<Individual>();			

			if (dr instanceof DoubleMaxValue) {
				for(Entry<Individual, SortedSet<Double>> entry : mapping.entrySet()) {
					if(entry.getValue().first() <= ((DoubleMaxValue)dr).getValue()) {
						returnSet.add(entry.getKey());
					}
				}				
			} else if (dr instanceof DoubleMinValue) {
				for(Entry<Individual, SortedSet<Double>> entry : mapping.entrySet()) {
					if(entry.getValue().last() >= ((DoubleMinValue)dr).getValue()) {
						returnSet.add(entry.getKey());
					}
				}
			}
		}
			
		throw new ReasoningMethodUnsupportedException("Retrieval for description "
					+ description + " unsupported.");		
			
		// return rs.retrieval(concept);
//		SortedSet<Individual> inds = new TreeSet<Individual>();
//		for (Individual i : individuals) {
//			if (hasType(concept, i)) {
//				inds.add(i);
//			}
//		}
//		return inds;
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
	public SortedSet<DatatypeProperty> getStringDatatypePropertiesImpl() {
		return stringDatatypeProperties;
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
		return rc.isSuperClassOfImpl(superConcept, subConcept);
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
		AbstractReasonerComponent reasoner = cm.reasoner(FastInstanceChecker.class, owl);
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
	public Map<Individual, SortedSet<Constant>> getDatatypeMembersImpl(
			DatatypeProperty datatypeProperty) {
		return rc.getDatatypeMembersImpl(datatypeProperty);
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
	public boolean isSatisfiableImpl() {
		return rc.isSatisfiable();
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

	/* (non-Javadoc)
	 * @see org.dllearner.core.BaseReasoner#remainsSatisfiable(org.dllearner.core.owl.Axiom)
	 */
	@Override
	public boolean remainsSatisfiableImpl(Axiom axiom) {
		return rc.remainsSatisfiableImpl(axiom);
	}

	@Override
	protected Set<Description> getAssertedDefinitionsImpl(NamedClass nc) {
		return rc.getAssertedDefinitionsImpl(nc);
	}

    public OWLAPIReasoner getReasonerComponent() {
        return rc;
    }

    @Autowired(required = false)
    public void setReasonerComponent(OWLAPIReasoner rc) {
        this.rc = rc;
    }

    public boolean isDefaultNegation() {
        return defaultNegation;
    }

    public void setDefaultNegation(boolean defaultNegation) {
        this.defaultNegation = defaultNegation;
    }

    public String getForAllSemantics() {
        return forAllSemantics;
    }

    public void setForAllSemantics(String forAllSemantics) {
        this.forAllSemantics = forAllSemantics;
    }
}
