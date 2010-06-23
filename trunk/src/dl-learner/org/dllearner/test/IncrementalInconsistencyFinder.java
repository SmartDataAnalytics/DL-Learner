package org.dllearner.test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.kb.extraction.ExtractionAlgorithm;
import org.dllearner.tools.ore.explanation.ExplanationGenerator;
import org.dllearner.tools.ore.explanation.PelletExplanationGenerator;
import org.dllearner.utilities.JamonMonitorLogger;
import org.dllearner.utilities.owl.OWLVocabulary;
import org.mindswap.pellet.PelletOptions;
import org.semanticweb.HermiT.Reasoner.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.clarkparsia.owlapi.explanation.BlackBoxExplanation;
import com.clarkparsia.owlapi.explanation.HSTExplanationGenerator;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.jamonapi.Monitor;

public class IncrementalInconsistencyFinder {
	
	private static Logger logger = Logger.getRootLogger();
	
	private static final String ENDPOINT_URL = "http://dbpedia-live.openlinksw.com/sparql";
	private static String DEFAULT_GRAPH_URI = "http://dbpedia.org";
//	private static final String ENDPOINT_URL = "http://localhost:8890/sparql";
//	private static String DEFAULT_GRAPH_URI = "http://opencyc2.org";
	
	private static int RESULT_LIMIT = 100;
	private static int RECURSION_DEPTH = 3;
	
	//stop if algorithm founds unsatisfiable class or ontology is inconsistent
	private static boolean BREAK_AFTER_ERROR_FOUND = true;
	
	private OWLOntology ontology;
	private OWLOntologyManager manager;
	private OWLDataFactory factory;
	private OWLReasoner reasoner;
	
	private String endpointURI;
	
	private Monitor overallMonitor = JamonMonitorLogger.getTimeMonitor(ExtractionAlgorithm.class, "Overall monitor");
	private Monitor queryMonitor = JamonMonitorLogger.getTimeMonitor(ExtractionAlgorithm.class, "Query monitor");
	private Monitor reasonerMonitor = JamonMonitorLogger.getTimeMonitor(ExtractionAlgorithm.class, "Reasoning monitor");
	
	public IncrementalInconsistencyFinder() throws OWLOntologyCreationException, IOException{
		manager = OWLManager.createOWLOntologyManager();
		ontology = manager.createOntology();
		factory = manager.getOWLDataFactory();
		
		SimpleLayout layout = new SimpleLayout();
		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		DailyRollingFileAppender fileAppender = new DailyRollingFileAppender(layout, "log/incremental.log", "'.'yyyy-MM-dd_HH");
		logger.removeAllAppenders();
		logger.addAppender(consoleAppender);
		logger.addAppender(fileAppender);
		logger.setLevel(Level.INFO);
		
	}
	
