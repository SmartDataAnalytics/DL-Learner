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

import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.config.ConfigEntry;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.InvalidConfigOptionValueException;
import org.dllearner.core.config.StringConfigOption;
import org.dllearner.core.dl.All;
import org.dllearner.core.dl.AtomicConcept;
import org.dllearner.core.dl.AtomicRole;
import org.dllearner.core.dl.Bottom;
import org.dllearner.core.dl.Concept;
import org.dllearner.core.dl.Conjunction;
import org.dllearner.core.dl.Disjunction;
import org.dllearner.core.dl.Exists;
import org.dllearner.core.dl.Individual;
import org.dllearner.core.dl.MultiConjunction;
import org.dllearner.core.dl.MultiDisjunction;
import org.dllearner.core.dl.Negation;
import org.dllearner.core.dl.RoleHierarchy;
import org.dllearner.core.dl.SubsumptionHierarchy;
import org.dllearner.core.dl.Top;
import org.dllearner.kb.OWLFile;
import org.dllearner.utilities.ConceptComparator;
import org.dllearner.utilities.RoleComparator;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.inference.OWLReasoner;
import org.semanticweb.owl.inference.OWLReasonerException;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLNamedObject;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;

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

	private String reasonerType = "FaCT++";
	
	private Set<KnowledgeSource> sources;
	private OWLReasoner reasoner;
	// the data factory is used to generate OWL API objects
	private OWLDataFactory factory;
	
	private ConceptComparator conceptComparator = new ConceptComparator();
	private RoleComparator roleComparator = new RoleComparator();
	private SubsumptionHierarchy subsumptionHierarchy;
	private RoleHierarchy roleHierarchy;	
	private Set<Concept> allowedConceptsInSubsumptionHierarchy;
	
	// primitives
	Set<AtomicConcept> atomicConcepts;
	Set<AtomicRole> atomicRoles;
	SortedSet<Individual> individuals;	
	
	public OWLAPIReasoner(Set<KnowledgeSource> sources) {
		this.sources = sources;
	}
	
	public static String getName() {
		return "OWL API reasoner";
	}	
	
	public static Collection<ConfigOption<?>> createConfigOptions() {
		Collection<ConfigOption<?>> options = new LinkedList<ConfigOption<?>>();
		StringConfigOption type = new StringConfigOption("reasonerType", "FaCT++ or Pellet", "FaCT++");
		type.setAllowedValues(new String[] {"FaCT++", "Pellet"});
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
		Set<OWLObjectProperty> properties = new TreeSet<OWLObjectProperty>(namedObjectComparator);
		Set<OWLIndividual> owlIndividuals = new TreeSet<OWLIndividual>(namedObjectComparator);
		
		for(KnowledgeSource source : sources) {
			if(!(source instanceof OWLFile)) {
				System.out.println("Currently, only OWL files are supported. Ignoring knowledge source " + source + ".");
			} else {
				URL url = ((OWLFile)source).getURL();
				try {
					OWLOntology ontology = manager.loadOntologyFromPhysicalURI(url.toURI());
					classes.addAll(ontology.getReferencedClasses());
					properties.addAll(ontology.getReferencedObjectProperties());
					owlIndividuals.addAll(ontology.getReferencedIndividuals());
				} catch (OWLOntologyCreationException e) {
					e.printStackTrace();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
		}
		
		// create actual reasoner
		if(reasonerType.equals("FaCT++")) {
			try {
				reasoner = new uk.ac.manchester.cs.factplusplus.owlapi.Reasoner(manager);
			} catch (Exception e) {
				e.printStackTrace();
			}			
		} else {
			// instantiate Pellet reasoner
			reasoner = new org.mindswap.pellet.owlapi.Reasoner(manager);
		}
		factory = manager.getOWLDataFactory();
		
		// read in primitives
		atomicConcepts = new TreeSet<AtomicConcept>(conceptComparator);
		for(OWLClass owlClass : classes)
			atomicConcepts.add(new AtomicConcept(owlClass.getURI().toString()));
		atomicRoles = new TreeSet<AtomicRole>(roleComparator);
		for(OWLObjectProperty owlProperty : properties)
			atomicRoles.add(new AtomicRole(owlProperty.getURI().toString()));
		individuals = new TreeSet<Individual>();
		for(OWLIndividual owlIndividual : owlIndividuals)
			individuals.add(new Individual(owlIndividual.getURI().toString()));
		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Reasoner#getAtomicConcepts()
	 */
	public Set<AtomicConcept> getAtomicConcepts() {
		return atomicConcepts;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Reasoner#getAtomicRoles()
	 */
	public Set<AtomicRole> getAtomicRoles() {
		return atomicRoles;
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
	public void prepareSubsumptionHierarchy(Set<AtomicConcept> allowedConcepts) {
		
		// implementation almost identical to DIG reasoner
		// except function calls
		
		allowedConceptsInSubsumptionHierarchy = new TreeSet<Concept>(conceptComparator);
		allowedConceptsInSubsumptionHierarchy.addAll(allowedConcepts);
		allowedConceptsInSubsumptionHierarchy.add(new Top());
		allowedConceptsInSubsumptionHierarchy.add(new Bottom());

		TreeMap<Concept, TreeSet<Concept>> subsumptionHierarchyUp = new TreeMap<Concept, TreeSet<Concept>>(
				conceptComparator);
		TreeMap<Concept, TreeSet<Concept>> subsumptionHierarchyDown = new TreeMap<Concept, TreeSet<Concept>>(
				conceptComparator);

		// refinements of top
		TreeSet<Concept> tmp = getMoreSpecialConcepts(new Top());
		tmp.retainAll(allowedConceptsInSubsumptionHierarchy);
		subsumptionHierarchyDown.put(new Top(), tmp);

		// refinements of bottom
		tmp = getMoreGeneralConcepts(new Bottom());
		tmp.retainAll(allowedConceptsInSubsumptionHierarchy);
		subsumptionHierarchyUp.put(new Bottom(), tmp);

		// refinements of atomic concepts
		for (AtomicConcept atom : atomicConcepts) {
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
	public void prepareRoleHierarchy(Set<AtomicRole> allowedRoles) {
		// code copied from DIG reasoner
		
		TreeMap<AtomicRole, TreeSet<AtomicRole>> roleHierarchyUp = new TreeMap<AtomicRole, TreeSet<AtomicRole>>(
				roleComparator);
		TreeMap<AtomicRole, TreeSet<AtomicRole>> roleHierarchyDown = new TreeMap<AtomicRole, TreeSet<AtomicRole>>(
				roleComparator);
 
		// refinement of atomic concepts
		for (AtomicRole role : atomicRoles) {
			roleHierarchyDown.put(role, getMoreSpecialRoles(role));
			roleHierarchyUp.put(role, getMoreGeneralRoles(role));
		}

		roleHierarchy = new RoleHierarchy(allowedRoles, roleHierarchyUp,
				roleHierarchyDown);
	}	
	
	@Override
	public RoleHierarchy getRoleHierarchy() {
		return roleHierarchy;
	}	
	
	@Override
	public boolean subsumes(Concept superConcept, Concept subConcept) {
		try {
			return reasoner.isSubClassOf(getOWLAPIDescription(subConcept), getOWLAPIDescription(superConcept));			
		} catch (OWLReasonerException e) {
			e.printStackTrace();
			throw new Error("Subsumption Error in OWL API.");
		}
	}
	
	private TreeSet<Concept> getMoreGeneralConcepts(Concept concept) {
		return null;
	}
	
	private TreeSet<Concept> getMoreSpecialConcepts(Concept concept) {
		return null;
	}	
	
	private TreeSet<AtomicRole> getMoreGeneralRoles(AtomicRole role) {
		return null;
	}
	
	private TreeSet<AtomicRole> getMoreSpecialRoles(AtomicRole role) {
		return null;
	}
	
	@Override
	public boolean instanceCheck(Concept concept, Individual individual) {
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
	public SortedSet<Individual> retrieval(Concept concept) {
		OWLDescription d = getOWLAPIDescription(concept);
		try {
			reasoner.getIndividuals(d, false);
		} catch (OWLReasonerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public Set<AtomicConcept> getConcepts(Individual individual) {
		Set<Set<OWLClass>> result = null;
		try {
			 result = reasoner.getTypes(factory.getOWLIndividual(URI.create(individual.getName())),false);
		} catch (OWLReasonerException e) {
			e.printStackTrace();
			throw new Error("GetConcepts() reasoning error in OWL API.");
		}
		// the OWL API returns a set of sets; each inner set consists of
		// atomic classes
		Set<AtomicConcept> concepts = new HashSet<AtomicConcept>();
		for(Set<OWLClass> classes : result) {
			// take one element from the set and ignore the rest
			// (TODO: we need to make sure we always ignore the same concepts)
			OWLClass concept = classes.iterator().next();
			concepts.add(new AtomicConcept(concept.getURI().toString()));
		}
		return concepts;
	}
	
	@Override
	public Map<Individual, SortedSet<Individual>> getRoleMembers(AtomicRole atomicRole) {
		return null;
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
	
	private Set<Concept> owlClassesToAtomicConcepts(Set<OWLClass> owlClasses) {
		Set<Concept> concepts = new HashSet<Concept>();
		for(OWLClass owlClass : owlClasses)
			concepts.add(owlClassToAtomicConcept(owlClass));
		return concepts;
	}
	
	private Concept owlClassToAtomicConcept(OWLClass owlClass) {
		return new AtomicConcept(owlClass.getURI().toString());
	}
	
	public OWLDescription getOWLAPIDescription(Concept concept) {
		if (concept instanceof AtomicConcept) {
			return factory.getOWLClass(URI.create(((AtomicConcept)concept).getName()));
		} else if (concept instanceof Bottom) {
			return factory.getOWLNothing();
		} else if (concept instanceof Top) {
			return factory.getOWLThing();
		} else if (concept instanceof Negation) {
			return factory.getOWLObjectComplementOf(
					getOWLAPIDescription(concept.getChild(0)));
		} else if (concept instanceof Conjunction) {
			OWLDescription d1 = getOWLAPIDescription(concept.getChild(0));
			OWLDescription d2 = getOWLAPIDescription(concept.getChild(1));
			Set<OWLDescription> d = new HashSet<OWLDescription>();
			d.add(d1);
			d.add(d2);
			return factory.getOWLObjectIntersectionOf(d);
		} else if (concept instanceof Disjunction) {
			OWLDescription d1 = getOWLAPIDescription(concept.getChild(0));
			OWLDescription d2 = getOWLAPIDescription(concept.getChild(1));
			Set<OWLDescription> d = new HashSet<OWLDescription>();
			d.add(d1);
			d.add(d2);
			return factory.getOWLObjectUnionOf(d);			
		} else if (concept instanceof All) {
			OWLObjectProperty role = factory.getOWLObjectProperty(
					URI.create(((All) concept).getRole().getName()));
			OWLDescription d = getOWLAPIDescription(concept.getChild(0));
			return factory.getOWLObjectAllRestriction(role, d);
		} else if(concept instanceof Exists) {
			OWLObjectProperty role = factory.getOWLObjectProperty(
					URI.create(((Exists) concept).getRole().getName()));
			OWLDescription d = getOWLAPIDescription(concept.getChild(0));
			return factory.getOWLObjectSomeRestriction(role, d);
		} else if(concept instanceof MultiConjunction) {
			Set<OWLDescription> descriptions = new HashSet<OWLDescription>();
			for(Concept child : concept.getChildren())
				descriptions.add(getOWLAPIDescription(child));
			return factory.getOWLObjectIntersectionOf(descriptions);
		} else if(concept instanceof MultiDisjunction) {
			Set<OWLDescription> descriptions = new HashSet<OWLDescription>();
			for(Concept child : concept.getChildren())
				descriptions.add(getOWLAPIDescription(child));
			return factory.getOWLObjectUnionOf(descriptions);			
		}
			
		throw new IllegalArgumentException("Unsupported concept type.");
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

}
