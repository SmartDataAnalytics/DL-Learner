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
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.config.ConfigEntry;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.InvalidConfigOptionValueException;
import org.dllearner.core.config.StringConfigOption;
import org.dllearner.core.owl.AssertionalAxiom;
import org.dllearner.core.owl.ClassAssertionAxiom;
import org.dllearner.core.owl.Constant;
import org.dllearner.core.owl.Datatype;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.DatatypePropertyHierarchy;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.EquivalentClassesAxiom;
import org.dllearner.core.owl.FunctionalObjectPropertyAxiom;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.InverseObjectPropertyAxiom;
import org.dllearner.core.owl.KB;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Negation;
import org.dllearner.core.owl.Nothing;
import org.dllearner.core.owl.ObjectAllRestriction;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectPropertyAssertion;
import org.dllearner.core.owl.ObjectPropertyHierarchy;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.dllearner.core.owl.PropertyAxiom;
import org.dllearner.core.owl.SubClassAxiom;
import org.dllearner.core.owl.SubObjectPropertyAxiom;
import org.dllearner.core.owl.SubsumptionHierarchy;
import org.dllearner.core.owl.SymmetricObjectPropertyAxiom;
import org.dllearner.core.owl.TerminologicalAxiom;
import org.dllearner.core.owl.Thing;
import org.dllearner.core.owl.TransitiveObjectPropertyAxiom;
import org.dllearner.core.owl.TypedConstant;
import org.dllearner.core.owl.Union;
import org.dllearner.core.owl.UntypedConstant;
import org.dllearner.kb.OWLFile;
import org.dllearner.utilities.ConceptComparator;
import org.dllearner.utilities.RoleComparator;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.inference.OWLReasoner;
import org.semanticweb.owl.inference.OWLReasonerException;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLConstant;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataRange;
import org.semanticweb.owl.model.OWLDataType;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLNamedObject;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyFormat;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLOntologyStorageException;
import org.semanticweb.owl.model.OWLTypedConstant;
import org.semanticweb.owl.model.OWLUntypedConstant;
import org.semanticweb.owl.model.UnknownOWLOntologyException;
import org.semanticweb.owl.util.SimpleURIMapper;
import org.semanticweb.owl.vocab.NamespaceOWLOntologyFormat;

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
	
	private String reasonerType = "pellet";
	
	private Set<KnowledgeSource> sources;
	private OWLReasoner reasoner;
	// the data factory is used to generate OWL API objects
	private OWLDataFactory factory;
	// static factory
	private static OWLDataFactory staticFactory = OWLManager.createOWLOntologyManager().getOWLDataFactory();
	
	private ConceptComparator conceptComparator = new ConceptComparator();
	private RoleComparator roleComparator = new RoleComparator();
	private SubsumptionHierarchy subsumptionHierarchy;
	private ObjectPropertyHierarchy roleHierarchy;	
	private DatatypePropertyHierarchy datatypePropertyHierarchy;
	private Set<Description> allowedConceptsInSubsumptionHierarchy;
	
	// primitives
	Set<NamedClass> atomicConcepts = new TreeSet<NamedClass>(conceptComparator);
	Set<ObjectProperty> atomicRoles = new TreeSet<ObjectProperty>(roleComparator);
	Set<DatatypeProperty> datatypeProperties = new TreeSet<DatatypeProperty>();
	Set<DatatypeProperty> booleanDatatypeProperties = new TreeSet<DatatypeProperty>();
	Set<DatatypeProperty> doubleDatatypeProperties = new TreeSet<DatatypeProperty>();
	Set<DatatypeProperty> intDatatypeProperties = new TreeSet<DatatypeProperty>();
	SortedSet<Individual> individuals = new TreeSet<Individual>();	
	
	// namespaces
	private Map<String, String> prefixes = new TreeMap<String,String>();
	private String baseURI;
	
	public OWLAPIReasoner(Set<KnowledgeSource> sources) {
		this.sources = sources;
	}
	
	public static String getName() {
		return "OWL API reasoner";
	}	
	
	public static Collection<ConfigOption<?>> createConfigOptions() {
		Collection<ConfigOption<?>> options = new LinkedList<ConfigOption<?>>();
		StringConfigOption type = new StringConfigOption("reasonerType", "FaCT++ or Pellet", "pellet");
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
		String name = entry.getOptionName();
		if(name.equals("reasonerType"))
			reasonerType = (String) entry.getValue();
	}	
	
	@Override
	public void init() {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		// it is a bit cumbersome to obtain all classes, because there
		// are no reasoner queries to obtain them => hence we query them
		// for each ontology and add them to a set; a comparator avoids
		// duplicates by checking URIs
		Comparator<OWLNamedObject> namedObjectComparator = new Comparator<OWLNamedObject>() {
			public int compare(OWLNamedObject o1, OWLNamedObject o2) {
				return o1.getURI().compareTo(o2.getURI());
			}	
		};		
		Set<OWLClass> classes = new TreeSet<OWLClass>(namedObjectComparator);
		Set<OWLObjectProperty> owlObjectProperties = new TreeSet<OWLObjectProperty>(namedObjectComparator);
		Set<OWLDataProperty> owlDatatypeProperties = new TreeSet<OWLDataProperty>(namedObjectComparator);
		Set<OWLIndividual> owlIndividuals = new TreeSet<OWLIndividual>(namedObjectComparator);
		
		Set<OWLOntology> allImports = new HashSet<OWLOntology>();
		
		for(KnowledgeSource source : sources) {
			// OWL files are read directly
			if(source instanceof OWLFile) {
				URL url = ((OWLFile)source).getURL();

				try {
					OWLOntology ontology = manager.loadOntologyFromPhysicalURI(url.toURI());
					allImports.addAll(manager.getImportsClosure(ontology));
					classes.addAll(ontology.getReferencedClasses());
					owlObjectProperties.addAll(ontology.getReferencedObjectProperties());
					owlDatatypeProperties.addAll(ontology.getReferencedDataProperties());				
					owlIndividuals.addAll(ontology.getReferencedIndividuals());
					
					// TODO: this obviously works only for exactly one knowledge source
					OWLOntologyFormat format = manager.getOntologyFormat(ontology);
					if(format instanceof NamespaceOWLOntologyFormat) {
						prefixes = ((NamespaceOWLOntologyFormat)format).getNamespacesByPrefixMap();
						baseURI = prefixes.get("");
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
				
				URI ontologyURI = URI.create("http://example.com");
				OWLOntology ontology = null;
				try {
					ontology = manager.createOntology(ontologyURI);
				} catch (OWLOntologyCreationException e) {
					e.printStackTrace();
				}
				OWLAPIAxiomConvertVisitor.fillOWLOntology(manager, ontology, kb);
				allImports.add(ontology);
				atomicConcepts.addAll(kb.findAllAtomicConcepts());
				atomicRoles.addAll(kb.findAllAtomicRoles());
				individuals.addAll(kb.findAllIndividuals());
				// TODO: add method to find datatypes
			}
		}
		
		// create actual reasoner
		if(reasonerType.equals("fact")) {
			try {
				reasoner = new uk.ac.manchester.cs.factplusplus.owlapi.Reasoner(manager);
			} catch (Exception e) {
				e.printStackTrace();
			}			
		} else {
			// instantiate Pellet reasoner
			reasoner = new org.mindswap.pellet.owlapi.Reasoner(manager);
			
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
		try {
			reasoner.loadOntologies(allImports);
			reasoner.classify();
			reasoner.realise();
		} catch (OWLReasonerException e) {
			e.printStackTrace();
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
			atomicConcepts.add(new NamedClass(owlClass.getURI().toString()));
		for(OWLObjectProperty owlProperty : owlObjectProperties)
			atomicRoles.add(new ObjectProperty(owlProperty.getURI().toString()));
		for(OWLDataProperty owlProperty : owlDatatypeProperties) {
			DatatypeProperty dtp = new DatatypeProperty(owlProperty.getURI().toString());
			Set<OWLDataRange> ranges = owlProperty.getRanges(allImports);
			OWLDataRange range = ranges.iterator().next();
			if(range.isDataType()) {
				URI uri = ((OWLDataType)range).getURI();
				if(uri.equals(Datatype.BOOLEAN.getURI()))
					booleanDatatypeProperties.add(dtp);
				else if(uri.equals(Datatype.DOUBLE.getURI()))
					doubleDatatypeProperties.add(dtp);
				else if(uri.equals(Datatype.INT.getURI()))
					intDatatypeProperties.add(dtp);				
			}
			datatypeProperties.add(dtp);
		}
		for(OWLIndividual owlIndividual : owlIndividuals)
			individuals.add(new Individual(owlIndividual.getURI().toString()));
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Reasoner#getAtomicConcepts()
	 */
	public Set<NamedClass> getAtomicConcepts() {
		return atomicConcepts;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Reasoner#getAtomicRoles()
	 */
	public Set<ObjectProperty> getAtomicRoles() {
		return atomicRoles;
	}

	@Override
	public Set<DatatypeProperty> getDatatypeProperties() {
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
	public ReasonerType getReasonerType() {
		if(reasonerType.equals("FaCT++"))
			return ReasonerType.FACT;
		else
			return ReasonerType.PELLET;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Reasoner#prepareSubsumptionHierarchy(java.util.Set)
	 */
	public void prepareSubsumptionHierarchy(Set<NamedClass> allowedConcepts) {
		
		// implementation almost identical to DIG reasoner
		// except function calls
		
		allowedConceptsInSubsumptionHierarchy = new TreeSet<Description>(conceptComparator);
		allowedConceptsInSubsumptionHierarchy.addAll(allowedConcepts);
		allowedConceptsInSubsumptionHierarchy.add(new Thing());
		allowedConceptsInSubsumptionHierarchy.add(new Nothing());

		TreeMap<Description, TreeSet<Description>> subsumptionHierarchyUp = new TreeMap<Description, TreeSet<Description>>(
				conceptComparator);
		TreeMap<Description, TreeSet<Description>> subsumptionHierarchyDown = new TreeMap<Description, TreeSet<Description>>(
				conceptComparator);

		// refinements of top
		TreeSet<Description> tmp = getMoreSpecialConcepts(new Thing());
		tmp.retainAll(allowedConceptsInSubsumptionHierarchy);
		subsumptionHierarchyDown.put(new Thing(), tmp);

		// refinements of bottom
		tmp = getMoreGeneralConcepts(new Nothing());
		tmp.retainAll(allowedConceptsInSubsumptionHierarchy);
		subsumptionHierarchyUp.put(new Nothing(), tmp);

		// refinements of atomic concepts
		for (NamedClass atom : atomicConcepts) {
			tmp = getMoreSpecialConcepts(atom);
			tmp.retainAll(allowedConceptsInSubsumptionHierarchy);
			subsumptionHierarchyDown.put(atom, tmp);

			tmp = getMoreGeneralConcepts(atom);
			tmp.retainAll(allowedConceptsInSubsumptionHierarchy);
			subsumptionHierarchyUp.put(atom, tmp);
		}

		// create subsumption hierarchy
		subsumptionHierarchy = new SubsumptionHierarchy(allowedConcepts,
				subsumptionHierarchyUp, subsumptionHierarchyDown);		
	}

	@Override
	public SubsumptionHierarchy getSubsumptionHierarchy() {
		return subsumptionHierarchy;
	}	
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.Reasoner#prepareRoleHierarchy(java.util.Set)
	 */
	@Override
	public void prepareRoleHierarchy(Set<ObjectProperty> allowedRoles) {
		// code copied from DIG reasoner
		
		TreeMap<ObjectProperty, TreeSet<ObjectProperty>> roleHierarchyUp = new TreeMap<ObjectProperty, TreeSet<ObjectProperty>>(
				roleComparator);
		TreeMap<ObjectProperty, TreeSet<ObjectProperty>> roleHierarchyDown = new TreeMap<ObjectProperty, TreeSet<ObjectProperty>>(
				roleComparator);
 
		// refinement of atomic concepts
		for (ObjectProperty role : atomicRoles) {
			roleHierarchyDown.put(role, getMoreSpecialRoles(role));
			roleHierarchyUp.put(role, getMoreGeneralRoles(role));
		}

		roleHierarchy = new ObjectPropertyHierarchy(allowedRoles, roleHierarchyUp,
				roleHierarchyDown);
	}	
	
	@Override
	public ObjectPropertyHierarchy getRoleHierarchy() {
		return roleHierarchy;
	}	
	
	@Override
	public void prepareDatatypePropertyHierarchy(Set<DatatypeProperty> allowedRoles) {
		// code copied from DIG reasoner
		
		TreeMap<DatatypeProperty, TreeSet<DatatypeProperty>> datatypePropertyHierarchyUp = new TreeMap<DatatypeProperty, TreeSet<DatatypeProperty>>(
				roleComparator);
		TreeMap<DatatypeProperty, TreeSet<DatatypeProperty>> datatypePropertyHierarchyDown = new TreeMap<DatatypeProperty, TreeSet<DatatypeProperty>>(
				roleComparator);
 
		// refinement of atomic concepts
		for (DatatypeProperty role : datatypeProperties) {
			datatypePropertyHierarchyDown.put(role, getMoreSpecialDatatypeProperties(role));
			datatypePropertyHierarchyUp.put(role, getMoreGeneralDatatypeProperties(role));
		}

		datatypePropertyHierarchy = new DatatypePropertyHierarchy(allowedRoles, datatypePropertyHierarchyUp,
				datatypePropertyHierarchyDown);
	}	
	
	@Override
	public DatatypePropertyHierarchy getDatatypePropertyHierarchy() {
		return datatypePropertyHierarchy;
	}		
	
	@Override
	public boolean subsumes(Description superConcept, Description subConcept) {
		try {
			return reasoner.isSubClassOf(OWLAPIDescriptionConvertVisitor.getOWLDescription(subConcept), OWLAPIDescriptionConvertVisitor.getOWLDescription(superConcept));			
		} catch (OWLReasonerException e) {
			e.printStackTrace();
			throw new Error("Subsumption Error in OWL API.");
		}
	}
	
	private TreeSet<Description> getMoreGeneralConcepts(Description concept) {
		Set<Set<OWLClass>> classes = null;
		try {
			classes = reasoner.getSuperClasses(getOWLAPIDescription(concept));
		} catch (OWLReasonerException e) {
			e.printStackTrace();
			throw new Error("OWL API classification error.");
		}
		return getFirstClasses(classes);
	}
	
	private TreeSet<Description> getMoreSpecialConcepts(Description concept) {
		Set<Set<OWLClass>> classes = null;
		try {
			classes = reasoner.getSubClasses(getOWLAPIDescription(concept));
		} catch (OWLReasonerException e) {
			e.printStackTrace();
			throw new Error("OWL API classification error.");
		}
		return getFirstClasses(classes);
	}	
	
	private TreeSet<ObjectProperty> getMoreGeneralRoles(ObjectProperty role) {
		Set<Set<OWLObjectProperty>> properties;
		try {
			properties = reasoner.getSuperProperties(getOWLAPIDescription(role));
		} catch (OWLReasonerException e) {
			e.printStackTrace();
			throw new Error("OWL API classification error.");
		}		
		return getFirstObjectProperties(properties);
	}
	
	private TreeSet<ObjectProperty> getMoreSpecialRoles(ObjectProperty role) {
		Set<Set<OWLObjectProperty>> properties;
		try {
			properties = reasoner.getSubProperties(getOWLAPIDescription(role));
		} catch (OWLReasonerException e) {
			e.printStackTrace();
			throw new Error("OWL API classification error.");
		}		
		return getFirstObjectProperties(properties);		
	}
	
	private TreeSet<DatatypeProperty> getMoreGeneralDatatypeProperties(DatatypeProperty role) {
		Set<Set<OWLDataProperty>> properties;
		try {
			properties = reasoner.getSuperProperties(getOWLAPIDescription(role));
		} catch (OWLReasonerException e) {
			e.printStackTrace();
			throw new Error("OWL API classification error.");
		}		
		return getFirstDatatypeProperties(properties);
	}
	
	private TreeSet<DatatypeProperty> getMoreSpecialDatatypeProperties(DatatypeProperty role) {
		Set<Set<OWLDataProperty>> properties;
		try {
			properties = reasoner.getSubProperties(getOWLAPIDescription(role));
		} catch (OWLReasonerException e) {
			e.printStackTrace();
			throw new Error("OWL API classification error.");
		}		
		return getFirstDatatypeProperties(properties);		
	}	
	
	@Override
	public boolean instanceCheck(Description concept, Individual individual) {
		OWLDescription d = getOWLAPIDescription(concept);
		OWLIndividual i = factory.getOWLIndividual(URI.create(individual.getName()));
		try {
			return reasoner.hasType(i,d,false);
		} catch (OWLReasonerException e) {
			e.printStackTrace();
			throw new Error("Instance check error in OWL API.");
		}
	}
	
	@Override
	public SortedSet<Individual> retrieval(Description concept) {
		OWLDescription d = getOWLAPIDescription(concept);
		Set<OWLIndividual> individuals = null;
		try {
			individuals = reasoner.getIndividuals(d, false);
		} catch (OWLReasonerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SortedSet<Individual> inds = new TreeSet<Individual>();
		for(OWLIndividual ind : individuals)
			inds.add(new Individual(ind.getURI().toString()));
		return inds;
	}
	
	@Override
	public Set<NamedClass> getConcepts(Individual individual) {
		Set<Set<OWLClass>> result = null;
		try {
			 result = reasoner.getTypes(factory.getOWLIndividual(URI.create(individual.getName())),false);
		} catch (OWLReasonerException e) {
			e.printStackTrace();
			throw new Error("GetConcepts() reasoning error in OWL API.");
		}
		return getFirstClassesNoTopBottom(result);
	}
	
	@Override
	public boolean isSatisfiable() {
		try {
			return reasoner.isSatisfiable(factory.getOWLThing());
		} catch (OWLReasonerException e) {
			e.printStackTrace();
			throw new Error("Satisfiability check error in OWL API.");
		}
	}
	
	@Override
	public Description getDomain(ObjectProperty objectProperty) {
		OWLObjectProperty prop = getOWLAPIDescription(objectProperty);
		try {
			// TODO: look up why OWL API return a two dimensional set here
			// instead of only one description (probably there can be several
			// domain axiom for one property and the inner set is a conjunction
			// of descriptions (?))
			// Answer: this function is just horribly broken in OWL API
			Set<Set<OWLDescription>> set = reasoner.getDomains(prop);
			if(set.size()==0)
				return new Thing();
			OWLClass oc = (OWLClass) set.iterator().next();
			return new NamedClass(oc.getURI().toString());
		} catch (OWLReasonerException e) {
			throw new Error(e);
		}
	}
	
	@Override
	public Description getDomain(DatatypeProperty datatypeProperty) {
		OWLDataProperty prop = getOWLAPIDescription(datatypeProperty);
		try {
			// TODO: look up why OWL API return a two dimensional set here
			// instead of only one description (probably there can be several
			// domain axiom for one property and the inner set is a conjunction
			// of descriptions (?))
			// Answer: this function is just horribly broken in OWL API
			Set<Set<OWLDescription>> set = reasoner.getDomains(prop);
			if(set.size()==0)
				return new Thing();
			OWLClass oc = (OWLClass) set.iterator().next();
			return new NamedClass(oc.getURI().toString());
		} catch (OWLReasonerException e) {
			throw new Error(e);
		}		
	}
	
	@Override
	public Description getRange(ObjectProperty objectProperty) {
		OWLObjectProperty prop = getOWLAPIDescription(objectProperty);
		try {
			Set<OWLDescription> set = reasoner.getRanges(prop);
			if(set.size()==0)
				return new Thing();
			OWLClass oc = (OWLClass) set.iterator().next();
			return new NamedClass(oc.getURI().toString());
		} catch (OWLReasonerException e) {
			throw new Error(e);
		}		
	}
	
	@Override
	public Map<Individual, SortedSet<Individual>> getRoleMembers(ObjectProperty atomicRole) {
		OWLObjectProperty prop = getOWLAPIDescription(atomicRole);
		Map<Individual, SortedSet<Individual>> map = new TreeMap<Individual, SortedSet<Individual>>();
		for(Individual i : individuals) {
			OWLIndividual ind = factory.getOWLIndividual(URI.create(i.getName()));
			
			// get all related individuals via OWL API
			Set<OWLIndividual> inds = null;
			try {
				inds = reasoner.getRelatedIndividuals(ind, prop);
			} catch (OWLReasonerException e) {
				e.printStackTrace();
			}
			
			// convert data back to DL-Learner structures
			SortedSet<Individual> is = new TreeSet<Individual>();
			for(OWLIndividual oi : inds)
				is.add(new Individual(oi.getURI().toString()));
			map.put(i, is);
		}
		return map;
	}
	
	@Override
	public Map<Individual, SortedSet<Constant>> getDatatypeMembers(DatatypeProperty datatypeProperty) {
		OWLDataProperty prop = getOWLAPIDescription(datatypeProperty);
		Map<Individual, SortedSet<Constant>> map = new TreeMap<Individual, SortedSet<Constant>>();
		for(Individual i : individuals) {
			OWLIndividual ind = factory.getOWLIndividual(URI.create(i.getName()));
			
			// get all related values via OWL API
			Set<OWLConstant> constants = null;
			try {
				constants = reasoner.getRelatedValues(ind, prop);
			} catch (OWLReasonerException e) {
				e.printStackTrace();
			}
			
			// convert data back to DL-Learner structures
			SortedSet<Constant> is = new TreeSet<Constant>();
			for(OWLConstant oi : constants) {
				// for typed constants we have to figure out the correct
				// data type and value
				if(oi instanceof OWLTypedConstant) {
					Datatype dt = convertDatatype(((OWLTypedConstant)oi).getDataType());
					is.add(new TypedConstant(oi.getLiteral(),dt));
				// for untyped constants we have to figure out the value
				// and language tag (if any)
				} else {
					OWLUntypedConstant ouc = (OWLUntypedConstant) oi;
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
	
	// OWL API often returns a set of sets of classes, where each inner
	// set consists of equivalent classes; this method picks one class
	// from each inner set to flatten the set of sets
	private TreeSet<Description> getFirstClasses(Set<Set<OWLClass>> setOfSets) {
		TreeSet<Description> concepts = new TreeSet<Description>(conceptComparator);
		for(Set<OWLClass> innerSet : setOfSets) {
			// take one element from the set and ignore the rest
			// (TODO: we need to make sure we always ignore the same concepts)
			OWLClass concept = innerSet.iterator().next();
			if(concept.isOWLThing()) {
				concepts.add(new Thing());
			} else if(concept.isOWLNothing()) {
				concepts.add(new Nothing());
			} else {
				concepts.add(new NamedClass(concept.getURI().toString()));
			}
		}
		return concepts;		
	}
	
	private Set<NamedClass> getFirstClassesNoTopBottom(Set<Set<OWLClass>> setOfSets) {
		Set<NamedClass> concepts = new HashSet<NamedClass>();
		for(Set<OWLClass> innerSet : setOfSets) {
			// take one element from the set and ignore the rest
			// (TODO: we need to make sure we always ignore the same concepts)
			OWLClass concept = innerSet.iterator().next();
			if(!concept.isOWLThing() && !concept.isOWLNothing())
				concepts.add(new NamedClass(concept.getURI().toString()));
		}
		return concepts;			
	}
	
	private TreeSet<ObjectProperty> getFirstObjectProperties(Set<Set<OWLObjectProperty>> setOfSets) {
		TreeSet<ObjectProperty> roles = new TreeSet<ObjectProperty>(roleComparator);
		for(Set<OWLObjectProperty> innerSet : setOfSets) {
			// take one element from the set and ignore the rest
			// (TODO: we need to make sure we always ignore the same concepts)
			OWLObjectProperty property = innerSet.iterator().next();
			roles.add(new ObjectProperty(property.getURI().toString()));
		}
		return roles;		
	}	
	
	private TreeSet<DatatypeProperty> getFirstDatatypeProperties(Set<Set<OWLDataProperty>> setOfSets) {
		TreeSet<DatatypeProperty> roles = new TreeSet<DatatypeProperty>(roleComparator);
		for(Set<OWLDataProperty> innerSet : setOfSets) {
			OWLDataProperty property = innerSet.iterator().next();
			roles.add(new DatatypeProperty(property.getURI().toString()));
		}
		return roles;		
	}		
	
	@SuppressWarnings({"unused"})
	private Set<Description> owlClassesToAtomicConcepts(Set<OWLClass> owlClasses) {
		Set<Description> concepts = new HashSet<Description>();
		for(OWLClass owlClass : owlClasses)
			concepts.add(owlClassToAtomicConcept(owlClass));
		return concepts;
	}
	
	private Description owlClassToAtomicConcept(OWLClass owlClass) {
		return new NamedClass(owlClass.getURI().toString());
	}
	
	public static void exportKBToOWL(File owlOutputFile, KB kb, URI ontologyURI) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		//URI ontologyURI = URI.create("http://example.com");
		URI physicalURI = owlOutputFile.toURI();
		SimpleURIMapper mapper = new SimpleURIMapper(ontologyURI, physicalURI);
		manager.addURIMapper(mapper);
		OWLOntology ontology;
		try {
			ontology = manager.createOntology(ontologyURI);
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
	
	public static Datatype convertDatatype(OWLDataType dataType) {
		URI uri = dataType.getURI();
		if(uri.equals(Datatype.BOOLEAN.getURI()))
			return Datatype.BOOLEAN;
		else if(uri.equals(Datatype.DOUBLE.getURI()))
			return Datatype.DOUBLE;
		else if(uri.equals(Datatype.INT.getURI()))
			return Datatype.INT;			
		
		throw new Error("Unsupported datatype " + dataType + ". Please inform a DL-Learner developer to add it.");
	}
	
	private static OWLObjectProperty getOWLAPIDescription(ObjectProperty role) {
		return staticFactory.getOWLObjectProperty(URI.create(role.getName()));
	}
	
	private static OWLDataProperty getOWLAPIDescription(DatatypeProperty datatypeProperty) {
		return staticFactory.getOWLDataProperty(URI.create(datatypeProperty.getName()));
	}	
	
	@Deprecated
	public static OWLDescription getOWLAPIDescription(Description concept) {
		if (concept instanceof NamedClass) {
			return staticFactory.getOWLClass(URI.create(((NamedClass)concept).getName()));
		} else if (concept instanceof Nothing) {
			return staticFactory.getOWLNothing();
		} else if (concept instanceof Thing) {
			return staticFactory.getOWLThing();
		} else if (concept instanceof Negation) {
			return staticFactory.getOWLObjectComplementOf(
					getOWLAPIDescription(concept.getChild(0)));
		} else if (concept instanceof ObjectAllRestriction) {
			OWLObjectProperty role = staticFactory.getOWLObjectProperty(
					URI.create(((ObjectAllRestriction) concept).getRole().getName()));
			OWLDescription d = getOWLAPIDescription(concept.getChild(0));
			return staticFactory.getOWLObjectAllRestriction(role, d);
		} else if(concept instanceof ObjectSomeRestriction) {
			OWLObjectProperty role = staticFactory.getOWLObjectProperty(
					URI.create(((ObjectSomeRestriction) concept).getRole().getName()));
			OWLDescription d = getOWLAPIDescription(concept.getChild(0));
			return staticFactory.getOWLObjectSomeRestriction(role, d);
		} else if(concept instanceof Intersection) {
			Set<OWLDescription> descriptions = new HashSet<OWLDescription>();
			for(Description child : concept.getChildren())
				descriptions.add(getOWLAPIDescription(child));
			return staticFactory.getOWLObjectIntersectionOf(descriptions);
		} else if(concept instanceof Union) {
			Set<OWLDescription> descriptions = new HashSet<OWLDescription>();
			for(Description child : concept.getChildren())
				descriptions.add(getOWLAPIDescription(child));
			return staticFactory.getOWLObjectUnionOf(descriptions);			
		}
			
		throw new IllegalArgumentException("Unsupported concept type.");
	}	
	
	@Deprecated
	public static void fillOWLAPIOntology(OWLOntologyManager manager, OWLOntology ontology, KB kb) {

		// OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		// OWLOntology ontology = manager.createOntology(ontologyURI);
		try {	
			for (AssertionalAxiom axiom : kb.getAbox()) {
				if (axiom instanceof ClassAssertionAxiom) {
					OWLDescription d = getOWLAPIDescription(((ClassAssertionAxiom) axiom)
							.getConcept());
					OWLIndividual i = factory.getOWLIndividual(URI.create(
							((ClassAssertionAxiom) axiom).getIndividual().getName()));
					OWLAxiom axiomOWLAPI = factory.getOWLClassAssertionAxiom(i, d);
					AddAxiom addAxiom = new AddAxiom(ontology, axiomOWLAPI);

						manager.applyChange(addAxiom);

				} else if (axiom instanceof ObjectPropertyAssertion) {
					OWLObjectProperty role = factory.getOWLObjectProperty(
							URI.create(((ObjectPropertyAssertion) axiom).getRole().getName()));
					OWLIndividual i1 = factory.getOWLIndividual(
							URI.create(((ObjectPropertyAssertion) axiom).getIndividual1().getName()));
					OWLIndividual i2 = factory.getOWLIndividual(
							URI.create(((ObjectPropertyAssertion) axiom).getIndividual2().getName()));
					OWLAxiom axiomOWLAPI = factory.getOWLObjectPropertyAssertionAxiom(i1, role, i2);
					AddAxiom addAxiom = new AddAxiom(ontology, axiomOWLAPI);
					manager.applyChange(addAxiom);
				}
			}

			for (PropertyAxiom axiom : kb.getRbox()) {
				if (axiom instanceof FunctionalObjectPropertyAxiom) {
					OWLObjectProperty role = factory.getOWLObjectProperty(
							URI.create(((FunctionalObjectPropertyAxiom) axiom).getRole().getName()));
					OWLAxiom axiomOWLAPI = factory.getOWLFunctionalObjectPropertyAxiom(role);
					AddAxiom addAxiom = new AddAxiom(ontology, axiomOWLAPI);
					manager.applyChange(addAxiom);
				} else if (axiom instanceof SymmetricObjectPropertyAxiom) {
					OWLObjectProperty role = factory.getOWLObjectProperty(
							URI.create(((SymmetricObjectPropertyAxiom) axiom).getRole().getName()));
					OWLAxiom axiomOWLAPI = factory.getOWLSymmetricObjectPropertyAxiom(role);
					AddAxiom addAxiom = new AddAxiom(ontology, axiomOWLAPI);
					manager.applyChange(addAxiom);					
				} else if (axiom instanceof TransitiveObjectPropertyAxiom) {
					OWLObjectProperty role = factory.getOWLObjectProperty(
							URI.create(((TransitiveObjectPropertyAxiom) axiom).getRole().getName()));
					OWLAxiom axiomOWLAPI = factory.getOWLTransitiveObjectPropertyAxiom(role);
					AddAxiom addAxiom = new AddAxiom(ontology, axiomOWLAPI);
					manager.applyChange(addAxiom);					
				} else if (axiom instanceof InverseObjectPropertyAxiom) {
					OWLObjectProperty role = factory.getOWLObjectProperty(
							URI.create(((InverseObjectPropertyAxiom) axiom).getRole().getName()));
					OWLObjectProperty inverseRole = factory.getOWLObjectProperty(
							URI.create(((InverseObjectPropertyAxiom) axiom).getInverseRole().getName()));
					OWLAxiom axiomOWLAPI = factory.getOWLInverseObjectPropertiesAxiom(role, inverseRole);
					AddAxiom addAxiom = new AddAxiom(ontology, axiomOWLAPI);
					manager.applyChange(addAxiom);
				} else if (axiom instanceof SubObjectPropertyAxiom) {
					OWLObjectProperty role = factory.getOWLObjectProperty(
							URI.create(((SubObjectPropertyAxiom) axiom).getRole().getName()));
					OWLObjectProperty subRole = factory.getOWLObjectProperty(
							URI.create(((SubObjectPropertyAxiom) axiom).getSubRole().getName()));
					OWLAxiom axiomOWLAPI = factory.getOWLSubObjectPropertyAxiom(subRole, role);
					AddAxiom addAxiom = new AddAxiom(ontology, axiomOWLAPI);
					manager.applyChange(addAxiom);
				}
			}

			for (TerminologicalAxiom axiom : kb.getTbox()) {
				if (axiom instanceof EquivalentClassesAxiom) {
					OWLDescription d1 = getOWLAPIDescription(((EquivalentClassesAxiom) axiom).getConcept1());
					OWLDescription d2 = getOWLAPIDescription(((EquivalentClassesAxiom) axiom).getConcept2());
					Set<OWLDescription> ds = new HashSet<OWLDescription>();
					ds.add(d1);
					ds.add(d2);
					OWLAxiom axiomOWLAPI = factory.getOWLEquivalentClassesAxiom(ds);
					AddAxiom addAxiom = new AddAxiom(ontology, axiomOWLAPI);
					manager.applyChange(addAxiom);					
				} else if (axiom instanceof SubClassAxiom) {
					OWLDescription subConcept = getOWLAPIDescription(((SubClassAxiom) axiom)
							.getSubConcept());
					OWLDescription superConcept = getOWLAPIDescription(((SubClassAxiom) axiom)
							.getSuperConcept());
					OWLAxiom axiomOWLAPI = factory.getOWLSubClassAxiom(subConcept, superConcept);
					AddAxiom addAxiom = new AddAxiom(ontology, axiomOWLAPI);
					manager.applyChange(addAxiom);
				}
			}
		} catch (OWLOntologyChangeException e) {
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
		 // System.out.println(System.getProperty("java.library.path"));
		
		String uri = "http://www.co-ode.org/ontologies/pizza/2007/02/12/pizza.owl";
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		try {
			manager.loadOntologyFromPhysicalURI(URI.create(uri));
			new org.mindswap.pellet.owlapi.Reasoner(manager);
			System.out.println("Reasoner loaded succesfully.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return the booleanDatatypeProperties
	 */
	@Override
	public Set<DatatypeProperty> getBooleanDatatypeProperties() {
		return booleanDatatypeProperties;
	}

	/**
	 * @return the doubleDatatypeProperties
	 */
	@Override
	public Set<DatatypeProperty> getDoubleDatatypeProperties() {
		return doubleDatatypeProperties;
	}

	/**
	 * @return the intDatatypeProperties
	 */
	@Override
	public Set<DatatypeProperty> getIntDatatypeProperties() {
		return intDatatypeProperties;
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

}