	/**
	 * THis method checks incrementally for unsatisfiable classes in the knowledgebase.
	 * @param endpointURI
	 */
	private void checkForUnsatisfiableClasses(String endpointURI){
		this.endpointURI = endpointURI;
		logger.info("Searching for unsatisfiable classes in " + endpointURI);
		
//		reasoner = PelletIncremantalReasonerFactory.getInstance().createReasoner(ontology);
		ReasonerFactory reasonerFactory = new ReasonerFactory();
		reasoner = reasonerFactory.createNonBufferingReasoner(ontology);
		
		overallMonitor.reset();
		reasonerMonitor.reset();
		queryMonitor.reset();
		overallMonitor.start();
		
		
		Set<OWLClass> visitedClasses = new HashSet<OWLClass>();
		Set<OWLClass> classesToVisit = new HashSet<OWLClass>();
		Set<OWLClass> tmp = new HashSet<OWLClass>();
		
		//we are starting with disjointClasses axioms
		Set<OWLDisjointClassesAxiom> disjointAxioms = retrieveDisjointClassAxioms();
		manager.addAxioms(ontology, disjointAxioms);
		
		for(OWLDisjointClassesAxiom ax : disjointAxioms){
			for(OWLClassExpression cl : ax.getClassExpressions()){
				if(!cl.isAnonymous()){
					classesToVisit.add(cl.asOWLClass());
				}
			}
		}
		boolean foundUnsatisfiableClasses = false;
		Set<OWLClass> unsatClasses;
		logger.info("Starting with " + classesToVisit.size() + " classes to visit.");
		for(int i = 1; i <= RECURSION_DEPTH; i++){
			logger.info("Recursion depth = " + i);
			for(OWLClass cl : classesToVisit){
				logger.info("Starting retrieving axioms for class " + cl);
				Set<OWLAxiom> axioms = retrieveAxiomsForClass(cl, false);
//				Set<OWLAxiom> axioms = retrieveAxiomsForClassSingleQuery(cl, false);
				manager.addAxioms(ontology, axioms);
//				if(!axioms.isEmpty()){
//					logger.info("Checking for unsatisfiable classes");
//					reasonerMonitor.start();
//					unsatClasses = reasoner.getUnsatisfiableClasses().getEntities();
//					foundUnsatisfiableClasses = !unsatClasses.isEmpty();
//					logger.info("Found " + unsatClasses.size() + " unsatisfiable classes");
//					reasonerMonitor.stop();
//					
//				}
				for(OWLAxiom ax : axioms){
					tmp.addAll(ax.getClassesInSignature());
				}
				if(foundUnsatisfiableClasses && BREAK_AFTER_ERROR_FOUND){
					logger.info("Found unsatisfiable classes. Aborting.");
					break;
				}
			}
			if(foundUnsatisfiableClasses && BREAK_AFTER_ERROR_FOUND){
				break;
			}
			visitedClasses.addAll(classesToVisit);
			tmp.removeAll(visitedClasses);
			classesToVisit.clear();
			classesToVisit.addAll(tmp);
			tmp.clear();
		}
		

		overallMonitor.stop();
		showStats();
		
		Node<OWLClass> unsatisfiableClasses = reasoner.getUnsatisfiableClasses();
		logger.info("Found " + unsatisfiableClasses.getSize() + " unsatisfiable class(es):");
		logger.info(unsatisfiableClasses.getEntities());
		if(!unsatisfiableClasses.getEntities().isEmpty()){
			BlackBoxExplanation expGen = new BlackBoxExplanation(ontology, reasonerFactory, reasoner);
			HSTExplanationGenerator hstExpGen = new HSTExplanationGenerator(expGen);
			logger.info(hstExpGen.getExplanations(unsatisfiableClasses.getRepresentativeElement()));
		}
		
		
	}
	
	/**
	 * This method checks incrementally the consistency of the knowledgebase.
	 * @param endpointURI
	 */
	private void checkForInconsistency(String endpointURI){
		this.endpointURI = endpointURI;
		logger.info("Searching for inconsistency in " + endpointURI);
		
		PelletOptions.USE_COMPLETION_QUEUE = true;
		PelletOptions.USE_INCREMENTAL_CONSISTENCY = true;
		PelletOptions.USE_SMART_RESTORE = false;
		
		OWLReasoner reasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner(ontology);
		
		overallMonitor.reset();
		reasonerMonitor.reset();
		queryMonitor.reset();
		overallMonitor.start();
		
		Set<OWLClass> visitedClasses = new HashSet<OWLClass>();
		Set<OWLObjectProperty> visitedObjectProperties = new HashSet<OWLObjectProperty>();
		Set<OWLNamedIndividual> visitedIndividuals = new HashSet<OWLNamedIndividual>();
		Set<OWLClass> classesToVisit = new HashSet<OWLClass>();
		Set<OWLClass> tmp = new HashSet<OWLClass>();
		
		//we are starting with disjointClasses axioms
		Set<OWLDisjointClassesAxiom> disjointAxioms = retrieveDisjointClassAxioms();
		manager.addAxioms(ontology, disjointAxioms);
		
		for(OWLDisjointClassesAxiom ax : disjointAxioms){
			for(OWLClassExpression cl : ax.getClassExpressions()){
				if(!cl.isAnonymous()){
					classesToVisit.add(cl.asOWLClass());
				}
			}
		}
		boolean isConsistent = true;
		Set<OWLAxiom> axioms;
		logger.info("Starting with " + classesToVisit.size() + " classes to visit.");
		for(int i = 1; i <= RECURSION_DEPTH; i++){
			logger.info("Recursion depth = " + i);
			//we retrieve axioms for each class
			for(OWLClass cl : classesToVisit){
				axioms = retrieveAxiomsForClass(cl);
				manager.addAxioms(ontology, axioms);
				if(!axioms.isEmpty()){
					logger.info("Checking for consistency");
					reasonerMonitor.start();
					isConsistent = reasoner.isConsistent();
					reasonerMonitor.stop();
					
				}
				for(OWLAxiom ax : axioms){
					tmp.addAll(ax.getClassesInSignature());
				}
				if(!isConsistent && BREAK_AFTER_ERROR_FOUND){
					logger.info("Detected inconsistency. Aborting.");
					break;
				}
				
			}
			//we retrieve axioms for each individual in the ontology
			logger.info("Retrieving axioms for " + ontology.getIndividualsInSignature().size() + " instances");
			int cnt = 0;
			for(OWLNamedIndividual ind : ontology.getIndividualsInSignature()){
				if(!visitedIndividuals.contains(ind)){
					manager.addAxioms(ontology, retrieveAxiomsForIndividual(ind));
					visitedIndividuals.add(ind);
					logger.info("Checking for consistency");
					reasonerMonitor.start();
					isConsistent = reasoner.isConsistent();
					reasonerMonitor.stop();
				}
				if(!isConsistent && BREAK_AFTER_ERROR_FOUND){
					logger.info("Detected inconsistency. Aborting.");
					break;
				}
				cnt++;
				if(cnt == 100){
					break;
				}
				
			}
			cnt = 0;
			//we retrieve axioms for each object property in the ontology
			logger.info("Retrieving axioms for " + ontology.getObjectPropertiesInSignature().size() + " properties");
			for(OWLObjectProperty prop : ontology.getObjectPropertiesInSignature()){
				if(!visitedObjectProperties.contains(prop)){
					manager.addAxioms(ontology, retrieveAxiomsForObjectProperty(prop));
					visitedObjectProperties.add(prop);
					logger.info("Checking for consistency");
					reasonerMonitor.start();
					isConsistent = reasoner.isConsistent();
					reasonerMonitor.stop();
				}
				if(!isConsistent && BREAK_AFTER_ERROR_FOUND){
					logger.info("Detected inconsistency. Aborting.");
					break;
				}
				cnt++;
				if(cnt == 100){
					break;
				}
				
			}
			if(!isConsistent && BREAK_AFTER_ERROR_FOUND){
				break;
			}
			visitedClasses.addAll(classesToVisit);
			tmp.removeAll(visitedClasses);
			classesToVisit.clear();
			classesToVisit.addAll(tmp);
			tmp.clear();
		}
		
		overallMonitor.stop();
		//show some statistics
		showStats();

		//compute an explanation
		if(!reasoner.isConsistent()){
			ExplanationGenerator expGen = new PelletExplanationGenerator(ontology);
			logger.info(expGen.getExplanation(factory.getOWLSubClassOfAxiom(factory.getOWLThing(), factory.getOWLNothing())));
		}
	}
	
