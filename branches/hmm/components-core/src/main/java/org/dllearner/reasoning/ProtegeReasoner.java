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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.ReasoningMethodUnsupportedException;
import org.dllearner.core.options.BooleanConfigOption;
import org.dllearner.core.options.ConfigOption;
import org.dllearner.core.owl.Axiom;
import org.dllearner.core.owl.BooleanValueRestriction;
import org.dllearner.core.owl.Constant;
import org.dllearner.core.owl.DataRange;
import org.dllearner.core.owl.Datatype;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.DatatypeSomeRestriction;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.DoubleMaxValue;
import org.dllearner.core.owl.DoubleMinValue;
import org.dllearner.core.owl.Entity;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.KB;
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
import org.dllearner.core.owl.TypedConstant;
import org.dllearner.core.owl.Union;
import org.dllearner.core.owl.UntypedConstant;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.kb.OWLFile;
import org.dllearner.kb.OWLOntologyKnowledgeSource;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.owl.ConceptComparator;
import org.dllearner.utilities.owl.ConceptTransformation;
import org.dllearner.utilities.owl.DLLearnerDescriptionConvertVisitor;
import org.dllearner.utilities.owl.OWLAPIAxiomConvertVisitor;
import org.dllearner.utilities.owl.OWLAPIConverter;
import org.dllearner.utilities.owl.OWLAPIDescriptionConvertVisitor;
import org.dllearner.utilities.owl.RoleComparator;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLNamedObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.ReasonerProgressMonitor;
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;

public class ProtegeReasoner extends AbstractReasonerComponent {
	
	private OWLOntologyManager manager;
	private OWLOntology ontology;
	private OWLDataFactory factory;
	private OWLReasoner reasoner;
	
	private ReasonerProgressMonitor progressMonitor;
	
	private Set<OWLOntology> loadedOntologies;
	
	private ConceptComparator conceptComparator = new ConceptComparator();
	private RoleComparator roleComparator = new RoleComparator();
	
	Set<NamedClass> atomicConcepts = new TreeSet<NamedClass>(conceptComparator);
	Set<ObjectProperty> atomicRoles = new TreeSet<ObjectProperty>(roleComparator);
	SortedSet<DatatypeProperty> datatypeProperties = new TreeSet<DatatypeProperty>();
	SortedSet<DatatypeProperty> booleanDatatypeProperties = new TreeSet<DatatypeProperty>();
	SortedSet<DatatypeProperty> doubleDatatypeProperties = new TreeSet<DatatypeProperty>();
	SortedSet<DatatypeProperty> intDatatypeProperties = new TreeSet<DatatypeProperty>();
	SortedSet<DatatypeProperty> stringDatatypeProperties = new TreeSet<DatatypeProperty>();
	TreeSet<Individual> individuals = new TreeSet<Individual>();	
	
	
	//CWA
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
	
	
	// namespaces
	private Map<String, String> prefixes = new TreeMap<String,String>();
	private String baseURI;
	
	// references to OWL API ontologies
	private List<OWLOntology> owlAPIOntologies = new LinkedList<OWLOntology>();
	private boolean defaultNegation = true;

	public ProtegeReasoner(Set<KnowledgeSource> sources) {
		super(sources);
	}
	
	public ProtegeReasoner(Set<KnowledgeSource> sources, OWLReasoner reasoner) {
		this(sources);
		this.reasoner = reasoner;
	}


	public boolean isConsistent(){
		return reasoner.isConsistent();
	}

	@Override
	public ReasonerType getReasonerType() {
		return ReasonerType.PROTEGE;
	}
	
	public void setProgressMonitor(ReasonerProgressMonitor progressMonitor){
		this.progressMonitor = progressMonitor;
	}

	@Override
	public void releaseKB() {
		reasoner.dispose();
	}
	
	/**
	 * @return The options of this component.
	 */
	public static Collection<ConfigOption<?>> createConfigOptions() {
		Collection<ConfigOption<?>> options = new LinkedList<ConfigOption<?>>();
		
		options.add(new BooleanConfigOption("defaultNegation", "Whether to use default negation, i.e. an instance not being in a class means that it is in the negation of the class.", true, false, true));
		
		return options;
	}
	
	public OWLOntologyManager getOWLOntologyManager(){
		return manager;
	}
	
	public void setOWLReasoner(OWLReasoner owlReasoner){
		this.reasoner = owlReasoner;
	}

