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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
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
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.owl.ConceptTransformation;
import org.dllearner.utilities.owl.DLLearnerDescriptionConvertVisitor;
import org.dllearner.utilities.owl.OWLAPIConverter;
import org.dllearner.utilities.owl.OWLPunningDetector;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

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
@ComponentAnn(name = "materializable fast instance checker", shortName = "mat-fic", version = 0.9)
public class MaterializableFastInstanceChecker extends AbstractReasonerComponent {

	private static Logger logger = Logger.getLogger(MaterializableFastInstanceChecker.class);

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
	// data property mappings
	private Map<DatatypeProperty, Map<Individual, SortedSet<Constant>>> dpPos = new TreeMap<DatatypeProperty, Map<Individual, SortedSet<Constant>>>();
		
	
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

    @ConfigOption(name="defaultNegation", description = "Whether to use default negation, i.e. an instance not being in a class means that it is in the negation of the class.", defaultValue = "true", required = false)
    private boolean defaultNegation = true;

    @ConfigOption(name = "forAllRetrievalSemantics", description = "This option controls how to interpret the all quantifier in forall r.C. The standard option is" +
            "to return all those which do not have an r-filler not in C. The domain semantics is to use those" +
            "which are in the domain of r and do not have an r-filler not in C. The forallExists semantics is to"+
            "use those which have at least one r-filler and do not have an r-filler not in C.",defaultValue = "standard",propertyEditorClass = StringTrimmerEditor.class)
    private ForallSemantics forallSemantics = ForallSemantics.Standard;
    
    private boolean materializeExistentialRestrictions = false;

	private boolean useCaching = false;
    private boolean handlePunning = false;
    
    private GenericIndividualGenerator individualGenerator = new GenericIndividualGenerator();

    public enum ForallSemantics { 
    	Standard, // standard all quantor
    	NonEmpty, // p only C for instance a returns false if there is no fact p(a,x) for any x  
    	SomeOnly  // p only C for instance a returns false if there is no fact p(a,x) with x \ in C  
    }
    
	/**
	 * Creates an instance of the fast instance checker.
	 */
	public MaterializableFastInstanceChecker() {
	}

    public MaterializableFastInstanceChecker(TreeSet<Individual> individuals,
			Map<NamedClass, TreeSet<Individual>> classInstancesPos,
			Map<ObjectProperty, Map<Individual, SortedSet<Individual>>> opPos,
			Map<DatatypeProperty, Map<Individual, SortedSet<Integer>>> id,
			Map<DatatypeProperty, TreeSet<Individual>> bdPos,
			Map<DatatypeProperty, TreeSet<Individual>> bdNeg,
			KnowledgeSource... sources) {
		super(new HashSet<KnowledgeSource>(Arrays.asList(sources)));
		this.individuals = individuals;
		this.classInstancesPos = classInstancesPos;
		this.opPos = opPos;
		this.id = id;
		this.bdPos = bdPos;
		this.bdNeg = bdNeg;
		
		if(rc == null){
            rc = new OWLAPIReasoner(new HashSet<KnowledgeSource>(Arrays.asList(sources)));
            try {
				rc.init();
			} catch (ComponentInitException e) {
				e.printStackTrace();
			}
        }
		
		atomicConcepts = rc.getNamedClasses();
		datatypeProperties = rc.getDatatypeProperties();
		booleanDatatypeProperties = rc.getBooleanDatatypeProperties();
		doubleDatatypeProperties = rc.getDoubleDatatypeProperties();
		intDatatypeProperties = rc.getIntDatatypeProperties();
		stringDatatypeProperties = rc.getStringDatatypeProperties();
		atomicRoles = rc.getObjectProperties();
		
		for (NamedClass atomicConcept : rc.getNamedClasses()) {
			TreeSet<Individual> pos = classInstancesPos.get(atomicConcept);
			if(pos != null){
				classInstancesNeg.put(atomicConcept, (TreeSet<Individual>) Helper.difference(individuals, pos));
			} else {
				classInstancesPos.put(atomicConcept, new TreeSet<Individual>());
				classInstancesNeg.put(atomicConcept, individuals);
			}
		}
		for(ObjectProperty p : atomicRoles){
			if(opPos.get(p) == null){
				opPos.put(p, new HashMap<Individual, SortedSet<Individual>>());
			}
		}
		
		for (DatatypeProperty dp : booleanDatatypeProperties) {
			if(bdPos.get(dp) == null){
				bdPos.put(dp, new TreeSet<Individual>());
			}
			if(bdNeg.get(dp) == null){
				bdNeg.put(dp, new TreeSet<Individual>());
			}
			
		}
	}