	private Set<OWLDisjointClassesAxiom> retrieveDisjointClassAxioms(){
		logger.info("Retrieving disjointClasses axioms");
		queryMonitor.start();
		
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * WHERE {");
		sb.append("?subject ").append("<").append(OWL.disjointWith).append(">").append(" ?object");
		sb.append("}");
		sb.append("LIMIT ").append(RESULT_LIMIT);
		System.out.println(sb);
		Query query = QueryFactory.create(sb.toString());
		QueryExecution sparqlQueryExec = QueryExecutionFactory.sparqlService(endpointURI, query, DEFAULT_GRAPH_URI);
		ResultSet sparqlResults = sparqlQueryExec.execSelect();
		
		Set<OWLDisjointClassesAxiom> axioms = new HashSet<OWLDisjointClassesAxiom>();
		
		QuerySolution solution;
		RDFNode rdfNodeSubject;
		RDFNode rdfNodeObject;
		OWLClass disjointClass1;
		OWLClass disjointClass2;
		while(sparqlResults.hasNext()){
			solution = sparqlResults.nextSolution();
			
			rdfNodeSubject = solution.getResource("?subject");
			rdfNodeObject = solution.getResource("?object");
			
			//skip if solution contains blank node
			if(rdfNodeSubject.isAnon() || rdfNodeObject.isAnon()){
				continue;
			}
			
			disjointClass1 = factory.getOWLClass(IRI.create(rdfNodeSubject.toString()));
			disjointClass2 = factory.getOWLClass(IRI.create(rdfNodeObject.toString()));
			
			axioms.add(factory.getOWLDisjointClassesAxiom(disjointClass1, disjointClass2));
		}
		queryMonitor.stop();
		logger.info("Found " + axioms.size() + " axioms in " + queryMonitor.getLastValue() + " ms");
		return axioms;
	}
	
	/**
	 * Get axioms for a given class.
	 * Axiom types: SubClassOf, EquivalentClass, ClassAssertion
	 * 
	 * @param cl
	 * @return
	 */
	private Set<OWLAxiom> retrieveAxiomsForClass(OWLClass cl){
		logger.info("Retrieving axioms for class " + cl);
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		queryMonitor.start();
		
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * WHERE {");
		sb.append("{<").append(cl.toStringID()).append("> ").append("?predicate").append(" ?object.}");
		sb.append(" UNION ");
		sb.append("{?subject").append("?predicate").append(" <").append(cl.toStringID()).append("> }");
		sb.append("}");
		sb.append("LIMIT ").append(RESULT_LIMIT);
		
		Query query = QueryFactory.create(sb.toString());
		QueryExecution queryExec = QueryExecutionFactory.sparqlService(endpointURI, query, DEFAULT_GRAPH_URI);
		ResultSet results = queryExec.execSelect();
		QuerySolution solution;
		RDFNode rdfNodeSubject;
		RDFNode rdfNodePredicate;
		RDFNode rdfNodeObject;
		while(results.hasNext()){
			solution = results.nextSolution();
			if(solution.get("?object") != null){
				rdfNodePredicate = solution.getResource("?predicate");
				rdfNodeObject = solution.get("?object");
				//skip if object is a blank node
				if(rdfNodeObject.isAnon()){
					continue;
				}
				if(rdfNodePredicate.equals(RDFS.subClassOf)){
					axioms.add(factory.getOWLSubClassOfAxiom(cl, factory.getOWLClass(IRI.create(rdfNodeObject.toString()))));
				} else if(rdfNodePredicate.equals(OWL.equivalentClass)){
					axioms.add(factory.getOWLEquivalentClassesAxiom(cl, factory.getOWLClass(IRI.create(rdfNodeObject.toString()))));
				}
			} else if(solution.get("?subject") != null){
				rdfNodePredicate = solution.getResource("?predicate");
				rdfNodeSubject = solution.get("?subject");
				//skip if subject is a blank node
				if(rdfNodeSubject.isAnon()){
					continue;
				}
				if(rdfNodePredicate.equals(RDF.type)){
					axioms.add(factory.getOWLClassAssertionAxiom(cl, factory.getOWLNamedIndividual(IRI.create(rdfNodeSubject.toString()))));
				} else if(rdfNodePredicate.equals(RDFS.subClassOf)){
					axioms.add(factory.getOWLSubClassOfAxiom(factory.getOWLClass(IRI.create(rdfNodeSubject.toString())), cl));
				}
			}
			
			
		}
		if(axioms.isEmpty()){
			axioms.addAll(getAxiomsFromLinkedDataSource(cl.getIRI()));
		}
		queryMonitor.stop();
		logger.info("Found " + axioms.size() + " axioms in " + queryMonitor.getLastValue() + " ms");
		return axioms;
	}
	
	/**
	 * Get axioms for a given ObjectProperty.
	 * Axiom types: TransitiveProperty, FunctionalProperty, InverseFunctionalProperty, SymmetricProperty, EquivalentProperty,
	 * SubProperty, InverseOf, Domain, Range
	 * 
	 * @param prop
	 * @return
	 */
	private Set<OWLAxiom> retrieveAxiomsForObjectProperty(OWLObjectProperty prop){
		logger.info("Retrieving axioms for property " + prop);
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		queryMonitor.start();
		
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * WHERE {");
		sb.append("<").append(prop.toStringID()).append("> ").append("?predicate").append(" ?object.}");
		sb.append("LIMIT ").append(RESULT_LIMIT);
		
		Query query = QueryFactory.create(sb.toString());
		QueryExecution queryExec = QueryExecutionFactory.sparqlService(endpointURI, query, DEFAULT_GRAPH_URI);
		ResultSet results = queryExec.execSelect();
		QuerySolution solution;
		RDFNode rdfNodePredicate;
		RDFNode rdfNodeObject;
		while(results.hasNext()){
			solution = results.nextSolution();
			rdfNodePredicate = solution.getResource("?predicate");
			rdfNodeObject = solution.get("?object");
			//skip if object is a blank node
			if(rdfNodeObject.isAnon()){
				continue;
			}
			if(rdfNodePredicate.equals(RDF.type)){
				if(rdfNodeObject.equals(OWL.TransitiveProperty)){
					axioms.add(factory.getOWLTransitiveObjectPropertyAxiom(prop));
				} else if(rdfNodeObject.equals(OWL.FunctionalProperty)){
					axioms.add(factory.getOWLFunctionalObjectPropertyAxiom(prop));
				} else if(rdfNodeObject.equals(OWL.InverseFunctionalProperty)){
					axioms.add(factory.getOWLInverseFunctionalObjectPropertyAxiom(prop));
				} else if(rdfNodeObject.equals(OWL.SymmetricProperty)){
					axioms.add(factory.getOWLSymmetricObjectPropertyAxiom(prop));
				}
			} else if(rdfNodePredicate.equals(RDFS.subPropertyOf)){
				axioms.add(factory.getOWLSubObjectPropertyOfAxiom(prop,
						factory.getOWLObjectProperty(IRI.create(rdfNodeObject.toString()))));
			} else if(rdfNodePredicate.equals(OWL.inverseOf)){
				axioms.add(factory.getOWLInverseObjectPropertiesAxiom(prop,
						factory.getOWLObjectProperty(IRI.create(rdfNodeObject.toString()))));
			} else if(rdfNodePredicate.equals(RDFS.domain)){
				axioms.add(factory.getOWLObjectPropertyDomainAxiom(prop,
						factory.getOWLClass(IRI.create(rdfNodeObject.toString()))));
			} else if(rdfNodePredicate.equals(RDFS.range)){
				axioms.add(factory.getOWLObjectPropertyRangeAxiom(prop,
						factory.getOWLClass(IRI.create(rdfNodeObject.toString()))));
			} else if(rdfNodePredicate.equals(OWL.equivalentProperty)){
				axioms.add(factory.getOWLEquivalentObjectPropertiesAxiom(prop,
						factory.getOWLObjectProperty(IRI.create(rdfNodeObject.toString()))));
			}
			
		}
		if(axioms.isEmpty()){
			axioms.addAll(getAxiomsFromLinkedDataSource(prop.getIRI()));
		}
		queryMonitor.stop();
		logger.info("Found " + axioms.size() + " axioms in " + queryMonitor.getLastValue() + " ms");
		return axioms;
	}

	/**
	 * Retrieve axioms for a given individual.
	 * Axiom types: SameAs, DifferentFrom, ClassAssertion, ObjectPropertyAssertion, DataPropertyAssertion
	 * @param ind
	 * @return
	 */
	private Set<OWLAxiom> retrieveAxiomsForIndividual(OWLNamedIndividual ind){
		logger.info("Retrieving axioms for individual " + ind);
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		queryMonitor.start();
		
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * WHERE {");
		sb.append("<").append(ind.toStringID()).append("> ").append("?predicate").append(" ?object.}");
		sb.append("LIMIT ").append(RESULT_LIMIT);
		
		Query query = QueryFactory.create(sb.toString());
		QueryExecution queryExec = QueryExecutionFactory.sparqlService(endpointURI, query, DEFAULT_GRAPH_URI);
		ResultSet results = queryExec.execSelect();
		QuerySolution solution;
		RDFNode rdfNodePredicate;
		RDFNode rdfNodeObject;
		while(results.hasNext()){
			solution = results.nextSolution();
			rdfNodePredicate = solution.getResource("?predicate");
			rdfNodeObject = solution.get("?object");
			//skip if object is a blank node
			if(rdfNodeObject.isAnon()){
				continue;
			}
			if(rdfNodePredicate.equals(RDF.type)){
				axioms.add(factory.getOWLClassAssertionAxiom(factory.getOWLClass(IRI.create(rdfNodeObject.toString())), ind));
			} else if(rdfNodePredicate.equals(OWL.sameAs)){
				axioms.add(factory.getOWLSameIndividualAxiom(ind, factory.getOWLNamedIndividual(IRI.create(rdfNodeObject.toString()))));
			} else if(rdfNodePredicate.equals(OWL.differentFrom)){
				axioms.add(factory.getOWLDifferentIndividualsAxiom(ind, factory.getOWLNamedIndividual(IRI.create(rdfNodeObject.toString()))));
			} else if(rdfNodeObject.isLiteral()){
				if(rdfNodeObject.equals(RDFS.comment)){
					
				} else if(rdfNodeObject.equals(RDFS.label)){
					
				} else {
					axioms.add(factory.getOWLDataPropertyAssertionAxiom(
							factory.getOWLDataProperty(IRI.create(rdfNodePredicate.toString())),
							ind,
							rdfNodeObject.toString()));
				}
				
			} else if(rdfNodeObject.isResource()){
				axioms.add(factory.getOWLObjectPropertyAssertionAxiom(
						factory.getOWLObjectProperty(IRI.create(rdfNodePredicate.toString())),
						ind,
						factory.getOWLNamedIndividual(IRI.create(rdfNodeObject.toString()))));
			}
		}
		if(axioms.isEmpty()){
			axioms.addAll(getAxiomsFromLinkedDataSource(ind.getIRI()));
		}
		queryMonitor.stop();
		logger.info("Found " + axioms.size() + " axioms in " + queryMonitor.getLastValue() + " ms");
		return axioms;
	}
	
	
	
	private Set<OWLAxiom> retrieveSubClassAxioms(OWLClass cl){
		logger.info("Retrieving subClassOf axioms for class " + cl);
		queryMonitor.start();
		
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		QuerySolution solution;
		RDFNode rdfNode;
		
		Query sparqlQuery = createSimpleSelectSPARQLQuery("?x", OWLVocabulary.RDFS_SUBCLASS_OF,
				cl.toStringID(), "regex(?x,\"http://dbpedia.org/ontology/\", \"i\")", RESULT_LIMIT);
		QueryExecution sparqlQueryExec = QueryExecutionFactory.sparqlService(endpointURI, sparqlQuery, DEFAULT_GRAPH_URI);
		ResultSet sparqlResults = sparqlQueryExec.execSelect();
		if(!sparqlResults.hasNext()){
			logger.info("Got empty SPARQL result. Trying to get informations from linked data uri.");
			try{
				axioms.addAll(manager.loadOntology(IRI.create(cl.toStringID())).getLogicalAxioms());
			} catch (Exception e){
				logger.info("No linked data retrieved.");
			}
		} else {
			OWLClass subClass;
			while(sparqlResults.hasNext()){
				solution = sparqlResults.nextSolution();
				
				rdfNode = solution.getResource("?x");
				if(rdfNode.isAnon()){
					System.out.println("BLANKNODE detected in solution " + solution);
					continue;
				}
				
				subClass = factory.getOWLClass(IRI.create(rdfNode.toString()));
				
				axioms.add(factory.getOWLSubClassOfAxiom(subClass, cl));
			}
		}
		
		queryMonitor.stop();
		logger.info("Found " + axioms.size() + " axioms in " + queryMonitor.getLastValue() + " ms");
		return axioms;
	}
	
	private Set<OWLObjectPropertyDomainAxiom> retrievePropertyDomainAxioms(OWLClass cl){
		logger.info("Retrieving objectPropertyDomain axioms for class " + cl);
		queryMonitor.start();
		
		Query sparqlQuery = createSimpleSelectSPARQLQuery("?s", OWLVocabulary.RDFS_domain, cl.toStringID(),
				"regex(?s,\"http://dbpedia.org/ontology/\", \"i\")", RESULT_LIMIT);
		QueryExecution sparqlQueryExec = QueryExecutionFactory.sparqlService(endpointURI, sparqlQuery, DEFAULT_GRAPH_URI);
		ResultSet sparqlResults = sparqlQueryExec.execSelect();
		
		Set<OWLObjectPropertyDomainAxiom> axioms = new HashSet<OWLObjectPropertyDomainAxiom>();
		
		QuerySolution solution;
		RDFNode rdfNodeSubject;
		OWLObjectProperty property;
		while(sparqlResults.hasNext()){
			solution = sparqlResults.nextSolution();
			
			rdfNodeSubject = solution.getResource("?s");
			
			property = factory.getOWLObjectProperty(IRI.create(rdfNodeSubject.toString()));
			
			axioms.add(factory.getOWLObjectPropertyDomainAxiom(property, cl));
		}
		queryMonitor.stop();
		logger.info("Found " + axioms.size() + " axioms in " + queryMonitor.getLastValue() + " ms");
		return axioms;
	}
	
	private Set<OWLObjectPropertyRangeAxiom> retrievePropertyRangeAxioms(OWLClass cl){
		logger.info("Retrieving objectPropertyRange axioms for class " + cl);
		queryMonitor.start();
		
		Query sparqlQuery = createSimpleSelectSPARQLQuery("?s", OWLVocabulary.RDFS_range, cl.toStringID(),
				"regex(?s,\"http://dbpedia.org/ontology/\", \"i\")", RESULT_LIMIT);
		QueryExecution sparqlQueryExec = QueryExecutionFactory.sparqlService(endpointURI, sparqlQuery, DEFAULT_GRAPH_URI);
		ResultSet sparqlResults = sparqlQueryExec.execSelect();
		
		Set<OWLObjectPropertyRangeAxiom> axioms = new HashSet<OWLObjectPropertyRangeAxiom>();
		
		QuerySolution solution;
		RDFNode rdfNodeSubject;
		OWLObjectProperty property;
		while(sparqlResults.hasNext()){
			solution = sparqlResults.nextSolution();
			
			rdfNodeSubject = solution.getResource("?s");
			
			property = factory.getOWLObjectProperty(IRI.create(rdfNodeSubject.toString()));
			
			axioms.add(factory.getOWLObjectPropertyRangeAxiom(property, cl));
		}
		queryMonitor.stop();
		logger.info("Found " + axioms.size() + " axioms in " + queryMonitor.getLastValue() + " ms");
		return axioms;
	}
	
	private Set<OWLClassAssertionAxiom> retrieveClassAssertionAxioms(OWLClass cl){
		logger.info("Retrieving classAssertion axioms for class " + cl);
		queryMonitor.start();
		
		Query sparqlQuery = createSimpleSelectSPARQLQuery("?s", OWLVocabulary.RDF_TYPE, cl.toStringID(),
				null, RESULT_LIMIT);
		QueryExecution sparqlQueryExec = QueryExecutionFactory.sparqlService(endpointURI, sparqlQuery, DEFAULT_GRAPH_URI);
		ResultSet sparqlResults = sparqlQueryExec.execSelect();
		
		Set<OWLClassAssertionAxiom> axioms = new HashSet<OWLClassAssertionAxiom>();
		
		QuerySolution solution;
		RDFNode rdfNodeSubject;
		OWLNamedIndividual individual;
		while(sparqlResults.hasNext()){
			solution = sparqlResults.nextSolution();
			
			rdfNodeSubject = solution.getResource("?s");
			
			individual = factory.getOWLNamedIndividual(IRI.create(rdfNodeSubject.toString()));
			
			axioms.add(factory.getOWLClassAssertionAxiom(cl, individual));
		}
		queryMonitor.stop();
		logger.info("Found " + axioms.size() + " axioms in " + queryMonitor.getLastValue() + " ms");
		return axioms;
	}
	
	private Set<OWLAxiom> retrieveAxiomsForClass(OWLClass cl, boolean retrieveABoxAxioms){
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		axioms.addAll(retrieveSubClassAxioms(cl));
		axioms.addAll(retrievePropertyDomainAxioms(cl));
		axioms.addAll(retrievePropertyRangeAxioms(cl));
		if(retrieveABoxAxioms){
			axioms.addAll(retrieveClassAssertionAxioms(cl));
		}
		return axioms;
	}
	
	private Set<OWLAxiom> retrieveAxiomsForClassSingleQuery(OWLClass cl, boolean retrieveABoxAxioms){
		logger.info("Retrieving axioms for class " + cl);
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		queryMonitor.start();
		
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * WHERE {");
		sb.append("{").append("<").append(cl.toStringID()).append("> <").append(OWLVocabulary.RDFS_SUBCLASS_OF).append("> ?superClass.").append("FILTER regex(?superClass,\"http://dbpedia.org/ontology/\", \"i\")}");
		sb.append("UNION");
		sb.append("{").append("?subClass ").append("<").append(OWLVocabulary.RDFS_SUBCLASS_OF).append("> <").append(cl.toStringID()).append(">.").append("FILTER regex(?subClass,\"http://dbpedia.org/ontology/\", \"i\")}");
		sb.append("UNION");
		sb.append("{").append("?domainProperty ").append("<").append(OWLVocabulary.RDFS_domain).append("> <").append(cl.toStringID()).append(">.").append("FILTER regex(?domainProperty,\"http://dbpedia.org/ontology/\", \"i\")}");
		sb.append("UNION");
		sb.append("{").append("?rangeProperty ").append("<").append(OWLVocabulary.RDFS_range).append("> <").append(cl.toStringID()).append(">.").append("FILTER regex(?rangeProperty,\"http://dbpedia.org/ontology/\", \"i\")}");
		if(retrieveABoxAxioms){
			sb.append("UNION");
			sb.append("{").append("?instance ").append("<").append(OWLVocabulary.RDF_TYPE).append("> <").append(cl.toStringID()).append(">}");
		}
		sb.append("}");
//		sb.append("LIMIT ").append(RESULT_LIMIT);
		
		Query query = QueryFactory.create(sb.toString());
		QueryExecution queryExec = QueryExecutionFactory.sparqlService(endpointURI, query, DEFAULT_GRAPH_URI);
		ResultSet results = queryExec.execSelect();
		QuerySolution solution;
		RDFNode rdfNode;
		while(results.hasNext()){
			solution = results.nextSolution();
			if(solution.varNames().next().equals("subClass")){
				rdfNode = solution.getResource("?subClass");
				axioms.add(factory.getOWLSubClassOfAxiom(factory.getOWLClass(IRI.create(rdfNode.toString())), cl));
			} else if(solution.varNames().next().equals("superClass")){
				rdfNode = solution.getResource("?superClass");
				axioms.add(factory.getOWLSubClassOfAxiom(cl, factory.getOWLClass(IRI.create(rdfNode.toString()))));
			} else if(solution.varNames().next().equals("domainProperty")){
				rdfNode = solution.getResource("?domainProperty");
				axioms.add(factory.getOWLObjectPropertyDomainAxiom(factory.getOWLObjectProperty(IRI.create(rdfNode.toString())), cl));
			} else if(solution.varNames().next().equals("rangeProperty")){
				rdfNode = solution.getResource("?rangeProperty");
				axioms.add(factory.getOWLObjectPropertyRangeAxiom(factory.getOWLObjectProperty(IRI.create(rdfNode.toString())), cl));
			} else if(solution.varNames().next().equals("instance")){
				rdfNode = solution.getResource("?instance");
				axioms.add(factory.getOWLClassAssertionAxiom(cl, factory.getOWLNamedIndividual(IRI.create(rdfNode.toString()))));
			} 
			
		}
		queryMonitor.stop();
		logger.info("Found " + axioms.size() + " axioms in " + queryMonitor.getLastValue() + " ms");
		return axioms;
	}
	
	private Set<OWLAxiom> getAxiomsFromLinkedDataSource(IRI iri){
		logger.info("Trying to get informations from linked data uri.");
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		try{
			axioms.addAll(manager.loadOntology(iri).getLogicalAxioms());
		} catch (Exception e){
			logger.info("No linked data retrieved.");
		}
		logger.info("Got " + axioms.size() + " logical axioms from linked data source");
		return axioms;
	}
	
	
	
	private void showStats(){
		logger.info("###########################STATS###########################");
		logger.info(ontology);
		logger.info("Overall execution time: " + overallMonitor.getTotal() + " ms");
		logger.info("Overall query time: " + queryMonitor.getTotal() + " ms (" + (int)(queryMonitor.getTotal()/overallMonitor.getTotal()*100) + "%)");
		logger.info("Number of queries sent: " + (int)queryMonitor.getHits());
		logger.info("Average query time: " + queryMonitor.getAvg() + " ms");
		logger.info("Longest query time: " + queryMonitor.getMax() + " ms");
		logger.info("Shortest query time: " + queryMonitor.getMin() + " ms");
		logger.info("Overall reasoning time: " + reasonerMonitor.getTotal() + " ms (" + (int)(reasonerMonitor.getTotal()/overallMonitor.getTotal()*100) + "%)");
		logger.info("Number of reasoner calls: " + (int)reasonerMonitor.getHits());
		logger.info("Average reasoning time: " + reasonerMonitor.getAvg() + " ms");
		logger.info("Longest reasoning time: " + reasonerMonitor.getMax() + " ms");
		logger.info("Shortest reasoning time: " + reasonerMonitor.getMin() + " ms");
	}
	
	
	private Query createSimpleSelectSPARQLQuery(String subject, String predicate, String object, String filter, int limit){
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT * WHERE {");
		if(!subject.startsWith("?")){
			sb.append("<").append(subject).append(">");
		} else {
			sb.append(subject);
		}
		sb.append(" ");
		if(!predicate.startsWith("?")){
			sb.append("<").append(predicate).append(">");
		} else {
			sb.append(predicate);
		}
		sb.append(" ");
		if(!object.startsWith("?")){
			sb.append("<").append(object).append(">");
		} else {
			sb.append(object);
		}
//		if(filter != null){
//			sb.append(" FILTER ");
//			sb.append(filter);
//		}
		sb.append("}");
		sb.append(" LIMIT ");
		sb.append(limit);
		Query query = QueryFactory.create(sb.toString());
		return query;
	}
	
	public static void main(String[] args) throws OWLOntologyCreationException, IOException{
		IncrementalInconsistencyFinder incFinder = new IncrementalInconsistencyFinder();
//		incFinder.checkForUnsatisfiableClasses(ENDPOINT_URL);
		incFinder.checkForInconsistency(ENDPOINT_URL);
		
//		String queryString = "CONSTRUCT { ?x <" + RDFS.subClassOf + "> ?y } WHERE { ?x <" + RDFS.subClassOf + "> ?y } ORDER BY ?x LIMIT 100 ";
//		Query query = QueryFactory.create(queryString) ;
//		QueryExecution qexec = QueryExecutionFactory.sparqlService(ENDPOINT_URL, query);
//		Model resultModel = qexec.execConstruct() ;
//		qexec.close() ;
//		resultModel.write(System.out);
		
		
	}
	
	

}