	@Override
	public void init() throws ComponentInitException {
		// reset variables (otherwise subsequent initialisation with
		// different knowledge sources will merge both)
		atomicConcepts = new TreeSet<NamedClass>(conceptComparator);
		atomicRoles = new TreeSet<ObjectProperty>(roleComparator);
		datatypeProperties = new TreeSet<DatatypeProperty>();
		booleanDatatypeProperties = new TreeSet<DatatypeProperty>();
		doubleDatatypeProperties = new TreeSet<DatatypeProperty>();
		intDatatypeProperties = new TreeSet<DatatypeProperty>();
		individuals = new TreeSet<Individual>();	
				
		// create OWL API ontology manager
		manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		
		
		//////////////////////////////////////////
		//
		//
		//
		Comparator<OWLNamedObject> namedObjectComparator = new Comparator<OWLNamedObject>() {
			public int compare(OWLNamedObject o1, OWLNamedObject o2) {
				return o1.getIRI().compareTo(o2.getIRI());
			}
		};
		Set<OWLClass> classes = new TreeSet<OWLClass>(namedObjectComparator);
		Set<OWLObjectProperty> owlObjectProperties = new TreeSet<OWLObjectProperty>(
				namedObjectComparator);
		Set<OWLDataProperty> owlDatatypeProperties = new TreeSet<OWLDataProperty>(
				namedObjectComparator);
		Set<OWLNamedIndividual> owlIndividuals = new TreeSet<OWLNamedIndividual>(
				namedObjectComparator);
		loadedOntologies = new HashSet<OWLOntology>();
		Set<OWLOntology> allImports = new HashSet<OWLOntology>();
		prefixes = new TreeMap<String, String>();

		for (KnowledgeSource source : sources) {

            if (source instanceof OWLOntologyKnowledgeSource) {
                ontology = ((OWLOntologyKnowledgeSource) source).createOWLOntology(manager);
                owlAPIOntologies.add(ontology);
            }

			if (source instanceof OWLFile
					|| source instanceof SparqlKnowledgeSource
					|| source instanceof OWLAPIOntology) {

                classes.addAll(ontology.getClassesInSignature(true));
				owlObjectProperties.addAll(ontology.getObjectPropertiesInSignature(true));
				owlDatatypeProperties.addAll(ontology.getDataPropertiesInSignature(true));
				owlIndividuals.addAll(ontology.getIndividualsInSignature(true));

					// if several knowledge sources are included, then we can
					// only
					// guarantee that the base URI is from one of those sources
					// (there
					// can't be more than one); but we will take care that all
					// prefixes are
					// correctly imported
					OWLOntologyFormat format = manager.getOntologyFormat(ontology);
					if (format instanceof PrefixOWLOntologyFormat) {
						prefixes.putAll(((PrefixOWLOntologyFormat) format).getPrefixName2PrefixMap());
						baseURI = ((PrefixOWLOntologyFormat) format).getDefaultPrefix();
						prefixes.remove("");
					}
					
					// read in primitives
					for(OWLClass owlClass : classes)
						atomicConcepts.add(new NamedClass(owlClass.toStringID()));
					for(OWLObjectProperty owlProperty : owlObjectProperties)
						atomicRoles.add(new ObjectProperty(owlProperty.toStringID()));
					for(OWLDataProperty owlProperty : owlDatatypeProperties) {
						DatatypeProperty dtp = new DatatypeProperty(owlProperty.toStringID());
						Set<OWLDataRange> ranges = owlProperty.getRanges(allImports);
						for(OWLDataRange range : ranges){
							if(range.isDatatype()) {
								if(range.asOWLDatatype().isBoolean())
									booleanDatatypeProperties.add(dtp);
								else if(range.asOWLDatatype().isDouble())
									doubleDatatypeProperties.add(dtp);
								else if(range.asOWLDatatype().isInteger())
									intDatatypeProperties.add(dtp);
								else if(range.asOWLDatatype().isString())
									stringDatatypeProperties.add(dtp);
							}
						}
						datatypeProperties.add(dtp);
					}
					for(OWLNamedIndividual owlIndividual : owlIndividuals) {
						individuals.add(new Individual(owlIndividual.toStringID()));
					}		

//				} catch (OWLOntologyCreationException e) {
//					e.printStackTrace();
//				} catch (URISyntaxException e) {
//					e.printStackTrace();
//				}
				// all other sources are converted to KB and then to an
				// OWL API ontology
			} else {
				KB kb = ((AbstractKnowledgeSource)source).toKB();
				// System.out.println(kb.toString(null,null));

				IRI ontologyIRI = IRI.create("http://example.com");
				ontology = null;
				try {
					ontology = manager.createOntology(ontologyIRI);
				} catch (OWLOntologyCreationException e) {
					e.printStackTrace();
				}
				OWLAPIAxiomConvertVisitor
						.fillOWLOntology(manager, ontology, kb);
				owlAPIOntologies.add(ontology);
				allImports.add(ontology);
				atomicConcepts.addAll(kb.findAllAtomicConcepts());
				atomicRoles.addAll(kb.findAllAtomicRoles());
				individuals.addAll(kb.findAllIndividuals());
				// TODO: add method to find datatypes
			}
		}
		dematerialise();
	}
	
