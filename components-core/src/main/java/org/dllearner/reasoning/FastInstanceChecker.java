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
import java.util.Iterator;
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
import org.dllearner.utilities.Helper;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLFacetRestriction;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.vocab.OWLFacet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;

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
@ComponentAnn(name = "fast instance checker", shortName = "fic", version = 0.9)
public class FastInstanceChecker extends AbstractReasonerComponent {

	private static Logger logger = Logger.getLogger(FastInstanceChecker.class);

//	private boolean defaultNegation = true;

	private Set<OWLClass> atomicConcepts;
	private Set<OWLObjectProperty> atomicRoles;
	private Set<OWLDataProperty> datatypeProperties;
	private Set<OWLDataProperty> booleanDatatypeProperties = new TreeSet<OWLDataProperty>();
	private Set<OWLDataProperty> doubleDatatypeProperties = new TreeSet<OWLDataProperty>();
	private Set<OWLDataProperty> intDatatypeProperties = new TreeSet<OWLDataProperty>();
	private Set<OWLDataProperty> stringDatatypeProperties = new TreeSet<OWLDataProperty>();	
	private TreeSet<OWLIndividual> individuals;

	// private ReasonerComponent rs;

	private OWLAPIReasoner rc;

	// we use sorted sets (map indices) here, because they have only log(n)
	// complexity for checking whether an element is contained in them
	// instances of classes
	private Map<OWLClass, TreeSet<OWLIndividual>> classInstancesPos = new TreeMap<OWLClass, TreeSet<OWLIndividual>>();
	private Map<OWLClass, TreeSet<OWLIndividual>> classInstancesNeg = new TreeMap<OWLClass, TreeSet<OWLIndividual>>();
	// object property mappings
	private Map<OWLObjectProperty, Map<OWLIndividual, SortedSet<OWLIndividual>>> opPos = new TreeMap<OWLObjectProperty, Map<OWLIndividual, SortedSet<OWLIndividual>>>();
	// data property mappings
	private Map<OWLDataProperty, Map<OWLIndividual, SortedSet<OWLLiteral>>> dpPos = new TreeMap<OWLDataProperty, Map<OWLIndividual, SortedSet<OWLLiteral>>>();
		
	
	// datatype property mappings
	// we have one mapping for true and false for efficiency reasons
	private Map<OWLDataProperty, TreeSet<OWLIndividual>> bdPos = new TreeMap<OWLDataProperty, TreeSet<OWLIndividual>>();
	private Map<OWLDataProperty, TreeSet<OWLIndividual>> bdNeg = new TreeMap<OWLDataProperty, TreeSet<OWLIndividual>>();
	// for int and double we assume that a property can have several values,
	// althoug this should be rare,
	// e.g. hasValue(object,2) and hasValue(object,3)
	private Map<OWLDataProperty, Map<OWLIndividual, SortedSet<Double>>> dd = new TreeMap<OWLDataProperty, Map<OWLIndividual, SortedSet<Double>>>();
	private Map<OWLDataProperty, Map<OWLIndividual, SortedSet<Integer>>> id = new TreeMap<OWLDataProperty, Map<OWLIndividual, SortedSet<Integer>>>();
	private Map<OWLDataProperty, Map<OWLIndividual, SortedSet<String>>> sd = new TreeMap<OWLDataProperty, Map<OWLIndividual, SortedSet<String>>>();
	
	private Map<OWLDataProperty, Map<OWLIndividual, SortedSet<Number>>> numericValueMappings = new TreeMap<OWLDataProperty, Map<OWLIndividual, SortedSet<Number>>>();
	
	
	
    @ConfigOption(name="defaultNegation", description = "Whether to use default negation, i.e. an instance not being in a class means that it is in the negation of the class.", defaultValue = "true", required = false)
    private boolean defaultNegation = true;

    @ConfigOption(name = "forAllRetrievalSemantics", description = "This option controls how to interpret the all quantifier in forall r.C. The standard option is" +
            "to return all those which do not have an r-filler not in C. The domain semantics is to use those" +
            "which are in the domain of r and do not have an r-filler not in C. The forallExists semantics is to"+
            "use those which have at least one r-filler and do not have an r-filler not in C.",defaultValue = "standard",propertyEditorClass = StringTrimmerEditor.class)
    private ForallSemantics forallSemantics = ForallSemantics.Standard;

    public enum ForallSemantics { 
    	Standard, // standard all quantor
    	NonEmpty, // p only C for instance a returns false if there is no fact p(a,x) for any x  
    	SomeOnly  // p only C for instance a returns false if there is no fact p(a,x) with x \ in C  
    }
    
    private boolean materializeExistentialRestrictions = false;

	private boolean useCaching = true;
    private boolean handlePunning = false;
    
	/**
	 * Creates an instance of the fast instance checker.
	 */
	public FastInstanceChecker() {
	}

