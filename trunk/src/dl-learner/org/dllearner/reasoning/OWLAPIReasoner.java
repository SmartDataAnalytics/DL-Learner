/**
 * Copyright (C) 2008, Jens Lehmann
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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.configurators.OWLAPIReasonerConfigurator;
import org.dllearner.core.options.ConfigEntry;
import org.dllearner.core.options.ConfigOption;
import org.dllearner.core.options.InvalidConfigOptionValueException;
import org.dllearner.core.options.StringConfigOption;
import org.dllearner.core.owl.Axiom;
import org.dllearner.core.owl.Constant;
import org.dllearner.core.owl.Datatype;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Entity;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.KB;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Nothing;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.Thing;
import org.dllearner.core.owl.TypedConstant;
import org.dllearner.core.owl.UntypedConstant;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.kb.OWLFile;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.utilities.owl.ConceptComparator;
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
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
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
import org.semanticweb.owlapi.model.OWLTypedLiteral;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;
import org.semanticweb.owlapi.reasoner.FreshEntityPolicy;
import org.semanticweb.owlapi.reasoner.IndividualNodeSetPolicy;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.NullReasonerProgressMonitor;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.ReasonerProgressMonitor;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;

import uk.ac.manchester.cs.factplusplus.owlapiv3.FaCTPlusPlusReasonerFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLStringLiteralImpl;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

/**
 * Mapping to OWL API reasoner interface. The OWL API currently 
 * supports two reasoners: FaCT++ and Pellet. FaCT++ is connected
 * using JNI and native libraries, while Pellet is a pure Java
 * library.
 * 
 * @author Jens Lehmann
 *
 */
public class OWLAPIReasoner extends ReasonerComponent {

//	private static Logger logger = Logger
//	.getLogger(OWLAPIReasoner.class);	
	
	//private String reasonerType = "pellet";
	private OWLAPIReasonerConfigurator configurator;
	@Override
	public OWLAPIReasonerConfigurator getConfigurator(){
		return configurator;
	}
	
	private OWLReasoner reasoner;
	private OWLOntologyManager manager;
	private OWLOntology ontology;
	// the data factory is used to generate OWL API objects
	private OWLDataFactory factory;
	// static factory
//	private static OWLDataFactory staticFactory = OWLManager.createOWLOntologyManager().getOWLDataFactory();
	
	private ConceptComparator conceptComparator = new ConceptComparator();
	private RoleComparator roleComparator = new RoleComparator();
//	private ClassHierarchy subsumptionHierarchy;
//	private ObjectPropertyHierarchy roleHierarchy;	
//	private DatatypePropertyHierarchy datatypePropertyHierarchy;
//	private Set<Description> allowedConceptsInSubsumptionHierarchy;
	
	// primitives
	Set<NamedClass> atomicConcepts = new TreeSet<NamedClass>(conceptComparator);
	Set<ObjectProperty> atomicRoles = new TreeSet<ObjectProperty>(roleComparator);
	SortedSet<DatatypeProperty> datatypeProperties = new TreeSet<DatatypeProperty>();
	SortedSet<DatatypeProperty> booleanDatatypeProperties = new TreeSet<DatatypeProperty>();
	SortedSet<DatatypeProperty> doubleDatatypeProperties = new TreeSet<DatatypeProperty>();
	SortedSet<DatatypeProperty> intDatatypeProperties = new TreeSet<DatatypeProperty>();
	SortedSet<DatatypeProperty> stringDatatypeProperties = new TreeSet<DatatypeProperty>();
	SortedSet<Individual> individuals = new TreeSet<Individual>();	
	
	// namespaces
	private Map<String, String> prefixes = new TreeMap<String,String>();
	private String baseURI;
	
	// references to OWL API ontologies
	private List<OWLOntology> owlAPIOntologies = new LinkedList<OWLOntology>();
	
	public OWLAPIReasoner(Set<KnowledgeSource> sources) {
		super(sources);
		this.configurator = new OWLAPIReasonerConfigurator(this);
	}
	
