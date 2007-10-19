package org.dllearner.reasoning;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
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

import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.config.CommonConfigOptions;
import org.dllearner.core.config.ConfigEntry;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.InvalidConfigOptionValueException;
import org.dllearner.core.dl.All;
import org.dllearner.core.dl.AssertionalAxiom;
import org.dllearner.core.dl.AtomicConcept;
import org.dllearner.core.dl.AtomicRole;
import org.dllearner.core.dl.Bottom;
import org.dllearner.core.dl.Concept;
import org.dllearner.core.dl.ConceptAssertion;
import org.dllearner.core.dl.Conjunction;
import org.dllearner.core.dl.Disjunction;
import org.dllearner.core.dl.Equality;
import org.dllearner.core.dl.Exists;
import org.dllearner.core.dl.FunctionalRoleAxiom;
import org.dllearner.core.dl.Inclusion;
import org.dllearner.core.dl.Individual;
import org.dllearner.core.dl.InverseRoleAxiom;
import org.dllearner.core.dl.KB;
import org.dllearner.core.dl.MultiConjunction;
import org.dllearner.core.dl.MultiDisjunction;
import org.dllearner.core.dl.Negation;
import org.dllearner.core.dl.RBoxAxiom;
import org.dllearner.core.dl.RoleAssertion;
import org.dllearner.core.dl.SubRoleAxiom;
import org.dllearner.core.dl.SymmetricRoleAxiom;
import org.dllearner.core.dl.TerminologicalAxiom;
import org.dllearner.core.dl.Top;
import org.dllearner.core.dl.TransitiveRoleAxiom;
import org.dllearner.utilities.ConceptComparator;
import org.dllearner.utilities.Helper;
import org.semanticweb.kaon2.api.DefaultOntologyResolver;
import org.semanticweb.kaon2.api.KAON2Connection;
import org.semanticweb.kaon2.api.KAON2Exception;
import org.semanticweb.kaon2.api.KAON2Manager;
import org.semanticweb.kaon2.api.Ontology;
import org.semanticweb.kaon2.api.Request;
import org.semanticweb.kaon2.api.formatting.OntologyFileFormat;
import org.semanticweb.kaon2.api.logic.Formula;
import org.semanticweb.kaon2.api.logic.Literal;
import org.semanticweb.kaon2.api.logic.QueryDefinition;
import org.semanticweb.kaon2.api.logic.Variable;
import org.semanticweb.kaon2.api.owl.axioms.ObjectPropertyAttribute;
import org.semanticweb.kaon2.api.owl.elements.Description;
import org.semanticweb.kaon2.api.owl.elements.OWLClass;
import org.semanticweb.kaon2.api.owl.elements.ObjectProperty;
import org.semanticweb.kaon2.api.reasoner.Query;
import org.semanticweb.kaon2.api.reasoner.SubsumptionHierarchy;
import org.semanticweb.kaon2.api.reasoner.SubsumptionHierarchy.Node;

/**
 * 
 * Subsumption-Hierarchie wird automatisch beim ersten Aufruf von getMoreGeneral|SpecialConcept
 * berechnet.
 * 
 * @author jl
 * 
 */
public class KAON2Reasoner extends ReasonerComponent {

	// configuration options
	private boolean una = false;
	
	ConceptComparator conceptComparator = new ConceptComparator();
	
	Set<AtomicConcept> atomicConcepts;
	Set<AtomicRole> atomicRoles;
	SortedSet<Individual> individuals;
	SubsumptionHierarchy kaon2SubsumptionHierarchy = null;
	org.dllearner.core.dl.SubsumptionHierarchy subsumptionHierarchy;
	
	private org.semanticweb.kaon2.api.reasoner.Reasoner kaon2Reasoner;
	private KAON2Connection kaon2Connection;