	public MaterializableFastInstanceChecker(Set<KnowledgeSource> sources) {
        super(sources);
    }

    public MaterializableFastInstanceChecker(KnowledgeSource... sources) {
        super(new HashSet<KnowledgeSource>(Arrays.asList(sources)));
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

		if (rc == null) {
			rc = new OWLAPIReasoner(sources);
			rc.init();
		}
		atomicConcepts = rc.getNamedClasses();
		datatypeProperties = rc.getDatatypeProperties();
		booleanDatatypeProperties = rc.getBooleanDatatypeProperties();
		doubleDatatypeProperties = rc.getDoubleDatatypeProperties();
		intDatatypeProperties = rc.getIntDatatypeProperties();
		stringDatatypeProperties = rc.getStringDatatypeProperties();
		atomicRoles = rc.getObjectProperties();
		individuals = (TreeSet<Individual>) rc.getIndividuals();

		loadOrDematerialize();
	}
	
	private void loadOrDematerialize(){
		if(useCaching){
			File cacheDir = new File("cache");
			cacheDir.mkdirs();
			HashFunction hf = Hashing.md5();
			Hasher hasher = hf.newHasher();
			hasher.putBoolean(materializeExistentialRestrictions);
			hasher.putBoolean(handlePunning);
			for (OWLOntology ont : rc.getOWLAPIOntologies()) {
				hasher.putInt(ont.getLogicalAxioms().hashCode());
				hasher.putInt(ont.getAxioms().hashCode());
			}
			String filename = hasher.hash().toString() + ".obj";
			
			File cacheFile = new File(cacheDir, filename);
			if(cacheFile.exists()){
				logger.debug("Loading materialization from disk...");
				try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(cacheFile))){
					Materialization mat = (Materialization) ois.readObject();
					classInstancesPos = mat.classInstancesPos;
					classInstancesNeg = mat.classInstancesNeg;
					opPos = mat.opPos;
					dpPos = mat.dpPos;
					bdPos = mat.bdPos;
					bdNeg = mat.bdNeg;
					dd = mat.dd;
					id = mat.id;
					sd = mat.sd;
				} catch (ClassNotFoundException | IOException e) {
					e.printStackTrace();
				} 
				logger.debug("done.");
			} else {
				dematerialize();
				Materialization mat = new Materialization();
				mat.classInstancesPos = classInstancesPos;
				mat.classInstancesNeg = classInstancesNeg;
				mat.opPos = opPos;
				mat.dpPos = dpPos;
				mat.bdPos = bdPos;
				mat.bdNeg = bdNeg;
				mat.dd = dd;
				mat.id = id;
				mat.sd = sd;
				try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(cacheFile))){
					oos.writeObject(mat);
				} catch (IOException e) {
					e.printStackTrace();
				} 
			}
		} else {
			dematerialize();
		}
	}
	
	private void dematerialize(){
		long dematStartTime = System.currentTimeMillis();

		//atomic concepts
		logger.debug("dematerialising concepts");
		Set<NamedClass> classes = rc.getNamedClasses();
		int i = 1;
		for (NamedClass atomicConcept : classes) {
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

		//atomic object properties
		logger.debug("dematerialising object properties");
		for (ObjectProperty atomicRole : atomicRoles) {
			opPos.put(atomicRole, rc.getPropertyMembers(atomicRole));
		}
		
		//atomic datatype properties
		logger.debug("dematerialising datatype properties");
		for (DatatypeProperty atomicRole : datatypeProperties) {
			dpPos.put(atomicRole, rc.getDatatypeMembers(atomicRole));
		}

		//boolean datatype properties
		for (DatatypeProperty dp : booleanDatatypeProperties) {
			bdPos.put(dp, (TreeSet<Individual>) rc.getTrueDatatypeMembers(dp));
			bdNeg.put(dp, (TreeSet<Individual>) rc.getFalseDatatypeMembers(dp));
		}

		//integer datatype properties
		for (DatatypeProperty dp : intDatatypeProperties) {
			id.put(dp, rc.getIntDatatypeMembers(dp));
		}

		//double datatype properties
		for (DatatypeProperty dp : doubleDatatypeProperties) {
			dd.put(dp, rc.getDoubleDatatypeMembers(dp));
		}

		//String datatype properties
		for (DatatypeProperty dp : stringDatatypeProperties) {
			sd.put(dp, rc.getStringDatatypeMembers(dp));
		}			
		
		// class assertion axioms with complex concepts
		List<OWLOntology> ontologies = rc.getOWLAPIOntologies();
		for (OWLOntology ontology : ontologies) {
			Set<OWLClassAssertionAxiom> axioms = ontology.getAxioms(AxiomType.CLASS_ASSERTION);
			// for each axiom C(x)
			for (OWLClassAssertionAxiom axiom : axioms) {
				OWLIndividual ind = axiom.getIndividual();
				OWLClassExpression ce = axiom.getClassExpression();
				// if C is of type 'r some D'
				if(ce instanceof OWLObjectSomeValuesFrom){
					OWLObjectPropertyExpression propertyExpression = ((OWLObjectSomeValuesFrom) ce).getProperty();
					OWLClassExpression filler = ((OWLObjectSomeValuesFrom) ce).getFiller();
					if(!propertyExpression.isAnonymous()){
						ObjectProperty prop = new ObjectProperty(propertyExpression.asOWLObjectProperty().toStringID());
						
						Map<Individual, SortedSet<Individual>> map = opPos.get(prop);
						if(map == null){
							map = new HashMap<Individual, SortedSet<Individual>>();
							opPos.put(prop, map);
						}
						
						Individual individual = new Individual(ind.toStringID());
						SortedSet<Individual> values = map.get(individual);
						if(values == null){
							values = new TreeSet<Individual>();
							map.put(individual, values);
						}
						
						// if there is not already at least one entry r(x,o) we add
						// r(x,o_genid)
						if(values.isEmpty()){
							Individual newIndividual = individualGenerator.newIndividual();
							values.add(newIndividual);
							
							// if D != owl:Thing and D is atomic, we add D_i(o_genid)
							// for all D_i \sqsubseteq D
							if(!filler.isOWLThing()){
								if(!filler.isAnonymous()){
									NamedClass cls = new NamedClass(filler.asOWLClass().toStringID());
									classInstancesPos.get(cls).add(newIndividual);
									// get all super classes and add genInd to each
									Set<OWLClass> superClasses = rc.getReasoner().getSuperClasses(ce, false).getFlattened();
									superClasses.remove(ontology.getOWLOntologyManager().getOWLDataFactory().getOWLThing());
									for (OWLClass sup : superClasses) {
										classInstancesPos.get(OWLAPIConverter.convertClass(sup)).add(newIndividual);
									}
									
								}
							}
						}
					} else {
						
					}
				}
			}
		}
		
		if(materializeExistentialRestrictions){
			logger.debug("Materializing existential restrictions ...");
			ExistentialRestrictionMaterialization materialization = new ExistentialRestrictionMaterialization(rc.getReasoner().getRootOntology());
			int cnt = 0;
			for (NamedClass cls : atomicConcepts) {System.out.println(cnt++ + "/" + atomicConcepts.size());
				TreeSet<Individual> individuals = classInstancesPos.get(cls);
				Set<OWLClassExpression> superClass = materialization.materialize(cls.getName());
				for (OWLClassExpression sup : superClass) {
					fill(individuals, DLLearnerDescriptionConvertVisitor.getDLLearnerDescription(sup));
				}
			}
			logger.debug("...done.");
		}
		
		//materialize facts based on OWL punning, i.e.:
		//for each A in N_C
		if(handlePunning){
			OWLOntology ontology = rc.getReasoner().getRootOntology();
			
			Individual genericIndividual = new Individual("http://dl-learner.org/punning#genInd");
			Map<Individual, SortedSet<Individual>> map = new HashMap<Individual, SortedSet<Individual>>();
			for (Individual individual : individuals) {
				SortedSet<Individual> objects = new TreeSet<Individual>();
				objects.add(genericIndividual);
				map.put(individual, objects);
			}
			for (NamedClass cls : atomicConcepts) {
				classInstancesNeg.get(cls).add(genericIndividual);
				if(OWLPunningDetector.hasPunning(ontology, cls)){
					Individual clsAsInd = new Individual(cls.getName());
					//for each x \in N_I with A(x) we add relatedTo(x,A)
					SortedSet<Individual> individuals = classInstancesPos.get(cls);
					for (Individual individual : individuals) {
						SortedSet<Individual> objects = map.get(individual);
						if(objects == null){
							objects = new TreeSet<Individual>();
							map.put(individual, objects);
						}
						objects.add(clsAsInd);
						
					}
				}
			}
			opPos.put(OWLPunningDetector.punningProperty, map);
			atomicRoles = new TreeSet<ObjectProperty>(atomicRoles);
			atomicRoles.add(OWLPunningDetector.punningProperty);
			atomicRoles = Collections.unmodifiableSet(atomicRoles);
//			individuals.add(genericIndividual);
		}
		
		long dematDuration = System.currentTimeMillis() - dematStartTime;
		logger.debug("TBox dematerialised in " + dematDuration + " ms");
	}
	
	private void fill(SortedSet<Individual> individuals, Description d){
		if(d instanceof Intersection){
			List<Description> children = d.getChildren();
			for (Description child : children) {
				fill(individuals, child);
			}
		} else if(d instanceof ObjectSomeRestriction){
			ObjectProperty role = (ObjectProperty) ((ObjectSomeRestriction) d).getRole();
			Map<Individual, SortedSet<Individual>> map = opPos.get(role);
			//create new individual as object value for each individual
			SortedSet<Individual> newIndividuals = new TreeSet<Individual>();
			for (Individual individual : individuals) {
				Individual newIndividual = individualGenerator.newIndividual();
				newIndividuals.add(newIndividual);
				SortedSet<Individual> values = map.get(individual);
				if(values == null){
					values = new TreeSet<Individual>();
					map.put(individual, values);
				}
				values.add(newIndividual);
			}
			fill(newIndividuals, d.getChild(0));
			
		} else if(d instanceof NamedClass){
			classInstancesPos.get(d).addAll(individuals);
		} else {
			throw new UnsupportedOperationException("Should not happen.");
		}
	}

	@Override
	public boolean hasTypeImpl(Description description, Individual individual)
			throws ReasoningMethodUnsupportedException {

//		 System.out.println("FIC: " + description + " " + individual);

		if (description instanceof NamedClass) {
			if(((NamedClass) description).getURI().equals(Thing.instance.getURI())){
				return true;
			} else
			if(!atomicConcepts.contains(description)) {
				throw new ReasoningMethodUnsupportedException("Class " + description + " is not contained in knowledge base.");
			}
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
			if(handlePunning && op == OWLPunningDetector.punningProperty && child.equals(new NamedClass(Thing.uri.toString()))){
				return true;
			}
			Map<Individual, SortedSet<Individual>> mapping = opPos.get(op);

			if (mapping == null) {
				logger.warn("Instance check of a description with an undefined property (" + op
						+ ").");
				return false;
			}
			
			SortedSet<Individual> roleFillers = mapping.get(individual);
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
				if(forallSemantics == ForallSemantics.Standard) {
					return true;	
				} else {
					return false;
				}
			}
			boolean hasCorrectFiller = false;
			for (Individual roleFiller : roleFillers) {
				if (hasTypeImpl(child, roleFiller)) {
					hasCorrectFiller = true;
				} else {
					return false;
				}				
			}
			
			if(forallSemantics == ForallSemantics.SomeOnly) {
				return hasCorrectFiller;
			} else {
				return true;
			}
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
			if (roleFillers == null
					|| (roleFillers.size() < number && op != OWLPunningDetector.punningProperty)
					) {
				return false;
			}

			int index = 0;
			for (Individual roleFiller : roleFillers) {
				index++;
				if (hasTypeImpl(child, roleFiller)) {
					nrOfFillers++;
					if (nrOfFillers == number 
							|| (handlePunning && op == OWLPunningDetector.punningProperty)
							) {
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
			if(dr.isDatatype() 
//					&& ((Datatype)dr).isTopDatatype()
					){
				 if(dpPos.get(dp).containsKey(individual)){
					 return true;
				 } else {
					 return false;
				 }
			}
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
			if(classInstancesPos.containsKey((NamedClass) description)){
				return (TreeSet<Individual>) classInstancesPos.get((NamedClass) description).clone();
			} else {
				return new TreeSet<Individual>();
			}
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
	
	public void setPrefixes(Map<String, String> prefixes) {
		rc.setPrefixes(prefixes);
	}
	
	/**
	 * @param baseURI the baseURI to set
	 */
	public void setBaseURI(String baseURI) {
		rc.setBaseURI(baseURI);
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
	public DataRange getRangeImpl(DatatypeProperty datatypeProperty) {
		return rc.getRange(datatypeProperty);
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
		return dpPos.get(datatypeProperty);
//		return rc.getDatatypeMembersImpl(datatypeProperty);
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

	public ForallSemantics getForAllSemantics() {
		return forallSemantics;
	}

	public void setForAllSemantics(ForallSemantics forallSemantics) {
		this.forallSemantics = forallSemantics;
	}
	
	/**
	 * @param materializeExistentialRestrictions the materializeExistentialRestrictions to set
	 */
	public void setMaterializeExistentialRestrictions(boolean materializeExistentialRestrictions) {
		this.materializeExistentialRestrictions = materializeExistentialRestrictions;
	}
	
	/**
	 * @param handlePunning the handlePunning to set
	 */
	public void setHandlePunning(boolean handlePunning) {
		this.handlePunning = handlePunning;
	}
	
	/**
	 * @param useCaching the useCaching to set
	 */
	public void setUseMaterializationCaching(boolean useCaching) {
		this.useCaching = useCaching;
	}
}