    public FastInstanceChecker(TreeSet<OWLIndividual> individuals,
			Map<OWLClass, TreeSet<OWLIndividual>> classInstancesPos,
			Map<OWLObjectProperty, Map<OWLIndividual, SortedSet<OWLIndividual>>> opPos,
			Map<OWLDataProperty, Map<OWLIndividual, SortedSet<Integer>>> id,
			Map<OWLDataProperty, TreeSet<OWLIndividual>> bdPos,
			Map<OWLDataProperty, TreeSet<OWLIndividual>> bdNeg,
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
		
		atomicConcepts = rc.getClasses();
		datatypeProperties = rc.getDatatypeProperties();
		booleanDatatypeProperties = rc.getBooleanDatatypeProperties();
		doubleDatatypeProperties = rc.getDoubleDatatypeProperties();
		intDatatypeProperties = rc.getIntDatatypeProperties();
		stringDatatypeProperties = rc.getStringDatatypeProperties();
		atomicRoles = rc.getObjectProperties();
		
		for (OWLClass atomicConcept : atomicConcepts) {
			TreeSet<OWLIndividual> pos = classInstancesPos.get(atomicConcept);
			if(pos != null){
				classInstancesNeg.put(atomicConcept, (TreeSet<OWLIndividual>) Helper.difference(individuals, pos));
			} else {
				classInstancesPos.put(atomicConcept, new TreeSet<OWLIndividual>());
				classInstancesNeg.put(atomicConcept, individuals);
			}
		}
		
		for(OWLObjectProperty p : atomicRoles){
			if(opPos.get(p) == null){
				opPos.put(p, new HashMap<OWLIndividual, SortedSet<OWLIndividual>>());
			}
		}
		
		for (OWLDataProperty dp : booleanDatatypeProperties) {
			if(bdPos.get(dp) == null){
				bdPos.put(dp, new TreeSet<OWLIndividual>());
			}
			if(bdNeg.get(dp) == null){
				bdNeg.put(dp, new TreeSet<OWLIndividual>());
			}
			
		}
	}

	public FastInstanceChecker(Set<KnowledgeSource> sources) {
        super(sources);
    }