	public KAON2Reasoner(KB kb, Map<URL,org.dllearner.core.OntologyFormat> imports) {

		if(imports.size()>1)
			System.out.println("Warning: KAON2-Reasoner currently supports only one import file. Ignoring all other imports.");
		
		try {
			kaon2Connection = KAON2Manager.newConnection();
		} catch (KAON2Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	
		DefaultOntologyResolver resolver = new DefaultOntologyResolver();
		
		// Set<String> ontologyURIs = new HashSet<String>();
		String ontologyURI = "";
		URL importFile = null;
		if (!imports.isEmpty()) {
			// alter Code - nicht empfehlenswert, da feste URI zugewiesen wird
			// resolver.registerReplacement("foo", imports.get(0).toURI().toString());
			
			// neuer Code - liest Dateien richtig ein
			// es ist aber noch nicht richtig klar, was bei mehreren eingelesenen
			// Ontologien passieren soll
			// for(File file : imports) {
			//	String ontologyURI = resolver.registerOntology(file);
			//	ontologyURIs.add(ontologyURI);
			//}
			
			// eine beliebige Datei auswählen
			importFile = imports.keySet().iterator().next();
			
			try {
				// System.out.println(imports.get(0));
				// resolver.r
				
				// TODO: testen, ob Umstellung von File auf URL funktioniert!
				// ontologyURI = resolver.registerOntology(importFile);
				ontologyURI = resolver.registerOntology(importFile.toString());

			} catch (KAON2Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} else {
			// falls nur aus Config-Datei gelesen wird, dann wird resolver
			// eigentlich nicht gebraucht => trotzdem erwartet KAON2 ein physische
			// URI
			// resolver.registerReplacement("foo", "file:foo.xml");
			resolver.registerReplacement("http://localhost/foo", "file:nothing.xml");
		}

		kaon2Connection.setOntologyResolver(resolver);
		Ontology ontology = null;
		
		if (!imports.isEmpty()) {
			System.out.print("Importing Ontology " + importFile.toString() + " ... ");
			ontology = importKB(ontologyURI, imports.get(importFile), kaon2Connection);
		} else {
			try {
				// ontology = connection.createOntology("foo", new
				// HashMap<String, Object>());
				ontology = kaon2Connection.createOntology("http://localhost/foo",
						new HashMap<String, Object>());
			} catch (KAON2Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
        
		// System.out.println(importedFile.getPath().toURI().toString());
		// resolver.registerReplacement("foo", "file:" +
		// importedFile.getPath());
		// resolver.registerReplacement("foo",
		// importedFile.toURI().toString());

		kaon2Reasoner = getKAON2Reasoner(kb, ontology);

		// Individuals, Concepts und Roles aus Wissensbasis auslesen
		Request<ObjectProperty> requestRoles = ontology
				.createEntityRequest(ObjectProperty.class);
		Request<OWLClass> requestConcepts = ontology.createEntityRequest(OWLClass.class);
		Request<org.semanticweb.kaon2.api.owl.elements.Individual> requestIndividuals = ontology
				.createEntityRequest(org.semanticweb.kaon2.api.owl.elements.Individual.class);

		atomicConcepts = new HashSet<AtomicConcept>();
		atomicRoles = new HashSet<AtomicRole>();
		individuals = new TreeSet<Individual>();

		try {
			for (ObjectProperty role : requestRoles.get()) {
				atomicRoles.add(new AtomicRole(role.toString()));
				// getRole(role.toString());
			}

			for (OWLClass concept : requestConcepts.get()) {
				// Top und Bottom sind bei mir keine atomaren Konzepte, sondern
				// werden
				// extra behandelt
				if (!concept.equals(KAON2Manager.factory().thing())
						&& !concept.equals(KAON2Manager.factory().nothing()))
					atomicConcepts.add(new AtomicConcept(concept.toString()));
				// System.out.println(concept.toString());
			}
			for (org.semanticweb.kaon2.api.owl.elements.Individual ind : requestIndividuals.get()) {
				// getIndividual(ind.toString());
				individuals.add(new Individual(ind.toString()));
			}

			// je nachdem, ob unique names assumption aktiviert ist, muss
			// man jetzt noch hinzuf�gen, dass die Individuen verschieden sind
			if (una) {
				Set<org.semanticweb.kaon2.api.owl.elements.Individual> individualsSet = new HashSet<org.semanticweb.kaon2.api.owl.elements.Individual>();
				for (Individual individual : individuals)
					individualsSet.add(KAON2Manager.factory().individual(individual.getName()));
				ontology.addAxiom(KAON2Manager.factory().differentIndividuals(
						individualsSet));
			}

		} catch (KAON2Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// wandelt einen Bezeichner in eine URI für die interne KB
	// (http://localhost/foo) um;
	// die Umwandlungen machen KAON2 noch langsamer, aber sie sind notwendig
	// für eine korrekte Ontologie, die wiederum für den Export wichtig ist;
	// es wird einfach gesagt, dass alles was nicht mit "http://" beginnt ein
	// interner Bezeichner ist;
	// eine sauberere Lösung wäre für die internen Sachen auch immer eine URI
	// zu fordern bzw. eine ähnliche Heuristik wie hier schon beim parsen zu
	// verwenden; für DIG ist das allerdings nicht notwendig (basiert auf DLs,
	// also benötigt keine URIs) und erfordert relativ umfangreiche Änderungen
	// => es wird jetzt doch die saubere Lösung verwendet
	/*
	private String getInternalURI(String name) {
		if(name.startsWith("http://"))
			return name;
		else
			return internalNamespace + name;
	}
	
	// wandelt eine lokal vergebene URI in einen Bezeichner um
	private String getNameFromInternalURI(String uri) {
		if(uri.startsWith(internalNamespace))
			return uri.substring(internalNamespace.length());
		else
			return uri;
	}
	*/
	
	// TODO: hier werden momentan keine allowed concepts berücksichtigt
	// (benötigt rekursive Aufrufe, da ein erlaubtes Konzept von einem nicht
	// erlaubten verdeckt werden könnte)
	public void prepareSubsumptionHierarchy(Set<AtomicConcept> allowedConcepts) {
		try {
			kaon2SubsumptionHierarchy = kaon2Reasoner.getSubsumptionHierarchy();
		} catch (KAON2Exception e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// umwandeln in eine für die Lernalgorithmen verwertbare 
		// Subsumptionhierarchie
		TreeMap<Concept,TreeSet<Concept>> subsumptionHierarchyUp = new TreeMap<Concept,TreeSet<Concept>>(conceptComparator);
		TreeMap<Concept,TreeSet<Concept>> subsumptionHierarchyDown = new TreeMap<Concept,TreeSet<Concept>>(conceptComparator);
		
		Top top = new Top();
		OWLClass kaon2Top = KAON2Manager.factory().thing();
		subsumptionHierarchyDown.put(top, (TreeSet<Concept>) getConceptsFromSubsumptionHierarchyNodes(kaon2SubsumptionHierarchy.getNodeFor(kaon2Top).getChildNodes()));
		// subsumptionHierarchyUp.put(top, new TreeSet<Concept>(conceptComparator));
		
		Bottom bottom = new Bottom();
		OWLClass kaon2Bottom = KAON2Manager.factory().nothing();
		subsumptionHierarchyUp.put(bottom, (TreeSet<Concept>) getConceptsFromSubsumptionHierarchyNodes(kaon2SubsumptionHierarchy.getNodeFor(kaon2Bottom).getParentNodes()));
		// subsumptionHierarchyDown.put(bottom, new TreeSet<Concept>(conceptComparator));
		
		for(AtomicConcept ac : atomicConcepts) {
			OWLClass kaon2Ac = (OWLClass) getKAON2Description(ac);
			subsumptionHierarchyDown.put(ac, (TreeSet<Concept>) getConceptsFromSubsumptionHierarchyNodes(kaon2SubsumptionHierarchy.getNodeFor(kaon2Ac).getChildNodes()));
			subsumptionHierarchyUp.put(ac, (TreeSet<Concept>) getConceptsFromSubsumptionHierarchyNodes(kaon2SubsumptionHierarchy.getNodeFor(kaon2Ac).getParentNodes()));
		}
		
		subsumptionHierarchy = new org.dllearner.core.dl.SubsumptionHierarchy(atomicConcepts, subsumptionHierarchyUp, subsumptionHierarchyDown);
	}
	
	@Override	
	public SortedSet<Individual> retrieval(Concept c) {
		SortedSet<Individual> result = new TreeSet<Individual>();
		Description d = getKAON2Description(c);

		Query query = null;
		try {
			query = kaon2Reasoner.createQuery(d);
			query.open();
			while (!query.afterLast()) {
				// tupleBuffer = query.tupleBuffer();
				String individual = query.tupleBuffer()[0].toString();
				result.add(new Individual(individual));
				query.next();
			}
			query.close();
			query.dispose();
		} catch (KAON2Exception e) {
			e.printStackTrace();
			System.exit(0);
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(0);
		}
		return result;
	}

	public boolean instanceCheck(Concept c, String s) {
		boolean result;

		Description d = getKAON2Description(c);
		// ev. Aufruf Main.getIndividual g�nstiger??
		org.semanticweb.kaon2.api.owl.elements.Individual i = KAON2Manager.factory().individual(s);

		// Individual i = Main.getIndividual(s);
		// ClassMember cm = KAON2Manager.factory().classMember(d, i);
		// Formula f = KAON2Manager.factory().classMember(d,i);
		// Constant constant = KAON2Manager.factory().constant(i);
		// Predicate predicate = KAON2Manager.factory().p
		Literal l = KAON2Manager.factory().literal(true, d, i);
		// Formula f = KAON2Manager.factory().

		// TODO: mal mit Boris abkl�ren wie der Instance-Check
		// gemacht werden soll; eine Formel zu erstellen erscheint
		// mir ziemlich umst�ndlich und ist mir auch nicht ganz
		// klar wie das gemacht wird

		QueryDefinition qd = KAON2Manager.factory().queryDefinition(l, new Variable[] {});

		// kaon2Reasoner.createQuery(l, new Variable[] { });
		// Query q;
		result = true;
		try {
			Query query = kaon2Reasoner.createQuery(qd);
			query.open();
			if (query.afterLast())
				result = false;
			query.close();
			query.dispose();
		} catch (KAON2Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	@Override
	public boolean subsumes(Concept superConcept, Concept subConcept) {
		Description d1 = getKAON2Description(superConcept);
		Description d2 = getKAON2Description(subConcept);
		try {
			return kaon2Reasoner.subsumedBy(d2, d1);
		} catch (KAON2Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		throw new Error("Subsumption Error in KAON2.");
	}

	@Override	
	public boolean isSatisfiable() {
		try {
			return kaon2Reasoner.isSatisfiable();
		} catch (KAON2Exception e1) {
			e1.printStackTrace();
		} catch (InterruptedException e2) {
			e2.printStackTrace();
		}
		throw new Error("Error in satisfiability check in KAON2.");
	}

	/*
	private SortedSet<Concept> getMoreGeneralConcepts(Concept concept) {
		// if (subsumptionHierarchy == null) {
		//	computeSubsumptionHierarchy();
		// }

		Description d = getKAON2Description(concept);
		if(!(d instanceof OWLClass)) {
			System.out.println("description: " + d);
			System.out.println("concept:" + concept);
		}
		OWLClass owlClass = (OWLClass) d;
		return getConceptsFromSubsumptionHierarchyNodes(kaon2SubsumptionHierarchy.getNodeFor(
				owlClass).getParentNodes());
	}

	private SortedSet<Concept> getMoreSpecialConcepts(Concept concept) {
		// if (subsumptionHierarchy == null) {
		//	computeSubsumptionHierarchy();
		// }

		OWLClass owlClass = (OWLClass) getKAON2Description(concept);
		return getConceptsFromSubsumptionHierarchyNodes(kaon2SubsumptionHierarchy.getNodeFor(
				owlClass).getChildNodes());
	}
	*/

	@Override	
	public org.dllearner.core.dl.SubsumptionHierarchy getSubsumptionHierarchy() {
		return subsumptionHierarchy;
	}
	
	private SortedSet<Concept> getConceptsFromSubsumptionHierarchyNodes(Set<Node> nodes) {
		SortedSet<Concept> ret = new TreeSet<Concept>(conceptComparator);
		for (Node node : nodes) {
			// es wird nur das erste Konzept unter mehreren �quivalenten
			// beachtet
			Iterator<OWLClass> it = node.getOWLClasses().iterator();
			ret.add(getConcept(it.next()));
			if (node.getOWLClasses().size() > 1)
				System.out
						.println("Warning: Ontology contains equivalent classes. Only one"
								+ "representative of each equivalence class is used for learning. The others"
								+ "are ignored.");
		}
		return ret;
	}

	public Concept getConcept(Description description) {
		if (description.equals(KAON2Manager.factory().thing())) {
			return new Top();
		} else if (description.equals(KAON2Manager.factory().nothing())) {
			return new Bottom();
		} else if (description instanceof OWLClass) {
			return new AtomicConcept(description.toString());
		} else {
			throw new Error("Transforming complex KAON2 descriptions not supported.");
		}
	}

	@Override	
	public Map<org.dllearner.core.dl.Individual, SortedSet<org.dllearner.core.dl.Individual>> getRoleMembers(AtomicRole atomicRole) {
		Map<org.dllearner.core.dl.Individual, SortedSet<org.dllearner.core.dl.Individual>> returnMap = new TreeMap<org.dllearner.core.dl.Individual, SortedSet<org.dllearner.core.dl.Individual>>();

		Query query;
		Object[] tupleBuffer;
		ObjectProperty role = KAON2Manager.factory().objectProperty(atomicRole.getName());
		// positiver Query
		try {
			query = kaon2Reasoner.createQuery(role);

			query.open();
			while (!query.afterLast()) {
				tupleBuffer = query.tupleBuffer();
				org.dllearner.core.dl.Individual individual1 = new org.dllearner.core.dl.Individual(tupleBuffer[0].toString());
				org.dllearner.core.dl.Individual individual2 = new org.dllearner.core.dl.Individual(tupleBuffer[1].toString());
				// addIndividualToRole(aBox.rolesPos, name, individual1,
				// individual2);
				Helper.addMapEntry(returnMap, individual1, individual2);
				query.next();
			}
			query.close();
			query.dispose();
		} catch (KAON2Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return returnMap;
	}

	public void saveOntology(File file, org.dllearner.core.OntologyFormat format) {
		// File exportFile = new File(baseDir, fileName);
		// String format = OntologyFileFormat.OWL_RDF;
		String kaon2Format = "";
		if(format.equals(org.dllearner.core.OntologyFormat.RDF_XML))
			kaon2Format = OntologyFileFormat.OWL_RDF;
		else {
			System.err.println("Warning: Cannot export format " + format + ". Exiting.");
			System.exit(0);
		}
			
		try {
			kaon2Reasoner.getOntology().saveOntology(kaon2Format,file,"ISO-8859-1");
		} catch (KAON2Exception e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static Ontology importKB(File importFile) {
		
		Ontology ontology = null;
		try {
			System.out.print("Importing " + importFile + " ... ");
			long importStartTime = System.currentTimeMillis();
			
			// TODO: hier wird Ontologie richtig importiert; dass muss im 
			// Konstruktor eventuell entsprechend angepasst werden
			DefaultOntologyResolver resolver = new DefaultOntologyResolver();
			String ontologyURI = resolver.registerOntology(importFile);
			KAON2Connection connection = KAON2Manager.newConnection();
			connection.setOntologyResolver(resolver);
			ontology = connection.openOntology(ontologyURI, new HashMap<String,Object>());
			long importDuration = System.currentTimeMillis() - importStartTime;
			System.out.println("OK (" + importDuration + " ms) [ontology URI " + ontologyURI + "]");
		} catch (KAON2Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ontology;	
	}
	
	private static Ontology importKB(String ontologyURI, org.dllearner.core.OntologyFormat format, KAON2Connection connection) {
		Ontology ontology = null;
		try {
			long importStartTime = System.currentTimeMillis();
			ontology = connection.openOntology(ontologyURI, new HashMap<String, Object>());
			long importDuration = System.currentTimeMillis() - importStartTime;
			System.out.println("OK (" + importDuration + " ms)");
		} catch (KAON2Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ontology;
	}

	// Umwandlung eines Konzepts in eigener Darstellung zu einem
	// KAON2-Konzept (hat die st�ndige Umwandlung eine gro�e Auswirkung
	// auf die Effizienz? - es m�ssen zumindest h�ufig neue Konzepte
	// erzeugt werden)
	public static Description getKAON2Description(Concept concept) {
		if (concept instanceof AtomicConcept) {
			return KAON2Manager.factory().owlClass(((AtomicConcept) concept).getName());
		} else if (concept instanceof Bottom) {
			return KAON2Manager.factory().nothing();
		} else if (concept instanceof Top) {
			return KAON2Manager.factory().thing();
		} else if (concept instanceof Negation) {
			return KAON2Manager.factory().objectNot(
					getKAON2Description(concept.getChild(0)));
		} else if (concept instanceof Conjunction) {
			Description d1 = getKAON2Description(concept.getChild(0));
			Description d2 = getKAON2Description(concept.getChild(1));
			return KAON2Manager.factory().objectAnd(d1, d2);
		} else if (concept instanceof Disjunction) {
			Description d1 = getKAON2Description(concept.getChild(0));
			Description d2 = getKAON2Description(concept.getChild(1));
			return KAON2Manager.factory().objectOr(d1, d2);
		} else if (concept instanceof All) {
			ObjectProperty role = KAON2Manager.factory().objectProperty(
					((All) concept).getRole().getName());
			Description d = getKAON2Description(concept.getChild(0));
			return KAON2Manager.factory().objectAll(role, d);
		} else if(concept instanceof Exists) {
			ObjectProperty role = KAON2Manager.factory().objectProperty(
					((Exists) concept).getRole().getName());
			Description d = getKAON2Description(concept.getChild(0));
			return KAON2Manager.factory().objectSome(role, d);
		} else if(concept instanceof MultiConjunction) {
			List<Description> descriptions = new LinkedList<Description>();
			for(Concept child : concept.getChildren()) {
				descriptions.add(getKAON2Description(child));
			}
			return KAON2Manager.factory().objectAnd(descriptions);
		} else if(concept instanceof MultiDisjunction) {
			List<Description> descriptions = new LinkedList<Description>();
			for(Concept child : concept.getChildren()) {
				descriptions.add(getKAON2Description(child));
			}
			return KAON2Manager.factory().objectOr(descriptions);			
		}
			
		throw new IllegalArgumentException("Unsupported concept type.");
	}

	public static org.semanticweb.kaon2.api.reasoner.Reasoner getKAON2Reasoner(KB kb) {
		try {
			KAON2Connection connection = KAON2Manager.newConnection();
			
			DefaultOntologyResolver resolver = new DefaultOntologyResolver();
			resolver.registerReplacement("http://localhost/foo", "file:nothing.xml");
			connection.setOntologyResolver(resolver);
			Ontology ontology = connection.createOntology("http://localhost/foo",
							new HashMap<String, Object>());
			return getKAON2Reasoner(kb, ontology);
			
		} catch (KAON2Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static org.semanticweb.kaon2.api.reasoner.Reasoner getKAON2Reasoner(KB kb,
			Ontology ontology) {

		org.semanticweb.kaon2.api.reasoner.Reasoner reasoner = null;

		try {

			for (AssertionalAxiom axiom : kb.getAbox()) {
				if (axiom instanceof ConceptAssertion) {
					Description d = getKAON2Description(((ConceptAssertion) axiom)
							.getConcept());
					// TODO: checken ob unterschiedliche Objekte
					// unterschiedliche
					// Individuen sind, auch wenn sie den gleichen Namen haben
					org.semanticweb.kaon2.api.owl.elements.Individual i = KAON2Manager.factory().individual(
							((ConceptAssertion) axiom).getIndividual().getName());
					ontology.addAxiom(KAON2Manager.factory().classMember(d, i));
				} else if (axiom instanceof RoleAssertion) {
					ObjectProperty role = KAON2Manager.factory().objectProperty(
							((RoleAssertion) axiom).getRole().getName());
					org.semanticweb.kaon2.api.owl.elements.Individual i1 = KAON2Manager.factory().individual(
							((RoleAssertion) axiom).getIndividual1().getName());
					org.semanticweb.kaon2.api.owl.elements.Individual i2 = KAON2Manager.factory().individual(
							((RoleAssertion) axiom).getIndividual2().getName());
					// Code zur Unterst�tzung negierter Rollenzusicherungen,
					// falls sp�ter ben�tigt
					// Literal l = KAON2Manager.factory().literal(true, role,
					// i1, i2);
					// Rule r = KAON2Manager.factory().rule(new Formula[] {},
					// true, new Formula[] { l });
					// changes.add(new OntologyChangeEvent(r,
					// OntologyChangeEvent.ChangeType.ADD));
					ontology.addAxiom(KAON2Manager.factory().objectPropertyMember(role,
							i1, i2));
				}
			}

			for (RBoxAxiom axiom : kb.getRbox()) {
				if (axiom instanceof FunctionalRoleAxiom) {
					ObjectProperty role = KAON2Manager.factory().objectProperty(
							((FunctionalRoleAxiom) axiom).getRole().getName());
					ontology.addAxiom(KAON2Manager.factory().objectPropertyAttribute(
							role, ObjectPropertyAttribute.OBJECT_PROPERTY_FUNCTIONAL));
				} else if (axiom instanceof SymmetricRoleAxiom) {
					ObjectProperty role = KAON2Manager.factory().objectProperty(
							((SymmetricRoleAxiom) axiom).getRole().getName());
					ontology.addAxiom(KAON2Manager.factory().objectPropertyAttribute(
							role, ObjectPropertyAttribute.OBJECT_PROPERTY_SYMMETRIC));

					// alternative Implementierung ohne Hilfskonstrukt
					//ObjectProperty inverseRole = KAON2Manager.factory().objectProperty(
					//		((SymmetricRoleAxiom) axiom).getRole().getName());		
					//ontology.addAxiom(KAON2Manager.factory().inverseObjectProperties(
					//		role, inverseRole));
					//ontology.addAxiom(KAON2Manager.factory().equivalentObjectProperties(role,
					//		inverseRole));
				} else if (axiom instanceof TransitiveRoleAxiom) {
					ObjectProperty role = KAON2Manager.factory().objectProperty(
							((SymmetricRoleAxiom) axiom).getRole().getName());
					ontology.addAxiom(KAON2Manager.factory().objectPropertyAttribute(
							role, ObjectPropertyAttribute.OBJECT_PROPERTY_TRANSITIVE));
				} else if (axiom instanceof InverseRoleAxiom) {
					ObjectProperty role = KAON2Manager.factory().objectProperty(
							((InverseRoleAxiom) axiom).getRole().getName());
					ObjectProperty inverseRole = KAON2Manager.factory().objectProperty(
							((InverseRoleAxiom) axiom).getInverseRole().getName());
					ontology.addAxiom(KAON2Manager.factory().inverseObjectProperties(
							role, inverseRole));
				} else if (axiom instanceof SubRoleAxiom) {
					ObjectProperty role = KAON2Manager.factory().objectProperty(
							((SubRoleAxiom) axiom).getRole().getName());
					ObjectProperty subRole = KAON2Manager.factory().objectProperty(
							((SubRoleAxiom) axiom).getSubRole().getName());
					ontology.addAxiom(KAON2Manager.factory().subObjectPropertyOf(subRole,
							role));
				}
			}

			for (TerminologicalAxiom axiom : kb.getTbox()) {
				if (axiom instanceof Equality) {
					Description d1 = getKAON2Description(((Equality) axiom).getConcept1());
					Description d2 = getKAON2Description(((Equality) axiom).getConcept2());
					ontology.addAxiom(KAON2Manager.factory().equivalentClasses(d1, d2));
				} else if (axiom instanceof Inclusion) {
					Description subConcept = getKAON2Description(((Inclusion) axiom)
							.getSubConcept());
					Description superConcept = getKAON2Description(((Inclusion) axiom)
							.getSuperConcept());
					ontology.addAxiom(KAON2Manager.factory().subClassOf(subConcept,
							superConcept));
				}
			}

			// ontology.applyChanges(changes);
			reasoner = ontology.createReasoner();
		} catch (KAON2Exception e) {
			e.printStackTrace();
			System.out.println(e);
		}

		return reasoner;
	}

	public Map<String, SortedSet<String>> getNegatedRoleMembers(AtomicRole atomicRole) {
		Map<String, SortedSet<String>> returnMap = new TreeMap<String, SortedSet<String>>();

		Query query;
		Object[] tupleBuffer;
		ObjectProperty role = KAON2Manager.factory().objectProperty(atomicRole.getName());
		// negativer Query
		// wird �ber Rule-ML gemacht
		Variable X = KAON2Manager.factory().variable("X");
		Variable Y = KAON2Manager.factory().variable("Y");
		// ObjectProperty role = roles.get(name);
		Literal l = KAON2Manager.factory().literal(true, role, X, Y);
		Formula f = null;

		// falls closed world assumption, dann reicht default negation
//		if (Config.owa)
			// wegen BUG IN KAON2 momentan auskommentiert
			// f = KAON2Manager.factory().classicalNegation(l);
//			;
//		else
			f = KAON2Manager.factory().defaultNegation(l);

		// if-Teil entf�llt, sobald Bug in KAON2 gefixt wurde
//		if (!Config.owa) {
			// ClassicalNegation cn =
			// KAON2Manager.factory().classicalNegation(l);
			try {
				query = kaon2Reasoner.createQuery(f, new Variable[] { X, Y }, null, null);

				// BUG IN KAON2, DESWEGEN AUSKOMMENTIERT

				// System.out.println();
				query.open();
				while (!query.afterLast()) {
					tupleBuffer = query.tupleBuffer();
					String individual1 = tupleBuffer[0].toString();
					String individual2 = tupleBuffer[1].toString();
					// addIndividualToRole(aBox.rolesNeg, name,
					// individual1,individual2);
					System.out.println(atomicRole.getName() + " " + individual1 + " "
							+ individual2);
					query.next();
				}
				query.close();
				query.dispose();
			} catch (KAON2Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//		}

		return returnMap;
	}

	public void terminateReasoner() {
		kaon2Reasoner.dispose();
		try {
			kaon2Connection.close();
		} catch (KAON2Exception e) {
			e.printStackTrace();
		}
	}

	public ReasonerType getReasonerType() {
		return ReasonerType.KAON2;
	}

	public Set<AtomicConcept> getAtomicConcepts() {
		return atomicConcepts;
	}

	public Set<AtomicRole> getAtomicRoles() {
		return atomicRoles;
	}

	public SortedSet<Individual> getIndividuals() {
		return individuals;
	}

	public static Collection<ConfigOption<?>> createConfigOptions() {
		Collection<ConfigOption<?>> options = new LinkedList<ConfigOption<?>>();
		options.add(CommonConfigOptions.getUNA());
		return options;
	}	
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#applyConfigEntry(org.dllearner.core.ConfigEntry)
	 */
	@Override
	public <T> void applyConfigEntry(ConfigEntry<T> entry) throws InvalidConfigOptionValueException {
		String name = entry.getOptionName();
		if(name.equals("una"))
			una = (Boolean) entry.getValue();
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#init()
	 */
	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}
	
	// Problem: mit den eigenen Datenstrukturen wird OWL nicht vollständig
	// abgedeckt z.B. data types, d.h. ohne Erweiterung der internen Strukturen
	// kann nicht jede KAON2-Ontologie importiert werden
	// TODO: unvollständig
	/*
	public static void importKAON2Ontology(KB kb, Ontology ontology) throws KAON2Exception {
		Set<Axiom> axioms = ontology.createAxiomRequest().getAll();
		
		// KB kb = new KB();
		for(Axiom axiom : axioms) {
			if(axiom instanceof ClassMember) {
				String individual = ((ClassMember)axiom).getIndividual().toString();
				Concept concept = importKAON2Concept(((ClassMember)axiom).getDescription());
				ConceptAssertion ca = new ConceptAssertion(concept, individual);
				kb.addABoxAxiom(ca);
			// da es eine externe API ist, können wir nicht sicher sein alle
			// Axiome erwischt zu haben
			} else {
				throw new RuntimeException("Failed to import the following axiom: " + axiom);
			}
		}
	}
	*/
	
	// TODO: unvollständig
	/*
	private static Concept importKAON2Concept(Description description) {
		if (description.equals(KAON2Manager.factory().thing())) {
			return new Top();
		} else if (description.equals(KAON2Manager.factory().nothing())) {
			return new Bottom();
		} else if (description instanceof OWLClass) {
			return new AtomicConcept(description.toString());
		} else {
			throw new RuntimeException("Failed to convert the following KAON2 description: " + description);
		}		
	}
	*/


}