	private void dematerialise(){
		long dematStartTime = System.currentTimeMillis();
		logger.debug("dematerialising concepts");
		progressMonitor.reasonerTaskStarted("Preparing DL-Learner ...");
		int size = atomicConcepts.size() + atomicRoles.size() 
		+ booleanDatatypeProperties.size() + intDatatypeProperties.size()
		+ doubleDatatypeProperties.size();
		int cnt = 1;
		
		
		for (NamedClass atomicConcept : atomicConcepts) {
			SortedSet<Individual> pos = getIndividualsWithPellet(atomicConcept);
			classInstancesPos.put(atomicConcept, (TreeSet<Individual>) pos);
			if (defaultNegation) {
				classInstancesNeg.put(atomicConcept, (TreeSet<Individual>) Helper.difference(individuals, pos));
			} else {
				Negation negatedAtomicConcept = new Negation(atomicConcept);
				classInstancesNeg.put(atomicConcept, (TreeSet<Individual>) getIndividuals(negatedAtomicConcept));
			}
			progressMonitor.reasonerTaskProgressChanged(cnt++, size);
		}

		logger.debug("dematerialising object properties");

		for (ObjectProperty atomicRole : atomicRoles) {
			opPos.put(atomicRole, getPropertyMembers(atomicRole));
			progressMonitor.reasonerTaskProgressChanged(cnt++, size);
		}

		logger.debug("dematerialising datatype properties");

		for (DatatypeProperty dp : booleanDatatypeProperties) {
			bdPos.put(dp, (TreeSet<Individual>) getTrueDatatypeMembers(dp));
			bdNeg.put(dp, (TreeSet<Individual>) getFalseDatatypeMembers(dp));
			progressMonitor.reasonerTaskProgressChanged(cnt++, size);
		}

		for (DatatypeProperty dp : intDatatypeProperties) {
			id.put(dp, getIntDatatypeMembers(dp));
			progressMonitor.reasonerTaskProgressChanged(cnt++, size);
		}

		for (DatatypeProperty dp : doubleDatatypeProperties) {
			dd.put(dp, getDoubleDatatypeMembers(dp));
			progressMonitor.reasonerTaskProgressChanged(cnt++, size);
		}

		long dematDuration = System.currentTimeMillis() - dematStartTime;
		logger.debug("TBox dematerialised in " + dematDuration + " ms");
		progressMonitor.reasonerTaskStopped();
	}
	

	@Override
	public String getBaseURI() {
		return baseURI;
	}

	@Override
	public SortedSet<Individual> getIndividuals() {
		return individuals;
	}

	@Override
	public Set<NamedClass> getNamedClasses() {
		return Collections.unmodifiableSet(atomicConcepts);
	}

	@Override
	public Set<ObjectProperty> getObjectProperties() {
		return Collections.unmodifiableSet(atomicRoles);
	}

	@Override
	public Map<String, String> getPrefixes() {
		return prefixes;
	}
	
	public Set<Description> getComplementClasses(Description desc){
		OWLClassExpression owlDesc = OWLAPIDescriptionConvertVisitor.getOWLClassExpression(desc);
		Set<Description> complements = new HashSet<Description>();
		for(OWLClass comp : reasoner.getDisjointClasses(owlDesc).getFlattened()){
			complements.add(OWLAPIConverter.convertClass(comp));
		}
		return complements;
		
	}

	@Override
	public SortedSet<DatatypeProperty> getDatatypePropertiesImpl() {
		return datatypeProperties;
	}
	