    public FastInstanceChecker(KnowledgeSource... sources) {
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

        if(rc == null){
            rc = new OWLAPIReasoner(sources);
            rc.init();
        }
		atomicConcepts = rc.getClasses();
		datatypeProperties = rc.getDatatypeProperties();
		booleanDatatypeProperties = rc.getBooleanDatatypeProperties();
		doubleDatatypeProperties = rc.getDoubleDatatypeProperties();
		intDatatypeProperties = rc.getIntDatatypeProperties();
		stringDatatypeProperties = rc.getStringDatatypeProperties();
		atomicRoles = rc.getObjectProperties();
		individuals = (TreeSet<OWLIndividual>) rc.getIndividuals();
		
//		loadOrDematerialize();
		materialize();
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
				materialize();
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
			materialize();
		}
	}
	
	private void materialize(){
		long dematStartTime = System.currentTimeMillis();

		logger.debug("materialising concepts");
		for (OWLClass atomicConcept : atomicConcepts) {
			if(!atomicConcept.getIRI().isReservedVocabulary()){
				SortedSet<OWLIndividual> pos = rc.getIndividuals(atomicConcept);
				classInstancesPos.put(atomicConcept, (TreeSet<OWLIndividual>) pos);

				if (isDefaultNegation()) {
					classInstancesNeg.put(atomicConcept, (TreeSet<OWLIndividual>) Helper.difference(individuals, pos));
				} else {
					// Pellet needs approximately infinite time to answer
					// negated queries
					// on the carcinogenesis data set (and probably others), so
					// we have to
					// be careful here
					OWLObjectComplementOf negatedAtomicConcept = df.getOWLObjectComplementOf(atomicConcept);
					classInstancesNeg.put(atomicConcept, (TreeSet<OWLIndividual>) rc.getIndividuals(negatedAtomicConcept));
				}
			} else {
				System.err.println(atomicConcept);
			}
		}

		logger.debug("materialising object properties");

		for (OWLObjectProperty atomicRole : atomicRoles) {
//			System.out.println(atomicRole + " " + rc.getPropertyMembers(atomicRole));
			opPos.put(atomicRole, rc.getPropertyMembers(atomicRole));
		}
		
		logger.debug("materialising datatype properties");
		
		for (OWLDataProperty atomicRole : datatypeProperties) {
			dpPos.put(atomicRole, rc.getDatatypeMembers(atomicRole));
		}

		for (OWLDataProperty dp : booleanDatatypeProperties) {
			bdPos.put(dp, (TreeSet<OWLIndividual>) rc.getTrueDatatypeMembers(dp));
			bdNeg.put(dp, (TreeSet<OWLIndividual>) rc.getFalseDatatypeMembers(dp));
		}

		for (OWLDataProperty dp : intDatatypeProperties) {
			id.put(dp, rc.getIntDatatypeMembers(dp));
		}

		for (OWLDataProperty dp : doubleDatatypeProperties) {
			dd.put(dp, rc.getDoubleDatatypeMembers(dp));
		}

		for (OWLDataProperty dp : stringDatatypeProperties) {
			sd.put(dp, rc.getStringDatatypeMembers(dp));
		}		
		
		if(materializeExistentialRestrictions){
			ExistentialRestrictionMaterialization materialization = new ExistentialRestrictionMaterialization(rc.getReasoner().getRootOntology());
			for (OWLClass cls : atomicConcepts) {
				TreeSet<OWLIndividual> individuals = classInstancesPos.get(cls);
				Set<OWLClassExpression> superClass = materialization.materialize(cls.toStringID());
				for (OWLClassExpression sup : superClass) {
					fill(individuals, sup);
				}
			}
		}
		
		//materialize facts based on OWL punning, i.e.:
				//for each A in N_C
				if(handlePunning && OWLPunningDetector.hasPunning(rc.getReasoner().getRootOntology())){
					OWLOntology ontology = rc.getReasoner().getRootOntology();
					
					OWLIndividual genericIndividual = df.getOWLNamedIndividual(IRI.create("http://dl-learner.org/punning#genInd"));
					Map<OWLIndividual, SortedSet<OWLIndividual>> map = new HashMap<OWLIndividual, SortedSet<OWLIndividual>>();
					for (OWLIndividual individual : individuals) {
						SortedSet<OWLIndividual> objects = new TreeSet<OWLIndividual>();
						objects.add(genericIndividual);
						map.put(individual, objects);
					}
					for (OWLClass cls : atomicConcepts) {
						classInstancesNeg.get(cls).add(genericIndividual);
						if(OWLPunningDetector.hasPunning(ontology, cls)){
							OWLIndividual clsAsInd = df.getOWLNamedIndividual(IRI.create(cls.toStringID()));
							//for each x \in N_I with A(x) we add relatedTo(x,A)
							SortedSet<OWLIndividual> individuals = classInstancesPos.get(cls);
							for (OWLIndividual individual : individuals) {
								SortedSet<OWLIndividual> objects = map.get(individual);
								if(objects == null){
									objects = new TreeSet<OWLIndividual>();
									map.put(individual, objects);
								}
								objects.add(clsAsInd);
								
							}
						}
					}
					opPos.put(OWLPunningDetector.punningProperty, map);
					atomicRoles = new TreeSet<OWLObjectProperty>(atomicRoles);
					atomicRoles.add(OWLPunningDetector.punningProperty);
					atomicRoles = Collections.unmodifiableSet(atomicRoles);
//					individuals.add(genericIndividual);
				}
		
		long dematDuration = System.currentTimeMillis() - dematStartTime;
		logger.debug("TBox materialised in " + dematDuration + " ms");
	}
	
	private void fill(SortedSet<OWLIndividual> individuals, OWLClassExpression d){
		if(!d.isAnonymous()){
			classInstancesPos.get(d).addAll(individuals);
		} else if(d instanceof OWLObjectIntersectionOf){
			Set<OWLClassExpression> operands = ((OWLObjectIntersectionOf) d).getOperands();
			for (OWLClassExpression operand : operands) {
				fill(individuals, operand);
			}
		} else if(d instanceof OWLObjectSomeValuesFrom){
			OWLObjectProperty role = ((OWLObjectSomeValuesFrom) d).getProperty().asOWLObjectProperty();
			OWLClassExpression filler = ((OWLObjectSomeValuesFrom) d).getFiller();
			Map<OWLIndividual, SortedSet<OWLIndividual>> map = opPos.get(role);
			//create new individual as object value for each individual
			SortedSet<OWLIndividual> newIndividuals = new TreeSet<OWLIndividual>();
			int i = 0;
			for (OWLIndividual individual : individuals) {
				OWLIndividual newIndividual = df.getOWLNamedIndividual(IRI.create("http://dllearner.org#genInd_" + i++));
				newIndividuals.add(newIndividual);
				SortedSet<OWLIndividual> values = map.get(individual);
				if(values == null){
					values = new TreeSet<OWLIndividual>();
					map.put(individual, values);
				}
				values.add(newIndividual);
			}
			fill(newIndividuals, filler);
			
		} else {
			throw new UnsupportedOperationException("Should not happen.");
		}
	}

	@Override
	public boolean hasTypeImpl(OWLClassExpression description, OWLIndividual individual)
			throws ReasoningMethodUnsupportedException {

		if (description.isOWLThing()) {
			return true;
		} else if (description.isOWLNothing()) {
			return false;
		} else if (!description.isAnonymous()) {
			return classInstancesPos.get(description).contains(individual);
		} else if (description instanceof OWLObjectComplementOf) {
			OWLClassExpression operand = ((OWLObjectComplementOf) description).getOperand();
			if(!operand.isAnonymous()) {
				return classInstancesNeg.get(operand).contains(individual);
			} else {
				if(isDefaultNegation()) {
					return !hasTypeImpl(operand, individual);
				} else {
					logger.debug("Converting OWLClassExpression to negation normal form in fast instance check (should be avoided if possible).");
					return hasTypeImpl(description.getNNF(), individual);					
				}
			}
		} else if (description instanceof OWLObjectUnionOf) {
			for (OWLClassExpression operand : ((OWLObjectUnionOf) description).getOperands()) {
				if(hasTypeImpl(operand, individual)){
					return true;
				}
			}
			return false;
		} else if (description instanceof OWLObjectIntersectionOf) {
			for (OWLClassExpression operand : ((OWLObjectIntersectionOf) description).getOperands()) {
				if(!hasTypeImpl(operand, individual)){
					return false;
				}
			}
			return true;
		}
		else if (description instanceof OWLObjectSomeValuesFrom) {
			OWLObjectPropertyExpression property = ((OWLObjectSomeValuesFrom) description).getProperty();
			OWLClassExpression fillerConcept = ((OWLObjectSomeValuesFrom) description).getFiller();
			
			if (property.isAnonymous()) {
				throw new ReasoningMethodUnsupportedException("Retrieval for OWLClassExpression "
						+ description + " unsupported. Inverse object properties not supported.");
			}
			
			if(handlePunning && property == OWLPunningDetector.punningProperty && fillerConcept.isOWLThing()){
				return true;
			}
			
			SortedSet<OWLIndividual> roleFillers = opPos.get(property.asOWLObjectProperty()).get(individual);	
			
			if(roleFillers == null){
				return false;
			}
			
			for (OWLIndividual roleFiller : roleFillers) {
				if (hasTypeImpl(fillerConcept, roleFiller)) {
					return true;
				}
			}
			
			return false;
		} else if (description instanceof OWLObjectAllValuesFrom) {
			OWLObjectPropertyExpression property = ((OWLObjectAllValuesFrom) description).getProperty();
			OWLClassExpression fillerConcept = ((OWLObjectAllValuesFrom) description).getFiller();
			
			if (property.isAnonymous()) {
				throw new ReasoningMethodUnsupportedException("Retrieval for OWLClassExpression "
						+ description + " unsupported. Inverse object properties not supported.");
			}
			
			Map<OWLIndividual, SortedSet<OWLIndividual>> mapping = opPos.get(property);
			
			SortedSet<OWLIndividual> roleFillers = mapping.get(individual);
			
			if (roleFillers == null) {
                return forallSemantics == ForallSemantics.Standard;
			}
			boolean hasCorrectFiller = false;
			for (OWLIndividual roleFiller : roleFillers) {
				if (hasTypeImpl(fillerConcept, roleFiller)) {
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
		} else if (description instanceof OWLObjectMinCardinality) {
			OWLObjectPropertyExpression property = ((OWLObjectMinCardinality) description).getProperty();
			OWLClassExpression fillerConcept = ((OWLObjectMinCardinality) description).getFiller();
			int cardinality = ((OWLObjectMinCardinality) description).getCardinality();
			
			// special case: there are always at least zero fillers
			if (cardinality == 0) {
				return true;
			}
			
			if (property.isAnonymous()) {
				throw new ReasoningMethodUnsupportedException("Retrieval for OWLClassExpression "
						+ description + " unsupported. Inverse object properties not supported.");
			}
			
			Map<OWLIndividual, SortedSet<OWLIndividual>> mapping = opPos.get(property);

			int nrOfFillers = 0;

			SortedSet<OWLIndividual> roleFillers = mapping.get(individual);
			
			// return false if there are none or not enough role fillers
			if (roleFillers == null || (roleFillers.size() < cardinality && property != OWLPunningDetector.punningProperty)) {
				return false;
			}

			int index = 0;
			for (OWLIndividual roleFiller : roleFillers) {
				index++;
				if (hasTypeImpl(fillerConcept, roleFiller)) {
					nrOfFillers++;
					if (nrOfFillers == cardinality
							|| (handlePunning && property == OWLPunningDetector.punningProperty)) {
						return true;
					}
					// early abort: e.g. >= 10 hasStructure.Methyl;
					// if there are 11 fillers and 2 are not Methyl, the result
					// is false
				} else {
					if (roleFillers.size() - index < cardinality) {
						return false;
					}
				}
			}
			return false;
		} else if (description instanceof OWLObjectMaxCardinality) {
			OWLObjectPropertyExpression property = ((OWLObjectMaxCardinality) description).getProperty();
			OWLClassExpression fillerConcept = ((OWLObjectMaxCardinality) description).getFiller();
			int cardinality = ((OWLObjectMaxCardinality) description).getCardinality();
			
			if (property.isAnonymous()) {
				throw new ReasoningMethodUnsupportedException("Retrieval for OWLClassExpression "
						+ description + " unsupported. Inverse object properties not supported.");
			}
			
			Map<OWLIndividual, SortedSet<OWLIndividual>> mapping = opPos.get(property);

			int nrOfFillers = 0;

			SortedSet<OWLIndividual> roleFillers = mapping.get(individual);
			
			// return true if there are none or not enough role fillers
			if (roleFillers == null || roleFillers.size() < cardinality) {
				return true;
			}

			int index = 0;
			for (OWLIndividual roleFiller : roleFillers) {
				index++;
				if (hasTypeImpl(fillerConcept, roleFiller)) {
					nrOfFillers++;
					if (nrOfFillers > cardinality) {
						return false;
					}
					// early abort: e.g. <= 5 hasStructure.Methyl;
					// if there are 6 fillers and 2 are not Methyl, the result
					// is true
				} else {
					if (roleFillers.size() - index <= cardinality) {
						return true;
					}
				}
			}
			return true;
		} else if (description instanceof OWLObjectHasValue) {
			OWLObjectPropertyExpression property = ((OWLObjectHasValue) description).getProperty();
			OWLIndividual value = ((OWLObjectHasValue)description).getValue();
			
			if (property.isAnonymous()) {
				throw new ReasoningMethodUnsupportedException("Retrieval for OWLClassExpression "
						+ description + " unsupported. Inverse object properties not supported.");
			}
			
			Map<OWLIndividual, SortedSet<OWLIndividual>> mapping = opPos.get(property.asOWLObjectProperty());
			
			SortedSet<OWLIndividual> values = mapping.get(individual);
			
			return values != null && values.contains(value);
		} 
//		else if (OWLClassExpression instanceof BooleanValueRestriction) {
//			DatatypeProperty dp = ((BooleanValueRestriction) description)
//					.getRestrictedPropertyExpression();
//			boolean value = ((BooleanValueRestriction) description).getBooleanValue();
//
//			if (value) {
//				// check whether the OWLIndividual is in the set of individuals
//				// mapped
//				// to true by this datatype property
//				return bdPos.get(dp).contains(individual);
//			} else {
//				return bdNeg.get(dp).contains(individual);
//			}
//		} 
		else if (description instanceof OWLDataSomeValuesFrom) {
			OWLDataPropertyExpression property = ((OWLDataSomeValuesFrom) description).getProperty();
			OWLDataRange filler = ((OWLDataSomeValuesFrom) description).getFiller();
			
			if (property.isAnonymous()) {
				throw new ReasoningMethodUnsupportedException("Retrieval for OWLClassExpression "
						+ description + " unsupported. Inverse object properties not supported.");
			}
			
			if(filler.isDatatype()){
				 return dpPos.get(property).containsKey(individual);
			} else if(filler instanceof OWLDatatypeRestriction){
				OWLDatatype datatype = ((OWLDatatypeRestriction) filler).getDatatype();
				Set<OWLFacetRestriction> facetRestrictions = ((OWLDatatypeRestriction) filler).getFacetRestrictions();
				
				if(datatype.isDouble()){
					SortedSet<Double> values = dd.get(property).get(individual);
					
					if(values == null) {
						return false;
					}
					
					double min = -Double.MAX_VALUE;
					double max = Double.MAX_VALUE;
					for (OWLFacetRestriction facet : facetRestrictions) {
						if(facet.getFacet() == OWLFacet.MIN_INCLUSIVE){
							min = facet.getFacetValue().parseDouble();
						} else if(facet.getFacet() == OWLFacet.MAX_INCLUSIVE){
							max = facet.getFacetValue().parseDouble();
						} 
					}
					
					//we can return false if largest number is below minimum or lowest number is above maximum
					if(values.last() < min || values.first() > max) {
						return false;
					}
					
					//search a value which is in the interval
					for (Double value : values) {
						if(value >= min && value <= max){
							return true;
						}
					}
				} else if(datatype.isInteger()){
					SortedSet<Integer> values = id.get(property).get(individual);
					
					int min = Integer.MIN_VALUE;
					int max = Integer.MAX_VALUE;
					for (OWLFacetRestriction facet : facetRestrictions) {
						if(facet.getFacet() == OWLFacet.MIN_INCLUSIVE){
							min = facet.getFacetValue().parseInteger();
						} else if(facet.getFacet() == OWLFacet.MAX_INCLUSIVE){
							max = facet.getFacetValue().parseInteger();
						} 
					}
					
					//we can return false if largest number is below minimum or lowest number is above maximum
					if(values.last() < min || values.first() > max) {
						return false;
					}
					
					//search a value which is in the interval
					for (Integer value : values) {
						if(value >= min && value <= max){
							return true;
						}
					}
				}
			}
		} else if (description instanceof OWLDataHasValue) {
			OWLDataPropertyExpression property = ((OWLDataHasValue) description).getProperty();
			OWLLiteral value = ((OWLDataHasValue) description).getValue();
			
			if (property.isAnonymous()) {
				throw new ReasoningMethodUnsupportedException("Retrieval for OWLClassExpression "
						+ description + " unsupported. Inverse object properties not supported.");
			}
			
			Map<OWLIndividual, SortedSet<OWLLiteral>> mapping = dpPos.get(property);
			
			SortedSet<OWLLiteral> values = mapping.get(individual);
			
			return values != null && values.contains(value);
		}

		throw new ReasoningMethodUnsupportedException("Instance check for OWLClassExpression "
				+ description + " unsupported.");
	}

	@Override
	public SortedSet<OWLIndividual> getIndividualsImpl(OWLClassExpression concept) throws ReasoningMethodUnsupportedException {
		return getIndividualsImplFast(concept);
	}
	
	public SortedSet<OWLIndividual> getIndividualsImplStandard(OWLClassExpression concept)
		throws ReasoningMethodUnsupportedException {
		if (!concept.isAnonymous()) {
	 		return classInstancesPos.get((OWLClass) concept);
	 	} else if (concept instanceof OWLObjectComplementOf){
	 		OWLClassExpression operand = ((OWLObjectComplementOf) concept).getOperand();
	 		if(!operand.isAnonymous()) {
	 			return classInstancesNeg.get(operand);
	 		}
	 	}
	 
	 	// return rs.retrieval(concept);
	 	SortedSet<OWLIndividual> inds = new TreeSet<OWLIndividual>();
	 	for (OWLIndividual i : individuals) {
	 		if (hasType(concept, i)) {
	 			inds.add(i);
	 		}
	 	}
		return inds;
	}
	
	@SuppressWarnings("unchecked")
	public SortedSet<OWLIndividual> getIndividualsImplFast(OWLClassExpression description)
			throws ReasoningMethodUnsupportedException {
		// policy: returned sets are clones, i.e. can be modified
		// (of course we only have to clone the leafs of a class OWLClassExpression tree)
		if (description.isOWLThing()) {
			return (TreeSet<OWLIndividual>) individuals.clone();
		} else if (description.isOWLNothing()) {
			return new TreeSet<OWLIndividual>();
		} else if (!description.isAnonymous()) {
			if(classInstancesPos.containsKey(description.asOWLClass())){
				return (TreeSet<OWLIndividual>) classInstancesPos.get(description).clone();
			} else {
				return new TreeSet<OWLIndividual>();
			}
		} else if (description instanceof OWLObjectComplementOf) {
			OWLClassExpression operand = ((OWLObjectComplementOf) description).getOperand();
			if(!operand.isAnonymous()) {
				return (TreeSet<OWLIndividual>) classInstancesNeg.get(operand).clone();
			}
			// implement retrieval as default negation
			return Helper.difference((TreeSet<OWLIndividual>) individuals.clone(), getIndividualsImpl(operand));
		} else if (description instanceof OWLObjectUnionOf) {
			SortedSet<OWLIndividual> ret = new TreeSet<OWLIndividual>();
			for (OWLClassExpression operand : ((OWLObjectUnionOf) description).getOperands()) {
				ret.addAll(getIndividualsImpl(operand));
			}
			return ret;
		} else if (description instanceof OWLObjectIntersectionOf) {
			Iterator<OWLClassExpression> iterator = ((OWLObjectIntersectionOf) description).getOperands().iterator();
			// copy instances of first element and then subtract all others
			SortedSet<OWLIndividual> ret = getIndividualsImpl(iterator.next());
			while(iterator.hasNext()){
				ret.retainAll(getIndividualsImpl(iterator.next()));
			}
			return ret;
		} else if (description instanceof OWLObjectSomeValuesFrom) {
			SortedSet<OWLIndividual> returnSet = new TreeSet<OWLIndividual>();
			
			OWLObjectPropertyExpression property = ((OWLObjectSomeValuesFrom) description).getProperty();
			OWLClassExpression filler = ((OWLObjectSomeValuesFrom) description).getFiller();
			
			//get instances of filler concept
			SortedSet<OWLIndividual> targetSet = getIndividualsImpl(filler);
			
			if (property.isAnonymous()) {
				throw new ReasoningMethodUnsupportedException("Retrieval for OWLClassExpression "
						+ description + " unsupported. Inverse object properties not supported.");
			}
			Map<OWLIndividual, SortedSet<OWLIndividual>> mapping = opPos.get(property.asOWLObjectProperty());			
			
			// each individual is connected to a set of individuals via the property;
			// we loop through the complete mapping
			for(Entry<OWLIndividual, SortedSet<OWLIndividual>> entry : mapping.entrySet()) {
				SortedSet<OWLIndividual> inds = entry.getValue();
				for(OWLIndividual ind : inds) {
					if(targetSet.contains(ind)) {
						returnSet.add(entry.getKey());
						// once we found an individual, we do not need to check the others
						break;
					}
				}
			}
			return returnSet;
		} else if (description instanceof OWLObjectAllValuesFrom) {
			// \forall restrictions are difficult to handle; assume we want to check
			// \forall hasChild.male with domain(hasChild)=Person; then for all non-persons
			// this is satisfied trivially (all of their non-existing children are male)
//			if(!configurator.getForallRetrievalSemantics().equals("standard")) {
//				throw new Error("Only forallExists semantics currently implemented.");
//			}
			
			// problem: we need to make sure that \neg \exists r.\top \equiv \forall r.\bot
			// can still be reached in an algorithm (\forall r.\bot \equiv \bot under forallExists
			// semantics)
			
			OWLObjectPropertyExpression property = ((OWLObjectAllValuesFrom) description).getProperty();
			OWLClassExpression filler = ((OWLObjectAllValuesFrom) description).getFiller();
			
			if (property.isAnonymous()) {
				throw new ReasoningMethodUnsupportedException("Retrieval for OWLClassExpression "
						+ description + " unsupported. Inverse object properties not supported.");
			}
			
			//get instances of filler concept
			SortedSet<OWLIndividual> targetSet = getIndividualsImpl(filler);
			
			Map<OWLIndividual, SortedSet<OWLIndividual>> mapping = opPos.get(property.asOWLObjectProperty());
//			SortedSet<OWLIndividual> returnSet = new TreeSet<OWLIndividual>(mapping.keySet());
			SortedSet<OWLIndividual> returnSet = (SortedSet<OWLIndividual>) individuals.clone();
			
			// each individual is connected to a set of individuals via the property;
			// we loop through the complete mapping
			for(Entry<OWLIndividual, SortedSet<OWLIndividual>> entry : mapping.entrySet()) {
				SortedSet<OWLIndividual> inds = entry.getValue();
				for(OWLIndividual ind : inds) {
					if(!targetSet.contains(ind)) {
						returnSet.remove(entry.getKey());
						break;
					}
				}
			}
			return returnSet;
		} else if (description instanceof OWLObjectMinCardinality) {
			OWLObjectPropertyExpression property = ((OWLObjectMinCardinality) description).getProperty();
			OWLClassExpression filler = ((OWLObjectMinCardinality) description).getFiller();
			
			if (property.isAnonymous()) {
				throw new ReasoningMethodUnsupportedException("Retrieval for OWLClassExpression "
						+ description + " unsupported. Inverse object properties not supported.");
			}
			
			//get instances of filler concept
			SortedSet<OWLIndividual> targetSet = getIndividualsImpl(filler);
			
			Map<OWLIndividual, SortedSet<OWLIndividual>> mapping = opPos.get(property.asOWLObjectProperty());
			
			SortedSet<OWLIndividual> returnSet = new TreeSet<OWLIndividual>();

			int number = ((OWLObjectMinCardinality) description).getCardinality();

			for(Entry<OWLIndividual, SortedSet<OWLIndividual>> entry : mapping.entrySet()) {
				int nrOfFillers = 0;
				int index = 0;
				SortedSet<OWLIndividual> inds = entry.getValue();
				
				// we do not need to run tests if there are not sufficiently many fillers
				if(inds.size() < number) {
					continue;
				}
				
				for(OWLIndividual ind : inds) {
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
		} else if (description instanceof OWLObjectMaxCardinality) {
			OWLObjectPropertyExpression property = ((OWLObjectMaxCardinality) description).getProperty();
			OWLClassExpression filler = ((OWLObjectMaxCardinality) description).getFiller();
			int number = ((OWLObjectMaxCardinality) description).getCardinality();
			
			if (property.isAnonymous()) {
				throw new ReasoningMethodUnsupportedException("Retrieval for OWLClassExpression "
						+ description + " unsupported. Inverse object properties not supported.");
			}
			
			//get instances of filler concept
			SortedSet<OWLIndividual> targetSet = getIndividualsImpl(filler);
			
			Map<OWLIndividual, SortedSet<OWLIndividual>> mapping = opPos.get(property.asOWLObjectProperty());
			
			// initially all individuals are in the return set and we then remove those
			// with too many fillers			
			SortedSet<OWLIndividual> returnSet = (SortedSet<OWLIndividual>) individuals.clone();

			for(Entry<OWLIndividual, SortedSet<OWLIndividual>> entry : mapping.entrySet()) {
				int nrOfFillers = 0;
				int index = 0;
				SortedSet<OWLIndividual> inds = entry.getValue();
				
				// we do not need to run tests if there are not sufficiently many fillers
				if(number < inds.size()) {
					returnSet.add(entry.getKey());
					continue;
				}
				
				for(OWLIndividual ind : inds) {
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
		} else if (description instanceof OWLObjectHasValue) {
			OWLObjectPropertyExpression property = ((OWLObjectHasValue) description).getProperty();
			OWLIndividual value = ((OWLObjectHasValue)description).getValue();
			
			if (property.isAnonymous()) {
				throw new ReasoningMethodUnsupportedException("Retrieval for OWLClassExpression "
						+ description + " unsupported. Inverse object properties not supported.");
			}
			
			Map<OWLIndividual, SortedSet<OWLIndividual>> mapping = opPos.get(property.asOWLObjectProperty());
			
			SortedSet<OWLIndividual> returnSet = new TreeSet<OWLIndividual>();
			
			for(Entry<OWLIndividual, SortedSet<OWLIndividual>> entry : mapping.entrySet()) {
				if(entry.getValue().contains(value)) {
					returnSet.add(entry.getKey());
				}
			}
			return returnSet;
		} 
//		else if (description instanceof BooleanValueRestriction) {
//			DatatypeProperty dp = ((BooleanValueRestriction) description)
//					.getRestrictedPropertyExpression();
//			boolean value = ((BooleanValueRestriction) description).getBooleanValue();
//
//			if (value) {
//				return (TreeSet<OWLIndividual>) bdPos.get(dp).clone();
//			} else {
//				return (TreeSet<OWLIndividual>) bdNeg.get(dp).clone();
//			}
//		} 
		else if (description instanceof OWLDataSomeValuesFrom) {
			OWLDataPropertyExpression property = ((OWLDataSomeValuesFrom) description).getProperty();
			OWLDataRange filler = ((OWLDataSomeValuesFrom) description).getFiller();
			
			if (property.isAnonymous()) {
				throw new ReasoningMethodUnsupportedException("Retrieval for OWLClassExpression "
						+ description + " unsupported. Inverse object properties not supported.");
			}
			
			if(filler.isDatatype()){
				//we assume that the values are of the given datatype
				return new TreeSet<OWLIndividual>(dpPos.get(property).keySet());
//				OWLDatatype dt = filler.asOWLDatatype();
//				if(dt.isDouble()){
//					return new TreeSet<OWLIndividual>(dd.get(property).keySet());
//				} else if(dt.isInteger()){
//					return new TreeSet<OWLIndividual>(id.get(property).keySet());
//				} else if(dt.isBoolean()){
//					return bdPos.get(property);
//				}
			} else if(filler instanceof OWLDatatypeRestriction){
				OWLDatatype datatype = ((OWLDatatypeRestriction) filler).getDatatype();
				Set<OWLFacetRestriction> facetRestrictions = ((OWLDatatypeRestriction) filler).getFacetRestrictions();
				
				if(datatype.isDouble()){
					double min = -Double.MAX_VALUE;
					double max = Double.MAX_VALUE;
					for (OWLFacetRestriction facet : facetRestrictions) {
						if(facet.getFacet() == OWLFacet.MIN_INCLUSIVE){
							min = facet.getFacetValue().parseDouble();
						} else if(facet.getFacet() == OWLFacet.MAX_INCLUSIVE){
							max = facet.getFacetValue().parseDouble();
						} 
					}
					Map<OWLIndividual, SortedSet<Double>> mapping = dd.get(property);			
					SortedSet<OWLIndividual> returnSet = new TreeSet<OWLIndividual>();	
					
					for(Entry<OWLIndividual, SortedSet<Double>> entry : mapping.entrySet()) {
						//we can skip of largest number is below minimum or lowest number is above maximum
						if(entry.getValue().last() < min ||
								entry.getValue().first() > max) {
							continue;
						}
						
						//search a value which is in the interval
						for (Double value : entry.getValue()) {
							if(value >= min && value <= max){
								returnSet.add(entry.getKey());
								break;
							}
						}
					}
					return returnSet;
				}
			}
		} else if (description instanceof OWLDataHasValue){
			OWLDataPropertyExpression property = ((OWLDataHasValue) description).getProperty();
			OWLLiteral value = ((OWLDataHasValue) description).getValue();
			
			if (property.isAnonymous()) {
				throw new ReasoningMethodUnsupportedException("Retrieval for OWLClassExpression "
						+ description + " unsupported. Inverse object properties not supported.");
			}
			
			SortedSet<OWLIndividual> returnSet = new TreeSet<OWLIndividual>();	
			
			Map<OWLIndividual, SortedSet<OWLLiteral>> mapping = dpPos.get(property);
			
			for(Entry<OWLIndividual, SortedSet<OWLLiteral>> entry : mapping.entrySet()) {
				if(entry.getValue().contains(value)) {
					returnSet.add(entry.getKey());
				}
			}
			
			return returnSet;
		}
			
		throw new ReasoningMethodUnsupportedException("Retrieval for OWLClassExpression "
					+ description + " unsupported.");		
			
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.Reasoner#getAtomicConcepts()
	 */
	@Override
	public Set<OWLClass> getClasses() {
		return atomicConcepts;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.Reasoner#getAtomicRoles()
	 */
	@Override
	public Set<OWLObjectProperty> getObjectProperties() {
		return atomicRoles;
	}

	@Override
	public Set<OWLDataProperty> getDatatypePropertiesImpl() {
		return datatypeProperties;
	}

	@Override
	public Set<OWLDataProperty> getBooleanDatatypePropertiesImpl() {
		return booleanDatatypeProperties;
	}

	@Override
	public Set<OWLDataProperty> getDoubleDatatypePropertiesImpl() {
		return doubleDatatypeProperties;
	}

	@Override
	public Set<OWLDataProperty> getIntDatatypePropertiesImpl() {
		return intDatatypeProperties;
	}

	@Override
	public Set<OWLDataProperty> getStringDatatypePropertiesImpl() {
		return stringDatatypeProperties;
	}	
	
	@Override
	protected SortedSet<OWLClassExpression> getSuperClassesImpl(OWLClassExpression concept) throws ReasoningMethodUnsupportedException {
		return rc.getSuperClassesImpl(concept);
	}
	
	@Override
	protected SortedSet<OWLClassExpression> getSubClassesImpl(OWLClassExpression concept) throws ReasoningMethodUnsupportedException {
		return rc.getSubClassesImpl(concept);
	}

	@Override
	protected SortedSet<OWLObjectProperty> getSuperPropertiesImpl(OWLObjectProperty role) throws ReasoningMethodUnsupportedException {
		return rc.getSuperPropertiesImpl(role);
	}	

	@Override
	protected SortedSet<OWLObjectProperty> getSubPropertiesImpl(OWLObjectProperty role) throws ReasoningMethodUnsupportedException {
		return rc.getSubPropertiesImpl(role);
	}
	
	@Override
	protected SortedSet<OWLDataProperty> getSuperPropertiesImpl(OWLDataProperty role) throws ReasoningMethodUnsupportedException {
		return rc.getSuperPropertiesImpl(role);
	}	

	@Override
	protected SortedSet<OWLDataProperty> getSubPropertiesImpl(OWLDataProperty role) throws ReasoningMethodUnsupportedException {
		return rc.getSubPropertiesImpl(role);
	}	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.Reasoner#getIndividuals()
	 */
	@Override
	public SortedSet<OWLIndividual> getIndividuals() {
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
//	public void prepareRoleHierarchyImpl(Set<OWLObjectProperty> allowedRoles) {
//		rc.prepareRoleHierarchy(allowedRoles);
//	}

//	@Override
//	public ObjectPropertyHierarchy getRoleHierarchy() {
//		return rc.getRoleHierarchy();
//	}

//	@Override
//	public void prepareDatatypePropertyHierarchyImpl(Set<OWLDataProperty> allowedRoles) {
//		rc.prepareDatatypePropertyHierarchyImpl(allowedRoles);
//	}

//	@Override
//	public DatatypePropertyHierarchy getDatatypePropertyHierarchy() {
//		return rc.getDatatypePropertyHierarchy();
//	}

	@Override
	public boolean isSuperClassOfImpl(OWLClassExpression superConcept, OWLClassExpression subConcept) {
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
	public OWLClassExpression getDomainImpl(OWLObjectProperty objectProperty) {
		return rc.getDomain(objectProperty);
	}

	@Override
	public OWLClassExpression getDomainImpl(OWLDataProperty datatypeProperty) {
		return rc.getDomain(datatypeProperty);
	}

	@Override
	public OWLClassExpression getRangeImpl(OWLObjectProperty objectProperty) {
		return rc.getRange(objectProperty);
	}
	
	@Override
	public OWLDataRange getRangeImpl(OWLDataProperty datatypeProperty) {
		return rc.getRange(datatypeProperty);
	}

	@Override
	public Map<OWLIndividual, SortedSet<OWLIndividual>> getPropertyMembersImpl(OWLObjectProperty atomicRole) {
		return opPos.get(atomicRole);
	}

	@Override
	public final SortedSet<OWLIndividual> getTrueDatatypeMembersImpl(OWLDataProperty datatypeProperty) {
		return bdPos.get(datatypeProperty);
	}
	
	@Override
	public final SortedSet<OWLIndividual> getFalseDatatypeMembersImpl(OWLDataProperty datatypeProperty) {
		return bdNeg.get(datatypeProperty);
	}
	
	@Override
	public Map<OWLIndividual, SortedSet<Integer>> getIntDatatypeMembersImpl(OWLDataProperty datatypeProperty) {
		return id.get(datatypeProperty);
	}		
	
	@Override
	public Map<OWLIndividual, SortedSet<Double>> getDoubleDatatypeMembersImpl(OWLDataProperty datatypeProperty) {
		return dd.get(datatypeProperty);
	}	
	
	@Override
	public Map<OWLIndividual, SortedSet<OWLLiteral>> getDatatypeMembersImpl(OWLDataProperty datatypeProperty) {
		return dpPos.get(datatypeProperty);
//		return rc.getDatatypeMembersImpl(OWLDataProperty);
	}		
	
	@Override
	public Set<OWLIndividual> getRelatedIndividualsImpl(OWLIndividual individual, OWLObjectProperty objectProperty) throws ReasoningMethodUnsupportedException {
		return rc.getRelatedIndividuals(individual, objectProperty);
	}
	
	@Override
	protected Map<OWLObjectProperty,Set<OWLIndividual>> getObjectPropertyRelationshipsImpl(OWLIndividual individual) {
		return rc.getObjectPropertyRelationships(individual);
	}	
	
	@Override
	public Set<OWLLiteral> getRelatedValuesImpl(OWLIndividual individual, OWLDataProperty datatypeProperty) throws ReasoningMethodUnsupportedException {
		return rc.getRelatedValues(individual, datatypeProperty);
	}	
	
	@Override
	public boolean isSatisfiableImpl() {
		return rc.isSatisfiable();
	}	
	
	@Override
	public Set<OWLLiteral> getLabelImpl(OWLEntity entity) throws ReasoningMethodUnsupportedException {
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
	protected Set<OWLClass> getTypesImpl(OWLIndividual individual) {
		return rc.getTypesImpl(individual);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.BaseReasoner#remainsSatisfiable(org.dllearner.core.owl.Axiom)
	 */
	@Override
	public boolean remainsSatisfiableImpl(OWLAxiom axiom) {
		return rc.remainsSatisfiableImpl(axiom);
	}

	@Override
	protected Set<OWLClassExpression> getAssertedDefinitionsImpl(OWLClass nc) {
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
	 * @param useCaching the useCaching to set
	 */
	public void setUseMaterializationCaching(boolean useCaching) {
		this.useCaching = useCaching;
	}
	
	/**
	 * @param handlePunning the handlePunning to set
	 */
	public void setHandlePunning(boolean handlePunning) {
		this.handlePunning = handlePunning;
	}

}