	public static String getName() {
		return "OWL API reasoner";
	}	
	
	
	public static Collection<ConfigOption<?>> createConfigOptions() {
		Collection<ConfigOption<?>> options = new LinkedList<ConfigOption<?>>();
		StringConfigOption type = new StringConfigOption("reasonerType", "FaCT++ or Pellet, which means \"pellet\" or \"fact\"", "pellet", false, true);
		type.setAllowedValues(new String[] {"fact", "pellet"});
		// closure option? see:
		// http://owlapi.svn.sourceforge.net/viewvc/owlapi/owl1_1/trunk/tutorial/src/main/java/uk/ac/manchester/owl/tutorial/examples/ClosureAxiomsExample.java?view=markup
		options.add(type);
		return options;
	}	
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#applyConfigEntry(org.dllearner.core.config.ConfigEntry)
	 */
	@Override
	public <T> void applyConfigEntry(ConfigEntry<T> entry) throws InvalidConfigOptionValueException {
		
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
		stringDatatypeProperties = new TreeSet<DatatypeProperty>();
		individuals = new TreeSet<Individual>();	
				
		// create OWL API ontology manager
		manager = OWLManager.createOWLOntologyManager();
		
		// it is a bit cumbersome to obtain all classes, because there
		// are no reasoner queries to obtain them => hence we query them
		// for each ontology and add them to a set; a comparator avoids
		// duplicates by checking URIs
		Comparator<OWLNamedObject> namedObjectComparator = new Comparator<OWLNamedObject>() {
			public int compare(OWLNamedObject o1, OWLNamedObject o2) {
				return o1.getIRI().compareTo(o2.getIRI());
			}	
		};		
		Set<OWLClass> classes = new TreeSet<OWLClass>(namedObjectComparator);
		Set<OWLObjectProperty> owlObjectProperties = new TreeSet<OWLObjectProperty>(namedObjectComparator);
		Set<OWLDataProperty> owlDatatypeProperties = new TreeSet<OWLDataProperty>(namedObjectComparator);
		Set<OWLNamedIndividual> owlIndividuals = new TreeSet<OWLNamedIndividual>(namedObjectComparator);
		
		Set<OWLOntology> allImports = new HashSet<OWLOntology>();
		prefixes = new TreeMap<String,String>();
		
		for(KnowledgeSource source : sources) {
			
			if(source instanceof OWLFile || source instanceof SparqlKnowledgeSource || source instanceof OWLAPIOntology) {
				URL url=null;
				if(source instanceof OWLFile){
					 url = ((OWLFile)source).getURL();
				}

				try {
					
					if(source instanceof OWLAPIOntology) {
						ontology = ((OWLAPIOntology)source).getOWLOntolgy();
					} else if (source instanceof SparqlKnowledgeSource) { 
						ontology = ((SparqlKnowledgeSource)source).getOWLAPIOntology();
					} else {
						ontology = manager.loadOntologyFromOntologyDocument(IRI.create(url.toURI()));
					}
					
					owlAPIOntologies.add(ontology);
					// imports includes the ontology itself
					Set<OWLOntology> imports = manager.getImportsClosure(ontology);
					allImports.addAll(imports);
//					System.out.println(imports);
					for(OWLOntology ont : imports) {
						classes.addAll(ont.getClassesInSignature());
						owlObjectProperties.addAll(ont.getObjectPropertiesInSignature());
						owlDatatypeProperties.addAll(ont.getDataPropertiesInSignature());			
						owlIndividuals.addAll(ont.getIndividualsInSignature());
					}
					
					// if several knowledge sources are included, then we can only
					// guarantee that the base URI is from one of those sources (there
					// can't be more than one); but we will take care that all prefixes are
					// correctly imported
					OWLOntologyFormat format = manager.getOntologyFormat(ontology);
					if(format instanceof PrefixOWLOntologyFormat) {
						prefixes.putAll(((PrefixOWLOntologyFormat)format).getPrefixName2PrefixMap());
						baseURI = ((PrefixOWLOntologyFormat) format).getDefaultPrefix();
						prefixes.remove("");						
					}
					
				} catch (OWLOntologyCreationException e) {
					e.printStackTrace();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			// all other sources are converted to KB and then to an
			// OWL API ontology
			} else {
				KB kb = source.toKB();
//				System.out.println(kb.toString(null,null));
				
				IRI ontologyURI = IRI.create("http://example.com");
				ontology = null;
				try {
					ontology = manager.createOntology(ontologyURI);
				} catch (OWLOntologyCreationException e) {
					e.printStackTrace();
				}
				OWLAPIAxiomConvertVisitor.fillOWLOntology(manager, ontology, kb);
				owlAPIOntologies.add(ontology);
				allImports.add(ontology);
				atomicConcepts.addAll(kb.findAllAtomicConcepts());
				atomicRoles.addAll(kb.findAllAtomicRoles());
				individuals.addAll(kb.findAllIndividuals());
				// TODO: add method to find datatypes
			}
		}
		
		//configure reasoner
		ReasonerProgressMonitor progressMonitor = new NullReasonerProgressMonitor();
		FreshEntityPolicy freshEntityPolicy = FreshEntityPolicy.ALLOW;
		long timeOut = Integer.MAX_VALUE;
		IndividualNodeSetPolicy individualNodeSetPolicy = IndividualNodeSetPolicy.BY_NAME;
		OWLReasonerConfiguration conf = new SimpleConfiguration(progressMonitor, freshEntityPolicy, timeOut, individualNodeSetPolicy);
		
		// create actual reasoner
		if(configurator.getReasonerType().equals("fact")) {
			try {
				reasoner = new FaCTPlusPlusReasonerFactory().createNonBufferingReasoner(ontology, conf);
			} catch (Exception e) {
				e.printStackTrace();
			}		
			System.out.println("Using FaCT++.");
		} else {
			// instantiate Pellet reasoner
			reasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner(ontology, conf);
			
			// change log level to WARN for Pellet, because otherwise log
			// output will be very large
			Logger pelletLogger = Logger.getLogger("org.mindswap.pellet");
			pelletLogger.setLevel(Level.WARN);
		}

		/*
		Set<OWLOntology> importsClosure = manager.getImportsClosure(ontology);
		System.out.println("imports closure : " + importsClosure);
        try {
			reasoner.loadOntologies(importsClosure);
		} catch (OWLReasonerException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/		
		
//		System.out.println(classes);
//		System.out.println(properties);
//		System.out.println(individuals);
		
		// compute class hierarchy and types of individuals
		// (done here to speed up later reasoner calls)
		boolean inconsistentOntology = !reasoner.isConsistent();
		
		if(!inconsistentOntology) {
			reasoner.prepareReasoner();
		} else {
			throw new ComponentInitException("Inconsistent ontologies.");
		}
			
		factory = manager.getOWLDataFactory();
		
//		try {
//			if(reasoner.isDefined(factory.getOWLIndividual(URI.create("http://example.com/father#female"))))
//				System.out.println("DEFINED.");
//			else
//				System.out.println("NOT DEFINED.");
//		} catch (OWLReasonerException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		// read in primitives
		for(OWLClass owlClass : classes)
			atomicConcepts.add(new NamedClass(owlClass.toStringID()));
		for(OWLObjectProperty owlProperty : owlObjectProperties)
			atomicRoles.add(new ObjectProperty(owlProperty.toStringID()));
		for(OWLDataProperty owlProperty : owlDatatypeProperties) {
			DatatypeProperty dtp = new DatatypeProperty(owlProperty.toStringID());
			Set<OWLDataRange> ranges = owlProperty.getRanges(allImports);
			Iterator<OWLDataRange> it = ranges.iterator();
			if(it.hasNext()) {
				OWLDataRange range = it.next();
				if(range.isDatatype()) {
					URI uri = ((OWLDatatype)range).getIRI().toURI();
					if(uri.equals(Datatype.BOOLEAN.getURI()))
						booleanDatatypeProperties.add(dtp);
					else if(uri.equals(Datatype.DOUBLE.getURI()))
						doubleDatatypeProperties.add(dtp);
					else if(uri.equals(Datatype.INT.getURI()))
						intDatatypeProperties.add(dtp);		
					else if(uri.equals(Datatype.STRING.getURI()))
						stringDatatypeProperties.add(dtp);	
				}
			} else {
				stringDatatypeProperties.add(dtp);
			}
			datatypeProperties.add(dtp);
		}
		for(OWLNamedIndividual owlIndividual : owlIndividuals) {
			individuals.add(new Individual(owlIndividual.toStringID()));
		}		
		
		// remove top and bottom properties (for backwards compatibility)
//		atomicRoles.remove(new ObjectProperty("http://www.w3.org/2002/07/owl#bottomObjectProperty"));
//		atomicRoles.remove(new ObjectProperty("http://www.w3.org/2002/07/owl#topObjectProperty"));
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Reasoner#getAtomicConcepts()
	 */
	public Set<NamedClass> getNamedClasses() {		
		return Collections.unmodifiableSet(atomicConcepts);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Reasoner#getAtomicRoles()
	 */
	public Set<ObjectProperty> getObjectProperties() {
		return Collections.unmodifiableSet(atomicRoles);
	}

	@Override
	public SortedSet<DatatypeProperty> getDatatypePropertiesImpl() {
		return datatypeProperties;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.Reasoner#getIndividuals()
	 */
	public SortedSet<Individual> getIndividuals() {
		return individuals;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Reasoner#getReasonerType()
	 */
	@Override
	public ReasonerType getReasonerType() {
		if(configurator.getReasonerType().equals("fact"))
			return ReasonerType.OWLAPI_FACT;
		else
			return ReasonerType.OWLAPI_PELLET;
	}

//	@Override
//	public ObjectPropertyHierarchy prepareRoleHierarchy() {
//		// code copied from DIG reasoner
//		
//		TreeMap<ObjectProperty, TreeSet<ObjectProperty>> roleHierarchyUp = new TreeMap<ObjectProperty, TreeSet<ObjectProperty>>(
//				roleComparator);
//		TreeMap<ObjectProperty, TreeSet<ObjectProperty>> roleHierarchyDown = new TreeMap<ObjectProperty, TreeSet<ObjectProperty>>(
//				roleComparator);
// 
//		// refinement of atomic concepts
//		for (ObjectProperty role : atomicRoles) {
//			roleHierarchyDown.put(role, getMoreSpecialRolesImpl(role));
//			roleHierarchyUp.put(role, getMoreGeneralRolesImpl(role));
//		}
//
//		roleHierarchy = new ObjectPropertyHierarchy(atomicRoles, roleHierarchyUp,
//				roleHierarchyDown);
//		return roleHierarchy;
//	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.Reasoner#prepareRoleHierarchy(java.util.Set)
	 */
//	public void prepareRoleHierarchy(Set<ObjectProperty> allowedRoles) {
//		// code copied from DIG reasoner
//		
//		TreeMap<ObjectProperty, TreeSet<ObjectProperty>> roleHierarchyUp = new TreeMap<ObjectProperty, TreeSet<ObjectProperty>>(
//				roleComparator);
//		TreeMap<ObjectProperty, TreeSet<ObjectProperty>> roleHierarchyDown = new TreeMap<ObjectProperty, TreeSet<ObjectProperty>>(
//				roleComparator);
// 
//		// refinement of atomic concepts
//		for (ObjectProperty role : atomicRoles) {
//			roleHierarchyDown.put(role, getMoreSpecialRolesImpl(role));
//			roleHierarchyUp.put(role, getMoreGeneralRolesImpl(role));
//		}
//
//		roleHierarchy = new ObjectPropertyHierarchy(allowedRoles, roleHierarchyUp,
//				roleHierarchyDown);
//	}	
	
//	@Override
//	public ObjectPropertyHierarchy getRoleHierarchy() {
//		return roleHierarchy;
//	}	
		
//	public void prepareDatatypePropertyHierarchyImpl(Set<DatatypeProperty> allowedRoles) {
//		// code copied from DIG reasoner
//		
//		TreeMap<DatatypeProperty, TreeSet<DatatypeProperty>> datatypePropertyHierarchyUp = new TreeMap<DatatypeProperty, TreeSet<DatatypeProperty>>(
//				roleComparator);
//		TreeMap<DatatypeProperty, TreeSet<DatatypeProperty>> datatypePropertyHierarchyDown = new TreeMap<DatatypeProperty, TreeSet<DatatypeProperty>>(
//				roleComparator);
// 
//		// refinement of atomic concepts
//		for (DatatypeProperty role : datatypeProperties) {
//			datatypePropertyHierarchyDown.put(role, getMoreSpecialDatatypePropertiesImpl(role));
//			datatypePropertyHierarchyUp.put(role, getMoreGeneralDatatypePropertiesImpl(role));
//		}
//
//		datatypePropertyHierarchy = new DatatypePropertyHierarchy(allowedRoles, datatypePropertyHierarchyUp,
//				datatypePropertyHierarchyDown);
//	}	
	
//	@Override
//	public DatatypePropertyHierarchy getDatatypePropertyHierarchy() {
//		return datatypePropertyHierarchy;
//	}		
	
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
		
		classes = reasoner.getSuperClasses(OWLAPIDescriptionConvertVisitor.getOWLClassExpression(concept), false);
		
		return getFirstClasses(classes);
	}
	
	@Override
	protected TreeSet<Description> getSubClassesImpl(Description concept) {
		NodeSet<OWLClass> classes = null;
		
		classes = reasoner.getSubClasses(OWLAPIDescriptionConvertVisitor.getOWLClassExpression(concept), false);
		
		return getFirstClasses(classes);
	}
	
	@Override
	protected TreeSet<ObjectProperty> getSuperPropertiesImpl(ObjectProperty role) {
		NodeSet<OWLObjectProperty> properties = null;
		
		properties = reasoner.getSuperObjectProperties(OWLAPIConverter.getOWLAPIObjectProperty(role), false);
		 	
		return getFirstObjectProperties(properties);
	}
	
	@Override
	protected TreeSet<ObjectProperty> getSubPropertiesImpl(ObjectProperty role) {
		NodeSet<OWLObjectProperty> properties = null;
		
		properties = reasoner.getSubObjectProperties(OWLAPIConverter.getOWLAPIObjectProperty(role), false);
			
		return getFirstObjectProperties(properties);		
	}
	
	@Override
	protected TreeSet<DatatypeProperty> getSuperPropertiesImpl(DatatypeProperty role) {
		NodeSet<OWLDataProperty> properties = null;
		
		properties = reasoner.getSuperDataProperties(OWLAPIConverter.getOWLAPIDataProperty(role), false);
		 
		return getFirstDatatypeProperties(properties);
	}
	
	@Override
	protected TreeSet<DatatypeProperty> getSubPropertiesImpl(DatatypeProperty role) {
		NodeSet<OWLDataProperty> properties = null;
		
		properties = reasoner.getSubDataProperties(OWLAPIConverter.getOWLAPIDataProperty(role), false);
			
		return getFirstDatatypeProperties(properties);		
	}	
	
	@Override
	public boolean hasTypeImpl(Description concept, Individual individual) {
		OWLClassExpression d = OWLAPIDescriptionConvertVisitor.getOWLClassExpression(concept);
		OWLIndividual i = factory.getOWLNamedIndividual(IRI.create(individual.getName()));
		return reasoner.isEntailed(factory.getOWLClassAssertionAxiom(d, i));
	}
	
	@Override
	public SortedSet<Individual> getIndividualsImpl(Description concept) {
//		OWLDescription d = getOWLAPIDescription(concept);
		OWLClassExpression d = OWLAPIDescriptionConvertVisitor.getOWLClassExpression(concept);
		Set<OWLNamedIndividual> individuals = reasoner.getInstances(d, false).getFlattened();
		SortedSet<Individual> inds = new TreeSet<Individual>();
		for(OWLNamedIndividual ind : individuals)
			inds.add(new Individual(ind.toStringID()));
		return inds;
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
		
			// Pellet returns a set of nodes of named classes, which are more
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
		OWLClass oc = set.iterator().next().getRepresentativeElement();
		return new NamedClass(oc.toStringID());

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
			for(OWLLiteral oi : constants) {
				// for typed constants we have to figure out the correct
				// data type and value
				if(oi instanceof OWLTypedLiteral) {
					Datatype dt = OWLAPIConverter.convertDatatype(((OWLTypedLiteral)oi).getDatatype());
					is.add(new TypedConstant(oi.getLiteral(),dt));
				// for untyped constants we have to figure out the value
				// and language tag (if any)
				} else {
					OWLStringLiteralImpl ouc = (OWLStringLiteralImpl) oi;
					if(ouc.hasLang())
						is.add(new UntypedConstant(ouc.getLiteral(), ouc.getLang()));
					else
						is.add(new UntypedConstant(ouc.getLiteral()));
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
	
	private Set<NamedClass> getFirstClassesNoTopBottom(Set<Node<OWLClass>> nodeSet) {
		Set<NamedClass> concepts = new HashSet<NamedClass>();
		for(Node<OWLClass> node : nodeSet) {
			// take one element from the set and ignore the rest
			// (TODO: we need to make sure we always ignore the same concepts)
			OWLClass concept = node.getRepresentativeElement();
			if(!concept.isOWLThing() && !concept.isOWLNothing())
				concepts.add(new NamedClass(concept.toStringID()));
		}
		return concepts;			
	}
	
	private TreeSet<ObjectProperty> getFirstObjectProperties(NodeSet<OWLObjectProperty> nodeSet) {
		TreeSet<ObjectProperty> roles = new TreeSet<ObjectProperty>(roleComparator);
		for(Node<OWLObjectProperty> node : nodeSet) {
			// take one element from the set and ignore the rest
			// (TODO: we need to make sure we always ignore the same concepts)
			OWLObjectProperty property = node.getRepresentativeElement();
			roles.add(new ObjectProperty(property.toStringID()));
		}
		roles.remove(new ObjectProperty(factory.getOWLTopObjectProperty().toStringID()));
		roles.remove(new ObjectProperty(factory.getOWLBottomObjectProperty().toStringID()));
		return roles;		
	}	
	
	private TreeSet<DatatypeProperty> getFirstDatatypeProperties(NodeSet<OWLDataProperty> nodeSet) {
		TreeSet<DatatypeProperty> roles = new TreeSet<DatatypeProperty>(roleComparator);
		for(Node<OWLDataProperty> node : nodeSet) {
			OWLDataProperty property = node.getRepresentativeElement();
			roles.add(new DatatypeProperty(property.toStringID()));
		}
		roles.remove(new DatatypeProperty(factory.getOWLTopDataProperty().toStringID()));
		roles.remove(new DatatypeProperty(factory.getOWLBottomDataProperty().toStringID()));		
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
	 * Test 
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		String iri = "http://www.co-ode.org/ontologies/pizza/2007/02/12/pizza.owl";
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		try {
			OWLOntology ontology = manager.loadOntologyFromOntologyDocument(IRI.create(iri));
			new PelletReasonerFactory().createReasoner(ontology);
			System.out.println("Reasoner loaded succesfully.");
		} catch (Exception e) {
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

	/* (non-Javadoc)
	 * @see org.dllearner.core.Reasoner#getBaseURI()
	 */
	public String getBaseURI() {
		return baseURI;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Reasoner#getPrefixes()
	 */
	public Map<String, String> getPrefixes() {
		return prefixes;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.ReasonerComponent#releaseKB()
	 */
	@Override
	public void releaseKB() {
		reasoner.dispose();
	}

	public List<OWLOntology> getOWLAPIOntologies() {
		return owlAPIOntologies;
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
	
}