	@Override
	public boolean isSuperClassOfImpl(Description superConcept, Description subConcept) {
		return reasoner.isEntailed(factory.getOWLSubClassOfAxiom(OWLAPIDescriptionConvertVisitor.getOWLClassExpression(subConcept),
				OWLAPIDescriptionConvertVisitor.getOWLClassExpression(superConcept)));			
	}
	
	@Override
	protected boolean isEquivalentClassImpl(Description class1, Description class2) {
		return reasoner.isEntailed(factory.getOWLEquivalentClassesAxiom(OWLAPIDescriptionConvertVisitor.getOWLClassExpression(class1),
				OWLAPIDescriptionConvertVisitor.getOWLClassExpression(class2)));		
	}

	@Override
	protected TreeSet<Description> getSuperClassesImpl(Description concept) {
		NodeSet<OWLClass> classes = null;
		
		classes = reasoner.getSuperClasses(OWLAPIDescriptionConvertVisitor.getOWLClassExpression(concept), true);
		
		return getFirstClasses(classes);
	}
	
	@Override
	protected TreeSet<Description> getSubClassesImpl(Description concept) {
		NodeSet<OWLClass> classes = null;
		
		classes = reasoner.getSubClasses(OWLAPIDescriptionConvertVisitor.getOWLClassExpression(concept), true);
		
		return getFirstClasses(classes);
	}
	
	@Override
	protected TreeSet<ObjectProperty> getSuperPropertiesImpl(ObjectProperty role) {
		NodeSet<OWLObjectPropertyExpression> properties = null;
		
		properties = reasoner.getSuperObjectProperties(OWLAPIConverter.getOWLAPIObjectProperty(role), true);
		 	
		return getFirstObjectProperties(properties);
	}
	
	@Override
	protected TreeSet<ObjectProperty> getSubPropertiesImpl(ObjectProperty role) {
		NodeSet<OWLObjectPropertyExpression> properties = null;
		
		properties = reasoner.getSubObjectProperties(OWLAPIConverter.getOWLAPIObjectProperty(role), true);
			
		return getFirstObjectProperties(properties);		
	}
	
	@Override
	protected TreeSet<DatatypeProperty> getSuperPropertiesImpl(DatatypeProperty role) {
		NodeSet<OWLDataProperty> properties = null;
		
		properties = reasoner.getSuperDataProperties(OWLAPIConverter.getOWLAPIDataProperty(role), true);
		 
		return getFirstDatatypeProperties(properties);
	}
	
	@Override
	protected TreeSet<DatatypeProperty> getSubPropertiesImpl(DatatypeProperty role) {
		NodeSet<OWLDataProperty> properties = null;
		
		properties = reasoner.getSubDataProperties(OWLAPIConverter.getOWLAPIDataProperty(role), true);
			
		return getFirstDatatypeProperties(properties);		
	}	
	
	@Override
	public boolean hasTypeImpl(Description description, Individual individual) throws ReasoningMethodUnsupportedException {
//		OWLClassExpression d = OWLAPIDescriptionConvertVisitor.getOWLClassExpression(concept);
//		OWLIndividual i = factory.getOWLIndividual(URI.create(individual.getName()));
//		try {
//			return reasoner.hasType(i,d,false);
//		} catch (OWLReasonerException e) {
//			e.printStackTrace();
//			throw new Error("Instance check error in OWL API.");
//		}
		if (description instanceof NamedClass) {
			return classInstancesPos.get((NamedClass) description).contains(individual);
		} else if (description instanceof Negation) {
			Description child = description.getChild(0);
			if (child instanceof NamedClass) {
				return classInstancesNeg.get((NamedClass) child).contains(individual);
			} else {
				// default negation
				if(defaultNegation ) {
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
		}

		throw new ReasoningMethodUnsupportedException("Instance check for description "
				+ description + " unsupported.");
	
	}
	
	private SortedSet<Individual> getIndividualsWithPellet(Description concept){

		OWLClassExpression d = OWLAPIDescriptionConvertVisitor.getOWLClassExpression(concept);
		Set<OWLNamedIndividual> individuals = reasoner.getInstances(d, false).getFlattened();
		SortedSet<Individual> inds = new TreeSet<Individual>();
		for(OWLNamedIndividual ind : individuals)
			if(ind != null){
				inds.add(new Individual(ind.toStringID()));
			}
		return inds;
	}
	
	@Override
	public SortedSet<Individual> getIndividualsImpl(Description concept)  throws ReasoningMethodUnsupportedException{
//
//		OWLClassExpression d = OWLAPIDescriptionConvertVisitor.getOWLClassExpression(concept);
//		Set<OWLIndividual> individuals = null;
//		
//		individuals = reasoner.getIndividuals(d, false);
//		
//		SortedSet<Individual> inds = new TreeSet<Individual>();
//		for(OWLIndividual ind : individuals)
//			inds.add(new Individual(ind.getURI().toString()));
//		return inds;
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
			if(((NamedClass) description).getName().equals("http://www.w3.org/2002/07/owl#Nothing")){
				return new TreeSet<Individual>();
			}
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
	
	@Override
	public Set<NamedClass> getTypesImpl(Individual individual) {
		Set<Node<OWLClass>> result = null;
		
		result = reasoner.getTypes(factory.getOWLNamedIndividual(IRI.create(individual.getName())),false).getNodes();
		
		return getFirstClassesNoTopBottom(result);
	}
	
	@Override
	public boolean isSatisfiableImpl() {
		return reasoner.isSatisfiable(factory.getOWLThing());
	}
	
	@Override
	public Description getDomainImpl(ObjectProperty objectProperty) {
		OWLObjectProperty prop = OWLAPIConverter.getOWLAPIObjectProperty(objectProperty);

		// Pellet returns a set of sets of named class, which are more
		// general than the actual domain/range
		NodeSet<OWLClass> set = reasoner.getObjectPropertyDomains(prop, false);
		return getDescriptionFromReturnedDomain(set);

	}
	
	@Override
	public Description getDomainImpl(DatatypeProperty datatypeProperty) {
		OWLDataProperty prop = OWLAPIConverter
				.getOWLAPIDataProperty(datatypeProperty);

		NodeSet<OWLClass> set = reasoner.getDataPropertyDomains(prop, true);
		return getDescriptionFromReturnedDomain(set);

	}
	
	@Override
	public Description getRangeImpl(ObjectProperty objectProperty) {
		OWLObjectProperty prop = OWLAPIConverter
				.getOWLAPIObjectProperty(objectProperty);

		NodeSet<OWLClass> set = reasoner.getObjectPropertyRanges(prop, true);
		if (set.isEmpty())
			return new Thing();
		return getDescriptionFromReturnedDomain(set);

	}
	
	private Description getDescriptionFromReturnedDomain(NodeSet<OWLClass> set) {
		if(set.isEmpty())
			return new Thing();
		
		Set<OWLClassExpression> union = new HashSet<OWLClassExpression>();
		Set<OWLClassExpression> domains = new HashSet<OWLClassExpression>();
		
		for(Node<OWLClass> descs : set){
			for(OWLClassExpression desc : descs){
				union.add(desc);
			}
		}
		for(OWLClassExpression desc : union){
			boolean isSuperClass = false;
			for(Description d : getClassHierarchy().getSubClasses(OWLAPIConverter.convertClass(desc.asOWLClass()))){
				if(union.contains(OWLAPIConverter.getOWLAPIDescription(d))){
					isSuperClass = true;
					break;
				}
			}
			if(!isSuperClass){
				domains.add(desc);
			}
		}
		
		OWLClass oc = (OWLClass) domains.iterator().next();
		if(oc.isOWLThing()) {
			return new Thing();
		} else {
			return new NamedClass(oc.toStringID());
		}					
	}
	
	@Override
	public Map<Individual, SortedSet<Individual>> getPropertyMembersImpl(ObjectProperty atomicRole) {
		OWLObjectProperty prop = OWLAPIConverter.getOWLAPIObjectProperty(atomicRole);
		Map<Individual, SortedSet<Individual>> map = new TreeMap<Individual, SortedSet<Individual>>();
		for(Individual i : individuals) {
			OWLNamedIndividual ind = factory.getOWLNamedIndividual(IRI.create(i.getName()));
			
			// get all related individuals via OWL API
			Set<OWLNamedIndividual> inds = reasoner.getObjectPropertyValues(ind, prop).getFlattened();
			
			
			// convert data back to DL-Learner structures
			SortedSet<Individual> is = new TreeSet<Individual>();
			for(OWLNamedIndividual oi : inds)
				is.add(new Individual(oi.toStringID()));
			map.put(i, is);
		}
		return map;
	}
	
	@Override
	protected Map<ObjectProperty,Set<Individual>> getObjectPropertyRelationshipsImpl(Individual individual) {
		OWLNamedIndividual ind = factory.getOWLNamedIndividual(IRI.create(individual.getName()));
		Map<OWLObjectPropertyExpression, Set<OWLNamedIndividual>> mapAPI = new HashMap<OWLObjectPropertyExpression, Set<OWLNamedIndividual>>();
		
//		Map<OWLObjectPropertyExpression, Set<OWLIndividual>> mapAPI = ind.getObjectPropertyValues(ontology);
		//no method found in the new reasoner interface, so we have to ask the reasoner for each property in the ontology
		for(OWLObjectProperty prop : ontology.getObjectPropertiesInSignature(true)){
			mapAPI.put(prop, reasoner.getObjectPropertyValues(ind, prop).getFlattened());
		}
		
		Map<ObjectProperty,Set<Individual>> map = new TreeMap<ObjectProperty, Set<Individual>>();
		for(Entry<OWLObjectPropertyExpression,Set<OWLNamedIndividual>> entry : mapAPI.entrySet()) {
			ObjectProperty prop = OWLAPIConverter.convertObjectProperty(entry.getKey().asOWLObjectProperty());
			Set<Individual> inds = OWLAPIConverter.convertIndividuals(entry.getValue());
			map.put(prop, inds);
		}
		return map;
	}
	
	@Override
	public Set<Individual> getRelatedIndividualsImpl(Individual individual, ObjectProperty objectProperty) {
		OWLNamedIndividual ind = factory.getOWLNamedIndividual(IRI.create(individual.getName()));
		OWLObjectProperty prop = OWLAPIConverter.getOWLAPIObjectProperty(objectProperty);
		Set<OWLNamedIndividual> inds = null;
		
		inds = reasoner.getObjectPropertyValues(ind, prop).getFlattened();
		
		// convert data back to DL-Learner structures
		SortedSet<Individual> is = new TreeSet<Individual>();
		for(OWLNamedIndividual oi : inds) {
			is.add(new Individual(oi.toStringID()));
		}
		return is;
	}
	
	@Override
	public Set<Constant> getRelatedValuesImpl(Individual individual, DatatypeProperty datatypeProperty) {
		OWLNamedIndividual ind = factory.getOWLNamedIndividual(IRI.create(individual.getName()));
		OWLDataProperty prop = OWLAPIConverter.getOWLAPIDataProperty(datatypeProperty);
		Set<OWLLiteral> constants = null;
	
		constants = reasoner.getDataPropertyValues(ind, prop);
		
		return OWLAPIConverter.convertConstants(constants);	
	}	
	
	public Map<Individual, SortedSet<Double>> getDoubleValues(
			DatatypeProperty datatypeProperty) {
		OWLDataProperty prop = OWLAPIConverter
				.getOWLAPIDataProperty(datatypeProperty);
		Map<Individual, SortedSet<Double>> map = new TreeMap<Individual, SortedSet<Double>>();
		for (Individual i : individuals) {
			OWLNamedIndividual ind = factory.getOWLNamedIndividual(IRI
					.create(i.getName()));

			// get all related individuals via OWL API
			Set<OWLLiteral> inds = null;

			inds = reasoner.getDataPropertyValues(ind, prop);

			// convert data back to DL-Learner structures
			SortedSet<Double> is = new TreeSet<Double>();
			for (OWLLiteral oi : inds) {
				Double d = Double.parseDouble(oi.getLiteral());
				is.add(d);
			}
			map.put(i, is);
		}
		return map;
	}	
	
	@Override
	public Map<Individual, SortedSet<Constant>> getDatatypeMembersImpl(DatatypeProperty datatypeProperty) {
		OWLDataProperty prop = OWLAPIConverter.getOWLAPIDataProperty(datatypeProperty);
		Map<Individual, SortedSet<Constant>> map = new TreeMap<Individual, SortedSet<Constant>>();
		for(Individual i : individuals) {
			OWLNamedIndividual ind = factory.getOWLNamedIndividual(IRI.create(i.getName()));
			
			// get all related values via OWL API
			Set<OWLLiteral> constants = null;
			
			constants = reasoner.getDataPropertyValues(ind, prop);
			
			
			// convert data back to DL-Learner structures
			SortedSet<Constant> is = new TreeSet<Constant>();
			for(OWLLiteral literal : constants) {
				// for typed constants we have to figure out the correct
				// data type and value
				if(!literal.isRDFPlainLiteral()) {
					Datatype dt = OWLAPIConverter.convertDatatype(literal.getDatatype());
					is.add(new TypedConstant(literal.getLiteral(), dt));
				// for untyped constants we have to figure out the value
				// and language tag (if any)
				} else {
					if(literal.hasLang())
						is.add(new UntypedConstant(literal.getLiteral(), literal.getLang()));
					else
						is.add(new UntypedConstant(literal.getLiteral()));
				}
			}	
			// only add individuals using the datatype property
			if(is.size()>0)
				map.put(i, is);
		}
		return map;
	}
	
	// OWL API returns a set of nodes of classes, where each node
	// consists of equivalent classes; this method picks one class
	// from each node to flatten the set of nodes
	private TreeSet<Description> getFirstClasses(NodeSet<OWLClass> nodeSet) {
		TreeSet<Description> concepts = new TreeSet<Description>(conceptComparator);
		for(Node<OWLClass> node : nodeSet) {
			// take one element from the set and ignore the rest
			// (TODO: we need to make sure we always ignore the same concepts)
			OWLClass concept = node.getRepresentativeElement();
			if(concept.isOWLThing()) {
				concepts.add(new Thing());
			} else if(concept.isOWLNothing()) {
				concepts.add(new Nothing());
			} else {
				concepts.add(new NamedClass(concept.toStringID()));
			}
		}
		return concepts;		
	}
	
	private Set<NamedClass> getFirstClassesNoTopBottom(Set<Node<OWLClass>> setOfSets) {
		Set<NamedClass> concepts = new HashSet<NamedClass>();
		for(Node<OWLClass> innerSet : setOfSets) {
			// take one element from the set and ignore the rest
			// (TODO: we need to make sure we always ignore the same concepts)
			OWLClass concept = innerSet.iterator().next();
			if(!concept.isOWLThing() && !concept.isOWLNothing())
				concepts.add(new NamedClass(concept.toStringID()));
		}
		return concepts;			
	}
	
	private TreeSet<ObjectProperty> getFirstObjectProperties(NodeSet<OWLObjectPropertyExpression> nodeSet) {
		TreeSet<ObjectProperty> roles = new TreeSet<ObjectProperty>(roleComparator);
		for(Node<OWLObjectPropertyExpression> node : nodeSet) {
			if(node.isBottomNode() || node.isTopNode()){
				continue;
			}
			// take one element from the set and ignore the rest
			// (TODO: we need to make sure we always ignore the same concepts)
			
			for(OWLObjectPropertyExpression property : node.getEntities()){
				if(!property.isAnonymous()){
					roles.add(new ObjectProperty(property.asOWLObjectProperty().toStringID()));
					break;
				}
			}
			//TODO: We get a problem when the returned representative element is anonymous, so we now use the code above
//			OWLObjectPropertyExpression property = node.getRepresentativeElement();
//			roles.add(new ObjectProperty(property.asOWLObjectProperty().toStringID()));
		}
		roles.remove(new ObjectProperty(factory.getOWLTopObjectProperty().toStringID()));
		roles.remove(new ObjectProperty(factory.getOWLBottomObjectProperty().toStringID()));
		return roles;		
	}		
	
	private TreeSet<DatatypeProperty> getFirstDatatypeProperties(NodeSet<OWLDataProperty> setOfSets) {
		TreeSet<DatatypeProperty> roles = new TreeSet<DatatypeProperty>(roleComparator);
		for(Node<OWLDataProperty> innerSet : setOfSets) {
			OWLDataProperty property = innerSet.iterator().next();
			roles.add(new DatatypeProperty(property.toStringID()));
		}
		return roles;		
	}		
	
	@SuppressWarnings({"unused"})
	private Set<Description> owlClassesToAtomicConcepts(Set<OWLClass> owlClasses) {
		Set<Description> concepts = new HashSet<Description>();
		for(OWLClass owlClass : owlClasses)
			concepts.add(OWLAPIConverter.convertClass(owlClass));
		return concepts;
	}
	
	public static void exportKBToOWL(File owlOutputFile, KB kb, IRI ontologyIRI) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		//URI ontologyURI = URI.create("http://example.com");
		IRI physicalIRI = IRI.create(owlOutputFile.toURI());
		SimpleIRIMapper mapper = new SimpleIRIMapper(ontologyIRI, physicalIRI);
		manager.addIRIMapper(mapper);
		OWLOntology ontology;
		try {
			ontology = manager.createOntology(ontologyIRI);
			// OWLAPIReasoner.fillOWLAPIOntology(manager, ontology, kb);
			OWLAPIAxiomConvertVisitor.fillOWLOntology(manager, ontology, kb);
			manager.saveOntology(ontology);
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownOWLOntologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OWLOntologyStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	/**
	 * @return the booleanDatatypeProperties
	 */
	@Override
	public SortedSet<DatatypeProperty> getBooleanDatatypePropertiesImpl() {
		return booleanDatatypeProperties;
	}

	/**
	 * @return the doubleDatatypeProperties
	 */
	@Override
	public SortedSet<DatatypeProperty> getDoubleDatatypePropertiesImpl() {
		return doubleDatatypeProperties;
	}

	/**
	 * @return the intDatatypeProperties
	 */
	@Override
	public SortedSet<DatatypeProperty> getIntDatatypePropertiesImpl() {
		return intDatatypeProperties;
	}

	/**
	 * @return the intDatatypeProperties
	 */
	@Override
	public SortedSet<DatatypeProperty> getStringDatatypePropertiesImpl() {
		return stringDatatypeProperties;
	}	

	public OWLOntology getOWLAPIOntologies() {
		return owlAPIOntologies.get(0);
	}
	
	/*public void setReasonerType(String type){
		configurator.setReasonerType(type);
	}*/

//	@Override
//	public boolean hasDatatypeSupport() {
//		return true;
//	}
	
	@Override
	public Set<NamedClass> getInconsistentClassesImpl() {
		Set<NamedClass> concepts = new HashSet<NamedClass>();

		for (OWLClass concept : reasoner.getUnsatisfiableClasses().getEntities()){
			concepts.add(new NamedClass(concept.toStringID()));
		}

		return concepts;
	}
	
	
	public Set<OWLClass> getInconsistentOWLClasses() {
		return reasoner.getUnsatisfiableClasses().getEntities();
	}

	@Override
	public Set<Constant> getLabelImpl(Entity entity) {
		OWLEntity owlEntity = OWLAPIConverter.getOWLAPIEntity(entity);
		Set<OWLAnnotation> labelAnnotations = owlEntity.getAnnotations(owlAPIOntologies.get(0),
				factory.getRDFSLabel());
		Set<Constant> annotations = new HashSet<Constant>();
		for(OWLAnnotation label : labelAnnotations) {
			OWLLiteral c =  (OWLLiteral)label.getValue();
			annotations.add(OWLAPIConverter.convertConstant(c));
		}
		return annotations;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.BaseReasoner#remainsSatisfiable(org.dllearner.core.owl.Axiom)
	 */
	@Override
	public boolean remainsSatisfiableImpl(Axiom axiom) {
		boolean consistent = true;
		OWLAxiom axiomOWLAPI = OWLAPIAxiomConvertVisitor.convertAxiom(axiom);
		
		try {
			manager.applyChange(new AddAxiom(ontology, axiomOWLAPI));
		} catch (OWLOntologyChangeException e1) {
			e1.printStackTrace();
		}
		
		consistent = reasoner.isConsistent();

		
		try {
			manager.applyChange(new RemoveAxiom(ontology, axiomOWLAPI));
		} catch (OWLOntologyChangeException e) {
			e.printStackTrace();
		}
		
		return consistent;
	}

	public Set<OWLOntology> getLoadedOWLAPIOntologies(){
		return loadedOntologies;
	}
	
	public OWLDataFactory getOWLDataFactory(){
		return factory;
	}
	
	public boolean isSatisfiable(OWLClassExpression d){
		return reasoner.isSatisfiable(d);
	}
	
	/**
	 * Returns asserted class definitions of given class
	 * @param nc the class
	 * @return the asserted class definitions
	 */
	@Override
	protected Set<Description> getAssertedDefinitionsImpl(NamedClass nc){
		OWLClass owlClass = OWLAPIDescriptionConvertVisitor.getOWLClassExpression(nc).asOWLClass();
		Set<OWLClassExpression> owlAPIDescriptions = owlClass.getEquivalentClasses(new HashSet<OWLOntology>(owlAPIOntologies));
		Set<Description> definitions = new HashSet<Description>();
		for(OWLClassExpression owlAPIDescription : owlAPIDescriptions) {
			definitions.add(DLLearnerDescriptionConvertVisitor.getDLLearnerDescription(owlAPIDescription));
		}
		return definitions;
	}
	
	public static String getName() {
		return "Protege internal reasoner";
	}	

}
